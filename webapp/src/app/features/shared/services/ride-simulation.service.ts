import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';

import { VehiclesApiService } from '../api/vehicles-api.service';
import { RoutingService, LatLng } from './routing.service';
import { resamplePolyline } from '../map/route-utils';
import { VehicleMarker } from '../map/vehicle-marker';
import { DriverRidesService } from '../../driver/services/driver-rides.service';
import { LocationDTO } from '../models/location';

type RideLike = {
  id: number;
  driverEmail: string | null | undefined;
  startLocation: { latitude: number; longitude: number };
  endLocation: { latitude: number; longitude: number };
  waypoints?: { latitude: number; longitude: number }[] | null;
};

@Injectable({ providedIn: 'root' })
export class RideSimulationService {
  private vehiclesSub = new BehaviorSubject<VehicleMarker[]>([]);
  readonly vehicles$ = this.vehiclesSub.asObservable();

  private routePathSub = new BehaviorSubject<LatLng[]>([]);
  readonly routePath$ = this.routePathSub.asObservable();

  private simStartTimeout?: number;
  private simAbort?: AbortController;
  private vehicleSub?: Subscription;

  private onPickupReachedCallback?: () => void;
  private onCompleteCallback?: () => void;
  private currentPhase: 'to-pickup' | 'in-progress' | 'idle' = 'idle';
  readonly stopAfterNextStep: WritableSignal<boolean> = signal(false);
  lastPosition: { lat: number; lng: number } | null = null;
  lastStopLocation: LocationDTO | null = null;

  constructor(
    private vehiclesApi: VehiclesApiService,
    private routing: RoutingService,
  ) {}

  stop(): void {
    if (this.simStartTimeout) window.clearTimeout(this.simStartTimeout);
    this.simStartTimeout = undefined;

    this.simAbort?.abort();
    this.simAbort = undefined;

    this.vehicleSub?.unsubscribe();
    this.vehicleSub = undefined;

    this.vehiclesSub.next([]);
    this.routePathSub.next([]);

    this.onPickupReachedCallback = undefined;
    this.onCompleteCallback = undefined;
    this.currentPhase = 'idle';
  }

  start(
    ride: RideLike,
    opts?: {
      startDelayMs?: number;
      stepMeters?: number;
      tickMs?: number;
    },
    onPickupReached?: () => void,
    onComplete?: () => void
  ): void {
    this.stop();

    if (!ride.driverEmail) {
      console.error('Simulation: missing driverEmail on ride');
      return;
    }

    this.onPickupReachedCallback = onPickupReached;
    this.onCompleteCallback = onComplete;

    const startDelayMs = opts?.startDelayMs ?? 0;
    const stepMeters = opts?.stepMeters ?? 100;
    const tickMs = opts?.tickMs ?? 1000;

    this.simAbort = new AbortController();
    const signal = this.simAbort.signal;

    // ucitaj vozilo
    this.vehicleSub = this.vehiclesApi.getDriverVehicleForMap(ride.driverEmail).subscribe({
      next: (vehicle) => {
        const pickupPoint: LatLng = [ride.startLocation.latitude, ride.startLocation.longitude];
        const currentPoint: LatLng = [vehicle.lat, vehicle.lng];

        // Postavi vozilo na trenutnu poziciju
        const initialVehicle: VehicleMarker = {
          ...vehicle,
          lat: currentPoint[0],
          lng: currentPoint[1],
          status: 'OCCUPIED' // Postavi na OCCUPIED kada startuje
        };
        this.vehiclesSub.next([initialVehicle]);

        // Azuriraj poziciju u backend
        this.vehiclesApi.updateVehiclePosition(vehicle.id, {
          latitude: currentPoint[0],
          longitude: currentPoint[1],
        }).subscribe({ error: (e) => console.error('Position update failed', e) });

        // putanja do pickupa
        this.buildPickupRoutePath(currentPoint, pickupPoint, signal).catch(e => {
          if (e?.name !== 'AbortError') console.error('Pickup route build failed', e);
        });

        // putanja pickup -> waypoints -> dropoff
        this.buildFullRideRoutePath(ride, signal).catch(e => {
          if (e?.name !== 'AbortError') console.error('Full route build failed', e);
        });

        // pokreni simulaciju
        this.simStartTimeout = window.setTimeout(() => {
          void this.runSimulation(vehicle.id, currentPoint, ride, signal, stepMeters, tickMs);
        }, startDelayMs);
      },
      error: (err) => console.error('Failed to load driver vehicle', err),
    });
  }

  private async buildPickupRoutePath(from: LatLng, pickup: LatLng, signal: AbortSignal): Promise<void> {
    const seg = await this.routing.fetchRoute(from, pickup, signal);
    if (!signal.aborted && seg.coordinates.length) {
      this.routePathSub.next(seg.coordinates);
    }
  }

  private async buildFullRideRoutePath(ride: RideLike, signal: AbortSignal): Promise<void> {
    const stops: LatLng[] = [
      [ride.startLocation.latitude, ride.startLocation.longitude],
      ...(ride.waypoints ?? []).map(w => [w.latitude, w.longitude] as LatLng),
      [ride.endLocation.latitude, ride.endLocation.longitude],
    ];

    let full: LatLng[] = [];
    for (let i = 0; i < stops.length - 1; i++) {
      const seg = await this.routing.fetchRoute(stops[i], stops[i + 1], signal);
      const coords = [...seg.coordinates];
      if (i > 0 && coords.length) coords.shift();
      if (coords.length) full = full.concat(coords);
    }

    // Ne setujem ovdje routePath jer prikazujemo prvo samo do pickup-a
    // Full path ce biti prikazan tek kada vozilo stigne do pickup-a
  }

  private async runSimulation(
    vehicleId: number,
    startPoint: LatLng,
    ride: RideLike,
    signal: AbortSignal,
    stepMeters: number,
    tickMs: number,
  ): Promise<void> {
    try {
      const pickupPoint: LatLng = [ride.startLocation.latitude, ride.startLocation.longitude];

      // krecemo se od trenutne pozicije do pickupa
      this.currentPhase = 'to-pickup';
      console.log('Phase 1: Moving to pickup...');

      await this.moveAlongRoute(vehicleId, startPoint, pickupPoint, signal, stepMeters, tickMs);

      if (signal.aborted) return;

      // Stigli smo do pickupa
      console.log('Reached pickup!');
      if (this.onPickupReachedCallback) {
        this.onPickupReachedCallback();
      }

      // Pauza na pickup-u (putnici ulaze)
      await this.sleep(3000);

      // sada idemo kroz waypoints do destinacije
      this.currentPhase = 'in-progress';
      console.log('Phase 2: Moving to destination...');

      const stops: LatLng[] = [
        pickupPoint,
        ...(ride.waypoints ?? []).map(w => [w.latitude, w.longitude] as LatLng),
        [ride.endLocation.latitude, ride.endLocation.longitude],
      ];

      // Azuriraj prikaz rute da prikazuje cijelu rutu od pickup-a do dropoff-a
      await this.buildFullRoutePath(stops, signal);

      // Krecemo se kroz sve stop tacke
      for (let segIdx = 0; segIdx < stops.length - 1; segIdx++) {
        if (signal.aborted) return;

        await this.moveAlongRoute(
          vehicleId,
          stops[segIdx],
          stops[segIdx + 1],
          signal,
          stepMeters,
          tickMs
        );

        // Pauza izmedju stop tacaka
        const isLastStop = segIdx === stops.length - 2;
        if (!isLastStop) {
          await this.sleep(3000);
        }
      }

      // Zavrsili smo
      this.currentPhase = 'idle';
      if (this.onCompleteCallback) {
        this.onCompleteCallback();
      }
    } catch (e: any) {
      if (e?.name === 'AbortError') return;
      console.error('Simulation failed', e);
    }
  }

  private async moveAlongRoute(
    vehicleId: number,
    from: LatLng,
    to: LatLng,
    signal: AbortSignal,
    stepMeters: number,
    tickMs: number
  ): Promise<void> {
    const seg = await this.routing.fetchRoute(from, to, signal);
    const sampled = resamplePolyline(seg.coordinates, stepMeters);
    const path: LatLng[] = sampled.length >= 2 ? sampled : [from, to];

    for (let i = 0; i < path.length; i++) {
      if (signal.aborted) return;

      const [lat, lng] = path[i];
      const current = this.vehiclesSub.value[0];
      if (!current) return;

      const moved: VehicleMarker = { ...current, lat, lng, status: 'OCCUPIED' };
      this.lastPosition = { lat, lng };
      this.lastStopLocation = { 
        latitude: lat, 
        longitude: lng,
        address: "adresa nije dostupna"
      }; 
      this.vehiclesSub.next([moved]);

      this.vehiclesApi.updateVehiclePosition(vehicleId, {
        latitude: lat,
        longitude: lng,
      }).subscribe({ error: (e) => console.error('Position update failed', e) });

      if (this.stopAfterNextStep()) {
        this.stopAfterNextStep.set(false);
        this.simAbort?.abort();  // Trigger clean stop
        break;  // Exit this segment
      }

      await this.sleep(tickMs);
    }
  }

  private async buildFullRoutePath(stops: LatLng[], signal: AbortSignal): Promise<void> {
    let full: LatLng[] = [];
    for (let i = 0; i < stops.length - 1; i++) {
      const seg = await this.routing.fetchRoute(stops[i], stops[i + 1], signal);
      const coords = [...seg.coordinates];
      if (i > 0 && coords.length) coords.shift();
      if (coords.length) full = full.concat(coords);
    }
    if (!signal.aborted) this.routePathSub.next(full);
  }

  private sleep(ms: number): Promise<void> {
    return new Promise(res => setTimeout(res, ms));
  }

  getCurrentPhase(): 'to-pickup' | 'in-progress' | 'idle' {
    return this.currentPhase;
  }
}

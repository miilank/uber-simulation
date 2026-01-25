import { Injectable } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';

import { VehiclesApiService } from '../api/vehicles-api.service';
import { RoutingService, LatLng } from './routing.service';
import { resamplePolyline } from '../map/route-utils';
import { VehicleMarker } from '../map/vehicle-marker';

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

  private onCompleteCallback?: () => void;

  constructor(
    private vehiclesApi: VehiclesApiService,
    private routing: RoutingService,
  ) {}

  // zaustavlja simulaciju
  stop(): void {
    if (this.simStartTimeout) window.clearTimeout(this.simStartTimeout);
    this.simStartTimeout = undefined;

    this.simAbort?.abort();
    this.simAbort = undefined;

    this.vehicleSub?.unsubscribe();
    this.vehicleSub = undefined;

    this.vehiclesSub.next([]);
    this.routePathSub.next([]);

    this.onCompleteCallback = undefined;
  }

  // pokrece simulaciju
  start(ride: RideLike, opts?: { startDelayMs?: number; stepMeters?: number; tickMs?: number; }, onComplete?: () => void): void {
    this.stop();

    if (!ride.driverEmail) {
      console.error('Simulation: missing driverEmail on ride');
      return;
    }

    this.onCompleteCallback = onComplete;

    const startDelayMs = opts?.startDelayMs ?? 30_000;
    const stepMeters = opts?.stepMeters ?? 100;
    const tickMs = opts?.tickMs ?? 1000;

    const stops: LatLng[] = [
      [ride.startLocation.latitude, ride.startLocation.longitude],
      ...(ride.waypoints ?? []).map(w => [w.latitude, w.longitude] as LatLng),
      [ride.endLocation.latitude, ride.endLocation.longitude],
    ];

    // Abort controller za sve async fetchRoute pozive
    this.simAbort = new AbortController();
    const signal = this.simAbort.signal;

    // ucitaj vehicle marker
    this.vehicleSub = this.vehiclesApi.getDriverVehicleForMap(ride.driverEmail).subscribe({
      next: (v) => {
        // inicijalno na pickup
        const initial: VehicleMarker = { ...v, lat: stops[0][0], lng: stops[0][1] };
        this.vehiclesSub.next([initial]);

        // odmah upisi poziciju i u backend
        this.vehiclesApi.updateVehiclePosition(initial.id, {
          latitude: initial.lat,
          longitude: initial.lng,
        }).subscribe({ error: (e) => console.error('Position update failed', e) });

        // izgradi rutu
        this.buildFullRoutePath(stops, signal).catch(e => {
          if (e?.name !== 'AbortError') console.error('Full route build failed', e);
        });

        // start simulation
        this.simStartTimeout = window.setTimeout(() => {
          void this.runMoves(initial.id, stops, signal, stepMeters, tickMs);
        }, startDelayMs);
      },
      error: (err) => console.error('Failed to load driver vehicle', err),
    });
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

  private async runMoves(
    vehicleId: number,
    stops: LatLng[],
    signal: AbortSignal,
    stepMeters: number,
    tickMs: number,
  ): Promise<void> {
    try {
      for (let segIdx = 0; segIdx < stops.length - 1; segIdx++) {
        const from = stops[segIdx];
        const to = stops[segIdx + 1];

        const seg = await this.routing.fetchRoute(from, to, signal);
        const sampled = resamplePolyline(seg.coordinates, stepMeters);
        const path: LatLng[] = sampled.length >= 2 ? sampled : ([from, to] as LatLng[]);

        for (let i = 0; i < path.length; i++) {
          if (signal.aborted) return;

          const [lat, lng] = path[i];
          const current = this.vehiclesSub.value[0];
          if (!current) return;

          const moved: VehicleMarker = { ...current, lat, lng };
          this.vehiclesSub.next([moved]);

          this.vehiclesApi.updateVehiclePosition(vehicleId, {
            latitude: lat,
            longitude: lng,
          }).subscribe({ error: (e) => console.error('Position update failed', e) });

          await this.sleep(tickMs);
        }

        const isLastStop = segIdx === stops.length - 2;
        if (!isLastStop) {
          await this.sleep(3000);
        }
      }
      if (this.onCompleteCallback) {
        this.onCompleteCallback();
      }
    } catch (e: any) {
      if (e?.name === 'AbortError') return;
      console.error('Simulation failed', e);
    }
  }

  private sleep(ms: number): Promise<void> {
    return new Promise(res => setTimeout(res, ms));
  }
}

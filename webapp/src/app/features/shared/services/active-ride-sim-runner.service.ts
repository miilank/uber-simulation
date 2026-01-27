import { Injectable, Injector } from '@angular/core';
import { RideSimulationService } from './ride-simulation.service';
import { RideApiService } from '../api/ride-api.service';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { VehicleMarker } from '../map/vehicle-marker';
import { LatLng } from './routing.service';
import { VehiclesApiService } from '../api/vehicles-api.service';
import { RoutingService } from './routing.service';
import { LocationDTO } from '../models/location';

type RideLike = {
  id: number;
  driverEmail: string | null | undefined;
  startLocation: { latitude: number; longitude: number };
  endLocation: { latitude: number; longitude: number };
  waypoints?: { latitude: number; longitude: number }[] | null;
  status?: string;
};

interface SimulationInstance {
  rideId: number;
  simulator: RideSimulationService;
}

@Injectable({ providedIn: 'root' })
export class ActiveRideSimRunnerService {
  private simulations = new Map<number, SimulationInstance>();

  private pickupReachedSubject = new Subject<number>();
  readonly onPickupReached$ = this.pickupReachedSubject.asObservable();

  private completionSubject = new Subject<number>();
  readonly onSimulationComplete$ = this.completionSubject.asObservable();

  constructor(
    private rideApi: RideApiService,
    private vehiclesApi: VehiclesApiService,
    private routing: RoutingService
  ) {}

  startForRide(ride: RideLike): void {
    if (!ride?.driverEmail) return;

    // Ako simulacija vec postoji za ovaj ride, ne pokreci ponovo
    if (this.simulations.has(ride.id)) {
      console.log(`Simulation already running for ride ${ride.id}`);
      return;
    }

    // Kreiraj novu instancu simulatora sa injectovanim dependencies
    const simulator = new RideSimulationService(this.vehiclesApi, this.routing);

    const instance: SimulationInstance = {
      rideId: ride.id,
      simulator,
    };

    this.simulations.set(ride.id, instance);

    // Pokreni simulaciju
    simulator.start(
      ride,
      {
        startDelayMs: 0,
        stepMeters: 100,
        tickMs: 1000
      },
      () => {
        // Stigli do pickup-a
        this.rideApi.arrivedAtPickup(ride.id).subscribe({
          next: () => {
            console.log(`Backend notified: arrived at pickup for ride ${ride.id}`);
            this.pickupReachedSubject.next(ride.id);
          },
          error: (err) => console.error('Failed to notify pickup arrival', err)
        });
      },
      () => {
        // Stigli do destinacije - automatski stopuj simulaciju
        console.log(`Simulation completed for ride ${ride.id}`);
        this.completionSubject.next(ride.id);
      }
    );

    console.log(`Started simulation for ride ${ride.id}`);
  }

  stopForRide(rideId: number): void {
    const instance = this.simulations.get(rideId);
    if (!instance) return;

    instance.simulator.stop();
    this.simulations.delete(rideId);

    console.log(`Stopped simulation for ride ${rideId}`);
  }

  getVehicles$(rideId: number): Observable<VehicleMarker[]> | null {
    const instance = this.simulations.get(rideId);
    return instance ? instance.simulator.vehicles$ : null;
  }

  getRoutePath$(rideId: number): Observable<LatLng[]> | null {
    const instance = this.simulations.get(rideId);
    return instance ? instance.simulator.routePath$ : null;
  }

  getCurrentPhase(rideId: number): 'to-pickup' | 'in-progress' | 'idle' {
    const instance = this.simulations.get(rideId);
    return instance ? instance.simulator.getCurrentPhase() : 'idle';
  }

  isRunning(rideId: number): boolean {
    return this.simulations.has(rideId);
  }

  stopAll(): void {
    this.simulations.forEach((_, rideId) => this.stopForRide(rideId));
  }
  requestStopAfterNextStep(rideId: number): void {
    const instance = this.simulations.get(rideId);
    if (instance) {
      instance.simulator.stopAfterNextStep.set(true);
      console.log(`Stop-after-next-step requested for ride ${rideId}`);
    }
  }
  stopRideEarly(rideId: number): { location: LocationDTO | null } {
    const instance = this.simulations.get(rideId);
    if (!instance) return { location: null };

    const location = instance.simulator.lastStopLocation;
    instance.simulator.stop();
    return { location };
  }
}


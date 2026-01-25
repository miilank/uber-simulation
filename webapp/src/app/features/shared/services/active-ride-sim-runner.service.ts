import { Injectable } from '@angular/core';
import { RideSimulationService } from './ride-simulation.service';
import {Subject} from 'rxjs';

type RideLike = {
  id: number;
  driverEmail: string | null | undefined;
  startLocation: { latitude: number; longitude: number };
  endLocation: { latitude: number; longitude: number };
  waypoints?: { latitude: number; longitude: number }[] | null;
  status?: string;
};

@Injectable({ providedIn: 'root' })
export class ActiveRideSimRunnerService {
  private runningRideId: number | null = null;

  private completionSubject = new Subject<number>();
  readonly onSimulationComplete$ = this.completionSubject.asObservable();

  constructor(private sim: RideSimulationService) {}

  startForRide(ride: RideLike): void {
    if (!ride?.driverEmail) return;

    if (this.runningRideId === ride.id) return;

    this.sim.stop();
    this.runningRideId = ride.id;

    this.sim.start(
      ride,
      {
        startDelayMs: 0,
        stepMeters: 100,
        tickMs: 1000
      },
      () => {
        this.notifyCompletion(ride.id);
      }
    );
  }

  stopForRide(rideId: number): void {
    if (this.runningRideId !== rideId) return;
    this.sim.stop();
    this.runningRideId = null;
  }

  notifyCompletion(rideId: number): void {
    this.completionSubject.next(rideId);
  }
}

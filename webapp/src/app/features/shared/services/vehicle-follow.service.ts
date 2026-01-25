import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { VehiclesApiService } from '../api/vehicles-api.service';
import { VehicleMarker } from '../map/vehicle-marker';

@Injectable({ providedIn: 'root' })
export class VehicleFollowService {
  private timers = new Map<string, number>();
  private subjects = new Map<string, BehaviorSubject<VehicleMarker | null>>();

  constructor(private vehiclesApi: VehiclesApiService) {}

  vehicle$(driverEmail: string): Observable<VehicleMarker | null> {
    if (!this.subjects.has(driverEmail)) {
      this.subjects.set(driverEmail, new BehaviorSubject<VehicleMarker | null>(null));
    }
    return this.subjects.get(driverEmail)!.asObservable();
  }

  start(driverEmail: string, pollMs = 1000): void {
    this.stop(driverEmail);

    const subj =
      this.subjects.get(driverEmail) ?? new BehaviorSubject<VehicleMarker | null>(null);
    this.subjects.set(driverEmail, subj);

    const tick = () => {
      this.vehiclesApi.getDriverVehicleForMap(driverEmail).subscribe({
        next: (v) => subj.next(v),
        error: () => {},
      });
    };

    tick();
    this.timers.set(driverEmail, window.setInterval(tick, pollMs));
  }

  stop(driverEmail: string): void {
    const t = this.timers.get(driverEmail);
    if (t) window.clearInterval(t);
    this.timers.delete(driverEmail);
  }
}

import { HttpClient } from "@angular/common/http";
import { computed, Injectable, signal, Signal, WritableSignal, inject } from "@angular/core";
import { ConfigService } from "../../../core/services/config.service";
import { RideDTO } from "../../shared/models/ride";
import { Observable, tap } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AdminDriverRidesService {
  private http = inject(HttpClient);
  private config = inject(ConfigService);

  public rides: WritableSignal<RideDTO[]> = signal<RideDTO[]>([]);

  public readonly currentRide: Signal<RideDTO | null> = computed(() => {
    const inProgressRides: RideDTO[] = this.rides()
      .filter(ride => ride.status === 'IN_PROGRESS');

    return inProgressRides.length === 0 ? null : inProgressRides[0];
  });

  fetchRidesForDriver(driverEmail: string): Observable<RideDTO[]> {
    return this.http.get<RideDTO[]>(`${this.config.baseUrl}/admin/drivers/${encodeURIComponent(driverEmail)}/rides`).pipe(
      tap(res => this.rides.set(res))
    );
  }
}

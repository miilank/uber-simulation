import { HttpClient } from "@angular/common/http";
import { computed, Injectable, signal, Signal, WritableSignal } from "@angular/core";
import { ConfigService } from "../../../core/services/config.service";
import {RideDetailDTO, RideDTO} from "../../shared/models/ride";
import { Observable, tap } from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class DriverRidesService {
    public rides: WritableSignal<RideDTO[]> = signal<RideDTO[]>([]);
    public readonly currentRide: Signal<RideDTO | null> = computed(() => {
      const activeRides: RideDTO[] = this.rides()
        .sort((a: RideDTO, b: RideDTO) => {
          if (a.status === 'IN_PROGRESS' && b.status !== 'IN_PROGRESS') return -1;
          if (b.status === 'IN_PROGRESS' && a.status !== 'IN_PROGRESS') return 1;
          if (a.status === 'ACCEPTED' && b.status !== 'ACCEPTED') return -1;
          if (b.status === 'ACCEPTED' && a.status !== 'ACCEPTED') return 1;

          return new Date(a.scheduledTime).getTime() - new Date(b.scheduledTime).getTime();
        });

      return activeRides.length === 0 ? null : activeRides[0];
    });

    constructor(
        private http: HttpClient,
        private config: ConfigService
    ) {}

    fetchRides() : Observable<RideDTO[]> {
        return this.http.get<RideDTO[]>(this.config.ridesUrl).pipe(
            tap(res => this.rides.set(res))
        );
    }

    startRide(rideId : number) : Observable<RideDTO> {
        return this.http.put<RideDTO>(this.config.startRideUrl(rideId), {}).pipe(
            tap((ride) => {
                let ridesArr: RideDTO[] = this.rides().map(r => {
                    if(r.id == ride.id) {
                        return ride;
                    } else return r;
                });

                this.rides.set(ridesArr);
            })
        )
    }

    completeRide(rideId: number): Observable<RideDTO> {
      return this.http.put<RideDTO>(this.config.completeRideUrl(rideId), {}).pipe(
        tap((completedRide) => {
          // Ukloni zavrsenu voznju iz liste (prebacuje se u history)
          let ridesArr: RideDTO[] = this.rides().filter(r => r.id !== completedRide.id);
          this.rides.set(ridesArr);
        })
      )
    }
    cancelRide(rideId: number, userId: number, reason: string): Observable<RideDTO> {
      return this.http.post<RideDTO>(`${this.config.ridesUrl}/${rideId}/cancel`, { reason, userId }).pipe(
        tap((cancelledRide) => {
          let ridesArr: RideDTO[] = this.rides().filter(r => r.id !== cancelledRide.id);
          this.rides.set(ridesArr);
        })
      );
    }

    getRideDetails(rideId: number): Observable<RideDetailDTO> {
      return this.http.get<RideDetailDTO>(`${this.config.baseUrl}/drivers/rides/${rideId}/details`);
    }
}

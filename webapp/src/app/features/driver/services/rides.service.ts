import { HttpClient } from "@angular/common/http";
import { computed, Injectable, signal, Signal, WritableSignal } from "@angular/core";
import { ConfigService } from "../../../core/services/config.service";
import { Ride, RideDTO } from "../../shared/models/ride";
import { Observable, tap } from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class RidesService {
    public rides: WritableSignal<RideDTO[]> = signal<RideDTO[]>([]);
    public readonly currentRide: Signal<RideDTO | null> = computed(() => {

      const pastRides: RideDTO[] = this.rides()
        .filter((r: RideDTO) => new Date(r.scheduledTime).getTime() <= Date.now())
        .sort((a: RideDTO, b: RideDTO) =>
          Number(b.status === 'IN_PROGRESS') - Number(a.status === 'IN_PROGRESS')
        );


      if (pastRides.length === 0) {
            return null;
        }

        return pastRides[0];
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
}

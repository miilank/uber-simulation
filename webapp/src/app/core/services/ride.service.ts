import { HttpClient } from "@angular/common/http";
import { ConfigService } from "./config.service";
import { computed, Injectable, Signal, signal, WritableSignal } from "@angular/core";
import { Observable } from "rxjs/internal/Observable";
import { LocationDTO } from "../../features/shared/models/location";
import {PassengerDTO} from '../../features/shared/models/passenger';
import { tap } from "rxjs";
import { RideDTO } from "../../features/shared/models/ride";


export interface RideEstimateDTO {
  estimatedDistance: number;
  vehicleType: 'STANDARD' | 'LUXURY' | 'VAN';
}

export interface RideEstimateResponseDTO {
  finalPrice: number;
  priceDisplay: string;
}

export type RideStatus = 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface UserRideDTO {
  id: number;
  status: RideStatus;
  startLocation: LocationDTO;
  endLocation: LocationDTO;
  waypoints: LocationDTO[];
  passengerEmails: string[];
  passengers?: PassengerDTO[];
  driverEmail: string;
  vehicleId?: number;
  vehicleModel?: string;
  vehicleLicensePlate?: string;
}

@Injectable({
  providedIn: 'root'
})

export class RideService{
   public rides: WritableSignal<RideDTO[]> = signal<RideDTO[]>([]);
   
    constructor(
    private http: HttpClient,
    private config: ConfigService
  ) {}

  public calculatePriceEstimate(request: RideEstimateDTO): Observable<RideEstimateResponseDTO> {
    return this.http.post<{finalPrice: number, priceDisplay: string}>(this.config.priceEstimateUrl, request);
  }

  getMyInProgressRide(): Observable<UserRideDTO> {
    return this.http.get<UserRideDTO>(this.config.ridesUrl + '/current-in-progress');
  }
  fetchRides() : Observable<RideDTO[]> {
          return this.http.get<RideDTO[]>(this.config.ridesUrl+'/passenger').pipe(
              tap(res => this.rides.set(res))
          );
      }
  cancelRide(rideId: number, userId: number, reason: string): Observable<RideDTO> {
        return this.http.post<RideDTO>(`${this.config.ridesUrl}/${rideId}/cancel`, { reason, userId }).pipe(
          tap((cancelledRide) => {
            let ridesArr: RideDTO[] = this.rides().filter(r => r.id !== cancelledRide.id);
            this.rides.set(ridesArr);
          })
        );
      }
}

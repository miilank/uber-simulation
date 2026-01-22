import { HttpClient } from "@angular/common/http";
import { ConfigService } from "./config.service";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs/internal/Observable";

export interface RideEstimateDTO {
  estimatedDistance: number;
  vehicleType: 'STANDARD' | 'LUXURY' | 'VAN';
}

export interface RideEstimateResponseDTO {
  finalPrice: number;
  priceDisplay: string;
}

export interface LocationDTO {
  latitude: number;
  longitude: number;
  address: string;
}

export type RideStatus = 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface RideDTO {
  id: number;
  status: RideStatus;
  startLocation: LocationDTO;
  endLocation: LocationDTO;
  passengerEmails: string[];
  driverEmail: string;

  vehicleId?: number;
  vehicleModel?: string;
  vehicleLicensePlate?: string;
}

@Injectable({
  providedIn: 'root'
})

export class RideService{
    constructor(
    private http: HttpClient,
    private config: ConfigService
  ) {}

  public calculatePriceEstimate(request: RideEstimateDTO): Observable<RideEstimateResponseDTO> {
    return this.http.post<{finalPrice: number, priceDisplay: string}>(this.config.priceEstimateUrl, request);
  }

  getMyInProgressRide(): Observable<RideDTO> {
    return this.http.get<RideDTO>(this.config.ridesUrl + '/current-in-progress');
  }
}

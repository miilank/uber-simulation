import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LocationDTO } from '../models/location';
import { RideDTO } from '../models/ride';

export interface RideETADTO {
  rideId: number;
  distanceToNextPointKm: number;
  etaToNextPointSeconds: number;
  phase: 'TO_PICKUP' | 'IN_PROGRESS';
  progressPercent: number;
}

@Injectable({ providedIn: 'root' })
export class RideApiService {
  private readonly baseUrl = 'http://localhost:8080/api/rides';

  constructor(private http: HttpClient) {}

  startRide(rideId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${rideId}/start`, {});
  }

  getRideETA(rideId: number): Observable<RideETADTO> {
    return this.http.get<RideETADTO>(`${this.baseUrl}/${rideId}/eta`);
  }

  arrivedAtPickup(rideId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${rideId}/arrived-pickup`, {});
  }

  completeRide(rideId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${rideId}/complete`, {});
  }

  reportInconsistency(rideId: number, passengerId: number, description: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${rideId}/inconsistency`, {
      rideId,
      passengerId,
      description
    });
  }
  stopRideEarly(rideId: number, dto: LocationDTO): Observable<any> {
    console.log('Stopping ride early with DTO:', dto);
    return this.http.post(`${this.baseUrl}/${rideId}/stop-early`, dto );
  }
}

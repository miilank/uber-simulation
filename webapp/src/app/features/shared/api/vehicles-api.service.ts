import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VehicleMarker } from '../map/vehicle-marker';

@Injectable({ providedIn: 'root' })
export class VehiclesApiService {
  private readonly baseUrl = 'http://localhost:8080/api/vehicles';

  constructor(private http: HttpClient) {}

  getMapVehicles(): Observable<VehicleMarker[]> {
    return this.http.get<VehicleMarker[]>(`${this.baseUrl}/map`);
  }
}

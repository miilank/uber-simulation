import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DriverListItemDTO {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  available: boolean;
  active: boolean;
  hasActiveRide: boolean;
  currentRideId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminDriverService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/admin';

  getAllDrivers(): Observable<DriverListItemDTO[]> {
    return this.http.get<DriverListItemDTO[]>(`${this.baseUrl}/drivers/all-with-status`);
  }

  getDriverDetails(driverId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/drivers/${driverId}`);
  }
}

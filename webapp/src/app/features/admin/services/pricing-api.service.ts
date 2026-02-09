import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PricingConfig, PricingUpdateRequest, VehicleType } from '../../shared/models/pricing';

@Injectable({ providedIn: 'root' })
export class PricingApiService {
  private readonly baseUrl = 'http://localhost:8080/api/pricing';

  constructor(private http: HttpClient) {}

  getAllPricing(): Observable<PricingConfig[]> {
    return this.http.get<PricingConfig[]>(this.baseUrl);
  }

  updatePricing(vehicleType: VehicleType, request: PricingUpdateRequest): Observable<PricingConfig> {
    return this.http.put<PricingConfig>(`${this.baseUrl}/${vehicleType}`, request);
  }
}

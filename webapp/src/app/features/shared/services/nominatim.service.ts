import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, map, Observable, of } from 'rxjs';

export interface NominatimResult {
  place_id?: number;
  display_name?: string;
  lat: string;
  lon: string;
  address?: {
    road?: string;
    house_number?: string;
    city?: string;
    town?: string;
    village?: string;
    postcode?: string;
    country?: string;
    municipality?: string;
    suburb?: string;
    hamlet?: string;
    state?: string;
  };
  boundingbox?: string[];
  formattedText: string;
}
export interface ReverseGeocodeResult {
  display_name: string;
  address: {
    road?: string;
    city?: string;
    state?: string;
    country?: string;
  };
}
@Injectable({
  providedIn: 'root'
})
export class NominatimService {
  private readonly baseUrl = 'https://nominatim.openstreetmap.org/search';

  constructor(private http: HttpClient) {}

  search(query: string, limit = 6): Observable<NominatimResult[]> {
    const params = new HttpParams()
      .set('q', query)
      .set('format', 'jsonv2')
      .set('addressdetails', '1')
      .set('countrycodes', 'rs')
      .set('limit', String(limit))
      .set('layer', 'address')
      .set('viewbox', '19.708797,45.316329,19.958903,45.199067');

    return this.http.get<NominatimResult[]>(this.baseUrl, { params }).pipe(
      map(results =>
        results.map(r => ({
          ...r,
          formattedText: this.formatResult(r)
        }))
      )
    );
  }

  formatResult(r: NominatimResult): string {
    function pickFirst<T>(...values: (T | undefined)[]): T | undefined {
      return values.find(v => v !== undefined);
    }

    const addr = r.address ?? {};

    const road = addr.road?.trim() ?? '';
    const house = addr.house_number?.trim() ?? '';

    let streetPart = '';
    if (road && house) streetPart = `${road} ${house}`;
    else if (road) streetPart = road;
    else if (house) streetPart = house;

    let city: string | undefined = pickFirst(
      addr.city,
      addr.town,
      addr.village,
      addr.municipality,
      addr.suburb,
      addr.hamlet,
      addr.state
    );

    const parts: string[] = [];
    if (streetPart) parts.push(streetPart);
    if (city) parts.push(city);

    if (parts.length === 0) {
      return (r.display_name ?? '').trim();
    }

    return parts.join(', ');
  }
  getAddress(lat: number, lng: number): Observable<string> {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`;
    
    return this.http.get<ReverseGeocodeResult>(url).pipe(
      map(result => {
        const parts = [
          result.address.road,
          result.address.city,
          result.address.state,
          result.address.country
        ].filter(Boolean);
        
        return parts.join(', ') || 'Unknown location';
      }),
      catchError(() => of('Address unavailable'))
    );
  }
}
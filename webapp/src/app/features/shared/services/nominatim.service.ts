import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export interface NominatimResult {
  place_id: number;
  display_name: string;
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
  };
  boundingbox?: string[];
  formattedText: string;
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
      .set('limit', String(limit))
      .set('layer', "address")
      .set('viewbox', '45.316329,19.708797,45.199067,19.958903');

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

  const street: string = addr.road ?? '';

  const house: string = (addr.house_number===undefined)? '' : ` ${addr.house_number}`;

  let city: string | undefined = pickFirst(
    addr.city,
    addr.town,
    addr.village,
    addr.municipality
  );

  if (city === undefined) {
    city = '';
  } else {
    city = `, ${city}`;
  }

  return street+house+city;
}
}
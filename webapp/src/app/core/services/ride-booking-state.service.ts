import { Injectable, signal } from '@angular/core';
import { NominatimResult } from '../../features/shared/services/nominatim.service';
export interface RouteInfo {
  totalDistance: number;  // meters
  totalDuration: number;  // seconds
}
// bridge between sidebar and map
@Injectable({ providedIn: 'root' })
export class RideBookingStateService {
  pickup = signal<NominatimResult | null>(null);
  dropoff = signal<NominatimResult | null>(null);
  stops = signal<(NominatimResult | null)[]>([]);

  routeInfo = signal<RouteInfo | null>(null);

  setPickup(r: NominatimResult | null) { this.pickup.set(r); }
  setDropoff(r: NominatimResult | null) { this.dropoff.set(r); }
  setRouteInfo(info: RouteInfo) { this.routeInfo.set(info); }

  addStop() {
    this.stops.update(arr => [...arr, null]);
  }

  setStop(index: number, r: NominatimResult | null) {
    this.stops.update(arr => {
      const copy = [...arr];
      copy[index] = r;
      return copy;
    });
  }

  removeStop(index: number) {
    this.stops.update(arr => arr.filter((_, i) => i !== index));
  }

  reset() {
    this.pickup.set(null);
    this.dropoff.set(null);
    this.stops.set([]);
    this.routeInfo.set(null);
  }
}

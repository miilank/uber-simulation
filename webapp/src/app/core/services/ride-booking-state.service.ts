import { Injectable, signal } from '@angular/core';
import { NominatimResult } from '../../features/shared/services/nominatim.service';

// bridge between sidebar and map
@Injectable({ providedIn: 'root' })
export class RideBookingStateService {
  pickup = signal<NominatimResult | null>(null);
  dropoff = signal<NominatimResult | null>(null);
  stops = signal<(NominatimResult | null)[]>([]);

  setPickup(r: NominatimResult | null) { this.pickup.set(r); }
  setDropoff(r: NominatimResult | null) { this.dropoff.set(r); }

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

  clearStops() { this.stops.set([]); }
}

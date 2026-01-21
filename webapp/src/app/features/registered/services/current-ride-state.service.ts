import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CurrentRideStateService {
  panicSignal = signal<{pressed: boolean; rideId: number; userId: number}>({pressed: false, rideId: 0, userId: 0})
}

import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CurrentRideStateService {
  panicSignal = signal<{pressed: boolean; rideId: number; userId: number}>({pressed: false, rideId: 0, userId: 0})

  setPanic(rideId: number, userId: number) {
    const state = {pressed: true, rideId, userId};
    this.panicSignal.set(state);
    localStorage.setItem('panic_currentRide', JSON.stringify(state));
  }

  loadPanic() {
    const saved = localStorage.getItem('panic_currentRide');
    if (saved) {
      this.panicSignal.set(JSON.parse(saved));
    }
  }
}

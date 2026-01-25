import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CurrentRideStateService {
  panicSignal = signal<{pressed: boolean; rideId: number; userId: number}>({pressed: false, rideId: 0, userId: 0})

  setPanic(rideId: number, userId: number) {
    const fullState = { pressed: true, rideId, userId};
    localStorage.setItem(this.getStorageKey(rideId), JSON.stringify(fullState));
    this.panicSignal.set(fullState);
  }

  loadPanic(rideId: number): void {
    const saved = localStorage.getItem(this.getStorageKey(rideId));
    if (!saved) return;
    
    try {
      const state = JSON.parse(saved);
      this.panicSignal.set(state);
    } catch (e) {
      console.error('Corrupted panic storage');
    }
  }
  private getStorageKey(rideId: number): string {
    return `panic_${rideId}`;
  }
  clearPanic(rideId: number): void {
    localStorage.removeItem(this.getStorageKey(rideId));
    this.panicSignal.set({pressed: false, rideId: 0, userId: 0});
  }
}

import { HttpClient } from '@angular/common/http';
import { effect, Injectable } from '@angular/core';
import { CurrentRideStateService } from '../../features/registered/services/current-ride-state.service';
import { ConfigService } from './config.service';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
constructor(
    private rideState: CurrentRideStateService,
    private http: HttpClient,
    private configService: ConfigService,
  ) {
    effect(() => {
      const panicActive = this.rideState.panicSignal().pressed;
      if (panicActive) {
        this.notifyAdminAboutPanic(this.rideState.panicSignal().rideId, this.rideState.panicSignal().userId);
      }
    });
  }
  notifyAdminAboutPanic(rideId: number, userId: number | null) : void {
    this.http.post(this.configService.ridesUrl + `/${rideId}/panic`, { userId }).subscribe({
      next: () => {
        console.log('Admin notified about panic');
      },
      error: (err) => {
        console.error('Failed to notify admin about panic', err);
      }
    });
  }
}




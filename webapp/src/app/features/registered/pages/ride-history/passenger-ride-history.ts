import { Component, inject, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { Subscription } from 'rxjs';
import { PassengerRideDetailsDrawer } from './common/ride-details-drawer/ride-details-drawer';
import { RatingModalComponent, RatingData } from '../../../shared/components/rating-modal.component';
import { RideApiService } from '../../../shared/api/ride-api.service';

interface RideHistoryItem {
  id: number;
  date: string;
  time: string;
  from: string;
  to: string;
  status: string;
  cancelledBy: string | null;
  panic: boolean;
  price: string;
  actualEndTime?: string;
  alreadyRated?: boolean;
  canRate?: boolean;
  ratingDisabledReason?: string;
}

type RideHistoryResponse = {
  rides: RideHistoryItem[];
  total: number;
  page: number;
  size: number;
};

@Component({
  selector: 'app-passenger-ride-history',
  imports: [CommonModule, FormsModule, PassengerRideDetailsDrawer, RatingModalComponent],
  templateUrl: './passenger-ride-history.html',
})
export class PassengerRideHistory implements OnInit, OnDestroy {

  private http = inject(HttpClient);
  private userService = inject(CurrentUserService);
  private cdr = inject(ChangeDetectorRef);
  private rideApi = inject(RideApiService);

  fromDate = '';
  toDate = '';
  passengerId: number | null = null;

  isDetailsOpen = false;
  selectedRide: RideHistoryItem | null = null;

  isRatingModalOpen = false;
  selectedRideForRating: RideHistoryItem | null = null;

  rides: RideHistoryItem[] = [];

  private subscription?: Subscription;

  ngOnInit() {
    this.subscription = this.userService.currentUser$.subscribe(user => {
      if (user?.id) {
        const newPassengerId = typeof user.id === 'number' ? user.id : parseInt(user.id as any, 10);

        if (this.passengerId !== newPassengerId) {
          this.passengerId = newPassengerId;
          this.load();
        }
      }
    });

    this.userService.fetchMe().subscribe();
  }

  ngOnDestroy() {
    this.subscription?.unsubscribe();
  }

  load() {
    if (!this.passengerId) return;

    let params = new HttpParams().set('userId', this.passengerId);

    if (this.fromDate) params = params.set('startDate', this.fromDate);
    if (this.toDate) params = params.set('endDate', this.toDate);

    this.http
      .get<RideHistoryResponse>('http://localhost:8080/api/rides/history/passenger', { params })
      .subscribe({
        next: (res) => {
          this.rides = (res.rides || []).map(ride => {
            const ratingCheck = this.checkCanRate(ride);
            return {
              ...ride,
              canRate: ratingCheck.canRate,
              ratingDisabledReason: ratingCheck.reason
            };
          });
          this.cdr.detectChanges();
        }
      });
  }

  reset() {
    this.fromDate = '';
    this.toDate = '';
    this.load();
  }

  apply() {
    this.load();
  }

  openDetails(ride: RideHistoryItem) {
    this.selectedRide = ride;
    this.isDetailsOpen = true;
  }

  closeDetails() {
    this.isDetailsOpen = false;
    this.selectedRide = null;
  }

  lastDays(days: number) {
    const today = new Date();
    const from = new Date();
    from.setDate(today.getDate() - days);

    const toIso = today.toISOString().slice(0, 10);
    this.fromDate = from.toISOString().slice(0, 10);
    this.toDate = toIso;

    this.apply();
  }

  checkCanRate(ride: RideHistoryItem): { canRate: boolean; reason?: string } {
    if (ride.alreadyRated) {
      return { canRate: false, reason: 'You have already rated this ride' };
    }

    if (ride.status === 'CANCELLED') {
      return { canRate: false, reason: 'Cancelled rides cannot be rated' };
    }

    if (ride.status !== 'COMPLETED' && ride.status !== 'STOPPED') {
      return { canRate: false, reason: 'Only completed rides can be rated' };
    }

    if (ride.actualEndTime) {
      const endTime = new Date(ride.actualEndTime);
      const now = new Date();
      const daysDiff = (now.getTime() - endTime.getTime()) / (1000 * 60 * 60 * 24);

      if (daysDiff > 3) {
        return { canRate: false, reason: 'Rating period expired (max 3 days)' };
      }
    }

    return { canRate: true };
  }

  openRatingModal(ride: RideHistoryItem): void {
    if (!ride.canRate) {
      return;
    }
    this.selectedRideForRating = ride;
    this.isRatingModalOpen = true;
  }

  closeRatingModal(): void {
    this.isRatingModalOpen = false;
    this.selectedRideForRating = null;
  }

  submitRating(ratingData: RatingData): void {
    if (!this.selectedRideForRating?.id) {
      console.error('No ride selected for rating');
      return;
    }

    const request = {
      rideId: this.selectedRideForRating.id,
      vehicleRating: ratingData.vehicleRating,
      driverRating: ratingData.driverRating,
      comment: ratingData.comment
    };

    this.rideApi.submitRating(request).subscribe({
      next: () => {
        console.log('Rating submitted successfully');

        this.rides = this.rides.map(r =>
          r.id === this.selectedRideForRating!.id
            ? { ...r, canRate: false, ratingDisabledReason: 'You have already rated this ride', alreadyRated: true }
            : r
        );

        this.cdr.detectChanges();
        this.closeRatingModal();
      },
      error: (err) => {
        console.error('Failed to submit rating', err);

        const errorMessage = err.error?.message || err.message || '';
        let reason = 'Failed to submit rating';

        if (errorMessage.includes('already rated')) {
          reason = 'You have already rated this ride';
        } else if (errorMessage.includes('expired') || errorMessage.includes('3 days')) {
          reason = 'Rating period expired (max 3 days)';
        }

        this.rides = this.rides.map(r =>
          r.id === this.selectedRideForRating!.id
            ? { ...r, canRate: false, ratingDisabledReason: reason, alreadyRated: true }
            : r
        );

        this.cdr.detectChanges();
        this.closeRatingModal();
      }
    });
  }
}

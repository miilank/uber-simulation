import { Component, inject, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { Subscription } from 'rxjs';
import { PassengerRideDetailsDrawer } from './common/ride-details-drawer/ride-details-drawer';

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
}

type RideHistoryResponse = {
  rides: RideHistoryItem[];
  total: number;
  page: number;
  size: number;
};

@Component({
  selector: 'app-passenger-ride-history',
  imports: [CommonModule, FormsModule, PassengerRideDetailsDrawer],
  templateUrl: './passenger-ride-history.html',
})
export class PassengerRideHistory implements OnInit, OnDestroy {

  private http = inject(HttpClient);
  private userService = inject(CurrentUserService);
  private cdr = inject(ChangeDetectorRef);

  fromDate = '';
  toDate = '';

  passengerId: number | null = null;

  isDetailsOpen = false;
  selectedRide: RideHistoryItem | null = null;

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
          this.rides = res.rides || [];
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
}


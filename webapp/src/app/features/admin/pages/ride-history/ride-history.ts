import { Component, inject, OnDestroy, OnInit, ChangeDetectorRef, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { Subscription } from 'rxjs';
import { PassengerRideDetailsDrawer } from '../../../registered/pages/ride-history/common/ride-details-drawer/ride-details-drawer';
import { UserSearch } from '../../components/user-search/user-search';
import { User } from '../../../../core/models/user';

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
  selector: 'admin-ride-history',
  imports: [PassengerRideDetailsDrawer, CommonModule, FormsModule, UserSearch],
  templateUrl: './ride-history.html',
})
export class AdminRideHistory{
  private http = inject(HttpClient);
  private userService = inject(CurrentUserService);
  private cdr = inject(ChangeDetectorRef);

  fromDate = '';
  toDate = '';

  passengerId: number | null = null;
  selectedUser = signal<User | null>(null);

  isDetailsOpen = false;
  selectedRide: RideHistoryItem | null = null;

  rides: RideHistoryItem[] = [];
  constructor() {
    effect(() => {
      const user = this.selectedUser();
      
      if (user?.id) {
        this.load();
      } else {
        this.rides = [];
      }
    });
  }
  load() {
    const user = this.selectedUser();
    
    if (!user?.id) {
      return;
    }

    const userId = typeof user.id === 'number' ? user.id : parseInt(user.id as any, 10);
    let params = new HttpParams();

    if (this.fromDate) params = params.set('startDate', this.fromDate);
    if (this.toDate) params = params.set('endDate', this.toDate);
    if (user?.role == "PASSENGER"){
      params = params.set('userId', userId);
      this.http
        .get<RideHistoryResponse>('http://localhost:8080/api/rides/history/passenger', { params })
        .subscribe({
          next: (res) => {
            this.rides = res.rides || [];
            this.cdr.detectChanges();
          }
        });
    
    }
    if (user?.role == "DRIVER"){
      params = params.set('driverId', userId);
      this.http
      .get<RideHistoryResponse>('http://localhost:8080/api/rides/history', { params })
      .subscribe({
        next: (res) => {
          this.rides = res.rides || [];
          this.cdr.detectChanges();
        }
      });
    
    }
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

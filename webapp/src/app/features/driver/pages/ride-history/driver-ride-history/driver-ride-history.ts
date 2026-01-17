import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClient, HttpParams} from '@angular/common/http';
import {RideDetailsDrawer} from '../common/ride-details-drawer/ride-details-drawer';

type RideStatus = 'COMPLETED' | 'CANCELLED';

export type Ride = {
  id?: number;
  date: string;
  time: string;
  from: string;
  to: string;
  status: RideStatus;
  cancelledBy?: 'User' | 'Driver' | null;
  panic: boolean;
  price: string;
};

type RideHistoryResponse = {
  rides: Ride[];
  total: number;
  page: number;
  size: number;
};

@Component({
  selector: 'app-driver-ride-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RideDetailsDrawer],
  templateUrl: './driver-ride-history.html',
})
export class DriverRideHistory implements OnInit {
  private http = inject(HttpClient);

  fromDate = '';
  toDate = '';

  // hardcode driver id (JWT should be implemented)
  driverId = 1;

  isDetailsOpen = false;
  selectedRide: Ride | null = null;

  rides: Ride[] = [];

  ngOnInit() {
    this.load();
  }

  load() {
    let params = new HttpParams().set('driverId', this.driverId);

    if (this.fromDate) params = params.set('startDate', this.fromDate);
    if (this.toDate) params = params.set('endDate', this.toDate);

    this.http
      .get<RideHistoryResponse>('http://localhost:8080/api/rides/history', { params })
      .subscribe(res => (this.rides = res.rides));
  }

  reset() {
    this.fromDate = '';
    this.toDate = '';
    this.load();
  }

  apply() {
    this.load();
  }

  openDetails(ride: Ride) {
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

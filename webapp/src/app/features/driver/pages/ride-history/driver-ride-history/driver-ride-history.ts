import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RideDetailsDrawer } from '../components/ride-details-drawer/ride-details-drawer';

type RideStatus = 'COMPLETED' | 'CANCELLED';

export type Ride = {
  date: string;
  time: string;
  from: string;
  to: string;
  status: RideStatus;
  cancelledBy?: 'User' | 'Driver';
  panic: boolean;
  price: string;
};

@Component({
  selector: 'app-driver-ride-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RideDetailsDrawer],
  templateUrl: './driver-ride-history.html',
})
export class DriverRideHistory {
  fromDate = '';
  toDate = '';

  isDetailsOpen = false;
  selectedRide: Ride | null = null;

  rides: Ride[] = [
    { date: '05.12.', time: '22:15 - 22:32', from: 'Bulevar oslobođenja 76', to: 'Naučno tehnološki park', status: 'COMPLETED', panic: false, price: '€12.50' },
    { date: '20.11.', time: '14:20 - 14:45', from: 'Bulevar oslobođenja 76', to: 'Naučno tehnološki park', status: 'CANCELLED', cancelledBy: 'User', panic: false, price: '€15.00' },
    { date: '20.10.', time: '09:10 - 09:28', from: 'Bulevar oslobođenja 76', to: 'Naučno tehnološki park', status: 'COMPLETED', panic: true, price: '€11.00' },
    { date: '22.09.', time: '18:45 - 19:05', from: 'Bulevar oslobođenja 76', to: 'Naučno tehnološki park', status: 'COMPLETED', panic: false, price: '€13.50' },
    { date: '20.09.', time: '16:30 - 16:50', from: 'Bulevar oslobođenja 76', to: 'Naučno tehnološki park', status: 'COMPLETED', panic: false, price: '€10.50' },
    { date: '12.09.', time: '11:30 - 11:50', from: 'Bulevar oslobođenja 76', to: 'Naučno tehnološki park', status: 'CANCELLED', cancelledBy: 'Driver', panic: true, price: '€14.00' },
  ];

  reset() {
    this.fromDate = '';
    this.toDate = '';
  }

  openDetails(ride: Ride) {
    this.selectedRide = ride;
    this.isDetailsOpen = true;
  }

  closeDetails() {
    this.isDetailsOpen = false;
    this.selectedRide = null;
  }
}

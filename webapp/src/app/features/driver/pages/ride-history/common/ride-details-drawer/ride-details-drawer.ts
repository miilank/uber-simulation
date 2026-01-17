import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import type { Ride } from '../../driver-ride-history/driver-ride-history';

type Passenger = { name: string; phone: string };

@Component({
  selector: 'app-ride-details-drawer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details-drawer.html',
})
export class RideDetailsDrawer {
  @Input() open = false;
  @Input() ride: Ride | null = null;
  @Output() close = new EventEmitter<void>();

  // mock details (later from db)
  passengers: Passenger[] = [
    { name: 'Ana Marković', phone: '+381 64 123 4567' },
    { name: 'Marko Petrović', phone: '+381 65 234 5678' },
  ];

  // mock additional fields
  get startDateTime() {
    // can be parsed from db
    return this.ride ? `${this.ride.date}2025, ${this.ride.time.split(' - ')[0]}` : '';
  }
  get endDateTime() {
    return this.ride ? `${this.ride.date}2025, ${this.ride.time.split(' - ')[1]}` : '';
  }
  get duration() {
    // dummy
    return this.ride?.status ? '17 minutes' : '';
  }

  // panic details (mock)
  get panicYes() {
    return !!this.ride?.panic;
  }
  get panicTimestamp() {
    return this.ride?.panic ? `${this.ride.date}2025, 09:15` : '';
  }

  // cancelled details (mock)
  get cancelReason() {
    return 'Changed plans';
  }
  get cancelTime() {
    return this.ride ? `${this.ride.date}2025, 14:18` : '';
  }

  onBackdropClick() {
    this.close.emit();
  }
  onClose() {
    this.close.emit();
  }
}

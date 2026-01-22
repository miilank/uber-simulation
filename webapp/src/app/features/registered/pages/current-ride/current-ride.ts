import { Component, EventEmitter, inject, Output, signal, OnInit, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import {VehicleMarker} from '../../../shared/map/vehicle-marker';
import {VehiclesApiService} from '../../../shared/api/vehicles-api.service';
import { FormsModule } from '@angular/forms';
import {MapComponent} from '../../../shared/map/map';
import { CurrentRideStateService } from '../../services/current-ride-state.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { UserService } from '../../../../core/services/user.service';

type RideStatus = 'Assigned' | 'Started' | 'Finished' | 'Cancelled';
type PassengerItem = { id: number; name: string; role: 'You' | 'Passenger' };

@Component({
  selector: 'app-current-ride',
  standalone: true,
  imports: [CommonModule, FormsModule, MapComponent],
  templateUrl: './current-ride.html',
})
export class CurrentRideComponent implements OnInit {
  constructor(private vehiclesApi: VehiclesApiService, private cdr: ChangeDetectorRef) {}
  private notificationService = inject(NotificationService);
  private userService = inject(UserService);
  rideState = inject(CurrentRideStateService);
  vehicles: VehicleMarker[] = [];
  ngOnInit(): void {
    this.vehiclesApi.getMapVehicles().subscribe({
      next: (data) => {
        this.vehicles = data
        this.rideState.loadPanic();
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load vehicles', err),
    });
  }
  // page state (mock for now)

  onPanic(): void {
     if (this.rideState.panicSignal().pressed) return;

    const userId = this.userService.getCurrentUserId() ?? 0;
    const rideId = 1; // Mock ride ID
    this.rideState.setPanic(rideId, userId);
  }

  currentRideStatus: RideStatus = 'Started';

  fromAddress = 'Bulevar oslobođenja 46';
  toAddress = 'Ulica Narodnih heroja 14';

  vehicleText = 'Skoda Octavia • NS-123-AB';

  passengers: PassengerItem[] = [
    { id: 1, name: 'Milan Kacarevic', role: 'You' },
    { id: 2, name: 'Mirko Mirkovic', role: 'Passenger' },
    { id: 3, name: 'Ana Jovanovic', role: 'Passenger' },
    { id: 4, name: 'Ana Markovic', role: 'Passenger' },
  ];

  // ETA (mock)
  etaMinutes = 7;

  reportNote = '';
  submittingReport = false;

  statusPillClasses: Record<RideStatus, string> = {
    Assigned: 'bg-blue-100 text-blue-700',
    Started: 'bg-green-100 text-green-700',
    Finished: 'bg-gray-100 text-slate-700',
    Cancelled: 'bg-red-100 text-red-700',
  };

  get isRideActive(): boolean {
    return this.currentRideStatus === 'Started' || this.currentRideStatus === 'Assigned';
  }

  get etaText(): string {
    if (!this.isRideActive) return '--';
    if (this.etaMinutes <= 1) return '< 1 min';
    return `${this.etaMinutes} min`;
  }

  submitReport(): void {
    const note = this.reportNote.trim();
    if (!this.isRideActive || !note) return;

    this.submittingReport = true;

    // UI-only placeholder: later API call
    setTimeout(() => {
      this.submittingReport = false;
      this.reportNote = '';
    }, 600);
  }
}

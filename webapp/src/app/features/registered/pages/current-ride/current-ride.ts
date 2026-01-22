import { Component, inject, OnInit, ChangeDetectorRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { VehicleMarker } from '../../../shared/map/vehicle-marker';
import { VehiclesApiService } from '../../../shared/api/vehicles-api.service';
import { MapComponent } from '../../../shared/map/map';

import { CurrentRideStateService } from '../../services/current-ride-state.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { UserService } from '../../../../core/services/user.service';
import { RideDTO, RideService } from '../../../../core/services/ride.service';

type UiRideStatus = 'Assigned' | 'Started' | 'Finished' | 'Cancelled';
type PassengerItem = { id: number; name: string; role: 'You' | 'Passenger' };

@Component({
  selector: 'app-current-ride',
  standalone: true,
  imports: [CommonModule, FormsModule, MapComponent],
  templateUrl: './current-ride.html',
})
export class CurrentRideComponent implements OnInit {
  constructor(
    private rideState: CurrentRideStateService,
    private vehiclesApi: VehiclesApiService,
    private cdr: ChangeDetectorRef
  ) {}

  private notificationService = inject(NotificationService);
  private userService = inject(UserService);
  private rideService = inject(RideService);

  panicSent = signal(false);

  vehicles: VehicleMarker[] = [];

  currentRideStatus: UiRideStatus = 'Started';
  fromAddress = '';
  toAddress = '';
  vehicleText = '';
  passengers: PassengerItem[] = [];

  etaMinutes = 1;

  reportNote = '';
  submittingReport = false;

  private ride: RideDTO | null = null;

  ngOnInit(): void {
    this.rideService.getMyInProgressRide().subscribe({
      next: (r) => {
        this.ride = r;

        this.currentRideStatus = 'Started';
        this.fromAddress = r.startLocation.address;
        this.toAddress = r.endLocation.address;

        this.vehicleText =
          r.vehicleModel && r.vehicleLicensePlate
            ? `${r.vehicleModel} • ${r.vehicleLicensePlate}`
            : 'Vehicle';

        this.passengers = (r.passengerEmails ?? []).map((email, idx) => ({
          id: idx + 1,
          name: email,
          role: 'Passenger'
        }));

        this.vehiclesApi.getDriverVehicleForMap(r.driverEmail).subscribe({
          next: (v) => {
            this.vehicles = [v];
            this.cdr.detectChanges();

            this.runSimulation(r);
          },
          error: (err) => console.error('Failed to load driver vehicle', err),
        });
      },
      error: () => {
        this.ride = null;
        this.vehicles = [];
        this.currentRideStatus = 'Cancelled';
        this.fromAddress = '';
        this.toAddress = '';
        this.vehicleText = '';
      }
    });
  }

  private runSimulation(r: RideDTO) {
    window.setTimeout(() => {
      if (!this.vehicles.length) return;

      this.vehicles = [{
        ...this.vehicles[0],
        lat: r.startLocation.latitude,
        lng: r.startLocation.longitude
      }];
      this.cdr.detectChanges();

      window.setTimeout(() => {
        if (!this.vehicles.length) return;

        this.vehicles = [{
          ...this.vehicles[0],
          lat: r.endLocation.latitude,
          lng: r.endLocation.longitude
        }];
        this.cdr.detectChanges();

        window.alert('Ride finished!');
        this.currentRideStatus = 'Finished';
        this.cdr.detectChanges();
      }, 10_000);

    }, 30_000);
  }

  onPanic(): void {
     if (this.rideState.panicSignal().pressed) return;

    const userId = this.userService.getCurrentUserId() ?? 0;
    const rideId = 1; // Mock ride ID
    this.rideState.setPanic(rideId, userId);
  }

  statusPillClasses: Record<UiRideStatus, string> = {
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
    return;
  }
}

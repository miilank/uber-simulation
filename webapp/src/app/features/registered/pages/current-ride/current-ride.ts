import { ChangeDetectorRef, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { VehicleMarker } from '../../../shared/map/vehicle-marker';
import { MapComponent } from '../../../shared/map/map';

import { CurrentRideStateService } from '../../services/current-ride-state.service';
import { UserService } from '../../../../core/services/user.service';
import { RideDTO, RideService } from '../../../../core/services/ride.service';

import { LatLng } from '../../../shared/services/routing.service';
import { LocationDTO } from '../../../shared/models/location';

import { Subscription } from 'rxjs';
import { VehicleFollowService } from '../../../shared/services/vehicle-follow.service';

type UiRideStatus = 'Assigned' | 'Started' | 'Finished' | 'Cancelled';
type PassengerItem = { id: number; name: string; email: string; role: 'You' | 'Passenger' };

@Component({
  selector: 'app-current-ride',
  standalone: true,
  imports: [CommonModule, FormsModule, MapComponent],
  templateUrl: './current-ride.html',
})
export class CurrentRideComponent implements OnInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);
  private userService = inject(UserService);
  private rideService = inject(RideService);
  public rideState = inject(CurrentRideStateService);

  private follow = inject(VehicleFollowService);
  private subs: Subscription[] = [];

  private driverEmail?: string;

  ride: RideDTO | null = null;

  waypoints: string[] = [];
  waypointLocations: LocationDTO[] = [];

  // map inputs
  vehicles: VehicleMarker[] = [];
  routePath: LatLng[] = [];
  routePoints: { lat: number; lon: number; label?: string }[] = [];

  currentRideStatus: UiRideStatus = 'Started';
  fromAddress = '';
  toAddress = '';
  vehicleText = '';
  passengers: PassengerItem[] = [];
  etaMinutes = 1;

  reportNote = '';
  submittingReport = false;

  ngOnInit(): void {
    this.rideService.getMyInProgressRide().subscribe({
      next: (r) => {
        this.ride = r;
        this.currentRideStatus = 'Started';

        this.fromAddress = r.startLocation.address;
        this.toAddress = r.endLocation.address;

        this.routePoints = [
          { lat: r.startLocation.latitude, lon: r.startLocation.longitude, label: 'Pickup' },
          ...(r.waypoints ?? []).map((w, i) => ({
            lat: w.latitude,
            lon: w.longitude,
            label: `Stop ${i + 1}`,
          })),
          { lat: r.endLocation.latitude, lon: r.endLocation.longitude, label: 'Destination' },
        ];

        this.waypointLocations = (r.waypoints ?? []);
        this.waypoints = this.waypointLocations.map(w => w.address).filter(Boolean);

        this.vehicleText =
          r.vehicleModel && r.vehicleLicensePlate
            ? `${r.vehicleModel} • ${r.vehicleLicensePlate}`
            : 'Vehicle';

        this.passengers = (r.passengers ?? []).map((p, idx: number) => ({
          id: idx + 1,
          name: `${p.firstName} ${p.lastName}`.trim(),
          email: p.email,
          role: 'Passenger' as const,
        }));

        // FOLLOW (per driver)
        if (r.driverEmail) {
          this.driverEmail = r.driverEmail;
          this.follow.start(this.driverEmail, 1000);
          this.subs.push(
            this.follow.vehicle$(this.driverEmail).subscribe(v => {
              this.vehicles = v ? [v] : [];
              this.cdr.detectChanges();
            })
          );
        }
      },
      error: () => {
        this.clearUi();
      }
    });
  }

  private clearUi(): void {
    if (this.driverEmail) this.follow.stop(this.driverEmail);
    this.driverEmail = undefined;

    this.ride = null;
    this.vehicles = [];
    this.routePath = [];
    this.routePoints = [];
    this.currentRideStatus = 'Cancelled';
    this.fromAddress = '';
    this.toAddress = '';
    this.vehicleText = '';
  }

  onPanic(): void {
    if (this.rideState.panicSignal().pressed) return;

    const userId = this.userService.getCurrentUserId() ?? 0;
    const rideId = this.ride?.id;
    if (!rideId) return;

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

  ngOnDestroy(): void {
    if (this.driverEmail) this.follow.stop(this.driverEmail);
    this.subs.forEach(s => s.unsubscribe());
    this.subs = [];
  }
}

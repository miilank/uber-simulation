import { ChangeDetectorRef, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { VehicleMarker } from '../../../shared/map/vehicle-marker';
import { MapComponent } from '../../../shared/map/map';

import { CurrentRideStateService } from '../../services/current-ride-state.service';
import { UserService } from '../../../../core/services/user.service';
import { UserRideDTO, RideService } from '../../../../core/services/ride.service';

import { LatLng } from '../../../shared/services/routing.service';
import { LocationDTO } from '../../../shared/models/location';

import {interval, Subscription} from 'rxjs';
import { VehicleFollowService } from '../../../shared/services/vehicle-follow.service';
import { NotificationService } from '../../../../core/services/notification.service';
import {RideApiService, RideETADTO} from '../../../shared/api/ride-api.service';
import {switchMap} from 'rxjs/operators';
import {RatingData, RatingModalComponent} from '../../../shared/components/rating-modal.component';

type UiRideStatus = 'Assigned' | 'Started' | 'Finished' | 'Cancelled';
type PassengerItem = { id: number; name: string; email: string; role: 'You' | 'Passenger' };

@Component({
  selector: 'app-current-ride',
  standalone: true,
  imports: [CommonModule, FormsModule, MapComponent, RatingModalComponent],
  templateUrl: './current-ride.html',
})
export class CurrentRideComponent implements OnInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);
  private userService = inject(UserService);
  private rideService = inject(RideService);
  public rideState = inject(CurrentRideStateService);
  private notificationService = inject(NotificationService);
  private rideApi = inject(RideApiService);
  private follow = inject(VehicleFollowService);

  private subs: Subscription[] = [];
  private driverEmail?: string;

  private etaPollSub?: Subscription;
  currentETA: RideETADTO | null = null;
  ridePhase: 'TO_PICKUP' | 'IN_PROGRESS' | 'IDLE' = 'IDLE';

  ride: UserRideDTO | null = null;

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

  reportNote = '';
  submittingReport = false;

  isRatingModalOpen = false;

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

        this.rideState.loadPanic(r.id);
        this.startETAPolling(r.id);

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
    this.stopETAPolling();
    this.ride = null;
    this.vehicles = [];
    this.routePath = [];
    this.routePoints = [];
    this.currentRideStatus = 'Cancelled';
    this.fromAddress = '';
    this.toAddress = '';
    this.vehicleText = '';
  }

  private startETAPolling(rideId: number): void {
    this.stopETAPolling();

    this.etaPollSub = interval(2000)
      .pipe(switchMap(() => this.rideApi.getRideETA(rideId)))
      .subscribe({
        next: (eta) => {
          this.currentETA = eta;
          this.ridePhase = eta.phase as any;

          if (eta.etaToNextPointSeconds === 0 && eta.distanceToNextPointKm < 0.05) {
            console.log('Vehicle arrived at destination (via ETA)');
            this.stopETAPolling();
          }

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to fetch ETA', err);
        }
      });
  }

  private stopETAPolling(): void {
    if (this.etaPollSub) {
      this.etaPollSub.unsubscribe();
      this.etaPollSub = undefined;
    }
    this.currentETA = null;
  }

  onPanic(): void {
    if (this.rideState.panicSignal().pressed) return;

    const userId = this.userService.getCurrentUserId() ?? 0;
    const rideId = this.ride?.id;
    if (!rideId) return;

    this.rideState.setPanic(rideId, userId);
  }

  get displayStatus(): string {
    if (this.ridePhase === 'TO_PICKUP') return 'Arriving to pickup';
    if (this.ridePhase === 'IN_PROGRESS') return 'In progress';
    return 'Started';
  }

  get displayStatusClass(): string {
    if (this.ridePhase === 'TO_PICKUP') return 'bg-blue-100 text-blue-700';
    if (this.ridePhase === 'IN_PROGRESS') return 'bg-green-100 text-green-700';
    return 'bg-green-100 text-green-700';
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
    if (!this.currentETA) return '--';

    const minutes = Math.floor(this.currentETA.etaToNextPointSeconds / 60);
    if (minutes === 0) return '< 1 min';
    return `${minutes} min`;
  }

  get distanceText(): string {
    if (!this.currentETA) return '--';
    return `${this.currentETA.distanceToNextPointKm.toFixed(1)} km`;
  }

  get phaseLabel(): string {
    if (this.ridePhase === 'TO_PICKUP') return 'Arriving to pickup';
    if (this.ridePhase === 'IN_PROGRESS') return 'In progress';
    return '';
  }

  openRatingModal(): void {
    this.isRatingModalOpen = true;
  }

  closeRatingModal(): void {
    this.isRatingModalOpen = false;
  }

  submitRating(ratingData: RatingData): void {
    if (!this.ride?.id) {
      console.error('No ride ID available');
      return;
    }

    const request = {
      rideId: this.ride.id,
      vehicleRating: ratingData.vehicleRating,
      driverRating: ratingData.driverRating,
      comment: ratingData.comment
    };

    this.rideApi.submitRating(request).subscribe({
      next: (response) => {
        console.log('Rating submitted successfully', response);
        this.closeRatingModal();
      },
      error: (err) => {
        console.error('Failed to submit rating', err);
      }
    });
  }

  submitReport(): void {
    if (!this.isRideActive || !this.reportNote.trim() || this.submittingReport) {
      return;
    }

    const userId = this.userService.getCurrentUserId();
    const rideId = this.ride?.id;

    if (!userId || !rideId) {
      console.error('Missing userId or rideId');
      return;
    }

    this.submittingReport = true;

    this.rideApi.reportInconsistency(rideId, userId, this.reportNote.trim()).subscribe({
      next: () => {
        console.log('Inconsistency reported successfully');
        this.reportNote = '';
        this.submittingReport = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to report inconsistency', err);
        this.submittingReport = false;
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.driverEmail) this.follow.stop(this.driverEmail);
    this.stopETAPolling();
    this.subs.forEach(s => s.unsubscribe());
    this.subs = [];
  }
}

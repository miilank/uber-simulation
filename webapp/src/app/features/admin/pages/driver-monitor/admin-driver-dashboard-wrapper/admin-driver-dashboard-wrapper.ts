import {
  ChangeDetectorRef,
  Component,
  computed,
  effect,
  inject,
  Input,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../../../../shared/map/map';
import { RideDTO, RideStatus } from '../../../../shared/models/ride';
import { CurrentRideStateService } from '../../../../registered/services/current-ride-state.service';
import { VehicleFollowService } from '../../../../shared/services/vehicle-follow.service';
import { ActiveRideSimRunnerService } from '../../../../shared/services/active-ride-sim-runner.service';
import { RideApiService, RideETADTO } from '../../../../shared/api/ride-api.service';
import { VehicleMarker } from '../../../../shared/map/vehicle-marker';
import { LatLng } from '../../../../shared/services/routing.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AdminDriverRidesService } from '../../../services/admin-driver-rides.service';

type Passenger = { name: string; email: string };

@Component({
  selector: 'app-admin-driver-dashboard-wrapper',
  standalone: true,
  imports: [CommonModule, MapComponent],
  templateUrl: './admin-driver-dashboard-wrapper.html',
})
export class AdminDriverDashboardWrapper implements OnInit, OnDestroy {
  @Input() driverEmail!: string;

  private cdr = inject(ChangeDetectorRef);
  private ridesService = inject(AdminDriverRidesService);
  private rideState = inject(CurrentRideStateService);
  private rideApi = inject(RideApiService);
  private follow = inject(VehicleFollowService);
  private simRunner = inject(ActiveRideSimRunnerService);

  protected simulationCompleted = signal<boolean>(false);
  protected currentETA = signal<RideETADTO | null>(null);
  protected ridePhase = signal<'TO_PICKUP' | 'IN_PROGRESS' | 'IDLE'>('IDLE');

  private subs: Subscription[] = [];
  private etaPollSub?: Subscription;
  private currentRideId?: number;

  vehicles: VehicleMarker[] = [];
  routePath: LatLng[] = [];
  routePoints: { lat: number; lon: number; label?: string }[] = [];

  readonly rides = this.ridesService.rides;
  readonly currentRide = this.ridesService.currentRide;

  constructor() {
    effect(() => {
      const r = this.currentRide();

      if (this.currentRideId && (!r || r.id !== this.currentRideId)) {
        this.cleanupCurrentRideSubscriptions();
      }

      if (!r) {
        this.simulationCompleted.set(false);
        this.stopETAPolling();
        if (this.driverEmail) this.follow.stop(this.driverEmail);
        this.currentRideId = undefined;

        this.routePoints = [];
        this.vehicles = [];
        this.routePath = [];
        this.ridePhase.set('IDLE');
        return;
      }

      this.currentRideId = r.id;

      this.routePoints = [
        { lat: r.startLocation.latitude, lon: r.startLocation.longitude, label: 'Pickup' },
        ...(r.waypoints ?? []).map((w, i) => ({
          lat: w.latitude,
          lon: w.longitude,
          label: `Stop ${i + 1}`,
        })),
        { lat: r.endLocation.latitude, lon: r.endLocation.longitude, label: 'Destination' },
      ];

      this.rideState.loadPanic(r.id);

      if (r.status === 'IN_PROGRESS') {
        if (this.driverEmail) {
          this.follow.stop(this.driverEmail);
        }

        if (!this.simRunner.isRunning(r.id)) {
          console.log(`Starting simulation for ride ${r.id}`);
          this.simulationCompleted.set(false);
          this.simRunner.startForRide(r);
        } else {
          const simulation = this.simRunner.getSimulation(r.id);
          if (simulation?.completed) {
            this.simulationCompleted.set(true);
          }
        }

        const vehiclesSub = this.simRunner.getVehicles$(r.id);
        const routePathSub = this.simRunner.getRoutePath$(r.id);

        if (vehiclesSub) {
          this.subs.push(
            vehiclesSub.subscribe(v => {
              this.vehicles = v;
              this.cdr.detectChanges();
            })
          );
        }

        if (routePathSub) {
          this.subs.push(
            routePathSub.subscribe(p => {
              this.routePath = p;
              this.cdr.detectChanges();
            })
          );
        }

        this.startETAPolling(r.id);
      } else {
        if (r.driverEmail && r.driverEmail !== this.driverEmail) {
          if (this.driverEmail) this.follow.stop(this.driverEmail);

          this.follow.start(r.driverEmail, 1000);
          this.subs.push(
            this.follow.vehicle$(r.driverEmail).subscribe(v => {
              this.vehicles = v ? [v] : [];
              this.cdr.detectChanges();
            })
          );
        }

        this.stopETAPolling();
      }
    });
  }

  ngOnInit() {
    if (!this.driverEmail) {
      console.error('Driver email is required');
      return;
    }

    this.ridesService.fetchRidesForDriver(this.driverEmail).subscribe();

    this.subs.push(
      this.simRunner.onPickupReached$.subscribe(rideId => {
        const current = this.currentRide();
        if (current && current.id === rideId) {
          this.ridePhase.set('IN_PROGRESS');
          this.cdr.detectChanges();
        }
      })
    );

    this.subs.push(
      this.simRunner.onSimulationComplete$.subscribe(rideId => {
        const current = this.currentRide();
        if (current && current.id === rideId) {
          this.simulationCompleted.set(true);
          this.stopETAPolling();
          this.cdr.detectChanges();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.cleanupCurrentRideSubscriptions();
  }

  private cleanupCurrentRideSubscriptions(): void {
    if (this.driverEmail) this.follow.stop(this.driverEmail);
    this.stopETAPolling();
    this.subs.forEach(s => s.unsubscribe());
    this.subs = [];
  }

  private startETAPolling(rideId: number): void {
    this.stopETAPolling();
    this.etaPollSub = interval(2000)
      .pipe(switchMap(() => this.rideApi.getRideETA(rideId)))
      .subscribe({
        next: (eta) => {
          this.currentETA.set(eta);
          this.ridePhase.set(eta.phase as any);
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
    this.currentETA.set(null);
  }

  readonly currentPassengers = computed<Passenger[]>(() =>
    this.currentRide()?.passengers?.map(p => ({
      name: `${p.firstName} ${p.lastName}`.trim(),
      email: p.email,
    })) ?? []
  );

  readonly currentRideStatus = computed<RideStatus>(() =>
    this.currentRide()?.status ?? 'PENDING'
  );

  readonly currentRideStart = computed<string>(() =>
    this.currentRide()?.startLocation.address ?? ''
  );

  readonly currentRideEnd = computed<string>(() =>
    this.currentRide()?.endLocation.address ?? ''
  );

  get etaText(): string {
    const eta = this.currentETA();
    if (!eta) return '--';

    const minutes = Math.floor(eta.etaToNextPointSeconds / 60);
    if (minutes === 0) return '< 1 min';
    return `${minutes} min`;
  }

  get distanceText(): string {
    const eta = this.currentETA();
    if (!eta) return '--';
    return `${eta.distanceToNextPointKm.toFixed(1)} km`;
  }

  get phaseLabel(): string {
    const phase = this.ridePhase();
    if (phase === 'TO_PICKUP') return 'Arriving to pickup';
    if (phase === 'IN_PROGRESS') return 'In progress';
    return '';
  }

  statusPillClasses: Record<RideStatus, string> = {
    PENDING: 'bg-gray-100 text-slate-700',
    ACCEPTED: 'bg-blue-100 text-blue-700',
    IN_PROGRESS: 'bg-green-100 text-green-700',
    COMPLETED: 'bg-gray-100 text-slate-700',
    CANCELLED: 'bg-red-100 text-red-700',
    STOPPED: 'bg-yellow-100 text-yellow-700',
  };
}

import {
  ChangeDetectorRef,
  Component,
  computed,
  effect,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../../../../shared/map/map';

import { DriverRidesService } from '../../../services/driver-rides.service';
import { RideDTO, RideStatus } from '../../../../shared/models/ride';
import { CurrentUserService } from '../../../../../core/services/current-user.service';
import { Driver } from '../../../../shared/models/driver';
import { CurrentRideStateService } from '../../../../registered/services/current-ride-state.service';

import { VehicleFollowService } from '../../../../shared/services/vehicle-follow.service';
import { ActiveRideSimRunnerService } from '../../../../shared/services/active-ride-sim-runner.service';
import { RideApiService, RideETADTO } from '../../../../shared/api/ride-api.service';

import { VehicleMarker } from '../../../../shared/map/vehicle-marker';
import { LatLng } from '../../../../shared/services/routing.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { BookedRidesComponent } from "../../booked-rides/booked-rides";
import { LocationDTO } from '../../../../shared/models/location';
import { SuccessAlert } from '../../../../shared/components/success-alert';
import { NominatimService } from '../../../../shared/services/nominatim.service';

type Passenger = { name: string; email: string };

@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [CommonModule, MapComponent, BookedRidesComponent, SuccessAlert],
  templateUrl: './driver-dashboard.html',
})
export class DriverDashboard implements OnInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);

  private workMinutes = signal<number>(24);
  private readonly workLimitMinutes = 8 * 60;

  protected simulationCompleted = signal<boolean>(false);
  protected currentETA = signal<RideETADTO | null>(null);
  protected ridePhase = signal<'TO_PICKUP' | 'IN_PROGRESS' | 'IDLE'>('IDLE');

  ridesService = inject(DriverRidesService);
  userService = inject(CurrentUserService);
  rideState = inject(CurrentRideStateService);
  rideApi = inject(RideApiService);
  nominatim = inject(NominatimService);

  private follow = inject(VehicleFollowService);
  private simRunner = inject(ActiveRideSimRunnerService);

  private subs: Subscription[] = [];
  private driverEmail?: string;
  private etaPollSub?: Subscription;
  private currentRideId?: number;
  showStopSuccess = false;

  // map inputs
  vehicles: VehicleMarker[] = [];
  routePath: LatLng[] = [];
  routePoints: { lat: number; lon: number; label?: string }[] = [];

  readonly rides = this.ridesService.rides;
  readonly currentRide = this.ridesService.currentRide;

  constructor() {
    effect(() => {
      const r = this.currentRide();

      // Cleanup za stari ride
      if (this.currentRideId && (!r || r.id !== this.currentRideId)) {
        this.cleanupCurrentRideSubscriptions();
      }
      // scenario 1: nema voznje
      if (!r) {
        this.simulationCompleted.set(false);
        this.stopETAPolling();
        if (this.driverEmail) this.follow.stop(this.driverEmail);
        this.driverEmail = undefined;
        this.currentRideId = undefined;

        this.routePoints = [];
        this.vehicles = [];
        this.routePath = [];
        this.ridePhase.set('IDLE');
        return;
      }

      // scenario 2: ima voznje
      this.currentRideId = r.id;

      // route points
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

      // scenario 3: voznja je in progress
      if (r.status === 'IN_PROGRESS') {
        // Stopuj follow jer simulacija preuzima da ne bi bilo konflikata jer pocne auto da se krece izmedju dvije medjutacke
        if (this.driverEmail) {
          this.follow.stop(this.driverEmail);
        }

        // Pokreni simulaciju
        if (!this.simRunner.isRunning(r.id)) {
          console.log(`Starting simulation for ride ${r.id}`);
          this.simulationCompleted.set(false);
          this.simRunner.startForRide(r);
        } else {
          console.log(`Simulation already exists for ride ${r.id}`);
          const simulation = this.simRunner.getSimulation(r.id);
          if (simulation?.completed) {
            this.simulationCompleted.set(true);
            console.log(`Simulation is completed, enabling button`);
          }
        }
        // Subscribe na vehicles i routePath iz simulacije
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
        // Scenario 4: voznja je accepted, tj ceka start, prati vozaca
        if (r.driverEmail && r.driverEmail !== this.driverEmail) {
          if (this.driverEmail) this.follow.stop(this.driverEmail);
          this.driverEmail = r.driverEmail;

          // prati se pozicija vozaca i prikazuje se na mapi - ovdje nema rout patha jer ceka start, nema linije od vozila do pickupa
          this.follow.start(this.driverEmail, 1000);
          this.subs.push(
            this.follow.vehicle$(this.driverEmail).subscribe(v => {
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
    // ucita sve voznje vozaca
    this.ridesService.fetchRides().subscribe();

    this.userService.currentUser$.subscribe(current => {
      if (current) {
        this.workMinutes.set((current as Driver).workedMinutesLast24h);
      }
    });
    // ucita podatke o vozacu
    this.userService.fetchMe().subscribe();

    // osluskuj kad vozilo stane na pickup
    this.subs.push(
      this.simRunner.onPickupReached$.subscribe(rideId => {
        const current = this.currentRide();
        if (current && current.id === rideId) {
          this.ridePhase.set('IN_PROGRESS');
          console.log('Pickup reached! Now in progress phase.');
          this.cdr.detectChanges();
        }
      })
    );

    // osluskuje kad se simulacija zavrsi
    this.subs.push(
      this.simRunner.onSimulationComplete$.subscribe(rideId => {
        const current = this.currentRide();
        if (current && current.id === rideId) {
          this.simulationCompleted.set(true); // omoguci complete ride dugme
          this.stopETAPolling();
          this.cdr.detectChanges();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.cleanupCurrentRideSubscriptions();
    // NE stopuj sve simulacije - one treba da nastave u pozadini, ovo ispod je stop sve
    // this.simRunner.stopAll();
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

  onPanic(): void {
    if (this.rideState.panicSignal().pressed) return;

    const userId = this.userService.getCurrentUserId() ?? 0;
    const rideId = this.currentRide()?.id ?? 0;
    if (!rideId) return;

    this.rideState.setPanic(rideId, userId);
  }

  readonly bookedRides = computed(() =>
    this.rides()
      .filter(ride => (this.currentRide() !== null && ride.id !== this.currentRide()!.id))
      .map(ride => ({
        id: ride.id,
        date: this.formatDate(ride.scheduledTime),
        time: this.formatTime(ride.scheduledTime),
        from: ride.startLocation.address,
        to: ride.endLocation.address,
        passengers: ride.passengerEmails.length,
        requirements: this.formatRequirements(ride),
        status: ride.status,
      }))
  );

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

  requirementEmoji: Record<string, string> = {
    Baby: '🧸',
    Luxury: '🚗',
    Standard: '🧭',
    Pets: '🐾',
    Van: '🚐',
  };

  requirementClasses: Record<string, string> = {
    Baby: 'bg-[#FFEDD4] text-[#CA3500]',
    Luxury: 'bg-[#DBEAFE] text-[#1447E6]',
    Standard: 'bg-[#F3E8FF] text-[#8200DB]',
    Pets: 'bg-[#FEF3C6] text-[#BB4D00]',
    Van: 'bg-[#E0E7FF] text-[#432DD7]',
  };

  startCurrentRide() {
    const r = this.currentRide();
    if (!r) return;

    this.rideApi.startRide(r.id).subscribe({
      next: () => {
        this.ridesService.fetchRides().subscribe(); // backend mijenja status u in_progress i refresa
        this.ridePhase.set('TO_PICKUP');
      },
    });
  }

  completeCurrentRide() {
    const r = this.currentRide();

    if (!r || r.status !== 'IN_PROGRESS') {
      return;
    }

    this.ridesService.completeRide(r.id).subscribe({
      next: () => {
        this.simRunner.stopForRide(r.id);
        this.simulationCompleted.set(false);
        this.rideState.clearPanic(r.id);

        this.ridesService.fetchRides().subscribe();
      },
      error: (err) => console.error('Failed to complete ride', err)
    });
  }

  get workMinutesText(): string {
    return this.formatMinutes(this.workMinutes());
  }

  get workLimitText(): string {
    return this.formatMinutes(this.workLimitMinutes);
  }

  get workPercent(): number {
    const p = (this.workMinutes() / this.workLimitMinutes) * 100;
    return Math.max(0, Math.min(100, Math.round(p)));
  }

  private formatMinutes(total: number): string {
    const h = Math.floor(total / 60);
    const m = total % 60;
    return `${h}h ${m.toString().padStart(2, '0')}min`;
  }

  private formatDate(date: string | Date): string {
    const d = new Date(date);
    return d.toLocaleDateString('sr-RS', { day: '2-digit', month: '2-digit' });
  }

  private formatTime(date: string | Date): string {
    const d = new Date(date);
    return d.toLocaleTimeString('sr-RS', { hour: '2-digit', minute: '2-digit' });
  }

  private formatRequirements(ride: RideDTO): string[] {
    const requirements: string[] = [];
    if (ride.isBabyFriendly) requirements.push('Baby');
    if (ride.isPetsFriendly) requirements.push('Pets');
    if (ride.vehicleType) requirements.push(ride.vehicleType);
    return requirements;
  }

 stopRideEarly() {
  const r = this.currentRide();
  if (!r) return;

  const result = this.simRunner.stopRideEarly(r.id);

  if (result.location) {
    this.nominatim.getAddress(result.location.latitude, result.location.longitude)
      .subscribe({
        next: (address) => {
          const dto: LocationDTO = {
              latitude: result.location!.latitude,
              longitude: result.location!.longitude,
              address: address
          };

          this.rideApi.stopRideEarly(r.id, dto).subscribe({
            next: () => {
              this.ridesService.fetchRides().subscribe();
              this.simulationCompleted.set(true);
              this.showStopSuccess = true;
            }
          });
        }
      });
}
}
}

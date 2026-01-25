import {
  ChangeDetectorRef,
  Component,
  computed,
  effect,
  inject,
  OnDestroy,
  signal,
  Signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../../../../shared/map/map';

import { DriverRidesService } from '../../../services/driver-rides.service';
import { RideDTO, RideStatus } from '../../../../shared/models/ride';
import { UserService } from '../../../../../core/services/user.service';
import { Driver } from '../../../../shared/models/driver';
import { CurrentRideStateService } from '../../../../registered/services/current-ride-state.service';

import { VehicleFollowService } from '../../../../shared/services/vehicle-follow.service';
import { ActiveRideSimRunnerService } from '../../../../shared/services/active-ride-sim-runner.service';

import { VehicleMarker } from '../../../../shared/map/vehicle-marker';
import { LatLng } from '../../../../shared/services/routing.service';
import { Subscription } from 'rxjs';

type Passenger = { name: string; email: string };

type BookedRide = {
  id: number;
  date: string;
  time: string;
  from: string;
  to: string;
  passengers: number;
  requirements: string[];
  status: RideStatus;
};

@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [CommonModule, MapComponent],
  templateUrl: './driver-dashboard.html',
})
export class DriverDashboard implements OnDestroy {
  private cdr = inject(ChangeDetectorRef);

  private workMinutes = signal<number>(24);
  private readonly workLimitMinutes = 8 * 60;

  protected simulationCompleted = signal<boolean>(false);

  ridesService = inject(DriverRidesService);
  userService = inject(UserService);
  rideState = inject(CurrentRideStateService);

  private follow = inject(VehicleFollowService);
  private simRunner = inject(ActiveRideSimRunnerService);

  private subs: Subscription[] = [];
  private driverEmail?: string;

  // map inputs
  vehicles: VehicleMarker[] = [];
  routePath: LatLng[] = [];
  routePoints: { lat: number; lon: number; label?: string }[] = [];

  readonly rides = this.ridesService.rides;
  readonly currentRide = this.ridesService.currentRide;

  constructor() {
    effect(() => {
      const r = this.currentRide();

      // cleanup if no ride
      if (!r) {
        this.simulationCompleted.set(false);
        if (this.driverEmail) this.follow.stop(this.driverEmail);
        this.driverEmail = undefined;

        this.routePoints = [];
        this.vehicles = [];
        this.routePath = [];
        return;
      }

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

      // follow per driverEmail
      if (r.driverEmail && r.driverEmail !== this.driverEmail) {
        if (this.driverEmail) this.follow.stop(this.driverEmail);
        this.driverEmail = r.driverEmail;

        this.follow.start(this.driverEmail, 1000);
        this.subs.push(
          this.follow.vehicle$(this.driverEmail).subscribe(v => {
            this.vehicles = v ? [v] : [];
            this.cdr.detectChanges();
          })
        );
      }

      // start/stop simulation runner (root) based on ride status
      if (r.status === 'IN_PROGRESS') {
        this.simulationCompleted.set(false);
        this.simRunner.startForRide(r);
      } else {
        this.simRunner.stopForRide(r.id);
      }
    });
  }

  ngOnInit() {
    this.ridesService.fetchRides().subscribe();

    this.userService.currentUser$.subscribe(current => {
      if (current) {
        this.workMinutes.set((current as Driver).workedMinutesLast24h);
      }
    });

    this.userService.fetchMe().subscribe();
    this.rideState.loadPanic();

    this.subs.push(
      this.simRunner.onSimulationComplete$.subscribe(rideId => {
        const current = this.currentRide();
        if (current && current.id === rideId) {
          this.simulationCompleted.set(true);
          this.cdr.detectChanges();
        }
      })
    );
  }

  ngOnDestroy(): void {
    // NE STOPUJ SIMULACIJU OVDJE — runner je root i mora da nastavi dok ride traje
    if (this.driverEmail) this.follow.stop(this.driverEmail);
    this.subs.forEach(s => s.unsubscribe());
    this.subs = [];
  }

  
  onPanic(): void {
    if (this.rideState.panicSignal().pressed) return;

    const userId = this.userService.getCurrentUserId() ?? 0;
    const rideId = this.currentRide()?.id ?? 0;
    if (!rideId) return;

    this.rideState.setPanic(rideId, userId);
  }


  readonly bookedRides: Signal<BookedRide[]> = computed(() =>
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

  readonly currentRideStatus: Signal<RideStatus> = computed(() =>
    this.currentRide()?.status ?? 'PENDING'
  );

  readonly currentRideStart: Signal<string> = computed(() =>
    this.currentRide()?.startLocation.address ?? ''
  );

  readonly currentRideEnd: Signal<string> = computed(() =>
    this.currentRide()?.endLocation.address ?? ''
  );

  statusPillClasses: Record<RideStatus, string> = {
    PENDING: 'bg-gray-100 text-slate-700',
    ACCEPTED: 'bg-blue-100 text-blue-700',
    IN_PROGRESS: 'bg-green-100 text-green-700',
    COMPLETED: 'bg-gray-100 text-slate-700',
    CANCELLED: 'bg-red-100 text-red-700',
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

    this.ridesService.startRide(r.id).subscribe({
      next: () => this.ridesService.fetchRides().subscribe(),
    });
  }

  completeCurrentRide() {
    const r = this.currentRide();

    if (!r || r.status !== 'IN_PROGRESS' || !this.simulationCompleted()) {
      return;
    }

    this.ridesService.completeRide(r.id).subscribe({
      next: () => {
        this.ridesService.fetchRides().subscribe();
        this.simRunner.stopForRide(r.id);
        this.simulationCompleted.set(false);
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
}

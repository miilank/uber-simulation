import { Component, computed, inject, signal, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent} from '../../../../shared/map/map';
import { RidesService } from '../../../services/rides.service';
import { RideDTO, RideStatus } from '../../../../shared/models/ride';
import { UserService } from '../../../../../core/services/user.service';
import { Driver } from '../../../../shared/models/driver';
import { CurrentRideStateService } from '../../../../registered/services/current-ride-state.service';
import { NotificationService } from '../../../../../core/services/notification.service';

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
export class DriverDashboard {

  private workMinutes = signal<number>(24);
  private readonly workLimitMinutes = 8 * 60;

  ridesService = inject(RidesService);
  userService = inject(UserService);
  rideState = inject(CurrentRideStateService);
  notificationService = inject(NotificationService);
  readonly rides = this.ridesService.rides;
  readonly currentRide = this.ridesService.currentRide;

  ngOnInit() {
    this.ridesService.fetchRides().subscribe();
    this.userService.currentUser$.subscribe(current => {
          if (current) {
            this.workMinutes.set((current as Driver).workedMinutesLast24h);
          }
        });
    this.userService.fetchMe().subscribe();
    this.rideState.loadPanic();
  }
  onPanic(): void {
    if (this.rideState.panicSignal().pressed) return;

    const userId = this.userService.getCurrentUserId() ?? 0;
    const rideId = this.currentRide()?.id ?? 0;
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
      status: ride.status
    })))

  readonly currentPassengers: Signal<Passenger[]> = computed(() =>
    this.currentRide()?.passengerEmails.map(email => ({
      name: '',
      email
    })) ?? []
  );

  readonly currentRideStatus: Signal<RideStatus> = computed(() =>
    this.currentRide()?.status ?? 'PENDING'
  );

  readonly currentRideStart: Signal<string> = computed(() =>
    this.currentRide()?.startLocation.address ?? ''
  )

  readonly currentRideEnd: Signal<string> = computed(() =>
    this.currentRide()?.endLocation.address ?? ''
  )

  // currentPassengers: Passenger[] = [
  //   { name: 'Mirko Mirkovic', phone: '+381 63 111 2222' },
  //   { name: 'Jovan Markovic', phone: '+381 63 111 2222' },
  //   { name: 'Milan Kacarevic', phone: '+381 62 333 4444' },
  //   { name: 'Luka Petrovic', phone: '+381 60 555 6666' },
  //   { name: 'Ana Jovanovic', phone: '+381 69 777 8888' },
  // ];

  // bookedRides: BookedRide[] = [
  //   {
  //     id: 1,
  //     date: '18.08.',
  //     time: '22:30',
  //     from: 'Bulevar M. Pupina 10',
  //     to: 'Trg slobode 1',
  //     passengers: 2,
  //     requirements: ['Sedan', 'Baby'],
  //     status: 'Scheduled',
  //   },
  //   {
  //     id: 2,
  //     date: '18.08.',
  //     time: '22:40',
  //     from: 'Laze Telečkog 5',
  //     to: 'Bul. cara Lazara 56',
  //     passengers: 1,
  //     requirements: ['SUV'],
  //     status: 'Scheduled',
  //   },
  //   {
  //     id: 3,
  //     date: '18.08.',
  //     time: '23:10',
  //     from: 'Bulevar oslobođenja 1',
  //     to: 'Dunavski park',
  //     passengers: 3,
  //     requirements: ['Van', 'Pets', 'Baby'],
  //     status: 'Scheduled',
  //   },
  // ];

  statusPillClasses: Record<RideStatus, string> = {
    PENDING: 'bg-gray-100 text-slate-700',
    ACCEPTED: 'bg-blue-100 text-blue-700',
    IN_PROGRESS: 'bg-green-100 text-green-700',
    COMPLETED: '',
    CANCELLED: ''
  };

  requirementEmoji: Record<string, string> = {
    Baby: '🧸',
    Luxury: '🚗',
    SUV: '🧭',
    Pets: '🐾',
    Van: '🚐',
  };

  requirementClasses: Record<string, string> = {
    Baby: 'bg-[#FFEDD4] text-[#CA3500]',
    Luxury: 'bg-[#DBEAFE] text-[#1447E6]',
    SUV: 'bg-[#F3E8FF] text-[#8200DB]',
    Pets: 'bg-[#FEF3C6] text-[#BB4D00]',
    Van: 'bg-[#E0E7FF] text-[#432DD7]',
  };

  startCurrentRide() {
    this.ridesService.startRide(this.currentRide()!.id).subscribe();
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
    let requirements: string[] = [];
    if(ride.isBabyFriendly) requirements.push('Baby');
    if(ride.isPetsFriendly) requirements.push('Pets');

    if(ride.vehicleType) requirements.push(ride.vehicleType);

    return requirements;
  }

}

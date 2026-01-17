import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent} from '../../../../shared/map/map';

type Passenger = { name: string; phone: string };

type RideStatus = 'Scheduled' | 'Assigned' | 'Started';

type Requirement = 'Sedan' | 'SUV' | 'Baby' | 'Pets' | 'Van';

type BookedRide = {
  id: number;
  date: string;
  time: string;
  from: string;
  to: string;
  passengers: number;
  requirements: Requirement[];
  status: RideStatus;
};

@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [CommonModule, MapComponent],
  templateUrl: './driver-dashboard.html',
})
export class DriverDashboard {
  private readonly workMinutes = 265;     // 4h 25min
  private readonly workLimitMinutes = 480; // 8h

  currentRideStatus: RideStatus = 'Assigned';

  currentPassengers: Passenger[] = [
    { name: 'Mirko Mirkovic', phone: '+381 63 111 2222' },
    { name: 'Jovan Markovic', phone: '+381 63 111 2222' },
    { name: 'Milan Kacarevic', phone: '+381 62 333 4444' },
    { name: 'Luka Petrovic', phone: '+381 60 555 6666' },
    { name: 'Ana Jovanovic', phone: '+381 69 777 8888' },
  ];

  bookedRides: BookedRide[] = [
    {
      id: 1,
      date: '18.08.',
      time: '22:30',
      from: 'Bulevar M. Pupina 10',
      to: 'Trg slobode 1',
      passengers: 2,
      requirements: ['Sedan', 'Baby'],
      status: 'Scheduled',
    },
    {
      id: 2,
      date: '18.08.',
      time: '22:40',
      from: 'Laze Telečkog 5',
      to: 'Bul. cara Lazara 56',
      passengers: 1,
      requirements: ['SUV'],
      status: 'Scheduled',
    },
    {
      id: 3,
      date: '18.08.',
      time: '23:10',
      from: 'Bulevar oslobođenja 1',
      to: 'Dunavski park',
      passengers: 3,
      requirements: ['Van', 'Pets', 'Baby'],
      status: 'Scheduled',
    },
  ];

  statusPillClasses: Record<RideStatus, string> = {
    Scheduled: 'bg-gray-100 text-slate-700',
    Assigned: 'bg-blue-100 text-blue-700',
    Started: 'bg-green-100 text-green-700',
  };

  requirementEmoji: Record<Requirement, string> = {
    Baby: '🧸',
    Sedan: '🚗',
    SUV: '🧭',
    Pets: '🐾',
    Van: '🚐',
  };

  requirementClasses: Record<Requirement, string> = {
    Baby: 'bg-[#FFEDD4] text-[#CA3500]',
    Sedan: 'bg-[#DBEAFE] text-[#1447E6]',
    SUV: 'bg-[#F3E8FF] text-[#8200DB]',
    Pets: 'bg-[#FEF3C6] text-[#BB4D00]',
    Van: 'bg-[#E0E7FF] text-[#432DD7]',
  };

  get workMinutesText(): string {
    return this.formatMinutes(this.workMinutes);
  }

  get workLimitText(): string {
    return this.formatMinutes(this.workLimitMinutes);
  }

  get workPercent(): number {
    const p = (this.workMinutes / this.workLimitMinutes) * 100;
    return Math.max(0, Math.min(100, Math.round(p)));
  }

  private formatMinutes(total: number): string {
    const h = Math.floor(total / 60);
    const m = total % 60;
    return `${h}h ${m.toString().padStart(2, '0')}min`;
  }
}

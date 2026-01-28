import { Component, computed, EventEmitter, inject, Output, Signal } from '@angular/core';
import { RideDTO, RideStatus } from '../../../shared/models/ride';
import { DriverRidesService } from '../../services/driver-rides.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../../core/services/user.service';

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
  selector: 'app-booked-rides',
  imports: [CommonModule, FormsModule],
  templateUrl: './booked-rides.html',
})
export class BookedRidesComponent {
  ridesService = inject(DriverRidesService);
  userService = inject(UserService);
  readonly rides = this.ridesService.rides;
  readonly currentRide = this.ridesService.currentRide;

  cancelReason = '';
  isCancelOpen = false;
  readonly maxLength = 500;
  rideCancelledReason = "";

  isValidReason(): boolean {
    return this.cancelReason.trim().length >= 10 && this.cancelReason.trim().length <= this.maxLength;
  }

  cancelRide(rideId: number) {
    this.rideCancelledReason = this.cancelReason.trim();
    this.cancelReason = '';
    this.ridesService.cancelRide(rideId, this.userService.getCurrentUserId() ?? 0, this.rideCancelledReason).subscribe();
    this.closeCancel();
  }

  closeCancel() {
    this.isCancelOpen = false;
  }
  openCancel() {
    this.isCancelOpen = true;
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

import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  SimpleChanges,
  inject,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { DriverRidesService } from '../../../../services/driver-rides.service';
import { RideDetailDTO } from '../../../../../shared/models/ride';

interface RideHistoryItem {
  id: number;
  date: string;
  time: string;
  from: string;
  to: string;
  status: string;
  cancelledBy: string | null;
  panic: boolean;
  price: string;
}

@Component({
  selector: 'app-ride-details-drawer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details-drawer.html',
})
export class RideDetailsDrawer implements OnChanges {
  private driverService = inject(DriverRidesService);
  private cdr = inject(ChangeDetectorRef);

  @Input() open = false;
  @Input() ride: RideHistoryItem | null = null;
  @Output() close = new EventEmitter<void>();

  rideDetails: RideDetailDTO | null = null;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ride'] && this.ride?.id) {
      this.loadDetails();
    }
  }

  loadDetails() {
    if (!this.ride?.id) return;

    this.driverService.getRideDetails(this.ride.id).subscribe({
      next: (details) => {
        this.rideDetails = details;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading ride details:', err)
    });
  }

  get passengers() {
    return this.rideDetails?.passengers.map(p => ({
      name: `${p.firstName} ${p.lastName}`,
      email: p.email
    })) || [];
  }

  get startDateTime() {
    const time = this.rideDetails?.actualStartTime || this.rideDetails?.estimatedStartTime;
    if (!time) return 'N/A';

    const date = new Date(time);
    return date.toLocaleString('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  get endDateTime() {
    const time = this.rideDetails?.actualEndTime || this.rideDetails?.estimatedEndTime;
    if (!time) return 'N/A';

    const date = new Date(time);
    return date.toLocaleString('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  get duration() {
    if (!this.rideDetails?.actualStartTime || !this.rideDetails?.actualEndTime) return '';

    const start = new Date(this.rideDetails.actualStartTime).getTime();
    const end = new Date(this.rideDetails.actualEndTime).getTime();
    const diffMs = end - start;
    const minutes = Math.floor(diffMs / 60000);

    return `${minutes} minutes`;
  }

  get panicYes() {
    return this.rideDetails?.panicActivated || false;
  }

  get panicTimestamp() {
    if (!this.rideDetails?.panicActivatedAt) return '';
    const date = new Date(this.rideDetails.panicActivatedAt);
    return date.toLocaleString('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  get cancelReason() {
    return this.rideDetails?.cancellationReason || 'N/A';
  }

  get cancelTime() {
    if (!this.rideDetails?.cancellationTime) return '';
    const date = new Date(this.rideDetails.cancellationTime);
    return date.toLocaleString('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  get inconsistencies() {
    return this.rideDetails?.inconsistencies || [];
  }

  formatInconsistencyDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleString('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  get stoppedLocation() {
    return this.rideDetails?.stoppedLocation || 'N/A';
  }

  get stoppedTime() {
    if (!this.rideDetails?.stoppedAt) return 'N/A';
    const date = new Date(this.rideDetails.stoppedAt);
    return date.toLocaleString('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  get routeStops(): { label: string; address: string }[] {
    const wps = this.rideDetails?.waypoints ?? [];
    return wps
      .map((w, i) => ({ label: `Stop ${i + 1}`, address: w.address }))
      .filter(x => x.address.trim().length > 0);
  }

  onBackdropClick() {
    this.close.emit();
  }

  onClose() {
    this.close.emit();
  }
}

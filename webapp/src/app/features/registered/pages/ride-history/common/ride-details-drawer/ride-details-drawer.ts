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
import { DriverRidesService } from '../../../../../driver/services/driver-rides.service';
import { RideDetailDTO } from '../../../../../shared/models/ride';  
import { MapComponent } from '../../../../../shared/map/map';
import { NominatimService } from '../../../../../shared/services/nominatim.service';

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
  imports: [CommonModule, MapComponent],
  templateUrl: './ride-details-drawer.html',
})
export class PassengerRideDetailsDrawer implements OnChanges {
  private driverService = inject(DriverRidesService);
  private cdr = inject(ChangeDetectorRef);
  private nominatim = inject(NominatimService); 

  @Input() open = false;
  @Input() ride: RideHistoryItem | null = null;
  @Output() close = new EventEmitter<void>();

  rideDetails: RideDetailDTO | null = null;
  showMap = false;
  mapRoutePoints: { lat: number; lon: number; label?: string }[] = [];

  toggleMap() {
    this.showMap = !this.showMap;
    
    if (this.showMap && this.mapRoutePoints.length === 0) {
      this.geocodeRouteAddresses();
    }
  }

  private async geocodeRouteAddresses() {
    if (!this.rideDetails) return;
    const points: { lat: number; lon: number; label?: string }[] = [];

    try {
      if (this.rideDetails.startAddress) {
        const pickup = await this.nominatim.search(this.rideDetails.startAddress).toPromise();
        if (pickup && pickup.length > 0) {
          points.push({
            lat: Number(pickup[0].lat),
            lon: Number(pickup[0].lon),
            label: 'Pickup'
          });
        }
      }

      if (this.rideDetails.waypoints) {
        for (let i = 0; i < this.rideDetails.waypoints.length; i++) {
          const wp = this.rideDetails.waypoints[i];
          if (wp.address) {
            const result = await this.nominatim.search(wp.address).toPromise();
            if (result && result.length > 0) {
              points.push({
                lat: Number(result[0].lat),
                lon: Number(result[0].lon),
                label: `Stop ${i + 1}`
              });
            }
          }
        }
      }
      if (this.rideDetails.endAddress) {
        const destination = await this.nominatim.search(this.rideDetails.endAddress).toPromise();
        if (destination && destination.length > 0) {
          points.push({
            lat: Number(destination[0].lat),
            lon: Number(destination[0].lon),
            label: 'Destination'
          });
        }
      }

      this.mapRoutePoints = points;
      this.cdr.detectChanges();
    } catch (error) {
      console.error('Error geocoding addresses:', error);
    }
  }

  closeMap() {
    this.showMap = false;
  }
  ngOnChanges(changes: SimpleChanges) {
    if (changes['ride'] && this.ride?.id) {
      this.mapRoutePoints = [];
      this.loadDetails();
    }
  }

  loadDetails() {
    if (!this.ride?.id) return;

    this.driverService.getRideDetails(this.ride.id).subscribe({
      next: (details) => {
        this.rideDetails = details;
        if (this.showMap) {
          this.geocodeRouteAddresses();
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading ride details:', err)
    });
  }

  get driver() {
    return this.rideDetails?.driver ? [{
      name: `${this.rideDetails.driver.firstName} ${this.rideDetails.driver.lastName}`,
      email: this.rideDetails.driver.email
    }] : [];
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
    this.showMap = false;
    this.close.emit();
  }

  onClose() {
    this.showMap = false;
    this.close.emit();
  }
}

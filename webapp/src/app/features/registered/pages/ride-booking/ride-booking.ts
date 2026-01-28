import { ChangeDetectorRef, Component } from '@angular/core';
import { MapComponent } from "../../../shared/map/map";
import { VehicleMarker } from '../../../shared/map/vehicle-marker';
import { VehiclesApiService } from '../../../shared/api/vehicles-api.service';

@Component({
  selector: 'ride-booking',
  imports: [MapComponent],
  templateUrl: './ride-booking.html',
})
export class RideBooking {
  vehicles: VehicleMarker[] = [];

  constructor(private vehiclesApi: VehiclesApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.vehiclesApi.getMapVehicles().subscribe({
      next: (data) => {
        this.vehicles = data
        this.cdr.detectChanges()
      },
      error: (err) => console.error('Failed to load vehicles', err),
    });
  }
}

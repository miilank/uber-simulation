import { Component, OnInit } from '@angular/core';
import { MapComponent } from '../../../../shared/map/map';
import { HeaderComponent } from '../../../../shared/components/header.component';
import { RegisteredSidebar } from '../../../components/registered-sidebar';
import { VehiclesApiService } from '../../../../shared/api/vehicles-api.service';
import { VehicleMarker } from '../../../../shared/map/vehicle-marker';

@Component({
  selector: 'app-registered-dashboard',
  standalone: true,
  imports: [MapComponent],
  templateUrl: './registered-dashboard.html',
})
export class RegisteredDashboard implements OnInit {
  vehicles: VehicleMarker[] = [];

  constructor(private vehiclesApi: VehiclesApiService) {}

  ngOnInit(): void {
    this.vehiclesApi.getMapVehicles().subscribe({
      next: (data) => (this.vehicles = data),
      error: (err) => console.error('Failed to load vehicles', err),
    });
  }
}

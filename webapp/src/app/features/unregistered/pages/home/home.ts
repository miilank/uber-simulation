import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../../shared/components/header.component';
import { MapComponent } from '../../../shared/map/map';
import { VehicleMarker } from '../../../shared/map/vehicle-marker';
import { VehiclesApiService } from '../../../shared/api/vehicles-api.service';
import { EstimatePanelComponent } from '../../components/estimate-window.component';

@Component({
  selector: 'app-unregistered-home',
  standalone: true,
  imports: [HeaderComponent, MapComponent, EstimatePanelComponent],
  templateUrl: './home.html',
})
export class UnregisteredHomeComponent implements OnInit {
  vehicles: VehicleMarker[] = [];

  constructor(private vehiclesApi: VehiclesApiService) {}

  ngOnInit(): void {
    this.vehiclesApi.getMapVehicles().subscribe({
      next: (data) => (this.vehicles = data),
      error: (err) => console.error('Failed to load vehicles', err),
    });
  }
}

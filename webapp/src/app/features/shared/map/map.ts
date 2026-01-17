import {
  AfterViewInit,
  Component,
  Inject,
  Input,
  OnChanges,
  PLATFORM_ID,
  SimpleChanges} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import type * as Leaflet from 'leaflet';
import type {Map as LeafletMap, LayerGroup as LeafletLayerGroup} from 'leaflet';
import { VehicleMarker } from './vehicle-marker';

@Component({
  selector: 'app-map',
  standalone: true,
  templateUrl: './map.html',
})
export class MapComponent implements AfterViewInit, OnChanges {
  @Input() vehicles: VehicleMarker[] = [];

  private L?: typeof Leaflet;
  private map?: LeafletMap;
  private markersLayer?: LeafletLayerGroup;

  constructor(@Inject(PLATFORM_ID) private platformId: object) {}

  async ngAfterViewInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;

    this.L = (await import('leaflet')) as unknown as typeof Leaflet;

    this.map = this.L.map('map', {
      center: [45.2499, 19.8399],
      zoom: 13,
    });

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
    }).addTo(this.map);

    this.markersLayer = this.L.layerGroup().addTo(this.map);
    setTimeout(()=> this.map?.invalidateSize(), 0);

    this.renderVehicles();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(changes['vehicles'] && this.map && this.markersLayer) {
      this.renderVehicles();
    }
  }

  private renderVehicles(): void {
    if (!this.L || !this.map || !this.markersLayer) return;

    this.markersLayer.clearLayers();

    const freeIcon = this.L.icon({
      iconUrl: '/icons/free-car.png',
      iconSize: [18, 18],
      iconAnchor: [18, 18],
      tooltipAnchor: [0, -18],
    });

    const busyIcon = this.L.icon({
      iconUrl: '/icons/busy-car.png',
      iconSize: [18, 18],
      iconAnchor: [18, 18],
      tooltipAnchor: [0, -18],
    });

    for (const v of this.vehicles) {
      const icon = v.status === 'AVAILABLE' ? freeIcon : busyIcon;

      this.L.marker([v.lat, v.lng], { icon })
        .addTo(this.markersLayer)
        .bindTooltip(`Vehicle #${v.id} • ${v.status}`, { direction: 'top' });
    }
  }
}

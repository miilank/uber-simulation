import {
  AfterViewInit,
  Component,
  Inject,
  Input,
  OnChanges,
  PLATFORM_ID,
  SimpleChanges,
  effect,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import type * as Leaflet from 'leaflet';
import type { Map as LeafletMap, LayerGroup as LeafletLayerGroup } from 'leaflet';
import { VehicleMarker } from './vehicle-marker';
import { EstimatePanelComponent } from '../../unregistered/components/estimate-window.component';
import { RideBookingStateService } from '../../../core/services/ride-booking-state.service';
import { NominatimResult } from '../services/nominatim.service';

@Component({
  selector: 'app-map',
  standalone: true,
  templateUrl: './map.html',
  imports: [EstimatePanelComponent],
})
export class MapComponent implements AfterViewInit, OnChanges {
  @Input() vehicles: VehicleMarker[] = [];

  private L?: typeof Leaflet;
  private map?: LeafletMap;

  private vehiclesLayer?: LeafletLayerGroup;
  private bookingLayer?: LeafletLayerGroup;

  constructor(
    @Inject(PLATFORM_ID) private platformId: object,
    public bookingState: RideBookingStateService
  ) {
    effect(() => {
      const pickup = this.bookingState.pickup();
      const dropoff = this.bookingState.dropoff();
      const stops = this.bookingState.stops();

      this.renderBooking(pickup, stops, dropoff);
    });
  }

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

    this.vehiclesLayer = this.L.layerGroup().addTo(this.map);
    this.bookingLayer = this.L.layerGroup().addTo(this.map);

    setTimeout(() => this.map?.invalidateSize(), 0);

    this.renderVehicles();
    this.renderBooking(
      this.bookingState.pickup(),
      this.bookingState.stops(),
      this.bookingState.dropoff()
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['vehicles'] && this.map && this.vehiclesLayer) {
      this.renderVehicles();
    }
  }

  private renderVehicles(): void {
    if (!this.L || !this.map || !this.vehiclesLayer) return;

    this.vehiclesLayer.clearLayers();

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
        .addTo(this.vehiclesLayer)
        .bindTooltip(`Vehicle #${v.id} • ${v.status}`, { direction: 'top' });
    }
  }

  private toLatLng(r: NominatimResult): [number, number] {
    return [Number(r.lat), Number(r.lon)];
  }

  private renderBooking(
    pickup: NominatimResult | null,
    stops: (NominatimResult | null)[],
    dropoff: NominatimResult | null
  ): void {
    if (!this.L || !this.map || !this.bookingLayer) return;

    this.bookingLayer.clearLayers();

    const points: [number, number][] = [];

    // pickup
    if (pickup) {
      const p = this.toLatLng(pickup);
      points.push(p);
      this.L.circleMarker(p, { radius: 7 }).addTo(this.bookingLayer)
        .bindTooltip('Pickup', { direction: 'top' });
    }

    // stop
    for (const s of stops) {
      if (!s) continue;
      const pt = this.toLatLng(s);
      points.push(pt);
      this.L.circleMarker(pt, { radius: 6 }).addTo(this.bookingLayer)
        .bindTooltip('Stop', { direction: 'top' });
    }

    // dropoff
    if (dropoff) {
      const d = this.toLatLng(dropoff);
      points.push(d);
      this.L.circleMarker(d, { radius: 7 }).addTo(this.bookingLayer)
        .bindTooltip('Destination', { direction: 'top' });
    }


    if (points.length >= 2) {
      this.buildFullRoute(points).then(route => {
        this.L!.polyline(route).addTo(this.bookingLayer!);
      });
    }
  }

  private async fetchRoute(
    from: [number, number],
    to: [number, number]
  ): Promise<[number, number][]> {

    const url =
      `https://router.project-osrm.org/route/v1/driving/` +
      `${from[1]},${from[0]};${to[1]},${to[0]}` +
      `?overview=full&geometries=geojson`;

    const res = await fetch(url);
    const data = await res.json();

    return data.routes[0].geometry.coordinates
      .map((c: [number, number]) => [c[1], c[0]]);
  }

  private async buildFullRoute(points: [number, number][]) {
    if (points.length < 2) return [];

    let fullRoute: [number, number][] = [];

    for (let i = 0; i < points.length - 1; i++) {
      const segment = await this.fetchRoute(points[i], points[i + 1]);

      // avoid duplicate connection point
      if (i > 0) segment.shift();

      fullRoute = fullRoute.concat(segment);
    }

    return fullRoute;
  }
}

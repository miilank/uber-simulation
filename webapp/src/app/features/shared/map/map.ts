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
import { Router, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';
import type * as Leaflet from 'leaflet';
import type { Map as LeafletMap, LayerGroup as LeafletLayerGroup } from 'leaflet';
import { VehicleMarker } from './vehicle-marker';
import { RideBookingStateService } from '../../../core/services/ride-booking-state.service';
import { NominatimResult } from '../services/nominatim.service';

@Component({
  selector: 'app-map',
  standalone: true,
  templateUrl: './map.html',
  imports: [],
})
export class MapComponent implements AfterViewInit, OnChanges {
  @Input() vehicles: VehicleMarker[] = [];

  private L?: typeof Leaflet;
  private map?: LeafletMap;

  private vehiclesLayer?: LeafletLayerGroup;
  private bookingLayer?: LeafletLayerGroup;

  private bookingToken = 0;
  private bookingAbort?: AbortController;

  private mapReady = false;

  private routeOwnerUrl: string | null = null;
  private lastNavUrl: string;

  constructor(
    @Inject(PLATFORM_ID) private platformId: object,
    private router: Router,
    public bookingState: RideBookingStateService
  ) {
    this.lastNavUrl = this.router.url;

    this.router.events
      .pipe(filter((e): e is NavigationStart => e instanceof NavigationStart))
      .subscribe((e) => {
        if (e.url !== this.lastNavUrl) {
          this.lastNavUrl = e.url;
          this.routeOwnerUrl = null;
          this.bookingState.reset();
          this.clearBookingLayerOnly();
        }
      });

    effect(() => {
      const pickup = this.bookingState.pickup();
      const dropoff = this.bookingState.dropoff();
      const stops = this.bookingState.stops();

      if (!this.mapReady) return;

      const hasAnyPoint =
        !!pickup || !!dropoff || (stops?.some(s => !!s) ?? false);

      if (!hasAnyPoint) {
        this.routeOwnerUrl = null;
        this.clearBookingLayerOnly();
        this.bookingState.setRouteInfo({ totalDistance: 0, totalDuration: 0 });
        return;
      }

      this.routeOwnerUrl = this.router.url;
      void this.renderBooking(pickup, stops, dropoff);
    });
  }

  async ngAfterViewInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;

    this.L = (await import('leaflet')) as unknown as typeof Leaflet;

    this.map = this.L.map('map', {
      center: [45.2655, 19.8399],
      zoom: 13,
    });

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
    }).addTo(this.map);

    this.vehiclesLayer = this.L.layerGroup().addTo(this.map);
    this.bookingLayer = this.L.layerGroup().addTo(this.map);

    setTimeout(() => this.map?.invalidateSize(), 0);

    this.mapReady = true;

    this.renderVehicles();
    await this.renderBooking(
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

  private clearBookingLayerOnly(): void {
    this.bookingToken++;
    this.bookingAbort?.abort();
    this.bookingAbort = undefined;
    this.bookingLayer?.clearLayers();
  }

  private renderVehicles(): void {
    if (!this.L || !this.map || !this.vehiclesLayer) return;

    this.vehiclesLayer.clearLayers();

    const freeIcon = this.L.icon({
      iconUrl: '/icons/free-car.png',
      iconSize: [18, 18],
      iconAnchor: [9, 9],
      tooltipAnchor: [0, -9],
    });

    const busyIcon = this.L.icon({
      iconUrl: '/icons/busy-car.png',
      iconSize: [18, 18],
      iconAnchor: [9, 9],
      tooltipAnchor: [0, -9],
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

  private async renderBooking(
    pickup: NominatimResult | null,
    stops: (NominatimResult | null)[],
    dropoff: NominatimResult | null
  ): Promise<void> {
    if (!this.L || !this.map || !this.bookingLayer) return;

    const ownerAtStart = this.routeOwnerUrl;
    const urlAtStart = this.router.url;

    if (!ownerAtStart || ownerAtStart !== urlAtStart) {
      this.clearBookingLayerOnly();
      return;
    }

    const myToken = ++this.bookingToken;

    this.bookingAbort?.abort();
    this.bookingAbort = new AbortController();
    const signal = this.bookingAbort.signal;

    this.bookingLayer.clearLayers();

    const points: [number, number][] = [];

    if (pickup) {
      const p = this.toLatLng(pickup);
      points.push(p);
      this.L.circleMarker(p, { radius: 7 }).addTo(this.bookingLayer).bindTooltip('Pickup', { direction: 'top' });
    }

    for (const s of stops) {
      if (!s) continue;
      const pt = this.toLatLng(s);
      points.push(pt);
      this.L.circleMarker(pt, { radius: 6 }).addTo(this.bookingLayer).bindTooltip('Stop', { direction: 'top' });
    }

    if (dropoff) {
      const d = this.toLatLng(dropoff);
      points.push(d);
      this.L.circleMarker(d, { radius: 7 }).addTo(this.bookingLayer).bindTooltip('Destination', { direction: 'top' });
    }

    if (points.length < 2) {
      this.bookingState.setRouteInfo({ totalDistance: 0, totalDuration: 0 });
      return;
    }

    try {
      const { totalDistance, totalDuration, routeCoords } = await this.buildFullRoute(points, signal);

      if (myToken !== this.bookingToken) return;
      if (this.routeOwnerUrl !== ownerAtStart) return;
      if (this.router.url !== urlAtStart) return;

      if (routeCoords.length > 0) {
        this.L.polyline(routeCoords).addTo(this.bookingLayer);
      }

      this.bookingState.setRouteInfo({ totalDistance, totalDuration });
    } catch (e: any) {
      if (e?.name === 'AbortError') return;
      return;
    }
  }

  private async fetchRoute(
    from: [number, number],
    to: [number, number],
    signal: AbortSignal
  ): Promise<{ distance: number; duration: number; coordinates: [number, number][] }> {
    const url =
      `https://router.project-osrm.org/route/v1/driving/` +
      `${from[1]},${from[0]};${to[1]},${to[0]}` +
      `?overview=full&geometries=geojson`;

    const res = await fetch(url, { signal });
    const data = await res.json();

    const route = data?.routes?.[0];
    if (!route?.geometry?.coordinates) return { distance: 0, duration: 0, coordinates: [] };

    return {
      distance: route.distance ?? 0,
      duration: route.duration ?? 0,
      coordinates: route.geometry.coordinates.map((c: [number, number]) => [c[1], c[0]]),
    };
  }

  private async buildFullRoute(points: [number, number][], signal: AbortSignal): Promise<{
    totalDistance: number;
    totalDuration: number;
    routeCoords: [number, number][];
  }> {
    let fullRoute: [number, number][] = [];
    let totalDistance = 0;
    let totalDuration = 0;

    for (let i = 0; i < points.length - 1; i++) {
      const segment = await this.fetchRoute(points[i], points[i + 1], signal);
      totalDistance += segment.distance;
      totalDuration += segment.duration;

      const coords = segment.coordinates;
      if (i > 0 && coords.length > 0) coords.shift();
      if (coords.length > 0) fullRoute = fullRoute.concat(coords);
    }

    return { totalDistance, totalDuration, routeCoords: fullRoute };
  }
}

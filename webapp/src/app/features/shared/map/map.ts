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
import { RoutingService, LatLng } from '../services/routing.service';

type RouteLabels = { start?: string; mid?: string; end?: string };
type DrawState = { token: number; abort?: AbortController };

@Component({
  selector: 'app-map',
  standalone: true,
  templateUrl: './map.html',
  imports: [],
})

export class MapComponent implements AfterViewInit, OnChanges {
  @Input() vehicles: VehicleMarker[] = [];
  @Input() routeStart?: { lat: number; lon: number } | null;
  @Input() routeEnd?: { lat: number; lon: number } | null;
  @Input() routePath: [number, number][] = [];
  @Input() routePoints: { lat: number; lon: number; label?: string }[] = [];

  private L?: typeof Leaflet;
  private map?: LeafletMap;
  private vehiclesLayer?: LeafletLayerGroup;
  private bookingLayer?: LeafletLayerGroup;
  private routeLayer?: LeafletLayerGroup;
  private mapReady = false;
  private routeOwnerUrl: string | null = null;
  private lastNavUrl: string;
  private bookingDrawState: DrawState = { token: 0 };
  private currentRideDrawState: DrawState = { token: 0 };

  constructor(
    @Inject(PLATFORM_ID) private platformId: object,
    private router: Router,
    public bookingState: RideBookingStateService,
    private routing: RoutingService
  ) {
    this.lastNavUrl = this.router.url;

    this.router.events
      .pipe(filter((e): e is NavigationStart => e instanceof NavigationStart))
      .subscribe((e) => {
        if (e.url !== this.lastNavUrl) {
          this.lastNavUrl = e.url;
          this.routeOwnerUrl = null;
          this.bookingState.reset();
          this.clearLayer(this.bookingLayer, this.bookingDrawState);
        }
      });

    effect(() => {
      const pickup = this.bookingState.pickup();
      const dropoff = this.bookingState.dropoff();
      const stops = this.bookingState.stops();

      if (!this.mapReady) return;

      const hasAnyPoint = !!pickup || !!dropoff || (stops?.some(s => !!s) ?? false);

      if (!hasAnyPoint) {
        this.routeOwnerUrl = null;
        this.clearLayer(this.bookingLayer, this.bookingDrawState);
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
    this.routeLayer = this.L.layerGroup().addTo(this.map);

    setTimeout(() => this.map?.invalidateSize(), 0);

    this.mapReady = true;

    this.renderVehicles();

    await this.renderBooking(
      this.bookingState.pickup(),
      this.bookingState.stops(),
      this.bookingState.dropoff()
    );

    void this.renderRouteInputs();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['vehicles'] && this.map && this.vehiclesLayer) {
      this.renderVehicles();
    }

    if ((changes['routePoints'] || changes['routePath'] || changes['routeStart'] || changes['routeEnd']) && this.map && this.routeLayer) {
      void this.renderRouteInputs();
    }
  }

  private clearLayer(layer: LeafletLayerGroup | undefined, state: DrawState): void {
    state.token++;
    state.abort?.abort();
    state.abort = undefined;
    layer?.clearLayers();
  }

  private toLatLngFromNominatim(r: NominatimResult): LatLng {
    return [Number(r.lat), Number(r.lon)];
  }

  private async drawRouteOnLayer(
    points: LatLng[],
    layer: LeafletLayerGroup,
    state: DrawState,
    labels: RouteLabels,
    fit = true
  ): Promise<{ totalDistance: number; totalDuration: number; routeCoords: LatLng[] }> {
    if (!this.L || !this.map) return { totalDistance: 0, totalDuration: 0, routeCoords: [] };

    const myToken = ++state.token;
    state.abort?.abort();
    state.abort = new AbortController();
    const signal = state.abort.signal;

    layer.clearLayers();

    for (let i = 0; i < points.length; i++) {
      const label =
        i === 0 ? (labels.start ?? 'From')
          : i === points.length - 1 ? (labels.end ?? 'To')
            : (labels.mid ?? 'Stop');

      const radius = (i === 0 || i === points.length - 1) ? 7 : 6;
      this.L.circleMarker(points[i], { radius })
        .addTo(layer)
        .bindTooltip(label, { direction: 'top' });
    }

    if (points.length < 2) {
      return { totalDistance: 0, totalDuration: 0, routeCoords: [] };
    }

    const { totalDistance, totalDuration, routeCoords } = await this.routing.buildFullRoute(points, signal);

    if (myToken !== state.token) return { totalDistance: 0, totalDuration: 0, routeCoords: [] };

    if (routeCoords.length > 0) {
      this.L.polyline(routeCoords).addTo(layer);
      if (fit) this.map.fitBounds(this.L.latLngBounds(routeCoords), { padding: [20, 20] });
    }

    return { totalDistance, totalDuration, routeCoords };
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

  private async renderBooking(
    pickup: NominatimResult | null,
    stops: (NominatimResult | null)[],
    dropoff: NominatimResult | null
  ): Promise<void> {
    if (!this.L || !this.map || !this.bookingLayer) return;

    const ownerAtStart = this.routeOwnerUrl;
    const urlAtStart = this.router.url;

    if (!ownerAtStart || ownerAtStart !== urlAtStart) {
      this.clearLayer(this.bookingLayer, this.bookingDrawState);
      return;
    }

    const points: LatLng[] = [];

    if (pickup) points.push(this.toLatLngFromNominatim(pickup));
    for (const s of stops) if (s) points.push(this.toLatLngFromNominatim(s));
    if (dropoff) points.push(this.toLatLngFromNominatim(dropoff));

    try {
      const { totalDistance, totalDuration } = await this.drawRouteOnLayer(
        points,
        this.bookingLayer,
        this.bookingDrawState,
        { start: 'Pickup', mid: 'Stop', end: 'Destination' },
        false
      );

      if (this.routeOwnerUrl !== ownerAtStart) return;
      if (this.router.url !== urlAtStart) return;

      this.bookingState.setRouteInfo({ totalDistance, totalDuration });
    } catch (e: any) {
      if (e?.name === 'AbortError') return;
    }
  }

  private async renderRouteInputs(): Promise<void> {
    if (!this.L || !this.map || !this.routeLayer) return;

    if ((!this.routePoints || this.routePoints.length < 2) && (!this.routePath || this.routePath.length < 2)) {
      this.clearLayer(this.routeLayer, this.currentRideDrawState);
      return;
    }

    this.clearLayer(this.routeLayer, this.currentRideDrawState);

    if (this.routePoints?.length) {
      this.routePoints.forEach((p, i) => {
        const label = p.label ?? (i === 0 ? 'Pickup' : i === this.routePoints.length - 1 ? 'Destination' : `Stop ${i}`);
        const radius = (i === 0 || i === this.routePoints.length - 1) ? 7 : 6;

        this.L!.circleMarker([p.lat, p.lon], { radius })
          .addTo(this.routeLayer!)
          .bindTooltip(label, { direction: 'top' });
      });
    }

    if (this.routePath && this.routePath.length >= 2) {
      this.L.polyline(this.routePath as any).addTo(this.routeLayer);
      this.map.fitBounds(this.L.latLngBounds(this.routePath as any), { padding: [20, 20] });
      return;
    }

    const points: LatLng[] = (this.routePoints ?? []).map(p => [p.lat, p.lon]);

    try {
      const { routeCoords } = await this.routing.buildFullRoute(points, this.currentRideDrawState.abort?.signal ?? undefined as any);
      if (routeCoords.length) {
        this.L.polyline(routeCoords).addTo(this.routeLayer);
        this.map.fitBounds(this.L.latLngBounds(routeCoords), { padding: [20, 20] });
      }
    } catch (e: any) {
      if (e?.name === 'AbortError') return;
    }
  }
}

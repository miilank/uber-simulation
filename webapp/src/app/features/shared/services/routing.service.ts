import { Injectable } from '@angular/core';

export type LatLng = [number, number];  // tacka predstavljena kao tuple

export type RouteSegment = {  // jedan segment rute: metri, sekunde, LatLng
  distance: number;
  duration: number;
  coordinates: LatLng[];
};

@Injectable({ providedIn: 'root' })
export class RoutingService {
  private readonly baseUrl = 'https://router.project-osrm.org';

  // ovo zove OSRM
  async fetchRoute(from: LatLng, to: LatLng, signal: AbortSignal): Promise<RouteSegment> {
    const url =
      `${this.baseUrl}/route/v1/driving/` +
      `${from[1]},${from[0]};${to[1]},${to[0]}` +
      `?overview=full&geometries=geojson`;

    const res = await fetch(url, { signal });
    const data = await res.json();

    const route = data?.routes?.[0];
    if (!route?.geometry?.coordinates) {
      return { distance: 0, duration: 0, coordinates: [] };
    }

    return {
      distance: route.distance ?? 0,
      duration: route.duration ?? 0,
      coordinates: route.geometry.coordinates.map((c: [number, number]) => [c[1], c[0]] as LatLng),
    };
  }

  // pravi rutu kroz vise tacaka
  async buildFullRoute(points: LatLng[], signal: AbortSignal): Promise<{
    totalDistance: number;
    totalDuration: number;
    routeCoords: LatLng[];
  }> {
    let fullRoute: LatLng[] = [];
    let totalDistance = 0;
    let totalDuration = 0;

    for (let i = 0; i < points.length - 1; i++) {
      const seg = await this.fetchRoute(points[i], points[i + 1], signal);
      totalDistance += seg.distance;
      totalDuration += seg.duration;

      const coords = [...seg.coordinates];
      if (i > 0 && coords.length > 0) coords.shift();
      if (coords.length > 0) fullRoute = fullRoute.concat(coords);
    }

    return { totalDistance, totalDuration, routeCoords: fullRoute };
  }
}

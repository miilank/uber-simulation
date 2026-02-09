// Initialize the map (Novi Sad center)
const map = L.map("map").setView([45.2671, 19.8335], 13);

// Base map tiles (OpenStreetMap)
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
  maxZoom: 18,
}).addTo(map);

// Map of markers: vehicleId (string) -> Leaflet marker instance
const markers = new Map();

// Local icons served from the app's assets folder:
const freeIcon = L.icon({
  iconUrl: "icons/img_free_car.png",
  iconSize: [20, 20],
  iconAnchor: [12, 24],
});

const busyIcon = L.icon({
  iconUrl: "icons/img_busy_car.png",
  iconSize: [20, 20],
  iconAnchor: [12, 24],
});

// Called from Android:
// window.setVehiclesJson("<json-string>")
window.setVehiclesJson = function (vehiclesJson) {
  let vehicles;

  // Parse JSON payload from Android
  try {
    vehicles = JSON.parse(vehiclesJson);
  } catch (e) {
    console.error("Invalid JSON from Android:", e);
    return;
  }

  // Remove markers that are no longer present in the incoming list
  const incomingIds = new Set(vehicles.map((v) => String(v.id)));
  for (const [id, marker] of markers.entries()) {
    if (!incomingIds.has(id)) {
      map.removeLayer(marker);
      markers.delete(id);
    }
  }

  // Add or update markers
  for (const v of vehicles) {
    const id = String(v.id);
    const icon = v.status === "AVAILABLE" ? freeIcon : busyIcon;
    const pos = [v.lat, v.lng];

    if (markers.has(id)) {
      // Update existing marker
      const m = markers.get(id);
      m.setLatLng(pos);
      m.setIcon(icon);
      m.setTooltipContent(`Vehicle #${v.id} • ${v.status}`);
    } else {
      // Create new marker
      const m = L.marker(pos, { icon })
        .addTo(map)
        .bindTooltip(`Vehicle #${v.id} • ${v.status}`, { direction: "top" });

      markers.set(id, m);
    }
  }
};

// ROUTE LAYER
let routeLayer = L.layerGroup().addTo(map);
const OSRM_BASE = "https://router.project-osrm.org";

let routeAbort = null;

async function fetchRouteSegment(from, to, signal) {
  const url =
    `${OSRM_BASE}/route/v1/driving/` +
    `${from[1]},${from[0]};${to[1]},${to[0]}` +
    `?overview=full&geometries=geojson`;

  const res = await fetch(url, { signal });
  const data = await res.json();

  const route = data?.routes?.[0];
  if (!route?.geometry?.coordinates) return [];

  // osrm lonlat to leaflet latlon
  return route.geometry.coordinates.map((c) => [c[1], c[0]]);
}

async function buildFullRoute(points, signal) {
  let full = [];
  for (let i = 0; i < points.length - 1; i++) {
    const coords = await fetchRouteSegment(points[i], points[i + 1], signal);
    if (i > 0 && coords.length) coords.shift(); // avoid duplicates
    full = full.concat(coords);
  }
  return full;
}

window.setRoutePointsJson = async function (routePointsJson) {
  let pts;
  try {
    pts = JSON.parse(routePointsJson);
  } catch (e) {
    console.error("Invalid routePoints JSON:", e);
    return;
  }

  routeLayer.clearLayers();
  if (routeAbort) routeAbort.abort();
  routeAbort = new AbortController();
  const signal = routeAbort.signal;

  const points = (pts ?? [])
    .filter((p) => p && typeof p.lat === "number" && typeof p.lon === "number")
    .map((p) => [p.lat, p.lon]);

  if (points.length < 2) return;

  // draw point markers
  pts.forEach((p, i) => {
    if (!p) return;
    const isEnd = i === 0 || i === pts.length - 1;
    const radius = isEnd ? 7 : 6;
    const label =
      p.label ??
      (i === 0 ? "Pickup" : i === pts.length - 1 ? "Destination" : `Stop ${i}`);

    L.circleMarker([p.lat, p.lon], { radius })
      .addTo(routeLayer)
      .bindTooltip(label, { direction: "top" });
  });

  try {
    const routeCoords = await buildFullRoute(points, signal);
    if (routeCoords.length) {
      L.polyline(routeCoords).addTo(routeLayer);
      // fit so user sees full route
      map.fitBounds(L.latLngBounds(routeCoords), { padding: [20, 20] });
    }
  } catch (e) {
    if (e?.name === "AbortError") return;
    console.error("Route draw failed:", e);
  }
};

window.clearRoute = function () {
  routeLayer.clearLayers();
  if (routeAbort) routeAbort.abort();
};

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

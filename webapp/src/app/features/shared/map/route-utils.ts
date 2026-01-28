export type LatLng = [number, number];

export function haversineMeters(a: LatLng, b: LatLng): number {
  const R = 6371000;
  const toRad = (x: number) => (x * Math.PI) / 180;
  const dLat = toRad(b[0] - a[0]);
  const dLon = toRad(b[1] - a[1]);
  const lat1 = toRad(a[0]);
  const lat2 = toRad(b[0]);

  const s =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) ** 2;

  return 2 * R * Math.asin(Math.sqrt(s));
}

function lerp(a: number, b: number, t: number) {
  return a + (b - a) * t;
}

export function resamplePolyline(coords: LatLng[], stepMeters = 100): LatLng[] {
  if (coords.length < 2) return coords;

  const out: LatLng[] = [coords[0]];
  let carry = 0;

  for (let i = 0; i < coords.length - 1; i++) {
    const p0 = coords[i];
    const p1 = coords[i + 1];
    const segLen = haversineMeters(p0, p1);

    let dist = carry;
    while (dist + stepMeters <= segLen) {
      dist += stepMeters;
      const t = dist / segLen;
      out.push([lerp(p0[0], p1[0], t), lerp(p0[1], p1[1], t)]);
    }

    carry = segLen - dist;
  }

  const last = coords[coords.length - 1];
  const lastOut = out[out.length - 1];
  if (!lastOut || lastOut[0] !== last[0] || lastOut[1] !== last[1]) out.push(last);

  return out;
}

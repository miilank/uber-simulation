export type VehicleStatus = 'AVAILABLE' | 'OCCUPIED';

export interface VehicleMarker {
  id: number;
  lat: number;
  lng: number;
  status: VehicleStatus;
  label?: string;
}

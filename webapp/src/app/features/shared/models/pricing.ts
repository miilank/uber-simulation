export interface PricingConfig {
  id: number;
  vehicleType: VehicleType;
  basePrice: number;
  pricePerKm: number;
  lastUpdated: string;
  updatedBy: string;
}

export type VehicleType = 'STANDARD' | 'LUXURY' | 'VAN';

export interface PricingUpdateRequest {
  basePrice: number;
  pricePerKm: number;
}

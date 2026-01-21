export enum VehicleType {
  STANDARD = 'Standard',
  LUXURY = 'Luxury',
  VAN = 'Van'
}

export function vehicleTypeKeyFromValue(
  value: string
): keyof typeof VehicleType {
  return (Object.keys(VehicleType) as Array<keyof typeof VehicleType>)
    .find(key => VehicleType[key] === value) ?? 'STANDARD';
}

export interface Vehicle {
  id: number;
  model: string;
  type: string;
  licensePlate: string;
  seatCount: number;
  babyFriendly: boolean;
  petsFriendly: boolean;
}
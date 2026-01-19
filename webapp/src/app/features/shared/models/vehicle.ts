export enum VehicleType {
  STANDARD = 'Standard',
  LUXURY = 'Luxury',
  VAN = 'Van'
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
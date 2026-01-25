import { LocationDTO } from "./location";
import { VehicleType } from "./vehicle";
import {PassengerDTO} from './passenger';

export type RideStatus = 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Ride {
    date: string;
    time: string;
    from: string;
    to: string;
    status: RideStatus;
    cancelledBy?: 'User' | 'Driver';
    panic: boolean;
    price: string;
}

export interface RideDTO {
  id: number;
  creatorEmail: string;
  driverEmail: string | null;
  status: RideStatus;
  startLocation: LocationDTO;
  endLocation: LocationDTO;
  waypoints: LocationDTO[];
  passengers: PassengerDTO[];
  passengerEmails: string[];
  vehicleType: VehicleType;
  totalPrice: number;
  panicActivated: boolean;
  cancelledBy: string | null;
  scheduledTime: Date;
  isBabyFriendly: boolean;
  isPetsFriendly: boolean;
}

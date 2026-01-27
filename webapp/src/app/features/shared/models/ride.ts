import { LocationDTO } from "./location";
import { VehicleType } from "./vehicle";
import {PassengerDTO} from './passenger';

export type RideStatus = 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'STOPPED';

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
  basePrice: number;
  panicActivated: boolean;
  cancelledBy: string | null;
  scheduledTime: Date;
  isBabyFriendly: boolean;
  isPetsFriendly: boolean;
}

export interface RideDetailDTO {
  id: number;
  status: string;
  startAddress: string;
  endAddress: string;
  actualStartTime: string;
  actualEndTime: string;
  estimatedStartTime: string;
  estimatedEndTime: string;
  passengers: Array<{
    firstName: string;
    lastName: string;
    email: string;
  }>;
  totalPrice: number;
  cancelledBy: string | null;
  cancellationReason: string | null;
  cancellationTime: string | null;
  panicActivated: boolean;
  panicActivatedBy: string | null;
  panicActivatedAt: string | null;
  stoppedLocation: string | null;
  stoppedAt: string | null;
  inconsistencies: Array<{
    rideId: number;
    passengerId: number;
    passengerName: string;
    description: string;
    createdAt: string;
  }>;
}

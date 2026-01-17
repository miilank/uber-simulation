import { Ride } from "./ride";
import { Rating } from "./rating";
import { User } from "../../../core/models/user";
import { Vehicle } from "./vehicle";

export interface Driver extends User {
    role: 'DRIVER';
    vehicle: Vehicle;
    available: boolean;
    active: boolean;
    workedMinutesLast24h: number;
    rides: Ride[];
    ratings: Rating[];
    averageRating: number;
}
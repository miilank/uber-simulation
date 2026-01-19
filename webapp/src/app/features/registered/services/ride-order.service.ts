import { Injectable } from "@angular/core";

export interface RideCreationDTO {
    startLocation: Location,
    endLocation: Location,
    waypoints: Location[],
    babyFriendly: boolean,
    petFriendly: boolean,
    linkedPassengerEmails: string,
    scheduledTimeISO: string,
    favoriteRouteId: number
}

@Injectable({
  providedIn: 'root'
})
export class RideOrderService {

}
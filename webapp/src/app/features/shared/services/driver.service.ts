import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ConfigService } from "../../../core/services/config.service";
import { User } from "../../../core/models/user";
import { Observable } from "rxjs";
import { VehicleType } from "../models/vehicle";
import { Driver } from "../models/driver";

export interface DriverCreationDTO {
    email: string,
    firstName: string,
    lastName: string,
    address: string,
    phoneNumber: string,
    vehicle: VehicleCreationDTO
}

export interface VehicleCreationDTO {
    model: string,
    type: keyof typeof VehicleType,
    licensePlate: string,
    seatCount: number,
    babyFriendly: boolean,
    petsFriendly: boolean
}

export interface DriverUpdateDTO {
    driverId: number;
    email: string;

    newFirstName: string;
    newLastName: string;
    newPhoneNumber: string;
    newAddress: string;
    newProfilePicture: string;

    oldFirstName: string;
    oldLastName: string;
    oldPhoneNumber: string;
    oldAddress: string;
    oldProfilePicture: string;
}



@Injectable({
  providedIn: 'root'
})
export class DriverService {
    constructor(
        private http: HttpClient,
        private config: ConfigService
    ) {}

    updateDriver(updated: User) : Observable<User> {
        return this.http.put<User>(this.config.driverProfileUrl, updated);
    }

    createDriver(driver: DriverCreationDTO) : Observable<void> {
        return this.http.post<void>(this.config.driverUrl, driver);
    }

    activateDriver(token: string, password: string) : Observable<void> {
        return this.http.put<void>(this.config.driverActivateUrl, {token, password});
    }

    getPendingUpdates() : Observable<DriverUpdateDTO[]> {
        return this.http.get<DriverUpdateDTO[]>(this.config.driverPendingUpdatesUrl);
    }

    approveUpdate(id: number) : Observable<void> {
        return this.http.put<void>(`${this.config.driverUrl}/${id}/approve-update`, {});
    }

    rejectUpdate(id: number) : Observable<void> {
        return this.http.put<void>(`${this.config.driverUrl}/${id}/reject-update`, {});
    }
}
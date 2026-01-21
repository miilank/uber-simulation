import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ConfigService } from "../../../core/services/config.service";
import { User } from "../../../core/models/user";
import { Observable } from "rxjs";
import { VehicleType } from "../models/vehicle";

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
    type: 'STANDARD' | 'LUXURY' | 'VAN',
    licensePlate: string,
    seatCount: number,
    babyFriendly: boolean,
    petsFriendly: boolean
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

    activateDriver(token: string, password: string) {
        return this.http.put<void>(this.config.driverActivateUrl, {token, password});
    }
}
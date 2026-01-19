import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ConfigService } from "../../../core/services/config.service";
import { User } from "../../../core/models/user";
import { Observable } from "rxjs";

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
}
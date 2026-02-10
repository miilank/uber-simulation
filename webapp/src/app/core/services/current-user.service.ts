import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import { User } from '../models/user';
import { ConfigService } from './config.service';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { Driver } from '../../features/shared/models/driver';
import { Vehicle } from '../../features/shared/models/vehicle';

export interface DriverDTO {
    vehicle: Vehicle;
    available: boolean;
    active: boolean;
    workedMinutesLast24h: number;
}

@Injectable({
  providedIn: 'root'
})
export class CurrentUserService {
    private currentUserSubject = new BehaviorSubject<User | Driver | null>(null);
    currentUser$ = this.currentUserSubject.asObservable();
    
    constructor(
        private http: HttpClient,
        private config: ConfigService
    ) {}
    
    fetchMe(): Observable<User> {
        return this.http.get<User>(this.config.profile_url).pipe(
            switchMap(user => {
                if(user.role == 'DRIVER'){
                    return this.http.get<Driver>(this.config.driverProfileUrl).pipe(
                        map(dto => {
                            let driver: Driver = { ...dto };
                            
                            this.currentUserSubject.next(driver as Driver);
                            return driver as Driver;
                        }))

                } else {
                    this.currentUserSubject.next(user);                    
                    return of(user);
                }
            })
        )
    }

    clearCurrentUser() : void {
        this.currentUserSubject.next(null);
    };

    updateUser(updated: FormData) : Observable<User> {
        return this.http.put<User>(this.config.profile_url, updated).pipe(
            tap((user) => this.currentUserSubject.next(user))
        );
    }

    changePassword(oldPassword: string, newPassword: string): Observable<void> {
        let body = {oldPassword, newPassword }
        return this.http.put(this.config.changePasswordUrl, body).pipe(
              map(() => {
                console.log('Password change request sent.');
              }),
            );
    }

    getCurrentUserId(): number | null {
        const user = this.currentUserSubject.getValue();
        const id = user?.id;
        return id ? parseInt(id as any, 10) : null;
    }

}

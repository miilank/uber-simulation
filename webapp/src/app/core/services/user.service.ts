import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import { User } from '../models/user';
import { ConfigService } from './config.service';
import { BehaviorSubject, Observable, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
    

    private currentUserSubject = new BehaviorSubject<User | null>(null);
    currentUser$ = this.currentUserSubject.asObservable();
    
    constructor(
        private http: HttpClient,
        private config: ConfigService
    ) {
    }
    
    fetchMe(): Observable<User> {
        return this.http.get<User>(this.config.profile_url).pipe(
            tap(user => this.currentUserSubject.next(user))
        )
    }

    clearCurrentUser() : void {
        this.currentUserSubject.next(null);
    };

    updateUser(updated: User) : Observable<User> {
        return this.http.put<User>(this.config.profile_url, updated).pipe(
            tap((user) => this.currentUserSubject.next(user))
        );
    }

    changePassword(oldPassword: string, newPassword: string): Observable<void> {
        let body = {oldPassword, newPassword }
        return this.http.put(this.config.change_password_url, body).pipe(
              map(() => {
                console.log('Password change request sent.');
              }),
            );
    }

}

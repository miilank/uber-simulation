import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {map, switchMap, tap} from 'rxjs/operators';
import { User } from '../models/user';
import { ConfigService } from './config.service';
import { BehaviorSubject, Observable } from 'rxjs';

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

}

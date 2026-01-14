import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { UserService } from './user.service'
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { of } from 'rxjs/internal/observable/of';
import { Observable, throwError } from 'rxjs';
import { ConfigService } from './config.service';
import { User } from '../models/user';
import { RegisterRequestDto } from '../auth/register-request.dto';
import { register } from 'module';

export interface AuthResponse {
  token: string;
  email?: string;
  firstName?: string;
  
  role: 'PASSENGER' | 'DRIVER' | 'ADMIN';
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  constructor(
    private userService: UserService,
    private http: HttpClient,
    private config: ConfigService
  ) {
  }

  private accessToken: string | null = null;
  private tokenKey: string = "uberplus.access_token"

  login(email: string, password: string): Observable<User> {
    const body = { email, password };
    return this.http.post<AuthResponse>(this.config.loginUrl, body).pipe(
      tap(resp => {
        if (!resp || !resp.token) {
          throw new Error('Invalid login response');
        }
        this.accessToken = resp.token;
        localStorage.setItem(this.tokenKey, resp.token);
      }),
      switchMap(() => this.userService.fetchMe()),
      catchError(err => {
        console.log(err);
        localStorage.removeItem(this.tokenKey)
        return throwError(() => err["error"]);
      })
    );
  }

  private extractErrorMessage(err: any): string {
    if (err instanceof HttpErrorResponse) {
      const body = err.error;
      if (body) {
        if (typeof body === 'string') {
          return body;
        }
        if (body.message) {
          return body.message;
        }
        if (body.error) {
          return body.error;
        }
      }
      return `Server returned status ${err.status}`;
    }
    return 'Unknown error.';
  }

  public register(registerRequest: RegisterRequestDto) {
    console.log("auth serv");
    
    return this.http.post(this.config.registerUrl, registerRequest)
      .pipe(map(() => {
        console.log('Sent sign up request.');
      }));
  }

  public logout() : void {
    this.userService.clearCurrentUser();
    localStorage.removeItem(this.tokenKey);
    this.accessToken = null;
  }

  public tokenIsPresent() : boolean {
    return this.accessToken != undefined && this.accessToken != null;
  }

  public getToken() : string | null {
    if (!this.accessToken) this.accessToken = localStorage.getItem(this.tokenKey);

    return this.accessToken
  }

}

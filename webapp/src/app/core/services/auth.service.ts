import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrentUserService } from './current-user.service'
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { ConfigService } from './config.service';
import { User } from '../models/user';
import { RegisterRequestDto } from '../auth/register-request.dto';

export interface AuthResponse {
  token: string;
  email?: string;
  firstName?: string;
  
  role: 'PASSENGER' | 'DRIVER' | 'ADMIN';
}

export interface ActivationResponse {
  message: string;
}

export interface ForgotPasswordResponse {
  message: string;
  token: string;
}

export interface ResetPasswordResponse {
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  constructor(
    private userService: CurrentUserService,
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


  public register(form: FormData) {
    return this.http.post(this.config.registerUrl, form).pipe(
      map(() => {
        console.log('Sent sign up request.');
      }),
      catchError(err => {
        return throwError(() => err["error"])
      })
    );
  }

  public logout() : void {
    this.userService.clearCurrentUser();
    localStorage.removeItem(this.tokenKey);
    this.accessToken = null;
  }

  public tokenIsPresent() : boolean {    
    return !!this.getToken();
  }

  public getToken() : string | null {
    if (!this.accessToken) this.accessToken = localStorage.getItem(this.tokenKey);

    return this.accessToken
  }
  
  public activateAccount(token: string): Observable<ActivationResponse> {
    return this.http.get<ActivationResponse>(`${this.config.activateUrl}?token=${token}`);
  }

  public forgotPassword(email: string): Observable<ForgotPasswordResponse> {
    return this.http.post<ForgotPasswordResponse>(`${this.config.forgotPassUrl}`, { email });
  }

  public resetPassword(token: string, newPassword: string, confirmPassword: string): Observable<ResetPasswordResponse> {
    return this.http.post<ResetPasswordResponse>(`${this.config.resetPassUrl}`, { 
      token, 
      newPassword,
      confirmPassword
    });
  }
}

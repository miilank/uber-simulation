import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
    public baseUrl = 'http://localhost:8080/api';

    public loginUrl = this.baseUrl + '/auth/login';
    public registerUrl = this.baseUrl + '/auth/register';
    public profile_url = this.baseUrl + '/users/profile';
    public activate_url = this.baseUrl + '/auth/activate'
    public forgot_pass_url = this.baseUrl + '/auth/forgot-password'
    public reset_pass_url = this.baseUrl + '/auth/reset-password'

}
import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
    public baseUrl = 'http://localhost:8080/api';

    public loginUrl = this.baseUrl + '/auth/login';
    public registerUrl = this.baseUrl + '/auth/register';
    public profile_url = this.baseUrl + '/users/profile';
    public change_password_url = this.baseUrl + '/users/change-password'

    public driver_profile_url = this.baseUrl + '/drivers/profile'
}
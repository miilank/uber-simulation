import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
    public baseUrl = 'http://localhost:8080/api';

    public loginUrl = this.baseUrl + '/auth/login';
    public registerUrl = this.baseUrl + '/auth/register';
    public profile_url = this.baseUrl + '/users/profile';
    public changePasswordUrl = this.baseUrl + '/users/change-password'

    public driverProfileUrl = this.baseUrl + '/drivers/profile'
    public activateUrl = this.baseUrl + '/auth/activate'
    public forgotPassUrl = this.baseUrl + '/auth/forgot-password'
    public resetPassUrl = this.baseUrl + '/auth/reset-password'
    
    public ridesUrl = this.baseUrl + '/rides'; 

}
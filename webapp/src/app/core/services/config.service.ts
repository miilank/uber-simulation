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

  public driverUrl = this.baseUrl + '/drivers';
  public driverProfileUrl = this.driverUrl + '/profile';
  public driverActivateUrl = this.driverUrl + '/activate';
  public driverPendingUpdatesUrl = this.driverUrl + '/pending-updates';

  public activateUrl = this.baseUrl + '/auth/activate'
  public forgotPassUrl = this.baseUrl + '/auth/forgot-password'
  public resetPassUrl = this.baseUrl + '/auth/reset-password'

  public ridesUrl = this.baseUrl + '/rides';
  public priceEstimateUrl = this.ridesUrl + '/estimate';
  public getPanicsUrl = this.baseUrl + '/admin/panic-notifications';

  public favouriteRoutesUrl = this.baseUrl + '/favorite-routes'

  public historyReportUrl = this.ridesUrl + '/history-report';

  searchUsersUrl(searchString:string, pageSize:number, pageNumber:number) : string {
    return `${this.baseUrl}/users?search=${searchString}&pageSize=${pageSize}&pageNumber=${pageNumber}`;
  }

  startRideUrl(rideId: number): string {
      return this.ridesUrl + '/' + rideId.toString() + '/start';
  }
  completeRideUrl(rideId: number): string {
    return `${this.baseUrl}/rides/${rideId}/complete`;
  }

  readonly chatUrl = `${this.baseUrl}/chat`;
}

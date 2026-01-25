import { Routes } from '@angular/router';
import { SignInComponent } from './core/auth/signin';
import { RegisteredProfileComponent } from './features/registered/pages/profile';
import { UserRegistrationComponent } from './core/auth/userRegistration';
import { DriverLayout } from './features/driver/layout/driver-layout/driver-layout';
import { DriverRideHistory } from './features/driver/pages/ride-history/driver-ride-history/driver-ride-history';
import { RegisteredLayout } from './features/registered/registered-layout/registered-layout';
import { DriverProfileComponent } from './features/driver/pages/profile/profile';
import { MapComponent } from './features/shared/map/map';
import {UnregisteredHomeComponent} from './features/unregistered/pages/home/home';
import {DriverDashboard} from './features/driver/pages/dashboard/driver-dashboard/driver-dashboard';
import { RegisteredDashboard } from './features/registered/pages/dashboard/registered-dashboard/registered-dashboard';
import { AccountActivationComponent } from './core/auth/account-activation';
import { RideBooking } from './features/registered/pages/ride-booking/ride-booking';
import { RideBookingSidebar } from './features/registered/components/ride-booking-sidebar/ride-booking-sidebar';
import { RegisteredSidebar } from './features/registered/components/registered-sidebar';
import {CurrentRideComponent} from './features/registered/pages/current-ride/current-ride';
import { DriverRegistration } from './features/admin/pages/driver-registration/driver-registration';
import { AdminLayout } from './features/admin/layout/admin-layout/admin-layout';
import { DriverActivationComponent } from './core/auth/driver-activation';
import { PanicResponseComponent } from './features/admin/pages/panic-response/panic-response';
import { FavouriteRoutes } from './features/registered/pages/favourite-rides/favourite-routes';

export const routes: Routes = [
  { path: '', component: UnregisteredHomeComponent },
  { path: 'registerUser', component: UserRegistrationComponent },
  { path: 'signIn', component: SignInComponent },
  { path: 'activate', component: AccountActivationComponent},
  {path: 'activate-driver', component: DriverActivationComponent},
  { path: 'map', component: MapComponent },
  { path: 'user', component: RegisteredLayout, children: [
      { path: 'profile', component: RegisteredProfileComponent },
      { path: 'dashboard', component: RegisteredDashboard },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'booking', component: RideBooking },
      { path: 'current-ride', component: CurrentRideComponent },
      { path: 'booking', outlet: 'aside', component: RideBookingSidebar },
      { path: '', outlet: 'aside', component: RegisteredSidebar },
      { path: 'favorite-routes', component: FavouriteRoutes }
  ] },
  { path: 'registerUser', component: UserRegistrationComponent },
  { path: 'signIn', component: SignInComponent },
  { path: 'driver', component: DriverLayout, children: [
      { path: 'profile', component: DriverProfileComponent },
      { path: 'ride-history', component: DriverRideHistory },
      { path: 'dashboard', component: DriverDashboard },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ] },

  { path: 'admin', component: AdminLayout, children: [
    { path: 'profile', component: RegisteredProfileComponent },
    { path: 'register-driver', component: DriverRegistration },
    { path: 'dashboard', component: RegisteredDashboard },
    { path: 'panic-notifications', component: PanicResponseComponent },
    { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  ] }
];

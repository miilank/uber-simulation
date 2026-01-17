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

export const routes: Routes = [
  { path: '', component: UnregisteredHomeComponent },
  { path: 'registerUser', component: UserRegistrationComponent },
  { path: 'signIn', component: SignInComponent },
  { path: 'map', component: MapComponent },
  { path: 'user', component: RegisteredLayout, children: [
    { path: 'profile', component: RegisteredProfileComponent },
    { path: '', pathMatch: 'full', redirectTo: 'profile' },
  ] },
  { path: 'registerUser', component: UserRegistrationComponent },
  { path: 'signIn', component: SignInComponent },
  { path: 'driver', component: DriverLayout, children: [
      { path: 'profile', component: DriverProfileComponent },
      { path: 'ride-history', component: DriverRideHistory },
      { path: 'dashboard', component: DriverDashboard },
    ] }
];

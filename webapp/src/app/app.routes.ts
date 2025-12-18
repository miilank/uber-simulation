import { Routes } from '@angular/router';
import { SignInComponent } from './core/auth/signin';
import { ProfileComponent } from './features/registered/pages/profile';
import { UserRegistrationComponent } from './core/auth/userRegistration';
import { DriverLayout } from './features/driver/layout/driver-layout/driver-layout';
import { DriverRideHistory } from './features/driver/pages/ride-history/driver-ride-history/driver-ride-history';
import { RegisteredLayout } from './features/registered/registered-layout/registered-layout';

export const routes: Routes = [
  { path: '', redirectTo: 'signIn', pathMatch: 'full' },
  { path: 'user', component: RegisteredLayout, children: [
    { path: 'profile', component: ProfileComponent },
    { path: '', pathMatch: 'full', redirectTo: 'profile' },
  ] },
  { path: 'registerUser', component: UserRegistrationComponent },
  { path: 'signIn', component: SignInComponent },
  { path: 'driver', component: DriverLayout, children: [
      { path: 'ride-history', component: DriverRideHistory },
      { path: '', pathMatch: 'full', redirectTo: 'ride-history' },
    ] }
];

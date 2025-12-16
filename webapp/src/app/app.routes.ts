import { Routes } from '@angular/router';
import { ProfileComponent } from './pages/profile';
import { UserRegistrationComponent } from './core/auth/userRegistration';
import { SignInComponent } from './core/auth/signin';

export const routes: Routes = [
  { path: '', redirectTo: 'signIn', pathMatch: 'full' },
  { path: 'profile', component: ProfileComponent },
  { path: 'registerUser', component: UserRegistrationComponent },
  { path: 'signIn', component: SignInComponent }
];

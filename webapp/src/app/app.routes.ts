import { Routes } from '@angular/router';
import { ProfileComponent } from './pages/profile';
import { UserRegistrationComponent } from './core/auth/registration';

export const routes: Routes = [
  { path: '', redirectTo: 'registerUser', pathMatch: 'full' },
  { path: 'profile', component: ProfileComponent },
  { path: 'registerUser', component: UserRegistrationComponent }
];

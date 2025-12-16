import { Routes } from '@angular/router';
import { ProfileComponent } from './features/registered/pages/profile';

export const routes: Routes = [
  { path: '', component: ProfileComponent },
  { path: 'profile', component: ProfileComponent },
];

import { Component } from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import { Router, RouterLink } from '@angular/router';

type Item = { label: string; route: string; icon: string };

@Component({
  selector: 'app-driver-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, NgOptimizedImage],
  templateUrl: './driver-sidebar.html',
})
export class DriverSidebar {
  constructor(private router: Router) {}

  items: Item[] = [
    { label: 'Dashboard', route: '/driver/dashboard', icon: 'dashboard' },
    { label: 'Ride history', route: '/driver/ride-history', icon: 'history' },
    { label: 'Booked rides', route: '/driver/booked-rides', icon: 'bookedrides' },
    { label: 'Reports', route: '/driver/reports', icon: 'reports' },
    { label: 'Profile', route: '/driver/profile', icon: 'user' },
    { label: 'Support', route: '/driver/support', icon: 'support' },
  ];

  isActive(route: string) {
    return this.router.url === route;
  }

  iconSrc(icon: string, route: string) {
    const variant = this.isActive(route) ? 'black' : 'white';
    return `/icons/${icon}-${variant}.png`;
  }
}

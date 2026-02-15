import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink, UrlTree } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
type Item = { label: string; route: string | UrlTree; icon: string };

@Component({
  selector: 'app-admin-sidebar',
  imports: [CommonModule, RouterLink, NgOptimizedImage],
  templateUrl: './admin-sidebar.html',
})
export class AdminSidebar {
  router = inject(Router)
  authService = inject(AuthService)

  items: Item[] = [];

  ngOnInit() {
    this.items = [
      { label: 'Dashboard', route: '/admin/dashboard', icon: 'dashboard' },
      { label: 'Ride tracking', route: '/admin/ride-tracking', icon: 'ridetracking' },
      { label: 'Ride history', route: '/admin/ride-history', icon: 'history' },
      { label: 'Pricing', route: '/admin/ride-pricing', icon: 'pricing' },
      { label: 'Register a driver', route: '/admin/register-driver', icon: 'car' },
      { label: 'Review profile changes', route: '/admin/driver-profile-changes', icon: 'pencil' },
      { label: 'Reports', route: '/admin/reports', icon: 'reports' },
      { label: 'Block users', route: '/admin/block-users', icon: 'blocked' },
      { label: 'Profile', route: '/admin/profile', icon: 'user' },
      { label: 'PANIC notifications', route: '/admin/panic-notifications', icon: 'panic' },
      { label: 'Support', route: '/admin/support', icon: 'support' },
    ];
  }

  isActive(route: string | UrlTree) {
    const currentUrl = this.router.url;

    if (route instanceof UrlTree) {
      return this.router.serializeUrl(route) === currentUrl;
    }

    return route === currentUrl;
  }

  iconSrc(icon: string, route: string | UrlTree) {
    const variant = this.isActive(route) ? 'black' : 'white';
    return `/icons/${icon}-${variant}.png`;
  }

  signOut() : void {
    this.authService.logout();
    this.router.navigateByUrl('/').catch(console.error);
  }
}

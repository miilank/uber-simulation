import { Component, inject } from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import { Router, RouterLink, UrlTree } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

type Item = { label: string; route: string | UrlTree; icon: string };

@Component({
  selector: 'app-registered-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, NgOptimizedImage],
  template: `
    <nav class="mt-24 flex flex-col gap-3 font-poppins px-10">
    @for (it of items; track it.route) {
        <a
        [routerLink]="it.route"
        class="flex items-center gap-4 h-12 px-10 rounded-full cursor-pointer select-none transition-all duration-250"
        [ngClass]="isActive(it.route)
        ? 'bg-app-accent text-app-dark hover:brightness-95'
        : 'text-white hover:bg-white/5'">
        <img
            [ngSrc]="iconSrc(it.icon, it.route)"
            [class]="it.icon === 'support' ? 'w-6 h-6 -ml-1' : 'w-5 h-5'"
            [width]="it.icon === 'support' ? 24 : 20"
            [height]="it.icon === 'support' ? 24 : 20"
            alt=""/>
        <span class="text-base">{{ it.label }}</span>
        </a>
    }

    <button
        type="button"
        class="flex items-center gap-4 h-12 px-10 rounded-full text-white text-left hover:bg-white/5 cursor-pointer select-none transition-all duration-250"
        (click)="signOut()">
        <img ngSrc="/icons/signout-white.png" class="w-5 h-5" width="20" height="20" alt=""/>
        <span class="text-base">Sign out</span>
    </button>
    </nav>
    `,
})
export class RegisteredSidebar {
  router = inject(Router);
  authService = inject(AuthService);

  items: Item[] = [];

  ngOnInit() {
    const bookingUrl = this.router.createUrlTree(
      ['/user', { outlets: { primary: ['booking'], aside: ['booking'] } }]
    );

    this.items = [
      { label: 'Dashboard', route: '/user/dashboard', icon: 'dashboard' },
      { label: 'Book a ride', route: bookingUrl, icon: 'car' },
      { label: 'Current ride', route: '/user/current-ride', icon: 'current_ride' },
      { label: 'Ride history', route: '/user/ride-history', icon: 'history' },
      { label: 'Booked rides', route: '/user/booked-rides', icon: 'bookedrides' },
      { label: 'Favorite routes', route: '/user/favorite-routes', icon: 'routes'},
      { label: 'Reports', route: '/user/reports', icon: 'reports' },
      { label: 'Profile', route: '/user/profile', icon: 'user' },
      { label: 'Support', route: '/user/support', icon: 'support' },
    ];
  }

  isActive(route: string | UrlTree) : boolean {
    const currentUrl = this.router.url;

    if (route instanceof UrlTree) {
      return this.router.serializeUrl(route) === currentUrl;
    }

    return route === currentUrl;
  }

  iconSrc(icon: string, route: string | UrlTree) : string {
    const variant = this.isActive(route) ? 'black' : 'white';
    return `/icons/${icon}-${variant}.png`;
  }

  signOut() : void {
    this.authService.logout();
    this.router.navigateByUrl('/').catch(console.error);
  }
}

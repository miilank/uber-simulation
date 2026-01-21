import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, UrlTree } from '@angular/router';
type Item = { label: string; route: string | UrlTree; icon: string };

@Component({
  selector: 'app-admin-sidebar',
  imports: [CommonModule, RouterLink, NgOptimizedImage],
  templateUrl: './admin-sidebar.html',
})
export class AdminSidebar {
  constructor(private router: Router) {}
  items: Item[] = [];

  ngOnInit() {
    this.items = [
      { label: 'Dashboard', route: '/admin/dashboard', icon: 'dashboard' },
      { label: 'Register a driver', route: '/admin/register-driver', icon: 'car' },
      { label: 'Profile', route: '/admin/profile', icon: 'user' },
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
}

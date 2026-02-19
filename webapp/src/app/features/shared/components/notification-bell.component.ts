import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { AppNotification } from '../models/notification';
import { CurrentUserService } from '../../../core/services/current-user.service';
import { User } from '../../../core/models/user';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="relative">
      <!-- Bell Icon -->
      <button
        (click)="toggleDropdown()"
        class="relative p-2 hover:bg-white/10 rounded-full transition-colors">
        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
        </svg>

        <!-- Badge -->
        <span
          *ngIf="(unreadCount$ | async)! > 0"
          class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center font-semibold">
          {{ unreadCount$ | async }}
        </span>
      </button>

      <!-- Dropdown -->
      <div
        *ngIf="showDropdown"
        class="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-xl border border-gray-200 z-[100000]">

        <div class="p-4 border-b border-gray-200">
          <h3 class="font-semibold text-gray-800">Notifications</h3>
        </div>

        <div class="max-h-96 overflow-y-auto">
          <div *ngFor="let notification of (notifications$ | async)"
               (click)="handleNotificationClick(notification)"
               class="p-4 border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition-colors"
               [ngClass]="{'bg-blue-50': !notification.read}">

            <div class="flex items-start gap-3">
              <!-- Icon -->
              <div class="flex-shrink-0 mt-1">
                <svg *ngIf="notification.type === 'RIDE_ACCEPTED'"
                     class="w-5 h-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"/>
                  <path fill-rule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm9.707 5.707a1 1 0 00-1.414-1.414L9 12.586l-1.293-1.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                </svg>

                <svg *ngIf="notification.type === 'RIDE_COMPLETED'"
                     class="w-5 h-5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                </svg>
              </div>

              <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-800 break-words">{{ notification.message }}</p>
                <span class="text-xs text-gray-500 mt-1 block">{{ formatTime(notification.createdAt) }}</span>
              </div>
            </div>
          </div>

          <div *ngIf="(notifications$ | async)?.length === 0"
               class="p-8 text-center text-gray-400">
            <svg class="w-12 h-12 mx-auto mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
            <p>No notifications</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Click outside overlay -->
    <div *ngIf="showDropdown"
         (click)="showDropdown = false"
         class="fixed inset-0 z-[90]">
    </div>
  `
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  private notificationService = inject(NotificationService);
  private websocketService = inject(WebSocketService);
  private router = inject(Router);
  private userService = inject(CurrentUserService);

  showDropdown = false;
  notifications$ = this.notificationService.notifications$;
  unreadCount$ = this.notificationService.unreadCount$;

  currentUser: User | null = null;

  private wsSubscription?: Subscription;

  ngOnInit(): void {
    this.notificationService.loadNotifications();

    // slusaj nove notifikacije preko WebSocketa
    this.wsSubscription = this.websocketService.notifications$.subscribe({
      next: (notification) => {
        if (notification) {
          this.notificationService.addNotification(notification);
        }
      }
    });

    this.userService.currentUser$.subscribe({
      next: (user) => this.currentUser = user
    })
  }

  ngOnDestroy(): void {
    this.wsSubscription?.unsubscribe();
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }

  handleNotificationClick(notification: AppNotification): void {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id);
    }

    // Navigacija na osnovu tipa notifikacije
    if (notification.rideId) {
      if(this.currentUser != null && this.currentUser.role == "PASSENGER") {
          this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
            this.router.navigate(['/user/current-ride'], {
              queryParams: { t: Date.now() } // timestamp da forsira reload
            });
          });
        }
      else if (this.currentUser != null && this.currentUser.role == "DRIVER") {
          this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
            this.router.navigate(['/driver/booked-rides'], {
              queryParams: { t: Date.now() } // timestamp da forsira reload
            });
          });
      }
    }



    this.showDropdown = false;
  }

  formatTime(timestamp: string): string {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (minutes < 1440) return `${Math.floor(minutes / 60)}h ago`;
    return date.toLocaleDateString();
  }
}

import { ChangeDetectorRef, Component, Input, signal } from '@angular/core';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import { Subscription } from 'rxjs';
import { CurrentUserService } from '../../../core/services/current-user.service';
import { User } from '../../../core/models/user';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, NgOptimizedImage],
  template: `
    <header class="w-full bg-app-dark">
      <div class="flex items-center justify-between h-23.5 px-6 md:px-8">
        <!-- Logo Section -->
        <div class="flex items-center gap-2.5 cursor-pointer"
             (click)="onLogoClick()">
          <img
            ngSrc="https://api.builder.io/api/v1/image/assets/TEMP/880510fa722bf78df9fb6fda1ee63b8ba1554443?width=112"
            width="56"
            height="56"
            alt="UberPLUS"
            class="w-14 h-14 object-contain"
          />
          <div class="text-2xl md:text-4xl font-semibold">
            <span class="font-light text-white">Uber</span><span class="font-semibold text-app-accent">PLUS</span>
          </div>
        </div>

        <!-- User Profile Button -->
        @if (showUserProfile) {
          <div class="flex items-center gap-2.5 bg-white rounded-full px-3 py-1.5 md:px-2 md:py-2 shadow-lg">
            <div class="w-4 h-4 rounded-full bg-green-500 flex items-center justify-center shrink-0 ml-2"></div>
            <span class="text-black text-base md:text-[18px] font-normal mr-1">{{user.firstName}} {{user.lastName}}</span>
            <img
              [ngSrc]="displayedProfilePicture()"
              width="40"
              height="40"
              alt="Profile"
              class="w-8 h-8 md:w-10 md:h-10 rounded-full object-cover"
              (error)="onProfileImgError($event)"
            />
          </div>
        }
        @else {
          <div class="flex items-center gap-2 md:gap-3">
            <button class="px-8 md:px-6 py-8 md:py-2.5 rounded-full border-2 md:border-3 bg-app-dark border-app-accent text-app-accent text-sm md:text-base font-normal hover:bg-app-accent hover:text-app-dark transition-colors" routerLink="/signIn">
              Sign In
            </button>
            <button class="px-8 md:px-6 py-8 md:py-2.5 rounded-full border-2 md:border-3 bg-app-accent border-app-dark text-app-dark text-sm md:text-base font-normal hover:bg-app-accent-dark hover:text-white transition-colors" routerLink="/registerUser">
              Register
            </button>
          </div>
        }
      </div>
    </header>
  `,
  styles: []
})
export class HeaderComponent {
  user: User = {
    id: 0,
    firstName: '',
    lastName: '',
    email: '',
    address: '',
    phoneNumber: '',
    profilePicture: 'defaultprofile.png',
    role: 'PASSENGER',
    blocked: false,
    blockReason: ''
  }

  displayedProfilePicture = signal<string>('/defaultprofile.png');

  @Input() showUserProfile: boolean = true;

  private sub?: Subscription;
  isChangePasswordOpen: boolean = false;

  ngOnInit(): void {
    this.sub = this.userService.currentUser$.subscribe(current => {
      if (current) {
        this.displayedProfilePicture.set(current.profilePicture
          ? `${current.profilePicture}?cb=${Date.now()}`
          : '/defaultprofile.png');

        this.user = {...current}
        this.cdr.detectChanges();
      }
    });
    this.userService.fetchMe().subscribe();
  }

  constructor(private router: Router, private userService: CurrentUserService, private cdr: ChangeDetectorRef) {}

  onLogoClick(): void {
    switch(this.user.role) {
      case "ADMIN":
        this.router.navigate(["/admin/dashboard"]); break;
      case "DRIVER":
        this.router.navigate(["/driver/dashboard"]); break;
      case "PASSENGER":
        // this.router.navigate(["/user/dashboard"]); break;
        this.router.navigateByUrl("/user/dashboard"); break;
      default:
        this.router.navigateByUrl("/");
    }
  }

  onProfileImgError(ev: Event) {
    const img = ev.target as HTMLImageElement;
    img.src = '/defaultprofile.png';
  }
}

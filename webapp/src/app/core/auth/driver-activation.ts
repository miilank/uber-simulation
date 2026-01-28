import { ChangeDetectorRef, Component, inject, NgZone, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { DriverService } from '../../features/shared/services/driver.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-account-activation',
  standalone: true,
  imports: [ CommonModule, RouterModule, FormsModule ],
  template: `
  <div class="min-h-screen bg-gray-50 flex items-center justify-center px-4">
  <div class="max-w-md w-full">
    <div class="bg-white rounded-lg shadow-lg p-8 text-center">

    @if (password_input()) {
        <form  class="mt-4 space-y-4" (ngSubmit)="activateAccount()" novalidate>
          <div class="flex flex-col gap-2">
            <label class="text-sm text-gray-700">Set Password</label>
            <input
              type="password"
              name="password"
              [(ngModel)]="password"
              #passwordCtrl = "ngModel"
              required
              minlength="8"
              class="input-base"
              placeholder="Enter new password (min 8 chars)"
            />
            @if (passwordCtrl.invalid && (passwordCtrl.dirty || passwordCtrl.touched)) {
                <p class="text-red-400 text-xs">
                @if (passwordCtrl.errors?.['required']) { <span>Password is required.</span> }
                @if (passwordCtrl.errors?.['minlength']) { <span>Password must be at least 8 characters.</span> }
                </p>
            }
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-sm text-gray-700">Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              [(ngModel)]="confirmPassword"
              #confirmCtrl = "ngModel"
              required
              class="input-base"
              placeholder="Confirm new password"
            />

            @if ((password() !== confirmPassword()) && (confirmCtrl.dirty || confirmCtrl.touched)) {
                <p class="text-red-400 text-xs">
                    <span>Passwords must match.</span> 
                </p>
            }

            @if (confirmCtrl.invalid && (confirmCtrl.dirty || confirmCtrl.touched)) {
                <p class="text-red-400 text-xs">
                @if (passwordCtrl.errors?.['required']) { <span>Confirm your password.</span> }
                </p>
            }
          </div>
          <div class="flex gap-3 justify-end pt-2">
            <button
              type="submit"
              class="h-10 px-4 rounded-full bg-app-accent text-sm text-neutral-900 hover:bg-lime-500 cursor-pointer">
              Confirm
            </button>
          </div>
        </form>
    }
      
      <!-- Loading State -->
      @if (loading()) {
        <div class="space-y-4">
          <div class="flex justify-center">
            <svg class="w-16 h-16 text-gray-300 animate-spin fill-green-600" viewBox="0 0 100 101" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z" fill="currentColor"/>
              <path d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z" fill="currentFill"/>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-800">Activating Your Account</h2>
          <p class="text-gray-600">Please wait while we verify your email...</p>
        </div>
      }

      <!-- Success State -->
      @if (success()) {
        <div class="space-y-4">
          <div class="flex justify-center">
            <svg class="w-16 h-16 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-800">Account Activated!</h2>
          <p class="text-gray-600">Your account has been successfully activated.</p>
          <p class="text-sm text-gray-500">Redirecting you to login...</p>
        </div>
      }

      <!-- Error State -->
      @if (error()) {
        <div class="space-y-4">
          <div class="flex justify-center">
            <svg class="w-16 h-16 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-800">Activation Failed</h2>
          <div class="bg-red-50 border border-red-200 rounded-lg p-4">
            <p class="text-red-800">{{ errorMessage }}</p>
          </div>
          <a routerLink="/signIn" class="inline-block mt-4 px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition">
            Go to Login
          </a>
        </div>
      }

    </div>
  </div>
</div>
  `
})
export class DriverActivationComponent{
  password_input = signal<boolean>(true);
  loading = signal<boolean>(false);
  success = signal<boolean>(false);
  error = signal<boolean>(false);
  errorMessage = '';

  password = signal<string>('');
  confirmPassword = signal<string>('');
  
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private driverService = inject(DriverService);


  activateAccount() {
    const token = this.route.snapshot.queryParams['token'];

    this.driverService.activateDriver(token, this.password()).subscribe({
      next: (response) => {
          this.loading.set(false);
          this.success.set(true);
        //   this.cdr.detectChanges();
          setTimeout(() => this.router.navigate(['/signIn']), 2000);
      },
      error: (err) => {
          this.loading.set(false);
          this.error.set(true);
          this.errorMessage = err.error?.message || 'Activation failed';
        //   this.cdr.detectChanges();   
       }
    });
  }
}

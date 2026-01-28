import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../features/shared/components/header.component';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ResetPasswordComponent } from './reset-password';

@Component({
  selector: 'app-signin',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, ResetPasswordComponent],
  template: `
<div class="min-h-screen flex flex-col bg-linear-to-b from-[#d6f4a2] to-[#f7f7f7] font-poppins">
  <!-- Header -->
  <app-header [showUserProfile]="false"></app-header>

  <!-- Main Content -->
  <main class="flex-1 flex items-center justify-center px-4 md:px-6 py-8 md:py-12">
    <!-- Login Form Container -->
    <div class="w-full max-w-sm md:max-w-md bg-app-dark rounded-[44px] shadow-xl px-4 md:px-6 lg:px-8 py-8 md:py-10">

      <!-- Sign In Title -->
      <h1 class="text-white text-center text-3xl md:text-4xl lg:text-5xl font-semibold mb-6 md:mb-8">
        Sign In
      </h1>

      <!-- Form -->
      <form #signInForm="ngForm" (ngSubmit)="signInForm.valid && onSubmit()">
        <div class="space-y-5 mb-5">
          <!-- Email Address -->
          <div class="flex flex-col gap-2">
            <label class="text-white font-normal font-poppins">Email Address</label>
            <input
              type="email"
              name="email"
              [(ngModel)]="email"
              #emailCtrl="ngModel"
              required
              email
              placeholder="Enter your email"
              class="input-base w-full"
            />

            @if (emailCtrl.invalid && (emailCtrl.dirty || emailCtrl.touched)) {
              <p class="text-red-400 text-xs">
                @if (emailCtrl.errors?.['required']) {
                  <span>Email is required.</span>
                }
                @if (emailCtrl.errors?.['email']) {
                  <span>Please enter a valid email address.</span>
                }
              </p>
            }
          </div>

          <!-- Password -->
          <div class="flex flex-col gap-2">
            <label class="text-white font-normal font-poppins">Password</label>
            <input
              type="password"
              name="password"
              [(ngModel)]="password"
              #passwordCtrl="ngModel"
              required
              minlength="6"
              placeholder="Enter your password"
              class="input-base w-full"
            />

            @if (passwordCtrl.invalid && (passwordCtrl.dirty || passwordCtrl.touched)) {
              <p class="text-red-400 text-xs">
                @if (passwordCtrl.errors?.['required']) {
                  <span>Password is required.</span>
                }
                @if (passwordCtrl.errors?.['minlength']) {
                  <span>Password must be at least 6 characters.</span>
                }
              </p>
            }
          
            <!-- Ne ponasa se dobro. -->
              <div *ngIf="errorMessage" class="text-red-400 text-xs">
                  {{ errorMessage }}
              </div>

          </div>
        </div>

        <div class="flex justify-center mb-6">
          <button
            type="button"
            class="text-white text-xs md:text-sm font-normal hover:underline"
            (click)="openForgot()"
          >
            Forgot password?
          </button>

        </div>  

        <!-- Sign In Button -->
        <button
          type="submit"
          [disabled]="signInForm.invalid"
          class="w-full bg-app-accent border border-app-dark text-app-dark text-sm font-normal py-2.5 rounded-full hover:bg-app-accent-dark disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          Sign in
        </button>
      </form>
    </div>
    @if (isForgotOpen) {
  <div class="fixed inset-0 z-50 flex items-center justify-center">
    <!-- Backdrop -->
    <div
      class="absolute inset-0 bg-black/50"
      (click)="closeForgot()"
    ></div>

    <!-- Modal -->
    <div class="relative z-10 w-full max-w-sm bg-white rounded-2xl shadow-xl p-6">
      
      @if (isForgotOpen && forgotStep === "email") {
        <h2 class="text-lg font-semibold mb-2 text-gray-900">
          Reset password
        </h2>
        <p class="text-sm text-gray-600 mb-4">
          Enter your email and a reset link will be sent to you.
        </p>

        <form #emailForm="ngForm" (ngSubmit)="emailForm.valid && sendResetEmail()">
          <label class="block text-sm font-medium text-gray-700 mb-1">
            Email
          </label>
          <input
            type="email"
            [(ngModel)]="emailForReset"
            name="resetEmail"
            #resetEmailCtrl="ngModel"
            required
            email
            class="input-base w-full mb-4"
            placeholder="you@example.com"
          />
          @if (resetEmailCtrl.invalid && (resetEmailCtrl.dirty || resetEmailCtrl.touched)) {
            <p class="text-red-400 text-xs mb-2">
              @if (resetEmailCtrl.errors?.['required']) {
                <span>Email is required.</span>
              }
              @if (resetEmailCtrl.errors?.['email']) {
                <span>Enter a valid email address.</span>
              }
            </p>
          }
          @if (resetError) {
            <div class="bg-red-50 border border-red-200 rounded-lg p-3 mb-3">
              <p class="text-red-800 text-sm">{{ resetError }}</p>
            </div>
          }
          <div class="flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 rounded-full text-sm border border-gray-300 text-gray-700 hover:bg-gray-100"
              (click)="closeForgot()"
              [disabled]="sendingEmail"
            >
              Cancel
            </button>
            <button
              type="submit"
              [disabled]="emailForm.invalid || sendingEmail"
              class="px-4 py-2 rounded-full text-sm bg-app-accent text-app-dark hover:bg-app-accent-dark disabled:opacity-50 disabled:cursor-not-allowed"
            >
              @if (sendingEmail) {
                <span>Sending...</span>
              } @else {
                <span>Send email</span>
              }
            </button>
          </div>
        </form>
      }
      @if (resetEmailSent) {
        <div class="text-center space-y-4">
          <div class="flex justify-center">
            <svg class="w-16 h-16 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
            </svg>
          </div>
          <h2 class="text-lg font-semibold text-gray-900">Email Sent!</h2>
          <p class="text-sm text-gray-600">
            Check your inbox at <span class="font-medium">{{ emailForReset }}</span> for the password reset link.
          </p>
        </div>
      }

      @if (isForgotOpen && forgotStep === 'password') {
      <app-reset-password 
        [token]="resetToken" 
        (closeModal)="closeForgot()">
      </app-reset-password>
    }
    </div>
  </div>
}
  </main>
</div>
`})
export class SignInComponent implements OnInit{
  email = '';
  password = '';

  isForgotOpen = false;
  forgotStep: 'email' | 'password' = 'email';

  emailForReset = '';
  resetToken = '';
  newPassword = '';
  confirmPassword = '';

  errorMessage: string | null = null;
  resetError: string | null = null;
  resetEmailSent = false;
  sendingEmail = false;

  private router = inject(Router);
  private route = inject(ActivatedRoute);

  constructor(public authService : AuthService, private cdr: ChangeDetectorRef) {
  }
  
  ngOnInit() {
    const tokenFromUrl = this.route.snapshot.queryParams['token'];
    
    if (tokenFromUrl) {
      this.resetToken = tokenFromUrl;
      this.isForgotOpen = true;
      this.forgotStep = 'password';
      this.cdr.detectChanges();
    }
  }

  onSubmit() {
    this.authService.login(this.email, this.password).subscribe({
      next: user => {
        switch (user.role) {
          case 'ADMIN':
            this.router.navigate(['/admin/profile']);
            break;
          case 'DRIVER':
            this.router.navigate(['/driver/dashboard']);
            break;
          default:
            this.router.navigate(['/user/dashboard']);
        }
      },
      error: err => {
        this.errorMessage = err["message"];
        this.cdr.markForCheck();
      }
    });
  }

  openForgot() {
    this.isForgotOpen = true;
    this.forgotStep = 'email';
    this.emailForReset = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.resetError = null;
    this.resetEmailSent = false;
  }

  closeForgot() {
    this.isForgotOpen = false;
    this.router.navigate([], {
      queryParams: { token: null },
      queryParamsHandling: 'merge'
    });
  }

  sendResetEmail() {
    this.resetError = null;
    this.resetEmailSent = false;
    this.sendingEmail = true;
    
    this.authService.forgotPassword(this.emailForReset).subscribe({
      next: (response) => {
        this.sendingEmail = false;
        this.resetEmailSent = true;
        this.resetToken = response.token;
        this.cdr.detectChanges();
        
        setTimeout(() => {
          this.closeForgot();
        }, 3000);
      },
      error: (err) => {
        this.sendingEmail = false;
        this.resetError = err.error?.message || 'Failed to send reset email';
        this.cdr.detectChanges();
      }
    });
  }
}


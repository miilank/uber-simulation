import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../features/shared/components/header.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-signin',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent],
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
          @if (forgotStep === 'email') {
            <!-- Step 1: email -->
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
                <p class="text-red-400 text-xs">
                  @if (resetEmailCtrl.errors?.['required']) {
                    <span>Email is required.</span>
                  }
                  @if (resetEmailCtrl.errors?.['email']) {
                    <span>Enter a valid email address.</span>
                  }
                </p>
              }
              <div class="flex justify-end gap-2">
                <button
                  type="button"
                  class="px-4 py-2 rounded-full text-sm border border-gray-300 text-gray-700 hover:bg-gray-100"
                  (click)="closeForgot()"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  [disabled]="emailForm.invalid"
                  class="px-4 py-2 rounded-full text-sm bg-app-accent text-app-dark hover:bg-app-accent-dark disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Send email
                </button>
              </div>
            </form>
          }

          @if (forgotStep === 'password') {
            <!-- Step 2: new password -->
            <h2 class="text-lg font-semibold mb-2 text-gray-900">
              Set new password
            </h2>
            <p class="text-sm text-gray-600 mb-4">
              Enter your new password and confirm it.
            </p>

            <form #newPasswordForm="ngForm" (ngSubmit)="newPasswordForm.valid && setNewPassword()">
              <label class="block text-sm font-medium text-gray-700 mb-1">
                New password
              </label>
              <input
                type="password"
                [(ngModel)]="newPassword"
                name="newPassword"
                #newPasswordCtrl="ngModel"
                required
                minlength="6"
                class="input-base w-full mb-3"
                placeholder="New password"
              />
              @if (newPasswordCtrl.invalid && (newPasswordCtrl.dirty || newPasswordCtrl.touched)) {
                <p class="text-red-400 text-xs">
                  @if (newPasswordCtrl.errors?.['required']) {
                    <span>Password is required.</span>
                  }
                  @if (newPasswordCtrl.errors?.['minlength']) {
                    <span>Password must be at least 6 characters.</span>
                  }
                </p>
              }
              <label class="block text-sm font-medium text-gray-700 mb-1 mt-2">
                Confirm password
              </label>
              <input
                type="password"
                [(ngModel)]="confirmPassword"
                name="confirmPassword"
                #confirmPasswordCtrl="ngModel"
                required
                minlength="6"
                class="input-base w-full mb-4"
                placeholder="Confirm password"
              />
              @if (confirmPasswordCtrl.invalid && (confirmPasswordCtrl.dirty || confirmPasswordCtrl.touched)) {
                <p class="text-red-400 text-xs">
                  @if (confirmPasswordCtrl.errors?.['required']) {
                    <span>Password is required.</span>
                  } 
                  @if (newPassword !== confirmPassword) {
                    <span>Passwords do not match.</span>
                  }
                </p>
              }
              <div class="flex justify-end gap-2">
                <button
                  type="button"
                  class="px-4 py-2 rounded-full text-sm border border-gray-300 text-gray-700 hover:bg-gray-100"
                  (click)="closeForgot()"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  [disabled]="newPasswordForm.invalid || !newPassword || !confirmPassword || newPassword !== confirmPassword"
                  class="px-4 py-2 rounded-full text-sm bg-app-accent text-app-dark hover:bg-app-accent-dark disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Save password
                </button>
              </div>
            </form>
          }
        </div>
      </div>
    }
  </main>
</div>
`})
export class SignInComponent {
  email = '';
  password = '';

  isForgotOpen = false;
  forgotStep: 'email' | 'password' = 'email';

  emailForReset = '';
  newPassword = '';
  confirmPassword = '';

  private router = inject(Router);

  onSubmit() {
    this.router.navigate(['/user/profile']);
  }

  openForgot() {
    this.isForgotOpen = true;
    this.forgotStep = 'email';
    this.emailForReset = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  closeForgot() {
    this.isForgotOpen = false;
  }

  sendResetEmail() {
    this.forgotStep = 'password';
  }

  setNewPassword() {
    this.closeForgot();
  }
}


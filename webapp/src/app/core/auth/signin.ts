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
  </main>
</div>
`})
export class SignInComponent {
  email = '';
  password = '';
  private router = inject(Router);
  
  onSubmit() {
    this.router.navigate(['/profile']);
    console.log('Logging in with', this.email, this.password);
  }
}

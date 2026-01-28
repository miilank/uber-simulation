import { ChangeDetectorRef, Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule], // Add FormsModule
  template: `
    <div class="w-full max-w-sm bg-white rounded-2xl shadow-xl p-6">
      
      @if (success) {
        <div class="text-center space-y-4">
          <div class="flex justify-center">
            <svg class="w-16 h-16 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h2 class="text-lg font-semibold text-gray-900">Password Reset Successfully!</h2>
          <p class="text-sm text-gray-600">Redirecting to login...</p>
        </div>
      }

      @if (!success && !invalidToken) {
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
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent mb-3"
            placeholder="New password"
          />
          @if (newPasswordCtrl.invalid && (newPasswordCtrl.dirty || newPasswordCtrl.touched)) {
            <p class="text-red-400 text-xs mb-2">
              @if (newPasswordCtrl.errors?.['required']) {
                <span>Password is required.</span>
              }
              @if (newPasswordCtrl.errors?.['minlength']) {
                <span>Password must be at least 8 characters.</span>
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
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent mb-4"
            placeholder="Confirm password"
          />
          @if (confirmPasswordCtrl.touched && newPassword !== confirmPassword) {
            <p class="text-red-400 text-xs mb-2">
              <span>Passwords do not match.</span>
            </p>
          }

          @if (error) {
            <p class="text-red-400 text-xs mb-2">{{ errorMessage }}</p>
          }

          <div class="flex justify-end gap-2 mt-4">
            <button
              type="button"
              class="px-4 py-2 rounded-full text-sm border border-gray-300 text-gray-700 hover:bg-gray-100"
              (click)="goToLogin()"
            >
              Cancel
            </button>
            <button
              type="submit"
              [disabled]="loading || newPasswordForm.invalid || !newPassword || !confirmPassword || newPassword !== confirmPassword"
              class="px-4 py-2 rounded-full text-sm bg-app-accent text-app-dark hover:bg-app-accent-dark disabled:opacity-50 disabled:cursor-not-allowed"
            >
              @if (loading) {
                <span>Saving...</span>
              } @else {
                <span>Save password</span>
              }
            </button>
          </div>
        </form>
      }

      @if (invalidToken) {
        <div class="text-center space-y-4">
          <div class="flex justify-center">
            <svg class="w-16 h-16 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h2 class="text-lg font-semibold text-gray-900">Invalid Reset Link</h2>
          <p class="text-sm text-gray-600">{{ errorMessage }}</p>
          <button 
            (click)="goToLogin()"
            class="mt-4 px-4 py-2 rounded-full text-sm bg-app-accent text-app-dark hover:bg-app-accent-dark">
            Go to Login
          </button>
        </div>
      }

    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
  loading = false;
  success = false;
  error = false;
  invalidToken = false;
  errorMessage = '';
  @Input() token: string = '';  
  newPassword = '';
  confirmPassword = '';
  
  @Output() closeModal = new EventEmitter<void>();
  
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  ngOnInit() {
    this.token = this.route.snapshot.queryParams['token'];
  }

  setNewPassword() {
    this.loading = true;
    this.error = false;

    this.authService.resetPassword(this.token, this.newPassword,this.confirmPassword).subscribe({
      next: (response) => {
        this.loading = false;
        this.success = true;
        this.cdr.detectChanges();
        setTimeout(() => this.close(), 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = true;
        if (!this.token) {
            this.invalidToken = true;
            this.errorMessage = 'Invalid or missing reset token';
        }
        this.errorMessage = err.error?.message || 'Failed to reset password';
        this.cdr.detectChanges();
      }
    });
  }
    close() { 
    this.closeModal.emit();
  }
  goToLogin() {
    this.router.navigate(['/signIn']);
  }
}

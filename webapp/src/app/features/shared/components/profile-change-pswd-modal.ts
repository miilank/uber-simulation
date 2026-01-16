import { ChangeDetectorRef, Component, EventEmitter, input, Input, Output, SimpleChange, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'change-password-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div
      *ngIf="isChangePasswordOpen"
      class="fixed inset-0 z-50 flex items-center justify-center">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/50"></div>

      <!-- Modal Card -->
      <div
        class="relative bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6 z-10"
        (click)="$event.stopPropagation()">

        <!-- Header -->
        <div class="flex items-center justify-between">
          <h4 class="text-lg font-medium">Change Password</h4>
          <button
            (click)="onClose(false)"
            class="text-gray-500 hover:text-gray-700 cursor-pointer"
            type="button"
          >
            &times;
          </button>
        </div>

        <!-- Form -->
        <form  class="mt-4 space-y-4" (ngSubmit)="onConfirm()" novalidate>
          <div class="flex flex-col gap-2">
            <label class="text-sm text-gray-700">Old Password</label>
            <input
              type="password"
              name="oldPassword"
              [(ngModel)]="oldPassword"
              class="input-base"
              placeholder="Enter old password"
            />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-sm text-gray-700">New Password</label>
            <input
              type="password"
              name="newPassword"
              [(ngModel)]="newPassword"
              class="input-base"
              placeholder="Enter new password (min 8 chars)"
            />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-sm text-gray-700">Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              [(ngModel)]="confirmPassword"
              class="input-base"
              placeholder="Confirm new password"
            />
          </div>

          <!-- Error message area -->
          <div *ngIf="errorMessage" class="text-center text-sm text-red-700">
            {{ errorMessage }}
          </div>

          <div class="flex gap-3 justify-end pt-2">
            <button
              type="button"
              class="h-10 px-4 rounded-full border border-gray-300 text-sm text-gray-700 hover:bg-gray-50 cursor-pointer"
              (click)="onClose(false)">
              Cancel
            </button>

            <button
              type="submit"
              class="h-10 px-4 rounded-full bg-app-accent text-sm text-neutral-900 hover:bg-lime-500 cursor-pointer">
              Confirm
            </button>
          </div>
        </form>
      </div>
  `,
})
export class ChangePasswordModal {
  @Input() isChangePasswordOpen: boolean = false;
  @Output() close: EventEmitter<boolean> = new EventEmitter<boolean>();

  oldPassword: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  errorMessage: string | null = null;

  constructor(private userService: UserService, private cdr: ChangeDetectorRef) {}

  onClose(updated: boolean): void {
    this.clearPasswordFields();
    this.errorMessage = null;
    this.close.emit(updated);
  }

  onConfirm(): void {
    let validation = this.validatePasswords();
    this.errorMessage = validation.message;
    
    if(validation.valid){
      this.userService.changePassword(this.oldPassword, this.newPassword).subscribe({
        next: () => {this.onClose(true)},
        error: err => {
          console.log(err);
          
          this.errorMessage = err["error"]["message"];
          this.cdr.markForCheck();
        }
    });
    }
  }

  clearPasswordFields(): void {
    this.oldPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  private validatePasswords(): { valid: boolean; message: string | null } {

    if (!this.oldPassword) {
      return { valid: false, message: 'Please enter your old password.' };
    }
    if (!this.newPassword) {
      return { valid: false, message: 'Please enter your new password.' };
    }
    if (!this.confirmPassword) {
      return { valid: false, message: 'Please confirm your new password.' };
    }
    if (!(this.newPassword === this.confirmPassword)) {
      return { valid: false, message: 'The passwords do not match.' };
    }
    if (this.newPassword.length < 8) {
      return { valid: false, message: 'Password must be at least 8 characters.'}
    }

    return { valid: true, message: null };
  }

}

import { Component, EventEmitter, Input, Output, SimpleChange, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User } from '../../../core/models/user';
import { log } from 'console';

@Component({
  selector: 'profile-info-card',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
      <div class="flex flex-col">
        <div class="flex flex-col gap-6 w-full">
              <!-- Profile Header Card -->
              <div class="border-[1.5px] border-gray-200 rounded-3xl shadow-lg p-8">
                <div class="flex items-center gap-6">
                  <!-- Profile Picture -->
                  <div class="relative group w-30 h-30 cursor-pointer">
                    <div class="w-full h-full rounded-full border-[3px] border-app-accent p-0.5">
                      <img
                        class="w-full h-full rounded-full object-cover group-hover:brightness-80"
                        src="defaultprofile.png"
                        alt="Profile"
                      />
                    </div>
                    <button class="absolute bottom-0 right-0 w-9 h-9 bg-app-accent rounded-full shadow-lg flex items-center justify-center">
                      <img src="camera.svg" alt="Camera">
                    </button>
                  </div>

                  <!-- User Info -->
                  <div class="flex flex-col gap-1">
                    <h2 class="text-[28px] font-normal font-poppins text-black leading-10.5">{{user.firstName}} {{user.lastName}}</h2>
                    <p class="text-base font-normal font-poppins text-gray-600">{{user.email}}</p>
                  </div>
                </div>
              </div>

              <!-- Info Card -->
              <div class="border-[1.5px] border-gray-200 rounded-3xl shadow-lg p-8 flex flex-col gap-11">
                <h3 class="text-[22px] font-normal font-poppins text-black leading-8.25">Personal Information</h3>

                <!-- Form Fields -->
                <div class="flex flex-col gap-2.5">

                  <!-- Email Field -->
                  <div class="flex flex-col gap-2">
                    <label class="text-sm font-normal font-poppins text-gray-700 flex items-center gap-2">
                      <img class="w-6 h-6" src="mail.svg" alt="Mail"/>
                      Email
                    </label>
                    <input
                      type="email"
                      [(ngModel)]="editableUser.email"
                      class="input-base brightness-95"
                      placeholder="E-Mail"
                      disabled
                    />
                  </div>

                  <!-- First Name & Last Name Row -->
                  <div class="flex gap-2.5 flex-col sm:flex-row">
                    <div class="flex-1 flex flex-col gap-2">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        <img class="w-6 h-6" src="profile.svg" alt="Phone" />
                        First Name</label>
                      <input
                        type="text"
                        [(ngModel)]="editableUser.firstName"
                        class="input-base"
                        placeholder="First name"
                      />
                    </div>
                    <div class="flex-1 flex flex-col gap-2">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        <img class="w-6 h-6" src="profile.svg" alt="Phone" />
                        Last Name</label>
                      <input
                        type="text"
                        [(ngModel)]="editableUser.lastName"
                        class="input-base"
                        placeholder="Last name"
                      />
                    </div>
                  </div>

                  <!-- Address Field -->
                  <div class="flex flex-col gap-2">
                    <label class="text-sm font-normal font-poppins text-gray-700 flex items-center gap-2">
                      <img class="w-6 h-6" src="location.svg" alt="address"/>
                      Address
                    </label>
                    <input
                      type="text"
                      [(ngModel)]="editableUser.address"
                      class="input-base"
                      placeholder="Address"
                    />
                  </div>

                  <!-- Phone Number Field -->
                  <div class="flex flex-col gap-2">
                    <label class="text-sm font-normal font-poppins text-gray-700 flex items-center gap-2">
                      <img class="w-6 h-6" src="phone.svg" alt="Phone" />
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      [(ngModel)]="editableUser.phoneNumber"
                      class="input-base"
                      placeholder="Phone number"
                    />
                  </div>
                </div>

                <!-- Action Buttons -->
                <div class="flex flex-col gap-3">

                  <!-- Error message area -->
                  <div *ngIf="errorMessage" class="text-center text-sm text-red-700">
                    {{ errorMessage }}
                  </div>

                  <button (click)="onOpenPswdChange()" class="cursor-pointer h-12 border-[1.5px] border-gray-300 rounded-full text-sm font-normal font-poppins text-gray-700 hover:bg-gray-50 transition-colors">
                    Change Password
                  </button>

                  <div class="flex gap-4 flex-col sm:flex-row">
                    <button (click)="onSave()" class="cursor-pointer flex-1 h-12 bg-app-accent rounded-full text-sm font-normal font-poppins text-neutral-900 hover:bg-lime-500 transition-colors">
                      Update Profile
                    </button>
                    <button (click)="onCancel()" class="cursor-pointer flex-1 h-12 border-[1.5px] border-gray-300 rounded-full text-sm font-normal font-poppins text-gray-700 hover:bg-gray-50 transition-colors">
                      Cancel
                    </button>
                    
                  </div>

                </div>
              </div>
        </div>

      </div>
  `,
})
export class ProfileInfoCard {
  @Output() save = new EventEmitter<User>();
  @Output() openPswdChange = new EventEmitter<void>();
  
  editableUser!: User;

  private _user!: User;

  @Input()
  set user(value: User) {
    this._user = value;
    this.editableUser = { ...value };
    this.errorMessage = null;
  }

  get user(): User {
    return this._user;
  }

  errorMessage: string | null = null;

  ngOnChanges(): void {    
    this.editableUser = { ...this.user } as User;
  }

  onOpenPswdChange(): void {    
    this.openPswdChange.emit();
  }

  onCancel(): void { 
    this.editableUser = { ...this.user }
  }

  onSave(): void {
    let validation = this.validateEditableUser(this.editableUser);
    this.errorMessage = validation.message;
    
    if (validation.valid) {
      this.save.emit({ ...this.editableUser });
    }
  }

  private validateEditableUser(u: User): { valid: boolean; message: string | null } {

    const first = (u.firstName || '').trim();
    const last = (u.lastName || '').trim();
    const email = (u.email || '').trim();
    const address = (u.address || '').trim();
    const phone = (u.phoneNumber || '').trim();

    if (!first) {
      return { valid: false, message: 'Please enter a first name.' };
    }
    if (!last) {
      return { valid: false, message: 'Please enter a last name.' };
    }
    if (!email) {
      return { valid: false, message: 'Please enter an email address.' };
    }
    if (!this.isEmailValid(email)) {
      return { valid: false, message: 'Please enter a valid email address.' };
    }
    if (!address) {
      return { valid: false, message: 'Please enter an address.' };
    }
    if (!phone) {
      return { valid: false, message: 'Please enter a phone number.' };
    }
    if (!this.isPhoneValid(phone)) {
      return { valid: false, message: 'Invalid phone number.' };
    }

    return { valid: true, message: null };
  }

  private isEmailValid(email: string): boolean {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/i;
    return re.test(email);
  }

  private isPhoneValid(phone: string): boolean {
    const digits = phone.replace(/\D/g, '');
    if (digits.length < 7) {
      return false;
    }
    const re = /^[\d+\-\s().]+$/;
    return re.test(phone);
  }
}

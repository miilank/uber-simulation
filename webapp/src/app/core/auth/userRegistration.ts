import { ChangeDetectorRef, Component, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../features/shared/components/header.component';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RegisterRequestDto } from './register-request.dto';
import { first } from 'rxjs';
import { ProfilePictureComponent } from "../../features/shared/components/profile-picture.component";

@Component({
  selector: 'user-registration',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, ProfilePictureComponent],
  template: `
    <div class="min-h-screen flex flex-col bg-linear-to-b from-[#d6f4a2] to-[#f7f7f7] font-poppins">
      <!-- Header -->
      <app-header [showUserProfile]="false"></app-header>

      <!-- Centered form card -->
      <main class="flex-1 flex items-center justify-center px-4 py-8">
        <div class="w-full max-w-4xl bg-[#181818] rounded-[44px] px-8 py-10 md:px-16 md:py-12 shadow-xl">
          <div class="flex justify-center mb-8">
              <app-profile-picture
              [editable]="true"
              (avatarSelected)="setPicture($event)"
              class="w-30 h-30"></app-profile-picture>
          </div>


          <form #registerForm="ngForm" (ngSubmit)="registerForm.valid && onSubmit()" class="space-y-6">
            <!-- Row 1 -->
            <div class="grid grid-cols-4 gap-4">
              <!-- Name -->
              <div class="col-span-1 flex flex-col gap-2">
                <label class="text-white font-normal font-poppins">Name</label>
                <input
                  type="text"
                  name="firstName"
                  [(ngModel)]="firstName"
                  #firstNameCtrl="ngModel"
                  required
                  minlength="2"
                  class="input-base w-full"
                  placeholder="Name"
                />
                @if (firstNameCtrl.invalid && (firstNameCtrl.dirty || firstNameCtrl.touched)) {
                  <p class="text-red-400 text-xs">
                    @if (firstNameCtrl.errors?.['required']) { <span>Name is required.</span> }
                    @if (firstNameCtrl.errors?.['minlength']) { <span>Name must be at least 2 characters.</span> }
                  </p>
                }
              </div>

              <!-- Surname -->
              <div class="col-span-1 flex flex-col gap-2">
                <label class="text-white font-normal font-poppins">Surname</label>
                <input
                  type="text"
                  name="lastName"
                  [(ngModel)]="lastName"
                  #lastNameCtrl="ngModel"
                  required
                  minlength="2"
                  class="input-base w-full"
                  placeholder="Surname"
                />
                @if (lastNameCtrl.invalid && (lastNameCtrl.dirty || lastNameCtrl.touched)) {
                  <p class="text-red-400 text-xs">
                    @if (lastNameCtrl.errors?.['required']) { <span>Surname is required.</span> }
                    @if (lastNameCtrl.errors?.['minlength']) { <span>Surname must be at least 2 characters.</span> }
                  </p>
                }
              </div>

              <!-- Email -->
              <div class="col-span-2 flex flex-col gap-2">
                <label class="text-white font-normal font-poppins">Email</label>
                <input
                  type="email"
                  name="email"
                  [(ngModel)]="email"
                  #emailCtrl="ngModel"
                  required
                  email
                  class="input-base w-full"
                  placeholder="Email"
                />
                @if (emailCtrl.invalid && (emailCtrl.dirty || emailCtrl.touched)) {
                  <p class="text-red-400 text-xs">
                    @if (emailCtrl.errors?.['required']) { <span>Email is required.</span> }
                    @if (emailCtrl.errors?.['email']) { <span>Please enter a valid email address.</span> }
                  </p>
                }
              </div>
            </div>

            <!-- Row 2 -->
            <div class="grid grid-cols-2 gap-4">
              <!-- Address -->
              <div class="flex flex-col gap-2">
                <label class="text-white font-normal font-poppins">Address</label>
                <input
                  type="text"
                  name="address"
                  [(ngModel)]="address"
                  #addressCtrl="ngModel"
                  required
                  minlength="5"
                  class="input-base w-full"
                  placeholder="Address"
                />
                @if (addressCtrl.invalid && (addressCtrl.dirty || addressCtrl.touched)) {
                  <p class="text-red-400 text-xs">
                    @if (addressCtrl.errors?.['required']) { <span>Address is required.</span> }
                    @if (addressCtrl.errors?.['minlength']) { <span>Address must be at least 5 characters.</span> }
                  </p>
                }
              </div>

              <!-- Phone Number -->
              <div class="flex flex-col gap-2">
                <label class="text-white font-normal font-poppins">Phone Number</label>
                <input
                  type="tel"
                  name="phone"
                  [(ngModel)]="phone"
                  #phoneCtrl="ngModel"
                  required
                  pattern="[+]?[0-9\\s\\-\\(\\)]{10,}"
                  class="input-base w-full"
                  placeholder="Phone number"
                />
                @if (phoneCtrl.invalid && (phoneCtrl.dirty || phoneCtrl.touched)) {
                  <p class="text-red-400 text-xs">
                    @if (phoneCtrl.errors?.['required']) { <span>Phone number is required.</span> }
                    @if (phoneCtrl.errors?.['pattern']) { <span>Please enter a valid phone number.</span> }
                  </p>
                }
              </div>
            </div>

            <!-- Row 3 -->
            <div class="grid grid-cols-2 gap-4">
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
                  class="input-base w-full"
                  placeholder="Password"
                />
                @if (passwordCtrl.invalid && (passwordCtrl.dirty || passwordCtrl.touched)) {
                  <p class="text-red-400 text-xs">
                    @if (passwordCtrl.errors?.['required']) { <span>Password is required.</span> }
                    @if (passwordCtrl.errors?.['minlength']) { <span>Password must be at least 8 characters.</span> }
                  </p>
                }
              </div>

              <!-- Confirm Password -->
              <div class="flex flex-col gap-2">
                <label class="text-white font-normal font-poppins">Confirm Password</label>
                <input
                  type="password"
                  name="confirmPassword"
                  [(ngModel)]="confirmPassword"
                  #confirmCtrl="ngModel"
                  required
                  class="input-base w-full"
                  placeholder="Confirm Password"
                />
                @if (confirmCtrl.invalid && (confirmCtrl.dirty || confirmCtrl.touched)) {
                  <p class="text-red-400 text-xs">
                    @if (confirmCtrl.errors?.['required']) { <span>Confirm password is required.</span> }
                  </p>
                }
              </div>
            </div>

            <!-- Register button -->
            <div class="pt-4">
              <button
                type="submit"
                [disabled]="registerForm.invalid || password !== confirmPassword || isSubmitting"
                class="w-full bg-app-accent text-black text-sm font-normal font-poppins py-3 rounded-full hover:bg-app-accent-dark disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                @if (isSubmitting) {
                  <span>Registering...</span>
                } @else {
                  <span>Register</span>
                }
              </button>
            </div>
          </form>
        </div>
        @if (registrationSuccess) {
        <div class="fixed inset-0 z-50 flex items-center justify-center">
          <!-- Backdrop -->
          <div class="absolute inset-0 bg-black/50"></div>

          <!-- Modal -->
          <div class="relative z-10 w-full max-w-sm bg-white rounded-2xl shadow-xl p-6">
            <div class="text-center space-y-4">
              <div class="flex justify-center">
                <svg class="w-16 h-16 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
              <h2 class="text-lg font-semibold text-gray-900">Registration Successful!</h2>
              <p class="text-sm text-gray-600">
                Please check your email at <span class="font-medium">{{ email }}</span> to activate your account.
              </p>
              <button
                (click)="closeSuccessPopup()"
                class="mt-4 px-6 py-2 rounded-full text-sm bg-app-accent text-app-dark hover:bg-app-accent-dark transition"
              >
                Go to Sign In
              </button>
            </div>
          </div>
        </div>
      }

      <!-- Error Message -->
      @if (registrationError) {
        <div class="fixed top-4 right-4 z-50 bg-red-50 border border-red-200 rounded-lg p-4 shadow-lg max-w-md">
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <div class="flex-1">
              <p class="text-sm text-red-800">{{ registrationError }}</p>
            </div>
            <button (click)="registrationError = null" class="text-red-500 hover:text-red-700">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
        </div>
      }
      </main>
    </div>
  `
})
export class UserRegistrationComponent {
  firstName = '';
  lastName = '';
  email = '';
  address = '';
  phone = '';
  password = '';
  confirmPassword = '';

  registrationSuccess = false;
  registrationError: string | null = null;
  isSubmitting = false;

  selectedPicture: File | null = null;

  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  constructor(public authService : AuthService) {
  }

  onSubmit() {
    this.isSubmitting = true;
    console.log("Submitted");
    

    let req: RegisterRequestDto = {
      email: this.email,
      password: this.password,
      confirmPassword: this.confirmPassword,
      firstName: this.firstName,
      lastName: this.lastName,
      address: this.address,
      phoneNumber: this.phone
    }

    const form: FormData = new FormData();

    const jsonBlob: Blob  = new Blob([JSON.stringify(req)], { type: 'application/json' });
    form.append('user', jsonBlob);
        
    if (this.selectedPicture) {
      form.append('avatar', this.selectedPicture, this.selectedPicture.name);
    }
    
    this.authService.register(form).subscribe({
        next: () => {
          this.isSubmitting = false;
          this.registrationSuccess = true;
          this.cdr.detectChanges();
          console.log('Signup request sent successfully!');
        },
        error: (err) => {
          this.isSubmitting = false;
          this.registrationError = err.error?.message || 'Registration failed. Please try again.';
          this.cdr.detectChanges();
          console.error('Signup failed', err);
        }}
      )
  }

  setPicture(file: File) {
    this.selectedPicture = file;
  }

  closeSuccessPopup() {
    this.registrationSuccess = false;
    this.router.navigate(['/signin']);
  }
}

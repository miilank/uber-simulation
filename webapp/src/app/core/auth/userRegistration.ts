import { Component, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../features/shared/components/header.component';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RegisterRequestDto } from './register-request.dto';
import { first } from 'rxjs';

@Component({
  selector: 'user-registration',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent],
  template: `
    <div class="min-h-screen flex flex-col bg-linear-to-b from-[#d6f4a2] to-[#f7f7f7] font-poppins">
      <!-- Header -->
      <app-header [showUserProfile]="false"></app-header>

      <!-- Centered form card -->
      <main class="flex-1 flex items-center justify-center px-4 py-8">
        <div class="w-full max-w-4xl bg-[#181818] rounded-[44px] px-8 py-10 md:px-16 md:py-12 shadow-xl">
          <div class="flex justify-center mb-8">
            <div class="w-32 h-32 rounded-full bg-white flex items-center justify-center">
              <img
                src="defaultprofile.png"
                alt="Profile"
                class="w-28 h-28 rounded-full object-cover"
              />
            </div>
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
                    @if (passwordCtrl.errors?.['minlength']) { <span>Password must be at least 6 characters.</span> }
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
                [disabled]="registerForm.invalid || password !== confirmPassword"
                class="w-full bg-app-accent text-black text-sm font-normal font-poppins py-3 rounded-full hover:bg-app-accent-dark disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                Register
              </button>
            </div>
          </form>
        </div>
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

  private router = inject(Router);

  constructor(public authService : AuthService) {
  }

  onSubmit() {
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
    
    this.authService.register(req).subscribe({
  next: () => {
    console.log('Signup request sent successfully!');
  },
  error: (err) => {
    console.error('Signup failed', err);
  }}
  )
    // this.router.navigate(['/login']);
  }
}

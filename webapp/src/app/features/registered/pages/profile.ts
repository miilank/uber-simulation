import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-white">
      <div class="flex flex-col min-h-screen">
        <div class="flex flex-1">
          <!-- Sidebar -->
          <!-- <app-sidebar></app-sidebar> -->

          <!-- Main Content -->
          <main class="flex flex-1 p-8 justify-center items-center">
            <div class="flex flex-col gap-6 w-full">
              <!-- Profile Header Card -->
              <div class="border-[1.5px] border-gray-200 rounded-3xl shadow-lg p-8">
                <div class="flex items-center gap-6">
                  <!-- Profile Picture -->
                  <div class="relative group w-[120px] h-[120px] cursor-pointer">
                    <div class="w-full h-full rounded-full border-[3px] border-app-accent p-0.5">
                      <img
                        class="w-full h-full rounded-full object-cover group-hover:brightness-80"
                        src="defaultprofile.png"
                        alt="Profile"
                      />
                    </div>
                    <button class="absolute bottom-0 right-0 w-9 h-9 bg-app-accent rounded-full shadow-lg flex items-center justify-center">
                      <!-- <svg class="w-5 h-5" viewBox="0 0 20 20" fill="none">
                        <path d="M11.6596 3.33203C11.9602 3.33203 12.2551 3.41332 12.5132 3.5673C12.7713 3.72128 12.9829 3.94222 13.1257 4.20669L13.5305 4.9564C13.6733 5.22087 13.8849 5.4418 14.143 5.59578C14.4011 5.74976 14.6961 5.83106 14.9966 5.83105H16.6602C17.102 5.83105 17.5258 6.00658 17.8382 6.31902C18.1506 6.63146 18.3262 7.05522 18.3262 7.49707V14.9941C18.3262 15.436 18.1506 15.8598 17.8382 16.1722C17.5258 16.4846 17.102 16.6602 16.6602 16.6602H3.33203C2.89018 16.6602 2.46642 16.4846 2.15398 16.1722C1.84154 15.8598 1.66602 15.436 1.66602 14.9941V7.49707C1.66602 7.05522 1.84154 6.63146 2.15398 6.31902C2.46642 6.00658 2.89018 5.83105 3.33203 5.83105H4.99555C5.29578 5.83107 5.59044 5.74996 5.84837 5.59629C6.10629 5.44261 6.3179 5.2221 6.46081 4.95806L6.86815 4.20502C7.01106 3.94098 7.22267 3.72047 7.48059 3.5668C7.73852 3.41313 8.03318 3.33201 8.33341 3.33203H11.6596Z" stroke="#1F1F1F" stroke-width="1.66602" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M9.99609 13.3281C11.3763 13.3281 12.4951 12.2093 12.4951 10.8291C12.4951 9.44893 11.3763 8.33008 9.99609 8.33008C8.61592 8.33008 7.49707 9.44893 7.49707 10.8291C7.49707 12.2093 8.61592 13.3281 9.99609 13.3281Z" stroke="#1F1F1F" stroke-width="1.66602" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg> -->
                      <img src="camera.svg" alt="Camera">
                    </button>
                  </div>

                  <!-- User Info -->
                  <div class="flex flex-col gap-1">
                    <h2 class="text-[28px] font-normal font-poppins text-black leading-[42px]">Andrew Wilson</h2>
                    <p class="text-base font-normal font-poppins text-gray-600">andrewwilson&#64;email.com</p>
                  </div>
                </div>
              </div>

              <!-- Info Card -->
              <div class="border-[1.5px] border-gray-200 rounded-3xl shadow-lg p-8 flex flex-col gap-11">
                <h3 class="text-[22px] font-normal font-poppins text-black leading-[33px]">Personal Information</h3>

                <!-- Form Fields -->
                <div class="flex flex-col gap-2.5">
                  <!-- First Name & Last Name Row -->
                  <div class="flex gap-2.5 flex-col sm:flex-row">
                    <div class="flex-1 flex flex-col gap-2">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        <img class="w-6 h-6" src="profile.svg" alt="Phone" />
                        First Name</label>
                      <input
                        type="text"
                        [(ngModel)]="firstName"
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
                        [(ngModel)]="lastName"
                        class="input-base"
                        placeholder="Last name"
                      />
                    </div>
                  </div>

                  <!-- Email Field -->
                  <div class="flex flex-col gap-2">
                    <label class="text-sm font-normal font-poppins text-gray-700 flex items-center gap-2">
                      <!-- <svg class="w-4 h-4" viewBox="0 0 16 16" fill="none">
                        <path d="M14.6523 4.66211L8.6642 8.47638C8.46099 8.59441 8.23018 8.65657 7.99518 8.65657C7.76019 8.65657 7.52938 8.59441 7.32617 8.47638L1.33203 4.66211" stroke="#364153" stroke-width="1.33203" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M13.3203 2.66406H2.66406C1.9284 2.66406 1.33203 3.26043 1.33203 3.99609V11.9883C1.33203 12.7239 1.9284 13.3203 2.66406 13.3203H13.3203C14.056 13.3203 14.6523 12.7239 14.6523 11.9883V3.99609C14.6523 3.26043 14.056 2.66406 13.3203 2.66406Z" stroke="#364153" stroke-width="1.33203" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg> -->
                      <img class="w-6 h-6" src="mail.svg" alt="Mail"/>
                      Email
                    </label>
                    <input
                      type="email"
                      [(ngModel)]="email"
                      class="input-base"
                      placeholder="E-Mail"
                    />
                  </div>

                  <!-- Address Field -->
                  <div class="flex flex-col gap-2">
                    <label class="text-sm font-normal font-poppins text-gray-700 flex items-center gap-2">
                      <!-- <svg class="w-4 h-4" viewBox="0 0 16 16" fill="none">
                        <path d="M13.3203 6.66016C13.3203 9.98557 9.63125 13.4489 8.39246 14.5185C8.27706 14.6053 8.13658 14.6522 7.99219 14.6522C7.8478 14.6522 7.70732 14.6053 7.59191 14.5185C6.35312 13.4489 2.66406 9.98557 2.66406 6.66016C2.66406 5.24705 3.22542 3.89182 4.22463 2.8926C5.22385 1.89339 6.57908 1.33203 7.99219 1.33203C9.40529 1.33203 10.7605 1.89339 11.7597 2.8926C12.759 3.89182 13.3203 5.24705 13.3203 6.66016Z" stroke="#364153" stroke-width="1.33203" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M7.99219 8.6582C9.09568 8.6582 9.99023 7.76365 9.99023 6.66016C9.99023 5.55667 9.09568 4.66211 7.99219 4.66211C6.8887 4.66211 5.99414 5.55667 5.99414 6.66016C5.99414 7.76365 6.8887 8.6582 7.99219 8.6582Z" stroke="#364153" stroke-width="1.33203" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg> -->
                      <img class="w-6 h-6" src="location.svg" alt="address"/>
                      Address
                    </label>
                    <input
                      type="text"
                      [(ngModel)]="address"
                      class="input-base"
                      placeholder="Address"
                    />
                  </div>

                  <!-- Phone Number Field -->
                  <div class="flex flex-col gap-2">
                    <label class="text-sm font-normal font-poppins text-gray-700 flex items-center gap-2">
                      <!-- <svg class="w-4 h-4" viewBox="0 0 16 16" fill="none">
                        <path d="M9.21233 11.0345C9.34988 11.0977 9.50484 11.1121 9.65169 11.0755C9.79854 11.0388 9.92852 10.9532 10.0202 10.8327L10.2566 10.523C10.3807 10.3576 10.5416 10.2233 10.7266 10.1309C10.9115 10.0384 11.1155 9.99023 11.3223 9.99023H13.3203C13.6736 9.99023 14.0124 10.1306 14.2622 10.3804C14.512 10.6302 14.6523 10.969 14.6523 11.3223V13.3203C14.6523 13.6736 14.512 14.0124 14.2622 14.2622C14.0124 14.512 13.6736 14.6523 13.3203 14.6523C10.1408 14.6523 7.09156 13.3893 4.84332 11.1411C2.59508 8.89282 1.33203 5.84355 1.33203 2.66406C1.33203 2.31079 1.47237 1.97198 1.72217 1.72217C1.97198 1.47237 2.31079 1.33203 2.66406 1.33203H4.66211C5.01539 1.33203 5.35419 1.47237 5.604 1.72217C5.8538 1.97198 5.99414 2.31079 5.99414 2.66406V4.66211C5.99414 4.8689 5.94599 5.07285 5.85351 5.25781C5.76103 5.44277 5.62676 5.60366 5.46133 5.72773L5.14963 5.96151C5.02736 6.05487 4.94118 6.18768 4.90573 6.33737C4.87028 6.48707 4.88774 6.64442 4.95516 6.7827C5.86539 8.63147 7.36242 10.1266 9.21233 11.0345Z" stroke="#364153" stroke-width="1.33203" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg> -->
                      <img class="w-6 h-6" src="phone.svg" alt="Phone" />
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      [(ngModel)]="phoneNumber"
                      class="input-base"
                      placeholder="Phone number"
                    />
                  </div>
                </div>

                <!-- Action Buttons -->
                <div class="flex flex-col gap-3">
                  <button class="h-12 border-[1.5px] border-gray-300 rounded-full text-sm font-normal font-poppins text-gray-700 hover:bg-gray-50 transition-colors">
                    Change Password
                  </button>

                  <div class="flex gap-4 flex-col sm:flex-row">
                    <button class="flex-1 h-12 bg-lime-400 rounded-full text-sm font-normal font-poppins text-neutral-900 hover:bg-lime-500 transition-colors">
                      Update Profile
                    </button>
                    <button class="flex-1 h-12 border-[1.5px] border-gray-300 rounded-full text-sm font-normal font-poppins text-gray-700 hover:bg-gray-50 transition-colors">
                      Cancel
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </main>
        </div>
      </div>
    </div>
  `,
})
export class ProfileComponent {
  firstName: string = 'Andrew';
  lastName: string = 'Wilson';
  email: string = 'andrewwilson@email.com';
  address: string = 'Novi Sad';
  phoneNumber: string = '+381 65 123 1233';
}

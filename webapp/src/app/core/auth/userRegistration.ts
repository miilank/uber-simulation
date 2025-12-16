import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../components/header.component';

@Component({
  selector: 'user-registration',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent],
  template: `
    <div class="min-h-screen flex flex-col bg-linear-to-b from-[#d6f4a2] to-[#f7f7f7]">
  <!-- Header -->
  <app-header [showUserProfile]="false"></app-header>

  <!-- Centered form card -->
  <main class="flex-1 flex items-center justify-center px-4 py-8">
    <div
      class="w-full max-w-4xl bg-[#181818] rounded-[44px] px-8 py-10 md:px-16 md:py-12 shadow-xl"
    >
      <div class="flex justify-center mb-8">
        <div class="w-32 h-32 rounded-full bg-white flex items-center justify-center">
            <img
            src="defaultprofile.png"
            alt="Profile"
            class="w-28 h-28 rounded-full object-cover"
            />
        </div>
    </div>
<form class="space-y-6">
  <!-- Row 1: Name (1/4) + Surname (1/4) + Email (1/2) -->
  <div class="grid grid-cols-4 gap-4">
    <div class="col-span-1 flex flex-col gap-2">
      <label class="text-white font-normal font-poppins ">Name</label>
      <input
        type="text"
        placeholder="Value"
        class="input-base"
        placeholder="Name"
      />
    </div>

    <div class="col-span-1 flex flex-col gap-2">
      <label class="text-white font-normal font-poppins ">Surname</label>
      <input
        type="text"
        placeholder="Value"
        class="input-base"
        placeholder="Surname"
      />
    </div>

    <div class="col-span-2 flex flex-col gap-2">
      <label class="text-white font-normal font-poppins ">Email</label>
      <input
        type="email"
        placeholder="Value"
        class="input-base"
        placeholder="Email"
      />
    </div>
  </div>

  <!-- Row 2: Address (1/2) + Phone (1/2) -->
  <div class="grid grid-cols-2 gap-4">
    <div class="flex flex-col gap-2">
      <label class="text-white font-normal font-poppins ">Address</label>
      <input
        type="text"
        placeholder="Value"
        class="input-base"
        placeholder="Address"
      />
    </div>

    <div class="flex flex-col gap-2">
      <label class="text-white font-normal font-poppins ">Phone Number</label>
      <input
        type="tel"
        placeholder="Value"
        class="input-base"
        placeholder="Phone number"
      />
    </div>
  </div>

  <!-- Row 3: Password (1/2) + Confirm (1/2) -->
  <div class="grid grid-cols-2 gap-4">
    <div class="flex flex-col gap-2">
      <label class="text-white font-normal font-poppins ">Password</label>
      <input
        type="password"
        placeholder="Value"
        class="input-base"
        placeholder="Password"
      />
    </div>

    <div class="flex flex-col gap-2">
      <label class="text-white font-normal font-poppins ">Confirm Password</label>
      <input
        type="password"
        placeholder="Value"
        class="input-base"
        placeholder="Confirm Password"
      />
    </div>
  </div>

  <!-- Register button -->
  <div class="pt-4">
    <button
      type="submit"
      class="w-full bg-app-accent text-black font-normal font-poppins py-3 rounded-full hover:bg-app-accent-dark transition"
    >
      Register
    </button>
  </div>
</form>


    </div>
  </main>
</div>

`})
export class UserRegistrationComponent {
  // Component logic goes here
}

        

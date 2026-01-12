import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileInfoCard } from "../../shared/components/profile-info-card";
import { ChangePasswordModal } from "../../shared/components/profile-change-pswd-modal";
import { User } from '../../shared/models/user';

@Component({
  selector: 'registered-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ProfileInfoCard, ChangePasswordModal],
  template: `
    <div class="min-h-screen bg-white">
      <div class="flex flex-col min-h-screen">
        <div class="flex flex-1">
          <!-- Sidebar -->
          <!-- <app-sidebar></app-sidebar> -->

          <!-- Main Content -->
          <main class="flex flex-1 p-8">
            <div class="flex flex-col gap-6 w-full">

              <!-- Info Card -->
              <profile-info-card
                [user]="user"
                (save)="saveProfile($event)"
                (openPswdChange)="openPswdChange()"> </profile-info-card>

            </div>
          </main>
        </div>

        <!-- Change Password Modal -->
        <change-password-modal
          [isChangePasswordOpen]="isChangePasswordOpen"
          (close)="closePswdChange()"> </change-password-modal>
    </div>
  `,
})
export class RegisteredProfileComponent {
  user: User = {
    id: '1',
    firstName: 'Andrew',
    lastName: 'Wilson',
    email: 'andrewwilson@email.com',
    address: 'Novi Sad',
    phoneNumber: '+381 65 123 1233'
  }

  isChangePasswordOpen: boolean = false;

  closePswdChange(): void {
    this.isChangePasswordOpen = false;
  }

  openPswdChange(): void {
    this.isChangePasswordOpen = true;
  }

  saveProfile(updated: User): void {
    console.log(updated);
  }
  
}

import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileInfoCard } from "../../shared/components/profile-info-card";
import { ChangePasswordModal } from "../../shared/components/profile-change-pswd-modal";
import { User } from '../../../core/models/user';
import { Subscription, tap } from 'rxjs';
import { UserService } from '../../../core/services/user.service';
import { error, log } from 'console';
import { SuccessAlert } from "../../shared/components/success-alert";

@Component({
  selector: 'registered-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ProfileInfoCard, ChangePasswordModal, SuccessAlert],
  template: `
    <div class="min-h-screen bg-white">
      <div class="flex flex-col min-h-screen">
        <div class="flex flex-1">

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
          (close)="closePswdChange($event)"> </change-password-modal>

        <success-alert
          [isOpen]="isSuccessOpen"
          [message]="successMessage"
          [title]="successTitle"
          (close)="closeSuccessModal()"> </success-alert>
    </div>
  `,
})
export class RegisteredProfileComponent {
  user: User = {
    id: '',
    firstName: '',
    lastName: '',
    email: '',
    address: '',
    phoneNumber: '',
    role: 'PASSENGER'
  };

  private sub?: Subscription;
  isChangePasswordOpen: boolean = false;
  isSuccessOpen: boolean = false;
  successTitle: string = "Success";
  successMessage: string = "Profile successfully updated!";

  constructor(private userService: UserService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.sub = this.userService.currentUser$.subscribe(current => {
      if (current) {
        this.user = { ...current };
        this.cdr.detectChanges();
      }
    });

    this.userService.fetchMe().subscribe();
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }


  closePswdChange(updated: boolean): void {
    if (updated) {
      this.successMessage = "Password successfully updated."
      this.isSuccessOpen = true;
    }
    this.isChangePasswordOpen = false;
  }

  closeSuccessModal(): void {
    this.isSuccessOpen = false;
  }

  openPswdChange(): void {
    this.isChangePasswordOpen = true;
  }

  saveProfile(updated: User): void {
    this.userService.updateUser(updated).subscribe(
      {
        next: (user) => {
          this.successMessage = "Profile successfully updated."
          this.isSuccessOpen = true; 
          this.cdr.detectChanges()
        },
        error: err => {
          console.log(err["message"])
        }
      }
    )
  }
  
}



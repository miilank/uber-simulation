import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileInfoCard } from "../../shared/components/profile-info-card";
import { ChangePasswordModal } from "../../shared/components/profile-change-pswd-modal";
import { User } from '../../../core/models/user';
import { Subscription, tap } from 'rxjs';
import { CurrentUserService } from '../../../core/services/current-user.service';
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
              @if (user.blocked) {
              <div class="border-[1.5px] bg-red-50 border-red-200 rounded-3xl shadow-lg p-8 flex flex-col gap-6">
                <h3 class="text-[22px] font-normal font-poppins text-red-500 leading-8.25"> Your account has been blocked. </h3>
                <div class="text-[20px] text-red-400"> {{user.blockReason}} </div>
              </div>
             }

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
    id: 0,
    firstName: '',
    lastName: '',
    email: '',
    address: '',
    phoneNumber: '',
    role: 'PASSENGER',
    profilePicture: 'defaultprofile.png',
    blocked: false,
    blockReason: ''
  };

  private sub?: Subscription;
  isChangePasswordOpen: boolean = false;
  isSuccessOpen: boolean = false;
  successTitle: string = "Success";
  successMessage: string = "Profile successfully updated!";

  constructor(private userService: CurrentUserService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.sub = this.userService.currentUser$.subscribe(current => {
      if (current) {
        this.user = { ...current };        
        this.cdr.detectChanges();
        console.log(current);
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

    saveProfile(updated: {user: User, picture: File | null}): void {
      const form: FormData = new FormData();

      const jsonBlob: Blob  = new Blob([JSON.stringify(updated.user)], { type: 'application/json' });
      form.append('update', jsonBlob);
          
      if (updated.picture) {
        form.append('avatar', updated.picture, updated.picture.name);
      }

    this.userService.updateUser(form).subscribe(
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



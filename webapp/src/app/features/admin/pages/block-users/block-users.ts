import { Component, signal } from '@angular/core';
import { UserSearch } from "../../components/user-search/user-search";
import { User } from '../../../../core/models/user';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../shared/services/users.service';
import { ErrorAlert } from "../../../shared/components/error-alert";
import { SuccessAlert } from "../../../shared/components/success-alert";

@Component({
  selector: 'app-block-users',
  standalone: true,
  imports: [UserSearch, FormsModule, ErrorAlert, SuccessAlert],
  templateUrl: './block-users.html',
})
export class BlockUsersComponent {
  selectedUser = signal<User | null>(null);
  blockReason = signal<string>('');

  errorMessage: string = 'Failed to block user.';
  errorTitle: string = 'Error';
  isErrorOpen = signal<boolean>(false);

  successMessage: string = "Successfully blocked user.";
  successTitle: string = "Success";
  isSuccessOpen = signal<boolean>(false);

  updatedSelectedUser = signal<User | null>(null);

  constructor(private userService: UserService) {}

  blockUser() : void {
    if(!this.blockReason()) {
      this.errorMessage = "Please write the reason for blocking this user.";
      this.isErrorOpen.set(true);
      return;
    }

    if(!this.selectedUser()) {
      this.errorMessage = "Please select a user.";
      this.isErrorOpen.set(true);
      return;
    }

    this.userService.blockUser(this.selectedUser()!.id, this.blockReason()).subscribe({
      next: () => {
        this.selectedUser.update(u => u ? { ...u, blocked: true } : u);
        this.successMessage = "Successfully blocked user.";
        this.isSuccessOpen.set(true);

      },
      error: err => {
          this.errorMessage = err.error?.message || "Failed to block user.";
          this.isErrorOpen.set(true);
      }
    })
  }

  unblockUser() : void {
        this.userService.unblockUser(this.selectedUser()!.id).subscribe({
      next: () => {
        this.selectedUser.update(u => u ? { ...u, blocked: false } : u);
          this.successMessage = "Successfully unblocked user.";
          this.isSuccessOpen.set(true);
      },
      error: err => {
          this.errorMessage = err.error?.message || "Failed to unblock user.";
          this.isErrorOpen.set(true);
      }
    })
  }

  closeErrorAlert() {
    this.isErrorOpen.set(false);
  }

  closeSuccessAlert() {
    this.isSuccessOpen.set(false);
  }
}

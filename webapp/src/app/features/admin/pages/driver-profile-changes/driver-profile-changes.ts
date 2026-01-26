import { Component, inject, OnInit, signal } from '@angular/core';
import { User } from '../../../../core/models/user';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DriverService, DriverUpdateDTO } from '../../../shared/services/driver.service';
import { ErrorAlert } from "../../../shared/components/error-alert";
import { SuccessAlert } from "../../../shared/components/success-alert";

@Component({
  selector: 'app-driver-profile-changes',
  imports: [CommonModule, FormsModule, ErrorAlert, SuccessAlert],
  templateUrl: './driver-profile-changes.html',
})
export class DriverProfileChanges implements OnInit {
  driverService = inject(DriverService);

  updateRequests = signal<DriverUpdateDTO[]>([]);

  // ---- Alerts ----
  isErrorOpen = signal<boolean>(false);
  errorTitle: string = "Error";
  errorMessage: string = "Error occured.";

  isSuccessOpen = signal<boolean>(false);
  successTitle: string = "Success";
  successMessage: string = "Success!"
  // ----------------

  ngOnInit() {
    this.driverService.getPendingUpdates().subscribe({
      next: (updates) => {
        this.updateRequests.set(updates);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Fetching requests failed. Please try again later.';
        this.isErrorOpen.set(true);
      } 
    })
  }

  onApprove(requestId: number, index: number){
    this.driverService.approveUpdate(requestId).subscribe({
      next: () => {
        this.updateRequests().splice(index, 1);
        this.successMessage = "Request successfully approved."
        this.isSuccessOpen.set(true);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to approve the request. Please try again later.';
        this.isErrorOpen.set(true);
      } 
    })
  }

  onReject(requestId: number, index: number) {
      this.driverService.rejectUpdate(requestId).subscribe({
      next: () => {
        this.updateRequests().splice(index, 1);
        this.successMessage = "Request successfully rejected."
        this.isSuccessOpen.set(true);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to reject the request. Please try again later.';
        this.isErrorOpen.set(true);
      } 
    })
  }

  closeErrorAlert(): void {
    this.isErrorOpen.set(false);
  }

  closeSuccessAlert(): void {
    this.isSuccessOpen.set(false);
  }
}

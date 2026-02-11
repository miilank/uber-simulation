import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileInfoCard } from '../../../shared/components/profile-info-card';
import { Vehicle } from '../../../shared/models/vehicle';
import { ChangePasswordModal } from "../../../shared/components/profile-change-pswd-modal";
import { Subscription } from 'rxjs';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { Driver } from '../../../shared/models/driver';
import { DriverService } from '../../../shared/services/driver.service';
import { SuccessAlert } from "../../../shared/components/success-alert";
import { User } from '../../../../core/models/user';

@Component({
  selector: 'driver-profile',
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
                <h3 class="text-[22px] font-normal font-poppins text-red-500 leading-8.25"> Your account has been blocked and you are no longer able to work with us. </h3>
                <div class="text-[20px] text-red-400"> {{user.blockReason}} </div>
              </div>
            }

              <!-- Work Time Progress Card -->
              <div class="border-[1.5px] border-gray-200 rounded-3xl shadow-lg p-8 flex flex-col gap-6">
                <h3 class="text-[22px] font-normal font-poppins text-black leading-8.25"> Driver Activity</h3>

                <div class="w-full flex items-center justify-between">
                  <div class="text-sm text-gray-600">
                    Time worked today
                  </div>

                  <div class="text-[22px] font-normal font-poppins text-black leading-8.25">
                    {{ timeWorkedHours }}h {{ timeWorkedMinutesRemainder }}m
                  </div>
                </div>

                <!-- Progress bar container -->
                <div class="w-full">
                  <div
                    class="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                    <!-- filled bar -->
                    <div
                      [style.width.%]="progressPercent"
                      class="h-full rounded-full transition-all duration-500 bg-lime-400"
                    ></div>
                  </div>

                  <div class="flex justify-between text-xs text-gray-500 mt-2">
                    <span>0h</span>
                    <span>8h</span>
                  </div>

                </div>
              </div>

              <!-- Info Card -->
              <profile-info-card
                [user]="user"
                (save)="saveProfile($event)"
                (openPswdChange)="openPswdChange()"> </profile-info-card>

              <!-- Vehicle Info -->
              <div class="border-[1.5px] border-gray-200 rounded-3xl shadow-lg p-8 flex flex-col gap-11">
                <h3 class="text-[22px] font-normal font-poppins text-black leading-8.25">Vehicle Information</h3>

                <div class="flex flex-col gap-2.5">
                  <!-- Vehicle Model Type Row -->
                  <div class="flex gap-2.5 flex-col sm:flex-row">
                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Vehicle Model</label>

                        <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                          {{ user.vehicle.model }}
                        </p>
                    </div>

                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Vehicle Type</label>

                        <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                          {{ user.vehicle.type }}
                        </p>
                    </div>

                  </div>

                  <!-- License plates Seats Row -->
                  <div class="flex gap-2.5 flex-col sm:flex-row">
                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        License Plate</label>

                        <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                          {{ user.vehicle.licensePlate }}
                        </p>
                    </div>

                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Seats</label>

                        <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                          {{ user.vehicle.seatCount }}
                        </p>
                    </div>

                  </div>

                  <!-- Infant Pet Support -->
                  <div class="flex gap-2.5 flex-col sm:flex-row">
                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Infant Support</label>

                        <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                          {{ user.vehicle.babyFriendly ? 'True' : 'False' }}
                        </p>
                    </div>

                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Pet Support</label>

                        <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                          {{ user.vehicle.petsFriendly ? 'True' : 'False' }}
                        </p>
                    </div>

                  </div>

                </div>
              </div>

              <change-password-modal
                [isChangePasswordOpen]="isChangePasswordOpen"
                (close)="closePswdChange($event)"> </change-password-modal>

              <success-alert
                [isOpen]="isSuccessOpen"
                [message]="successMessage"
                [title]="successTitle"
                (close)="closeSuccessModal()"> </success-alert>
            </div>

          </main>
        </div>


      </div>
    </div>
  `,
})
export class DriverProfileComponent {
  vehicle: Vehicle = {
    id: 0,
    model: '',
    type: '',
    licensePlate: '',
    seatCount: 0,
    babyFriendly: false,
    petsFriendly: false
  }

  user: Driver = {
    role: 'DRIVER',
    vehicle: this.vehicle,
    available: false,
    active: false,
    workedMinutesLast24h: 0,
    rides: [],
    ratings: [],
    averageRating: 0,
    id: 0,
    firstName: '',
    lastName: '',
    email: '',
    address: '',
    phoneNumber: '',
    profilePicture: "defaultprofile.png",
    blocked: false,
    blockReason: ''
  }

  private sub?: Subscription;
  isChangePasswordOpen: boolean = false;
  isSuccessOpen: boolean = false;
  successTitle: string = "Success";
  successMessage: string = "Profile successfully updated!";

  constructor(private userService: CurrentUserService,
              private cdr: ChangeDetectorRef,
              private driverService: DriverService) {}

  readonly MAX_WORK_MINUTES = 8 * 60;

  ngOnInit(): void {
    this.sub = this.userService.currentUser$.subscribe(current => {
      if (current) {
        this.user = { ...current as Driver };
        this.cdr.detectChanges();
      }
    });

    this.userService.fetchMe().subscribe();
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  get progressPercent(): number {
    const pct = (this.user.workedMinutesLast24h / this.MAX_WORK_MINUTES) * 100;
    return Math.min(100, Math.max(0, Math.round(pct)));
  }

  get timeWorkedHours(): number {
    return Math.floor(Math.min(this.user.workedMinutesLast24h, this.MAX_WORK_MINUTES) / 60);
  }
  get timeWorkedMinutesRemainder(): number {
    return Math.floor(Math.min(this.user.workedMinutesLast24h, this.MAX_WORK_MINUTES) % 60);
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


      this.driverService.updateDriver(form).subscribe(
      {
        next: (user) => {
          this.successMessage = "Profile update request sent. Changes will be visible once the request is approved."
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

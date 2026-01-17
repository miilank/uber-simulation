import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileInfoCard } from '../../../shared/components/profile-info-card';
import { User } from '../../../../core/models/user';
import { Vehicle } from '../../../../core/models/vehicle';
import { ChangePasswordModal } from "../../../shared/components/profile-change-pswd-modal";

@Component({
  selector: 'driver-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ProfileInfoCard, ChangePasswordModal],
  template: `
    <div class="min-h-screen bg-white">
      <div class="flex flex-col min-h-screen">
        <div class="flex flex-1">
          <!-- Main Content -->
          <main class="flex flex-1 p-8">
            <div class="flex flex-col gap-6 w-full">

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
                        {{ vehicleModel }}
                      </p>
                    </div>

                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Vehicle Type</label>

                      <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                        {{ vehicle.type }}
                      </p>
                    </div>

                  </div>

                  <!-- License plates Seats Row -->
                  <div class="flex gap-2.5 flex-col sm:flex-row">
                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        License Plate</label>

                      <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                        {{ vehicle.licensePlate }}
                      </p>
                    </div>

                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Seats</label>

                      <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                        {{ vehicle.seatCount }}
                      </p>
                    </div>

                  </div>

                  <!-- Infant Pet Support -->
                  <div class="flex gap-2.5 flex-col sm:flex-row">
                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Infant Support</label>

                      <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                        {{ vehicle.babyFriendly ? 'True' : 'False' }}
                      </p>
                    </div>

                    <div class="flex-1 flex flex-col">
                      <label class="text-sm font-normal font-poppins flex items-center text-gray-700">
                        Pet Support</label>

                      <p class="text-[20px] font-normal font-poppins text-black leading-8.25">
                        {{ vehicle.petsFriendly ? 'True' : 'False' }}
                      </p>
                    </div>

                  </div>

                </div>
              </div>

              <change-password-modal
                [isChangePasswordOpen]="isChangePasswordOpen"
                (close)="closePswdChange()"> </change-password-modal>
            </div>

          </main>
        </div>


      </div>
    </div>
  `,
})
export class DriverProfileComponent {
  vehicleModel: string = "Model"

  user: User = {
    id: '1',
    firstName: 'Andrew',
    lastName: 'Wilson',
    email: 'andrewwilson@email.com',
    address: 'Novi Sad',
    phoneNumber: '+381 65 123 1233',
    role: "ADMIN"
  }

  vehicle: Vehicle = {
    id: 0,
    model: 'Model',
    type: 'Type',
    licensePlate: 'DSDSDS-111',
    seatCount: 10,
    babyFriendly: true,
    petsFriendly: false
  }

  isChangePasswordOpen: boolean = false;

  timeWorkedMinutes: number = 150;

  readonly MAX_WORK_MINUTES = 8 * 60;

  get progressPercent(): number {
    const pct = (this.timeWorkedMinutes / this.MAX_WORK_MINUTES) * 100;
    return Math.min(100, Math.max(0, Math.round(pct)));
  }

  get timeWorkedHours(): number {
    return Math.floor(Math.min(this.timeWorkedMinutes, this.MAX_WORK_MINUTES) / 60);
  }
  get timeWorkedMinutesRemainder(): number {
    return Math.floor(Math.min(this.timeWorkedMinutes, this.MAX_WORK_MINUTES) % 60);
  }

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

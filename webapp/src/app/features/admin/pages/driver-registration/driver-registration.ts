import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VehicleType, vehicleTypeKeyFromValue } from '../../../shared/models/vehicle';
import { CommonModule } from '@angular/common';
import { DriverCreationDTO, DriverService, VehicleCreationDTO } from '../../../shared/services/driver.service';
import { ProfilePictureComponent } from "../../../shared/components/profile-picture.component";

@Component({
  selector: 'app-driver-registration',
  imports: [FormsModule, CommonModule, ProfilePictureComponent],
  templateUrl: './driver-registration.html',
})
export class DriverRegistration {
  firstName = '';
  lastName = '';
  email = '';
  address = '';
  phone = '';
  password = '';
  confirmPassword = '';

  vehicleTypes: string[] = [];

  pets: boolean = false;
  infants: boolean = false;
  vehicleType: string = '';
  vehicleModel: string = '';
  licensePlate: string = '';
  passengers: number = 0;

  registrationSuccess = false;
  registrationError: string | null = null;
  isSubmitting = false;

  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  selectedPicture: File | null = null;

  constructor(public driverService : DriverService) {
  }

  ngOnInit() {
      this.vehicleTypes = Object.values(VehicleType);
    }

  onSubmit() {
    this.isSubmitting = true;
    console.log("Submitted");
    
    let vehicle: VehicleCreationDTO = {
      model: this.vehicleModel,
      type: vehicleTypeKeyFromValue(this.vehicleType),
      licensePlate: this.licensePlate,
      seatCount: this.passengers,
      babyFriendly: this.infants,
      petsFriendly: this.pets
    }

    let req: DriverCreationDTO = {
      email: this.email,
      firstName: this.firstName,
      lastName: this.lastName,
      address: this.address,
      phoneNumber: this.phone,
      vehicle: vehicle
    }

    const form: FormData = new FormData();

    const jsonBlob: Blob  = new Blob([JSON.stringify(req)], { type: 'application/json' });
    form.append('user', jsonBlob);
        
    if (this.selectedPicture) {
      form.append('avatar', this.selectedPicture, this.selectedPicture.name);
    }
    
    this.driverService.createDriver(form).subscribe({
          next: () => {
            this.isSubmitting = false;
            this.registrationSuccess = true;
            this.cdr.detectChanges();
            console.log('Signup request sent successfully!');
          },
          error: (err) => {
            this.isSubmitting = false;
            this.registrationError = err.error?.message || 'Registration failed. Please try again.';
            this.cdr.detectChanges();
            console.error('Signup failed', err);
          }}
        )
  }

  setPicture(file: File) {
    this.selectedPicture = file;
  }

  closeSuccessPopup() {
    this.registrationSuccess = false;
    this.router.navigate(['/admin/dashboard']);
  }
}

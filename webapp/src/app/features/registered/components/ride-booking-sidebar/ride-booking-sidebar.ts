import { Component, inject, signal } from '@angular/core';
import { LocationSearchInput } from "../../../shared/components/location-search-input/location-search-input";
import { NominatimResult } from '../../../shared/services/nominatim.service';
import { RideBookingStateService } from '../../../../core/services/ride-booking-state.service';
import { NgOptimizedImage, Location, CommonModule } from '@angular/common';
import { UserService } from '../../../../core/services/user.service';
import { User } from '../../../../core/models/user';
import { FormsModule } from '@angular/forms';
import { VehicleType } from '../../../shared/models/vehicle';
import { RideOrderService } from '../../services/ride-order.service';
import { LocationDTO, nominatimToLocation } from '../../../shared/models/location';
import { request } from 'http';

@Component({
  selector: 'ride-booking-sidebar',
  imports: [LocationSearchInput, NgOptimizedImage, CommonModule, FormsModule],
  templateUrl: './ride-booking-sidebar.html',
})
export class RideBookingSidebar {
  bookingState = inject(RideBookingStateService);
  location = inject(Location);
  userService = inject(UserService);
  rideOrderService = inject(RideOrderService);

  vehicleTypes: string[] = [];

  pets: boolean = false;
  infants: boolean = false;
  vehicleType: string = 'any';


  step = signal<number>(1);
  user = signal<User>({
    id: '',
    firstName: '',
    lastName: '',
    email: '',
    address: '',
    phoneNumber: '',
    role: 'ADMIN'
  });

  ngOnInit() {
    this.userService.currentUser$.subscribe(
      {
        next: user => (user !== null ? this.user.set(user) : '')
      }
    )
    this.vehicleTypes = Object.values(VehicleType);
  }

  onPickupSelected(res: NominatimResult) {
    this.bookingState.setPickup(res);
  }

  onDropoffSelected(res: NominatimResult) {
    this.bookingState.setDropoff(res);
  }

  addStop() {
    this.bookingState.addStop();
  }

  addPassenger() {}

  onStopSelected(index: number, res: NominatimResult) {
    this.bookingState.setStop(index, res);
  }

  removeStop(index: number) {
    this.bookingState.removeStop(index);
  }

  nextStep() {
    if (this.step() >= 3) return;
    this.step.set(this.step() + 1);
  }

  previousStep() {
    if (this.step() <= 1) {
      console.log(this.location.getState());
      
      this.location.back();
    }
    this.step.set(this.step() - 1);
  }

  bookRide() {
    const requestVehicleType: VehicleType | undefined =
      Object.values(VehicleType).includes(this.vehicleType as VehicleType)
      ? (this.vehicleType as VehicleType)
      : undefined;

    if(this.bookingState.pickup() == null) return;
    if(this.bookingState.dropoff() == null) return;

    let requestStops : LocationDTO[] = []

    this.bookingState.stops().forEach((res) => {
        if (res!==null) {
          requestStops.push(nominatimToLocation(res))
        };
      }
    )

    this.rideOrderService.requestRide(
      nominatimToLocation(this.bookingState.pickup()!),
      nominatimToLocation(this.bookingState.dropoff()!),
      requestVehicleType,
      requestStops,
      this.infants, this.pets,
      [],
      new Date(),
      undefined   
    ).subscribe();
  }
}

import { ChangeDetectorRef, Component, inject, signal } from '@angular/core';
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
import { SuccessAlert } from "../../../shared/components/success-alert";
import { ErrorAlert } from "../../../shared/components/error-alert";
import { Router, RouterLink, UrlTree } from "@angular/router";
import { FavoriteRouteDTO, FavouriteRoutesService } from '../../services/favourite-routes.service';

@Component({
  selector: 'ride-booking-sidebar',
  imports: [LocationSearchInput, NgOptimizedImage, CommonModule, FormsModule, ErrorAlert, RouterLink, SuccessAlert],
  templateUrl: './ride-booking-sidebar.html',
})
export class RideBookingSidebar {
  bookingState = inject(RideBookingStateService);
  rideOrderService = inject(RideOrderService);
  userService = inject(UserService);
  favRoutesService = inject(FavouriteRoutesService);

  location = inject(Location);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);

  dashboardUrl: UrlTree = this.router.parseUrl('/user/dashboard');

  vehicleTypes: string[] = [];

  pets: boolean = false;
  infants: boolean = false;
  vehicleType: string = 'any';

  minDate!: string;
  maxDate!: string;
  scheduledDate = signal<string>('');

  passengerEmails = signal<string[]>([]);

  // ---- Alerts ----
  isSuccessOpen = signal<boolean>(false);
  successMessage: string = "Ride successfully booked!";

  isErrorOpen = signal<boolean>(false);
  errorTitle: string = "Error";
  errorMessage: string = "Error occured during ride booking.";

  isSuccessAlertOpen = signal<boolean>(false);
  successAlertTitle: string = "Success";
  // ----------------

  step = signal<number>(1);

  user = signal<User>({
    id: '',
    firstName: '',
    lastName: '',
    email: '',
    address: '',
    phoneNumber: '',
    role: 'PASSENGER'
  });

  // ---- Favorite routes ----
  favoriteRouteName: string = '';
  favoriteRoutes = signal<FavoriteRouteDTO[]>([]);
  selectedRoute: FavoriteRouteDTO | null = null;
  // -------------------------


  ngOnInit() {
    this.userService.currentUser$.subscribe(
      {
        next: user => (user !== null ? this.user.set(user) : '')
      }
    )

    this.favRoutesService.getFavoriteRoutes().subscribe({
      next: (routes => {
        this.favoriteRoutes.set(routes);
      }),
      error: (err => {
        this.errorMessage = err.error?.message || 'Fetching routes failed.';
        this.isErrorOpen.set(true);
      })
    }
    )

    this.vehicleTypes = Object.values(VehicleType);

    const now = new Date();
    this.scheduledDate.set(this.toDatetimeLocalValue(now));
    this.minDate = this.toDatetimeLocalValue(now);
    this.maxDate = this.toDatetimeLocalValue(new Date(now.getTime() + 5 * 60 * 60000));
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


  onStopSelected(index: number, res: NominatimResult) {
    this.bookingState.setStop(index, res);
    console.log(this.bookingState.stops());
    
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


  saveRoute() : void {
    const requestVehicleType: VehicleType | undefined =
      Object.values(VehicleType).includes(this.vehicleType as VehicleType)
      ? (this.vehicleType as VehicleType)
      : undefined;

    if (this.favoriteRouteName === '') {
      this.errorMessage="Route name cannot be empty.";
      this.isErrorOpen.set(true);
      return;
    }

    if (this.bookingState.pickup() == null || this.bookingState.dropoff()==null){
      this.errorMessage="Ride stops cannot be empty.";
      this.isErrorOpen.set(true);
      return;
    }
    let requestStops : LocationDTO[] = []

    for (const res of this.bookingState.stops()) {
      if (res === null) {
        this.errorMessage = 'Ride stops cannot be empty.';
        this.isErrorOpen.set(true);
        return;
      }
      else {
        requestStops.push(nominatimToLocation(res))
      };
    }

    this.favRoutesService.createFavoriteRoute(
      this.favoriteRouteName,
      nominatimToLocation(this.bookingState.pickup()!),
      nominatimToLocation(this.bookingState.dropoff()!),
      requestStops,
      requestVehicleType,
      this.infants, this.pets
    ).subscribe({
      next: (routes => {
        this.successMessage="Route successfully saved!";
        this.isSuccessAlertOpen.set(true);
      }),
      error: (err => {
        this.errorMessage = err.error?.message || 'Saving route failed. Please try again later';
        this.isErrorOpen.set(true);
      })
    })

  }


  bookRide() : void {
    const requestVehicleType: VehicleType | undefined =
      Object.values(VehicleType).includes(this.vehicleType as VehicleType)
      ? (this.vehicleType as VehicleType)
      : undefined;

    if(this.bookingState.pickup() == null || this.bookingState.dropoff()==null){
      this.errorMessage="Ride stops cannot be empty.";
      this.isErrorOpen.set(true);
      return;
    }

    let requestStops : LocationDTO[] = []

    for (const res of this.bookingState.stops()) {
      if (res === null) {
        this.errorMessage = 'Ride stops cannot be empty.';
        this.isErrorOpen.set(true);
        return;
      }
      else {
        requestStops.push(nominatimToLocation(res))
      };
    }

    if (this.passengerEmails().includes('')) {
      this.errorMessage="Passenger emails cannot be empty.";
      this.isErrorOpen.set(true);
      return;
    }

    this.rideOrderService.requestRide(
      nominatimToLocation(this.bookingState.pickup()!),
      nominatimToLocation(this.bookingState.dropoff()!),
      requestVehicleType,
      requestStops,
      this.infants, this.pets,
      this.passengerEmails(),
      this.scheduledDate(),
      this.bookingState.routeInfo()?.totalDistance!,
      (this.bookingState.routeInfo()?.totalDuration!/60),
      undefined   
    ).subscribe({
      next: (ride => {
        this.successMessage = `Ride successfully assigned to driver: ${ride.driverEmail}!
        Car is expected to arrive at ${new Date(ride.scheduledTime).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
      })}. Total price is: ${ride.totalPrice}.`;
        this.isSuccessOpen.set(true);
      }),
      error: (err => {
        this.errorMessage = err.error?.message || 'Booking failed. Please try again.';
        this.isErrorOpen.set(true);
      })
    });
  }


  setFromRoute(route: FavoriteRouteDTO) : void {    
    this.bookingState.setPickup({
      lat: route.startLocation.latitude.toString(),
      lon: route.startLocation.longitude.toString(),
      formattedText: route.startLocation.address,
    });

    this.bookingState.setDropoff({
      lat: route.endLocation.latitude.toString(),
      lon: route.endLocation.longitude.toString(),
      formattedText: route.endLocation.address,
    });

    this.bookingState.stops.set(
      route.waypoints.map(location => ({
        lat: location.latitude.toString(),
        lon: location.longitude.toString(),
        formattedText: location.address,
      }))
    );

    this.infants = route.babyFriendly;
    this.pets = route.petsFriendly;
    this.vehicleType = route.vehicleType ?? 'any';

    this.cdr.detectChanges();
  }


  isDateValid(date:string) : boolean {
    return date <= this.maxDate && date >= this.minDate;
  }

  addPassenger() {
    this.passengerEmails().push('');
  }

  removePassenger(index: number) {
    this.passengerEmails().splice(index, 1);
  }

  addEmail(e: FocusEvent, index: number) {
    const input: HTMLInputElement = e.target as HTMLInputElement;
    const value: string = input.value;

    this.passengerEmails()[index] = value
    console.log(this.passengerEmails())
  }

  closeErrorAlert(): void {
    this.isErrorOpen.set(false);
  }

  closeSuccessAlert(): void {
    this.isSuccessAlertOpen.set(false);
  }

  private toDatetimeLocalValue(d: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
}

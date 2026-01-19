import { Component } from '@angular/core';
import { LocationSearchInput } from "../../../shared/components/location-search-input/location-search-input";

@Component({
  selector: 'ride-booking-sidebar',
  imports: [LocationSearchInput],
  templateUrl: './ride-booking-sidebar.html',
})
export class RideBookingSidebar {
  pickup: String = '';
  dropoff: String = '';
}

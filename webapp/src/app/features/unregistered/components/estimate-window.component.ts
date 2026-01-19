import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { EstimateResultsComponent } from './estimate-results.component';
import { Router } from '@angular/router';
import { LocationSearchInput } from '../../shared/components/location-search-input/location-search-input';
import { NominatimResult } from '../../shared/services/nominatim.service';

@Component({
  selector: 'app-estimate-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, EstimateResultsComponent, LocationSearchInput],
  template: `
  @if(isRoot && !showResults){
  <div class="font-sans fixed bottom-6 left-6 z-1000 w-80 bg-white rounded-3xl shadow-[0px_13px_27px_0px_rgba(0,0,0,0.25)] items-center gap-4 p-6 max-w-sm md:bottom-8 md:left-8 md:w-96 animate-slideUp"[class.animate-slideDown]="isClosing">
        <h3 class="text-3xl text-center font-semibold text-black md:text-3xl lg:text-3xl mb-4 md:mb-4">Go anywhere</h3>
        <form #f = "ngForm" (ngSubmit)="onSubmit(f)" novalidate class="space-y-3">
            <location-search-input
              [hintMessage]="'Pickup location'"
              [inputClass]="'input-estimate w-full'"
              [containerClass]="'my-3'"
              (selected)="onPickupSelected($event)">
            </location-search-input>

            <location-search-input
              [hintMessage]="'Dropoff location'"
              [inputClass]="'input-estimate w-full'"
              [containerClass]="'my-3'"
              (selected)="onDropoffSelected($event)">
            </location-search-input>
            <button 
            type = "submit"
            class="w-full bg-app-accent hover:bg-app-accent-dark text-app-dark px-6 py-3 rounded-3xl font-semibold transition-colors shadow-[0px_7px_27px_0px_rgba(0,0,0,0.25)] font-[Poppins] text-base">
            Get estimate
            </button>
</form>
</div>
}
@if (showResults){
<app-estimate-results
  [pickup]="pickupLocation"
  [dropoff]="dropoffLocation"
  [estimateRange]="estimateRange"
  [distance]="distance"
  (vehicleTypeChanged)="onVehicleTypeChange($event)"
  (backToMap)="onClose()"
  (bookRide)="onBookRide()"></app-estimate-results>
}
`,
styles: [`
    @keyframes slideUp {
    from { transform: translateY(100%); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
  }
  @keyframes slideDown {
    from { transform: translateY(0); opacity: 1; }
    to { transform: translateY(150%); opacity: 0; }
  }
  .animate-slideUp {
    animation: slideUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  }
  .animate-slideDown {
    animation: slideDown 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  }
  `]
})
export class EstimatePanelComponent {
  showResults = false;
  isClosing = false;
  pickupLocation = '';
  dropoffLocation = '';
  estimateRange = '€10-€13';
  distance = '3.1 km';

  pickupResult?: NominatimResult;
  dropoffResult?: NominatimResult;
  
  @ViewChild('f') estimateForm!: NgForm;
  constructor(private router: Router) {}
  onPickupSelected(res: NominatimResult) {
    this.pickupResult = res;
    this.pickupLocation = res.formattedText;
  }

  onDropoffSelected(res: NominatimResult) {
    this.dropoffResult = res;
    this.dropoffLocation = res.formattedText;
  }
  onSubmit(form: NgForm) {
  if (!this.pickupResult || !this.dropoffResult) {
      return;
    }

    this.isClosing = true;
    this.showResults = true;
  }
  get isRoot(): boolean {
    const url = this.router.url.split('?')[0].split('#')[0];
    return url === '/' || url === '';
  }
  onClose() {
    this.showResults = false;
    this.isClosing = false;
    this.estimateForm?.resetForm();
  }
    onBookRide() {
    console.log('Booking ride from', this.pickupLocation, 'to', this.dropoffLocation);
    // TODO: Implement ride booking logic
  }

  onVehicleTypeChange(vehicleType: string) {
  console.log('Vehicle type changed to:', vehicleType);
  // TODO: Recalculate price based on vehicle type
}

}
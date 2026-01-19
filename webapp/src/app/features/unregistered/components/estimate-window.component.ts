import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { EstimateResultsComponent } from './estimate-results.component';
import { delay } from 'rxjs';

@Component({
  selector: 'app-estimate-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, EstimateResultsComponent],
  template: `
  @if(!showResults){
  <!-- <div class="font-sans fixed bottom-6 left-6 z-[1000] w-80 bg-white rounded-3xl shadow-[0px_13px_27px_0px_rgba(0,0,0,0.25)] items-center gap-4 p-6 max-w-sm md:bottom-8 md:left-8 md:w-96 animate-slideUp"[class.animate-slideDown]="isClosing">
        <h3 class="text-3xl text-center font-semibold text-black md:text-3xl lg:text-3xl font-semibold mb-4 md:mb-4">Go anywhere</h3>
        <form #f = "ngForm" (ngSubmit)="onSubmit(f)" novalidate class="space-y-3">
            <input
            #pickup="ngModel"
            name="pickup"
            ngModel
            required
            placeholder="Pickup location" 
            type="text" class="input-estimate w-full"
            [class.!border-red-500]="f.submitted && pickup.invalid">
            <input #dropoff="ngModel"
            name="dropoff"
            ngModel 
            required
            placeholder="Dropoff route"
            type="text"
            class="input-estimate w-full"
            [class.!border-red-500]="f.submitted && dropoff.invalid">
            <button 
            type = "submit"
            class="w-full bg-[var(--color-app-accent)] hover:bg-[#A9D53A] text-app-dark px-6 py-3 rounded-3xl font-semibold transition-colors shadow-[0px_7px_27px_0px_rgba(0,0,0,0.25)] font-[Poppins] text-base">
            Get estimate
            </button>
</form>
</div> -->
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
  @ViewChild('f') estimateForm!: NgForm;
  onSubmit(form: NgForm) {
 if (form.valid) {
      this.pickupLocation = form.value.pickup;
      this.dropoffLocation = form.value.dropoff;
      this.isClosing = true;
      this.showResults = true;
    }
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
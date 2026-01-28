import { ChangeDetectorRef, Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-estimate-results',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed inset-0 z-1001 bg-black/50 flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl shadow-[0px_13px_27px_0px_rgba(0,0,0,0.25)] inline-flex flex-col justify-end items-center gap-4 w-1/3 h-fit animate-slideUp">
        <p class="self-stretch text-center justify-start text-black-900 text-3xl font-semibold font-['Poppins'] leading-9 pt-5">Go anywhere</p>
        
        <div class="self-stretch inline-flex justify-center items-start gap-4 px-5">
          <input type="text" 
                [value]="pickup" 
                readonly 
                class="flex-1 h-12 min-w-32 px-4 py-3 bg-neutral-100 rounded-3xl outline outline-offset-[-0.52px] outline-stone-300 text-neutral-900 text-base font-normal font-['Poppins'] leading-4 cursor-not-allowed focus:outline-none">
          
          <input type="text" 
                [value]="dropoff" 
                readonly 
                class="flex-1 h-12 min-w-32 px-4 py-3 bg-neutral-100 rounded-3xl outline outline-offset-[-0.52px] outline-stone-300 text-neutral-900 text-base font-normal font-['Poppins'] leading-4 cursor-not-allowed focus:outline-none">
        </div>

        
                <div data-layer="Connected button group" class="ConnectedButtonGroup self-stretch h-12 rounded-2xl inline-flex justify-start items-center gap-0.5 overflow-hidden px-5">
          <button 
            (click)="selectVehicleType('standard')"
            [ngClass]="{'bg-teal-700': selectedVehicleType === 'standard'}"
            class="flex-1 min-w-12 bg-gray-100 rounded-tr rounded-tl-2xl rounded-br rounded-bl-2xl inline-flex flex-col justify-center items-center gap-0.5 overflow-hidden transition-colors cursor-pointer">
            <div class="self-stretch min-w-12 px-3 py-1.5 inline-flex justify-center items-center gap-1 overflow-hidden">
              <div [ngClass]="{'text-app-accent': selectedVehicleType === 'standard', 'text-stone-900': selectedVehicleType !== 'standard'}" 
                   class="justify-center text-sm font-medium font-['Poppins'] leading-5 tracking-tight transition-colors">
                Standard
              </div>
            </div>
          </button>
          
          <button 
            (click)="selectVehicleType('luxury')"
            [ngClass]="{'bg-teal-700': selectedVehicleType === 'luxury'}"
            class="flex-1 min-w-12 bg-gray-100 rounded inline-flex flex-col justify-center items-center gap-0.5 overflow-hidden transition-colors cursor-pointer">
            <div class="self-stretch px-3 py-1.5 inline-flex justify-center items-center gap-1 overflow-hidden">
              <div [ngClass]="{'text-app-accent': selectedVehicleType === 'luxury', 'text-stone-900': selectedVehicleType !== 'luxury'}" 
                   class="justify-center text-sm font-medium font-['Poppins'] leading-5 tracking-tight transition-colors">
                Luxury
              </div>
            </div>
          </button>
          
          <button 
            (click)="selectVehicleType('van')"
            [ngClass]="{'bg-teal-700': selectedVehicleType === 'van'}"
            class="flex-1 min-w-12 bg-gray-100 rounded-tl rounded-tr-2xl rounded-bl rounded-br-2xl inline-flex flex-col justify-center items-center gap-0.5 overflow-hidden transition-colors cursor-pointer">
            <div class="self-stretch px-3 py-1.5 inline-flex justify-center items-center gap-1 overflow-hidden">
              <div [ngClass]="{'text-app-accent': selectedVehicleType === 'van', 'text-stone-900': selectedVehicleType !== 'van'}" 
                   class="justify-center text-sm font-medium font-['Poppins'] leading-5 tracking-tight transition-colors">
                Van
              </div>
            </div>
          </button>
        </div>
        
        <div class="self-stretch h-32 bg-[radial-gradient(ellipse_100.12%_81.15%_at_50.02%_100.18%,rgba(255,255,255,0)_0%,var(--Primary-Accent,rgba(192,236,78,0.50))_100%)] rounded-3xl shadow-[0px_5px_27px_0px_rgba(0,0,0,0.25)] flex flex-col justify-center items-center gap-4 px-5 mx-5">
          <div data-layer="Estimate price" class="EstimatePrice text-center justify-start text-black text-base font-normal font-['Poppins'] leading-5">Estimate price</div>
          <div data-layer="€10 - €13" class="1013 self-stretch text-center justify-start text-neutral-900 text-4xl font-semibold font-['Poppins'] leading-12">{{ estimateRange }}</div>
          <div data-layer="Frame 19" class="Frame19 inline-flex justify-center items-start gap-4">
            <div data-layer="ETA 12:54" class="Eta1254 text-center justify-start text-black text-base font-normal font-['Poppins'] leading-5">{{ arrivalTime }}</div>
            <div data-layer="3.1 km" class="1Km text-center justify-start text-black text-base font-normal font-['Poppins'] leading-5">{{ distance }}</div>
          </div>
        </div>
        
        <div data-layer="Frame 16" class="Frame16 self-stretch inline-flex justify-start items-start gap-4 mx-5 my-5">
          <button (click)="onMapView()" 
                  data-layer="Button" 
                  class="Button flex-1 h-12 p-3 bg-Background rounded-3xl shadow-[0px_4px_4px_0px_rgba(0,0,0,0.25)] outline -outline-offset-1 outline-black flex justify-center items-center gap-2 overflow-hidden hover:bg-neutral-50 transition-colors cursor-pointer">
            <div data-layer="Calculate" class="Calculate justify-start text-stone-900 text-base font-normal font-['Poppins'] leading-4">Back to Map</div>
          </button>
          <button (click)="onBookRide()" 
                  data-layer="Button" 
                  class="Button flex-1 h-12 p-3 bg-app-accent rounded-3xl shadow-[0px_4px_4px_0px_rgba(0,0,0,0.25)] flex justify-center items-center gap-2 overflow-hidden hover:opacity-90 transition-opacity cursor-pointer">
            <div data-layer="Calculate" class="Calculate justify-start text-stone-900 text-base font-normal font-['Poppins'] leading-4">Book a ride</div>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @keyframes slideUp {
      from { transform: translateY(100%); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    .animate-slideUp {
      animation: slideUp 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    }
  `]
})
export class EstimateResultsComponent {
  @Input() pickup = 'negde';
  @Input() dropoff = 'drugde';
  @Input() estimateRange = '€10-€13';
  @Input() distance = '10-13 km';
  @Input() arrivalTime = '10-15 min';

  selectedVehicleType: 'standard' | 'luxury' | 'van' = 'standard';
  
  @Output() backToMap = new EventEmitter<void>();
  @Output() bookRide = new EventEmitter<void>();
  @Output() vehicleTypeChanged = new EventEmitter<string>();

  constructor(
    private cdr: ChangeDetectorRef
  ) {}
  ngOnChanges(changes: SimpleChanges) {
    if (changes['estimateRange']) {
      this.cdr.detectChanges();
    }
  }

  selectVehicleType(type: 'standard' | 'luxury' | 'van') {
    this.selectedVehicleType = type;
    this.vehicleTypeChanged.emit(type);
    this.cdr.detectChanges();
  }
  

  onMapView() {
    console.log('Map');
    this.backToMap.emit();
  }
  
  onBookRide() {
    console.log('Book');
    this.bookRide.emit();
  }
}

import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-estimate-results',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed inset-0 z-[1001] bg-black/50 flex items-center justify-center p-4 animate-slideUp">
      <div class="bg-white rounded-3xl shadow-[0px_13px_27px_0px_rgba(0,0,0,0.25)] inline-flex flex-col justify-end items-center gap-4 w-1/3 h-fit">
        <p class="self-stretch text-center justify-start text-black-900 text-3xl font-semibold font-['Poppins'] leading-9 pt-5">Go anywhere
          <div class="self-stretch inline-flex justify-center items-start gap-4 px-5">
            <div class="flex-1 h-12 flex justify-start items-start">
              <div class="flex-1 h-12 min-w-32 px-4 py-3 bg-neutral-100 rounded-3xl outline outline-1 outline-offset-[-0.52px] outline-stone-300 flex justify-start items-center gap-2 overflow-hidden">
                <div class="flex-1 justify-start text-neutral-900 text-base font-normal font-['Poppins'] leading-4">{{pickup}}</div>
              </div>
            </div>
            <div class="flex-1 h-12 flex justify-start items-start">
              <div class="flex-1 h-12 min-w-32 px-4 py-3 bg-neutral-100 rounded-3xl outline outline-1 outline-offset-[-0.52px] outline-stone-300 flex justify-start items-center gap-2 overflow-hidden">
                <div class="flex-1 justify-start text-neutral-900 text-base font-normal font-['Poppins'] leading-4">{{dropoff}}</div>
              </div>
            </div>
          </div>
            <div data-layer="Connected button group" class="ConnectedButtonGroup self-stretch h-12 rounded-2xl inline-flex justify-start items-center gap-0.5 overflow-hidden px-5">
        <div data-layer="Segment 1" data-selected="True" data-show-focus-indicator="false" data-show-icon="false" data-show-label-text="true" data-state="Enabled" class="Segment1 flex-1 min-w-12 bg-teal-700 rounded-3xl inline-flex flex-col justify-center items-center gap-0.5 overflow-hidden">
          <div data-layer="State-layer" class="StateLayer self-stretch min-w-12 px-3 py-1.5 inline-flex justify-center items-center gap-1 overflow-hidden">
            <div data-layer="Label" class="Label justify-center text-Primary-Accent text-sm font-medium font-['Poppins'] leading-5 tracking-tight">Standard</div>
          </div>
        </div>
        <div data-layer="Segment 2" data-selected="False" data-show-focus-indicator="false" data-show-icon="false" data-show-label-text="true" data-state="Enabled" class="Segment2 flex-1 min-w-12 bg-Background rounded inline-flex flex-col justify-center items-center gap-0.5 overflow-hidden">
          <div data-layer="State-layer" class="StateLayer self-stretch px-3 py-1.5 inline-flex justify-center items-center gap-1 overflow-hidden">
            <div data-layer="Label" class="Label justify-center text-Element-Background text-sm font-medium font-['Poppins'] leading-5 tracking-tight">Deluxe</div>
          </div>
        </div>
        <div data-layer="End segment" data-selected="False" data-show-focus-indicator="false" data-show-icon="false" data-show-label-text="true" data-state="Enabled" class="EndSegment flex-1 min-w-12 bg-Background rounded-tl rounded-tr-2xl rounded-bl rounded-br-2xl inline-flex flex-col justify-center items-center gap-0.5 overflow-hidden">
          <div data-layer="State-layer" class="StateLayer self-stretch px-3 py-1.5 inline-flex justify-center items-center gap-1 overflow-hidden">
            <div data-layer="Label" class="Label justify-center text-Element-Background text-sm font-medium font-['Poppins'] leading-5 tracking-tight">Extra Large</div>
          </div>
        </div>
      </div>
        <div class="bg-green-50 rounded-t-3xl p-6 text-center">
          <div class="text-3xl font-bold text-green-700 mb-2">{{ estimateRange }}</div>
          <div class="text-lg text-green-800 mb-4">{{ distance }} km</div>
        </div>
        <div class="p-6 space-y-3">
          <button class="w-full bg-blue-500 hover:bg-blue-600 text-white py-4 px-6 rounded-2xl text-base font-semibold transition-colors" (click)="onMapView()">
            View on map
          </button>
          <button class="w-full bg-[var(--color-app-accent)] hover:bg-[#A9D53A] text-app-dark py-4 px-6 rounded-2xl text-base font-semibold transition-colors" (click)="onBookRide()">
            Book a ride
          </button>
          <button class="w-full text-gray-500 py-3 text-sm" (click)="close.emit()">Cancel</button>
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
  @Output() close = new EventEmitter<void>();
  pickup = 'negde';
  dropoff = 'drugde';
  estimateRange = '€10-€13';
  distance = '10-13';
  
  onMapView() { console.log('Map'); }
  onBookRide() { console.log('Book'); }
}

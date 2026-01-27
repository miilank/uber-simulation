import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface RatingData {
  vehicleRating: number;
  driverRating: number;
  comment: string;
}

@Component({
  selector: 'app-rating-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-5500"
         *ngIf="isOpen"
         (click)="onBackdropClick($event)">
      <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full mx-4"
           (click)="$event.stopPropagation()">

        <h2 class="text-2xl font-bold text-gray-900 mb-6">Rate Your Ride</h2>

        <!-- Driver Rating -->
        <div class="mb-6">
          <label class="block text-sm font-semibold text-gray-700 mb-2">
            Driver Rating
          </label>
          <div class="flex gap-2">
            <button *ngFor="let star of [1,2,3,4,5]"
                    type="button"
                    (click)="setDriverRating(star)"
                    class="text-3xl transition-colors hover:scale-110 transform"
                    [class.text-yellow-400]="star <= driverRating"
                    [class.text-gray-300]="star > driverRating">
              ★
            </button>
          </div>
        </div>

        <!-- Vehicle Rating -->
        <div class="mb-6">
          <label class="block text-sm font-semibold text-gray-700 mb-2">
            Vehicle Rating
          </label>
          <div class="flex gap-2">
            <button *ngFor="let star of [1,2,3,4,5]"
                    type="button"
                    (click)="setVehicleRating(star)"
                    class="text-3xl transition-colors hover:scale-110 transform"
                    [class.text-yellow-400]="star <= vehicleRating"
                    [class.text-gray-300]="star > vehicleRating">
              ★
            </button>
          </div>
        </div>

        <!-- Comment -->
        <div class="mb-6">
          <label class="block text-sm font-semibold text-gray-700 mb-2">
            Comment (optional)
          </label>
          <textarea
            [(ngModel)]="comment"
            class="w-full min-h-24 rounded-xl border border-gray-200 bg-white px-4 py-3 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100"
            placeholder="Share your experience..."
            maxlength="500"
          ></textarea>
          <div class="text-xs text-gray-500 mt-1">{{ comment.length }}/500</div>
        </div>

        <!-- Buttons -->
        <div class="flex gap-3">
          <button
            (click)="onCancel()"
            class="flex-1 h-11 px-6 rounded-full bg-gray-200 text-gray-800 text-sm font-semibold hover:bg-gray-300 transition-colors">
            Cancel
          </button>
          <button
            (click)="onSubmit()"
            [disabled]="!isValid()"
            class="flex-1 h-11 px-6 rounded-full bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed">
            Submit Rating
          </button>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class RatingModalComponent {
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() submit = new EventEmitter<RatingData>();

  driverRating = 0;
  vehicleRating = 0;
  comment = '';

  setDriverRating(rating: number): void {
    this.driverRating = rating;
  }

  setVehicleRating(rating: number): void {
    this.vehicleRating = rating;
  }

  isValid(): boolean {
    return this.driverRating > 0 && this.vehicleRating > 0;
  }

  onBackdropClick(event: MouseEvent): void {
    this.close.emit();
  }

  onCancel(): void {
    this.close.emit();
  }

  onSubmit(): void {
    if (this.isValid()) {
      this.submit.emit({
        vehicleRating: this.vehicleRating,
        driverRating: this.driverRating,
        comment: this.comment.trim()
      });
      this.resetForm();
    }
  }

  resetForm(): void {
    this.driverRating = 0;
    this.vehicleRating = 0;
    this.comment = '';
  }
}

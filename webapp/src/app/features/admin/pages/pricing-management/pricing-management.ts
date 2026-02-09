import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PricingApiService } from '../../services/pricing-api.service';
import { PricingConfig, VehicleType } from '../../../shared/models/pricing';

@Component({
  selector: 'app-pricing-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './pricing-management.html',
})
export class PricingManagement implements OnInit {
  private pricingApi = inject(PricingApiService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  pricingConfigs: PricingConfig[] = [];
  editingType: VehicleType | null = null;
  editForm!: FormGroup;
  loading = false;

  vehicleTypeLabels: Record<VehicleType, string> = {
    STANDARD: 'Standard',
    LUXURY: 'Luxury',
    VAN: 'Van'
  };

  vehicleTypeIcons: Record<VehicleType, string> = {
    STANDARD: '🚘',
    LUXURY: '🚙',
    VAN: '🚐'
  };

  vehicleTypeDescriptions: Record<VehicleType, string> = {
    STANDARD: 'Regular sedan cars',
    LUXURY: 'Premium vehicles',
    VAN: 'Large capacity vehicles'
  };

  ngOnInit(): void {
    this.loadPricing();

    this.editForm = this.fb.group({
      basePrice: ['', [Validators.required, Validators.min(0)]],
      pricePerKm: ['', [Validators.required, Validators.min(0)]]
    });
  }

  loadPricing(): void {
    this.loading = true;
    this.cdr.detectChanges();
    this.pricingApi.getAllPricing().subscribe({
      next: (configs) => {
        this.pricingConfigs = configs;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load pricing', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  startEdit(config: PricingConfig): void {
    this.editingType = config.vehicleType;
    this.editForm.patchValue({
      basePrice: config.basePrice,
      pricePerKm: config.pricePerKm
    });
  }

  cancelEdit(): void {
    this.editingType = null;
    this.editForm.reset();
  }

  saveEdit(vehicleType: VehicleType): void {
    if (this.editForm.invalid) return;

    this.loading = true;
    this.pricingApi.updatePricing(vehicleType, this.editForm.value).subscribe({
      next: (updated) => {
        const index = this.pricingConfigs.findIndex(c => c.vehicleType === vehicleType);
        if (index !== -1) {
          this.pricingConfigs[index] = updated;
        }
        this.loading = false;
        this.editingType = null;
        this.editForm.reset();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to update pricing', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatDateTime(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleString('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  get basePriceError(): string | null {
    const control = this.editForm.get('basePrice');
    if (control?.hasError('required') && control.touched) {
      return 'Base price is required';
    }
    if (control?.hasError('min') && control.touched) {
      return 'Base price must be positive';
    }
    return null;
  }

  get pricePerKmError(): string | null {
    const control = this.editForm.get('pricePerKm');
    if (control?.hasError('required') && control.touched) {
      return 'Price per km is required';
    }
    if (control?.hasError('min') && control.touched) {
      return 'Price per km must be positive';
    }
    return null;
  }
}

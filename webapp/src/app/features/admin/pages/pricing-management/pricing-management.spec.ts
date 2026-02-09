import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PricingManagement } from './pricing-management';

describe('PricingManagement', () => {
  let component: PricingManagement;
  let fixture: ComponentFixture<PricingManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PricingManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PricingManagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

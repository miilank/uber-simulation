import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverProfileChanges } from './driver-profile-changes';

describe('DriverProfileChanges', () => {
  let component: DriverProfileChanges;
  let fixture: ComponentFixture<DriverProfileChanges>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverProfileChanges]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverProfileChanges);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

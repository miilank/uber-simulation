import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideBookingSidebar } from './ride-booking-sidebar';

describe('RideBookingSidebar', () => {
  let component: RideBookingSidebar;
  let fixture: ComponentFixture<RideBookingSidebar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideBookingSidebar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideBookingSidebar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

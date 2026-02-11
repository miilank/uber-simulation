import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PassengerRideDetailsDrawer } from './ride-details-drawer';

describe('PassengerRideDetailsDrawer', () => {
  let component: PassengerRideDetailsDrawer;
  let fixture: ComponentFixture<PassengerRideDetailsDrawer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerRideDetailsDrawer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PassengerRideDetailsDrawer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

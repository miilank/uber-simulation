import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PassengerRideHistory } from './passenger-ride-history';

describe('PassengerRideHistory', () => {
  let component: PassengerRideHistory;
  let fixture: ComponentFixture<PassengerRideHistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerRideHistory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PassengerRideHistory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

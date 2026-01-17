import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideDetailsDrawer } from './ride-details-drawer';

describe('RideDetailsDrawer', () => {
  let component: RideDetailsDrawer;
  let fixture: ComponentFixture<RideDetailsDrawer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideDetailsDrawer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideDetailsDrawer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

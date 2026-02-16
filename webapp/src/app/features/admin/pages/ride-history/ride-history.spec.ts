import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminRideHistory } from './ride-history';

describe('AdminRideHistory', () => {
  let component: AdminRideHistory;
  let fixture: ComponentFixture<AdminRideHistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRideHistory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminRideHistory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminDriverDashboardWrapper } from './admin-driver-dashboard-wrapper';

describe('AdminDriverDashboardWrapper', () => {
  let component: AdminDriverDashboardWrapper;
  let fixture: ComponentFixture<AdminDriverDashboardWrapper>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDriverDashboardWrapper]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminDriverDashboardWrapper);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

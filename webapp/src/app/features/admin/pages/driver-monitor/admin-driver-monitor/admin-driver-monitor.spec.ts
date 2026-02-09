import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminDriverMonitor } from './admin-driver-monitor';

describe('AdminDriverMonitor', () => {
  let component: AdminDriverMonitor;
  let fixture: ComponentFixture<AdminDriverMonitor>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDriverMonitor]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminDriverMonitor);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

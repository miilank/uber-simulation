import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisteredDashboard } from './registered-dashboard';

describe('RegisteredDashboard', () => {
  let component: RegisteredDashboard;
  let fixture: ComponentFixture<RegisteredDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisteredDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisteredDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

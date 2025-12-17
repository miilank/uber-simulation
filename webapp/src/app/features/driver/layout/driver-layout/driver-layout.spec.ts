import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverLayout } from './driver-layout';

describe('DriverLayout', () => {
  let component: DriverLayout;
  let fixture: ComponentFixture<DriverLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverLayout]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverLayout);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

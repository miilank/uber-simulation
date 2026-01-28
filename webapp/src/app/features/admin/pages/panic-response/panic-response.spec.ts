import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PanicResponse } from './panic-response';

describe('PanicResponse', () => {
  let component: PanicResponse;
  let fixture: ComponentFixture<PanicResponse>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PanicResponse]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PanicResponse);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import {BookedRidesComponent} from './booked-rides';

describe('BookedRides', () => {
  let component: BookedRidesComponent;
  let fixture: ComponentFixture<BookedRidesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookedRidesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookedRidesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

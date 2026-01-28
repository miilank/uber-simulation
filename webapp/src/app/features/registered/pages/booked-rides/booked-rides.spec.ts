import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookedRides } from './booked-rides';

describe('BookedRides', () => {
  let component: BookedRides;
  let fixture: ComponentFixture<BookedRides>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookedRides]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookedRides);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

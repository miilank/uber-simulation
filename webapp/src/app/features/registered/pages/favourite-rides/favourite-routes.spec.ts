import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FavouriteRides } from './favourite-rides';

describe('FavouriteRides', () => {
  let component: FavouriteRides;
  let fixture: ComponentFixture<FavouriteRides>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FavouriteRides]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FavouriteRides);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

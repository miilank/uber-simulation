import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationSearchInput } from './location-search-input';

describe('LocationSearchInput', () => {
  let component: LocationSearchInput;
  let fixture: ComponentFixture<LocationSearchInput>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LocationSearchInput]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationSearchInput);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

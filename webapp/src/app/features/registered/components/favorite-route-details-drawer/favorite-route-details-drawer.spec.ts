import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FavoriteRouteDetailsDrawer } from './favorite-route-details-drawer';

describe('FavoriteRouteDetailsDrawer', () => {
  let component: FavoriteRouteDetailsDrawer;
  let fixture: ComponentFixture<FavoriteRouteDetailsDrawer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FavoriteRouteDetailsDrawer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FavoriteRouteDetailsDrawer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

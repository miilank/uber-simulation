import { Component, EventEmitter, Input, Output } from '@angular/core';

type Route = {
  id: number;
  startLocation: string;
  endLocation: string;
  waypoints: string[],
  requirements: string[];
};


@Component({
  selector: 'app-favorite-route-details-drawer',
  imports: [],
  templateUrl: './favorite-route-details-drawer.html',
})
export class FavoriteRouteDetailsDrawer {
  @Input() open = false;
  @Input() route: Route | null = null;
  @Output() close = new EventEmitter<void>();

  onClose() {
    this.close.emit();
  }
}

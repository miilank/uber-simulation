import { Component, inject } from '@angular/core';
import { PanicsService } from '../../services/panics.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-panic-response',
  imports: [DatePipe],
  templateUrl: './panic-response.html',
})
export class PanicResponseComponent {
  private panicsService = inject(PanicsService);
  readonly unresolvedPanics = this.panicsService.unresolvedPanics;

  resolvePanic(panicId: number) {
    this.panicsService.resolvePanic(panicId);
  }
}

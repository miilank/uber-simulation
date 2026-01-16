import { Component, EventEmitter, input, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'success-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
    class="fixed top-27.5 right-4 z-50">
      <div class="border-l-4 border-app-accent text-green-700 p-4 rounded-lg transform transition-all duration-300 ease-out origin-top-right max-w-sm"
        [ngClass]="{
          'opacity-100 translate-y-0 pointer-events-auto': isOpen,
          'opacity-0 -translate-y-2 pointer-events-none': !isOpen
        }" style="background-color:#e0fabe">
        <p class="text-lg font-semibold">{{title}}</p>
        <p>{{message}}</p>
      </div>
    </div>
  `,
  
})
export class SuccessAlert implements OnChanges {
  @Input() isOpen: boolean = false;
  @Input() message: string = '';
  @Input() title: string = '';
  @Output() close: EventEmitter<void> = new EventEmitter<void>;

  onClose(): void {
    this.close.emit();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen']) {
      if (this.isOpen) {
        setTimeout(() => {
          this.close.emit();
        }, 3000);
      }
    }
  }

}

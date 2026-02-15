import { Component, EventEmitter, input, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'error-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
    class="fixed top-27.5 right-4 z-800">
      <div class="border-l-4 bg-red-50 border-red-200 text-red-500 p-4 rounded-lg transform transition-all duration-300 ease-out origin-top-right max-w-sm font-poppins"
        [ngClass]="{
          'opacity-100 translate-y-0 pointer-events-auto': isOpen,
          'opacity-0 -translate-y-2 pointer-events-none': !isOpen
        }">
        <p id="errorTitle" class="text-lg font-semibold">{{title}}</p>
        <p id="errorMessage" class="overflow-hidden wrap-break-word whitespace-normal">{{message}}</p>
      </div>
    </div>
  `,
  
})
export class ErrorAlert implements OnChanges {
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
        }, 6000);
      }
    }
  }

}

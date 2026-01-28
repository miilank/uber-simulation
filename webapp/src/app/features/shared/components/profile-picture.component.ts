import { ChangeDetectorRef, Component, EventEmitter, inject, Input, OnDestroy, OnInit, Output, signal, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigService } from '../../../core/services/config.service';

@Component({
  selector: 'app-profile-picture',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="relative group w-full h-full"
    [class.group]="editable"
    [class.cursor-pointer]="editable">
      <div class="w-full h-full rounded-full border-[3px] border-app-accent p-0.5 overflow-hidden">
        <img
          [src]="displaySrc()"
          (error)="onImageError($event)"
          alt="Profile"
          class="w-full h-full rounded-full object-cover group-hover:brightness-80"
        />
      </div>

       @if(editable) {
        <button
            (click)="fileInput.click()"
            type="button"
            class="absolute bottom-0 right-0 w-9 h-9 bg-app-accent rounded-full shadow-lg flex items-center justify-center"
            aria-label="Change profile picture"
        >
            <img src="camera.svg" alt="Camera" />
        </button>
       }

      <input
        #fileInput
        type="file"
        accept="image/*"
        (change)="onFileChange($event)"
        class="hidden"
      />
    </div>
  `
})
export class ProfilePictureComponent {
  @Input() editable = false;
  @Input() inputSrc?: string;
  
  defaultSrc = 'defaultprofile.png';

  @Output() avatarSelected = new EventEmitter<File>();

  configService = inject(ConfigService);

  displaySrc = signal<string|null>(null);
  private objectUrl: string | null = null;

  ngOnInit(): void {
    this.updateDisplaySrc();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fetch'] || changes['inputSrc']) {
      this.updateDisplaySrc();
    }
  }

  private updateDisplaySrc() {
    if (this.objectUrl) {
      this.displaySrc.set(this.objectUrl);
      return;
    }

    if (this.inputSrc != null) {
      this.displaySrc.set(this.inputSrc);
    } else {
      this.displaySrc.set(this.defaultSrc);
    }
  }


  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }

    const file : File = input.files[0];
    this.objectUrl = URL.createObjectURL(file);
    this.displaySrc.set(this.objectUrl);

    this.avatarSelected.emit(file);
    input.value = '';
  }

    onImageError(event: Event) {
    const img = event.target as HTMLImageElement;

    try {
      if (this.objectUrl && img.src === this.objectUrl) {
        URL.revokeObjectURL(this.objectUrl);
        this.objectUrl = null;
      }
    } catch {}

    this.displaySrc.set(this.defaultSrc);
    img.src = this.defaultSrc;
  }

}

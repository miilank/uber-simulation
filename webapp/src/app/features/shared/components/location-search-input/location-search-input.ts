import { ChangeDetectorRef, Component, Input, output, signal } from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import { catchError, debounceTime, distinctUntilChanged, filter, map, of, startWith, switchMap } from 'rxjs';
import { NominatimResult, NominatimService } from '../../services/nominatim.service';

@Component({
  selector: 'location-search-input',
  imports: [ReactiveFormsModule],
  standalone: true,
  templateUrl: './location-search-input.html',
})
export class LocationSearchInput {
  @Input() hintMessage: string = 'Look up an address';
  @Input() inputClass: string = '';    
  @Input() containerClass: string = '';

  selected = output<NominatimResult>();

  addressControl = new FormControl('');

  searchResults = signal<NominatimResult[]>([]);
  autocompleteOpen = signal<boolean>(false);

  constructor(private nominatim: NominatimService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
      this.addressControl.valueChanges
      .pipe(
        startWith(this.addressControl.value || ''),
        map(v => (typeof v === 'string' ? v.trim() : '')),
        debounceTime(400),
        distinctUntilChanged(),
        switchMap(v =>
          this.nominatim.search(v, 6).pipe(
            catchError(err => {
              console.log("Search error.")
              return of([] as NominatimResult[])
            })
          )
        )
      )
      .subscribe({
        next: (results) => {
          this.searchResults.set(results);
          if(this.searchResults().length > 0) {
            this.autocompleteOpen.set(true);
          } else {
            this.autocompleteOpen.set(false);
          }
          }
      })
    }

  OnSelect(res: NominatimResult) {
    this.addressControl.setValue(res.formattedText, {emitEvent: false});
    this.autocompleteOpen.set(false);
    this.searchResults.set([]);
    this.selected.emit(res);
  }
}

import { ChangeDetectorRef, Component, input, Input, output, signal, SimpleChanges } from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import { catchError, debounceTime, distinctUntilChanged, filter, map, of, startWith, Subscription, switchMap } from 'rxjs';
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
  
  @Input() value: string | undefined = '';

  private skipNextSearch = false;

  selected = output<NominatimResult>();

  addressControl = new FormControl('');

  searchResults = signal<NominatimResult[]>([]);
  autocompleteOpen = signal<boolean>(false);

  sub?: Subscription;

  constructor(private nominatim: NominatimService) {}

  ngOnInit(): void {
    this.sub = this.addressControl.valueChanges
    .pipe(
      startWith(this.addressControl.value || ''),
      map(v => (typeof v === 'string' ? v.trim() : '')),
      filter(v => {
        if (this.skipNextSearch) {
          this.skipNextSearch = false;
          return false;
        }
        return true;
      }),
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

    this.addressControl.setValue(this.value ?? '', { emitEvent: false });    
  }

    
  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }


  ngOnChanges(changes: SimpleChanges) {
    if (changes['value']) {
      const newVal: string | null = changes['value'].currentValue ?? '';
      if (!this.addressControl.dirty) {
        this.skipNextSearch = true; // Verovatno postoji bolje resenje, ali ja ga ne znam
        this.addressControl.setValue(newVal, { emitEvent: false });
      }
    }
  }

  OnSelect(res: NominatimResult) {
    this.addressControl.setValue(res.formattedText, {emitEvent: false});
    this.autocompleteOpen.set(false);
    this.searchResults.set([]);
    this.selected.emit(res);
  }
}

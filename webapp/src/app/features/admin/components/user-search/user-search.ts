import { Component, inject, output, signal, Signal } from '@angular/core';
import { UserService } from '../../../shared/services/users.service';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { User } from '../../../../core/models/user';
import { CommonModule } from '@angular/common';
import { catchError, debounceTime, distinctUntilChanged, filter, map, of, startWith, switchMap } from 'rxjs';

@Component({
  selector: 'app-user-search',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './user-search.html',
})
export class UserSearch {
  userService: UserService = inject(UserService);

  onSelected = output<User>();
  selectedUser = signal<User | null>(null);

  searchControl = new FormControl('');
  searchResults = signal<User[]>([]);

  ngOnInit(): void {
    this.searchControl.valueChanges
    .pipe(
      startWith(this.searchControl.value || ''),
      map(v => (typeof v === 'string' ? v.trim() : '')),
      filter(v => v.length > 2),
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(v =>
        this.userService.searchUsers(v, 15, 0).pipe(
          catchError(err => {
            console.log("Search error.")
            return of([] as User[])
          })
        )
      )
    )
    .subscribe({
      next: (results) => {
        this.searchResults.set(results);
        }
      })
  }

  clearSearch() {
    this.searchControl.setValue("");
    this.searchResults.set([]);
  }

  selectUser(user: User) {
    this.onSelected.emit(user);
    this.selectedUser.set(user);
  }
}

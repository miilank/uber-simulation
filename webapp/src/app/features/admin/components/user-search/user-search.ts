import { Component, effect, inject, input, model, output, signal, Signal } from '@angular/core';
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
  selectedUser = model<User | null>(null);

  searchControl = new FormControl('');
  searchResults = signal<User[]>([]);

    constructor() {
      effect(() => {
        const u = this.selectedUser();
        if (!u) return;

        this.searchResults.update(list => {
          const idx = list.findIndex(x => x.id === u.id);
          if (idx === -1) return [...list, u];
          const copy = list.slice();
          copy[idx] = u;
          return copy;
        });

        this.selectedUser.set(u);
      });
  }

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
    this.selectedUser.set(user);
  }
}

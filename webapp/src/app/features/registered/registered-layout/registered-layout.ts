import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../shared/components/header.component';
import { RegisteredSidebar } from '../components/registered-sidebar';


@Component({
  selector: 'app-registered-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent],
  template: `<div class="min-h-screen bg-app-dark">
        <app-header></app-header>

        <div class="flex h-[calc(100vh-94px)]">
            <aside class="w-106 bg-app-dark text-white">
              <!-- <app-registered-sidebar></app-registered-sidebar> -->
              <router-outlet name="aside"></router-outlet>
            </aside>

            <main class="flex-1 bg-white overflow-auto">
            <router-outlet></router-outlet>
            </main>
        </div>
        </div>
        `,
})
export class RegisteredLayout {
  firstName = 'Andrew'
  lastName = 'Wilson'
}
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../shared/components/header.component';
import { RegisteredSidebar } from '../components/registered-sidebar';


@Component({
  selector: 'app-registered-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, RegisteredSidebar],
  template: `<div class="min-h-screen bg-app-dark">
        <app-header [firstName]="firstName" [lastName]="lastName"></app-header>

        <div class="flex h-[calc(100vh-94px)]">
            <aside class="w-[424px] bg-app-dark text-white px-10">
            <app-registered-sidebar></app-registered-sidebar>
            </aside>

            <main class="flex-1 bg-white p-4 md:p-6 overflow-auto">
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
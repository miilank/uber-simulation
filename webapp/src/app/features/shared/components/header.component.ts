import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="w-full bg-app-dark">
      <div class="flex items-center justify-between h-[94px] px-6 md:px-8">
        <!-- Logo Section -->
        <div class="flex items-center gap-2.5">
          <img 
            src="https://api.builder.io/api/v1/image/assets/TEMP/880510fa722bf78df9fb6fda1ee63b8ba1554443?width=112"
            alt="UberPLUS"
            class="w-14 h-14 object-contain"
          />
          <div class="text-2xl md:text-4xl font-semibold">
            <span class="font-light text-white">Uber</span><span class="font-semibold text-app-accent">PLUS</span>
          </div>
        </div>

        <!-- User Profile Button -->
        <div class="flex items-center gap-2.5 bg-white rounded-full px-3 py-1.5 md:px-4 md:py-2 shadow-lg">
        
        <!-- Promeni boju !!! -->
        <div class="w-5 h-5 rounded-full bg-green-500 flex items-center justify-center flex-shrink-0"></div>
          <span class="text-black text-sm md:text-base font-medium mr-1">{{firstName}} {{lastName}}</span>
          <img 
            src="defaultprofile.png"
            alt="Profile"
            class="w-8 h-8 md:w-10 md:h-10 rounded-full object-cover"
          />
        </div>
      </div>
    </header>
  `,
  styles: []
})
export class HeaderComponent {
  @Input()
  firstName: string = '';

  @Input()
  lastName: string = '';
}

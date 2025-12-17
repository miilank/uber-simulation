import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header.component';
import { DriverSidebar } from '../../components/driver-sidebar/driver-sidebar';


@Component({
  selector: 'app-driver-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, DriverSidebar],
  templateUrl: './driver-layout.html',
})
export class DriverLayout {
  firstName = 'John'
  lastName = 'Doe'
}

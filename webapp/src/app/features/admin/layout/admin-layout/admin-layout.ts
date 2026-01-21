import { Component } from '@angular/core';
import { HeaderComponent } from "../../../shared/components/header.component";
import { DriverSidebar } from "../../../driver/common/driver-sidebar/driver-sidebar";
import { RouterModule } from "@angular/router";
import { AdminSidebar } from "../../components/admin-sidebar/admin-sidebar";

@Component({
  selector: 'app-admin-layout',
  imports: [HeaderComponent, RouterModule, AdminSidebar],
  templateUrl: './admin-layout.html',
})
export class AdminLayout {

}

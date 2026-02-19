import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header.component';
import { DriverSidebar } from '../../common/driver-sidebar/driver-sidebar';
import { Subscription } from 'rxjs';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { WebSocketService } from '../../../../core/services/websocket.service';


@Component({
  selector: 'app-driver-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, DriverSidebar],
  templateUrl: './driver-layout.html',
})
export class DriverLayout {
    private userSubscription?: Subscription;
  
    constructor(
      private currentUserService: CurrentUserService,
      private websocketService: WebSocketService
    ) {}
  
    ngOnInit(): void {
      this.userSubscription = this.currentUserService.currentUser$.subscribe({
        next: (user) => {
          if (user?.id && user.role === 'DRIVER' && !this.websocketService.isConnected()) {
            this.websocketService.connect(user.id);
          }
        }
      });
    }
  
    ngOnDestroy(): void {
      this.userSubscription?.unsubscribe();
      this.websocketService.disconnect();
    }
}

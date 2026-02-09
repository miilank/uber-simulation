import { Component, OnInit, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminDriverService, DriverListItemDTO } from '../../../services/admin-driver.service';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AdminDriverDashboardWrapper } from '../admin-driver-dashboard-wrapper/admin-driver-dashboard-wrapper';

@Component({
  selector: 'app-admin-driver-monitor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    AdminDriverDashboardWrapper
  ],
  templateUrl: './admin-driver-monitor.html',
})
export class AdminDriverMonitor implements OnInit, OnDestroy {
  private adminDriverService = inject(AdminDriverService);

  allDrivers: DriverListItemDTO[] = [];
  filteredDrivers: DriverListItemDTO[] = [];
  selectedDriver: DriverListItemDTO | null = null;

  searchQuery: string = '';

  private refreshSubscription?: Subscription;

  ngOnInit(): void {
    this.loadDrivers();

    // Auto-refresh every 10 seconds
    this.refreshSubscription = interval(10000)
      .pipe(switchMap(() => this.adminDriverService.getAllDrivers()))
      .subscribe({
        next: (drivers) => {
          this.allDrivers = drivers;
          this.filterDrivers();

          // Update selected driver if still in list
          if (this.selectedDriver) {
            const updatedDriver = drivers.find(d => d.id === this.selectedDriver!.id);
            if (updatedDriver) {
              this.selectedDriver = updatedDriver;
            }
          }
        },
        error: (err) => console.error('Failed to refresh drivers', err)
      });
  }

  ngOnDestroy(): void {
    this.refreshSubscription?.unsubscribe();
  }

  loadDrivers(): void {
    this.adminDriverService.getAllDrivers().subscribe({
      next: (drivers) => {
        this.allDrivers = drivers;
        this.filteredDrivers = drivers;
      },
      error: (err) => {
        console.error('Failed to load drivers', err);
      }
    });
  }

  filterDrivers(): void {
    if (!this.searchQuery.trim()) {
      this.filteredDrivers = this.allDrivers;
      return;
    }

    const query = this.searchQuery.toLowerCase().trim();

    this.filteredDrivers = this.allDrivers.filter(driver => {
      const fullName = `${driver.firstName} ${driver.lastName}`.toLowerCase();
      const email = driver.email.toLowerCase();

      return fullName.includes(query) || email.includes(query);
    });
  }

  selectDriver(driver: DriverListItemDTO): void {
    this.selectedDriver = driver;
  }

  deselectDriver(): void {
    this.selectedDriver = null;
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.filterDrivers();
  }
}

import { ChangeDetectorRef, Component, inject, OnInit, signal } from '@angular/core';
import { FavoriteRouteDTO, FavouriteRoutesService } from '../../services/favourite-routes.service';
import { CommonModule } from '@angular/common';
import { ErrorAlert } from "../../../shared/components/error-alert";
import { SuccessAlert } from "../../../shared/components/success-alert";
import { FavoriteRouteDetailsDrawer } from "../../components/favorite-route-details-drawer/favorite-route-details-drawer";
import { VehicleType } from '../../../shared/models/vehicle';

type Route = {
  id: number;
  name: string;
  startLocation: string;
  endLocation: string;
  waypoints: string[],
  requirements: string[];
};

@Component({
  selector: 'app-favourite-routes',
  imports: [CommonModule, ErrorAlert, SuccessAlert, FavoriteRouteDetailsDrawer],
  templateUrl: './favourite-routes.html',
})
export class FavouriteRoutes implements OnInit{
  routesService = inject(FavouriteRoutesService);

  routes = signal<Route[]>([]);

  isDetailsOpen = false;
  selectedRoute: Route | null = null;

  // ---- Alerts ----
  isErrorOpen = signal<boolean>(false);
  errorTitle: string = "Error";
  errorMessage: string = "Error occured.";

  isSuccessOpen = signal<boolean>(false);
  successTitle: string = "Success";
  successMessage: string = "Success!"
  // ----------------

  ngOnInit() {
    this.routesService.getFavoriteRoutes().subscribe({
      next: (dtos => 
        {
          this.routes.set(dtos.map(dto => this.dtoToRoute(dto)));
        }
      ),
      error: (err => {
        this.errorMessage = err.error?.message || 'Fetching routes failed. Please try again later.';
        this.isErrorOpen.set(true);
      }
    )
    })
  }

  openDetails(r: Route){
    this.isDetailsOpen = true;
    this.selectedRoute = r;
  }

  closeDetails() {
    this.isDetailsOpen = false;
  }

  deleteRoute(r: Route, index: number){
    this.routesService.deleteFavoriteRoute(r.id).subscribe({
      next: (() => 
        {
          this.routes().splice(index, 1);
          this.successMessage = 'Route successfully deleted!';
          this.isSuccessOpen.set(true);
        }
      ),
      error: (err => {
        this.errorMessage = err.error?.message || 'Delete failed. Please try again later.';
        this.isErrorOpen.set(true);
      }
    )
    });
  }

  requirementEmoji: Record<string, string> = {
    Baby: '🧸',
    Luxury: '🚗',
    Standard: '🧭',
    Pets: '🐾',
    Van: '🚐',
  };

  requirementClasses: Record<string, string> = {
    Baby: 'bg-[#FFEDD4] text-[#CA3500]',
    Luxury: 'bg-[#DBEAFE] text-[#1447E6]',
    Standard: 'bg-[#F3E8FF] text-[#8200DB]',
    Pets: 'bg-[#FEF3C6] text-[#BB4D00]',
    Van: 'bg-[#E0E7FF] text-[#432DD7]',
  };

  private dtoToRoute(dto: FavoriteRouteDTO): Route {
    return {
      id: dto.id,
      name: dto.name,
      startLocation: dto.startLocation.address,
      endLocation: dto.endLocation.address,
      waypoints: (dto.waypoints || []).map(dto => dto.address),
      requirements: this.formatRequirements(dto)
    };
  }

  private formatRequirements(route: FavoriteRouteDTO): string[] {
    let requirements: string[] = [];
    if(route.babyFriendly) requirements.push('Baby');
    if(route.petsFriendly) requirements.push('Pets');

    const vehicle = vehicleApiToDisplay[route.vehicleType ?? ''] ?? undefined;
    if (vehicle) requirements.push(vehicle);

    return requirements;
  }

  closeErrorAlert(): void {
    this.isErrorOpen.set(false);
  }

  closeSuccessAlert(): void {
    this.isSuccessOpen.set(false);
  }
}

  const vehicleApiToDisplay: Record<string, string> = {
    STANDARD: VehicleType.STANDARD,
    LUXURY: VehicleType.LUXURY,
    VAN: VehicleType.VAN,
    Standard: VehicleType.STANDARD,
    Luxury: VehicleType.LUXURY,
    Van: VehicleType.VAN,
  };
import { inject, Injectable } from "@angular/core";
import { LocationDTO } from "../../shared/models/location";
import { VehicleType, vehicleTypeKeyFromValue } from "../../shared/models/vehicle";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { ConfigService } from "../../../core/services/config.service";

export interface FavoriteRouteDTO {
    id: number;
    name: string;
    startLocation: LocationDTO;
    endLocation: LocationDTO;
    waypoints: LocationDTO[];
    vehicleType: VehicleType | undefined;
    babyFriendly: boolean;
    petsFriendly: boolean;
    createdAt: Date;
}

export interface FavoriteRouteCreationDTO {
    name: string;
    startLocation: LocationDTO;
    endLocation: LocationDTO;
    waypoints: LocationDTO[];
    vehicleType: (keyof typeof VehicleType) | undefined;
    babyFriendly: boolean;
    petsFriendly: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class FavouriteRoutesService {
  http = inject(HttpClient);
  config = inject(ConfigService);

  getFavoriteRoutes() : Observable<FavoriteRouteDTO[]> {
    return this.http.get<FavoriteRouteDTO[]>(this.config.favouriteRoutesUrl);
  }

  createFavoriteRoute(name: string, startLocation: LocationDTO, endLocation: LocationDTO,
    waypoints: LocationDTO[], vehicleType: VehicleType | undefined, babyFriendly: boolean, petsFriendly: boolean) : Observable<FavoriteRouteDTO> {
    let vehicleTypeDTO: (keyof typeof VehicleType) | undefined;

    if(vehicleType) {
        vehicleTypeDTO = vehicleTypeKeyFromValue(vehicleType);
    }
    
    let body: FavoriteRouteCreationDTO = {
        name,
        startLocation,
        endLocation,
        waypoints,
        vehicleType: vehicleTypeDTO,
        babyFriendly,
        petsFriendly
    }

    return this.http.post<FavoriteRouteDTO>(this.config.favouriteRoutesUrl, body);
  }

  deleteFavoriteRoute(id: number) : Observable<void> {
    return this.http.delete<void>(`${this.config.favouriteRoutesUrl}/${id.toString()}`);
  }
}

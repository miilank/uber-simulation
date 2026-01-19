import { NominatimResult } from "../services/nominatim.service";

export interface LocationDTO {
    latitude: number,
    longitude: number,
    address: string
}

export function nominatimToLocation(result: NominatimResult): LocationDTO {
  return {
    latitude: Number(result.lat),
    longitude: Number(result.lon),
    address: result.formattedText
  };
}
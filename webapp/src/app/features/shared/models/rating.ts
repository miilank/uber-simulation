export interface Rating {
  id: number;
  rideId: number;
  passengerId: number;
  driverId: number;
  vehicleRating: number;
  driverRating: number;
  comment?: string;
//   createdAt: string;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  address: string;
  phoneNumber: string;
  profilePicture: string;
  role: "ADMIN" | "DRIVER" | "PASSENGER"
}
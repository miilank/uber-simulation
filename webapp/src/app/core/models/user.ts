
export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  address: string;
  phoneNumber: string;
  avatarUrl?: string;
  role: "ADMIN" | "DRIVER" | "PASSENGER"
}
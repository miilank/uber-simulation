type RideStatus = 'COMPLETED' | 'CANCELLED';

export interface Ride {
    date: string;
    time: string;
    from: string;
    to: string;
    status: RideStatus;
    cancelledBy?: 'User' | 'Driver';
    panic: boolean;
    price: string;
}
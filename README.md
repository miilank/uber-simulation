### UberPLUS FullStack Project

#### Members
- Andrej Dobrić (SV18/2023)
- Luka Stević (SV65/2023)
- Milan Kačarević (SV73/2023)

#### About the Project
UberPLUS is a university ride-sharing application inspired by Uber, built across multiple courses as a full-stack project.

#### Tech Stack
- **Backend:** Java, Spring Boot, PostgreSQL
- **Frontend:** Angular (TypeScript, HTML, Tailwind CSS), Leaflet maps
- **Mobile:** Android (Java, XML)
- **Testing:** JUnit, Jasmine, Selenium
- **Real-time:** WebSockets (STOMP over SockJS)
- **Maps & Routing:** Leaflet, Nominatim (geocoding), OSRM (route calculation)

#### Features
- User registration and login with JWT authentication and email activation
- Ride booking with multiple stops, waypoints, and linked passengers
- Automatic driver assignment based on availability and proximity
- Real-time vehicle tracking on an interactive map
- Driver and passenger ride history with filtering and sorting
- Ride cancellation and early stopping with location recalculation
- PANIC button for emergency notifications to admins
- Rating system for drivers and vehicles after completed rides
- Inconsistency reporting during active rides
- Live chat support between users and administrators via WebSockets
- Real-time notifications via WebSockets
- Admin panel: driver management, user blocking, ride monitoring, price configuration
- Scheduled rides up to 5 hours in advance with reminder notifications
- Ride reports with charts (distance, earnings, ride count) per date range
- Favorite routes for quick rebooking
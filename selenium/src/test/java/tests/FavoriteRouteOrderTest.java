package tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pages.BookedRidesPage;
import pages.LoginPage;
import pages.RideBookingPage;

import java.util.List;

public class FavoriteRouteOrderTest extends TestBase {
    static final String email = "rideordertest@uberplus.com";
    static final String password = "password";
    static final String blockedEmail = "blocked@uberplus.com";

    @Test
    public void withVehicleTypeTest() {
        driver.get("http://localhost:4200/signIn");
        LoginPage loginPage = new LoginPage(driver);

        Assumptions.assumeTrue(loginPage.isOnLoginPage());

        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
        loginPage.login();

        RideBookingPage bookingPage = new RideBookingPage(driver);
        bookingPage.goToBooking();
        Assertions.assertTrue(bookingPage.isBookingStep1());

        bookingPage.selectFavoriteByName("TestRoute1");

        Assertions.assertTrue(bookingPage.pickupAddressMatches("Pickup Street"));
        Assertions.assertTrue(bookingPage.dropoffAddressMatches("Dropoff Street"));

        Assertions.assertTrue(bookingPage.waypointAddressesMatch(List.of("Waypoint1", "Waypoint2", "Waypoint3")));

        bookingPage.nextStep();
        Assertions.assertTrue(bookingPage.isBookingStep2());

        bookingPage.nextStep();
        Assertions.assertTrue(bookingPage.isBookingStep3());

        Assertions.assertTrue(bookingPage.petsFriendlyIs(true));
        Assertions.assertTrue(bookingPage.babyFriendlyIs(false));

        Assertions.assertTrue(bookingPage.selectedVehicleTypeMatches("Standard"));

        bookingPage.bookRide();
        Assertions.assertTrue(bookingPage.successfulBooking());

        BookedRidesPage bookedRidesPage = new BookedRidesPage(driver);
        bookedRidesPage.goToBookedRides();
        Assertions.assertTrue(bookedRidesPage.isOnBookedRidesPage());
        Assertions.assertFalse(bookedRidesPage.bookedRidesEmpty());
        Assertions.assertTrue(bookedRidesPage.firstRidePickupMatches("Pickup Street"));
        Assertions.assertTrue(bookedRidesPage.firstRideDropoffMatches("Dropoff Street"));
    }

    @Test
    public void noVehicleTypeTest() {
        driver.get("http://localhost:4200/signIn");
        LoginPage loginPage = new LoginPage(driver);
        Assumptions.assumeTrue(loginPage.isOnLoginPage());

        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
        loginPage.login();

        RideBookingPage bookingPage = new RideBookingPage(driver);
        bookingPage.goToBooking();
        Assertions.assertTrue(bookingPage.isBookingStep1());

        bookingPage.selectFavoriteByName("TestRoute2");

        Assertions.assertTrue(bookingPage.pickupAddressMatches("Pickup Street"));
        Assertions.assertTrue(bookingPage.dropoffAddressMatches("Dropoff Street"));

        Assertions.assertTrue(bookingPage.waypointAddressesMatch(List.of("Waypoint1", "Waypoint2", "Waypoint3")));

        bookingPage.nextStep();
        Assertions.assertTrue(bookingPage.isBookingStep2());

        bookingPage.nextStep();
        Assertions.assertTrue(bookingPage.isBookingStep3());

        Assertions.assertTrue(bookingPage.petsFriendlyIs(true));
        Assertions.assertTrue(bookingPage.babyFriendlyIs(false));

        Assertions.assertTrue(bookingPage.selectedVehicleTypeMatches("Any"));

        bookingPage.bookRide();
        Assertions.assertTrue(bookingPage.successfulBooking());

        BookedRidesPage bookedRidesPage = new BookedRidesPage(driver);
        bookedRidesPage.goToBookedRides();
        Assertions.assertTrue(bookedRidesPage.isOnBookedRidesPage());
        Assertions.assertFalse(bookedRidesPage.bookedRidesEmpty());
        Assertions.assertTrue(bookedRidesPage.firstRidePickupMatches("Pickup Street"));
        Assertions.assertTrue(bookedRidesPage.firstRideDropoffMatches("Dropoff Street"));
    }

    @Test
    public void blockedTest() {
        driver.get("http://localhost:4200/signIn");
        LoginPage loginPage = new LoginPage(driver);
        Assumptions.assumeTrue(loginPage.isOnLoginPage());

        loginPage.enterEmail(blockedEmail);
        loginPage.enterPassword(password);
        loginPage.login();

        RideBookingPage bookingPage = new RideBookingPage(driver);
        bookingPage.goToBooking();
        Assertions.assertTrue(bookingPage.isBookingStep1());

        bookingPage.selectFavoriteByName("TestRoute");

        Assertions.assertTrue(bookingPage.pickupAddressMatches("Pickup Street"));
        Assertions.assertTrue(bookingPage.dropoffAddressMatches("Dropoff Street"));

        bookingPage.nextStep();
        Assertions.assertTrue(bookingPage.isBookingStep2());

        bookingPage.nextStep();
        Assertions.assertTrue(bookingPage.isBookingStep3());

        Assertions.assertTrue(bookingPage.petsFriendlyIs(true));
        Assertions.assertTrue(bookingPage.babyFriendlyIs(true));

        Assertions.assertTrue(bookingPage.selectedVehicleTypeMatches("Any"));

        bookingPage.bookRide();
        Assertions.assertTrue(bookingPage.blockedPopupVisible());
        Assertions.assertTrue(bookingPage.errorMessageMatches("Blocked for testing."));

        Assertions.assertFalse(bookingPage.successfulBooking());
    }
}

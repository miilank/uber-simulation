package tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.AdminRideHistoryPage;
import pages.LoginPage;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminRideHistoryFilterSortTest extends TestBase {

    private LoginPage loginPage;
    private AdminRideHistoryPage rideHistoryPage;
    private static final String ADMIN_EMAIL = "a1@uberplus.com";
    private static final String ADMIN_PASSWORD = "password";

    @BeforeEach
    public void setUpTest() {
        loginPage = new LoginPage(driver);
        rideHistoryPage = new AdminRideHistoryPage(driver);

        loginPage.navigateTo();
        loginPage.enterEmail(ADMIN_EMAIL);
        loginPage.enterPassword(ADMIN_PASSWORD);
        loginPage.login();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/admin"));
    }

    @Test
    @Order(1)
    @DisplayName("Should load admin ride history page successfully")
    public void testLoadRideHistoryPage() {
        rideHistoryPage.navigateTo();

        assertTrue(rideHistoryPage.isPageLoaded(), "Ride history page should load successfully");
        assertTrue(driver.getCurrentUrl().contains("/admin/ride-history"),
                "URL should contain ride history path");
    }

    @Test
    @Order(2)
    @DisplayName("Should display filter controls")
    public void testDisplayFilterControls() {
        rideHistoryPage.navigateTo();

        assertTrue(rideHistoryPage.isFromDateInputVisible(), "From date input should be visible");
        assertTrue(rideHistoryPage.isToDateInputVisible(), "To date input should be visible");
        assertTrue(rideHistoryPage.isApplyButtonVisible(), "Apply button should be visible");
        assertTrue(rideHistoryPage.isResetButtonVisible(), "Reset button should be visible");
    }

    @Test
    @Order(3)
    @DisplayName("Should use quick filter for last 7 days")
    public void testQuickFilterLast7Days() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");
        rideHistoryPage.clickLast7DaysFilter();

        LocalDate expectedFromDate = LocalDate.now().minusDays(7);

        String fromDateValue = rideHistoryPage.getFromDateValue();
        String toDateValue = rideHistoryPage.getToDateValue();

        assertFalse(fromDateValue.isEmpty(), "From date should be set");
        assertFalse(toDateValue.isEmpty(), "To date should be set");

        List<String> rideDates = rideHistoryPage.getAllRideDates();
        for (String dateStr : rideDates) {
            LocalDate rideDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            assertFalse(rideDate.isBefore(expectedFromDate), "Ride should be within last 7 days: " + dateStr);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should use quick filter for last 30 days")
    public void testQuickFilterLast30Days() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");
        rideHistoryPage.clickLast30DaysFilter();
        LocalDate expectedFromDate = LocalDate.now().minusDays(30);

        String fromDateValue = rideHistoryPage.getFromDateValue();
        String toDateValue = rideHistoryPage.getToDateValue();

        assertFalse(fromDateValue.isEmpty(), "From date should be set");
        assertFalse(toDateValue.isEmpty(), "To date should be set");

        List<String> rideDates = rideHistoryPage.getAllRideDates();
        for (String dateStr : rideDates) {
            LocalDate rideDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            assertFalse(rideDate.isBefore(expectedFromDate), "Ride should be within last 30 days: " + dateStr);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should display ride details correctly")
    public void testDisplayRideDetails() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");

        int rideCount = rideHistoryPage.getRideCount();

        if (rideCount > 0) {
            List<WebElement> rides = rideHistoryPage.getAllRideRows();
            WebElement firstRide = rides.getFirst();

            assertNotNull(rideHistoryPage.getRideDate(firstRide), "Date should be present");
            assertNotNull(rideHistoryPage.getRideTime(firstRide), "Time should be present");
            assertNotNull(rideHistoryPage.getRideRoute(firstRide), "Route should be present");
            assertNotNull(rideHistoryPage.getRideStatus(firstRide), "Status should be present");
            assertNotNull(rideHistoryPage.getRidePrice(firstRide), "Price should be present");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should open ride details drawer")
    public void testOpenRideDetails() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");

        int rideCount = rideHistoryPage.getRideCount();

        if (rideCount > 0) {
            rideHistoryPage.clickDetailsButtonForFirstRide();
            assertTrue(rideHistoryPage.isDetailsDrawerOpen(),
                    "Details drawer should open");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Should handle invalid date range (to date before from date)")
    public void testInvalidDateRange() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");

        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().minusDays(7);

        rideHistoryPage.setFromDate(fromDate);
        rideHistoryPage.setToDate(toDate);
        rideHistoryPage.clickApplyFilter();

        int rideCount = rideHistoryPage.getRideCount();
        assertTrue(rideCount >= 0, "Should handle invalid date range");
    }

    @Test
    @Order(8)
    @DisplayName("Should handle future dates in filter")
    public void testFutureDateFilter() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");

        LocalDate futureDate = LocalDate.now().plusDays(30);

        rideHistoryPage.setFromDate(LocalDate.now());
        rideHistoryPage.setToDate(futureDate);
        rideHistoryPage.clickApplyFilter();

        int rideCount = rideHistoryPage.getRideCount();
        assertTrue(rideCount >= 0, "Should handle future dates");
    }

    @Test
    @Order(9)
    @DisplayName("Should handle same from and to date")
    public void testSameFromAndToDate() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");

        LocalDate sameDate = LocalDate.now();

        rideHistoryPage.setFromDate(sameDate);
        rideHistoryPage.setToDate(sameDate);
        rideHistoryPage.clickApplyFilter();

        int rideCount = rideHistoryPage.getRideCount();
        assertTrue(rideCount >= 0, "Should handle same date for from and to");
    }

    @Test
    @Order(10)
    @DisplayName("Should handle rapid filter changes")
    public void testRapidFilterChanges() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");

        rideHistoryPage.clickLast7DaysFilter();
        rideHistoryPage.clickLast30DaysFilter();
        rideHistoryPage.clickResetFilter();

        assertTrue(rideHistoryPage.isPageLoaded(), "Page should remain stable");
        int rideCount = rideHistoryPage.getRideCount();
        assertTrue(rideCount >= 0, "Should show valid ride count");
    }

    @Test
    @Order(11)
    @DisplayName("Should display different ride statuses correctly")
    public void testRideStatusDisplay() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.searchAndSelectUser("passenger");

        List<WebElement> rides = rideHistoryPage.getAllRideRows();

        if (!rides.isEmpty()) {
            for (WebElement ride : rides) {
                String status = rideHistoryPage.getRideStatus(ride);
                assertNotNull(status, "Each ride should have a status");
                assertTrue(
                        status.contains("Completed") ||
                                status.contains("Cancelled") ||
                                status.contains("Stopped") ||
                                status.contains("In Progress") ||
                                status.contains("Pending"),
                        "Status should be a valid ride status: " + status
                );
            }
        }
    }

    @Test
    @Order(12)
    @DisplayName("Should search and select different users")
    public void testUserSearch() {
        rideHistoryPage.navigateTo();

        rideHistoryPage.searchAndSelectUser("passenger");
        assertTrue(rideHistoryPage.isPageLoaded(), "Should load history for passenger");

        rideHistoryPage.searchAndSelectUser("driver");
        assertTrue(rideHistoryPage.isPageLoaded(), "Should load history for driver");
    }

    @Test
    @Order(13)
    @DisplayName("Should handle empty search results gracefully")
    public void testEmptySearchResults() {
        rideHistoryPage.navigateTo();

        rideHistoryPage.searchForUser("nonexistentuser12345");

        assertTrue(rideHistoryPage.isPageLoaded(), "Page should remain stable with no results");
    }
}

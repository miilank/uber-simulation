package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BookedRidesPage {
    private WebDriver driver;

    @FindBy(css="div[name='rideContainer']")
    private List<WebElement> bookedRides;

    public BookedRidesPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void goToBookedRides() {
        driver.get("http://localhost:4200/user/booked-rides");
    }

    public boolean isOnBookedRidesPage() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).
                    until(ExpectedConditions.visibilityOfElementLocated(new By.ByXPath("//div[normalize-space()='Booked rides:']")));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public boolean bookedRidesEmpty() {
        return bookedRides.isEmpty();
    }

    public boolean firstRidePickupMatches(String address) {
        return bookedRides.get(0).findElement(By.name("pickupAddress"))
                .getText().trim().equals(address.trim());
    }

    public boolean firstRideDropoffMatches(String address) {
        return bookedRides.get(0).findElement(By.name("dropoffAddress"))
                .getText().trim().equals(address.trim());
    }
}

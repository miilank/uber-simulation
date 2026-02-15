package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RideBookingPage {
    WebDriver driver;

    @FindBy(css="a[name='booking']")
    WebElement bookingSidebarLink;

    @FindBy(css="#favoriteRouteSelect")
    WebElement favoriteRouteSelect;

    @FindBy(css="#pickup input")
    WebElement pickupInput;

    @FindBy(css="#dropoff input")
    WebElement dropoffInput;

    @FindBy(css="#vehicleTypeSelect")
    WebElement vehicleTypeSelect;

    @FindBy(id="errorMessage")
    WebElement errorMessage;

    private final By petsCheckmarkSvg = By.id("petsCheckmark");
    private final By infantCheckmarkSvg = By.id("infantCheckmark");

    @FindBy(xpath="//button//span[normalize-space()='Book']")
    WebElement bookButton;

    public RideBookingPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void goToBooking() {
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(bookingSidebarLink));
        element.click();
    }

    public boolean isBookingStep1() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2)).
                    until(ExpectedConditions.visibilityOfElementLocated(new By.ByCssSelector("#step1header")));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public boolean isBookingStep2() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2)).
                    until(ExpectedConditions.visibilityOfElementLocated(new By.ByCssSelector("#step2header")));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public boolean isBookingStep3() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2)).
                    until(ExpectedConditions.visibilityOfElementLocated(new By.ByCssSelector("#step3header")));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public void selectFavoriteByName(String routeName) {
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(favoriteRouteSelect));
        Select select = new Select(element);
        select.selectByVisibleText(routeName);
    }

    public boolean pickupAddressMatches(String address) {
        return pickupInput.getAttribute("value").equals(address);
    }

    public boolean dropoffAddressMatches(String address) {
        return dropoffInput.getAttribute("value").equals(address);
    }

    public boolean waypointAddressesMatch(List<String> addresses) {
        for(int i = 0; i < addresses.size(); i++) {
            String text = addresses.get(i);
            if(!driver.findElement(new By.ByCssSelector("#waypoint" + i + " input")).getAttribute("value").equals(text)) {
                return false;
            }
        }
        return true;
    }

    public void nextStep(){
        driver.findElement(new By.ByXPath("//button//span[normalize-space()='Next']")).click();
    }

    public void previousStep() {
        driver.findElement(new By.ByXPath("//button/span[text()='Back']")).click();
    }

    public boolean selectedVehicleTypeMatches(String type) {
        Select select = new Select(vehicleTypeSelect);
        return select.getFirstSelectedOption().getText().equals(type);
    }

    public boolean petsFriendlyIs(boolean expected) {
        boolean actual = !driver.findElements(petsCheckmarkSvg).isEmpty();
        return actual == expected;
    }

    public boolean babyFriendlyIs(boolean expected) {
        boolean actual = !driver.findElements(infantCheckmarkSvg).isEmpty();
        return actual == expected;
    }

    public void bookRide() {
        bookButton.click();
    }

    public boolean successfulBooking() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).
                    until(ExpectedConditions.visibilityOfElementLocated(new By.ByXPath("//h2[text()='Booking Successful!']")));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public boolean blockedPopupVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).
                    until(ExpectedConditions.visibilityOfElementLocated(new By.ByXPath("//p[text()='You have been blocked.']")));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public boolean errorMessageMatches(String message) {
        return errorMessage.getText().equals(message);
    }
}

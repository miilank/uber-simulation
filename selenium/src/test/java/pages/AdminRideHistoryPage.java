package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminRideHistoryPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:4200";
    private static final String PAGE_URL = BASE_URL + "/admin/ride-history";

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(css = "input[type='date']")
    private List<WebElement> dateInputs;

    @FindBy(css = ".apply-button")
    private WebElement applyButton;

    @FindBy(css = ".reset-button")
    private WebElement resetButton;

    @FindBy(css = ".last7-button")
    private WebElement last7daysButton;

    @FindBy(css = ".last30-button")
    private WebElement last30daysButton;

    public AdminRideHistoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    public void navigateTo() {
        driver.get(PAGE_URL);
    }

    public boolean isPageLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pageTitle));
            return pageTitle.getText().toLowerCase().contains("ride history");
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void searchForUser(String searchTerm) {
        try {
            WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("app-user-search input")
            ));
            searchInput.clear();
            searchInput.sendKeys(searchTerm);
        } catch (Exception e) {
            System.out.println("Could not find search input: " + e.getMessage());
        }
    }

    public void searchAndSelectUser(String searchTerm) {
        searchForUser(searchTerm);

        try {

            WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(
                    By.name("search_result")
            ));
            firstResult.click();
        } catch (Exception e) {
            System.out.println("Could not select user: " + e.getMessage());
        }
    }

    public boolean isFromDateInputVisible() {
        try {
            return !dateInputs.isEmpty() && dateInputs.getFirst().isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isToDateInputVisible() {
        try {
            return dateInputs.size() >= 2 && dateInputs.get(1).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isApplyButtonVisible() {
        return isButtonVisible(applyButton);
    }

    public boolean isResetButtonVisible() {
        return isButtonVisible(resetButton);
    }

    private boolean isButtonVisible(WebElement button) {
        try {
            return button.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    public void setFromDate(LocalDate date) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='date']")));
            WebElement fromDateInput = dateInputs.getFirst();
            String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];",
                    fromDateInput,
                    dateString
            );
        } catch (Exception e) {
            System.out.println("Could not set from date: " + e.getMessage());
        }
    }

    public void setToDate(LocalDate date) {
        try {
            WebElement toDateInput = dateInputs.get(1);
            String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];",
                    toDateInput,
                    dateString
            );
        } catch (Exception e) {
            System.out.println("Could not set to date: " + e.getMessage());
        }
    }

    public String getFromDateValue() {
        try {
            wait.until(ExpectedConditions.attributeContains(dateInputs.getFirst(),"value",""));
            return dateInputs.getFirst().getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }

    public String getToDateValue() {
        try {
            wait.until(ExpectedConditions.attributeContains(dateInputs.get(1),"value",""));
            return dateInputs.get(1).getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }

    public void clickApplyFilter() {
        wait.until(ExpectedConditions.elementToBeClickable(applyButton));
        applyButton.click();
    }

    public void clickResetFilter() {
        wait.until(ExpectedConditions.elementToBeClickable(resetButton));
        resetButton.click();
    }

    public void clickLast7DaysFilter() {
        wait.until(ExpectedConditions.elementToBeClickable(last7daysButton));
        last7daysButton.click();
    }

    public void clickLast30DaysFilter() {
        wait.until(ExpectedConditions.elementToBeClickable(last30daysButton));
        last30daysButton.click();
    }

    public int getRideCount() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".divide-y > div")));
            List<WebElement> rows = driver.findElements(By.cssSelector(".divide-y > div"));
            return rows.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<WebElement> getAllRideRows() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".divide-y > div")));
            return driver.findElements(By.cssSelector(".divide-y > div"));
        } catch (TimeoutException e) {
            return new ArrayList<>();
        }
    }

    public List<String> getAllRideDates() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".divide-y > div")));
        List<String> dates = new ArrayList<>();
        try {
            List<WebElement> rows = getAllRideRows();
            for (WebElement row : rows) {
                String date = getRideDate(row);
                if (date != null && !date.isEmpty()) {
                    dates.add(date);
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting ride dates: " + e.getMessage());
        }
        return dates;
    }

    public String getRideDate(WebElement rideRow) {
        try {
            List<WebElement> cells = rideRow.findElements(By.cssSelector("div"));
            if (!cells.isEmpty()) {
                return cells.getFirst().getText().trim();
            }
        } catch (Exception e) {
            System.out.println("Could not get ride date: " + e.getMessage());
        }
        return null;
    }

    public String getRideTime(WebElement rideRow) {
        try {
            List<WebElement> cells = rideRow.findElements(By.cssSelector("div"));
            if (cells.size() > 1) {
                return cells.get(1).getText().trim();
            }
        } catch (Exception e) {
            System.out.println("Could not get ride time: " + e.getMessage());
        }
        return null;
    }

    public String getRideRoute(WebElement rideRow) {
        try {
            List<WebElement> cells = rideRow.findElements(By.cssSelector("div"));
            if (cells.size() > 2) {
                return cells.get(2).getText().trim();
            }
        } catch (Exception e) {
            System.out.println("Could not get ride route: " + e.getMessage());
        }
        return null;
    }

    public String getRideStatus(WebElement rideRow) {
        try {
            List<WebElement> statusBadges = rideRow.findElements(By.cssSelector("span"));
            for (WebElement badge : statusBadges) {
                String text = badge.getText().trim();
                if (text.contains("Completed") || text.contains("Cancelled") ||
                        text.contains("Stopped") || text.contains("Progress") ||
                        text.contains("Pending")) {
                    return text;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not get ride status: " + e.getMessage());
        }
        return null;
    }

    public String getRidePrice(WebElement rideRow) {
        try {
            List<WebElement> cells = rideRow.findElements(By.cssSelector("div"));
            for (int i = cells.size() - 3; i < cells.size(); i++) {
                if (i >= 0) {
                    String text = cells.get(i).getText().trim();
                    if (text.contains("€") || text.matches("\\d+\\.\\d+")) {
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not get ride price: " + e.getMessage());
        }
        return null;
    }

    public void clickDetailsButtonForFirstRide() {
        try {
            List<WebElement> rows = getAllRideRows();
            if (!rows.isEmpty()) {
                WebElement firstRow = rows.getFirst();
                WebElement detailsButton = firstRow.findElement(By.cssSelector("button"));
                wait.until(ExpectedConditions.elementToBeClickable(detailsButton));
                detailsButton.click();
            }
        } catch (Exception e) {
            System.out.println("Could not click details button: " + e.getMessage());
        }
    }

    public boolean isDetailsDrawerOpen() {
        try {
            WebElement drawer = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("app-ride-details-drawer")
            ));
            return drawer.isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }


}

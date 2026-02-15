package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.*;

public class BasicTest {

    WebDriver driver;

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testGooglePageTitle() {
        driver.get("https://www.google.com");
        String pageTitle = driver.getTitle();
        assertNotNull(pageTitle);
        assertTrue(pageTitle.contains("Google"), "Title should contain Google");
    }

    @Test
    public void testGooglePageUrl() {
        driver.get("https://www.google.com");
        String currentUrl = driver.getCurrentUrl();
        assertEquals("https://www.google.com/", currentUrl, "URL should match");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
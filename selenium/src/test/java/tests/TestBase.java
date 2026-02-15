package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestBase {
    public static WebDriver driver;

    @BeforeEach
    public void initializeWebDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        Path chromeUserDataDir;
        try {
            chromeUserDataDir = Files.createTempDirectory("chrome-temp-profile-");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp user-data-dir for Chrome", e);
        }

        options.addArguments("--user-data-dir=" + chromeUserDataDir.toString());
        options.addArguments("--incognito");
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--disable-notifications");

        options.addArguments("--disable-features=PasswordLeakDetection,PasswordManager");
        options.addArguments("--disable-password-manager-reauthentication");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);

        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }


    @AfterEach
    public void quitDriver() {
        driver.quit();
    }
}
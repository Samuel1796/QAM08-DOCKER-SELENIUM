package org.example.base;

import org.example.pages.CartPage;
import org.example.pages.CheckoutPage;
import org.example.pages.InventoryPage;
import org.example.pages.LoginPage;
import org.example.utils.ConfigLoader;
import org.example.utils.DriverManager;
import org.example.utils.ScreenshotUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

/**
 * Abstract base class for all UI test classes in the Swag Labs test suite.
 *
 * <p>Registers {@link ScreenshotUtil} as a JUnit 5 extension so that a browser
 * screenshot is automatically captured and attached to the Allure report whenever
 * a test fails. Manages the {@link WebDriver} lifecycle via {@link DriverManager}
 * and exposes pre-initialised page objects to every subclass.
 */
@ExtendWith(ScreenshotUtil.class)
public abstract class BaseTest {

    protected LoginPage loginPage;
    protected InventoryPage inventoryPage;
    protected CartPage cartPage;
    protected CheckoutPage checkoutPage;

    @BeforeEach
    void setUp() {
        DriverManager.initDriver();
        WebDriver driver = DriverManager.getDriver();
        driver.get(ConfigLoader.get("base.url"));
        loginPage = new LoginPage(driver);
        inventoryPage = new InventoryPage(driver);
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
    }

    @AfterEach
    void tearDown() {
        // Capture screenshot before quitting if the test failed.
        // TestWatcher.testFailed fires after @AfterEach, so we use the flag set by ScreenshotUtil.
        if (Boolean.TRUE.equals(ScreenshotUtil.testFailed.get())) {
            ScreenshotUtil.captureAndAttach(DriverManager.getDriver());
            ScreenshotUtil.testFailed.remove();
        }
        DriverManager.quitDriver();
    }
}

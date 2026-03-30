package org.example.pages;

import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * Page Object for the Swag Labs cart page (/cart.html).
 *
 * <p>Provides methods to inspect cart contents and proceed to checkout.
 */
public class CartPage {

    private final WebDriver driver;

    public CartPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    /** Returns the number of items currently in the cart. */
    public int getCartItemCount() {
        return driver.findElements(By.className("cart_item")).size();
    }

    /**
     * Returns the cart badge count from the DOM directly.
     * Returns 0 if the badge is not present.
     */
    public int getCartBadgeCount() {
        try {
            return Integer.parseInt(
                    driver.findElement(By.className("shopping_cart_badge")).getText());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Clicks the Checkout button on the cart page and waits for checkout step-one to load.
     * Uses JavaScript click for reliability across browser versions.
     */
    public void proceedToCheckout() {
        WebElement btn = SeleniumUtils.waitForVisible(driver, By.id("checkout"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        SeleniumUtils.waitForUrlContains(driver, "checkout-step-one");
    }
}

package org.example.pages;

import org.example.utils.ConfigLoader;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

/**
 * Page Object for the Swag Labs inventory page (/inventory.html).
 *
 * <p>Provides methods to interact with product listings and the shopping cart
 * badge/link after a successful login.
 */
public class InventoryPage {

    private final WebDriver driver;

    @FindBy(className = "inventory_item")
    private List<WebElement> inventoryItems;

    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    /**
     * Clicks the "Add to cart" button for the item at the given index.
     * Uses a live {@code driver.findElements} query to avoid stale Page Factory proxy issues.
     *
     * @param index zero-based index of the inventory item to add
     */
    public void addItemToCartByIndex(int index) {
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        WebElement btn = items.get(index).findElement(By.cssSelector("[data-test^='add-to-cart']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    /**
     * Returns the current cart badge count by querying the DOM directly.
     * Waits briefly for the badge to appear after an add-to-cart action.
     * Returns 0 if the badge is not present (empty cart).
     */
    public int getCartBadgeCount() {
        try {
            return Integer.parseInt(
                    SeleniumUtils.waitForVisible(driver, By.className("shopping_cart_badge")).getText());
        } catch (Exception e) {
            return 0;
        }
    }

    /** Returns the total number of inventory items on the page. */
    public int getInventoryItemCount() {
        return driver.findElements(By.className("inventory_item")).size();
    }

    /**
     * Navigates directly to the cart page URL for reliability.
     * Waits until the cart URL is confirmed before returning.
     */
    public void goToCart() {
        driver.navigate().to(ConfigLoader.get("base.url") + "cart.html");
        SeleniumUtils.waitForUrlContains(driver, "cart.html");
    }
}

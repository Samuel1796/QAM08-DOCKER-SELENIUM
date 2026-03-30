package org.example.pages;

import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Page Object for the Swag Labs checkout pages (/checkout-step-one.html,
 * /checkout-step-two.html, /checkout-complete.html).
 *
 * <p>Covers the full checkout flow: filling in customer information,
 * reviewing the order summary, and confirming the purchase.
 */
public class CheckoutPage {

    private final WebDriver driver;

    @FindBy(id = "first-name")
    private WebElement firstNameField;

    @FindBy(id = "last-name")
    private WebElement lastNameField;

    @FindBy(id = "postal-code")
    private WebElement postalCodeField;

    @FindBy(id = "continue")
    private WebElement continueButton;

    @FindBy(id = "finish")
    private WebElement finishButton;

    @FindBy(css = "[data-test='error']")
    private WebElement errorMessage;

    @FindBy(className = "complete-header")
    private WebElement confirmationHeader;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void fillCheckoutInfo(String firstName, String lastName, String postalCode) {
        // Use live findElement calls to avoid stale Page Factory proxy issues
        SeleniumUtils.waitForVisible(driver, By.id("first-name")).clear();
        driver.findElement(By.id("first-name")).sendKeys(firstName);
        driver.findElement(By.id("last-name")).clear();
        driver.findElement(By.id("last-name")).sendKeys(lastName);
        driver.findElement(By.id("postal-code")).clear();
        driver.findElement(By.id("postal-code")).sendKeys(postalCode);
    }

    public void clickContinue() {
        WebElement btn = SeleniumUtils.waitForVisible(driver, By.id("continue"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public void clickFinish() {
        SeleniumUtils.waitAndClick(driver, By.id("finish"));
    }

    public String getErrorMessage() {
        return SeleniumUtils.waitForVisible(driver, By.cssSelector("[data-test='error']")).getText();
    }

    public boolean isErrorDisplayed() {
        try {
            // Wait for error element to appear after form submission
            SeleniumUtils.waitForVisible(driver, By.cssSelector("[data-test='error']"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getConfirmationMessage() {
        return SeleniumUtils.waitForVisible(driver, By.className("complete-header")).getText();
    }
}

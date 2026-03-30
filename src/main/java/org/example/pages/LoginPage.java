package org.example.pages;

import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Page Object for the Swag Labs login page (/).
 *
 * <p>Encapsulates all interactions with the username/password form and
 * the error message displayed on invalid login attempts.
 */
public class LoginPage {

    private final WebDriver driver;

    @FindBy(id = "user-name")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "login-button")
    private WebElement loginButton;

    @FindBy(css = "[data-test='error']")
    private WebElement errorMessage;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void enterUsername(String username) {
        usernameField.clear();
        usernameField.sendKeys(username);
    }

    public void enterPassword(String password) {
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    public void clickLogin() {
        SeleniumUtils.waitAndClick(driver, loginButton);
    }

    public String getErrorMessage() {
        return SeleniumUtils.waitForVisible(driver, By.cssSelector("[data-test='error']")).getText();
    }

    public boolean isErrorDisplayed() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}

package org.example.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared Selenium wait utilities used by all page objects.
 *
 * <p>All methods are static; this class is not meant to be instantiated.
 * The explicit wait duration is read from {@code ConfigLoader.getInt("explicit.wait.seconds")}.
 */
public class SeleniumUtils {

    private SeleniumUtils() {
        // utility class
    }

    /**
     * Waits until the element located by the given {@code locator} is visible on the page.
     *
     * @param driver  the {@link WebDriver} instance to use
     * @param locator the {@link By} locator strategy for the target element
     * @return the visible {@link WebElement}
     */
    public static WebElement waitForVisible(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(ConfigLoader.getInt("explicit.wait.seconds")));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the current browser URL contains the given {@code fragment}.
     *
     * @param driver   the {@link WebDriver} instance to use
     * @param fragment the substring expected to appear in the current URL
     */
    public static void waitForUrlContains(WebDriver driver, String fragment) {
        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(ConfigLoader.getInt("explicit.wait.seconds")));
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    /**
     * Waits until the given {@code element} is clickable and then clicks it.
     *
     * @param driver  the {@link WebDriver} instance to use
     * @param element the {@link WebElement} to wait for and click
     */
    public static void waitAndClick(WebDriver driver, WebElement element) {
        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(ConfigLoader.getInt("explicit.wait.seconds")));
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    /**
     * Waits until the element located by the given {@code locator} is clickable and then clicks it.
     *
     * @param driver  the {@link WebDriver} instance to use
     * @param locator the {@link By} locator strategy for the target element
     */
    public static void waitAndClick(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(ConfigLoader.getInt("explicit.wait.seconds")));
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }
}

package org.example.utils;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
/**
 * JUnit 5 {@link TestWatcher} extension that automatically captures a browser screenshot
 * on test failure and attaches it to the Allure report.
 *
 * <p>Because JUnit 5 calls {@code testFailed} after all {@code @AfterEach} methods complete
 * (by which point the driver is already quit), this extension instead stores a failure flag
 * in a {@link ThreadLocal} during {@code testFailed}. {@link BaseTest} reads this flag in
 * its {@code @AfterEach} to capture the screenshot <em>before</em> quitting the driver.
 *
 * <p>Register this extension on a test class or base class via:
 * <pre>{@code @ExtendWith(ScreenshotUtil.class)}</pre>
 */
public class ScreenshotUtil implements TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtil.class);

    /** Stores whether the current test has failed, so BaseTest can capture before driver quit. */
    public static final ThreadLocal<Boolean> testFailed = ThreadLocal.withInitial(() -> false);

    /**
     * Called by the JUnit 5 engine when a test fails. Sets the {@link #testFailed} flag
     * so that {@link org.example.base.BaseTest} can capture a screenshot before quitting
     * the driver in its {@code @AfterEach}.
     *
     * @param context the JUnit 5 {@link ExtensionContext} for the failed test
     * @param cause   the {@link Throwable} that caused the test to fail
     */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        testFailed.set(true);
    }

    /**
     * Captures the current browser viewport as a PNG and attaches it to the Allure report.
     *
     * <p>Called by {@link org.example.base.BaseTest} from its {@code @AfterEach} while the
     * driver is still alive. If the driver is {@code null} or screenshot capture fails,
     * a {@code WARN} is logged and no exception is thrown.
     *
     * @param driver the active {@link WebDriver} instance
     */
    public static void captureAndAttach(WebDriver driver) {
        if (driver == null) {
            log.warn("WebDriver unavailable, skipping screenshot");
            return;
        }
        try {
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(bytes), "png");
            log.info("Screenshot captured and attached to Allure report");
        } catch (Exception e) {
            log.warn("Failed to capture screenshot", e);
        }
    }
}

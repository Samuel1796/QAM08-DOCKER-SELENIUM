package org.example.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Manages the {@link WebDriver} lifecycle using a {@link ThreadLocal} for thread-safety.
 *
 * <p>Uses WebDriverManager to resolve and configure ChromeDriver automatically,
 * and reads runtime options (headless mode, implicit wait) from {@link ConfigLoader}.
 */
public class DriverManager {

    private static final Logger log = LoggerFactory.getLogger(DriverManager.class);

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverManager() {
        // utility class
    }

    /**
     * Initialises a {@link ChromeDriver} instance and stores it in the current thread's
     * {@link ThreadLocal} slot.
     *
     * <p><b>Side effects:</b>
     * <ul>
     *   <li>Calls {@code WebDriverManager.chromedriver().setup()} to resolve and cache the
     *       matching ChromeDriver binary.</li>
     *   <li>Reads {@code headless} from {@link ConfigLoader}; when {@code true}, adds
     *       {@code --headless}, {@code --no-sandbox}, and {@code --disable-dev-shm-usage}
     *       to {@link ChromeOptions}.</li>
     *   <li>Applies an implicit wait of {@code implicit.wait.seconds} seconds to the new driver.</li>
     *   <li>Stores the driver in {@link ThreadLocal}, making it available via {@link #getDriver()}
     *       on the same thread.</li>
     * </ul>
     *
     * <p><b>Thread-safety:</b> Each calling thread receives its own independent
     * {@link WebDriver} instance. Concurrent threads do not share driver state.
     */
    public static void initDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (ConfigLoader.getBoolean("headless")) {
            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
            log.info("ChromeDriver initialised in headless mode");
        } else {
            log.info("ChromeDriver initialised in headed mode");
        }

        WebDriver webDriver = new ChromeDriver(options);
        webDriver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigLoader.getInt("implicit.wait.seconds")));

        driver.set(webDriver);
        log.debug("WebDriver stored in ThreadLocal for thread '{}'", Thread.currentThread().getName());
    }

    /**
     * Returns the {@link WebDriver} instance associated with the current thread.
     *
     * @return the current thread's {@link WebDriver}, or {@code null} if {@link #initDriver()}
     *         has not been called on this thread or the driver has been removed via {@link #quitDriver()}
     */
    public static WebDriver getDriver() {
        return driver.get();
    }

    /**
     * Quits the {@link WebDriver} for the current thread and removes it from the
     * {@link ThreadLocal} to prevent memory leaks.
     *
     * <p><b>Side effects:</b>
     * <ul>
     *   <li>Calls {@link WebDriver#quit()} on the current thread's driver instance, closing
     *       all associated browser windows and ending the WebDriver session.</li>
     *   <li>Any exception thrown by {@code quit()} is caught and logged at {@code WARN} level
     *       so that teardown failures do not mask test failures.</li>
     *   <li>Calls {@link ThreadLocal#remove()} unconditionally to release the reference,
     *       even if {@code quit()} threw an exception.</li>
     * </ul>
     *
     * <p><b>Thread-safety:</b> Only affects the {@link WebDriver} bound to the calling thread.
     * Other threads' driver instances are unaffected.
     */
    public static void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            try {
                webDriver.quit();
                log.debug("WebDriver quit successfully on thread '{}'", Thread.currentThread().getName());
            } catch (Exception e) {
                log.warn("Exception while quitting WebDriver on thread '{}': {}",
                        Thread.currentThread().getName(), e.getMessage(), e);
            }
        }
        driver.remove();
    }
}

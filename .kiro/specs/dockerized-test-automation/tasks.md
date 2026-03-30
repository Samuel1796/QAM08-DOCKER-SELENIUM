# Implementation Plan: Dockerized Test Automation

## Overview

Incremental implementation of a Dockerized Selenium test automation suite for Swag Labs using Java 11, Maven, JUnit 5, Page Object Model, Allure reporting, and GitHub Actions CI. Each task builds on the previous, ending with full Docker and CI wiring.

## Tasks

- [x] 1. Configure Maven build (pom.xml)
  - Set `maven.compiler.source` and `maven.compiler.target` to `11`
  - Add `aspectj.version` property (`1.9.22`)
  - Add dependencies: `junit-jupiter` 5.10.2 (test), `selenium-java` 4.20.0, `webdrivermanager` 5.8.0, `allure-junit5` 2.29.0 (test), `javafaker` 1.0.2, `slf4j-api` 2.0.13, `logback-classic` 1.5.6 (test), `jqwik` 1.8.4 (test), `assertj-core` 3.25.3 (test)
  - Add plugins: `maven-compiler-plugin` (Java 11), `maven-surefire-plugin` 3.2.5 with aspectjweaver `-javaagent` argLine, `allure-maven` 2.12.0 with `reportVersion` 2.29.0
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Create test configuration file
  - Create `src/test/resources/test.properties` with keys: `base.url`, `headless`, `implicit.wait.seconds`, `explicit.wait.seconds`, `faker.seed`
  - Set defaults: `base.url=https://www.saucedemo.com/`, `headless=true`, `implicit.wait.seconds=5`, `explicit.wait.seconds=10`, `faker.seed=42`
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 3. Implement ConfigLoader utility
  - Create `src/main/java/org/example/utils/ConfigLoader.java`
  - Load `test.properties` from classpath in a `static` block; throw `IllegalStateException` if file not found
  - Implement `get(String key)`: prefer `System.getProperty(key)`, fall back to file value; throw `IllegalStateException("Missing required property: <key>")` if absent
  - Implement `getBoolean(String key)` and `getInt(String key)` delegating to `get()`
  - Add Javadoc on all public methods per Requirements 12.4
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 12.4_

  - [ ]* 3.1 Write property tests for ConfigLoader
    - Create `src/test/java/org/example/ConfigLoaderPropertyTest.java`
    - **Property 1: ConfigLoader file read round-trip** — generate arbitrary key/value pairs, write to temp properties file, verify `get()` returns exact value
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**
    - **Property 2: ConfigLoader missing key throws exception** — generate keys absent from file and system properties, verify `IllegalStateException` message contains key
    - **Validates: Requirements 2.6**
    - **Property 3: ConfigLoader system property override** — set system property to a different value, verify `get()` returns system property value
    - **Validates: Requirements 2.7**
    - Each `@Property(tries = 100)` with tag comment `// Feature: dockerized-test-automation, Property N: ...`

- [ ] 4. Implement DriverManager utility
  - Create `src/main/java/org/example/utils/DriverManager.java`
  - Declare `ThreadLocal<WebDriver> driver`
  - Implement `initDriver()`: call `WebDriverManager.chromedriver().setup()`, build `ChromeOptions` (add `--headless`, `--no-sandbox`, `--disable-dev-shm-usage` when `ConfigLoader.getBoolean("headless")` is true), create `ChromeDriver`, apply implicit wait from `ConfigLoader.getInt("implicit.wait.seconds")`, store in `ThreadLocal`
  - Implement `getDriver()` returning the current thread's instance
  - Implement `quitDriver()`: call `driver.quit()` in try-catch (log WARN on exception), then `driver.remove()`
  - Add Javadoc on `initDriver()` and `quitDriver()` per Requirements 12.3
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 12.3_

  - [ ]* 4.1 Write property tests for DriverManager
    - Create `src/test/java/org/example/DriverManagerPropertyTest.java`
    - **Property 4: DriverManager headless Chrome options** — when `headless=true`, verify `ChromeOptions` args contain all three required flags
    - **Validates: Requirements 3.2**
    - **Property 5: DriverManager ThreadLocal isolation** — spawn N threads each calling `initDriver()`, verify each thread gets a distinct `WebDriver` instance; quit all drivers after
    - **Validates: Requirements 3.5**
    - Each `@Property(tries = 100)`

- [x] 5. Implement SeleniumUtils utility
  - Create `src/main/java/org/example/utils/SeleniumUtils.java`
  - Implement `waitForVisible(WebDriver driver, By locator)`: use `WebDriverWait` with `ConfigLoader.getInt("explicit.wait.seconds")` and `ExpectedConditions.visibilityOfElementLocated`; return the `WebElement`
  - Implement `waitForUrlContains(WebDriver driver, String fragment)`: use `WebDriverWait` with `ExpectedConditions.urlContains`
  - Implement `waitAndClick(WebDriver driver, WebElement element)`: use `WebDriverWait` with `ExpectedConditions.elementToBeClickable(element)` then click
  - _Requirements: 4.6_

- [x] 6. Implement TestDataFactory utility
  - Create `src/main/java/org/example/utils/TestDataFactory.java`
  - Define inner `record CheckoutInfo(String firstName, String lastName, String postalCode)`
  - Initialise `Faker` with `new Random(ConfigLoader.getInt("faker.seed"))` (default `42`)
  - Implement `validCheckoutInfo()`: return `CheckoutInfo` with faker-generated first name, last name, and numerify postal code
  - Implement `invalidCredentials()`: return `Stream<Arguments>` covering wrong username/correct password, correct username/wrong password, both wrong, SQL injection, XSS, 256-char string, whitespace-only, empty fields
  - Implement `invalidCheckoutData()`: return `Stream<Arguments>` covering empty first name, empty last name, empty postal code, all empty, special-char postal code, 21-char postal code, whitespace-only first name, Unicode/emoji first name
  - Implement edge-case helpers: `emptyString()`, `whitespaceOnly()`, `longString(int length)`, `specialChars()`, `unicodeEmoji()`
  - Implement `withSeed(long seed)` factory returning a scoped instance for property tests
  - _Requirements: 5.7, 7.7, 13.1, 13.2, 13.3, 13.4, 13.5, 13.6_

  - [ ]* 6.1 Write property tests for TestDataFactory
    - Create `src/test/java/org/example/TestDataFactoryPropertyTest.java`
    - **Property 13: TestDataFactory seed reproducibility** — for any seed, calling `withSeed(seed).validCheckoutInfo()` twice returns equal records; same for `invalidCredentials()` stream contents
    - **Validates: Requirements 5.7, 13.1, 13.6**
    - `@Property(tries = 100)`

- [x] 7. Implement Page Objects
  - Create `src/main/java/org/example/pages/LoginPage.java` with `@FindBy` fields for `#user-name`, `#password`, `#login-button`, `[data-test='error']`; implement `enterUsername`, `enterPassword`, `clickLogin`, `getErrorMessage`, `isErrorDisplayed`; call `PageFactory.initElements(driver, this)` in constructor
  - Create `src/main/java/org/example/pages/InventoryPage.java` with `@FindBy` fields for `.inventory_item`, `.shopping_cart_badge`, `.shopping_cart_link`; implement `addItemToCartByIndex`, `addAllItemsToCart`, `getCartBadgeCount`, `getInventoryItemCount`, `goToCart`
  - Create `src/main/java/org/example/pages/CartPage.java` with `@FindBy` fields for `.cart_item`, `.shopping_cart_badge`, `#checkout`; implement `getCartItemNames`, `getCartItemCount`, `getCartBadgeCount`, `removeItemByIndex`, `proceedToCheckout`
  - Create `src/main/java/org/example/pages/CheckoutPage.java` with `@FindBy` fields for `#first-name`, `#last-name`, `#postal-code`, `#continue`, `#finish`, `[data-test='error']`, `.complete-header`; implement `fillCheckoutInfo`, `clickContinue`, `clickFinish`, `getErrorMessage`, `isErrorDisplayed`, `getConfirmationMessage`
  - Each page object uses `SeleniumUtils` for waits where appropriate
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

- [x] 8. Implement ScreenshotUtil (JUnit 5 TestWatcher)
  - Create `src/main/java/org/example/utils/ScreenshotUtil.java` implementing `TestWatcher`
  - Override `testFailed(ExtensionContext context, Throwable cause)`: call `DriverManager.getDriver()`; if null log WARN and return; otherwise cast to `TakesScreenshot`, call `getScreenshotAs(OutputType.BYTES)`, attach via `Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(bytes), "png")`
  - Wrap `getScreenshotAs` in try-catch; log WARN with stack trace on exception, do not rethrow
  - Add Javadoc on `testFailed` per Requirements 12.5
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 12.5_

  - [ ]* 8.1 Write property tests for ScreenshotUtil
    - Create `src/test/java/org/example/ScreenshotUtilPropertyTest.java`
    - **Property 7: Screenshot captured and attached on failure** — mock `DriverManager` returning a `TakesScreenshot` WebDriver stub that returns random PNG bytes; verify `Allure.addAttachment` is called with correct MIME type
    - **Validates: Requirements 8.1, 8.2, 5.5, 6.5, 7.5**
    - **Property 8: Screenshot capture is safe when driver is unavailable** — call `testFailed()` with null driver; verify no exception thrown
    - **Validates: Requirements 8.4**
    - `@Property(tries = 100)`

- [x] 9. Implement BaseTest
  - Create `src/test/java/org/example/base/BaseTest.java`
  - Annotate with `@ExtendWith(ScreenshotUtil.class)`
  - Declare `protected` fields: `loginPage`, `inventoryPage`, `cartPage`, `checkoutPage`
  - `@BeforeEach setUp()`: call `DriverManager.initDriver()`, get driver, navigate to `ConfigLoader.get("base.url")`, instantiate all four page objects
  - `@AfterEach tearDown()`: call `DriverManager.quitDriver()`
  - _Requirements: 3.4, 8.3, 12.1, 12.2_

- [x] 10. Implement LoginTest
  - Create `src/test/java/org/example/tests/LoginTest.java` extending `BaseTest`
  - Annotate class with `@Feature("Login")` and `@Story("Authentication")`
  - Implement `validLoginNavigatesToInventory()`: enter `standard_user`/`secret_sauce`, click login, assert URL contains `inventory`; annotate with `@Description`
  - Implement `@ParameterizedTest @MethodSource("org.example.utils.TestDataFactory#invalidCredentials") invalidCredentialsShowError(String username, String password)`: enter credentials, click login, assert `loginPage.isErrorDisplayed()` is true
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 9.3_

- [x] 11. Implement CartTest
  - Create `src/test/java/org/example/tests/CartTest.java` extending `BaseTest`
  - Annotate class with `@Feature("Cart")` and `@Story("Product Selection")`
  - Add `@BeforeEach` login step (valid credentials) before each test
  - Implement `addSingleItemIncreasesBadge()`: add item at index 0, assert badge count is 1
  - Implement `cartPageListsAddedItems()`: add two items, go to cart, assert `cartPage.getCartItemNames()` contains both product names
  - Implement `removeItemDecrementsBadge()`: add item, go to cart, remove it, assert badge count is 0 and item absent
  - Implement `@ParameterizedTest @CsvSource({"1","2","3"}) addMultipleItemsMatchesBadge(int quantity)`: add `quantity` items, assert badge equals `quantity`; add max-items case via `@MethodSource`
  - Implement edge-case tests: add same product twice (badge stays 1), navigate to empty cart (no items listed)
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 9.3_

- [x] 12. Implement CheckoutTest
  - Create `src/test/java/org/example/tests/CheckoutTest.java` extending `BaseTest`
  - Annotate class with `@Feature("Checkout")` and `@Story("Purchase Flow")`
  - Add `@BeforeEach` login + add item + go to cart + proceed to checkout step
  - Implement `validCheckoutShowsOrderSummary()`: fill with `TestDataFactory.validCheckoutInfo()`, click continue, assert URL contains `checkout-step-two`
  - Implement `finishOrderShowsConfirmation()`: complete checkout, click finish, assert `checkoutPage.getConfirmationMessage()` is non-blank
  - Implement `@ParameterizedTest @MethodSource("org.example.utils.TestDataFactory#invalidCheckoutData") invalidFormDataShowsError(String firstName, String lastName, String postalCode)`: fill form, click continue, assert `checkoutPage.isErrorDisplayed()` is true
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 9.3_

- [ ] 13. Checkpoint — verify unit and integration tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 14. Create Dockerfile
  - Create `Dockerfile` at project root
  - Base image: `FROM selenium/standalone-chrome:latest`
  - `USER root`; install `maven wget tar` via `apt-get`
  - Download `allure-2.29.0.tgz` from GitHub releases, extract to `/opt`, symlink to `/usr/local/bin/allure`, remove archive
  - `WORKDIR /app`, `COPY . .`, run `mvn dependency:resolve -q`
  - `CMD ["sh", "-c", "mvn verify -Dheadless=true && allure generate target/allure-results --clean -o /allure-report"]`
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

- [x] 15. Create docker-compose.yml
  - Create `docker-compose.yml` at project root
  - Define `tests` service: `build: .`, `container_name: selenide-tests`, mount named volumes `allure-results:/app/target/allure-results` and `allure-report:/allure-report`
  - Define `allure-serve` service: `image: nginx:alpine`, `container_name: allure-serve`, `depends_on: tests: condition: service_completed_successfully`, `ports: "4040:80"`, mount `allure-report:/usr/share/nginx/html:ro`
  - Declare top-level `volumes: allure-results:` and `allure-report:`
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

- [x] 16. Create GitHub Actions CI workflow
  - Create `.github/workflows/ci.yml`
  - Trigger on `push` and `pull_request` to `main`
  - Steps: checkout (`actions/checkout@v4`), `docker build -t dockerized-test-automation .`, `docker compose up --exit-code-from tests`, upload `target/allure-results` artifact (`if: always()`), upload `allure-report` artifact (`if: always()`)
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8_

- [x] 17. Final checkpoint — full suite green
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Property tests use jqwik with `@Property(tries = 100)` and tag comments referencing the property number from the design document
- Each task references specific requirements for traceability
- Selenium integration tests require a running browser and are executed inside Docker via `mvn verify`
- Unit and property tests run without a browser via `mvn test`

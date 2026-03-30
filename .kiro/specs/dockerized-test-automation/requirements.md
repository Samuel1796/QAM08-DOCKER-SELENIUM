# Requirements Document

## Introduction

This feature delivers a fully Dockerized, CI-ready Selenium test automation suite for the Swag Labs demo application (https://www.saucedemo.com/). The suite covers Login, Cart, and Checkout UI flows using Java + Maven + Selenium WebDriver, structured with the Page Object Model (POM) and Page Factory pattern. Tests run inside a Docker container, produce Allure reports with failure screenshots, and are triggered automatically via a GitHub Actions CI pipeline.

## Glossary

- **Test_Suite**: The collection of JUnit 5 test classes covering Login, Cart, and Checkout flows.
- **Page_Object**: A Java class representing a single UI page, encapsulating locators and interactions via Page Factory.
- **Driver_Manager**: The component responsible for initialising and tearing down the WebDriver instance.
- **Config_Loader**: The component that reads `test.properties` and exposes configuration values to the rest of the suite.
- **Allure_Reporter**: The Allure framework integration that collects test results and generates HTML reports.
- **Screenshot_Util**: The utility that captures a browser screenshot and attaches it to the Allure report.
- **Docker_Image**: The OCI-compliant container image built from the project `Dockerfile`.
- **CI_Pipeline**: The GitHub Actions workflow that builds the Docker image and executes the Test_Suite on every push or pull request.
- **Headless_Mode**: A browser configuration flag that runs Chrome without a visible UI, required inside Docker.
- **POM**: Page Object Model design pattern.
- **SUT**: System Under Test — the Swag Labs web application at https://www.saucedemo.com/.
- **TestDataFactory**: A utility class that uses Java Faker with a fixed seed to generate reproducible test data for parameterised tests.

---

## Requirements

### Requirement 1: Project Structure and Build Configuration

**User Story:** As a developer, I want a well-structured Maven project with all required dependencies declared, so that the project builds and runs consistently across environments.

#### Acceptance Criteria

1. THE Test_Suite SHALL be a Maven project with `groupId` `org.example` and `artifactId` `QAM08_DOCKER`.
2. THE Test_Suite SHALL declare dependencies for Selenium WebDriver, WebDriverManager, JUnit 5, Allure, Java Faker, and a Java SLF4J logging implementation in `pom.xml`.
3. THE Test_Suite SHALL use Java 11 or higher as the compiler source and target level.
4. WHEN `mvn test` is executed, THE Test_Suite SHALL compile all sources and run all test classes without manual classpath configuration.
5. THE Test_Suite SHALL organise source files under `src/main/java/org/example` for page objects and utilities, and `src/test/java/org/example` for test classes.

---

### Requirement 2: Configuration Management

**User Story:** As a developer, I want all environment-specific settings stored in a properties file, so that I can change base URL, headless mode, and wait timeouts without modifying source code.

#### Acceptance Criteria

1. THE Config_Loader SHALL read configuration from `src/test/resources/test.properties`.
2. THE Config_Loader SHALL expose a `baseUrl` property containing the SUT URL.
3. THE Config_Loader SHALL expose a `headless` boolean property controlling whether Chrome runs in Headless_Mode.
4. THE Config_Loader SHALL expose an `implicitWaitSeconds` integer property controlling the WebDriver implicit wait duration.
5. THE Config_Loader SHALL expose an `explicitWaitSeconds` integer property controlling the maximum explicit wait duration.
6. IF a required property is missing from `test.properties`, THEN THE Config_Loader SHALL throw an `IllegalStateException` with a message identifying the missing key.
7. WHERE a system property with the same key is set at runtime, THE Config_Loader SHALL use the system property value instead of the file value, allowing Docker and CI overrides.

---

### Requirement 3: WebDriver Initialisation and Teardown

**User Story:** As a developer, I want a centralised Driver_Manager that handles browser setup and teardown, so that every test gets a clean, correctly configured browser session.

#### Acceptance Criteria

1. THE Driver_Manager SHALL use WebDriverManager to automatically resolve and download the ChromeDriver binary.
2. WHEN `headless` is `true` in configuration, THE Driver_Manager SHALL launch Chrome with `--headless`, `--no-sandbox`, and `--disable-dev-shm-usage` arguments.
3. THE Driver_Manager SHALL apply the `implicitWaitSeconds` value from Config_Loader to the WebDriver instance after creation.
4. WHEN a test finishes, THE Driver_Manager SHALL quit the WebDriver instance and release all associated resources.
5. THE Driver_Manager SHALL store the WebDriver instance in a `ThreadLocal` variable to support parallel test execution.

---

### Requirement 4: Page Object Model Implementation

**User Story:** As a developer, I want each SUT page represented as a Page_Object using Page Factory, so that locators are centralised and tests remain readable and maintainable.

#### Acceptance Criteria

1. THE Test_Suite SHALL provide a `LoginPage` Page_Object encapsulating the username field, password field, and login button locators and their interaction methods.
2. THE Test_Suite SHALL provide an `InventoryPage` Page_Object encapsulating product listing, add-to-cart buttons, and navigation to the cart.
3. THE Test_Suite SHALL provide a `CartPage` Page_Object encapsulating cart item display, item removal, and checkout navigation.
4. THE Test_Suite SHALL provide a `CheckoutPage` Page_Object encapsulating the checkout information form, continue button, finish button, and order confirmation message.
5. WHEN a Page_Object is instantiated, THE Page_Object SHALL initialise all `@FindBy`-annotated fields using `PageFactory.initElements`.
6. THE Test_Suite SHALL provide a `BasePage` class that all Page_Objects extend, containing the shared WebDriver reference and common wait utilities.

---

### Requirement 5: Login Flow Tests

**User Story:** As a QA engineer, I want automated tests for the Login flow, so that authentication behaviour is verified on every build.

#### Acceptance Criteria

1. WHEN valid credentials (`standard_user` / `secret_sauce`) are submitted, THE Test_Suite SHALL verify that the inventory page URL is loaded.
2. WHEN invalid credentials are submitted, THE Test_Suite SHALL verify that an error message is displayed on the login page.
3. WHEN the username field is left empty and the form is submitted, THE Test_Suite SHALL verify that a field-level validation error is displayed.
4. WHEN the password field is left empty and the form is submitted, THE Test_Suite SHALL verify that a field-level validation error is displayed.
5. IF a login test fails, THEN THE Screenshot_Util SHALL capture a screenshot and attach it to the Allure report for that test.
6. THE Test_Suite SHALL parameterise invalid credential tests using JUnit 5 `@ParameterizedTest` with `@MethodSource`, covering: wrong username with correct password, correct username with wrong password, both wrong, SQL injection strings, XSS strings, strings exceeding 255 characters, whitespace-only values, and empty fields.
7. THE TestDataFactory SHALL supply the invalid credential argument stream for the parameterised login tests, using Java Faker initialised with a fixed seed value of `42L` to ensure reproducibility across runs.

---

### Requirement 6: Cart Flow Tests

**User Story:** As a QA engineer, I want automated tests for the Cart flow, so that product selection and cart management are verified on every build.

#### Acceptance Criteria

1. WHEN a product is added to the cart from the inventory page, THE Test_Suite SHALL verify that the cart badge count increments by one.
2. WHEN the cart page is opened, THE Test_Suite SHALL verify that all previously added products are listed.
3. WHEN a product is removed from the cart page, THE Test_Suite SHALL verify that the cart badge count decrements by one and the item is no longer listed.
4. WHEN multiple products are added, THE Test_Suite SHALL verify that the cart badge count equals the number of added products.
5. IF a cart test fails, THEN THE Screenshot_Util SHALL capture a screenshot and attach it to the Allure report for that test.
6. THE Test_Suite SHALL parameterise multi-product add and remove tests using JUnit 5 `@ParameterizedTest` with `@CsvSource` or `@MethodSource`, covering quantities: 1, 2, 3, and the maximum number of available products.
7. THE parameterised cart tests SHALL include edge cases: adding the same product twice (verifying the badge count remains at 1), attempting to remove a product not present in the cart (verifying no error is thrown), and navigating to an empty cart (verifying no items are listed).

---

### Requirement 7: Checkout Flow Tests

**User Story:** As a QA engineer, I want automated tests for the Checkout flow, so that the end-to-end purchase process is verified on every build.

#### Acceptance Criteria

1. WHEN valid checkout information (first name, last name, postal code) is submitted, THE Test_Suite SHALL verify that the order summary page is displayed.
2. WHEN the finish button is clicked on the order summary page, THE Test_Suite SHALL verify that the order confirmation message is displayed.
3. WHEN the checkout information form is submitted with an empty first name, THE Test_Suite SHALL verify that a validation error is displayed.
4. WHEN the checkout information form is submitted with an empty postal code, THE Test_Suite SHALL verify that a validation error is displayed.
5. IF a checkout test fails, THEN THE Screenshot_Util SHALL capture a screenshot and attach it to the Allure report for that test.
6. THE Test_Suite SHALL parameterise checkout form validation tests using JUnit 5 `@ParameterizedTest` with `@MethodSource`, covering: empty first name, empty last name, empty postal code, all fields empty, postal code containing special characters, postal code exceeding 20 characters, first name containing only whitespace, and first name containing Unicode or emoji characters.
7. THE TestDataFactory SHALL supply valid checkout information for non-parameterised checkout tests using Java Faker initialised with a fixed seed to ensure reproducibility across runs.

---

### Requirement 8: Screenshot Capture on Failure

**User Story:** As a QA engineer, I want screenshots automatically captured and attached to Allure reports on test failure, so that I can diagnose failures without re-running tests.

#### Acceptance Criteria

1. WHEN a test fails, THE Screenshot_Util SHALL capture the current browser viewport as a PNG image.
2. THE Screenshot_Util SHALL attach the PNG image to the current Allure test result using `Allure.addAttachment`.
3. THE Screenshot_Util SHALL be invoked from a test listener or `@AfterMethod`/`@AfterEach` hook so that no test class needs to call it directly.
4. IF the WebDriver instance is unavailable at the time of capture, THEN THE Screenshot_Util SHALL log a warning and skip the attachment without throwing an exception.

---

### Requirement 9: Allure Reporting

**User Story:** As a QA engineer, I want Allure reports generated after each test run, so that I have a structured, visual record of test results.

#### Acceptance Criteria

1. THE Allure_Reporter SHALL collect test results in the `target/allure-results` directory during test execution.
2. WHEN `mvn allure:report` is executed after tests, THE Allure_Reporter SHALL generate an HTML report in `target/allure-report`.
3. THE Test_Suite SHALL annotate test methods with `@Story`, `@Feature`, and `@Description` Allure annotations to provide structured metadata in the report.
4. THE Allure_Reporter SHALL include failure screenshots as attachments within the corresponding failed test entry.

---

### Requirement 10: Dockerfile

**User Story:** As a DevOps engineer, I want a Dockerfile that packages the test suite into a runnable container image, so that tests execute in a consistent, isolated environment.

#### Acceptance Criteria

1. THE Docker_Image SHALL use `selenium/standalone-chrome:latest` as the base image.
2. THE Docker_Image SHALL switch to `USER root` and install Maven, wget, and tar via `apt-get` during the image build.
3. THE Docker_Image SHALL install Allure CLI version `2.29.0` by downloading the archive from GitHub releases, extracting it to `/opt`, and creating a symlink at `/usr/local/bin/allure`.
4. THE Docker_Image SHALL set `WORKDIR /app`, copy project files into the image, and run `mvn dependency:resolve -q` to pre-cache dependencies.
5. WHEN the container starts, THE Docker_Image SHALL execute `mvn verify -Dheadless=true` followed by `allure generate target/allure-results --clean -o /allure-report` as the default CMD.
6. WHEN `docker build -t dockerized-test-automation .` is executed from the project root, THE Docker_Image SHALL build successfully without errors.
7. WHEN `docker run dockerized-test-automation` is executed, THE Docker_Image SHALL run the full Test_Suite and exit with code `0` on success or a non-zero code on failure.

---

### Requirement 11: GitHub Actions CI Pipeline

**User Story:** As a DevOps engineer, I want a GitHub Actions workflow that builds the Docker image and runs the test suite automatically, so that every code change is validated in CI.

#### Acceptance Criteria

1. THE CI_Pipeline SHALL trigger on every push to the `main` branch and on every pull request targeting `main`.
2. THE CI_Pipeline SHALL check out the repository source code as its first step.
3. THE CI_Pipeline SHALL build the Docker_Image using `docker build`.
4. THE CI_Pipeline SHALL run the Test_Suite by executing `docker compose up --exit-code-from tests` against the built image.
5. WHEN tests pass, THE CI_Pipeline SHALL exit with status `0` and mark the workflow run as successful.
6. WHEN tests fail, THE CI_Pipeline SHALL exit with a non-zero status and mark the workflow run as failed.
7. THE CI_Pipeline SHALL upload the `target/allure-results` directory as a workflow artifact so reports are accessible from the GitHub Actions run summary.
8. THE CI_Pipeline SHALL upload the `allure-report` directory as a workflow artifact in addition to `allure-results`.

---

### Requirement 12: Code Quality and Documentation

**User Story:** As a developer, I want the codebase to follow SOLID and DRY principles with Javadoc on complex logic, so that the project is maintainable and onboarding is straightforward.

#### Acceptance Criteria

1. THE Test_Suite SHALL apply the Single Responsibility Principle by separating page interaction logic (Page_Objects), driver lifecycle (Driver_Manager), configuration (Config_Loader), and reporting utilities (Screenshot_Util, Allure_Reporter) into distinct classes.
2. THE Test_Suite SHALL apply the DRY principle by extracting repeated setup and teardown logic into base classes or shared listeners rather than duplicating it across test classes.
3. THE Driver_Manager SHALL include Javadoc on its initialisation and teardown methods describing parameters, return values, and side effects.
4. THE Config_Loader SHALL include Javadoc on its property-access methods describing the expected property key and the exception thrown when the key is absent.
5. THE Screenshot_Util SHALL include Javadoc on its capture method describing when it is called and what it attaches to the report.
6. THE Test_Suite SHALL use meaningful, descriptive names for all classes, methods, and variables with no single-letter identifiers outside loop counters.

---

### Requirement 13: Test Data Management

**User Story:** As a QA engineer, I want a centralised TestDataFactory that generates reproducible test data using a fixed seed, so that CI failures can be reproduced locally with identical inputs.

#### Acceptance Criteria

1. THE Test_Suite SHALL include a `TestDataFactory` class that initialises Java Faker with a fixed seed read from `test.properties` under the key `faker.seed`, defaulting to `42` when the key is absent.
2. THE TestDataFactory SHALL provide a static factory method that returns valid checkout information (first name, last name, postal code) as a plain data object or record.
3. THE TestDataFactory SHALL provide a static factory method that returns a `Stream<Arguments>` of invalid credential sets for use with JUnit 5 `@MethodSource` parameterised tests.
4. THE TestDataFactory SHALL provide a static factory method that returns a `Stream<Arguments>` of invalid checkout form data sets for use with JUnit 5 `@MethodSource` parameterised tests.
5. THE TestDataFactory SHALL provide static helper methods returning edge-case strings: empty string, whitespace-only string, string exceeding 255 characters, string containing special characters, and string containing Unicode or emoji characters.
6. THE fixed seed SHALL ensure that identical data is generated on every run so that failures observed in CI are reproducible in a local environment without additional configuration.

---

### Requirement 14: Docker Compose Orchestration

**User Story:** As a DevOps engineer, I want a `docker-compose.yml` that runs the test suite and serves the Allure report automatically, so that a single command delivers both test execution and a browsable report.

#### Acceptance Criteria

1. THE project SHALL include a `docker-compose.yml` file defining two services: `tests` and `allure-serve`.
2. THE `tests` service SHALL build from the project Dockerfile, use container name `selenide-tests`, and mount the named volume `allure-results` to `/app/target/allure-results` and the named volume `allure-report` to `/allure-report`.
3. THE `allure-serve` service SHALL use the `nginx:alpine` image, use container name `allure-serve`, declare a dependency on the `tests` service with condition `service_completed_successfully`, expose port `4040` on the host mapped to port `80` in the container, and mount the `allure-report` named volume to `/usr/share/nginx/html` in read-only mode.
4. THE `docker-compose.yml` SHALL declare named volumes `allure-results` and `allure-report` at the top-level `volumes` key.
5. WHEN `docker compose up` is executed, THE Test_Suite SHALL run to completion and the Allure report SHALL be served and accessible at `http://localhost:4040` after the `tests` service exits successfully.

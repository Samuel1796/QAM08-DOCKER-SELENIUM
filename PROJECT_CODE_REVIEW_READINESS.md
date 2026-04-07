# Project Code Review Readiness Guide

## 1) What this project is about

This project containerizes a Selenium-based test automation suite for Swag Labs and integrates it into GitHub Actions. The main goal is to make test execution consistent across local machines and CI by running the same tooling (Java, Maven, Chrome, Allure) inside Docker.

Core outcomes:
- UI test automation implemented with JUnit 5 + Selenium WebDriver
- Dockerized test runtime via `Dockerfile`
- Local orchestration via `docker-compose.yml`
- CI pipeline in `.github/workflows/api-test-automation-ci.yml`
- Allure reports published and shared in Slack

---

## 2) Objectives and how they were tackled

### Objective A: Understand Docker benefits in QA workflows
Addressed by:
- Isolating browser/runtime dependencies inside container image
- Removing host-specific WebDriver/browser mismatches
- Standardizing execution in local and CI environments

### Objective B: Write a Dockerfile for automation projects
Addressed by:
- Using `selenium/standalone-chrome` as stable browser base
- Installing Maven and Allure CLI in the image
- Defining a container entry command that runs tests and generates reports

### Objective C: Build, run, and debug test containers
Addressed by:
- Building image with `docker build`
- Running tests and exporting artifacts using mounted `target` directory
- Serving Allure report with Nginx via Docker Compose

### Objective D: Integrate containerized tests into CI
Addressed by:
- GitHub Actions jobs for Maven test execution, report publishing, and Docker validation
- Artifact upload/download between jobs
- Slack notification job with rich Block Kit message (report-focused)

---

## 3) Project structure and responsibilities

- `src/main/java/org/example/pages/*`
  - Page Object Model classes for Swag Labs pages (`LoginPage`, `InventoryPage`, `CartPage`, `CheckoutPage`)
- `src/test/java/org/example/tests/*`
  - Test scenarios (`LoginTest`, `CartTest`) with Allure annotations
- `src/test/java/org/example/base/BaseTest.java`
  - Driver lifecycle and page object initialization
- `src/test/resources/test.properties`
  - Runtime flags such as `headless=true`
- `Dockerfile`
  - Container image definition and automated test/report entry command
- `docker-compose.yml`
  - Two-service flow: execute tests, then serve Allure report
- `.github/workflows/api-test-automation-ci.yml`
  - CI pipeline: test, report publish, Docker execution check, Slack notification

---

## 4) End-to-end execution flow

1. Tests run with Maven (`mvn test` / `mvn verify`).
2. Surefire and Allure raw results are generated under `target/`.
3. Allure HTML report is generated (`mvn allure:report` or Allure CLI in Docker).
4. CI publishes report to GitHub Pages (`gh-pages`).
5. Slack receives a report-only Block Kit message containing:
   - overall status (from `test` + `allure-report` jobs)
   - repository and branch metadata
   - buttons for Allure report and workflow run

---

## 5) Commands you should know before review

### Local Maven run
```powershell
mvn clean test
mvn allure:report
```

### Local Docker run
```powershell
docker build -t api-test-automation .
docker run --rm -v "${PWD}\target:/app/target" api-test-automation
```

### Local Docker Compose run and report serving
```powershell
docker compose up --build
```
Then open: `http://localhost:4040`

---

## 6) CI workflow breakdown

### Job: `test`
- Compiles and runs tests on Ubuntu with JDK 17
- Always uploads:
  - `allure-results`
  - `surefire-reports`

### Job: `allure-report`
- Downloads `allure-results`
- Reuses history from `gh-pages` when available
- Generates and publishes Allure HTML report to GitHub Pages
- Uploads generated report as artifact

### Job: `docker-test`
- Builds image and runs tests inside Docker
- Uploads Docker-produced test reports

### Job: `notify-slack`
- Runs regardless of upstream success/failure (`if: always()`)
- Builds Slack payload using Block Kit JSON
- Sends report-focused notification through `SLACK_WEBHOOK_URL`

---

## 7) Secrets and environment requirements

Required GitHub secrets:
- `SLACK_WEBHOOK_URL` (for Slack notifications)

Used by default in workflow/runtime:
- `GITHUB_TOKEN` (for publishing to `gh-pages`)
- GitHub Actions metadata variables (`GITHUB_REPOSITORY`, `GITHUB_REF_NAME`, etc.)

---

## 8) Rubric mapping

### Test suite (30)
- Implemented Selenium tests for core Swag Labs flows
- Structured with Page Object Model + shared `BaseTest`

### Containerization (50)
- `Dockerfile` implemented
- Image builds and executes tests
- Compose flow supports report persistence and report serving

### CI pipeline (20)
- GitHub Actions workflow runs tests and publishes reports
- Docker test path is validated in CI
- Slack notification posts report-only status summary

---

## 9) Review notes and known gaps to discuss

- Current explicit tests present in repository: login and cart flows.
- `CheckoutPage` exists and supports checkout actions, but reviewers may ask whether dedicated checkout test classes are complete for full objective coverage.
- `continue-on-error: true` is intentionally used in some jobs/steps to preserve report and notification generation even on failures.
- Slack step is non-blocking (`continue-on-error: true`) to avoid masking test/report outcomes when webhook delivery fails.

---

## 10) Suggested demo script for code review

1. Explain architecture quickly: POM + BaseTest + utilities.
2. Run `mvn test` and show generated artifacts in `target/`.
3. Run `docker build` + `docker run` and show parity with local run.
4. Run `docker compose up --build` and open Allure at `http://localhost:4040`.
5. Walk through CI jobs and show Slack report notification format.

This sequence demonstrates technical scope, reproducibility, and CI readiness in under 10 minutes.


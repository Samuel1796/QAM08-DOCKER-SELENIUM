FROM selenium/standalone-chrome:latest

# /**
#  * @purpose Run Maven Selenium UI tests inside a pre-configured Chrome container.
#  * @why selenium/standalone-chrome ships browser + driver, reducing host setup drift.
#  * @command docker build -t api-test-automation .
#  */
USER root

# /**
#  * @purpose Install build-time/runtime tools used by the test entrypoint.
#  * @details maven runs tests; wget/tar install the Allure CLI binary.
#  */
RUN apt-get update && \
    apt-get install -y maven wget tar && \
    rm -rf /var/lib/apt/lists/*

# /**
#  * @purpose Install Allure CLI so report generation can run after tests in the same container.
#  * @command allure generate target/allure-results --clean -o /allure-report
#  */
ARG ALLURE_VERSION=2.29.0
RUN wget -qO /tmp/allure.tgz "https://github.com/allure-framework/allure2/releases/download/${ALLURE_VERSION}/allure-${ALLURE_VERSION}.tgz" && \
    tar -xzf /tmp/allure.tgz -C /opt && \
    ln -s /opt/allure-${ALLURE_VERSION}/bin/allure /usr/local/bin/allure && \
    rm /tmp/allure.tgz

WORKDIR /app

# /**
#  * @purpose Copy source and warm dependency cache to speed up repeat builds.
#  * @command mvn dependency:resolve -q
#  */
COPY . .
RUN mvn dependency:resolve -q

# /**
#  * @method Container entry command.
#  * @logic 1) Execute verification suite in headless mode.
#  *        2) Generate static Allure HTML report from raw result files.
#  * @command docker run --rm -v <host-target-path>:/app/target api-test-automation
#  */
CMD mvn verify -Dheadless=true; allure generate target/allure-results --clean -o /allure-report

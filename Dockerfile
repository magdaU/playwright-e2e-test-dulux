# syntax=docker/dockerfile:1

# Reproducible E2E test runner for the Dulux Playwright framework.
# Mirrors the GitHub Actions pipeline: Java 25, Chromium installed with its
# OS dependencies, Cucumber smoke suite run headless.
FROM maven:3.9-eclipse-temurin-25

# - PLAYWRIGHT_BROWSERS_PATH: install Chromium to a shared, world-readable path
#   so the non-root runtime user can find it.
# - MAVEN_OPTS pins a local repository inside /app so dependencies resolved at
#   build time are reused at run time (no re-download on container start).
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright \
    MAVEN_OPTS="-Dmaven.repo.local=/app/.m2/repository" \
    HEADLESS=true \
    CUCUMBER_TAGS=@smoke

WORKDIR /app

# 1) Resolve Maven dependencies first for better layer caching.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# 2) Install Chromium + the OS libraries it needs (apt requires root here).
RUN mvn -B -q exec:java \
        -Dexec.mainClass=com.microsoft.playwright.CLI \
        -Dexec.args="install --with-deps chromium"

# 3) Add the test sources.
COPY src ./src

# 4) Run as a non-root user so Chromium's sandbox behaves exactly as it does on
#    the CI runner. The shared /app and /ms-playwright are handed to that user.
RUN useradd --create-home --uid 1001 pwuser \
    && chown -R pwuser:pwuser /app /ms-playwright
USER pwuser

# CUCUMBER_TAGS / HEADLESS can be overridden at run time, e.g.
#   docker run --rm -e CUCUMBER_TAGS="@regression" dulux-e2e-tests
CMD ["sh", "-c", "mvn -B -Dheadless=true -Dtest=CucumberRunner -Dcucumber.filter.tags=\"${CUCUMBER_TAGS}\" test"]

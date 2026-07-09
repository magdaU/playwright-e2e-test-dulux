<div align="center">

# 🎭 Playwright E2E Automation Framework

### UI end-to-end test automation for [Dulux UK](https://www.dulux.co.uk) — Java · Playwright · Cucumber BDD · Allure · CI/CD

[![E2E Tests](https://github.com/magdaU/playwright-java-dulux-uk/actions/workflows/e2e-tests.yml/badge.svg)](https://github.com/magdaU/playwright-java-dulux-uk/actions/workflows/e2e-tests.yml)
[![Allure Report](https://img.shields.io/badge/Allure-Report-brightgreen?logo=qameta&logoColor=white)](https://magdau.github.io/playwright-java-dulux-uk/)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Playwright](https://img.shields.io/badge/Playwright-1.50-2EAD33?logo=playwright&logoColor=white)](https://playwright.dev/java/)
[![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Cucumber](https://img.shields.io/badge/Cucumber-BDD-23D96C?logo=cucumber&logoColor=white)](https://cucumber.io/)
[![Test Strategy](https://img.shields.io/badge/Test-Strategy-8A2BE2?logo=readthedocs&logoColor=white)](docs/TEST_STRATEGY.md)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

<!--
  HERO IMAGE
  Replace the line below with a banner once you have one
  (e.g. an Allure dashboard screenshot or a custom title card).
  Save it as docs/images/hero.png and it will render here.
-->
<div align="center">
  <img src="docs/images/allure-overview.png" alt="Allure report overview" width="850">
</div>

---

## 📖 Overview

A production-style **UI end-to-end test automation framework** that exercises real customer journeys on the [Dulux UK](https://www.dulux.co.uk) website — buying a colour tester and launching the Visualizer app — across **desktop and mobile viewports**.

The project is built the way a commercial QA framework is built: a clean **Page Object Model**, a **Cucumber BDD** layer that describes behaviour in business language, **AssertJ** assertions kept out of the page layer, **Allure** reporting with screenshots on failure, and a **GitHub Actions** pipeline that runs the smoke suite on every push and publishes a live report to GitHub Pages.

> **Why Dulux?** It's a real, public, JavaScript-heavy e-commerce site — cookie banners, dropdown navigation, new-tab flows and responsive layouts — which makes it a realistic target for demonstrating robust, non-flaky automation.

> 🧭 **New here?** Read the [**Test Strategy**](docs/TEST_STRATEGY.md) — what we test, why, the scope, risk analysis and the roadmap.

---

## ✨ Key Features

- 🧱 **Page Object Model + Component Objects** — pages and reusable UI components (`NavigationComponent`, `AlertComponent`) each extend a shared `BasePage`, eliminating duplication.
- 🥒 **Cucumber BDD layer** — scenarios written in plain-language Gherkin, with `@smoke` / `@regression` / `@desktop` / `@mobile` tags runnable by expression.
- 📱 **Cross-viewport coverage** — the same journeys run at desktop (1920×1080) and mobile (375×667) resolutions.
- 🔌 **Dependency Injection** — PicoContainer shares a single `CucumberContext` (browser state + business helpers) across all step definitions.
- ✅ **Assertions in the test layer only** — page objects never assert; AssertJ fluent assertions live in tests/steps.
- 📊 **Allure + Cucumber HTML reporting** — screenshot auto-attached to every failed scenario, plus Trend, Categories, Executors and Environment dashboard widgets.
- 🖼 **Visual regression testing** — self-hosted pixel-diff comparison against committed baselines, run as a separate non-blocking CI job.
- 🚀 **CI/CD with GitHub Actions** — smoke suite on every push/PR, artifacts uploaded, live report deployed to GitHub Pages.

---

## 🏛 Architecture

A layered framework: business-readable Gherkin and JUnit tests sit on top, the page layer talks to Playwright, and reporting/CI wrap the whole thing.

```mermaid
flowchart TD
    subgraph Spec["📝 Specification layer"]
        FF["Gherkin feature files<br/>tester_purchase · visualizer_experience"]
        JT["JUnit tests<br/>TesterProductTest · VisualizerAppTest"]
    end

    subgraph Glue["🔗 Orchestration layer"]
        RUN["CucumberRunner / BaseTest"]
        STEPS["Step definitions<br/>CommonSteps · AddTesterToCartSteps · VisualizerAppSteps"]
        HOOKS["CucumberHooks<br/>@Before / @After + screenshot on failure"]
        CTX["CucumberContext<br/>browser state + business methods"]
    end

    subgraph Page["🧱 Page Object layer"]
        BASE["BasePage"]
        POM["HomePage · ColorSelectionPage · CartPage"]
        COMP["NavigationComponent · AlertComponent"]
    end

    subgraph Engine["⚙️ Engine"]
        PW["Playwright (Chromium)"]
        SITE["dulux.co.uk"]
    end

    subgraph Out["📊 Reporting & CI"]
        ALLURE["Allure + Cucumber HTML"]
        CI["GitHub Actions → GitHub Pages"]
    end

    FF --> RUN
    JT --> RUN
    RUN --> STEPS
    STEPS --> CTX
    HOOKS --> CTX
    CTX --> POM
    CTX --> COMP
    POM --> BASE
    COMP --> BASE
    BASE --> PW
    PW --> SITE
    HOOKS --> ALLURE
    RUN --> CI
    CI --> ALLURE
```

### Design patterns

- **Page Object Model (POM)** — each page is a class extending `BasePage` (shared `Page` field).
- **Component Objects** — reusable UI parts separated from full-page objects.
- **Base classes** — `BasePage` and `BaseTest` remove duplicated setup/teardown.
- **BDD (Given/When/Then)** — Cucumber scenarios describe behaviour, not implementation.
- **Dependency Injection** — PicoContainer injects `CucumberContext` into every step class.

---

## 🗂 Project Structure

```
src/
└── test/
    ├── java/
    │   └── com/github/magdalena/
    │       ├── cucumber/
    │       │   ├── steps/
    │       │   │   ├── CommonSteps.java           # Reusable Given/When steps (shared across features)
    │       │   │   ├── AddTesterToCartSteps.java  # Then steps for tester purchase
    │       │   │   └── VisualizerAppSteps.java    # Then steps for visualizer experience
    │       │   ├── CucumberContext.java           # Shared browser state + high-level business methods
    │       │   ├── CucumberHooks.java             # @Before / @After – screenshot on failure
    │       │   └── CucumberRunner.java            # JUnit Platform suite runner with Allure plugin
    │       ├── page/
    │       │   ├── component/
    │       │   │   ├── AlertComponent.java        # Handles alert/banner interactions
    │       │   │   └── NavigationComponent.java   # Top nav, hamburger menu, search
    │       │   └── pom/
    │       │       ├── CartPage.java              # Shopping cart page actions
    │       │       ├── ColorSelectionPage.java    # Colour picker & tester purchase
    │       │       └── HomePage.java              # Home page navigation & cookies
    │       ├── support/
    │       │   └── PlaywrightConfig.java          # Reads headless flag from system property / env var
    │       └── tests/
    │           ├── BaseTest.java                  # Shared JUnit setup / teardown
    │           ├── purchase/
    │           │   └── TesterProductTest.java     # Add colour tester to cart (desktop & mobile)
    │           └── visualizer/
    │               └── VisualizerAppTest.java     # Visualizer app new-tab flow (desktop & mobile)
    └── resources/
        └── features/
            ├── tester_purchase.feature            # BDD scenarios for TC-01 / TC-02
            └── visualizer_experience.feature      # BDD scenarios for TC-03 / TC-04
```

---

## 🧰 Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Playwright | 1.50.0 | Browser automation |
| JUnit Jupiter | 5.11.1 | Test runner |
| Cucumber | 7.18.1 | BDD layer (Gherkin feature files) |
| PicoContainer | – | Dependency injection for Cucumber |
| AssertJ | 3.24.2 | Fluent assertions |
| Allure | 2.29.1 | Test reporting |
| image-comparison | 4.4.0 | Visual regression (pixel-diff) testing |
| Maven | 3.x | Build & dependency management |
| GitHub Actions | – | CI/CD pipeline |

---

## 🚀 Getting Started

### Prerequisites
- **Java 21+**
- **Maven 3.x**
- Internet access (tests run against `dulux.co.uk`)

### Clone & install browsers
```bash
git clone https://github.com/magdaU/playwright-java-dulux-uk.git
cd playwright-java-dulux-uk

# install the Playwright browser binaries (Chromium)
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium"
```

### Run the suite
```bash
mvn test
```

That's it — `mvn test` runs the JUnit tests and the full Cucumber suite.

---

## 🐳 Run in Docker

Run the full suite in a reproducible container — no local Java, Maven, or browser install required. Just clone and run.

The image mirrors the CI pipeline: **Java 21**, **Chromium installed with its OS dependencies**, tests executed **headless** as a non-root user.

### Files

| File | Purpose |
|---|---|
| `Dockerfile` | Builds the test runner image (`maven:3.9-eclipse-temurin-21` base) |
| `.dockerignore` | Keeps `target/`, `.git/`, IDE files, etc. out of the build context |
| `docker-compose.yml` | One-command run with sensible defaults + report volumes |

### Quick start (Docker Compose)

```bash
# Build the image and run the @smoke suite
docker compose up --build

# Run a different tag expression
CUCUMBER_TAGS="@regression" docker compose up --build
```

Allure results and Cucumber reports are written back to the host under `target/allure-results` and `target/cucumber-reports`.

### Plain Docker

```bash
# Build
docker build -t dulux-e2e-tests .

# Run the smoke suite (default)
docker run --rm --shm-size=1g dulux-e2e-tests

# Run a custom tag expression
docker run --rm --shm-size=1g -e CUCUMBER_TAGS="@desktop" dulux-e2e-tests

# Persist reports to the host
docker run --rm --shm-size=1g \
  -v "$(pwd)/target/allure-results:/app/target/allure-results" \
  dulux-e2e-tests
```

> **Why `--shm-size=1g`?** Chromium can crash with the default 64 MB `/dev/shm` inside containers. Compose sets this automatically.

### Configuration

| Variable | Default | Meaning |
|---|---|---|
| `HEADLESS` | `true` | Headless browser (always `true` in the container) |
| `CUCUMBER_TAGS` | `@smoke` | Cucumber tag expression to run |

---

## ▶️ Running Tests

### Test cases covered

| ID | Scenario | Viewport | Expected result |
|---|---|---|---|
| **TC-01** | Add colour tester to cart | 🖥️ Desktop | Cart contains 1× Dulux Colour Tester – *Gentle Lavender* |
| **TC-02** | Add colour tester to cart | 📱 Mobile | Cart contains 1× Dulux Colour Tester – *Gentle Lavender* |
| **TC-03** | Open Visualizer App | 🖥️ Desktop | Visualizer App opens in a **new tab** |
| **TC-04** | Open Visualizer App | 📱 Mobile | Error message shown – app **not supported** on mobile |

### Common commands

```bash
# all tests (JUnit + Cucumber)
mvn test

# only the Cucumber suite
mvn test -Dtest=CucumberRunner

# by tag
mvn test -Dtest=CucumberRunner "-Dcucumber.filter.tags=@smoke"
mvn test -Dtest=CucumberRunner "-Dcucumber.filter.tags=@regression"
mvn test -Dtest=CucumberRunner "-Dcucumber.filter.tags=@desktop"
mvn test -Dtest=CucumberRunner "-Dcucumber.filter.tags=@smoke and @desktop"

# a single JUnit class
mvn test -Dtest=TesterProductTest
mvn test -Dtest=VisualizerAppTest

# headless (used by CI)
mvn test -Dheadless=true
```

> **Headed vs headless:** tests run **headed** by default (`headless=false`). Pass `-Dheadless=true` or set `HEADLESS=true` to run without a browser window.

### Tags

| Tag | Meaning |
|---|---|
| `@smoke` | Fast critical path – runs on every push |
| `@regression` | Full regression suite |
| `@desktop` | Scenarios using the 1920×1080 viewport |
| `@mobile` | Scenarios using the 375×667 viewport |
| `@purchase` | Tester purchase feature |
| `@visualizer` | Visualizer experience feature |

### Example feature file

```gherkin
@purchase @regression
Feature: Purchase a colour tester

  @smoke @desktop
  Scenario: Desktop customer adds a tester from the colour finder
    Given a desktop customer starts with an empty basket
    When the customer browses to shade "Gentle Lavender" from colour family "Violet"
    And the customer adds a tester to the basket
    Then the basket contains 1 item
    And the basket includes tester "Dulux Colour Tester" for shade "Gentle Lavender"
```

---

## 🖼 Visual Regression Testing

`VisualRegressionTest` checks that the colour finder, shade selection and cart pages haven't shifted layout, styling or content unexpectedly. Playwright's Java bindings don't ship the Node test runner's `toHaveScreenshot()` / snapshot-management utilities, so comparison is done with the self-hosted [`image-comparison`](https://github.com/romankh3/image-comparison) library instead — no external service, no account, no cost.

**How it works:**
- Baselines live in [`src/test/resources/visual-baselines/`](src/test/resources/visual-baselines) and are committed to the repo.
- The first run against a new snapshot name has nothing to compare against, so it captures the current screenshot as the baseline rather than failing.
- Every later run does a pixel diff against the baseline; on mismatch, a diff image (with the changed regions highlighted) is written to `target/visual-diffs/` and attached to the Allure report.
- To intentionally update a baseline after a real UI change, delete the corresponding PNG under `visual-baselines/` and re-run — it will be regenerated.

**CI:** runs as its own `visual-regression` job with `continue-on-error: true` — a real production layout change (or a stateful UI quirk, like a "last selected" checkbox carried over between runs) won't block the smoke suite while baselines stabilise. Diff images are uploaded as a `visual-diffs` artifact for review.

```bash
# run standalone
mvn test -Dtest=VisualRegressionTest
```

---

## 📊 Reports

Two reports are produced on every run, plus a screenshot attached to any failing scenario.

### 🌐 Live Allure report (GitHub Pages)

After every push to `main`, the Allure report is published to:

**➡️ https://magdau.github.io/playwright-java-dulux-uk/**

<div align="center">
  <img src="docs/images/allure-suites.png" alt="Allure suites / behaviours view" width="800">
  <br><em>Allure — scenarios, steps and durations</em>
</div>

#### Dashboard widgets

The published report is enriched with the standard Allure dashboard widgets:

| Widget | What it shows | How it's populated |
|---|---|---|
| 📈 **Trend** | Pass/fail history across builds | CI restores the previous run's `history/` from the `gh-pages` branch before generating, so the trend accumulates over time |
| 🗂️ **Categories** | Failures grouped into defect buckets (product defects, locator/timeout, navigation, infrastructure) | `categories.json` copied into `allure-results` |
| 🖥️ **Executors** | The CI build that produced the report, linked back to the GitHub Actions run | `executor.json` generated in CI from the run metadata |
| 🌱 **Environment** | Application, URL, browser and tooling versions | `environment.properties` copied into `allure-results` |

`categories.json` and `environment.properties` live under [`src/test/resources/allure/`](src/test/resources/allure/) and are written into `target/allure-results` at runtime by a `@BeforeAll` hook, so the **Categories** and **Environment** widgets appear in local, Docker and CI reports alike. The **Executors** and **Trend** widgets are populated by the CI pipeline only.

> ℹ️ The **Trend** chart needs at least two builds on `main` to show history — the first run seeds the baseline.

### 🥒 Cucumber HTML report

A standalone HTML report is also generated on every run at `target/cucumber-reports/report.html`.

### Generate locally

```bash
mvn test                # run tests → writes target/allure-results
mvn allure:report       # static HTML → target/site/allure-maven-plugin/
mvn allure:serve        # OR serve the live report in the browser
```

**What's recorded:** passed / failed / skipped scenarios, per-step durations, and a full-page screenshot attached to every failed scenario (visible in the Allure timeline).

> 📸 *The report screenshots above live in `docs/images/`. See `docs/images/README.md` for exactly which views to capture and how to regenerate them.*

For **Cucumber scenarios**, a screenshot is attached to the report automatically on failure (visible in Cucumber HTML report and Allure).

---

## ⚙️ GitHub Actions CI

The pipeline is defined in [`.github/workflows/e2e-tests.yml`](.github/workflows/e2e-tests.yml).

### When it runs

| Trigger | Default tag |
|---|---|
| Push to `main`, `feature/**` or `fix/**` | `@smoke` |
| Pull Request to `main` | `@smoke` |
| Manual (`workflow_dispatch`) | configurable tag expression |

> **Why `fix/**`?** Without this pattern, branches named `fix/something` are silently skipped by CI — pushes land with no feedback until a PR is opened. Adding `fix/**` alongside `feature/**` ensures the smoke suite runs on every active branch.

### Pipeline steps

1. Checkout + restore the previous **Allure history** from `gh-pages` (for the Trend widget).
2. Set up **Temurin JDK 21** (Maven cache).
3. Install the **Playwright Chromium** browser.
4. Run the smoke suite headless (`-Dheadless=true`).
5. Add Allure metadata — `environment.properties`, `categories.json`, `executor.json` and the carried-over history.
6. Generate the **Allure** report.
7. Upload artifacts (`cucumber-reports`, `allure-results`).
8. Publish the report to **GitHub Pages** (only on `main`).

### Run manually with a custom tag

**Actions** → **E2E Tests** → **Run workflow** → enter a `cucumber_tags` expression (e.g. `@regression`, `@smoke and @desktop`) → **Run workflow**.

---

## 👉 Future Improvements

**Done:**

- ✅ Extract duplicated `tearDown` / `setupMobile|Desktop` / `createSetup` into `BaseTest`
- ✅ Adopt the **Page Object Pattern** with inheritance via `BasePage`
- ✅ Remove assertions from page objects — all assertions now live in the test layer
- ✅ Introduce **AssertJ** for readable value/string assertions
- ✅ Add a **Cucumber BDD** layer (Cucumber 7 + PicoContainer DI, shared `CucumberContext`)
- ✅ Add `@smoke` / `@regression` tagging, runnable by expression
- ✅ Add **Allure** reporting with screenshot-on-failure, published to GitHub Pages
- ✅ Set up **GitHub Actions** CI (smoke on every push/PR, artifacts uploaded)
- ✅ Extend CI triggers to `fix/**` branches
- ✅ Collapse duplicated locator methods in `ColorSelectionPage` (`chooseColour` / `chooseShade`)
- ✅ Visual regression testing — pixel-diff comparison against committed baselines for the colour finder, shade selection and cart pages, run as a separate non-blocking CI job

**To improve:**

> A point-by-point backlog of concrete clean-ups and enhancements found in the current code.

*Code quality / DRY*

1. **De-duplicate the journey logic** — the same flows are implemented twice: once in the JUnit tests (`TesterProductTest` / `VisualizerAppTest` via `BaseTest`) and once in the Cucumber layer (`CucumberContext` + steps). Pick one source of truth, or have the JUnit tests reuse the business methods, so a UI change is fixed in one place.
2. **Single source for viewport sizes** — `1920×1080` / `375×667` are hard-coded in both `BaseTest` and `CucumberContext`. Extract to a shared constant/config.
3. **Remove the manual timestamped screenshots** — `TesterProductTest` / `VisualizerAppTest` write `Screenshots/.../<timestamp>.png` on *every* run (even passing ones). This duplicates Allure's screenshot-on-failure; attach to Allure instead or drop it.
4. **Tidy small smells** — unused `AriaRole` import in `HomePage`, and the `DUlUX_PAGE` constant typo (inconsistent casing).

*Test design / robustness*

5. **Wrap raw selectors in page objects** — `page.locator("pre")`, `page.locator("body")` and the OneTrust id `#onetrust-reject-all-handler` are used directly in tests/steps. Move them behind page-object methods so locators live in one layer.
6. **Make cookie consent resilient** — `rejectAllCookies()` clicks immediately; if the banner is slow or absent the run breaks. Guard with a presence/visibility check so it tolerates both states.
7. **Add a bounded retry policy for flaky steps** — running against live production, a small, explicit, *reported* retry (e.g. Surefire `rerunFailingTestsCount` or Cucumber retry) would cut false reds without hiding real failures.
8. **Centralise test data** — shades (`Gentle Lavender`), colour families (`Violet`) and expected URLs are scattered across features and tests. Externalise so the data matrix can grow without code edits.
9. **Reduce visual regression false positives** — the colour finder baseline can mismatch on stateful UI (e.g. a "last selected" checkbox carried over from a previous run). Consider clearing cookies/storage before the screenshot, or excluding that region from the diff.

*Coverage / scale*

10. **Cross-browser** — both `BaseTest` and `CucumberContext` hard-code `playwright.chromium()`. Parameterise to also run Firefox and WebKit.
11. **Parallel execution** — scenarios run serially today; enabling JUnit/Cucumber parallelism would shorten CI feedback.

*Tooling / CI*

12. **Enforce style in CI** — a Checkstyle config exists for the IDE but isn't run by Maven/CI. Wire the `maven-checkstyle-plugin` into the build so style is gated, not optional.
13. **Generate the Allure report inside Docker** — the container produces `allure-results` but leaves report generation manual; an optional step would make the image self-contained.

*Non-functional / advanced testing*

14. **Lighthouse CI** — add a `lhci` job to the GitHub Actions pipeline to audit performance, accessibility, SEO and best-practices budgets on the critical pages, with thresholds that fail the build on regression. Complements the functional suite with quality signals it doesn't currently measure (and gives a first, cheap accessibility check).
15. **Load / performance testing with k6** — model the critical revenue path (browse → select shade → add tester) as a [k6](https://k6.io/) script and run it on a schedule against a non-production environment to track latency and error rate under concurrency. Keep it out of the per-push pipeline (separate workflow / nightly) so it never gates fast feedback.

---

## 👩‍💻 Author

**Magdalena Ukleja**

[![GitHub](https://img.shields.io/badge/GitHub-magdaU-181717?logo=github&logoColor=white)](https://github.com/magdaU)

QA Automation Engineer — Java · Playwright · Cucumber BDD · CI/CD.

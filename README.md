<div align="center">

# 🎭 Playwright Java E2E Automation Framework

### UI end-to-end test automation for [Dulux UK](https://www.dulux.co.uk) — Java · Playwright · Cucumber BDD · Allure · CI/CD

[![E2E Tests](https://github.com/magdaU/playwright-java-dulux-uk/actions/workflows/e2e-tests.yml/badge.svg)](https://github.com/magdaU/playwright-java-dulux-uk/actions/workflows/e2e-tests.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📖 Overview

Java-based UI end-to-end automation framework for real Dulux UK customer journeys (buy a colour tester, launch the Visualizer app), tested with a Page Object Model + BDD architecture built on Playwright, Cucumber and Allure.

Also available as a [Python port](https://github.com/magdaU/playwright-python-dulux-uk) of this same project.

---

## 🧰 Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Playwright | 1.50 | Browser automation (Chromium, Firefox, WebKit) |
| JUnit 5 | – | Test runner, launched via `cucumber-junit-platform-engine` |
| Cucumber | 7.18 | BDD layer — Gherkin feature files → JUnit test items |
| PicoContainer (`cucumber-picocontainer`) | – | Dependency injection, one context instance per scenario |
| AssertJ | – | Fluent assertions |
| Allure (`allure-cucumber7-jvm`) | 2.29 | Test reporting with Gherkin step rendering |
| Maven | – | Build tool |
| Docker / Docker Compose | – | Containerised, reproducible test runs |
| GitHub Actions | – | CI/CD pipeline, GitHub Pages |

---

## 🏛 Architecture

**Page Object Model + Component Objects**, with Gherkin scenarios (`src/test/resources/features`) bound to Playwright actions via Cucumber step definitions. A per-scenario `Context`, injected with PicoContainer, carries shared state between step classes.

```
Gherkin / JUnit
       ↓
Cucumber Steps
       ↓
Cucumber Context
       ↓
Page Objects + Components
       ↓
Playwright
       ↓
Dulux UK
```

Full project structure and design rationale: [Architecture](docs/architecture.md).

---

## 🖼️ Visual Regression

Pixel-based visual regression testing detects unexpected UI changes in:

* colour finder;
* shade selection;
* shopping cart.

---

## ▶️ Run Tests

### Local

```bash
mvn test
```

### Smoke

```bash
mvn test -Dtest=CucumberRunner "-Dcucumber.filter.tags=@smoke"
```

### Docker

```bash
docker compose up --build
```

---

## 📚 Docs

- [Getting Started](docs/GETTING_STARTED.md) — what this project is, what it demonstrates, prerequisites, install & run, day-to-day developer/tester workflow.
- [Features Guide](docs/FEATURES_GUIDE.md) — a functional walkthrough of the site areas under test.
- [Test Strategy](docs/TEST_STRATEGY.md) — what we test, why, scope, risk analysis, coverage gaps, roadmap.
- [Test Plan](docs/TEST_PLAN.md) · [Test Cases](docs/TEST_CASES.md) · [Test Results](docs/TEST_RESULTS.md) · [Test Summary Report](docs/TEST_SUMMARY_REPORT.md) — the supporting QA artifacts for this suite.
- [Architecture](docs/architecture.md) — tech stack, design rationale, project structure, a full sample scenario walkthrough.
- [Lessons Learned](docs/LESSONS_LEARNED.md) — real issues this suite caught, root-caused and fixed.
- [Testing Without Requirements](docs/TESTING_WITHOUT_REQUIREMENTS.md) — how test scope is derived with no internal spec available.

---

## 🚀 Future Improvements

* Parallel test execution
* API testing
* Accessibility testing
* Improved test-data management
* AI-assisted test analysis

---

## 👩‍💻 Author

**Magdalena Ukleja**

[![GitHub](https://img.shields.io/badge/GitHub-magdaU-181717?logo=github&logoColor=white)](https://github.com/magdaU)

QA Automation Engineer — Java · Python · Playwright · BDD · CI/CD.

---

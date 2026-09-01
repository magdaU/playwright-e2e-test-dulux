# 🎭 Playwright Java — Dulux UK E2E Automation

### Java · Playwright · Cucumber BDD · JUnit 5 · Allure · CI/CD

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Playwright](https://img.shields.io/badge/Playwright-green)](https://playwright.dev/java/)
[![Cucumber](https://img.shields.io/badge/Cucumber-BDD-brightgreen)](https://cucumber.io/)
[![Allure](https://img.shields.io/badge/Allure-reporting-blue)](https://allurereport.org/)

## 📌 Overview

A Java-based end-to-end UI automation framework built with Playwright for selected Dulux UK customer journeys.

The project demonstrates maintainable test automation using **Page Object Model, Cucumber BDD, reusable components, visual regression, reporting and CI/CD**.

## 🧪 Test Coverage

| Area                   | Coverage |
| ---------------------- | -------- |
| Colour tester purchase | ✅        |
| Colour selection       | ✅        |
| Shopping cart          | ✅        |
| Visualizer             | ✅        |
| Desktop                | ✅        |
| Mobile                 | ✅        |
| BDD / Gherkin          | ✅        |
| Visual regression      | ✅        |
| Allure reporting       | ✅        |
| Docker                 | ✅        |
| GitHub Actions         | ✅        |

## 🏗️ Architecture

```text
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

### Design Patterns

* Page Object Model
* Component Objects
* Base Page / Base Test
* BDD with Gherkin
* Dependency Injection with PicoContainer
* AssertJ assertions

## 📊 Reporting & CI/CD

* Allure reporting
* Cucumber HTML reports
* Screenshots on failure
* GitHub Actions
* GitHub Pages report
* Dockerized test execution

## 🖼️ Visual Regression

Pixel-based visual regression testing is used to detect unexpected UI changes in:

* colour finder;
* shade selection;
* shopping cart.

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

## 🧰 Tech Stack

**Java 21 · Playwright · JUnit 5 · Cucumber · PicoContainer · AssertJ · Maven · Allure · Docker · GitHub Actions**

## 📚 Documentation

* [Test Strategy](docs/TEST_STRATEGY.md)
* [Architecture](docs/architecture.md)

## 🚀 Future Improvements

* Firefox / WebKit execution
* Parallel test execution
* API testing
* Accessibility testing
* Improved test-data management
* AI-assisted test analysis

> Portfolio project demonstrating Java-based UI automation and modern QA engineering practices.

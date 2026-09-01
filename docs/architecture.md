# Architecture

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

## Design patterns

- **Page Object Model (POM)** — each page is a class extending `BasePage` (shared `Page` field).
- **Component Objects** — reusable UI parts separated from full-page objects.
- **Base classes** — `BasePage` and `BaseTest` remove duplicated setup/teardown.
- **BDD (Given/When/Then)** — Cucumber scenarios describe behaviour, not implementation.
- **Dependency Injection** — PicoContainer injects `CucumberContext` into every step class.

## Project structure

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

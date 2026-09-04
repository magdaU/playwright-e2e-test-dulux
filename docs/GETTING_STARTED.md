# Getting Started

## Overview

A Java end-to-end UI automation framework for real [Dulux UK](https://www.dulux.co.uk)
customer journeys (buy a colour tester, launch the Visualizer app), built on a Page Object
Model + Cucumber BDD architecture with Playwright, JUnit 5 and Allure.

> **Status:** implemented and passing in CI — 4 Cucumber scenarios (desktop + mobile
> `purchase`, desktop + mobile `visualizer`), plus 3 non-blocking visual regression checks.

📚 **Docs:** [Features Guide](FEATURES_GUIDE.md) (functional walkthrough of the site areas
under test) · [Test Strategy](TEST_STRATEGY.md) (what we test, why, scope, risk analysis,
roadmap) · [Test Plan](TEST_PLAN.md) · [Test Cases](TEST_CASES.md) ·
[Test Results](TEST_RESULTS.md) · [Test Summary Report](TEST_SUMMARY_REPORT.md) ·
[Architecture](architecture.md) (tech stack, design rationale, project structure) ·
[Lessons Learned](LESSONS_LEARNED.md) · [Testing Without Requirements](TESTING_WITHOUT_REQUIREMENTS.md).

## What This Project Demonstrates

This isn't a toy TODO-app suite — it's built against a real, live production e-commerce
site, which surfaces the same problems a professional QA/SDET role deals with day to day:

- **Risk-based test strategy, not just test scripts** — [Test Strategy](TEST_STRATEGY.md)
  defines scope with explicit trade-offs (what's deliberately *out*, and why), a
  prioritised risk register, and entry/exit criteria — the kind of document a team lead
  reviews before trusting a suite's "green".
- **Real issues found and fixed, not simulated ones** — this suite has caught genuine
  problems while it was being built: a click resolving against a still-loading page after
  a full navigation, a hidden duplicate element on the same page confusing a locator, two
  page-object methods that had silently become copies of each other, and a CI reporting
  step that succeeded while publishing the wrong report. Each is root-caused and
  documented in [Lessons Learned](LESSONS_LEARNED.md) rather than papered over with a
  blanket retry.
- **Judgement calls, documented** — visual regression was implemented here as a
  non-blocking check, a **deliberately different call** from the
  [Python sibling project](https://github.com/magdaU/playwright-python-dulux-uk), which
  deferred the same feature for a different, equally defensible reason — see
  [Lessons Learned #4](LESSONS_LEARNED.md#4-visual-regression-implemented-here-deferred-in-the-python-sibling).
- **Engineering discipline in the framework itself** — Page Object Model with no
  assertions in page objects, role-based locators over brittle CSS/XPath, web-first waits
  instead of sleeps, and shared locator logic collapsed rather than duplicated.
- **CI/CD pipeline design, not just a green checkmark** — a blocking smoke gate and a
  separate, non-blocking visual regression job, kept apart deliberately so a real
  production layout change can't redden the push/PR gate, with Allure history/trend
  published to GitHub Pages.
- **Stack-agnostic QA design** — the same architecture and strategy exist in a
  [Python/pytest-bdd sibling project](https://github.com/magdaU/playwright-python-dulux-uk),
  showing this is a transferable way of thinking about test design, not a one-language
  trick — see [Testing Without Requirements](TESTING_WITHOUT_REQUIREMENTS.md) for how the
  two projects cross-check each other in place of a spec neither has.

## Key Features

- 🧱 **Page Object Model + Component Objects**, no assertions in page objects — all
  Playwright web-first assertions and AssertJ calls live in the step/test layer.
- 🥒 **BDD with Cucumber** — Gherkin `@tag`s drive `-Dcucumber.filter.tags`, run via
  `CucumberRunner` on the JUnit Platform.
- 🧪 **Dual entry point** — the same journeys also exist as plain JUnit 5 tests
  (`TesterProductTest`, `VisualizerAppTest`) with rich Allure annotations (`@Epic`,
  `@Feature`, `@Story`, `@Severity`), for fine-grained Allure storytelling alongside the
  BDD living documentation.
- 📱 **Cross-viewport coverage** — `purchase` and `visualizer` at desktop `1920×1080` and
  mobile `375×667`, via `CucumberContext.useViewport(...)`.
- 🖼️ **Visual regression** — three key pages (empty cart, colour finder, Violet shade
  grid) pixel-diffed against committed baselines, wired as its own non-blocking CI job.
- 📸 **Screenshot on failure** — attached automatically to every failed scenario/test via
  `CucumberHooks`, making failures self-explanatory without re-running locally.
- 📊 **Allure reporting** published to GitHub Pages via CI, plus a standalone Cucumber
  HTML/JSON/XML report and 🐳 **Docker/Compose** for a reproducible run.

See [Architecture](architecture.md) for the full feature list and the reasoning behind
each design choice.

## Prerequisites

- **Java 25**
- **Maven**
- Internet access (tests run against `dulux.co.uk`)

## Install & run

```bash
mvn -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium"

mvn test-compile              # confirms feature/step wiring compiles without a browser
mvn -Dtest=CucumberRunner "-Dcucumber.filter.tags=@smoke" test   # fast critical-path set
mvn test                      # full suite
```

Full tag reference and more CLI examples are in
[Architecture — Tags / markers reference](architecture.md#tags--markers-reference).

## Run in Docker

```bash
docker compose up --build
CUCUMBER_TAGS="@regression" docker compose up --build   # a different tag expression
```

Allure results and Cucumber reports are written back to the host under
`./target/allure-results` and `./target/cucumber-reports`.

## Reports

```bash
mvn test                       # produces target/allure-results
allure serve target/allure-results
```

In CI, the report is generated automatically (via the Allure CLI, not the Maven plugin —
see [Lessons Learned #3](LESSONS_LEARNED.md#3-a-green-ci-step-can-still-publish-the-wrong-report)
for why) and published to GitHub Pages on every push to `main`.

### Regenerating visual regression baselines

**Always regenerate via Docker — never on a local Windows machine.** CI runs Ubuntu;
Windows and Linux render fonts differently enough to fail every check even when nothing
actually changed (see [Lessons Learned #7](LESSONS_LEARNED.md#7-visual-regression-failed-33-on-ci--two-environment-root-causes-both-fixed)):

```bash
rm src/test/resources/visual-baselines/*.png
docker build -t dulux-e2e-baseline -f Dockerfile .
docker run --rm --shm-size=1gb \
  -v "$(pwd)/src/test/resources/visual-baselines:/app/src/test/resources/visual-baselines" \
  dulux-e2e-baseline \
  sh -c "mvn -B -Dheadless=true -Dtest=VisualRegressionTest test"
```

`VisualComparisonUtil` writes a screenshot as the new baseline whenever the file doesn't
exist yet, so deleting the old ones first and running the container once is enough to
bootstrap fresh ones. Commit the regenerated PNGs along with whatever page-object or
feature change prompted the refresh.

## Working with the Project (Developer / Tester Guide)

### Project structure

See [Architecture — Project structure](architecture.md#project-structure) for the full
directory layout (`cucumber/`, `page/`, `support/`, `tests/`). In short:

- **`src/test/resources/features/*.feature`** — Gherkin scenarios (business-readable, no
  Playwright code).
- **`src/test/java/.../cucumber/steps/`** — step definitions binding Gherkin steps to
  `CucumberContext`/page-object calls.
- **`src/test/java/.../page/`** — Page Object Model + Component Objects; navigation and
  interaction only, no assertions.
- **`src/test/java/.../cucumber/CucumberContext.java`** — shared browser state and
  business-level methods, injected into every step class with PicoContainer.
- **`src/test/java/.../tests/`** — the parallel plain-JUnit entry point and
  `VisualRegressionTest`.

### Adding or changing a scenario

1. Write/edit the Gherkin in `src/test/resources/features/*.feature` — keep it
   business-readable, no selectors or technical detail.
2. Add or reuse step definitions in `cucumber/steps/`, calling into `CucumberContext` /
   page objects — never assert inside a page object.
3. Tag the scenario with the relevant `@tag`s (see
   [Tags / markers reference](architecture.md#tags--markers-reference)) so it's picked up
   by the right slice.
4. Run it locally before pushing:
   ```bash
   mvn test-compile                                       # sanity-check step defs wire up
   mvn -Dtest=CucumberRunner "-Dcucumber.filter.tags=@yourtag" test
   ```

### Everyday commands

```bash
mvn -Dtest=CucumberRunner "-Dcucumber.filter.tags=@smoke and @desktop" test   # narrow by tag
mvn -Dtest=CucumberRunner "-Dcucumber.filter.tags=@purchase" test             # one journey
mvn -Dheadless=false test                                                    # watch it run headed
mvn -Dtest=VisualRegressionTest test                                         # visual checks only
```

### Debugging a failing test

- Run with `-Dheadless=false` to watch the browser interact with the real site.
- `CucumberHooks` attaches a full-page screenshot to Allure on every scenario failure —
  `allure serve target/allure-results` after a run shows it inline, no local re-run needed.
- For a step-by-step trace, add `page.pause()` temporarily in the relevant page object or
  step definition (headed mode only) to drop into the Playwright Inspector.

### Git workflow

- Branch off `main` for each change (`feature/…`, `fix/…`, `docs/…`).
- Open a PR into `main` — the smoke suite and the non-blocking visual regression job run
  automatically on every push and PR.
- `main` merges trigger the Allure report publish to GitHub Pages.

### Where to look next

- [Features Guide](FEATURES_GUIDE.md) — what each site area under test actually does,
  reverse-engineered from the live site (see
  [Testing Without Requirements](TESTING_WITHOUT_REQUIREMENTS.md) for why that's necessary
  here).
- [Architecture](architecture.md) — full tech stack, design rationale, and the tags
  reference.
- [Test Strategy](TEST_STRATEGY.md) — scope, risk register, coverage gaps, and the
  reasoning behind choices like the non-blocking visual regression job.
- [Test Plan](TEST_PLAN.md) — execution-facing scope, schedule, environments and
  entry/exit criteria.
- [Test Cases](TEST_CASES.md) — individual test case specs, automated and manual/candidate.
- [Test Results](TEST_RESULTS.md) / [Test Summary Report](TEST_SUMMARY_REPORT.md) — latest
  known execution status and cycle-level outcome.
- [Lessons Learned](LESSONS_LEARNED.md) — real incidents this suite has hit (a navigation
  race condition, a duplicated locator, a CI report published from the wrong path), each
  with root cause, fix, and the takeaway.

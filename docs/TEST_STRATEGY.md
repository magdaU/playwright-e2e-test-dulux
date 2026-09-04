# 🧭 Test Strategy — Dulux UK E2E Automation

> Living document. It describes **what** we test, **why**, and **how** the automation
> framework in this repository is designed to deliver fast, trustworthy feedback on the
> [Dulux UK](https://www.dulux.co.uk) customer journeys. See also: [Getting Started](GETTING_STARTED.md) ·
> [Features Guide](FEATURES_GUIDE.md) · [Test Plan](TEST_PLAN.md) · [Test Cases](TEST_CASES.md) ·
> [Test Results](TEST_RESULTS.md) · [Test Summary Report](TEST_SUMMARY_REPORT.md) ·
> [Architecture](architecture.md) · [Lessons Learned](LESSONS_LEARNED.md) ·
> [Testing Without Requirements](TESTING_WITHOUT_REQUIREMENTS.md).

| | |
|---|---|
| **Product under test** | Dulux UK e-commerce website (`https://www.dulux.co.uk`) |
| **Test type** | UI end-to-end (black-box, browser-driven) |
| **Framework** | Playwright for Java · Cucumber BDD · JUnit 5 · Allure |
| **Pipeline** | GitHub Actions → smoke suite on every push/PR, report published to GitHub Pages |
| **Owner** | QA / SDET |
| **Status** | Implemented and passing in CI — 4 Cucumber scenarios (desktop + mobile `purchase`, desktop + mobile `visualizer`) plus 3 non-blocking visual regression checks. A [Python port](https://github.com/magdaU/playwright-python-dulux-uk) of this same suite exists as an independent second implementation against the same site. |

---

## 1. Purpose & objectives

The goal of this automation is **not** to test everything the Dulux site does — it is to
continuously prove that the **highest-value customer journeys still work** across the
viewports our customers actually use.

Quality objectives, in priority order:

1. **Protect revenue paths** — a customer must always be able to find a colour and add a
   tester to the basket. This is the critical, money-making flow.
2. **Protect cross-device parity** — the same journey must work on desktop and mobile,
   which use *different* navigation (top nav vs. hamburger menu).
3. **Fast, actionable feedback** — every push runs the smoke suite headless in CI in
   minutes, with a screenshot on any failure and a published Allure report.
4. **Trustworthy results** — a red build means a real regression, not a flaky test. Test
   stability is treated as a first-class feature, not an afterthought.

---

## 2. System under test (SUT)

The SUT is the **live, public production** Dulux UK website. This is deliberate (it gives
realistic coverage) but it is also the single biggest source of risk (see §10).

Characteristics that shape the test design:

- JavaScript-heavy SPA-style storefront with client-side navigation.
- A cookie consent banner that blocks interaction until dismissed.
- "Find a colour" flow that triggers a **full page navigation** (not a dropdown).
- A Visualizer link that opens in a **new browser tab**.
- Responsive layouts: desktop exposes a top navigation bar; mobile collapses it behind a
  hamburger menu.
- Third-party dependencies (analytics, the Adjust-powered Visualizer) that can return
  environment-specific messages.

---

## 3. Scope

### ✅ In scope

| Area | Covered journeys |
|---|---|
| **Tester purchase** | Browse to a shade via the colour finder and add a tester to the basket (desktop + mobile) |
| **Visualizer experience** | Open the Visualizer from a selected shade page (desktop opens a new tab; mobile surfaces the store-data message) |
| **Cross-viewport** | Every journey runs at desktop `1920×1080` and mobile `375×667` |
| **Cookie consent** | Implicitly exercised — every journey rejects cookies before proceeding |

### ❌ Out of scope (for this suite)

- Checkout, payment and order fulfilment (no transactions against production).
- Account creation, login and profile management.
- API / service-level, contract, unit and component testing (the app is third-party; we
  own no production code to unit-test).
- Performance, load and stress testing.
- Accessibility (a11y) and full cross-browser matrix — **candidates for the roadmap (§13)**.

### 🖼️ Visual regression

Pixel-diff regression *is* in scope, unlike a plain accessibility/cross-browser gap — it
runs as its own **non-blocking** CI job (`continue-on-error: true`) against three key
pages (empty cart, colour finder landing page, Violet shade grid), comparing against a
committed baseline under `src/test/resources/visual-baselines/` via
[`VisualComparisonUtil`](../src/test/java/com/github/magdalena/support/VisualComparisonUtil.java)
(the `image-comparison` library — Playwright's Java bindings don't ship the Node runner's
`toHaveScreenshot()` snapshot tooling, so this is a deliberate, dependency-light
alternative). See [Architecture](architecture.md) for how it's wired.

> **Baselines must be regenerated via Docker, never on a local Windows machine.**
> Cross-platform font rendering differences (and a cookie-banner CSS transition
> Playwright's `ScreenshotAnimations.DISABLED` now accounts for) caused every check to
> fail on CI until the baselines were captured inside the project's own Docker image
> instead — see [Lessons Learned #7](LESSONS_LEARNED.md#7-visual-regression-failed-33-on-ci--two-environment-root-causes-both-fixed)
> for the full root-cause writeup and [Getting Started — Reports](GETTING_STARTED.md#reports)
> for the regeneration command.

> **A different call than the Python sibling.** The [Python port](https://github.com/magdaU/playwright-python-dulux-uk)
> considered the same feature and **deliberately deferred** it, reasoning that a
> production site whose layout is still visibly drifting would make snapshot baselines go
> stale immediately, producing noise rather than signal. This project made the opposite
> call: implemented it now, but scoped to non-blocking so a real layout change can't
> redden the `smoke` gate while a baseline is refreshed. Both are defensible; the
> difference is the accepted trade-off (early coverage with occasional baseline-refresh
> noise, vs. no coverage until the site looks stable) — see [Lessons Learned](LESSONS_LEARNED.md#4-visual-regression-implemented-here-deferred-in-the-python-sibling).

> **Note on the test pyramid.** This repository is intentionally an **E2E layer only**,
> because we do not own the application code. We compensate for the known cost of E2E
> (slower, more brittle) by keeping the suite small, journey-focused, and tag-sliced so the
> critical `@smoke` set stays fast.

---

## 4. Test approach

### 4.1 Levels & style

- **Behaviour-Driven (BDD)** — journeys are described in business-readable Gherkin
  (`*.feature`) so intent is reviewable by non-engineers. Cucumber step definitions are the
  glue; the [`CucumberRunner`](../src/test/java/com/github/magdalena/cucumber/CucumberRunner.java)
  drives them via the JUnit Platform.
- **Parallel JUnit tests** — the same journeys also exist as plain JUnit 5 tests
  (`TesterProductTest`, `VisualizerAppTest`) annotated with rich Allure metadata
  (`@Epic`, `@Feature`, `@Story`, `@Severity`). This gives two entry points: BDD for
  living documentation, JUnit for fine-grained Allure storytelling.

### 4.2 Design principles

| Principle | How it's applied |
|---|---|
| **Page Object Model** | Each page (`HomePage`, `ColorSelectionPage`, `CartPage`) and reusable component (`NavigationComponent`, `AlertComponent`) extends a shared `BasePage`. UI locators live in one place. |
| **No assertions in page objects** | Pages only *act* and *expose* locators. All assertions live in the test/step layer (AssertJ + Playwright web-first assertions). |
| **Single shared state** | `CucumberContext` holds browser/viewport state and high-level business methods, injected into every step class via PicoContainer DI. |
| **Role-based locators** | Locators prefer `getByRole` / `getByLabel` / `getByText` over brittle CSS/XPath, matching how a user perceives the page and surviving DOM churn. |
| **Web-first waits** | Playwright's auto-waiting assertions replace manual sleeps; explicit `waitForLoadState()` is used only where a real navigation occurs. |

### 4.3 Test design techniques

- **Scenario / user-journey based** — each test mirrors a real customer task end to end.
- **Equivalence partitioning** — one representative shade (`Violet Morning` / `Violet`)
  stands in for the colour-finder space; the *flow*, not the data permutation, is the risk.
- **State verification** — basket starts empty → exactly 1 item with the right product and
  shade after adding (guards against silent over/under-counting).
- **Cross-configuration testing** — desktop vs. mobile as distinct navigation paths.

---

## 5. Test data strategy

- **Data is inline and self-describing** in the Gherkin and tests (shade name, colour
  family, expected product label) — no external fixtures to drift out of sync.
- **No persistent data is created** on production: the basket flow stops *before* checkout,
  and each scenario runs in a fresh, isolated `BrowserContext` (no shared cookies/storage),
  so runs never contaminate one another.
- **Self-cleaning** — because no order is placed and contexts are disposed after every
  scenario, there is no teardown/data-reset burden.

---

## 6. Environments

| Environment | Where | Purpose |
|---|---|---|
| **Local (headed)** | Developer machine, `headless=false` by default | Authoring and debugging — watch the journey run |
| **Local (headless)** | `-Dheadless=true` | Fast local verification before pushing |
| **Docker** | `docker compose up --build` | Reproducible run matching CI exactly (Java 25 + Chromium, `shm_size: 1gb`) |
| **CI** | GitHub Actions `ubuntu-latest`, headless | Gate on every push/PR; publishes the Allure report |

The `HEADLESS` flag is resolved by [`PlaywrightConfig`](../src/test/java/com/github/magdalena/support/PlaywrightConfig.java)
from the `headless` system property, falling back to the `HEADLESS` env var — so the same
code runs identically across all four environments.

---

## 7. Tooling

| Concern | Tool |
|---|---|
| Browser automation | Playwright for Java (Chromium) |
| Test runner | JUnit 5 (Jupiter + Platform Suite) |
| BDD | Cucumber 7 |
| Assertions | Playwright web-first assertions + AssertJ |
| Dependency injection | PicoContainer (Cucumber) |
| Reporting | Allure + Cucumber HTML/JSON/JUnit XML |
| CI/CD | GitHub Actions, GitHub Pages |
| Containerisation | Docker / Docker Compose |
| Build | Maven |

---

## 8. Test selection & tagging strategy

Tags are the contract between "what changed" and "what we run":

| Tag | Meaning | When it runs |
|---|---|---|
| `@smoke` | Minimal critical-path set, fast | **Every push & PR** (CI default) |
| `@regression` | Full journey coverage | On demand / scheduled / pre-release |
| `@desktop` | Desktop-viewport variant | Filtered as needed |
| `@mobile` | Mobile-viewport variant | Filtered as needed |
| `@purchase`, `@visualizer` | Feature grouping | Targeted debugging of one journey |
| `@visual` (JUnit `@Tag`, not a Cucumber tag) | Pixel-diff checks | Own non-blocking CI job, run standalone with `-Dtest=VisualRegressionTest` |

Selection is driven by `-Dcucumber.filter.tags=...` (CI defaults to `@smoke`, overridable
via the `workflow_dispatch` input or the `CUCUMBER_TAGS` env var in Docker).

---

## 9. CI/CD & reporting

**Pipeline** ([`.github/workflows/e2e-tests.yml`](../.github/workflows/e2e-tests.yml)) — two jobs:

- `cucumber-smoke` (blocking): checkout → JDK 25 → install Chromium → run smoke suite
  headless → generate Allure report → upload artifacts → publish to GitHub Pages (on
  `main`).
- `visual-regression` (`continue-on-error: true`, non-blocking): checkout → JDK 25 →
  install Chromium → run `VisualRegressionTest` → upload any pixel diffs as a build
  artifact. Deliberately separate from the smoke job so a real production layout change
  can't redden the push/PR gate — see §3.

**Reporting layers:**

- **Allure** — the primary dashboard, enriched with **Trend** (history carried across builds
  via `gh-pages`), **Categories** (failure buckets), **Executors** (the CI run that produced
  it) and **Environment** widgets.
- **Cucumber HTML/JSON/XML** — a standalone report plus machine-readable output.
- **Screenshot on failure** — a full-page screenshot is automatically attached to every
  failed scenario (`CucumberHooks`) and to the JUnit tests, making failures self-explanatory.

**Key metrics tracked:** pass/fail rate and trend, per-step duration, failure categories,
and flaky-test signals (a test that fails then passes on re-run without a code change).

---

## 10. Risk analysis & mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| **Testing against live production** — content/layout/availability can change at any time | High | High | Role/label/text-based locators that tolerate DOM change; small focused suite; treat unexpected failures as *signal* and triage fast |
| **Cookie banner / consent variations** block interaction | Medium | High | Cookies are explicitly rejected at the start of every journey |
| **Flakiness** from network, animations, third-party scripts | Medium | High | Playwright auto-waiting, isolated `BrowserContext` per scenario, `shm_size: 1gb` in Docker to prevent Chromium crashes, explicit `waitForLoadState()` on real navigations |
| **Third-party Visualizer/Adjust** returns environment-specific messages | Medium | Medium | Mobile scenario asserts the known store-data message rather than assuming success; behaviour is documented, not hidden |
| **New-tab handling** for the Visualizer | Low | Medium | `context.waitForPage(...)` captures the popup deterministically on desktop |
| **No test data isolation guarantees on prod** | Low | Medium | Flow stops before checkout; no orders created; fresh context per scenario |
| **Single browser (Chromium) only** | Medium | Low | Accepted for now; cross-browser is on the roadmap (§13) |
| **Product catalogue drift** — a shade this suite relies on can be removed or restructured by the retailer without notice | Medium | High | **Materialised** (2026-09-04): "Gentle Lavender" was found removed from the "Violet" family while re-verifying the suite after the Java 25 upgrade — every purchase-journey test that browsed to it via the colour finder timed out. Confirmed on the live site (screenshot) and fixed by refreshing test data to "Violet Morning", present in the catalogue. The [Python sibling](https://github.com/magdaU/playwright-python-dulux-uk) had already hit the *same* removal independently, refreshing to the *same* replacement shade — see [Lessons Learned #5](LESSONS_LEARNED.md#5-product-catalogue-drift-materialised-here-too) |
| **Basket markup drift** — the basket quantity locator's uniqueness can be broken by a redesign | Medium | High | **Materialised** (2026-09-04): immediately after fixing the catalogue-drift row above, `CartPage.getQuantity()`'s `page.getByLabel("Quantity")` started failing with a Playwright strict-mode violation — the control had been redesigned into a `group` wrapping decrease/input/increase elements, all exposing an accessible name containing "Quantity". This was flagged as a foreseeable risk (§14) copied from the Python sibling's identical incident before it happened here; fixed the same way, by narrowing to `getByRole(AriaRole.SPINBUTTON, name="Quantity input")` — see [Lessons Learned #6](LESSONS_LEARNED.md#6-basket-markup-drift-materialised-here-too) |
| **Navigation resolves the wrong element on a full page navigation** | Materialised | High | **Fixed** (2026-05-11): `clickDropdownFindColour()` triggers a full page navigation rather than a dropdown reveal; without an explicit `page.waitForLoadState()` afterwards, the next click resolved against the still-loading outgoing page and timed out. A same-day intermediate fix (filtering `clickFindColour()`'s locator to `visible=true`, to dodge a hidden duplicate "Find a colour" tab-link elsewhere on the page) was superseded once the real root cause — the missing load-state wait — was identified; see [Lessons Learned #1](LESSONS_LEARNED.md#1-navigation-resolves-a-stale-or-hidden-element-after-a-full-page-navigation) |
| **Duplicated locator methods silently drift apart** | Materialised | Low | **Fixed** (2026-07-09): `chooseColour()` and a typo'd `choseSpecificTypeColor()` in `ColorSelectionPage` had identical bodies — a change to one without the other would have silently broken only half of the affected scenarios. Collapsed into a shared `clickButtonByName()` private helper; see [Lessons Learned #2](LESSONS_LEARNED.md#2-duplicated-locator-methods-that-could-silently-drift-apart) |
| **CI reporting tool produces a different output path than the pipeline expects** | Materialised | Medium | **Fixed** (2026-05-06): `mvn allure:report` (the Maven plugin) writes to `target/site/allure-maven-plugin`, not the `target/allure-report` path the GitHub Pages publish step expected — the job appeared to succeed while publishing an empty/stale report. Switched to the Allure CLI (`allure generate ... -o target/allure-report`) with an explicit output directory; see [Lessons Learned #3](LESSONS_LEARNED.md#3-a-green-ci-step-can-still-publish-the-wrong-report) |

---

## 11. Entry & exit criteria

**Entry (a build is allowed to run the suite):**

- Code compiles (`mvn test-compile`) and the Chromium browser installs successfully.
- The target environment (`HEADLESS`, viewport) is resolvable.

**Exit (a build/release is considered green):**

- 100% of `@smoke` scenarios pass on both viewports.
- No new entries in the Allure **Categories** "Product defects" bucket.
- Any failure is triaged to a root cause (real regression vs. environment/flake) before the
  result is trusted.

---

## 12. Roles & responsibilities

| Role | Responsibility |
|---|---|
| **SDET / QA** | Own the framework, author scenarios, triage failures, keep the suite stable |
| **Reviewers** | Read Gherkin to confirm scenarios describe the *right* behaviour |
| **CI** | Run the smoke gate on every push/PR and publish the report |

---

## 13. Maintenance & roadmap

The framework is intentionally small and clean so it stays cheap to maintain. Planned
improvements, roughly in priority order:

- [x] **Visual regression** — `VisualRegressionTest` snapshot-compares the empty cart,
  colour finder and Violet shade grid against committed baselines, wired as its own
  non-blocking CI job. See §3 for why this project made the opposite call from its
  [Python sibling](https://github.com/magdaU/playwright-python-dulux-uk), which deferred
  the same feature.
- [x] **Harden the basket quantity locator** — fixed 2026-09-04, the same day it was
  flagged, once the risk materialised for real. See §10 ("Basket markup drift") and
  [Lessons Learned #6](LESSONS_LEARNED.md#6-basket-markup-drift-materialised-here-too).
- [ ] **Cross-browser** — add Firefox and WebKit projects to widen real coverage.
- [ ] **Accessibility checks** — integrate an a11y scan into the critical journeys.
- [ ] **Tablet viewport** — a third breakpoint between mobile and desktop.
- [ ] **Retry policy for known-flaky steps** — bounded, explicit, and reported (never silent).
- [ ] **Scheduled regression run** — nightly `@regression` against production to catch drift.
- [ ] **Centralised test data** — externalise shades/products if the data matrix grows.

---

## 14. Coverage gaps & improvement opportunities

Areas deliberately left untested today (§3), plus what the risk actually is if they stay
uncovered and a concrete way each could be tested — not just a note that it's "future work".

| Area | Risk if left untested | How it could be tested | Priority |
|---|---|---|---|
| **Checkout / payment / order fulfilment** | A broken checkout ships unnoticed until a customer complaint or a revenue drop is reported — the single biggest gap given this is an e-commerce site | Not against production (no real transactions). Would need a retailer-provided staging/sandbox environment with a test payment provider before this is testable at all | High — blocked on environment access, not effort |
| **Only one colour family / shade path exercised** (`Violet` / "Violet Morning") | A shade with no tester option, or a family with an unusual layout, could break the flow without being caught — this project's *previous* pinned shade ("Gentle Lavender") was in fact removed from the catalogue (§10) | Turn the purchase scenario into a Scenario Outline with 2-3 more family/shade pairs, including an edge case | Medium |
| **Basket edit/remove flows** | Only *adding* a tester is verified; incrementing/decrementing quantity or removing an item is unverified | Extend `CartPage` with increment/decrement/remove actions and add a `@regression`-tagged scenario asserting basket state after each | Medium |
| **Site search** | `NavigationComponent` exposes `searchClickOnPage()`/`inputColorOnSearchBoxAndEnter()` and it's exercised as setup for the Visualizer scenarios, but no scenario asserts search *itself* works for an arbitrary term | Add a scenario: search for a known shade, assert it navigates to that shade's detail page | Low–Medium |
| **Negative / error-state paths** (e.g. a failed add-to-basket request) | The suite only proves the happy path; a customer-visible error state is invisible to it today | Use Playwright's `page.route()` to intercept and force an error response on the add-to-basket call, then assert the UI surfaces it | Medium |
| **Full cross-browser coverage on every push** | A Firefox/WebKit-only regression ships on `main` undetected until someone runs the suite manually against another engine | Deliberate trade-off to protect push/PR speed (§10, single-browser risk). If it becomes a real problem, add the two `@smoke` scenarios to a small WebKit job rather than the whole suite | Low (accepted trade-off) |
| **API / contract-level testing** | None today — a backend contract change could break the UI with only this slow, browser-driven E2E layer to catch it | Not currently actionable: Dulux is a third-party site exposing no API we're entitled to test against | Not planned |
| **Performance / load** | A slow-loading shade page degrades conversion without failing any functional assertion here | Out of scope for a UI E2E suite by design; would need a dedicated tool (Lighthouse CI, k6) against a staging environment | Not planned |

---

> **Guiding principle:** keep the suite **small, fast, and trustworthy**. A test that is
> flaky or slow is worse than no test, because it erodes confidence in the green build.

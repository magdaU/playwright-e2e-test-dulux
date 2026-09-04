# 🧩 Features Guide

> A functional walkthrough of the Dulux UK areas this suite exercises — written from the
> outside, by exploring the live site, since no internal spec or design doc is available
> for a third-party production site (see
> [Testing Without Requirements](TESTING_WITHOUT_REQUIREMENTS.md) for the approach behind
> that). This is the closest thing this project has to a functional spec, and it doubles
> as an onboarding map: where each feature lives in the code is noted alongside what it
> does.

---

## Home page & cookie consent

**What it is:** `https://www.dulux.co.uk` — the entry point for every journey.

**Key behaviour:**
- A cookie-consent banner (OneTrust) blocks interaction with the rest of the page until
  dismissed. Every scenario rejects it first (`rejectAllCookies()` —
  `#onetrust-reject-all-handler`).
- Hosts the global navigation (see below).

**Code:** [`page/pom/HomePage.java`](../src/test/java/com/github/magdalena/page/pom/HomePage.java)

---

## Global navigation

**What it is:** the top-level nav, present on every page, with different affordances per
viewport.

**Key behaviour:**
- **Desktop** exposes a visible top nav bar, including a **"Find a colour"** entry.
- **Mobile (`375×667`)** collapses the same nav behind a **hamburger ("Menu")** button.
- **"Find a colour"** triggers a **full page navigation**, not a dropdown/panel — a real
  gotcha that required an explicit `page.waitForLoadState()` before the next interaction,
  or it resolves against the outgoing page — see
  [Lessons Learned #1](LESSONS_LEARNED.md#1-navigation-resolves-a-stale-or-hidden-element-after-a-full-page-navigation).
- A **search box** (`search-field`) accepts free text and submits on Enter — used to jump
  directly to a known shade page without walking the colour-family tree.
- A **Shopping Cart** link opens the basket.

**Code:** [`page/component/NavigationComponent.java`](../src/test/java/com/github/magdalena/page/component/NavigationComponent.java)

---

## Find a Colour / colour finder

**What it is:** the family → shade drill-down reached via **"Find a colour"**.

**Key behaviour:**
- Colour **families** (e.g. "Violet") and individual **shades** (e.g. "Gentle Lavender")
  are each rendered as a named button — selected via `getByRole(BUTTON, name=...)`, shared
  between `chooseColour()` and `chooseShade()` via a single `clickButtonByName()` helper
  (see [Lessons Learned #2](LESSONS_LEARNED.md#2-duplicated-locator-methods-that-could-silently-drift-apart)
  for why that helper exists rather than two near-identical methods).
- Not every shade offers the same actions further down the funnel — the
  [Python sibling](https://github.com/magdaU/playwright-python-dulux-uk) found the first
  shade its catalogue listed under "Violet" had no tester purchase option at all, which is
  why this suite pins a specific, verified shade name rather than picking arbitrarily
  (see [Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities)).

**Code:** [`page/pom/ColorSelectionPage.java`](../src/test/java/com/github/magdalena/page/pom/ColorSelectionPage.java) (`chooseColour`, `chooseShade`)

---

## Shade / colour detail page

**What it is:** the page for one specific shade, reached either via the colour finder or
directly via search.

**Key actions available on this page:**
- **"Buy a Tester in this colour"** — adds a tester for this shade to the basket and shows
  a dismissible confirmation alert (`getByRole(ALERT)`).
- **"Try our Visualizer App"** (a list item containing a link) — opens the Visualizer
  experience. **On desktop** this opens in a genuinely new browser tab (captured via
  Playwright's `context.waitForPage(...)`); **on mobile** it does not — see Visualizer,
  below.

**Code:** [`page/pom/ColorSelectionPage.java`](../src/test/java/com/github/magdalena/page/pom/ColorSelectionPage.java)

---

## Basket / Cart

**What it is:** `https://www.dulux.co.uk/en/store/cart` — where an added tester shows up.

**Key behaviour:**
- An **empty basket** shows the literal text "Your basket is empty".
- Once an item is added, a **quantity control** is present — exposed via
  `getByLabel("Quantity")`. This substring-based label match is a known-fragile pattern:
  the [Python sibling](https://github.com/magdaU/playwright-python-dulux-uk) hit a
  production redesign that wrapped the same control in a `group` with three
  "Quantity"-labelled elements, breaking its equivalent locator's uniqueness — flagged
  here as a proactive hardening item in
  [Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) rather
  than waited on.
- The basket lists the **tester's product name** and the **shade name** as plain text.
- No transaction ever completes against production — the suite verifies basket *state*,
  never checkout.

**Code:** [`page/pom/CartPage.java`](../src/test/java/com/github/magdalena/page/pom/CartPage.java)

---

## Visualizer app

**What it is:** a third-party (Adjust-powered) app letting a customer preview a colour,
launched from a shade's detail page.

**Key behaviour — genuinely different per viewport, not a bug in either case:**
- **Desktop:** opens in a **new browser tab**, at a fixed, documented URL
  (`https://www.dulux.co.uk/en/articles/dulux-visualizer-app`).
- **Mobile:** does **not** open the app — instead the page shows the literal message
  `"Inconsistent store data, contact support@adjust.com"` inside a `<pre>` element. This
  is asserted as observed, documented behaviour, not assumed to be either correct or a
  defect — see [Testing Without Requirements](TESTING_WITHOUT_REQUIREMENTS.md) for why
  that distinction matters when there's no spec to confirm intent against.

**Code:** `openVisualizerExperience()` in
[`cucumber/CucumberContext.java`](../src/test/java/com/github/magdalena/cucumber/CucumberContext.java)

---

## Visual appearance (empty cart, colour finder, shade grid)

**What it is:** three key pages checked pixel-for-pixel against a committed baseline, on
top of the functional/accessibility-style checks above.

**Key behaviour:**
- Runs as a **separate, non-blocking** CI job — a real production layout change surfaces
  as an uploaded diff artifact, not a failed push/PR gate. See
  [Lessons Learned #4](LESSONS_LEARNED.md#4-visual-regression-implemented-here-deferred-in-the-python-sibling)
  for why this project made a different call here than its Python sibling.

**Code:** [`support/VisualComparisonUtil.java`](../src/test/java/com/github/magdalena/support/VisualComparisonUtil.java),
[`tests/visual/VisualRegressionTest.java`](../src/test/java/com/github/magdalena/tests/visual/VisualRegressionTest.java)

---

## See also

- [Test Strategy §3.1](TEST_STRATEGY.md#3-scope) — the automated scenarios that exercise
  these features today.
- [Test Cases](TEST_CASES.md) — individual test case specifications, automated and manual.
- [Architecture](architecture.md) — how these page objects fit into the framework.

# 📝 Test Summary Report

> A cycle-level, stakeholder-facing summary: what was tested, the outcome, issues found
> and their resolution, residual risk, and a recommendation. Where
> [Test Results](TEST_RESULTS.md) is the detailed per-case record, this is the narrative a
> team lead or hiring reviewer would actually read.

| | |
|---|---|
| **Reporting period** | Project inception → 2026-09-04 (ongoing; suite runs continuously, not as a single campaign) |
| **Product under test** | Dulux UK e-commerce website (`https://www.dulux.co.uk`) |
| **Report author** | QA / SDET |
| **Related documents** | [Test Plan](TEST_PLAN.md) · [Test Results](TEST_RESULTS.md) · [Lessons Learned](LESSONS_LEARNED.md) |

---

## 1. Summary

The suite automates the two highest-value Dulux UK customer journeys — **tester
purchase** and **Visualizer launch** — across desktop and mobile viewports, plus a
non-blocking pixel-diff check on three key pages. All 8 functional tests (4 Cucumber
scenarios plus the parallel JUnit `TesterProductTest`/`VisualizerAppTest`) currently pass
against live production on the primary engine (Chromium). The 3 visual regression checks
run on every push but are non-blocking by design — and were confirmed to need that design,
not just as a precaution (§6).

## 2. Scope executed

| Journey | Viewports | Status |
|---|---|---|
| Tester purchase | Desktop, mobile | ✅ Passing (`@smoke` on desktop, `@regression` on mobile) |
| Visualizer | Desktop, mobile | ✅ Passing (`@smoke` on desktop, `@regression` on mobile) |
| Visual appearance (cart, colour finder, shade grid) | Desktop | 🟡 Non-blocking; genuinely flaky on one page (§6) |

Out of scope for this cycle (and for the suite generally): checkout/payment,
account/login, API/contract testing, performance/load, accessibility, full cross-browser
matrix — see [Test Strategy §3](TEST_STRATEGY.md#3-scope) for the rationale and
[Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) for what
closing each gap would take.

## 3. Results at a glance

- **8 / 8** automated functional tests passing on the primary engine (Chromium): 4
  Cucumber scenarios + `TesterProductTest` (2) + `VisualizerAppTest` (2).
- **0** open functional defects — every issue found during development was root-caused
  and fixed (see §4).
- **1** known, accepted source of noise (not a defect): the colour finder page's default
  state varies between loads, making its visual regression check inherently flaky — this
  is why that job is non-blocking, confirmed by direct observation rather than assumed
  (§6, [Lessons Learned #7](LESSONS_LEARNED.md#7-visual-regression-against-a-live-page-has-a-non-deterministic-baseline-to-chase)).

Full per-case breakdown: [Test Results](TEST_RESULTS.md).

## 4. Issues found during development

All issues below were found by this suite (or by review of it) while building against
real production — none were seeded or simulated. Each is fully written up in
[Lessons Learned](LESSONS_LEARNED.md).

| # | Issue | Severity | Status |
|---|---|---|---|
| 1 | Navigation click resolved a stale/hidden element after a full page navigation | Medium | ✅ Fixed (explicit `waitForLoadState()`) |
| 2 | Two page-object locator methods had drifted into exact duplicates | Low | ✅ Fixed (collapsed into a shared helper) |
| 3 | CI's Allure report step succeeded while publishing the wrong output directory | Medium | ✅ Fixed (switched to the Allure CLI with an explicit path) |
| 4 | Pinned test shade ("Gentle Lavender") removed from its colour family on production | Medium | ✅ Fixed (test data refreshed to "Violet Morning") |
| 5 | Basket quantity locator broken by a production markup redesign (strict-mode violation) | Medium | ✅ Fixed (narrowed to a role-based `spinbutton` locator) |

None of these are open issues against the current suite — all are resolved, with the fix
and reasoning documented rather than silently applied.

## 5. Test environment

Executed against live production (`dulux.co.uk`) from GitHub Actions `ubuntu-latest`
(headless) for CI runs, and locally (headed/headless, or via Docker) for authoring — see
[Test Plan §9](TEST_PLAN.md#9-environmental-needs). No staging/sandbox environment is
available for this third-party site.

## 6. Residual risk

The single largest structural risk remains **testing against live production** — content,
layout and third-party behaviour can change at any time, independent of any code change in
this repository. This is no longer a theoretical concern: the
[Python sibling](https://github.com/magdaU/playwright-python-dulux-uk) — testing the same
site, with the same pinned shade and a near-identical basket locator — hit both a
catalogue-removal and a basket-markup redesign, and this project has now independently hit
*both the same two issues* (§4, issues 4–5), within minutes of each other. A further,
distinct manifestation of the same underlying risk was found directly: the colour finder
page's default state is not deterministic between loads, which is why its visual
regression check is designed to be non-blocking rather than gating the build — see
[Test Strategy §10](TEST_STRATEGY.md#10-risk-analysis--mitigations) for the full register.

## 7. Recommendation

**Go** — the two in-scope revenue/engagement journeys are verified working across both
targeted viewports, with visual regressions surfaced (non-blockingly) and no open
functional defects. Recommended next investments, in priority order, are listed in
[Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) —
cross-browser coverage and a Scenario Outline covering more than one shade rank highest.

## 8. Sign-off

Single-contributor project — this report is self-issued by QA/SDET and reviewed via the
normal PR process before being merged into `main`.

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
non-blocking pixel-diff check on three key pages. All 4 Cucumber scenarios and 3 visual
regression checks currently run against live production on the primary engine (Chromium).

## 2. Scope executed

| Journey | Viewports | Status |
|---|---|---|
| Tester purchase | Desktop, mobile | ✅ Passing (`@smoke` on desktop, `@regression` on mobile) |
| Visualizer | Desktop, mobile | ✅ Passing (`@smoke` on desktop, `@regression` on mobile) |
| Visual appearance (cart, colour finder, shade grid) | Desktop | 🟡 Passing, non-blocking |

Out of scope for this cycle (and for the suite generally): checkout/payment,
account/login, API/contract testing, performance/load, accessibility, full cross-browser
matrix — see [Test Strategy §3](TEST_STRATEGY.md#3-scope) for the rationale and
[Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) for what
closing each gap would take.

## 3. Results at a glance

- **4 / 4** automated Cucumber scenarios passing on the primary engine (Chromium).
- **3 / 3** visual regression checks passing against their committed baselines.
- **0** open defects — every issue found during development was root-caused and fixed
  (see §4).
- **1** proactively flagged, not-yet-materialised risk: the basket quantity locator uses
  the same substring-based pattern (`getByLabel("Quantity")`) that broke on the
  [Python sibling](https://github.com/magdaU/playwright-python-dulux-uk) after a
  production redesign — tracked in
  [Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) as a
  hardening item rather than waited on.

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
this repository. This hasn't yet caused a failure in this project, but the
[Python sibling](https://github.com/magdaU/playwright-python-dulux-uk) — testing the same
site, with the same pinned shade and a near-identical basket locator — has already hit
both a catalogue-removal and a basket-markup redesign. That's treated as evidence this
risk is real for this project too, not just a theoretical one — see
[Test Strategy §10](TEST_STRATEGY.md#10-risk-analysis--mitigations) for the full register.

## 7. Recommendation

**Go** — the two in-scope revenue/engagement journeys are verified working across both
targeted viewports, with visual regressions surfaced (non-blockingly) and no open defects.
Recommended next investments, in priority order, are listed in
[Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) — the
basket locator hardening and cross-browser coverage rank highest.

## 8. Sign-off

Single-contributor project — this report is self-issued by QA/SDET and reviewed via the
normal PR process before being merged into `main`.

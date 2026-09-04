# 📊 Test Results

> A point-in-time record of the latest known execution result per test case — the
> **narrative** counterpart to the [Test Summary Report](TEST_SUMMARY_REPORT.md). The
> **live source of truth** for any given run is always the published Allure report (see
> [Getting Started — Reports](GETTING_STARTED.md#reports)), not this file. This document
> is refreshed after a significant change, not on every push.

**Legend:** ✅ Pass · 🟡 Non-blocking (visual regression job, `continue-on-error: true`) ·
— Not yet executed in this configuration.

---

## Latest execution record

| TC ID | Scenario | Chromium (desktop) | Chromium (mobile) | Notes |
|---|---|---|---|---|
| TC-01 | Desktop customer adds a tester from the colour finder | ✅ | — | Gates every push/PR as `@smoke` |
| TC-02 | Mobile customer adds a tester from the colour finder | — | ✅ | `@regression`, run via `workflow_dispatch` or the full suite |
| TC-03 | Desktop customer opens the Visualizer for a shade | ✅ | — | Gates every push/PR as `@smoke` |
| TC-04 | Mobile customer tries to open the Visualizer for a shade | — | ✅ | Asserts the documented store-data message, not app success |
| TC-05 | Empty cart page visual baseline | 🟡 | — | Non-blocking `visual-regression` CI job |
| TC-06 | Colour finder landing page visual baseline | 🟡 | — | Non-blocking `visual-regression` CI job |
| TC-07 | Violet shade grid visual baseline | 🟡 | — | Non-blocking `visual-regression` CI job |

**Overall status:** all 4 Cucumber scenarios pass on Chromium, the only browser engine
currently exercised (§10 of the [Test Strategy](TEST_STRATEGY.md#10-risk-analysis--mitigations)
tracks single-browser coverage as an accepted, low-impact risk). The 3 visual regression
checks run on every push/PR but are non-blocking by design — see
[Lessons Learned #4](LESSONS_LEARNED.md#4-visual-regression-implemented-here-deferred-in-the-python-sibling).

---

## How this maps to CI

| Job | What it covers | Where to find current results |
|---|---|---|
| `cucumber-smoke` (blocking) | TC-01 – TC-04, tag-selectable via `workflow_dispatch` (`@smoke` by default) | [`e2e-tests.yml`](../.github/workflows/e2e-tests.yml) run history + Allure report on GitHub Pages |
| `visual-regression` (non-blocking) | TC-05 – TC-07 | Same workflow run, `visual-diffs` artifact (only produced on a mismatch) |

## Historical incidents affecting results

Real, root-caused issues from this repository's own commit history — not simulated —
that changed how confidently a "pass" here can be read. Full write-ups in
[Lessons Learned](LESSONS_LEARNED.md).

- **2026-05-06** — the CI step generating the Allure report succeeded, but was publishing
  the wrong output directory to GitHub Pages (Maven plugin vs. Allure CLI output paths
  didn't match what the publish step expected). Fixed by switching to the Allure CLI with
  an explicit output path.
- **2026-05-11** — the `purchase` and `visualizer` journeys intermittently failed on
  `clickFindColour()` after "Find a colour" triggered a full page navigation the next step
  didn't wait for. Fixed with an explicit `page.waitForLoadState()`.
- **2026-07-09** — no functional failure, but a code-review finding: `ColorSelectionPage`
  had two locator methods with identical bodies, a latent risk that a future fix to one
  would silently miss the other. Collapsed into a shared helper.

None of these are open issues against the current suite.

## See also

- [Test Cases](TEST_CASES.md) — what each TC ID actually verifies.
- [Test Summary Report](TEST_SUMMARY_REPORT.md) — cycle-level narrative and recommendation.
- [Test Plan](TEST_PLAN.md) — schedule and environments these results were produced under.

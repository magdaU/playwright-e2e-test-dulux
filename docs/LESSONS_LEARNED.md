# 📓 Lessons Learned

> Concrete incidents this suite has hit while running against a real, live production
> site — what happened, the root cause, the fix, and the takeaway. Full risk-register
> context lives in [Test Strategy §10](TEST_STRATEGY.md#10-risk-analysis--mitigations);
> this document is the narrative, incident-by-incident version. Every incident below is
> sourced from this repository's own commit history, not invented for the write-up.

---

## 1. Navigation resolves a stale or hidden element after a full page navigation

**What happened:** clicking "Find a colour" in the top nav (`clickDropdownFindColour()`)
intermittently failed the very next step with a Playwright `TimeoutError` on
`clickFindColour()`. A `page.evaluate()` diagnostic showed the current URL was already
`/en/colour-details/filters/h_White` at click time — the button triggers a **full page
navigation**, not a dropdown reveal.

**Root cause:** without waiting for the new page to finish loading, the next click
resolved its locator against the still-loading *outgoing* page and hit a stale element
covered by the sticky header after scrolling.

**First attempt (superseded):** the page also contains a second, hidden "Find a colour"
anchor inside colour-detail sections (e.g. `/en/colour-details/h_White#tabId=item0`) that
appears earlier in DOM order than the visible mega-menu link. The first fix filtered
`clickFindColour()`'s locator to `.filter(visible=true)` to dodge that hidden duplicate —
which helped, but didn't address the real timing problem.

**Real fix:** adding an explicit `page.waitForLoadState()` immediately after the
"Find a colour" click, in `NavigationComponent.clickDropdownFindColour()`. Once the
navigation is properly waited on, the hidden-duplicate-link problem stops mattering —
the visibility filter was removed again as unnecessary once the actual root cause was
addressed. Both changes shipped same-day (2026-05-11, PR #4).

**Lesson:** a flaky locator symptom can have a decoy fix (filter around the *symptom*)
and a real one (wait for the *cause* — the navigation itself). `page.evaluate()`-based
diagnostics (checking the actual URL at failure time) settled which was which faster than
guessing from the error message alone.

---

## 2. Duplicated locator methods that could silently drift apart

**What happened:** `ColorSelectionPage` had two public methods, `chooseColour()` and a
typo'd `choseSpecificTypeColor()`, with byte-for-byte identical bodies — both resolving a
button by accessible name.

**Root cause:** no functional bug yet, but a latent one: if only one of the two callers
ever needed a locator tweak (e.g. to handle a future ambiguous match), the other would
silently keep using the old, un-fixed logic. Two copies of "the same thing" is a
maintenance liability even when they currently behave identically.

**Fix (2026-07-09, PR #15):** collapsed both into a shared private `clickButtonByName()`
helper, and renamed the typo'd method to `chooseShade` — consistent with the
`colourFamily`/`shade` naming already used elsewhere (`CucumberContext.browseToShade`).

**Lesson:** duplicated page-object logic isn't just a style nit — it's two independent
places a real site change (§10, catalogue/markup drift) can be fixed in one and missed in
the other. Caught here by code review rather than a test failure, which is the cheaper
time to catch it.

---

## 3. A green CI step can still publish the wrong report

**What happened:** the Allure HTML report published to GitHub Pages was stale/wrong even
though the `mvn allure:report` CI step reported success.

**Root cause:** the Maven Allure plugin (`mvn allure:report`) writes its HTML output to
`target/site/allure-maven-plugin`, but the GitHub Pages publish step (`peaceiris/actions-gh-pages`)
was configured with `publish_dir: target/site/allure-maven-plugin` at first and later
found to not match what the pipeline actually needed once results/history handling was
built out — the step *succeeded* at generating a report, but not necessarily the one the
next step expected to find.

**Fix (2026-05-06):** switched from the Maven plugin to the Allure CLI
(`npm install -g allure-commandline`, then `allure generate target/allure-results --clean
-o target/allure-report`), with `publish_dir: target/allure-report` — an explicit,
CLI-controlled output path independent of the Maven plugin's own directory convention.

**Lesson:** "the build step exited 0" and "the artifact downstream steps expect actually
exists at that path" are two different claims. A green step that doesn't fail loudly on a
missing or misplaced output is easy to trust prematurely — worth an explicit path/existence
check (or, as here, switching to a tool whose output path you control directly) rather
than relying on a plugin's default convention matching the rest of the pipeline by luck.

---

## 4. Visual regression: implemented here, deferred in the Python sibling

**What happened:** this project and its [Python port](https://github.com/magdaU/playwright-python-dulux-uk)
independently considered the same feature — pixel-diff regression on key pages — and
reached **opposite decisions**.

**This project's reasoning (2026-07-09):** Playwright's Java bindings don't ship the Node
test runner's `toHaveScreenshot()`/snapshot-management tooling, so pixel comparison was
built directly on the `image-comparison` library instead (`VisualComparisonUtil`,
`VisualRegressionTest`) — no external account or paid service needed. To manage the risk
of a still-drifting production layout producing noisy diffs, the check was wired as its
own **non-blocking** CI job (`continue-on-error: true`), covering three key pages (empty
cart, colour finder landing page, Violet shade grid) rather than gating the smoke suite on
it.

**The Python sibling's reasoning:** considered the same feature and **deliberately
deferred** it — reasoning that, having *already* hit both a catalogue drift and a basket
markup redesign on this same production site within one working session, a snapshot
baseline taken "now" would go stale at the very next incidental UI tweak, producing
pixel-diff noise on changes that aren't real regressions.

**Neither call is wrong.** The Python project's caution is well-founded given what it had
just observed; this project's earlier, non-blocking implementation gets earlier signal at
the cost of occasional baseline-refresh churn — a trade-off made deliberately, not by
default. See [Test Strategy §3](TEST_STRATEGY.md#3-scope) for the full
reasoning and how the non-blocking job is wired.

**Lesson:** two reasonable engineers testing the *same* live site can make opposite,
still-defensible calls about the same feature, once the trade-off (early coverage vs.
baseline noise) is made explicit rather than assumed. Documenting *why*, not just *what*,
is what makes the divergence a case study instead of an inconsistency.

---

## See also

- [Test Strategy §10 — Risk analysis & mitigations](TEST_STRATEGY.md#10-risk-analysis--mitigations) — the same incidents as a likelihood/impact register, plus foreseeable risks not yet materialised here.
- [Test Strategy §14 — Coverage gaps & improvement opportunities](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) — where the basket-locator fragility flagged by the Python sibling's incident is tracked as a proactive fix here.
- [Getting Started](GETTING_STARTED.md) — install, run, and day-to-day developer/tester workflow.

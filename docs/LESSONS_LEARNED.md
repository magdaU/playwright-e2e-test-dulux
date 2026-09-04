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

## 5. Product catalogue drift, materialised here too

**What happened:** while re-verifying the suite after upgrading to Java 25, the purchase
journey (Cucumber, both viewports, and the equivalent JUnit `TesterProductTest`) started
failing with a Playwright `TimeoutError` waiting for a button named "Gentle Lavender" on
the "Violet" shade grid — the pinned shade this suite had used as its test data since the
project began.

**Root cause:** confirmed directly against production (screenshot of the "Violet" family
page): "Gentle Lavender" is no longer listed. The current line-up is Cotton Breeze, Scent
Bottle, Violet Morning, Romantic Reverie, Sugared Lilac, Lilac Fancy, Violet Storm,
Pressed Thistle, Amethyst Starling, Heather Climb, Royal Berry, Deep Aubergine — a genuine
retailer-side catalogue change, not a test or environment problem. The Visualizer
scenarios, which reach a shade via search rather than the family grid, kept passing
throughout — confirming the shade removal was specific to the colour-finder listing, not
a broader outage.

**Notably:** the [Python sibling](https://github.com/magdaU/playwright-python-dulux-uk)
had already hit this *exact* removal independently, testing the same production site, and
had already refreshed its own test data to "Violet Morning" — the same replacement shade
chosen here, arrived at separately. This is the risk flagged pre-emptively in
[Test Strategy §10](TEST_STRATEGY.md#10-risk-analysis--mitigations) after reading the
Python project's own incident write-up; it went from a foreseeable risk to a materialised
one during this repo's own next full test run.

**Fix:** refreshed the pinned shade to "Violet Morning" across the Gherkin feature files
and the parallel JUnit tests (`TesterProductTest`, `VisualizerAppTest`); re-generated the
three visual regression baselines, since the "Violet" grid's committed appearance had also
changed (both from the catalogue reshuffle and from an unrelated live-chat widget now
present on the page).

**Lesson:** a risk register entry copied from a sibling project's real incident isn't
speculative — it's a specific, testable prediction about *this* project's own future. It
came true within the same working session it was written down in, which is as strong a
confirmation as this kind of foresight gets — and, as #6 below shows, it wasn't the only
one that did.

---

## 6. Basket markup drift, materialised here too

**What happened:** immediately after fixing #5 above, re-running the purchase journey hit
a *new* failure: `com.microsoft.playwright.PlaywrightException: strict mode violation:
getByLabel("Quantity") resolved to 4 elements`.

**Root cause:** the basket's quantity control had been redesigned into a
`<div role="group" aria-label="Quantity">` wrapping a decrease button, the quantity
`<input>`, and an increase button — all four elements (the group plus its three children)
expose an accessible name containing "Quantity", so `getByLabel("Quantity")` in
`CartPage.getQuantity()` matched all of them instead of one.

**Notably:** this is *exactly* the risk flagged in
[Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) as a
proactive hardening item, copied from the
[Python sibling](https://github.com/magdaU/playwright-python-dulux-uk)'s own identical
incident — down to the same `group`-wrapping-three-labelled-elements markup shape. It was
flagged as "due, not hypothetical" in the same commit that fixed #5, and materialised
before that commit was even pushed.

**Fix:** narrowed `CartPage.getQuantity()` from `page.getByLabel("Quantity")` to
`page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName("Quantity input"))`
— targeting the input specifically by role, exactly as the Python sibling's own fix did.

**Lesson:** two separately-predicted risks, both sourced from the same sibling project's
history, both materialised in the same test run. When a risk register entry cites another
project's *specific, concrete* incident as evidence rather than a generic "production can
change" caveat, treat the predicted fix as a to-do, not a maybe — the cost of applying it
pre-emptively is far lower than the cost of two production sites drifting in the same
direction independently, which is exactly what happened here.

---

## 7. Visual regression against a live page has a non-deterministic baseline to chase

**What happened:** after fixing #5 and #6, the three visual regression checks were
re-run twice in immediate succession against freshly-regenerated baselines. The first run
passed 3/3; the very next run — no code change in between — failed 2/3, with diffs
showing a genuinely different default state: the colour-finder landing page pre-selected a
different palette swatch and reported a different colour count ("119" vs. the previous
run's "253") purely from loading the page again.

**Root cause:** the colour-finder landing page's default selected filter is not a fixed,
deterministic starting state — it varies between page loads (likely session/analytics- or
A/B-test-driven), independent of any test or environment change. A separate, earlier diff
in the same investigation also showed the cookie-consent banner rendered in Polish instead
of English on one run, suggesting locale/geolocation detection can vary too.

**Not fixed — documented instead.** No page-object or test change addresses this; forcing
determinism would mean pinning the page to a specific state via URL parameters or extra
setup steps, which changes what the check actually verifies. This is left as-is.

**Lesson:** this is direct, first-hand confirmation of the exact caution the
[Python sibling](https://github.com/magdaU/playwright-python-dulux-uk) used to justify
*deferring* visual regression altogether (see
[Lessons Learned #4](#4-visual-regression-implemented-here-deferred-in-the-python-sibling)) —
and independent evidence that this project's choice to implement it as **non-blocking**
(§3 of the [Test Strategy](TEST_STRATEGY.md#3-scope)) was the right call for *this* page
specifically: a blocking check here would redden the build on days nothing actually
changed.

---

## See also

- [Test Strategy §10 — Risk analysis & mitigations](TEST_STRATEGY.md#10-risk-analysis--mitigations) — the same incidents as a likelihood/impact register.
- [Test Strategy §14 — Coverage gaps & improvement opportunities](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities) — where the basket-locator fragility flagged by the Python sibling's incident is tracked as a proactive fix here.
- [Getting Started](GETTING_STARTED.md) — install, run, and day-to-day developer/tester workflow.

# 🔍 Testing Without Business Requirements

> This project tests a real, third-party production site with **no access to a spec,
> ticket, design file, or backlog** — the situation an external tester or developer is
> actually in far more often than a tidy requirements doc would suggest. This document is
> about the skill that implies: what's still meaningfully testable, and how, when the
> "requirements" have to be derived rather than handed to you.

---

## 1. Why this situation is normal, not an edge case

A contractor auditing a client's site, a consultant doing a pre-acquisition technical
review, a new hire testing a legacy system nobody wrote docs for, or — as here — a
portfolio project against a public production site all share the same constraint: no
internal requirements exist to test against. Treating that as a blocker means not testing
at all. Treating it as a design problem produces a different, but still rigorous, kind of
test strategy.

## 2. What replaces a requirements document

| Instead of... | This project uses... |
|---|---|
| A written spec of expected behaviour | The **live UI itself**, read carefully — see the [Features Guide](FEATURES_GUIDE.md), which is a reverse-engineered functional description, not a copy of an internal doc |
| Acceptance criteria from a ticket | **Accessible roles and names** (ARIA semantics) as an implicit contract — a button labelled "Buy a Tester in this colour" states its own intent, testably, without anyone writing it down first |
| A UX spec for "correct" behaviour | **General e-commerce conventions** (empty-basket messaging, quantity controls, confirmation on add-to-cart) as a reasonable, checkable baseline |
| A visual design spec | A **self-hosted pixel baseline**, captured from the site's own current state and re-approved deliberately, rather than an external design file — see [Lessons Learned #4](LESSONS_LEARNED.md#4-visual-regression-implemented-here-deferred-in-the-python-sibling) |
| A second team's sign-off on "what should happen" | The **Python sibling project** ([`playwright-python-dulux-uk`](https://github.com/magdaU/playwright-python-dulux-uk)) — an independent second implementation against the same site, useful as a cross-check when something looks like it might be a bug rather than a feature, and as a source of *foreseeable* risks this project hasn't hit yet (see [Test Strategy §14](TEST_STRATEGY.md#14-coverage-gaps--improvement-opportunities)) |
| A product owner to ask "is this intended?" | **Explicit non-claims** — behaviour is documented as *observed*, not asserted as *correct*, whenever intent can't actually be confirmed (see §4) |

## 3. Techniques that work without a spec

- **Exploratory testing first, automation second.** Every scenario in this suite started
  as manual exploration of the live site — clicking through the purchase and Visualizer
  flows by hand, across viewports, before writing a single line of Gherkin. Automation
  encodes what exploration already found, it doesn't substitute for it.
- **Cross-configuration comparison as a bug-finding technique.** Running the same
  interaction on desktop vs. mobile surfaces two different kinds of finding: a
  **deliberate design difference** (mobile Visualizer shows a store-data message instead
  of opening the app — consistent every time, in every run, so read as intended behaviour)
  vs. a **genuine defect or instability** (the navigation-timing issue root-caused in
  [Lessons Learned #1](LESSONS_LEARNED.md#1-navigation-resolves-a-stale-or-hidden-element-after-a-full-page-navigation) —
  a real `TimeoutError`, not a design choice). The pattern, not a single observation, is
  what tells them apart.
- **State-based (black-box) verification instead of business-rule verification.** Nobody
  documented that "adding one tester should result in a basket with quantity 1" — but it's
  checkable *as a state transition* (empty → 1 item, correct product, correct shade)
  without needing to know any pricing or inventory rule behind it.
- **Equivalence partitioning from what's observably in the catalogue**, not from a data
  dictionary. A representative shade/family pair ("Violet Morning" / "Violet") stands in
  for "the colour-finder flow in general" — chosen and manually verified, because there's
  no fixture data to draw from instead.
- **Self-hosted visual baselines as a requirements substitute for "correct appearance".**
  With no design file to compare against, a committed screenshot of the site's own current
  state stands in for one — see [Lessons Learned #4](LESSONS_LEARNED.md#4-visual-regression-implemented-here-deferred-in-the-python-sibling)
  for how that's balanced against the risk of a still-changing production layout.

## 4. What can't be tested this way — and how that's handled honestly

- **Business logic that isn't observable from the UI** (pricing rules, inventory
  thresholds, backend validation) — genuinely untestable black-box, from outside. Not
  claimed as covered anywhere in this project's docs.
- **Whether an observed behaviour is *intended*.** The mobile Visualizer's store-data
  message (see the [Features Guide](FEATURES_GUIDE.md#visualizer-app)) is asserted as
  *what happens*, in the Gherkin's own wording ("the page shows message ..."), never
  phrased as "correctly shows an error" or similar, because there's no way to confirm from
  outside whether it's a bug or a deliberate fallback. This distinction — documenting
  behaviour vs. asserting correctness — is the single most important discipline when
  testing without a requirements source, because it's the difference between a test suite
  that's honest about its own limits and one that quietly launders a guess into a
  "requirement."
- **Whether a fix is *complete*.** A fix can only be verified against what was actually
  observed to be broken (e.g. the missing `waitForLoadState()` — [Lessons Learned #1](LESSONS_LEARNED.md#1-navigation-resolves-a-stale-or-hidden-element-after-a-full-page-navigation)),
  not against an intended design the tester was never shown.

## 5. Practical checklist for testing an unfamiliar system with no spec

1. Explore manually first — walk the critical path by hand before writing anything.
2. Write down what you *observe*, separately from what you *assume* is intended.
3. Compare across configurations (viewport, browser, locale) — consistency vs.
   inconsistency is often the only signal available for "is this a feature or a bug".
4. Reach for an external, objective standard (accessibility, security headers, performance
   budgets) wherever one exists — it's requirements you don't have to invent.
5. Verify state transitions, not business rules you can't see.
6. If a second independent implementation or team exists, use disagreement between them as
   a signal worth investigating, not as noise to average away.
7. Say, explicitly, what you couldn't test and why — an honest gap is worth more than a
   test that quietly assumes an answer nobody gave you.

## See also

- [Features Guide](FEATURES_GUIDE.md) — the functional spec this approach produced.
- [Test Strategy §2](TEST_STRATEGY.md#2-system-under-test-sut) — characteristics of the
  system under test that shaped this approach.
- [Lessons Learned](LESSONS_LEARNED.md) — concrete incidents where this way of working
  paid off or had to adapt.

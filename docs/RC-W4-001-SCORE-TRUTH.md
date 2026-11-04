# RC-W4-001: Score truth and useful base-only comparisons

**Project:** Raccoon Coefficient ONLY, standalone RuneLite plugin.
**Date:** 2026-11-04
**Baseline:** Wave 3 commit `2174a6fbb3992e39943066717da01223f14470f9`
**Owner:** Build now, no owner/native testing today; no merge or release without approval.
**Status:** CODED; CI build evidence pending; NOT NATIVE VERIFIED.

## Real engineering defect

Waves 2–3 introduced unrated set effects and weapon passives. Before this wave, `GearScoreCalculator.compare` returned an entirely unknown delta whenever an effect was unscored, even if the *affected worn slots and candidate* had perfectly verified base equipment stats. This prevented the user from seeing useful base-stat progression on Fang, Venator, Twinflame, Void, Crystal, and other qualifying gear, which undermined the product's core promise.

## Correction

- Preserve the original weighted base formula and the Wave 2/3 detection engines unchanged.
- Keep a verified **complete** delta in `GearScoreComparison.getDelta()` only when before/after totals are complete. The arrow and colors retain their original meaning.
- Add `GearScoreComparison.getBaseDelta()` only when the candidate and **all displaced slots** have verified base item stats, while overall total is incomplete. This is not a claim about set/passive effectiveness.
- For incomplete totals, the hover remains an unknown-total comparison but adds a separately labeled `Base ↑ +5.0 (total ?)` (or down/equal) instead of discarding a known base-stat difference.
- If any affected/displaced slot has unavailable verified stats, no delta is shown. Missing worn container remains unknown, not empty.
- A two-handed replacement subtracts both equipped weapon and offhand base stats; one-handed does not remove the shield. Ammo remains in ammo slot.
- Keep complete-total delta as `rounded visible after score - rounded visible before score`. Both displayed totals and the delta are one-decimal HALF_UP; this prevents a tooltip from contradicting the visible change after rounding. Base-only deltas round their verified base difference once and are *not* represented as total-change numbers.
- Retain existing Equipment Stats `Gear Score: value ?` behavior for any incomplete total.
- No changes to the compact tooltip layout pending real RuneLite screenshots. Same-line vs line break remains a native-review issue.

## Boundary

No invented effect-to-score weights, no charges or broken-item tracking, no RuneLite gameplay automation, no bank intelligence, no boss/Slayer optimizers, no new panel/sidebar, no cross-plugin coupling. TPT, Project Burrow and HoverLite must remain untouched.

## Test obligations

`GearScoreTruthTest.java` tests known Fang base-only deltas, negative/equal labels, existing unknown-total semantics, two-hand+shield displacement, missing affected gear, unrelated missing stats, null worn equipment, normal complete-delta behavior, visible rounding consistency, and unmodified Wave 1 base calculation. Existing Wave 1–3 tests must remain green. Run `bash ./gradlew --no-daemon --stacktrace clean build` on JDK 17 targeting Java 11 and RuneLite 1.13.6; retain actual CI receipts.

## FERAL-VERIFY-001

TICKET: RC-W4-001
STATUS: CODED pending independent CI build
WHAT CHANGED: explicit base-only comparisons while keeping full-score completeness honest
EXACT FILES CHANGED: `GearScoreComparison.java`, `GearScoreCalculator.java`, `GearScoreText.java`, `GearScoreTruthTest.java`, this document and CI
WHAT THE PLAYER CAN NOW DO: source-level hover presentation of known base delta even when unapproved set/passive effects prevent a certified full delta
EVIDENCE: branch commits and CI receipt once successful
OWNER TEST STEPS: NONE TODAY
STILL BROKEN: native UI not verified; unapproved set/passive point mappings remain intentionally unrated; original hover routing and Equipment Stats placement still need native proof
NEXT ACTION: independent audit, source fixes, and later owner RuneLite tests

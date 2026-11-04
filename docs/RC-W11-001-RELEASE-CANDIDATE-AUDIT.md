# RC-W11-001 | Release Candidate Source Hardening & Auditable Build

**Date:** 2026-11-04
**Repository:** `feralenterprises/Raccoon-Coefficient` only
**Authoritative source base:** Wave 10 commit `3f2952202c2c7a5b15c46f116c75eb516d9768ed`
**Status:** CODED, CI verification pending, **NOT NATIVE VERIFIED**, **NOT OWNER ACCEPTED**

## Mission and boundaries

This is a release-*candidate* engineering pass, **not** a release. No code merges, RuneLite Plugin Hub submission, plugin deployment, native RuneLite tests, owner action, charge/broken-item tracking, account/bank scanning, or modification/import of HoverLite, TrashPanda Tracker or Project Burrow. The standalone plugin and locked Wave 1 formula remain unchanged.

## Source audit

The actual cumulative GitHub branch contains the fully connected production classes:

- `RaccoonCoefficientPlugin` registers `EquipmentScoreOverlay` and `GearScoreTooltipOverlay` on startup, removes them on shutdown.
- Both overlays construct `GearScoreCalculator.wave3(names)`. This uses the original Wave 1 base coefficient table, Wave 2 pending armour set detector, and Wave 3 pending weapon passive detector. The Wave 10 calibration ledger is intentionally **research only** and does **not** alter the visible number or invent effect weights.
- `GearScoreCalculator.compare` handles one-handed and two-handed weapon/shield displacements, unknown replaced items, arithmetic rounding and differentiated verified base-only versus full totals.
- `HoveredEquipmentItem` and `ItemHoverAction` check the default menu action and eligible item widget before accepting its ID; `ScoreTooltipQueue` owns only its appended score component and never removes third-party tooltips.
- Production dependencies remain RuneLite/Java. `runelite-plugin.properties` names the registered plugin. `build.gradle` pins RuneLite `1.13.6` and Java target 11. Existing standalone imports check must remain passing.

## Newly discovered engineering risk and repair

**RC-W11-F01 | Repeat equipment-stat requests within a hover (P2; source-proven).**

A single hypothetical comparison could query `ItemStatSource.getVerifiedBonus` repeatedly for the candidate, displaced items, `affectedSlots`, and the before/after loadout scans. This creates avoidable expensive `ItemManager.getItemStats` lookups and potentially inconsistent before/after calculations if a supplied source changes during a comparison.

**Repair:** `GearScoreCalculator.compare` now wraps its supplied stat source in a **single-call memoizing view**. Each distinct item ID is resolved at most once during that comparison. Both known `EquipmentBonus` and unknown `null` values are cached locally. The cache has **no state beyond one compare invocation**, preserving the next RuneLite frame's fresh item/equipment evidence. `score`'s standalone behavior and the original formula are unchanged. `ComparisonStatSnapshotTest` verifies the lookup cap, null negative caching, two-handed displacement, and that two separate calls can see updated stats.

## Deterministic model-based regression testing

`ReleaseCandidateSimulationTest` executes **2,048 deterministic, seeded, structurally legal synthetic equipment loadouts** (seed `2026110411`). Each candidate is compared against a *separate reference implementation* of the locked Wave 1 scoring formula and an independently constructed after-swap loadout. Cases vary positive and negative equipment bonuses, decimal magic damage and rounding, prayer, empty slots, ring/ammo/body/head/weapon/shield slots, two-handed replacements, and removing existing two-handed weapons when equipping shields.

Both `GearScoreCalculator.wave1()` and the fully wired `wave3()` must match the reference score and visible delta for every synthetic no-effect case. Additional cases assert that absent stats are incomplete and real Void, Crystal/Bowfa, Fang and Venator effects remain explicitly unscored rather than silently becoming zero. This is **automated model validation**, not in-game item-stat coverage or logged-in RuneLite proof.

## Reproducible release-candidate artifact

`build.gradle` configures Java JAR archive tasks for stable file ordering and no filesystem timestamps. Isolated Wave 11 Actions CI:

1. Checks out this exact standalone source branch, installs JDK 17 and targets Java 11, and rejects cross-plugin imports.
2. Runs `bash ./gradlew --no-daemon --stacktrace clean build`, including the entire prior JUnit suite and Wave 11 test additions.
3. Inspects the candidate JAR to confirm production plugin/overlay/calibration classes, confirms class-file **major version 55 (Java 11)**, records the exact HEAD SHA and SHA-256 checksum, and uploads JAR, test receipts and manifest.

The JAR is an **engineering candidate artifact**, not verified deployable in RuneLite or approved for Plugin Hub. The build is considered **BUILT** only when all CI gates pass.

## Known remaining defects / risks

- **Native rendering is unverified.** No logged-in RuneLite screenshots showing the Gear Score header, actual menu-item hover behavior, or HoverLite coexistence. Synthetic mocked APIs cannot prove native compatibility.
- **Set/passive effects are detected but not scored.** The 27-effect Wave 10 ledger is not connected to the player-visible number; no owner-approved universal effectiveness coefficients exist. This is deliberately honest, not silently considered complete.
- **Real item identity completeness is unknown.** Exact RuneLite item variants, name changes, extra quiver slots, and panel geometry need native tests. Name-based rule matching remains limited.
- **No user-approved merge or release.** Waves are nested draft PRs. A green JUnit build does not constitute `FUNCTIONALLY VERIFIED` or `OWNER ACCEPTED`. Independent adversarial review by a separate reviewer is still pending.
- **Source performance is bounded, not benchmarked.** Memoization reduces stat lookups but per-frame item-name metadata calls and actual rendering cost still need profiling in RuneLite.

## FERAL-VERIFY-001 handback

**TICKET:** RC-W11-001

**STATUS:** CODED pending CI, **NOT NATIVE VERIFIED**.

**WHAT CHANGED:** source-proven repeated lookup issue repaired; deterministic 2,048-case reference suite; reproducible JAR configuration and branch CI requiring actual JAR structure, Java 11 bytecode, source commit and SHA-256 evidence.

**EXACT FILES:** `src/main/java/com/raccooncoefficient/score/GearScoreCalculator.java`; `src/test/java/com/raccooncoefficient/score/ComparisonStatSnapshotTest.java`; `src/test/java/com/raccooncoefficient/score/ReleaseCandidateSimulationTest.java`; `build.gradle`; `.github/workflows/rc-wave11-candidate-build.yml`; this document.

**WHAT THE PLAYER CAN NOW DO:** The same source-level equipment swap comparison, with fewer redundant stats lookups. No new UI and **not native verified**.

**EVIDENCE:** GitHub head, CI logs and candidate checksum, to be attached after successful run.

**OWNER TEST STEPS:** NONE TODAY.

**STILL BROKEN:** native UI proof, realistic item-variant coverage, effect weight approval, release permission.

**NEXT ACTION:** verify CI, independent read-only audit, then at most three actual native tests when the owner approves. No merge/release without explicit approval.

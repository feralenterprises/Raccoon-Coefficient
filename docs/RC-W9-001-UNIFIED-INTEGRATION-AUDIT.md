# RC-W9-001 | Unified Waves 1–8 source integration audit and repairs

Date: 2026-11-04
Repository: `feralenterprises/Raccoon-Coefficient`
Authoritative baseline: Wave 8 commit `c9a67c50a4472fd99ff0bcbca2875ae3aae5c13e`
Status: BUILT (isolated GitHub Actions Java build/tests passed), NOT NATIVE VERIFIED, NOT OWNER ACCEPTED
Owner scope: directly implement here, source/automated tests only; no native/owner tests, merge, release, charge counters or broken-item tracking

## Authority and provenance: verified from actual GitHub files

- Wave 1 core baseline: `aee30346a90980ee555c050d600ab0ac8211f1c5`
- Waves 2–8: PRs #2, #3, #4, #5, #6, #7 and #8 are all **draft, open and unmerged**. The base commit of each PR is exactly the head commit of the preceding wave, forming one continuous source lineage. The Wave 8 tree contains all current main/plugin Java production classes, all Wave 1–8 tests, docs and workflows.
- `RaccoonCoefficientPlugin.startUp` registers exactly `EquipmentScoreOverlay` and `GearScoreTooltipOverlay`; `shutDown` removes them. Both production constructors use `GearScoreCalculator.wave3(names)`, which includes the exact Wave 1 coefficient table, `PendingSetEffectBonus` Wave 2, and `PendingWeaponPassiveBonus` Wave 3. They read only RuneLite's own equipment and item databases.
- `GearScoreCalculator.compare` implements Wave 4 base-only vs fully certified deltas; `GearScoreText` supplies tooltip colors and explicit unknown state; W5–W7 item routing validates the top menu action, explicit item ID, and eligible RuneLite widget; W8 appends a marked score tooltip through RuneLite `TooltipManager` without importing HoverLite.
- `runelite-plugin.properties` points to `com.raccooncoefficient.RaccoonCoefficientPlugin`; `build.gradle` pins RuneLite client `1.13.6`, target Java 11. The dev launcher class is deliberately excluded from JUnit.

## Actual source defects corrected in Wave 9

**RC-W9-F01 | Duplicate worn slot could inflate the base score (P1 correctness):**
`GearScoreCalculator.score` used to sum every `WornItem`, even if two occupied entries had the same slot. `indexBySlot` in `compare` retained only the *last* occupied item, leading to a potentially false delta or verdict. `SetEffectDetector` similarly overwrote names on duplicate keys. RuneLite normally enumerates one entry per slot, so this is a defensive consistency repair, not a claimed player-observed problem. Added shared `WornLayout` validator: skip second score for the same slot, mark incomplete, refuse delta, and mark set/passive conclusions UNKNOWN when the snapshot is structurally contradictory.

**RC-W9-F02 | Fresh score could be lost when rendered twice in one tooltip cycle (P2):**
Wave 8 `ScoreTooltipQueue.addOnce` skipped all later marked tooltips, retaining the *first* score even if later renderer evidence differed. Now it replaces **only its own marked component** at the same queue index, never adding a duplicate, clearing the queue, or moving text owned by RuneLite/HoverLite. `TooltipManager.getTooltips()` is a mutable list in the source-confirmed RuneLite implementation, and the TooltipOverlay clears it after each render. The fix is tested without loading any other plugin.

**RC-W9-F03 | Source README failed to describe the current product (P2 documentation):**
The old README claimed only Wave 1 existed and set/passive detection was not yet implemented. It now describes actual code wiring and explicitly explains unscored context-dependent passives and the `?` / base-only presentation, plus the distinction between a green build and native proof.

**RC-W9-F04 | No persistent CI gate on the eventual main branch (P1 release readiness):**
Waves 2–8 used separately branch-scoped CI workflows, so a future main-branch PR could lack one stable comprehensive check. Added `.github/workflows/rc-unified-ci.yml` for Wave 9 development, `main` pushes, `main` pull requests and manual dispatch. It runs full Gradle compile/test, standalone dependency guard, and uploads test reports/JARs. Existing historical workflow files remain for audit provenance.

## Coverage added this wave

- `WornLayoutIntegrationTest`: both duplicated same item and conflicting items in one slot; prevents double-counting and verified deltas; keeps active Void and Fang detection on legitimate snapshots but rejects malformed snapshots as unknown.
- `ScoreTooltipQueueUpdateTest`: HoverLite-like front label, native middle label and Raccoon tail score survive in the same order; latest Raccoon evidence wins at the identical list index; clear starts next cycle; null input cannot alter other tooltips.
- Entire prior Waves 1–8 JUnit suite must also pass on pinned RuneLite 1.13.6.

## Remaining blockers, not disguised as passing tests

- **NOT NATIVE VERIFIED:** The actual RuneLite Equipment Stats title, all hover widget/menu paths, and live joint operation with HoverLite require logged-in/native proof.
- **NOT FULLY SCORED:** Wave 2 armour-set and Wave 3 weapon-passive effects have no approved universal numeric Gear Score weights. The displayed subtotal is explicitly incomplete when these are recognized or unresolved.
- **COVERAGE LIMITS:** Identity lookup is largely name-based; special ammo/quiver and item variants need source/owner verification; set rule catalog isn't exhaustive across items; run-time performance has no profiling receipt.
- **RELEASE NOT AUTHORIZED:** PR chain remains nested drafts, not merged into main; branch CI success is not native functionality, Plugin Hub approval, or owner acceptance. No other plugin was changed.

## FERAL-VERIFY-001 handback

TICKET: RC-W9-001
STATUS: BUILT, NOT NATIVE VERIFIED
WHAT CHANGED: repaired duplicate slot/false upgrade edge cases; refreshed own tooltip score within a render cycle; refreshed project docs and main-branch CI
EXACT FILES CHANGED: `src/main/java/com/raccooncoefficient/score/WornLayout.java`, `GearScoreCalculator.java`, `SetEffectDetector.java`, `WeaponPassiveDetector.java`; `src/main/java/com/raccooncoefficient/ScoreTooltipQueue.java`; `src/test/java/com/raccooncoefficient/score/WornLayoutIntegrationTest.java`, `src/test/java/com/raccooncoefficient/ScoreTooltipQueueUpdateTest.java`; `README.md`, `.github/workflows/rc-unified-ci.yml`, this document.
WHAT PLAYER CAN NOW DO: source-level safeguards against impossible duplicate-slot totals and stale same-cycle score tooltips; native behavior not verified.
EVIDENCE: [Unified GitHub Actions run #41892542615](https://github.com/feralenterprises/Raccoon-Coefficient/actions/runs/41892542615) succeeded with Java compile, full JUnit tests, release-boundary guard and `BUILD SUCCESSFUL in 45s`. [Test/JAR artifact](https://github.com/feralenterprises/Raccoon-Coefficient/actions/runs/41892542615/artifacts/13355355427). This result covers the source head before this documentation-only amendment; the final head is separately checked by CI.
OWNER TEST STEPS: NONE TODAY per owner instruction.
STILL BROKEN: no native proof; unapproved set/passive numeric weights; limited special-slot/variant evidence.
NEXT ACTION: independent review and owner decision on future native verification, numeric score calibration, and release; no merge without explicit approval.

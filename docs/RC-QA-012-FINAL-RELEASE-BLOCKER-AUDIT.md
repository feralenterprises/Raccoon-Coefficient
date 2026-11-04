# FERAL CREW | RC-QA-012 | FINAL BOSS RELEASE-BLOCKER AUDIT

**Date:** 2026-11-04  
**Scope:** Raccoon Coefficient, standalone RuneLite plugin  
**Authoritative production source:** \`feralenterprises/Raccoon-Coefficient\` at commit \`a72774d9a0c428e431500655fb381d3de6b4b226\` (Wave 11), draft PR #11  
**Comparison API source:** RuneLite **1.13.6 release commit** \`d62b158e83450b0708f053c753e3508c762a0803\`  
**Disposition:** **RELEASE HOLD / BLOCKED**, not a failed Java build  
**Audit type:** Adversarial source review by the same assistant that authored the development waves. **Not a genuinely independent second-party sign-off.**  
**This audit branch:** Documentation only. No production code edits, merge or release.

## Results first

**What is proven by source and CI**

1. Production \`RaccoonCoefficientPlugin\` uses \`@PluginDescriptor\` and registers \`EquipmentScoreOverlay\` and \`GearScoreTooltipOverlay\` during startup; removes both at shutdown.
2. Both production overlay constructors instantiate \`GearScoreCalculator.wave3(names)\`. Wave 3 integrates the locked Wave 1 fixed coefficients and deliberately **unrated** Wave 2 set effects / Wave 3 weapon passives. The Wave 10 calibration ledger is **not connected to the displayed total** by design, so game-mechanic percentages are not fabricated as Gear Score points.
3. \`WornEquipment.read\` reads RuneLite's \`InventoryID.WORN\`, not a bank or account archive; unavailable data is treated separately from a verified empty container.
4. \`ItemHoverAction\` and \`HoveredEquipmentItem\` inspect the default RuneLite menu action and associated widget, and do not alter menu entries. The source-level live correctness of item/menu/slot mapping remains **not proved**.
5. RuneLite 1.13.6 \`OverlayManager\` maps \`OverlayLayer.ABOVE_WIDGETS\` to top-level interface hooks and sorts overlay positions so dynamic overlays precede the built-in \`OverlayPosition.TOOLTIP\`. Built-in \`TooltipOverlay\` renders the shared list in queue order and clears it after rendering. This is consistent with Raccoon's \`TooltipManager.add\` and HoverLite's separate \`addFront\` on paper. **Live coexistence remains untested.**
6. Wave 11 CI for exact source SHA \`a72774d...\` **PASSED**: JDK 17, Java 11 classfile major 55, full Gradle compile/test/build, 2,048 deterministic synthetic swaps plus previous suites, standalone import guard and expected candidate JAR classes.
7. Engineering candidate \`raccoon-coefficient-1.0.0.jar\` SHA-256 \`190cd23ba9d57f24d762de91aa3e16e6f5cceb3e661e13f0b3e9c3043015335a\` was identical in repeated CI builds. This proves reproducible engineering output **for that build**, not that Plugin Hub will deploy those exact bytes.

## Actual release gates, ordered by impact

| ID | Level | Finding | Verified vs unresolved | Required closeout |
|---|---|---|---|---|
| RC-QA-012-P0 | Release blocker | No actual native RuneLite evidence that the Equipment Stats label and score hover appear correctly in a logged-in client. | **Confirmed evidence gap**, not a proven code defect. The current UI tests use mock widgets and fake Java graphics. | Native screenshots and owner verification of at most three focused actions when permitted. |
| RC-QA-012-P1 | Release blocker | **Plugin Hub standard-mode build has not run**. \`runelite-plugin.properties\` says \`build=standard\`, for which Plugin Hub replaces \`build.gradle\` and \`settings.gradle\`; the signed-off Gradle JAR is **not necessarily** the binary Hub would build. | **Confirmed workflow difference**, not evidence that the Hub build fails. Metadata matches the documented template: displayName, author, description, tags, plugins, blank version (allowed), standard build. | An official Hub-compatible dry-run or submission CI and review, but **do not submit without owner approval**. |
| RC-QA-012-P2 | Release blocker | Wave 1–11 commits remain nested unmerged PRs; the verified candidate is only on the Wave 11 feature branch. | **Confirmed** from live GitHub PR metadata. PR #1 open and unmerged; PRs #2–#11 open draft/unmerged. | Independently reviewed, authorized integration and merge plan. Do not merge before approval. |
| RC-QA-012-P3 | Functional verification | Actual \`MenuEntry.getWidget()\`, \`getItemId()\`, \`getParam1()\` and the top action vary by RuneLite surface and menu mode. The stricter current action classifier can fail closed on some valid item actions. | **Unverified compatibility risk**, not confirmed broken native hover. | Native hover checks across inventory, equipment, bank, and a legitimate low-priority item action; capture only if these are actually present. |
| RC-QA-012-P4 | Functional verification | Resizable title geometry, special quiver/ammo slots and localization/item variants are not tested against real game widgets/item definitions. | **Unverified compatibility risk.** Header width model is synthetic; no game screenshots. | Native UI checks and scope-specific follow-up only if mismatches observed. |
| RC-QA-012-P5 | Owner product decision | 27 recognized effects have no universal approved numerical score weight. Full effect-dependent totals remain \`?\`, with verified base-only delta when appropriate. | **Confirmed deliberate incomplete scoring**, not a missing integration bug. | Owner decides whether to keep a base-stat-only Gear Score plus separate effect indicator or authorize a context-specific model. No invented values. |

## Source observations not currently release-blocking defects

- \`EquipmentHover.itemId\` is a public legacy utility exercised by \`EquipmentHoverTest\`; production hover resolution goes through \`menuItemId\`. The legacy utility appears unused by production but removing it is a cleanup, not a justified P0 fix.
- The \`shadowJar\` Gradle task assembles a large developer/test classpath artifact; it is not the regular candidate JAR or the Hub standard-mode build. **Do not distribute the shadow JAR** in place of the ordinary plugin or Hub-built artifact.
- The production set/passive calibration ledger remains research-only; **do not claim player-visible bonuses were added in Wave 10**.
- Only \`GearScoreTooltipOverlay\` directly checks live hover menu action. \`EquipmentScoreOverlay\` accesses \`InterfaceID.Equipment.UNIVERSE\` and bounded title widget search. Exact native coordinates/visibility are not proven.
- Runtime performance is not benchmarked; the per-comparison memoization from Wave 11 prevents duplicate stats lookups **within one hypothetical comparison**, not across separate frames. Never extrapolate microbenchmark claims.
- RuneLite 1.13.6 client \`ItemManager.getItemStats\` canonicalizes item variants and returns null on missing/stale or noted item stats; the source adapter preserves this null as unknown. That is sensible, but no real item-ID regression suite currently confirms all player-facing item scores.
- The Wave 11 JAR has all named production classes and compiles with the required Java 11 classfile. It has no authorization to merge, publish or deploy.

## Sources actually inspected

**Repository at Wave 11 source commit:**
- [RaccoonCoefficientPlugin.java](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/src/main/java/com/raccooncoefficient/RaccoonCoefficientPlugin.java)
- [GearScoreCalculator.java](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/src/main/java/com/raccooncoefficient/score/GearScoreCalculator.java)
- [EquipmentScoreOverlay.java](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/src/main/java/com/raccooncoefficient/EquipmentScoreOverlay.java)
- [GearScoreTooltipOverlay.java](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/src/main/java/com/raccooncoefficient/GearScoreTooltipOverlay.java)
- [HoveredEquipmentItem.java](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/src/main/java/com/raccooncoefficient/HoveredEquipmentItem.java), [ItemHoverAction.java](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/src/main/java/com/raccooncoefficient/ItemHoverAction.java), [WornEquipment.java](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/src/main/java/com/raccooncoefficient/WornEquipment.java)
- [runelite-plugin.properties](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/runelite-plugin.properties), [build.gradle](https://github.com/feralenterprises/Raccoon-Coefficient/blob/a72774d9a0c428e431500655fb381d3de6b4b226/build.gradle)
- [CI run #41895786958](https://github.com/feralenterprises/Raccoon-Coefficient/actions/runs/41895786958), [source SHA + candidate test evidence](https://github.com/feralenterprises/Raccoon-Coefficient/actions/runs/41895786958/artifacts/13355318664).

**Exactly pinned RuneLite 1.13.6 source, not latest master:**
- [OverlayManager.java](https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-client/src/main/java/net/runelite/client/ui/overlay/OverlayManager.java)
- [OverlayRenderer.java](https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-client/src/main/java/net/runelite/client/ui/overlay/OverlayRenderer.java)
- [TooltipOverlay.java](https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-client/src/main/java/net/runelite/client/ui/overlay/tooltip/TooltipOverlay.java)
- [TooltipManager.java](https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-client/src/main/java/net/runelite/client/ui/overlay/tooltip/TooltipManager.java)
- [ItemManager.java](https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-client/src/main/java/net/runelite/client/game/ItemManager.java)
- [MenuEntry.java](https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-api/src/main/java/net/runelite/api/MenuEntry.java)
- [Official Plugin Hub contribution and standard-build instructions](https://github.com/runelite/plugin-hub/blob/master/README.md)
- [HoverLite's own overlay](https://github.com/feralenterprises/hoverlite/blob/main/src/main/java/com/feralenterprises/hoverlite/HoverLiteOverlay.java), read only for existing queue coexistence, not imported.

## Safe future owner-native verification (NOT requested now)

1. Open RuneLite logged in, open Equipment Stats, photograph the raccoon and \`Gear Score:\` header in standard/resized mode. Confirm score responds to a worn item change and never collides with the title.
2. Hover a verified equippable inventory item, then the corresponding worn item. Record the score/delta and the normal game tooltip. For an unrated effect item (e.g. Fang or Bowfa+Crystal), verify \`Base +/-\` and \`total ?\` instead of a fabricated full advantage.
3. Enable HoverLite alongside Raccoon Coefficient. Hover the same equippable item; confirm both display correctly in order, no duplication or hidden native text.

**No owner test steps today.** These are recorded exclusively for the future verification window.

## FERAL-VERIFY-001 handback

**TICKET:** RC-QA-012  
**STATUS:** **BLOCKED FOR RELEASE** (source audit done; no new production code; NOT NATIVE VERIFIED)  
**WHAT CHANGED:** Report-only audit branch. **Zero production source changes.**  
**WHAT THE PLAYER CAN NOW DO:** No player-visible change claimed. The previously generated Wave 11 engineering JAR remains the candidate, not a public release.  
**EVIDENCE:** Actual SHA, source links, pinned RuneLite internals, Plugin Hub requirements, passing Wave 11 build and checksum.  
**OWNER TEST STEPS:** NONE TODAY.  
**STILL BROKEN/UNVERIFIED:** Release gates P0/P1/P2, native item/UI variants, product scoring policy; no confirmed new crash or incorrect-scoring regression from this audit.  
**NEXT ACTION:** Second-party independent source review, then the three focused native checks when the owner authorizes; official Plugin Hub build and PR chain integration only after review and explicit release approval.

**NO MERGE. NO RELEASE. NO TEST CLAIMS BEYOND THE ACTUAL EVIDENCE.**

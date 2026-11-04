# RC-UI-015 | Invisible equipped Gear Score: native-failure recovery

Date: 2026-11-04
Baseline: RC-UI-014 PR #14 commit 8b4942852bec37aa08ec8582e441b169f3f5897e
STATUS: CODED pending CI, NOT NATIVE VERIFIED, not owner accepted.

## Actual owner native evidence

The screenshot taken 2026-11-04 20:30 shows the Equipment Stats window open,
all gear slots and vanilla bonuses rendered, but NO total Gear Score above
the helmet. So RC-UI-014 FAILED the native acceptance check despite passing
mocked Java2D render tests. Do not mark that ticket FUNCTIONALLY VERIFIED.

## Source-level hypotheses, not false certainties

The old overlay registered only MANUAL + drawAfterInterface(InterfaceID.EQUIPMENT).
It is not known whether that callback was invoked; it may also have returned
early because panel root or helmet widget bounds/visibility differed from
the synthetic test widgets. We cannot distinguish these from screenshots
alone.

Actual RuneLite 1.13.6 source confirms ABOVE_WIDGETS registers on three
top-level interface draw hooks via OverlayManager.rebuildOverlayLayers.
OverlayRenderer.renderAfterInterface calls the overlay. The already
working hover uses that phase, making it the best supported replacement.

## Corrective code

- EquipmentScoreOverlay: use ABOVE_WIDGETS top-level render, no additional
  per-equipment hook to avoid duplicates. Only paint when a real helmet SLOT0
  widget is visible and sized. Check panel visibility and bounds.
- GearScoreHelmetLayout: when universe root bounds are null, zero-size, or
  do not intersect the visible helmet, use a conservative rectangle derived
  from the observed helmet widget instead of hiding all score text.
- State-transition SLF4J console logs: "Raccoon Coefficient total: ..." with
  reason for suppression or "visible over helmet"; no per-frame log flood,
  account identifiers, or item data.
- Java2D tests: TOPLEVEL overlay registration, zero-size-root fallback,
  move/resize geometry, genuine hidden widget suppression, worn-item
  changes and non-overlap with title/helmet.
- Locked Wave 1 score, hover UI, tooltip coexistence, armour/passive rules
  and plugin independence unchanged. Weapon score balancing stays a separate
  v2 research decision.

## FERAL-VERIFY-001

TICKET: RC-UI-015
STATUS: CODED pending CI, NOT NATIVE VERIFIED
WHAT CHANGED: top-level hook, defensive geometry, clear native diagnostics
EXACT FILES: EquipmentScoreOverlay.java, GearScoreHelmetLayout.java,
GearScoreHelmetLayoutTest.java, EquipmentScoreOverlayRenderTest.java,
this report, Windows README, RC-UI-015 packaging workflow.
WHAT PLAYER CAN NOW DO: test score display above helmet through a confirmed
RuneLite top-level overlay route; not native verified until owner screenshot.
EVIDENCE: real owner failure screenshot; pinned RuneLite 1.13.6 source;
pending build/test receipt.
OWNER TEST STEPS: (1) extract new ZIP separately and START-RACCOON.bat,
(2) open Equip Your Character and look above helmet,
(3) if missing, send console line prefixed Raccoon Coefficient total:.
STILL BROKEN: Native appearance until retested; weapon weight balancing
and context-dependent score totals still unapproved.
NEXT ACTION: close only with actual owner native screenshot.

No main merge, Plugin Hub release, bank scanning, charge/durability tracking,
or code dependencies on HoverLite, TPT, or Burrow.

# RC-W5-001 | Hover routing and Equipment Stats header reliability

Date: 2026-11-04
Status: CODED; CI evidence pending; NOT NATIVE VERIFIED; OWNER ACCEPTANCE NOT GRANTED
Repo: feralenterprises/Raccoon-Coefficient
Baseline: Wave 4 commit d594bf195ac8445e5e346f1f7c46dc8934f535a3
Development branch: feature/rc-wave5-hover-ui-20261104

## Problem and source-level solution

The old `GearScoreTooltipOverlay.hoveredItemId()` inspected only the last menu entry, required that entry's widget, and accessed `widget.getChild(1)`. Widget child index 1 is not a verified universal hovered-item ID. RuneLite 1.13.6 explicitly exposes `MenuEntry.getItemId()`, `MenuEntry.getWidget()` and `Menu.getMenuEntries()`.

Wave 5 introduces `HoveredEquipmentItem`: examine menu entries from highest priority to lowest, skip null/unassociated entries, and require a known inventory, worn-equipment, bank, GE-offer or side-equipment widget. Prefer the entry's own item ID; if it is missing, use the associated widget's item ID. Do not guess child(1), scan a bank, alter menu entries or invoke plugin code from another repository. Non-equippable items still return unknown stats from the existing verified RuneLite stat adapter and do not produce invented comparisons.

The Equipment Stats header previously clipped everything to an assumed 32% of a nominal 500 px title region, without measuring actual score text. `GearScoreHeaderLayout` calculates usable width, respects the vanilla title's bounding box where present, and selects either full `Gear Score: ...`, compact `GS: ...` with the same raccoon mark, or no label if either would overflow. It uses graphics' current RuneScape font measurements before choosing and restores font, color, and clip after drawing. It does not overwrite vanilla widgets.

## Verification required
- Run full JDK 17 / Java 11 target Gradle clean build, ensuring all Waves 1–4 tests still pass.
- Add source-level resolver tests for top-menu priority, menu item ID over widget ID, child widget container parent, unsupported widget rejection, inventory/bank/equipment/GE hover, absent menu entries.
- Add header geometry tests for full/compact/hidden cases, vanilla title collision, absent/tiny widgets, unknown marker.
- Confirm compile-time compatibility against pinned RuneLite 1.13.6 and a repository isolation guard.

## Known limitations to preserve in handback
- **NOT NATIVE VERIFIED**: RuneLite live hover mapping and Equipment Stats actual geometry are not established by mocked API widgets or layout tests.
- Partial support for side interfaces and special slots remains provisional until live evidence.
- Compact single-line hover styling remains a later owner-visual decision. No tooltip takeover implemented here.
- Wave 2/3 set/passive numeric mappings remain unapproved and incomplete.
- No charge or broken-item management, bank/account scanning, monster DPS, TPT/Project Burrow/HoverLite integration, new panels, or releases.

## FERAL-VERIFY-001 handback

TICKET: RC-W5-001
STATUS: CODED pending independent build receipt, NOT NATIVE VERIFIED.
WHAT CHANGED: safe menu-item resolution and text-width-aware header placement.
EXACT FILES CHANGED: HoveredEquipmentItem.java, EquipmentHover.java, GearScoreTooltipOverlay.java, GearScoreHeaderLayout.java, EquipmentScoreOverlay.java, GearScoreText.java, HoveredEquipmentItemTest.java, GearScoreHeaderLayoutTest.java, this handback and isolated CI workflow.
WHAT THE PLAYER CAN NOW DO: source is designed to resolve item hovers on eligible widgets without child guessing and avoid cropped equipment score labels. Not independently witnessed in-game.
EVIDENCE: see Wave 5 PR and isolated CI receipt.
OWNER TEST STEPS: NONE TODAY (owner prohibited native testing until project built).
STILL BROKEN: native behavior unverified; special-slot hover unknown; set/passive coefficients pending; some score labels may be hidden if the header is too small.
NEXT ACTION: targeted independent code audit and later native verification when authorized.

Do not merge main or release, and do not touch other plugins.

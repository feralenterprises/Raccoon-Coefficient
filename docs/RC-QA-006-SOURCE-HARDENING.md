# RC-QA-006 | Wave 1–5 adversarial source hardening

Baseline: Wave 5 `bf80e1c041ee4a394141bdf8648c386fd3afd340`
Date: 2026-11-04
Repository: `feralenterprises/Raccoon-Coefficient`
Status: CODED pending CI; NOT NATIVE VERIFIED; OWNER ACCEPTANCE NOT GRANTED

## Verified defects in the actual source

1. **Possible wrong hover item:** `EquipmentHover.menuItemId` trusted `widget.getItemId()` even for a generic Inventory/Bank/GE *container widget* when `MenuEntry.getItemId()` was unavailable. A container is not a uniquely hovered item. Fixed: prefer explicit menu item ID, then use widget fallback only on item-specific direct child/slot surfaces. Return NO_ITEM for a generic container with no menu item ID. Retain worn/side dynamic widget support provisionally pending native proof.
2. **Null worn array:** `WornEquipment.read` dereferenced `ItemContainer.getItems().length` without checking whether `getItems()` returned null, despite handling missing containers. Fixed: return unavailable/null; `WornEquipment.contains` now also safely handles null.
3. **False complete zero:** `GearScoreCalculator.wave1().score(null, null)` returned a certified complete 0.0 even though the worn equipment list was unavailable. Fixed: preserve the legacy displayed 0.0 subtotal but mark incomplete; distinct from a truly observed empty list.

## Proof scope and remaining risk

`SourceHardeningTest` adds source-level regressions for all three. The existing Waves 1–5 tests must stay green. A green CI build confirms Java compilation/test behavior against pinned RuneLite 1.13.6, **not** real in-game GUI, event timing, font placement, or menu mappings.

Potential next audit priorities: exact menu item / widget source authority on each native hover surface, dynamically changing bank/GE widget identities, current localization/item-name ambiguity for set detection, performance of repeated RuneLite item metadata lookups per render, and how unapproved set/passive scores should appear visually. These remain unverified, not accepted.

No charge/broken-state tracking, TPT, Project Burrow or HoverLite import, account/bank scanning, optimizer, merge, release, or owner testing today.

## FERAL-VERIFY-001

TICKET: RC-QA-006
STATUS: CODED (await CI)
WHAT CHANGED: guard false-positive hover fallback, guard unavailable worn item arrays, mark unavailable equipment incomplete in all scoring factories
EXACT FILES CHANGED: EquipmentHover.java, WornEquipment.java, score/GearScoreCalculator.java, SourceHardeningTest.java, this QA document, isolated CI workflow
WHAT THE PLAYER CAN NOW DO: source-level avoidance of misleading hover and missing-equipment values; native experience unverified
EVIDENCE: GitHub commit/PR and standalone CI receipt when complete
OWNER TEST STEPS: NONE TODAY
STILL BROKEN: native hover/header behavior unverified, set/passive numeric scoring unapproved
NEXT ACTION: CI, QA review, later limited owner-native tests

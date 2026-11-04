# RC-UI-014 | Above-helmet total and stat-distribution audit

Date: 2026-11-04
Baseline: standalone Raccoon Windows packaging source bc09c9f76cd40ec3ba1e1abca788ac416d5c42e3
Status: CODED pending CI; NOT NATIVE VERIFIED; not owner accepted.

## Owner's exact visual target

In the "Equip Your Character..." screen, draw one concise total Gear Score
directly ABOVE the helmet equipment slot, BELOW the native title. The user
circled this empty region in their November 4 native screenshots.

Source uses actual RuneLite InterfaceID.Equipment.UNIVERSE panel and
InterfaceID.Equipment.SLOT0 helmet widget bounds. The whole raccoon icon
plus Gear Score line is horizontally centered on helmet SLOT0, baseline
11 px above its top. Full label is preferred; compact GS: fallback when
needed; otherwise HIDDEN to prevent title or widget overlap. A real worn
equipment snapshot powers the live total. It never changes game widgets,
tooltips or existing scoring math.

JUnit source-level tests validate representative screenshot geometry,
moving and cramped panel positions, and actual Java2D overlay drawing,
including item-change refresh. Native RuneLite appearance is not tested.

## The v1 stat balance problem, measured from the user's screenshot

Locked Wave 1 formula:
  0.1 * sum(five attack + five defence bonuses)
  + melee strength + ranged strength + magic damage (%) + 0.5 * prayer

Native screenshot shows:
- Attack 23+105+23-47-4 = 100
- Defence 362+361+360+35+364 = 1482
- Melee Strength +128; Ranged Strength +0; Magic damage 0.0%; Prayer +10

Base subtotal: (100+1482)*0.1 + 128 + 10*0.5 = 291.2
Melee strength alone: 128/291.2 = 43.96% of subtotal.
Defence combined: 148.2/291.2 = 50.89% of subtotal.
Attack combined: 10/291.2 = 3.43%; prayer: 5/291.2 = 1.72%.

One point of strength counts TEN TIMES a point of attack or defence.
This is a structural imbalance, especially for powerful weapons. The
above percentages are STAT-FAMILY decomposition, NOT actual per-item slot
shares. To audit the weapon slot, sum verified stats by equipment slot.

## Proposed v2 research, NOT an unapproved math change

1. Gather verified RuneLite equippable item stat data for representative
   beginner / midgame / endgame melee, ranged and magic kits.
2. Report real per-slot contribution shares and marginal weapon swaps,
   including 2H/offhand, negative stat bonuses, ammo and empty slots.
3. Prototype a versioned slot/family-normalized formula with transparent
   comparison tables and monotonicity tests.
4. Obtain explicit owner approval before replacing the locked v1 formula
   or displaying a new v2 number. Do not invent game-effective DPS points.

No change to GearScoreCoefficients or player-facing hover scoring in this UI
ticket; no arbitrary weight caps in production.

## FERAL-VERIFY-001

TICKET: RC-UI-014
STATUS: CODED pending CI; NOT NATIVE VERIFIED
WHAT CHANGED: above-helmet total score placement plus tests and measured
stat-distribution diagnosis.
EXACT FILES: EquipmentScoreOverlay.java; GearScoreHelmetLayout.java;
GearScoreHelmetLayoutTest.java; EquipmentScoreOverlayRenderTest.java;
this report; Windows packaging workflow and notes.
WHAT PLAYER CAN NOW DO: Code can show total directly above helmet; real native
verification required before acceptance.
EVIDENCE: GitHub commit, JUnit build and updated Windows ZIP receipt after CI.
OWNER TEST STEPS: 1) Start new development client, 2) open Equip Your
Character, 3) equip/unequip item and check visible score changes.
STILL BROKEN/UNVERIFIED: native display, context-dependent set/passive totals,
and proper v2 balancing research.
NEXT ACTION: owner verify score location; then review v2 side-by-side proposal.

Strictly independent plugin, no TPT, Burrow or HoverLite changes, no merge/release.

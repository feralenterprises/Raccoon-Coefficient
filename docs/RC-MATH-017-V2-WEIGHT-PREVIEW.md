# RC-MATH-017 | Gear Score v2 Weight Calibration (Experimental Only)

Date: 2026-11-04. Base: RC-UI-016, a7515616a7df895f0973b072564360f6ed85e663.
Status: CODED research proposal, NOT LIVE, NOT NATIVE VERIFIED.

## Actual player evidence

- Abyssal whip: +82 Slash Attack, +82 Melee Strength, displayed Gear Score 90.2.
- Ahrim's robetop: Magic Attack +30, Ranged Attack -10; Defence 52/37/63/30/0; Magic Damage +1%; score 21.2.
- Guthan's platebody: Magic Attack -30, Ranged Attack -10; Defence 122/120/107/-6/132; score 43.5.
- Elite Void Magic total appeared as 46.3 with +5.0% Magic Damage in the native Equipment Stats; active set recognition and score conversion were not confirmed in this build.

## Verified v1 imbalance

The old formula awards 1.00 point per Melee/Ranged Strength, but only 0.10 for each Attack or Defence bonus. The whip receives 82 / 90.2 = 90.9% of its item score from Strength alone. Ahrim's +30 Magic Attack gets 3.0 points, +1% Magic Damage 1 point, and -10 Ranged Attack subtracts 1.0. Guthan's 475 total Defence gets 47.5, offset by -4.0 Attack.

This is stat arithmetic, not proof that either combat role is actually better in PvM.

## Proposed weights, not approved for live plugin

| Bonus | v1 coefficient | v2 trial coefficient |
|---|---:|---:|
| Stab/Slash/Crush Attack | 0.10 | 0.10 |
| Magic Attack | 0.10 | 0.40 |
| Ranged Attack | 0.10 | 0.10 |
| All Defence bonuses | 0.10 | 0.08 |
| Melee Strength | 1.00 | 0.30 |
| Ranged Strength | 1.00 | 0.30 |
| Magic Damage percentage point | 1.00 | 2.00 |
| Prayer | 0.50 | 0.50 |

Signed negatives remain negative; Magic Attack -30 now subtracts 12 rather than 3, as important as the boost to +30. No unsupported boss multipliers, no hidden cap, and no invented combat DPS equivalences.

### Exact comparisons from the owner's screenshots

| Item | v1 | proposed v2 | change |
|---|---:|---:|---:|
| Abyssal whip | 90.2 | 32.8 | -57.4 |
| Ahrim's robetop | 21.2 | 27.6 | +6.4 |
| Guthan's platebody | 43.5 | 25.0 | -18.5 |

These three item examples are a tiny sample. Validate representative ranged and magic weapons, low-mid-high tier gear, hybrid and negative stats, 2-handed/shield swaps and armour set activation before any approval. The experimental score remains a generic item-stat indicator, not a combat effectiveness guarantee.

## Separate issue: set effects

The current production score uses the original Wave 3 calculator. Active-but-unrated sets are expected to carry an unknown-total indication instead of invented points. The Elite Void native screenshot showed a 46.3 number without an obvious unknown mark, which may mean the full set was not recognized in the live source.

Investigate actual worn ID-to-name mappings, full set pieces and the detector's returned ACTIVE/UNKNOWN/INACTIVE states. Simply adding the +5% Magic Damage panel value as flat score points could double count game-derived modifiers if the model is later changed. Detection/explanation and numeric score conversion require separate acceptance.

## Scope / FERAL-VERIFY-001

TICKET: RC-MATH-017
STATUS: CODED, NOT LIVE, NOT NATIVE VERIFIED, not owner accepted.
WHAT CHANGED: Experimental score model and exact screenshot-based tests, not production formula or UI.
FILES: score/GearScoreV2Preview.java, score/GearScoreV2PreviewTest.java, docs/RC-MATH-017-V2-WEIGHT-PREVIEW.md and separate CI.
WHAT PLAYER CAN NOW DO: No changed visible behavior. Compare candidate v1 versus v2 source test outputs.
EVIDENCE: Screenshot stats and passing CI when confirmed.
OWNER TEST STEPS: None now.
STILL OPEN: Live Elite Void recognition; unapproved numeric set/passive weights; limited sample; weapon-specific distribution study, user approval.
NEXT ACTION: Review sample changes, broaden verified gear coverage, then choose exact v2 policy before wiring production.

No cross-project coupling, portable RuneLite, bank scan, durability tracking, merge, or release.

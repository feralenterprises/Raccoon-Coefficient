# RC-W10-001 | Source-calibrated combat-effect ledger

**Project:** Raccoon Coefficient only. Standalone RuneLite plugin.
**Date:** 2026-11-04
**Baseline:** Wave 9 `e7ea3bfdd0e9def7fb5d148c82d444978154c308`
**Status:** CODED pending CI; **NOT NATIVE VERIFIED**, **NOT OWNER ACCEPTED**
**Owner scope:** Develop in GitHub directly. No owner testing now, no merge or release, no charge/broken-state tracking and no dependencies on HoverLite/TPT/Burrow.

## Decision boundary: mechanics ≠ generic Gear Score

The locked Wave 1 number includes equipment bonuses, NOT a fight-specific damage-per-second calculation. A percentage applied to a spell, special mechanic or damage received does not automatically mean "+that percentage" of Gear Score.

Example proofs:
- The Void mage helm set gives **+45% magic accuracy**, not +45% damage; elite magic adds +5% damage. Void ranged is +10% accuracy/damage, with elite ranged an extra +2.5% ranged damage. Source: https://oldschool.runescape.wiki/w/Void_Knight_equipment
- Each Crystal piece gives ranged accuracy/damage **only with crystal bow or Bow of Faerdhinen**: helm +5%/+2.5%, body +15%/+7.5%, legs +10%/+5%; combined +30%/+15%. Source: https://oldschool.runescape.wiki/w/Crystal_armour
- Inquisitor's armour after **22 July 2026** is **piecewise** for the crush attack style: great helm +0.5% accuracy/damage; hauberk and plateskirt +1% each. Do not reinstate the obsolete full-set or mace multipliers. Official update: https://oldschool.runescape.wiki/w/Update:Summer_Sweep-Up_2026 and item source: https://oldschool.runescape.wiki/w/Inquisitor%27s_armour
- Justiciar is an **incoming-damage** modifier: a style's defence bonus / 3000, not +X% outgoing damage or a universal DPS boost. Source: https://oldschool.runescape.wiki/w/Justiciar_armour
- Obsidian +10% melee accuracy/damage requires eligible obsidian melee weapon and helm/body/legs. Source: https://oldschool.runescape.wiki/w/Obsidian_armour
- Fang's 15%–85% normal-hit damage band reduces variance; it does **not** increase expected DPS. Its double accuracy roll depends on target defence and attack style. Source: https://oldschool.runescape.wiki/w/Osmumten%27s_fang
- Venator ricochets are **multitarget** and conditional; no universal single-target bonus can be assigned. Source: https://oldschool.runescape.wiki/w/Venator_bow
- Twinflame's standard-spellbook +10% accuracy/damage is spell-conditional. The second hit for qualifying elemental spells is 40% of the first hit, rounded down, **not** a free permanent +40% gear damage bonus; Strike and Surge are excluded. Source: https://oldschool.runescape.wiki/w/Twinflame_staff

**Reference date:** All cited source mechanics checked 2026-11-04, with live-source verification through OSRS Wiki search. These numbers are **game mechanic percentages**, NOT approved Gear Score coefficients.

## Source implementation

`EffectCalibrationLedger.java` is a pure Java catalog that covers all **27 detector rules** (22 armour effects + 5 weapon rules) with stable IDs, a combat-context classification, source URL via its original detector, exact mechanical accuracy/damage percentages **only where applicable**, a separate second-hit percentage where applicable, and explanatory notes. Null means no fixed universally applicable percentage exists, not 0%.

`observe(worn, identities)` uses the existing Wave 2 and Wave 3 detectors; each effect becomes `ABSENT`, `DETECTED_UNRATED`, or `UNKNOWN`. There is **no player-specific activation claim for unknown combat conditions**. If a future developer adds a detector rule but fails to add a calibration entry, an integration test must fail rather than leaving an unseen bonus. The ledger is immutable and has **no score-conversion function**. Every `Mechanic.hasApprovedGearScoreWeight()` deliberately returns false.

The actual `GearScoreCalculator.wave3()` and the player-visible overlay remain **unchanged** by this ticket. PendingSetEffectBonus and PendingWeaponPassiveBonus still return an unknown extension instead of fabricated points. The new source is an offline engineering/calibration foundation for owner policy approval.

Also updates the Inquisitor piecewise mechanics and Justiciar description in `SetEffectDetector` without changing any equipment activation/detection rule.

## Calibration policies to consider later (NONE automatically approved)

**Option A: Base-stat Gear Score + separate recognized effect indicators.** The base number stays consistently comparable as equipment-stat score. Contextual effects can be shown as facts, not points. This preserves the product's one-number premise without falsely advertising DPS.

**Option B: Fight-context Gear Score.** Requires selected combat style, spell/attack type, target stats, multicombat layout, possible set activation conditions, damage model, and explicit owner-approved translation from effectiveness to score points. This becomes a distinct scenario-based model, not the generic equipment score. It is **not built or requested here**.

Do not treat examples like "+10%" as approval to add +10 points. These are different units and can mislead the player.

## Automated proof gate

`EffectCalibrationLedgerTest` checks all 27 detector definitions have facts and links, correct Void magic vs ranged distinctions, all current Inquisitor percentages, Crystal pieces summing 30% accuracy/15% damage only in the eligible context, Fang's **variance** not mean DPS, Twinflame's extra hit vs 10% spell boost, Venator's multitarget condition, and a full Void kit continuing to show **base-only subtotal** without invented effect points. Source tests must pass together with Waves 1–9; no native game claim.

## FERAL-VERIFY-001 handback

**TICKET:** RC-W10-001
**STATUS:** CODED pending CI; NOT NATIVE VERIFIED
**WHAT CHANGED:** Added explicit sourced calibration ledger for all current effect rules; refined Inquisitor and Justiciar descriptions
**EXACT FILES CHANGED:** `src/main/java/com/raccooncoefficient/score/EffectCalibrationLedger.java`, `src/main/java/com/raccooncoefficient/score/SetEffectDetector.java`, `src/test/java/com/raccooncoefficient/score/EffectCalibrationLedgerTest.java`, this document, and Wave 10 isolated CI.
**WHAT THE PLAYER CAN NOW DO:** No new visible controls yet. The source now supports accurate effect interpretation while preserving base score and unknown total semantics; native behavior remains unverified.
**EVIDENCE:** Head commit, passing CI run and test/JAR artifact to be recorded after build
**OWNER TEST STEPS:** NONE TODAY
**STILL BROKEN:** No owner-approved generic weights for context-dependent effects; no native RuneLite verification; not a complete DPS or encounter simulator
**NEXT ACTION:** Independent mechanical-data QA, determine future display policy, then native verification when owner authorizes it.

**No main merge, no release, no charge/broken handling or plugin coupling.**

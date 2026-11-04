# RC-W2-001 | Raccoon Coefficient Wave 2: Verified Armour-Set Effects

Date: 2026-11-04
Status: ENGINEERING DISPATCH / NOT IMPLEMENTED / NOT NATIVE VERIFIED
Source baseline: `aee30346a90980ee555c050d600ab0ac8211f1c5`
Repository: `feralenterprises/Raccoon-Coefficient`
Scope: Standalone Raccoon Coefficient RuneLite plugin ONLY.
Owner directive: Build and research today; do NOT request owner/native testing today. No main merge or release.

## Mission
Build the Wave 2 **set-effect detection, validity, variant, and integration infrastructure** for the generic Gear Score plugin. Resolve the live-game rules accurately before assigning a score. Preserve Wave 1 formula exactly, one-decimal HALF_UP rounding, negative bonuses, honest missing-data handling, hypothetical slot swaps, and the tiny existing Equipment Stats/hover UI.

**Numeric set-effect coefficients have NOT been owner-approved.** Do not silently equate “10% damage” with “+10 gear score,” pretend effects based on a target or player Hitpoints are constant, or introduce DPS, boss optimization, account data, bank reads, or new control panels. Build as much as possible without inventing numeric coefficients, then separately present a concise proposed score-mapping decision for owner approval. Until that approval, use zero contribution *only* for a verified inactive effect, not for an active-but-unrated effect. Distinguish “effect active but unscored” from “no active effect,” and avoid displaying a certified full total where contributions are unknown.

## Verified OSRS research (checked 2026-11-04)

1. **Void Knight**: full set = appropriate helm + gloves + body + robe. Melee helm: +10% melee accuracy and damage. Ranged helm: +10% ranged accuracy/damage, elite top+robe adds +2.5% ranged damage. Mage helm: +45% magic accuracy; elite top+robe adds +5% magic damage. Void seal/mace not required. Track melee/ranged/mage variants, normal/elite tops and robes, and ornament variants. Do not assume a mixed elite/normal pairing grants the elite bonus.
   https://oldschool.runescape.wiki/w/Void_Knight_equipment
   https://oldschool.runescape.wiki/w/Elite_Void_Knight_equipment
2. **Obsidian**: helmet + platebody + platelegs grants +10% melee damage/accuracy **only with obsidian melee weapons**. Berserker necklace grants a separate +20% damage effect with qualifying weapons; do not double count or silently mix it into the set term. Count only when the relevant weapon is actually worn.
   https://oldschool.runescape.wiki/w/Obsidian_armour
3. **Barrows**: each brother's **four-piece** set requires the matching weapon and three armour pieces; completely broken items do not activate the effect. Amulet of the Damned adds other effects, with charge state. Many effects depend on HP, target, proc, or combat context; do not assign constant points. Validate IDs for intact, 100/75/50/25 variants and explicitly reject 0/broken states.
   https://oldschool.runescape.wiki/w/Passive_effect
   https://oldschool.runescape.wiki/w/Barrows_armour
   https://oldschool.runescape.wiki/w/Amulet_of_the_Damned
4. **Justiciar**: faceguard + chestguard + legguards. Damage reduction outside excluded contexts depends on the relevant defence bonus / 3000. It is not a fixed universal bonus; mark as context dependent.
   https://oldschool.runescape.wiki/w/Justiciar_armour
5. **Inquisitor (2026 RULE CHANGE)**: as of the 2026-07-22 game update, the previous full-set +1% effect was redistributed to individual pieces: great helm 0.5%, hauberk 1%, plateskirt 1% crush accuracy and damage. Inquisitor mace special set multiplier removed and strength/crush stats adjusted. DO NOT implement outdated all-or-nothing 2024 formulas. Treat this as piecewise armour effect, conditioned on crush.
   https://oldschool.runescape.wiki/w/Inquisitor%27s_armour
6. **Crystal**: crystal helm/body/legs individually add +5/+15/+10% ranged accuracy and +2.5/+7.5/+5% ranged damage when using a crystal bow/Bow of Faerdhinen. Not an all-or-nothing full-set bonus; each piece can qualify.
   https://oldschool.runescape.wiki/w/Crystal_armour
7. **Moons of Peril**: Blood requires the dual macuahuitl plus helm/body/legs for conditional attack-speed effect; Blue requires its spear and armour for a binding-spell-triggered extra attack; Eclipse atlatl and full armour yield burn chance on hits to nonimmune targets. These are target/action/context dependent, so DETECT but do not fabricate a constant gear-score increase. Verify broken-piece behavior from current Wiki before rejecting them by analogy with Barrows.
   https://oldschool.runescape.wiki/w/Blood_Moon_armour
   https://oldschool.runescape.wiki/w/Blue_Moon_armour
   https://oldschool.runescape.wiki/w/Eclipse_Moon_armour

## Code engineering requirements

- Work from the exact verified source tree on this branch; audit branch/head before editing.
- Create a clean, pure-Java set-effect model and rule catalog. Use verified RuneLite item IDs and reliable canonical/variant handling; do NOT infer set membership from vague substring matching or make up IDs. Keep rule activation independently testable.
- Represent effects with ID, source URL, required equipped pieces, valid variants, weapon predicates (when relevant), live state: ACTIVE / INACTIVE / UNKNOWN, and scoring state: APPROVED_NUMERIC / ACTIVE_UNRATED / NOT_APPLICABLE. Include duplicate-count safeguards.
- Respect equipped *slot placement*, two-hand/shield displacement, weapons and ammo, and recompute activation for before and hypothetical after equipment. Swaps that break/complete a set must change status accurately.
- A missing worn container is UNAVAILABLE, not a confirmed empty equipment loadout (Wave 1 RC-P1-002). A missing item definition/stat is UNKNOWN, not zero.
- Preserve `GearScoreCalculator` source formula and existing Wave 1 outputs when no applicable set effect is worn. Do not add weapon passives (Wave 3).
- Integrate set contributors only where verified and owner-approved numeric mapping exists. Do not silently register proposed coefficients. For active unscored effects, return an honest incomplete status or explicitly base-only label; write a test proving no fabricated complete number is shown.
- Do not corrupt RuneLite vanilla Equipment Stats or tooltip entry ordering. No new sidebar, config dependency, external state, network, bank reader, plugin API coupling, or UI redesign.
- Ensure no imports/build dependency on TrashPanda Tracker, Project Burrow, or HoverLite. Independent gradle root/package/repo only.

## Minimum automated test matrix (developer tests allowed; no owner/native testing today)

1. Void all 3 helms, normal vs elite, missing glove/robe/helm, mixed normal/elite, ornament IDs.
2. Obsidian exact 3 pieces with qualifying vs nonqualifying weapon, and presence/absence of Berserker necklace.
3. All 6 Barrows four-piece sets, 100/75/50/25 variants, broken variant rejection, wrong brother mix, amulet effects only when valid.
4. Justiciar complete/partial; Inquisitor each piece independent and *no old full-set bonus*; crystal partial and correct weapon predicate.
5. Moons full sets, weapon swapped, broken-piece exceptions, contextual effect stays UNRATED.
6. Two-handed weapon or shield removing required set weapon; swapping a set piece creates or breaks activation; missing source/worn container remains UNKNOWN.
7. Original Wave 1 formula, negative values, rounding, unknown and two-handed/ammo tests remain unchanged.
8. Verify no duplicate contribution or accidental use of character stats/monster info/other plugins.
9. Document unit test receipts and an independent Gradle/JDK build (if actually run). Claim `BUILT` only for a real successful build receipt; label `NOT NATIVE VERIFIED` until RuneLite evidence exists.

## Definition of this ticket's outcome
Deliver an actual working set-activation engine, exact source links, changed files, and a concise table:
Set | Condition / valid IDs | Active/partial/unknown detection | Score rule | Status/remaining blocker.
Wave 2 activation infrastructure can be CODED/BUILT before numeric policy approval; do NOT call full Wave 2 player-visible scoring complete until approved score mapping exists.

## Locked handback (FERAL-VERIFY-001)
TICKET:
STATUS: CODED | BUILT | FUNCTIONALLY VERIFIED | OWNER ACCEPTED | BLOCKED
WHAT CHANGED:
EXACT FILES CHANGED:
WHAT THE PLAYER CAN NOW DO:
EVIDENCE: branch, commit URL, actual commands/logs, source links
OWNER TEST STEPS: NONE TODAY (later: no more than three focused checks)
STILL BROKEN:
NEXT ACTION:

Explicitly: NO merge to main, NO Plugin Hub release, NO modifying other repos, NO owner testing today. Only the owner may accept/complete.

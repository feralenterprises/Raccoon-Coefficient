# RC-W3-001 | Wave 3: general weapon-passive detection

Date: 2026-11-04
Source baseline: Wave 2 branch `feature/rc-wave2-set-effects-20261104`, commit `a27742ea25ead9b0b74266938fbad9e8231fba94`
Status: CODED pending isolated CI receipt; NOT NATIVE VERIFIED; OWNER ACCEPTANCE NOT GRANTED
Scope: standalone Raccoon Coefficient ONLY. No TPT, Burrow, or HoverLite integration, code, data, services, or release dependencies.

## Owner scope
Develop here; no owner/native RuneLite testing today; no merge or Plugin Hub release. Do not build charge counters, durability/broken-state management, bank scanners, slayer/boss DPS calculators, attack-style selection UI, spellcasting telemetry, or gear suggestions.

The locked Wave 1 formula is unchanged. Wave 2 set effects are preserved. **There is no owner-approved numeric conversion for any Wave 3 passive**, so the `WeaponPassive` extension must never add fabricated points. An equipped named weapon with an unrated/conditional passive triggers an incomplete *base equipment subtotal* (display line's existing `?` and unknown hover delta), not a certified full Gear Score.

## Source-grounded rules

| ID | Exact eligible weapon names | OSRS mechanical fact | Runtime state and score handling |
|---|---|---|---|
| `fang.stab_accuracy` | Osmumten's fang, Osmumten's fang (or) | Accuracy reroll on stab-style attacks only. The reroll procedure differs within ToA vs outside. | PRESENT_CONDITIONAL, unrated |
| `fang.damage_band` | Same as Fang | Successful ordinary hits draw from ~15%–85% of max rather than usual damage distribution. This lowers variance, **not mean DPS**. | PRESENT_UNRATED; no independent damage multiplier |
| `venator.ricochet` | Venator bow, Venator bow (uncharged) | Up to two secondary bounces to nearby targets in multicombat when the bow is charged. | PRESENT_CONDITIONAL, not proof bow is charged or targets exist |
| `twinflame.standard_spell_bonus` | Twinflame staff | +10% accuracy and damage for standard-spellbook casts. | PRESENT_CONDITIONAL, unrated |
| `twinflame.second_hit` | Twinflame staff | Qualifying elemental casts **except Strike and Surge** deliver second hit worth 40% of first rounded down. | PRESENT_CONDITIONAL, unrated |

Wiki sources (checked 2026-11-04):
- https://oldschool.runescape.wiki/w/Osmumten%27s_fang
- https://oldschool.runescape.wiki/w/Venator_bow
- https://oldschool.runescape.wiki/w/Twinflame_staff

Venator ricochet behaviour was updated on 19 August 2026. We do not infer any universal damage increase from it, because it depends on the scene and eligible secondary targets. Twinflame's automatic elemental weakness selection is explicitly encounter-dependent and **not** assigned a generic bonus. No special attack contribution is included.

## Engineering decisions

- Pure-Java `WeaponPassiveDetector` reads ONLY worn weapon-slot identity via existing RuneLite item-name adapter; it never scans banks.
- Exact weapon names (including approved cosmetic/uncharged names), **not fuzzy substrings**.
- Every rule has stable ID, source URL, mechanical description, explicit condition, and state: ABSENT / PRESENT_UNRATED / PRESENT_CONDITIONAL / UNKNOWN. No blanket assertion that conditions are currently met.
- Null/unavailable worn list, missing RuneLite item names, or an invalid duplicate weapon slot become UNKNOWN, never “verified empty”.
- `PendingWeaponPassiveBonus` is the existing `WeaponPassive` extension implementation; returns `BigDecimal.ZERO` only for verified absence of all monitored passives, or `null` if a passive is present/conditional/unknown.
- Factory `GearScoreCalculator.wave3(names)` retains the existing Wave 2 set bridge, uses locked Wave 1 coefficients, and adds the weapon-passive bridge. Both current and hypothetical after-loadout run the same score engine. Changes that add/remove a pending weapon cannot claim a known total delta.
- No new player-facing panel, tooltip takeover, extra item stats, or cross-repository code.
- No native test claims from unit tests.

## Completion gates

The source must compile and its dedicated JUnit tests must pass on isolated GitHub Actions, with downloadable receipts and a specific SHA. Tests verify Fang ornament name, the two Fang semantics, Venator uncharged named state as CONDITIONAL (not active), Twinflame spell restrictions, non-matching weapons, wrong slots, missing identity, duplicate weapon slots, Wave 1 base score preservation, unknown swap deltas, and unavailable-vs-empty worn-state behavior.

**What remains owner-dependent:** Whether any conditional passive should ever receive a stable generic Gear Score increment at all, and if so, how. No numeric proposal is pre-approved.

## FERAL-VERIFY-001 handback

TICKET: RC-W3-001
STATUS: CODED (pending CI) / NOT NATIVE VERIFIED
WHAT CHANGED: source-based weapon passive detection and honest Wave 3 scoring bridge.
EXACT FILES CHANGED: See PR diff.
WHAT THE PLAYER CAN NOW DO: Their score computation recognizes named weapon mechanics and declines to fabricate unapproved passive points; native presentation still unverified.
EVIDENCE: branch + test workflow + commit.
OWNER TEST STEPS: NONE TODAY.
STILL BROKEN: numeric passives not owner-approved, native display unverified.
NEXT ACTION: Independent code review, build receipt, and scoring policy decision later.

No merge, release, or OWNER ACCEPTED status without direct owner authorization.

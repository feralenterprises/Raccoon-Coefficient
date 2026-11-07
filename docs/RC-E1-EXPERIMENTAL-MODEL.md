# RC-E1 | Gear Score audit and experimental E1 layer

Date: 2026-11-07
Baseline: `feralenterprises/raccoon-coefficient` branch
`research/rc-weapon-capability-model-20261104`, commit
`cfefa0e9f2ce4c71186937aa04cc7ffec73c405e` (the tip; every other branch is
an ancestor of it). The attached `RACCOON-WEAPON-VALIDATION-AND-MELEE-WAVE.zip`
contains research only (no plugin source).

Status (updated 2026-11-07 after merging RACCOON-CRYSTAL-WAVE): **CODED, score package tested offline. Not built with Gradle, not
tested in RuneLite, not owner approved.** Locked v1 remains the default.

## 1. Live contract (unchanged)

Locked v1 = 0.1 x each attack bonus + 0.1 x each defence bonus + melee
strength + ranged strength + magic damage % + 0.5 x Prayer. Negative bonuses
kept, display rounded HALF_UP to one decimal, missing item data stays unknown
("?"). A detected set effect or weapon passive that has no approved value
also marks the total "?" (the Wave 2/3 contract). Default config:
**Scoring model = Locked v1 (approved)**. All 95 pre-existing score tests still
pass unchanged.

## 2. Where v1 misleads (audit)

| Problem | Example (independent reference values) | Cause |
|---|---|---|
| Powered staves score as almost nothing | Trident of the seas alone 3.6 vs abyssal whip alone 90.2 | The trident's damage is its built-in spell (max 28 at 99 Magic), which has no equipment stat. Whip max is 25 at 99 Strength. |
| Attack speed ignored | AGS (6-tick) kit 321.6 vs whip + defender 274.0 | The 2h's strength is counted fully although it attacks 1.5x less often. |
| Ammunition counted when not fired | Whip kit + dragon arrows +60.0; magic shortbow + dragon arrows "beats" rune arrows (+11.0) | Ammo bonuses only apply to a weapon that fires that ammo; a magic shortbow cannot fire dragon arrows. |
| Hidden ammunition | Toxic blowpipe shows 23.0 | Dart strength lives inside the weapon and is not an equipment stat. |
| Unrated effects | Any Void/Crystal/Fang/Twinflame setup shows "?" | By design in v1: no approved values. |

## 3. Game facts used (sources)

| Fact | Source |
|---|---|
| Accuracy roll = effective level x (bonus + 64); max hit from effective strength x (strength bonus + 64); Void multiplies effective level by 1.1 (melee/ranged), 1.45 (magic accuracy), elite ranged damage 1.125 | OSRS Wiki DPS calculator `PlayerVsNPCCalc.ts` (weirdgloop/osrs-dps-calc @ 4c9a0bd); w/Void_Knight_equipment |
| Elite void mage +5% magic damage is a visible additive bonus | DPS calculator `Equipment.ts` |
| Void needs top + robe + Void knight gloves + helm; elite needs elite top + robe | w/Void_Knight_equipment; `BaseCalc.ts` |
| Crystal armour with crystal bow/BoFa: helm +5%/+2.5%, body +15%/+7.5%, legs +10%/+5% accuracy/damage, per piece | DPS calculator (`(20+pieces)/20`, `(40+pieces)/40`) |
| Inquisitor (crush only): +0.5% helm, +1% body, +1% legs, per piece | DPS calculator; existing detector note (22 July 2026 change) |
| Obsidian: +10% accuracy/damage with full set and a TzHaar weapon | DPS calculator |
| Barrows: Ahrim drains Strength, Guthan heals, Karil drains Agility, Torag drains run energy, Dharok scales with missing HP, Verac 25% ignores defence | w/Barrows_equipment |
| Fang: stab styles make two attack rolls vs one defence roll outside ToA; 15-85% damage band does not raise expected damage; 5 ticks | w/Osmumten%27s_fang; `BaseCalc.getFangAccuracyRoll` |
| Twinflame: +10% accuracy and damage with standard spells; 40% second hit on elemental spells except Strike/Surge; 6-tick casts | w/Twinflame_staff |
| Tumeken's shadow: x3 worn magic attack and magic damage (x4 in ToA) for its built-in spell, total capped at 100%; max hit floor(Magic/3)+1; 5 ticks | w/Tumeken%27s_shadow |
| Powered staff built-in max hits (trident seas /3-5, swamp /3-2, sanguinesti /3, ayak and accursed /3-6, thammaron /3-8, warped (8L+96)/37) | DPS calculator `PlayerVsNPCCalc.ts` |
| Arrow tiers per bow (steel oak+, mithril willow+, adamant maple+, rune/ice yew+, amethyst/broad magic+, dragon: dark/twisted/3rd age/venator/scorching) | w/Arrows |
| Ammo-slot bonuses only count when the weapon fires that ammo; crystal bow, BoFa, blowpipe, thrown weapons never use the ammo slot | DPS calculator `ammoApplicability`; w/Ranged_Strength (darts/knives/chinchompas supply their own) |
| RuneLite exposes `ItemEquipmentStats.aspeed` | RuneLite 1.13.6 source (tag `runelite-parent-1.13.6`, d62b158) |

The DPS calculator is GPL-3.0. Nothing was copied into this BSD-2 plugin;
the facts were re-implemented from item names and wiki rules.

## 4. E1: how facts become points (scoring judgements; need approval)

**J1. Equivalent-bonus pricing (core idea).** Because accuracy and max hit are
proportional to (bonus + 64), a multiplier m equals (m - 1) x (bonus + 64)
extra bonus. E1 prices that equivalent bonus with the locked v1 weights. Magic
damage multipliers use (m - 1) x (100 + magic damage %). This is a game-fact
identity (apart from integer flooring); using the v1 weights to price it is
the judgement.

**J2. Reference speed 4 ticks.** Damage term x (4 / speed - 1) is added.
Ranged uses Rapid (listed speed - 1). Spell staves (Kodai, Twinflame, Ahrim's
staff, Staff of the dead, ...) get no speed term because E1 credits no spell
damage to them.

**J3. Powered staves.** Built-in max hit M at 99 Magic is priced as the melee
strength bonus giving the same max hit for a reference player (99 Strength,
aggressive, no prayer/potion): 640 x M / 110 - 64. Trident of the seas: +98.9.

**J4. Target-dependent accuracy at "parity".** Fang and Verac are priced at a
target whose defence roll equals the attack roll: Fang ~2/3 hit chance = a
normal roll x1.5 (+0.5 x (stab + 64) attack points); Verac 0.625 = x4/3.

**J5. Not scored (shown as EXCLUDED, total stays known):** base Ahrim,
Guthan, Karil, Torag (utility), Dharok + damned recoil and Justiciar
(defensive), Fang damage band, Twinflame second hit, Venator bounce, Scythe
extra hits, Twisted bow scaling, Soulreaper stacks, all special attacks, all
monster-specific bonuses.

**J8. Brought forward from RACCOON-CRYSTAL-WAVE (see
RC-E1-CRYSTAL-RECONCILIATION.md):** Dharok priced at the Crystal Wave's fixed
reference of 50 of 99 HP (damage x1.4851); amulet of the damned: Karil +12.5%
expected ranged damage, Verac +7 prayer, Ahrim +7.5% expected autocast damage
(Ahrim is new); Void ranged gives ogre bows accuracy only.

**J6. Unknown (total shows "?"):** blowpipe darts, uncatalogued launcher/ammo
tiers, Moon sets, missing speed, any item without verified stats or name.

**J7. Ammo relevance.** Remove the ammo slot's ranged attack and ranged
strength when the weapon does not fire it.

Style for melee effects = the weapon's highest melee attack type. Speed and
set terms are computed from base totals (no stacking order modelled).

## 5. Surprising results, explained (not tuned)

**F. Fang below rapier in E1 (272.1 vs 292.3).** The 5-tick speed removes
43.6 points; the accuracy passive adds only 10.9 because v1 prices one
attack point at a tenth of a strength point. A DPS calculator usually ranks
Fang higher. Changing that means changing J1's use of the v1 attack weight,
which is your call.

**K. Kodai below trident (126.1 vs 208.2).** E1 credits a weapon's own
damage source. A Kodai's damage is whichever spell you cast. Giving spell
staves a reference-spell baseline is an open decision (Q3 below).

**V. Incomplete Void scores higher than complete Void.** Barrows gloves'
stats outweigh the set effect at this kit's stats under v1 weights.

**D. Defence dominates armour.** The Bandos whip kit (274.0) beats full Void
(223.8 in E1) mostly through defence points. That is the locked v1 formula.

## 6. Questions for the owner before E1 can become default

1. Approve J1 (equivalent-bonus pricing with the v1 weights)?
2. Approve the 4-tick reference and Rapid for ranged (J2)?
3. Powered staves at the 99-Magic equal-max-hit rule (J3)? Give spell staves
   a reference-spell baseline, or keep zero?
4. Parity reference for Fang/Verac (J4), or leave them unscored?
5. Keep utility/defensive effects at 0 (J5), or give small fixed values?
6. Keep Dharok's 50/99 HP reference (J8, about +99 points on a typical
   Dharok kit), use full HP (0), or leave Dharok unscored?

## 7. Placement ("number above the character")

The attached ZIP has no UI code, so it cannot reproduce the issue. In the
repo tip the total is a real TEXT child of the Equipment interface (group 84)
positioned 5 px above `InterfaceID.Equipment.SLOT0` (helmet) using
parent-relative coordinates from canvas bounds. Two plausible mechanisms,
not confirmed: (a) the earlier Java2D overlay versions (RC-UI-014/015) used
root/fallback rectangles; (b) the helmet slot sits directly above the
character model on that screen, so "above the helmet" also reads as "above
the character". Without a native screenshot of the current build the cause
is not determined and placement code was **not changed**.

## 8. Not verified

Gradle build, RuneLite UI files (config, plugin, total widget, tooltip
overlay) were not compiled: this sandbox cannot reach Maven Central,
repo.runelite.net or services.gradle.org. RuneLite `aspeed` is assumed to be
ticks matching the wiki. Item names are assumed to match RuneLite's
`getMembersName()` (lower-cased). Locked `(l)` Void variants not recognised.

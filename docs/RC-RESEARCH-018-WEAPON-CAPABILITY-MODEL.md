# RC-RESEARCH-018 | Weapon Capabilities and Cross-Style Gear Score

**Date checked:** 2026-11-04
**Authority:** independent OSRS Wiki weapon/item pages, RuneLite 1.13.6 source, Raccoon Coefficient's actual research-branch source
**Source baseline:** ca1d12bb45378c9f501e408a112412bd4f60aa3d (RC-MATH-017)
**Status:** RESEARCHED, **NOT IMPLEMENTED**, **NOT NATIVE VERIFIED**, **NOT OWNER ACCEPTED**
**Scope:** The intrinsic *equipment* value of weapons across melee, ranged and magic. **No DPS, monster targeting, bank scanning, account profiling, market prices, charge-count bookkeeping, or dynamic gameplay telemetry.**

## 1. Concrete answer: Why Trident of the Seas gets 7.7 versus whip's 32.8

| Weapon | Observable equipment stats | Experimental v2 stat subtotal | Fact missing from the score |
|---|---|---:|---|
| Abyssal whip | Slash Attack +82; Melee Strength +82 | **32.8** | No special correction needed to account for basic melee weapon function |
| Trident of the seas | Magic Attack +15; 21 combined Defence points observed in owner's prior example | **7.7** | **It possesses its own built-in spell** that can be used regardless of spellbook, rather than relying on a manually selected combat spell |
| Bow of faerdhinen | Ranged Attack +128; innate Ranged Strength +106 | **118.8** before its crystal armour bonus | One weapon already has much larger *raw ranged* stat points than melee or magic weapons |

The trident is a level-75 one-handed **powered staff** with an intrinsic spell; the whip requires 70 Attack, and both are established late-midgame weapons. This is a reasonable **progression comparison**, not a claim of equal damage or versatility. The OSRS Wiki categorizes powered staves as spellbook-independent built-in spell weapons, unlike ordinary autocast weapons.

Current Raccoon v2 preview is a flat, signed-weight summation of item equipment bonus fields. It has no concept of the intrinsic powered spell. A trident's Magic Damage field is not a measure of its built-in spell's strength. **Increasing the global Magic Attack coefficient cannot solve this** without also inflating Ahrim's robes and autocasting wands.

Likewise, preserving Ranged Strength at 1.00 (owner's explicit preference) means Bowfa's self-contained +106 Ranged Strength dominates the unnormalized cross-style total. The next fix is therefore **weapon-role classification plus cross-style normalization**, NOT another global melee/ranged nerf.

Numerical sensitivity only: forcing the Trident into the whip's 32.8 score would require **+25.1** above its raw 7.7. That +25.1 is **NOT a proven OSRS bonus**. It is a *calibration target* illustrating the missing scale. Do not hard-code it as an ability bonus without reviewing a wider item sample and owner approval.

## 2. What the actual RuneLite API knows, and what it does not

RuneLite 1.13.6 \`ItemEquipmentStats\` contains:
- equipment slot, two-handedness;
- five Attack bonuses, five Defence bonuses;
- Melee Strength, Ranged Strength, Magic Damage %, Prayer;
- **aspeed**, the item's attack speed metadata.

It contains **no built-in-spell category, spellbook capabilities, family-relative progression tier, special effect description, venom/heal classification, required set identity, or intrinsic passive formula**. \`ItemStats\` has equipable, weight, GE limit, and the \`ItemEquipmentStats\` record, not those capabilities. \`ItemComposition\` exposes an item ID and in-game name, but names alone are not a safe source of all weapon mechanics and variants.

Current Raccoon \`RuneLiteItemStatSource\` passes bonus fields into \`EquipmentBonus\` and deliberately does not use \`aspeed\`. **Keep combat timing out of this non-DPS Gear Score.** RuneLite cannot automatically infer the trident's powered spell from its stat adapter; we need a small *maintained item-ID capability catalog* with source URLs, verification dates, alias/variant coverage and explicit unknown states. Start with exact item IDs only when verified. Uncharged/empty and equipped variants must be distinguished by actual IDs/definitions, **without tracking charge quantities**.

## 3. Source-backed examples to cover in a minimal catalog

| Example | Class/ability to recognize | Relevant verified constraint | Score policy |
|---|---|---|---|
| Dragon scimitar | Standard melee weapon | Attack level 60 weapon; normal +67 Slash and +66 Strength | Raw melee profile, progression reference |
| Abyssal whip | Standard melee weapon | Attack level 70; +82 Slash, +82 Strength | Mid-to-high melee **anchor**, no invented spell premium |
| Iban's staff | Restricted special-spell/autocast staff | Casts Iban Blast, not a generic powered staff | Do not classify as a standard Trident-family powered staff |
| Master wand | **Autocast** rather than powered | +20 Magic Attack, +20 Magic Defence, **+10% Magic Damage since July 2026**; spell selection depends on spellbook | Its experimental base is already **29.6** (= 20×0.40 + 20×0.08 + 10×2). Don't add a universal "magic weapon" boost or duplicate its recorded Magic Damage |
| Warped sceptre | Early powered staff | Level 62; built-in spell, +12 Magic Attack | First powered tier candidate; rank below Trident of the seas unless stronger contrary evidence |
| **Trident of the seas** | Mid progression powered staff | Level 75; one-handed; spellbook-independent built-in attack; +15 Magic Attack | Built-in spell identity is explicit; compare to whip as progression peer, **not equal guaranteed score** |
| Trident of the swamp | Upgraded powered staff | Level 78; +25 Magic Attack; own spell and venom behavior | Must rank at least as high as Seas in the identical role, absent owner-approved context |
| Sanguinesti staff | Advanced powered staff | Level 82; +25 Magic Attack and healing-capable built-in spell | Preserve meaningful progression above both tridents; don't pretend healing is universally worth a combat % |
| Tumeken's shadow | Signature powered staff / multipliers | Level 85; +35 Magic Attack; passive multiplies worn Magic stats, including *negative* bonuses | **Not** a universal fixed +N. Preserve conditional/passive marker; no double-counting boosted gear |
| Bow of faerdhinen | Self-contained ranged weapon | Level 80; +128 Ranged Attack, +106 Ranged Strength; crystal armour adds separate effects | Keep **1.00 Ranged Strength within Ranged**; normalize cross-family presentation; crystal synergy separately detected |
| Toxic blowpipe | Ranged weapon with internal ammunition | Charged weapon has +30 Ranged Attack, +20 innate Ranged Strength; darts affect Ranged Strength internally | Never count inserted darts a second time as an equipped ammo slot; don't reduce all ranged because Bowfa's score is large |
| Twisted bow | Conditional ranged passive | Passive scales with target Magic | Mark conditional and not universally scored; no monster-specific calculation |

This research set deliberately spans **basic melee, mid melee, restricted spellcast, autocast, multiple powered staves, a signature magic passive, and self-ammo/ammo-based ranged**. This is better coverage than only balancing a whip against a trident.

## 4. Proposed scoring architecture, independent of DPS

Use three separate concepts. The player can still see **one Gear Score**, but its pieces must be inspectable.

**A. Verified base equipment stats:** present coefficients remain separate for physical/magic/ranged attack, defensive bonuses, Strength, Magic Damage and Prayer. Negative values stay negative. Preserve the owner's decision that **Ranged Strength raw coefficient remains 1.00** until further evidence.

**B. Weapon role and intrinsic capability:** one explicitly classified **primary weapon role** per equipped weapon: MELEE, RANGED, POWERED_MAGIC, AUTOCAST_MAGIC, or HYBRID, with subtype flags such as INTRINSIC_SPELL, AUTOCAST_SPELLBOOKS, SELF_AMMO, CONDITIONAL_PASSIVE, BUILT_IN_HEAL. A powered spell's existence must be recognized even if item Magic Damage reads 0%. Do not use generic +N for every magic staff.

**C. Equipment synergy / conditional modifiers:** full Void/Elite Void, Crystal+Bowfa, Barrows, etc. derived from actual *equipped* slots (not bank). Show these as recognized/unrated or incomplete rather than silently ignoring them. Keep contextual target-only and special effect bonuses separate from the universally comparable gear grade.

### Crucial: Cross-style normalization before composing a total

Current v2 preview calculates different style families on very different native scales:
- Whip raw v2 **32.8**.
- Trident raw v2 **7.7** (missing powered spell).
- Bowfa raw v2 **118.8** (includes full innate +106 Ranged Strength).

A single *raw sum* will never treat these families comparably while preserving Ranged Strength 1.00. Instead:
1. Calculate each style's **own internal stat/capability quality** using its correct fields, ammo rules, intrinsic spell identity, and owner-accepted weapon family tiers.
2. Calibrate **cross-style presentation** using a small set of transparent *progression benchmarks* (e.g., dragon scimitar / whip for melee; warped / seas / swamp / Sanguinesti for powered Magic; RCB / blowpipe / Bowfa for ranged). Preserve relative differences **within each style**.
3. Normalize that *role-specific offensive index* to a shared Gear Score band. Keep defensiveness/prayer as independently sourced contributions, once.
4. Assign weapon-context traits explicitly rather than adding several simultaneous MELEE+MAGIC+RANGED damage claims for a hybrid weapon. Show conditional status; do not rank via boss or monster choice.
5. Treat unknown identities as **unknown capability** and avoid displaying falsely complete totals.

**This cannot be a mechanically objective equivalence without some human policy choice.** Gear Score is an **item quality/progression index**, not a damage or DPS metric. Document reference benchmarks and owner-approved mapping so everyone knows what a number means. Don't use GE price or skill requirement alone as a proxy for quality.

### What to show in native RuneLite

An example of the *intended structure*, **not an approved score**, is:

- \`Gear Score: [number] • Powered Magic\`
- \`Base stats: 7.7 | Built-in spell: recognized | Role normalization: pending\`
- \`Set: Elite Void Magic active (accuracy +45%, extra damage +5%)\`
- If a set or passive is known but unrated: show \`Partial\` or \`?\` explicitly rather than writing a pretend exact total.

These are research display concepts, not a request to expand or redesign the currently accepted minimal equipment UI. The owner has approved the **small total above helmet**; explanatory metadata should preferably live in a tooltip or detail view, not clutter the panel.

## 5. Two high-priority engine investigations

### RC-RESEARCH-018-F01 | Powered staff catalog gap (source-confirmed)

Production \`GearScoreCalculator.wave3\` knows only the base equipment bonus record plus named **pending** set and some weapon passive detection. It has no generic intrinsic powered-spell model. Therefore the Trident is mathematically treated as +Magic Attack and Defence only.

**Next engineering action:** create a typed, versioned weapon capability/variant catalog, backed by OSRS Wiki URLs and testable on exact known item IDs. Start with six families, fail closed on unknown IDs, and do not map all staffs to one class. Add a research-only classifier/fixture test before any number enters the live UI.

### RC-RESEARCH-018-F02 | Set detection visibility and double counting (unverified native behavior)

The OSRS Wiki confirms **Void Magic** grants **+45% Magic accuracy** and full **Elite Void Magic** grants **+5% Magic damage**. Equipment Stats may already display the latter while RuneLite's **per-item** \`ItemEquipmentStats.mdmg\` cannot independently encode that complete four-piece set effect. Raccoon currently attempts active set detection by exact item *names*, not by a proven live item-ID-to-rule map. The owner's previously displayed 46.3 had no clear unrated \`?\` marker, so **live activation evidence is missing**. Do not assert the detector is working from isolated unit tests.

**Next engineering action:** log actual equipped item IDs + canonical item names + required slots and ACTIVE/UNKNOWN/INACTIVE state in a **controlled native test only**. Avoid logging account IDs or persisting private data. Verify four-piece standard and Elite variants separately. When presenting set magic damage, never add a second instance of a multiplier already represented by the stats source.

## 6. Acceptance requirements before any live score change

1. A fixture catalog with exact item IDs and variants, source/date metadata, class, intrinsic spell flag, partial/conditional effects. Unknown remains unknown. Charged/uncharged variants must not claim working built-in spells when the item definition indicates otherwise; no charge **amount** tracking.
2. Test the invariants: same-style upgrades monotonic when the source warrants it, whitelist-only identity, no duplicate ammo, no double counted set effects, no fake universal value for Shadow/Twisted Bow, no changes to 1.00 Ranged Strength *within ranged*, missing data not treated as zero.
3. Test target-independent progression anchors with owner-visible before/after score breakdown. **Trident and whip should occupy roughly comparable progression bands**, but need not show identical numbers or identical functionality.
4. Keep legacy v1 and trial v2 behind distinct research switches until the owner accepts the full policy. Do not modify their native RuneLite launcher or 247 KB lightweight package, the current tooltip, or approved above-helmet score UI.

## 7. FERAL-VERIFY-001 handback

**TICKET:** RC-RESEARCH-018
**STATUS:** RESEARCHED / REPORT ONLY, **NOT NATIVE VERIFIED**, **NOT OWNER ACCEPTED**.
**WHAT CHANGED:** Source-backed taxonomy and falsifiable scoring architecture; no production source or scoring edits.
**PLAYER-VISIBLE CHANGE:** None.
**EVIDENCE:** Pinned RuneLite 1.13.6 code, actual Raccoon source, OSRS Wiki mechanics pages as of 2026-11-04; exact calculations for whip, trident, Bowfa and Master wand.
**OWNER TEST STEPS:** None; this is research.
**STILL BROKEN:** Powered spell not in live score; v2 family normalization uncalibrated; Elite Void live set identification unproven; cross-style numeric policy unapproved.
**NEXT ACTION:** Build isolated, test-driven ID/variant capability catalog and normalization prototype on this research branch; have owner review examples before plugging values into production.

**Strict boundaries:** stand-alone Raccoon Coefficient only. No main merge, RuneLite Plugin Hub release, portable client, bank scan, cost tracking, player activity or DPS model, charge/durability count tracking, or dependencies on HoverLite, TrashPanda Tracker or Burrow.

## Source register

- RuneLite pinned \`ItemEquipmentStats\`: https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-client/src/main/java/net/runelite/client/game/ItemEquipmentStats.java
- RuneLite pinned \`ItemStats\`: https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-client/src/main/java/net/runelite/client/game/ItemStats.java
- RuneLite pinned \`ItemManager\`: https://github.com/runelite/runelite/blob/d62b158e83450b0708f053c753e3508c762a0803/runelite-client/src/main/java/net/runelite/client/game/ItemManager.java
- Raccoon \`GearScoreCalculator\`: https://github.com/feralenterprises/Raccoon-Coefficient/blob/ca1d12bb45378c9f501e408a112412bd4f60aa3d/src/main/java/com/raccooncoefficient/score/GearScoreCalculator.java
- Raccoon \`GearScoreV2Preview\`: https://github.com/feralenterprises/Raccoon-Coefficient/blob/ca1d12bb45378c9f501e408a112412bd4f60aa3d/src/main/java/com/raccooncoefficient/score/GearScoreV2Preview.java
- Raccoon \`SetEffectDetector\`: https://github.com/feralenterprises/Raccoon-Coefficient/blob/ca1d12bb45378c9f501e408a112412bd4f60aa3d/src/main/java/com/raccooncoefficient/score/SetEffectDetector.java
- OSRS Wiki powered staff: https://oldschool.runescape.wiki/w/Powered_staff
- OSRS Wiki Trident: https://oldschool.runescape.wiki/w/Trident_of_the_seas
- OSRS Wiki Warped sceptre: https://oldschool.runescape.wiki/w/Warped_sceptre
- OSRS Wiki Swamp trident: https://oldschool.runescape.wiki/w/Trident_of_the_swamp
- OSRS Wiki Sanguinesti staff: https://oldschool.runescape.wiki/w/Sanguinesti_staff
- OSRS Wiki Tumeken's shadow: https://oldschool.runescape.wiki/w/Tumeken%27s_shadow
- OSRS Wiki Abyssal whip: https://oldschool.runescape.wiki/w/Abyssal_whip
- OSRS Wiki Bowfa: https://oldschool.runescape.wiki/w/Bow_of_faerdhinen
- OSRS Wiki toxic blowpipe: https://oldschool.runescape.wiki/w/Toxic_blowpipe
- OSRS Wiki master wand: https://oldschool.runescape.wiki/w/Master_wand
- OSRS Wiki Void: https://oldschool.runescape.wiki/w/Void_Knight_equipment

**No damage-speed-based or monster-dependent comparisons were used in the scoring design.**

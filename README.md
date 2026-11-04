# Raccoon Coefficient

**Number go up, brrr.** 🦝

A **standalone** RuneLite plugin that turns worn equipment stats into a visible, one-decimal **Gear Score**, plus an item-hover comparison. It has **no code, runtime, account-data, or release dependency** on HoverLite, TrashPanda Tracker, or Project Burrow.

> Development status (2026-11-04): Waves 1–9 source implementation. Automated compile/tests are the engineering gate, **not proof of a native RuneLite session**. No production merge or plugin release is approved yet.

## What the plugin displays

- Equipment Stats header: a small raccoon icon and `Gear Score: N.N`, or compact `GS:` when space is limited.
- Item hovers on eligible RuneLite widgets: the item's own stat score and a hypothetical change after replacing the applicable equipped slot, including two-handed weapon/shield interactions.
- If current equipment, item stats or a set/passive contribution cannot be fully verified, the score is marked `?`, not invented.
- When set/weapon contributions have no owner-approved numeric mapping, a hover can still show `Base ↑ +N.N (total ?)`: this is only the **verified base-stat change**, not a complete benefit prediction.
- Uses RuneLite's ordinary tooltip queue without removing other tooltips. The existing separate HoverLite plugin is **not required**.

## Locked Wave 1 base formula

```
base = 0.1 × (all attack bonuses + all defence bonuses)
     + melee strength + ranged strength + magic damage (%) + 0.5 × prayer
```

Negative bonuses remain negative; one-decimal display uses HALF_UP rounding. Unavailable verified stats never count as proven zero.

Waves 2–3 **recognize**, but do not numerically weight, certain equipped armour-set effects (Void, Obsidian, Barrows, Justiciar, Inquisitor, Crystal, Moons) and weapon mechanics (Osmumten's fang, Venator bow, Twinflame staff). Exact item identities and effect conditions are source-grounded, but incomplete/conditional effects **do not have owner-approved universal score points**.

Wave 4 adds explicitly labeled base-only swap deltas. Waves 5–8 improve hover source validation, header clipping and shared-tooltip coexistence. Wave 9 audits the combined code, protects inconsistent worn snapshots, refreshes the Raccoon-owned score when a later render sees new data, and establishes unified CI.

## Development and testing

```bash
./gradlew clean build
./gradlew run
```

The first command compiles production classes and runs source-level JUnit tests. `run` launches the RuneLite developer client for later **manual** verification; this launch is intentionally excluded from automated unit tests. Do not interpret a green CI build as native UI proof.

QA tickets and known limitations are in `docs/`. Current Wave 9 work is staged in a draft PR, not merged or owner accepted. No charge counters, broken-state tracking, bank scanning, boss DPS calculators, or integration with other plugins are part of this scope.

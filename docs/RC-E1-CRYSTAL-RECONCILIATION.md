# RC-E1 | Reconciling RACCOON-CRYSTAL-WAVE-SOURCE.zip with E1

Date: 2026-11-07. Inputs: the E1 build (base `cfefa0e`) and
`RACCOON-CRYSTAL-WAVE-SOURCE.zip` (which continued the Dharok wave; it forked
before the repo's V2-preview/RC-RESEARCH-018 commits, and its UI files are
identical to that base).

## Rule used

One pricing table (`ExperimentalE1.SET_PRICING`) prices every effect, so each
detected effect contributes at most once. The Crystal Wave's seven separate
`*Bonus` classes were **not** copied; their mechanics were moved into that
table and their test scenarios rewritten against E1. Locked v1 keeps zero
set/passive points (unrated effects still show "?").

## Item by item

| Crystal Wave work | Where it is now | Change |
|---|---|---|
| Crystal helm/body/legs per-piece bonus | Already in E1 (`crystal.*`) | Same formula. Fixed: unfired ammo no longer inflates it |
| Elven-clan recolours of crystal pieces and Bow of faerdhinen (c) | `SetEffectDetector.canonical` | Ported unchanged |
| Fully degraded Barrows " 0" cannot activate a set | `SetEffectDetector.canonical` | Ported unchanged |
| Amulet of the damned rules: Karil, Dharok, Verac | `SetEffectDetector` + ledger catalog | Ported; Ahrim damned added (wiki) |
| Karil damned 0.125 x (ranged str + 64) | E1 `barrows.karil.damned` | Same value; uses fired ammo only |
| Verac damned +7 prayer = 3.5 | E1 `barrows.verac.damned` | Same value |
| Dharok fixed 50/99 HP reference 0.4851 x (str + 64) | E1 `barrows.dharok` | Same value; owner question 6 |
| Void melee/ranged, elite ranged, magic accuracy, elite magic +5 | Already in E1 (`void.*`) | Void melee accuracy now uses the weapon's style only |
| Void ranged: ogre bows accuracy only | E1 price() | Ported (ogre max hit ignores other gear) |
| Ledger `DETECTED_EXPERIMENTAL_MAPPING` | Not ported | Ledger describes the locked default, where these stay unrated |
| Hover "Known Δ (total ?)" subtotal delta | Not ported | E1 gives complete deltas; v1 has no rated set points |
| wave2/wave3 adding experimental points to the default total | **Rejected** | Default stays locked v1 |
| Flipped guard tests (Void "must remain unapproved", ledger 10.0 -> 17.0) | Not ported | Base guard tests kept |

## Issues found in the Crystal Wave (fixed here)

1. **Changed the live score.** Its default calculator adds experimental
   points (full Void melee 225.8 instead of 204.4, shown with no "?"; Dharok
   390.0 instead of 290.6). Here those points exist only in E1.
2. **Double count with unfired ammo.** BoFa + full crystal + dragon arrows
   scored 326.0: +60 for arrows BoFa never fires, plus +9.0 more because the
   crystal multiplier was applied to that +60. E1: 257.0, same as without arrows.
3. **Void melee accuracy credited three times.** It applied the 10% to
   stab + slash + crush summed (+21.4 on the whip kit); E1 uses the weapon's
   best style (+19.4).
4. **Mixed signals.** Some experimental totals showed as complete, others
   with "?" while still including experimental points.

Measured with the Crystal Wave's own compiled calculator on the same 67-item
fixture: `tools/e1-reference/crystal_wave_wave3_scores.tsv`.

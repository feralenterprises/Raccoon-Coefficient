# RC-W8-001 | Tooltip coexistence and source reliability

Date: 2026-11-04
Repo: feralenterprises/Raccoon-Coefficient
Wave 7 baseline: `27f603f607dd32a52a60d7a71026ed199ea6eece`
Status: CODED pending isolated build; NOT NATIVE VERIFIED; OWNER ACCEPTANCE NOT GRANTED

## Evidence inspected

HoverLite at independent repo feralenterprises/hoverlite, branch main:
- HoverLiteOverlay.java queues `tooltips.addFront(new Tooltip(value))` for the default menu action and renders ABOVE_WIDGETS, with `drawAfterInterface(InterfaceID.TOPLEVEL_DISPLAY)`.
- HoverLite V05OverlayTest.java tests the actual overlay rendering against RuneLite API mocks.
- It is a *mouse tooltip replacement* plugin. Raccoon Coefficient must **add** a score, not replace it.

RuneLite upstream TooltipManager.java:
- `add` appends; `addFront` prepends; `clear` removes after rendering.
- `TooltipOverlay.renderTooltips` walks the queue in order, stacking vertically; it renders either a text tooltip or an owned LayoutableRenderableEntity and then clears.
- `TooltipOverlay` also draws after TOPLEVEL_DISPLAY and uses TOOLTIP position with highest priority.

Implication: If HoverLite calls addFront and Raccoon calls add, HoverLite stays first and Raccoon second regardless of overlay invocation order. They need no shared state and no cross-imports.

## Source corrections

1. `ScoreTooltipQueue.java`: only append a marked Raccoon-owned component if none has already been queued this tooltip-render cycle. Do not inspect, remove, clear, reorder or overwrite entries owned by other plugins. On the next RuneLite clear, a fresh score may be queued.
2. `GearScoreTooltipOverlay.java`: use that idempotent append function; align full-screen draw hook with TooltipOverlay and HoverLite; keep positioning ABOVE_WIDGETS with dynamic overlay style. Preserve existing white score and colored delta presentation.
3. Add a package-private constructor using generic ItemStatSource and GearScoreCalculator to **test the actual overlay render path**, while keeping the original injected RuneLite adapters in production.
4. `TooltipCoexistenceTest.java`: uses RuneLite API proxies to execute the real score overlay, plus the exact HoverLite source-confirmed addFront operation. Both invocation orders, native third-party tooltip preservation, no duplicate on double render, post-clear next frame, non-item action suppression, unavailable worn container, unverifiable item and full-screen draw hook.

## Explicit limits

- These tests **simulate HoverLite's tooltip-queue contract**, not the complete live HoverLite plugin, RuneLite logged-in graphics pipeline, or exact overlay ordering on all client versions. Actual coexistence is **NOT NATIVE VERIFIED**.
- Do not embed or import HoverLite, alter its source, read its config, suppress its tooltip, or copy its full overlay implementation. No coupling to TPT/Burrow either.
- Do not introduce account/bank scanning, durability/charge tracking, new panels, or score coefficients for unrated set/weapon effects.
- No merge/release and no owner native tests today.

## FERAL-VERIFY-001 handback

TICKET: RC-W8-001
STATUS: CODED pending CI; NOT NATIVE VERIFIED
WHAT CHANGED: independent shared-tooltip queue coexistence and idempotent Raccoon score delivery
EXACT FILES: ScoreTooltipQueue.java, GearScoreTooltipOverlay.java, TooltipCoexistenceTest.java, this document, rc-wave8-build.yml
WHAT PLAYER CAN NOW DO: the code can add an equipment score after other tooltips without deleting their labels, subject to native verification
EVIDENCE: exact GitHub head/CI build receipt, to be attached once successful
OWNER TEST STEPS: NONE TODAY
STILL BROKEN: native ordering/layout remains unverified; effect scoring unapproved; special item menu surfaces need client evidence
NEXT ACTION: inspect CI results, independent source review, then unified Waves 1-8 source review

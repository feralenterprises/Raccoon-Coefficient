# RC-QA-007 | Source-driven hover-policy alignment after HoverLite comparison

Date: 2026-11-04
Project: **Raccoon Coefficient**, standalone RuneLite plugin
Baseline: RC-QA-006 commit `e5dccafeb1ad4b059d979042bbf9d6ed7092fdbe`
Status: CODED; independent CI pending; NOT NATIVE VERIFIED; OWNER ACCEPTANCE NOT GRANTED

## Source comparison

**Reference read only**: HoverLite GitHub repository `feralenterprises/hoverlite`, default branch `main`. We inspected the exact published source, not just README/marketing.

- `HoverLiteOverlay.java` uses `client.getMenuEntries()`, reads **only the final (default left-click) entry**, screens out overlay/low-priority menu actions, categorizes it, and emits display text through `TooltipManager.addFront`. [Source](https://github.com/feralenterprises/hoverlite/blob/main/src/main/java/com/feralenterprises/hoverlite/HoverLiteOverlay.java)
- `HoverCategory.java` identifies item contexts through **MenuAction types** and for widget/CC_OP interactions uses `WidgetUtil.componentToInterface(entry.getParam1())`. [Source](https://github.com/feralenterprises/hoverlite/blob/main/src/main/java/com/feralenterprises/hoverlite/HoverCategory.java)
- `V05OverlayTest.java` directly renders the HoverLite overlay against proxy Client/MenuEntry objects, guarding actual tooltip queueing at a source level. [Source](https://github.com/feralenterprises/hoverlite/blob/main/src/test/java/com/feralenterprises/hoverlite/V05OverlayTest.java)

**Raccoon Coefficient before this ticket:** `HoveredEquipmentItem.resolve()` scanned the full menu stack backward and took the first entry associated with any eligible widget; it did not validate MenuAction type. If the default interaction was an NPC, synthetic overlay, or another non-item action, a lower-priority item could incorrectly be treated as actively hovered.

## Narrow independent implementation

- New pure policy `ItemHoverAction.java` validates item-type menu actions and widget action groups using RuneLite API only.
- `HoveredEquipmentItem.java` now selects **only the current default entry**. No fallback to older menu actions if default is not eligible. Its existing guarded `EquipmentHover.menuItemId` validates the item widget and chooses the authoritative entry item ID before direct-child fallback.
- Menu actions and options that are absent, plugin overlays, RuneLite low-priority actions, NPC/world/ground/player actions, or generic non-item widget actions yield NO_ITEM.
- Existing `HoveredEquipmentItemTest.java` updated with explicit MenuAction/param1 inputs and positive/negative source tests for menu ordering, overlay types, item groups and missing entries.
- Keep existing modern `client.getMenu().getMenuEntries()` access in Raccoon Coefficient rather than changing APIs solely to mimic HoverLite. Do not copy its entire overlay or tooltip manager queueing policy.
- IMPORTANT: Raccoon Coefficient **adds** an equipment-score tooltip; HoverLite is a **replacement** for built-in mouse tooltip text. Do not copy HoverLite's `respectNative` suppression blindly, as that could hide the score whenever game-native item tooltips are active. Do not use HoverLite's `addFront` until native comparison confirms it does not reorder/obscure other tooltip content.
- No HoverLite source code or import is copied into the plugin. This is an independent use of RuneLite's published interfaces and publicly inspectable design lessons.

## FERAL-VERIFY-001

TICKET: RC-QA-007
STATUS: CODED pending independent build receipt; NOT NATIVE VERIFIED.
WHAT CHANGED: Guard default hovered action against stale/background inventory items, and validate item action types.
EXACT FILES CHANGED: `src/main/java/com/raccooncoefficient/ItemHoverAction.java`, `HoveredEquipmentItem.java`, `src/test/java/com/raccooncoefficient/HoveredEquipmentItemTest.java`, this document, and the isolated CI workflow.
WHAT THE PLAYER CAN NOW DO: Raccoon Coefficient's source can avoid adding item scores for non-item default interactions; native behavior not yet witnessed.
EVIDENCE: source diff and CI build receipt when verified.
OWNER TEST STEPS: NONE TODAY.
STILL BROKEN: live hover behavior in inventory/bank/equipment unknown until native validation; possible interaction with HoverLite tooltip ordering remains unverified; Wave 2/3 numeric coefficients remain pending owner decision.
NEXT ACTION: isolated Gradle build and independent QA, then no more than 3 owner-native tests when authorized.

Strict boundaries: no merge/release, no changes to HoverLite/TPT/Burrow, no cross-repository dependencies, bank scanning, charge or durability tracking.

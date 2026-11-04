RACCOON COEFFICIENT | RC-UI-016 | LIGHTWEIGHT WINDOWS BUILD
=============================================================

NO RUNE LITE PORTABLE CLIENT IS INCLUDED IN THIS ZIP.

What's included:
  - Raccoon Coefficient compiled plugin JAR (small, not runnable alone)
  - Full source code, Gradle project and Windows wrapper
  - START-DEV-RUNELITE.bat
  - CLEAN-BUILD-AND-RUN.bat
  - BUILD-ONLY.bat

HOW TO CHECK THE EQUIPPED TOTAL
1. Extract All into a NEW folder and close any earlier development RuneLite.
2. Double click START-DEV-RUNELITE.bat. Java JDK 17 and internet are
   needed to run the Gradle development environment the first time.
3. Open "Equip Your Character". Gear Score should now be drawn as a real
   Equipment-interface TEXT widget, centered above the helmet slot.
   The plugin's item hover score is unchanged.

WHY THE PREVIOUS TWO BUILDS FAILED
RC-UI-014 and RC-UI-015 painted overlay graphics. The real RuneLite
equipment-window screenshots showed no visible total, even though Java2D
tests passed. This build does NOT register an equipment-total overlay.
It adds an actual text child inside RuneLite's Equipment interface,
using the same supported widget API used by RuneLite's Wiki DPS button.

If the total still doesn't appear, copy the console line beginning:
  Raccoon Coefficient total widget:
That is the native reason for the missing/hidden text child.

NO FORCED INSTALL OR RELEASE
- This launches a separate RuneLite development session through Gradle.
- The small JAR is not a standalone Windows executable or Plugin Hub install.
- No RuneLite executable or portable fat JAR is bundled.
- No changes to HoverLite, TrashPanda Tracker, or Burrow.
- The original Gear Score formula remains unchanged; v2 balance is future work.
- CI passing is NOT owner native verification.

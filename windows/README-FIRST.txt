RACCOON COEFFICIENT | HELMET TOTAL DISPLAY NATIVE FIX RC-UI-015
=================================================

YOUR JAVA 17 INSTALLATION IS FINE.

WHY THE OLD BUNDLE WAS SLOW
The original START-RACCOON.bat launched the Gradle wrapper, which downloads
and unpacks Gradle 8.10 the first time. If you opened START and REBUILD together,
both could contend over C:\Users\<you>\.gradle\wrapper\dists. The "Deleting
directory ... gradle-8.10" message is Gradle's own distribution cleanup,
not proof of a plugin compilation problem.

USE THIS HELMET TOTAL DISPLAY BUILD
1. Right-click this ZIP, click Extract All.
2. Open the extracted RACCOON-COEFFICIENT-WINDOWS folder.
3. Double-click START-RACCOON.bat.

The new START button uses the INCLUDED PORTABLE RuneLite Java JAR. It does
NOT run Gradle, delete folders, or rebuild before launching. JDK 17 is needed.
On the first login, RuneLite itself may still download normal game resources.

THREE DOUBLE-CLICK OPTIONS
START-RACCOON.bat       Play/test in a separate RuneLite developer client.
                        No Gradle download or rebuild.
REBUILD-AND-START.bat   Fresh clean compile + unit tests + new portable jar
                        and then launch. Internet required on the first build.
BUILD-ONLY.bat          Fresh compile and unit tests without launching RuneLite.

You do NOT need to open both Windows launchers simultaneously.
Clean build scripts use Raccoon-Coefficient\.gradle-rc-cache, so they do not
touch the Gradle cache shared with your other projects.

PACKAGE CONTENTS
PORTABLE-CLIENT\raccoon-coefficient-1.0.0-all.jar
  Standalone developer launcher bundling RuneLite dependencies. This is
  NOT the Plugin Hub installable JAR.
BUILT-JAR\raccoon-coefficient-1.0.0.jar
  Verified compiled plugin binary, exact Wave 11 source output.
Raccoon-Coefficient\
  Full plugin source and Windows Gradle wrapper, for repeatable clean builds.

If the portable launcher displays an exception, send the error text from
its window. The portable JAR is a developer client, not the publicly supported
RuneLite Plugin Hub build. Windows native login/UI has not been verified.

SCREEN FIX (RC-UI-015): The prior helmet UI build passed synthetic tests but
did NOT appear in the owner's actual RuneLite screenshot. This version
routes the total through RuneLite's top-level widget overlay rendering phase
(the same phase that works for item tooltip overlays) and uses the real
helmet widget's bounds, including a fallback for missing panel dimensions.
Open Equip Your Character and look ABOVE your helmet, BELOW the title.
If still missing, read the START window and send us the line beginning
"Raccoon Coefficient total:". The native status tells us whether RuneLite
saw the helmet widget, hid the panel, or blocked layout.
The original score weighting is unchanged while a balanced v2 is studied.

This bundle does NOT replace or integrate HoverLite, TPT, or Burrow.
No merge or release authorized.

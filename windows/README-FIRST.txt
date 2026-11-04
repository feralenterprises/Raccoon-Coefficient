RACCOON COEFFICIENT | WINDOWS ONE-CLICK PACKAGE
================================================

WHAT YOU DOWNLOADED
One clean ZIP containing:
  - Precompiled, SHA-verified Raccoon Coefficient plugin JAR
  - Full, standalone plugin source and Gradle wrapper
  - Double-click Windows launchers
  - Automated test reports

GET GOING
1) Right-click the downloaded ZIP, choose "Extract All".
   Open the extracted folder; do NOT double-click scripts inside the ZIP.
2) Make sure you have Java JDK 17 installed and an internet connection.
   Download Java from https://adoptium.net/temurin/releases/?version=17
3) Double-click START-RACCOON.bat.

WHAT EACH BUTTON DOES
START-RACCOON.bat          Easiest: opens a separate RuneLite developer client
                           with Raccoon Coefficient loaded.
REBUILD-AND-START.bat      Clean build, run automated tests, then open RuneLite.
BUILD-ONLY.bat             Clean build + unit tests, without starting RuneLite.

NO COMMAND PROMPT TYPING REQUIRED.
These BAT files automatically change to the included project folder. You can
launch them from Explorer regardless of your current terminal directory.

INCLUDED PREBUILT FILE
  BUILT-JAR\raccoon-coefficient-1.0.0.jar
The exact same JAR is also available in
  Raccoon-Coefficient\build\libs\raccoon-coefficient-1.0.0.jar
It is a plugin binary, NOT a standalone executable: double-clicking the JAR
does NOT install the plugin into your normal RuneLite client.

IMPORTANT
- This launches a SEPARATE RuneLite development client, not your installed
  Plugin Hub client. Sign in there only if you want to perform live testing.
- First launch can download Gradle 8.10 / RuneLite dependency files.
  Later launches reuse the downloads in the local Gradle cache.
- Build target is Java 11 bytecode. JDK 17 is recommended for building/running.
- The plugin has ZERO dependency on TPT, Burrow or HoverLite.
- No merge, Plugin Hub submission, production release or native verification
  is implied by this package.
- The certified compile/test receipt is from GitHub Actions Linux CI. The BAT
  files are Windows-ready but have NOT been executed on a Windows device here.

IF SOMETHING GOES WRONG
- "JAVA_HOME is invalid": Correct or unset the JAVA_HOME environment variable
  and install a valid JDK 17.
- "JAVA not found": Install JDK 17 and reopen the BAT.
- "Could not resolve" or "Read timed out": Check internet/firewall, retry.
- "Gradle wrapper not found": Extract the FULL ZIP into a folder. Do not move
  a BAT without moving the sibling Raccoon-Coefficient folder.
- If the native RuneLite client refuses to load because of a game revision,
  that is a native compatibility blocker, not a reason to change the score.

This is a DEVELOPMENT CANDIDATE. Source: feralenterprises/Raccoon-Coefficient
Authoritative prebuilt code commit:
  a72774d9a0c428e431500655fb381d3de6b4b226
No release approval granted. Number go up, brrr.

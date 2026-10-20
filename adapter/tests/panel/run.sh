#!/bin/bash
# Plain-JVM checks of the panel provider's caller rules: allowed package and certificate, other
# packages, another certificate, debug and release builds.
set -euo pipefail
cd "$(dirname "$0")/../.."
OUT=tests/panel/out
rm -rf "$OUT" && mkdir -p "$OUT"
javac -nowarn -source 8 -target 8 -d "$OUT" src/local/spotify/unified/PanelAccess.java tests/panel/src/local/spotify/unified/PanelAccessTest.java
java -cp "$OUT" local.spotify.unified.PanelAccessTest

#!/bin/bash
# Plain-JVM checks of our App Remote (AppRemoteWire, AppRemoteAccess): the WAMP frames as the
# Spotify Android SDK reads them, its JSON names, paging, JSON text and the allowed clients.
set -euo pipefail
cd "$(dirname "$0")/../.."
OUT=tests/appremote/out
rm -rf "$OUT" && mkdir -p "$OUT"
javac -nowarn -source 8 -target 8 -d "$OUT" src/local/spotify/unified/AppRemoteWire.java src/local/spotify/unified/AppRemoteAccess.java src/local/spotify/unified/PanelAccess.java tests/appremote/src/local/spotify/unified/AppRemoteTest.java
java -cp "$OUT" local.spotify.unified.AppRemoteTest

#!/bin/bash
# Plain-JVM checks of the Spotify Connect messages our interface encodes and decodes (ConnectWire).
set -euo pipefail
cd "$(dirname "$0")/../.."
OUT=tests/connect/out
rm -rf "$OUT" && mkdir -p "$OUT"
javac -nowarn -source 8 -target 8 -d "$OUT" src/local/spotify/unified/ConnectWire.java tests/connect/src/local/spotify/unified/ConnectWireTest.java
java -cp "$OUT" local.spotify.unified.ConnectWireTest

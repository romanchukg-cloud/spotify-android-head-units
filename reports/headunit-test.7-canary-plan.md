# HeadUnit test.7 — production canary prepared, not executed

Prepared 2026-10-14. Operator authorization is still required. No production report has been submitted by this plan.

## Exact public action

One synthetic manual schema-2 report will be sent through the signed, unmodified test.7 APK on an isolated Android 13 emulator. The endpoint is `https://diagnostics.localcharge.app/v1/reports`, project `spotify-android-head-units`, destination `romanchukg-cloud/spotify-android-head-units`.

Expected public issue title: **[TEST] Diagnostics server delivery**. The shared renderer recognizes `app.version: server-canary` and adds an explicit synthetic-test notice. The payload below contains no user or vehicle data. It tests the real APK's persisted queue, native scheduler, HTTPS intake and receipt-to-GitHub delivery. Queue injection is a test setup after operator approval; it does not replace or certify the ordinary in-app consent UI, which was tested separately with fake delivery.

The test uses one persisted UUIDv4 key, one private random per-report token and one frozen body. After confirmed delivery, one intentional duplicate replay may use the exact same request identity to verify that the receipt/issue remains the same. Do not generate a new key, loop submissions or create a second issue if the result is ambiguous. Close the one test issue after verification. Do not publish a new APK release or change the download page as part of this canary.

## Public report body

```json
{
  "schema_version": 2,
  "report_source": "manual",
  "project_id": "spotify-android-head-units",
  "consent": "public_issue",
  "app": {"version": "server-canary", "build": "headunit-test.7"},
  "device": {
    "android_api": 33,
    "android_version": "13",
    "manufacturer": "Synthetic",
    "model": "SYNTHETIC TEST",
    "firmware": "synthetic-no-vehicle"
  },
  "problem": {"category": "playback"},
  "settings": {},
  "components": [],
  "capture": {"duration_seconds": 1, "truncated": false},
  "events": [
    {"elapsed_ms": 0, "type": "lifecycle", "data": {"state": "started"}},
    {"elapsed_ms": 0, "type": "playback", "data": {"state": "unknown", "bridge_connected": false}},
    {"elapsed_ms": 1000, "type": "audio_focus", "data": {"state": "unknown"}}
  ]
}
```

The compact frozen body SHA-256 is `521b946eeafcbfcb4e9c27e1ff2a2c0dbe3ca0ba2946fe0e019aa6e109d02642`. Validate this against the prepared private request before executing; this checksum identifies public synthetic content, not a credential. The key/token remain outside Git and outside the report.

## Execution boundaries

1. Use only a dedicated Spotify emulator; no car or signed-in user installation. Verify the installed APK hash is `5e5d49cfa0451eef5ff18b4845462e86dbf36ea2e25e5b610f7d540a8b07dee6`.
2. Keep automatic sending opted out. Persist the approved synthetic manual request in the private queue before starting the backend. No GitHub credential enters the APK.
3. Start the real app/job, confirm durable Queued and then Report sent with a URL in the exact Spotify repository. Observe the native scheduler rather than posting directly from a shell script.
4. Verify the public issue marker/receipt and synthetic notice. Replay once with the same request identity only after the first delivery is confirmed; confirm the same receipt/issue.
5. Close the test issue, stop the emulator and record the receipt, issue URL, replay result and limitations. Never print the token or include it in logs, exports or URLs.

Strict validation against the current local shared-server validator passed before authorization. This does not establish live delivery or physical lighting compatibility. A car update and DiLink 5.1 lighting checks remain separate.

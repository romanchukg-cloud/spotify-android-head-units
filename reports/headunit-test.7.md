# HeadUnit test.7 — Support and shared diagnostics

Prepared locally on 2026-10-14. This is a signed client build, not a published release or a vehicle installation. The public release/site remains test.6. The last verified car installation remains test.6 on DiLink 5 / Android 12.

## Build identity and update compatibility

- File: `Spotify-5.5.0-HeadUnit-test.7.apk`
- Package: `com.spotify.music`; upstream version: `5.5.0.16251`; versionCode: **95484** (published test.6: 95483).
- SHA-256: `5e5d49cfa0451eef5ff18b4845462e86dbf36ea2e25e5b610f7d540a8b07dee6`
- Size: **57,946,112 bytes**.
- Original signing certificate SHA-256: `a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1`, matching test.6. The signing key was not changed or copied.
- Non-debuggable; minimum API 28; target API 33; all three DEX files remain version 039. Signature and 16-KiB ZIP/native alignment pass.
- Build identity/footer/output filename now derive from `adapter/build-info.json`.
- In-place emulator update with `install -r` succeeded. The existing `headunit_v1` preference file had the same SHA-256 immediately before and after installation. This does not establish real signed-in account switching or vehicle behavior.

## Implemented in this APK

Settings > **Support** provides **Automatically send diagnostics**, independent manual **Send diagnostics**, public JSON preview/consent, pending status/cancellation, validated GitHub links and **Export diagnostics**. All new Support/notice/export text is English. Existing app preferences and language support are preserved.

The first-use notice defaults to a selected automatic choice. Nothing automatic is queued before Continue; a prior opt-out is preserved. Measured BYD audio integration and playback-bridge failures can trigger automatic reports. Successful sessions and unobserved physical lighting/cluster output cannot. No crash/ANR detection is claimed. Audio focus is explicitly unknown unless measured.

The endpoint is `https://diagnostics.localcharge.app`, project `spotify-android-head-units`, playback profile. New reports use schema **2**, explicit `report_source: manual/automatic`, and `consent: public_issue` on the unchanged `/v1/reports` API paths. Existing pending manual v1 bodies can be retried unchanged. Before each automatic POST, GET `/` must advertise exact schema 2, automatic reporting and `project_build_failure_utc_day` grouping. Missing capabilities defer automatic submission. Grouping is implemented by the shared server, not by inventing fields in the client.

The exact body, UUIDv4 idempotency key and random 256-bit base64url report token are persisted before POST. Three queue slots are available; automatic work uses at most one and never evicts manual work. Persisted fingerprint/build and installation limits allow one new automatic report per 24 hours. Retries keep the original body/key/token, respect rate limits/backoff and end after 24 hours. Native persisted JobScheduler work handles retries and seven-day content cleanup. Android force-stop, scheduling and firmware restrictions can defer work; opening the backend reschedules it. No permanent diagnostics service was added.

Opt-out removes unattempted automatic payloads, scrubs retained automatic content and attempts DELETE for known queued receipts. It leaves manual work intact. Claimed/public delivery cannot be recalled. If POST acceptance is unknown after a lost response, cancellation stops local retries and explicitly reports that a public issue may still appear.

Private recording uses only approved playback-profile event fields and safe failure enums. BYD integration failures use the server-supported playback stage. Four fixed process slots have two 64-KiB files each; cross-process locks protect rotation/pruning. Snapshots include only the last ten minutes, at most 500 events/256 KiB. Active processes prune all slots at startup, snapshot and every minute; a stopped app cannot execute a deletion timer. No raw logcat, exception text/stacks, arbitrary preferences, account files, titles/playlists, screenshots, coordinates, VIN or persistent device IDs enter reports. Unsupported settings are omitted; components remain empty until registered. ZIP export contains only the frozen `report.json` and disclosure, never queue tokens or private logs. Read grants expose one read-only ZIP, bounded to four cached exports and 24-hour access.

Reports go to the common server and a bounded summary may become a **public GitHub Issue**. Technical metadata is not an anonymity guarantee. No GitHub credential is embedded in the APK. Pending is local; Queued is server acceptance; Report sent requires confirmed delivery and a URL in this exact repository. Successful calls do not prove physical lighting or cluster output. For lighting, enable Music rhythm in the vehicle's own menu.

## Verification

The final APK audit passed **39/39** checks. Packaging verification preserved 2,438 backend payload entries, 22,982 backend resource strings and the original backend resource chunks apart from the required UI library mapping; 5,114 UI classes remain relocated.

On disposable AOSP emulators Android 9/API28, Android 12/API32 and Android 13/API33, the diagnostic logic suite passed **94 checks per API**. Coverage includes profile/schema rejection, redaction, bounds/retention, two-UID URI grants/revocation, private export, stable replay, offline/lost responses, queue capacity, validation/conflict errors, 429/503 backoff, cancellation races, capability fallback, automatic daily limits, opt-out isolation and no healthy-session uploads. All submission/status/cancellation traffic in these tests uses a fake transport; the fixture does not have the INTERNET permission or the real upload job service.

The production first-use/Support views with fake delivery passed **7 checks per API**. Real process-termination persistence passed **2 seed + 6 restart checks per API**, including automatic/manual exact bodies, keys/tokens and the automatic daily limit. Rhythm and resume regression fixtures passed **18 and 19 checks per API**, respectively. The Android 12 tablet UI fixture required inspecting interactive accessibility windows rather than only the active root; the final suite passed. These are emulator tests, not vehicle or audible playback verification.

The exact production APK was installed on all three emulators and its installed SHA-256 matched the artifact. The new-player launch on API28 displayed the default-checked notice through the factory hook. Production Support/notice views were inspected on API28/32/33. On API33, public preview was opened and cancelled; Save ZIP completed through the real DocumentsUI picker, and the saved ZIP was read and checked for the exact project/schema/build and its two allowed entries. All production emulator installations were left with explicit automatic opt-out and zero queued reports, avoiding real issue submissions. Existing signing/package settings were preserved; no signed-in vehicle/account data was used in these tests.

Read-only production HTTPS checks returned healthy API v1, schema versions `[1,2]`, intake/delivery enabled and automatic reporting/grouping enabled. **No Spotify production report canary or public test issue was created.** Common server connectivity or fake delivery is not an end-to-end verification of this client on a real car.

## Changed source areas and remaining checks

New client files: `DiagnosticActivity`, `DiagnosticNoticeActivity`, `DiagnosticLog`, `DiagnosticSchema`, `DiagnosticReport`, `DiagnosticReportProvider`, `DiagnosticsConfig`, `DiagnosticsQueue`, `DiagnosticTransport`, `DiagnosticHttps`, `DiagnosticUploadJob`, `AutomaticDiagnostics` and `DiagnosticText`. Settings/provider/factory and playback/BYD/wheel hooks integrate them. Build generators, manifest patching, audit, adapter README and `adapter/tests/diagnostics` were updated. Internal shared support/server materials remain local and ignored; no other project's code was changed.

Still required: install this APK as an update on the car, confirm UI/first-use consent and signed-in data preservation, exercise a real safe support case and verify the resulting receipt/issue when explicitly authorized. Physical DiLink 5.1 / Android 13 lighting compatibility, vehicle sleep/reboot and two-account playback remain unverified. Source/APK preparation does not change the installed vehicle version or publish the release.

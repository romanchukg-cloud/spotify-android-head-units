# Panel provider: Spotify's compact player in another app's panel

Another app (the host) can show Spotify's compact player in a panel of its own. Spotify draws the block in its own process and hands the host a surface (`SurfaceControlViewHost`, Android 11 / API 30 and newer); the host puts it into a `SurfaceView`. Touches reach Spotify through the system: no virtual display, no ADB, no injected touches. The first host is our Google Maps Automotive patch for BYD (`com.google.android.apps.maps.automotive`).

Source: `src/local/spotify/unified/PanelProviderService.java` (service and protocol), `PanelPlayerView.java` (the block), `PanelAccess.java` (allowed hosts); the debug-only test host is `src-debug/local/spotify/unified/PanelTestActivity.java`.

## Finding the provider

| Item | Value |
|---|---|
| Service | `com.spotify.music/local.spotify.unified.PanelProviderService` (process `:bydui`, exported, no permission) |
| Intent action | `local.byd.panel.action.PROVIDER` — find providers with `PackageManager.queryIntentServices(new Intent(ACTION), PackageManager.GET_META_DATA)` |
| `local.byd.panel.label` | string, `Spotify` |
| `local.byd.panel.min_width_dp` | integer, `280` |
| `local.byd.panel.min_height_dp` | integer, `160` |

The host must see the provider's package (Android 11+): declare `<queries><intent><action android:name="local.byd.panel.action.PROVIDER"/></intent></queries>`.

Bind with `bindService(intent.setComponent(found), connection, Context.BIND_AUTO_CREATE)`; on Android 14+ add `Context.BIND_ALLOW_ACTIVITY_STARTS`, so that a tap on "Open Spotify" in the block may open Spotify while the host is visible (Android 10–13 allow it for a service bound by a visible app).

Binding has no side effects: it starts Spotify's light interface process only. It plays nothing (the auto-resume and "play when opening Spotify" paths run only when Spotify's own screen opens), opens no screen and changes no profile or scale. The block joins Spotify's media session only once the Spotify backend runs; until then, and when signed out, it shows "Open Spotify". Connected with nothing playing, it shows "Nothing is playing" with the controls (play resumes Spotify's last context).

## Protocol (version 1)

`Messenger` messages, no AIDL. The host sends a `Message` with `replyTo` set to its own `Messenger`; data travel in `Message.getData()`.

| `what` | From | Data (`Bundle`) | Answer |
|---|---|---|---|
| 1 `OPEN` | host | `version` int = 1; `hostToken` IBinder (`Bundle.putBinder`, the value of `SurfaceView.getHostToken()`); `displayId` int; `widthPx`, `heightPx` int (the SurfaceView's size); `densityDpi` int (the host's); `night` boolean | 101 `OPENED` or 199 `ERROR` |
| 2 `RESIZE` | host | `widthPx`, `heightPx` | none (the block relayouts) |
| 3 `THEME` | host | `night` | none (the block redraws in the host's theme) |
| 4 `CLOSE` | host | — | none (the block and its surface are released) |
| 101 `OPENED` | Spotify | `version` int = 1; `surfacePackage` (`SurfaceControlViewHost.SurfacePackage`) | the host calls `surfaceView.setChildSurfacePackage(surfacePackage)` |
| 102 `CLOSED` | Spotify | `reason` string (`provider_stopped`, `error`) | the provider closed the block itself; the host may OPEN again |
| 199 `ERROR` | Spotify | `code` string, `message` string | see below |

- One block per host uid: a new `OPEN` replaces the previous block. `RESIZE`, `THEME` and `CLOSE` without an open block are ignored.
- The block is released on `CLOSE`, when the host unbinds and when the host's process dies (death of its `replyTo` binder).
- Everything runs on Spotify's main thread; the host may send from any thread.

### Error codes

| `code` | Meaning |
|---|---|
| `forbidden` | The sending app is not an allowed host (package and signing certificate); nothing is shown |
| `unsupported` | The device runs Android 10 or older (no `SurfaceControlViewHost`) |
| `unsupported_version` | `version` is not 1 |
| `bad_request` | `hostToken`, `widthPx` or `heightPx` missing, or an unknown `what` |
| `bad_display` | No display with this `displayId` |
| `internal` | The block failed; any open block of this host was closed |

## Who may connect

Each message is checked by its `Message.sendingUid`: one of the packages of that uid must be an allowed host, and the SHA-256 of that package's current signing certificate (`GET_SIGNING_CERTIFICATES`; exactly one signer) must match. Allowed hosts (`PanelAccess.HOSTS`, one line per host):

| Package | Certificate SHA-256 |
|---|---|
| `com.google.android.apps.maps.automotive` | `a8401545b03562933c095667ae0eb54bab189d8571010e4eb3384753ad6508d5` |

Spotify's own uid is allowed only in debug builds (`HU_DEBUG=1`, for the test host). Release builds refuse it, and the audit (`tools/apk_audit.py`) checks that a release has neither the test host nor the debug flag. `tests/panel/run.sh` checks the rules on the JVM.

## The block

- Cover, title and artist, previous / play-pause / next and a thin progress line. Touch targets are at least 48 dp.
- A panel taller than wide gets a column (a side panel in landscape); otherwise a strip (a bottom panel in portrait). `RESIZE` switches between them.
- Colours follow `night`: a black block by night, a white one by day, to sit on the host's black or white card. The block is an opaque rectangle: keep the SurfaceView inside the card's padding, away from its rounded corners.
- Sizes use the host's `densityDpi`.
- A tap on the cover or the title opens Spotify; so does "Open Spotify".

## Host checklist

1. Find the provider (action, meta-data) and bind (see above).
2. Lay out the `SurfaceView`, call `setZOrderOnTop(true)` (touches go to the embedded block only when its surface is above the host's window), and wait for `surfaceCreated`.
3. Send `OPEN` with `replyTo`; on `OPENED` call `setChildSurfacePackage`. Open once per SurfaceView: an attached package survives the SurfaceView's surface being destroyed and created again (the host activity stopped and started), so do not send `OPEN` again on every `surfaceCreated`.
   - To show a new block (after `CLOSE`, `CLOSED`, `ERROR`, or to `OPEN` again) use a new SurfaceView: Android 11 and 12 (checked on Android 12) detach the old package without attaching the new one to a SurfaceView that already had one, and the panel stays black.
4. Send `RESIZE` after the SurfaceView's size changes and `THEME` when day/night changes.
5. Send `CLOSE` and unbind when the panel goes away; on `CLOSED` or `ERROR` show the host's own placeholder.

## Testing

A debug build (`HU_DEBUG=1 ./build.sh`, output `Spotify-5.5.0-HeadUnit-test.N-debug.apk`) adds `PanelTestActivity` in its own process `:paneltest`: `adb shell am start -n com.spotify.music/local.spotify.unified.PanelTestActivity`. Its buttons: Open, Resize (column 300×560 dp ↔ strip 720×180 dp), Theme, Close and Exit (the host process dies). Never publish a debug build.

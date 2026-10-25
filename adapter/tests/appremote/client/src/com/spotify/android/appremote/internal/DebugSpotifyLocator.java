package com.spotify.android.appremote.internal;
import android.content.Context;
/**
 * The SDK looks for this class before its release locator, which accepts only Spotify's own
 * signing certificates: the test client points it at our build of com.spotify.music.
 */
public final class DebugSpotifyLocator implements PackageProvider {
 public DebugSpotifyLocator(){}
 public String getPackageName(Context c){return "com.spotify.music";}
}

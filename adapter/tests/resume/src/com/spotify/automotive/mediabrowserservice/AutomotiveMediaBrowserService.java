package com.spotify.automotive.mediabrowserservice;
import android.service.media.MediaBrowserService;
import android.media.browse.MediaBrowser;
import android.media.session.*;
import android.os.*;
import java.util.*;
/** Synthetic queue. No Spotify credentials, network, audio or SDK are used. */
public class AutomotiveMediaBrowserService extends MediaBrowserService {
 public static volatile MediaSession session;public static volatile int plays;
 public void onCreate(){super.onCreate();session=new MediaSession(this,"Resume readiness fixture");session.setCallback(new MediaSession.Callback(){public void onPlay(){plays++;}},new Handler(Looper.getMainLooper()));set(0,0);session.setActive(true);setSessionToken(session.getSessionToken());}
 public static void set(int state,long actions){session.setPlaybackState(new PlaybackState.Builder().setState(state,0,1).setActions(actions).build());}
 public BrowserRoot onGetRoot(String p,int uid,Bundle extras){return new BrowserRoot("root",null);}
 public void onLoadChildren(String id,Result<List<MediaBrowser.MediaItem>> result){result.sendResult(Collections.emptyList());}
 public void onDestroy(){session.release();session=null;super.onDestroy();}
}

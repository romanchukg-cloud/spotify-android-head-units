package local.spotify.unified;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
/**
 * Spotify's "Device Broadcast Status" broadcasts (developer.spotify.com, Android media
 * notifications), which the Automotive backend does not send: other apps on the unit (launchers,
 * scrobblers, head-up displays) learn the track (metadatachanged: id, artist, album, track, length
 * in seconds), play/pause and seeks (playbackstatechanged: playing, playbackPosition in ms) and
 * queue changes (queuechanged), each with timeSent. Sent from the backend's process as its media
 * session changes, unless turned off in Head unit settings.
 */
final class HuBroadcasts {
 static final String METADATA="com.spotify.music.metadatachanged",PLAYBACK="com.spotify.music.playbackstatechanged",QUEUE="com.spotify.music.queuechanged";
 private static String track;private static boolean playing,known;private static long position,at;
 private HuBroadcasts(){}

 /** A new track (the session's metadata names another one); a session without a track sends nothing. */
 static synchronized void metadata(Context c,Object value){
  String title=text(value,"android.media.metadata.TITLE");if(title==null)return;
  String uri=text(value,"android.media.metadata.MEDIA_URI"),artist=text(value,"android.media.metadata.ARTIST"),key=uri!=null?uri:title+"\n"+artist;
  if(key.equals(track))return;track=key;
  Object length=HeadUnitRuntime.call(value,"getLong","android.media.metadata.DURATION");
  send(c,new Intent(METADATA).putExtra("id",uri).putExtra("artist",artist).putExtra("album",text(value,"android.media.metadata.ALBUM")).putExtra("track",title)
   .putExtra("length",length instanceof Long?(int)Math.max(0,(Long)length/1000):0));
 }
 /** Play/pause, or a jump of the position (a seek), as Spotify sends them; not every position update. */
 static synchronized void playback(Context c,Object value){
  Object st=HeadUnitRuntime.call(value,"getState"),pos=HeadUnitRuntime.call(value,"getPosition"),upd=HeadUnitRuntime.call(value,"getLastPositionUpdateTime");
  if(!(st instanceof Integer))return;
  int state=(Integer)st;if(state==0 || state==8)return;
  boolean now=state==HuSession.STATE_PLAYING || state==HuSession.STATE_BUFFERING;
  long p=pos instanceof Long?Math.max(0,(Long)pos):0,t=upd instanceof Long && (Long)upd>0?(Long)upd:SystemClock.elapsedRealtime();
  long expected=position+(playing?t-at:0);
  boolean changed=!known || now!=playing || Math.abs(p-expected)>=3000;
  known=true;playing=now;position=p;at=t;
  if(changed)send(c,new Intent(PLAYBACK).putExtra("playing",now).putExtra("playbackPosition",(int)Math.min(Integer.MAX_VALUE,p)));
 }
 static void queue(Context c){send(c,new Intent(QUEUE));}

 private static void send(Context c,Intent i){
  try{if(!HeadUnitPreferences.broadcast())return;}catch(RuntimeException e){return;}
  c.sendBroadcast(i.putExtra("timeSent",System.currentTimeMillis()));
 }
 private static String text(Object m,String key){Object t=HeadUnitRuntime.call(m,"getText",key);if(t==null)t=HeadUnitRuntime.call(m,"getString",key);return t==null?null:t.toString();}
}

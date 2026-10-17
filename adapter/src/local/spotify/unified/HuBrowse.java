package local.spotify.unified;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.view.*;
import android.widget.*;
import java.util.*;
/**
 * Browse items of the Spotify backend for our interface: their texts, covers and group titles,
 * and the cover card used by Home, Library, Recents and Browse.
 */
final class HuBrowse {
 static final String GROUP="android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT";
 private HuBrowse(){}
 static String id(MediaItem i){return i.getMediaId();}
 static String title(MediaItem i){MediaDescriptionCompat d=i.getDescription();return d==null || d.getTitle()==null?"":d.getTitle().toString();}
 static String subtitle(MediaItem i){MediaDescriptionCompat d=i.getDescription();return d==null || d.getSubtitle()==null?"":d.getSubtitle().toString();}
 static String icon(MediaItem i){MediaDescriptionCompat d=i.getDescription();return d==null || d.getIconUri()==null?null:d.getIconUri().toString();}
 static String group(MediaItem i){MediaDescriptionCompat d=i.getDescription();Bundle e=d==null?null:d.getExtras();String g=e==null?null:e.getString(GROUP);return g==null?"":g.trim();}
 static boolean artist(MediaItem i){String id=id(i);return id!=null && id.startsWith("spotify:artist:");}
 private static Bundle extras(MediaItem i){MediaDescriptionCompat d=i.getDescription();return d==null?null:d.getExtras();}
 static long duration(MediaItem i){Bundle e=extras(i);return e==null?0:Math.max(0,e.getLong("android.media.metadata.DURATION"));}
 static boolean explicit(MediaItem i){Bundle e=extras(i);return e!=null && e.getLong("android.media.IS_EXPLICIT")==1;}
 static int number(MediaItem i){Bundle e=extras(i);return e==null?0:(int)e.getLong("android.media.metadata.TRACK_NUMBER");}
 /** The item's browser actions (play, download, downloading, downloaded). */
 static java.util.ArrayList<String> actions(MediaItem i){Bundle e=extras(i);java.util.ArrayList<String> a=e==null?null:e.getStringArrayList("androidx.media.utils.extras.CUSTOM_BROWSER_ACTION_ID_LIST");return a==null?new java.util.ArrayList<>():a;}
 /** The Spotify URI of a media id, without the browse tree's query (?ref=…&pos=…). */
 static String uri(String id){if(id==null)return null;int q=id.indexOf('?');return q<0?id:id.substring(0,q);}
 /** playlist, album, artist or show (podcasts and audiobooks) for a page's media id, or null. */
 static String kind(String id){String u=uri(id);if(u==null)return null;for(String k:new String[]{"playlist","album","artist","show"})if(u.startsWith("spotify:"+k+":"))return k;return null;}
 /** The first child whose id starts with the prefix (for example spotify:home in the root). */
 static MediaItem find(List<MediaItem> items,String prefix){for(MediaItem i:items)if(id(i)!=null && id(i).startsWith(prefix))return i;return null;}
 /** Items in their order, grouped by the backend's group titles. */
 static LinkedHashMap<String,List<MediaItem>> groups(List<MediaItem> items){
  LinkedHashMap<String,List<MediaItem>> out=new LinkedHashMap<>();
  for(MediaItem i:items)out.computeIfAbsent(group(i),k->new ArrayList<>()).add(i);
  return out;
 }
 /** A cover card: square (round for artists) cover, title and subtitle. */
 static final class Card extends LinearLayout {
  final ImageView cover;final TextView title,subtitle;final int size;
  Card(HuUi ui,int size){
   super(ui.activity);this.size=size;setOrientation(VERTICAL);setPadding(ui.dp(8),ui.dp(8),ui.dp(8),ui.dp(10));
   setBackground(ui.pressable(0x00000000,ui.dp(14)));
   cover=new ImageView(ui.activity);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setClipToOutline(true);addView(cover,new LayoutParams(size,size));
   title=ui.label("",18,ui.text,false);title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.END);title.setPadding(0,ui.dp(10),0,0);
   subtitle=ui.label("",15,ui.secondary,false);subtitle.setSingleLine(true);subtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
   addView(title,new LayoutParams(size,-2));addView(subtitle,new LayoutParams(size,-2));
   this.ui=ui;
  }
  private final HuUi ui;
  void bind(MediaItem item,View.OnClickListener click){
   boolean round=artist(item);cover.setBackground(HuUi.round(ui.control,round?size/2f:ui.dp(8)));
   cover.setImageDrawable(null);HuCovers.into(cover,icon(item),size);
   title.setText(title(item));String s=subtitle(item);subtitle.setText(s);subtitle.setVisibility(s.isEmpty()?GONE:VISIBLE);
   title.setGravity(round?Gravity.CENTER:Gravity.START);subtitle.setGravity(round?Gravity.CENTER:Gravity.START);
   setContentDescription(title(item));setOnClickListener(click);
  }
 }
}

package local.spotify.unified;
import android.os.*;
import java.util.ArrayList;
/** Convert legacy media IPC payloads to the relocated UI's matching parcel types. */
public final class ParcelBridge {
 /** Parcelables the interface sends to the backend (a browser request's ResultReceiver, a queue
  * command's MediaDescriptionCompat) must use the backend's class name on the wire: both classes
  * exist in this APK, so the backend would otherwise create the relocated one and fail its cast
  * (a queue command crashed the music process). The parcel formats are the same. */
 public static void putBackendParcelable(Bundle bundle,String key,Parcelable value) {
  if(value!=null && value.getClass().getName().startsWith("local.bydui.android.support.v4.")) {
   try {
    Class<?> target=Class.forName(value.getClass().getName().substring("local.bydui.".length()));
    Parcelable.Creator<?> creator=(Parcelable.Creator<?>)target.getField("CREATOR").get(null);
    Parcel parcel=Parcel.obtain();
    try {
     value.writeToParcel(parcel,0);parcel.setDataPosition(0);
     value=(Parcelable)creator.createFromParcel(parcel);
    } finally {parcel.recycle();}
   } catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot translate parcel for the backend",e);}
  }
  bundle.putParcelable(key,value);
 }
 private static Parcelable adapt(Parcelable value) {
  if(value==null || !value.getClass().getName().startsWith("android.support.v4.media."))return value;
  Parcel parcel=Parcel.obtain();
  try {
   Class<?> target=Class.forName("local.bydui."+value.getClass().getName());
   Parcelable.Creator<?> creator=(Parcelable.Creator<?>)target.getField("CREATOR").get(null);
   value.writeToParcel(parcel,0);parcel.setDataPosition(0);
   return (Parcelable)creator.createFromParcel(parcel);
  } catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot translate media parcel",e);}
  finally {parcel.recycle();}
 }
 public static Parcelable getParcelable(Bundle b,String key){return adapt(b.getParcelable(key));}
 /** SavedStateHandle stores its keys (Strings) through this call too, so only Parcelables are adapted. */
 @SuppressWarnings({"unchecked","rawtypes"})
 public static ArrayList<Parcelable> getParcelableArrayList(Bundle b,String key){
  ArrayList source=b.getParcelableArrayList(key);
  if(source==null)return null;
  ArrayList out=new ArrayList(source.size());for(Object o:source)out.add(o instanceof Parcelable?adapt((Parcelable)o):o);return out;
 }
 public static Parcelable[] getParcelableArray(Bundle b,String key){
  Parcelable[] source=b.getParcelableArray(key);if(source==null)return null;
  Parcelable[] out=new Parcelable[source.length];for(int i=0;i<source.length;i++)out[i]=adapt(source[i]);return out;
 }
}

package local.spotify.unified;
import android.os.*;
import java.util.ArrayList;
/** Convert legacy media IPC payloads to the relocated UI's matching parcel types. */
public final class ParcelBridge {
 /** Browser request callbacks must use the backend's class name on the wire.
  * Both implementations preserve the same IResultReceiver Binder protocol. */
 public static void putBrowserParcelable(Bundle bundle,String key,Parcelable value) {
  try {
   Class<?> localReceiver=Class.forName("local.bydui.android.support.v4.os.ResultReceiver");
   if(localReceiver.isInstance(value)) {
    Class<?> receiver=Class.forName("android.support.v4.os.ResultReceiver");
    Parcelable.Creator<?> creator=(Parcelable.Creator<?>)receiver.getField("CREATOR").get(null);
    Parcel parcel=Parcel.obtain();
    try {
     value.writeToParcel(parcel,0);parcel.setDataPosition(0);
     value=(Parcelable)creator.createFromParcel(parcel);
    } finally {parcel.recycle();}
   }
  } catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot translate browser callback",e);}
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
 public static ArrayList<Parcelable> getParcelableArrayList(Bundle b,String key){
  ArrayList<Parcelable> source=b.getParcelableArrayList(key);
  if(source==null)return null;
  ArrayList<Parcelable> out=new ArrayList<>(source.size());for(Parcelable p:source)out.add(adapt(p));return out;
 }
 public static Parcelable[] getParcelableArray(Bundle b,String key){
  Parcelable[] source=b.getParcelableArray(key);if(source==null)return null;
  Parcelable[] out=new Parcelable[source.length];for(int i=0;i<source.length;i++)out[i]=adapt(source[i]);return out;
 }
}

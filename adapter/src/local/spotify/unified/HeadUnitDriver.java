package local.spotify.unified;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import com.byd.spi.ipc.cursor.BinderCursor;
/**
 * The driver's side, for the interface's controls. "Auto" asks the car: on BYD its car service's
 * driver seat (the call the BYD player app made through BYD's DiCar library), on Android
 * Automotive the occupant zone service. Other head units have no such information and stay
 * left-hand drive unless right-hand drive is set in Head unit settings. Seats are numbered as in
 * VehicleAreaSeat: 1 the front left seat, 4 the front right one.
 */
public final class HeadUnitDriver {
 static final String AUTO="auto",LEFT="left",RIGHT="right";
 static final int SEAT_LEFT=1,SEAT_RIGHT=4;
 private static Integer seat;
 private HeadUnitDriver(){}
 /** Right-hand drive: by the setting ("left", "right"), or as the car says ("auto"). */
 static boolean right(Context c,String setting){
  if(RIGHT.equals(setting))return true;
  if(LEFT.equals(setting))return false;
  return detected(c)==SEAT_RIGHT;
 }
 /** The driver's seat as the car reports it, or 0 when it says nothing. Asked once per process. */
 static synchronized int detected(Context c){
  if(seat==null){
   int s=0;
   if(HeadUnitProfile.isByd())s=byd(c);
   if(s==0)s=automotive(c);
   seat=s;Log.i("SpotifyHU","Driver seat="+s);
  }
  return seat;
 }
 /** BYD: the car service's binder from its provider, then ICarInfoService.getDriverSeat (transaction 1). */
 private static int byd(Context c){
  // As BYD's DiCar/Spi client asks: the service's interface name is the one selection argument.
  try(Cursor cursor=c.getContentResolver().query(Uri.parse("content://com.byd.car.server.provider.CarServiceProvider/sync_binder"),null,null,new String[]{"com.byd.car.carinfo.ICarInfoService"},null)){
   if(cursor==null){Log.i("SpotifyHU","BYD car service not found");return 0;}
   Bundle extras=cursor.getExtras();extras.setClassLoader(BinderCursor.class.getClassLoader());
   Parcelable holder=extras.getParcelable("binder");
   IBinder binder=holder instanceof BinderCursor.BinderParcelable?((BinderCursor.BinderParcelable)holder).binder:null;
   if(binder==null){Log.i("SpotifyHU","BYD car info service unavailable");return 0;}
   Parcel data=Parcel.obtain(),reply=Parcel.obtain();
   try{
    data.writeInterfaceToken("com.byd.car.carinfo.ICarInfoService");
    if(!binder.transact(1,data,reply,0))return 0;
    reply.readException();return reply.readInt();
   }finally{data.recycle();reply.recycle();}
  }catch(RuntimeException | RemoteException e){Log.w("SpotifyHU","BYD driver seat unavailable",e);return 0;}
 }
 /** Android Automotive (Android 11+): CarOccupantZoneManager.getDriverSeat, through the android.car library. */
 private static int automotive(Context c){
  if(Build.VERSION.SDK_INT<30 || !c.getPackageManager().hasSystemFeature("android.hardware.type.automotive"))return 0;
  try{
   Class<?> car=Class.forName("android.car.Car");
   Object connection=car.getMethod("createCar",Context.class).invoke(null,c);
   if(connection==null)return 0;
   try{
    Object zones=car.getMethod("getCarManager",String.class).invoke(connection,"car_occupant_zone_service");
    return zones==null?0:(Integer)zones.getClass().getMethod("getDriverSeat").invoke(zones);
   }finally{car.getMethod("disconnect").invoke(connection);}
  }catch(ReflectiveOperationException | RuntimeException | LinkageError e){Log.w("SpotifyHU","Car driver seat unavailable",e);return 0;}
 }
}

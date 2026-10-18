package com.byd.spi.ipc.cursor;
import android.os.*;
/**
 * The class name under which BYD's car service hands out its binder: a Parcelable holding one
 * strong binder. Only this name and format are shared with BYD; HeadUnitDriver reads it to ask
 * the car which seat is the driver's.
 */
public final class BinderCursor {
 private BinderCursor(){}
 public static final class BinderParcelable implements Parcelable {
  public final IBinder binder;
  BinderParcelable(IBinder binder){this.binder=binder;}
  public int describeContents(){return 0;}
  public void writeToParcel(Parcel p,int flags){p.writeStrongBinder(binder);}
  public static final Creator<BinderParcelable> CREATOR=new Creator<BinderParcelable>(){
   public BinderParcelable createFromParcel(Parcel p){return new BinderParcelable(p.readStrongBinder());}
   public BinderParcelable[] newArray(int n){return new BinderParcelable[n];}
  };
 }
}

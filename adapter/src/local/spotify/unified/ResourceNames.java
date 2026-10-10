package local.spotify.unified;
import android.content.res.Resources;
public final class ResourceNames {
 public static int getIdentifier(Resources r,String name,String type,String pkg){
  if("com.spotify.music".equals(pkg)){
   int id=r.getIdentifier(name,type,"com.spotify.music.ui");
   if(id!=0)return id;
  }
  return r.getIdentifier(name,type,pkg);
 }
}

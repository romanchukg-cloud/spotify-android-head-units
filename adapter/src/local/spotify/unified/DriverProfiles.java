package local.spotify.unified;
import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Base64;
import org.json.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Runs only in the backend process. Credentials never leave this process. */
public final class DriverProfiles {
 private static volatile Object storage;
 private static final long PROCESS_START=android.os.Process.getStartUptimeMillis();
 public static void bindStorage(Object value){storage=value;}
 public static void bindCandidate(Object value,Object client){if(client!=null && client.getClass().getName().equals("bh.b"))bindStorage(value);}
 public static boolean restartPending(Context c){return c.getSharedPreferences("driver_profile_transition",0).getInt("pid",0)==android.os.Process.myPid() && c.getSharedPreferences("driver_profile_transition",0).getLong("started",-1)==PROCESS_START;}
 private static Bundle restartResult(Context c,boolean qr){
  Bundle b=new Bundle();b.putBoolean("ready",true);b.putBoolean("restart",true);b.putBoolean("qr",qr);b.putInt("backendPid",android.os.Process.myPid());
  ActivityManager manager=c.getSystemService(ActivityManager.class);
  java.util.List<ActivityManager.RunningAppProcessInfo> running=manager.getRunningAppProcesses();
  if(running!=null)for(ActivityManager.RunningAppProcessInfo p:running)
   if(p.uid==android.os.Process.myUid() && p.processName.equals(c.getPackageName()+":bydui"))b.putInt("uiPid",p.pid);
  return b;
 }
 private static Object field(Object object,String name)throws Exception {return object.getClass().getField(name).get(object);}
 private static Object single(Object source)throws Exception {
  Class<?> type=Class.forName("io.reactivex.rxjava3.core.Single");
  Object limited=type.getMethod("timeout",long.class,TimeUnit.class).invoke(source,8L,TimeUnit.SECONDS);
  return type.getMethod("blockingGet").invoke(limited);
 }
 private static Object current(Object store)throws Exception {
  Object result=single(store.getClass().getMethod("d").invoke(store));
  String type=result.getClass().getName();if(type.equals("cb.f"))return field(result,"a");if(type.equals("cb.e"))return null;
  throw new IllegalStateException("Cannot read active account");
 }
 private static String username(Object user)throws Exception {return user==null?"":(String)field(user,"j");}
 private static String saveCurrent(JSONObject vault,Object user)throws Exception {
  String username=username(user);if(username.isEmpty())return "";
  byte[] credential=(byte[])field(field(user,"l"),"k");
  if(credential.length==0)throw new IllegalStateException("No reusable login");
  JSONArray profiles=vault.getJSONArray("profiles");JSONObject profile=null;
  for(int i=0;i<profiles.length();i++)if(username.equals(profiles.getJSONObject(i).getString("user"))){profile=profiles.getJSONObject(i);break;}
  if(profile==null){
   if(profiles.length()>=8)throw new IllegalStateException("Profile limit reached");
   profile=new JSONObject().put("id",UUID.randomUUID().toString()).put("name",username).put("user",username);profiles.put(profile);
  }
  profile.put("credential",Base64.encodeToString(credential,Base64.NO_WRAP));
  return profile.getString("id");
 }
 private static JSONObject find(JSONObject vault,String id)throws Exception {
  JSONArray profiles=vault.getJSONArray("profiles");
  for(int i=0;i<profiles.length();i++)if(profiles.getJSONObject(i).getString("id").equals(id))return profiles.getJSONObject(i);
  throw new IllegalArgumentException("Unknown profile");
 }
 private static void checkStored(Object result)throws Exception {
  if(result!=Class.forName("cb.c").getField("a").get(null))throw new IllegalStateException("Account store rejected change");
 }
 private static void clearSlot(Object store)throws Exception {
  Object result=single(store.getClass().getMethod("l").invoke(store));
  if(result!=Class.forName("cb.c").getField("a").get(null) && result!=Class.forName("cb.a").getField("c").get(null))throw new IllegalStateException("Account slot could not be cleared");
 }
 public static synchronized Bundle call(Context context,String method,String id,Bundle extras)throws Exception {
  if(restartPending(context))return restartResult(context,context.getSharedPreferences("driver_profile_transition",0).getBoolean("qr",false));
  Object store=storage;Bundle response=new Bundle();
  if(store==null){response.putBoolean("ready",false);return response;}
  ProfileVault disk=new ProfileVault(context);JSONObject vault=disk.read();
  Object user=current(store);String active=saveCurrent(vault,user);disk.write(vault);
  response.putBoolean("ready",true);
  if("list".equals(method)){
   JSONArray profiles=vault.getJSONArray("profiles");ArrayList<String> ids=new ArrayList<>(),names=new ArrayList<>();
   for(int i=0;i<profiles.length();i++){JSONObject p=profiles.getJSONObject(i);ids.add(p.getString("id"));names.add(p.getString("name"));}
   response.putStringArrayList("ids",ids);response.putStringArrayList("names",names);response.putString("active",active);return response;
  }
  if("rename".equals(method)){
   String name=extras==null?"":extras.getString("name","").trim();
   if(name.isEmpty() || name.length()>40)throw new IllegalArgumentException("Invalid profile name");
   find(vault,id).put("name",name);disk.write(vault);return response;
  }
  if("remove".equals(method)){
   if(id==null || id.equals(active))throw new IllegalArgumentException("Active profile cannot be removed");
   find(vault,id);JSONArray old=vault.getJSONArray("profiles"),remaining=new JSONArray();
   for(int i=0;i<old.length();i++)if(!id.equals(old.getJSONObject(i).getString("id")))remaining.put(old.getJSONObject(i));
   vault.put("profiles",remaining);disk.write(vault);return response;
  }
  if(!"restore".equals(method) && !"add".equals(method))throw new IllegalArgumentException("Unknown profile operation");
  Object target=null;
  if("restore".equals(method)){
   JSONObject profile=find(vault,id);String name=profile.getString("user");byte[] credential=Base64.decode(profile.getString("credential"),Base64.NO_WRAP);
   if(credential.length==0)throw new IllegalStateException("Missing reusable login");
   Class<?> blob=Class.forName("ya.c"),info=Class.forName("ya.e"),token=Class.forName("ya.a");
   Object auth=blob.getConstructor(String.class,byte[].class).newInstance(name,credential);
   target=info.getConstructor(String.class,token,blob).newInstance(name,null,auth);
  } else if(vault.getJSONArray("profiles").length()>=8)throw new IllegalStateException("Profile limit reached");
  // The saved vault is committed before changing the SDK's active-user slot.
  // Failures restore that slot before returning; the caller never restarts then.
  local.spotify.close.PlaybackGate.beginAccountTransition(context);
  try {
   // storeUser does not replace an existing slot (userAlreadyExists).
   clearSlot(store);
   if(target!=null)checkStored(single(store.getClass().getMethod("n",Class.forName("ya.e")).invoke(store,target)));
   if(!context.getSharedPreferences("driver_profile_transition",0).edit().putInt("pid",android.os.Process.myPid()).putLong("started",PROCESS_START).putBoolean("qr",target==null).commit())throw new IllegalStateException("Transition save failed");
  } catch(Exception failure){
   boolean recovered=false;
   try{
    if(!username(current(store)).equals(username(user))){clearSlot(store);if(user!=null)checkStored(single(store.getClass().getMethod("n",Class.forName("ya.e")).invoke(store,user)));}
    recovered=true;
   }catch(Exception ignored){}
   if(recovered)local.spotify.close.PlaybackGate.abortAccountTransition(context);
   throw failure;
  }
  return restartResult(context,target==null);
 }
}

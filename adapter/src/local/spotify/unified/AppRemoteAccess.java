package local.spotify.unified;
import java.util.*;
/**
 * Who may connect to our App Remote service (HuAppRemoteService): a known client package whose
 * current signing certificate has one of its listed SHA-256 digests (Waze from Google Play, and
 * Waze for BYD with its own key). This app itself, the SDK test client (tests/appremote/client)
 * and a diagnostic Waze signed with the project's key are allowed only in debug builds. Plain
 * Java, so the rules are checked on the JVM (tests/appremote). HuWazeLink uses the same rules.
 */
final class AppRemoteAccess {
 /** Allowed clients: package name and the SHA-256 of its signing certificate (hex, lower case). */
 static final String[][] CLIENTS={
  {"com.waze","03637f6c5d8f604e6fdb79a6ffbfa578de4e318f8da22fc6106665247f8807d7"},
  {"com.waze","50120cc57caa95d380fdf280e3a9c0853ed09ec8a617fe7bed33893c39b66276"},
 };
 /**
  * Debug builds only: the test client on Spotify's own SDK and a diagnostic copy of Waze, both
  * signed with the project's key.
  */
 static final String[][] TEST_CLIENTS={
  {"hu.appremote","a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1"},
  {"com.waze","a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1"},
 };
 private AppRemoteAccess(){}
 /**
  * The client package a message comes from, or null when it is not allowed. uid is the message's
  * sending uid; packages are those of that uid. A client must be signed by exactly one certificate.
  */
 static String client(int uid,int ownUid,boolean debug,String ownPackage,String[] packages,PanelAccess.Certificates certificates){
  if(uid<0)return null;
  if(uid==ownUid)return debug?ownPackage:null;
  if(packages==null)return null;
  List<String[]> clients=new ArrayList<>(Arrays.asList(CLIENTS));if(debug)clients.addAll(Arrays.asList(TEST_CLIENTS));
  for(String[] allowed:clients)for(String p:packages){
   if(!allowed[0].equals(p))continue;
   String[] digests=certificates.of(p);
   if(digests!=null && digests.length==1 && allowed[1].equalsIgnoreCase(digests[0]))return p;
  }
  return null;
 }
}

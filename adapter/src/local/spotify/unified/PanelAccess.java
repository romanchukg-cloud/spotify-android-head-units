package local.spotify.unified;
/**
 * Who may open a panel block (PanelProviderService): an allowed host package whose current
 * signing certificate has the listed SHA-256 digest. This app itself is allowed only in debug
 * builds (the test host). Plain Java, so the rules are checked on the JVM (tests/panel).
 */
final class PanelAccess {
 /** Allowed hosts: package name and the SHA-256 of its signing certificate (hex, lower case). */
 static final String[][] HOSTS={
  {"com.google.android.apps.maps.automotive","a8401545b03562933c095667ae0eb54bab189d8571010e4eb3384753ad6508d5"},
 };
 /** The SHA-256 digests of a package's current signers, or null when it is not installed or visible. */
 interface Certificates {String[] of(String pkg);}
 private PanelAccess(){}
 /**
  * The host package a message comes from, or null when it is not allowed. uid is the message's
  * sending uid; packages are those of that uid. A host must be signed by exactly one certificate.
  */
 static String host(int uid,int ownUid,boolean debug,String ownPackage,String[] packages,Certificates certificates){
  if(uid<0)return null;
  if(uid==ownUid)return debug?ownPackage:null;
  if(packages==null)return null;
  for(String[] allowed:HOSTS)for(String p:packages){
   if(!allowed[0].equals(p))continue;
   String[] digests=certificates.of(p);
   if(digests!=null && digests.length==1 && allowed[1].equalsIgnoreCase(digests[0]))return p;
  }
  return null;
 }
}

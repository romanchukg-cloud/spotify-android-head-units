package local.spotify.unified;
import java.util.*;
/** Plain-JVM checks of the panel provider's caller rules (PanelAccess). */
public final class PanelAccessTest {
 private static int passed;
 private static void ok(boolean condition,String name){if(!condition)throw new AssertionError(name);passed++;}
 public static void main(String[] args){
  String maps="com.google.android.apps.maps.automotive",cert="a8401545b03562933c095667ae0eb54bab189d8571010e4eb3384753ad6508d5";
  String own="com.spotify.music";int me=10100,host=10200,other=10300;
  Map<String,String[]> installed=new HashMap<>();
  installed.put(maps,new String[]{cert});installed.put("com.example.other",new String[]{cert});
  PanelAccess.Certificates certs=installed::get;
  ok(maps.equals(PanelAccess.host(host,me,false,own,new String[]{maps},certs)),"allowed package with its certificate");
  ok(maps.equals(PanelAccess.host(host,me,false,own,new String[]{maps},p->new String[]{cert.toUpperCase()})),"certificate digest compared case-insensitively");
  ok(PanelAccess.host(other,me,false,own,new String[]{"com.example.other"},certs)==null,"other package refused, even with the same certificate");
  ok(PanelAccess.host(host,me,false,own,new String[]{maps},p->new String[]{"00"+cert.substring(2)})==null,"allowed package with another certificate refused");
  ok(PanelAccess.host(host,me,false,own,new String[]{maps},p->new String[]{cert,cert})==null,"several signers refused");
  ok(PanelAccess.host(host,me,false,own,new String[]{maps},p->null)==null,"invisible or missing package refused");
  ok(PanelAccess.host(host,me,false,own,null,certs)==null,"uid without packages refused");
  ok(PanelAccess.host(-1,me,true,own,new String[]{maps},certs)==null,"local message without a sending uid refused");
  ok(maps.equals(PanelAccess.host(host,me,false,own,new String[]{"com.example.shared",maps},certs)),"allowed package within a shared uid");
  ok(PanelAccess.host(me,me,false,own,new String[]{own},certs)==null,"own uid refused in a release build");
  ok(own.equals(PanelAccess.host(me,me,true,own,new String[]{own},certs)),"own uid allowed in a debug build");
  ok(maps.equals(PanelAccess.host(host,me,true,own,new String[]{maps},certs)),"allowed host also in a debug build");
  ok(PanelAccess.host(other,me,true,own,new String[]{"com.example.other"},certs)==null,"debug build still refuses other packages");
  ok(PanelAccess.HOSTS.length>=1 && PanelAccess.HOSTS[0][0].equals(maps) && PanelAccess.HOSTS[0][1].equals(cert),"Maps Automotive is the listed host");
  System.out.println("Panel access checks passed: "+passed);
 }
}

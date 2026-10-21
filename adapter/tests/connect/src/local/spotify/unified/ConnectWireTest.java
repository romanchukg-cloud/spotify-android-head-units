package local.spotify.unified;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
/** Plain-JVM checks of the Spotify Connect messages (ConnectWire), built here as the core sends them. */
public final class ConnectWireTest {
 private static int passed;
 private static void ok(boolean condition,String name){if(!condition)throw new AssertionError(name);passed++;}
 private static void varint(ByteArrayOutputStream o,long v){while(v>=0x80){o.write((int)(v&0x7f)|0x80);v>>>=7;}o.write((int)v);}
 private static void bool(ByteArrayOutputStream o,int field,boolean v){varint(o,field<<3);varint(o,v?1:0);}
 private static void bytes(ByteArrayOutputStream o,int field,byte[] b){varint(o,field<<3|2);varint(o,b.length);o.write(b,0,b.length);}
 private static void text(ByteArrayOutputStream o,int field,String s){bytes(o,field,s.getBytes(StandardCharsets.UTF_8));}
 private static byte[] device(boolean self,String local,String id,String name,String type,boolean active,int category){
  ByteArrayOutputStream d=new ByteArrayOutputStream();
  bool(d,1,self);text(d,2,local);text(d,3,"dedup");text(d,4,id);text(d,5,name);text(d,6,type);text(d,7,"state");bool(d,9,active);
  varint(d,18<<3);varint(d,1729500000000L);varint(d,19<<3);varint(d,65535);
  // An unknown fixed64 and fixed32 field and a repeated string are skipped.
  varint(d,90<<3|1);d.write(new byte[8],0,8);varint(d,91<<3|5);d.write(new byte[4],0,4);text(d,29,"audio/track");text(d,29,"audio/episode");
  if(category>0){ByteArrayOutputStream p=new ByteArrayOutputStream();text(p,1,"out");varint(p,4<<3);varint(p,category);bytes(d,38,p.toByteArray());}
  return d.toByteArray();
 }
 public static void main(String[] args){
  ByteArrayOutputStream s=new ByteArrayOutputStream();
  bool(s,1,true);bool(s,2,true);text(s,3,"Tang");bool(s,4,false);
  bytes(s,5,device(true,"local-car","car-id","Tang","AUTOMOBILE",false,1));
  bytes(s,5,device(false,"local-phone","phone-id","Phone · Ünïcödé","SMARTPHONE",true,0));
  bool(s,6,true);bool(s,8,true);
  ConnectWire.State st=ConnectWire.state(s.toByteArray());
  ok("Tang".equals(st.localName),"local name");
  ok(!st.local,"playback is elsewhere");
  ok(st.devices.size()==2,"two devices");
  ConnectWire.Device car=st.devices.get(0),phone=st.devices.get(1);
  ok(car.self && !car.active && "car-id".equals(car.id) && "local-car".equals(car.localId) && "AUTOMOBILE".equals(car.type) && car.category==1,"the car: self, ids, type, category");
  ok(!phone.self && phone.active && "Phone · Ünïcödé".equals(phone.name) && "SMARTPHONE".equals(phone.type) && phone.category==0,"the phone: active, UTF-8 name");
  ok(ConnectWire.state(new byte[0]).devices.isEmpty(),"empty state");
  ok(ConnectWire.state(null).devices.isEmpty(),"no body");
  byte[] t=ConnectWire.transfer("phone-id");
  ok(t[0]==0x12 && t[1]==8 && "phone-id".equals(new String(t,2,8,StandardCharsets.UTF_8)),"TransferRequest carries the identifier as field 2");
  StringBuilder longId=new StringBuilder();for(int i=0;i<200;i++)longId.append('x');
  byte[] tl=ConnectWire.transfer(longId.toString());ok(tl.length==203 && (tl[1]&0xff)==0xc8 && tl[2]==1,"a long identifier's length as a two-byte varint");
  ok(ConnectWire.stateRequest().length==2 && ConnectWire.stateRequest()[0]==0x18 && ConnectWire.stateRequest()[1]==1,"StateRequest asks for the local device");
  ok(ConnectWire.status(new byte[0])==0 && ConnectWire.status(null)==0,"empty StatusResponse is OK");
  ok(ConnectWire.status(new byte[]{0x08,0x02})==2,"StatusResponse code");
  boolean refused=false;try{ConnectWire.state(new byte[]{0x2a,(byte)0xff,0x01});}catch(IllegalArgumentException e){refused=true;}
  ok(refused,"a length past the end is refused");
  System.out.println("Connect message checks passed: "+passed);
 }
}

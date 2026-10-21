package local.spotify.unified;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
/**
 * The few Spotify Connect messages our interface uses, encoded and decoded by hand
 * (connect_messages.proto in the native library; HeadUnitConnect sends them). Plain Java, so the
 * plain-JVM tests (tests/connect) check it.
 */
final class ConnectWire {
 private ConnectWire(){}
 static final class State {String localName;boolean local;final List<Device> devices=new ArrayList<>();}
 static final class Device {String id,localId,name,type;boolean self,disabled,active,activating,group,offline;int category;}

 /** StateRequest: include_local_device (3) = true. */
 static byte[] stateRequest(){return new byte[]{0x18,0x01};}
 /** TransferRequest: connect_device_identifier (2). */
 static byte[] transfer(String id){
  byte[] s=(id==null?"":id).getBytes(StandardCharsets.UTF_8);java.io.ByteArrayOutputStream o=new java.io.ByteArrayOutputStream();
  o.write(0x12);int n=s.length;while(n>=0x80){o.write((n&0x7f)|0x80);n>>>=7;}o.write(n);o.write(s,0,s.length);return o.toByteArray();
 }
 /** StatusResponse: status (1), 0 = OK; an empty reply is OK. */
 static int status(byte[] b){
  if(b==null)return 0;Wire w=new Wire(b,0,b.length);
  while(w.more()){int tag=(int)w.varint();if(tag>>>3==1 && (tag&7)==0)return (int)w.varint();w.skip(tag&7);}
  return 0;
 }
 /** StateResponse: local_name (3), should_use_local_playback (4), devices (5). */
 static State state(byte[] b){
  State s=new State();Wire w=new Wire(b,0,b==null?0:b.length);
  while(w.more()){
   int tag=(int)w.varint(),f=tag>>>3,t=tag&7;
   if(f==3 && t==2)s.localName=w.string();
   else if(f==4 && t==0)s.local=w.varint()!=0;
   else if(f==5 && t==2)s.devices.add(device(w.message()));
   else w.skip(t);
  }
  return s;
 }
 /**
  * Device: is_self 1, local_identifier 2, connect_state_identifier 4, name 5, type 6, is_disabled
  * 8, is_active 9, is_being_activated 10, is_group 15, is_offline 16, output_peripheral 38
  * (its category 4).
  */
 private static Device device(Wire w){
  Device d=new Device();
  while(w.more()){
   int tag=(int)w.varint(),f=tag>>>3,t=tag&7;
   if(t==0 && f==1)d.self=w.varint()!=0;
   else if(t==0 && f==8)d.disabled=w.varint()!=0;
   else if(t==0 && f==9)d.active=w.varint()!=0;
   else if(t==0 && f==10)d.activating=w.varint()!=0;
   else if(t==0 && f==15)d.group=w.varint()!=0;
   else if(t==0 && f==16)d.offline=w.varint()!=0;
   else if(t==2 && f==2)d.localId=w.string();
   else if(t==2 && f==4)d.id=w.string();
   else if(t==2 && f==5)d.name=w.string();
   else if(t==2 && f==6)d.type=w.string();
   else if(t==2 && f==38){Wire p=w.message();while(p.more()){int pt=(int)p.varint();if(pt>>>3==4 && (pt&7)==0)d.category=(int)p.varint();else p.skip(pt&7);}}
   else w.skip(t);
  }
  return d;
 }
 private static final class Wire {
  final byte[] b;int p;final int end;
  Wire(byte[] b,int p,int end){this.b=b;this.p=p;this.end=end;}
  boolean more(){return p<end;}
  long varint(){long v=0;int s=0;while(p<end && s<64){int x=b[p++]&0xff;v|=(long)(x&0x7f)<<s;if(x<0x80)return v;s+=7;}throw new IllegalArgumentException("varint");}
  int length(){long n=varint();if(n<0 || n>end-p)throw new IllegalArgumentException("length");return (int)n;}
  String string(){int len=length();String s=new String(b,p,len,StandardCharsets.UTF_8);p+=len;return s;}
  Wire message(){int len=length();Wire m=new Wire(b,p,p+len);p+=len;return m;}
  void skip(int type){if(type==0)varint();else if(type==1)p+=8;else if(type==5)p+=4;else if(type==2){int n=length();p+=n;}else throw new IllegalArgumentException("wire type "+type);if(p>end)throw new IllegalArgumentException("past end");}
 }
}

"""Merge two compiled Android resource packages, preserving original resource IDs.
UI IDs move from package 0x7f to 0x7e; backend stays 0x7f.
"""
import struct
U16=lambda b,p:struct.unpack_from('<H',b,p)[0]
U32=lambda b,p:struct.unpack_from('<I',b,p)[0]
def chunks(b,start=0,end=None):
 p=start
 while p<(len(b) if end is None else end):
  t,h,n=struct.unpack_from('<HHI',b,p);assert n>=h>=8
  yield p,t,h,n;p+=n
 assert p==(len(b) if end is None else end)
def strings(b):
 count,sc,flags,start,styles=struct.unpack_from('<5I',b,8)
 out=[]
 def length(p,utf8):
  x=b[p] if utf8 else U16(b,p);p+=1 if utf8 else 2;flag=0x80 if utf8 else 0x8000
  if x&flag:
   y=b[p] if utf8 else U16(b,p);p+=1 if utf8 else 2;x=((x&~flag)<<(8 if utf8 else 16))|y
  return x,p
 for i in range(count):
  p=start+U32(b,28+4*i);size,p=length(p,bool(flags&256))
  if flags&256:size,p=length(p,True);s=b[p:p+size].decode('utf8')
  else:s=b[p:p+size*2].decode('utf-16-le')
  out.append(s)
 offsets=[U32(b,28+count*4+i*4) for i in range(sc)]
 return out,offsets,b[styles:] if styles else b''
def pool(ss,style_offsets=(),style_data=b''):
 data=bytearray();offs=[]
 for s in ss:
  offs.append(len(data));v=s.encode('utf-16-le');n=len(v)//2
  data+=struct.pack('<H',n) if n<0x8000 else struct.pack('<HH',0x8000|(n>>16),n&65535)
  data+=v+b'\0\0'
 data+=b'\0'*((-len(data))%4)
 start=28+4*(len(ss)+len(style_offsets));st=start+len(data) if style_data else 0
 result=struct.pack('<HHI5I',1,28,start+len(data)+len(style_data),len(ss),len(style_offsets),0,start,st)
 return result+b''.join(struct.pack('<I',v) for v in [*offs,*style_offsets])+data+style_data
def remap(v):return (v&0xffffff)|0x7e000000 if v>>24==0x7f else v
def patch_value(b,p,string_delta=0,mapper=remap):
 typ=b[p+3];val=U32(b,p+4)
 if typ==3:val+=string_delta
 elif typ in (1,2,7,8):val=mapper(val)
 struct.pack_into('<I',b,p+4,val)
def add_ui_library(raw):
 b=bytearray(raw);name='com.spotify.music.ui'.encode('utf-16-le')
 assert not any(t==0x203 for p,t,h,n in chunks(b,U16(b,2)))
 b+=struct.pack('<HHII',0x203,12,272,1)+struct.pack('<I',126)+name+b'\0'*(256-len(name))
 struct.pack_into('<I',b,4,len(b));return bytes(b)
def patch_package(raw,delta):
 b=bytearray(raw);struct.pack_into('<I',b,8,126)
 name='com.spotify.music.ui'.encode('utf-16-le');b[12:268]=name+b'\0'*(256-len(name))
 for p,t,h,n in chunks(b,U16(b,2)):
  if t!=0x201:continue
  flags=b[p+9];assert flags==0,flags
  count=U32(b,p+12);start=U32(b,p+16)
  for i in range(count):
   off=U32(b,p+h+i*4)
   if off==0xffffffff:continue
   e=p+start+off;size=U16(b,e);ef=U16(b,e+2)
   if ef&1:
    struct.pack_into('<I',b,e+8,remap(U32(b,e+8)))
    for j in range(U32(b,e+12)):
     q=e+size+j*12;struct.pack_into('<I',b,q,remap(U32(b,q)));patch_value(b,q+4,delta)
   else:patch_value(b,e+size,delta)
 # Android needs an explicit build-time mapping for a nonstandard package ID.
 lib=struct.pack('<HHII',0x203,12,272,1)+struct.pack('<I',126)+name+b'\0'*(256-len(name))
 b+=lib;struct.pack_into('<I',b,4,len(b))
 return bytes(b)
def merge(backend,ui,transform):
 bc=list(chunks(backend,U16(backend,2)));uc=list(chunks(ui,U16(ui,2)))
 bs=next(backend[p:p+n] for p,t,h,n in bc if t==1);us=next(ui[p:p+n] for p,t,h,n in uc if t==1)
 bss,bso,bsd=strings(bs);uss,uso,usd=strings(us);assert not uso and not usd
 merged=pool(bss+[transform(s) for s in uss],bso,bsd)
 body=merged+b''.join(add_ui_library(backend[p:p+n]) if t==0x200 else backend[p:p+n] for p,t,h,n in bc if t!=1)+b''.join(patch_package(ui[p:p+n],len(bss)) for p,t,h,n in uc if t==0x200)
 return struct.pack('<HHII',2,12,12+len(body),U32(backend,8)+U32(ui,8))+body
def xml(raw,transform,mapper=remap):
 assert U16(raw,0)==3
 body=bytearray()
 for p,t,h,n in chunks(raw,U16(raw,2)):
  part=bytearray(raw[p:p+n])
  if t==1:
   ss,so,sd=strings(part);part=pool([transform(s) for s in ss],so,sd)
  elif t==0x180:
   for q in range(h,n,4):struct.pack_into('<I',part,q,mapper(U32(part,q)))
  elif t==0x102:
   ast=U16(part,24);asz=U16(part,26);ac=U16(part,28)
   for i in range(ac):patch_value(part,16+ast+i*asz+12,mapper=mapper)
  elif t==0x104:patch_value(part,20,mapper=mapper)
  body+=part
 return struct.pack('<HHI',3,8,8+len(body))+body

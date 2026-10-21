from pathlib import Path
import zipfile,json,struct
from merge_resources import chunks,strings,U16,U32
from build_config import BUILD
b=Path(__file__).resolve().parent;p=b.parent/'spotify55-adaptation'
CHANGED=('classes.dex','classes2.dex','AndroidManifest.xml','resources.arsc','stamp-cert-sha256')
with zipfile.ZipFile(p/'SpotifySDK-5.5.0-BYD-test.apk') as old,zipfile.ZipFile(b/BUILD['apk_name']) as new:
 names=set(new.namelist())
 preserved=[n for n in old.namelist() if not n.startswith('META-INF/') and n not in CHANGED]
 # Every other backend payload is the SDK container's own.
 assert all(n in names and old.read(n)==new.read(n) for n in preserved)
 assert all(n in names and old.read(n)==new.read(n) for n in old.namelist() if n.startswith('META-INF/services/'))
 # The resource table: the backend's package keeps every chunk, plus one dynamic-reference entry
 # for our package 0x7e (the widgets' resources); the string pool only grows.
 ot=old.read('resources.arsc');nt=new.read('resources.arsc')
 oc=list(chunks(ot,U16(ot,2)));nc=list(chunks(nt,U16(nt,2)))
 op=next(ot[q:q+n] for q,t,h,n in oc if t==0x200)
 np=next(nt[q:q+n] for q,t,h,n in nc if t==0x200 and U32(nt,q+8)==0x7f)
 lib=[np[q:q+n] for q,t,h,n in chunks(np,U16(np,2)) if t==0x203]
 assert len(lib)==1 and U32(lib[0],8)==1 and U32(lib[0],12)==0x7e
 restored=bytearray(np[:U16(np,2)]+b''.join(np[q:q+n] for q,t,h,n in chunks(np,U16(np,2)) if t!=0x203))
 struct.pack_into('<I',restored,4,len(restored))
 assert op==restored,'Original backend resource chunks changed'
 os_,oo,od=strings(next(ot[q:q+n] for q,t,h,n in oc if t==1));ns,no,nd=strings(next(nt[q:q+n] for q,t,h,n in nc if t==1))
 assert ns[:len(os_)]==os_ and oo==no and od==nd
 assert U32(nt,8)==2 and any(t==0x200 and U32(nt,q+8)==0x7e for q,t,h,n in nc)
 # Added: our DEX, the interface typeface, the widgets' resources.
 extra={n for n in names if not n.startswith('META-INF/')}-set(old.namelist())
 allowed=lambda n:n=='classes2.dex' or n.startswith(('assets/hu_fonts/','hu/res/'))
 assert all(allowed(n) for n in extra),sorted(n for n in extra if not allowed(n))
 assert 'classes2.dex' in names and {'assets/hu_fonts/Figtree-wght.ttf','hu/res/layout/hu_widget_wide.xml','hu/res/xml/hu_widget_wide_info.xml'}<=extra
 # No BYD-derived interface: no third DEX, no relocated resources.
 assert 'classes3.dex' not in names and not any(n.startswith('ui/') for n in names)
report={'backend_payloads_preserved':len(preserved),'backend_resource_chunks_preserved':True,'resource_packages':2,'widget_resources':len([n for n in extra if n.startswith('hu/res/')]),'backend_services_metadata_preserved':True,'byd_interface':False}
(b/'structural-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))

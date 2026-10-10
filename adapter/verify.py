from pathlib import Path
import os,zipfile,json,re,subprocess,struct
from merge_resources import chunks,strings,U16,U32
b=Path(__file__).resolve().parent;p=b.parent/'spotify55-adaptation'
with zipfile.ZipFile(p/'SpotifySDK-5.5.0-BYD-test.apk') as old,zipfile.ZipFile(b/'Spotify-5.5.0-HeadUnit-test.4.apk') as new:
 preserved=[n for n in old.namelist() if not n.startswith('META-INF/') and n not in ('classes.dex','classes2.dex','AndroidManifest.xml','resources.arsc','stamp-cert-sha256')]
 assert all(old.read(n)==new.read(n) for n in preserved)
 ot=old.read('resources.arsc');nt=new.read('resources.arsc')
 oc=list(chunks(ot,U16(ot,2)));nc=list(chunks(nt,U16(nt,2)))
 op=next(ot[q:q+n] for q,t,h,n in oc if t==0x200)
 np=next(nt[q:q+n] for q,t,h,n in nc if t==0x200 and U32(nt,q+8)==127)
 # The Android 32 XML parser uses the first package's dynamic reference table.
 # Only append the UI package mapping; original backend resource chunks stay intact.
 lib=[np[q:q+n] for q,t,h,n in chunks(np,U16(np,2)) if t==0x203]
 assert len(lib)==1 and U32(lib[0],8)==1 and U32(lib[0],12)==126
 restored=bytearray(np[:U16(np,2)]+b''.join(np[q:q+n] for q,t,h,n in chunks(np,U16(np,2)) if t!=0x203))
 struct.pack_into('<I',restored,4,len(restored))
 assert op==restored,'Original backend resource chunks changed'
 os,oo,od=strings(next(ot[q:q+n] for q,t,h,n in oc if t==1))
 ns,no,nd=strings(next(nt[q:q+n] for q,t,h,n in nc if t==1))
 assert ns[:len(os)]==os and oo==no and od==nd
 assert U32(nt,8)==2
 assert all(n in new.namelist() and old.read(n)==new.read(n) for n in old.namelist() if n.startswith('META-INF/services/'))
cm=json.loads((b/'class-map.json').read_text());descriptors={'L'+s.replace('.','/')+';' for s in cm}
for f in (b/'ui-smali').rglob('*.smali'):
 assert not (set(re.findall(r'L[\w/$]+;',f.read_text()))&descriptors),f
report={'backend_payloads_preserved':len(preserved),'backend_resource_strings_preserved':len(os),'backend_resource_chunks_preserved':True,'backend_ui_library_mapping_added':True,'ui_classes_relocated':len(cm),'resource_packages':2,'backend_services_metadata_preserved':True}
(b/'structural-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))

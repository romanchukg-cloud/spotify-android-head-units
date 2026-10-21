from pathlib import Path
import shutil,re,xml.etree.ElementTree as E
from build_config import BUILD
base=Path(__file__).resolve().parent;prev=base.parent/'spotify55-adaptation';out=base/'backend'
if not out.exists():shutil.copytree(prev/'backend',out,ignore=shutil.ignore_patterns('build','original'))
# Resource IDs stay exactly the stable backend's. Builds up to test.12 added placeholder
# resources here for the manifest references of the BYD-derived interface.
shutil.copyfile(prev/'backend/res/values/public.xml',out/'res/values/public.xml')
(out/'res/values/unified_refs.xml').unlink(missing_ok=True)
# The widgets' manifest entries point at our own XML resources (res-hu, packaged as 0x7e), which
# the backend's resource compile does not know: they compile against placeholder XML files with
# new IDs in the backend's table (never packaged), and package.py rewrites those IDs in the
# binary manifest (hu-refs.json).
import json
HU_REFS={'hu_ref_widget_wide':'xml/hu_widget_wide_info','hu_ref_widget_square':'xml/hu_widget_square_info'}
pub=E.parse(out/'res/values/public.xml');pr=pub.getroot();nxt=max(int(n.get('id'),16) for n in pr if n.get('type')=='xml')+1;refs={}
for name,target in HU_REFS.items():
 (out/'res/xml'/(name+'.xml')).write_text('<?xml version="1.0" encoding="utf-8"?>\n<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android" />\n')
 E.SubElement(pr,'public',{'type':'xml','name':name,'id':hex(nxt)});refs[hex(nxt)]=target;nxt+=1
pub.write(out/'res/values/public.xml',encoding='utf-8',xml_declaration=True)
(base/'hu-refs.json').write_text(json.dumps(refs,indent=1)+'\n')
A='{http://schemas.android.com/apk/res/android}';E.register_namespace('android',A[1:-1])
m=E.parse(prev/'backend/AndroidManifest.xml');mr=m.getroot();app=mr.find('application')
app.set(A+'appComponentFactory','local.spotify.unified.UnifiedFactory');app.set(A+'extractNativeLibs','true')
# The playback notification, and the close-control provider that the interface process calls
# (the backend declares the provider and its signature permission).
for name in ['android.permission.POST_NOTIFICATIONS','com.spotify.music.permission.LOCAL_CLOSE_CONTROL']:
 mr.insert(0,E.Element('uses-permission',{A+'name':name}))
# Keeps playback state right when the interface's task is removed from recents.
E.SubElement(app,'service',{A+'name':'local.spotify.close.TaskGuardService',A+'process':':bydui',A+'exported':'false',A+'foregroundServiceType':'mediaPlayback',A+'stopWithTask':'false'})
m.write(out/'AndroidManifest.xml',encoding='utf-8',xml_declaration=True)
y=(out/'apktool.yml').read_text();y=re.sub(r'targetSdkVersion: \d+','targetSdkVersion: 33',y);y=re.sub(r'versionCode: \d+','versionCode: '+str(BUILD['version_code']),y);(out/'apktool.yml').write_text(y)

exec((base/"patch_headunit.py").read_text(),{"__file__":str(base/"patch_headunit.py")})

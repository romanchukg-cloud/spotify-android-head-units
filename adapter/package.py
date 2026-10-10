from pathlib import Path
import os,zipfile,json,subprocess,hashlib
from merge_resources import merge,xml
b=Path(__file__).resolve().parent;p=b.parent/'spotify55-adaptation';bt=Path(os.environ.get('ANDROID_SDK_ROOT',os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk'))))/'build-tools'/os.environ.get('BUILD_TOOLS_VERSION','35.0.0')
cm=json.loads((b/'class-map.json').read_text());refs={int(k,16):int(v,16) for k,v in json.loads((b/'manifest-refs.json').read_text()).items()}
def transform(s):
 if s in cm:return cm[s]
 if s=='com.vivid.music.byd':return 'com.spotify.music'
 if s.startswith('com.vivid.music.byd.'):return 'com.spotify.music.'+s[len('com.vivid.music.byd.'):]
 if s.startswith('res/'):return 'ui/'+s
 return s
with zipfile.ZipFile(p/'SpotifySDK-5.5.0-BYD-test.apk') as sdk,zipfile.ZipFile(b/'ui-resources.apk') as ui,zipfile.ZipFile(b/'backend-rebuilt.apk') as built,zipfile.ZipFile(b/'unsigned.apk','w') as out:
 for info in sdk.infolist():
  n=info.filename
  if (n.startswith('META-INF/') and (n.endswith(('.SF','.RSA','.DSA','.EC')) or n=='META-INF/MANIFEST.MF')) or n in ('stamp-cert-sha256','classes2.dex'):continue
  data=sdk.read(n)
  if n=='classes.dex':data=built.read(n)
  if n=='AndroidManifest.xml':data=xml(built.read(n),lambda s:s,lambda v:refs.get(v,v))
  if n=='resources.arsc':data=merge(data,ui.read(n),transform)
  out.writestr(info,data)
 for info in ui.infolist():
  n=info.filename
  if n.startswith('META-INF/services/'):
   dest='META-INF/services/'+transform(n[len('META-INF/services/'):]);out.writestr(dest,'\n'.join(transform(v) for v in ui.read(n).decode().splitlines())+'\n');continue
  if not n.startswith(('res/','assets/')):continue
  data=ui.read(n);dest='ui/'+n if n.startswith('res/') else n
  if dest in out.namelist():
   assert out.read(dest)==data,('Asset collision',dest);continue
  if data[:4]==b'\x03\x00\x08\x00':data=xml(data,transform)
  info.filename=dest;out.writestr(info,data)
 out.writestr('classes2.dex',(b/'helper-dex/classes.dex').read_bytes())
 out.writestr('classes3.dex',(b/'ui.dex').read_bytes())
subprocess.run([str(bt/'zipalign'),'-P','16','-f','4',str(b/'unsigned.apk'),str(b/'aligned.apk')],check=True)
output=b/'Spotify-5.5.0-HeadUnit-test.4.apk'
subprocess.run([str(bt/'apksigner'),'sign','--ks',os.environ.get('HU_KEYSTORE',str(Path.home()/'.android/debug.keystore')),'--ks-pass',os.environ.get('HU_KEYSTORE_PASS','pass:android'),'--out',str(output),str(b/'aligned.apk')],check=True)
subprocess.run([str(bt/'apksigner'),'verify','--verbose',str(output)],check=True)
subprocess.run([str(bt/'zipalign'),'-c','-P','16','4',str(output)],check=True)
(b/'verification.json').write_text(json.dumps({'file':output.name,'sha256':hashlib.sha256(output.read_bytes()).hexdigest(),'size':output.stat().st_size},indent=2))
print(output)

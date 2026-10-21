from pathlib import Path
import os,zipfile,json,subprocess,hashlib
from build_config import BUILD
from merge_resources import merge,xml
b=Path(__file__).resolve().parent;p=b.parent/'spotify55-adaptation';bt=Path(os.environ.get('ANDROID_SDK_ROOT',os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk'))))/'build-tools'/os.environ.get('BUILD_TOOLS_VERSION','35.0.0')
# Our interface's typeface (HuUi.font) and its licence.
FONTS={'assets/hu_fonts/Figtree-wght.ttf':b/'fonts/figtree/Figtree-wght.ttf','assets/hu_fonts/OFL.txt':b/'fonts/figtree/OFL.txt'}
# Our own resources (res-hu, the widgets; widget_resources.py) become package 0x7e beside the
# backend's 0x7f; their files go under hu/res/.
HU=zipfile.ZipFile(b/'hu-res/hu-resources.apk')
def hu_path(s):return 'hu/'+s if s.startswith('res/') else s
# The widgets' manifest entries: placeholder IDs (prepare.py) to our XML resources' IDs.
ids=json.loads((b/'hu-res/ids.json').read_text());refs={int(k,16):int(ids[v],16) for k,v in json.loads((b/'hu-refs.json').read_text()).items()}
# The SDK container's payloads stay byte for byte, its resource table keeps every chunk of the
# backend's package (merge_resources); the rebuilt backend gives classes.dex and the manifest, the
# helper DEX (our code) is classes2.dex.
with zipfile.ZipFile(p/'SpotifySDK-5.5.0-BYD-test.apk') as sdk,zipfile.ZipFile(b/'backend-rebuilt.apk') as built,HU,zipfile.ZipFile(b/'unsigned.apk','w') as out:
 for info in sdk.infolist():
  n=info.filename
  if (n.startswith('META-INF/') and (n.endswith(('.SF','.RSA','.DSA','.EC')) or n=='META-INF/MANIFEST.MF')) or n in ('stamp-cert-sha256','classes2.dex'):continue
  data=sdk.read(n)
  if n=='classes.dex':data=built.read(n)
  if n=='AndroidManifest.xml':data=xml(built.read(n),lambda s:s,lambda v:refs.get(v,v))
  if n=='resources.arsc':data=merge(data,HU.read('resources.arsc'),hu_path)
  out.writestr(info,data)
 for info in HU.infolist():
  n=info.filename
  if not n.startswith('res/'):continue
  data=HU.read(n);dest=hu_path(n);assert dest not in out.namelist(),('Resource path collision',dest)
  if data[:4]==b'\x03\x00\x08\x00':data=xml(data,hu_path)
  out.writestr(zipfile.ZipInfo(dest,info.date_time),data,compress_type=zipfile.ZIP_DEFLATED)
 out.writestr('classes2.dex',(b/'helper-dex/classes.dex').read_bytes())
 for name,src in FONTS.items():
  assert name not in out.namelist(),name
  out.write(src,name,compress_type=zipfile.ZIP_STORED)
subprocess.run([str(bt/'zipalign'),'-P','16','-f','4',str(b/'unsigned.apk'),str(b/'aligned.apk')],check=True)
output=b/BUILD['apk_name']
subprocess.run([str(bt/'apksigner'),'sign','--ks',os.environ.get('HU_KEYSTORE',str(Path.home()/'.android/debug.keystore')),'--ks-pass',os.environ.get('HU_KEYSTORE_PASS','pass:android'),'--out',str(output),str(b/'aligned.apk')],check=True)
subprocess.run([str(bt/'apksigner'),'verify','--verbose',str(output)],check=True)
subprocess.run([str(bt/'zipalign'),'-c','-P','16','4',str(output)],check=True)
(b/'verification.json').write_text(json.dumps({'file':output.name,'sha256':hashlib.sha256(output.read_bytes()).hexdigest(),'size':output.stat().st_size},indent=2))
print(output)

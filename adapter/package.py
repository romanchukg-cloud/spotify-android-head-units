from pathlib import Path
import os,zipfile,json,subprocess,hashlib
from build_config import BUILD
b=Path(__file__).resolve().parent;p=b.parent/'spotify55-adaptation';bt=Path(os.environ.get('ANDROID_SDK_ROOT',os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk'))))/'build-tools'/os.environ.get('BUILD_TOOLS_VERSION','35.0.0')
# Our interface's typeface (HuUi.font) and its licence.
FONTS={'assets/hu_fonts/Figtree-wght.ttf':b/'fonts/figtree/Figtree-wght.ttf','assets/hu_fonts/OFL.txt':b/'fonts/figtree/OFL.txt'}
# The SDK container's payloads and resource table stay byte for byte; the rebuilt backend gives
# classes.dex and the manifest, the helper DEX (our code) is classes2.dex.
with zipfile.ZipFile(p/'SpotifySDK-5.5.0-BYD-test.apk') as sdk,zipfile.ZipFile(b/'backend-rebuilt.apk') as built,zipfile.ZipFile(b/'unsigned.apk','w') as out:
 for info in sdk.infolist():
  n=info.filename
  if (n.startswith('META-INF/') and (n.endswith(('.SF','.RSA','.DSA','.EC')) or n=='META-INF/MANIFEST.MF')) or n in ('stamp-cert-sha256','classes2.dex'):continue
  data=sdk.read(n)
  if n in ('classes.dex','AndroidManifest.xml'):data=built.read(n)
  out.writestr(info,data)
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

#!/usr/bin/env python3
"""Static policy audit of the final APK. Requires apktool and Android build-tools.
This checks structure and guarded call sites, not runtime SDK/API compatibility or playback.
"""
import argparse, hashlib, os, re, shutil, subprocess, sys, tempfile, zipfile
from pathlib import Path
import xml.etree.ElementTree as ET
p=argparse.ArgumentParser();p.add_argument('apk',type=Path);p.add_argument('--min-api',type=int,default=28);p.add_argument('--decoded',type=Path);args=p.parse_args()
checks=[]
def check(name,ok,detail=''):
 checks.append(bool(ok));print(('PASS' if ok else 'FAIL')+' '+name+(' — '+detail if detail else ''))
def run(cmd):return subprocess.run(cmd,capture_output=True,text=True,check=True).stdout
apk=args.apk.resolve();digest=hashlib.sha256(apk.read_bytes()).hexdigest();print(f'APK {apk.name}\nSHA256 {digest}')
temp=None
if args.decoded:root=args.decoded
else:
 temp=tempfile.TemporaryDirectory(prefix='spotify-hu-audit-');root=Path(temp.name)/'decoded'
 subprocess.run(['apktool','d','-f',str(apk),'-o',str(root)],stdout=subprocess.DEVNULL,check=True)
A='{http://schemas.android.com/apk/res/android}'
m=ET.parse(root/'AndroidManifest.xml').getroot();app=m.find('application');uses=m.find('uses-sdk')
sdk=Path(os.environ.get('ANDROID_SDK_ROOT',os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk'))));bt=sdk/'build-tools'/os.environ.get('BUILD_TOOLS_VERSION','35.0.0')
badging=run([str(bt/'aapt2'),'dump','badging',str(apk)]);package=re.search(r"package: name='([^']+)'",badging)
# apktool moves SDK numbers into apktool.yml.
yaml=(root/'apktool.yml').read_text();minimum=re.search(r'minSdkVersion: [\'\"]?(\d+)',yaml)
check('Package / minimum API',package and package[1]=='com.spotify.music' and minimum and int(minimum[1])==args.min_api,f'minSdk={minimum[1] if minimum else "unknown"}')
feature=next((n for n in m.findall('uses-feature') if n.get(A+'name')=='android.hardware.type.automotive'),None)
check('Optional automotive feature',feature is not None and feature.get(A+'required')=='false')
check('Legacy Back dispatch',app.get(A+'enableOnBackInvokedCallback')=='false')
metadata={n.get(A+'name'):n.get(A+'value') for n in app.findall('meta-data')};perms={n.get(A+'name') for n in m.findall('uses-permission')}
check('Privacy metadata',metadata.get('firebase_crashlytics_collection_enabled')=='false' and not any(k.startswith(('com.android.stamp.','com.android.vending.')) for k in metadata) and 'com.google.android.gms.permission.AD_ID' not in perms)
check('Boot permission','android.permission.RECEIVE_BOOT_COMPLETED' in perms)
def component(kind,name):return next((n for n in app.findall(kind) if n.get(A+'name')==name),None)
provider=component('provider','local.spotify.unified.HeadUnitSettingsProvider');settings=component('activity','local.spotify.unified.HeadUnitSettingsActivity');service=component('service','local.spotify.unified.HeadUnitPlaybackService')
check('Private settings / playback components',provider is not None and provider.get(A+'exported')=='false' and settings is not None and settings.get(A+'exported')=='false' and service is not None and service.get(A+'exported')=='false' and service.get(A+'foregroundServiceType')=='mediaPlayback')
receivers=[n for n in app.findall('receiver') if any(x.get(A+'name')=='android.intent.action.MEDIA_BUTTON' for x in n.findall('.//action'))]
check('One cold-start media receiver',len(receivers)==1 and receivers[0].get(A+'name')=='local.spotify.unified.HeadUnitMediaReceiver')
check('No vendor restore service',not any('RestoreStateService' in n.get(A+'name','') for n in app.findall('service')))
files={}
for folder in root.glob('smali*'):
 for f in folder.rglob('*.smali'):
  text=f.read_text();match=re.search(r'^\.class[^\n]* (L[^;]+;)',text,re.M)
  if match:files[match[1]]=text
check('Helper / relocated UI present',all(k in files for k in ('Llocal/spotify/unified/HeadUnitProfile;','Llocal/spotify/unified/HeadUnitSettingsActivity;','Llocal/bydui/com/vivid/spotify/MainActivity;')))
methods=lambda s: re.findall(r'\.method[^\n]*\n.*?\.end method',s,re.S)
vendor=files.get('Llocal/bydui/com/vivid/spotify/byd/b;','');vendor_calls=0;unsafe=[];listener_types=set()
for method in methods(vendor):
 if 'Landroid/hardware/bydauto/' in method or re.search(r' (s|u)\(\)V\n',method):
  vendor_calls+=1;code=re.sub(r'^\s*\.(?:line|prologue|epilogue|local|end local|restart local)[^\n]*$', '',method.split('    .locals',1)[-1],flags=re.M);pos=code.find('Llocal/spotify/unified/HeadUnitProfile;->isByd()Z')
  if pos<0 or not re.search(r'if-nez v0, (:\w+)\s+(?:const/4 v0, 0x0\s+)?return(?:-void| v0)\s+\1',code):unsafe.append(method.splitlines()[0])
  listener_types.update(re.findall(r'new-instance \w+, (Llocal/bydui/com/vivid/spotify/byd/b\$[^;]+;)',method))
for clazz,text in files.items():
 if 'Landroid/hardware/bydauto/' not in text or clazz.endswith('/byd/b;'):continue
 if clazz not in listener_types:unsafe.append(clazz)
 for caller,body in files.items():
  for method in methods(body):
   if 'new-instance' in method and clazz in method and 'HeadUnitProfile;->isByd()Z' not in method:unsafe.append(caller+' '+method.splitlines()[0])
check('Vendor (BYD) coupling',vendor_calls>0 and not unsafe,f'{vendor_calls} entry-guarded methods; {len(listener_types)} listeners only constructed behind profile guard'+('; '+str(unsafe) if unsafe else ''))
controllers=files.get('Lle/d;','')
check('External controller policy hook','HeadUnitControllers;->allow(' in controllers and 'HeadUnitControllers;->matches(' in controllers and 'HeadUnitControllers;->result(' in controllers)
gate=files.get('Llocal/spotify/close/PlaybackGate;','');death=files.get('Llocal/spotify/close/PlaybackGate$1;','')
check('Binder death does not close gate','binderDied()V' in death and 'PlaybackGate;->change(' not in death and 'UI binder died' in death)
check('BYD legacy UI trust gated',all('HeadUnitProfile;->isByd()Z' in x for x in methods(gate) if ' trustedLocalUi(' in x) and ' trustedLocalUi(' in gate)
blocked=[k for k in ('Lbd/j;','Landroid/support/v4/media/session/MediaSessionCompat$Callback$MediaSessionCallbackApi21;') if 'PlaybackGate;->blocked()Z' in files.get(k,'')]
check('Explicit transport commands unblocked',not blocked and 'PlaybackGate;->userPlay()V' in files.get('Lbd/j;',''))
profiles=component('provider','local.spotify.unified.ProfilesProvider');coordinator=component('activity','local.spotify.unified.ProfilesActivity')
check('Private accounts / isolated coordinator',profiles is not None and profiles.get(A+'exported')=='false' and coordinator is not None and coordinator.get(A+'exported')=='false' and coordinator.get(A+'process')==':profiles' and coordinator.get(A+'taskAffinity')=='com.spotify.music.profiles' and app.get(A+'allowBackup')=='false')
check('Account storage / settings entry','DriverProfiles;->bindCandidate(' in files.get('Lca/c;','') and 'Llocal/spotify/unified/ProfilesActivity;' in files.get('Llocal/spotify/unified/HeadUnitSettingsActivity;','') and 'Landroid/security/keystore/KeyGenParameterSpec$Builder;' in files.get('Llocal/spotify/unified/ProfileVault;',''))
check('Account transition transport guards',all('PlaybackGate;->accountTransition()Z' in files.get(k,'') for k in ('Lbd/j;','Landroid/support/v4/media/session/MediaSessionCompat$Callback$MediaSessionCallbackApi21;','Llocal/spotify/unified/HeadUnitPlaybackService;')))
main=files.get('Llocal/bydui/com/vivid/spotify/MainActivity;','')
check('Settings entry / density context','HeadUnitSettings;->open(' in main and 'HeadUnitSettings;->scaled(' in main)
# UI package resource IDs are retained as a second package by this APK.
res=root/'res'
with zipfile.ZipFile(apk) as z:
 names=z.namelist()
 check('Landscape / portrait resources',all(any(n.startswith('ui/res/'+q+'/') for n in names) for q in ('layout-land','layout-w600dp-land','layout-w840dp-land','layout-w600dp-h900dp-port')) and (res/'values-h400dp-land').is_dir())
 versions={n:int(z.read(n)[4:7]) for n in names if re.fullmatch(r'classes\d*\.dex',n)}
 check('DEX version compatible with Android 9',all(v<=39 for v in versions.values()),str(versions))
# Audit app-owned strings from decoded combined resources, accounting for apktool package prefixes.
def strings(locale):
 found={}
 for f in res.glob('values'+('-'+locale if locale else '')+'/*.xml'):
  for n in ET.parse(f).getroot().findall('string'):found[n.get('name')]=''.join(n.itertext())
 return found
owned=['added_to_queue', 'added_to_your_episodes', 'added_to_your_library', 'added_to_your_liked_songs', 'app_name', 'back', 'byd_error', 'clear_all', 'collect_episode_title', 'collect_song_title', 'dialog_cancel', 'dialog_content', 'dialog_go_to_set', 'dialog_title', 'multi_episode_count', 'multi_song_count', 'play_what_you_love', 'recent_searches', 'removed_from_your_episodes', 'removed_from_your_library', 'removed_from_your_liked_songs', 'retry', 'safety_subtitle', 'safety_title', 'search', 'search_for_something', 'search_menu_title', 'slogan', 'something_wrong', 'speed', 'unknown_song', 'widget_slogan', 'wifi_notification', 'your_library']
base=strings('');ui_names=[k for k in base if k.startswith('ui_')]
for locale in ('uk','ru','ar','zh-rCN'):
 translated=strings(locale)
 names=owned
 missing=[k for k in names if not translated.get(k)]
 check('UI translations '+locale,bool(names) and not missing,f'{len(names)} strings, missing={missing}')
signer=bt/'apksigner';align=bt/'zipalign'
try:out=run([str(signer),'verify','--verbose','--min-sdk-version',str(args.min_api),str(apk)]);check('APK signature', 'Verified using v3 scheme (APK Signature Scheme v3): true' in out)
except (OSError,subprocess.CalledProcessError):check('APK signature',False)
try:run([str(align),'-c','-P','16','4',str(apk)]);check('ZIP / native 16 KiB alignment',True)
except (OSError,subprocess.CalledProcessError):check('ZIP / native 16 KiB alignment',False)
print(f'RESULT {sum(checks)} PASS, {len(checks)-sum(checks)} FAIL')
print('LIMIT: static policy checks do not validate login, audio, all SDK API calls, or real head-unit firmware.')
if temp:temp.cleanup()
sys.exit(0 if all(checks) else 1)

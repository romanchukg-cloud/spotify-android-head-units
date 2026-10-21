#!/usr/bin/env python3
"""Static policy audit of the final APK. Requires apktool and Android build-tools.
This checks structure and guarded call sites, not runtime SDK/API compatibility or playback.
"""
import argparse, hashlib, os, re, shutil, subprocess, sys, tempfile, zipfile
from pathlib import Path
import xml.etree.ElementTree as ET
p=argparse.ArgumentParser();p.add_argument('apk',type=Path);p.add_argument('--min-api',type=int,default=28);p.add_argument('--decoded',type=Path);p.add_argument('--debug-build',action='store_true',help='the HU_DEBUG=1 variant with the panel test host');args=p.parse_args()
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
# Our interface is the main screen; the launcher keeps the earlier component name as an alias of it.
alias=component('activity-alias','local.bydui.com.vivid.spotify.MainActivity');own=component('activity','local.spotify.unified.HuPlayerActivity')
launchers=[n.get(A+'name') for n in list(app.findall('activity'))+list(app.findall('activity-alias')) if any(c.get(A+'name')=='android.intent.category.LAUNCHER' for c in n.findall('.//category'))]
check('Main screen is our interface',alias is not None and alias.get(A+'targetActivity')=='local.spotify.unified.HuPlayerActivity' and alias.get(A+'exported')=='true' and own is not None and own.get(A+'process')==':bydui' and component('activity','local.bydui.com.vivid.spotify.MainActivity') is None and launchers==['local.bydui.com.vivid.spotify.MainActivity'],f'launchers={launchers}')
files={}
for folder in root.glob('smali*'):
 for f in folder.rglob('*.smali'):
  text=f.read_text();match=re.search(r'^\.class[^\n]* (L[^;]+;)',text,re.M)
  if match:files[match[1]]=text
check('Helper present',all(k in files for k in ('Llocal/spotify/unified/HeadUnitProfile;','Llocal/spotify/unified/HeadUnitSettingsActivity;','Llocal/spotify/unified/HuPlayerActivity;')))
methods=lambda s: re.findall(r'\.method[^\n]*\n.*?\.end method',s,re.S)
# Builds up to test.12 carried the BYD (Vivid) player app's interface as relocated classes.
byd=[k for k in files if k.startswith(('Llocal/bydui/','Lcom/vivid/'))];vehicle=[k for k,v in files.items() if 'Landroid/hardware/bydauto/' in v]
check('No BYD-derived interface or vehicle API',not byd and not vehicle,f'{len(byd)} BYD-derived classes, {len(vehicle)} using the vehicle API')
driver=files.get('Llocal/spotify/unified/HeadUnitDriver;','')
queries=[n.get(A+'authorities') for q in m.findall('queries') for n in q.findall('provider')]
check('Driving side / BYD car service only on BYD','HeadUnitProfile;->isByd()Z' in driver and 'com.byd.car.carinfo.ICarInfoService' in driver and 'com.byd.car.server.provider.CarServiceProvider' in queries and 'Lcom/byd/spi/ipc/cursor/BinderCursor$BinderParcelable;' in files)
panel=component('service','local.spotify.unified.PanelProviderService')
panel_meta={n.get(A+'name'):n.get(A+'value') for n in (panel.findall('meta-data') if panel is not None else [])}
check('Panel provider / exported service, action and meta-data',panel is not None and panel.get(A+'exported')=='true' and panel.get(A+'permission') is None and panel.get(A+'process')==':bydui' and any(x.get(A+'name')=='local.byd.panel.action.PROVIDER' for x in panel.findall('.//action')) and panel_meta.get('local.byd.panel.label')=='Spotify' and all(panel_meta.get(k) for k in ('local.byd.panel.min_width_dp','local.byd.panel.min_height_dp')) and 'com.google.android.apps.maps.automotive' in [n.get(A+'name') for q in m.findall('queries') for n in q.findall('package')])
provider=''.join(v for k,v in files.items() if k.startswith(('Llocal/spotify/unified/PanelProviderService','Llocal/spotify/unified/PanelPlayerView')))
access=files.get('Llocal/spotify/unified/PanelAccess;','')
check('Panel provider / caller checked in every message','Landroid/os/Message;->sendingUid:I' in provider and 'PanelAccess;->host(' in provider and 'a8401545b03562933c095667ae0eb54bab189d8571010e4eb3384753ad6508d5' in access)
check('Panel provider / no music start, screen or setting on bind',provider!='' and not any(v in provider for v in ('ui_open','HeadUnitRuntime;->playerOpened','HeadUnitRuntime;->requestResume','"set"','TaskGuardService')))
build_info=files.get('Llocal/spotify/unified/HeadUnitBuild;','');test_host=component('activity','local.spotify.unified.PanelTestActivity')
debug_flag='DEBUG:Z = true' in build_info
check('Panel provider / test host and own uid only in debug builds',(test_host is not None and 'Llocal/spotify/unified/PanelTestActivity;' in files and debug_flag) if args.debug_build else (test_host is None and component('activity','local.spotify.unified.WidgetTestActivity') is None and 'Llocal/spotify/unified/PanelTestActivity;' not in files and 'Llocal/spotify/unified/WidgetTestActivity;' not in files and 'DEBUG:Z = false' in build_info),'debug build' if args.debug_build else 'release build')
widgets=[component('receiver','local.spotify.unified.'+c) for c in ('HuWidgetWide','HuWidgetSquare')];actions=component('receiver','local.spotify.unified.HuWidgetActions')
check('Widgets / providers with resources, private button receiver',all(w is not None and w.get(A+'process')==':bydui' and any(x.get(A+'name')=='android.appwidget.action.APPWIDGET_UPDATE' for x in w.findall('.//action')) and any(md.get(A+'name')=='android.appwidget.provider' and md.get(A+'resource') for md in w.findall('meta-data')) for w in widgets) and actions is not None and actions.get(A+'exported')=='false' and 'HeadUnitRuntime;->metadata(' in files.get('Landroid/support/v4/media/session/MediaSessionCompat;',''))
alias=component('activity-alias','local.bydui.com.vivid.spotify.MainActivity');views=[f for f in alias.findall('intent-filter') if any(x.get(A+'name')=='android.intent.action.VIEW' for x in f.findall('action'))] if alias is not None else []
hosts={(d.get(A+'scheme'),d.get(A+'host')) for f in views for d in f.findall('data')}
check('Links / spotify:, open.spotify.com and spotify.link open our interface',{('spotify',None),('https','open.spotify.com'),('https','spotify.link')}<=hosts and all(any(c.get(A+'name')=='android.intent.category.BROWSABLE' for c in f.findall('category')) for f in views))
compat=files.get('Landroid/support/v4/media/session/MediaSessionCompat;','')
check('Broadcasts / session metadata, playback and queue hooks','HeadUnitRuntime;->metadata(' in compat and 'HeadUnitRuntime;->playback(' in compat and 'HeadUnitRuntime;->queue()' in compat and 'com.spotify.music.metadatachanged' in files.get('Llocal/spotify/unified/HuBroadcasts;',''))
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
android_id=[k for k,v in files.items() if not k.startswith('Llocal/') and '"android_id"' in v]
check('Sign-in / pinned device ID',bool(android_id) and all('HeadUnitIdentity;->secureString(' in files[k] and 'Settings$Secure;->getString(' not in files[k] for k in android_id) and 'headunit_identity_v1' in files.get('Llocal/spotify/unified/HeadUnitIdentity;',''),f'{len(android_id)} SDK call sites')
accounts='Llocal/spotify/unified/HeadUnitAccounts;->'
check('Sign-in / reasons and stale account removals',all(accounts+h in files.get(k,'') for k,h in (('Lzb/a;','loggedIn('),('Lzb/a;','loggedOut('),('Lzb/a;','deprecated()V'),('Lzb/a;','androidAccount('),('Lac/j;','logoutRequested('),('Lvr/e;','logoutRequested('),('Lza/d;','tokenError('),('Lis/e;','serverRejected()V'),('Lcom/spotify/automotive/accountmanagerv2/AccountRemovedBroadcastReceiver;','ignoreRemoval('))))
check('Sign-in / one recovery per boot',all(accounts+'beforeStartupRead(' in files.get(k,'') for k in ('Lac/j;','Lvr/e;')) and all(v in files.get('Llocal/spotify/unified/HeadUnitAccounts;','') for v in ('recovery_boot','DriverProfiles;->restoreSaved(','expected_empty')))
check('Account transition transport guards',all('PlaybackGate;->accountTransition()Z' in files.get(k,'') for k in ('Lbd/j;','Landroid/support/v4/media/session/MediaSessionCompat$Callback$MediaSessionCallbackApi21;','Llocal/spotify/unified/HeadUnitPlaybackService;')))
runtime=files.get('Llocal/spotify/unified/HeadUnitRuntime;','')
rhythm=files.get('Llocal/spotify/unified/BydMusicRhythm;','')
rhythm_output=files.get('Llocal/spotify/unified/BydMusicRhythm$DirectOutput;','')
check('BYD rhythm / off-main profile guard','HeadUnitProfile;->isByd()Z' in rhythm and 'Landroid/os/HandlerThread;' in rhythm and 'BydMusicRhythm;->init()V' in runtime)
check('BYD rhythm / one token and vehicle-owned effects',all(v in rhythm_output for v in ('startAudioOutput','stopAudioOutput','com.byd.mediacenter','Landroid/os/Binder;')) and not any(v in rhythm_output for v in ('setColor','setBrightness','BYDAutoAtmosphereLamp')))
check('BYD rhythm / playback and session cleanup','BydMusicRhythm;->playback(I)V' in runtime and 'BydMusicRhythm;->stop()V' in gate and 'HeadUnitRuntime;->sessionEnded()V' in files.get('Landroid/support/v4/media/session/MediaSessionCompat;',''))
diagnostic_provider=component('provider','local.spotify.unified.DiagnosticReportProvider');diagnostic_activity=component('activity','local.spotify.unified.DiagnosticActivity')
if diagnostic_provider is not None:
 check('Diagnostics / private components and temporary URI grants',diagnostic_provider.get(A+'exported')=='false' and diagnostic_provider.get(A+'grantUriPermissions')=='true' and diagnostic_activity is not None and diagnostic_activity.get(A+'exported')=='false' and diagnostic_activity.get(A+'process')==':settings' and 'android.permission.READ_LOGS' not in perms)
 diagnostic_log=files.get('Llocal/spotify/unified/DiagnosticLog;','');diagnostic_report=files.get('Llocal/spotify/unified/DiagnosticReport;','');diagnostic_share=files.get('Llocal/spotify/unified/DiagnosticActivity;','')
 schema=files.get('Llocal/spotify/unified/DiagnosticSchema;','');config=files.get('Llocal/spotify/unified/DiagnosticsConfig;','');queue=files.get('Llocal/spotify/unified/DiagnosticsQueue;','');https=files.get('Llocal/spotify/unified/DiagnosticHttps;','');automatic=files.get('Llocal/spotify/unified/AutomaticDiagnostics;','')
 rhythm_all=''.join(v for k,v in files.items() if k.startswith('Llocal/spotify/unified/BydMusicRhythm'))
 check('Diagnostics / bounded allowlisted private recording','MAX_BYTES:I' in diagnostic_log and 'MAX_PENDING:I' in diagnostic_log and 'DiagnosticSchema;->eventData(' in diagnostic_log and 'pruneAll(' in diagnostic_log and 'getCause()Ljava/lang/Throwable;' in diagnostic_log and 'getMessage()Ljava/lang/String;' not in diagnostic_log and 'DiagnosticLog;->failure(' in rhythm_all)
 check('Diagnostics / strict Spotify playback v2','spotify-android-head-units' in config and 'https://diagnostics.localcharge.app' in config and all(v in schema for v in ('Wrong project/consent','Event is not in playback profile','Components are not registered','Too many events')) and 'schema_version' in diagnostic_report and 'public_issue' in diagnostic_report)
 check('Diagnostics / exact persisted manual consent and receipt','Landroid/util/AtomicFile;' in queue and 'Ljava/security/SecureRandom;' in queue and 'Public-report consent required' in queue and all(v in queue for v in ('manual','cancel_uncertain','delivery_uncertain','invalid_issue_link','report_conflict')))
 check('Diagnostics / fixed HTTPS origin and receipt headers','Ljavax/net/ssl/HttpsURLConnection;' in https and 'setInstanceFollowRedirects(Z)V' in https and all(v in https for v in ('Idempotency-Key','X-Report-Token','/v1/reports')) and 'setSSLSocketFactory' not in https and 'setHostnameVerifier' not in https)
 job=component('service','local.spotify.unified.DiagnosticUploadJob');notice=component('activity','local.spotify.unified.DiagnosticNoticeActivity')
 check('Diagnostics / native persisted job and private notice',job is not None and job.get(A+'permission')=='android.permission.BIND_JOB_SERVICE' and notice is not None and notice.get(A+'exported')=='false' and 'Landroid/app/job/JobScheduler;' in files.get('Llocal/spotify/unified/DiagnosticUploadJob;','') and 'setPersisted(Z)' in files.get('Llocal/spotify/unified/DiagnosticUploadJob;',''))
 check('Diagnostics / consent and negotiated automatic support','Server support is checked before each automatic upload.' in diagnostic_share and 'DiagnosticsConfig;->automaticSupported()Z' in automatic and 'notice_accepted' in automatic and 'Send public report' in diagnostic_share)
 check('Diagnostics / sanitized ZIP export and share/save UI','report.json' in diagnostic_report and 'device.json' not in diagnostic_report and 'DiagnosticSchema;->report(' in diagnostic_report and 'android.intent.action.SEND' in diagnostic_share and 'android.intent.action.CREATE_DOCUMENT' in diagnostic_share and 'setClipData(' in diagnostic_share)
update_receiver=component('receiver','local.spotify.unified.UpdateResultReceiver')
if update_receiver is not None or 'android.permission.REQUEST_INSTALL_PACKAGES' in perms:
 feed=files.get('Llocal/spotify/unified/UpdateFeed;','');updates=files.get('Llocal/spotify/unified/AppUpdates;','');dialog=files.get('Llocal/spotify/unified/UpdateDialog;','')
 check('Updates / installer permission and private result receiver','android.permission.REQUEST_INSTALL_PACKAGES' in perms and update_receiver is not None and update_receiver.get(A+'exported')=='false' and not update_receiver.findall('intent-filter') and update_receiver.get(A+'process') is None and 'AppUpdates;->owns(' in files.get('Llocal/spotify/unified/DiagnosticUploadJob;',''))
 check('Updates / pinned feed, release URLs and certificate',all(v in feed for v in ('https://diagnostics.localcharge.app/v1/updates/spotify-android-head-units','a29d95f7a8eda7508dd97fa0c7c6889a65677050e69e4c0f14123d42d33dc2b1','https://github.com/romanchukg-cloud/spotify-android-head-units/releases/','objects.githubusercontent.com')) and 'setInstanceFollowRedirects(Z)V' in updates and 'UpdateFeed;->downloadOrigin(' in updates)
 check('Updates / verified APK and Android Update confirmation',all(v in updates for v in ('getPackageArchiveInfo(','getApkContentsSigners()','UpdateDownload;->hash(','PackageInstaller$Session;->commit(','setRequireUserAction(I)V','AutoResumePolicy;->busy(')) and 'install(' not in files.get('Llocal/spotify/unified/UpdateResultReceiver;','') and 'Not a system installer' in dialog and 'UpdateDialog;->maybeOffer(' in ''.join(v for k,v in files.items() if k.startswith('Llocal/spotify/unified/UnifiedFactory')))
factory=''.join(v for k,v in files.items() if k.startswith('Llocal/spotify/unified/UnifiedFactory'))
policy=files.get('Llocal/spotify/unified/AutoResumePolicy;','')
check('Acknowledged resume / bounded readiness retries','Auto resume confirmed PLAYING' in runtime and 'AutoResumePolicy;->shouldPlay(' in ''.join(v for k,v in files.items() if k.startswith('Llocal/spotify/unified/HeadUnitPlaybackService')) and 'expired(JJ)Z' in policy)
check('Opt-in player launch / profile handoff guard','play_open' in files.get('Llocal/spotify/unified/HeadUnitPreferences;','') and 'ui_open' in factory and 'local.profile.qr' in factory and 'getTaskId()I' in factory)
main=''.join(v for k,v in files.items() if k.startswith(('Llocal/spotify/unified/HuPlayerActivity','Llocal/spotify/unified/HuRail')))
check('Settings entry / density context','HeadUnitSettings;->open(' in main and 'HeadUnitSettings;->scaled(' in main)
with zipfile.ZipFile(apk) as z:
 names=z.namelist()
 check('Interface typeface / widget resources only',all(n in names for n in ('assets/hu_fonts/Figtree-wght.ttf','assets/hu_fonts/OFL.txt','hu/res/layout/hu_widget_wide.xml','hu/res/xml/hu_widget_square_info.xml')) and 'classes3.dex' not in names and not any(n.startswith('ui/') for n in names))
 versions={n:int(z.read(n)[4:7]) for n in names if re.fullmatch(r'classes\d*\.dex',n)}
 check('DEX version compatible with Android 9',all(v<=39 for v in versions.values()),str(versions))
# Our interface's texts (HuText): a table of keys and one row per language, all the same length.
text=files.get('Llocal/spotify/unified/HuText;','')
rows=[int(m[2])-int(m[1])+1 for m in re.finditer(r'filled-new-array/range \{v(\d+) \.\. v(\d+)\}, \[Ljava/lang/String;',text)]
check('UI translations (en, uk, ru, ar, zh)',len(rows)==6 and len(set(rows))==1 and all('"'+l+'"' in text for l in ('uk','ru','ar','zh')),f'{rows[0] if rows else 0} texts, {len(rows)} tables')
signer=bt/'apksigner';align=bt/'zipalign'
try:out=run([str(signer),'verify','--verbose','--min-sdk-version',str(args.min_api),str(apk)]);check('APK signature', 'Verified using v3 scheme (APK Signature Scheme v3): true' in out)
except (OSError,subprocess.CalledProcessError):check('APK signature',False)
try:run([str(align),'-c','-P','16','4',str(apk)]);check('ZIP / native 16 KiB alignment',True)
except (OSError,subprocess.CalledProcessError):check('ZIP / native 16 KiB alignment',False)
print(f'RESULT {sum(checks)} PASS, {len(checks)-sum(checks)} FAIL')
print('LIMIT: static policy checks do not validate login, audio, all SDK API calls, or real head-unit firmware.')
if temp:temp.cleanup()
sys.exit(0 if all(checks) else 1)

from pathlib import Path
import re,xml.etree.ElementTree as E
base=Path(__file__).resolve().parent;out=base/'backend';stable=base.parent/'spotify55-adaptation/backend'
# All edits are regenerated from the reviewed stable sources.
for rel in ['bd/j.smali','android/support/v4/media/session/MediaSessionCompat$Callback$MediaSessionCallbackApi21.smali']:
 p=out/'smali'/rel;s=(stable/'smali'/rel).read_text()
 def patch(m):
  body=m[0];name=body.splitlines()[0].split('(',1)[0].split()[-1]
  guard=r'    invoke-static \{\}, Llocal/spotify/close/PlaybackGate;->blocked\(\)Z\n.*?if-eqz [^,]+, (?P<allowed>:\w+)\n.*?    (?P=allowed)\n'
  if 'PlaybackGate;->blocked()Z' in body:
   replacement='    invoke-static {p1}, Llocal/spotify/close/PlaybackGate;->mediaButton(Landroid/content/Intent;)V\n' if name=='onMediaButtonEvent' else '    invoke-static {}, Llocal/spotify/close/PlaybackGate;->userPlay()V\n'
   # Account changes must block transport until both old SDK processes stop.
   exit_='    const/4 v0, 0x0\n    return v0\n' if name=='onMediaButtonEvent' else '    return-void\n'
   lock='    invoke-static {}, Llocal/spotify/close/PlaybackGate;->accountTransition()Z\n    move-result v0\n    if-eqz v0, :hu_account_ready\n'+exit_+'    :hu_account_ready\n'
   body,count=re.subn(guard,lambda m:lock+replacement,body,count=1,flags=re.S);assert count==1
  if name in ('onPause','onStop'):
   body=re.sub(r'(    \.locals \d+\n)',r'\1\n    invoke-static {}, Llocal/spotify/unified/HeadUnitRuntime;->explicitPause()V\n',body,count=1)
  return body
 s=re.sub(r'\.method[^\n]*\n.*?\.end method',patch,s,flags=re.S);p.write_text(s)
p=out/'smali/android/support/v4/media/session/MediaSessionCompat.smali';s=(stable/'smali/android/support/v4/media/session/MediaSessionCompat.smali').read_text()
s=re.sub(r'(\.method public setPlaybackState\([^\n]*\n    \.locals \d+\n)',r'\1\n    invoke-static {p1}, Llocal/spotify/unified/HeadUnitRuntime;->playback(Ljava/lang/Object;)V\n',s)
s,count=re.subn(r'(\.method public release\(\)V\n    \.locals \d+\n)',r'\1\n    invoke-static {}, Llocal/spotify/unified/HeadUnitRuntime;->sessionEnded()V\n',s);assert count==1;p.write_text(s)
p=out/'smali/le/d.smali';s=(stable/'smali/le/d.smali').read_text()
a='''.method public final a(ILjava/lang/String;)Z
    .locals 2
    const/16 v0, 0x2710
    if-ge p1, v0, :hu_check_package
    const/4 v0, 0x1
    return v0
    :hu_check_package
    iget-object v0, p0, Lle/d;->a:Landroid/content/ContextWrapper;
    invoke-static {v0, p1, p2}, Llocal/spotify/unified/HeadUnitControllers;->matches(Landroid/content/Context;ILjava/lang/String;)Z
    move-result v1
    if-eqz v1, :hu_bad_identity
    invoke-virtual {p0, p2}, Lle/d;->b(Ljava/lang/String;)Z
    move-result v1
    return v1
    :hu_bad_identity
    invoke-static {p2, v1}, Llocal/spotify/unified/HeadUnitControllers;->result(Ljava/lang/String;Z)Z
    move-result v1
    return v1
.end method'''
s=re.sub(r'\.method public final a\(ILjava/lang/String;\)Z.*?\.end method',lambda m:a,s,flags=re.S)
start=s.index('.method public final b(');prefix=s[:start];body=s[start:]
body=body.replace('.locals 10','.locals 12',1)
body=body.replace('    .locals 12','''    .locals 12
    move-object v10, p1
    iget-object v11, p0, Lle/d;->a:Landroid/content/ContextWrapper;
    invoke-static {v11, p1}, Llocal/spotify/unified/HeadUnitControllers;->allow(Landroid/content/Context;Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :hu_spotify_allowlist
    return v0
    :hu_spotify_allowlist''',1)
body=re.sub(r'    return (v\d+)',lambda m:'    invoke-static {v10, '+m[1]+'}, Llocal/spotify/unified/HeadUnitControllers;->result(Ljava/lang/String;Z)Z\n    move-result '+m[1]+'\n    return '+m[1],body)
p.write_text(prefix+body)
# Manifest policy and standalone receivers/settings.
A='{http://schemas.android.com/apk/res/android}';E.register_namespace('android',A[1:-1]);p=out/'AndroidManifest.xml';m=E.parse(p);root=m.getroot();app=root.find('application');app.set(A+'enableOnBackInvokedCallback','false')
for n in list(root):
 if n.tag=='uses-permission' and n.get(A+'name')=='com.google.android.gms.permission.AD_ID':root.remove(n)
 if n.tag=='uses-feature' and n.get(A+'name')=='android.hardware.type.automotive':n.set(A+'required','false')
for n in list(app):
 if n.tag=='meta-data' and n.get(A+'name','').startswith(('com.android.stamp.','com.android.vending.')):app.remove(n)
E.SubElement(root,'uses-permission',{A+'name':'android.permission.RECEIVE_BOOT_COMPLETED'})
E.SubElement(app,'meta-data',{A+'name':'firebase_crashlytics_collection_enabled',A+'value':'false'})
E.SubElement(app,'provider',{A+'name':'local.spotify.unified.HeadUnitSettingsProvider',A+'authorities':'com.spotify.music.local.headunit',A+'exported':'false',A+'initOrder':'100'})
E.SubElement(app,'activity',{A+'name':'local.spotify.unified.HeadUnitSettingsActivity',A+'process':':settings',A+'exported':'false',A+'theme':'@android:style/Theme.Material.NoActionBar'})
E.SubElement(app,'activity',{A+'name':'local.spotify.unified.DiagnosticActivity',A+'process':':settings',A+'exported':'false',A+'theme':'@android:style/Theme.Material.NoActionBar',A+'configChanges':'orientation|screenSize'})
E.SubElement(app,'activity',{A+'name':'local.spotify.unified.DiagnosticNoticeActivity',A+'process':':settings',A+'exported':'false',A+'theme':'@android:style/Theme.Material.NoActionBar',A+'configChanges':'orientation|screenSize'})
E.SubElement(app,'service',{A+'name':'local.spotify.unified.DiagnosticUploadJob',A+'exported':'true',A+'permission':'android.permission.BIND_JOB_SERVICE'})
E.SubElement(app,'provider',{A+'name':'local.spotify.unified.DiagnosticReportProvider',A+'authorities':'com.spotify.music.local.diagnostics',A+'exported':'false',A+'grantUriPermissions':'true'})
E.SubElement(app,'service',{A+'name':'local.spotify.unified.HeadUnitPlaybackService',A+'exported':'false',A+'foregroundServiceType':'mediaPlayback'})
r=E.SubElement(app,'receiver',{A+'name':'local.spotify.unified.HeadUnitMediaReceiver',A+'exported':'true'})
f=E.SubElement(r,'intent-filter');E.SubElement(f,'action',{A+'name':'android.intent.action.MEDIA_BUTTON'})
f=E.SubElement(r,'intent-filter');E.SubElement(f,'action',{A+'name':'android.intent.action.BOOT_COMPLETED'})
m.write(p,encoding='utf-8',xml_declaration=True)
print('Head unit patches: playback gate, media session, controllers, settings and diagnostics')

# Bind the SDK's existing auth storage; never duplicate or replace the SDK.
p=out/'smali/ca/c.smali';s=(stable/'smali/ca/c.smali').read_text()
pattern=r'(\.method public synthetic constructor <init>\(ILjava/lang/Object;\)V\n.*?)(    return-void)'
s,count=re.subn(pattern,lambda m:m[1]+'    invoke-static {p0, p2}, Llocal/spotify/unified/DriverProfiles;->bindCandidate(Ljava/lang/Object;Ljava/lang/Object;)V\n'+m[2],s,count=1,flags=re.S);assert count==1;p.write_text(s)
# The coordinator uses plain Application and survives the SDK/UI handoff.
app.set(A+'allowBackup','false')
E.SubElement(app,'provider',{A+'name':'local.spotify.unified.ProfilesProvider',A+'authorities':'com.spotify.music.local.profiles',A+'exported':'false'})
E.SubElement(app,'activity',{A+'name':'local.spotify.unified.ProfilesActivity',A+'process':':profiles',A+'exported':'false',A+'taskAffinity':'com.spotify.music.profiles',A+'excludeFromRecents':'true',A+'theme':'@android:style/Theme.Material.NoActionBar',A+'configChanges':'orientation|screenSize'})
m.write(out/'AndroidManifest.xml',encoding='utf-8',xml_declaration=True)

# Sign-in stability: a stable device ID, recorded sign-out reasons, ignored
# removals of inactive Android accounts and one recovery of a saved login lost
# on the head unit. Every pattern must match exactly once in the stable SDK.
H='Llocal/spotify/unified/HeadUnitAccounts;->'
def signin(rel,changes):
 p=out/'smali'/rel;s=(stable/'smali'/rel).read_text()
 for pattern,repl in changes:
  assert len(re.findall(pattern,s,flags=re.S))==1,(rel,pattern)
  s=re.sub(pattern,repl,s,count=1,flags=re.S)
 p.write_text(s)
def at_entry(sig,code):return (r'(\.method '+re.escape(sig)+r'\n    \.locals \d+\n)',lambda m:m[1]+'\n    '+code+'\n')
# Saved logins are bound to the device ID, which the SDK reads from ANDROID_ID.
device_id=(r'(const-string (\w+), "android_id"\n\n    )invoke-static \{(\w+), \2\}, Landroid/provider/Settings\$Secure;->getString\(Landroid/content/ContentResolver;Ljava/lang/String;\)Ljava/lang/String;',
 lambda m:m[1]+'invoke-static {'+m[3]+', '+m[2]+'}, Llocal/spotify/unified/HeadUnitIdentity;->secureString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;')
signin('ib/c.smali',[device_id]);signin('v5/q.smali',[device_id])
signin('za/d.smali',[(r'(invoke-virtual \{\w+\}, Lcom/spotify/authentication/login5esperanto/EsAccessTokenClient\$ErrorResponse;->j\(\)Lfb/b;\n\n    move-result-object (\w+)\n)',lambda m:m[1]+'\n    invoke-static {'+m[2]+'}, '+H+'tokenError(Ljava/lang/Object;)V\n')])
signin('is/e.smali',[(r'(    const-string \w+, "Auth response: InvalidCredentials\. logging out user\.\.\."\n)',lambda m:'    invoke-static {}, '+H+'serverRejected()V\n\n'+m[1])])
for rel in ['ac/j.smali','vr/e.smali']:
 signin(rel,[(r'(\.method public constructor <init>\((?:(?!\.end method).)*?)(    invoke-virtual \{(\w+)\}, Lca/c;->d\(\)Lio/reactivex/rxjava3/core/Single;)',lambda m:m[1]+'    invoke-static {'+m[3]+'}, '+H+'beforeStartupRead(Ljava/lang/Object;)V\n\n'+m[2]),
  at_entry('public final d(Leb/a;Llu/i;)Ljava/lang/Object;','invoke-static {p1}, '+H+'logoutRequested(Ljava/lang/Object;)V')])
signin('zb/a.smali',[at_entry('public final a()V','invoke-static {}, '+H+'deprecated()V'),
 at_entry('public final b(Ljava/lang/String;)V','invoke-static {p1}, '+H+'androidAccount(Ljava/lang/String;)V'),
 at_entry('public final c(Ljava/lang/String;)V','invoke-static {p1}, '+H+'loggedIn(Ljava/lang/String;)V'),
 at_entry('public final d(Leb/a;)V','invoke-static {p1}, '+H+'loggedOut(Ljava/lang/Object;)V')])
signin('com/spotify/automotive/accountmanagerv2/AccountRemovedBroadcastReceiver.smali',[at_entry('public final onReceive(Landroid/content/Context;Landroid/content/Intent;)V',
 'invoke-static {p1, p2}, '+H+'ignoreRemoval(Landroid/content/Context;Landroid/content/Intent;)Z\n    move-result v0\n    if-eqz v0, :hu_account_removal\n    return-void\n    :hu_account_removal')])
print('Sign-in patches: device ID, sign-out reasons, account removal, recovery')

# App updates: Android's installer permission and the private receiver for the
# PackageInstaller session status. incremental_release.py adds the same
# declarations to a published binary manifest.
MANIFEST_ADDITIONS=[('uses-permission','android.permission.REQUEST_INSTALL_PACKAGES'),('receiver','local.spotify.unified.UpdateResultReceiver')]
if (out/'AndroidManifest.xml').exists():
 p=out/'AndroidManifest.xml';m=E.parse(p);root=m.getroot();app=root.find('application')
 for tag,name in MANIFEST_ADDITIONS:
  E.SubElement(root if tag=='uses-permission' else app,tag,{A+'name':name,**({} if tag=='uses-permission' else {A+'exported':'false'})})
 m.write(p,encoding='utf-8',xml_declaration=True)
print('App updates: installer permission and private result receiver')

# Offline downloads: Spotify Automotive implements downloads (browser actions
# download/downloading/downloaded, offline settings), but its remote
# configuration android-automotive.is_offline_enabled keeps them off for this
# client. The head unit treats the flag as on; Premium and Spotify's download
# limits still apply on the server.
signin('pq/d.smali',[(r'(    const-string v3, "is_offline_enabled"\n\n    invoke-virtual \{v2, v0, v3, v8\}, Lh6/q;->c\(Ljava/lang/String;Ljava/lang/String;Z\)Z\n\n    move-result v18\n)',
 lambda m:m[1]+'\n    const/16 v18, 0x1\n')])
# The backend adds these actions to items only for a media host, which it detects from
# the host-version root hint of the last client that connected. The interface and the
# media-button receiver connect without one, so every client counts as a media host.
signin('ee/a.smali',[(r'\.method public final c\(\)Z\n.*?\.end method','.method public final c()Z\n    .locals 1\n    const/4 v0, 0x1\n    return v0\n.end method')])
print('Offline downloads: client flag on, every client a media host')

# Voice search: our search's microphone (HuSearch) opens the head unit's speech recognition.
# Android 11+ package visibility needs the recogniser intent declared to find it.
p=out/'AndroidManifest.xml';m=E.parse(p);root=m.getroot();app=root.find('application')
q=E.SubElement(root,'queries');i=E.SubElement(q,'intent');E.SubElement(i,'action',{A+'name':'android.speech.action.RECOGNIZE_SPEECH'})
m.write(p,encoding='utf-8',xml_declaration=True)
print('Voice search: recogniser visibility query')

# Jam: the queue card (HuQueue) opens Spotify's own settings page with an extra;
# HeadUnitJam.opened scrolls it to the Jam section so the QR code fits.
signin('com/spotify/automotive/settingspage/SettingsPageActivity.smali',[(r'(\.method public final onCreate\(Landroid/os/Bundle;\)V\n(?:(?!\.end method).)*?)(    return-void\n)',
 lambda m:m[1]+'    invoke-static {p0}, Llocal/spotify/unified/HeadUnitJam;->opened(Landroid/app/Activity;)V\n\n'+m[2])])
print('Jam: settings page scrolls to Jam')


# Our own interface is the main screen. The launcher entry keeps the component name of the
# BYD-derived activity of builds up to test.12 as an alias of HuPlayerActivity, so launcher icons,
# pinned shortcuts, the playback notification and profile switching still open the app by it.
p=out/'AndroidManifest.xml';m=E.parse(p);root=m.getroot();app=root.find('application')
E.SubElement(app,'activity',{A+'name':'local.spotify.unified.HuPlayerActivity',A+'process':':bydui',A+'exported':'false',A+'theme':'@android:style/Theme.Material.NoActionBar',A+'configChanges':'orientation|screenSize',A+'windowSoftInputMode':'adjustResize'})
alias=E.SubElement(app,'activity-alias',{A+'name':'local.bydui.com.vivid.spotify.MainActivity',A+'targetActivity':'local.spotify.unified.HuPlayerActivity',A+'exported':'true'})
f=E.SubElement(alias,'intent-filter');E.SubElement(f,'action',{A+'name':'android.intent.action.MAIN'});E.SubElement(f,'category',{A+'name':'android.intent.category.LAUNCHER'})
m.write(p,encoding='utf-8',xml_declaration=True)
print('Own interface: main screen (launcher alias local.bydui.com.vivid.spotify.MainActivity -> HuPlayerActivity)')

# Driving side: HeadUnitDriver asks the car which seat is the driver's. BYD's car service answers
# through its provider (visible to the app only when declared under <queries>); Android Automotive
# through the android.car library, which other devices lack (not required).
p=out/'AndroidManifest.xml';m=E.parse(p);root=m.getroot();app=root.find('application')
q=E.SubElement(root,'queries');E.SubElement(q,'provider',{A+'authorities':'com.byd.car.server.provider.CarServiceProvider'})
E.SubElement(app,'uses-library',{A+'name':'android.car',A+'required':'false'})
m.write(p,encoding='utf-8',xml_declaration=True)
print('Driving side: car service visibility and the optional android.car library')

# Panel provider: other apps' panels embed our compact player (PanelProviderService, protocol in
# PANEL_PROVIDER.md). Exported without a permission: every message's sending uid is checked
# against the allowed hosts in code (PanelAccess), whose packages are declared visible.
from build_config import DEBUG
p=out/'AndroidManifest.xml';m=E.parse(p);root=m.getroot();app=root.find('application')
q=E.SubElement(root,'queries');E.SubElement(q,'package',{A+'name':'com.google.android.apps.maps.automotive'})
svc=E.SubElement(app,'service',{A+'name':'local.spotify.unified.PanelProviderService',A+'process':':bydui',A+'exported':'true'})
f=E.SubElement(svc,'intent-filter');E.SubElement(f,'action',{A+'name':'local.byd.panel.action.PROVIDER'})
for key,value in (('local.byd.panel.label','Spotify'),('local.byd.panel.min_width_dp','280'),('local.byd.panel.min_height_dp','160')):
 E.SubElement(svc,'meta-data',{A+'name':key,A+'value':value})
# Debug builds only: the test host, in its own process (exported so adb can start it).
if DEBUG:E.SubElement(app,'activity',{A+'name':'local.spotify.unified.PanelTestActivity',A+'process':':paneltest',A+'exported':'true',A+'label':'Panel test',A+'theme':'@android:style/Theme.Material.NoActionBar',A+'configChanges':'orientation|screenSize'})
m.write(p,encoding='utf-8',xml_declaration=True)
print('Panel provider: exported service, allowed-host visibility'+(', debug test host' if DEBUG else ''))

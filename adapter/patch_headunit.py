from pathlib import Path
import re,xml.etree.ElementTree as E
base=Path(__file__).resolve().parent;out=base/'backend';stable=base.parent/'spotify55-adaptation/backend';ui=base/'ui-smali/local/bydui'
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
s=re.sub(r'(\.method public setPlaybackState\([^\n]*\n    \.locals \d+\n)',r'\1\n    invoke-static {p1}, Llocal/spotify/unified/HeadUnitRuntime;->playback(Ljava/lang/Object;)V\n',s);p.write_text(s)
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
# Keep the BYD adapter dormant on all other profiles, including before a
# missing vendor listener subclass can be instantiated.
p=ui/'com/vivid/spotify/byd/b.smali';s=p.read_text();guarded=[]
def vendor(m):
 body=m[0];sig=body.splitlines()[0]
 if 'Landroid/hardware/bydauto/' not in body and not re.search(r' (s|u)\(\)V$',sig):return body
 assert '<init>' not in sig
 ret=sig[-1];assert ret in 'VZ',sig
 locals_=int(re.search(r'\.locals (\d+)',body)[1]);assert locals_>=1
 exit_='    return-void' if ret=='V' else '    const/4 v0, 0x0\n    return v0'
 injected='\n    invoke-static {}, Llocal/spotify/unified/HeadUnitProfile;->isByd()Z\n    move-result v0\n    if-nez v0, :hu_byd_allowed\n'+exit_+'\n    :hu_byd_allowed\n'
 body=re.sub(r'(    \.locals \d+\n)',lambda n:n[1]+injected,body,count=1)
 # Standard media events are handled once by our manifest receiver.
 body=re.sub(r'    const-string v2, "android.intent.action.MEDIA_BUTTON"\n.*?invoke-virtual \{v1, v2\}, Landroid/content/IntentFilter;->addAction\(Ljava/lang/String;\)V\n','',body,flags=re.S)
 guarded.append(sig);return body
s=re.sub(r'\.method[^\n]*\n.*?\.end method',vendor,s,flags=re.S);p.write_text(s)
# Log all broadcasts that reach vendor-specific dynamic receivers.
for p in (ui/'com/vivid/spotify/byd').glob('*.smali'):
 s=p.read_text();s=re.sub(r'(\.method[^\n]* onReceive\(Landroid/content/Context;Landroid/content/Intent;\)V\n    \.locals \d+\n)',r'\1\n    invoke-static {p1, p2}, Llocal/spotify/unified/HeadUnitRuntime;->broadcast(Landroid/content/Context;Landroid/content/Intent;)V\n',s);p.write_text(s)
# The settings button retains the original driving-state check.
p=ui/'com/vivid/spotify/MainActivity.smali';s=p.read_text()
s=s.replace('invoke-virtual {p1, p0}, Llocal/bydui/com/vivid/aaosaudiokit/b;->q(Landroid/app/Activity;)V','invoke-static {p0}, Llocal/spotify/unified/HeadUnitSettings;->open(Landroid/app/Activity;)V')
s+='''
.method protected attachBaseContext(Landroid/content/Context;)V
    .locals 1
    invoke-static {p1}, Llocal/spotify/unified/HeadUnitSettings;->scaled(Landroid/content/Context;)Landroid/content/Context;
    move-result-object v0
    invoke-super {p0, v0}, Llocal/bydui/com/vivid/spotify/b;->attachBaseContext(Landroid/content/Context;)V
    return-void
.end method
''';p.write_text(s)
# Profile detection precedes the UI application's own initialization.
p=ui/'com/vivid/spotify/byd/BYDApplication.smali';s=p.read_text();s=re.sub(r'(\.method public onCreate\(\)V\n    \.locals \d+\n)',r'\1\n    invoke-static {p0}, Llocal/spotify/unified/HeadUnitProfile;->configure(Landroid/content/Context;)V\n',s);p.write_text(s)
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
E.SubElement(app,'service',{A+'name':'local.spotify.unified.HeadUnitPlaybackService',A+'exported':'false',A+'foregroundServiceType':'mediaPlayback'})
r=E.SubElement(app,'receiver',{A+'name':'local.spotify.unified.HeadUnitMediaReceiver',A+'exported':'true'})
f=E.SubElement(r,'intent-filter');E.SubElement(f,'action',{A+'name':'android.intent.action.MEDIA_BUTTON'})
f=E.SubElement(r,'intent-filter');E.SubElement(f,'action',{A+'name':'android.intent.action.BOOT_COMPLETED'})
m.write(p,encoding='utf-8',xml_declaration=True)
(base/'vendor-guards.txt').write_text('\n'.join(guarded)+'\n')
print('Head unit patches:',len(guarded),'guarded BYD methods')

# Bind the SDK's existing auth storage; never duplicate or replace the SDK.
p=out/'smali/ca/c.smali';s=(stable/'smali/ca/c.smali').read_text()
pattern=r'(\.method public synthetic constructor <init>\(ILjava/lang/Object;\)V\n.*?)(    return-void)'
s,count=re.subn(pattern,lambda m:m[1]+'    invoke-static {p0, p2}, Llocal/spotify/unified/DriverProfiles;->bindCandidate(Ljava/lang/Object;Ljava/lang/Object;)V\n'+m[2],s,count=1,flags=re.S);assert count==1;p.write_text(s)
# The coordinator uses plain Application and survives the SDK/UI handoff.
app.set(A+'allowBackup','false')
E.SubElement(app,'provider',{A+'name':'local.spotify.unified.ProfilesProvider',A+'authorities':'com.spotify.music.local.profiles',A+'exported':'false'})
E.SubElement(app,'activity',{A+'name':'local.spotify.unified.ProfilesActivity',A+'process':':profiles',A+'exported':'false',A+'taskAffinity':'com.spotify.music.profiles',A+'excludeFromRecents':'true',A+'theme':'@android:style/Theme.Material.NoActionBar',A+'configChanges':'orientation|screenSize'})
m.write(out/'AndroidManifest.xml',encoding='utf-8',xml_declaration=True)

#!/usr/bin/env python3
"""Logged-out tablet smoke checks; does NOT certify account/audio behavior."""
import os,subprocess,time,json,sys,xml.etree.ElementTree as E
from pathlib import Path
base=Path(__file__).resolve().parent.parent;adb=os.environ.get('ADB','adb');serial=sys.argv[1];api=sys.argv[2];folder=base/'diagnostics'/('api'+api);folder.mkdir(parents=True,exist_ok=True);records=[]
def call(*args,binary=False):return subprocess.check_output([adb,'-s',serial,*args],stderr=subprocess.STDOUT,text=not binary,timeout=60)
def shell(*args):return call('shell',*args)
def instrument(mode,**kw):return shell('am','instrument','-w','-e','mode',mode,*sum((['-e',k,str(v)] for k,v in kw.items()),[]),'hu.tests/hu.tests.Runner')
def snap(name):
 data=call('exec-out','screencap','-p',binary=True);(folder/(name+'.png')).write_bytes(data[data.index(b'\x89PNG'):])
 shell('uiautomator','dump','/sdcard/hu-matrix.xml');(folder/(name+'.xml')).write_text(call('exec-out','cat','/sdcard/hu-matrix.xml'))
for name in ['client','test']:print(call('install','--no-incremental','-r',str(base/'tests/out'/f'{name}.apk')),flush=True)
shell('settings','put','system','screen_off_timeout','1800000');shell('settings','put','system','accelerometer_rotation','0');shell('settings','put','system','user_rotation','0');shell('input','keyevent','82')
shell('logcat','-c');shell('am','start','-n','hu.browser/.Main');time.sleep(3);result=instrument('checks');(folder/'policy.txt').write_text(result);print(result,flush=True)
if 'FAIL' in result:raise RuntimeError('Policy instrumentation failed; inspect policy.txt')
for width,height,density in [(800,480,160),(1024,600,160),(1280,720,240),(1920,720,240),(768,1024,160)]:
 name=f'{width}x{height}-{density}';shell('wm','size',f'{width}x{height}');shell('wm','density',str(density));time.sleep(1)
 shell('am','start','-n','com.spotify.music/local.bydui.com.vivid.spotify.MainActivity');time.sleep(2);snap(name+'-login')
 root=E.parse(folder/(name+'-login.xml'));login=any('log in' in n.get('text','').lower() or n.get('resource-id','').endswith('/btn_login') for n in root.iter('node'))
 if not login:
  time.sleep(3);snap(name+'-login');root=E.parse(folder/(name+'-login.xml'));login=any(n.get('resource-id','').endswith('/btn_login') for n in root.iter('node'))
 print(name,'login visible',login,flush=True)
 settings_run=subprocess.Popen([adb,'-s',serial,'shell','am','instrument','-w','-e','mode','settings','hu.tests/hu.tests.Runner'],stdout=subprocess.PIPE);time.sleep(3);snap(name+'-settings');r=E.parse(folder/(name+'-settings.xml'));setting=any('Stop music' in n.get('text','') for n in r.iter('node'))
 if not setting:
  time.sleep(3);snap(name+'-settings');r=E.parse(folder/(name+'-settings.xml'));setting=any('Stop music' in n.get('text','') for n in r.iter('node'))
 settings_run.communicate(timeout=30)
 records.append({'screen':name,'logged_out_login_visible':login,'settings_visible':setting});print(name,'settings visible',setting,flush=True)
shell('am','force-stop','hu.browser');shell('am','start','-n','hu.browser/.Main');time.sleep(3);browser=shell('logcat','-d','-s','HU_BROWSER');(folder/'browser-allowed.txt').write_text(browser);print(browser,flush=True)
instrument('external',value='false');shell('am','force-stop','hu.browser');shell('am','start','-n','hu.browser/.Main');time.sleep(3);browser=shell('logcat','-d','-s','HU_BROWSER','SpotifyHU');(folder/'browser-rejected.txt').write_text(browser);print(browser[-1800:],flush=True)
instrument('external',value='true')
# Exercise legacy Back from logged-out main (Now Playing/list require login).
shell('am','start','-n','com.spotify.music/local.bydui.com.vivid.spotify.MainActivity');time.sleep(1);shell('input','keyevent','4');time.sleep(.5);(folder/'back-top.txt').write_text(shell('dumpsys','activity','activities'))
# Establish a framework playback session + PendingIntent without a Spotify account.
print(instrument('prime-cold'),flush=True)
for key in ['85','87','88','126','127','79']:shell('input','keyevent',key);time.sleep(.4)
for command in ['play','next']:shell('cmd','media_session','dispatch',command);time.sleep(.4)
(folder/'media-smoke.txt').write_text(shell('logcat','-d','-s','SpotifyHU'))
(folder/'crash.txt').write_text(shell('logcat','-d','-b','crash'))
(folder/'matrix.json').write_text(json.dumps(records,indent=2));print(json.dumps(records),flush=True)

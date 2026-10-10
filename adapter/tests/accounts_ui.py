#!/usr/bin/env python3
"""Disposable, logged-out emulator test. Does not perform Spotify authorization."""
import subprocess,time,sys,re,json,xml.etree.ElementTree as E
from pathlib import Path
serial=sys.argv[1];api=sys.argv[2];base=Path(__file__).resolve().parent.parent;seed='--seed' in sys.argv;folder=base/'diagnostics'/('accounts-api'+api+('-seeded' if seed else ''));folder.mkdir(parents=True,exist_ok=True)
def adb(*args,binary=False):return subprocess.check_output(['adb','-s',serial,*args],text=not binary,timeout=60)
def shell(*args):return adb('shell',*args)
def tree():
 for attempt in range(3):
  shell('rm','-f','/sdcard/accounts-ui.xml');shell('uiautomator','dump','/sdcard/accounts-ui.xml')
  try:return E.fromstring(adb('exec-out','cat','/sdcard/accounts-ui.xml'))
  except (E.ParseError,subprocess.CalledProcessError):time.sleep(2)
 raise RuntimeError('UI hierarchy unavailable')
def click(node):
 x,y,xx,yy=map(int,re.findall(r'\d+',node.get('bounds')));shell('input','tap',str((x+xx)//2),str((y+yy)//2))
def find(root,text):return next((n for n in root.iter('node') if n.get('text','').casefold()==text.casefold()),None)
def snapshot(name):
 root=tree();E.ElementTree(root).write(folder/(name+'.xml'),encoding='unicode');data=adb('exec-out','screencap','-p',binary=True);(folder/(name+'.png')).write_bytes(data[data.index(b'\x89PNG'):]);return root
def top():
 d=shell('dumpsys','activity','activities');return '\n'.join(l.strip() for l in d.splitlines() if 'ResumedActivity' in l)
shell('wm','size','800x480');shell('wm','density','160');shell('input','keyevent','82');shell('logcat','-c');shell('am','start','-n','com.spotify.music/local.bydui.com.vivid.spotify.MainActivity');time.sleep(3)
fixture=subprocess.Popen(['adb','-s',serial,'shell','am','instrument','-w','-e','mode','accounts-ui','-e','seed',str(seed).lower(),'hu.tests/hu.tests.Runner'],stdout=subprocess.PIPE)
time.sleep(8);settings=snapshot('settings');settings_task=re.search(r' t(\d+)',top())[1];entry=find(settings,'Accounts');assert entry is not None,'Accounts absent in head unit settings';click(entry);time.sleep(3)
assert re.search(r' t(\d+)',top())[1]!=settings_task,'Accounts shares the player task'

for attempt in range(12):
 accounts=tree()
 if find(accounts,'Loading profiles…') is None:break
 time.sleep(1)
accounts=snapshot('accounts');
if seed:
 assert find(accounts,'Synthetic driver B') is not None,'Second synthetic row hidden'
 assert len([n for n in accounts.iter('node') if n.get('text')=='Switch account'])==2,'Switch buttons missing'
add=find(accounts,'Add account · QR');assert add is not None,'Add account absent';assert find(accounts,'Could not update profiles. Your saved profiles have been kept. Try again.') is None,'SDK auth storage not ready'
# End instrumentation before testing backend death: Android force-stops the
# whole target package when an instrumented backend is killed.
shell('am','force-stop','com.spotify.music');fixture.communicate(timeout=15)
shell('am','start','-n','com.spotify.music/local.bydui.com.vivid.spotify.MainActivity');time.sleep(4)
login=next(n for n in tree().iter('node') if n.get('resource-id','').endswith('/btn_login'));click(login)
for attempt in range(20):
 time.sleep(1)
 if 'QrCodeLoginActivityV2' in top():break
assert 'QrCodeLoginActivityV2' in top(),'Normal QR entry did not open'
entry=find(tree(),'Accounts');assert entry is not None,'Accounts entry absent on QR screen';click(entry);time.sleep(3)
for attempt in range(12):
 accounts=tree()
 if find(accounts,'Loading profiles…') is None:break
 time.sleep(1)
add=find(accounts,'Add account · QR');assert add is not None,'Accounts did not reopen from QR'
old=shell('pidof','com.spotify.music').strip();click(add);time.sleep(1)
confirm=tree();go=find(confirm,'Continue');assert go is not None,'Add confirmation absent';click(go)
qr=False
for i in range(25):
 time.sleep(1)
 if 'QrCodeLoginActivityV2' in top():qr=True;break
assert qr,'Native QR login did not open'
new=shell('pidof','com.spotify.music').strip();assert old and new and old!=new,'Backend was not restarted'
# Do not save the QR UI or screenshot: it contains a live sign-in session.
q=tree();entry=find(q,'Accounts');assert entry is not None,'Return to Accounts absent on native QR screen';click(entry);time.sleep(3)
returned=snapshot('returned-accounts');assert find(returned,'Add account · QR') is not None,'Accounts did not reopen'
crash=shell('logcat','-d','-b','crash');assert 'FATAL EXCEPTION' not in crash,'Unexpected app crash'
(folder/'result.json').write_text(json.dumps({'api':int(api),'viewport':'800x480@160','settings_accounts_entry':True,'separate_task':True,'accounts_add_visible':True,'native_qr_opened':True,'backend_restarted':True,'return_from_qr':True,'crash_buffer_empty':True,'real_authorization':False,'synthetic_profile_rows':seed},indent=2)+'\n');print((folder/'result.json').read_text(),flush=True)

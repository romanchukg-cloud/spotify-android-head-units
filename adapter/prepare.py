from pathlib import Path
import shutil,re,json,xml.etree.ElementTree as E
base=Path(__file__).resolve().parent;prev=base.parent/'spotify55-adaptation';ui=base/'ui';out=base/'backend'
if not out.exists():shutil.copytree(prev/'backend',out,ignore=shutil.ignore_patterns('build','original'))
files=list((ui/'smali').rglob('*.smali'))
classes={re.search(r'^\.class[^\n]*?(L[^;\n]+;)' ,p.read_text(),re.M).group(1) for p in files}
cm={c:'Llocal/bydui/'+c[1:] for c in classes};dm={c[1:-1].replace('/','.'):v[1:-1].replace('/','.') for c,v in cm.items()}
ids={int(n.get('id'),16):int(n.get('id'),16)-0x01000000 for n in E.parse(ui/'res/values/public.xml').getroot()}
resnames={(n.get('type'),n.get('name')):hex(int(n.get('id'),16)-0x01000000) for n in E.parse(ui/'res/values/public.xml').getroot()}
def transform(s):
 if s in dm:return dm[s]
 if s=='com.vivid.music.byd':return 'com.spotify.music'
 if s.startswith('com.vivid.music.byd.'):return 'com.spotify.music.'+s[len('com.vivid.music.byd.'):]
 if s.startswith('res/'):return 'ui/'+s
 return s
for p in files:
 s=p.read_text()
 if p.name=='BYDApplication.smali':
  def replace_method(sig,body):
   global_unused=None
   return re.sub(r'(\.method '+re.escape(sig)+r'\n).*?\.end method',lambda m:m[1]+body+'\n.end method',s,flags=re.S)
  # Standalone player: OEM vehicle permissions are not prerequisites for music.
  s=replace_method('public c()Z','    .locals 1\n    const/4 v0, 0x1\n    return v0')
  s=replace_method('public b(Landroid/content/Context;)Ljava/util/Set;','    .locals 1\n    invoke-static {}, Ljava/util/Collections;->emptySet()Ljava/util/Set;\n    move-result-object v0\n    return-object v0')
  s=replace_method('public e()Z','    .locals 1\n    const/4 v0, 0x0\n    return v0')
  s=replace_method('private z()V','    .locals 0\n    return-void')
  s=replace_method('public k()Z','    .locals 1\n    const/4 v0, 0x0\n    return v0')
  s=re.sub(r'    :try_start_0\n    new-instance v0, Lcom/byd/car/DiCarConfig\$Builder;.*?    :goto_0\n', '',s,flags=re.S)
 s=re.sub(r'L[\w/$]+;',lambda m:cm.get(m[0],m[0]),s)
 s=re.sub(r'0x[0-9a-fA-F]+',lambda m:hex(ids[int(m[0],16)]) if int(m[0],16) in ids else m[0],s)
 s=re.sub(r'"([^"\n]*)"',lambda m:'"'+transform(m[1])+'"',s)
 # Dynamic UI resources live in the second resource-table package.
 if p.name!='e2.smali':
  s=re.sub(r'invoke-virtual (\{[^}]+\}), Landroid/content/res/Resources;->getIdentifier\(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;\)I',r'invoke-static \1, Llocal/spotify/unified/ResourceNames;->getIdentifier(Landroid/content/res/Resources;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I',s)
 # Bundle IPC names refer to canonical backend media Parcelables. Recreate
 # these values with the relocated UI creators before its casts are executed.
 for method,ret in [('getParcelable','Landroid/os/Parcelable;'),('getParcelableArrayList','Ljava/util/ArrayList;'),('getParcelableArray','[Landroid/os/Parcelable;')]:
  pattern=r'invoke-virtual (\{[^}]+\}), Landroid/os/Bundle;->'+method+r'\(Ljava/lang/String;\)'+re.escape(ret)
  s=re.sub(pattern,lambda m:'invoke-static '+m[1]+', Llocal/spotify/unified/ParcelBridge;->'+method+'(Landroid/os/Bundle;Ljava/lang/String;)'+ret,s)
 # Search/custom-action requests carry a legacy ResultReceiver. Keeping its
 # relocated class name crashes the backend when it casts the incoming Bundle.
 if str(p.relative_to(ui/'smali'))=='android/support/v4/media/MediaBrowserCompat$k.smali':
  s=re.sub(r'invoke-virtual (\{[^}]+\}), Landroid/os/Bundle;->putParcelable\(Ljava/lang/String;Landroid/os/Parcelable;\)V',r'invoke-static \1, Llocal/spotify/unified/ParcelBridge;->putBrowserParcelable(Landroid/os/Bundle;Ljava/lang/String;Landroid/os/Parcelable;)V',s)
 dest=base/'ui-smali'/('local/bydui/'+str(p.relative_to(ui/'smali')));dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(s)
(base/'class-map.json').write_text(json.dumps(dm,indent=2));(base/'resource-map.json').write_text(json.dumps({hex(k):hex(v) for k,v in ids.items()},indent=2))
A='{http://schemas.android.com/apk/res/android}';E.register_namespace('android',A[1:-1])
m=E.parse(prev/'backend/AndroidManifest.xml');mr=m.getroot();app=mr.find('application');um=E.parse(ui/'AndroidManifest.xml').getroot();ua=um.find('application')
app.set(A+'appComponentFactory','local.spotify.unified.UnifiedFactory');app.set(A+'theme','@style/AppTheme');app.set(A+'icon','@mipmap/ic_launcher')
app.set(A+'requestLegacyExternalStorage','true');app.set(A+'extractNativeLibs','true')
existing={(n.tag,n.get(A+'name')) for n in mr}
for node in um:
 if node.tag.startswith('uses-permission') and 'BYDAUTO_' not in node.get(A+'name','') and node.get(A+'name')!='android.permission.START_ACTIVITIES_FROM_BACKGROUND' and (node.tag,node.get(A+'name')) not in existing:
  mr.insert(0,node);existing.add((node.tag,node.get(A+'name')))
for node in ua:
 if node.tag not in ('service','activity','provider','meta-data'):continue
 if node.get(A+'name')=='com.vivid.spotify.byd.RestoreStateService':continue
 if node.get(A+'name')=='local.spotify.close.TaskGuardService':node.set(A+'name','local.spotify.close.TaskGuardService')
 for el in node.iter():
  for k,v in list(el.attrib.items()):
   if v.startswith('@'):
    mt=re.match(r'@([\w]+)/(.+)',v)
    if mt and (mt[1],mt[2]) in resnames:v='@'+resnames[(mt[1],mt[2])]
   else:v=transform(v)
   el.set(k,v)
 if node.tag!='meta-data':
  node.set(A+'process',':bydui')
  if node.tag=='activity' and not node.get(A+'theme'):node.set(A+'theme','@'+resnames[('style','Theme_VIVID_MUSIC_Base')])
 if node.tag=='provider':node.attrib.pop(A+'multiprocess',None)
 app.append(node)
# Compile placeholders against the backend package, then rewrite their binary
# manifest references to the second resource package during final packaging.
bp=E.parse(prev/'backend/res/values/public.xml');br=bp.getroot();maxids={};examples={}
for n in br:
 typ=n.get('type');maxids[typ]=max(maxids.get(typ,0),int(n.get('id'),16));examples.setdefault(typ,n.get('name'))
vals=E.Element('resources');patches={};refs={}
for n in app.iter():
 for k,v in list(n.attrib.items()):
  if not v.startswith('@0x7e'):continue
  target=int(v[1:],16);typ=next(t for (t,name),ident in resnames.items() if int(ident,16)==target)
  name='unified_ref_'+v[3:]
  if target in refs:
   n.set(k,refs[target]);continue
  refs[target]='@'+typ+'/'+name;maxids[typ]+=1;srcid=maxids[typ]
  E.SubElement(vals,'item',{'type':typ,'name':name}).text='@'+typ+'/'+examples[typ]
  E.SubElement(br,'public',{'type':typ,'name':name,'id':hex(srcid)})
  patches[hex(srcid)]=hex(target);n.set(k,'@'+typ+'/'+name)
E.ElementTree(vals).write(out/'res/values/unified_refs.xml',encoding='utf-8',xml_declaration=True)
bp.write(out/'res/values/public.xml',encoding='utf-8',xml_declaration=True)
(base/'manifest-refs.json').write_text(json.dumps(patches,indent=2))
m.write(out/'AndroidManifest.xml',encoding='utf-8',xml_declaration=True)
y=(out/'apktool.yml').read_text();y=re.sub(r'targetSdkVersion: \d+','targetSdkVersion: 33',y);(out/'apktool.yml').write_text(y)
print('Relocated',len(files),'UI classes;',len(ids),'resource IDs')

exec((base/"patch_headunit.py").read_text(),{"__file__":str(base/"patch_headunit.py")})

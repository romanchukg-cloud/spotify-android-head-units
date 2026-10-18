from pathlib import Path
import zipfile,json
from build_config import BUILD
b=Path(__file__).resolve().parent;p=b.parent/'spotify55-adaptation'
CHANGED=('classes.dex','classes2.dex','AndroidManifest.xml','stamp-cert-sha256')
ADDED={'classes2.dex','assets/hu_fonts/Figtree-wght.ttf','assets/hu_fonts/OFL.txt'}
with zipfile.ZipFile(p/'SpotifySDK-5.5.0-BYD-test.apk') as old,zipfile.ZipFile(b/BUILD['apk_name']) as new:
 names=set(new.namelist())
 preserved=[n for n in old.namelist() if not n.startswith('META-INF/') and n not in CHANGED]
 # Every backend payload, the resource table included, is the SDK container's own.
 assert all(n in names and old.read(n)==new.read(n) for n in preserved)
 assert all(n in names and old.read(n)==new.read(n) for n in old.namelist() if n.startswith('META-INF/services/'))
 extra={n for n in names if not n.startswith('META-INF/')}-set(old.namelist())
 assert extra==ADDED-set(old.namelist()),sorted(extra)
 # No BYD-derived interface: no third DEX, no relocated resources.
 assert 'classes3.dex' not in names and not any(n.startswith('ui/') for n in names)
report={'backend_payloads_preserved':len(preserved),'backend_resource_table_unchanged':True,'backend_services_metadata_preserved':True,'added':sorted(ADDED),'byd_interface':False}
(b/'structural-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))

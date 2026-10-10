#!/usr/bin/env python3
"""Prepare ignored local inputs; never downloads upstream proprietary binaries."""
from pathlib import Path
import argparse,hashlib,shutil,subprocess
p=argparse.ArgumentParser();p.add_argument('--sdk-apk',type=Path,required=True);p.add_argument('--ui-apk',type=Path,required=True);a=p.parse_args()
base=Path(__file__).resolve().parent;inputs=base.parent/'spotify55-adaptation'
expected={'sdk':'6527109d69dbbee4c9bef177879dbb9cebedc806dd6b07cb0e89a223125247d2','ui':'087a78a67501543bceac2700cbe3e2c3b9ea6e84037fd898fe8941b1f7b72ace'}
for kind,path,name in [('sdk',a.sdk_apk,'SpotifySDK-5.5.0-BYD-test.apk'),('ui',a.ui_apk,'Spotify-BYD-for-5.5-test.apk')]:
 if hashlib.sha256(path.read_bytes()).hexdigest()!=expected[kind]:raise SystemExit('Unexpected '+kind+' input SHA-256; use the prepared 5.5.0 inputs described in README.')
 inputs.mkdir(exist_ok=True);target=inputs/name
 if path.resolve()!=target.resolve():shutil.copy2(path,target)
 dest=inputs/('backend' if kind=='sdk' else 'ui')
 if dest.exists():raise SystemExit(f'{dest} already exists; preserve or remove it explicitly before preparing new inputs.')
 subprocess.run(['apktool','d',str(target),'-o',str(dest)],check=True)
shutil.copytree(inputs/'ui',base/'ui',ignore=shutil.ignore_patterns('build','original'))
print('Inputs prepared. Signing keys are not included.')

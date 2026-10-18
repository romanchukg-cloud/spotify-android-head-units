#!/usr/bin/env python3
"""Prepare ignored local inputs; never downloads upstream proprietary binaries."""
from pathlib import Path
import argparse,hashlib,shutil,subprocess
p=argparse.ArgumentParser();p.add_argument('--sdk-apk',type=Path,required=True);a=p.parse_args()
base=Path(__file__).resolve().parent;inputs=base.parent/'spotify55-adaptation'
# Spotify Automotive 5.5.0 (the music backend) in its prepared container.
expected='6527109d69dbbee4c9bef177879dbb9cebedc806dd6b07cb0e89a223125247d2';name='SpotifySDK-5.5.0-BYD-test.apk'
if hashlib.sha256(a.sdk_apk.read_bytes()).hexdigest()!=expected:raise SystemExit('Unexpected SDK input SHA-256; use the prepared 5.5.0 input described in README.')
inputs.mkdir(exist_ok=True);target=inputs/name
if a.sdk_apk.resolve()!=target.resolve():shutil.copy2(a.sdk_apk,target)
dest=inputs/'backend'
if dest.exists():raise SystemExit(f'{dest} already exists; preserve or remove it explicitly before preparing new inputs.')
subprocess.run(['apktool','d',str(target),'-o',str(dest)],check=True)
print('Inputs prepared. Signing keys are not included.')

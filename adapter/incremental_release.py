#!/usr/bin/env python3
"""Incremental release: the previous published APK plus the current adapter.

build.sh is the reference build and needs the prepared input APKs. When only the
previous release is at hand, this rebuilds that release's backend DEX with the
smali patches added since it, recompiles the helper DEX from src/ and sets the new
versionCode. Resources, the UI DEX and native libraries stay byte-identical.
Signing follows separately (zipalign + apksigner); see .github/workflows/release.yml.

  python3 adapter/incremental_release.py --base Spotify-5.5.0-HeadUnit-test.7.apk \
    --base-info reports/headunit-test.7/release.json --since "Sign-in stability" \
    --tools jadx-1.5.1-all.jar --android-jar android.jar --out unsigned.apk

--tools is any jar with smali/baksmali 3 and D8 (jadx-all bundles both).
"""
import argparse, hashlib, json, re, shutil, struct, subprocess, sys, zipfile
from pathlib import Path
ADAPTER = Path(__file__).resolve().parent
sys.path.insert(0, str(ADAPTER.parent / 'tools'))
from apk_compat_audit import Dex  # noqa: E402
import build_config  # noqa: E402

SIGNATURE = re.compile(r'META-INF/[^/]+\.(SF|RSA|DSA|EC|MF)$')


def run(*cmd):
    subprocess.run([str(c) for c in cmd], check=True)


def patch_section(since):
    """The patch_headunit.py code from the '# <since>' comment to the end of the file."""
    text = (ADAPTER / 'patch_headunit.py').read_text()
    match = re.search(r'^# ' + re.escape(since) + r'\b', text, re.M)
    if not match:
        raise SystemExit('No patch section "# %s" in patch_headunit.py' % since)
    return text[match.start():]


def set_version_code(axml, code):
    """Rewrites the manifest element's versionCode in binary XML; returns (bytes, old)."""
    data = bytearray(axml)
    o, strings, resmap = 8, [], []
    while o < len(data):
        typ, header, size = struct.unpack_from('<HHI', data, o)
        if typ == 0x0001:
            count, flags, start = struct.unpack_from('<I', data, o + 8)[0], struct.unpack_from('<I', data, o + 16)[0], struct.unpack_from('<I', data, o + 20)[0]
            offsets = struct.unpack_from('<%dI' % count, data, o + header)
            for so in offsets:
                p = o + start + so
                if flags & 0x100:
                    p += 2 if data[p] & 0x80 else 1
                    n = data[p]; p += 1
                    if n & 0x80:
                        n = ((n & 0x7f) << 8) | data[p]; p += 1
                    strings.append(bytes(data[p:p + n]).decode('utf-8', 'replace'))
                else:
                    n = struct.unpack_from('<H', data, p)[0]
                    strings.append(bytes(data[p + 2:p + 2 + 2 * n]).decode('utf-16le', 'replace'))
        elif typ == 0x0180:
            resmap = list(struct.unpack_from('<%dI' % ((size - header) // 4), data, o + header))
        elif typ == 0x0102:
            if strings[struct.unpack_from('<I', data, o + 20)[0]] != 'manifest':
                raise SystemExit('First element is not <manifest>')
            start, attr_size, count = struct.unpack_from('<HHH', data, o + 24)
            for i in range(count):
                a = o + 16 + start + i * attr_size
                name = struct.unpack_from('<I', data, a + 4)[0]
                if strings[name] == 'versionCode' or (name < len(resmap) and resmap[name] == 0x0101021b):
                    if data[a + 15] not in (0x10, 0x11):
                        raise SystemExit('versionCode is not an integer attribute')
                    old = struct.unpack_from('<I', data, a + 16)[0]
                    struct.pack_into('<I', data, a + 16, code)
                    return bytes(data), old
            raise SystemExit('No versionCode on <manifest>')
        o += size
    raise SystemExit('No <manifest> element')


def check_references(dexes, helper):
    """Every call from the SDK/UI DEX into local.spotify.* must exist in the new helper DEX."""
    declared = helper.declared
    missing = [(d.name, m) for d in dexes for m in set(d.methods)
               if m[0].startswith('Llocal/spotify/') and m[0] not in d.defined and m not in declared]
    if missing:
        raise SystemExit('Helper DEX lacks methods called by the APK: %s' % missing[:10])
    return sum(1 for d in dexes for m in set(d.methods) if m[0].startswith('Llocal/spotify/') and m[0] not in d.defined)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--base', required=True, type=Path)
    ap.add_argument('--base-info', required=True, type=Path)
    ap.add_argument('--since', required=True, help='patch_headunit.py section heading added after the base release')
    ap.add_argument('--tools', required=True, type=Path)
    ap.add_argument('--android-jar', required=True, type=Path)
    ap.add_argument('--work', type=Path, default=ADAPTER / 'incremental')
    ap.add_argument('--out', required=True, type=Path)
    a = ap.parse_args()
    info = json.loads(a.base_info.read_text())
    digest = hashlib.sha256(a.base.read_bytes()).hexdigest()
    if digest != info['sha256']:
        raise SystemExit('Base APK SHA-256 %s does not match %s' % (digest, a.base_info))
    if build_config.BUILD['version_code'] <= info['versionCode']:
        raise SystemExit('build-info.json versionCode must be above the base release')
    if a.work.exists():
        shutil.rmtree(a.work)
    stable, out = a.work / 'stable', a.work / 'out'
    with zipfile.ZipFile(a.base) as z:
        z.extract('classes.dex', a.work)
        ui = Dex('classes3.dex', z.read('classes3.dex'))
        manifest = z.read('AndroidManifest.xml')

    java = ['java', '-cp', a.tools]
    run(*java, 'com.android.tools.smali.baksmali.Main', 'd', '--use-locals', '--sequential-labels',
        a.work / 'classes.dex', '-o', stable / 'smali')
    shutil.copytree(stable / 'smali', out / 'smali')
    exec(compile(patch_section(a.since), 'patch_headunit.py', 'exec'), {'re': re, 'out': out, 'stable': stable})
    run(*java, 'com.android.tools.smali.smali.Main', 'a', '--api', '28', out / 'smali', '-o', a.work / 'backend.dex')

    build_config.generate()
    sources = sorted(str(p) for p in (ADAPTER / 'src/local/spotify').glob('*/*.java'))
    sources += sorted(str(p) for p in (ADAPTER / 'generated/local/spotify/unified').glob('*.java'))
    classes = a.work / 'classes'
    run('javac', '-nowarn', '-source', '8', '-target', '8', '-cp', a.android_jar, '-d', classes, *sources)
    (a.work / 'helper').mkdir()
    run(*java, 'com.android.tools.r8.D8', '--min-api', '28', '--lib', a.android_jar, '--output', a.work / 'helper',
        *sorted(str(p) for p in classes.rglob('*.class')))

    backend = (a.work / 'backend.dex').read_bytes()
    helper_bytes = (a.work / 'helper/classes.dex').read_bytes()
    refs = check_references([Dex('classes.dex', backend), ui], Dex('classes2.dex', helper_bytes))
    manifest, old = set_version_code(manifest, build_config.BUILD['version_code'])
    if old != info['versionCode']:
        raise SystemExit('Base manifest versionCode %d differs from %s' % (old, a.base_info))

    replace = {'classes.dex': backend, 'classes2.dex': helper_bytes, 'AndroidManifest.xml': manifest}
    with zipfile.ZipFile(a.base) as zin, zipfile.ZipFile(a.out, 'w') as zout:
        for entry in zin.infolist():
            if SIGNATURE.match(entry.filename):
                continue
            copy = zipfile.ZipInfo(entry.filename, date_time=entry.date_time)
            copy.compress_type, copy.external_attr = entry.compress_type, entry.external_attr
            zout.writestr(copy, replace.get(entry.filename, zin.read(entry.filename)))
    summary = {'base': info['file'], 'base_sha256': digest, 'version_code': build_config.BUILD['version_code'],
               'label': build_config.BUILD['label'], 'patch_section': a.since, 'helper_references': refs,
               'certificate_sha256': info['certificateSha256']}
    (a.work / 'incremental.json').write_text(json.dumps(summary, indent=2) + '\n')
    print(json.dumps(summary, indent=2))


if __name__ == '__main__':
    main()

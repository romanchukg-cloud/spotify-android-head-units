#!/usr/bin/env python3
"""Incremental release: the previous published APK plus the current adapter.

build.sh is the reference build and needs the prepared input APKs. When only the
previous release is at hand, this rebuilds that release's backend DEX with the
smali patches added since it, recompiles the helper DEX from src/, adds the section's
MANIFEST_ADDITIONS to the binary manifest and sets the new versionCode. Resources, the UI
DEX and native libraries stay byte-identical.
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


ANDROID = 'http://schemas.android.com/apk/res/android'


def xml_chunks(data):
    if struct.unpack_from('<H', data)[0] != 3 or struct.unpack_from('<I', data, 4)[0] != len(data):
        raise SystemExit('Expected complete binary Android XML')
    offset = struct.unpack_from('<H', data, 2)[0]
    while offset < len(data):
        kind, header, size = struct.unpack_from('<HHI', data, offset)
        if size < header or size < 8 or offset + size > len(data):
            raise SystemExit('Invalid XML chunk')
        yield kind, data[offset:offset + size]
        offset += size


def pool_strings(pool):
    count, styles, flags, start, style_start = struct.unpack_from('<5I', pool, 8)
    header, values = struct.unpack_from('<H', pool, 2)[0], []
    for i in range(count):
        o = start + struct.unpack_from('<I', pool, header + i * 4)[0]
        if flags & 0x100:
            o += 2 if pool[o] & 0x80 else 1
            n = pool[o]; o += 1
            if n & 0x80:
                n = ((n & 0x7f) << 8) | pool[o]; o += 1
            values.append(pool[o:o + n].decode('utf-8'))
        else:
            n = struct.unpack_from('<H', pool, o)[0]
            if n & 0x8000:
                n = ((n & 0x7fff) << 16) | struct.unpack_from('<H', pool, o + 2)[0]; o += 2
            values.append(pool[o + 2:o + 2 + 2 * n].decode('utf-16le'))
    return values


def append_string(pool, value):
    """Appends an ASCII string after the existing ones, so their indexes and the resource map stay valid."""
    count, styles, flags, start, style_start = struct.unpack_from('<5I', pool, 8)
    header = struct.unpack_from('<H', pool, 2)[0]
    if len(value) >= 0x7f or not value.isascii():
        raise SystemExit('Unsupported manifest string %r' % value)
    old = pool[start:style_start or len(pool)]
    encoded = bytes([len(value)]) * 2 + value.encode() + b'\0' if flags & 0x100 else struct.pack('<H', len(value)) + value.encode('utf-16le') + b'\0\0'
    content = old + encoded
    content += b'\0' * (-len(content) % 4)
    result = bytearray(pool[:header + count * 4] + struct.pack('<I', len(old)) + pool[header + count * 4:start] + content + (pool[style_start:] if style_start else b''))
    struct.pack_into('<I', result, 4, len(result))
    struct.pack_into('<5I', result, 8, count + 1, styles, flags & ~1, start + 4, start + 4 + len(content) if style_start else 0)
    return bytes(result)


def add_declarations(axml, additions):
    """Adds <uses-permission> and non-exported <receiver> elements (MANIFEST_ADDITIONS) to a binary manifest."""
    parts = list(xml_chunks(axml))
    pool = next(raw for kind, raw in parts if kind == 1)
    names = pool_strings(pool)
    original = names[:]
    for tag, name in additions:
        if tag not in ('uses-permission', 'receiver'):
            raise SystemExit('Unsupported manifest addition %s' % tag)
        for value in (tag, name):
            if value not in names:
                pool = append_string(pool, value)
                names.append(value)
    index = {value: names.index(value) for value in set(names)}
    resources = next(raw for kind, raw in parts if kind == 0x180)
    for attr, ident in (('name', 0x01010003), ('exported', 0x01010010)):
        if attr not in index or struct.unpack_from('<I', resources, 8 + index[attr] * 4)[0] != ident or ANDROID not in index:
            raise SystemExit('Manifest lacks the android:%s attribute' % attr)

    def attribute(attr, value, kind):
        return struct.pack('<IIIHBBI', index[ANDROID], index[attr], value if kind == 3 else 0xffffffff, 8, 0, kind, value)

    def element(tag, attrs):  # Attributes in ascending resource ID order, as aapt writes them.
        start = struct.pack('<HHI2I2I6H', 0x102, 16, 36 + 20 * len(attrs), 0, 0xffffffff, 0xffffffff, index[tag], 20, 20, len(attrs), 0, 0, 0)
        return start + b''.join(attrs) + struct.pack('<HHI4I', 0x103, 16, 24, 0, 0xffffffff, 0xffffffff, index[tag])

    present, stack = set(), []
    for kind, raw in parts:
        if kind == 0x102:
            tag = names[struct.unpack_from('<I', raw, 20)[0]]
            start, size, count = struct.unpack_from('<3H', raw, 24)
            for i in range(count):
                a = 16 + start + i * size
                if names[struct.unpack_from('<I', raw, a + 4)[0]] == 'name' and raw[a + 15] == 3:
                    present.add((tag, names[struct.unpack_from('<I', raw, a + 16)[0]], tuple(stack)))
            stack.append(tag)
        elif kind == 0x103:
            stack.pop()
    permissions = b''.join(element(tag, [attribute('name', index[name], 3)]) for tag, name in additions
                           if tag == 'uses-permission' and (tag, name, ('manifest',)) not in present)
    receivers = b''.join(element(tag, [attribute('name', index[name], 3), attribute('exported', 0, 0x12)]) for tag, name in additions
                         if tag == 'receiver' and (tag, name, ('manifest', 'application')) not in present)
    output, stack = bytearray(axml[:8]), []
    for kind, raw in parts:
        if kind == 1:
            raw = pool
        elif kind == 0x102:
            tag = names[struct.unpack_from('<I', raw, 20)[0]]
            if tag == 'application' and stack == ['manifest']:
                output += permissions
            stack.append(tag)
        elif kind == 0x103:
            if stack == ['manifest', 'application']:
                output += receivers
            stack.pop()
        output += raw
    struct.pack_into('<I', output, 4, len(output))
    if pool_strings(pool)[:len(original)] != original:
        raise SystemExit('Manifest string indexes changed')
    return bytes(output)


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
        # Up to test.12 the BYD-derived interface was classes3.dex.
        ui = [Dex('classes3.dex', z.read('classes3.dex'))] if 'classes3.dex' in z.namelist() else []
        manifest = z.read('AndroidManifest.xml')

    java = ['java', '-cp', a.tools]
    run(*java, 'com.android.tools.smali.baksmali.Main', 'd', '--use-locals', '--sequential-labels',
        a.work / 'classes.dex', '-o', stable / 'smali')
    shutil.copytree(stable / 'smali', out / 'smali')
    section = {'re': re, 'out': out, 'stable': stable}
    exec(compile(patch_section(a.since), 'patch_headunit.py', 'exec'), section)
    additions = section.get('MANIFEST_ADDITIONS', [])
    run(*java, 'com.android.tools.smali.smali.Main', 'a', '--api', '28', out / 'smali', '-o', a.work / 'backend.dex')

    build_config.generate()
    sources = sorted(str(p) for p in (ADAPTER / 'src/local/spotify').glob('*/*.java'))
    sources += sorted(str(p) for p in (ADAPTER / 'src/com').rglob('*.java'))
    sources += sorted(str(p) for p in (ADAPTER / 'generated/local/spotify/unified').glob('*.java'))
    classes, stubs = a.work / 'classes', a.work / 'stubs'
    # The backend's own support-library media classes: compile-time only, never in the helper DEX.
    run('javac', '-nowarn', '-source', '8', '-target', '8', '-cp', a.android_jar, '-d', stubs,
        *sorted(str(p) for p in (ADAPTER / 'stubs').rglob('*.java')))
    run('javac', '-nowarn', '-source', '8', '-target', '8', '-cp', '%s:%s' % (a.android_jar, stubs), '-d', classes, *sources)
    (a.work / 'helper').mkdir()
    run(*java, 'com.android.tools.r8.D8', '--min-api', '28', '--lib', a.android_jar, '--classpath', stubs,
        '--output', a.work / 'helper', *sorted(str(p) for p in classes.rglob('*.class')))

    changed = sorted(str(f.relative_to(out / 'smali')) for f in (out / 'smali').rglob('*.smali')
                     if f.read_bytes() != (stable / 'smali' / f.relative_to(out / 'smali')).read_bytes())
    # A section without smali changes keeps the published backend DEX byte-identical.
    backend = (a.work / 'backend.dex').read_bytes() if changed else (a.work / 'classes.dex').read_bytes()
    helper_bytes = (a.work / 'helper/classes.dex').read_bytes()
    helper = Dex('classes2.dex', helper_bytes)
    refs = check_references([Dex('classes.dex', backend), *ui], helper)
    missing = [name for tag, name in additions if tag == 'receiver' and 'L' + name.replace('.', '/') + ';' not in helper.defined]
    if missing:
        raise SystemExit('Manifest receivers missing from the helper DEX: %s' % missing)
    manifest = add_declarations(manifest, additions)
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
               'manifest_additions': additions, 'changed_smali': changed,
               'certificate_sha256': info['certificateSha256']}
    (a.work / 'incremental.json').write_text(json.dumps(summary, indent=2) + '\n')
    print(json.dumps(summary, indent=2))


if __name__ == '__main__':
    main()

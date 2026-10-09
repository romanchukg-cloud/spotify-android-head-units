#!/usr/bin/env python3
"""Head-unit compatibility audit for an APK; standard library only.

Checks what a release must satisfy to run on generic Android head units:
install/minSdk, process routing, framework API levels used by the dex code,
native libraries, resources, translations, signing and leftover vendor (BYD)
coupling. Prints a Markdown report; exits 1 when any check fails.

    python3 tools/apk_audit.py app.apk                 # target Android 8.0 (API 26)
    python3 tools/apk_audit.py app.apk --min-api 28    # current Android 9 baseline
    python3 tools/apk_audit.py app.apk --no-api        # skip the framework download

The API check downloads Robolectric's android-all framework jars (about 100 MB
per Android version) from Maven Central once and caches them.
"""
import argparse
import collections
import gzip
import io
import json
import os
import re
import struct
import sys
import time
import urllib.error
import urllib.request
import zipfile

MAVEN = 'https://repo1.maven.org/maven2/org/robolectric/android-all/'
# Android version prefix of the android-all artifact for each API level.
ANDROID_ALL = {26: r'8\.0\.0_r4-', 27: r'8\.1\.0-', 28: r'9-', 29: r'10-', 30: r'11-', 31: r'12-',
               32: r'12\.1-', 33: r'13-', 34: r'14-', 35: r'15-', 36: r'16-'}
PLATFORM = ('android/', 'java/', 'javax/', 'dalvik/', 'org/json/', 'org/w3c/', 'org/xml/', 'org/apache/http/')
# android-all leaves out java.lang.Object, so overrides of its methods would look new.
OBJECT_METHODS = {'equals(Ljava/lang/Object;)Z', 'hashCode()I', 'toString()Ljava/lang/String;',
                  'clone()Ljava/lang/Object;', 'finalize()V', 'getClass()Ljava/lang/Class;'}
LIBC_API = {'LIBC': 0, 'LIBC_N': 24, 'LIBC_O': 26, 'LIBC_P': 28, 'LIBC_Q': 29, 'LIBC_R': 30,
            'LIBC_S': 31, 'LIBC_T': 33, 'LIBC_U': 34, 'LIBC_V': 35, 'LIBC_W': 36}
VENDOR_CLASS = re.compile(r'^Landroid/hardware/bydauto/|^Lcom/byd/')
VENDOR_STRING = re.compile(r'(^|[^a-z])(byd|bydauto|vivid\.test)[._]|sys\.byd\.|com\.vivid\.music\.byd|BYDAUTO_', re.I)
# String-resource prefixes that belong to AndroidX/Material rather than the app.
LIBRARY_STRINGS = ('abc_', 'mtrl_', 'material_', 'm3_', 'androidx_', 'bottomsheet_', 'bottom_sheet', 'character_counter',
                   'clear_text', 'error_', 'exposed_', 'fab_', 'hide_', 'icon_content', 'item_view', 'password_', 'path_',
                   'search_menu', 'status_bar', 'appbar_', 'call_notification', 'chip_', 'side_sheet', 'searchbar_',
                   'searchview_', 'nav_app_bar', 'not_set', 'clock_', 'common_google', 'fallback_menu', 'copy_toast')


class Report:
    def __init__(self):
        self.rows, self.sections = [], []

    def check(self, status, name, detail):
        self.rows.append((status, name, detail))

    def section(self, title, lines):
        self.sections.append((title, lines))

    def render(self):
        out = ['| Status | Check | Result |', '|---|---|---|']
        out += ['| %s | %s | %s |' % r for r in self.rows]
        for title, lines in self.sections:
            out += ['', '## ' + title, ''] + lines
        return '\n'.join(out)

    def failed(self):
        return any(r[0] == 'FAIL' for r in self.rows)


# ---------------------------------------------------------------- binary XML

def string_pool(b, o):
    _, hs, _, count, _, flags, sstart, _ = struct.unpack_from('<HHIIIIII', b, o)
    offs = struct.unpack_from('<%dI' % count, b, o + hs)
    out = []
    for so in offs:
        p = o + sstart + so
        if flags & 0x100:
            p += 2 if b[p] & 0x80 else 1
            n = b[p]
            p += 1
            if n & 0x80:
                n = ((n & 0x7f) << 8) | b[p]
                p += 1
            out.append(b[p:p + n].decode('utf-8', 'replace'))
        else:
            n = struct.unpack_from('<H', b, p)[0]
            p += 2
            if n & 0x8000:
                n = ((n & 0x7fff) << 16) | struct.unpack_from('<H', b, p)[0]
                p += 2
            out.append(b[p:p + 2 * n].decode('utf-16le', 'replace'))
    return out


def parse_axml(b):
    """Returns the manifest as nested dicts: {'tag', 'attrs', 'children'}."""
    o, strings, root, stack = 8, [], None, []
    while o < len(b):
        t, h, s = struct.unpack_from('<HHI', b, o)
        if t == 0x0001:
            strings = string_pool(b, o)
        elif t == 0x0102:
            name = strings[struct.unpack_from('<I', b, o + 20)[0]]
            astart, asize, count = struct.unpack_from('<HHH', b, o + 24)
            attrs = {}
            for i in range(count):
                a = o + 16 + astart + i * asize
                _, an, raw, _, _, dt, data = struct.unpack_from('<IIIHBBI', b, a)
                key = strings[an]
                if dt == 0x03:
                    val = strings[data]
                elif dt == 0x12:
                    val = data != 0
                elif dt in (0x10, 0x11):
                    val = data
                elif dt == 0x01:
                    val = '@%08x' % data
                else:
                    val = strings[raw] if raw != 0xffffffff else data
                attrs[key] = val
            node = {'tag': name, 'attrs': attrs, 'children': []}
            (stack[-1]['children'] if stack else []).append(node)
            root = root or node
            stack.append(node)
        elif t == 0x0103:
            stack.pop()
        o += s
    return root


def walk(node):
    yield node
    for c in node['children']:
        yield from walk(c)


# ---------------------------------------------------------------------- dex

def uleb(b, o):
    r = s = 0
    while True:
        x = b[o]
        o += 1
        r |= (x & 0x7f) << s
        if x < 0x80:
            return r, o
        s += 7


def _widths():
    w = [1] * 256
    spans = [(0x02, 0x02, 2), (0x03, 0x03, 3), (0x05, 0x05, 2), (0x06, 0x06, 3), (0x08, 0x08, 2), (0x09, 0x09, 3),
             (0x13, 0x13, 2), (0x14, 0x14, 3), (0x15, 0x16, 2), (0x17, 0x17, 3), (0x18, 0x18, 5), (0x19, 0x1a, 2),
             (0x1b, 0x1b, 3), (0x1c, 0x1c, 2), (0x1f, 0x20, 2), (0x22, 0x23, 2), (0x24, 0x26, 3), (0x29, 0x29, 2),
             (0x2a, 0x2c, 3), (0x2d, 0x3d, 2), (0x44, 0x6d, 2), (0x6e, 0x72, 3), (0x74, 0x78, 3), (0x90, 0xaf, 2),
             (0xd0, 0xe2, 2), (0xfa, 0xfb, 4), (0xfc, 0xfd, 3), (0xfe, 0xff, 2)]
    for lo, hi, n in spans:
        for i in range(lo, hi + 1):
            w[i] = n
    return w


WIDTH = _widths()


class Dex:
    def __init__(self, name, b):
        self.name = name
        (sz_s, off_s, sz_t, off_t, sz_p, off_p, sz_f, off_f,
         sz_m, off_m, sz_c, off_c) = struct.unpack_from('<12I', b, 0x38)

        def mutf8(o):
            _, o = uleb(b, o)
            return b[o:b.index(0, o)].decode('utf-8', 'replace')
        self.strings = [mutf8(struct.unpack_from('<I', b, off_s + 4 * i)[0]) for i in range(sz_s)]
        types = [self.strings[struct.unpack_from('<I', b, off_t + 4 * i)[0]] for i in range(sz_t)]
        protos = []
        for i in range(sz_p):
            _, ret, poff = struct.unpack_from('<III', b, off_p + 12 * i)
            params = ''
            if poff:
                n = struct.unpack_from('<I', b, poff)[0]
                params = ''.join(types[x] for x in struct.unpack_from('<%dH' % n, b, poff + 4))
            protos.append('(%s)%s' % (params, types[ret]))
        self.fields = []
        for i in range(sz_f):
            c, t, n = struct.unpack_from('<HHI', b, off_f + 8 * i)
            self.fields.append((types[c], self.strings[n], types[t]))
        self.methods = []
        for i in range(sz_m):
            c, p, n = struct.unpack_from('<HHI', b, off_m + 8 * i)
            self.methods.append((types[c], self.strings[n], protos[p]))
        self.defined, self.declared = set(), set()
        self.calls = collections.defaultdict(set)    # ('M'|'F', ref tuple) -> callers
        self.consts = collections.defaultdict(set)   # string constant -> callers
        for i in range(sz_c):
            cls_idx, _, _, _, _, _, cdata, _ = struct.unpack_from('<8I', b, off_c + 32 * i)
            self.defined.add(types[cls_idx])
            if cdata:
                self._class_data(b, cdata)

    def _class_data(self, b, o):
        nsf, o = uleb(b, o)
        nif, o = uleb(b, o)
        ndm, o = uleb(b, o)
        nvm, o = uleb(b, o)
        for _ in range(nsf + nif):
            _, o = uleb(b, o)
            _, o = uleb(b, o)
        for count in (ndm, nvm):
            idx = 0
            for _ in range(count):
                d, o = uleb(b, o)
                idx += d
                _, o = uleb(b, o)
                code, o = uleb(b, o)
                self.declared.add(self.methods[idx])
                if code:
                    self._code(b, code, self.methods[idx])

    def _code(self, b, code, caller):
        n = struct.unpack_from('<I', b, code + 12)[0]
        base, pc = code + 16, 0
        who = '%s->%s' % (caller[0][1:-1].replace('/', '.'), caller[1])
        while pc < n:
            unit = struct.unpack_from('<H', b, base + 2 * pc)[0]
            op = unit & 0xff
            if op == 0 and unit:
                size = struct.unpack_from('<H', b, base + 2 * pc + 2)[0]
                if unit == 0x0100:
                    pc += size * 2 + 4
                elif unit == 0x0200:
                    pc += size * 4 + 2
                elif unit == 0x0300:
                    count = struct.unpack_from('<I', b, base + 2 * pc + 4)[0]
                    pc += (size * count + 1) // 2 + 4
                else:
                    pc += 1
                continue
            arg = struct.unpack_from('<H', b, base + 2 * pc + 2)[0] if pc + 1 < n else 0
            if 0x6e <= op <= 0x72 or 0x74 <= op <= 0x78:
                self.calls[('M', self.methods[arg])].add(who)
            elif 0x52 <= op <= 0x6d:
                self.calls[('F', self.fields[arg])].add(who)
            elif op == 0x1a:
                self.consts[self.strings[arg]].add(who)
            elif op == 0x1b:
                self.consts[self.strings[struct.unpack_from('<I', b, base + 2 * pc + 2)[0]]].add(who)
            pc += WIDTH[op]


# ------------------------------------------------------ framework (android-all)

def class_file(b):
    """Returns (name, super, interfaces, fields, methods) from a .class file."""
    count = struct.unpack_from('>H', b, 8)[0]
    cp, o, i = [None] * count, 10, 1
    while i < count:
        tag = b[o]
        if tag == 1:
            n = struct.unpack_from('>H', b, o + 1)[0]
            cp[i] = b[o + 3:o + 3 + n].decode('utf-8', 'replace')
            o += 3 + n
        elif tag in (7, 8, 16, 19, 20):
            cp[i] = ('ref', struct.unpack_from('>H', b, o + 1)[0])
            o += 3
        elif tag == 15:
            o += 4
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            o += 5
        elif tag in (5, 6):
            o += 9
            i += 1
        else:
            raise ValueError('constant pool tag %d' % tag)
        i += 1

    def cls(k):
        return cp[cp[k][1]] if k else None
    _, this, sup, nif = struct.unpack_from('>HHHH', b, o)
    o += 8
    ifaces = [cls(x) for x in struct.unpack_from('>%dH' % nif, b, o)]
    o += 2 * nif
    members = []
    for _ in range(2):
        n = struct.unpack_from('>H', b, o)[0]
        o += 2
        found = []
        for _ in range(n):
            _, name, desc, na = struct.unpack_from('>HHHH', b, o)
            o += 8
            for _ in range(na):
                o += 6 + struct.unpack_from('>I', b, o + 2)[0]
            found.append(cp[name] + ('' if not members else cp[desc]))
        members.append(found)
    return cls(this), cls(sup), ifaces, members[0], members[1]


def http(url):
    """Opens a URL, retrying the rate limiting Maven Central applies to bursts."""
    request = urllib.request.Request(url, headers={'User-Agent': 'apk-audit/1.0 (+head-unit compatibility check)'})
    for attempt in range(5):
        try:
            return urllib.request.urlopen(request, timeout=60)
        except urllib.error.HTTPError as e:
            if e.code not in (429, 500, 502, 503, 504) or attempt == 4:
                raise
            time.sleep(2 ** (attempt + 1))


def fetch(url, dest):
    tmp = dest + '.part'
    with http(url) as r, open(tmp, 'wb') as f:
        while True:
            chunk = r.read(1 << 20)
            if not chunk:
                break
            f.write(chunk)
    os.replace(tmp, dest)


def framework(api, cache):
    """Class model of the Android framework at an API level, cached as JSON."""
    path = os.path.join(cache, 'framework-%d.json.gz' % api)
    if not os.path.exists(path):
        build_framework(api, cache, path)
    with gzip.open(path, 'rt') as f:
        data = json.load(f)
    for entry in data['classes'].values():
        entry[2], entry[3] = set(entry[2]), set(entry[3])
    return data


def build_framework(api, cache, path):
    os.makedirs(cache, exist_ok=True)
    pattern = re.compile('^android-all-(' + ANDROID_ALL[api] + r'robolectric.*)\.jar$')
    local = sorted(m.group(1) for m in map(pattern.match, os.listdir(cache)) if m)
    if local:
        version = local[-1]
    else:
        with http(MAVEN + 'maven-metadata.xml') as r:
            versions = re.findall(r'<version>([^<]+)</version>', r.read().decode())
        pick = [v for v in versions if re.match('^' + ANDROID_ALL[api] + r'robolectric', v)]
        if not pick:
            raise SystemExit('No android-all artifact for API %d' % api)
        version = pick[-1]
    jar = os.path.join(cache, 'android-all-%s.jar' % version)
    if not os.path.exists(jar):
        print('Downloading android-all %s (API %d)...' % (version, api), file=sys.stderr)
        fetch('%s%s/android-all-%s.jar' % (MAVEN, version, version), jar)
    model, rids = {}, {}
    with zipfile.ZipFile(jar) as z:
        for entry in z.namelist():
            if not entry.endswith('.class') or not entry.startswith(PLATFORM):
                continue
            name, sup, ifaces, fields, methods = class_file(z.read(entry))
            model[name] = [sup, ifaces, fields, methods]
        for entry in z.namelist():
            if re.match(r'android/R\$\w+\.class$', entry):
                rids.update(r_ids(z.read(entry)))
    with gzip.open(path + '.part', 'wt') as f:
        json.dump({'version': version, 'classes': model, 'rids': rids}, f)
    os.replace(path + '.part', path)


def r_ids(b):
    """Constant values of android.R$* fields: {'0x01010000': 'attr/theme'}."""
    count = struct.unpack_from('>H', b, 8)[0]
    cp, o, i = [None] * count, 10, 1
    while i < count:
        tag = b[o]
        if tag == 1:
            n = struct.unpack_from('>H', b, o + 1)[0]
            cp[i] = b[o + 3:o + 3 + n].decode()
            o += 3 + n
        elif tag == 3:
            cp[i] = struct.unpack_from('>i', b, o + 1)[0]
            o += 5
        elif tag in (7, 8, 16, 19, 20):
            cp[i] = ('ref', struct.unpack_from('>H', b, o + 1)[0])
            o += 3
        elif tag == 15:
            o += 4
        elif tag in (4, 9, 10, 11, 12, 17, 18):
            o += 5
        elif tag in (5, 6):
            o += 9
            i += 1
        i += 1
    _, this, _, nif = struct.unpack_from('>HHHH', b, o)
    kind = cp[cp[this][1]].split('$')[-1]
    o += 8 + 2 * nif
    count = struct.unpack_from('>H', b, o)[0]
    o += 2
    out = {}
    for _ in range(count):
        _, name, _, na = struct.unpack_from('>HHHH', b, o)
        o += 8
        for _ in range(na):
            an, length = struct.unpack_from('>HI', b, o)
            if cp[an] == 'ConstantValue':
                value = cp[struct.unpack_from('>H', b, o + 6)[0]]
                if isinstance(value, int):
                    out['0x%08x' % (value & 0xffffffff)] = kind + '/' + cp[name]
            o += 6 + length
    return out


def resolves(model, cls, member, is_field, seen=None):
    seen = seen if seen is not None else set()
    if cls is None or cls in seen or cls not in model:
        return False
    seen.add(cls)
    sup, ifaces, fields, methods = model[cls]
    if member in (fields if is_field else methods):
        return True
    if resolves(model, sup, member, is_field, seen):
        return True
    if any(resolves(model, i, member, is_field, seen) for i in ifaces):
        return True
    return not is_field and cls in ifaces and resolves(model, 'java/lang/Object', member, False, seen)


def api_gaps(dexes, low, high):
    """Framework refs that resolve at API `high` but not at API `low`."""
    defined = set().union(*(d.defined for d in dexes))
    gaps = collections.defaultdict(set)
    for d in dexes:
        for (kind, ref), callers in d.calls.items():
            cls = ref[0]
            if cls in defined or not cls.startswith('L') or not cls[1:].startswith(PLATFORM):
                continue
            c = cls[1:-1]
            member = ref[1] if kind == 'F' else ref[1] + ref[2]
            is_field = kind == 'F'
            if not is_field and member in OBJECT_METHODS:
                continue
            if c not in low['classes']:
                if c in high['classes']:
                    gaps['class %s' % c.replace('/', '.')].update('%s:%s' % (d.name, x) for x in callers)
                continue
            if resolves(high['classes'], c, member, is_field) and not resolves(low['classes'], c, member, is_field):
                label = '%s %s.%s%s' % ('field' if is_field else 'method', c.replace('/', '.'), ref[1],
                                        '' if is_field else ref[2])
                gaps[label].update('%s:%s' % (d.name, x) for x in callers)
    return gaps


# ------------------------------------------------------------------ resources

def unpack_locale(two, base):
    if not two[0]:
        return ''
    if two[0] & 0x80:
        a, b0 = two[1], two[0]
        return ''.join(chr(base + x) for x in (a & 0x1f, ((a & 0xe0) >> 5) + ((b0 & 0x03) << 3), (b0 & 0x7c) >> 2))
    return two.decode('latin1').strip('\0')


def config(cfg):
    cfg = cfg + b'\0' * 64
    lang = unpack_locale(cfg[8:10], ord('a'))
    region = unpack_locale(cfg[10:12], ord('0'))
    return {'locale': lang + ('-r' + region if region else ''), 'orientation': cfg[12],
            'density': struct.unpack_from('<H', cfg, 14)[0], 'sdk': struct.unpack_from('<H', cfg, 24)[0],
            'uimode': cfg[29], 'sw': struct.unpack_from('<H', cfg, 30)[0],
            'w': struct.unpack_from('<H', cfg, 32)[0], 'h': struct.unpack_from('<H', cfg, 34)[0],
            'layoutdir': cfg[28] & 0xc0}


def qualifier(c):
    parts = []
    if c['sw']:
        parts.append('sw%ddp' % c['sw'])
    if c['w']:
        parts.append('w%ddp' % c['w'])
    if c['h']:
        parts.append('h%ddp' % c['h'])
    if c['orientation']:
        parts.append({1: 'port', 2: 'land'}.get(c['orientation'], 'orientation%d' % c['orientation']))
    return '-'.join(parts)


def parse_arsc(b):
    _, hs, size, _ = struct.unpack_from('<HHII', b, 0)
    values = string_pool(b, hs)
    packages, o = {}, hs
    while o < size:
        t, h, s = struct.unpack_from('<HHI', b, o)
        if t == 0x0200:
            pid = struct.unpack_from('<I', b, o + 8)[0]
            pkg = {'name': b[o + 12:o + 268].decode('utf-16le').split('\0')[0], 'flags': collections.Counter(),
                   'entries': collections.defaultdict(list)}
            tstr, _, kstr = struct.unpack_from('<III', b, o + 268)
            types, keys = string_pool(b, o + tstr), string_pool(b, o + kstr)
            p = o + h
            while p < o + s:
                ct, ch, cs = struct.unpack_from('<HHI', b, p)
                if ct == 0x0201:
                    tid, flags, _, count, estart = struct.unpack_from('<BBHII', b, p + 8)
                    csize = struct.unpack_from('<I', b, p + 20)[0]
                    cfg = config(b[p + 20:p + 20 + csize])
                    pkg['flags']['sparse' if flags & 1 else 'offset16' if flags & 2 else 'dense'] += 1
                    if flags & 1:
                        offs = [(i, x * 4) for i, x in (struct.unpack_from('<HH', b, p + ch + 4 * k) for k in range(count))]
                    elif flags & 2:
                        offs = [(i, x * 4) for i, x in enumerate(struct.unpack_from('<%dH' % count, b, p + ch)) if x != 0xffff]
                    else:
                        offs = [(i, x) for i, x in enumerate(struct.unpack_from('<%dI' % count, b, p + ch)) if x != 0xffffffff]
                    for i, eo in offs:
                        e = p + estart + eo
                        esize, eflags = struct.unpack_from('<HH', b, e)
                        if eflags & 0x8:
                            pkg['flags']['compact-entries'] += 1
                            key, vals = keys[esize], [(eflags >> 8, struct.unpack_from('<I', b, e + 4)[0])]
                        else:
                            key = keys[struct.unpack_from('<I', b, e + 4)[0]]
                            if eflags & 1:
                                n = struct.unpack_from('<I', b, e + 12)[0]
                                vals = [(b[e + 16 + 12 * k + 7], struct.unpack_from('<I', b, e + 16 + 12 * k + 8)[0])
                                        for k in range(n)]
                            else:
                                vals = [(b[e + 11], struct.unpack_from('<I', b, e + 12)[0])]
                        rid = (pid << 24) | (tid << 16) | i
                        text = values[vals[0][1]] if not eflags & 1 and vals and vals[0][0] == 3 else None
                        pkg['entries'][rid].append({'type': types[tid - 1], 'key': key, 'cfg': cfg, 'vals': vals,
                                                    'text': text})
                p += cs
            packages[pid] = pkg
        o += s
    return packages


def universal(c, api):
    return (not c['orientation'] and c['sdk'] <= api and not c['uimode'] and not c['sw'] and not c['w']
            and not c['h'] and not c['layoutdir'] and not c['locale'])


# --------------------------------------------------------------------- native

def elf_info(b):
    """Returns (bits, max LOAD alignment, needed libs, required libc version names)."""
    is64 = b[4] == 2
    if is64:
        phoff, = struct.unpack_from('<Q', b, 0x20)
        phentsize, phnum = struct.unpack_from('<HH', b, 0x36)
    else:
        phoff, = struct.unpack_from('<I', b, 0x1c)
        phentsize, phnum = struct.unpack_from('<HH', b, 0x2a)
    loads, dynamic, align = [], None, 0
    for i in range(phnum):
        o = phoff + i * phentsize
        if is64:
            ptype, _, off, vaddr, _, filesz, _, palign = struct.unpack_from('<IIQQQQQQ', b, o)
        else:
            ptype, off, vaddr, _, filesz, _, _, palign = struct.unpack_from('<IIIIIIII', b, o)
        if ptype == 1:
            loads.append((vaddr, off, filesz))
            align = max(align, palign)
        elif ptype == 2:
            dynamic = (off, filesz)

    def to_off(addr):
        for vaddr, off, filesz in loads:
            if vaddr <= addr < vaddr + filesz:
                return addr - vaddr + off
        return None
    tags = collections.defaultdict(list)
    if dynamic:
        fmt, step = ('<qQ', 16) if is64 else ('<iI', 8)
        for o in range(dynamic[0], dynamic[0] + dynamic[1], step):
            tag, val = struct.unpack_from(fmt, b, o)
            if tag == 0:
                break
            tags[tag].append(val)
    strtab = to_off(tags[5][0]) if tags.get(5) else None

    def cstr(o):
        return b[o:b.index(0, o)].decode('latin1')
    needed = [cstr(strtab + x) for x in tags.get(1, [])] if strtab is not None else []
    versions = set()
    if tags.get(0x6ffffffe) and strtab is not None:
        o = to_off(tags[0x6ffffffe][0])
        for _ in range(tags[0x6fffffff][0]):
            _, cnt, _, aux, nxt = struct.unpack_from('<HHIII', b, o)
            a = o + aux
            for _ in range(cnt):
                _, _, _, name, anext = struct.unpack_from('<IHHII', b, a)
                versions.add(cstr(strtab + name))
                a += anext
            o += nxt
    return (64 if is64 else 32), align, needed, versions


# ---------------------------------------------------------------------- APK

def signing_schemes(path, names):
    schemes = ['v1'] if any(re.match(r'META-INF/[^/]+\.(RSA|DSA|EC)$', n) for n in names) else []
    with open(path, 'rb') as f:
        data = f.read()
    eocd = data.rfind(b'PK\x05\x06')
    cd = struct.unpack_from('<I', data, eocd + 16)[0]
    if data[cd - 16:cd] == b'APK Sig Block 42':
        size = struct.unpack_from('<Q', data, cd - 24)[0]
        o, end = cd - size - 8 + 8, cd - 24
        known = {0x7109871a: 'v2', 0xf05368c0: 'v3', 0x1b93ad61: 'v3.1', 0x6dff800d: 'source-stamp'}
        while o < end:
            length, pid = struct.unpack_from('<QI', data, o)
            if pid in known:
                schemes.append(known[pid])
            o += 8 + length
    return schemes


def short(callers, limit):
    callers = sorted(callers)
    more = len(callers) - limit
    text = ', '.join('`%s`' % c for c in callers[:limit])
    return text + (' (+%d more)' % more if more > 0 else '')


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('apk')
    ap.add_argument('--min-api', type=int, default=26, help='oldest Android API level to support (default 26)')
    ap.add_argument('--baseline-api', type=int, default=28,
                    help='API level the dex code was compiled for; Spotify Automotive and the BYD UI use 28')
    ap.add_argument('--locales', default='uk,ru,ar,zh-rCN', help='locales the app UI must be translated into')
    ap.add_argument('--no-api', action='store_true', help='skip the framework API check (no download)')
    ap.add_argument('--cache', default=os.path.join(os.path.expanduser('~'), '.cache', 'apk-audit'))
    ap.add_argument('--sites', type=int, default=3, help='call sites listed per finding')
    args = ap.parse_args()
    rep = Report()
    z = zipfile.ZipFile(args.apk)
    names = z.namelist()

    # Manifest
    man = parse_axml(z.read('AndroidManifest.xml'))
    sdk = next((n['attrs'] for n in walk(man) if n['tag'] == 'uses-sdk'), {})
    app = next(n for n in walk(man) if n['tag'] == 'application')
    min_sdk, target = int(sdk.get('minSdkVersion', 1)), int(sdk.get('targetSdkVersion', 0))
    rep.check('PASS' if min_sdk <= args.min_api else 'FAIL', 'minSdkVersion',
              '%d; API %d devices refuse to install it' % (min_sdk, args.min_api) if min_sdk > args.min_api
              else '%d, targetSdk %d' % (min_sdk, target))
    factory = app['attrs'].get('appComponentFactory')
    procs = sorted({n['attrs']['process'] for n in walk(man) if 'process' in n['attrs']})
    if factory and args.min_api < 28:
        rep.check('FAIL', 'Process routing', '`appComponentFactory` (%s) is ignored below API 28; processes %s '
                  'would all start `%s`' % (factory, procs, app['attrs'].get('name')))
    else:
        rep.check('PASS', 'Process routing', 'factory %s, processes %s' % (factory or 'none', procs or ['main']))
    dexes = [Dex(n, z.read(n)) for n in sorted(names, key=lambda n: (len(n), n)) if re.match(r'classes\d*\.dex$', n)]
    if app['attrs'].get('enableOnBackInvokedCallback') is True:
        legacy = [d.name for d in dexes
                  if any(m[1] == 'onBackPressed' and m[2] == '()V' and m[0] in d.defined for m in d.declared)
                  and not any(k[1][0] == 'Landroid/window/OnBackInvokedCallback;' for k in d.calls)
                  and not any(m[0] == 'Landroid/window/OnBackInvokedCallback;' for m in d.methods)]
        rep.check('FAIL' if legacy else 'PASS', 'Back key on Android 13+',
                  'application opts into OnBackInvokedCallback, but %s override onBackPressed() without it: '
                  'Back skips their navigation' % ', '.join(legacy) if legacy else 'predictive back supported')
    else:
        rep.check('PASS', 'Back key on Android 13+', 'legacy onBackPressed() dispatch')
    exported = []
    for n in walk(man):
        a = n['attrs']
        if n['tag'] in ('activity', 'service', 'receiver', 'provider') and a.get('exported') is True \
                and 'permission' not in a and not any(c['tag'] == 'intent-filter' and any(
                    x['attrs'].get('name') == 'android.intent.category.LAUNCHER' for x in walk(c)) for c in n['children']):
            exported.append('%s `%s`' % (n['tag'], a.get('name')))
    rep.check('INFO' if exported else 'PASS', 'Exported without permission', '; '.join(exported) or 'none')
    metas = {n['attrs'].get('name'): n['attrs'].get('value') for n in walk(man) if n['tag'] == 'meta-data'}
    stale = [k for k in metas if k and (k.startswith('com.android.stamp.') or k.startswith('com.android.vending.'))]
    rep.check('WARN' if stale else 'PASS', 'Play Store metadata', ', '.join('`%s`' % k for k in stale) or 'none')
    crashlytics = any('crashlytics' in (k or '').lower() for k in metas) and \
        metas.get('firebase_crashlytics_collection_enabled') is not False
    rep.check('WARN' if crashlytics else 'PASS', 'Crash reporting',
              'Firebase Crashlytics is enabled' if crashlytics else 'no active Crashlytics')
    vendor_perms = [n['attrs']['name'] for n in walk(man)
                    if n['tag'].startswith('uses-permission') and VENDOR_STRING.search(n['attrs'].get('name', ''))]
    vendor_meta = [k for k in metas if k and VENDOR_STRING.search(k + ' ' + str(metas[k]))]

    # Signing and packaging
    schemes = signing_schemes(args.apk, names)
    need_v2 = target >= 30
    rep.check('FAIL' if need_v2 and not {'v2', 'v3'} & set(schemes) else 'PASS', 'APK signature',
              ', '.join(schemes) or 'unsigned')
    arsc = z.getinfo('resources.arsc')
    with open(args.apk, 'rb') as f:
        f.seek(arsc.header_offset + 26)
        nlen, xlen = struct.unpack('<HH', f.read(4))
    data_off = arsc.header_offset + 30 + nlen + xlen
    ok = arsc.compress_type == zipfile.ZIP_STORED and data_off % 4 == 0
    rep.check('PASS' if ok or target < 30 else 'FAIL', 'resources.arsc storage',
              'stored, 4-byte aligned' if ok else 'compressed or unaligned (Android 11+ rejects targetSdk 30+)')

    # Framework API levels
    rids = None
    if not args.no_api:
        low = framework(args.min_api, args.cache)
        rids = set(low['rids'])
        baseline = max(args.baseline_api, args.min_api)
        if args.min_api >= args.baseline_api:
            rep.check('PASS', 'Framework APIs', 'min API %d is not below the code baseline %d'
                      % (args.min_api, args.baseline_api))
        else:
            gaps = api_gaps(dexes, low, framework(baseline, args.cache))
            sites = sum(len(v) for v in gaps.values())
            added = 'API %d' % baseline if args.min_api + 1 == baseline else 'API %d-%d' % (args.min_api + 1, baseline)
            rep.check('FAIL' if gaps else 'PASS', 'Framework APIs',
                      '%d APIs from %s used at %d call sites with no fallback on API %d'
                      % (len(gaps), added, sites, args.min_api) if gaps else
                      'every API %d reference also resolves on API %d' % (baseline, args.min_api))
            lines = ['Code compiled for API %d can drop `SDK_INT` checks below that level, so each call below runs '
                     'unguarded on API %d unless a shim replaced it. Static initializers (`<clinit>`) fail the whole '
                     'class.' % (baseline, args.min_api), '']
            for k in sorted(gaps, key=lambda k: (-len(gaps[k]), k)):
                flag = ' **static init**' if any('<clinit>' in c for c in gaps[k]) else ''
                lines.append('- %s%s: %s' % (k, flag, short(gaps[k], args.sites)))
            if gaps:
                rep.section('APIs missing on API %d' % args.min_api, lines)

    # Native libraries
    abis = collections.defaultdict(list)
    worst = 0
    misaligned = []
    for n in names:
        m = re.match(r'lib/([^/]+)/([^/]+\.so)$', n)
        if not m:
            continue
        bits, align, needed, versions = elf_info(z.read(n))
        level = max([LIBC_API.get(v, 0) for v in versions] or [0])
        worst = max(worst, level)
        abis[m.group(1)].append((m.group(2), level))
        if bits == 64 and align < 0x4000:
            misaligned.append(n)
    rep.check('FAIL' if worst > args.min_api else 'PASS', 'Native libc symbols',
              '%s (%s)' % ('need API %d' % worst if worst else 'baseline libc only', ', '.join(sorted(abis)))
              if abis else 'no native libraries')
    rep.check('WARN' if misaligned else 'PASS', '16 KB page alignment',
              '%d 64-bit libraries below 16 KB alignment' % len(misaligned) if misaligned else 'all 64-bit LOAD segments')
    has32 = any(a in abis for a in ('armeabi-v7a', 'armeabi'))
    rep.check('PASS' if has32 or not abis else 'WARN', '32-bit ARM', 'armeabi-v7a present' if has32 else
              'missing: many head units run 32-bit userspace')

    # Resources
    packages = parse_arsc(z.read('resources.arsc'))
    fmt = collections.Counter()
    for p in packages.values():
        fmt.update(p['flags'])
    risky = {k: v for k, v in fmt.items() if k in ('compact-entries', 'offset16')}
    rep.check('WARN' if risky else 'PASS', 'Resource table format', ', '.join('%s %d' % kv for kv in fmt.items()))
    fallback_lines, screen_only, sdk_only = [], [], []
    for pid, p in sorted(packages.items()):
        buckets = collections.Counter(qualifier(e['cfg']) for es in p['entries'].values() for e in es
                                      if e['type'] in ('layout', 'dimen') and qualifier(e['cfg']))
        fallback_lines.append('- package 0x%02x `%s`: layout/dimen screen buckets %s' % (
            pid, p['name'], ', '.join('%s (%d)' % kv for kv in sorted(buckets.items())) or 'none'))
        for rid, es in p['entries'].items():
            if not any(universal(e['cfg'], args.min_api) for e in es):
                tag = '%s/%s' % (es[0]['type'], es[0]['key'])
                if es[0]['key'].startswith(LIBRARY_STRINGS):
                    continue
                if all(e['cfg']['sdk'] > args.min_api for e in es):
                    sdk_only.append(tag)
                elif not any(e['cfg']['locale'] for e in es):
                    screen_only.append(tag)
    rep.check('WARN' if screen_only else 'PASS', 'Resources without default',
              '%d only exist for some screens/orientations; %d only above API %d (expected for Material You)'
              % (len(screen_only), len(sdk_only), args.min_api))
    if screen_only:
        fallback_lines += ['', 'Only in screen/orientation-specific configs: ' + ', '.join(sorted(screen_only)[:40])]
    rep.section('Screen buckets', fallback_lines)
    if rids is not None:
        bad = set()
        for p in packages.values():
            for es in p['entries'].values():
                for e in es:
                    if e['cfg']['sdk'] <= args.min_api:
                        for t, v in e['vals']:
                            if t in (1, 2) and v >> 24 == 1 and '0x%08x' % v not in rids:
                                bad.add('%s/%s -> %s%08x' % (e['type'], e['key'], '?' if t == 2 else '@', v))
        rep.check('WARN' if bad else 'PASS', 'Framework resource refs',
                  '%d values point at android resources absent on API %d' % (len(bad), args.min_api) if bad
                  else 'all resolve on API %d' % args.min_api)
        if bad:
            rep.section('Framework resource refs missing on API %d' % args.min_api, ['- ' + x for x in sorted(bad)])
    want = [l for l in args.locales.split(',') if l]
    loc_lines = []
    missing_total = 0
    for pid, p in sorted(packages.items()):
        have = collections.defaultdict(set)
        for es in p['entries'].values():
            for e in es:
                if e['type'] == 'string' and e['cfg']['locale']:
                    have[e['cfg']['locale']].add(e['key'])
        # Strings no locale translates are identifiers or debug text, not UI.
        translated = set().union(*have.values()) if have else set()
        base = {e['key']: e['text'] for es in p['entries'].values() for e in es
                if e['type'] == 'string' and not e['cfg']['locale'] and e['key'] in translated
                and not e['key'].startswith(LIBRARY_STRINGS)}
        if not base:
            continue
        for loc in want:
            got = have.get(loc, set()) | have.get(loc.split('-')[0], set())
            miss = sorted(k for k in base if k not in got)
            missing_total += len(miss)
            if miss:
                loc_lines.append('- `%s` %s: %d of %d app strings missing: %s' % (
                    p['name'], loc, len(miss), len(base), ', '.join(miss[:12]) + (' ...' if len(miss) > 12 else '')))
    rep.check('WARN' if missing_total else 'PASS', 'Translations (%s)' % ','.join(want),
              '%d missing app strings' % missing_total if missing_total else 'complete')
    if loc_lines:
        rep.section('Missing translations', loc_lines)

    # Vendor coupling
    vendor = collections.defaultdict(set)
    defined = set().union(*(d.defined for d in dexes))
    for d in dexes:
        for (kind, ref), callers in d.calls.items():
            if VENDOR_CLASS.search(ref[0]) and ref[0] not in defined:
                vendor['%s.%s' % (ref[0][1:-1].replace('/', '.'), ref[1])].update(callers)
        for s, callers in d.consts.items():
            # Relocated vendor SDK interface descriptors are inert; only report reachable hooks.
            if VENDOR_STRING.search(s) and len(s) < 120 and not s.startswith('local.bydui.com.byd.'):
                vendor['"%s"' % s].update(callers)
    rep.check('INFO' if vendor or vendor_perms or vendor_meta else 'PASS', 'Vendor (BYD) coupling',
              '%d code references, %d permissions, %d meta-data' % (len(vendor), len(vendor_perms), len(vendor_meta)))
    lines = ['Each must be inert on other head units: guarded by a vendor check, or removed.', '']
    lines += ['- permission `%s`' % p for p in vendor_perms] + ['- meta-data `%s`' % m for m in vendor_meta]
    lines += ['- %s: %s' % (k, short(v, args.sites)) for k, v in sorted(vendor.items())]
    rep.section('Vendor coupling', lines)

    print('# Head-unit audit: %s\n' % os.path.basename(args.apk))
    print('Package `%s` %s, min API target %d\n' % (man['attrs'].get('package'), man['attrs'].get('versionName'),
                                                     args.min_api))
    print(rep.render())
    sys.exit(1 if rep.failed() else 0)


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Update-server record (schema 1) for a published Spotify release, checked against the
server's Spotify rules. Publish it with the operator step in adapter/README.md (App updates)."""
import argparse, json, re
from pathlib import Path

RELEASES = 'https://github.com/romanchukg-cloud/spotify-android-head-units/releases/'
ap = argparse.ArgumentParser(description=__doc__)
ap.add_argument('release_json', type=Path, help='reports/headunit-test.N/release.json of the published release')
ap.add_argument('notes', type=Path, help='plain English notes, at most 2000 characters')
ap.add_argument('--out', type=Path)
a = ap.parse_args()
info = json.loads(a.release_json.read_text())
match = re.fullmatch(r'Spotify-5\.5\.0-HeadUnit-test\.([1-9][0-9]*)\.apk', info['file'])
if not match:
    raise SystemExit('Unexpected APK name %s' % info['file'])
n = int(match[1])
tag = 'v5.5.0-headunit-test.%d' % n
notes = a.notes.read_text().strip()
record = {'schema_version': 1, 'project_id': 'spotify-android-head-units', 'build': n, 'version': 'test.%d' % n,
          'version_code': 95477 + n, 'package_name': 'com.spotify.music',
          'apk_url': '%sdownload/%s/%s' % (RELEASES, tag, info['file']), 'sha256': info['sha256'], 'size': info['size'],
          'notes': notes, 'notes_url': RELEASES + 'tag/' + tag, 'min_sdk': 28}
problems = [name for name, ok in (
    ('versionCode', info['versionCode'] == record['version_code']), ('package', info['package'] == 'com.spotify.music'),
    ('minSdk', info['minSdk'] == 28), ('sha256', re.fullmatch('[0-9a-f]{64}', info['sha256']) is not None),
    ('size', 0 < info['size'] <= 512 * 1024 * 1024), ('notes', len(notes) <= 2000),
    ('releaseUrl', info['releaseUrl'] == record['notes_url'])) if not ok]
if problems:
    raise SystemExit('Release metadata does not match the update feed rules: %s' % ', '.join(problems))
text = json.dumps(record, indent=2) + '\n'
if a.out:
    a.out.write_text(text)
print(text, end='')

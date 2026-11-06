"""One source for the local APK identity and generated Java build label."""
import json,os,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
BUILD=json.loads((ROOT/'build-info.json').read_text())
assert re.fullmatch(r'headunit-test\.\d+',BUILD['label'])
assert re.fullmatch(r'Spotify-5\.5\.0-HeadUnit-test\.\d+\.apk',BUILD['apk_name'])
assert isinstance(BUILD['version_code'],int) and BUILD['version_code']>95483
NUMBER=int(BUILD['label'].rsplit('.',1)[1])
# The shared update server accepts exactly versionCode 95477 + N for test.N.
assert BUILD['version_code']==95477+NUMBER and BUILD['apk_name']=='Spotify-5.5.0-HeadUnit-test.%d.apk'%NUMBER
# HU_DEBUG=1 builds the debug variant: the panel test host and its own-uid access, a "-debug" APK.
DEBUG=os.environ.get('HU_DEBUG')=='1'
# HU_EDITION=alongside builds the edition for cars that already have Spotify as a system app: its
# own package and account type, so it installs next to the built-in app. No in-app updates yet.
EDITION=os.environ.get('HU_EDITION','');assert EDITION in ('','alongside')
PACKAGE='io.github.romanchukgcloud.spotifyhu' if EDITION else 'com.spotify.music'
# Strings in the resource table that name the app: the account type belongs to the first app that
# declares it, and the backend builds its cover URIs from the provider's authority.
TABLE_STRINGS={'com.spotify':'spotifyhu.a','com.spotify.music.mediaapi':PACKAGE+'.mediaapi'} if EDITION else {}
if EDITION:BUILD['label']+='-'+EDITION;BUILD['apk_name']=BUILD['apk_name'][:-4]+'-'+EDITION+'.apk'
if DEBUG:BUILD['apk_name']=BUILD['apk_name'][:-4]+'-debug.apk'
def generate():
 p=ROOT/'generated/local/spotify/unified/HeadUnitBuild.java';p.parent.mkdir(parents=True,exist_ok=True)
 p.write_text('package local.spotify.unified;\npublic final class HeadUnitBuild {\n'
              f' public static final String LABEL="{BUILD["label"]}";\n'
              f' public static final int VERSION_CODE={BUILD["version_code"]};\n'
              f' public static final int BUILD={NUMBER};\n'
              f' public static final String PACKAGE="{PACKAGE}";\n'
              f' public static final boolean ALONGSIDE={"true" if EDITION else "false"};\n'
              f' public static final boolean DEBUG={"true" if DEBUG else "false"};\n'+'}\n')
if __name__=='__main__':generate()

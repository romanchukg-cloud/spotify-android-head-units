"""One source for the local APK identity and generated Java build label."""
import json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
BUILD=json.loads((ROOT/'build-info.json').read_text())
assert re.fullmatch(r'headunit-test\.\d+',BUILD['label'])
assert re.fullmatch(r'Spotify-5\.5\.0-HeadUnit-test\.\d+\.apk',BUILD['apk_name'])
assert isinstance(BUILD['version_code'],int) and BUILD['version_code']>95483
def generate():
 p=ROOT/'generated/local/spotify/unified/HeadUnitBuild.java';p.parent.mkdir(parents=True,exist_ok=True)
 p.write_text('package local.spotify.unified;\npublic final class HeadUnitBuild {\n'
              f' public static final String LABEL="{BUILD["label"]}";\n'
              f' public static final int VERSION_CODE={BUILD["version_code"]};\n'+'}\n')
if __name__=='__main__':generate()

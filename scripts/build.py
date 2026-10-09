"""Generate static localized Pages and READMEs; no runtime dependencies."""
from pathlib import Path
from html import escape as e
import json
ROOT=Path(__file__).resolve().parents[1]
CONTENT=json.loads((ROOT/'scripts/content.json').read_text())
LABELS={'en':'English','uk':'Українська','ru':'Русский','ar':'العربية','zh-Hans':'简体中文'}
SITE='https://romanchukg-cloud.github.io/spotify-byd/'
REPO='https://github.com/romanchukg-cloud/spotify-byd'
RELEASE=REPO+'/releases/tag/v5.5.0-byd-unified-test.1'
SHA='0e057caf7f3a7962da0ba73207b16a7f699cc8b3220d198d9102bf60654d3d65'
def route(lang):return '' if lang=='en' else lang+'/'
for lang,c in CONTENT.items():
 prefix='' if lang=='en' else '../'
 direction='rtl' if lang=='ar' else 'ltr'
 alternate='\n'.join(f'<link rel="alternate" hreflang="{k}" href="{SITE+route(k)}">' for k in CONTENT)
 links=''.join(f'<a lang="{k}" hreflang="{k}" dir="auto" href="{prefix+route(k) or "./"}"'+(' aria-current="page"' if k==lang else '')+f'>{e(LABELS[k])}</a>' for k in CONTENT)
 cards=''.join(f'<article class="card"><h2>{e(t)}</h2><p>{e(s)}</p></article>' for t,s in c['cards'])
 steps=''.join(f'<li>{e(s)}</li>' for s in c['steps'])
 html=f'''<!doctype html>
<html lang="{lang}" dir="{direction}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="description" content="{e(c['description'],quote=True)}">
<title>{e(c['title'])}</title>
<link rel="stylesheet" href="{prefix}style.css">
<link rel="canonical" href="{SITE+route(lang)}">
<link rel="alternate" hreflang="x-default" href="{SITE}">
{alternate}
</head>
<body><main>
<nav><strong>{e(c['name'])}</strong><a href="{REPO}" dir="ltr">GitHub ↗</a></nav>
<nav class="language-switch" aria-label="{e(c['languages'])}">{links}</nav>
<header><div class="badge">{e(c['badge'])}</div>
<h1>{e(c['headline'])}<br><span>{e(c['highlight'])}</span></h1>
<p>{e(c['intro'])}</p>
<a class="button" href="{RELEASE}">{e(c['download'])} ↗</a>
<div class="meta">{e(c['meta'])}</div></header>
<div class="grid">{cards}</div>
<section><h2>{e(c['testedTitle'])}</h2><p>{e(c['tested'])}</p>
<p><strong>{e(c['pendingStrong'])}</strong> {e(c['pending'])}</p><p>{e(c['compatibility'])}</p></section>
<section><h2>{e(c['installTitle'])}</h2><ol>{steps}</ol></section>
<section><h2>{e(c['verifyTitle'])}</h2><p>{e(c['verify'])}</p><code dir="ltr">{SHA}</code></section>
<footer>{e(c['footer'])}</footer>
</main></body></html>
'''
 dest=ROOT/'docs'/route(lang)/'index.html';dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(html)
 readme='README.md' if lang=='en' else f'README.{lang}.md'
 languages=' · '.join(f'[{LABELS[k]}]('+('README.md' if k=='en' else f'README.{k}.md')+')' for k in CONTENT)
 markdown=f"# {c['name']}\n\n{languages}\n\n{c['intro']}\n\n[{c['download']}]({RELEASE}) · [🌐 {c['name']}]({SITE+route(lang)})\n\n## {c['testedTitle']}\n\n{c['tested']}\n\n**{c['pendingStrong']}** {c['pending']}\n\n{c['compatibility']}\n\n## {c['installTitle']}\n\n"
 markdown+='\n'.join(f'{i}. {s}' for i,s in enumerate(c['steps'],1))
 markdown+=f"\n\n{c['meta']}\n\n## {c['verifyTitle']}\n\n`Spotify-5.5.0-BYD-unified-test.apk` · 57 243 570 bytes\n\n{c['verify']}\n\n```text\n{SHA}\n```\n\n{c['footer']}\n"
 if lang=='ar':markdown='<div dir="rtl">\n\n'+markdown+'\n</div>\n'
 (ROOT/readme).write_text(markdown)
print('Generated 5 pages and 5 READMEs. English is the default.')

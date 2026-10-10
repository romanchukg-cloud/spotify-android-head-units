from pathlib import Path
import shutil,re,json,xml.etree.ElementTree as E
base=Path(__file__).resolve().parent;src=base.parent/'spotify55-adaptation/ui/res';dst=base/'ui/res'
# Preserve original resource IDs and data-binding tags when adding configurations.
for typ in ['layout','drawable','xml','values']:
 for width in ['600','840']:
  original=src/(typ+'-w'+width+'dp-h480dp')
  if original.exists():
   target=dst/(typ+'-w'+width+'dp-land')
   wide=src/(typ+'-w840dp-h480dp')
   if wide.exists():shutil.copytree(wide,target,dirs_exist_ok=True)
   shutil.copytree(original,target,dirs_exist_ok=True)
 original=src/(typ+'-w600dp-h480dp')
 if original.exists():
  target=dst/(typ+'-land');wide=src/(typ+'-w840dp-h480dp')
  if wide.exists():shutil.copytree(wide,target,dirs_exist_ok=True)
  shutil.copytree(original,target,dirs_exist_ok=True)
 # These resources describe the large portrait layout only.
 original=src/(typ+'-w600dp-h900dp')
 if original.exists():
  target=dst/(typ+'-w600dp-h900dp-port');shutil.copytree(original,target,dirs_exist_ok=True)
  old=dst/original.name
  if old.exists():shutil.rmtree(old)
for width in ['', '-w600dp','-w840dp']:
 folder=dst/('values'+width+'-land');folder.mkdir(exist_ok=True)
 source=src/('values-w'+('840' if '840' in width else '600')+'dp-h480dp')/'dimens.xml'
 if not source.exists():continue
 tree=E.parse(src/'values-w840dp-h480dp/dimens.xml');wide_nodes={n.get('name'):n for n in tree.getroot()}
 for n in E.parse(source).getroot():
  old=wide_nodes.get(n.get('name'))
  if old is not None:tree.getroot().remove(old)
  tree.getroot().append(n)
 large=dst/('values'+width+'-h400dp-land');large.mkdir(exist_ok=True);tree.write(large/'dimens.xml',encoding='utf-8',xml_declaration=True)
 # Compact dimensions apply below h400dp; h400dp restores the original values.
 for n in tree.getroot():
  if n.text and re.fullmatch(r'[\d.]+(?:dip|sp)',n.text):
   value=float(re.match(r'[\d.]+',n.text)[0]);unit='sp' if n.text.endswith('sp') else 'dip'
   name=n.get('name','')
   factor=.72 if any(v in name for v in ['margin','padding','spacer']) else .82
   if unit=='sp':value=max(14,value*.85)
   elif value>20:value=max(20,value*factor)
   n.text=f'{value:.1f}{unit}'
 tree.write(folder/'dimens.xml',encoding='utf-8',xml_declaration=True)
keys=['added_to_queue','added_to_your_episodes','added_to_your_library','added_to_your_liked_songs','app_name','back','byd_error','clear_all','collect_episode_title','collect_song_title','dialog_cancel','dialog_content','dialog_go_to_set','dialog_title','multi_episode_count','multi_song_count','play_what_you_love','recent_searches','removed_from_your_episodes','removed_from_your_library','removed_from_your_liked_songs','retry','safety_subtitle','safety_title','search','search_for_something','search_menu_title','slogan','something_wrong','speed','unknown_song','widget_slogan','wifi_notification','your_library']
values={
'uk':['Додано до черги','Додано до ваших епізодів','Додано до медіатеки','Додано до вподобаних пісень','Spotify','Назад','Не вдалося запустити плеєр. Спробуйте ще раз.','Очистити все','Ваші епізоди','Вподобані пісні','Скасувати','Spotify не отримав потрібних дозволів. Відкрити налаштування дозволів?','Відкрити налаштування','Попередження','епізодів','Пісні','Слухайте те, що любите','Останні пошуки','Видалено з ваших епізодів','Видалено з медіатеки','Видалено з вподобаних пісень','Повторити','Ця дія недоступна під час руху','Недоступно з міркувань безпеки','Пошук','Шукайте виконавців, пісні, подкасти тощо.','Пошук','Слухати — це все','Тут немає вмісту для перегляду','Швидкість','Нічого не відтворюється','Слухати — це все','Підключіться до Wi-Fi, щоб усі функції були доступні.','Ваша медіатека'],
'ru':['Добавлено в очередь','Добавлено в ваши эпизоды','Добавлено в медиатеку','Добавлено в понравившиеся песни','Spotify','Назад','Не удалось запустить плеер. Попробуйте ещё раз.','Очистить всё','Ваши эпизоды','Понравившиеся песни','Отмена','Spotify не получил необходимые разрешения. Открыть настройки разрешений?','Открыть настройки','Предупреждение','эпизодов','Песни','Слушайте то, что любите','Недавние поиски','Удалено из ваших эпизодов','Удалено из медиатеки','Удалено из понравившихся песен','Повторить','Это действие недоступно во время движения','Недоступно из соображений безопасности','Поиск','Ищите исполнителей, песни, подкасты и многое другое.','Поиск','Слушать — это всё','Здесь нет контента для просмотра','Скорость','Ничего не играет','Слушать — это всё','Подключитесь к Wi-Fi, чтобы все функции были доступны.','Ваша медиатека']}
for lang,texts in values.items():
 assert len(texts)==len(keys)
 p=dst/('values-'+lang)/'strings.xml';tree=E.parse(p);root=tree.getroot();by={n.get('name'):n for n in root}
 for key,value in zip(keys,texts):
  n=by.get(key)
  if n is None:n=E.SubElement(root,'string',{'name':key})
  n.text=value
 tree.write(p,encoding='utf-8',xml_declaration=True)
neutral={'values':'Could not start the player. Please try again.','values-ar':'تعذر بدء المشغل. حاول مرة أخرى.','values-zh-rCN':'无法启动播放器，请重试。','values-zh':'无法启动播放器，请重试。'}
for p in dst.glob('values*/strings.xml'):
 tree=E.parse(p);changed=False
 for n in tree.getroot():
  if n.get('name')=='byd_error' and p.parent.name not in ['values-uk','values-ru']:
   n.text=neutral.get(p.parent.name,neutral['values']);changed=True
 if changed:tree.write(p,encoding='utf-8',xml_declaration=True)
report={}
for lang in ['uk','ru','ar','zh-rCN']:
 p=dst/('values-'+lang)/'strings.xml';names={n.get('name') for n in E.parse(p).getroot()};missing=set(keys)-names
 # Chinese has a common 'zh' fallback; make CN completeness explicit.
 if lang in ['ar','zh-rCN'] and missing:
  fallback={n.get('name'):n for n in E.parse(dst/('values-zh' if lang=='zh-rCN' else 'values')/'strings.xml').getroot()}
  for key,value in {'app_name':'Spotify','wifi_notification':'连接 Wi-Fi 以使用全部功能。' if lang=='zh-rCN' else 'اتصل بشبكة Wi-Fi لاستخدام جميع الميزات.'}.items():
   node=E.Element('string',{'name':key});node.text=value;fallback.setdefault(key,node)
  t=E.parse(p)
  for key in missing:
   if key in fallback:t.getroot().append(fallback[key])
  t.write(p,encoding='utf-8',xml_declaration=True);names={n.get('name') for n in t.getroot()};missing=set(keys)-names
 report[lang]={'owned_strings':len(keys),'missing':sorted(missing)};assert not missing,(lang,missing)
(base/'translation-audit.json').write_text(json.dumps(report,indent=2,ensure_ascii=False))
print('Landscape aliases and full app-owned uk/ru translations generated')

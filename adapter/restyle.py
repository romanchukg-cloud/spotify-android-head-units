"""Generate the New head-unit style as "car" UI-mode resources.

Classic resources stay untouched. HeadUnitStyle selects the New style at runtime
by setting the UI-mode type of the player's configuration to CAR, so Android picks
the -car resources written here and falls back to the Classic ones for everything
else. Everything is derived from the local decoded interface at build time; no
BYD layouts are committed. Runs after patch_resources.py and before apktool.
"""
from pathlib import Path
import re, shutil, xml.etree.ElementTree as ET

BASE = Path(__file__).resolve().parent
RES = BASE / 'ui/res'
ANDROID = 'http://schemas.android.com/apk/res/android'
APP = 'http://schemas.android.com/apk/res-auto'
A, P = '{%s}' % ANDROID, '{%s}' % APP
ET.register_namespace('android', ANDROID)
ET.register_namespace('app', APP)

# Qualifiers that come after the UI-mode type in a resource directory name.
LATE = re.compile(r'^(night|notnight|[a-z]*dpi|v\d+|finger|notouch|keysexposed|keyshidden|keyssoft|nokeys|qwerty|12key|navexposed|navhidden|nonav|dpad|trackball|wheel)$')


def car(folder):
    """values-w840dp-h480dp -> values-w840dp-h480dp-car; values-night -> values-car-night."""
    parts = folder.split('-')
    i = next((k for k, q in enumerate(parts[1:], 1) if LATE.match(q)), len(parts))
    return '-'.join(parts[:i] + ['car'] + parts[i:])


def clean():
    for d in RES.iterdir():
        if d.is_dir() and 'car' in d.name.split('-')[1:]:
            shutil.rmtree(d)
    for f in list(RES.glob('*/hu_*.xml')) + list(RES.glob('font/hu_*.ttf')):  # values-*/hu_strings.xml included
        f.unlink()


def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8')


def resources(items):
    return '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n' + ''.join('    %s\n' % i for i in items) + '</resources>\n'


# ---------------------------------------------------------------- colours
# (light, dark) values. None keeps the Classic value of that mode.
PALETTE = {
    'colorBackground': ('#fff6f6f6', None),
    'colorPrimaryComBG': ('#ffffffff', '#ff1c1c1c'),
    'colorHighlight': ('#ffebebeb', '#ff2a2a2a'),
    'colorSecondary': ('#ff555555', '#ffb3b3b3'),
    'colorBar': ('#29000000', '#4dffffff'),
    # The Classic fade under the mini-player shows as a band between the rounded cards.
    'colorShadowStart': ('#00000000', '#00000000'),
    'colorShadowEnd': ('#00000000', '#00000000'),
    'colorDialogBackground': ('#ffffffff', '#ff242424'),
    'colorDialogText': ('#ff121212', None),
    'colorDialogDivider': ('#ffe0e0e0', None),
    'colorToast': ('#ff121212', '#ff2a2a2a'),
}
# New colours used only by New-style drawables.
NEW_COLORS = {
    'hu_bar_bg': ('#ffffffff', '#ff282828'),
    'hu_page_bg': ('#ffffffff', '#ff181818'),
    'hu_nav_selected': ('#ffebebeb', '#ff2a2a2a'),
    'hu_chip_bg': ('#ffebebeb', '#ff2a2a2a'),
    'hu_row_bg': ('#ffececec', '#ff232323'),
    'hu_row_current': ('#ffdcdcdc', '#ff333333'),
}


def classic_colors(folder):
    path = RES / folder / 'colors.xml'
    if not path.exists():
        return {}
    return {c.get('name'): c.text for c in ET.parse(path).getroot().findall('color')}


def colors():
    light, dark = classic_colors('values'), classic_colors('values-night')
    for name in PALETTE:
        if name not in light and name not in dark:
            raise SystemExit('restyle: unknown colour ' + name)
    # Every overridden name gets both a -car and a -car-night value, or a light
    # override would win over the Classic night value at night.
    day = ['<color name="%s">%s</color>' % (n, l or light.get(n) or dark.get(n)) for n, (l, d) in PALETTE.items()]
    night = ['<color name="%s">%s</color>' % (n, d or dark.get(n) or light.get(n)) for n, (l, d) in PALETTE.items()]
    write(RES / 'values-car/colors.xml', resources(day))
    write(RES / 'values-car-night/colors.xml', resources(night))
    write(RES / 'values/hu_colors.xml', resources('<color name="%s">%s</color>' % (n, l) for n, (l, d) in NEW_COLORS.items()))
    write(RES / 'values-night/hu_colors.xml', resources('<color name="%s">%s</color>' % (n, d) for n, (l, d) in NEW_COLORS.items()))


# ---------------------------------------------------------------- drawables
SHAPES = {
    # The mini-player is the content card's footer: its bottom corners match the card's.
    'hu_now_playing_bar_bg': '<shape xmlns:android="%s"><corners android:topLeftRadius="18.0dip" android:topRightRadius="18.0dip" android:bottomLeftRadius="22.0dip" android:bottomRightRadius="22.0dip" /><solid android:color="@color/hu_bar_bg" /></shape>' % ANDROID,
    'hu_now_playing_page_bg': '<shape xmlns:android="%s"><corners android:radius="22.0dip" /><solid android:color="@color/hu_page_bg" /></shape>' % ANDROID,
    # Material "settings" icon (Apache 2.0), in the text colour.
    'hu_ic_settings': ('<vector xmlns:android="%s" android:width="28.0dip" android:height="28.0dip" android:viewportWidth="24.0" android:viewportHeight="24.0">'
                       '<path android:fillColor="@color/colorPrimary" android:pathData="M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z" /></vector>') % ANDROID,
    'hu_round_button': '<shape xmlns:android="%s" android:shape="oval"><solid android:color="@color/hu_chip_bg" /></shape>' % ANDROID,
    'hu_root_menu_item_bg': ('<selector xmlns:android="{a}">'
                             '<item android:state_selected="true"><inset android:insetLeft="14.0dip" android:insetRight="14.0dip" android:insetTop="10.0dip" android:insetBottom="10.0dip">'
                             '<shape><corners android:radius="20.0dip" /><solid android:color="@color/hu_nav_selected" /></shape></inset></item>'
                             '<item android:drawable="@android:color/transparent" /></selector>').format(a=ANDROID),
}


# Classic drawables replaced in every size variant they exist in.
REPLACED = {
    # Library and search filters: filled pills instead of outlines.
    'btn_unfocus': '<shape xmlns:android="%s" android:shape="rectangle"><corners android:radius="@dimen/labels_item_radius" /><solid android:color="@color/hu_chip_bg" /></shape>' % ANDROID,
    # Navigation rail: a rounded highlight instead of the side bar. The code picks these by name.
    'root_menu_item_bg': SHAPES['hu_root_menu_item_bg'],
    # List and queue rows: rounded instead of rectangular.
    'bg_ripple': '<ripple xmlns:android="%s" android:color="?tab_item_bg_color_pressed"><item><shape android:shape="rectangle"><solid android:color="?app_bg_color" /><corners android:radius="14.0dip" /></shape></item></ripple>' % ANDROID,
    'queue_bg_ripple': '<ripple xmlns:android="%s" android:color="?tab_item_bg_color_pressed"><item><shape android:shape="rectangle"><solid android:color="?queue_bg_color" /><corners android:radius="14.0dip" /></shape></item></ripple>' % ANDROID,
    'right_driver_root_menu_item_bg': SHAPES['hu_root_menu_item_bg'],
}


def drawables():
    for name, xml in SHAPES.items():
        write(RES / 'drawable' / (name + '.xml'), '<?xml version="1.0" encoding="utf-8"?>\n' + xml + '\n')
    for name, xml in REPLACED.items():
        paths = [p for p in RES.glob('drawable*/%s.xml' % name) if 'car' not in p.parent.name.split('-')]
        if not paths:
            raise SystemExit('restyle: drawable not found: ' + name)
        for path in paths:
            write(RES / car(path.parent.name) / path.name, '<?xml version="1.0" encoding="utf-8"?>\n' + xml + '\n')


# ---------------------------------------------------------------- font
FONT = '@font/hu_figtree_family'


def fonts():
    """Figtree (SIL OFL 1.1, fonts/figtree) as a variable font family with five weights."""
    source = BASE / 'fonts/figtree/Figtree-wght.ttf'
    target = RES / 'font/hu_figtree.ttf'
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
    entries = ''.join('<font android:font="@font/hu_figtree" android:fontStyle="normal" android:fontWeight="%d" android:fontVariationSettings="\'wght\' %d" />' % (w, w)
                      for w in (400, 500, 600, 700, 800))
    write(RES / 'font/hu_figtree_family.xml', '<?xml version="1.0" encoding="utf-8"?>\n<font-family xmlns:android="%s">%s</font-family>\n' % (ANDROID, entries))


def text_styles():
    """New-style copies of the interface's text appearances with Figtree."""
    out = []
    for style in ET.parse(RES / 'values/styles.xml').getroot().findall('style'):
        if not style.get('name', '').startswith('TextAppearance.VIVID.BYD.'):
            continue
        family = next((i for i in style.findall('item') if i.get('name') == 'android:fontFamily'), None)
        if family is None:
            continue
        medium = family.text == 'sans-serif-medium'
        family.text = FONT
        if medium:
            ET.SubElement(style, 'item', {'name': 'android:textFontWeight'}).text = '600'
        out.append(ET.tostring(style, encoding='unicode').strip())
    if not out:
        raise SystemExit('restyle: no text appearances found')
    return out


# ---------------------------------------------------------------- theme
THEME = 'Theme_VIVID_MUSIC_Base'
THEME_ITEMS = {
    'colorNPBButton': '@color/colorPrimary',          # mini-player buttons: white at night, black by day
    'tab_text_color_selected': '@color/colorPrimary',  # navigation: no green text
    'progress_bar_progress': '@color/colorPrimary',
    'now_playing_rest_time_text_color': '@color/colorSecondary',
    'now_playing_bar_bg': '@drawable/hu_now_playing_bar_bg',
    'now_playing_page_bg': '@drawable/hu_now_playing_page_bg',
    'queue_bg_color': '@color/hu_row_bg',            # queue rows stand out from the panel by day
    'now_playing_cover_bg': '@color/hu_row_current',  # the current row and cover placeholders
}


def theme():
    for folder in ('values', 'values-v31'):
        path = RES / folder / 'styles.xml'
        style = next((s for s in ET.parse(path).getroot().findall('style') if s.get('name') == THEME), None)
        if style is None:
            continue
        seen = set()
        for item in style.findall('item'):
            if item.get('name') in THEME_ITEMS:
                item.text = THEME_ITEMS[item.get('name')]
                seen.add(item.get('name'))
        missing = set(THEME_ITEMS) - seen
        if missing:
            raise SystemExit('restyle: theme items not found: ' + ', '.join(sorted(missing)))
        ET.SubElement(style, 'item', {'name': 'android:fontFamily'}).text = FONT
        extra = text_styles() if folder == 'values' else []
        write(RES / car(folder) / 'styles.xml', resources([ET.tostring(style, encoding='unicode').strip()] + extra))


# ---------------------------------------------------------------- dimensions
DIMENS = {
    'media_cover_item_radius': '8.0dip',
    'now_playing_bar_cover_radius': '8.0dip',
    'now_playing_page_cover_radius': '12.0dip',
    'base_list_item_radius': '6.0dip',
    'fragment_details_list_img_radius': '10.0dip',
    'font_size_caption': '16.0sp',
    'small_radius': '12.0dip',
    # The expanded player covers the content card exactly instead of sitting inside it.
    'now_playing_page_padding': '0.0dip',
    'now_playing_bar_height': '112.0dip',
    'medium_radius': '14.0dip',
    # The title and artist move under the player cover, so the cover gets smaller.
    'now_playing_page_cover_size': lambda v: scaled(v, 0.9),
    'now_playing_page_cover_margin_top': lambda v: scaled(v, 0.85),
}


def scaled(value, factor):
    m = re.fullmatch(r'(-?[\d.]+)([a-z]+)', value or '')
    return '%.1f%s' % (float(m[1]) * factor, m[2]) if m else value


def dimens():
    found = set()
    for folder in sorted(d.name for d in RES.iterdir() if d.is_dir() and d.name.startswith('values')):
        if 'car' in folder.split('-'):
            continue
        path = RES / folder / 'dimens.xml'
        if not path.exists():
            continue
        items = ['<dimen name="%s">%s</dimen>' % (d.get('name'), DIMENS[d.get('name')](d.text) if callable(DIMENS[d.get('name')]) else DIMENS[d.get('name')])
                 for d in ET.parse(path).getroot().findall('dimen') if d.get('name') in DIMENS]
        if items:
            found.update(re.search(r'name="([^"]+)"', i)[1] for i in items)
            target = RES / car(folder) / 'hu_dimens.xml'
            write(target, resources(items))
    if set(DIMENS) - found:
        raise SystemExit('restyle: dimensions not found: ' + ', '.join(sorted(set(DIMENS) - found)))


# ---------------------------------------------------------------- layouts
HORIZONTAL = re.compile(r'^layout_constraint(Start|End|Left|Right)_to\w+$|^layout_constraintHorizontal_\w+$')
VERTICAL = re.compile(r'^layout_constraint(Top|Bottom|Baseline)_to\w+$|^layout_constraintVertical_\w+$')
MARGINS_H = ('layout_marginStart', 'layout_marginEnd', 'layout_marginLeft', 'layout_marginRight')


def by_id(root):
    return {e.get(A + 'id', '').split('/')[-1]: e for e in root.iter() if e.get(A + 'id')}


def strip(e, pattern, margins=()):
    for k in list(e.attrib):
        local = k.split('}')[-1]
        if (k.startswith(P) and pattern.match(local)) or (k.startswith(A) and local in margins):
            del e.attrib[k]


def link(e, **constraints):
    for side, target in constraints.items():
        e.set(P + 'layout_constraint' + side, 'parent' if target == 'parent' else '@id/' + target)


def tint_like(ids):
    """The like icon comes from the Spotify backend and is always white; follow the text colour."""
    for child in ids.get('control_button_collect', []):
        if child.tag == 'ImageView':
            child.set(A + 'tint', '@color/colorPrimary')


def mini_player(root):
    """Controls first (closest to the driver in a left-hand-drive car), then like, cover and title."""
    ids = by_id(root)
    tint_like(ids)
    # A footer of the content card: no outer padding and no shadow (the shadow was clipped to a
    # rectangle around the card's rounded corner).
    root.set(A + 'padding', '0.0dip')
    for card in root.iter():
        if card.get(A + 'tag') == 'binding_1':
            card.set(A + 'elevation', '0.0dip')
            card.set(A + 'outlineProvider', 'background')
    order = [i for i in ('control_button_skip_to_previous', 'control_button_play', 'control_button_skip_to_next',
                         'control_button_collect', 'now_playing_bar_cover') if i in ids]
    if 'control_button_play' not in ids or 'title_layout' not in ids or 'now_playing_bar_cover' not in ids:
        raise SystemExit('restyle: unexpected now_playing_bar layout')
    previous = 'parent'
    for name in order:
        e = ids[name]
        strip(e, HORIZONTAL, MARGINS_H)
        if previous == 'parent':
            link(e, Start_toStartOf='parent')
            e.set(A + 'layout_marginStart', '8.0dip')
        else:
            link(e, Start_toEndOf=previous)
        previous = name
    ids['now_playing_bar_cover'].set(A + 'layout_marginStart', '16.0dip')
    title = ids['title_layout']
    strip(title, HORIZONTAL, MARGINS_H)
    link(title, Start_toEndOf='now_playing_bar_cover', End_toEndOf='parent')
    title.set(A + 'layout_width', '0.0dip')
    title.set(A + 'layout_marginStart', '20.0dip')
    title.set(A + 'layout_marginEnd', '24.0dip')


def player(root):
    """Title and artist move under the cover; like joins the end of the control row."""
    ids = by_id(root)
    tint_like(ids)
    need = ('cover', 'title', 'subtitle', 'seek_bar', 'time_info_layout', 'bottom_menu')
    if any(n not in ids for n in need):
        return False
    title, subtitle = ids['title'], ids['subtitle']
    for e in (title, subtitle):
        strip(e, HORIZONTAL, MARGINS_H)
        strip(e, VERTICAL, ('layout_marginTop',))
        link(e, Left_toLeftOf='cover', Right_toRightOf='cover')
        e.set(A + 'layout_width', '0.0dip')
    link(title, Top_toBottomOf='cover')
    title.set(A + 'layout_marginTop', '20.0dip')
    title.set(A + 'minHeight', '0.0dip')          # the Classic title is a tall block aligned to its bottom
    for child in title:
        child.set(A + 'layout_gravity', 'start|top')
    link(subtitle, Top_toBottomOf='title')
    subtitle.set(A + 'layout_marginTop', '6.0dip')
    # The progress, time and control rows form a packed chain centred on the cover.
    seek, times, menu = ids['seek_bar'], ids['time_info_layout'], ids['bottom_menu']
    for e in (seek, times, menu):
        strip(e, VERTICAL, ('layout_marginTop',))
    link(seek, Top_toTopOf='cover', Bottom_toTopOf='time_info_layout')
    seek.set(P + 'layout_constraintVertical_chainStyle', 'packed')
    link(times, Top_toBottomOf='seek_bar', Bottom_toTopOf='bottom_menu')
    times.set(A + 'layout_marginTop', '8.0dip')
    link(menu, Top_toBottomOf='time_info_layout', Bottom_toBottomOf='cover')
    menu.set(A + 'layout_marginTop', '28.0dip')
    if 'control_button_collect' in ids:
        like = ids['control_button_collect']
        strip(like, HORIZONTAL, MARGINS_H)
        strip(like, VERTICAL, ('layout_marginTop',))
        link(like, Top_toTopOf='bottom_menu', Bottom_toBottomOf='bottom_menu', Right_toLeftOf='cover')
        like.set(A + 'layout_width', '@dimen/now_playing_page_button_click_area')
        like.set(A + 'layout_height', '@dimen/now_playing_page_button_click_area')
        like.set(A + 'layout_marginRight', '@dimen/now_playing_page_left_view_margin_end')
        for k in [k for k in menu.attrib if k.endswith('layout_constraintRight_toLeftOf') or k.endswith('layout_constraintEnd_toStartOf')]:
            del menu.attrib[k]
        link(menu, Right_toLeftOf='control_button_collect')
        menu.set(A + 'layout_marginEnd', '8.0dip')
    if 'control_button_speed' in ids and 'playlist' in ids:
        speed = ids['control_button_speed']
        strip(speed, HORIZONTAL, MARGINS_H)
        strip(speed, VERTICAL, ('layout_marginTop',))
        link(speed, Left_toRightOf='playlist', Top_toTopOf='playlist', Bottom_toBottomOf='playlist')
    return True


SETTINGS = {'values': 'Settings', 'values-uk': 'Налаштування', 'values-ru': 'Настройки', 'values-ar': 'الإعدادات',
            'values-zh': '设置', 'values-zh-rCN': '设置', 'values-zh-rHK': '設定', 'values-zh-rMO': '設定', 'values-zh-rTW': '設定'}


def strings():
    for folder, text in SETTINGS.items():
        if folder == 'values' or (RES / folder).is_dir():
            write(RES / folder / 'hu_strings.xml', resources(['<string name="hu_settings">%s</string>' % text]))


def library_header(root):
    """The Library title opens the settings: show it as a settings button."""
    ids = by_id(root)
    if 'iv_logo' not in ids or 'tv_name' not in ids:
        return False
    logo, name = ids['iv_logo'], ids['tv_name']
    logo.set(A + 'src', '@drawable/hu_ic_settings')
    logo.set(A + 'background', '@drawable/hu_round_button')
    logo.set(A + 'scaleType', 'centerInside')
    logo.set(A + 'contentDescription', '@string/hu_settings')
    name.set(A + 'text', '@string/hu_settings')
    return True


def equaliser():
    """The day equaliser is black and disappears on the grey current row; use the green one."""
    source = RES / 'raw-night/playing_animation.gif'
    if not source.exists():
        raise SystemExit('restyle: raw-night/playing_animation.gif not found')
    (RES / 'raw-car').mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, RES / 'raw-car/playing_animation.gif')


def layouts():
    counts = {}
    for name, transform in (('now_playing_bar', mini_player), ('fragment_now_playing', player), ('fragment_category', library_header)):
        for path in sorted(RES.glob('layout*/%s.xml' % name)):
            if 'car' in path.parent.name.split('-'):
                continue
            tree = ET.parse(path)
            if transform(tree.getroot()) is False:
                continue
            target = RES / car(path.parent.name) / path.name
            target.parent.mkdir(parents=True, exist_ok=True)
            tree.write(target, encoding='utf-8', xml_declaration=True)
            counts[name] = counts.get(name, 0) + 1
    if counts.get('now_playing_bar', 0) < 7 or counts.get('fragment_now_playing', 0) < 7 or counts.get('fragment_category', 0) < 7:
        raise SystemExit('restyle: expected seven layout variants each, got %s' % counts)
    return counts


if __name__ == '__main__':
    clean()
    fonts()
    strings()
    equaliser()
    colors()
    drawables()
    theme()
    dimens()
    counts = layouts()
    print('Restyle: New style resources (%d mini-player and %d player layouts)' % (counts['now_playing_bar'], counts['fragment_now_playing']))

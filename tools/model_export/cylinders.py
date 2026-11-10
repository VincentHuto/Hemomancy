"""Export the cylinder meshes, 16x16 atlases and editable Blockbench models."""

import argparse
import json

import numpy as np
from PIL import Image, ImageDraw

from export_scriptorium_upgrades import ASSETS, ROOT, blockbench

ITEM_MODELS = ASSETS / 'models/item'
TEXTURES = ASSETS / 'textures/item'
DISPLAY = {
    'gui': {'rotation': [26, 34, 12], 'scale': [1.43] * 3},
    'ground': {'translation': [0, 2, 0], 'scale': [.7] * 3},
    'fixed': {'scale': [1.2] * 3},
    'thirdperson_righthand': {'translation': [0, 2, 0], 'scale': [.8] * 3},
    'firstperson_righthand': {'rotation': [0, 30, 0], 'translation': [0, 2, 0]},
}

# The left 12x6 pixels are the recording surface. The right four columns
# wrap the collars. Below those tiles, an 8x8 stamp maps across each end.
ART = {
    'wax_cylinder': (
        'a85b23 c77a28 de9838 f1b94f f8d575 754121 483027 9a582b',
        (
            '3323344333222332',
            '2233433322333443',
            '2333332233323443',
            '3322223333222332',
            '2223333222331221',
            '1122222332221221',
        ),
        (
            '11233211',
            '12344321',
            '23455432',
            '34566543',
            '34566543',
            '23455432',
            '12344321',
            '11233211',
        ),
        ('1', '4'),
    ),
    'ambergris_cylinder': (
        '652c2c 8d4236 b46748 d7915c efbc7d 381f29 221924 6e5360 9b7578',
        (
            '2334454332247857',
            '3433543323435875',
            '3324322434325695',
            '2312234553435695',
            '3223434332235875',
            '2122331223126756',
        ),
        (
            '66788766',
            '67899876',
            '78344387',
            '89577598',
            '89577598',
            '78344387',
            '67899876',
            '66788766',
        ),
        ('1', '5'),
    ),
    'corpse_wax_cylinder': (
        '663635 9e5d55 888363 a9a582 d0c9a4 e9dfba 3c262b 70514d c7a582',
        (
            '5566544554438538',
            '4555445643519658',
            '4335455421136758',
            '3445112224456588',
            '3112443544558758',
            '2144334433438987',
        ),
        (
            '88344388',
            '83466438',
            '34788743',
            '46877864',
            '46877864',
            '34788743',
            '83466438',
            '88344388',
        ),
        ('7', '6'),
    ),
}

# Each section gives its x bounds, radius and side UV tile. Keep the maximum
# radius at 2.5 and the spindle length at 11 pixels for the two machine mounts.
BODY = [0, 0, 12, 6]
COLLAR = [12, 0, 16, 6]
PROFILES = {
    'wax_cylinder': [
        ('turned shoulder', 2.5, 3.25, 2.25, COLLAR),
        ('beeswax', 3.25, 12.75, 2.5, BODY),
        ('turned shoulder', 12.75, 13.5, 2.25, COLLAR),
    ],
    'ambergris_cylinder': [
        ('hematic collar', 2.5, 3.5, 2.5, COLLAR),
        ('mnemonic resin', 3.5, 12.5, 2.25, BODY),
        ('hematic collar', 12.5, 13.5, 2.5, COLLAR),
    ],
    'corpse_wax_cylinder': [
        ('bound cuff', 2.5, 3.75, 2.5, COLLAR),
        ('adipocere', 3.75, 12.75, 2.25, BODY),
        ('knotted cuff', 12.75, 13.5, 2.5, COLLAR),
    ],
}


def atlas(medium, recorded):
    colors, side, cap, (groove, highlight) = ART[medium]
    palette = [tuple(bytes.fromhex(rgb)) for rgb in colors.split()]
    image = Image.new('RGB', (16, 16), palette[0])
    for y, row in enumerate(side):
        for x, ink in enumerate(row):
            if recorded and x in (2, 5, 8, 10):
                ink = groove if y < 5 else highlight
            image.putpixel((x, y), palette[int(ink) - 1])
    for y, row in enumerate(cap, 8):
        for x, ink in enumerate(row):
            image.putpixel((x, y), palette[int(ink) - 1])
    return image


def face(uv):
    return {'texture': '#skin', 'uv': [round(value, 2) for value in uv]}


def geometry(medium):
    elements = []
    profile = PROFILES[medium]
    for i, (name, left, right, radius, side_uv) in enumerate(profile):
        heights = [-radius, .5 - radius, 1.5 - radius, radius - 1.5, radius - .5, radius]
        widths = [radius - 1.5, radius - .75, radius, radius - .75, radius - 1.5]
        u0, v0, u1, v1 = side_uv
        for j, half_width in enumerate(widths):
            bottom, top = heights[j:j + 2]
            strip = [u0, v0 + (v1 - v0) * (radius - top) / (2 * radius),
                     u1, v0 + (v1 - v0) * (radius - bottom) / (2 * radius)]
            faces = {direction: face(strip) for direction in ('north', 'south')}
            if j >= 2:
                faces['up'] = face(side_uv)
            if j <= 2:
                faces['down'] = face(side_uv)
            # One cap illustration spans the stepped cross section. Render the inner
            # shoulder only on the wider section to avoid coplanar end faces.
            cap_uv = [4 - half_width * 1.6, 12 - top * 1.6,
                      4 + half_width * 1.6, 12 - bottom * 1.6]
            if i == 0 or radius > profile[i - 1][3]:
                faces['west'] = face(cap_uv)
            if i == len(profile) - 1 or radius > profile[i + 1][3]:
                faces['east'] = face(cap_uv)
            elements.append({'name': name, 'from': [left, 8 + bottom, 8 - half_width],
                             'to': [right, 8 + top, 8 + half_width], 'faces': faces})
    return elements


def model(medium):
    skin = 'hemomancy:item/' + medium
    return {'parent': 'minecraft:block/block', 'ambientocclusion': False,
            'textures': {'skin': skin, 'particle': skin}, 'elements': geometry(medium)}


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')


def export():
    for medium in PROFILES:
        skin = 'hemomancy:item/' + medium
        atlas(medium, False).save(TEXTURES / f'{medium}.png')
        atlas(medium, True).save(TEXTURES / f'{medium}_recorded.png')
        blank = model(medium)
        write(ITEM_MODELS / 'cylinder' / f'{medium}.json', blank)
        write(ITEM_MODELS / 'cylinder' / f'{medium}_recorded.json',
              {'parent': 'hemomancy:item/cylinder/' + medium,
               'textures': {'skin': skin + '_recorded'}})
        write(ITEM_MODELS / f'{medium}.json', {'parent': 'minecraft:builtin/entity',
              'textures': {'particle': skin}, 'display': DISPLAY})
        write(ITEM_MODELS / 'bbmodel' / f'{medium}.bbmodel',
              blockbench(medium, dict(blank, display=DISPLAY)))


def preview():
    from clairaudiograph import render

    sheet = Image.new('RGB', (780, 460), (30, 24, 28))
    draw = ImageDraw.Draw(sheet)
    for col, medium in enumerate(PROFILES):
        draw.text((col * 260 + 12, 10), medium.replace('_', ' ').title(), fill='white')
        for row, recorded in enumerate((False, True)):
            skin = np.array(atlas(medium, recorded).convert('RGBA'))
            tile = render(geometry(medium), {'skin': skin}, -34 if not recorded else 34,
                          pitch=26, size=220, scale=15)
            sheet.paste(tile, (col * 260 + 20, 20 + row * 220))
    path = ROOT / 'build/model-previews/cylinders.png'
    path.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(path)
    print(path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export()
    if args.preview:
        preview()

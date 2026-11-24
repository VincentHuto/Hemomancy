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

# Atlas layout: the left 12x6 pixels are the recording surface (rows run around
# the circumference, top edge to bottom edge), the right four columns wrap the
# collars, and an 8x8 stamp below them maps across each end. Rows 0 and 5 sit at
# the silhouette, so they carry the shadow and rows 1-2 the sheen.
MEDIA = {
    'wax_cylinder': {
        # Turned honey wax: a soft sheen, a few drip specks, darker turned shoulders.
        'body': ['a8661f', 'e4a640', 'f6cd6a', 'e9b04c', 'd9952f', 'a8661f'],
        'speck': 'fbe08a', 'vein': None,
        'collar': ['7a4418', 'b8762a', 'd9952f', 'b8762a', '7a4418', '5e3412'],
        'collar_mark': None,
        'cap': ['b8762a', 'd9952f', 'e9b04c', '7a4418', '2a1608'],
    },
    'ambergris_cylinder': {
        # Mottled Mnemonic Ambergris set in hematic-iron collars with a red rivet.
        'body': ['6a4632', 'a8784e', 'd4a66a', 'c08e5c', '966a46', '6a4632'],
        'speck': 'e8c890', 'vein': '4a2e2a',
        'collar': ['1e161a', '4c3e44', '5e4e56', '3a2e34', '2a2026', '160f12'],
        'collar_mark': ('9a1820', 'rivet'),
        'cap': ['3a2e34', '5e4e56', 'c08e5c', '966a46', '160f12'],
    },
    'corpse_wax_cylinder': {
        # Pressed adipocere, greasy and pale, stitched into hematic-iron cuffs.
        'body': ['8a7e60', 'd6cca6', 'ece2be', 'd8ce9e', 'bdb38c', '8a7e60'],
        'speck': 'e4d690', 'vein': None,
        'collar': ['1e161a', '4c3e44', '5e4e56', '3a2e34', '2a2026', '160f12'],
        'collar_mark': ('d8cfb0', 'stitch'),
        'cap': ['3a2e34', '5e4e56', 'ece2be', 'bdb38c', '2a1a14'],
    },
}


def rgb(hex_value):
    return tuple(bytes.fromhex(hex_value))


def shade(color, amount):
    return tuple(max(0, min(255, round(channel * amount))) for channel in color)
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
    art = MEDIA[medium]
    rng = np.random.default_rng(sum(map(ord, medium)))
    image = Image.new('RGB', (16, 16), rgb(art['body'][0]))
    # Recording surface: one shaded row per band of the circumference, lightly mottled.
    for y, base in enumerate(art['body']):
        for x in range(12):
            color = shade(rgb(base), 1 + rng.uniform(-.03, .03))
            if 1 <= y <= 4 and rng.random() < .035:
                color = rgb(art['speck'])
            image.putpixel((x, y), color)
    if art['vein']:
        # A few short veins wandering along the body.
        for start, row in ((2, 3), (8, 2)):
            for step in range(3):
                image.putpixel((start + step, min(5, max(0, row + (step == 1) - (step == 2)))), rgb(art['vein']))
    if medium == 'corpse_wax_cylinder':
        # The pressed seam along the top, stitched shut.
        for x in range(12):
            if x % 3 == 1:
                image.putpixel((x, 0), rgb('d8cfb0'))
    if recorded:
        # Fine grooves cut round the body by the stylus, each with a lit land beside it.
        for y in range(6):
            for x in range(1, 12):
                if x % 2 == 1:
                    image.putpixel((x, y), shade(rgb(art['body'][y]), .74))
                elif 1 <= y <= 2:
                    image.putpixel((x, y), shade(rgb(art['body'][y]), 1.1))
    # Collar tile: turned or iron bands, with rivets or cross-stitching.
    for y, base in enumerate(art['collar']):
        for x in range(12, 16):
            image.putpixel((x, y), shade(rgb(base), 1.08 if x == 13 else .9 if x == 15 else 1))
    if art['collar_mark']:
        mark, kind = art['collar_mark']
        if kind == 'rivet':
            for x, y in ((13, 2), (15, 4)):
                image.putpixel((x, y), rgb(mark))
                image.putpixel((x - 1, y), shade(rgb(mark), .6))
        else:
            for x, y in ((12, 1), (14, 1), (13, 2), (12, 3), (14, 3), (13, 4)):
                image.putpixel((x, y), rgb(mark))
    # End stamp: outer rim, body, inner ring and the mandrel bore at the centre.
    rim, body, light, ring, bore = (rgb(value) for value in art['cap'])
    for y in range(8):
        for x in range(8):
            distance = max(abs(x - 3.5), abs(y - 3.5)) + .35 * min(abs(x - 3.5), abs(y - 3.5))
            if distance > 3.6:
                color = shade(rim, .8)
            elif distance > 3:
                color = rim
            elif distance > 2.2:
                color = light if x + y < 7 else body
            elif distance > 1.3:
                color = ring
            else:
                color = bore
            image.putpixel((x, y + 8), color)
    return image


def face(uv):
    return {'texture': '#skin', 'uv': [round(value, 2) for value in uv]}


def geometry(medium):
    elements = []
    profile = PROFILES[medium]
    for i, (name, left, right, radius, side_uv) in enumerate(profile):
        heights = [-radius, .5 - radius, 1.5 - radius, radius - 1.5, radius - .5, radius]
        widths = [radius - 1.5, radius - .75, radius, radius - .75, radius - 1.5]
        # One texel per model unit: each face takes a face-sized window of its tile.
        u0, v0, u1, v1 = side_uv
        u_end = u0 + min(right - left, u1 - u0)
        for j, half_width in enumerate(widths):
            bottom, top = heights[j:j + 2]
            strip = [u0, v0 + radius - top, u_end, v0 + radius - bottom]
            faces = {direction: face(strip) for direction in ('north', 'south')}
            flat = [u0, v0, u_end, v0 + min(2 * half_width, v1 - v0)]
            if j >= 2:
                faces['up'] = face(flat)
            if j <= 2:
                faces['down'] = face(flat)
            # One cap illustration spans the stepped cross section. Render the inner
            # shoulder only on the wider section to avoid coplanar end faces.
            cap_uv = [4 - half_width, 12 - top, 4 + half_width, 12 - bottom]
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

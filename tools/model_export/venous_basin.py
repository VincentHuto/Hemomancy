"""Author the Venous Basin and the Blood Basin: Hemorath's standing basins (basins and channels).

Venous Basin (`hemomancy:venous_basin`, properties `facing` and `fill` 0-3): a squat octagonal bowl of polished
venous stone with a thick rolled rim, banded twice in hematic iron (bone bosses on the upper band), on a short
stepped pedestal with a bone collar. On its facing side a pouring spout: a notch through the rim over an
iron-walled trough carried on a bone corbel, where a gutter can meet it. Fill as the Blood Gutter: 0 dry (a dark
stain ring on the floor, a tide line on the walls), 1 stained (a dark tacky film), 2 half (a blood surface
halfway up), 3 full (just under the rim, standing in the notch and the spout).

Blood Basin (`hemomancy:blood_basin`, no properties): the grand form that holds a Saint's refill in Hemorath's
chamber. A broad octagonal bowl that fills the cell: three hematic iron bands, crimson lacquer panels framed in
bone on its four faces, bone ribs up its four corners, a wide rim, a stepped plinth gripped by four bone talons,
and always brimming.

The bowls are true octagons: four axis-aligned walls and four turned 45 degrees, meeting cleanly. The blood is
the animated hemomancy:block/blood_still; everything else is painted into the `venous_basin` atlas in the venous
stone, hematic iron, bone and lacquer of the mausoleum set pieces. Authored for `facing=north` with the spout
toward -z. 16 px per block.

Emits venous_basin_<0..3>, blood_basin, the atlas, both blockstates, models/item/venous_basin.json, Blockbench
projects and, with `--preview`, build/model-previews/venous_basin.png and blood_basin.png.
Python 3 + Pillow + NumPy.
"""
import argparse
import json
import math

import numpy as np
from PIL import Image, ImageDraw

import blood_gutter as bg
from clairaudiograph import clamp, noise, render
from export_scriptorium_upgrades import texture_image
from fixture_atlas import ASSETS, PALETTE, ROOT, box, build, separate, spin, write
from reliquary_sconce import bounds

ATLAS = 'venous_basin'
TEXTURES = {'blood': 'hemomancy:block/blood_still'}
PARTICLE = 'hemomancy:block/polished_venous_stone'
TAN = math.tan(math.radians(22.5))

VENOUS = (112, 10, 18)          # polished venous stone's mid tone, a touch cooler
VENOUS_HI = (156, 20, 30)
VENOUS_IN = (66, 6, 12)
VENOUS_FLOOR = (84, 10, 16)   # the bowl's floor, scraped paler than its walls
BONE = (200, 184, 152)          # aged bone
LACQUER = PALETTE['lacquer']


# --- octagons -----------------------------------------------------------------------------------

def half_side(apothem):
    return apothem * TAN


def wall(name, side, R, r, y0, y1, mat, inner=None, span=None):
    """One straight side of an octagon (`side` n/e/s/w, or nw/ne/sw/se for the diagonals) from apothem r out to R.
    `span` cuts an axis wall to (from, to) along its length, measured from the block centre."""
    a = half_side(R)
    t0, t1 = span or (-a, a)
    base = {'n': ((8 + t0, y0, 8 - R), (8 + t1, y1, 8 - r), 'south'),
            's': ((8 + t0, y0, 8 + r), (8 + t1, y1, 8 + R), 'north'),
            'w': ((8 - R, y0, 8 + t0), (8 - r, y1, 8 + t1), 'east'),
            'e': ((8 + r, y0, 8 + t0), (8 + R, y1, 8 + t1), 'west')}
    axis = {'nw': ('n', 45), 'ne': ('n', -45), 'se': ('s', 45), 'sw': ('s', -45)}
    faces = {}
    if side in base:
        lo, hi, face_in = base[side]
        if inner:
            faces[face_in] = inner
        return box(name, lo, hi, mat, **faces)
    src, angle = axis[side]
    lo, hi, face_in = base[src]
    if inner:
        faces[face_in] = inner
    return box(name, lo, hi, mat, rotation=spin('y', angle, (8, (y0 + y1) / 2, 8)), **faces)


SIDES = ('n', 'e', 's', 'w', 'nw', 'ne', 'sw', 'se')


def oct_ring(name, R, r, y0, y1, mat, inner=None, skip=()):
    return [wall(name, s, R, r, y0, y1, mat, inner) for s in SIDES if s not in skip]


def oct_solid(name, R, y0, y1, mat):
    """A solid octagonal slab: two crossing strips and four diagonal walls deep enough to fill the corners."""
    a = half_side(R)
    e = [box(name, (8 - a, y0, 8 - R), (8 + a, y1, 8 + R), mat),
         box(name, (8 - R, y0, 8 - a), (8 - a, y1, 8 + a), mat, skip=('east',)),
         box(name, (8 + a, y0, 8 - a), (8 + R, y1, 8 + a), mat, skip=('west',))]
    for s in ('nw', 'ne', 'sw', 'se'):
        e.append(wall(name, s, R, R * .55, y0, y1, mat))
    return e


def square(name, half, y0, y1, mat, only=('up',)):
    return box(name, (8 - half, y0, 8 - half), (8 + half, y1, 8 + half), mat,
               skip=[f for f in ('north', 'east', 'south', 'west', 'up', 'down') if f not in only])


# --- the venous basin ---------------------------------------------------------------------------

R_BOWL, R_IN, R_BAND, R_RIM = 6.0, 4.5, 6.5, 6.75
FLOOR_Y, RIM_Y = 8, 14
NOTCH = (7, 9)                  # the notch's x span; the spout trough is one pixel wider each side
NOTCH_Y = 12
LEVELS = {2: 11.0, 3: 13.0}


def venous_body():
    e = []
    e += oct_solid('plinth', 6, 0, 1, 'stone')
    e += oct_solid('plinth_step', 5, 1, 2, 'stone')
    e += oct_solid('stem', 2.5, 2, 4, 'stone')
    e += oct_solid('collar', 3.5, 4, 5, 'bone')
    e += oct_solid('underside', 5, 5, 6, 'stone')
    # The bowl: its foot, the floor (corners hidden in the low band), the walls and the rolled rim.
    e.append(square('bowl_base', R_BOWL / math.sqrt(2) - .02, 6, 7, 'stone', only=('down',)))
    e.append(square('floor', R_IN, 7, FLOOR_Y, 'stone_in', only=('up',)))
    e += oct_ring('wall', R_BOWL, R_IN, 6, NOTCH_Y, 'stone', inner='stone_in')
    e += oct_ring('band_low', R_BAND, R_BOWL, 7, 8, 'iron')
    a_band = half_side(R_BAND)
    e += oct_ring('band_high', R_BAND, R_BOWL, NOTCH_Y - 1, NOTCH_Y, 'iron', skip=('n',))
    e.append(wall('band_high', 'n', R_BAND, R_BOWL, NOTCH_Y - 1, NOTCH_Y, 'iron', span=(-a_band, NOTCH[0] - 9)))
    e.append(wall('band_high', 'n', R_BAND, R_BOWL, NOTCH_Y - 1, NOTCH_Y, 'iron', span=(NOTCH[1] - 7, a_band)))
    # Above the notch floor the north wall and the rim part around the notch.
    a = half_side(R_BOWL)
    e += oct_ring('wall_top', R_BOWL, R_IN, NOTCH_Y, RIM_Y - 1, 'stone', inner='stone_in', skip=('n',))
    for span in ((-a, NOTCH[0] - 8), (NOTCH[1] - 8, a)):
        e.append(wall('wall_top', 'n', R_BOWL, R_IN, NOTCH_Y, RIM_Y - 1, 'stone', inner='stone_in', span=span))
    a_rim = half_side(R_RIM)
    e += oct_ring('rim', R_RIM, R_IN, RIM_Y - 1, RIM_Y, 'rim', inner='stone_in', skip=('n',))
    for span in ((-a_rim, NOTCH[0] - 8), (NOTCH[1] - 8, a_rim)):
        e.append(wall('rim', 'n', R_RIM, R_IN, RIM_Y - 1, RIM_Y, 'rim', inner='stone_in', span=span))
    # Bone bosses riveting the high band on the three sides away from the spout.
    for s in ('e', 's', 'w'):
        e.append(wall('boss', s, R_BAND + .5, R_BAND, NOTCH_Y - 1 + .25, NOTCH_Y - .25, 'bone', span=(-.5, .5)))
    e += spout()
    return e


def spout():
    """The pouring lip: an iron-walled trough out through the notch, carried on a bone corbel."""
    x0, x1 = NOTCH
    z_in = 8 - R_IN
    return [box('spout_bed', (x0 - 1, NOTCH_Y - 1, 0), (x1 + 1, NOTCH_Y, z_in), 'iron', skip=('south',)),
            box('spout_wall', (x0 - 1, NOTCH_Y, 0), (x0, NOTCH_Y + 1, 8 - R_RIM), 'iron', skip=('south',)),
            box('spout_wall', (x1, NOTCH_Y, 0), (x1 + 1, NOTCH_Y + 1, 8 - R_RIM), 'iron', skip=('south',)),
            box('spout_tip', (x0 - 1, NOTCH_Y - 1.5, 0), (x1 + 1, NOTCH_Y - 1, .75), 'iron', skip=('up',)),
            box('corbel', (x0, NOTCH_Y - 3, .75), (x1, NOTCH_Y - 1, 8 - R_BOWL), 'bone', skip=('south', 'up')),
            box('corbel_toe', (x0, NOTCH_Y - 4, 1.25), (x1, NOTCH_Y - 3, 8 - R_BOWL), 'bone', skip=('south', 'up'))]


def venous_fill(level):
    if level == 0:
        return [square('stain', R_IN, FLOOR_Y + .1, FLOOR_Y + .11, 'stain')]
    if level == 1:
        return [square('film', R_IN, FLOOR_Y + .1, FLOOR_Y + .11, 'film')]
    out = [square('surface', R_IN, LEVELS[level] - .01, LEVELS[level], 'blood')]
    if level == 3:
        x0, x1 = NOTCH
        # Brimming: the blood stands in the notch and runs out along the spout to its lip.
        out.append(box('spout_blood', (x0, NOTCH_Y, 0), (x1, NOTCH_Y + .6, 8 - R_IN), 'blood',
                       skip=('down', 'east', 'west', 'south')))
    return out


# --- the blood basin ----------------------------------------------------------------------------

G_FOOT, G_BOWL, G_IN, G_BAND = 6.25, 7.25, 5.25, 7.75
G_FLOOR, G_RIM, G_LEVEL = 7, 15, 14.5


def grand_body():
    e = []
    e += oct_solid('plinth', 7.75, 0, 1, 'stone')
    e += oct_solid('plinth_step', 6.75, 1, 2, 'stone')
    e += oct_solid('stem', 4, 2, 3, 'stone')
    e += oct_solid('collar', 5, 3, 4, 'iron')
    e += oct_solid('underside', 6, 4, 5, 'stone')
    # A bowl that flares in two tiers: a narrower foot course, then the broad upper bowl under a wide rim.
    e.append(square('bowl_base', G_FOOT / math.sqrt(2) - .02, 5, 6, 'stone', only=('down',)))
    e.append(square('floor', G_IN, 6, G_FLOOR, 'stone_in', only=('up',)))
    e += oct_ring('foot_wall', G_FOOT, G_IN, 5, 8, 'stone', inner='stone_in')
    e += oct_ring('band', G_FOOT + .5, G_FOOT, 5.5, 6.5, 'iron')
    e += oct_ring('wall', G_BOWL, G_IN, 8, G_RIM - 1, 'stone', inner='stone_in')
    e += oct_ring('band', G_BAND, G_BOWL - .75, 8, 9, 'iron')
    e += oct_ring('band', G_BAND, G_BOWL, 13, 14, 'iron')
    e += oct_ring('rim', G_BAND + .25, G_IN, G_RIM - 1, G_RIM, 'rim', inner='stone_in')
    # Lacquer panels framed in bone on the four faces, between the bands.
    for s in ('n', 'e', 's', 'w'):
        e.append(wall('panel', s, G_BOWL + .25, G_BOWL, 9, 13, 'panel', span=(-2.5, 2.5)))
    # Bone ribs up the four corners, from the foot band to the rim, each ending in a knuckle on the rim.
    for s in ('nw', 'ne', 'sw', 'se'):
        e.append(wall('rib', s, G_BAND + .5, G_BAND - .25, 9, G_RIM - .5, 'rib', span=(-.75, .75)))
        e.append(wall('rib_foot', s, G_FOOT + 1, G_FOOT + .25, 6.5, 9, 'rib', span=(-.6, .6)))
        e.append(wall('rib_cap', s, G_BAND + .75, G_BAND - .5, G_RIM - 1.25, G_RIM + .25, 'bone', span=(-1, 1)))
    # Four bone talons gripping the plinth's corners, their red-stained points hooked over its edge.
    for s in ('nw', 'ne', 'sw', 'se'):
        e.append(wall('talon', s, 7.25, 3.5, 2, 3.25, 'bone', span=(-.75, .75)))
        e.append(wall('talon_tip', s, 8.25, 6.75, .25, 2.5, 'talon', span=(-.6, .6)))
    return e


def grand_fill():
    return [square('surface', G_IN, G_LEVEL - .01, G_LEVEL, 'blood')]


# --- painters -----------------------------------------------------------------------------------

def shade(rgb, f):
    """Scale a colour: venous stone keeps its hue lit or shadowed, where adding light would turn it pink."""
    return tuple(max(0, min(255, int(c * f))) for c in rgb)


KEYS = {}           # atlas key -> the element's top y, for the interior tide line


def stone(face, lx, ly, w, h, key):
    """Polished venous stone: dark red with paler flecks, lit along top edges, shadowed underneath."""
    n = noise('stone', key, face, lx, ly)
    f = .9 + noise('tone', key, face, lx // 2, ly // 2) * .14 + n * .08
    if face == 'up':
        f += .14
    elif face == 'down':
        f -= .4
    elif ly == 0 and h > 1:
        f += .2
    elif ly == h - 1 and h > 2:
        f -= .16
    rgb = shade(VENOUS, f)
    if n > .93:
        rgb = VENOUS_HI
    elif n < .04:
        rgb = shade(VENOUS, .62)
    return rgb + (255,)


def rim(face, lx, ly, w, h, key):
    """The rolled rim: a polished top, its outer edge catching the light."""
    n = noise('rim', key, face, lx, ly)
    if face == 'up':
        rgb = shade(VENOUS, 1.16 + n * .1)
        if n > .9:
            rgb = VENOUS_HI
        return rgb + (255,)
    return shade(VENOUS, 1.24 + n * .08) + (255,)


def stone_in(face, lx, ly, w, h, key):
    """Inside the bowl: the stone in shadow, a blood-black tide line a little under the half mark."""
    n = noise('in', key, face, lx, ly)
    if face == 'up':
        return shade(VENOUS_FLOOR, .92 + n * .16) + (255,)
    rgb = shade(VENOUS_IN, 1.05 + n * .14 - ly * .3 / max(h, 1))
    top = KEYS.get(key)
    if face != 'up' and top is not None and 10 <= top - ly - .5 < 11 and n > .15:
        rgb = (36, 4, 8)
    return rgb + (255,)


def iron(face, lx, ly, w, h, key):
    """Hematic iron: a cold sheen on top, a rivet head every third texel along a band."""
    n = noise('iron', key, face, lx, ly)
    if face == 'up':
        rgb = clamp(PALETTE['iron'], 20 + int(n * 10) - 5)
    elif face == 'down':
        rgb = clamp(PALETTE['iron'], -26)
    else:
        rgb = clamp(PALETTE['iron'], int(n * 12) - 6)
        if h == 1 and lx % 3 == 1:
            rgb = clamp(PALETTE['ferric'], 8)
        elif h > 1 and ly == 0:
            rgb = clamp(PALETTE['iron'], 18)
    if noise('vein', key, face, lx, ly) < .05:
        rgb = (100, 24, 34)
    return rgb + (255,)


def bone(face, lx, ly, w, h, key):
    n = noise('bone', key, face, lx, ly)
    rgb = clamp(BONE, int(n * 14) - 7 + (10 if face == 'up' else -16 if face == 'down' else 0))
    if noise('stain', key, face, lx, ly // 2) < .1:
        rgb = (164, 116, 100)
    return rgb + (255,)


def rib(face, lx, ly, w, h, key):
    """A bone rib knuckled every three texels, stained red toward its foot."""
    n = noise('rib', key, face, lx, ly)
    if face in ('up', 'down'):
        return clamp(BONE, -12) + (255,)
    rgb = clamp(BONE, int(n * 10) - 5 - (18 if ly % 3 == 2 else 0))
    if ly >= h - 2:
        rgb = clamp((150, 70, 64), int(n * 10))
    return rgb + (255,)


def talon(face, lx, ly, w, h, key):
    return clamp((134, 50, 48), int(noise('talon', key, face, lx, ly) * 12) - 6 + (16 if ly == 0 else 0)) + (255,)


def panel(face, lx, ly, w, h, key):
    """Crimson lacquer framed in bone."""
    if face in ('up', 'down') or w < 3:
        return clamp(BONE, -16) + (255,)
    n = noise('panel', key, face, lx, ly)
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(BONE, int(n * 10) - 14) + (255,)
    if lx == w // 2 and ly == h // 2 - 1:
        return (196, 48, 58, 255)                      # a lacquer bead at the panel's heart
    return shade(LACQUER, 1.15 - ly * .12 + n * .06) + (255,)


def octagon_distance(dx, dz):
    return max(abs(dx), abs(dz), (abs(dx) + abs(dz)) / math.sqrt(2))


def stain(face, lx, ly, w, h, key):
    """A dry basin: a dark ring where the blood stood longest, flecks of crust inside it."""
    half = w / 2
    d = octagon_distance(lx + .5 - half, ly + .5 - half)
    n = noise('stain', lx, ly)
    if d > half - .05:
        return (0, 0, 0, 0)
    if d > half - 1.5:
        return ((30, 8, 8) if n > .3 else (46, 14, 12)) + (255,)
    if n > .84:
        return (40, 12, 10, 255)
    return (0, 0, 0, 0)


def film(face, lx, ly, w, h, key):
    """A dark tacky film over the floor, glossy in points."""
    half = w / 2
    d = octagon_distance(lx + .5 - half, ly + .5 - half)
    n = noise('film', lx, ly)
    if d > half - .05 or n < .08:
        return (0, 0, 0, 0)
    if n > .94:
        return (156, 36, 42, 255)
    return clamp((50, 4, 9), int(noise('tone', lx // 2, ly) * 14) - 7) + (255,)


SPECIAL = dict(stone=stone, rim=rim, stone_in=stone_in, iron=iron, bone=bone, rib=rib, talon=talon, panel=panel,
               stain=stain, film=film)


# --- output -------------------------------------------------------------------------------------

def model(elements, height):
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False,
            'texture_size': [128, height],
            'textures': dict({'atlas': f'hemomancy:block/{ATLAS}', 'particle': PARTICLE}, **TEXTURES),
            'elements': elements}


def venous_blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for level in range(4):
            v = {'model': f'hemomancy:block/venous_basin_{level}'}
            if y:
                v['y'] = y
            variants[f'facing={facing},fill={level}'] = v
    return {'variants': variants}


def preview_sheet(models, order, title, out_name, scale=15):
    tex = {'atlas': np.array(Image.open(ASSETS / f'textures/block/{ATLAS}.png').convert('RGBA')),
           'blood': np.array(texture_image(TEXTURES['blood']).convert('RGBA').crop((0, 0, 16, 16)))}
    views = [(30, 25), (-35, 50), (160, 25), (90, 8)]
    size = 300
    sheet = Image.new('RGBA', (size * 4, 30 + size * len(order)), (30, 24, 28, 255))
    draw = ImageDraw.Draw(sheet)
    for row, name in enumerate(order):
        for col, (yaw, pitch) in enumerate(views):
            sheet.paste(render(models[name], tex, yaw, pitch, size, scale), (size * col, 30 + size * row))
        draw.text((8, 34 + size * row), name, fill=(220, 200, 200))
    draw.text((10, 8), title, fill=(220, 200, 200))
    out = ROOT / f'build/model-previews/{out_name}.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(out)
    print(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    groups = {'venous': venous_body(), 'grand': grand_body(), 'grand_fill': grand_fill()}
    for level in range(4):
        groups[f'fill{level}'] = venous_fill(level)
    moved = separate(groups['venous']) + separate(groups['grand'])
    atlas_groups, world = bg.split(groups, set(TEXTURES))
    for g, elements in atlas_groups.items():
        for i, el in enumerate(elements):
            KEYS[(g, i)] = el['hi'][1]
    atlas, painted = build(atlas_groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    built = {g: world[g] + painted[g] for g in groups}

    models, outputs = {}, {}
    for level in range(4):
        models[f'venous_basin_{level}'] = built['venous'] + built[f'fill{level}']
    models['blood_basin'] = built['grand'] + built['grand_fill']
    for name, elements in models.items():
        outputs[f'block/{name}'] = model(elements, atlas.height)
    write({k: v for k, v in outputs.items() if 'venous' in k}, 'venous_basin', outputs['block/venous_basin_2'])
    write({'block/blood_basin': outputs['block/blood_basin']}, 'blood_basin', outputs['block/blood_basin'])
    item = {'parent': 'hemomancy:block/venous_basin_2'}
    (ASSETS / 'models/item/venous_basin.json').write_text(json.dumps(item, indent=2) + '\n')
    (ASSETS / 'blockstates/venous_basin.json').write_text(json.dumps(venous_blockstate(), indent=2) + '\n')
    (ASSETS / 'blockstates/blood_basin.json').write_text(
        json.dumps({'variants': {'': {'model': 'hemomancy:block/blood_basin'}}}, indent=2) + '\n')
    for name in ('venous_basin_3', 'blood_basin'):
        lo, hi = bounds(models[name])
        print(f'  {name}: {len(models[name])} cubes, bounds Block.box({lo[0]}, {lo[1]}, {lo[2]}, '
              f'{hi[0]}, {hi[1]}, {hi[2]}) for facing=north')
    print(f'Exported Venous Basin (4 fills) and Blood Basin: {moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        preview_sheet(models, [f'venous_basin_{n}' for n in range(4)],
                      'Venous Basin - fill 0 dry / 1 stained / 2 half / 3 full; spout faces north', 'venous_basin')
        preview_sheet(models, ['blood_basin'], 'Blood Basin - the grand basin, always full', 'blood_basin', 14)


if __name__ == '__main__':
    main()

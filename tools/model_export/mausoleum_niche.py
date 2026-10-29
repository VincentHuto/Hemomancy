"""Author the mausoleum niche fronts: `sealed_niche` and `opened_niche`, one shared atlas.

A loculus is a horizontal burial slot three blocks long cut into a crypt-gallery wall; each of its
three cells takes one front block, so a front tiles side by side. The block fills the opening cell in
the wall plane, its face toward the room (north, z = 0) and the recess behind it (south).

Both fronts share a bone frame flush with the wall face (z 0..2): a lintel with a row of tooth dentils
along its underside and slim vertebral posts at each cell edge, so three fronts in a row read as three
sealed plates parted by two-pixel bone mullions. The fronts carry their own materials (bone, Crimson
Lacquer, hematic iron) because they sit in every theme's wall.

- sealed_niche: a Crimson Lacquer sealing plate set back behind the frame (z 2..5), its lacquer
  cracked and flaking, crossed by a hematic-iron strap pinned with a seal boss of crimson wax, and a
  bone name tablet with its incised line of script.
- opened_niche: the frame with the plate broken out. Lacquer shards stay caught in the frame corners,
  a broken half of the tablet lies on a bone shelf lip along the bottom (y 0..2, z 0..4), and the middle
  is open so the loot barrel in the recess behind stays visible and clickable.

Python 3 + Pillow + NumPy. Writes the atlas, both block models, both facing blockstates and one
Blockbench project per block. `--preview` also writes turnarounds and three-in-a-row wall previews to
build/model-previews/.
"""
import argparse
import copy
import json

import numpy as np
from PIL import Image, ImageDraw

from clairaudiograph import clamp, noise, render
from fixture_atlas import ASSETS, PALETTE, ROOT, box, build, mirror_x, model_json, preview, separate, write

ATLAS = 'mausoleum_niche'
PARTICLE = f'hemomancy:block/{ATLAS}'

BONE = PALETTE['bone']
LACQUER = PALETTE['lacquer']
LACQUER_DEEP = PALETTE['lacquer_deep']
CRACK = (36, 10, 16)
UNDERCOAT = (118, 84, 76)      # the gesso under the lacquer where it has flaked away
INCISION = (126, 98, 86)
WAX = (150, 18, 28)


def pair(el):
    return [el, mirror_x(el)]


# --- geometry ----------------------------------------------------------------------------------

def frame(sill):
    """The bone frame shared by both fronts. Back faces are skipped where the sealing plate covers them."""
    back = ('south',) if sill else ()
    e = [box('lintel', (0, 14, 0), (16, 16, 2), 'bone_frame', skip=back)]
    for x in (2.5, 4.5, 6.5):
        e += pair(box('dentil', (x, 13, 0), (x + 1, 14, 1.5), 'bone', skip=('up',) + back))
    bottom = 1.5 if sill else 2
    e += pair(box('post', (0, bottom, 0), (1, 14, 2), 'post', skip=back))
    if sill:
        e.append(box('sill', (0, 0, 0), (16, 1.5, 2), 'bone_frame', skip=back))
    return e


def sealed():
    e = frame(sill=True)
    e.append(box('plate', (0, 0, 2), (16, 16, 5), 'lacquer_deep', north='plate', south='plate_back'))
    e.append(box('strap', (1, 8.5, 1.5), (15, 9.5, 2), 'strap', skip=('south',)))
    e += pair(box('strap_pin', (1.5, 8.25, 1.25), (2.5, 9.75, 1.5), 'ferric', skip=('south',)))
    e.append(box('seal_boss', (6.5, 7.5, 1), (9.5, 10.5, 1.5), 'ferric', north='seal', skip=('south',)))
    e.append(box('seal_rim', (6.25, 7.25, 1.5), (9.75, 10.75, 2), 'iron', skip=('south',)))
    e.append(box('tablet', (4, 2.5, 1.25), (12, 6.5, 2), 'bone', north='tablet', skip=('south',)))
    return e


def opened():
    e = frame(sill=False)
    e.append(box('shelf', (0, 0, 0), (16, 2, 4), 'shelf'))
    # Lacquer shards still caught where the plate met the frame.
    for name, lo, hi in [('shard_tl0', (1, 11.5, 2), (5, 14, 2.75)), ('shard_tl1', (1, 9, 2), (2.5, 11.5, 2.75)),
                         ('shard_tr0', (12, 12.5, 2), (15, 14, 2.75)), ('shard_tr1', (14, 10.5, 2), (15, 12.5, 2.75)),
                         ('shard_bl0', (1, 2, 2), (4, 3.5, 2.75)), ('shard_bl1', (1, 3.5, 2), (2, 6.5, 2.75)),
                         ('shard_br0', (12.5, 2, 2), (15, 4.5, 2.75))]:
        e.append(box(name, lo, hi, 'lacquer_deep', north='shard', south='shard_back'))
    # Half the name tablet, fallen flat onto the shelf, and a chip of plate beside it.
    e.append(box('tablet_half', (5, 2, .75), (9, 2.5, 3.25), 'bone', up='tablet_half'))
    e.append(box('shelf_chip', (10.5, 2, 1.25), (12, 2.25, 2.5), 'lacquer_deep', up='shard'))
    return e


# --- special faces ----------------------------------------------------------------------------

def bone_frame(face, lx, ly, w, h, key):
    """Dressed bone: a pale arris along the top, a darker joint line along the foot, old staining."""
    n = noise('frame', key, face, lx, ly)
    rgb = clamp(BONE, int(n * 10) - 5 + (14 if ly == 0 and face != 'up' else 0) - (16 if ly == h - 1 and h > 1 else 0))
    if noise('stain', key, face, lx // 3, ly) < .05:
        rgb = clamp((194, 166, 146), int(n * 8))
    elif n > .96:
        rgb = clamp(BONE, -40)
    return rgb + (255,)


def post(face, lx, ly, w, h, key):
    """A slim column of vertebrae: bodies and darker discs every second texel."""
    n = noise('post', key, face, lx, ly)
    if face in ('up', 'down'):
        return clamp(BONE, int(n * 8) - 10) + (255,)
    if ly % 3 == 2:
        return clamp(BONE, int(n * 8) - 52) + (255,)
    return clamp(BONE, int(n * 10) - 6 + (10 if ly % 3 == 0 else 0)) + (255,)


def crack_map():
    """Hairline crazing across the plate: a few wandering cracks seeded at fixed points."""
    cracks = set()
    for seed, (x, y) in enumerate([(4, 4), (11, 5), (5, 12), (10, 11), (7, 3)]):
        for step in range(9):
            cracks.add((x, y))
            if noise('crack', seed, step) < .5:
                x += 1 if seed % 2 else -1
            else:
                y += 1 if seed < 3 else -1
            if not (1 <= x <= 14 and 2 <= y <= 13):
                break
    return cracks


CRACKS = crack_map()


def plate(face, lx, ly, w, h, key):
    """Crimson Lacquer seal: shadowed where the frame overhangs, crazed, flaked to gesso in two places."""
    n = noise('plate', lx, ly)
    near = min(lx - 1, 14 - lx, ly - 2, 13 - ly)
    rgb = clamp(LACQUER, (8 - ly) * 1.5 + int(n * 8) - 4)
    if near < 0:
        rgb = clamp(LACQUER_DEEP, -30)
    elif near == 0:
        rgb = clamp(LACQUER_DEEP, -10 + int(n * 6))
    elif near == 1 and noise('bead', lx, ly) < .9:
        rgb = clamp(LACQUER_DEEP, 4 + int(n * 6))
    if near >= 1:
        if (lx, ly) in CRACKS:
            rgb = CRACK
        elif (lx - 1, ly) in CRACKS or (lx, ly - 1) in CRACKS:
            rgb = clamp(LACQUER, 28) if n > .5 else rgb
        if (abs(lx - 11) + abs(ly - 11) <= 1 and noise('flake', lx, ly) < .8) or (lx, ly) in ((3, 6), (4, 6), (3, 7)):
            rgb = clamp(UNDERCOAT, int(n * 10) - 5)
    return rgb + (255,)


def plate_back(face, lx, ly, w, h, key):
    return clamp((70, 44, 42), int(noise('back', lx, ly) * 12) - 6) + (255,)


def seal(face, lx, ly, w, h, key):
    """The seal boss: an iron collar round a pressed crimson-wax core."""
    if lx == w // 2 and ly == h // 2:
        return WAX + (255,)
    if 0 < lx < w - 1 and 0 < ly < h - 1:
        return clamp(WAX, -30) + (255,)
    return clamp(PALETTE['ferric'], 18 if ly == 0 else -6) + (255,)


def strap(face, lx, ly, w, h, key):
    n = noise('strap', key, face, lx, ly)
    rgb = clamp(PALETTE['iron'], int(n * 10) - 5 + (16 if ly == 0 and face == 'north' else 0))
    if noise('vein', key, face, lx, ly) < .04:
        rgb = (98, 24, 34)
    return rgb + (255,)


def tablet(face, lx, ly, w, h, key):
    """A bone name tablet: a bevelled edge round one incised line of script."""
    n = noise('tablet', key, lx, ly)
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(BONE, -14 + int(n * 8) + (16 if ly == 0 else 0)) + (255,)
    if SCRIPT[(ly - 1) % 2][(lx - 1) % len(SCRIPT[0])] == '#':
        return INCISION + (255,)
    return clamp(BONE, 8 + int(n * 8)) + (255,)


SCRIPT = ['##.#.#', '#.##..']


def tablet_half(face, lx, ly, w, h, key):
    n = noise('tablet_half', lx, ly)
    if lx == w - 1 and noise('break', ly) < .5:
        return clamp(BONE, -34) + (255,)
    if ly == h // 2 and noise('glyph', lx) < .7:
        return INCISION + (255,)
    return clamp(BONE, 4 + int(n * 8)) + (255,)


def shard(face, lx, ly, w, h, key):
    """A broken lacquer shard: wet red face, crazed, its broken edge showing the pale gesso."""
    n = noise('shard', key, face, lx, ly)
    if noise('edge', key, lx, ly) < .45 and (lx == w - 1 or ly == h - 1) and w > 1 and h > 1:
        return clamp(UNDERCOAT, int(n * 10)) + (255,)
    if n < .12:
        return CRACK + (255,)
    return clamp(LACQUER, (h / 2 - ly) * 8 + int(n * 8) - 4) + (255,)


def shard_back(face, lx, ly, w, h, key):
    return clamp(UNDERCOAT, int(noise('shard_back', key, lx, ly) * 12) - 10) + (255,)


def shelf(face, lx, ly, w, h, key):
    """The shelf lip: worn bone, polished on its top where coffins and urns were slid in and out."""
    n = noise('shelf', key, face, lx, ly)
    if face == 'up':
        return clamp(BONE, 10 + int(n * 8) - (12 if ly == h - 1 else 0)) + (255,)
    return bone_frame(face, lx, ly, w, h, key)


SPECIAL = dict(bone_frame=bone_frame, post=post, plate=plate, plate_back=plate_back, seal=seal, strap=strap,
               tablet=tablet, tablet_half=tablet_half, shard=shard, shard_back=shard_back, shelf=shelf)


def blockstate(name):
    turns = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    variants = {}
    for facing, y in turns.items():
        v = {'model': f'hemomancy:block/{name}'}
        if y:
            v['y'] = y
        variants[f'facing={facing}'] = v
    return {'variants': variants}


# --- previews ---------------------------------------------------------------------------------

def cube(lo, hi, texture):
    return {'from': list(lo), 'to': list(hi),
            'faces': {f: {'uv': [0, 0, 16, 16], 'texture': '#' + texture}
                      for f in ('north', 'south', 'east', 'west', 'up', 'down')}}


def shifted(elements, dx):
    out = copy.deepcopy(elements)
    for el in out:
        el['from'][0] += dx
        el['to'][0] += dx
        if 'rotation' in el:
            el['rotation']['origin'][0] += dx
    return out


def row_preview(elements, title, out_name, barrels):
    """Three fronts side by side in a venous-stone wall, the loculus recess behind them."""
    textures = {
        'atlas': np.array(Image.open(ASSETS / f'textures/block/{ATLAS}.png').convert('RGBA')),
        'wall': np.array(Image.open(ASSETS / 'textures/block/polished_venous_stone_bricks.png').convert('RGBA')),
        'barrel': np.array(Image.open(ASSETS / 'textures/block/blood_wood_planks.png').convert('RGBA'))}
    model = []
    for dx in (-16, 0, 16):
        model += shifted(elements, dx)
    for bx in range(-48, 64, 16):
        for by in (-16, 16):
            model.append(cube((bx, by, 0), (bx + 16, by + 16, 16), 'wall'))
    for bx in (-48, -32, 32, 48):
        model.append(cube((bx, 0, 0), (bx + 16, 16, 16), 'wall'))
    for bx in range(-16, 32, 16):
        model.append(cube((bx, 0, 32), (bx + 16, 16, 48), 'wall'))
        model.append(cube((bx, -16, 16), (bx + 16, 0, 32), 'wall'))
        model.append(cube((bx, 16, 16), (bx + 16, 32, 32), 'wall'))
        if barrels:
            model.append(cube((bx, 0, 16), (bx + 16, 16, 32), 'barrel'))
    for bx in (-32, 32):
        model.append(cube((bx, 0, 16), (bx + 16, 16, 32), 'wall'))
    views = [(0, 6), (28, 18), (-40, 12)]
    size = 640
    sheet = Image.new('RGBA', (size * 3, size + 30), (30, 24, 28, 255))
    for i, (yaw, pitch) in enumerate(views):
        sheet.paste(render(model, textures, yaw, pitch, size, 9), (size * i, 30))
    ImageDraw.Draw(sheet).text((10, 8), title, fill=(220, 200, 200))
    out = ROOT / f'build/model-previews/{out_name}.png'
    sheet.convert('RGB').save(out)
    print(out)


def bounds(elements):
    lo = [min(el['from'][k] for el in elements) for k in range(3)]
    hi = [max(el['to'][k] for el in elements) for k in range(3)]
    return lo + hi


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    groups = {'sealed_niche': sealed(), 'opened_niche': opened()}
    moved = 0
    for elements in groups.values():
        for el in elements:
            el['density'] = 1
        moved += separate(elements)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    for name, elements in models.items():
        model = model_json(elements, ATLAS, atlas.height, PARTICLE)
        write({f'block/{name}': model}, name, model)
        (ASSETS / f'blockstates/{name}.json').write_text(json.dumps(blockstate(name), indent=2) + '\n')
        print(f'{name}: {len(elements)} cubes, extents {bounds(elements)}')
    print(f'Exported mausoleum niches: {moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        for name, elements in models.items():
            label = name.replace('_', ' ').title()
            preview(elements, ATLAS, f'{label} - block model, actual atlas', name)
            row_preview(elements, f'{label} - three fronts as one loculus in a venous-stone wall',
                        f'{name}_row', barrels=name == 'opened_niche')


if __name__ == '__main__':
    main()

"""Author the Witness Stone: the carved stele of the Beholding trial.

A chunky stele of dark venous stone on a stepped foot: a narrow shaft bound to the step by a hematic
iron band, then a broad rounded head carried on bone corbels. The head's north face is a dressed
roundel holding a carved eye (relief lids, an iris with a slit pupil, pale worn whites), ringed by a
carved channel that runs down the shaft as a tear. When a memory touches the stone the eye wakes:
the lit model swaps the iris and the plates behind the channel for full-bright crimson.

The channel is cut through the stone face (cutout texels) over a plate set a quarter pixel behind,
so the lit model only has to swap the plate. Emits the unlit and lit block models, the atlas, the
blockstate (facing x lit) and an editable Blockbench project. Python 3 + Pillow + NumPy. `--preview`
writes build/model-previews/witness_stone.png and witness_stone_lit.png.

Coordinates are block pixels for facing=north; the eye looks north (-z).
"""
import argparse
import json
import math

import carved_stone
from carved_stone import stone_rgb, world
from fixture_atlas import ASSETS, box, build, mirror_x, model_json, preview, separate, write

ATLAS = 'witness_stone'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}
EYE = (8, 10.5)          # centre of the eye on the north face
RING = (3.6, 4.4)        # radii of the carved channel around it
FRONT = 6                # the stele's north face plane

SHAPE = [(2, 0, 4, 14, 2, 12), (2.5, 2, 5, 13.5, 3, 11), (3.5, 3, 6, 12.5, 6, 10), (2.5, 6, 6, 13.5, 16, 10)]


def pair(el):
    return [el, mirror_x(el)]


def stele():
    e = []
    # Stepped foot, two bone knuckles left on its front corners.
    e.append(box('foot', (2, 0, 4), (14, 2, 12), 'stone'))
    e.append(box('foot_step', (2.5, 2, 5), (13.5, 3, 11), 'stone'))
    e += pair(box('offering_knuckle', (2.5, 2, 4.25), (4, 2.75, 4.75), 'bone'))
    # Shaft, bound to the step by a hematic iron band with two bone studs.
    e.append(box('shaft', (3.5, 3, 6), (12.5, 6, 10), 'face', skip=('down', 'up')))
    e.append(box('band', (3.25, 3, 5.75), (12.75, 4, 10.25), 'iron', skip=('down',)))
    e += pair(box('band_stud', (4.25, 3.25, 5.5), (5, 3.75, 5.75), 'bone', skip=('south',)))
    # Broad head over bone corbels, rounded in two steps.
    e.append(box('head', (2.5, 6, 6), (13.5, 14, 10), 'face'))
    e.append(box('head_round', (3.5, 14, 6), (12.5, 15, 10), 'face', skip=('down',)))
    e.append(box('head_crown', (5.5, 15, 6), (10.5, 16, 10), 'face', skip=('down',)))
    e += pair(box('side_cramp', (2.25, 8, 7.5), (2.5, 13, 8.5), 'iron', skip=('east',)))
    e += pair(box('corbel', (2.75, 4.5, 6.75), (3.5, 6, 9.25), 'bone', skip=('east', 'up')))
    # The carved eye: relief lids around a recessed iris (the iris is in the swapped group).
    e.append(box('upper_lid', (6.5, 12, 5.5), (9.5, 13, 6), 'lid', skip=('south',)))
    e += pair(box('upper_lid_flank', (5.5, 11, 5.5), (6.5, 12, 6), 'lid', skip=('south',)))
    e.append(box('lower_lid', (6.5, 8, 5.5), (9.5, 9, 6), 'lid', skip=('south',)))
    e += pair(box('lower_lid_flank', (5.5, 9, 5.5), (6.5, 10, 6), 'lid', skip=('south',)))
    return e


def waking(lit):
    """The parts that wake: the iris and the plates seen through the channel."""
    suffix, extra = ('glow', BRIGHT) if lit else ('dull', None)
    only_north = ('south', 'east', 'west', 'up', 'down')
    return [box('iris', (6.5, 9, 5.75), (9.5, 12, 6), f'iris_{suffix}', skip=('south',), extra=extra),
            box('channel_plate', (3, 6, 6.25), (13, 14, 6.5), f'channel_{suffix}', skip=only_north, extra=extra),
            box('channel_plate_crown', (6, 14, 6.25), (10, 15, 6.5), f'channel_{suffix}', skip=only_north,
                extra=extra),
            box('tear_plate', (7, 4, 6.25), (9, 6, 6.5), f'channel_{suffix}', skip=only_north, extra=extra)]


# --- special faces --------------------------------------------------------------------------

def channel(x, y):
    """True where the carved channel cuts the north face: the ring and the tear below it."""
    r = math.hypot(x - EYE[0], y - EYE[1])
    return RING[0] - .01 <= r <= RING[1] or (x == EYE[0] and 4 < y < 6.5)


def face(face, lx, ly, w, h, key):
    """The stele's north face: the channel cut through, dried seep under it, the eye's whites and corners."""
    if face != 'north':
        return carved_stone.stone(face, lx, ly, w, h, key)
    x, y, _ = world(key, face, lx, ly)
    if channel(x, y):
        return 0, 0, 0, 0
    dx, dy = x - EYE[0], y - EYE[1]
    if dy == 0 and abs(dx) == 2:
        return (148, 130, 124, 255)        # the whites, worn pale
    if dy == 0 and abs(dx) == 3:
        return (38, 26, 30, 255)           # the deep-cut corners of the eye
    inside = math.hypot(dx, dy) < RING[0]
    rgb = stone_rgb(key, face, lx, ly, w, h, tone=16 if inside else 0, vein=not inside, mottle=8 if inside else 22)
    if channel(x, y + 1) and carved_stone.noise('seep', x, y) < .6:
        rgb = tuple(int(c * .7 + s * .3) for c, s in zip(rgb, (96, 34, 40)))   # old blood seeped from the cut
    return rgb + (255,)


def iris(lit):
    def paint(face, lx, ly, w, h, key):
        if face != 'north':
            return ((176, 22, 38) if lit else (70, 32, 36)) + (255,)
        corner = lx in (0, w - 1) and ly in (0, h - 1)
        if lx == w // 2:
            return ((255, 132, 116) if lit else (18, 10, 14)) + (255,)   # the slit pupil
        if corner:
            return ((150, 16, 32) if lit else (58, 28, 32)) + (255,)
        return ((218, 34, 50) if lit else (88, 38, 42)) + (255,)
    return paint


def channel_plate(lit):
    def paint(face, lx, ly, w, h, key):
        n = carved_stone.noise('channel', key, lx, ly)
        if lit:
            return ((236, 64, 70) if n > .82 else (204, 28, 44) if n > .25 else (172, 18, 36)) + (255,)
        return ((52, 18, 26) if n > .75 else (30, 18, 22)) + (255,)
    return paint


SPECIAL = dict(carved_stone.SPECIAL, face=face, iris_dull=iris(False), iris_glow=iris(True),
               channel_dull=channel_plate(False), channel_glow=channel_plate(True))


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for lit in ('false', 'true'):
            model = 'hemomancy:block/witness_stone' + ('_lit' if lit == 'true' else '')
            variants[f'facing={facing},lit={lit}'] = dict({'model': model}, **({'y': y} if y else {}))
    return {'variants': variants}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body, dull, glow = stele(), waking(False), waking(True)
    moved = separate(body + dull)
    separate(body + glow)
    groups = {'body': body, 'dull': dull, 'glow': glow}
    carved_stone.register(groups)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'
    unlit = model_json(models['body'] + models['dull'], ATLAS, atlas.height, particle)
    lit = model_json(models['body'] + models['glow'], ATLAS, atlas.height, particle)
    write({'block/witness_stone': unlit, 'block/witness_stone_lit': lit}, 'witness_stone', lit)
    (ASSETS / 'blockstates/witness_stone.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    print(f'Exported Witness Stone: {len(body) + len(dull)} cubes ({len(glow)} swapped when lit), '
          f'{moved} faces separated, 128x{atlas.height} atlas')
    print('Voxel shape (Block.box, north):', SHAPE)
    if args.preview:
        preview(unlit['elements'], ATLAS, 'Witness Stone - unlit block model, actual atlas', 'witness_stone')
        preview(lit['elements'], ATLAS, 'Witness Stone - lit (iris and channel full-bright)', 'witness_stone_lit')


if __name__ == '__main__':
    main()

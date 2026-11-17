"""Author the Sanguine Vigil: the Harbinger ward-post that binds hostiles and tends kept creatures.

A two-block sentinel. A stepped plinth of dark venous stone carries a hematic iron socket and a slim
iron shaft ringed by three bone vertebrae, with two sinew stays running up its flanks. A blood gauge
is let into the shaft's north face, and a small bone dish stands out from the socket to take the
Temper catalyst. The shaft ends in a notched iron yoke holding a lacquered eye, bone spurs at its
sides and a bone crest over it.

The eye and the gauge are the swapped group. Dormant (no blood stored) the eye is shut, a dark lid
seam across it, and the gauge is dry. Lit (`lit=true`, the ward watching) the lid is gone: a
full-bright crimson iris around a dark slit pupil looks north and south, and the gauge runs red.

The whole post is one model that stands 32 pixels tall from the controller block; the cell above is
the multiblock's invisible filler. Emits the dormant and lit block models, the atlas, the blockstate
(facing x lit), the item model and an editable Blockbench project. Python 3 + Pillow + NumPy.
`--preview` writes build/model-previews/sanguine_vigil.png and sanguine_vigil_lit.png.

Coordinates are block pixels for facing=north; the dish and the gauge face north (-z).
"""
import argparse
import json

import carved_stone
from fixture_atlas import ASSETS, box, build, mirror_x, model_json, preview, separate, write, write_tall_item
from witness_stone import channel_plate

ATLAS = 'sanguine_vigil'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}

# Per cell, for facing north or south; east and west swap x and z.
SHAPE = {'lower': [(2, 0, 2, 14, 3, 14), (4.5, 3, 4.5, 11.5, 5, 11.5), (5.5, 5, 5.5, 10.5, 16, 10.5)],
         'upper': [(5.5, 0, 5.5, 10.5, 4, 10.5), (3, 4, 6, 13, 16, 10)]}


def pair(el):
    return [el, mirror_x(el)]


def post():
    e = []
    # Stepped plinth and the iron socket the shaft stands in.
    e.append(box('plinth', (2, 0, 2), (14, 2, 14), 'stone'))
    e.append(box('plinth_step', (3, 2, 3), (13, 3, 13), 'stone', skip=('down',)))
    e.append(box('socket', (4.5, 3, 4.5), (11.5, 5, 11.5), 'iron', skip=('down',)))
    e += pair(box('socket_stud', (5, 3.5, 11.5), (5.75, 4.25, 11.75), 'bone', skip=('north',)))
    # The catalyst dish, on an iron arm out of the socket's north face.
    e.append(box('dish_arm', (7, 3, 2.75), (9, 4, 4.5), 'iron', skip=('down', 'south')))
    e.append(box('catalyst_dish', (5.5, 4, 1.5), (10.5, 5, 4.5), 'bone', up='panel'))
    # Shaft, three bone vertebrae, a sinew stay up each flank.
    e.append(box('shaft', (6, 5, 6), (10, 19, 10), 'iron', skip=('down', 'up')))
    for y in (7, 11.5, 16):
        e.append(box('vertebra', (5.5, y, 5.5), (10.5, y + 1.5, 10.5), 'bone'))
    e += pair(box('sinew_stay', (4.25, 5, 7.5), (5, 20, 8.5), 'gristle', skip=('down', 'up')))
    # The yoke: a neck, then an iron frame with notched corners around the eye.
    e.append(box('neck', (5, 19, 6), (11, 20, 10), 'iron'))
    e.append(box('yoke_foot', (4, 20, 6.5), (12, 22, 9.5), 'iron'))
    e += pair(box('yoke_side', (3, 22, 6.5), (5, 30, 9.5), 'iron'))
    e.append(box('yoke_brow', (4, 30, 6.5), (12, 31.5, 9.5), 'iron'))
    e.append(box('crest', (6.5, 31.5, 7), (9.5, 32, 9), 'bone', skip=('down',)))
    e += pair(box('spur', (1.5, 25, 7.5), (3, 26.5, 8.5), 'bone', skip=('east',)))
    e += pair(box('brow_horn', (3, 30, 7.25), (4, 31.25, 8.75), 'bone', skip=('down', 'east')))
    for z, hidden in ((6.25, 'south'), (9.5, 'north')):
        e += pair(box('yoke_stud', (3.5, 25.25, z), (4.5, 26.25, z + .25), 'bone', skip=(hidden,)))
    return e


def waking(lit):
    """The parts that wake with the ward: the eye and the blood gauge."""
    suffix, extra = ('glow', BRIGHT) if lit else ('dull', None)
    return [box('eye', (5.5, 22.5, 6), (10.5, 29.5, 10), f'eye_{suffix}', extra=extra),
            box('blood_gauge', (7, 5.5, 5.75), (9, 18.5, 6), f'channel_{suffix}',
                skip=('south', 'up', 'down'), extra=extra)]


# --- special faces --------------------------------------------------------------------------

def eye(lit):
    def paint(face, lx, ly, w, h, key):
        if face not in ('north', 'south'):
            return ((150, 16, 32) if lit else (70, 32, 36)) + (255,)
        corner = lx in (0, w - 1) and ly in (0, h - 1)
        mid_x, mid_y = lx == w // 2, ly == h // 2
        if not lit:
            # Shut: the lid's seam across the middle, a worn highlight on the lid above it.
            if mid_y:
                return (18, 10, 14, 255)
            if corner:
                return (48, 24, 28, 255)
            return ((104, 44, 48) if ly == h // 2 - 1 else (70, 32, 36)) + (255,)
        if corner:
            return (92, 12, 24, 255)
        tall = 1 <= ly <= h - 2
        if mid_x and tall:
            return (26, 6, 12, 255)                # the slit pupil
        if abs(lx - w // 2) == 1 and tall:
            return (255, 104, 96, 255)             # the iris, hottest against the pupil
        if mid_x:
            return (218, 34, 50, 255)
        return (150, 16, 32, 255)
    return paint


SPECIAL = dict(carved_stone.SPECIAL, eye_dull=eye(False), eye_glow=eye(True),
               channel_dull=channel_plate(False), channel_glow=channel_plate(True))


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for lit in ('false', 'true'):
            model = 'hemomancy:block/sanguine_vigil' + ('_lit' if lit == 'true' else '')
            variants[f'facing={facing},lit={lit}'] = dict({'model': model}, **({'y': y} if y else {}))
    return {'variants': variants}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body, dull, glow = post(), waking(False), waking(True)
    moved = separate(body + dull)
    separate(body + glow)
    groups = {'body': body, 'dull': dull, 'glow': glow}
    carved_stone.register(groups)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'
    dormant = model_json(models['body'] + models['dull'], ATLAS, atlas.height, particle)
    lit = model_json(models['body'] + models['glow'], ATLAS, atlas.height, particle)
    write({'block/sanguine_vigil': dormant, 'block/sanguine_vigil_lit': lit}, 'sanguine_vigil', lit)
    write_tall_item('sanguine_vigil', lit)
    (ASSETS / 'blockstates/sanguine_vigil.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    print(f'Exported Sanguine Vigil: {len(body) + len(dull)} cubes ({len(glow)} swapped when lit), '
          f'{moved} faces separated, 128x{atlas.height} atlas')
    print('Voxel shape (Block.box per cell, north):', SHAPE)
    if args.preview:
        preview(dormant['elements'], ATLAS, 'Sanguine Vigil - dormant block model, actual atlas', 'sanguine_vigil',
                lift=8, size=400, scale=11)
        preview(lit['elements'], ATLAS, 'Sanguine Vigil - lit (eye and gauge full-bright)', 'sanguine_vigil_lit',
                lift=8, size=400, scale=11)


if __name__ == '__main__':
    main()

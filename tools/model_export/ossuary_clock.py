"""Author the Ossuary Clock: a blood-wood longcase clock built on the bones of the dead.

A knuckle-footed plinth with a skull plaque, a trunk flanked by stacked vertebra columns, a glazed
door over a bone pendulum with a Mnemonic Ambergris bob and two bound-bone weights, and a hood
carrying a bone chapter dial under a blood-moon lunette (the clock reports the moon phase).

Emits the lower and upper block models, the item model, the painted atlas and an editable
Blockbench project. Python 3 + Pillow + NumPy. `--preview` also writes a textured turnaround to
build/model-previews/ossuary_clock.png.

Coordinates are block pixels for the north-facing state, authored as one 32-pixel-tall piece and cut
at y = 16 into the two block halves. The front (player side) is z = 0.
"""
import argparse
import math

from clairaudiograph import clamp, noise
from fixture_atlas import (ASSETS, PALETTE, box, build, item_elements, mirror_x, model_json, preview, separate, spin,
                           split_storeys, write)

ATLAS = 'ossuary_clock'
PARTICLE = 'hemomancy:block/blood_wood_planks'
SOCKET = (28, 6, 10)
CRIMSON = (150, 18, 28)


def pair(el):
    return [el, mirror_x(el)]


def vertebra_column(name, cx, cz, y0, y1):
    """Alternating vertebral bodies and darker discs, each body with a spinous process facing out."""
    e, y, i = [], y0, 0
    while y < y1 - 1e-6:
        top = min(y + 1, y1)
        e.append(box(f'{name}_vertebra{i}', (cx - .875, y, cz - .75), (cx + .875, top, cz + .75), 'bone'))
        e.append(box(f'{name}_process{i}', (cx - .25, y + .25, cz - 1.25), (cx + .25, top - .25, cz - .75), 'bone',
                     skip=('south',)))
        y = top
        if y < y1 - 1e-6:
            e.append(box(f'{name}_disc{i}', (cx - .625, y, cz - .625), (cx + .625, y + .5, cz + .625), 'gristle'))
            y += .5
        i += 1
    return e


def clock():
    e = []
    # Plinth on bone knuckle feet, its front set with a skull plaque between lacquered insets.
    for x, z in [(1, 1.25), (13, 1.25), (1, 12.75), (13, 12.75)]:
        e.append(box('foot', (x, 0, z), (x + 2, 1.25, z + 2), 'bone'))
    e += pair(box('foot_toe', (1.5, 0, .75), (2.5, .75, 1.25), 'bone', skip=('south',)))
    e.append(box('plinth', (1.5, 1.25, 2), (14.5, 5, 14), 'wood'))
    e.append(box('skull_plaque', (6.25, 1.75, 1.5), (9.75, 4.5, 2), 'bone', north='skull', skip=('south',)))
    e += pair(box('plinth_inset', (2.5, 2, 1.75), (5.5, 4.25, 2), 'panel', skip=('south',)))
    e.append(box('plinth_cap', (1, 5, 1.5), (15, 5.75, 14.5), 'lacquer'))
    e.append(box('plinth_bead', (1.25, 5.75, 1.75), (14.75, 6.25, 14.25), 'bone'))

    # Trunk: a dark-boarded cavity behind a glazed, arch-headed door.
    e.append(box('trunk', (2.5, 6.25, 4.5), (13.5, 19.75, 13), 'wood', north='cavity'))
    e += pair(box('door_stile', (2.5, 6.25, 3), (4.5, 19.75, 4.5), 'wood'))
    e.append(box('door_rail_bottom', (4.5, 6.25, 3), (11.5, 7.75, 4.5), 'wood'))
    e.append(box('door_rail_top', (4.5, 18.25, 3), (11.5, 19.75, 4.5), 'wood'))
    e += pair(box('door_arch', (4.5, 17.25, 3), (5.5, 18.25, 4.5), 'wood'))
    e.append(box('door_glass', (4.5, 7.75, 3.25), (11.5, 18.25, 3.375), 'glass',
                 skip=('east', 'west', 'up', 'down')))
    e.append(box('door_keyhole', (11.75, 12, 2.875), (12.25, 13, 3), 'iron', skip=('south',)))
    e += pair(box('trunk_side_panel', (2.25, 8, 5.5), (2.5, 18, 11.5), 'panel', skip=('east',)))
    e += vertebra_column('left_column', 2.5, 3, 6.25, 19.75)
    e += vertebra_column('right_column', 13.5, 3, 6.25, 19.75)

    # Pendulum and weights inside the trunk.
    e.append(box('suspension', (7.25, 17.5, 3.75), (8.75, 18.25, 4.5), 'iron', skip=('south',)))
    e.append(box('pendulum_rod', (7.75, 11, 3.875), (8.25, 17.5, 4.25), 'bone', skip=('south',)))
    e.append(box('pendulum_bob', (6.4, 8.65, 3.5), (9.6, 11.85, 4.25), 'bone', north='bob', south='bob',
                 rotation=spin('z', 45, (8, 10.25, 3.875))))
    for x in (4.875, 9.875):
        e.append(box('weight_chain', (x + .5, 15.75, 4), (x + .75, 18.25, 4.25), 'chain', skip=('south',)))
        e.append(box('weight_cap', (x, 15.25, 3.75), (x + 1.25, 15.75, 4.5), 'iron', skip=('south',)))
        e.append(box('weight', (x + .125, 13.25, 3.875), (x + 1.125, 15.25, 4.375), 'bone', skip=('south',)))
        e.append(box('weight_band', (x, 13, 3.75), (x + 1.25, 13.25, 4.5), 'iron', skip=('south',)))

    # Waist moulding up to the wider hood.
    e.append(box('waist_moulding', (1.75, 19.75, 2.25), (14.25, 20.5, 13.75), 'lacquer'))
    e.append(box('hood_base', (1.25, 20.5, 1.5), (14.75, 21, 14.25), 'wood'))

    # Hood: a lacquered face carrying the bone chapter dial and the blood-moon lunette.
    e.append(box('hood', (2, 21, 2.75), (14, 29.75, 13.5), 'wood'))
    e.append(box('hood_face', (3, 21.25, 2.25), (13, 29.5, 2.75), 'lacquer', skip=('south',)))
    e += pair(box('hood_side_fret', (1.75, 22, 4.5), (2, 28.75, 11.5), 'fret', skip=('east',)))
    e += pair(box('hood_column', (2.25, 21.5, 1.75), (3.25, 29.25, 2.75), 'bone', skip=('south',)))
    e += pair(box('hood_column_base', (2, 21, 1.5), (3.5, 21.5, 3), 'ferric'))
    e += pair(box('hood_column_capital', (2, 29.25, 1.5), (3.5, 29.75, 3), 'ferric'))
    e.append(box('dial', (5, 21.75, 2), (11, 27.75, 2.25), 'bone', north='dial', skip=('south',)))
    e.append(box('bezel_top', (4.75, 27.75, 1.875), (11.25, 28, 2.25), 'ferric', skip=('south',)))
    e.append(box('bezel_bottom', (4.75, 21.5, 1.875), (11.25, 21.75, 2.25), 'ferric', skip=('south',)))
    e += pair(box('bezel_side', (4.75, 21.75, 1.875), (5, 27.75, 2.25), 'ferric', skip=('south',)))
    e.append(box('lunette', (5.5, 28, 2), (10.5, 29.25, 2.25), 'lacquer_deep', north='lunette', skip=('south',)))
    e += pair(box('lunette_step', (5.25, 28, 1.875), (5.5, 29.25, 2.25), 'ferric', skip=('south',)))
    e += pair(box('lunette_shoulder', (5.5, 29.25, 1.875), (6.5, 29.5, 2.25), 'ferric', skip=('south',)))
    e.append(box('lunette_crown', (6.5, 29.25, 1.875), (9.5, 29.75, 2.25), 'ferric', skip=('south',)))
    centre = (8, 24.75, 1.75)
    e.append(box('hand_boss', (7.625, 24.375, 1.625), (8.375, 25.125, 2), 'ferric', skip=('south',)))
    e.append(box('minute_hand', (7.875, 24.75, 1.75), (8.125, 27.25, 1.875), 'hand', skip=('south',),
                 rotation=spin('z', 22.5, centre)))
    e.append(box('hour_hand', (7.875, 24.75, 1.6875), (8.125, 26.5, 1.8125), 'hand', skip=('south',),
                 rotation=spin('z', -45, centre)))

    # Cornice and broken pediment of ribs around an ambergris finial.
    e.append(box('cornice', (1.25, 29.75, 1.5), (14.75, 30.5, 14.25), 'lacquer'))
    e.append(box('crown_base', (2, 30.5, 2.5), (14, 31, 13.5), 'wood'))
    e += pair(box('pediment_rib_low', (2, 30.5, 1.75), (4.75, 31, 2.75), 'bone'))
    e += pair(box('pediment_rib_high', (4.25, 31, 1.75), (6.5, 31.5, 2.75), 'bone'))
    e.append(box('finial_plinth', (6.5, 30.5, 1.5), (9.5, 31, 3.5), 'lacquer'))
    e.append(box('finial', (7, 31, 1.75), (9, 32, 3.75), 'ambergris'))
    return e


# --- special faces --------------------------------------------------------------------------

def dial(face, lx, ly, w, h, key):
    """Bone chapter dial: crimson quarter pips, iron hour pips, lacquered spandrels in the corners."""
    c = (w - 1) / 2
    dx, dy = lx - c, ly - c
    r = math.hypot(dx, dy)
    n = noise('dial', lx, ly)
    if r > w / 2 - .1:
        return clamp(PALETTE['lacquer_deep'], int(n * 8)) + (255,)
    if r > w / 2 - 1.6:
        angle = math.degrees(math.atan2(dx, -dy)) % 30
        if min(angle, 30 - angle) < 9:
            quarter = round(math.degrees(math.atan2(dx, -dy)) / 30) % 3 == 0
            return (CRIMSON if quarter else (54, 40, 46)) + (255,)
        return clamp(PALETTE['bone'], -16 + int(n * 8)) + (255,)
    if r > w / 2 - 2:
        return PALETTE['iron'] + (255,)
    if r < 1:
        return PALETTE['ferric'] + (255,)
    rgb = clamp(PALETTE['bone'], 10 + int(n * 8))
    if noise('thread', round(math.degrees(math.atan2(dx, dy)) / 40)) < .3 and n < .4:
        rgb = (190, 150, 134)
    return rgb + (255,)


def lunette(face, lx, ly, w, h, key):
    """Moon-phase arch: a blood moon rising between two bone hemispheres on a night ground."""
    mid = w / 2
    moon = (abs(lx + .5 - mid) <= (1 if ly != 1 else 2)) and ly < 3
    if moon:
        return ((214, 70, 78) if (ly, lx) == (0, int(mid) - 1) else (170, 22, 34)) + (255,)
    if (ly >= h - 2 and (lx < 2 or lx >= w - 2)) or (ly == h - 3 and (lx == 0 or lx == w - 1)):
        return clamp(PALETTE['bone'], int(noise('hemisphere', lx, ly) * 10) - 10) + (255,)
    if noise('star', lx, ly) > .9:
        return (200, 186, 164, 255)
    return (26, 12, 20, 255)


SKULL = ['..BBB..',
         '.BBBBB.',
         'BEEBEEB',
         'BBBNBBB',
         '.BBBBB.',
         '.BDBDB.']


def skull(face, lx, ly, w, h, key):
    ch = SKULL[min(ly, len(SKULL) - 1)][min(lx, len(SKULL[0]) - 1)]
    n = noise('skull', lx, ly)
    if ch == '.':
        return clamp(PALETTE['lacquer_deep'], int(n * 6)) + (255,)
    if ch in 'END':
        return SOCKET + (255,)
    return clamp(PALETTE['bone'], int(n * 12) - (14 if ly == h - 1 else 0)) + (255,)


def bob(face, lx, ly, w, h, key):
    """An ambergris lozenge set in bone, a crimson bead at its heart."""
    n = noise('bob', face, lx, ly)
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(PALETTE['bone'], int(n * 10) - 12) + (255,)
    if lx == w // 2 and ly == h // 2:
        return CRIMSON + (255,)
    rgb = clamp(PALETTE['ambergris'], int(n * 18) - 6)
    return ((140, 127, 85) if n > .88 else rgb) + (255,)


SPECIAL = dict(dial=dial, lunette=lunette, skull=skull, bob=bob)

DISPLAY = {
    'gui': {'rotation': [20, 200, 0], 'translation': [0, -3.4, 0], 'scale': [.45, .45, .45]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 1, 0], 'scale': [.2, .2, .2]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, -4, 0], 'scale': [.5, .5, .5]},
    'head': {'rotation': [0, 180, 0], 'translation': [0, 4, 0], 'scale': [.6, .6, .6]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1.5], 'scale': [.25, .25, .25]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1.5], 'scale': [.25, .25, .25]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -1, 0], 'scale': [.3, .3, .3]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -1, 0], 'scale': [.3, .3, .3]},
}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    elements = clock()
    moved = separate(elements)
    lower, upper = split_storeys(elements)
    atlas, models = build({'lower': lower, 'upper': upper}, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    item = model_json(item_elements((models['lower'], 0), (models['upper'], 16)), ATLAS, atlas.height, PARTICLE,
                      DISPLAY)
    outputs = {
        'block/ossuary_clock': model_json(models['lower'], ATLAS, atlas.height, PARTICLE),
        'block/ossuary_clock_upper': model_json(models['upper'], ATLAS, atlas.height, PARTICLE),
        'item/ossuary_clock': item,
    }
    write(outputs, 'ossuary_clock', item)
    print(f'Exported Ossuary Clock: {len(lower)} lower + {len(upper)} upper cubes, {moved} faces separated, '
          f'128x{atlas.height} atlas')
    if args.preview:
        preview(item['elements'], ATLAS, 'Ossuary Clock - lower + upper block models, actual atlas', 'ossuary_clock',
                lift=8, size=400, scale=11)


if __name__ == '__main__':
    main()

"""Author the Humoral Barometer: a wall-hung banjo barometer that reads weather as the four humours.

A blood-wood banjo board inlaid with a bone ring carries an iron bezel and a quartered humour dial
(sanguine, choleric, melancholic, phlegmatic) under a hematic iron needle. Up the neck a glass
column draws blood down from the Bleeding Bulb crowning the board, between two bone scales.
No brass: the bezel, collar and needle are hematic iron (copper is an antiseptic Unstained metal).

Emits the block model, the painted atlas and an editable Blockbench project; the item model parents
the block. Python 3 + Pillow + NumPy. `--preview` also writes a textured turnaround to
build/model-previews/humoral_barometer.png.

Coordinates are block pixels for the south-facing state: the wall is at z = 16 and the dial faces
-z. HumoralBarometerBlock's shapes follow these bounds.
"""
import argparse
import math

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, PALETTE, box, build, mirror_x, model_json, preview, separate, spin, write

ATLAS = 'humoral_barometer'
PARTICLE = 'hemomancy:block/blood_wood_planks'
DIAL_CENTRE = (8, 5.5)
FLAT = ('south', 'east', 'west', 'up', 'down')
# Banjo body as pixel-circle slices (y0, y1, half width) so the front faces never overlap.
BODY = [(.5, 1.5, 2), (1.5, 2.5, 3.5), (2.5, 3.5, 4.5), (3.5, 7.5, 5), (7.5, 8.5, 4.5), (8.5, 9.5, 3.5)]


def barometer():
    cx, cy = DIAL_CENTRE
    e = [box('body', (cx - hw, y0, 14.75), (cx + hw, y1, 16), 'wood') for y0, y1, hw in BODY]
    e.append(box('neck', (6, 9.5, 14.75), (10, 14.25, 16), 'wood'))
    e.append(box('crest', (6.5, 14.25, 14.75), (9.5, 15, 16), 'wood'))
    e.append(box('crest_cap', (7.25, 15, 14.75), (8.75, 15.5, 16), 'wood'))
    e += [box('scroll_ear', (5, 9.5, 15), (6, 10.5, 16), 'bone')]
    e.append(mirror_x(e[-1]))
    e.append(box('drop_finial', (7.25, 0, 15), (8.75, .5, 15.75), 'bone'))

    # Dial stack: bone inlay ring, iron bezel, quartered humour face, needle on a ferric boss.
    e.append(box('inlay', (cx - 4.5, cy - 4.5, 14.5), (cx + 4.5, cy + 4.5, 14.75), 'bone', north='inlay', skip=FLAT,
                 density=2))
    e.append(box('bezel', (cx - 4, cy - 4, 14.25), (cx + 4, cy + 4, 14.5), 'iron', north='bezel', skip=FLAT,
                 density=2))
    e.append(box('dial', (cx - 3.5, cy - 3.5, 14), (cx + 3.5, cy + 3.5, 14.25), 'bone', north='humour_dial',
                 skip=FLAT, density=2))
    pivot = (cx, cy, 13.8125)
    e.append(box('needle', (cx - .125, cy, 13.75), (cx + .125, cy + 3, 13.875), 'ferric', north='needle',
                 skip=('south',), rotation=spin('z', -22.5, pivot), density=2))
    e.append(box('needle_tail', (cx - .25, cy - 1.25, 13.75), (cx + .25, cy, 13.875), 'iron', skip=('south',),
                 rotation=spin('z', -22.5, pivot)))
    e.append(box('needle_boss', (cx - .5, cy - .5, 13.625), (cx + .5, cy + .5, 14), 'ferric', skip=('south',)))

    # Neck: a glass humour column fed from the Bleeding Bulb, between two bone scales.
    e.append(box('column_foot', (7.25, 9.5, 13.875), (8.75, 10, 14.5), 'ferric', skip=('south',)))
    e.append(box('column_glass', (7.5, 10, 14), (8.5, 13.5, 14.5), 'tube', skip=('south', 'up', 'down'), density=4))
    e.append(box('column_blood', (7.75, 11, 14.125), (8.25, 13.5, 14.375), 'blood', skip=('south',)))
    e.append(box('scale', (6.25, 10.25, 14.5), (7.25, 13.25, 14.75), 'bone', north='scale', skip=('south',),
                 density=2))
    e.append(mirror_x(e[-1]))
    e.append(box('bulb_collar', (6.75, 13.5, 13.75), (9.25, 14, 14.75), 'ferric', skip=('south',)))
    e.append(box('bleeding_bulb', (6.875, 14, 13.875), (9.125, 15.25, 14.75), 'bulb', skip=('south',), density=2))
    e.append(box('bulb_crown', (7.375, 15.25, 14.125), (8.625, 15.875, 14.75), 'bulb', skip=('south',), density=2))
    return e


# --- special faces --------------------------------------------------------------------------

HUMOURS = {  # quadrant -> wash over the bone face
    (-1, -1): (150, 18, 28),    # sanguine, upper left
    (1, -1): (160, 128, 58),    # choleric (yellow bile), upper right
    (1, 1): (30, 22, 28),       # melancholic (black bile), lower right
    (-1, 1): (190, 196, 184),   # phlegmatic, lower left
}


def polar(lx, ly, w, h):
    dx, dy = lx - (w - 1) / 2, ly - (h - 1) / 2
    return dx, dy, math.hypot(dx, dy)


def humour_dial(face, lx, ly, w, h, key):
    dx, dy, r = polar(lx, ly, w, h)
    n = noise('humour', lx, ly)
    if r > w / 2:
        return (0, 0, 0, 0)
    if r > w / 2 - 1.3:
        quarter = min(abs(dx), abs(dy)) < .6   # iron ticks at the four cardinal humours only
        return (PALETTE['iron'] if quarter else clamp(PALETTE['bone'], -6 + int(n * 8))) + (255,)
    if r < 1.2:
        return PALETTE['ferric'] + (255,)
    wash = HUMOURS[(1 if dx > 0 else -1, 1 if dy > 0 else -1)]
    bone = clamp(PALETTE['bone'], int(n * 8))
    mix = .85 if r > w / 2 - 2.4 else .55
    return tuple(int(b + (c - b) * mix) for b, c in zip(bone, wash)) + (255,)


def needle(face, lx, ly, w, h, key):
    """Polished hematic iron, tipped in crimson."""
    if ly < 2:
        return (176, 24, 36, 255)
    return clamp(PALETTE['ferric'], 36 - ly * 2) + (255,)


def bezel(face, lx, ly, w, h, key):
    dx, dy, r = polar(lx, ly, w, h)
    if r > w / 2:
        return (0, 0, 0, 0)
    if abs(abs(dx) - abs(dy)) < 1 and r > w / 2 - 1.5:
        return clamp(PALETTE['bone'], -8) + (255,)      # four bone rivets on the diagonals
    shift = 22 if dy < 0 and r > w / 2 - 1.2 else 0     # cold sheen on the upper rim
    return clamp(PALETTE['ferric'] if r > w / 2 - 1.2 else PALETTE['iron'], shift + int(noise('bezel', lx, ly) * 8)) \
        + (255,)


def inlay(face, lx, ly, w, h, key):
    _, _, r = polar(lx, ly, w, h)
    if w / 2 - 1.1 < r <= w / 2:
        return clamp(PALETTE['bone'], int(noise('inlay', lx, ly) * 10) - 12) + (255,)
    return (0, 0, 0, 0)


def scale(face, lx, ly, w, h, key):
    if ly % 4 == 1:
        return (150, 18, 28, 255)
    if ly % 2 == 1 and lx == (0 if key[1] % 2 else w - 1):
        return (54, 40, 46, 255)
    return clamp(PALETTE['bone'], int(noise('scale', key, lx, ly) * 10) - 6) + (255,)


def bulb(face, lx, ly, w, h, key):
    """The Bleeding Bulb: wet crimson flesh, darker veins, one bright highlight."""
    n = noise('bulb', face, lx, ly)
    if face in ('north', 'up') and (lx, ly) == (1, 1):
        return (226, 92, 98, 255)
    if noise('bulb_vein', face, lx + ly) < .25 and n < .5:
        return (92, 12, 24, 255)
    return clamp(PALETTE['blood'], int(n * 18) - 6 - (20 if ly == h - 1 else 0)) + (255,)


def tube(face, lx, ly, w, h, key):
    if lx in (0, w - 1):
        return (198, 214, 222, 255) if ly % 5 else (236, 244, 248, 255)
    return (0, 0, 0, 0)


SPECIAL = dict(humour_dial=humour_dial, needle=needle, bezel=bezel, inlay=inlay, scale=scale, bulb=bulb, tube=tube)

DISPLAY = {
    'gui': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [.85, .85, .85]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, -4], 'scale': [.75, .75, .75]},
}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    elements = barometer()
    moved = separate(elements)
    atlas, models = build({'block': elements}, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    block = model_json(models['block'], ATLAS, atlas.height, PARTICLE, DISPLAY)
    write({'block/humoral_barometer': block}, 'humoral_barometer', block)
    print(f'Exported Humoral Barometer: {len(elements)} cubes, {moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        preview(models['block'], ATLAS, 'Humoral Barometer - block model (wall at +z), actual atlas',
                'humoral_barometer')


if __name__ == '__main__':
    main()

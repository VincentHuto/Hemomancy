"""Author the Offering Stand: the Litany trial's bone-stemmed display stand.

A stepped foot of hematic iron and dark venous stone, a stem of stacked vertebrae, and a shallow
Crimson Lacquer dish with a bone rim and four bone talons curling in over it. The block entity
renders the one offering candidate it holds lying in the dish; DISH_TOP is the height of the dish's
inner floor, where the renderer rests the item.

A second style, `bowl` (Velorum's Litany, the Rule), is a frozen bowl standing straight on a refectory
table top (the block below is the table, so the bowl sits at the bottom of its own cell): a squat
deepslate bowl on a low foot, rime crusted over its rim and running down its sides, a skin of ice in
it and frost spilled round its foot. BOWL_DISH is the ice skin's height, where the renderer rests the
held item. Each style has its own atlas (`offering_stand`, `offering_stand_bowl`) so re-running leaves
the stand byte-identical.

Emits per style the block model (`offering_stand[_bowl]`), the atlas and an editable Blockbench project,
and one blockstate (style). No item model. Python 3 + Pillow + NumPy. `--preview` also writes textured
turnarounds to build/model-previews/offering_stand.png and offering_stand_bowl.png.

Coordinates are block pixels. The stand is near-symmetric, so it has no facing; north is z = 0.
"""
import argparse
import json

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, PALETTE, box, build, model_json, preview, separate, spin, write

ATLAS = 'offering_stand'
PARTICLE = 'hemomancy:block/polished_venous_stone'
DISH_TOP = 13.0   # block pixels; the dish's inner floor, where the renderer rests the held item

VENOUS = (60, 18, 26)
MORTAR = (34, 9, 15)
VEIN = (122, 20, 34)


def octagon(name, x0, x1, z0, z1, y0, y1, chamfer, mat, **kw):
    """An octagonal slab as three non-overlapping boxes."""
    return [box(f'{name}_core', (x0, y0, z0 + chamfer), (x1, y1, z1 - chamfer), mat, **kw),
            box(f'{name}_front', (x0 + chamfer, y0, z0), (x1 - chamfer, y1, z0 + chamfer), mat, **kw),
            box(f'{name}_back', (x0 + chamfer, y0, z1 - chamfer), (x1 - chamfer, y1, z1), mat, **kw)]


def foot():
    e = octagon('base', 3, 13, 3, 13, 0, 1, 1.5, 'iron')
    # Four iron toes splayed out from the base flats.
    e.append(box('toe_n', (7, 0, 2.25), (9, .75, 3), 'ferric', skip=('south',)))
    e.append(box('toe_s', (7, 0, 13), (9, .75, 13.75), 'ferric', skip=('north',)))
    e.append(box('toe_w', (2.25, 0, 7), (3, .75, 9), 'ferric', skip=('east',)))
    e.append(box('toe_e', (13, 0, 7), (13.75, .75, 9), 'ferric', skip=('west',)))
    e += octagon('plinth', 4, 12, 4, 12, 1, 2.75, 1.25, 'venous')
    e += octagon('plinth_cap', 4.5, 11.5, 4.5, 11.5, 2.75, 3.25, 1, 'ferric')
    e += octagon('socket', 5.25, 10.75, 5.25, 10.75, 3.25, 4.25, .75, 'venous')
    e.append(box('stem_collar', (6.25, 4.25, 6.25), (9.75, 4.75, 9.75), 'ferric'))
    return e


def stem():
    """Four vertebral bodies on gristle discs, each with a spinous process and two transverse processes."""
    e, y = [], 4.75
    for i in range(4):
        top = y + 1.25
        e.append(box(f'vertebra{i}', (6.75, y, 6.75), (9.25, top, 9.25), 'bone'))
        # Spinous processes alternate front and back; transverse processes splay out sideways.
        if i % 2 == 0:
            e.append(box(f'process{i}', (7.5, y + .25, 5.75), (8.5, top - .25, 6.75), 'bone', skip=('south',)))
        else:
            e.append(box(f'process{i}', (7.5, y + .25, 9.25), (8.5, top - .25, 10.25), 'bone', skip=('north',)))
        e.append(box(f'transverse_w{i}', (5.75, y + .375, 7.5), (6.75, top - .375, 8.5), 'bone', skip=('east',)))
        e.append(box(f'transverse_e{i}', (9.25, y + .375, 7.5), (10.25, top - .375, 8.5), 'bone', skip=('west',)))
        y = top
        if i < 3:
            e.append(box(f'disc{i}', (7.125, y, 7.125), (8.875, y + .5, 8.875), 'gristle'))
            y += .5
    # y is now 11.25: an iron collar seats the dish on the top vertebra.
    e.append(box('dish_collar', (6.5, y, 6.5), (9.5, y + .5, 9.5), 'iron'))
    return e


def dish():
    e = octagon('cradle', 5, 11, 5, 11, 11.75, 12.25, 1.25, 'lacquer_deep')
    e += octagon('dish', 3.5, 12.5, 3.5, 12.5, 12.25, DISH_TOP, 2, 'lacquer', up='dish_floor')
    # Bone rim along the eight edges of the dish.
    rim = (DISH_TOP, DISH_TOP + 1)
    e.append(box('rim_n', (5.5, rim[0], 3.5), (10.5, rim[1], 4), 'bone', skip=('down',)))
    e.append(box('rim_s', (5.5, rim[0], 12), (10.5, rim[1], 12.5), 'bone', skip=('down',)))
    e.append(box('rim_w', (3.5, rim[0], 5.5), (4, rim[1], 10.5), 'bone', skip=('down',)))
    e.append(box('rim_e', (12, rim[0], 5.5), (12.5, rim[1], 10.5), 'bone', skip=('down',)))
    for cx, cz, angle in [(4.68, 4.68, 45), (11.32, 4.68, -45), (4.68, 11.32, -45), (11.32, 11.32, 45)]:
        e.append(box('rim_corner', (cx - 1.45, rim[0], cz - .25), (cx + 1.45, rim[1], cz + .25), 'bone',
                     skip=('down',), rotation=spin('y', angle, (cx, DISH_TOP + .5, cz))))
    # Four bone talons rise from the dish's flats and hook in over the rim.
    e += talon('n', (7.5, 3, 8.5, 3.75), 'x', 45, (8, 14.5, 3.375))
    e += talon('s', (7.5, 12.25, 8.5, 13), 'x', -45, (8, 14.5, 12.625))
    e += talon('w', (3, 7.5, 3.75, 8.5), 'z', -45, (3.375, 14.5, 8))
    e += talon('e', (12.25, 7.5, 13, 8.5), 'z', 45, (12.625, 14.5, 8))
    return e


def talon(side, rect, axis, angle, pivot):
    x0, z0, x1, z1 = rect
    return [box(f'talon_{side}_root', (x0, 12, z0), (x1, 14.75, z1), 'bone'),
            box(f'talon_{side}_hook', (x0 + .125, 14.25, z0 + .125), (x1 - .125, 16, z1 - .125), 'talon',
                rotation=spin(axis, angle, pivot))]


# --- special faces --------------------------------------------------------------------------

def venous(face, lx, ly, w, h, key):
    """Dark venous stone: dressed courses with darker joints and thin live veins."""
    n = noise('venous', key, face, lx, ly)
    if face not in ('up', 'down') and h >= 4 and ly % 4 == 3:
        return clamp(MORTAR, int(n * 6)) + (255,)
    if noise('vein', key, face, lx // 2, ly) < .06:
        return VEIN + (255,)
    return clamp(VENOUS, int(n * 14) - 7 + (10 if ly == 0 and face not in ('up', 'down') else 0)) + (255,)


def dish_floor(face, lx, ly, w, h, key):
    """The lacquered floor of the dish: a dark well ringed in bright lacquer, old blood dried at its heart."""
    cx, cy = (w - 1) / 2, (h - 1) / 2
    r = max(abs(lx - cx), abs(ly - cy))
    n = noise('dish', key, lx, ly)
    if r <= 1:
        return ((98, 10, 22) if n > .3 else (70, 8, 16)) + (255,)
    if r <= 2.5:
        return clamp(PALETTE['lacquer_deep'], int(n * 10) - 5) + (255,)
    return clamp(PALETTE['lacquer'], int(n * 10) - 5 + (16 if lx + ly < w // 2 else 0)) + (255,)


def talon_tex(face, lx, ly, w, h, key):
    """Bone hook darkening to a lacquer-stained point."""
    n = noise('talon', key, face, lx, ly)
    if face == 'up':
        return clamp((120, 40, 44), int(n * 10)) + (255,)
    return clamp(PALETTE['bone'], int(n * 12) - 6 - (h - 1 - ly) * 8) + (255,)


SPECIAL = dict(venous=venous, dish_floor=dish_floor, talon=talon_tex)


# --- the bowl style (Velorum's Litany, the Rule) ---------------------------------------------
#
# A frozen refectory bowl: dark deepslate, squat on a low foot, its rim crusted white with rime that runs
# down the sides in drapes, short icicles under the overhang of its wall, a skin of blue ice filling it and
# frost spilled on the table round its foot. No hematic iron, no copper: the monastery's crockery.

BOWL_ATLAS = 'offering_stand_bowl'
BOWL_PARTICLE = 'minecraft:block/polished_deepslate'
BOWL_DISH = 4.25   # block pixels; the ice skin in the bowl, where the renderer rests the held item
BOWL_RIM = BOWL_DISH + 1

SLATE = (50, 50, 60)
SLATE_DARK = (30, 30, 38)
RIME = (226, 236, 248)
RIME_SHADE = (176, 196, 222)
ICE = (148, 184, 232)
ICE_DEEP = (112, 152, 214)


def bowl_body():
    e = octagon('foot', 5, 11, 5, 11, 0, 1, 1, 'slate')
    e += octagon('belly', 4, 12, 4, 12, 1, 2.25, 1.5, 'slate')
    e += octagon('wall', 3, 13, 3, 13, 2.25, BOWL_DISH, 2, 'slate', up='ice_skin')
    # The rim, rimed along its top.
    rim = (BOWL_DISH, BOWL_RIM)
    e.append(box('rim_n', (5, rim[0], 3), (11, rim[1], 3.75), 'slate', up='rime', skip=('down',)))
    e.append(box('rim_s', (5, rim[0], 12.25), (11, rim[1], 13), 'slate', up='rime', skip=('down',)))
    e.append(box('rim_w', (3, rim[0], 5), (3.75, rim[1], 11), 'slate', up='rime', skip=('down',)))
    e.append(box('rim_e', (12.25, rim[0], 5), (13, rim[1], 11), 'slate', up='rime', skip=('down',)))
    for cx, cz, angle in [(4.1, 4.1, 45), (11.9, 4.1, -45), (4.1, 11.9, -45), (11.9, 11.9, 45)]:
        e.append(box('rim_corner', (cx - 1.5, rim[0], cz - .375), (cx + 1.5, rim[1], cz + .375), 'slate', up='rime',
                     skip=('down',), rotation=spin('y', angle, (cx, BOWL_DISH + .5, cz))))
    return e


def bowl_rime():
    """Rime drapes from the rim down the outer wall, icicles under the wall's overhang, frost on the table."""
    e = []
    # Drapes on the four flats: (side, start, end along the flat, how far down from the rim top).
    for side, a0, a1, drop in [('n', 5.25, 7.25, 2.5), ('n', 8.5, 9.75, 1.5), ('s', 6, 7.5, 1.75),
                               ('s', 8.75, 10.75, 2.75), ('w', 5.5, 7, 2), ('w', 8.25, 10.5, 2.75),
                               ('e', 5.25, 6.75, 2.75), ('e', 8, 9.5, 1.5)]:
        y0, y1 = BOWL_RIM - drop, BOWL_RIM + .25
        if side == 'n':
            e.append(box('drape_n', (a0, y0, 2.75), (a1, y1, 3), 'rime_drape', skip=('south',)))
        elif side == 's':
            e.append(box('drape_s', (a0, y0, 13), (a1, y1, 13.25), 'rime_drape', skip=('north',)))
        elif side == 'w':
            e.append(box('drape_w', (2.75, y0, a0), (3, y1, a1), 'rime_drape', skip=('east',)))
        else:
            e.append(box('drape_e', (13, y0, a0), (13.25, y1, a1), 'rime_drape', skip=('west',)))
    # Short icicles under the wall's overhang (its underside at y 2.25 stands a pixel proud of the belly).
    for x, z, length in [(6.5, 3.25, 1.25), (9.75, 3.25, .75), (12, 7, 1), (6, 12, .75), (10.25, 12, 1.25),
                         (3.25, 9.25, 1)]:
        e.append(box('icicle', (x, 2.25 - length, z), (x + .75, 2.25, z + .75), 'ice', skip=('up',)))
    # Frost spilled on the table round the foot: thin and flat, never quite a ring.
    for lo, hi in [((3.5, 0, 6), (5, .125, 10.5)), ((5.5, 0, 3.75), (10, .125, 5)), ((11, 0, 5), (12.25, .125, 8.5)),
                   ((6.5, 0, 11), (11.5, .125, 12.5)), ((11, 0, 9.5), (11.75, .125, 11))]:
        e.append(box('frost_spill', lo, hi, 'rime', skip=('down',)))
    return e


def slate(face, lx, ly, w, h, key):
    """Dark deepslate crockery, a little frost caught along its upper edges and nowhere else."""
    n = noise('slate', key, face, lx, ly)
    if face == 'up':
        if noise('frost', key, lx, ly) < .2:
            return clamp(RIME_SHADE, int(n * 14) - 7) + (255,)
        return clamp(SLATE, int(n * 12) - 6) + (255,)
    if face == 'down':
        return clamp(SLATE_DARK, int(n * 8)) + (255,)
    if ly == 0 and noise('speck', key, face, lx) < .3:
        return clamp(RIME_SHADE, -int(n * 24)) + (255,)
    if h >= 2 and ly == h - 1:
        return clamp(SLATE_DARK, int(n * 8)) + (255,)
    return clamp(SLATE, int(n * 10) - 5) + (255,)


def rime(face, lx, ly, w, h, key):
    """Hoar frost: white crystals with blue-grey shade between them."""
    n = noise('rime', key, face, lx, ly)
    if n > .8:
        return (246, 250, 255, 255)
    return clamp(RIME if n > .3 else RIME_SHADE, int(n * 12) - 6) + (255,)


def rime_drape(face, lx, ly, w, h, key):
    """A drape of rime: thick white at the rim, breaking into a ragged lower edge over the stone."""
    n = noise('drape', key, face, lx, ly)
    if face in ('up', 'down'):
        return rime(face, lx, ly, w, h, key)
    if ly >= h - 1 and noise('ragged', key, face, lx) < .5:
        return clamp(RIME_SHADE, -26 + int(n * 10)) + (255,)
    return clamp(RIME if ly < h // 2 or n > .4 else RIME_SHADE, int(n * 12) - 6 - ly * 3) + (255,)


def ice_skin(face, lx, ly, w, h, key):
    """The ice in the bowl: clear blue darkening toward the middle, white frost feathering in from the rim, a pale
    crack or two across it."""
    cx, cy = (w - 1) / 2, (h - 1) / 2
    r = max(abs(lx - cx), abs(ly - cy)) / max(cx, 1)
    n = noise('ice_skin', key, lx, ly)
    if r > .8 and n < .6:
        return clamp(RIME_SHADE, int(n * 16)) + (255,)
    if (lx - ly) % 5 == 0 and noise('crack', key, (lx + ly) // 3) < .5 and r < .8:
        return (214, 230, 250, 255)
    deep = r < .4
    return clamp(ICE_DEEP if deep else ICE, int(n * 14) - 7 + (6 if lx + ly < w // 2 else 0)) + (255,)


def ice(face, lx, ly, w, h, key):
    n = noise('ice', key, face, lx, ly)
    if face not in ('up', 'down') and ly == h - 1:
        return (232, 242, 255, 255)
    return clamp(ICE, int(n * 16) - 4) + (255,)


BOWL_SPECIAL = dict(slate=slate, rime=rime, rime_drape=rime_drape, ice_skin=ice_skin, ice=ice)


def bounds(elements):
    lo = [min(el['from'][a] for el in elements) for a in range(3)]
    hi = [max(el['to'][a] for el in elements) for a in range(3)]
    return lo, hi


STYLES = {'stand': '', 'bowl': '_bowl'}      # style -> model name suffix


def blockstate():
    return {'variants': {f'style={style}': {'model': f'hemomancy:block/offering_stand{suffix}'}
                         for style, suffix in STYLES.items()}}


def export_bowl(preview_too):
    body = bowl_body() + bowl_rime()
    moved = separate(body)
    atlas, models = build({'body': body}, BOWL_SPECIAL)
    atlas.save(ASSETS / f'textures/block/{BOWL_ATLAS}.png')
    model = model_json(models['body'], BOWL_ATLAS, atlas.height, BOWL_PARTICLE)
    write({'block/offering_stand_bowl': model}, 'offering_stand_bowl', model)
    lo, hi = bounds(models['body'])
    print(f'Exported Offering Stand (bowl): {len(body)} cubes, {moved} faces separated, 128x{atlas.height} atlas')
    print(f'BOWL_DISH = {BOWL_DISH:g} px; model bounds {lo} .. {hi}')
    if preview_too:
        preview(model['elements'], BOWL_ATLAS,
                f'Offering Stand, bowl style - on a table top, actual atlas (ice skin y={BOWL_DISH:g})',
                'offering_stand_bowl', lift=-5, scale=22)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export_bowl(args.preview)
    body = foot() + stem() + dish()
    moved = separate(body)
    atlas, models = build({'body': body}, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    model = model_json(models['body'], ATLAS, atlas.height, PARTICLE)
    write({'block/offering_stand': model}, 'offering_stand', model)
    (ASSETS / 'blockstates/offering_stand.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    lo, hi = bounds(models['body'])
    print(f'Exported Offering Stand: {len(body)} cubes, {moved} faces separated, 128x{atlas.height} atlas')
    print(f'DISH_TOP = {DISH_TOP:g} px; model bounds {lo} .. {hi}')
    if args.preview:
        preview(model['elements'], ATLAS, f'Offering Stand - block model, actual atlas (dish floor y={DISH_TOP:g})',
                'offering_stand')


if __name__ == '__main__':
    main()

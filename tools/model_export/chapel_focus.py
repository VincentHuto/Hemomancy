"""Author the Chapel Focus: the reliquary altar at the heart of a Chapel wing.

A small altar of dark venous stone on a stepped plinth, bone knuckles on the plinth's corners. The altar die is
bound at its corners by hematic iron pilasters and ribbed in bone round both flanks and the back, where the ribs
bed into an iron spine. Its north face carries an iron-framed Crimson Lacquer panel holding a lacquer node in a
bone rim, the same node the ring door wears on each of its rings. Over the die an overhanging mensa on bone corbels, strapped across its brow in iron, carries an iron
socket and a lacquer cup; in the cup sits the lens, a faceted blood-glass gem, gripped by four bone talons. A runnel
is cut through the mensa from the cup to the brow.

Unlit, the lens is near-black glass, the node a dull boss and the runnel dried. When the wing's trial is complete
the focus is LIT: the lit model swaps the lens, the node and the runnel for full-bright crimson, and the node on the
ring door wakes with it.

Emits the unlit and lit block models, the item model, the atlas, the blockstate (facing x lit) and an editable
Blockbench project. Python 3 + Pillow + NumPy. `--preview` writes build/model-previews/chapel_focus.png and
chapel_focus_lit.png.

Coordinates are block pixels for facing=north; the node faces north (-z). 16 px per block (density 1).
"""
import argparse
import json

import carved_stone
from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, PALETTE, box, build, mirror_x, model_json, preview, separate, spin, write

ATLAS = 'chapel_focus'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}
LENS = (8, 8)             # the lens's centre (x, z)
MENSA = 10.5              # the mensa's top face

# The ring door's lit-node colours, so a lit focus reads as the node it feeds.
GLOW_HOT = (255, 120, 112)
GLOW = (236, 40, 52)
GLOW_DEEP = (176, 18, 36)

SHAPE = [(2, 0, 2, 14, 1.5, 14), (2.75, 1.5, 2.75, 13.25, 2.5, 13.25), (3.5, 2.5, 4.25, 12.5, 9, 11.5),
         (2.75, 9, 3.25, 13.25, 10.5, 12.5), (5, 10.5, 5, 11, 13, 11)]


def pair(el):
    return [el, mirror_x(el)]


def octagon(name, x0, x1, z0, z1, y0, y1, chamfer, mat, **kw):
    """An octagonal slab as three non-overlapping boxes."""
    return [box(f'{name}_core', (x0, y0, z0 + chamfer), (x1, y1, z1 - chamfer), mat, **kw),
            box(f'{name}_front', (x0 + chamfer, y0, z0), (x1 - chamfer, y1, z0 + chamfer), mat, **kw),
            box(f'{name}_back', (x0 + chamfer, y0, z1 - chamfer), (x1 - chamfer, y1, z1), mat, **kw)]


def plinth():
    e = [box('plinth', (2, 0, 2), (14, 1.5, 14), 'stone'),
         box('plinth_step', (2.75, 1.5, 2.75), (13.25, 2.5, 13.25), 'stone', skip=('down',)),
         box('kerb', (3.5, 2.5, 4.5), (12.5, 3.25, 11.5), 'iron', skip=('down',))]
    for z in (3, 11.75):
        e += pair(box('knuckle', (3, 2.5, z), (4.25, 3.5, z + 1.25), 'bone', skip=('down',)))
    return e


def die():
    e = [box('die', (4, 3.25, 5), (12, 9, 11), 'stone', north='dressed', skip=('down', 'up'))]
    # Iron pilasters on the die's four corners.
    for z in (4.5, 10.5):
        e += pair(box('pilaster', (3.5, 3.25, z), (4.5, 9, z + 1), 'iron', skip=('down',)))
    # Bone ribs down both flanks, between the pilasters.
    for i, y in enumerate((4.25, 5.75, 7.25)):
        e += pair(box(f'rib{i}', (3.75, y, 5.5), (4, y + .75, 10.5), 'rib', skip=('east',)))
        e.append(box(f'back_rib{i}', (4.5, y, 11), (11.5, y + .75, 11.25), 'rib', skip=('north',)))
    # An iron spine down the back, the ribs bedded into it.
    e.append(box('back_spine', (7.25, 3.25, 11), (8.75, 9, 11.5), 'iron', skip=('north', 'down')))
    # The north face: a bone-framed lacquer panel carrying the node in its bone rim.
    e.append(box('panel', (5, 3.75, 4.75), (11, 8.5, 5), 'front_panel', skip=('south',)))
    e.append(box('node_rim', (6.5, 4.75, 4.5), (9.5, 7.75, 4.75), 'bone', north='node_rim', skip=('south',)))
    return e


def mensa():
    e = [box('mensa', (3, 9, 3.5), (13, MENSA, 12.5), 'dressed', up='mensa_top'),
         box('brow_strap', (2.75, 9.25, 3.25), (13.25, 10.25, 3.5), 'iron', skip=('south',))]
    e += pair(box('strap_stud', (3.25, 9.5, 3), (4, 10, 3.25), 'bone', skip=('south',)))
    e += pair(box('corbel', (5.25, 8, 3.75), (6.25, 9, 5), 'bone', skip=('up', 'south')))
    e += pair(box('corbel_tip', (5.5, 7.25, 4), (6, 8, 4.75), 'bone', skip=('up', 'south')))
    return e


def reliquary():
    cx, cz = LENS
    e = octagon('socket', cx - 3, cx + 3, cz - 3, cz + 3, MENSA, MENSA + 1, 1.25, 'ferric')
    e += octagon('cup', cx - 2.25, cx + 2.25, cz - 2.25, cz + 2.25, MENSA + 1, MENSA + 1.5, 1, 'lacquer')
    # Four bone talons rise from the socket's flats and hook in over the lens.
    e += talon('n', (cx - .5, cz - 3, cx + .5, cz - 2.25), 'x', 22.5, (cx, 12.25, cz - 2.6))
    e += talon('s', (cx - .5, cz + 2.25, cx + .5, cz + 3), 'x', -22.5, (cx, 12.25, cz + 2.6))
    e += talon('w', (cx - 3, cz - .5, cx - 2.25, cz + .5), 'z', -22.5, (cx - 2.6, 12.25, cz))
    e += talon('e', (cx + 2.25, cz - .5, cx + 3, cz + .5), 'z', 22.5, (cx + 2.6, 12.25, cz))
    return e


def talon(side, rect, axis, angle, pivot):
    x0, z0, x1, z1 = rect
    return [box(f'talon_{side}_root', (x0, MENSA + 1, z0), (x1, 12.25, z1), 'bone', skip=('down',)),
            box(f'talon_{side}_hook', (x0 + .125, 12, z0 + .125), (x1 - .125, 13, z1 - .125), 'talon',
                rotation=spin(axis, angle, pivot))]


def waking(lit):
    """The parts that wake: the lens, the node and the runnel."""
    cx, cz = LENS
    suffix, extra = ('glow', BRIGHT) if lit else ('dull', None)
    lens = f'lens_{suffix}'
    return [box('lens', (cx - 1.25, MENSA + 1.5, cz - 1.25), (cx + 1.25, 13, cz + 1.25), lens, skip=('down',),
                extra=extra),
            box('lens_facet', (cx - 1.25, MENSA + 1.25, cz - 1.25), (cx + 1.25, 12.75, cz + 1.25), lens,
                skip=('down',), rotation=spin('y', 45, (cx, 12, cz)), extra=extra),
            box('node', (7, 5.25, 4.25), (9, 7.25, 4.5), f'node_{suffix}', skip=('south',), extra=extra),
            box('runnel', (7.5, MENSA, 3.5), (8.5, MENSA + .05, cz - 3), f'runnel_{suffix}',
                skip=('down', 'south', 'east', 'west'), extra=extra),
            box('runnel_lip', (7.5, 9.5, 3.45), (8.5, MENSA + .05, 3.5), f'runnel_{suffix}',
                skip=('south', 'east', 'west', 'up', 'down'), extra=extra)]


# --- special faces --------------------------------------------------------------------------

def rib(face, lx, ly, w, h, key):
    """A bone rib, its ends stained where they bed into the iron."""
    n = noise('rib', key, face, lx, ly)
    if face in ('west', 'east') and lx in (0, w - 1):
        return clamp((168, 120, 104), int(n * 10)) + (255,)
    return clamp(PALETTE['bone'], int(n * 12) - 6 - (16 if ly == h - 1 else 0)) + (255,)


def front_panel(face, lx, ly, w, h, key):
    """Crimson Lacquer in an iron frame, wet along its top, a dried runnel of blood under the node."""
    n = noise('front_panel', key, face, lx, ly)
    if face != 'north':
        return clamp(PALETTE['iron'], int(n * 8)) + (255,)
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(PALETTE['iron'], int(n * 10) - 5 + (16 if ly == 0 else 0)) + (255,)
    if ly >= h - 2 and lx in (w // 2 - 1, w // 2) and n < .7:
        return (70, 10, 20, 255)
    return clamp(PALETTE['lacquer'], (h / 2 - ly) / h * 24 + int(n * 6) - 3 + (30 if ly == 1 else 0)) + (255,)


def node_rim(face, lx, ly, w, h, key):
    """The node's bone rim, worn pale on its upper lip."""
    n = noise('node_rim', key, face, lx, ly)
    return clamp(PALETTE['bone'], int(n * 10) - 5 + (10 if ly == 0 else -14 if ly == h - 1 else 0)) + (255,)


def mensa_top(face, lx, ly, w, h, key):
    """The mensa's top: dressed stone, darkened where the runnel's blood has stained either side of its cut."""
    x, _, z = carved_stone.world(key, face, lx, ly)
    rgb = carved_stone.stone_rgb(key, face, lx, ly, w, h, tone=6, mottle=12)
    if abs(x - 8) < 1.5 and z < 5.5 and noise('stain', lx, ly) < .55:
        rgb = tuple(int(c * .65 + s * .35) for c, s in zip(rgb, (96, 30, 38)))
    elif 4.5 < x < 11.5 and 4.5 < z < 11.5:
        rgb = clamp(rgb, -16)       # the socket's shadow ring
    return rgb + (255,)


def talon_tex(face, lx, ly, w, h, key):
    """Bone hook darkening to a lacquer-stained point."""
    n = noise('talon', key, face, lx, ly)
    if face == 'up':
        return clamp((120, 40, 44), int(n * 10)) + (255,)
    return clamp(PALETTE['bone'], int(n * 12) - 6 - (h - 1 - ly) * 10) + (255,)


def lens(lit):
    """Blood-glass: near-black with one cold glint unlit; crimson with a hot heart when lit."""
    def paint(face, lx, ly, w, h, key):
        n = noise('lens', key, face, lx, ly)
        if face == 'up':
            d = max(abs(lx - (w - 1) / 2), abs(ly - (h - 1) / 2))
            if lit:
                return (GLOW_HOT if d < .6 else GLOW if d < 1.2 else GLOW_DEEP) + (255,)
            if lx == 0 and ly == 0:
                return (112, 82, 90, 255)
            return clamp((30, 10, 16), int(n * 8)) + (255,)
        if lit:
            return (GLOW if ly < h - 1 and n > .3 else GLOW_DEEP) + (255,)
        if ly == 0 and lx == 1:
            return (86, 52, 60, 255)            # a glint along the facet's upper edge
        return clamp((44, 12, 20), int(n * 8) - (8 if ly == h - 1 else 0)) + (255,)
    return paint


def node(lit):
    """The ring door's node in small: a dull lacquer boss, or blood-light hot at its heart."""
    def paint(face, lx, ly, w, h, key):
        if lit:
            if face == 'north':
                return (GLOW_HOT if (lx, ly) == (w - 1, 0) else GLOW) + (255,)
            return GLOW + (255,)
        n = noise('node', key, face, lx, ly)
        if face == 'north' and lx == 0 and ly == 0:
            return clamp(PALETTE['lacquer'], 20) + (255,)
        return clamp(PALETTE['lacquer_deep'], -18 + int(n * 8)) + (255,)
    return paint


def runnel(lit):
    def paint(face, lx, ly, w, h, key):
        n = noise('runnel', key, face, lx, ly)
        if lit:
            return (GLOW_HOT if n > .85 else GLOW if n > .3 else GLOW_DEEP) + (255,)
        return ((60, 16, 24) if n > .7 else (36, 14, 20)) + (255,)
    return paint


SPECIAL = dict(carved_stone.SPECIAL, rib=rib, front_panel=front_panel, node_rim=node_rim, mensa_top=mensa_top, talon=talon_tex,
               lens_dull=lens(False), lens_glow=lens(True), node_dull=node(False), node_glow=node(True),
               runnel_dull=runnel(False), runnel_glow=runnel(True))


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for lit in ('false', 'true'):
            model = 'hemomancy:block/chapel_focus' + ('_lit' if lit == 'true' else '')
            variants[f'facing={facing},lit={lit}'] = dict({'model': model}, **({'y': y} if y else {}))
    return {'variants': variants}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body, dull, glow = plinth() + die() + mensa() + reliquary(), waking(False), waking(True)
    moved = separate(body + dull)
    separate(body + glow)
    groups = {'body': body, 'dull': dull, 'glow': glow}
    carved_stone.register(groups)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'
    unlit = model_json(models['body'] + models['dull'], ATLAS, atlas.height, particle)
    lit = model_json(models['body'] + models['glow'], ATLAS, atlas.height, particle)
    write({'block/chapel_focus': unlit, 'block/chapel_focus_lit': lit}, 'chapel_focus', lit)
    (ASSETS / 'models/item/chapel_focus.json').write_text(
        json.dumps({'parent': 'hemomancy:block/chapel_focus'}, indent=2) + '\n')
    (ASSETS / 'blockstates/chapel_focus.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    print(f'Exported Chapel Focus: {len(body) + len(dull)} cubes ({len(glow)} swapped when lit), '
          f'{moved} faces separated, 128x{atlas.height} atlas')
    print('Voxel shape (Block.box, north):', SHAPE)
    if args.preview:
        preview(unlit['elements'], ATLAS, 'Chapel Focus - unlit block model, actual atlas', 'chapel_focus')
        preview(lit['elements'], ATLAS, 'Chapel Focus - lit (lens, node and runnel full-bright)', 'chapel_focus_lit')


if __name__ == '__main__':
    main()

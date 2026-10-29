"""Author the Spawn Mark: the cracked floor seal a Downfall wave rises from.

A slab of dark venous stone nearly the width of the cell, capped by a hematic iron ring seal pinned with
four bone pegs. Inside the ring sits an iron boss; the wave the seal answers is cut across it as one, two
or three notches (blockstate `wave` 0-2). Cracks radiate from the ring out to the slab's broken edge.
While the wave is due (`due`), the cracks and the notches seep full-bright crimson.

A second style, `ash` (Putriciel's Purge Corridors, where bog revenants rise from the ash), sinks the same seal in a
low heap of grey-black ash: the slab is buried to its ring under ash cracked over the old fissures, the ring and
pegs are sooted, and charred bone bits lie in the heap. Due, its cracks seep the same crimson. It paints its own
atlas (`spawn_mark_ash`), so the venous outputs stay byte-identical.

The plate is 0..2 px tall and has no collision. Model space is block pixels, 16 per block (density 1).

Emits `spawn_mark[_ash]_<0|1|2>` and `spawn_mark[_ash]_<0|1|2>_due`, the painted atlases, the due x wave x style
blockstate and editable Blockbench projects. Python 3 + Pillow + NumPy. `--preview` writes
build/model-previews/spawn_mark.png and spawn_mark_ash.png.
"""
import argparse
import json
import math

import numpy as np
from PIL import Image, ImageDraw

from clairaudiograph import clamp, noise, render
from fixture_atlas import ASSETS, PALETTE, ROOT, box, build, model_json, separate, spin, write

ATLAS = 'spawn_mark'
PARTICLE = 'hemomancy:block/polished_venous_stone'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}

STONE = (68, 12, 24)        # dark venous stone, a shade under venous_stone's mid tone
CRACK = (14, 2, 6)
SEEP = (192, 18, 34)
SEEP_HOT = (242, 58, 56)
GROOVE = (34, 8, 14)

SLAB_TOP = 1.0
RING_OUT, RING_IN, RING_TOP = 5.5, 4.5, 1.75     # octagon apothems around (8, 8)
BOSS_LO, BOSS_HI, BOSS_TOP = 4.5, 11.5, 2.0      # 7 px across: room for three 1 px notches between four bars
BOSS_Z = (5.5, 10.5)
GLOW_Y = 1.05                                    # the seep plane, a hair over the slab
# Notches across the boss for each wave, as x spans; the bars fill the rest.
NOTCHES = {0: [(7.5, 8.5)], 1: [(6.5, 7.5), (8.5, 9.5)], 2: [(5.5, 6.5), (7.5, 8.5), (9.5, 10.5)]}

# Element key -> its 'lo' corner, so painters can draw top faces in block coordinates.
ORIGIN = {}


# --- geometry ---------------------------------------------------------------------------------

def plate():
    e = [box('slab', (1, 0, 1), (15, SLAB_TOP, 15), 'stone', up='stone_top')]
    # Octagonal ring seal: four straight sides on the axes, four turned 45 degrees across the corners.
    side = 2 * RING_OUT * math.tan(math.radians(22.5))
    mid = (RING_OUT + RING_IN) / 2
    for lo, hi in [((8 - side / 2, SLAB_TOP, 8 - RING_OUT), (8 + side / 2, RING_TOP, 8 - RING_IN)),
                   ((8 - side / 2, SLAB_TOP, 8 + RING_IN), (8 + side / 2, RING_TOP, 8 + RING_OUT)),
                   ((8 - RING_OUT, SLAB_TOP, 8 - side / 2), (8 - RING_IN, RING_TOP, 8 + side / 2)),
                   ((8 + RING_IN, SLAB_TOP, 8 - side / 2), (8 + RING_OUT, RING_TOP, 8 + side / 2))]:
        e.append(box('ring', lo, hi, 'ring', skip=('down',)))
    d = mid / math.sqrt(2)
    for sx, sz, angle in [(-1, -1, 45), (1, -1, -45), (-1, 1, -45), (1, 1, 45)]:
        cx, cz = 8 + sx * d, 8 + sz * d
        e.append(box('ring_corner', (cx - side / 2, SLAB_TOP, cz - .5), (cx + side / 2, RING_TOP - .01, cz + .5), 'ring',
                     skip=('down',), rotation=spin('y', angle, (cx, 1.4, cz))))
    # Bone pegs pinning the ring at its four axis points.
    for cx, cz in [(8, 8 - mid), (8, 8 + mid), (8 - mid, 8), (8 + mid, 8)]:
        e.append(box('ring_peg', (cx - .625, SLAB_TOP, cz - .625), (cx + .625, BOSS_TOP, cz + .625), 'bone',
                     skip=('down',)))
    return e


def boss(wave):
    """The iron boss cut by the wave's notches; returns the bars and the notch floors."""
    edges = [BOSS_LO] + [x for span in NOTCHES[wave] for x in span] + [BOSS_HI]
    bars = [box(f'boss_bar{i}', (edges[i], SLAB_TOP, BOSS_Z[0]), (edges[i + 1], BOSS_TOP, BOSS_Z[1]), 'boss',
                skip=('down',)) for i in range(0, len(edges), 2)]
    grooves = [box('notch', (x0, SLAB_TOP, BOSS_Z[0]), (x1, SLAB_TOP + .25, BOSS_Z[1]), 'groove',
                   skip=('down', 'east', 'west')) for x0, x1 in NOTCHES[wave]]
    return bars, grooves


def seep(wave):
    """Full-bright notch floors that replace the dark grooves while the wave is due."""
    return [box('notch_seep', (x0, SLAB_TOP, BOSS_Z[0]), (x1, SLAB_TOP + .3, BOSS_Z[1]), 'seep',
                skip=('down', 'east', 'west'), extra=BRIGHT) for x0, x1 in NOTCHES[wave]]


def crack_plane():
    return [box('crack_seep', (1, SLAB_TOP, 1), (15, GLOW_Y, 15), 'crack_seep',
                skip=('down', 'north', 'south', 'east', 'west'), extra=BRIGHT)]


# --- cracks -------------------------------------------------------------------------------------

# The cracks over the 16 x 16 cell, row = z (north at the top), column = x. '+' is near the seal and seeps
# hottest, '#' runs on out to the slab's edge. What falls under the ring or the boss is hidden.
CRACKS = """
................
.##....#.....##.
..##..##....##..
...++.+....++...
....++++...+....
.......+..++....
..............#.
.###........+++.
...+............
................
..........+.....
...++.....++....
..++..+....+++..
.##..###.....++.
.#..##.#......#.
................
"""


def crack_mask():
    """Pixel mask of the cracks, indexed [z][x]: 2 near the seal, 1 further out."""
    rows = CRACKS.strip().splitlines()
    return np.array([[{'.': 0, '#': 1, '+': 2}[c] for c in row] for row in rows])


MASK = crack_mask()


def top_cell(key, lx, ly):
    lo = ORIGIN[key]
    return int(math.floor(lo[0] + lx + .5)), int(math.floor(lo[2] + ly + .5))


# --- painters -----------------------------------------------------------------------------------

def stone(face, lx, ly, w, h, key):
    n = noise('stone', key, face, lx, ly)
    rgb = clamp(STONE, int(n * 16) - 8 - (10 if ly == h - 1 else 0) + (8 if ly == 0 else 0))
    if n > .94:
        rgb = (96, 20, 34)
    return rgb + (255,)


def stone_top(face, lx, ly, w, h, key):
    x, z = top_cell(key, lx, ly)
    n = noise('stone_top', x, z)
    if MASK[z, x]:
        return CRACK + (255,)
    if MASK[z - 1, x] or MASK[z, x - 1]:
        return clamp(STONE, 22 + int(n * 6)) + (255,)   # the broken lip catching light below a crack
    # Bevelled, chipped edge: paler where the slab lip catches the light.
    edge = x in (1, 14) or z in (1, 14)
    shift = int(n * 16) - 8 + (10 if edge else 0)
    if noise('fleck', x, z) > .97:
        return (84, 18, 30, 255)
    return clamp(STONE, shift) + (255,)


def ring(face, lx, ly, w, h, key):
    """Hematic iron, blood weeping out along the grain; a pale sheen on the upper edge of the sides."""
    n = noise('ring', key, face, lx, ly)
    rgb = clamp(PALETTE['iron'], int(n * 12) - 6 + (16 if face == 'up' else 0) + (14 if ly == 0 and face != 'up' else 0))
    if noise('vein', key, face, lx) < .12 and face == 'up':
        rgb = (104, 26, 36)
    return rgb + (255,)


def boss_paint(face, lx, ly, w, h, key):
    n = noise('boss', key, face, lx, ly)
    rgb = clamp(PALETTE['ferric'], int(n * 10) - 14 + (10 if face == 'up' and ly == 0 else 0)
                - (12 if face == 'up' and ly == h - 1 else 0))
    return rgb + (255,)


def groove(face, lx, ly, w, h, key):
    n = noise('groove', key, face, lx, ly)
    return ((70, 12, 22) if n > .85 else clamp(GROOVE, int(n * 8) - 4)) + (255,)


def seep_paint(face, lx, ly, w, h, key):
    n = noise('seep', key, face, lx, ly)
    return (SEEP_HOT if n > .7 else SEEP) + (255,)


def crack_seep(face, lx, ly, w, h, key):
    x, z = top_cell(key, lx, ly)
    if not MASK[z, x]:
        return (0, 0, 0, 0)
    return (SEEP_HOT if MASK[z, x] == 2 else SEEP) + (255,)


SPECIAL = dict(stone=stone, stone_top=stone_top, ring=ring, boss=boss_paint, groove=groove, seep=seep_paint,
               crack_seep=crack_seep)


# --- the ash style ------------------------------------------------------------------------------

ASH_ATLAS = 'spawn_mark_ash'
ASH = (88, 84, 82)
ASH_DARK = (40, 36, 36)
CHAR = (24, 18, 18)


def heap():
    """The ash heaped round the buried slab: a low bank on the edges, crests in the corners, spills onto the slab."""
    e = [box('heap_n', (0, 0, 0), (16, 1.5, 2.5), 'ash', skip=('down',)),
         box('heap_s', (0, 0, 13.5), (16, 1.5, 16), 'ash', skip=('down',)),
         box('heap_w', (0, 0, 2.5), (2.5, 1.5, 13.5), 'ash', skip=('down',)),
         box('heap_e', (13.5, 0, 2.5), (16, 1.5, 13.5), 'ash', skip=('down',))]
    for lo, hi in [((.5, 1.5, .5), (3.5, 2.5, 2.5)), ((12.5, 1.5, .25), (15.5, 2.25, 2.25)),
                   ((.25, 1.5, 11), (2.5, 2.75, 15)), ((11.5, 1.5, 13.75), (15.75, 2.5, 15.75)),
                   ((6, 1.5, .25), (9.5, 2, 2)), ((13.75, 1.5, 6), (15.75, 2, 9.5)),
                   ((1, 2.5, .75), (3, 3, 2.25)), ((.75, 2.75, 12), (2, 3.25, 14))]:
        e.append(box('heap_crest', lo, hi, 'ash', skip=('down',)))
    for lo, hi in [((2.5, SLAB_TOP, 2.5), (4, 1.5, 3.75)), ((12, SLAB_TOP, 2.5), (13.5, 1.5, 3.75)),
                   ((2.5, SLAB_TOP, 12.25), (4, 1.5, 13.5)), ((12, SLAB_TOP, 12.25), (13.5, 1.5, 13.5))]:
        e.append(box('heap_spill', lo, hi, 'ash', skip=('down',)))
    # Charred bone bits lying in the heap.
    e += [box('bone_bit', (9.75, 1.5, .6), (12.25, 2, 1.4), 'char_bone', skip=('down',)),
          box('bone_bit', (14, 1.5, 10.5), (14.75, 2, 13), 'char_bone', skip=('down',)),
          box('bone_bit', (4, 1.5, 14.25), (7, 1.9, 15), 'char_bone', skip=('down',)),
          box('bone_knob', (3.4, 1.5, 14), (4, 2.1, 15.25), 'char_bone', skip=('down',))]
    return e


def ash(face, lx, ly, w, h, key):
    """Grey-black ash: soft and mottled, darker low down, the odd white fleck."""
    n = noise('ash', key, face, lx, ly)
    if n > .96:
        return (150, 146, 142, 255)
    dark = face not in ('up', 'down') and ly >= h - 1
    return clamp(ASH_DARK if n < .3 or dark else ASH, int(n * 14) - 7) + (255,)


def ash_floor(face, lx, ly, w, h, key):
    """The ash over the buried slab, cracked open over the seal's old fissures."""
    x, z = top_cell(key, lx, ly)
    n = noise('ash_floor', x, z)
    if MASK[z, x]:
        return CRACK + (255,)
    if MASK[z - 1, x] or MASK[z, x - 1]:
        return clamp(ASH, 10 + int(n * 6)) + (255,)
    return clamp(ASH_DARK if n < .2 else ASH, int(n * 14) - 4) + (255,)


def sooted_ring(face, lx, ly, w, h, key):
    rgb = ring(face, lx, ly, w, h, key)[:3]
    k = .55 + .3 * noise('soot', key, face, lx, ly)
    return tuple(int(c * (1 - k) + d * k) for c, d in zip(rgb, CHAR)) + (255,)


def char_bone(face, lx, ly, w, h, key):
    """Bone burned black, pale only where the char flaked off."""
    n = noise('char_bone', key, face, lx, ly)
    if n > .75:
        return clamp((170, 156, 132), int(n * 10) - 10) + (255,)
    return clamp(CHAR if n < .45 else (58, 46, 40), int(n * 10)) + (255,)


ASH_SPECIAL = dict(SPECIAL, stone=ash, stone_top=ash_floor, ring=sooted_ring, bone=char_bone, ash=ash,
                   char_bone=char_bone)


# --- output -------------------------------------------------------------------------------------

STYLES = {'venous': '', 'ash': '_ash'}      # style -> model name suffix


def blockstate():
    variants = {}
    for wave in range(3):
        for due in (False, True):
            for style, suffix in STYLES.items():
                name = f'spawn_mark{suffix}_{wave}' + ('_due' if due else '')
                variants[f'due={str(due).lower()},style={style},wave={wave}'] = {'model': f'hemomancy:block/{name}'}
    return {'variants': variants}


def bounds(elements):
    lo = [min(e['from'][k] for e in elements) for k in range(3)]
    hi = [max(e['to'][k] for e in elements) for k in range(3)]
    return [math.floor(v) for v in lo], [math.ceil(v) for v in hi]


def preview(models, atlas=ATLAS, prefix='spawn_mark', title='Spawn Mark'):
    """Six models in a row seen from above, then the two extremes up close at a lower and a steeper angle."""
    texture = np.array(Image.open(ASSETS / f'textures/block/{atlas}.png').convert('RGBA'))

    def raised(elements):
        out = json.loads(json.dumps(elements))
        for el in out:   # centre the plate in the frame (render centres on y = 8)
            el['from'][1] += 7
            el['to'][1] += 7
            if 'rotation' in el:
                el['rotation']['origin'][1] += 7
        return out

    order = [f'{prefix}_{w}' + s for s in ('', '_due') for w in range(3)]
    small, big = 260, 420
    sheet = Image.new('RGBA', (small * 6, 30 + small + big), (30, 24, 28, 255))
    draw = ImageDraw.Draw(sheet)
    for i, name in enumerate(order):
        sheet.paste(render(raised(models[name]), {'atlas': texture}, 20, 60, small, 12), (small * i, 30))
        draw.text((small * i + 8, 34), name, fill=(220, 200, 200))
    for i, (name, yaw, pitch) in enumerate([(f'{prefix}_0', 25, 35), (f'{prefix}_0_due', -30, 70),
                                            (f'{prefix}_2', 150, 35), (f'{prefix}_2_due', 200, 70)]):
        x = i * (small * 6 // 4)
        sheet.paste(render(raised(models[name]), {'atlas': texture}, yaw, pitch, big, 22), (x, 30 + small))
        draw.text((x + 8, 34 + small), f'{name} yaw {yaw} pitch {pitch}', fill=(220, 200, 200))
    draw.text((10, 8), f'{title} - wave 0/1/2, dormant and due (seep is full-bright)', fill=(220, 200, 200))
    out = ROOT / f'build/model-previews/{prefix}.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(out)
    print(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    groups = {'plate': plate(), 'cracks': crack_plane()}
    for wave in range(3):
        groups[f'bars{wave}'], groups[f'grooves{wave}'] = boss(wave)
        groups[f'seep{wave}'] = seep(wave)
    moved = 0
    for wave in range(3):
        moved += separate(groups['plate'] + groups[f'bars{wave}'] + groups[f'grooves{wave}'])
    for g, elements in groups.items():
        for i, el in enumerate(elements):
            ORIGIN[(g, i)] = el['lo']
    atlas, built = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')

    models, outputs = {}, {}
    for wave in range(3):
        dormant = built['plate'] + built[f'bars{wave}'] + built[f'grooves{wave}']
        due = built['plate'] + built[f'bars{wave}'] + built[f'seep{wave}'] + built['cracks']
        for name, elements in ((f'spawn_mark_{wave}', dormant), (f'spawn_mark_{wave}_due', due)):
            models[name] = elements
            outputs[f'block/{name}'] = model_json(elements, ATLAS, atlas.height, PARTICLE)
    write(outputs, 'spawn_mark', outputs['block/spawn_mark_2_due'])

    # The ash style: the same seal with its slab painted as buried ash and the heap round it, on its own atlas.
    ash_groups = {g: elements for g, elements in groups.items()}
    ash_groups['heap'] = heap()
    moved += separate(ash_groups['heap'])
    for i, el in enumerate(ash_groups['heap']):
        ORIGIN[('heap', i)] = el['lo']
    ash_atlas, ash_built = build(ash_groups, ASH_SPECIAL)
    ash_atlas.save(ASSETS / f'textures/block/{ASH_ATLAS}.png')
    ash_models, ash_outputs = {}, {}
    for wave in range(3):
        base = ash_built['plate'] + ash_built['heap'] + ash_built[f'bars{wave}']
        dormant = base + ash_built[f'grooves{wave}']
        due = base + ash_built[f'seep{wave}'] + ash_built['cracks']
        for name, elements in ((f'spawn_mark_ash_{wave}', dormant), (f'spawn_mark_ash_{wave}_due', due)):
            ash_models[name] = elements
            ash_outputs[f'block/{name}'] = model_json(elements, ASH_ATLAS, ash_atlas.height,
                                                      f'hemomancy:block/{ASH_ATLAS}')
    write(ash_outputs, 'spawn_mark_ash', ash_outputs['block/spawn_mark_ash_2_due'])
    (ASSETS / 'blockstates/spawn_mark.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    for name, elements in models.items():
        lo, hi = bounds(elements)
        print(f'  {name}: {len(elements)} cubes, bounds Block.box({lo[0]}, {lo[1]}, {lo[2]}, {hi[0]}, {hi[1]}, {hi[2]})')
    print(f'Exported Spawn Mark: 6 models, {moved} faces separated, 128x{atlas.height} atlas; '
          f'ash style: 6 models (+{len(ash_groups["heap"])} heap cubes), 128x{ash_atlas.height} atlas')
    if args.preview:
        preview(models)
        preview(ash_models, ASH_ATLAS, 'spawn_mark_ash', 'Spawn Mark (ash)')


if __name__ == '__main__':
    main()

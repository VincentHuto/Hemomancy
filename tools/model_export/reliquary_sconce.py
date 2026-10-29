"""Author the Reliquary Sconce: the standard mausoleum wall light.

A hematic iron back plate carries a bone-framed lacquer inset and is crowned with a turned diamond, a bone
boss with a crimson eye and a bone finial. From an iron collar a polished iron bracket arm, braced from below, reaches out
into the room and holds a lacquered bone cup of bone-oil. The flame burns on crossed planes, full-bright through
NeoForge element light data, in one of two kinds (blockstate `flame`): `pale`, a gold-white bone-oil tongue on a
crimson root, the mausoleums' standard light; `crimson`, Hemorath's deep red tongue with a darker root and a
hot crimson-orange core; and `smoulder`, Putriciel's: no tongue, only a low bed of Flammeus embers in the cup
(crimson coals with pink-white hot spots under a cracked black char crust).

The sconce fills the air cell in front of a wall: authored for `facing=north`, its back plate lies against
the cell's south face (z 15..16) and it reaches north into the room. Facing is the direction the sconce
looks away from its wall. 16 px per block (density 1).

Emits `reliquary_sconce` and `reliquary_sconce_crimson` on the `reliquary_sconce` atlas, `reliquary_sconce_smoulder`
on its own `reliquary_sconce_smoulder` atlas (so adding it left the other two byte-identical), the facing x flame
blockstate and editable Blockbench projects.
Python 3 + Pillow + NumPy. `--preview` writes build/model-previews/reliquary_sconce.png and reliquary_sconce_crimson.png.

FLAME is the flame's centre in block pixels for `facing=north`; the block emits its particles there.
EMBER_BED is the smoulder's ember-bed height in block pixels; a smouldering sconce emits there instead.
"""
import argparse
import json
import math

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, PALETTE, box, build, model_json, preview, separate, spin, write

ATLAS = 'reliquary_sconce'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}

CUP = (8, 8)                    # cup centre (x, z)
OIL = 9.1                       # the oil's surface, where the flame stands
FLAME_TOP = 12.0
FLAME = (8, round((OIL + FLAME_TOP) / 2, 2), 8)
EMBER_BED = 9.6


def octagon(name, x0, x1, z0, z1, y0, y1, chamfer, mat, **kw):
    """An octagonal prism as three non-overlapping slabs."""
    return [box(f'{name}_core', (x0, y0, z0 + chamfer), (x1, y1, z1 - chamfer), mat, **kw),
            box(f'{name}_front', (x0 + chamfer, y0, z0), (x1 - chamfer, y1, z0 + chamfer), mat, **kw),
            box(f'{name}_back', (x0 + chamfer, y0, z1 - chamfer), (x1 - chamfer, y1, z1), mat, **kw)]


def plate():
    e = [box('plate', (5, 3, 15), (11, 12, 16), 'iron'),
         # The crown: a square turned 45 degrees, set a hair behind the plate's face, and a bone finial on its point.
         box('crown', (6, 10, 15.25), (10, 14, 16), 'iron', rotation=spin('z', 45, (8, 12, 15.5))),
         box('finial', (7.375, 14.5, 15.25), (8.625, 15.75, 16), 'bone'),
         box('inset', (6, 4, 14.75), (10, 10.5, 15), 'panel', skip=('south',)),
         # The bone boss on the crown, its crimson eye watching over the flame.
         box('boss', (6.75, 11, 14.25), (9.25, 13.5, 15.25), 'bone', north='boss', skip=('south',)),
         box('collar', (7, 4.5, 14.25), (9, 6.5, 14.75), 'ferric', skip=('south',))]
    for x in (5.6, 10.4):
        for y in (3.6, 11.4):
            e.append(box('rivet', (x - .4, y - .4, 14.5), (x + .4, y + .4, 15), 'ferric', skip=('south',)))
    return e


def bracket():
    cx, cz = CUP
    return [box('arm', (7.4, 5, cz - .5), (8.6, 6, 14.25), 'ferric', skip=('south',)),
            # A gusset from low on the plate up under the arm; +45 about x drops its wall end.
            box('brace', (7.5, 3.5, 12.1), (8.5, 4.5, 15.1), 'iron', rotation=spin('x', 45, (8, 4, 13.6))),
            box('drip_knob', (7.5, 4, cz - 1), (8.5, 5, cz), 'ferric'),
            box('drip_bead', (7.75, 3.25, cz - .75), (8.25, 4, cz - .25), 'bone', skip=('up',))]


def cup():
    cx, cz = CUP
    e = octagon('cup_heel', cx - 1.25, cx + 1.25, cz - 1.25, cz + 1.25, 6, 7, .5, 'bone')
    e += octagon('cup', cx - 2, cx + 2, cz - 2, cz + 2, 7, 9, 1, 'lacquer', up='oil')
    for lo, hi in [((cx - 1.75, 9, cz - 2), (cx + 1.75, 9.75, cz - 1.5)),
                   ((cx - 1.75, 9, cz + 1.5), (cx + 1.75, 9.75, cz + 2)),
                   ((cx - 2, 9, cz - 1.75), (cx - 1.5, 9.75, cz + 1.75)),
                   ((cx + 1.5, 9, cz - 1.75), (cx + 2, 9.75, cz + 1.75))]:
        e.append(box('cup_rim', lo, hi, 'bone', skip=('down',)))
    return e


def flame(kind='pale'):
    cx, cz = CUP
    suffix = '' if kind == 'pale' else '_' + kind
    planes = [box('oil_glow', (cx - 1.5, OIL, cz - 1.5), (cx + 1.5, OIL + .05, cz + 1.5), 'oil_glow' + suffix,
                  skip=('down', 'north', 'south', 'east', 'west'), extra=BRIGHT)]
    for angle in (45, -45):
        planes.append(box('flame', (cx - 1.5, OIL, cz), (cx + 1.5, FLAME_TOP, cz), 'flame' + suffix,
                          skip=('east', 'west', 'up', 'down'), rotation=spin('y', angle, (cx, OIL, cz)),
                          extra=BRIGHT))
    return planes


def smoulder():
    """Putriciel's smouldering cup: a char-crusted ember bed over the oil and a few coals heaped on it."""
    cx, cz = CUP
    e = [box('ember_bed', (cx - 1.5, OIL, cz - 1.5), (cx + 1.5, OIL + .1, cz + 1.5), 'ember_bed',
             skip=('down', 'north', 'south', 'east', 'west'), extra=BRIGHT)]
    for x, z, s, h in [(-.7, -.5, .55, .6), (.6, -.6, .45, .5), (.1, .7, .6, .75), (-.9, .8, .4, .4),
                       (1.0, .5, .35, .35)]:
        e.append(box('coal', (cx + x - s, OIL, cz + z - s), (cx + x + s, OIL + h, cz + z + s), 'coal',
                     skip=('down',), extra=BRIGHT))
    return e


# --- painters -----------------------------------------------------------------------------------

def boss(face, lx, ly, w, h, key):
    """A bone boss with a crimson lacquer eye."""
    n = noise('boss', lx, ly)
    if lx == w // 2 and ly == h // 2:
        return (150, 18, 30, 255)
    return clamp(PALETTE['bone'], int(n * 12) - 6 - (20 if lx in (0, w - 1) or ly in (0, h - 1) else 0)) + (255,)


def oil(face, lx, ly, w, h, key):
    n = noise('oil', key, lx, ly)
    return clamp((132, 110, 70), int(n * 14) - 7) + (255,)


def oil_glow(face, lx, ly, w, h, key):
    n = noise('oil_glow', lx, ly)
    return ((250, 226, 160) if n > .5 else (226, 188, 118)) + (255,)


def flame_tex(face, lx, ly, w, h, key):
    """Bone-oil flame: a pale gold-white tongue rising from a crimson root."""
    u = abs((lx + .5) / w * 2 - 1)
    v = (ly + .5) / h                          # 0 at the tip, 1 at the root
    width = (v / .65) ** .7 if v < .65 else 1 - (v - .65) * .6
    if u > width * 1.02:
        return (0, 0, 0, 0)
    if v > .8:
        return (178, 24, 40, 255)
    if u < width * .5 and v > .3:
        return (255, 248, 226, 255)
    return (242, 214, 148, 255)


def oil_glow_crimson(face, lx, ly, w, h, key):
    n = noise('oil_glow', lx, ly)
    return ((236, 92, 60) if n > .5 else (176, 30, 34)) + (255,)


def flame_crimson(face, lx, ly, w, h, key):
    """Hemorath's flame: a deep red tongue, a darker root, a hot crimson-orange core."""
    u = abs((lx + .5) / w * 2 - 1)
    v = (ly + .5) / h                          # 0 at the tip, 1 at the root
    width = (v / .65) ** .7 if v < .65 else 1 - (v - .65) * .6
    if u > width * 1.02:
        return (0, 0, 0, 0)
    if v > .8:
        return (96, 6, 18, 255)
    if u < width * .5 and .3 < v <= .8:
        return (255, 112, 64, 255) if v > .45 else (238, 66, 50, 255)
    if v < .2:
        return (214, 34, 40, 255)
    return (180, 18, 32, 255)


# The Flammeus fire's palette (manipulation_flame.fsh): black soot, deep and bright crimson, pink-white heat.
SOOT = (18, 8, 10)
CRIMSON_DEEP = (112, 6, 18)
CRIMSON = (184, 14, 30)
RED = (230, 28, 40)
HEAT = (255, 178, 192)


def ember_bed(face, lx, ly, w, h, key):
    """The smoulder: crimson embers glowing up through a cracked black char crust."""
    n = noise('ember_bed', lx, ly)
    if noise('crust', lx, ly) < .55 and n < .8:
        return clamp(SOOT, int(n * 10)) + (255,)
    if n > .86:
        return HEAT + (255,)
    return (RED if n > .55 else CRIMSON if n > .25 else CRIMSON_DEEP) + (255,)


def coal(face, lx, ly, w, h, key):
    """A smouldering coal: a red-hot crown, char-black flanks with a crimson crack."""
    n = noise('coal', key, face, lx, ly)
    if face == 'up':
        if n < .3:
            return clamp(SOOT, int(n * 12)) + (255,)              # char crusting over the crown
        return (HEAT if n > .85 else RED if n > .5 else CRIMSON) + (255,)
    return (CRIMSON_DEEP if n > .78 else clamp(SOOT, int(n * 12))) + (255,)


SPECIAL = dict(boss=boss, oil=oil, oil_glow=oil_glow, flame=flame_tex, oil_glow_crimson=oil_glow_crimson,
               flame_crimson=flame_crimson, ember_bed=ember_bed, coal=coal)
FLAMES = ('pale', 'crimson')
STATES = FLAMES + ('smoulder',)
SMOULDER_ATLAS = 'reliquary_sconce_smoulder'


def model_name(kind):
    return 'reliquary_sconce' if kind == 'pale' else f'reliquary_sconce_{kind}'


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for kind in STATES:
            variant = {'model': f'hemomancy:block/{model_name(kind)}'}
            if y:
                variant['y'] = y
            variants[f'facing={facing},flame={kind}'] = variant
    return {'variants': variants}


def bounds(elements):
    """Pixel AABB of the elements, rotations included, rounded outward for Block.box."""
    lo, hi = [99.0] * 3, [-99.0] * 3
    for el in elements:
        corners = [[x, y, z] for x in (el['from'][0], el['to'][0]) for y in (el['from'][1], el['to'][1])
                   for z in (el['from'][2], el['to'][2])]
        rot = el.get('rotation')
        for p in corners:
            if rot:
                a, b = {'x': (1, 2), 'y': (2, 0), 'z': (0, 1)}[rot['axis']]
                ang = math.radians(rot['angle'])
                o = rot['origin']
                va, vb = p[a] - o[a], p[b] - o[b]
                p[a] = o[a] + va * math.cos(ang) - vb * math.sin(ang)
                p[b] = o[b] + va * math.sin(ang) + vb * math.cos(ang)
            for k in range(3):
                lo[k], hi[k] = min(lo[k], p[k]), max(hi[k], p[k])
    return [max(0, math.floor(v)) for v in lo], [min(16, math.ceil(v)) for v in hi]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body = plate() + bracket() + cup()
    moved = separate(body)
    groups = {'body': body}
    for kind in FLAMES:
        groups['lit_' + kind] = flame(kind)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'
    outputs = {f'block/{model_name(kind)}': model_json(models['body'] + models['lit_' + kind], ATLAS, atlas.height,
                                                       particle) for kind in FLAMES}
    write(outputs, 'reliquary_sconce', outputs['block/reliquary_sconce'])
    # The smoulder paints the same body on its own atlas, so the pale and crimson outputs never move.
    embers = smoulder()
    s_atlas, s_models = build({'body': body, 'lit_smoulder': embers}, SPECIAL)
    s_atlas.save(ASSETS / f'textures/block/{SMOULDER_ATLAS}.png')
    smouldering = model_json(s_models['body'] + s_models['lit_smoulder'], SMOULDER_ATLAS, s_atlas.height,
                             f'hemomancy:block/{SMOULDER_ATLAS}')
    write({'block/reliquary_sconce_smoulder': smouldering}, 'reliquary_sconce_smoulder', smouldering)
    (ASSETS / 'blockstates/reliquary_sconce.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    model = outputs['block/reliquary_sconce']
    lo, hi = bounds(model['elements'])
    print(f'  reliquary_sconce: {len(model["elements"])} cubes, bounds Block.box({lo[0]}, {lo[1]}, {lo[2]}, '
          f'{hi[0]}, {hi[1]}, {hi[2]}) for facing=north')
    print(f'Exported Reliquary Sconce: {len(body)} cubes (+{len(groups["lit_pale"])} flame, pale and crimson), '
          f'{moved} faces separated, 128x{atlas.height} atlas, FLAME = {FLAME}')
    print(f'  smoulder: +{len(embers)} ember cubes on its own 128x{s_atlas.height} atlas, EMBER_BED = {EMBER_BED}')
    if args.preview:
        for kind in FLAMES:
            preview(outputs[f'block/{model_name(kind)}']['elements'], ATLAS,
                    f'Reliquary Sconce ({kind} flame) - facing north, wall at z 16 (flame is full-bright)',
                    model_name(kind))
        preview(smouldering['elements'], SMOULDER_ATLAS,
                'Reliquary Sconce (smoulder) - facing north, Flammeus embers under char (full-bright)',
                'reliquary_sconce_smoulder')


if __name__ == '__main__':
    main()

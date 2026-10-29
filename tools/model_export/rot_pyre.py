"""Author the Rot Pyre: Putriciel's bone pyre, the fire of the Saint who rotted and kept.

Long bones lean together in a cone over a ring of mud-caked venous stones, two crossed on the bed behind.
A skull sits in the pyre's open mouth, facing out over the low front stone. Moss creeps over the stone
tops, rot stains the bones from the ground up, and two pale fungus caps have taken root on the ring.
The unlit model lays cold char on the bed: dead coals, grey ash, no glow. The lit model swaps it for a
live ember bed in the Flammeus palette (crimson coals with pink-white hot spots under a black char
crust), full-bright through NeoForge element light data. The fire itself is not baked: the block
entity renderer draws Flammeus shader tongues on the bed (client/render/world/PyreFlames).

Emits `rot_pyre` and `rot_pyre_lit`, the painted atlas, the lit blockstate (four random turns; the fire
stays on the block's centre line) and an editable Blockbench project. Python 3 + Pillow + NumPy.
`--preview` writes build/model-previews/rot_pyre.png and rot_pyre_lit.png.

FLAME_TIP is the drawn flame's top in block pixels; the renderer sizes the fire to it and the block emits
its particles there.
"""
import argparse
import json
import math

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, box, build, model_json, preview, separate, spin, write

ATLAS = 'rot_pyre'
FLAME_TIP = 13
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}

MUD_STONE = (64, 26, 28)
MOSS = (72, 86, 40)
MUD = (46, 34, 26)
ROT = (112, 104, 62)
BED = 0.75


def bone(name, x, y, z, length, axis=None, angle=0, thick=1.5, knob=2.25):
    """A long bone standing up from (x, y, z), then leant `angle` about `axis` from that foot."""
    t, k = thick / 2, knob / 2
    rot = spin(axis, angle, (x, y, z)) if axis else None
    return [box(f'{name}_shaft', (x - t, y + 1.25, z - t), (x + t, y + length - 1.25, z + t), 'bone', rotation=rot,
                skip=('up', 'down')),
            box(f'{name}_foot', (x - k, y, z - k * .8), (x + k, y + 1.25, z + k * .8), 'bone', rotation=rot),
            box(f'{name}_head', (x - k, y + length - 1.25, z - k * .8), (x + k, y + length, z + k * .8), 'bone',
                rotation=rot)]


def lying_bone(name, lo_x, hi_x, y, z, thick=1.25, knob=2.0):
    """A bone lying east-west on the bed."""
    t, k = thick / 2, knob / 2
    return [box(f'{name}_shaft', (lo_x + 1, y, z - t), (hi_x - 1, y + thick, z + t), 'bone', skip=('east', 'west')),
            box(f'{name}_end', (lo_x, y, z - k), (lo_x + 1, y + knob, z + k), 'bone'),
            box(f'{name}_end', (hi_x - 1, y, z - k), (hi_x, y + knob, z + k), 'bone')]


def stones():
    e = []
    # (centre x, centre z, half x, half z, height, turn)
    for cx, cz, hx, hz, h, turn in [
            (8, 2.25, 1.75, 1.0, 1.25, 0),        # the low front stone under the skull
            (4.0, 3.6, 1.4, 1.2, 2.25, 22.5), (12.1, 3.7, 1.3, 1.1, 1.75, -22.5),
            (2.4, 8.2, 1.0, 1.6, 2.0, 0), (13.6, 7.6, 1.0, 1.5, 1.5, 0),
            (3.9, 12.3, 1.3, 1.2, 1.5, -22.5), (12.2, 12.2, 1.4, 1.3, 2.25, 45),
            (8.2, 13.7, 1.6, 1.0, 2.0, 0)]:
        rot = spin('y', turn, (cx, 0, cz)) if turn else None
        e.append(box('stone', (cx - hx, 0, cz - hz), (cx + hx, h, cz + hz), 'stone', up='moss', skip=('down',),
                     rotation=rot))
    # Pale fungus caps rooted on two of the stones.
    for x, z, y, s in [(12.1, 11.9, 2.25, 1.0), (2.4, 8.9, 2.0, .75)]:
        e.append(box('fungus_stem', (x - .25, y, z - .25), (x + .25, y + 1, z + .25), 'fungus', skip=('down',)))
        e.append(box('fungus_cap', (x - s, y + 1, z - s), (x + s, y + 1.5, z + s), 'fungus_cap'))
    return e


def bed():
    """Rotted mud and ash over the hearth, an octagon of three slabs."""
    return [box('bed_core', (3, 0, 4.5), (13, BED, 11.5), 'mud', skip=('down',)),
            box('bed_front', (4.5, 0, 3), (11.5, BED, 4.5), 'mud', skip=('down',)),
            box('bed_back', (4.5, 0, 11.5), (11.5, BED, 13), 'mud', skip=('down',))]


def pyre():
    e = []
    # The cone: two long bones leant in from each side, the front pair spread to leave the mouth open.
    for x, z, axis, angle, length in [
            (4.2, 3.4, 'x', 22.5, 10.0), (11.8, 3.4, 'x', 22.5, 9.5),
            (6.4, 12.8, 'x', -22.5, 10.5), (9.8, 12.6, 'x', -22.5, 9.75),
            (3.2, 6.4, 'z', -22.5, 9.75), (3.4, 10.0, 'z', -22.5, 10.25),
            (12.8, 6.0, 'z', 22.5, 10.25), (12.6, 9.8, 'z', 22.5, 9.5)]:
        e += bone('cone', x, BED, z, length, axis, angle)
    # Two bones crossed on the bed behind the flame, and a short one fallen across the front.
    e += lying_bone('cross', 4.5, 11.5, BED, 10.5)
    e += lying_bone('front', 9.5, 13.5, BED, 4.75, thick=1.0, knob=1.5)
    # The skull in the mouth, facing out (north), its jaw slack.
    e.append(box('skull', (5.5, 1.5, 2.75), (10.5, 4.75, 6.25), 'skull_side', north='skull', up='skull_top'))
    e.append(box('skull_jaw', (6.5, BED, 3.25), (9.5, 1.5, 5.75), 'skull_side', north='jaw', skip=('up',)))
    return e


# (x, z, half size, height) of the coals heaped on the bed, cold or burning.
COALS = [(5.2, 7.6, .6, .7), (10.8, 7.2, .5, .6), (8.2, 9.6, .7, .8), (10.4, 9.8, .45, .5),
         (4.9, 5.4, .45, .5), (11.2, 5.2, .5, .55), (6.4, 10.6, .5, .5)]


def cold():
    """Char left on a dead pyre: a layer of ash and dead coals, nothing lit."""
    e = [box('char', (4.5, BED, 4.5), (11.5, BED + .1, 11.5), 'char',
             skip=('down', 'north', 'south', 'east', 'west'))]
    for x, z, s, h in COALS:
        e.append(box('coal', (x - s, BED, z - s), (x + s, BED + h * .8, z + s), 'coal', skip=('down',)))
    return e


def lit():
    """A live ember bed: crimson coals glowing through black char, full-bright."""
    e = [box('embers', (4.5, BED, 4.5), (11.5, BED + .1, 11.5), 'embers',
             skip=('down', 'north', 'south', 'east', 'west'), extra=BRIGHT)]
    for x, z, s, h in COALS:
        e.append(box('ember', (x - s, BED, z - s), (x + s, BED + h, z + s), 'ember_lump', skip=('down',),
                     extra=BRIGHT))
    return e


# --- painters -----------------------------------------------------------------------------------

def stone(face, lx, ly, w, h, key):
    n = noise('stone', key, face, lx, ly)
    rgb = clamp(MUD_STONE, int(n * 16) - 8 + (10 if ly == 0 else 0) - (12 if ly == h - 1 else 0))
    if ly == 0 and noise('moss_lip', key, face, lx) < .5:
        rgb = clamp(MOSS, int(n * 10) - 14)   # moss spilling over the top edge
    elif ly >= h - 1 and n < .5:
        rgb = clamp(MUD, int(n * 8))          # mud caked at the foot
    return rgb + (255,)


def moss(face, lx, ly, w, h, key):
    n = noise('moss', key, lx, ly)
    if n < .22:
        return clamp(MUD_STONE, int(n * 20)) + (255,)
    return clamp(MOSS, int(n * 22) - 11 + (12 if noise('tuft', key, lx, ly) > .8 else 0)) + (255,)


def mud(face, lx, ly, w, h, key):
    n = noise('mud', key, face, lx, ly)
    if n > .93:
        return (126, 120, 104, 255)            # ash
    if n < .07:
        return clamp(ROT, -10) + (255,)        # olive rot (not fire)
    return clamp(MUD, int(n * 12) - 6) + (255,)


def rot_bone(face, lx, ly, w, h, key):
    """Old bone, yellowed; rot rises a little way from the ground end, with the odd blotch higher up."""
    n = noise('bone', key, face, lx, ly)
    rgb = clamp((204, 190, 156), int(n * 14) - 7 - (12 if lx in (0, w - 1) and w > 1 else 0))
    low = face not in ('up', 'down') and ly >= h - 2 and h > 3
    if (low and n < .7) or noise('rot', key, face, lx, ly // 2) < .06:
        rgb = clamp(ROT, int(n * 12) - 6)
    elif noise('moss', key, face, lx, ly) < .025:
        rgb = MOSS
    elif n > .95:
        rgb = (158, 134, 104)
    return rgb + (255,)


SKULL = ['.ooo.',
         'oEoEo',
         'ooNoo',
         'ooooo']


def skull(face, lx, ly, w, h, key):
    """The cranium's face: two deep sockets over a nasal notch, the crown corners worn away."""
    ch = SKULL[min(ly, len(SKULL) - 1)][min(lx, len(SKULL[0]) - 1)]
    n = noise('skull', lx, ly)
    if ch == '.':
        return clamp(ROT, -24) + (255,)
    if ch == 'E':
        return (26, 20, 16, 255)
    if ch == 'N':
        return (60, 44, 34, 255)
    return clamp((206, 192, 158), int(n * 12) - 6 - (14 if ly == h - 1 else 0)) + (255,)


def skull_side(face, lx, ly, w, h, key):
    n = noise('skull_side', key, face, lx, ly)
    if noise('rot', key, face, lx, ly // 2) < .18:
        return clamp(ROT, int(n * 10)) + (255,)
    return clamp((198, 184, 150), int(n * 12) - 8) + (255,)


def skull_top(face, lx, ly, w, h, key):
    n = noise('skull_top', lx, ly)
    if noise('moss_cap', lx, ly) < .15:
        return clamp(MOSS, int(n * 14)) + (255,)
    return clamp((206, 192, 158), int(n * 12) - 6) + (255,)


def jaw(face, lx, ly, w, h, key):
    """A row of teeth over the slack jaw."""
    if ly == 0:
        return ((222, 210, 172) if lx % 2 == 0 else (40, 30, 24)) + (255,)
    return clamp((186, 170, 136), int(noise('jaw', lx, ly) * 10)) + (255,)


def fungus(face, lx, ly, w, h, key):
    return clamp((206, 196, 172), int(noise('stem', lx, ly) * 10)) + (255,)


def fungus_cap(face, lx, ly, w, h, key):
    n = noise('cap', key, face, lx, ly)
    if face == 'up':
        return ((150, 54, 60) if n > .75 else clamp((214, 200, 176), int(n * 10))) + (255,)
    return clamp((176, 156, 132), int(n * 10)) + (255,)


# The Flammeus fire's palette (manipulation_flame.fsh): black soot, deep and bright crimson, pink-white heat.
SOOT = (18, 8, 10)
CRIMSON_DEEP = (112, 6, 18)
CRIMSON = (184, 14, 30)
RED = (230, 28, 40)
HEAT = (255, 178, 192)


def embers(face, lx, ly, w, h, key):
    """The burning bed: crimson coals under a cracked black char crust, pink-white where they burn hottest."""
    n = noise('embers', lx, ly)
    crust = noise('crust', lx // 2, ly // 2)
    if crust < .32 and n < .7:
        return clamp(SOOT, int(n * 10)) + (255,)
    if n > .88:
        return HEAT + (255,)
    if n > .66:
        return RED + (255,)
    if n < .22:
        return CRIMSON_DEEP + (255,)
    return clamp(CRIMSON, int(n * 16) - 8) + (255,)


def ember_lump(face, lx, ly, w, h, key):
    """A burning coal: glowing crimson on top with a pink heart, char-black flanks cracked with red."""
    n = noise('lump', key, face, lx, ly)
    if face == 'up':
        if n > .78:
            return HEAT + (255,)
        return (RED if n > .4 else CRIMSON) + (255,)
    return (CRIMSON_DEEP if n > .62 else clamp(SOOT, int(n * 12))) + (255,)


def char(face, lx, ly, w, h, key):
    """Cold char: black coal dust and grey ash, no ember left."""
    n = noise('char', lx, ly)
    if n > .86:
        return clamp((104, 98, 92), int(n * 12)) + (255,)     # ash
    if n > .6:
        return clamp((48, 42, 40), int(n * 10)) + (255,)
    return clamp((24, 20, 20), int(n * 10)) + (255,)


def coal(face, lx, ly, w, h, key):
    n = noise('coal', key, face, lx, ly)
    if face == 'up' and n > .8:
        return (92, 88, 84, 255)                                # an ash skin on the dead coal
    return clamp((30, 26, 26), int(n * 14) - 4) + (255,)


SPECIAL = dict(stone=stone, moss=moss, mud=mud, bone=rot_bone, skull=skull, skull_side=skull_side,
               skull_top=skull_top, jaw=jaw, fungus=fungus, fungus_cap=fungus_cap, embers=embers,
               ember_lump=ember_lump, char=char, coal=coal)


def blockstate():
    variants = {}
    for lit_state in (False, True):
        model = 'hemomancy:block/rot_pyre' + ('_lit' if lit_state else '')
        variants[f'lit={str(lit_state).lower()}'] = [dict({'model': model}, **({'y': y} if y else {}))
                                                     for y in (0, 90, 180, 270)]
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
    body = stones() + bed() + pyre()
    moved = separate(body)
    dead, flames = cold(), lit()
    atlas, models = build({'body': body, 'cold': dead, 'lit': flames}, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'
    unlit = model_json(models['body'] + models['cold'], ATLAS, atlas.height, particle)
    lit_model = model_json(models['body'] + models['lit'], ATLAS, atlas.height, particle)
    write({'block/rot_pyre': unlit, 'block/rot_pyre_lit': lit_model}, 'rot_pyre', lit_model)
    (ASSETS / 'blockstates/rot_pyre.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    for name, model in (('rot_pyre', unlit), ('rot_pyre_lit', lit_model)):
        lo, hi = bounds(model['elements'])
        print(f'  {name}: {len(model["elements"])} cubes, bounds Block.box({lo[0]}, {lo[1]}, {lo[2]}, '
              f'{hi[0]}, {hi[1]}, {hi[2]})')
    print(f'Exported Rot Pyre: {len(body)} cubes (+{len(dead)} cold / {len(flames)} lit), {moved} faces separated, '
          f'128x{atlas.height} atlas, FLAME_TIP = {FLAME_TIP}')
    if args.preview:
        preview(unlit['elements'], ATLAS, 'Rot Pyre - unlit (cold char), actual atlas', 'rot_pyre')
        preview(lit_model['elements'], ATLAS, 'Rot Pyre - lit (ember bed full-bright; the fire is drawn by the BER)', 'rot_pyre_lit')


if __name__ == '__main__':
    main()

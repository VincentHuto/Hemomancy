"""The Myelin Borer: a lacquered boring head on a body of sheath segments, eight hooked legs and a short stinger.

Emits MyelinBorerMesh.java (animation lives in the hand-written MyelinBorerModel), the atlas and its emissive layer.

    python tools/model_export/myelin_borer.py            # mesh + textures
    python tools/model_export/myelin_borer.py --preview  # also build/myelin_borer_preview.png
    node tools/model_export/java_model_to_bbmodel.mjs src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/MyelinBorerMesh.java --texture textures/entity/myelin_borer/model_myelin_borer.png --out src/main/resources/assets/hemomancy/models/entity/bbmodel

Model space: head at -z, claws on y = 0 under the root. Whole-number cubes at 16 px per block. Each segment is an
ivory sheath internode like the Myelin Sheath block; the gaps between them are raw crimson nodes that glow faintly,
and the head is dark lacquered chitin with violet sensing palps, the Cortical Drift's nerve colour.
"""
import argparse
import math

from PIL import Image

import hemomancy_books as hb
import unstained_weapons as uw
from vampire_bat import java_source, mirror_left, posed

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/MyelinBorerMesh.java'
TEXTURES = uw.ASSETS / 'textures/entity/myelin_borer'

BODY_HEIGHT = 3
RING_WIDTHS = (5, 5, 4, 3, 2)
RING_DEPTH = 2
LEGGED_RINGS = 3
LEG_YAW = 10


def add_leg(r, index, parent, half_width, z):
    name = f'left_leg{index}'
    yaw = LEG_YAW * (1 - index)
    r.part(name, parent, (half_width, 0, z), (0, yaw, 15))
    r.cube(name, (0, 0, 0, 2, 1, 1), 'leg')
    claw = f'left_claw{index}'
    r.part(claw, name, (2, 0, 0), (0, 0, -15))
    r.cube(claw, (0, 0, 0, 1, 3, 1), 'claw')


def borer():
    r = uw.Rig('myelin_borer', 'MyelinBorerMesh', 'myelin_borer')
    r.parts[0]['pivot'] = (0, 24, 0)
    r.part('body', 'root', (0, -BODY_HEIGHT, -2))
    r.cube('body', (-2.5, -2, -1, 5, 3, 3), 'sheath')
    r.cube('body', (-1.5, -3, -1, 3, 1, 2), 'ridge')
    add_leg(r, 0, 'body', 2.5, 0)

    r.part('head', 'body', (0, 0, -1))
    r.cube('head', (-2, -2, -3, 4, 3, 3), 'head')
    r.cube('head', (-1, -1, -4, 2, 2, 1), 'bore')
    r.part('left_palp', 'head', (1, 0, -3), (0, -15, 0))
    r.cube('left_palp', (0, 0, -2, 1, 1, 2), 'palp')
    r.part('left_palp_tip', 'left_palp', (0, 0, -2), (10, 0, 0))
    r.cube('left_palp_tip', (0, 0, -2, 1, 1, 2), 'palp_tip')

    parent = 'body'
    for i, width in enumerate(RING_WIDTHS):
        name = f'ring{i}'
        r.part(name, parent, (0, 0, 2 if i == 0 else RING_DEPTH))
        r.cube(name, (-width / 2, -2, 0, width, BODY_HEIGHT, RING_DEPTH), 'sheath')
        if i + 1 < LEGGED_RINGS + 1:
            add_leg(r, i + 1, name, width / 2, 0.5)
        if 0 < i < len(RING_WIDTHS) - 1:
            bristle = f'left_bristle{i}'
            r.part(bristle, name, (width / 2 - 1, -2, 0.5), (0, 0, 20))
            r.cube(bristle, (0, -2, 0, 1, 2, 1), 'bristle')
        parent = name
    r.part('tail', parent, (0, -0.5, RING_DEPTH))
    r.cube('tail', (-1, -1, 0, 2, 2, 2), 'tail')
    r.cube('tail', (-0.5, -0.5, 2, 1, 1, 2), 'sting')
    mirror_left(r)
    return r


# ---------------------------------------------------------------- painting

IVORY = (220, 208, 184)
IVORY_LIGHT = (240, 232, 214)
IVORY_DARK = (194, 180, 152)
IVORY_DEEP = (156, 142, 116)
NODE = (150, 30, 42)
NODE_GLOW = (200, 44, 52)
LACQUER = (62, 18, 28)
LACQUER_LIGHT = (98, 30, 40)
LACQUER_DEEP = (36, 10, 18)
BORE = (122, 110, 140)
VIOLET = (90, 74, 126)
VIOLET_LIGHT = (180, 164, 218)
VIOLET_GLOW = (150, 120, 220)
BONE = (156, 142, 116)
CLAW = (40, 22, 30)


def flat(rgb):
    return rgb + (255,), None


def lit(rgb, glow):
    return rgb + (255,), glow + (255,)


def sheath(t):
    # Model y points down: 'down' is the back, 'up' the belly. Front and back faces are the raw nodes between internodes.
    if t['face'] in ('north', 'south'):
        return lit(NODE, NODE_GLOW) if t['v'] == 1 and t['fw'] > 2 else flat(NODE)
    if t['face'] == 'up':
        return flat(IVORY_DEEP)
    if t['face'] == 'down':
        if t['u'] in (0, t['fw'] - 1):
            return flat(IVORY_DARK)
        return flat(IVORY_LIGHT if t['v'] == 0 else IVORY)
    if t['v'] == t['fh'] - 1:
        return flat(IVORY_DEEP)
    return flat(hb.noisy(IVORY if t['v'] else IVORY_LIGHT, t, 6))


def ridge(t):
    return flat(IVORY_LIGHT if t['face'] == 'down' else IVORY_DARK)


def head(t):
    if t['face'] == 'down':
        return flat(LACQUER_LIGHT if t['v'] == t['fh'] - 1 else LACQUER)
    if t['face'] == 'up':
        return flat(LACQUER_DEEP)
    if t['face'] in ('east', 'west') and t['v'] == 0 and t['u'] == 0:
        # A pit eye on each cheek, just behind the bore.
        return lit(VIOLET_LIGHT, VIOLET_GLOW)
    return flat(LACQUER if t['v'] < t['fh'] - 1 else LACQUER_DEEP)


def bore(t):
    if t['face'] == 'north':
        # A ringed drilling mouth.
        return flat(LACQUER_DEEP if (t['u'], t['v']) == (0, 0) or (t['u'], t['v']) == (1, 1) else BORE)
    return flat(BORE)


def palp(t):
    return flat(VIOLET)


def palp_tip(t):
    if t['face'] == 'north' or t['u'] == 0:
        return lit(VIOLET_LIGHT, VIOLET_GLOW)
    return flat(VIOLET)


def leg(t):
    return flat(LACQUER_LIGHT if t['face'] == 'down' else LACQUER)


def claw(t):
    return flat(CLAW if t['v'] == t['fh'] - 1 else BONE)


def bristle(t):
    return flat(IVORY_DEEP if t['v'] == t['fh'] - 1 else BONE)


def tail(t):
    if t['face'] in ('north', 'south'):
        return flat(NODE)
    return flat(IVORY_DARK)


def sting(t):
    if t['face'] == 'south':
        return lit(NODE, NODE_GLOW)
    return flat(LACQUER)


PAINTERS = dict(sheath=sheath, ridge=ridge, head=head, bore=bore, palp=palp, palp_tip=palp_tip, leg=leg, claw=claw,
                bristle=bristle, tail=tail, sting=sting)


def paint(material, t):
    return PAINTERS[material](t)


# ---------------------------------------------------------------- preview

def preview(rig, image):
    wave = {f'ring{i}': (0, 12 * math.sin(i * 0.75), 0) for i in range(len(RING_WIDTHS))}
    poses = [({}, 0.65, 0.35), ({}, math.pi - 0.65, 0.35), ({}, math.pi / 2, 0.02), ({}, 0, 0.02), ({}, 0.4, 1.3),
             (wave, 0.5, 0.6)]
    tiles = [uw.render_quads(posed(rig, **pose), image, yaw, pitch, size=420) for pose, yaw, pitch in poses]
    sheet = Image.new('RGB', (420 * 3, 420 * 2))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % 3) * 420, (i // 3) * 420))
    out = ROOT / 'build/myelin_borer_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = borer()
    size = hb.layout(rig)
    image, glow = uw.paint_atlas(rig, size, paint)
    TEXTURES.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURES / 'model_myelin_borer.png')
    glow.save(TEXTURES / 'model_myelin_borer_glow.png')
    source = java_source(rig, size).replace('model.entity.mob.animal;', 'model.entity.mob.arthropod;', 1)
    JAVA.write_text(source, encoding='utf-8')
    print(f'myelin_borer: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        preview(rig, image)


if __name__ == '__main__':
    main()

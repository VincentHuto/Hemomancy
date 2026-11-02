"""The Fungling: a stepped mushroom cap on a pale stalk with one eye and four short tentacle feet.

Emits FunglingMesh.java (animation lives in the hand-written FunglingModel), the atlas and its emissive layer.

    python tools/model_export/fungling.py            # mesh + textures
    python tools/model_export/fungling.py --preview  # also build/fungling_preview.png
    node tools/model_export/java_model_to_bbmodel.mjs src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/FunglingMesh.java --texture textures/entity/fungling/model_fungling.png --out src/main/resources/assets/hemomancy/models/entity/bbmodel

Model space: eye at -z, feet on y = 0 under the root. Whole-number cubes and flat colour at 16 px per block. Each foot
runs along its own +x, so one yaw at the hip swings it and one roll at the hip lifts it.
"""
from pathlib import Path
import argparse
import math

from PIL import Image

import hemomancy_books as hb
import unstained_weapons as uw
from vampire_bat import java_source, mirror_left, posed

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/FunglingMesh.java'
TEXTURES = uw.ASSETS / 'textures/entity/fungling'

HIP_HEIGHT = 3
STALK = 6
FOOT_YAW = 45
FOOT_DROP, TOE_LIFT = 24, 34


def left_foot(r, name, z, yaw):
    r.part(name, 'body', (1, 0, z), (0, yaw, FOOT_DROP))
    r.cube(name, (0, -1, -1, 4, 2, 2), 'foot')
    toe = name.replace('foot', 'toe')
    r.part(toe, name, (4, 0, 0), (0, 0, -TOE_LIFT))
    r.cube(toe, (0, -0.5, -0.5, 3, 1, 1), 'toe')


def fungling():
    r = uw.Rig('fungling', 'FunglingMesh', 'fungling')
    r.parts[0]['pivot'] = (0, 24, 0)
    r.part('body', 'root', (0, -HIP_HEIGHT, 0))
    r.cube('body', (-2, -STALK, -2, 4, STALK + 1, 4), 'stalk')

    r.part('eye', 'body', (0, -STALK + 3, -2))
    r.cube('eye', (-1.5, -1.5, -1, 3, 3, 1), 'eye')

    r.part('cap', 'body', (0, -STALK, 0))
    r.cube('cap', (-5, -2, -5, 10, 3, 10), 'cap_rim')
    r.cube('cap', (-4, -4, -4, 8, 2, 8), 'cap')
    r.cube('cap', (-2, -5, -2, 4, 1, 4), 'crown')

    left_foot(r, 'left_front_foot', -1, FOOT_YAW)
    left_foot(r, 'left_hind_foot', 1, -FOOT_YAW)
    mirror_left(r)
    return r


# ---------------------------------------------------------------- painting

CRIMSON = (136, 32, 40)
CRIMSON_LIGHT = (168, 50, 48)
MAROON = (92, 22, 32)
GILL = (182, 122, 84)
GILL_DARK = (130, 76, 58)
FLESH = (208, 166, 150)
FLESH_DARK = (172, 120, 110)
RUST = (138, 74, 58)
RUST_DARK = (104, 54, 48)
AMBER = (232, 158, 54)
AMBER_DIM = (150, 84, 26)
PUPIL = (28, 12, 14)
IRIS = (240, 176, 62)

# Lit spots as (face, u, v). Few and wide apart, so the cap reads as a cap and not as noise.
RIM_SPOTS = {('north', 2, 1), ('north', 7, 0), ('east', 3, 0), ('east', 8, 1), ('south', 1, 0), ('south', 6, 1),
             ('west', 2, 1), ('west', 7, 0)}
TOP_SPOTS = {('down', 1, 2), ('down', 5, 1), ('down', 2, 5), ('down', 6, 5), ('north', 5, 0), ('south', 2, 0)}


def flat(rgb):
    return rgb + (255,), None


def lit(rgb, glow=None):
    return rgb + (255,), (glow or rgb) + (255,)


def spot(t, spots):
    return (t['face'], t['u'], t['v']) in spots


def cap_rim(t):
    # Model y points down, so a cube's 'up' face is the underside and 'down' is the top.
    if t['face'] == 'up':
        du, dv = abs(t['u'] + 0.5 - t['fw'] / 2), abs(t['v'] + 0.5 - t['fh'] / 2)
        if max(du, dv) <= 2:
            return flat(RUST_DARK)
        # Gills run out from the stalk, one line in two.
        return flat(GILL_DARK if int(dv if du > dv else du) % 2 else GILL)
    if spot(t, RIM_SPOTS):
        return lit(AMBER, AMBER_DIM)
    if t['face'] == 'down':
        return flat(CRIMSON)
    return flat(MAROON if t['v'] == t['fh'] - 1 else CRIMSON)


def cap(t):
    if spot(t, TOP_SPOTS):
        return lit(AMBER, AMBER_DIM)
    return flat(CRIMSON_LIGHT if t['face'] == 'down' else CRIMSON)


def crown(t):
    return flat(CRIMSON_LIGHT)


def stalk(t):
    band = (t['v'] + 0.5) / t['fh'] if t['side'] else 1
    if band > 0.8:
        return flat(RUST)
    if band > 0.6:
        return flat(FLESH_DARK)
    return flat(FLESH)


def eye(t):
    if t['face'] != 'north':
        return flat(FLESH_DARK)
    if t['u'] == 1 and t['v'] == 1:
        return lit(PUPIL, (0, 0, 0))
    if t['u'] != 1 and t['v'] != 1:
        return lit(AMBER_DIM)
    return lit(IRIS, AMBER)


def foot(t):
    return flat(RUST if t['face'] == 'up' else FLESH_DARK if t['face'] == 'down' else RUST)


def toe(t):
    return flat(RUST_DARK)


PAINTERS = dict(cap_rim=cap_rim, cap=cap, crown=crown, stalk=stalk, eye=eye, foot=foot, toe=toe)


def paint(material, t):
    return PAINTERS[material](t)


# ---------------------------------------------------------------- preview

def preview(rig, image):
    stride = {'left_front_foot': (0, FOOT_YAW + 25, FOOT_DROP - 22), 'right_hind_foot': (0, -FOOT_YAW + 25, -FOOT_DROP + 22),
              'right_front_foot': (0, -FOOT_YAW - 20, -FOOT_DROP), 'left_hind_foot': (0, -FOOT_YAW - 20, FOOT_DROP)}
    poses = [({}, 0.65, 0.2), ({}, math.pi - 0.65, 0.2), ({}, math.pi / 2, 0.02), ({}, 0, 0.02), ({}, 0.4, -1.2),
             (stride, 0.5, 0.15)]
    tiles = [uw.render_quads(posed(rig, **pose), image, yaw, pitch, size=420) for pose, yaw, pitch in poses]
    sheet = Image.new('RGB', (420 * 3, 420 * 2))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % 3) * 420, (i // 3) * 420))
    out = ROOT / 'build/fungling_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = fungling()
    size = hb.layout(rig)
    image, glow = uw.paint_atlas(rig, size, paint)
    TEXTURES.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURES / 'model_fungling.png')
    glow.save(TEXTURES / 'model_fungling_glow.png')
    JAVA.write_text(java_source(rig, size), encoding='utf-8')
    print(f'fungling: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        preview(rig, image)


if __name__ == '__main__':
    main()

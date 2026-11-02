"""The Crimson Doe: a slender deer with pale hide, visible veins and short crystal antlers.

Emits CrimsonDoeMesh.java (animation lives in the hand-written CrimsonDoeModel), the atlas and its emissive layer.

    python tools/model_export/crimson_doe.py            # mesh + textures
    python tools/model_export/crimson_doe.py --preview  # also build/crimson_doe_preview.png
    node tools/model_export/java_model_to_bbmodel.mjs src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/CrimsonDoeMesh.java --texture textures/entity/crimson_doe/model_crimson_doe.png --out src/main/resources/assets/hemomancy/models/entity/bbmodel

Model space: head at -z, feet on y = 0 under the root. Every cube has whole-number sizes, so each face takes whole texels
at 16 px per block, and every joint's pivot sits on the bone it turns. Hooves stand flat in the rest pose because each
leg's angles cancel out.
"""
from pathlib import Path
import argparse
import math

from PIL import Image

import hemomancy_books as hb
import unstained_weapons as uw
from vampire_bat import java_source, mirror_left, posed

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/CrimsonDoeMesh.java'
TEXTURES = uw.ASSETS / 'textures/entity/crimson_doe'

CLEAR = (0, 0, 0, 0)

BODY_HEIGHT = 13
THIGH, GASKIN, CANNON, HOOF = 4, 4, 5, 1
THIGH_ANGLE, GASKIN_ANGLE = -18, 24
HIP_HEIGHT = (THIGH - 1) * math.cos(math.radians(THIGH_ANGLE)) + GASKIN * math.cos(math.radians(GASKIN_ANGLE)) + CANNON + HOOF
FORELEG, SHANK = 5, 5
SHOULDER_HEIGHT = FORELEG + SHANK + HOOF
NECK_ANGLE = 28
NECK = 7


def left_foreleg(r):
    r.part('left_front_leg', 'body', (2, BODY_HEIGHT - SHOULDER_HEIGHT, -4.5))
    r.cube('left_front_leg', (-1, -1, -1, 2, FORELEG + 1, 3), 'veined_leg')
    r.part('left_front_shank', 'left_front_leg', (0, FORELEG, 0))
    r.cube('left_front_shank', (-0.5, 0, -0.5, 1, SHANK, 1), 'shank')
    r.part('left_front_hoof', 'left_front_shank', (0, SHANK, 0))
    r.cube('left_front_hoof', (-1, 0, -1, 2, HOOF, 2), 'hoof')


def left_hindleg(r):
    r.part('left_hind_leg', 'body', (2, BODY_HEIGHT - HIP_HEIGHT, 5), (THIGH_ANGLE, 0, 0))
    r.cube('left_hind_leg', (-1, -1, -2, 2, THIGH, 4), 'veined_leg')
    r.part('left_hind_gaskin', 'left_hind_leg', (0, THIGH - 1, 0), (GASKIN_ANGLE - THIGH_ANGLE, 0, 0))
    r.cube('left_hind_gaskin', (-1, 0, -1, 2, GASKIN, 2), 'leg')
    r.part('left_hind_cannon', 'left_hind_gaskin', (0, GASKIN, 0), (-GASKIN_ANGLE, 0, 0))
    r.cube('left_hind_cannon', (-0.5, 0, -0.5, 1, CANNON, 1), 'shank')
    r.part('left_hind_hoof', 'left_hind_cannon', (0, CANNON, 0))
    r.cube('left_hind_hoof', (-1, 0, -1, 2, HOOF, 2), 'hoof')


def left_head_side(r):
    r.part('left_ear', 'head', (2, -2, 0), (-8, -24, 38))
    r.cube('left_ear', (-1, -6, 0, 3, 6, 0), 'ear')

    # A short beam that sweeps back from the brow, with two tines reaching forward.
    r.part('left_antler', 'head', (1, -3, -1), (-30, 0, 24))
    r.cube('left_antler', (-0.5, -4, -0.5, 1, 4, 1), 'crystal')
    r.part('left_brow_tine', 'left_antler', (0, -1, 0), (70, 0, 0))
    r.cube('left_brow_tine', (-0.5, -2, -0.5, 1, 2, 1), 'crystal')
    r.part('left_top_tine', 'left_antler', (0, -4, 0), (55, 0, -18))
    r.cube('left_top_tine', (-0.5, -2, -0.5, 1, 2, 1), 'crystal')


def doe():
    r = uw.Rig('crimson_doe', 'CrimsonDoeMesh', 'crimson_doe')
    r.parts[0]['pivot'] = (0, 24, 0)
    r.part('body', 'root', (0, -BODY_HEIGHT, 0))
    r.cube('body', (-2.5, -4, -7, 5, 7, 6), 'hide')                             # deep chest
    r.cube('body', (-2, -4, -1, 4, 6, 5), 'hide')                               # tucked waist
    r.cube('body', (-2.5, -4, 4, 5, 6, 4), 'hide')                              # rump

    r.part('tail', 'body', (0, -3, 8), (14, 0, 0))
    r.cube('tail', (-1, 0, 0, 2, 4, 1), 'tail')

    r.part('neck', 'body', (0, -2, -6), (NECK_ANGLE, 0, 0))
    r.cube('neck', (-1, -NECK, -1, 2, NECK + 2, 3), 'neck')
    r.part('head', 'neck', (0, -NECK, 0), (-NECK_ANGLE - 4, 0, 0))
    r.cube('head', (-2, -3, -3, 4, 4, 4), 'skull')
    r.cube('head', (-1, -2, -5, 2, 3, 2), 'snout')
    r.cube('head', (-1, -1, -6, 2, 2, 1), 'muzzle')

    left_foreleg(r)
    left_hindleg(r)
    left_head_side(r)
    mirror_left(r)
    return r


# ---------------------------------------------------------------- painting

HIDE = (205, 171, 158)
HIDE_LIGHT = (219, 190, 175)
HIDE_DARK = (183, 143, 133)
SADDLE = (153, 91, 88)
SADDLE_DARK = (131, 72, 73)
BELLY = (232, 217, 199)
INNER_EAR = (178, 92, 94)
SOCK = (117, 70, 70)
HOOF_DARK = (45, 29, 31)
HOOF_LIGHT = (72, 47, 47)
NOSE = (43, 27, 30)
EYE = (24, 14, 16)
GLINT = (255, 54, 48)
VEIN = (176, 84, 84)
VEIN_GLOW = (140, 22, 26)
CRYSTAL = (150, 18, 34)
CRYSTAL_LIGHT = (214, 40, 52)
CRYSTAL_BRIGHT = (255, 112, 104)

# Vessels on the flank as (z, y) from the body's pivot: one trunk from the shoulder to the hip and the branches it throws.
FLANK = (((-5, 0), (-2, -1), (2, 0), (5, -1)), ((-2, -1), (-1, -2)), ((2, 0), (4, 1)))


def segment_distance(z, y, a, b):
    dz, dy = b[0] - a[0], b[1] - a[1]
    t = max(0, min(1, ((z - a[0]) * dz + (y - a[1]) * dy) / (dz * dz + dy * dy)))
    return math.hypot(z - a[0] - dz * t, y - a[1] - dy * t)


def on_vessel(z, y):
    return any(segment_distance(z, y, a, b) < 0.4 for vessel in FLANK for a, b in zip(vessel, vessel[1:]))


def lit(rgb, glow=None):
    return rgb + (255,), (glow or rgb) + (255,)


def flat(rgb):
    return rgb + (255,), None


def coat(t):
    """Darker along the spine, paler toward the belly. Model y points down, so a cube's 'down' face is the top."""
    if t['face'] == 'down':
        return SADDLE if t['n'] < 0.94 else SADDLE_DARK
    if t['face'] == 'up':
        return BELLY
    band = (t['v'] + 0.5) / t['fh']
    if band < 0.2:
        return SADDLE
    if band > 0.82:
        return BELLY
    return HIDE_LIGHT if t['n'] > 0.96 else HIDE_DARK if t['n'] < 0.04 else HIDE


def hide(t):
    if t['face'] in ('west', 'east') and on_vessel(t['gz'], t['gy'] + 0.5 - (24 - BODY_HEIGHT)):
        return lit(VEIN, VEIN_GLOW)
    return flat(coat(t))


def leg(t):
    return flat(HIDE_DARK if t['face'] == 'south' or t['n'] < 0.05 else HIDE)


def veined_leg(t):
    # One vessel running down from the body, stopping short of the joint.
    if t['face'] in ('west', 'east') and t['u'] == t['fw'] // 2 and t['v'] < t['fh'] * 0.45:
        return lit(VEIN, VEIN_GLOW)
    return leg(t)


def shank(t):
    return flat(SOCK if (t['v'] + 0.5) / t['fh'] > 0.6 else HIDE_DARK)


def neck(t):
    if t['face'] in ('west', 'east') and t['u'] == t['fw'] // 2 and 3 <= t['v'] < t['fh'] - 2:
        return lit(VEIN, VEIN_GLOW)
    if t['face'] == 'north':
        return flat(BELLY)
    if t['face'] == 'south':
        return flat(SADDLE)
    return flat(HIDE_DARK if t['n'] < 0.05 else HIDE)


def skull(t):
    # The eye sits high on the side of the head, as a prey animal's does: a dark almond with one lit pixel.
    if t['face'] in ('west', 'east') and t['v'] == 1 and 1 <= t['u'] <= 2:
        return lit(GLINT) if t['u'] == (1 if t['face'] == 'east' else 2) else flat(EYE)
    if t['face'] == 'down':
        return flat(SADDLE)
    if t['face'] == 'up':
        return flat(BELLY)
    return flat(HIDE if t['v'] < t['fh'] - 1 else HIDE_LIGHT)


def snout(t):
    if t['face'] == 'down':
        return flat(SADDLE)
    if t['face'] == 'up' or (t['side'] and t['v'] == t['fh'] - 1):
        return flat(BELLY)
    return flat(HIDE)


def muzzle(t):
    if t['face'] == 'down' or (t['face'] == 'north' and t['v'] == 0):
        return flat(NOSE)
    return flat(BELLY)


def ear(t):
    # A leaf: narrow at the base, broad through the middle, drawn to a point.
    widths = {0: 1, 5: 1}
    if abs(t['u'] + 0.5 - t['fw'] / 2) > widths.get(t['v'], 3) / 2:
        return CLEAR, None
    # Both faces of a flat panel share one plane, so they take the same paint.
    return flat(INNER_EAR if t['u'] == 1 and 1 <= t['v'] <= 4 else HIDE_DARK)


def tail(t):
    return flat(SADDLE if t['face'] in ('south', 'down') else BELLY)


def hoof(t):
    return flat(HOOF_LIGHT if t['v'] == 0 and t['side'] else HOOF_DARK)


def crystal(t):
    # A lit facet and a dark one, so the prong reads as cut rather than painted.
    rgb = CRYSTAL_BRIGHT if t['face'] == 'down' else CRYSTAL_LIGHT if t['face'] in ('north', 'east') else CRYSTAL
    return lit(rgb, tuple(c // 2 for c in rgb))


PAINTERS = dict(hide=hide, leg=leg, veined_leg=veined_leg, shank=shank, neck=neck, skull=skull, snout=snout, muzzle=muzzle, ear=ear, tail=tail,
                hoof=hoof, crystal=crystal)


def paint(material, t):
    return PAINTERS[material](t)


# ---------------------------------------------------------------- preview

STRIDE = {'left_front_leg': (-32, 0, 0), 'left_front_shank': (40, 0, 0), 'right_front_leg': (26, 0, 0),
          'left_hind_leg': (THIGH_ANGLE + 22, 0, 0), 'right_hind_leg': (THIGH_ANGLE - 28, 0, 0),
          'right_hind_gaskin': (GASKIN_ANGLE - THIGH_ANGLE + 30, 0, 0), 'right_hind_cannon': (-GASKIN_ANGLE - 40, 0, 0)}


def preview(rig, image):
    poses = [({}, 0.65, 0.2), ({}, math.pi - 0.65, 0.2), ({}, math.pi / 2, 0.02), ({}, 0, 0.02), ({}, math.pi / 2, 1.4),
             (STRIDE, math.pi / 2, 0.02)]
    tiles = [uw.render_quads(posed(rig, **pose), image, yaw, pitch, size=420) for pose, yaw, pitch in poses]
    sheet = Image.new('RGB', (420 * 3, 420 * 2))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % 3) * 420, (i // 3) * 420))
    out = ROOT / 'build/crimson_doe_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = doe()
    size = hb.layout(rig)
    image, glow = uw.paint_atlas(rig, size, paint)
    TEXTURES.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURES / 'model_crimson_doe.png')
    glow.save(TEXTURES / 'model_crimson_doe_glow.png')
    JAVA.write_text(java_source(rig, size), encoding='utf-8')
    print(f'crimson_doe: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        preview(rig, image)


if __name__ == '__main__':
    main()

"""The Venous Strider: a tall heron with pale bare skin, a veined cloak of folded wings and long jointed legs.

Emits VenousStriderMesh.java (animation lives in the hand-written VenousStriderModel), the atlas and its emissive layer.

    python tools/model_export/venous_strider.py            # mesh + textures
    python tools/model_export/venous_strider.py --preview  # also build/venous_strider_preview.png
    node tools/model_export/java_model_to_bbmodel.mjs src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/VenousStriderMesh.java --texture textures/entity/venous_strider/model_venous_strider.png --out src/main/resources/assets/hemomancy/models/entity/bbmodel

Model space: head at -z, feet on y = 0 under the root. Legs hang from the root rather than the tilted body, so their angles
only have to cancel each other for the feet to stand flat. A bird's visible backward joint is its ankle: tibia slants back,
tarsus forward. Whole-number cube sizes at 16 px per block.
"""
import argparse
import math

from PIL import Image

import hemomancy_books as hb
import unstained_weapons as uw
from vampire_bat import java_source, mirror_left, posed

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/VenousStriderMesh.java'
TEXTURES = uw.ASSETS / 'textures/entity/venous_strider'

CLEAR = (0, 0, 0, 0)

TIBIA, TARSUS, FOOT = 6, 7, 1
TIBIA_ANGLE, TARSUS_ANGLE = 18, -14
HIP_HEIGHT = TIBIA * math.cos(math.radians(TIBIA_ANGLE)) + TARSUS * math.cos(math.radians(TARSUS_ANGLE)) + FOOT
BODY_TILT = -12
NECK_ANGLES = (35, -55, 45)
NECK_LENGTHS = (4, 5, 3)
HEAD_ANGLE = 6


def left_leg(r):
    r.part('left_leg', 'root', (2, -HIP_HEIGHT, 1), (TIBIA_ANGLE, 0, 0))
    r.cube('left_leg', (-1, -2, -1, 2, 4, 2), 'thigh')
    r.cube('left_leg', (-0.5, 2, -0.5, 1, TIBIA - 2, 1), 'shin')
    r.part('left_tarsus', 'left_leg', (0, TIBIA, 0), (TARSUS_ANGLE - TIBIA_ANGLE, 0, 0))
    r.cube('left_tarsus', (-0.5, 0, -0.5, 1, TARSUS, 1), 'shin')
    r.part('left_foot', 'left_tarsus', (0, TARSUS, 0), (-TARSUS_ANGLE, 0, 0))
    r.cube('left_foot', (-0.5, 0, -4, 1, FOOT, 4), 'toe')
    r.cube('left_foot', (-0.5, 0, 0, 1, FOOT, 2), 'toe')
    for name, turn in (('left_toe_out', -32), ('left_toe_in', 32)):
        r.part(name, 'left_foot', (0, 0, -0.5), (0, turn, 0))
        r.cube(name, (-0.5, 0, -3, 1, FOOT, 3), 'toe')


def left_wing(r):
    # Folded along the flank; it hinges along z at the shoulder and its primaries hinge at the wrist.
    r.part('left_wing', 'body', (2.5, -3, -5), (0, 0, -4))
    r.cube('left_wing', (0, 0, 0, 1, 5, 8), 'wing')
    r.part('left_primaries', 'left_wing', (0, 0, 8), (0, -8, 0))
    r.cube('left_primaries', (1, 0, 0, 0, 4, 7), 'primaries')


def strider():
    r = uw.Rig('venous_strider', 'VenousStriderMesh', 'venous_strider')
    r.parts[0]['pivot'] = (0, 24, 0)
    r.part('body', 'root', (0, -HIP_HEIGHT - 1, 0), (BODY_TILT, 0, 0))
    r.cube('body', (-2.5, -3, -6, 5, 6, 6), 'breast')
    r.cube('body', (-2, -3, 0, 4, 5, 5), 'breast')

    r.part('tail', 'body', (0, -2, 5), (-10, 0, 0))
    r.cube('tail', (-2, 0, 0, 4, 1, 6), 'tail')

    previous, net = 'body', BODY_TILT
    for i, (name, angle, length) in enumerate(zip(('neck_base', 'neck_mid', 'neck_top'), NECK_ANGLES, NECK_LENGTHS)):
        pivot = (0, -2, -5) if i == 0 else (0, -NECK_LENGTHS[i - 1], 0)
        r.part(name, previous, pivot, (angle, 0, 0))
        r.cube(name, (-1, -length, -1, 2, length + 1, 2), 'veined_neck' if name == 'neck_mid' else 'neck')
        previous, net = name, net + angle

    r.part('head', 'neck_top', (0, -NECK_LENGTHS[2], 0), (HEAD_ANGLE - net, 0, 0))
    r.cube('head', (-1, -2, -2, 2, 3, 3), 'skull')
    r.cube('head', (-0.5, -1, -8, 1, 1, 6), 'bill')
    r.part('jaw', 'head', (0, 0, -2))
    r.cube('jaw', (-0.5, 0, -5, 1, 1, 5), 'jaw')
    r.part('crest', 'head', (0, -2, 0), (-14, 0, 0))
    r.cube('crest', (0, -1, 0, 0, 3, 8), 'crest')

    left_wing(r)
    left_leg(r)
    mirror_left(r)
    return r


# ---------------------------------------------------------------- painting

SKIN = (214, 180, 176)
SKIN_LIGHT = (228, 202, 194)
SKIN_DARK = (188, 148, 150)
PLUME = (112, 92, 108)
PLUME_LIGHT = (138, 116, 128)
PLUME_DARK = (82, 64, 82)
OXBLOOD = (96, 32, 44)
OXBLOOD_DEEP = (58, 20, 32)
LEG = (76, 44, 58)
LEG_LIGHT = (122, 84, 92)
TOE = (52, 30, 42)
BILL = (198, 178, 150)
BILL_DARK = (150, 122, 104)
BILL_TIP = (96, 32, 44)
EYE = (24, 14, 18)
GLINT = (255, 86, 62)
VEIN = (168, 76, 88)
VEIN_WING = (150, 66, 80)
VEIN_GLOW = (92, 14, 22)

# Vessels across the folded wing as (z, y) from the shoulder: a trunk along the arm and shafts that fan off it.
WING_VESSELS = (((0, 2), (4, 2), (8, 3)), ((3, 2), (5, 4)))


def segment_distance(z, y, a, b):
    dz, dy = b[0] - a[0], b[1] - a[1]
    t = max(0, min(1, ((z - a[0]) * dz + (y - a[1]) * dy) / (dz * dz + dy * dy)))
    return math.hypot(z - a[0] - dz * t, y - a[1] - dy * t)


def lit(rgb, glow=None):
    return rgb + (255,), (glow or rgb) + (255,)


def flat(rgb):
    return rgb + (255,), None


def speckle(t, base, light, dark):
    return light if t['n'] > 0.95 else dark if t['n'] < 0.05 else base


def breast(t):
    # Model y points down, so a cube's 'down' face is the top.
    if t['face'] == 'down':
        return flat(PLUME)
    if t['face'] == 'north' and t['u'] == t['fw'] // 2 and t['v'] < t['fh'] - 1:
        return lit(VEIN, VEIN_GLOW)
    return flat(speckle(t, SKIN, SKIN_LIGHT, SKIN_DARK))


def neck(t):
    # One vessel winding down the bare throat: it steps across the column every three texels.
    if t['face'] == 'north' and t['u'] == (t['v'] // 3) % 2:
        return lit(VEIN, VEIN_GLOW)
    if t['face'] == 'south':
        return flat(SKIN_DARK)
    return flat(SKIN)


def veined_neck(t):
    # A branch comes round to the side of the neck here, the one stretch a side view shows.
    if t['face'] in ('west', 'east') and t['u'] == 0 and 1 <= t['v'] <= 3:
        return lit(VEIN, VEIN_GLOW)
    return neck(t)


def skull(t):
    if t['face'] in ('west', 'east') and t['u'] == 1 and t['v'] == 1:
        return lit(GLINT, tuple(c // 2 for c in GLINT))
    if t['face'] in ('west', 'east') and t['u'] == (0 if t['face'] == 'east' else 2) and t['v'] == 1:
        return flat(EYE)
    if t['face'] == 'down':
        return flat(PLUME_DARK)
    return flat(SKIN_LIGHT if t['face'] == 'up' or t['v'] == t['fh'] - 1 else SKIN)


def bill(t):
    return flat(BILL_TIP if t['gz'] < HEAD_ORIGIN_Z - 7 else BILL_DARK if t['face'] == 'up' else BILL)


def jaw(t):
    return flat(BILL_DARK)


def crest(t):
    # Rooted at the skull, then parting into two streamers that thin toward the back.
    along = t['gz'] - HEAD_ORIGIN_Z
    if t['v'] == 1 and along > 3 or t['v'] == 2 and along > 6 or t['v'] == 0 and along > 7:
        return CLEAR, None
    return flat(OXBLOOD if along < 4 else OXBLOOD_DEEP)


def wing(t):
    if t['face'] in ('west', 'east'):
        z, y = t['gz'] - WING_ORIGIN_Z, t['gy'] + 0.5 - WING_ORIGIN_Y
        if any(segment_distance(z, y, a, b) < 0.35 for vessel in WING_VESSELS for a, b in zip(vessel, vessel[1:])):
            return lit(VEIN_WING, VEIN_GLOW)
    if t['face'] == 'down':
        return flat(PLUME_DARK)
    # Rows of covert feathers: each row's lower edge darker.
    return flat(PLUME_DARK if t['v'] % 2 == 1 and (t['u'] + t['v']) % 4 == 0 else speckle(t, PLUME, PLUME_LIGHT, PLUME))


def primaries(t):
    # Long feathers sweeping to a low point at the tail end, oxblood at the tips.
    along = t['gz'] - PRIMARIES_ORIGIN_Z
    if along >= 7 - (t['fh'] - 1 - t['v']):
        return CLEAR, None
    return flat(OXBLOOD_DEEP if along >= 5 else OXBLOOD if along >= 3 else PLUME_DARK if t['v'] % 2 else PLUME)


def tail(t):
    if t['face'] in ('down', 'up'):
        return flat(OXBLOOD if t['u'] % 2 == 0 else OXBLOOD_DEEP)
    return flat(OXBLOOD_DEEP)


def thigh(t):
    return flat(PLUME_DARK if t['v'] == t['fh'] - 1 else speckle(t, PLUME, PLUME_LIGHT, PLUME_DARK))


def shin(t):
    # Pale at the ankle, where the skin stretches over the joint.
    return flat(LEG_LIGHT if t['v'] == 0 and t['fh'] >= 6 else LEG)


def toe(t):
    return flat(TOE)


PAINTERS = dict(breast=breast, neck=neck, veined_neck=veined_neck, skull=skull, bill=bill, jaw=jaw, crest=crest, wing=wing, primaries=primaries,
                tail=tail, thigh=thigh, shin=shin, toe=toe)


def paint(material, t):
    return PAINTERS[material](t)


# ---------------------------------------------------------------- preview

STEP = {'left_leg': (TIBIA_ANGLE - 42, 0, 0), 'left_tarsus': (TARSUS_ANGLE - TIBIA_ANGLE + 62, 0, 0),
        'left_foot': (-TARSUS_ANGLE + 30, 0, 0), 'right_leg': (TIBIA_ANGLE + 16, 0, 0)}


def preview(rig, image):
    poses = [({}, 0.65, 0.2), ({}, math.pi - 0.65, 0.2), ({}, math.pi / 2, 0.02), ({}, 0, 0.02), ({}, math.pi / 2, 1.4),
             (STEP, math.pi / 2, 0.02)]
    tiles = [uw.render_quads(posed(rig, **pose), image, yaw, pitch, size=420) for pose, yaw, pitch in poses]
    sheet = Image.new('RGB', (420 * 3, 420 * 2))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % 3) * 420, (i // 3) * 420))
    out = ROOT / 'build/venous_strider_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


HEAD_ORIGIN_Z = WING_ORIGIN_Z = WING_ORIGIN_Y = PRIMARIES_ORIGIN_Z = 0


def main():
    global HEAD_ORIGIN_Z, WING_ORIGIN_Z, WING_ORIGIN_Y, PRIMARIES_ORIGIN_Z
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = strider()
    world = uw.part_world(rig)
    HEAD_ORIGIN_Z = world['head'][2]
    WING_ORIGIN_Y, WING_ORIGIN_Z = world['left_wing'][1], world['left_wing'][2]
    PRIMARIES_ORIGIN_Z = world['left_primaries'][2]
    size = hb.layout(rig)
    image, glow = uw.paint_atlas(rig, size, paint)
    TEXTURES.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURES / 'model_venous_strider.png')
    glow.save(TEXTURES / 'model_venous_strider_glow.png')
    JAVA.write_text(java_source(rig, size), encoding='utf-8')
    print(f'venous_strider: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}, height {HIP_HEIGHT:.2f} at hip')
    if args.preview:
        preview(rig, image)


if __name__ == '__main__':
    main()

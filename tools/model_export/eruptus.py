"""The Erythromycelium Eruptus: a dead traveller on the ground, and the fungus that climbs out and walks away with it.

Emits ErythromyceliumEruptusMesh.java; the states and animation live in the hand-written ErythromyceliumEruptusModel.
The atlas is hand-painted and stays as it is, so every cube names its own texture offset.

    python tools/model_export/eruptus.py            # mesh
    python tools/model_export/eruptus.py --preview  # also build/eruptus_preview.png
    node tools/model_export/java_model_to_bbmodel.mjs src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/monster/ErythromyceliumEruptusMesh.java --texture textures/entity/erythromycelium_eruptus/model_erythromycelium_eruptus.png --out src/main/resources/assets/hemomancy/models/entity/bbmodel

The bind pose is the erupted stance: the host hangs from the growth with its feet clear and the four legs on the ground.
The model works from there back to the corpse, so its numbers are offsets from this pose.
"""
import argparse
import math

from PIL import Image

import unstained_weapons as uw
from vampire_bat import java_source, posed

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/monster/ErythromyceliumEruptusMesh.java'
TEXTURE = uw.ASSETS / 'textures/entity/erythromycelium_eruptus/model_erythromycelium_eruptus.png'

HOIST = 25
LIMB_HANG = 35
HIP_ROLL, KNEE, SHIN, TIP = 14, 30, 22, 12
HIP_PITCH, HIP_YAW = 18, 27
HALF_SPAN = 3.5

# Texture offsets on the hand-painted atlas.
HEAD, TORSO, ARM, LEG = (0, 0), (16, 16), (32, 48), (16, 48)
LEG_THICK, LEG_THIN = (47, 126), (48, 127)
CAP, LUMP, LUMP_WIDE, STALK, STALK_THICK = (6, 107), (10, 111), (8, 109), (2, 198), (9, 205)
MOUTH, MOUTH_NECK, MOUTH_LIP, MOUTH_THROAT, TONGUE = (9, 110), (12, 113), (10, 111), (11, 112), (87, 139)
HYPHAE = (60, 191)


def leg(r, side, end):
    """One leg along +x, or its mirror along -x. The hip sits on the growth, the tip on the ground."""
    sign = 1 if side == 'left' else -1
    name = f'{side}_{end}_'
    z = -HALF_SPAN if end == 'front' else HALF_SPAN
    pitch, yaw = (-HIP_PITCH, HIP_YAW) if end == 'front' else (HIP_PITCH, -HIP_YAW)

    def box(x, y, z_, w, h, d):
        return (x if sign > 0 else -x - w, y, z_, w, h, d)

    r.part(name + 'hip', 'growth', (sign * 4, -5, z), (pitch, sign * yaw, sign * HIP_ROLL))
    r.cube(name + 'hip', box(-2, 0, -1, 7, 2, 2), LEG_THICK)
    r.part(name + 'knee', name + 'hip', (sign * 5, 0, 0), (0, 0, sign * KNEE))
    r.cube(name + 'knee', box(0, 0, -1, 7, 2, 2), LEG_THIN)
    r.part(name + 'shin', name + 'knee', (sign * 7, 0, 0), (0, 0, sign * SHIN))
    r.cube(name + 'shin', box(-1, 0, -1, 13, 2, 2), LEG_THICK)
    r.part(name + 'tip', name + 'shin', (sign * 12, 1, 0), (0, 0, sign * TIP))
    r.cube(name + 'tip', box(-1, -1, -1, 6, 2, 2), LEG_THIN)
    r.cube(name + 'tip', box(5, -1, 0, 10, 1, 1), LEG_THIN)


def eruptus():
    r = uw.Rig('eruptus', 'ErythromyceliumEruptusMesh', 'erythromycelium_eruptus')
    r.parts[0]['pivot'] = (0, 24 - HOIST, 0)

    r.part('host', 'root', (0, 0, -4), (-90, 180, 0))
    r.cube('host', (-4, -4, -2, 8, 12, 4), TORSO)
    r.part('host_head', 'host', (0, -4, 0))
    r.cube('host_head', (-4, -8, -4, 8, 8, 8), HEAD)
    for name, x, box in (('host_left_arm', 4, (0, -1.5, -2, 4, 12, 4)), ('host_right_arm', -4, (-4, -1.5, -2, 4, 12, 4))):
        r.part(name, 'host', (x, -2.5, 0), (LIMB_HANG, 0, 0))
        r.cube(name, box, ARM)
    for name, x in (('host_left_leg', 2), ('host_right_leg', -2)):
        r.part(name, 'host', (x, 8, 0), (LIMB_HANG, 0, 0))
        r.cube(name, (-2, 0, -2, 4, 12, 4), LEG)

    r.part('growth', 'root')
    r.part('stalk', 'growth', (0, -9.5, 0), (-90, 0, 0))
    r.cube('stalk', (-2, -2, -5.5, 4, 4, 11), STALK)
    r.cube('stalk', (-2.5, -2.5, 1.5, 5, 5, 4), STALK_THICK)
    r.part('cap', 'stalk', (0, 0, -9))
    r.cube('cap', (-4, -4, -4.5, 8, 8, 9), CAP)
    r.cube('cap', (-5, -6, -5.5, 5, 5, 5), LUMP)
    r.cube('cap', (-5, 1, 0.5, 5, 5, 5), LUMP)
    r.cube('cap', (0, -6, -1.5, 7, 7, 7), LUMP_WIDE)

    # Three skirts of hyphae round the stalk's foot, each a panel with a hinged flap on either end.
    for index, tilt in enumerate((0, 37.5, -37.5)):
        skirt = f'skirt_{index}'
        r.part(skirt, 'stalk', (0, -1, 6), (0, 0, tilt))
        r.cube(skirt, (-8, -5, 0, 16, 10, 0), HYPHAE)
        r.part(f'{skirt}_left', skirt, (8, 0, 0), (0, -42.5, 0))
        r.cube(f'{skirt}_left', (0, -5, 0, 16, 10, 0), HYPHAE)
        r.part(f'{skirt}_right', skirt, (-8, 0, 0), (0, 42.5, 0))
        r.cube(f'{skirt}_right', (-16, -5, 0, 16, 10, 0), HYPHAE)

    r.part('mouth', 'growth', (0, -3, 0), (-37.5, 0, 0))
    r.cube('mouth', (-3, 0, -3, 6, 4, 6), MOUTH)
    r.cube('mouth', (-2, -1, -2, 4, 5, 4), MOUTH_THROAT)
    r.cube('mouth', (-2, 4, -2, 4, 4, 4), MOUTH_NECK)
    r.cube('mouth', (-3, 6, -3, 6, 4, 6), MOUTH_LIP)
    r.part('jaw', 'mouth', (0, 10, 0), (-27.5, 0, 0))
    r.cube('jaw', (-2, 0, -2, 4, 5, 4), MOUTH_THROAT)
    r.cube('jaw', (0, 2, -2, 0, 17, 4), TONGUE)

    for side in ('left', 'right'):
        for end in ('front', 'hind'):
            leg(r, side, end)
    for part in r.parts:
        for cube in part['cubes']:
            cube['uv'] = cube['material']
    return r


def extent(rig):
    """The lowest and highest model y of the bind pose; the ground is y = 24."""
    import numpy as np
    points = [pts for pts, _ in uw.rig_quads(rig)]
    return max(float(np.max(p[:, 1])) for p in points), min(float(np.min(p[:, 1])) for p in points)


def preview(rig, image):
    tiles = [uw.render_quads(posed(rig), image, yaw, pitch, size=420) for yaw, pitch in
             ((0.65, 0.2), (math.pi - 0.65, 0.2), (math.pi / 2, 0.02), (0, 0.02), (0.4, 1.2), (0.65, -0.5))]
    sheet = Image.new('RGB', (420 * 3, 420 * 2))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % 3) * 420, (i // 3) * 420))
    out = ROOT / 'build/eruptus_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = eruptus()
    source = java_source(rig, (256, 256)).replace('client.model.entity.mob.animal;', 'client.model.entity.mob.monster;')
    JAVA.write_text(source, encoding='utf-8')
    low, high = extent(rig)
    print(f'eruptus: {sum(len(p["cubes"]) for p in rig.parts)} cubes; lowest y {low:.2f}, highest y {high:.2f} (ground is 24)')
    if args.preview:
        preview(rig, Image.open(TEXTURE))


if __name__ == '__main__':
    main()

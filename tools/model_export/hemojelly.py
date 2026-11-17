"""The Hemojelly: a stepped blood bell over a clotted heart, four frilled oral arms and eight long stinging tendrils.

Emits HemojellyMesh.java (animation lives in the hand-written HemojellyModel), the atlas and its emissive layer.

    python tools/model_export/hemojelly.py            # mesh + textures
    python tools/model_export/hemojelly.py --preview  # also build/hemojelly_preview.png
    node tools/model_export/java_model_to_bbmodel.mjs src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic/HemojellyMesh.java --texture textures/entity/hemojelly/model_hemojelly.png --out src/main/resources/assets/hemomancy/models/entity/bbmodel

Model space: feet on y = 0 under the root. The bell's margin sits ten pixels up, where the entity floats at the
surface, and the tendrils hang two blocks below it, the depth of its feeding net. Whole-number cubes at 16 px per
block. The bell and arms are translucent; the heart is drawn first so it shows through them.
"""
import argparse
import math

from PIL import Image

import hemomancy_books as hb
import unstained_weapons as uw
from vampire_bat import java_source, posed

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic/HemojellyMesh.java'
TEXTURES = uw.ASSETS / 'textures/entity/hemojelly'

MARGIN = 10
TENDRIL_JOINTS = ('upper', 'middle', 'lower', 'tip')
TENDRIL_SEGMENT = 8
ARM_SEGMENT = 6
TENDRILS = ((5, 0), (-5, 0), (0, 5), (0, -5), (4, 4), (-4, 4), (4, -4), (-4, -4))
ARMS = ((1, 1), (-1, 1), (1, -1), (-1, -1))


def hemojelly():
    r = uw.Rig('hemojelly', 'HemojellyMesh', 'hemojelly')
    r.parts[0]['pivot'] = (0, 24, 0)
    r.part('heart', 'root', (0, -MARGIN, 0))
    r.cube('heart', (-2, -5, -2, 4, 3, 4), 'heart')
    r.cube('heart', (-1, -2, -1, 2, 1, 2), 'clot')

    r.part('bell', 'root', (0, -MARGIN, 0))
    r.cube('bell', (-6, -2, -6, 12, 3, 12), 'skirt')
    r.cube('bell', (-5, -5, -5, 10, 3, 10), 'dome')
    r.cube('bell', (-4, -7, -4, 8, 2, 8), 'dome')
    r.cube('bell', (-2, -8, -2, 4, 1, 4), 'crown')

    for i, (x, z) in enumerate(ARMS):
        name = f'arm{i}'
        r.part(name, 'root', (x, -MARGIN + 1, z))
        r.cube(name, (-1, 0, -1, 2, ARM_SEGMENT, 2), 'arm')
        r.part(name + '_frill', name, (0, ARM_SEGMENT, 0))
        r.cube(name + '_frill', (-1, 0, -1, 2, ARM_SEGMENT, 2), 'frill')

    for i, (x, z) in enumerate(TENDRILS):
        parent = r.part(f'tendril{i}', 'root', (x, -MARGIN + 1, z))
        r.cube(parent, (-0.5, 0, -0.5, 1, TENDRIL_SEGMENT, 1), 'tendril')
        for joint in TENDRIL_JOINTS[1:]:
            parent = r.part(f'tendril{i}_{joint}', parent, (0, TENDRIL_SEGMENT, 0))
            r.cube(parent, (-0.5, 0, -0.5, 1, TENDRIL_SEGMENT, 1), 'tip' if joint == 'tip' else 'tendril')
    return r


# ---------------------------------------------------------------- painting

ROSE = (176, 42, 56)
ROSE_LIGHT = (210, 82, 88)
CRIMSON = (150, 26, 42)
MAROON = (92, 16, 28)
CLOT = (58, 10, 18)
CANAL = (118, 18, 34)
MARGIN_PALE = (236, 156, 150)
HEART_HOT = (255, 70, 52)
HEART_GLOW = (220, 40, 36)
BEAD = (255, 168, 120)
BEAD_GLOW = (176, 64, 46)

BELL_ALPHA = 178
ARM_ALPHA = 200
TENDRIL_ALPHA = 220


def flat(rgb, alpha=255):
    return rgb + (alpha,), None


def lit(rgb, glow, alpha=255):
    return rgb + (alpha,), glow + (255,)


def noisy(rgb, t, amount=8):
    return hb.noisy(rgb, t, amount)


def radial_canal(t):
    """Canals run out from the crown along the diagonals and down each side, as a real bell's radial canals do."""
    if t['side']:
        return t['u'] in (0, t['fw'] // 2)
    return abs(abs(t['gx']) - abs(t['gz'])) < 0.1 and math.hypot(t['gx'], t['gz']) > 1


def skirt(t):
    # Model y points down: a cube's 'up' face is its underside.
    if t['face'] == 'up':
        return flat(MAROON, BELL_ALPHA)
    if t['side']:
        # A pale scalloped margin, one lobe in three dark.
        if t['v'] == t['fh'] - 1:
            return flat(MARGIN_PALE if t['u'] % 3 else ROSE_LIGHT, BELL_ALPHA)
        return flat(noisy(ROSE, t), BELL_ALPHA)
    return flat(CANAL if radial_canal(t) else noisy(ROSE, t), BELL_ALPHA)


def dome(t):
    if t['face'] == 'up':
        return flat(MAROON, BELL_ALPHA)
    if radial_canal(t):
        return flat(CANAL, BELL_ALPHA)
    shade = ROSE_LIGHT if t['face'] == 'down' else ROSE
    return flat(noisy(shade, t), BELL_ALPHA)


def crown(t):
    return flat(ROSE_LIGHT if t['face'] == 'down' else ROSE, BELL_ALPHA)


def heart(t):
    # Four gonad lobes round a hot core, read through the bell.
    if t['face'] in ('up', 'down'):
        cu, cv = abs(t['u'] + 0.5 - t['fw'] / 2), abs(t['v'] + 0.5 - t['fh'] / 2)
        if cu < 1 and cv < 1:
            return lit(HEART_HOT, HEART_GLOW)
        return flat(CRIMSON if (cu < 1) != (cv < 1) else MAROON)
    if t['v'] == 1 and t['u'] in (1, 2):
        return lit(HEART_HOT, HEART_GLOW)
    return flat(noisy(CRIMSON, t, 10))


def clot(t):
    return flat(CLOT)


def arm(t):
    if not t['side']:
        return flat(MAROON, ARM_ALPHA)
    return flat(CRIMSON if t['u'] % 2 else MAROON, ARM_ALPHA)


def frill(t):
    if not t['side']:
        return flat(CRIMSON, ARM_ALPHA)
    # Ruffled lips: light folds every other row, staggered by face.
    fold = (t['v'] + t['u']) % 2 == 0
    return flat(ROSE if fold else CRIMSON, ARM_ALPHA)


FACE_STEP = dict(north=0, east=3, south=1, west=4)


def bead(t, every):
    """Sparse stinging beads, staggered round the tendril so they spiral instead of banding."""
    return t['side'] and (t['v'] + FACE_STEP[t['face']]) % every == 0


def tendril(t):
    if bead(t, 8):
        return lit(BEAD, BEAD_GLOW, TENDRIL_ALPHA)
    return flat(CRIMSON, TENDRIL_ALPHA)


def tip(t):
    if bead(t, 4):
        return lit(BEAD, BEAD_GLOW, TENDRIL_ALPHA)
    return flat(ROSE, TENDRIL_ALPHA)


PAINTERS = dict(heart=heart, clot=clot, skirt=skirt, dome=dome, crown=crown, arm=arm, frill=frill, tendril=tendril,
                tip=tip)


def paint(material, t):
    return PAINTERS[material](t)


# ---------------------------------------------------------------- preview

def preview(rig, image):
    gather = {f'tendril{i}': (-math.copysign(6, z) if z else 0, 0, math.copysign(6, x) if x else 0)
              for i, (x, z) in enumerate(TENDRILS)}
    poses = [({}, 0.65, 0.2), ({}, math.pi / 2, 0.02), ({}, 0.4, 1.2), ({}, 0.4, -1.1), (gather, 0.65, 0.2),
             ({}, 0, 0.02)]
    tiles = [uw.render_quads(posed(rig, **pose), image, yaw, pitch, size=420) for pose, yaw, pitch in poses]
    sheet = Image.new('RGB', (420 * 3, 420 * 2))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % 3) * 420, (i // 3) * 420))
    out = ROOT / 'build/hemojelly_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = hemojelly()
    size = hb.layout(rig)
    image, glow = uw.paint_atlas(rig, size, paint)
    TEXTURES.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURES / 'model_hemojelly.png')
    glow.save(TEXTURES / 'model_hemojelly_glow.png')
    source = java_source(rig, size).replace('model.entity.mob.animal;', 'model.entity.mob.aquatic;', 1)
    JAVA.write_text(source, encoding='utf-8')
    print(f'hemojelly: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        preview(rig, image)


if __name__ == '__main__':
    main()

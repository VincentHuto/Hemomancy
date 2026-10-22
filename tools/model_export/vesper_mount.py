"""Vesper's mount: the crowned scorpion-thing he rides as the Crowned Refusal.

The rider (Vesper himself) stays in VesperTheCrownedRefusalModel; this script owns only the mount. It is built as a
huge insect: a thorax carrying the throne, a new neck and mandibled head between two grasping claw arms, six
four-segment legs (coxa, femur, tibia, hooked tarsus), a segmented abdomen and a six-segment tail that curls over the
rider, and two pairs of veined wings whose membranes are flat planes shaped by the texture's alpha.

Emits VesperMountModel.java and the textures:
  vesper_mount.png   the atlas (chitin, lacquer, bone, underbelly, membranes)

    python tools/model_export/vesper_mount.py            # write model + texture
    python tools/model_export/vesper_mount.py --preview  # also write build/vesper_mount_preview.png

Model space is Minecraft's: y points down, the ground is y = 24 and the head faces -z. Every segment of a limb, wing
or tail extends along its part's local +x (limbs, wings) or +z (tail, neck), so animation reads as plain joint angles.
Palette: it is an overgrown morphling, a blood organism grown over an insect. Mottled oxblood chitin sinks into dark
crevices, raw pore-pitted flesh and pale hyphae creep over it, branching veins run through everything, joints are wet
sinew, spines are blood-rooted bone, and the membranes carry a capillary web. Only Vesper's throne keeps its lacquer;
strapped behind it hang a Hermit's bottle gourd and the shattered morphling jar the organism grew out of.
No copper, brass or bronze anywhere on this body.
"""
import argparse
import math
from pathlib import Path

from annetta_kit import (ROOT, ASSETS, Rig, add, bone, clamp, entity_java_source, h, layout, mix, paint_atlases,
                         preview_sheet, quads, smooth)
import annetta_kit

# Vanilla texel density: one texture pixel per model unit, so the mount reads as a Minecraft mob rather than a
# high-resolution paint-over. The kit paints Annetta at two; the UV layout (and so the Java model) is unchanged.
TEXEL_SCALE = 1
annetta_kit.SCALE = TEXEL_SCALE

MODEL_DIR = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame'
TEXTURE_DIR = ASSETS / 'textures/entity/boss/endgame'
JAVA = MODEL_DIR / 'VesperMountModel.java'
ATLAS_WIDTH = 256

# Rest pose of the wings is fully flared; the animation folds them. Degrees (x, y, z).
FORE_WING_FLARED = (0.0, -8.0, -28.0)
HIND_WING_FLARED = (0.0, -30.0, -16.0)
FORE_WING_FOLDED = (0.0, -86.0, -10.0)
HIND_WING_FOLDED = (0.0, -94.0, -6.0)


def box(x, y_up, z, w, hgt, d):
    """A cube given with its bottom at y_up (up-positive), converted to Minecraft's y-down box."""
    return (x, -(y_up + hgt), z, w, hgt, d)


def up(x, y_up, z):
    return (x, -y_up, z)


def mirror_rot(rot):
    return (rot[0], -rot[1], -rot[2])


def mirror_box(b):
    x, y, z, w, hgt, d = b
    return (-x - w, y, z, w, hgt, d)


class Sided:
    """Builds a right (+x) part and its mirrored left twin together."""

    def __init__(self, rig):
        self.rig = rig

    def part(self, name, parent, pivot, rotation=(0, 0, 0)):
        for side, sign in (('r', 1), ('l', -1)):
            p = parent.format(s=side) if '{s}' in parent else parent
            self.rig.part(name.format(s=side), p, (pivot[0] * sign, pivot[1], pivot[2]),
                          rotation if sign > 0 else mirror_rot(rotation))

    def cube(self, name, b, material, **extra):
        self.rig.cube(name.format(s='r'), b, material, side=1, **extra)
        self.rig.cube(name.format(s='l'), mirror_box(b), material, side=-1, **extra)


def segment(length, thick, deep=None, start=0.0):
    """A limb segment along local +x, centred on the part's axis."""
    deep = thick if deep is None else deep
    return (start, -thick / 2, -deep / 2, length - start, thick, deep)


def mount():
    r = Rig('vesper_mount', 'VesperMountModel', 'vesper_mount')
    s = Sided(r)

    # ---- body: the hub sits at the thorax/abdomen joint, about 2 blocks off the ground.
    r.part('body', 'root', (0, -8, -4))
    r.part('thorax', 'body', (0, 0, 0))
    r.cube('thorax', box(-13, -6, -34, 26, 18, 30), 'carapace')
    r.cube('thorax', box(-11, 12, -32, 22, 4, 24), 'carapace_plate')
    r.cube('thorax', box(-12, -9, -32, 24, 4, 26), 'underbelly')
    r.cube('thorax', box(-14.5, 2, -30, 2.5, 8, 22), 'carapace_plate')
    r.cube('thorax', box(12, 2, -30, 2.5, 8, 22), 'carapace_plate')
    for i, z in enumerate((-31, -24, -17, -10)):
        r.cube('thorax', box(-8 + i % 2, 15.5, z, 16 - 2 * (i % 2), 2, 5), 'ridge')

    # ---- the throne the rider sits in, grown out of the thorax's back plate.
    # A raised dais lifts the throne so the rider's legs hang clear of the thorax.
    r.part('seat', 'thorax', up(0, 27, -4))
    r.cube('seat', box(-7, -14, -6, 14, 14, 12), 'throne')
    r.cube('seat', box(-8.5, -15, -7.5, 17, 2, 15), 'ridge')
    r.cube('seat', box(-9, 0, -8, 18, 3, 16), 'throne')
    r.cube('seat', box(-9.5, 3, 6, 19, 22, 3), 'throne')
    r.cube('seat', box(-7, 25, 6.5, 14, 6, 2), 'throne')
    for i, (x, hgt) in enumerate(((-7, 9), (-3.5, 13), (-1, 16), (1.5, 13), (5, 9))):
        r.cube('seat', box(x, 31, 7, 2, hgt, 1.5), 'bone_spike')
    r.cube('seat', box(-11, 3, -6, 2, 7, 12), 'throne')
    r.cube('seat', box(9, 3, -6, 2, 7, 12), 'throne')
    r.cube('seat', box(-11.5, 10, -7, 3, 2, 3), 'bone_spike')
    r.cube('seat', box(8.5, 10, -7, 3, 2, 3), 'bone_spike')

    # ---- strapped to the back of the throne: a big Hermit's bottle gourd slung across it, and the morphling jar
    # this thing burst out of, shattered on the thorax beside it, its tissue spilling over the rim and rooting into
    # the throne and the body. Both clear the carapace wound socket behind the throne.
    def gb(x, y, z, w, hgt, d, k=1.5):
        return box(x * k, y * k, z * k, w * k, hgt * k, d * k)

    r.part('throne_gourd', 'seat', up(-7, 2, 15), (-6, 18, 22))
    r.cube('throne_gourd', gb(-4, -1, -4, 8, 1.5, 8), 'gourd')
    r.cube('throne_gourd', gb(-5, 0, -5, 10, 10, 10), 'gourd')
    for b in ((-6, 1, -4, 1.2, 8, 8), (4.8, 1, -4, 1.2, 8, 8), (-4, 1, -6, 8, 8, 1.2), (-4, 1, 4.8, 8, 8, 1.2)):
        r.cube('throne_gourd', gb(*b), 'gourd')
    r.cube('throne_gourd', gb(-3, 10, -3, 6, 2.5, 6), 'gourd')
    r.cube('throne_gourd', gb(-4, 12, -4, 8, 7, 8), 'gourd')
    for b in ((-4.8, 13, -3, 0.9, 5, 6), (3.9, 13, -3, 0.9, 5, 6), (-3, 13, -4.8, 6, 5, 0.9), (-3, 13, 3.9, 6, 5, 0.9)):
        r.cube('throne_gourd', gb(*b), 'gourd')
    r.cube('throne_gourd', gb(-1.8, 19, -1.8, 3.6, 4, 3.6), 'gourd')
    r.cube('throne_gourd', gb(-2.3, 23, -2.3, 4.6, 2, 4.6), 'stopper')
    r.cube('throne_gourd', gb(-3.4, 10.6, -3.4, 6.8, 1.2, 6.8), 'rope')
    r.cube('throne_gourd', gb(-5.4, 5, -5.4, 10.8, 1.2, 10.8), 'rope')
    r.cube('throne_gourd', gb(-4, 5, -9, 1.2, 1.2, 4.6), 'rope')
    r.cube('throne_gourd', gb(2.8, 5, -9, 1.2, 1.2, 4.6), 'rope')
    r.cube('throne_gourd', gb(-1.2, 15, -8, 1.2, 1.2, 4.2), 'rope')
    r.cube('throne_gourd', gb(5.4, 1.5, -2.5, 1, 4, 1), 'bone')
    r.cube('throne_gourd', gb(5.4, 2.5, 1.5, 1, 3, 1), 'bone')

    r.part('throne_jar', 'seat', up(13.5, -10, 7), (8, -25, -12))
    r.cube('throne_jar', gb(-3.5, 0, -3.5, 7, 1, 7, 1.4), 'glass')
    r.cube('throne_jar', gb(-4, 1, -4, 8, 8, 8, 1.4), 'glass_broken')
    r.cube('throne_jar', gb(-4, 8, -4.1, 2.5, 3.5, 0.4, 1.4), 'glass')
    r.cube('throne_jar', gb(1.2, 8, 3.7, 2.8, 2.2, 0.4, 1.4), 'glass')
    r.cube('throne_jar', gb(3.7, 8, -2.5, 0.4, 3, 2, 1.4), 'glass')
    r.cube('throne_jar', gb(-3.3, 3, -3.3, 6.6, 7.5, 6.6, 1.4), 'spill')
    r.cube('throne_jar', gb(-2.5, 10.5, -2, 5, 2.5, 4.5, 1.4), 'spill')
    r.cube('throne_jar', gb(-1.2, 12.5, -1, 2.4, 2, 2.2, 1.4), 'spill')
    # Tendrils climbing out over the broken rim: into the throne's back, up its side, and down into the thorax.
    r.part('throne_jar_tendril_a', 'throne_jar', up(-4, 13, -1), (0, 30, 30))
    r.cube('throne_jar_tendril_a', (-16, -1.5, -1.5, 16, 3, 3), 'spill')
    r.part('throne_jar_tendril_b', 'throne_jar', up(-3, 12, 3), (0, -40, -20))
    r.cube('throne_jar_tendril_b', (-14, -1.2, -1.2, 14, 2.4, 2.4), 'spill')
    r.part('throne_jar_tendril_c', 'throne_jar', up(4, 10, -2), (0, 60, 75))
    r.cube('throne_jar_tendril_c', (-14, -1.2, -1.2, 14, 2.4, 2.4), 'spill')
    # The jar's neck and stopper, snapped off and caught in the gourd's rope.
    r.part('throne_jar_neck', 'throne_gourd', up(8.4, 7.5, -4), (60, 25, -20))
    r.cube('throne_jar_neck', gb(-1.6, 0, -1.6, 3.2, 3, 3.2, 1.4), 'glass')
    r.cube('throne_jar_neck', gb(-2.1, 3, -2.1, 4.2, 1.6, 4.2, 1.4), 'stopper')

    # ---- neck and head (new: the old mount had none). Vesper grips the nape for the burrow.
    r.part('neck', 'thorax', up(0, 4, -33), (-14, 0, 0))
    r.cube('neck', box(-6, -6, -12, 12, 12, 13), 'carapace')
    r.cube('neck', box(-5, 5.5, -11, 10, 2, 10), 'carapace_plate')
    r.cube('neck', box(-4.5, -7.5, -11, 9, 2, 10), 'underbelly')
    r.part('burrow_neck_grip', 'neck', up(0, 7, -4))
    r.part('wound_neck', 'neck', up(0, -6, -7))
    r.part('head', 'neck', up(0, 1, -12), (10, 0, 0))
    r.cube('head', box(-9, -7, -15, 18, 14, 15), 'carapace')
    r.cube('head', box(-10, 3, -16, 20, 5, 13), 'carapace_plate')
    r.cube('head', box(-6, 8, -14, 12, 3, 10), 'ridge')
    r.cube('head', box(-7, -8, -14, 14, 2, 12), 'underbelly')
    for x in (-7.5, 4.5):
        r.cube('head', box(x, 1, -15.5, 3, 3, 1), 'eye')
        r.cube('head', box(x + 0.5, -2.5, -15.5, 2, 2, 1), 'eye')
    s.part('mandible_{s}', 'head', up(5, -5, -14), (0, 18, 0))
    s.cube('mandible_{s}', (-1.5, -1.5, -11, 3, 3, 11), 'bone')
    s.cube('mandible_{s}', (-2.5, -1, -14, 2, 2, 4), 'bone_spike')
    s.part('antenna_{s}', 'head', up(5, 7, -12), (-28, 24, 0))
    s.cube('antenna_{s}', (-0.5, -0.5, -20, 1, 1, 20), 'carapace_plate')

    # ---- grasping claw arms beside the head.
    s.part('claw_arm_{s}', 'thorax', up(12, 2, -30), (0, 55, 22))
    s.cube('claw_arm_{s}', segment(19, 7, 8), 'carapace')
    s.cube('claw_arm_{s}', (-2, -4.5, -4.5, 4, 9, 9), 'joint')
    s.part('claw_fore_{s}', 'claw_arm_{s}', (19, 0, 0), (0, 45, -30))
    s.cube('claw_fore_{s}', segment(17, 6, 7), 'carapace')
    s.cube('claw_fore_{s}', (8, -4, -1, 6, 1.5, 2), 'bone_spike')
    s.part('pincer_{s}', 'claw_fore_{s}', (17, 0, 0), (0, -10, 0))
    s.cube('pincer_{s}', (-1, -4.5, -4.5, 10, 9, 9), 'carapace_plate')
    s.cube('pincer_{s}', (9, -3, -3.5, 9, 3, 4), 'bone')
    s.cube('pincer_{s}', (16, -2.5, -4.5, 3, 2, 3), 'bone_spike')
    s.part('pincer_finger_{s}', 'pincer_{s}', (8, 3, 0), (0, 0, 14))
    s.cube('pincer_finger_{s}', (0, -1, -3, 10, 3, 3.5), 'bone')
    s.cube('pincer_finger_{s}', (8, -2.5, -3.5, 3, 2, 3), 'bone_spike')
    s.part('claw_tip_{s}', 'pincer_{s}', (18, 0, 0))

    # ---- six legs: coxa, femur rising to a high knee, tibia, hooked tarsus.
    for i, (z, sweep) in enumerate(((-28, 34), (-17, 4), (-7, -30))):
        s.part(f'leg{i}_coxa_{{s}}', 'thorax', up(12.5, 0, z), (0, sweep, 0))
        s.cube(f'leg{i}_coxa_{{s}}', segment(9, 7, 7), 'joint')
        s.part(f'leg{i}_femur_{{s}}', f'leg{i}_coxa_{{s}}', (8, 0, 0), (0, 0, -54))
        s.cube(f'leg{i}_femur_{{s}}', segment(27, 6, 6), 'carapace')
        s.cube(f'leg{i}_femur_{{s}}', (4, -4, -1, 18, 1.5, 2), 'ridge')
        s.cube(f'leg{i}_femur_{{s}}', (25, -4, -4, 4, 8, 8), 'joint')
        s.part(f'leg{i}_tibia_{{s}}', f'leg{i}_femur_{{s}}', (27, 0, 0), (0, 0, 120))
        s.cube(f'leg{i}_tibia_{{s}}', segment(44, 5, 5), 'carapace')
        for k in range(3):
            s.cube(f'leg{i}_tibia_{{s}}', (8 + k * 12, -3.5, -0.75, 3, 1.5, 1.5), 'bone_spike')
        s.part(f'leg{i}_tarsus_{{s}}', f'leg{i}_tibia_{{s}}', (44, 0, 0), (0, 0, 22))
        s.cube(f'leg{i}_tarsus_{{s}}', segment(12, 3.5, 3.5), 'carapace_plate')
        s.cube(f'leg{i}_tarsus_{{s}}', (11, -1, -1.5, 3, 5, 3), 'bone_spike')

    # ---- abdomen: four overlapping plates swelling then tapering to the tail.
    r.part('abdomen', 'body', (0, 0, -4))
    # The raised top scutes are their own parts so the Carapace Aneurysm can blow them off and regrow them.
    for scute in ('scute_front', 'scute_mid', 'scute_rear'):
        r.part(scute, 'abdomen')
    scutes = ('scute_front', 'scute_mid', 'scute_rear', 'scute_rear')
    for i, (z, width, top, bottom) in enumerate(((0, 30, 14, -7), (11, 33, 15, -9), (22, 30, 14, -8),
                                                 (32, 24, 12, -5))):
        r.cube('abdomen', box(-width / 2, bottom, z, width, top - bottom, 12), 'carapace')
        r.cube(scutes[i], box(-width / 2 + 1.5, top, z + 0.5, width - 3, 1.5, 10), 'carapace_plate')
        r.cube(scutes[i], box(-3, top + 1.5, z + 2, 6, 1.5, 7), 'ridge')
        r.cube('abdomen', box(-width / 2 + 2, bottom - 1.5, z + 1, width - 4, 1.5, 10), 'underbelly')
    r.part('wound_carapace', 'abdomen', up(0, 15.5, 14))
    r.part('wound_tail', 'abdomen', up(0, -9, 38))

    # ---- tail: six segments curling up and forward over the rider, ending in a forked stinger.
    lengths = (22, 20, 19, 17, 15, 13)
    thick = (12, 11, 10, 9, 8, 7)
    r.part('tail0', 'abdomen', up(0, 6, 42), (44, 0, 0))
    for i, (length, t) in enumerate(zip(lengths, thick)):
        name = f'tail{i}'
        if i > 0:
            r.part(name, f'tail{i - 1}', (0, 0, lengths[i - 1]), (27, 0, 0))
        r.cube(name, (-t / 2, -t / 2, 0, t, t, length), 'carapace')
        r.cube(name, (-t / 2 + 1, -t / 2 - 1.5, 1, t - 2, 1.5, length - 3), 'carapace_plate')
        r.cube(name, (-t / 2 - 0.5, -t / 2 - 0.5, length - 3, t + 1, t + 1, 3), 'ridge')
    r.part('stinger', 'tail5', (0, 0, 13), (30, 0, 0))
    r.cube('stinger', (-4, -4, 0, 8, 8, 8), 'underbelly')
    r.cube('stinger', (-1.5, -1.5, 7, 3, 3, 9), 'bone')
    r.cube('stinger', (-1, 1, 14, 2, 2, 4), 'bone_spike')
    s.part('stinger_prong_{s}', 'stinger', (2, -1, 6), (0, -14, 0))
    s.cube('stinger_prong_{s}', (-0.75, -0.75, 0, 1.5, 1.5, 9), 'bone_spike')
    r.part('stinger_tip', 'stinger', (0, 2, 18))

    # ---- wings: two pairs behind the throne. Rest pose is flared; animation folds them flat along the back.
    for prefix, pivot, rest, length, chord in (('wing_fore', up(10, 14, 2), FORE_WING_FLARED, 78, 26),
                                               ('wing_hind', up(11, 12, 14), HIND_WING_FLARED, 64, 24)):
        s.part(prefix + '_{s}', 'thorax', pivot, rest)
        s.cube(prefix + '_{s}', (-2, -2.5, -2.5, 5, 5, 5), 'joint')
        s.cube(prefix + '_{s}', segment(length, 1.6, 1.6), 'spar')
        # Membranes are their own parts so the renderer can draw them in a translucent pass.
        s.part(prefix + '_membrane_{s}', prefix + '_{s}', (0, 0, 0))
        s.cube(prefix + '_membrane_{s}', (1, 0, 0, length - 1, 0, chord), 'membrane', length=length, chord=chord)
        s.part(prefix + '_tip_{s}', prefix + '_{s}', (length, 0, 4))

    # ---- the scute a wound splits into: never drawn with the body; VesperWoundLayer hinges four of them open.
    r.part('wound_scute', 'root', (0, 0, 0))
    r.cube('wound_scute', (-5, -1, -11, 10, 2, 11), 'wound_scute')
    return r


# ---------------------------------------------------------------- painting
#
# The mount is an overgrown morphling: a blood organism grown over an insect. Its armour is read as scutes, not as
# noise: every chitin surface is split into cellular plates (each its own tone), lit along the free margin that
# overlaps the next plate and shadowed where the plate before it lies over its root. Living tissue only shows in the
# seams between plates, and where the organism swells it pushes the plates apart. The palette is warm umber-maroon
# horn with worn ochre margins and a paler soft belly, so the red is kept for what is alive: seams, vessels, flesh.

from functools import lru_cache

LACQUER = (138, 14, 26)
SCUTE_DARK = (42, 17, 21)
SCUTE_MID = (94, 42, 44)
SCUTE_WARM = (118, 60, 54)
MARGIN = (164, 126, 98)
UNDERLAP = (18, 8, 10)
SEAM_DEEP = (52, 6, 12)
SEAM_FLESH = (128, 24, 32)
FLESH_DARK = (104, 18, 26)
FLESH_RAW = (162, 44, 48)
FLESH_WET = (206, 92, 86)
VEIN = (176, 18, 28)
VEIN_HOT = (226, 62, 48)
HYPHA = (196, 170, 152)
# Scales grow back along the body and downward, so their free margins face the tail and the ground.
GROWTH = (0.0, 0.45, 0.89)


def fbm(x, y, z, scale, seed=0, octaves=3):
    total, amp, norm = 0.0, 1.0, 0.0
    for o in range(octaves):
        total += smooth(x, y, z, scale / (2 ** o), seed + o * 17) * amp
        norm += amp
        amp *= 0.5
    return total / norm


def warped(t, amount=2.0, scale=7.0, seed=40):
    """The pixel's model-space position pushed around by noise, so patterns grow instead of tiling."""
    x, y, z = t['gx'], t['gy'], t['gz']
    return (x + (smooth(x, y, z, scale, seed) - 0.5) * 2 * amount,
            y + (smooth(x, y, z, scale, seed + 3) - 0.5) * 2 * amount,
            z + (smooth(x, y, z, scale, seed + 6) - 0.5) * 2 * amount)


def ridge(p, scale, seed):
    """1 along the noise's mid-level crossings: thin branching lines (veins, hyphae, creases)."""
    return 1.0 - abs(2.0 * smooth(p[0], p[1], p[2], scale, seed) - 1.0)


def crevice(t):
    edge = min(t['fu'], 1 - t['fu'], t['fv'], 1 - t['fv'])
    return clamp(1.0 - edge / 0.14)


@lru_cache(maxsize=None)
def _feature(cx, cy, cz, seed):
    return (cx + 0.15 + 0.7 * h(cx * 1.7 + seed, cy * 1.3, cz * 0.9),
            cy + 0.15 + 0.7 * h(cx * 0.7, cy * 1.9 + seed, cz * 1.1),
            cz + 0.15 + 0.7 * h(cx * 1.3, cy * 0.5, cz * 1.7 + seed))


def cells(p, scales, seed):
    """
    Worley cells with per-axis cell sizes: (distance to the plate's centre, distance to its border, cell id, offset
    from the centre, in cell units).
    """
    x, y, z = p[0] / scales[0], p[1] / scales[1], p[2] / scales[2]
    xi, yi, zi = math.floor(x), math.floor(y), math.floor(z)
    best = second = 9.0
    cell, offset = None, (0.0, 0.0, 0.0)
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            for dz in (-1, 0, 1):
                key = (xi + dx, yi + dy, zi + dz)
                fx, fy, fz = _feature(key[0], key[1], key[2], seed)
                d = (fx - x) ** 2 + (fy - y) ** 2 + (fz - z) ** 2
                if d < best:
                    second, best, cell, offset = best, d, key, (x - fx, y - fy, z - fz)
                elif d < second:
                    second = d
    d1, d2 = math.sqrt(best), math.sqrt(second)
    return d1, d2 - d1, cell, offset


def cube_frame(cube, lo=3.4, hi=7.0, k=0.6):
    """Cell sizes stretched along the cube's long axis, and the growth direction scales overlap toward."""
    _, _, _, w, hgt, d = cube['box']
    dims = (w, hgt, d)
    long_axis = max(range(3), key=lambda i: dims[i])
    minor = min(v for v in dims if v > 0.01) if any(v > 0.01 for v in dims) else lo
    base = clamp(minor * k, lo, hi)
    scales = [base, base, base]
    scales[long_axis] = base * 1.8
    growth = [0.0, 0.3, 0.0]
    growth[long_axis] = 1.0
    norm = math.sqrt(sum(g * g for g in growth))
    return scales, tuple(g / norm for g in growth)


def overgrowth(t, bias=0.0):
    """0 on clean armour, 1 where the blood organism has swollen up between and over the plates."""
    low = clamp((t['gy'] - 8.0) / 20.0) * 0.05
    g = fbm(t['gx'], t['gy'], t['gz'], 12.0, seed=7) + bias + low
    return clamp((g - 0.62) / 0.07)


def flesh_patch(t, p):
    rgb = mix(FLESH_DARK, FLESH_RAW, fbm(p[0], p[1], p[2], 3.0, seed=21, octaves=2))
    pore = smooth(t['gx'], t['gy'], t['gz'], 1.5, 23)
    if pore > 0.8:
        rgb = mix(rgb, (46, 5, 9), 0.7)                   # pitted pores, in small clusters
    elif pore < 0.16:
        rgb = mix(rgb, FLESH_WET, 0.55)                    # swollen wet blisters
    if ridge(p, 3.0, 61) > 0.95:
        rgb = mix(rgb, HYPHA, 0.5)                         # pale hyphae threading the growth
    return rgb


def veins(rgb, p, depth):
    """A few vessels where the organism runs thick, swelling and thinning along their length."""
    q = (p[0] * 1.3 + 11.0, p[1] * 1.3, p[2] * 1.3)
    if smooth(q[0], q[1], q[2], 14.0, 37) > 0.45:
        swell = smooth(q[0], q[1], q[2], 4.0, 39)
        r = ridge(q, 9.0, 31)
        if r > 0.975 - 0.025 * swell:
            return mix(rgb, VEIN_HOT if swell > 0.6 else VEIN, depth)
        if r > 0.955 - 0.025 * swell:
            return mix(rgb, (34, 3, 7), 0.5 * depth)
    return rgb


def scutes(cube, t, dark=SCUTE_DARK, mid=SCUTE_MID, gap=0.1, gloss=0.0):
    """
    Overlapping scutes: each plate domed and toned on its own, a worn pale free margin where it overlaps the next,
    deep shadow at its root under the plate before it. Seams stay dark; only where the organism swells do they open
    onto raw tissue.
    """
    p = warped(t, 1.0, 9.0)
    scales, growth = cube_frame(cube)
    d1, edge, cell, off = cells(p, scales, 13)
    tone = h(cell[0] * 3.1, cell[1] * 2.3, cell[2] * 1.7)
    rgb = mix(dark, mid, 0.2 + 0.65 * tone)
    if tone > 0.84:
        rgb = mix(rgb, SCUTE_WARM, 0.45)                    # the odd warmer, younger plate
    rgb = add(rgb, (t['n'] - 0.5) * 10)
    # Domed: the plate swells in the middle and falls away to its border.
    dome = clamp(edge / 0.4)
    rgb = mix(mix(rgb, UNDERLAP, 0.55), rgb, 0.45 + 0.55 * dome)
    length = math.sqrt(off[0] ** 2 + off[1] ** 2 + off[2] ** 2) + 1e-6
    along = (off[0] * growth[0] + off[1] * growth[1] + off[2] * growth[2]) / length
    rgb = mix(rgb, MARGIN, 0.6 * clamp(along * 1.4) * (1.0 - clamp(edge / 0.24)))     # worn free margin
    rgb = mix(rgb, UNDERLAP, 0.85 * clamp(-along * 1.4) * (1.0 - clamp(edge / 0.36)))  # root in shadow
    if gloss and smooth(t['gx'], t['gy'], t['gz'], 2.0, 46) > 0.74:
        rgb = mix(rgb, MARGIN, gloss * 0.3)
    grow = overgrowth(t)
    seam = gap + 0.2 * grow
    if edge < seam:
        if grow > 0.25:
            rgb = veins(flesh_patch(t, p), p, 0.8)          # tissue swelling up between the plates
        else:
            rgb = mix(UNDERLAP, (60, 10, 16), clamp(edge / seam) * 0.6)
    elif grow > 0.88:
        rgb = mix(rgb, flesh_patch(t, p), (grow - 0.88) / 0.12 * 0.75)
    return mix(rgb, (10, 4, 6), crevice(t) * 0.3)


def soft_belly(t):
    """The soft underside: pale, mottled membrane swelling in uneven segments, creases that fade in and out."""
    p = warped(t, 2.2, 6.0)
    band = (p[2] / 5.5) % 1.0
    swell = math.sin(band * math.pi)
    mottle = fbm(p[0], p[1], p[2], 3.5, seed=9)
    rgb = mix((110, 54, 60), (168, 106, 102), 0.2 + 0.55 * swell + 0.25 * mottle)
    if (band < 0.12 or band > 0.93) and smooth(p[0], p[1], p[2], 3.0, 11) > 0.38:
        rgb = mix(rgb, (66, 24, 32), 0.7)                  # creases, broken where the segments fuse
    if mottle > 0.66:
        rgb = mix(rgb, (128, 44, 52), 0.5)                 # bruised blotches
    rgb = veins(rgb, p, 0.5)
    if smooth(t['gx'], t['gy'], t['gz'], 1.5, 23) < 0.12:
        rgb = mix(rgb, (214, 170, 150), 0.4)               # wet sheen
    return mix(rgb, (60, 24, 28), crevice(t) * 0.4)


def wing_alpha(cube, t):
    """Membrane silhouette and veins in the plane: u runs root->tip along the spar, v from spar to trailing edge."""
    if t['face'] not in ('up', 'down'):
        return None
    u = abs(t['lx']) / cube['length']
    v = abs(t['lz']) / cube['chord']
    if u < 0 or u > 1:
        return None
    ragged = (smooth(u * 30, v * 4, 0, 1.7, 90) - 0.5) * 0.08
    width = 0.95 * math.sin(math.pi * min(1.0, (u + 0.06) / 1.06)) ** 0.6 * (1.0 - 0.35 * u) + ragged
    if v > width:
        return None
    pw = (u * cube['length'], v * cube['chord'], 0.0)
    rgb = mix((70, 22, 26), (122, 48, 46), smooth(t['gx'], t['gy'], t['gz'], 6.0))
    alpha = 140 + int(40 * smooth(pw[0], pw[1], 0, 5.0, 77))
    for k, vein in enumerate((0.2, 0.45, 0.72)):
        wander = (smooth(u * 20, k * 10, 0, 2.5, 80 + k) - 0.5) * 0.12
        if abs(v - (vein + wander) * width * (1.0 - 0.2 * u)) < 0.032 - 0.014 * u and u > 0.05 * k:
            return (36, 12, 14, 235)
    if ridge(pw, 3.6, 85) > 0.94:
        rgb, alpha = mix(rgb, (150, 30, 36), 0.6), max(alpha, 200)
    if width - v < 0.05:
        rgb, alpha = (150, 60, 52), 220
    return rgb + (alpha,)


def gourd(t):
    """The Hermit's dried bottle gourd: tan rind ribbed lengthwise, speckled, the organism's stain seeping up it."""
    p = warped(t, 0.8, 5.0, 61)
    rgb = mix((150, 118, 80), (196, 164, 116), fbm(p[0], p[1], p[2], 5.0, seed=62))
    rib = 0.5 + 0.5 * math.sin(math.atan2(t['lz'], t['lx']) * 9.0)
    rgb = mix(rgb, (118, 88, 58), clamp(rib - 0.7) * 1.8)
    if h(t['gx'] * 3.3, t['gy'] * 3.3, t['gz'] * 3.3) > 0.93:
        rgb = mix(rgb, (92, 64, 42), 0.6)                  # dried speckle
    stain = clamp((fbm(t['gx'], t['gy'], t['gz'], 6.0, seed=64) - 0.45) * 2.5)
    rgb = mix(rgb, (98, 26, 28), stain * 0.6)             # blood wicking into the rind
    return mix(rgb, (84, 60, 40), crevice(t) * 0.35)


def rope(t):
    twist = (t['u'] + t['v']) % 4
    rgb = (122, 88, 56) if twist < 2 else (92, 64, 40)
    return add(rgb, (t['n'] - 0.5) * 14)


def glass(cube, t, broken=False):
    """The morphling jar's green glass, as on the item: dark maroon rim lines, pale streaks, the tissue behind it."""
    edge = min(t['fu'], 1 - t['fu'], t['fv'], 1 - t['fv'])
    if broken and t['face'] == 'up':
        return None
    if broken and t['side']:
        # A jagged break: everything above a wandering line has shattered away.
        line = 0.38 + 0.32 * smooth(t['gx'] * 2.0, 0, t['gz'] * 2.0, 1.4, 66) \
            + 0.12 * h(t['u'] * 1.0, 3.0, 7.0)
        if t['fv'] < 1.0 - line:
            return None
        if t['fv'] < 1.0 - line + 0.07:
            return (206, 234, 212, 255)                    # bright fractured edge
    rgb = mix((58, 128, 92), (96, 182, 128), smooth(t['gx'], t['gy'], t['gz'], 2.0, 67))
    if 0.18 < t['fu'] < 0.28 and t['side']:
        rgb = mix(rgb, (200, 240, 210), 0.7)              # a long pale streak of light down the glass
    inside = smooth(t['gx'], t['gy'], t['gz'], 2.4, 68)
    if inside > 0.55 and t['side']:
        rgb = mix(rgb, (90, 20, 34), (inside - 0.55) * 2.2)   # the morphling pressed against the glass
    if edge < 0.09 and not (broken and t['side'] and t['fv'] < 0.5):
        rgb = (72, 16, 34)                                 # the item's dark maroon outline
    return rgb + (255,)


def paint(cube, t):
    m = cube['material']
    if m == 'membrane':
        return wing_alpha(cube, t)
    if m in ('glass', 'glass_broken'):
        return glass(cube, t, m == 'glass_broken')
    if m == 'carapace':
        rgb = scutes(cube, t)
    elif m == 'carapace_plate':
        rgb = scutes(cube, t, dark=(30, 14, 16), mid=(74, 38, 36), gap=0.09, gloss=1.0)
    elif m == 'wound_scute':
        if t['face'] == 'up':
            rgb = scutes(cube, t, dark=(30, 14, 16), mid=(74, 38, 36), gap=0.09, gloss=1.0)
        else:
            p = warped(t)
            rgb = veins(flesh_patch(t, p), p, 1.0)
            if t['side']:
                rgb = mix(rgb, (60, 6, 12), 0.35)
    elif m == 'ridge':
        rgb = mix((58, 30, 30), MARGIN, clamp(t['fv'] * 1.2) * 0.6 + t['n'] * 0.2)
        rgb = mix(rgb, (20, 8, 10), crevice(t) * 0.4)
    elif m == 'underbelly':
        rgb = soft_belly(t)
    elif m == 'joint':
        # Sinew: wet tendon, striated along its length.
        p = warped(t, 1.2, 4.0)
        stri = 0.5 + 0.5 * math.sin((p[0] + p[2]) * 1.5 + fbm(p[0], p[1], p[2], 3.0, seed=14) * 6)
        rgb = mix((110, 44, 46), (172, 112, 100), stri * 0.7 + t['n'] * 0.3)
        rgb = mix(rgb, (60, 14, 18), crevice(t) * 0.55)
        if smooth(t['gx'], t['gy'], t['gz'], 1.2, 48) > 0.8:
            rgb = mix(rgb, (226, 176, 160), 0.4)
    elif m in ('bone', 'bone_spike'):
        # Grown bone, never clean: yellowed and uneven, washed with old blood.
        rgb = mix((126, 102, 80), (180, 160, 128), fbm(t['gx'], t['gy'], t['gz'], 3.0, seed=70))
        rgb = mix(rgb, (92, 30, 30), clamp((fbm(t['gx'], t['gy'], t['gz'], 5.0, seed=72) - 0.45) * 1.6) * 0.6)
        rgb = mix(rgb, (52, 18, 18), crevice(t) * 0.4)
        if h(t['gx'] * 4.3, t['gy'] * 4.3, t['gz'] * 4.3) > 0.9:
            rgb = add(rgb, -40)
        if m == 'bone_spike':
            rgb = mix(rgb, (76, 16, 20), clamp(1.0 - t['fv'] * 1.6) * 0.75)
    elif m == 'spar':
        rgb = mix((40, 18, 20), (88, 46, 42), t['n'])
        rgb = mix(rgb, MARGIN, clamp(t['fv'] - 0.8) * 2.0)
    elif m == 'eye':
        rgb = (255, 52, 34) if t['n'] > 0.25 else (200, 20, 20)
    elif m == 'throne':
        # His throne is made, not grown, so it keeps its lacquer; the organism is only starting to claim it.
        rgb = mix((14, 8, 10), (34, 16, 18), t['n'])
        edge = min(t['fu'], 1 - t['fu'], t['fv'], 1 - t['fv'])
        if edge < 0.1:
            rgb = mix(rgb, LACQUER, 0.85)
        p = warped(t)
        rgb = mix(rgb, flesh_patch(t, p), overgrowth(t, -0.06))
    elif m == 'gourd':
        rgb = gourd(t)
    elif m == 'rope':
        rgb = rope(t)
    elif m == 'stopper':
        rgb = mix((96, 18, 36), (140, 34, 52), t['n'])
        rgb = mix(rgb, (52, 8, 20), crevice(t) * 0.6)
    elif m == 'spill':
        # The morphling that broke its jar: raw, wet, veined tissue, rooting into the throne and the body.
        p = warped(t)
        rgb = veins(flesh_patch(t, p), p, 1.0)
        rgb = mix(rgb, (60, 8, 14), crevice(t) * 0.35)
    else:
        raise ValueError(m)
    return rgb + (255,)


# ---------------------------------------------------------------- Java

EXTRA_IMPORTS = ('import com.mojang.blaze3d.vertex.VertexConsumer;\n'
                 'import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBurrowRules;\n'
                 'import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperMountControllerRules;\n'
                 'import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperMoveTimeline;\n'
                 'import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseOneAttack;\n'
                 'import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;\n'
                 'import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules;\n'
                 'import com.vincenthuto.hemomancy.common.util.HemoEasing;\n')
EXTRA_METHODS = Path(__file__).with_name('vesper_mount_anim.java').read_text(encoding='utf-8')


def wing_overrides(folded):
    out = {}
    for prefix, flared, fold in (('wing_fore', FORE_WING_FLARED, FORE_WING_FOLDED),
                                 ('wing_hind', HIND_WING_FLARED, HIND_WING_FOLDED)):
        if folded:
            delta = tuple(a - b for a, b in zip(fold, flared))
            out[prefix + '_r'] = delta
            out[prefix + '_l'] = mirror_rot(delta)
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = mount()
    size = layout(rig, ATLAS_WIDTH)
    (atlas,) = paint_atlases(rig, size, [paint])
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    atlas.save(TEXTURE_DIR / 'vesper_mount.png')
    JAVA.write_text(entity_java_source(rig, size, 'VesperTheCrownedRefusalEntity', 'vesper_mount.py',
                                       extra_methods=EXTRA_METHODS, extra_imports=EXTRA_IMPORTS,
                                       package='com.vincenthuto.hemomancy.client.model.entity.boss.endgame',
                                       entity_package='com.vincenthuto.hemomancy.common.entity.boss.endgame',
                                       plain_layer_location=True),
                    encoding='utf-8')
    print(f'vesper mount: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        flared = quads(rig, hidden=('wound_scute',))
        folded = quads(rig, hidden=('wound_scute',), overrides=wing_overrides(True))
        preview_sheet([(folded, atlas, 0.0, 0.15), (folded, atlas, math.pi / 2, 0.1),
                       (folded, atlas, math.pi * 0.75, 0.35), (folded, atlas, 0.0, 1.45),
                       (flared, atlas, 0.0, 0.2), (flared, atlas, math.pi / 2, 0.1),
                       (flared, atlas, math.pi * 0.8, 0.5), (flared, atlas, 0.0, 1.45)],
                      ROOT / 'build/vesper_mount_preview.png')


if __name__ == '__main__':
    main()

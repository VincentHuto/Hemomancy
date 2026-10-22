"""The Latent Infection: what the Draught of Still Mercy forces out of Annetta on the Unstained route.

Emits LatentAnnettaInfectionModel.java plus 2x painted textures:
  latent_annetta_infection.png       crimson fungus and vessel, ivory bone, black claws, red hair-rope
  latent_annetta_infection_glow.png  the chest core and infected nodes only, rendered emissive

    python tools/model_export/latent_annetta_infection.py            # write model + textures
    python tools/model_export/latent_annetta_infection.py --preview  # also write build/latent_annetta_infection_preview.png

Built at half size and rendered at 2x: a gaunt thing about five blocks tall, only vaguely humanoid. A pointed bone
hood arches over an empty face and runs down into a toothed maw opening on the chest around a red core; ropes of
red hair hang from under the hood (hers, taken with it). A ragged skirt of hanging vessel strands hides the serpentine
lower half, whose tail trails behind over the floor shedding roots and mycelium. The far-left arm is twisted vessels
ringed with vertebrae, coiled until it lashes out; the two skinny right arms end in long black hooked claws and
both grip one gnarled lance. Crimson and ivory: the ivory is what is left of her robes and bones. No copper, no blue.
"""
import argparse
from pathlib import Path
import math

from annetta_kit import (MODEL_DIR, ROOT, TEXTURE_DIR, Rig, add, bone, clamp, entity_java_source, h, layout, mix,
                         paint_atlases, preview_sheet, quads, smooth, transforms)

import annetta_kit

# Block texel density: it renders at 2x, so two texture pixels per model unit is already 16 px per block, like the
# mod's blocks and items. Pinned here so it never depends on another script's setting.
TEXEL_SCALE = 2
annetta_kit.SCALE = TEXEL_SCALE

JAVA = MODEL_DIR / 'LatentAnnettaInfectionModel.java'
ATLAS_WIDTH = 128
WAIST_Y = 4.0

# (length, height, width) per spearhead section from its socket to the point: a leaf that swells, then tapers.
SPEAR_HEAD = [(1.6, 1.8, 0.7), (1.8, 2.4, 0.62), (1.6, 2.1, 0.54), (1.4, 1.5, 0.46), (1.2, 0.9, 0.38),
              (1.0, 0.5, 0.3), (0.8, 0.25, 0.22)]

WAIST_Z = -1.5
FLOOR_Y = 24.0

# The serpent body is rooted where it meets the floor, not hung from the waist: the coils rise from that base to the
# waist, so bending them lifts, lunges and sways the whole upper body while the tail stays on the floor.
# (length, width, depth, lean) per coil from the base up; lean is degrees from upright toward the front, so the body
# leaves the floor heading forward and curls up into the waist: one smooth J from the tail tip to the torso.
COILS = [(3.4, 5.6, 4.6, 75), (4.0, 5.8, 4.8, 45), (4.6, 5.6, 4.6, 20), (5.0, 5.3, 4.3, 5), (5.0, 5.0, 4.0, 0)]

# (length, width, height, yRot) per tail segment trailing back from the base along the floor, each relative to the
# one before; widths carry straight on from the lowest coil and taper, and the tail curls round beside the body.
TAIL = [(5.0, 5.4, 4.2, 0), (5.0, 4.8, 3.7, 12), (4.8, 4.2, 3.2, 20), (4.6, 3.6, 2.8, 24), (4.4, 3.0, 2.4, 26),
        (4.0, 2.4, 2.0, 26), (3.6, 1.8, 1.5, 24), (3.2, 1.2, 1.0, 20)]


def claw_arm(r, name, pivot, rotation, forearm_rot):
    r.part(name, 'torso', pivot, rotation)
    r.cube(name, (-0.7, -0.4, -0.7, 1.4, 10.4, 1.4), 'sinew')
    r.cube(name, (-1.25, -1.0, -1.25, 2.5, 2.4, 2.5), 'bone')        # shoulder socket, sunk into the torso
    r.cube(name, (-1.0, 9.0, -1.0, 2.0, 2.0, 2.0), 'bone')           # elbow knuckle over the joint
    r.cube(name, (-0.35, 9.4, 0.6, 0.7, 0.7, 2.6), 'bone')           # a spur jutting back off the elbow
    fore = f'{name}_forearm'
    r.part(fore, name, (0, 10.0, 0), forearm_rot)
    r.cube(fore, (-0.65, -0.6, -0.65, 1.3, 9.6, 1.3), 'sinew')
    r.cube(fore, (-0.9, 3.0, -0.9, 1.8, 1.0, 1.8), 'bone')
    r.cube(fore, (-0.85, 8.2, -0.85, 1.7, 1.4, 1.7), 'bone')        # wrist knuckle
    hand = f'{name}_hand'
    r.part(hand, fore, (0, 9.0, 0))
    r.cube(hand, (-1.2, -0.3, -1.0, 2.4, 2.1, 2.0), 'bone')
    for i, x in enumerate((-0.8, 0.0, 0.8)):
        finger = f'{name}_finger_{i}'
        r.part(finger, hand, (x, 1.6, -0.6), (-35, 0, (i - 1) * 12))
        r.cube(finger, (-0.45, 0.0, -0.45, 0.9, 5.0, 0.9), 'claw', tip=False)
        tip = f'{finger}_tip'
        r.part(tip, finger, (0, 4.8, 0), (-40, 0, 0))
        r.cube(tip, (-0.32, 0.0, -0.32, 0.64, 4.5, 0.64), 'claw', tip=True)
    thumb = f'{name}_thumb'
    r.part(thumb, hand, (0.0, 1.4, 0.9), (55, 0, 0))
    r.cube(thumb, (-0.35, 0.0, -0.35, 0.7, 3.8, 0.7), 'claw', tip=True)
    return hand


def spear_rotation(r, rear, front):
    """Degrees that lay the spear's -z axis from the rear grip through the front grip."""
    import numpy as np
    m = transforms(r)
    a = (m[rear] @ np.array([0, 1, 0, 1]))[:3]
    b = (m[front] @ np.array([0, 1, 0, 1]))[:3]
    forward = m[rear][:3, :3].T @ ((b - a) / np.linalg.norm(b - a))
    back = -forward
    return math.degrees(-math.asin(back[1])), math.degrees(math.atan2(back[0], back[2])), 0.0


def infection():
    r = Rig('latent_annetta_infection', 'LatentAnnettaInfectionModel', 'latent_annetta_infection')

    # Where the body meets the floor: a swollen knot the tail trails back from and the coils rise out of. Its pivot is
    # placed so the coils end at the waist (WAIST_Y, WAIST_Z).
    rise_y = sum(length * math.cos(math.radians(lean)) for length, _, _, lean in COILS)
    rise_z = sum(length * math.sin(math.radians(lean)) for length, _, _, lean in COILS)
    base_y = WAIST_Y + rise_y
    floor = FLOOR_Y - base_y
    r.part('coil_base', 'root', (0, base_y, WAIST_Z + rise_z + 1.6))
    r.cube('coil_base', (-2.9, floor - 4.6, -3.0, 5.8, 4.6, 6.0), 'flesh', seg=0, coil='base')

    # The tail trails back from the base over the floor and curls round beside the body, every joint overlapped so
    # the bends never open a seam.
    parent, pivot = 'coil_base', (0, 0, 1.8)
    for i, (length, w, hgt, yr) in enumerate(TAIL):
        name = f'tail_{i}'
        r.part(name, parent, pivot, (0, yr, 0))
        lap = w * 0.3
        r.cube(name, (-w / 2, floor - hgt, -lap, w, hgt, length + lap), 'flesh', seg=i, coil='tail')
        if i % 2 == 1:
            for k, side in enumerate((-1, 1)):
                strand = f'root_strand_{i}_{k}'
                r.part(strand, name, (side * w * 0.45, floor - 0.6, length * 0.5), (0, side * 60, side * 70))
                r.cube(strand, (-0.2, 0.0, -0.2, 0.4, 3.0 + 1.5 * h(i, k), 0.4), 'root')
                r.cube(strand, (-0.12, 2.6, -0.12, 0.24, 2.5, 0.24), 'mycelium')
        parent, pivot = name, (0, 0, length)

    # The coils rise from the base, each leaning less than the one below, into the waist. Every coil runs a little
    # past both its ends so the curve stays one body however far it bends.
    parent, pivot, lean_before = 'coil_base', (0, 0, -1.6), 0.0
    for i, (length, w, d, lean) in enumerate(COILS):
        name = f'coil_{i}'
        r.part(name, parent, pivot, (lean - lean_before, 0, 0))
        r.cube(name, (-w / 2, -length - d * 0.25, -d / 2, w, length + d * 0.55, d), 'flesh', seg=i, coil='rise')
        parent, pivot, lean_before = name, (0, -length, 0), lean

    # The waist: a knot where the coils end, hung with a ragged skirt of vessels and robe rags that flare out over the
    # coils instead of hiding them.
    r.part('skirt', parent, pivot, (-lean_before, 0, 0))
    r.cube('skirt', (-2.9, -1.4, -2.3, 5.8, 3.6, 4.6), 'flesh')
    for i in range(14):
        angle = math.radians(i * 360 / 14 + 8)
        dx, dz = math.sin(angle), math.cos(angle)
        name = f'skirt_strand_{i}'
        flare = 9 + 12 * max(0.0, dz)
        r.part(name, 'skirt', (dx * 2.8, 0.6, dz * 2.2), (dz * flare, 0, -dx * flare))
        length = 8.5 + 3.0 * h(i, 5) + 5.0 * max(0.0, -dz)
        r.cube(name, (-0.8, -0.6, -0.4, 1.6, length + 0.6, 0.8), 'strand', seg=i)
        r.cube(name, (-0.35, length, -0.3, 0.7, 2.0 + 2.0 * h(i, 6), 0.6), 'strand', seg=i)
        if i % 3 == 0:
            r.cube(name, (-0.3, length - 0.5, -0.3, 0.6, 3.0, 0.6), 'root')

    # Gaunt torso, hunched forward, ribbed, with the maw opening down its front.
    r.part('torso', 'skirt', (0, 0, 0), (24, 0, 0))
    r.cube('torso', (-2.5, -9.0, -1.8, 5.0, 9.5, 3.6), 'flesh')
    r.cube('torso', (-3.8, -13.0, -2.4, 7.6, 4.5, 4.8), 'flesh')
    r.cube('torso', (-2.2, -8.0, -2.1, 4.4, 5.0, 0.6), 'core')
    for i in range(5):
        y = -12.0 + i * 1.7
        r.cube('torso', (-2.9, y, -2.6, 1.0, 1.1, 0.9), 'tooth')
        r.cube('torso', (1.9, y, -2.6, 1.0, 1.1, 0.9), 'tooth')
    for i in range(3):
        r.cube('torso', (-2.7, -4.5 + i * 1.4, 1.6, 5.4, 0.7, 0.7), 'bone')
    for side, sign in (('left', 1), ('right', -1)):
        name = f'{side}_pauldron'
        r.part(name, 'torso', (sign * 4.0, -12.5, 0), (0, 0, sign * -20))
        r.cube(name, (-1.6, -1.2, -1.8, 3.2, 1.6, 3.6), 'bone')
        for k in range(4):
            r.cube(name, (-1.9 + k * 1.0, -2.4, -0.3, 0.6, 1.4, 0.6), 'bone')

    # The hood: a pointed bone cowl over an empty face, its inner rim lined with teeth.
    r.part('head_mass', 'torso', (0, -13.0, -1.0), (-10, 0, 0))
    r.cube('head_mass', (-2.0, -5.0, -1.5, 4.0, 5.0, 3.5), 'void')
    for side, sign in (('left', 1), ('right', -1)):
        cowl = f'{side}_cowl'
        r.part(cowl, 'head_mass', (sign * 2.0, 0.5, -1.8), (0, 0, sign * 16))
        r.cube(cowl, (-0.6, -9.0, 0.0, 1.2, 9.5, 4.5), 'hood')
        for k in range(5):
            r.cube(cowl, (-sign * 0.9 - 0.4, -7.5 + k * 1.6, -0.3, 0.8, 0.9, 0.8), 'tooth')
    # The cowl's crown arches forward over the face and hooks down at its point.
    r.part('crest', 'head_mass', (0, -8.0, 1.5), (-55, 0, 0))
    r.cube('crest', (-2.6, -4.5, -1.5, 5.2, 5.0, 4.0), 'hood')
    r.part('crest_tip', 'crest', (0, -4.5, -0.5), (-40, 0, 0))
    r.cube('crest_tip', (-1.8, -3.5, -1.0, 3.6, 3.8, 3.0), 'hood')
    r.part('crest_hook', 'crest_tip', (0, -3.4, -0.4), (-45, 0, 0))
    r.cube('crest_hook', (-0.9, -2.6, -0.6, 1.8, 2.8, 1.8), 'hood')
    for k in range(4):
        r.cube('crest', (-2.2 + k * 1.3, 0.2, -1.9, 0.8, 1.2, 0.8), 'tooth')
    for i in range(8):
        x = -2.4 + (i % 4) * 1.6 + (0.4 if i >= 4 else 0)
        name = f'hair_strand_{i}'
        r.part(name, 'head_mass', (x, -3.0 - (i % 3) * 1.2, 0.5 if i < 4 else -1.6), (0, 0, (x / 2.4) * 8))
        r.cube(name, (-0.4, 0.0, -0.4, 0.8, 10.0 + 4.0 * h(i, 8), 0.8), 'hair')

    # Far-left arm: twisted vessels ringed with vertebrae. It hangs from the shoulder and hooks forward and in at
    # its end like a held lash, never back over the shoulder. Each segment overlaps the one above and its vertebra
    # sits over the joint, so the arm bends as one cord.
    parent, pivot = 'torso', (4.8, -12.0, 0)
    for i in range(8):
        name = f'whip_arm_{i}'
        r.part(name, parent, pivot, (-11, 0, 3) if i else (-28, 0, -20))
        thick = 2.0 - i * 0.15
        r.cube(name, (-thick / 2, -0.7, -thick / 2, thick, 5.3, thick), 'vessel_bundle')
        r.cube(name, (-thick / 2 - 0.45, -0.45, -thick / 2 - 0.45, thick + 0.9, 0.9, thick + 0.9), 'bone')
        parent, pivot = name, (0, 4.6, 0)

    # Two skinny right arms with hooked black claws; the upper hand is the rear grip on the lance.
    upper = claw_arm(r, 'upper_claw_arm', (-4.6, -12.0, -0.5), (-15, 0, 16), (-75, 0, 0))
    lower = claw_arm(r, 'lower_claw_arm', (-3.2, -7.5, -1.0), (-78, 0, 12), (-8, 0, 0))
    r.part('spear', upper, (0, 1.0, 0), spear_rotation(r, upper, lower))
    # A gnarled shaft of hardened blood, bound with vessels at the grips and capped with a bone spike behind.
    r.cube('spear', (-0.55, -0.55, -16.0, 1.1, 1.1, 30.0), 'spear_shaft')
    r.cube('spear', (-0.9, -0.9, -6.0, 1.8, 1.8, 3.0), 'vessel_bundle')
    r.cube('spear', (-0.85, -0.85, 4.0, 1.7, 1.7, 2.5), 'vessel_bundle')
    r.cube('spear', (-0.75, -0.75, 13.5, 1.5, 1.5, 1.0), 'bone')
    r.cube('spear', (-0.4, -0.4, 14.5, 0.8, 0.8, 1.6), 'bone')
    r.cube('spear', (-0.2, -0.2, 16.1, 0.4, 0.4, 1.2), 'bone')
    # A bone socket, then a long leaf-shaped head that narrows to a needle point, with a raised midrib.
    r.cube('spear', (-0.85, -0.85, -17.6, 1.7, 1.7, 1.6), 'bone')
    r.cube('spear', (-0.65, -0.65, -18.4, 1.3, 1.3, 0.8), 'bone')
    for k, (length, height, width) in enumerate(SPEAR_HEAD):
        z = -18.4 - sum(seg[0] for seg in SPEAR_HEAD[:k + 1])
        r.cube('spear', (-width / 2, -height / 2, z, width, height, length), 'spear_blade', seg=k)
    r.cube('spear', (-0.42, -0.35, -25.4, 0.84, 0.7, 7.0), 'spear_rib')
    # Two barbs at the base of the head, hooked back toward the shaft.
    for name, sign in (('spear_barb_top', -1), ('spear_barb_bottom', 1)):
        r.part(name, 'spear', (0, sign * 0.95, -19.9), (-sign * 34, 0, 0))
        r.cube(name, (-0.22, -0.22, 0.0, 0.44, 0.44, 2.6), 'spear_blade', seg=len(SPEAR_HEAD))
    return r


# ---------------------------------------------------------------- paint
#
# Clean material zones rather than speckle: every material reads from a few coherent features (fibres, a wet
# gradient, a bevelled edge), and the ivory and ichor come in deliberate shapes, not scattered noise.

def bevel(rgb, t, light=10, dark=16):
    """A one-pixel edge on every face: lit along the top, shadowed down the other sides, so boxes read cleanly."""
    if t['fw'] < 3 or t['fh'] < 3:
        return rgb
    if t['v'] == 0:
        return add(rgb, light)
    if t['u'] == 0 or t['u'] == t['fw'] - 1 or t['v'] == t['fh'] - 1:
        return add(rgb, -dark)
    return rgb


def wetness(t):
    """0 high on the body, 1 down at the floor where the blood pools and soaks."""
    return clamp((t['gy'] + 10.0) / 34.0)


def crimson(t):
    """Fibrous crimson flesh: wavy vertical striations, darker and wetter towards the floor."""
    tone = smooth(t['gx'], t['gy'], t['gz'], 3.0, 2)
    rgb = mix((140, 24, 36), (70, 9, 20), tone * 0.55 + wetness(t) * 0.45)
    if t['side']:
        lane = (t['u'] + round(smooth(t['gx'], t['gy'], t['gz'], 2.0, 4) * 3)) % 3
        rgb = add(rgb, (8, 0, -9)[lane])
    return add(rgb, (t['n'] - 0.5) * 6)


def pustule(t):
    """Swollen infected nodes: 2 is the bright core, 1 the dark crust round it, 0 none."""
    k = smooth(t['gx'], t['gy'], t['gz'], 1.7, 21)
    return 2 if k > 0.875 else 1 if k > 0.845 else 0


def ichor_core(t):
    return mix((236, 172, 62), (190, 108, 34), smooth(t['gx'] * 2, t['gy'] * 2, t['gz'] * 2, 1.0, 22))


def with_pustules(rgb, t):
    kind = pustule(t)
    if kind == 2:
        return ichor_core(t)
    if kind == 1:
        return (92, 38, 18)
    return rgb


def rag(t):
    """Ivory robe rag: a few large torn patches, each edged with blood soaked into the cloth."""
    k = smooth(t['gx'] * 1.1, t['gy'] * 0.8, t['gz'] * 1.1, 3.0, 13)
    return 2 if k > 0.7 else 1 if k > 0.665 else 0


def robe(t):
    rgb = add((220, 208, 184), (smooth(t['gx'], t['gy'], t['gz'], 1.5, 17) - 0.5) * 18)
    if t['side'] and t['u'] % 5 == 2:
        rgb = add(rgb, -14)           # weave of the cloth
    return rgb


def soak(rgb, t, amount):
    """Blood wicking up into ivory: a rough gradient, with drips running further."""
    edge = amount + 0.18 * (smooth(t['gx'] * 3, t['gy'], t['gz'] * 3, 1.0, 51) - 0.5)
    if edge > 0.55:
        return mix(rgb, (110, 14, 26), clamp((edge - 0.55) / 0.2) * 0.9)
    return rgb


def strand_kind(i):
    """Skirt strands: every third is a rag of her robe, the rest are vessels."""
    return 'rag' if i % 3 == 1 else 'vessel'


def paint(cube, t):
    m = cube['material']
    if m == 'flesh':
        patch = rag(t) if cube.get('seg') is None else 0
        if patch == 2:
            rgb = soak(robe(t), t, wetness(t) * 1.2)
        elif patch == 1:
            rgb = (96, 14, 24)        # the torn, blood-soaked hem of the rag
        else:
            rgb = with_pustules(crimson(t), t)
            if cube.get('seg') is not None:
                rgb = tail(rgb, cube, t)
        return bevel(rgb, t) + (255,)
    if m == 'strand':
        i = cube['seg']
        if strand_kind(i) == 'rag':
            rgb = soak(robe(t), t, t['fv'] * 0.9 + 0.1)
            if t['side'] and t['fv'] > 0.85 and t['u'] % 2 == 0:
                rgb = (60, 8, 16)     # frayed threads, black with blood
        else:
            rgb = mix((116, 16, 30), (46, 5, 13), t['fv'] * 0.8 + 0.2 * smooth(0, i, 0, 1.0, 7))
            if t['side'] and t['u'] % 3 == 1:
                rgb = add(rgb, 18)    # the wet line down each vessel
            elif t['side'] and t['u'] % 3 == 0:
                rgb = add(rgb, -16)
            if t['fv'] > 0.92:
                rgb = (34, 3, 8)      # dark drip end
            rgb = with_pustules(rgb, t)
        return rgb + (255,)
    if m == 'core':
        k = smooth(t['gx'] * 3, t['gy'] * 3, t['gz'], 1.0, 31)
        rim = min(t['fu'], 1 - t['fu'], t['fv'], 1 - t['fv'])
        rgb = mix((230, 50, 52), (120, 8, 22), k * 0.6 + (0.4 if rim < 0.15 else 0.0))
        return rgb + (255,)
    if m == 'void':
        return mix((22, 5, 9), (8, 1, 4), t['fv']) + (255,)
    if m == 'hood':
        rgb = add((222, 210, 184), (smooth(t['gx'], t['gy'], t['gz'], 2.0, 41) - 0.5) * 22)
        if t['side'] and t['u'] % 4 == 1 and t['fv'] > 0.25:
            rgb = add(rgb, -12)       # growth ridges running down the cowl
        rgb = soak(rgb, t, (t['fv'] * 0.75 if t['side'] else 0.2) + 0.08)
        return bevel(rgb, t, light=8, dark=24) + (255,)
    if m == 'tooth':
        rgb = (238, 230, 212) if t['fv'] < 0.7 else (176, 70, 64) if t['fv'] < 0.85 else (110, 14, 26)
        return rgb + (255,)
    if m == 'hair':
        k = (t['u'] + round(t['gy'] * 1.5)) % 4
        rgb = ((132, 46, 30), (104, 32, 22), (156, 62, 40), (118, 40, 26))[k]
        if t['fv'] > 0.82:
            rgb = mix(rgb, (60, 8, 14), (t['fv'] - 0.82) / 0.18)   # matted with blood at the ends
        return rgb + (255,)
    if m == 'vessel_bundle':
        twist = (t['u'] + t['v'] // 2) % 4
        rgb = ((132, 22, 34), (96, 12, 24), (154, 40, 46), (58, 7, 15))[twist]
        if twist == 2 and t['u'] % 8 < 2:
            rgb = (140, 62, 40)       # a hair-rope twisted in with them
        return with_pustules(rgb, t) + (255,)
    if m == 'sinew':
        rgb = crimson(t)
        if t['side'] and t['u'] % 2 == 0:
            rgb = add(rgb, -10)       # tight cords of sinew
        return bevel(rgb, t, light=8, dark=10) + (255,)
    if m == 'claw':
        rgb = mix((58, 44, 48), (16, 12, 14), t['fv'] if cube.get('tip') else t['fv'] * 0.5)
        if t['side'] and t['u'] == 0:
            rgb = add(rgb, 30)        # glossy edge
        return rgb + (255,)
    if m == 'bone':
        rgb = soak(bone(t), t, wetness(t) * 0.8)
        return bevel(rgb, t, light=10, dark=26) + (255,)
    if m == 'root':
        return mix((96, 58, 46), (54, 30, 26), t['fv']) + (255,)
    if m == 'mycelium':
        return add((228, 222, 206), -t['fv'] * 30) + (255,)
    if m == 'spear_shaft':
        rgb = mix((88, 12, 20), (48, 5, 11), smooth(t['gx'], t['gy'], t['gz'], 1.2, 15))
        if abs(math.sin(t['lz'] * 0.9 + t['lx'] * 3.0)) < 0.1:
            rgb = ichor_core(t)       # gnarled seams of ichor
        return rgb + (255,)
    if m == 'spear_blade':
        k = cube.get('seg', 0) / len(SPEAR_HEAD)
        rgb = mix((150, 20, 30), (104, 10, 20), k)
        if t['face'] in ('up', 'down'):
            rgb = (226, 98, 84)       # the honed edge, top and bottom
        elif t['side'] and (t['fv'] < 0.12 or t['fv'] > 0.88):
            rgb = (196, 58, 54)       # bevel running to the edge
        elif t['side'] and t['face'] in ('north', 'south'):
            rgb = add(rgb, -12)
        return rgb + (255,)
    if m == 'spear_rib':
        rgb = mix((92, 8, 18), (60, 4, 12), t['fv'])
        if abs(math.sin(t['lz'] * 1.3)) < 0.18:
            rgb = ichor_core(t)       # the infection threading down the midrib
        return rgb + (255,)
    raise ValueError('unpainted material ' + m)


def tail(rgb, cube, t):
    """The serpent body: a dark dorsal ridge, a paler belly and creased rings between the coils. The rising coils run
    along their y axis with their back to +z; the base and tail lie along z with their back up."""
    rising = cube.get('coil') == 'rise'
    back, belly = ('south', 'north') if rising else ('up', 'down')
    if t['face'] == back:
        rgb = mix(rgb, (52, 6, 14), 0.5)
        if abs(t['fu'] - 0.5) < 0.12:
            rgb = (34, 4, 10)
    elif t['face'] == belly:
        rgb = mix(rgb, (168, 70, 70), 0.35)
        if rising and (t['ly'] % 1.5) < 0.3:
            rgb = add(rgb, -18)       # belly scutes up the front of the rising coils
    if ((t['ly'] if rising else t['lz']) % 2.5) < 0.45:
        rgb = add(rgb, -24)
    return rgb


def glow(cube, t):
    m = cube['material']
    if m == 'core':
        return (255, 80, 72, 230)
    if m in ('flesh', 'strand') and pustule(t) == 2:
        if m == 'strand' and strand_kind(cube['seg']) == 'rag':
            return None
        if m == 'flesh' and cube.get('seg') is None and rag(t):
            return None
        return ichor_core(t) + (150,)
    if m == 'spear_shaft' and abs(math.sin(t['lz'] * 0.9 + t['lx'] * 3.0)) < 0.1:
        return ichor_core(t) + (90,)
    if m == 'spear_rib' and abs(math.sin(t['lz'] * 1.3)) < 0.18:
        return ichor_core(t) + (140,)
    return None


# ---------------------------------------------------------------- Java

EXTRA_FIELDS = '''

	private final ModelPart[] coils;
	private final ModelPart[] tail;
	private final ModelPart[] whipArm;
	private final ModelPart[] hair;
	private final ModelPart[] skirtStrands;'''

EXTRA_CTOR = '''
		this.coils = new ModelPart[]{this.coil0, this.coil1, this.coil2, this.coil3, this.coil4};
		this.tail = new ModelPart[]{this.tail0, this.tail1, this.tail2, this.tail3, this.tail4, this.tail5,
				this.tail6, this.tail7};
		this.whipArm = new ModelPart[]{this.whipArm0, this.whipArm1, this.whipArm2, this.whipArm3, this.whipArm4,
				this.whipArm5, this.whipArm6, this.whipArm7};
		this.hair = new ModelPart[]{this.hairStrand0, this.hairStrand1, this.hairStrand2, this.hairStrand3,
				this.hairStrand4, this.hairStrand5, this.hairStrand6, this.hairStrand7};
		this.skirtStrands = new ModelPart[]{this.skirtStrand0, this.skirtStrand1, this.skirtStrand2, this.skirtStrand3,
				this.skirtStrand4, this.skirtStrand5, this.skirtStrand6, this.skirtStrand7, this.skirtStrand8,
				this.skirtStrand9, this.skirtStrand10, this.skirtStrand11, this.skirtStrand12, this.skirtStrand13};'''

# The animation timelines live as plain Java beside this script so they read and diff as Java.
EXTRA_METHODS = (Path(__file__).with_name('latent_annetta_infection_anim.java')).read_text(encoding='utf-8')

EXTRA_IMPORTS = """import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules;
"""


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = infection()
    size = layout(rig, ATLAS_WIDTH)
    base, glow_image = paint_atlases(rig, size, [paint, glow])
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURE_DIR / 'latent_annetta_infection.png')
    glow_image.save(TEXTURE_DIR / 'latent_annetta_infection_glow.png')
    JAVA.write_text(entity_java_source(rig, size, 'LatentAnnettaInfectionEntity', 'latent_annetta_infection.py',
                                       extra_fields=EXTRA_FIELDS, extra_ctor=EXTRA_CTOR,
                                       extra_methods=EXTRA_METHODS, extra_imports=EXTRA_IMPORTS), encoding='utf-8')
    print(f'latent infection: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        q = quads(rig)
        preview_sheet([(q, base, 0.0, 0.1), (q, base, math.pi / 2, 0.1), (q, base, 0.6, 0.15),
                       (q, base, math.pi * 0.8, 0.25)], ROOT / 'build/latent_annetta_infection_preview.png')


if __name__ == '__main__':
    main()

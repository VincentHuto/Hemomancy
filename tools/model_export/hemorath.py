"""Hemorath: skeletal Saint of Iron and Death, based on the supplied concept sheet.

python tools/model_export/hemorath.py --preview

Authors the Java rig, embedded-texture Blockbench project, fitted base/overload
atlases, and review images. Geometry uses the shared quarter-unit grid throughout.
"""
import argparse
import base64
import json
import math
import subprocess

from PIL import Image, ImageDraw
import annetta_kit as kit
from annetta_kit import Rig, ROOT, ASSETS, layout, paint_atlases, entity_java_source, quads, add, mix

kit.SCALE = 2
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/hemorath/HemorathModel.java'
TEXTURES = ASSETS / 'textures/entity/boss/hemorath'
BBMODEL = ASSETS / 'models/entity/bbmodel/bosses/HemorathModel.bbmodel'


def snap(value):
    return round(value * 4) / 4


def euler(matrix):
    return tuple(math.degrees(a) for a in (math.atan2(matrix[2, 1], matrix[2, 2]),
                 math.asin(max(-1, min(1, -matrix[2, 0]))), math.atan2(matrix[1, 0], matrix[0, 0])))


def mirrored_box(r, part, sign, box, material, **extra):
    x, y, z, w, h, d = box
    r.cube(part, (x if sign > 0 else -x - w, y, z, w, h, d), material, **extra)


def chain(r, name, parent, pivot, links, pendant=False):
    r.part(name, parent, pivot)
    # Four bars leave real open space in each interlocking link.
    for i in range(links):
        link = f'{name}_link_{i}'
        r.part(link, name, (0, i * 1.5, 0), (0, 90 if i % 2 else 0, 0))
        for box in ((-.75, 0, -.25, .25, 1.75, .5), (.5, 0, -.25, .25, 1.75, .5),
                    (-.5, 0, -.25, 1, .25, .5), (-.5, 1.5, -.25, 1, .25, .5)):
            r.cube(link, box, 'iron')
    if pendant:
        # its own part at the chain's end, so the anim can carry it along a bent chain
        r.part(f'{name}_pendant', name, (0, links * 1.5, 0))
        r.cube(f'{name}_pendant', (-.25, 0, -.25, .5, 2.5, .5), 'iron')
        r.cube(f'{name}_pendant', (-1, .5, -.25, 2, .5, .5), 'iron')


def arm(r, side, sign):
    name = f'{side}_arm'
    r.part(name, 'chest', (sign * 4, -6, 0), (-8 if sign > 0 else -18, 0, -35 if sign > 0 else 16))
    r.cube(name, (-1, -1, -1, 2, 2, 2), 'bone')
    r.cube(name, (-.5, .5, -.5, 1, 7.5, 1), 'bone')
    r.cube(name, (-.75, 7, -.75, 1.5, 1.5, 1.5), 'bone')
    fore = f'{side}_forearm'
    r.part(fore, name, (0, 7.5, 0), (0 if sign > 0 else -45, 0, -105 if sign > 0 else -10))
    r.cube(fore, (-.75, -.5, -.5, .5, 8, 1), 'bone')
    r.cube(fore, (.25, -.5, -.5, .5, 8, 1), 'bone')
    r.cube(fore, (-1, 6.75, -.75, 2, 1, 1.5), 'iron')
    hand = f'{side}_hand'
    r.part(hand, fore, (0, 7.5, 0), (-5 if sign > 0 else -16, 0, 0))
    r.cube(hand, (-1, -.5, -.5, 2, 2.5, 1), 'bone')
    for i, length in enumerate((2.5, 3.25, 3.5, 2.75)):
        finger = f'{side}_finger_{i}'
        r.part(finger, hand, (-1 + i * .5, 1.5, -.25), (-75 if sign > 0 else -10, 0, (i - 1) * 5))
        r.cube(finger, (-.25, -.25, -.25, .5, 1.5 if sign > 0 else length, .5), 'bone')
        tip = f'{finger}_tip'
        r.part(tip, finger, (0, 1.25 if sign > 0 else length - .25, 0), (-90 if sign > 0 else -24, 0, 0))
        r.cube(tip, (-.25, 0, -.25, .5, 1.25, .5), 'bone')
        if sign < 0:
            # the claw: an iron hook on every fingertip
            r.cube(tip, (-.25, 1.25, -.5, .5, .25, .5), 'iron_tip')
    r.part(f'{side}_thumb', hand, (sign * 1, 0, -.25), (-55, 0, -sign * 40))
    r.cube(f'{side}_thumb', (-.25, 0, -.25, .5, 2, .5), 'bone')
    # vessels along the humerus, shown from the Filling state (the model scales them 0 to 1.15)
    vessel = f'{side}_arm_vessel'
    r.part(vessel, name, (sign * .75, 3.5, -.25))
    r.cube(vessel, (-.25 if sign > 0 else -.5, -2, -.5, .75, 2.5, .75), 'vessel')
    r.cube(vessel, (-.25 if sign > 0 else -.5, 1, -.25, .75, 2, .75), 'vessel')


def staff(r):
    # The staff is held by the articulated hand; its rotation counters that whole chain.
    world = kit.transforms(r)
    hand = world['left_hand']
    pivot = (0, 1, -1)
    origin_y = (hand[:3, :3] @ pivot + hand[:3, 3])[1]
    bottom = snap(24 - origin_y)
    r.part('staff', 'left_hand', pivot, euler(hand[:3, :3].T))
    r.cube('staff', (-.5, -15.5, -.5, 1, bottom + 15.5, 1), 'iron')
    for y in (-14, -9, -4, 3, 9, bottom - 1):
        r.cube('staff', (-.75, y, -.75, 1.5, .75, 1.5), 'iron')
    r.cube('staff', (-1, bottom - .75, -1, 2, .75, 2), 'iron')
    r.part('staff_crown', 'staff', (0, -13, 0))
    r.cube('staff_crown', (-4.5, -.5, -.5, 9, .75, 1), 'iron')
    r.cube('staff_crown', (-3.5, -3.5, -.5, 7, .75, 1), 'iron')
    for x, height in ((-3.5, 4.5), (0, 6.5), (3.5, 5.5)):
        r.cube('staff_crown', (x - .25, -height, -.25, .5, height + .75, .5), 'iron')
        r.cube('staff_crown', (x - 1, -height + 1, -.25, 2, .5, .5), 'iron')
    chain(r, 'staff_chain_left', 'staff_crown', (-4, .25, 0), 5, True)
    chain(r, 'staff_chain_right', 'staff_crown', (4, .25, 0), 7, True)
    chain(r, 'staff_chain_center', 'staff_crown', (1.5, -2.5, -.25), 4)


# The nine spike-patch sites in eruption order (HemorathIronRules.SITES): parent and offset.
PATCH_SITES = (('right_forearm', 0, 5, -.75), ('right_arm', 0, 5, -1), ('left_rib_2', 2, 0, -2.5),
               ('right_rib_5', -2, 0, -2.5), ('left_forearm', 0, 2, -.75), ('left_arm', 0, 3, -1),
               ('right_shin', 0, 4, -.75), ('left_shin', 0, 2, -.75), ('head', 2, -3, -1.75))


def vessels(r):
    """The vessel in three nested stages inside the ribcage, each at its true size (the model only scales a shown stage
    between 0.8 and 1.15): dried cords always, four lobes between the ribs, then the heart mass with its aorta."""
    r.part('vessel_cords', 'chest', (0, -1.5, .25))
    for x in (-2.25, -1.5, -.75, .25, 1, 1.75):
        r.cube('vessel_cords', (x, -6, -.25, .5, 12, .5), 'dried_vessel')
    r.part('vessel_lobes', 'chest', (0, -4.5, -.75))
    for sign in (1, -1):
        for y in (-2, 1.5):
            r.cube('vessel_lobes', (.5 if sign > 0 else -2.5, y, -1, 2, 2.5, 1.5), 'vessel')
    # the heart mass fills the ribcage's width and presses on the front ribs once shown
    r.part('vessel_mass', 'chest', (0, -3.5, -.5))
    r.cube('vessel_mass', (-2, -2.25, -1.5, 4, 4.5, 3), 'vessel')
    r.cube('vessel_mass', (-1.5, -3.75, -1, 3, 1.5, 2), 'vessel')
    r.cube('vessel_mass', (-.5, -6.5, -.5, 1, 3, 1), 'vessel')
    r.cube('vessel_mass', (-2.5, 2, -1, 1.5, 1.5, 1.5), 'vessel')
    r.cube('vessel_mass', (1, 2, -1, 1.5, 1.5, 1.5), 'vessel')


def site_points(r):
    """Each patch site in the painter's unrotated part space, for the rust stain the bone carries under it."""
    world = kit.part_world(r)
    return [tuple(a + b for a, b in zip(world[parent], (x, y, z))) for parent, x, y, z in PATCH_SITES]


def saint():
    r = Rig('hemorath', 'HemorathModel', 'hemorath')
    r.part('hips', pivot=(0, 11, 0))
    r.cube('hips', (-2.5, -.5, -1.5, 5, 1.5, 3), 'bone')
    r.cube('hips', (-.5, -6, .75, 1, 6.5, 1), 'bone')
    r.part('chest', 'hips', (0, -6, 0), (18, 0, -4))
    r.cube('chest', (-.5, -8, 1.25, 1, 14, 1.25), 'bone')
    # There is no solid torso cube behind these ribs: the hollow chest remains visible from every side.
    r.part('ribcage', 'chest')
    r.cube('ribcage', (-.5, -7, -2.25, 1, 8.5, .75), 'bone')
    for side, sign in (('left', 1), ('right', -1)):
        for i in range(7):
            rib = f'{side}_rib_{i}'
            width = snap(3.75 - i * .125)
            r.part(rib, 'ribcage', (0, -6.5 + i * 1.25, 0), (0, 0, sign * (5 + i)))
            for box in ((.5, 0, 1.25, width - .5, .5, .75),
                        (width - .5, 0, -1.5, .75, .5, 3.5),
                        (.5, 0, -2.25, width - .5, .5, .75)):
                mirrored_box(r, rib, sign, box, 'bone')
        r.part(f'{side}_clavicle', 'chest', (0, -7.25, 0), (0, 0, -sign * 9))
        mirrored_box(r, f'{side}_clavicle', sign, (.5, -.25, -.75, 4, .75, 1), 'bone')
        arm(r, side, sign)
        for i, (z, count) in enumerate(((1, 9), (-1, 11))):
            name = f'{side}_shoulder_chain' + ('' if i == 0 else '_front')
            chain(r, name, 'chest', (sign * (4.5 + i * .5), -6.5, z), count, i == 0)

    # A bowed, faceless skull under a cap of bone and a curtain of dried blood and ivory cords.
    r.cube('chest', (-.75, -9.5, -.75, 1.5, 2.5, 1.5), 'bone')
    r.part('head', 'chest', (0, -9, -1), (12, 0, 6))
    r.cube('head', (-2.5, -4.5, -1.5, 5, 4.5, 3.5), 'bone')
    r.cube('head', (-2, -3.75, -2, 4, 3.5, .5), 'void')
    r.cube('head', (-2.75, -5, -2, 5.5, 1, 4.25), 'bone')
    r.cube('head', (-2, -5.5, -1.5, 4, .5, 3.5), 'bone')
    for i in range(5):
        r.part(f'brow_bone_{i}', 'head', (-2 + i, -4.5, -2.25), (-10, 0, (i - 2) * 5))
        r.cube(f'brow_bone_{i}', (-.25, 0, -.25, .5, 1.5, .5), 'bone')
    for side, sign in (('left', 1), ('right', -1)):
        r.part(f'{side}_cowl', 'head', (sign * 2.25, -3.5, -.5), (0, 0, -sign * 12))
        r.cube(f'{side}_cowl', (-.5, -1, -1.5, 1, 6, 3), 'bone')
    # the face cords part over the void (the card, the Gorge): two curtains under one hinge
    r.part('face_cords', 'head')
    r.part('face_cords_left', 'face_cords', (0, -3.75, -2.25))
    r.part('face_cords_right', 'face_cords', (0, -3.75, -2.25))
    for i in range(9):
        name = f'face_cord_{i}'
        curtain = 'face_cords_left' if i < 5 else 'face_cords_right'
        r.part(name, curtain, (-2 + i * .5, (i % 3) * .25, -(i % 2) * .25),
               (0, 0, (i - 4) * 1.5))
        length = 4.5 + (i * 2 % 5)
        r.cube(name, (-.25, 0, -.25, .25, length, .25), 'blood' if i % 2 else 'bone')
        r.cube(name, (-.25, length, -.25, .25, 1.25, .25), 'blood')

    r.part('halo', 'chest', (0, -11.5, 3), (0, 0, 4))
    for i in range(16):
        angle = math.radians(i * 22.5)
        name = f'halo_segment_{i}'
        r.part(name, 'halo', (snap(math.sin(angle) * 6.75), snap(-math.cos(angle) * 6.75), 0), (0, 0, i * 22.5))
        r.cube(name, (-1.5, -.25, -.25, 3, .5, .5), 'iron')
        if i % 2 == 0:
            r.cube(name, (-.25, -2.75, -.25, .5, 2.75, .5), 'iron')
            r.cube(name, (-.75, -1.75, -.25, 1.5, .5, .5), 'iron')
    for i in range(12):
        angle = math.radians(i * 30)
        name = f'halo_inner_{i}'
        r.part(name, 'halo', (snap(math.sin(angle) * 5.5), snap(-math.cos(angle) * 5.5), 0), (0, 0, i * 30))
        r.cube(name, (-1.5, -.25, -.25, 3, .25, .5), 'iron')
    # the halo's spikes lengthen with his patches (the model scales them)
    for i in range(8):
        angle = math.radians(i * 45 + 22.5)
        name = f'halo_spike_{i}'
        r.part(name, 'halo', (snap(math.sin(angle) * 7.25), snap(-math.cos(angle) * 7.25), 0), (0, 0, i * 45 + 22.5))
        r.cube(name, (-.5, -1.5, -.25, .75, 1.5, .5), 'iron')
        r.cube(name, (-.25, -2.5, -.25, .5, 1, .5), 'iron')
        r.cube(name, (-.25, -3.25, -.25, .25, .75, .25), 'iron_tip')
    # two short chains hang off the halo's lower ring and swing when it is lifted, thrown and caught
    for side, sign in (('left', 1), ('right', -1)):
        chain(r, f'halo_chain_{side}', 'halo', (sign * 4.25, 5.25, 0), 4, True)
    staff(r)
    # hidden until Chained Debt and the Tithe: the root of the world-geometry chain
    chain(r, 'claw_chain', 'right_hand', (0, 2, -.25), 3)

    # Thin bone legs are mostly concealed by the ragged column of cloth.
    for side, sign in (('left', 1), ('right', -1)):
        r.part(f'{side}_leg', 'hips', (sign * 1.5, 0, 0), (0, 0, sign * -4))
        r.cube(f'{side}_leg', (-.5, -.25, -.5, 1, 6.5, 1), 'bone')
        r.part(f'{side}_shin', f'{side}_leg', (0, 6, 0), (3, 0, 0))
        r.cube(f'{side}_shin', (-.5, -.5, -.5, 1, 6, 1), 'bone')
        r.cube(f'{side}_shin', (-.75, 5.25, -2, 1.5, 1, 3), 'bone')

    for i in range(14):
        angle = i * 360 / 14
        a = math.radians(angle)
        name = f'robe_strip_{i}'
        length = 10.5 + (i * 3 % 4)
        r.part(name, 'hips', (snap(math.sin(a) * 2.75), 0, snap(math.cos(a) * 1.75)),
               (math.cos(a) * 10, angle, -math.sin(a) * 10))
        material = 'blood_cloth' if i % 3 == 1 else 'ivory_cloth'
        r.cube(name, (-1, -.5, -.25, 2, length, .5), material)
        r.cube(name, (-1, length - .5, -.25, .75, 2, .5), material)
    # Narrow shroud ribbons drape off the back and shoulders, leaving the front rib cage exposed.
    for i in range(8):
        x = -3.5 + i
        name = f'shroud_{i}'
        r.part(name, 'chest', (x, -6.5, 2.5 + (i % 2) * .5), (-18, 0, x * -2))
        r.cube(name, (-.5, 0, -.25, 1, 18 + (i * 3 % 5), .5), 'blood_cloth' if i % 2 else 'ivory_cloth')
    chain(r, 'waist_chain', 'hips', (-2.5, 0, -2.25), 7)

    r.part('train', pivot=(0, 23.75, 0))
    for i, angle in enumerate((35, 70, 110, 145, 180, 215, 250, 290, 325)):
        a = math.radians(angle)
        name = f'train_{i}'
        width = 1.5 if i % 2 else 2.5
        length = 3.5 if i % 3 else 5
        material = 'blood_cloth' if i % 3 == 1 else 'ivory_cloth'
        r.part(name, 'train', (snap(math.sin(a) * 2.5), 0, snap(math.cos(a) * 1.75)), (0, angle, 0))
        r.cube(name, (-width / 2, -.25, 0, width, .5, length), material)
        r.part(name + '_bend', name, (0, 0, length - .5), (0, 15 if i % 2 else -15, 0))
        r.cube(name + '_bend', (-width / 2, -.25, 0, width, .5, 3.5), material)
        r.cube(name + '_bend', (-.5, -.25, 3.5, 1, .5, 1.5), material)
    vessels(r)
    # Iron erupts through the skeleton in patches, one per 1000 points he leaks, in this order; never armour.
    for i, (parent, x, y, z) in enumerate(PATCH_SITES):
        name = f'spike_patch_{i}'
        r.part(name, parent, (x, y, z), (-12, 0, (i % 3 - 1) * 13))
        r.cube(name, (-.75, -.5, -.25, 1.5, 1.25, .5), 'iron')
        spikes = 3 + i % 3
        for j in range(spikes):
            spike = f'{name}_spike_{j}'
            lean = (j - (spikes - 1) / 2) * 22
            r.part(spike, name, (snap(-.5 + (j % 3) * .5), 0, -.25), (-20 + (j % 2) * 14, 0, lean))
            r.cube(spike, (-.5, -1.25, -.5, .75, 1.25, .75), 'iron')
            r.cube(spike, (-.25, -2.25, -.25, .5, 1, .5), 'iron')
            r.cube(spike, (-.25, -3, -.25, .25, .75, .25), 'iron_tip')
    world = kit.transforms(r)
    for part in r.parts:
        if hanging(part['name']):
            part['rotation'] = euler(world[part['parent']][:3, :3].T)
        assert all(snap(v) == v for v in part['pivot']), part['name']
        for cube in part['cubes']:
            assert all(snap(v) == v for v in cube['box']), cube
            assert min(cube['box'][3:]) >= .25, cube
    return r


SITES = []


def hanging(name):
    """A chain root that hangs plumb from a tilted parent: its rotation counters the parent's."""
    return (('shoulder_chain' in name or 'halo_chain' in name)
            and '_link_' not in name and not name.endswith('_pendant'))


def stained(t):
    """Bone over a patch site carries a rust stain, so the iron reads as under the bone before it breaks out."""
    return any(abs(t['gx'] - x) <= .75 and abs(t['gy'] - y) <= .75 and abs(t['gz'] - z) <= .75 for x, y, z in SITES)


def paint(cube, t, glow=False):
    mat = cube['material']
    u, v, n = t['u'], t['v'], (t['n'] - .5) * 12
    lit = False
    if mat == 'bone':
        rgb = add((224, 211, 185), n)
        if u == 0 or v == t['fh'] - 1:
            rgb = (170, 151, 124)
        if (u + v * 3) % 23 == 0:
            rgb = (123, 102, 78)
        if stained(t):
            rgb = mix(rgb, (98, 46, 30), .5)
    elif mat == 'iron':
        # hematic iron rusting toward dried blood, never the orange that read as copper
        corrosion = kit.smooth(t['gx'], t['gy'], t['gz'], scale=2, seed=9)
        rgb = mix((40, 31, 33), (112, 40, 28), min(1, max(0, (corrosion - .25) * 1.7)))
        rgb = add(rgb, n)
        if u == 0 or v == 0:
            rgb = add(rgb, 14)
        if (u * 3 + v) % 7 == 0 and t['n'] > .6:
            rgb = (86, 30, 22)
    elif mat == 'iron_tip':
        rgb = add((139, 18, 24), n * .5)
        if u == 0 and v == 0:
            rgb = (205, 120, 110)
        lit = u == 0 and v == 0
    elif mat == 'vessel':
        rgb = add((104, 12, 20), n * .6)
        if v == 0:
            rgb = (178, 40, 44)
        if (u * 5 + v * 3) % 13 == 0:
            rgb = (52, 6, 10)
        lit = v % 4 == 1
    elif mat == 'dried_vessel':
        rgb = add((78, 28, 24), n * .5)
        if (u + v) % 3 == 0:
            rgb = add(rgb, -14)
    elif mat == 'void':
        rgb = (27, 17, 15)
    elif mat == 'blood':
        rgb = add((119, 16, 19), n)
        if u == 0:
            rgb = (164, 29, 24)
    else:
        red = mat == 'blood_cloth'
        rgb = (101, 15, 19) if red else (221, 209, 185)
        fold = (-17, -4, 8, -3)[u % 4]
        rgb = add(rgb, fold + n)
        if t['side'] and v > t['fh'] - 4:
            rgb = mix(rgb, (56, 23, 17) if red else (119, 93, 68), .45)
        if not red and (u == t['fw'] - 1 or (u + v // 6) % 19 == 0):
            rgb = (128, 39, 28)
        if red and t['side'] and v >= t['fh'] - 3 and u % 3 == 1:
            rgb = add(rgb, 26)          # the wet gloss on the blood-cloth hems
    # only vessel core lines and spike tips glow: he lights from within, and only when fed
    return (*rgb, 255) if not glow else ((255, 210, 196, 255) if lit else (0, 0, 0, 0))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = saint()
    SITES[:] = site_points(rig)
    size = layout(rig, 128)
    base, glow = paint_atlases(rig, size, [paint, lambda c, t: paint(c, t, True)])
    TEXTURES.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURES / 'hemorath.png')
    glow.save(TEXTURES / 'hemorath_overload.png')
    source = entity_java_source(rig, size, 'HemorathEntity', 'hemorath.py',
        package='com.vincenthuto.hemomancy.client.model.entity.boss.hemorath',
        entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath',
        extra_imports='import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathHaloPath;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings;\n'
                      'import com.vincenthuto.hemomancy.common.util.HemoEasing;\n'
                      'import static com.vincenthuto.hemomancy.common.util.HemoEasing.catmullRom;\n'
                      'import org.joml.Quaternionf;\nimport org.joml.Matrix3f;\nimport org.joml.Matrix4f;\n',
        extra_methods=(ROOT / 'tools/model_export/hemorath_anim.java').read_text())
    source = source.replace('public class HemorathModel extends HierarchicalModel<HemorathEntity>',
                            'public class HemorathModel<T extends HemorathEntity> extends HierarchicalModel<T>')
    source = source.replace('import net.minecraft.world.entity.HumanoidArm;\n', '')
    JAVA.write_text(source, encoding='utf-8')
    subprocess.run(['node', str(ROOT / 'tools/model_export/java_model_to_bbmodel.mjs'), '--source', str(JAVA),
                    '--texture', 'textures/entity/boss/hemorath/hemorath.png', '--output', str(BBMODEL)],
                   cwd=ROOT, check=True)
    project = json.loads(BBMODEL.read_text())
    project['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(
        (TEXTURES / 'hemorath.png').read_bytes()).decode('ascii')
    BBMODEL.write_text(json.dumps(project, indent=2) + '\n', encoding='utf-8')
    print(f'Hemorath: {sum(len(p["cubes"]) for p in rig.parts)} cubes; {len(rig.parts)} joints; atlas {base.size}')
    if args.preview:
        def posed(overrides):
            world = kit.transforms(rig, overrides)
            for part in rig.parts:
                name = part['name']
                if name != 'staff' and not hanging(name):
                    continue
                rotation = euler(world[part['parent']][:3, :3].T)
                overrides[name] = tuple(a - b for a, b in zip(rotation, part['rotation']))
            return quads(rig, overrides=overrides)
        def state(lobes, mass, arms, patches, halo=True):
            hidden = ['claw_chain'] + [f'spike_patch_{i}' for i in range(patches, 9)]
            if lobes <= 0:
                hidden.append('vessel_lobes')
            if mass <= 0:
                hidden.append('vessel_mass')
            if arms <= 0:
                hidden += ['left_arm_vessel', 'right_arm_vessel']
            if not halo:
                hidden.append('halo')
            cords = .6 if lobes <= 0 else 1
            scales = {'vessel_lobes': (lobes,) * 3, 'vessel_mass': (mass,) * 3,
                      'left_arm_vessel': (arms,) * 3, 'right_arm_vessel': (arms,) * 3,
                      'vessel_cords': (cords, 1, cords)}
            overrides = {}
            world = kit.transforms(rig, overrides)
            for part in rig.parts:
                name = part['name']
                if name != 'staff' and not hanging(name):
                    continue
                rotation = euler(world[part['parent']][:3, :3].T)
                overrides[name] = tuple(a - b for a, b in zip(rotation, part['rotation']))
            return quads(rig, hidden=hidden, overrides=overrides, scales=scales)
        sunken = state(0, 0, 0, 0)
        filling = state(.95, 0, .85, 4)
        brimming = state(1.12, 1.05, 1.1, 9)
        away = state(.95, 0, .85, 4, halo=False)
        tiles = [(sunken, base, 0, 0), (sunken, base, -.6, -.12), (sunken, base, math.pi, 0),
                 (filling, base, -.3, 0), (brimming, base, -.3, 0), (brimming, base, math.pi / 2, 0),
                 (state(0, 0, 0, 4), base, -.3, 0), (state(0, 0, 0, 9), base, -.3, 0), (away, base, -.3, 0)]
        kit.preview_sheet(tiles, ROOT / 'build/hemorath_preview.png', cols=3)
        preview = Image.open(ROOT / 'build/hemorath_preview.png')
        draw = ImageDraw.Draw(preview)
        for i, label in enumerate(('SUNKEN FRONT', 'SUNKEN THREE QUARTER', 'SUNKEN BACK', 'FILLING, 4 PATCHES',
                                   'BRIMMING, 9 PATCHES', 'BRIMMING SIDE', 'PATCHES 4', 'PATCHES 9', 'HALO AWAY')):
            draw.text(((i % 3) * 420 + 14, (i // 3) * 420 + 12), label, fill=(222, 206, 180))
        preview.save(ROOT / 'build/hemorath_preview.png')
        kit.render_quads(sunken, base, -.35, -.08, 1100).save(ROOT / 'build/hemorath_portrait.png')
        kit.render_quads(brimming, base, -.35, -.08, 1100).save(ROOT / 'build/hemorath_portrait_brimming.png')


if __name__ == '__main__':
    main()

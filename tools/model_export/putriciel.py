"""Putriciel's fungal reliquary model, based on the supplied Rot and Offering sheet, rebuilt for the Saint overhaul
(docs/superpowers/specs/2026-11-05-saint-putriciel-overhaul-design.md section 12, and the 2026-11-06 revision R1, R3).

python tools/model_export/putriciel.py --preview

Writes the Java rig (with putriciel_anim.java as its pose code), portable Blockbench source, five fitted atlases
(base, spore glow, charred, embers, burning) and review images. All saved cube geometry and pivots use quarter units.

Texel density: the rig is authored at the old scale and rescaled by 0.7 about the feet (y = 24), every pivot and box
snapped to quarter units and no cube under half a unit; at 2 px a unit and render scale 2.0 the atlas lands at 16 px
per block (2 x 16 / 2.0), the world size unchanged (0.7 x 2.0 = the old 1.4). The script asserts it.

Palette (revision R3): ochre, olive, mould green, muted yellow, pale cream, ivory and blackened charcoal in three value
bands, graded from the light crown, hood rim, mantles and sac crowns down to the tide-marked robe, charcoal hem and
roots; no red anywhere outside the embers and burning atlases, which hold only the Flammeus fire (crimson body,
pink-white heat, black soot), and no brass, bronze, copper or silver: the iron is blackened, its edges mould-bloomed,
and ochre and muted yellow stay matte, noise-broken and under 15% of the base. No bone-brick trims; bone is on the
fingers only.

Fire (revision R1): the generated model carries the fire anchor table (part, point, normal, steady height, region,
kindle order and tick), every part's region and every growth part's kindle tick and regrowth step; the burning atlas
lights what burns under the flames, drawn by PutricielBurnLayer one region at a time.
"""
import argparse
import base64
import colorsys
import json
import math
import subprocess

from PIL import Image, ImageDraw
import annetta_kit as kit
from annetta_kit import Rig, ROOT, ASSETS, layout, paint_atlases, entity_java_source, quads, add, mix, camel
from hemorath import snap, euler, chain
from unstained_weapons import f

kit.SCALE = 2
RIG_SCALE = 0.7
RENDER_SCALE = 2.0
MIN_CUBE = 0.5
FEET_Y = 24.0
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/putriciel/PutricielModel.java'
TEXTURES = ASSETS / 'textures/entity/boss/putriciel'
BBMODEL = ASSETS / 'models/entity/bbmodel/bosses/PutricielModel.bbmodel'

# The fire palette, and only in the embers and burning atlases.
CRIMSON = (214, 30, 46)
HEAT = (255, 168, 190)
SOOT = (22, 10, 12)
FIRE = (CRIMSON, HEAT, SOOT)

# The body fire's regions, in PutricielFireAnchors.Region's order.
REGIONS = ('STAFF', 'CENSER', 'BACK', 'SHOULDER', 'CROWN', 'CHEST', 'HEM')

# The staff's shaft rings: the four upper ones are rot-soaked rag wraps (they burn), the lower and the foot ring iron.
WRAP_YS = (-12, -7, -2, 4)
# Which wrap the fire starts at: the grip wrap, the first above the hand.
GRIP_WRAP = 2


def shelf(r, name, parent, pivot, width=4.5, depth=3, pale=False, rotation=(0, 0, 0), scheme=None, moss=False):
    """A bracket shelf: a zoned cap over a narrower top layer, gills under it and a stalk at its root. Each shelf takes
    one of three colour schemes (ochre-led, olive-led, pale-led; revision R3) so no two neighbours match."""
    r.part(name, parent, pivot, rotation)
    material = 'pale_cap' if pale else 'shelf_cap'
    if scheme is None:
        scheme = sum(ord(c) * (i + 3) for i, c in enumerate(name)) % 3
    r.cube(name, (-width / 2, -.5, -depth / 2, width, .75, depth), material, cap_width=width, cap_depth=depth,
           variant=scheme)
    r.cube(name, (-(width - 1) / 2, -1, -(depth - .5) / 2, width - 1, .5, depth - .5),
           material, cap_width=width, cap_depth=depth, variant=scheme)
    r.cube(name, (-(width - 1) / 2, .25, -(depth - .5) / 2, width - 1, .25, depth - .5), 'gills', pale=pale,
           variant=scheme)
    r.cube(name, (-.5, .25, depth / 2 - 1, 1, 1, 1), 'stem' if pale else 'flesh')
    if moss:
        # a tuft of moss where the shelf roots into the body (its -z side faces the body)
        r.cube(name, (-1.5, .25, -depth / 2 + .25, .75, .75, .75), 'moss')
        r.cube(name, (-.75, .5, -depth / 2 + .5, .75, .75, .75), 'moss')


def mushroom(r, name, parent, pivot, height=2.5):
    r.part(name, parent, pivot)
    r.cube(name, (-.25, -height, -.25, .5, height, .5), 'stem')
    shelf(r, name + '_cap', name, (0, -height, 0), 3, 2.5, True)


def spore(r, name, parent, pivot, diameter=3.5, index=0):
    r.part(name, parent, pivot, (0, 0, (index - 1) * 7))
    narrow = diameter - 1
    for w, h, d in ((narrow, diameter, narrow), (diameter, narrow, narrow), (narrow, narrow, diameter)):
        r.cube(name, (-w / 2, -h / 2, -d / 2, w, h, d), 'spore', spore=index, diameter=diameter)
    r.cube(name, (-.5, diameter / 2 - .25, -.5, 1, 1.25, 1), 'ichor')


def arm(r, side, sign):
    name = f'{side}_arm'
    r.part(name, 'chest', (sign * 5.5, -5.5, 0), (-6 if sign > 0 else -38, 0, -35 if sign > 0 else 40))
    r.cube(name, (-1.75, -1.25, -1.75, 3.5, 3.5, 3.5), 'flesh')
    r.cube(name, (-1.25, 1.5, -1.25, 2.5, 5, 2.5), 'flesh')
    r.cube(name, (-1.75, -.5, 1, 3.5, 8, .75), 'blood_cloth')
    r.cube(name, (-1.25, 2, -1.5, .5, 4, .5), 'seam')
    fore = f'{side}_forearm'
    r.part(fore, name, (0, 6, 0), (0 if sign > 0 else -46, 0, -105 if sign > 0 else -10))
    r.cube(fore, (-1.25, -.75, -1.25, 2.5, 7, 2.5), 'flesh')
    r.cube(fore, (-1.5, 4.75, -1.5, 3, .75, 3), 'iron')
    hand = f'{side}_hand'
    r.part(hand, fore, (0, 6.5, 0))
    if sign > 0:
        r.cube(hand, (-1.25, -.5, -.75, 2.5, 2.75, 1.5), 'flesh')
        for i in range(4):
            finger = f'gripping_finger_{i}'
            r.part(finger, hand, (-1 + i * .5, 1.5, -.5), (-75, 0, 0))
            r.cube(finger, (-.25, -.25, -.25, .5, 1.75, .5), 'bone')
            r.part(finger + '_tip', finger, (0, 1.5, 0), (-90, 0, 0))
            r.cube(finger + '_tip', (-.25, 0, -.25, .5, 1.25, .5), 'bone')
    else:
        # A palm-up offering hand, with crooked fingers, five nodules in the palm and long hanging ichor drips.
        world = kit.transforms(r)
        next(p for p in r.parts if p['name'] == hand)['rotation'] = euler(world[fore][:3, :3].T)
        r.cube(hand, (-2, -.75, -2, 4, 1.5, 3.5), 'flesh')
        for i in range(5):
            nodule = f'palm_nodule_{i}'
            r.part(nodule, hand, (-1.5 + i * .75, -.75, -.5 + (i % 2) * .5))
            r.cube(nodule, (-.5, -.75, -.5, 1, .75, 1), 'nodule')
        for i in range(4):
            finger = f'offering_finger_{i}'
            r.part(finger, hand, (-1.5 + i, -.25, -1.75), (0, (i - 1) * 7, 0))
            r.cube(finger, (-.25, -.25, -2.25, .5, .75, 2.5), 'bone')
            r.part(finger + '_tip', finger, (0, 0, -2), (-32, 0, 0))
            r.cube(finger + '_tip', (-.25, -.25, -1.5, .5, .75, 1.75), 'bone')
        r.part('offering_thumb', hand, (-1.75, 0, -.5), (0, 35, -15))
        r.cube('offering_thumb', (-1.75, -.25, -.25, 2, .75, .75), 'bone')
        r.part('offering_drips', hand)
        for i in range(7):
            x = -1.5 + i * .5
            r.cube('offering_drips', (x, .75, -2.5 + (i % 3) * .5, .25, 3.5 + (i * 3 % 5), .25), 'ichor')
        shelf(r, 'knuckle_growth', hand, (1.25, -.75, .5), 2.5, 2, scheme=2)
    shelf(r, f'{side}_elbow_growth', fore, (0, .75, 1), 4, 2.5, scheme=0 if sign > 0 else 1)


def censer(r, name, parent, pivot):
    r.part(name, parent, pivot)
    # A pierced lid: four bars round an open ring, so the fire shows through it in the interval.
    for box in ((-1.25, 0, -1.25, 2.5, .5, .75), (-1.25, 0, .5, 2.5, .5, .75),
                (-1.25, 0, -.5, .75, .5, 1), (.5, 0, -.5, .75, .5, 1)):
        r.cube(name, box, 'iron', lid=True)
    r.cube(name, (-1.5, -1.5, -1.5, 3, .5, 3), 'iron')
    for sx in (-1.25, 1):
        for sz in (-1.25, 1):
            r.cube(name, (sx, -2.75, sz, .25, 3.25, .25), 'iron')
    r.cube(name, (-.75, -.5, -.75, 1.5, .5, 1.5), 'offering')
    for i in range(3):
        r.cube(name, (-.75 + i * .75, .5, -.25, .25, 1.5 + i, .25), 'ichor')


def staff(r):
    world = kit.transforms(r)
    hand = world['left_hand']
    pivot = (0, 1, -1)
    bottom = snap(FEET_Y - (hand[:3, :3] @ pivot + hand[:3, 3])[1])
    r.part('staff', 'left_hand', pivot, euler(hand[:3, :3].T))
    r.cube('staff', (-.5, -15, -.5, 1, bottom + 15, 1), 'iron')
    r.cube('staff', (-1, bottom - .75, -1, 2, .75, 2), 'iron')
    for y in (9, bottom - 1.5):
        r.cube('staff', (-.75, y, -.75, 1.5, .75, 1.5), 'iron')
    # The four upper rings are rot-soaked rag wraps (revision R3): what burns along the staff in the interval.
    for i, y in enumerate(WRAP_YS):
        wrap = f'staff_wrap_{i}'
        r.part(wrap, 'staff', (0, y + .375, 0))
        r.cube(wrap, (-.875, -.875, -.875, 1.75, 1.75, 1.75), 'staff_wrap')
        r.cube(wrap, ((-1.25, .75)[i % 2], -.25, -.375, .75, 2.25, .75), 'staff_wrap')
    r.part('staff_foot', 'staff', (0, bottom, 0))
    r.part('staff_head', 'staff', (0, -13, 0))
    r.cube('staff_head', (-4.5, -.5, -.5, 9, .75, 1), 'iron')
    r.cube('staff_head', (-3, -3.5, -.5, 6, .75, 1), 'iron')
    for x, height in ((-3, 4.5), (0, 6), (3, 5)):
        r.cube('staff_head', (x - .25, -height, -.25, .5, height + .75, .5), 'iron', prong=True)
        r.cube('staff_head', (x - .75, -height + 1, -.25, 1.5, .5, .5), 'iron')
    for side, x, count in (('left', -4, 6), ('right', 4, 8)):
        name = 'staff_chain_' + side
        chain(r, name, 'staff_head', (x, .25, 0), count)
        censer(r, 'censer_' + side, name, (0, count * 1.5, 0))
    for i, y in enumerate((-9, 6, 12)):
        shelf(r, f'staff_growth_{i}', 'staff', (0, y, .5), 3, 2, scheme=i % 3)


def legs(r):
    """Jointed legs under the robe (resolution 7): hip, knee and ankle pivots, knees a little bent, toes turned in."""
    for side, sign in (('left', 1), ('right', -1)):
        thigh = f'{side}_thigh'
        r.part(thigh, 'hips', (sign * 2, 4, 0), (-6, -sign * 6, 0))
        r.cube(thigh, (-1.5, -.5, -1.5, 3, 5, 3), 'flesh')
        r.cube(thigh, (-1.75, 1, -1.75, 3.5, 2, 3.5), 'blood_cloth')
        shin = f'{side}_shin'
        r.part(shin, thigh, (0, 4.5, 0), (10, 0, 0))
        r.cube(shin, (-1.25, -.5, -1.25, 2.5, 4.5, 2.5), 'flesh')
        r.cube(shin, (-1.5, 1.5, -1.5, 3, 1.5, 3), 'blood_cloth')
        foot = f'{side}_foot'
        r.part(foot, shin, (0, 4, 0), (-4, 0, 0))
        r.cube(foot, (-1.25, -.5, -2.75, 2.5, 1, 4), 'root')


def saint():
    r = Rig('putriciel', 'PutricielModel', 'putriciel')
    r.part('hips', pivot=(0, 11, 0))
    r.cube('hips', (-4, -.75, -3, 8, 5, 6), 'flesh')
    legs(r)
    # The permanent hunch the fungal structures force: 18 degrees at rest (the fire straightens it to 2).
    r.part('chest', 'hips', (0, -8, 0), (18, 0, -3))
    r.cube('chest', (-4.25, -6.5, -3, 8.5, 12, 6), 'blood_cloth')
    r.cube('chest', (-3, -6.75, -3.25, 6, 10, .5), 'flesh')
    r.cube('chest', (-4.75, -6.5, -2.25, 9.5, 3, 4.5), 'flesh')
    r.cube('chest', (-3, -7.75, -2.5, 6, .75, 5), 'iron')
    # The eschar seams: dark in the base atlas, lit only in the embers atlas.
    r.part('eschar_seams', 'chest')
    for box in ((-2.5, -5, -3.5, .5, 7, .5), (-.25, -6, -3.5, .5, 8.5, .5), (2, -4.5, -3.5, .5, 6, .5),
                (-1.5, -1, -3.5, 3.5, .5, .5), (-3, 0, 3, .5, 6, .5), (2.5, -2, 3, .5, 6.5, .5)):
        r.cube('eschar_seams', box, 'seam')
    for side, sign in (('left', 1), ('right', -1)):
        name = f'{side}_collar'
        r.part(name, 'chest', (sign * 3.5, -6.75, -3.25), (0, 0, sign * 28))
        r.cube(name, (-1, 0, -.25, 2, 5.5, .5), 'ivory_cloth')
        for i in range(4):
            r.cube(name, (-1 + i * .5, 5, -.25, .5, 1 + i * .5, .5), 'ivory_cloth')
        # The stole (revision R3): an ivory strip down the chest front from the collar to the belt, the man's
        # vestment under the rot, carried on down the front robe panels.
        stole = f'stole_{side}'
        r.part(stole, 'chest', (sign * 3.25, -6.25, -3.5))
        r.cube(stole, (-.75, 0, -.25, 1.5, 11.5, .5), 'stole')
        mantle = f'{side}_mantle'
        r.part(mantle, 'chest', (sign * 4, -6.75, 0), (0, 0, -sign * 15))
        r.cube(mantle, (-1.75, -.5, -2.75, 3.5, 1.5, 5.5), 'ivory_cloth')
        for i in range(4):
            r.cube(mantle, (-1.5 + i, 1, -2.75, .5, 1.5 + (i % 3), .5), 'ivory_cloth')
        # two moss tufts on each shoulder
        r.cube(mantle, (-1.25, -1.25, -1.75, .75, .75, .75), 'moss')
        r.cube(mantle, (.25, -1.25, .75, .75, .75, .75), 'moss')
        arm(r, side, sign)
        chain(r, f'{side}_vestment_chain', 'chest', (sign * 4.5, -3, -2.5), 9 if sign > 0 else 7)
        for i in range(3 if sign > 0 else 5):
            shelf(r, f'{side}_shoulder_shelf_{i}', 'chest',
                  (sign * (4.25 + (i % 2) * .75), -6.5 + i * 1.5, 1),
                  4 + (i % 2), 3, rotation=(0, sign * 25, sign * -8), scheme=(i + (2 if sign > 0 else 0)) % 3)
    r.part('spore_cluster', 'chest')
    for i, (x, y, z, d) in enumerate(((-1.5, -.5, -4, 4), (1.75, 1.5, -4.25, 3.5),
                                      (-1.25, 4, -4.25, 3.5), (1.5, 5, -3.75, 3))):
        spore(r, f'spore_{i}', 'spore_cluster', (x, y, z), d, i)
    spore(r, 'shoulder_spore', 'right_arm', (-1.75, -.75, 0), 3.5, 4)

    # A hollow face inside a ragged mycelial hood, surmounted by a tall open reliquary crown; the purifier's own face
    # waits a unit behind the hollow, and the fire pushes it forward.
    r.part('head', 'chest', (0, -8.5, -1), (6, 0, 0))
    r.cube('head', (-2.25, -4.75, -1.75, 4.5, 4.75, 3.5), 'void')
    r.cube('head', (-3.25, -5.75, -2.75, 6.5, .75, 5.5), 'ivory_cloth')
    r.cube('head', (-3.25, -5, -2.75, .75, 6, 5.5), 'flesh', hood=True)
    r.cube('head', (2.5, -5, -2.75, .75, 6, 5.5), 'flesh', hood=True)
    r.cube('head', (-2.5, .25, -2.75, 5, .75, 5.5), 'iron')
    r.part('purifier_face', 'head', (0, -2.25, -.75))
    r.cube('purifier_face', (-1.75, -2, -.5, 3.5, 4, .5), 'ash_face')
    r.part('face_vines', 'head')
    for i in range(9):
        r.cube('face_vines', (-2 + i * .5, -4.75, -3 + (i % 2) * .25, .5, 4.5 + (i * 2 % 5), .5),
               'ichor' if i % 3 else 'root')
    r.part('crown', 'head', (0, -5.75, 0))
    for y in (0, -4):
        for box in ((-3, y, -2.5, 6, .5, .5), (-3, y, 2, 6, .5, .5),
                    (-3, y, -2, .5, .5, 4), (2.5, y, -2, .5, .5, 4)):
            r.cube('crown', box, 'iron')
    for i, (x, z, height) in enumerate(((-3, -2, 7), (3, -2, 8.5), (-3, 2, 6), (3, 2, 7.5),
                                        (0, -2.25, 6), (0, 2.25, 9))):
        name = f'crown_spire_{i}'
        r.part(name, 'crown', (x, 0, z), (0, 0, (i % 3 - 1) * 4))
        r.cube(name, (-.25, -height, -.25, .5, height, .5), 'iron', spire=True)
        r.cube(name, (-1, -height + 1, -.25, 2, .5, .5), 'iron', spire=True)
        r.cube(name, (-.75, -height + 2.5, -.25, 1.5, .5, .5), 'iron', spire=True)
    shelf(r, 'head_shelf_left', 'head', (2.75, -2.5, .75), 4, 3, scheme=2)
    shelf(r, 'head_shelf_right', 'head', (-2.75, -3.5, .75), 4.5, 3, scheme=0)
    mushroom(r, 'crown_mushroom', 'head', (-2.5, -4.5, 1.5), 3)
    mushroom(r, 'shoulder_mushroom', 'right_arm', (-1, -1.5, 1), 3)
    staff(r)

    r.part('back_colony', 'chest')
    for i, (x, y, z) in enumerate(((-3.5, -7, 3), (1, -6, 3.5), (4, -4, 2.75), (-3, -3, 3.75),
                                    (1, -1, 4), (3.5, 1, 3.75), (-3, 3, 3.75), (.5, 5, 4),
                                    (4, 7, 3.5), (-3.5, 9, 3), (0, 10.5, 3.5))):
        shelf(r, f'back_shelf_{i}', 'back_colony', (x, y, z), 4.5 + (i % 3) * .5, 3.5, scheme=i % 3, moss=i % 2 == 0)
    mushroom(r, 'back_mushroom', 'back_colony', (2, -5, 4), 4)
    # Two sacs on the back colony, so the out-of-rhythm swell reads from behind: eight sacs in all.
    spore(r, 'back_spore_0', 'back_colony', (-1.5, 1.5, 5), 3, 6)
    spore(r, 'back_spore_1', 'back_colony', (2.5, -2.5, 5), 3, 7)
    mould = 0
    for i in range(14):
        angle = i * 360 / 14
        a = math.radians(angle)
        name = f'robe_panel_{i}'
        length = 11 + (i * 2 % 3)
        r.part(name, 'hips', (snap(math.sin(a) * 3.5), 0, snap(math.cos(a) * 2.5)),
               (math.cos(a) * 10, angle, -math.sin(a) * 10))
        ivory = i % 4 == 0
        material = 'ivory_cloth' if ivory else 'blood_cloth'
        # every other rot-dyed panel is deep mould green (revision R3); the front three carry the stole down
        green = not ivory and mould % 2 == 1
        mould += 0 if ivory else 1
        extra = dict(robe=True, green=green, stole_band=6 <= i <= 8)
        r.cube(name, (-1.75, -.5, -.25, 3.5, length, .5), material, **extra)
        r.cube(name, (-1.75, length - .5, -.25, 1.25, 1.5, .5), material, **extra)
    for i, (x, y, z) in enumerate(((-4.5, 4, 2), (-5, 7, 2), (-5.5, 10, 2.5),
                                    (4.5, 5, 2.5), (5, 8, 2), (5.5, 11, 2.5))):
        shelf(r, f'hem_shelf_{i}', 'hips', (x, y, z), 5 + (i % 2), 3.5, scheme=(i + 1) % 3)
    spore(r, 'hem_spore', 'hips', (4.5, 9, -1.5), 3.5, 5)
    r.part('ground_roots', pivot=(0, 23.75, 0))
    for i in range(8):
        a = math.radians(i * 45)
        name = f'root_trail_{i}'
        r.part(name, 'ground_roots', (snap(math.sin(a) * 3), 0, snap(math.cos(a) * 2.5)), (0, i * 45, 0))
        r.cube(name, (-.5, -.25, 0, 1, .5, 3 + (i % 3)), 'root')
        r.cube(name, (-.75, -.25, 2.5, 1.5, .5, 2), 'blood_cloth')
    for i, (x, y, z) in enumerate(((-3, -5, 4), (3, -2, 4), (-4, 3, 3.5), (2, 6, 4))):
        mushroom(r, f'growth_mushroom_{i}', 'back_colony', (x, y, z), 3 + i * .5)
        name = f'rot_drip_{i}'
        r.part(name, 'back_colony', (x, y + 1, z))
        r.cube(name, (-.25, 0, -.25, .5, 3 + i, .5), 'ichor')
        r.cube(name, (-.5, 3 + i, -.5, 1, 1, 1), 'ichor')
    rescale(r, RIG_SCALE)
    staff_to_floor(r)
    for part in r.parts:
        assert all(snap(v) == v for v in part['pivot']), part['name']
        for cube in part['cubes']:
            assert all(snap(v) == v for v in cube['box']), cube
            assert min(cube['box'][3:]) >= MIN_CUBE, cube
            cube['part'] = part['name']
        if part['name'].startswith('crown_spire_'):
            for cube in part['cubes']:
                cube['spire_top'] = part['cubes'][0]['box'][1]
    return r


def rescale(r, factor):
    """Scales the rig about the feet: top-level pivots round y = 24, children and boxes uniformly; quarter units,
    no cube under half a unit (it grows about its centre)."""
    for part in r.parts:
        x, y, z = part['pivot']
        if part['parent'] == 'root':
            part['pivot'] = (snap(x * factor), snap(FEET_Y + (y - FEET_Y) * factor), snap(z * factor))
        elif part['parent'] is not None:
            part['pivot'] = (snap(x * factor), snap(y * factor), snap(z * factor))
        for cube in part['cubes']:
            bx, by, bz, w, h, d = cube['box']
            out = []
            for origin, size in ((bx, w), (by, h), (bz, d)):
                scaled = max(MIN_CUBE, snap(size * factor))
                centre = (origin + size / 2) * factor
                out.append((snap(centre - scaled / 2), scaled))
            cube['box'] = (out[0][0], out[1][0], out[2][0], out[0][1], out[1][1], out[2][1])


def staff_to_floor(r):
    """Snapping the rescale moves the staff's foot off the floor: stretch the shaft (and move its foot cap, last
    ring and foot point) by the nearest quarter so the foot stands at y = 24 again."""
    import numpy as np
    world = kit.transforms(r)
    staff_part = next(p for p in r.parts if p['name'] == 'staff')
    bottom = max(c['box'][1] + c['box'][4] for c in staff_part['cubes'])
    delta = snap(FEET_Y - (world['staff'] @ np.array([0, bottom, 0, 1]))[1])
    if delta == 0:
        return
    cubes = staff_part['cubes']
    x, y, z, w, h, d = cubes[0]['box']
    cubes[0]['box'] = (x, y, z, w, max(MIN_CUBE, h + delta), d)
    for cube in cubes[1:]:
        x, y, z, w, h, d = cube['box']
        if y + h >= bottom - 1.25:
            cube['box'] = (x, y + delta, z, w, h, d)
    foot = next(p for p in r.parts if p['name'] == 'staff_foot')
    fx, fy, fz = foot['pivot']
    foot['pivot'] = (fx, fy + delta, fz)


def cap_radius(cube, t):
    width = cube['cap_width'] * RIG_SCALE
    depth = cube['cap_depth'] * RIG_SCALE
    return math.sqrt((t['lx'] / (width / 2)) ** 2 + (t['lz'] / (depth / 2)) ** 2)


# ---------------------------------------------------------------- the base palette (revision R3.1)

ROBE = (70, 64, 40)          # rot-dyed olive brown
ROBE_MOULD = (52, 66, 40)    # every other rot-dyed panel
TIDE = (128, 108, 56)        # ochre tide marks where the rot wicked up
HEM = (34, 34, 22)           # the hem's lowest three texels
WET = (150, 152, 120)        # single-texel wet highlights
IVORY = (236, 228, 204)
CREAM = (222, 210, 176)
STAIN = (166, 140, 80)       # ochre stain at the vestment edges
BLOOM = (110, 140, 76)       # mould-green bloom creeping in from the seams
STITCH = (46, 44, 32)        # the stole's charcoal stitched bands
FLESH = (150, 144, 118)      # pallid grey cream
MOTTLE = (88, 90, 56)
VEIN = (96, 124, 66)
ESCHAR = (42, 36, 30)
SCAB = (70, 60, 44)
SAC_CROWN, SAC_MIDDLE, SAC_BASE, SAC_VEIN = (226, 218, 184), (196, 176, 98), (150, 124, 58), (84, 92, 48)
CAP_HEART, CAP_OLIVE, CAP_OCHRE, CAP_YELLOW, CAP_RIM = (58, 58, 34), (96, 96, 56), (150, 126, 62), (192, 172, 96), \
    (228, 216, 176)
GILL_PORE, GILL_DARK, GILL_BLOOM = (176, 150, 78), (92, 88, 52), (110, 150, 76)
PALE_CAP, FRECKLE = (232, 222, 192), (160, 134, 70)
ICHOR, ICHOR_WET = (28, 30, 16), (180, 186, 150)
BARK, FIBRE = (52, 46, 30), (120, 100, 52)
WRAP_OCHRE, WRAP_DARK, WRAP_SOAK = (110, 92, 48), (46, 44, 28), (30, 30, 18)
MOSS, MOSS_UNDER = (126, 164, 84), (76, 100, 52)
BONE, JOINT = (210, 198, 164), (126, 108, 68)
GLOW_CREAM, GLOW_GILL = (255, 233, 145), (118, 206, 92)

# Each shelf's scheme: the radii (of 1 at the rim) where its zones end, heart to rim.
SCHEMES = {
    0: ((.22, CAP_HEART), (.42, CAP_OLIVE), (.62, CAP_OCHRE), (.8, CAP_YELLOW)),     # ochre-led
    1: ((.32, CAP_HEART), (.66, CAP_OLIVE), (.74, CAP_OCHRE), (.82, CAP_YELLOW)),    # olive-led
    2: ((.2, CAP_OLIVE), (.48, (150, 146, 112)), (.6, CAP_OCHRE), (.7, (206, 196, 158))),  # pale-led
}


def tide(t):
    """0 at the crown, 1 at the floor: the rot wicks up from the ground."""
    return kit.clamp((t['gy'] - 4.0) / (FEET_Y - 4.0))


def edge(t):
    """Texels from the nearest edge of the face."""
    return min(t['u'], t['v'], t['fw'] - 1 - t['u'], t['fh'] - 1 - t['v'])


def cloth(cube, t):
    mat = cube['material']
    n = (t['n'] - .5) * 12
    pale = mat in ('ivory_cloth', 'stole')
    robe = cube.get('robe', False)
    stole = mat == 'stole' or (robe and cube.get('stole_band') and t['side'] and abs(t['fu'] - .5) < .22)
    if pale or stole:
        rgb = mix(IVORY, CREAM, kit.smooth(t['gx'], t['gy'], t['gz'], scale=.9, seed=13))
        rgb = add(rgb, n * .6)
        seam = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.7, seed=17)
        wide = min(t['fw'], t['fh']) >= 4
        if edge(t) == 0 and t['side'] and t['n'] > (.45 if wide else .7):
            rgb = mix(rgb, STAIN, .7)
        elif edge(t) <= (1 if wide else 0) and seam > (.68 if wide else .74):
            rgb = BLOOM
        if stole and t['side'] and t['v'] % 4 == 0:
            rgb = STITCH
        if robe and not stole:
            rgb = mix(rgb, mix(rgb, (118, 108, 74), .7), tide(t) ** 2)
    else:
        rgb = ROBE_MOULD if cube.get('green') else ROBE
        rgb = add(rgb, (-6, -2, 4, 0)[t['u'] % 4] + n)
        if t['side'] and robe:
            rgb = mix(rgb, mix(rgb, HEM, .55), tide(t))
    if robe:
        # ochre tide marks at noise-wobbled heights, the hem charcoal over its lowest three texels
        wobble = (kit.smooth(t['gx'], 0, t['gz'], scale=.8, seed=23) - .5) * 1.2
        for height in (18.0, 19.75, 21.25):
            if abs(t['gy'] + wobble - height) < .26 and t['side'] and not stole:
                rgb = TIDE if t['n'] > .25 else add(TIDE, -18)
        if t['side'] and t['v'] >= t['fh'] - 3 and t['face'] != 'down':
            rgb = HEM
    wet = kit.smooth(t['gx'], t['gy'], t['gz'], scale=1.1, seed=29)
    if not pale and wet > .74:
        rgb = mix(rgb, (24, 26, 14), .45)
        if t['n'] > .965:
            rgb = WET
    return rgb


def paint(cube, t, glow=False):
    mat = cube['material']
    u, v, n = t['u'], t['v'], (t['n'] - .5) * 12
    lit = None
    if mat == 'spore':
        radius = cube['diameter'] * RIG_SCALE / 2
        # graded crown to base, each sac's grading shifted by its seed
        shift = ((cube['spore'] * .37) % 1 - .5) * .3
        height = kit.clamp((t['ly'] + radius) / (2 * radius) + shift)
        broken = (kit.smooth(t['gx'], t['gy'], t['gz'], scale=.45, seed=cube['spore'] + 31) - .5) * .25
        height = kit.clamp(height + broken)
        if height < .4:
            rgb = mix(SAC_CROWN, (210, 200, 158), height / .4)
        elif height < .72:
            rgb = mix((188, 172, 112), SAC_MIDDLE, (height - .4) / .32)
        else:
            rgb = mix(SAC_MIDDLE, SAC_BASE, (height - .72) / .28)
        cells = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.6, seed=cube['spore'])
        if abs(cells - .5) < .045:
            rgb = SAC_VEIN
        rgb = add(rgb, n * .5)
        if height < .32 and cells > .45:
            lit = GLOW_CREAM
    elif mat in ('shelf_cap', 'pale_cap'):
        radius = cap_radius(cube, t) + (t['n'] - .5) * .12
        top_layer = cube['box'][3] < cube['cap_width'] * RIG_SCALE - .3
        if mat == 'shelf_cap':
            zones = SCHEMES[cube['variant']]
            rgb = zones[-1][1]
            for limit, zone in zones:
                if radius < limit:
                    rgb = zone
                    break
            if t['face'] == 'up':
                # the cap's underside, in the shade of its own brim
                rgb = mix(rgb, (40, 40, 24), .55)
            elif t['side'] and not top_layer:
                # the cap's brim seen edge-on: the pale cream rim, a lighter top-edge texel every few
                rgb = add(CAP_RIM, 10) if t['u'] % 3 == 0 else CAP_RIM
            elif .86 < radius < 1.02 and not top_layer:
                rgb = CAP_RIM
        else:
            rgb = PALE_CAP if radius > .25 else (204, 194, 160)
            if t['n'] > .92:
                rgb = FRECKLE
            if t['side'] and t['v'] == 0:
                rgb = (244, 238, 216)
        rgb = add(rgb, n * .7)
    elif mat == 'gills':
        if cube.get('pale'):
            rgb = (214, 204, 170) if (u + v * 2) % 3 else (176, 166, 132)
        else:
            root = abs(t['lz']) < cube['box'][5] * .3 and abs(t['lx']) < cube['box'][3] * .36
            if root:
                rgb = GILL_BLOOM
                lit = GLOW_GILL
            elif (u + v * 2) % 4 == 0 and cube['variant'] != 1:
                rgb = GILL_PORE
            else:
                rgb = GILL_DARK
        rgb = add(rgb, n * .5)
    elif mat == 'flesh':
        mottle = kit.smooth(t['gx'], t['gy'], t['gz'], scale=1.1, seed=3)
        rgb = mix(FLESH, MOTTLE, mottle)
        vein = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.8, seed=5)
        scar = kit.smooth(t['gx'], t['gy'], t['gz'], scale=1.3, seed=9)
        if abs(vein - .5) < .035:
            rgb = VEIN
        if scar > .78:
            rgb = ESCHAR
        elif scar > .73:
            rgb = SCAB
        if cube.get('hood'):
            rgb = mix(rgb, (30, 30, 20), .45)
        rgb = add(rgb, n * .6)
    elif mat == 'stem':
        rgb = add((206, 196, 160), n * .6)
        if u == 0:
            rgb = (166, 156, 120)
    elif mat == 'iron':
        # Blackened iron, an olive mould bloom on its edges, single ochre flecks: no brass and no rust red.
        soot = kit.smooth(t['gx'], t['gy'], t['gz'], scale=1.4, seed=7)
        rgb = mix((34, 33, 30), (96, 90, 72), soot ** 2 * .6)
        if cube['part'] == 'staff_head':
            rgb = mix(rgb, (24, 22, 20), .6)
        if (u == 0 or v == 0) and min(t['fw'], t['fh']) >= 3 and t['n'] > .35:
            rgb = mix(rgb, (98, 110, 52), .45)
        if min(t['fw'], t['fh']) >= 3 and 0 < u < t['fw'] - 1 and (u + v * 3 + int(t['n'] * 5)) % 17 == 0:
            rgb = (110, 92, 44)
    elif mat == 'void':
        rgb = (20, 24, 13)
    elif mat == 'bone':
        rgb = add(BONE, n)
        if u == 0:
            rgb = JOINT
    elif mat == 'ichor':
        rgb = add(ICHOR, n * .4)
        if t['side'] and v == 0 and u % 2 == 0:
            rgb = ICHOR_WET
    elif mat == 'root':
        rgb = add(BARK, n * .6)
        if t['face'] == 'down' and (u + v) % 3 == 0:
            rgb = FIBRE
    elif mat == 'staff_wrap':
        # a rot-soaked rag in loose diagonal turns; the soaked texels ichor-dark
        turn = ((u + v) // 2) % 3
        rgb = (WRAP_OCHRE, WRAP_DARK, mix(WRAP_OCHRE, WRAP_DARK, .5))[turn]
        if t['n'] > .82:
            rgb = WRAP_SOAK
        rgb = add(rgb, n * .4)
    elif mat == 'moss':
        rgb = MOSS_UNDER if t['face'] == 'up' else MOSS
        rgb = add(rgb, n * .8)
    elif mat == 'offering':
        # Smouldering rot in the censers: ochre, a cream glow at half strength.
        rgb = add((150, 128, 60), n)
        lit = (120, 112, 80)
    elif mat == 'seam':
        rgb = (40, 34, 28) if (u + v) % 3 else SCAB
    elif mat == 'ash_face':
        rgb = add((206, 196, 172), n)
        if 1 <= t['v'] <= 3 and t['u'] in (1, 2, 4, 5):
            rgb = (40, 36, 30)
    elif mat == 'nodule':
        rgb = (150, 140, 90) if t['face'] != 'up' else (232, 216, 160)
    else:
        rgb = cloth(cube, t)
    if glow:
        return (*lit, 255) if lit else (0, 0, 0, 0)
    return (*rgb, 255)


# ---------------------------------------------------------------- the fire's atlases

CHARRED_GROWTH = ('spore', 'shelf_cap', 'pale_cap', 'gills', 'ichor', 'root', 'flesh', 'offering', 'stem', 'moss',
                  'staff_wrap')
CHARRED_CLOTH = ('blood_cloth', 'ivory_cloth', 'stole')
SCORCHED = (52, 48, 40)
ASH_CREAM = (196, 188, 168)


def charred(cube, t):
    """The growths burned to charcoal with alligator cracks and pale ash rims. The cloth scorches by height (R3.3):
    the hem and lower robe fully, the vestment remnants only at their edges, pale ash cream inside, so in the fire the
    man's vestments still show pale under the flames. The rest stays clear so the base atlas shows through."""
    mat = cube['material']
    if mat in CHARRED_GROWTH:
        cell = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.5, seed=41)
        rgb = (30, 28, 26)
        if abs(cell - .5) < .05:
            rgb = (58, 54, 48)
        if mat in ('shelf_cap', 'pale_cap') and cap_radius(cube, t) > .8:
            rgb = (150, 146, 138)
        return (*add(rgb, (t['n'] - .5) * 8), 255)
    if mat in CHARRED_CLOTH:
        robe = cube.get('robe', False)
        low = tide(t)
        if mat != 'blood_cloth' and not (robe and low > .82):
            rgb = SCORCHED if edge(t) == 0 or t['n'] < .08 else ASH_CREAM
        elif robe:
            rgb = mix((84, 78, 64), SCORCHED, kit.clamp((low - .55) / .3))
        else:
            rgb = (64, 60, 50)
        return (*add(rgb, (t['n'] - .5) * 10), 255)
    return (0, 0, 0, 0)


def embers(cube, t):
    """The fire palette only: the seams, the sacs' splits, the censers' insides, the palm and its nodules."""
    mat = cube['material']
    heat = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.7, seed=59)
    if mat == 'seam':
        return (*(HEAT if heat > .62 else CRIMSON), 255)
    if mat == 'offering':
        return (*(HEAT if heat > .5 else CRIMSON), 255)
    if mat == 'nodule':
        return (*HEAT, 255)
    if mat == 'spore':
        cells = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.6, seed=cube['spore'])
        if cells < .29:
            return (*(CRIMSON if heat > .4 else SOOT), 255)
    return (0, 0, 0, 0)


FACE_NORMALS = {'west': (-1, 0, 0), 'east': (1, 0, 0), 'north': (0, 0, -1), 'south': (0, 0, 1),
                'down': (0, -1, 0), 'up': (0, 1, 0)}


def wrap_centres(rig):
    return [next(p for p in rig.parts if p['name'] == f'staff_wrap_{i}')['pivot'][1] for i in range(len(WRAP_YS))]


def burning(cube, t, wraps):
    """What burns under the flames (R1.7), in the fire palette only: the shelf and cap rims crimson with pink-white
    flecks, the char's alligator cracks glowing pink-white on the caps, the staff wraps in crimson streaks with hot
    cores, the iron heated for a unit either side of each wrap, the censer lid bars' inner faces, the top 1.5 units
    of each crown spire, the hem shelf rims, the robe's lower edge and the sac tears. Everything else is clear."""
    mat = cube['material']
    part = cube['part']
    heat = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.6, seed=61)
    if mat in ('shelf_cap', 'pale_cap'):
        radius = cap_radius(cube, t)
        crack = abs(kit.smooth(t['gx'], t['gy'], t['gz'], scale=.5, seed=41) - .5) < .05
        if radius > .8 or (t['side'] and t['v'] == 0):
            return (*(HEAT if heat > .68 else CRIMSON), 255)
        if crack:
            return (*HEAT, 255)
        if radius > .66 and heat > .55:
            return (*CRIMSON, 255)
        return (0, 0, 0, 0)
    if mat == 'gills' and edge(t) == 0 and heat > .45:
        return (*CRIMSON, 255)
    if mat == 'staff_wrap':
        streak = (t['u'] + t['v']) % 4
        return (*(HEAT if streak == 0 else CRIMSON if streak in (1, 2) else SOOT), 255)
    if mat == 'iron' and part == 'staff':
        y = t['ly']
        near = min(abs(y - c) for c in wraps) - .875 * RIG_SCALE
        if near < .5:
            return (*CRIMSON, 255)
        if near < 1.0 and (t['u'] + t['v']) % 2 == 0:
            return (*CRIMSON, 255)
        return (0, 0, 0, 0)
    if mat == 'iron' and cube.get('lid'):
        x, _, z, w, _, d = cube['box']
        cx, cz = x + w / 2, z + d / 2
        nx, ny, nz = FACE_NORMALS[t['face']]
        if ny != 0 or nx * -cx + nz * -cz > 0:
            return (*(HEAT if heat > .6 else CRIMSON), 255)
        return (0, 0, 0, 0)
    if mat == 'iron' and cube.get('spire'):
        top = cube['spire_top']
        if t['ly'] < top + .5:
            return (*HEAT, 255)
        if t['ly'] < top + 1.5:
            return (*(CRIMSON if (t['u'] + t['v']) % 3 else HEAT), 255)
        return (0, 0, 0, 0)
    if mat == 'offering':
        return (*(HEAT if heat > .45 else CRIMSON), 255)
    if mat == 'spore':
        radius = cube['diameter'] * RIG_SCALE / 2
        cells = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.6, seed=cube['spore'])
        if t['ly'] < -radius * .35 and cells < .36:
            return (*(HEAT if cells < .22 else CRIMSON), 255)
        return (0, 0, 0, 0)
    if cube.get('robe') and t['side']:
        bottom = cube['box'][1] + cube['box'][4]
        if t['ly'] > bottom - .55:
            return (*(HEAT if heat > .7 else CRIMSON), 255)
        if t['ly'] > bottom - 1.05 and heat > .5:
            return (*CRIMSON, 255)
    return (0, 0, 0, 0)


# ---------------------------------------------------------------- regions, growth parts and fire anchors (R1.1)

def region_of(name):
    if name in ('staff', 'staff_head') or name.startswith(('staff_wrap_', 'staff_growth_')):
        return 'STAFF'
    if name in ('censer_left', 'censer_right'):
        return 'CENSER'
    if name.startswith(('back_shelf_', 'back_mushroom', 'growth_mushroom_', 'back_spore_')):
        return 'BACK'
    if name.startswith(('left_shoulder_shelf_', 'right_shoulder_shelf_', 'shoulder_mushroom')) or name in (
            'left_mantle', 'right_mantle', 'shoulder_spore', 'left_elbow_growth', 'right_elbow_growth',
            'knuckle_growth'):
        return 'SHOULDER'
    if name.startswith(('crown_spire_', 'crown_mushroom')) or name in ('head_shelf_left', 'head_shelf_right'):
        return 'CROWN'
    if name in ('spore_0', 'spore_1', 'spore_2', 'spore_3'):
        return 'CHEST'
    if name.startswith(('hem_shelf_', 'robe_panel_')) or name == 'hem_spore':
        return 'HEM'
    return None


def growth_parts(r):
    """The growths that burn down in the fire and regrow in the Embering (each drawn with its children)."""
    names = [p['name'] for p in r.parts]
    sacs = ['spore_0', 'spore_1', 'spore_2', 'spore_3', 'shoulder_spore', 'hem_spore', 'back_spore_0', 'back_spore_1']
    shelves = [n for n in names if n.startswith(('back_shelf_', 'hem_shelf_', 'left_shoulder_shelf_',
                                                  'right_shoulder_shelf_', 'staff_growth_'))]
    others = ['growth_mushroom_0', 'growth_mushroom_1', 'growth_mushroom_2', 'growth_mushroom_3', 'crown_mushroom',
              'shoulder_mushroom', 'back_mushroom', 'head_shelf_left', 'head_shelf_right', 'left_elbow_growth',
              'right_elbow_growth', 'knuckle_growth']
    return sacs + shelves + others


UP = (0.0, -1.0, 0.0)


def unit(v):
    length = math.sqrt(sum(c * c for c in v))
    return tuple(c / length for c in v)


def fire_table(r):
    """The fire anchor table: about seventy named points on the generator's parts (R1.1), each with an outward normal
    (in its part's units), a steady-burn tongue height (blocks), a region and its kindle tick on the Ignition's clock
    (R1.2), plus each growth part's kindle tick, Embering regrowth step and whether the burn eats it further (R1.8)."""
    import numpy as np
    world = kit.transforms(r)
    parts = {p['name']: p for p in r.parts}

    def to_world(part, point):
        return (world[part] @ np.array([*point, 1.0]))[:3]

    def local_dir(part, direction):
        return tuple(world[part][:3, :3].T @ np.array(direction))

    def outward(part, point, up=.65):
        p = to_world(part, point)
        horizontal = np.array([p[0], 0.0, p[2]])
        length = np.linalg.norm(horizontal)
        horizontal = horizontal / length if length > 1e-6 else np.zeros(3)
        return unit(local_dir(part, tuple(horizontal * .75 + np.array(UP) * up)))

    def main(part):
        return parts[part]['cubes'][0]['box']

    def shelf_rim(part):
        x, y, z, w, h, d = main(part)
        cx, cz = x + w / 2, z + d / 2
        n = outward(part, (cx, y, cz), up=0)
        dx, dz = n[0], n[2]
        if abs(dx) + abs(dz) < 1e-6:
            dz = 1.0
        s = .85 / math.sqrt((dx / (w / 2)) ** 2 + (dz / (d / 2)) ** 2)
        point = (cx + dx * s, y, cz + dz * s)
        return point, outward(part, point)

    def tip(part):
        x, y, z, w, h, d = main(part)
        return (x + w / 2, y, z + d / 2), unit(local_dir(part, UP))

    def tear(part):
        x, y, z, w, h, d = main(part)
        point = (x + w / 2, y + .25, z + d / 2)
        return point, outward(part, point, up=1.2)

    anchors = []

    def anchor(part, point, normal, size, region, kindle):
        anchors.append(dict(part=part, point=tuple(point), normal=unit(normal), size=size, region=region,
                            kindle=min(29.0, kindle)))

    # STAFF (12): the four wraps, the three prongs, the head bar's ends, the three staff growths. The fire runs up from
    # the grip wrap to the head at a tenth of its length a tick from 16, and down at half that speed.
    grip = parts[f'staff_wrap_{GRIP_WRAP}']['pivot'][1]
    head = parts['staff_head']['pivot'][1]
    reach = grip - head

    def staff_kindle(y):
        return 16 + (10 * (grip - y) / reach if y <= grip else 20 * (y - grip) / reach)

    for i in range(len(WRAP_YS)):
        name = f'staff_wrap_{i}'
        anchor(name, (0, 0, 0), (0, -1, 0), .45, 'STAFF', min(28.0, staff_kindle(parts[name]['pivot'][1])))
    prongs = [c for c in parts['staff_head']['cubes'] if c.get('prong')]
    for k, cube in enumerate(prongs):
        x, y, z, w, h, d = cube['box']
        anchor('staff_head', (x + w / 2, y, z + d / 2), (0, -1, 0), (.5, .65, .55)[k], 'STAFF', 26.25 + k * .25)
    bx, by, bz, bw, bh, bd = main('staff_head')
    for k, sign in enumerate((-1, 1)):
        anchor('staff_head', (bx + (bw if sign > 0 else 0), by + bh / 2, bz + bd / 2), (sign, .35, 0), .3, 'STAFF',
               26 + k * .1)
    for i in range(3):
        name = f'staff_growth_{i}'
        point, normal = shelf_rim(name)
        anchor(name, point, normal, .4, 'STAFF', min(28.0, staff_kindle(parts[name]['pivot'][1])))

    # CENSER (2): the coals in each bowl, lit inside at 22.
    for k, name in enumerate(('censer_left', 'censer_right')):
        x, y, z, w, h, d = next(c for c in parts[name]['cubes'] if c['material'] == 'offering')['box']
        anchor(name, (x + w / 2, y, z + d / 2), (0, -1, 0), .65, 'CENSER', 22 + k * .5)

    # BACK (19): the colony's root catches at 20 and the fire spreads shelf by shelf, by distance, to the outermost
    # shelves by 28; over the root stands the back column.
    root_point = (0.0, -1.75, 2.5)
    root_world = to_world('back_colony', root_point)
    back = []
    for i in range(11):
        point, normal = shelf_rim(f'back_shelf_{i}')
        back.append((f'back_shelf_{i}', point, normal, .7))
    for name in ('back_mushroom', 'growth_mushroom_0', 'growth_mushroom_1', 'growth_mushroom_2', 'growth_mushroom_3'):
        point, normal = shelf_rim(name + '_cap')
        back.append((name + '_cap', point, normal, .55))
    for name in ('back_spore_0', 'back_spore_1'):
        point, normal = tear(name)
        back.append((name, point, normal, .6))
    distances = [np.linalg.norm(to_world(part, point) - root_world) for part, point, _, _ in back]
    near, far = min(distances), max(distances)
    anchor('back_colony', root_point, local_dir('back_colony', (0, -.6, .8)), 1.5, 'BACK', 20)
    for (part, point, normal, size), distance in zip(back, distances):
        anchor(part, point, normal, size, 'BACK', 20.5 + 7.5 * (distance - near) / (far - near))

    # SHOULDER (15): the shoulders catch at 18 and the fire spreads down the arms to the elbows by 24, the knuckle 26.
    for k, side in enumerate(('left', 'right')):
        for i in range(3 if side == 'left' else 5):
            point, normal = shelf_rim(f'{side}_shoulder_shelf_{i}')
            anchor(f'{side}_shoulder_shelf_{i}', point, normal, .25, 'SHOULDER', 18 + k * .25 + i * .3)
        x, y, z, w, h, d = main(f'{side}_mantle')
        point = (x + w / 2, y, z + d / 2)
        anchor(f'{side}_mantle', point, outward(f'{side}_mantle', point, up=1.0), .2, 'SHOULDER', 18 + k * .5)
    point, normal = shelf_rim('shoulder_mushroom_cap')
    anchor('shoulder_mushroom_cap', point, normal, .25, 'SHOULDER', 20)
    point, normal = tear('shoulder_spore')
    anchor('shoulder_spore', point, normal, .3, 'SHOULDER', 20.5)
    for k, side in enumerate(('left', 'right')):
        point, normal = shelf_rim(f'{side}_elbow_growth')
        anchor(f'{side}_elbow_growth', point, normal, .25, 'SHOULDER', 23.5 + k * .5)
    point, normal = shelf_rim('knuckle_growth')
    anchor('knuckle_growth', point, normal, .2, 'SHOULDER', 26)

    # CROWN (9): a candelabrum, the spires lighting one a tick from 24 to 29; the head shelves and the crown mushroom.
    for i in range(6):
        point, normal = tip(f'crown_spire_{i}')
        anchor(f'crown_spire_{i}', point, normal, .28, 'CROWN', 24 + i)
    for k, name in enumerate(('head_shelf_left', 'head_shelf_right')):
        point, normal = shelf_rim(name)
        anchor(name, point, normal, .38, 'CROWN', 26.5 + k)
    point, normal = shelf_rim('crown_mushroom_cap')
    anchor('crown_mushroom_cap', point, normal, .3, 'CROWN', 28)

    # CHEST (6): fire breathing out of the split sacs from 16, the two upper seam ends.
    for i in range(4):
        point, normal = tear(f'spore_{i}')
        anchor(f'spore_{i}', point, normal, .4, 'CHEST', 16 + i * .25)
    seams = parts['eschar_seams']['cubes']
    for k in (0, 2):
        x, y, z, w, h, d = seams[k]['box']
        point = (x + w / 2, y, z)
        anchor('eschar_seams', point, outward('eschar_seams', point, up=.4), .3, 'CHEST', 16.5)

    # HEM (7): low tongues on the hem shelves from 26.
    for i in range(6):
        point, normal = shelf_rim(f'hem_shelf_{i}')
        anchor(f'hem_shelf_{i}', point, normal, .22, 'HEM', 26 + i * .25)
    point, normal = tear('hem_spore')
    anchor('hem_spore', point, normal, .22, 'HEM', 26.5)

    ranked = sorted(range(len(anchors)), key=lambda i: (anchors[i]['kindle'], i))
    for order, i in enumerate(ranked):
        anchors[i]['order'] = order

    # Each growth part's kindle tick (the earliest anchor on it or its cap, else its region's start), its regrowth
    # step (the back from the root outward, the arms and the crown from their tips inward, 3 ticks a step) and
    # whether the burn eats it further (the two outermost back shelves and the back mushroom).
    kindle = {}
    for a in anchors:
        base = a['part'][:-4] if a['part'].endswith('_cap') else a['part']
        kindle[base] = min(kindle.get(base, 99.0), a['kindle'])
    growth = []
    back_shelves = sorted([g for g in growth_parts(r) if g.startswith('back_shelf_')], key=lambda g: kindle[g])
    for g in growth_parts(r):
        region = region_of(g)
        if region == 'BACK':
            # the colony regrows shelf by shelf from its root outward; a sac or mushroom with the shelves round it
            step = back_shelves.index(g) if g in back_shelves else sum(1 for s in back_shelves if kindle[s] < kindle[g])
        elif g == 'knuckle_growth' or g in ('crown_mushroom',):
            step = 0
        elif g.endswith('_elbow_growth') or g.startswith('head_shelf_'):
            step = 1
        elif g in ('shoulder_spore', 'shoulder_mushroom'):
            step = 2
        elif g.startswith(('left_shoulder_shelf_', 'right_shoulder_shelf_')):
            step = 3 + int(g[-1]) % 2
        else:
            step = int(g[-1]) if g[-1].isdigit() else 0
        tick = kindle.get(g, {'STAFF': 22.0, 'CHEST': 16.0, 'HEM': 26.0}.get(region, 20.0))
        growth.append(dict(part=g, kindle=tick, step=step,
                           eaten=g in ('back_shelf_9', 'back_shelf_10', 'back_mushroom')))
    for g in growth:
        # the kindle step PutricielBodyFireRules.blacken reads: one tick a step from the Ignition's tick 10, so each
        # part blackens over the six ticks before the fire reaches it
        g['order'] = max(0, min(14, round(g['kindle'] - 16)))
    for a in anchors:
        a['palm'] = a['region'] == 'SHOULDER' and ('right_arm' in ancestors(r, a['part'])
                                                   or a['part'].startswith('right_'))
    return anchors, growth


def ancestors(r, name):
    parents = {p['name']: p['parent'] for p in r.parts}
    chain_names = []
    while name is not None:
        chain_names.append(name)
        name = parents[name]
    return list(reversed(chain_names))


def descendants(r, name):
    out = [name]
    for p in r.parts:
        if p['parent'] in out:
            out.append(p['name'])
    return out


def fire_java(r, anchors, growth):
    """The generated fields and methods the pose code and PutricielFireAnchors read."""
    def floats(values):
        return ', '.join(f(v) for v in values)

    def ints(values):
        return ', '.join(str(v) for v in values)

    def strings(values):
        return ', '.join(f'"{v}"' for v in values)

    def wrapped(text, indent='\t\t\t'):
        lines, line = [], ''
        for item in text.split(', '):
            if len(line) + len(item) > 104:
                lines.append(line.rstrip())
                line = ''
            line += item + ', '
        lines.append(line.rstrip(', '))
        return ('\n' + indent).join(lines)

    regions = [region_of(p['name']) for p in r.parts]
    fields = f'''

	// ---------------------------------------------------------------- generated by putriciel.py: the body fire (R1)
	/** The fire anchors (design R1.1), in table order. Points and normals are in their part's units. */
	static final int ANCHOR_COUNT = {len(anchors)};
	static final String[] ANCHOR_PARTS = {{
			{wrapped(strings(a['part'] for a in anchors))}}};
	static final float[] ANCHOR_POINTS = {{
			{wrapped(floats(c for a in anchors for c in a['point']))}}};
	static final float[] ANCHOR_NORMALS = {{
			{wrapped(floats(c for a in anchors for c in a['normal']))}}};
	/** Each anchor's tongue height at the steady burn, blocks (PutricielFireAnchors fits it to its region's range). */
	static final float[] ANCHOR_HEIGHTS = {{
			{wrapped(floats(a['size'] for a in anchors))}}};
	/** Whether each anchor is on the offering palm's own arm and shoulder. */
	static final boolean[] ANCHOR_PALM = {{
			{wrapped(', '.join('true' if a['palm'] else 'false' for a in anchors))}}};
	/** Each anchor's region, as {{@code PutricielFireAnchors.Region}}'s ordinal. */
	static final int[] ANCHOR_REGIONS = {{
			{wrapped(ints(REGIONS.index(a['region']) for a in anchors))}}};
	/** Each anchor's place in the body's kindle order (0 catches first) and its kindle tick on the Ignition clock. */
	static final int[] ANCHOR_ORDERS = {{
			{wrapped(ints(a['order'] for a in anchors))}}};
	static final float[] ANCHOR_KINDLE = {{
			{wrapped(floats(a['kindle'] for a in anchors))}}};
	/** The growth parts that burn down and regrow (design R1.8), each drawn with its children. */
	static final int GROWTH_COUNT = {len(growth)};
	static final String[] GROWTH_PARTS = {{
			{wrapped(strings(g['part'] for g in growth))}}};
	/** Each growth's kindle tick on the Ignition clock. */
	static final float[] GROWTH_KINDLE = {{
			{wrapped(floats(g['kindle'] for g in growth))}}};
	/** Each growth's kindle step, one tick a step from the Ignition's tick 10 (PutricielBodyFireRules' order). */
	static final int[] GROWTH_ORDERS = {{
			{wrapped(ints(g['order'] for g in growth))}}};
	/** The Embering's regrowth rank, three ticks a step: the back from its root, the arms and crown from their tips. */
	static final int[] GROWTH_STEPS = {{
			{wrapped(ints(g['step'] for g in growth))}}};
	/** The growths the burn eats further, 0.35 to 0.2 through the burn-down. */
	static final boolean[] GROWTH_EATEN = {{
			{wrapped(', '.join('true' if g['eaten'] else 'false' for g in growth))}}};
	/** Every part with a region, and its region's ordinal. */
	static final String[] REGION_PARTS = {{
			{wrapped(strings(p['name'] for p, g in zip(r.parts, regions) if g))}}};
	static final int[] REGION_OF = {{
			{wrapped(ints(REGIONS.index(g) for g in regions if g))}}};'''

    def parts_array(names):
        return 'new ModelPart[]{' + ', '.join(camel(n) for n in names) + '}'

    anchor_chains = ',\n\t\t\t\t'.join(parts_array(ancestors(r, a['part'])) for a in anchors)
    growth_chains = ',\n\t\t\t\t'.join(parts_array(ancestors(r, g['part'])[:-1]) for g in growth)
    growth_trees = ',\n\t\t\t\t'.join(parts_array(descendants(r, g['part'])) for g in growth)
    by_region = []
    for region in REGIONS:
        names = [p['name'] for p, g in zip(r.parts, regions) if g == region]
        by_region.append(parts_array(names))
    region_parts = ',\n\t\t\t\t'.join(by_region)
    methods = f'''

	// ---------------------------------------------------------------- generated by putriciel.py: the fire's part chains
	/** Each fire anchor's part chain from the root, in table order. */
	private ModelPart[][] anchorChains() {{
		return new ModelPart[][]{{
				{anchor_chains}}};
	}}

	/** Each growth part's ancestors from the root (not the part itself), in table order. */
	private ModelPart[][] growthChains() {{
		return new ModelPart[][]{{
				{growth_chains}}};
	}}

	/** Each growth part and its descendants, in table order. */
	private ModelPart[][] growthTrees() {{
		return new ModelPart[][]{{
				{growth_trees}}};
	}}

	/** The parts of each region, by {{@code PutricielFireAnchors.Region}}'s ordinal. */
	private ModelPart[][] regionParts() {{
		return new ModelPart[][]{{
				{region_parts}}};
	}}
'''
    return fields, methods


# ---------------------------------------------------------------- palette statistics (R3.2)

def luminance(rgb):
    r, g, b = rgb
    return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255


def family(rgb):
    """'red', 'ochre' (ochre and muted yellow), 'green' (mould green) or None; matches PutricielPaletteTest."""
    r, g, b = rgb
    hue, sat, val = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    hue *= 360
    if hue > 180:
        hue -= 360
    if -20 <= hue <= 15 and sat >= .4 and r >= 70:
        return 'red'
    if 30 <= hue <= 58 and sat >= .45 and val >= .45:
        return 'ochre'
    if 78 <= hue <= 150 and sat >= .25 and val >= .2:
        return 'green'
    return None


def palette_stats(image):
    counts = dict(n=0, dark=0, mid=0, light=0, ochre=0, green=0, red=0, low_hue=0)
    for r, g, b, a in image.getdata():
        if a == 0:
            continue
        counts['n'] += 1
        lum = luminance((r, g, b))
        counts['dark' if lum < .22 else 'mid' if lum <= .6 else 'light'] += 1
        fam = family((r, g, b))
        if fam:
            counts[fam] += 1
        hue, sat, _ = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        if sat > .15 and 15 < hue * 360 < 30:
            counts['low_hue'] += 1
    n = counts['n']
    return {k: (v / n if k != 'n' else v) for k, v in counts.items()}


def check_palette(base, glow, char_atlas, ember_atlas, burn_atlas):
    stats = palette_stats(base)
    print('palette: ' + ', '.join(f'{k} {v:.1%}' for k, v in stats.items() if k != 'n'))
    assert stats['red'] == 0, 'a red texel in the base atlas'
    assert stats['low_hue'] == 0, 'a warm colour under hue 30 in the base atlas'
    assert .25 <= stats['dark'] <= .35, f'dark band {stats["dark"]:.3f} outside 25 to 35%'
    assert .40 <= stats['mid'] <= .50, f'mid band {stats["mid"]:.3f} outside 40 to 50%'
    assert .20 <= stats['light'] <= .30, f'light band {stats["light"]:.3f} outside 20 to 30%'
    assert stats['ochre'] <= .15, f'ochre and muted yellow at {stats["ochre"]:.3f}, over the 15% brass guard'
    assert .05 <= stats['green'] <= .10, f'mould green at {stats["green"]:.3f}, outside 5 to 10%'
    for image, name in ((glow, 'glow'), (char_atlas, 'charred')):
        assert palette_stats(image)['red'] == 0, f'a red texel in the {name} atlas'
    for image, name in ((ember_atlas, 'embers'), (burn_atlas, 'burning')):
        for r, g, b, a in image.getdata():
            assert a == 0 or (r, g, b) in FIRE, f'{name} atlas texel {(r, g, b)} is not crimson, pink-white or soot'
    return stats


# ---------------------------------------------------------------- the preview

def palette_sheet(rig, base, stats, char_atlas, burn_atlas, posed):
    """build/putriciel_palette.png: a swatch per material, the luminance histogram against the band targets, and
    the burning atlas over a charred pose."""
    materials = {}
    for part in rig.parts:
        for cube in part['cubes']:
            materials.setdefault(cube['material'], []).append(cube)
    sheet = Image.new('RGB', (1260, 900), (30, 32, 40))
    draw = ImageDraw.Draw(sheet)
    for k, (mat, cubes) in enumerate(sorted(materials.items())):
        x0, y0 = 20 + (k % 6) * 200, 20 + (k // 6) * 120
        u, v = cubes[0]['uv']
        _, _, _, w, hgt, d = cubes[0]['box']
        crop = base.crop((u * kit.SCALE, v * kit.SCALE, int((u + 2 * (w + d)) * kit.SCALE),
                          int((v + hgt + d) * kit.SCALE)))
        crop = crop.resize((max(8, crop.width * 4), max(8, crop.height * 4)), Image.NEAREST)
        crop = crop.crop((0, 0, min(180, crop.width), min(80, crop.height)))
        sheet.paste(crop, (x0, y0 + 16), crop)
        draw.text((x0, y0), mat, fill=(223, 202, 157))
    hist = [0] * 32
    for r, g, b, a in base.getdata():
        if a:
            hist[min(31, int(luminance((r, g, b)) * 32))] += 1
    top = max(hist)
    hx, hy = 20, 620
    for i, count in enumerate(hist):
        height = int(200 * count / top)
        lum = i / 32
        colour = (120, 120, 140) if lum < .22 else (170, 170, 120) if lum <= .6 else (230, 226, 200)
        draw.rectangle((hx + i * 18, hy + 200 - height, hx + i * 18 + 14, hy + 200), fill=colour)
    draw.text((hx, hy - 20), f'luminance: dark {stats["dark"]:.0%} (25-35)  mid {stats["mid"]:.0%} (40-50)  light '
                             f'{stats["light"]:.0%} (20-30)  ochre/yellow {stats["ochre"]:.0%} (<=15)  mould green '
                             f'{stats["green"]:.0%} (5-10)', fill=(223, 202, 157))
    burned = Image.alpha_composite(base.convert('RGBA'), char_atlas.convert('RGBA'))
    lit = burned.copy()
    lit.paste(burn_atlas, (0, 0), burn_atlas)
    tile = kit.render_quads(posed, lit, -.6, -.05, 300)
    sheet.paste(tile, (940, 580))
    draw.text((944, 584), 'BURNING OVER CHARRED', fill=(223, 202, 157))
    sheet.save(ROOT / 'build/putriciel_palette.png')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    assert kit.SCALE * 16 / RENDER_SCALE == 16, 'Putriciel must land at 16 px per block'
    rig = saint()
    size = layout(rig, 256)
    wraps = wrap_centres(rig)
    base, glow, char_atlas, ember_atlas, burn_atlas = paint_atlases(
        rig, size, [paint, lambda c, t: paint(c, t, True), charred, embers, lambda c, t: burning(c, t, wraps)])
    anchors, growth = fire_table(rig)
    names = {p['name'] for p in rig.parts}
    assert all(a['part'] in names for a in anchors), 'every anchor on a part that exists'
    assert all(region_of(a['part'][:-4] if a['part'].endswith('_cap') else a['part']) in (a['region'], None)
               for a in anchors), 'every anchor on a part of its region'
    assert 66 <= len(anchors) <= 74, f'{len(anchors)} fire anchors'
    stats = check_palette(base, glow, char_atlas, ember_atlas, burn_atlas)
    TEXTURES.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURES / 'putriciel.png')
    glow.save(TEXTURES / 'putriciel_glow.png')
    char_atlas.save(TEXTURES / 'putriciel_charred.png')
    ember_atlas.save(TEXTURES / 'putriciel_embers.png')
    burn_atlas.save(TEXTURES / 'putriciel_burning.png')
    fields, methods = fire_java(rig, anchors, growth)
    source = entity_java_source(rig, size, 'PutricielEntity', 'putriciel.py',
        package='com.vincenthuto.hemomancy.client.model.entity.boss.putriciel',
        entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel',
        extra_imports='import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;\n'
                      'import com.vincenthuto.hemomancy.common.util.HemoEasing;\n'
                      'import com.mojang.blaze3d.vertex.VertexConsumer;\n'
                      'import org.joml.Quaternionf;\nimport org.joml.Matrix3f;\n'
                      'import static com.vincenthuto.hemomancy.common.util.HemoEasing.catmullRom;\n',
        extra_fields=fields,
        extra_methods=methods + (ROOT / 'tools/model_export/putriciel_anim.java').read_text())
    source = source.replace('public class PutricielModel extends HierarchicalModel<PutricielEntity>',
                            'public class PutricielModel<T extends PutricielEntity> extends HierarchicalModel<T>')
    source = source.replace('import net.minecraft.world.entity.HumanoidArm;\n', '')
    JAVA.write_text(source, encoding='utf-8')
    subprocess.run(['node', str(ROOT / 'tools/model_export/java_model_to_bbmodel.mjs'), '--source', str(JAVA),
                    '--texture', 'textures/entity/boss/putriciel/putriciel.png', '--output', str(BBMODEL)],
                   cwd=ROOT, check=True)
    project = json.loads(BBMODEL.read_text())
    project['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(
        (TEXTURES / 'putriciel.png').read_bytes()).decode('ascii')
    BBMODEL.write_text(json.dumps(project, indent=2) + '\n', encoding='utf-8')
    print(f'Putriciel: {sum(len(p["cubes"]) for p in rig.parts)} cubes; {len(rig.parts)} joints; atlas {base.size}; '
          f'{kit.SCALE * 16 / RENDER_SCALE:.0f} px per block; {len(anchors)} fire anchors; {len(growth)} growth parts')
    if args.preview:
        def posed(overrides, hidden=()):
            world = kit.transforms(rig, overrides)
            for name, parent in (('staff', 'left_hand'), ('right_hand', 'right_forearm')):
                part = next(p for p in rig.parts if p['name'] == name)
                rotation = euler(world[parent][:3, :3].T)
                overrides[name] = tuple(a - b for a, b in zip(rotation, part['rotation']))
            return quads(rig, hidden=hidden, overrides=overrides)
        stable = posed({})
        fire = posed({'chest': (-16, 0, 0), 'head': (-4, 0, 0)})
        offering = posed({'chest': (6, 0, 0), 'right_arm': (-40, 0, 10), 'left_thigh': (-60, 0, 0),
                          'left_shin': (80, 0, 0), 'right_thigh': (-20, 0, 0), 'right_shin': (30, 0, 0)})
        kit.preview_sheet([(stable, base, 0, 0), (stable, base, -.65, -.1), (stable, base, math.pi, 0),
                           (stable, base, math.pi / 2, 0), (fire, char_atlas, -.3, 0), (offering, base, -.3, 0)],
                          ROOT / 'build/putriciel_preview.png', cols=3)
        preview = Image.open(ROOT / 'build/putriciel_preview.png')
        draw = ImageDraw.Draw(preview)
        for i, label in enumerate(('FRONT', 'THREE QUARTER', 'BACK', 'SIDE', 'FIRE (CHARRED)', 'OFFERING')):
            draw.text(((i % 3) * 420 + 14, (i // 3) * 420 + 12), label, fill=(223, 202, 157))
        preview.save(ROOT / 'build/putriciel_preview.png')
        kit.render_quads(stable, base, .35, -.08, 1100).save(ROOT / 'build/putriciel_portrait.png')
        palette_sheet(rig, base, stats, char_atlas, burn_atlas, fire)


if __name__ == '__main__':
    main()

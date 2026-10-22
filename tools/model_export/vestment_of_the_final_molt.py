"""Vestment of the Final Molt: the Enlightened Unstained's luna moth hood and wing cloak.

Emits VestmentOfTheFinalMoltArmorModel.java (box-UV mesh plus its cloak animation) and the painted armor texture
(one texel per model unit, the mod's block density). Wings, tails, sleeve wings and antenna plumes are zero-depth planes whose silhouettes, venation and
eyespots are painted from each texel's position on the plane, so the shape lives in the alpha channel.

    python tools/model_export/vestment_of_the_final_molt.py            # write model + texture
    python tools/model_export/vestment_of_the_final_molt.py --preview  # also write build/vestment_of_the_final_molt_preview.png

Then regenerate the Blockbench project:
    node tools/model_export/java_model_to_bbmodel.mjs src/main/java/com/vincenthuto/hemomancy/client/model/armor/VestmentOfTheFinalMoltArmorModel.java --texture textures/models/armor/vestment_of_the_final_molt_layer_1.png --out src/main/resources/assets/hemomancy/models/item/bbmodel

Palette: cocoon ivory, white thorax fur, celadon wings over verdigris roots, a dusky lilac costa, cream margins
and still-water eyespot windows. No blood red, no brass.
"""
from pathlib import Path
import argparse
import math

from PIL import Image

from unstained_weapons import add, ceil, f, face_rects, h, local_point, mix, render_quads, rot_matrix

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/armor/VestmentOfTheFinalMoltArmorModel.java'
TEXTURE = ASSETS / 'textures/models/armor/vestment_of_the_final_molt_layer_1.png'
SCALE = 1           # texture pixels per model unit: armor renders 1:1, so this is the mod's 16 px/block block density
ATLAS_WIDTH = 128   # declared (model-unit) atlas width; the PNG is SCALE times this


# ---------------------------------------------------------------- rig

class Rig:
    def __init__(self):
        self.parts = []

    def part(self, name, parent, pivot=(0, 0, 0), rotation=(0, 0, 0)):
        self.parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=[]))
        return name

    def cube(self, part, box, material, **extra):
        next(p for p in self.parts if p['name'] == part)['cubes'].append(dict(box=box, material=material, **extra))

    def mirrored(self, part, box, material, **extra):
        """Add a cube and its mirror (x flipped) on the part named with right_ in place of left_."""
        self.cube(part, box, material, side=1, **extra)
        x, y, z, w, hgt, d = box
        self.cube(part.replace('left_', 'right_'), (-x - w, y, z, w, hgt, d), material, side=-1, **extra)


def sided(rig, name, parent, pivot, rotation):
    """Left/right pair of parts; rotation is given for the left (+x) side."""
    x, y, z = pivot
    rx, ry, rz = rotation
    rig.part('left_' + name, parent.replace('right_', 'left_'), (x, y, z), (rx, ry, rz))
    rig.part('right_' + name, parent.replace('left_', 'right_'), (-x, y, z), (rx, -ry, -rz))


def vestment():
    r = Rig()
    r.part('head', None)
    r.part('hat', None)
    r.part('body', None)
    r.part('right_arm', None, (-5, 2, 0))
    r.part('left_arm', None, (5, 2, 0))
    r.part('right_leg', None, (-1.9, 12, 0))
    r.part('left_leg', None, (1.9, 12, 0))

    # ---- raised hood: a cocoon of wound silk, layered so it reads round, framed in thorax fur.
    r.part('hood_up', 'head')
    r.cube('hood_up', (-4.85, -8.85, -4.35, 9.7, 9.1, 9.0), 'hood_shell')
    r.cube('hood_up', (-4.4, -8.4, -3.9, 8.8, 8.6, 8.4), 'hood_lining')
    r.cube('hood_up', (-4.35, -9.6, -3.85, 8.7, 0.75, 8.2), 'cocoon')
    r.cube('hood_up', (-4.35, -8.35, 4.65, 8.7, 7.5, 0.6), 'cocoon')
    r.cube('hood_up', (-3.6, -10.1, -3.0, 7.2, 0.5, 6.6), 'cocoon')
    r.cube('hood_up', (-3.25, -5.5, 5.25, 6.5, 10.5, 0.75), 'hood_fall')
    r.mirrored('hood_up', (4.85, -7.75, -3.5, 0.5, 7.25, 7.5), 'cocoon')
    r.cube('hood_up', (-4.6, -9.35, -5.6, 9.2, 1.6, 1.4), 'hood_brim')
    r.cube('hood_up', (-1.25, -10.1, -6.1, 2.5, 1.25, 1.25), 'cocoon')
    r.cube('hood_up', (-4.0, -7.95, -5.15, 8.0, 0.75, 0.85), 'fur')
    r.mirrored('hood_up', (3.55, -7.75, -5.1, 1.3, 8.0, 1.0), 'fur')
    r.part('left_hood_antenna', 'hood_up', (1.6, -9.6, -4.6), (-35, 0, 22))
    r.part('right_hood_antenna', 'hood_up', (-1.6, -9.6, -4.6), (-35, 0, -22))
    antenna(r, 'hood_antenna')

    # ---- body: ivory robe bound in cocoon thread, a fur collar and yoke, the moth's white abdomen down the spine.
    r.cube('body', (-4.5, -0.25, -2.5, 9, 12.5, 5), 'robe')
    r.cube('body', (-1.5, 1.5, -2.85, 3, 10.5, 0.5), 'placket')
    r.cube('body', (-1.25, 2.0, -3.4, 2.5, 2.5, 0.75), 'brooch')
    r.cube('body', (-4.75, -0.75, -2.9, 9.5, 2.5, 5.8), 'fur')
    r.cube('body', (-4.25, -0.5, 2.6, 8.5, 3.25, 1.5), 'fur')
    r.cube('body', (-1.25, 2.75, 2.55, 2.5, 9.75, 1.3), 'abdomen')
    r.cube('body', (-4.75, 8.25, -2.75, 9.5, 1.0, 5.5), 'sash')
    r.cube('body', (-4.65, 10.0, -2.7, 9.3, 0.75, 5.4), 'sash')
    r.part('left_front_panel', 'body', (0.25, 11.0, -2.6), (0, 0, -4))
    r.part('right_front_panel', 'body', (-0.25, 11.0, -2.6), (0, 0, 4))
    r.mirrored('left_front_panel', (0, 0, -0.5, 4.0, 8.5, 0.5), 'panel')

    # ---- lowered hood: the cocoon slumps into a collar roll; the antenna plumes rise behind the neck.
    r.part('hood_down', 'body')
    r.cube('hood_down', (-4.5, -2.0, 1.75, 9, 2.75, 2.5), 'cocoon')
    r.mirrored('hood_down', (3.75, -1.75, -2.5, 1.25, 2.25, 4.25), 'cocoon')
    r.cube('hood_down', (-3.5, 0.75, 3.75, 7, 3, 0.75), 'hood_drape')
    r.part('left_collar_antenna', 'hood_down', (1.6, -1.5, 4.0), (-50, 0, 24))
    r.part('right_collar_antenna', 'hood_down', (-1.6, -1.5, 4.0), (-50, 0, -24))
    antenna(r, 'collar_antenna')

    # ---- wings: forewings over hindwings, hindwings trailing two-segment swallowtails.
    sided(r, 'forewing', 'body', (1.0, 0.75, 3.1), (12, -18, 12))
    r.mirrored('left_forewing', (0, -0.5, 0, 11, 15.5, 0), 'wing', shape='forewing')
    sided(r, 'hindwing', 'body', (1.0, 6.5, 2.9), (14, -11, 4))
    r.mirrored('left_hindwing', (0, -0.5, 0, 9.5, 12.5, 0), 'wing', shape='hindwing')
    sided(r, 'tail', 'left_hindwing', (5.5, 11.0, 0.02), (10, 0, -8))
    r.mirrored('left_tail', (-2, 0, 0, 4, 6.5, 0), 'wing', shape='tail')
    sided(r, 'tail_tip', 'left_tail', (0.75, 6.25, 0.02), (12, 0, -16))
    r.mirrored('left_tail_tip', (-2, 0, 0, 4, 5.5, 0), 'wing', shape='tail_tip')

    # ---- sleeves: fur caps, ivory sleeves with bell cuffs, a small hindwing hanging behind each arm.
    r.cube('right_arm', (-3.35, -2.35, -2.35, 4.7, 9.0, 4.7), 'robe')
    r.cube('right_arm', (-3.6, 6.25, -2.6, 5.2, 3.0, 5.2), 'cuff')
    r.cube('right_arm', (-3.7, -2.75, -2.7, 5.4, 2.25, 5.4), 'fur')
    r.cube('left_arm', (-1.35, -2.35, -2.35, 4.7, 9.0, 4.7), 'robe')
    r.cube('left_arm', (-1.6, 6.25, -2.6, 5.2, 3.0, 5.2), 'cuff')
    r.cube('left_arm', (-1.7, -2.75, -2.7, 5.4, 2.25, 5.4), 'fur')
    r.part('right_sleeve_wing', 'right_arm', (-3.6, -1.0, 1.0), (15, 0, 10))
    r.cube('right_sleeve_wing', (0, -0.5, 0, 0, 11.5, 5.5), 'wing', shape='sleeve', side=-1)
    r.part('left_sleeve_wing', 'left_arm', (3.6, -1.0, 1.0), (15, 0, -10))
    r.cube('left_sleeve_wing', (0, -0.5, 0, 0, 11.5, 5.5), 'wing', shape='sleeve', side=1)
    return r


def antenna(r, name):
    """Bipectinate plume: a cream rachis with a feathered comb plane facing forward."""
    r.mirrored('left_' + name, (-0.25, -6.5, -0.25, 0.5, 6.5, 0.5), 'antenna_shaft')
    r.mirrored('left_' + name, (-1.5, -6.5, 0, 3.0, 6.0, 0), 'antenna_feather')


# ---------------------------------------------------------------- wing shapes (a = outward/back, b = down)

FOREWING = [(0, 0.2), (2.5, -0.2), (5.5, 0.1), (8.2, 0.7), (10.2, 1.4), (10.9, 2.4), (10.6, 3.8), (9.8, 6.2),
            (8.8, 8.8), (7.6, 11.2), (6.6, 13.0), (5.4, 14.2), (3.6, 14.8), (1.8, 14.3), (0.6, 12.6), (0.1, 9.0),
            (0, 4)]
FOREWING_COSTA = FOREWING[:6]
HINDWING = [(0, 0), (3, -0.4), (6, 0), (8.3, 1.0), (9.3, 3.0), (9.2, 5.6), (8.4, 8.0), (7.2, 10.0), (6.4, 11.4),
            (4.6, 11.6), (3.0, 10.8), (1.4, 9.2), (0.3, 6.6), (0, 3)]
SLEEVE = [(0, 0), (2.5, -0.2), (4.6, 0.6), (5.4, 2.4), (5.2, 5.2), (4.4, 7.6), (3.4, 9.6), (2.6, 10.8), (1.8, 10.4),
          (1.0, 8.4), (0.3, 5), (0, 2)]
EYESPOTS = dict(forewing=((5.6, 5.4), (1.45, 1.75)), hindwing=((4.6, 5.2), (1.5, 1.6)),
                sleeve=((2.6, 4.2), (1.0, 1.2)))

PALETTE = dict(
    cocoon=(236, 233, 222), fur=(240, 241, 234), lining=(40, 60, 58), robe=(232, 230, 220),
    wing=(186, 222, 178), wing_root=(140, 190, 164), wing_pale=(216, 236, 206), vein=(150, 190, 158),
    costa=(124, 98, 134), costa_light=(160, 136, 166), margin=(230, 222, 178), water=(150, 212, 218),
    slate=(62, 76, 102), yellow=(234, 214, 142), silver=(200, 208, 218), verdigris=(86, 156, 136),
    antenna=(218, 202, 152),
)


def inside(poly, x, y):
    hit = False
    for (x0, y0), (x1, y1) in zip(poly, poly[1:] + poly[:1]):
        if (y0 > y) != (y1 > y) and x < x0 + (y - y0) * (x1 - x0) / (y1 - y0):
            hit = not hit
    return hit


def seg_dist(px, py, a, b):
    (x0, y0), (x1, y1) = a, b
    dx, dy = x1 - x0, y1 - y0
    t = max(0, min(1, ((px - x0) * dx + (py - y0) * dy) / max(dx * dx + dy * dy, 1e-9)))
    return math.hypot(px - x0 - t * dx, py - y0 - t * dy)


def poly_dist(poly, x, y, closed=True):
    pts = poly + poly[:1] if closed else poly
    return min(seg_dist(x, y, a, b) for a, b in zip(pts, pts[1:]))


def eyespot(shape, a, b):
    """Return a colour for the eyespot ring this point falls in, or None outside it."""
    (cx, cy), (rx, ry) = EYESPOTS[shape]
    e = math.hypot((a - cx) / rx, (b - cy) / ry)
    if e >= 1:
        return None
    if SCALE < 2:
        # At block density an eyespot is ~3 texels across: one clear window, the split ring, a lilac rim.
        if e < 0.4:
            return PALETTE['water'] + (150,)
        if e < 0.8:
            return (PALETTE['slate'] if (a - cx) + 0.4 * (b - cy) < 0 else PALETTE['yellow']) + (255,)
        return PALETTE['costa_light'] + (255,)
    if e < 0.5:
        return PALETTE['water'] + (150,)           # the clear window: still water held in the scale
    if e < 0.72:
        # Luna eyespots are split: dark slate toward the body, yellow toward the margin.
        return (PALETTE['slate'] if (a - cx) + 0.4 * (b - cy) < 0 else PALETTE['yellow']) + (255,)
    if e < 0.86:
        return PALETTE['costa_light'] + (255,)
    return (232, 238, 224, 255)


def paint_wing(shape, a, b, t):
    n = t['n']
    if shape in ('tail', 'tail_tip'):
        drift, lengths = (0.12, 6.5) if shape == 'tail' else (0.1, 5.5)
        if shape == 'tail':
            half = 1.5 - 0.65 * min(b, 6) / 6
        else:
            # Narrows, then swells into the spatulate tip the living tails end in.
            half = 0.85 - 0.08 * b if b < 2.5 else 0.65 + 0.55 * math.sin(math.pi * min(1, (b - 2.5) / 3.0))
            if b > 4.3:
                half *= max(0, 1 - ((b - 4.3) / 1.2) ** 2) ** 0.5
        off = a - drift * b
        if abs(off) >= half or b < 0 or b >= lengths:
            return (0, 0, 0, 0)
        edge = half - abs(off)
        rgb = mix(PALETTE['wing'], PALETTE['wing_pale'], min(1, (b + (6.5 if shape == 'tail_tip' else 0)) / 12))
        if abs(off) < 0.2 and b < lengths - 1.5:
            rgb = PALETTE['vein']
        if edge < 0.4:
            rgb = PALETTE['margin'] if off > 0 else PALETTE['costa_light']   # cream outer edge, lilac inner
        if shape == 'tail_tip' and b > 3.0:
            rgb = mix(rgb, (226, 214, 216), min(1, (b - 3.0) / 2))   # the tips blush lilac-white
        return add(rgb, (n - 0.5) * 8) + (255,)

    poly = dict(forewing=FOREWING, hindwing=HINDWING, sleeve=SLEEVE)[shape]
    if not inside(poly, a, b):
        return (0, 0, 0, 0)
    spot = eyespot(shape, a, b)
    if spot:
        return spot
    reach = dict(forewing=14.5, hindwing=11.5, sleeve=10.5)[shape]
    r = math.hypot(a, b) / reach
    rgb = mix(PALETTE['wing_root'], PALETTE['wing'], min(1, r / 0.45))
    if r > 0.55:
        rgb = mix(rgb, PALETTE['wing_pale'], min(1, (r - 0.55) / 0.5))
    rgb = add(rgb, (n - 0.5) * 9 * min(1, SCALE / 2))   # quieter grain at block density, so it never reads as speckle
    # Veins fan from the wing root; at block density they are a texel wide so they stay unbroken lines.
    vein_half = 0.22 if SCALE >= 2 else 0.5
    if r * reach > 2.5:
        angle = math.atan2(b, a)
        for vein in (0.18, 0.45, 0.72, 0.98, 1.22, 1.42):
            if abs(angle - vein) * r * reach < vein_half:
                rgb = mix(rgb, PALETTE['vein'], 0.7)
    # Plum line linking the forewing eyespot to the costa, as on the living moth.
    if shape == 'forewing' and abs(a - 5.6) < 0.3 and 0.8 < b < 3.7:
        rgb = PALETTE['costa']
    edge = poly_dist(poly, a, b)
    if shape == 'forewing' and poly_dist(FOREWING_COSTA, a, b, closed=False) < 0.95 and b < 4:
        rgb = PALETTE['costa_light'] if n > 0.8 else PALETTE['costa']
    elif a < 1.4 + 0.08 * b and shape != 'sleeve':
        rgb = mix(PALETTE['fur'], (214, 228, 214), n)                      # fuzz where the wing meets the body
    elif edge < 0.45:
        rgb = PALETTE['margin']
    elif edge < 0.9:
        rgb = mix(rgb, PALETTE['margin'], 0.35)
    return rgb + (255,)


def cocoon(t):
    """Wound silk: slightly wavering threads in bands, the way a cocoon is spun."""
    gx, gz, gy = t['gx'], t['gz'], t['fy']
    wave = gy + 0.5 * math.sin(gx * 0.55 + gz * 0.45) + 0.25 * math.sin(gx * 1.7 - gz * 1.3)
    k = (wave * 1.25) % 1.5
    rgb = PALETTE['cocoon']
    if k < 0.3:
        rgb = (220, 218, 207)
    elif 0.75 < k < 0.95:
        rgb = (245, 244, 237)
    if (gx + gz + 2 * gy) % 6.5 < 0.4:
        rgb = (226, 224, 214)                     # the odd thread spun across the grain
    return add(rgb, (t['n'] - 0.5) * 5)


def fur(t):
    rgb = add(PALETTE['fur'], (t['n'] - 0.5) * 22)
    if t['n'] > 0.88:
        rgb = (210, 212, 202)
    elif t['n'] < 0.08:
        rgb = (224, 232, 220)
    return rgb


def paint(cube, t):
    material, face = cube['material'], t['face']
    lx, ly, lz = t['lx'], t['ly'], t['lz']
    if material == 'wing':
        side = cube.get('side', 1)
        a, b = (lz, ly) if cube['shape'] == 'sleeve' else (lx * side, ly)
        return paint_wing(cube['shape'], a, b, t)
    if material == 'antenna_feather':
        side = cube.get('side', 1)
        a, b = lx * side, -ly                     # b climbs from the base to the tip
        span = max(0.0, min(1.0, b / 6))
        half = 0.3 + 1.15 * math.sin(math.pi * min(1, span * 1.15)) if span < 0.95 else 0.2
        if abs(a) > half or b <= 0.2:
            return (0, 0, 0, 0)
        # Barbs slant up toward the tip; gaps between them are open air.
        if (b - abs(a) * 0.8) * SCALE % 2 >= 1.1:
            return (0, 0, 0, 0)
        return add(PALETTE['antenna'], (t['n'] - 0.5) * 16 - abs(a) * 10) + (255,)
    if material == 'antenna_shaft':
        rgb = PALETTE['antenna'] if int(-ly * SCALE) % 3 else (184, 166, 118)
        return rgb + (255,)
    if material == 'hood_shell':
        if face == 'north':
            arch = -7.75 + max(0, abs(lx) - 2.4) * 0.7
            if abs(lx) < 3.85 and ly > arch:
                return (0, 0, 0, 0)               # the face opening
        if face == 'down' and abs(lx) < 4.4 and -3.9 < lz < 4.3:
            return (0, 0, 0, 0)                   # the neck opening
        rgb = cocoon(t)
        return rgb + (255,)
    if material == 'hood_lining':
        if face in ('north', 'down', 'up'):
            return (0, 0, 0, 0)
        return add(PALETTE['lining'], (t['n'] - 0.5) * 8) + (255,)
    if material == 'hood_brim':
        if face == 'down':
            return add(PALETTE['lining'], 14) + (255,)
        rgb = cocoon(t)
        if face == 'north' and t['v'] == t['fh'] - 1:
            rgb = PALETTE['silver']               # a single silver thread hemming the brim
        return rgb + (255,)
    if material == 'hood_drape':
        # Drape tapers to a point between the shoulder blades.
        if t['side'] and abs(lx) > 3.5 * (1 - (ly - 0.75) / 3.2):
            return (0, 0, 0, 0)
        return cocoon(t) + (255,)
    if material == 'hood_fall':
        if t['side'] and abs(lx) > 3.25 * min(1, 1 - (ly - 0.0) / 5.6):
            return (0, 0, 0, 0)
        rgb = cocoon(t)
        if t['side'] and abs(lx) > 3.25 * min(1, 1 - (ly - 0.0) / 5.6) - 0.4 and ly > 0:
            rgb = PALETTE['silver']
        return rgb + (255,)
    if material == 'cocoon':
        return cocoon(t) + (255,)
    if material == 'fur':
        if t['side'] and t['v'] == t['fh'] - 1 and t['n'] > 0.45:
            return (0, 0, 0, 0)                   # ragged lower edge
        return fur(t) + (255,)
    if material == 'abdomen':
        rgb = fur(t)
        seg = (ly - 2.75) % 1.6
        if seg < 0.5:
            rgb = mix(rgb, (204, 208, 200), 0.7)  # segment creases
        return rgb + (255,)
    if material == 'sash':
        rgb = add(PALETTE['cocoon'], (t['n'] - 0.5) * 8)
        if t['side'] and t['v'] == 0:
            rgb = PALETTE['costa_light']
        elif t['side'] and (t['gx'] * SCALE) % 4 < 1:
            rgb = (214, 212, 202)
        return rgb + (255,)
    if material in ('robe', 'cuff'):
        rgb = add(PALETTE['robe'], math.sin(t['gx'] * 1.4) * 5 + (t['n'] - 0.5) * 6)
        if material == 'robe' and t['side']:
            gx, gy = t['gx'], t['fy']
            # Loose cocoon threads still wound across the robe.
            if (gx * 0.6 + gy) % 4.5 < 0.25 or (-gx * 0.6 + gy + 2.2) % 4.5 < 0.25:
                rgb = PALETTE['silver']
        if material == 'cuff' and t['side'] and t['v'] >= t['fh'] - 2:
            rgb = PALETTE['margin'] if t['v'] == t['fh'] - 1 else PALETTE['costa_light']
        return rgb + (255,)
    if material == 'placket':
        rgb = add(PALETTE['robe'], 6)
        if t['side'] and (t['u'] == 0 or t['u'] == t['fw'] - 1):
            rgb = PALETTE['verdigris'] if t['v'] % 3 else PALETTE['silver']
        elif t['side'] and t['v'] % 6 == 3:
            rgb = PALETTE['silver']               # ties across the opening
        return rgb + (255,)
    if material == 'brooch':
        if face == 'north':
            a, b = (lx + 1.25), (ly - 2.0)
            e = math.hypot((a - 1.25) / 1.25, (b - 1.25) / 1.25)
            if e >= 1.0:
                return (0, 0, 0, 0)
            if e < 0.45:
                return PALETTE['water'] + (255,)
            if e < 0.7:
                return (PALETTE['slate'] if a < 1.25 else PALETTE['yellow']) + (255,)
            return PALETTE['verdigris'] + (255,)
        return PALETTE['verdigris'] + (255,)
    if material == 'panel':
        side = cube.get('side', 1)
        a, b = lx * side, ly
        scallop = 7.6 + 0.55 * abs(math.sin(a * math.pi / 1.33))
        if b > scallop:
            return (0, 0, 0, 0)
        rgb = add(PALETTE['robe'], math.sin(a * 1.6) * 5 + (t['n'] - 0.5) * 6)
        if b > scallop - 0.6:
            rgb = PALETTE['margin']
        elif a > 3.45:
            rgb = PALETTE['verdigris'] if int(b * SCALE) % 3 else PALETTE['silver']
        elif 0.9 < math.hypot(a - 2, b - 5.4) < 1.5 and math.hypot(a - 2.5, b - 5.1) > 1.2:
            rgb = PALETTE['silver']               # the Lady's crescent, stitched in silver
        return rgb + (255,)
    raise KeyError(material)


# ---------------------------------------------------------------- box UV and atlas

def layout(rig):
    width = ATLAS_WIDTH
    u = v = row = 0
    for part in rig.parts:
        for cube in part['cubes']:
            _, _, _, w, hgt, d = cube['box']
            cw, ch = ceil(2 * (w + d)) + 1, ceil(hgt + d) + 1
            if u + cw > width:
                u, v, row = 0, v + row, 0
            cube['uv'] = (u, v)
            u += cw
            row = max(row, ch)
    height = 32
    while height < v + row:
        height *= 2
    return width, height


def part_world(rig):
    world = {None: (0, 0, 0)}
    for part in rig.parts:
        world[part['name']] = tuple(a + b for a, b in zip(world[part['parent']], part['pivot']))
    return world


def paint_atlas(rig, size, painter=None):
    painter = painter or paint
    image = Image.new('RGBA', (size[0] * SCALE, size[1] * SCALE))
    world = part_world(rig)
    for part in rig.parts:
        origin = world[part['name']]
        for cube in part['cubes']:
            u, v = cube['uv']
            x, y, z, w, hgt, d = cube['box']
            for face in ('down', 'up', 'west', 'east', 'north', 'south'):
                rect = face_rects(u, v, w, hgt, d)[face]
                lo_u, hi_u = sorted((rect[0], rect[2]))
                lo_v, hi_v = sorted((rect[1], rect[3]))
                if hi_u - lo_u < 1e-6 or hi_v - lo_v < 1e-6:
                    continue
                pu0, pv0 = math.floor(lo_u * SCALE), math.floor(lo_v * SCALE)
                pu1, pv1 = ceil(hi_u * SCALE), ceil(hi_v * SCALE)
                for py in range(pv0, pv1):
                    for px in range(pu0, pu1):
                        fu = min(1, max(0, ((px + 0.5) / SCALE - lo_u) / (hi_u - lo_u)))
                        fv = min(1, max(0, ((py + 0.5) / SCALE - lo_v) / (hi_v - lo_v)))
                        lx, ly, lz = local_point(face, cube['box'], fu, fv)
                        gx, gy, gz = lx + origin[0], ly + origin[1], lz + origin[2]
                        t = dict(face=face, u=px - pu0, v=py - pv0, fw=pu1 - pu0, fh=pv1 - pv0,
                                 side=face not in ('up', 'down'), lx=lx, ly=ly, lz=lz,
                                 gx=gx, fy=gy, gz=gz, n=h(gx * SCALE, gy * SCALE, gz * SCALE))
                        image.putpixel((px, py), painter(cube, t))
    return image


# ---------------------------------------------------------------- Java

def camel(name):
    head, *rest = name.split('_')
    return head + ''.join(word.capitalize() for word in rest)


ANIMATED = ['hood_up', 'hood_down', 'left_forewing', 'right_forewing', 'left_hindwing', 'right_hindwing',
            'left_tail', 'right_tail', 'left_tail_tip', 'right_tail_tip', 'left_sleeve_wing', 'right_sleeve_wing',
            'left_front_panel', 'right_front_panel']


def java_source(rig, size):
    by_name = {p['name']: p for p in rig.parts}
    lines = []
    for part in rig.parts:
        chain = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            u, v = cube['uv']
            box = ', '.join(f(c) for c in cube['box'])
            chain += f'\n\t\t\t\t.texOffs({u}, {v}).addBox({box}, new CubeDeformation(0.0F))'
        parent = 'root' if part['parent'] is None else camel(part['parent'])
        rot = part['rotation']
        pivot = ', '.join(f(p) for p in part['pivot'])
        if any(rot):
            pose = f'PartPose.offsetAndRotation({pivot}, {", ".join(f(math.radians(a)) for a in rot)})'
        elif any(part['pivot']):
            pose = f'PartPose.offset({pivot})'
        else:
            pose = 'PartPose.ZERO'
        has_children = any(p['parent'] == part['name'] for p in rig.parts)
        decl = f'PartDefinition {camel(part["name"])} = ' if has_children else ''
        lines.append(f'\t\t{decl}{parent}.addOrReplaceChild("{part["name"]}", {chain}, {pose});')
    body = '\n\n'.join(lines)

    fields = '\n'.join(f'\tprivate final ModelPart {camel(n)};' for n in ANIMATED)
    # Every animated part's parent is either a HumanoidModel field or an animated part assigned before it.
    ctor = '\n'.join(f'\t\tthis.{camel(n)} = this.{camel(by_name[n]["parent"])}.getChild("{n}");' for n in ANIMATED)

    def rest(name, axis):
        return f(math.radians(by_name[name]['rotation']['xyz'.index(axis)]))

    return f'''package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.Lazy;

/**
 * Luna moth hood and wing cloak. Generated by tools/model_export/vestment_of_the_final_molt.py; edit the rig
 * there and re-run it. Wings, tails, sleeve wings and antenna plumes are zero-depth planes shaped by the texture.
 */
public class VestmentOfTheFinalMoltArmorModel<T extends LivingEntity> extends HumanoidModel<T> {{
	public static final ModelLayerLocation VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("vestment_of_the_final_molt_chest"), "main");
	public static final Lazy<VestmentOfTheFinalMoltArmorModel<LivingEntity>> chest = Lazy.of(
			() -> new VestmentOfTheFinalMoltArmorModel<>(
					Minecraft.getInstance().getEntityModels().bakeLayer(VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER)));

{fields}

	public VestmentOfTheFinalMoltArmorModel(ModelPart root) {{
		super(root, RenderType::entityTranslucent);
{ctor}
	}}

	public void setHoodRaised(boolean hoodRaised) {{
		this.hoodUp.visible = hoodRaised;
		this.hoodDown.visible = !hoodRaised;
	}}

	public static LayerDefinition createBodyLayer() {{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

{body}

		return LayerDefinition.create(mesh, {size[0]}, {size[1]});
	}}

	/**
	 * Breathing and stride for the cloak. Called after the wearer's pose has been copied onto the standard parts,
	 * so the front panels can follow the legs.
	 */
	public void animateCloak(float limbSwing, float limbSwingAmount, float ageInTicks) {{
		float breath = Mth.sin(ageInTicks * 0.07F) * 0.04F;
		float stride = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
		float swing = Mth.sin(limbSwing * 0.6662F) * stride;
		float flutter = Mth.sin(ageInTicks * 0.09F + 1.3F) * 0.05F;

		this.leftForewing.xRot = {rest('left_forewing', 'x')} + stride * 0.25F + breath * 0.5F;
		this.rightForewing.xRot = this.leftForewing.xRot;
		this.leftForewing.yRot = {rest('left_forewing', 'y')} - breath - stride * 0.1F;
		this.rightForewing.yRot = -this.leftForewing.yRot;
		this.leftHindwing.xRot = {rest('left_hindwing', 'x')} + stride * 0.35F + breath * 0.5F;
		this.rightHindwing.xRot = this.leftHindwing.xRot;
		this.leftHindwing.zRot = {rest('left_hindwing', 'z')} + swing * 0.06F;
		this.rightHindwing.zRot = -{rest('left_hindwing', 'z')} + swing * 0.06F;
		this.leftTail.xRot = {rest('left_tail', 'x')} + stride * 0.45F + flutter;
		this.rightTail.xRot = {rest('right_tail', 'x')} + stride * 0.45F - flutter;
		this.leftTailTip.xRot = {rest('left_tail_tip', 'x')} + stride * 0.4F + flutter * 1.5F;
		this.rightTailTip.xRot = {rest('right_tail_tip', 'x')} + stride * 0.4F - flutter * 1.5F;
		this.leftSleeveWing.xRot = {rest('left_sleeve_wing', 'x')} + stride * 0.3F;
		this.rightSleeveWing.xRot = this.leftSleeveWing.xRot;
		this.leftSleeveWing.zRot = {rest('left_sleeve_wing', 'z')} - swing * 0.1F;
		this.rightSleeveWing.zRot = {rest('right_sleeve_wing', 'z')} - swing * 0.1F;
		// The panels hang in front of the thighs, so they only ever swing forward with a striding leg.
		this.leftFrontPanel.xRot = Math.min(this.leftLeg.xRot, 0.0F) * 0.9F;
		this.rightFrontPanel.xRot = Math.min(this.rightLeg.xRot, 0.0F) * 0.9F;
	}}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {{
		this.head.visible = this.hoodUp.visible;
		this.hat.visible = false;
		renderPart(this.head, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.body, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.rightArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.leftArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
	}}

	private static void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {{
		if (part.visible) {{
			part.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		}}
	}}
}}
'''


# ---------------------------------------------------------------- preview

MANNEQUIN = dict(head=((-4, -8, -4, 8, 8, 8), 0), body=((-4, 0, -2, 8, 12, 4), 1),
                 right_arm=((-3, -2, -2, 4, 12, 4), 0), left_arm=((-1, -2, -2, 4, 12, 4), 0),
                 right_leg=((-2, 0, -2, 4, 12, 4), 2), left_leg=((-2, 0, -2, 4, 12, 4), 2))
MANNEQUIN_COLOURS = [(150, 122, 104), (64, 70, 92), (52, 56, 70)]


def transforms(rig):
    import numpy as np
    out = {}
    for part in rig.parts:
        m = np.eye(4)
        m[:3, :3] = rot_matrix(part['rotation'])
        m[:3, 3] = part['pivot']
        out[part['name']] = out.get(part['parent'], np.eye(4)) @ m
    return out


def quads(rig, hidden=(), mannequin_row=None, v_offset=0):
    import numpy as np
    m_all = transforms(rig)

    def skip(name):
        while name is not None:
            if name in hidden:
                return True
            name = next(p['parent'] for p in rig.parts if p['name'] == name)
        return False

    result = []
    for part in rig.parts:
        if skip(part['name']):
            continue
        m = m_all[part['name']]
        cubes = [(c['box'], c['uv'], False) for c in part['cubes']]
        if mannequin_row is not None and part['name'] in MANNEQUIN:
            box, colour = MANNEQUIN[part['name']]
            cubes.append((box, colour, True))
        for box, uv, flat in cubes:
            x, y, z, w, hgt, d = box
            rects = face_rects(uv[0], uv[1], w, hgt, d) if not flat else None
            for face in ('down', 'up', 'west', 'east', 'north', 'south'):
                corners, uvs = [], []
                for fu, fv in ((0, 0), (1, 0), (1, 1), (0, 1)):
                    corners.append(local_point(face, box, fu, fv))
                    if flat:
                        uvs.append((uv * 4 + 2, mannequin_row + 2))
                    else:
                        rect = rects[face]
                        uvs.append(((rect[0] + (rect[2] - rect[0]) * fu) * SCALE,
                                    (rect[1] + (rect[3] - rect[1]) * fv) * SCALE + v_offset))
                pts = np.array([list(c) + [1] for c in corners]) @ m.T
                result.append((pts[:, :3], np.array(uvs, float)))
    return result


def preview(rig, image):
    tex = Image.new('RGBA', (image.width, image.height + 8))
    tex.paste(image, (0, 0))
    for i, colour in enumerate(MANNEQUIN_COLOURS):
        for px in range(i * 4, i * 4 + 4):
            for py in range(image.height, image.height + 8):
                tex.putpixel((px, py), colour + (255,))
    row = image.height
    up = quads(rig, ('hood_down',), row)
    down = quads(rig, ('hood_up',), row)
    bare = quads(rig, ('hood_down', 'right_leg', 'left_leg'))
    size = 420
    tiles = [render_quads(up, tex, 0.0, 0.0, size), render_quads(up, tex, math.pi, 0.0, size),
             render_quads(up, tex, math.pi * 0.78, 0.25, size), render_quads(up, tex, math.pi / 2, 0.0, size),
             render_quads(up, tex, 0.55, 0.2, size), render_quads(down, tex, math.pi * 1.2, 0.2, size),
             render_quads(down, tex, 0.4, 0.1, size), render_quads(bare, tex, math.pi * 0.85, -0.2, size)]
    cols = 4
    sheet = Image.new('RGB', (size * cols, size * math.ceil(len(tiles) / cols)), (30, 32, 40))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % cols) * size, (i // cols) * size))
    out = ROOT / 'build/vestment_of_the_final_molt_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    big = image.resize((image.width * 3, image.height * 3), Image.NEAREST)
    big.save(ROOT / 'build/vestment_of_the_final_molt_atlas_x3.png')
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = vestment()
    size = layout(rig)
    image = paint_atlas(rig, size)
    image.save(TEXTURE)
    JAVA.write_text(java_source(rig, size), encoding='utf-8')
    print(f'vestment: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]} '
          f'({image.width}x{image.height} px)')
    if args.preview:
        preview(rig, image)


if __name__ == '__main__':
    main()

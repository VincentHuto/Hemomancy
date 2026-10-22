"""Author the Resonant Forge: a cam-read trip hammer, a quench basin and a recording grindstone.

Emits every Resonant Forge block model (three stage bodies, the inventory model and the animated
parts ResonantForgeRenderer draws) plus editable Blockbench projects. `--preview` also writes
textured renders of all three stages to build/resonant_forge_preview.png.

Model space is the north-facing controller block: x runs from the hammer bay (-16..0) through the
basin (0..16) to the grinder bay (16..32); z runs from the back row (0..16, the controller cell)
to the front row (16..32, the player's side). The side bays are two blocks tall; the basin column
stays one block tall so its headroom is open. Animated parts are authored in place and pivot
about the constants below, which ResonantForgeRenderer mirrors.

Python 3 + Pillow + NumPy.
"""
from pathlib import Path
import argparse
import copy
import json
import math
import zlib

import coplanar

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODELS = ASSETS / 'models/block'
BBMODEL = MODELS / 'bbmodel'

# Pivots shared with ResonantForgeRenderer (model pixels).
HELVE_PIVOT = (-6.5, 18.0, 12.0)        # helve fulcrum, rotates about X
SPRING_ANCHOR = (-6.5, 28.75, 1.0)      # base spring pole flexes about its lash on the mast, about X
SPRING_BEND = (-6.5, 28.5, 14.0)        # the pole's bent tip flexes again about the bend, about X
HAMMER_AXIS = (-6.5, 12.5, 5.5)         # pattern cylinder axis (along X), crank turns with it
CRANK_RADIUS = 2.5
TREADLE_PIVOT = (-14.0, 8.0, 24.0)      # treadle rocks about X; high enough that its tail clears the sleeper
WHEEL_AXIS = (24.0, 10.0, 22.0)         # grindstone axis (along Z): the stone faces the worker
WHEEL_RADIUS = 6.5
LAYSHAFT = (10.0, 12.5)                 # y/z of the bevel-driven layshaft (along X) behind the stone
RECORD_AXIS = (22.5, 19.5, 8.5)         # recording cylinder axis (along X)
STYLUS_PIVOT = (22.5, 27.0, 3.75)       # base sound-box arm hinge, rotates about X
CARRIAGE_TRAVEL = 4.0                   # Precision carriage travel either side of centre
GOVERNOR_AXIS = (27.0, 0.0, 12.5)       # flyball spindle on the layshaft, rotates about Y
GRIND_POINT = (17.0, 10.0, 22.0)        # where the item meets the stone's inner rim
HEART_CENTER = (8.0, 9.0, 7.5)          # Masterwork heart pulse centre
CYLINDER_RADIUS = 2.5
CYLINDER_HALF = 5.0

TEXTURES = {
    'plank': 'hemomancy:block/blood_wood_planks',
    'log': 'hemomancy:block/blood_wood_log',
    'logtop': 'hemomancy:block/blood_wood_log_top',
    'oak': 'minecraft:block/stripped_dark_oak_log',
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'anvil': 'minecraft:block/anvil',
    'stone': 'minecraft:block/grindstone_side',
    'rim': 'minecraft:block/grindstone_round',
    'pivot': 'minecraft:block/grindstone_pivot',
    'blood': 'hemomancy:block/blood_still',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'leather': 'minecraft:block/brown_wool',
    'flesh': 'hemomancy:block/bog_flesh',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'scab': 'hemomancy:block/blood_scorched_scab',
    'amber': 'hemomancy:block/mnemonic_ambergris_block',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'wax': 'hemomancy:item/ambergris_cylinder_recorded',
    'waxcap': 'hemomancy:item/ambergris_cylinder',
}
# Materials that sample a sub-rectangle of their texture instead of world-aligned tiling.
REGIONS = {'wax': (0, 0, 16, 8), 'waxcap': (0, 8, 16, 16),
           'stone': (0, 0, 12, 12), 'rim': (0, 0, 8, 12), 'pivot': (0, 0, 10, 6)}
CAP = {'wax': 'waxcap', 'amber': 'amber', 'stone': 'stone', 'log': 'logtop', 'bone': 'bonecap'}
TAN = math.tan(math.radians(22.5))


class Model:
    def __init__(self):
        self.elements = []
        self.group = 'static'

    def box(self, name, a, b, mat, rot=None, faces=None, caps=None):
        """Axis-aligned cube from a to b; rot=(axis, angle, origin) applies a Java model rotation."""
        a, b = [round(min(p, q), 4) for p, q in zip(a, b)], [round(max(p, q), 4) for p, q in zip(a, b)]
        if any(q - p <= 0 for p, q in zip(a, b)):
            return
        sides = faces or ('north', 'south', 'east', 'west', 'up', 'down')
        out = {}
        for side in sides:
            m = mat
            if caps and side in caps:
                m = caps[side]
            out[side] = (m, uv(side, a, b, m))
        self.elements.append(dict(name=name, group=self.group, a=a, b=b, rot=rot, faces=out))

    def octo(self, name, axis, centre, radius, start, end, mat, cap=None, eps=True):
        """Regular octagonal prism along `axis` from start to end (flat-to-flat 2*radius)."""
        cap = cap or CAP.get(mat, mat)
        half_w, half_a = radius, radius * TAN
        i = 'xyz'.index(axis)
        others = [k for k in range(3) if k != i]
        pieces = [(half_w, half_a, 0), (half_a, half_w, 0), (half_w, half_a, 45), (half_w, half_a, -45)]
        for n, (h0, h1, angle) in enumerate(pieces):
            a, b = [0] * 3, [0] * 3
            inset = n * .01 if eps else 0
            a[i], b[i] = start + inset, end - inset
            a[others[0]], b[others[0]] = centre[others[0]] - h0, centre[others[0]] + h0
            a[others[1]], b[others[1]] = centre[others[1]] - h1, centre[others[1]] + h1
            rot = (axis, angle, list(centre)) if angle else None
            ends = {'x': ('east', 'west'), 'y': ('up', 'down'), 'z': ('north', 'south')}[axis]
            self.box(name, a, b, mat, rot, caps={ends[0]: cap, ends[1]: cap})

    def disc(self, name, axis, centre, radius, start, end, mat, cap=None):
        """Stepped round prism of axis-aligned cubes; caps share one UV frame so the face reads whole."""
        cap = cap or mat
        i = 'xyz'.index(axis)
        o = [k for k in range(3) if k != i]
        ends = {'x': ('east', 'west'), 'y': ('up', 'down'), 'z': ('north', 'south')}[axis]
        for degrees in (14, 38, 52, 76):
            h0 = round(radius * math.cos(math.radians(degrees)) * 4) / 4
            h1 = round(radius * math.sin(math.radians(degrees)) * 4) / 4
            a, b = [0] * 3, [0] * 3
            a[i], b[i] = start, end
            a[o[0]], b[o[0]] = centre[o[0]] - h0, centre[o[0]] + h0
            a[o[1]], b[o[1]] = centre[o[1]] - h1, centre[o[1]] + h1
            before = len(self.elements)
            self.box(name, a, b, mat)
            face_uv = []
            for k in o:
                lo = (a[k] - (centre[k] - radius)) / (2 * radius) * 16
                hi = (b[k] - (centre[k] - radius)) / (2 * radius) * 16
                face_uv.append((round(lo, 3), round(hi, 3)))
            for side in ends:
                self.elements[before]['faces'][side] = (cap, region(
                    [face_uv[0][0], 16 - face_uv[1][1], face_uv[0][1], 16 - face_uv[1][0]], cap))

    def ring(self, name, axis, centre, outer, inner, start, end, mat):
        """Hollow square collar along `axis`."""
        i = 'xyz'.index(axis)
        o = [k for k in range(3) if k != i]
        for s0, s1 in ((-outer, -inner), (inner, outer)):
            a, b = [0] * 3, [0] * 3
            a[i], b[i] = start, end
            a[o[0]], b[o[0]] = centre[o[0]] + s0, centre[o[0]] + s1
            a[o[1]], b[o[1]] = centre[o[1]] - outer, centre[o[1]] + outer
            self.box(name, a, b, mat)
            a[o[0]], b[o[0]] = centre[o[0]] - inner, centre[o[0]] + inner
            a[o[1]], b[o[1]] = centre[o[1]] + s0, centre[o[1]] + s1
            self.box(name, a, b, mat)

    def rod(self, name, p0, p1, thick, mat, axis):
        """A square rod between two points that differ only along `axis`."""
        a = [c - thick / 2 for c in p0]
        b = [c + thick / 2 for c in p1]
        i = 'xyz'.index(axis)
        a[i], b[i] = min(p0[i], p1[i]), max(p0[i], p1[i])
        self.box(name, a, b, mat)


def uv(side, a, b, mat):
    """World-aligned UVs so neighbouring cubes continue the same texture."""
    if side in ('north', 'south'):
        u, v = (a[0], b[0]), (16 - b[1], 16 - a[1])
    elif side in ('east', 'west'):
        u, v = (a[2], b[2]), (16 - b[1], 16 - a[1])
    else:
        u, v = (a[0], b[0]), (a[2], b[2])
    out = []
    for lo, hi in (u, v):
        size = min(hi - lo, 16)
        start = lo % 16
        if start + size > 16:
            start = 16 - size
        out.append((round(start, 3), round(start + size, 3)))
    return region([out[0][0], out[1][0], out[0][1], out[1][1]], mat)


def region(coords, mat):
    """Squeeze a 0..16 UV rectangle into the material's opaque sub-rectangle, if it has one."""
    u0, v0, u1, v1 = coords
    if mat in REGIONS:
        r = REGIONS[mat]
        sx, sy = (r[2] - r[0]) / 16, (r[3] - r[1]) / 16
        u0, u1 = r[0] + u0 * sx, r[0] + u1 * sx
        v0, v1 = r[1] + v0 * sy, r[1] + v1 * sy
    return [round(u0, 3), round(v0, 3), round(u1, 3), round(v1, 3)]


# ---------------------------------------------------------------- shared structure

def sills(m, t):
    """Two long sills tying the three stations together, with sleepers under each bay."""
    beam = 'plank' if t == 0 else 'iron'
    for z0 in (1, 28):
        m.box('sill', (-16, 0, z0), (32, 2, z0 + 3), beam)
    for x0 in (-16, -2, 16, 30):
        m.box('sleeper', (x0, 0, 0), (x0 + 2, 1.5, 32), 'oak' if t == 0 else 'chisel')
    if t > 0:
        for x0 in (-16.5 + .5, 30):
            for z0 in (0.5, 27.5):
                m.box('cast_foot', (x0, 0, z0), (x0 + 2, 3, z0 + 4), 'chisel')


def hammer_bay(m, t):
    frame = 'oak' if t == 0 else 'pillar'
    # Anvil on a stump, under the helve head.
    m.box('stump', (-10.5, 1.5, 17.5), (-2.5, 7, 26.5), 'log', caps={'up': 'logtop'})
    if t > 0:
        m.ring('stump_band', 'y', (-6.5, 0, 22), 4.6, 4, 5, 6, 'iron')
    m.box('anvil_foot', (-10, 7, 18.5), (-3, 8, 25.5), 'anvil')
    m.box('anvil_waist', (-8.5, 8, 20), (-4.5, 10, 24), 'anvil')
    m.box('anvil_face', (-11, 10, 19.5), (-2, 12, 24.5), 'anvil')
    m.box('anvil_horn', (-2, 10.5, 20.5), (0, 11.75, 23.5), 'anvil')
    # Fulcrum cheeks carrying the helve axle.
    for x0 in (-11.5, -3):
        m.box('fulcrum_cheek', (x0, 1.5, 10.5), (x0 + 1.5, HELVE_PIVOT[1] + 1.5, 13.5), frame)
    m.octo('fulcrum_axle', 'x', HELVE_PIVOT, .75, -12, -1, 'iron')
    # Bearing posts for the pattern cylinder at the back.
    for x0 in (-13.5, -1):
        m.box('cylinder_post', (x0, 1.5, 4), (x0 + 1.5, 14, 7), frame)
        m.box('cylinder_bearing', (x0 - .25, 11, 3.5), (x0 + 1.75, 14, 7.5), 'iron' if t == 0 else 'chisel')
    # Treadle hinge brackets.
    m.box('treadle_bracket', (-16, 1.5, 23), (-15, TREADLE_PIVOT[1] + .75, 25), 'iron')
    m.box('treadle_bracket', (-11.25, 1.5, 23), (-10.25, TREADLE_PIVOT[1] + .75, 25), 'iron')
    # Mast behind the cylinder and a front post beside the basin.
    m.box('mast', (-8, 1.5, 0), (-5, 31, 2), frame)
    m.box('front_post', (-2.5, 1.5, 27.5), (-0.5, 24, 29.5), frame)
    if t == 0:
        # Lashing for the bent blood-wood spring pole, an animated part, on the mast.
        m.box('spring_lash', (-8.25, 27.25, -0.25), (-4.75, 30.25, 2.25), 'leather')
        m.box('front_cap', (-3, 24, 27), (0, 25, 30), 'plank')
        m.box('tool_peg', (-4.5, 19, 28), (-2.5, 20, 29), 'oak')
        m.box('tongs', (-4.25, 13.5, 28.25), (-3.75, 19, 28.75), 'anvil')
        m.box('tongs', (-3.5, 13.5, 28.25), (-3, 19, 28.75), 'anvil')
    else:
        # Iron gallows: an overhead beam with a stacked leaf spring over the helve tail.
        m.box('gallows_beam', (-16, 28, 1), (-0.5, 30.5, 4), 'iron')
        m.box('gallows_brace', (-3, 22, 1.5), (-1, 28, 3.5), 'pillar')
        for n, y in enumerate((24.5, 25.5, 26.5)):
            m.box('leaf_spring', (-8 + n, y, 2.5), (-5 - n, y + 1, 9 - n * 2.5), 'iron')
        m.box('spring_clamp', (-8.5, 26, 2), (-4.5, 28, 4), 'chisel')
        m.box('front_cap', (-3, 24, 27), (0, 25.5, 30), 'chisel')
        # Selector index plate beside the follower: notches that pick one groove.
        m.box('index_plate', (-13.25, 15, 1), (-12.75, 20, 8), 'iron')
        for n in range(5):
            m.box('index_notch', (-13.5, 15.5 + n, 1.5 + n * 1.25), (-12.5, 16, 2.25 + n * 1.25), 'vein')
        m.box('index_pointer', (-12.75, 17, 4), (-11.75, 17.5, 7.5), 'vein')


def basin(m, t):
    wall = 'plank' if t == 0 else 'iron'
    # Quench trough in the front row.
    x0, x1, z0, z1, top = 1.5, 14.5, 15, 30, 9
    m.box('trough_floor', (x0, 1.5, z0), (x1, 3, z1), wall)
    m.box('trough_wall', (x0, 1.5, z0), (x1, top, z0 + 1.5), wall)
    m.box('trough_wall', (x0, 1.5, z1 - 1.5), (x1, top, z1), wall)
    m.box('trough_wall', (x0, 1.5, z0), (x0 + 1.5, top, z1), wall)
    m.box('trough_wall', (x1 - 1.5, 1.5, z0), (x1, top, z1), wall)
    band = 'iron' if t == 0 else 'chisel'
    for y in ((3.5, 4.5), (7, 8)) if t == 0 else ((2.5, 4), (5.5, 7)):
        m.box('trough_band', (x0 - .25, y[0], z0 - .25), (x1 + .25, y[1], z1 + .25), band, faces=('north', 'south', 'east', 'west'))
    m.box('trough_lip', (x0 - .5, top, z0 - .5), (x1 + .5, top + .75, z0 + 1), band)
    m.box('trough_lip', (x0 - .5, top, z1 - 1), (x1 + .5, top + .75, z1 + .5), band)
    # Tongs resting across the rim.
    m.box('tong_rest', (11, top + .75, 17), (11.75, top + 1.25, 28), 'anvil')
    m.box('tong_rest', (12.25, top + .75, 17), (13, top + 1.25, 28), 'anvil')
    if t < 2:
        # Back-row cistern feeding the trough through a spout.
        staves = 'plank' if t == 0 else 'iron'
        m.disc('cistern', 'y', (8, 0, 7.5), 5.5, 1.5, 13.5, staves, cap='plank' if t == 0 else 'chisel')
        for y in (3, 11) if t == 0 else (2.5, 7, 11.5):
            m.ring('cistern_hoop', 'y', (8, 0, 7.5), 5.25, 4.5, y, y + 1, 'iron' if t == 0 else 'chisel')
        m.box('spout', (7, 10.5, 12.5), (9, 12, 17.5), 'iron')
        m.box('spout_mouth', (7, 9.5, 16.5), (9, 10.5, 17.5), 'iron')
    if t >= 1:
        # Float gauge: a rod riding the blood with a dial on the trough wall.
        m.box('gauge_rod', (3.5, 3, 27), (4.25, 13.5, 27.75), 'iron')
        m.box('gauge_dial', (2.5, 9.75, 28.5), (6, 13, 30.25), 'chisel')
        m.box('gauge_face', (3, 10.25, 30.25), (5.5, 12.5, 30.5), 'vein', faces=('south',))


def grinder_bay(m, t):
    frame = 'oak' if t == 0 else 'pillar'
    cx, cy, cz = WHEEL_AXIS
    ly, lz = LAYSHAFT
    rx, ry, rz = RECORD_AXIS
    # A-frame trestles front and back of the stone, legs splayed across the bay.
    for z0 in (16, 27.5):
        for angle in (22.5, -22.5):
            m.box('trestle_leg', (cx - .75, 1.5, z0), (cx + .75, cy + .5, z0 + 1.5), frame, rot=('z', angle, [cx, cy, z0]))
        m.box('trestle_tie', (cx - 4.5, 3.5, z0 + .25), (cx + 4.5, 4.5, z0 + 1.25), frame)
        m.box('axle_bearing', (cx - 1.5, cy - 1.5, z0 - .25), (cx + 1.5, cy + 1.5, z0 + 1.75), 'iron' if t == 0 else 'chisel')
    # Slurry trough the lower rim runs through.
    m.box('slurry_trough', (19, 1.5, 18.25), (29, 4.5, 26), 'plank' if t == 0 else 'iron')
    m.box('slurry', (19.75, 3.75, 19), (28.25, 4.25, 25.25), 'blood', faces=('up',))
    # Tool rest on the basin side, where the item is held to the stone.
    gx, gy, gz = GRIND_POINT
    m.box('tool_rest', (gx - 2.5, gy - 1, gz - 3), (gx + .25, gy, gz + 3), 'iron')
    m.box('tool_rest_strut', (gx - 2, 1.5, gz - .5), (gx - 1, gy - 1, gz + .5), 'iron')
    m.box('tool_rest_foot', (gx - 2.75, 1.5, gz - 1.5), (gx - .25, 2.25, gz + 1.5), 'iron')
    # Layshaft behind the stone: a bevel pair turns the stone's spin across the bay.
    m.octo('layshaft', 'x', (cx, ly, lz), .6, cx, 31.5, 'iron')
    m.octo('bevel_wheel', 'z', WHEEL_AXIS, 1.6, 13.25, 14.25, 'chisel' if t else 'iron', cap='chisel' if t else 'iron')
    m.octo('bevel_layshaft', 'x', (cx, ly, lz), 1.6, cx + .75, cx + 1.75, 'chisel' if t else 'iron', cap='chisel' if t else 'iron')
    m.box('layshaft_hanger', (29.5, 1.5, lz - .75), (31, ly + 1, lz + .75), frame)
    # Recording cylinder bearings in the back row; machined posts also carry the lead screw.
    top = ry + 1.5 if t == 0 else 31
    for x0 in (15.75, 27.75):
        m.box('record_post', (x0, 1.5, rz - 2), (x0 + 1.5, top, rz + 1.5), frame)
        m.box('record_bearing', (x0 - .25, ry - 1.25, rz - 2.25), (x0 + 1.75, ry + 1.25, rz + 1.75),
              'iron' if t == 0 else 'chisel')
    m.octo('record_mandrel', 'x', RECORD_AXIS, .6, 15.75, 31.25, 'iron')
    # Belt from the layshaft up to the mandrel: the stone's turning drives the wax.
    m.octo('layshaft_pulley', 'x', (cx, ly, lz), 1.75, 30.25, 31.25, 'iron')
    m.octo('mandrel_pulley', 'x', RECORD_AXIS, 1.25, 30.25, 31.25, 'iron')
    mid_y, mid_z = (ly + ry) / 2, (lz + rz) / 2
    length = math.hypot(ry - ly, rz - lz)
    for off in (-1.5, 1.5):
        m.box('drive_belt', (30.35, mid_y - length / 2, mid_z + off - .25), (31.15, mid_y + length / 2, mid_z + off + .25),
              'leather', rot=('x', -22.5, [30.75, mid_y, mid_z]))
    m.box('back_mast', (30, 1.5, 0), (32, 30, 2.5), frame)
    # Speaking tube from a cup over the grinding point (Masterwork replaces it with the horn).
    if t < 2:
        tx = gx + 1
        m.box('listening_cup', (gx - 1.5, 13, gz - 1.5), (gx + 1.5, 15.5, gz + 1.5), 'bone')
        m.box('listening_cup_mouth', (gx - 2, 12.5, gz - 2), (gx + 2, 13, gz + 2), 'bone')
        tube = 'oak' if t == 0 else 'iron'
        m.box('speaking_tube', (tx - .5, 15.5, gz - .5), (tx + .5, 28, gz + .5), tube)
        m.box('speaking_tube', (tx - .5, 27, rz + 2.5), (tx + .5, 28, gz + .5), tube)
        m.box('speaking_tube', (tx - .5, 27, rz + 2), (rx - 1, 28, rz + 3), tube)
        m.box('tube_stay', (tx - 1, 1.5, gz - 4.5), (tx, 27, gz - 3.5), frame)
        m.box('tube_clip', (tx - 1.25, 26.5, gz - 4.75), (tx + .75, 28.5, gz - 3.25), 'iron')
    if t == 0:
        m.box('stylus_hinge_post', (rx - 1, 15, STYLUS_PIVOT[2] - .75), (rx + 1, 28, STYLUS_PIVOT[2] + .75), frame)
        m.box('hose', (rx - 1.25, 26.75, rz + 1.75), (rx - .25, 28.25, rz + 3.25), 'leather')
    else:
        # Lead screw and guide rail carrying the indexed stylus carriage across the wax.
        m.octo('lead_screw', 'x', (rx, 29, rz), .6, 17.25, 31, 'iron')
        for x in range(18, 28, 2):
            m.ring('screw_thread', 'x', (rx, 29, rz), .9, .55, x, x + .5, 'vein')
        m.box('guide_rail', (17.25, 30.25, rz - 2.25), (27.75, 31.25, rz - 1.25), 'iron')
        m.octo('screw_gear', 'x', (rx, 29, rz), 1.75, 30.25, 31, 'vein', cap='vein')
        m.box('hose', (rx - 1.75, 25.5, rz + 1.75), (rx - .25, 28, rz + 3.25), 'leather')
        # Governor bevel housing on the layshaft.
        ox, _, oz = GOVERNOR_AXIS
        m.box('governor_housing', (ox - 1.5, ly - 1.5, oz - 1.5), (ox + 1.5, ly + 2, oz + 1.5), 'chisel')


# ---------------------------------------------------------------- Masterwork additions

def masterwork(m):
    # Heart-chamber in the back of the basin column, caged in iron.
    cx, cy, cz = HEART_CENTER
    for x0, z0 in ((2, 1.5), (13, 1.5), (2, 12.5), (13, 12.5)):
        m.box('heart_cage_post', (x0, 1.5, z0), (x0 + 1, 15.5, z0 + 1), 'pillar')
    m.ring('heart_cage_band', 'y', (8, 0, 7.5), 6, 5.5, 14.75, 15.5, 'chisel')
    m.ring('heart_cage_band', 'y', (8, 0, 7.5), 6.5, 5, 1.5, 3, 'chisel')
    m.box('heart_bed', (4, 1.5, 3.5), (12, 3.5, 11.5), 'scab')
    m.box('heart_bed', (5, 3.5, 4.5), (11, 4.25, 10.5), 'flesh')
    # Arteries out of the chamber: into the trough, and along the sills to both bays.
    m.box('artery', (7, 9, 12), (9, 11, 18.5), 'vein')
    m.box('artery_mouth', (6.5, 8, 17.5), (9.5, 9.5, 19), 'vein')
    m.box('artery', (-14, 2, 7.75), (3.5, 3.5, 9.25), 'vein')
    m.box('artery', (-13.75, 3.5, 7.75), (-12.25, 13.5, 9.25), 'vein')
    m.box('artery', (12.5, 2, 2.5), (31.5, 3.5, 4), 'vein')
    m.box('artery', (30, 3.5, 2.5), (31.5, 26, 4), 'vein')
    m.box('artery', (13, 2, 4), (14.5, 3.5, 7), 'vein')
    m.box('artery', (-10, 7, 17), (-8.5, 8.5, 18), 'vein')
    m.box('artery', (-9.5, 2, 9.25), (-8.5, 7.5, 18), 'vein')
    # Master pattern magazine above the hammer: three ambergris cylinders in a bone rack.
    m.box('magazine_cheek', (-14, 26, 10), (-13, 31, 27), 'chisel')
    m.box('magazine_cheek', (-1.5, 26, 10), (-0.5, 31, 27), 'chisel')
    m.box('magazine_floor', (-14, 26, 10), (-0.5, 26.75, 27), 'chisel')
    for z in (13, 18.5, 24):
        m.octo('master_pattern', 'x', (-7.25, 29, z), 2.25, -12.5, -2, 'amber')
        m.box('pattern_cradle', (-12.75, 26.75, z - 2.5), (-1.75, 27.25, z + 2.5), 'bone')
    m.box('magazine_feed', (-13.75, 14.5, 8), (-12.75, 26, 9.5), 'bone')
    m.box('magazine_feed', (-13.75, 14.5, 6), (-12.75, 15.5, 9.5), 'bone')
    # Crowns on both towers.
    m.box('crown', (-8.5, 31, -0.5), (-4.5, 32, 2.5), 'venous')
    m.box('crown', (29.5, 30, -0.25), (32, 32, 2.75), 'venous')
    m.box('crown', (-3, 25.5, 27), (0, 26.5, 30), 'venous')
    m.box('crown_spike', (-2, 26.5, 28), (-1, 28.5, 29), 'bone')
    # The phonograph horn: throat at the sound-box, bell opening toward the worker over the stone.
    hx, hy = RECORD_AXIS[0], 25.5
    rings = [(10.75, 1.25), (13, 1.75), (15.25, 2.25), (17.5, 2.9), (19.75, 3.6), (22, 4.4), (24.25, 5.2), (26.5, 6.0)]
    for z, r in rings:
        horn_ring(m, hx, hy, r, z, z + 2.25, .5, 'bone')
    horn_ring(m, hx, hy, 6.4, 28.75, 29.5, .9, 'chisel')
    m.box('horn_stay', (hx + .5, 11.5, 16.25), (hx + 1.5, 23, 17.25), 'chisel')
    m.box('horn_collar', (hx - .25, 22.5, 16), (hx + 2, 23.25, 17.5), 'vein')
    m.box('horn_throat_hose', (hx - 1, 24.75, 9.75), (hx + 1, 26.25, 11), 'leather')


def horn_ring(m, hx, hy, r, z0, z1, thick, mat):
    """One octagonal band of the horn: eight plates, every other one turned 45 degrees about Z."""
    for angle in (0, 45):
        for sign in (-1, 1):
            for vertical in (False, True):
                a, b = [0, 0, z0], [0, 0, z1]
                lo, hi = (r - thick, r) if sign > 0 else (-r, -r + thick)
                if vertical:
                    a[0], b[0] = hx + lo, hx + hi
                    a[1], b[1] = hy - r * TAN, hy + r * TAN
                else:
                    a[0], b[0] = hx - r * TAN, hx + r * TAN
                    a[1], b[1] = hy + lo, hy + hi
                m.box('horn', a, b, mat, rot=('z', 45, [hx, hy, z0]) if angle else None)


def static(t):
    m = Model()
    sills(m, t)
    hammer_bay(m, t)
    basin(m, t)
    grinder_bay(m, t)
    if t == 2:
        masterwork(m)
    return m


# ---------------------------------------------------------------- animated parts

def helve(iron):
    m = Model()
    px, py, pz = HELVE_PIVOT
    beam = 'iron' if iron else 'log'
    m.box('helve', (px - 1.5, py - 1.5, 2.5), (px + 1.5, py + 1.5, 21), beam)
    m.ring('helve_strap', 'z', (px, py, 0), 1.9, 1.5, pz - 1.5, pz + 1.5, 'iron' if not iron else 'chisel')
    # Follower finger riding the pattern cylinder.
    m.box('follower', (px - .5, 15, 5), (px + .5, py - 1.5, 6), 'iron')
    m.box('follower_tip', (px - 1, 14.75, 4.75), (px + 1, 15.75, 6.25), 'vein')
    if iron:
        m.box('spring_link', (px - .5, py + 1.5, 4), (px + .5, 24.5, 5), 'iron')
    m.box('tail_weight', (px - 2, py - 1.75, 2.5), (px + 2, py + 2, 4.75), 'iron' if not iron else 'chisel')
    if not iron:
        m.box('cord', (px - .25, py + 1.5, 21.75), (px + .25, 24.75, 22.25), 'leather')
    return m


def spring_pole():
    """Base stage spring pole from the mast lash to the bend."""
    m = Model()
    m.box('spring_pole', (-7.25, 28, 0.5), (-5.75, 29.5, 14), 'log')
    return m


def spring_tip():
    """The pole's bent tip over the hammer head, carrying the cord; it flexes about the bend."""
    m = Model()
    m.box('spring_tip', (-7, 27, 14), (-6, 28.5, 22.5), 'log', rot=('x', 22.5, list(SPRING_BEND)))
    return m


def head(iron):
    m = Model()
    px, py, pz = HELVE_PIVOT
    m.box('head_eye', (px - 2.5, py - 2, 20), (px + 2.5, py + 2, 25), 'iron' if not iron else 'chisel')
    m.box('head', (px - 2, 14, 20.5), (px + 2, py - 2, 24.5), 'iron')
    m.box('head_face', (px - 2.5, 12.75, 20), (px + 2.5, 14, 25), 'anvil')
    if iron:
        m.box('head_crest', (px - .5, py + 2, 21.5), (px + .5, py + 3, 23.5), 'vein')
    return m


def crank():
    m = Model()
    x, y, z = HAMMER_AXIS
    m.octo('mandrel', 'x', HAMMER_AXIS, .6, -15.5, 0, 'iron')
    m.box('crank_web', (-15.5, y - .75, z - .75), (-14.75, y + CRANK_RADIUS + .75, z + .75), 'iron')
    m.box('crank_pin', (-16, y + CRANK_RADIUS - .5, z - .5), (-14.75, y + CRANK_RADIUS + .5, z + .5), 'vein')
    for x0 in (-12, -1.5):
        m.octo('mandrel_chuck', 'x', HAMMER_AXIS, 1.25, x0, x0 + .5, 'chisel')
    return m


def pitman():
    """Connecting rod from the crank pin down to the treadle tail, at the crank's rest height."""
    m = Model()
    x, y, z = HAMMER_AXIS
    top = y + CRANK_RADIUS
    m.box('pitman_rod', (-16, TREADLE_PIVOT[1], z - .5), (-15, top + .5, z + .5), 'oak')
    m.box('pitman_eye', (-16, top - .5, z - 1), (-14.75, top + 1, z + 1), 'iron')
    return m


def treadle():
    m = Model()
    px, py, pz = TREADLE_PIVOT
    x, y, z = HAMMER_AXIS
    m.box('treadle_arm', (-16, py - .5, z - .75), (-14.5, py + .5, pz + 1), 'oak')
    m.box('treadle_plate', (-16, py - .25, pz - 2), (-11.5, py + .75, 31.5), 'plank')
    m.box('treadle_tread', (-15.5, py + .75, pz), (-12, py + 1, 31), 'leather', faces=('up',))
    m.octo('treadle_pivot', 'x', TREADLE_PIVOT, .75, -16, -11, 'iron')
    return m


def wheel():
    m = Model()
    cx, cy, cz = WHEEL_AXIS
    m.disc('wheel', 'z', WHEEL_AXIS, WHEEL_RADIUS, 19, 25, 'rim', cap='stone')
    m.disc('wheel_hub', 'z', WHEEL_AXIS, 2, 18.5, 25.5, 'pivot', cap='pivot')
    m.octo('wheel_axle', 'z', WHEEL_AXIS, .75, 13.25, 30.25, 'iron')
    # Hand crank on the worker's end of the axle.
    m.box('crank_web', (cx - .75, cy - .75, 29.5), (cx + .75, cy + 4.75, 30.25), 'iron')
    m.box('crank_handle', (cx - .75, cy + 3.25, 30.25), (cx + .75, cy + 4.75, 32), 'oak')
    return m


def stylus():
    """Base sound-box on a hinged arm; the needle rests on the recording cylinder."""
    m = Model()
    sx, sy, sz = STYLUS_PIVOT
    rx, ry, rz = RECORD_AXIS
    m.box('stylus_arm', (sx - .5, sy - .5, sz - .75), (sx + .5, sy + .5, rz + .5), 'oak')
    m.octo('stylus_hinge', 'x', STYLUS_PIVOT, .75, sx - 1.25, sx + 1.25, 'iron')
    m.box('stylus_link', (rx - .4, ry + CYLINDER_RADIUS + 3.75, rz - .4), (rx + .4, sy - .5, rz + .4), 'iron')
    sound_box(m, rx, ry + CYLINDER_RADIUS, rz)
    return m


def sound_box(m, x, contact, z):
    m.octo('sound_box', 'y', (x, 0, z), 2.25, contact + 2, contact + 3.5, 'iron', cap='iron')
    m.octo('diaphragm', 'y', (x, 0, z), 1.5, contact + 3.5, contact + 3.75, 'amber', cap='amber')
    m.box('needle_bar', (x - .4, contact + .5, z - .4), (x + .4, contact + 2, z + .4), 'iron')
    m.box('needle', (x - .25, contact, z - .25), (x + .25, contact + .5, z + .25), 'bone')


def carriage():
    """Precision carriage: a nut riding the lead screw that carries the sound-box over the wax."""
    m = Model()
    rx, ry, rz = RECORD_AXIS
    m.box('carriage_nut', (rx - 1.5, 27.75, rz - 1.5), (rx + 1.5, 30.25, rz + 1.5), 'chisel')
    m.box('carriage_shoe', (rx - 1, 30.25, rz - 2.5), (rx + 1, 31.75, rz - .5), 'iron')
    m.box('carriage_hanger', (rx - .5, ry + CYLINDER_RADIUS + 3.75, rz - .5), (rx + .5, 27.75, rz + .5), 'iron')
    m.box('index_pointer', (rx - .25, 28.5, rz + 1.5), (rx + .25, 29.5, rz + 2.5), 'vein')
    sound_box(m, rx, ry + CYLINDER_RADIUS, rz)
    return m


GOVERNOR_SWING = 1.75


def governor():
    m = Model()
    gx, _, gz = GOVERNOR_AXIS
    m.box('spindle', (gx - .4, 12, gz - .4), (gx + .4, 29.5, gz + .4), 'iron')
    m.box('spindle_cap', (gx - 1, 29.5, gz - 1), (gx + 1, 30.5, gz + 1), 'vein')
    m.box('sleeve', (gx - .9, 24, gz - .9), (gx + .9, 25.5, gz + .9), 'vein')
    arm = GOVERNOR_SWING / math.sin(math.radians(45))
    for sign in (-1, 1):
        # Arms splayed at 45 degrees from the spindle cap, balls at their ends.
        m.box('fly_arm', (gx - .25, 29.5 - arm, gz - .25), (gx + .25, 29.5, gz + .25), 'iron',
              rot=('x', 45 * sign, [gx, 29.5, gz]))
        m.box('fly_link', (gx - .2, 25, gz - .2), (gx + .2, 25 + arm * .6, gz + .2), 'iron',
              rot=('x', -45 * sign, [gx, 25, gz]))
        bz = gz - sign * GOVERNOR_SWING
        m.octo('fly_ball', 'x', (gx, 29.5 - GOVERNOR_SWING, bz), 1, gx - 1, gx + 1, 'vein', cap='vein')
    return m


def heart():
    m = Model()
    cx, cy, cz = HEART_CENTER
    m.box('myocardium', (cx - 3, cy - 3.5, cz - 3), (cx + 3, cy + 3, cz + 3), 'flesh')
    m.box('left_lobe', (cx - 4, cy - 2, cz - 2), (cx - 2.5, cy + 2.5, cz + 2.5), 'flesh')
    m.box('ventricle', (cx - .5, cy - 5, cz - 2), (cx + 3, cy - 3, cz + 2.25), 'flesh')
    m.box('apex', (cx + .75, cy - 6, cz - 1), (cx + 2.75, cy - 4.5, cz + 1.25), 'vein')
    m.box('atrium', (cx - 3.25, cy + 2.5, cz - 2.5), (cx + .5, cy + 4.5, cz + 1.5), 'flesh')
    m.box('aorta', (cx - .5, cy + 3, cz - 1), (cx + 1.5, cy + 6, cz + 1), 'vein')
    m.box('aortic_arch', (cx - .5, cy + 4.75, cz - 4.5), (cx + 1.5, cy + 6.5, cz + 1), 'vein')
    m.box('pulmonary', (cx + 1.5, cy + 2, cz + .5), (cx + 3, cy + 5, cz + 2), 'vein')
    m.box('vena_cava', (cx - 5, cy - 1, cz - 1), (cx - 3.5, cy + 1, cz + 1), 'vein')
    for x0, y0, y1 in ((-2.25, -2.5, 2.25), (.75, -4.25, 1.5), (2, -1, 2.5)):
        m.box('coronary', (cx + x0, cy + y0, cz + 3), (cx + x0 + .75, cy + y1, cz + 3.25), 'vein', faces=('south', 'east', 'west', 'up'))
    m.box('coronary', (cx - 2.25, cy + 1.5, cz + 3), (cx + 1.5, cy + 2.25, cz + 3.25), 'vein', faces=('south', 'up', 'down'))
    return m


def cylinder(mat):
    """Pattern cylinder centred on the origin with its axis along X; drawn wherever a slot holds one."""
    m = Model()
    m.octo('cylinder', 'x', (0, 0, 0), CYLINDER_RADIUS, -CYLINDER_HALF, CYLINDER_HALF, mat,
           cap='waxcap' if mat == 'wax' else 'bone')
    m.octo('cylinder_core', 'x', (0, 0, 0), .9, -CYLINDER_HALF - .5, CYLINDER_HALF + .5, 'iron')
    return m


PARTS = {
    'helve': lambda: helve(False), 'helve_iron': lambda: helve(True),
    'head': lambda: head(False), 'head_iron': lambda: head(True),
    'spring_pole': spring_pole, 'spring_tip': spring_tip,
    'crank': crank, 'pitman': pitman, 'treadle': treadle, 'wheel': wheel,
    'stylus': stylus, 'carriage': carriage, 'governor': governor, 'heart': heart,
    'cylinder_wax': lambda: cylinder('wax'), 'cylinder_ambergris': lambda: cylinder('amber'),
}
STAGES = {'resonant_forge': 0, 'resonant_forge_precision': 1, 'resonant_forge_masterwork': 2}


def stage_parts(t):
    """The animated parts each stage shows, for the inventory model and previews."""
    parts = ['crank', 'pitman', 'treadle', 'wheel', 'helve_iron' if t else 'helve', 'head_iron' if t else 'head']
    parts += ['carriage', 'governor'] if t else ['stylus', 'spring_pole', 'spring_tip']
    if t == 2:
        parts.append('heart')
    return parts


def placed_cylinders(source=None):
    """Inventory/preview cylinders placed in both cradles, copied from `source` (default: wax)."""
    out = Model()
    for centre in (HAMMER_AXIS, RECORD_AXIS):
        for e in (source or cylinder('wax')).elements:
            e = dict(e)
            e['a'] = [e['a'][0] + centre[0], e['a'][1] + centre[1], e['a'][2] + centre[2]]
            e['b'] = [e['b'][0] + centre[0], e['b'][1] + centre[1], e['b'][2] + centre[2]]
            if e['rot']:
                axis, angle, origin = e['rot']
                e['rot'] = (axis, angle, [origin[0] + centre[0], origin[1] + centre[1], origin[2] + centre[2]])
            out.elements.append(e)
    return out


# ---------------------------------------------------------------- export

def to_json(model, groups=False):
    used = []
    elements = []
    for e in model.elements:
        faces = {}
        for side, (mat, coords) in e['faces'].items():
            if mat not in used:
                used.append(mat)
            faces[side] = dict(uv=coords, texture='#' + mat)
        el = {'name': e['name'], 'from': e['a'], 'to': e['b']}
        if e['rot']:
            axis, angle, origin = e['rot']
            el['rotation'] = dict(angle=angle, axis=axis, origin=origin)
        el['faces'] = faces
        elements.append(el)
    for el in elements:
        for k in ('from', 'to'):
            assert all(-16 <= v <= 32 for v in el[k]), (el['name'], el[k])
    textures = {'particle': 'hemomancy:block/hematic_iron_block'}
    textures.update({mat: TEXTURES[mat] for mat in used})
    # The bodies span several cells, so vanilla corner occlusion would sample unrelated neighbours.
    return dict(parent='minecraft:block/block', ambientocclusion=False, textures=textures, elements=elements)


def write_json(name, data):
    text = json.dumps(data, indent=1)
    (MODELS / (name + '.json')).write_text(text + '\n', encoding='utf-8')


def bbmodel(name, groups):
    """Editable Blockbench project with the static body and every animated part as named groups."""
    textures, tex_index, elements, outliner = [], {}, [], []
    uid = 0
    for group, model in groups:
        children = []
        for e in model.elements:
            faces = {}
            for side in ('north', 'east', 'south', 'west', 'up', 'down'):
                if side in e['faces']:
                    mat, coords = e['faces'][side]
                    if mat not in tex_index:
                        ns, path = TEXTURES[mat].split(':')
                        folder, file = path.split('/', 1)
                        tex_index[mat] = len(textures)
                        textures.append(dict(path=str(ASSETS.parent / ns / 'textures' / (path + '.png')).replace('\\', '/'),
                                             name=file + '.png', folder=folder, namespace=ns, id=str(len(textures)),
                                             render_mode='default'))
                    faces[side] = dict(uv=coords, texture=tex_index[mat])
                else:
                    faces[side] = dict(uv=[0, 0, 0, 0], texture=None)
            u = '%08x-0000-4000-8000-%012x' % (zlib.crc32(name.encode()), uid)
            uid += 1
            rotation, origin = [0, 0, 0], [8, 8, 8]
            if e['rot']:
                axis, angle, origin = e['rot']
                rotation['xyz'.index(axis)] = angle
            elements.append(dict(name=e['name'], **{'from': e['a'], 'to': e['b']}, rotation=rotation, origin=origin,
                                 faces=faces, uuid=u, box_uv=False, autouv=0, rescale=False))
            children.append(u)
        outliner.append(dict(name=group, origin=[8, 8, 8], children=children))
    return dict(meta=dict(format_version='4.10', model_format='java_block', box_uv=False), name=name,
                resolution=dict(width=16, height=16), elements=elements, outliner=outliner, textures=textures)


def views(model):
    """Java-model-shaped views for coplanar.py; they share each element's from/to lists."""
    out = []
    for e in model.elements:
        view = {'name': e['name'], 'from': e['a'], 'to': e['b'],
                'faces': {side: {'texture': TEXTURES[mat], 'uv': coords} for side, (mat, coords) in e['faces'].items()}}
        if e['rot']:
            axis, angle, origin = e['rot']
            view['rotation'] = {'axis': axis, 'angle': angle, 'origin': origin}
        out.append(view)
    return out


def separate(statics, parts):
    """Pull apart coplanar faces that would z-fight, including between the body and its parts at rest.

    Parts animate, so each must also be clean on its own rather than relying on the body to hide
    its faces. Parts are shared by several stages, so parts and stages are re-checked until a full
    pass moves nothing. Cylinders are held still while the cradles make room for them.
    """
    log = []
    for _ in range(10):
        moved = sum(coplanar.resolve(views(model), log=log) for model in parts.values())
        for t, body in statics.items():
            for name in ('cylinder_wax', 'cylinder_ambergris'):
                cylinders = views(placed_cylinders(parts[name]))
                elements = views(body) + [v for p in stage_parts(t) for v in views(parts[p])] + cylinders
                moved += coplanar.resolve(elements, frozen={id(v) for v in cylinders}, log=log)
        if not moved:
            return log
    raise ValueError("Resonant Forge stages keep moving each other's faces")


def export():
    statics = {t: static(t) for t in STAGES.values()}
    parts = {part: build() for part, build in PARTS.items()}
    separate(statics, parts)
    for name, t in STAGES.items():
        write_json(name, to_json(statics[t]))
    for part, model in parts.items():
        write_json('resonant_forge_' + part, to_json(model))
    item = Model()
    item.elements = copy.deepcopy(statics[0].elements + placed_cylinders(parts['cylinder_wax']).elements
                                  + [e for p in stage_parts(0) for e in parts[p].elements])
    coplanar.resolve(views(item))
    write_json('resonant_forge_item', to_json(item))
    BBMODEL.mkdir(parents=True, exist_ok=True)
    for name, t in STAGES.items():
        groups = [('static_station', statics[t])] + [(p, parts[p]) for p in stage_parts(t)]
        groups.append(('cylinders_at_rest', placed_cylinders(parts['cylinder_wax'])))
        (BBMODEL / (name + '.bbmodel')).write_text(json.dumps(bbmodel(name, groups)), encoding='utf-8')
    for old in ('hammer', 'cam'):
        path = MODELS / ('resonant_forge_' + old + '.json')
        if path.exists():
            path.unlink()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        export()
        print('Exported Resonant Forge stage, part and inventory models.')
    if args.preview:
        import resonant_forge_preview
        print(resonant_forge_preview.render())

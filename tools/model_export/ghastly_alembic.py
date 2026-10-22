"""Author the Ghastly Alembic: a sand-bath cucurbit under a ghast-pale still head, its Hematic
Condenser worm tub, and the Athanor tower furnace that carries the whole train.

Emits the three stage models (plus the lit base), editable Blockbench projects and the generated
AlembicGeometry.java (collision cells and the render anchors GhastlyAlembicRenderer reads).
`--preview` also writes textured renders of every stage to build/ghastly_alembic_preview.png.

Model space is the north-facing controller block: the operator stands to the north (low z). The
train is authored running east (+x) and mirrored on export, so the beak, receiver and worm tub run
west and the Athanor's fuel tower stands east: the base keeps its long-standing west footprint,
leaving the east floor tile open for leak crystals. The Athanor is authored in absolute pixels; its
JSON is lowered by LIFT so it stays inside vanilla's element bounds, and the renderer raises it back.

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
GEOMETRY = ROOT / 'src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/AlembicGeometry.java'

LIFT = 7                                  # Athanor raises the Condenser train onto its hearth
CUCURBIT = (8.0, 8.0)                     # x/z of the boiling vessel's axis
GLASS_RADIUS = 5.0                        # flat-to-flat half width of the octagonal cucurbit
GLASS_BOTTOM, GLASS_TOP = 4.5, 15.0       # glass shows above the sand bath, under the shoulder
FLUID_RADIUS, FLUID_BOTTOM, FLUID_TOP = 4.55, 4.5, 14.75
HEAD = 4.5                                # still head (ambix) half width
BEAK_ORIGIN = (12.5, 21.5, 8.0)           # where the beak leaves the head's east facet
BEAK_ANGLE = -22.5
FIREBOX = (4.0, 1.5, 12.0, 5.0)           # Athanor mouth x0, y0, x1, y1 on the hearth front
FIREBOX_Z = 0.2                           # glow plane: behind the grate bars, in front of the coals
TUB = (23.5, 10.5, 5.0)                   # Condenser worm tub centre x/z and half width
RECEIVER = (23.5, 2.75)                   # Condenser receiver centre x/z
TOWER = (-6.0, 8.0, 3.75)                 # Athanor fuel tower centre x/z and half width

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'glass': 'hemomancy:block/ghastly_alembic_glass',
    'tear': 'hemomancy:block/sanguine_glass',
    'blood': 'hemomancy:block/blood_still',
    'brine': 'hemomancy:block/phlegethontic_ichor_still',
    'ash': 'hemomancy:block/blood_scorched_scab',
    'ember': 'hemomancy:block/hemorrhagic_crust',
    'plank': 'hemomancy:block/blood_wood_planks',
    'log': 'hemomancy:block/blood_wood_log',
    'logtop': 'hemomancy:block/blood_wood_log_top',
    'brick': 'hemomancy:block/polished_venous_stone_bricks',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'stone': 'hemomancy:block/polished_venous_stone',
    'flesh': 'hemomancy:block/bog_flesh',
}
TRANSLUCENT = {'glass'}
CAP = {'bone': 'bonecap', 'pillar': 'pillartop', 'log': 'logtop'}
TAN = math.tan(math.radians(22.5))
DISPLAY = {
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2, 0], 'scale': [0.22, 0.22, 0.22]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 1, 0], 'scale': [0.22, 0.22, 0.22]},
    'ground': {'translation': [0, 3, 0], 'scale': [0.2, 0.2, 0.2]},
    'gui': {'rotation': [30, 225, 0], 'translation': [1, -2, 0], 'scale': [0.4, 0.4, 0.4]},
    'fixed': {'rotation': [0, 180, 0], 'translation': [1, -3.5, 0], 'scale': [0.24, 0.24, 0.24]},
}


class Model:
    def __init__(self):
        self.elements = []

    def box(self, name, a, b, mat, rot=None, faces=None, caps=None):
        """Axis-aligned cube from a to b; rot=(axis, angle, origin) applies a Java model rotation."""
        a, b = [round(min(p, q), 4) for p, q in zip(a, b)], [round(max(p, q), 4) for p, q in zip(a, b)]
        if any(q - p <= 0 for p, q in zip(a, b)):
            return
        out = {}
        for side in faces or ('north', 'south', 'east', 'west', 'up', 'down'):
            m = caps.get(side, mat) if caps else mat
            coords = uv(side, a, b)
            if m in TRANSLUCENT and side not in ('up', 'down'):
                # Each pane spans the whole glass texture so its highlight lands once per facet.
                coords = [0, coords[1], 16, coords[3]]
            out[side] = (m, coords)
        self.elements.append(dict(name=name, a=a, b=b, rot=rot, faces=out))

    def octo(self, name, centre, radius, y0, y1, mat, cap=None):
        """Solid regular octagonal prism standing on y (flat-to-flat 2*radius)."""
        cap = cap or CAP.get(mat, mat)
        cx, cz = centre
        w, n = radius, radius * TAN
        for i, (hx, hz, angle) in enumerate(((w, n, 0), (n, w, 0), (w, n, 45), (w, n, -45))):
            inset = i * .04   # caps sit clear of coplanar.TOLERANCE instead of fighting
            self.box(name, (cx - hx, y0 + inset, cz - hz), (cx + hx, y1 - inset, cz + hz), mat,
                     ('y', angle, [cx, y0, cz]) if angle else None, caps={'up': cap, 'down': cap})

    def shell(self, name, centre, radius, y0, y1, thick, mat, ends=False):
        """Hollow octagon of eight panes, so whatever stands inside stays visible through glass."""
        cx, cz = centre
        half = radius * TAN
        sides = ('north', 'south', 'east', 'west') + (('up', 'down') if ends else ())
        glass = mat in TRANSLUCENT
        for angle in (0, 45):
            rot = ('y', angle, [cx, y0, cz]) if angle else None
            for s in (-1, 1):
                lo, hi = sorted((s * radius, s * (radius - thick)))
                # Glass panes drop their sliver edges: a thin edge face reads as a stripe through the glass.
                xs = ('east', 'west') + sides[4:] if glass else sides
                zs = ('north', 'south') + sides[4:] if glass else sides
                self.box(name, (cx + lo, y0, cz - half), (cx + hi, y1, cz + half), mat, rot, faces=xs)
                self.box(name, (cx - half, y0, cz + lo), (cx + half, y1, cz + hi), mat, rot, faces=zs)

    def ring(self, name, axis, centre, outer, inner, start, end, mat, rot=None):
        """Hollow square collar along `axis`."""
        i = 'xyz'.index(axis)
        o = [k for k in range(3) if k != i]
        for s0, s1 in ((-outer, -inner), (inner, outer)):
            a, b = [0] * 3, [0] * 3
            a[i], b[i] = start, end
            a[o[0]], b[o[0]] = centre[o[0]] + s0, centre[o[0]] + s1
            a[o[1]], b[o[1]] = centre[o[1]] - outer, centre[o[1]] + outer
            self.box(name, a, b, mat, rot)
            a[o[0]], b[o[0]] = centre[o[0]] - inner, centre[o[0]] + inner
            a[o[1]], b[o[1]] = centre[o[1]] + s0, centre[o[1]] + s1
            self.box(name, a, b, mat, rot)

    def mirrored(self):
        """Reflect across x = 8 (east becomes west), re-deriving world-aligned UVs."""
        out = Model()
        swap = {'east': 'west', 'west': 'east'}
        for e in self.elements:
            a = [16 - e['b'][0], e['a'][1], e['a'][2]]
            b = [16 - e['a'][0], e['b'][1], e['b'][2]]
            rot = None
            if e['rot']:
                axis, angle, o = e['rot']
                rot = (axis, angle if axis == 'x' else -angle, [16 - o[0], o[1], o[2]])
            faces = {}
            for side, (mat, _) in e['faces'].items():
                side = swap.get(side, side)
                coords = uv(side, a, b)
                if mat in TRANSLUCENT and side not in ('up', 'down'):
                    coords = [0, coords[1], 16, coords[3]]
                faces[side] = (mat, coords)
            out.elements.append(dict(name=e['name'], a=a, b=b, rot=rot, faces=faces))
        return out

    def lifted(self, dy):
        out = Model()
        for e in self.elements:
            e = copy.deepcopy(e)
            e['a'][1] += dy
            e['b'][1] += dy
            if e['rot']:
                e['rot'][2][1] += dy
            out.elements.append(e)
        return out


def uv(side, a, b):
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
    return [out[0][0], out[1][0], out[0][1], out[1][1]]


# ---------------------------------------------------------------- the still (every stage)

def sand_bath(m, lit):
    """Iron trivet and sand-bath pan; the base and Condenser draw heat through it from below."""
    cx, cz = CUCURBIT
    for sx in (-1, 1):
        for sz in (-1, 1):
            x, z = cx + sx * 4.25, cz + sz * 4.25
            m.box('trivet_leg', (x - .75, .75, z - .75), (x + .75, 2, z + .75), 'iron')
            x, z = cx + sx * 5, cz + sz * 5
            m.box('trivet_claw', (x - 1.25, 0, z - 1.25), (x + 1.25, .75, z + 1.25), 'bold')
    m.box('trivet_bar', (3, .75, 7.4), (13, 1.5, 8.6), 'iron')
    m.box('trivet_bar', (7.4, .75, 3), (8.6, 1.5, 13), 'iron')
    m.octo('pan', CUCURBIT, 6.5, 2, 4.75, 'chisel')
    m.shell('pan_lip', CUCURBIT, 6.85, 4, 5.25, .6, 'iron', ends=True)
    m.octo('sand', CUCURBIT, 6.25, 4.75, 5, 'ember' if lit else 'ash')
    if lit:
        # Coals glowing through the trivet gaps.
        for x0, z0 in ((3.5, 3.5), (9.75, 3.5), (3.5, 9.75), (9.75, 9.75)):
            m.box('coal', (x0, .25, z0), (x0 + 2.75, 1.25, z0 + 2.75), 'ember')


def cucurbit(m):
    m.shell('cucurbit_glass', CUCURBIT, GLASS_RADIUS, GLASS_BOTTOM, GLASS_TOP, .25, 'glass')
    cx, cz = CUCURBIT
    # Four hematic iron straps, clasped by two hoops, hold the glass in the bath.
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        r = GLASS_RADIUS
        x0, x1 = (cx + r, cx + r + .5) if dx > 0 else ((cx - r - .5, cx - r) if dx < 0 else (cx - .6, cx + .6))
        z0, z1 = (cz + r, cz + r + .5) if dz > 0 else ((cz - r - .5, cz - r) if dz < 0 else (cz - .6, cz + .6))
        m.box('cucurbit_strap', (x0, 4.75, z0), (x1, GLASS_TOP, z1), 'iron')
    for y in (7.75, 11.75):
        m.shell('cucurbit_hoop', CUCURBIT, GLASS_RADIUS + .3, y, y + .75, .3, 'iron', ends=True)
    m.octo('shoulder', CUCURBIT, GLASS_RADIUS + .5, GLASS_TOP, 16.5, 'chisel')
    m.octo('shoulder_crown', CUCURBIT, 3.75, 16.5, 17.5, 'iron')
    m.octo('neck', CUCURBIT, 1.75, 17.5, 19.75, 'iron')
    m.octo('neck_collar', CUCURBIT, 2.25, 18.25, 19, 'chisel')


def still_head(m):
    """The ambix: a ghast-pale bone dome weeping blood tears, with its gutter rim and finial."""
    cx, cz = CUCURBIT
    m.octo('head_gutter', CUCURBIT, HEAD + .5, 19.5, 20.5, 'iron')
    m.octo('head_dome', CUCURBIT, HEAD, 20.5, 24, 'bone')
    m.shell('head_band', CUCURBIT, HEAD + .2, 23.25, 24, .3, 'iron', ends=True)
    m.octo('head_shoulder', CUCURBIT, 3.25, 24, 26, 'bone')
    m.octo('head_crown', CUCURBIT, 1.75, 26, 27.25, 'iron')
    m.octo('finial_knob', CUCURBIT, 1.1, 27.25, 28.75, 'venous')
    m.box('finial_spike', (cx - .4, 28.75, cz - .4), (cx + .4, 31, cz + .4), 'bone')
    # Face on the front facet: shut eyes over a slack mouth, with tears running to the gutter.
    z = cz - HEAD
    for x0 in (cx - 1.6, cx + .6):
        m.box('ghast_eye', (x0, 22, z - .1), (x0 + 1, 23, z), 'ash', faces=('north', 'east', 'west', 'up', 'down'))
        m.box('ghast_tear', (x0 + .3, 20.5, z - .15), (x0 + .7, 22, z - .05), 'tear',
              faces=('north', 'east', 'west', 'down'))
    m.box('ghast_mouth', (cx - .65, 20.75, z - .1), (cx + .65, 21.5, z), 'ash', faces=('north', 'east', 'west', 'up', 'down'))


def beak(m, reach):
    """The head's beak, sloping down toward +x; returns where it ends."""
    ox, oy, oz = BEAK_ORIGIN
    rot = ('z', BEAK_ANGLE, [ox, oy, oz])
    m.box('beak_root', (ox - .75, oy - 1, oz - 1), (ox + 2.5, oy + 1, oz + 1), 'iron', rot)
    m.box('beak', (ox + 2.5, oy - .65, oz - .65), (ox + reach, oy + .65, oz + .65), 'iron', rot)
    m.box('beak_band', (ox + 2.25, oy - .9, oz - .9), (ox + 3, oy + .9, oz + .9), 'chisel', rot)
    a = math.radians(BEAK_ANGLE)
    return ox + reach * math.cos(a), oy + reach * math.sin(a), oz


def drop(m, tip, bottom):
    """Elbow at the beak's tip and the vertical drop pipe beneath it."""
    x, y, z = tip
    m.box('beak_elbow', (x - 1, y - 1.25, z - 1), (x + 1, y + .75, z + 1), 'chisel')
    m.box('drop_pipe', (x - .6, bottom, z - .6), (x + .6, y - 1.25, z + .6), 'iron')
    m.box('drop_collar', (x - .9, bottom + .5, z - .9), (x + .9, bottom + 1.25, z + .9), 'chisel')


def retort_stand(m):
    """Iron stand behind the still whose clamp steadies the neck."""
    m.box('stand_foot', (0, 0, 11.75), (3.5, 1, 15.5), 'bold')
    m.box('stand_post', (1, 1, 12.75), (2.5, 25.5, 14.25), 'pillar', caps={'up': 'pillartop'})
    m.box('stand_cap', (.75, 25.5, 12.5), (2.75, 26.25, 14.5), 'chisel')
    m.box('stand_arm', (2.5, 18.25, 13), (6.75, 19, 14), 'iron')
    m.ring('stand_clamp', 'y', (8, 0, 8), 2.75, 2.25, 18.25, 19, 'iron')
    m.box('stand_arm', (6.5, 18.25, 10.25), (7.5, 19, 13.5), 'iron')


def base_receiver(m):
    """The basic train ends in a small receiving vial on a bone pedestal."""
    rx, rz = 21.0, 8.0
    m.box('pedestal', (19.25, 0, 6.25), (22.75, 1.5, 9.75), 'bone', caps={'up': 'bonecap', 'down': 'bonecap'})
    m.octo('vial_foot', (rx, rz), 2.5, 1.5, 2.5, 'chisel')
    m.shell('vial_glass', (rx, rz), 2.25, 2.5, 9.5, .2, 'glass')
    m.octo('vial_distillate', (rx, rz), 2, 2.5, 5, 'blood')
    m.octo('vial_lid', (rx, rz), 2.5, 9.5, 10.25, 'chisel')
    m.octo('vial_neck', (rx, rz), 1.25, 10.25, 11, 'chisel')


# ---------------------------------------------------------------- Hematic Condenser

def condenser(m):
    tx, tz, tr = TUB
    # Blood-wood worm tub, iron-hooped, brimming with dark brine around the worm.
    m.octo('tub', (tx, tz), tr, 0, 12.5, 'plank', cap='plank')
    for y in (1.5, 6, 10.5):
        m.shell('tub_hoop', (tx, tz), tr + .3, y, y + 1, .4, 'iron', ends=True)
    m.shell('tub_rim', (tx, tz), tr, 12.5, 13.25, .75, 'plank', ends=True)
    m.octo('tub_brine', (tx, tz), tr - .75, 12.5, 12.75, 'brine')
    m.ring('worm_turn', 'y', (tx, 0, tz), 3, 2.4, 12.6, 13.2, 'iron')
    m.ring('worm_turn', 'y', (tx, 0, tz), 1.9, 1.3, 12.6, 13.4, 'iron')
    # Tap on the tub front, dripping into the receiver.
    rx, rz = RECEIVER
    m.box('tap_spout', (rx - .6, 7.5, rz), (rx + .6, 8.5, tz - tr + .25), 'iron')
    m.box('tap_mouth', (rx - .75, 7, rz - .75), (rx + .75, 7.5, rz + .75), 'chisel')
    m.box('tap_key', (rx - .25, 8.5, rz + 1.25), (rx + .25, 9.75, rz + 1.75), 'iron')
    m.box('tap_key', (rx - 1, 9.75, rz + 1.25), (rx + 1, 10.25, rz + 1.75), 'iron')
    # Receiver globe.
    m.octo('receiver_foot', (rx, rz), 2.75, 0, 1, 'chisel')
    m.shell('receiver_glass', (rx, rz), 2.5, 1, 6, .2, 'glass')
    m.octo('receiver_distillate', (rx, rz), 2.25, 1, 3.5, 'blood')
    m.octo('receiver_lid', (rx, rz), 2.75, 6, 6.5, 'chisel')
    m.octo('receiver_neck', (rx, rz), 1.25, 6.5, 7, 'chisel')
    # Return line from the tub foot back into the bath, carrying the cohobation.
    m.box('return_pipe', (13.75, 1, 9.75), (tx - tr + .25, 2, 10.75), 'iron')
    m.box('return_valve', (15.5, .75, 9.5), (16.75, 2.75, 11), 'chisel')
    m.box('return_wheel', (15.75, 2.75, 9.25), (16.5, 3.25, 11.25), 'iron')
    # Cooling fins along the long beak.
    ox, oy, oz = BEAK_ORIGIN
    rot = ('z', BEAK_ANGLE, [ox, oy, oz])
    for x in (5, 6.5, 8):
        m.ring('beak_fin', 'x', (ox, oy, oz), 1.4, .65, ox + x, ox + x + .5, 'chisel', rot)


# ---------------------------------------------------------------- Athanor

def athanor(m):
    """Tower furnace: a hearth under the still, fuel tower opposite the train, bellows behind."""
    m.box('hearth_plinth', (0, 0, 0), (16, 1.5, 16), 'brick')
    m.box('hearth', (.5, 1.5, .5), (15.5, 6, 15.5), 'iron')
    for x0, z0 in ((.25, .25), (14.25, .25), (.25, 14.25), (14.25, 14.25)):
        m.box('hearth_post', (x0, 1.5, z0), (x0 + 1.5, 6, z0 + 1.5), 'pillar', caps={'up': 'pillartop', 'down': 'pillartop'})
    m.box('hearth_coping', (0, 6, 0), (16, 7, 16), 'chisel')
    x0, y0, x1, y1 = FIREBOX
    m.box('firebox_back', (x0, y0, .45), (x1, y1, .5), 'ash', faces=('north',))
    m.box('firebox_coals', (x0, y0, .35), (x1, y0 + 1.25, .5), 'ember', faces=('north', 'up'))
    for a, b in (((x0 - 1.5, y0), (x0, y1 + 1.25)), ((x1, y0), (x1 + 1.5, y1 + 1.25)), ((x0, y1), (x1, y1 + 1.25))):
        m.box('firebox_surround', (a[0], a[1], .25), (b[0], b[1], .5), 'brick', faces=('north', 'east', 'west', 'up', 'down'))
    m.box('firebox_jamb', (x0 - .75, y0, 0), (x0, y1 + .5, .25), 'iron')
    m.box('firebox_jamb', (x1, y0, 0), (x1 + .75, y1 + .5, .25), 'iron')
    m.box('firebox_lintel', (x0 - .75, y1, -.25), (x1 + .75, y1 + .75, .25), 'bold')
    for x in (5.5, 7.75, 10):
        m.box('firebox_bar', (x, y0, -.1), (x + .5, y1, .15), 'iron')
    # Stoking apron in front and the bellows behind, both kept low.
    m.box('apron', (1.5, 0, -3), (14.5, 1, -.5), 'stone')
    m.box('apron_ash', (4.5, 1, -2.25), (11.5, 1.4, -.5), 'ash', faces=('north', 'east', 'west', 'up'))
    m.box('apron_ember', (6.5, 1.4, -1.75), (9, 1.6, -.75), 'ember', faces=('north', 'east', 'west', 'up'))
    m.box('bellows_board', (3, 1, 16.5), (13, 1.75, 22), 'plank')
    m.box('bellows_lung', (3.5, 1.75, 16.75), (12.5, 4.5, 21.25), 'flesh')
    m.box('bellows_board', (3, 4.5, 16.5), (13, 5.25, 22), 'plank')
    m.box('bellows_nozzle', (7, 2.25, 15.5), (9, 3.75, 16.75), 'iron')
    m.box('bellows_handle', (7.25, 5.25, 20.5), (8.75, 6, 23.5), 'log')
    # Fuel tower (the slow Henry): bricks hooped in iron, a capped hopper and the flue.
    tx, tz, tr = TOWER
    m.box('tower_duct', (tx + tr - .5, 1.5, 5.75), (.75, 5, 10.25), 'brick')
    m.box('tower_duct_band', (tx + tr, 1.25, 5.5), (-1, 5.25, 10.5), 'chisel')
    m.octo('tower_plinth', (tx, tz), tr + .5, 0, 1.5, 'stone')
    m.octo('tower', (tx, tz), tr, 1.5, 27, 'iron')
    for y in (1.5, 9, 16.5, 24):
        m.shell('tower_hoop', (tx, tz), tr + .3, y, y + 1, .4, 'chisel', ends=True)
    m.box('tower_door', (tx - 1.5, 9.5, tz - tr - .25), (tx + 1.5, 13.5, tz - tr), 'bold')
    m.box('tower_door_latch', (tx + .75, 11, tz - tr - .75), (tx + 1.25, 12, tz - tr - .25), 'iron')
    m.octo('tower_cap', (tx, tz), tr + .5, 27, 28.5, 'chisel')
    m.octo('tower_hopper', (tx, tz), 3.25, 28.5, 30.5, 'iron')
    m.octo('flue', (tx, tz), 1.5, 30.5, 37.5, 'pillar')
    m.octo('flue_cap', (tx, tz), 2, 37, 38, 'chisel')
    # Iron trestle carrying the worm tub and receiver level with the raised still.
    for x0 in (16.5, 28.25):
        for z0 in (0, 15):
            m.box('trestle_leg', (x0, 0, z0), (x0 + 1, 6, z0 + 1), 'pillar', caps={'up': 'pillartop', 'down': 'pillartop'})
    m.box('trestle_brace', (17.5, 2, 15.25), (28.25, 2.75, 15.75), 'iron')
    m.box('trestle_brace', (28.5, 2, 1), (29, 2.75, 15), 'iron')
    m.box('trestle_deck', (16, 6, 0), (29.25, 7, 16), 'chisel')


def glass_texture():
    """Pale, mostly clear pane with one highlight streak and soft edges."""
    from PIL import Image
    image = Image.new('RGBA', (16, 16))
    for x in range(16):
        colour, alpha = (226, 212, 218), 34
        if x in (3, 4):
            colour, alpha = (255, 242, 246), 118
        elif x in (2, 5):
            colour, alpha = (240, 226, 232), 64
        elif x in (0, 15):
            colour, alpha = (196, 176, 186), 82
        for y in range(16):
            speck = x == 11 and y % 6 == 1
            image.putpixel((x, y), colour + ((alpha + 50) if speck else alpha,))
    image.save(ASSETS / 'textures/block/ghastly_alembic_glass.png')


def static(t, lit=False):
    m = Model()
    sand_bath(m, lit)
    cucurbit(m)
    still_head(m)
    retort_stand(m)
    if t == 0:
        drop(m, beak(m, 9.25), 11)
        base_receiver(m)
    else:
        tx, tz, _ = TUB
        drop(m, beak(m, (tx - BEAK_ORIGIN[0]) / math.cos(math.radians(BEAK_ANGLE))), 12.75)
        condenser(m)
    if t == 2:
        m = m.lifted(LIFT)
        athanor(m)
    return m.mirrored()


STAGES = {'ghastly_alembic': (0, False), 'ghastly_alembic_on': (0, True),
          'ghastly_alembic_condenser': (1, False), 'ghastly_alembic_athanor': (2, False)}


# ---------------------------------------------------------------- export

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


def to_json(model, name, dy=0):
    used, elements = [], []
    for e in model.elements:
        faces = {}
        for side, (mat, coords) in e['faces'].items():
            if mat not in used:
                used.append(mat)
            faces[side] = dict(uv=coords, texture='#' + mat)
        a = [e['a'][0], round(e['a'][1] - dy, 4), e['a'][2]]
        b = [e['b'][0], round(e['b'][1] - dy, 4), e['b'][2]]
        assert all(-16 <= v <= 32 for v in a + b), (name, e['name'], a, b)
        el = {'name': e['name'], 'from': a, 'to': b}
        if e['rot']:
            axis, angle, origin = e['rot']
            el['rotation'] = dict(angle=angle, axis=axis, origin=[origin[0], round(origin[1] - dy, 4), origin[2]])
        el['faces'] = faces
        elements.append(el)
    textures = {'particle': 'hemomancy:block/hematic_iron_block'}
    textures.update({mat: TEXTURES[mat] for mat in used})
    # Tall glass and the open frame read wrong with vanilla corner occlusion.
    return {'parent': 'minecraft:block/block',
            'render_type': 'translucent', 'ambientocclusion': False, 'gui_light': 'front',
            'textures': textures, 'elements': elements, 'display': DISPLAY}


def bbmodel(name, model, dy=0):
    textures, tex_index, elements, children = [], {}, [], []
    for uid, e in enumerate(model.elements):
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
        rotation, origin = [0, 0, 0], [8, 8, 8]
        if e['rot']:
            axis, angle, o = e['rot']
            rotation['xyz'.index(axis)] = angle
            origin = [o[0], o[1] - dy, o[2]]
        elements.append(dict(name=e['name'], **{'from': [e['a'][0], e['a'][1] - dy, e['a'][2]],
                                                 'to': [e['b'][0], e['b'][1] - dy, e['b'][2]]},
                             rotation=rotation, origin=origin, faces=faces, uuid=u, box_uv=False, autouv=0,
                             rescale=False))
        children.append(u)
    return dict(meta=dict(format_version='4.10', model_format='java_block', box_uv=False), name=name,
                ambientocclusion=False, front_gui_light=True, resolution=dict(width=16, height=16),
                elements=elements, outliner=[dict(name=name, origin=[8, 8, 8], children=children)],
                textures=textures, display=DISPLAY)


def bounds(e):
    """Axis-aligned bounds of an element after its rotation, for collision."""
    corners = [(x, y, z) for x in (e['a'][0], e['b'][0]) for y in (e['a'][1], e['b'][1]) for z in (e['a'][2], e['b'][2])]
    if e['rot']:
        axis, angle, o = e['rot']
        m = coplanar._matrix({'axis': axis, 'angle': angle})
        corners = [tuple(c + o[i] for i, c in enumerate(coplanar._apply(m, tuple(p[i] - o[i] for i in range(3)))))
                   for p in corners]
    lo = [math.floor(round(min(c[i] for c in corners) * 4, 3)) / 4 for i in range(3)]
    hi = [math.ceil(round(max(c[i] for c in corners) * 4, 3)) / 4 for i in range(3)]
    return lo + hi


def geometry(stages):
    rows = []
    for model in stages:
        boxes = sorted({tuple(bounds(e)) for e in model.elements})
        rows.append('        {\n' + ''.join('            {%s},\n' % ', '.join(repr(float(v)) for v in box)
                                         for box in boxes) + '        },\n')
    x0, y0, x1, y1 = FIREBOX
    java = GEOMETRY_TEMPLATE.format(
        lift=LIFT, fluid_radius=FLUID_RADIUS, fluid_bottom=FLUID_BOTTOM, fluid_top=FLUID_TOP,
        fx0=x0, fy0=y0, fx1=x1, fy1=y1, fz=FIREBOX_Z, boxes=''.join(rows))
    GEOMETRY.write_text(java, encoding='utf-8')


GEOMETRY_TEMPLATE = '''package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Generated by tools/model_export/ghastly_alembic.py from the authored stage models. */
public final class AlembicGeometry {{
    public static final double ATHANOR_LIFT = {lift}.0 / 16.0;
    /** Octagonal fluid column inside the cucurbit, in model pixels of the unlifted still. */
    public static final float FLUID_RADIUS = {fluid_radius}f, FLUID_BOTTOM = {fluid_bottom}f, FLUID_TOP = {fluid_top}f;
    /** Athanor firebox mouth on the hearth front (unlifted pixels): x0, y0, x1, y1 and glow plane z. */
    public static final float[] FIREBOX = {{{fx0}f, {fy0}f, {fx1}f, {fy1}f, {fz}f}};
    private static final double[][][] BOXES = {{
{boxes}    }};
    private static final Map<BlockPos, VoxelShape>[][] CELLS = buildCells();

    private AlembicGeometry() {{}}

    public static Map<BlockPos, VoxelShape> cells(int stage, Direction facing) {{
        return CELLS[Math.clamp(stage, 0, 2)][facing.get2DDataValue()];
    }}

    public static VoxelShape shape(int stage, Direction facing, BlockPos offset) {{
        return cells(stage, facing).getOrDefault(offset, Shapes.empty());
    }}

    @SuppressWarnings("unchecked")
    private static Map<BlockPos, VoxelShape>[][] buildCells() {{
        Map<BlockPos, VoxelShape>[][] result = new Map[3][4];
        for (int stage = 0; stage < 3; stage++) {{
            for (Direction facing : Direction.Plane.HORIZONTAL) {{
                Map<BlockPos, VoxelShape> cells = new LinkedHashMap<>();
                for (double[] box : BOXES[stage]) {{
                    double x0=box[0], z0=box[2], x1=box[3], z1=box[5];
                    int turns = switch (facing) {{ case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; }};
                    for (int turn = 0; turn < turns; turn++) {{
                        double nextX0=16-z1, nextX1=16-z0;
                        z0=x0; z1=x1; x0=nextX0; x1=nextX1;
                    }}
                    VoxelShape shape = Block.box(x0, box[1], z0, x1, box[4], z1);
                    for (int x=(int)Math.floor(x0/16); x<(int)Math.ceil(x1/16); x++)
                        for (int y=(int)Math.floor(box[1]/16); y<(int)Math.ceil(box[4]/16); y++)
                            for (int z=(int)Math.floor(z0/16); z<(int)Math.ceil(z1/16); z++) {{
                                BlockPos offset = new BlockPos(x,y,z);
                                VoxelShape clipped = Shapes.join(shape.move(-x,-y,-z), Shapes.block(), BooleanOp.AND);
                                if (!clipped.isEmpty()) cells.merge(offset, clipped, Shapes::or);
                            }}
                }}
                // One box per cell keeps the hover outline readable over the glass; collision follows it.
                cells.replaceAll((offset, shape) -> Shapes.create(shape.bounds()));
                result[stage][facing.get2DDataValue()] = Map.copyOf(cells);
            }}
        }}
        return result;
    }}
}}
'''


def export():
    glass_texture()
    built = {name: static(t, lit) for name, (t, lit) in STAGES.items()}
    for model in built.values():
        coplanar.resolve(views(model), translucent={TEXTURES[m] for m in TRANSLUCENT})
    BBMODEL.mkdir(parents=True, exist_ok=True)
    for name, model in built.items():
        dy = LIFT if STAGES[name][0] == 2 else 0
        (MODELS / (name + '.json')).write_text(json.dumps(to_json(model, name, dy), indent=1) + '\n', encoding='utf-8')
        if name != 'ghastly_alembic_on':
            (BBMODEL / (name + '.bbmodel')).write_text(json.dumps(bbmodel(name, model, dy)), encoding='utf-8')
    geometry([built['ghastly_alembic'], built['ghastly_alembic_condenser'], built['ghastly_alembic_athanor']])
    for stale in ('textures/block/ghastly_alembic_condenser_atlas.png', 'textures/block/ghastly_alembic_athanor_atlas.png'):
        path = ASSETS / stale
        if path.exists():
            path.unlink()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        export()
        print('Exported Ghastly Alembic stage models, Blockbench projects and AlembicGeometry.java.')
    if args.preview:
        import ghastly_alembic_preview
        print(ghastly_alembic_preview.render())

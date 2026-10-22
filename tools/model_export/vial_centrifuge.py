"""Author the Vial Centrifuge: a swing-bucket bench centrifuge over a splash bowl, its Calibrated
governor and dial, and the Fractionating rotor that forks its spin into paired layered receivers.

Emits the three stage bodies, the animated parts VialCentrifugeRenderer draws and editable
Blockbench projects. `--preview` also writes textured renders of every stage to
build/vial_centrifuge_preview.png.

Model space is the north-facing block: the operator stands to the north (low z) and the dial faces
them. Everything stays on one block's footprint; the rotor and governor rise above it. Rotor angles
run from +x toward -z, the direction a positive Y rotation turns in both block models and PoseStack,
so slot k's bucket hangs at k * 45 degrees and balanced pairs face each other. Buckets are authored
hanging at angle 0 and swing outward about the tangent axis through PIVOT. Pivots mirror the
constants in VialCentrifugeRenderer.

Python 3 + Pillow + NumPy.
"""
from pathlib import Path
import argparse
import json
import math
import zlib

import coplanar

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODELS = ASSETS / 'models/block'
BBMODEL = MODELS / 'bbmodel'
PREFIX = 'vial_centrifuge'

# Shared with VialCentrifugeRenderer (model pixels).
CENTRE = (8.0, 8.0)
PIVOT = (12.0, 14.75, 8.0)         # slot-0 bucket trunnion; buckets swing about Z through it
MAX_SWING = 50.0                   # VialCentrifugeRotorRules.MAX_SWING
CRANK_AXIS = (4.5, 8.0)            # y/z of the crank and flywheel shaft (along X)
CRANK_RADIUS = 2.25
DIAL = (8.0, 4.75, 1.375)          # calibration needle pivot (about Z)
HINGE = (8.5, 22.0, 8.0)           # flyball arm hinge at angle 0, swings about Z
FLYBALL_ARM = 3.5
JAR = (3.0, 3.0)                   # blood reservoir jar axis
JAR_FLUID = (1.3, 2.4, 6.2)        # reservoir fluid radius, bottom and full height
SPLEEN = (8.0, 4.5, 14.125)        # Fractionating spleen pulse centre
RECEIVERS = ((3.0, 13.5), (13.0, 13.5))
BOWL_RADIUS, BOWL_RIM = 6.75, 11.0

TEXTURES = {
    'plank': 'hemomancy:block/blood_wood_planks',
    'log': 'hemomancy:block/blood_wood_log',
    'logtop': 'hemomancy:block/blood_wood_log_top',
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'hyphae': 'hemomancy:block/calcified_hyphae',
    'glass': 'hemomancy:block/ghastly_alembic_glass',
    'sanguine': 'hemomancy:block/sanguine_glass',
    'blood': 'hemomancy:block/blood_still',
    'amber': 'hemomancy:block/mnemonic_ambergris_block',
    'flesh': 'hemomancy:block/bog_flesh',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'scab': 'hemomancy:block/blood_scorched_scab',
    'brick': 'hemomancy:block/polished_venous_stone_bricks',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
}
TRANSLUCENT = {'glass', 'sanguine'}
CAP = {'bone': 'bonecap', 'pillar': 'pillartop', 'log': 'logtop'}
TAN = math.tan(math.radians(22.5))


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
            # Glass panes keep the world-aligned, face-sized UVs too, so the glass sits at block texel density.
            out[side] = (m, coords)
        self.elements.append(dict(name=name, a=a, b=b, rot=rot, faces=out))

    def octo(self, name, axis, centre, radius, start, end, mat, cap=None):
        """Solid regular octagonal prism along `axis` (flat-to-flat 2*radius)."""
        cap = cap or CAP.get(mat, mat)
        i = 'xyz'.index(axis)
        o = [k for k in range(3) if k != i]
        ends = {'x': ('east', 'west'), 'y': ('up', 'down'), 'z': ('north', 'south')}[axis]
        for n, (h0, h1, angle) in enumerate(((radius, radius * TAN, 0), (radius * TAN, radius, 0),
                                             (radius, radius * TAN, 45), (radius, radius * TAN, -45))):
            inset = n * .04   # caps sit clear of coplanar.TOLERANCE instead of fighting
            a, b = [0] * 3, [0] * 3
            a[i], b[i] = start + inset, end - inset
            a[o[0]], b[o[0]] = centre[o[0]] - h0, centre[o[0]] + h0
            a[o[1]], b[o[1]] = centre[o[1]] - h1, centre[o[1]] + h1
            self.box(name, a, b, mat, (axis, angle, list(centre)) if angle else None,
                     caps={ends[0]: cap, ends[1]: cap})

    def shell(self, name, centre, radius, y0, y1, thick, mat, ends=False):
        """Hollow upright octagon of eight panes, so whatever stands inside stays visible."""
        cx, cz = centre
        half = radius * TAN
        sides = ('north', 'south', 'east', 'west') + (('up', 'down') if ends else ())
        for angle in (0, 45):
            rot = ('y', angle, [cx, y0, cz]) if angle else None
            for s in (-1, 1):
                lo, hi = sorted((s * radius, s * (radius - thick)))
                self.box(name, (cx + lo, y0, cz - half), (cx + hi, y1, cz + half), mat, rot, faces=sides)
                self.box(name, (cx - half, y0, cz + lo), (cx + half, y1, cz + hi), mat, rot, faces=sides)

    def disc(self, name, axis, centre, radius, start, end, mat, cap=None):
        """Stepped round prism; caps share one UV frame so the face reads whole."""
        cap = cap or mat
        i = 'xyz'.index(axis)
        o = [k for k in range(3) if k != i]
        ends = {'x': ('east', 'west'), 'y': ('up', 'down'), 'z': ('north', 'south')}[axis]
        for n, degrees in enumerate((14, 38, 52, 76)):
            h0 = round(radius * math.cos(math.radians(degrees)) * 4) / 4
            h1 = round(radius * math.sin(math.radians(degrees)) * 4) / 4
            a, b = [0] * 3, [0] * 3
            a[i], b[i] = start + n * .04, end - n * .04
            a[o[0]], b[o[0]] = centre[o[0]] - h0, centre[o[0]] + h0
            a[o[1]], b[o[1]] = centre[o[1]] - h1, centre[o[1]] + h1
            before = len(self.elements)
            self.box(name, a, b, mat)
            face = []
            for k in o:
                lo = (a[k] - (centre[k] - radius)) / (2 * radius) * 16
                hi = (b[k] - (centre[k] - radius)) / (2 * radius) * 16
                face.append((round(lo, 3), round(hi, 3)))
            for side in ends:
                self.elements[before]['faces'][side] = (cap, [face[0][0], 16 - face[1][1], face[0][1], 16 - face[1][0]])

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

    def radial(self, name, step, r0, r1, t0, t1, y0, y1, mat):
        """Box spanning radius r0..r1 and tangent t0..t1 about CENTRE, placed at step * 45 degrees.

        Quarter turns are exact; odd steps add vanilla's 45 degree rotation about the axis.
        """
        cx, cz = CENTRE
        c, s = [(1, 0), (0, 1), (-1, 0), (0, -1)][(step // 2) % 4]
        xs = [cx + c * r + s * t for r in (r0, r1) for t in (t0, t1)]
        zs = [cz - s * r + c * t for r in (r0, r1) for t in (t0, t1)]
        rot = ('y', 45, [cx, y0, cz]) if step % 2 else None
        self.box(name, (min(xs), y0, min(zs)), (max(xs), y1, max(zs)), mat, rot)


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


# ---------------------------------------------------------------- body (every stage)

def plinth(m, t):
    if t == 0:
        # Two blood-wood sleepers under a plank deck, strapped at the corners.
        for z0 in (1.5, 12):
            m.box('sleeper', (.5, 0, z0), (15.5, 1, z0 + 2.5), 'log', caps={'east': 'logtop', 'west': 'logtop'})
        m.box('deck', (1, 1, 1), (15, 2, 15), 'plank')
        for x0 in (.75, 13.75):
            for z0 in (.75, 13.75):
                m.box('corner_strap', (x0, .75, z0), (x0 + 1.5, 2.25, z0 + 1.5), 'iron')
    else:
        m.box('plinth', (1, 0, 1), (15, 1.5, 15), 'brick')
        m.box('bed_plate', (1.5, 1.5, 1.5), (14.5, 2, 14.5), 'chisel')
        for x0 in (.5, 13):
            for z0 in (.5, 13):
                m.box('cast_foot', (x0, 0, z0), (x0 + 2.5, 2.25, z0 + 2.5), 'bold')


def drum(m, t):
    """Gear drum under the bowl; the drive shaft enters it from the east."""
    body, band = ('plank', 'iron') if t == 0 else ('iron', 'chisel')
    m.octo('drum', 'y', (CENTRE[0], 0, CENTRE[1]), 5, 2, 7, body)
    for y in ((2.5, 3.25), (5.75, 6.5)):
        m.shell('drum_hoop', CENTRE, 5.3, y[0], y[1], .4, band, ends=True)
    y, z = CRANK_AXIS
    m.octo('shaft_bearing', 'x', (0, y, z), 1, 12.9, 14, 'chisel')


def bowl(m, t):
    """Splash bowl the buckets hang into; its stained floor is where every spin leaves its mark."""
    wall, lip = ('plank', 'iron') if t == 0 else ('iron', 'chisel')
    cx, cz = CENTRE
    m.octo('bowl_floor', 'y', (cx, 0, cz), BOWL_RADIUS, 7, 8, wall)
    m.shell('bowl_wall', CENTRE, BOWL_RADIUS, 8, 10.5, .6, wall)
    m.shell('bowl_lip', CENTRE, BOWL_RADIUS + .35, 10.5, BOWL_RIM, .6, lip, ends=True)
    if t == 0:
        m.shell('bowl_hoop', CENTRE, BOWL_RADIUS + .25, 8.5, 9.25, .3, 'iron', ends=True)
    m.octo('bowl_stain', 'y', (cx, 0, cz), BOWL_RADIUS - .65, 8, 8.2, 'scab')
    m.octo('spindle_boss', 'y', (cx, 0, cz), 1.5, 8, 9.25, 'chisel')


def reservoir(m, t):
    """Blood jar at the front-left corner, filled through a spout off the bowl's diagonal facet."""
    jx, jz = JAR
    m.octo('jar_foot', 'y', (jx, 0, jz), 1.85, 2, 2.35, 'chisel')
    m.shell('jar_glass', JAR, 1.6, 2.35, 6.25, .2, 'glass')
    m.octo('jar_collar', 'y', (jx, 0, jz), 1.85, 6.25, 6.6, 'iron' if t == 0 else 'chisel')
    m.octo('jar_drop', 'y', (jx, 0, jz), .45, 6.6, 9.75, 'iron')
    # Spout authored running west out of the bowl, then turned to the north-west facet.
    cx, cz = CENTRE
    m.box('jar_spout', (cx - 7.4, 9, cz - .4), (cx - 6.4, 9.75, cz + .4), 'iron', ('y', -45, [cx, 9, cz]))


def powder_tray(m, t):
    """Open tray at the front-right corner where the hematic iron powder settles."""
    tx, tz = 13.25, 2.75
    wall = 'plank' if t == 0 else 'iron'
    m.ring('tray_wall', 'y', (tx, 0, tz), 1.5, 1.1, 2, 4.5, wall)
    m.box('tray_floor', (tx - 1.1, 2, tz - 1.1), (tx + 1.1, 2.25, tz + 1.1), wall)
    m.box('powder', (tx - 1.1, 2.25, tz - 1.1), (tx + 1.1, 3.75, tz + 1.1), 'scab')
    m.box('powder_mound', (tx - .65, 3.75, tz - .65), (tx + .65, 4.2, tz + .65), 'scab')


def brake(m):
    """Base stage: a hand brake on the drum front, leaning toward the operator."""
    origin = [8, 3.5, 2.55]
    m.octo('brake_pivot', 'x', origin, .5, 6.75, 9.25, 'iron')
    m.box('brake_lever', (7.6, 3.5, 2.25), (8.4, 7.25, 2.85), 'iron', ('x', -22.5, origin))
    m.box('brake_grip', (7.45, 5.75, 2.1), (8.55, 7, 3), 'bone', ('x', -22.5, origin))


def dial(m):
    """Calibrated stage: the bone-rimmed red calibration dial read through the needle part."""
    x, y, z = DIAL
    m.box('dial_boss', (7.25, 4, 2.6), (8.75, 5.5, 3.1), 'chisel')
    m.octo('dial_rim', 'z', (x, y, 0), 2.1, 1.75, 2.75, 'bone', cap='bonecap')
    m.octo('dial_face', 'z', (x, y, 0), 1.6, 1.6, 1.75, 'vein')
    for degrees in (-120, -60, 0, 60, 120):
        a = math.radians(degrees)
        # Positive degrees sit on the operator's right, the -x side as they face south.
        tx, ty = x - 1.25 * math.sin(a), y + 1.25 * math.cos(a)
        m.box('dial_tick', (tx - .15, ty - .15, 1.5), (tx + .15, ty + .15, 1.6), 'hyphae',
              faces=('north', 'east', 'west', 'up', 'down'))


def fractionation(m):
    """Fractionating stage: a forked crosshead off the bowl feeding two layered receivers."""
    for n, (rx, rz) in enumerate(RECEIVERS):
        m.octo('receiver_foot', 'y', (rx, 0, rz), 1.85, 2, 2.4, 'chisel')
        m.shell('receiver_glass', (rx, rz), 1.6, 2.4, 6.4, .2, 'glass')
        # Packed red cells, the pale buffy coat, then plasma; the second fraction is mostly plasma.
        red, coat = (4.2, 4.5) if n == 0 else (3.3, 3.55)
        m.octo('receiver_cells', 'y', (rx, 0, rz), 1.35, 2.45, red, 'blood')
        m.octo('receiver_coat', 'y', (rx, 0, rz), 1.35, red, coat, 'hyphae')
        m.octo('receiver_plasma', 'y', (rx, 0, rz), 1.35, coat, 5.6, 'amber')
        m.octo('receiver_collar', 'y', (rx, 0, rz), 1.85, 6.4, 6.75, 'chisel')
        m.box('receiver_drop', (rx - .4, 6.75, rz - .4), (rx + .4, 8.75, rz + .4), 'iron')
        m.box('receiver_elbow', (rx - .4, 8.75, rz - .4), (rx + .4, 9.5, 15.75), 'iron')
        m.box('receiver_band', (rx - 1.85, 3.9, rz - .3), (rx + 1.85, 4.3, rz + .3), 'bold')
    m.box('crosshead', (3.4, 8.75, 15), (12.6, 9.5, 15.75), 'iron')
    m.box('crosshead_fork', (7, 8.5, 14.6), (9, 10.25, 16), 'bold')
    m.box('crosshead_spout', (7.4, 9.5, 13.9), (8.6, 10.1, 14.6), 'chisel')
    # The spleen's hilum vessels feed both receivers.
    for x0, x1 in ((4.55, 5.85), (10.15, 11.45)):
        m.box('hilum_vessel', (x0, 4.6, 13.9), (x1, 5.2, 14.5), 'vein')


def static(t):
    m = Model()
    plinth(m, t)
    drum(m, t)
    bowl(m, t)
    reservoir(m, t)
    powder_tray(m, t)
    if t == 0:
        brake(m)
    else:
        dial(m)
    if t == 2:
        fractionation(m)
    return m


# ---------------------------------------------------------------- animated parts

def rotor(t):
    """Spindle, hub and eight forked arms; each fork carries one bucket's trunnion pin."""
    m = Model()
    cx, cz = CENTRE
    px, py, _ = PIVOT
    radius = px - cx
    arm = 'plank' if t == 0 else 'iron'
    hub = 'log' if t == 0 else 'chisel'
    m.octo('spindle', 'y', (cx, 0, cz), .6, 8.5, py + 2.5, 'iron')
    m.octo('hub', 'y', (cx, 0, cz), 1.6, py - .5, py + 1.75, hub, cap='logtop' if t == 0 else 'chisel')
    m.octo('hub_cap', 'y', (cx, 0, cz), 1, py + 1.75, py + 2.5, 'bone' if t == 0 else 'iron')
    for step in range(4):
        m.radial('arm', step, -(radius - .25), radius - .25, -.375, .375, py + .5, py + 1.25, arm)
    for step in range(8):
        m.radial('fork', step, radius - .3, radius + .3, -1.4, 1.4, py + .5, py + 1.25, arm)
        for sign in (-1, 1):
            m.radial('fork_cheek', step, radius - .3, radius + .3, sign * 1.12, sign * 1.4, py - .6, py + .5, 'iron')
        if t > 0:
            m.radial('index_mark', step, radius - .15, radius + .15, -.15, .15, py + 1.25, py + 1.5, 'hyphae')
    if t == 2:
        # Fractionating rotor: a banding hoop ties the forks; a red glass fraction collar crowns the hub.
        m.shell('fraction_hoop', CENTRE, radius + .6, py + .55, py + 1.2, .3, 'chisel', ends=True)
        m.octo('fraction_collar', 'y', (cx, 0, cz), 2.4, py + 1.75, py + 2.25, 'sanguine')
    return m


def bucket():
    """Slot-0 swing bucket hanging from its trunnion pin."""
    m = Model()
    px, py, pz = PIVOT
    m.ring('bucket_cup', 'y', (px, 0, pz), .85, .6, py - 4.25, py - 2, 'iron')
    m.box('bucket_floor', (px - .6, py - 4.25, pz - .6), (px + .6, py - 4, pz + .6), 'iron')
    for sign in (-1, 1):
        m.box('bucket_strap', (px - .3, py - 2.5, pz + sign * .85), (px + .3, py + .3, pz + sign * 1.1), 'iron')
    m.octo('bucket_pin', 'z', (px, py, 0), .3, pz - 1.35, pz + 1.35, 'chisel')
    return m


def vial_glass():
    m = Model()
    px, py, pz = PIVOT
    m.shell('vial_glass', (px, pz), .55, py - 4, py - .8, .12, 'glass', ends=True)
    m.box('vial_stopper', (px - .45, py - .8, pz - .45), (px + .45, py - .4, pz + .45), 'hyphae')
    return m


def vial_contents(kind):
    """Blood for a filled vial, a dried crust for a spent one, sacred amber for a syringe sample."""
    m = Model()
    px, py, pz = PIVOT
    top = {'blood': py - 1.5, 'spent': py - 3.5, 'hallowed': py - 1.75}[kind]
    mat = {'blood': 'blood', 'spent': 'scab', 'hallowed': 'amber'}[kind]
    m.box('vial_' + kind, (px - .4, py - 3.95, pz - .4), (px + .4, top, pz + .4), mat)
    if kind == 'spent':
        m.box('vial_smear', (px - .43, top, pz - .2), (px - .41, py - 1.2, pz + .2), 'vein', faces=('east', 'west'))
    return m


def crank():
    m = Model()
    y, z = CRANK_AXIS
    m.octo('crank_shaft', 'x', (0, y, z), .45, 13.5, 15.6, 'iron')
    m.box('crank_web', (15.25, y - .6, z - .6), (15.75, y + CRANK_RADIUS + .6, z + .6), 'iron')
    m.octo('crank_handle', 'x', (0, y + CRANK_RADIUS, z), .5, 15.75, 17.25, 'log', cap='logtop')
    return m


def flywheel():
    m = Model()
    y, z = CRANK_AXIS
    m.octo('flywheel_shaft', 'x', (0, y, z), .45, 13.5, 15.25, 'iron')
    m.disc('flywheel', 'x', (0, y, z), 3.25, 15.1, 16, 'iron', cap='bold')
    m.octo('flywheel_hub', 'x', (0, y, z), .9, 14.6, 16.3, 'chisel')
    m.octo('flywheel_knob', 'x', (0, y + 2.5, z), .45, 16, 17, 'bone', cap='bonecap')
    return m


def governor(t):
    """Spindle extension carrying the flyball hinges; it turns with the rotor."""
    m = Model()
    cx, cz = CENTRE
    hx, hy, hz = HINGE
    top = PIVOT[1] + 2.5
    m.octo('governor_spindle', 'y', (cx, 0, cz), .45, top, hy + .5, 'iron')
    m.octo('governor_sleeve', 'y', (cx, 0, cz), .9, top + 1.5, top + 2.5, 'chisel')
    m.octo('governor_cap', 'y', (cx, 0, cz), .9, hy + .5, hy + 1.25, 'chisel')
    for sign in (-1, 1):
        m.box('hinge_lug', (cx + sign * .45, hy - .35, hz - .45), (cx + sign * (hx - cx + .35), hy + .5, hz + .45), 'iron')
    if t == 2:
        m.octo('governor_crown', 'y', (cx, 0, cz), .6, hy + 1.25, hy + 2.25, 'venous')
        m.box('governor_spike', (cx - .25, hy + 2.25, cz - .25), (cx + .25, hy + 3.5, cz + .25), 'bone')
    else:
        m.octo('governor_knob', 'y', (cx, 0, cz), .5, hy + 1.25, hy + 1.75, 'iron')
    return m


def flyball():
    """One governor arm at angle 0, hanging 22.5 degrees out; it swings about Z through HINGE."""
    m = Model()
    hx, hy, hz = HINGE
    rot = ('z', 22.5, [hx, hy, hz])
    m.box('flyball_arm', (hx - .175, hy - FLYBALL_ARM, hz - .175), (hx + .175, hy, hz + .175), 'iron', rot)
    a = math.radians(22.5)
    bx, by = hx + FLYBALL_ARM * math.sin(a), hy - FLYBALL_ARM * math.cos(a)
    m.octo('flyball', 'z', (bx, by, 0), .8, hz - .8, hz + .8, 'vein')
    return m


def needle():
    m = Model()
    x, y, z = DIAL
    m.box('needle', (x - .15, y, z - .075), (x + .15, y + 1.35, z + .075), 'hyphae')
    m.octo('needle_hub', 'z', (x, y, 0), .35, z - .2, 1.6, 'chisel')
    return m


def spleen():
    """The Fractionating stage's grafted spleen, filtering what the rotor throws off."""
    m = Model()
    cx, cy, cz = SPLEEN
    m.octo('spleen', 'x', (0, cy, cz), 1.75, cx - 2.25, cx + 2.25, 'flesh')
    m.octo('spleen_pole', 'x', (0, cy - .25, cz), 1.25, cx - 2.75, cx - 2.25, 'flesh')
    m.octo('spleen_pole', 'x', (0, cy + .25, cz + .125), 1.25, cx + 2.25, cx + 2.75, 'flesh')
    m.octo('spleen_swell', 'y', (cx + .5, 0, cz + .5), 1.25, cy + 1.25, cy + 2, 'flesh')
    # The hilum notch, where the splenic vessels enter on the face the operator rarely sees.
    m.box('splenic_vein', (cx - 2, cy + .25, cz + 1.6), (cx + 1.75, cy + .75, cz + 1.85), 'vein',
          faces=('south', 'up', 'down', 'east', 'west'))
    m.box('splenic_vein', (cx - .5, cy - 1.25, cz + 1.6), (cx, cy + .25, cz + 1.85), 'vein',
          faces=('south', 'east', 'west', 'down'))
    return m


def reservoir_blood():
    """Unit-height reservoir fluid at the jar's floor; the renderer scales it by stored blood."""
    m = Model()
    radius, bottom, _ = JAR_FLUID
    m.octo('reservoir_blood', 'y', (JAR[0], 0, JAR[1]), radius, bottom, bottom + 1, 'blood')
    return m


PARTS = {
    'rotor': lambda: rotor(0), 'rotor_calibrated': lambda: rotor(1), 'rotor_fractionating': lambda: rotor(2),
    'bucket': bucket, 'vial_glass': vial_glass, 'vial_blood': lambda: vial_contents('blood'),
    'vial_spent': lambda: vial_contents('spent'), 'vial_hallowed': lambda: vial_contents('hallowed'),
    'crank': crank, 'flywheel': flywheel, 'governor': lambda: governor(1),
    'governor_crowned': lambda: governor(2), 'flyball': flyball, 'needle': needle, 'spleen': spleen,
    'reservoir_blood': reservoir_blood,
}
STAGES = {'vial_centrifuge': 0, 'vial_centrifuge_calibrated': 1, 'vial_centrifuge_fractionating': 2}


def stage_parts(t):
    """Animated parts each stage shows at rest (buckets are placed per slot separately)."""
    if t == 0:
        return ['rotor', 'crank']
    parts = ['rotor_calibrated' if t == 1 else 'rotor_fractionating', 'flywheel', 'needle',
             'governor' if t == 1 else 'governor_crowned']
    return parts + (['spleen'] if t == 2 else [])


# ---------------------------------------------------------------- clearance

def swing_clearance():
    """Fully swung buckets must stay over the bowl rim and inside the block's footprint."""
    px, py, _ = PIVOT
    radius = px - CENTRE[0]
    a = math.radians(MAX_SWING)
    worst_r, lowest = 0, 99
    for e in bucket().elements:
        for x in (e['a'][0], e['b'][0]):
            for y in (e['a'][1], e['b'][1]):
                for z in (e['a'][2], e['b'][2]):
                    dx, dy = x - px, y - py
                    r = radius + dx * math.cos(a) - dy * math.sin(a)
                    yy = py + dx * math.sin(a) + dy * math.cos(a)
                    worst_r = max(worst_r, math.hypot(r, z - PIVOT[2]))
                    if r > BOWL_RADIUS - .6:
                        lowest = min(lowest, yy)
    assert worst_r < 8, ('swung bucket leaves the block', worst_r)
    assert lowest > BOWL_RIM, ('swung bucket clips the bowl rim', lowest)
    return worst_r, lowest


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


def to_json(model, translucent):
    used, elements = [], []
    for e in model.elements:
        faces = {}
        for side, (mat, coords) in e['faces'].items():
            if mat not in used:
                used.append(mat)
            faces[side] = dict(uv=coords, texture='#' + mat)
        assert all(-16 <= v <= 32 for v in e['a'] + e['b']), (e['name'], e['a'], e['b'])
        el = {'name': e['name'], 'from': e['a'], 'to': e['b']}
        if e['rot']:
            axis, angle, origin = e['rot']
            el['rotation'] = dict(angle=angle, axis=axis, origin=origin)
        el['faces'] = faces
        elements.append(el)
    textures = {'particle': 'hemomancy:block/hematic_iron_block'}
    textures.update({mat: TEXTURES[mat] for mat in used})
    out = {'parent': 'minecraft:block/block'}
    if translucent:
        out['render_type'] = 'translucent'
    # The open frame and glass read wrong with vanilla corner occlusion.
    out.update(ambientocclusion=False, textures=textures, elements=elements)
    return out


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
                ambientocclusion=False, resolution=dict(width=16, height=16), elements=elements,
                outliner=outliner, textures=textures)


def blockstates():
    variants = {}
    for name, stage in STAGES.items():
        for facing, angle in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            entry = {'model': 'hemomancy:block/' + name}
            if angle:
                entry['y'] = angle
            variants['facing=%s,stage=%d' % (facing, stage)] = entry
    return {'variants': variants}


def separate(statics, parts):
    """Pull apart coplanar faces, each part on its own and every stage with its parts at rest.

    Parts animate, so each must be clean alone rather than relying on the body to hide its faces.
    Parts are shared by stages, so passes repeat until nothing moves.
    """
    translucent = {TEXTURES[m] for m in TRANSLUCENT}
    for _ in range(10):
        moved = sum(coplanar.resolve(views(model), translucent=translucent) for model in parts.values())
        for t, body in statics.items():
            elements = views(body) + [v for p in stage_parts(t) for v in views(parts[p])]
            moved += coplanar.resolve(elements, translucent=translucent)
        if not moved:
            return
    raise ValueError("Vial Centrifuge stages keep moving each other's faces")


def export():
    swing_clearance()
    statics = {t: static(t) for t in STAGES.values()}
    parts = {part: build() for part, build in PARTS.items()}
    separate(statics, parts)
    for name, t in STAGES.items():
        (MODELS / (name + '.json')).write_text(json.dumps(to_json(statics[t], True), indent=1) + '\n', encoding='utf-8')
    for part, model in parts.items():
        glass = any(mat in TRANSLUCENT for e in model.elements for mat, _ in e['faces'].values())
        (MODELS / ('%s_%s.json' % (PREFIX, part))).write_text(json.dumps(to_json(model, glass), indent=1) + '\n',
                                                              encoding='utf-8')
    BBMODEL.mkdir(parents=True, exist_ok=True)
    for name, t in STAGES.items():
        groups = [('static_station', statics[t])] + [(p, parts[p]) for p in stage_parts(t)]
        groups.append(('bucket_slot_0', parts['bucket']))
        if t:
            groups.append(('flyball', parts['flyball']))
        (BBMODEL / (name + '.bbmodel')).write_text(json.dumps(bbmodel(name, groups)), encoding='utf-8')
    (ASSETS / 'blockstates/vial_centrifuge.json').write_text(json.dumps(blockstates(), indent=2) + '\n', encoding='utf-8')
    for stale in ('models/block/bbmodel/CentrifugeArmsModel.bbmodel', 'models/block/bbmodel/CentrifugeStandModel.bbmodel',
                  'models/block/bbmodel/ModelCentrifugeArms.bbmodel', 'textures/entity/model_centrifuge_arms.png',
                  'textures/entity/model_centrifuge_stand.png'):
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
        print('Exported Vial Centrifuge stage bodies, animated parts, Blockbench projects and blockstate.')
    if args.preview:
        import vial_centrifuge_preview
        print(vial_centrifuge_preview.render())

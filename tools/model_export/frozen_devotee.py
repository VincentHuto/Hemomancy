"""Author the Frozen Devotee: one of Velorum's congregation, stilled at prayer and left under rime; and
the Witness Devotee, one of his Procession who still turns its head.

A robed figure in undyed grey-blue cloth, crusted pale blue-white where the frost gathered: thickest
on the shoulders, the hood and the hem along the floor, with icicles hanging from the sleeves, the
hood's brim and the chin. Faint full-bright frost lines sit in the robe's folds. The skin that shows
(face, hands, the soles of the feet) is pallid and frost-bitten.

Five poses share one set of parts and differ only in posture:
  0 kneeling   upright on its heels, hooded, hands clasped at the chest;
  1 bowed      torso bent forward from the hips, bare frost-whitened head bowed further;
  2 reaching   upright, bare head tipped back, both arms raised forward toward the Saint;
  3 prostrate  hips raised over the heels, back rounded down to a hooded forehead on the floor,
               arms stretched out ahead along the floor; fits in the lower cell, so its upper model is empty;
  4 striding   the Procession: standing mid-step, hooded, head carried a little ahead, hands hidden in
               the joined sleeves; the left leg swung forward with its bare foot flat ahead of the hem,
               the right swung back on its toes with the heel lifted, the robe hanging in a drape between.

Parts are authored upright in one body frame and posed by rigid turns about x (the hip, the neck, the
shoulders, the robe's swing at the thigh). A turn's quarter-turns are baked into the box exactly; the
remainder (+-22.5 or +-45 degrees) becomes the element's own rotation about its centre, so every
element stays a legal block model element. Icicles are placed after posing so they always hang
straight down.

Head (blockstate `head` forward|left|right): turned, the head looks toward the figure's own left (-x)
or right (+x). A block element turns about one axis only, so each pose gets the closest legal read:
  kneeling, striding  the head is level, so it simply yaws +-45 about the vertical neck axis;
  bowed      the bowed head's -45 x tilt cannot share an element with a y turn: the turned head comes
             level, sinks 2.25 px into the bent shoulders and hangs 1.5 px ahead of the chest, then
             yaws 45 (the bent trunk still carries the bow; the head lifts off the chest to look aside);
  reaching   the tipped-back head (+22.5 x) comes level under the raised arms and yaws 45;
  prostrate  the body's long axis is z, so a turn of the head relative to the body is a roll about z.
             A 45 roll leaves a hood-sized diamond standing on its corner; instead the head lies level,
             face down (-90 x, all baked), and rolls a baked quarter turn (no rotation spent) onto its
             cheek, face to the side: the way a prone body turns its head.
A turned head's corners swing out by a third of its width, so it is brought forward or back on the
neck until it stays inside z 1..15. y-turned elements that cross the storey seam are cut there (a cut
across a vertical axis turn stays legal). A cell the turn leaves unchanged shares the forward model.

The Witness Devotee reuses the striding models: its lower cell and its unlit upper cell are the
devotee's own; lit, the upper is the striding head turned to its `turn` side with two full-bright
pale blue-white eye glints on the hooded face (witness_devotee_upper_lit_<left|right>).

Emits the block models, one shared atlas, both blockstates and editable Blockbench projects (the
kneeling devotee, the lit witness). Python 3 + Pillow + NumPy. `--preview` writes to
build/model-previews/: a turnaround per pose (frozen_devotee_<pose>.png), the line-up
(frozen_devotee_lineup.png), every head per pose (frozen_devotee_heads.png), a mixed-head procession
(frozen_devotee_procession.png) and the witness, lit and at low light (witness_devotee[_night].png).

Coordinates are block pixels for facing=north, the figure facing -z, authored 32 px tall and cut at
y = 16 into the lower and upper cells. Everything stays inside x 1..15, z 1..15.
"""
import argparse
import copy
import json
import math

import numpy as np
from PIL import Image, ImageDraw

import coplanar
from clairaudiograph import clamp, noise, render
from fixture_atlas import ASSETS, ROOT, box, build, item_elements, mirror_x, model_json, preview, separate, spin, \
    split_storeys, write

ATLAS = 'frozen_devotee'
PARTICLE = f'hemomancy:block/{ATLAS}'
POSES = ('kneeling', 'bowed', 'reaching', 'prostrate', 'striding')
# Faint, not a lamp: the frost lines read at light 7 in a dark nave.
FROST_GLOW = {'shade': False, 'neoforge_data': {'block_light': 7, 'sky_light': 7}}

# Cold and saturated on purpose: Minecraft's warm block light turns paler frost tan underground.
CLOTH = (52, 60, 86)
RIME = (180, 208, 246)
RIME_SHADOW = (138, 174, 232)
ICE = (110, 160, 236)
SKIN = (178, 176, 190)
FROSTBITE = (150, 168, 208)
HAIR = (200, 206, 218)

# Pivots in the upright body frame, as (y, z).
TORSO_TOP = 14.5
HIP = (5.5, 10.75)
NECK = (15, 10.75)
SHOULDER = (13.5, 10.75)
ELBOW = (9.5, 10.75)
BOWED_SETBACK = 1      # the bowed figure sits back on its heels so its head stays inside the cell


# --- posing ---------------------------------------------------------------------------------

def _rot(theta):
    c, s = math.cos(math.radians(theta)), math.sin(math.radians(theta))
    return np.array([[c, -s], [s, c]])   # acts on (y, z): +theta turns +y toward +z, as the game does


IDENTITY = (0.0, np.eye(2), np.zeros(2))


def turn(theta, pivot):
    m = _rot(theta)
    p = np.array(pivot, dtype=float)
    return float(theta), m, p - m @ p


def shift(dy, dz):
    return 0.0, np.eye(2), np.array([dy, dz], dtype=float)


def then(*steps):
    """Compose posing steps, the first applied first."""
    theta, m, t = IDENTITY
    for s_theta, s_m, s_t in steps:
        theta, m, t = theta + s_theta, s_m @ m, s_m @ t + s_t
    return theta, m, t


def apply_point(pose, y, z):
    _, m, t = pose
    return m @ np.array([y, z]) + t


NORMALS = {'up': (1, 0), 'down': (-1, 0), 'south': (0, 1), 'north': (0, -1)}


def _quarter_face(face, k):
    if face not in NORMALS:
        return face
    n = np.rint(_rot(90 * k) @ np.array(NORMALS[face], dtype=float)).astype(int)
    return next(f for f, v in NORMALS.items() if tuple(n) == v)


def place(el, pose):
    """The element moved by a pose: quarter-turns baked into the box, the rest as a rotation about its centre."""
    theta, m, t = pose
    out = copy.deepcopy(el)
    if theta == 0 and not t.any():
        return out
    assert not el['rotation'], f'{el["name"]} is already rotated'
    k = round(theta / 90)
    rest = theta - 90 * k
    lo, hi = np.array(el['lo'], dtype=float), np.array(el['hi'], dtype=float)
    centre = (lo + hi) / 2
    half = (hi - lo) / 2
    if k % 2:
        half[1], half[2] = half[2], half[1]
    moved = m @ centre[1:] + t
    out['lo'] = [lo[0], moved[0] - half[1], moved[1] - half[2]]
    out['hi'] = [hi[0], moved[0] + half[1], moved[1] + half[2]]
    out['faces'] = {_quarter_face(f, k): v for f, v in el['faces'].items()}
    out['skip'] = {_quarter_face(f, k) for f in el['skip']}
    if abs(rest) > 1e-6:
        assert any(abs(rest - a) < 1e-6 for a in (-45, -22.5, 22.5, 45)), (el['name'], theta)
        out['rotation'] = spin('x', rest, (centre[0], moved[0], moved[1]))
    return out


def posed(elements, pose):
    return [place(el, pose) for el in elements]


def pair(el):
    return [el, mirror_x(el)]


LOOKS = ('forward', 'left', 'right')
# The figure faces -z, so its own left is -x: a +45 turn about y swings the face toward -x.
YAW = {'forward': 0, 'left': 45, 'right': -45}
# A face-down head (prostrate) turns a whole quarter about the body's long axis, z: the face (down) swings to -x
# (the figure's left) under a -90 turn, to +x under +90.
ROLL = {'left': -90, 'right': 90}
# +90 about z carries east to up, up to west, west to down and down to east.
ROLL_FACES = {'east': 'up', 'up': 'west', 'west': 'down', 'down': 'east'}


def turned(elements, look, x, z):
    """A head (and what hangs from it) yawed about the vertical neck axis through (x, z) toward the figure's own
    left or right. y turns keep icicles hanging straight down."""
    if look == 'forward':
        return elements
    out = copy.deepcopy(elements)
    for el in out:
        assert not el['rotation'], f'{el["name"]} is already rotated'
        el['rotation'] = spin('y', YAW[look], (x, (el['lo'][1] + el['hi'][1]) / 2, z))
    return out


def rolled(elements, look, x, y):
    """A face-down head rolled a baked quarter turn about the z axis through (x, y): it lies on its cheek with the
    face to one side. Boxes stay axis-aligned, so no rotation is spent."""
    if look == 'forward':
        return elements
    turns = ROLL[look] // 90 % 4
    out = []
    for el in elements:
        assert not el['rotation'], f'{el["name"]} is already rotated'
        el = copy.deepcopy(el)
        for _ in range(turns):
            (x0, y0), (x1, y1) = el['lo'][:2], el['hi'][:2]
            el['lo'][:2] = [x - (y1 - y), y + (x0 - x)]
            el['hi'][:2] = [x - (y0 - y), y + (x1 - x)]
            el['faces'] = {ROLL_FACES.get(f, f): v for f, v in el['faces'].items()}
            el['skip'] = {ROLL_FACES.get(f, f) for f in el['skip']}
        out.append(el)
    return out


def split_yawed(elements, at=16):
    """Cut y-turned elements that cross a storey seam: a cut across a vertical axis turn is still a legal element,
    and split_storeys files a rotated element whole by its origin."""
    out = []
    for el in elements:
        rot = el['rotation']
        if not rot or rot['axis'] != 'y' or not el['lo'][1] < at < el['hi'][1]:
            out.append(el)
            continue
        bottom, top = copy.deepcopy(el), copy.deepcopy(el)
        bottom['hi'][1] = top['lo'][1] = at
        bottom['skip'].add('up')
        top['skip'].add('down')
        for part in (bottom, top):
            part['rotation'] = dict(rot, origin=[rot['origin'][0], (part['lo'][1] + part['hi'][1]) / 2,
                                                 rot['origin'][2]])
        out += [bottom, top]
    return out


# --- shared parts (upright body frame) ------------------------------------------------------

def frost_line(name, lo, hi):
    return box(name, lo, hi, 'frost', extra=FROST_GLOW)


def seiza_base():
    """Legs folded under the robe: a long lap with the knees forward, the seat on the heels behind it."""
    e = [box('lap', (3.5, 0, 3), (12.5, 4.5, 10), 'robe', skip=('down',)),
         box('seat', (3.75, 0, 9.5), (12.25, 6, 14), 'robe', skip=('down',)),
         box('lap_rime', (4, 4.5, 3.5), (12, 5, 8.5), 'rime', skip=('down',))]
    e += pair(box('knee', (3.75, 0, 2), (7.75, 4, 3), 'robe', skip=('down', 'south')))
    e += pair(box('knee_crust', (4, 3.5, 1.75), (7.5, 4.75, 3.5), 'rime'))
    e += pair(box('sole', (4.75, .25, 14), (7.25, 2.25, 14.75), 'skin', south='sole', skip=('down', 'north')))
    e += hem_drifts(3.5, 12.5, 2.75, 14.25)
    e += pair(frost_line('knee_fold', (5.5, .5, 1.9), (5.75, 3, 2)))
    e += pair(frost_line('lap_side_fold', (3.4, 1, 5.5), (3.5, 3.75, 5.75)))
    return e


def hem_drifts(x0, x1, z0, z1):
    """The floor-side crust: rime caked on the robe's hem where it meets the floor, drifted at the corners."""
    return [box('hem_crust', (x0 - .25, 0, z0), (x1 + .25, 1.5, z1), 'rime', skip=('down', 'up')),
            box('hem_drift', (x0 - 1, 0, z0 + .5), (x0 - .25, 1, z0 + 3), 'rime', skip=('down',)),
            box('hem_drift', (x1 + .25, 0, z1 - 3.5), (x1 + 1, .75, z1 - 1), 'rime', skip=('down',)),
            box('hem_drift', (7.25, 0, z1), (8.75, .75, z1 + .5), 'rime', skip=('down',))]


def torso(top=TORSO_TOP):
    """The robed trunk from the hips up, crusted at the shoulders, belted with a frozen cord."""
    e = [box('torso', (4.5, 5, 8.5), (11.5, top, 13), 'robe', skip=('down',))]
    e.append(box('belt', (4.25, 6.5, 8.25), (11.75, 7.5, 13.25), 'cord'))
    e.append(box('belt_knot', (7.25, 5.25, 8), (8.75, 7.25, 8.25), 'cord', skip=('south',)))
    e.append(box('shoulder_crust', (3.5, top - 1, 8), (12.5, top + .75, 13.5), 'rime'))
    e.append(box('back_crust', (5.5, top - 4.5, 13), (10.5, top - 1, 13.5), 'rime', skip=('north',)))
    if top >= TORSO_TOP:   # a shortened trunk is too short for the long fold lines
        e.append(frost_line('back_fold', (7.75, 7.5, 13), (8, top - 5, 13.1)))
        e += pair(frost_line('torso_side_fold', (4.4, 8, 10.5), (4.5, top - 2, 10.75)))
    return e


def arms_clasped():
    """Upper arms at the sides, wide sleeves folded forward, bare hands pressed together at the chest."""
    e = []
    e += pair(box('upper_arm', (2.25, 9, 9), (4.5, 14.5, 12.5), 'robe'))
    e += pair(box('arm_crust', (2, 13, 8.75), (4.75, 15, 12.75), 'rime'))
    e += pair(box('sleeve', (2.5, 8.5, 5.5), (7, 11.5, 10), 'robe'))
    e += pair(box('sleeve_drape', (2.75, 7, 5.75), (6.5, 8.5, 9), 'robe', skip=('up',)))
    e += pair(box('cuff_crust', (2.75, 6.5, 5.75), (6.5, 7, 9), 'rime', skip=('up',)))
    e += pair(frost_line('sleeve_fold', (4.5, 7.25, 5.65), (4.75, 10.5, 5.75)))
    e.append(box('hands', (6.5, 9.5, 4.5), (9.5, 14, 6.5), 'skin', north='hands', up='knuckles'))
    return e


def upper_arm():
    """The shoulder-to-elbow half of a straight arm hanging from SHOULDER."""
    return [box('arm', (2, ELBOW[0], 9.25), (4.5, 14, 12.25), 'robe'),
            box('arm_crust', (1.75, 12, 9), (4.75, 14.5, 12.5), 'rime'),
            frost_line('arm_fold', (1.9, 10, 10.5), (2, 12, 10.75))]


def forearm():
    """The elbow-to-hand half: a wide crusted cuff and an open, frost-bitten hand."""
    return [box('forearm', (2, 7, 9.25), (4.5, ELBOW[0], 12.25), 'robe'),
            box('cuff', (1.5, 5, 8.75), (5, 7, 12.75), 'robe'),
            box('cuff_crust', (1.5, 4.75, 8.75), (5, 5, 12.75), 'rime', skip=('up',)),
            box('hand', (2.25, 2.25, 10.75), (4.25, 4.75, 12.25), 'skin', north='palm', down='knuckles')]


def both(elements, pose):
    """A left-side part and its mirror, both moved by the same pose."""
    return posed(elements, pose) + posed([mirror_x(el) for el in elements], pose)


def head(hooded, drop=0):
    """A head bowed or raised about the neck; hooded, or bare with frost-whitened hair. `drop` lowers it onto a
    shorter trunk."""
    e = [box('neck', (6.5, 14, 9.25), (9.5, 15.75, 12.25), 'skin'),
         box('head', (5, 15.5, 7.75), (11, 21.5, 13.75), 'skin' if hooded else 'hair',
             north='face_hooded' if hooded else 'face_bare', **({} if hooded else
                                                               {'east': 'hair_side', 'west': 'hair_side'}))]
    if hooded:
        e.append(box('hood_top', (4.5, 21.5, 7.25), (11.5, 22.5, 14.25), 'hood'))
        e += pair(box('hood_side', (4, 15, 7.25), (5, 22, 14.25), 'hood'))
        e.append(box('hood_back', (5, 15, 13.75), (11, 21.5, 14.75), 'hood'))
        e.append(box('hood_crust', (4.25, 22.5, 7.5), (11.75, 23.25, 14), 'rime'))
        e.append(box('brim_crust', (3.75, 21.25, 6.75), (12.25, 22.75, 7.75), 'rime'))
        e += pair(frost_line('hood_fold', (3.9, 16, 11.5), (4, 20.5, 11.75)))
    else:
        e.append(box('hair_crust', (5.5, 21.5, 8.25), (10.5, 22, 13.25), 'rime'))
        e.append(box('fringe', (5, 20.75, 7.5), (11, 21.5, 7.75), 'hair', skip=('south',)))
        e.append(box('hair_back', (5.25, 16.5, 13.75), (10.75, 21, 14), 'hair', skip=('north',)))
    for el in e:
        el['lo'][1] -= drop
        el['hi'][1] -= drop
    return e


def icicle(name, x, y, z, length):
    """Hangs straight down from (x, y, z): a 1 px shank tapering to a half-pixel tip."""
    shank = max(.75, length * .55)
    return [box(f'{name}_icicle', (x - .5, y - shank, z - .5), (x + .5, y, z + .5), 'ice'),
            box(f'{name}_tip', (x - .25, y - length, z - .25), (x + .25, y - shank, z + .25), 'ice', skip=('up',))]


def hang(pose, points):
    """Icicles under posed attachment points, given as (name, x, y, z, length) in the body frame."""
    e = []
    for name, x, y, z, length in points:
        wy, wz = apply_point(pose, y, z)
        e += icicle(name, x, wy, wz, length)
    return e


SLEEVE_ICICLES = [('sleeve', 3.25, 6.5, 6.5, 2), ('sleeve', 5.75, 6.5, 5.75, 1.25),
                  ('sleeve', 12.75, 6.5, 7, 1.5), ('sleeve', 10.25, 6.5, 6, 2.25)]
CHIN_ICICLES = [('chin', 8, 15.5, 8, 1.25)]
BRIM_ICICLES = [('brim', 4.5, 21.25, 7, 1.5), ('brim', 11.5, 21.25, 7, 1)]


# --- poses ----------------------------------------------------------------------------------

def upright_head(hooded, at, look, icicles):
    """A level head on its neck at `at` (y, z), turned toward `look`, with the icicles that hang from it."""
    skull = shift(at[0] - NECK[0], at[1] - NECK[1])
    return inside(turned(posed(head(hooded), skull) + hang(skull, icicles), look, 8, at[1]))


def inside(elements, lo=1, hi=15):
    """A turned head's corners swing out by a third of its width: bring it forward or back on the neck until it
    stays inside the cell (z lo..hi)."""
    pts = np.vstack([corners(el['lo'], el['hi'], el['rotation']) for el in elements])
    dz = max(0.0, lo - pts[:, 2].min()) - max(0.0, pts[:, 2].max() - hi)
    if abs(dz) < 1e-9:
        return elements
    out = copy.deepcopy(elements)
    for el in out:
        el['lo'][2] += dz
        el['hi'][2] += dz
        if el['rotation']:
            el['rotation']['origin'][2] += dz
    return out


def corners(lo, hi, rot):
    pts = np.array([[x, y, z] for x in (lo[0], hi[0]) for y in (lo[1], hi[1]) for z in (lo[2], hi[2])], dtype=float)
    if rot:
        o = np.array(rot['origin'], dtype=float)
        pts = (pts - o) @ np.array(coplanar._matrix(rot)).T + o
    return pts


def kneeling(look='forward'):
    e = seiza_base() + torso() + arms_clasped() + hang(IDENTITY, SLEEVE_ICICLES)
    return e + upright_head(True, NECK, look, BRIM_ICICLES + CHIN_ICICLES)


BOWED_TURN_DROP = 2.25     # the turned head sinks into the bent shoulders
BOWED_TURN_FORWARD = 1.5   # and hangs out ahead of the chest


def bowed(look='forward'):
    body = then(turn(-22.5, HIP), shift(0, BOWED_SETBACK))
    e = seiza_base() + posed(torso() + arms_clasped(), body) + hang(body, SLEEVE_ICICLES)
    if look == 'forward':
        skull = then(turn(-22.5, NECK), body)
        return e + posed(head(hooded=False), skull) + hang(skull, CHIN_ICICLES)
    # The bowed head's -45 tilt cannot share an element with a y turn: the turned head comes level on the bent neck
    # and drops into the shoulders, lifted off the chest to look aside.
    ny, nz = apply_point(body, *NECK)
    return e + upright_head(False, (ny - BOWED_TURN_DROP, nz - BOWED_TURN_FORWARD), look, CHIN_ICICLES)


def reaching(look='forward'):
    raised = turn(135, SHOULDER)
    arms = both(upper_arm() + forearm(), raised)
    e = seiza_base() + torso() + arms
    e += hang(raised, [('cuff', 3.25, 4.75, 9.25, 1.5), ('cuff', 12.75, 4.75, 12.25, 2)])
    if look == 'forward':
        skull = then(turn(22.5, NECK), shift(-.5, -1.25))   # tipped back, the head settles forward over the neck
        return e + posed(head(hooded=False), skull) + hang(skull, CHIN_ICICLES)
    # Tipped back is an x turn too: the turned head comes level between the raised arms, looking aside under them.
    return e + upright_head(False, (NECK[0], NECK[1] - .75), look, CHIN_ICICLES)


# The Procession: standing, the trunk and head of the kneeling figure carried on legs mid-step.
STRIDE_LIFT = 7           # the hips rise from the heels to a standing height
STRIDE_SETBACK = -2.75    # and the trunk moves forward, so the trailing leg's hem stays inside the cell
STRIDE_HIP = (HIP[0] + STRIDE_LIFT + 1, HIP[1] + STRIDE_SETBACK)
STRIDE_SWING = 22.5       # each leg's swing, forward and back
STRIDE_KNEE_Y = 11        # the robe swings from the upper thigh, low enough that both hems stay inside the cell
STRIDE_HEAD_FORWARD = .75  # the head carried a little ahead of the shoulders, as walking
STRIDE_NECK = (NECK[0] + STRIDE_LIFT, NECK[1] + STRIDE_SETBACK - STRIDE_HEAD_FORWARD)


def arms_tucked():
    """Upper arms at the sides, the forearms folded across the belly with the hands hidden in the joined sleeves."""
    e = []
    e += pair(box('upper_arm', (2.25, 9, 9), (4.5, 14.5, 12.5), 'robe'))
    e += pair(box('arm_crust', (2, 13, 8.75), (4.75, 15, 12.75), 'rime'))
    e += pair(box('sleeve', (2.5, 8, 6), (8, 11, 10), 'robe', skip=('east',)))
    e += pair(box('sleeve_drape', (2.75, 6, 6.25), (7.75, 8, 9.5), 'robe', skip=('up',)))
    e += pair(box('cuff_crust', (2.75, 5.5, 6.25), (7.75, 6, 9.5), 'rime', skip=('up',)))
    e += pair(frost_line('sleeve_fold', (4.5, 6.25, 6.15), (4.75, 10.5, 6.25)))
    e.append(box('sleeve_mouth', (7.5, 8.25, 5.9), (8.5, 10.75, 6), 'shadow', skip=('south',)))
    return e


def stride_leg(lo_x, hi_x, theta):
    """One leg under the robe: a skirt panel swung from the hip, crusted at its hem."""
    y0 = 0.75
    hy, hz = STRIDE_HIP
    mid = (lo_x + hi_x) / 2
    leg = [box('skirt', (lo_x, y0, hz - 2), (hi_x, hy - 1, hz + 2), 'robe'),
           box('skirt_hem', (lo_x - .25, y0 - .25, hz - 2.25), (hi_x + .25, y0 + 1.25, hz + 2.25), 'rime'),
           frost_line('skirt_fold', (mid - .125, y0 + 2, hz - 2.35), (mid + .125, hy - 3, hz - 2.25))]
    return posed(leg, turn(theta, (STRIDE_KNEE_Y, hz)))


def striding_base():
    """Legs mid-stride under the robe: the left leg swung forward with its foot flat ahead of the hem, the right swung
    back on its toes with the heel lifted; the robe between them hangs in a drape."""
    hy, hz = STRIDE_HIP
    e = stride_leg(3.75, 8, STRIDE_SWING) + stride_leg(8, 12.25, -STRIDE_SWING)
    e.append(box('hips', (4.25, hy - 4, hz - 2.5), (11.75, hy + 1, hz + 2.5), 'robe'))
    e.append(box('drape', (5, 2, hz - 2.5), (11, hy - 3.5, hz + 2.5), 'robe', skip=('up',)))
    e.append(box('drape_crust', (5.25, 1.5, hz - 2.25), (10.75, 2, hz + 2.25), 'rime', skip=('up',)))
    # Front foot: flat on the floor, ahead of the swung hem.
    e.append(box('foot', (4.75, 0, 1.5), (7.25, 1.75, 5), 'skin', up='knuckles', skip=('down',)))
    e.append(box('foot_drift', (4.25, 0, 4.5), (8, .75, 6.5), 'rime', skip=('down',)))
    # Back foot: on its toes behind, the heel lifted, the sole turned to the back.
    heel = turn(-45, (.65, 12))
    e += posed([box('foot', (8.75, 0, 11.75), (11.25, 1.75, 15), 'skin', south='sole', skip=('down',))], heel)
    e.append(box('toe_drift', (8.5, 0, 10.75), (11.5, .5, 12.75), 'rime', skip=('down',)))
    return e


def striding(look='forward'):
    trunk = shift(STRIDE_LIFT, STRIDE_SETBACK)
    e = striding_base() + posed(torso() + arms_tucked(), trunk)
    e += hang(trunk, [('sleeve', 3.5, 5.5, 7, 1.5), ('sleeve', 6.75, 5.5, 6.75, 2), ('sleeve', 12.25, 5.5, 7.25, 1.25)])
    return e + upright_head(True, STRIDE_NECK, look, BRIM_ICICLES + CHIN_ICICLES)


def kowtow_base():
    """Shins flat on the floor, the robe heaped over the raised haunches."""
    e = [box('shins', (3.5, 0, 8.5), (12.5, 3, 14), 'robe', skip=('down',)),
         box('haunch', (3.5, 2.5, 9.5), (12.5, 8.5, 13.75), 'robe'),
         box('haunch_crust', (4, 8.5, 10), (12, 9, 13.5), 'rime')]
    e += pair(box('knee', (3.75, 0, 7.5), (7.5, 2.75, 8.5), 'robe', skip=('down', 'south')))
    e += pair(box('sole', (4.5, .25, 14), (7, 2.25, 14.75), 'skin', south='sole', skip=('down', 'north')))
    e += hem_drifts(3.5, 12.5, 8.25, 14.25)
    e += pair(frost_line('haunch_fold', (3.4, 3, 11), (3.5, 7.5, 11.25)))
    return e


PROSTRATE_TOP = 9         # a shorter trunk: the hunched back has to fit one cell with the head on the floor
PROSTRATE_HIP = (10, 13.25)
PROSTRATE_ARM_SINK = 2.5  # the upper arms reach down into the floor-laid forearms
PROSTRATE_ELBOW_Y = 2
PROSTRATE_HANDS_Z = 1.25  # the fingertips reach the front of the cell, ahead of the head
PROSTRATE_TURN_NECK = 22.5   # the turned head lies level: -112.5 + 22.5 = -90, a whole quarter turn
PROSTRATE_TURN_BACK = 2.5      # dz: draws the turned head back toward the shoulders
PROSTRATE_CHEEK = .25        # the turned head's lowest face rests this far above the floor


def prostrate(look='forward'):
    top = PROSTRATE_TOP
    drop = TORSO_TOP - top
    neck = (NECK[0] - drop, NECK[1])
    hy, hz = PROSTRATE_HIP
    body = then(turn(-112.5, HIP), shift(hy - HIP[0], hz - HIP[1]))
    # Forward, the forehead rests on the floor (-135 in all: a baked half turn and +45). Turned, the head lies level
    # face down (-90, all baked) so it can roll about z: the cheek turns toward the floor.
    skull = then(turn(-22.5 if look == 'forward' else PROSTRATE_TURN_NECK, neck), body)
    # Arms: upper arms leaning forward from the shoulders, forearms laid flat ahead along the floor.
    sy, sz = apply_point(body, SHOULDER[0] - drop, SHOULDER[1])
    upper = then(turn(22.5, SHOULDER), shift(sy - SHOULDER[0] - PROSTRATE_ARM_SINK, sz - SHOULDER[1]))
    reach = ELBOW[0] - 2.25   # elbow to fingertips
    lower = then(turn(90, ELBOW), shift(PROSTRATE_ELBOW_Y - ELBOW[0], PROSTRATE_HANDS_Z + reach - ELBOW[1]))
    skull_els = posed(head(hooded=True, drop=drop), skull)
    if look != 'forward':
        cy, _ = apply_point(skull, 18.5 - drop, 10.75)
        skull_els = rolled(skull_els, look, 8, cy)
        floor = min(el['lo'][1] for el in skull_els)
        for el in skull_els:
            el['lo'][1] += PROSTRATE_CHEEK - floor
            el['hi'][1] += PROSTRATE_CHEEK - floor
            el['lo'][2] += PROSTRATE_TURN_BACK
            el['hi'][2] += PROSTRATE_TURN_BACK
    e = kowtow_base() + posed(torso(top=top), body) + skull_els
    e += both(upper_arm(), upper) + both(forearm(), lower)
    e += hang(body, [('back', 8, 6, 13.5, 1.25)])
    return e


BUILDERS = dict(kneeling=kneeling, bowed=bowed, reaching=reaching, prostrate=prostrate, striding=striding)


# --- special faces --------------------------------------------------------------------------

def robe_paint(face, lx, ly, w, h, key, heavy=False):
    """Undyed grey-blue cloth hanging in soft folds; rime settles on the upper edges, ice in the deep folds."""
    n = noise('robe', key, face, lx, ly)
    if face in ('up', 'down'):
        if face == 'up' and noise('rime', key, face, lx, ly) < (.75 if heavy else .55):
            return clamp(RIME, int(n * 10) - 14) + (255,)
        return clamp(CLOTH, int(n * 6) - 3) + (255,)
    fold = (lx + int(noise('fold', key, face) * 4)) % 4
    rgb = clamp(CLOTH, (-9, -2, 7, 2)[fold] + int(n * 4) - 2 - (8 if ly == h - 1 and h > 2 else 0))
    settled = (2 if heavy else 1) if h > 2 else 0
    if ly < settled and noise('lip', key, face, lx) < .7:
        return clamp(RIME, int(n * 10) - 16) + (255,)
    if fold == 0 and noise('ice', key, face, lx, ly // 2) < .12:
        return clamp(ICE, -26 + int(n * 8)) + (255,)
    if noise('fleck', key, face, lx, ly) < (.08 if heavy else .035):
        return clamp(RIME, -30) + (255,)
    return rgb + (255,)


def rime(face, lx, ly, w, h, key):
    n = noise('rime', key, face, lx, ly)
    if n > .95:
        return (238, 244, 250, 255)
    patch = -12 if noise('patch', key, face, lx // 2, ly // 2) < .3 else 0
    shade = -14 if face not in ('up', 'down') and ly == h - 1 and h > 1 else 0
    return clamp(RIME, int(n * 8) - 4 + patch + shade) + (255,)


def ice(face, lx, ly, w, h, key):
    n = noise('ice', key, face, lx, ly)
    if ly == 0 or (lx == 0 and face != 'down'):
        return (212, 232, 248, 255)
    if n < .15:
        return (112, 154, 204, 255)
    return clamp(ICE, int(n * 12) - 6) + (255,)


def frost(face, lx, ly, w, h, key):
    """The full-bright frost in the folds: pale ice blue, brighter where it beads."""
    n = noise('glow', key, face, lx, ly)
    return ((196, 226, 246) if n > .6 else (146, 194, 232)) + (255,)


def skin(face, lx, ly, w, h, key):
    n = noise('skin', key, face, lx, ly)
    if noise('bite', key, face, lx // 2, ly // 2) < .15:
        return clamp(FROSTBITE, int(n * 8) - 4) + (255,)
    if n > .95:
        return (226, 234, 244, 255)
    return clamp(SKIN, int(n * 10) - 5) + (255,)


def sole(face, lx, ly, w, h, key):
    n = noise('sole', key, lx, ly)
    return clamp((154, 154, 178) if ly < h - 1 else (126, 128, 160), int(n * 8) - 4) + (255,)


def hands(face, lx, ly, w, h, key):
    """Two palms pressed together, fingers up: a dark seam between them, frost-blue fingertips."""
    if lx == w // 2 and ly > 0:
        return (132, 140, 160, 255)
    if ly == 0:
        return FROSTBITE + (255,)
    if ly == h - 1:
        return clamp(SKIN, -22) + (255,)
    return skin(face, lx, ly, w, h, key)


def palm(face, lx, ly, w, h, key):
    """An open hand turned outward: fingers parted by dark lines, frost-blue tips."""
    if ly >= h - 1:
        return FROSTBITE + (255,)
    if ly >= h - 2 and lx % 2 == 1:
        return (132, 140, 160, 255)
    return skin(face, lx, ly, w, h, key)


def knuckles(face, lx, ly, w, h, key):
    return ((150, 160, 184) if (lx + ly) % 2 else FROSTBITE) + (255,)


FACE = ['ssssss',
        'swsswW',
        'sEssEs',
        'ssssss',
        'ssmmss',
        'ssssss']
FACE_COLOURS = {'w': (226, 236, 246), 'W': (214, 228, 242), 'E': (64, 70, 88), 'n': (170, 176, 190),
                'm': (138, 148, 172), 'h': HAIR}


def face_paint(face, lx, ly, w, h, key, bare):
    """Eyes shut, rime on the brows and the chin, lips gone blue; under a hood the brow is in shadow."""
    row = FACE[min(ly, len(FACE) - 1)]
    ch = row[min(lx, len(row) - 1)]
    if bare and ly == 0:
        ch = 'h'
    if ch in FACE_COLOURS:
        return FACE_COLOURS[ch] + (255,)
    rgb = skin(face, lx, ly, w, h, key)
    if not bare and ly == 0:
        return clamp(rgb[:3], -46) + (255,)
    return rgb


def hair_side(face, lx, ly, w, h, key):
    """The side of a bare head: hair above and behind, cheek and ear below at the front. East faces run back to
    front, west faces front to back."""
    front = lx >= w // 2 if face == 'east' else lx < w - w // 2
    if front and ly >= 2:
        ear = ly in (2, 3) and (lx == w // 2 if face == 'east' else lx == w - w // 2 - 1)
        return (clamp(SKIN, -18) + (255,)) if ear else skin(face, lx, ly, w, h, key)
    return hair(face, lx, ly, w, h, key)


def hair(face, lx, ly, w, h, key):
    """Frost-whitened hair: grey strands under the white, a rime glitter on top."""
    n = noise('hair', key, face, lx, ly)
    if n > .9:
        return (244, 248, 252, 255)
    if (lx + int(noise('strand', key, face) * 3)) % 3 == 0:
        return clamp((150, 158, 174), int(n * 10) - 5) + (255,)
    return clamp(HAIR, int(n * 12) - 8) + (255,)


def cord(face, lx, ly, w, h, key):
    """A frozen rope belt, its twist picked out in rime."""
    if (lx + ly) % 3 == 0:
        return (176, 196, 220, 255)
    return clamp((84, 84, 94), int(noise('cord', key, face, lx, ly) * 10) - 5 - (14 if (lx + ly) % 3 == 1 else 0)) \
        + (255,)


def shadow(face, lx, ly, w, h, key):
    """The dark mouth of the joined sleeves, where the hands are hidden."""
    return clamp((30, 32, 44), int(noise('shadow', key, face, lx, ly) * 6) - 3) + (255,)


def glint(face, lx, ly, w, h, key):
    """A witness's opened eye: a pale blue-white point, full-bright."""
    return (226, 242, 255, 255)


SPECIAL = dict(robe=robe_paint, hood=lambda *a: robe_paint(*a, heavy=True), rime=rime, ice=ice, frost=frost,
               skin=skin, sole=sole, hands=hands, palm=palm, knuckles=knuckles, hair=hair, cord=cord,
               face_hooded=lambda *a: face_paint(*a, bare=False), face_bare=lambda *a: face_paint(*a, bare=True),
               hair_side=hair_side, shadow=shadow, glint=glint)


# --- the witness ----------------------------------------------------------------------------

WITNESS = 'witness_devotee'
TURNS = ('left', 'right')
# The opened eyes outshine the frost: the glints are full-bright, the block itself gives light 7 while lit.
GLINT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}


def eye_glints(look):
    """Two opened eyes on the striding devotee's hooded face (FACE's 'E' texels, rows 2 and columns 1 and 4), turned
    with its head."""
    skull = shift(STRIDE_NECK[0] - NECK[0], STRIDE_NECK[1] - NECK[1])
    eyes = [box('eye_glint', (x, 18.5, 7.5), (x + 1, 19.5, 7.75), 'glint', skip=('south',), extra=GLINT)
            for x in (6, 9)]
    return turned(posed(eyes, skull), look, 8, STRIDE_NECK[1])


def witness_lit(turn_to):
    return striding(turn_to) + eye_glints(turn_to)


# --- checks, blockstate, previews -----------------------------------------------------------

def world_corners(el):
    return corners(el['from'], el['to'], el.get('rotation'))


def bounds(elements, dy=0):
    pts = np.vstack([world_corners(el) for el in elements]) if elements else np.zeros((1, 3))
    pts[:, 1] += dy
    return pts.min(axis=0), pts.max(axis=0)


def storeys(elements):
    """Separate, cut and file one variant's elements into its lower and upper cells."""
    moved = separate(elements)
    lower, upper = split_storeys(split_yawed(elements))
    return moved, lower, upper


def signature(elements):
    """A variant's geometry, for sharing one model between looks that leave a cell unchanged."""
    return json.dumps([[el['name'], el['lo'], el['hi'], el['mat'], el['faces'], sorted(el['skip']), el['rotation'],
                        el['extra']] for el in elements], sort_keys=True)


def model_name(pose, half, look):
    return f'frozen_devotee_{pose}_{half}' + ('' if look == 'forward' else f'_{look}')


def blockstate(names):
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for half in ('lower', 'upper'):
            for look in LOOKS:
                for pose, name in enumerate(POSES):
                    variant = {'model': f'hemomancy:block/{names[name, half, look]}'}
                    if y:
                        variant['y'] = y
                    variants[f'facing={facing},half={half},head={look},pose={pose}'] = variant
    return {'variants': variants}


def witness_blockstate(names):
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for half in ('lower', 'upper'):
            for lit in ('false', 'true'):
                for turn_to in TURNS:
                    if half == 'lower':
                        model = names['striding', 'lower', 'forward']
                    elif lit == 'false':
                        model = names['striding', 'upper', 'forward']
                    else:
                        model = f'{WITNESS}_upper_lit_{turn_to}'
                    variant = {'model': f'hemomancy:block/{model}'}
                    if y:
                        variant['y'] = y
                    variants[f'facing={facing},half={half},lit={lit},turn={turn_to}'] = variant
    return {'variants': variants}


def sheet(rows, title, out_name, tile=300, scale=8.5, lift=8, night=False):
    """A grid of renders: `rows` is a list of rows of (label, elements, yaw, pitch). `night` dims every texel but the
    full-bright glints', as a dark hall at low light would."""
    texture = np.array(Image.open(ASSETS / f'textures/block/{ATLAS}.png').convert('RGBA'))
    if night:
        lit = texture.copy()
        texture[..., :3] = (texture[..., :3] * .35).astype(texture.dtype)
        h, w = texture.shape[:2]
        for row in rows:
            for _, elements, _, _ in row:
                for el in elements:
                    if el.get('neoforge_data', {}).get('block_light') == 15:
                        for face in el['faces'].values():
                            u0, v0, u1, v1 = face['uv']
                            ys, xs = slice(round(v0 * h / 16), round(v1 * h / 16)), slice(round(u0 * w / 16), round(u1 * w / 16))
                            texture[ys, xs] = lit[ys, xs]
    cols = max(len(r) for r in rows)
    out = Image.new('RGBA', (tile * cols, tile * len(rows) + 24), (30, 24, 28, 255))
    draw = ImageDraw.Draw(out)
    for r, row in enumerate(rows):
        for c, (label, elements, yaw, pitch) in enumerate(row):
            model = copy.deepcopy(elements)
            for el in model:
                el['from'][1] -= lift
                el['to'][1] -= lift
                if 'rotation' in el:
                    el['rotation']['origin'][1] -= lift
            out.paste(render(model, {'atlas': texture}, yaw, pitch, tile, scale), (c * tile, 24 + r * tile))
            draw.text((c * tile + 6, 28 + r * tile), label, fill=(200, 200, 216))
    draw.text((10, 6), title, fill=(220, 220, 236))
    path = ROOT / f'build/model-previews/{out_name}.png'
    path.parent.mkdir(parents=True, exist_ok=True)
    out.convert('RGB').save(path)
    print(path)


def previews(joined, witness):
    for name in POSES:
        preview(joined[name, 'forward'], ATLAS, f'Frozen Devotee - {name} (pose {POSES.index(name)}), lower + upper',
                f'frozen_devotee_{name}', lift=8, size=400, scale=12)
    sheet([[(name, joined[name, 'forward'], 20, 18) for name in POSES],
           [(name + ' profile', joined[name, 'forward'], 90, 8) for name in POSES]],
          'Frozen Devotee - kneeling, bowed, reaching, prostrate, striding (16 px/block)', 'frozen_devotee_lineup')
    # Turned heads: each pose looking to its own left, ahead and to its own right, seen from the front and from
    # above-front (the read a player gets walking a procession's flank).
    rows = []
    for name in POSES:
        rows.append([(f'{name} {look}', joined[name, look], 0, 12) for look in LOOKS]
                    + [(f'{name} {look} above', joined[name, look], 25, 50) for look in LOOKS])
    sheet(rows, 'Frozen Devotee - head left / forward / right (front, then above)', 'frozen_devotee_heads',
          tile=220, scale=6)
    procession = []
    for i, look in enumerate(('forward', 'left', 'forward', 'right', 'forward')):
        procession.append((look, joined['striding', look], -35, 14))
    sheet([procession], 'Frozen Devotee - the Procession (striding, mixed heads)', 'frozen_devotee_procession',
          tile=240, scale=7)
    sheet([[('unlit', joined['striding', 'forward'], 0, 10)]
           + [(f'lit {t}', witness[t], 0, 10) for t in TURNS]
           + [(f'lit {t} 3/4', witness[t], -30 if t == 'left' else 30, 20) for t in TURNS]],
          'Witness Devotee - unlit, lit turn left / right', 'witness_devotee', tile=300, scale=8.5)
    sheet([[('unlit', joined['striding', 'forward'], 0, 10)]
           + [(f'lit {t}', witness[t], 0, 10) for t in TURNS]
           + [(f'lit {t} 3/4', witness[t], -30 if t == 'left' else 30, 15) for t in TURNS]],
          'Witness Devotee at low light: the opened eyes', 'witness_devotee_night', tile=300, scale=8.5, night=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    groups, names, signatures, moved, cubes = {}, {}, {}, 0, 0
    for name in POSES:
        for look in LOOKS:
            m, lower, upper = storeys(BUILDERS[name](look))
            moved += m
            for half, els in (('lower', lower), ('upper', upper)):
                sig = signature(els)
                shared = signatures.get((name, half, 'forward'))
                if look != 'forward' and sig == shared:
                    names[name, half, look] = names[name, half, 'forward']
                    continue
                signatures[name, half, look] = sig
                names[name, half, look] = model_name(name, half, look)
                groups[names[name, half, look]] = els
                cubes += len(els)
        turned_halves = [names[name, h, look] != names[name, h, 'forward'] for h in ('lower', 'upper')
                         for look in ('left', 'right')]
        assert any(turned_halves), f'{name}: a turned head changed no model'
    for turn_to in TURNS:
        m, lower, upper = storeys(witness_lit(turn_to))
        moved += m
        assert signature(lower) == signatures['striding', 'lower', 'forward'], 'the witness glints reach the lower cell'
        groups[f'{WITNESS}_upper_lit_{turn_to}'] = upper
        cubes += len(upper)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    outputs = {}
    for model, els in models.items():
        for el in els:
            assert all(-16 <= v <= 32 for v in el['from'] + el['to']), (model, el['name'])
        outputs[f'block/{model}'] = model_json(els, ATLAS, atlas.height, PARTICLE)
    joined, witness = {}, {}
    for name in POSES:
        print(f'  {name}:')
        for look in LOOKS:
            lower, upper = models[names[name, 'lower', look]], models[names[name, 'upper', look]]
            joined[name, look] = item_elements((lower, 0), (upper, 16))
            lo, hi = bounds(joined[name, look])
            assert lo[0] >= 1 - 1e-6 and hi[0] <= 15 + 1e-6 and lo[2] >= 1 - 1e-6 and hi[2] <= 15 + 1e-6, \
                (name, look, lo, hi)
            assert lo[1] >= -1e-6, (name, look, lo)
            print(f'    {look:7s} {names[name, "lower", look]} + {names[name, "upper", look]}; '
                  + outline_report(lower, upper))
    for turn_to in TURNS:
        upper = models[f'{WITNESS}_upper_lit_{turn_to}']
        witness[turn_to] = item_elements((models[names['striding', 'lower', 'forward']], 0), (upper, 16))
        print(f'  witness lit {turn_to}: {len(upper)} upper cubes; upper box {fmt(bounds(upper)[0])}..'
              f'{fmt(bounds(upper)[1])}')
    write(outputs, 'frozen_devotee', model_json(joined['kneeling', 'forward'], ATLAS, atlas.height, PARTICLE))
    write({}, WITNESS, model_json(witness['left'], ATLAS, atlas.height, PARTICLE))
    (ASSETS / 'blockstates/frozen_devotee.json').write_text(json.dumps(blockstate(names), indent=2) + '\n')
    (ASSETS / f'blockstates/{WITNESS}.json').write_text(json.dumps(witness_blockstate(names), indent=2) + '\n')
    print(f'Exported Frozen Devotee + Witness Devotee: {cubes} cubes over {len(groups)} models, '
          f'{moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        previews(joined, witness)


def outline_report(lower, upper):
    """Boxes to trace the Java outlines from: the lower cell in two bands (feet/robe, body), the upper cell whole."""
    parts = []
    for label, els, band in (('lower 0-8', lower, (0, 8)), ('lower 8-16', lower, (8, 16)), ('upper', upper, (0, 16))):
        pts = np.vstack([world_corners(el) for el in els]) if els else np.zeros((0, 3))
        pts = pts[(pts[:, 1] >= band[0] - 1e-6) & (pts[:, 1] <= band[1] + 1e-6)] if len(pts) else pts
        parts.append(f'{label} ' + (f'{fmt(pts.min(axis=0))}..{fmt(pts.max(axis=0))}' if len(pts) else 'empty'))
    return '; '.join(parts)


def fmt(v):
    return '(' + ', '.join(f'{c:g}' for c in np.round(v, 2)) + ')'


if __name__ == '__main__':
    main()

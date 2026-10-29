"""Author the ring door: the set-piece door before a Saint's chamber.

A venous-stone slab five blocks wide and five tall stands in a hematic-iron frame. Cut into its face is a round well
holding three concentric rings, outer, middle and inner, each carrying a bone-rimmed lacquer node at its top and a
notch at its bottom, and each grooved with a channel for blood. Iron talons reach in from the slab's corners to grip
the well's bone moulding, a witnessing-eye keystone hangs from the lintel, and a vertical seam runs the slab's height
through the well. As the Chapels are completed their nodes light and the rings turn until every notch lies on the seam.
A Silentium's blood fills the channels one ring at a time and the slab sinks through the threshold's slot; an
Apotheos cracks it with hyphae and it bursts.

Emits the static frame (baked: `ring_door` plus `ring_door_frame_<n>` sections that RingDoorBakedModel adds), the
slab (`ring_door_slab_<n>`), each ring and its lit node (`ring_door_<ring>_<n>`, `ring_door_<ring>_lit_<n>`), the
blockstate, the painted atlas, an editable Blockbench project and RingDoorGeometry.java (fillers, open-door shapes,
render anchors and every section's pixel shift). Python 3 + Pillow + NumPy. `--preview` writes turnarounds to
build/model-previews/.

Coordinates are block pixels for the north-facing state, from the controller's cell corner: the face is x -32..48,
y 0..80, one cell deep (z 0..16), its front toward -z (north), toward the nave. Rings are authored about their own
centre at (8, 8) and drawn by the renderer at (RING_X, RING_Y), turned about z. Block models stop at -16..32, so every
part is exported in sections, each moved by a whole-block shift that the renderer (or the baked model) moves back.
"""
import argparse
import copy
import json
import math

from clairaudiograph import clamp, noise
from export_scriptorium_upgrades import ROOT
from fixture_atlas import ASSETS, PALETTE, box, build, model_json, separate, write
from saint_sarcophagus import BONE_PALE, TOMB, TOMB_LIGHT, sigil, tomb

ATLAS = 'ring_door'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}
GEOMETRY = ROOT / 'src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/puzzle/RingDoorGeometry.java'
PARTICLE = 'hemomancy:block/polished_venous_stone'

# The face: five cells wide and five tall, the controller at the bottom centre.
CELLS = [(x, y, 0) for x in range(-2, 3) for y in range(5)]
FACE = (-32, 0, 48, 80)
JAMB = 6                    # each jamb's width
LINTEL = 74                 # the lintel's underside, the slab's top
SLAB = (-26, 1, 4, 42, LINTEL, 12)
CENTRE = (8.0, 38.0)        # the rings' common centre on the slab
RING_C = 8.0                # each ring is authored about (RING_C, RING_C)
FRONT, CHANNEL_FRONT, BACK = 1.5, 2.6, 4.0
STEP = .06                  # odd segments sit this far back so overlapping neighbours never fight
# Each ring as (name, bands); a band is (r0, r1, material). The channel's centre line carries the node.
RINGS = [
    ('outer', [(23, 26, 'ringstone'), (26, 28, 'channel'), (28, 31, 'ringstone')]),
    ('middle', [(13.5, 16.5, 'ringstone'), (16.5, 18.5, 'channel'), (18.5, 21.5, 'ringstone')]),
    ('inner', [(0, 7, 'ringstone'), (7, 9, 'channel'), (9, 12, 'ringstone')]),
]
WELL = 31.6                 # the round well the rings turn in; its bone moulding lies just outside
SEGMENTS = 16
NODE_RIM, NODE_CORE = 3.0, 2.0

STONE = (70, 12, 20)
RING_STONE = (104, 24, 32)
WELL_DARK = (12, 2, 5)
CHANNEL = (44, 6, 12)
GLOW_HOT = (255, 120, 112)
GLOW = (236, 40, 52)

GROUPS = {}


# --- shapes ---------------------------------------------------------------------------------

def radial(name, centre, degrees, r0, r1, t0, t1, z0, z1, mat, skip=(), extra=None, **faces):
    """A box from radius r0 to r1 and tangent t0..t1 about centre (x, y), aimed `degrees` from +x toward +y.

    Quarter turns are exact; the remainder (a multiple of 22.5 within +-45) is a model rotation about z through the
    centre. Faces are named before the turn: `skip` and `faces` may say 'outer', 'inner', 'side' or 'front'."""
    cx, cy = centre
    quarter = round(degrees / 90)
    rest = degrees - quarter * 90
    c, s = [(1, 0), (0, 1), (-1, 0), (0, -1)][quarter % 4]
    xs = [cx + c * r - s * t for r in (r0, r1) for t in (t0, t1)]
    ys = [cy + s * r + c * t for r in (r0, r1) for t in (t0, t1)]
    outer, inner = {0: ('east', 'west'), 1: ('up', 'down'), 2: ('west', 'east'), 3: ('down', 'up')}[quarter % 4]
    sides = [f for f in ('east', 'west', 'up', 'down') if f not in (outer, inner)]
    names = {'outer': [outer], 'inner': [inner], 'side': sides, 'front': ['north'], 'back': ['south']}
    skipped = set()
    for k in skip:
        skipped.update(names.get(k, [k]))
    named = {}
    for k, v in faces.items():
        for f in names.get(k, [k]):
            named[f] = v
    rot = {'angle': rest, 'axis': 'z', 'origin': [cx, cy, z0]} if rest else None
    return box(name, (min(xs), min(ys), z0), (max(xs), max(ys), z1), mat, skip=skipped, rotation=rot, extra=extra,
               **named)


def tiles(name, lo, hi, mat, skip=(), extra=None, **faces):
    """A box cut at every block line, so each piece lies in one cell; the cut faces are dropped."""
    cuts = []
    for axis in range(3):
        points = [lo[axis]] + [v for v in range(-64, 97, 16) if lo[axis] < v < hi[axis]] + [hi[axis]]
        cuts.append(list(zip(points, points[1:])))
    low = {0: 'west', 1: 'down', 2: 'north'}
    high = {0: 'east', 1: 'up', 2: 'south'}
    out = []
    for xa, xb in cuts[0]:
        for ya, yb in cuts[1]:
            for za, zb in cuts[2]:
                piece_skip = set(skip)
                for axis, (a, b) in enumerate(((xa, xb), (ya, yb), (za, zb))):
                    if a != lo[axis]:
                        piece_skip.add(low[axis])
                    if b != hi[axis]:
                        piece_skip.add(high[axis])
                out.append(box(name, (xa, ya, za), (xb, yb, zb), mat, skip=piece_skip, extra=extra, **faces))
    return out


def mirror(el):
    """The box reflected across the door's centre line x = 8."""
    out = copy.deepcopy(el)
    out['lo'][0], out['hi'][0] = 16 - el['hi'][0], 16 - el['lo'][0]
    swap = {'east': 'west', 'west': 'east'}
    out['skip'] = {swap.get(s, s) for s in el['skip']}
    out['faces'] = {swap.get(k, k): v for k, v in el['faces'].items()}
    if el['rotation']:
        out['rotation'] = dict(el['rotation'], origin=[16 - el['rotation']['origin'][0]] + el['rotation']['origin'][1:],
                               angle=-el['rotation']['angle'])
    return out


def pair(els):
    return els + [mirror(el) for el in els]


# --- the frame ------------------------------------------------------------------------------

def frame():
    x0, _, x1, top = FACE
    e = tiles('jamb', (x0, 0, 0), (x0 + JAMB, top, 16), 'jamb', skip=('west', 'up', 'down'), south='iron')
    e += tiles('jamb_panel', (x0 + 1.5, 6, -.5), (x0 + 4.5, 68, 0), 'panel', skip=('south',))
    e += tiles('jamb_base', (x0, 0, -1), (x0 + JAMB, 5, 0), 'ferric', skip=('south', 'west', 'down'))
    e += tiles('jamb_capital', (x0, 69, -1), (x0 + JAMB, LINTEL, 0), 'capital', skip=('south', 'west'))
    e = pair(e)
    e += tiles('lintel', (x0 + JAMB, LINTEL, 0), (x1 - JAMB, top, 16), 'lintel', skip=('up', 'east', 'west'),
               south='iron')
    e += tiles('keystone_surround', (1, 71, -1), (15, top, 0), 'bone', skip=('south', 'up'))
    e += tiles('keystone', (2, 72, -1.5), (14, top, -1), 'sigil', skip=('south', 'up'), east='ferric', west='ferric',
               down='ferric')
    e += tiles('threshold', (x0 + JAMB, 0, 0), (x1 - JAMB, 1, 16), 'threshold', skip=('down', 'east', 'west'))
    return e


# --- the slab -------------------------------------------------------------------------------

def talon(corner, degrees, curl):
    """An iron talon from a slab corner toward the well: two jointed segments and a bone claw, curling as it goes."""
    out, at, angle = [], corner, degrees
    for name, length, half, mat, z in (('talon', 7, 2, 'talon', 1.0), ('talon', 5, 1.5, 'talon', 1.3),
                                       ('claw', 5, 1, 'claw', 1.6)):
        out.append(radial(name, at, angle, 0, length, -half, half, z, BACK, mat, skip=('back',)))
        a = math.radians(angle)
        at = (at[0] + length * math.cos(a), at[1] + length * math.sin(a))
        angle += curl
    tip = math.hypot(at[0] - CENTRE[0], at[1] - CENTRE[1])
    assert tip >= WELL, ('a talon reaches into the well', tip)
    return out


def slab():
    x0, y0, z0, x1, y1, z1 = SLAB
    e = tiles('slab', (x0, y0, z0), (x1, y1, z1), 'slab', skip=('down', 'east', 'west'), up='stone')
    # The seam's iron strips above and below the well.
    e += tiles('seam_strip', (6.5, y0, 3), (9.5, CENTRE[1] - WELL - .5, z0), 'seam', skip=('south', 'down'))
    e += tiles('seam_strip', (6.5, CENTRE[1] + WELL + .5, 3), (9.5, y1, z0), 'seam', skip=('south', 'up'))
    # Straps across the back, seen from the chamber.
    for ya in (14, 57):
        e += tiles('strap', (x0, ya, z1), (x1, ya + 4, z1 + 1), 'strap', skip=('north', 'east', 'west'))
    for corner, degrees, curl in (((x0 + 2, y1 - 2), -45, -22.5), ((x1 - 2, y1 - 2), -135, 22.5),
                                  ((x0 + 2, y0 + 2), 45, 22.5), ((x1 - 2, y0 + 2), 135, -22.5)):
        e += talon(corner, degrees, curl)
    return e


# --- the rings ------------------------------------------------------------------------------

def ring(name, bands):
    """One ring about (RING_C, RING_C): sixteen segments per band, the node and notch on the channel."""
    centre = (RING_C, RING_C)
    e = []
    channel = next((r0 + r1) / 2 for r0, r1, mat in bands if mat == 'channel')
    for k in range(SEGMENTS):
        degrees = k * 360 / SEGMENTS
        for r0, r1, mat in bands:
            t = r1 * math.tan(math.pi / SEGMENTS)
            front = (CHANNEL_FRONT if mat == 'channel' else FRONT) + (STEP if k % 2 else 0)
            skip = ['side', 'back']
            if mat == 'channel' or r0 == 0:
                skip += ['inner', 'outer'] if mat == 'channel' else ['inner']
            seg = radial(name + '_' + mat, centre, degrees, r0, r1, -t, t, front, BACK, mat, skip=skip,
                         outer='ringrim', inner='ringrim')
            seg['band'], seg['rim'] = (r0, r1), r1 == bands[-1][1] and name != 'outer'
            e.append(seg)
        if name != 'inner' and k % 2 == 0 and degrees not in (90, 270):
            r1 = bands[-1][1]
            e.append(radial(name + '_stud', centre, degrees, r1 - 2.6, r1 - .6, -1, 1, FRONT - .5, FRONT, 'bone',
                            skip=('back',)))
    # The node: a lacquer boss in a bone rim, standing in the channel at the top.
    ny = RING_C + channel
    e.append(box(name + '_node_rim', (RING_C - NODE_RIM, ny - NODE_RIM, .9), (RING_C + NODE_RIM, ny + NODE_RIM,
                                                                            CHANNEL_FRONT), 'bone', skip=('south',)))
    e.append(box(name + '_node', (RING_C - NODE_CORE, ny - NODE_CORE, .45), (RING_C + NODE_CORE, ny + NODE_CORE, .9),
                 'node', skip=('south',)))
    if name == 'inner':
        # The hub: a witnessing eye on a bone star, turning with the inner ring.
        e.append(box('hub_eye', (RING_C - 4, RING_C - 4, .5), (RING_C + 4, RING_C + 4, FRONT), 'sigil',
                     skip=('south',), east='bone', west='bone', up='bone', down='bone'))
        e.append(box('hub_star', (RING_C - 4, RING_C - 4, .9), (RING_C + 4, RING_C + 4, FRONT), 'bone',
                     skip=('south',), rotation={'angle': 45, 'axis': 'z', 'origin': [RING_C, RING_C, .9]}))
    glow = [box(name + '_node_glow', (RING_C - NODE_CORE - .15, ny - NODE_CORE - .15, .3),
                (RING_C + NODE_CORE + .15, ny + NODE_CORE + .15, .45), 'glow', skip=('south',), extra=BRIGHT)]
    return e, glow, channel


# --- painters -------------------------------------------------------------------------------

def element(key):
    group, index = key
    return GROUPS[group][index]


def absolute(key, face, lx, ly):
    """The model point a texel of an unturned element's face paints, (x, y) for front and back faces."""
    el = element(key)
    lo, hi = el['lo'], el['hi']
    y = hi[1] - ly - .5
    if face == 'north':
        return hi[0] - lx - .5, y
    if face == 'south':
        return lo[0] + lx + .5, y
    return None


def slab_tex(face, lx, ly, w, h, key):
    """The slab: carved venous stone, a dark well for the rings ringed in bone, vein-trees run out to the corners in
    blood, and the seam down its middle. Its back is coursed masonry."""
    x, y = absolute(key, face, lx, ly)
    n = noise('slab', face, round(x * 2), round(y * 2))
    x0, y0, _, x1, y1, _ = SLAB
    if face == 'south':
        course = int(y - y0) // 4
        joint = int(y - y0) % 4 == 3 or int(x - x0 + (course % 2) * 4) % 9 == 8
        if joint:
            return clamp((34, 6, 10), int(n * 8)) + (255,)
        return clamp(STONE, int(noise('block', course, int(x - x0 + (course % 2) * 4) // 9) * 18) - 9
                     + int(n * 8) - 4) + (255,)
    dx, dy = x - CENTRE[0], y - CENTRE[1]
    r = math.hypot(dx, dy)
    if r < WELL:
        return clamp(WELL_DARK, int(n * 6)) + (255,)
    if r < WELL + 1.2:
        return clamp(BONE_PALE, int(n * 10) - 14 - (14 if dy < 0 else 0)) + (255,)
    if abs(dx) < 1:
        return (20, 4, 8, 255)
    edge = min(x - x0, x1 - x, y - y0, y1 - y)
    if edge < 1.5:
        return clamp(TOMB_LIGHT, int(n * 8) - (0 if y1 - y < 1.5 or x - x0 < 1.5 else 18)) + (255,)
    if edge < 3:
        return clamp(STONE, -18 + int(n * 6)) + (255,)
    # Vein-trees: a trunk out along each diagonal, twigs forking off it toward the edges.
    angle = math.degrees(math.atan2(dy, dx)) % 90
    trunk = abs(angle - 45) * r * math.pi / 180
    fork = ((r - WELL) % 7) < 1.2 and abs(angle - 45) * r * math.pi / 180 < 4.5 and r > WELL + 4
    twig = noise('twig', round(x), round(y)) < .025 and r > WELL + 3
    if (trunk < .8 and r > WELL + 2) or fork or twig:
        return clamp(PALETTE['blood'], 10 + int(n * 18)) + (255,)
    return clamp(STONE, int(n * 12) - 6 + (6 if (round(x) + round(y)) % 5 == 0 else 0)) + (255,)


def stone(face, lx, ly, w, h, key):
    return clamp(STONE, int(noise('stone', key, face, lx, ly) * 12) - 6 + (12 if ly == 0 else 0)) + (255,)


def ring_radius(key, face, lx, ly):
    """How far from its ring's centre a texel of a segment's front lies; turning about the centre keeps it."""
    x, y = absolute(key, face, lx, ly)
    return math.hypot(x - RING_C, y - RING_C), x, y


def ringstone(face, lx, ly, w, h, key):
    """Polished venous stone, bevelled: a bone fillet round the ring's outer edge, a shadowed inner edge, runes cut
    into every third segment of the broad bands, and the notch cut across the bottom."""
    el = element(key)
    n = noise('ring', key, face, lx, ly)
    if face != 'north':
        return clamp(TOMB, -10 + int(n * 10)) + (255,)
    r, x, y = ring_radius(key, face, lx, ly)
    r0, r1 = el['band']
    if not el['rotation'] and y < RING_C and abs(x - RING_C) < 1:
        return (14, 2, 6, 255)
    if not el['rotation'] and y < RING_C and abs(x - RING_C) < 2:
        return clamp(BONE_PALE, -30 + int(n * 8)) + (255,)
    if el['rim'] and r > r1 - 1:
        return clamp(BONE_PALE, -16 + int(n * 10)) + (255,)
    if r < r0 + 1 and r0 > 0:
        return clamp(RING_STONE, -30 + int(n * 6)) + (255,)
    if r1 - r0 >= 3 and key[1] % 3 == 0 and r0 + 1.2 < r < r1 - 1.2 and noise('rune', key, lx, ly) < .35:
        return clamp(PALETTE['blood'], -6 + int(n * 16)) + (255,)
    return clamp(RING_STONE, int(n * 12) - 6 + (8 if r > r1 - 2 else 0)) + (255,)


def ringrim(face, lx, ly, w, h, key):
    return clamp(RING_STONE, -34 + int(noise('rim', key, face, lx, ly) * 10)) + (255,)


def channel(face, lx, ly, w, h, key):
    """The blood channel: a dark groove, wet, its floor flecked with old offerings; the notch crosses it too."""
    el = element(key)
    n = noise('channel', key, face, lx, ly)
    if face == 'north' and not el['rotation']:
        x, y = absolute(key, face, lx, ly)
        if y < RING_C and abs(x - RING_C) < 1:
            return (10, 2, 4, 255)
    if n > .92:
        return (96, 14, 22, 255)
    return clamp(CHANNEL, int(n * 10) - 5) + (255,)


def node(face, lx, ly, w, h, key):
    """A dark lacquer boss: dull until its Chapel is seen through."""
    n = noise('node', key, face, lx, ly)
    if face == 'north' and lx == 1 and ly == 1:
        return clamp(PALETTE['lacquer'], 20) + (255,)
    return clamp(PALETTE['lacquer_deep'], -18 + int(n * 8)) + (255,)


def glow(face, lx, ly, w, h, key):
    """A lit node: blood-light from within, hot at its heart."""
    if face == 'north':
        d = max(abs(lx - (w - 1) / 2), abs(ly - (h - 1) / 2))
        return (GLOW_HOT if d < 1.2 else GLOW) + (255,)
    return GLOW + (255,)


def jamb(face, lx, ly, w, h, key):
    """Hematic iron jamb, bone rivets down both edges of its face."""
    n = noise('jamb', key, face, lx, ly)
    if face == 'north':
        x, y = absolute(key, face, lx, ly)
        if (lx in (0, w - 1)) and int(y) % 8 == 4:
            return clamp(PALETTE['bone'], int(n * 10) - 10) + (255,)
    if noise('vein', key, face, lx, ly // 2) < .04:
        return (98, 24, 34, 255)
    return clamp(PALETTE['iron'], int(n * 12) - 6) + (255,)


def panel(face, lx, ly, w, h, key):
    """A lacquered strip down the jamb's face, bone-edged."""
    n = noise('panel', key, face, lx, ly)
    if face == 'north' and lx not in (0, w - 1):
        return clamp(PALETTE['lacquer'], -10 + int(n * 10)) + (255,)
    return clamp(PALETTE['bone'], int(n * 10) - 20) + (255,)


def capital(face, lx, ly, w, h, key):
    """The jamb's capital: iron banded in bone."""
    if ly in (0, 2):
        return clamp(PALETTE['bone'], int(noise('cap', key, face, lx) * 10) - 14) + (255,)
    return clamp(PALETTE['ferric'], int(noise('cap', key, face, lx, ly) * 10) - 5) + (255,)


def lintel(face, lx, ly, w, h, key):
    """The lintel: iron, a row of bone dentils along its lower edge."""
    n = noise('lintel', key, face, lx, ly)
    if face == 'north' and ly >= h - 2 and lx % 2 == 0:
        return clamp(PALETTE['bone'], int(n * 10) - 12) + (255,)
    if face == 'down':
        return clamp(PALETTE['iron'], -14 + int(n * 8)) + (255,)
    return clamp(PALETTE['iron'], int(n * 12) - 6 + (10 if ly == 0 else 0)) + (255,)


def threshold(face, lx, ly, w, h, key):
    """The threshold plate, the slot the slab sinks through dark down its middle."""
    n = noise('sill', key, face, lx, ly)
    if face == 'up':
        el = element(key)
        z = el['lo'][2] + ly + .5
        if SLAB[2] - .5 < z < SLAB[5] + .5:
            return (12, 2, 4, 255) if SLAB[2] < z < SLAB[5] else clamp(PALETTE['ferric'], 10) + (255,)
    return clamp(PALETTE['iron'], int(n * 12) - 6) + (255,)


def seam(face, lx, ly, w, h, key):
    """The seam's iron strip, grooved down its middle."""
    n = noise('seam', key, face, lx, ly)
    if face == 'north' and lx == 1:
        return (20, 4, 8, 255)
    return clamp(PALETTE['ferric'], int(n * 10) - 5) + (255,)


def talon_tex(face, lx, ly, w, h, key):
    """Blackened hematic iron, a bone knuckle band at each joint and blood showing in the grain."""
    n = noise('talon', key, face, lx, ly)
    if face != 'north' and face != 'south' and lx in (0, w - 1):
        return clamp(PALETTE['bone'], -26 + int(n * 8)) + (255,)
    if noise('vein', key, face, lx, ly) < .07:
        return (118, 26, 36, 255)
    return clamp((48, 42, 52), int(n * 10) - 5 + (14 if ly == 0 else 0)) + (255,)


def claw(face, lx, ly, w, h, key):
    """A bone claw, yellowed, darkening to a stained point."""
    n = noise('claw', key, face, lx, ly)
    return clamp(BONE_PALE, int(n * 10) - 8 - lx * 6) + (255,)


def strap(face, lx, ly, w, h, key):
    n = noise('strap', key, face, lx, ly)
    if face == 'south' and ly in (1, 2) and lx % 6 == 2:
        return clamp(PALETTE['bone'], -16) + (255,)
    return clamp(PALETTE['iron'], int(n * 10) - 5) + (255,)


SPECIAL = dict(slab=slab_tex, stone=stone, ringstone=ringstone, ringrim=ringrim, channel=channel, node=node, glow=glow,
               jamb=jamb, panel=panel, capital=capital, lintel=lintel, threshold=threshold, seam=seam, strap=strap, talon=talon_tex, claw=claw,
               sigil=sigil, tomb=tomb)


# --- sections -------------------------------------------------------------------------------

def shifts_for(el):
    """Whole-block shifts that bring an element inside a block model's -16..32 reach, preferring three-block windows
    so neighbouring elements share a section."""
    lo, hi = el['from'], el['to']
    out = []
    for axis in range(3):
        fits = [s for s in range(-96, 97, 16) if lo[axis] - s >= -16 and hi[axis] - s <= 32]
        assert fits, (el['name'], lo, hi)
        aligned = [s for s in fits if s % 48 == 0]
        out.append(min(aligned or fits, key=abs))
    return tuple(out)


def sections(elements):
    """{shift: elements moved by -shift}, the shift (0, 0, 0) first when present."""
    out = {}
    for el in elements:
        s = shifts_for(el)
        moved = copy.deepcopy(el)
        for k in ('from', 'to'):
            moved[k] = [round(v - d, 4) for v, d in zip(moved[k], s)]
        if 'rotation' in moved:
            moved['rotation']['origin'] = [round(v - d, 4) for v, d in zip(moved['rotation']['origin'], s)]
        for f in moved['faces'].values():
            f.pop('cullface', None)
        out.setdefault(s, []).append(moved)
    return dict(sorted(out.items(), key=lambda item: (item[0] != (0, 0, 0), item[0])))


def moved(elements, dx, dy, dz=0, turn=0.0):
    """Elements moved by (dx, dy, dz); with `turn`, first turned about (RING_C, RING_C) by that many degrees."""
    out = []
    for el in copy.deepcopy(elements):
        if turn:
            rot = el.get('rotation')
            if rot:
                assert rot['axis'] == 'z', el['name']
                if rot['origin'][:2] != [RING_C, RING_C]:
                    # a part turned about its own point: only the hub star, which is centred on the ring anyway
                    rot['origin'][:2] = [RING_C, RING_C]
                rot['angle'] = rot['angle'] + turn
            else:
                el['rotation'] = {'angle': turn, 'axis': 'z', 'origin': [RING_C, RING_C, 0]}
        for k in ('from', 'to'):
            el[k] = [round(v + d, 4) for v, d in zip(el[k], (dx, dy, dz))]
        if 'rotation' in el:
            el['rotation']['origin'] = [v + d for v, d in zip(el['rotation']['origin'], (dx, dy, dz))]
        out.append(el)
    return out


# --- outputs --------------------------------------------------------------------------------

def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        variants[f'facing={facing}'] = dict({'model': 'hemomancy:block/ring_door'}, **({'y': y} if y else {}))
    return {'variants': variants}


def fmt(v):
    return f'{v:g}' if v != int(v) else f'{v:.1f}'


def java_sections(name, table):
    rows = ', '.join(f'new Section("{model}", {s[0]}, {s[1]}, {s[2]})' for model, s in table)
    return f'    public static final Section[] {name} = {{{rows}}};\n'


def geometry_java(tables, channels, frame_boxes, fragment_uv):
    boxes = ',\n'.join('        {' + ', '.join(fmt(v) for v in b) + '}' for b in frame_boxes)
    x0, y0, z0, x1, y1, z1 = SLAB
    frame_rows, slab_rows = java_sections('FRAME', tables.pop('frame')), java_sections('SLAB_SECTIONS', tables.pop('slab'))
    rings = ''.join(java_sections(key.upper(), table) for key, table in tables.items())
    u0, v0, u1, v1 = fragment_uv
    return f'''package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Generated by tools/model_export/ring_door.py; pixels of the north-facing door, whose face runs x -32..48 and
 * y 0..80 from the controller's cell corner, one cell deep, its front toward -z.
 */
public final class RingDoorGeometry {{
    /** One exported model of a part, authored this many pixels ({{@code x}}, {{@code y}}, {{@code z}}) back. */
    public record Section(String model, int x, int y, int z) {{}}

    /** The cells the fillers stand in, from the controller of a north-facing door (turn with rotateNorthOffsets). */
    public static final BlockPos[] FILLERS = fillers();
    /** The rings' common centre (x, y) on the slab and the plane their front faces stand in. */
    public static final float RING_X = {fmt(CENTRE[0])}f, RING_Y = {fmt(CENTRE[1])}f, RING_FRONT_Z = {fmt(FRONT)}f;
    /** Each ring's own centre in its models: the renderer turns them about it. */
    public static final float RING_LOCAL = {fmt(RING_C)}f;
    /** Radius of each ring's blood channel centre line, outer to inner; its node sits on it at the top. */
    public static final float[] CHANNEL_RADII = {{{', '.join(fmt(c) + 'f' for c in channels)}}};
    /** Half the width of each ring's channel, and the plane its floor stands in. */
    public static final float CHANNEL_HALF = 1f, CHANNEL_FRONT_Z = {fmt(CHANNEL_FRONT)}f;
    /** The slab that sinks or shatters: x0, y0, z0, x1, y1, z1. */
    public static final float[] SLAB = {{{fmt(x0)}f, {fmt(y0)}f, {fmt(z0)}f, {fmt(x1)}f, {fmt(y1)}f, {fmt(z1)}f}};
    /** The round well the rings turn in. */
    public static final float WELL = {fmt(WELL)}f;
    /** A patch of the slab's carved face in the ring door atlas (u0, v0, u1, v1, 0..1): the burst's fragments. */
    public static final float[] FRAGMENT_UV = {{{fmt(u0)}f, {fmt(v0)}f, {fmt(u1)}f, {fmt(v1)}f}};
    /** The static frame sections; the first, shift 0, is the blockstate model and the baked model adds the rest. */
{frame_rows}    /** The slab's sections, drawn by the renderer. */
{slab_rows}    /** Each ring's sections and its lit node's, about (RING_LOCAL, RING_LOCAL). */
{rings}    /** Frame boxes left standing when the door is open: jambs, lintel and threshold. */
    private static final double[][] FRAME_BOXES = {{
{boxes}
    }};
    private static final Map<BlockPos, VoxelShape>[] OPEN_CELLS = openCells();

    private RingDoorGeometry() {{}}

    /** Whether the north-frame cell at {{@code offset}} holds the frame, so its filler stays when the door opens. */
    public static boolean isFrame(BlockPos offset) {{
        return Math.abs(offset.getX()) == 2 || offset.getY() == 4;
    }}

    /** The door's outline in one of its cells, from the controller; {{@code offset}} is in world (turned) space. */
    public static VoxelShape shape(Direction facing, BlockPos offset, boolean open) {{
        return open ? OPEN_CELLS[facing.get2DDataValue()].getOrDefault(offset, Shapes.empty()) : Shapes.block();
    }}

    /** A north-facing pixel point turned to {{@code facing}} and returned in block units from the controller. */
    public static double[] point(Direction facing, double x, double y, double z) {{
        for (int turn = 0; turn < turns(facing); turn++) {{
            double nextX = 16 - z;
            z = x;
            x = nextX;
        }}
        return new double[]{{x / 16, y / 16, z / 16}};
    }}

    private static int turns(Direction facing) {{
        return switch (facing) {{ case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; }};
    }}

    private static BlockPos[] fillers() {{
        List<BlockPos> cells = new ArrayList<>();
        for (int x = -2; x <= 2; x++)
            for (int y = 0; y <= 4; y++)
                if (x != 0 || y != 0) cells.add(new BlockPos(x, y, 0));
        return cells.toArray(BlockPos[]::new);
    }}

    @SuppressWarnings("unchecked")
    private static Map<BlockPos, VoxelShape>[] openCells() {{
        Map<BlockPos, VoxelShape>[] result = new Map[4];
        for (Direction facing : Direction.Plane.HORIZONTAL) {{
            Map<BlockPos, VoxelShape> cells = new LinkedHashMap<>();
            for (double[] box : FRAME_BOXES) {{
                double x0 = box[0], z0 = box[2], x1 = box[3], z1 = box[5];
                for (int turn = 0; turn < turns(facing); turn++) {{
                    double nextX0 = 16 - z1, nextX1 = 16 - z0;
                    z0 = x0; z1 = x1; x0 = nextX0; x1 = nextX1;
                }}
                VoxelShape shape = Block.box(x0, box[1], z0, x1, box[4], z1);
                for (int x = (int) Math.floor(x0 / 16); x < (int) Math.ceil(x1 / 16); x++)
                    for (int y = (int) Math.floor(box[1] / 16); y < (int) Math.ceil(box[4] / 16); y++)
                        for (int z = (int) Math.floor(z0 / 16); z < (int) Math.ceil(z1 / 16); z++) {{
                            BlockPos offset = new BlockPos(x, y, z);
                            VoxelShape clipped = Shapes.join(shape.move(-x, -y, -z), Shapes.block(), BooleanOp.AND);
                            if (!clipped.isEmpty()) cells.merge(offset, clipped, Shapes::or);
                        }}
            }}
            cells.replaceAll((offset, shape) -> shape.optimize());
            result[facing.get2DDataValue()] = Map.copyOf(cells);
        }}
        return result;
    }}
}}
'''


FRAME_BOXES = [(-32, 0, 0, -26, 80, 16), (42, 0, 0, 48, 80, 16), (-26, 74, 0, 42, 80, 16), (-26, 0, 0, 42, 1, 16)]


def game_uvs(elements):
    """The preview rasterizer lays u across side faces and v across top faces the other way from Minecraft's face
    baker (a north face's u0 is at its max x in game); flip them so the preview shows what the game draws."""
    for el in elements:
        for side, f in el['faces'].items():
            u0, v0, u1, v1 = f['uv']
            f['uv'] = [u0, v1, u1, v0] if side in ('up', 'down') else [u1, v0, u0, v1]
    return elements


def preview_sheet(panels, out_name, size=520, scale=5.2):
    """A row of front views (yaw, pitch) of whole-door element lists, recentred on the door."""
    import numpy as np
    from PIL import Image, ImageDraw
    from clairaudiograph import render
    texture = np.array(Image.open(ASSETS / f'textures/block/{ATLAS}.png').convert('RGBA'))
    cols = min(len(panels), 3)
    rows = math.ceil(len(panels) / cols)
    sheet = Image.new('RGBA', (size * cols, (size + 26) * rows), (30, 24, 28, 255))
    draw = ImageDraw.Draw(sheet)
    for i, (title, elements, yaw, pitch) in enumerate(panels):
        tile = render(game_uvs(moved(elements, 0, -32, 0)), {'atlas': texture}, yaw, pitch, size, scale)
        x, y = size * (i % cols), (size + 26) * (i // cols)
        sheet.paste(tile, (x, y + 26))
        draw.text((x + 10, y + 6), title, fill=(220, 200, 200))
    out = ROOT / f'build/model-previews/{out_name}.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(out)
    print(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    groups = {'frame': frame(), 'slab': slab()}
    glows, channels = {}, []
    for name, bands in RINGS:
        groups[name], glows[name + '_lit'], c = ring(name, bands)
        channels.append(c)
    groups.update(glows)
    moved_faces = separate(groups['frame']) + separate(groups['slab'])
    GROUPS.update(groups)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')

    outputs, tables = {}, {}
    for part, elements in models.items():
        table = []
        for n, (shift, section) in enumerate(sections(elements).items()):
            if part == 'frame' and n == 0:
                assert shift == (0, 0, 0), 'the frame section around the controller is the blockstate model'
                model = 'ring_door'
            else:
                model = f'ring_door_{part}_{n}'
            for el in section:
                assert all(-16 <= v <= 32 for v in el['from'] + el['to']), (model, el['name'], el['from'], el['to'])
            outputs[f'block/{model}'] = model_json(section, ATLAS, atlas.height, PARTICLE)
            table.append((model, shift))
        tables[part] = table
    # The fragments' face: the first slab tile's front, read back from its packed UVs.
    tile = next(el for el in models['slab'] if el['name'] == 'slab' and 'north' in el['faces'])
    u0, v0, u1, v1 = tile['faces']['north']['uv']
    fragment_uv = (round(u0 / 16, 5), round(v0 / 16, 5), round(u1 / 16, 5), round(v1 / 16, 5))
    for old in (ASSETS / 'models/block').glob('ring_door_*.json'):
        if f'block/{old.stem}' not in outputs:
            old.unlink()
    aligned = models['frame'] + models['slab']
    for name, _ in RINGS:
        aligned += moved(models[name] + models[name + '_lit'], CENTRE[0] - RING_C, CENTRE[1] - RING_C)
    project = model_json(aligned, ATLAS, atlas.height, PARTICLE)
    write(outputs, 'ring_door', project)
    (ASSETS / 'blockstates/ring_door.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    ring_tables = {}
    for name, _ in RINGS:
        ring_tables[name] = tables[name]
        ring_tables[name + '_lit'] = tables[name + '_lit']
    GEOMETRY.write_text(geometry_java(dict(frame=tables['frame'], slab=tables['slab'], **ring_tables), channels,
                                      FRAME_BOXES, fragment_uv), encoding='utf-8')
    counts = ', '.join(f'{part} {len(models[part])}/{len(tables[part])}' for part in models)
    print(f'Exported ring door: elements/sections {counts}; {moved_faces} faces separated; 128x{atlas.height} atlas')

    if args.preview:
        def rings(turns, lit=0):
            out = []
            for i, (name, _) in enumerate(RINGS):
                parts = models[name] + (models[name + '_lit'] if i < lit else [])
                out += moved(parts, CENTRE[0] - RING_C, CENTRE[1] - RING_C, 0, turns[i])
            return out
        body = models['frame'] + models['slab']
        scrambled = (112.5, -67.5, 157.5)
        preview_sheet([('closed, no node lit', body + rings(scrambled), 0, 6),
                       ('two nodes lit', body + rings((0, 0, scrambled[2]), 2), 0, 6),
                       ('every node lit, notches on the seam', body + rings((0, 0, 0), 3), 0, 6),
                       ('three-quarter', body + rings((0, 0, 0), 3), 35, 18),
                       ('open: the frame and the track', models['frame'], 20, 22),
                       ('the back, from the chamber', body + rings((0, 0, 0)), 180, 8)], 'ring_door')


if __name__ == '__main__':
    main()

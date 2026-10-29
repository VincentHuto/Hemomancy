"""Author the Containment Anchor block: the dormant, built form of Seraphae's chain anchor pylon.

The fight's ContainmentAnchorEntity re-cut as architecture: a stepped calcite base, a fluted pillar
girt with hematic-iron bands, a capital and a crowned head carrying the cross-arms. From each arm tip a
heavy iron chain hangs plumb, then slumps across the base ledge to an eye bolt in the plinth corner, so
the four chains pinwheel round the base. The head keeps the entity's ring-and-cross glyph as a carved
seam with a faint cold tint; nothing glows, the anchor is asleep.

The snapped variant (`snapped=true`, Seraphae's Downfall) is the same anchor broken: see the snapped
section below. It has its own atlas (`containment_anchor_block_snapped`) so the whole anchor's outputs
stay byte-identical when this re-runs.

Emits the lower and upper block models of each (`containment_anchor_block_<half>[_snapped]`), the
blockstate (half x snapped), the painted atlases and editable Blockbench projects. Python 3 + Pillow +
NumPy. `--preview` also writes textured turnarounds to build/model-previews/containment_anchor_block.png,
containment_anchor_block_snapped.png and containment_anchor_block_compare.png (whole over snapped).

Coordinates are block pixels, authored as one 32-pixel-tall piece and cut at y = 16 into the two halves.
The anchor is free-standing and four-way symmetric (no facing); its footprint stays inside x, z 1..15.
"""
import argparse
import copy
import json
import math

from clairaudiograph import clamp, noise
from fixture_atlas import (ASSETS, PALETTE, box, build, item_elements, model_json, preview, preview_pair, separate,
                           spin, split_storeys, write)

NAME = 'containment_anchor_block'
ATLAS = NAME
PARTICLE = f'hemomancy:block/{ATLAS}'

CALCITE = (216, 214, 206)
CALCITE_VEIN = (176, 178, 176)
QUARTZ = (234, 230, 222)
SEAM = (148, 162, 164)       # the dormant cold seam: a carved channel with a faint cold tint, not lit
LINK_DARK = (40, 34, 42)

CHAIN_X = 2.25               # centre line of the west chain; the other three are its quarter turns
LINK_W, LINK_H, LINK_STEP = 2.5, 4, 3


# --- quarter turns ----------------------------------------------------------------------------

FACE_TURN = {'north': 'east', 'east': 'south', 'south': 'west', 'west': 'north', 'up': 'up', 'down': 'down'}


def turn(el):
    """The element turned 90 degrees clockwise seen from above: (x, z) -> (16 - z, x)."""
    out = copy.deepcopy(el)
    (x0, y0, z0), (x1, y1, z1) = el['lo'], el['hi']
    out['lo'] = [16 - z1, y0, x0]
    out['hi'] = [16 - z0, y1, x1]
    out['skip'] = {FACE_TURN[s] for s in el['skip']}
    out['faces'] = {FACE_TURN[k]: v for k, v in el['faces'].items()}
    if el['rotation']:
        r = el['rotation']
        ox, oy, oz = r['origin']
        axis, angle = r['axis'], r['angle']
        if axis == 'z':
            axis, angle = 'x', -angle
        elif axis == 'x':
            axis = 'z'
        out['rotation'] = dict(angle=angle, axis=axis, origin=[16 - oz, oy, ox])
    return out


def four_ways(elements):
    out, current = [], list(elements)
    for _ in range(4):
        out += current
        current = [turn(el) for el in current]
    return out


# --- geometry ----------------------------------------------------------------------------------

def base():
    e = [box('plinth', (1, 0, 1), (15, 2, 15), 'calcite'),
         box('step', (3.5, 2, 3.5), (12.5, 3.5, 12.5), 'calcite'),
         box('socle_band', (4.25, 3.5, 4.25), (11.75, 4.25, 11.75), 'band'),
         box('socle', (4.5, 4.25, 4.5), (11.5, 7, 11.5), 'calcite'),
         box('socle_cap', (4.25, 7, 4.25), (11.75, 7.75, 11.75), 'quartz')]
    return e


def pillar():
    e = [box('pillar', (5.5, 7.75, 5.5), (10.5, 23, 10.5), 'calcite', north='flute', south='flute',
             east='flute', west='flute', skip=('down',))]
    for i, y in enumerate((10, 15, 20.5)):
        e.append(box(f'band{i}', (5.25, y, 5.25), (10.75, y + 1, 10.75), 'band'))
    # Shackle rings hanging from the middle band: the binding the anchor once held taut.
    e += four_ways([box('band_ring', (7, 12.25, 5), (9, 15, 5.25), 'ring', skip=('south',))])
    return e


def head():
    e = [box('capital', (4.75, 23, 4.75), (11.25, 24, 11.25), 'quartz'),
         box('crown', (4.5, 24, 4.5), (11.5, 29, 11.5), 'calcite', north='glyph', south='glyph', east='glyph',
             west='glyph'),
         box('crown_cap', (5, 29, 5), (11, 30, 11), 'quartz', skip=('down',))]
    e += four_ways([box('crenel', (4.25, 29, 4.25), (5.5, 30.5, 5.5), 'quartz', skip=('down',))])
    e.append(box('finial_collar', (6.75, 30, 6.75), (9.25, 31, 9.25), 'ferric', skip=('down',)))
    e.append(box('finial', (7.25, 31, 7.25), (8.75, 32, 8.75), 'quartz', skip=('down',)))
    # Cross-arms: one stone arm each way out of the crown, capped with an iron collar and an eye.
    arm = [box('arm', (1.75, 25.75, 7), (4.5, 27.75, 9), 'calcite', skip=('east',)),
           box('arm_cap', (1, 25.5, 6.75), (1.75, 28, 9.25), 'quartz'),
           box('arm_collar', (2, 25.5, 6.75), (3, 28, 9.25), 'band'),
           box('arm_eye', (1.75, 24.25, 7.5), (2.75, 25.25, 8.5), 'ring', skip=('up',))]
    return e + four_ways(arm)


def link(name, cx, cy, cz, flat=False, lying=False, tilt=0):
    """One heavy link centred on (cx, cy, cz). A `flat` link faces north/south (thin in z); otherwise it
    faces east/west (thin in x). A `lying` link rests on its back along z, its open face upward. `tilt`
    leans an east/west-facing link about x (negative swings its foot toward +z)."""
    if lying:
        lo, hi = (cx - LINK_W / 2, cy - .25, cz - LINK_H / 2), (cx + LINK_W / 2, cy + .25, cz + LINK_H / 2)
    elif flat:
        lo, hi = (cx - LINK_W / 2, cy - LINK_H / 2, cz - .5), (cx + LINK_W / 2, cy + LINK_H / 2, cz + .5)
    else:
        lo, hi = (cx - .5, cy - LINK_H / 2, cz - LINK_W / 2), (cx + .5, cy + LINK_H / 2, cz + LINK_W / 2)
    return box(name, lo, hi, 'link', rotation=spin('x', tilt, (cx, cy, cz)) if tilt else None)


def west_chain():
    """Plumb from the arm eye, then slack: it folds over onto the ledge and lies along it to the bolt."""
    e, top, i = [], 24.75, 0
    while top - LINK_H > 3.5:
        e.append(link(f'chain_link{i}', CHAIN_X, top - LINK_H / 2, 8, flat=i % 2 == 0))
        top -= LINK_STEP
        i += 1
    e.append(link('chain_fold', CHAIN_X, 4.5, 9.4, tilt=-45))
    e.append(link('chain_lying', CHAIN_X, 2.25, 12, lying=True))
    e.append(box('eye_bolt_plate', (1, 2, 13.25), (3.5, 2.25, 14.75), 'iron', skip=('down',)))
    e.append(box('eye_bolt', (1, 2.25, 13.75), (3.5, 4.75, 14.25), 'ring', skip=('down',)))
    return e


def anchor():
    return base() + pillar() + head() + four_ways(west_chain())


# --- the snapped anchor (Seraphae's Downfall, the Infirmary Row) -------------------------------
#
# The same anchor after the binding failed: the plinth corner chipped, the pillar split down its west
# face and broken through high on the shaft, and the whole head (capital, crown and a finger of shaft)
# slid east off the break and slumped there, so the stump's jagged top shows on its west side. The east
# arm sheared off when the head fell; it lies on the south ledge with its chain still through its eye,
# the chain coiled back round the east ledge to its bolt. The rest are snapped: a torn stub dangles from
# the north arm and the south arm, a sprung link from the raised west arm, the north chain lies bolted
# and torn along its ledge, and the south-west bolt holds one sprung link over the plinth edge.

FOOT = 21.5                  # the head's broken foot, in the upright anchor
SLIDE = 1.5                  # how far the head slid east off the break before it tipped
LEAN = -22.5                 # its lean about z (negative tips the top east)
PIVOT = (10.5, 19.25, 8)     # the stump's east lip, which the head rests and hinges on


def head_point(x, y, z):
    """Where a point of the upright head sits once the head has slid and slumped."""
    dx, dy = x + SLIDE - PIVOT[0], y - FOOT
    a = math.radians(LEAN)
    return PIVOT[0] + dx * math.cos(a) - dy * math.sin(a), PIVOT[1] + dx * math.sin(a) + dy * math.cos(a), z


def slump(elements):
    """The head as it lies now: slid east, its foot set on the stump's lip, tipped about that lip."""
    out = []
    for el in elements:
        el = copy.deepcopy(el)
        for corner in (el['lo'], el['hi']):
            corner[0] += SLIDE
            corner[1] -= FOOT - PIVOT[1]
        el['rotation'] = spin('z', LEAN, PIVOT)
        out.append(el)
    return out


def snapped_base():
    """The anchor's base with its north-east plinth corner knocked off and the socle split on its west."""
    return [box('plinth', (1, 0, 3.5), (15, 2, 15), 'calcite'),
            box('plinth_n', (1, 0, 1), (11.75, 2, 3.5), 'calcite', east='broken'),
            box('plinth_chip', (11.75, 0, 1), (15, 1, 3.5), 'calcite', up='broken'),
            box('step', (3.5, 2, 3.5), (12.5, 3.5, 12.5), 'calcite'),
            box('socle_band', (4.25, 3.5, 4.25), (11.75, 4.25, 11.75), 'band'),
            box('socle', (4.5, 4.25, 4.5), (11.5, 7, 11.5), 'calcite', west='calcite_cracked'),
            box('socle_cap', (4.25, 7, 4.25), (11.75, 7.75, 11.75), 'quartz')]


def stump():
    """The pillar split down its west face and broken through above the third band's seat: a jagged stump."""
    e = [box('pillar', (5.5, 7.75, 5.5), (10.5, 18, 10.5), 'calcite', north='flute', south='flute',
             east='flute', west='flute_cracked', up='broken', skip=('down',))]
    for i, y in enumerate((10, 15)):
        e.append(box(f'band{i}', (5.25, y, 5.25), (10.75, y + 1, 10.75), 'band'))
    # The jagged break: four quarters of the shaft standing to different heights, fracture on top and inside.
    quarters = [('nw', (5.5, 5.5), 19.75, ('north', 'west')), ('sw', (5.5, 8), 20.25, ('south', 'west')),
                ('ne', (8, 5.5), 19.25, ('north', 'east')), ('se', (8, 8), 19.25, ('south', 'east'))]
    for name, (x, z), top, outer in quarters:
        faces = {f: ('flute_cracked' if f == 'west' else 'flute') if f in outer else 'broken'
                 for f in ('north', 'south', 'east', 'west')}
        e.append(box(f'stump_{name}', (x, 18, z), (x + 2.5, top, z + 2.5), 'broken', skip=('down',), **faces))
    e.append(box('stump_spike', (5.5, 20.25, 8.5), (6.75, 21.75, 9.75), 'broken', skip=('down',),
                 west='flute', south='flute'))
    # The third band burst when the shaft broke: a torn strip of it still hangs off the west face.
    e.append(box('band2_torn', (4.75, 17, 6), (5.25, 19.75, 7.25), 'band',
                 rotation=spin('x', 22.5, (5, 19.75, 6.6))))
    # Two shackle rings still hang from the middle band; the south one hangs askew on a sprung pin.
    e.append(box('band_ring_n', (7, 12.25, 5), (9, 15, 5.25), 'ring', skip=('south',)))
    e.append(turn(turn(turn(box('band_ring_w', (7, 12.25, 5), (9, 15, 5.25), 'ring', skip=('south',))))))
    e.append(box('band_ring_s', (7, 12.25, 10.75), (9, 15, 11), 'ring', skip=('north',),
                 rotation=spin('z', 22.5, (8, 15, 10.9))))
    return e


def fallen_head():
    """The head with a finger of shaft under it, upright as built; slump() lays it on the stump."""
    e = [box('shaft_top', (5.5, FOOT, 5.5), (10.5, 23, 10.5), 'calcite', north='flute', south='flute',
             east='flute', west='flute', down='broken'),
         box('capital', (4.75, 23, 4.75), (11.25, 24, 11.25), 'quartz'),
         box('crown', (4.5, 24, 4.5), (11.5, 29, 11.5), 'calcite', north='glyph', south='glyph', east='glyph',
             west='glyph_cracked'),
         box('crown_cap', (5, 29, 5), (11, 30, 11), 'quartz', skip=('down',))]
    # The west crenels stand; the east two broke off in the fall (one lies on the west ledge).
    for x, z in ((4.25, 4.25), (4.25, 10.25)):
        e.append(box('crenel', (x, 29, z), (x + 1.25, 30.5, z + 1.25), 'quartz', skip=('down',)))
    for z in (4.25, 10.25):
        e.append(box('crenel_stub', (10.25, 29, z), (11.5, 29.5, z + 1.25), 'quartz', up='broken', skip=('down',)))
    e.append(box('finial_collar', (6.75, 30, 6.75), (9.25, 31, 9.25), 'ferric', skip=('down',)))
    e.append(box('finial', (7.25, 31, 7.25), (8.75, 32, 8.75), 'quartz', skip=('down',)))
    arm = [box('arm', (1.75, 25.75, 7), (4.5, 27.75, 9), 'calcite', skip=('east',)),
           box('arm_cap', (1, 25.5, 6.75), (1.75, 28, 9.25), 'quartz'),
           box('arm_collar', (2, 25.5, 6.75), (3, 28, 9.25), 'band'),
           box('arm_eye', (1.75, 24.25, 7.5), (2.75, 25.25, 8.5), 'ring', skip=('up',))]
    turned = four_ways(arm)
    e += turned[0:4] + turned[4:8] + turned[12:16]          # west, north, south; the east arm sheared off
    e.append(box('arm_stub_e', (11.5, 25.75, 7), (12, 27.75, 9), 'calcite', east='broken'))
    return e


def sprung_link(name, cx, cy, cz, flat=False, tilt=0, axis='x'):
    """A link pulled open at its foot, swinging from its top: the broken end of a chain."""
    el = link(name, cx, cy, cz, flat=flat)
    el['mat'] = 'sprung'
    el['skip'] = {'down'}
    if tilt:
        el['rotation'] = spin(axis, tilt, (cx, cy + LINK_H / 2, cz))
    return el


def hanging_stub(prefix, eye, links, flat_first, axis, tilt):
    """A torn chain hanging plumb from an eye: whole links, then a sprung one swung outward."""
    x, top, z = eye
    e = []
    for i in range(links):
        e.append(link(f'{prefix}{i}', x, top - LINK_H / 2, z, flat=(i % 2 == 0) == flat_first))
        top -= LINK_STEP
    e.append(sprung_link(f'{prefix}_end', x, top - LINK_H / 2, z, flat=(links % 2 == 0) == flat_first, tilt=tilt,
                         axis=axis))
    return e


def lie_x(name, cx, cz, mat='link'):
    return box(name, (cx - LINK_H / 2, 2, cz - LINK_W / 2), (cx + LINK_H / 2, 2.5, cz + LINK_W / 2), mat)


def edge_x(name, cx, cz, mat='link'):
    return box(name, (cx - LINK_H / 2, 2, cz - .5), (cx + LINK_H / 2, 2 + LINK_W, cz + .5), mat)


def lie_z(name, cx, cz, mat='link'):
    return box(name, (cx - LINK_W / 2, 2, cz - LINK_H / 2), (cx + LINK_W / 2, 2.5, cz + LINK_H / 2), mat)


def edge_z(name, cx, cz, mat='link'):
    return box(name, (cx - .5, 2, cz - LINK_H / 2), (cx + .5, 2 + LINK_W, cz + LINK_H / 2), mat)


def bolts():
    """The four plinth eye bolts, where they always were."""
    plate = box('eye_bolt_plate', (1, 2, 13.25), (3.5, 2.25, 14.75), 'iron', skip=('down',))
    eye = box('eye_bolt', (1, 2.25, 13.75), (3.5, 4.75, 14.25), 'ring', skip=('down',))
    return four_ways([plate, eye])


def snapped_chains():
    e = []
    # Torn ends from the head's arms: a stub off the north and the south arm, one sprung link off the west.
    e += hanging_stub('north_stub', head_point(8, 24.25, 2.25), 2, False, 'x', 22.5)
    e += hanging_stub('south_stub', head_point(8, 24.25, 13.75), 1, False, 'x', -22.5)
    e += hanging_stub('west_stub', head_point(2.25, 24.25, 8), 0, True, 'z', -22.5)
    # North: still bolted at the north-west corner, lying east along its ledge and torn off there.
    e += [edge_x('north_lie0', 3.75, 2.25), lie_x('north_lie1', 6.5, 2.25), edge_x('north_lie2', 9.25, 2.25)]
    end = lie_x('north_end', 11.75, 2.25, 'sprung')
    end['rotation'] = spin('z', -22.5, (11.75, 2.25, 2.25))      # its torn end dips onto the chipped corner
    e.append(end)
    # East: slack, coiled from the north-east bolt down the east ledge and round onto the south ledge,
    # where the sheared arm lies with the chain still through its eye.
    e += [lie_z('east_coil0', 13.75, 4), edge_z('east_coil1', 13.75, 6.75), lie_z('east_coil2', 13.75, 9.5)]
    e.append(box('east_coil_turn', (11.25, 2, 12.5), (15.25, 3, 13.5), 'link',
                 rotation=spin('y', 45, (13.25, 2.5, 13))))
    e += [lie_x('east_coil3', 10.5, 13.75), edge_x('east_coil4', 7.75, 13.75)]
    arm = [box('fallen_arm', (2.75, 2, 12.75), (5.5, 4, 14.75), 'calcite', west='broken'),
           box('fallen_arm_collar', (5.25, 1.875, 12.5), (6.25, 4.375, 15), 'band'),
           box('fallen_arm_cap', (6.25, 1.875, 12.5), (7, 4.375, 15), 'quartz')]
    for el in arm:
        el['rotation'] = spin('y', 22.5, (5, 3, 13.75))
    e += arm
    # South-west: the bolt holds one sprung link, pulled over the plinth edge.
    e.append(box('sw_sprung', (1, .75, 13.75), (3.5, 4.75, 14.75), 'sprung', skip=('down',),
                 rotation=spin('x', -45, (2.25, 2.75, 14.25))))
    # Rubble on the west ledge: the broken crenel and a calcite chip.
    e.append(box('rubble_crenel', (1.5, 2, 5.5), (2.75, 3.25, 6.75), 'quartz', up='broken',
                 rotation=spin('y', 22.5, (2.1, 2.6, 6.1))))
    e.append(box('rubble_chip', (1.25, 2, 8.5), (2.5, 2.75, 9.75), 'broken',
                 rotation=spin('y', -22.5, (1.9, 2.4, 9.1))))
    return e


def snapped_anchor():
    return snapped_base() + stump() + slump(fallen_head()) + bolts() + snapped_chains()


# --- special faces ----------------------------------------------------------------------------

def calcite(face, lx, ly, w, h, key):
    """Pale calcite: soft grey veins running on the diagonal, worn darker at the arrises."""
    n = noise('calcite', key, face, lx, ly)
    edge = (lx in (0, w - 1) or ly in (0, h - 1)) and w >= 3 and h >= 3
    rgb = clamp(CALCITE, int(n * 10) - 5 - (12 if edge else 0) + (8 if ly == 0 and face != 'up' else 0))
    if noise('vein', key, face, (lx + ly) // 2, (lx - ly) // 5) < .07:
        rgb = clamp(CALCITE_VEIN, int(n * 8))
    elif n > .97:
        rgb = clamp(CALCITE, -30)
    return rgb + (255,)


def quartz(face, lx, ly, w, h, key):
    n = noise('quartz', key, face, lx, ly)
    top = ly == 0 and face not in ('up', 'down')
    rgb = clamp(QUARTZ, int(n * 8) - 4 + (14 if top else 0) - (16 if ly == h - 1 and h > 1 else 0))
    if face == 'up' and (lx in (0, w - 1) or ly in (0, h - 1)):
        rgb = clamp(QUARTZ, 10)
    return rgb + (255,)


def flute(face, lx, ly, w, h, key):
    """Pillar face: two shallow flutes between three fillets, the fillets catching the light."""
    rgb = calcite(face, lx, ly, w, h, key)[:3]
    if lx in (1, 3):
        rgb = clamp(rgb, -26)
    elif lx in (0, 4):
        rgb = clamp(rgb, -10)
    return rgb + (255,)


GLYPH = ['..###..',
         '.#.#.#.',
         '#######',
         '.#.#.#.',
         '..###..']


def glyph(face, lx, ly, w, h, key):
    """The crown: the entity's ring-and-cross binding glyph cut as a channel, faintly cold and unlit."""
    if face in ('north', 'south', 'east', 'west') and GLYPH[ly % 5][lx % 7] == '#':
        return clamp(SEAM, int(noise('seam', key, face, lx, ly) * 10) - 5) + (255,)
    return calcite(face, lx, ly, w, h, key)


def band(face, lx, ly, w, h, key):
    """Hematic-iron band: polished top lip, darker belly, a rivet every third texel."""
    n = noise('band', key, face, lx, ly)
    if face in ('up', 'down'):
        return clamp(PALETTE['iron'], int(n * 10) - 2 + (10 if face == 'up' else -10)) + (255,)
    if ly == 0:
        rgb = clamp(PALETTE['ferric'], 14 + int(n * 8))
    else:
        rgb = clamp(PALETTE['iron'], int(n * 10) - 5)
        if lx % 3 == 1 and h >= 2:
            rgb = clamp(PALETTE['ferric'], 6)
    if noise('vein', key, face, lx, ly) < .015:
        rgb = (98, 24, 34)
    return rgb + (255,)


def ring(face, lx, ly, w, h, key):
    """A forged ring or eye: an open centre on its broad faces, iron all round."""
    if w >= 3 and h >= 3 and 0 < lx < w - 1 and 0 < ly < h - 1:
        return (0, 0, 0, 0)
    lit = ly == 0 or lx == 0
    return clamp(PALETTE['ferric'] if lit else PALETTE['iron'], int(noise('ring', key, face, lx, ly) * 8)) + (255,)


def chain_link(face, lx, ly, w, h, key):
    """A heavy chain link: open slot down its broad faces, bright on the upper-left arris."""
    n = noise('link', key, face, lx, ly)
    if w >= 3 and h >= 3 and 0 < lx < w - 1 and 0 < ly < h - 1:
        return (0, 0, 0, 0)
    if lx == 0 or ly == 0:
        rgb = clamp(PALETTE['ferric'], int(n * 10) - 4)
    elif lx == w - 1 or ly == h - 1:
        rgb = clamp(LINK_DARK, int(n * 6))
    else:
        rgb = clamp(PALETTE['iron'], int(n * 10) - 4)
    return rgb + (255,)


def ferric(face, lx, ly, w, h, key):
    n = noise('ferric', key, face, lx, ly)
    return clamp(PALETTE['ferric'], int(n * 10) - 5 + (20 if ly == 0 and h > 1 else 0)) + (255,)


BROKEN = (198, 194, 184)      # fresh fracture: the calcite's raw, granular inside
BROKEN_PIT = (132, 130, 128)


def broken(face, lx, ly, w, h, key):
    """A fracture surface: raw granular calcite, paler than the weathered skin, pitted with shadowed hollows
    and a few glittering quartz grains."""
    n = noise('broken', key, face, lx, ly)
    hollow = noise('hollow', key, face, lx // 2, ly // 2)
    if hollow < .16:
        rgb = clamp(BROKEN_PIT, int(n * 14) - 4)
    elif hollow < .26:
        rgb = clamp(BROKEN, -26 + int(n * 8))
    elif n > .93:
        rgb = (242, 240, 234)
    else:
        rgb = clamp(BROKEN, int(n * 16) - 8)
    return rgb + (255,)


def crack(base_fn):
    """A face split by a dark crack wandering down it, with a pale lip on one side."""
    def paint_cracked(face, lx, ly, w, h, key):
        if face in ('north', 'south', 'east', 'west'):
            x = w // 2 + (1 if (ly // 2) % 3 == 1 else -1 if (ly // 2) % 3 == 2 else 0)
            if lx == x:
                return clamp((96, 94, 92), int(noise('crack', key, face, ly) * 10)) + (255,)
            if lx == x + 1:
                return clamp(BROKEN, 12) + (255,)
        return base_fn(face, lx, ly, w, h, key)
    return paint_cracked


def sprung(face, lx, ly, w, h, key):
    """A link pulled open: the ring broken at its foot, one side bent out and gone bright where it tore."""
    rgb = chain_link(face, lx, ly, w, h, key)
    if w >= 3 and h >= 3:
        if ly == h - 1 and 0 < lx:
            return (0, 0, 0, 0)
        if lx == w - 1 and ly >= h - 2:
            return (0, 0, 0, 0)
        if lx == w - 1 and ly == h - 3:
            return clamp(PALETTE['ferric'], 30) + (255,)
    return rgb


SPECIAL = dict(calcite=calcite, quartz=quartz, flute=flute, glyph=glyph, band=band, ring=ring, link=chain_link,
               ferric=ferric, broken=broken, flute_cracked=crack(flute), calcite_cracked=crack(calcite),
               glyph_cracked=crack(glyph), sprung=sprung)


def blockstate():
    return {'variants': {f'half={half},snapped={str(snapped).lower()}':
                         {'model': f'hemomancy:block/{NAME}_{half}{"_snapped" if snapped else ""}'}
                         for half in ('lower', 'upper') for snapped in (False, True)}}


def bounds(elements):
    """Axis-aligned bounds of a model's elements, rotations applied."""
    lo, hi = [99.0] * 3, [-99.0] * 3
    for el in elements:
        corners = [[x, y, z] for x in (el['from'][0], el['to'][0]) for y in (el['from'][1], el['to'][1])
                   for z in (el['from'][2], el['to'][2])]
        rot = el.get('rotation')
        for c in corners:
            if rot:
                ax = [(1, 2), (2, 0), (0, 1)]['xyz'.index(rot['axis'])]
                a = math.radians(rot['angle'])
                v, w = c[ax[0]] - rot['origin'][ax[0]], c[ax[1]] - rot['origin'][ax[1]]
                c[ax[0]] = rot['origin'][ax[0]] + v * math.cos(a) - w * math.sin(a)
                c[ax[1]] = rot['origin'][ax[1]] + v * math.sin(a) + w * math.cos(a)
            for k in range(3):
                lo[k], hi[k] = min(lo[k], c[k]), max(hi[k], c[k])
    return [round(v, 2) for v in lo], [round(v, 2) for v in hi]


def export(elements, atlas_name, suffix, project):
    """Cut a 32-pixel anchor into its two storeys, paint its atlas and write its models and project."""
    for el in elements:
        el['density'] = 1
    moved = separate(elements)
    lower, upper = split_storeys(elements)
    atlas, models = build({'lower': lower, 'upper': upper}, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{atlas_name}.png')
    particle = f'hemomancy:block/{atlas_name}'
    lower_model = model_json(models['lower'], atlas_name, atlas.height, particle)
    upper_model = model_json(models['upper'], atlas_name, atlas.height, particle)
    whole = model_json(item_elements((models['lower'], 0), (models['upper'], 16)), atlas_name, atlas.height,
                       particle)
    write({f'block/{NAME}_lower{suffix}': lower_model, f'block/{NAME}_upper{suffix}': upper_model}, project, whole)
    print(f'Exported {project}: {len(lower)} lower + {len(upper)} upper cubes, {moved} faces separated, '
          f'128x{atlas.height} atlas')
    print(f'  lower bounds {bounds(models["lower"])}, upper bounds {bounds(models["upper"])}')
    return whole, models


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    whole, _ = export(anchor(), ATLAS, '', NAME)
    broken_whole, models = export(snapped_anchor(), f'{ATLAS}_snapped', '_snapped', f'{NAME}_snapped')
    head = [{'from': e['lo'], 'to': e['hi'], 'rotation': e['rotation']} for e in slump(fallen_head())[:3]]
    print(f'  slumped head core (shaft top, capital, crown) bounds {bounds(head)}')
    (ASSETS / f'blockstates/{NAME}.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    if args.preview:
        preview(whole['elements'], ATLAS, 'Containment Anchor - lower + upper block models, actual atlas', NAME,
                lift=8, size=400, scale=11)
        preview(broken_whole['elements'], f'{ATLAS}_snapped', 'Containment Anchor, snapped - lower + upper',
                f'{NAME}_snapped', lift=8, size=400, scale=11)
        preview_pair((whole['elements'], ATLAS), (broken_whole['elements'], f'{ATLAS}_snapped'),
                     'Containment Anchor - whole (top) vs snapped (bottom)', f'{NAME}_compare', lift=8, size=360,
                     scale=10)


if __name__ == '__main__':
    main()

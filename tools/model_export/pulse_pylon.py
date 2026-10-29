"""Author the Pulse Pylon: the Repose trial's three-block pylon that fires the stilling pulse.

A stepped plinth of dark venous stone on hematic-iron corner shoes, a tapering venous shaft bound in
polished iron bands with a vertical blood channel down its north face into a lacquered catch basin,
and a head carrying an exposed Crimson Lacquer eye (the focus) facing north under a bone brow, with
an iron crown and a bone finial. Four iron bearing pins stand out of the head at ring height.

The charge ring is NOT part of the static models: it is the standalone `pulse_pylon_ring` model, a
horizontal hematic-iron ring with full-bright crimson inlay, authored in upper-cell coordinates and
drawn by the block entity renderer around the head at RING_Y (it flares up before each pulse).

A second style, `anchor` (Seraphae's Still Ward), re-cuts the pylon as a containment anchor with a pale
Lux lens and the white-gold `pulse_pylon_ring_anchor`; see the anchor-style section below. A third, `pyre`
(Putriciel's Ash Garden), re-cuts it as a bone pyre on a charred crib with no ring: its charge feeds a
Flammeus fire the renderer draws on the pyre's ember bed (PyreFlames); see the pyre-style section. A
fourth, `frost` (Velorum's Cell Row), re-cuts it as a frost pillar of deepslate brick and packed ice under
rime and icicles with a full-bright frost heart, and the pale `pulse_pylon_ring_frost`; see the frost-style
section. Each style has its own atlas (`pulse_pylon`, `pulse_pylon_anchor`, `pulse_pylon_pyre`,
`pulse_pylon_frost`) so re-running leaves the others byte-identical.

Emits per style the lower, middle and upper block models (`pulse_pylon[_anchor|_pyre|_frost]_<part>`), the ring
model (all but the pyre), the atlas and an editable Blockbench project, and one blockstate (facing x part x style; the
`charge` property is a wildcard). No item model. Python 3 + Pillow + NumPy. `--preview` writes
build/model-previews/pulse_pylon.png, pulse_pylon_ring.png, pulse_pylon_anchor.png,
pulse_pylon_ring_anchor.png, pulse_pylon_pyre.png, pulse_pylon_frost.png, pulse_pylon_ring_frost.png and
pulse_pylon_compare.png (venous over anchor).

Coordinates are block pixels for the north-facing state, authored as one 48-pixel-tall piece and cut
at y = 16 and y = 32 into the three cells. The front (the eye and the blood channel) is z = 0.
"""
import argparse
import json
import math

from clairaudiograph import clamp, noise
from containment_anchor_block import (LINK_H, LINK_STEP, band as anchor_band, calcite as anchor_calcite,
                                      chain_link as anchor_link, ferric as anchor_ferric, four_ways,
                                      glyph as anchor_glyph, link, quartz as anchor_quartz, ring as anchor_ring,
                                      turn)
from fixture_atlas import (ASSETS, PALETTE, box, build, item_elements, mirror_x, model_json, preview, preview_pair,
                           separate, spin, split_storeys, write)

ATLAS = 'pulse_pylon'
PARTICLE = 'hemomancy:block/polished_venous_stone'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}

UPPER = 32          # the upper cell's floor in the authored piece
RING_Y = 6.0        # ring centre height in upper-cell pixels
RING_INNER = 6.0    # ring radii in pixels about the cell's centre (8, 8)
RING_OUTER = 7.5
RING_SEGMENTS = 16

VENOUS = (60, 18, 26)
MORTAR = (34, 9, 15)
VEIN = (122, 20, 34)


def pair(el):
    return [el, mirror_x(el)]


def plinth():
    e = [box('step1', (1, 0, 1), (15, 2.5, 15), 'venous'),
         box('step2', (2, 2.5, 2), (14, 5, 14), 'venous'),
         box('step2_cap', (1.75, 5, 1.75), (14.25, 5.5, 14.25), 'ferric'),
         box('step3', (3, 5.5, 3), (13, 7.5, 13), 'venous'),
         box('step3_cap', (2.75, 7.5, 2.75), (13.25, 8, 13.25), 'ferric')]
    # Hematic-iron shoes clasping the four corners of the bottom step.
    for x, z in [(.5, .5), (12.5, .5), (.5, 12.5), (12.5, 12.5)]:
        e.append(box('corner_shoe', (x, 0, z), (x + 3, 3, z + 3), 'iron', skip=('down',)))
    # The front of the second step: a lacquer inset either side of the catch basin.
    e += pair(box('step_panel', (3, 3, 1.75), (5.5, 4.5, 2), 'lacquer_deep', skip=('south',)))
    # Catch basin under the channel, sitting on the second step's cap.
    e.append(box('basin', (6, 5.5, 1.5), (10, 6.75, 2.75), 'lacquer', up='pool'))
    e += pair(box('basin_lip', (5.75, 5.5, 1.25), (6.25, 7, 2.75), 'ferric'))
    return e


def channel(name, y0, y1, z0):
    """The blood channel down the shaft's north face: two iron lips and a recessed bloody runnel."""
    return [box(f'{name}_lip_w', (6.75, y0, z0 - .5), (7.25, y1, z0), 'iron', skip=('south',)),
            box(f'{name}_lip_e', (8.75, y0, z0 - .5), (9.25, y1, z0), 'iron', skip=('south',)),
            box(f'{name}_blood', (7.25, y0, z0 - .25), (8.75, y1, z0), 'channel', skip=('south', 'east', 'west'))]


def shaft():
    e = []
    segments = [(3.5, 8, 16), (4, 17.5, 26), (4.5, 27.5, 34.5)]
    for i, (inset, y0, y1) in enumerate(segments):
        e.append(box(f'shaft{i}', (inset, y0, inset), (16 - inset, y1, 16 - inset), 'venous'))
        e += channel(f'channel{i}', y0, y1, inset)
    # The channel runs down over the top step into the basin.
    e += channel('channel_step', 6.75, 8, 2.75)
    # Polished iron bands at each taper, with lacquer seams.
    for i, (inset, y0) in enumerate([(2.75, 16), (3.25, 26)]):
        e.append(box(f'band{i}', (inset, y0, inset), (16 - inset, y0 + 1.5, 16 - inset), 'ferric'))
        e.append(box(f'band{i}_seam', (inset + .25, y0 + 1.5, inset + .25), (16 - inset - .25, y0 + 1.75,
                                                                              16 - inset - .25), 'lacquer'))
    # Iron straps down the shaft's back corners.
    for i, (inset, y0, y1) in enumerate(segments):
        e += pair(box(f'strap{i}', (inset - .25, y0, 16 - inset - 1), (inset + 1, y1, 16 - inset + .25), 'iron'))
    return e


def head():
    u = UPPER
    e = [box('neck_collar', (4, u + 2.5, 4), (12, u + 3.5, 12), 'ferric'),
         box('neck', (5, u + 3.5, 5), (11, u + 4, 11), 'iron'),
         box('head', (4.5, u + 4, 4.5), (11.5, u + 12, 11.5), 'venous')]
    # Iron corner straps on the head.
    for x, z in [(4.25, 4.25), (10.75, 4.25), (4.25, 10.75), (10.75, 10.75)]:
        e.append(box('head_strap', (x, u + 3.75, z), (x + 1, u + 12.25, z + 1), 'ferric'))
    # Bearing pins the charge ring rides on, inside its inner radius.
    e.append(box('pin_n', (7.5, u + 5.5, 2.75), (8.5, u + 6.5, 4.5), 'ferric', skip=('south',)))
    e.append(box('pin_s', (7.5, u + 5.5, 11.5), (8.5, u + 6.5, 13.25), 'ferric', skip=('north',)))
    e.append(box('pin_w', (2.75, u + 5.5, 7.5), (4.5, u + 6.5, 8.5), 'ferric', skip=('east',)))
    e.append(box('pin_e', (11.5, u + 5.5, 7.5), (13.25, u + 6.5, 8.5), 'ferric', skip=('west',)))
    # The focus: a lacquered eye on a bone-bordered plate, under a bone brow.
    e.append(box('eye_plate', (5.5, u + 7, 4.25), (10.5, u + 11.5, 4.5), 'lacquer_deep', skip=('south',)))
    e.append(box('eye', (6, u + 7.5, 3.5), (10, u + 11, 4.25), 'lacquer', north='eye', skip=('south',)))
    e.append(box('brow', (5.5, u + 11, 3.25), (10.5, u + 11.75, 4.5), 'bone', skip=('south',)))
    e.append(box('lid', (6.5, u + 7, 3.75), (9.5, u + 7.5, 4.25), 'bone', skip=('south',)))
    # The back plate behind the head: a lacquer panel crossed by a bone bar.
    e.append(box('back_panel', (5.5, u + 7, 11.5), (10.5, u + 11, 11.75), 'lacquer_deep', skip=('north',)))
    e.append(box('back_bar', (7.5, u + 7, 11.75), (8.5, u + 11, 12), 'bone', skip=('north',)))
    # Crown and finial.
    e += [box('crown', (4, u + 12, 4), (12, u + 13, 12), 'ferric'),
          box('cap', (5, u + 13, 5), (11, u + 14, 11), 'venous'),
          box('cap_lacquer', (6, u + 14, 6), (10, u + 14.75, 10), 'lacquer'),
          box('finial', (7.25, u + 14.75, 7.25), (8.75, u + 16, 8.75), 'bone',
              rotation=spin('y', 45, (8, u + 15.5, 8)))]
    return e


def ring():
    """The charge ring in upper-cell space: sixteen iron segments with an inlay round the outside and top (crimson
    on the venous ring, white-gold on the anchor's: each style's atlas paints `inlay` its own way)."""
    e = []
    y0, y1 = RING_Y - .75, RING_Y + .75
    body_outer = RING_OUTER - .25
    mid = (RING_INNER + body_outer) / 2
    for k in range(RING_SEGMENTS):
        phi = k * 360 / RING_SEGMENTS
        quarter = round(phi / 90)
        delta = phi - quarter * 90
        along_x = quarter % 2 == 0
        a = math.radians(phi)
        dirx, dirz = math.sin(a), -math.cos(a)   # phi = 0 points north

        def seg(name, r0, r1, ya, yb, mat, extra=None, skip=()):
            rc = (r0 + r1) / 2
            half_t = (r1 - r0) / 2
            half_l = rc * math.tan(math.radians(180 / RING_SEGMENTS)) + .08
            cx, cz = 8 + dirx * rc, 8 + dirz * rc
            if along_x:
                lo, hi = (cx - half_l, ya, cz - half_t), (cx + half_l, yb, cz + half_t)
            else:
                lo, hi = (cx - half_t, ya, cz - half_l), (cx + half_t, yb, cz + half_l)
            rotation = spin('y', -delta, (cx, (ya + yb) / 2, cz)) if delta else None
            return box(f'{name}{k}', lo, hi, mat, rotation=rotation, extra=extra, skip=skip)

        e.append(seg('ring', RING_INNER, body_outer, y0, y1, 'ferric'))
        e.append(seg('inlay_outer', body_outer, RING_OUTER, RING_Y - .25, RING_Y + .25, 'inlay', BRIGHT))
        e.append(seg('inlay_top', mid - .25, mid + .25, y1, y1 + .125, 'inlay', BRIGHT, skip=('down',)))
    return e


# --- special faces --------------------------------------------------------------------------

def venous(face, lx, ly, w, h, key):
    """Dark venous stone: dressed courses with staggered joints and thin live veins."""
    n = noise('venous', key, face, lx, ly)
    if face not in ('up', 'down') and h >= 4:
        if ly % 4 == 3:
            return clamp(MORTAR, int(n * 6)) + (255,)
        if w >= 6 and lx % 6 == (0 if (ly // 4) % 2 == 0 else 3):
            return clamp(MORTAR, 8 + int(n * 6)) + (255,)
    if noise('vein', key, face, lx // 2, ly) < .05:
        return VEIN + (255,)
    return clamp(VENOUS, int(n * 14) - 7 + (10 if ly % 4 == 0 and face not in ('up', 'down') else 0)) + (255,)


def channel_tex(face, lx, ly, w, h, key):
    """Blood running down the channel: dark runnel, a wetter bright streak, clots."""
    n = noise('channel', key, face, lx, ly)
    if face in ('up', 'down'):
        return (110, 10, 22, 255)
    if lx == 0 and noise('streak', key, ly // 2) > .35:
        return (186, 28, 40, 255)
    if n < .08:
        return (70, 6, 14, 255)
    return clamp(PALETTE['blood'], int(n * 16) - 12) + (255,)


def pool(face, lx, ly, w, h, key):
    n = noise('pool', key, lx, ly)
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(PALETTE['lacquer_deep'], int(n * 8)) + (255,)
    return ((176, 26, 38) if n > .8 else clamp((122, 12, 24), int(n * 12))) + (255,)


EYE = ['.RR.',
       'RPPR',
       'RPHR',
       '.RR.']


def eye(face, lx, ly, w, h, key):
    """The focus: a crimson lacquer iris round a black slit, one wet glint."""
    ch = EYE[min(ly, 3)][min(lx, 3)]
    if ch == '.':
        return (96, 12, 24, 255)
    if ch == 'P':
        return (20, 6, 10, 255)
    if ch == 'H':
        return (236, 120, 120, 255)
    return (196, 30, 44, 255)


def inlay(face, lx, ly, w, h, key):
    n = noise('inlay', key, face, lx, ly)
    return ((236, 64, 70) if n > .82 else clamp((200, 22, 36), int(n * 20) - 6)) + (255,)


SPECIAL = dict(venous=venous, channel=channel_tex, pool=pool, eye=eye, inlay=inlay)


# --- the anchor style (Seraphae's Repose, the Still Ward) ------------------------------------
#
# The same three-storey pylon re-cut in the containment anchor's vocabulary: a stepped calcite plinth
# with a hematic-iron socle band, a fluted calcite shaft girt with iron bands and hung with shackle
# rings, and a crowned head with cross-arms whose chains hang plumb and slump across the plinth to
# their corner bolts. No arm on the front: there the crown carries a pale Lux lens (warm white-gold,
# full-bright) under a quartz hood, her hand reaching out. The charge ring is the pale counterpart
# `pulse_pylon_ring_anchor`, at the same RING_Y round the neck under the capital, below the arms; the
# chains hang inside its inner radius.

ANCHOR_ATLAS = 'pulse_pylon_anchor'
ANCHOR_PARTICLE = 'minecraft:block/calcite'
ANCHOR_CHAIN_X = 2.75        # inside the ring's inner radius (6) with the link's thickness
ARM_Y = 9.5                  # the cross-arms' underside in upper-cell pixels, clear above the ring

LUX_CORE = (255, 251, 232)
LUX = (252, 230, 168)
LUX_RIM = (230, 190, 110)


def anchor_plinth():
    e = [box('plinth', (1, 0, 1), (15, 2.5, 15), 'calcite'),
         box('step', (3.5, 2.5, 3.5), (12.5, 4.5, 12.5), 'calcite'),
         box('socle_band', (4.25, 4.5, 4.25), (11.75, 5.25, 11.75), 'band'),
         box('socle', (4.5, 5.25, 4.5), (11.5, 8, 11.5), 'calcite'),
         box('socle_cap', (4.25, 8, 4.25), (11.75, 8.75, 11.75), 'quartz')]
    return e


def anchor_shaft():
    u = UPPER
    e = [box('shaft', (5, 8.75, 5), (11, u + 7, 11), 'calcite', north='flute6', south='flute6', east='flute6',
             west='flute6')]
    for i, y in enumerate((14, 21.5, 29, u + 3)):
        e.append(box(f'band{i}', (4.75, y, 4.75), (11.25, y + 1, 11.25), 'band'))
    # Shackle rings hanging from the second band, one to a face: the binding the ward once held taut.
    e += four_ways([box('band_ring', (7, 18.75, 4.5), (9, 21.5, 4.75), 'ring', skip=('south',))])
    return e


def anchor_head():
    u = UPPER
    e = [box('capital', (4.25, u + 7, 4.25), (11.75, u + 8, 11.75), 'quartz'),
         box('crown', (4.5, u + 8, 4.5), (11.5, u + 13.5, 11.5), 'calcite', south='glyph', east='glyph',
             west='glyph'),
         box('crown_cap', (5, u + 13.5, 5), (11, u + 14.5, 11), 'quartz', skip=('down',))]
    e += four_ways([box('crenel', (4.25, u + 13.5, 4.25), (5.5, u + 15, 5.5), 'quartz', skip=('down',))])
    e.append(box('finial_collar', (6.75, u + 14.5, 6.75), (9.25, u + 15.25, 9.25), 'ferric', skip=('down',)))
    e.append(box('finial', (7.25, u + 15.25, 7.25), (8.75, u + 16, 8.75), 'quartz', skip=('down',)))
    # The Lux lens on the front, framed in hematic iron under a quartz hood.
    e.append(box('lens', (6, u + 8.75, 3.5), (10, u + 12.75, 4.5), 'lux', extra=BRIGHT, skip=('south',)))
    e.append(box('bezel_foot', (5.5, u + 8.25, 3.75), (10.5, u + 8.75, 4.5), 'band', skip=('south',)))
    e += pair(box('bezel_side', (5.5, u + 8.75, 3.75), (6, u + 12.75, 4.5), 'band', skip=('south',)))
    e.append(box('brow', (5.25, u + 12.75, 3.25), (10.75, u + 13.5, 4.5), 'quartz', skip=('south',)))
    # Cross-arms to the back and sides, each capped and collared with an eye for its chain.
    a = u + ARM_Y
    arm = [box('arm', (1.75, a, 7), (4.5, a + 2, 9), 'calcite', skip=('east',)),
           box('arm_cap', (1, a - .25, 6.75), (1.75, a + 2.25, 9.25), 'quartz'),
           box('arm_collar', (2, a - .25, 6.75), (3, a + 2.25, 9.25), 'band'),
           box('arm_eye', (ANCHOR_CHAIN_X - .5, a - 1.25, 7.5), (ANCHOR_CHAIN_X + .5, a - .25, 8.5), 'ring',
               skip=('up',))]
    turned = four_ways(arm)
    return e + turned[0:4] + turned[8:16]          # west, east, south: the front keeps the lens


def anchor_west_chain():
    """Plumb from the west arm's eye, then slack across the ledge to the south-west bolt."""
    e, top, i = [], UPPER + ARM_Y - 1.25, 0
    while top - LINK_H > 5:
        e.append(link(f'chain_link{i}', ANCHOR_CHAIN_X, top - LINK_H / 2, 8, flat=i % 2 == 0))
        top -= LINK_STEP
        i += 1
    e.append(link('chain_fold', ANCHOR_CHAIN_X, 5, 9.4, tilt=-45))
    e.append(link('chain_lying', 2.25, 2.75, 12, lying=True))
    e.append(box('eye_bolt_plate', (1, 2.5, 13.25), (3.5, 2.75, 14.75), 'iron', skip=('down',)))
    e.append(box('eye_bolt', (1, 2.75, 13.75), (3.5, 5.25, 14.25), 'ring', skip=('down',)))
    return e


def anchor_chains():
    turned = four_ways(anchor_west_chain())
    n = len(turned) // 4
    north = turned[n:2 * n]
    # The front has no arm: its bolt stays, empty, but its chain is not hung.
    return turned[:n] + turned[2 * n:] + [el for el in north if el['name'].startswith('eye_bolt')]


def anchor_body():
    return anchor_plinth() + anchor_shaft() + anchor_head() + anchor_chains()


def flute6(face, lx, ly, w, h, key):
    """The six-wide shaft face: two shallow flutes either side of a central fillet."""
    rgb = anchor_calcite(face, lx, ly, w, h, key)[:3]
    if lx in (1, w - 2):
        rgb = clamp(rgb, -26)
    elif lx in (0, w - 1):
        rgb = clamp(rgb, -10)
    return rgb + (255,)


LENS = ['.RR.',
        'RCCR',
        'RCHR',
        '.RR.']


def lux(face, lx, ly, w, h, key):
    """The Lux lens: a round warm white-gold glass in its iron bezel, white-hot at the heart."""
    if face != 'north':
        return clamp(LUX_RIM, int(noise('lux', key, face, lx, ly) * 10)) + (255,)
    ch = LENS[min(ly, 3)][min(lx, 3)]
    if ch == '.':
        return clamp(PALETTE['iron'], 6) + (255,)
    if ch == 'H':
        return (255, 255, 248, 255)
    if ch == 'C':
        return LUX_CORE + (255,)
    return LUX + (255,)


def lux_inlay(face, lx, ly, w, h, key):
    n = noise('lux_inlay', key, face, lx, ly)
    return (LUX_CORE if n > .82 else clamp(LUX, int(n * 16) - 8)) + (255,)


ANCHOR_SPECIAL = dict(calcite=anchor_calcite, quartz=anchor_quartz, glyph=anchor_glyph, band=anchor_band,
                      ring=anchor_ring, link=anchor_link, ferric=anchor_ferric, flute6=flute6, lux=lux,
                      inlay=lux_inlay)


# --- the pyre style (Putriciel's Repose, the Ash Garden) -------------------------------------
#
# The same three-storey pylon re-cut as a bone pyre: a stepped blackstone plinth with charred-log corner
# posts and an ash heap at its foot; a crib of charred timbers laid crosswise, tapering up the shaft round a
# char core, with long bones stood against its faces under sinew lashings; and at the head a charred
# platform carrying a bed of Flammeus embers in a basket of bones leaning outward, a skull set in front. No
# ring: the renderer draws the fire on the bed (PYRE_BED), growing with the charge. No copper or brass.

PYRE_ATLAS = 'pulse_pylon_pyre'
PYRE_PARTICLE = 'minecraft:block/blackstone'
PYRE_BED = 3.6              # the ember bed's surface in upper-cell pixels, where the fire stands
CRIB_LAYERS = 13            # two-pixel layers from y 8 to 34
BLACKSTONE = (40, 34, 40)
CHAR = (26, 20, 20)
ASH = (112, 106, 100)
SINEW = (128, 82, 76)
SOOT = (18, 8, 10)
EMBER_DEEP = (112, 6, 18)
EMBER = (184, 14, 30)
EMBER_RED = (230, 28, 40)
EMBER_HEAT = (255, 178, 192)


def pyre_plinth():
    e = [box('step1', (1, 0, 1), (15, 2.5, 15), 'blackstone'),
         box('step2', (2, 2.5, 2), (14, 5, 14), 'blackstone'),
         box('step2_cap', (1.75, 5, 1.75), (14.25, 5.5, 14.25), 'charred', up='charred_top'),
         box('step3', (3, 5.5, 3), (13, 7.5, 13), 'blackstone'),
         box('step3_cap', (2.75, 7.5, 2.75), (13.25, 8, 13.25), 'charred', up='charred_top')]
    # Charred log posts at the bottom step's corners, burned down to stumps.
    for x, z, h in [(.5, .5, 3.5), (12.5, .5, 3), (.5, 12.5, 3), (12.5, 12.5, 3.5)]:
        e.append(box('corner_post', (x, 0, z), (x + 3, h, z + 3), 'charred', up='log_end', skip=('down',)))
    # Ash spilled down the front onto the second step.
    e.append(box('ash_heap', (5.5, 5.5, 1.75), (10.5, 6.25, 2.75), 'ash', skip=('down',)))
    e.append(box('ash_heap_top', (6.5, 6.25, 2), (9.5, 6.75, 2.75), 'ash', skip=('down',)))
    return e


def crib_inset(k):
    return 3.5 + round(k / (CRIB_LAYERS - 1) * 4) / 4


def pyre_crib():
    """Charred timbers laid two to a layer, each layer crosswise to the last, round a char core."""
    e = []
    for k in range(CRIB_LAYERS):
        y0, inset = 8 + 2 * k, crib_inset(k)
        lo, hi = inset, 16 - inset
        for a, b in ((lo, lo + 2), (hi - 2, hi)):
            if k % 2 == 0:      # logs along x, their ends east and west
                e.append(box(f'crib{k}', (lo, y0, a), (hi, y0 + 2, b), 'charred', east='log_end', west='log_end',
                             up='charred_top'))
            else:               # logs along z, their ends north and south
                e.append(box(f'crib{k}', (a, y0, lo), (b, y0 + 2, hi), 'charred', north='log_end', south='log_end',
                             up='charred_top'))
    e.append(box('core', (6, 8, 6), (10, 8 + 2 * CRIB_LAYERS, 10), 'char', skip=('down',)))
    return e


def pyre_bones():
    """Long bones stood against the crib's faces, two tiers, each held by a sinew lashing."""
    e = []
    for y0, y1, k in ((8.5, 21, 0), (20, 33.5, 6)):
        inset = crib_inset(k) - .1
        e.append(box('stood_bone', (7.25, y0 + 1, inset - 1.5), (8.75, y1 - 1, inset), 'bone', skip=('south',)))
        e.append(box('stood_bone_knob', (6.75, y0, inset - 1.75), (9.25, y0 + 1, inset), 'bone', skip=('south',)))
        e.append(box('stood_bone_knob', (6.75, y1 - 1, inset - 1.75), (9.25, y1, inset), 'bone', skip=('south',)))
        mid = (y0 + y1) / 2
        e.append(box('lashing', (6.5, mid - .5, inset - 1.75), (9.5, mid + .5, inset - 1.25), 'sinew',
                     skip=('south',)))
    return four_ways(e)


def pyre_head():
    u = UPPER
    e = [box('platform', (3.75, u + 2, 3.75), (12.25, u + 3.25, 12.25), 'charred', up='charred_top'),
         box('ember_bed', (4.5, u + 3.25, 4.5), (11.5, u + PYRE_BED, 11.5), 'embers', extra=BRIGHT,
             skip=('down',))]
    for x, z, sz, h in [(5.6, 7.2, .6, .6), (10.2, 6.4, .5, .5), (8.4, 10.1, .7, .7), (6.4, 10.4, .45, .4),
                        (10.6, 9.8, .5, .45)]:
        e.append(box('coal', (x - sz, u + PYRE_BED, z - sz), (x + sz, u + PYRE_BED + h, z + sz), 'coal',
                     skip=('down',), extra=BRIGHT))
    # The basket: long bones leaning out from the bed's edge, two to a side.
    for x, z, axis, angle in [(5.75, 4.75, 'x', -22.5), (10.25, 4.75, 'x', -22.5),
                              (5.75, 11.25, 'x', 22.5), (10.25, 11.25, 'x', 22.5),
                              (4.75, 5.75, 'z', 22.5), (4.75, 10.25, 'z', 22.5),
                              (11.25, 5.75, 'z', -22.5), (11.25, 10.25, 'z', -22.5)]:
        e += pyre_bone(x, u + 3.25, z, 8.5, axis, angle)
    # A skull set in the crib's top at the front, below the basket, watching the room.
    e.append(box('skull', (5.75, u - 1.5, 2.25), (10.25, u + 1.75, 5.25), 'skull_side', north='skull',
                 up='skull_top'))
    return e


def pyre_bone(x, y, z, length, axis, angle, thick=1.25, knob=2.0):
    t, k = thick / 2, knob / 2
    rot = spin(axis, angle, (x, y, z))
    return [box('basket_shaft', (x - t, y + 1, z - t), (x + t, y + length - 1, z + t), 'bone', rotation=rot,
                skip=('up', 'down')),
            box('basket_foot', (x - k, y, z - k * .8), (x + k, y + 1, z + k * .8), 'bone', rotation=rot),
            box('basket_head', (x - k, y + length - 1, z - k * .8), (x + k, y + length, z + k * .8), 'bone',
                rotation=rot)]


def pyre_body():
    return pyre_plinth() + pyre_crib() + pyre_bones() + pyre_head()


def blackstone(face, lx, ly, w, h, key):
    """Blackstone: near-black with grey glints and dark coursing on the sides."""
    n = noise('blackstone', key, face, lx, ly)
    if n > .93:
        return (78, 70, 76, 255)
    if face not in ('up', 'down') and h >= 4 and ly % 4 == 3:
        return clamp(CHAR, int(n * 6)) + (255,)
    return clamp(BLACKSTONE, int(n * 14) - 7 - (8 if ly < 1 and face not in ('up', 'down') else 0)) + (255,)


def charred(face, lx, ly, w, h, key):
    """Charred timber: alligator-cracked black char, grey ash in the cracks, the odd dull ember."""
    n = noise('charred', key, face, lx, ly)
    if noise('crack', key, face, lx // 2, ly) < .12:
        return clamp(ASH, int(n * 12) - 30) + (255,)
    if n > .97:
        return EMBER_DEEP + (255,)
    return clamp(CHAR, int(n * 14) - 4 + (8 if (lx + ly) % 3 == 0 else 0)) + (255,)


def charred_top(face, lx, ly, w, h, key):
    n = noise('charred_top', key, lx, ly)
    if n > .88:
        return clamp(ASH, int(n * 10) - 6) + (255,)
    return clamp(CHAR, int(n * 12)) + (255,)


def log_end(face, lx, ly, w, h, key):
    """A burned log end: rings gone to char round a black heart, ash on the rim."""
    cx, cy = (w - 1) / 2, (h - 1) / 2
    r = max(abs(lx - cx), abs(ly - cy))
    n = noise('log_end', key, face, lx, ly)
    if r >= max(cx, cy) - .1:
        return clamp(ASH, int(n * 10) - 24) + (255,)
    return clamp((44, 34, 32) if int(r * 2) % 2 == 0 else CHAR, int(n * 8)) + (255,)


def char_core(face, lx, ly, w, h, key):
    return clamp((16, 12, 12), int(noise('core', key, face, lx, ly) * 10)) + (255,)


def ash(face, lx, ly, w, h, key):
    n = noise('ash', key, face, lx, ly)
    return ((40, 34, 34) if n < .18 else clamp(ASH, int(n * 16) - 8)) + (255,)


def sinew(face, lx, ly, w, h, key):
    n = noise('sinew', key, face, lx, ly)
    return clamp(SINEW, int(n * 12) - 6 - (16 if (lx + ly) % 2 else 0)) + (255,)


def pyre_bone_tex(face, lx, ly, w, h, key):
    """Old bone, soot-blackened from below."""
    n = noise('pyre_bone', key, face, lx, ly)
    rgb = clamp(PALETTE['bone'], int(n * 14) - 10)
    if face not in ('up', 'down') and ly >= h - 2 and n < .6:
        rgb = clamp(CHAR, int(n * 20))
    elif noise('soot', key, face, lx, ly // 2) < .08:
        rgb = (70, 60, 56)
    return rgb + (255,)


PYRE_SKULL = ['.ooo.',
              'oEoEo',
              'ooNoo',
              'ooooo']


def pyre_skull(face, lx, ly, w, h, key):
    ch = PYRE_SKULL[min(ly, 3)][min(lx, 4)]
    if ch == '.':
        return clamp(CHAR, 6) + (255,)
    if ch == 'E':
        return (20, 10, 10, 255)
    if ch == 'N':
        return (52, 36, 32, 255)
    return clamp(PALETTE['bone'], int(noise('pyre_skull', lx, ly) * 12) - 10) + (255,)


def pyre_skull_side(face, lx, ly, w, h, key):
    n = noise('pyre_skull_side', key, face, lx, ly)
    if ly >= h - 1 and n < .7:
        return clamp(CHAR, int(n * 14)) + (255,)
    return clamp(PALETTE['bone'], int(n * 12) - 14) + (255,)


def pyre_embers(face, lx, ly, w, h, key):
    """The bed: crimson coals under a cracked black char crust, pink-white where they burn hottest."""
    n = noise('pyre_embers', key, face, lx, ly)
    if face != 'up':
        return (EMBER_DEEP if n > .6 else clamp(SOOT, int(n * 10))) + (255,)
    if noise('crust', lx // 2, ly // 2) < .32 and n < .7:
        return clamp(SOOT, int(n * 10)) + (255,)
    if n > .88:
        return EMBER_HEAT + (255,)
    return (EMBER_RED if n > .62 else EMBER if n > .22 else EMBER_DEEP) + (255,)


def pyre_coal(face, lx, ly, w, h, key):
    n = noise('pyre_coal', key, face, lx, ly)
    if face == 'up':
        return (EMBER_HEAT if n > .8 else EMBER_RED if n > .4 else EMBER) + (255,)
    return (EMBER_DEEP if n > .62 else clamp(SOOT, int(n * 12))) + (255,)


PYRE_SPECIAL = dict(blackstone=blackstone, charred=charred, charred_top=charred_top, log_end=log_end, char=char_core,
                    ash=ash, sinew=sinew, bone=pyre_bone_tex, skull=pyre_skull, skull_side=pyre_skull_side,
                    skull_top=pyre_bone_tex, embers=pyre_embers, coal=pyre_coal)


# --- the frost style (Velorum's Repose, the Cell Row) ----------------------------------------
#
# The same three-storey pylon re-cut as a frost pillar: a stepped deepslate-brick plinth under deepslate-tile caps,
# rime crusted on every ledge and draped down the faces, blue-ice shards breaking out of its top step; a shaft of
# packed ice veined with blue ice, girt by rimed deepslate collars hung with icicles; and a head of cold blue-white
# frost (full-bright) between four deepslate posts under a rimed crown, a frost star on its front and an ice spike for
# a finial. Ice spurs stand out of the neck at ring height for the pale `pulse_pylon_ring_frost` to ride on. No iron
# in the body, no copper or brass anywhere.

FROST_ATLAS = 'pulse_pylon_frost'
FROST_PARTICLE = 'minecraft:block/packed_ice'
DS_BRICK = (70, 70, 80)
DS_MORTAR = (32, 32, 40)
DS_TILE = (56, 56, 66)
RIME_WHITE = (230, 239, 250)
RIME_GREY = (178, 196, 222)
PACKED = (150, 182, 228)
PACKED_LIGHT = (196, 216, 244)
BLUE_ICE = (104, 146, 226)
HEART = (206, 230, 255)
HEART_LINE = (172, 208, 248)


def turned(el, k):
    for _ in range(k):
        el = turn(el)
    return el


def frost_plinth():
    e = [box('step1', (1, 0, 1), (15, 3, 15), 'ds_brick', up='rime'),
         box('step2', (2, 3, 2), (14, 5.5, 14), 'ds_brick'),
         box('step2_cap', (1.75, 5.5, 1.75), (14.25, 6, 14.25), 'ds_tile', up='rime'),
         box('step3', (3, 6, 3), (13, 8, 13), 'ds_brick'),
         box('step3_cap', (2.75, 8, 2.75), (13.25, 8.5, 13.25), 'ds_tile', up='rime')]
    # Rime drapes down each face of the steps, and icicles under the caps' overhangs; varied face to face.
    drapes = [[(3, 6, 2.5), (9, 11.5, 1.5)], [(4.5, 7, 2), (10, 12, 2.5)], [(2.5, 4.5, 1.5), (7.5, 10.5, 2.5)],
              [(5, 8.5, 2.5), (11, 12.5, 1.25)]]
    icicles = [[(4.5, 1.5), (7.25, 2.25), (11, 1)], [(3.75, 2), (9.5, 1.25), (12, 2)], [(5.5, 1), (8.75, 2.5)],
               [(4, 2.25), (6.5, 1), (10.25, 1.75)]]
    for k in range(4):
        side = []
        for a0, a1, drop in drapes[k]:
            side.append(box('step_drape', (a0, 6 - drop, 1.5), (a1, 6.25, 2), 'rime_drape', skip=('south',)))
        for x, length in icicles[k]:
            side.append(box('cap_icicle', (x, 5.5 - length, 1.25), (x + .75, 5.5, 2), 'ice', skip=('up', 'south')))
        side.append(box('upper_drape', (5 + k % 2, 8.5 - 1.5 - k % 3 * .5, 2.5), (8 + k % 2, 8.75, 3),
                        'rime_drape', skip=('south',)))
        e += [turned(el, k) for el in side]
    # Blue-ice shards broken out of the top step, leaning out from the shaft.
    for x, z, axis, angle, h in [(3.5, 5.5, 'z', 22.5, 4.5), (12.5, 4.25, 'z', -22.5, 3.5), (4.5, 12.5, 'x', 22.5, 3),
                                 (12.4, 11.25, 'z', -22.5, 5)]:
        e.append(box('shard', (x - .75, 8.5, z - .75), (x + .75, 8.5 + h, z + .75), 'blue_ice',
                     rotation=spin(axis, angle, (x, 8.5, z))))
        e.append(box('shard_tip', (x - .375, 8.5 + h, z - .375), (x + .375, 8.5 + h + 1.25, z + .375), 'ice',
                     skip=('down',), rotation=spin(axis, angle, (x, 8.5, z))))
    return e


def frost_shaft():
    e = [box('shaft', (4, 8.5, 4), (12, UPPER + 2.5, 12), 'packed_ice')]
    # Rimed deepslate collars at the cell joint and higher, each hung with icicles of its own.
    collars = [(15, [[(5, 2.5), (9.5, 1.5)], [(6.25, 1.75), (10.5, 3)], [(4.75, 1.25), (8, 2.75), (11, 1.5)],
                     [(7, 2.25)]]),
               (25.5, [[(6.5, 1.75), (10, 2.75)], [(4.5, 2.5), (8.5, 1.25)], [(9, 2)], [(5, 3), (10.25, 1.5)]])]
    for y, sides in collars:
        e.append(box('collar', (3.5, y, 3.5), (12.5, y + 1.5, 12.5), 'ds_tile', up='rime'))
        for k, hangs in enumerate(sides):
            side = []
            for x, length in hangs:
                side.append(box('collar_icicle', (x, y - length + .75, 3.25), (x + .75, y, 4), 'ice',
                                skip=('up', 'south')))
                side.append(box('collar_icicle_tip', (x + .1875, y - length, 3.4375),
                                (x + .5625, y - length + .75, 3.8125), 'ice', skip=('up',)))
            side.append(box('collar_rime', (4.5 + k, y + 1.5, 3.25), (8.5 + k, y + 1.875, 4.5), 'rime',
                            skip=('down',)))
            e += [turned(el, k) for el in side]
    # Rime ridges climbing the shaft's corners, broken where the collars cross them.
    for k, (y0, y1) in enumerate([(9, 13.5), (17.5, 23), (27.5, 31), (10.5, 14)]):
        e.append(turned(box('corner_rime', (3.75, y0, 3.75), (4.75, y1, 4.75), 'rime'), k))
    return e


def frost_head():
    u = UPPER
    e = [box('neck_collar', (4, u + 2.5, 4), (12, u + 3.5, 12), 'ds_tile', up='rime'),
         box('neck', (5, u + 3.5, 5), (11, u + 4, 11), 'ds_brick'),
         box('heart', (5, u + 4, 5), (11, u + 12, 11), 'heart', north='heart_star', extra=BRIGHT)]
    # Four deepslate posts frame the frost heart.
    for x, z in [(4.25, 4.25), (10.75, 4.25), (4.25, 10.75), (10.75, 10.75)]:
        e.append(box('head_post', (x, u + 3.75, z), (x + 1, u + 12.25, z + 1), 'ds_brick'))
    # Ice spurs at ring height for the charge ring to ride on, inside its inner radius.
    e.append(box('spur_n', (7.5, u + 5.5, 3), (8.5, u + 6.5, 5), 'blue_ice', skip=('south',)))
    e.append(box('spur_s', (7.5, u + 5.5, 11), (8.5, u + 6.5, 13), 'blue_ice', skip=('north',)))
    e.append(box('spur_w', (3, u + 5.5, 7.5), (5, u + 6.5, 8.5), 'blue_ice', skip=('east',)))
    e.append(box('spur_e', (11, u + 5.5, 7.5), (13, u + 6.5, 8.5), 'blue_ice', skip=('west',)))
    # Crown, rime cap and an ice spike for a finial; icicles under the crown's overhang.
    e += [box('crown', (4, u + 12, 4), (12, u + 13, 12), 'ds_tile', up='rime'),
          box('cap', (5, u + 13, 5), (11, u + 14, 11), 'ds_brick', up='rime'),
          box('finial', (7, u + 14, 7), (9, u + 15, 9), 'blue_ice', rotation=spin('y', 45, (8, u + 15, 8))),
          box('finial_tip', (7.5, u + 15, 7.5), (8.5, u + 16, 8.5), 'ice', skip=('down',),
              rotation=spin('y', 45, (8, u + 15, 8)))]
    for k, hangs in enumerate([[(5.75, 1.5), (9.25, 1)], [(6.5, 1.25)], [(5.5, 1), (9.75, 1.5)], [(8.5, 1.75)]]):
        for x, length in hangs:
            e.append(turned(box('crown_icicle', (x, u + 12 - length, 4.125), (x + .5, u + 12, 4.625), 'ice',
                                skip=('up',)), k))
    return e


def frost_body():
    return frost_plinth() + frost_shaft() + frost_head()


def ds_brick(face, lx, ly, w, h, key):
    """Deepslate bricks: dark grey-blue courses four texels deep, staggered joints, the top course frosted."""
    n = noise('ds_brick', key, face, lx, ly)
    if face in ('up', 'down'):
        return clamp(DS_TILE, int(n * 12) - 6) + (255,)
    if ly == 0 and h >= 2 and noise('fringe', key, face, lx) < .45:
        return clamp(RIME_GREY, -int(n * 20)) + (255,)
    if ly % 4 == 3 or (h >= 3 and lx % 8 == (0 if (ly // 4) % 2 == 0 else 4)):
        return clamp(DS_MORTAR, int(n * 8)) + (255,)
    return clamp(DS_BRICK, int(n * 14) - 7 + int(noise('brick', key, face, lx // 8, ly // 4) * 12) - 6) + (255,)


def ds_tile(face, lx, ly, w, h, key):
    n = noise('ds_tile', key, face, lx, ly)
    if face in ('up', 'down') and (lx % 4 == 3 or ly % 4 == 3):
        return clamp(DS_MORTAR, int(n * 8)) + (255,)
    return clamp(DS_TILE, int(n * 12) - 6 + (10 if ly == 0 and face not in ('up', 'down') else 0)) + (255,)


def frost_rime(face, lx, ly, w, h, key):
    """Hoar frost: white crystals with blue-grey shade between them."""
    n = noise('frost_rime', key, face, lx, ly)
    if n > .8:
        return (246, 250, 255, 255)
    return clamp(RIME_WHITE if n > .3 else RIME_GREY, int(n * 12) - 6) + (255,)


def frost_drape(face, lx, ly, w, h, key):
    """A drape of rime: thick white at the top, a ragged lower edge."""
    n = noise('frost_drape', key, face, lx, ly)
    if face in ('up', 'down'):
        return frost_rime(face, lx, ly, w, h, key)
    if ly >= h - 1 and noise('ragged', key, face, lx) < .5:
        return clamp(RIME_GREY, -30 + int(n * 10)) + (255,)
    return clamp(RIME_WHITE if ly < h // 2 or n > .4 else RIME_GREY, int(n * 12) - 6 - ly * 3) + (255,)


def packed_ice(face, lx, ly, w, h, key):
    """Packed ice: pale blue, cloudy, veined down its length with streaks of deeper blue ice and fine white cracks."""
    n = noise('packed', key, face, lx, ly)
    if face not in ('up', 'down'):
        if noise('vein', key, face, lx) < .16 and noise('vein_run', key, face, lx, ly // 6) < .85:
            return clamp(BLUE_ICE, int(n * 10) - 5) + (255,)
        if (lx + ly // 3) % 9 == 0 and noise('crack', key, face, lx, ly // 3) < .25:
            return clamp(PACKED_LIGHT, 20) + (255,)
    return clamp(PACKED if n < .85 else PACKED_LIGHT, int(n * 8) - 4) + (255,)


def frost_ice(face, lx, ly, w, h, key):
    """Icicle ice: clear pale blue, white at the drip."""
    n = noise('frost_ice', key, face, lx, ly)
    if face not in ('up', 'down') and ly == h - 1:
        return (236, 246, 255, 255)
    return clamp(PACKED_LIGHT, int(n * 16) - 10) + (255,)


def blue_ice(face, lx, ly, w, h, key):
    n = noise('blue_ice', key, face, lx, ly)
    if lx == 0 and face not in ('up', 'down'):
        return clamp(PACKED_LIGHT, int(n * 10)) + (255,)
    return clamp(BLUE_ICE, int(n * 16) - 8) + (255,)


def heart(face, lx, ly, w, h, key):
    """The frost heart: cold blue-white light broken into ice facets by fainter blue lines."""
    n = noise('heart', key, face, lx, ly)
    if (lx + ly) % 6 == 0 and noise('facet', key, face, (lx - ly) // 3) < .4:
        return clamp(HEART_LINE, int(n * 10)) + (255,)
    return (clamp(HEART, int(n * 12) - 4) if n < .9 else (250, 252, 255)) + (255,)


FROST_STAR = ['......',
              '..HH..',
              'H.HH.H',
              '.HCCH.',
              'HHWWHH',
              '.HCCH.',
              'H.HH.H',
              '..HH..']


def heart_star(face, lx, ly, w, h, key):
    """The front of the heart: a six-armed frost star, white-hot at its middle, on the faceted light."""
    row = FROST_STAR[min(ly, len(FROST_STAR) - 1)]
    ch = row[min(max(lx, 0), len(row) - 1)] if lx < len(row) else '.'
    if ch == 'W':
        return (255, 255, 255, 255)
    if ch == 'C':
        return (236, 246, 255, 255)
    if ch == 'H':
        return (104, 156, 232, 255)
    return heart(face, lx, ly, w, h, key)


def frost_ring(face, lx, ly, w, h, key):
    """The frost ring's body: pale rimed ice, whiter on its upper face."""
    n = noise('frost_ring', key, face, lx, ly)
    return clamp(RIME_GREY if face != 'up' else RIME_WHITE, int(n * 14) - 7) + (255,)


def frost_inlay(face, lx, ly, w, h, key):
    n = noise('frost_inlay', key, face, lx, ly)
    return ((255, 255, 255) if n > .82 else clamp((206, 232, 255), int(n * 16) - 8)) + (255,)


FROST_SPECIAL = dict(ds_brick=ds_brick, ds_tile=ds_tile, rime=frost_rime, rime_drape=frost_drape,
                     packed_ice=packed_ice, ice=frost_ice, blue_ice=blue_ice, heart=heart, heart_star=heart_star,
                     ferric=frost_ring, inlay=frost_inlay)


def split_three(elements):
    """Cut the 48-pixel piece at y = 16 and y = 32 into the lower, middle and upper cells."""
    lower, rest = split_storeys(elements, 16)
    middle, upper = split_storeys(rest, 16)
    return lower, middle, upper


def bounds(elements):
    lo = [round(min(el['from'][a] for el in elements), 3) for a in range(3)]
    hi = [round(max(el['to'][a] for el in elements), 3) for a in range(3)]
    return lo, hi


STYLES = {'venous': '', 'anchor': '_anchor', 'pyre': '_pyre', 'frost': '_frost'}      # style -> model name suffix


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for part in ('lower', 'middle', 'upper'):
            for style, suffix in STYLES.items():
                variants[f'facing={facing},part={part},style={style}'] = dict(
                    {'model': f'hemomancy:block/pulse_pylon{suffix}_{part}'}, **({'y': y} if y else {}))
    return {'variants': variants}


def export(body, special, atlas_name, particle, suffix, title, with_ring=True):
    """Cut a 48-pixel pylon into its three cells, paint its atlas with its ring (when it has one), write the models
    and project."""
    moved = separate(body)
    halo = ring() if with_ring else []
    moved += separate(halo)
    lower, middle, upper = split_three(body)
    groups = {'lower': lower, 'middle': middle, 'upper': upper}
    if with_ring:
        groups['ring'] = halo
    atlas, models = build(groups, special)
    atlas.save(ASSETS / f'textures/block/{atlas_name}.png')
    h = atlas.height
    outputs = {f'block/pulse_pylon{suffix}_{part}': model_json(models[part], atlas_name, h, particle)
               for part in ('lower', 'middle', 'upper')}
    stacked = [(models['lower'], 0), (models['middle'], 16), (models['upper'], 32)]
    if with_ring:
        outputs[f'block/pulse_pylon_ring{suffix}'] = model_json(models['ring'], atlas_name, h, particle)
        stacked.append((models['ring'], 32))
    joined = model_json(item_elements(*stacked), atlas_name, h, particle)
    write(outputs, f'pulse_pylon{suffix}', joined)
    print(f'Exported {title}: {len(lower)} lower + {len(middle)} middle + {len(upper)} upper cubes, '
          f'{len(halo)} ring cubes, {moved} faces separated, 128x{h} atlas')
    for part in groups:
        print(f'  {part}: bounds {bounds(models[part])}')
    return joined, outputs.get(f'block/pulse_pylon_ring{suffix}')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    joined, halo = export(plinth() + shaft() + head(), SPECIAL, ATLAS, PARTICLE, '', 'Pulse Pylon')
    anchor_joined, anchor_halo = export(anchor_body(), ANCHOR_SPECIAL, ANCHOR_ATLAS, ANCHOR_PARTICLE, '_anchor',
                                        'Pulse Pylon (anchor)')
    pyre_joined, _ = export(pyre_body(), PYRE_SPECIAL, PYRE_ATLAS, PYRE_PARTICLE, '_pyre', 'Pulse Pylon (pyre)',
                            with_ring=False)
    frost_joined, frost_halo = export(frost_body(), FROST_SPECIAL, FROST_ATLAS, FROST_PARTICLE, '_frost',
                                      'Pulse Pylon (frost)')
    (ASSETS / 'blockstates/pulse_pylon.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    print(f'PYRE_BED = {PYRE_BED:g} px (upper cell): the pyre style\'s fire root, about (8, 8)')
    print(f'RING_Y = {RING_Y:g} px (upper cell) for every ringed style, radius {RING_OUTER:g} outer / {RING_INNER:g} inner, '
          f'about (8, 8)')
    if args.preview:
        preview(joined['elements'], ATLAS, 'Pulse Pylon - lower + middle + upper + charge ring, actual atlas',
                'pulse_pylon', lift=16, size=400, scale=7)
        preview(halo['elements'], ATLAS,
                f'Pulse Pylon ring - BER model, upper-cell space, centre y={RING_Y:g}', 'pulse_pylon_ring')
        preview(anchor_joined['elements'], ANCHOR_ATLAS,
                'Pulse Pylon, anchor style - lower + middle + upper + charge ring', 'pulse_pylon_anchor', lift=16,
                size=400, scale=7)
        preview(anchor_halo['elements'], ANCHOR_ATLAS,
                f'Pulse Pylon anchor ring - BER model, upper-cell space, centre y={RING_Y:g}',
                'pulse_pylon_ring_anchor')
        preview(pyre_joined['elements'], PYRE_ATLAS,
                'Pulse Pylon, pyre style - lower + middle + upper (the fire is drawn by the BER)', 'pulse_pylon_pyre',
                lift=16, size=400, scale=7)
        preview(frost_joined['elements'], FROST_ATLAS,
                'Pulse Pylon, frost style - lower + middle + upper + charge ring', 'pulse_pylon_frost', lift=16,
                size=400, scale=7)
        preview(frost_halo['elements'], FROST_ATLAS,
                f'Pulse Pylon frost ring - BER model, upper-cell space, centre y={RING_Y:g}', 'pulse_pylon_ring_frost')
        preview_pair((joined['elements'], ATLAS), (anchor_joined['elements'], ANCHOR_ATLAS),
                     'Pulse Pylon - venous (top) vs anchor (bottom), rings at rest', 'pulse_pylon_compare', lift=16,
                     size=360, scale=6.5)


if __name__ == '__main__':
    main()

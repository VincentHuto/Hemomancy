"""Author the Crimson Troupe's set-piece blocks (chunk 15 T7, NQ-15-1 A): the kit the big top, the backyard, the
midway, the five teaching camps and the wrecked stage wagon are built from (tools/worldgen/circus_sites.py).

Materials are the Troupe's: blood-wood, hematic iron, bone, Crimson Lacquer, canvas and crimson thread. No brass,
bronze, copper or gold anywhere: copper and its alloys are Unstained metals, and gilt is not the Troupe's.
Everything is 16 px per block. Each piece is authored facing north (its front toward -z, its back on the south side
of its cell) and turned by its blockstate.

Pieces:
- big top: king pole and quarter pole (foot, shaft, collar and finial sections), ring curb and its bend, seating
  tier, gallery rail, trapeze, safety net, scalloped valance, guy stake, the Ringmaster's podium (two tall), crimson
  footlight, hanging Crossbar rig, Spindle rack, bone-and-thread trophy, pelt drape, hub plinth;
- backyard and midway: wagon wheel (two blocks across, four quarters), wagon panel, wrecked panel, cook pot, costume
  rack, trunk (it does not open), ticket window, pennant line;
- camps: knife wheel (two tall), target board, stilt rack (two tall), weight frame, fire pit, bellows stand, hound
  hoop, prompt-book table;
- canvas walls: striped canvas, laced canvas, wainscot, sawdust;
- the four evidence records, each its own object: a stage bill, a loft rigging log, a quarters ledger, a carousel
  maker's plate.

Re-run from the repository root: python tools/model_export/circus_set_pieces.py [--preview]
`--preview` writes build/model-previews/circus_<piece>.png for every modelled piece.
"""
import argparse
import json
import math

from PIL import Image

from clairaudiograph import clamp, noise
from court_kit import BRIGHT, facing_variants
from court_outpost_kit import export
from fixture_atlas import ASSETS, box, item_elements, model_json, preview, spin, split_storeys, write_tall_item

CRIMSON = (150, 22, 34)
CRIMSON_LO = (112, 14, 26)
CANVAS = (214, 198, 170)
CANVAS_LO = (184, 166, 138)
THREAD = (176, 24, 38)
THREAD_LO = (124, 14, 28)
PELT = (78, 46, 40)
PELT_LO = (52, 30, 28)
PAPER = (228, 214, 186)
INK = (58, 30, 32)
EMBER = (255, 104, 60)
EMBER_HI = (255, 176, 120)
EMBER_LO = (190, 34, 34)
GLOW = (255, 72, 76)
STONE = (78, 62, 64)
STONE_LO = (58, 44, 48)
ASH = (64, 58, 60)
SAWDUST = (176, 138, 96)
VELVET = (64, 12, 24)
CHAR = (34, 24, 26)


def canvas(face, lx, ly, w, h, key):
    """Big-top canvas: crimson and bone stripes four texels wide, a fine weave, grime toward the hem."""
    n = noise('canvas', key, face, lx, ly)
    stripe = CRIMSON if (lx // 4) % 2 == 0 else CANVAS
    weave = 6 if (lx + ly) % 2 == 0 else -4
    return clamp(stripe, weave + int(n * 8) - 4 - (10 if ly == h - 1 and h > 3 else 0)) + (255,)


def canvas_plain(face, lx, ly, w, h, key):
    n = noise('cplain', key, face, lx, ly)
    return clamp(CANVAS if n > .2 else CANVAS_LO, 4 if (lx + ly) % 2 == 0 else -4) + (255,)


def thread(face, lx, ly, w, h, key):
    """Crimson troupe thread: twisted, a darker turn every other texel along its length."""
    along = ly if h >= w else lx
    n = noise('thread', key, face, lx, ly)
    return clamp(THREAD if (along + lx + ly) % 3 else THREAD_LO, int(n * 10) - 5) + (255,)


def pelt(face, lx, ly, w, h, key):
    n = noise('pelt', key, face, lx, ly)
    streak = noise('streak', key, face, lx) > .6
    return clamp(PELT if streak else PELT_LO, int(n * 14) - 7) + (255,)


def paper(face, lx, ly, w, h, key):
    """Bone-tinted paper; a broad face carries ruled ink lines."""
    n = noise('paper', key, face, lx, ly)
    if w >= 4 and h >= 4 and face in ('north', 'up') and ly % 2 == 1 and 0 < lx < w - 1 \
            and noise('word', key, lx // 2, ly) > .3:
        return INK + (255,)
    return clamp(PAPER, int(n * 10) - 5) + (255,)


def bill(face, lx, ly, w, h, key):
    """The stage bill: a crimson masthead, a bone mask in the middle, ruled lines under it."""
    if face != 'north' or w < 6:
        return paper(face, lx, ly, w, h, key)
    if ly < 2:
        return clamp(CRIMSON, int(noise('mast', lx) * 10)) + (255,)
    cx, cy = w / 2 - .5, h * .4
    if abs(lx - cx) <= 1.5 and abs(ly - cy) <= 1.5:
        return ((34, 18, 22) if (abs(lx - cx) < .6 and ly == int(cy)) else CANVAS) + (255,)
    if ly > cy + 2 and ly % 2 == 0 and 0 < lx < w - 1 and noise('bword', lx // 2, ly) > .25:
        return INK + (255,)
    return clamp(PAPER, int(noise('bill', lx, ly) * 10) - 5) + (255,)


def ledger(face, lx, ly, w, h, key):
    """A ledger's open page: two ruled columns of figures."""
    if face != 'up' or w < 4:
        return paper(face, lx, ly, w, h, key)
    if lx == w // 2:
        return (176, 40, 44, 255)
    if ly % 2 == 0 and lx % (w // 2) not in (0,) and noise('fig', key, lx, ly) > .35:
        return INK + (255,)
    return clamp(PAPER, int(noise('ledger', key, lx, ly) * 8) - 4) + (255,)


def ember(face, lx, ly, w, h, key):
    n = noise('ember', key, face, lx, ly)
    return (EMBER_HI if n > .75 else EMBER if n > .3 else EMBER_LO) + (255,)


def glow(face, lx, ly, w, h, key):
    n = noise('glow', key, face, lx, ly)
    return clamp(GLOW, 20 if ly == 0 else int(n * 16) - 8) + (255,)


def stone(face, lx, ly, w, h, key):
    """Venous stone for hearth rings: grey-red, a darker vein, worn pale on top."""
    n = noise('stone', key, face, lx, ly)
    rgb = STONE_LO if noise('svein', key, face, (lx + ly) // 2) > .85 else STONE
    return clamp(rgb, int(n * 12) - 6 + (14 if face == 'up' else 0)) + (255,)


def ash(face, lx, ly, w, h, key):
    n = noise('ash', key, face, lx, ly)
    if n > .93:
        return EMBER_LO + (255,)
    return clamp(ASH, int(n * 16) - 8) + (255,)


def char(face, lx, ly, w, h, key):
    """A charred log: black, cracked, an ember glint in the cracks."""
    n = noise('char', key, face, lx, ly)
    if noise('crack', key, face, lx, ly // 2) > .9:
        return EMBER_LO + (255,)
    return clamp(CHAR, int(n * 12) - 4) + (255,)


def velvet(face, lx, ly, w, h, key):
    n = noise('velvet', key, face, lx, ly)
    return clamp(VELVET, int(n * 10) - 5 + (12 if ly == 0 else 0)) + (255,)


def sawdust(face, lx, ly, w, h, key):
    n = noise('dust', key, face, lx, ly)
    return clamp(SAWDUST, int(n * 24) - 12) + (255,)


SPECIAL = dict(canvas=canvas, canvas_plain=canvas_plain, thread=thread, pelt=pelt, paper=paper, bill=bill,
               ledger=ledger, ember=ember, glow=glow, stone=stone, ash=ash, char=char, velvet=velvet,
               sawdust=sawdust)

PIECES = {}


def piece(name, title, kind='facing'):
    """Registers a modelled piece. `kind`: facing (one model, four facings), plain (one model, no state), tall (two
    storeys, facing x half), pole (four sections), wheel (four quarters, facing), octagonal (a straight and a
    diagonal model, eight rotations a 45 degree step apart)."""
    def register(fn):
        PIECES[name] = (fn, title, kind)
        return fn
    return register


def ring(lo_x, lo_z, hi_x, hi_z, y0, y1, mat, t=1.0, name='ring'):
    """A square frame of four boxes one wall thick."""
    return [box(f'{name}_n', (lo_x, y0, lo_z), (hi_x, y1, lo_z + t), mat),
            box(f'{name}_s', (lo_x, y0, hi_z - t), (hi_x, y1, hi_z), mat),
            box(f'{name}_w', (lo_x, y0, lo_z + t), (lo_x + t, y1, hi_z - t), mat),
            box(f'{name}_e', (hi_x - t, y0, lo_z + t), (hi_x, y1, hi_z - t), mat)]


def tangent_box(name, cx, cy, z0, z1, length, depth, theta, mat):
    """A box in the x-y plane centred at (cx, cy) whose long side runs at `theta` degrees; the angle is folded into
    the -45..45 a block model allows by swapping the box's long axis."""
    t = ((theta + 45) % 180) - 45
    long_x = True
    if t > 45:
        t -= 90
        long_x = False
    hx, hy = (length / 2, depth / 2) if long_x else (depth / 2, length / 2)
    rotation = spin('z', t, (cx, cy, (z0 + z1) / 2)) if abs(t) > 1e-6 else None
    return box(name, (cx - hx, cy - hy, z0), (cx + hx, cy + hy, z1), mat, rotation=rotation)


# --- the big top ----------------------------------------------------------------------------------------------------

def pole(half, section):
    """One section of a pole `half` pixels across from its centre."""
    a, b = 8 - half, 8 + half
    p = min(8.0, half + 2)
    pa, pb = 8 - p, 8 + p
    if section == 'foot':
        e = [box('shoe', (pa, 0, pa), (pb, 4, pb), 'iron'),
             box('shoe_lip', (pa - .5, 0, pa - .5), (pb + .5, 1, pb + .5), 'ferric'),
             box('shaft', (a, 4, a), (b, 16, b), 'wood')]
        for i, (x0, z0, x1, z1) in enumerate(((7, pa - 1.5, 9, pa), (7, pb, 9, pb + 1.5),
                                              (pa - 1.5, 7, pa, 9), (pb, 7, pb + 1.5, 9))):
            e.append(box(f'wedge_{i}', (x0, 0, z0), (x1, 3, z1), 'bone'))
        e.append(box('band', (a - .25, 10, a - .25), (b + .25, 11, b + .25), 'iron'))
        return e
    if section == 'shaft':
        return [box('shaft', (a, 0, a), (b, 16, b), 'wood'),
                box('stripe_lo', (a - .25, 3, a - .25), (b + .25, 4, b + .25), 'lacquer'),
                box('band', (a - .25, 7.5, a - .25), (b + .25, 8.5, b + .25), 'iron'),
                box('stripe_hi', (a - .25, 12, a - .25), (b + .25, 13, b + .25), 'lacquer')]
    if section == 'collar':
        e = [box('shaft', (a, 0, a), (b, 16, b), 'wood'),
             box('sleeve', (pa, 4, pa), (pb, 12, pb), 'iron'),
             box('lace_ring', (pa - .25, 7.5, pa - .25), (pb + .25, 8.5, pb + .25), 'thread'),
             box('lip_lo', (pa - .5, 3.5, pa - .5), (pb + .5, 4.5, pb + .5), 'ferric'),
             box('lip_hi', (pa - .5, 11.5, pa - .5), (pb + .5, 12.5, pb + .5), 'ferric')]
        for i, (x0, z0, x1, z1) in enumerate(((7.25, pa - 2, 8.75, pa - .5), (7.25, pb + .5, 8.75, pb + 2),
                                              (pa - 2, 7.25, pa - .5, 8.75), (pb + .5, 7.25, pb + 2, 8.75))):
            e.append(box(f'hook_{i}', (x0, 9, z0), (x1, 10.5, z1), 'bone'))
        return e
    k = max(1.0, half - 1.5)
    return [box('shaft', (a, 0, a), (b, 6, b), 'wood'),
            box('cap', (a - .5, 6, a - .5), (b + .5, 7.5, b + .5), 'iron'),
            box('knob_lo', (8 - k, 7.5, 8 - k), (8 + k, 10, 8 + k), 'bone'),
            box('knob', (8 - k - .75, 10, 8 - k - .75), (8 + k + .75, 12, 8 + k + .75), 'bone'),
            box('knob_tip', (8 - k + .5, 12, 8 - k + .5), (8 + k - .5, 14, 8 + k - .5), 'bone'),
            box('tassel', (7.5, 4, a - 1), (8.5, 7, a - .25), 'thread')]


@piece('circus_king_pole', 'Circus King Pole', 'pole')
def king_pole():
    return {s: pole(5, s) for s in ('foot', 'shaft', 'collar', 'finial')}


@piece('circus_quarter_pole', 'Circus Quarter Pole', 'pole')
def quarter_pole():
    return {s: pole(3, s) for s in ('foot', 'shaft', 'collar', 'finial')}


@piece('circus_ring_curb', 'Ring Curb', 'octagonal')
def ring_curb():
    """Two models: the straight curb, and the same curb cut on the diagonal where the ring steps from one row to the
    next (turned 45 degrees about y, so its panelled face is north-west)."""
    straight = [box('sill', (0, 0, 5), (16, 1, 11), 'iron'),
                box('kerb', (0, 1, 5), (16, 5, 11), 'lacquer'),
                box('inset', (1, 1.5, 4.5), (15, 4.5, 5), 'panel'),
                box('cap', (0, 5, 4.5), (16, 6.5, 11.5), 'bone')]
    for i, x in enumerate((3.5, 11.5)):
        straight.append(box(f'strap_{i}', (x, 0, 4.25), (x + 1, 6.75, 11.75), 'iron'))
    straight.append(box('stud', (7.25, 6.5, 7.25), (8.75, 7, 8.75), 'ferric'))
    r = spin('y', 45, (8, 0, 8))
    diagonal = [box('sill', (-3, 0, 5), (19, 1, 11), 'iron', rotation=r),
                box('kerb', (-3, 1, 5), (19, 5, 11), 'lacquer', rotation=r),
                box('inset', (-1, 1.5, 4.5), (17, 4.5, 5), 'panel', rotation=r),
                box('cap', (-3, 5, 4.5), (19, 6.5, 11.5), 'bone', rotation=r),
                box('strap', (7.5, 0, 4.25), (8.5, 6.75, 11.75), 'iron', rotation=r)]
    return {'body': straight, 'diagonal': diagonal}


@piece('circus_seating', 'Seating Tier')
def seating():
    e = [box('seat', (0, 8, 1.5), (16, 9.5, 11), 'wood'),
         box('nosing', (0, 7.75, .5), (16, 9.75, 1.5), 'bone'),
         box('footboard', (0, 2, 3.5), (16, 3, 11), 'wood'),
         box('riser', (0, 0, 13.5), (16, 16, 15.5), 'panel'),
         box('brace', (2.5, 4, 2.25), (13.5, 5, 3.25), 'iron')]
    for i, (x, z, top) in enumerate(((1, 2, 8), (13.5, 2, 8), (1, 12, 16), (13.5, 12, 16))):
        e.append(box(f'leg_{i}', (x, 0, z), (x + 1.5, top, z + 1.5), 'iron'))
    return e


@piece('circus_gallery_rail', 'Gallery Rail')
def gallery_rail():
    e = [box('handrail', (0, 14, 6.5), (16, 16, 9.5), 'lacquer'),
         box('rail_lo', (0, 0, 7), (16, 2, 9), 'iron')]
    for i, x in enumerate((1.25, 5.25, 9.25, 13.25)):
        e += [box(f'baluster_{i}', (x, 2, 7.25), (x + 1.5, 14, 8.75), 'bone'),
              box(f'collar_{i}', (x - .25, 6.5, 7), (x + 1.75, 8, 9), 'bone')]
    return e


@piece('circus_trapeze', 'Trapeze')
def trapeze():
    return [box('line_w', (2.5, 4, 7.5), (3.5, 16, 8.5), 'thread'),
            box('line_e', (12.5, 4, 7.5), (13.5, 16, 8.5), 'thread'),
            box('bar', (1.5, 3, 7.25), (14.5, 4.25, 8.75), 'iron'),
            box('grip_w', (4.5, 2.75, 7), (6.5, 4.5, 9), 'bone'),
            box('grip_e', (9.5, 2.75, 7), (11.5, 4.5, 9), 'bone'),
            box('knot_w', (2.25, 4.25, 7.25), (3.75, 5.5, 8.75), 'thread'),
            box('knot_e', (12.25, 4.25, 7.25), (13.75, 5.5, 8.75), 'thread')]


@piece('circus_net', 'Rigging Net', 'net')
def net():
    """The sheet, an iron rim for a side with no net beside it, and a thread lashing for a side tied off on a pole
    (each authored on the north side; the blockstate turns them)."""
    return {'body': net_sheet(),
            # The rim stands a hair proud of the block edge so the cord ends inside it never share its face, and the
            # lashing stops at the rim's face, a little thinner, so the two never share a plane.
            'rim': [box('rim', (0, .5, -.1), (16, 2.5, 1.5), 'iron')],
            'tie': [box('lash', (6.5, 1.1, -7), (9.5, 2.4, -.1), 'thread'),
                    box('wrap', (5.5, .5, -2.5), (10.5, 3, -1), 'thread')]}


def net_sheet():
    e = []
    for i in range(4):
        c = 2 + i * 4
        e.append(box(f'cord_x{i}', (0, 1.5, c - .5), (16, 2, c + .5), 'thread'))
        e.append(box(f'cord_z{i}', (c - .5, 1, 0), (c + .5, 1.5, 16), 'thread'))
        for j in range(4):
            d = 2 + j * 4
            e.append(box(f'knot_{i}_{j}', (c - .75, .75, d - .75), (c + .75, 2.25, d + .75), 'bone'))
    return e


@piece('circus_valance', 'Scalloped Valance')
def valance():
    e = [box('rail', (0, 15, 12.5), (16, 16, 15.5), 'iron'),
         box('band', (0, 10, 13), (16, 15, 15), 'canvas')]
    for i in range(3):
        x0 = i * 16 / 3
        e += [box(f'tongue_{i}', (x0 + .25, 7, 13.25), (x0 + 5.08, 10, 14.75), 'canvas_plain'),
              box(f'tip_{i}', (x0 + 1.25, 5, 13.25), (x0 + 4.08, 7, 14.75), 'canvas_plain'),
              box(f'point_{i}', (x0 + 2.17, 4, 13.25), (x0 + 3.17, 5, 14.75), 'canvas_plain'),
              box(f'tassel_{i}', (x0 + 2.42, 1.5, 13.75), (x0 + 2.92, 4, 14.25), 'thread'),
              box(f'bead_{i}', (x0 + 2.17, 1, 13.5), (x0 + 3.17, 2, 14.5), 'bone')]
    e.append(box('piping', (0, 9.5, 12.75), (16, 10, 15.25), 'thread'))
    return e


@piece('circus_guy_stake', 'Guy Stake')
def guy_stake():
    lean = spin('x', 22.5, (8, 0, 8))
    return [box('stake', (7, 0, 7), (9, 10, 9), 'bone', rotation=lean),
            box('cap', (6.5, 9, 6.5), (9.5, 10.5, 9.5), 'iron', rotation=lean),
            box('collar', (6.75, 2, 6.75), (9.25, 3, 9.25), 'iron', rotation=lean),
            box('rope', (7.5, 9, 2.5), (8.5, 10, 12), 'thread', rotation=spin('x', 22.5, (8, 9.5, 11.8))),
            box('toggle', (6.5, 8.75, 5), (9.5, 10.25, 6.5), 'bone', rotation=spin('x', 22.5, (8, 9.5, 11.8))),
            box('peg_ring', (5.5, 0, 5.5), (10.5, .5, 10.5), 'ferric')]


@piece('circus_podium', "Ringmaster's Podium", 'tall')
def podium():
    e = [box('plinth', (2, 0, 2), (14, 2, 14), 'iron'),
         box('drum', (3, 2, 3), (13, 13, 13), 'lacquer'),
         box('cap', (2.5, 13, 2.5), (13.5, 14.5, 13.5), 'ferric'),
         box('mask', (6, 6, 2.5), (10, 10, 3), 'bone'),
         box('mask_brow', (5.5, 9.5, 2.25), (10.5, 10.5, 2.75), 'bone'),
         box('post', (6, 14.5, 7), (10, 18, 11), 'wood'),
         box('desk', (2.5, 18, 3), (13.5, 19.5, 13), 'wood', rotation=spin('x', -22.5, (8, 18.75, 8))),
         box('page', (4, 19.5, 4.5), (12, 19.75, 11.5), 'paper', rotation=spin('x', -22.5, (8, 18.75, 8))),
         box('ledge', (2.5, 17, 1.5), (13.5, 18, 3), 'bone', rotation=spin('x', -22.5, (8, 18.75, 8)))]
    for i, (x, z) in enumerate(((2.75, 6), (2.75, 10), (12.75, 6), (12.75, 10), (6, 12.75), (10, 12.75))):
        e.append(box(f'rib_{i}', (x, 2, z), (x + .5, 13, z + .5), 'bone'))
    for i, x in enumerate((4.5, 11)):
        e.append(box(f'rib_front_{i}', (x, 2, 2.75), (x + .5, 13, 3), 'bone'))
    return e


@piece('circus_footlight', 'Crimson Footlight')
def footlight():
    return [box('base', (2, 0, 8), (14, 2, 15), 'iron'),
            box('hood_back', (2, 2, 13), (14, 6, 15), 'ferric'),
            box('hood_top', (2, 5, 9), (14, 6.5, 15), 'ferric'),
            box('reflector', (2.5, 2, 12.5), (13.5, 5, 13), 'lacquer'),
            box('lens', (3, 2, 9), (13, 5, 12.5), 'glow', extra=BRIGHT),
            box('lip', (2, 2, 8.5), (14, 2.5, 9), 'iron'),
            box('cheek_w', (1.5, 0, 8), (2.5, 6.5, 15), 'bone'),
            box('cheek_e', (13.5, 0, 8), (14.5, 6.5, 15), 'bone')]


@piece('circus_crossbar_rig', 'Hanging Crossbar Rig')
def crossbar_rig():
    e = [box('hook', (7.5, 13, 7.5), (8.5, 16, 8.5), 'iron'),
         box('ring', (7, 12.5, 7), (9, 13, 9), 'ferric'),
         box('bar', (2, 11, 7.25), (14, 12.5, 8.75), 'wood'),
         box('cross', (7.25, 11.25, 3), (8.75, 12.25, 13), 'lacquer'),
         box('cap_w', (1.5, 10.75, 7), (2.5, 12.75, 9), 'bone'),
         box('cap_e', (13.5, 10.75, 7), (14.5, 12.75, 9), 'bone'),
         box('cap_n', (7, 10.75, 2.5), (9, 12.75, 3.5), 'bone'),
         box('cap_s', (7, 10.75, 12.5), (9, 12.75, 13.5), 'bone')]
    for i, (x, z, low) in enumerate(((2.75, 7.75, 3), (12.75, 7.75, 4.5), (7.75, 3.25, 5.5), (7.75, 12.25, 2))):
        e += [box(f'string_{i}', (x, low, z), (x + .5, 11, z + .5), 'thread'),
              box(f'toggle_{i}', (x - .5, low - 1, z - .5), (x + 1, low, z + 1), 'bone')]
    return e


@piece('circus_spindle_rack', 'Spindle Rack')
def spindle_rack():
    e = [box('board', (1, 3, 14.5), (15, 14, 16), 'wood'),
         box('shelf', (1, 3, 10.5), (15, 4.5, 16), 'lacquer'),
         box('top_rail', (1, 13, 13), (15, 14, 14.5), 'lacquer'),
         box('finial_w', (1, 14, 13.5), (2.5, 15.5, 15), 'bone'),
         box('finial_e', (13.5, 14, 13.5), (15, 15.5, 15), 'bone'),
         box('bracket_w', (2, 1.5, 13), (3, 3, 16), 'iron'),
         box('bracket_e', (13, 1.5, 13), (14, 3, 16), 'iron')]
    for i, x in enumerate((2, 6.5, 11)):
        e += [box(f'base_{i}', (x, 4.5, 11.5), (x + 3, 5.5, 14.5), 'bone'),
              box(f'wound_{i}', (x + .5, 5.5, 12), (x + 2.5, 10.5, 14), 'thread'),
              box(f'top_{i}', (x, 10.5, 11.5), (x + 3, 11.5, 14.5), 'bone'),
              box(f'pin_{i}', (x + 1.25, 11.5, 12.75), (x + 1.75, 13, 13.25), 'iron')]
    return e


@piece('circus_trophy', 'Bone and Thread Trophy')
def trophy():
    return [box('plaque', (3, 4, 15), (13, 14, 16), 'lacquer'),
            box('plaque_rim', (2.5, 3.5, 15.25), (13.5, 14.5, 15.75), 'bone'),
            box('bone_a', (7.25, 1.5, 13.5), (8.75, 16.5, 15), 'bone', rotation=spin('z', 45, (8, 9, 14.25))),
            box('bone_b', (7.25, 1.5, 13.5), (8.75, 16.5, 15), 'bone', rotation=spin('z', -45, (8, 9, 14.25))),
            box('knot', (6, 7, 12.5), (10, 11, 15), 'bone'),
            box('brow', (5.5, 10, 12.25), (10.5, 11, 13), 'bone'),
            box('wrap', (5.75, 8.25, 12.25), (10.25, 9.25, 15.25), 'thread'),
            box('tassel_w', (4, 2, 14), (4.75, 7, 14.75), 'thread'),
            box('tassel_e', (11.25, 2, 14), (12, 7, 14.75), 'thread'),
            box('drop_w', (3.75, 1.25, 13.75), (5, 2.25, 15), 'bone'),
            box('drop_e', (11, 1.25, 13.75), (12.25, 2.25, 15), 'bone')]


@piece('circus_pelt_drape', 'Pelt Drape')
def pelt_drape():
    e = [box('rail', (0, 13, 11), (16, 14, 12), 'iron'),
         box('bracket_w', (1, 12, 12), (2, 15, 16), 'iron'),
         box('bracket_e', (14, 12, 12), (15, 15, 16), 'iron'),
         box('front', (1.5, 3, 10), (14.5, 14, 11), 'pelt'),
         box('back', (2, 6, 12), (14, 14, 13), 'pelt'),
         box('fold', (1.5, 14, 10), (14.5, 15, 13), 'pelt')]
    for i, (x, low) in enumerate(((1.5, 1.5), (4.5, 2.5), (8, 1), (11.5, 2), (13, 1.5))):
        e.append(box(f'rag_{i}', (x, low, 10.25), (x + 1.5, 3, 10.75), 'pelt'))
    e += [box('claw_w', (1.25, .5, 9.75), (2.25, 1.5, 11), 'bone'),
          box('claw_e', (13.75, .5, 9.75), (14.75, 1.5, 11), 'bone')]
    return e


@piece('circus_plinth', 'Hub Plinth', 'plain')
def plinth():
    e = [box('base', (0, 0, 0), (16, 3, 16), 'stone'),
         box('drum', (1, 3, 1), (15, 12, 15), 'lacquer'),
         box('cap', (0, 12, 0), (16, 16, 16), 'stone'),
         box('band', (.75, 6.5, .75), (15.25, 8.5, 15.25), 'bone')]
    for i, (x, z) in enumerate(((.5, .5), (14, .5), (.5, 14), (14, 14))):
        e.append(box(f'post_{i}', (x, 3, z), (x + 1.5, 12, z + 1.5), 'iron'))
    return e


# --- backyard and midway --------------------------------------------------------------------------------------------

def wheel_parts():
    """The whole wheel in a 32 x 32 frame (hub at 16, 16), split into its four quarter cells."""
    parts = []
    for k in range(16):
        phi = math.radians(k * 22.5)
        cx, cy = 16 + 13.25 * math.cos(phi), 16 + 13.25 * math.sin(phi)
        parts.append(tangent_box(f'felloe_{k}', cx, cy, 6.5, 9.5, 5.6, 2.5, math.degrees(phi) + 90, 'lacquer'))
        tx, ty = 16 + 15 * math.cos(phi), 16 + 15 * math.sin(phi)
        parts.append(tangent_box(f'tyre_{k}', tx, ty, 6, 10, 6, 1, math.degrees(phi) + 90, 'iron'))
    for k in range(8):
        phi = math.radians(k * 45 + 22.5)
        cx, cy = 16 + 8.25 * math.cos(phi), 16 + 8.25 * math.sin(phi)
        parts.append(tangent_box(f'spoke_{k}', cx, cy, 7.25, 8.75, 8.5, 1.5, math.degrees(phi), 'wood'))
    for q, (x0, y0) in enumerate(((12, 12), (16, 12), (12, 16), (16, 16))):
        parts += [box(f'hub_{q}', (x0, y0, 5), (x0 + 4, y0 + 4, 11), 'iron'),
                  box(f'cap_{q}', (x0 + (1 if x0 == 12 else 0), y0 + (1 if y0 == 12 else 0), 4.25),
                      (x0 + (4 if x0 == 12 else 3), y0 + (4 if y0 == 12 else 3), 5), 'bone'),
                  box(f'axle_{q}', (x0 + (2.5 if x0 == 12 else 0), y0 + (2.5 if y0 == 12 else 0), 11),
                      (x0 + (4 if x0 == 12 else 1.5), y0 + (4 if y0 == 12 else 1.5), 16), 'iron')]
    quarters = {q: [] for q in range(4)}
    for p in parts:
        origin = p['rotation']['origin'] if p['rotation'] else [(a + b) / 2 for a, b in zip(p['lo'], p['hi'])]
        q = (1 if origin[0] >= 16 else 0) + (2 if origin[1] >= 16 else 0)
        dx, dy = (16 if q & 1 else 0), (16 if q & 2 else 0)
        p['lo'] = [p['lo'][0] - dx, p['lo'][1] - dy, p['lo'][2]]
        p['hi'] = [p['hi'][0] - dx, p['hi'][1] - dy, p['hi'][2]]
        if p['rotation']:
            o = p['rotation']['origin']
            p['rotation'] = dict(p['rotation'], origin=[o[0] - dx, o[1] - dy, o[2]])
        quarters[q].append(p)
    return quarters


@piece('circus_wagon_wheel', 'Wagon Wheel', 'wheel')
def wagon_wheel():
    return {f'q{q}': els for q, els in wheel_parts().items()}


@piece('circus_cookpot', 'Cook Pot')
def cookpot():
    return ring(1, 1, 15, 15, 0, 2.5, 'stone', t=2, name='hearth') + [
        box('coals', (3, 0, 3), (13, 1, 13), 'ember', extra=BRIGHT),
        box('pot', (4, 3, 4), (12, 9, 12), 'iron'),
        box('pot_foot', (5, 2, 5), (11, 3, 11), 'iron'),
        box('rim', (3.5, 8.5, 3.5), (12.5, 9.5, 12.5), 'ferric'),
        box('stew', (4.5, 8.75, 4.5), (11.5, 9.25, 11.5), 'blood'),
        box('crane_post', (13.5, 2.5, 7.5), (14.5, 15, 8.5), 'iron'),
        box('crane_arm', (7.5, 14, 7.5), (14.5, 15, 8.5), 'iron'),
        box('chain', (7.75, 9.5, 7.75), (8.25, 14, 8.25), 'chain'),
        box('bale', (5, 9.5, 7.75), (11, 10, 8.25), 'iron'),
        box('ladle', (10.5, 8, 10.5), (11.25, 13, 11.25), 'bone', rotation=spin('z', 22.5, (10.9, 9, 10.9)))]


@piece('circus_costume_rack', 'Costume Rack')
def costume_rack():
    e = [box('foot_w', (0, 0, 5), (3.5, 1, 11), 'iron'),
         box('foot_e', (12.5, 0, 5), (16, 1, 11), 'iron'),
         box('upright_w', (1, 1, 7.25), (2.5, 15, 8.75), 'wood'),
         box('upright_e', (13.5, 1, 7.25), (15, 15, 8.75), 'wood'),
         box('rail', (1, 14, 7.5), (15, 15, 8.5), 'iron'),
         box('knob_w', (.75, 15, 7), (2.75, 16, 9), 'bone'),
         box('knob_e', (13.25, 15, 7), (15.25, 16, 9), 'bone')]
    for i, (x, mat, low) in enumerate(((3, 'lacquer', 4), (6.5, 'velvet', 2.5), (10, 'canvas', 5))):
        e += [box(f'hook_{i}', (x + 1.5, 13, 7.75), (x + 2, 14, 8.25), 'iron'),
              box(f'shoulders_{i}', (x, 12, 6.5), (x + 3.5, 13, 9.5), 'bone'),
              box(f'garment_{i}', (x + .25, low, 6.75), (x + 3.25, 12, 9.25), mat),
              box(f'hem_{i}', (x, low - .5, 6.5), (x + 3.5, low, 9.5), 'thread')]
    return e


@piece('circus_trunk', 'Costume Trunk')
def trunk():
    e = [box('body', (1, 0, 3), (15, 9, 13), 'lacquer'),
         box('lid', (.5, 9, 2.5), (15.5, 11, 13.5), 'lacquer_deep'),
         box('hasp', (7, 6, 2.5), (9, 9.5, 3), 'iron'),
         box('lock', (7.25, 9, 2.25), (8.75, 10.25, 2.5), 'ferric'),
         box('handle_w', (.5, 5, 6.5), (1, 6, 9.5), 'iron'),
         box('handle_e', (15, 5, 6.5), (15.5, 6, 9.5), 'iron')]
    for i, x in enumerate((3, 11.5)):
        e.append(box(f'strap_{i}', (x, 0, 2.75), (x + 1.5, 11.25, 13.25), 'iron'))
    for i, (x, y, z) in enumerate(((.75, 0, 2.75), (13.75, 0, 2.75), (.75, 0, 11.75), (13.75, 0, 11.75))):
        e.append(box(f'corner_{i}', (x, y, z), (x + 1.5, y + 2, z + 1.5), 'bone'))
    return e


@piece('circus_ticket_window', 'Ticket Window')
def ticket_window():
    e = [box('apron', (0, 0, 6), (16, 9, 10), 'panel'),
         box('counter', (0, 9, 2), (16, 10.5, 10), 'wood'),
         box('nosing', (0, 8.75, 1.5), (16, 10.75, 2), 'bone'),
         box('arch', (0, 14.5, 6.5), (16, 16, 9.5), 'lacquer'),
         box('tray', (6, 10.5, 3), (10, 11, 6), 'bone'),
         box('tickets', (2.5, 10.5, 3.5), (5, 11.75, 5.5), 'paper'),
         box('tickets_tie', (2.25, 11, 4.25), (5.25, 11.5, 4.75), 'thread')]
    for i, x in enumerate((1.75, 4.75, 7.75, 10.75, 13.75)):
        e.append(box(f'bar_{i}', (x, 10.5, 7.5), (x + .75, 14.5, 8.5), 'iron'))
    return e


@piece('circus_pennant_line', 'Pennant Line')
def pennant_line():
    e = [box('cord', (0, 14, 7.75), (16, 14.5, 8.25), 'thread')]
    for i, (x, mat) in enumerate(((1, 'lacquer'), (6, 'canvas_plain'), (11, 'lacquer'))):
        e += [box(f'flag_{i}', (x, 11, 7.85), (x + 4, 14, 8.15), mat),
              box(f'flag_mid_{i}', (x + 1, 9, 7.85), (x + 3, 11, 8.15), mat),
              box(f'flag_tip_{i}', (x + 1.5, 7, 7.85), (x + 2.5, 9, 8.15), mat)]
    return e


# --- the camps ------------------------------------------------------------------------------------------------------

@piece('circus_knife_wheel', 'Knife Wheel', 'tall')
def knife_wheel():
    e = [box('foot', (0, 0, 9), (16, 1.5, 15), 'iron'),
         box('leg_w', (1, 1.5, 11), (2.5, 16, 12.5), 'iron'),
         box('leg_e', (13.5, 1.5, 11), (15, 16, 12.5), 'iron'),
         box('axle', (7, 15, 9.5), (9, 17, 12.5), 'iron')]
    rows = ((4, 6), (5, 9), (6, 11), (8, 12.5), (10, 13.5), (13, 14), (19, 14), (22, 13.5), (24, 12.5),
            (26, 11), (27, 9), (28, 6))
    for i, (top, half) in enumerate(rows):
        bottom = rows[i - 1][0] if i else 3
        e.append(box(f'board_{i}', (8 - half, bottom, 7), (8 + half, top, 9), 'wood'))
    e += [box('spiral_a', (1.5, 15, 6.75), (14.5, 17, 7), 'lacquer'),
          box('spiral_b', (7, 5, 6.75), (9, 27, 7), 'lacquer'),
          box('hub', (6, 14, 6.25), (10, 18, 6.75), 'bone'),
          box('strap_hi', (5, 21, 6.5), (11, 22, 6.75), 'pelt'),
          box('strap_lo', (5, 9, 6.5), (11, 10, 6.75), 'pelt')]
    for i, (x, yy) in enumerate(((2.5, 12), (13, 20), (4.5, 24), (11.5, 7), (8, 25.5))):
        e += [box(f'blade_{i}', (x, yy, 5), (x + .5, yy + .5, 7), 'iron'),
              box(f'grip_{i}', (x - .25, yy - .25, 3.25), (x + .75, yy + .75, 5), 'bone')]
    return e


@piece('circus_target_board', 'Target Board')
def target_board():
    e = [box('leg_w', (3, 0, 8.5), (4, 6, 10), 'iron'),
         box('leg_e', (12, 0, 8.5), (13, 6, 10), 'iron'),
         box('strut', (7.5, 0, 9), (8.5, 6, 11.5), 'iron', rotation=spin('x', -22.5, (8, 6, 9.5))),
         box('board', (2, 5, 7), (14, 15, 9), 'wood'),
         box('board_lo', (3, 4, 7), (13, 5, 9), 'wood'),
         box('board_hi', (3, 15, 7), (13, 16, 9), 'wood'),
         box('ring', (4, 6.5, 6.75), (12, 13.5, 7), 'bone'),
         box('inner', (5.5, 8, 6.5), (10.5, 12, 6.75), 'lacquer'),
         box('eye', (7, 9.25, 6.25), (9, 10.75, 6.5), 'bone')]
    for i, (x, yy) in enumerate(((4.5, 12.5), (10.5, 7.5), (8.5, 10.5))):
        e += [box(f'blade_{i}', (x, yy, 5), (x + .5, yy + .5, 6.75), 'iron'),
              box(f'grip_{i}', (x - .25, yy - .25, 3.5), (x + .75, yy + .75, 5), 'bone')]
    return e


@piece('circus_stilt_rack', 'Stilt Rack', 'tall')
def stilt_rack():
    e = [box('foot', (1, 0, 11), (15, 1.5, 16), 'iron'),
         box('upright_w', (1, 1.5, 14), (2.5, 30, 15.5), 'wood'),
         box('upright_e', (13.5, 1.5, 14), (15, 30, 15.5), 'wood'),
         box('rail_lo', (1, 8, 13.5), (15, 9, 15.5), 'lacquer'),
         box('rail_hi', (1, 24, 13.5), (15, 25, 15.5), 'lacquer'),
         box('cap', (.5, 30, 13.5), (15.5, 31, 16), 'bone')]
    for i, x in enumerate((3.5, 6.25, 9, 11.75)):
        lean = 1.5 if i % 2 else 0
        e += [box(f'stilt_{i}', (x, 1.5, 12 - lean), (x + 1, 29, 13 - lean), 'wood'),
              box(f'shoe_{i}', (x - .5, 1.5, 10.5 - lean), (x + 1.5, 2.5, 13.5 - lean), 'iron'),
              box(f'step_{i}', (x - .25, 10, 10 - lean), (x + 1.25, 11, 12 - lean), 'bone'),
              box(f'band_{i}', (x - .25, 16, 11.75 - lean), (x + 1.25, 17, 13.25 - lean), 'lacquer'),
              box(f'strap_{i}', (x - .25, 12.5, 11.75 - lean), (x + 1.25, 13.5, 13.25 - lean), 'thread')]
    return e


@piece('circus_weight_frame', 'Weight Frame')
def weight_frame():
    e = [box('foot_w', (0, 0, 5), (3, 1, 11), 'iron'),
         box('foot_e', (13, 0, 5), (16, 1, 11), 'iron'),
         box('upright_w', (1, 1, 7.5), (2, 11, 8.5), 'iron'),
         box('upright_e', (14, 1, 7.5), (15, 11, 8.5), 'iron'),
         box('cradle_w', (.5, 10, 7), (2.5, 11, 9), 'ferric'),
         box('cradle_e', (13.5, 10, 7), (15.5, 11, 9), 'ferric'),
         box('bar', (0, 11, 7.5), (16, 12, 8.5), 'iron'),
         box('bell_bar', (5, 0, 3.5), (11, 1, 4.5), 'iron'),
         box('bell_w', (4, 0, 2.5), (5.5, 2.5, 5.5), 'bone'),
         box('bell_e', (10.5, 0, 2.5), (12, 2.5, 5.5), 'bone')]
    for side, x in (('w', 2.75), ('e', 11.25)):
        e += [box(f'plate_{side}', (x, 8, 5), (x + 2, 15, 11), 'bone'),
              box(f'rim_{side}', (x + .25, 8.5, 4.75), (x + 1.75, 14.5, 11.25), 'lacquer'),
              box(f'collar_{side}', (x + (2 if side == 'w' else -.75), 10.75, 7.25),
                  (x + (2.75 if side == 'w' else 0), 12.25, 8.75), 'ferric')]
    return e


@piece('circus_fire_pit', 'Fire Pit')
def fire_pit():
    e = ring(0, 0, 16, 16, 0, 4, 'stone', t=2.5, name='kerb') + [
        box('ash', (2.5, 0, 2.5), (13.5, 1.5, 13.5), 'ash'),
        box('coals', (4, 1.5, 4), (12, 2.5, 12), 'ember', extra=BRIGHT),
        box('log_a', (3, 2, 7), (13, 4, 9), 'char', rotation=spin('y', 22.5, (8, 3, 8))),
        box('log_b', (7, 2, 3), (9, 4, 13), 'char', rotation=spin('y', 22.5, (8, 3, 8))),
        box('grate', (2, 4, 7.5), (14, 4.5, 8.5), 'iron')]
    for i, (x, z, h) in enumerate(((6, 6, 7), (9, 8, 9), (7, 9.5, 6), (9.5, 5.5, 5))):
        e.append(box(f'tongue_{i}', (x, 3, z), (x + 1.5, 3 + h, z + 1.5), 'ember', extra=BRIGHT))
    return e


@piece('circus_bellows_stand', 'Bellows Stand')
def bellows_stand():
    e = [box('top', (1, 6, 4), (15, 7, 14), 'wood'),
         box('board_lo', (3, 7, 4), (13, 8, 14), 'lacquer'),
         box('leather', (3.5, 8, 4.5), (12.5, 11, 13.5), 'pelt'),
         box('board_hi', (3, 11, 4), (13, 12, 14), 'lacquer'),
         box('nozzle', (7, 8.5, .5), (9, 10.5, 4), 'iron'),
         box('nozzle_tip', (7.5, 9, 0), (8.5, 10, .5), 'ferric'),
         box('handle_lo', (6, 7, 14), (10, 8, 16), 'bone'),
         box('handle_hi', (6, 11, 14), (10, 12, 16), 'bone'),
         box('rivets', (4, 12, 6), (12, 12.25, 12), 'iron')]
    for i, (x, z) in enumerate(((2, 5), (12.5, 5), (2, 11.5), (12.5, 11.5))):
        e.append(box(f'leg_{i}', (x, 0, z), (x + 1.5, 6, z + 1.5), 'wood'))
    return e


@piece('circus_hound_hoop', 'Hound Hoop')
def hound_hoop():
    e = [box('foot', (5, 0, 6), (11, 1, 10), 'iron'),
         box('post', (7.5, 1, 7.5), (8.5, 4.5, 8.5), 'iron')]
    for k in range(8):
        phi = math.radians(k * 45 + 22.5)
        cx, cy = 8 + 5.6 * math.cos(phi), 10 + 5.6 * math.sin(phi)
        e.append(tangent_box(f'hoop_{k}', cx, cy, 7.5, 8.5, 4.9, 1.25, math.degrees(phi) + 90, 'iron'))
        e.append(tangent_box(f'wrap_{k}', cx, cy, 7.25, 8.75, 2, 1.5, math.degrees(phi) + 90, 'thread'))
        if k in (1, 2, 3, 5):
            e.append(tangent_box(f'flame_{k}', 8 + 6.4 * math.cos(phi), 10 + 6.4 * math.sin(phi), 7.6, 8.4,
                                 1.2, 1.6, math.degrees(phi), 'ember'))
            e[-1]['extra'] = BRIGHT
    return e


@piece('circus_prompt_table', 'Prompt-Book Table')
def prompt_table():
    e = [box('top', (0, 11, 1), (16, 12.5, 15), 'wood'),
         box('edge', (0, 10.75, .75), (16, 11, 15.25), 'bone'),
         box('apron', (1.5, 9.5, 2), (14.5, 11, 14), 'panel'),
         box('cover_w', (3, 12.5, 4), (8, 13, 12), 'lacquer_deep'),
         box('cover_e', (8, 12.5, 4), (13, 13, 12), 'lacquer_deep'),
         box('page_w', (3.5, 13, 4.5), (7.75, 13.75, 11.5), 'paper'),
         box('page_e', (8.25, 13, 4.5), (12.5, 13.75, 11.5), 'paper'),
         box('ribbon', (7.75, 12.75, 11.5), (8.25, 13.5, 13.5), 'thread'),
         box('stack', (12.5, 12.5, 12), (15.5, 14.5, 14.5), 'lacquer'),
         box('stack_pages', (12.75, 12.75, 11.75), (15.25, 14.25, 14.25), 'paper'),
         box('quill', (1.5, 12.5, 6), (2.25, 13, 12), 'bone', rotation=spin('y', 22.5, (2, 12.75, 9)))]
    for i, (x, z) in enumerate(((1, 2), (13.5, 2), (1, 12.5), (13.5, 12.5))):
        e += [box(f'leg_{i}', (x, 0, z), (x + 1.5, 11, z + 1.5), 'lacquer'),
              box(f'turn_{i}', (x - .25, 4, z - .25), (x + 1.75, 5, z + 1.75), 'bone')]
    return e


# --- the four evidence records ------------------------------------------------------------------------------------

@piece('circus_stage_record', 'Troupe Stage Record')
def stage_record():
    return [box('board', (2, 2, 9), (14, 15, 10.5), 'wood'),
            box('frame_top', (1.5, 14.5, 8.75), (14.5, 15.5, 10.75), 'lacquer'),
            box('bill', (3, 3.5, 8.75), (13, 14, 9), 'bill'),
            box('leg_w', (2.5, 0, 10), (4, 3, 11.5), 'iron'),
            box('leg_e', (12, 0, 10), (13.5, 3, 11.5), 'iron'),
            box('tack_w', (3.25, 13, 8.5), (4.25, 13.75, 8.75), 'bone'),
            box('tack_e', (11.75, 13, 8.5), (12.75, 13.75, 8.75), 'bone'),
            box('torn', (9, 3.5, 8.75), (13, 5, 9), 'canvas_plain')]


@piece('circus_loft_record', 'Troupe Loft Record')
def loft_record():
    e = [box('rail', (1, 9, 13), (15, 11, 16), 'wood'),
         box('book', (5, 2, 13.5), (11, 9, 15.25), 'lacquer_deep'),
         box('book_pages', (5.5, 2.5, 13.25), (10.5, 8.5, 15.5), 'paper'),
         box('lanyard', (7.75, 9, 13.25), (8.25, 12, 13.75), 'thread')]
    for i, x in enumerate((2.5, 12.5)):
        e += [box(f'pin_{i}', (x, 6, 12), (x + 1, 14, 13), 'bone'),
              box(f'pin_head_{i}', (x - .25, 13, 11.75), (x + 1.25, 14.5, 13.25), 'bone'),
              box(f'coil_{i}', (x - 1, 11.25, 11.5), (x + 2, 12.75, 14.5), 'thread')]
    return e


@piece('circus_quarters_record', 'Troupe Quarters Record')
def quarters_record():
    return [box('desk', (2, 0, 3), (14, 7, 13), 'panel'),
            box('desk_top', (1.5, 7, 2.5), (14.5, 7.5, 13.5), 'wood'),
            box('cover', (3, 7.5, 4), (12, 8, 12), 'lacquer_deep'),
            box('pages', (3.5, 8, 4.5), (11.5, 8.75, 11.5), 'ledger'),
            box('inkwell', (12, 7.5, 10.5), (13.75, 9.5, 12.25), 'iron'),
            box('ink', (12.25, 9.25, 10.75), (13.5, 9.5, 12), 'blood'),
            box('quill', (12.75, 9.5, 11.25), (13.25, 13, 11.75), 'bone',
                rotation=spin('z', 22.5, (13, 9.5, 11.5)))]


@piece('impaled_procession_record', "Carousel Maker's Plate")
def carousel_record():
    e = [box('pedestal', (4, 0, 4), (12, 6, 12), 'lacquer'),
         box('pedestal_cap', (3.5, 6, 3.5), (12.5, 7, 12.5), 'bone'),
         box('plate', (3, 7, 7.5), (13, 15, 9), 'ferric'),
         box('horse_body', (6, 10, 7.25), (10, 12, 7.5), 'bone'),
         box('horse_head', (9.5, 11.5, 7.25), (11, 13.5, 7.5), 'bone'),
         box('horse_pole', (7.75, 8.5, 7.25), (8.25, 14, 7.5), 'iron'),
         box('stand', (7, 7, 9), (9, 12, 10.5), 'iron')]
    for i, (x, y) in enumerate(((3.5, 7.5), (11.75, 7.5), (3.5, 13.75), (11.75, 13.75))):
        e.append(box(f'rivet_{i}', (x, y, 7.25), (x + .75, y + .75, 7.5), 'bone'))
    return e


# --- export ---------------------------------------------------------------------------------------------------------

def write_item(name, parent=None, elements=None, atlas=None, height=None):
    path = ASSETS / 'models' / 'item' / f'{name}.json'
    if elements is not None:
        write_tall_item(name, model_json(elements, atlas, height, f'hemomancy:block/{atlas}'))
        return
    path.write_text(json.dumps({'parent': parent or f'hemomancy:block/{name}'}, indent=2) + '\n')


def atlas_height(name):
    return Image.open(ASSETS / f'textures/block/{name}.png').height


def export_piece(name, fn, title, kind, show):
    if kind in ('facing', 'plain'):
        variants = facing_variants(f'hemomancy:block/{name}') if kind == 'facing' else \
            {'': {'model': f'hemomancy:block/{name}'}}
        out = export(name, {'body': fn()}, title, variants, show, SPECIAL)
        write_item(name)
        return out
    if kind == 'tall':
        lower, upper = split_storeys(fn())
        models = {f'block/{name}_lower': 'lower', f'block/{name}_upper': 'upper'}
        variants = {}
        for half in ('lower', 'upper'):
            variants.update(facing_variants(f'hemomancy:block/{name}_{half}', f'half={half},'))
        out = export(name, {'lower': lower, 'upper': upper}, title, variants, False, SPECIAL, models)
        whole = item_elements((out[f'block/{name}_lower']['elements'], 0), (out[f'block/{name}_upper']['elements'], 16))
        write_item(name, elements=whole, atlas=name, height=atlas_height(name))
        if show:
            preview(whole, name, title, f'circus_{name}', lift=8)
        return out
    if kind == 'pole':
        groups = fn()
        models = {f'block/{name}_{s}': s for s in groups}
        # A section walled in on all four sides (through the roof canvas) is a full laced-canvas block.
        variants = {f'section={s},sealed={sealed}': {'model': f'hemomancy:block/{name}_sealed' if sealed == 'true'
                                                     else f'hemomancy:block/{name}_{s}'}
                    for s in groups for sealed in ('false', 'true')}
        out = export(name, groups, title, variants, False, SPECIAL, models)
        (ASSETS / f'models/block/{name}_sealed.json').write_text(json.dumps(
            {'parent': 'minecraft:block/cube_all', 'textures': {'all': 'hemomancy:block/circus_laced_canvas'}},
            indent=2) + '\n')
        whole = item_elements((out[f'block/{name}_foot']['elements'], 0), (out[f'block/{name}_finial']['elements'], 16))
        write_item(name, elements=whole, atlas=name, height=atlas_height(name))
        if show:
            preview(whole, name, title, f'circus_{name}', lift=8, scale=10)
        return out
    if kind == 'wheel':
        groups = fn()
        models = {f'block/{name}_{q}': q for q in groups}
        variants = {}
        for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            for q in range(4):
                variants[f'facing={facing},quarter={q}'] = dict({'model': f'hemomancy:block/{name}_q{q}'},
                                                                **({'y': y} if y else {}))
        out = export(name, groups, title, variants, False, SPECIAL, models)
        whole = []
        for q in range(4):
            dx, dy = (16 if q & 1 else 0), (16 if q & 2 else 0)
            for el in item_elements((out[f'block/{name}_q{q}']['elements'], dy)):
                el['from'][0] += dx
                el['to'][0] += dx
                if 'rotation' in el:
                    el['rotation']['origin'][0] += dx
                whole.append(el)
        for el in whole:
            for v in ('from', 'to'):
                el[v] = [el[v][0] - 8, el[v][1] - 8, el[v][2]]
            if 'rotation' in el:
                o = el['rotation']['origin']
                el['rotation']['origin'] = [o[0] - 8, o[1] - 8, o[2]]
        write_item(name, elements=whole, atlas=name, height=atlas_height(name))
        if show:
            preview(whole, name, title, f'circus_{name}', lift=8, scale=9)
        return out
    if kind == 'octagonal':
        # rotation counts 45 degree steps clockwise from north; odd steps show the diagonal model, which is authored
        # at north-west (step 7) and turned a quarter for each pair of steps after it.
        variants = {}
        for step in range(8):
            model, y = ('diagonal', (step + 1) // 2 % 4 * 90) if step % 2 else ('body', step // 2 * 90)
            path = f'hemomancy:block/{name}' + ('_diagonal' if model == 'diagonal' else '')
            variants[f'rotation={step}'] = dict({'model': path}, **({'y': y} if y else {}))
        models = {f'block/{name}': 'body', f'block/{name}_diagonal': 'diagonal'}
        out = export(name, fn(), title, variants, show, SPECIAL, models)
        write_item(name)
        return out
    if kind == 'net':
        groups = fn()
        models = {f'block/{name}': 'body', f'block/{name}_rim': 'rim', f'block/{name}_tie': 'tie'}
        out = export(name, groups, title, {}, show, SPECIAL, models)
        parts = [{'apply': {'model': f'hemomancy:block/{name}'}}]
        for side, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            turn = {'y': y} if y else {}
            parts.append({'when': {side: 'edge|tied'}, 'apply': dict({'model': f'hemomancy:block/{name}_rim'}, **turn)})
            parts.append({'when': {side: 'tied'}, 'apply': dict({'model': f'hemomancy:block/{name}_tie'}, **turn)})
        (ASSETS / f'blockstates/{name}.json').write_text(json.dumps({'multipart': parts}, indent=2) + '\n')
        write_item(name)
        return out
    raise ValueError(kind)


# --- full-block surfaces --------------------------------------------------------------------------------------------

def texture(painter):
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = painter(x, y)
    return image


def canvas_texel(x, y):
    return canvas('north', x, y, 16, 16, 'cube')


def laced_texel(x, y):
    """Two canvas flaps meeting down the middle, iron eyelets on each lip, crimson thread crossing between them."""
    if x in (7, 8):
        return clamp(CRIMSON_LO, -18 if x == 7 else -6) + (255,)
    for ey in (1, 5, 9, 13):
        if y in (ey, ey + 1) and x in (5, 10):
            return (40, 36, 46, 255)
        if y == ey and x in (4, 6, 9, 11) or y == ey + 1 and x in (4, 6, 9, 11):
            return (92, 88, 98, 255)
    phase = y % 4
    if (x == 6 + phase // 2 and phase in (2, 3)) or (x == 9 - phase // 2 and phase in (2, 3)):
        return clamp(THREAD, int(noise('lace', x, y) * 12)) + (255,)
    return canvas_texel(x if x < 7 else x + 2, y)


def wainscot_texel(x, y):
    """Lacquered boards three texels high under a bone bead rail and an iron fillet."""
    if y < 2:
        return clamp((212, 196, 166), (10 if y == 0 else -14) + int(noise('bead', x) * 8)) + (255,)
    if y == 2:
        return (52, 50, 60, 255)
    if (y - 3) % 4 == 3:
        return (70, 8, 18, 255)
    board = (y - 3) // 4
    n = noise('wain', board, x, y)
    return clamp((136, 22, 36), int(noise('wgrain', board, y) * 14) - 7 + int(n * 6) - (8 if x == (board * 5) % 16 else 0)) + (255,)


def wainscot_top_texel(x, y):
    n = noise('wtop', x, y)
    return clamp((80, 38, 40), int(noise('wtg', y) * 14) - 7 + int(n * 6) - (16 if y in (0, 15) else 0)) + (255,)


def sawdust_texel(x, y):
    n = noise('saw', x, y)
    if n > .95:
        return (138, 26, 34, 255)
    if n < .04:
        return (226, 212, 186, 255)
    return clamp(SAWDUST, int(noise('saw2', x // 2, y // 2) * 20) - 10 + int(n * 10) - 5) + (255,)


def panel_texel(x, y):
    """A painted wagon panel: a lacquer field inside a bone moulding, a fret cartouche in the middle, iron rivets."""
    n = noise('wpanel', x, y)
    if x in (0, 15) or y in (0, 15):
        return clamp((92, 12, 24), int(n * 8) - 4) + (255,)
    if x in (1, 14) or y in (1, 14):
        return clamp((212, 196, 166), int(n * 10) - 8 - (12 if x == 14 or y == 14 else 0)) + (255,)
    if (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        return (52, 50, 60, 255)
    if 5 <= x <= 10 and 5 <= y <= 10:
        if x in (5, 10) or y in (5, 10) or (x + y) % 4 == 1 or (x - y) % 4 == 1:
            return clamp((212, 196, 166), int(n * 10) - 14) + (255,)
        return clamp((92, 12, 24), int(n * 6) - 8) + (255,)
    return clamp((136, 22, 36), (8 - y) * 1.5 + int(n * 6) - 3) + (255,)


def wrecked_texel(x, y):
    """The same panel after the fire: lacquer flaked to char, a split down the boards, the moulding scorched."""
    n = noise('wreck', x, y)
    crack = abs(x - (6 + int(noise('split', y // 3) * 3))) < 1
    if crack:
        return (20, 14, 16, 255)
    if noise('char', x // 2, y // 2) > .5 or n > .8:
        return clamp(CHAR, int(n * 14) - 4) + (255,)
    rgb = panel_texel(x, y)
    return clamp(rgb[:3], -36 + int(n * 10)) + (255,)


def plinth_side_texel(x, y):
    n = noise('pside', x, y)
    if 5 <= y <= 9:
        if y in (5, 9):
            return clamp((212, 196, 166), int(n * 8) - 8) + (255,)
        if y == 7 and x % 4 == 1:
            return (212, 196, 166, 255)
        return clamp((136, 22, 36), int(n * 6) - 3 - (y - 6) * 4) + (255,)
    return stone('north', x, y, 16, 16, 'plinth')


def plinth_top_texel(x, y):
    d = math.hypot(x - 7.5, y - 7.5)
    if 5.6 < d < 6.6:
        return clamp((212, 196, 166), int(noise('pring', x, y) * 8) - 6) + (255,)
    return stone('up', x, y, 16, 16, 'plinth_top')


CUBES = {
    'circus_canvas': ('Striped Canvas', {'all': canvas_texel}),
    'circus_laced_canvas': ('Laced Canvas', {'side': laced_texel, 'end': canvas_texel}),
    'circus_wainscot': ('Lacquered Wainscot', {'side': wainscot_texel, 'end': wainscot_top_texel}),
    'circus_sawdust': ('Ring Sawdust', {'all': sawdust_texel}),
    'circus_wagon_panel': ('Wagon Panel', {'all': panel_texel}),
    'circus_wrecked_panel': ('Wrecked Wagon Panel', {'all': wrecked_texel}),
}


def export_cube(name, faces):
    textures = {}
    for slot, painter in faces.items():
        tex_name = name if slot in ('all', 'side') else f'{name}_{slot}'
        texture(painter).save(ASSETS / f'textures/block/{tex_name}.png')
        textures[slot] = f'hemomancy:block/{tex_name}'
    if 'all' in textures:
        model = {'parent': 'minecraft:block/cube_all', 'textures': textures}
    else:
        model = {'parent': 'minecraft:block/cube_column', 'textures': textures}
    (ASSETS / f'models/block/{name}.json').write_text(json.dumps(model, indent=2) + '\n')
    (ASSETS / f'blockstates/{name}.json').write_text(
        json.dumps({'variants': {'': {'model': f'hemomancy:block/{name}'}}}, indent=2) + '\n')
    write_item(name)
    print(f'Exported {name}: 16x16 surface')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('only', nargs='*')
    args = parser.parse_args()
    for name, (title, faces) in CUBES.items():
        if not args.only or name in args.only:
            export_cube(name, faces)
    for name, (fn, title, kind) in PIECES.items():
        if args.only and name not in args.only:
            continue
        export_piece(name, fn, title, kind, args.preview)


if __name__ == '__main__':
    main()

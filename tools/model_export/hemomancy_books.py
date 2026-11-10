"""Authored book models: Liber Sanguinium, Liber Immaculatus, Book of Observances, the Harbinger Assignment Ledger and
the Mnemonic Folio.

Every book shares one chassis so a single HemoBookModel can open, close and flip it:
    left_lid / right_lid      a board, pivoting on the spine line (x = 0)
    spine                     the spine's back, which HemoBookModel slides back and narrows as the boards open
    left_pages / right_pages  the two halves of the text block, which turn with their boards
    flip_page1 / flip_page2   two loose leaves that sweep from one side to the other
    left_closed, left_open,   children of the lids, shown only while the book is shut or open
    right_closed, right_open  (clasp straps, the ledger's strap)
The spine's back always spans the two boards' hinge corners, so it never cuts through them or crosses itself.
A fitting that wraps a board must not end on one of the board's own faces, or the two z-fight: EPS laps it past the
inner face and holds it off the hinge edge.

Model space: the spine runs along y with the head at -y, boards reach out along +x, and the book's thickness is z.
The front board is left_lid on the -z side. The rigs below are written in design units (a design unit is a pixel at
block size); every box is multiplied by SCALE on the way into the mesh, which keeps one texel per model unit, and
HemoBookLayers.MODEL_SCALE draws it back at 1 / SCALE. The books are therefore 32 px per block, a deliberate exception
to the 16 px rule for these focal items. Painters work in design units too, but borders and rows in texels.

    python tools/model_export/hemomancy_books.py            # HemoBookLayers.java + textures/entity/book/*.png
    python tools/model_export/hemomancy_books.py --preview  # also build/hemomancy_books_preview.png

Harbinger books use hematic iron, bone and lacquer, never brass, bronze or copper. Unstained books use white vellum,
patinated copper and pale silver, never blood red.
"""
from pathlib import Path
import argparse
import math

from PIL import Image

import unstained_weapons as uw

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/item/book/HemoBookLayers.java'
TEXTURES = uw.ASSETS / 'textures/entity/book'

CLEAR = (0, 0, 0, 0)
SCALE = 2          # texels per design unit
EPS = 0.02
ATLAS_WIDTH = 128


# ---------------------------------------------------------------- chassis

class Book:
    def __init__(self, name, method, W, H, T, b, s):
        self.name, self.method = name, method
        self.W, self.H, self.T, self.b, self.s = W, H, T, b, s
        self.Wp, self.Hp = W - 0.5, H - 1      # text block: inset from the fore-edge, head and foot
        self.outer = T / 2 + b                  # outer face of a board, |z|
        self.rig = uw.Rig(name, 'HemoBookLayers', name)

    def cube(self, part, box, material):
        self.rig.cube(part, tuple(round(c * SCALE, 4) for c in box), material)

    def pair(self, side_box, material, left='left_lid', right='right_lid'):
        """A box on the front (-z) side and its mirror on the back."""
        self.cube(left, side_box, material)
        self.cube(right, mirror(side_box), material)


def mirror(box):
    x, y, z, w, h, d = box
    return x, y, -z - d, w, h, d


def chassis(book, cover, spine, pages, leaf):
    r, W, H, T, b, s = book.rig, book.W, book.H, book.T, book.b, book.s
    for side in ('left', 'right'):
        r.part(f'{side}_lid')
        r.part(f'{side}_closed', f'{side}_lid')
        r.part(f'{side}_open', f'{side}_lid')
    book.pair((0, -H / 2, -book.outer, W, H, b), cover)
    r.part('spine')
    book.cube('spine', (-s, -H / 2, -book.outer, s, H, 2 * book.outer), spine)
    # The text blocks overlap by 0.02 past the centre so a resting flip leaf sits hidden inside them.
    r.part('left_pages')
    r.part('right_pages')
    block = (0.1, -book.Hp / 2, -T / 2, book.Wp - 0.1, book.Hp, T / 2 + 0.02)
    book.cube('left_pages', block, pages)
    book.cube('right_pages', mirror(block), pages)
    for name in ('flip_page1', 'flip_page2'):
        r.part(name)
        book.cube(name, (0.15, -book.Hp / 2 + 0.1, -0.005, book.Wp - 0.3, book.Hp - 0.2, 0.01), leaf)


def corner_caps(book, material, size, wrap_spine=False):
    """Caps on all four corners of both boards; the fore-edge pair wraps the board's edge."""
    W, H, o = book.W, book.H, book.outer
    for top in (True, False):
        y = -H / 2 - 0.25 if top else H / 2 - size + 0.25
        # Fore-edge: wraps the outer face, the head or foot, and the fore-edge, and laps 0.1 onto the inside.
        book.pair((W - size + 0.25, y, -o - 0.25, size, size, book.b + 0.35), material)
        if wrap_spine:
            book.pair((EPS, y, -o - 0.25, size - 0.25 - EPS, size, 0.5), material)


def spine_bands(book, ys, material, height=0.5, proud=0.25):
    for y in ys:
        book.cube('spine', (-book.s - proud, y - height / 2, -book.outer, proud, height, 2 * book.outer), material)


# ---------------------------------------------------------------- books

def liber_sanguinum():
    """Deep crimson leather over boards: iron caps, filigree and a rosette holding a blood-glass drop."""
    bk = Book('liber_sanguinum', 'LiberSanguinum', W=7, H=10, T=2, b=0.5, s=0.75)
    chassis(bk, 'crimson_board', 'crimson_spine', 'vellum_pages', 'vellum_leaf')
    W, o = bk.W, bk.outer
    corner_caps(bk, 'iron_cap', 1.5, wrap_spine=True)
    spine_bands(bk, (-3, 0, 3), 'iron_band')
    # Filigree frame, raised a pixel's eighth off the leather, with stems running in to the rosette.
    z = -o - 0.125
    for box in ((1.25, -3.75, z, 4.5, 0.5, 0.125), (1.25, 3.25, z, 4.5, 0.5, 0.125),
                (1.25, -3.25, z, 0.5, 6.5, 0.125), (5.25, -3.25, z, 0.5, 6.5, 0.125),
                (3.25, -3.25, z, 0.5, 1.75, 0.125), (3.25, 1.5, z, 0.5, 1.75, 0.125)):
        bk.cube('left_lid', box, 'iron_filigree')
    # Rosette: an iron ring, a dark seat and the blood-glass drop proud of it.
    bk.cube('left_lid', (2, -1.5, -o - 0.375, 3, 3, 0.375), 'iron_rosette')
    bk.cube('left_lid', (2.5, -1, -o - 0.5, 2, 2, 0.125), 'iron_seat')
    bk.cube('left_lid', (3, -0.875, -o - 0.75, 1, 1.75, 0.25), 'blood_glass')
    # Back board: the frame without the rosette, and a small iron lozenge.
    for box in ((1.25, -3.75, z, 4.5, 0.5, 0.125), (1.25, 3.25, z, 4.5, 0.5, 0.125),
                (1.25, -3.25, z, 0.5, 6.5, 0.125), (5.25, -3.25, z, 0.5, 6.5, 0.125)):
        bk.cube('right_lid', mirror(box), 'iron_filigree')
    bk.cube('right_lid', mirror((3, -1, -o - 0.25, 1, 2, 0.25)), 'iron_rosette')
    clasp(bk, catch='iron', strap='crimson_strap', tip='iron')
    # Chapter tabs on the fore-edge, as the reader draws them, and a crimson ribbon from the foot.
    for part, y, z0, mat in (('left_pages', -3.5, -0.75, 'tab_crimson'), ('left_pages', 1, -0.5, 'tab_black'),
                             ('right_pages', -1.75, 0.5, 'tab_bone'), ('right_pages', 2.5, 0.25, 'tab_crimson')):
        bk.cube(part, (bk.Wp, y, z0, 0.75, 1, 0.25), mat)
    bk.cube('right_pages', (1.25, bk.Hp / 2 - 0.5, 0.03, 0.5, 3, 0.1), 'ribbon_crimson')
    return bk


def liber_immaculatus():
    """White vellum binding with patinated copper fittings, pale silver rules and the Lady's crescent."""
    bk = Book('liber_immaculatus', 'LiberImmaculatus', W=7, H=10, T=2, b=0.5, s=0.75)
    chassis(bk, 'vellum_board', 'vellum_spine', 'silvered_pages', 'pale_leaf')
    o = bk.outer
    corner_caps(bk, 'copper_cap', 1.25, wrap_spine=True)
    spine_bands(bk, (-3.25, 3.25), 'copper_band', height=0.75)
    # Double silver rule.
    z = -o - 0.125
    rule = ((1, -4, z, 5, 0.25, 0.125), (1, 3.75, z, 5, 0.25, 0.125),
            (1, -3.75, z, 0.25, 7.5, 0.125), (5.75, -3.75, z, 0.25, 7.5, 0.125))
    for box in rule:
        bk.cube('left_lid', box, 'silver_rule')
        bk.cube('right_lid', mirror(box), 'silver_rule')
    # The crescent is a cut-out copper plate over blind-tooled still water.
    bk.cube('left_lid', (1.5, -3.25, -o - 0.25, 4, 4, 0.25), 'copper_crescent')
    clasp(bk, catch='copper', strap='white_strap', tip='copper')
    bk.cube('right_pages', (1.25, bk.Hp / 2 - 0.5, 0.03, 0.5, 3, 0.1), 'ribbon_slate')
    return bk


def book_of_observances():
    """A slate-teal girdle book: its leather runs past the foot into a tail knotted round a copper ring."""
    bk = Book('book_of_observances', 'BookOfObservances', W=5, H=8, T=1.5, b=0.5, s=0.5)
    chassis(bk, 'teal_board', 'teal_spine', 'linen_pages', 'linen_leaf')
    W, H, o, b = bk.W, bk.H, bk.outer, bk.b
    # Copper binding along the head and foot of each board.
    for y in (-H / 2 - 0.125, H / 2 - 0.375):
        bk.pair((EPS, y, -o - 0.125, W + 0.125 - EPS, 0.5, b + 0.125 + EPS), 'copper_edge')
    spine_bands(bk, (-2, 0, 2), 'copper_band', height=0.5, proud=0.125)
    # Three studs, one for each guide whose observances it groups.
    for y in (-2.5, -1.25, 0):
        bk.cube('left_lid', (W / 2 - 0.375, y, -o - 0.25, 0.75, 0.75, 0.25), 'copper_stud')
    # Three ribbons out of the head: Acolyte, Zealot, Guardian.
    head = -bk.Hp / 2
    for part, x, length, mat in (('left_pages', 2.75, 1.25, 'ribbon_linen'), ('right_pages', 3.5, 1.75, 'ribbon_slate'),
                                 ('left_pages', 4.25, 1.0, 'ribbon_verdigris')):
        z = -0.13 if part == 'left_pages' else 0.03
        bk.cube(part, (x, head - length, z, 0.5, length + 0.5, 0.1), mat)
    # The tail leaves the back board's foot in its plane and tapers to a knot and the ring.
    z0, y0 = o - b, H / 2
    bk.cube('right_lid', (0.5, y0, z0, W - 1, 1, b), 'teal_tail')
    bk.cube('right_lid', (1, y0 + 1, z0, W - 2, 1, b), 'teal_tail')
    bk.cube('right_lid', (1.5, y0 + 2, z0, W - 3, 0.75, b), 'teal_tail')
    bk.cube('right_lid', (1.75, y0 + 2.75, z0 - 0.25, 1.5, 1, b + 0.5), 'teal_knot')
    ry = y0 + 3.75
    for box in ((1.25, ry, z0, 2.5, 0.5, b), (1.25, ry + 2, z0, 2.5, 0.5, b),
                (1.25, ry + 0.5, z0, 0.5, 1.5, b), (3.25, ry + 0.5, z0, 0.5, 1.5, b)):
        bk.cube('right_lid', box, 'copper_ring')
    return bk


def assignment_ledger():
    """A Vicar's account book: tall, near-black oxblood, iron hinges, a buckled strap and loose waybills."""
    bk = Book('harbinger_assignment_ledger', 'HarbingerAssignmentLedger', W=5, H=11, T=2.5, b=0.5, s=0.75)
    chassis(bk, 'oxblood_board', 'oxblood_spine', 'ledger_pages', 'ledger_leaf')
    W, H, o, s = bk.W, bk.H, bk.outer, bk.s
    corner_caps(bk, 'iron_cap', 1.25)
    spine_bands(bk, (-2.5, 0, 2.5), 'oxblood_band', height=0.5)
    # Hinge plates: across the spine's back and out onto each board near the head and foot.
    for y in (-H / 2 + 0.5, H / 2 - 1.75):
        bk.cube('spine', (-s - 0.25, y, -o, 0.25, 1.25, 2 * o), 'iron_hinge')
        bk.pair((0, y, -o - 0.25, 1.25, 1.25, 0.25), 'iron_hinge')
    # An iron-framed vellum index card.
    bk.cube('left_lid', (1.25, -4, -o - 0.25, 3, 2, 0.25), 'index_card')
    # The buckle stays on the front board; the strap belongs to the back and comes free when the book opens.
    bk.cube('left_lid', (W - 2.25, 0.25, -o - 0.375, 1, 1.75, 0.375), 'iron_buckle')
    sy, sh = 0.5, 1.25
    bk.cube('right_closed', (0.5, sy, o, W - 0.25, sh, 0.25), 'oxblood_strap')
    bk.cube('right_closed', (W + 0.25, sy, -o - 0.25, 0.25, sh, 2 * o + 0.5), 'oxblood_strap')
    bk.cube('right_closed', (W - 2.75, sy, -o - 0.25, 3, sh, 0.25), 'oxblood_strap')
    bk.cube('right_open', (0.5, sy, o, W + 2.25, sh, 0.25), 'oxblood_strap')
    # Loose waybills out of the head, one sealed in red wax.
    head = -bk.Hp / 2
    bk.cube('left_pages', (0.75, head - 1.25, -0.6, 1.5, 1.75, 0.1), 'waybill')
    bk.cube('left_pages', (1.1, head - 1.1, -0.85, 0.75, 0.75, 0.25), 'wax_seal')
    bk.cube('right_pages', (2.5, head - 1.75, 0.4, 1.25, 2.25, 0.1), 'waybill_old')
    bk.cube('right_pages', (1.25, bk.Hp / 2 - 0.5, 0.03, 0.5, 2.5, 0.1), 'ribbon_crimson')
    return bk


def mnemonic_folio():
    """A broad plan portfolio: scorched umber boards, bone caps and compass rose, and black plan sheets ruled in blood."""
    bk = Book('mnemonic_folio', 'MnemonicFolio', W=8, H=9, T=1.25, b=0.5, s=0.5)
    chassis(bk, 'umber_board', 'umber_spine', 'plan_pages', 'plan_leaf')
    W, o = bk.W, bk.outer
    corner_caps(bk, 'bone_cap', 1.5)
    spine_bands(bk, (-2.5, 2.5), 'bone_band', height=0.5, proud=0.125)
    bk.cube('left_lid', (W / 2 - 1.5, -1.5, -o - 0.25, 3, 3, 0.25), 'bone_rose')
    clasp(bk, catch='bone', strap='bone_tie', tip='bone')
    # Plans too large for the boards, out of the head and the fore-edge.
    head = -bk.Hp / 2
    bk.cube('left_pages', (1.5, head - 1, -0.4, 3, 1.5, 0.1), 'plan_sheet')
    bk.cube('right_pages', (4.5, head - 0.5, 0.25, 2, 1, 0.1), 'plan_sheet')
    bk.cube('left_pages', (bk.Wp - 0.5, 1.5, -0.25, 1.25, 2.25, 0.1), 'plan_sheet')
    return bk


def clasp(bk, catch, strap, tip):
    """A catch plate on the front board's fore-edge and a strap from the back that wraps over to it when shut."""
    W, o = bk.W, bk.outer
    bk.cube('left_lid', (W - 1, -1, -o - 0.25, 1.25, 2, bk.b + 0.25 + EPS), catch + '_catch')
    bk.cube('right_closed', (W - 2, -0.5, o, 2.25, 1, 0.25), strap)
    bk.cube('right_closed', (W + 0.25, -0.5, -o - 0.5, 0.25, 1, 2 * o + 0.75), strap)
    bk.cube('right_closed', (W - 0.75, -0.5, -o - 0.5, 1.0, 1, 0.25), tip + '_catch')
    bk.cube('right_open', (W - 2, -0.5, o, 3.5, 1, 0.25), strap)
    bk.cube('right_open', (W + 1.5, -0.5, o - 0.125, 0.5, 1, 0.5), tip + '_catch')


BOOKS = (liber_sanguinum, liber_immaculatus, book_of_observances, assignment_ledger, mnemonic_folio)


# ---------------------------------------------------------------- painting

CRIMSON = (128, 18, 30)
CRIMSON_DARK = (92, 12, 22)
CRIMSON_DEEP = (66, 8, 16)
IRON = uw_iron = (62, 60, 70)
IRON_LIGHT = (112, 104, 114)
IRON_DARK = (38, 36, 44)
GLASS = (196, 30, 42)
GLASS_HOT = (255, 122, 122)
GLASS_DEEP = (110, 10, 20)
VELLUM = (232, 220, 192)
VELLUM_SHADE = (208, 194, 162)
BONE = (212, 196, 166)
OXBLOOD = (48, 22, 24)
OXBLOOD_DARK = (32, 14, 16)
OXBLOOD_LIGHT = (64, 30, 32)
WHITE = (236, 231, 219)
WHITE_SHADE = (220, 213, 198)
TEAL = (79, 127, 134)
TEAL_DARK = (58, 95, 102)
TEAL_LIGHT = (96, 142, 148)
SLATE = (111, 143, 168)
LINEN = uw.PALETTE['linen']
UMBER = (84, 40, 30)
UMBER_DARK = (62, 28, 22)
UMBER_DEEP = (44, 20, 16)
PLAN = (32, 27, 31)

# Blind-tooled ripples per (book, board material): arc rows and the pressed colour.
TOOLING = {('liber_immaculatus', 'vellum_board'): ((1.5, 2.5), (194, 186, 168)),
           ('book_of_observances', 'teal_board'): ((1.75, 2.75), (48, 80, 86))}


def noisy(rgb, t, amount=10):
    return uw.add(rgb, (t['n'] - 0.5) * amount)


def border(t):
    """True on the outermost ring of texels of a side face."""
    return t['u'] == 0 or t['v'] == 0 or t['u'] == t['fw'] - 1 or t['v'] == t['fh'] - 1


def leather(base, dark, deep, t):
    rgb = base if t['n'] < 0.62 else dark
    if t['n'] > 0.94:
        rgb = deep
    if t['n'] < 0.08:
        rgb = uw.add(base, 14)
    return noisy(rgb, t, 6)


def iron(t, base=IRON):
    rgb = noisy(base, t, 8)
    if t['face'] in ('north', 'south') and t['v'] == 0:
        rgb = IRON_LIGHT                               # catch-light along the upper edge
    elif t['face'] in ('up',):
        rgb = uw.add(base, 18)
    elif t['n'] > 0.9:
        rgb = IRON_DARK
    return rgb


def bone(t):
    rgb = noisy(BONE, t, 8)
    if t['face'] in ('north', 'south') and t['v'] == 0:
        return uw.add(rgb, 18)
    return uw.add(rgb, -34) if t['n'] > 0.92 else rgb


def copper(t):
    """Patina without uw.verdigris's bare-copper flecks, which read as rust on parts this small."""
    n = t['n']
    rgb = (62, 122, 108) if n < 0.25 else (148, 204, 178) if n > 0.85 else uw.PALETTE['verdigris']
    if t['face'] in ('north', 'south') and t['v'] == 0:
        rgb = uw.add(rgb, 22)
    return rgb


def page_surface(t, ink, paper=VELLUM, rubric=None):
    """A written leaf: a margin, rows of script in paragraphs with ragged ends, and a rubricated initial."""
    rgb = noisy(paper, t, 6)
    u, v, fw, fh = t['u'], t['v'], t['fw'], t['fh']
    if u < 1 or v < 1 or u > fw - 2 or v > fh - 2 or v % 2 == 0:
        return rgb
    row = v // 2
    if row % 5 == 4:
        return rgb                                     # paragraph break
    if rubric and row % 5 == 0 and u <= 2:
        return rubric                                  # the paragraph's initial
    if u > fw - 2 - int(uw.h(row, fw, 3) * 4):
        return rgb                                     # ragged line end
    if t['n'] > 0.25:
        return uw.mix(paper, ink, 0.42 if t['n'] > 0.7 else 0.24)
    return rgb


# ---------------------------------------------------------------- written pages
# Each book's six visible page surfaces (the two text blocks' inner faces and both sides of both loose leaves) take a
# different layout from the book's theme: plain script, a pictogram over the text, a diagram, a pictogram set into
# the text, or a pair of pictograms. Pictograms are 7x7 texels; X is drawn, anything else is a blank around it.

PICTOGRAMS = dict(
    drop=('...X...', '..XXX..', '..XXX..', '.XXXXX.', '.XXXXX.', '.XXXXX.', '..XXX..'),
    heart=('.XX.XX.', 'XXXXXXX', 'XXXXXXX', '.XXXXX.', '..XXX..', '...X...', '.......'),
    vein=('...X...', '...X...', '..XXX..', '.X.X.X.', 'X..X..X', '..X.X..', '.X...X.'),
    fungus=('..XXX..', '.XXXXX.', 'XXXXXXX', '...X...', '...X...', '..XXX..', '.......'),
    hand=('.X.X.X.', '.X.X.X.', '.XXXXX.', 'XXXXXX.', '.XXXXX.', '..XXX..', '..XXX..'),
    crescent=('..XXX..', '.XX....', 'XX.....', 'XX.....', 'XX.....', '.XX....', '..XXX..'),
    waves=('.......', '.XX..XX', 'X..XX..', '.......', '.XX..XX', 'X..XX..', '.......'),
    chalice=('XXXXXXX', '.XXXXX.', '..XXX..', '...X...', '...X...', '..XXX..', '.XXXXX.'),
    star=('...X...', '...X...', 'XXXXXXX', '..XXX..', '.XX.XX.', '.X...X.', '.......'),
    eye=('.......', '..XXX..', '.X...X.', 'X..X..X', '.X...X.', '..XXX..', '.......'),
    candle=('...X...', '..X.X..', '...X...', '..XXX..', '..XXX..', '..XXX..', '.XXXXX.'),
    key=('.XXX...', 'X...X..', 'X...X..', '.XXXXXX', '....X.X', '....X.X', '.......'),
    seal=('..XXX..', '.XXXXX.', 'XX.X.XX', 'XXX.XXX', 'XX.X.XX', '.XXXXX.', '..XXX..'),
    compass=('...X...', '..X.X..', '.X.X.X.', 'XXXXXXX', '.X.X.X.', '..X.X..', '...X...'),
)

# The page surface each (part, face) pair shows. A text block's outer face lies against its board, unseen.
PAGE_ORDER = {('left_pages', 'south'): 0, ('left_pages', 'north'): 0, ('right_pages', 'north'): 1,
              ('right_pages', 'south'): 1, ('flip_page1', 'north'): 2, ('flip_page1', 'south'): 3,
              ('flip_page2', 'north'): 4, ('flip_page2', 'south'): 5}


def picto(name, u, v, u0, v0):
    """'ink' or 'blank' inside a pictogram's box (with a texel of clear margin), None outside it."""
    rows = PICTOGRAMS[name]
    du, dv = u - u0, v - v0
    if -1 <= du <= 7 and -1 <= dv <= 7:
        return 'ink' if 0 <= du < 7 and 0 <= dv < 7 and rows[dv][du] == 'X' else 'blank'
    return None


def sanguine_circle(u, v, fw, fh):
    """A blood-memory circle: a ring of eight temper nodes round a centre, the Liber's teaching diagram."""
    cx, cy, R = (fw - 1) / 2, 6.0, min(4.0, (fw - 3) / 2)
    dx, dy = u - cx, v - cy
    r = math.hypot(dx, dy)
    if r > R + 1.5:
        return None
    for k in range(8):
        a = k * math.pi / 4
        if math.hypot(dx - R * math.cos(a), dy - R * math.sin(a)) < 0.75:
            return 'accent'
    if r < 0.8:
        return 'accent'
    if abs(r - R) < 0.5 or (r < R and min(abs(dx), abs(dy)) < 0.3):
        return 'ink'
    return 'blank'


def still_rings(u, v, fw, fh):
    """Rings spreading on still water from a single fall, the Lethean figure."""
    cx, cy = (fw - 1) / 2, 6.0
    r = math.hypot(u - cx, v - cy)
    if r > min(5.5, fw / 2 - 0.5):
        return None
    if r < 0.7:
        return 'accent'
    if any(abs(r - ring) < 0.4 for ring in (2.2, 4.6)):
        return 'ink'
    return 'blank'


def checklist(u, v, fw, fh):
    """An observance list: a box per entry, some ticked."""
    if v > fh * 0.65 or v % 3 == 0:
        return None
    row = v // 3
    if u <= 2:
        if v % 3 == 1 and u in (1, 2) or v % 3 == 2 and u == 1:
            return 'ink'
        if v % 3 == 2 and u == 2:
            return 'accent' if uw.h(row, 7) > 0.4 else 'blank'
        return 'blank'
    return None


def ledger_table(u, v, fw, fh):
    """Ruled account columns: entry, the assigning officer and a tick."""
    if u == fw - 4 or (fw >= 12 and u == fw // 2):
        return 'rule'
    if v % 4 == 0:
        return 'rule'
    if u == fw - 3 and v % 4 == 2:
        return 'accent' if uw.h(v, 11) > 0.35 else 'blank'
    return None


def floor_plan(u, v, fw, fh):
    """A rite's floor seen from above: a ring, its cardinal stations and the grid it was set out on."""
    cx, cy = (fw - 1) / 2, 6.0
    dx, dy = u - cx, v - cy
    r = math.hypot(dx, dy)
    if r > 6.5:
        return None
    if max(abs(dx), abs(dy)) > 5.4:
        return 'blank'
    if min(math.hypot(dx - x, dy - y) for x, y in ((4, 0), (-4, 0), (0, 4), (0, -4))) < 0.8:
        return 'accent'
    if abs(r - 4) < 0.5 or abs(dx) < 0.3 or abs(dy) < 0.3:
        return 'ink'
    return 'rule' if u % 4 == 0 or v % 4 == 0 else 'blank'


def text_only(u, v, fw, fh, idx, theme):
    return None


def picto_top(u, v, fw, fh, idx, theme):
    pics = theme['pictos']
    return picto(pics[idx % len(pics)], u, v, (fw - 7) // 2, 2)


def diagram(u, v, fw, fh, idx, theme):
    return theme['diagram'](u, v, fw, fh)


def picto_inset(u, v, fw, fh, idx, theme):
    pics = theme['pictos']
    u0 = fw - 9 if fw >= 13 else (fw - 7) // 2
    return picto(pics[(idx + 1) % len(pics)], u, v, u0, fh // 2 - 3)


def picto_pair(u, v, fw, fh, idx, theme):
    pics = theme['pictos']
    if fw >= 17:
        left = picto(pics[(idx + 2) % len(pics)], u, v, fw // 2 - 8, fh - 9)
        return left or picto(pics[(idx + 3) % len(pics)], u, v, fw // 2 + 1, fh - 9)
    return picto(pics[(idx + 2) % len(pics)], u, v, (fw - 7) // 2, fh - 9)


LAYOUTS = (text_only, picto_top, diagram, picto_inset, picto_pair, picto_top)

THEMES = dict(
    liber_sanguinum=dict(paper=VELLUM, ink=(90, 20, 24), rubric=(156, 22, 32), picture=(96, 14, 22),
                         accent=(176, 24, 34), pictos=('heart', 'drop', 'vein', 'fungus', 'hand'),
                         diagram=sanguine_circle),
    liber_immaculatus=dict(paper=WHITE, ink=(88, 104, 120), rubric=(62, 122, 108), picture=(74, 96, 116),
                           accent=(62, 122, 108), pictos=('crescent', 'waves', 'chalice', 'star', 'drop'),
                           diagram=still_rings),
    book_of_observances=dict(paper=LINEN, ink=(88, 104, 120), rubric=(62, 122, 108), picture=(74, 96, 116),
                             accent=(62, 122, 108), pictos=('eye', 'candle', 'hand', 'crescent', 'waves'),
                             diagram=checklist),
    harbinger_assignment_ledger=dict(paper=VELLUM, ink=(60, 40, 30), rubric=(156, 22, 32), picture=(70, 46, 34),
                                     accent=(156, 22, 32), pictos=('seal', 'key', 'compass', 'drop', 'eye'),
                                     diagram=ledger_table),
    mnemonic_folio=dict(paper=PLAN, ink=(150, 34, 38), rubric=(196, 44, 48), picture=(178, 36, 40),
                        accent=(226, 214, 186), pictos=('compass', 'seal', 'key', 'star', 'eye'),
                        diagram=floor_plan),
)


def written_page(t, theme):
    """One page surface: its layout's pictogram or diagram, script everywhere else."""
    u, v, fw, fh = t['u'], t['v'], t['fw'], t['fh']
    paper = noisy(theme['paper'], t, 6)
    if u < 1 or v < 1 or u > fw - 2 or v > fh - 2:
        return paper
    idx = PAGE_ORDER.get((t.get('part'), t['face']), 0)
    mark = LAYOUTS[idx % len(LAYOUTS)](u, v, fw, fh, idx, theme)
    if mark == 'ink':
        return uw.mix(theme['paper'], theme['picture'], 0.8 if t['n'] > 0.15 else 0.6)
    if mark == 'accent':
        return theme['accent']
    if mark == 'rule':
        return uw.mix(theme['paper'], theme['picture'], 0.35)
    if mark == 'blank':
        return paper
    return page_surface(t, theme['ink'], theme['paper'], theme['rubric'])


def page_edge(t, paper=VELLUM, line=VELLUM_SHADE):
    return line if (t['u'] + t['v']) % 3 == 0 and t['n'] > 0.4 else noisy(paper, t, 6)


def make_painter(bk):
    W, H, T = bk.W, bk.H, bk.T

    def inner_face(t):
        return t['face'] in ('north', 'south') and abs(abs(t['gz']) - T / 2) < 0.01

    def cap_shape(t, size):
        """Corner caps are triangles: keep texels within `size` (taxicab) of the board's nearest corner."""
        cx = W + 0.25 if t['gx'] > W / 2 else 0
        cy = -H / 2 - 0.25 if t['gyf'] < 0 else H / 2 + 0.25
        return abs(t['gx'] - cx) + abs(t['gyf'] - cy) <= size

    def board(base, dark, deep, endpaper, wear=None, tooled=True, inlay=None):
        """Leather over a board: worn edges, a blind-tooled line a texel in, and an endpaper inside."""
        def paint(t):
            if inner_face(t):
                # The leather turns in round the board's margin; the endpaper fills what the text block covers.
                if t['gx'] > bk.Wp - 0.05 or abs(t['gyf']) > bk.Hp / 2:
                    return leather(dark, deep, deep, t)
                return endpaper(t) if callable(endpaper) else noisy(endpaper, t, 6)
            rgb = leather(base, dark, deep, t)
            if wear and t['side'] and border(t):
                return wear
            u, v, fw, fh = t['u'], t['v'], t['fw'], t['fh']
            flat = t['face'] in ('north', 'south')
            ring = u == 1 or v == 1 or u == fw - 2 or v == fh - 2
            if flat and tooled and ring and 1 <= u <= fw - 2 and 1 <= v <= fh - 2:
                return uw.add(deep, -6)
            if flat and inlay and inlay(t):
                return iron(t)
            return rgb
        return paint

    def scrolls(t):
        """Iron scrollwork inlaid in each inner corner of the Liber Sanguinium's filigree frame."""
        x, y = t['gx'], t['gyf']
        for cx, sx in ((1.75, 1), (5.25, -1)):
            for cy, sy in ((-3.25, 1), (3.25, -1)):
                dx, dy = (x - cx) * sx, (y - cy) * sy
                if 0 < dx < 1.3 and 0 < dy < 1.3 and abs(math.hypot(dx - 1.1, dy - 1.1) - 0.95) < 0.22:
                    return True
        return False

    def marbled(colours):
        """Marbled endpaper: combed bands of colour."""
        def paint(t):
            k = math.sin(t['gx'] * 2.1 + math.sin(t['gyf'] * 1.7) * 1.6 + t['gz'])
            rgb = colours[0] if k < -0.3 else colours[1] if k < 0.45 else colours[2]
            return noisy(rgb, t, 6)
        return paint

    def crescent(t):
        """The Lady's crescent on its plate (x 1.5-5.5, y -3.25-0.75): a disc cut by a smaller one toward the fore-edge."""
        x, y = t['gx'] - 3.5, t['gyf'] + 1.25
        return math.hypot(x, y) <= 1.95 and math.hypot(x - 0.85, y) > 1.45

    def ripples(t, rows):
        """Blind-tooled still water: shallow arcs across the lower board."""
        x, y = t['gx'] - W / 2, t['gyf']
        return any(abs(y - (yy + 0.08 * x * x)) < 0.25 for yy in rows) and abs(x) < W / 2 - 0.9

    def compass_rose(t):
        """Bone on the folio's plate (centred on the board): a ring round an eight-point star, cut out of the plate."""
        x, y = t['gx'] - W / 2, t['gyf']
        r = math.hypot(x, y)
        if 1.2 < r < 1.5:
            return True
        return abs(x) + abs(y) * 2.6 < 1.3 or abs(y) + abs(x) * 2.6 < 1.3

    def rosette(t):
        """Iron ring with eight petals notched round it."""
        if t['face'] not in ('north', 'south'):
            return iron(t, (74, 72, 82))
        x, y = t['gx'] - 3.5, t['gyf']
        r = math.hypot(x, y)
        petal = (math.atan2(y, x) / (2 * math.pi) * 8) % 1
        if r > 1.1 and (petal < 0.18 or petal > 0.82):
            return IRON_DARK
        if 0.95 < r < 1.2:
            return IRON_LIGHT
        return iron(t, (74, 72, 82))

    def stitched(base, dark, deep, thread):
        """A strap or tail with a running stitch a texel in from its two long edges."""
        def paint(t):
            u, v, fw, fh = t['u'], t['v'], t['fw'], t['fh']
            if t['face'] in ('north', 'south') and max(fw, fh) >= 6 and min(fw, fh) >= 4:
                along, across, length = (u, v, fh) if fw >= fh else (v, u, fw)
                if (across == 1 or across == length - 2) and along % 2 == 0:
                    return thread
            return leather(base, dark, deep, t)
        return paint

    theme = THEMES[bk.name]

    def pages(paper, line, edge=None):
        def paint(t):
            if t['face'] in ('north', 'south'):
                return written_page(t, theme)
            if edge:
                return edge(t)
            return page_edge(t, paper, line)
        return paint

    def ribbon(rgb, notch=True):
        def paint(t):
            if notch and t['side'] and t['v'] == t['fh'] - 1 and t['face'] in ('north', 'south'):
                return CLEAR
            return noisy(rgb, t, 8) + (255,)
        return paint

    def card(t):
        if border(t) or not t['side'] or t['face'] not in ('north', 'south'):
            return iron(t)
        if t['v'] == 1:
            return uw.mix(VELLUM, CRIMSON, 0.7)          # the rubric line naming the assignment
        return uw.mix(VELLUM, (120, 104, 80), 0.4) if t['v'] % 2 == 1 and t['n'] > 0.25 else noisy(VELLUM, t, 6)

    def buckle(t):
        if t['face'] in ('north', 'south') and not border(t):
            return CLEAR                                  # an open frame the strap passes through
        return iron(t)

    def glass(t):
        # A drop: round at the foot, pointed toward the head.
        x, y = t['gx'] - 3.5, t['gyf']
        if t['face'] in ('north', 'south') and abs(x) > 0.5 * (1 - max(0, -y) / 0.9):
            return CLEAR
        if t['face'] in ('north', 'south') and t['v'] == 0:
            return GLASS_HOT + (255,)
        return (GLASS_DEEP if t['n'] > 0.75 else GLASS) + (255,)

    m = dict(
        crimson_board=board(CRIMSON, CRIMSON_DARK, CRIMSON_DEEP, marbled(((92, 12, 22), (214, 198, 164), (48, 30, 34))),
                            wear=CRIMSON_DEEP, inlay=scrolls),
        crimson_spine=lambda t: leather(CRIMSON_DARK, CRIMSON_DEEP, (48, 6, 12), t),
        crimson_strap=stitched(CRIMSON_DARK, CRIMSON_DEEP, CRIMSON_DEEP, BONE),
        vellum_pages=pages(VELLUM, VELLUM_SHADE),
        vellum_leaf=lambda t: written_page(t, theme) if t['face'] in ('north', 'south') else page_edge(t),
        iron_band=lambda t: iron(t),
        iron_filigree=lambda t: IRON_LIGHT if t['face'] in ('north', 'south') and (t['u'] + t['v']) % 4 == 0 else iron(t),
        iron_rosette=rosette,
        iron_seat=lambda t: noisy(IRON_DARK, t, 6),
        iron_catch=lambda t: iron(t),
        tab_crimson=lambda t: noisy(CRIMSON, t, 8),
        tab_black=lambda t: noisy((34, 30, 36), t, 8),
        tab_bone=lambda t: noisy(BONE, t, 8),
        ribbon_crimson=ribbon((88, 10, 20)),

        vellum_board=board(WHITE, WHITE_SHADE, (206, 198, 182), marbled(((176, 196, 210), (232, 228, 218), (126, 164, 160))),
                           tooled=False),
        vellum_spine=lambda t: leather(WHITE_SHADE, (208, 200, 184), (196, 188, 170), t),
        white_strap=lambda t: leather(WHITE, WHITE_SHADE, WHITE_SHADE, t),
        silvered_pages=pages(WHITE, WHITE_SHADE, edge=lambda t: uw.silver(t)),
        pale_leaf=lambda t: written_page(t, theme) if t['face'] in ('north', 'south') else uw.silver(t),
        copper_band=lambda t: copper(t),
        copper_catch=lambda t: copper(t),
        silver_rule=lambda t: uw.silver(t, 'silver_bright' if t['n'] > 0.7 else 'silver'),
        ribbon_slate=ribbon(SLATE),

        teal_board=board(TEAL, TEAL_DARK, (50, 84, 90), LINEN, wear=TEAL_DARK),
        teal_spine=lambda t: leather(TEAL_DARK, (50, 84, 90), (44, 74, 80), t),
        teal_tail=stitched(TEAL, TEAL_DARK, TEAL_DARK, (200, 196, 180)),
        teal_knot=lambda t: TEAL_DARK if (t['u'] + t['v']) % 2 == 0 else leather(TEAL_LIGHT, TEAL, TEAL, t),
        linen_pages=pages(LINEN, (196, 190, 172)),
        linen_leaf=lambda t: written_page(t, theme) if t['face'] in ('north', 'south') else page_edge(t, LINEN),
        copper_edge=lambda t: copper(t),
        copper_stud=lambda t: (148, 204, 178) if t['face'] in ('north', 'south') and t['u'] == 0 and t['v'] == 0 else copper(t),
        copper_ring=lambda t: copper(t),
        ribbon_linen=ribbon((240, 238, 231)),
        ribbon_verdigris=ribbon(uw.PALETTE['verdigris']),

        oxblood_board=board(OXBLOOD, OXBLOOD_DARK, (24, 10, 12), VELLUM_SHADE, wear=OXBLOOD_DARK),
        oxblood_spine=lambda t: leather(OXBLOOD_DARK, (24, 10, 12), (20, 8, 10), t),
        oxblood_band=lambda t: leather(OXBLOOD_LIGHT, OXBLOOD, OXBLOOD, t),
        oxblood_strap=stitched(OXBLOOD_LIGHT, OXBLOOD, OXBLOOD_DARK, (112, 92, 80)),
        ledger_pages=pages(VELLUM, VELLUM_SHADE),
        ledger_leaf=lambda t: written_page(t, theme) if t['face'] in ('north', 'south') else page_edge(t),
        iron_hinge=lambda t: IRON_LIGHT if t['face'] in ('north', 'south', 'west', 'east') and t['u'] % 3 == 1 and t['v'] == 0 else iron(t),
        index_card=card,
        iron_buckle=buckle,
        waybill=lambda t: noisy((226, 214, 186), t, 8) if t['side'] else noisy(VELLUM_SHADE, t, 6),
        waybill_old=lambda t: noisy((204, 186, 150), t, 10) if t['side'] else noisy((184, 168, 134), t, 6),
        wax_seal=lambda t: (156, 22, 32) if t['n'] < 0.7 else (120, 12, 22),

        umber_board=board(UMBER, UMBER_DARK, UMBER_DEEP, PLAN, wear=UMBER_DEEP),
        umber_spine=lambda t: leather(UMBER_DARK, UMBER_DEEP, UMBER_DEEP, t),
        plan_pages=pages(PLAN, (22, 18, 22)),
        plan_leaf=lambda t: written_page(t, theme) if t['face'] in ('north', 'south') else page_edge(t, PLAN, (22, 18, 22)),
        plan_sheet=lambda t: (150, 34, 38) if t['face'] in ('north', 'south') and t['u'] % 3 == 1 and t['n'] > 0.45 else noisy(PLAN, t, 6),
        bone_band=lambda t: bone(t),
        bone_catch=lambda t: bone(t),
        bone_tie=stitched(BONE, VELLUM_SHADE, VELLUM_SHADE, (150, 34, 38)),
    )

    def paint(material, t):
        if material in ('iron_cap', 'copper_cap', 'bone_cap'):
            if not cap_shape(t, 1.25 if material == 'copper_cap' else 1.5):
                return CLEAR, None
            rgb = {'iron_cap': iron, 'copper_cap': copper, 'bone_cap': bone}[material](t)
            if t['face'] in ('north', 'south') and cap_shape(t, 0.75) and not cap_shape(t, 0.45):
                rgb = uw.add(rgb, 34)                     # a rivet head set into the cap
            return rgb + (255,), None
        if material == 'copper_crescent':
            if t['face'] in ('north', 'south') and not crescent(t):
                return CLEAR, None
            return copper(t) + (255,), None
        if material == 'bone_rose':
            if t['face'] not in ('north', 'south') or not compass_rose(t):
                return CLEAR, None
            return bone(t) + (255,), None
        if material == 'blood_glass':
            return glass(t), None
        tooled = TOOLING.get((bk.name, material))
        if tooled and not inner_face(t) and t['face'] in ('north', 'south') and ripples(t, tooled[0]):
            return tooled[1] + (255,), None
        rgb = m[material](t)
        if len(rgb) == 4:
            return rgb, None
        return rgb + (255,), None

    return paint


# ---------------------------------------------------------------- atlas (box-UV, painted by position)

def texel_info(face, box, rect, px, py, origin):
    t = uw.texel_info(face, box, rect, px, py, origin)
    x0, y0, x1, y1 = rect
    lo_v, hi_v = sorted((y0, y1))
    lo_u, hi_u = sorted((x0, x1))
    fu = min(1, max(0, (px + 0.5 - lo_u) / max(hi_u - lo_u, 1e-6)))
    fv = min(1, max(0, (py + 0.5 - lo_v) / max(hi_v - lo_v, 1e-6)))
    _, ly, _ = uw.local_point(face, box, fu, fv)
    t['gyf'] = (ly + origin[1]) / SCALE
    t['gx'] /= SCALE
    t['gz'] /= SCALE
    return t


def layout(rig):
    """uw.layout on a wider atlas: assign texOffs row by row, growing the height as needed."""
    for height in (64, 128, 256):
        u = v = row = 0
        ok = True
        for part in rig.parts:
            for cube in part['cubes']:
                _, _, _, w, hgt, d = cube['box']
                cw, ch = uw.ceil(2 * (w + d)) + 1, uw.ceil(hgt + d) + 1
                if u + cw > ATLAS_WIDTH:
                    u, v, row = 0, v + row, 0
                if v + ch > height:
                    ok = False
                cube['uv'] = (u, v)
                u += cw
                row = max(row, ch)
        if ok:
            return ATLAS_WIDTH, height
    raise ValueError(rig.name + ' does not fit a 128x256 atlas')


def paint_atlas(rig, size, painter):
    image = Image.new('RGBA', size)
    world = uw.part_world(rig)
    for part in rig.parts:
        origin = world[part['name']]
        for cube in part['cubes']:
            u, v = cube['uv']
            _, _, _, w, hgt, d = cube['box']
            for face in ('down', 'up', 'west', 'east', 'north', 'south'):
                rect = uw.face_rects(u, v, w, hgt, d)[face]
                lo_u, hi_u = sorted((rect[0], rect[2]))
                lo_v, hi_v = sorted((rect[1], rect[3]))
                for py in range(math.floor(lo_v), uw.ceil(hi_v)):
                    for px in range(math.floor(lo_u), uw.ceil(hi_u)):
                        t = texel_info(face, cube['box'], rect, px, py, origin)
                        t['part'] = part['name']
                        rgba, _ = painter(cube['material'], t)
                        image.putpixel((px, py), rgba)
    return image


# ---------------------------------------------------------------- Java

def layer_method(bk, size):
    lines = []
    for part in bk.rig.parts:
        if part['parent'] is None:
            continue
        chain = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            u, v = cube['uv']
            chain += f'\n\t\t\t\t.texOffs({u}, {v}).addBox({", ".join(uw.f(c) for c in cube["box"])})'
        parent = 'root' if part['parent'] == 'root' else part['parent']
        declare = f'PartDefinition {part["name"]} = ' if any(p['parent'] == part['name'] for p in bk.rig.parts) else ''
        lines.append(f'\t\t{declare}{parent}.addOrReplaceChild("{part["name"]}", {chain}, PartPose.ZERO);')
    body = '\n'.join(lines)
    return f'''
	public static LayerDefinition create{bk.method}Layer() {{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
{body}
		return LayerDefinition.create(mesh, {size[0]}, {size[1]});
	}}
'''


def closed_bounds(bk):
    """The shut book's extent (min x, y, z, max x, y, z) in model units, open-only fittings left out."""
    lo, hi = [math.inf] * 3, [-math.inf] * 3
    for part in bk.rig.parts:
        if part['name'].endswith('_open'):
            continue
        for cube in part['cubes']:
            x, y, z, w, h, d = cube['box']
            for i, (a, size) in enumerate(((x, w), (y, h), (z, d))):
                lo[i], hi[i] = min(lo[i], a), max(hi[i], a + size)
    return lo + hi


def java_source(entries):
    fields = '\n'.join(
        f'\tpublic static final ModelLayerLocation {camel_upper(bk.name)} = new ModelLayerLocation(\n'
        f'\t\t\tHemomancy.rloc("book/{bk.name}"), "main");\n'
        f'\t/** The shut book\'s extent in model units: min x, y, z, then max x, y, z. */\n'
        f'\tpublic static final float[] {camel_upper(bk.name)}_BOUNDS = {{'
        f'{", ".join(uw.f(c) for c in closed_bounds(bk))}}};' for bk, _ in entries)
    methods = ''.join(layer_method(bk, size) for bk, size in entries)
    return f'''package com.vincenthuto.hemomancy.client.model.item.book;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The book meshes {{@link HemoBookModel}} animates. Each shares the part names that model reads.
 * Generated by tools/model_export/hemomancy_books.py; edit the rigs there and re-run it.
 */
public final class HemoBookLayers {{
	/** The meshes are built at twice block size for 32 px per block; draw them at this scale to restore it. */
	public static final float MODEL_SCALE = {uw.f(1 / SCALE)};
{fields}

	private HemoBookLayers() {{
	}}
{methods}}}
'''


def camel_upper(name):
    return name.upper()


# ---------------------------------------------------------------- preview

def posed_quads(bk, open_amount, flip1=0.0, flip2=1.0):
    """The rig at an animation pose, as HemoBookModel.setupAnim sets it."""
    f = math.degrees(1.25 * open_amount)
    rot = dict(left_lid=f, left_pages=f, right_lid=-f, right_pages=-f,
               flip_page1=f - 2 * f * flip1, flip_page2=f - 2 * f * flip2)
    saved = {p['name']: (p['rotation'], p['pivot'], [dict(c) for c in p['cubes']]) for p in bk.rig.parts}
    for p in bk.rig.parts:
        if p['name'] in rot:
            p['rotation'] = (0, rot[p['name']], 0)
        if p['name'] == 'spine':
            a = math.radians(f)
            p['pivot'] = (-bk.outer * SCALE * math.sin(a), 0, 0)
            for c in p['cubes']:
                x, y, z, w, h, d = c['box']
                c['box'] = (x, y, z * math.cos(a), w, h, d * math.cos(a))
    hidden = ('left_open', 'right_open') if open_amount < 0.5 else ('left_closed', 'right_closed')
    quads = uw.rig_quads(bk.rig, hidden)
    for p in bk.rig.parts:
        p['rotation'], p['pivot'], p['cubes'] = saved[p['name']]
    return quads


def preview(entries, images):
    tiles = []
    for bk, _ in entries:
        image = images[bk.name]
        tiles.append(uw.render_quads(posed_quads(bk, 0), image, 0.35, 0.2))
        tiles.append(uw.render_quads(posed_quads(bk, 0), image, 2.5, 0.25))
        tiles.append(uw.render_quads(posed_quads(bk, 1, 0.35, 0.8), image, math.pi / 2, 0.6))
        tiles.append(uw.render_quads(posed_quads(bk, 1, 0.5, 1.0), image, math.pi / 2 + 0.6, 0.25))
    cols = 4
    sheet = Image.new('RGB', (360 * cols, 360 * math.ceil(len(tiles) / cols)), (30, 32, 40))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % cols) * 360, (i // cols) * 360))
    out = ROOT / 'build/hemomancy_books_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    TEXTURES.mkdir(parents=True, exist_ok=True)
    entries, images = [], {}
    for build in BOOKS:
        bk = build()
        size = layout(bk.rig)
        image = paint_atlas(bk.rig, size, make_painter(bk))
        image.save(TEXTURES / f'{bk.name}.png')
        images[bk.name] = image
        entries.append((bk, size))
        print(f'{bk.name}: {sum(len(p["cubes"]) for p in bk.rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    JAVA.parent.mkdir(parents=True, exist_ok=True)
    JAVA.write_text(java_source(entries), encoding='utf-8')
    if args.preview:
        preview(entries, images)


if __name__ == '__main__':
    main()

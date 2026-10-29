"""Putriciel's burned and rotten materials for the mausoleum rework (Track 10): every hall alternates a charred band
with an overgrown one, and these are the blocks the two bands are made of.

    textures/block/packed_ash.png (+ _1, _2)          compacted grey-black ash, char flecks and pale bone grit
    textures/block/charred_planks.png                 burned planks: black char split by alligator checks
    textures/block/charred_log.png / _top.png         charred bark in deep alligator scales; a burned end grain
    textures/block/charred_bookshelf.png              a shelf burned half to ash: stumps, ash heaps, a few spines
    textures/block/sodden_mud_bricks.png (+ _1, _2)   the walls' mud bricks gone soft and wet, moss in the joints
    textures/block/spore_shelf.png / _bloom.png       the atlas of the bracket-fungus cluster, dim and in bloom

Packed ash has three layouts, each turned four ways at random per position (ash has no up). Sodden mud bricks have
three moss layouts picked at random per position, never turned, so the courses stay level. Charred planks feed
stairs and a slab; charred_log is a pillar (`axis`); the bookshelf's ends are charred planks, as vanilla's are oak.

The char is near black with a silvery sheen on the top edge of each block it has split into; the checks between
the blocks show the wood under the char as dull brown-grey. Nothing glows: the fire is long out. The mud bricks
keep vanilla mud_bricks' layout (two 8 px courses, half-offset head joints, a two-row bed joint) in darker, wetter
browns with gloss highlights, so a patch reads as the same wall gone soft.

The spore shelf is three overlapping shelf caps and a small fourth stepping out of a wall face, authored for
facing=north (the wall at z = 16, the caps growing toward -z); the blockstate turns it for the other three. Each
cap is stepped bands approximating a half disc, zoned ochre-grey on top; under each band a thin gill layer whose
faces are drawn full-bright (`shade: false`, block and sky light 15) in a faint green, and a few full-bright spore
motes hang beneath. `bloom=true` swaps the atlas for one with brighter gills and motes on the same UVs.

    python tools/model_export/putriciel_materials.py [--preview]

`--preview` writes build/model-previews/putriciel_materials.png (swatches beside the vanilla blocks they stand
for, tiled walls, iso blocks) and build/model-previews/spore_shelf.png / spore_shelf_bloom.png (turnarounds).
All noise is hashed from the texel, so a rerun writes the same pixels.
"""
import argparse
import copy
import math

from PIL import Image

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, ROOT, box, build, model_json, preview, preview_pair, separate, write
from hematic_iron_plating import iso
from seraphae_growth import mix, row, vanilla, variant, wrap_noise, write_json

TEXTURES = ASSETS / 'textures/block'
PREVIEW = ROOT / 'build/model-previews/putriciel_materials.png'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}


def tone(palette, v):
    """The palette entry nearest a fractional index, clamped to its ends."""
    return palette[max(0, min(len(palette) - 1, int(round(v))))]


def jitter(rgb, *key, amount=6):
    return clamp(rgb, (noise(*key) - .5) * amount)


# --- packed ash --------------------------------------------------------------------------------------------------

ASH = [(38, 36, 36), (46, 44, 44), (55, 53, 52), (64, 61, 59), (74, 71, 68), (85, 82, 78)]
FLECK = (22, 19, 19)              # charcoal
CHIP = (54, 44, 37)               # a charcoal chip's lit corner, the wood's brown in it
GRIT = (190, 182, 166)            # bone ground to grit
GRIT_DULL = (158, 150, 136)

# (clod seed, fleck seed, grit seed, flecks, grits)
ASH_LAYOUTS = [(81, 82, 83, 6, 4), (84, 85, 86, 5, 5), (87, 88, 89, 7, 3)]


def clod_field(seed, count=10):
    """Low pressed clods: each texel's nearest and second-nearest clod centre, wrapping at 16."""
    centres = [(noise(seed, 'cx', i) * 16, noise(seed, 'cy', i) * 16) for i in range(count)]

    def at(x, y):
        best = []
        for i, (cx, cy) in enumerate(centres):
            dx = (x + .5 - cx + 8) % 16 - 8
            dy = (y + .5 - cy + 8) % 16 - 8
            best.append((math.hypot(dx, dy), i))
        best.sort()
        return best[0][1], best[0][0], best[1][0]
    return at


def packed_ash(clod_seed, fleck_seed, grit_seed, flecks, grits):
    cell = clod_field(clod_seed)
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            i, d1, d2 = cell(x, y)
            # fine powder over soft pressed clods: the grain speckle carries most of the variation
            v = (2.6 + (noise(clod_seed, 'tone', i) - .5) * .8 + (wrap_noise(clod_seed, 4, 4, x, y) - .5) * 1.0
                 + (noise(clod_seed, 'grain', x, y) - .5) * 1.8)
            if d2 - d1 < .5:
                v -= 1.3                        # the faint seam where two clods were pressed together
            elif cell((x - 1) % 16, (y - 1) % 16)[0] != i:
                v += .6                         # the clod's upper-left lip catches the light
            rgb = tone(ASH, v)
            image.putpixel((x, y), rgb + (255,))
    for k in range(flecks):
        x, y = int(noise(fleck_seed, 'fx', k) * 16), int(noise(fleck_seed, 'fy', k) * 16)
        image.putpixel((x, y), FLECK + (255,))
        if noise(fleck_seed, 'long', k) < .5:
            dx, dy = (1, 0) if noise(fleck_seed, 'dir', k) < .6 else (0, 1)
            image.putpixel(((x + dx) % 16, (y + dy) % 16), FLECK + (255,))
        if noise(fleck_seed, 'chip', k) < .35:
            image.putpixel(((x - 1) % 16, y), CHIP + (255,))
    for k in range(grits):
        x, y = int(noise(grit_seed, 'gx', k) * 16), int(noise(grit_seed, 'gy', k) * 16)
        image.putpixel((x, y), (GRIT if noise(grit_seed, 'pale', k) < .6 else GRIT_DULL) + (255,))
        below = ((x + 1) % 16, (y + 1) % 16)
        if image.getpixel(below)[:3] != FLECK:
            image.putpixel(below, ASH[1] + (255,))
    return image


# --- char --------------------------------------------------------------------------------------------------------

CHAR = (36, 31, 30)               # the char's open face
CHAR_SHEEN = (60, 56, 58)         # the silvery sheen along a block's top edge
CHAR_SHADE = (24, 20, 20)
CHAR_GAP = (15, 12, 12)           # board gaps and the deepest checks
GRAIN = (76, 64, 55)              # the wood under the char, dull brown-grey
GRAIN_DEEP = (50, 41, 36)

# Planks: four 4 px boards (the bottom row of each a gap), one head joint each, staggered as vanilla's. The checks
# split each board into short char blocks; a check can stop short of a row. SPLITS run along a board's middle row.
PLANK_JOINTS = [5, 12, 2, 9]
PLANK_CHECKS = [(1, 10), (3, 8), (7, 13), (3, 14)]
CHECK_SPANS = ((0, 1, 2), (0, 1), (1, 2), (0, 1, 2))   # the rows a check opens across; some stop short
PLANK_SPLITS = {(0, 12), (0, 13), (2, 9), (2, 10), (3, 5), (3, 6)}


def char_block(seed, x, y, local, length, row, rows):
    """One texel of a char block: sheen on its top row, shade on its bottom row and right end, lit left end."""
    rgb = jitter(CHAR, seed, 'char', x, y, amount=6)
    if row == 0:
        rgb = mix(rgb, CHAR_SHEEN, .5 if 0 < local < length - 1 else .25)
    elif row == 1 and noise(seed, 'streak', y, (x + int(noise(seed, 'phase', y) * 5)) // 3) < .3:
        rgb = clamp(rgb, 5)                     # a faint streak of the grain along the board
    elif row == rows - 1:
        rgb = mix(rgb, CHAR_SHADE, .6)
    if local == length - 1:
        rgb = mix(rgb, CHAR_SHADE, .55)
    elif local == 0:
        rgb = clamp(rgb, 6)
    if row > 0 and noise(seed, 'silver', x, y) > .96:
        rgb = mix(rgb, CHAR_SHEEN, .8)
    return rgb


def check(seed, x, y, deep):
    """A check in the char: mostly a dark fissure, the wood showing dull brown-grey in some of it."""
    n = noise(seed, 'grain', x, y)
    if n < (.6 if deep else .4):
        return mix(CHAR_GAP, GRAIN_DEEP, n)
    if deep or n < .75:
        return jitter(GRAIN_DEEP, seed, 'deep', x, y, amount=6)
    return jitter(GRAIN, seed, 'grain_tone', x, y, amount=8)


def charred_planks(seed=91):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        board, row = divmod(y, 4)
        joint, checks = PLANK_JOINTS[board], PLANK_CHECKS[board]
        for x in range(16):
            if row == 3:
                rgb = jitter(CHAR_GAP, seed, 'gap', x, y, amount=8)
            elif x == joint:
                rgb = CHAR_GAP
            elif x in checks and row in CHECK_SPANS[int(noise(seed, 'span', board, x) * len(CHECK_SPANS))]:
                rgb = check(seed, x, y, deep=row == 0)
            elif row == 1 and (board, x) in PLANK_SPLITS:
                rgb = check(seed, x, y, deep=False)
            else:
                cuts = sorted({joint} | {c for c in checks if row in CHECK_SPANS[
                    int(noise(seed, 'span', board, c) * len(CHECK_SPANS))]})
                left = max([c for c in cuts if c < x], default=max(cuts) - 16)
                right = min([c for c in cuts if c > x], default=min(cuts) + 16)
                rgb = char_block(seed, x, y, x - left - 1, right - left - 1, row, 3)
            image.putpixel((x, y), rgb + (255,))
    return image


# Log bark: four 4 px columns of scales, a deep vertical check closing each column on the right; each column's
# horizontal checks are staggered from its neighbours' so the scales sit like burned brickwork. Every column's
# checks wrap at 16, so a pillar of logs shows no seam.
BARK = (33, 28, 27)
BARK_CHECK = (16, 13, 13)
LOG_ROWS = [(3, 10), (6, 13), (0, 8), (4, 11)]


def charred_log(seed=93):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            column, lx = divmod(x, 4)
            rows = LOG_ROWS[column]
            if lx == 3:
                rgb = check(seed, x, y, deep=True) if noise(seed, 'v', x, y) > .7 else BARK_CHECK
            elif y in rows:
                rgb = check(seed, x, y, deep=True) if lx == 1 and noise(seed, 'h', x, y) > .5 else BARK_CHECK
            else:
                above = max([r for r in rows if r < y], default=max(rows) - 16)
                below = min([r for r in rows if r > y], default=min(rows) + 16)
                height = below - above - 1
                rgb = jitter(BARK, seed, 'bark', x, y, amount=5)
                ly = y - above - 1
                if ly == 0:
                    rgb = mix(rgb, CHAR_SHEEN, .75 if lx < 2 else .45)
                elif ly == height - 1:
                    rgb = mix(rgb, CHAR_SHADE, .65)
                if lx == 0:
                    rgb = clamp(rgb, 7)
                elif lx == 2:
                    rgb = mix(rgb, CHAR_SHADE, .5)
                if ly > 0 and noise(seed, 'silver', x, y) > .96:
                    rgb = mix(rgb, CHAR_SHEEN, .85)
            image.putpixel((x, y), rgb + (255,))
    return image


END = [(70, 58, 50), (57, 47, 41)]   # the end grain's rings, charred to dull grey-brown
PITH = (40, 33, 30)
END_CHECK = (22, 18, 17)
END_CHECKS = (25, 118, 200, 292)       # radial checks opened by the fire, degrees


def charred_log_top(seed=94):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                rgb = jitter(BARK, seed, 'rim', x, y, amount=8)
                if (x + y) % 3 == 0:
                    rgb = mix(rgb, CHAR_SHEEN, .5)
                elif (x * 7 + y) % 5 == 0:
                    rgb = BARK_CHECK
                image.putpixel((x, y), rgb + (255,))
                continue
            dx, dy = x - 7.5, y - 7.5
            r = max(abs(dx), abs(dy)) * .75 + math.hypot(dx, dy) * .25
            ring = int(r)
            rgb = PITH if r < 1.2 else jitter(END[ring % 2], seed, 'ring', x, y, amount=6)
            rgb = mix(rgb, CHAR, (r - 4.6) / 2.2)          # burned black toward the bark
            angle = math.degrees(math.atan2(dy, dx)) % 360
            for k, a in enumerate(END_CHECKS):
                gap = abs((angle - a + 180) % 360 - 180)
                reach = 6.0 - noise(seed, 'reach', k) * 2.5
                if 2.2 < r < reach and math.radians(gap) * r < .45:
                    rgb = END_CHECK
            image.putpixel((x, y), rgb + (255,))
    return image


# --- the burned bookshelf ----------------------------------------------------------------------------------------

SOOT = (17, 14, 14)               # the back of the shelf
SOOT_DEEP = (11, 9, 9)            # in under the board above
STUMP = (30, 26, 26)
STUMP_ASH = (104, 100, 96)
HEAP = [(80, 76, 73), (100, 96, 92), (128, 124, 118)]
SPINES = {'red': (100, 42, 36), 'olive': (76, 76, 44), 'leather': (84, 58, 40)}
GILT = (150, 126, 84)             # a faded title band
HEAP_PROFILE = {2: (1, 1), 3: (1, 2, 1), 4: (1, 2, 2, 1)}

# Each shelf is 14 px wide, 6 rows high. ('spine', width, colour, height), ('stump', width, height),
# ('heap', width), ('gap', width).
SHELVES = [
    [('spine', 2, 'red', 6), ('stump', 1, 2), ('stump', 2, 3), ('heap', 4), ('gap', 1), ('spine', 2, 'olive', 4),
     ('stump', 1, 1), ('gap', 1)],
    [('stump', 2, 2), ('heap', 3), ('spine', 1, 'leather', 5), ('spine', 2, 'red', 3), ('stump', 1, 3),
     ('heap', 3), ('stump', 2, 1)],
]
SHELF_BOTTOMS = (6, 14)


def charred_bookshelf(seed=95):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            if y in (0, 8):
                rgb = mix(jitter(CHAR, seed, 'frame', x, y), CHAR_SHEEN, .55)
            elif y in (7, 15):
                rgb = jitter(CHAR_SHADE, seed, 'frame', x, y, amount=4)
            elif x == 0:
                rgb = mix(jitter(CHAR, seed, 'side', x, y), CHAR_SHEEN, .25)
            elif x == 15:
                rgb = jitter(CHAR_SHADE, seed, 'side', x, y, amount=4)
            else:
                rgb = SOOT_DEEP if y in (1, 9) else jitter(SOOT, seed, 'back', x, y, amount=4)
            if y in (0, 8) and x in (4, 11):
                rgb = GRAIN_DEEP                                           # a check in the board
            image.putpixel((x, y), rgb + (255,))
    for shelf, bottom in zip(SHELVES, SHELF_BOTTOMS):
        x0 = 1
        for slot in shelf:
            kind, width = slot[0], slot[1]
            for i in range(width):
                x = x0 + i
                if kind == 'stump':
                    h = max(1, slot[2] - (noise(seed, 'stump', x, bottom) < .35))
                    for k in range(h):
                        y = bottom - k
                        rgb = jitter(STUMP, seed, 'stump', x, y)
                        if i == 0:
                            rgb = mix(rgb, CHAR_SHEEN, .35)
                        if k == h - 1:
                            rgb = jitter(STUMP_ASH, seed, 'cap', x, y, amount=10)
                        image.putpixel((x, y), rgb + (255,))
                elif kind == 'heap':
                    h = HEAP_PROFILE[width][i]
                    for k in range(h):
                        y = bottom - k
                        rgb = HEAP[2] if k == h - 1 else HEAP[1] if k else HEAP[0]
                        if i == width - 1:
                            rgb = clamp(rgb, -14)
                        image.putpixel((x, y), jitter(rgb, seed, 'heap', x, y) + (255,))
                elif kind == 'spine':
                    colour, full = SPINES[slot[2]], slot[3]
                    h = full - (full < 6 and noise(seed, 'ragged', x, bottom) < .4)
                    for k in range(h):
                        y = bottom - k
                        rgb = jitter(colour, seed, 'spine', x, y)
                        if i == 0:
                            rgb = clamp(rgb, 10)
                        elif i == width - 1:
                            rgb = clamp(rgb, -16)
                        if width == 2 and k == 2:
                            rgb = mix(rgb, GILT, .35)
                        if k == h - 1:
                            rgb = mix(rgb, CHAR, .75)                      # scorched from the top
                        elif k == h - 2:
                            rgb = mix(rgb, CHAR, .35)
                        image.putpixel((x, y), rgb + (255,))
            x0 += width
    return image


# --- sodden mud bricks -------------------------------------------------------------------------------------------

MUD = [(50, 38, 32), (59, 45, 37), (69, 53, 43), (80, 62, 49), (93, 73, 57)]
GLOSS = (128, 108, 90)            # wet sheen
JOINT = (42, 33, 28)
JOINT_DEEP = (30, 24, 21)
MOSS = [(50, 66, 34), (62, 82, 40), (76, 100, 46)]
ROOTLET = (104, 84, 58)
ROOTLET_SHADE = (78, 60, 44)

# Vanilla mud_bricks' layout: head joints of each course, the two-row bed joint at the course's rows 6 and 7.
MUD_JOINTS = [(0, 8), (4, 12)]
# (moss seed, moss threshold, rootlet paths): rootlets hang from the joints down over the brick below.
MUD_LAYOUTS = [
    (71, .64, [[(2, 7), (3, 8)], [(10, 15), (11, 0)]]),
    (72, .57, [[(5, 9), (5, 10), (6, 11)]]),
    (73, .5, [[(8, 1), (9, 2)], [(1, 10), (1, 11)]]),
]


def sodden_mud_bricks(moss_seed, threshold, rootlets, seed=97):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        course, r = divmod(y, 8)
        joints = MUD_JOINTS[course]
        for x in range(16):
            moss = wrap_noise(moss_seed, 4, 2, x, y) * .8 + noise(moss_seed, 'tuft', x, y) * .2
            if r >= 6 or x in joints:
                rgb = JOINT_DEEP if r == 7 else jitter(JOINT, seed, 'joint', x, y, amount=6)
                sag = r == 6 and x not in joints and noise(seed, 'sag', x, course) < .3
                if sag:
                    rgb = MUD[1]                                           # the soft brick slumps into the joint
                elif moss > threshold:
                    rgb = MOSS[2] if r == 6 and moss > threshold + .1 else MOSS[1] if r == 6 else MOSS[0]
                    rgb = jitter(rgb, moss_seed, 'moss', x, y)
                image.putpixel((x, y), rgb + (255,))
                continue
            left = max([j for j in joints if j < x], default=max(joints) - 16)
            lx = x - left - 1
            brick = (course, left)
            v = (2.0 + (noise(seed, 'brick', *brick) - .5) * 1.2 + (wrap_noise(seed, 4, 4, x, y) - .5) * 1.2
                 + (noise(seed, 'grain', x, y) - .5) * 1.0)
            v += 1.2 if r == 0 else -1.2 if r == 5 else 0
            v += .6 if lx == 0 else -1.0 if lx == 6 else 0
            rgb = tone(MUD, v)
            glint = noise(seed, 'glint', *brick)
            if r == 1 and lx in (1 + int(glint * 3), 2 + int(glint * 3)):
                rgb = mix(rgb, GLOSS, .8 if lx == 1 + int(glint * 3) else .45)   # a wet highlight under the lip
            elif r == 5 and moss > threshold + .14:
                rgb = jitter(MOSS[1], moss_seed, 'creep', x, y)            # moss creeping up from the joint
            image.putpixel((x, y), rgb + (255,))
    for path in rootlets:
        for i, (x, y) in enumerate(path):
            x, y = x % 16, y % 16
            image.putpixel((x, y), jitter(ROOTLET, seed, 'root', x, y, amount=10) + (255,))
            if i == len(path) - 1:
                below = (x, (y + 1) % 16)
                image.putpixel(below, mix(image.getpixel(below)[:3], ROOTLET_SHADE, .6) + (255,))
    return image


# --- the spore shelf ---------------------------------------------------------------------------------------------

SHELF = 'spore_shelf'
CAP = [(88, 80, 64), (106, 96, 74), (124, 111, 84), (146, 132, 98)]   # ochre-grey, back to rim
CAP_UNDER = (74, 66, 52)
MYCELIUM = (98, 92, 78)
ROT_STAIN = (72, 70, 50)
# Full-bright parts: (gill, gill gap, gill edge, mote). Dim is faint enough to read as a glow, not a lamp.
GLOW = {False: ((84, 112, 64), (42, 54, 38), (112, 150, 80), (130, 168, 100)),
        True: ((146, 206, 106), (70, 98, 56), (186, 236, 130), (210, 250, 170))}

# Caps: (centre x, underside y, half width at the wall, depth out from the wall). Bigger low, smaller high, so the
# cluster steps up and out of the face.
CAPS = [(6.5, 5.0, 5.5, 8.0), (11.0, 9.5, 4.0, 6.0), (4.5, 12.5, 3.0, 4.5), (12.5, 2.5, 2.5, 3.5)]
BANDS = (0.0, .45, .78, 1.0)        # band edges as fractions of the depth, wall first
BAND_THICK = (2.0, 1.5, 1.0)      # wall band to rim band: the cap thins toward its rim
GILL_THICK = .5
MOTES = [(5.0, 3.0, 11.0), (8.5, 1.75, 9.5), (12.0, 7.75, 12.0), (4.0, 10.75, 13.5), (9.5, 4.0, 13.0)]


def cap(index, cx, y, half, depth):
    """A shelf cap: stepped bands narrowing toward its rim, a low dome at the back, gills under every band."""
    e = []
    for b in range(3):
        z_out, z_in = 16 - depth * BANDS[b + 1], 16 - depth * BANDS[b]
        mid = (BANDS[b] + BANDS[b + 1]) / 2
        w = half * math.sqrt(max(.15, 1 - mid * mid))
        band = f'cap{b + 1}'                       # cap1 is the band at the wall, cap3 the palest, the rim
        e.append(box(f'cap{index}_band{b}', (cx - w, y, z_out), (cx + w, y + BAND_THICK[b], z_in), band,
                     down='under'))
        inset = .5
        e.append(box(f'cap{index}_gills{b}', (cx - w + inset, y - GILL_THICK, z_out + (inset if b == 2 else 0)),
                     (cx + w - inset, y, z_in), 'gill', skip=('up',), extra=BRIGHT))
    dome_w = half * .7
    e.append(box(f'cap{index}_dome', (cx - dome_w, y + BAND_THICK[0], 16 - depth * .3),
                 (cx + dome_w, y + BAND_THICK[0] + .5, 16), 'cap2', skip=('down',)))
    # where the cap leaves the wall, a low collar of mycelium on the stone, narrower than the cap
    pad = half * .75
    e.append(box(f'cap{index}_pad', (cx - pad, y - 1.0, 15.5), (cx + pad, y + BAND_THICK[0] + .5, 16), 'mycelium'))
    return e


def spore_shelf():
    e = []
    for i, (cx, y, half, depth) in enumerate(CAPS):
        e += cap(i, cx, y, half, depth)
    for i, (x, y, z) in enumerate(MOTES):
        e.append(box(f'mote{i}', (x - .25, y - .25, z - .25), (x + .25, y + .25, z + .25), 'mote', extra=BRIGHT))
    return e


def cap_painter(level):
    """A cap band's top is zoned like a bracket fungus: rings parallel to the rim, the rim the palest."""
    def paint(face, lx, ly, w, h, key):
        n = noise('cap', key, face, lx, ly)
        if face == 'up':
            v = level + (.6 if ly == 0 and level == 3 else 0) - (ly % 2) * .5 + (n - .5) * .8
            rgb = tone(CAP, v)
            if noise('stain', key, lx // 2, ly) < .08:
                rgb = mix(rgb, ROT_STAIN, .5)
        else:
            # the cap's edge: a pale rim on top, the underside's colour below
            rgb = CAP[min(3, level + 1)] if ly == 0 else CAP_UNDER
        return clamp(rgb, int(n * 6) - 3) + (255,)
    return paint


def under(face, lx, ly, w, h, key):
    return clamp(CAP_UNDER, int(noise('under', key, lx, ly) * 6) - 3) + (255,)


def mycelium(face, lx, ly, w, h, key):
    n = noise('mycelium', key, face, lx, ly)
    rgb = mix(MYCELIUM, ROT_STAIN, .55) if n < .25 else MYCELIUM
    return clamp(rgb, int(n * 8) - 4 - (8 if face != 'north' else 0)) + (255,)


def glow_painters(bloom):
    gill_line, gill_gap, gill_edge, mote_rgb = GLOW[bloom]

    def gill(face, lx, ly, w, h, key):
        if face == 'down':
            # gill blades run from the wall to the rim (along v); the outermost texel is the green edge
            rgb = gill_edge if ly == 0 or lx in (0, w - 1) else gill_line if lx % 2 else gill_gap
        else:
            rgb = gill_edge
        return clamp(rgb, int(noise('gill', key, face, lx, ly) * 8) - 4) + (255,)

    def mote(face, lx, ly, w, h, key):
        return mote_rgb + (255,)
    return {'gill': gill, 'mote': mote}


def shelf_special(bloom):
    special = {f'cap{i}': cap_painter(i) for i in range(4)}
    special.update(under=under, mycelium=mycelium, **glow_painters(bloom))
    return special


SHELF_FACING = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def shelf_blockstate():
    variants = {}
    for facing, y in SHELF_FACING.items():
        for bloom in (False, True):
            model = {'model': f'hemomancy:block/{SHELF}{"_bloom" if bloom else ""}'}
            if y:
                model['y'] = y
            variants[f'bloom={str(bloom).lower()},facing={facing}'] = model
    return {'variants': variants}


def export_shelf():
    elements = spore_shelf()
    moved = separate(elements)
    atlases = {}
    for bloom in (False, True):
        atlas, models = build({'block': copy.deepcopy(elements)}, shelf_special(bloom), atlas_w=64)
        name = SHELF + ('_bloom' if bloom else '')
        atlas.save(TEXTURES / f'{name}.png')
        atlases[bloom] = (atlas, models['block'])
    atlas, block = atlases[False]
    model = model_json(block, SHELF, atlas.height, f'hemomancy:block/{SHELF}')
    model['texture_size'] = [64, atlas.height]
    bloom_model = copy.deepcopy(model)
    bloom_model['textures'] = {'atlas': f'hemomancy:block/{SHELF}_bloom', 'particle': f'hemomancy:block/{SHELF}'}
    write({f'block/{SHELF}': model, f'block/{SHELF}_bloom': bloom_model}, SHELF, model)
    write_json(ASSETS / f'blockstates/{SHELF}.json', shelf_blockstate())
    write_json(ASSETS / f'models/item/{SHELF}.json', {'parent': f'hemomancy:block/{SHELF}'})
    lo = [min(el['from'][a] for el in block) for a in range(3)]
    hi = [max(el['to'][a] for el in block) for a in range(3)]
    print(f'wrote spore_shelf: {len(elements)} cubes, {moved} faces separated, 64x{atlas.height} atlas, '
          f'extent {lo} .. {hi} (facing north)')
    return block


# --- block models ------------------------------------------------------------------------------------------------

def tex(name):
    return f'hemomancy:block/{name}'


def cube_all(name):
    return {'parent': 'minecraft:block/cube_all', 'textures': {'all': tex(name)}}


def random_blockstate(name, count, turns=(0,)):
    options = []
    for i in range(count):
        for y in turns:
            option = {'model': tex(variant(name, i))}
            if y:
                option['y'] = y
            options.append(option)
    return {'variants': {'': options}}


STAIR_SHAPES = {'straight': '', 'inner_left': '_inner', 'inner_right': '_inner', 'outer_left': '_outer',
                'outer_right': '_outer'}


def stairs_blockstate(name):
    """Vanilla's stairs blockstate (oak_stairs.json) for `name`."""
    variants = {}
    for facing, y0 in {'east': 0, 'north': 270, 'south': 90, 'west': 180}.items():
        for half in ('bottom', 'top'):
            for shape, suffix in STAIR_SHAPES.items():
                left = shape.endswith('left')
                if half == 'bottom':
                    y = y0 - 90 if left else y0
                else:
                    y = y0 + 90 if shape != 'straight' and not left else y0
                y %= 360
                option = {'model': tex(name + suffix)}
                if half == 'top':
                    option['x'] = 180
                if y:
                    option['y'] = y
                if half == 'top' or y:
                    option['uvlock'] = True
                variants[f'facing={facing},half={half},shape={shape}'] = option
    return {'variants': variants}


def export_blocks():
    images = {}
    for i, layout in enumerate(ASH_LAYOUTS):
        images[variant('packed_ash', i)] = packed_ash(*layout)
    for i, layout in enumerate(MUD_LAYOUTS):
        images[variant('sodden_mud_bricks', i)] = sodden_mud_bricks(*layout)
    images['charred_planks'] = charred_planks()
    images['charred_log'] = charred_log()
    images['charred_log_top'] = charred_log_top()
    images['charred_bookshelf'] = charred_bookshelf()
    for name, image in images.items():
        image.save(TEXTURES / f'{name}.png')

    models, states, items = {}, {}, {}
    for name, layouts, turns in (('packed_ash', ASH_LAYOUTS, (0, 90, 180, 270)),
                                 ('sodden_mud_bricks', MUD_LAYOUTS, (0,))):
        for i in range(len(layouts)):
            models[variant(name, i)] = cube_all(variant(name, i))
        states[name] = random_blockstate(name, len(layouts), turns)
        items[name] = name
    planks = {'bottom': tex('charred_planks'), 'top': tex('charred_planks'), 'side': tex('charred_planks')}
    models['charred_planks'] = cube_all('charred_planks')
    states['charred_planks'] = {'variants': {'': {'model': tex('charred_planks')}}}
    items['charred_planks'] = 'charred_planks'
    for suffix, parent in (('', 'stairs'), ('_inner', 'inner_stairs'), ('_outer', 'outer_stairs')):
        models[f'charred_planks_stairs{suffix}'] = {'parent': f'minecraft:block/{parent}', 'textures': planks}
    states['charred_planks_stairs'] = stairs_blockstate('charred_planks_stairs')
    items['charred_planks_stairs'] = 'charred_planks_stairs'
    models['charred_planks_slab'] = {'parent': 'minecraft:block/slab', 'textures': planks}
    models['charred_planks_slab_top'] = {'parent': 'minecraft:block/slab_top', 'textures': planks}
    states['charred_planks_slab'] = {'variants': {'type=bottom': {'model': tex('charred_planks_slab')},
                                                  'type=double': {'model': tex('charred_planks')},
                                                  'type=top': {'model': tex('charred_planks_slab_top')}}}
    items['charred_planks_slab'] = 'charred_planks_slab'
    log = {'end': tex('charred_log_top'), 'side': tex('charred_log')}
    models['charred_log'] = {'parent': 'minecraft:block/cube_column', 'textures': log}
    models['charred_log_horizontal'] = {'parent': 'minecraft:block/cube_column_horizontal', 'textures': log}
    states['charred_log'] = {'variants': {'axis=x': {'model': tex('charred_log_horizontal'), 'x': 90, 'y': 90},
                                          'axis=y': {'model': tex('charred_log')},
                                          'axis=z': {'model': tex('charred_log_horizontal'), 'x': 90}}}
    items['charred_log'] = 'charred_log'
    models['charred_bookshelf'] = {'parent': 'minecraft:block/cube_column',
                                   'textures': {'end': tex('charred_planks'), 'side': tex('charred_bookshelf')}}
    states['charred_bookshelf'] = {'variants': {'': {'model': tex('charred_bookshelf')}}}
    items['charred_bookshelf'] = 'charred_bookshelf'

    for name, model in models.items():
        write_json(ASSETS / f'models/block/{name}.json', model)
    for name, state in states.items():
        write_json(ASSETS / f'blockstates/{name}.json', state)
    for name, parent in items.items():
        write_json(ASSETS / f'models/item/{name}.json', {'parent': tex(parent)})
    print(f'wrote {len(images)} textures, {len(models)} block models, {len(states)} blockstates')
    return images


# --- preview -----------------------------------------------------------------------------------------------------

def scattered(layouts, cols, rows, scale, turns, seed):
    """A floor or wall of random layouts, each turned by a random multiple of 90 degrees when `turns`."""
    t = 16 * scale
    sheet = Image.new('RGBA', (cols * t, rows * t))
    for r in range(rows):
        for c in range(cols):
            image = layouts[int(noise('pick', seed, c, r) * len(layouts))]
            if turns:
                image = image.rotate(90 * int(noise('turn', seed, c, r) * 4))
            sheet.paste(image.resize((t, t), Image.NEAREST), (c * t, r * t))
    return sheet


def bands(images, scale=4):
    """A wall in Putriciel's bands: charred courses over sodden ones, an ash floor at the foot."""
    t = 16 * scale
    cols = 8
    stack = ['charred_log', 'charred_planks', 'charred_bookshelf', 'sodden', 'sodden', 'ash']
    sheet = Image.new('RGBA', (cols * t, len(stack) * t))
    for r, kind in enumerate(stack):
        for c in range(cols):
            if kind == 'sodden':
                image = images[variant('sodden_mud_bricks', int(noise('band', r, c) * 3))]
            elif kind == 'ash':
                image = images[variant('packed_ash', int(noise('band', r, c) * 3))].rotate(90 * (c % 4))
            elif kind == 'charred_log':
                image = images['charred_log'] if c % 3 == 0 else images['charred_planks']
            else:
                image = images[kind]
            sheet.paste(image.resize((t, t), Image.NEAREST), (c * t, r * t))
    return sheet


def preview_blocks(images):
    gap, bg = 12, (36, 32, 34, 255)
    sw = 16 * 6
    swatch_names = ['mud_bricks', 'sodden_mud_bricks', 'sodden_mud_bricks_1', 'sodden_mud_bricks_2', 'soul_soil',
                    'packed_ash', 'packed_ash_1', 'packed_ash_2', 'dark_oak_planks', 'charred_planks', 'dark_oak_log',
                    'charred_log', 'charred_log_top', 'bookshelf', 'charred_bookshelf']
    swatches = row([(images.get(n) or vanilla(n)).resize((sw, sw), Image.NEAREST) for n in swatch_names], gap, bg)
    ash = [images[variant('packed_ash', i)] for i in range(3)]
    mud = [images[variant('sodden_mud_bricks', i)] for i in range(3)]
    walls = row([scattered(ash, 5, 5, 5, True, 1), scattered(mud, 5, 5, 5, False, 2),
                 scattered([images['charred_planks']], 3, 3, 5, False, 3),
                 scattered([images['charred_log']], 3, 3, 5, False, 4), bands(images)], gap, bg)
    p, lg, lt, bs = images['charred_planks'], images['charred_log'], images['charred_log_top'], images['charred_bookshelf']
    isos = row([iso(ash[0], ash[1], ash[2]), iso(mud[0], mud[1], mud[2]), iso(p, p, p), iso(p, p, p, shape='stairs'),
                iso(p, p, p, shape='slab'), iso(lt, lg, lg), iso(p, bs, bs), iso(vanilla('mud_bricks'),
                vanilla('mud_bricks'), vanilla('mud_bricks'))], gap, bg)
    parts = [swatches, walls, isos]
    sheet = Image.new('RGBA', (max(x.width for x in parts) + 2 * gap, sum(x.height for x in parts) + 4 * gap), bg)
    y = gap
    for part in parts:
        sheet.paste(part, (gap, y), part)
        y += part.height + gap
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW)
    print('wrote', PREVIEW)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    images = export_blocks()
    block = export_shelf()
    if args.preview:
        preview_blocks(images)
        preview(copy.deepcopy(block), SHELF, 'Spore Shelf - facing north (wall behind), dim atlas', SHELF, lift=-1)
        preview_pair((copy.deepcopy(block), SHELF), (copy.deepcopy(block), f'{SHELF}_bloom'),
                     'Spore Shelf - dim (top) and bloom (bottom) atlases', f'{SHELF}_bloom', lift=-1)


if __name__ == '__main__':
    main()

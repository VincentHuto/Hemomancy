"""The Blood Temple (data/hemomancy/structure/blood_temple.nbt): the Hematic Order's wayside shrine, where a Harbinger
Hermit keeps the heart he offered (the Mortal Display) for the one he will invite. Most players meet the Order here
first, so it is the Order at its most deliberate: a stepped podium, a columned portico, a vaulted nave whose blood
gutter runs from the door to the heart, and the heart on its dais in the apse.

Authored along x, the door in the west, the apse in the east (the structure turns it):

    x0-2    the steps up the podium, a lit brazier on a plinth either side at their head.
    x3-7    the forecourt and portico: four hematic iron columns with bone capitals under a tiled deck and a bone
            balustrade.
    x8      the west front: an arched door under a rose of sanguine glass; the bell-cote astride the gable above.
    x9-22   the nave: deepslate-tile floor, the gutter down the middle (dry at the door, brimming at the chancel),
            lacquer kneelers either side, bone pilasters with crimson sconces, sanguine lancets, a plating barrel vault
            ribbed in bone. The Cardinal Focus stands before the chancel on a ring of venous inlay. North of the nave,
            the Hermit's cell (bedroll, lectern, the temple's chest); south, the reliquary chapel.
    x23-28  the apse, behind a bone chancel arch: the dais with the Mortal Display facing the door, two brimming basins,
            iron columns before a reredos of plating and bone, the east rose above it.

The Mortal Display, the Cardinal Focus and the Hermit's marker (`minecraft:light[level=11]`) lie within seven blocks
of the anchor jigsaw under the Focus on both axes; BloodTempleStructure starts the temple with that jigsaw on the
chunk's middle, so all three land in one chunk and its afterPlace links them in a single pass.

Row 0 is the footing course and lands on the surface (project_start_to_heightmap); the podium floor is row 3.
"""
from .kit import (AIR, BONE, BONE_CHISELED, BONE_STAIRS, BONE_WALL, COBBLED, CUT, CUT_CHISELED, CUT_SMOOTH,
                  CUT_STAIRS, GLASS, IRON_CHISELED, LAC, LAC_STAIRS, PACKED_ASH, PILLAR, PLANKS, PLATING,
                  PLATING_SLAB, PLATING_STAIRS, POLISHED, POLISHED_STAIRS, TILE, TILE_STAIRS, VENOUS, Piece,
                  bone_field, deep_field, finish, gable_roof, h, quoin, slab, stairs, tile_floor)

LOOT = 'hemomancy:chests/blood_temple'
SIZE = (31, 24, 23)
ZC = 11                 # the temple's centre line
FLOOR = 3               # the podium and nave floor
HALF = 4                # the nave's open half width (z 7..15)
NAVE = (9, 22)          # the nave's open x span; its walls are x8 and x23, z6 and z16
APSE = (24, 27)         # the apse's open x span; its walls are x23 (shared) and x28, z7 and z15
WALL_TOP = 10           # the cornice course of the nave and apse
SIDE_TOP = FLOOR + 6    # the side rooms' ceiling
FOCUS = (20, FLOOR, ZC)
DISPLAY = (26, FLOOR + 3, ZC)
HERMIT = (19, FLOOR + 1, ZC + 2)
PILASTERS = tuple(range(NAVE[0] + 1, NAVE[1] + 1, 3))


def build():
    p = Piece('blood_temple', SIZE)
    ground(p)
    podium(p)
    side_room(p, 3, ZC - HALF - 1, 'cell')
    side_room(p, SIZE[2] - 4, ZC + HALF + 1, 'chapel')
    nave(p)
    apse(p)
    portico(p)
    roofs(p)
    nave_vault(p)
    apse_vault(p)
    rose(p, NAVE[0] - 1, FLOOR + 9, ZC)
    rose(p, APSE[1] + 1, FLOOR + 5, ZC)
    bell_cote(p)
    cell(p)
    chapel(p)
    furnish(p)
    finish(p)
    # The anchor contract: whatever the rotation, all three stay within the anchor's chunk when it sits on the middle.
    for name, (x, _, z) in (('heart', DISPLAY), ('Focus', FOCUS), ('Hermit', HERMIT)):
        if abs(x - FOCUS[0]) > 7 or abs(z - FOCUS[2]) > 7:
            raise ValueError('the %s at %s is more than seven blocks from the anchor' % (name, (x, z)))
    return p


# --- the ground and the podium -------------------------------------------------------------------------------------------

def ground(p):
    """An irregular venous apron round the podium; outside it the template leaves the terrain alone."""
    sx, sy, sz = SIZE
    for x in range(sx):
        for z in range(sz):
            d = max(abs(z - ZC) / 11.5, abs(x - 15) / 15.5) + (h('apron', x, z) - 0.5) * 0.12
            if d > 1.0:
                continue
            r = h('ground', x, z)
            p.set(x, 0, z, VENOUS if r < 0.25 else PACKED_ASH if r < 0.35 else 'minecraft:coarse_dirt'
                  if r < 0.65 else 'minecraft:rooted_dirt')
            for y in range(1, sy):
                p.set(x, y, z, AIR)


def podium(p):
    """The podium: x3..29, z3..19, three courses of deepslate under a polished cornice, steps up the west front."""
    x0, x1, z0, z1 = 3, 29, 3, 19
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            p.set(x, 0, z, COBBLED)
            edge = x in (x0, x1) or z in (z0, z1)
            for y in (1, 2):
                p.set(x, y, z, deep_field(p, x, y, z, 'podium') if edge else COBBLED)
            p.set(x, FLOOR, z, tile_floor(p, x, FLOOR, z))
    for x, z in ((x0, z0), (x0, z1), (x1, z0), (x1, z1)):
        for y in (1, 2):
            p.set(x, y, z, quoin(x, y, z))
    for x in range(x0, x1 + 1):
        p.set(x, 1, z0 - 1, POLISHED_STAIRS, stairs('south'))
        p.set(x, 1, z1 + 1, POLISHED_STAIRS, stairs('north'))
    for z in range(z0, z1 + 1):
        p.set(x1 + 1, 1, z, POLISHED_STAIRS, stairs('west'))
    # The west steps: three flights, the middle one in venous stone, between polished cheeks.
    for x in (0, 1, 2):
        for z in range(z0, z1 + 1):
            for y in range(0, x + 1):
                p.set(x, y, z, COBBLED)
            p.set(x, x + 1, z, CUT_STAIRS if ZC - 2 <= z <= ZC + 2 else TILE_STAIRS, stairs('east'))
    for z in (z0, z1):
        for x in (0, 1, 2):
            for y in range(0, x + 2):
                p.set(x, y, z, POLISHED if y == x + 1 else COBBLED)
    # The forecourt: the walk to the door in venous brick, a polished border.
    for x in range(3, 8):
        for z in range(z0, z1 + 1):
            if ZC - 2 <= z <= ZC + 2:
                p.set(x, FLOOR, z, CUT_CHISELED if z == ZC and x % 2 == 1 else CUT)
            elif z in (z0, z1):
                p.set(x, FLOOR, z, POLISHED)


def walls(p, x0, x1, z0, z1, top, field):
    """A wall ring (x0..x1, z0..z1) from the floor to `top`: a polished deepslate base course, the field, a smooth venous top
    course, quoined corners."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x not in (x0, x1) and z not in (z0, z1):
                continue
            for y in range(FLOOR + 1, top + 1):
                if x in (x0, x1) and z in (z0, z1):
                    block = quoin(x, y, z)
                elif y == FLOOR + 1:
                    block = POLISHED
                elif y == top:
                    block = CUT_SMOOTH
                else:
                    block = field(x, y, z)
                p.set(x, y, z, block)


# --- the nave ------------------------------------------------------------------------------------------------------------

def nave(p):
    x0, x1 = NAVE[0] - 1, NAVE[1] + 1
    z0, z1 = ZC - HALF - 1, ZC + HALF + 1
    walls(p, x0, x1, z0, z1, WALL_TOP, lambda x, y, z: deep_field(p, x, y, z, 'nave'))
    for x in range(NAVE[0], NAVE[1] + 1):
        for z in range(ZC - HALF, ZC + HALF + 1):
            p.set(x, FLOOR, z, CUT_SMOOTH if abs(z - ZC) == HALF else tile_floor(p, x, FLOOR, z, 'nave'))
            for y in range(FLOOR + 1, WALL_TOP + 1):
                p.set(x, y, z, AIR)
    # A bone dado and cornice, bone pilasters every three blocks.
    for x in range(NAVE[0], NAVE[1] + 1):
        for z in (z0, z1):
            p.set(x, FLOOR + 3, z, BONE)
            p.set(x, WALL_TOP, z, BONE)
    for x in PILASTERS:
        for z in (z0, z1):
            p.set(x, FLOOR + 1, z, CUT)
            for y in range(FLOOR + 2, WALL_TOP):
                p.set(x, y, z, BONE)
            p.set(x, WALL_TOP, z, BONE_CHISELED)
    # Lancets between the pilasters: two tall of sanguine glass on a bone sill under a bone lintel.
    for x in range(NAVE[0] + 2, NAVE[1], 3):
        if 14 <= x <= 16:
            continue
        for z in (z0, z1):
            for y in (FLOOR + 5, FLOOR + 6):
                p.set(x, y, z, GLASS)
            p.set(x, FLOOR + 4, z, BONE)
            p.set(x, FLOOR + 7, z, BONE_CHISELED)
    # The west door, three wide and three tall under a bone arch.
    for z in range(ZC - 1, ZC + 2):
        for y in range(FLOOR + 1, FLOOR + 4):
            p.set(x0, y, z, AIR)
        p.set(x0, FLOOR, z, CUT_CHISELED if z == ZC else CUT)
    for z in (ZC - 2, ZC + 2):
        for y in range(FLOOR + 1, FLOOR + 5):
            p.set(x0, y, z, BONE if y < FLOOR + 4 else BONE_CHISELED)
    p.set(x0, FLOOR + 4, ZC - 1, BONE_STAIRS, stairs('north', 'top'))
    p.set(x0, FLOOR + 4, ZC, AIR)
    p.set(x0, FLOOR + 4, ZC + 1, BONE_STAIRS, stairs('south', 'top'))
    p.set(x0, FLOOR + 5, ZC, BONE_CHISELED)
    p.set(x0, FLOOR + 5, ZC - 1, BONE)
    p.set(x0, FLOOR + 5, ZC + 1, BONE)
    # Side doors into the cell (north) and the chapel (south), each under a bone arch.
    for z in (z0, z1):
        for xx in (14, 15, 16):
            for y in range(FLOOR + 1, FLOOR + 4):
                p.set(xx, y, z, AIR)
            p.set(xx, FLOOR, z, CUT)
        p.set(14, FLOOR + 4, z, BONE_STAIRS, stairs('west', 'top'))
        p.set(15, FLOOR + 4, z, AIR)
        p.set(16, FLOOR + 4, z, BONE_STAIRS, stairs('east', 'top'))
        p.set(15, FLOOR + 5, z, BONE_CHISELED)
    # The chancel arch in the east wall: five wide, stepping in to a bone keystone.
    x = NAVE[1] + 1
    for z in range(ZC - 3, ZC + 4):
        p.set(x, FLOOR, z, CUT_SMOOTH)
        for y in range(FLOOR + 1, WALL_TOP):
            p.set(x, y, z, BONE if abs(z - ZC) == 3 else AIR)
    for y, reach in ((FLOOR + 5, 2), (FLOOR + 6, 1)):
        p.set(x, y, ZC - reach, BONE_STAIRS, stairs('north', 'top'))
        p.set(x, y, ZC + reach, BONE_STAIRS, stairs('south', 'top'))
        for z in range(ZC - 3, ZC + 4):
            if abs(z - ZC) > reach:
                p.set(x, y, z, BONE)
    for z in range(ZC - 3, ZC + 4):
        p.set(x, FLOOR + 7, z, BONE_CHISELED if z == ZC else BONE)
    for y in range(FLOOR + 8, WALL_TOP + 1):
        for z in range(ZC - 3, ZC + 4):
            p.set(x, y, z, deep_field(p, x, y, z, 'chancel'))


def nave_vault(p):
    """The nave's barrel vault of plating, carved into the roof's fill, ribbed in bone over each pilaster pair."""
    for x in range(NAVE[0], NAVE[1] + 1):
        y, reach = WALL_TOP + 1, HALF
        rib = x in PILASTERS
        while reach > 0:
            block = BONE_STAIRS if rib else PLATING_STAIRS
            p.set(x, y, ZC - reach, block, stairs('north', 'top'))
            p.set(x, y, ZC + reach, block, stairs('south', 'top'))
            for z in range(ZC - reach + 1, ZC + reach):
                p.set(x, y, z, AIR)
            reach -= 1
            y += 1
        p.set(x, y, ZC, BONE_CHISELED if rib else PLATING)


# --- the apse ------------------------------------------------------------------------------------------------------------

def apse(p):
    x1 = APSE[1] + 1
    z0, z1 = ZC - 4, ZC + 4
    for x in range(APSE[0], x1 + 1):
        for z in range(z0, z1 + 1):
            if x == x1 or z in (z0, z1):
                for y in range(FLOOR + 1, WALL_TOP + 1):
                    if x == x1 and z in (z0, z1):
                        block = quoin(x, y, z)
                    elif y == FLOOR + 1:
                        block = POLISHED
                    elif y == WALL_TOP:
                        block = CUT_SMOOTH
                    else:
                        block = deep_field(p, x, y, z, 'apse')
                    p.set(x, y, z, block)
            else:
                p.set(x, FLOOR, z, tile_floor(p, x, FLOOR, z, 'apse'))
                for y in range(FLOOR + 1, WALL_TOP + 1):
                    p.set(x, y, z, AIR)
    # The dais: two courses of venous stone with stairs on their west faces, the heart on top facing the door.
    for x in range(25, 28):
        for z in range(ZC - 2, ZC + 3):
            p.set(x, FLOOR + 1, z, CUT_SMOOTH)
    for z in range(ZC - 2, ZC + 3):
        p.set(24, FLOOR + 1, z, CUT_STAIRS, stairs('east'))
    for x in (26, 27):
        for z in range(ZC - 1, ZC + 2):
            p.set(x, FLOOR + 2, z, CUT_SMOOTH)
    for z in range(ZC - 1, ZC + 2):
        p.set(25, FLOOR + 2, z, CUT_STAIRS, stairs('east'))
    p.set(26, FLOOR + 2, ZC, CUT_CHISELED)
    p.set(*DISPLAY, 'hemomancy:mortal_display', {'facing': 'west'})
    for z in (ZC - 3, ZC + 3):
        p.basin(25, FLOOR + 1, z, 'west', 3)
    # The reredos: plating and bone behind the dais, iron columns flanking the heart.
    for z in range(ZC - 3, ZC + 4):
        for y in range(FLOOR + 1, FLOOR + 3):
            p.set(x1, y, z, PLATING if abs(z - ZC) % 2 == 0 else BONE)
    for z in (ZC - 2, ZC + 2):
        for y in range(FLOOR + 2, FLOOR + 6):
            p.set(27, y, z, PILLAR, {'axis': 'y'})
        p.set(27, FLOOR + 6, z, BONE_CHISELED)
    for z in (ZC - 3, ZC + 3):
        p.sconce(27, FLOOR + 4, z, 'west')


def apse_vault(p):
    """A barrel vault over the apse, carved into its roof, a boss at the crown and a lantern hung from it."""
    for x in range(APSE[0], APSE[1] + 1):
        y, reach = WALL_TOP + 1, 3
        while reach > 0:
            p.set(x, y, ZC - reach, PLATING_STAIRS, stairs('north', 'top'))
            p.set(x, y, ZC + reach, PLATING_STAIRS, stairs('south', 'top'))
            for z in range(ZC - reach + 1, ZC + reach):
                p.set(x, y, z, AIR)
            reach -= 1
            y += 1
        p.set(x, y, ZC, BONE_CHISELED if x in (25, 26) else PLATING)
    p.hung_lantern(25, WALL_TOP + 3, ZC, 1)


def rose(p, x, yc, zc):
    """A round window of sanguine glass in a bone ring, five across, in the wall plane at x, an iron boss at its heart."""
    for dy in range(-2, 3):
        for dz in range(-2, 3):
            d = dy * dy + dz * dz
            if d <= 5:
                p.set(x, yc + dy, zc + dz, IRON_CHISELED if d == 0 else GLASS if d <= 2 else BONE)


# --- the side rooms ------------------------------------------------------------------------------------------------------

def side_room(p, outer, inner, label):
    """A low room against the nave over x12..18, from its outer wall at z=outer to the nave wall at z=inner, under a
    plating lean-to stepping down from the nave's eave."""
    x0, x1 = 12, 18
    z0, z1 = min(outer, inner), max(outer, inner)
    walls(p, x0, x1, z0, z1, SIDE_TOP, lambda x, y, z: bone_field(p, x, y, z, label))
    for x in range(x0 + 1, x1):
        for z in range(z0 + 1, z1):
            p.set(x, FLOOR, z, LAC if (x + z) % 5 else PLANKS)
            for y in range(FLOOR + 1, SIDE_TOP):
                p.set(x, y, z, AIR)
            p.set(x, SIDE_TOP, z, LAC)
    for x in (x0 + 2, x1 - 2):
        for y in (FLOOR + 2, FLOOR + 3):
            p.set(x, y, outer, GLASS)
        p.set(x, FLOOR + 4, outer, BONE_CHISELED)
    step = 1 if outer > inner else -1
    courses = ((inner + step, SIDE_TOP + 1, PLATING, None), (inner + 2 * step, SIDE_TOP + 1, PLATING_SLAB, 'bottom'),
               (outer, SIDE_TOP + 1, PLATING_SLAB, 'bottom'), (outer + step, SIDE_TOP, PLATING_SLAB, 'top'))
    for x in range(x0 - 1, x1 + 2):
        for z, y, block, kind in courses:
            if 0 <= z < SIZE[2]:
                p.set(x, y, z, block, slab(kind) if kind else None)


def cell(p):
    """The Hermit's cell on the north side: a bedroll, a lectern, the temple's chest, a candle and a jar."""
    p.set(13, FLOOR + 1, 4, 'minecraft:red_bed', {'facing': 'west', 'occupied': 'false', 'part': 'head'})
    p.set(14, FLOOR + 1, 4, 'minecraft:red_bed', {'facing': 'west', 'occupied': 'false', 'part': 'foot'})
    p.set(17, FLOOR + 1, 4, 'minecraft:lectern', {'facing': 'south', 'has_book': 'false', 'powered': 'false'})
    p.barrel(17, FLOOR + 1, 5, 'west', loot=LOOT)
    p.set(13, FLOOR + 1, 5, LAC_STAIRS, stairs('north', 'top'))
    p.candle(13, FLOOR + 2, 5)
    p.jar(16, FLOOR + 1, 4, 'south')
    p.set(15, FLOOR + 1, 4, 'minecraft:gray_carpet')
    p.sconce(15, FLOOR + 3, 4, 'south')
    p.hung_lantern(15, SIDE_TOP - 1, 5, 1)


def chapel(p):
    """The reliquary chapel on the south side: a shelf of specimen jars, vein banners, kneelers and a candle."""
    zf = SIZE[2] - 5
    for x in (13, 14, 16, 17):
        p.set(x, FLOOR + 1, zf, CUT_SMOOTH)
        p.jar(x, FLOOR + 2, zf, 'north')
    p.set(15, FLOOR + 1, zf, CUT_CHISELED)
    p.candle(15, FLOOR + 2, zf)
    p.banner(13, FLOOR + 4, zf, 'north', color='red')
    p.banner(17, FLOOR + 4, zf, 'north', color='black')
    for x in (14, 16):
        p.set(x, FLOOR + 1, zf - 1, LAC_STAIRS, stairs('south'))
    p.hung_lantern(15, SIDE_TOP - 1, zf - 1, 1)


# --- the portico, the roofs and the bell-cote ----------------------------------------------------------------------------

def portico(p):
    """Four hematic iron columns before the west front, a tiled deck over them with a bone balustrade."""
    for z in (ZC - 4, ZC - 2, ZC + 2, ZC + 4):
        p.set(5, FLOOR, z, CUT_CHISELED)
        p.set(5, FLOOR + 1, z, CUT)
        for y in range(FLOOR + 2, FLOOR + 6):
            p.set(5, y, z, PILLAR, {'axis': 'y'})
        p.set(5, FLOOR + 6, z, BONE_CHISELED)
    for x in range(4, 8):
        for z in range(ZC - 5, ZC + 6):
            edge = x == 4 or z in (ZC - 5, ZC + 5)
            p.set(x, FLOOR + 7, z, BONE if edge else TILE)
            if edge:
                p.put(x, FLOOR + 8, z, BONE_WALL)
    for z in (ZC - 4, ZC + 4):
        p.lantern(6, FLOOR + 6, z)
    for z in (ZC - 3, ZC + 3):
        p.banner(7, FLOOR + 4, z, 'west', color='red')


def roofs(p):
    """A gabled plating roof over the nave, a lower one over the apse, both filled solid for the vaults to be carved."""
    gable_roof(p, NAVE[0] - 1, NAVE[1] + 1, ZC - HALF - 2, ZC + HALF + 2, WALL_TOP + 1, PLATING_STAIRS, PLATING_SLAB,
               along='x', gable=lambda x, y, z: deep_field(p, x, y, z, 'gable'), fill=PLATING)
    gable_roof(p, APSE[0], APSE[1] + 2, ZC - 5, ZC + 5, WALL_TOP + 1, PLATING_STAIRS, PLATING_SLAB, along='x',
               gable=lambda x, y, z: deep_field(p, x, y, z, 'apse_gable') if x == APSE[1] + 2 else PLATING,
               fill=PLATING)


def bell_cote(p):
    """An open stage on four deepslate piers banded in venous stone astride the west gable's ridge, a lantern hung in it, an iron finial."""
    bx, base = NAVE[0], WALL_TOP + 8
    for x in range(bx - 1, bx + 2):
        for z in range(ZC - 1, ZC + 2):
            p.set(x, base - 1, z, CUT_SMOOTH)
            p.set(x, base + 4, z, CUT)
            for y in range(base, base + 4):
                p.set(x, y, z, AIR)
    for x in (bx - 1, bx + 1):
        for z in (ZC - 1, ZC + 1):
            for y in range(base, base + 4):
                p.set(x, y, z, CUT if y in (base, base + 3) else deep_field(p, x, y, z, 'cote'))
    p.hung_lantern(bx, base + 3, ZC, 1)
    p.set(bx, base + 5, ZC, IRON_CHISELED)


def furnish(p):
    # The gutter: from the door to the chancel, dry at the door, stained, then brimming toward the heart.
    for x in range(NAVE[0], NAVE[1] + 2):
        if (x, FLOOR, ZC) != FOCUS:
            p.gutter(x, FLOOR, ZC, 0 if x < 12 else 1 if x < 17 else 2 if x < 21 else 3)
    # The Cardinal Focus on a ring of venous inlay, the gutter running into it; the anchor jigsaw under it.
    fx, fy, fz = FOCUS
    for dx in (-1, 0, 1):
        for dz in (-1, 1):
            p.set(fx + dx, fy, fz + dz, CUT_CHISELED if dx else CUT)
    p.set(fx, fy, fz, 'hemomancy:cardinal_focus')
    p.jigsaw(fx, 0, fz, 'up_north', 'hemomancy:blood_temple/anchor', 'hemomancy:blood_temple/anchor',
             'minecraft:empty', final=COBBLED)
    # Kneelers of lacquer either side of the gutter, facing the heart.
    for x in range(NAVE[0] + 2, NAVE[1] - 1):
        if abs(x - fx) <= 1 or 14 <= x <= 16:
            continue
        for z in (ZC - 3, ZC - 2, ZC + 2, ZC + 3):
            p.set(x, FLOOR + 1, z, LAC_STAIRS, stairs('west'))
    # Crimson sconces on the pilasters away from the side doors, lanterns hung down the vault.
    for x in PILASTERS:
        if x != 16:
            p.sconce(x, FLOOR + 4, ZC - HALF, 'south')
            p.sconce(x, FLOOR + 4, ZC + HALF, 'north')
    for x in (12, 18):
        p.hung_lantern(x, WALL_TOP + HALF, ZC, 3)
    # Lit braziers on plinths at the head of the steps, candles inside the door.
    for z in (ZC - 4, ZC + 4):
        p.set(2, FLOOR, z, CUT)
        p.brazier(2, FLOOR + 1, z)
    for z in (ZC - 3, ZC + 3):
        p.candle(NAVE[0], FLOOR + 1, z)
    p.put(*HERMIT, 'minecraft:light', {'level': '11'})

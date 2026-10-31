"""Generates Mason's Respite, the Vein-Mason's waystation (data/hemomancy/structure/masons_respite.nbt). Re-run from the
repository root: python tools/worldgen/masons_respite.py

A walled stone yard in front of the Anchorite's cell, 15 x 11 x 15, painted on the mausoleum kit's Room canvas.

    z0-7   the cell: cut venous stone plinth, quoins and top plate, a bone-brick field, a lacquer gable roof with a
           bone ridge, a chimney on the west gable over the hearth, buttresses on the north and east faces. Inside: the
           hearth and mantel, the reading lectern under the north window, a desk and stool, vein-diagram banners, the
           bedroll by the east window, a supply barrel, a lantern hung between two charred tie beams.
    z7     the eave over the cell door. The Anchorite stands here, at the template centre (7, 1, 7), on cut stone;
           MasonsRespiteStructure.afterPlace spawns him at the first air cell over a sturdy floor there.
    z7-13  the yard. West: the mason's lean-to (stonecutter, grindstone, banker, stacked cut stone). East: the teaching
           loggia (Cerebral Scarring Station, Mason's Effigy, a vein diagram on its back wall). Centre: the Iron Brazier
           in a ring of packed ash, on the walk from the gate to the cell door.
    z14    the gate wall: an arched gate between two lantern piers.

The station, the Effigy and the Brazier are the Vein-Mason lesson's working set (VeinMasonMissionSourceTest reads them
from the template). The station stands within the eight blocks MachineAccessEvents.isSupervisedScarLesson allows
between it and the Anchorite. Its filler cell is authored here; afterPlace binds it to the station.

Template y0 is the foundation course and lands on the surface (project_start_to_heightmap); y1 is the walking floor.
Every cell of the footprint is written (air included), so terrain never pokes into the yard or the cell.
"""
import math
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from nbtlib import Compound, List, String  # noqa: E402

from mausoleum import checks  # noqa: E402
from mausoleum.kit import AIR, Room, barrel, connect_walls, slab, stairs  # noqa: E402

OUT = os.path.join('src', 'main', 'resources', 'data', 'hemomancy', 'structure', 'masons_respite.nbt')
LOOT = 'hemomancy:chests/harbinger_outpost'
SIZE = (15, 11, 15)
CENTRE = (7, 7)

# Cut venous stone: plinths, quoins, trim, the yard.
CUT = 'hemomancy:polished_venous_stone_bricks'
CUT_CRACKED = 'hemomancy:cracked_polished_venous_stone_bricks'
CUT_SMOOTH = 'hemomancy:polished_venous_stone'
CUT_CHISELED = 'hemomancy:chiseled_polished_venous_stone'
CUT_STAIRS = 'hemomancy:polished_venous_stone_brick_stairs'
CUT_SLAB = 'hemomancy:polished_venous_stone_brick_slab'
CUT_WALL = 'hemomancy:polished_venous_stone_brick_wall'
VENOUS = 'hemomancy:venous_stone'
# Bone brick: the wall field.
BONE = 'hemomancy:bone_bricks'
BONE_CRACKED = 'hemomancy:cracked_bone_bricks'
BONE_CHISELED = 'hemomancy:chiseled_bone_bricks'
BONE_STAIRS = 'hemomancy:bone_brick_stairs'
BONE_SLAB = 'hemomancy:bone_brick_slab'
BONE_WALL = 'hemomancy:bone_brick_wall'
# Lacquer: roofs and furniture.
LAC_STAIRS = 'hemomancy:dark_lacquer_planks_stairs'
LAC_SLAB = 'hemomancy:dark_lacquer_planks_slab'
BEAM = 'hemomancy:charred_log'
ASH = 'hemomancy:packed_ash'
CHAIN = 'hemomancy:hematic_iron_chain'
LANTERN = 'hemomancy:hematic_lantern'
BARS = 'hemomancy:hematic_iron_bars'

# What finish() may swap in to break a flat face.
VARIANTS = {CUT: [CUT_CRACKED, CUT_SMOOTH], CUT_SMOOTH: [CUT, VENOUS], VENOUS: [CUT_SMOOTH, CUT_CRACKED],
            BONE: [BONE_CRACKED], BONE_CRACKED: [BONE], CUT_CRACKED: [CUT], ASH: [VENOUS],
            'minecraft:coarse_dirt': ['minecraft:rooted_dirt', VENOUS], 'minecraft:rooted_dirt': ['minecraft:coarse_dirt']}

# Hemomancy blocks and vanilla blocks with state that the shared table (mausoleum/checks.py) does not cover.
BOOL = ('true', 'false')
FACING4 = ('north', 'east', 'south', 'west')
EXTRA_STATES = {
    # BrazierBlock.RITUAL_PHASE is 0..2; its blockstate file only switches on 1|2.
    'hemomancy:iron_brazier': {'face': ('floor', 'wall', 'ceiling'), 'facing': FACING4,
                               'ritual_phase': ('0', '1', '2'), 'waterlogged': BOOL},
    'hemomancy:hematic_lantern': {'hanging': BOOL, 'waterlogged': BOOL},
    'hemomancy:hematic_iron_chain': {'axis': ('x', 'y', 'z'), 'waterlogged': BOOL},
    'hemomancy:hematic_iron_bars': {'axis': ('x', 'y', 'z'), 'north': BOOL, 'south': BOOL, 'east': BOOL,
                                    'west': BOOL, 'up': BOOL, 'down': BOOL, 'waterlogged': BOOL},
    'minecraft:campfire': {'facing': FACING4, 'lit': BOOL, 'signal_fire': BOOL, 'waterlogged': BOOL},
    'minecraft:red_bed': {'facing': FACING4, 'occupied': BOOL, 'part': ('head', 'foot')},
    'minecraft:lectern': {'facing': FACING4, 'has_book': BOOL, 'powered': BOOL},
    'minecraft:stonecutter': {'facing': FACING4},
    'minecraft:grindstone': {'face': ('floor', 'wall', 'ceiling'), 'facing': FACING4},
    'minecraft:water_cauldron': {'level': ('1', '2', '3')},
    'minecraft:white_wall_banner': {'facing': FACING4},
}
BARE_VANILLA = {'minecraft:coarse_dirt', 'minecraft:rooted_dirt', 'minecraft:gray_carpet'}
# Never on blood-side work (no copper/brass/bronze accents on Harbinger stone or machines).
FORBIDDEN = ('copper', 'brass', 'bronze', 'gold', 'gilded')

H = lambda *v: random.Random('/'.join(map(str, ('masons_respite',) + v))).random()  # noqa: E731


def field(x, y, z):
    """The bone-brick wall field, speckled with cracked bricks."""
    return BONE_CRACKED if H('field', x, y, z) < 0.16 else BONE


def cut(x, y, z):
    return CUT_CRACKED if H('cut', x, y, z) < 0.18 else CUT


def lantern(room, x, y, z, hanging=True):
    room.set(x, y, z, LANTERN, {'hanging': 'true' if hanging else 'false', 'waterlogged': 'false'})


def chain(room, x, y, z):
    room.set(x, y, z, CHAIN, {'axis': 'y', 'waterlogged': 'false'})


def vein_banner(room, x, y, z, facing):
    """A vein diagram: a white wall banner with the red Hemomancy veins, hung on the wall behind `facing`."""
    room.set(x, y, z, 'minecraft:white_wall_banner', {'facing': facing}, Compound({
        'id': String('minecraft:banner'),
        'patterns': List[Compound]([Compound({'color': String('red'), 'pattern': String('hemomancy:hemomancy_veins')})]),
    }))


def window(room, cells, axis):
    """Hematic iron bars set in the wall plane; their arms are joined in bars() once the walls stand."""
    for cell in cells:
        room.set(*cell, BARS, {'axis': axis})


# --- the ground ------------------------------------------------------------------------------------------------------

def ground(room):
    sx, _, sz = room.size
    for x in range(sx):
        for z in range(sz):
            # The apron round the cell: trodden earth and loose venous stone.
            r = H('apron', x, z)
            room.set(x, 0, z, VENOUS if r < 0.3 else 'minecraft:rooted_dirt' if r < 0.5 else 'minecraft:coarse_dirt')
            for y in range(1, room.size[1]):
                room.set(x, y, z, AIR)
    # The yard, x1..13, z7..13: venous stone flags, the walk in cut bricks, the ash ring round the Brazier.
    bx, bz = 7, 10
    for x in range(1, 14):
        for z in range(7, 14):
            d = math.hypot(x - bx, z - bz)
            r = H('yard', x, z)
            if 1.5 < d <= 2.5:
                block = ASH
            elif d <= 1.5:
                block = CUT_CHISELED if d == 0 else CUT_SMOOTH
            elif x == 7 or z == 7:
                block = cut(x, 0, z)                          # the walk, and the threshold row under the eave
            else:
                block = VENOUS if r < 0.55 else CUT_SMOOTH if r < 0.85 else CUT_CRACKED
            room.set(x, 0, z, block)
    # The lean-to floors: the mason's yard in smooth cut stone, the teaching loggia in bone brick.
    for x in range(1, 4):
        for z in range(7, 13):
            room.set(x, 0, z, VENOUS if H('shop', x, z) < 0.3 else CUT_SMOOTH)
    for x in range(11, 14):
        for z in range(7, 13):
            room.set(x, 0, z, BONE_CHISELED if (x, z) == (12, 10) else CUT if x == 11 else BONE)
    # The cell floor: cut brick, smooth hearthstones before the fire.
    for x in range(2, 13):
        for z in range(1, 7):
            room.set(x, 0, z, cut(x, 0, z))
    for z in (3, 4):
        room.set(3, 0, z, CUT_SMOOTH)
    # The gate threshold.
    room.set(6, 0, 14, CUT_SMOOTH)
    room.set(7, 0, 14, CUT_CHISELED)
    room.set(8, 0, 14, CUT_SMOOTH)


# --- the cell --------------------------------------------------------------------------------------------------------

def cell(room):
    x0, x1, z0, z1 = 2, 12, 1, 6
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x not in (x0, x1) and z not in (z0, z1):
                continue
            corner = x in (x0, x1) and z in (z0, z1)
            for y in range(1, 6):
                if corner:
                    block = CUT_SMOOTH if y % 2 else CUT          # quoins
                elif y in (1, 5):
                    block = cut(x, y, z)                         # plinth and top plate
                else:
                    block = field(x, y, z)
                room.set(x, y, z, block)
    # Gable fill under the roof on both ends.
    for x in (x0, x1):
        for z in range(2, 6):
            room.set(x, 6, z, field(x, 6, z))
        for z in (3, 4):
            room.set(x, 7, z, field(x, 7, z))
    roof(room)

    # The door: three wide, rounded with upside-down stairs, a chiselled bone keystone.
    for x in (6, 7, 8):
        for y in (1, 2):
            room.set(x, y, 6, AIR)
    room.set(7, 3, 6, AIR)
    room.set(6, 3, 6, CUT_STAIRS, stairs('west', 'top'))
    room.set(8, 3, 6, CUT_STAIRS, stairs('east', 'top'))
    room.set(7, 4, 6, BONE_CHISELED)
    room.set(6, 4, 6, CUT)
    room.set(8, 4, 6, CUT)
    # Lanterns under the eave either side of the door.
    lantern(room, 5, 4, 7)
    lantern(room, 9, 4, 7)

    # Windows: over the lectern in the north wall, over the bedroll in the east gable.
    window(room, [(7, 2, 1), (7, 3, 1)], 'z')
    window(room, [(12, 2, 3), (12, 3, 3), (12, 2, 4), (12, 3, 4)], 'x')

    # Buttresses: three on the north face, two on the east gable, each a two-course pier with a sloped cap.
    for x in (4, 10):
        buttress(room, x, 0, 'south')
    for z in (2, 5):
        buttress(room, 13, z, 'west')

    # The hearth: a firebox in the west gable, backed by the chimney stack, a corbelled mantel over it.
    for z in (3, 4):
        for y in (1, 2):
            room.set(2, y, z, AIR)
        room.set(2, 3, z, CUT_CHISELED)
        room.set(3, 3, z, CUT_STAIRS, stairs('west', 'top'))
    room.set(2, 1, 4, 'minecraft:campfire', {'facing': 'east', 'lit': 'true', 'signal_fire': 'false',
                                             'waterlogged': 'false'})
    room.set(2, 1, 3, BEAM, {'axis': 'z'})                       # split wood stacked in the firebox
    room.set(3, 4, 3, 'hemomancy:mnemonic_candle', {'lit': 'true', 'waterlogged': 'false'})
    for z in (3, 4):
        for y in range(1, 10):
            room.set(1, y, z, cut(1, y, z) if y < 9 else CUT)
        room.set(1, 10, z, CUT_WALL)
    room.set(1, 0, 3, CUT)
    room.set(1, 0, 4, CUT)
    room.set(4, 1, 3, 'minecraft:gray_carpet')
    room.set(4, 1, 4, 'minecraft:gray_carpet')

    # The reading wall: the lectern under the window, vein diagrams either side, a desk with a jar and a candle.
    room.set(7, 1, 2, 'minecraft:lectern', {'facing': 'south', 'has_book': 'false', 'powered': 'false'})
    vein_banner(room, 5, 4, 2, 'south')
    vein_banner(room, 9, 4, 2, 'south')
    for x in (8, 9):
        room.set(x, 1, 2, LAC_STAIRS, stairs('north', 'top'))
    room.set(8, 2, 2, 'hemomancy:specimen_jar', {'facing': 'south', 'waterlogged': 'false'})
    room.set(9, 2, 2, 'hemomancy:mnemonic_candle', {'lit': 'true', 'waterlogged': 'false'})
    room.set(8, 1, 3, LAC_STAIRS, stairs('south'))
    # The bedroll by the east window, the supply barrel in the corner with a jar on it.
    room.set(10, 1, 5, 'minecraft:red_bed', {'facing': 'east', 'occupied': 'false', 'part': 'foot'})
    room.set(11, 1, 5, 'minecraft:red_bed', {'facing': 'east', 'occupied': 'false', 'part': 'head'})
    barrel(room, 11, 1, 2, 'up', loot=LOOT)
    room.set(11, 2, 2, 'hemomancy:specimen_jar', {'facing': 'west', 'waterlogged': 'false'})
    # Two charred tie beams, a lantern hung from the ridge between them.
    for x in (5, 9):
        for z in range(2, 6):
            room.set(x, 5, z, BEAM, {'axis': 'z'})
    chain(room, 7, 7, 4)
    chain(room, 7, 6, 4)
    lantern(room, 7, 5, 4)


def roof(room):
    """A lacquer gable over x1..13, z0..7: eaves at y5, ridge at y8, capped in cut-stone slabs."""
    for x in range(1, 14):
        for i, (zn, zs) in enumerate(((0, 7), (1, 6), (2, 5), (3, 4))):
            room.set(x, 5 + i, zn, LAC_STAIRS, stairs('south'))
            room.set(x, 5 + i, zs, LAC_STAIRS, stairs('north'))
        for z in (3, 4):
            room.set(x, 9, z, CUT_SLAB, slab())


def buttress(room, x, z, toward):
    """A pier against the wall on the `toward` side of (x, z): two courses and a sloped cap, on a cut-stone footing."""
    for y in range(0, 3):
        room.set(x, y, z, CUT)
    room.set(x, 3, z, CUT_STAIRS, stairs(toward))


# --- the yard --------------------------------------------------------------------------------------------------------

def yard(room):
    for x, z in [(x, z) for x in (0, 14) for z in range(6, 15)] + [(x, 14) for x in range(15)] + [(1, 6), (13, 6)]:
        room.set(x, 0, z, CUT)                                      # footings under the yard walls
    # West and east walls, z6..14: piers at the ends, a raised field behind the lean-tos.
    for x in (0, 14):
        for z in range(6, 15):
            if z in (6, 14):
                for y in range(1, 5):
                    room.set(x, y, z, CUT)
                room.set(x, 5, z, CUT_WALL)
            else:
                room.set(x, 1, z, cut(x, 1, z))
                for y in range(2, 5):
                    room.set(x, y, z, field(x, y, z))
                room.set(x, 5, z, BONE_SLAB, slab())
    # A cut-stone pilaster halfway along each raised wall (the loggia's vein diagram hangs on the east one).
    for x in (0, 14):
        for y in range(1, 5):
            room.set(x, y, 10, CUT_CHISELED if y == 3 else CUT)
        room.set(x, 5, 10, CUT_SLAB, slab())
    # Returns from the side walls to the cell's corners, under the eave.
    for x in (1, 13):
        room.set(x, 1, 6, cut(x, 1, 6))
        for y in range(2, 6):
            room.set(x, y, 6, field(x, y, 6))
    # The gate wall: a low wall with a bone rail, two lantern piers, the arched gate between them.
    for x in list(range(1, 5)) + list(range(10, 14)):
        room.set(x, 1, 14, cut(x, 1, 14))
        room.set(x, 2, 14, field(x, 2, 14))
        room.set(x, 3, 14, BONE_WALL)
    for x in (5, 9):
        for y in range(1, 5):
            room.set(x, y, 14, CUT_CHISELED if y == 3 else CUT)
        lantern(room, x, 5, 14, hanging=False)
    room.set(6, 3, 14, BONE_STAIRS, stairs('west', 'top'))
    room.set(8, 3, 14, BONE_STAIRS, stairs('east', 'top'))
    for x in (6, 7, 8):
        room.set(x, 4, 14, BONE_CHISELED if x == 7 else BONE)
        room.set(x, 5, 14, BONE_SLAB, slab())

    lean_to(room, 1, 4, 'west', LAC_SLAB)
    lean_to(room, 13, 10, 'east', BONE_SLAB)

    # The mason's lean-to: stonecutter, grindstone, the banker with a half-dressed block, stacked cut stone.
    room.set(2, 1, 8, 'minecraft:stonecutter', {'facing': 'east'})
    barrel(room, 1, 1, 7, 'up')
    room.set(1, 1, 9, CUT_SMOOTH)                                   # the banker
    room.set(1, 2, 9, CUT_STAIRS, stairs('west'))                   # a block half dressed on it
    room.set(2, 1, 10, 'minecraft:grindstone', {'face': 'floor', 'facing': 'north'})
    for (x, y, z) in ((1, 1, 11), (1, 2, 11), (1, 1, 12), (2, 1, 12), (1, 2, 12)):
        room.set(x, y, z, CUT if (x + y + z) % 2 else CUT_SMOOTH)
    room.set(2, 2, 12, CUT_SLAB, slab())
    room.set(3, 1, 12, CUT_STAIRS, stairs('west'))
    room.set(2, 1, 11, CUT_SLAB, slab())
    room.set(3, 1, 11, BONE_SLAB, slab())
    lantern(room, 3, 2, 10)

    # The teaching loggia: the scarring station and the Effigy face the yard, a vein diagram on the wall behind.
    room.set(12, 1, 9, 'hemomancy:scar_station', {'facing': 'west', 'waterlogged': 'false'})
    room.set(12, 2, 9, 'hemomancy:filler_block', {'waterlogged': 'false'})
    room.set(12, 1, 11, 'hemomancy:mason_effigy', {'facing': 'west', 'waterlogged': 'false'})
    vein_banner(room, 13, 3, 10, 'west')
    room.set(13, 1, 12, 'hemomancy:specimen_jar', {'facing': 'west', 'waterlogged': 'false'})
    room.set(13, 1, 7, 'hemomancy:mnemonic_candle', {'lit': 'true', 'waterlogged': 'false'})
    lantern(room, 11, 2, 10)

    # The Brazier at the heart of the ash ring, facing the gate.
    room.set(7, 1, 10, 'hemomancy:iron_brazier', {'face': 'floor', 'facing': 'south', 'ritual_phase': '0',
                                                  'waterlogged': 'false'})
    # The yard's corners: a water trough by the mason's yard, the mason's stock of bone brick by the loggia.
    room.set(1, 1, 13, 'minecraft:water_cauldron', {'level': '3'})
    room.set(13, 1, 13, BONE)
    room.set(13, 2, 13, BONE_SLAB, slab())
    room.set(12, 1, 13, BONE_STAIRS, stairs('east'))


def lean_to(room, back, front, side, slab_block):
    """A half-slab lean-to over z7..12 from the raised wall at x=back (+-1 inward) to the posts at x=front."""
    step = 1 if front > back else -1
    xs = [back + step * i for i in range(4)]
    for z in range(7, 13):
        for i, x in enumerate(xs):
            y = 4 if i < 2 else 3
            room.set(x, y, z, slab_block, slab('top' if i % 2 == 0 else 'bottom'))
    for z in (7, 12):
        for y in (1, 2):
            room.set(front, y, z, CUT_WALL)


# --- finishing -------------------------------------------------------------------------------------------------------

STEP6 = {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0), 'up': (0, 1, 0),
         'down': (0, -1, 0)}
AXIS_OF = {'north': 'z', 'south': 'z', 'east': 'x', 'west': 'x', 'up': 'y', 'down': 'y'}


def bars(room):
    """Joins each bar to the bars and full faces beside it, as HematicIronBarsBlock.getStateForPlacement would."""
    cells = [pos for pos, entry in room.blocks.items() if entry[0] == BARS]
    for (x, y, z) in cells:
        axis = dict(room.blocks[(x, y, z)][1])['axis']
        state = {'axis': axis, 'waterlogged': 'false'}
        for side, (dx, dy, dz) in STEP6.items():
            joined = False
            if AXIS_OF[side] != axis:
                other = room.blocks.get((x + dx, y + dy, z + dz))
                if other and other[0] == BARS:
                    joined = dict(other[1])['axis'] != AXIS_OF[side]
                else:
                    joined = checks.cube(other)
            state[side] = 'true' if joined else 'false'
        room.set(x, y, z, BARS, state)


def finish(room):
    walls = sorted(pos for pos, entry in room.blocks.items() if entry[0].endswith('_wall'))
    for pos in walls:
        name, _props, nbt = room.blocks[pos]
        room.set(*pos, name, {'waterlogged': 'false'}, nbt)
    connect_walls(room, walls)
    bars(room)
    rng = random.Random('masons_respite/finish')
    for _ in range(16):
        windows = checks.flat_windows(room, open_outside=True)
        if not windows:
            return
        for name, cells in windows:
            if any(room.get(*c) != name for c in cells):
                continue
            if name not in VARIANTS:
                raise ValueError('a flat face of %s at %s has no variant to break it' % (name, cells[0]))
            room.set(*rng.choice(cells), rng.choice(VARIANTS[name]))
    left = checks.flat_windows(room, open_outside=True)
    if left:
        raise ValueError('%d flat faces left' % len(left))


# --- checks ----------------------------------------------------------------------------------------------------------

def problems(room):
    out = []
    seen = set()
    for (name, props, _nbt) in room.blocks.values():
        if (name, props) in seen or name == AIR:
            continue
        seen.add((name, props))
        path = name.split(':', 1)[1]
        if any(word in path for word in FORBIDDEN):
            out.append('forbidden block %s' % name)
        if name in EXTRA_STATES:
            table = EXTRA_STATES[name]
        elif name.startswith('minecraft:') and name not in checks.VANILLA and name not in BARE_VANILLA \
                and not any(name.endswith(s) for s, _ in checks.FAMILIES):
            out.append('%s: add its states to EXTRA_STATES' % name)
            continue
        else:
            table = checks.expected(name)
            if table is None:
                out.append('%s has no blockstate' % name)
                continue
        for key, value in props:
            if key not in table:
                out.append('%s has no property %s' % (name, key))
            elif value not in table[key]:
                out.append('%s.%s does not accept %s' % (name, key, value))
        missing = set(table) - {k for k, _ in props}
        if missing and name.endswith(('_stairs', '_wall', '_slab')):
            out.append('%s is placed without %s' % (name, ', '.join(sorted(missing))))
    # Loot tables named by containers exist.
    for pos, (name, _props, nbt) in room.blocks.items():
        if nbt is not None and 'LootTable' in nbt:
            ns, path = str(nbt['LootTable']).split(':')
            if not os.path.exists(os.path.join('src', 'main', 'resources', 'data', ns, 'loot_table', path + '.json')):
                out.append('missing loot table %s at %s' % (nbt['LootTable'], pos))
    # The Anchorite's spawn: air at the centre over a sturdy cut-stone floor, headroom above.
    cx, cz = CENTRE
    if room.get(cx, 1, cz) != AIR or room.get(cx, 2, cz) != AIR or not checks.cube(room.blocks.get((cx, 0, cz))):
        out.append('the centre (%d, 1, %d) must be air over a full floor block' % (cx, cz))
    # The station's lesson reach (MachineAccessEvents.isSupervisedScarLesson: within 8 blocks of the Anchorite).
    for pos, (name, _p, _n) in room.blocks.items():
        if name == 'hemomancy:scar_station' and (pos[0] - cx) ** 2 + (pos[2] - cz) ** 2 > 36:
            out.append('scar station at %s is too far from the Anchorite' % (pos,))
    for required in ('hemomancy:scar_station', 'hemomancy:mason_effigy', 'hemomancy:iron_brazier'):
        if required not in {entry[0] for entry in room.blocks.values()}:
            out.append('missing %s' % required)
    return out


def build():
    room = Room(*SIZE)
    ground(room)
    cell(room)
    yard(room)
    finish(room)
    return room


def main():
    room = build()
    found = problems(room)
    if found:
        print('\n'.join(found))
        print('%d problems; nothing written' % len(found))
        sys.exit(1)
    room.save(OUT)
    solid = sum(1 for entry in room.blocks.values() if entry[0] != AIR)
    print('masons_respite: %s, %d cells, %d blocks' % (room.size, len(room.blocks), solid))


if __name__ == '__main__':
    main()

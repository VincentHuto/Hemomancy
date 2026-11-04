"""The Harbinger Outpost's twelve jigsaw pieces (data/hemomancy/structure/harbinger_outpost/).

The courtyard (start piece, anchored under the plaza at (15, 0, 15)) connects four mandatory double-wall segments. The
north and south segments connect the four corner homes; the south segment connects the gatehouse and the north segment
the two-story barracks hall. Connector names, pools and positions, the station cells, the ladder runs and the resident
markers are the contract HarbingerOutpostAssemblyGameTests pins; HarbingerOutpostStructure.afterPlace spawns a resident
on each `minecraft:light` marker (11 Vicar, 12 Artificer, 13 Alchemist, 14 Mnemonist), binds the multiblock fillers,
rekindles the braziers and props the cobbled-deepslate footing (row 0) down to the ground.

Every piece: rows 0-1 are footing, row 2 the walking floor, the walls stand from row 3.
"""
from .kit import (AIR, BARS, BONE, BONE_CHISELED, BONE_SLAB, BONE_STAIRS, BONE_WALL, CANDLE, CHISELED_DEEP, COBBLED,
                  CUT, CUT_CHISELED, CUT_SLAB, CUT_SMOOTH, CUT_STAIRS, CUT_WALL, DEEP, DEEP_SLAB, DEEP_STAIRS, GLASS,
                  IRON_BLOCK, IRON_CHISELED, LAC, LAC_SLAB, LAC_STAIRS, LOG, PLANKS, PLATING, PLATING_SLAB,
                  PLATING_STAIRS, PLATING_WALL, POLISHED, TILE, TILE_SLAB, TILE_STAIRS, TRAPDOOR, VENOUS, Piece,
                  bone_field, cut_field, deep_field, finish, gable_roof, h, hip_roof, quoin, slab, stairs, tile_floor, trapdoor)

LOOT = 'hemomancy:chests/harbinger_outpost'
POOL = 'hemomancy:harbinger_outpost/'


def footing(p):
    sx, sy, sz = p.size
    p.box(0, sx - 1, 0, sy - 1, 0, sz - 1, AIR)
    p.box(0, sx - 1, 0, 1, 0, sz - 1, COBBLED)


def ring_stairs_in(p, block, x0, x1, z0, z1, y):
    """A ring of stairs on the box's edge rising toward its middle, the corners turned (outer)."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            fz = 'south' if z == z0 else 'north' if z == z1 else None
            fx = 'east' if x == x0 else 'west' if x == x1 else None
            if not fz and not fx:
                continue
            if fz and fx:
                side = 'left' if {'north': 'west', 'east': 'north', 'south': 'east', 'west': 'south'}[fz] == fx \
                    else 'right'
                p.set(x, y, z, block, stairs(fz, 'bottom', 'inner_' + side))
            else:
                p.set(x, y, z, block, stairs(fz or fx))


def lamp_post(p, x, y, z, height=2):
    for dy in range(height):
        p.put(x, y + dy, z, CUT_WALL)
    p.lantern(x, y + height, z, hanging=False)


# --- the courtyard ------------------------------------------------------------------------------------------------------

def courtyard(name='courtyard', awning='minecraft:red_wool'):
    p = Piece(name, (31, 17, 31))
    footing(p)
    # The plaza: a deepslate-tile field with an inset venous border, crossed by brick roads that carry the vigil's
    # channels out toward the gates.
    for x in range(31):
        for z in range(31):
            r = h(name, 'pave', x, z)
            p.set(x, 2, z, POLISHED if r < 0.08 else tile_floor(p, x, 2, z))
    for n in range(3, 28):
        for e in (3, 27):
            block = CUT_CHISELED if n % 6 == 3 else CUT_SMOOTH
            p.set(n, 2, e, block)
            p.set(e, 2, n, block)
    for a in range(1, 30):
        for c in range(13, 18):
            for x, z in ((c, a), (a, c)):
                p.set(x, 2, z, POLISHED if c in (13, 17) else deep_field(p, x, 2, z, 'road'))
    for a in range(1, 30):
        for x, z in ((15, a), (a, 15)):
            p.set(x, 2, z, CUT_CHISELED if a % 4 == 2 else CUT)
    # The vigil: a stepped dais of venous stone and deepslate with the outpost's brazier on top, four basins on its
    # lip pouring into the channels that run out along the roads.
    p.box(11, 19, 2, 2, 11, 19, POLISHED)
    p.box(12, 18, 3, 3, 12, 18, TILE)
    ring_stairs_in(p, TILE_STAIRS, 12, 18, 12, 18, 3)
    p.box(13, 17, 4, 4, 13, 17, CUT_SMOOTH)
    ring_stairs_in(p, CUT_STAIRS, 13, 17, 13, 17, 4)
    p.box(14, 16, 4, 4, 14, 16, CUT)
    p.set(15, 4, 15, CUT_CHISELED)
    p.brazier(15, 5, 15, 'south')
    # The basins stand on the dais's border and pour off its lip into the channels.
    for (x, z), facing, run in (((15, 11), 'north', range(10, 5, -1)), ((15, 19), 'south', range(20, 25)),
                                ((11, 15), 'west', range(10, 5, -1)), ((19, 15), 'east', range(20, 25))):
        p.basin(x, 3, z, facing, 3)
        for i, a in enumerate(run):
            # As VenousBasinBlock.flow would leave it: brimming for four, then one lower.
            fill = 3 if i < 4 else 2
            gx, gz = (15, a) if facing in ('north', 'south') else (a, 15)
            p.gutter(gx, 2, gz, fill)
    for x, z in ((11, 11), (19, 11), (11, 19), (19, 19)):
        lamp_post(p, x, 3, z)
    # Lamp posts where the roads meet the border.
    for x, z in ((12, 3), (18, 3), (12, 27), (18, 27), (3, 12), (3, 18), (27, 12), (27, 18)):
        lamp_post(p, x, 3, z)
    # Market stalls: blood-wood posts under a cloth awning with a lacquer ridge, a counter of barrels and a bench.
    for x0, z0 in ((6, 7), (21, 7), (6, 21), (21, 21)):
        for x in (x0, x0 + 3):
            for z in (z0, z0 + 2):
                p.box(x, x, 3, 5, z, z, (LOG, {'axis': 'y'}))
        p.box(x0, x0 + 3, 6, 6, z0, z0 + 2, awning)
        p.box(x0, x0 + 3, 7, 7, z0 + 1, z0 + 1, (LAC_SLAB, slab()))
        p.barrel(x0 + 1, 3, z0)
        p.barrel(x0 + 2, 3, z0)
        p.jar(x0 + 1, 4, z0, 'south')
        p.candle(x0 + 2, 4, z0)
        p.set(x0 + 1, 3, z0 + 1, LAC_STAIRS, stairs('north'))
    p.barrel(9, 3, 22, loot=LOOT)
    p.set(21, 3, 15, 'hemomancy:rite_fragment_inscription', {'facing': 'west'}, _inscription(
        'hemomancy:harbinger_outpost/crimson_beacon_fragment'))
    for x in (6, 24):
        p.set(x, 4, 15, 'hemomancy:abocipher_emitter', None, _nbt({
            'id': 'hemomancy:abocipher_emitter', 'Profile': 'harbinger_outpost'}))
    for x, z, facing, nm, target in ((15, 0, 'north', 'north_out', 'north_in'), (0, 15, 'west', 'west_out', 'west_in'),
                                    (30, 15, 'east', 'east_out', 'east_in'), (15, 30, 'south', 'south_out', 'south_in')):
        p.connector(x, 4, z, facing, nm, POOL + 'wall_' + facing, target)
    # Anchors the rotated start piece on the chunk centre, under the plaza.
    p.jigsaw(15, 0, 15, 'up_north', 'hemomancy:harbinger_outpost/anchor', 'hemomancy:harbinger_outpost/anchor',
             'minecraft:empty', final=COBBLED)
    return p


def _nbt(values):
    from nbtlib import Compound, String
    return Compound({k: String(v) for k, v in values.items()})


def _inscription(inscription_id):
    return _nbt({'id': 'hemomancy:discovery_inscription', 'InscriptionId': inscription_id})


# --- the curtain walls --------------------------------------------------------------------------------------------------

def wall(side):
    along_x = side in ('north', 'south')
    p = Piece('wall_' + side, (31, 17, 13) if along_x else (13, 17, 31))
    width, _, depth = p.size
    footing(p)
    outer, inner = (0, 4) if side in ('north', 'west') else (12, 8)
    corridor = range(1, 4) if outer == 0 else range(9, 12)
    inward = 1 if outer < inner else -1

    def xz(along, cross):
        return (along, cross) if along_x else (cross, along)

    for x in range(width):
        for z in range(depth):
            along, cross = (x, z) if along_x else (z, x)
            if cross in corridor:
                block = CUT if cross == outer + inward * 2 else tile_floor(p, x, 2, z)
                if cross == outer + inward * 2 and along % 5 == 0:
                    block = CUT_CHISELED
            elif cross == (2 if outer == 0 else 10) or cross == (10 if outer == 0 else 2):
                block = CUT if along % 5 else CUT_CHISELED
            else:
                block = tile_floor(p, x, 2, z)
            p.set(x, 2, z, block)
    piers = (4, 11, 19, 26)
    for along in range(31):
        for cross in (outer, inner):
            x, z = xz(along, cross)
            opening = cross == inner and 14 <= along <= 16
            opening |= along_x and cross == outer and 14 <= along <= 16
            for y in range(3, 8):
                if opening and y <= 5:
                    continue
                if along in piers:
                    block = quoin(x, y, z)
                elif y == 3:
                    block = POLISHED
                elif y == 6:
                    block = CUT_SMOOTH
                else:
                    block = deep_field(p, x, y, z)
                p.set(x, y, z, block)
            if opening:
                # A lintel of venous stone over the passage, a keystone at its middle.
                p.set(x, 6, z, CUT_CHISELED if along == 15 else CUT)
            # The coping, then merlons on the outer face and a balustrade of posts on the inner.
            p.set(x, 8, z, TILE)
            if cross == outer:
                if along % 3 == 0 or along in piers:
                    p.set(x, 9, z, DEEP if along not in piers else CUT)
                    p.set(x, 10, z, DEEP_SLAB if along not in piers else CUT_SLAB, slab())
            elif along % 2 == 0:
                p.put(x, 9, z, CUT_WALL)
        if along in piers:
            # The piers rise a course above the coping on the inner face and carry a lantern.
            x, z = xz(along, inner)
            p.set(x, 9, z, CUT)
            p.lantern(x, 10, z, hanging=False)
    # Tie beams across the corridor, each with a lantern hung under it.
    for along in (7, 15, 23):
        for cross in corridor:
            x, z = xz(along, cross)
            p.set(x, 8, z, LOG, {'axis': 'z' if along_x else 'x'})
        x, z = xz(along, outer + inward * 2)
        p.lantern(x, 7, z)
    # Sconces on the outer wall's inner face.
    for along in (3, 27):
        x, z = xz(along, outer + inward)
        p.sconce(x, 5, z, _facing_from(inward, along_x))
    for along in (8, 22):
        x, z = xz(along, inner - inward)
        p.banner(x, 5, z, side)
    if side == 'north':
        for cx in (6, 24):
            watchtower(p, cx)
        p.connector(15, 4, 12, 'south', 'north_in', 'minecraft:empty', 'north_out')
        p.connector(0, 4, 2, 'west', 'vicar_out', POOL + 'vicar', 'vicar_in')
        p.connector(30, 4, 2, 'east', 'artificer_out', POOL + 'artificer', 'artificer_in')
        p.connector(15, 4, 0, 'north', 'hall_out', POOL + 'barracks_hall', 'hall_in')
    elif side == 'south':
        p.connector(15, 4, 0, 'north', 'south_in', 'minecraft:empty', 'south_out')
        p.connector(0, 4, 10, 'west', 'alchemist_out', POOL + 'alchemist', 'alchemist_in')
        p.connector(30, 4, 10, 'east', 'mnemonist_out', POOL + 'mnemonist', 'mnemonist_in')
        p.connector(15, 4, 12, 'south', 'gate_out', POOL + 'gate', 'gate_in')
    elif side == 'west':
        p.connector(12, 4, 15, 'east', 'west_in', 'minecraft:empty', 'west_out')
    else:
        p.connector(0, 4, 15, 'west', 'east_in', 'minecraft:empty', 'east_out')
    return p


def _facing_from(inward, along_x):
    """The way a fixture on the outer wall faces: into the corridor."""
    if along_x:
        return 'south' if inward > 0 else 'north'
    return 'east' if inward > 0 else 'west'


def watchtower(p, cx):
    """A watchtower over the north wall: a deepslate drum on a tile platform, quoined corners, barred windows and a
    hipped plating roof under an iron finial, climbed by a ladder against the outer wall."""
    p.box(cx - 2, cx + 2, 8, 8, 0, 4, TILE)
    for x in range(cx - 2, cx + 3):
        for z in range(5):
            if x not in (cx - 2, cx + 2) and z not in (0, 4):
                for y in range(9, 13):
                    p.set(x, y, z, AIR)
                continue
            corner = x in (cx - 2, cx + 2) and z in (0, 4)
            for y in range(9, 13):
                p.set(x, y, z, quoin(x, y, z) if corner else CUT_SMOOTH if y == 12 else deep_field(p, x, y, z))
    # Barred windows, two tall, in the middle of each face (the south face's over the landing).
    for x, z, axis in ((cx, 0, 'z'), (cx, 4, 'z'), (cx - 2, 2, 'x'), (cx + 2, 2, 'x')):
        for y in (10, 11):
            p.bars(x, y, z, axis)
    hip_roof(p, cx - 2, cx + 2, 0, 4, 13, PLATING_STAIRS, IRON_CHISELED)
    p.set(cx, 15, 2, IRON_CHISELED)
    p.lantern(cx, 16, 2, hanging=False)
    for y in range(12, 15):
        p.chain(cx, y, 2)
    p.lantern(cx, 11, 2)
    # Climb against the continuous north wall, through the platform, then step south onto the landing.
    p.set(cx - 1, 8, 1, AIR)
    for y in range(3, 10):
        p.put(cx - 1, y, 1, 'minecraft:ladder', {'facing': 'south'})


# --- the corner homes ---------------------------------------------------------------------------------------------------

HOME_HEIGHT = 19


def room(name, left, north):
    p = Piece(name, (13, HOME_HEIGHT, 13))
    footing(p)
    door_z = 2 if north else 10
    door_x = 2 if left else 10
    for x in range(13):
        for z in range(13):
            if x in (0, 12) or z in (0, 12):
                floor = CUT
            elif name == 'artificer':
                floor = tile_floor(p, x, 2, z)
            elif name == 'alchemist':
                floor = CUT_SMOOTH if x in (4, 8) or z in (4, 8) else tile_floor(p, x, 2, z)
            elif name == 'vicar':
                floor = CUT if x in (5, 6, 7) else LAC
            else:
                floor = LOG if x in (3, 9) or z in (3, 9) else LAC
            if floor == LOG:
                p.set(x, 2, z, LOG, {'axis': 'z' if z not in (3, 9) else 'x'})
            else:
                p.set(x, 2, z, floor)
    # The patrol route's floor: a tile walk with a venous line where it meets the wall's runner.
    for x in range(1, 12):
        for z in (range(1, 4) if north else range(9, 12)):
            p.set(x, 2, z, CUT if z == door_z else TILE)
    for z in range(1, 12):
        for x in (range(1, 4) if left else range(9, 12)):
            p.set(x, 2, z, CUT if x == door_x else TILE)
    # A timber frame: a venous plinth course, blood-wood posts every four and a wall plate, bone-brick infill.
    for y in range(3, 8):
        for x in range(13):
            for z in range(13):
                if x not in (0, 12) and z not in (0, 12):
                    continue
                along = x if z in (0, 12) else z
                corner = x in (0, 12) and z in (0, 12)
                if corner or along % 4 == 0:
                    face = (LOG, {'axis': 'y'})
                elif y == 3:
                    face = cut_field(p, x, y, z)
                elif y == 7:
                    face = (LOG, {'axis': 'x' if z in (0, 12) else 'z'})
                else:
                    face = bone_field(p, x, y, z)
                p.set(x, y, z, *face) if isinstance(face, tuple) else p.set(x, y, z, face)
    # Windows of sanguine glass under bone lintels.
    for a in (2, 10):
        for x, z in ((a, 0), (a, 12), (0, a), (12, a)):
            for y in (4, 5):
                p.set(x, y, z, GLASS)
            p.set(x, 6, z, BONE_CHISELED)
    # The ceiling and a hipped lacquer roof over a plate band, capped in plating.
    p.box(1, 11, 8, 8, 1, 11, LAC)
    for x in range(13):
        for z in (0, 12):
            p.set(x, 8, z, CUT_SMOOTH)
            p.set(z, 8, x, CUT_SMOOTH)
    top = hip_roof(p, 0, 12, 0, 12, 9, LAC_STAIRS, PLATING_SLAB, fill=PLANKS)
    p.lantern(6, 7, 6)
    # The patrol walk turns through an L-shaped clear route in each corner home.
    side_x = 12 if left else 0
    end_z = 12 if north else 0
    p.box(side_x, side_x, 3, 5, door_z - 1, door_z + 1, AIR)
    p.box(door_x - 1, door_x + 1, 3, 5, end_z, end_z, AIR)
    for x, z, axis in ((side_x, door_z, 'z'), (door_x, end_z, 'x')):
        # A lintel over each passage.
        lintel = [(x, 6, z + d) for d in (-1, 0, 1)] if axis == 'z' else [(x + d, 6, z) for d in (-1, 0, 1)]
        for i, cell in enumerate(lintel):
            p.set(*cell, CUT_CHISELED if i == 1 else CUT)
    p.connector(side_x, 4, door_z, 'east' if left else 'west', name + '_in', 'minecraft:empty', name + '_out')
    # The workroom's partition: bone brick on a venous course.
    inner_x = 4 if left else 8
    inner_z = 4 if north else 8
    for x in (range(inner_x, 13) if left else range(0, inner_x + 1)):
        for y in range(3, 8):
            p.set(x, y, inner_z, bone_field(p, x, y, inner_z, 'part') if y in (4, 5, 6) else CUT)
    room_door_z = {'vicar': 6, 'artificer': 9, 'alchemist': 6, 'mnemonist': 4}[name]
    for z in (range(inner_z, 13) if north else range(0, inner_z + 1)):
        for y in range(3, 8):
            if room_door_z <= z <= room_door_z + 1 and y <= 5:
                continue
            p.set(inner_x, y, z, bone_field(p, inner_x, y, z, 'part') if y in (4, 5, 6) else CUT)
    p.set(6, 5, inner_z, GLASS)
    for z in (5, 7):
        p.banner(1, 5, z, 'east')
        p.banner(11, 5, z, 'west')
    # Open both courtyard-facing sides of the inward corner, so it is a walk-through entrance.
    courtyard_x = 12 if left else 0
    courtyard_z = 12 if north else 0
    x_step = -1 if left else 1
    z_step = -1 if north else 1
    for offset in range(3):
        p.box(courtyard_x, courtyard_x, 3, 5, courtyard_z + z_step * offset, courtyard_z + z_step * offset, AIR)
        p.box(courtyard_x + x_step * offset, courtyard_x + x_step * offset, 3, 5, courtyard_z, courtyard_z, AIR)
    p.set(courtyard_x, 6, courtyard_z, CUT_CHISELED)
    p.set(courtyard_x + x_step, 6, courtyard_z, CUT)
    p.set(courtyard_x, 6, courtyard_z + z_step, CUT)
    FURNISH[name](p, top)
    spawn_x, spawn_z, marker = {'vicar': (6, 7, 11), 'artificer': (6, 9, 12), 'alchemist': (5, 6, 13),
                                'mnemonist': (5, 5, 14)}[name]
    p.put(spawn_x, 3, spawn_z, 'minecraft:light', {'level': str(marker)})
    return p


def spire(p, top, finial):
    """A venous spire over the hip's cap: two wall posts under the finial."""
    p.set(6, top, 6, CUT)
    p.put(6, top + 1, 6, CUT_WALL)
    p.put(6, top + 2, 6, CUT_WALL)
    finial(p, 6, top + 3, 6)


def vicar(p, top):
    p.put(6, 3, 5, 'hemomancy:somnolent_seat', {'facing': 'south'})
    p.put(6, 4, 5, 'hemomancy:somnolent_seat_filler', {'facing': 'south'})
    p.put(6, 3, 6, 'hemomancy:harbinger_escritoire', {'facing': 'north'})
    # The altar shelf of venous stone with candles, a vein banner over it, the memory echo on the north wall.
    p.box(5, 8, 3, 3, 9, 9, CUT_SMOOTH)
    p.set(6, 3, 9, CUT_CHISELED)
    p.set(7, 3, 9, CUT_CHISELED)
    p.candle(5, 4, 9)
    p.candle(8, 4, 9)
    p.jar(7, 4, 9, 'north')
    p.set(6, 4, 1, 'hemomancy:blood_echo_inscription', {'facing': 'south', 'waterlogged': 'false'},
          _inscription('hemomancy:harbinger_outpost/crude_memory_echo'))
    p.set(10, 3, 5, 'minecraft:bookshelf')
    p.set(11, 3, 5, 'minecraft:bookshelf')
    p.candle(11, 4, 5)
    p.sconce(5, 5, 11, 'north')
    p.sconce(8, 5, 11, 'north')
    spire(p, top, lambda q, x, y, z: q.lantern(x, y, z, hanging=False))


def artificer(p, top):
    p.put(6, 3, 6, 'hemomancy:resonant_forge', {'facing': 'north'})
    for z in (6, 7):
        for x in (5, 7):
            for y in (3, 4):
                p.put(x, y, z, 'hemomancy:filler_block')
    p.put(6, 3, 7, 'hemomancy:filler_block')
    p.set(3, 3, 8, 'minecraft:anvil', {'facing': 'east'})
    p.set(7, 3, 9, 'minecraft:smithing_table')
    # Stock: hematic iron, a rack of tools behind bars, chains on the beam.
    p.set(1, 3, 5, IRON_BLOCK)
    p.set(2, 3, 5, PLATING)
    p.set(2, 4, 5, PLATING_SLAB, slab())
    p.set(1, 3, 6, 'minecraft:grindstone', {'face': 'floor', 'facing': 'east'})
    p.chain(2, 7, 7)
    p.chain(2, 6, 7)
    p.sconce(1, 5, 9, 'east')
    # The forge's chimney rises through the roof, capped in plating.
    for y in range(9, 17):
        for x in (10, 11):
            for z in (9, 10):
                p.set(x, y, z, deep_field(p, x, y, z, 'chimney') if y < 16 else CUT)
    p.box(10, 11, 17, 17, 9, 10, (PLATING_SLAB, slab()))
    p.set(6, top, 6, IRON_CHISELED)


def alchemist(p, top):
    p.put(5, 3, 5, 'hemomancy:vial_centrifuge', {'facing': 'south'})
    p.put(8, 3, 5, 'hemomancy:ghastly_alembic', {'facing': 'south'})
    p.set(6, 3, 7, 'hemomancy:phlebotomists_cabinet')
    p.set(5, 3, 3, 'minecraft:brewing_stand', {'has_bottle_0': 'true', 'has_bottle_1': 'true', 'has_bottle_2': 'true'},
          _vial_display())
    p.set(9, 3, 4, 'minecraft:brewing_stand', {'has_bottle_0': 'true', 'has_bottle_1': 'true', 'has_bottle_2': 'true'},
          _vial_display())
    # A shelf of specimen jars on lacquer brackets along the partition.
    for x in (9, 10, 11):
        p.set(x, 4, 7, LAC_SLAB, slab('top'))
        p.jar(x, 5, 7, 'north')
    p.set(11, 3, 5, 'minecraft:water_cauldron', {'level': '3'})
    p.sconce(11, 5, 4, 'west')
    # Vent pipes of plating through the roof.
    for y in range(9, 15):
        p.set(3, y, 3, PLATING)
    p.put(3, 15, 3, PLATING_WALL)
    p.set(6, top, 6, IRON_CHISELED)


def mnemonist(p, top):
    p.put(6, 3, 6, 'hemomancy:somatic_loom', {'facing': 'south'})
    p.put(6, 4, 6, 'hemomancy:filler_block')
    for z in (5, 7):
        p.put(6, 3, z, 'hemomancy:filler_block')
    p.box(2, 4, 3, 6, 4, 4, 'minecraft:bookshelf')
    p.set(3, 3, 4, 'hemomancy:mnemonic_reliquary', {'facing': 'south'})
    p.set(2, 7, 4, CUT)
    p.set(4, 7, 4, CUT)
    p.candle(1, 3, 6)
    p.candle(1, 3, 7)
    p.set(3, 3, 7, 'minecraft:lectern', {'facing': 'north', 'has_book': 'false', 'powered': 'false'})
    p.sconce(1, 5, 3, 'east')
    spire(p, top, lambda q, x, y, z: q.candle(x, y, z))


FURNISH = {'vicar': vicar, 'artificer': artificer, 'alchemist': alchemist, 'mnemonist': mnemonist}


def _vial_display():
    from nbtlib import Byte, Compound, Int, List, String
    bottles = [Compound({'Slot': Byte(slot), 'id': String('minecraft:potion'), 'count': Int(1), 'components': Compound({
        'minecraft:potion_contents': Compound({'potion': String('minecraft:healing')})})}) for slot in range(3)]
    return Compound({'id': String('minecraft:brewing_stand'), 'Items': List[Compound](bottles)})


# --- the barracks hall --------------------------------------------------------------------------------------------------

def barracks_hall():
    p = Piece('barracks_hall', (15, 22, 11))
    footing(p)
    for x in range(15):
        for z in range(11):
            p.set(x, 2, z, CUT if x in (1, 13) or z in (1, 9) else tile_floor(p, x, 2, z))
    posts = (0, 4, 7, 10, 14)
    for y in range(3, 15):
        for x in range(15):
            for z in range(11):
                if x not in (0, 14) and z not in (0, 10):
                    continue
                along = x if z in (0, 10) else z
                post = (along in posts) if z in (0, 10) else along == 5 or x in (0, 14) and z in (0, 10)
                if post:
                    face = (LOG, {'axis': 'y'})
                elif y in (3, 8):
                    face = cut_field(p, x, y, z) if y == 3 else CUT_SMOOTH
                elif y in (7, 14):
                    face = (LOG, {'axis': 'x' if z in (0, 10) else 'z'})
                else:
                    face = bone_field(p, x, y, z)
                p.set(x, y, z, *face) if isinstance(face, tuple) else p.set(x, y, z, face)
    # The upper floor, reached by a ladder at the west wall.
    p.box(1, 13, 8, 8, 1, 9, LAC)
    for x in range(2, 13):
        for z in (4, 8):
            p.set(x, 8, z, LOG, {'axis': 'x'})
    # A gabled lacquer roof along the hall, its gables in bone with a round window, a plating ridge.
    p.box(1, 13, 15, 15, 1, 9, LAC)

    def gable(x, y, z):
        if y == 18 and z == 5:
            return GLASS
        return BONE_CHISELED if (y, z) == (19, 5) else bone_field(p, x, y, z, 'gable')

    gable_roof(p, 0, 14, 0, 10, 15, LAC_STAIRS, PLATING_SLAB, along='x', gable=gable, fill=PLANKS)
    for x in range(15):
        p.set(x, 20, 5, PLATING_SLAB, slab())
    # Courtyard-facing entry under a venous lintel; daylight on both stories.
    p.box(6, 8, 3, 5, 10, 10, AIR)
    for x in (6, 7, 8):
        p.set(x, 6, 10, CUT_CHISELED if x == 7 else CUT)
    for y in (5, 12):
        for x in (2, 12):
            for z in (0, 10):
                p.set(x, y, z, GLASS)
        for z in (3, 7):
            for x in (0, 14):
                p.set(x, y, z, GLASS)
    for y in range(3, 11):
        p.put(1, y, 2, 'minecraft:ladder', {'facing': 'east'})
    # Shared worktables, heat, storage and a lit common room below.
    for x, block, props in ((5, 'minecraft:crafting_table', None), (7, 'minecraft:furnace', {'facing': 'south', 'lit': 'false'}),
                            (9, 'minecraft:smoker', {'facing': 'south', 'lit': 'false'}),
                            (11, 'minecraft:cartography_table', None)):
        p.set(x, 3, 2, block, props)
    p.set(6, 3, 2, LAC_STAIRS, stairs('north', 'top'))
    p.set(10, 3, 2, LAC_STAIRS, stairs('north', 'top'))
    for x in (3, 11):
        p.barrel(x, 3, 5)
    for x in (4, 10):
        p.box(x, x + 1, 3, 3, 7, 7, (LAC_SLAB, slab('top')))
        p.set(x, 3, 6, LAC_STAIRS, stairs('south'))
        p.set(x + 1, 3, 6, LAC_STAIRS, stairs('south'))
        p.lantern(x, 7, 5)
        p.lantern(x, 14, 5)
    p.candle(4, 4, 7)
    p.jar(11, 4, 7, 'north')
    p.sconce(13, 5, 5, 'west')
    # Four usable beds and storage on the upper floor.
    for x in (3, 6, 9, 12):
        p.set(x, 9, 2, 'minecraft:red_bed', {'facing': 'north', 'occupied': 'false', 'part': 'head'})
        p.set(x, 9, 3, 'minecraft:red_bed', {'facing': 'north', 'occupied': 'false', 'part': 'foot'})
    for x in (4, 10):
        p.barrel(x, 9, 7, loot=LOOT if x == 10 else None)
        p.banner(x, 12, 9, 'north')
    p.connector(7, 4, 10, 'south', 'hall_in', 'minecraft:empty', 'hall_out')
    return p


# --- the gatehouse ------------------------------------------------------------------------------------------------------

def gate():
    p = Piece('gate', (9, 12, 9))
    footing(p)
    for x in range(9):
        for z in range(9):
            if x == 4:
                block = CUT_CHISELED if z in (2, 6) else CUT
            elif x in (2, 6):
                block = POLISHED
            else:
                block = tile_floor(p, x, 2, z)
            p.set(x, 2, z, block)
    for x in (0, 1, 7, 8):
        for y in range(3, 9):
            for z in range(9):
                pier = x in (0, 8) and z in (0, 4, 8)
                p.set(x, y, z, quoin(x, y, z) if pier else CUT_SMOOTH if y == 6 else POLISHED if y == 3
                      else deep_field(p, x, y, z))
    p.box(0, 8, 8, 8, 0, 8, TILE)
    # The passage ceiling: lacquer between blood-wood beams.
    p.box(2, 6, 7, 7, 0, 8, LAC)
    for z in (1, 4, 7):
        p.box(2, 6, 7, 7, z, z, (LOG, {'axis': 'x'}))
    # The roof: a tile deck, merlons on the edge.
    p.box(1, 7, 9, 9, 1, 7, TILE)
    for x in range(9):
        for z in range(9):
            if x in (0, 8) or z in (0, 8):
                p.set(x, 9, z, DEEP)
                if (x + z) % 2 == 0:
                    p.set(x, 10, z, DEEP_SLAB, slab())
    p.box(2, 6, 10, 10, 2, 6, (TILE_SLAB, slab()))
    p.set(4, 10, 4, CUT)
    p.lantern(4, 11, 4, hanging=False)
    # Arched gateways at both ends: a venous lintel and keystone over a raised portcullis.
    for z in (0, 8):
        p.box(3, 5, 3, 6, z, z, AIR)
        for x in (2, 6):
            p.box(x, x, 3, 5, z, z, lambda x, y, z: quoin(x, y, z))
        p.set(2, 6, z, CUT_STAIRS, stairs('west', 'top'))
        p.set(6, 6, z, CUT_STAIRS, stairs('east', 'top'))
        p.box(2, 6, 7, 7, z, z, CUT)
        p.set(4, 7, z, CUT_CHISELED)
    for x in (3, 4, 5):
        p.bars(x, 6, 0, 'z')
    for z in (2, 6):
        p.banner(2, 5, z, 'east')
        p.banner(6, 5, z, 'west')
    for x in (1, 7):
        p.banner(x, 10, 7, 0, standing=True)
    for x in (3, 5):
        p.lantern(x, 6, 4)
    p.connector(4, 4, 0, 'north', 'gate_in', 'minecraft:empty', 'gate_out')
    return p


def pieces():
    """Every piece, finished: walls, bars, panes and gutters joined as the game would join them."""
    built = [courtyard(), courtyard('courtyard_crimson', 'minecraft:black_wool'),
             wall('north'), wall('south'), wall('west'), wall('east'),
             room('vicar', True, True), room('artificer', False, True),
             room('alchemist', True, False), room('mnemonist', False, False),
             barracks_hall(), gate()]
    for p in built:
        finish(p)
    return built

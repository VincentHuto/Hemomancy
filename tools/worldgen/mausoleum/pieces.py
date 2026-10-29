"""Room builders for the Saint mausoleums. Every builder returns a Room in its own template frame (entry in the west
wall, the way on through the east wall, branches in the north/south walls) with its jigsaw connectors set.

The complex, entrance to tomb (Track 3):
  top -> stairs -> antechamber -> descent (drops 7) -> gallery (Track 1) -> descent_turn_0 -> descent_turn_1 (a stair
  spiralling two full turns round an open well, 16 down, and out the way it came in) -> threshold (Track 2) -> nave
  (31 x 16 x 21, an arcaded hall with a gallery level over both aisles) -> three wings and the chamber.
Running back the way the visitor came, the nave lies under the gallery and the descent, and the chamber under the
entrance: the Saint's life walked backwards, rest above and the beginning below it.

A wing is an approach (two layouts per slot), its kind's trial hall and its kind's focus room. Slot 0 leaves the nave at
its floor, slot 1 from the gallery level seven up, slot 2 down a flight of five into a crypt wing. Approaches carry
optional niches; a niche that cannot fit falls back to a blind arch, never an open hole.
"""
import os
import random

from nbtlib import Compound, String, Int, IntArray, Byte

from . import entrance, kit, trials
from .kit import AIR, Room, arch, arch_z, bone, field, mass, corridor, stairs, barrel, inscription, loculus, rail, post
from .themes import THEMES

STRUCTURES = os.path.join('src', 'main', 'resources', 'data', 'hemomancy', 'structure', 'mausoleum')
EMPTY = 'minecraft:empty'
MANDATORY = 1


def pool_name(saint, piece):
    return 'hemomancy:mausoleum/%s/%s' % (saint, piece)


def conn(name):
    return 'hemomancy:mausoleum/' + name


def theme(saint):
    return THEMES[saint]


def crypt(saint):
    return THEMES[saint].CRYPT


# --- the entrance (Track 7: built through the kit; the old hand-built NBTs stay as reference) --------------------

def top(saint):
    return entrance.top(saint, crypt(saint), conn, pool_name)


def entrance_stairs(saint):
    return entrance.stair(saint, crypt(saint), conn, pool_name)


def antechamber(saint):
    return entrance.antechamber(saint, crypt(saint), conn, pool_name)


# --- Track 1: the ossuary descent and the crypt gallery ----------------------------------------------------------

GALLERY_PIERS = (1, 5, 9, 13, 17, 21, 25)
GALLERY_CENTRE = 16
TREASURE_BAY = 2
ECHO_BAY = 5


def gallery(saint):
    """The crypt gallery (Track 1): a vaulted nave between two arcades of piers, with an aisle behind each arcade
    running along a wall of loculi, six bays long; the treasure room opens behind an iron door off the north aisle.
    Floor at y=0, entry west, exit east (onto the spiral), centre line z=16. Across z, mirrored about the centre:
    backing 8, loculus recess 9, wall plane 10, aisle 11-12, arcade 13, nave 14-18, arcade 19, aisle 20-21, wall plane
    22, recess 23, backing 24."""
    c = crypt(saint)
    rng = random.Random('gallery/' + saint)
    centre = GALLERY_CENTRE
    room = Room(27, 12, 25)
    for x in range(27):
        for z in range(8, 25):
            room.set(x, 0, z, c['floor'])
            for y in range(1, 9):
                room.set(x, y, z, field(rng, c))
            for y in range(9, 12):
                room.set(x, y, z, c['vault'])
    bays = [(p + 1, p + 2, p + 3) for p in GALLERY_PIERS[:-1]]
    for x in range(1, 26):
        for z in range(11, 22):
            d = abs(z - centre)
            if d <= 2:
                for y in range(1, 9):
                    room.set(x, y, z, AIR)
                floor = c['inlay'] if d == 2 else c['runner'] if d == 0 else c['floor']
            elif d == 3:
                floor = c['inlay']
            else:
                for y in range(1, 6):
                    room.set(x, y, z, AIR)
                room.set(x, 6, z, c['vault'])
                floor = c['floor_alt'] if rng.random() < 0.12 else c['floor']
            room.set(x, 0, z, floor)
        room.set(x, 8, centre - 2, c['trim_stairs'], stairs('north', 'top'))
        room.set(x, 8, centre + 2, c['trim_stairs'], stairs('south', 'top'))
        room.set(x, 9, centre - 2, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, 9, centre + 2, c['vault_stairs'], stairs('south', 'top'))
        for z in (centre - 1, centre, centre + 1):
            room.set(x, 9, z, AIR)
        room.set(x, 10, centre - 1, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, 10, centre, AIR)
        room.set(x, 10, centre + 1, c['vault_stairs'], stairs('south', 'top'))
        room.set(x, 5, 11, c['trim_stairs'], stairs('north', 'top'))
        room.set(x, 5, 21, c['trim_stairs'], stairs('south', 'top'))
        room.set(x, 5, 10, c['trim'])
        room.set(x, 5, 22, c['trim'])
    for xs in bays:
        for z in (centre - 3, centre + 3):
            for y in range(1, 5):
                for x in xs:
                    room.set(x, y, z, AIR)
            room.set(xs[0], 5, z, c['trim_stairs'], stairs('west', 'top'))
            room.set(xs[1], 5, z, AIR)
            room.set(xs[2], 5, z, c['trim_stairs'], stairs('east', 'top'))
            room.set(xs[0], 6, z, c['trim'])
            room.set(xs[1], 6, z, c['accent'])
            room.set(xs[2], 6, z, c['trim'])
            room.set(xs[1], 8, z, c['trim'])
        for z in (centre - 1, centre, centre + 1):
            room.set(xs[1], 0, z, c['runner'])
        room.set(xs[1], 11, centre, c['light'])
        room.set(xs[1], 6, 12, c['light'])
        room.set(xs[1], 6, 20, c['light'])
    for p in GALLERY_PIERS:
        for z in (centre - 3, centre + 3):
            room.set(p, 1, z, c['base'])
            for y in (2, 3, 4):
                room.set(p, y, z, c['trim'])
            room.set(p, 5, z, c['accent'])
            for y in (6, 7, 8):
                room.set(p, y, z, c['trim'])
        for z in (11, 12, 20, 21):
            room.set(p, 5, z, c['trim'])
        for plane in (10, 22):
            for y in range(1, 5):
                room.set(p, y, plane, c['trim'])
        room.set(p, 9, centre - 2, c['trim_stairs'], stairs('north', 'top'))
        room.set(p, 9, centre + 2, c['trim_stairs'], stairs('south', 'top'))
        room.set(p, 10, centre - 1, c['trim_stairs'], stairs('north', 'top'))
        room.set(p, 10, centre, c['accent'])
        room.set(p, 10, centre + 1, c['trim_stairs'], stairs('south', 'top'))

    slots = [(b, y, side) for b in range(len(bays)) for y in (2, 4) for side in ('north', 'south')
             if not (b == TREASURE_BAY and side == 'north')]
    lower = [slot for slot in slots if slot[1] == 2]
    loot = set(rng.sample(lower, 3))
    rest = [slot for slot in slots if slot not in loot]
    remains = set(rng.sample(rest, 4))
    broken = set(rng.sample([slot for slot in rest if slot not in remains], 4))
    for b, xs in enumerate(bays):
        for side, plane, recess, facing in (('north', 10, 9, 'south'), ('south', 22, 23, 'north')):
            if b == TREASURE_BAY and side == 'north':
                continue
            for x in xs:
                room.set(x, 1, plane, c['base'])
                room.set(x, 3, plane, c['trim'])
            for y in (2, 4):
                slot = (b, y, side)
                kind = 'loot' if slot in loot else 'remains' if slot in remains else \
                    'broken' if slot in broken else 'sealed'
                loculus(room, c, xs, y, plane, recess, kind, rng, facing)
    inscription(room, bays[ECHO_BAY][1], 3, 22, 'hemomancy:blood_echo_inscription', 'north',
                'hemomancy:mausoleum/saints_echo')

    dx = bays[TREASURE_BAY]
    for y in (1, 2, 3):
        room.set(dx[0], y, 10, c['trim'])
        room.set(dx[2], y, 10, c['trim'])
    room.set(dx[1], 3, 10, c['accent'])
    for x in dx:
        room.set(x, 4, 10, c['trim'])
    door = {'facing': 'north', 'hinge': 'left', 'open': 'false', 'powered': 'false'}
    room.set(dx[1], 1, 10, 'hemomancy:hematic_iron_door', dict(door, half='lower'))
    room.set(dx[1], 2, 10, 'hemomancy:hematic_iron_door', dict(door, half='upper'))
    room.set(dx[2], 2, 11, c['button'], {'face': 'wall', 'facing': 'south', 'powered': 'false'})
    for z in (8, 9):
        room.set(dx[1], 0, z, c['runner'])
        room.set(dx[1], 1, z, AIR)
        room.set(dx[1], 2, z, AIR)
    # Consecrated stone cannot be dug or built on, so the way out is part of the room: a plate inside the door.
    room.set(dx[1], 1, 9, c['button'].replace('_button', '_pressure_plate'), {'powered': 'false'})
    treasure_room(room, c, rng, dx[1])

    for x in (0, 26):
        arch(room, c, x, 0, centre)
        for z in (centre - 1, centre, centre + 1):
            room.set(x, 0, z, c['runner'])
    room.jigsaw(0, 0, centre, 'west_up', conn('gallery_in'), conn('descent_out'), EMPTY, c['runner'])
    room.jigsaw(26, 0, centre, 'east_up', conn('gallery_out'), conn('descent_turn_in'),
                pool_name(saint, 'descent_turn_0'), c['runner'], MANDATORY)
    return room


def treasure_room(room, c, rng, cx):
    """North of the gallery: a small reliquary with the treasure barrel on a plinth and ossuary shelves either side."""
    x0, x1, z0, z1 = cx - 4, cx + 4, 1, 7
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0, z1 + 1):
            for y in range(0, 6):
                inside = x0 < x < x1 and z0 < z
                if y == 0:
                    room.set(x, y, z, c['floor'])
                elif y == 5:
                    room.set(x, y, z, c['vault'])
                elif inside:
                    room.set(x, y, z, AIR)
                else:
                    room.set(x, y, z, field(rng, c))
    for x in range(x0 + 1, x1):
        for z in range(z0 + 1, z1 + 1):
            if x in (x0 + 1, x1 - 1) or z in (z0 + 1, z1):
                room.set(x, 0, z, c['inlay'])
        room.set(x, 4, z0 + 1, c['trim_stairs'], stairs('north', 'top'))
        room.set(x, 4, z1, c['trim_stairs'], stairs('south', 'top'))
    for z in range(z0 + 2, z1):
        room.set(x0 + 1, 4, z, c['trim_stairs'], stairs('west', 'top'))
        room.set(x1 - 1, 4, z, c['trim_stairs'], stairs('east', 'top'))
        room.set(cx, 0, z, c['runner'])
    room.set(cx, 5, 4, c['light'])
    for (x, z) in ((cx - 1, 4), (cx + 1, 4), (cx, 3), (cx, 5)):
        room.set(x, 5, z, c['accent'])
    room.set(cx, 1, z0 + 1, c['accent'])
    barrel(room, cx, 2, z0 + 1, 'south', 'hemomancy:chests/mausoleum_treasure')
    room.set(cx - 1, 1, z0 + 1, c['trim_stairs'], stairs('east'))
    room.set(cx + 1, 1, z0 + 1, c['trim_stairs'], stairs('west'))
    kit.wall_light(room, c, cx - 2, 3, z0, 'south')
    kit.wall_light(room, c, cx + 2, 3, z0, 'south')
    for z in (3, 4, 5):
        room.set(x0, 2, z, AIR)
        room.set(x0 - 1, 2, z, *bone('z'))
        room.set(x1, 2, z, AIR)
        room.set(x1 + 1, 2, z, *bone('z'))
    inscription(room, cx, 1, 5, 'hemomancy:rite_fragment_inscription', 'south',
                'hemomancy:mausoleum/sanguine_brotherhood_fragment')


DESCENT_PILASTERS = (1, 5, 9, 13, 17)


def descent_walk(x):
    """The block a visitor stands in at x: the top landing, six steps down, then the bottom landing."""
    if x <= 3:
        return 8
    if x <= 9:
        return 7 - (x - 4)
    return 1


def descent(saint):
    """The ossuary descent (Track 1): from the antechamber a grand stair drops seven blocks under a stepped vault, its
    walls banded with bone, onto the landing before the crypt gallery. Centre line z=5."""
    c = crypt(saint)
    rng = random.Random('descent/' + saint)
    room = Room(19, 14, 11)
    for x in range(19):
        for z in range(11):
            room.set(x, 0, z, c['floor'])
            for y in range(1, 10):
                room.set(x, y, z, field(rng, c))
            for y in range(10, 14):
                room.set(x, y, z, c['vault'])
    for x in range(1, 18):
        walk = descent_walk(x)
        for z in range(2, 9):
            for y in range(1, walk - 1):
                room.set(x, y, z, c['base'])
            if x <= 3:
                floor = c['inlay'] if z in (2, 8) else c['runner'] if z == 5 else c['floor']
                room.set(x, walk - 1, z, floor)
            elif x <= 9:
                room.set(x, walk - 1, z, c['trim_stairs'], stairs('west'))
            else:
                floor = c['inlay'] if z in (2, 8) else c['runner'] if z == 5 else \
                    c['floor_alt'] if rng.random() < 0.12 else c['floor']
                room.set(x, 0, z, floor)
            for y in range(walk, 10):
                room.set(x, y, z, AIR)
        room.set(x, 10, 2, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, 10, 8, c['vault_stairs'], stairs('south', 'top'))
        for z in range(3, 8):
            room.set(x, 10, z, AIR)
        room.set(x, 11, 3, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, 11, 7, c['vault_stairs'], stairs('south', 'top'))
        for z in (4, 5, 6):
            room.set(x, 11, z, AIR)
        room.set(x, 12, 4, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, 12, 5, AIR)
        room.set(x, 12, 6, c['vault_stairs'], stairs('south', 'top'))
        for plane, recess in ((1, 0), (9, 10)):
            room.set(x, walk, plane, c['base'])
            room.set(x, 9, plane, c['trim'])
            for y in (3, 6):
                if y <= walk:
                    continue
                roll = rng.random()
                if roll < 0.5:
                    room.set(x, y, plane, *bone('z'))
                elif roll < 0.8:
                    room.set(x, y, plane, AIR)
                    room.set(x, y, recess, *bone('z'))
    for x in range(13, 16):
        for z in range(4, 7):
            room.set(x, 0, z, c['inlay'])
    room.set(14, 0, 5, c['accent'])
    for p in DESCENT_PILASTERS:
        walk = descent_walk(p)
        for z in (2, 8):
            room.set(p, walk, z, c['base'])
            for y in range(walk + 1, 9):
                room.set(p, y, z, c['trim'])
            room.set(p, 9, z, c['accent'])
        room.set(p, 10, 2, c['trim_stairs'], stairs('north', 'top'))
        room.set(p, 10, 8, c['trim_stairs'], stairs('south', 'top'))
        room.set(p, 11, 3, c['trim_stairs'], stairs('north', 'top'))
        room.set(p, 11, 7, c['trim_stairs'], stairs('south', 'top'))
        room.set(p, 12, 4, c['trim_stairs'], stairs('north', 'top'))
        room.set(p, 12, 5, c['accent'])
        room.set(p, 12, 6, c['trim_stairs'], stairs('south', 'top'))
    for x in (3, 7, 11, 15):
        room.set(x, 13, 5, c['light'])
    for x in (11, 15):
        kit.wall_light(room, c, x, 5, 1, 'south')
        kit.wall_light(room, c, x, 5, 9, 'north')
    arch(room, c, 0, 7, 5)
    arch(room, c, 18, 0, 5)
    for z in (4, 5, 6):
        room.set(0, 7, z, c['runner'])
        room.set(18, 0, z, c['runner'])
    room.jigsaw(0, 6, 5, 'west_up', conn('descent_in'), conn('antechamber_out'), EMPTY, c['base'])
    room.jigsaw(18, 0, 5, 'east_up', conn('descent_out'), conn('gallery_in'), pool_name(saint, 'gallery'),
                c['runner'], MANDATORY)
    return room


# --- Track 3: the spiral --------------------------------------------------------------------------------------------

SHAFT = 15
TURN_DROP = 8
# The two turn pieces stacked: descent_turn_1 holds absolute rows 0-8, descent_turn_0 rows 9-22.
TURN_ROWS = {'descent_turn_1': (0, 9), 'descent_turn_0': (9, 14)}
SHAFT_TOP = 22
ENTRY_STAND = 17
EXIT_STAND = 1
WELL = range(5, 10)


def descent_turns(saint):
    """The spiral (Track 3, built by kit.stair_shaft): a square drum 15 across, a three-wide stair round an open well
    five across, two full turns sixteen down from the gallery's floor, leaving through the west wall under the way in.
    Built whole, then cut into its two stacked pieces, joined by an aligned down/up connector in the well."""
    c = crypt(saint)
    rng = random.Random('spiral/' + saint)
    whole = kit.stair_shaft(c, rng, kit.Shaft(SHAFT, SHAFT_TOP, ENTRY_STAND, turns=2))
    arch(whole, c, 0, ENTRY_STAND - 1, 2)
    arch(whole, c, 0, EXIT_STAND - 1, 2)
    turns = {}
    for name, (y0, height) in TURN_ROWS.items():
        room = Room(SHAFT, height, SHAFT)
        for (x, y, z), value in whole.blocks.items():
            if y0 <= y < y0 + height:
                room.blocks[(x, y - y0, z)] = value
        turns[name] = room
    upper, lower = turns['descent_turn_0'], turns['descent_turn_1']
    upper.jigsaw(0, ENTRY_STAND - 1 - 9, 2, 'west_up', conn('descent_turn_in'), conn('gallery_out'), EMPTY,
                 c['runner'])
    upper.jigsaw(7, 0, 7, 'down_north', conn('descent_turn_down'), conn('descent_turn_up'),
                 pool_name(saint, 'descent_turn_1'), AIR, MANDATORY, joint='aligned')
    lower.jigsaw(7, 8, 7, 'up_north', conn('descent_turn_up'), conn('descent_turn_down'), EMPTY, AIR,
                 joint='aligned')
    lower.jigsaw(0, EXIT_STAND - 1, 2, 'west_up', conn('descent_turn_out'), conn('threshold_in'),
                 pool_name(saint, 'threshold'), c['runner'], MANDATORY)
    return turns


# --- Track 2: the threshold hall ---------------------------------------------------------------------------------

THRESHOLD_PILASTERS = (1, 4, 7, 10, 13)
THRESHOLD_LINE = 7
WARD_STRIP = 'hemomancy:polished_venous_stone'


def threshold_hall(saint):
    """The threshold hall (Track 2): a short vaulted passage between the spiral's foot and the nave. The ward begins at
    the line in its floor (x=7): a strip of polished venous stone between trim bands, framed by a rib across the vault.
    The ward anchor is buried in the north wall behind the line. Floor at y=0, entry west, exit east, centre line z=5.
    Corridor air x 1-13, z 3-7, y 1-5 (the ward anchor's threshold slab is its x=7 cross-section); walls z 0-2 and
    8-10; stepped vault y 6-8, closed at y=9."""
    c = crypt(saint)
    rng = random.Random('threshold/' + saint)
    line = THRESHOLD_LINE
    room = Room(15, 10, 11)
    for x in range(15):
        for z in range(11):
            room.set(x, 0, z, c['floor'])
            for y in range(1, 9):
                room.set(x, y, z, field(rng, c))
            room.set(x, 9, z, c['vault'])
    for x in range(1, 14):
        for z in range(3, 8):
            for y in range(1, 6):
                room.set(x, y, z, AIR)
            floor = c['inlay'] if z in (3, 7) else c['runner'] if z == 5 else \
                c['floor_alt'] if rng.random() < 0.12 else c['floor']
            room.set(x, 0, z, floor)
        for plane in (2, 8):
            room.set(x, 1, plane, c['base'])
            room.set(x, 5, plane, c['trim'])
        room.set(x, 6, 3, c['trim_stairs'], stairs('north', 'top'))
        room.set(x, 6, 7, c['trim_stairs'], stairs('south', 'top'))
        for z in (4, 5, 6):
            room.set(x, 6, z, AIR)
        room.set(x, 7, 4, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, 7, 5, AIR)
        room.set(x, 7, 6, c['vault_stairs'], stairs('south', 'top'))
        room.set(x, 8, 5, c['vault'])
    for p in THRESHOLD_PILASTERS:
        for plane in (2, 8):
            room.set(p, 1, plane, c['base'])
            for y in (2, 3, 4):
                room.set(p, y, plane, c['trim'])
            room.set(p, 5, plane, c['accent'])
        room.set(p, 7, 4, c['trim_stairs'], stairs('north', 'top'))
        room.set(p, 7, 6, c['trim_stairs'], stairs('south', 'top'))
        room.set(p, 8, 5, c['accent'])
    for xs in ((2, 3), (11, 12)):
        for plane, recess in ((2, 1), (8, 9)):
            for x in xs:
                room.set(x, 3, plane, AIR)
                room.set(x, 3, recess, *bone('x'))
                room.set(x, 2, plane, c['trim'])
                room.set(x, 4, plane, c['trim'])
    for xs in ((5, 6), (8, 9)):
        for plane in (2, 8):
            for x in xs:
                room.set(x, 3, plane, c['accent'])
    for x in (3, 5, 9, 11):
        room.set(x, 8, 5, c['light'])
    # Sconces set into the walls over the accent bands (the corridor's air stays clear for the ward's slab).
    for x in (5, 9):
        kit.sconce(room, x, 4, 2, 'south')
        kit.sconce(room, x, 4, 8, 'north')
    for z in range(3, 8):
        room.set(line - 1, 0, z, c['trim'])
        room.set(line, 0, z, WARD_STRIP)
        room.set(line + 1, 0, z, c['trim'])
    for plane in (2, 8):
        room.set(line, 1, plane, c['accent'])
        room.set(line, 5, plane, c['accent'])
        room.set(line, 6, plane, c['trim'])
    room.set(line, 6, 3, c['trim'])
    room.set(line, 6, 7, c['trim'])
    room.set(line, 7, 4, c['trim'])
    room.set(line, 7, 6, c['trim'])
    room.set(line, 8, 5, c['accent'])
    room.set(line, 3, 1, 'hemomancy:ward_anchor', {'facing': 'east'}, Compound({
        'id': String('hemomancy:ward_anchor'), 'Saint': String(saint.upper()),
        'ThresholdFrom': IntArray([0, -2, 2]), 'ThresholdTo': IntArray([0, 2, 6])}))
    for x in (0, 14):
        arch(room, c, x, 0, 5)
        for z in (4, 5, 6):
            room.set(x, 0, z, c['runner'])
    room.jigsaw(0, 0, 5, 'west_up', conn('threshold_in'), conn('descent_turn_out'), EMPTY, c['runner'])
    room.jigsaw(14, 0, 5, 'east_up', conn('threshold_out'), conn('nave_in'), pool_name(saint, 'nave'), c['runner'],
                MANDATORY)
    return room


# --- the nave -------------------------------------------------------------------------------------------------------

NAVE_L, NAVE_H, NAVE_W = 31, 16, 21
NAVE_ZC = 10
NAVE_PIERS = (1, 5, 9, 13, 17, 21, 25, 29)
NAVE_GALLERY = 6
# Wing doorways: slot -> (x, floor y, side). 'low' is the side the level and crypt wings leave by, 'high' the side
# the upper wing leaves by from the gallery. build.LOW_SIDE fixes which template side each is so the upper wing turns
# away from the crypt gallery overhead.
WING_DOORS = {0: (10, 0, 'low'), 1: (17, NAVE_GALLERY, 'high'), 2: (25, 0, 'low')}
GALLERY_STAIR = range(2, 3 + NAVE_GALLERY)
# The congregation's places in the vessel, left empty: a theme fills them through an optional
# nave_sockets(room, c, sockets) hook in its themes/<saint>.py. Each is (kind, (x, y, z), facing): the floor cell a
# two-tall piece stands in, facing east down the vessel to the ring door. Rows of three either side of the walk: the
# runner and one block either side of it (z 9-11) stay clear, as do the entry end and the gallery stairs' bays
# (x 1-5), the level wing's bay (x 9-13) and the crypt wing's bays and the door's apron (x 21-29).
NAVE_ROWS = (6, 8, 14, 16, 18, 20)
NAVE_PEWS = (NAVE_ZC - 4, NAVE_ZC - 3, NAVE_ZC - 2, NAVE_ZC + 2, NAVE_ZC + 3, NAVE_ZC + 4)
NAVE_SOCKETS = [('congregation', (x, 1, z), 'east') for x in NAVE_ROWS for z in NAVE_PEWS]


def nave(saint, low_side):
    """The nave (Track 3): 31 long, 21 wide, 16 tall. A central vessel nine wide under a stepped barrel vault, arcades of
    eight piers either side, aisles behind them with loculi in the outer walls, and a gallery over each aisle at y=6
    (four clear over five) looking down into the vessel, reached by a stair climbing each aisle from the entry end. The ring door closes the
    east end; the wings leave through the side walls (WING_DOORS). Centre line z=10."""
    c = crypt(saint)
    rng = random.Random('nave/' + saint)
    zc = NAVE_ZC
    room = Room(NAVE_L, NAVE_H, NAVE_W)
    mass(room, c, rng, 0, NAVE_L - 1, 0, NAVE_H - 1, 0, NAVE_W - 1)
    sides = {'north': (2, 3, 4, 5, 1, 0), 'south': (18, 17, 16, 15, 19, 20)}  # aisle cells, arcade, wall, back
    for x in range(1, NAVE_L - 1):
        # The vessel: floor, air to the cornice, then the vault from y=11 to its crown at y=15.
        for z in range(zc - 4, zc + 5):
            room.set(x, 0, z, kit.paved(c, rng, abs(z - zc), 4))
            for y in range(1, 11):
                room.set(x, y, z, AIR)
        kit.vault_x(room, c, x, zc, 4, 11)
        for side, (a0, a1, a2, arcade, wall, back) in sides.items():
            for z in (a0, a1, a2):
                room.set(x, 0, z, c['floor_alt'] if rng.random() < 0.1 else c['floor'])
                for y in range(1, NAVE_GALLERY):
                    room.set(x, y, z, AIR)
                room.set(x, NAVE_GALLERY, z, c['floor'])
                for y in range(NAVE_GALLERY + 1, 11):
                    room.set(x, y, z, AIR)
                room.set(x, 11, z, c['vault'])
            # Arcade plane: open bays between the piers, the gallery's edge and its balustrade above.
            for y in range(1, NAVE_GALLERY - 2):
                room.set(x, y, arcade, AIR)
            room.set(x, 0, arcade, c['inlay'])
            room.set(x, NAVE_GALLERY, arcade, c['inlay'])
            room.set(x, NAVE_GALLERY + 1, arcade, c['trim_wall'], rail('x'))
            for y in range(NAVE_GALLERY + 2, 11):
                room.set(x, y, arcade, AIR)
            # Outer wall: a base course, a cornice under the gallery, a frieze along the gallery.
            room.set(x, 1, wall, c['base'])
            room.set(x, NAVE_GALLERY - 1, wall, c['trim'])
            room.set(x, NAVE_GALLERY + 1, wall, c['base'])
            room.set(x, 10, wall, c['trim'])
    for p in NAVE_PIERS:
        for side, (a0, a1, a2, arcade, wall, back) in sides.items():
            room.set(p, 1, arcade, c['base'])
            for y in range(2, NAVE_GALLERY - 1):
                room.set(p, y, arcade, c['trim'])
            room.set(p, NAVE_GALLERY - 1, arcade, c['accent'])
            room.set(p, NAVE_GALLERY + 1, arcade, c['trim_wall'], post())
            for y in range(NAVE_GALLERY + 2, 11):
                room.set(p, y, arcade, c['trim'])
            room.set(p, 10, arcade, c['accent'])
            for y in range(1, 11):
                room.set(p, y, wall, c['trim'])
            room.set(p, NAVE_GALLERY - 1, wall, c['accent'])
        kit.rib_x(room, c, p, zc, 4, 11)
        room.set(p, 0, zc, c['inlay'])
    for b, p in enumerate(NAVE_PIERS[:-1]):
        xs = (p + 1, p + 2, p + 3)
        for side, (a0, a1, a2, arcade, wall, back) in sides.items():
            room.set(xs[0], NAVE_GALLERY - 2, arcade, c['trim_stairs'], stairs('west', 'top'))
            room.set(xs[1], NAVE_GALLERY - 2, arcade, AIR)
            room.set(xs[2], NAVE_GALLERY - 2, arcade, c['trim_stairs'], stairs('east', 'top'))
            room.set(xs[0], NAVE_GALLERY - 1, arcade, c['trim'])
            room.set(xs[1], NAVE_GALLERY - 1, arcade, c['accent'])
            room.set(xs[2], NAVE_GALLERY - 1, arcade, c['trim'])
            # Loculi in the aisle walls, and lights in the gallery wall.
            facing = 'south' if side == 'north' else 'north'
            if not any(abs(xs[1] - door[0]) <= 2 for door in WING_DOORS.values()) and xs[0] > GALLERY_STAIR[-1]:
                kind = 'remains' if rng.random() < 0.3 else 'broken' if rng.random() < 0.3 else 'sealed'
                loculus(room, c, xs, 3, wall, back, kind, rng, facing)
                room.set(xs[0], 2, wall, c['trim'])
                room.set(xs[1], 2, wall, c['trim'])
                room.set(xs[2], 2, wall, c['trim'])
        if b % 2 == 0:
            room.set(xs[1], 15, zc, c['light'])
    # The gallery stairs: up each aisle from the entry end, under an open stairwell in the gallery floor.
    for side, (a0, a1, a2, arcade, wall, back) in sides.items():
        for i, x in enumerate(GALLERY_STAIR):
            stand = 1 + i if i else 1
            for z in (a0, a1, a2):
                for y in range(1, stand - 1):
                    room.set(x, y, z, c['base'])
                if i:
                    room.set(x, stand - 1, z, c['trim_stairs'], stairs('east'))
                for y in range(stand, 11):
                    room.set(x, y, z, AIR)
            room.set(x, NAVE_GALLERY + 1, arcade, AIR)
            if room.get(x, stand, arcade) == AIR:
                room.set(x, stand, arcade, c['trim_wall'], rail('x'))
        for z in (a0, a1, a2):
            room.set(GALLERY_STAIR[-1] + 1, NAVE_GALLERY, z, c['inlay'])
        # Over the stairwell the aisle rises two storeys: a half vault leans off the outer wall.
        for x in GALLERY_STAIR:
            room.set(x, 10, a0, c['vault_stairs'], stairs(side, 'top'))
        # Sconces along the gallery wall, every other bay, hung over the gallery walk.
        for b, p in enumerate(NAVE_PIERS[:-1]):
            if b % 2 == 1:
                kit.wall_light(room, c, p + 2, 9, wall, 'south' if side == 'north' else 'north')
    ring_door(room, c, saint)
    arch(room, c, 0, 0, zc)
    room.jigsaw(0, 0, zc, 'west_up', conn('nave_in'), conn('threshold_out'), EMPTY, c['runner'])
    room.jigsaw(NAVE_L - 1, 0, zc, 'east_up', conn('nave_out'), conn('chamber_in'), pool_name(saint, 'chamber'),
                c['track'], MANDATORY)
    high_side = 'south' if low_side == 'north' else 'north'
    for slot, (x, floor, side) in WING_DOORS.items():
        face = low_side if side == 'low' else high_side
        plane, passage, edge = (1, (0,), 0) if face == 'north' else (NAVE_W - 2, (NAVE_W - 1,), NAVE_W - 1)
        arch_z(room, c, plane, floor, x)
        for z in passage:
            for y in range(floor + 1, floor + 4):
                for dx in (-1, 0, 1):
                    room.set(x + dx, y, z, AIR)
        for dx in (-1, 0, 1):
            room.set(x + dx, floor, plane, c['runner'])
            room.set(x + dx, floor, edge, c['runner'])
        room.jigsaw(x, floor, edge, face + '_up', conn('wing_out'), conn('wing_in'),
                    pool_name(saint, 'wing_%d' % slot), c['runner'], MANDATORY)
    fill_sockets = getattr(theme(saint), 'nave_sockets', None)
    if fill_sockets:
        fill_sockets(room, c, NAVE_SOCKETS)
    return room


# The ring door (Track 5): one multiblock, a 5 x 5 face one deep in the nave's east end wall, its controller at the
# bottom centre facing west into the nave. Its 24 fillers grow from the controller when the template is placed
# (FillersPlaced 0), so those cells are authored as air. Open, the frame keeps its outer columns and top row and the
# passage through it is 3 wide and 4 tall; the slab sinks straight down through the floor track under the door.
RING_DOOR_HALF = 2
RING_DOOR_H = 5


def track_course(room, c, x, floor, zc):
    """The floor track a ring door's slab sinks into: a dark course the door's width, accent stones at its ends."""
    for d in range(-RING_DOOR_HALF, RING_DOOR_HALF + 1):
        room.set(x, floor, zc + d, c['accent'] if abs(d) == RING_DOOR_HALF else c['track'])


def ring_door(room, c, saint):
    """The door and its surround in the east end wall (x = NAVE_L-1), symmetric about the centre line. Across the
    vessel's nine-wide end wall (d = z - zc): pilasters at d +-3 rising from the base course to accent capitals in the
    lintel course over the door (y 6); a stepped arch springing from the capitals, one course in per course, its
    tympanum a panel with a lamp at its heart and an archivolt of trim outside its haunch stairs, closed by the accent
    keystone at y 10, under the vault's spring."""
    zc, x = NAVE_ZC, NAVE_L - 1
    top = RING_DOOR_H + 1
    for d in range(-RING_DOOR_HALF, RING_DOOR_HALF + 1):
        for y in range(1, top):
            room.set(x, y, zc + d, AIR)
    room.set(x, 1, zc, 'hemomancy:ring_door', {'facing': 'west', 'open': 'false'}, Compound({
        'id': String('hemomancy:ring_door'), 'Saint': String(saint.upper()), 'FillersPlaced': Byte(0)}))
    track_course(room, c, x, 0, zc)
    for side, outward in ((-1, 'north'), (1, 'south')):
        pilaster, wall = zc + side * 3, zc + side * 4
        room.set(x, 1, pilaster, c['base'])
        room.set(x, 1, wall, c['base'])
        for y in range(2, top):
            room.set(x, y, pilaster, c['trim'])
        room.set(x, top, pilaster, c['accent'])
        room.set(x, top, wall, c['trim'])
        # The arch: haunch stairs stepping in from the capital, the archivolt over each step.
        for step in range(3):
            y = top + 1 + step
            room.set(x, y, zc + side * (3 - step), c['trim_stairs'], stairs(outward, 'top'))
            room.set(x, y + 1, zc + side * (3 - step), c['trim'])
    for d in range(-RING_DOOR_HALF, RING_DOOR_HALF + 1):
        room.set(x, top, zc + d, c['trim'])
    for y in range(top + 1, top + 4):
        reach = 2 - (y - top - 1)
        for d in range(-reach, reach + 1):
            room.set(x, y, zc + d, c['seal'])
    room.set(x, top + 2, zc, c['light'])
    room.set(x, top + 4, zc, c['accent'])
    # The apron: an inlay sill the surround's width before the door, an accent on the centre line behind it.
    for d in range(-3, 4):
        room.set(x - 1, 0, zc + d, c['inlay'])
    room.set(x - 2, 0, zc, c['accent'])


# --- wings ----------------------------------------------------------------------------------------------------------

def niche_door(room, c, saint, x, floor, plane, passage, edge, face):
    """An opening in a side wall for an optional niche: the arch, the passage through the wall, the connector."""
    arch_z(room, c, plane, floor, x)
    for z in passage:
        for y in range(floor + 1, floor + 4):
            for dx in (-1, 0, 1):
                room.set(x + dx, y, z, AIR)
        for dx in (-1, 0, 1):
            room.set(x + dx, floor, z, c['runner'])
    room.jigsaw(x, floor, edge, face + '_up', conn('niche_out'), conn('niche_in'), pool_name(saint, 'niche'),
                c['runner'])


def wing_ends(room, c, saint, kind, entry_floor, exit_floor, zc, length):
    arch(room, c, 0, entry_floor, zc)
    arch(room, c, length - 1, exit_floor, zc)
    for z in (zc - 1, zc, zc + 1):
        room.set(0, entry_floor, z, c['runner'])
        room.set(length - 1, exit_floor, z, c['runner'])
    room.jigsaw(0, entry_floor, zc, 'west_up', conn('wing_in'), conn('wing_out'), EMPTY, c['runner'])
    room.jigsaw(length - 1, exit_floor, zc, 'east_up', conn('approach_out'), conn('hall_in'),
                pool_name(saint, kind.lower() + '_hall'), c['runner'], MANDATORY)


def approach_corridor(saint, kind):
    """A level approach, layout a: a three-wide vaulted corridor with a niche off either side."""
    c = crypt(saint)
    rng = random.Random('approach_a/%s/%s' % (saint, kind))
    room = Room(11, 8, 9)
    mass(room, c, rng, 0, 10, 0, 7, 0, 8)
    corridor(room, c, rng, 1, 9, 4, 1, 0, 5, pilasters=4, lights=8, phase=1)
    for x in (1, 2, 8, 9):
        for plane, recess in ((2, 1), (6, 7)):
            room.set(x, 3, plane, AIR)
            room.set(x, 3, recess, *bone('x'))
            room.set(x, 2, plane, c['trim'])
            room.set(x, 4, plane, c['trim'])
    niche_door(room, c, saint, 5, 0, 2, (0, 1), 0, 'north')
    niche_door(room, c, saint, 5, 0, 6, (7, 8), 8, 'south')
    wing_ends(room, c, saint, kind, 0, 0, 4, 11)
    return room


def approach_ossuary(saint, kind):
    """A level approach, layout b: a five-wide ossuary passage, its walls two tiers of loculi, one niche to the south."""
    c = crypt(saint)
    rng = random.Random('approach_b/%s/%s' % (saint, kind))
    room = Room(11, 9, 11)
    mass(room, c, rng, 0, 10, 0, 8, 0, 10)
    corridor(room, c, rng, 1, 9, 5, 2, 0, 5, pilasters=4, lights=4)
    for xs in ((3, 4, 5), (7, 8, 9)):
        for plane, recess, facing in ((2, 1, 'south'), (8, 9, 'north')):
            if plane == 8 and xs[1] == 4:
                continue
            for y in (2, 4):
                kind_of = 'remains' if rng.random() < 0.4 else 'broken' if rng.random() < 0.3 else 'sealed'
                loculus(room, c, xs, y, plane, recess, kind_of, rng, facing)
            for x in xs:
                room.set(x, 3, plane, c['trim'])
    niche_door(room, c, saint, 4, 0, 8, (9, 10), 10, 'south')
    wing_ends(room, c, saint, kind, 0, 0, 5, 11)
    return room


CRYPT_DROP = 5


def crypt_stand_straight(x, drop=CRYPT_DROP):
    if x <= 2:
        return 1 + drop
    if x <= 2 + drop:
        return 1 + drop - (x - 2)
    return 1


def crypt_stand_landing(x, drop=CRYPT_DROP):
    if drop != CRYPT_DROP:
        return crypt_stand_straight(x, drop)
    return {0: 6, 1: 6, 2: 6, 3: 5, 4: 4, 5: 4, 6: 4, 7: 4, 8: 3, 9: 2, 10: 1}.get(x, 1)


def crypt_approach(saint, kind, layout):
    """The crypt wing's approach: a stair down a flight of five under a vault that keeps its height, so the hall at
    its foot is a full storey under the nave's floor. Layout a is one straight flight with a niche at the bottom;
    layout b breaks the flight at a landing with a niche off it. A theme whose crypt hall does its own descending (its
    CRYPT_DROP, at least 1) shortens the flight: the chamber stays the lowest room."""
    c = crypt(saint)
    rng = random.Random('crypt_%s/%s/%s' % (layout, saint, kind))
    drop = getattr(theme(saint), 'CRYPT_DROP', CRYPT_DROP)
    shape = crypt_stand_straight if layout == 'a' else crypt_stand_landing
    stand_at = lambda x: shape(x, drop)
    room = Room(13, 12, 9)
    mass(room, c, rng, 0, 12, 0, 11, 0, 8)
    zc = 4
    for x in range(1, 12):
        stand = stand_at(x)
        for z in range(zc - 1, zc + 2):
            for y in range(1, stand - 1):
                room.set(x, y, z, c['base'])
            if stand < stand_at(x - 1):
                room.set(x, stand - 1, z, c['trim_stairs'], stairs('west'))
            else:
                room.set(x, stand - 1, z, kit.paved(c, rng, abs(z - zc), 1))
            for y in range(stand, 10):
                room.set(x, y, z, AIR)
        for plane in (zc - 2, zc + 2):
            room.set(x, stand, plane, c['base'])
            room.set(x, 9, plane, c['trim'])
        kit.vault_x(room, c, x, zc, 1, 10)
        if x % 4 == 1:
            for plane in (zc - 2, zc + 2):
                for y in range(stand + 1, 9):
                    room.set(x, y, plane, c['trim'])
                room.set(x, 9, plane, c['accent'])
            kit.rib_x(room, c, x, zc, 1, 10)
        elif x % 4 == 3:
            for plane, recess in ((zc - 2, zc - 3), (zc + 2, zc + 3)):
                room.set(x, stand + 3, plane, AIR)
                room.set(x, stand + 3, recess, *bone('x'))
        if x in (3, 7, 11):
            room.set(x, 10, zc, c['light'])
    if layout == 'a':
        niche_door(room, c, saint, 9, 0, zc - 2, (zc - 4, zc - 3), 0, 'north')
    else:
        niche_door(room, c, saint, 6, stand_at(6) - 1, zc + 2, (zc + 3, zc + 4), 8, 'south')
    # Sconces hung on alternating pilasters at head height over their step (not where a niche door opened the wall).
    for x in (5, 9):
        kit.pilaster_sconces(room, c, x, zc, 1, stand_at(x) + 3)
    wing_ends(room, c, saint, kind, drop, 0, zc, 13)
    return room


# --- trial halls and focus rooms ------------------------------------------------------------------------------------

def hall_shell(saint, key, length, height, width, half, clear, lights=6):
    c = crypt(saint)
    rng = random.Random('%s/%s' % (key, saint))
    zc = width // 2
    room = Room(length, height, width)
    mass(room, c, rng, 0, length - 1, 0, height - 1, 0, width - 1)
    corridor(room, c, rng, 1, length - 1, zc, half, 0, clear, pilasters=4, lights=lights)
    arch(room, c, 0, 0, zc)
    for z in (zc - 1, zc, zc + 1):
        room.set(0, 0, z, c['runner'])
    return room, c, rng, zc


def hall_sockets(zc, marks):
    """The trial hall's set-piece sockets, left empty unless a theme fills them through an optional
    hall_sockets(room, c, sockets) hook: (kind, (x, y, z), facing). entry_flank is either side of the entry arch, just
    inside the hall, facing in. A socket that would stand on or beside a mark is left out."""
    sockets = [('entry_flank', (1, 1, zc + side * 2), 'east') for side in (-1, 1)]
    return [s for s in sockets if all(max(abs(s[1][0] - m.pos[0]), abs(s[1][2] - m.pos[2])) > 1 for m in marks)]


def hall_connectors(room, c, saint, kind, zc, marks):
    fill_sockets = getattr(theme(saint), 'hall_sockets', None)
    if fill_sockets:
        fill_sockets(room, c, hall_sockets(zc, marks))
    room.jigsaw(0, 0, zc, 'west_up', conn('hall_in'), conn('approach_out'), EMPTY, c['runner'])
    room.jigsaw(room.size[0] - 1, 0, zc, 'east_up', conn('hall_out'), conn('focus_in'),
                pool_name(saint, kind.lower() + '_focus'), c['runner'], MANDATORY)


def beholding_hall(saint):
    """The Beholding: a long hall where the memory plays. Four witness steles stand either side of the walk, two near
    the door and two near the focus, all in sight from the focus at the end; the player walks back to each in turn."""
    room, c, rng, zc = hall_shell(saint, 'beholding', 19, 12, 13, 4, 6)
    marks = [trials.Mark(5, 1, zc - 3, 'WITNESS', 0), trials.Mark(5, 1, zc + 3, 'WITNESS', 1),
             trials.Mark(12, 1, zc - 3, 'WITNESS', 2), trials.Mark(12, 1, zc + 3, 'WITNESS', 3)]
    for mark in marks:
        x, y, z = mark.pos
        for (cx, cz) in kit.ring(x, z, 1.4):
            if abs(cz - zc) <= 4:
                room.set(cx, 0, cz, c['inlay'])
        trials.witness(room, c, mark, 'south' if z < zc else 'north')
        # A sconce on the wall behind each stele, so all four read from the focus when the memory plays.
        kit.wall_light(room, c, x, 3, zc - 5 if z < zc else zc + 5, 'south' if z < zc else 'north')
    for x in range(1, 19):
        room.set(x, 0, zc, c['runner'])
    hall_connectors(room, c, saint, 'BEHOLDING', zc, marks)
    return room, marks


def litany_hall(saint):
    """The Litany: a scriptorium-ossuary. The litany stone stands at the door with the words to give; the six candidates
    wait on stands along the walls; the focus at the end takes three of them, in the stone's order."""
    room, c, rng, zc = hall_shell(saint, 'litany', 19, 10, 11, 3, 5)
    order = list(range(6))
    random.Random('litany_stands/' + saint).shuffle(order)
    marks = [trials.Mark(2, 1, zc - 3, 'LITANY_STONE', 0)]
    trials.litany_stone(room, c, marks[0], 'south')
    stands = [(6, zc - 3), (10, zc - 3), (14, zc - 3), (6, zc + 3), (10, zc + 3), (14, zc + 3)]
    for i, (x, z) in enumerate(stands):
        mark = trials.Mark(x, 1, z, 'OFFERING_STAND', order[i])
        trials.offering_stand(room, c, mark)
        room.set(x, 0, z, c['accent'])
        marks.append(mark)
    for x in range(3, 18, 2):
        for plane, recess in ((zc - 4, zc - 5), (zc + 4, zc + 5)):
            if room.get(x, 3, plane) not in (c['trim'], c['accent']):
                room.set(x, 3, plane, AIR)
                room.set(x, 3, recess, *bone('x'))
    hall_connectors(room, c, saint, 'LITANY', zc, marks)
    return room, marks


def downfall_hall(saint):
    """The Downfall: a gauntlet down a long, broken hall. The trial starts at the gate mark by the door; each wave rises
    from its pair of seals further down as the challenger advances, and the fall ends at the focus."""
    room, c, rng, zc = hall_shell(saint, 'downfall', 25, 11, 11, 3, 6)
    marks = [trials.Mark(2, 1, zc, 'START', 0)]
    room.set(2, 0, zc, c['accent'])
    for z in range(zc - 3, zc + 4):
        room.set(3, 0, z, c['trim'])
    for wave in range(3):
        x = 7 + wave * 6
        for z in (zc - 2, zc + 2):
            mark = trials.Mark(x, 1, z, 'SPAWN', wave)
            trials.spawn_seal(room, c, mark)
            marks.append(mark)
    # The fall: cracked flags and toppled stones along the walls.
    for x in range(4, 24):
        for z in (zc - 3, zc + 3):
            if rng.random() < 0.25:
                room.set(x, 0, z, c['floor_alt'])
        if x % 5 == 2:
            z = zc - 3 if x % 2 else zc + 3
            room.set(x, 1, z, c['trim_stairs'], stairs('east' if x % 2 else 'west'))
    hall_connectors(room, c, saint, 'DOWNFALL', zc, marks)
    return room, marks


def repose_hall(saint):
    """The Repose: a square hall of stillness. A ring in the floor marks where a body must keep still; a pylon in each
    corner fires the pulses."""
    room, c, rng, zc = hall_shell(saint, 'repose', 15, 14, 13, 5, 7, lights=0)
    centre = (7, zc)
    marks = [trials.Mark(centre[0], 1, centre[1], 'CIRCLE', 0)]
    for (x, z) in kit.ring(centre[0], centre[1], trials.REPOSE_RADIUS):
        room.set(x, 0, z, c['accent'])
    room.set(centre[0], 0, centre[1], c['inlay'])
    for i, (x, z) in enumerate(((2, zc - 4), (12, zc - 4), (2, zc + 4), (12, zc + 4))):
        mark = trials.Mark(x, 3, z, 'PYLON', i)
        trials.pylon(room, c, mark, 'south' if z < zc else 'north')
        marks.append(mark)
    room.set(centre[0], 13, zc, c['light'])
    hall_connectors(room, c, saint, 'REPOSE', zc, marks)
    return room, marks


HALLS = {'BEHOLDING': beholding_hall, 'LITANY': litany_hall, 'DOWNFALL': downfall_hall, 'REPOSE': repose_hall}


def hall(saint, kind):
    """A kind's trial hall and its marks: the theme's own builder when its HALLS names one (same size at its exit end,
    same connectors, the focus room's run unchanged), else the shared one."""
    return getattr(theme(saint), 'HALLS', {}).get(kind, HALLS[kind])(saint)

# The focus room continues its hall's run (same width, height and centre line) and closes it with an apse.
FOCUS_ROOMS = {'BEHOLDING': (9, 12, 13, 4, 6), 'LITANY': (9, 10, 11, 3, 5), 'DOWNFALL': (7, 11, 11, 3, 6),
               'REPOSE': (7, 14, 13, 5, 7)}


def focus_room(saint, kind, slot, hall, marks):
    """The kind's focus room: the hall's run continued to an apse wall, the focus on an inlaid dais near it. Every mark
    in the hall is written into the focus's NBT as an offset from the focus (Track 3)."""
    length, height, width, half, clear = FOCUS_ROOMS[kind]
    c = crypt(saint)
    rng = random.Random('focus/%s/%s' % (kind, saint))
    zc = width // 2
    room = Room(length, height, width)
    mass(room, c, rng, 0, length - 1, 0, height - 1, 0, width - 1)
    corridor(room, c, rng, 0, length - 2, zc, half, 0, clear, pilasters=4, lights=0)
    fx = length - 4
    for (x, z) in kit.ring(fx, zc, 1.8):
        room.set(x, 0, z, c['inlay'])
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            room.set(fx + dx, 0, zc + dz, c['accent'] if dx == dz == 0 else c['trim'])
    # The apse: a blind arch with lights either side, over a bench of the base course.
    for z in range(zc - half, zc + half + 1):
        room.set(length - 2, 1, z, c['base'])
    for z in (zc - 2, zc + 2):
        kit.wall_light(room, c, length - 1, 4, z, 'west')
    for y in range(1, 6):
        room.set(length - 1, y, zc, c['accent'] if y in (1, 5) else c['trim'])
    hall_length = hall.size[0]
    focus_in_hall = (hall_length + fx, 1, zc)
    offsets = [((m.pos[0] - focus_in_hall[0], m.pos[1] - focus_in_hall[1], m.pos[2] - focus_in_hall[2]), m)
               for m in marks]
    room.set(fx, 1, zc, 'hemomancy:chapel_focus', {'facing': 'north', 'lit': 'false'},
             trials.focus_nbt(saint.upper(), kind, slot, offsets))
    room.jigsaw(0, 0, zc, 'west_up', conn('focus_in'), conn('hall_out'), EMPTY, c['runner'])
    return room


# --- niches ---------------------------------------------------------------------------------------------------------

def niche(saint, layout):
    """An optional alcove off an approach: three deep, five wide, four tall under a short vault. The ossuary is bones
    and sealed loculi, the reliquary a loot urn on a plinth, the shrine a lit stele."""
    c = crypt(saint)
    rng = random.Random('niche_%s/%s' % (layout, saint))
    room = Room(5, 7, 7)
    mass(room, c, rng, 0, 4, 0, 6, 0, 6)
    zc = 3
    for x in range(0, 4):
        for z in range(1, 6):
            room.set(x, 0, z, kit.paved(c, rng, abs(z - zc), 2))
            for y in range(1, 5):
                room.set(x, y, z, AIR)
        room.set(x, 5, 1, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, 5, 5, c['vault_stairs'], stairs('south', 'top'))
        for z in range(2, 5):
            room.set(x, 5, z, AIR)
        room.set(x, 6, 2, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, 6, 4, c['vault_stairs'], stairs('south', 'top'))
        room.set(x, 6, 3, c['vault'])
    for z in range(1, 6):
        room.set(3, 1, z, c['base'])
    if layout == 'ossuary':
        for z in range(1, 6):
            for y in (2, 3):
                room.set(4, y, z, AIR)
                if rng.random() < 0.6:
                    room.set(3, y, z, *bone('z'))
                else:
                    room.set(3, y, z, c['seal'])
        room.set(2, 1, zc, *bone('y'))
    elif layout == 'reliquary':
        room.set(3, 1, zc, c['accent'])
        barrel(room, 3, 2, zc, 'west', 'hemomancy:chests/mausoleum')
        for z in (zc - 2, zc + 2):
            room.set(3, 2, z, *bone('y'))
    else:
        room.set(3, 2, zc, c['accent'])
        room.set(3, 3, zc, c['light'])
        room.set(3, 4, zc, c['trim_stairs'], stairs('east'))
        for z in (zc - 1, zc + 1):
            room.set(2, 1, z, c['trim_stairs'], stairs('east'))
    room.set(1, 6, zc, c['light'])
    # The mouth: an arch three wide in the alcove's open end, framing it from the approach's passage.
    arch(room, c, 0, 0, zc)
    for z in (zc - 2, zc + 2):
        room.set(0, 4, z, c['trim'])
    room.jigsaw(0, 0, zc, 'west_up', conn('niche_in'), conn('niche_out'), EMPTY, c['runner'])
    return room


def niche_wall(saint):
    """The fallback when no niche fits: a blind arch one block deep with bones laid in its back wall."""
    c = crypt(saint)
    rng = random.Random('niche_wall/' + saint)
    room = Room(2, 6, 5)
    mass(room, c, rng, 0, 1, 0, 5, 0, 4)
    for z in range(1, 4):
        room.set(0, 0, z, c['runner'] if z == 2 else c['floor'])
        for y in range(1, 4):
            room.set(0, y, z, AIR)
        room.set(1, 2, z, *bone('z'))
    room.set(1, 1, 2, c['accent'])
    arch(room, c, 0, 0, 2)
    room.jigsaw(0, 0, 2, 'west_up', conn('niche_in'), conn('niche_out'), EMPTY, c['runner'])
    return room


# --- the chamber ----------------------------------------------------------------------------------------------------

# Track 4. Along x from the ring door: the open door's passage through the west wall, directly behind the door's
# column in the nave (its sill and floor track at CHAMBER_ENTRY; chamber_entry), the processional (a
# landing and six stairs down, x 1-8), the pillared hall (x 9-25) with the boss floor at its centre and an aisle behind
# each pillar rank, the dais' three steps (x 24-26), and the apse under its own vault (x 26-34), the tomb on its top
# step. Everything is symmetric about the centre line z = CHAMBER_ZC.
CHAMBER_L, CHAMBER_H, CHAMBER_W = 37, 17, 27
CHAMBER_ZC = 13
CHAMBER_ENTRY = 6
CHAMBER_WAY = 3                         # the processional's half width
CHAMBER_PILLARS = (9, 13, 17, 21, 25)   # both ranks, on the planes zc +- CHAMBER_RANK
CHAMBER_BAYS = (11, 15, 19, 23)         # the bay centres between them
CHAMBER_RANK = 8
CHAMBER_AISLE = 10                      # each aisle's centre line, off zc; aisles are 3 wide
CHAMBER_CLEAR = (9, 23)                 # the boss floor: x 9-23, z zc +- 7, floor course y 0, open to the vault
CHAMBER_STEPS = (24, 25, 26)            # one riser each, the top step's course is CHAMBER_DAIS
CHAMBER_DAIS = 3
# The apse's half width at each x: the triumphal arch (x 26-27, springing a course below the nave), the tomb bay, then
# the conch drawing in round the head of the tomb.
CHAMBER_APSE = {26: 7, 27: 7, 28: 7, 29: 7, 30: 7, 31: 7, 32: 7, 33: 6, 34: 5}
CHAMBER_BACK = 35                       # the apse's back wall
# The sarcophagus' main block, facing west. Its cells are x 29-32, y 4-5, z zc-1..zc+1; its lid slides off over
# z = zc-2 at y 4-5, which stays open; the Saint rises at (31.0, 4, zc+0.5).
CHAMBER_TOMB = (30, CHAMBER_DAIS + 1, CHAMBER_ZC)

# Set-piece sockets, left empty: Track 6's blocks stand in them when a theme track fills them through an optional
# chamber_sockets(room, c, sockets) hook in its themes/<saint>.py. Each is (kind, (x, y, z), facing): the cell the
# piece stands in and the way it faces into the room.
#   dais_flank    on plinths on the top step either side of the tomb's foot: the Saint's signature pair, the first
#                 thing beside the tomb seen from the door.
#   apse_niche    in the apse's back wall: the tall middle niche behind the tomb's head holds the Saint's relic or
#                 effigy; the two low niches beside it hold votive pieces.
#   aisle_end     the arched recess closing each aisle at the dais end: one large piece per side, seen down the aisle.
#   aisle_alcove  an arched recess in each aisle bay's outer wall: the congregation (Track 6's devotees), facing in.
CHAMBER_SOCKETS = (
    [('dais_flank', (28, CHAMBER_DAIS + 2, CHAMBER_ZC + side * 6), 'west') for side in (-1, 1)]
    + [('apse_niche', (CHAMBER_BACK, CHAMBER_DAIS + 1, CHAMBER_ZC + dz), 'west') for dz in (0, -3, 3)]
    + [('aisle_end', (CHAMBER_STEPS[-1], 1, CHAMBER_ZC + side * CHAMBER_AISLE), 'west') for side in (-1, 1)]
    + [('aisle_alcove', (x, 1, CHAMBER_ZC - CHAMBER_AISLE - 2), 'south') for x in CHAMBER_BAYS]
    + [('aisle_alcove', (x, 1, CHAMBER_ZC + CHAMBER_AISLE + 2), 'north') for x in CHAMBER_BAYS])


def chamber(saint):
    """The chamber, the lowest and grandest room: the place the story started, built to hold the Saint's fight. 37
    long, 27 wide, 17 tall. The ring door opens onto a processional that steps down six to the hall floor, so the first
    view is the tomb on its dais framed by the two pillar ranks. The hall's centre is a clear, flat 15 x 15 under a
    stepped barrel vault; arcades open onto lower vaulted aisles; three steps rise to the apse, whose own vault springs
    behind a triumphal arch and draws in round the tomb's head. The set-piece sockets (CHAMBER_SOCKETS) are built into
    the architecture and stay empty until a theme track fills them."""
    c = crypt(saint)
    rng = random.Random('chamber/' + saint)
    zc = CHAMBER_ZC
    room = Room(CHAMBER_L, CHAMBER_H, CHAMBER_W)
    mass(room, c, rng, 0, CHAMBER_L - 1, 0, CHAMBER_H - 1, 0, CHAMBER_W - 1)
    chamber_processional(room, c, rng)
    chamber_hall(room, c, rng)
    chamber_apse(room, c, rng)
    room.set(*CHAMBER_TOMB, 'hemomancy:saint_sarcophagus', {'facing': 'west', 'waterlogged': 'false'}, Compound({
        'id': String('hemomancy:saint_sarcophagus'), 'SaintType': String(saint.upper()),
        'CorpusState': String('DORMANT'), 'ExtractionAttempts': Int(0), 'CooldownTicks': Int(0),
        'FillersPlaced': Byte(0)}))
    fill_sockets = getattr(theme(saint), 'chamber_sockets', None)
    if fill_sockets:
        fill_sockets(room, c, CHAMBER_SOCKETS)
    chamber_entry(room, c)
    room.jigsaw(0, CHAMBER_ENTRY, zc, 'west_up', conn('chamber_in'), conn('nave_out'), EMPTY, c['track'])
    return room


def chamber_entry(room, c):
    """The west wall behind the ring door's column: the floor track continued under the door's back face, and the open
    door's passage (3 wide, 4 tall; the frame keeps its outer columns and top row) through to the processional, with
    trim jambs, a lintel course, haunch stairs and the keystone a course over it, filling the processional's vault."""
    zc, floor = CHAMBER_ZC, CHAMBER_ENTRY
    track_course(room, c, 0, floor, zc)
    for y in range(floor + 1, floor + RING_DOOR_H):
        for d in (-1, 0, 1):
            room.set(0, y, zc + d, AIR)
        room.set(0, y, zc - 2, c['trim'])
        room.set(0, y, zc + 2, c['trim'])
    for side in (-1, 1):
        room.set(0, floor + 1, zc + side * 3, c['base'])
    for d in range(-RING_DOOR_HALF, RING_DOOR_HALF + 1):
        room.set(0, floor + RING_DOOR_H, zc + d, c['trim'])
    room.set(0, floor + RING_DOOR_H + 1, zc - 1, c['trim_stairs'], stairs('north', 'top'))
    room.set(0, floor + RING_DOOR_H + 1, zc, c['accent'])
    room.set(0, floor + RING_DOOR_H + 1, zc + 1, c['trim_stairs'], stairs('south', 'top'))


def chamber_course(x):
    """The processional's walking course at x: the landing's floor (x 1-2), then a stair down per block to the hall."""
    return CHAMBER_ENTRY if x <= 2 else CHAMBER_ENTRY + 3 - x


def chamber_processional(room, c, rng):
    """Seven wide through the thick west wall, a stepped vault over it, its last rib framing the portal to the hall."""
    zc, half = CHAMBER_ZC, CHAMBER_WAY
    for x in range(1, CHAMBER_PILLARS[0]):
        course = chamber_course(x)
        for z in range(zc - half, zc + half + 1):
            for y in range(1, course):
                room.set(x, y, z, c['base'])
            if x <= 2:
                room.set(x, course, z, kit.paved(c, rng, abs(z - zc), half))
            else:
                room.set(x, course, z, c['trim_stairs'], stairs('west'))
            for y in range(course + 1, 11):
                room.set(x, y, z, AIR)
        for plane in (zc - half - 1, zc + half + 1):
            room.set(x, course + 1, plane, c['base'])
            room.set(x, 10, plane, c['trim'])
        crown = kit.vault_x(room, c, x, zc, half, 11)
        if x in (3, 6):
            room.set(x, crown, zc, c['light'])
    for x in (1, 5, 8):
        for plane in (zc - half - 1, zc + half + 1):
            for y in range(chamber_course(x) + 2, 10):
                room.set(x, y, plane, c['trim'])
            room.set(x, 10, plane, c['accent'])
        kit.rib_x(room, c, x, zc, half, 11)


def chamber_hall(room, c, rng):
    """The hall: the boss floor between the pillar ranks, arcades, a clerestory course, the high vault, and an aisle
    behind each rank with its own vault, an alcove per bay and an arched recess at its dais end."""
    zc, rank = CHAMBER_ZC, CHAMBER_RANK
    x0, x1 = CHAMBER_PILLARS[0], CHAMBER_PILLARS[-1]
    centre = (CHAMBER_CLEAR[0] + CHAMBER_CLEAR[1]) // 2
    inlay = set(kit.ring(centre, zc, 5))
    for x in range(x0, x1 + 1):
        for z in range(zc - rank + 1, zc + rank):
            room.set(x, 0, z, c['inlay'] if (x, z) in inlay else kit.paved(c, rng, abs(z - zc), rank - 1))
            for y in range(1, 9):
                room.set(x, y, z, AIR)
        kit.vault_x(room, c, x, zc, rank - 1, 9)
        for side in (-1, 1):
            ac = zc + side * CHAMBER_AISLE
            outer = ac + side * 2
            for z in range(ac - 1, ac + 2):
                room.set(x, 0, z, kit.paved(c, rng, abs(z - ac), 1))
                for y in range(1, 6):
                    room.set(x, y, z, AIR)
            kit.vault_x(room, c, x, ac, 1, 6)
            room.set(x, 1, outer, c['base'])
            room.set(x, 5, outer, c['trim'])
            plane = zc + side * rank
            if x in CHAMBER_PILLARS:
                room.set(x, 1, plane, c['base'])
                for y in (2, 3, 4, 6, 7):
                    room.set(x, y, plane, c['trim'])
                room.set(x, 5, plane, c['accent'])
                room.set(x, 8, plane, c['accent'])
                room.set(x, 1, outer, c['base'])
                for y in range(2, 5):
                    room.set(x, y, outer, c['trim'])
                room.set(x, 5, outer, c['accent'])
                kit.rib_x(room, c, x, ac, 1, 6)
            else:
                # An arcade bay: open to y 4, an arch of haunch stairs and a keystone, the clerestory course above.
                bay = (x - x0) % 4
                for y in range(1, 5):
                    room.set(x, y, plane, AIR)
                if bay == 2:
                    room.set(x, 5, plane, AIR)
                    room.set(x, 6, plane, c['accent'])
                else:
                    room.set(x, 5, plane, c['trim_stairs'], stairs('west' if bay == 1 else 'east', 'top'))
                    room.set(x, 6, plane, field(rng, c))
                room.set(x, 7, plane, field(rng, c))
                room.set(x, 8, plane, c['trim'])
        if x in CHAMBER_PILLARS:
            kit.rib_x(room, c, x, zc, rank - 1, 9)
            room.set(x, CHAMBER_H - 1, zc, c['light'])
    for side in (-1, 1):
        ac = zc + side * CHAMBER_AISLE
        outer = ac + side * 2
        for x in CHAMBER_BAYS:
            # The bay alcove: three wide, three tall, one deep, an arched head; its socket is the middle cell.
            for dx in (-1, 0, 1):
                room.set(x + dx, 0, outer, c['inlay'])
                for y in range(1, 4):
                    room.set(x + dx, y, outer, AIR)
            room.set(x - 1, 4, outer, c['trim_stairs'], stairs('west', 'top'))
            room.set(x, 4, outer, AIR)
            room.set(x + 1, 4, outer, c['trim_stairs'], stairs('east', 'top'))
            room.set(x, 5, outer, c['accent'])
            room.set(x, 7, ac, c['light'])
        # The aisle's west end: a plain wall with a lamp; its dais end: an arched recess.
        for z in range(ac - 1, ac + 2):
            room.set(x0 - 1, 1, z, c['base'])
            room.set(x0 - 1, 5, z, c['trim'])
        kit.wall_light(room, c, x0 - 1, 3, ac, 'east')
        arch(room, c, x1 + 1, 0, ac)
        for z in range(ac - 1, ac + 2):
            room.set(x1 + 1, 0, z, c['inlay'])
        # The west wall either side of the portal.
        for d in range(CHAMBER_WAY + 2, rank):
            z = zc + side * d
            room.set(x0 - 1, 1, z, c['base'])
            room.set(x0 - 1, 8, z, c['trim'])
        kit.wall_light(room, c, x0 - 1, 5, zc + side * (rank - 2), 'east')
    # The dais' steps, straight across the hall, one stair riser each.
    for step, x in enumerate(CHAMBER_STEPS):
        for z in range(zc - rank + 1, zc + rank):
            for y in range(1, step + 1):
                room.set(x, y, z, c['base'])
            room.set(x, step + 1, z, c['trim_stairs'], stairs('east'))


def chamber_apse(room, c, rng):
    """The apse: a triumphal arch a course below the nave vault, the tomb bay, and a conch drawing in round the head;
    its walls and back wall dressed with a base course, a cornice, pilasters and ribs, and the sockets built in."""
    zc, top = CHAMBER_ZC, CHAMBER_DAIS
    for x, half in CHAMBER_APSE.items():
        arch_bay = x <= CHAMBER_STEPS[-1] + 1
        spring = 8 if arch_bay else CHAMBER_H - 1 - half
        for z in range(zc - half, zc + half + 1):
            if x > CHAMBER_STEPS[-1]:
                for y in range(1, top):
                    room.set(x, y, z, c['base'])
                room.set(x, top, z, kit.paved(c, rng, abs(z - zc), half))
            for y in range(top + 1, spring):
                room.set(x, y, z, AIR)
        kit.vault_x(room, c, x, zc, half, spring)
        for side in (-1, 1):
            plane = zc + side * (half + 1)
            if arch_bay:
                # The triumphal arch's responds.
                for y in range(top + 1, 7):
                    room.set(x, y, plane, c['trim'])
                room.set(x, 7, plane, c['accent'])
            else:
                room.set(x, top + 1, plane, c['base'])
                room.set(x, spring - 1, plane, c['trim'])
    kit.rib_x(room, c, CHAMBER_STEPS[-1], zc, 7, 8)
    for x in (29, 32):
        for side in (-1, 1):
            plane = zc + side * (CHAMBER_APSE[x] + 1)
            for y in range(top + 2, 8):
                room.set(x, y, plane, c['trim'])
            room.set(x, 8, plane, c['accent'])
        kit.rib_x(room, c, x, zc, CHAMBER_APSE[x], CHAMBER_H - 1 - CHAMBER_APSE[x])
    for x in (30, 31):
        room.set(x, CHAMBER_H - 1, zc, c['light'])
    # The back wall: a base course and cornice across the conch's mouth, the relic niche and two votive niches.
    back, inner = CHAMBER_BACK, CHAMBER_APSE[CHAMBER_BACK - 1]
    for z in range(zc - inner, zc + inner + 1):
        room.set(back, top + 1, z, c['base'])
        room.set(back, 10, z, c['trim'])
    for y in range(top + 1, top + 5):
        room.set(back, y, zc, AIR)
        room.set(back, y, zc - 1, c['trim'])
        room.set(back, y, zc + 1, c['trim'])
    room.set(back, top + 5, zc - 1, c['trim_stairs'], stairs('north', 'top'))
    room.set(back, top + 5, zc, c['accent'])
    room.set(back, top + 5, zc + 1, c['trim_stairs'], stairs('south', 'top'))
    room.set(back, top, zc, c['inlay'])
    for dz in (-3, 3):
        for y in (top + 1, top + 2):
            room.set(back, y, zc + dz, AIR)
        room.set(back, top, zc + dz, c['inlay'])
        room.set(back, top + 3, zc + dz, c['accent'])
        kit.wall_light(room, c, back, top + 5, zc + dz, 'west')
    # The dais flank plinths.
    for kind, (x, y, z), _ in CHAMBER_SOCKETS:
        if kind == 'dais_flank':
            room.set(x, y - 1, z, c['base'])

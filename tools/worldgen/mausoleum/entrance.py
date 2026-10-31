"""The entrance (Track 7): the pavilion over the ground (top), the stair down from it (stairs) and the antechamber at its
foot, built through the kit like the rest of the complex. They replace the hand-built top/stairs/antechamber2way NBTs,
which stay in structure/mausoleum/ as reference only.

The connectors keep the old chain: top's down connector at (7, 0, 8) meets the stair's up connector, the stair's east
connector meets the antechamber's west one, and the antechamber's east connector meets the descent (its floor is the
descent's entry floor). The pavilion's first row is a footing that replaces the ground's top block (start_height 0);
its step ring, portico stair and floor stand one row up, on the ground; the west gap (x 0-2, z 7-9) is the way in
(MausoleumComplexResourceTest.theEntranceStandsOnTheGround).

Walking in: up the portico stair, through the arched door, along the runner to the landing steps at x=6 and 7, then a
flight of stairs dropping one per block east (x 7-16 in the pavilion's frame) under a raking vault, into the
antechamber.
"""
import random

from nbtlib import String

from . import kit
from .kit import AIR, Room, stairs, slab, post, rail

EMPTY = 'minecraft:empty'
MANDATORY = 1

# The pavilion: 17 x 17, its corners chamfered (left to the ground). Rings count in from the edge: 0 the step, 1 the
# stylobate, 2 the outer skin, 3 the inner skin, 4 and in the hall (x 4-12). The way in is the west gap at z 7-9.
TOP = 17
TOP_H = 15
CENTRE = 8
SPRING = 7           # the hall's vault springs here; the walls' cornices are on the course under it
PILASTERS = (2, 6, 10, 14)
LANCETS = (4, 8, 12)
WELL = range(8, 12)  # the stairwell's open cells along x at z 7-9; the landing step is x=7
LANDING = 7
# The hall's floor row once footing() has lifted the pavilion onto its footing course (row 0, the ground's top block).
FLOOR = 1

# The stair: 10 long, 11 tall, 7 wide; a flight 3 wide (z 2-4) dropping from the landing to the antechamber.
STAIR_L, STAIR_H, STAIR_W = 10, 11, 7
STAIR_ZC = 3

# The antechamber: 11 x 11 x 11, two-skinned walls round a 7 x 7 room (x, z 2-8) under a cross vault.
ANTE = 11
ANTE_H = 11
ANTE_ZC = 5


def ring_of(x, z):
    return min(x, z, TOP - 1 - x, TOP - 1 - z)


def footprint(x, z):
    """The chamfer: corner cells whose two edge distances sum under 2 are left to the ground."""
    return min(x, TOP - 1 - x) + min(z, TOP - 1 - z) >= 2


def roof_y(r):
    return SPRING - 1 + r


def stair_state(c, facing='west', half='bottom'):
    props = stairs(facing, half)
    return '%s[%s]' % (c['trim_stairs'], ','.join('%s=%s' % kv for kv in sorted(props.items())))


def top(saint, c, conn, pool_name):
    """The pavilion: a stepped stylobate; a two-skinned cella whose outer skin is banded (skirt, base, field, cornice)
    and bayed by pilasters with a lamp-lit lancet in each bay; a portico of two columns and a lintel before the west
    door; inside, a hall nine square under a cross vault with the stairwell's balustrade round the flight down; and a
    stepped pyramid roof with a lamp at its apex."""
    rng = random.Random('top/' + saint)
    room = Room(TOP, TOP_H, TOP)
    for x in range(TOP):
        for z in range(TOP):
            if not footprint(x, z):
                continue
            r = ring_of(x, z)
            if r == 0:
                inward = 'east' if x == 0 else 'west' if x == TOP - 1 else 'south' if z == 0 else 'north'
                room.set(x, 0, z, c['trim_stairs'], kit.stairs(inward))
            elif r == 1:
                room.set(x, 0, z, c['base'])
            elif r <= 3:
                room.set(x, 0, z, c['base'])
                for y in range(1, SPRING):
                    room.set(x, y, z, kit.field(rng, c))
                for y in range(SPRING, roof_y(r)):
                    room.set(x, y, z, c['vault'])
            else:
                room.set(x, 0, z, c['floor'])
                for y in range(SPRING, roof_y(r)):
                    room.set(x, y, z, c['vault'])
    outer_skin(room, c, rng)
    hall_interior(room, c, rng)
    roof(room, c)
    portico(room, c)
    stairwell(room, c, rng)
    room = footing(room, c)
    room.jigsaw(LANDING, 0, CENTRE, 'down_east', conn('top_out'), conn('stairs'), pool_name(saint, 'stairs'),
                stair_state(c), joint='aligned')
    return room


def footing(room, c):
    """Lifts the pavilion one course onto a footing of base under its whole footprint. The footing is the template's
    row 0, which replaces the ground's top block (and beard_thin levels the land around to it), so the step ring and
    the portico stair stand on the ground instead of sinking half a block into it. The flight below keeps its height:
    the old landing step stays in the footing at x=LANDING, a new step at floor level comes in west of it, and the
    footing is cut open over the flight's first steps."""
    sx, sy, sz = room.size
    lifted = Room(sx, sy + 1, sz)
    for (x, y, z), (name, props, nbt) in room.blocks.items():
        lifted.set(x, y + 1, z, name, dict(props), nbt)
    for x in range(TOP):
        for z in range(TOP):
            if footprint(x, z):
                lifted.set(x, 0, z, c['base'])
    for z in range(CENTRE - 1, CENTRE + 2):
        lifted.set(LANDING - 1, 1, z, c['trim_stairs'], stairs('west'))
        lifted.set(LANDING, 1, z, AIR)
        lifted.set(LANDING, 0, z, c['trim_stairs'], stairs('west'))
        for x in WELL:
            lifted.set(x, 0, z, AIR)
    return lifted


def outer_skin(room, c, rng):
    """The outer face (ring 2): a skirt of stairs at its foot on the stylobate, a base course, pilasters with accent
    capitals, a lancet in each bay (trim sill, a sconce set into it, accent head), the cornice course and an overhanging
    cornice of upside-down stairs over the stylobate."""
    kit.ring_stairs(room, c['trim_stairs'], 1, TOP - 2, 1, TOP - 2, 1, outward=False, half='bottom')
    kit.ring_stairs(room, c['trim_stairs'], 1, TOP - 2, 1, TOP - 2, SPRING - 1, outward=False, half='top')
    lo, hi = 2, TOP - 3
    for along in range(lo, hi + 1):
        for (x, z, facing) in ((along, lo, 'north'), (along, hi, 'south'), (lo, along, 'west'), (hi, along, 'east')):
            room.set(x, 1, z, c['base'])
            room.set(x, SPRING - 1, z, c['trim'])
            if along in PILASTERS:
                for y in range(2, SPRING - 2):
                    room.set(x, y, z, c['trim'])
                room.set(x, SPRING - 2, z, c['accent'])
            elif along in LANCETS:
                room.set(x, 2, z, c['trim'])
                kit.sconce(room, x, 3, z, facing)
                room.set(x, 4, z, c['trim_wall'], kit.post())
                room.set(x, 5, z, c['accent'])


def hall_interior(room, c, rng):
    """The hall (rings 4+, x and z 4-12): the inner skin dressed with a base course, a dado, the field, a cornice;
    pilasters at 4, 8 and 12 along each wall with sconces set into the wall at the bay centres; an inlay border; then a
    cross vault from SPRING to a lamp at its crown."""
    lo, hi = 4, TOP - 5
    for x in range(lo, hi + 1):
        for z in range(lo, hi + 1):
            for y in range(1, SPRING):
                room.set(x, y, z, AIR)
            room.set(x, 0, z, c['inlay'] if x in (lo, hi) or z in (lo, hi) else
                     c['floor_alt'] if rng.random() < 0.12 else c['floor'])
    plane_lo, plane_hi = lo - 1, hi + 1
    for along in range(lo, hi + 1):
        for (x, z, facing) in ((along, plane_lo, 'south'), (along, plane_hi, 'north'), (plane_lo, along, 'east'),
                               (plane_hi, along, 'west')):
            room.set(x, 1, z, c['base'])
            room.set(x, 2, z, c['trim'])
            room.set(x, SPRING - 1, z, c['trim'])
            if along in (4, 8, 12):
                for y in range(3, SPRING - 2):
                    room.set(x, y, z, c['trim'])
                room.set(x, SPRING - 2, z, c['accent'])
            elif along in (6, 10):
                kit.sconce(room, x, 3, z, facing)
    crown = kit.vault_ceiling(room, c, lo, hi, lo, hi, SPRING, kind='cross')
    room.set(CENTRE, crown, CENTRE, c['light'])


def roof(room, c):
    """A stepped pyramid from ring 2 over the cornice to a lamp at the apex, every course a ring of stairs climbing
    inward with its corners turned."""
    for r in range(2, CENTRE):
        kit.ring_stairs(room, c['trim_stairs'], r, TOP - 1 - r, r, TOP - 1 - r, roof_y(r), outward=False,
                        half='bottom')
    room.set(CENTRE, roof_y(CENTRE), CENTRE, c['light'])
    for (x, z) in ((CENTRE - 1, CENTRE), (CENTRE + 1, CENTRE), (CENTRE, CENTRE - 1), (CENTRE, CENTRE + 1)):
        room.set(x, roof_y(CENTRE - 1), z, c['accent'])


def portico(room, c):
    """The west door: an arch through both skins, the runner to the landing, and before it two columns on the
    stylobate carrying a lintel under the cornice, with a slab roof over it."""
    for x in (2, 3):
        kit.arch(room, c, x, 0, CENTRE)
    for x in range(0, LANDING):
        room.set(x, 0, CENTRE, c['runner'])
    for z in (CENTRE - 1, CENTRE + 1):
        for x in (2, 3):
            room.set(x, 0, z, c['floor'])
    for z in range(CENTRE - 1, CENTRE + 2):
        room.set(1, 1, z, AIR)
        room.set(0, 0, z, c['trim_stairs'], kit.stairs('east'))
        room.set(1, 0, z, c['inlay'])
    for z in (CENTRE - 2, CENTRE + 2):
        room.set(1, 1, z, c['base'])
        for y in range(2, SPRING - 2):
            room.set(1, y, z, c['trim'])
        room.set(1, SPRING - 2, z, c['accent'])
    for z in range(CENTRE - 2, CENTRE + 3):
        room.set(1, SPRING - 1, z, c['trim'])
        room.set(0, SPRING - 1, z, c['trim_stairs'], kit.stairs('east', 'top'))
        room.set(1, SPRING, z, c['trim_slab'], slab('bottom'))
        room.set(0, SPRING, z, c['trim_slab'], slab('bottom'))
    room.set(1, SPRING - 1, CENTRE, c['accent'])


def stairwell(room, c, rng):
    """The landing step and the open well over the first steps of the flight, railed round with a newel at each corner
    of the landing."""
    for z in range(CENTRE - 1, CENTRE + 2):
        room.set(LANDING, 0, z, c['trim_stairs'], kit.stairs('west'))
        for x in WELL:
            room.set(x, 0, z, AIR)
    for x in range(LANDING, WELL[-1] + 2):
        for z, toward in ((CENTRE - 2, None), (CENTRE + 2, None)):
            end = x in (LANDING, WELL[-1] + 1)
            room.set(x, 1, z, c['trim_wall'], kit.post() if end else rail('x'))
    for z in range(CENTRE - 1, CENTRE + 2):
        room.set(WELL[-1] + 1, 1, z, c['trim_wall'], rail('z'))
    for z in (CENTRE - 2, CENTRE + 2):
        room.set(LANDING, 1, z, c['accent'])
        room.set(LANDING, 2, z, c['trim_wall'], kit.post())
        room.set(WELL[-1] + 1, 0, z, c['inlay'])


def stair(saint, c, conn, pool_name):
    """The flight down from the landing: three wide, dropping one a block under a raking vault; its walls carry a
    string course along the steps, pilasters every three with accent capitals, sconces set into the wall beside every
    other pilaster, and a raking cornice. Its first steps lie open to the pavilion's stairwell."""
    rng = random.Random('stairs/' + saint)
    room = Room(STAIR_L, STAIR_H, STAIR_W)
    zc = STAIR_ZC
    for x in range(STAIR_L):
        for z in range(STAIR_W):
            for y in range(STAIR_H):
                room.set(x, y, z, kit.field(rng, c) if y else c['base'])
    for x in range(1, STAIR_L):
        stand = 12 - x
        for z in range(zc - 1, zc + 2):
            for y in range(1, stand - 1):
                room.set(x, y, z, c['base'])
            room.set(x, stand - 1, z, c['trim_stairs'], stairs('west'))
            for y in range(stand, STAIR_H):
                room.set(x, y, z, AIR)
        cornice = stand + 3
        for plane in (zc - 2, zc + 2):
            room.set(x, stand - 1, plane, c['base'])
            if stand < STAIR_H:
                room.set(x, stand, plane, c['base'])
            if cornice < STAIR_H:
                room.set(x, cornice, plane, c['trim'])
        if stand + 4 < STAIR_H:
            room.set(x, stand + 4, zc - 1, c['vault_stairs'], stairs('north', 'top'))
            room.set(x, stand + 4, zc + 1, c['vault_stairs'], stairs('south', 'top'))
            for y in range(stand + 5, STAIR_H):
                for z in range(zc - 1, zc + 2):
                    room.set(x, y, z, c['vault'])
            if stand + 5 < STAIR_H:
                room.set(x, stand + 5, zc, c['accent'] if x % 2 else c['vault'])
        elif stand + 3 < STAIR_H:
            # Where the vault would rise into the pavilion's floor, the ceiling is the pavilion's: band the open wall.
            for plane in (zc - 2, zc + 2):
                for y in range(stand + 4, STAIR_H):
                    room.set(x, y, plane, c['base'] if (y - stand) % 3 == 1 else kit.field(rng, c))
    for x in (2, 5, 8):
        stand = 12 - x
        for plane in (zc - 2, zc + 2):
            for y in range(stand + 1, min(stand + 3, STAIR_H)):
                room.set(x, y, plane, c['trim'])
            if stand + 3 < STAIR_H:
                room.set(x, stand + 3, plane, c['accent'])
    # Sconces set into the wall beside the lower two pilasters, at head height over the step.
    for x in (6, 9):
        for plane, facing in ((zc - 2, 'south'), (zc + 2, 'north')):
            kit.sconce(room, x, 12 - x + 2, plane, facing)
    room.jigsaw(0, STAIR_H - 1, zc, 'up_east', conn('stairs'), conn('top_out'), EMPTY, c['base'], joint='aligned')
    room.jigsaw(STAIR_L - 1, 0, zc, 'east_up', conn('stairs_out'), conn('antechamber'), pool_name(saint, 'antechamber'),
                c['base'])
    return room


def antechamber(saint, c, conn, pool_name):
    """The antechamber: a room seven square inside two-skinned walls. Each wall has a base course, a dado, a cornice
    and pilasters at its ends; the west and east walls carry the arched doorways, the north and south a loculus of
    urns between two sconces. Floor: an inlay border, the runner across, an inlay ring round an accent boss. Over it a
    cross vault from y 6 to a lamp at its crown."""
    rng = random.Random('antechamber/' + saint)
    room = Room(ANTE, ANTE_H, ANTE)
    zc = ANTE_ZC
    kit.mass(room, c, rng, 0, ANTE - 1, 0, ANTE_H - 1, 0, ANTE - 1)
    for x in range(ANTE):
        for z in range(ANTE):
            room.set(x, 0, z, c['base'])
            room.set(x, 1, z, c['floor'])
    lo, hi = 2, 8
    for x in range(lo, hi + 1):
        for z in range(lo, hi + 1):
            for y in range(2, 6):
                room.set(x, y, z, AIR)
            room.set(x, 1, z, c['inlay'] if x in (lo, hi) or z in (lo, hi) else
                     c['floor_alt'] if rng.random() < 0.12 else c['floor'])
    for (x, z) in kit.ring(zc, zc, 2):
        room.set(x, 1, z, c['inlay'])
    for x in range(ANTE):
        room.set(x, 1, zc, c['runner'])
    room.set(zc, 1, zc, c['accent'])
    for along in range(lo, hi + 1):
        for (x, z) in ((along, 1), (along, 9), (1, along), (9, along)):
            room.set(x, 2, z, c['base'])
            room.set(x, 3, z, c['trim'])
            room.set(x, 5, z, c['trim'])
            if along in (lo, hi):
                room.set(x, 4, z, c['accent'])
    for plane, back, facing in ((1, 0, 'south'), (9, 10, 'north')):
        kit.loculus(room, c, (4, 5, 6), 4, plane, back, 'urn', rng, facing)
        for x in (3, 7):
            kit.sconce(room, x, 4, plane, facing)
    for x, facing in ((1, 'west'), (9, 'east')):
        kit.arch(room, c, x, 1, zc)
    for x in (0, 10):
        for z in range(zc - 1, zc + 2):
            for y in range(2, 5):
                room.set(x, y, z, AIR)
            room.set(x, 1, z, c['runner'] if z == zc else c['floor'])
        room.set(x, 5, zc, AIR)
    crown = kit.vault_ceiling(room, c, lo, hi, lo, hi, 6, kind='cross')
    room.set(zc, crown, zc, c['light'])
    room.jigsaw(0, 0, zc, 'west_up', conn('antechamber'), conn('stairs_out'), EMPTY, c['base'])
    room.jigsaw(ANTE - 1, 0, zc, 'east_up', conn('antechamber_out'), conn('descent_in'), pool_name(saint, 'descent'),
                c['base'], MANDATORY)
    return room

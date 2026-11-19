"""The Crimson Troupe pavilion (chunk 15 T8): the big top, its backyard and its midway, drawn on a 65 x 23 x 65 core
and moved into one 81 x 23 x 65 template with a side ring either side (side_rings.py, 2026-11-16).

The big top is an octagon of laced canvas on a wainscot, 43 by 39, under a stepped canvas roof that rises to the two
king poles standing in the ring curb. The Impaled Procession turns on its plinth in the middle of the sawdust ring;
six crimson footlights stand behind the curb. Two real galleries run along the long walls for every player: the
lower (north) gallery, the loft, at deck height 5 over the north seating, and the upper (south) gallery at deck height
7 over the south seating, with the Ringmaster's box built out from it over the ring. Each gallery has a flight of
stairs at both ends. A survival player walks up the south-west or south-east flight, along the gallery and in at the
box's back corners, and stands beside him. Rigging towers stand at the east and west ends, their trapezes reaching
over a net toward the ring. Seating banks face the ring from north and south.

Outside: the midway to the north (the ticket booth, the banner line, the pennants over the path to the door and the
Threadkeeper's registry stall), the backyard to the south and east (three painted wagons, the cookhouse and the
dressing tent where the Understudy keeps the quarters ledger), and the Strongman's and the Beast Tamer's side rings to
the west and east (side_rings.py). Guy lines run out from the long walls.

The template places all ten actors and the three records the pavilion keeps (the carousel maker's plate, the loft
rigging log, the quarters ledger). Light is the template's own: hematic lanterns hung from the roof and under the
decks, and the footlights. checks.py proves every standable cell under the roof has block light.
"""
import math

from nbtlib import Compound, String

from . import kit, side_rings
from .kit import (AIR, BONE, CANVAS, CHAIN, LACED, LACQUER, LOG, PLANKS, QUARTER_POLE, SAWDUST, STONE,
                  WAINSCOT, Site)

NAME = 'crimson_troupe_pavilion'
# The big top and its midway and backyard are drawn in the 65-block core (every coordinate below is a core one); the
# template is that core moved MARGIN cells east, with a performance yard in each new margin (2026-11-16).
CORE = (65, 23, 65)
MARGIN = 8
SIZE = (CORE[0] + 2 * MARGIN, CORE[1], CORE[2])
CX, CZ = 32, 32
# The big top's footprint: |dx| <= HALF_X, |dz| <= HALF_Z, with the corners cut at |dx| + |dz| <= CHAMFER.
HALF_X, HALF_Z, CHAMFER = 21, 19, 34
WALL_TOP = 11
EAVE = WALL_TOP + 1
RING_R = 7
LANTERN = 'hemomancy:hematic_lantern'
EMITTER = 'hemomancy:abocipher_emitter'
NORTH_DECK, SOUTH_DECK = 5, 7
BOX = (32, SOUTH_DECK, 44)        # the Ringmaster's box: centre x, deck y, front row z
PERCH = (32, SOUTH_DECK + 1, 45)  # where he stands, behind the box's front rail
# The finale's rafter perches (2026-11-18), in template cells (core x + MARGIN), mirrored by FinaleStage in Java:
# the two rigging-tower platforms and the four quarter poles' collars he blinks between, and four places on the ring
# curb he drops to after a volley, within a melee reach of the floor. Points in the air, not blocks.
RAFTER_PERCHES = [(24, 10, 32), (56, 10, 32), (32, 11, 24), (48, 11, 24), (32, 11, 40), (48, 11, 40)]
LOW_PERCHES = [(33, 2, 32), (47, 2, 32), (40, 2, 25), (40, 2, 39)]

# The actors, by entity id, on their marks (feet cells).
ACTORS = (
    ('impaled_procession', 32, 1, 32),
    ('circus_ringmaster',) + PERCH,
    ('circus_fire_eater', 28, 1, 28), ('circus_acrobat', 36, 1, 28),
    ('circus_stilt_walker', 28, 1, 36), ('circus_knife_thrower', 36, 1, 36),
    ('circus_threadkeeper', 8, 1, 7), ('circus_understudy', 56, 1, 53),
)
# The records the pavilion keeps, by block, cell and facing.
RECORDS = (('impaled_procession_record', 28, 1, 24, 'south'), ('circus_loft_record', 40, NORTH_DECK + 1, 14, 'south'),
           ('circus_quarters_record', 57, 1, 54, 'west'))


def inside(x, z):
    ax, az = abs(x - CX), abs(z - CZ)
    return ax <= HALF_X and az <= HALF_Z and ax + az <= CHAMFER


def wall(x, z):
    return inside(x, z) and any(not inside(x + dx, z + dz) for dx, dz in kit.STEP.values())


def depth(x, z):
    """How far in from the wall a column is, in cells."""
    ax, az = abs(x - CX), abs(z - CZ)
    return min(HALF_X - ax, HALF_Z - az, CHAMFER - ax - az)


def roof(x, z):
    """The roof's row over a column: one step up for every two cells in from the wall."""
    return EAVE + depth(x, z) // 2


def interior(x, y, z):
    return inside(x, z) and not wall(x, z) and 1 <= y < roof(x, z)


# --- the big top ------------------------------------------------------------------------------------------------------

DOORS = {  # wall cells left open, up to the given row
    **{(x, 13): 4 for x in range(30, 35)},             # the main door to the midway
    **{(53, z): 3 for z in (29, 30)},                  # the artists' door to the east yard
    **{(11, z): 3 for z in (29, 30)},                  # and its twin to the west yard
    **{(x, 51): 4 for x in range(31, 34)},             # the back door to the wagons, under the upper gallery
}


def ground(site):
    """The floor under the tent: blood-wood with a lacquer board every third, a stone footing under the wall."""
    for x in range(CORE[0]):
        for z in range(CORE[2]):
            if not inside(x, z):
                continue
            if wall(x, z):
                site.put(x, 0, z, STONE if (x + z) % 2 else BONE)
            else:
                site.put(x, 0, z, LACQUER if x % 3 == 2 else PLANKS)
                for y in range(1, roof(x, z)):
                    site.put(x, y, z, AIR)


def walls(site):
    for x in range(CORE[0]):
        for z in range(CORE[2]):
            if not wall(x, z):
                continue
            outside = [side for side, (dx, dz) in kit.STEP.items() if not inside(x + dx, z + dz)]
            if all(s in ('north', 'south') for s in outside):
                laced = x % 3 == 0
            elif all(s in ('east', 'west') for s in outside):
                laced = z % 3 == 0
            else:
                laced = x % 2 == 0
            door = DOORS.get((x, z), 0)
            for y in range(1, WALL_TOP + 1):
                if y <= door:
                    site.put(x, y, z, AIR)
                else:
                    site.put(x, y, z, WAINSCOT if y <= 2 else LACED if laced else CANVAS)


def canopy(site):
    """The stepped roof, its panels alternating striped and laced canvas round the king poles."""
    for x in range(CORE[0]):
        for z in range(CORE[2]):
            if not inside(x, z):
                continue
            panel = int((math.atan2(z - CZ, x - CX) + math.pi) / (2 * math.pi / 28)) % 2
            site.put(x, roof(x, z), z, LACED if panel else CANVAS)


def ring(site):
    f = site.frame(0, 0, 0)
    kit.ring(f, CX, 1, CZ, RING_R, footlights=(45, 90, 135, 225, 270, 315))
    # A lacquer track round the ring's middle, so the sawdust is never one flat field.
    for dx in range(-RING_R, RING_R + 1):
        for dz in range(-RING_R, RING_R + 1):
            if 3.5 <= math.hypot(dx, dz) < 4.5:
                site.put(CX + dx, 0, CZ + dz, LACQUER)
    # The Procession turns on its plinth, set flush in the ring floor on a venous-stone footing.
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            site.put(CX + dx, 0, CZ + dz, STONE)
    site.put(CX, 0, CZ, 'hemomancy:circus_plinth')
    # The two king poles stand in the curb and run up through the roof.
    for x in (CX - RING_R, CX + RING_R):
        kit.pole(f, x, 1, CZ, roof(x, CZ) + 1, collars=(6, 11, 15))
        # The canvas is laced tight round the pole where it runs through the roof: the stepped roof leaves the cells
        # beside it open to the sky, so they are closed, and the pole section there seals (TroupePoleBlock.SEALED).
        y = roof(x, CZ)
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                if (dx or dz) and site.get(x + dx, y, CZ + dz) in (None, AIR):
                    site.put(x + dx, y, CZ + dz, LACED)


def gallery(site, z0, z1, deck, edge, facing, gaps, posts):
    """A gallery deck at row `deck` over z0..z1 (inside the tent), a lacquer beam under its open edge, a rail along
    it but at the stair heads in `gaps`, and quarter poles under the edge at `posts`."""
    for x in range(CORE[0]):
        for z in range(z0, z1 + 1):
            if inside(x, z) and not wall(x, z):
                site.put(x, deck, z, LACQUER if x % 3 == 2 else PLANKS)
        if inside(x, edge) and not wall(x, edge):
            site.put(x, deck - 1, edge, LOG, {'axis': 'x'})
            if x not in gaps:
                site.put(x, deck + 1, edge, 'hemomancy:circus_gallery_rail', {'facing': facing})
    f = site.frame(0, 0, 0)
    for x in posts:
        kit.pole(f, x, 1, edge, deck - 2, QUARTER_POLE, collars=((deck - 2) // 2,))


def galleries(site):
    f = site.frame(0, 0, 0)
    # The lower gallery (the loft) over the north seating; its flights climb north at both ends.
    gallery(site, 14, 17, NORTH_DECK, 17, 'south', gaps=(16, 17, 47, 48), posts=(20, 24, 28, 36, 40, 44))
    for x in (16, 47):
        kit.gallery_stairs(f, x, 1, 22, NORTH_DECK, 'north')
    # The upper gallery over the south seating; its flights climb south, and the box is built out from it.
    gallery(site, 47, 50, SOUTH_DECK, 47, 'north', gaps=(19, 20, 44, 45) + tuple(range(30, 35)),
            posts=(17, 23, 27, 37, 41, 47))
    for x in (19, 44):
        kit.gallery_stairs(f, x, 1, 40, SOUTH_DECK, 'south')
    kit.ringmaster_box(f, *BOX)


def seating(site):
    # The north banks face the ring across the door aisle; the south bank sits under the box.
    for ox in (28, 43):
        kit.seating(site.frame(ox, 0, 21, 'south'), 0, 7, 1, 0, 3)
    kit.seating(site.frame(0, 0, 0), 23, 41, 1, 42, 3)


def rigging(site):
    # Two rigging towers at the ends, their trapezes reaching toward the ring over a net.
    # The nets are slung three blocks up and run all the way to the king poles.
    kit.rigging_tower(site.frame(16, 0, CZ, 'east'), 0, 1, 0, 8, net_y=4, net_reach=8)
    kit.rigging_tower(site.frame(48, 0, CZ, 'west'), 0, 1, 0, 8, net_y=4, net_reach=8)
    f = site.frame(0, 0, 0)
    # Quarter poles carry the canvas at the ring's corners; crossbar rigs of spare thread hang under the deck edges.
    for x, z in ((24, 24), (40, 24), (24, 40), (40, 40)):
        kit.pole(f, x, 1, z, roof(x, z) - 1, QUARTER_POLE, collars=(5, 10))
    for x in (22, 26, 38, 42):
        f.piece(x, NORTH_DECK - 2, 17, 'circus_crossbar_rig', 'south')
        f.piece(x, SOUTH_DECK - 2, 47, 'circus_crossbar_rig', 'north')


def dressing(site):
    """What hangs on the tent's own walls: the troupe's trophies, a mask board by the door, pelts and Spindles."""
    f = site.frame(0, 0, 0)
    for x, piece in ((22, 'circus_trophy'), (26, 'circus_pelt_drape'), (38, 'circus_spindle_rack'),
                     (42, 'circus_pelt_drape')):
        f.piece(x, NORTH_DECK + 2, 14, piece, 'south')
        f.piece(x, SOUTH_DECK + 2, 50, piece, 'north')
    for z, mask in ((24, 'laughing_mask'), (25, 'mourning_mask'), (26, 'blank_mask')):
        f.piece(12, 4, z, mask, 'east')
        f.piece(52, 4, z + 14, mask, 'west')


def lamp(site, x, y, z):
    """A hematic lantern hung at (x, y, z) on a chain up to the first block above it."""
    site.put(x, y, z, LANTERN, {'hanging': 'true'})
    up = y + 1
    while up < CORE[1] and site.get(x, up, z) in (None, AIR):
        site.put(x, up, z, CHAIN, {'axis': 'y'})
        up += 1


# Lanterns under the roof (clear of the carousel's sweep) and under the two decks and the box.
ROOF_LAMPS = ((21, 9, 21), (32, 9, 20), (43, 9, 21), (21, 9, 43), (43, 9, 43), (14, 6, 23), (50, 6, 23),
              (14, 6, 41), (50, 6, 41), (27, 11, 27), (37, 11, 27), (27, 11, 37), (37, 11, 37))
DECK_LAMPS = ((18, NORTH_DECK - 1, 15), (26, NORTH_DECK - 1, 15), (38, NORTH_DECK - 1, 15), (46, NORTH_DECK - 1, 15),
              (18, SOUTH_DECK - 1, 49), (26, SOUTH_DECK - 1, 49), (38, SOUTH_DECK - 1, 49),
              (46, SOUTH_DECK - 1, 49), (32, SOUTH_DECK - 1, 45))
GALLERY_LAMPS = ((24, NORTH_DECK + 4, 15), (40, NORTH_DECK + 4, 15), (24, SOUTH_DECK + 3, 49),
                 (40, SOUTH_DECK + 3, 49))
# Under each rigging tower's platform and over its trapeze arm.
TOWER_LAMPS = ((16, 8, CZ), (19, 14, CZ), (48, 8, CZ), (45, 14, CZ))
EMITTERS = ((17, NORTH_DECK + 1, 15), (47, SOUTH_DECK + 1, 49), (26, 1, 50))


def lights(site):
    for x, y, z in ROOF_LAMPS + DECK_LAMPS + GALLERY_LAMPS + TOWER_LAMPS:
        lamp(site, x, y, z)
    for x, y, z in EMITTERS:
        site.put(x, y, z, EMITTER, None, Compound({'id': String(EMITTER), 'Profile': String('harbinger_outpost')}))


# --- outside ----------------------------------------------------------------------------------------------------------

def path(site, x0, x1, z0, z1):
    """A trodden path of sawdust with a plank laid across it every third row."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            site.put(x, 0, z, PLANKS if z % 3 == 0 else SAWDUST)


def guy_lines(site):
    f = site.frame(0, 0, 0)
    for x in range(14, 51, 6):
        if not 28 <= x <= 36:
            f.piece(x, 1, 11, 'circus_guy_stake', 'south')
        f.piece(x, 1, 53, 'circus_guy_stake', 'north')
    # The flanks carry no stakes: the side yards stand there.
    # The valance round the eaves of the long walls, and over the main door.
    for x in range(17, 48):
        site.put(x, WALL_TOP, 12, 'hemomancy:circus_valance', {'facing': 'north'})
        site.put(x, WALL_TOP, 52, 'hemomancy:circus_valance', {'facing': 'south'})
    for x in range(30, 35):
        site.put(x, 5, 12, 'hemomancy:circus_valance', {'facing': 'north'})


def midway(site):
    f = site.frame(0, 0, 0)
    path(site, 30, 34, 0, 12)
    kit.ticket_booth(f, 36, 1, 4)
    # Pennant lines over the path, strung between two quarter poles.
    for x in (29, 35):
        kit.pole(f, x, 1, 9, 5, QUARTER_POLE, collars=(3,))
    for x in range(30, 35):
        site.put(x, 5, 9, 'hemomancy:circus_pennant_line', {'facing': 'north'})
    # The Threadkeeper's registry stall: a lean-to with the prompt table under it.
    kit.lean_to(f, 6, 1, 3, width=5)
    f.piece(8, 1, 4, 'circus_prompt_table', 'south')
    f.piece(10, 1, 4, 'circus_trunk', 'south')
    path(site, 7, 29, 7, 8)


def backyard(site):
    f = site.frame(0, 0, 0)
    kit.wagon(f, 26, 2, 56)
    kit.wagon(f, 36, 2, 56)
    kit.wagon(f, 44, 2, 56)
    kit.cookhouse(f, 20, 1, 55)
    kit.dressing_tent(f, 54, 1, 52)
    path(site, 31, 33, 52, 54)


def records(site):
    f = site.frame(0, 0, 0)
    for piece, x, y, z, facing in RECORDS:
        f.piece(x, y, z, piece, facing)


def build_core():
    """The big top, the midway and the backyard on the 65-block core, in core coordinates."""
    site = Site(CORE[0], CORE[1], CORE[2], ground=0, seed=15008)
    ground(site)
    walls(site)
    canopy(site)
    ring(site)
    galleries(site)
    seating(site)
    rigging(site)
    dressing(site)
    midway(site)
    backyard(site)
    guy_lines(site)
    records(site)
    lights(site)
    for actor in ACTORS:
        site.entity(*actor)
    return site


def build():
    """The template: the core moved MARGIN cells east, the Strongman's and the Beast Tamer's yards either side."""
    core = build_core()
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=15008)
    for (x, y, z), entry in core.blocks.items():
        site.blocks[(x + MARGIN, y, z)] = entry
    for kind, x, y, z in core.entities:
        site.entity(kind, x + MARGIN, y, z)
    side_rings.build(site, CX - HALF_X + MARGIN, CX + HALF_X + MARGIN, CZ)
    site.interior = lambda x, y, z: interior(x - MARGIN, y, z)
    site.finish()
    return site

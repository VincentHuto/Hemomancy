"""Hemorath (Track 8 owns this file): a powerful early Hemomancer, never a true Harbinger, whose gift of eternal
bloodlust was his downfall. His successors' fear of ending like him shaped the tradition that became the Hematic Order,
so his is the one complex the Order itself built and keeps. Walked backwards: the entrance is the Order's reliquary, the
chamber the pit where the first of them bled him dry to make him stop.

Palette: venous stone walls, hematic-iron trims, bone accents, deepslate-tile floors with venous inlays, dark
blackstone vaults. Motif: basins and channels. A blood gutter runs down the walk of every room the complex is walked
through, dry near the surface and wetter the deeper it goes (GUTTER_FILL); every hall has a basin; the Downfall is a
hall that descends past a basin at each landing; the chamber's floor is a dry round pit with channels running out to
four wall basins (`blood_basin`, the Saint's refill) and the processional is one long wet gutter from the ring door."""
import math
import random

from .. import kit
from ..kit import AIR, stairs

SAINT = 'hemorath'
BIOMES = ['minecraft:badlands', 'minecraft:wooded_badlands', 'minecraft:eroded_badlands', 'minecraft:savanna',
          'minecraft:savanna_plateau']
# Chapel kinds by slot: slot 0 is the wing level with the nave, 1 the upper wing, 2 the crypt wing.
CHAPELS = ['BEHOLDING', 'LITANY', 'DOWNFALL']

IRON = 'hemomancy:hematic_iron_plating'
VENOUS = 'hemomancy:polished_venous_stone'
# Roles for the kit. Every stairs named here takes the vanilla stairs properties.
CRYPT = kit.Theme({
    'base': VENOUS, 'wall': 'hemomancy:polished_venous_stone_bricks',
    'wall_alt': ['hemomancy:cracked_polished_venous_stone_bricks', 'hemomancy:venous_stone'],
    'trim': IRON, 'trim_stairs': IRON + '_stairs', 'trim_wall': IRON + '_wall', 'trim_slab': IRON + '_slab',
    'accent': 'hemomancy:chiseled_bone_bricks', 'floor': 'minecraft:deepslate_tiles',
    'floor_alt': 'minecraft:cracked_deepslate_tiles', 'inlay': VENOUS, 'runner': VENOUS,
    'seal': 'hemomancy:bone_bricks', 'vault': 'minecraft:polished_blackstone_bricks',
    'vault_stairs': 'minecraft:polished_blackstone_brick_stairs', 'button': 'minecraft:polished_blackstone_button',
    'light': 'minecraft:shroomlight', 'track': IRON, 'floor_slab': 'minecraft:deepslate_tile_slab',
    'vault_alt': ['minecraft:cracked_polished_blackstone_bricks']},
    variants={IRON: ['hemomancy:chiseled_hematic_iron_block'], VENOUS: ['hemomancy:venous_stone'],
              'hemomancy:bone_bricks': ['hemomancy:cracked_bone_bricks']})

GUTTER = 'hemomancy:blood_gutter'
BASIN = 'hemomancy:venous_basin'
WELL = 'hemomancy:blood_basin'
SIDES = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}
OPPOSITE = {'north': 'south', 'south': 'north', 'east': 'west', 'west': 'east'}

# How wet each piece's gutter runs (0 dry, 1 stained, 2 half, 3 full): the deeper, the wetter. The entrance pavilion,
# whose runner meets no level doorway, keeps its stone; the upper wing is the driest; the crypt wing runs wet to the
# Downfall, whose landings set their own (DOWNFALL_LANDINGS).
GUTTER_FILL = {'stairs': 0, 'antechamber': 0, 'descent': 0, 'gallery': 0, 'descent_turn_0': 1, 'descent_turn_1': 1,
               'threshold': 1, 'nave': 1, 'wing_0_a': 1, 'wing_0_b': 1, 'beholding_hall': 1, 'beholding_focus': 1,
               'wing_1_a': 0, 'wing_1_b': 0, 'litany_hall': 0, 'litany_focus': 0, 'wing_2_a': 2, 'wing_2_b': 2,
               'downfall_hall': 2, 'downfall_focus': 3}


# --- gutters and basins -------------------------------------------------------------------------------------------

def gutter(room, x, y, z, fill, open_to=()):
    """A gutter cell; its sides are wired once the piece is done (wire). open_to: sides left open although nothing on
    that side is a gutter (the channel spills over a stair edge or runs under a door)."""
    room.set(x, y, z, GUTTER, {'fill': str(fill)})
    if open_to:
        _opens(room).setdefault((x, y, z), set()).update(open_to)


def _opens(room):
    if not hasattr(room, 'gutter_opens'):
        room.gutter_opens = {}
    return room.gutter_opens


def basin(room, x, y, z, facing, fill, block=BASIN):
    """A basin standing on the floor at (x, y, z), its lip toward `facing`. A blood_basin (the chamber's wells) has no
    state of its own."""
    if room.get(x, y, z) != AIR or room.get(x, y - 1, z) in (None, AIR):
        raise ValueError('no room for a basin at %s' % ((x, y, z),))
    room.set(x, y, z, block, None if block == WELL else {'facing': facing, 'fill': str(fill)})


def _is_basin(room, x, y, z):
    return room.get(x, y, z) in (BASIN, WELL)


def _floorish(room, c, x, y, z):
    return room.get(x, y, z) in (c['floor'], c['floor_alt'], c['inlay'], c['runner'], c['accent']) \
        and room.get(x, y + 1, z) == AIR


def channel(room, c, fill):
    """The walk's gutter: from every level doorway whose connector leaves runner behind, inward along the walk over
    plain floor with headroom until it meets a stair, a set piece, a wall or another gutter. Each doorway's connector
    leaves a gutter too, open through its face (a final state is turned with its piece)."""
    for (x, y, z), (name, props, nbt) in list(room.blocks.items()):
        if name != 'minecraft:jigsaw' or str(nbt['final_state']) != c['runner'] or str(nbt['name']).endswith('niche_out'):
            continue
        front = dict(props)['orientation'].split('_')[0]
        if front not in SIDES:
            continue
        dx, dz = SIDES[OPPOSITE[front]]
        cx, cz, run = x + dx, z + dz, 0
        while room.get(cx, y, cz) != GUTTER and _floorish(room, c, cx, y, cz):
            gutter(room, cx, y, cz, fill)
            cx, cz, run = cx + dx, cz + dz, run + 1
        if run and _stair_at(room, cx, y, cz):
            _opens(room).setdefault((cx - dx, y, cz - dz), set()).add(OPPOSITE[front])
        if run or room.get(cx, y, cz) == GUTTER:
            _outlet(room, x, y, z, front, fill)


def _stair_at(room, x, y, z):
    """Whether a flight meets the walk at this cell: a step up at the floor's course or a step down just under it."""
    here, below = room.get(x, y, z) or '', room.get(x, y - 1, z) or ''
    return here.endswith('_stairs') or (here == AIR and below.endswith('_stairs'))


def _outlet(room, x, y, z, front, fill):
    _opens(room)[(x, y, z)] = {front}
    room.gutter_outlets = getattr(room, 'gutter_outlets', {})
    room.gutter_outlets[(x, y, z)] = fill


def wire(room):
    """Every gutter's sides: open toward a neighbouring gutter or outlet, toward a basin standing on the next floor
    cell, and wherever it was left open; then each outlet's connector takes its gutter as its final state."""
    outlets = getattr(room, 'gutter_outlets', {})
    opens = _opens(room)
    cells = [pos for pos, (name, _, _) in room.blocks.items() if name == GUTTER] + list(outlets)

    def sides(x, y, z):
        out = {}
        for side, (dx, dz) in SIDES.items():
            n = (x + dx, y, z + dz)
            joined = room.get(*n) == GUTTER or n in outlets or _is_basin(room, x + dx, y + 1, z + dz)
            out[side] = 'true' if joined or side in opens.get((x, y, z), ()) else 'false'
        return out

    for pos in cells:
        x, y, z = pos
        if pos in outlets:
            name, props, nbt = room.blocks[pos]
            state = dict(sides(*pos), fill=str(outlets[pos]))
            nbt['final_state'] = type(nbt['final_state'])(
                '%s[%s]' % (GUTTER, ','.join('%s=%s' % kv for kv in sorted(state.items()))))
        else:
            fill = dict(room.blocks[pos][1])['fill']
            room.set(x, y, z, GUTTER, dict(sides(*pos), fill=fill))


def loop(cx, cz, radius):
    """A closed ring of floor cells round (cx, cz), each meeting the next side to side, so a channel laid on it runs
    unbroken."""
    cells = []
    for i in range(720):
        a = 2 * math.pi * i / 720
        cell = (cx + round(radius * math.cos(a)), cz + round(radius * math.sin(a)))
        if cells and cell == cells[-1]:
            continue
        if cells and abs(cell[0] - cells[-1][0]) + abs(cell[1] - cells[-1][1]) == 2:
            cells.append((cell[0], cells[-1][1]))
        cells.append(cell)
    seen = []
    for cell in cells:
        if cell not in seen:
            seen.append(cell)
    return seen


# --- motif passes, by piece -----------------------------------------------------------------------------------------

def motif(name, room, c):
    dress = DRESSING.get(name)
    if dress:
        dress(room, c)
    if name in GUTTER_FILL:
        channel(room, c, GUTTER_FILL[name])
    wire(room)
    for (x, y, z), (block, props, nbt) in list(room.blocks.items()):
        if block == 'hemomancy:reliquary_sconce':
            room.set(x, y, z, block, dict(props, flame='crimson'), nbt)


def dress_descent(room, c):
    """The bottom landing: a dry basin either side, against the walls before the gallery."""
    for z, facing in ((2, 'south'), (8, 'north')):
        basin(room, 12, 1, z, facing, 0)


def dress_gallery(room, c):
    """Dry basins either side of the way on to the spiral."""
    from .. import pieces
    for d, facing in ((-2, 'south'), (2, 'north')):
        basin(room, 25, 1, pieces.GALLERY_CENTRE + d, facing, 0)


def dress_nave(room, c):
    """A basin either side of the ring door's apron, stained: the blood the gutter brings runs on under the door."""
    from .. import pieces
    for d, facing in ((-3, 'south'), (3, 'north')):
        basin(room, pieces.NAVE_L - 2, 1, pieces.NAVE_ZC + d, facing, 1)
    gutter(room, pieces.NAVE_L - 2, 0, pieces.NAVE_ZC, GUTTER_FILL['nave'], open_to=('east',))


def dress_beholding(room, c):
    """The Reliquary: the hall's side walls a gallery of sealed niches, the Order's relics of him, two of them opened
    on an urn (chests/mausoleum). The bays beside the witness lights keep one high row; the others take two."""
    rng = random.Random('hemorath/beholding_niches')
    zc = room.size[2] // 2
    loot = {(7, 'north'), (15, 'south')}
    for xs in ((3, 4, 5), (7, 8, 9), (11, 12, 13), (15, 16, 17)):
        lit = any(room.get(x, 3, zc + s * 6) == 'hemomancy:reliquary_sconce' or
                  room.get(x, 3, zc + s * 4) == 'hemomancy:reliquary_sconce' for x in xs for s in (-1, 1))
        for side, plane, recess, facing in (('north', zc - 5, zc - 6, 'south'), ('south', zc + 5, zc + 6, 'north')):
            for y in ((5,) if lit else (3, 5)):
                if any(room.get(x, y, plane) not in [c['wall']] + list(c['wall_alt']) for x in xs):
                    continue
                kind = 'loot' if (xs[0], side) in loot and y == 3 else 'sealed'
                kit.loculus(room, c, xs, y, plane, recess, kind, rng, facing)


def dress_litany(room, c):
    """The Ration Hall: the six candidates on iron, and half-full basins along the walls between the stands."""
    zc = room.size[2] // 2
    for x in (6, 10, 14):
        for z in (zc - 3, zc + 3):
            room.set(x, 0, z, IRON)
    for x in (8, 12, 16):
        for z, facing in ((zc - 3, 'south'), (zc + 3, 'north')):
            basin(room, x, 1, z, facing, 2)


def dress_downfall_focus(room, c):
    """The fall ends in a basin: the focus stands in a ring of full channel round its block."""
    from .. import pieces
    length = pieces.FOCUS_ROOMS['DOWNFALL'][0]
    fx, zc = length - 4, room.size[2] // 2
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            if dx or dz:
                gutter(room, fx + dx, 0, zc + dz, 3)


def hall_sockets(room, c, sockets):
    """Every trial hall keeps a dry basin either side of its way in."""
    for kind, (x, y, z), facing in sockets:
        if kind == 'entry_flank':
            basin(room, x, y, z, facing, 0)


DRESSING = {'descent': dress_descent, 'gallery': dress_gallery, 'nave': dress_nave,
            'beholding_hall': dress_beholding, 'litany_hall': dress_litany, 'downfall_focus': dress_downfall_focus}


# --- the chamber: the pit -------------------------------------------------------------------------------------------

PIT = (17, 5)                 # the dry round channel's centre x (centre line z) and radius, on the boss floor
SPOKES = (15, 19)             # the x of the four channels out to the wall basins, each side
SPOKE_FILL = 1


def chamber_sockets(room, c, sockets):
    """The tomb's foot flanked by full basins; the relic niche behind his head sealed; crimson lights in the votive
    niches; the four aisle alcoves the spokes reach hold the Saint's wells (blood_basin), the rest and the aisle ends dry
    basins."""
    for kind, (x, y, z), facing in sockets:
        if kind == 'dais_flank':
            basin(room, x, y, z, facing, 3)
        elif kind == 'apse_niche':
            if room.get(x, y + 2, z) == AIR:
                for dy in (0, 1, 2, 3):
                    if room.get(x, y + dy, z) == AIR:
                        room.set(x, y + dy, z, kit.SEALED_NICHE, {'facing': facing})
            else:
                kit.sconce(room, x, y, z, facing)
        elif kind == 'aisle_alcove':
            if x in SPOKES:
                basin(room, x, y, z, facing, 3, WELL)
            else:
                basin(room, x, y, z, facing, 0)
        elif kind == 'aisle_end':
            basin(room, x, y, z, facing, 0)


def dress_chamber(room, c):
    """The processional's one long gutter, running full from the ring door, over the landing, spilling down the steps
    and on across the hall floor into the pit; the pit's ring dry; a dry channel on from it to the dais steps; and four
    stained spokes out through the arcades to the wells in the aisle alcoves."""
    from .. import pieces
    zc, entry = pieces.CHAMBER_ZC, pieces.CHAMBER_ENTRY
    x0, x1 = pieces.CHAMBER_CLEAR
    rng = random.Random('hemorath/chamber_floor')
    # The shared inlay ring gives way to the pit.
    for x in range(x0, x1 + 1):
        for z in range(zc - 6, zc + 7):
            if room.get(x, 0, z) == c['inlay']:
                room.set(x, 0, z, kit.paved(c, rng, abs(z - zc), 7))
    gutter(room, 1, entry, zc, 3, open_to=('west',))
    gutter(room, 2, entry, zc, 3, open_to=('east',))
    cx, radius = PIT
    ring = loop(cx, zc, radius)
    for x, z in ring:
        gutter(room, x, 0, z, 0)
    west = min(x for x, z in ring if z == zc)
    east = max(x for x, z in ring if z == zc)
    for x in range(pieces.CHAMBER_PILLARS[0], west):
        gutter(room, x, 0, zc, 3, open_to=('west',) if x == pieces.CHAMBER_PILLARS[0] else ())
    for x in range(east + 1, pieces.CHAMBER_STEPS[0]):
        gutter(room, x, 0, zc, 0, open_to=('east',) if x == pieces.CHAMBER_STEPS[0] - 1 else ())
    for x in SPOKES:
        for side in (-1, 1):
            near = max(abs(z - zc) for rx, z in ring if rx == x and (z - zc) * side > 0)
            for d in range(near + 1, pieces.CHAMBER_AISLE + 2):
                gutter(room, x, 0, zc + side * d, SPOKE_FILL)


DRESSING['chamber'] = dress_chamber


# --- the Downfall: the Draining Pit ---------------------------------------------------------------------------------

# Along x: (first x, last x, floor course) per landing, a flight between each. The hall keeps the shared Downfall's
# length, width and exit floor, so its focus room continues the run unchanged; its entry is five up, and the crypt
# approach drops only one (CRYPT_DROP) so the chamber stays the lowest room.
CRYPT_DROP = 1
DOWNFALL_L, DOWNFALL_W, DOWNFALL_H = 25, 11, 16
DOWNFALL_LANDINGS = [(1, 4, 5), (7, 11, 3), (14, 18, 1), (20, 24, 0)]
DOWNFALL_FLIGHTS = [(5, 6), (12, 13), (19,)]
DOWNFALL_FILL = [2, 2, 3, 3]          # each landing's channel and basins, wetter as the hall goes down
DOWNFALL_BASIN = [0, 1, 2, 3]
DOWNFALL_WAVES = [(1, (8, 10)), (2, (15, 17)), (3, (21, 23))]    # landing, the x of its two seals in the channel
DOWNFALL_SPRING = 12
DOWNFALL_PILASTERS = (3, 7, 11, 15, 19, 23)


def downfall_floor(x):
    for x0, x1, floor in DOWNFALL_LANDINGS:
        if x0 <= x <= x1:
            return floor
    for i, flight in enumerate(DOWNFALL_FLIGHTS):
        if x in flight:
            return DOWNFALL_LANDINGS[i][2] - 1 - flight.index(x)
    return DOWNFALL_LANDINGS[0][2]


def downfall_hall(saint):
    """The Draining Pit: one long vault over a floor that falls five in three flights past four landings, a basin either
    side of every landing and a channel down the walk, wetter at each. The gate mark is at the top; each wave rises
    from its seals in the channel as the challenger comes down to them; the last landing runs on into the focus room
    under a lowered arch, where the focus stands in the last basin."""
    from .. import pieces, trials
    c = CRYPT
    rng = random.Random('downfall/' + saint)
    length, width, height = DOWNFALL_L, DOWNFALL_W, DOWNFALL_H
    zc, half = width // 2, 3
    cornice = DOWNFALL_SPRING - 1
    room = kit.Room(length, height, width)
    kit.mass(room, c, rng, 0, length - 1, 0, height - 1, 0, width - 1)
    flights = {x for flight in DOWNFALL_FLIGHTS for x in flight}
    for x in range(1, length):
        floor = downfall_floor(x)
        for z in range(zc - half, zc + half + 1):
            for y in range(0, floor):
                room.set(x, y, z, c['base'])
            if x in flights:
                room.set(x, floor, z, c['trim_stairs'], stairs('west'))
            else:
                room.set(x, floor, z, kit.paved(c, rng, abs(z - zc), half))
            for y in range(floor + 1, DOWNFALL_SPRING):
                room.set(x, y, z, AIR)
        for plane in (zc - half - 1, zc + half + 1):
            room.set(x, floor + 1, plane, c['base'])
            room.set(x, cornice, plane, c['trim'])
            if x not in flights:
                room.set(x, floor + 2, plane, c['trim'])
            room.set(x, 8, plane, c['trim'])
        crown = kit.vault_x(room, c, x, zc, half, DOWNFALL_SPRING)
        if x % 6 == 3:
            room.set(x, crown, zc, c['light'])
    for i, x in enumerate(DOWNFALL_PILASTERS):
        floor = downfall_floor(x)
        for plane in (zc - half - 1, zc + half + 1):
            room.set(x, floor + 1, plane, c['base'])
            for y in range(floor + 2, cornice):
                room.set(x, y, plane, c['trim'])
            room.set(x, 8, plane, c['accent'])
            room.set(x, cornice, plane, c['accent'])
        kit.rib_x(room, c, x, zc, half, DOWNFALL_SPRING)
        if i % 2 == 1:
            kit.pilaster_sconces(room, c, x, zc, half, floor + 3)
    # The exit: a wall down to the focus room's height over the run on, its lintel and keystone the arch's head.
    clear = pieces.FOCUS_ROOMS['DOWNFALL'][4]
    x = length - 1
    for z in range(zc - half, zc + half + 1):
        for y in range(clear + 1, DOWNFALL_SPRING + half + 1):
            room.set(x, y, z, kit.field(rng, c))
        room.set(x, clear + 1, z, c['trim'])
    room.set(x, clear, zc - half, c['trim_stairs'], stairs('north', 'top'))
    room.set(x, clear, zc + half, c['trim_stairs'], stairs('south', 'top'))
    room.set(x, clear + 1, zc, c['accent'])

    # The entry: the arch at the top landing, the gate line and its mark.
    top = DOWNFALL_LANDINGS[0][2]
    kit.arch(room, c, 0, top, zc)
    for z in (zc - 1, zc + 1):
        room.set(0, top, z, c['runner'])
    marks = [trials.Mark(2, top + 1, zc, 'START', 0)]
    room.set(2, top, zc, c['accent'])
    for z in range(zc - half, zc + half + 1):
        if z != zc:
            room.set(3, top, z, c['trim'])
    # The channel down every landing, a branch to the basin either side, the seals in it.
    for i, (x0, x1, floor) in enumerate(DOWNFALL_LANDINGS):
        fill = DOWNFALL_FILL[i]
        for x in range(x0, x1 + 1):
            if (x, floor, zc) != (2, top, zc):
                gutter(room, x, floor, zc, fill, open_to=('east',) if x == x1 and i < 3 else
                       ('west',) if x == x0 and i > 0 else ())
        bx = x1 if i < 3 else 22
        for side, facing in ((-1, 'south'), (1, 'north')):
            for d in (1, 2):
                gutter(room, bx, floor, zc + side * d, fill)
            basin(room, bx, floor + 1, zc + side * half, facing, DOWNFALL_BASIN[i])
    room.set(3, top, zc, c['trim'])
    gutter(room, 3, top, zc, DOWNFALL_FILL[0])
    for wave, (landing, xs) in enumerate(DOWNFALL_WAVES):
        floor = DOWNFALL_LANDINGS[landing][2]
        for x in xs:
            mark = trials.Mark(x, floor + 1, zc, 'SPAWN', wave)
            room.set(x, floor + 1, zc, 'hemomancy:spawn_mark', {'wave': str(wave), 'due': 'false'})
            marks.append(mark)
    room.jigsaw(0, top, zc, 'west_up', pieces.conn('hall_in'), pieces.conn('approach_out'), pieces.EMPTY,
                c['runner'])
    room.jigsaw(length - 1, 0, zc, 'east_up', pieces.conn('hall_out'), pieces.conn('focus_in'),
                pieces.pool_name(saint, 'downfall_focus'), c['runner'], pieces.MANDATORY)
    return room, marks


HALLS = {'DOWNFALL': downfall_hall}

"""Putriciel (Track 10 owns this file): Mortem and Flammeus. His gift was consecration corruption, the rot that follows a
purge and comes back stronger. His complex was burned and rebuilt in cycles until they buried it, and it was never the
Order's. Walked backwards: the entrance is the last pyre, the chamber the first field he blessed.

Palette: mud-brick walls and vaults with packed mud, mossy stone-brick trims, bases and floors, packed-ash inlays,
chiseled bone for the keystones and capitals and bone brick for the seals. Motif: rot and burn in bands, read from the
door: every hall alternates a charred band, where everything but the bone went black (the wall field rough blackstone,
the dressed stone polished blackstone brick, the floor ash, the lamps out, the sconces smouldering, a rot pyre against
the wall), with an overgrown band, where the moss came back thick (moss over the floor and up the low courses, hyphae
patches, an infected-cap fringe under the cornice, roots hanging from the vault, spore shelves for lamps, the only green
light). The bands tighten toward the chamber (BAND) and their edges are ragged; the entrance is burned through, inside and
out. The fire is Flammeus fire, crimson with black soot.

The Burned Library's shelves are ash where it burned and rotting where it grew back; the litany stone is charred. The
Purge Corridors alternate burned sections, where the bog revenants rise from spawn marks in the ash, with rotten ones
whose floor is sodden mud brick (slow) under low roots. The Ash Garden's pylons are pyres round a circle of ash, the only
still ground, in beds of moss and burned stumps. The chamber is a rotted field: moss and rooted earth under the vault, the
tomb's dais an island of mud brick, a rot pyre at each corner of it, the vault's keystones charred."""
import random

from .. import kit
from ..kit import AIR, STEP, OPPOSITE, stairs, slab

SAINT = 'putriciel'
BIOMES = ['minecraft:swamp', 'minecraft:mangrove_swamp', 'minecraft:dark_forest']
# Chapel kinds by slot: slot 0 is the wing level with the nave, 1 the upper wing, 2 the crypt wing.
CHAPELS = ['LITANY', 'DOWNFALL', 'REPOSE']

ASH = 'hemomancy:packed_ash'
CHARRED_PLANKS = 'hemomancy:charred_planks'
CHARRED_SLAB = 'hemomancy:charred_planks_slab'
CHARRED_LOG = 'hemomancy:charred_log'
CHARRED_SHELF = 'hemomancy:charred_bookshelf'
SODDEN = 'hemomancy:sodden_mud_bricks'
SPORE_SHELF = 'hemomancy:spore_shelf'
PYRE = 'hemomancy:rot_pyre'
SCONCE = 'hemomancy:reliquary_sconce'
CAP = 'hemomancy:infected_cap'
HYPHAE = 'hemomancy:hyphae_block'
BOG_BODY = 'hemomancy:bog_body'
BLACKSTONE = 'minecraft:blackstone'
BLACK_BRICKS = 'minecraft:polished_blackstone_bricks'
BLACK_CRACKED = 'minecraft:cracked_polished_blackstone_bricks'
BLACK_STAIRS = 'minecraft:polished_blackstone_brick_stairs'
CHARRED_KEY = 'minecraft:chiseled_polished_blackstone'
MOSSY = 'minecraft:mossy_stone_bricks'
MOSS = 'minecraft:moss_block'
MOSS_CARPET = 'minecraft:moss_carpet'
ROOTED = 'minecraft:rooted_dirt'
ROOTS = 'minecraft:hanging_roots'
MANGROVE_ROOTS = 'minecraft:mangrove_roots'
BOOKSHELF = 'minecraft:bookshelf'

CRYPT = kit.Theme({
    'base': MOSSY, 'wall': 'minecraft:mud_bricks', 'wall_alt': ['minecraft:packed_mud'],
    'trim': MOSSY, 'trim_stairs': 'minecraft:mossy_stone_brick_stairs',
    'trim_wall': 'minecraft:mossy_stone_brick_wall', 'trim_slab': 'minecraft:mossy_stone_brick_slab',
    'accent': 'hemomancy:chiseled_bone_bricks', 'floor': MOSSY,
    'floor_alt': 'minecraft:mossy_cobblestone', 'inlay': ASH, 'runner': 'minecraft:packed_mud',
    'seal': 'hemomancy:bone_bricks', 'vault': 'minecraft:mud_bricks',
    'vault_stairs': 'minecraft:mud_brick_stairs', 'button': 'minecraft:polished_blackstone_button',
    'light': 'minecraft:verdant_froglight', 'track': BLACK_BRICKS, 'floor_slab': 'minecraft:mossy_stone_brick_slab',
    'vault_alt': ['minecraft:packed_mud']},
    variants={MOSSY: ['minecraft:stone_bricks', 'minecraft:cracked_stone_bricks'],
              'hemomancy:bone_bricks': ['hemomancy:cracked_bone_bricks'],
              'hemomancy:cracked_bone_bricks': ['hemomancy:bone_bricks'],
              ASH: ['minecraft:cracked_stone_bricks', BLACK_CRACKED], 'minecraft:cracked_stone_bricks': [ASH],
              BLACKSTONE: [BLACK_CRACKED, BLACK_BRICKS], BLACK_BRICKS: [BLACK_CRACKED, BLACKSTONE],
              BLACK_CRACKED: [BLACK_BRICKS, BLACKSTONE], MOSS: [ROOTED, MOSSY], ROOTED: [MOSS],
              SODDEN: ['minecraft:mud'], 'minecraft:mud': [SODDEN], HYPHAE: [MOSS], CAP: [HYPHAE],
              CHARRED_PLANKS: [CHARRED_LOG]})

# Band width by piece, along its long axis (the descent turns' shafts band by course): the bands tighten toward the
# chamber. None: burned through (the entrance, the last pyre).
BAND = {'top': None, 'stairs': None, 'antechamber': None, 'descent': 8, 'gallery': 8, 'descent_turn_0': 7,
        'descent_turn_1': 7, 'niche_ossuary': 7, 'niche_reliquary': 7, 'niche_shrine': 7, 'niche_wall': 7,
        'threshold': 7, 'nave': 6, 'wing_0_a': 6, 'wing_0_b': 6, 'litany_hall': 5, 'litany_focus': 5,
        'wing_1_a': 5, 'wing_1_b': 5, 'downfall_focus': 4, 'wing_2_a': 4, 'wing_2_b': 4, 'repose_hall': 4,
        'repose_focus': 4, 'chamber': 3}
RAGGED = 5                 # bands this wide or wider get a ragged edge (a cell either way, by course and depth)
# The Purge Corridors band by section instead: the burned sections round each wave's seals (x 6-8, 12-14, 18-20),
# rotten between them.
DOWNFALL_BURNED = ((6, 8), (12, 14), (18, 20))
DOWNFALL_ROTTEN = ((4, 5), (9, 11), (15, 17), (21, 23))

# Rot pyres the generic pass stands against a burned band's walls (by default one per ten blocks of length): none in the
# threshold (its corridor is pinned open), the turns' shafts, the niches, or the rooms that set their own.
PYRES = {'top': 0, 'threshold': 0, 'descent_turn_0': 0, 'descent_turn_1': 0, 'niche_ossuary': 0, 'niche_reliquary': 0,
         'niche_shrine': 0, 'niche_wall': 0, 'downfall_hall': 0, 'repose_hall': 0, 'chamber': 0, 'litany_hall': 0}
PYRE_SPACING = 6
# Overgrowth in a rotten band.
ROOT_CHANCE = 0.45
MOSS_FLOOR = 0.6
MOSS_WALL = 0.45
CARPET = 0.4               # moss carpet on a floor cell along the wall foot
HYPHAE_PATCHES = 6.0       # patches per hundred exposed wall faces
SHELF_EVERY = 4            # a spore shelf on roughly one exposed wall face in this many, at lamp height
# What each block of the palette burns to: dressed stone (trims, bases, piers) to polished blackstone brick and its
# shaped pieces to the same family; the mud of the field and vault to rough blackstone; floors to ash.
CHAR_SHAPED = {'minecraft:mossy_stone_brick_stairs': BLACK_STAIRS, 'minecraft:mud_brick_stairs': BLACK_STAIRS,
               'minecraft:mossy_stone_brick_slab': 'minecraft:polished_blackstone_brick_slab',
               'minecraft:mud_brick_slab': 'minecraft:polished_blackstone_brick_slab',
               'minecraft:mossy_stone_brick_wall': 'minecraft:polished_blackstone_brick_wall',
               'minecraft:mud_brick_wall': 'minecraft:polished_blackstone_brick_wall'}
CHAR_DRESSED = (MOSSY, 'minecraft:stone_bricks', 'minecraft:cracked_stone_bricks')
CHAR_FIELD = ('minecraft:mud_bricks', 'minecraft:packed_mud', 'minecraft:mossy_cobblestone')

SET_PIECE_PREFIXES = ('hemomancy:', 'minecraft:jigsaw', 'minecraft:barrel')
HORIZONTAL = ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1))
NEIGHBOURS = HORIZONTAL + ((0, 1, 0), (0, -1, 0))
# Pieces whose outer faces stand in the open.
ABOVE_GROUND = ('top',)


# --- the hooks --------------------------------------------------------------------------------------------------------

def chamber_sockets(room, c, sockets):
    """Rot pyres on the dais flanks (the island's front corners); a bog body in each aisle alcove and at each aisle's
    end, the faithful the field took; behind his head the relic niche grown shut with hyphae under a spore shelf, and a
    spore shelf in each votive niche."""
    for kind, (x, y, z), facing in sockets:
        if kind == 'dais_flank':
            pyre(room, x, y, z)
        elif kind in ('aisle_alcove', 'aisle_end'):
            room.set(x, y, z, BOG_BODY, {'facing': facing, 'harvested': 'true', 'waterlogged': 'false'})
        elif kind == 'apse_niche':
            if room.get(x, y + 2, z) == AIR:
                room.set(x, y, z, HYPHAE)
                room.set(x, y + 1, z, CAP, {'snowy': 'false'})
                spore_shelf(room, x, y + 2, z, facing)
            else:
                spore_shelf(room, x, y, z, facing)


def hall_sockets(room, c, sockets):
    """Every trial hall's way in is flanked by a pair of rot pyres."""
    for kind, (x, y, z), facing in sockets:
        if kind == 'entry_flank':
            pyre(room, x, y, z)


def nave_sockets(room, c, sockets):
    """The congregation: bog bodies laid in the vessel's rows, every other place, facing the ring door."""
    for kind, (x, y, z), facing in sockets:
        if kind == 'congregation' and (x + z) % 2 == 0:
            room.set(x, y, z, BOG_BODY, {'facing': facing, 'harvested': 'true', 'waterlogged': 'false'})


def motif(name, room, c):
    """A piece's own dressing, then the bands: burned bands charred, overgrown bands grown over; then the lamps (a
    smouldering sconce in a burned band, a spore shelf in an overgrown one) and the pyres."""
    dress = DRESSING.get(name)
    if dress:
        dress(room, c)
    band = band_of(name, room)
    rng = random.Random('putriciel/bands/' + name)
    char(room, band, rng, name in ABOVE_GROUND)
    lamps(room, band)
    overgrow(room, c, band, rng, name)
    pyres(room, c, band, rng, name)
    for (x, y, z), (block, props, nbt) in list(room.blocks.items()):
        if block == 'hemomancy:pulse_pylon':
            room.set(x, y, z, block, dict(props, style='pyre'), nbt)
        elif block == 'hemomancy:spawn_mark':
            room.set(x, y, z, block, dict(props, style='ash'), nbt)
        elif block == 'hemomancy:litany_stone':
            room.set(x, y, z, block, dict(props, charred='true'), nbt)


def pyre(room, x, y, z, lit=True):
    if room.get(x, y, z) != AIR or room.get(x, y - 1, z) in (None, AIR):
        raise ValueError('no room for a rot pyre at %s' % ((x, y, z),))
    room.set(x, y, z, PYRE, {'lit': 'true' if lit else 'false'})


def spore_shelf(room, x, y, z, facing):
    """A spore shelf in the air cell at (x, y, z), growing out of the wall behind it toward `facing`."""
    room.set(x, y, z, SPORE_SHELF, {'facing': facing, 'bloom': 'false'})


# --- bands ------------------------------------------------------------------------------------------------------------

def band_of(name, room):
    """A function (x, y, z) -> True where the piece is burned, False where it is overgrown. A wide band's edge is
    ragged: each course and depth shifts it a cell either way, so the char reads as a burn, not a painted stripe."""
    if name == 'downfall_hall':
        return lambda x, y, z: not any(lo <= x <= hi for lo, hi in DOWNFALL_ROTTEN)
    width = BAND.get(name, 6)
    if width is None:
        return lambda x, y, z: True

    def ragged(a, b):
        return random.Random('%s/%d/%d' % (name, a, b)).randint(-1, 1) if width >= RAGGED else 0

    if name.startswith('descent_turn'):
        return lambda x, y, z: ((y + ragged(x, z)) // width) % 2 == 0
    if room.size[0] >= room.size[2]:
        return lambda x, y, z: ((x + ragged(y, z)) // width) % 2 == 0
    return lambda x, y, z: ((z + ragged(x, y)) // width) % 2 == 0


def _plain(entry):
    name, props, nbt = entry
    return not props and nbt is None


def _air(room, x, y, z):
    return room.get(x, y, z) == AIR


def char(room, band, rng, open_outside=False):
    """The burned bands: everything the fire reached went black but the bone. The mud of the wall field and vault turns
    to rough blackstone, the dressed stone (trims, bases, piers) to polished blackstone brick and its stairs, slabs and
    walls to the same family, so the architecture still reads in black; floors to ash and scorched brick; the crown lamps
    are put out. Bone accents and seals are left pale. Above ground the outer faces burn too."""
    def open_(x, y, z):
        if not room.inside(x, y, z) or room.blocks.get((x, y, z)) is None:
            return open_outside and y >= 1
        return _air(room, x, y, z)

    for (x, y, z), entry in list(room.blocks.items()):
        if not band(x, y, z):
            continue
        name, props, nbt = entry
        if name in CHAR_SHAPED:
            room.set(x, y, z, CHAR_SHAPED[name], dict(props), nbt)
            continue
        if not _plain(entry):
            continue
        if name == 'minecraft:verdant_froglight':
            room.set(x, y, z, CHARRED_KEY)
            continue
        if name not in CHAR_DRESSED and name not in CHAR_FIELD:
            continue
        sides = any(open_(x + dx, y, z + dz) for dx, _, dz in HORIZONTAL)
        up, down = open_(x, y + 1, z), open_(x, y - 1, z)
        if not (sides or up or down):
            continue
        roll = rng.random()
        if up and not sides and not down:
            room.set(x, y, z, ASH if roll < 0.5 else BLACK_CRACKED if roll < 0.75 else 'minecraft:cracked_stone_bricks')
        elif name in CHAR_DRESSED:
            room.set(x, y, z, BLACK_CRACKED if roll < 0.2 else BLACK_BRICKS)
        else:
            room.set(x, y, z, BLACKSTONE if roll < 0.55 else BLACK_CRACKED if roll < 0.85 else BLACK_BRICKS)


def lamps(room, band):
    """A sconce burns low (smoulder) in a burned band; in an overgrown band it is gone and a spore shelf grows from the
    wall in its place."""
    for (x, y, z), (name, props, nbt) in list(room.blocks.items()):
        if name != SCONCE:
            continue
        props = dict(props)
        if band(x, y, z):
            room.set(x, y, z, SCONCE, dict(props, flame='smoulder'))
        else:
            spore_shelf(room, x, y, z, props['facing'])


# --- overgrowth -------------------------------------------------------------------------------------------------------

def overgrow(room, c, band, rng, name):
    """The overgrown bands: moss over most of the floor and up the low courses of the wall field, moss carpet along the
    wall foot, hyphae patches spreading over the field and vault, the infected-cap fringe along the top of the wall under
    the cornice, roots hanging from the vault over head height, and spore shelves on the walls at lamp height."""
    field = set([c['wall']] + list(c['wall_alt']))
    floors = (c['floor'], c['floor_alt'])
    ceilings = field | {c['vault'], MOSSY} | set(c['vault_alt'])
    keep_open = _pinned(name)
    faces = []
    for (x, y, z), entry in sorted(room.blocks.items()):
        if band(x, y, z) or not _plain(entry):
            continue
        block = entry[0]
        if block in floors and _air(room, x, y + 1, z):
            roll = rng.random()
            if roll < MOSS_FLOOR:
                room.set(x, y, z, MOSS)
            elif roll < MOSS_FLOOR + 0.1:
                room.set(x, y, z, ROOTED)
            continue
        if block not in ceilings:
            continue
        sides = [(dx, dz) for dx, _, dz in HORIZONTAL if _air(room, x + dx, y, z + dz)]
        if sides and block in field:
            faces.append((x, y, z, sides))
            h = _height_over_floor(room, x + sides[0][0], y, z + sides[0][1])
            if h <= 2 and rng.random() < MOSS_WALL:
                room.set(x, y, z, MOSS)
            elif not _air(room, x, y + 1, z) and room.get(x, y + 1, z) not in field and h >= 4:
                room.set(x, y, z, CAP, {'snowy': 'false'})
        elif not sides and _air(room, x, y - 1, z) and _height_over_floor(room, x, y - 1, z) >= 4 \
                and (x, y - 1, z) not in keep_open and rng.random() < ROOT_CHANCE:
            room.set(x, y - 1, z, ROOTS, {'waterlogged': 'false'})
    # Hyphae patches creep over the field and vault that is left, a few cells from each seed.
    patches = round(len(faces) * HYPHAE_PATCHES / 100)
    for seed in rng.sample(faces, min(patches, len(faces))):
        cells, queue = [], [seed[:3]]
        size = rng.randint(3, 8)
        while queue and len(cells) < size:
            cell = queue.pop(rng.randrange(len(queue)))
            entry = room.blocks.get(cell)
            if cell in cells or not entry or not _plain(entry) or entry[0] not in field | {MOSS} or band(*cell) \
                    or not any(_air(room, cell[0] + a, cell[1] + b, cell[2] + d) for a, b, d in NEIGHBOURS):
                continue
            cells.append(cell)
            room.set(*cell, HYPHAE)
            queue += [(cell[0] + a, cell[1] + b, cell[2] + d) for a, b, d in NEIGHBOURS]
    # Moss carpet along the wall foot, off the walk and clear of doors, connectors and set pieces.
    for (x, y, z), (block, props, nbt) in sorted(room.blocks.items()):
        if block != AIR or band(x, y, z) or (x, y, z) in keep_open or room.get(x, y - 1, z) not in (MOSS, ROOTED) \
                or not any(room.get(x + dx, y, z + dz) not in (None, AIR) for dx, _, dz in HORIZONTAL) \
                or _beside_anything(room, x, y, z) or _near_connector(room, x, y, z) or rng.random() >= CARPET:
            continue
        room.set(x, y, z, MOSS_CARPET)
    # Spore shelves at lamp height (two to four courses over the floor), out of the walk and clear of set pieces.
    count = 0
    for x, y, z, sides in faces:
        dx, dz = sides[0]
        cell = (x + dx, y, z + dz)
        h = _height_over_floor(room, *cell)
        if not 2 <= h <= 4 or room.get(*cell) != AIR or room.get(x, y, z) not in field | {MOSS, HYPHAE} \
                or cell in keep_open:
            continue
        count += 1
        if count % SHELF_EVERY or _beside_anything(room, *cell):
            continue
        spore_shelf(room, *cell, _facing(dx, dz))


def _near_connector(room, x, y, z):
    """Whether a connector lies within two cells (a doorway's threshold stays bare)."""
    return any(room.get(x + dx, y + dy, z + dz) == 'minecraft:jigsaw'
               for dx in range(-2, 3) for dy in (-1, 0) for dz in range(-2, 3))


def _facing(dx, dz):
    for side, step in STEP.items():
        if step == (dx, dz):
            return side
    raise ValueError((dx, dz))


def _height_over_floor(room, x, y, z):
    """How many open courses stand under an air cell before something solid (its height over the floor); 99 off the
    room's edge."""
    h = 0
    while room.inside(x, y - 1 - h, z) and _air(room, x, y - 1 - h, z):
        h += 1
    return h + 1 if room.inside(x, y - 1 - h, z) else 99


def _beside_anything(room, x, y, z):
    """Whether a cell touches a set piece, a connector, a door or another lamp (so growth never fouls them)."""
    for dx, dy, dz in NEIGHBOURS:
        name = room.get(x + dx, y + dy, z + dz)
        if name and (name.startswith(SET_PIECE_PREFIXES) and name not in (ASH, SODDEN, CHARRED_SHELF, CHARRED_PLANKS,
                                                                           CHARRED_LOG, CHARRED_SLAB, HYPHAE, CAP)
                     or name.endswith(('_door', '_button'))):
            return True
    return False


def _pinned(name):
    """Air cells a JVM test keeps open: the threshold's corridor and the chamber's boss floor."""
    from .. import pieces
    if name == 'threshold':
        return {(x, y, z) for x in range(1, 14) for y in range(1, 6) for z in range(3, 8)}
    if name == 'chamber':
        x0, x1 = pieces.CHAMBER_CLEAR
        zc = pieces.CHAMBER_ZC
        return {(x, y, z) for x in range(x0, x1 + 1) for y in range(1, 5) for z in range(zc - 7, zc + 8)}
    return set()


# --- pyres ------------------------------------------------------------------------------------------------------------

def pyres(room, c, band, rng, name):
    """Stands the piece's share of rot pyres in its burned bands (PYRES): on floor cells with a wall at their back, open
    along the wall either side and three open in front, two clear of set pieces, connectors and flights."""
    count = PYRES.get(name, max(1, room.size[0] // 10) if BAND.get(name, 6) else 2)
    placed = [pos for pos, (block, _, _) in room.blocks.items() if block == PYRE]
    keep_open = _pinned(name)
    for x, y, z in _pyre_sites(room, c, band, rng):
        if count <= 0:
            break
        if (x, y, z) in keep_open or any((x - px) ** 2 + (z - pz) ** 2 < PYRE_SPACING ** 2 and abs(y - py) < 4
                                         for px, py, pz in placed):
            continue
        pyre(room, x, y, z)
        placed.append((x, y, z))
        count -= 1


def _pyre_sites(room, c, band, rng):
    sites = []
    for (x, y, z), (block, props, nbt) in room.blocks.items():
        if block != AIR or not band(x, y, z) or not _air(room, x, y + 1, z):
            continue
        below = room.get(x, y - 1, z)
        if below in (None, AIR) or below.endswith(('_stairs', '_slab')) or below.startswith('minecraft:jigsaw'):
            continue
        backs = [(dx, dz) for dx, _, dz in HORIZONTAL if not _air(room, x + dx, y, z + dz)]
        if len(backs) != 1:
            continue
        bx, bz = backs[0]
        fx, fz = -bx, -bz
        if any(not _air(room, x + fx * d, y, z + fz * d) for d in (1, 2, 3)):
            continue
        if _crowded(room, x, y, z):
            continue
        sites.append((x, y, z))
    rng.shuffle(sites)
    return sites


def _crowded(room, x, y, z):
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            for dy in range(-1, 3):
                entry = room.blocks.get((x + dx, y + dy, z + dz))
                if entry is None:
                    if dy == -1 and abs(dx) <= 1 and abs(dz) <= 1:
                        return True
                    continue
                name, props, nbt = entry
                if name == AIR:
                    if dy == -1 and abs(dx) <= 1 and abs(dz) <= 1:
                        return True
                    continue
                if name == 'minecraft:jigsaw' or name.startswith('hemomancy:') and name not in (
                        ASH, HYPHAE, CAP, 'hemomancy:bone_bricks', 'hemomancy:cracked_bone_bricks',
                        'hemomancy:chiseled_bone_bricks', 'hemomancy:bone_brick_wall'):
                    return True
                if name.endswith('_stairs') and dy <= 1 and dict(props).get('half') == 'bottom':
                    return True
    return False


# --- the last pyre (the pavilion) ------------------------------------------------------------------------------------

def dress_top(room, c):
    """The pavilion hall burns in its four corners: the last pyre they lit, the first thing seen through the door."""
    from .. import entrance
    lo, hi = 4, entrance.TOP - 5
    for x in (lo, hi):
        for z in (lo, hi):
            pyre(room, x, 1, z)


# --- the Burned Library (Litany) --------------------------------------------------------------------------------------

def dress_litany(room, c):
    """The scriptorium's side walls are shelving between the pilasters, three courses tall: charred, and fallen to ash
    in places, where the hall burned; rotting where it grew back (laid before the bands, which leave shelves alone). The
    candidates stand among scorched remains: an ash bed round each stand, a fallen board or a bone beside it."""
    rng = random.Random('putriciel/library')
    zc = room.size[2] // 2
    band = band_of('litany_hall', room)
    field = set([c['wall']] + list(c['wall_alt']))
    for x in range(1, room.size[0] - 1):
        for plane in (zc - 4, zc + 4):
            for y in (2, 3, 4):
                if room.get(x, y, plane) not in field:
                    continue
                if band(x, y, plane):
                    room.set(x, y, plane, ASH if rng.random() < 0.3 else CHARRED_SHELF)
                else:
                    room.set(x, y, plane, HYPHAE if rng.random() < 0.15 else BOOKSHELF)
    for x, y, z in _stands(room):
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                if (dx or dz) and room.get(x + dx, 0, z + dz) in (c['floor'], c['floor_alt']):
                    room.set(x + dx, 0, z + dz, ASH)
        side = 1 if rng.random() < 0.5 else -1
        cell = (x + side, 1, z)
        if room.get(*cell) == AIR:
            if rng.random() < 0.5:
                room.set(*cell, CHARRED_SLAB, slab())
            else:
                room.set(*cell, *kit.bone('x'))


def _stands(room):
    return sorted(pos for pos, (name, _, _) in room.blocks.items() if name == 'hemomancy:offering_stand')


# --- the Purge Corridors (Downfall) -----------------------------------------------------------------------------------

def dress_downfall(room, c):
    """The gauntlet alternates: each wave's seals sit in a burned section, ash underfoot round them; between them the
    rotten sections, whose floor is sodden mud brick (slow going) under roots hanging low from the vault."""
    rng = random.Random('putriciel/purge')
    zc = room.size[2] // 2
    for lo, hi in DOWNFALL_BURNED:
        for x in range(lo, hi + 1):
            for z in range(zc - 3, zc + 4):
                if room.get(x, 0, z) in (c['floor'], c['floor_alt'], c['inlay']) and room.get(x, 1, z) == AIR:
                    room.set(x, 0, z, ASH)
    for lo, hi in DOWNFALL_ROTTEN:
        for x in range(lo, hi + 1):
            for z in range(zc - 3, zc + 4):
                if room.get(x, 0, z) in (c['floor'], c['floor_alt'], c['runner'], c['inlay'], c['trim']) \
                        and room.get(x, 1, z) == AIR:
                    room.set(x, 0, z, SODDEN)
                # The roots hang low: a mat of mangrove roots under the vault, hanging roots under the mat.
                top = _ceiling(room, x, z)
                if top is None:
                    continue
                low = top - 1
                if rng.random() < 0.7:
                    room.set(x, low, z, MANGROVE_ROOTS, {'waterlogged': 'false'})
                    if rng.random() < 0.45 and low - 1 >= 4 and room.get(x, low - 1, z) == AIR:
                        low -= 1
                        room.set(x, low, z, MANGROVE_ROOTS, {'waterlogged': 'false'})
                    if low - 1 >= 3 and room.get(x, low - 1, z) == AIR:
                        room.set(x, low - 1, z, ROOTS, {'waterlogged': 'false'})


def _ceiling(room, x, z):
    """The first air course under the vault over the walk at (x, z), or None."""
    y = 1
    while room.inside(x, y, z) and room.get(x, y, z) == AIR:
        y += 1
    return y if y > 4 and room.inside(x, y, z) else None


# --- the Ash Garden (Repose) ------------------------------------------------------------------------------------------

def dress_ash_garden(room, c):
    """The still circle is ash, edged in blackstone; the floor round it is garden beds of moss and rooted earth with
    burned stumps and bone heaps standing in them, clear of the walk in and of the pyres."""
    from .. import trials
    rng = random.Random('putriciel/garden')
    zc = room.size[2] // 2
    cx = 7
    r = trials.REPOSE_RADIUS
    for x in range(1, room.size[0]):
        for z in range(1, room.size[2] - 1):
            if room.get(x, 1, z) != AIR or room.get(x, 0, z) in (None, AIR, 'minecraft:jigsaw'):
                continue
            d2 = (x - cx) ** 2 + (z - zc) ** 2
            if d2 <= (r - 0.5) ** 2:
                room.set(x, 0, z, ASH)
            elif d2 < (r + 0.5) ** 2:
                room.set(x, 0, z, BLACK_BRICKS)
            elif abs(z - zc) > 1:
                room.set(x, 0, z, MOSS if rng.random() < 0.65 else ROOTED)
    # Burned stumps and bone heaps in the beds, off the walk and two clear of the pylons and the circle.
    pylons = [pos for pos, (name, _, _) in room.blocks.items() if name == 'hemomancy:pulse_pylon']
    spots = [(x, z) for x in range(2, room.size[0] - 2) for z in range(2, room.size[2] - 2)
             if abs(z - zc) > 2 and (x - cx) ** 2 + (z - zc) ** 2 > (r + 1.5) ** 2
             and all(max(abs(x - px), abs(z - pz)) > 2 for px, _, pz in pylons)
             and room.get(x, 1, z) == AIR and room.get(x, 0, z) in (MOSS, ROOTED)]
    rng.shuffle(spots)
    placed = []
    for x, z in spots:
        if len(placed) >= 6 or any(abs(x - a) + abs(z - b) < 3 for a, b in placed):
            continue
        if len(placed) % 2 == 0:
            room.set(x, 1, z, CHARRED_LOG, {'axis': 'y'})
        else:
            room.set(x, 1, z, *kit.bone('x' if rng.random() < 0.5 else 'z'))
        placed.append((x, z))
    for x, z in spots:
        if room.get(x, 1, z) == AIR and rng.random() < 0.25:
            room.set(x, 1, z, MOSS_CARPET)


# --- the chamber: the first field -------------------------------------------------------------------------------------

BACK_PYRES = ((33, -5), (33, 5))     # the island's back corners, in the conch behind the tomb's head


def dress_chamber(room, c):
    """The rotted field: the hall floor and the aisles moss and rooted earth with mud where it is wettest, moss carpet
    and roots in the aisles (the boss floor stays flat and open); the dais steps and the apse floor an island of mud
    brick, a rot pyre at each of its back corners (the flank sockets hold the front pair); the vault's keystones
    charred and its crown lamps out, so the room is lit by the pyres and the spore glow the fight turns between."""
    from .. import pieces
    rng = random.Random('putriciel/field')
    zc = pieces.CHAMBER_ZC
    x0, x1 = pieces.CHAMBER_PILLARS[0], pieces.CHAMBER_STEPS[0] - 1
    clear = _pinned('chamber')
    paving = (c['floor'], c['floor_alt'], c['inlay'], c['runner'])
    for x in range(x0 - 1, x1 + 1):
        for z in range(1, pieces.CHAMBER_W - 1):
            if room.get(x, 0, z) not in paving or room.get(x, 1, z) != AIR:
                continue
            roll = rng.random()
            room.set(x, 0, z, MOSS if roll < 0.6 else ROOTED if roll < 0.85 else 'minecraft:mud')
            if (x, 1, z) not in clear and rng.random() < 0.3:
                room.set(x, 1, z, MOSS_CARPET)
    # The island: the dais' steps in mud brick, the apse floor laid in mud brick and packed mud.
    for step, x in enumerate(pieces.CHAMBER_STEPS):
        for z in range(zc - pieces.CHAMBER_RANK + 1, zc + pieces.CHAMBER_RANK):
            entry = room.blocks.get((x, step + 1, z))
            if entry and entry[0] == c['trim_stairs']:
                room.set(x, step + 1, z, 'minecraft:mud_brick_stairs', dict(entry[1]))
    top = pieces.CHAMBER_DAIS
    for x, half in pieces.CHAMBER_APSE.items():
        for z in range(zc - half, zc + half + 1):
            if room.get(x, top, z) in paving and room.get(x, top + 1, z) == AIR:
                room.set(x, top, z, 'minecraft:packed_mud' if rng.random() < 0.25 else 'minecraft:mud_bricks')
    for x, dz in BACK_PYRES:
        pyre(room, x, top + 1, zc + dz)
    # Charred keystones (an accent in the vault or over an opening; the ring door's own stays, the arch check reads it);
    # the crown lamps out.
    for (x, y, z), (name, props, nbt) in list(room.blocks.items()):
        if x > 0 and (name == c['light'] or name == c['accent'] and (y >= 9 or room.get(x, y - 1, z) == AIR)):
            room.set(x, y, z, CHARRED_KEY)
    # The field's lights, the two the fight turns between: a spore shelf high on every rank pillar's hall face, and a
    # pyre at the aisle foot of every other pillar.
    for i, x in enumerate(pieces.CHAMBER_PILLARS):
        for side in (-1, 1):
            plane = zc + side * pieces.CHAMBER_RANK
            facing = 'north' if side > 0 else 'south'
            if room.get(x, 6, plane - side) == AIR:
                spore_shelf(room, x, 6, plane - side, facing)
            if i % 2 == 0 and room.get(x, 1, plane + side) == AIR:
                pyre(room, x, 1, plane + side)
    # Roots hang from the aisle vaults and down the arcade bays' heads.
    for x in range(x0, x1 + 1):
        for side in (-1, 1):
            for z in range(zc + side * 9, zc + side * 12, side):
                if room.get(x, 5, z) == AIR and room.get(x, 6, z) not in (None, AIR) and rng.random() < 0.4:
                    room.set(x, 5, z, ROOTS, {'waterlogged': 'false'})


DRESSING = {'top': dress_top, 'litany_hall': dress_litany, 'downfall_hall': dress_downfall, 'repose_hall': dress_ash_garden,
            'chamber': dress_chamber}

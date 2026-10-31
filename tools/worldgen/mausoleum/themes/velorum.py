"""Velorum (Track 11 owns this file): Congeatio and Tenebris. Perfection through stillness: he stopped, and his
congregation stopped with him. Nobody built his complex for him; it is his own monastery, frozen around him, and it was
never the Order's. Walked backwards: the entrance is the gate the last devotee closed, the chamber the cell where his
stillness started.

Palette: the monastery's deepslate (polished deepslate bases, deepslate-brick trims and vaults, deepslate-tile floors,
dark lacquer for its furniture and cell fittings, hematic iron only in the fittings) under the ice that took it: a wall
field of packed ice, blue ice where it is oldest, blue-ice inlays in the floors. The frost thickens toward the cell
(FROST): at the gate the stone still shows through the ice, in the chamber the ice has gone blue. Rime crusts every
ledge (the rimed deepslate-brick family), icicles hang from the vaults, frozen runoff runs down the pilasters. Light is
blue-white and low: the wall lamps are rime lanterns recessed into the stone, and only the nave keeps its sea lanterns.

Motif: the frozen congregation, devotees everywhere, all turned toward his cell (east, the way on, in every piece that
runs that way), a few with their heads turned toward the walk. The Procession (Beholding) is two files frozen mid-step,
four of them the witnesses whose heads still turn. The Rule (Litany) is a refectory: the candidates wait in frozen bowls
down one long lacquer table, the devotees kneeling at it. The Cell Row (Repose) is a corridor of monks' cells with frost
pillars for pylons and the still circle a blue-ice ring before his own cell door; the cells' devotees show who kept
still. The chamber is the nave of his monastery inverted into his cell: rows kneeling in the aisles toward the dais,
the faithful prostrate round the tomb."""
import random

from .. import kit
from ..kit import AIR, STEP, OPPOSITE, LEFT_OF, stairs, slab

SAINT = 'velorum'
BIOMES = ['minecraft:snowy_plains', 'minecraft:snowy_taiga', 'minecraft:ice_spikes', 'minecraft:grove',
          'minecraft:snowy_slopes']
# Chapel kinds by slot: slot 0 is the wing level with the nave, 1 the upper wing, 2 the crypt wing.
CHAPELS = ['BEHOLDING', 'LITANY', 'REPOSE']

PACKED = 'minecraft:packed_ice'
BLUE = 'minecraft:blue_ice'
TILES = 'minecraft:deepslate_tiles'
CRACKED_TILES = 'minecraft:cracked_deepslate_tiles'
BRICKS = 'minecraft:deepslate_bricks'
CRACKED_BRICKS = 'minecraft:cracked_deepslate_bricks'
POLISHED = 'minecraft:polished_deepslate'
SEA_LANTERN = 'minecraft:sea_lantern'
RIMED = 'hemomancy:rimed_deepslate_bricks'
RIMED_STAIRS = 'hemomancy:rimed_deepslate_brick_stairs'
RIMED_SLAB = 'hemomancy:rimed_deepslate_brick_slab'
RIMED_WALL = 'hemomancy:rimed_deepslate_brick_wall'
LANTERN = 'hemomancy:rime_lantern'
ICICLE = 'hemomancy:hanging_icicle'
DRIP = 'hemomancy:frozen_drip'
LACQUER = 'hemomancy:dark_lacquer_planks'
LACQUER_STAIRS = 'hemomancy:dark_lacquer_planks_stairs'
LACQUER_SLAB = 'hemomancy:dark_lacquer_planks_slab'
DEVOTEE = 'hemomancy:frozen_devotee'
WITNESS = 'hemomancy:witness_devotee'
SCONCE = 'hemomancy:reliquary_sconce'

CRYPT = kit.Theme({
    'base': POLISHED, 'wall': PACKED, 'wall_alt': [BLUE],
    'trim': BRICKS, 'trim_stairs': 'minecraft:deepslate_brick_stairs',
    'trim_wall': 'minecraft:deepslate_brick_wall', 'trim_slab': 'minecraft:deepslate_brick_slab',
    'accent': 'minecraft:chiseled_deepslate', 'floor': TILES, 'floor_alt': CRACKED_TILES, 'inlay': BLUE,
    'runner': POLISHED, 'seal': POLISHED, 'vault': BRICKS, 'vault_stairs': 'minecraft:deepslate_brick_stairs',
    'button': 'minecraft:polished_blackstone_button', 'light': SEA_LANTERN, 'track': POLISHED,
    'floor_slab': 'minecraft:deepslate_tile_slab', 'vault_alt': [CRACKED_BRICKS]},
    variants={BLUE: [PACKED], POLISHED: [TILES, CRACKED_TILES], BRICKS: [CRACKED_BRICKS],
              CRACKED_BRICKS: [BRICKS], RIMED: [BRICKS, CRACKED_BRICKS], CRACKED_TILES: [TILES],
              LACQUER: [LACQUER_SLAB]})

# How far the stillness has frozen each piece: 1 at the gate (the stone shows through the ice), 2 the outer halls,
# 3 the deep wings, 4 the chamber (the ice gone blue). Walked backwards: it spread out from his cell.
FROST = {'top': 1, 'stairs': 1, 'antechamber': 1, 'descent': 1, 'gallery': 2, 'descent_turn_0': 2,
         'descent_turn_1': 2, 'niche_ossuary': 2, 'niche_reliquary': 2, 'niche_shrine': 2, 'niche_wall': 2,
         'threshold': 2, 'nave': 2, 'wing_0_a': 2, 'wing_0_b': 2, 'beholding_hall': 2, 'beholding_focus': 2,
         'wing_1_a': 3, 'wing_1_b': 3, 'litany_hall': 3, 'litany_focus': 3, 'wing_2_a': 3, 'wing_2_b': 3,
         'repose_hall': 4, 'repose_focus': 4, 'chamber': 4}
# Per level: the share of the wall field still ice (the rest the monastery's tiles), the share of the ice gone blue,
# the share of exposed ledges rimed, the chance a vault cell over head height hangs an icicle and the share of those
# two cells long, and the share of pilasters with frozen runoff down their faces.
ICE_FIELD = {1: 0.3, 2: 0.85, 3: 1.0, 4: 1.0}
BLUE_FIELD = {1: 0.0, 2: 0.08, 3: 0.2, 4: 0.45}
RIME = {1: 0.55, 2: 1.0, 3: 1.0, 4: 1.0}
ICICLES = {1: 0.05, 2: 0.12, 3: 0.2, 4: 0.26}
LONG_ICICLES = {1: 0.0, 2: 0.25, 3: 0.45, 4: 0.55}
DRIPS = {1: 0.15, 2: 0.4, 3: 0.6, 4: 0.8}
ICE_PATCH = 2              # the ice/stone pattern of the wall field changes every this many blocks
ICICLE_CLEAR = 4           # an icicle's lowest cell stands over at least this many open courses... less one (h >= 4)

# The congregation the generic pass kneels in a piece (devotees): how many and the poses drawn for them, in order. The
# rooms that dress their own (the Procession, the Rule, the Cell Row, the chamber, the gate) and the ones that must stay
# open (the threshold's pinned corridor, the shafts) are 0. Unlisted pieces take one per twelve blocks of length.
DEVOTEES = {'top': 0, 'stairs': 0, 'antechamber': (2, ['kneeling', 'kneeling']),
            'descent': (2, ['kneeling', 'bowed']), 'gallery': (3, ['kneeling', 'bowed', 'prostrate']),
            'descent_turn_0': 0, 'descent_turn_1': 0, 'threshold': 0, 'nave': 0,
            'niche_ossuary': (1, ['prostrate']), 'niche_reliquary': 0, 'niche_shrine': (1, ['kneeling']),
            'niche_wall': 0, 'beholding_hall': 0, 'litany_hall': 0, 'repose_hall': 0, 'chamber': 0,
            'beholding_focus': (2, ['prostrate', 'prostrate']), 'litany_focus': (2, ['bowed', 'kneeling']),
            'repose_focus': (2, ['prostrate', 'prostrate'])}
DEFAULT_POSES = ['kneeling', 'bowed', 'kneeling', 'prostrate']
DEVOTEE_SPACING = 3
TURNED = 0.25              # the share of the generic congregation whose heads have turned toward the walk
# Pieces whose way on is not east (the congregation faces away from its wall there instead).
FACE_OUT = ('niche_ossuary', 'niche_reliquary', 'niche_shrine', 'beholding_focus', 'litany_focus', 'repose_focus')

SIDES = ('north', 'south', 'east', 'west')
SET_PIECE_PREFIXES = ('hemomancy:', 'minecraft:jigsaw', 'minecraft:barrel')
STONE = (RIMED, LANTERN, LACQUER)
HANGING = (ICICLE, DRIP, 'minecraft:chain')
ABOVE_GROUND = ('top',)


# --- the hooks --------------------------------------------------------------------------------------------------------

# Track 6: down each chamber aisle the alcoves cycle kneeling, bowed, prostrate, bowed (the far aisle half a cycle on,
# so the two sides never mirror); the pair flanking the tomb reach toward it. The nave's rows kneel or bow, drawn once
# per place from a fixed seed.
ALCOVE_POSES = ['kneeling', 'bowed', 'prostrate', 'bowed']
NAVE_POSES = ['kneeling', 'kneeling', 'bowed']
NAVE_TURNED = 6            # one in this many of the nave's rows has turned its head toward the aisle


def chamber_sockets(room, c, sockets):
    """The aisle alcoves' congregation and the reaching pair on the dais flanks; in the relic niche behind his head the
    first who stilled with him, kneeling toward the tomb; a rime lantern burning in each votive niche."""
    alcoves = 0
    for kind, (x, y, z), facing in sockets:
        if kind == 'apse_niche':
            if room.get(x, y + 2, z) == AIR:
                devotee(room, x, y, z, facing, 'kneeling')
            else:
                room.set(x, y, z, LANTERN)
            continue
        if kind not in ('aisle_alcove', 'dais_flank'):
            continue
        if kind == 'dais_flank':
            pose = 'reaching'
        else:
            pose = ALCOVE_POSES[(alcoves + alcoves // 4 * 2) % len(ALCOVE_POSES)]
            alcoves += 1
        devotee(room, x, y, z, facing, pose)


def nave_sockets(room, c, sockets):
    """The nave's rows kneel or bow toward the ring door; a few have turned their heads toward the aisle."""
    from .. import pieces
    rng = random.Random('devotees/nave')
    for kind, (x, y, z), facing in sockets:
        pose = rng.choice(NAVE_POSES)
        head = 'forward'
        if rng.randrange(NAVE_TURNED) == 0:
            head = _head_toward(facing, 'north' if z > pieces.NAVE_ZC else 'south')
        devotee(room, x, y, z, facing, pose, head)


def hall_sockets(room, c, sockets):
    """Every trial hall's way in is flanked by a pair kneeling toward its focus."""
    for kind, (x, y, z), facing in sockets:
        if kind == 'entry_flank' and kit.two_tall_fits(room, x, y, z):
            devotee(room, x, y, z, facing, 'kneeling')


def motif(name, room, c):
    """A piece's own dressing, then the frost: the wall field frozen to its level, the lamps put behind ice, the
    congregation, rime on the ledges, runoff down the pilasters, and the icicles last (they hang from what is left)."""
    level = FROST.get(name, 2)
    rng = random.Random('velorum/frost/' + name)
    dress = DRESSING.get(name)
    if dress:
        dress(room, c)
    freeze_field(room, c, level, rng)
    lamps(room, c, name)
    congregation(room, c, name, rng)
    rime(room, c, level, rng, name in ABOVE_GROUND)
    drips(room, c, level, rng, name)
    icicles(room, level, rng, name, name in ABOVE_GROUND)


def devotee(room, x, y, z, facing, pose, head='forward'):
    if not kit.two_tall_fits(room, x, y, z):
        raise ValueError('no room for a devotee at %s' % ((x, y, z),))
    kit.devotee(room, x, y, z, facing, pose, head)


def _head_toward(facing, toward):
    """The way a devotee facing `facing` turns its head to look `toward`: left, right, or forward if it cannot."""
    if toward == LEFT_OF[facing]:
        return 'left'
    if toward == OPPOSITE[LEFT_OF[facing]]:
        return 'right'
    return 'forward'


# --- small tests on cells ---------------------------------------------------------------------------------------------

def _air(room, x, y, z):
    return room.get(x, y, z) in (AIR,) + HANGING


def _plain(entry):
    name, props, nbt = entry
    return not props and nbt is None


def _solid(room, x, y, z):
    """Plain masonry: a full block of the room that is not a set piece, a connector or anything with state."""
    entry = room.blocks.get((x, y, z))
    if entry is None:
        return False
    name, props, nbt = entry
    if name == AIR or nbt is not None or name.startswith(SET_PIECE_PREFIXES) and name not in STONE:
        return False
    return not props and not name.endswith(('_stairs', '_slab', '_wall', '_button', '_glass'))


def _height_over_floor(room, x, y, z):
    """How many open courses stand under an air cell before something solid (its height over the floor, counting
    itself); 99 off the room's edge."""
    h = 0
    while room.inside(x, y - 1 - h, z) and room.get(x, y - 1 - h, z) == AIR:
        h += 1
    return h + 1 if room.inside(x, y - 1 - h, z) and room.get(x, y - 1 - h, z) is not None else 99


def _pinned(name):
    """Air cells a JVM test keeps open: the threshold's corridor, the ring door's filler cells (the door grows into
    them when placed) and the chamber's boss floor."""
    from .. import pieces
    if name == 'nave':
        half = pieces.RING_DOOR_HALF
        return {(pieces.NAVE_L - 1, y, pieces.NAVE_ZC + dz) for y in range(1, pieces.RING_DOOR_H + 1)
                for dz in range(-half, half + 1)}
    if name == 'threshold':
        return {(x, y, z) for x in range(1, 14) for y in range(1, 6) for z in range(3, 8)}
    if name == 'chamber':
        x0, x1 = pieces.CHAMBER_CLEAR
        zc = pieces.CHAMBER_ZC
        return {(x, y, z) for x in range(x0, x1 + 1) for y in range(1, 5) for z in range(zc - 7, zc + 8)}
    return set()


def _over_doorway(room, x, y, z):
    """Whether a connector lies under or beside the column below (x, y, z): a doorway's passage, up to its arch."""
    return any(room.get(x + dx, y - dy, z + dz) == 'minecraft:jigsaw'
               for dx in (-1, 0, 1) for dz in (-1, 0, 1) for dy in range(-1, 7))


def _near_connector(room, x, y, z, reach=2):
    return any(room.get(x + dx, y + dy, z + dz) == 'minecraft:jigsaw'
               for dx in range(-reach, reach + 1) for dy in (-1, 0, 1) for dz in range(-reach, reach + 1))


# --- the frost --------------------------------------------------------------------------------------------------------

def freeze_field(room, c, level, rng):
    """The wall field to its level: where the frost is thin the monastery's deepslate tiles show through in patches;
    deeper, the ice is whole and, where it is oldest, blue."""
    field = (c['wall'],) + tuple(c['wall_alt'])

    def patch(x, y, z, salt):
        return random.Random('%s/%d/%d/%d' % (salt, x // ICE_PATCH, y // ICE_PATCH, z // ICE_PATCH)).random()

    for (x, y, z), entry in list(room.blocks.items()):
        if entry[0] not in field or not _plain(entry):
            continue
        if patch(x, y, z, 'ice') >= ICE_FIELD[level]:
            room.set(x, y, z, CRACKED_TILES if rng.random() < 0.15 else TILES)
        elif patch(x + 1, y, z + 1, 'blue') < BLUE_FIELD[level] or entry[0] == BLUE and rng.random() < 0.5:
            room.set(x, y, z, BLUE)
        else:
            room.set(x, y, z, PACKED)


def lamps(room, c, name):
    """Blue-white light behind ice. A sconce set into a wall becomes a rime lantern flush in its recess; one hung in
    front of a wall goes, and a lantern is set into the field behind it (beside the pilaster it hung on, where it hung on
    one). Every crown lamp is a rime lantern too, but in the nave, the one well-lit room, where the lamps are sea
    lanterns."""
    field = (c['wall'],) + tuple(c['wall_alt']) + (PACKED, BLUE, TILES, CRACKED_TILES)
    light = SEA_LANTERN if name == 'nave' else LANTERN
    for (x, y, z), (block, props, nbt) in list(room.blocks.items()):
        if block == c['light'] and name != 'nave':
            room.set(x, y, z, LANTERN)
            continue
        if block != SCONCE:
            continue
        facing = dict(props)['facing']
        dx, dz = STEP[facing]
        ax, az = dz, dx                         # along the wall
        if _solid(room, x + ax, y, z + az) and _solid(room, x - ax, y, z - az):
            room.set(x, y, z, light)
            continue
        wall = (x - dx, y, z - dz)
        room.set(x, y, z, AIR)
        if room.get(*wall) in field:
            room.set(*wall, light)
            continue
        for s in (1, -1):
            beside = (wall[0] + s * ax, y, wall[2] + s * az)
            if room.get(*beside) in field:
                room.set(*beside, light)
                break
        else:
            if _solid(room, *wall):
                room.set(*wall, light)


def rime(room, c, level, rng, open_outside=False):
    """Rime on every ledge: a deepslate-brick trim block, stairs, slab or wall whose top lies open turns to its rimed
    twin (the same state). Where the frost is thin only some of the ledges have taken it."""
    twins = {c['trim']: RIMED, c['trim_stairs']: RIMED_STAIRS, c['trim_slab']: RIMED_SLAB, c['trim_wall']: RIMED_WALL}

    def open_above(x, y, z):
        if not room.inside(x, y + 1, z) or room.blocks.get((x, y + 1, z)) is None:
            return open_outside
        return _air(room, x, y + 1, z) or room.get(x, y + 1, z) == ICICLE

    for (x, y, z), (block, props, nbt) in sorted(room.blocks.items()):
        if block not in twins or nbt is not None or not open_above(x, y, z):
            continue
        if block == c['trim'] and props:
            continue
        if rng.random() < RIME[level]:
            room.set(x, y, z, twins[block], dict(props))


def drips(room, c, level, rng, name):
    """Frozen runoff down the pilasters: in front of a pilaster's face, from under its capital down to head height, the
    lowest cell tapering to beads. A share of the pilasters by level."""
    keep_open = _pinned(name)
    trims = (c['trim'], RIMED)
    done = set()
    for (x, y, z), (block, props, nbt) in sorted(room.blocks.items()):
        if block != c['accent'] or (x, z) in done:
            continue
        # A capital over a pilaster: trim under it, with one open face (the room side).
        if room.get(x, y - 1, z) not in trims or room.get(x, y - 2, z) not in trims:
            continue
        fronts = [side for side in SIDES if room.get(x + STEP[side][0], y - 1, z + STEP[side][1]) == AIR]
        if len(fronts) != 1:
            continue
        done.add((x, z))
        if rng.random() >= DRIPS[level]:
            continue
        facing = fronts[0]
        fx, fz = x + STEP[facing][0], z + STEP[facing][1]
        cells = []
        cy = y - 1
        while room.get(x, cy, z) in trims and room.get(fx, cy, fz) == AIR and (fx, cy, fz) not in keep_open \
                and _height_over_floor(room, fx, cy, fz) >= 3:
            cells.append(cy)
            cy -= 1
        cells = cells[:rng.randint(2, 3)]
        if len(cells) < 2 or any(_beside_set_piece(room, fx, cy, fz) for cy in cells):
            continue
        for i, cy in enumerate(cells):
            room.set(fx, cy, fz, DRIP, {'facing': facing, 'end': 'true' if i == len(cells) - 1 else 'false'})


def _beside_set_piece(room, x, y, z):
    for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, -1, 0)):
        block = room.get(x + dx, y + dy, z + dz)
        if block and block.startswith(SET_PIECE_PREFIXES) and block not in STONE + HANGING \
                or block and block.endswith(('_door', '_button')):
            return True
    return False


def icicles(room, level, rng, name, open_outside=False):
    """Icicles from the vaults: under a ceiling cell with a full face down, over enough open courses that no icicle
    hangs in a face, a short cluster or a long icicle two cells deep. Above ground they hang from the overhangs too."""
    keep_open = _pinned(name)
    for (x, y, z), (block, props, nbt) in sorted(room.blocks.items()):
        if block == AIR or block.startswith(SET_PIECE_PREFIXES) and block not in STONE or nbt is not None:
            continue
        p = dict(props)
        if block.endswith('_stairs') and p.get('half') != 'top' or block.endswith('_slab') and p.get('type') == 'bottom' \
                or block.endswith(('_wall', '_button', '_door', '_trapdoor', '_bars')) or block in HANGING:
            continue
        below = (x, y - 1, z)
        if not room.inside(*below) or room.get(*below) != AIR or below in keep_open:
            continue
        h = _height_over_floor(room, *below)
        if h < ICICLE_CLEAR or (h == 99 and not open_outside) or _over_doorway(room, *below):
            continue
        if rng.random() >= ICICLES[level]:
            continue
        tip = (x, y - 2, z)
        if h >= ICICLE_CLEAR + 1 and tip not in keep_open and rng.random() < LONG_ICICLES[level]:
            room.set(*below, ICICLE, {'length': 'base'})
            room.set(*tip, ICICLE, {'length': 'tip'})
        else:
            room.set(*below, ICICLE, {'length': 'short'})


# --- the congregation -------------------------------------------------------------------------------------------------

def congregation(room, c, name, rng):
    """Kneels the piece's share of devotees (DEVOTEES) against its walls, turned toward his cell (east) or, where the
    way on is not east, away from the wall; a few have turned their heads toward the walk."""
    spec = DEVOTEES.get(name, (max(0, room.size[0] // 12), DEFAULT_POSES))
    if not spec:
        return
    count, poses = spec
    keep_open = _pinned(name)
    placed = [pos for pos, (block, props, _) in room.blocks.items()
              if block in (DEVOTEE, WITNESS) and dict(props).get('half') == 'lower']
    for x, y, z, back in _devotee_sites(room, c, rng):
        if count <= 0:
            break
        if (x, y, z) in keep_open or any((x - px) ** 2 + (z - pz) ** 2 < DEVOTEE_SPACING ** 2 and abs(y - py) < 3
                                         for px, py, pz in placed):
            continue
        away = OPPOSITE[back]
        facing = away if name in FACE_OUT else 'east'
        head = _head_toward(facing, away) if rng.random() < TURNED else 'forward'
        devotee(room, x, y, z, facing, poses[len(placed) % len(poses)] if poses else 'kneeling', head)
        placed.append((x, y, z))
        count -= 1


def _devotee_sites(room, c, rng):
    """Floor cells a devotee can kneel in: two tall and open over it, on plain paving, its back to a wall with open
    floor along the wall either side and two open cells in front, clear of stairs, set pieces and connectors."""
    floors = (c['floor'], c['floor_alt'], c['inlay'], c['runner'])
    sites = []
    for (x, y, z), (name, props, nbt) in room.blocks.items():
        if name not in floors or props:
            continue
        sy = y + 1
        if not (kit.two_tall_fits(room, x, sy, z) and room.get(x, sy + 2, z) == AIR):
            continue
        backs = [side for side in SIDES if _solid(room, x + STEP[side][0], sy, z + STEP[side][1])
                 and _solid(room, x + STEP[side][0], sy + 1, z + STEP[side][1])]
        if len(backs) != 1:
            continue
        bx, bz = STEP[backs[0]]
        if not all(room.get(x - bx * d, sy + h, z - bz * d) == AIR for d in (1, 2) for h in (0, 1)):
            continue
        ax, az = bz, bx
        if not all(room.get(x + s * ax, sy, z + s * az) == AIR for s in (-1, 1)):
            continue
        if _crowded(room, x, sy, z):
            continue
        sites.append((x, sy, z, backs[0]))
    sites.sort()
    rng.shuffle(sites)
    return sites


def _crowded(room, x, y, z):
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            for dy in range(-1, 3):
                entry = room.blocks.get((x + dx, y + dy, z + dz))
                if entry is None:
                    if dy == -1 and abs(dx) <= 1 and abs(dz) <= 1:
                        return True     # a ledge or the room's edge underfoot
                    continue
                name, props, nbt = entry
                if name == AIR:
                    if dy == -1 and abs(dx) <= 1 and abs(dz) <= 1:
                        return True     # a drop beside it
                    continue
                if name in ('minecraft:jigsaw', 'minecraft:barrel') or name.startswith('hemomancy:') and name not in STONE:
                    return True
                if name.endswith('_stairs') and dy <= 1 and dict(props).get('half') == 'bottom':
                    return True         # a flight beside it
    return False


# --- the gate (the pavilion) ------------------------------------------------------------------------------------------

def dress_top(room, c):
    """Just inside the west door, the last devotee: the one who closed the gate, turned back toward it, reaching."""
    from .. import entrance
    devotee(room, 5, entrance.FLOOR + 1, entrance.CENTRE - 2, 'west', 'reaching')


# --- the Procession (Beholding) ---------------------------------------------------------------------------------------

PROCESSION = (3, 7, 9, 14, 16)        # the x of the files' other devotees; the witnesses stand at 5 and 12


def dress_beholding(room, c):
    """Two files frozen mid-step down the hall toward his cell, either side of the walk. Four of them are the
    witnesses, the four who followed him into stillness: their heads still turn toward the walk when the memory touches
    them (witness_devotee, on the trial's marks)."""
    zc = room.size[2] // 2
    for (x, y, z), (block, props, nbt) in list(room.blocks.items()):
        if block == 'hemomancy:witness_stone':
            turn = 'right' if z < zc else 'left'      # walking east, the walk is to the north file's right
            for dy, half in ((0, 'lower'), (1, 'upper')):
                room.set(x, y + dy, z, WITNESS, {'facing': 'east', 'half': half, 'lit': 'false', 'turn': turn})
    for x in PROCESSION:
        for z in (zc - 3, zc + 3):
            devotee(room, x, 1, z, 'east', 'striding')


# --- the Rule (Litany): the refectory ---------------------------------------------------------------------------------

TABLE = range(4, 17)                  # the lacquer table's length down the centre line
BOWLS = (5, 7, 9, 11, 13, 15)         # the candidates' bowls on it
TABLE_LEGS = (4, 10, 16)              # trestles: an upside-down lacquer stair under the board
DINERS = {-1: (6, 10, 14), 1: (8, 12)}    # the devotees kneeling at the table, by side, between the bowls


def refectory(saint):
    """The Rule: the monastery's refectory. The litany stone by the door carries the rule; the six candidates wait in
    frozen bowls down one long lacquer table on the centre line, the devotees kneeling at it between them, the walk
    either side. Same length, width and exit floor as the shared Litany, so its focus room continues the run."""
    from .. import pieces, trials
    room, c, rng, zc = pieces.hall_shell(saint, 'litany', 19, 10, 11, 3, 5)
    marks = [trials.Mark(2, 1, zc - 3, 'LITANY_STONE', 0)]
    trials.litany_stone(room, c, marks[0], 'south')
    for x in TABLE:
        if x in TABLE_LEGS:
            room.set(x, 1, zc, LACQUER_STAIRS, stairs('east' if x < 10 else 'west', 'top'))
        else:
            room.set(x, 1, zc, LACQUER_SLAB, slab('top'))
        room.set(x, 0, zc, c['inlay'])
    order = list(range(6))
    random.Random('litany_stands/' + saint).shuffle(order)
    for i, x in enumerate(BOWLS):
        mark = trials.Mark(x, 2, zc, 'OFFERING_STAND', order[i])
        trials.offering_stand(room, c, mark, style='bowl')
        marks.append(mark)
    for side, xs in DINERS.items():
        for x in xs:
            devotee(room, x, 1, zc + side, 'south' if side < 0 else 'north', 'bowed')
    # A lacquer dado along both walls between the pilasters.
    for x in range(1, 18):
        for plane in (zc - 4, zc + 4):
            if room.get(x, 2, plane) in (c['trim'],) and room.get(x, 3, plane) in (c['wall'],) + tuple(c['wall_alt']):
                room.set(x, 2, plane, LACQUER)
    pieces.hall_connectors(room, c, saint, 'LITANY', zc, marks)
    return room, marks


# --- the Cell Row (Repose) --------------------------------------------------------------------------------------------

ROW_L, ROW_H, ROW_W = 15, 14, 13
ROW_HALF = 3                          # the corridor's half width (z 3-9); the cells open off its walls at z 2 and 10
ROW_CLEAR = 6
CELLS = (1, 5, 9)                     # each cell's west x; three wide, the partitions between them at 4, 8 and 12
CELL_H = 3
ROW_CIRCLE = 10                       # the still circle's centre x: its ring ends a step short of his cell door
ROW_PYLONS = ((8, -1), (12, -1), (8, 1), (12, 1))   # (x, side): frost pillars in the partitions' fronts
# Who kept still: by cell (west x, side), the pose in it, or None for the empty one. The still knelt or lay down facing
# the circle; the ones caught reaching for the door were taken where they stood.
CELL_POSES = {(1, -1): 'prostrate', (5, -1): 'kneeling', (9, -1): 'reaching',
              (1, 1): 'reaching', (5, 1): None, (9, 1): 'kneeling'}


def cell_row(saint):
    """The Cell Row: a corridor of monks' cells, three either side, opening off it under lacquer lintels; frost
    pillars (the pylons) in the partitions between the last cells; the still circle a blue-ice ring before his cell
    door, the arch at the end of the row into the focus room. Same length, width and exit floor as the shared Repose."""
    from .. import pieces, trials
    c = CRYPT
    rng = random.Random('cell_row/' + saint)
    zc = ROW_W // 2
    room = kit.Room(ROW_L, ROW_H, ROW_W)
    kit.mass(room, c, rng, 0, ROW_L - 1, 0, ROW_H - 1, 0, ROW_W - 1)
    kit.corridor(room, c, rng, 1, ROW_L - 2, zc, ROW_HALF, 0, ROW_CLEAR, pilasters=4, lights=4, phase=2,
                 sconces=False)
    kit.arch(room, c, 0, 0, zc)
    for z in (zc - 1, zc, zc + 1):
        room.set(0, 0, z, c['runner'])
    # His cell door: the arch at the row's end, three wide, into the focus room beyond.
    x = ROW_L - 1
    for z in range(zc - ROW_HALF, zc + ROW_HALF + 1):
        room.set(x, 0, z, kit.paved(c, rng, abs(z - zc), ROW_HALF))
    kit.arch(room, c, x, 0, zc)
    for z in (zc - 1, zc, zc + 1):
        room.set(x, 0, z, c['runner'])
    # The cells: three wide, two deep, three tall, a lacquer lintel over the open front, a lacquer pallet inside.
    for side in (-1, 1):
        plane = zc + side * (ROW_HALF + 1)
        back = plane + side
        facing = 'south' if side < 0 else 'north'
        for x0 in CELLS:
            for x in range(x0, x0 + 3):
                for z in (plane, back):
                    room.set(x, 0, z, c['floor_alt'] if rng.random() < 0.15 else c['floor'])
                    for y in range(1, CELL_H + 1):
                        room.set(x, y, z, AIR)
                room.set(x, CELL_H + 1, plane, LACQUER)
                room.set(x, CELL_H + 1, back, c['vault'])
            room.set(x0, CELL_H, plane, LACQUER_STAIRS, stairs('west', 'top'))
            room.set(x0 + 2, CELL_H, plane, LACQUER_STAIRS, stairs('east', 'top'))
            room.set(x0 + 2, 1, back, LACQUER_SLAB, slab('bottom'))
            pose = CELL_POSES[(x0, side)]
            if pose:
                devotee(room, x0 + 1, 1, back, facing, pose)
                room.set(x0, CELL_H, back + side, LANTERN)     # each cell's own light, high in its back wall
            else:
                room.set(x0 + 1, 2, back + side, LANTERN)     # the empty cell's votive light, where its monk knelt
    # The still circle: a blue-ice ring on the tiles, a chiseled stone at its heart.
    marks = [trials.Mark(ROW_CIRCLE, 1, zc, 'CIRCLE', 0)]
    for x, z in kit.ring(ROW_CIRCLE, zc, trials.REPOSE_RADIUS):
        room.set(x, 0, z, c['inlay'])
    room.set(ROW_CIRCLE, 0, zc, c['accent'])
    for i, (x, side) in enumerate(ROW_PYLONS):
        mark = trials.Mark(x, 3, zc + side * (ROW_HALF + 1), 'PYLON', i)
        trials.pylon(room, c, mark, 'south' if side < 0 else 'north', style='frost')
        marks.append(mark)
    pieces.hall_connectors(room, c, saint, 'REPOSE', zc, marks)
    return room, marks


HALLS = {'LITANY': refectory, 'REPOSE': cell_row}


# --- the chamber: his cell --------------------------------------------------------------------------------------------

AISLE_ROWS = (10, 12, 14, 16, 18, 20, 22)     # the aisles' kneeling rows, along each aisle's inner line
PROSTRATE = ((30, 5), (32, 5))                # (x, |dz|): the faithful laid out round the tomb, facing it
PILLAR_LAMP = 6                               # the rank pillars' lantern course, over the arcade's capital


def dress_chamber(room, c):
    """The nave of his monastery turned into his cell: a row kneeling down each aisle's inner line toward the dais,
    seen through the arcades from the open floor; the faithful prostrate round the tomb on the apse floor. A rime
    lantern is set into every rank pillar over its arcade, so the open floor is lit, if low."""
    from .. import pieces
    zc = pieces.CHAMBER_ZC
    for x in pieces.CHAMBER_PILLARS:
        for side in (-1, 1):
            plane = zc + side * pieces.CHAMBER_RANK
            if room.get(x, PILLAR_LAMP, plane) == c['trim']:
                room.set(x, PILLAR_LAMP, plane, LANTERN)
    for side in (-1, 1):
        z = zc + side * (pieces.CHAMBER_AISLE - 1)
        for i, x in enumerate(AISLE_ROWS):
            head = _head_toward('east', 'south' if side < 0 else 'north') if i % 3 == 1 else 'forward'
            devotee(room, x, 1, z, 'east', 'kneeling' if i % 2 == 0 else 'bowed', head)
    top = pieces.CHAMBER_DAIS + 1
    for x, dz in PROSTRATE:
        for side in (-1, 1):
            devotee(room, x, top, zc + side * dz, 'south' if side < 0 else 'north', 'prostrate')


def dress_nave(room, c):
    """The one well-lit room: a crown lamp over every bay of the vessel, not every other, and a sea lantern for the
    keystone of every other arcade arch, lighting the aisles under the galleries."""
    from .. import pieces
    zc = pieces.NAVE_ZC
    for b, p in enumerate(pieces.NAVE_PIERS[:-1]):
        if room.get(p + 2, 15, zc) == c['vault']:
            room.set(p + 2, 15, zc, SEA_LANTERN)
        if b % 2 == 0:
            for arcade in (zc - 5, zc + 5):
                if room.get(p + 2, pieces.NAVE_GALLERY - 1, arcade) == c['accent']:
                    room.set(p + 2, pieces.NAVE_GALLERY - 1, arcade, SEA_LANTERN)


DRESSING = {'top': dress_top, 'beholding_hall': dress_beholding, 'chamber': dress_chamber, 'nave': dress_nave}

"""Seraphae (Track 9 owns this file): Lux and Ductilis. Her gift was cursed, uncontrollable healing: everything she
touched grew. Her people contained her because they could not kill her, so her complex is a containment, not a tomb,
and it was never the Order's. Walked backwards: the entrance is the last ward they built, the chamber the infirmary
where she first laid hands on someone and could not stop.

Palette: quartz-brick walls speckled with calcite, smooth-quartz trims, polished-diorite floors with calcite runners and
smooth-quartz inlays, hematic-iron chains and collars, bone. Motif: chains and overgrowth. Containment anchors stand in
every hall, chained up to the vault and across to the far wall from an iron shackle post (anchors_and_chains). Where her radiance reached, the
stone has grown, more the deeper the hall (GROWTH): healed patches knit over the wall field, bone outgrowths, swollen
pillars, and in the deepest halls the growth glows from inside. Wall lights are sea lanterns recessed behind diorite
grilles (grilles). The Ward Walk's witness stones are the heads of patients' beds, the Infirmary Row is a ward of beds
with broken anchors along it, the Still Ward a round room whose pylons are anchors, and the chamber's tomb lies under
the largest anchor, hung from the apse vault where the chains of the four anchors round the tomb meet."""
import math
import random

from .. import kit
from ..kit import AIR, stairs, slab, STEP, OPPOSITE

SAINT = 'seraphae'
BIOMES = ['minecraft:meadow', 'minecraft:cherry_grove', 'minecraft:flower_forest', 'minecraft:sunflower_plains']
# Chapel kinds by slot: slot 0 is the wing level with the nave, 1 the upper wing, 2 the crypt wing.
CHAPELS = ['BEHOLDING', 'DOWNFALL', 'REPOSE']

HEALED = 'hemomancy:healed_quartz_bricks'
HEALED_CALCITE = 'hemomancy:healed_calcite'
RADIANT = 'hemomancy:radiant_healed_quartz'
OUTGROWTH = 'hemomancy:bone_outgrowth'
CHAIN = 'hemomancy:hematic_iron_chain'
COLLAR = 'hemomancy:chiseled_hematic_iron_block'
SHACKLE = 'hemomancy:hematic_iron_plating_wall'     # a slim iron post where a chain turns or branches
IRON_STAIRS = 'hemomancy:hematic_iron_plating_stairs'
ANCHOR = 'hemomancy:containment_anchor_block'
SCONCE = 'hemomancy:reliquary_sconce'
GRILLE = 'minecraft:diorite_wall'
LANTERN = 'minecraft:sea_lantern'

CRYPT = kit.Theme({
    'base': 'minecraft:polished_diorite', 'wall': 'minecraft:quartz_bricks', 'wall_alt': ['minecraft:calcite'],
    'trim': 'minecraft:smooth_quartz', 'trim_stairs': 'minecraft:smooth_quartz_stairs',
    'trim_wall': 'minecraft:diorite_wall', 'trim_slab': 'minecraft:smooth_quartz_slab',
    'accent': 'minecraft:chiseled_quartz_block', 'floor': 'minecraft:polished_diorite',
    'floor_alt': 'minecraft:calcite', 'inlay': 'minecraft:smooth_quartz', 'runner': 'minecraft:calcite',
    'seal': 'minecraft:calcite', 'vault': 'minecraft:quartz_bricks', 'vault_stairs': 'minecraft:quartz_stairs',
    'button': 'minecraft:stone_button', 'light': 'minecraft:sea_lantern',
    'track': 'minecraft:smooth_quartz', 'floor_slab': 'minecraft:polished_diorite_slab',
    'vault_alt': ['minecraft:calcite']},
    variants={HEALED: [HEALED_CALCITE], HEALED_CALCITE: [HEALED], RADIANT: [HEALED]})

# How far her growth has reached into each piece: 0 clean (the last ward they built), 1 healed patches, 2 with bone
# outgrowths, 3 with patches that glow from inside, 4 the chamber, where it began.
GROWTH = {'top': 0, 'stairs': 0, 'antechamber': 0, 'descent': 1, 'gallery': 1, 'descent_turn_0': 1,
          'descent_turn_1': 1, 'niche_ossuary': 1, 'niche_reliquary': 1, 'niche_shrine': 1, 'niche_wall': 1,
          'threshold': 1, 'nave': 2, 'wing_0_a': 2, 'wing_0_b': 2, 'beholding_hall': 2, 'beholding_focus': 2,
          'wing_1_a': 2, 'wing_1_b': 2, 'downfall_hall': 3, 'downfall_focus': 3, 'wing_2_a': 3, 'wing_2_b': 3,
          'repose_hall': 3, 'repose_focus': 3, 'chamber': 4}
# Per level: patches per hundred exposed wall faces, a patch's size range, the chance a cell beside a patch puts out a
# bone outgrowth, and the chance a patch's heart glows.
PATCHES = {1: 0.5, 2: 1.0, 3: 1.6, 4: 2.4}
PATCH_SIZE = {1: (3, 6), 2: (4, 9), 3: (5, 12), 4: (6, 16)}
OUTGROW = {1: 0.0, 2: 0.10, 3: 0.16, 4: 0.22}
GLOW = {1: 0.0, 2: 0.0, 3: 0.45, 4: 0.6}
# A wall outgrowth stands over at least this many open courses, out of reach and clear of a fight's floor.
OUT_OF_REACH = 4

# Anchors the generic pass stands in a piece (anchors_and_chains): by default one per nine blocks of its length; the
# chamber and the trial halls with their own layouts set theirs.
ANCHORS = {'top': 0, 'stairs': 0, 'antechamber': 2, 'threshold': 0, 'descent_turn_0': 0, 'descent_turn_1': 0, 'nave': 4,
           'beholding_hall': 4, 'downfall_hall': 0, 'repose_hall': 0, 'chamber': 0, 'niche_ossuary': 0,
           'niche_reliquary': 0, 'niche_shrine': 0, 'niche_wall': 0}
ANCHOR_SPACING = 7
CHAIN_REACH = 9            # the furthest a chain is drawn across a room to its far wall
SPAN_RISE = 3              # a chain is drawn across this many courses over the anchor's floor... plus one: floor + 4

SIDES = ('north', 'south', 'east', 'west')
SET_PIECE_PREFIXES = ('hemomancy:', 'minecraft:jigsaw', 'minecraft:barrel')


# --- the hooks --------------------------------------------------------------------------------------------------------

def chamber_sockets(room, c, sockets):
    """Track 6's dormant anchors either side of the tomb's foot (two of the fight's four, R1) and at the end of each
    aisle; a patient's bed in every aisle alcove; behind her head, in the relic niche, the first anchor they built,
    broken; the low votive niches grown shut."""
    for kind, (x, y, z), facing in sockets:
        if kind in ('dais_flank', 'aisle_end'):
            anchor(room, x, y, z)
        elif kind == 'aisle_alcove':
            alcove_bed(room, c, x, y, z, facing)
        elif kind == 'apse_niche':
            if room.get(x, y + 2, z) == AIR:
                anchor(room, x, y, z, snapped=True)
            else:
                room.set(x, y, z, HEALED_CALCITE)
                room.set(x, y + 1, z, *outgrowth('up'))


def hall_sockets(room, c, sockets):
    """Every trial hall's way in is flanked by a pair of anchors."""
    for kind, (x, y, z), facing in sockets:
        if kind == 'entry_flank':
            anchor(room, x, y, z)


def motif(name, room, c):
    """A piece's own dressing, then the whole-complex passes: lit grilles for wall lights, the anchors and their chains,
    and her growth."""
    dress = DRESSING.get(name)
    if dress:
        dress(room, c)
    grilles(room, c)
    anchors_and_chains(room, c, name)
    level = GROWTH.get(name, 0)
    if level:
        grow(room, c, name, level)


def anchor(room, x, y, z, snapped=False):
    if not kit.two_tall_fits(room, x, y, z):
        raise ValueError('no room for a containment anchor at %s' % ((x, y, z),))
    kit.containment_anchor(room, x, y, z, snapped)


def outgrowth(facing):
    return OUTGROWTH, {'facing': facing, 'waterlogged': 'false'}


def chain(axis):
    return CHAIN, {'axis': axis, 'waterlogged': 'false'}


# --- grilles ----------------------------------------------------------------------------------------------------------

def grilles(room, c):
    """Every wall light becomes a sea lantern recessed behind a diorite grille: the stone the sconce hung on turns to a
    diorite baluster, the lantern is set in the mass behind it, and the sconce goes. A sconce with no mass behind its
    wall (a room's outer skin) keeps its flame."""
    for (x, y, z), (name, props, nbt) in list(room.blocks.items()):
        if name != SCONCE:
            continue
        facing = dict(props)['facing']
        dx, dz = STEP[facing]
        wall = (x - dx, y, z - dz)
        behind = (x - 2 * dx, y, z - 2 * dz)
        if not (_solid(room, *wall) and _solid(room, *behind)):
            continue
        room.set(*wall, GRILLE, kit.post())
        room.set(*behind, LANTERN)
        room.set(x, y, z, AIR)


def _solid(room, x, y, z):
    """Plain masonry: a full block of the room that is not a set piece, a connector or anything with state."""
    entry = room.blocks.get((x, y, z))
    if entry is None:
        return False
    name, props, nbt = entry
    if name == AIR or nbt is not None or name.startswith(SET_PIECE_PREFIXES) and name not in GROWN:
        return False
    return not props and not name.endswith(('_stairs', '_slab', '_wall', '_button', '_glass'))


GROWN = (HEALED, HEALED_CALCITE, RADIANT)


# --- anchors and chains -----------------------------------------------------------------------------------------------

def anchors_and_chains(room, c, name):
    """Stands the piece's share of anchors against its walls (ANCHORS), then chains every anchor in it: a whole anchor
    up to the vault, and across from a shackle four courses over its floor to the far wall; a snapped anchor keeps only a
    torn length hanging from the vault over it."""
    count = ANCHORS.get(name, max(1, room.size[0] // 9))
    rng = random.Random('seraphae/anchors/' + name)
    placed = [pos for pos in _anchors(room)]
    for x, y, z in _anchor_sites(room, c, rng):
        if count <= 0:
            break
        if any((x - px) ** 2 + (z - pz) ** 2 < ANCHOR_SPACING ** 2 and abs(y - py) < 4 for px, py, pz in placed):
            continue
        anchor(room, x, y, z)
        placed.append((x, y, z))
        count -= 1
    for x, y, z in sorted(_anchors(room)):
        if dict(room.blocks[(x, y, z)][1]).get('snapped') == 'true':
            torn_chain(room, x, y, z, rng)
        else:
            tether(room, x, y, z)


def _anchors(room):
    return [pos for pos, (name, props, _) in room.blocks.items()
            if name == ANCHOR and dict(props).get('half') == 'lower']


def _anchor_sites(room, c, rng):
    """Floor cells an anchor can stand in: two tall and the course over it open, on plain paving, its back to a wall
    with open floor along the wall either side and at least three open cells in front, clear of stairs, set pieces and
    connectors."""
    floors = (c['floor'], c['floor_alt'], c['inlay'])
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
        if not all(room.get(x - bx * d, sy + h, z - bz * d) == AIR for d in (1, 2, 3) for h in (0, 1)):
            continue
        ax, az = bz, bx
        if not all(room.get(x + s * ax, sy + h, z + s * az) == AIR for s in (-1, 1) for h in (0, 1)):
            continue
        if _crowded(room, x, sy, z):
            continue
        sites.append((x, sy, z))
    sites.sort()
    rng.shuffle(sites)
    return sites


def _crowded(room, x, y, z):
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            for dy in range(-1, 4):
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
                if name == 'minecraft:jigsaw' or name.startswith('hemomancy:') and name not in GROWN:
                    return True
                if name.endswith('_stairs') and dy <= 1 and dict(props).get('half') == 'bottom':
                    return True         # a flight beside it
    return False


def tether(room, x, y, z):
    """A whole anchor's chains: up from its head to the vault, and from a shackle four courses over its floor straight
    across to the far wall (the side its back is not to), when that wall is in reach and nothing stands in the way."""
    top = y + 2
    ceiling = top
    while room.get(x, ceiling, z) == AIR:
        ceiling += 1
    if room.get(x, ceiling, z) is None or ceiling == top:
        return
    for cy in range(top, ceiling):
        room.set(x, cy, z, *chain('y'))
    jy = y + SPAN_RISE
    if jy >= ceiling:
        return
    backs = [side for side in SIDES if _solid(room, x + STEP[side][0], y, z + STEP[side][1])]
    if len(backs) != 1:
        return
    dx, dz = STEP[OPPOSITE[backs[0]]]
    run = []
    for d in range(1, CHAIN_REACH + 1):
        cell = (x + dx * d, jy, z + dz * d)
        if room.get(*cell) != AIR:
            if run and _solid(room, *cell):
                for pos in run:
                    room.set(*pos, *chain('x' if dx else 'z'))
                room.set(x, jy, z, SHACKLE, kit.post())
            return
        run.append(cell)


def torn_chain(room, x, y, z, rng):
    """A snapped anchor's chain: a torn length hanging from the vault over it, stopping short of its broken head."""
    top = y + 2
    ceiling = top
    while room.get(x, ceiling, z) == AIR:
        ceiling += 1
    if room.get(x, ceiling, z) is None:
        return
    for cy in range(ceiling - 1, max(top + 1, ceiling - 1 - rng.randint(1, 3)) - 1, -1):
        room.set(x, cy, z, *chain('y'))


# --- growth -----------------------------------------------------------------------------------------------------------

def grow(room, c, name, level):
    """Her growth over the piece: healed patches knit across the exposed wall field and vault (quartz bricks heal to
    healed quartz, calcite to healed calcite), their hearts lit from inside from level 3; then bone outgrowths push out
    of the patches into the air beside them, on walls only out of reach over the floor, and down from the vault."""
    rng = random.Random('seraphae/growth/' + name)
    heal = {c['wall']: HEALED, c['vault']: HEALED}
    for block in list(c['wall_alt']) + list(c['vault_alt']):
        heal[block] = HEALED_CALCITE
    faces = sorted(pos for pos, (block, props, nbt) in room.blocks.items()
                   if block in heal and not props and nbt is None and _exposed(room, *pos))
    if not faces:
        return
    patches = max(1, round(len(faces) * PATCHES[level] / 100))
    lo, hi = PATCH_SIZE[level]
    patch_cells = []
    for seed in rng.sample(faces, min(patches, len(faces))):
        if room.get(*seed) not in heal:
            continue
        size = rng.randint(lo, hi)
        cells, queue = [seed], [seed]
        while queue and len(cells) < size:
            cx, cy, cz = queue.pop(rng.randrange(len(queue)))
            for ddx, ddy, ddz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
                n = (cx + ddx, cy + ddy, cz + ddz)
                if n not in cells and room.get(*n) in heal and _exposed(room, *n) and rng.random() < 0.7:
                    cells.append(n)
                    queue.append(n)
                    if len(cells) >= size:
                        break
        glow = rng.random() < GLOW[level]
        for i, cell in enumerate(cells):
            room.set(*cell, RADIANT if glow and i < 2 else heal[room.get(*cell)])
        patch_cells += cells
    if not OUTGROW[level]:
        return
    for (x, y, z) in patch_cells:
        for facing, (dx, dy, dz) in (('north', (0, 0, -1)), ('south', (0, 0, 1)), ('east', (1, 0, 0)),
                                     ('west', (-1, 0, 0)), ('down', (0, -1, 0))):
            cell = (x + dx, y + dy, z + dz)
            if room.get(*cell) != AIR or rng.random() >= OUTGROW[level]:
                continue
            if facing != 'down' and any(room.get(cell[0], y - h, cell[2]) != AIR for h in range(1, OUT_OF_REACH + 1)):
                continue
            if _beside_anything(room, *cell):
                continue
            room.set(*cell, *outgrowth(facing))


def _exposed(room, x, y, z):
    return any(room.get(x + dx, y + dy, z + dz) == AIR
               for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)))


def _beside_anything(room, x, y, z):
    """Whether a cell touches a chain, a set piece, a connector or another outgrowth (so growth never fouls them)."""
    for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
        name = room.get(x + dx, y + dy, z + dz)
        if name and name not in GROWN and (name.startswith('hemomancy:') or name == 'minecraft:jigsaw'
                                           or name in (GRILLE, LANTERN)):
            return True
    return False


# --- beds ---------------------------------------------------------------------------------------------------------------

def bed(room, c, x, y, z, toward, grown=False):
    """A patient's bed carved in quartz: its head at (x, y, z), a stair whose riser stands on the `toward` side as the
    headboard, its foot a slab one cell the other way. A grown bed's foot has healed over into a lump with bone growing
    out of it: the patient she touched."""
    dx, dz = STEP[toward]
    foot = (x - dx, y, z - dz)
    for cell in ((x, y, z), foot):
        if room.get(*cell) != AIR or room.get(cell[0], y - 1, cell[2]) in (None, AIR):
            raise ValueError('no room for a bed at %s' % ((x, y, z),))
    room.set(x, y, z, c['trim_stairs'], stairs(toward))
    if grown:
        room.set(*foot, HEALED_CALCITE)
        if room.get(foot[0], y + 1, foot[2]) == AIR:
            room.set(foot[0], y + 1, foot[2], *outgrowth('up'))
    else:
        room.set(*foot, c['trim_slab'], slab('bottom'))


def alcove_bed(room, c, x, y, z, facing):
    """A bed laid along an aisle alcove (three wide, one deep): its head against the alcove's east jamb."""
    bed(room, c, x + 1, y, z, 'east', grown=(x % 4 == 3))


# --- the pieces -------------------------------------------------------------------------------------------------------

def dress_beholding(room, c):
    """The Ward Walk: each witness stone is the head of a patient's bed laid toward the walk; the two nearer the focus
    have grown."""
    for (x, y, z), (name, props, nbt) in list(room.blocks.items()):
        if name == 'hemomancy:witness_stone':
            toward = dict(props)['facing']
            dx, dz = STEP[toward]
            bed(room, c, x + dx, y, z + dz, OPPOSITE[toward], grown=x > room.size[0] // 2)


# The Infirmary Row (the shared Downfall hall, 25 x 11, centre line z 5, walls at z 1 and 9): beds head to the wall
# down both sides between the seals, and broken anchors along the way.
ROW_BEDS = (5, 10, 16, 22)
ROW_BROKEN = ((11, 1), (15, -1), (20, 1))      # (x, side): the side of the centre line, against the wall


def dress_downfall(room, c):
    zc = room.size[2] // 2
    for i, x in enumerate(ROW_BEDS):
        for side, toward in ((-1, 'north'), (1, 'south')):
            for cell in ((x, 1, zc + side * 3), (x, 1, zc + side * 2)):
                if room.get(*cell) != AIR and room.get(*cell).endswith('_stairs'):
                    room.set(*cell, AIR)     # a toppled stone where the bed stands
            bed(room, c, x, 1, zc + side * 3, toward, grown=(i + side) % 3 == 0)
    for x, side in ROW_BROKEN:
        anchor(room, x, 1, zc + side * 3, snapped=True)


# The Still Ward: a round room 15 x 13 under a stepped dome, its four pylons anchors standing against the wall on the
# diagonals, the still circle a calcite ring in the floor. Same length, width and exit floor as the shared Repose, so
# its focus room continues the run.
STILL_L, STILL_H, STILL_W = 15, 14, 13
STILL_R = 5.5                  # the open floor's radius
STILL_SPRING = 7               # the dome springs over the seventh course
STILL_PYLONS = ((3, -3), (11, -3), (3, 3), (11, 3))


def still_ward(saint):
    from .. import pieces, trials
    c = CRYPT
    rng = random.Random('still_ward/' + saint)
    length, height, width = STILL_L, STILL_H, STILL_W
    zc = width // 2
    cx = length // 2
    room = kit.Room(length, height, width)
    kit.mass(room, c, rng, 0, length - 1, 0, height - 1, 0, width - 1)

    def d(x, z):
        return math.hypot(x - cx, z - zc)

    # The drum: open floor inside the circle, a base course, a dado and a cornice round the wall.
    for x in range(length):
        for z in range(width):
            r = d(x, z)
            if r <= STILL_R:
                room.set(x, 0, z, c['floor_alt'] if rng.random() < 0.1 else c['floor'])
                for y in range(1, STILL_SPRING + 1):
                    room.set(x, y, z, AIR)
            elif r <= STILL_R + 1.2:
                room.set(x, 1, z, c['base'])
                room.set(x, 3, z, c['trim'])
                room.set(x, STILL_SPRING, z, c['trim'])
    # The dome: each course draws in a block, ringed with upside-down stairs whose backs are to the wall.
    for k in range(6):
        y = STILL_SPRING + 1 + k
        inner = STILL_R - 1 - k
        for x in range(length):
            for z in range(width):
                r = d(x, z)
                if r <= inner:
                    room.set(x, y, z, AIR)
                elif r <= inner + 1:
                    ox, oz = x - cx, z - zc
                    facing = ('east' if ox > 0 else 'west') if abs(ox) >= abs(oz) else ('south' if oz > 0 else 'north')
                    room.set(x, y, z, c['vault_stairs'], stairs(facing, 'top'))
    room.set(cx, height - 1, zc, c['light'])
    # Pilasters round the drum between the pylons, each with a rib up into the dome's first course.
    for angle in range(0, 360, 45):
        a = math.radians(angle + 22.5)
        x, z = cx + round((STILL_R + 0.7) * math.cos(a)), zc + round((STILL_R + 0.7) * math.sin(a))
        if room.get(x, 2, z) not in (None, AIR) and d(x, z) > STILL_R:
            for y in range(2, STILL_SPRING):
                room.set(x, y, z, c['trim'])
            room.set(x, STILL_SPRING, z, c['accent'])
    # The still circle: a calcite ring on the diorite floor inside a smooth-quartz border, a chiseled stone at its
    # heart.
    for x in range(length):
        for z in range(width):
            if d(x, z) <= trials.REPOSE_RADIUS + 0.5:
                room.set(x, 0, z, c['inlay'])
    for x in range(length):
        for z in range(width):
            if d(x, z) < trials.REPOSE_RADIUS - 0.5:
                room.set(x, 0, z, c['floor'])
    for x, z in kit.ring(cx, zc, trials.REPOSE_RADIUS):
        room.set(x, 0, z, 'minecraft:calcite')
    room.set(cx, 0, zc, c['accent'])
    # The way in (five wide through the drum wall, under an arch) and the way on (the same, under a keystone).
    for x in range(1, 3):
        for z in range(zc - 2, zc + 3):
            if d(x, z) > STILL_R:
                room.set(x, 0, z, kit.paved(c, rng, abs(z - zc), 2))
                for y in range(1, 5):
                    room.set(x, y, z, AIR)
    kit.arch(room, c, 0, 0, zc)
    for z in (zc - 1, zc, zc + 1):
        room.set(0, 0, z, c['runner'])
    for x in range(length - 3, length):
        for z in range(zc - 2, zc + 3):
            if d(x, z) > STILL_R:
                room.set(x, 0, z, kit.paved(c, rng, abs(z - zc), 2))
                for y in range(1, 5):
                    room.set(x, y, z, AIR)
    x = length - 1
    for z in range(zc - 3, zc + 4):
        room.set(x, 5, z, c['trim'])
    for z, facing in ((zc - 2, 'north'), (zc + 2, 'south')):
        room.set(x, 4, z, c['trim_stairs'], stairs(facing, 'top'))
    room.set(x, 5, zc, c['accent'])
    # The pylons, cut as anchors, against the drum on the diagonals; the trial's marks.
    marks = [trials.Mark(cx, 1, zc, 'CIRCLE', 0)]
    for i, (px, dz) in enumerate(STILL_PYLONS):
        mark = trials.Mark(px, 3, zc + dz, 'PYLON', i)
        trials.pylon(room, c, mark, 'south' if dz < 0 else 'north', style='anchor')
        marks.append(mark)
    # Sconces between the pylons, at the ends of the cross axis.
    for z, facing in ((zc - 6, 'south'), (zc + 6, 'north')):
        kit.wall_light(room, c, cx, 4, z, facing)
    pieces.hall_connectors(room, c, saint, 'REPOSE', zc, marks)
    return room, marks


HALLS = {'REPOSE': still_ward}


def dress_still_ward(room, c):
    """The pylons are anchors: chain each one's head up to the dome."""
    for (x, y, z), (name, props, nbt) in list(room.blocks.items()):
        if name == 'hemomancy:pulse_pylon' and dict(props)['part'] == 'upper':
            cy = y + 1
            while room.get(x, cy, z) == AIR:
                room.set(x, cy, z, *chain('y'))
                cy += 1


# --- the chamber: the infirmary -------------------------------------------------------------------------------------

CONCH_ANCHORS = ((33, -4), (33, 4))      # the fight's other two anchors (R1), in the conch behind the tomb
KNOT_Y = 9                               # where the chains meet, under the apse vault's springing
HUB_X = 31                               # the largest anchor hangs over the tomb's head end


def dress_chamber(room, c):
    """The tomb under the largest anchor, hung from the apse crown, where the chains of the four anchors round the tomb
    meet; the rank pillars swollen at mid-height with bone climbing them; the conch's two anchors."""
    from .. import pieces
    zc = pieces.CHAMBER_ZC
    top = pieces.CHAMBER_DAIS + 1
    for x, dz in CONCH_ANCHORS:
        anchor(room, x, top, zc + dz)
    flanks = [(x, y, z) for kind, (x, y, z), _ in pieces.CHAMBER_SOCKETS if kind == 'dais_flank']
    # The largest anchor: a chain from a collar in the crown, a collar head flared with iron, a quartz shaft, and the
    # knot at its foot where the chains meet.
    crown = pieces.CHAMBER_H - 1
    room.set(HUB_X, crown, zc, COLLAR)
    for y in range(KNOT_Y + 4, crown):
        room.set(HUB_X, y, zc, *chain('y'))
    room.set(HUB_X, KNOT_Y + 3, zc, COLLAR)
    for side, (dx, dz) in STEP.items():
        room.set(HUB_X + dx, KNOT_Y + 3, zc + dz, IRON_STAIRS, stairs(OPPOSITE[side], 'top'))
    for y in (KNOT_Y + 1, KNOT_Y + 2):
        room.set(HUB_X, y, zc, 'minecraft:quartz_pillar', {'axis': 'y'})
    room.set(HUB_X, KNOT_Y, zc, c['accent'])
    # Each side's pair of anchors: up from its head, across to the centre line at the knot's course, then along it.
    for ax, pairs in ((flanks[0][0], [(y, z) for _, y, z in flanks]),
                      (CONCH_ANCHORS[0][0], [(top, zc + dz) for _, dz in CONCH_ANCHORS])):
        for ay, az in pairs:
            for y in range(ay + 2, KNOT_Y):
                room.set(ax, y, az, *chain('y'))
            room.set(ax, KNOT_Y, az, SHACKLE, kit.post())
            step = 1 if az < zc else -1
            for z in range(az + step, zc, step):
                room.set(ax, KNOT_Y, z, *chain('z'))
        room.set(ax, KNOT_Y, zc, SHACKLE, kit.post())
        step = 1 if ax < HUB_X else -1
        for x in range(ax + step, HUB_X, step):
            room.set(x, KNOT_Y, zc, *chain('x'))
    # The rank pillars swell at mid-height; bone climbs their faces toward the hall over the swelling.
    for x in pieces.CHAMBER_PILLARS:
        for side in (-1, 1):
            z = zc + side * pieces.CHAMBER_RANK
            kit.swollen_pillar(room, x, z, 1, 5, HEALED_CALCITE, c['vault_stairs'],
                               sides=('east', 'west', 'north' if side < 0 else 'south'))
            inward = 'south' if side < 0 else 'north'
            for y in (6, 7):
                cell = (x, y, z - side)
                if room.get(*cell) == AIR and (x + y) % 2 == 0:
                    room.set(*cell, *outgrowth(inward))


def dress_nave(room, c):
    """The nave's arcade piers have swollen at mid-height."""
    from .. import pieces
    for p in pieces.NAVE_PIERS:
        for z in (pieces.NAVE_ZC - 5, pieces.NAVE_ZC + 5):
            if room.get(p, 2, z) == c['trim']:
                kit.swollen_pillar(room, p, z, 1, pieces.NAVE_GALLERY - 1, HEALED_CALCITE, c['vault_stairs'])


DRESSING = {'beholding_hall': dress_beholding, 'downfall_hall': dress_downfall, 'repose_hall': dress_still_ward,
            'chamber': dress_chamber, 'nave': dress_nave}

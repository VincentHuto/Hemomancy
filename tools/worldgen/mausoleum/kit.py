"""The mausoleum kit (Track 7): the Room canvas every template is painted on, the Theme a Saint paints it with, and the
architectural modules that build a room from a vocabulary (base course, dado, pilasters, cornice, vaults, arches,
pillars, niches, daises, floor inlays, sconces) instead of a flat single-block shell. Theme-agnostic: every module paints
with a Theme's roles; themes/<saint>.py supplies the block family.

Conventions. A piece is authored with its entry in the west wall (x=0) and its way on through the east wall; side
branches open in the north (z=0) or south wall. Rooms are carved from solid mass: paint the mass, carve the air, then
dress the faces. A run along x is described by its x span, its centre line z, its half width (air either side of the
centre) and its floor y. Every room ends with finish(), which breaks any flat surface larger than 3 x 3 that the
modules left (checks.py is the acceptance test build.py runs on every template).

Jigsaw pieces are placed with a known shape (vanilla never recomputes a stair's shape or a wall's sides), so every
state the kit writes carries every property its block has: stairs facing/half/shape/waterlogged, slabs type, walls the
four sides, up and waterlogged.

Preview: python tools/worldgen/mausoleum/kit_preview.py [saint] renders one test room per module to
build/mausoleum-kit/<saint>/<module>.png.
"""
import os
import random

import nbtlib
from nbtlib import Compound, List, String, Int, Byte

DATA_VERSION = 3955
AIR = 'minecraft:air'


class Room:
    """A generated template: blocks keyed by position, then saved as a structure NBT."""

    def __init__(self, sx, sy, sz):
        self.size = (sx, sy, sz)
        self.blocks = {}
        # Walls finish() joins to what stands beside them once the theme has dressed the room (connect_walls).
        self.joined = set()

    def inside(self, x, y, z):
        return 0 <= x < self.size[0] and 0 <= y < self.size[1] and 0 <= z < self.size[2]

    def set(self, x, y, z, name, props=None, nbt=None):
        if not self.inside(x, y, z):
            raise ValueError('(%d,%d,%d) outside %s' % (x, y, z, self.size))
        self.blocks[(x, y, z)] = (name, tuple(sorted((props or {}).items())), nbt)

    def get(self, x, y, z):
        entry = self.blocks.get((x, y, z))
        return entry[0] if entry else None

    def unset(self, x, y, z):
        """Leaves a cell out of the template (structure void): whatever the world has there stays."""
        self.blocks.pop((x, y, z), None)

    def fill(self, x0, x1, y0, y1, z0, z1, block):
        """Fills an inclusive box; block is a name, a (name, props) pair, or a callable (x, y, z) -> either."""
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    value = block(x, y, z) if callable(block) else block
                    if isinstance(value, tuple):
                        self.set(x, y, z, *value)
                    else:
                        self.set(x, y, z, value)

    def carve(self, x0, x1, y0, y1, z0, z1):
        self.fill(x0, x1, y0, y1, z0, z1, AIR)

    def jigsaw(self, x, y, z, orientation, name, target, pool, final='minecraft:air', priority=0, joint='rollable'):
        """A connector. Mandatory continuations carry priority 1 so their branch claims its space before an optional
        niche can (placement_priority orders the expansion queue, selection_priority this piece's own connectors)."""
        self.set(x, y, z, 'minecraft:jigsaw', {'orientation': orientation}, Compound({
            'id': String('minecraft:jigsaw'), 'name': String(name), 'target': String(target), 'pool': String(pool),
            'joint': String(joint), 'final_state': String(final), 'placement_priority': Int(priority),
            'selection_priority': Int(priority)}))

    def save(self, path):
        palette, index = [], {}
        blocks = List[Compound]()
        for (x, y, z), (name, props, nbt) in sorted(self.blocks.items()):
            key = (name, props)
            if key not in index:
                index[key] = len(palette)
                state = Compound({'Name': String(name)})
                if props:
                    state['Properties'] = Compound({k: String(v) for k, v in props})
                palette.append(state)
            entry = Compound({'pos': List[Int]([Int(x), Int(y), Int(z)]), 'state': Int(index[key])})
            if nbt is not None:
                entry['nbt'] = nbt
            blocks.append(entry)
        root = Compound({
            'DataVersion': Int(DATA_VERSION),
            'size': List[Int]([Int(v) for v in self.size]),
            'palette': List[Compound](palette),
            'blocks': blocks,
        })
        os.makedirs(os.path.dirname(path), exist_ok=True)
        nbtlib.File(root).save(path, gzipped=True)


# --- the theme ------------------------------------------------------------------------------------------------------

class Theme(dict):
    """A Saint's block family by role. Read as a dict (c['wall']) so modules and theme hooks stay terse.

    Roles: base (base course, plinths, stair cores), wall + wall_alt (the wall field and its speckle), trim + trim_stairs
    + trim_slab + trim_wall (pilasters, cornices, arch stones, dado, balustrades), accent (capitals, keystones, inlay
    crosses), floor + floor_alt + floor_slab, inlay (borders, rings), runner (the walk's centre line), seal (loculus
    seals), vault + vault_alt + vault_stairs (ceilings), light (crown and boss lamps; walls take sconces), button, track
    (the ring door's floor course).

    `variants` maps a block to the alternates finish() may swap in to break a flat surface; wall, floor and vault get
    their _alt roles automatically."""
    ROLES = ('base', 'wall', 'wall_alt', 'trim', 'trim_stairs', 'trim_slab', 'trim_wall', 'accent', 'floor',
             'floor_alt', 'floor_slab', 'inlay', 'runner', 'seal', 'vault', 'vault_alt', 'vault_stairs', 'button',
             'light', 'track')
    FAMILIES = {'trim_stairs': '_stairs', 'vault_stairs': '_stairs', 'trim_slab': '_slab', 'floor_slab': '_slab',
                'trim_wall': '_wall', 'button': '_button'}

    def __init__(self, roles, variants=None):
        missing = [role for role in self.ROLES if role not in roles]
        if missing:
            raise ValueError('a theme needs every role; missing %s' % ', '.join(missing))
        for role, suffix in self.FAMILIES.items():
            if not roles[role].endswith(suffix):
                raise ValueError('%s must be a %s block, not %s' % (role, suffix[1:], roles[role]))
        super().__init__(roles)
        self.variants = {}
        for block, alternates in ((roles['wall'], roles['wall_alt']), (roles['floor'], [roles['floor_alt']]),
                                  (roles['vault'], roles['vault_alt'])):
            self.variants.setdefault(block, [])
            self.variants[block] += [a for a in alternates if a != block and a not in self.variants[block]]
        for block, alternates in (variants or {}).items():
            self.variants.setdefault(block, [])
            self.variants[block] += [a for a in alternates if a not in self.variants[block]]


# --- block states -------------------------------------------------------------------------------------------------

LEFT_OF = {'north': 'west', 'east': 'north', 'south': 'east', 'west': 'south'}
OPPOSITE = {'north': 'south', 'south': 'north', 'east': 'west', 'west': 'east'}
STEP = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}


def stairs(facing, half='bottom', shape='straight'):
    return {'facing': facing, 'half': half, 'shape': shape, 'waterlogged': 'false'}


def corner_stairs(facing, other, half='bottom', inner=True):
    """A stairs at the corner of a ring: `facing` on one side, `other` on the side it turns onto. Inner hugs both (three
    quarters, a vault's corner), outer is the quarter between them (a step's or a roof's corner)."""
    side = 'left' if LEFT_OF[facing] == other else 'right'
    return stairs(facing, half, ('inner_' if inner else 'outer_') + side)


def slab(kind='bottom'):
    return {'type': kind, 'waterlogged': 'false'}


def post():
    """A free-standing wall post (balustrade baluster)."""
    return {'up': 'true', 'north': 'none', 'south': 'none', 'east': 'none', 'west': 'none', 'waterlogged': 'false'}


def rail(axis, up=False):
    """A wall section running along axis ('x' or 'z'), low, so a row of them reads as a balustrade."""
    sides = ('east', 'west') if axis == 'x' else ('north', 'south')
    props = {'up': 'true' if up else 'false', 'north': 'none', 'south': 'none', 'east': 'none', 'west': 'none',
             'waterlogged': 'false'}
    for side in sides:
        props[side] = 'low'
    return props


def wall_end(axis, toward):
    """A rail's last post, joined only toward `toward` (one of the two sides along axis)."""
    props = post()
    props[toward] = 'low'
    return props


# What a wall joins besides another wall: a neighbour whose touching face is a full square. Names that are not whole
# cubes (or carry a model) never qualify; stairs qualify by their back.
NOT_SOLID = ('_stairs', '_slab', '_wall', '_button', '_pressure_plate', '_door', '_trapdoor', '_fence', '_bars',
             '_pane', '_lantern', '_carpet', '_chain', 'minecraft:chain', 'minecraft:jigsaw', AIR, '_glass',
             'minecraft:hanging_roots', 'minecraft:mangrove_roots', 'minecraft:barrel')


def _wall_name(name):
    return name is not None and name.endswith('_wall')


def _solid_face(room, x, y, z, toward):
    """Whether the block at (x, y, z) shows a full face toward `toward` (the side facing the wall)."""
    entry = room.blocks.get((x, y, z))
    if entry is None:
        return False
    name, props, _ = entry
    props = dict(props)
    if name.endswith('_stairs'):
        return props.get('shape') in ('straight', 'inner_left', 'inner_right') and props.get('facing') == toward
    if name.endswith('_slab'):
        return props.get('type') == 'double'
    if name.endswith(NOT_SOLID) or name in NOT_SOLID or name.startswith('hemomancy:') and props.keys() - {'axis'}:
        return False
    from . import checks
    return name not in checks.SET_PIECES


def _covers_side(room, x, y, z, side):
    """Whether the block over a wall at (x, y - 1, z) covers that wall's arm toward `side` (or, for side None, its
    post), so the arm stands tall (or the post must rise): vanilla's test on the block above's lower face."""
    entry = room.blocks.get((x, y, z))
    if entry is None:
        return False
    name, props, _ = entry
    props = dict(props)
    if _wall_name(name):
        return props.get('up') == 'true' if side is None else props.get(side, 'none') != 'none'
    if name.endswith('_stairs'):
        return props.get('half') == 'bottom'
    if name.endswith('_slab'):
        return props.get('type') in ('bottom', 'double')
    if name in ('minecraft:chain', 'hemomancy:hematic_iron_chain'):
        return side is None and props.get('axis') == 'y'
    return _solid_face(room, x, y, z, 'down')


def connect_walls(room, cells):
    """Gives each wall at `cells` the state vanilla's WallBlock would: an arm toward every wall or full face beside it,
    tall where the block above covers it, and a post where the run ends, turns, or something above needs one. Jigsaw
    pieces keep the template's state until a neighbour changes, so a balustrade that butts a pier or a wall must be
    saved joined to it."""
    for (x, y, z) in cells:
        name, props, nbt = room.blocks[(x, y, z)]
        state = dict(props)
        for side, (dx, dz) in STEP.items():
            other = room.get(x + dx, y, z + dz)
            joined = _wall_name(other) or _solid_face(room, x + dx, y, z + dz, OPPOSITE[side])
            state[side] = ('tall' if _covers_side(room, x, y + 1, z, side) else 'low') if joined else 'none'
        n, s, e, w = (state[side] == 'none' for side in ('north', 'south', 'east', 'west'))
        straight_tall = state['north'] == state['south'] == 'tall' or state['east'] == state['west'] == 'tall'
        above = room.blocks.get((x, y + 1, z))
        if above and _wall_name(above[0]) and dict(above[1]).get('up') == 'true':
            up = True
        elif (n and s and e and w) or n != s or e != w:
            up = True
        elif straight_tall:
            up = False
        else:
            up = _covers_side(room, x, y + 1, z, None)
        state['up'] = 'true' if up else 'false'
        room.set(x, y, z, name, state, nbt)


def bone(axis):
    return 'minecraft:bone_block', {'axis': axis}


def field(rng, c, chance=0.14):
    """The wall field, speckled with the palette's variants so no face is one flat block."""
    return c['wall_alt'][int(rng.random() * len(c['wall_alt']))] if rng.random() < chance else c['wall']


def barrel(room, x, y, z, facing, loot=None, items=None):
    nbt = Compound({'id': String('minecraft:barrel')})
    if loot:
        nbt['LootTable'] = String(loot)
    if items:
        nbt['Items'] = List[Compound]([Compound({'Slot': Byte(slot), 'id': String(item), 'count': Int(count)})
                                       for slot, (item, count) in enumerate(items)])
    room.set(x, y, z, 'minecraft:barrel', {'facing': facing, 'open': 'false'}, nbt)


def inscription(room, x, y, z, block, facing, inscription_id):
    room.set(x, y, z, block, {'facing': facing, 'waterlogged': 'false'}, Compound({
        'id': String('hemomancy:discovery_inscription'), 'InscriptionId': String(inscription_id)}))


# --- mass, floors and speckle ---------------------------------------------------------------------------------------

def mass(room, c, rng, x0, x1, y0, y1, z0, z1):
    """Solid masonry: a floor course at y0, the speckled wall field, a vault course at y1."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            room.set(x, y0, z, c['floor'])
            for y in range(y0 + 1, y1):
                room.set(x, y, z, field(rng, c))
            room.set(x, y1, z, c['vault'])


def paved(c, rng, d, half, alt=0.12):
    """The floor block a distance d from a run's centre line: runner down the middle, inlay border at the walls."""
    if d == 0:
        return c['runner']
    if d == half:
        return c['inlay']
    return c['floor_alt'] if rng.random() < alt else c['floor']


def speckle(room, c, rng, x0, x1, y0, y1, z0, z1, chance=0.14):
    """Swaps a fraction of the box's blocks that have variants (the wall field, floor, vault) for one of them."""
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            for z in range(z0, z1 + 1):
                entry = room.blocks.get((x, y, z))
                if entry and entry[2] is None and not entry[1] and entry[0] in c.variants and rng.random() < chance:
                    room.set(x, y, z, rng.choice(c.variants[entry[0]]))


def floor_grid(room, c, rng, x0, x1, z0, z1, y, bands=4, phase=0):
    """A large floor laid in bays: inlay bands across it every `bands` blocks (from x0 + phase) framing fields of
    floor with a cross of floor_alt at each bay's heart."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if (x - x0 - phase) % bands == 0:
                room.set(x, y, z, c['inlay'])
            elif room.get(x, y, z) in (c['floor'], c['floor_alt'], None):
                room.set(x, y, z, c['floor_alt'] if rng.random() < 0.12 else c['floor'])


# --- vaults ---------------------------------------------------------------------------------------------------------

def vault_x(room, c, x, zc, half, spring):
    """A stair-stepped barrel vault across a run along x, from y=spring: each course draws the sides in by one with
    upside-down stairs until the crown. Returns the crown's y (the block above the last open course)."""
    y = spring
    reach = half
    while reach > 0:
        room.set(x, y, zc - reach, c['vault_stairs'], stairs('north', 'top'))
        room.set(x, y, zc + reach, c['vault_stairs'], stairs('south', 'top'))
        for z in range(zc - reach + 1, zc + reach):
            room.set(x, y, z, AIR)
        reach -= 1
        y += 1
    room.set(x, y, zc, c['vault'])
    return y


def vault_z(room, c, z, xc, half, spring):
    """vault_x for a run along z."""
    y = spring
    reach = half
    while reach > 0:
        room.set(xc - reach, y, z, c['vault_stairs'], stairs('west', 'top'))
        room.set(xc + reach, y, z, c['vault_stairs'], stairs('east', 'top'))
        for x in range(xc - reach + 1, xc + reach):
            room.set(x, y, z, AIR)
        reach -= 1
        y += 1
    room.set(xc, y, z, c['vault'])
    return y


def rib_x(room, c, x, zc, half, spring):
    """A transverse rib over a pilaster pair: trim stairs on the vault's steps and an accent at the crown."""
    y = spring
    reach = half
    while reach > 0:
        room.set(x, y, zc - reach, c['trim_stairs'], stairs('north', 'top'))
        room.set(x, y, zc + reach, c['trim_stairs'], stairs('south', 'top'))
        reach -= 1
        y += 1
    room.set(x, y, zc, c['accent'])


def ring_stairs(room, block, x0, x1, z0, z1, y, outward, half):
    """A rectangular ring of stairs on the box's edge, each side facing out of the box (outward) or into it, with the
    corners turned (inner corners for a ring facing out, outer for one facing in)."""
    sides = {'north': z0, 'south': z1, 'west': x0, 'east': x1}
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            on_z = 'north' if z == z0 else 'south' if z == z1 else None
            on_x = 'west' if x == x0 else 'east' if x == x1 else None
            if not on_z and not on_x:
                continue
            fz = on_z if outward or not on_z else OPPOSITE[on_z]
            fx = on_x if outward or not on_x else OPPOSITE[on_x]
            if on_z and on_x and x0 != x1 and z0 != z1:
                room.set(x, y, z, block, corner_stairs(fz, fx, half, inner=outward))
            else:
                room.set(x, y, z, block, stairs(fz if on_z and z0 != z1 else fx, half))
    return sides


def vault_ceiling(room, c, x0, x1, z0, z1, spring, kind='cross', axis='x', keystone=True):
    """A stepped vault over the box (x0..x1, z0..z1 are the room's open cells), springing at y=spring. A barrel draws
    in across `axis` only; a cross (a stepped dome) draws in from all four sides, its corners turned. Solid vault fills
    behind every step. The crown is a keystone row (or boss) in accent when `keystone`. Returns the crown's y."""
    if kind == 'barrel':
        if axis == 'x':
            zc, half = (z0 + z1) // 2, (z1 - z0) // 2
            crown = None
            for x in range(x0, x1 + 1):
                crown = vault_x(room, c, x, zc, half, spring)
                if keystone:
                    room.set(x, crown, zc, c['accent'] if (x - x0) % 2 == 0 else c['vault'])
            return crown
        xc, half = (x0 + x1) // 2, (x1 - x0) // 2
        crown = None
        for z in range(z0, z1 + 1):
            crown = vault_z(room, c, z, xc, half, spring)
            if keystone:
                room.set(xc, crown, z, c['accent'] if (z - z0) % 2 == 0 else c['vault'])
        return crown
    y = spring
    ax0, ax1, az0, az1 = x0, x1, z0, z1
    while ax1 - ax0 >= 2 and az1 - az0 >= 2:
        ring_stairs(room, c['vault_stairs'], ax0, ax1, az0, az1, y, outward=True, half='top')
        for x in range(ax0 + 1, ax1):
            for z in range(az0 + 1, az1):
                room.set(x, y, z, AIR)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if not (ax0 <= x <= ax1 and az0 <= z <= az1):
                    room.set(x, y, z, c['vault'])
        ax0, ax1, az0, az1 = ax0 + 1, ax1 - 1, az0 + 1, az1 - 1
        y += 1
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            inner = ax0 <= x <= ax1 and az0 <= z <= az1
            room.set(x, y, z, (c['accent'] if keystone else c['vault']) if inner else c['vault'])
    return y


# --- doorways -------------------------------------------------------------------------------------------------------

def _voussoir(room, c, x, y, z):
    """Dresses a wall-field cell over an arch's haunch as an arch stone; leaves anything else (a loculus, a lamp)."""
    if room.get(x, y, z) in [c['wall']] + list(c['wall_alt']):
        room.set(x, y, z, c['trim'])


def arch(room, c, x, floor, z_centre, axis_faces=('north', 'south')):
    """A 3-wide, 3-tall doorway in an end wall at x (the opening runs along x): trim jambs, stepped haunches, an
    archivolt of trim over them and a keystone. Every doorway in the complex goes through arch or arch_z."""
    for y in range(floor + 1, floor + 4):
        for z in (z_centre - 1, z_centre, z_centre + 1):
            room.set(x, y, z, AIR)
        room.set(x, y, z_centre - 2, c['trim'])
        room.set(x, y, z_centre + 2, c['trim'])
    room.set(x, floor + 4, z_centre - 1, c['trim_stairs'], stairs(axis_faces[0], 'top'))
    room.set(x, floor + 4, z_centre, AIR)
    room.set(x, floor + 4, z_centre + 1, c['trim_stairs'], stairs(axis_faces[1], 'top'))
    room.set(x, floor + 5, z_centre, c['accent'])
    if room.inside(x, floor + 5, z_centre + 1):
        _voussoir(room, c, x, floor + 4, z_centre - 2)
        _voussoir(room, c, x, floor + 4, z_centre + 2)
        _voussoir(room, c, x, floor + 5, z_centre - 1)
        _voussoir(room, c, x, floor + 5, z_centre + 1)


def arch_z(room, c, z, floor, x_centre):
    """The same doorway in a side wall at z (the opening runs along z)."""
    for y in range(floor + 1, floor + 4):
        for x in (x_centre - 1, x_centre, x_centre + 1):
            room.set(x, y, z, AIR)
        room.set(x_centre - 2, y, z, c['trim'])
        room.set(x_centre + 2, y, z, c['trim'])
    room.set(x_centre - 1, floor + 4, z, c['trim_stairs'], stairs('west', 'top'))
    room.set(x_centre, floor + 4, z, AIR)
    room.set(x_centre + 1, floor + 4, z, c['trim_stairs'], stairs('east', 'top'))
    room.set(x_centre, floor + 5, z, c['accent'])
    if room.inside(x_centre + 1, floor + 5, z):
        _voussoir(room, c, x_centre - 2, floor + 4, z)
        _voussoir(room, c, x_centre + 2, floor + 4, z)
        _voussoir(room, c, x_centre - 1, floor + 5, z)
        _voussoir(room, c, x_centre + 1, floor + 5, z)


def threshold_floor(room, c, x, floor, z_centre, axis='x'):
    """The runner across a doorway's sill."""
    for d in (-1, 0, 1):
        if axis == 'x':
            room.set(x, floor, z_centre + d, c['runner'])
        else:
            room.set(z_centre + d, floor, x, c['runner'])


# --- runs: corridors and halls --------------------------------------------------------------------------------------

def corridor(room, c, rng, x0, x1, zc, half, floor, clear, pilasters=4, lights=0, vault=True, phase=0, sconces=True,
             recess=False):
    """A corridor or hall along x: carved air (half either side of zc, clear tall), a paved floor, and on both walls
    a base course, a dado band (when clear >= 5), the speckled field and a cornice under the ceiling; pilasters every
    `pilasters` blocks with accent capitals; a stair-stepped vault from the cornice up with a rib over every pilaster
    pair (when vault); sconces on alternating pilasters (hung in front of the pilaster, or set into the wall beside it
    when `recess`, for a run whose air must stay clear); crown lights every `lights` blocks. The walls are the planes
    zc +- (half + 1). Returns the cornice's y."""
    top = floor + clear
    dado = floor + 2 if clear >= 5 else None
    for x in range(x0, x1 + 1):
        for z in range(zc - half, zc + half + 1):
            room.set(x, floor, z, paved(c, rng, abs(z - zc), half))
            for y in range(floor + 1, top + 1):
                room.set(x, y, z, AIR)
        for plane in (zc - half - 1, zc + half + 1):
            room.set(x, floor + 1, plane, c['base'])
            if dado:
                room.set(x, dado, plane, c['trim'])
            room.set(x, top, plane, c['trim'])
        if vault:
            crown = vault_x(room, c, x, zc, half, top + 1)
            if lights and (x - x0 - lights // 2 - phase) % lights == 0:
                room.set(x, crown, zc, c['light'])
    if pilasters:
        for i, x in enumerate(range(x0 + 1 + phase, x1, pilasters)):
            for plane in (zc - half - 1, zc + half + 1):
                room.set(x, floor + 1, plane, c['base'])
                for y in range(floor + 2, top):
                    room.set(x, y, plane, c['trim'])
                room.set(x, top, plane, c['accent'])
            if vault:
                rib_x(room, c, x, zc, half, top + 1)
            if sconces and i % 2 == 1:
                pilaster_sconces(room, c, x, zc, half, floor + 3, recess)
    return top


def pilaster_sconces(room, c, x, zc, half, y, recess=False):
    """A sconce at each wall of a pilaster pair: hung in the air cell in front of it, or (recess) set into the wall
    cell beside it with the mass behind as its back. Skipped where something already stands."""
    for plane, facing in ((zc - half - 1, 'south'), (zc + half + 1, 'north')):
        dz = STEP[facing][1]
        if recess:
            for cx in (x + 1, x - 1):
                back = (cx, y, plane - dz)
                if room.get(cx, y, plane) in [c['wall']] + list(c['wall_alt']) and room.get(*back) not in (None, AIR):
                    sconce(room, cx, y, plane, facing)
                    break
        elif room.get(x, y, plane + dz) == AIR and room.get(x, y, plane) not in (None, AIR):
            sconce(room, x, y, plane + dz, facing)


run_x = corridor


def hall(room, c, rng, x0, x1, zc, half, floor, clear, rows=3, spacing=4, phase=2, skip=(), pilasters=4, lights=0,
         vault=True, sconces=True):
    """A hall along x: the corridor's walls and vault, a double row of free-standing pillars `rows` off the centre line
    every `spacing` blocks (a base, a trim shaft, an accent capital under a trim impost), an inlay border round the
    floor, and a gallery ledge on corbels at mid-height when it is 9 or more clear. `skip` lists pillar x's to leave out
    (a mark, a socket). Returns the cornice's y."""
    top = corridor(room, c, rng, x0, x1, zc, half, floor, clear, pilasters, lights, vault, sconces=sconces)
    for x in range(x0, x1 + 1):
        for d in (-half, half):
            room.set(x, floor, zc + d, c['inlay'])
    for x in range(x0 + phase, x1, spacing):
        if x in skip:
            continue
        for side in (-1, 1):
            z = zc + side * rows
            room.set(x, floor, z, c['inlay'])
            room.set(x, floor + 1, z, c['base'])
            for y in range(floor + 2, top - 1):
                room.set(x, y, z, c['trim'])
            room.set(x, top - 1, z, c['accent'])
            room.set(x, top, z, c['trim'])
    if clear >= 9:
        ledge = floor + clear // 2
        for x in range(x0, x1 + 1):
            for plane, facing in ((zc - half, 'north'), (zc + half, 'south')):
                if room.get(x, ledge, plane) == AIR and room.get(x, ledge + 1, plane) == AIR:
                    room.set(x, ledge - 1, plane, c['trim_stairs'], stairs(facing, 'top'))
                    room.set(x, ledge, plane, c['trim_slab'], slab('bottom'))
    return top


# --- stairs, niches, daises -----------------------------------------------------------------------------------------

def stair_flight(room, c, x0, x1, zc, half, high_floor, facing='west', cap=None):
    """A straight flight along x dropping one per block from high_floor (the floor course at x0 - 1) toward +x, with
    `facing` its high side; solid base under every step. Returns the floor course at x1."""
    floor = high_floor
    for x in range(x0, x1 + 1):
        floor -= 1
        for z in range(zc - half, zc + half + 1):
            for y in range(0 if cap is None else cap, floor):
                room.set(x, y, z, c['base'])
            room.set(x, floor, z, c['trim_stairs'], stairs(facing))
    return floor


def niche(room, c, rng, x, y, plane_z, recess_z, facing, kind='sealed'):
    """A 1 x 2 recess in a wall: two stacked slots at (x, y) and (x, y + 1) in the wall plane, the recess one block
    back. Sealed: two sealed niche fronts over their seals; opened: two opened fronts over bones, a lamp-lit urn in
    the lower one when `kind` is 'urn'."""
    front = {'facing': facing}
    for dy in (0, 1):
        if kind == 'sealed':
            room.set(x, y + dy, recess_z, c['seal'])
            room.set(x, y + dy, plane_z, SEALED_NICHE, front)
        else:
            room.set(x, y + dy, recess_z, *bone('x' if facing in ('north', 'south') else 'z'))
            room.set(x, y + dy, plane_z, OPENED_NICHE, front)
    room.set(x, y - 1, plane_z, c['trim'])
    room.set(x, y + 2, plane_z, c['accent'] if rng.random() < 0.5 else c['trim'])


def dais(room, c, x0, x1, z0, z1, floor, steps, ring_radius=None):
    """A stepped plinth: `steps` courses, each a ring of stairs rising toward the centre with a base core, drawn in by
    one per course; the top an inlay field with an accent at its heart (and an inlay ring when ring_radius). Returns
    the y a piece stands at on top."""
    for k in range(steps):
        y = floor + 1 + k
        ax0, ax1, az0, az1 = x0 + k, x1 - k, z0 + k, z1 - k
        for x in range(ax0, ax1 + 1):
            for z in range(az0, az1 + 1):
                room.set(x, y, z, c['base'])
        ring_stairs(room, c['trim_stairs'], ax0, ax1, az0, az1, y, outward=False, half='bottom')
    k = steps
    ax0, ax1, az0, az1 = x0 + k, x1 - k, z0 + k, z1 - k
    y = floor + steps
    cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
    for x in range(ax0, ax1 + 1):
        for z in range(az0, az1 + 1):
            room.set(x, y, z, c['inlay'])
    if ring_radius:
        for (x, z) in ring(cx, cz, ring_radius):
            if ax0 <= x <= ax1 and az0 <= z <= az1:
                room.set(x, y, z, c['accent'])
    room.set(cx, y, cz, c['accent'])
    return y + 1


# --- the spiral (Track 3's descent, built here) ---------------------------------------------------------------------

class Shaft:
    """A square drum `size` across with a three-wide stair round an open well, `turns` full turns from an entry stand,
    dropping two per side. Corners and runs as the Track 3 spiral: the walk enters the north-west corner from the west,
    runs east along the north side, then south, west and north back to the corner."""

    def __init__(self, size=15, top=22, entry=17, turns=2):
        self.size, self.top, self.entry, self.turns = size, top, entry, turns
        self.corners = [(1, 1), (11, 1), (11, 11), (1, 11)]
        self.runs = [
            (lambda a: [(4 + a, z) for z in range(1, 4)], 'west'),
            (lambda a: [(x, 4 + a) for x in range(11, 14)], 'north'),
            (lambda a: [(10 - a, z) for z in range(11, 14)], 'east'),
            (lambda a: [(x, 10 - a) for x in range(1, 4)], 'south'),
        ]
        self.well = range(5, 10)

    def levels(self):
        """Every band cell's standing levels over the whole shaft, and the stairs: {(x, z, stand): high side}."""
        levels, stair = {}, {}
        stand = self.entry
        for _ in range(self.turns):
            for side in range(4):
                cx, cz = self.corners[side]
                for x in range(cx, cx + 3):
                    for z in range(cz, cz + 3):
                        levels.setdefault((x, z), []).append(stand)
                cells_at, facing = self.runs[side]
                for along in range(7):
                    step = along in (2, 5)
                    if step:
                        stand -= 1
                    for cell in cells_at(along):
                        levels.setdefault(cell, []).append(stand)
                        if step:
                            stair[cell + (stand,)] = facing
        for x in range(1, 4):
            for z in range(1, 4):
                levels[(x, z)].append(stand)
        self.exit = stand
        return levels, stair

    @staticmethod
    def balustrade_source(x, z):
        if z == 4:
            return x, 3
        if x == 10:
            return 11, z
        if z == 10:
            return x, 11
        return 3, z


def stair_shaft(c, rng, shaft):
    """The spiral as one Room: the drum's field with pilasters at the well's corners, the stair and its landings, a
    balustrade round the well whose core under each walk is coursed newel masonry (base banded with trim every third
    course, a capital under the rail), an ossuary pit with a lamp at the well's foot, and a stepped dome over the drum
    with a lamp at its crown. Sconces along each run, one per turn."""
    size, top = shaft.size, shaft.top
    room = Room(size, top + 1, size)
    levels, stair = shaft.levels()
    for x in range(size):
        for z in range(size):
            for y in range(top + 1):
                room.set(x, y, z, field(rng, c))
    for (x, z), stands in levels.items():
        _shaft_column(room, c, rng, shaft, x, z, sorted(stands), stair, rail_axis=None)
    for x in range(4, 11):
        for z in range(4, 11):
            if x in shaft.well and z in shaft.well:
                room.set(x, 0, z, c['base'] if (x + z) % 2 else c['floor'])
                for y in range(1, top):
                    room.set(x, y, z, AIR)
                room.set(x, top, z, c['vault'])
                continue
            sx, sz = shaft.balustrade_source(x, z)
            axis = 'x' if z in (4, 10) else 'z'
            _shaft_column(room, c, rng, shaft, x, z, sorted(levels[(sx, sz)]), {}, rail_axis=axis,
                          corner=(x in (4, 10) and z in (4, 10)))
    # The well's foot: an ossuary pit under the stair.
    for x in shaft.well:
        for z in shaft.well:
            if x in (5, 9) or z in (5, 9):
                room.set(x, 1, z, *bone('y' if (x + z) % 2 else 'x'))
    room.set(7, 1, 7, c['accent'])
    room.set(7, 2, 7, c['light'])
    # The dome over the drum: the band's ceiling steps in over the walk, the well's crown is a stepped boss.
    for x in range(1, 14):
        for z in range(1, 14):
            if room.get(x, top - 1, z) == AIR and not (x in shaft.well and z in shaft.well):
                edge = x in (1, 13) or z in (1, 13)
                if edge:
                    facing = 'north' if z == 1 else 'south' if z == 13 else 'west' if x == 1 else 'east'
                    room.set(x, top - 1, z, c['vault_stairs'], stairs(facing, 'top'))
    ring_stairs(room, c['vault_stairs'], 5, 9, 5, 9, top - 1, outward=True, half='top')
    for x in range(6, 9):
        for z in range(6, 9):
            room.set(x, top - 1, z, AIR)
    room.set(7, top, 7, c['light'])
    for (x, z) in ((6, 7), (8, 7), (7, 6), (7, 8)):
        room.set(x, top, z, c['accent'])
    # The drum: pilasters at the corners of the well's square, coursed with the band's capitals.
    for p in (4, 10):
        for plane in (0, 14):
            for y in range(top):
                room.set(p, y, plane, c['trim'])
                room.set(plane, y, p, c['trim'])
    for stand in [shaft.entry - 8 * t for t in range(shaft.turns)]:
        for (x, y, z), facing in (((7, stand + 1, 0), 'south'), ((14, stand - 1, 7), 'west'),
                                  ((7, stand - 3, 14), 'north'), ((0, stand - 5, 7), 'east')):
            if 0 < y < top:
                wall_light(room, c, x, y, z, facing)
    # The outer wall's courses: a string course along each run at every walk's floor height, a cornice under the dome.
    for y in range(1, top):
        for i in range(size):
            for (x, z) in ((i, 0), (i, size - 1), (0, i), (size - 1, i)):
                if room.get(x, y, z) in [c['wall']] + list(c['wall_alt']) and y % 4 == 0:
                    room.set(x, y, z, c['base'])
    return room


def _shaft_column(room, c, rng, shaft, x, z, stands, stair, rail_axis, corner=False):
    """One cell of the spiral: under the lowest walk solid stone (newel masonry where it faces the well), then for each
    walk a floor (carrying a step where the walk drops) with air above it up to the next walk's floor or the dome. A balustrade
    cell carries a rail on its floor."""
    top = shaft.top
    for y in range(0, stands[0] - 1):
        if rail_axis:
            room.set(x, y, z, c['trim'] if y % 3 == 2 else c['base'])
        else:
            room.set(x, y, z, c['base'])
    for i, stand in enumerate(stands):
        if stand - 1 < 0:
            continue
        facing = stair.get((x, z, stand))
        if rail_axis:
            room.set(x, stand - 1, z, c['trim'])
        else:
            edge = x in (1, 3, 11, 13) or z in (1, 3, 11, 13)
            room.set(x, stand - 1, z, c['inlay'] if edge and rng.random() < 0.5 else
                     c['floor_alt'] if rng.random() < 0.1 else c['floor'])
        ceiling = stands[i + 1] - 1 if i + 1 < len(stands) else top
        for y in range(stand, ceiling):
            room.set(x, y, z, AIR)
        if facing:
            # The step stands on the lower walk's floor, its high side level with the walk above.
            room.set(x, stand, z, c['trim_stairs'], stairs(facing))
        if not rail_axis and i + 1 < len(stands) and ceiling - stand >= 6:
            # A landing under a landing: corbel the walk above off the drum wall.
            fz = 'north' if z == 1 else 'south' if z == 13 else None
            fx = 'west' if x == 1 else 'east' if x == 13 else None
            if fz and fx:
                room.set(x, ceiling - 1, z, c['vault_stairs'], corner_stairs(fz, fx, 'top'))
            elif fz or fx:
                room.set(x, ceiling - 1, z, c['vault_stairs'], stairs(fz or fx, 'top'))
        if rail_axis and stand < ceiling:
            room.set(x, stand, z, c['trim_wall'], post() if corner else rail(rail_axis))
        if rail_axis and i + 1 < len(stands):
            # The underside of the walk above, seen from this one: a capital course under the next floor.
            room.set(x, stands[i + 1] - 2, z, c['accent'] if corner else c['trim'])
    if len(stands) and stands[-1] - 1 < top:
        room.set(x, top, z, c['vault'])


# --- wall dressing ------------------------------------------------------------------------------------------------

SEALED_NICHE = 'hemomancy:sealed_niche'
OPENED_NICHE = 'hemomancy:opened_niche'


def loculus(room, c, xs, y, plane_z, recess_z, kind, rng, facing):
    """One horizontal burial slot, three blocks long: its opening cells in the wall plane, the recess one block back.
    `facing` is the room side. A sealed slot shows three sealed niche fronts over its seal course; a looted urn (any
    other kind not named here) three opened fronts, whose frame and shelf lip leave the barrel behind clickable; a
    broken slot keeps one sealed plate over its last seal, one opened frame over its bones and a gap over the rubble;
    a slot of remains is open bone."""
    for x in xs:
        room.set(x, y, plane_z, AIR)
    front = {'facing': facing}
    if kind == 'sealed':
        for x in xs:
            room.set(x, y, recess_z, c['seal'])
            room.set(x, y, plane_z, SEALED_NICHE, front)
        if rng.random() < 0.35:
            room.set(xs[1], y, recess_z, c['accent'])
    elif kind == 'broken':
        room.set(xs[0], y, recess_z, c['seal'])
        room.set(xs[1], y, recess_z, *bone('x'))
        room.set(xs[2], y, recess_z, c['floor_alt'])
        room.set(xs[0], y, plane_z, SEALED_NICHE, front)
        room.set(xs[1], y, plane_z, OPENED_NICHE, front)
    elif kind == 'remains':
        for x in xs:
            room.set(x, y, recess_z, *bone('x'))
    else:
        room.set(xs[0], y, recess_z, *bone('x'))
        barrel(room, xs[1], y, recess_z, facing, 'hemomancy:chests/mausoleum')
        room.set(xs[2], y, recess_z, *bone('x'))
        for x in xs:
            room.set(x, y, plane_z, OPENED_NICHE, front)


# --- set pieces (Track 6) ------------------------------------------------------------------------------------------

DEVOTEE_POSES = {'kneeling': 0, 'bowed': 1, 'reaching': 2, 'prostrate': 3, 'striding': 4}


def sconce(room, x, y, z, facing, flame='pale'):
    """A reliquary sconce in the air cell at (x, y, z), hung on the wall behind it and facing away from that wall.
    `flame` is the sconce's flame: 'pale' (the standard bone-oil light) or 'crimson' (Hemorath's)."""
    room.set(x, y, z, 'hemomancy:reliquary_sconce', {'facing': facing, 'flame': flame})


def wall_light(room, c, x, y, z, facing):
    """The standard mausoleum wall light: (x, y, z) is the wall cell a lamp would be set into; the wall stays whole
    and a sconce hangs in the open cell in front of it, on the `facing` side."""
    dx, dz = STEP[facing]
    front = (x + dx, y, z + dz)
    if room.get(*front) != AIR:
        raise ValueError('a sconce at %s needs open air in front of its wall, found %s' % (front, room.get(*front)))
    room.set(x, y, z, c['wall'])
    sconce(room, *front, facing)


def devotee(room, x, y, z, facing, pose, head='forward'):
    """A frozen devotee two tall, its lower half standing on the floor at (x, y, z); `head` forward, or turned to its
    own left or right."""
    pose = str(DEVOTEE_POSES[pose])
    for dy, half in ((0, 'lower'), (1, 'upper')):
        room.set(x, y + dy, z, 'hemomancy:frozen_devotee', {'facing': facing, 'half': half, 'pose': pose,
                                                             'head': head})


def containment_anchor(room, x, y, z, snapped=False):
    """A dormant containment anchor two tall, its lower half on the floor at (x, y, z); `snapped`, one whose pillar
    broke and whose chains tore (Seraphae's Infirmary Row)."""
    for dy, half in ((0, 'lower'), (1, 'upper')):
        room.set(x, y + dy, z, 'hemomancy:containment_anchor_block',
                 {'half': half, 'snapped': 'true' if snapped else 'false'})


def two_tall_fits(room, x, y, z):
    """Whether a two-tall piece can stand at (x, y, z): both cells open, the floor under them solid."""
    below = room.get(x, y - 1, z)
    return room.get(x, y, z) == AIR and room.get(x, y + 1, z) == AIR and below not in (None, AIR)


def swollen_pillar(room, x, z, y0, y1, block, stairs_block, sides=('north', 'east', 'south', 'west')):
    """A pillar (the column x, y0..y1, z) that has swollen at mid-height: its middle course turns to `block`, and on
    each open side named in `sides` (a side kept clear, such as a boss floor's, is left out) a three-course bulge stands against it, flaring out from an upside-down stair, full at the middle
    and drawing back in on a stair above. Only open air is filled, so an arcade's arch stones and a wall stay. Returns
    the cells the bulge took."""
    m = (y0 + y1) // 2
    room.set(x, m, z, block)
    taken = [(x, m, z)]
    for side in sides:
        dx, dz = STEP[side]
        toward = OPPOSITE[side]
        for y, half in ((m - 1, 'top'), (m, None), (m + 1, 'bottom')):
            cell = (x + dx, y, z + dz)
            if not room.inside(*cell) or room.get(*cell) != AIR:
                continue
            if half:
                room.set(*cell, stairs_block, stairs(toward, half))
            else:
                room.set(*cell, block)
            taken.append(cell)
    return taken


def plinth(room, c, x, floor, z, height=1):
    """A stepped base for a set piece: base course, then the piece stands on top. Returns the y it stands at."""
    for y in range(floor + 1, floor + 1 + height):
        room.set(x, y, z, c['base'])
    return floor + 1 + height


def ring(cx, cz, radius):
    """The floor cells on a circle of the given radius around (cx, cz): an inlay ring a set piece or trial marks."""
    cells = set()
    r2_in = (radius - 0.5) ** 2
    r2_out = (radius + 0.5) ** 2
    span = int(radius) + 1
    for dx in range(-span, span + 1):
        for dz in range(-span, span + 1):
            d2 = dx * dx + dz * dz
            if r2_in <= d2 < r2_out:
                cells.add((cx + dx, cz + dz))
    return sorted(cells)


# --- finishing -----------------------------------------------------------------------------------------------------

def finish(room, c, seed, open_outside=False):
    """Breaks every flat surface larger than 3 x 3 the modules left: in each 4 x 4 window of one exposed full block, one
    cell (drawn from a fixed seed) takes one of that block's variants. A block with no variants is the room's author's
    to dress, so it raises."""
    from . import checks
    connect_walls(room, sorted(cell for cell in room.joined if _wall_name(room.get(*cell))))
    rng = random.Random('finish/' + seed)
    for _ in range(12):
        windows = checks.flat_windows(room, open_outside)
        if not windows:
            return room
        changed = False
        for name, cells in windows:
            if any(room.get(*cell) != name for cell in cells):
                continue    # an earlier swap in this pass already broke it
            if name not in c.variants or not c.variants[name]:
                raise ValueError('%s: a flat face of %s at %s has no variant to break it' % (seed, name, cells[0]))
            cell = rng.choice(cells)
            room.set(*cell, rng.choice(c.variants[name]))
            changed = True
        if not changed:
            break
    left = checks.flat_windows(room, open_outside)
    if left:
        raise ValueError('%s: %d flat faces left after finishing' % (seed, len(left)))
    return room

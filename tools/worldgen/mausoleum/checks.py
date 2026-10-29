"""Track 7's acceptance checks, run on every template build.py writes:

- flats: no exposed surface larger than 3 x 3 is a single flat block (a 4 x 4 window of one full block, every face of it
  open to the same air side);
- states: every placed state names only properties its block has, with values it accepts (a stairs needs facing, half,
  shape and waterlogged; a wall its four sides, up and waterlogged; and so on);
- arches: every horizontal doorway a connector opens has an arch over it (a keystone in the theme's accent within three
  courses of the opening's head);
- vaults: every room taller than 5 has a vault (each ceiling over air six or more tall is stepped with upside-down
  stairs somewhere along it).

The same property table backs MausoleumComplexResourceTest.everyPlacedStateIsARealState; a registry-backed GameTest
checks Hemomancy's own blocks in a loaded game.
"""
import json
import os

AIR = 'minecraft:air'
ASSETS = os.path.join('src', 'main', 'resources', 'assets')
SIDES = ('north', 'east', 'south', 'west')
BOOL = ('true', 'false')
WALL_SIDE = ('none', 'low', 'tall')
FACING4 = SIDES
FACING6 = SIDES + ('up', 'down')
ORIENTATIONS = tuple('%s_%s' % pair for pair in [
    ('down', 'east'), ('down', 'north'), ('down', 'south'), ('down', 'west'), ('up', 'east'), ('up', 'north'),
    ('up', 'south'), ('up', 'west'), ('west', 'up'), ('east', 'up'), ('north', 'up'), ('south', 'up')])

# Vanilla shape families by name suffix: property -> accepted values.
FAMILIES = [
    ('_stairs', {'facing': FACING4, 'half': ('top', 'bottom'),
                 'shape': ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'),
                 'waterlogged': BOOL}),
    ('_slab', {'type': ('top', 'bottom', 'double'), 'waterlogged': BOOL}),
    ('_wall', {'up': BOOL, 'north': WALL_SIDE, 'east': WALL_SIDE, 'south': WALL_SIDE, 'west': WALL_SIDE,
               'waterlogged': BOOL}),
    ('_button', {'face': ('floor', 'wall', 'ceiling'), 'facing': FACING4, 'powered': BOOL}),
    ('_pressure_plate', {'powered': BOOL}),
    ('_door', {'facing': FACING4, 'half': ('upper', 'lower'), 'hinge': ('left', 'right'), 'open': BOOL,
               'powered': BOOL}),
]
# Vanilla blocks with state the kit sets, and every vanilla block the kit places bare.
VANILLA = {
    'minecraft:bone_block': {'axis': ('x', 'y', 'z')},
    'minecraft:barrel': {'facing': FACING6, 'open': BOOL},
    'minecraft:jigsaw': {'orientation': ORIENTATIONS},
    'minecraft:chain': {'axis': ('x', 'y', 'z'), 'waterlogged': BOOL},
    'minecraft:quartz_pillar': {'axis': ('x', 'y', 'z')},
    'minecraft:basalt': {'axis': ('x', 'y', 'z')},
    'minecraft:polished_basalt': {'axis': ('x', 'y', 'z')},
    'minecraft:deepslate': {'axis': ('x', 'y', 'z')},
    'minecraft:lantern': {'hanging': BOOL, 'waterlogged': BOOL},
    'minecraft:soul_lantern': {'hanging': BOOL, 'waterlogged': BOOL},
    'minecraft:hanging_roots': {'waterlogged': BOOL},
    'minecraft:mangrove_roots': {'waterlogged': BOOL},
}


def _blockstate_keys(name):
    """The property names a Hemomancy block's blockstate file switches on, or None when it has no file."""
    namespace, path = name.split(':')
    file = os.path.join(ASSETS, namespace, 'blockstates', path + '.json')
    if not os.path.exists(file):
        return None
    with open(file) as f:
        data = json.load(f)
    keys = {}
    for variant in data.get('variants', {}):
        for pair in filter(None, variant.split(',')):
            key, value = pair.split('=')
            keys.setdefault(key, set()).add(value)
    for part in data.get('multipart', []):
        when = part.get('when', {})
        for clause in when.get('OR', [when]) if 'OR' in when else [when]:
            for key, value in clause.items():
                keys.setdefault(key, set()).update(str(value).split('|'))
    return keys


# Properties a Hemomancy block may carry without its blockstate switching on them (no model change).
SILENT = {'waterlogged': BOOL, 'powered': BOOL}
# Hemomancy blocks with state their blockstate file does not switch on (a block entity renderer draws it).
UNMODELLED = {'hemomancy:ring_door': {'open': BOOL}, 'hemomancy:pulse_pylon': {'charge': ('0', '1', '2', '3')}}


def expected(name):
    """The property table for a block: property -> accepted values (None: any value its blockstate names)."""
    if name not in SET_PIECES:
        for suffix, table in FAMILIES:
            if name.endswith(suffix):
                return table
    if name in VANILLA:
        return VANILLA[name]
    if name.startswith('hemomancy:'):
        keys = _blockstate_keys(name)
        if keys is None:
            return None
        table = dict(SILENT)
        table.update(UNMODELLED.get(name, {}))
        table.update({k: tuple(v) for k, v in keys.items()})
        return table
    return {}


def state_problems(room):
    seen = set()
    problems = []
    for (name, props, _nbt) in room.blocks.values():
        if (name, props) in seen or name == AIR:
            continue
        seen.add((name, props))
        table = expected(name)
        if table is None:
            problems.append('%s has no blockstate' % name)
            continue
        for key, value in props:
            if key not in table:
                problems.append('%s has no property %s' % (name, key))
            elif value not in table[key]:
                problems.append('%s.%s does not accept %s' % (name, key, value))
        if any(name.endswith(s) for s in ('_stairs', '_wall', '_slab')):
            missing = set(table) - {k for k, _ in props}
            if missing:
                problems.append('%s is placed without %s' % (name, ', '.join(sorted(missing))))
    return problems


# --- flat surfaces ----------------------------------------------------------------------------------------------------

NOT_CUBES = ('_stairs', '_slab', '_wall', '_button', '_pressure_plate', '_door', '_trapdoor', '_fence', '_bars',
             '_pane', '_lantern', '_carpet', 'minecraft:chain', 'minecraft:hanging_roots', 'minecraft:jigsaw',
             'minecraft:air', '_glass')


def cube(entry):
    if entry is None:
        return False
    name, props, _ = entry
    if name.endswith(NOT_CUBES) or name in NOT_CUBES:
        return False
    if name.startswith('hemomancy:') and props and dict(props).keys() - {'axis'}:
        return False    # a set piece (facing, half, lit...) is a model, not a face of masonry
    return name not in SET_PIECES


SET_PIECES = {'hemomancy:reliquary_sconce', 'hemomancy:sealed_niche', 'hemomancy:opened_niche', 'hemomancy:rot_pyre',
              'hemomancy:offering_stand', 'hemomancy:spawn_mark', 'hemomancy:blood_basin',
              'hemomancy:containment_anchor_block', 'hemomancy:saint_sarcophagus', 'hemomancy:ring_door',
              'hemomancy:chapel_focus', 'hemomancy:ward_anchor', 'hemomancy:sanguine_glass',
              'hemomancy:blood_gutter', 'hemomancy:venous_basin', 'hemomancy:bone_outgrowth',
              'hemomancy:spore_shelf', 'hemomancy:bog_body', 'hemomancy:hanging_icicle', 'hemomancy:frozen_drip'}
# Things that hang in a room's air (chains, Seraphae's bone outgrowths, Putriciel's roots and spore shelves, Velorum's
# icicles and frozen runoff): a vault check looks through them.
HANGING = {'minecraft:chain', 'hemomancy:hematic_iron_chain', 'hemomancy:bone_outgrowth', 'minecraft:hanging_roots',
           'minecraft:mangrove_roots', 'hemomancy:spore_shelf', 'hemomancy:hanging_icicle', 'hemomancy:frozen_drip'}
STEPS = {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0), 'up': (0, 1, 0),
         'down': (0, -1, 0)}


def flat_problems(room, open_outside=False, window=4):
    return ['%s face of %s from %s' % (side, name, cells[0]) for name, cells, side in
            flat_windows(room, open_outside, window, sides=True)]


def flat_windows(room, open_outside=False, window=4, sides=False):
    """Every window x window square of exposed faces of one full block in one plane, as (block, [cells]). open_outside:
    cells outside the template (and unset cells) count as open air, for a piece that stands above ground."""
    sx, sy, sz = room.size
    blocks = room.blocks

    def is_open(x, y, z):
        # Above ground, what lies outside the template (or is left out of it) is open air over the ground row.
        if not room.inside(x, y, z):
            return open_outside and y >= 1
        entry = blocks.get((x, y, z))
        if entry is None:
            return open_outside and y >= 1
        return entry[0] == AIR

    planes = {}
    for (x, y, z), entry in blocks.items():
        if not cube(entry):
            continue
        for side, (dx, dy, dz) in STEPS.items():
            if not is_open(x + dx, y + dy, z + dz):
                continue
            if side in ('north', 'south'):
                key, uv = (side, z), (x, y)
            elif side in ('east', 'west'):
                key, uv = (side, x), (z, y)
            else:
                key, uv = (side, y), (x, z)
            planes.setdefault(key, {})[uv] = entry[0]

    def cell(side, at, u, v):
        if side in ('north', 'south'):
            return (u, v, at)
        if side in ('east', 'west'):
            return (at, v, u)
        return (u, at, v)

    found = []
    for (side, at), cells in sorted(planes.items()):
        for (u, v), name in sorted(cells.items()):
            if all(cells.get((u + du, v + dv)) == name for du in range(window) for dv in range(window)):
                square = [cell(side, at, u + du, v + dv) for du in range(window) for dv in range(window)]
                found.append((name, square, side) if sides else (name, square))
    return found


# --- doorways and vaults ----------------------------------------------------------------------------------------------

def jigsaws(room):
    for (x, y, z), (name, props, nbt) in room.blocks.items():
        if name == 'minecraft:jigsaw':
            yield (x, y, z), dict(props)['orientation'].split('_')[0], nbt


INWARD = {'north': (0, 1), 'south': (0, -1), 'east': (-1, 0), 'west': (1, 0)}
# A wider opening is not a doorway: the run carries on into the next piece (a trial hall into its focus room).
DOORWAY_WIDTH = 5


def arch_problems(room, accent):
    """A doorway's arch may stand in the wall plane at the connector or up to three cells in along its passage."""
    problems = []
    for (x, y, z), front, nbt in jigsaws(room):
        if front in ('up', 'down'):
            continue
        if any(room.get(x, y + dy, z) == 'hemomancy:ring_door' for dy in (1, 2)):
            continue
        ix, iz = INWARD[front]
        found = width = None
        for depth in range(4):
            cx, cz = x + ix * depth, z + iz * depth
            sill = y + 1
            while sill < y + 3 and room.get(cx, sill, cz) != AIR:
                sill += 1
            if room.get(cx, sill, cz) != AIR:
                continue
            head = sill
            while room.get(cx, head, cz) == AIR:
                head += 1
            if width is None:
                ax, az = (1, 0) if ix == 0 else (0, 1)
                width = 1 + sum(1 for s in (1, -1) for d in range(1, 9)
                                if all(room.get(cx + s * ax * k, sill, cz + s * az * k) == AIR for k in range(1, d + 1)))
            if any(room.get(cx, h, cz) == accent for h in range(head, head + 3)):
                found = depth
                break
        if width is None:
            problems.append('connector %s at %s opens no doorway' % (nbt['name'], (x, y, z)))
        elif found is None and width <= DOORWAY_WIDTH:
            problems.append('the doorway at %s (%s) has no keystone' % ((x, y, z), nbt['name']))
    return problems


def vault_problems(room, tall=6):
    """Each ceiling over a run of air `tall` or more must belong to a stepped ceiling: an upside-down stairs on the
    ceiling cell or one of its horizontal neighbours, or the ceiling run it joins."""
    sx, sy, sz = room.size
    ceilings = set()
    for x in range(sx):
        for z in range(sz):
            run = 0
            for y in range(sy):
                if room.get(x, y, z) == AIR or room.get(x, y, z) in HANGING:
                    run += 1
                    continue
                if run >= tall and room.get(x, y, z) is not None:
                    ceilings.add((x, y, z))
                run = 0
    stepped = set()
    for (x, y, z) in ceilings:
        for dx, dz in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
            for dy in (-1, 0, 1):
                entry = room.blocks.get((x + dx, y + dy, z + dz))
                if entry and entry[0].endswith('_stairs') and dict(entry[1]).get('half') == 'top':
                    stepped.add((x, y, z))
    # Group the ceilings (neighbours within a block up or down) and require a stepped cell in each group.
    problems = []
    left = set(ceilings)
    while left:
        start = left.pop()
        group, queue = {start}, [start]
        while queue:
            x, y, z = queue.pop()
            for dx in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    for dy in (-1, 0, 1):
                        n = (x + dx, y + dy, z + dz)
                        if n in left:
                            left.discard(n)
                            group.add(n)
                            queue.append(n)
        if not group & stepped:
            low = min(group)
            problems.append('a flat ceiling over a tall room (%d cells, from %s)' % (len(group), low))
    return problems


def all_problems(name, room, accent, open_outside=False):
    out = ['%s: %s' % (name, p) for p in state_problems(room)]
    out += ['%s: flat %s' % (name, p) for p in flat_problems(room, open_outside)]
    out += ['%s: %s' % (name, p) for p in arch_problems(room, accent)]
    out += ['%s: %s' % (name, p) for p in vault_problems(room)]
    return out

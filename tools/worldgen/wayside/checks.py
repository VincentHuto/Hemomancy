"""Acceptance checks run on both wayside templates before anything is written:

- states: every palette id is a block this table knows (a Hemomancy id must also have a blockstate file), every
  property is one its block has (read from the Java block classes) with a value it accepts, and every stairs, slab,
  wall, fence and trapdoor carries its full shape;
- placeholders: the site has its two or three placeholder inscriptions, each hung on a full cube;
- mounts: wall-hung props have their wall, floor props stand on something, loot tables exist;
- water: every water cell (and waterlogged block) is held in by the template on its four sides and below, and no air
  pocket under the ground row touches water or void.
"""
import os

from .kit import AIR, WATER, PLACEHOLDER_BLOCK, SIDES, full

ASSETS = os.path.join('src', 'main', 'resources', 'assets', 'hemomancy', 'blockstates')
LOOT = os.path.join('src', 'main', 'resources', 'data')
BOOL = ('true', 'false')
F4 = ('north', 'east', 'south', 'west')
F6 = F4 + ('up', 'down')
AXIS = ('x', 'y', 'z')
WALL_SIDE = ('none', 'low', 'tall')
ROT16 = tuple(str(i) for i in range(16))

FAMILIES = [
    ('_stairs', {'facing': F4, 'half': ('top', 'bottom'),
                 'shape': ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'), 'waterlogged': BOOL}),
    ('_slab', {'type': ('top', 'bottom', 'double'), 'waterlogged': BOOL}),
    ('_wall', {'up': BOOL, 'north': WALL_SIDE, 'east': WALL_SIDE, 'south': WALL_SIDE, 'west': WALL_SIDE,
               'waterlogged': BOOL}),
    ('_fence', {'north': BOOL, 'east': BOOL, 'south': BOOL, 'west': BOOL, 'waterlogged': BOOL}),
    ('_trapdoor', {'facing': F4, 'half': ('top', 'bottom'), 'open': BOOL, 'powered': BOOL, 'waterlogged': BOOL}),
    ('_banner', {'rotation': ROT16}),
]
SHAPED = ('_stairs', '_slab', '_wall', '_fence', '_trapdoor')

# Vanilla blocks placed with state, and every vanilla block placed bare (a typo would load as air).
VANILLA = {
    'minecraft:water': {'level': ('0',)},
    'minecraft:barrel': {'facing': F6, 'open': BOOL},
    'minecraft:bone_block': {'axis': AXIS},
    'minecraft:mangrove_log': {'axis': AXIS},
    'minecraft:stripped_mangrove_log': {'axis': AXIS},
    'minecraft:muddy_mangrove_roots': {'axis': AXIS},
    'minecraft:mangrove_roots': {'waterlogged': BOOL},
    'minecraft:hanging_roots': {'waterlogged': BOOL},
    'minecraft:skeleton_skull': {'rotation': ROT16, 'powered': BOOL},
    'minecraft:grass_block': {'snowy': BOOL},
    'minecraft:podzol': {'snowy': BOOL},
    'minecraft:crimson_stem': {'axis': AXIS},
    'minecraft:stripped_crimson_stem': {'axis': AXIS},
    'minecraft:crimson_hyphae': {'axis': AXIS},
    'minecraft:polished_basalt': {'axis': AXIS},
    'minecraft:basalt': {'axis': AXIS},
    'minecraft:chain': {'axis': AXIS, 'waterlogged': BOOL},
    'minecraft:campfire': {'facing': F4, 'lit': BOOL, 'signal_fire': BOOL, 'waterlogged': BOOL},
    'minecraft:lectern': {'facing': F4, 'has_book': BOOL, 'powered': BOOL},
    'minecraft:decorated_pot': {'facing': F4, 'cracked': BOOL, 'waterlogged': BOOL},
    'minecraft:end_rod': {'facing': F6},
}
VANILLA_BARE = {
    'minecraft:air', 'minecraft:mud', 'minecraft:packed_mud', 'minecraft:mud_bricks', 'minecraft:rooted_dirt',
    'minecraft:moss_block', 'minecraft:moss_carpet', 'minecraft:dirt', 'minecraft:coarse_dirt', 'minecraft:clay',
    'minecraft:mangrove_planks', 'minecraft:lily_pad', 'minecraft:brown_mushroom', 'minecraft:cobweb',
    'minecraft:crimson_nylium', 'minecraft:netherrack', 'minecraft:blackstone', 'minecraft:polished_blackstone',
    'minecraft:polished_blackstone_bricks', 'minecraft:cracked_polished_blackstone_bricks', 'minecraft:smooth_basalt',
    'minecraft:nether_wart_block', 'minecraft:shroomlight', 'minecraft:soul_soil', 'minecraft:crimson_planks',
    'minecraft:red_wool', 'minecraft:brown_wool', 'minecraft:black_wool', 'minecraft:red_carpet',
    'minecraft:black_carpet', 'minecraft:brown_carpet', 'minecraft:white_carpet', 'minecraft:cartography_table',
    'minecraft:crimson_roots', 'minecraft:crimson_fungus', 'minecraft:gravel',
}

# Hemomancy blocks: property -> values, from the block classes (createBlockStateDefinition) and their blockstates.
HEMO = {
    'hemomancy:bog_body': {'facing': F4, 'harvested': BOOL, 'waterlogged': BOOL},           # BogBodyBlock
    'hemomancy:sodden_mud_bricks': {},                                                       # MudBlock
    'hemomancy:bog_iron': {},
    'hemomancy:bone_bricks': {},
    'hemomancy:chiseled_bone_bricks': {},
    'hemomancy:cracked_bone_bricks': {},
    'hemomancy:sealed_niche': {'facing': F4},                                                # NicheFrontBlock
    'hemomancy:opened_niche': {'facing': F4},
    'hemomancy:reliquary_sconce': {'facing': F4, 'flame': ('pale', 'crimson', 'smoulder')},  # ReliquarySconceBlock
    'hemomancy:mnemonic_candle': {'lit': BOOL, 'waterlogged': BOOL},                         # MnemonicCandleBlock
    'hemomancy:specimen_jar': {'facing': F4, 'waterlogged': BOOL},                           # SpecimenJarBlock
    'hemomancy:adipocere_crust': {'facing': F4},                                             # AdipocereCrustBlock
    'hemomancy:venous_basin': {'facing': F4, 'fill': ('0', '1', '2', '3')},                  # VenousBasinBlock
    'hemomancy:blood_echo_inscription': {'facing': F4, 'waterlogged': BOOL},                 # DiscoveryInscriptionBlock
    'hemomancy:hematic_iron_chain': {'axis': AXIS, 'waterlogged': BOOL},                     # ChainBlock
    'hemomancy:hematic_lantern': {'hanging': BOOL, 'waterlogged': BOOL},                     # LanternBlock
    'hemomancy:phlebotomists_field_case': {'facing': F4, 'open': BOOL},                      # PhlebotomistsFieldCaseBlock
    'hemomancy:clairaudiograph': {'facing': F4},                                             # ClairaudiographBlock
    'hemomancy:humoral_barometer': {'facing': F4},                                           # HumoralBarometerBlock
    'hemomancy:harbinger_escritoire': {'facing': F4, 'waterlogged': BOOL},                   # HarbingerEscritoireBlock
    'hemomancy:packed_ash': {},
    'hemomancy:bone_brick_stairs': None, 'hemomancy:bone_brick_slab': None, 'hemomancy:bone_brick_wall': None,
}


def table(name):
    if name in HEMO and HEMO[name] is not None:
        return HEMO[name]
    for suffix, props in FAMILIES:
        if name.endswith(suffix):
            return props
    if name in VANILLA:
        return VANILLA[name]
    if name in VANILLA_BARE:
        return {}
    return None


def state_problems(site):
    problems, seen = [], set()
    for name, props, _nbt in site.blocks.values():
        if (name, props) in seen:
            continue
        seen.add((name, props))
        if name.startswith('hemomancy:'):
            if name not in HEMO:
                problems.append('%s is not in the checked Hemomancy table' % name)
                continue
            if not os.path.exists(os.path.join(ASSETS, name.split(':')[1] + '.json')):
                problems.append('%s has no blockstate file' % name)
        props_table = table(name)
        if props_table is None:
            problems.append('%s is not a known block' % name)
            continue
        for key, value in props:
            if key not in props_table:
                problems.append('%s has no property %s' % (name, key))
            elif value not in props_table[key]:
                problems.append('%s.%s does not accept %s' % (name, key, value))
        if name.endswith(SHAPED) or name.endswith('_banner'):
            missing = set(props_table) - {k for k, _ in props}
            if missing:
                problems.append('%s is placed without %s' % (name, ', '.join(sorted(missing))))
    return problems


def placeholder_problems(site, placeholder, low=2, high=3):
    problems, found = [], 0
    for (x, y, z), (name, props, nbt) in site.blocks.items():
        if name != PLACEHOLDER_BLOCK:
            continue
        if nbt is None or str(nbt.get('InscriptionId')) != placeholder:
            problems.append('inscription at %s is not the %s placeholder' % ((x, y, z), placeholder))
            continue
        found += 1
        facing = dict(props)['facing']
        dx, dz = SIDES[facing]
        if not full(site.get(x - dx, y, z - dz)):
            problems.append('inscription at %s has no wall behind it' % ((x, y, z),))
    if not low <= found <= high:
        problems.append('%d placeholder inscriptions, wanted %d..%d' % (found, low, high))
    return problems


FLOOR_PROPS = ('_carpet', 'candle', 'roots', 'fungus', '_skull', 'specimen_jar', 'basin', 'lectern', 'campfire',
               'field_case', 'decorated_pot', '_banner', 'lily_pad', 'bog_body', 'mushroom', 'escritoire',
               'clairaudiograph')


def mount_problems(site):
    problems = []
    for (x, y, z), (name, props, nbt) in site.blocks.items():
        p = dict(props)
        below = site.get(x, y - 1, z)
        if name.endswith(FLOOR_PROPS) and name != 'minecraft:hanging_roots':
            if name == 'minecraft:lily_pad':
                if below != WATER:
                    problems.append('lily pad at %s is not on water' % ((x, y, z),))
            elif name == 'minecraft:crimson_roots' or name == 'minecraft:crimson_fungus':
                if below not in ('minecraft:crimson_nylium', 'minecraft:soul_soil', 'minecraft:netherrack'):
                    problems.append('%s at %s is not on nylium' % (name, (x, y, z)))
            elif below in (None, AIR, WATER):
                problems.append('%s at %s stands on %s' % (name, (x, y, z), below))
        if name == 'hemomancy:humoral_barometer':
            dx, dz = SIDES[p['facing']]                       # hangs on the wall it faces
            if not full(site.get(x + dx, y, z + dz)):
                problems.append('barometer at %s has no wall' % ((x, y, z),))
        if name == 'hemomancy:adipocere_crust':
            dx, dz = SIDES[p['facing']]                       # grows on the wall behind it
            if not full(site.get(x - dx, y, z - dz)):
                problems.append('adipocere crust at %s has no wall' % ((x, y, z),))
        if name.endswith('lantern') and p.get('hanging') == 'true':
            if site.get(x, y + 1, z) in (None, AIR):
                problems.append('hanging lantern at %s hangs from nothing' % ((x, y, z),))
        if name == 'minecraft:hanging_roots' and not full(site.get(x, y + 1, z)):
            problems.append('hanging roots at %s hang from nothing' % ((x, y, z),))
        if name == 'minecraft:barrel' and nbt is not None and 'LootTable' in nbt:
            ns, path = str(nbt['LootTable']).split(':')
            if not os.path.exists(os.path.join(LOOT, ns, 'loot_table', path + '.json')):
                problems.append('barrel at %s names a missing loot table %s' % ((x, y, z), nbt['LootTable']))
    return problems


def holds_water(site, x, y, z):
    """Whether the cell can sit beside water without letting it out: template water, a waterlogged block, or a full
    or water-tight block the template writes."""
    entry = site.blocks.get((x, y, z))
    if entry is None:
        return False
    name, props, _ = entry
    if name == AIR:
        return False
    return True


def water_problems(site):
    problems = []
    for (x, y, z), (name, props, _nbt) in site.blocks.items():
        wet = name == WATER or dict(props).get('waterlogged') == 'true'
        if not wet:
            continue
        for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, -1, 0)):
            if not holds_water(site, x + dx, y + dy, z + dz):
                problems.append('water at %s leaks toward %s' % ((x, y, z), (x + dx, y + dy, z + dz)))
                break
    for (x, y, z), (name, _props, _nbt) in site.blocks.items():
        if name != AIR or y > site.ground:
            continue
        for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, 1, 0), (0, -1, 0)):
            other = site.blocks.get((x + dx, y + dy, z + dz))
            if other is None:
                problems.append('sunken air at %s opens onto void at %s' % ((x, y, z), (x + dx, y + dy, z + dz)))
                break
            if other[0] == WATER or dict(other[1]).get('waterlogged') == 'true':
                if dy != -1:
                    problems.append('sunken air at %s touches water' % ((x, y, z),))
                    break
    return problems


def all_problems(name, site, placeholder):
    return ['%s: %s' % (name, p) for p in state_problems(site) + placeholder_problems(site, placeholder)
            + mount_problems(site) + water_problems(site)]

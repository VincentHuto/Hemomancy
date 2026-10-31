"""The Hematic Order's building kit for its surface sites (the Blood Temple and the Harbinger Outpost), painted on the
mausoleum kit's Room canvas.

The Order builds in deepslate (footings, plinths, curtain walls and floors) dressed with polished venous stone (quoins,
bands, sills and the walk's inlays), bone brick (trims, capitals, keystones and the infill of its timber frames),
blood-wood (posts and beams), dark lacquer (roofs, floors and furniture) and hematic iron (plating, ridges, bars,
chains and lanterns). Light is a crimson reliquary sconce on a wall, a hematic lantern on a chain or a post, or a lit
Iron Brazier, which the structure's afterPlace rekindles (a rotated brazier loads cold). Nothing here comes from the
Nether, and nothing is copper, brass, bronze, silver or gold (the Unstained's metals).

Every placed state carries every property its block has (jigsaw pieces are placed with the shape they were saved
with), and check() refuses a template whose palette names a property its block lacks, a block of the Nether or a
forbidden metal, a vanilla block not listed here, or a loot table that does not exist.
"""
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from nbtlib import Compound, List, String  # noqa: E402

from mausoleum import checks  # noqa: E402
from mausoleum.kit import AIR, OPPOSITE, STEP, Room, connect_walls  # noqa: E402

__all__ = ['AIR', 'OPPOSITE', 'STEP', 'Room']

# --- the palette ------------------------------------------------------------------------------------------------------

# Deepslate: footings, plinths, curtain walls, floors.
COBBLED = 'minecraft:cobbled_deepslate'
DEEP = 'minecraft:deepslate_bricks'
DEEP_CRACKED = 'minecraft:cracked_deepslate_bricks'
DEEP_STAIRS = 'minecraft:deepslate_brick_stairs'
DEEP_SLAB = 'minecraft:deepslate_brick_slab'
DEEP_WALL = 'minecraft:deepslate_brick_wall'
TILE = 'minecraft:deepslate_tiles'
TILE_CRACKED = 'minecraft:cracked_deepslate_tiles'
TILE_STAIRS = 'minecraft:deepslate_tile_stairs'
TILE_SLAB = 'minecraft:deepslate_tile_slab'
TILE_WALL = 'minecraft:deepslate_tile_wall'
POLISHED = 'minecraft:polished_deepslate'
POLISHED_STAIRS = 'minecraft:polished_deepslate_stairs'
POLISHED_SLAB = 'minecraft:polished_deepslate_slab'
CHISELED_DEEP = 'minecraft:chiseled_deepslate'
# Polished venous stone: quoins, bands, sills, inlays.
CUT = 'hemomancy:polished_venous_stone_bricks'
CUT_CRACKED = 'hemomancy:cracked_polished_venous_stone_bricks'
CUT_SMOOTH = 'hemomancy:polished_venous_stone'
CUT_CHISELED = 'hemomancy:chiseled_polished_venous_stone'
CUT_STAIRS = 'hemomancy:polished_venous_stone_brick_stairs'
CUT_SLAB = 'hemomancy:polished_venous_stone_brick_slab'
CUT_WALL = 'hemomancy:polished_venous_stone_brick_wall'
SMOOTH_STAIRS = 'hemomancy:polished_venous_stone_stairs'
SMOOTH_SLAB = 'hemomancy:polished_venous_stone_slab'
VENOUS = 'hemomancy:venous_stone'
VENOUS_STAIRS = 'hemomancy:venous_stone_stairs'
VENOUS_SLAB = 'hemomancy:venous_stone_slab'
VENOUS_WALL = 'hemomancy:venous_stone_wall'
# Bone brick: trims, capitals, keystones, timber infill.
BONE = 'hemomancy:bone_bricks'
BONE_CRACKED = 'hemomancy:cracked_bone_bricks'
BONE_CHISELED = 'hemomancy:chiseled_bone_bricks'
BONE_STAIRS = 'hemomancy:bone_brick_stairs'
BONE_SLAB = 'hemomancy:bone_brick_slab'
BONE_WALL = 'hemomancy:bone_brick_wall'
# Wood: blood-wood frames, lacquer roofs and floors.
LOG = 'hemomancy:blood_wood_log'
PLANKS = 'hemomancy:blood_wood_planks'
LAC = 'hemomancy:dark_lacquer_planks'
LAC_STAIRS = 'hemomancy:dark_lacquer_planks_stairs'
LAC_SLAB = 'hemomancy:dark_lacquer_planks_slab'
# Hematic iron.
PLATING = 'hemomancy:hematic_iron_plating'
PLATING_STAIRS = 'hemomancy:hematic_iron_plating_stairs'
PLATING_SLAB = 'hemomancy:hematic_iron_plating_slab'
PLATING_WALL = 'hemomancy:hematic_iron_plating_wall'
IRON_CHISELED = 'hemomancy:chiseled_hematic_iron_block'
IRON_BLOCK = 'hemomancy:hematic_iron_block'
PILLAR = 'hemomancy:hematic_iron_pillar'
BARS = 'hemomancy:hematic_iron_bars'
CHAIN = 'hemomancy:hematic_iron_chain'
LANTERN = 'hemomancy:hematic_lantern'
TRAPDOOR = 'hemomancy:hematic_iron_trapdoor'
# Glass and set pieces.
GLASS = 'hemomancy:sanguine_glass'
SCONCE = 'hemomancy:reliquary_sconce'
BRAZIER = 'hemomancy:iron_brazier'
CANDLE = 'hemomancy:mnemonic_candle'
JAR = 'hemomancy:specimen_jar'
GUTTER = 'hemomancy:blood_gutter'
BASIN = 'hemomancy:venous_basin'
PACKED_ASH = 'hemomancy:packed_ash'

# What a speckle may swap in to break a flat face, by block.
VARIANTS = {
    DEEP: [DEEP_CRACKED], DEEP_CRACKED: [DEEP], TILE: [TILE_CRACKED], TILE_CRACKED: [TILE], POLISHED: [TILE],
    CUT: [CUT_CRACKED], CUT_CRACKED: [CUT], CUT_SMOOTH: [CUT, VENOUS], VENOUS: [CUT_SMOOTH],
    BONE: [BONE_CRACKED], BONE_CRACKED: [BONE], PLATING: [IRON_CHISELED], COBBLED: [DEEP],
    LAC: [PLANKS], PLANKS: [LAC], PACKED_ASH: [VENOUS], 'minecraft:coarse_dirt': ['minecraft:rooted_dirt'],
    'minecraft:rooted_dirt': ['minecraft:coarse_dirt'],
}

# --- states -----------------------------------------------------------------------------------------------------------

BOOL = ('true', 'false')
FACING4 = ('north', 'east', 'south', 'west')
FACING6 = FACING4 + ('up', 'down')
AXIS = ('x', 'y', 'z')
# Vanilla blocks placed with state that the mausoleum table does not cover.
EXTRA_STATES = {
    BRAZIER: {'face': ('floor', 'wall', 'ceiling'), 'facing': FACING4, 'ritual_phase': ('0', '1', '2'),
              'waterlogged': BOOL},
    LANTERN: {'hanging': BOOL, 'waterlogged': BOOL},
    CHAIN: {'axis': AXIS, 'waterlogged': BOOL},
    BARS: {'axis': AXIS, 'north': BOOL, 'south': BOOL, 'east': BOOL, 'west': BOOL, 'up': BOOL, 'down': BOOL,
           'waterlogged': BOOL},
    'hemomancy:sanguine_pane': {'north': BOOL, 'south': BOOL, 'east': BOOL, 'west': BOOL, 'waterlogged': BOOL},
    'minecraft:red_bed': {'facing': FACING4, 'occupied': BOOL, 'part': ('head', 'foot')},
    'minecraft:lectern': {'facing': FACING4, 'has_book': BOOL, 'powered': BOOL},
    'minecraft:red_wall_banner': {'facing': FACING4},
    'minecraft:white_wall_banner': {'facing': FACING4},
    'minecraft:black_wall_banner': {'facing': FACING4},
    'minecraft:red_banner': {'rotation': tuple(str(i) for i in range(16))},
    'minecraft:ladder': {'facing': FACING4, 'waterlogged': BOOL},
    'minecraft:furnace': {'facing': FACING4, 'lit': BOOL},
    'minecraft:smoker': {'facing': FACING4, 'lit': BOOL},
    'minecraft:anvil': {'facing': FACING4},
    'minecraft:grindstone': {'face': ('floor', 'wall', 'ceiling'), 'facing': FACING4},
    'minecraft:brewing_stand': {'has_bottle_0': BOOL, 'has_bottle_1': BOOL, 'has_bottle_2': BOOL},
    'minecraft:light': {'level': tuple(str(i) for i in range(16)), 'waterlogged': BOOL},
    'minecraft:water_cauldron': {'level': ('1', '2', '3')},
    'minecraft:barrel': {'facing': FACING6, 'open': BOOL},
    'minecraft:chiseled_bookshelf': {'facing': FACING4, **{'slot_%d_occupied' % i: BOOL for i in range(6)}},
}
# Vanilla blocks placed bare (a typo would otherwise load as air).
BARE_VANILLA = {
    AIR, COBBLED, DEEP, DEEP_CRACKED, TILE, TILE_CRACKED, POLISHED, CHISELED_DEEP, 'minecraft:coarse_dirt',
    'minecraft:rooted_dirt', 'minecraft:dirt_path', 'minecraft:gravel', 'minecraft:bookshelf', 'minecraft:crafting_table',
    'minecraft:cartography_table', 'minecraft:smithing_table', 'minecraft:red_wool', 'minecraft:black_wool',
    'minecraft:red_carpet', 'minecraft:black_carpet', 'minecraft:gray_carpet', 'minecraft:cauldron', 'minecraft:bone_block',
    'minecraft:hay_block', 'minecraft:target', 'minecraft:cobweb',
}
# Never on Order work: the Nether's blocks, and the Unstained's metals.
FORBIDDEN = ('nether', 'blackstone', 'basalt', 'shroomlight', 'crimson', 'warped', 'soul', 'glowstone', 'magma',
             'copper', 'brass', 'bronze', 'silver', 'gold', 'gilded')


def stairs(facing, half='bottom', shape='straight'):
    return {'facing': facing, 'half': half, 'shape': shape, 'waterlogged': 'false'}


def slab(kind='bottom'):
    return {'type': kind, 'waterlogged': 'false'}


def wall():
    """A wall block's state before finish() joins it."""
    return {'up': 'true', 'north': 'none', 'south': 'none', 'east': 'none', 'west': 'none', 'waterlogged': 'false'}


def trapdoor(facing, half='top', open_=False):
    return {'facing': facing, 'half': half, 'open': 'true' if open_ else 'false', 'powered': 'false',
            'waterlogged': 'false'}


def h(*values):
    """Deterministic 0..1 noise for a named cell."""
    return random.Random('/'.join(map(str, values))).random()


# --- the canvas -------------------------------------------------------------------------------------------------------

class Piece(Room):
    """A template with the Order's helpers. Out-of-bounds writes raise, as on the Room."""

    def __init__(self, name, size, seed=None):
        super().__init__(*size)
        self.name = name
        self.rng = random.Random(seed or name)

    def box(self, x0, x1, y0, y1, z0, z1, block):
        """fill() that also takes None (leave the cell) from a callable."""
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    value = block(x, y, z) if callable(block) else block
                    if value is None:
                        continue
                    if isinstance(value, tuple):
                        self.set(x, y, z, *value)
                    else:
                        self.set(x, y, z, value)

    def put(self, x, y, z, name, props=None, nbt=None):
        """set() for a block whose waterlogged/wall state the helpers fill in."""
        props = dict(props or {})
        if name.endswith('_wall') and 'up' not in props:
            props.update(wall())
        if name in (LANTERN, CHAIN, CANDLE, JAR, BRAZIER, BARS, 'minecraft:ladder', 'hemomancy:sanguine_pane',
                    'hemomancy:harbinger_escritoire', 'hemomancy:warp_chair_filler', 'hemomancy:filler_block') \
                and 'waterlogged' not in props:
            props['waterlogged'] = 'false'
        self.set(x, y, z, name, props, nbt)

    def connector(self, x, y, z, facing, name, pool, target, final=AIR):
        """A side connector of the outpost's family: the jigsaw faces `facing`, rollable, priority 0."""
        self.jigsaw(x, y, z, '%s_up' % facing, 'hemomancy:harbinger_outpost/%s' % name,
                    'hemomancy:harbinger_outpost/%s' % target, pool, final=final)

    # --- props --------------------------------------------------------------------------------------------------------

    def lantern(self, x, y, z, hanging=True):
        self.put(x, y, z, LANTERN, {'hanging': 'true' if hanging else 'false'})

    def chain(self, x, y, z, axis='y'):
        self.put(x, y, z, CHAIN, {'axis': axis})

    def hung_lantern(self, x, y_top, z, drop):
        """A chain hung from the ceiling cell over y_top, `drop` links long, with a lantern at its end."""
        for y in range(y_top, y_top - drop, -1):
            self.chain(x, y, z)
        self.lantern(x, y_top - drop, z)

    def sconce(self, x, y, z, facing, flame='crimson'):
        """A reliquary sconce in the open cell (x, y, z), its back on the wall behind it, facing out of that wall."""
        self.set(x, y, z, SCONCE, {'facing': facing, 'flame': flame})

    def brazier(self, x, y, z, facing='south', face='floor'):
        """A lit Iron Brazier (afterPlace rekindles one a rotation put out)."""
        self.put(x, y, z, BRAZIER, {'face': face, 'facing': facing, 'ritual_phase': '1'})

    def candle(self, x, y, z):
        self.put(x, y, z, CANDLE, {'lit': 'true'})

    def jar(self, x, y, z, facing):
        self.put(x, y, z, JAR, {'facing': facing})

    def banner(self, x, y, z, facing, color='red', standing=False):
        """A vein banner: the Order's red field with the Hemomancy veins (`color` the field; the veins are white on red
        and red on white or black). Wall banners hang on the wall behind `facing`; a standing one takes a rotation."""
        veins = 'white' if color == 'red' else 'red'
        nbt = Compound({'id': String('minecraft:banner'), 'patterns': List[Compound]([
            Compound({'color': String(veins), 'pattern': String('hemomancy:hemomancy_veins')})])})
        if standing:
            self.set(x, y, z, 'minecraft:%s_banner' % color, {'rotation': str(facing)}, nbt)
        else:
            self.set(x, y, z, 'minecraft:%s_wall_banner' % color, {'facing': facing}, nbt)

    def barrel(self, x, y, z, facing='up', loot=None):
        nbt = Compound({'id': String('minecraft:barrel')})
        if loot:
            nbt['LootTable'] = String(loot)
        self.set(x, y, z, 'minecraft:barrel', {'facing': facing, 'open': 'false'}, nbt)

    def bars(self, x, y, z, axis='y'):
        self.put(x, y, z, BARS, {'axis': axis})

    def gutter(self, x, y, z, fill):
        """A gutter cell; finish() opens its sides toward its neighbours and toward a basin whose spout faces it."""
        self.set(x, y, z, GUTTER, {'fill': str(fill), 'north': 'false', 'south': 'false', 'east': 'false',
                                   'west': 'false'})

    def basin(self, x, y, z, facing, fill):
        """A venous basin standing on the floor at (x, y, z), its spout toward `facing`; finish() sets it pouring when it
        is brimming (fill 3) over a gutter in the floor in front of it."""
        self.set(x, y, z, BASIN, {'facing': facing, 'fill': str(fill), 'pouring': 'false'})

    def speckle(self, x0, x1, y0, y1, z0, z1, chance=0.15, seed=''):
        """Swaps a fraction of the box's plain blocks for one of their variants."""
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    entry = self.blocks.get((x, y, z))
                    if entry and not entry[1] and entry[2] is None and entry[0] in VARIANTS \
                            and h(self.name, seed, x, y, z) < chance:
                        options = VARIANTS[entry[0]]
                        self.set(x, y, z, options[int(h(self.name, 'pick', x, y, z) * len(options))])


# --- architecture -----------------------------------------------------------------------------------------------------

def hip_roof(p, x0, x1, z0, z1, y, block, cap, fill=None):
    """A hipped roof over the box: a ring of `block` stairs at y on the box's edge facing out, each course drawn in by
    one, until the ridge (a long box) or the peak (a square one) takes `cap` (a slab block on the top course). Under
    every course the cells inside the ring take `fill` (when given) so nothing shows through. Returns the top y."""
    k = 0
    while True:
        ax0, ax1, az0, az1 = x0 + k, x1 - k, z0 + k, z1 - k
        yy = y + k
        if ax1 - ax0 < 1 or az1 - az0 < 1:
            # The last course: one row (or one cell) under a cap.
            for x in range(ax0, ax1 + 1):
                for z in range(az0, az1 + 1):
                    p.set(x, yy, z, cap, slab('bottom') if cap.endswith('_slab') else None)
            return yy
        for x in range(ax0, ax1 + 1):
            for z in range(az0, az1 + 1):
                on_n, on_s, on_w, on_e = z == az0, z == az1, x == ax0, x == ax1
                if not (on_n or on_s or on_w or on_e):
                    if fill:
                        p.set(x, yy, z, fill)
                    continue
                fz = 'north' if on_n else 'south' if on_s else None
                fx = 'west' if on_w else 'east' if on_e else None
                if fz and fx:
                    # An outer corner: the quarter step between its two sides.
                    side = 'left' if {'north': 'west', 'east': 'north', 'south': 'east', 'west': 'south'}[fz] == fx \
                        else 'right'
                    p.set(x, yy, z, block, stairs(fz, 'bottom', 'outer_' + side))
                else:
                    p.set(x, yy, z, block, stairs(fz or fx))
        k += 1


def gable_roof(p, x0, x1, z0, z1, y, block, ridge, along='x', gable=None, fill=None):
    """A gabled roof running `along` x (or z) over the box: courses of `block` stairs rising from both long eaves to a
    ridge of `ridge` slabs. `gable` (a callable (x, y, z) -> block) fills the end walls under the slopes; `fill` the
    attic between them. Returns the ridge's y."""
    if along == 'x':
        lo, hi, a0, a1 = z0, z1, x0, x1
    else:
        lo, hi, a0, a1 = x0, x1, z0, z1
    k = 0
    while hi - lo - 2 * k >= 1:
        yy = y + k
        for a in range(a0, a1 + 1):
            for c, facing in ((lo + k, 'north' if along == 'x' else 'west'), (hi - k, 'south' if along == 'x' else 'east')):
                x, z = (a, c) if along == 'x' else (c, a)
                p.set(x, yy, z, block, stairs(facing))
            for c in range(lo + k + 1, hi - k):
                x, z = (a, c) if along == 'x' else (c, a)
                end = a in (a0, a1)
                if end and gable:
                    p.set(x, yy, z, *_as_entry(gable(x, yy, z)))
                elif fill:
                    p.set(x, yy, z, fill)
        k += 1
    yy = y + k
    if hi - lo - 2 * k == 0:
        for a in range(a0, a1 + 1):
            x, z = (a, lo + k) if along == 'x' else (lo + k, a)
            p.set(x, yy, z, ridge, slab('bottom') if ridge.endswith('_slab') else None)
    return yy


def _as_entry(value):
    return value if isinstance(value, tuple) else (value,)


def quoin(x, y, z):
    """Alternating long and short quoin stones up a corner."""
    return CUT_SMOOTH if y % 2 else CUT


def deep_field(p, x, y, z, seed='field'):
    return DEEP_CRACKED if h(p.name, seed, x, y, z) < 0.16 else DEEP


def bone_field(p, x, y, z, seed='bone'):
    return BONE_CRACKED if h(p.name, seed, x, y, z) < 0.14 else BONE


def cut_field(p, x, y, z, seed='cut'):
    return CUT_CRACKED if h(p.name, seed, x, y, z) < 0.18 else CUT


def tile_floor(p, x, y, z, seed='tile'):
    return TILE_CRACKED if h(p.name, seed, x, y, z) < 0.12 else TILE


# --- finishing ----------------------------------------------------------------------------------------------------------

STEP6 = {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0), 'up': (0, 1, 0),
         'down': (0, -1, 0)}
AXIS_OF = {'north': 'z', 'south': 'z', 'east': 'x', 'west': 'x', 'up': 'y', 'down': 'y'}
SIDES4 = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}


def _join_bars(p):
    """Joins each bar to the bars and full faces beside it, as HematicIronBarsBlock.getStateForPlacement would."""
    for (x, y, z) in [pos for pos, entry in p.blocks.items() if entry[0] == BARS]:
        axis = dict(p.blocks[(x, y, z)][1])['axis']
        state = {'axis': axis, 'waterlogged': 'false'}
        for side, (dx, dy, dz) in STEP6.items():
            joined = False
            if AXIS_OF[side] != axis:
                other = p.blocks.get((x + dx, y + dy, z + dz))
                if other and other[0] == BARS:
                    joined = dict(other[1])['axis'] != AXIS_OF[side]
                else:
                    joined = checks.cube(other)
            state[side] = 'true' if joined else 'false'
        p.set(x, y, z, BARS, state)


def _join_panes(p):
    pane = 'hemomancy:sanguine_pane'
    for (x, y, z) in [pos for pos, entry in p.blocks.items() if entry[0] == pane]:
        state = {'waterlogged': 'false'}
        for side, (dx, dz) in SIDES4.items():
            other = p.blocks.get((x + dx, y, z + dz))
            state[side] = 'true' if other and (other[0] in (pane, BARS) or checks.cube(other)) else 'false'
        p.set(x, y, z, pane, state)


def _wire_gutters(p):
    """As BloodGutterBlock.connectsTo: a gutter opens toward a gutter beside it and toward a basin whose spout faces it,
    standing on the floor beside it (one up) or level with it. A brimming basin over a gutter pours
    (VenousBasinBlock.POURING)."""
    for (x, y, z) in [pos for pos, entry in p.blocks.items() if entry[0] == BASIN]:
        state = dict(p.blocks[(x, y, z)][1])
        dx, dz = SIDES4[state['facing']]
        below = p.blocks.get((x + dx, y - 1, z + dz))
        state['pouring'] = 'true' if state['fill'] == '3' and below is not None and below[0] == GUTTER else 'false'
        p.set(x, y, z, BASIN, state)
    for (x, y, z) in [pos for pos, entry in p.blocks.items() if entry[0] == GUTTER]:
        state = dict(p.blocks[(x, y, z)][1])
        for side, (dx, dz) in SIDES4.items():
            n = p.blocks.get((x + dx, y, z + dz))
            above = p.blocks.get((x + dx, y + 1, z + dz))
            joined = n is not None and n[0] == GUTTER
            for basin in (n, above):
                joined |= basin is not None and basin[0] == BASIN and dict(basin[1])['facing'] == OPPOSITE[side]
            state[side] = 'true' if joined or state.get(side) == 'open' else 'false'
        p.set(x, y, z, GUTTER, state)


def finish(p, flats=False):
    """Joins walls, bars, panes and gutters; when `flats`, breaks every exposed 4 x 4 face of one block."""
    walls = sorted(pos for pos, entry in p.blocks.items() if entry[0].endswith('_wall'))
    connect_walls(p, walls)
    _join_bars(p)
    _join_panes(p)
    _wire_gutters(p)
    if not flats:
        return
    rng = random.Random('finish/' + p.name)
    for _ in range(16):
        windows = checks.flat_windows(p, open_outside=True)
        if not windows:
            return
        for name, cells in windows:
            if any(p.get(*c) != name for c in cells) or name not in VARIANTS:
                continue
            p.set(*rng.choice(cells), rng.choice(VARIANTS[name]))


# --- acceptance -------------------------------------------------------------------------------------------------------

def check(p):
    """Every problem with a template, as strings; empty when it may be written."""
    out = []
    seen = set()
    for (name, props, _nbt) in p.blocks.values():
        if (name, props) in seen or name == AIR:
            continue
        seen.add((name, props))
        path = name.split(':', 1)[1]
        if any(word in path for word in FORBIDDEN):
            out.append('forbidden block %s' % name)
        if name in EXTRA_STATES:
            table = EXTRA_STATES[name]
        elif name.startswith('minecraft:') and name not in checks.VANILLA and name not in BARE_VANILLA \
                and name != 'minecraft:jigsaw' and not any(name.endswith(s) for s, _ in checks.FAMILIES):
            out.append('%s: add it to BARE_VANILLA or EXTRA_STATES' % name)
            continue
        else:
            table = checks.expected(name)
            if table is None:
                out.append('%s has no blockstate' % name)
                continue
        for key, value in props:
            if key not in table:
                out.append('%s has no property %s' % (name, key))
            elif value not in table[key]:
                out.append('%s.%s does not accept %s' % (name, key, value))
        missing = set(table) - {k for k, _ in props}
        if missing and name.endswith(('_stairs', '_wall', '_slab')):
            out.append('%s is placed without %s' % (name, ', '.join(sorted(missing))))
    for pos, (name, _props, nbt) in p.blocks.items():
        if nbt is not None and 'LootTable' in nbt:
            ns, path = str(nbt['LootTable']).split(':')
            if not os.path.exists(os.path.join('src', 'main', 'resources', 'data', ns, 'loot_table', path + '.json')):
                out.append('missing loot table %s at %s' % (nbt['LootTable'], pos))
    return ['%s: %s' % (p.name, line) for line in out]

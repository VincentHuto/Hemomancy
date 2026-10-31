"""The wayside kit: a Site canvas on the mausoleum Room (structure NBT writer), and the few state helpers both sites use.

A site is placed by a single-element pool with terrain_adaptation beard_thin, so row `ground` of the template lands on
the column's top ground block (or the top water block of a swamp pool) and every unset cell is structure void: the
world keeps what it has there. A site paints its own ground inside an irregular outline and leaves the rest void, so
the edge blends into whatever terrain it lands on.

Water. The Ossuary Niche's pool element ignores waterlogging, so the template carries its own water: every
waterloggable state it writes says whether it is waterlogged (`wet` cells), and checks.py proves every water cell is
held in by the template on its four sides and below.

Joining. Jigsaw pieces are placed with the shape written (vanilla never recomputes a fence's or a wall's sides), so
finish() joins fences, walls, panes and bars to what stands beside them.
"""
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from mausoleum.kit import Room  # noqa: E402  (the shared structure-NBT canvas)
from nbtlib import Compound, String  # noqa: E402

AIR = 'minecraft:air'
WATER = 'minecraft:water'
PLACEHOLDER_BLOCK = 'hemomancy:blood_echo_inscription'
SIDES = {'north': (0, -1), 'east': (1, 0), 'south': (0, 1), 'west': (-1, 0)}
OPPOSITE = {'north': 'south', 'south': 'north', 'east': 'west', 'west': 'east'}

# Every state of these carries `waterlogged`; put() fills it in from the cell.
WATERLOGGABLE_SUFFIX = ('_stairs', '_slab', '_wall', '_fence', '_trapdoor', '_pane', '_bars')
WATERLOGGABLE = {'minecraft:chain', 'minecraft:mangrove_roots', 'minecraft:hanging_roots', 'minecraft:lantern',
                 'minecraft:campfire', 'minecraft:decorated_pot', 'hemomancy:bog_body', 'hemomancy:mnemonic_candle',
                 'hemomancy:specimen_jar', 'hemomancy:blood_echo_inscription', 'hemomancy:hematic_iron_chain',
                 'hemomancy:hematic_lantern', 'hemomancy:harbinger_escritoire'}

# Names that are not a full cube: nothing joins to them and nothing wall-mounted hangs on them.
NOT_FULL = ('_stairs', '_slab', '_wall', '_fence', '_trapdoor', '_carpet', '_pane', '_bars', '_banner', '_door',
            'lantern', 'chain', 'candle', 'campfire', 'roots', 'fungus', 'lily_pad', '_skull', 'lectern', 'sconce',
            'inscription', 'niche', 'bog_body', 'specimen_jar', 'basin', 'adipocere_crust', 'barometer',
            'escritoire', 'field_case', 'clairaudiograph', 'decorated_pot', 'end_rod', 'moss_carpet', 'air',
            'water', 'jigsaw', 'cobweb', 'brown_mushroom', 'red_mushroom', 'dead_bush')


def full(name):
    return name is not None and not name.endswith(NOT_FULL) and not any(t in name for t in ('carpet', 'roots'))


def waterloggable(name):
    return name in WATERLOGGABLE or name.endswith(WATERLOGGABLE_SUFFIX)


def stairs(facing, half='bottom', shape='straight'):
    return {'facing': facing, 'half': half, 'shape': shape}


def slab(kind='bottom'):
    return {'type': kind}


def trapdoor(facing, half='bottom', open_=False):
    return {'facing': facing, 'half': half, 'open': 'true' if open_ else 'false', 'powered': 'false'}


def h(*values):
    """Deterministic 0..1 hash noise."""
    acc = 2166136261
    for v in values:
        for ch in str(v):
            acc = ((acc ^ ord(ch)) * 16777619) & 0xFFFFFFFF
        acc = ((acc ^ 0x9E) * 16777619) & 0xFFFFFFFF
    return acc / 0xFFFFFFFF


class Site(Room):
    """A wayside template. `ground` is the row that lands on the column's top ground block."""

    def __init__(self, sx, sy, sz, ground, seed):
        super().__init__(sx, sy, sz)
        self.ground = ground
        self.wet = set()
        self.rng = random.Random(seed)

    def put(self, x, y, z, name, props=None, nbt=None):
        if not self.inside(x, y, z):
            return
        props = dict(props or {})
        if waterloggable(name) and 'waterlogged' not in props:
            props['waterlogged'] = 'true' if (x, y, z) in self.wet else 'false'
        self.set(x, y, z, name, props, nbt)

    def put_if_open(self, x, y, z, name, props=None, nbt=None):
        if self.get(x, y, z) in (None, AIR):
            self.put(x, y, z, name, props, nbt)

    def box(self, x0, x1, y0, y1, z0, z1, block):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    value = block(x, y, z) if callable(block) else block
                    if value is None:
                        continue
                    if isinstance(value, tuple):
                        self.put(x, y, z, *value)
                    else:
                        self.put(x, y, z, value)

    def air(self, x, y, z):
        if self.inside(x, y, z):
            self.set(x, y, z, AIR)

    def water(self, x, y, z):
        if self.inside(x, y, z):
            self.wet.add((x, y, z))
            self.set(x, y, z, WATER, {'level': '0'})

    def pick(self, *weighted):
        """One of (weight, value) pairs."""
        total = sum(w for w, _ in weighted)
        roll = self.rng.random() * total
        for w, value in weighted:
            roll -= w
            if roll <= 0:
                return value
        return weighted[-1][1]

    # --- props ----------------------------------------------------------------------------------------------------

    def inscription(self, x, y, z, facing, placeholder):
        """A wall-mounted Blood Echo placeholder in the open cell (x, y, z), its back on the wall behind (the cell
        one step against `facing`); the structure's afterPlace swaps its id for a real memo."""
        self.put(x, y, z, PLACEHOLDER_BLOCK, {'facing': facing}, Compound({
            'id': String('hemomancy:discovery_inscription'), 'InscriptionId': String(placeholder)}))

    def barrel(self, x, y, z, facing, loot=None):
        nbt = Compound({'id': String('minecraft:barrel')})
        if loot:
            nbt['LootTable'] = String(loot)
        self.put(x, y, z, 'minecraft:barrel', {'facing': facing, 'open': 'false'}, nbt)

    def candle(self, x, y, z):
        self.put(x, y, z, 'hemomancy:mnemonic_candle', {'lit': 'true'})

    def skull(self, x, y, z, rotation):
        """A standing skeleton skull; rotation 0 faces south, 4 west, 8 north, 12 east."""
        self.put(x, y, z, 'minecraft:skeleton_skull', {'rotation': str(rotation), 'powered': 'false'})

    def bone(self, x, y, z, axis):
        self.put(x, y, z, 'minecraft:bone_block', {'axis': axis})

    # --- joining --------------------------------------------------------------------------------------------------

    def finish(self):
        """Joins fences, walls, panes and bars to their own kind and to full cubes beside them."""
        families = (('_fence', '_fence'), ('_wall', '_wall'), ('_pane', ('_pane', '_bars')),
                    ('_bars', ('_pane', '_bars')))
        for (x, y, z), (name, props, nbt) in list(self.blocks.items()):
            family = next((f for f in families if name.endswith(f[0])), None)
            if family is None:
                continue
            p = dict(props)
            joined = {}
            for side, (dx, dz) in SIDES.items():
                other = self.get(x + dx, y, z + dz)
                joined[side] = other is not None and (other.endswith(family[1]) or full(other))
            if name.endswith('_wall'):
                for side, link in joined.items():
                    p[side] = 'low' if link else 'none'
                straight = (joined['north'] and joined['south'] and not joined['east'] and not joined['west']) or (
                    joined['east'] and joined['west'] and not joined['north'] and not joined['south'])
                above = self.get(x, y + 1, z)
                p['up'] = 'false' if straight and not full(above) and not (above or '').endswith('_wall') else 'true'
            else:
                for side, link in joined.items():
                    p[side] = 'true' if link else 'false'
            self.blocks[(x, y, z)] = (name, tuple(sorted(p.items())), nbt)


def outline(cx, cz, rx, rz, seed, wobble=0.12):
    """An irregular ellipse: returns d(x, z), the cell's normalised distance from the centre with hash wobble."""
    def d(x, z):
        base = ((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2
        return base ** 0.5 + (h(seed, x, z) - 0.5) * 2 * wobble
    return d

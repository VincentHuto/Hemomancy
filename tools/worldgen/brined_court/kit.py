"""The Brined Court template kit (Track 7; Track 8 may import it): a Room canvas that knows where the sea is.

Water rules. A Court template is placed with `override_liquid_settings: ignore_waterlogging`, so it carries its own
water: every waterloggable state it writes says whether it is waterlogged, and open sea is left as structure void
(unset cells keep the world's water or seabed). A Seat's waterline is the template row that lands at Y62, the top
water layer; cells at or below it are sea unless they are inside a dry room. An air cell below the waterline must
never touch water, a waterlogged block, void (the world's sea) or the template edge on its four sides or above it;
water may lie under air (a moon pool, the undercroft's pool), because still water does not climb. checks.py enforces
that, and CourtSeatTemplateTest repeats it on the saved NBT.

Residents (Track 9) and other runtime spots are jigsaw blocks with pool minecraft:empty and a name under
hemomancy:court_resident/<role> or hemomancy:court_mark/<what>. Jigsaw blocks are replaced by their final state when
the piece is placed (air in a dry room, water in the sea), so they never open a hole the way an ignored data-mode
structure block would. CourtResidentMarkers reads them back from a placed structure.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from mausoleum.kit import Room  # noqa: E402  (the shared structure-NBT canvas)

AIR = 'minecraft:air'
WATER = 'minecraft:water'
RESIDENT = 'hemomancy:court_resident/'
MARK = 'hemomancy:court_mark/'
START = 'hemomancy:court_seat_start'

# Families whose every state carries `waterlogged`; the canvas fills it in.
WATERLOGGABLE_SUFFIX = ('_stairs', '_slab', '_wall', '_fence', '_pane', 'iron_bars', '_chain', '_trapdoor')
WATERLOGGABLE = {'minecraft:sea_pickle', 'minecraft:glow_lichen', 'minecraft:chain', 'minecraft:glass_pane',
                 'minecraft:iron_bars', 'minecraft:ladder', 'minecraft:mangrove_roots', 'minecraft:decorated_pot',
                 'minecraft:hanging_roots', 'hemomancy:sunburst_lamp', 'hemomancy:sunburst_frond',
                 'hemomancy:hydra_crown', 'hemomancy:vent_offering_basin', 'hemomancy:petrified_follower'}
CONNECTS = ('_pane', '_fence', '_wall', 'iron_bars')
# Never in a Court template (copper family, flame light, lanterns).
FORBIDDEN = ('copper', 'torch', 'lantern', 'campfire', 'candle', 'fire', 'soul_')
SIDES = {'north': (0, -1), 'east': (1, 0), 'south': (0, 1), 'west': (-1, 0)}


def h(*values):
    """Deterministic 0..1 hash noise."""
    acc = 2166136261
    for v in values:
        for ch in str(v):
            acc = ((acc ^ ord(ch)) * 16777619) & 0xFFFFFFFF
        acc = ((acc ^ 0x9E) * 16777619) & 0xFFFFFFFF
    return acc / 0xFFFFFFFF


def waterloggable(name):
    return name in WATERLOGGABLE or name.endswith(WATERLOGGABLE_SUFFIX)


class Seat(Room):
    """A Court template. `waterline` is the template row at Y62; `dry` cells are inside air rooms below it."""

    def __init__(self, sx, sy, sz, waterline):
        super().__init__(sx, sy, sz)
        self.waterline = waterline
        self.dry_cells = set()
        self.reserved = set()

    def wet(self, x, y, z):
        return y <= self.waterline and (x, y, z) not in self.dry_cells

    def put(self, x, y, z, name, props=None, nbt=None, wet=None):
        if not self.inside(x, y, z) or (x, y, z) in self.reserved:
            return
        props = dict(props or {})
        if waterloggable(name) and 'waterlogged' not in props:
            props['waterlogged'] = 'true' if (self.wet(x, y, z) if wet is None else wet) else 'false'
        self.set(x, y, z, name, props, nbt)

    def solid(self, x, y, z, name):
        self.put(x, y, z, name)

    def air(self, x, y, z):
        """A dry cell. Below the waterline it joins the dry set, so neighbours written later stay dry too."""
        if not self.inside(x, y, z) or (x, y, z) in self.reserved:
            return
        if y <= self.waterline:
            self.dry_cells.add((x, y, z))
        self.set(x, y, z, AIR)

    def water(self, x, y, z):
        if self.inside(x, y, z) and (x, y, z) not in self.reserved:
            self.set(x, y, z, WATER, {'level': '0'})

    def void(self, x, y, z):
        self.unset(x, y, z)

    def jig(self, x, y, z, orientation, name, final, pool='minecraft:empty', target='minecraft:empty',
            joint='aligned'):
        self.reserved.discard((x, y, z))
        self.jigsaw(x, y, z, orientation, name, target, pool, final=final, joint=joint)
        self.reserved.add((x, y, z))

    def resident(self, x, y, z, role, facing='north'):
        """A Track 9 resident spot: the jigsaw becomes air or water, whichever the cell is."""
        final = WATER if self.wet(x, y, z) else AIR
        self.jig(x, y, z, 'up_' + facing, RESIDENT + role, final)

    def mark(self, x, y, z, what, facing='north'):
        final = WATER if self.wet(x, y, z) else AIR
        self.jig(x, y, z, 'up_' + facing, MARK + what, final)

    # --- shapes ---------------------------------------------------------------------------------------------------

    def box(self, x0, x1, y0, y1, z0, z1, name, **props):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    if callable(name):
                        value = name(x, y, z)
                        if value is None:
                            continue
                        if isinstance(value, tuple):
                            self.put(x, y, z, value[0], value[1])
                        else:
                            self.put(x, y, z, value)
                    else:
                        self.put(x, y, z, name, props or None)

    def hollow(self, x0, x1, y0, y1, z0, z1):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    self.air(x, y, z)

    def finish(self):
        """Dries every waterloggable block that a dry air cell sits beside or under (water would run out of it),
        then connects panes, fences and walls to what stands beside them (jigsaw pieces keep their written shape)."""
        for (x, y, z), (name, props, nbt) in list(self.blocks.items()):
            p = dict(props)
            if p.get('waterlogged') != 'true':
                continue
            for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, -1, 0)):
                if self.get(x + dx, y + dy, z + dz) == AIR:
                    p['waterlogged'] = 'false'
                    self.blocks[(x, y, z)] = (name, tuple(sorted(p.items())), nbt)
                    break
        for (x, y, z), (name, props, nbt) in list(self.blocks.items()):
            if not name.endswith(CONNECTS):
                continue
            p = dict(props)
            sides = {}
            for side, (dx, dz) in SIDES.items():
                other = self.get(x + dx, y, z + dz)
                link = other is not None and other not in (AIR, WATER) and not other.endswith(
                    ('_stairs', '_slab', 'sea_pickle', 'glow_lichen', 'frond', 'jigsaw', 'lamp', 'crown', 'basin',
                     'follower', '_carpet', 'cake', '_bed', 'lily_pad', 'seagrass', 'kelp', 'kelp_plant'))
                sides[side] = link
            if name.endswith('_wall'):
                for side, link in sides.items():
                    p[side] = 'low' if link else 'none'
                straight = (sides['north'] and sides['south'] and not sides['east'] and not sides['west']) or (
                    sides['east'] and sides['west'] and not sides['north'] and not sides['south'])
                above = self.get(x, y + 1, z)
                p['up'] = 'false' if straight and above in (None, AIR, WATER) else 'true'
            else:
                for side, link in sides.items():
                    p[side] = 'true' if link else 'false'
            self.blocks[(x, y, z)] = (name, tuple(sorted(p.items())), nbt)


def stairs(facing, half='bottom', shape='straight'):
    return {'facing': facing, 'half': half, 'shape': shape}


def slab(kind='bottom'):
    return {'type': kind}


def lichen(*faces):
    props = {f: 'false' for f in ('north', 'east', 'south', 'west', 'up', 'down')}
    for f in faces:
        props[f] = 'true'
    return props

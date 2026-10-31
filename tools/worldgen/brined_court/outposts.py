"""The Brined Court's six inner-house outposts (Track 8), one small template each, on Track 7's sea-aware canvas.

Each outpost tells you whose water you are in by its shape, keeps to two or three buildings at most and stays under
32 x 32. Light is biological (sea pickles, glow lichen, shroomlight, the glowing set pieces); no torches, lanterns or
copper. Resident spots for Track 9 are jigsaw marks (kit.py); every outpost carries one house marker at its edge.

Frames. Every outpost is a square, odd-sized template centred on its marker column, so the centre survives the jigsaw's
random rotation; the start jigsaw `hemomancy:court_outpost_start` stands on that column. `d` is height above the
marker row:
- Deep outposts (wake nursery, Line Camp, Screened Cleft, the Bell, Whale Fall): the marker is the first water block
  above the sampled floor (Track 6), so d = 0 is the bottom water row and d = -1 the floor. Rows under d = 0 are
  footings that reach down to whatever the real seabed does under the corners. The whole template is under the sea.
- The Tide Refuge is pinned to the sea surface like the Lighthouse Garden: d = 0 is Y62, the top water layer, and its
  own rock stack reaches eight blocks down, so it stands on any Rockpool Shore floor from Y54 to Y68.

CourtSeatLayout.OUTPOST_BELOW pins each frame's rows under the marker; CourtOutpostTemplateTest reads them back.
"""
import math

from . import kit
from .kit import AIR, WATER, Seat, h, lichen, slab, stairs

OUTPOST_START = 'hemomancy:court_outpost_start'
SEA = 1000                       # a waterline above every deep template: all of it is under the sea

# Track 8's set pieces are waterloggable (the sunburst bloom floats above the water and is not).
kit.WATERLOGGABLE.update({'hemomancy:holdfast_root_mass', 'hemomancy:nest_cup', 'hemomancy:rope_pod',
                          'hemomancy:current_strand', 'hemomancy:fold_veil', 'hemomancy:bell_sleeper',
                          'hemomancy:marrow_plume', 'hemomancy:house_marker', 'hemomancy:relay_marker',
                          'hemomancy:messenger_buoy', 'hemomancy:bone_worm_colony'})

ROCK = ['minecraft:stone', 'minecraft:andesite', 'minecraft:stone', 'minecraft:tuff', 'minecraft:cobblestone']
DEEP_ROCK = ['minecraft:deepslate', 'minecraft:cobbled_deepslate', 'minecraft:tuff', 'minecraft:deepslate']


def pick(palette, *key):
    return palette[int(h(*key) * len(palette)) % len(palette)]


class Outpost:
    """A template in its outpost frame: (dx, d, dz) from the marker column; `below` rows sit under d = 0."""

    site = None
    house = None
    size = 17
    below = 4
    above = 12
    waterline = None             # template row of the sea surface; None means wholly under the sea

    def __init__(self):
        self.c = self.size // 2
        height = self.below + self.above
        line = SEA if self.waterline is None else self.waterline
        self.room = Seat(self.size, height, self.size, line)

    # --- frame ------------------------------------------------------------------------------------------------------

    def at(self, dx, d, dz):
        return self.c + dx, self.below + d, self.c + dz

    def inside(self, dx, d, dz):
        return self.room.inside(*self.at(dx, d, dz))

    def put(self, dx, d, dz, name, props=None):
        self.room.put(*self.at(dx, d, dz), name, props)

    def air(self, dx, d, dz):
        self.room.air(*self.at(dx, d, dz))

    def water(self, dx, d, dz):
        self.room.water(*self.at(dx, d, dz))

    def get(self, dx, d, dz):
        return self.room.get(*self.at(dx, d, dz))

    def resident(self, dx, d, dz, role, facing='north'):
        self.room.resident(*self.at(dx, d, dz), role, facing)

    def mark(self, dx, d, dz, what, facing='north'):
        self.room.mark(*self.at(dx, d, dz), what, facing)

    def house_marker(self, dx, d, dz, facing):
        self.put(dx, d, dz, 'hemomancy:house_marker', {'house': self.house, 'facing': facing})

    def footing(self, dx, dz, top, name):
        """Fills a column from `top` down to the template's bottom row (the seabed under a corner may be lower)."""
        for d in range(-self.below, top + 1):
            self.put(dx, d, dz, name(dx, d, dz) if callable(name) else name)

    def start(self):
        """The start jigsaw on the marker column at d = 0, taking the final state of whatever the cell already is."""
        x, y, z = self.at(0, 0, 0)
        entry = self.room.blocks.get((x, y, z))
        if entry is None:
            final = WATER if self.room.wet(x, y, z) else AIR
        else:
            name, props, _ = entry
            final = name + ('[' + ','.join('%s=%s' % p for p in props) + ']' if props else '')
        self.room.jig(x, y, z, 'up_north', OUTPOST_START, final)

    def build(self):
        self.shape()
        self.start()
        self.room.finish()
        return self.room

    def shape(self):
        raise NotImplementedError


class TideRefuge(Outpost):
    """The Holding Stone's Tide Refuge: a rock stack round a sheltered pocket pool, its seaward entrance braced by
    Holdfast-rooted growth, a shelter room above the tide line on the north side and a safe passage cut through the
    stack east to west. The only outpost seen from the surface."""

    site, house = 'tide_refuge', 'holding_stone'
    size, below, above = 21, 8, 12
    waterline = 8                # d = 0, Y62

    POOL = (0, 1, 4.6)           # centre dx, dz and radius of the pocket pool
    STACK = 9.6

    def top(self, dx, dz):
        """The stack's surface height (d) at a column, or None outside it."""
        r = math.hypot(dx, dz * 1.08)
        angle = math.atan2(dz, dx)
        reach = self.STACK + 1.1 * math.sin(angle * 3 + .7) + (h('tide', dx, dz) - .5) * .9
        if r > reach:
            return None
        north = max(0.0, -dz / 10)
        rim = 2 + round(7 * north) + (1 if h('crag', dx, dz) > .7 else 0) + (2 if h('spire', dx, dz) > .94 else 0)
        if dz > 4 and abs(dx) <= 3:
            rim = min(rim, 1)                    # the seaward lip either side of the entrance
        if r > reach - 1.5:
            rim -= 1
        return rim

    def in_pool(self, dx, dz):
        px, pz, pr = self.POOL
        return math.hypot(dx - px, dz - pz) <= pr

    def shape(self):
        s = self.c
        for dx in range(-s, s + 1):
            for dz in range(-s, s + 1):
                top = self.top(dx, dz)
                if top is None:
                    continue
                channel = abs(dx) <= 1 and dz >= 3
                for d in range(-self.below, top + 1):
                    if (self.in_pool(dx, dz) or channel) and d >= -4:
                        continue
                    self.put(dx, d, dz, pick(ROCK, 'rock', dx, d, dz))
                if not self.in_pool(dx, dz) and not channel and top >= 1 and h('moss', dx, dz) < .35:
                    self.put(dx, top, dz, 'minecraft:mossy_cobblestone')
        self.pool()
        self.entrance()
        self.passage()
        self.shelter()
        self.house_marker(5, (self.top(5, 6) or 0) + 1, 6, 'south')

    def pool(self):
        s = self.c
        for dx in range(-s, s + 1):
            for dz in range(-s, s + 1):
                channel = abs(dx) <= 1 and dz >= 3 and self.top(dx, dz) is not None
                if not (self.in_pool(dx, dz) or channel):
                    continue
                self.put(dx, -5, dz, 'minecraft:gravel' if h('bed', dx, dz) < .4 else 'minecraft:sand')
                for d in range(-4, 1):
                    self.water(dx, d, dz)
                for d in range(1, 9):
                    self.air(dx, d, dz)          # open to the sky; clears any land a high shore puts here
                if h('pickle', dx, dz) < .16:
                    self.put(dx, -4, dz, 'minecraft:sea_pickle', {'pickles': str(2 + int(h('pk', dx, dz) * 2))})
                elif h('grass', dx, dz) < .3:
                    self.put(dx, -4, dz, 'minecraft:seagrass')
        # Slab steps down into the pool on its north and west rims.
        for dx, dz, facing in ((-1, -3, 'south'), (0, -3, 'south'), (1, -3, 'south'), (-4, 0, 'east'), (-4, 1, 'east')):
            self.put(dx, 0, dz, 'minecraft:stone_brick_stairs', stairs(facing))
        # A Holdfast mass cemented into the pool's east wall, gripping under the tide line.
        self.put(4, -1, 1, 'hemomancy:holdfast_root_mass', {'facing': 'west'})
        self.put(4, 0, 2, 'hemomancy:holdfast_root_mass', {'facing': 'west'})

    def entrance(self):
        """The braced seaward entrance: an arch of stone over the channel, a root mass gripping either jamb."""
        for dz in (6, 7):
            for dx in range(-2, 3):
                self.put(dx, 3, dz, 'minecraft:stone_bricks' if abs(dx) < 2 else 'minecraft:mossy_stone_bricks')
            for dx in (-2, 2):
                for d in range(-4, 3):
                    self.put(dx, d, dz, pick(ROCK, 'jamb', dx, d, dz))
            for dx in (-1, 0, 1):
                for d in (1, 2):
                    self.air(dx, d, dz)
        self.put(0, 4, 6, 'minecraft:chiseled_stone_bricks')
        for dx, facing in ((-1, 'east'), (1, 'west')):
            for d in (0, 1):
                self.put(dx, d, 7, 'hemomancy:holdfast_root_mass', {'facing': facing})
        for d in (-1, 1):
            self.put(-2 if d < 0 else 2, 2, 8, 'minecraft:glow_lichen', lichen('north'))
        # The braced member stands in a notch over the west jamb, where it grips the arch.
        for d in (4, 5):
            self.air(-2, d, 7)
        self.put(-2, 3, 7, 'minecraft:stone_bricks')
        self.resident(-2, 4, 7, 'holdfast_braced', 'south')

    def passage(self):
        """The safe passage: a two-high cut through the stack from the west face to the east, north of the pool."""
        for dx in range(-self.c, self.c + 1):
            top = self.top(dx, -4)
            if top is None:
                continue
            for dz in (-4, -3):
                if self.in_pool(dx, dz):
                    continue
                self.put(dx, 0, dz, 'minecraft:stone_bricks' if h('pave', dx, dz) < .7 else 'minecraft:cracked_stone_bricks')
                for d in (1, 2):
                    self.air(dx, d, dz)
                if top >= 3:
                    self.put(dx, 3, dz, pick(ROCK, 'lintel', dx, dz))
            if dx % 4 == 0:
                self.put(dx, 2, -5, 'minecraft:glow_lichen', lichen('south'))

    def shelter(self):
        """The shelter room above the tide line, its door on the passage: a bed, a fungal hearth, dried kelp stores."""
        x0, x1, z0, z1 = -4, 4, -9, -6
        for dx in range(x0 - 1, x1 + 2):
            for dz in range(z0 - 1, z1 + 2):
                edge = dx in (x0 - 1, x1 + 1) or dz in (z0 - 1, z1 + 1)
                self.put(dx, 0, dz, 'minecraft:stone_bricks')
                for d in range(1, 5):
                    if edge:
                        if dz != z1 + 1 or abs(dx) > 1 or d > 2:
                            self.put(dx, d, dz, 'minecraft:stone_bricks' if h('sw', dx, d, dz) < .75
                                     else 'minecraft:mossy_stone_bricks')
                    else:
                        self.air(dx, d, dz)
                self.put(dx, 5, dz, 'minecraft:stone_brick_slab' if not edge else 'minecraft:stone_bricks', slab('top')
                         if not edge else None)
                if not edge and self.get(dx, 6, dz) is None and h('roofmoss', dx, dz) < .5:
                    self.put(dx, 6, dz, 'minecraft:moss_carpet')
        for d in (1, 2):
            self.air(0, d, z1 + 1)               # the door onto the passage
        self.put(-3, 1, -9, 'minecraft:light_gray_bed', {'facing': 'north', 'part': 'head', 'occupied': 'false'})
        self.put(-3, 1, -8, 'minecraft:light_gray_bed', {'facing': 'north', 'part': 'foot', 'occupied': 'false'})
        self.put(3, 1, -8, 'minecraft:shroomlight')                      # the hearth: fungal light, no flame
        for dx, dz in ((2, -8), (4, -8), (3, -9), (3, -7)):
            self.put(dx, 1, dz, 'minecraft:cobblestone_wall')
        self.put(3, 2, -8, 'minecraft:stone_brick_slab', slab('bottom'))
        for dx, dz in ((-4, -7), (-4, -6)):
            self.put(dx, 1, dz, 'minecraft:dried_kelp_block')
        self.put(-4, 2, -7, 'minecraft:dried_kelp_block')
        self.put(1, 1, -9, 'minecraft:barrel', {'facing': 'up', 'open': 'false'})
        self.put(0, 4, -7, 'minecraft:glow_lichen', lichen('up'))
        self.put(-1, 3, -9, 'minecraft:glow_lichen', lichen('north'))
        self.resident(0, 1, -7, 'refuge_keeper', 'south')
        # A cairn on the stack over the shelter, the refuge's mark from the sea, a holdfast gripping its head.
        for d in range(6, 11):
            for dx, dz in ((-1, -8), (0, -8), (-1, -7), (0, -7)):
                if d < 10 or (dx, dz) == (0, -7):
                    self.put(dx, d, dz, 'minecraft:stone_bricks' if h('cairn', dx, d, dz) < .6
                             else 'minecraft:mossy_stone_bricks')
        self.put(0, 11, -7, 'hemomancy:holdfast_root_mass', {'facing': 'south'})
        self.put(-1, 10, -8, 'minecraft:glow_lichen', lichen('down'))


class WakeNursery(Outpost):
    """The Shared Wake's nursery ground: no buildings, a sheltered hollow on the bank ringed by boulders and kelp, its
    floor set with grown nest cups where the young are raised. Rare, and small: the school itself roams (Track 9)."""

    site, house = 'wake_nursery', 'shared_wake'
    size, below, above = 17, 4, 9

    def shape(self):
        s = self.c
        for dx in range(-s, s + 1):
            for dz in range(-s, s + 1):
                r = math.hypot(dx, dz)
                if r > s + .4:
                    continue
                ground = 'minecraft:sand' if h('sand', dx, dz) < .62 else 'minecraft:gravel'
                self.footing(dx, dz, -1, lambda x, d, z: ground if d >= -2 else 'minecraft:tuff')
                for d in range(0, 5):
                    self.water(dx, d, dz)        # the hollow: clears any bank rising into it
                if r >= 6.2:
                    # The ring of boulders and kelp that shelters the ground from the open current.
                    if h('boulder', dx, dz) < .55:
                        tall = 1 + int(h('bh', dx, dz) * 2.2)
                        for d in range(0, tall):
                            self.put(dx, d, dz, pick(['minecraft:tuff', 'minecraft:andesite', 'minecraft:mossy_cobblestone'],
                                                     'b', dx, d, dz))
                    elif h('kelp', dx, dz) < .7:
                        height = 4 + int(h('kh', dx, dz) * 4)
                        for d in range(0, height - 1):
                            self.put(dx, d, dz, 'minecraft:kelp_plant')
                        self.put(dx, height - 1, dz, 'minecraft:kelp', {'age': '25'})
                elif r < 5.8:
                    if h('grass', dx, dz) < .25:
                        self.put(dx, 0, dz, 'minecraft:seagrass')
        # Nest cups in a loose spiral on low pads, the youngest at the sheltered middle.
        for i in range(13):
            a = i * 2.4
            r = 1.4 + i * .34
            dx, dz = round(math.cos(a) * r), round(math.sin(a) * r)
            self.put(dx, 0, dz, 'minecraft:smooth_stone' if h('pad', i) < .5 else 'minecraft:tuff')
            self.put(dx, 1, dz, 'hemomancy:nest_cup')
        for dx, dz in ((0, 0), (3, -2), (-3, 3), (-1, -4), (4, 3)):
            if self.get(dx, 0, dz) == WATER or self.get(dx, 0, dz) == 'minecraft:seagrass':
                self.put(dx, 0, dz, 'minecraft:sea_pickle', {'pickles': '4'})
        self.resident(-2, 1, 2, 'wake_tender', 'east')
        self.resident(3, 1, -3, 'wake_tender', 'west')
        self.put(6, -1, -5, 'minecraft:tuff')
        self.house_marker(6, 0, -5, 'north')
        for d in range(1, 9):
            self.water(6, d, -5)


class LineCamp(Outpost):
    """The Woven Current's Line Camp: a gully (its own two rock walls) strung with living lines, two pods of grown
    tissue hung from them, lamp-lit with the Rope's violet glow. `joined` pods share a strand tube; `parted` pods have
    drifted apart, their broken strands hanging loose. Lines up the walls are climbable."""

    site, house = 'line_camp', 'woven_current'
    size, below, above = 21, 6, 16
    WALL = 7                      # inner face of each gully wall (|dx|)

    def __init__(self, joined):
        self.joined = joined
        super().__init__()

    def shape(self):
        s = self.c
        for dx in range(-s, s + 1):
            for dz in range(-s, s + 1):
                wall = abs(dx) >= self.WALL
                if wall:
                    lip = 11 + int(h('lip', dx, dz) * 3) - (1 if abs(dz) > 8 else 0)
                    top = lip if abs(dx) <= 9 else lip - 2 - int(h('lip2', dx, dz) * 3)
                    self.footing(dx, dz, top, lambda x, d, z: pick(DEEP_ROCK, 'gw', x, d, z))
                    if abs(dx) == self.WALL and h('lich', dx, dz) < .25:
                        d = 2 + int(h('lichd', dx, dz) * 8)
                        self.put(dx - (1 if dx > 0 else -1), d, dz, 'minecraft:glow_lichen',
                                 lichen('east' if dx < 0 else 'west'))
                else:
                    self.footing(dx, dz, -1, lambda x, d, z: 'minecraft:gravel' if d == -1 and h('gf', x, z) < .5
                                 else 'minecraft:sand' if d == -1 else pick(DEEP_ROCK, 'gf', x, d, z))
                    for d in range(0, 15):
                        self.water(dx, d, dz)
        self.lines()
        if self.joined:
            self.pod(-3, 6, 0, 'west')
            self.pod(3, 6, 0, 'east')
            # Joined: two strands run pod to pod, so a member can pass along them.
            for d in (5, 7):
                for dx in (-1, 0, 1):
                    self.put(dx, d, 0, 'hemomancy:current_strand', {'axis': 'x'})
            self.put(0, 6, 0, 'hemomancy:rope_pod')
            self.hang(-3, 10, 0, 13)
            self.hang(3, 10, 0, 13)
        else:
            self.pod(-4, 5, 0, 'west')
            self.pod(4, 7, 0, 'east')
            self.hang(-4, 9, 0, 13)
            self.hang(4, 11, 0, 13)
            # Parted: each pod's strand reaches toward the other and hangs loose short of it.
            for dx in (-2, -1):
                self.put(dx, 5, 0, 'hemomancy:current_strand', {'axis': 'x'})
            for d in (3, 4):
                self.put(-1, d, 0, 'hemomancy:current_strand', {'axis': 'y'})
            for dx in (1, 2):
                self.put(dx, 7, 0, 'hemomancy:current_strand', {'axis': 'x'})
            for d in (5, 6):
                self.put(1, d, 0, 'hemomancy:current_strand', {'axis': 'y'})
        self.resident(-5, 10, -4, 'line_tender', 'east')
        self.resident(3 if self.joined else 4, 6 if self.joined else 7, 0, 'pod_keeper', 'west')
        self.house_marker(0, 0, 9, 'south')

    def lines(self):
        """Two lines across the gully, anchored in bone at each wall, with lamp pods hung under them; climbing lines
        run up both walls."""
        for d, dz in ((11, -4), (11, 4), (13, 0)):
            for dx in range(-self.WALL + 1, self.WALL):
                self.put(dx, d, dz, 'hemomancy:current_strand', {'axis': 'x'})
            for dx in (-self.WALL, self.WALL):
                self.put(dx, d, dz, 'minecraft:bone_block', {'axis': 'x'})
        for dx in (-4, 0, 4):
            self.put(dx, 10, -4, 'hemomancy:rope_pod')
            self.put(dx + 1, 10, 4, 'hemomancy:rope_pod')
        for dx, dz in ((-self.WALL + 1, -6), (self.WALL - 1, 6), (-self.WALL + 1, 7)):
            for d in range(0, 11):
                self.put(dx, d, dz, 'hemomancy:current_strand', {'axis': 'y'})

    def pod(self, cx, cy, cz, door):
        """A lantern pod of grown tissue: a tall ribbed shell, open between its ribs so the lamp inside shows, a
        swim-through door toward the gully wall, a rounded cap and a trailing heel."""
        for dx in range(-2, 3):
            for d in range(-3, 4):
                for dz in range(-2, 3):
                    r = math.sqrt(dx * dx + dz * dz)
                    x, y, z = cx + dx, cy + d, cz + dz
                    if abs(d) == 3:
                        if r <= 1.0:
                            self.put(x, y, z, 'hemomancy:current_tissue')         # cap and heel
                    elif r <= 1.0:
                        self.water(x, y, z)
                    elif r <= 2.3 and not (abs(dx) == 2 and abs(dz) == 2):
                        rib = (dx + dz) % 2 == 0 or abs(d) == 2
                        if rib:
                            self.put(x, y, z, 'hemomancy:current_tissue')
                        else:
                            self.water(x, y, z)                                     # a window between the ribs
        step = -1 if door == 'west' else 1
        for d in (-1, 0):
            self.water(cx + 2 * step, cy + d, cz)
        self.put(cx, cy + 1, cz, 'hemomancy:rope_pod')
        for d in (cy - 5, cy - 4):
            self.put(cx, d, cz, 'hemomancy:current_strand', {'axis': 'y'})

    def hang(self, x, d0, z, d1):
        for d in range(d0, d1):
            self.put(x, d, z, 'hemomancy:current_strand', {'axis': 'y'})


class ScreenedCleft(Outpost):
    """The Veiled Fold's Screened Cleft: a doorway into a canyon wall (the outpost brings its own rock face), almost
    invisible behind a hanging veil. Inside, one host's receiving room; the doors to the house's further rooms stay
    closed to a visitor."""

    site, house = 'screened_cleft', 'veiled_fold'
    size, below, above = 21, 12, 14

    def face(self, dx, d):
        """How far the rock face stands out (dz) at a column and height: ragged, leaning back as it rises."""
        lean = d * .08
        return round(-1 + 1.4 * math.sin(dx * .7 + d * .3) * .7 + (h('face', dx, d) - .5) * 1.2 - lean)

    def shape(self):
        s = self.c
        for dx in range(-s, s + 1):
            crest = 10 + int(h('crest', dx) * 4) - (2 if abs(dx) > 8 else 0)
            for d in range(-self.below, crest + 1):
                front = self.face(dx, d) if d >= 0 else 1
                for dz in range(-s, front + 1):
                    self.put(dx, d, dz, pick(DEEP_ROCK + ['minecraft:blackstone'], 'cliff', dx, d, dz))
        # The apron in front of the face, and open water to the south.
        for dx in range(-s, s + 1):
            for dz in range(2, s + 1):
                self.footing(dx, dz, -1, lambda x, d, z: 'minecraft:gravel' if d == -1 and h('ap', x, z) < .6
                             else pick(DEEP_ROCK, 'ap', x, d, z))
                for d in range(0, 4):
                    self.water(dx, d, dz)
        self.cleft()
        self.room_()
        self.house_marker(-7, 0, 3, 'south')

    def cleft(self):
        """A one-wide slot in the face, the veil hung two blocks in so the opening reads as a shadowed crack."""
        for dz in range(-3, 3):
            for d in range(0, 4 if dz > -2 else 3):
                self.water(0, d, dz)
            self.put(0, -1, dz, 'minecraft:polished_deepslate')
        for d in range(0, 3):
            self.put(0, d, -1, 'hemomancy:fold_veil', {'facing': 'south'})
        self.put(0, 4, 1, 'minecraft:glow_lichen', lichen('up'))

    def room_(self):
        """The receiving room: tiled floor, bone posts, a low table with seats, veils on the walls, and a closed door
        to either side."""
        x0, x1, z0, z1 = -4, 4, -9, -4
        for dx in range(x0, x1 + 1):
            for dz in range(z0, z1 + 1):
                self.put(dx, -1, dz, 'minecraft:deepslate_tiles' if (dx + dz) % 2 else 'minecraft:polished_deepslate')
                for d in range(0, 5):
                    self.water(dx, d, dz)
                self.put(dx, 5, dz, 'minecraft:deepslate_tiles')
        for dx in range(x0 - 1, x1 + 2):
            for dz in range(z0 - 1, z1 + 2):
                if x0 <= dx <= x1 and z0 <= dz <= z1:
                    continue
                for d in range(-1, 6):
                    self.put(dx, d, dz, 'minecraft:polished_deepslate' if d in (-1, 5) else 'minecraft:deepslate_bricks')
        for dz in (-3, -2):
            for d in range(0, 3):
                self.water(0, d, dz)
        for dx, dz in ((x0, z0), (x1, z0), (x0, z1), (x1, z1)):
            for d in range(0, 5):
                self.put(dx, d, dz, 'minecraft:bone_block', {'axis': 'y'})
        # The low table and its seats.
        for dx in (-1, 0, 1):
            self.put(dx, 0, -7, 'minecraft:polished_deepslate_slab', slab('top'))
            self.put(dx, 0, -8, 'minecraft:deepslate_tile_stairs', stairs('south'))
        for dx in (-1, 1):
            self.put(dx, 0, -6, 'minecraft:deepslate_tile_stairs', stairs('north'))
        self.put(0, 1, -7, 'minecraft:sea_pickle', {'pickles': '2'})
        # Veils hung on the side walls; the closed doors behind them.
        for side in (-1, 1):
            wall_x = side * (x1 + 1)
            for dz in (-7, -6):
                for d in (0, 1, 2):
                    self.put(wall_x, d, dz, 'minecraft:deepslate_tiles')
                    self.put(wall_x - side, d, dz, 'hemomancy:fold_veil',
                             {'facing': 'east' if side < 0 else 'west'})
                self.put(wall_x, 3, dz, 'minecraft:chiseled_deepslate')
            # The further room beyond, sealed: it exists, but a guest does not go there.
            for dx in range(1, 4):
                for dz in (-8, -7, -6, -5):
                    for d in range(0, 3):
                        self.water(wall_x + side * dx, d, dz)
            self.put(wall_x + side * 2, 0, -7, 'minecraft:sea_pickle', {'pickles': '1'})
        for dz in (-8, -5):
            self.put(0, 4, dz, 'minecraft:glow_lichen', lichen('up'))
        self.put(0, 2, -9, 'hemomancy:fold_veil', {'facing': 'south'})
        self.resident(0, 0, -5, 'cleft_host', 'south')
        self.resident(3, 0, -8, 'cleft_veiled', 'west')


class StillBell(Outpost):
    """The Still Bell: one sealed diving bell grown from sclerotium (no metal) on a weighted rim of stone legs. Swim in
    under the rim and up through the moon pool into the air pocket: a ledge to stand on, a bed, kelp stores, a fungal
    light in the crown, and sealed sleepers hanging in the upper ring."""

    site, house = 'still_bell', 'still_bell'
    size, below, above = 17, 4, 13

    LEDGE = 2                    # the ledge row; water fills the bell below it and the moon pool at its level
    RIM = 5.5                    # outer radius of the bell's wall

    @staticmethod
    def interior(r, d):
        """Whether a cell is inside the air pocket (d >= 3)."""
        return (3 <= d <= 7 and r <= 4.5) or (d == 8 and r <= 3.6) or (d == 9 and r <= 2.3)

    @staticmethod
    def shell(r, d):
        return (2 <= d <= 7 and r <= 5.5) or (d == 8 and r <= 4.6) or (d == 9 and r <= 3.5) or (d == 10 and r <= 2.4)

    def shape(self):
        s = self.c
        for dx in range(-s, s + 1):
            for dz in range(-s, s + 1):
                r = math.hypot(dx, dz)
                ground = 'minecraft:gravel' if h('g', dx, dz) < .5 else 'minecraft:mud'
                self.footing(dx, dz, -1, lambda x, d, z: ground if d == -1 else pick(DEEP_ROCK, 'bf', x, d, z))
                for d in range(0, 12):
                    if self.interior(r, d):
                        self.air(dx, d, dz)
                    elif r <= 4.5 and d <= self.LEDGE:
                        self.water(dx, d, dz)
                    elif self.shell(r, d):
                        self.put(dx, d, dz, 'hemomancy:sclerotium_shell')
                    elif r <= s + .3 and d <= 1:
                        self.water(dx, d, dz)
                # The weighted rim: eight heavy stone legs under the wall, gaps between them to swim through.
                if 4.6 <= r <= 5.9:
                    if (math.degrees(math.atan2(dz, dx)) + 360 + 11) % 45 < 18:
                        for d in (0, 1):
                            self.put(dx, d, dz, 'minecraft:polished_basalt', {'axis': 'y'})
                        self.put(dx, -1, dz, 'minecraft:polished_blackstone')
        self.ledge()
        self.ring()
        # Weights hung off the rim outside: stone blocks bound in kelp on the seabed.
        for k in range(4):
            a = math.radians(45 + 90 * k)
            dx, dz = round(math.cos(a) * 7), round(math.sin(a) * 7)
            self.put(dx, 0, dz, 'minecraft:polished_blackstone_bricks')
            self.put(dx, 1, dz, 'minecraft:kelp', {'age': '25'})
        for dx, dz in ((-7, 2), (7, -2), (2, 7)):
            self.put(dx, 0, dz, 'minecraft:sea_pickle', {'pickles': '3'})
        self.house_marker(-6, 0, -5, 'north')

    def ledge(self):
        """The walking ledge round the inside wall at the waterline, the moon pool open in its middle."""
        for dx in range(-5, 6):
            for dz in range(-5, 6):
                r = math.hypot(dx, dz)
                if 2.6 <= r <= 4.5:
                    self.put(dx, self.LEDGE, dz, 'minecraft:polished_deepslate' if h('lt', dx, dz) < .7
                             else 'minecraft:deepslate_tiles')
                elif r < 2.6:
                    self.water(dx, self.LEDGE, dz)
                    if h('mp', dx, dz) < .2:
                        self.put(dx, 0, dz, 'minecraft:sea_pickle', {'pickles': '2'})
        # Rest and stores on the ledge: a bed, dried kelp, a barrel; the keeper by the pool.
        self.put(-3, 3, -2, 'minecraft:light_blue_bed', {'facing': 'west', 'part': 'head', 'occupied': 'false'})
        self.put(-2, 3, -2, 'minecraft:light_blue_bed', {'facing': 'west', 'part': 'foot', 'occupied': 'false'})
        self.put(3, 3, -2, 'minecraft:dried_kelp_block')
        self.put(3, 3, -1, 'minecraft:dried_kelp_block')
        self.put(3, 4, -2, 'minecraft:dried_kelp_block')
        self.put(2, 3, 3, 'minecraft:barrel', {'facing': 'up', 'open': 'false'})
        self.put(-1, 3, 3, 'minecraft:moss_carpet')
        self.resident(1, 3, -3, 'bell_keeper', 'south')

    def ring(self):
        """The upper ring at d = 7: four sealed sleepers hung from the dome between bone hooks, two of the hooks
        left for Track 9's sleeping residents; a shroomlight crown in the dome."""
        hooks = [(4, 0), (3, 3), (0, 4), (-3, 3), (-4, 0), (-3, -3), (0, -4), (3, -3)]
        for i, (dx, dz) in enumerate(hooks):
            if i in (0, 2, 4, 6):
                self.put(dx, 7, dz, 'hemomancy:bell_sleeper')        # a body's length, hung from the dome
                continue
            self.put(dx, 7, dz, 'minecraft:bone_block', {'axis': 'y'})
            if i in (1, 5):
                self.resident(dx, 6, dz, 'bell_sleeper')
        self.put(0, 9, 0, 'minecraft:shroomlight')
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            self.put(dx, 8, dz, 'minecraft:glow_lichen', lichen('up'))


class WhaleFall(Outpost):
    """Last Marrow's Whale Fall: one great skeleton on a sediment fan (set-piece bone, laid out as a whole animal, unlike
    the terrain's static fossil ribs). Homes under the ribcage; two hollow jaw bones where rooted elders live, red
    feeding plumes reaching out of their mouths and along the ribs."""

    site, house = 'whale_fall', 'last_marrow'
    size, below, above = 27, 4, 11
    SPINE_D = 3

    def shape(self):
        s = self.c
        for dx in range(-s, s + 1):
            for dz in range(-s, s + 1):
                r = math.hypot(dx * 1.3, dz)
                if r > s + 1:
                    continue
                fan = 'minecraft:mud' if h('fan', dx // 2, dz // 2) < .55 else 'minecraft:clay' if h('cl', dx, dz) < .5 \
                    else 'minecraft:gravel'
                self.footing(dx, dz, -1, lambda x, d, z: fan if d == -1 else 'minecraft:packed_mud' if d >= -2
                             else pick(DEEP_ROCK, 'wf', x, d, z))
                for d in range(0, 9):
                    self.water(dx, d, dz)
        self.spine()
        self.ribs()
        self.skull()
        self.jaw(-3)
        self.jaw(3)
        self.homes()
        self.flukes()
        self.house_marker(-8, 0, -11, 'north')

    def spine(self):
        for dz in range(-12, 9):
            self.put(0, self.SPINE_D, dz, 'minecraft:bone_block', {'axis': 'z'})
            if dz % 2 == 0:
                self.put(0, self.SPINE_D + 1, dz, 'minecraft:bone_block', {'axis': 'y'})
                if -8 <= dz <= 4:
                    self.put(0, self.SPINE_D + 2, dz, 'minecraft:bone_block', {'axis': 'y'})
                for side in (-1, 1):
                    self.put(side, self.SPINE_D, dz, 'minecraft:bone_block', {'axis': 'x'})
        # The tail sinks into the sediment toward its end.
        for dz in range(-12, -8):
            self.put(0, self.SPINE_D - 1, dz, 'minecraft:bone_block', {'axis': 'z'})

    def ribs(self):
        """Paired ribs arching out and down from the spine, longest amidships, the lowest ends sunk in the mud."""
        for dz in range(-6, 7, 2):
            length = 7.5 - abs(dz) * .35
            for side in (-1, 1):
                if h('missing', dz, side) < .12:
                    continue                     # a rib already gone to the fall's slow work
                steps = 18
                for i in range(steps + 1):
                    a = i / steps * math.radians(125)
                    x = side * (1.5 + length * math.sin(a))
                    d = self.SPINE_D + length * .62 * math.cos(a)
                    px, pd = round(x), round(d)
                    if pd < -1:
                        continue
                    self.put(px, pd, dz, 'minecraft:bone_block', {'axis': 'y' if a > .9 else 'x'})
                    if i == steps // 2 and h('plume', dz, side) < .5:
                        self.put(px + side, pd, dz, 'hemomancy:marrow_plume')
                if h('worms', dz, side) < .6:
                    self.put(side * round(1.5 + length * .9), 0, dz, 'hemomancy:bone_worm_colony',
                             {'worms': str(2 + int(h('wn', dz, side) * 4))})

    def skull(self):
        """The skull at the south end: a broad mass of bone, hollowed into a home with a blowhole for light."""
        for dx in range(-4, 5):
            for dz in range(9, 13):
                for d in range(0, 5):
                    shape = abs(dx) <= 4 - max(0, d - 2) and (d < 4 or abs(dx) <= 2)
                    if not shape:
                        continue
                    hollow = abs(dx) <= 2 and 10 <= dz <= 11 and 0 <= d <= 2
                    if hollow:
                        self.water(dx, d, dz)
                    elif d >= 3 or abs(dx) >= 3:
                        self.put(dx, d, dz, 'minecraft:calcite' if h('skull', dx, d, dz) < .7 else 'minecraft:diorite')
                    else:
                        self.put(dx, d, dz, 'minecraft:bone_block', {'axis': 'y'})
        for d in range(0, 3):
            self.water(0, d, 9)                  # the opening into the skull from under the ribcage
        self.water(0, 3, 11)
        self.water(0, 4, 11)                     # the blowhole
        self.put(0, 0, 11, 'minecraft:sea_pickle', {'pickles': '3'})
        self.put(-2, 0, 10, 'minecraft:moss_carpet')

    def jaw(self, side):
        """A hollow jaw bone lying forward of the skull: a rooted elder inside, its plumes out of the open end."""
        for dz in range(12, 14):
            for dx in (side - 1, side, side + 1):
                for d in (0, 1, 2):
                    inner = dx == side and d == 1
                    if inner:
                        self.water(dx, d, dz)
                    else:
                        self.put(dx, d, dz, 'minecraft:bone_block', {'axis': 'z'})
        self.resident(side, 1, 12, 'marrow_rooted', 'south')
        # The jaw's far end lies off the template's edge in the sediment; its plumes reach up beside the skull.
        self.put(side + (1 if side > 0 else -1) * 2, 0, 12, 'hemomancy:marrow_plume')
        self.put(side + (1 if side > 0 else -1) * 2, 0, 11, 'hemomancy:marrow_plume')
        self.put(side, 3, 12, 'hemomancy:marrow_plume')

    def homes(self):
        """Homes under the ribcage: low walls of packed mud between the ribs, moss beds, sea pickles."""
        for side in (-1, 1):
            for dz in range(-4, 5):
                x = side * 4
                if dz in (-4, 4):
                    for dx in range(1, 5):
                        self.put(side * dx, 0, dz, 'minecraft:mud_bricks')
                else:
                    self.put(x, 0, dz, 'minecraft:mud_bricks' if dz % 3 else 'minecraft:mud_brick_wall')
            for dz in (-2, 2):
                self.put(side * 2, 0, dz, 'minecraft:moss_carpet')
            self.put(side * 2, 0, 0, 'minecraft:sea_pickle', {'pickles': '4'})
            self.put(side * 3, 0, -3, 'hemomancy:marrow_plume')
        self.resident(-2, 0, -1, 'marrow_carer', 'east')
        self.resident(2, 0, 1, 'marrow_carer', 'west')

    def flukes(self):
        """The tail flukes splayed on the sediment at the north end."""
        for side in (-1, 1):
            for i in range(5):
                self.put(side * (1 + i), 0, -12 + (i // 2), 'minecraft:bone_block', {'axis': 'x'})


PIECES = [
    ('tide_refuge', 'refuge', TideRefuge),
    ('wake_nursery', 'nursery', WakeNursery),
    ('line_camp', 'joined', lambda: LineCamp(True)),
    ('line_camp', 'parted', lambda: LineCamp(False)),
    ('screened_cleft', 'cleft', ScreenedCleft),
    ('still_bell', 'bell', StillBell),
    ('whale_fall', 'fall', WhaleFall),
]

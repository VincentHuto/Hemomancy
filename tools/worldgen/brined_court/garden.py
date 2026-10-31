"""The Lighthouse Garden (the Giving Light's Seat): two jigsaw pieces round one centre column.

`garden` (the start piece) is the tide rock with everything above the undercroft's deep tiers: the lighthouse with
its bone-and-glass lantern room and living Sunburst lamp, the terraced gardens stepping south down into the reef, the
arbors, the guest house, the landing, and the undercroft's upper tiers. `undercroft` hangs under it by an aligned
down/up jigsaw pair: the deep moss tiers, the petrified followers on the lowest walls and the pool round the moss
island where Track 10's Marimo presence sits.

Frame. d is height above the waterline (d = 0 is Y62, the top water layer); dx/dz run from the centre column, north
is -z. The rock reaches 24 below the waterline so the Garden stands on the shelf wherever its floor is (a reef shelf
lies at about Y40 to Y58); rock under the seabed replaces terrain, and open sea is structure void.

The undercroft is a tube coliseum: one sealed shaft of radius 10.5 inside the rock, ring galleries every 4 blocks
(floors at d = 3, -1, -5, -9) stepping round an open centre, a pool at the bottom (d = -13..-12) round a moss island.
About eight blocks of it stand above the waterline and twelve below: damp, never drowned. It is entered from the
island top through a hatch in its roof under a frond-roofed kiosk east of the lighthouse. The deeper the tier, the more moss.
"""
import math

from .kit import AIR, Seat, h, lichen, slab, stairs

SIZE = 47
C = 23
DEPTH = 24                 # rows under the waterline in the garden piece
HEIGHT = DEPTH + 41        # up to the lighthouse finial
U_SIZE = 25
U_C = 12
U_TOP = -5                 # the undercroft piece's top row (d); its bottom is the coliseum floor
U_BOTTOM = -14

SHAFT = 10.5               # the coliseum's open radius; its wall runs to 12.5
LEDGE = 7.5                # gallery ledges run from here to the wall
TIERS = (3, -1, -5, -9)    # gallery floors (d)
CEILING = 7
POOL = (-13, -12)
ISLAND_TOP = 7

STONE = ['minecraft:stone_bricks', 'minecraft:stone_bricks', 'minecraft:cracked_stone_bricks',
         'minecraft:mossy_stone_bricks']
ROCK = ['minecraft:stone', 'minecraft:andesite', 'minecraft:stone', 'minecraft:tuff', 'minecraft:diorite']


def radius(dx, dz):
    return math.hypot(dx, dz)


def rock_top(dx, dz):
    """The tide rock's surface height (d) at a column, or None where the sea is. South is the terrace slope."""
    r = radius(dx, dz)
    angle = math.atan2(dz, dx)
    wobble = 1.6 * math.sin(angle * 3 + 1.1) + 1.1 * math.sin(angle * 5 + .4) + (h('rim', dx, dz) - .5) * .8
    west_lobe = 4.5 * max(0.0, -math.cos(angle)) ** 2       # the guest house stands on a western lobe
    reach = 13 + wobble + west_lobe
    if r <= 7:
        return ISLAND_TOP
    if r <= reach:
        return ISLAND_TOP - round((r - 7) / max(reach - 7, 1) * 6)
    if r <= reach + 4:
        return 1 - round((r - reach) * 2.2)
    return None


def terrace(dx, dz):
    """The southern terraces: (floor d, ring index) or None outside the terrace quadrant."""
    if dz <= 2:
        return None
    angle = math.degrees(math.atan2(dz, dx))
    if not 35 <= angle <= 145:
        return None
    r = radius(dx, dz)
    for i, (r0, r1, d) in enumerate(((8, 10.5, 5), (10.5, 13, 3), (13, 15.5, 1), (15.5, 18, -1), (18, 20.5, -3),
                                     (20.5, 22.5, -5))):
        if r0 <= r < r1:
            return d, i, r1 - r < 1
    return None


def rock_block(dx, y, dz):
    n = h('rock', dx, y, dz)
    return ROCK[int(n * len(ROCK))]


class Garden:
    def __init__(self):
        self.room = Seat(SIZE, HEIGHT, SIZE, DEPTH)

    def at(self, dx, d, dz):
        return C + dx, DEPTH + d, C + dz

    def put(self, dx, d, dz, name, props=None):
        if self.in_undercroft_piece(dx, d, dz):
            return
        x, y, z = self.at(dx, d, dz)
        self.room.put(x, y, z, name, props)

    def air(self, dx, d, dz):
        if not self.in_undercroft_piece(dx, d, dz):
            self.room.air(*self.at(dx, d, dz))

    def get(self, dx, d, dz):
        return self.room.get(*self.at(dx, d, dz))

    def in_undercroft_piece(self, dx, d, dz):
        return abs(dx) <= U_C and abs(dz) <= U_C and U_BOTTOM <= d <= U_TOP

    # --- the tide rock and the terraces -----------------------------------------------------------------------------

    def rock(self):
        for dx in range(-C, C + 1):
            for dz in range(-C, C + 1):
                top = rock_top(dx, dz)
                t = terrace(dx, dz)
                if t:
                    top = t[0] if top is None else min(top, t[0]) if radius(dx, dz) > 8 else top
                if top is None:
                    continue
                for d in range(-DEPTH, top + 1):
                    if self.in_undercroft_piece(dx, d, dz):
                        continue
                    if d == top and d > 0 and not t:
                        name = 'minecraft:moss_block' if h('turf', dx, dz) < .55 else rock_block(dx, d, dz)
                    elif d == top and d <= 0:
                        name = 'hemomancy:sunburst_crust' if h('crust', dx, dz) < .45 else rock_block(dx, d, dz)
                    elif d >= top - 2 and h('crustwall', dx, d, dz) < .12:
                        name = 'hemomancy:sunburst_crust'
                    else:
                        name = rock_block(dx, d, dz)
                    self.put(dx, d, dz, name)

    def terraces(self):
        for dx in range(-C, C + 1):
            for dz in range(-C, C + 1):
                t = terrace(dx, dz)
                if not t or radius(dx, dz) <= 8:
                    continue
                d, i, rim = t
                above_water = d >= 1
                if rim:
                    # The retaining wall at each step's edge: dressed stone, crusted gold where the sea washes it.
                    self.put(dx, d, dz, 'hemomancy:sunburst_crust' if not above_water and h('rimc', dx, dz) < .5
                             else 'minecraft:stone_bricks' if h('rimb', dx, dz) < .7 else 'minecraft:mossy_stone_bricks')
                    if above_water and h('rail', dx, dz) < .8:
                        self.put(dx, d + 1, dz, 'minecraft:mossy_stone_brick_wall')
                    continue
                self.put(dx, d, dz, 'minecraft:moss_block' if above_water else
                         ('hemomancy:sunburst_crust' if h('bed', dx, dz) < .6 else 'hemomancy:erythrocoral_block'))
                n = h('plant', dx, dz)
                if above_water:
                    if n < .22:
                        self.put(dx, d + 1, dz, 'hemomancy:sunburst_frond')
                    elif n < .42:
                        self.put(dx, d + 1, dz, 'minecraft:moss_carpet')
                    elif n < .5:
                        self.put(dx, d + 1, dz, 'minecraft:fern')
                else:
                    if n < .3:
                        self.put(dx, d + 1, dz, 'hemomancy:sunburst_frond')
                    elif n < .5:
                        self.put(dx, d + 1, dz, 'minecraft:seagrass')
                    elif n < .56 and d + 2 <= 0:
                        self.put(dx, d + 1, dz, 'minecraft:kelp_plant')
                        self.put(dx, d + 2, dz, 'minecraft:kelp', {'age': '20'})
                    elif n < .62:
                        self.put(dx, d + 1, dz, 'minecraft:sea_pickle', {'pickles': str(1 + int(n * 40) % 4)})
        # Floating blooms on the water beside the lowest dry terrace.
        for dx in range(-C, C + 1):
            for dz in range(14, C + 1):
                if self.get(dx, 0, dz) is None and self.get(dx, 1, dz) is None and h('lily', dx, dz) < .07:
                    self.put(dx, 1, dz, 'minecraft:lily_pad')

    def tables(self):
        """Communal tables and drying racks on the top terrace (d = 5)."""
        for x0, z0 in ((-3, 9), (2, 9)):
            for dx in range(x0, x0 + 3):
                self.put(dx, 6, z0, 'hemomancy:dark_lacquer_planks_slab', slab('top'))
                self.put(dx, 6, z0 - 1, 'hemomancy:dark_lacquer_planks_stairs', stairs('north'))
            self.put(x0 + 1, 7, z0, 'minecraft:cake')
        for dx in (-6, 6):
            for dz in (7, 8):
                self.put(dx, 6, dz, 'minecraft:dark_oak_fence')
                self.put(dx, 7, dz, 'minecraft:dried_kelp_block')
        self.room.resident(*self.at(0, 6, 7), 'gardener')
        self.room.resident(*self.at(4, 4, 12), 'gardener')

    def arbors(self):
        """Living arbors on the terraces: crusted posts arched over with frond, an anchored member at the heart of
        each (a Track 9 resident). One of them is the arbor member who can be seen withdrawing."""
        for dx, d, dz, role in ((-9, 3, 9, 'arbor_anchored'), (9, 3, 9, 'arbor_withdrawing'),
                                (-11, 1, 8, 'arbor_anchored'), (9, 1, 10, 'arbor_anchored')):
            for sx in (-1, 1):
                for k in range(1, 4):
                    self.put(dx + sx, d + k, dz, 'hemomancy:sunburst_crust')
            for sx in (-1, 0, 1):
                self.put(dx + sx, d + 4, dz, 'minecraft:moss_block')
                self.put(dx + sx, d + 5, dz, 'hemomancy:sunburst_frond')
            self.put(dx, d + 3, dz, 'minecraft:hanging_roots')
            self.put(dx - 1, d + 1, dz + 1, 'hemomancy:sunburst_frond')
            self.put(dx + 1, d + 1, dz - 1, 'hemomancy:sunburst_frond')
            self.room.resident(*self.at(dx, d + 1, dz), role, 'south')

    # --- the lighthouse ---------------------------------------------------------------------------------------------

    def lighthouse(self):
        base, top = ISLAND_TOP + 1, ISLAND_TOP + 21           # tower walls d 8..28
        gallery = top + 1
        for dx in range(-6, 7):
            for dz in range(-6, 7):
                r = radius(dx, dz)
                if r < 5.5:
                    self.put(dx, ISLAND_TOP, dz, 'minecraft:polished_andesite' if r < 3.5 else 'minecraft:stone_bricks')
                for d in range(base, top + 1):
                    if 3.5 <= r < 4.6:
                        band = (d - base) % 6
                        if band == 4:
                            name = 'hemomancy:sunburst_crust'
                        elif band == 5:
                            name = 'minecraft:calcite'
                        else:
                            name = STONE[int(h('tower', dx, d, dz) * len(STONE))]
                        self.put(dx, d, dz, name)
                    elif r < 3.5:
                        self.air(dx, d, dz)
                if r < 1.5:
                    for d in range(base, top + 1):
                        self.put(dx, d, dz, 'minecraft:chiseled_stone_bricks' if d % 5 == 0 else 'minecraft:stone_bricks')
        # Door (north) and windows on the four faces.
        for d in (base, base + 1):
            self.air(0, d, -4)
            self.air(0, d, -3)
        self.put(0, base + 2, -4, 'minecraft:chiseled_stone_bricks')
        for d in range(base + 5, top - 1, 6):
            for dx, dz in ((0, 4), (4, 0), (-4, 0), (0, -4)):
                self.put(dx, d, dz, 'minecraft:glass_pane')
                self.put(dx, d + 1, dz, 'minecraft:glass_pane')
        self.spiral(base, top)
        # Gallery, rail, lantern room and roof.
        for dx in range(-7, 8):
            for dz in range(-7, 8):
                r = radius(dx, dz)
                if r < 6.6:
                    self.put(dx, gallery, dz, 'minecraft:smooth_stone' if r >= 4.6 else 'minecraft:polished_andesite')
                if 5.6 <= r < 6.6:
                    self.put(dx, gallery + 1, dz, 'hemomancy:bone_brick_wall')
                if 4.6 <= r < 5.6:
                    self.put(dx, gallery - 1, dz, 'hemomancy:bone_brick_stairs', stairs(self.outward(dx, dz), 'top'))
        lantern = gallery + 1
        for dx in range(-4, 5):
            for dz in range(-4, 5):
                r = radius(dx, dz)
                if 3.0 <= r < 4.1:
                    for d in range(lantern, lantern + 5):
                        post = abs(dx) == abs(dz) or dx == 0 or dz == 0
                        self.put(dx, d, dz, 'minecraft:bone_block' if post and (abs(dx) + abs(dz)) in (4, 6)
                                 else 'minecraft:glass_pane')
                elif r < 3.0:
                    for d in range(lantern, lantern + 5):
                        self.air(dx, d, dz)
                    self.put(dx, lantern - 1, dz, 'hemomancy:sunburst_crust' if r >= 1 else 'minecraft:chiseled_stone_bricks')
        self.put(0, lantern, 0, 'hemomancy:chiseled_bone_bricks')
        self.put(0, lantern + 1, 0, 'hemomancy:sunburst_lamp')
        self.put(0, lantern + 2, 0, 'hemomancy:sunburst_lamp')
        for dx, dz in ((1, 1), (-1, -1), (1, -1), (-1, 1)):
            self.put(dx, lantern, dz, 'hemomancy:sunburst_frond')
        self.room.resident(*self.at(2, lantern, 0), 'lamp_keeper', 'west')
        roof = lantern + 5
        for ring, (r0, r1) in enumerate(((3.0, 4.6), (1.6, 3.0), (0, 1.6))):
            for dx in range(-5, 6):
                for dz in range(-5, 6):
                    r = radius(dx, dz)
                    if r0 <= r < r1:
                        if ring == 2:
                            self.put(dx, roof + ring, dz, 'hemomancy:dark_lacquer_planks')
                        else:
                            self.put(dx, roof + ring, dz, 'hemomancy:dark_lacquer_planks_stairs',
                                     stairs(self.inward(dx, dz)))
                    if ring == 0 and r < 3.0:
                        self.put(dx, roof, dz, 'hemomancy:dark_lacquer_planks')
        self.put(0, roof + 3, 0, 'minecraft:bone_block')
        self.put(0, roof + 4, 0, 'hemomancy:bone_brick_wall')

    def spiral(self, base, top):
        """Slab steps round the newel, half a block a step; each step gets headroom carved over it."""
        cells, seen = [], set()
        for i in range(720):
            a = math.radians(i * .5) - math.pi / 2
            cell = (round(2.5 * math.cos(a)), round(2.5 * math.sin(a)))
            if cell not in seen and 1.5 <= radius(*cell) < 3.5:
                seen.add(cell)
                cells.append(cell)
        k = 0
        d = base
        while d < top:
            dx, dz = cells[k % len(cells)]
            level = base + k // 2
            if level > top:
                break
            self.put(dx, level, dz, 'minecraft:smooth_stone_slab', slab('bottom' if k % 2 == 0 else 'top'))
            k += 1
            d = level
        # The last step comes up through the gallery floor.
        dx, dz = cells[(k - 1) % len(cells)]
        self.air(dx, top + 1, dz)

    @staticmethod
    def outward(dx, dz):
        if abs(dx) >= abs(dz):
            return 'east' if dx > 0 else 'west'
        return 'south' if dz > 0 else 'north'

    @staticmethod
    def inward(dx, dz):
        return {'east': 'west', 'west': 'east', 'north': 'south', 'south': 'north'}[Garden.outward(dx, dz)]

    # --- the guest house and the landing ---------------------------------------------------------------------------

    def guest_house(self):
        x0, x1, z0, z1 = -18, -10, -4, 4
        floor = 4
        for dx in range(x0 - 1, x1 + 2):
            for dz in range(z0 - 1, z1 + 2):
                for d in range(-DEPTH, floor + 1):
                    if not self.in_undercroft_piece(dx, d, dz) and self.get(dx, d, dz) is None or d == floor:
                        self.put(dx, d, dz, rock_block(dx, d, dz) if d < floor else 'minecraft:smooth_stone')
        for dx in range(x0, x1 + 1):
            for dz in range(z0, z1 + 1):
                edge = dx in (x0, x1) or dz in (z0, z1)
                corner = dx in (x0, x1) and dz in (z0, z1)
                self.put(dx, floor, dz, 'hemomancy:dark_lacquer_planks' if not edge else 'minecraft:stone_bricks')
                for d in range(floor + 1, floor + 5):
                    if corner:
                        self.put(dx, d, dz, 'minecraft:bone_block', {'axis': 'y'})
                    elif edge:
                        self.put(dx, d, dz, 'minecraft:calcite' if d > floor + 1 else 'minecraft:stone_bricks')
                    else:
                        self.air(dx, d, dz)
        # Gable roof along x, lacquer over bone ridge.
        for i in range(0, 6):
            for dx in range(x0 - 1, x1 + 2):
                for dz, facing in ((z0 - 1 + i, 'south'), (z1 + 1 - i, 'north')):
                    if dz > z1 + 1 - i or dz < z0 - 1 + i:
                        continue
                    self.put(dx, floor + 5 + i, dz, 'hemomancy:dark_lacquer_planks_stairs', stairs(facing))
                if z0 - 1 + i == z1 + 1 - i or z0 + i == z1 + 1 - i:
                    self.put(dx, floor + 5 + i, z0 - 1 + i, 'minecraft:bone_block', {'axis': 'x'})
            for dx in (x0, x1):
                for dz in range(z0 + i, z1 + 1 - i):
                    if i > 0:
                        self.put(dx, floor + 4 + i, dz, 'minecraft:calcite')
        # Door to the east, windows, beds, hearth and table.
        self.air(x1, floor + 1, 0)
        self.air(x1, floor + 2, 0)
        for dz in (-2, 2):
            self.put(x0, floor + 2, dz, 'minecraft:glass_pane')
            self.put(x1, floor + 2, dz, 'minecraft:glass_pane')
        for dx in (-16, -14):
            self.put(dx, floor + 1, z1 - 2, 'minecraft:orange_bed', {'facing': 'north', 'part': 'foot', 'occupied': 'false'})
            self.put(dx, floor + 1, z1 - 1, 'minecraft:orange_bed', {'facing': 'north', 'part': 'head', 'occupied': 'false'})
        self.put(-12, floor + 1, z1 - 1, 'minecraft:barrel', {'facing': 'up', 'open': 'false'})
        # The hearth of bloom light: a stone recess in the north wall with a living lamp in it.
        for dx in (-15, -13):
            for d in (floor + 1, floor + 2):
                self.put(dx, d, z0 + 1, 'minecraft:stone_bricks')
        self.put(-14, floor + 3, z0 + 1, 'minecraft:stone_brick_slab', slab('top'))
        self.put(-14, floor + 1, z0 + 1, 'hemomancy:sunburst_lamp')
        for dx in (-16, -15, -14):
            self.put(dx, floor + 1, 0, 'minecraft:dark_oak_fence')
            self.put(dx, floor + 2, 0, 'hemomancy:dark_lacquer_planks_slab', slab())
        self.put(-15, floor + 3, 0, 'minecraft:cake')
        self.put(-17, floor + 1, 0, 'hemomancy:dark_lacquer_planks_stairs', stairs('west'))
        self.put(-13, floor + 1, 0, 'hemomancy:dark_lacquer_planks_stairs', stairs('east'))
        self.put(-12, floor + 1, -2, 'minecraft:white_carpet')
        self.room.resident(*self.at(-12, floor + 1, 1), 'guest_host', 'west')
        # A cloth awning over the door.
        for dz in (-1, 0, 1):
            self.put(x1 + 1, floor + 4, dz, 'minecraft:orange_wool')

    def landing(self):
        """A stepped landing on the north shore and a short pier on bone posts."""
        for dz in range(-21, -11):
            for dx in (-1, 0, 1):
                self.put(dx, 1, dz, 'minecraft:stone_brick_slab', slab('top'))
            if dz % 3 == 0:
                for dx in (-2, 2):
                    for d in range(-DEPTH + 10, 3):
                        self.put(dx, d, dz, 'minecraft:bone_block', {'axis': 'y'})
        for step, d in enumerate(range(2, ISLAND_TOP + 1)):
            for dx in (-1, 0, 1):
                self.put(dx, d, -11 + step, 'minecraft:stone_brick_stairs', stairs('south'))
                for below in range(1, d):
                    if self.get(dx, below, -11 + step) is None:
                        self.put(dx, below, -11 + step, 'minecraft:stone_bricks')
        for dz in (-21, -18, -15):
            self.put(-2, 3, dz, 'hemomancy:sunburst_frond')
            self.put(2, 3, dz, 'hemomancy:sunburst_frond')

    # --- the undercroft's upper tiers -------------------------------------------------------------------------------

    def undercroft_upper(self):
        coliseum(self, -4, CEILING)
        # The hatch: a stair down through the roof east of the lighthouse, under a frond-roofed kiosk.
        for step, (dz, d) in enumerate(((-2, 7), (-1, 6), (0, 5), (1, 4))):
            for dx in (8, 9):
                self.put(dx, d, dz, 'minecraft:mossy_stone_brick_stairs', stairs('north'))
                for below in range(TIERS[0] + 1, d):
                    self.put(dx, below, dz, 'minecraft:mossy_stone_bricks')
                for clear in range(d + 1, d + 3):
                    if clear <= CEILING:
                        self.air(dx, clear, dz)
        for dx in (7, 10):
            for dz in (-4, 1):
                for d in range(ISLAND_TOP + 1, ISLAND_TOP + 4):
                    self.put(dx, d, dz, 'hemomancy:sunburst_crust')
            for dz in range(-3, 1):
                self.put(dx, ISLAND_TOP + 1, dz, 'minecraft:mossy_stone_brick_wall')
        for dx in range(7, 11):
            for dz in range(-4, 2):
                self.put(dx, ISLAND_TOP + 4, dz, 'minecraft:mossy_stone_brick_slab', slab())
                if h('kiosk', dx, dz) < .5:
                    self.put(dx, ISLAND_TOP + 5, dz, 'hemomancy:sunburst_frond')
        self.room.resident(*self.at(-8, TIERS[0] + 1, 4), 'undercroft_tender', 'east')
        self.room.resident(*self.at(9, TIERS[1] + 1, -2), 'undercroft_anchored', 'west')
        self.room.resident(*self.at(-9, TIERS[2] + 1, 3), 'undercroft_anchored_deep', 'east')
        self.room.jig(*self.at(0, -4, 0), 'down_north', 'hemomancy:court_seat/undercroft_socket', AIR,
                      pool='hemomancy:brined_court/lighthouse_garden/undercroft', target='hemomancy:court_seat/undercroft')

    def build(self):
        self.rock()
        self.terraces()
        self.landing()
        self.guest_house()
        self.undercroft_upper()
        self.lighthouse()
        self.tables()
        self.arbors()
        self.room.jig(*self.at(0, 0, 0), 'up_north', 'hemomancy:court_seat_start', AIR)
        self.room.finish()
        return self.room


# --- the coliseum, shared by both pieces ----------------------------------------------------------------------------

def moss_share(d):
    """How much of a surface the moss has taken at depth d: a little at the top, nearly all at the bottom."""
    return max(.1, min(.95, .25 + (3 - d) * .05))


def coliseum(piece, d_low, d_high):
    """Paints the coliseum between two heights onto either piece (anything with put/air/at/room)."""
    for dx in range(-13, 14):
        for dz in range(-13, 14):
            r = radius(dx, dz)
            if r >= 12.6:
                continue
            for d in range(d_low, d_high + 1):
                share = moss_share(d)
                if r >= SHAFT:
                    wall = 'minecraft:moss_block' if h('cwall', dx, d, dz) < share * .7 else (
                        'minecraft:mossy_stone_bricks' if h('cw2', dx, d, dz) < .5 + share / 2 else 'minecraft:stone_bricks')
                    piece.put(dx, d, dz, wall)
                    continue
                if d == CEILING:
                    piece.put(dx, d, dz, 'minecraft:stone_bricks' if r > 2 else 'minecraft:chiseled_stone_bricks')
                    continue
                tier = d in TIERS and r >= LEDGE
                if tier:
                    piece.put(dx, d, dz, 'minecraft:moss_block' if h('ledge', dx, d, dz) < share else 'minecraft:mossy_stone_bricks')
                elif d == U_BOTTOM:
                    piece.put(dx, d, dz, 'minecraft:moss_block')
                elif d in POOL and r < LEDGE:
                    if r < 3.5:
                        piece.put(dx, d, dz, 'minecraft:moss_block')
                    else:
                        piece.room.water(*piece.at(dx, d, dz))
                elif d in POOL:
                    piece.put(dx, d, dz, 'minecraft:moss_block')
                else:
                    piece.air(dx, d, dz)
    # Balustrades at each ledge's lip, stairs between tiers along the wall, dressing per tier.
    for t, floor in enumerate(TIERS):
        if not d_low <= floor <= d_high:
            continue
        stair_angle = math.radians(90 * t + 30)
        for dx in range(-11, 12):
            for dz in range(-11, 12):
                r = radius(dx, dz)
                if LEDGE <= r < LEDGE + 1 and not near_angle(dx, dz, stair_angle, .5):
                    if h('bal', dx, dz, t) < .85:
                        piece.put(dx, floor + 1, dz, 'minecraft:mossy_stone_brick_wall')
                elif LEDGE + 1 <= r < SHAFT:
                    n = h('dress', dx, dz, t)
                    if n < moss_share(floor) * .6:
                        piece.put(dx, floor + 1, dz, 'minecraft:moss_carpet')
                if 9.5 <= r < SHAFT and h('lich', dx, dz, t) < .3:
                    side = Garden.outward(dx, dz)
                    piece.put(dx, floor + 2, dz, 'minecraft:glow_lichen', lichen(side))
                if LEDGE <= r < LEDGE + 1.2 and h('vine', dx, dz, t) < .22 and floor - 1 >= d_low:
                    # Glow-berry vines hang from under the ledge into the tier below.
                    piece.put(dx, floor - 1, dz, 'minecraft:cave_vines', {'age': '25', 'berries': 'true'})
        below = TIERS[t + 1] if t + 1 < len(TIERS) else POOL[1]
        descend(piece, floor, below, stair_angle)


def near_angle(dx, dz, angle, width):
    a = math.atan2(dz, dx)
    diff = (a - angle + math.pi) % (2 * math.pi) - math.pi
    return abs(diff) < width


def descend(piece, floor, below, angle):
    """A stair run along the wall from this gallery down to the next surface, with headroom carved over it."""
    for step in range(floor - below):
        a = angle + step * .14
        dx, dz = round(9 * math.cos(a)), round(9 * math.sin(a))
        d = floor - step - 1
        tangent = math.degrees(a + math.pi / 2) % 360
        facing = ['east', 'south', 'west', 'north'][int(((tangent + 45) % 360) // 90)]
        for ox, oz in ((0, 0), (round(math.cos(a)), round(math.sin(a)))):
            piece.put(dx + ox, d, dz + oz, 'minecraft:mossy_stone_brick_stairs', stairs(opposite(facing)))
            for clear in range(d + 1, d + 4):
                piece.air(dx + ox, clear, dz + oz)


def opposite(facing):
    return {'east': 'west', 'west': 'east', 'north': 'south', 'south': 'north'}[facing]


class Undercroft:
    """The deep tiers and the pool, as their own piece (rows d = -13..-5)."""

    def __init__(self):
        self.room = Seat(U_SIZE, U_TOP - U_BOTTOM + 1, U_SIZE, -U_BOTTOM)

    def at(self, dx, d, dz):
        return U_C + dx, d - U_BOTTOM, U_C + dz

    def put(self, dx, d, dz, name, props=None):
        x, y, z = self.at(dx, d, dz)
        self.room.put(x, y, z, name, props)

    def air(self, dx, d, dz):
        self.room.air(*self.at(dx, d, dz))

    def get(self, dx, d, dz):
        return self.room.get(*self.at(dx, d, dz))

    def build(self):
        # The island rock round the shaft, where the tide rock reaches this deep.
        for dx in range(-U_C, U_C + 1):
            for dz in range(-U_C, U_C + 1):
                if radius(dx, dz) < 12.6:
                    continue
                if rock_top(dx, dz) is None and not terrace(dx, dz):
                    continue
                for d in range(U_BOTTOM, U_TOP + 1):
                    self.put(dx, d, dz, rock_block(dx, d, dz))
        coliseum(self, U_BOTTOM, U_TOP)
        self.followers()
        # Marimo's place: the moss island in the pool (Track 10 puts his presence here).
        for dx in range(-3, 4):
            for dz in range(-3, 4):
                if radius(dx, dz) < 3.5 and h('isl', dx, dz) < .5:
                    self.put(dx, POOL[1] + 1, dz, 'minecraft:moss_carpet')
        self.room.mark(*self.at(0, POOL[1] + 1, 0), 'marimo_presence')
        for dx in range(-7, 8):
            for dz in range(-7, 8):
                r = radius(dx, dz)
                if 3.5 <= r < LEDGE and h('pickle', dx, dz) < .12:
                    self.put(dx, POOL[0], dz, 'minecraft:sea_pickle', {'pickles': str(2 + int(h('pc', dx, dz) * 3))})
        self.room.resident(*self.at(4, TIERS[3] + 1, 8), 'undercroft_anchored_deep', 'north')
        self.room.resident(*self.at(-8, POOL[1] + 1, -3), 'undercroft_anchored_deep', 'east')
        self.room.jig(*self.at(0, U_TOP, 0), 'up_north', 'hemomancy:court_seat/undercroft', AIR,
                      pool='minecraft:empty', target='minecraft:empty')
        self.room.finish()
        return self.room

    def followers(self):
        """True petrification on the lowest walls: kneeling followers in niches cut into the shaft wall."""
        floors = (TIERS[3], U_BOTTOM + 2)
        for i in range(14):
            a = i * 2 * math.pi / 14 + .2
            floor = floors[i % 2]
            dx, dz = round(10.6 * math.cos(a)), round(10.6 * math.sin(a))
            if radius(dx, dz) < SHAFT or self.get(dx, floor + 1, dz) is None:
                continue
            ix, iz = round(9.6 * math.cos(a)), round(9.6 * math.sin(a))
            facing = Garden.inward(dx, dz)
            for d in (floor + 1, floor + 2):
                self.air(dx, d, dz)
            self.put(dx, floor + 1, dz, 'hemomancy:petrified_follower',
                     {'facing': facing, 'moss': 'whole' if h('mw', i) < .5 else 'partial'})
            self.put(dx, floor + 3, dz, 'minecraft:moss_block')
            if radius(ix, iz) >= LEDGE:
                self.put(ix, floor + 1, iz, 'minecraft:moss_carpet')

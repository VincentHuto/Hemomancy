"""The Vent Sanctum (the Kindling's Seat): two jigsaw pieces on a seeded vent mound, built round its chimneys.

The terrain's chimneys stay exactly where and as the terrain built them: the `sanctum` piece is structure void within
CHIMNEY_ZONE of the centre column at every height, and the seeded vent site puts every chimney well inside it. What
crowns the chimneys (the Hydra Crowns, the offering basins at their feet and the threshold walkway up to the tallest)
depends on where the terrain put them, so CourtStructure.afterPlace adds it at runtime from VentSanctumHeads; it only
ever writes into water.

`sanctum` (the start piece) holds the processional ring round the heads and the two halls of preparation cut into the
mound's flanks: dry, vaulted, entered from below through a flooded antechamber and a moon pool, with rest alcoves for
the Kindled elders (Track 9 residents) and cultivation basins. `body` hangs off its south edge: the mass of grown
tissue that members who did not keep a head became, sprawled down the mound's skirt.

Frame. d is height above the marker (d = 0 is the first water above the seabed at the centre column); rock under the
seabed replaces terrain and open water is structure void. Everything here is under the sea, so every air cell is in a
sealed room.
"""
import math

from .kit import AIR, WATER, Seat, h, lichen, slab, stairs

SIZE = 51
C = 25
BELOW = 10                 # rows under the marker
HEIGHT = BELOW + 17
START_LIFT = 14            # the start jigsaw's height above the marker (clear of every chimney and crown)
CHIMNEY_ZONE = 12.5
RING = (13.5, 15.6)
HALL_FLOOR = 2
BODY_LENGTH = 17

BRICKS = ['minecraft:polished_blackstone_bricks', 'minecraft:polished_blackstone_bricks',
          'minecraft:cracked_polished_blackstone_bricks']
FOOTING = ['minecraft:blackstone', 'minecraft:basalt', 'minecraft:smooth_basalt', 'minecraft:tuff']


def radius(dx, dz):
    return math.hypot(dx, dz)


class Sanctum:
    def __init__(self):
        # Every row is under the sea: the waterline is far above the template.
        self.room = Seat(SIZE, HEIGHT, SIZE, 10_000)

    def at(self, dx, d, dz):
        return C + dx, BELOW + d, C + dz

    def put(self, dx, d, dz, name, props=None):
        if radius(dx, dz) < CHIMNEY_ZONE:
            return
        self.room.put(*self.at(dx, d, dz), name, props)

    def air(self, dx, d, dz):
        if radius(dx, dz) >= CHIMNEY_ZONE:
            self.room.air(*self.at(dx, d, dz))

    def water(self, dx, d, dz):
        self.room.water(*self.at(dx, d, dz))

    def ring(self):
        """A raised processional terrace round the heads: walkway at d = 0, a parapet, carved pillars every 30
        degrees with living polyp light, and four gaps at the compass points facing the heads."""
        for dx in range(-17, 18):
            for dz in range(-17, 18):
                r = radius(dx, dz)
                if not RING[0] <= r < RING[1]:
                    continue
                angle = math.degrees(math.atan2(dz, dx)) % 360
                gate = min(abs(((angle - a + 180) % 360) - 180) for a in (0, 90, 180, 270)) < 7
                for d in range(-BELOW, 0):
                    self.put(dx, d, dz, FOOTING[int(h('foot', dx, d, dz) * len(FOOTING))])
                self.put(dx, 0, dz, 'minecraft:polished_blackstone' if r < 14.6 else BRICKS[int(h('rb', dx, dz) * 3)])
                if r >= 14.8 and not gate:
                    self.put(dx, 1, dz, 'minecraft:polished_blackstone_brick_wall')
        for i in range(12):
            a = math.radians(i * 30 + 15)
            px, pz = round(15 * math.cos(a)), round(15 * math.sin(a))
            for d in range(1, 5):
                self.put(px, d, pz, 'minecraft:polished_basalt', {'axis': 'y'})
            self.put(px, 5, pz, 'minecraft:chiseled_polished_blackstone')
            self.put(px, 6, pz, 'minecraft:sea_pickle', {'pickles': '4'})
            if i % 3 == 0:
                self.put(px, 2, pz, 'hemomancy:kindled_tissue')

    def hall(self, side):
        """A hall of preparation along +x (side 1) or -x (side -1)."""
        f = HALL_FLOOR
        x_in, x_out = 18, 25
        xs = lambda a, b: range(min(side * a, side * b), max(side * a, side * b) + 1)  # noqa: E731
        # Shell: footing down to the bottom row, a brick floor, two-thick side walls, end walls, a stepped vault.
        for x in xs(x_in - 1, x_out):
            end_wall = abs(x) in (x_in - 1, x_out)
            for dz in range(-5, 6):
                for d in range(-BELOW, f + 8):
                    if d < f:
                        name = FOOTING[int(h('hf', x, d, dz) * 4)]
                    elif d == f or (d <= f + 4 and (end_wall or abs(dz) >= 4)):
                        name = BRICKS[int(h('hall', x, d, dz) * 3)]
                    elif d == f + 5 and (abs(dz) >= 2 or abs(x) <= x_in):
                        name = BRICKS[int(h('hall', x, d, dz) * 3)]
                    elif d == f + 6 and abs(dz) <= 3:
                        name = BRICKS[int(h('hall', x, d, dz) * 3)]
                    elif d == f + 7 and abs(dz) <= 1:
                        name = 'minecraft:chiseled_polished_blackstone'
                    elif d == f + 5 and end_wall:
                        name = BRICKS[0]
                    else:
                        continue
                    self.put(x, d, dz, name)
        # The dry hall itself.
        for x in xs(x_in, x_out - 1):
            for dz in range(-3, 4):
                for d in range(f + 1, f + 5):
                    self.air(x, d, dz)
                if abs(dz) <= 1 and abs(x) > x_in:
                    self.air(x, f + 5, dz)
        # Rest alcoves for the Kindled elders, two a side, each with a basalt bench and tissue grown over its back.
        for k, x in enumerate((side * 21, side * 23)):
            for dz in (-4, 4):
                for d in (f + 1, f + 2, f + 3):
                    self.air(x, d, dz)
                self.put(x, f + 1, dz, 'minecraft:smooth_basalt')
                self.put(x, f + 3, dz + (1 if dz > 0 else -1), 'hemomancy:kindled_tissue')
                self.put(x, f + 4, dz, 'minecraft:polished_blackstone_brick_slab', slab('top'))
                self.room.resident(*self.at(x, f + 2, dz), 'kindled_elder', 'north' if dz > 0 else 'south')
        # Cultivation basins down the middle, and tissue taking the far wall.
        for x in (side * 20, side * 22):
            self.put(x, f + 1, 0, 'hemomancy:vent_offering_basin', {'facing': 'east' if side < 0 else 'west'})
        for dz in range(-3, 4):
            for d in range(f + 1, f + 5):
                if h('far', side, dz, d) < .6:
                    self.put(side * x_out, d, dz, 'hemomancy:kindled_tissue')
            if h('lich', side, dz) < .5:
                self.put(side * (x_out - 1), f + 4, dz, 'minecraft:glow_lichen', lichen('up'))
        for x in xs(x_in + 1, x_out - 2):
            if h('floor', x) < .5:
                self.put(x, f, 0, 'hemomancy:kindled_tissue')         # grown tissue down the walk (magma would burn)
        self.room.resident(*self.at(side * 24, f + 1, 0), 'kindled_preparer', 'west' if side > 0 else 'east')
        # The moon pool: water in a hole in the floor at the inner end, over a flooded antechamber with a sea door.
        for x in (side * x_in, side * (x_in + 1)):
            for dz in (-1, 0):
                self.water(x, f, dz)
        for x in xs(x_in - 1, x_in + 2):
            for dz in range(-2, 2):
                for d in range(f - 3, f):
                    inner = abs(x) in (x_in, x_in + 1) and dz in (-1, 0)
                    if inner:
                        self.water(x, d, dz)
                    else:
                        self.put(x, d, dz, BRICKS[int(h('ante', x, d, dz) * 3)])
                self.put(x, f - 4, dz, 'minecraft:polished_blackstone')
        for d in (f - 3, f - 2):
            for dz in (-1, 0):
                self.water(side * (x_in - 1), d, dz)                 # the sea door, facing the ring
        self.put(side * (x_in - 1), f - 1, -1, 'minecraft:polished_blackstone_brick_stairs',
                 stairs('east' if side < 0 else 'west', 'top'))
        self.put(side * (x_in - 1), f - 1, 0, 'minecraft:polished_blackstone_brick_stairs',
                 stairs('east' if side < 0 else 'west', 'top'))

    def gates(self):
        """An arch over each gap in the ring, facing a quarter of the heads: basalt posts, a carved lintel, a knot of
        tissue for a keystone, and offerings heaped at the posts' feet."""
        for k in range(4):
            a = math.radians(90 * k)
            ux, uz = round(math.cos(a)), round(math.sin(a))
            px, pz = -uz, ux                      # along the ring at the gap
            cx, cz = round(14.6 * ux), round(14.6 * uz)
            for side in (-2, 2):
                x, z = cx + px * side, cz + pz * side
                for d in range(1, 5):
                    self.put(x, d, z, 'minecraft:polished_basalt', {'axis': 'y'})
                self.put(x - ux, 1, z - uz, 'minecraft:bone_block', {'axis': 'y'})
            for t in range(-2, 3):
                x, z = cx + px * t, cz + pz * t
                self.put(x, 5, z, 'minecraft:chiseled_polished_blackstone' if t == 0 else 'minecraft:polished_blackstone_bricks')
            self.put(cx, 6, cz, 'hemomancy:kindled_tissue')
            self.put(cx, 7, cz, 'minecraft:sea_pickle', {'pickles': '3'})

    def facades(self, side):
        """The face each hall turns to the heads: buttresses either side of the sea door and a polyp relief in bone."""
        x = side * 17
        for dz in (-5, 5):
            for d in range(-2, 7):
                self.put(x - side, d, dz, 'minecraft:polished_basalt', {'axis': 'y'})
        for d, dz in ((5, 0), (6, -1), (6, 1), (7, 0), (4, -2), (4, 2)):
            self.put(x, d, dz, 'minecraft:bone_block', {'axis': 'y'})
        self.put(x, 6, 0, 'hemomancy:kindled_tissue')

    def build(self):
        self.ring()
        self.gates()
        self.hall(1)
        self.hall(-1)
        self.facades(1)
        self.facades(-1)
        # Light on the ring walk between the halls and the heads.
        for dz in (-8, 8):
            for x in (-16, 16):
                self.put(x, 1, dz, 'minecraft:sea_pickle', {'pickles': '3'})
        self.room.jig(*self.at(0, START_LIFT, 0), 'up_north', 'hemomancy:court_seat_start', WATER)
        self.room.jig(*self.at(0, 6, C), 'south_up', 'hemomancy:court_seat/body_socket', WATER,
                      pool='hemomancy:brined_court/vent_sanctum/body', target='hemomancy:court_seat/body',
                      joint='rollable')
        self.room.finish()
        return self.room


class Body:
    """Hydra's body: a sprawl of kindled tissue on the mound's skirt, budding pale polyps, its rim gone to bone."""
    WIDTH = 23

    def __init__(self):
        self.room = Seat(self.WIDTH, HEIGHT, BODY_LENGTH, 10_000)
        self.cx = self.WIDTH // 2

    def at(self, dx, d, z):
        return self.cx + dx, BELOW + d, z

    def build(self):
        for dx in range(-self.cx, self.cx + 1):
            for z in range(0, BODY_LENGTH):
                for d in range(-BELOW, 7):
                    # Two overlapping lobes, flattened, with a wobbling surface.
                    u = (dx + 3) / 8.5
                    v = (z - 9) / 6.5
                    w = (d + 3) / 5.0
                    a = u * u + v * v + w * w
                    u2, v2, w2 = (dx - 5) / 6, (z - 6) / 5, (d + 4) / 4.2
                    b = u2 * u2 + v2 * v2 + w2 * w2
                    wobble = (h('wob', dx, z) - .5) * .25
                    inside = min(a, b) < 1 + wobble
                    if not inside:
                        continue
                    surface = min(a, b) > .72 + wobble
                    if surface and h('nod', dx, d, z) < .08:
                        name = 'minecraft:bone_block' if d < -4 else 'hemomancy:kindled_tissue'
                    else:
                        name = 'hemomancy:kindled_tissue'
                    self.room.put(*self.at(dx, d, z), name)
        # Polyp budding and glow on top of the mass; a tendril rope back toward the heads.
        for dx in range(-self.cx, self.cx + 1):
            for z in range(0, BODY_LENGTH):
                top = None
                for d in range(6, -BELOW - 1, -1):
                    if self.room.get(*self.at(dx, d, z)) is not None:
                        top = d
                        break
                if top is None or top < -2:
                    continue
                n = h('bud', dx, z)
                if n < .1:
                    self.room.put(*self.at(dx, top + 1, z), 'minecraft:sea_pickle', {'pickles': '2'})
                elif n < .16:
                    self.room.put(*self.at(dx, top + 1, z), 'hemomancy:kindled_tissue')
        for z in range(0, 4):
            for d in (-2, -1):
                self.room.put(*self.at(-1 + (z % 2), d, z), 'hemomancy:kindled_tissue')
        self.room.jig(*self.at(0, 6, 0), 'north_up', 'hemomancy:court_seat/body', WATER, joint='rollable')
        self.room.mark(*self.at(-3, 5, 9), 'hydra_body')
        self.room.finish()
        return self.room

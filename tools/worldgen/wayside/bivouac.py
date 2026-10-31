"""The Sanguine Surveyor Bivouac: a Hematic Order survey party's camp in the crimson boughs.

17 x 8 x 15 (x, y, z). Row 0 lands on the column's top ground block and is the camp's trodden floor; the camp is
cleared to air above it inside its outline and left void past it, so the forest closes in at the edge.

- The fire: a lit campfire in a ring of blackstone slabs on packed ash, a chain spit across it on two posts, a
  stripped-stem bench and a pot beside it.
- The sleeping tent: a red canvas A-frame on crimson boards, open to the fire, with two bedrolls, the party's loot
  barrel, a Phlebotomist's field case and a hematic lantern hung from the ridge.
- The survey lean-to: a slatted tarp on stem posts over the work: a cartography table with the tithe candle (Log IV),
  the Harbinger escritoire where the logs were written, a clairaudiograph on a supply barrel, a specimen jar, and a
  humoral barometer hung on the back canvas; a placeholder log is pinned to its post.
- The bearing station: a polished basalt cairn (Log VI's basalt tooth) flying a red survey banner, with a log on its
  face and a sighting rod on a post beside it.
- Supplies: barrels, a crate, a pot and a second loot barrel stacked by the south edge, a log nailed to the stack, a
  lantern post, black and red survey banners at the camp's edge, and crimson roots, fungus and wart where the
  nylium is left.
"""
from .kit import Site, AIR, stairs, slab, trapdoor, outline, h

PLACEHOLDER = 'hemomancy:random/surveyor_log'
LOOT = 'hemomancy:chests/harbinger_outpost'

SX, SY, SZ = 17, 8, 15
G = 0
FIRE = (8, 7)
STEM = 'minecraft:crimson_stem'
FENCE = 'minecraft:crimson_fence'
TRAPDOOR = 'minecraft:crimson_trapdoor'
PLANKS = 'minecraft:crimson_planks'


class Bivouac:
    def __init__(self, seed='surveyor_bivouac'):
        self.seed = seed
        self.s = Site(SX, SY, SZ, G, seed)
        self.d = outline(8, 7, 8.6, 7.4, seed, wobble=0.08)

    def floor(self, x, z):
        s, d = self.s, self.d(x, z)
        fx, fz = FIRE
        if abs(x - fx) <= 1 and abs(z - fz) <= 1:
            return 'hemomancy:packed_ash'
        if d < 0.38:
            return s.pick((5, 'minecraft:netherrack'), (3, 'hemomancy:packed_ash'), (2, 'minecraft:soul_soil'),
                          (1, 'minecraft:blackstone'))
        if d < 0.72:
            return s.pick((6, 'minecraft:netherrack'), (2, 'minecraft:crimson_nylium'), (1, 'minecraft:blackstone'))
        return s.pick((6, 'minecraft:crimson_nylium'), (1, 'minecraft:netherrack'))

    def ground(self):
        s = self.s
        for x in range(SX):
            for z in range(SZ):
                d = self.d(x, z)
                if d >= 1.0:
                    continue
                s.put(x, G, z, self.floor(x, z))

    def fire(self):
        s = self.s
        fx, fz = FIRE
        s.put(fx, 1, fz, 'minecraft:campfire', {'facing': 'south', 'lit': 'true', 'signal_fire': 'false'})
        for dx, dz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            s.put(fx + dx, 1, fz + dz, 'minecraft:polished_blackstone_brick_slab', slab())
        s.put(fx, 1, fz - 1, 'minecraft:blackstone_slab', slab())
        # The spit: two posts and a chain across the fire.
        for x in (fx - 2, fx + 2):
            s.put(x, 1, fz, FENCE, {})
            s.put(x, 2, fz, FENCE, {})
        for x in range(fx - 1, fx + 2):
            s.put(x, 2, fz, 'minecraft:chain', {'axis': 'x'})
        # A bench of stripped stem south of the fire, a pot at its end.
        for x in (fx - 1, fx, fx + 1):
            s.put(x, 1, fz + 3, 'minecraft:stripped_crimson_stem', {'axis': 'x'})
        s.put(fx + 2, 1, fz + 2, 'minecraft:decorated_pot', {'facing': 'north', 'cracked': 'false'})
        s.put(fx - 3, 1, fz - 1, 'minecraft:stripped_crimson_stem', {'axis': 'z'})

    def tent(self):
        """A five-wide A-frame along x (x0..4), its ridge on z5, open to the east."""
        s = self.s
        x0, x1, zc = 0, 4, 5

        def canvas(x, z):
            return 'minecraft:brown_wool' if h(self.seed, 'canvas', x, z) < 0.18 else 'minecraft:red_wool'

        for x in range(x0, x1 + 1):
            s.put(x, 1, zc - 2, canvas(x, zc - 2))
            s.put(x, 1, zc + 2, canvas(x, zc + 2))
            s.put(x, 2, zc - 1, canvas(x, zc - 1))
            s.put(x, 2, zc + 1, canvas(x, zc + 1))
            s.put(x, 3, zc, 'minecraft:red_wool')
            # Carpet over each step softens the slope; a black cord along the ridge.
            s.put(x, 2, zc - 2, 'minecraft:red_carpet')
            s.put(x, 2, zc + 2, 'minecraft:red_carpet')
            s.put(x, 3, zc - 1, 'minecraft:red_carpet')
            s.put(x, 3, zc + 1, 'minecraft:red_carpet')
            s.put(x, 4, zc, 'minecraft:black_carpet')
            for z in range(zc - 1, zc + 2):
                s.put(x, 0, z, PLANKS)
                if x > x0:
                    s.air(x, 1, z)
            if x > x0:
                s.air(x, 2, zc)
        # The back gable closed.
        for z in range(zc - 1, zc + 2):
            s.put(x0, 1, z, canvas(x0, z))
        s.put(x0, 2, zc, canvas(x0, zc))
        # Front poles at the mouth's corners.
        s.put(x1 + 1, 2, zc - 2, FENCE, {})
        s.put(x1 + 1, 1, zc - 2, FENCE, {})
        s.put(x1 + 1, 2, zc + 2, FENCE, {})
        s.put(x1 + 1, 1, zc + 2, FENCE, {})
        # Bedrolls either side, the loot barrel at the back, the field case and a lantern from the ridge.
        for z in (zc - 1, zc + 1):
            s.put(x0 + 1, 1, z, 'minecraft:white_carpet')
            s.put(x0 + 2, 1, z, 'minecraft:brown_carpet' if z < zc else 'minecraft:black_carpet')
            s.put(x0 + 3, 1, z, 'minecraft:brown_carpet' if z < zc else 'minecraft:black_carpet')
        s.barrel(x0 + 1, 1, zc, 'east', LOOT)
        s.put(x0 + 2, 1, zc, 'hemomancy:phlebotomists_field_case', {'facing': 'east', 'open': 'false'})
        s.put(x0 + 3, 2, zc, 'hemomancy:hematic_lantern', {'hanging': 'true'})

    def lean_to(self):
        """A tarp on a slatted frame sloping from the back canvas (z0) to two front posts (z3), x7..12."""
        s = self.s
        xa, xb = 7, 12
        for y in (1, 2, 3):
            s.put(xa, y, 0, STEM, {'axis': 'y'})
            s.put(xb, y, 0, STEM, {'axis': 'y'})
            for x in range(xa + 1, xb):
                s.put(x, y, 0, 'minecraft:brown_wool' if h(self.seed, 'back', x, y) < 0.25 else 'minecraft:red_wool')
        for y in (1, 2):
            s.put(xa, y, 3, FENCE, {})
            s.put(xb, y, 3, FENCE, {})
        tarp = {0: 4, 1: 4, 2: 3, 3: 3}
        for x in range(xa, xb + 1):
            for z, y in tarp.items():
                s.put(x, y, z, TRAPDOOR, trapdoor('south', 'top'))
                colour = s.pick((5, 'minecraft:red_carpet'), (2, 'minecraft:black_carpet'),
                                (1, 'minecraft:brown_carpet'))
                s.put(x, y + 1, z, colour)
            for z in (0, 1, 2, 3):
                s.put(x, 0, z, PLANKS if xa < x < xb and z > 0 else s.get(x, 0, z) or 'minecraft:netherrack')
        # The work under it.
        s.put(8, 1, 1, 'minecraft:cartography_table')
        s.candle(8, 2, 1)
        s.put(8, 3, 1, 'hemomancy:humoral_barometer', {'facing': 'north'})
        s.put(9, 1, 1, 'hemomancy:harbinger_escritoire', {'facing': 'south'})
        s.barrel(10, 1, 1, 'up')
        s.put(10, 2, 1, 'hemomancy:clairaudiograph', {'facing': 'south'})
        s.put(11, 1, 1, PLANKS)
        s.put(11, 2, 1, TRAPDOOR, trapdoor('south'))
        s.put(11, 1, 2, 'hemomancy:specimen_jar', {'facing': 'south'})
        s.inscription(12, 2, 1, 'south', PLACEHOLDER)
        s.put(12, 1, 1, 'minecraft:stripped_crimson_stem', {'axis': 'y'})

    def bearing_station(self):
        s = self.s
        cx, cz = 14, 7
        for y in (1, 2, 3):
            s.put(cx, y, cz, 'minecraft:polished_basalt', {'axis': 'y'})
        s.put(cx, 4, cz, 'minecraft:red_banner', {'rotation': '4'})
        s.put(cx - 1, 1, cz, 'minecraft:polished_basalt', {'axis': 'x'})
        s.inscription(cx - 1, 2, cz, 'west', PLACEHOLDER)
        s.put(cx + 1, 1, cz, 'minecraft:cracked_polished_blackstone_bricks')
        s.put(cx, 1, cz + 1, 'minecraft:polished_blackstone_brick_stairs', stairs('north'))
        s.put(cx, 1, cz - 1, 'minecraft:polished_blackstone_brick_slab', slab())
        s.put(cx + 1, 2, cz, 'minecraft:polished_blackstone_brick_slab', slab())
        # The sighting rod: a post with an end rod laid west along the bearing.
        s.put(13, 1, 10, FENCE, {})
        s.put(13, 2, 10, FENCE, {})
        s.put(13, 3, 10, 'minecraft:end_rod', {'facing': 'west'})

    def supplies(self):
        s = self.s
        s.barrel(11, 1, 12, 'north')
        s.barrel(11, 2, 12, 'up')
        s.candle(11, 3, 12)
        s.barrel(12, 1, 12, 'north', LOOT)
        s.put(12, 1, 13, PLANKS)
        s.put(12, 2, 13, TRAPDOOR, trapdoor('north'))
        s.put(13, 1, 12, 'minecraft:decorated_pot', {'facing': 'west', 'cracked': 'true'})
        s.put(10, 1, 12, PLANKS)
        s.inscription(10, 2, 12, 'west', PLACEHOLDER)
        s.put(10, 1, 13, 'hemomancy:phlebotomists_field_case', {'facing': 'north', 'open': 'false'})
        # The lantern post by the tent.
        for y in (1, 2, 3):
            s.put(4, y, 10, FENCE, {})
        s.put(5, 3, 10, FENCE, {})
        s.put(5, 2, 10, 'hemomancy:hematic_lantern', {'hanging': 'true'})
        # Survey banners at the edge.
        s.put(2, 1, 12, 'minecraft:black_banner', {'rotation': '10'})
        s.put(15, 1, 2, 'minecraft:red_banner', {'rotation': '3'})

    def overgrowth(self):
        s = self.s
        for x in range(SX):
            for z in range(SZ):
                if s.get(x, 0, z) != 'minecraft:crimson_nylium' or s.get(x, 1, z) not in (None, AIR)                         or self.d(x, z) < 0.62 or any(s.get(x, y, z) not in (None, AIR) for y in range(2, SY)):
                    continue
                roll = h(self.seed, 'grow', x, z)
                if roll < 0.18:
                    s.put(x, 1, z, 'minecraft:crimson_roots')
                elif roll < 0.24:
                    s.put(x, 1, z, 'minecraft:crimson_fungus')
        # Wart heaped at the edge where the forest begins, a shroomlight or two sunk in the floor.
        edge = sorted((h(self.seed, 'wart', x, z), x, z) for x in range(SX) for z in range(SZ)
                      if 0.8 < self.d(x, z) < 0.97 and s.get(x, 0, z) is not None
                      and all(s.get(x, y, z) in (None, AIR) for y in range(1, SY)))
        for i, (_r, x, z) in enumerate(edge[:6]):
            for y in range(1, 3 if i % 3 == 0 else 2):
                s.put(x, y, z, 'minecraft:nether_wart_block')
        for (_r, x, z) in edge[6:9]:
            s.put(x, 0, z, 'minecraft:shroomlight')

    def underpin(self):
        """Every column the camp builds on has its floor block, even past the outline, and is clear above it."""
        s = self.s
        for (x, y, z), (name, _p, _n) in list(s.blocks.items()):
            if y >= 1 and name != AIR and s.get(x, 0, z) in (None, AIR):
                s.put(x, 0, z, 'minecraft:netherrack')
        for x in range(SX):
            for z in range(SZ):
                if s.get(x, 0, z) is not None:
                    for y in range(1, SY):
                        if s.get(x, y, z) is None:
                            s.air(x, y, z)

    def build(self):
        self.ground()
        self.fire()
        self.tent()
        self.lean_to()
        self.bearing_station()
        self.supplies()
        self.underpin()
        self.overgrowth()
        self.s.finish()
        return self.s

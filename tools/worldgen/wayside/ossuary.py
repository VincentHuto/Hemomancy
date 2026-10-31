"""The Bog-Body Ossuary Niche: a Harbinger burial stop dug into a peat mound at a swamp's edge.

15 x 12 x 13 (x, y, z), front to the south. Row 2 lands on the top ground block (the structure starts two below the
surface), so rows 0 and 1 are peat under the ground and the niche's floor sits one below it.

- The mound: grass, moss and rooted earth over mud and packed peat, with bog iron nodules and old bones in its
  flanks, mangrove roots tangling its shoulders and a dead mangrove stump on its crown.
- The niche: a five-wide alcove under a stepped bone-brick vault, walls of sodden mud brick on a bone-brick base. A
  rank of burial slots runs down each side wall (Brotherhood niche fronts, one opened slot keeping a bog body), the
  back wall holds a skull recess over a venous basin (Memo VI's names in the water) between two placeholder memos,
  pale reliquary sconces light it, and a loot barrel with a tithe candle and a field-preserved body lie by the door.
- The front: an arched bone-brick door, one step down, onto a peat yard; a cutting pool in the peat holds two bog
  bodies under the water (vivianite-stained, harvestable as in the world), a mangrove duckboard runs to the door past
  a lantern post, peat bricks dry in stacks, and a mud-brick stele by the pool carries the third memo.
"""
from .kit import Site, stairs, slab, outline, h

PLACEHOLDER = 'hemomancy:random/ossuary_memo'
LOOT = 'hemomancy:chests/bog_ossuary_niche'

SX, SY, SZ = 15, 12, 13
G = 2          # the ground row
FLOOR = 1      # the niche's floor row (one below the ground)
# The niche's open interior: x 5..9, z 2..6, air from row 2 up under the vault.
NX0, NX1, NZ0, NZ1 = 5, 9, 2, 6
FRONT = 7      # the façade row along z
POOL = (2, 5, 9, 11)        # x0, x1, z0, z1 of the cutting pool

MUD = 'minecraft:mud'
SODDEN = 'hemomancy:sodden_mud_bricks'
BONE = 'hemomancy:bone_bricks'
BONE_CHISELED = 'hemomancy:chiseled_bone_bricks'
BONE_CRACKED = 'hemomancy:cracked_bone_bricks'
BONE_STAIRS = 'hemomancy:bone_brick_stairs'
BONE_SLAB = 'hemomancy:bone_brick_slab'
BONE_WALL = 'hemomancy:bone_brick_wall'


class Ossuary:
    def __init__(self, seed='bog_ossuary'):
        self.seed = seed
        self.s = Site(SX, SY, SZ, G, seed)
        self.yard_d = outline(7, 6.2, 7.3, 6.3, seed + 'yard', wobble=0.08)
        self.mound = outline(7, 3.2, 7.2, 5.0, seed + 'mound', wobble=0.06)
        self.heights = {}

    # --- earth ----------------------------------------------------------------------------------------------------

    def subsoil(self, x, y, z):
        return self.s.pick((5, MUD), (3, 'minecraft:packed_mud'), (2, 'minecraft:dirt'), (1, 'minecraft:clay'),
                           (0.5, 'hemomancy:bog_iron'))

    def surface(self, x, z):
        return self.s.pick((6, ('minecraft:grass_block', {'snowy': 'false'})), (3, 'minecraft:moss_block'),
                           (2, MUD), (2, 'minecraft:rooted_dirt'), (1, 'minecraft:coarse_dirt'))

    def mound_top(self, x, z):
        """The mound's top row at (x, z), or None off the mound: a flattish dome, padded to cover the niche."""
        d = self.mound(x, z)
        top = None
        if d < 1.0 and z <= FRONT:
            top = G + int(round(6.2 * (1 - d ** 4)))
        if 3 <= x <= 11 and 0 <= z <= 6:
            top = max(top or 0, 9 if 5 <= x <= 9 and z <= 6 else 8)
        if 3 <= x <= 11 and z == FRONT:
            top = max(top or 0, 7 if 4 <= x <= 10 else 5)
        return top

    def ground(self):
        s = self.s
        for x in range(SX):
            for z in range(SZ):
                if self.yard_d(x, z) >= 1.0 and self.mound_top(x, z) is None:
                    continue
                for y in range(0, G):
                    s.put(x, y, z, self.subsoil(x, y, z))
                top = self.mound_top(x, z)
                self.heights[(x, z)] = top or G
                for y in range(G, (top or G)):
                    s.put(x, y, z, self.subsoil(x, y, z))
                s.put(x, top or G, z, *self.surface_pair(x, z))

    def surface_pair(self, x, z):
        value = self.surface(x, z)
        return value if isinstance(value, tuple) else (value, None)

    # --- the niche ------------------------------------------------------------------------------------------------

    def masonry(self, x, y, z):
        return self.s.pick((7, SODDEN), (3, 'minecraft:mud_bricks'), (1, 'minecraft:packed_mud'))

    def niche(self):
        s = self.s
        # Shell: walls x4/x10, back z1, façade z7, rows 1..7, over a floor at row 1.
        for x in range(NX0 - 1, NX1 + 2):
            for z in range(NZ0 - 1, FRONT + 1):
                for y in range(FLOOR, 8):
                    s.put(x, y, z, self.masonry(x, y, z))
        # Floor: packed peat field with a mud-brick runner to the basin.
        for x in range(NX0, NX1 + 1):
            for z in range(NZ0, NZ1 + 1):
                s.put(x, FLOOR, z, 'minecraft:mud_bricks' if x == 7 else
                      s.pick((4, 'minecraft:packed_mud'), (3, SODDEN), (1, MUD)))
        # Interior air: rows 2..4 full width, the vault above.
        for x in range(NX0, NX1 + 1):
            for z in range(NZ0, NZ1 + 1):
                for y in range(2, 5):
                    s.air(x, y, z)
                s.put(x, 5, z, BONE_STAIRS, stairs('west', 'top')) if x == NX0 else None
                s.put(x, 5, z, BONE_STAIRS, stairs('east', 'top')) if x == NX1 else None
                if NX0 < x < NX1:
                    s.air(x, 5, z)
                if x == NX0 + 1:
                    s.put(x, 6, z, BONE_STAIRS, stairs('west', 'top'))
                elif x == NX1 - 1:
                    s.put(x, 6, z, BONE_STAIRS, stairs('east', 'top'))
                elif x == 7:
                    s.air(x, 6, z)
                    s.put(x, 7, z, BONE_CHISELED if z in (NZ0, 4, NZ1) else BONE)
            # the vault's back course meets the back wall in bone
            s.put(x, 5, NZ0 - 1, BONE)
        # Base course of bone brick round the room at row 2, a bone-slab cornice under the vault at row 5.
        for z in range(NZ0, NZ1 + 1):
            for x in (NX0 - 1, NX1 + 1):
                s.put(x, 2, z, BONE_CRACKED if h(self.seed, x, z) < 0.3 else BONE)
                s.put(x, 5, z, BONE)
        for x in range(NX0, NX1 + 1):
            s.put(x, 2, NZ0 - 1, BONE_CRACKED if h(self.seed, x, 'b') < 0.25 else BONE)
        # Pilasters at the side walls' ends (z2, z6): bone brick with a chiseled capital.
        for x in (NX0 - 1, NX1 + 1):
            for z in (NZ0, NZ1):
                s.put(x, 3, z, BONE)
                s.put(x, 4, z, BONE_CHISELED)
        self.side_slots()
        self.back_wall()
        self.facade()
        self.contents()

    def side_slots(self):
        """A burial slot three long in each side wall at rows 3 and 4: niche fronts in the wall plane, the recess
        behind cut into the mound."""
        s = self.s
        zs = (3, 4, 5)
        # West wall (x4), facing east: row 3 opened over [bones, a bog body, bones]; row 4 sealed.
        for z in zs:
            s.put(4, 3, z, 'hemomancy:opened_niche', {'facing': 'east'})
            s.put(4, 4, z, 'hemomancy:sealed_niche', {'facing': 'east'})
            s.put(3, 4, z, 'minecraft:packed_mud')
        s.bone(3, 3, 3, 'z')
        s.put(3, 3, 4, 'hemomancy:bog_body', {'facing': 'north', 'harvested': 'false'})
        s.bone(3, 3, 5, 'z')
        # East wall (x10), facing west: row 3 sealed; row 4 broken (a sealed plate, an opened frame, a gap).
        for z in zs:
            s.put(10, 3, z, 'hemomancy:sealed_niche', {'facing': 'west'})
            s.put(11, 3, z, 'minecraft:packed_mud')
        s.put(10, 4, 3, 'hemomancy:sealed_niche', {'facing': 'west'})
        s.put(11, 4, 3, 'minecraft:packed_mud')
        s.put(10, 4, 4, 'hemomancy:opened_niche', {'facing': 'west'})
        s.bone(11, 4, 4, 'z')
        s.put(10, 4, 5, 'minecraft:cobweb')
        s.put(11, 4, 5, 'minecraft:rooted_dirt')

    def back_wall(self):
        s = self.s
        z = NZ0 - 1
        # The skull recess at x7: a chiseled plinth, the recess, a chiseled lintel; bones behind.
        s.put(7, 2, z, BONE_CHISELED)
        s.air(7, 3, z)
        s.put(7, 3, z - 1, BONE)
        s.skull(7, 3, z, 0)
        s.put(7, 4, z, BONE_CHISELED)
        # The two wall memos either side, on full mud brick.
        for x in (6, 8):
            s.put(x, 3, z, SODDEN)
            s.inscription(x, 3, NZ0, 'south', PLACEHOLDER)
        s.put(5, 3, z, BONE)
        s.put(9, 3, z, BONE)
        s.put(5, 4, z, BONE_CHISELED)
        s.put(9, 4, z, BONE_CHISELED)
        # Pale sconces in the back corners, on the bone columns.
        s.put(NX0, 4, NZ0, 'hemomancy:reliquary_sconce', {'facing': 'south', 'flame': 'pale'})
        s.put(NX1, 4, NZ0, 'hemomancy:reliquary_sconce', {'facing': 'south', 'flame': 'pale'})
        # Memo VI's basin before the recess, half full.
        s.put(7, 2, NZ0, 'hemomancy:venous_basin', {'facing': 'south', 'fill': '2'})
        # Corpse wax creeping off the back wall in the east corner.
        s.put(NX1, 3, NZ0, 'hemomancy:adipocere_crust', {'facing': 'south'})

    def facade(self):
        s = self.s
        z = FRONT
        for x in range(NX0 - 1, NX1 + 2):
            for y in range(2, 8):
                s.put(x, y, z, self.masonry(x, y, z))
        # Jambs of bone brick, chiseled where the arch springs.
        for x in (NX0, NX1):
            for y in (2, 3, 4):
                s.put(x, y, z, BONE)
            s.put(x, 5, z, BONE_CHISELED)
        # The door: three wide, one step down into the niche.
        for x in (6, 7, 8):
            s.put(x, FLOOR, z, 'minecraft:mud_bricks')
            s.put(x, 2, z, BONE_STAIRS, stairs('south'))
            s.air(x, 3, z)
            s.air(x, 4, z)
        s.put(6, 5, z, BONE_STAIRS, stairs('west', 'top'))
        s.put(8, 5, z, BONE_STAIRS, stairs('east', 'top'))
        s.air(7, 5, z)
        s.put(7, 6, z, BONE_CHISELED)
        s.put(7, 5, z, 'minecraft:hanging_roots')
        # A bone course over the arch, a stair coping where the façade meets the mound.
        for x in range(NX0, NX1 + 1):
            if x != 7:
                s.put(x, 6, z, BONE)
        for x in range(NX0 - 1, NX1 + 2):
            s.put(x, 8, z, BONE_STAIRS, stairs('north'))
        # Bone-brick wall stubs flanking the door, sunk in the peat.
        for x in (NX0 - 1, NX1 + 1):
            s.put(x, 3, z + 1, BONE_WALL, {})
            s.put(x, 4, z + 1, 'hemomancy:mnemonic_candle', {'lit': 'true'})

    def contents(self):
        s = self.s
        # By the door: the loot barrel with a tithe candle, a jar on a bone block, a body laid on the peat.
        s.barrel(NX0, 2, NZ1, 'east', LOOT)
        s.candle(NX0, 3, NZ1)
        s.put(NX1, 2, NZ1, BONE)
        s.put(NX1, 3, NZ1, 'hemomancy:specimen_jar', {'facing': 'west'})
        s.put(6, 2, 4, 'hemomancy:bog_body', {'facing': 'east', 'harvested': 'false'})
        s.put(NX1, 2, 3, 'minecraft:moss_carpet')
        s.put(NX1, 2, 4, 'minecraft:moss_carpet')
        s.put(NX0, 2, 3, 'minecraft:moss_carpet')
        s.skull(NX1, 2, 5, 9)

    # --- the yard -------------------------------------------------------------------------------------------------

    def pool(self):
        s = self.s
        x0, x1, z0, z1 = POOL
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                s.put(x, 0, z, s.pick((3, MUD), (1, 'minecraft:clay'), (1, 'hemomancy:bog_iron')))
                s.water(x, 1, z)
                s.water(x, 2, z)
        # The rim: wet peat and roots, one row round.
        for x in range(x0 - 1, x1 + 2):
            for z in range(z0 - 1, z1 + 2):
                if x0 <= x <= x1 and z0 <= z <= z1:
                    continue
                s.put(x, 1, z, MUD)
                if (x, z) == (x1 + 1, z0):
                    s.put(x, 2, z, 'hemomancy:bog_iron')
                else:
                    s.put(x, 2, z, *s.pick((3, (MUD, None)), (2, ('minecraft:muddy_mangrove_roots', {'axis': 'y'})),
                                          (1, ('minecraft:rooted_dirt', None))))
        # Two bog bodies laid in the cutting, waterlogged like the swamp's own.
        s.put(3, 1, 10, 'hemomancy:bog_body', {'facing': 'east', 'harvested': 'false'})
        s.put(5, 1, 9, 'hemomancy:bog_body', {'facing': 'south', 'harvested': 'false'})
        s.put(2, 3, 11, 'minecraft:lily_pad')
        s.put(4, 3, 9, 'minecraft:lily_pad')
        s.put(5, 3, 11, 'minecraft:lily_pad')

    def yard(self):
        s = self.s
        # The duckboard: a mangrove landing at the door, then single boards south on the peat.
        for x in (6, 7, 8):
            s.put(x, 3, FRONT + 1, 'minecraft:mangrove_slab', slab())
        for z in range(FRONT + 2, SZ):
            if self.yard_d(7, z) < 1.0 or z <= 11:
                s.put(7, 3, z, 'minecraft:mangrove_slab', slab())
                s.put(7, 2, z, 'minecraft:mangrove_planks' if z % 2 else 'minecraft:stripped_mangrove_log',
                      None if z % 2 else {'axis': 'x'})
        # The lantern post: mangrove fence, a hematic lantern on top.
        for y in (3, 4):
            s.put(6, y, 10, 'minecraft:mangrove_fence', {})
        s.put(6, 5, 10, 'hemomancy:hematic_lantern', {'hanging': 'false'})
        # The stele east of the path, carrying the third memo on its west face, a skull on its cap.
        sx, sz = 10, 10
        s.put(sx, 2, sz, BONE)
        s.put(sx, 3, sz, SODDEN)
        s.put(sx, 4, sz, BONE_CHISELED)
        s.put(sx, 5, sz, SODDEN)
        s.put(sx, 6, sz, BONE_SLAB, slab())
        s.inscription(sx - 1, 4, sz, 'west', PLACEHOLDER)
        s.put(sx + 1, 3, sz, BONE_STAIRS, stairs('west'))
        s.put(sx, 3, sz + 1, BONE_STAIRS, stairs('north'))
        s.bone(sx + 1, 3, sz + 1, 'x')
        # Peat stacks drying east of the mound: cut bricks under slab caps, on a fence rack.
        for (x, z, tall) in ((12, 7, 2), (13, 8, 1), (12, 9, 1)):
            for y in range(3, 3 + tall):
                s.put(x, y, z, 'minecraft:packed_mud' if (x + y + z) % 2 else 'minecraft:mud_bricks')
            s.put(x, 3 + tall, z, 'minecraft:mud_brick_slab', slab())
        s.put(13, 3, 7, 'minecraft:mangrove_fence', {})
        s.put(13, 4, 7, 'minecraft:mangrove_fence', {})
        s.put(13, 5, 7, 'minecraft:mangrove_trapdoor', {'facing': 'west', 'half': 'bottom', 'open': 'false',
                                                         'powered': 'false'})
        # Old bones half out of the peat by the pool.
        s.bone(1, 3, 8, 'x') if self.yard_d(1, 8) < 1.0 else None
        s.skull(6, 3, 12, 2) if self.yard_d(6, 12) < 1.0 else None

    def crown(self):
        """Roots over the mound's shoulders and a dead mangrove stump on its crown."""
        s = self.s
        sx, sz = 10, 2
        top = self.heights.get((sx, sz), G)
        for dy in (1, 2, 3):
            s.put(sx, top + dy, sz, 'minecraft:mangrove_log', {'axis': 'y'})
        s.put(sx + 1, top + 3, sz, 'minecraft:mangrove_log', {'axis': 'x'})
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            x, z = sx + dx, sz + dz
            t = self.heights.get((x, z))
            if t is not None:
                s.put(x, t + 1, z, 'minecraft:mangrove_roots')
        for (x, z) in ((3, 4), (11, 5), (4, 1), (2, 6)):
            t = self.heights.get((x, z))
            if t is not None and t + 1 < SY:
                s.put(x, t + 1, z, 'minecraft:mangrove_roots' if h(self.seed, x, z) < 0.6 else
                      'minecraft:moss_carpet')
        for (x, z) in ((6, 3), (8, 5), (5, 1), (9, 6), (12, 4)):
            t = self.heights.get((x, z))
            if t is not None and t + 1 < SY and s.get(x, t + 1, z) is None:
                s.put(x, t + 1, z, 'minecraft:moss_carpet')

    def build(self):
        self.ground()
        self.niche()
        self.pool()
        self.yard()
        self.crown()
        self.s.finish()
        return self.s

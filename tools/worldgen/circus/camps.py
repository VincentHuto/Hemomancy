"""The five Crimson Troupe teaching camps (chunk 15 T9), one 27 x 12 x 27 template each, built round its discipline's
apparatus.

Row 1 lands on the column's top ground block (the structure starts one below the surface), so row 0 is the camp's
foundation skirt under the yard and its bank. The yard is a rounded square of ring sawdust with a plank laid across it
every third row, never a block of `#minecraft:dirt`, ringed by a low earthwork bank of packed mud under mud-brick
slabs, open to the south where the path runs out.

Every camp has the same bones: a practice ring of radius 5 (the curb, a lacquer track and a venous-stone hub) where
its professors stand, the Threadkeeper's lean-to on the north-west with the Puppeteer's Spindle and a crafting table, a
painted wagon on the north-east, lamp posts at the four quarters and a pennant line over the way in. The apparatus
stands north of the ring: air a rigging tower whose trapeze reaches over a net, blades a knife wheel between two target
boards, pageantry a stilt rack and two weight frames, embers an ember pit, a bellows stand and two hound hoops, memory
a prompt-book table, a trunk, a costume rack and the quarters ledger.

A professor's practical sets two rehearsal threats four blocks east of them, one a block either side
(`CircusPracticalController.begin`); those cells are kept open.
"""
from . import kit
from .kit import AIR, LACQUER, LOG, PLANKS, QUARTER_POLE, SAWDUST, STONE, Site
from wayside.kit import h

SIZE = (27, 12, 27)
GROUND = 1
CX, CZ = 13, 13
RING = (13, 16)
RING_R = 5
FLOOR = GROUND + 1
BANK = 'minecraft:packed_mud'
BANK_SLAB = 'minecraft:mud_brick_slab'
SKIRT = ('minecraft:packed_mud', 'minecraft:mud_bricks')
LANTERN = 'hemomancy:hematic_lantern'

# The professors of each camp and their marks in the practice ring.
PROFESSORS = {
    'air': (('circus_acrobat', 10, 16),),
    'blades': (('circus_knife_thrower', 10, 16),),
    'pageantry': (('circus_stilt_walker', 10, 14), ('circus_strongman', 10, 18)),
    'embers': (('circus_fire_eater', 10, 14), ('circus_beast_tamer', 10, 18)),
    'memory': (('circus_understudy', 10, 16),),
}
THREADKEEPER = (6, 5)
SPINDLE = (7, 5)
CRAFTING_TABLE = (5, 5)
QUARTERS_RECORD = (13, 9)


def name(variant):
    return 'troupe_camp_' + variant


def bound(x, z):
    """The yard's rounded-square distance: under 1 is the yard, a step past it the bank."""
    dx, dz = abs(x - CX) / 12.0, abs(z - CZ) / 12.0
    return (dx ** 4 + dz ** 4) ** 0.25


def entrance(x, z):
    return 11 <= x <= 15 and z >= RING[1] + RING_R + 1


def ground(site, seed):
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            d = bound(x, z)
            if d >= 1.09 and not entrance(x, z):
                continue
            site.put(x, 0, z, SKIRT[int(h(seed, x, z) * 2)])
            if d < 1.0 or entrance(x, z):
                site.put(x, GROUND, z, PLANKS if z % 3 == 0 else SAWDUST)
                for y in range(FLOOR, SIZE[1]):
                    site.air(x, y, z)
            else:
                site.put(x, GROUND, z, BANK)
                site.put(x, FLOOR, z, BANK_SLAB, {'type': 'bottom'})
                for y in range(FLOOR + 1, SIZE[1]):
                    site.air(x, y, z)


def practice_ring(site):
    f = site.frame(0, 0, 0)
    rx, rz = RING
    kit.ring(f, rx, FLOOR, rz, RING_R)
    for dx in range(-RING_R, RING_R + 1):
        for dz in range(-RING_R, RING_R + 1):
            d = (dx * dx + dz * dz) ** 0.5
            if 2.5 <= d < 3.5:
                site.put(rx + dx, GROUND, rz + dz, LACQUER)
            elif abs(dx) <= 1 and abs(dz) <= 1:
                site.put(rx + dx, GROUND, rz + dz, STONE)


def threadkeeper_stall(site):
    """The Threadkeeper's lean-to, open to the south toward the ring, the Spindle and a crafting table under it."""
    kit.lean_to(site.frame(8, 0, 6, 'south'), 0, FLOOR, 0, width=5)
    site.put(SPINDLE[0], FLOOR, SPINDLE[1], 'hemomancy:puppeteers_spindle', {'facing': 'south'})
    site.put(CRAFTING_TABLE[0], FLOOR, CRAFTING_TABLE[1], 'minecraft:crafting_table')


def lamp_post(site, x, z, toward):
    """A quarter pole with a blood-wood arm reaching `toward`, a hematic lantern hung under the arm."""
    f = site.frame(0, 0, 0)
    kit.pole(f, x, FLOOR, z, 4, QUARTER_POLE, collars=(2,))
    dx, dz = kit.STEP[toward]
    site.put(x + dx, FLOOR + 3, z + dz, LOG, {'axis': 'x' if dx else 'z'})
    site.put(x + dx, FLOOR + 2, z + dz, LANTERN, {'hanging': 'true'})


def way_in(site):
    """Two quarter poles either side of the path with a pennant line strung between them."""
    f = site.frame(0, 0, 0)
    z = RING[1] + RING_R + 2
    for x in (11, 15):
        kit.pole(f, x, FLOOR, z, 5, QUARTER_POLE, collars=(3,))
    for x in range(12, 15):
        site.put(x, FLOOR + 3, z, 'hemomancy:circus_pennant_line', {'facing': 'south'})


def apparatus(site, variant):
    f = site.frame(0, 0, 0)
    if variant == 'air':
        kit.rigging_tower(site.frame(13, 0, 3, 'south'), 0, FLOOR, 0, 4)
    else:
        kit.APPARATUS[variant](f, 13, FLOOR, 7)


def build_camp(variant):
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=GROUND, seed=15009 + len(variant))
    ground(site, 15009)
    practice_ring(site)
    threadkeeper_stall(site)
    kit.wagon(site.frame(0, 0, 0), 17, FLOOR + 1, 5)
    for x, z, toward in ((3, 12, 'east'), (23, 13, 'west'), (6, 21, 'east'), (20, 21, 'west')):
        lamp_post(site, x, z, toward)
    way_in(site)
    apparatus(site, variant)
    site.entity('circus_threadkeeper', THREADKEEPER[0], FLOOR, THREADKEEPER[1])
    for role, x, z in PROFESSORS[variant]:
        site.entity(role, x, FLOOR, z)
    site.finish()
    return site


TEMPLATES = [(name(variant), (lambda v: lambda: build_camp(v))(variant))
             for variant in ('air', 'blades', 'pageantry', 'embers', 'memory')]

"""The abandoned stage (chunk 15 T9): a roadside wreck, 17 x 10 x 15, smaller than a camp.

Row 1 lands on the column's top ground block, so row 0 is the wreck's foundation skirt. A rutted stretch of road runs
east to west along the south edge; the troupe's stage wagon lies beside it burnt, its west wheels gone and its roof
broken open, the stage boards run out west of it as a split platform with the stage bill still standing on them. A
dropped wheel is propped by the road, a costume trunk has come off the tail, and a snapped quarter pole leans where the
pennants hung. Nobody is here.
"""
from . import kit
from .kit import AIR, PLANKS, QUARTER_POLE, Site
from wayside.kit import h, outline

NAME = 'troupe_abandoned_stage'
SIZE = (17, 10, 15)
GROUND = 1
FLOOR = GROUND + 1
WAGON = (6, FLOOR + 1, 3)
# The stage bill on the boards, and the board a reader stands on to its west.
RECORD = (5, FLOOR + 3, 5)
READER = (4, FLOOR + 3, 5)
ROAD = range(10, 14)
SKIRT = ('minecraft:packed_mud', 'minecraft:mud_bricks')


def ground(site, seed):
    patch = outline(8, 7, 8.2, 7.4, seed, wobble=0.08)
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            on_road = z in ROAD
            if patch(x, z) >= 1.0 and not on_road:
                continue
            site.put(x, 0, z, SKIRT[int(h(seed, x, z) * 2)])
            if on_road:
                rut = z in (ROAD.start + 1, ROAD.stop - 2)
                site.put(x, GROUND, z, 'minecraft:packed_mud' if rut else 'minecraft:gravel')
            else:
                site.put(x, GROUND, z, 'minecraft:coarse_dirt' if h(seed, 'g', x, z) < .3 else 'minecraft:gravel'
                         if h(seed, 'v', x, z) < .2 else 'minecraft:rooted_dirt')
            for y in range(FLOOR, SIZE[1]):
                site.air(x, y, z)


def debris(site):
    f = site.frame(0, 0, 0)
    # The trunk that came off the tail, and the snapped pole the pennants hung from, its finial in the grass.
    f.piece(12, FLOOR, 7, 'circus_trunk', 'east')
    kit.pole(f, 13, FLOOR, 3, 2, QUARTER_POLE)
    f.piece(14, FLOOR, 4, 'circus_pennant_line', 'east')
    f.slab(11, FLOOR, 4, kit.PLANK_SLAB)


def build():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=GROUND, seed=15010)
    ground(site, 15010)
    kit.stage_wreck(site.frame(0, 0, 0), *WAGON)
    debris(site)
    site.finish()
    return site


TEMPLATES = [(NAME, build)]

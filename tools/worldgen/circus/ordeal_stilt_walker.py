"""The Stilt Walker's stage in their Chamber of Will (2026-11-17): the High Promenade, where the Scarlet Mummer's ordeal
is fought.

The ring as the Stilt Walker saw it from up over the crowd: a ring of radius 9 boarded in dark lacquer with a blood-wood
board across every third row, a sawdust pad round a venous-stone hub where the Mummer is raised, and one chalk ring in
bone at radius 6, inside the troupe's curb and footlights. The floor is dark so the bold telegraphs read on it. Outside the curb, flush with the ring, a wide promenade of dark lacquer planked in
blood-wood, with a worn bone track at radius 15 where the true self's stilts come down, and past it empty seats facing
in and a stand of tall quarter poles, the other stilts, rising into the dark. Two stilt racks stand between the seats.
Open above: the Stilt Walker walks the track and stands about twelve blocks over the ring.
"""
import math

from . import kit
from .kit import BONE, LACQUER, PLANKS, QUARTER_POLE, SAWDUST, STONE, Site
from wayside.kit import h

SIZE = (41, 24, 41)
C = 20
FLOOR = 1
RING = 9
TRACK = 15
SEED = 15040


def _toward_centre(a):
    """The facing nearest to looking from angle `a` on the circle back at the centre."""
    return min(kit.STEP, key=lambda k: (kit.STEP[k][0] + math.cos(a)) ** 2 + (kit.STEP[k][1] + math.sin(a)) ** 2)


def build():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=SEED)
    f = site.frame(0, 0, 0)
    kit.ring(f, C, FLOOR + 1, C, RING + 1, footlights=(0, 45, 90, 135, 180, 225, 270, 315))
    for dx in range(-C, C + 1):
        for dz in range(-C, C + 1):
            d = math.hypot(dx, dz)
            x, z = C + dx, C + dz
            if d < RING + 0.7:
                site.put(x, FLOOR - 1, z, STONE)
                if d <= 1.5:
                    site.put(x, FLOOR, z, STONE)
                elif d < 3.0:
                    site.put(x, FLOOR, z, SAWDUST)
                elif 6.0 <= d < 7.0:
                    # A chalk ring in bone, broken here and there where it has been scuffed over.
                    site.put(x, FLOOR, z, LACQUER if h(SEED, 'scuff', x, z) < 0.12 else BONE)
                else:
                    # Dark lacquer boards, a blood-wood board across every third row: a dark floor the bold
                    # telegraphs read on from anywhere in the ring.
                    site.put(x, FLOOR, z, PLANKS if dz % 3 == 0 else LACQUER)
            elif RING + 1.5 < d < 19.7:
                # The promenade, flush with the ring: lacquer planked in blood-wood every third ring, and the worn bone
                # track where the stilts come down.
                site.put(x, FLOOR - 1, z, STONE)
                if TRACK - 0.5 <= d < TRACK + 0.5:
                    block = BONE if h(SEED, 'track', x, z) < 0.75 else STONE
                elif int(d) % 3 == 0:
                    block = PLANKS
                else:
                    block = LACQUER
                site.put(x, FLOOR, z, block)
    # The stand of tall poles: the other stilts, rising into the dark at uneven heights.
    for i, angle in enumerate(range(22, 360, 45)):
        a = math.radians(angle + 0.5)
        x, z = round(C + 18 * math.cos(a)), round(C + 18 * math.sin(a))
        height = 11 + int(h(SEED, 'pole', i) * 6)
        kit.pole(f, x, FLOOR + 1, z, height, QUARTER_POLE, collars=(4, 8))
    # Empty seats facing in between the poles, in short rows, and two stilt racks.
    for angle in range(0, 360, 45):
        for step in (-6, 0, 6):
            a = math.radians(angle + step)
            x, z = round(C + 19 * math.cos(a)), round(C + 19 * math.sin(a))
            if angle in (90, 270) and step == 0:
                f.tall(x, FLOOR + 1, z, 'circus_stilt_rack', _toward_centre(a))
            else:
                f.piece(x, FLOOR + 1, z, 'circus_seating', _toward_centre(a))
    site.finish()
    return site


TEMPLATE = ('ordeal/stilt_walker', build)

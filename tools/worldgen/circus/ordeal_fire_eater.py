"""The Fire Breather's stage in their Chamber of Will (2026-11-17): the Scorch Ring, where the Cinder Bellows ordeal is
fought.

A ring of radius 9 floored in charred planks burned in concentric bands of packed ash, with an iron grate at its heart
where the Bellows is raised, inside the troupe's curb and footlights. Past the north rim stands the pulpit, a crescent
gallery two blocks over the ring (radius 13 to 18, 110 degrees round north) where the Fire Breather paces out of
reach: its inner lip lined with footlights, fire pits and bellows stands behind, and soot-black flues rising off its
back into the dark. The rest of the apron is low and bare, so the fighter is never cornered against anything but the
curb. Open above.
"""
import math

from . import kit
from .kit import BONE, IRON, LACQUER, STONE, Site
from wayside.kit import h

SIZE = (41, 24, 41)
C = 20
FLOOR = 1
RING = 9
SEED = 15050
CHARRED = 'hemomancy:charred_planks'
ASH = 'hemomancy:packed_ash'
CHARRED_LOG = 'hemomancy:charred_log'
# The pulpit: its inner and outer radius, its half-span round north (degrees) and the row its top block sits on.
PULPIT_IN, PULPIT_OUT, PULPIT_SPAN, PULPIT_TOP = 13, 18, 55, FLOOR + 2


def _bearing(dx, dz):
    """Degrees from north (-z), positive toward east."""
    return math.degrees(math.atan2(dx, -dz))


def _wall(x, y, z):
    roll = h(SEED, 'wall', x, y, z)
    return STONE if roll < 0.6 else BONE if roll < 0.8 else CHARRED


def fire_eater():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=SEED)
    f = site.frame(0, 0, 0)
    kit.ring(f, C, FLOOR + 1, C, RING + 1, footlights=(0, 45, 90, 135, 180, 225, 315))
    for dx in range(-PULPIT_OUT - 1, PULPIT_OUT + 2):
        for dz in range(-PULPIT_OUT - 1, PULPIT_OUT + 2):
            d = math.hypot(dx, dz)
            x, z = C + dx, C + dz
            if not (0 <= x < SIZE[0] and 0 <= z < SIZE[2]):
                continue
            if d <= 2.5:
                # The grate at the heart: iron plate checked with lacquer, where the Bellows is raised.
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, IRON if (dx + dz) % 2 else LACQUER)
            elif d < RING + 0.5:
                # Charred plank burned in bands of ash: every third ring is ash, broken by the hash and flecked.
                band = int(d) % 3 == 0 and h(SEED, 'band', x, z) < 0.7
                fleck = h(SEED, 'fleck', x, z)
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, ASH if band else LACQUER if fleck < 0.12 else CHARRED)
            elif d < RING + 4.5:
                site.put(x, FLOOR - 1, z, STONE if h(SEED, 'apron', x, z) < 0.75 else ASH)
            bearing = _bearing(dx, dz)
            if PULPIT_IN <= d < PULPIT_OUT + 0.5 and abs(bearing) <= PULPIT_SPAN:
                for y in range(FLOOR - 1, PULPIT_TOP):
                    site.put(x, y, z, _wall(x, y, z))
                roll = h(SEED, 'pulpit', x, z)
                site.put(x, PULPIT_TOP, z, CHARRED if roll < 0.55 else LACQUER if roll < 0.8 else ASH)
    # Footlights along the pulpit's inner lip, facing the ring: a dangerous act ought to announce itself.
    for bearing in range(-50, 51, 20):
        a = math.radians(bearing)
        x, z = round(C + (PULPIT_IN + 0.5) * math.sin(a)), round(C - (PULPIT_IN + 0.5) * math.cos(a))
        f.piece(x, PULPIT_TOP + 1, z, 'circus_footlight', 'south')
    # Fire pits and bellows stands at the back of the pulpit, behind where the Fire Breather paces.
    for i, bearing in enumerate(range(-45, 46, 15)):
        a = math.radians(bearing)
        x, z = round(C + (PULPIT_OUT - 0.5) * math.sin(a)), round(C - (PULPIT_OUT - 0.5) * math.cos(a))
        f.piece(x, PULPIT_TOP + 1, z, 'circus_fire_pit' if i % 2 == 0 else 'circus_bellows_stand', 'south')
    # The flues: soot-black stacks off the pulpit's back, of uneven height, each with a fire pit burning on top.
    for bearing in range(-50, 51, 25):
        a = math.radians(bearing)
        x, z = round(C + (PULPIT_OUT + 1) * math.sin(a)), round(C - (PULPIT_OUT + 1) * math.cos(a))
        if not (0 <= z < SIZE[2]):
            continue
        top = SIZE[1] - 3 - int(h(SEED, 'flue', bearing) * 7)
        for y in range(FLOOR - 1, top):
            site.put(x, y, z, CHARRED_LOG if y % 4 else STONE, {'axis': 'y'} if y % 4 else None)
        f.piece(x, top, z, 'circus_fire_pit', 'south')
    site.finish()
    return site


TEMPLATE = ('ordeal/fire_eater', fire_eater)

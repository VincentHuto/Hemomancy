"""The Beast Tamer's stage in the Chamber of Will (2026-11-17): the Empty Menagerie, where the Sanguine Hound's ordeal
is fought.

A sawdust ring of radius 9 raked with blood-wood claw marks and strewn with gnawed bone, a feeding stone of bone at its
heart, the troupe's curb and footlights. Round it, on the lower apron at radius 11, the cage: hematic iron bars four high,
stayed by blood-wood posts, broken by two open doorways (the cage the Tamer learned to leave empty). The Tamer walks the
apron outside it at radius 13.5. Past the apron, in the dark at radius 17, six small empty cages stand open, a pelt hung
inside each and hound hoops propped between them. Open above.
"""
import math

from . import kit
from .kit import BONE, IRON, LACQUER, LACQUER_SLAB, LOG, PLANKS, SAWDUST, STONE, Site
from wayside.kit import h

SIZE = (41, 24, 41)
C = 20
FLOOR = 1
RING = 9
SEED = 15044
BARS = 'hemomancy:hematic_iron_bars'
CAGE = RING + 2
APRON = RING + 6.5
# The two open doorways in the cage, by angle (degrees, 0 toward +x): the Tamer's way in and the Hound's.
DOORS = (90, 270)


def _door(angle):
    return any(abs((angle - d + 180) % 360 - 180) < 9 for d in DOORS)


def build():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=SEED)
    f = site.frame(0, 0, 0)
    kit.ring(f, C, FLOOR + 1, C, RING + 1, footlights=(0, 45, 90, 135, 180, 225, 270, 315))
    for dx in range(-RING - 8, RING + 9):
        for dz in range(-RING - 8, RING + 9):
            d = math.hypot(dx, dz)
            x, z = C + dx, C + dz
            if d <= 1.5:
                # The feeding stone: bone, with a ring of sawdust round it.
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, BONE)
            elif d < RING + 0.5:
                # Sawdust raked with claw marks of blood-wood on every third ring, gnawed bone strewn over it.
                rake = int(d) % 3 == 0 and h(SEED, 'rake', x, z) < 0.5
                bone = h(SEED, 'bone', x, z) < 0.1
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, BONE if bone else PLANKS if rake else SAWDUST)
            elif d < APRON:
                # The apron round the cage, a step down from the ring, where the Tamer walks.
                roll = h(SEED, 'apron', x, z)
                site.put(x, FLOOR - 1, z, STONE if roll < 0.72 else LACQUER if roll < 0.88 else BONE)
    # The cage: bars four high at radius 11, a blood-wood post at every 30 degrees, two doorways left open.
    for dx, dz in kit.circle_cells(CAGE):
        angle = math.degrees(math.atan2(dz, dx)) % 360
        if _door(angle):
            continue
        x, z = C + dx, C + dz
        post = abs((angle + 15) % 30 - 15) < 2.6
        for y in range(FLOOR, FLOOR + 4):
            if post:
                f.log(x, y, z, 'y', LOG)
            else:
                site.put(x, y, z, BARS, {'axis': 'y'})
        if post:
            f.log(x, FLOOR + 4, z, 'y', LOG)
    # The doorways' jambs and a lintel left hanging: the gate taken off its hinges.
    for door in DOORS:
        a = math.radians(door)
        for side in (-1, 1):
            b = a + side * math.radians(11)
            x, z = round(C + CAGE * math.cos(b)), round(C + CAGE * math.sin(b))
            for y in range(FLOOR, FLOOR + 5):
                f.log(x, y, z, 'y', LOG)
    # Six small empty cages out in the dark, doors gone, a pelt hung inside, hoops propped between them.
    for i in range(6):
        a = math.radians(i * 60 + 30)
        cx, cz = round(C + 17 * math.cos(a)), round(C + 17 * math.sin(a))
        _cage(site, f, cx, cz, a)
        b = math.radians(i * 60)
        hx, hz = round(C + 16 * math.cos(b)), round(C + 16 * math.sin(b))
        toward = min(kit.STEP, key=lambda k: (kit.STEP[k][0] + math.cos(b)) ** 2 + (kit.STEP[k][1] + math.sin(b)) ** 2)
        site.put(hx, FLOOR - 1, hz, STONE)
        f.piece(hx, FLOOR, hz, 'circus_hound_hoop', toward)
    site.finish()
    return site


def _cage(site, f, cx, cz, a):
    """A three-by-three cage on a lacquer floor, barred on three sides, its open side toward the ring."""
    # The open side faces the ring: the step from the cage toward the centre.
    inward = min(kit.STEP, key=lambda k: (kit.STEP[k][0] + math.cos(a)) ** 2 + (kit.STEP[k][1] + math.sin(a)) ** 2)
    ox, oz = kit.STEP[inward]
    back = (-ox, -oz)
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            x, z = cx + dx, cz + dz
            site.put(x, FLOOR - 1, z, LACQUER)
            site.put(x, FLOOR + 3, z, LACQUER_SLAB, {'type': 'bottom'})
            edge = abs(dx) == 1 or abs(dz) == 1
            if not edge:
                continue
            corner = abs(dx) == 1 and abs(dz) == 1
            front = (dx, dz) == (ox, oz) or (ox and dx == ox) or (oz and dz == oz)
            for y in range(FLOOR, FLOOR + 3):
                if corner:
                    f.log(x, y, z, 'y', LOG)
                elif front:
                    continue
                elif (dx, dz) == back:
                    site.put(x, y, z, PLANKS)
                else:
                    site.put(x, y, z, BARS, {'axis': 'y'})
    # A pelt on the back wall, inside, and gnawed bone on the floor.
    f.piece(cx, FLOOR + 1, cz, 'circus_pelt_drape', inward)
    site.put(cx + back[0], FLOOR, cz + back[1], PLANKS)
    site.put(cx, FLOOR - 1, cz, BONE)


TEMPLATE = ('ordeal/beast_tamer', build)

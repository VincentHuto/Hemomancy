"""The Acrobat's stage in their Chamber of Will (2026-11-17): the High Loft, where the Veinwing Vulture's ordeal is
fought. Built with the circus kit like the Strongman's Iron Yard (ordeal_stages.strongman), laid by `OrdealStage`
centred on the stage origin with the ring's floor on row 1.

A sawdust ring of radius 9 with crimson-lacquer spokes and a bone hub (read from above, where the Acrobat hangs), the
troupe's curb and footlights round it, and past the curb a safety net over the dark for an apron: the Acrobat's net
goes up before they do. Six tall rigging masts stand at radius 15 at the net's edge, each with a trapeze on an arm
reaching in toward the ring, and lines of hematic chain hang out of the dark between them. Open above: the Acrobat
hangs in the rigging over the net, outside the ring.
"""
import math

from . import kit
from .kit import BONE, CHAIN, LACQUER, QUARTER_POLE, SAWDUST, STONE, Site
from wayside.kit import h

SIZE = (41, 24, 41)
C = 20
FLOOR = 1
RING = 9
MASTS = 15
SEED = 15030


def build():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=SEED)
    f = site.frame(0, 0, 0)
    kit.ring(f, C, FLOOR + 1, C, RING + 1, footlights=(0, 60, 120, 180, 240, 300))
    for dx in range(-RING - 5, RING + 6):
        for dz in range(-RING - 5, RING + 6):
            d = math.hypot(dx, dz)
            x, z = C + dx, C + dz
            if d <= 1.5:
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, BONE)
            elif d < RING + 0.5:
                # Sawdust, crossed by six lacquer spokes from the bone hub, flecked so no stretch reads flat.
                angle = math.degrees(math.atan2(dz, dx)) % 60
                spoke = min(angle, 60 - angle) * math.pi / 180 * d < 0.6
                site.put(x, FLOOR - 1, z, STONE)
                if spoke:
                    site.put(x, FLOOR, z, LACQUER)
                elif h(SEED, 'fleck', x, z) < 0.18 or 5.0 <= d < 5.8:
                    site.put(x, FLOOR, z, 'hemomancy:blood_wood_planks')
                else:
                    site.put(x, FLOOR, z, SAWDUST)
            elif RING + 1.5 <= d < MASTS - 0.5:
                # The safety net over the dark, past the curb: the apron.
                site.put(x, FLOOR, z, 'hemomancy:circus_net')
    # The rigging masts at the net's edge: tall quarter poles collared up their length, each with an arm reaching in
    # toward the ring and a trapeze hung from it; lines of chain hang from the dark between them.
    top = SIZE[1] - 3
    for i, angle in enumerate(range(0, 360, 60)):
        a = math.radians(angle + 30)
        x, z = round(C + MASTS * math.cos(a)), round(C + MASTS * math.sin(a))
        kit.pole(f, x, FLOOR - 1, z, top - FLOOR + 2, QUARTER_POLE, collars=(3, 9, 15))
        # The arm, a log reaching one step in toward the ring, and the trapeze under its end.
        sx, sz = -round(math.cos(a)), -round(math.sin(a))
        axis = 'x' if abs(math.cos(a)) > abs(math.sin(a)) else 'z'
        ax, az = x + sx * (1 if axis == 'x' else 0), z + sz * (1 if axis == 'z' else 0)
        if (ax, az) != (x, z):
            f.log(ax, top - 2, az, axis)
            facing = 'east' if axis == 'z' else 'north'
            f.put(ax, top - 3, az, 'hemomancy:circus_trapeze', {'facing': facing})
        # Footing round each mast so it stands on something at the net's edge.
        for fx, fz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            site.put(x + fx, FLOOR - 1, z + fz, STONE)
    for angle in range(0, 360, 60):
        a = math.radians(angle)
        x, z = round(C + (MASTS - 1) * math.cos(a)), round(C + (MASTS - 1) * math.sin(a))
        bottom = FLOOR + 6 + int(h(SEED, 'line', angle) * 8)
        for y in range(bottom, SIZE[1]):
            site.put(x, y, z, CHAIN, {'axis': 'y'})
    site.finish()
    return site


TEMPLATE = ('ordeal/acrobat', build)

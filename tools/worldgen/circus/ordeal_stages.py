"""The professors' stages in the Chamber of Will (2026-11-17): where a Crimson Troupe ordeal is fought, inside the
professor's own mind. One template per professor, laid by `OrdealStage` centred on the stage origin with the ring's
floor on row 1. The pilot has the Strongman's Iron Yard.

The Strongman's Iron Yard: a ring of radius 9 floored in hematic iron plate chalked with bone, a sawdust heart where
the Hulk is raised, the troupe's curb round it with footlights, weight racks and plinths standing out past the rim
where nothing in the ring can reach, and iron chains (his sinew in here) rising from the rim into the dark. Open above:
the Strongman circles over it.
"""
import math
import os

from . import kit
from .kit import BONE, CHAIN, IRON, SAWDUST, STONE, Site
from wayside.kit import h

SIZE = (41, 24, 41)
C = 20
FLOOR = 1
RING = 9
SEED = 15020


def strongman():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=SEED)
    f = site.frame(0, 0, 0)
    kit.ring(f, C, FLOOR + 1, C, RING + 1, footlights=(0, 45, 90, 135, 180, 225, 270, 315))
    for dx in range(-RING - 4, RING + 5):
        for dz in range(-RING - 4, RING + 5):
            d = math.hypot(dx, dz)
            x, z = C + dx, C + dz
            if d <= 2.5:
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, SAWDUST if (dx + dz) % 2 else 'hemomancy:dark_lacquer_planks')
            elif d < RING + 0.5:
                # Iron plate chalked with bone: a bone block on every third ring of plate, broken by the hash.
                chalk = int(d) % 3 == 0 and h(SEED, 'chalk', x, z) < 0.6 or h(SEED, 'fleck', x, z) < 0.12
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, BONE if chalk else IRON)
            elif d < RING + 4.5:
                # The apron past the rim, lower than the ring: where the racks and plinths stand out of reach.
                site.put(x, FLOOR - 1, z, STONE if h(SEED, 'apron', x, z) < 0.8 else BONE)
    for i, angle in enumerate(range(0, 360, 60)):
        a = math.radians(angle + 30)
        x, z = round(C + (RING + 3) * math.cos(a)), round(C + (RING + 3) * math.sin(a))
        toward = min(kit.STEP, key=lambda k: (kit.STEP[k][0] + math.cos(a)) ** 2 + (kit.STEP[k][1] + math.sin(a)) ** 2)
        if i % 2 == 0:
            f.piece(x, FLOOR, z, 'circus_weight_frame', toward)
        else:
            site.put(x, FLOOR, z, 'hemomancy:circus_plinth')
    # His sinew in here: iron chains rising from the rim into the dark, every 30 degrees, of uneven height.
    for angle in range(15, 360, 30):
        a = math.radians(angle)
        x, z = round(C + (RING + 2) * math.cos(a)), round(C + (RING + 2) * math.sin(a))
        top = SIZE[1] - 1 - int(h(SEED, 'cable', angle) * 6)
        for y in range(FLOOR + 1, top):
            site.put(x, y, z, CHAIN, {'axis': 'y'})
    site.finish()
    return site


def _discovered():
    """The other professors' stages: each is its own module here, ordeal_<role>.py, exposing TEMPLATE = (name, build)."""
    import importlib
    import pkgutil
    found = []
    folder = os.path.dirname(__file__)
    for module in sorted(info.name for info in pkgutil.iter_modules([folder])):
        if module.startswith('ordeal_') and module != 'ordeal_stages':
            found.append(importlib.import_module('.' + module, __package__).TEMPLATE)
    return found


TEMPLATES = [('ordeal/strongman', strongman)] + _discovered()


def ensure_folder(folder):
    os.makedirs(os.path.join(folder, 'ordeal'), exist_ok=True)

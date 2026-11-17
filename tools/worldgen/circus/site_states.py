"""The pavilion's two after-states (chunk 15 T11, D-44), written as overlays on the pavilion template.

An overlay holds only the cells that change, in the pavilion's own coordinates; every other cell is left out. The
game's CircusSiteStateService lays it over a settled pavilion one loaded chunk at a time, and only where the world
still holds the pavilion's own block, so a block a player placed or broke is left alone.

- Ruin (Liberation): the canvas tears open in three places and comes down in heaps on the floor, the plinth and the
  ring are splintered with wrecked panels and boards, the trapezes fall and leave their chains hanging, the nets are
  torn, the box's valance is down, and a third of the lanterns and half the footlights are gone.
  The side rings take damage too: a weight frame down, a run of the Strongman's curb gone, half their footlights out.
- Tenancy (Succession): the successor's banner hangs behind the Ringmaster's podium, crimson with a bone border and a
  bone skull. The game writes the holder's name on it.
"""
import math

from nbtlib import Compound, List, String

from . import pavilion
from .kit import AIR, CANVAS, CHAIN, LACED, PLANKS, Site, WRECKED
from wayside.kit import h

RUIN = 'crimson_troupe_pavilion_ruin'
TENANCY = 'crimson_troupe_pavilion_tenancy'
TEARS = ((24, 26, 4.5), (40, 38, 5.0), (35, 21, 3.5))
BANNER = (32, pavilion.SOUTH_DECK + 3, 46)
M = pavilion.MARGIN  # core coordinates (the tears, the ring, the banner) sit this far east in the template


def overlay(before, after):
    """A Site holding the cells where `after` differs from `before`."""
    site = Site(*pavilion.SIZE, ground=0, seed=0)
    for pos, entry in after.blocks.items():
        if before.blocks.get(pos) != entry:
            site.blocks[pos] = entry
    return site


def open_cell(site, x, y, z):
    return site.get(x, y, z) == AIR


def ruin_after():
    site = pavilion.build()
    seed = 15011
    for (tx, tz, r) in TEARS:
        for x in range(int(tx - r), int(tx + r) + 1):
            for z in range(int(tz - r), int(tz + r) + 1):
                if not pavilion.inside(x, z) or math.hypot(x - tx, z - tz) > r - (h(seed, x, z) - .5):
                    continue
                site.put(x + M, pavilion.roof(x, z), z, AIR)
                if open_cell(site, x + M, 1, z) and h(seed, 'heap', x, z) < .55:
                    site.put(x + M, 1, z, CANVAS if h(seed, 'c', x, z) < .5 else LACED)
                    if open_cell(site, x + M, 2, z) and h(seed, 'top', x, z) < .2:
                        site.put(x + M, 2, z, 'hemomancy:blood_wood_slab', {'type': 'bottom'})
    # The carousel's plinth and the ring are splintered.
    site.put(pavilion.CX + M, 0, pavilion.CZ, WRECKED)
    for dx in range(-pavilion.RING_R + 1, pavilion.RING_R):
        for dz in range(-pavilion.RING_R + 1, pavilion.RING_R):
            x, z = pavilion.CX + M + dx, pavilion.CZ + dz
            if max(abs(dx), abs(dz)) <= 1 or not open_cell(site, x, 1, z):
                continue
            roll = h(seed, 'ring', x, z)
            if roll < .12:
                site.put(x, 1, z, WRECKED)
            elif roll < .3:
                site.put(x, 1, z, 'hemomancy:blood_wood_slab', {'type': 'bottom'})
    yards(site, seed)
    for pos, (name, _props, _n) in list(site.blocks.items()):
        x, y, z = pos
        if name == 'hemomancy:circus_trapeze':
            site.put(x, y, z, CHAIN, {'axis': 'y'})
            for down in (1, 2):
                if open_cell(site, x, y - down, z):
                    site.put(x, y - down, z, CHAIN, {'axis': 'y'})
        elif name == 'hemomancy:circus_net' and h(seed, 'net', *pos) < .4:
            site.put(x, y, z, AIR)
        elif name == 'hemomancy:circus_valance' and y > pavilion.SOUTH_DECK and z < 47 and M <= x < M + 65:
            site.put(x, y, z, AIR)
        elif name == 'hemomancy:hematic_lantern' and h(seed, 'lamp', *pos) < .34:
            site.put(x, y, z, AIR)
        elif name == 'hemomancy:circus_footlight' and h(seed, 'foot', *pos) < .5:
            site.put(x, y, z, AIR)
    return site


def yards(site, seed):
    """The side rings in ruin: one of the Strongman's weight frames down as wreckage, a run of his ring's curb gone."""
    frames = sorted(pos for pos, (name, _p, _n) in site.blocks.items() if name == 'hemomancy:circus_weight_frame')
    if frames:
        x, y, z = frames[0]
        site.put(x, y, z, AIR)
        site.put(x, y - 1, z, WRECKED)
    west_curb = sorted(pos for pos, (name, _p, _n) in site.blocks.items()
                       if name == 'hemomancy:circus_ring_curb' and pos[0] < M + 11)
    for x, y, z in west_curb:
        if h(seed, 'yardcurb', x, z) < .3:
            site.put(x, y, z, AIR)


def tenancy_after():
    site = pavilion.build()
    x, y, z = BANNER
    x += M
    site.put(x, y, z, 'minecraft:red_wall_banner', {'facing': 'north'}, Compound({
        'id': String('minecraft:banner'),
        'patterns': List[Compound]([Compound({'color': String('white'), 'pattern': String('minecraft:border')}),
                                    Compound({'color': String('white'), 'pattern': String('minecraft:skull')})])}))
    return site


def build_ruin():
    return overlay(pavilion.build(), ruin_after())


def build_tenancy():
    return overlay(pavilion.build(), tenancy_after())


TEMPLATES = [(RUIN, build_ruin), (TENANCY, build_tenancy)]

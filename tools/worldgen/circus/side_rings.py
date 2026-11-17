"""The pavilion's two side rings (2026-11-16): the Strongman's yard west of the big top and the Beast Tamer's east, in
the margins the template grew for them. Each is built like a teaching camp's yard (camps.py), stretched along the
tent's flank: a long rounded yard of ring sawdust with a plank across every third row, a low packed-mud bank under
mud-brick slabs round it, a skirt of packed mud and mud bricks finishing the bank, and a practice ring of radius 5 in
the middle (the curb, a lacquer track, a venous-stone hub, footlights). The bank opens toward the tent's artists' door
with a sawdust path and two quarter poles carrying a pennant line; lamp posts stand at the yard's corners.

The pavilion's ground is row 0 (its floor), so the yard is the pavilion's row 0 and its pieces stand on row 1.
"""
import math

from . import kit
from .kit import LACQUER, LOG, PLANKS, QUARTER_POLE, SAWDUST, STONE
from wayside.kit import h

GROUND, FLOOR = 0, 1
CLEAR = 9
BANK = 'minecraft:packed_mud'
BANK_SLAB = 'minecraft:mud_brick_slab'
SKIRT = ('minecraft:packed_mud', 'minecraft:mud_bricks')
LANTERN = 'hemomancy:hematic_lantern'
# The yard's half size across (x) and along the flank (z); the bank and skirt by the rounded-square distance.
HALF_X, HALF_Z = 7.5, 12.5
BANK_AT, SKIRT_AT = 1.0, 1.12
RING_R = 5
# How far the yard centre stands out from the tent wall it faces.
OUT = 10
SEED = 15016


def bound(dx, dz):
    """The yard's rounded-square distance: under 1 is the yard, a step past it the bank, then the skirt."""
    return ((abs(dx) / HALF_X) ** 4 + (abs(dz) / HALF_Z) ** 4) ** 0.25


def build(site, west_wall, east_wall, cz):
    """Both rings, flanking the tent whose west and east walls stand at the given x, centred on row cz."""
    strongman = Yard(site, west_wall - OUT, cz, toward=1, door_x=west_wall)
    tamer = Yard(site, east_wall + OUT, cz, toward=-1, door_x=east_wall)
    for yard in (strongman, tamer):
        yard.lay()
    strongman_dressing(site, strongman)
    tamer_dressing(site, tamer)
    site.entity('circus_strongman', strongman.cx, FLOOR, cz)
    site.entity('circus_beast_tamer', tamer.cx, FLOOR, cz)


class Yard:
    """One side ring. `toward` is +1 when the tent lies east of the yard, -1 when it lies west."""

    def __init__(self, site, cx, cz, toward, door_x):
        self.site, self.cx, self.cz, self.toward, self.door_x = site, cx, cz, toward, door_x
        self.gate = range(cz - 3, cz - 1)  # the rows the bank opens on, in line with the artists' door (z 29-30)

    def opening(self, x, z):
        """The gap in the bank on the tent's side, and the path from it to the door."""
        if z not in self.gate:
            return False
        dx = (x - self.cx) * self.toward
        return dx > 0 and (x - self.door_x) * self.toward < 0

    def lay(self):
        site = self.site
        for x in range(site.size[0]):
            for z in range(site.size[2]):
                d = bound(x - self.cx, z - self.cz)
                if self.opening(x, z):
                    self.floor(x, z)
                    continue
                if d < BANK_AT:
                    self.floor(x, z)
                elif d < SKIRT_AT:
                    site.put(x, GROUND, z, BANK)
                    site.put(x, FLOOR, z, BANK_SLAB, {'type': 'bottom'})
                    self.clear(x, z, FLOOR + 1)
                elif d < SKIRT_AT + 0.08 and h(SEED, 'skirt', x, z) < 0.7:
                    site.put(x, GROUND, z, SKIRT[int(h(SEED, x, z) * 2)])
        self.ring()
        self.way_in()
        for sx in (-1, 1):
            for sz in (-1, 1):
                self.lamp_post(self.cx + sx * 5, self.cz + sz * 10, 'east' if sx < 0 else 'west')

    def floor(self, x, z):
        self.site.put(x, GROUND, z, PLANKS if z % 3 == 0 else SAWDUST)
        self.clear(x, z, FLOOR)

    def clear(self, x, z, y0):
        for y in range(y0, CLEAR):
            if self.site.get(x, y, z) is None:
                self.site.air(x, y, z)

    def ring(self):
        f = self.site.frame(0, 0, 0)
        # Footlights all round but on the line in from the tent (0 and 180 degrees).
        kit.ring(f, self.cx, FLOOR, self.cz, RING_R, footlights=(45, 90, 135, 225, 270, 315))
        for dx in range(-RING_R, RING_R + 1):
            for dz in range(-RING_R, RING_R + 1):
                d = math.hypot(dx, dz)
                x, z = self.cx + dx, self.cz + dz
                if 2.5 <= d < 3.5:
                    self.site.put(x, GROUND, z, LACQUER)
                elif abs(dx) <= 1 and abs(dz) <= 1:
                    self.site.put(x, GROUND, z, STONE)
        # The way into the ring: a two-cell break in the curb facing the tent.
        for dz in (-2, -1):
            x = self.cx + RING_R * self.toward
            if self.site.get(x, FLOOR, self.cz + dz) == 'hemomancy:circus_ring_curb':
                self.site.air(x, FLOOR, self.cz + dz)
                self.site.put(x, GROUND, self.cz + dz, SAWDUST)

    def way_in(self):
        """Two quarter poles either side of the gap with a pennant line strung between them over the path."""
        f = self.site.frame(0, 0, 0)
        x = self.cx + round(HALF_X) * self.toward
        z0, z1 = self.gate[0] - 1, self.gate[-1] + 1
        for z in (z0, z1):
            kit.pole(f, x, FLOOR, z, 5, QUARTER_POLE, collars=(3,))
        for z in range(z0 + 1, z1):
            self.site.put(x, FLOOR + 3, z, 'hemomancy:circus_pennant_line',
                          {'facing': 'east' if self.toward > 0 else 'west'})

    def lamp_post(self, x, z, toward):
        """A quarter pole with a blood-wood arm reaching `toward`, a hematic lantern hung under the arm."""
        f = self.site.frame(0, 0, 0)
        kit.pole(f, x, FLOOR, z, 4, QUARTER_POLE, collars=(2,))
        dx, dz = kit.STEP[toward]
        self.site.put(x + dx, FLOOR + 3, z + dz, LOG, {'axis': 'x' if dx else 'z'})
        self.site.put(x + dx, FLOOR + 2, z + dz, LANTERN, {'hanging': 'true'})


def strongman_dressing(site, yard):
    """The Strongman's stilt rack and weight frames at the yard's north end, two more frames at its south end."""
    f = site.frame(0, 0, 0)
    kit.pageantry_apparatus(f, yard.cx, FLOOR, yard.cz - 9)
    f.piece(yard.cx - 2, FLOOR, yard.cz + 8, 'circus_weight_frame', 'north')
    f.piece(yard.cx + 2, FLOOR, yard.cz + 8, 'circus_weight_frame', 'north')


def tamer_dressing(site, yard):
    """The Beast Tamer's hound hoops on the ring's track, a lean-to at the yard's north end, a trunk and a costume
    rack at its south end."""
    f = site.frame(0, 0, 0)
    f.piece(yard.cx, FLOOR, yard.cz - 3, 'circus_hound_hoop', 'south')
    f.piece(yard.cx - 3 * yard.toward, FLOOR, yard.cz, 'circus_hound_hoop', 'east' if yard.toward < 0 else 'west')
    f.piece(yard.cx, FLOOR, yard.cz + 3, 'circus_hound_hoop', 'north')
    kit.lean_to(site.frame(yard.cx + 2, 0, yard.cz - 9, 'south'), 0, FLOOR, 0, width=5)
    f.piece(yard.cx + 2, FLOOR, yard.cz + 9, 'circus_trunk', 'north')
    f.piece(yard.cx - 2, FLOOR, yard.cz + 9, 'circus_costume_rack', 'north')

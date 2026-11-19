"""The Retired Understudy's stage in the Chamber of Will (2026-11-17): the Prompt Corner, where the Mnemonist Puppet's
ordeal is fought inside the Understudy's own mind. Laid by `OrdealStage` centred on the stage origin, the ring's floor
on row 1.

A theatre's boards, not a sawdust yard: the ring of radius 9 floored in stage boards (dark lacquer and blood wood laid
in rows), four trapdoors framed in hematic iron, a few bone spike marks where the actors stand and a sawdust mark at the heart
where the puppet is raised; the troupe's curb and its footlights round it. North, past the apron, the wings: a raised
platform (three blocks up) where the Understudy paces the prompt corner, with their prompt table, the costume racks and
trunks, the fly ropes rising from its back into the dark and a dark flat of boards behind it, all framed by a proscenium of
venous stone with a lacquer header, a valance and the house curtains drawn back. The rest of the apron is the dressing
side: racks and trunks out of reach of the ring. Open above.
"""
import math

from . import kit
from .kit import BONE, CHAIN, IRON, LACQUER, PLANKS, SAWDUST, STONE, WAINSCOT, LACED, Site
from wayside.kit import h

TEMPLATE_NAME = 'ordeal/understudy'
SIZE = (41, 24, 41)
C = 20
FLOOR = 1
RING = 9
SEED = 15060
# The wings: an arc north of the ring (270 degrees, toward -z), from this radius out, this many degrees either side.
WINGS_IN, WINGS_OUT, WINGS_SPAN = 12.5, 17.5, 35
# The platform's top row: the Understudy's feet stand on it, two blocks over the ring's floor.
PLATFORM_TOP = FLOOR + 2
CURTAIN = 'minecraft:red_wool'
DARK_CURTAIN = 'minecraft:red_terracotta'
# Bone spike marks on the boards, where the actors stand.
MARKS = ((0, -6), (-3, 3), (3, 3), (6, -2), (-6, -2), (0, 7))


def angle_of(dx, dz):
    return math.degrees(math.atan2(dz, dx)) % 360


def in_wings(dx, dz, inner=WINGS_IN, outer=WINGS_OUT):
    d = math.hypot(dx, dz)
    return inner <= d <= outer and abs(angle_of(dx, dz) - 270) <= WINGS_SPAN


def facing_centre(dx, dz):
    """The kit facing that looks from (dx, dz) back toward the ring's centre."""
    return min(kit.STEP, key=lambda k: (kit.STEP[k][0] + dx) ** 2 + (kit.STEP[k][1] + dz) ** 2)


def build():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=SEED)
    f = site.frame(0, 0, 0)
    kit.ring(f, C, FLOOR + 1, C, RING + 1, footlights=(0, 45, 90, 135, 180, 225, 270, 315))
    traps = [(round(5 * math.cos(math.radians(a))), round(5 * math.sin(math.radians(a)))) for a in (45, 135, 225, 315)]
    for dx in range(-RING - 4, RING + 5):
        for dz in range(-RING - 4, RING + 5):
            d = math.hypot(dx, dz)
            x, z = C + dx, C + dz
            if d <= 1.6:
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, SAWDUST)
            elif d < RING + 0.5:
                site.put(x, FLOOR - 1, z, STONE)
                # Stage boards: dark lacquer, a blood-wood board every third row with its joints staggered; each of
                # the four trapdoors framed in hematic iron, and a few bone spike marks where an actor stands.
                trap = any(max(abs(dx - tx), abs(dz - tz)) == 1 for tx, tz in traps)
                if trap:
                    block = IRON
                elif (dx, dz) in MARKS:
                    block = BONE
                elif dz % 3 == 0 and h(SEED, 'joint', (dx + 40) // 4, dz) < 0.85 or h(SEED, 'end', x, z) < 0.18:
                    block = PLANKS
                else:
                    block = LACQUER
                site.put(x, FLOOR, z, block)
            elif d < RING + 4.5:
                # The apron past the rim, lower than the ring.
                site.put(x, FLOOR - 1, z, STONE if h(SEED, 'apron', x, z) < 0.85 else BONE)
    wings(site, f)
    proscenium(site, f)
    # The dressing side of the apron: racks and trunks standing out of reach, facing the ring.
    for i, angle in enumerate((20, 55, 90, 125, 160)):
        a = math.radians(angle)
        dx, dz = round((RING + 3) * math.cos(a)), round((RING + 3) * math.sin(a))
        name = 'circus_costume_rack' if i % 2 == 0 else 'circus_trunk'
        f.piece(C + dx, FLOOR, C + dz, name, facing_centre(dx, dz))
    site.finish()
    return site


def wings(site, f):
    """The raised prompt corner north of the ring: boards on a wainscot front, its fittings, the fly ropes and the flat."""
    for dx in range(-20, 21):
        for dz in range(-20, 0):
            if not in_wings(dx, dz):
                continue
            x, z = C + dx, C + dz
            for y in range(FLOOR - 1, PLATFORM_TOP):
                site.put(x, y, z, STONE)
            edge = not in_wings(dx, dz + 1)
            site.put(x, PLATFORM_TOP, z, WAINSCOT if edge else (LACQUER if (dx + dz) % 2 else PLANKS))
            if edge:
                # Its front toward the ring: a lit board edge, a stair down onto the apron in front of it.
                site.put(x, PLATFORM_TOP - 1, z, WAINSCOT)
    # A row of footlights along the wings' lip, shining down on the ring.
    for dx in (-6, -2, 2, 6):
        for dz in range(-12, -20, -1):
            if in_wings(dx, dz) and not in_wings(dx, dz + 1):
                f.put(C + dx, PLATFORM_TOP + 1, C + dz, 'hemomancy:circus_footlight', {'facing': 'south'})
                break
    # The prompt corner's fittings, back from the lip.
    f.piece(C - 7, PLATFORM_TOP + 1, C - 15, 'circus_prompt_table', 'south')
    f.piece(C + 7, PLATFORM_TOP + 1, C - 15, 'circus_costume_rack', 'south')
    f.piece(C + 9, PLATFORM_TOP + 1, C - 13, 'circus_trunk', 'south')
    f.piece(C - 9, PLATFORM_TOP + 1, C - 13, 'circus_trunk', 'south')
    # The flat behind the wings, dark boards between wainscot posts with a canvas panel here and there, and the fly ropes in front of it.
    for dx in range(-11, 12):
        dz = -round(math.sqrt(max(0, 18.5 ** 2 - dx ** 2)))
        x, z = C + dx, C + dz
        if not (0 <= z < SIZE[2]):
            continue
        for y in range(PLATFORM_TOP + 1, PLATFORM_TOP + 10):
            # Dark boards in the main, so the Understudy stands out against it: blood-wood battens, a canvas panel.
            if dx % 4 == 0:
                block = WAINSCOT if y < PLATFORM_TOP + 3 else STONE
            elif y == PLATFORM_TOP + 1:
                block = WAINSCOT
            elif dx % 8 == 2 and PLATFORM_TOP + 4 <= y <= PLATFORM_TOP + 7:
                block = LACED
            else:
                block = PLANKS if dx % 2 else LACQUER
            site.put(x, y, z, block)
    for dx in range(-9, 10, 3):
        dz = -round(math.sqrt(max(0, 17 ** 2 - dx ** 2)))
        top = SIZE[1] - 1 - int(h(SEED, 'fly', dx) * 5)
        for y in range(PLATFORM_TOP + 1, top):
            site.put(C + dx, y, C + dz, CHAIN, {'axis': 'y'})
        # A sandbag counterweight on each line.
        site.put(C + dx, PLATFORM_TOP + 3 + int(h(SEED, 'weight', dx) * 4), C + dz, 'minecraft:red_terracotta')


def proscenium(site, f):
    """Two venous-stone piers framing the wings, a lacquer header between them with a valance, the curtains drawn back."""
    top = FLOOR + 13
    piers = []
    for side in (-1, 1):
        a = math.radians(270 + side * (WINGS_SPAN + 6))
        dx, dz = round(13 * math.cos(a)), round(13 * math.sin(a))
        piers.append((dx, dz))
        for y in range(FLOOR - 1, top + 1):
            for ox in (0, side):
                for oz in (0, -1):
                    block = BONE if y % 4 == 0 else STONE
                    site.put(C + dx + ox, y, C + dz + oz, block)
        # The house curtain drawn back against the pier, gathered at a tie-back two-thirds of the way down.
        for y in range(FLOOR + 3, top):
            width = 2 if y > FLOOR + 8 else 1
            for k in range(1, width + 1):
                site.put(C + dx - side * k, y, C + dz, CURTAIN if (y + k) % 3 else DARK_CURTAIN)
    (lx, lz), (rx, rz) = piers
    z = C + min(lz, rz)
    for x in range(C + lx - 1, C + rx + 2):
        site.put(x, top, z, LACQUER)
        site.put(x, top, z - 1, LACQUER)
        site.put(x, top + 1, z, BONE if (x - C) % 4 == 0 else LACQUER)
    for x in range(C + lx + 1, C + rx):
        f.put(x, top - 1, z, 'hemomancy:circus_valance', {'facing': 'south'})


TEMPLATE = (TEMPLATE_NAME, build)

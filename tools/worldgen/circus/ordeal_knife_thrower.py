"""The Dagger Thrower's stage in their Chamber of Will (2026-11-17): the Throwing Line, where the Marrow Juggler
ordeal is fought.

The ring is the target. Its floor is laid as a knife act's board in concentric bands of dark lacquer and blood-wood
planks, chalked in bone at radius 3 and 6 round a bone bullseye, inside the troupe's curb and footlights. Past the
north rim the throwing gallery stands two blocks over the ring (radius 14 to 17, 120 degrees round north), lacquer on
venous stone with a blood-wood rail along its lip: the Dagger Thrower paces it out of reach. Knife wheels stand at its
back, and hematic chains rise off it into the dark like the lines a knife act strings. Target boards and trunks stand
round the low apron on the south side, past the curb, where nothing in the ring can reach. Open above.
"""
import math

from . import kit
from .kit import BONE, CHAIN, FENCE, LACQUER, PLANKS, STONE, Site
from wayside.kit import h

SIZE = (41, 24, 41)
C = 20
FLOOR = 1
RING = 9
SEED = 15030
# The gallery: inner and outer radius, half its span round north (degrees), and the row its walking surface sits on.
GALLERY_IN, GALLERY_OUT, GALLERY_SPAN, GALLERY_TOP = 14, 17.5, 60, FLOOR + 2


def _bearing(dx, dz):
    """Degrees from north (-z), positive toward east."""
    return math.degrees(math.atan2(dx, -dz))


def _board(d, x, z):
    """The ring's floor as a target board: a bone bullseye, bone chalk at 3 and 6, lacquer and plank bands between."""
    band = int(round(d))
    if band <= 1:
        return BONE
    if band in (3, 6):
        # Chalk worn through here and there where the knives went in.
        return LACQUER if h(SEED, 'nick', x, z) < 0.12 else BONE
    if band in (2, 5, 8):
        return LACQUER
    return PLANKS


def knife_thrower():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=SEED)
    f = site.frame(0, 0, 0)
    kit.ring(f, C, FLOOR + 1, C, RING + 1, footlights=(0, 45, 90, 135, 180, 225, 270, 315))
    for dx in range(-20, 21):
        for dz in range(-20, 21):
            d = math.hypot(dx, dz)
            x, z = C + dx, C + dz
            if d < RING + 0.5:
                site.put(x, FLOOR - 1, z, STONE)
                site.put(x, FLOOR, z, _board(d, x, z))
            elif d < RING + 4.5:
                # The apron past the rim, lower than the ring.
                site.put(x, FLOOR - 1, z, STONE if h(SEED, 'apron', x, z) < 0.75 else BONE)
            if GALLERY_IN <= d <= GALLERY_OUT and abs(_bearing(dx, dz)) <= GALLERY_SPAN:
                for y in range(FLOOR - 1, GALLERY_TOP):
                    site.put(x, y, z, STONE if h(SEED, 'gallery', x, y, z) < 0.85 else BONE)
                # The walking surface laid in boards running round the gallery: lacquer and plank by radius.
                site.put(x, GALLERY_TOP, z, LACQUER if int(d) % 2 == 0 else PLANKS)
    # The rail along the gallery's lip, broken where the throws go out over the ring.
    for dx in range(-20, 21):
        for dz in range(-20, 21):
            d = math.hypot(dx, dz)
            bearing = _bearing(dx, dz)
            if GALLERY_IN <= d < GALLERY_IN + 1 and abs(bearing) <= GALLERY_SPAN and int(abs(bearing)) % 20 > 4:
                site.put(C + dx, GALLERY_TOP + 1, C + dz, FENCE)
    # Knife wheels at the gallery's back, facing the ring.
    for bearing in (-40, 0, 40):
        a = math.radians(bearing)
        x, z = round(C + 16.5 * math.sin(a)), round(C - 16.5 * math.cos(a))
        f.tall(x, GALLERY_TOP + 1, z, 'circus_knife_wheel', 'south')
    # The act's lines: hematic chains rising off the gallery's back edge into the dark, of uneven height.
    for bearing in range(-55, 56, 15):
        if bearing in (-40, 0, 40) or abs(bearing) == 40:
            continue
        a = math.radians(bearing)
        x, z = round(C + 17 * math.sin(a)), round(C - 17 * math.cos(a))
        top = SIZE[1] - 1 - int(h(SEED, 'line', bearing) * 7)
        for y in range(GALLERY_TOP + 1, top):
            site.put(x, y, z, CHAIN, {'axis': 'y'})
    # Target boards and trunks round the low south apron, facing in, out of the ring's reach.
    for i, bearing in enumerate(range(110, 260, 30)):
        a = math.radians(bearing)
        x, z = round(C + 12 * math.sin(a)), round(C - 12 * math.cos(a))
        toward = min(kit.STEP, key=lambda k: (kit.STEP[k][0] + math.sin(a)) ** 2 + (kit.STEP[k][1] - math.cos(a)) ** 2)
        f.piece(x, FLOOR, z, 'circus_target_board' if i % 2 == 0 else 'circus_trunk', toward)
    site.finish()
    return site


TEMPLATE = ('ordeal/knife_thrower', knife_thrower)

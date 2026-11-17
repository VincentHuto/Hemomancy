"""Sprites for core-loop Track 9: the Living Land and Venous Cling memory overlays and the Hematic Cleats icon.

The two overlays keep to the frame the other memory overlays use (columns and rows 4 to 11, the four corner pixels clear) in the
Animus palette of the Blood Lash and Tendon Line overlays. The cleats are the hematic iron boots shod with bone: spurs
under each sole and a bone stud on each toe. Hematic iron and bone only, no copper.

The worn cleats' texture (textures/models/armor/hematic_cleats.png, 64 x 32) skins HematicCleatsModel: a hematic iron
shell and sole, a dark sinew wrap at the ankle, bone toe and heel spurs and bone studs. 16 texels per block, as the
model's boxes are drawn at one texel per pixel.

    python tools/model_export/living_land_sprites.py
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ITEMS = ROOT / 'src/main/resources/assets/hemomancy/textures/item'
ARMOR = ROOT / 'src/main/resources/assets/hemomancy/textures/models/armor'
MEMORIES = ITEMS / 'memories'

# Animus palette (memory_tendon_line_overlay): pale flesh, clotted blood, blood, bright blood; plus earth for the ground.
ANIMUS = {'A': (255, 199, 166), 'B': (173, 52, 78), 'C': (234, 60, 60), 'D': (245, 108, 131),
          'E': (126, 98, 82), 'F': (88, 66, 58)}

GLYPHS = {
    # A tooth of ground rising out of a level of earth, bound by a red vein.
    'memory_living_land_overlay': [
        '.......AA.......',
        '......ACCA......',
        '......ECBE......',
        '......EBCE......',
        '.....EECBEE.....',
        '....FFEDCEFF....',
        '....F.FFFF.F....',
    ],
    # A wall edge with a hand rooted to it by three veins.
    'memory_venous_cling_overlay': [
        '.....EF.........',
        '.....EF.CA......',
        '.....ECCDAA.....',
        '.....EF.CAA.....',
        '.....ECCDA......',
        '.....EF.C.......',
        '.....EF.........',
    ],
}

BONE = (243, 217, 191)
BONE_SHADE = (191, 165, 154)


def overlays():
    for name, rows in GLYPHS.items():
        image = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        for dy, row in enumerate(rows):
            for x, key in enumerate(row):
                if key != '.':
                    image.putpixel((x, 4 + dy), ANIMUS[key] + (255,))
        image.save(MEMORIES / f'{name}.png')
        print('wrote', name)


def cleats():
    boots = Image.open(ITEMS / 'hematic_iron_boots.png').convert('RGBA')
    width, height = boots.size
    lowest = max(y for y in range(height) for x in range(width) if boots.getpixel((x, y))[3] > 0)
    image = boots.copy()
    # Each boot's sole: spurs hang one pixel under every other sole pixel, the outer ones two.
    for x in range(width):
        if boots.getpixel((x, lowest))[3] == 0:
            continue
        if x % 2 == 0 and lowest + 1 < height:
            image.putpixel((x, lowest + 1), BONE + (255,))
            if lowest + 2 < height and boots.getpixel((x - 1 if x > 0 else x, lowest))[3] == 0:
                image.putpixel((x, lowest + 2), BONE_SHADE + (255,))
    # A bone stud on each toe: the outermost filled pixel of the second-lowest row on each side.
    row = lowest - 1
    filled = [x for x in range(width) if boots.getpixel((x, row))[3] > 0]
    if filled:
        image.putpixel((filled[0], row), BONE + (255,))
        image.putpixel((filled[-1], row), BONE + (255,))
    image.save(ITEMS / 'hematic_cleats.png')
    print('wrote hematic_cleats')


IRON = (84, 78, 80)
IRON_DARK = (54, 48, 50)
IRON_LIGHT = (122, 114, 116)
SINEW = (112, 26, 34)
SINEW_DARK = (70, 14, 20)


def box_faces(u, v, w, h, d):
    """The vanilla cube UV layout at texture offset (u, v) for a w x h x d box: (name, x0, y0, x1, y1) per face."""
    return [('top', u + d, v, u + d + w, v + d), ('bottom', u + d + w, v, u + d + 2 * w, v + d),
            ('east', u, v + d, u + d, v + d + h), ('north', u + d, v + d, u + d + w, v + d + h),
            ('west', u + d + w, v + d, u + 2 * d + w, v + d + h), ('south', u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h)]


def paint_box(image, u, v, w, h, d, shade):
    for name, x0, y0, x1, y1 in box_faces(u, v, w, h, d):
        for y in range(y0, y1):
            for x in range(x0, x1):
                image.putpixel((x, y), shade(name, x - x0, y - y0, x1 - x0, y1 - y0) + (255,))


def worn_cleats():
    import random
    rnd = random.Random(9101)
    image = Image.new('RGBA', (64, 32), (0, 0, 0, 0))

    def iron(name, x, y, w, h):
        if name == 'top':
            return IRON_LIGHT
        if name == 'bottom':
            return IRON_DARK
        base = IRON_LIGHT if y == 0 else IRON_DARK if y == h - 1 else IRON
        if rnd.random() < 0.12:  # scratches in the hematic iron
            base = tuple(min(255, c + 18) for c in base)
        return base

    def sinew(name, x, y, w, h):
        return SINEW_DARK if (x + y) % 3 == 0 else SINEW

    def sole(name, x, y, w, h):
        if name in ('top', 'bottom'):
            return IRON_DARK if (x + y) % 4 else IRON
        return IRON_DARK

    def bone(name, x, y, w, h):
        return BONE_SHADE if name in ('bottom', 'west') or (name != 'top' and y == h - 1) else BONE

    paint_box(image, 0, 0, 4, 5, 4, iron)       # shell
    paint_box(image, 24, 0, 4, 1, 4, sinew)     # sinew wrap
    paint_box(image, 0, 10, 5, 1, 6, sole)      # sole plate
    paint_box(image, 0, 18, 1, 1, 4, bone)      # toe spurs
    paint_box(image, 12, 18, 1, 1, 4, bone)     # heel spur
    paint_box(image, 24, 18, 1, 1, 1, bone)     # studs
    paint_box(image, 30, 18, 3, 1, 1, bone)     # side spurs
    ARMOR.mkdir(parents=True, exist_ok=True)
    image.save(ARMOR / 'hematic_cleats.png')
    print('wrote models/armor/hematic_cleats')


if __name__ == '__main__':
    overlays()
    cleats()
    worn_cleats()

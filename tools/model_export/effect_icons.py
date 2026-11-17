"""Own art for the mob effects that shared a byte-identical icon (VX-16, FX-19).

Each icon is drawn from a few shapes on an 18 x 18 grid, the size vanilla draws an effect at, so every texel lands on
one screen texel. A shape is shaded light on its upper-left edge and dark on its lower-right edge, then the whole icon
gets the dark outline the mod's other effect icons use. In each set that shared one picture, the effect the picture
was drawn for keeps it; this writes the others.

    python tools/model_export/effect_icons.py            # write the icons
    python tools/model_export/effect_icons.py --preview  # also build/effect_icons_preview.png
"""
import argparse
import math

from PIL import Image, ImageDraw

import unstained_weapons as uw

OUT = uw.ASSETS / 'textures/mob_effect'
SIZE = 18

OUTLINE = (34, 8, 16)
CRIMSON = (176, 24, 40)
BLOOD_DARK = (104, 10, 28)
BLOOD_SHINE = (232, 96, 104)
BONE = (214, 200, 172)
BONE_DARK = (150, 134, 110)
IVORY = (240, 232, 214)
IRON = (98, 92, 104)
IRON_DARK = (58, 52, 64)
IRON_LIGHT = (150, 144, 156)
LACQUER = (70, 20, 32)
LACQUER_LIGHT = (116, 36, 48)
STONE = (88, 76, 104)
STONE_LIGHT = (134, 118, 156)
FLESH = (206, 140, 130)
FLESH_DARK = (160, 92, 90)
SCAR = (236, 170, 168)
FROST = (196, 232, 246)
FROST_DARK = (110, 170, 204)
EMBER = (244, 150, 52)
EMBER_LIGHT = (255, 214, 120)
VIOLET = (134, 118, 176)
VIOLET_LIGHT = (196, 182, 232)
SPARK = (255, 236, 150)
BRINE = (64, 132, 128)
BRINE_LIGHT = (124, 188, 176)
VIVIANITE = (92, 120, 168)
VIVIANITE_LIGHT = (150, 182, 222)
WAX = (154, 22, 34)
WAX_LIGHT = (212, 64, 70)


class Icon:
    def __init__(self):
        self.px = {}

    def fill(self, cells, base, light=None, dark=None):
        """Paints cells, lighter where the cell above-left is outside the shape, darker where below-right is."""
        cells = set(cells)
        for x, y in cells:
            if not (0 <= x < SIZE and 0 <= y < SIZE):
                continue
            colour = base
            if light and ((x - 1, y) not in cells or (x, y - 1) not in cells):
                colour = light
            if dark and ((x + 1, y) not in cells or (x, y + 1) not in cells):
                colour = dark
            self.px[(x, y)] = colour
        return self

    def dot(self, x, y, colour):
        self.px[(x, y)] = colour
        return self

    def image(self):
        img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
        for (x, y), colour in self.px.items():
            img.putpixel((x, y), colour + (255,))
        for y in range(SIZE):
            for x in range(SIZE):
                if (x, y) in self.px:
                    continue
                if any((x + dx, y + dy) in self.px for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    img.putpixel((x, y), OUTLINE + (255,))
        return img


def disc(cx, cy, r):
    return {(x, y) for x in range(SIZE) for y in range(SIZE) if math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= r}


def rect(x0, y0, x1, y1):
    return {(x, y) for x in range(x0, x1 + 1) for y in range(y0, y1 + 1)}


def poly(points):
    def inside(px, py):
        hit = False
        for (ax, ay), (bx, by) in zip(points, points[1:] + points[:1]):
            if (ay > py) != (by > py) and px < ax + (py - ay) * (bx - ax) / (by - ay):
                hit = not hit
        return hit
    return {(x, y) for x in range(SIZE) for y in range(SIZE) if inside(x + 0.5, y + 0.5)}


def line(x0, y0, x1, y1):
    cells, steps = set(), max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(steps + 1):
        cells.add((round(x0 + (x1 - x0) * i / steps), round(y0 + (y1 - y0) * i / steps)))
    return cells


def drop(cx, cy, r, height):
    """A blood drop: a disc with a point rising above it."""
    body = disc(cx, cy, r)
    tip = poly([(cx, cy - height), (cx + r * 0.85, cy - r * 0.2), (cx - r * 0.85, cy - r * 0.2)])
    return body | tip


# ---------------------------------------------------------------- the icons

def grave_debt():
    """A headstone split by a crack that bleeds down its face."""
    icon = Icon()
    stone = (disc(9, 7, 5) & rect(0, 0, 17, 7)) | rect(4, 7, 13, 14)
    icon.fill(stone, STONE, STONE_LIGHT, IRON_DARK)
    icon.fill(rect(2, 15, 15, 16), BLOOD_DARK, CRIMSON)
    for x, y in ((8, 4), (9, 5), (8, 6), (9, 7), (9, 8)):
        icon.dot(x, y, OUTLINE)
    icon.fill(rect(9, 9, 9, 13), CRIMSON).dot(9, 13, BLOOD_DARK).dot(10, 10, CRIMSON)
    return icon


def insatiable_hunger():
    """A round maw, ringed with teeth, that is never shut."""
    icon = Icon()
    icon.fill(disc(9, 9, 7.5), CRIMSON, BLOOD_SHINE, BLOOD_DARK)
    icon.fill(disc(9, 9, 4.6), OUTLINE)
    icon.fill(disc(9, 9.5, 2.6), LACQUER)
    for angle in range(0, 360, 45):
        tx = 9 + 4.0 * math.cos(math.radians(angle + 22))
        ty = 9 + 4.0 * math.sin(math.radians(angle + 22))
        icon.dot(int(tx), int(ty), IVORY)
    return icon


def blood_drunkenness():
    """A brimming iron goblet sloshing blood over its lip."""
    icon = Icon()
    icon.fill(poly([(3, 3), (15, 3), (12, 9), (6, 9)]), IRON, IRON_LIGHT, IRON_DARK)
    icon.fill(rect(4, 3, 14, 4), CRIMSON, BLOOD_SHINE)
    icon.fill(rect(8, 10, 9, 12), IRON, IRON_LIGHT, IRON_DARK)
    icon.fill(rect(5, 13, 12, 14), IRON, IRON_LIGHT, IRON_DARK)
    icon.fill(drop(2, 7, 1.3, 2.4) | drop(15, 9, 1.3, 2.4) | rect(3, 4, 3, 5), CRIMSON, BLOOD_SHINE)
    icon.fill(rect(10, 1, 11, 2), CRIMSON, BLOOD_SHINE)
    return icon


def conductive_mark():
    """A brand ring burnt into the skin, with a spark caught in it."""
    icon = Icon()
    icon.fill(disc(9, 9, 7.5) - disc(9, 9, 5.2), BLOOD_DARK, CRIMSON)
    icon.fill(line(11, 3, 8, 8) | line(8, 8, 11, 9) | line(11, 9, 7, 15), SPARK)
    icon.dot(10, 4, IVORY).dot(9, 12, IVORY)
    return icon


def monolithic_dislocation():
    """A standing stone knocked out of its place, its old outline left behind."""
    icon = Icon()
    for x, y in line(4, 2, 4, 15) | line(4, 2, 9, 2) | line(9, 2, 9, 15) | line(4, 15, 9, 15):
        icon.dot(x, y, VIOLET)
    slab = poly([(9, 3), (14, 4), (12, 16), (7, 15)])
    icon.fill(slab, STONE, STONE_LIGHT, IRON_DARK)
    icon.fill(line(10, 6, 11, 9) | line(11, 9, 10, 12), VIOLET_LIGHT)
    return icon


def counterfeit_scar():
    """A stitched scar on a patch of skin that does not match the skin round it."""
    icon = Icon()
    icon.fill(poly([(2, 5), (13, 2), (16, 13), (5, 16)]), FLESH, SCAR, FLESH_DARK)
    icon.fill(line(4, 13, 13, 4) | line(5, 13, 13, 5), BLOOD_DARK, CRIMSON)
    for x, y in ((6, 11), (9, 8), (12, 5)):
        icon.dot(x - 1, y - 1, IVORY).dot(x + 1, y + 1, IVORY)
    return icon


def hemolysis():
    """A red cell split open, its pieces flying."""
    icon = Icon()
    cell = disc(8, 10, 6)
    icon.fill(cell, CRIMSON, BLOOD_SHINE, BLOOD_DARK)
    icon.fill(disc(8, 10, 2.6), BLOOD_DARK)
    for x, y in line(5, 16, 7, 12) | line(7, 12, 6, 9) | line(6, 9, 9, 6) | line(9, 6, 10, 3):
        icon.dot(x, y, OUTLINE)
    for cx, cy in ((14, 3), (16, 7), (3, 2)):
        icon.fill(disc(cx, cy, 1.2), CRIMSON, BLOOD_SHINE)
    return icon


def web_mobility():
    """A web and the strand its walker runs along."""
    icon = Icon()
    cells = set()
    for angle in range(0, 360, 60):
        cells |= line(9, 9, round(9 + 7.4 * math.cos(math.radians(angle))), round(9 + 7.4 * math.sin(math.radians(angle))))
    for r in (3, 6):
        points = [(round(9 + r * math.cos(math.radians(a))), round(9 + r * math.sin(math.radians(a)))) for a in range(0, 360, 60)]
        for a, b in zip(points, points[1:] + points[:1]):
            cells |= line(*a, *b)
    icon.fill(cells, BONE)
    icon.fill(disc(9, 9, 1.6), LACQUER, LACQUER_LIGHT)
    return icon


def ward_shield():
    return poly([(3, 2), (15, 2), (15, 9), (9, 16), (3, 9)])


def wither_resistance():
    """A bone ward over a black skull."""
    icon = Icon()
    icon.fill(ward_shield(), BONE, IVORY, BONE_DARK)
    icon.fill(disc(9, 8, 3.4) | rect(7, 9, 11, 12), IRON_DARK)
    icon.dot(8, 8, OUTLINE).dot(10, 8, OUTLINE).dot(8, 11, BONE_DARK).dot(10, 11, BONE_DARK)
    return icon


def explosion_resistance():
    """A hematic iron ward that a blast breaks against."""
    icon = Icon()
    burst = set()
    for angle in range(0, 360, 45):
        burst |= line(9, 9, round(9 + 8.4 * math.cos(math.radians(angle + 22))), round(9 + 8.4 * math.sin(math.radians(angle + 22))))
    icon.fill(burst | disc(9, 9, 6.6), EMBER, EMBER_LIGHT)
    icon.fill(poly([(5, 4), (13, 4), (13, 10), (9, 14), (5, 10)]), IRON, IRON_LIGHT, IRON_DARK)
    icon.fill(rect(9, 6, 9, 11), CRIMSON)
    return icon


def cryoprotection():
    """A drop of blood kept liquid inside a frost crystal."""
    icon = Icon()
    flake = set()
    for angle in range(0, 180, 60):
        dx, dy = math.cos(math.radians(angle + 90)), math.sin(math.radians(angle + 90))
        flake |= line(round(9 - 8 * dx), round(9 - 8 * dy), round(9 + 8 * dx), round(9 + 8 * dy))
    icon.fill(flake, FROST, None, FROST_DARK)
    icon.fill(drop(9, 10.5, 3.2, 6.5), CRIMSON, BLOOD_SHINE, BLOOD_DARK)
    return icon


def ender_resistance():
    """An arrow snapped against an iron plate."""
    icon = Icon()
    icon.fill(rect(12, 1, 15, 16), IRON, IRON_LIGHT, IRON_DARK)
    icon.fill(line(1, 13, 7, 9), BONE_DARK)
    icon.fill(line(1, 12, 2, 15) | line(0, 13, 3, 14), CRIMSON)
    icon.fill(line(9, 7, 11, 6), BONE_DARK)
    icon.fill(rect(10, 5, 11, 5), IRON_LIGHT)
    for x, y in ((8, 4), (9, 12), (6, 5)):
        icon.dot(x, y, SPARK)
    return icon


def stung():
    """A barbed stinger left in the skin, a bead of blood at the wound."""
    icon = Icon()
    icon.fill(rect(1, 13, 16, 16), FLESH, SCAR, FLESH_DARK)
    icon.fill(poly([(12, 1), (17, 4), (9, 14), (7, 14)]), LACQUER, LACQUER_LIGHT)
    for x, y in ((14, 6), (12, 9), (10, 12)):
        icon.dot(x, y, LACQUER_LIGHT)
    icon.fill(rect(7, 13, 9, 13), BLOOD_DARK)
    icon.fill(drop(5, 12, 1.6, 2.8), CRIMSON, BLOOD_SHINE)
    return icon


def pinned():
    """A brine-dark stake driven through a holdfast's grip."""
    icon = Icon()
    roots = line(2, 15, 6, 11) | line(16, 15, 12, 11) | line(5, 16, 7, 12) | line(13, 16, 11, 12)
    icon.fill(roots, BRINE, BRINE_LIGHT)
    icon.fill(poly([(7, 1), (11, 1), (11, 11), (9, 16), (7, 11)]), IRON, IRON_LIGHT, IRON_DARK)
    icon.fill(rect(5, 1, 13, 3), IRON, IRON_LIGHT, IRON_DARK)
    icon.fill(rect(7, 8, 11, 9), BRINE, BRINE_LIGHT)
    return icon


def morphling_sealed():
    """A vivianite jar shut under a blood-wax seal."""
    icon = Icon()
    icon.fill(rect(4, 6, 13, 16), VIVIANITE, VIVIANITE_LIGHT)
    icon.fill(rect(3, 3, 14, 5), IRON, IRON_LIGHT, IRON_DARK)
    icon.fill(disc(9, 5, 3.6), WAX, WAX_LIGHT)
    icon.dot(8, 4, OUTLINE).dot(10, 4, OUTLINE).dot(9, 6, OUTLINE)
    icon.fill(rect(6, 10, 11, 14), LACQUER, LACQUER_LIGHT)
    return icon


ICONS = dict(grave_debt=grave_debt, insatiable_hunger=insatiable_hunger, blood_drunkenness=blood_drunkenness,
             conductive_mark=conductive_mark, monolithic_dislocation=monolithic_dislocation,
             counterfeit_scar=counterfeit_scar, hemolysis=hemolysis, web_mobility=web_mobility,
             wither_resistance=wither_resistance, explosion_resistance=explosion_resistance,
             cryoprotection=cryoprotection, ender_resistance=ender_resistance, stung=stung, pinned=pinned,
             morphling_sealed=morphling_sealed)


def preview(images):
    scale, label = 8, 14
    sheet = Image.new('RGBA', (5 * SIZE * scale, 3 * (SIZE * scale + label)), (52, 52, 58, 255))
    draw = ImageDraw.Draw(sheet)
    for i, (name, img) in enumerate(images.items()):
        x, y = (i % 5) * SIZE * scale, (i // 5) * (SIZE * scale + label)
        sheet.alpha_composite(img.resize((SIZE * scale, SIZE * scale), Image.NEAREST), (x, y))
        draw.text((x + 4, y + SIZE * scale), name, fill=(255, 255, 255, 255))
    out = uw.ROOT / 'build/effect_icons_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    images = {name: draw().image() for name, draw in ICONS.items()}
    for name, img in images.items():
        img.save(OUT / f'{name}.png')
    print(f'effect icons: {len(images)} written')
    if args.preview:
        preview(images)


if __name__ == '__main__':
    main()

"""Author the Well Rested mob effect icon (core-loop Track 10): a pale crescent moon cradling a drop of blood.

A full night's sleep leaves a Harbinger Well Rested, and their blood comes back faster for five minutes. Writes
textures/mob_effect/well_rested.png (16 x 16, the size of the mod's other effect icons). Python 3 + Pillow.
"""
import math
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'src/main/resources/assets/hemomancy/textures/mob_effect/well_rested.png'

OUTLINE = (44, 12, 20, 255)
MOON = (226, 214, 188, 255)
MOON_SHADE = (176, 160, 136, 255)
BLOOD = (168, 20, 32, 255)
BLOOD_DARK = (110, 10, 22, 255)
SHINE = (236, 120, 128, 255)


def inside_moon(x, y):
    cx, cy = 7.0, 7.5
    outer = math.hypot(x - cx, y - cy) <= 6.2
    bite = math.hypot(x - (cx + 3.4), y - (cy - 2.2)) <= 5.0
    return outer and not bite


def inside_drop(x, y):
    # A drop hanging in the crescent's hollow: a circle with a point above it.
    cx, cy, r = 10.5, 10.0, 2.6
    if math.hypot(x - cx, y - cy) <= r:
        return True
    return cy - 4.8 <= y < cy and abs(x - cx) <= (y - (cy - 4.8)) / 4.8 * r


def main():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            sx, sy = x + .5, y + .5
            if inside_drop(sx, sy):
                px[x, y] = BLOOD_DARK if sx > 11.4 and sy > 10.2 else BLOOD
            elif inside_moon(sx, sy):
                px[x, y] = MOON_SHADE if sx < 4.0 or sy > 12.0 else MOON
    px[9, 9] = SHINE
    # Outline every transparent pixel that touches the art.
    filled = {(x, y) for y in range(16) for x in range(16) if px[x, y][3]}
    for y in range(16):
        for x in range(16):
            if (x, y) in filled:
                continue
            if any((x + dx, y + dy) in filled for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE
    img.save(OUT)
    print(f'Exported {OUT.name}')


if __name__ == '__main__':
    main()

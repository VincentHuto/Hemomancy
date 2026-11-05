"""Draws the Lidless Door item icon: a door silhouette of dark iron jambs around a black void with one open eye.

Run from the repository root: python tools/model_export/lidless_door_icon.py
Writes src/main/resources/assets/hemomancy/textures/item/lidless_door.png (16x16).
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/hemomancy/textures/item/lidless_door.png"

JAMB = (58, 52, 54, 255)
JAMB_LIGHT = (84, 76, 78, 255)
JAMB_DARK = (34, 30, 32, 255)
VOID = (6, 2, 3, 255)
VOID_DEEP = (2, 0, 1, 255)
SPECK = (90, 6, 6, 255)
SCLERA = (92, 8, 8, 255)
IRIS = (168, 20, 20, 255)
PUPIL = (0, 0, 0, 255)
CLEAR = (0, 0, 0, 0)


def main() -> None:
    image = Image.new("RGBA", (16, 16), CLEAR)
    pixels = image.load()
    # Frame: columns 3..12 across, rows 0..15 down; jambs two wide, lintel two tall.
    for y in range(16):
        for x in range(3, 13):
            pixels[x, y] = VOID if (x + y) % 3 else VOID_DEEP
    for y in range(16):
        for x in (3, 4):
            pixels[x, y] = JAMB_LIGHT if x == 3 else JAMB
        for x in (11, 12):
            pixels[x, y] = JAMB if x == 11 else JAMB_DARK
    for x in range(3, 13):
        pixels[x, 0] = JAMB_LIGHT
        pixels[x, 1] = JAMB
    for x in range(5, 11):
        pixels[x, 15] = JAMB_DARK
    for x, y in ((6, 11), (9, 13), (5, 4)):
        pixels[x, y] = SPECK
    # One open eye, lidless, looking out: an almond three wide with a slit pupil.
    for x in (6, 7, 8, 9):
        pixels[x, 7] = SCLERA
    pixels[7, 7] = IRIS
    pixels[8, 7] = IRIS
    pixels[7, 6] = SCLERA
    pixels[8, 6] = SCLERA
    pixels[7, 8] = SCLERA
    pixels[8, 8] = SCLERA
    pixels[8, 7] = PUPIL
    OUT.parent.mkdir(parents=True, exist_ok=True)
    image.save(OUT)
    print("wrote", OUT)


if __name__ == "__main__":
    main()

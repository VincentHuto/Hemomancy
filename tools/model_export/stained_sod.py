"""Author Stained Sod: grass that remembers the blood spilled on it (core-loop Track 10).

Grass under old stains in a region where much blood has been spilled turns to this, and turns back as the region
quiets. It is still grass: the vanilla grass and dirt pixels are kept, the grass is dulled to a sick olive, and dried
blood soaks through it in clotted patches, darkest at their hearts. Not a fungal ground cover: nothing grows from it.

Reads vanilla's grass_block_top, grass_block_side_overlay and dirt from the NeoForge client-extra jar and writes
textures/block/stained_sod_top.png and stained_sod_side.png. The block model and blockstate are authored by hand.
Python 3 + Pillow.
"""
import io
import math
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
VANILLA_JAR = ROOT / 'build/moddev/artifacts/neoforge-21.1.251-client-extra-aka-minecraft-resources.jar'
OUT = ROOT / 'src/main/resources/assets/hemomancy/textures/block'

OLIVE = (104, 112, 58)          # the plains grass tint, dulled toward brown
CLOT = (92, 18, 22)             # dried blood
CLOT_DARK = (58, 10, 14)


def vanilla(name):
    with zipfile.ZipFile(VANILLA_JAR) as jar:
        return Image.open(io.BytesIO(jar.read(f'assets/minecraft/textures/block/{name}.png'))).convert('RGBA')


def hash01(x, y, salt):
    h = (x * 374761393 + y * 668265263 + salt * 2246822519) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0xFFFF) / 0xFFFF


def soak(x, y):
    """How soaked a pixel is, 0 to 1: three clotted patches that tile across the block's edges."""
    best = 0.0
    for cx, cy, r in ((4.5, 5.0, 5.6), (11.5, 11.0, 4.8), (12.5, 3.0, 3.4)):
        for ox in (-16, 0, 16):
            for oy in (-16, 0, 16):
                d = math.hypot(x - cx - ox, y - cy - oy) / r
                best = max(best, 1.0 - d)
    return max(0.0, min(1.0, best + (hash01(x, y, 7) - 0.5) * 0.45))


def stain(grey, x, y, depth=1.0):
    """A grass pixel (grey luminance) dulled to olive and soaked by the patch it lies in."""
    lum = grey / 255.0
    base = tuple(int(c * lum * 1.15) for c in OLIVE)
    s = soak(x, y) * depth
    if s > 0.5:
        clot = CLOT_DARK if s > 0.78 else CLOT
        return tuple(int(c * (0.75 + 0.35 * lum)) for c in clot)
    if s > 0.22:
        mix = (s - 0.22) / 0.28
        return tuple(int(b + (c * (0.8 + 0.3 * lum) - b) * mix * 0.8) for b, c in zip(base, CLOT))
    return base


def top():
    grass = vanilla('grass_block_top')
    out = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            r, g, b, _ = grass.getpixel((x, y))
            out.putpixel((x, y), stain((r + g + b) // 3, x, y) + (255,))
    return out


def side():
    dirt = vanilla('dirt')
    overlay = vanilla('grass_block_side_overlay')
    out = dirt.copy()
    for y in range(16):
        for x in range(16):
            r, g, b, a = overlay.getpixel((x, y))
            if a > 0:
                out.putpixel((x, y), stain((r + g + b) // 3, x, y, depth=0.8) + (255,))
            elif y < 9 and soak(x, 1) > 0.35 and hash01(x // 2, y, 3) < 0.85 - y * 0.09:
                # Blood run down into the dirt under the soaked patches.
                dr, dg, db, _ = out.getpixel((x, y))
                out.putpixel((x, y), (int(dr * 0.55 + CLOT[0] * 0.45), int(dg * 0.55 + CLOT[1] * 0.45),
                                      int(db * 0.55 + CLOT[2] * 0.45), 255))
    return out


def main():
    top().save(OUT / 'stained_sod_top.png')
    side().save(OUT / 'stained_sod_side.png')
    print('Exported Stained Sod: stained_sod_top.png, stained_sod_side.png')


if __name__ == '__main__':
    main()

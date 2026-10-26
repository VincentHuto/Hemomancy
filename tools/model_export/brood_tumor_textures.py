"""Textures for the Spawn Mother's brood tumor block and the tissue her hazards are drawn with, in her palette.

    textures/block/brood_tumor_side.png            lower half: a taut amber sac over a clutch of yolk eggs, crimson veins
    textures/block/brood_tumor_upper.png           upper half: thinner, brighter membrane narrowing to the crown
    textures/block/brood_tumor_top.png             the crown's puckered vent, yolk showing through
    textures/block/brood_tumor_bottom.png          the root mat where it grips the floor
    textures/entity/boss/endgame/mycophant_hyphae.png  8x32 tendril skin (u round the tube, v along it): bone fibre over amber
    textures/entity/boss/endgame/mycophant_nectar.png  16x16 glossy nectar for her thrown globs

Blocks keep 16 texels a block like every other block; the tendril skin matches the Priestess tendril's 8x32 layout.

    python tools/model_export/brood_tumor_textures.py
"""
import math
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy/textures'

AMBER = (255, 168, 40)
YOLK = (255, 206, 80)
SPORE = (200, 176, 64)
CRIMSON = (168, 22, 30)
BONE = (232, 216, 176)
DEEP = (122, 54, 18)      # the sac's shadowed flesh
CLOT = (92, 14, 22)


def h(x, y, seed):
    """Stable per-texel noise in [0, 1)."""
    n = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65536.0


def smooth(x, y, seed, scale):
    """Value noise, tiling every 16 texels so faces meet without seams."""
    gx, gy = x / scale, y / scale
    x0, y0 = math.floor(gx), math.floor(gy)
    fx, fy = gx - x0, gy - y0
    period = max(1, round(16 / scale))

    def corner(cx, cy):
        return h(cx % period, cy % period, seed)

    sx, sy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
    top = corner(x0, y0) + (corner(x0 + 1, y0) - corner(x0, y0)) * sx
    bottom = corner(x0, y0 + 1) + (corner(x0 + 1, y0 + 1) - corner(x0, y0 + 1)) * sx
    return top + (bottom - top) * sy


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def vein(x, y, seed, scale=5.0, width=0.03):
    """A thin branching line where the noise crosses its middle."""
    return abs(smooth(x, y, seed, scale) - 0.5) < width


def eggs(x, y, centres):
    """Strength of the yolk eggs pressing through the membrane at this texel."""
    best = 0.0
    for cx, cy, r in centres:
        d = math.hypot(x + 0.5 - cx, y + 0.5 - cy) / r
        best = max(best, 1.0 - d)
    return best


def sac(size, seed, bright, egg_centres):
    image = Image.new('RGBA', (size, size))
    for y in range(size):
        for x in range(size):
            n = smooth(x, y, seed, 4.0)
            rgb = mix(DEEP, AMBER, 0.35 + 0.55 * n + bright)
            e = eggs(x, y, egg_centres)
            if 0 < e < 0.18:
                rgb = mix(rgb, DEEP, 0.55)              # the membrane pulled taut round each egg
            elif e > 0:
                rgb = mix(rgb, YOLK, 0.25 + 0.6 * e)
                if e > 0.62:
                    rgb = mix(rgb, BONE, 0.35)          # the egg's pale crown catching the light
            if vein(x, y, seed + 7):
                rgb = mix(rgb, CRIMSON, 0.75)
            elif vein(x, y, seed + 19, 3.0, 0.02):
                rgb = mix(rgb, CLOT, 0.45)
            if h(x, y, seed + 3) > 0.94:
                rgb = mix(rgb, SPORE, 0.7)              # spore dust stuck to the wet skin
            image.putpixel((x, y), rgb + (255,))
    return image


def top(seed):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            r = math.hypot(dx, dy)
            angle = math.atan2(dy, dx)
            pucker = 0.5 + 0.5 * math.cos(angle * 7 + r * 0.6)
            rgb = mix(DEEP, AMBER, 0.3 + 0.5 * smooth(x, y, seed, 4.0) + 0.2 * pucker)
            if r < 2.4:
                rgb = mix(YOLK, BONE, 0.4 - r * 0.12)  # the vent's yolk mouth
            elif r < 3.4:
                rgb = mix(rgb, CRIMSON, 0.7)
            elif vein(x, y, seed + 5):
                rgb = mix(rgb, CRIMSON, 0.6)
            image.putpixel((x, y), rgb + (255,))
    return image


def bottom(seed):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            rgb = mix(CLOT, DEEP, 0.4 + 0.5 * smooth(x, y, seed, 3.0))
            if vein(x, y, seed + 2, 2.5, 0.06):
                rgb = mix(rgb, BONE, 0.55)              # root fibres
            image.putpixel((x, y), rgb + (255,))
    return image


def hyphae(seed):
    """Tendril skin: pale fibre strands running the length over amber flesh, a crimson vessel down one side."""
    image = Image.new('RGBA', (8, 32))
    for y in range(32):
        for x in range(8):
            strand = 0.5 + 0.5 * math.sin(x * math.tau / 8 * 2 + math.sin(y * 0.4 + seed) * 0.8)
            rgb = mix(DEEP, AMBER, 0.3 + 0.5 * h(x, y // 2, seed))
            rgb = mix(rgb, BONE, strand * 0.65)
            if x == 5 and h(0, y, seed + 9) > 0.15:
                rgb = mix(rgb, CRIMSON, 0.7)
            if (y + x * 3) % 11 == 0:
                rgb = mix(rgb, YOLK, 0.6)               # swollen knots
            image.putpixel((x, y), rgb + (255,))
    return image


def nectar(seed):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            n = smooth(x, y, seed, 4.0)
            rgb = mix(AMBER, YOLK, n)
            if abs(smooth(x, y, seed + 4, 5.0) - 0.5) < 0.05:
                rgb = mix(rgb, BONE, 0.6)               # bright streaks running through it
            if h(x, y, seed + 1) > 0.95:
                rgb = mix(rgb, CRIMSON, 0.5)
            image.putpixel((x, y), rgb + (215,))
    return image


def main():
    block = ASSETS / 'block'
    entity = ASSETS / 'entity/boss/endgame'
    sac(16, 11, 0.0, [(4.5, 11.0, 3.6), (11.5, 9.5, 4.0), (8.0, 4.5, 2.8)]).save(block / 'brood_tumor_side.png')
    sac(16, 23, 0.15, [(6.0, 12.0, 3.0), (11.0, 6.0, 2.4)]).save(block / 'brood_tumor_upper.png')
    top(31).save(block / 'brood_tumor_top.png')
    bottom(37).save(block / 'brood_tumor_bottom.png')
    hyphae(41).save(entity / 'mycophant_hyphae.png')
    nectar(43).save(entity / 'mycophant_nectar.png')


if __name__ == '__main__':
    main()

"""Painted ground pools and the blood-creep overlay for the Annetta encounter.

    pool_humor.png         milky white-blue Unstained humor
    pool_reanimation.png   dark arterial pool with vessels reaching in from the rim
    annetta_blood_creep.png  whole-atlas wet blood laid over Annetta as the pool climbs her body

    python tools/model_export/annetta_pools.py
"""
import math

from PIL import Image

from annetta_kit import TEXTURE_DIR, h, mix, smooth

SIZE = 64
CREEP_SIZE = 128


def ragged(px, py, seed):
    """Distance from the pool's centre, pushed in and out by noise so the rim is a splash, not a circle."""
    x, y = (px + 0.5) / SIZE * 2 - 1, (py + 0.5) / SIZE * 2 - 1
    d = math.hypot(x, y)
    angle = math.atan2(y, x)
    rim = smooth(math.cos(angle) * 9 + 20, math.sin(angle) * 9 + 20, seed, 2.2, seed)
    wobble = 0.22 * (rim - 0.5) + 0.04 * math.sin(angle * 3 + seed) + 0.06 * (smooth(px, py, 0, 3.0, seed) - 0.5)
    return d / (0.82 + wobble), x, y


def pool(base, deep, accent, seed, alpha=225, accent_rule=None):
    image = Image.new('RGBA', (SIZE, SIZE))
    for py in range(SIZE):
        for px in range(SIZE):
            d, x, y = ragged(px, py, seed)
            if d > 1.0:
                # Droplets flung past the rim.
                if d < 1.25 and h(px, py, seed) > 0.93:
                    image.putpixel((px, py), deep + (alpha,))
                continue
            rgb = mix(base, deep, min(1.0, d * 0.7 + smooth(px, py, 0, 4.0, seed + 3) * 0.4))
            if accent_rule and accent_rule(px, py, d):
                rgb = accent
            sheen = math.hypot(x + 0.28, y + 0.3)
            if sheen < 0.16:
                rgb = mix(rgb, (255, 255, 255), 0.3 * (1 - sheen / 0.16))   # soft wet highlight
            edge = 1.0 - max(0.0, d - 0.85) / 0.15
            image.putpixel((px, py), rgb + (int(alpha * edge),))
    return image


def main():
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    pool((226, 240, 250), (150, 196, 230), (255, 255, 255), 7, alpha=200,
         accent_rule=lambda px, py, d: abs(math.sin(d * 18)) < 0.12).save(TEXTURE_DIR / 'pool_humor.png')
    pool((110, 8, 22), (46, 2, 10), (150, 20, 34), 13, alpha=240,
         accent_rule=lambda px, py, d: d > 0.3 and abs(math.sin(math.atan2(py - 32, px - 32) * 7 + d * 3)) < 0.1 * d
         ).save(TEXTURE_DIR / 'pool_reanimation.png')

    # Laid over Annetta's atlas, so it matches its size (128, at block density); patterns keep their old UV scale.
    creep = Image.new('RGBA', (CREEP_SIZE, CREEP_SIZE))
    k = 256 / CREEP_SIZE
    for py in range(CREEP_SIZE):
        for px in range(CREEP_SIZE):
            x, y = (px + 0.5) * k, (py + 0.5) * k
            rgb = mix((120, 10, 24), (52, 4, 12), smooth(x, y, 0, 6.0, 5))
            if abs(smooth(x, y, 0, 4.0, 9) - 0.5) < 0.05:
                rgb = (34, 2, 8)                            # vessels knitting through her
            elif h(px, py, 1) > 0.97:
                rgb = (190, 40, 50)                         # wet glint
            creep.putpixel((px, py), rgb + (255,))
    creep.save(TEXTURE_DIR / 'annetta_blood_creep.png')
    print('pools and blood creep written')


if __name__ == '__main__':
    main()

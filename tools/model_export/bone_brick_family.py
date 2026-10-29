"""Bone-brick masonry for the mausoleum rework (Track 7): ossuary courses cut from old bone.

    textures/block/bone_bricks.png           four 4 px courses in a running bond, ivory faces, warm grey mortar
    textures/block/cracked_bone_bricks.png   the same wall with three cracks and a chipped corner
    textures/block/chiseled_bone_bricks.png  a framed panel with a carved ossuary rosette (capitals, keystones)

Stairs, slab and wall reuse bone_bricks.png. The tones sit beside vanilla bone_block, a shade darker so the
mortar reads. Every course tiles across and down the 16 px face, so a wall of them shows no seam. All noise is
hashed from the texel, so a rerun writes the same pixels.

    python tools/model_export/bone_brick_family.py      also writes build/model-previews/bone_brick_family.png
"""
import math
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
TEXTURES = ROOT / 'src/main/resources/assets/hemomancy/textures/block'
PREVIEW = ROOT / 'build/model-previews/bone_brick_family.png'

FACE = (224, 218, 194)       # a brick's open face, a little below bone_block's 233/230/212
LIGHT = (237, 233, 215)      # the top arris catching the light
SHADE = (194, 186, 157)      # the brick's foot and right end, turned away from the light
GRAIN = (200, 192, 164)      # short grain lines running along the course
PIT = (176, 167, 138)        # pores in the cut bone
MORTAR = (150, 141, 113)     # the joints, a warm grey
CRACK = (104, 95, 74)

# Head joints of each 4 px course (x of the joint column); a course's bricks run between them. Lengths vary
# 6 to 8 so the bond does not read as a grid, and each course wraps at 16 so the face tiles across.
COURSES = [(0, 8), (4, 11), (1, 9), (5, 13)]


def h(x, y, seed):
    """Stable per-texel noise in [0, 1)."""
    n = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65536.0


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def shift(rgb, d):
    return tuple(max(0, min(255, round(c + d))) for c in rgb)


def brick_at(x, y):
    """(course, brick index, x within brick, brick length) or None on a mortar texel."""
    course, row = divmod(y, 4)
    if row == 3:
        return None
    joints = COURSES[course]
    if x in joints:
        return None
    for i, start in enumerate(joints):
        end = joints[(i + 1) % len(joints)]
        length = (end - start - 1) % 16
        local = (x - start - 1) % 16
        if local < length:
            return course, i, local, length, row
    return None


def bricks(seed=11):
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            here = brick_at(x, y)
            if here is None:
                rgb = shift(MORTAR, (h(x, y, seed + 1) - .5) * 14)
                image.putpixel((x, y), rgb + (255,))
                continue
            course, i, local, length, row = here
            tone = (h(course, i, seed + 2) - .5) * 12           # each brick its own cut of bone
            rgb = shift(FACE, tone + (h(x, y, seed + 3) - .5) * 8)
            if row == 0:
                rgb = mix(rgb, LIGHT, .7)
            elif row == 2:
                rgb = mix(rgb, SHADE, .55)
            if local == length - 1:
                rgb = mix(rgb, SHADE, .6)                        # the brick's right end
            elif local == 0 and row < 2:
                rgb = mix(rgb, LIGHT, .4)
            grain_row = 1 if h(course, i, seed + 4) < .7 else 2
            g0 = 1 + int(h(course, i, seed + 5) * max(1, length - 4))
            if row == grain_row and g0 <= local < g0 + 2 + int(h(course, i, seed + 6) * 2):
                rgb = mix(rgb, GRAIN, .8)
            if row > 0 and 0 < local < length - 1 and h(x, y, seed + 7) > .9:
                rgb = mix(rgb, PIT, .85)
            image.putpixel((x, y), rgb + (255,))
    return image


# Cracks as texel paths: one stepping down through two courses, one along a course, one short split.
CRACKS = [
    [(12, 0), (12, 1), (13, 2), (13, 3), (13, 4), (14, 5), (14, 6)],
    [(2, 9), (3, 9), (4, 10), (5, 10)],
    [(9, 12), (9, 13), (10, 14)],
]
CHIP = [(9, 4), (10, 4), (10, 5)]   # the top right corner of a course-1 brick knocked away
CHIP_EDGE = [(8, 4), (9, 5), (10, 6)]


def cracked(seed=11):
    image = bricks(seed)
    for path in CRACKS:
        for x, y in path:
            image.putpixel((x, y), CRACK + (255,))
            nx = min(15, x + 1)
            if brick_at(nx, y) is not None and (nx, y) not in path:
                image.putpixel((nx, y), mix(image.getpixel((nx, y))[:3], LIGHT, .35) + (255,))   # the lit lip
    for x, y in CHIP:
        image.putpixel((x, y), mix(PIT, MORTAR, .5) + (255,))
    for x, y in CHIP_EDGE:
        image.putpixel((x, y), mix(image.getpixel((x, y))[:3], PIT, .7) + (255,))
    return image


def chiseled(seed=23):
    """A one-texel raised frame round a sunk panel; in it a raised ring round a bored boss, studs in the corners."""
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            n = (h(x, y, seed) - .5) * 8
            edge = min(x, y, 15 - x, 15 - y)
            top_left = (x == edge or y == edge) and x < 15 - edge and y < 15 - edge
            if edge == 0:
                rgb = MORTAR                                                  # bedded in the wall's joints
            elif edge == 1:
                rgb = LIGHT if top_left else SHADE                            # the raised frame
            elif edge == 2:
                rgb = mix(PIT, MORTAR, .3) if top_left else mix(FACE, LIGHT, .3)   # the panel's sunk walls
            else:
                rgb = mix(SHADE, PIT, .3)                                     # the sunk panel
                cx, cy = x + .5 - 8, y + .5 - 8
                r = math.hypot(cx, cy)
                lit = (-cx - cy) / max(r, .01)                                 # light from the top left
                if r < 1.0:
                    rgb = LIGHT                                                # the central boss
                elif r < 2.0:
                    rgb = MORTAR                                               # the bore round the boss
                elif r < 3.6:
                    rgb = mix(FACE, LIGHT if lit > 0 else SHADE, abs(lit) * .7)   # the raised ring
                elif r < 4.4 and lit < -.3:
                    rgb = mix(PIT, MORTAR, .45)                                # the ring's shadow on the panel
                if edge == 3 and abs(abs(cx) - abs(cy)) < .1:
                    rgb = LIGHT                                                # studs in the panel's corners
                elif edge == 4 and abs(abs(cx) - abs(cy)) < .1 and cx > 0 and cy > 0:
                    rgb = mix(PIT, MORTAR, .45)                                # the bottom-right stud's shadow
            image.putpixel((x, y), shift(rgb, n) + (255,))
    return image


def preview(images):
    scale = 8
    gap = 8
    tile = 16 * scale
    width = max(len(images) * (tile + gap) - gap, 3 * tile)
    sheet = Image.new('RGBA', (width + 2 * gap, tile + gap + 3 * tile + 2 * gap), (36, 32, 30, 255))
    for i, image in enumerate(images):
        sheet.paste(image.resize((tile, tile), Image.NEAREST), (gap + i * (tile + gap), gap))
    big = images[0].resize((tile, tile), Image.NEAREST)
    for ty in range(3):
        for tx in range(3):
            sheet.paste(big, (gap + tx * tile, 2 * gap + tile + ty * tile))
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW)


def main():
    images = {'bone_bricks': bricks(), 'cracked_bone_bricks': cracked(), 'chiseled_bone_bricks': chiseled()}
    for name, image in images.items():
        image.save(TEXTURES / f'{name}.png')
        print('wrote', TEXTURES / f'{name}.png')
    preview(list(images.values()))
    print('wrote', PREVIEW)


if __name__ == '__main__':
    main()

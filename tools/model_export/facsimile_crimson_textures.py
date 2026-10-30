"""Facsimile skins the hive wears in its own colours (possession Track 4).

    textures/entity/facsimile/facsimile_golem.png                    128 x 128, IronGolemModel's UV
    textures/entity/facsimile/facsimile_golem_crackiness_{low,medium,high}.png
    textures/entity/facsimile/facsimile_blaze.png                    64 x 32, BlazeModel's UV

The golem is hematic iron, not vanilla's grey: red-black plates in courses, each with a lit top bevel and a turned
foot, oxblood lacquer in the seams and bone rivets at the plate corners (no copper, brass or silver anywhere). Its
eyes are sockets with a crimson ember. The three crack layers are cumulative like vanilla's and split the plates
along crimson fissures with a hot core, so a hurt facsimile bleeds light. The blaze is charred crimson: a dark head
crazed with glowing seams, black eye slits with a red glint, and rods banded ember and soot.

Every texel is painted on a box face by its face-local coordinates, so the art lands exactly on the model's 1/16
pixels (16 px per block). All noise is hashed from the texel, so a rerun writes the same pixels.

    python tools/model_export/facsimile_crimson_textures.py   also writes build/model-previews/facsimile_crimson.png
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'src/main/resources/assets/hemomancy/textures/entity/facsimile'
PREVIEW = ROOT / 'build/model-previews/facsimile_crimson.png'

# Hematic iron, pulled toward red-black from hematic_iron_plating's violet-grey.
FACE = (52, 36, 42)
LIGHT = (78, 60, 68)
SHEEN = (70, 62, 78)
SHADE = (36, 24, 29)
SEAM = (24, 14, 17)
LACQUER = (92, 16, 26)
LACQUER_GLOSS = (138, 34, 44)
BONE = (214, 200, 174)
BONE_SHADOW = (150, 132, 108)
SOCKET = (14, 8, 10)
EMBER = (226, 52, 44)

CRACK_RIM = (40, 8, 12)
CRACK = (168, 18, 30)
CRACK_HOT = (246, 88, 66)

CHAR = (44, 12, 16)
CHAR_LIGHT = (72, 20, 24)
GLOW = (190, 26, 34)
GLOW_HOT = (255, 104, 74)
SLIT = (10, 4, 6)


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


def name_seed(name):
    """A stable number per face name (str hash() changes per process)."""
    return sum(ord(c) * (i + 1) for i, c in enumerate(name))


def box_faces(u, v, w, hgt, d):
    """The six faces of a box at texOffs(u, v): name -> (x0, y0, width, height), Minecraft's cube unwrap."""
    return {
        'top': (u + d, v, w, d),
        'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, hgt),
        'front': (u + d, v + d, w, hgt),
        'left': (u + d + w, v + d, d, hgt),
        'back': (u + d + w + d, v + d, w, hgt),
    }


def paint_box(image, u, v, w, hgt, d, painter, seed):
    for name, (x0, y0, fw, fh) in box_faces(u, v, w, hgt, d).items():
        for fy in range(fh):
            for fx in range(fw):
                rgb = painter(name, fx, fy, fw, fh, seed + name_seed(name) % 97)
                if rgb is not None:
                    image.putpixel((x0 + fx, y0 + fy), rgb + (255,))


# ---------------------------------------------------------------- golem

def plate(name, fx, fy, fw, fh, seed, course=5, joint=6):
    """Riveted hematic plates in courses: a lacquered bed seam every `course` rows, staggered head joints."""
    if name in ('top', 'bottom'):
        course = max(course, fh)
    row, local_y = divmod(fy, course)
    if local_y == course - 1 and fy != fh - 1:
        return mix(LACQUER, LACQUER_GLOSS, 0.6) if h(fx, fy, seed + 3) < 0.18 else LACQUER
    offset = (row * joint // 2) % joint
    local_x = (fx + offset) % joint
    if local_x == joint - 1 and fw > joint:
        return SEAM
    # Bone rivets strap only some plates, at their two upper corners, so the iron reads before the bone does.
    riveted = h(row, (fx + offset) // joint, seed + 9) < 0.3 and fw > 3 and course > 3
    if riveted and local_y == 1 and local_x in (1, joint - 3):
        return BONE if h(fx, fy, seed + 5) < 0.85 else BONE_SHADOW
    if riveted and local_y == 2 and local_x in (1, joint - 3):
        return SHADE
    if local_y == 0:
        base = LIGHT
    elif local_y == course - 2:
        base = SHADE
    else:
        base = FACE
    # A slanting sheen across each plate, and a little rust-speckle.
    if (local_x + local_y) % 7 == 3 and local_y not in (0, course - 2):
        base = mix(base, SHEEN, 0.55)
    return shift(base, (h(fx, fy, seed) - 0.5) * 8)


def golem_head(name, fx, fy, fw, fh, seed):
    if name == 'front':
        # Brow band, then two eye sockets with an ember at their core.
        if fy == 2:
            return LACQUER
        if fy in (3, 4) and fx in (1, 2, 5, 6):
            return EMBER if fy == 4 and fx in (2, 5) else SOCKET
        if fy == fh - 1:
            return SEAM
    return plate(name, fx, fy, fw, fh, seed, course=5, joint=8)


def golem_nose(name, fx, fy, fw, fh, seed):
    return plate(name, fx, fy, fw, fh, seed, course=4, joint=9)


def golem_body(name, fx, fy, fw, fh, seed):
    if name in ('front', 'back') and fx == fw // 2:
        return SEAM
    if name == 'front' and fy == 1 and fx % 3 == 1:
        return BONE
    return plate(name, fx, fy, fw, fh, seed, course=6, joint=6)


def golem_waist(name, fx, fy, fw, fh, seed):
    if fy % 2 == 0:
        return mix(SHADE, LACQUER, 0.35)
    return shift(SEAM, h(fx, fy, seed) * 8)


def golem_arm(name, fx, fy, fw, fh, seed):
    if name not in ('top', 'bottom'):
        # A lacquered strap below the shoulder and above the fist; the fist plates are darker.
        if fy in (7, fh - 9):
            return LACQUER_GLOSS if fx % 2 == 0 else LACQUER
        if fy >= fh - 8:
            rgb = plate(name, fx, fy - (fh - 8), fw, 8, seed + 11, course=4, joint=fw + 1)
            return shift(rgb, -10)
    return plate(name, fx, fy, fw, fh, seed, course=6, joint=fw + 1)


def golem_leg(name, fx, fy, fw, fh, seed):
    if name not in ('top', 'bottom') and fy == 6:
        return LACQUER
    return plate(name, fx, fy, fw, fh, seed, course=5, joint=fw + 1)


GOLEM_BOXES = [
    ((0, 0, 8, 10, 8), golem_head),
    ((24, 0, 2, 4, 2), golem_nose),
    ((0, 40, 18, 12, 11), golem_body),
    ((0, 70, 9, 5, 6), golem_waist),
    ((60, 21, 4, 30, 6), golem_arm),
    ((60, 58, 4, 30, 6), golem_arm),
    ((37, 0, 6, 16, 5), golem_leg),
    ((60, 0, 6, 16, 5), golem_leg),
]


def golem():
    image = Image.new('RGBA', (128, 128))
    for i, (box, painter) in enumerate(GOLEM_BOXES):
        paint_box(image, *box, painter, seed=101 + i * 17)
    return image


def cracks(level):
    """Crimson fissures on every face; each level keeps the cracks of the levels below it."""
    image = Image.new('RGBA', (128, 128))
    walks_per_face = {1: 1, 2: 2, 3: 4}[level]
    for i, (box, _) in enumerate(GOLEM_BOXES):
        for name, (x0, y0, fw, fh) in box_faces(*box).items():
            area = fw * fh
            if area < 12:
                continue
            walks = max(1, round(walks_per_face * area / 120))
            for n in range(walks):
                seed = 900 + i * 131 + name_seed(name) % 53 + n * 7
                fx = int(h(n, i, seed) * fw)
                fy = int(h(i, n, seed + 1) * fh)
                length = 4 + int(h(n, n, seed + 2) * (3 + level * 3))
                for step in range(length):
                    if 0 <= fx < fw and 0 <= fy < fh:
                        hot = step > 0 and step < length - 1 and h(fx, fy, seed + 4) < 0.45
                        image.putpixel((x0 + fx, y0 + fy), (CRACK_HOT if hot else CRACK) + (255,))
                        # A dark rim on one side gives the fissure depth.
                        rx = fx + 1
                        if 0 <= rx < fw and image.getpixel((x0 + rx, y0 + fy))[3] == 0:
                            image.putpixel((x0 + rx, y0 + fy), CRACK_RIM + (255,))
                    r = h(fx, fy, seed + step)
                    fy += 1 if r < 0.55 else 0
                    fx += -1 if r < 0.3 else (1 if r > 0.7 else 0)
    return image


def layered_cracks():
    out = {}
    acc = Image.new('RGBA', (128, 128))
    for level, label in ((1, 'low'), (2, 'medium'), (3, 'high')):
        acc = Image.alpha_composite(acc, cracks(level))
        out[label] = acc.copy()
    return out


# ---------------------------------------------------------------- blaze

def blaze_head(name, fx, fy, fw, fh, seed):
    if name == 'front':
        if fy == 3 and fx in (1, 2, 5, 6):
            return SLIT
        if fy == 4 and fx in (2, 5):
            return GLOW_HOT
        if fy == 4 and fx in (1, 6):
            return SLIT
    # Glowing seams craze the char: a lattice that wanders with the hash.
    seam = ((fx + int(h(fx // 3, fy, seed) * 2)) % 6 == 0 and h(fx, fy // 2, seed + 4) < 0.6
            or (fy + int(h(fx // 2, fy, seed + 1) * 2)) % 7 == 0 and h(fx // 2, fy, seed + 5) < 0.5)
    if seam:
        return GLOW_HOT if h(fx, fy, seed + 2) < 0.2 else GLOW
    base = CHAR_LIGHT if (fx + fy) % 3 == 0 else CHAR
    return shift(base, (h(fx, fy, seed + 3) - 0.5) * 10)


def blaze_rod(name, fx, fy, fw, fh, seed):
    if name in ('top', 'bottom'):
        return GLOW_HOT
    # Bands of ember and soot, hottest at the rod's two ends.
    if fy in (0, fh - 1):
        return GLOW_HOT
    if fy % 3 == 1:
        return CHAR
    return mix(GLOW, GLOW_HOT, h(fx, fy, seed) * 0.4)


def blaze():
    image = Image.new('RGBA', (64, 32))
    paint_box(image, 0, 0, 8, 8, 8, blaze_head, seed=7)
    paint_box(image, 0, 16, 2, 8, 2, blaze_rod, seed=29)
    return image


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    body = golem()
    body.save(OUT / 'facsimile_golem.png')
    crack_levels = layered_cracks()
    for label, image in crack_levels.items():
        image.save(OUT / f'facsimile_golem_crackiness_{label}.png')
    fire = blaze()
    fire.save(OUT / 'facsimile_blaze.png')

    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    scale = 4
    sheet = Image.new('RGBA', (128 * 4 * scale + 64 * scale, 128 * scale), (30, 30, 34, 255))
    tiles = [body] + [Image.alpha_composite(body, crack_levels[k]) for k in ('low', 'medium', 'high')]
    for i, tile in enumerate(tiles):
        sheet.alpha_composite(tile.resize((128 * scale, 128 * scale), Image.NEAREST), (i * 128 * scale, 0))
    sheet.alpha_composite(fire.resize((64 * scale, 32 * scale), Image.NEAREST), (4 * 128 * scale, 0))
    sheet.save(PREVIEW)


if __name__ == '__main__':
    main()

"""Hematic-iron plating for the mausoleum rework (Track 8): the trim stone of Hemorath's complex.

    textures/block/hematic_iron_plating.png   two 8 px courses of long riveted plates in a half bond

Stairs, slab and wall reuse the same texture. The tones are lifted from hematic_iron_block.png (violet-grey iron,
a slanting blue-grey glint, crimson in the seams) but the face is calm: one bevel per plate, one glint, four rivets
strapping each head joint and a faint red bed joint, so the block tiles along a cornice or base course without a
pattern jumping out. Every course wraps at 16, so a wall of it shows no seam. All noise is hashed from the texel, so
a rerun writes the same pixels.

    python tools/model_export/hematic_iron_plating.py   also writes build/model-previews/hematic_iron_plating.png

The preview shows the texture beside hematic_iron_block / pillar / chiseled, a 3 x 3 tiled wall, a wall elevation of
polished venous stone bricks framed in plating (pilasters, base course, a cornice of slabs, stairs and a wall), and
iso views of the cube, stairs, slab and wall next to venous bricks and the iron block.
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
TEXTURES = ROOT / 'src/main/resources/assets/hemomancy/textures/block'
PREVIEW = ROOT / 'build/model-previews/hematic_iron_plating.png'

FACE = (57, 54, 62)          # the plate's open face, hematic_iron_block's mid tone (3a373d)
LIGHT = (80, 84, 95)         # the top and left bevel catching the light
SHEEN = (70, 84, 99)         # the blue-grey sheen the block texture carries (475767)
SHADE = (42, 37, 45)         # the foot and right end of a plate, turned away
SEAM = (34, 28, 34)          # head joints, as dark as the block's outline (241e24)
BLOOD = (58, 20, 30)         # the bed joint, dried blood in the seam (3c1520)
BEAD = (98, 22, 34)          # a few brighter beads along it (651320)
RIVET = (100, 109, 122)
RIVET_SHADOW = (33, 27, 33)

# Head joints of each 8 px course (x of the joint column). Each course is one 15 px plate; the second course is
# offset by half a block so the joints never line up, and both wrap at 16 so the face tiles across.
COURSES = [(15,), (7,)]
COURSE = 8
BEADS = {(3, 7), (12, 15)}   # (x, y) where the bed joint shows a brighter bead
GLINTS = (5, 8)              # where each course's slanting glint starts (local x + row)


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


def plate_at(x, y):
    """(course, plate index, x within plate, plate length, row within plate) or None on a joint texel."""
    course, row = divmod(y, COURSE)
    if row == COURSE - 1:
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


def plating(seed=31):
    image = Image.new('RGBA', (16, 16))
    rows = COURSE - 1
    for y in range(16):
        for x in range(16):
            here = plate_at(x, y)
            if here is None:
                if y % COURSE == COURSE - 1:
                    rgb = BEAD if (x, y) in BEADS else mix(SEAM, BLOOD, .75 + h(x, y, seed + 1) * .25)
                else:
                    rgb = shift(SEAM, (h(x, y, seed + 1) - .5) * 4)
                image.putpixel((x, y), rgb + (255,))
                continue
            course, i, local, length, row = here
            tone = (h(course, 0, seed + 2) - .5) * 6            # each course its own rolling
            rgb = shift(FACE, tone + (row - 3) * -1.5 + (h(x, y, seed + 3) - .5) * 4)   # lit from above
            # a glint slanting across the plate, as the block texture's blue streaks do
            glint = local + row - GLINTS[course]
            if 0 <= glint < 3:
                rgb = mix(rgb, SHEEN, (.38, .5, .28)[glint])
            elif glint == 3 or glint == -1:
                rgb = mix(rgb, SHEEN, .12)
            if row == 0 or local == 0:
                rgb = mix(rgb, LIGHT, .75 if row == 0 and local == 0 else .6)
            if row == rows - 1 or local == length - 1:
                rgb = mix(rgb, SHADE, .85)
            # a butt strap: four rivets round each head joint, one either side of it, top and bottom
            if row in (1, rows - 2) and local in (1, length - 2):
                rgb = RIVET
            elif row in (2, rows - 1) and local == 2:
                rgb = mix(rgb, RIVET_SHADOW, .45)                 # the left rivets' shadows (the right fall on the bevel)
            image.putpixel((x, y), rgb + (255,))
    return image


# --- preview ----------------------------------------------------------------------------------------------------

def shade(rgb, k):
    return tuple(max(0, min(255, round(c * k))) for c in rgb)


def iso(top, left, right, scale=6, shape='cube'):
    """A block drawn from the south-east, top lit, south face mid, east face dark. Shapes: cube, slab, stairs, wall.

    The stairs rise toward the north (their back away from the viewer) and the wall is a low run east-west with a
    post, as vanilla's models are; textures are uv-locked, so each face samples the texel at its world position.
    """
    boxes = {
        'cube': [(0, 0, 0, 16, 16, 16)],
        'slab': [(0, 0, 0, 16, 8, 16)],
        'stairs': [(0, 0, 0, 16, 8, 16), (0, 8, 0, 16, 16, 8)],
        'wall': [(4, 0, 4, 12, 16, 12), (0, 0, 5, 16, 14, 11)],
    }[shape]
    a = scale
    w, hgt = 32 * a + 2, 32 * a + 2
    out = Image.new('RGBA', (w, hgt), (0, 0, 0, 0))
    depth = {}
    ox, oy = 16 * a, 16 * a

    def put(px, py, d, rgb):
        if 0 <= px < w and 0 <= py < hgt and d > depth.get((px, py), -1e9):
            depth[(px, py)] = d
            out.putpixel((px, py), rgb + (255,))

    step = 1 / a
    for x0, y0, z0, x1, y1, z1 in boxes:
        n = int(16 * a)
        for i in range(n):
            for j in range(n):
                u, v = (i + .5) * step, (j + .5) * step
                # top face at y1: (x, z) = (u, v)
                if x0 <= u < x1 and z0 <= v < z1:
                    X, Y = (u - v) * a, (u + v) * a / 2 - y1 * a
                    put(int(ox + X), int(oy + Y), u + v + y1, shade(top.getpixel((int(u), int(v)))[:3], 1.0))
                # south face at z1: (x, y) = (u, v)
                if x0 <= u < x1 and y0 <= v < y1:
                    X, Y = (u - z1) * a, (u + z1) * a / 2 - v * a
                    put(int(ox + X), int(oy + Y), u + z1 + v, shade(left.getpixel((int(u), 15 - int(v)))[:3], .82))
                # east face at x1: (z, y) = (u, v)
                if z0 <= u < z1 and y0 <= v < y1:
                    X, Y = (x1 - u) * a, (x1 + u) * a / 2 - v * a
                    put(int(ox + X), int(oy + Y), x1 + u + v,
                        shade(right.getpixel((15 - int(u), 15 - int(v)))[:3], .64))
    return out


def tile(image, nx, ny, scale):
    t = 16 * scale
    big = image.resize((t, t), Image.NEAREST)
    sheet = Image.new('RGBA', (nx * t, ny * t))
    for ty in range(ny):
        for tx in range(nx):
            sheet.paste(big, (tx * t, ty * t))
    return sheet


def elevation(plate, brick, scale=4):
    """A wall face: venous-stone bricks between plating pilasters, on a plating base course, under a plating cornice."""
    t = 16 * scale
    cols, rows = 7, 5
    sheet = Image.new('RGBA', (cols * t, rows * t), (0, 0, 0, 0))
    big_plate = plate.resize((t, t), Image.NEAREST)
    big_brick = brick.resize((t, t), Image.NEAREST)
    for r in range(1, rows):
        for c in range(cols):
            trim = r == rows - 1 or c in (0, 3, cols - 1)
            sheet.paste(big_plate if trim else big_brick, (c * t, r * t))
    # the cornice row: bottom slabs over the bays, a stair, a wall run with a post over the middle pilaster
    lower = big_plate.crop((0, t // 2, t, t))
    for c in (0, 1, 5, 6):
        sheet.paste(lower, (c * t, t // 2))
    stairs = Image.new('RGBA', (t, t))
    stairs.paste(big_plate.crop((0, t // 2, t, t)), (0, t // 2))
    stairs.paste(big_plate.crop((t // 2, 0, t, t // 2)), (t // 2, 0))
    sheet.paste(stairs, (2 * t, 0), stairs)
    mirrored = stairs.transpose(Image.FLIP_LEFT_RIGHT)
    sheet.paste(mirrored, (4 * t, 0), mirrored)
    wall = Image.new('RGBA', (t, t))
    side = big_plate.crop((0, t - 14 * scale, t, t))
    wall.paste(side, (0, t - 14 * scale))
    post = big_plate.crop((4 * scale, 0, 12 * scale, t))
    wall.paste(post, (4 * scale, 0))
    sheet.paste(wall, (3 * t, 0), wall)
    return sheet


def preview(plate):
    blocks = TEXTURES
    brick = Image.open(blocks / 'polished_venous_stone_bricks.png').convert('RGBA')
    iron = Image.open(blocks / 'hematic_iron_block.png').convert('RGBA')
    pillar = Image.open(blocks / 'hematic_iron_pillar.png').convert('RGBA')
    chiseled = Image.open(blocks / 'chiseled_hematic_iron_block.png').convert('RGBA')
    gap = 12
    bg = (36, 32, 34, 255)

    swatches = [plate, iron, pillar, chiseled]
    sw = 16 * 8
    row1 = Image.new('RGBA', (len(swatches) * (sw + gap) - gap, sw), bg)
    for i, s in enumerate(swatches):
        row1.paste(s.resize((sw, sw), Image.NEAREST), (i * (sw + gap), 0))

    wall3 = tile(plate, 3, 3, 8)
    elev = elevation(plate, brick)

    shapes = [iso(plate, plate, plate, shape=s) for s in ('cube', 'stairs', 'slab', 'wall')]
    shapes.append(iso(brick, brick, brick, shape='cube'))
    shapes.append(iso(iron, iron, iron, shape='cube'))
    sw2 = shapes[0].width
    row3 = Image.new('RGBA', (len(shapes) * (sw2 + gap) - gap, shapes[0].height), bg)
    for i, s in enumerate(shapes):
        row3.paste(s, (i * (sw2 + gap), 0), s)

    width = max(row1.width, wall3.width + gap + elev.width, row3.width) + 2 * gap
    height = row1.height + max(wall3.height, elev.height) + row3.height + 4 * gap
    sheet = Image.new('RGBA', (width, height), bg)
    y = gap
    sheet.paste(row1, (gap, y))
    y += row1.height + gap
    sheet.paste(wall3, (gap, y))
    sheet.paste(elev, (2 * gap + wall3.width, y), elev)
    y += max(wall3.height, elev.height) + gap
    sheet.paste(row3, (gap, y), row3)
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW)


def main():
    image = plating()
    path = TEXTURES / 'hematic_iron_plating.png'
    image.save(path)
    print('wrote', path)
    preview(image)
    print('wrote', PREVIEW)


if __name__ == '__main__':
    main()

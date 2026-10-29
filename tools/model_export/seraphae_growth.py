"""Seraphae's grown stone for the mausoleum rework (Track 9): where her radiance reached, the walls healed.

    textures/block/healed_quartz_bricks.png        quartz bricks with pale scar pads knit over the joints
    textures/block/healed_calcite.png              calcite swollen with the same scar tissue in soft lumps
    textures/block/radiant_healed_quartz.png       the stone layer: healed bricks, cut away where the growth glows
    textures/block/radiant_healed_quartz_glow.png  the growth lit from inside, drawn full-bright under the cut-outs
    textures/block/bone_outgrowth.png              the atlas of the bone-spur cluster

Each stone has three growth layouts (the name, then _1 and _2; the glow layers end _glow, _glow_1, _glow_2) on the
same stone texture, and its blockstate picks one at random per position, never rotated, so a patch does not repeat.

The bricks keep vanilla quartz_bricks' layout (two 8 px courses, a full-width brick over two half-offset ones,
bed joints on rows 7 and 15) and its six tones, and the calcite keeps vanilla calcite's six greys, so either block
sits in a wall of the vanilla stone as a patch of the same wall that has grown. The growth is a field of soft
elliptical pads that wraps at 16, shaded from the upper left as a swollen surface: pale pink-ivory keloid with a
pearly crown, thin skin at the rim where the stone shows through, and a few fine pale capillaries. Healed scar,
not wound: nothing saturated, nothing red.

The radiant block is two layers. The outer cube is the healed bricks with the growth's thick middle cut out; an
inset cube 0.05 px behind it carries the glow texture with `shade: false` and full block and sky light, so the
cut-outs read as tissue lit from inside (Lux: warm white to pale gold, never blue). The block itself gives 12.

The bone outgrowth is five tapered, bent spurs on a fleshy collar, authored pointing up from the floor; the
blockstate turns it to all six facings as vanilla's amethyst cluster does. Elements bend only in 22.5 degree
steps (1.21.1's limit), so each spur is a chain of straight segments with a knuckle at every joint.

    python tools/model_export/seraphae_growth.py [--preview]

`--preview` writes build/model-previews/seraphae_growth.png (swatches beside vanilla quartz bricks and calcite,
3 x 3 tiled walls, patches set into a vanilla wall, the radiant block lit and in the dark, iso cubes) and
build/model-previews/bone_outgrowth.png (the cluster's turnaround). All noise is hashed from the texel, so a rerun
writes the same pixels.
"""
import argparse
import copy
import io
import json
import math
import zipfile

import numpy as np
from PIL import Image

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, ROOT, box, build, model_json, preview, separate, spin, write
from hematic_iron_plating import iso, tile

TEXTURES = ASSETS / 'textures/block'
PREVIEW = ROOT / 'build/model-previews/seraphae_growth.png'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}
FACES = ('down', 'up', 'north', 'south', 'west', 'east')

# --- the stone ---------------------------------------------------------------------------------------------------

# vanilla quartz_bricks' six tones, lightest first
Q_HIGH = (242, 239, 237)
Q_FACE = [(238, 234, 230), (238, 230, 222), (234, 226, 218)]
Q_JOINT = [(226, 222, 208), (221, 217, 203)]
# vanilla calcite's six greys, lightest first
CALCITE = [(255, 255, 255), (240, 245, 244), (237, 236, 230), (217, 219, 215), (201, 201, 196), (179, 179, 179)]
CALCITE_SHARE = [.074, .129, .27, .309, .164, .054]   # how much of vanilla calcite each grey covers

# Head joints of each course: one full-width brick over two half bricks offset by a quarter, as vanilla.
COURSE_JOINTS = [(15,), (3, 11)]


def quartz_bricks(seed):
    """Vanilla's quartz-brick layout repainted: a lit top row and left column, a shaded joint on the right."""
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        course, row = divmod(y, 8)
        for x in range(16):
            if row == 7 or x in COURSE_JOINTS[course]:
                rgb = Q_JOINT[noise(seed, 'joint', x, y) < .55]
            elif row == 0 or (x - 1) % 16 in COURSE_JOINTS[course]:
                rgb = Q_HIGH if noise(seed, 'lip', x, y) < .8 else Q_FACE[0]
            else:
                # horizontal runs of three tones, as vanilla's face is streaked along the course
                run = noise(seed, 'run', y, (x + int(noise(seed, 'phase', y) * 4)) // 3)
                rgb = Q_FACE[0 if run < .45 else 1 if run < .75 else 2]
            image.putpixel((x, y), rgb + (255,))
    return image


def wrap_noise(seed, pu, pv, u, v):
    """Smooth value noise on a lattice of pu x pv px that wraps at 16 in both directions."""
    cu, cv = 16 // pu, 16 // pv
    gu, gv = u / pu, v / pv
    u0, v0 = int(math.floor(gu)), int(math.floor(gv))
    tu, tv = gu - u0, gv - v0
    tu, tv = tu * tu * (3 - 2 * tu), tv * tv * (3 - 2 * tv)

    def at(i, j):
        return noise(seed, pu, pv, i % cu, j % cv)

    top = at(u0, v0) + (at(u0 + 1, v0) - at(u0, v0)) * tu
    bottom = at(u0, v0 + 1) + (at(u0 + 1, v0 + 1) - at(u0, v0 + 1)) * tu
    return top + (bottom - top) * tv


def calcite(seed):
    """Vanilla calcite's six greys as short streaks leaning like '/', quantised by vanilla's share of each.

    The noise runs fast across the streaks (x + y) and slowly along them (x - y); both wrap at 16 when x or y
    does, so the face tiles.
    """
    values = np.zeros((16, 16))
    for y in range(16):
        for x in range(16):
            a, b = (x + y + 1) % 16, (x - y) % 16
            values[y, x] = (wrap_noise(seed, 4, 8, a, b) * .55 + wrap_noise(seed + 1, 2, 4, a, b) * .35
                            + noise(seed, 'grain', x, y) * .1)
    order = np.argsort(values, axis=None)[::-1]     # brightest value first
    tones = np.empty(256, dtype=int)
    start = 0
    for i, share in enumerate(CALCITE_SHARE):
        end = 256 if i == len(CALCITE_SHARE) - 1 else start + round(share * 256)
        tones[order[start:end]] = i
        start = end
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            image.putpixel((x, y), CALCITE[tones[y * 16 + x]] + (255,))
    return image


# --- the growth --------------------------------------------------------------------------------------------------

KELOID_DEEP = (198, 160, 156)     # the crease where a pad leaves the stone, turned from the light
KELOID_SHADE = (214, 182, 176)
KELOID = (230, 204, 196)          # pale pink-ivory scar tissue
KELOID_LIT = (243, 226, 218)
PEARL = (251, 244, 237)           # the sheen on a swollen crown
CAPILLARY = (204, 150, 152)       # fine pale vessels: rose-grey, never red
LIGHT = np.array([-.55, -.62, .56])  # toward the light: up and left on the texture, then out of the face
LIGHT = LIGHT / np.linalg.norm(LIGHT)
THRESHOLD = .5                    # field value at a pad's edge


class Growth:
    """A tiling height field of soft elliptical pads: (cx, cy, rx, ry, swell) in texels."""

    def __init__(self, pads, seed, lumpiness=.05):
        self.pads, self.seed, self.lumpiness = pads, seed, lumpiness

    def at(self, x, y):
        total = 0.0
        for cx, cy, rx, ry, swell in self.pads:
            dx = (x - cx + 8) % 16 - 8
            dy = (y - cy + 8) % 16 - 8
            total += swell * math.exp(-((dx / rx) ** 2 + (dy / ry) ** 2))
        return total

    def height(self, x, y):
        return self.at(x + .5, y + .5) + (noise(self.seed, 'lump', x % 16, y % 16) - .5) * self.lumpiness

    def lit(self, x, y):
        """How the swollen surface faces the light, about 0 (away) to 1 (square on)."""
        gx = (self.at(x + 1.5, y + .5) - self.at(x - .5, y + .5)) / 2
        gy = (self.at(x + .5, y + 1.5) - self.at(x + .5, y - .5)) / 2
        normal = np.array([-gx * 1.8, -gy * 1.8, 1.0])
        normal /= np.linalg.norm(normal)
        return float(normal @ LIGHT)


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def keloid_tone(lit, depth):
    """Scar tissue by its slope to the light and how far above its rim it stands."""
    flat = float(LIGHT[2])
    if lit < flat:
        rgb = mix(KELOID, KELOID_SHADE, (flat - lit) / .3)
        if lit < flat - .3:
            rgb = mix(KELOID_SHADE, KELOID_DEEP, (flat - .3 - lit) / .25)
    else:
        rgb = mix(KELOID, KELOID_LIT, (lit - flat) / .25)
    if depth > .55 and lit > flat + .12:
        rgb = mix(rgb, PEARL, (lit - flat - .12) / .2)
    return rgb


def walk(path):
    """Grid texels along a polyline of (x, y) points, wrapping at 16."""
    out = []
    for (x0, y0), (x1, y1) in zip(path, path[1:]):
        steps = max(abs(x1 - x0), abs(y1 - y0))
        for i in range(steps):
            out.append((round(x0 + (x1 - x0) * i / steps) % 16, round(y0 + (y1 - y0) * i / steps) % 16))
    out.append((path[-1][0] % 16, path[-1][1] % 16))
    return out


def heal(stone, growth, veins, seed, rim=.12):
    """Knit the growth over a stone texture. Returns the image and a per-texel depth map (field above the rim)."""
    image = stone.copy()
    depth = np.zeros((16, 16))
    for y in range(16):
        for x in range(16):
            f = growth.height(x, y)
            base = stone.getpixel((x, y))[:3]
            if f < THRESHOLD:
                # the pad's swell throws a faint shadow down and right onto the stone it has not reached
                if growth.height(x - 1, y - 1) > THRESHOLD + .05:
                    image.putpixel((x, y), clamp(base, -10) + (255,))
                continue
            d = f - THRESHOLD
            depth[y, x] = d
            rgb = keloid_tone(growth.lit(x, y), d)
            rgb = clamp(rgb, (noise(seed, 'skin', x, y) - .5) * 5)
            if d < rim:
                # thin skin at the rim: the stone shows through it, a little pinker for the blood beneath
                rgb = mix(mix(base, KELOID_SHADE, .55), rgb, d / rim)
            image.putpixel((x, y), rgb + (255,))
    for path, strength in veins:
        for x, y in walk(path):
            here = image.getpixel((x, y))[:3]
            k = strength * (1.0 if depth[y, x] > 0 else .7)
            image.putpixel((x, y), mix(here, CAPILLARY, k) + (255,))
    return image, depth


# Pads stay inside the tile so a healed block set into a wall of the plain stone never shows growth cut off at its
# edge; only a vein or two runs out to a border. Every pad bridges a mortar joint inside the face. Each block has
# three layouts on the same stone, picked at random per position by the blockstate, so a patch of them does not
# repeat; layout 0 keeps the block's own name, the others add _1 and _2. Each layout is (pads, veins, seed).

# Healed quartz bricks.
BRICK_LAYOUTS = [
    # a broad pad across the middle bed joint, a smaller one over the lower course's right head joint, a nodule
    # on its left one
    ([(7.0, 7.0, 3.9, 2.6, 1.45), (11.6, 12.4, 2.0, 2.3, 1.35), (3.3, 11.6, 1.5, 1.4, 1.2)],
     [([(3, 8), (2, 10), (0, 11)], .28), ([(10, 5), (12, 3), (13, 0)], .26), ([(13, 14), (14, 16)], .24)], 43),
    # a pad climbing the lower course's left head joint, a ridge on the bed joint's right end, a nodule up high
    ([(3.4, 11.4, 2.4, 2.6, 1.4), (11.8, 7.2, 2.6, 1.9, 1.3), (6.0, 3.2, 1.4, 1.2, 1.1)],
     [([(5, 10), (7, 9), (8, 8)], .26), ([(13, 6), (14, 4), (16, 3)], .26), ([(2, 13), (1, 15), (1, 16)], .24)], 44),
    # sparser: a ridge on the bed joint's left end, the right head joint, a nodule high right
    ([(4.2, 7.0, 3.0, 1.8, 1.35), (11.2, 11.0, 1.7, 2.1, 1.25), (12.8, 3.6, 1.4, 1.3, 1.1)],
     [([(7, 7), (9, 8), (10, 9)], .26), ([(1, 6), (0, 4)], .24), ([(12, 13), (12, 16)], .22)], 45),
]

# Healed calcite: soft lumps of different size.
CALCITE_LAYOUTS = [
    ([(4.4, 4.4, 2.7, 2.3, 1.35), (10.8, 10.2, 3.2, 2.8, 1.5), (3.6, 12.0, 1.9, 1.7, 1.2),
      (12.4, 3.4, 1.6, 1.4, 1.1)],
     [([(6, 6), (8, 7), (9, 8)], .34), ([(2, 5), (0, 7)], .3), ([(13, 13), (14, 15)], .3),
      ([(11, 4), (10, 6), (11, 8)], .28)], 53),
    ([(10.6, 4.6, 3.0, 2.6, 1.45), (4.2, 10.8, 3.2, 2.7, 1.45), (12.2, 12.6, 1.6, 1.5, 1.1)],
     [([(7, 9), (8, 7), (8, 5)], .32), ([(13, 7), (14, 9), (16, 10)], .3), ([(2, 7), (1, 5), (0, 4)], .28)], 54),
    ([(7.8, 8.2, 2.8, 2.4, 1.4), (3.2, 3.4, 1.8, 1.6, 1.15), (12.6, 4.6, 1.7, 1.5, 1.15),
      (4.8, 13.0, 1.6, 1.4, 1.1)],
     [([(5, 5), (6, 7)], .3), ([(10, 9), (12, 11), (14, 12)], .32), ([(11, 3), (10, 1), (10, 0)], .28)], 55),
]

# Radiant: the same bricks, grown further; the pads' thick middles are cut away and glow.
RADIANT_LAYOUTS = [
    # a broad pad across the middle bed joint, a smaller one over the lower course's right head joint
    ([(7.0, 6.4, 4.4, 3.0, 1.6), (12.2, 12.8, 3.0, 2.4, 1.6)],
     [([(3, 9), (2, 11), (0, 12)], .34), ([(11, 4), (13, 2), (14, 0)], .32), ([(9, 9), (10, 11)], .26)], 61),
    # a broad pad low right on the lower course, a smaller one high left
    ([(10.0, 10.4, 4.0, 3.0, 1.6), (4.4, 4.0, 3.3, 2.7, 1.6)],
     [([(6, 10), (4, 11), (2, 13)], .34), ([(7, 3), (9, 2), (10, 0)], .3), ([(13, 7), (15, 6)], .28)], 62),
    # a tall pad over the lower course's left head joint and the bed joint, a broad one high right
    ([(4.2, 9.8, 3.0, 3.6, 1.6), (11.6, 4.6, 3.4, 2.6, 1.55)],
     [([(7, 11), (9, 12), (11, 14)], .32), ([(8, 4), (6, 3), (5, 1)], .3), ([(14, 8), (15, 10)], .28)], 63),
]
GLOW_FROM = .2                    # field depth above the rim where the stone is cut away

GLOW_CORE = (255, 252, 238)       # warm white at the thickest
GLOW = (252, 238, 204)            # pale gold
GLOW_RIM = (244, 216, 186)        # light through thin skin at the edge, warmer and pinker
GLOW_VESSEL = (230, 186, 160)     # the capillaries seen against the light


def radiant_layers(pads, veins, seed):
    stone, depth = heal(quartz_bricks(41), Growth(pads, seed), veins, seed)
    glow = Image.new('RGBA', (16, 16))
    peak = depth.max()
    for y in range(16):
        for x in range(16):
            d = depth[y, x]
            t = max(0.0, (d - GLOW_FROM) / max(peak - GLOW_FROM, 1e-6))
            rgb = mix(GLOW_RIM, GLOW, t / .45) if t < .45 else mix(GLOW, GLOW_CORE, (t - .45) / .55)
            rgb = clamp(rgb, (noise(seed, 'glow', x, y) - .5) * 6)
            glow.putpixel((x, y), rgb + (255,))
            if d > GLOW_FROM:
                stone.putpixel((x, y), (0, 0, 0, 0))
    for path, strength in veins:
        for x, y in walk(path):
            if depth[y, x] > GLOW_FROM:
                glow.putpixel((x, y), mix(glow.getpixel((x, y))[:3], GLOW_VESSEL, strength * 1.4) + (255,))
    return stone, glow


def variant(name, i):
    return name if i == 0 else f'{name}_{i}'


def cubes():
    """Every texture by name: three layouts of each block, and a glow layer for each radiant one."""
    out = {}
    for i, (pads, veins, seed) in enumerate(BRICK_LAYOUTS):
        out[variant('healed_quartz_bricks', i)] = heal(quartz_bricks(41), Growth(pads, seed), veins, seed)[0]
    for i, (pads, veins, seed) in enumerate(CALCITE_LAYOUTS):
        out[variant('healed_calcite', i)] = heal(calcite(48), Growth(pads, seed, lumpiness=.08), veins, seed)[0]
    for i, layout in enumerate(RADIANT_LAYOUTS):
        stone, glow = radiant_layers(*layout)
        out[variant('radiant_healed_quartz', i)] = stone
        out[variant('radiant_healed_quartz_glow', i)] = glow
    return out


def cube_model(name):
    return {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'hemomancy:block/{name}'}}


def radiant_model(i):
    def faces(texture):
        return {f: {'uv': [0, 0, 16, 16], 'texture': texture, 'cullface': f} for f in FACES}

    inset = .05
    glow = {'name': 'glow', 'from': [inset] * 3, 'to': [16 - inset] * 3, 'faces': faces('#glow')}
    glow.update(BRIGHT)
    stone = {'name': 'stone', 'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': faces('#stone')}
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
            'textures': {'stone': f'hemomancy:block/{variant("radiant_healed_quartz", i)}',
                         'glow': f'hemomancy:block/{variant("radiant_healed_quartz_glow", i)}',
                         'particle': 'hemomancy:block/healed_quartz_bricks'},
            'elements': [glow, stone]}


# --- the bone outgrowth ------------------------------------------------------------------------------------------

OUTGROWTH = 'bone_outgrowth'
BONE = (234, 225, 204)
BONE_SHADE = (208, 196, 172)
BONE_TIP = (242, 236, 222)
MARROW = (176, 150, 128)

# Spurs: base (x, z), base width, then segments of (length, end width, axis, angle). Each segment is three
# stacked boxes narrowing toward its end, all tilted about the segment's foot; a knuckle sits at every joint.
# They lean out from the cluster's middle and thin to a point.
SPURS = [
    ((8.0, 8.5), 2.5, [(4.5, 2.0, 'x', 0), (3.5, 1.5, 'x', -22.5), (3.0, .5, 'x', -45)]),
    ((5.0, 6.5), 2.0, [(3.5, 1.5, 'z', 22.5), (3.0, 1.0, 'z', 45), (1.75, .5, 'z', 45)]),
    ((11.0, 5.25), 2.0, [(3.0, 1.25, 'x', -22.5), (2.5, .5, 'x', -45)]),
    ((11.5, 11.0), 1.75, [(3.0, 1.25, 'z', -22.5), (2.25, .5, 'z', -45)]),
    ((5.5, 11.25), 1.5, [(2.25, 1.0, 'x', 0), (1.75, .5, 'x', 22.5)]),
]
SPUR_FOOT = .25         # spurs rise from inside the collar
SUBS = 3


def tilt(axis, angle, v):
    a = math.radians(angle)
    x, y, z = v
    if axis == 'x':
        return (x, y * math.cos(a) - z * math.sin(a), y * math.sin(a) + z * math.cos(a))
    return (x * math.cos(a) - y * math.sin(a), x * math.sin(a) + y * math.cos(a), z)


def spur(index, base, width, segments):
    e = []
    x, y, z = base[0], SPUR_FOOT, base[1]
    w = width
    for j, (length, end, axis, angle) in enumerate(segments):
        last = j == len(segments) - 1
        step = length / SUBS
        for k in range(SUBS):
            # the tip segment narrows all the way to its end width; the others leave the last step to the knuckle
            t = (k + 1) / SUBS if last else k / SUBS
            sw = w + (end - w) * t
            mat = 'tip' if last and k == SUBS - 1 else 'bone'
            skip = ('down',) if k else ('down',) if j == 0 else ()
            e.append(box(f'spur{index}_seg{j}_{k}', (x - sw / 2, y + k * step, z - sw / 2),
                         (x + sw / 2, y + (k + 1) * step, z + sw / 2), mat,
                         rotation=spin(axis, angle, (x, y, z)) if angle else None, skip=skip))
        dx, dy, dz = tilt(axis, angle, (0, length, 0))
        x, y, z = x + dx, y + dy, z + dz
        if not last:
            # the knuckle: a swollen joint, tilted halfway between the two segments
            k = end + .6
            nxt = segments[j + 1]
            same = nxt[2] == axis or not angle or not nxt[3]
            knuckle_axis = axis if angle else nxt[2]
            mean = round(((angle if knuckle_axis == axis else 0) + nxt[3]) / 2 / 22.5) * 22.5 if same else angle
            e.append(box(f'spur{index}_knuckle{j}', (x - k / 2, y - .45, z - k / 2), (x + k / 2, y + .45, z + k / 2),
                         'knuckle', rotation=spin(knuckle_axis, mean, (x, y, z)) if mean else None))
        w = end
    return e


def outgrowth():
    """The healed collar spreads in low lobes over the stone it grew from, swelling where the spurs leave it."""
    e = [box('collar', (4.5, 0, 5.0), (11.5, .5, 12.0), 'keloid'),
         box('collar_lobe_w', (3.0, 0, 6.0), (4.5, .35, 10.5), 'keloid', skip=('east',)),
         box('collar_lobe_e', (11.5, 0, 6.5), (13.25, .35, 11.0), 'keloid', skip=('west',)),
         box('collar_lobe_n', (6.0, 0, 3.25), (11.0, .35, 5.0), 'keloid', skip=('south',)),
         box('collar_lobe_s', (6.25, 0, 12.0), (10.25, .35, 13.25), 'keloid', skip=('north',)),
         box('collar_swell', (5.75, .5, 6.25), (10.5, 1.0, 11.0), 'keloid', skip=('down',))]
    for i, (base, width, segments) in enumerate(SPURS):
        r = width / 2 + .5
        e.append(box(f'spur{i}_collar', (base[0] - r, .5, base[1] - r), (base[0] + r, 1.5, base[1] + r), 'keloid',
                     skip=('down',)))
        e += spur(i, base, width, segments)
    return e


def bone(face, lx, ly, w, h, key):
    """Bone along its length: faint lengthwise grain, pores, shading toward the root."""
    n = noise('bone', key, face, lx, ly)
    if face in ('up', 'down'):
        rgb = clamp(BONE, int(n * 10) - 2)
        if w >= 2 and h >= 2 and 0 < lx < w - 1 and 0 < ly < h - 1:
            rgb = MARROW if face == 'down' else clamp(BONE, 6)
        return rgb + (255,)
    grain = noise('grain', key, face, lx)
    root = ly / max(h - 1, 1)
    rgb = mix(BONE, BONE_SHADE, root * .35 + (grain - .5) * .3)
    rgb = clamp(rgb, int(n * 8) - 4 + (10 if lx == 0 else -8 if lx == w - 1 and w > 1 else 0))
    if n > .94:
        rgb = clamp(BONE_SHADE, -26)
    return rgb + (255,)


def tip(face, lx, ly, w, h, key):
    """The spur's point: worn smooth and paler."""
    n = noise('tip', key, face, lx, ly)
    rgb = mix(BONE_TIP, BONE, ly / max(h, 1) * .6)
    return clamp(rgb, int(n * 6) - 3 + (8 if lx == 0 else 0)) + (255,)


def knuckle(face, lx, ly, w, h, key):
    """A swollen joint: yellower, a lit lip and a dark seam where it pinches."""
    n = noise('knuckle', key, face, lx, ly)
    rgb = clamp(mix(BONE, (212, 196, 166), .6), int(n * 8) - 4)
    if face not in ('up', 'down') and ly == h - 1:
        rgb = clamp(rgb, -22)
    elif face not in ('up', 'down') and ly == 0:
        rgb = clamp(rgb, 12)
    return rgb + (255,)


def keloid(face, lx, ly, w, h, key):
    """The healed collar: pink-ivory scar tissue, a pearly crown and a capillary or two."""
    n = noise('keloid', key, face, lx, ly)
    if face == 'up':
        rgb = mix(KELOID_LIT, PEARL, .4) if lx in (1, 2) and ly in (1, 2) else KELOID_LIT
        if lx in (0, w - 1) or ly in (0, h - 1):
            rgb = KELOID
        if noise('cap', key, (lx + ly) // 2, (lx - ly) // 3) < .09:
            rgb = mix(rgb, CAPILLARY, .45)
    elif face == 'down':
        rgb = KELOID_DEEP
    else:
        rgb = KELOID if ly == 0 else KELOID_SHADE
    return clamp(rgb, int(n * 6) - 3) + (255,)


SPECIAL = dict(bone=bone, tip=tip, knuckle=knuckle, keloid=keloid)

FACING = {'up': {}, 'down': {'x': 180}, 'north': {'x': 90}, 'south': {'x': 90, 'y': 180},
          'west': {'x': 90, 'y': 270}, 'east': {'x': 90, 'y': 90}}


def outgrowth_blockstate():
    return {'variants': {f'facing={f}': dict({'model': f'hemomancy:block/{OUTGROWTH}'}, **rot)
                         for f, rot in FACING.items()}}


# --- writing -----------------------------------------------------------------------------------------------------

def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n')


def random_blockstate(name, count):
    """The block's one state picks among its layouts per position, evenly; never rotated, so bricks stay upright."""
    return {'variants': {'': [{'model': f'hemomancy:block/{variant(name, i)}'} for i in range(count)]}}


def export_cubes():
    images = cubes()
    for name, image in images.items():
        image.save(TEXTURES / f'{name}.png')
    layouts = {'healed_quartz_bricks': BRICK_LAYOUTS, 'healed_calcite': CALCITE_LAYOUTS,
               'radiant_healed_quartz': RADIANT_LAYOUTS}
    for name, options in layouts.items():
        for i in range(len(options)):
            model = radiant_model(i) if name == 'radiant_healed_quartz' else cube_model(variant(name, i))
            write_json(ASSETS / f'models/block/{variant(name, i)}.json', model)
        write_json(ASSETS / f'blockstates/{name}.json', random_blockstate(name, len(options)))
        write_json(ASSETS / f'models/item/{name}.json', {'parent': f'hemomancy:block/{name}'})
    print('wrote healed_quartz_bricks, healed_calcite, radiant_healed_quartz: three layouts each (+ glow)')
    return images


def export_outgrowth():
    elements = outgrowth()
    moved = separate(elements)
    atlas, models = build({'block': elements}, SPECIAL, atlas_w=64)
    atlas.save(TEXTURES / f'{OUTGROWTH}.png')
    model = model_json(models['block'], OUTGROWTH, atlas.height, f'hemomancy:block/{OUTGROWTH}')
    model['texture_size'] = [64, atlas.height]
    write({f'block/{OUTGROWTH}': model}, OUTGROWTH, model)
    write_json(ASSETS / f'blockstates/{OUTGROWTH}.json', outgrowth_blockstate())
    write_json(ASSETS / f'models/item/{OUTGROWTH}.json', {'parent': f'hemomancy:block/{OUTGROWTH}'})
    extent = [min(min(e['from'][a], e['to'][a]) for e in model['elements']) for a in range(3)], \
        [max(max(e['from'][a], e['to'][a]) for e in model['elements']) for a in range(3)]
    print(f'wrote bone_outgrowth: {len(elements)} cubes, {moved} faces separated, 64x{atlas.height} atlas, '
          f'unrotated extent {extent}')
    return model


# --- preview -----------------------------------------------------------------------------------------------------

def vanilla(name):
    jars = sorted((ROOT / 'build/moddev/artifacts').glob('neoforge-*-client-extra-aka-minecraft-resources.jar'))
    if not jars:
        return Image.new('RGBA', (16, 16), (255, 0, 255, 255))
    with zipfile.ZipFile(jars[-1]) as jar:
        return Image.open(io.BytesIO(jar.read(f'assets/minecraft/textures/block/{name}.png'))).convert('RGBA')


def composite(stone, glow, dark=1.0):
    """The radiant block as seen: the stone layer lit by `dark`, the glow full-bright under its cut-outs."""
    out = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            s = stone.getpixel((x, y))
            rgb = glow.getpixel((x, y))[:3] if s[3] == 0 else clamp(tuple(round(c * dark) for c in s[:3]))
            out.putpixel((x, y), rgb + (255,))
    return out


def mixed_wall(plain, layouts, cols, rows, patch, scale, seed=0):
    """A wall face: the plain stone with the cells in `patch` (all cells when None) drawn as a random layout."""
    t = 16 * scale
    sheet = Image.new('RGBA', (cols * t, rows * t))
    big = [image.resize((t, t), Image.NEAREST) for image in layouts]
    flat = plain.resize((t, t), Image.NEAREST)
    for r in range(rows):
        for c in range(cols):
            grown = patch is None or (c, r) in patch
            pick = big[int(noise('wall', seed, c, r) * len(big))] if grown else flat
            sheet.paste(pick, (c * t, r * t))
    return sheet


def row(images, gap, bg):
    out = Image.new('RGBA', (sum(i.width for i in images) + gap * (len(images) - 1), max(i.height for i in images)),
                    bg)
    x = 0
    for i in images:
        out.paste(i, (x, 0), i)
        x += i.width + gap
    return out


def preview_cubes(images):
    gap, bg = 12, (36, 32, 34, 255)
    qb, ca = vanilla('quartz_bricks'), vanilla('calcite')
    hq = [images[variant('healed_quartz_bricks', i)] for i in range(len(BRICK_LAYOUTS))]
    hc = [images[variant('healed_calcite', i)] for i in range(len(CALCITE_LAYOUTS))]
    layers = [(images[variant('radiant_healed_quartz', i)], images[variant('radiant_healed_quartz_glow', i)])
              for i in range(len(RADIANT_LAYOUTS))]
    lit = [composite(stone, glow) for stone, glow in layers]
    dim = [composite(stone, glow, .55) for stone, glow in layers]
    sw = 16 * 6
    swatches = row([i.resize((sw, sw), Image.NEAREST) for i in [qb] + hq + lit + [ca] + hc], gap, bg)
    # a patch of 4-16 blocks, as the structures place them, inside a plain wall
    patch = {(1, 1), (2, 1), (3, 1), (1, 2), (2, 2), (3, 2), (4, 2), (2, 3), (3, 3), (5, 0)}
    walls = row([mixed_wall(qb, hq, 4, 4, None, 5), mixed_wall(qb, hq, 7, 5, patch, 5, 1),
                 mixed_wall(qb, lit, 4, 4, None, 5, 2)], gap, bg)
    walls2 = row([mixed_wall(ca, hc, 4, 4, None, 5, 3), mixed_wall(ca, hc, 7, 5, patch, 5, 4),
                  mixed_wall(composite(qb, qb, .55), dim, 7, 5, patch, 5, 5)], gap, bg)
    isos = row([iso(hq[0], hq[1], hq[2]), iso(qb, qb, qb), iso(lit[0], lit[1], lit[2]),
                iso(dim[1], dim[2], dim[0]), iso(hc[0], hc[1], hc[2]), iso(ca, ca, ca)], gap, bg)
    parts = [swatches, walls, walls2, isos]
    sheet = Image.new('RGBA', (max(p.width for p in parts) + 2 * gap, sum(p.height for p in parts) + 5 * gap), bg)
    y = gap
    for p in parts:
        sheet.paste(p, (gap, y), p)
        y += p.height + gap
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW)
    print('wrote', PREVIEW)


def preview_outgrowth(model):
    elements = copy.deepcopy(model['elements'])
    preview(elements, OUTGROWTH, 'Bone Outgrowth - facing up, actual atlas', OUTGROWTH, lift=-2, size=360, scale=17)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    images = export_cubes()
    model = export_outgrowth()
    if args.preview:
        preview_cubes(images)
        preview_outgrowth(model)


if __name__ == '__main__':
    main()

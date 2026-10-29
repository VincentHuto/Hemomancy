"""Velorum's monastery materials for the mausoleum rework (Track 11): he stopped, and his house froze around him.

    textures/block/rimed_deepslate_bricks.png (+ _1, _2)       deepslate bricks, a frost fringe and drip ticks on top
    textures/block/rimed_deepslate_bricks_top.png (+ _1, _2)   the rime crust over the bricks' top face
    textures/block/rime_lantern.png / rime_lantern_core.png    the clear-ice shell and the full-bright core behind it
    textures/block/hanging_icicle.png                          the atlas of every icicle cluster
    textures/block/frozen_drip.png                             the atlas of the frozen runoff sheets
    textures/block/dark_lacquer_planks.png                     near-black lacquered boards, oxblood sheen, iron nails

The rimed family is the trim of every exposed ledge. The bricks keep vanilla deepslate_bricks' layout (two 8 px
courses, head joints at 15 and 7, a lit top row, a two-row bed joint) and its seven greys, so a rimed cornice sits on
a deepslate wall as the same stone, frosted. The top face is a rime crust: a field of small crystal grains, each a
facet lit from the upper left, blue in the creases between grains, a few white glints, the crust dipping a little
over the joints. The side faces carry a fringe of the crust along their top two or three rows and a
few short ice ticks below it. Three layouts, each with its own crust and ticks; the cube and the slab pick one at
random per position and turn it four ways, the stairs pick a layout but keep their facing. The undersides are
vanilla deepslate bricks.

Stairs, slab and wall have their own elements (not vanilla's) so the fringe sits at the top edge of every exposed
ledge: each element's side faces are mapped from the texture's top row down, never from the block's world height.
A straight stair is a step and a full-height back column, so the riser and the step each get their own fringe and
the back face shows one fringe at the top. Upside-down stairs are authored separately (the x = 180 turn of the
bottom models) so their fringe also stays on top; the blockstate keeps vanilla's y turns and drops the x turn.

The rime lantern is a clear-ice cube (translucent, rime creeping in unevenly from the edges, streaks, a crack, a few
bubbles) over a core inset 2 px drawn full-bright (`shade: false`, block and sky light 15): a pale blue-white light
seen through ice.

The hanging icicle has three lengths: `short` (a cluster of four or five icicles ending in the cell), `base` (the
thick upper cell of a two-cell icicle, a central shaft and two or three small ones) and `tip` (the shaft tapering
out, below a base). Every icicle is stepped square segments in translucent pale ice with a lit left edge, a deep
right edge and vertical striations; a segment's underside is cut away where the narrower segment below meets it,
so no inner face shows through the ice. Two layouts per length, each turned four ways; the shaft is centred, so any
base meets any tip.

The frozen drip is runoff frozen down a wall face, authored for facing=north (the wall at z = 16): a thin film 0.75 px
proud with ribbons 1.5-3 px proud. `end=false` is a continuous sheet whose ribbons leave the cell where they enter it;
`end=true` lets the film give out and the ribbons taper into beads and short icicles. Two sheet layouts share the
ribbons' edges and differ in their bulges, so a run of them never repeats and never breaks.

    python tools/model_export/velorum_materials.py [--preview]

`--preview` writes build/model-previews/velorum_materials.png (swatches beside the vanilla blocks they stand for,
tiled faces, a frosted cornice on a deepslate wall, iso blocks, the lantern lit and in the dark),
build/model-previews/rimed_ledges.png (the stairs, slab and wall elements), hanging_icicle.png / frozen_drip.png
(blended turnarounds) and hanging_icicle_row.png / frozen_drip_column.png (a vault and pilasters at game scale).
All noise is hashed from the texel, so a rerun writes the same pixels.
"""
import argparse
import copy
import math

import numpy as np
from PIL import Image, ImageDraw

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, ROOT, box, build, separate, write
from hematic_iron_plating import iso
from putriciel_materials import cube_all, stairs_blockstate, tex, tone
from seraphae_growth import mix, row, vanilla, variant, wrap_noise, write_json

TEXTURES = ASSETS / 'textures/block'
PREVIEW = ROOT / 'build/model-previews/velorum_materials.png'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}
FACES = ('down', 'up', 'north', 'south', 'west', 'east')
TURNS = (0, 90, 180, 270)
BRICKS = 'rimed_deepslate_bricks'
STAIRS = 'rimed_deepslate_brick_stairs'
SLAB = 'rimed_deepslate_brick_slab'
WALL = 'rimed_deepslate_brick_wall'
UNDERSIDE = 'minecraft:block/deepslate_bricks'

# --- deepslate bricks ---------------------------------------------------------------------------------------------

# vanilla deepslate_bricks' seven greys, darkest first
SLATE = [(36, 36, 36), (45, 45, 45), (56, 55, 55), (65, 65, 65), (75, 76, 79), (88, 88, 88), (110, 110, 110)]
SLATE_HEADS = (15, 7)                     # each course's head joint
SLATE_ROWS = (6.0, 5.1, 4.9, 4.2, 3.4, 2.7)  # a brick's tone by row: lit top, darkening down to the bed joint


def deepslate_bricks(seed):
    """Vanilla's deepslate-brick layout repainted: a lit top row and left column, dark right end, two-row bed joint."""
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        course, r = divmod(y, 8)
        head = SLATE_HEADS[course]
        for x in range(16):
            if r == 7:
                rgb = SLATE[1] if noise(seed, 'bed', x, y) < .55 else SLATE[0]
            elif x == head:
                rgb = SLATE[0]
            elif r == 6:
                rgb = SLATE[2] if noise(seed, 'lip', x, y) < .85 else SLATE[1]
            else:
                lx = (x - head - 1) % 16              # 0 at the brick's left end, 14 at its right
                v = SLATE_ROWS[r] + (noise(seed, 'tone', x, y) - .5) * 1.5
                if lx == 0 and r > 0:
                    v = max(v, 6.0 if r < 4 else 5.0)
                if lx >= 13:
                    v -= 1.3
                elif lx == 12 and r == 0:
                    v -= 1.0
                rgb = tone(SLATE, v)
            image.putpixel((x, y), rgb + (255,))
    return image


def is_joint(x, y):
    course, r = divmod(y, 8)
    return r == 7 or x == SLATE_HEADS[course]


# --- rime ---------------------------------------------------------------------------------------------------------

RIME = [(122, 148, 190), (154, 180, 218), (184, 206, 237), (210, 227, 248), (231, 240, 253), (246, 250, 255)]
GLINT = (255, 255, 255)
ICE_BLUE = (150, 186, 238)               # a grain of clear ice in the crust, the blue of blue ice
TICK = [(118, 156, 218), (160, 194, 240), (206, 226, 251)]   # an ice tick: deep, body, its lit tip
LIGHT_DIR = (-.62, -.78)                 # toward the light on the texture: up and to the left


def grain_field(seed, count):
    """Small crystal grains: each texel's nearest grain, its offset from that grain's centre, and the two distances."""
    centres = [(noise(seed, 'gx', i) * 16, noise(seed, 'gy', i) * 16) for i in range(count)]

    def at(x, y):
        best = []
        for i, (cx, cy) in enumerate(centres):
            dx = (x + .5 - cx + 8) % 16 - 8
            dy = (y + .5 - cy + 8) % 16 - 8
            best.append((math.hypot(dx, dy), i, dx, dy))
        best.sort()
        d1, i, dx, dy = best[0]
        return i, dx, dy, d1, best[1][0]
    return at


def rime_tone(seed, field, x, y, lift=0.0, crease=1.3):
    """The crust at a texel: the grain's facet toward the light, brighter on its lit side, blue in the creases."""
    i, dx, dy, d1, d2 = field(x, y)
    angle = noise(seed, 'facet', i) * math.tau
    tilt = .55 + noise(seed, 'tilt', i) * .45
    lit = (math.cos(angle) * LIGHT_DIR[0] + math.sin(angle) * LIGHT_DIR[1]) * tilt
    v = 3.4 + lift + lit * 1.2 + (noise(seed, 'grain', x, y) - .5) * .7
    v -= (dx * LIGHT_DIR[0] + dy * LIGHT_DIR[1]) * .28    # each grain is a little dome: lit toward the light
    if d2 - d1 < .55:
        v -= crease                                        # the crease between two grains
    return v


def rime_top(seed, stone):
    field = grain_field(seed, 22)
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            v = rime_tone(seed, field, x, y)
            rgb = tone(RIME, v)
            under = stone.getpixel((x, y))[:3]
            if is_joint(x, y) and noise(seed, 'thin', x, y) < .7:
                rgb = mix(rgb, RIME[0], .55)                       # the crust dips over the joints
            elif noise(seed, 'blue', x, y) > .965:
                rgb = ICE_BLUE
            image.putpixel((x, y), rgb + (255,))
    glints(image, seed, 5)
    return image


def glints(image, seed, count):
    for k in range(count):
        x, y = int(noise(seed, 'glint_x', k) * 16), int(noise(seed, 'glint_y', k) * 16)
        if sum(image.getpixel((x, y))[:3]) < 3 * 150:
            continue
        image.putpixel((x, y), GLINT + (255,))
        nx = (x + 1) % 16
        image.putpixel((nx, y), mix(image.getpixel((nx, y))[:3], GLINT, .45) + (255,))


def rime_side(seed, stone, ticks):
    """The bricks with the crust's fringe over their top rows and short ice ticks hanging from it."""
    image = stone.copy()
    field = grain_field(seed + 1, 22)
    depth = []
    for x in range(16):
        d = 2 + (noise(seed, 'deep', x) > .6) - (noise(seed, 'thin', x) < .18)
        depth.append(d)
        for r in range(d):
            v = rime_tone(seed + 1, field, x, r, lift=.9 - r * .6, crease=.7)
            rgb = tone(RIME, v)
            if r == d - 1 and d > 1 and noise(seed, 'ragged', x) < .35:
                d = r                                             # the crust's ragged lower edge
                break
            image.putpixel((x, r), rgb + (255,))
        depth[-1] = d
        # the crust's shadow on the stone just under it
        below = image.getpixel((x, d))[:3]
        image.putpixel((x, d), mix(below, (24, 28, 40), .25) + (255,))
    for x, length in ticks:
        top = depth[x]
        for k in range(length):
            y = top + k
            rgb = TICK[2] if k == length - 1 else TICK[1] if k == 0 else mix(TICK[1], TICK[0], .5)
            image.putpixel((x, y), rgb + (255,))
            right = (x + 1) % 16
            image.putpixel((right, y), mix(image.getpixel((right, y))[:3], (20, 24, 36), .3) + (255,))
        tip = top + length
        image.putpixel((x, tip), mix(image.getpixel((x, tip))[:3], (18, 22, 34), .4) + (255,))
    # a few flecks of rime caught on the first bed joint's lip
    for k in range(3):
        x = int(noise(seed, 'fleck', k) * 16)
        image.putpixel((x, 6), mix(image.getpixel((x, 6))[:3], RIME[3], .6) + (255,))
    return image


# (seed, ticks: (x, rows below the crust))
RIME_LAYOUTS = [(141, [(3, 2), (9, 1), (13, 3)]), (151, [(1, 1), (6, 3), (11, 2)]), (161, [(4, 3), (8, 1), (14, 2)])]


# --- rime lantern -------------------------------------------------------------------------------------------------

LANTERN = 'rime_lantern'
CORE = [(150, 188, 236), (184, 212, 246), (210, 229, 251), (231, 242, 255), (247, 251, 255)]
CORE_INSET = 2
SHELL_CLEAR = (178, 208, 246)
SHELL_STREAK = (226, 239, 255)
CRACK = [(1, 11), (4, 8), (6, 8), (9, 4), (11, 2)]
BUBBLES = [(10, 11), (12, 9), (5, 4), (3, 13)]


def lantern_core():
    """A cold blue-white light: brightest in the middle, a darker frame at the edge of the 10 px face."""
    image = Image.new('RGBA', (16, 16))
    lo, hi = CORE_INSET, 15 - CORE_INSET
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = max(abs(dx), abs(dy)) * .7 + math.hypot(dx, dy) * .3
            v = 4.3 - r * .55 + (noise('core', x, y) - .5) * .5
            if x in (lo, hi) or y in (lo, hi):
                v = .9 + (noise('frame', x, y) > .7) * .5          # the frame round the light
            elif (x in (lo + 1, hi - 1) and y in (lo + 1, hi - 1)):
                v = 1.6                                         # the frame's inner corners
            elif abs(abs(dx) - abs(dy)) < .6 and 1.2 < r < 3.6:
                v -= .6                                         # faint facet lines to the corners
            image.putpixel((x, y), tone(CORE, v) + (255,))
    return image


def walk(path):
    out = []
    for (x0, y0), (x1, y1) in zip(path, path[1:]):
        steps = max(abs(x1 - x0), abs(y1 - y0))
        for i in range(steps):
            out.append((round(x0 + (x1 - x0) * i / steps), round(y0 + (y1 - y0) * i / steps)))
    out.append(path[-1])
    return out


def frost_reach(x, y):
    """How far the frost creeps in from the nearest edge at this texel, 1.2 to 3.4 px, wandering along the edge."""
    along = x if min(y, 15 - y) <= min(x, 15 - x) else y
    return 1.2 + wrap_noise(173, 4, 4, along, along) * 1.6 + noise('reach', along) * .6


def lantern_shell():
    """Clear ice: rime creeping in unevenly from the edges, a see-through middle with streaks, a crack, bubbles."""
    image = Image.new('RGBA', (16, 16))
    field = grain_field(171, 22)
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y) + .5
            reach = frost_reach(x, y)
            if edge < reach:
                rgb = tone(RIME, rime_tone(171, field, x, y, lift=.5, crease=.8))
                a = 246 if edge < 1 else int(236 - (edge / reach) * 70)
                image.putpixel((x, y), rgb + (a,))
            elif edge < reach + 1:
                image.putpixel((x, y), clamp(SHELL_STREAK, int(noise('bevel', x, y) * 10) - 6) + (128,))
            else:
                streak = (x - y + int(noise('streak', (x + y) // 5) * 3)) % 7
                if streak == 0:
                    image.putpixel((x, y), SHELL_STREAK + (118,))
                elif streak == 1:
                    image.putpixel((x, y), mix(SHELL_CLEAR, SHELL_STREAK, .5) + (92,))
                else:
                    image.putpixel((x, y), clamp(SHELL_CLEAR, int(noise('clear', x, y) * 10) - 5) + (66,))
    for x, y in walk(CRACK):
        image.putpixel((x, y), (238, 246, 255, 176))
    for x, y in BUBBLES:
        image.putpixel((x, y), (244, 250, 255, 168))
    return image


def lantern_model():
    def faces(texture, cull, uv=(0, 0, 16, 16)):
        out = {}
        for f in FACES:
            out[f] = {'uv': list(uv), 'texture': texture}
            if cull:
                out[f]['cullface'] = f
        return out

    lo, hi = CORE_INSET, 16 - CORE_INSET
    core = {'name': 'core', 'from': [lo] * 3, 'to': [hi] * 3, 'faces': faces('#core', False, (lo, lo, hi, hi))}
    core.update(BRIGHT)
    shell = {'name': 'shell', 'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': faces('#shell', True)}
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:translucent', 'ambientocclusion': False,
            'textures': {'shell': tex(LANTERN), 'core': tex(f'{LANTERN}_core'), 'particle': tex(LANTERN)},
            'elements': [core, shell]}


# --- dark lacquer planks ------------------------------------------------------------------------------------------

LACQUER = [(14, 11, 11), (19, 15, 15), (25, 19, 19), (32, 25, 24), (41, 32, 30)]
LACQUER_GAP = (7, 5, 6)
OXBLOOD = (62, 26, 30)                   # the lacquer's sheen
OXBLOOD_HIGH = (84, 38, 42)
NAIL = (80, 84, 95)                      # hematic iron, its lit tone
NAIL_SHADE = (42, 37, 45)
LACQUER_JOINTS = (11, 4, 14, 7)          # each board's head joint, staggered as vanilla's
NAILED = (1,)                            # the board nailed at its end, one nail a block
GLOSS = (2, 9, 6, 12)                    # where each board's slanting gloss streak starts


# Grain lines along each board: (board, row, start x, length, darker?). They wrap at 16 like the boards.
LACQUER_GRAIN = [(0, 1, 2, 7, True), (0, 2, 12, 5, False), (0, 0, 7, 4, False), (1, 2, 6, 8, True),
                 (1, 0, 13, 6, True), (1, 1, 9, 3, False), (2, 1, 0, 6, True), (2, 2, 8, 6, False),
                 (2, 0, 3, 5, True), (3, 2, 1, 9, True), (3, 1, 10, 4, False), (3, 0, 15, 5, True)]


def dark_lacquer_planks(seed=181):
    grain = {}
    for board, r, x0, length, dark in LACQUER_GRAIN:
        for k in range(length):
            grain[((x0 + k) % 16, board * 4 + r)] = -1.0 if dark else .8
    image = Image.new('RGBA', (16, 16))
    for y in range(16):
        board, r = divmod(y, 4)
        joint = LACQUER_JOINTS[board]
        for x in range(16):
            if r == 3:
                rgb = LACQUER_GAP if noise(seed, 'gap', x) < .7 else LACQUER[0]
            elif x == joint:
                rgb = LACQUER_GAP
            else:
                v = 2.2 + (noise(seed, 'board', board) - .5) * .5 + (noise(seed, 'fleck', x, y) - .5) * .5
                v += grain.get((x, y), 0) + (.4 if r == 0 else -.4 if r == 2 else 0)
                if (x - 1) % 16 == joint:
                    v += .6                               # the board's lit end
                elif (x + 1) % 16 == joint:
                    v -= .8
                rgb = tone(LACQUER, v)
                if r == 0:
                    rgb = mix(rgb, OXBLOOD, .14)                         # a faint sheen along the board's top
                g = (x - GLOSS[board]) % 16
                if r == 1 and g < 4:
                    rgb = mix(rgb, OXBLOOD_HIGH if g in (1, 2) else OXBLOOD, .32 if g in (1, 2) else .16)
            image.putpixel((x, y), rgb + (255,))
    for board in NAILED:
        joint = LACQUER_JOINTS[board]
        x, y = (joint + 2) % 16, board * 4 + 1
        image.putpixel((x, y), NAIL + (255,))
        image.putpixel(((x + 1) % 16, y), mix(image.getpixel(((x + 1) % 16, y))[:3], NAIL_SHADE, .5) + (255,))
    return image


# --- rimed family models ------------------------------------------------------------------------------------------

def face_uv(face, lo, hi):
    """UVs by position, except that side faces are mapped from the texture's top row down (the fringe's rows)."""
    x0, y0, z0 = lo
    x1, y1, z1 = hi
    h = y1 - y0
    return {'up': [x0, z0, x1, z1], 'down': [x0, 16 - z1, x1, 16 - z0], 'north': [16 - x1, 0, 16 - x0, h],
            'south': [x0, 0, x1, h], 'west': [z0, 0, z1, h], 'east': [16 - z1, 0, 16 - z0, h]}[face]


AXIS = {'west': (0, 0), 'east': (0, 1), 'down': (1, 0), 'up': (1, 1), 'north': (2, 0), 'south': (2, 1)}
OPPOSITE = {'west': 'east', 'east': 'west', 'down': 'up', 'up': 'down', 'north': 'south', 'south': 'north'}


def covered(face, el, others):
    """Whether another element's opposite face lies on this face and covers all of it."""
    axis, side = AXIS[face]
    plane = (el[1] if side else el[0])[axis]
    rest = [a for a in range(3) if a != axis]
    for other in others:
        if other is el:
            continue
        o_axis, o_side = AXIS[OPPOSITE[face]]
        if (other[1] if o_side else other[0])[o_axis] != plane:
            continue
        if all(other[0][a] <= el[0][a] and el[1][a] <= other[1][a] for a in rest):
            return True
    return False


def ledge_elements(boxes, skip=()):
    """Elements for boxes (lo, hi) in the rimed textures: frost on top, fringed sides, the vanilla bricks beneath."""
    out = []
    for el in boxes:
        lo, hi = el
        faces = {}
        for face in FACES:
            if face in skip or covered(face, el, boxes):
                continue
            texture = '#top' if face == 'up' else '#bottom' if face == 'down' else '#side'
            faces[face] = {'uv': face_uv(face, lo, hi), 'texture': texture}
            axis, side = AXIS[face]
            if (hi if side else lo)[axis] == (16 if side else 0):
                faces[face]['cullface'] = face
        out.append({'from': list(lo), 'to': list(hi), 'faces': faces})
    return out


# Facing east, as vanilla's stair models: the step on the west, the back column on the east.
STAIR_BOXES = {
    '': [((0, 0, 0), (8, 8, 16)), ((8, 0, 0), (16, 16, 16))],
    '_inner': [((8, 0, 0), (16, 16, 16)), ((0, 0, 8), (8, 16, 16)), ((0, 0, 0), (8, 8, 8))],
    '_outer': [((8, 0, 8), (16, 16, 16)), ((0, 0, 0), (16, 8, 8)), ((0, 0, 8), (8, 8, 16))],
}
WALL_BOXES = {'_post': [((4, 0, 4), (12, 16, 12))], '_side': [((5, 0, 0), (11, 14, 8))],
              '_side_tall': [((5, 0, 0), (11, 16, 8))]}
WALL_PARENTS = {'_post': 'template_wall_post', '_side': 'template_wall_side', '_side_tall': 'template_wall_side_tall'}


def flip_x180(boxes):
    """The boxes turned 180 degrees about the x axis through the block's centre: y -> 16 - y, z -> 16 - z."""
    return [((lo[0], 16 - hi[1], 16 - hi[2]), (hi[0], 16 - lo[1], 16 - lo[2])) for lo, hi in boxes]


def rimed_textures(i):
    return {'top': tex(variant(f'{BRICKS}_top', i)), 'side': tex(variant(BRICKS, i)), 'bottom': UNDERSIDE,
            'particle': tex(variant(BRICKS, i))}


def rimed_models():
    models = {}
    for i in range(len(RIME_LAYOUTS)):
        t = rimed_textures(i)
        models[variant(BRICKS, i)] = {'parent': 'minecraft:block/cube_bottom_top',
                                      'textures': {k: t[k] for k in ('top', 'side', 'bottom')}}
        models[variant(SLAB, i)] = {'parent': 'minecraft:block/slab', 'textures': t,
                                    'elements': ledge_elements([((0, 0, 0), (16, 8, 16))])}
        models[variant(f'{SLAB}_top', i)] = {'parent': 'minecraft:block/slab_top', 'textures': t,
                                             'elements': ledge_elements([((0, 8, 0), (16, 16, 16))])}
        for shape, boxes in STAIR_BOXES.items():
            parent = {'': 'stairs', '_inner': 'inner_stairs', '_outer': 'outer_stairs'}[shape]
            models[variant(f'{STAIRS}{shape}', i)] = {'parent': f'minecraft:block/{parent}', 'textures': t,
                                                      'elements': ledge_elements(boxes)}
            models[variant(f'{STAIRS}{shape}_top', i)] = {'parent': f'minecraft:block/{parent}', 'textures': t,
                                                          'elements': ledge_elements(flip_x180(boxes))}
    t = {'top': tex(f'{BRICKS}_top'), 'side': tex(BRICKS), 'bottom': UNDERSIDE, 'particle': tex(BRICKS)}
    for part, boxes in WALL_BOXES.items():
        skip = ('south',) if part != '_post' else ()
        models[f'{WALL}{part}'] = {'parent': f'minecraft:block/{WALL_PARENTS[part]}', 'textures': t,
                                   'elements': ledge_elements(boxes, skip)}
    models[f'{WALL}_inventory'] = {'parent': 'minecraft:block/wall_inventory', 'textures': t,
                                   'elements': ledge_elements([((4, 0, 4), (12, 16, 12)), ((5, 0, 0), (11, 13, 16))])}
    return models


def turned(name, count, turns=TURNS):
    out = []
    for i in range(count):
        for y in turns:
            option = {'model': tex(variant(name, i))}
            if y:
                option['y'] = y
            out.append(option)
    return out


def rimed_stairs_blockstate():
    """Vanilla's stairs table with each layout per state; the top half uses its own models instead of x = 180."""
    variants = {}
    for key, option in stairs_blockstate(STAIRS)['variants'].items():
        model = option['model'].split('/')[-1]
        if option.pop('x', 0):
            model += '_top'
        options = []
        for i in range(len(RIME_LAYOUTS)):
            o = {'model': tex(variant(model, i))}
            if option.get('y'):
                o['y'] = option['y']
                o['uvlock'] = True
            options.append(o)
        variants[key] = options
    return {'variants': variants}


def wall_blockstate():
    parts = [{'when': {'up': 'true'}, 'apply': {'model': tex(f'{WALL}_post')}}]
    for side, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for height, part in (('low', '_side'), ('tall', '_side_tall')):
            apply = {'model': tex(f'{WALL}{part}'), 'uvlock': True}
            if y:
                apply['y'] = y
            parts.append({'when': {side: height}, 'apply': apply})
    return {'multipart': parts}


def rimed_blockstates():
    n = len(RIME_LAYOUTS)
    return {
        BRICKS: {'variants': {'': turned(BRICKS, n)}},
        SLAB: {'variants': {'type=bottom': turned(SLAB, n), 'type=top': turned(f'{SLAB}_top', n),
                            'type=double': turned(BRICKS, n)}},
        STAIRS: rimed_stairs_blockstate(),
        WALL: wall_blockstate(),
    }


# --- ice for the modelled pieces ----------------------------------------------------------------------------------

ICE_DEEP = (96, 140, 228)
# Saturated on purpose: Minecraft's warm block light turns paler ice tan underground.
ICE_BODY = (134, 180, 250)
ICE_LIGHT = (186, 216, 255)
ICE_ALPHA = 205


def ice_painter(frost=0.0, alpha=ICE_ALPHA):
    """Pale ice: a lit left edge, a deep right edge, vertical striations; `frost` whitens it toward rime."""
    def paint(face, lx, ly, w, h, key):
        n = noise('ice', key, face, lx, ly)
        if face in ('up', 'down'):
            rgb = mix(ICE_BODY, ICE_LIGHT, .35 + n * .2)
            a = alpha
        elif w == 1:
            rgb = mix(ICE_BODY, ICE_LIGHT, .55 + n * .2)
            a = alpha + 15
        elif lx == 0:
            rgb, a = ICE_LIGHT, alpha + 25
        elif lx == w - 1:
            rgb, a = ICE_DEEP, alpha + 15
        else:
            stria = noise('stria', key, face, lx)
            rgb = mix(ICE_BODY, ICE_LIGHT if stria > .6 else ICE_DEEP, abs(stria - .5) * .7 + (n - .5) * .15)
            a = alpha - 12 + int(stria * 20)
        if frost:
            rgb = mix(rgb, RIME[4], frost * (.7 + n * .3))
            a = min(255, a + int(frost * 40))
        if n > .975:
            rgb, a = (246, 251, 255), 235
        return rgb + (min(255, a),)
    return paint


def ring_painter(name):
    """A segment's underside with the footprint of the narrower segment below it cut away (no face inside the ice)."""
    _, outer, inner, frost = name.split(':')
    outer, inner, frost = float(outer), float(inner), float(frost)
    base = ice_painter(frost)

    def paint(face, lx, ly, w, h, key):
        u, v = (lx + .5) / w * outer - outer / 2, (ly + .5) / h * outer - outer / 2
        if abs(u) < inner / 2 and abs(v) < inner / 2:
            return (0, 0, 0, 0)
        return base(face, lx, ly, w, h, key)
    return paint


def icicle(name, cx, cz, profile, top=16.0, cap_bottom=True):
    """Stepped square segments hanging from `top`: profile is [(width, height), ...] from the root down.

    The root third is frostier (rime where it meets the stone); every underside but the last is cut away where the
    next segment meets it."""
    out = []
    y = top
    for k, (w, h) in enumerate(profile):
        frost = .35 if k == 0 else .12 if k == 1 else 0.0
        lo, hi = (cx - w / 2, y - h, cz - w / 2), (cx + w / 2, y, cz + w / 2)
        last = k == len(profile) - 1
        faces = {}
        if not last:
            faces['down'] = f'ring:{w}:{profile[k + 1][0]}:{frost}'
        skip = ['up']
        if last and not cap_bottom:
            skip.append('down')
        out.append(box(f'{name}{k}', lo, hi, f'ice:{frost}', skip=skip, **faces))
        y -= h
    return out


def film(name, lo, hi):
    """A thin skin of ice under the ceiling where the icicles' roots join."""
    return box(name, lo, hi, 'ice:0.5', skip=('up',))


# --- the hanging icicle -------------------------------------------------------------------------------------------

ICICLE = 'hanging_icicle'
SHAFT_BASE = [(5, 3), (4, 6), (3, 7)]
TIP_PROFILES = [[(3, 4), (2, 5), (1.5, 3), (1, 2), (.5, 1)], [(3, 3.5), (2, 4.5), (1.5, 3.5), (1, 2.5), (.5, 1)]]


def icicle_short(layout):
    if layout == 0:
        parts = [('a', 5, 6, [(3, 3), (2, 4), (1, 3), (.5, 1.5)]),
                 ('b', 10.5, 9.5, [(2, 2.5), (1.5, 2.5), (1, 2), (.5, 1)]),
                 ('c', 7.5, 11.5, [(2.5, 3), (1.5, 3), (1, 2.5), (.5, 1)]),
                 ('d', 11.5, 4.5, [(1.5, 2), (1, 2), (.5, 1)])]
        films = [((3.5, 15.5, 4.5), (7, 16, 7.5)), ((6, 15.5, 7), (12, 16, 12.5)), ((10.5, 15.5, 3.5), (12.5, 16, 6))]
    else:
        parts = [('a', 9, 7, [(3, 3.5), (2, 4), (1, 3.5), (.5, 1.5)]),
                 ('b', 4.5, 10.5, [(2, 3), (1.5, 3), (1, 2), (.5, 1)]),
                 ('c', 12, 11.5, [(1.5, 2), (1, 2.5), (.5, 1)]),
                 ('d', 5, 4, [(2, 2), (1, 2.5), (.5, 1.5)]),
                 ('e', 12.5, 3.5, [(1, 2), (.5, 1)])]
        films = [((3.5, 15.5, 3), (10.5, 16, 8.5)), ((3.5, 15.5, 8.5), (6, 16, 12)),
                 ((10.5, 15.5, 10.5), (13.5, 16, 12.5))]
    e = [film(f'film{i}', lo, hi) for i, (lo, hi) in enumerate(films)]
    for name, cx, cz, profile in parts:
        e += icicle(name, cx, cz, profile)
    return e


def icicle_base(layout):
    e = [film('film0', (4.5, 15.25, 4.5), (11.5, 16, 11.5))]
    e += icicle('shaft', 8, 8, SHAFT_BASE)
    sides = ([('s1', 3.5, 5, [(2, 2.5), (1, 2), (.5, 1)]), ('s2', 12.5, 11.5, [(1.5, 2), (1, 1.5), (.5, 1)])]
             if layout == 0 else
             [('s1', 4, 11.5, [(2.5, 3), (1.5, 3), (1, 2), (.5, 1)]), ('s2', 12, 4, [(1.5, 2.5), (.5, 1.5)]),
              ('s3', 12.5, 12.5, [(1, 1.5), (.5, 1)])])
    for name, cx, cz, profile in sides:
        e += icicle(name, cx, cz, profile)
    return e


def icicle_tip(layout):
    if layout == 0:
        return icicle('shaft', 8, 8, TIP_PROFILES[0])
    # the second tip leans a half pixel off true as it narrows
    e = icicle('shaft', 8, 8, TIP_PROFILES[1][:2])
    e += icicle('lean', 8.5, 8, TIP_PROFILES[1][2:], top=16 - 8)
    return e


ICICLE_SHAPES = {'short': icicle_short, 'base': icicle_base, 'tip': icicle_tip}


# --- the frozen drip ----------------------------------------------------------------------------------------------

DRIP = 'frozen_drip'
WALL_Z = 16
# Ribbons at the cell edges, shared by every layout: (x0, x1, proud). Sheets meet sheets on these.
RIBBONS = [(3.5, 5.5, 2), (9, 10.5, 3), (12.5, 13.5, 1.5)]
FILM_EDGE = (2.5, 13.5)                  # the film's sides where it leaves the cell, top and bottom
FILM = .75                               # how far the film stands off the stone
ROW = 4                                  # the sheet is built in 4 px rows; the top and bottom rows meet the edges
FLUSH = ('south', 'up', 'down')          # rows of film and ribbon show no seam where they meet the next row
# Per layout: the film's sides in each row (bottom row first) and each ribbon's sideways wander per row.
SHEET_LAYOUTS = [
    {'film': [FILM_EDGE, (2, 13), (3, 14), FILM_EDGE], 'wander': [(0, .5, .5, 0), (0, -.5, 0, 0), (0, 0, .5, 0)],
     'bulges': [(1, 5, 9, 3.25, .5), (0, 10, 13, 2.75, .5), (2, 2, 4, 2.25, .5)]},
    {'film': [FILM_EDGE, (3, 12.5), (2, 13), FILM_EDGE], 'wander': [(0, -.5, 0, 0), (0, .5, .5, 0), (0, .5, 0, 0)],
     'bulges': [(1, 2, 5, 3.75, .5), (1, 11, 13, 3.5, .25), (0, 6, 8, 2.75, .5), (2, 9, 11, 2.25, .5)]},
]


def film_rows(rows, bottom=0.0):
    """The film in rows of its own width; each row is (x0, x1) or a list of pieces."""
    e = []
    for k, pieces in enumerate(rows):
        for j, (x0, x1) in enumerate(pieces if isinstance(pieces, list) else [pieces]):
            y0 = bottom + k * ROW
            e.append(box(f'film{k}_{j}', (x0, y0, WALL_Z - FILM), (x1, y0 + ROW, WALL_Z), 'sheet', skip=FLUSH))
    return e


def ribbon(name, x0, x1, proud, wander, rows):
    """A ribbon down the sheet: one box per 4 px row, nudged sideways by `wander`, rows bottom first."""
    return [box(f'{name}_{k}', (x0 + wander[k], k * ROW, WALL_Z - proud), (x1 + wander[k], (k + 1) * ROW,
                WALL_Z - FILM), 'ice:0.0', skip=FLUSH) for k in rows]


def bulges(layout, wander):
    e = []
    for i, (r, y0, y1, proud, grow) in enumerate(layout['bulges']):
        x0, x1, _ = RIBBONS[r]
        dx = wander[r][min(3, int(y0 // ROW))]
        e.append(box(f'bulge{i}', (x0 + dx - grow, y0, WALL_Z - proud), (x1 + dx + grow, y1, WALL_Z - FILM),
                     'ice:0.1', skip=('south',)))
    return e


def drip_sheet(layout):
    spec = SHEET_LAYOUTS[layout]
    e = film_rows(spec['film'])
    for i, (x0, x1, proud) in enumerate(RIBBONS):
        e += ribbon(f'ribbon{i}', x0, x1, proud, spec['wander'][i], range(4))
    return e + bulges(spec, spec['wander'])


# Per layout: the film's rows from y 4 up (it gives out raggedly), and where each ribbon stops and how it ends.
END_LAYOUTS = [
    {'film': [[(3.5, 6.5)], [(3, 8), (9.5, 12)], [(2.5, 12.5)]],
     'wander': [(0, 0, .5, 0), (0, 0, -.5, 0), (0, 0, 0, 0)],
     'ribbon1': 9, 'bulges': [(1, 9, 11, 3.5, .5)]},
    {'film': [[(8, 11)], [(3, 6), (6.5, 12.5)], [(2.5, 13)]], 'wander': [(0, .5, 0, 0), (0, 0, .5, 0), (0, 0, 0, 0)],
     'ribbon1': 10.5, 'bulges': [(0, 12, 14, 2.75, .5)]},
]


def drip_end(layout):
    """The film gives out unevenly and each ribbon tapers into beads or a short icicle."""
    spec = END_LAYOUTS[layout]
    e = film_rows(spec['film'], bottom=ROW)
    (a0, a1, ap), (b0, b1, bp), (c0, c1, cp) = RIBBONS
    wa, wb, wc = spec['wander']
    # ribbon 0: runs to y 5, then two beads, each smaller
    e += ribbon('ribbon0', a0, a1, ap, wa, range(2, 4))
    dx = wa[2]
    e.append(box('ribbon0_low', (a0 + dx, 5, WALL_Z - ap + .25), (a1 + dx, 8, WALL_Z - FILM), 'ice:0.0',
                 skip=('south',)))
    e.append(box('bead0', (a0 + dx + .25, 3, WALL_Z - ap + .25), (a1 + dx - .25, 5, WALL_Z - .5), 'ice:0.0',
                 skip=('south',)))
    e.append(box('bead0b', (a0 + dx + .75, 1.75, WALL_Z - 1.25), (a1 + dx - .75, 3, WALL_Z - .5), 'ice:0.0',
                 skip=('south',)))
    # ribbon 1: the thickest, tapers into a short icicle standing just off the wall
    stop = spec['ribbon1']
    e += ribbon('ribbon1', b0, b1, bp, wb, range(3, 4))
    dx = wb[2]
    e.append(box('ribbon1_low', (b0 + dx, stop, WALL_Z - bp + .25), (b1 + dx, 12, WALL_Z - FILM), 'ice:0.0',
                 skip=('south',)))
    e += icicle('drip1', (b0 + b1) / 2 + dx, WALL_Z - 1.75, [(1.5, 3), (1, 3), (.5, 1.5)], top=stop)
    # ribbon 2: a thin one that stops short with a single bead
    e += ribbon('ribbon2', c0, c1, cp, wc, range(3, 4))
    e.append(box('bead2', (c0 - .25, 10.5, WALL_Z - cp - .25), (c1 + .25, 12, WALL_Z - .5), 'ice:0.0',
                 skip=('south',)))
    return e + bulges(spec, spec['wander'])


def sheet_painter(face, lx, ly, w, h, key):
    """The film: thin clear ice with vertical flow lines, a little frost where it meets the stone."""
    n = noise('sheet', key, face, lx, ly)
    flow = noise('flow', key, face, lx)
    rgb = mix(ICE_BODY, ICE_LIGHT, .3 + flow * .4 + (n - .5) * .1)
    a = 104 + int(flow * 44)
    if face != 'north':
        rgb, a = ICE_LIGHT, 190
    if n > .98:
        rgb, a = (246, 251, 255), 220
    return rgb + (a,)


DRIP_SHAPES = {'false': drip_sheet, 'true': drip_end}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


# --- modelled pieces: atlas, models, blockstates ------------------------------------------------------------------

def special_for(groups):
    special = {'sheet': sheet_painter}
    for elements in groups.values():
        for el in elements:
            for mat in [el['mat']] + list(el['faces'].values()):
                if mat.startswith('ice:'):
                    special[mat] = ice_painter(float(mat.split(':')[1]))
                elif mat.startswith('ring:'):
                    special[mat] = ring_painter(mat)
    return special


def export_modelled(name, shapes, layouts=2):
    groups = {f'{key}_{i}': shape(i) for key, shape in shapes.items() for i in range(layouts)}
    moved = sum(separate(elements) for elements in groups.values())
    atlas, models = build(groups, special_for(groups), atlas_w=64)
    atlas.save(TEXTURES / f'{name}.png')
    outputs = {}
    for group, elements in models.items():
        model = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:translucent',
                 'ambientocclusion': False, 'texture_size': [64, atlas.height],
                 'textures': {'atlas': tex(name), 'particle': tex(name)}, 'elements': elements}
        outputs[f'block/{name}_{group}'] = model
    first = next(iter(outputs.values()))
    write(outputs, name, first)
    extents = {}
    for group, elements in models.items():
        lo = [min(min(e['from'][a], e['to'][a]) for e in elements) for a in range(3)]
        hi = [max(max(e['from'][a], e['to'][a]) for e in elements) for a in range(3)]
        extents[group] = (lo, hi)
    print(f'wrote {name}: {len(groups)} models, {moved} faces separated, 64x{atlas.height} atlas')
    for group, (lo, hi) in extents.items():
        print(f'    {group}: extent {lo} .. {hi}')
    return models


def icicle_blockstate():
    return {'variants': {f'length={key}': [dict({'model': tex(f'{ICICLE}_{key}_{i}')}, **({'y': y} if y else {}))
                                           for i in range(2) for y in TURNS] for key in ICICLE_SHAPES}}


def drip_blockstate():
    variants = {}
    for end in ('false', 'true'):
        for facing, y in FACING_Y.items():
            options = []
            for i in range(2):
                option = {'model': tex(f'{DRIP}_{"end" if end == "true" else "sheet"}_{i}')}
                if y:
                    option['y'] = y
                options.append(option)
            variants[f'end={end},facing={facing}'] = options
    return {'variants': variants}


# --- writing ------------------------------------------------------------------------------------------------------

def export_textures():
    images = {}
    for i, (seed, ticks) in enumerate(RIME_LAYOUTS):
        stone = deepslate_bricks(131 + i)
        images[variant(BRICKS, i)] = rime_side(seed, stone, ticks)
        images[variant(f'{BRICKS}_top', i)] = rime_top(seed, stone)
    images[LANTERN] = lantern_shell()
    images[f'{LANTERN}_core'] = lantern_core()
    images['dark_lacquer_planks'] = dark_lacquer_planks()
    for name, image in images.items():
        image.save(TEXTURES / f'{name}.png')
    return images


def export_blocks():
    models = rimed_models()
    states = rimed_blockstates()
    items = {BRICKS: BRICKS, STAIRS: STAIRS, SLAB: SLAB, WALL: f'{WALL}_inventory'}
    models[LANTERN] = lantern_model()
    states[LANTERN] = {'variants': {'': {'model': tex(LANTERN)}}}
    items[LANTERN] = LANTERN
    planks = 'dark_lacquer_planks'
    t = {'bottom': tex(planks), 'top': tex(planks), 'side': tex(planks)}
    models[planks] = cube_all(planks)
    states[planks] = {'variants': {'': {'model': tex(planks)}}}
    items[planks] = planks
    for suffix, parent in (('', 'stairs'), ('_inner', 'inner_stairs'), ('_outer', 'outer_stairs')):
        models[f'{planks}_stairs{suffix}'] = {'parent': f'minecraft:block/{parent}', 'textures': t}
    states[f'{planks}_stairs'] = stairs_blockstate(f'{planks}_stairs')
    items[f'{planks}_stairs'] = f'{planks}_stairs'
    models[f'{planks}_slab'] = {'parent': 'minecraft:block/slab', 'textures': t}
    models[f'{planks}_slab_top'] = {'parent': 'minecraft:block/slab_top', 'textures': t}
    states[f'{planks}_slab'] = {'variants': {'type=bottom': {'model': tex(f'{planks}_slab')},
                                             'type=double': {'model': tex(planks)},
                                             'type=top': {'model': tex(f'{planks}_slab_top')}}}
    items[f'{planks}_slab'] = f'{planks}_slab'
    for name, model in models.items():
        write_json(ASSETS / f'models/block/{name}.json', model)
    for name, state in states.items():
        write_json(ASSETS / f'blockstates/{name}.json', state)
    for name, parent in items.items():
        write_json(ASSETS / f'models/item/{name}.json', {'parent': tex(parent)})
    print(f'wrote {len(models)} block models, {len(states)} blockstates')


def export_icicles():
    models = export_modelled(ICICLE, ICICLE_SHAPES)
    write_json(ASSETS / f'blockstates/{ICICLE}.json', icicle_blockstate())
    write_json(ASSETS / f'models/item/{ICICLE}.json', {
        'parent': tex(f'{ICICLE}_short_0'),
        'display': {'gui': {'rotation': [30, 225, 0], 'translation': [0, -1.5, 0], 'scale': [.7, .7, .7]}}})
    return models


def export_drips():
    models = export_modelled(DRIP, {'sheet': drip_sheet, 'end': drip_end})
    write_json(ASSETS / f'blockstates/{DRIP}.json', drip_blockstate())
    write_json(ASSETS / f'models/item/{DRIP}.json', {
        'parent': tex(f'{DRIP}_end_0'),
        'display': {'gui': {'rotation': [30, 160, 0], 'translation': [0, 0, 0], 'scale': [.7, .7, .7]}}})
    return models


# --- preview ------------------------------------------------------------------------------------------------------

def render_blend(model, textures, yaw, pitch, size, scale, bg=(30, 24, 28), lift=0.0):
    """A painter's renderer for translucent models: faces sorted far to near and alpha-blended."""
    canvas = np.zeros((size, size, 3), dtype=float)
    canvas[:] = bg
    a, p = math.radians(yaw), math.radians(pitch)
    eye = np.array([math.sin(a) * math.cos(p), math.sin(p), -math.cos(a) * math.cos(p)])
    right = np.cross([0, 1, 0], eye)
    right /= np.linalg.norm(right)
    up = np.cross(eye, right)
    corners = {'north': [0, 1, 3, 2], 'south': [5, 4, 6, 7], 'west': [4, 0, 2, 6], 'east': [1, 5, 7, 3],
               'up': [2, 3, 7, 6], 'down': [4, 5, 1, 0]}
    normals = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0), 'up': (0, 1, 0),
               'down': (0, -1, 0)}
    shades = {'north': .8, 'south': .8, 'west': .6, 'east': .6, 'up': 1, 'down': .5}
    quads = []
    for e in model:
        lo, hi = e['from'], e['to']
        pts = np.array([[x, y - lift, z] for z in (lo[2], hi[2]) for y in (lo[1], hi[1]) for x in (lo[0], hi[0])],
                       dtype=float)
        bright = e.get('shade') is False
        for face, f in e['faces'].items():
            if np.dot(normals[face], eye) <= 0:
                continue
            c = pts[corners[face]] - [8, 8, 8]
            proj = np.stack([size / 2 + c @ right * scale, size / 2 - c @ up * scale], axis=1)
            depth = float((c @ eye).mean())
            quads.append((depth, proj, f, 1.0 if bright else shades[face]))
    quads.sort(key=lambda q: q[0])
    for depth, q, f, shade in quads:
        texture = textures[f['texture'][1:]]
        u0, v0, u1, v1 = f['uv']
        uv = np.array([[u0, v1], [u1, v1], [u1, v0], [u0, v0]]) / 16
        for ids in ([0, 1, 2], [0, 2, 3]):
            t, tuv = q[ids], uv[ids]
            xmin, ymin = np.maximum(np.floor(t.min(0)).astype(int), 0)
            xmax, ymax = np.minimum(np.ceil(t.max(0)).astype(int), size - 1)
            if xmax < xmin or ymax < ymin:
                continue
            xx, yy = np.meshgrid(np.arange(xmin, xmax + 1) + .5, np.arange(ymin, ymax + 1) + .5)
            den = (t[1, 1] - t[2, 1]) * (t[0, 0] - t[2, 0]) + (t[2, 0] - t[1, 0]) * (t[0, 1] - t[2, 1])
            if abs(den) < 1e-8:
                continue
            b0 = ((t[1, 1] - t[2, 1]) * (xx - t[2, 0]) + (t[2, 0] - t[1, 0]) * (yy - t[2, 1])) / den
            b1 = ((t[2, 1] - t[0, 1]) * (xx - t[2, 0]) + (t[0, 0] - t[2, 0]) * (yy - t[2, 1])) / den
            b2 = 1 - b0 - b1
            u = b0 * tuv[0, 0] + b1 * tuv[1, 0] + b2 * tuv[2, 0]
            v = b0 * tuv[0, 1] + b1 * tuv[1, 1] + b2 * tuv[2, 1]
            px = texture[np.clip((v * texture.shape[0]).astype(int), 0, texture.shape[0] - 1),
                         np.clip((u * texture.shape[1]).astype(int), 0, texture.shape[1] - 1)].astype(float)
            mask = (b0 >= -1e-6) & (b1 >= -1e-6) & (b2 >= -1e-6)
            alpha = (px[:, :, 3:] / 255) * mask[:, :, None]
            region = canvas[ymin:ymax + 1, xmin:xmax + 1]
            region[:] = region * (1 - alpha) + px[:, :, :3] * shade * alpha
    return Image.fromarray(canvas.clip(0, 255).astype('uint8'), 'RGB').convert('RGBA')


def load(name):
    return np.array(Image.open(TEXTURES / f'{name}.png').convert('RGBA'))


def turnaround(models, atlas, title, out_name, groups, lift=0.0):
    views = [(0, 10), (35, 25), (90, 10), (-60, -30)]
    size = 300
    texture = {'atlas': load(atlas)}
    sheet = Image.new('RGBA', (size * len(views), size * len(groups) + 30), (30, 24, 28, 255))
    for r, group in enumerate(groups):
        for i, (yaw, pitch) in enumerate(views):
            tile = render_blend(models[group], texture, yaw, pitch, size, 15, lift=lift)
            sheet.paste(tile, (size * i, 30 + size * r))
            ImageDraw.Draw(sheet).text((size * i + 6, 34 + size * r), group, fill=(220, 210, 220))
    ImageDraw.Draw(sheet).text((10, 8), title, fill=(220, 200, 200))
    out = ROOT / f'build/model-previews/{out_name}.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(out)
    print(out)


def translate(elements, dx=0.0, dy=0.0, dz=0.0):
    out = copy.deepcopy(elements)
    for e in out:
        e['from'] = [e['from'][0] + dx, e['from'][1] + dy, e['from'][2] + dz]
        e['to'] = [e['to'][0] + dx, e['to'][1] + dy, e['to'][2] + dz]
    return out


def rotate_y(elements, quarter):
    """A model turned by quarter * 90 degrees about the block's vertical centre line (the blockstate's y)."""
    out = copy.deepcopy(elements)
    for _ in range(quarter % 4):
        turned = []
        for e in out:
            (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
            faces = {{'north': 'east', 'east': 'south', 'south': 'west', 'west': 'north'}.get(k, k): v
                     for k, v in e['faces'].items()}
            turned.append(dict(e, **{'from': [16 - z1, y0, x0], 'to': [16 - z0, y1, x1], 'faces': faces}))
        out = turned
    return out


def cube_elements(texture_key):
    return [{'from': [0, 0, 0], 'to': [16, 16, 16],
             'faces': {f: {'uv': [0, 0, 16, 16], 'texture': '#' + texture_key} for f in FACES}}]


def icicle_row(models, size=(900, 300), scale=6):
    """A vault ceiling at game scale: deepslate overhead, a row of icicles and a two-cell icicle under it."""
    textures = {'atlas': load(ICICLE), 'stone': np.array(vanilla('deepslate_bricks'))}
    picks = [('short_0', 0), ('short_1', 1), ('base_0', 2), ('short_1', 3), ('base_1', 1), ('short_0', 2),
             ('short_0', 1)]
    scene = []
    for i, (group, turn) in enumerate(picks):
        x = (i - 3) * 16
        scene += translate(cube_elements('stone'), dx=x, dy=16)
        scene += translate(rotate_y(models[group], turn), dx=x)
        if group.startswith('base'):
            scene += translate(rotate_y(models[f'tip_{i % 2}'], turn), dx=x, dy=-16)
    tile = render_blend(scene, textures, 20, -18, size[0], scale, bg=(18, 20, 28), lift=0)
    return tile


def drip_column(models, size=(600, 420), scale=7):
    textures = {'atlas': load(DRIP), 'stone': np.array(vanilla('deepslate_bricks'))}
    scene = []
    for col, layouts in enumerate([('sheet_0', 'sheet_1', 'end_0'), ('sheet_1', 'sheet_0', 'end_1')]):
        x = (col - .5) * 24
        for k, group in enumerate(layouts):
            y = (1 - k) * 16
            scene += translate(cube_elements('stone'), dx=x, dy=y, dz=16)
            scene += translate(models[group], dx=x, dy=y)
    return render_blend(scene, textures, 25, 12, size[0], scale, bg=(18, 20, 28))


def composite_lantern(shell, core, dark=1.0):
    """The lantern's face as seen: the core (full-bright) through the shell, the shell lit by `dark`."""
    out = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            s = shell.getpixel((x, y))
            inside = CORE_INSET <= x < 16 - CORE_INSET and CORE_INSET <= y < 16 - CORE_INSET
            behind = core.getpixel((x, y))[:3] if inside else tuple(round(c * dark) for c in (20, 24, 34))
            a = s[3] / 255
            rgb = tuple(round(behind[i] * (1 - a) + s[i] * dark * a) for i in range(3))
            out.putpixel((x, y), rgb + (255,))
    return out


def cornice(images, scale=4):
    """A deepslate wall with a rimed cornice course, a slab ledge and a dado of rimed bricks."""
    t = 16 * scale
    cols = 8
    plain = vanilla('deepslate_bricks')
    tiles = vanilla('deepslate_tiles')
    stack = ['air', 'rimed', 'plain', 'plain', 'rimed', 'plain', 'tiles']
    sheet = Image.new('RGBA', (cols * t, len(stack) * t), (18, 20, 28, 255))
    for r, kind in enumerate(stack):
        for c in range(cols):
            if kind == 'air':
                continue
            if kind == 'rimed':
                image = images[variant(BRICKS, int(noise('cornice', r, c) * 3))]
            else:
                image = tiles if kind == 'tiles' else plain
            sheet.paste(image.resize((t, t), Image.NEAREST), (c * t, r * t))
    return sheet


def preview_blocks(images):
    gap, bg = 12, (36, 32, 34, 255)
    sw = 16 * 6
    names = ['deepslate_bricks'] + [variant(BRICKS, i) for i in range(3)] + [variant(f'{BRICKS}_top', i)
                                                                              for i in range(3)]
    names += ['snow', 'packed_ice', 'sea_lantern', 'dark_oak_planks', 'dark_lacquer_planks']
    swatches = row([(images.get(n) or vanilla(n)).resize((sw, sw), Image.NEAREST) for n in names], gap, bg)
    lit = composite_lantern(images[LANTERN], images[f'{LANTERN}_core'])
    dim = composite_lantern(images[LANTERN], images[f'{LANTERN}_core'], .45)
    raw = [images[LANTERN], images[f'{LANTERN}_core']]
    lanterns = row([x.resize((sw * 2, sw * 2), Image.NEAREST) for x in [lit, dim] + raw], gap, bg)
    tops = [images[variant(f'{BRICKS}_top', i)] for i in range(3)]
    floor = Image.new('RGBA', (5 * 80, 5 * 80))
    for r in range(5):
        for c in range(5):
            image = tops[int(noise('floor', r, c) * 3)].rotate(90 * int(noise('turn', r, c) * 4))
            floor.paste(image.resize((80, 80), Image.NEAREST), (c * 80, r * 80))
    lac = images['dark_lacquer_planks']
    panel = Image.new('RGBA', (4 * 80, 4 * 80))
    for r in range(4):
        for c in range(4):
            panel.paste(lac.resize((80, 80), Image.NEAREST), (c * 80, r * 80))
    walls = row([cornice(images), floor, panel], gap, bg)
    s, tp = images[BRICKS], images[f'{BRICKS}_top']
    isos = row([iso(tp, s, s), iso(tp, s, s, shape='stairs'), iso(tp, s, s, shape='slab'),
                iso(lit, lit, lit), iso(dim, dim, dim), iso(lac, lac, lac), iso(lac, lac, lac, shape='stairs')],
               gap, bg)
    parts = [swatches, lanterns, walls, isos]
    sheet = Image.new('RGBA', (max(x.width for x in parts) + 2 * gap, sum(x.height for x in parts) + 5 * gap), bg)
    y = gap
    for part in parts:
        sheet.paste(part, (gap, y), part)
        y += part.height + gap
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW)
    print('wrote', PREVIEW)


def preview_ledges():
    """The rimed stairs, slab and wall models drawn from their own elements and textures."""
    names = [variant(BRICKS, 0), variant(f'{BRICKS}_top', 0)]
    textures = {'side': load(names[0]), 'top': load(names[1]), 'bottom': np.array(vanilla('deepslate_bricks'))}
    models = rimed_models()
    shown = [(f'{STAIRS}', 0), (f'{STAIRS}_inner', 0), (f'{STAIRS}_outer', 0), (f'{STAIRS}_top', 0),
             (f'{SLAB}', 0), (f'{WALL}_inventory', 0)]
    size = 260
    sheet = Image.new('RGBA', (size * len(shown), size * 2 + 30), (30, 24, 28, 255))
    for i, (name, _) in enumerate(shown):
        elements = models[name]['elements']
        for r, (yaw, pitch) in enumerate([(35, 28), (-145, 28)]):
            tile = render_blend(elements, textures, yaw, pitch, size, 11)
            sheet.paste(tile, (size * i, 30 + size * r))
        ImageDraw.Draw(sheet).text((size * i + 6, 34), name, fill=(220, 210, 220))
    ImageDraw.Draw(sheet).text((10, 8), 'Rimed ledges - fringe on every exposed top edge', fill=(220, 200, 200))
    out = ROOT / 'build/model-previews/rimed_ledges.png'
    sheet.convert('RGB').save(out)
    print(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    images = export_textures()
    export_blocks()
    icicles = export_icicles()
    drips = export_drips()
    if args.preview:
        preview_blocks(images)
        preview_ledges()
        turnaround(icicles, ICICLE, 'Hanging Icicle - short, base and tip layouts (blended)', ICICLE,
                   list(icicles))
        turnaround(drips, DRIP, 'Frozen Drip - facing north (wall behind), sheet and end layouts', DRIP, list(drips))
        icicle_row(icicles).save(ROOT / 'build/model-previews/hanging_icicle_row.png')
        drip_column(drips).save(ROOT / 'build/model-previews/frozen_drip_column.png')
        print('wrote hanging_icicle_row.png, frozen_drip_column.png')


if __name__ == '__main__':
    main()

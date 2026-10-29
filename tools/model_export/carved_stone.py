"""Carved venous stone for the mausoleum set pieces (witness_stone.py, litany_stone.py).

The set pieces stand in four mausoleum themes (venous stone, quartz, mud brick, packed ice), so they
carry their own material: a deep grey-red stone run through with darker veins and the odd crimson
thread, worn pale on its arrises and grimed at the foot. Veins are painted from each texel's block
position, not its packed-face position, so a vein crosses from one face onto the next.

Register the element groups (and each group's storey offset) with `register` before `build`; the
painters read the elements back from the packer's (group, index) key.
"""
import math

from clairaudiograph import clamp, noise

STONE = (78, 62, 64)
VEIN = (54, 38, 42)
VEIN_RED = (104, 30, 40)
GRIME = (52, 42, 44)
THREAD = (138, 118, 112)   # a pale mycelial thread in the foot's grime

ELEMENTS = {}
Y_OFFSET = {}


def register(groups, offsets=None):
    ELEMENTS.clear()
    Y_OFFSET.clear()
    ELEMENTS.update(groups)
    Y_OFFSET.update(offsets or {})


def element(key):
    return ELEMENTS[key[0]][key[1]]


def world(key, face, lx, ly):
    """Block-space centre of one packed texel, by vanilla's face UV convention (u0 at the viewer's left)."""
    el = element(key)
    (x0, y0, z0), (x1, y1, z1) = el['lo'], el['hi']
    u, v = (lx + .5) / el['density'], (ly + .5) / el['density']
    dy = Y_OFFSET.get(key[0], 0)
    p = {'north': (x1 - u, y1 - v, z0), 'south': (x0 + u, y1 - v, z1), 'east': (x1, y1 - v, z1 - u),
         'west': (x0, y1 - v, z0 + u), 'up': (x0 + u, y1, z0 + v), 'down': (x0 + u, y0, z1 - v)}[face]
    return p[0], p[1] + dy, p[2]


def value_noise(x, y, z, scale, salt):
    x, y, z = x / scale, y / scale, z / scale
    ix, iy, iz = math.floor(x), math.floor(y), math.floor(z)
    fx, fy, fz = (t - math.floor(t) for t in (x, y, z))
    fx, fy, fz = (t * t * (3 - 2 * t) for t in (fx, fy, fz))
    total = 0
    for dx in (0, 1):
        for dy in (0, 1):
            for dz in (0, 1):
                w = (fx if dx else 1 - fx) * (fy if dy else 1 - fy) * (fz if dz else 1 - fz)
                total += w * noise(salt, ix + dx, iy + dy, iz + dz)
    return total


def veins(p):
    """0 = clear stone, 1 = a dark vein, 2 = a crimson thread."""
    x, y, z = p
    ridge = abs(value_noise(x, y * .7, z, 7, 'vein') * .75 + value_noise(x, y, z, 2, 'vein2') * .25 - .5)
    if ridge < .018:
        return 1
    red = abs(value_noise(x + 31, y, z - 17, 5, 'red') - .5)
    if red < .009 and value_noise(x, y, z, 7, 'redmask') > .6:
        return 2
    return 0


def stone_rgb(key, face, lx, ly, w, h, tone=0, vein=True, mottle=22):
    """Weathered venous stone at one texel; `tone` shifts the whole face (carved highlights, recesses)."""
    p = world(key, face, lx, ly)
    n = noise('stone', key, face, lx, ly)
    shift = tone + (value_noise(*p, 4, 'mottle') - .5) * mottle + n * 8 - 4
    shift += {'up': 8, 'down': -18}.get(face, 0)
    if face not in ('up', 'down') and w >= 3 and h >= 3:
        if ly == 0:
            shift += 9   # worn arris along the top edge
        elif lx in (0, w - 1) and noise('chip', key, face, ly // 2) < .2:
            shift -= 12  # chipped side arris
    if p[1] < 1.5 and face != 'up':
        rgb = clamp(GRIME, shift)
        if noise('thread', key, face, lx, ly // 2) < .05:
            rgb = THREAD
        return rgb
    if vein:
        kind = veins(p)
        if kind == 1:
            return clamp(VEIN, int(n * 6))
        if kind == 2:
            return VEIN_RED
    return clamp(STONE, shift)


def stone(face, lx, ly, w, h, key):
    return stone_rgb(key, face, lx, ly, w, h) + (255,)


def dressed(face, lx, ly, w, h, key):
    """Smoother dressed stone: lighter mottling, fewer veins, for carved faces that carry detail."""
    return stone_rgb(key, face, lx, ly, w, h, tone=6, mottle=12) + (255,)


def lid(face, lx, ly, w, h, key):
    """Carved relief: a worn, paler crown on top, the underside in shadow."""
    tone = {'up': 14, 'down': -26}.get(face, 4 if ly == 0 else -4)
    return stone_rgb(key, face, lx, ly, w, h, tone=tone, vein=False, mottle=10) + (255,)


SPECIAL = dict(stone=stone, dressed=dressed, lid=lid)

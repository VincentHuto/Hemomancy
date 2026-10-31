"""Shared materials for the Brined Court's set-piece blocks (Track 7, the two Seats).

The Court's light is biological: Sunburst crust and blooms at the Lighthouse Garden, polyp glow and mineral heat at
the Vent Sanctum. No copper, brass or bronze anywhere (those are Unstained metals); worked parts are bone, stone,
lacquer and grown tissue. Every painter here has the fixture_atlas signature (face, lx, ly, w, h, key) -> RGBA and is
merged into a generator's SPECIAL table; `BRIGHT` marks an element full-bright.

Used by sunburst_lamp.py, sunburst_frond.py, petrified_follower.py, hydra_crown.py, vent_offering_basin.py and
court_cubes.py. 16 px per block throughout.
"""
import json

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS

BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}
HALF_BRIGHT = {'neoforge_data': {'block_light': 11, 'sky_light': 15}}

SUN = (230, 148, 42)
SUN_HI = (252, 204, 96)
SUN_LO = (186, 92, 30)
SUN_CORE = (255, 236, 168)
FROND = (204, 136, 52)
FROND_LO = (150, 92, 40)
FROND_VEIN = (246, 196, 104)
MOSS = (84, 112, 50)
MOSS_HI = (112, 142, 62)
MOSS_LO = (56, 78, 36)
STONE = (112, 116, 104)
STONE_LO = (84, 88, 80)
SKIN = (150, 142, 124)
POLYP = (226, 210, 194)
POLYP_LO = (196, 168, 156)
AMBER = (240, 168, 58)
FLAME = (232, 96, 36)
CRIMSON = (176, 30, 38)
BLACKSTONE = (44, 38, 44)
BLACKSTONE_HI = (66, 58, 64)
EMBER = (255, 126, 44)
EMBER_HI = (255, 200, 110)


def sun(face, lx, ly, w, h, key):
    """Sunburst crust: gold-orange lichen in rosettes, pale centres where it has grown thick."""
    n = noise('sun', key, face, lx, ly)
    rosette = noise('rosette', key, face, lx // 2, ly // 2)
    if rosette > .82:
        rgb = SUN_HI if n > .4 else SUN
    elif n < .16:
        rgb = SUN_LO
    else:
        rgb = clamp(SUN, int(n * 18) - 9)
    return rgb + (255,)


def sun_core(face, lx, ly, w, h, key):
    n = noise('core', key, face, lx, ly)
    return (SUN_CORE if n > .3 else SUN_HI) + (255,)


def frond(face, lx, ly, w, h, key):
    """A broad frond: a pale midrib down its length, darker toward its base, gold at the tip."""
    long_axis = ly if h >= w else lx
    span = h if h >= w else w
    across = lx if h >= w else ly
    width = w if h >= w else h
    if width >= 3 and across == width // 2:
        return FROND_VEIN + (255,)
    t = long_axis / max(span - 1, 1)
    base = tuple(int(a + (b - a) * t) for a, b in zip(SUN_HI, FROND_LO))
    n = noise('frond', key, face, lx, ly)
    rgb = clamp(base, int(n * 16) - 8)
    if width >= 3 and across in (0, width - 1) and n > .55:
        return (0, 0, 0, 0)                  # a ragged edge
    return rgb + (255,)


def moss(face, lx, ly, w, h, key):
    n = noise('moss', key, face, lx, ly)
    clump = noise('clump', key, face, lx // 2, ly // 2)
    rgb = MOSS_HI if clump > .7 else MOSS_LO if clump < .2 else MOSS
    return clamp(rgb, int(n * 14) - 7) + (255,)


def mossstone(face, lx, ly, w, h, key):
    """True petrification: grey-green stone that was a person, moss rooted in its creases."""
    n = noise('mstone', key, face, lx, ly)
    if noise('creep', key, face, lx // 2, ly) < .22:
        return moss(face, lx, ly, w, h, key)
    rgb = STONE_LO if n < .2 else clamp(STONE, int(n * 14) - 7)
    if ly == h - 1 and h > 2:
        rgb = clamp(rgb, -12)
    return rgb + (255,)


def skin(face, lx, ly, w, h, key):
    n = noise('skin', key, face, lx, ly)
    return clamp(SKIN, int(n * 12) - 6) + (255,)


def polyp(face, lx, ly, w, h, key):
    n = noise('polyp', key, face, lx, ly)
    rgb = POLYP_LO if n < .25 else clamp(POLYP, int(n * 10) - 5)
    return rgb + (255,)


def blackstone(face, lx, ly, w, h, key):
    """Carved basalt-blackstone: tool marks across the face, a lighter worn arris on top."""
    n = noise('bstone', key, face, lx, ly)
    rgb = clamp(BLACKSTONE, int(n * 12) - 6)
    if ly == 0 and face not in ('up', 'down') and h > 1:
        rgb = BLACKSTONE_HI
    elif (lx + ly * 2) % 5 == 0 and n > .5:
        rgb = clamp(BLACKSTONE, -10)
    return rgb + (255,)


def ember(face, lx, ly, w, h, key):
    """Mineral heat: a bed of glowing vent salts, nothing burning."""
    n = noise('ember', key, face, lx, ly)
    return (EMBER_HI if n > .7 else EMBER if n > .2 else FLAME) + (255,)


def flame_gradient(t, n):
    """Polyp-pale at the root, amber, flame orange, crimson at the tip (t = 0 root .. 1 tip)."""
    stops = [POLYP, AMBER, FLAME, CRIMSON]
    t = max(0.0, min(1.0, t)) * (len(stops) - 1)
    i = min(int(t), len(stops) - 2)
    f = t - i
    rgb = tuple(int(a + (b - a) * f) for a, b in zip(stops[i], stops[i + 1]))
    return clamp(rgb, int(n * 14) - 7)


SPECIAL = dict(sun=sun, sun_core=sun_core, frond=frond, moss=moss, mossstone=mossstone, skin=skin, polyp=polyp,
               blackstone=blackstone, ember=ember)


def write_blockstate(name, variants):
    (ASSETS / f'blockstates/{name}.json').write_text(json.dumps({'variants': variants}, indent=2) + '\n')


def facing_variants(model, extra=''):
    """facing=north..west turned about y; `extra` is a property prefix like 'moss=whole,'."""
    out = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        out[f'{extra}facing={facing}'] = dict({'model': model}, **({'y': y} if y else {}))
    return out

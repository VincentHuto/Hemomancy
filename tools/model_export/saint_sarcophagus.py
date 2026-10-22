"""Author the Saint Sarcophagus: an entombed Saint's chest tomb on a venous-stone dais.

The tomb chest is polished venous stone, strapped at its corners in hematic iron and cut along both
flanks into arcaded panels carved with vein-trees. Its head end bears a witnessing eye, its foot end an
inscribed bone plaque with the slot the offering gutter drains into. Inside, the preserved corpus lies
in a stained linen shroud on a crimson pall, a crimson stole down its chest and its desiccated hands
clasping a sealed vial; erythrocytic fruiting bodies have crowned its head and broken through its side.
The lid is a heavy slab bound by two iron straps and bearing the Saint's gisant, cast in hematic iron: a
robed, veiled figure with a bone mask, hands folded over a lacquered heart, a bone skull at its feet.
In front stands the offering bowl, an iron basin on a bone stem, and a cresset on a tall iron stand
burns at each corner of the dais once the tomb has been consecrated.

Emits the static tomb, the lid, the bowl's blood, the cresset coals and the corpus's full-bright
memory veins as separate block models, the closed-tomb item model, the blockstate, the painted atlas,
an editable Blockbench project and SarcophagusGeometry.java (collision cells and render anchors).
Python 3 + Pillow + NumPy. `--preview` writes closed and open turnarounds to build/model-previews/.

Coordinates are block pixels for the north-facing state: the main block is z 0..16, the foot end with
the bowl faces the player at z = -16 and the head end lies in the back filler toward z = 32.
"""
import argparse
import copy
import json
import math

from clairaudiograph import clamp, noise
from export_scriptorium_upgrades import ROOT
from fixture_atlas import ASSETS, PALETTE, box, build, item_elements, mirror_x, model_json, preview, separate, write

ATLAS = 'saint_sarcophagus'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}
GEOMETRY = ROOT / 'src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/puzzle/SarcophagusGeometry.java'

# Renderer anchors (block pixels, north-facing). Written into SarcophagusGeometry.java.
LID_PIVOT = (13.25, 13.5)   # (x, y) of the rim edge the lid tips over once slid aside
LID_SLIDE = 8.0             # how far the lid slides east before it tips
LID_TIP = 18.0              # degrees the lid leans down past the rim
POOL = (6.0, 6.75, -14.0, 10.0, 8.25, -10.0)   # bowl blood at full fill: x0, y0, z0, x1, y1, z1
CRESSETS = [(1.75, 20.75, -10.0), (1.75, 20.75, 30.25), (14.25, 20.75, -10.0), (14.25, 20.75, 30.25)]

TOMB = (62, 10, 16)
TOMB_LIGHT = (92, 18, 26)
BRICK = (96, 10, 14)
MORTAR = (46, 6, 10)
EFFIGY = (86, 80, 92)
BONE_PALE = (226, 212, 184)
LINEN = (190, 176, 150)
SKIN = (132, 106, 90)
CAP = (176, 28, 36)
VEIN_CORE = (236, 52, 64)
VEIN_HOT = (255, 170, 150)


def pair(el):
    """The box and its east-west mirror."""
    return [el, mirror_x(el)]


def dais():
    e = [box('dais_base', (.5, 0, -15.5), (15.5, 1.5, 31.5), 'brick'),
         box('dais_step', (1.75, 1.5, -9), (14.25, 3, 30.75), 'brick', skip=('down',))]
    # Tallow candle stubs either side of the bowl, wax pooled at their feet.
    for x, z, h in ((3, -14.5, 3), (4.25, -13, 1.75), (2.75, -12.5, 2.25)):
        e += pair(box('candle', (x, 1.5, z), (x + 1, 1.5 + h, z + 1), 'tallow', skip=('down',), density=2))
        e += pair(box('wick', (x + .375, 1.5 + h, z + .375), (x + .625, 2 + h, z + .625), 'hand', skip=('down',)))
    e += pair(box('wax_pool', (2.5, 1.5, -15), (5.5, 1.75, -12), 'tallow', skip=('down',)))
    return e


def chest():
    e = [box('chest_plinth', (2.5, 3, -7.5), (13.5, 4.5, 29.5), 'tomb', skip=('down',))]
    # Arcaded flank panels between pilasters, blackened stone within.
    for z0, z1 in ((-5, 5), (6, 16), (17, 27)):
        e += pair(box('flank_panel', (3.25, 4.5, z0), (4.5, 12.5, z1), 'tomb', west='frieze', east='cavity',
                      skip=('up', 'down'), density=2))
    for z0 in (5, 16):
        e += pair(box('pilaster', (2.75, 4.5, z0), (4.5, 12.5, z0 + 1), 'tomb', skip=('up', 'down')))
    # Hematic iron corner straps.
    for z0, z1 in ((-7.5, -5), (27, 29.5)):
        e += pair(box('corner_strap', (2.5, 3, z0), (4.5, 13.5, z1), 'iron', skip=('down',)))
    e.append(box('foot_wall', (4.5, 4.5, -6.75), (11.5, 12.5, -5.25), 'tomb', north='plaque', south='cavity',
                 skip=('up', 'down'), density=2))
    e.append(box('head_wall', (4.5, 4.5, 27.25), (11.5, 12.5, 28.75), 'tomb', south='sigil', north='cavity',
                 skip=('up', 'down'), density=2))
    # The cornice the lid rests on: stone banded with bone dentils.
    e.append(box('cornice_foot', (4.5, 12.5, -7.25), (11.5, 13.5, -5.25), 'cornice', skip=('down',)))
    e.append(box('cornice_head', (4.5, 12.5, 27.25), (11.5, 13.5, 29.25), 'cornice', skip=('down',)))
    e += pair(box('cornice_side', (2.75, 12.5, -5), (4.5, 13.5, 27), 'cornice', skip=('down',)))
    e.append(box('tomb_floor', (4.5, 4, -5.25), (11.5, 4.5, 27.25), 'cavity', skip=('down', 'north', 'south',
                                                                                    'east', 'west')))
    return e


def corpus():
    """The preserved Saint on its pall; veins() inflates the bare flesh into the full-bright overlay."""
    e = [box('pall', (4.5, 4.5, -5.25), (11.5, 6.5, 27.25), 'pall', skip=('down', 'north', 'south', 'east', 'west')),
         box('pillow', (6, 6.5, 22), (10, 7.25, 26.25), 'linen', skip=('down',)),
         box('shroud_shins', (6.5, 6.5, -1), (9.5, 8.25, 4), 'linen', skip=('down',)),
         box('shroud_thighs', (6, 6.5, 4), (10, 8.75, 10), 'linen', skip=('down',)),
         box('shroud_torso', (6, 6.5, 10), (10, 9.75, 19), 'linen', skip=('down',)),
         box('shroud_shoulders', (5.5, 6.5, 19), (10.5, 9.25, 21.25), 'linen', skip=('down',))]
    e += pair(box('shroud_foot', (6.75, 8.25, -1), (7.75, 9.75, .25), 'linen', skip=('down',)))
    # Arms bound at the Saint's sides, forearms folded across the chest.
    e += pair(box('shroud_arm', (5, 6.5, 14.5), (6, 8.75, 20.5), 'linen', skip=('down',)))
    e += pair(box('shroud_forearm', (5.25, 8.75, 13), (7.25, 9.75, 15), 'linen', skip=('down',)))
    e.append(box('stole_chest', (6.75, 9.75, 15.25), (9.25, 10, 19), 'stole', skip=('down',)))
    e.append(box('stole_neck', (6, 9.25, 19), (10, 9.5, 21), 'stole', skip=('down',)))
    e += pair(box('stole_fall', (6.75, 8.75, 1.5), (7.5, 9, 10), 'stole', skip=('down',)))
    e += pair(box('stole_lap', (6.75, 9.75, 10), (7.5, 10, 12), 'stole', skip=('down',)))
    flesh = [box('neck', (7.25, 7, 21.25), (8.75, 8.5, 22), 'skin', skip=('down',)),
             box('head', (6.25, 7, 22), (9.75, 10.25, 25.75), 'skin', up='face', density=4),
             box('hand_left', (6.75, 9.75, 12.75), (8, 10.75, 15), 'skin', up='knuckles', density=2),
             box('hand_right', (8, 9.75, 13), (9.25, 10.75, 15.25), 'skin', up='knuckles', density=2)]
    vial = [box('vial', (7.5, 9.75, 10.75), (8.5, 11.25, 12.75), 'vial', density=2),
            box('vial_stopper', (7.625, 10, 10.25), (8.375, 11, 10.75), 'bone')]
    # Erythrocytic fruiting bodies crowning the head and breaking out through the shroud.
    caps = []
    for name, x, z, y, h, r in (('crown', 5.25, 24.5, 6.5, 2.5, .75), ('crown', 10.5, 24.25, 6.5, 2, .625),
                                ('crown', 6.5, 26.5, 7.25, 3, .875), ('crown', 9.25, 26.5, 7.25, 2.25, .625),
                                ('crown', 7.75, 26.75, 7.25, 3.5, 1.0), ('bloom', 10.5, 9, 6.5, 3, .875),
                                ('bloom', 10.75, 7.25, 6.5, 2, .625), ('bloom', 10, 10.75, 8.5, 2, .5)):
        caps.append(box(f'{name}_stipe', (x - .25, y, z - .25), (x + .25, y + h, z + .25), 'stipe', skip=('down',)))
        caps.append(box(f'{name}_cap', (x - r, y + h, z - r), (x + r, y + h + .75, z + r), 'cap', density=2))
    return e + flesh + vial + caps, flesh + vial


def bowl():
    e = [box('bowl_foot', (5.5, 1.5, -14.5), (10.5, 2.5, -9.5), 'tomb', skip=('down',)),
         box('bowl_knuckle', (6.75, 2.5, -13.25), (9.25, 3.25, -10.75), 'bone'),
         box('bowl_stem', (7.25, 3.25, -12.75), (8.75, 5.25, -11.25), 'bone', skip=('up', 'down')),
         box('bowl_knuckle', (6.75, 5.25, -13.25), (9.25, 6, -10.75), 'bone'),
         box('bowl_floor', (5.5, 6, -14.5), (10.5, 6.75, -9.5), 'iron', up='stain')]
    e.append(box('bowl_wall', (5, 6, -15), (11, 8.5, -14), 'iron', south='stain'))
    e.append(box('bowl_wall', (5, 6, -10), (11, 8.5, -9), 'iron', north='stain'))
    e += pair(box('bowl_wall', (5, 6, -14), (6, 8.5, -10), 'iron', east='stain'))
    e.append(box('bowl_rim', (4.75, 8.5, -15.25), (11.25, 9, -14), 'bone', skip=('down',)))
    e.append(box('bowl_rim', (4.75, 8.5, -10), (11.25, 9, -8.75), 'bone', skip=('down',)))
    e += pair(box('bowl_rim', (4.75, 8.5, -14), (6, 9, -10), 'bone', skip=('down',)))
    # The gutter that carries the offering into the tomb's foot.
    e.append(box('gutter', (7.25, 7, -8.75), (8.75, 7.75, -6.75), 'iron', up='stain'))
    e += pair(box('gutter_lip', (7, 7, -8.75), (7.25, 8.25, -6.75), 'iron'))
    return e


def cressets():
    e, coals = [], []
    for x, _, z in CRESSETS:
        e.append(box('cresset_foot', (x - 1.25, 1.5, z - 1.25), (x + 1.25, 2.25, z + 1.25), 'iron', skip=('down',)))
        e.append(box('cresset_stand', (x - .5, 2.25, z - .5), (x + .5, 18.5, z + .5), 'iron', skip=('up', 'down')))
        e.append(box('cresset_collar', (x - .75, 9, z - .75), (x + .75, 10, z + .75), 'bone'))
        e.append(box('cresset_floor', (x - 1.75, 18.5, z - 1.625), (x + 1.75, 19.25, z + 1.625), 'iron'))
        for dz in (-1.625, 1.125):
            e.append(box('cresset_bar', (x - 1.75, 19.25, z + dz), (x + 1.75, 21, z + dz + .5), 'iron', skip=('down',)))
        for dx in (-1.75, 1.25):
            e.append(box('cresset_bar', (x + dx, 19.25, z - 1.125), (x + dx + .5, 21, z + 1.125), 'iron',
                         skip=('down',)))
        e.append(box('cresset_ash', (x - 1.25, 19.25, z - 1.125), (x + 1.25, 20.25, z + 1.125), 'ash',
                     skip=('down',)))
        coals.append(box('cresset_coals', (x - 1, 20.25, z - .875), (x + 1, 20.75, z + .875), 'coal', skip=('down',),
                         extra=BRIGHT, density=2))
    return e, coals


def lid():
    e = [box('lid_slab', (2.25, 13.5, -7.75), (13.75, 15, 29.75), 'tomb', down='cavity'),
         box('lid_bevel', (3, 15, -7), (13, 15.75, 29), 'tomb', skip=('down',))]
    # Two iron straps bind the slab, clasped on either flank.
    for z0 in (1, 19):
        e.append(box('lid_strap', (2, 13.25, z0), (14, 15.25, z0 + 1.5), 'iron', skip=('down',)))
        e += pair(box('strap_clasp', (1.75, 13.5, z0 + .25), (2, 14.75, z0 + 1.25), 'ferric', skip=('east',)))
    # The gisant: a robed and veiled Saint with a bone mask, hands folded over a lacquered heart.
    e.append(box('effigy_cushion', (5, 15.75, 23), (11, 16.75, 27.75), 'effigy', up='tassel_cushion', skip=('down',)))
    e.append(box('effigy_head', (6.25, 15.75, 22.25), (9.75, 18.5, 25.75), 'effigy', up='mask', skip=('down',),
                 density=4))
    e.append(box('effigy_hood', (5.75, 15.75, 25.75), (10.25, 18.75, 26.75), 'effigy', skip=('down',)))
    e.append(box('effigy_shoulders', (5, 15.75, 18.75), (11, 17.75, 22.25), 'robe', skip=('down',)))
    e.append(box('effigy_chest', (5.75, 15.75, 9.5), (10.25, 18, 18.75), 'robe', skip=('down',)))
    e.append(box('effigy_skirt', (6, 15.75, -2.25), (10, 17.25, 9.5), 'robe', skip=('down',)))
    e += pair(box('effigy_arm', (4.75, 15.75, 13.5), (5.75, 17.5, 19.5), 'effigy', skip=('down',)))
    e += pair(box('effigy_forearm', (5, 17.25, 12.75), (7, 18.25, 14.5), 'effigy', skip=('down',)))
    e += pair(box('effigy_foot', (6.5, 17.25, -2.25), (7.75, 18.5, -1), 'effigy', skip=('down',)))
    e.append(box('effigy_hands', (6.75, 18, 12.5), (9.25, 19, 14.75), 'effigy', up='knuckles_iron', density=2))
    e.append(box('effigy_heart', (7.125, 18.25, 10.5), (8.875, 19.75, 12.5), 'lacquer', up='heart', density=2))
    e.append(box('effigy_stole', (6.75, 18, 14.75), (9.25, 18.25, 18.75), 'stole', skip=('down',)))
    e.append(box('effigy_stole_yoke', (6, 17.75, 18.75), (10, 18, 21.5), 'stole', skip=('down',)))
    e += pair(box('effigy_stole_fall', (6.75, 17.25, 1), (7.5, 17.5, 9.5), 'stole', skip=('down',)))
    e.append(box('effigy_skull', (6.75, 15.75, -6.25), (9.25, 17.75, -3.75), 'bone', north='skull', skip=('down',),
                 density=4))
    return e


def veins(parts):
    grow = .075
    return [box(el['name'] + '_vein', [a - grow for a in el['lo']], [b + grow for b in el['hi']], 'veins',
                skip=el['skip'], extra=BRIGHT, density=el['density']) for el in parts]


def pool():
    x0, y0, z0, x1, y1, z1 = POOL
    return [box('bowl_blood', (x0, y0, z0), (x1, y1, z1), 'pool', skip=('down',), density=2)]


# Coarse collision boxes (north-facing pixels); finer detail is decorative only.
COLLISION = [
    (.5, 0, -15.5, 15.5, 1.5, 31.5),
    (1.75, 1.5, -9, 14.25, 3, 30.75),
    (2.5, 3, -7.75, 13.5, 15, 29.75),
    (5, 15, -6.25, 11, 18.75, 27.75),
    (5, 1.5, -15, 11, 9, -9),
] + [b for x, _, z in CRESSETS for b in ((x - .5, 1.5, z - .5, x + .5, 18.5, z + .5),
                                         (x - 1.75, 18.5, z - 1.625, x + 1.75, 21, z + 1.625))]


# --- special faces --------------------------------------------------------------------------

def tomb(face, lx, ly, w, h, key):
    """Polished venous stone: deep crimson, mottled, with paler veins wandering through it."""
    n = noise('tomb', key, face, lx, ly)
    vein = abs(math.sin(lx * .7 + ly * .45 + noise('vphase', key, face) * 9) + math.sin(ly * .3 - lx * .2)) < .12
    if vein:
        return clamp(TOMB_LIGHT, int(n * 10)) + (255,)
    edge = (lx in (0, w - 1) or ly in (0, h - 1)) and w >= 3 and h >= 3
    return clamp(TOMB, int(n * 12) - 6 + (10 if ly == 0 and h > 1 and face not in ('up', 'down') else 0)
                 - (10 if edge else 0)) + (255,)


def brick(face, lx, ly, w, h, key):
    """Venous stone bricks laid in offset courses."""
    n = noise('brick', key, face, lx, ly)
    course = ly // 3 if face not in ('up', 'down') else ly // 4
    if face in ('up', 'down'):
        joint = ly % 4 == 3 or (lx + (course % 2) * 3) % 6 == 5
    else:
        joint = ly % 3 == 2 or (lx + (course % 2) * 3) % 6 == 5
    if joint:
        return clamp(MORTAR, int(n * 8)) + (255,)
    tone = int(noise('face', key, face, course, (lx + (course % 2) * 3) // 6) * 22) - 11
    return clamp(BRICK, tone + int(n * 8) - 4) + (255,)


def frieze(face, lx, ly, w, h, key):
    """A sunken lancet arch framed in bone with a vein-tree carved into its field."""
    n = noise('frieze', key, face, lx, ly)
    cx = (w - 1) / 2
    border = lx in (0, w - 1) or ly == h - 1
    spring = h * .38
    if ly < spring:
        # Pointed arch: two arcs meeting at the apex.
        r = w * .9
        inside = all(math.hypot(lx + .5 - ox, ly + .5 - spring) < r for ox in (w - r, r))
        rim = all(math.hypot(lx + .5 - ox, ly + .5 - spring) < r + 1.4 for ox in (w - r, r))
        if not rim:
            return clamp(TOMB, int(n * 10) - 6) + (255,)
        if not inside:
            return clamp(PALETTE['bone'], int(n * 10) - 16) + (255,)
    if border or lx in (1, w - 2):
        return clamp(PALETTE['bone'], int(n * 10) - (24 if lx in (1, w - 2) else 10)) + (255,)
    trunk = abs(lx - cx) < 1 and ly > spring * .6
    side = (ly + int(noise('fork', key, face) * 4)) % 5
    branch = ly < h - 3 and side == 0 and abs(lx - cx) < (h - ly) * .35 and abs(abs(lx - cx) - (ly % 5)) < 1.2
    twig = noise('twig', key, face, lx, ly) < .04 and abs(lx - cx) < w * .3
    if trunk or branch or twig:
        return clamp(PALETTE['blood'], 20 + int(n * 16)) + (255,)
    return clamp(PALETTE['lacquer_deep'], -18 + (h - ly) / max(h, 1) * 10 + int(n * 6)) + (255,)


def plaque(face, lx, ly, w, h, key):
    """The foot end: a bone plaque of inscription over the dark slot the offering gutter drains into."""
    n = noise('plaque', key, face, lx, ly)
    if 2 <= lx < w - 2 and 2 <= ly < h - 6:
        if lx in (2, w - 3) or ly in (2, h - 7):
            return clamp(PALETTE['bone'], int(n * 10) - 22) + (255,)
        line = (ly - 4) % 3 == 0 and ly >= 4
        if line and lx % 3 != 1 and noise('glyph', key, lx // 2, ly) > .35 and 3 < lx < w - 4:
            return (64, 16, 20, 255)
        return clamp(BONE_PALE, int(n * 10) - 8) + (255,)
    slot = abs(lx - (w - 1) / 2) < 2 and h - 4 <= ly <= h - 3
    if slot:
        return (20, 4, 8, 255)
    if abs(lx - (w - 1) / 2) < 1 and ly > h - 3:
        return clamp(PALETTE['blood'], int(n * 20)) + (255,)
    return tomb(face, lx, ly, w, h, key)


def sigil(face, lx, ly, w, h, key):
    """The head end: a witnessing eye in bone on a lacquered field, weeping down the stone."""
    n = noise('sigil', key, face, lx, ly)
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(PALETTE['bone'], int(n * 10) - 18) + (255,)
    u, v = (lx + .5 - w / 2) / (w * .4), (ly + .5 - h * .42) / (h * .2)
    lens = abs(v) - (1 - u * u)
    r = math.hypot(u * w * .4, v * h * .2)
    if abs(u) <= 1 and -.4 < lens <= 0:
        return clamp(PALETTE['bone'], int(n * 10) - 8) + (255,)
    if r < 1.2:
        return (20, 4, 8, 255)
    if r < 2.4 and lens < 0:
        return clamp(PALETTE['blood'], 10 + int(n * 16)) + (255,)
    if lx == round(w / 2 - .5) and ly > h * .55 and noise('tear', key, ly) < .8:
        return clamp(PALETTE['blood'], int(n * 16)) + (255,)
    return clamp(PALETTE['lacquer_deep'], (h / 2 - ly) / max(h, 1) * 14 + int(n * 6)) + (255,)


def cornice(face, lx, ly, w, h, key):
    """Stone cornice set with a band of bone dentils along its outer faces."""
    if face == 'up':
        return tomb(face, lx, ly, w, h, key)
    if face != 'down' and ly == h - 1 and lx % 2 == 0:
        return clamp(PALETTE['bone'], int(noise('dentil', key, face, lx) * 10) - 12) + (255,)
    return clamp(TOMB_LIGHT, int(noise('cornice', key, face, lx, ly) * 10) - 6 + (12 if ly == 0 else 0)) + (255,)


def pall(face, lx, ly, w, h, key):
    """Crimson funeral pall, its hem stitched in bone."""
    n = noise('pall', key, face, lx, ly)
    if face != 'up' and ly == h - 1 and lx % 2 == 0:
        return clamp(PALETTE['bone'], -20) + (255,)
    return clamp((112, 16, 30), int(n * 10) - 5 + (5 if (lx + ly) % 2 else -5)) + (255,)


def linen(face, lx, ly, w, h, key):
    """Burial linen wound in slanting strips, yellowed, seamed in shadow and soaked through with old blood."""
    n = noise('linen', key, face, lx, ly)
    wind = int(noise('wind', key, face) * 3)
    band = (ly + (lx // 2 if face in ('up', 'east', 'west') else 0) + wind) % 3
    blot = noise('blot', key, face, lx // 2, ly // 2)
    if blot < .1:
        return clamp((122, 36, 34), int(n * 14) - 7) + (255,)
    if blot < .2:
        return clamp((164, 104, 84), int(n * 12) - 6) + (255,)
    return clamp(LINEN, int(n * 10) - 5 + {0: -30, 1: 8, 2: 0}[band]) + (255,)


def skin(face, lx, ly, w, h, key):
    """Desiccated flesh drawn tight and cracked."""
    n = noise('skin', key, face, lx, ly)
    if noise('crack', key, face, lx, ly // 2) < .06:
        return clamp(SKIN, -40) + (255,)
    return clamp(SKIN, int(n * 14) - 7) + (255,)


def face_tex(face, lx, ly, w, h, key):
    """The Saint's face turned to the lid: sunken sockets, a fallen nose and bared teeth (chin at ly = 0)."""
    n = noise('face', key, lx, ly)
    u, v = (lx + .5) / w, (ly + .5) / h
    for ex in (.3, .7):
        d = math.hypot((u - ex) * 1.2, v - .62)
        if d < .1:
            return (22, 10, 10, 255)
        if d < .16:
            return clamp(SKIN, -34) + (255,)
    if abs(u - .5) < .07 and .4 < v < .52:
        return (34, 16, 16, 255)
    if .14 < v < .26 and abs(u - .5) < .24:
        return (204, 190, 160, 255) if int(lx) % 2 == 0 and v > .18 else (40, 18, 18, 255)
    if u < .12 or u > .88:
        return clamp(SKIN, -18 + int(n * 8)) + (255,)
    return clamp(SKIN, int(n * 12) - 2 + (8 if .66 < v < .9 else 0)) + (255,)


def knuckles_iron(face, lx, ly, w, h, key):
    if lx % 2 == 1 and ly < h - 1:
        return clamp(EFFIGY, -34) + (255,)
    return effigy(face, lx, ly, w, h, key)


def knuckles(face, lx, ly, w, h, key):
    if lx % 2 == 1 and ly < h - 1:
        return clamp(SKIN, -30) + (255,)
    return skin(face, lx, ly, w, h, key)


def vial(face, lx, ly, w, h, key):
    """A sealed vial of the Saint's blood, a glint down one side."""
    if face in ('up', 'down'):
        return clamp(PALETTE['blood'], -20) + (255,)
    if lx == 0 and ly < h - 1:
        return (232, 140, 140, 255)
    return clamp(PALETTE['blood'], int(noise('vial', key, face, lx, ly) * 20) - (24 if ly < 2 else 0)) + (255,)


def stole(face, lx, ly, w, h, key):
    """Crimson stole, darker along its edges, a bone thread stitched every few texels down its middle."""
    n = noise('stole', key, face, lx, ly)
    if face == 'up' and w >= 3 and lx == (w - 1) // 2 and ly % 4 == 1:
        return clamp(PALETTE['bone'], -20) + (255,)
    return clamp(PALETTE['lacquer'], int(n * 10) - 5 - (22 if face == 'up' and lx in (0, w - 1) else 0)) + (255,)


def tallow(face, lx, ly, w, h, key):
    """Bone-white tallow, a run of blood-dark drip down one side."""
    n = noise('tallow', key, face, lx, ly)
    if face in ('north', 'west') and lx == 0 and ly <= h * .6:
        return clamp(PALETTE['blood'], -10 + int(n * 12)) + (255,)
    return clamp((214, 200, 170), int(n * 12) - 6 + (10 if ly == 0 else 0)) + (255,)


def cap(face, lx, ly, w, h, key):
    """Erythrocytic fruiting body: wet crimson cap, pale flecks on top, pale gills beneath."""
    n = noise('cap', key, face, lx, ly)
    if face == 'down':
        return clamp((214, 176, 168), int(n * 12) - 6 - (20 if lx % 2 else 0)) + (255,)
    if face == 'up' and n > .78:
        return clamp(BONE_PALE, -10) + (255,)
    return clamp(CAP, int(n * 14) - 7 + (12 if ly == 0 else 0)) + (255,)


def stipe(face, lx, ly, w, h, key):
    return clamp((208, 186, 172), int(noise('stipe', key, face, lx, ly) * 14) - 7) + (255,)


def robe(face, lx, ly, w, h, key):
    """The gisant's robe cast in hematic iron: deep folds running its length, blood in the grain."""
    n = noise('robe', key, face, lx, ly)
    across = lx
    fold = (across + int(noise('pleat', key, face) * 4)) % 3
    shade = {0: -24, 1: 14, 2: 0}[fold]
    if noise('vein', key, face, lx, ly // 2) < .04:
        return (112, 28, 38, 255)
    return clamp(EFFIGY, shade + int(n * 8) - 4) + (255,)


def effigy(face, lx, ly, w, h, key):
    n = noise('effigy', key, face, lx, ly)
    if noise('vein', key, face, lx, ly) < .035:
        return (112, 28, 38, 255)
    return clamp(EFFIGY, int(n * 12) - 6 + (22 if ly == 0 and face not in ('up', 'down') else 0)
                 + (8 if face == 'up' else 0)) + (255,)


def mask(face, lx, ly, w, h, key):
    """The veiled head: a serene bone mask with closed eyes weeping blood, framed by the iron hood.

    Laid out in texels of the 14x14 face, chin at ly = 0 and crown at ly = 13.
    """
    n = noise('mask', key, lx, ly)
    if lx < 2 or lx > w - 3 or ly > h - 3:
        return effigy(face, lx, ly, w, h, key)
    seam = (58, 30, 30, 255)
    for eye in (4, w - 5):
        if (ly == 8 and abs(lx - eye) <= 1) or (ly == 7 and lx == eye):
            return seam
        if lx == eye and 3 <= ly <= 6:
            return clamp(PALETTE['blood'], 24 + int(n * 16)) + (255,)
    if lx in (w // 2 - 1, w // 2) and ly in (5, 6):
        return clamp(BONE_PALE, -48) + (255,)
    if ly == 2 and w // 2 - 2 <= lx <= w // 2 + 1:
        return seam
    return clamp(BONE_PALE, int(n * 8) - 4 - abs(lx * 2 - w + 1) * 2) + (255,)


def tassel_cushion(face, lx, ly, w, h, key):
    if (lx in (0, w - 1)) and (ly in (0, h - 1)):
        return clamp(PALETTE['bone'], -10) + (255,)
    return effigy(face, lx, ly, w, h, key)


def heart(face, lx, ly, w, h, key):
    n = noise('heart', key, lx, ly)
    if abs(lx - (w - 1) / 2) < 1 and ly < h - 1:
        return clamp(PALETTE['lacquer_deep'], -10) + (255,)
    return clamp(PALETTE['lacquer'], 18 - ly * 4 + int(n * 8)) + (255,)


def skull(face, lx, ly, w, h, key):
    """A skull at the gisant's feet, staring at whoever kneels at the bowl."""
    u, v = (lx + .5) / w, (ly + .5) / h
    n = noise('skull', key, lx, ly)
    for ex in (.3, .7):
        if math.hypot(u - ex, (v - .4) * 1.1) < .14:
            return (24, 10, 12, 255)
    if abs(u - .5) < .07 and .55 < v < .68:
        return (40, 18, 18, 255)
    if v > .78 and abs(u - .5) < .3:
        return (40, 18, 18, 255) if lx % 2 else clamp(BONE_PALE, -8) + (255,)
    return clamp(PALETTE['bone'], int(n * 12) - 6) + (255,)


def stain(face, lx, ly, w, h, key):
    """Iron gone black with old offerings."""
    n = noise('stain', key, face, lx, ly)
    return clamp((70, 14, 20), int(n * 16) - 8) + (255,)


def ash(face, lx, ly, w, h, key):
    n = noise('ash', key, face, lx, ly)
    if face == 'up' and n > .86:
        return (96, 30, 26, 255)
    return clamp((54, 48, 50), int(n * 18) - 9) + (255,)


def coal(face, lx, ly, w, h, key):
    """Live coals: crimson embers with hot cores."""
    n = noise('coal', key, face, lx, ly)
    if n > .8:
        return (255, 196, 120, 255)
    if n > .45:
        return (240, 84, 44, 255)
    return (168, 22, 26, 255)


def pool_tex(face, lx, ly, w, h, key):
    n = noise('pool', key, face, lx, ly)
    if face == 'up' and (lx + ly * 2) % 7 == 0 and n > .5:
        return (196, 52, 60, 255)
    return clamp((124, 8, 20), int(n * 14) - 7) + (255,)


def veins_tex(face, lx, ly, w, h, key):
    """Memory veins kindling under dead flesh: sparse branching crimson threads, clear between them."""
    lane = lx * 2 + ly + int(noise('trunk', key, face) * 13)
    trunk = lane % 9 == 0 and noise('run', key, face, lane // 9, ly // 3) < .55
    twig = noise('twig', key, face, lx, ly // 2) < .03
    if trunk or twig:
        return (VEIN_HOT if noise('hot', key, face, lx, ly) > .8 else VEIN_CORE) + (255,)
    return (0, 0, 0, 0)


SPECIAL = dict(tomb=tomb, brick=brick, frieze=frieze, plaque=plaque, sigil=sigil, cornice=cornice, pall=pall,
               linen=linen, skin=skin, face=face_tex, knuckles=knuckles, vial=vial, stole=stole, cap=cap,
               stipe=stipe, robe=robe, effigy=effigy, mask=mask, tassel_cushion=tassel_cushion, heart=heart,
               skull=skull, tallow=tallow, knuckles_iron=knuckles_iron, stain=stain, ash=ash, coal=coal, pool=pool_tex, veins=veins_tex)


# --- outputs --------------------------------------------------------------------------------

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -1, 0], 'scale': [0.3, 0.3, 0.3]},
    'ground': {'translation': [0, 2, 0], 'scale': [0.15, 0.15, 0.15]},
    'fixed': {'rotation': [0, 90, 0], 'translation': [0, 0, 0], 'scale': [0.3, 0.3, 0.3]},
    'head': {'translation': [0, 6, 0], 'scale': [0.4, 0.4, 0.4]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.14, 0.14, 0.14]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.14, 0.14, 0.14]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 2, 0], 'scale': [0.12, 0.12, 0.12]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 2, 0], 'scale': [0.12, 0.12, 0.12]},
}


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        variants[f'facing={facing}'] = dict({'model': 'hemomancy:block/saint_sarcophagus'}, **({'y': y} if y else {}))
    return {'variants': variants}


def drop_side_culling(elements):
    """Only the dais floor may cull: the model spans three cells, so one neighbour cannot hide a whole face."""
    for el in elements:
        for side, f in el['faces'].items():
            if side != 'down':
                f.pop('cullface', None)
    return elements


def opened(elements, t=1.0):
    """Lid elements slid aside and tipped over the rim, as the renderer draws them at open fraction t."""
    slide = LID_SLIDE * min(t / .7, 1)
    tip = LID_TIP * max((t - .6) / .4, 0)
    out = []
    for el in copy.deepcopy(elements):
        assert 'rotation' not in el, el['name']
        el['from'][0] += slide
        el['to'][0] += slide
        if tip:
            el['rotation'] = {'angle': -tip, 'axis': 'z', 'origin': [LID_PIVOT[0], LID_PIVOT[1], 8]}
        out.append(el)
    return out


def fmt(v):
    return f'{v:g}' if v != int(v) else f'{v:.1f}'


def geometry_java():
    boxes = ',\n'.join('        {' + ', '.join(fmt(v) for v in b) + '}' for b in COLLISION)
    cressets = ',\n'.join('        {' + ', '.join(fmt(v) + 'f' for v in c) + '}' for c in CRESSETS)
    x0, y0, z0, x1, y1, z1 = POOL
    return f'''package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Generated by tools/model_export/saint_sarcophagus.py; pixels of the north-facing tomb (foot end at z = -16). */
public final class SarcophagusGeometry {{
    /** Rim edge (x, y) the lid tips over once it has slid {fmt(LID_SLIDE)} pixels east. */
    public static final float LID_PIVOT_X = {fmt(LID_PIVOT[0])}f, LID_PIVOT_Y = {fmt(LID_PIVOT[1])}f;
    public static final float LID_SLIDE = {fmt(LID_SLIDE)}f, LID_TIP = {fmt(LID_TIP)}f;
    /** Offering bowl blood at full fill: floor and surface heights; its centre is the bowl particle anchor. */
    public static final float POOL_FLOOR = {fmt(y0)}f, POOL_TOP = {fmt(y1)}f;
    public static final float BOWL_X = {fmt((x0 + x1) / 2)}f, BOWL_Z = {fmt((z0 + z1) / 2)}f;
    /** Top of each cresset's coals (x, y, z). */
    public static final float[][] CRESSETS = {{
{cressets}
    }};
    private static final double[][] BOXES = {{
{boxes}
    }};
    private static final Map<BlockPos, VoxelShape>[] CELLS = buildCells();

    private SarcophagusGeometry() {{}}

    public static VoxelShape shape(Direction facing, BlockPos offset) {{
        return CELLS[facing.get2DDataValue()].getOrDefault(offset, Shapes.empty());
    }}

    /** A north-facing pixel point turned to {{@code facing}} and returned in block units from the main block. */
    public static double[] point(Direction facing, double x, double y, double z) {{
        for (int turn = 0; turn < turns(facing); turn++) {{
            double nextX = 16 - z;
            z = x;
            x = nextX;
        }}
        return new double[]{{x / 16, y / 16, z / 16}};
    }}

    private static int turns(Direction facing) {{
        return switch (facing) {{ case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; }};
    }}

    @SuppressWarnings("unchecked")
    private static Map<BlockPos, VoxelShape>[] buildCells() {{
        Map<BlockPos, VoxelShape>[] result = new Map[4];
        for (Direction facing : Direction.Plane.HORIZONTAL) {{
            Map<BlockPos, VoxelShape> cells = new LinkedHashMap<>();
            for (double[] box : BOXES) {{
                double x0=box[0], z0=box[2], x1=box[3], z1=box[5];
                for (int turn = 0; turn < turns(facing); turn++) {{
                    double nextX0=16-z1, nextX1=16-z0;
                    z0=x0; z1=x1; x0=nextX0; x1=nextX1;
                }}
                VoxelShape shape = Block.box(x0, box[1], z0, x1, box[4], z1);
                for (int x=(int)Math.floor(x0/16); x<(int)Math.ceil(x1/16); x++)
                    for (int y=(int)Math.floor(box[1]/16); y<(int)Math.ceil(box[4]/16); y++)
                        for (int z=(int)Math.floor(z0/16); z<(int)Math.ceil(z1/16); z++) {{
                            BlockPos offset = new BlockPos(x,y,z);
                            VoxelShape clipped = Shapes.join(shape.move(-x,-y,-z), Shapes.block(), BooleanOp.AND);
                            if (!clipped.isEmpty()) cells.merge(offset, clipped, Shapes::or);
                        }}
            }}
            cells.replaceAll((offset, shape) -> shape.optimize());
            result[facing.get2DDataValue()] = Map.copyOf(cells);
        }}
        return result;
    }}
}}
'''


def closeup(elements, title, out_name, views, lift=2, size=640, scale=13):
    """A larger sheet of chosen (yaw, pitch) views for reading the corpus and gisant."""
    import numpy as np
    from PIL import Image, ImageDraw
    from clairaudiograph import render
    texture = np.array(Image.open(ASSETS / f'textures/block/{ATLAS}.png').convert('RGBA'))
    model = copy.deepcopy(elements)
    for el in model:
        for k in ('from', 'to'):
            el[k][1] -= lift
        if 'rotation' in el:
            el['rotation']['origin'][1] -= lift
    sheet = Image.new('RGBA', (size * len(views), size + 30), (30, 24, 28, 255))
    for i, (yaw, pitch) in enumerate(views):
        sheet.paste(render(model, {'atlas': texture}, yaw, pitch, size, scale), (size * i, 30))
    ImageDraw.Draw(sheet).text((10, 8), title, fill=(220, 200, 200))
    out = ROOT / f'build/model-previews/{out_name}.png'
    sheet.convert('RGB').save(out)
    print(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    stand, coals = cressets()
    body_corpus, flesh = corpus()
    body = dais() + chest() + body_corpus + bowl() + stand
    top, blood, glow = lid(), pool(), veins(flesh)
    moved = separate(body) + separate(top)
    groups = {'body': body, 'lid': top, 'blood': blood, 'coals': coals, 'veins': glow}
    atlas, models = build(groups, SPECIAL)
    for elements in models.values():
        drop_side_culling(elements)
        for el in elements:
            assert all(-16 <= v <= 32 for v in el['from'] + el['to']), (el['name'], el['from'], el['to'])
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = 'hemomancy:block/polished_venous_stone'

    def block(elements):
        return model_json(elements, ATLAS, atlas.height, particle)

    item = model_json(item_elements((models['body'], 0), (models['lid'], 0)), ATLAS, atlas.height,
                      particle, DISPLAY)
    item['gui_light'] = 'front'
    outputs = {'block/saint_sarcophagus': block(models['body']),
               **{f'block/saint_sarcophagus_{part}': block(models[part]) for part in ('lid', 'blood', 'coals', 'veins')},
               'item/saint_sarcophagus': item}
    project = dict(item, elements=item['elements'] + item_elements((models['blood'], 0), (models['coals'], 0),
                                                                   (models['veins'], 0)))
    write(outputs, 'saint_sarcophagus', project)
    (ASSETS / 'blockstates/saint_sarcophagus.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    GEOMETRY.write_text(geometry_java())
    print(f'Exported Saint Sarcophagus: {len(body)} body, {len(top)} lid, {len(coals)} coal, {len(glow)} vein cubes, '
          f'{moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        lit = item_elements((models['body'], 0), (models['blood'], 0), (models['coals'], 0))
        preview(item['elements'], ATLAS, 'Saint Sarcophagus - sealed, actual atlas', 'saint_sarcophagus',
                lift=2, size=420, scale=7)
        preview(lit + opened(item_elements((models['lid'], 0))) + item_elements((models['veins'], 0)), ATLAS,
                'Saint Sarcophagus - consecrated, lid slid aside, corpus kindled', 'saint_sarcophagus_open',
                lift=2, size=420, scale=7)
        interior = lit + item_elements((models['veins'], 0))
        closeup(interior, 'Saint Sarcophagus - corpus from above (lid removed)', 'saint_sarcophagus_corpus',
                [(110, 55), (60, 70), (160, 50)])
        closeup(item['elements'], 'Saint Sarcophagus - gisant and foot end', 'saint_sarcophagus_gisant',
                [(-30, 35), (30, 25), (120, 40)])


if __name__ == '__main__':
    main()

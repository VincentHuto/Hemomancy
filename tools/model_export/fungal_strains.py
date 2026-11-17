"""Generates the strain beds (core-loop round two, P7): the eight fungal strains planted from their spores, Salt-Stained
Timber, the two catalyst crops (glowroot and emberpod) and Tainted Harvest. Writes textures, blockstates, models,
item models, block loot, the timber and crop recipes and the block and item names.
Run from the repo root: python tools/model_export/fungal_strains.py

Every sprite is painted here at 16 px a block. The strains take one of three shapes by substrate group: bracket shelves
for the flesh-feeding strains on Ravenous Heartwood, pale salt-crusted buttons for the Brined strains on Salt-Stained
Timber, thin capped stems for the rest on fungal ground. Each strain's colours come from its school. No copper, brass,
bronze or silver anywhere: the timber's salt is white mineral crust, the Ferric strain is rust and dark iron.
"""
import io
import json
import math
import os
import random

from PIL import Image

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
ASSETS = os.path.join(ROOT, 'src/main/resources/assets/hemomancy')
DATA = os.path.join(ROOT, 'src/main/resources/data/hemomancy')
TEX = os.path.join(ASSETS, 'textures')
LANG = os.path.join(ASSETS, 'lang/en_us.json')
SIZE = 16
CLEAR = (0, 0, 0, 0)

# name: (group, school palette dark -> bright, then a pale accent)
STRAINS = {
    'vivacious': ('flesh', [(58, 6, 16), (128, 16, 30), (196, 44, 52), (238, 120, 112)]),
    'fervent': ('flesh', [(80, 20, 8), (176, 62, 18), (236, 132, 40), (255, 210, 112)]),
    'ruinous': ('flesh', [(22, 20, 17), (52, 48, 38), (98, 106, 64), (152, 162, 96)]),
    'neurotic': ('brined', [(92, 70, 8), (180, 146, 26), (240, 208, 70), (255, 244, 168)]),
    'frigid': ('brined', [(30, 60, 98), (82, 142, 194), (170, 214, 242), (232, 246, 255)]),
    'ferric': ('brined', [(40, 28, 26), (100, 46, 36), (158, 74, 56), (96, 92, 98)]),
    'incandescent': ('common', [(140, 108, 56), (214, 184, 108), (252, 236, 178), (255, 254, 232)]),
    'umbral': ('common', [(16, 8, 24), (50, 20, 74), (112, 58, 162), (172, 118, 222)]),
}
ENGLISH = {name: name.capitalize() + ' Strain' for name in STRAINS}

SALT = [(112, 114, 116), (170, 172, 170), (220, 220, 214), (246, 246, 240)]
FLESH_STALK = [(46, 10, 18), (84, 22, 30), (120, 40, 44)]
PALE_STALK = [(150, 140, 128), (200, 190, 176), (232, 226, 214)]


def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    io.open(path, 'w', encoding='utf-8', newline='\n').write(json.dumps(obj, indent=2, ensure_ascii=False) + '\n')


def save(image, rel):
    path = os.path.join(TEX, rel + '.png')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    image.save(path)


class Canvas:
    """A 16 px sprite: y counts down from the top, as the texture does."""

    def __init__(self):
        self.image = Image.new('RGBA', (SIZE, SIZE), CLEAR)
        self.px = self.image.load()

    def put(self, x, y, colour):
        x, y = int(round(x)), int(round(y))
        if 0 <= x < SIZE and 0 <= y < SIZE:
            self.px[x, y] = tuple(colour[:3]) + (colour[3] if len(colour) == 4 else 255,)

    def filled(self, x, y):
        x, y = int(round(x)), int(round(y))
        return 0 <= x < SIZE and 0 <= y < SIZE and self.px[x, y][3] > 0

    def outline(self, colour):
        """A one-pixel dark edge round what is drawn: at 16 px a sprite without one melts into the block behind it."""
        edge = []
        for y in range(SIZE):
            for x in range(SIZE):
                if self.px[x, y][3]:
                    continue
                if any(0 <= x + dx < SIZE and 0 <= y + dy < SIZE and self.px[x + dx, y + dy][3] and self.px[x + dx, y + dy][:3] != colour
                       for dx, dy in ((1, 0), (-1, 0), (0, 1))):
                    edge.append((x, y))
        for x, y in edge:
            self.put(x, y, colour)


# ---------------------------------------------------------------- the strains

def stalk(c, x, top, bottom, ramp, width=1):
    for y in range(top, bottom + 1):
        for dx in range(width):
            c.put(x + dx, y, ramp[1] if dx == 0 else ramp[0])
    c.put(x, top, ramp[2])


def shelf(c, cx, y, half, pal):
    """One shelf of a bracket cluster seen edge on: a wide flat lens, its top banded in growth zones out to a pale lip,
    the dark pore face underneath, thickest in the middle where it meets the wood."""
    dark, mid, bright, pale = pal
    for dx in range(-half, half + 1):
        t = abs(dx) / (half + 0.5)
        thick = max(1, round(2.6 * math.sqrt(max(0.0, 1.0 - t * t)) + 0.4))
        band = int(t * 3.5) % 2
        for k in range(thick):
            if k == 0:
                colour = pale if abs(dx) == half else bright if band else mid
            elif k == thick - 1:
                colour = dark
            else:
                colour = lerp(mid, dark, 0.45)
            c.put(cx + dx, y + k, colour)


def flesh_strain(stage, pal, seed):
    """Bracket fungi stacked in an overlapping clump on the wood, like chicken-of-the-woods: no stem, only shelves, each
    tier offset a little from the last and the lowest widest."""
    c = Canvas()
    dark, mid, bright, pale = pal
    base = SIZE - 1
    if stage == 0:
        for x, h in ((6, 1), (7, 2), (8, 2), (9, 1)):
            for y in range(base - h + 1, base + 1):
                c.put(x, y, bright if y == base - h + 1 else mid)
        c.outline(lerp(dark, (0, 0, 0), 0.5))
        return c.image
    tiers = {1: [(7, base - 1, 3), (8, base - 3, 2)],
             2: [(7, base - 1, 5), (9, base - 4, 4), (6, base - 6, 3)],
             3: [(8, base - 1, 7), (6, base - 4, 6), (9, base - 7, 5), (7, base - 10, 4), (8, base - 12, 2)]}[stage]
    # The highest tier first, so each lower, nearer shelf lies over the one above it.
    for cx, y, half in reversed(tiers):
        shelf(c, cx + (seed % 3) - 1, y, half, pal)
    if stage == 3:
        # Beads of the strain's blood weeping from the lowest lip.
        c.put(1 + (seed % 3), base, bright)
        c.put(14 + (seed % 3) - 1, base, bright)
    c.outline(lerp(dark, (0, 0, 0), 0.55))
    return c.image


def button(c, cx, cy, r, pal, rng, gills=True):
    """A round-capped button: salt crust on top, the school's colour in the gills and the crown."""
    dark, mid, bright, pale = pal
    for dy in range(-r, 1):
        for dx in range(-r, r + 1):
            if dx * dx + (dy * 1.35) ** 2 > r * r + 0.6:
                continue
            light = 1.0 - (dy + r) / (2.0 * r + 1) * 0.3 - (dx + r) / (2.0 * r + 1) * 0.5
            salt = SALT[min(3, max(0, int(1 + 2.4 * light)))]
            # The crown takes the school's colour, as if the salt had cracked over it.
            if dx * dx + (dy + r * 0.6) ** 2 < (r * 0.55) ** 2 + 0.2:
                salt = lerp(salt, bright, 0.55)
            c.put(cx + dx, cy + dy, salt)
    # Salt crystals glinting on the crust.
    for _ in range(max(1, r - 1)):
        c.put(cx + rng.randint(-r + 1, r - 1), cy - rng.randint(1, max(1, r)), SALT[3])
    if gills:
        for dx in range(-r, r + 1):
            c.put(cx + dx, cy + 1, mid if abs(dx) < r else dark)
    for y in range(cy + 2, SIZE):
        c.put(cx, y, SALT[1])
        if y > cy + 2:
            c.put(cx + 1, y, SALT[0])


def brined_strain(stage, pal, seed):
    rng = random.Random(seed * 31 + stage)
    c = Canvas()
    dark = pal[0]
    if stage == 0:
        for x in (5, 8, 11):
            c.put(x, SIZE - 2, SALT[2])
            c.put(x, SIZE - 1, SALT[1])
            c.put(x + 1, SIZE - 2, pal[2])
        c.outline(lerp(SALT[0], (0, 0, 0), 0.45))
        return c.image
    caps = {1: [(6, 13, 1), (10, 14, 1)],
            2: [(5, 12, 2), (10, 10, 2), (12, 14, 1)],
            3: [(4, 11, 2), (9, 7, 3), (13, 11, 2), (7, 14, 1), (11, 14, 1)]}[stage]
    # Furthest first, so the nearer caps overlap them.
    for cx, cy, r in sorted(caps, key=lambda cap: cap[1]):
        button(c, cx, cy, r, pal, rng)
    c.outline(lerp(dark, (0, 0, 0), 0.6))
    return c.image


def cap(c, cx, cy, r, pal, glow):
    dark, mid, bright, pale = pal
    for dx in range(-r, r + 1):
        c.put(cx + dx, cy, mid if abs(dx) == r else bright)
    for dx in range(-max(0, r - 1), max(0, r - 1) + 1):
        c.put(cx + dx, cy - 1, pale if glow and dx == 0 else bright if dx else mid)
    if r >= 2:
        c.put(cx, cy - 2, mid)
    c.put(cx, cy + 1, dark)


def common_strain(stage, pal, seed, glow):
    rng = random.Random(seed * 7 + stage)
    c = Canvas()
    dark, mid, bright, pale = pal
    stalk_ramp = [lerp(PALE_STALK[0], mid, 0.45), lerp(PALE_STALK[1], mid, 0.3), lerp(PALE_STALK[2], bright, 0.3)]
    stems = {0: [(6, 13, 0), (9, 14, 0)],
             1: [(5, 11, 1), (10, 12, 1)],
             2: [(4, 9, 1), (8, 6, 2), (12, 10, 1), (10, 13, 1)],
             3: [(3, 8, 2), (7, 3, 2), (11, 6, 2), (13, 11, 1), (5, 12, 1), (9, 12, 1)]}[stage]
    for x, top, r in stems:
        lean = rng.choice((-1, 0, 0, 1)) if stage >= 2 else 0
        for y in range(top + 1, SIZE):
            t = (y - top) / max(1, SIZE - top)
            c.put(x + round(lean * (1 - t)), y, stalk_ramp[0] if y % 4 == 0 else stalk_ramp[1])
        if r == 0:
            c.put(x, top, bright)
            c.put(x, top + 1, mid)
        else:
            cap(c, x + lean, top, r, pal, glow)
    c.outline(lerp(dark, (0, 0, 0), 0.5))
    return c.image


def strain_stage(name, stage):
    group, pal = STRAINS[name]
    seed = sum(map(ord, name))
    if group == 'flesh':
        return flesh_strain(stage, pal, seed)
    if group == 'brined':
        return brined_strain(stage, pal, seed)
    return common_strain(stage, pal, seed, glow=name == 'incandescent')


# ---------------------------------------------------------------- Salt-Stained Timber

def grain(x, y, seed):
    return 0.5 + 0.3 * math.sin(2 * math.pi * (2 * x / SIZE + 0.15 * math.sin(2 * math.pi * y / SIZE + seed))) \
        + 0.15 * math.sin(2 * math.pi * (5 * x / SIZE + 0.5 * y / SIZE + seed))


TIMBER = [(62, 56, 50), (92, 84, 74), (120, 110, 96), (146, 136, 118)]


def salt_bloom(x, y, seed):
    """Where the salt has crusted: blotches that run along the grain, heavier toward the block's foot."""
    v = math.sin(x * 1.7 + seed) * math.cos(y * 0.6 + seed * 2) + 0.6 * math.sin((x + y * 0.3) * 0.9 + seed * 3)
    return v + (y / SIZE) * 0.9


def timber_side():
    c = Canvas()
    for y in range(SIZE):
        for x in range(SIZE):
            g = grain(x, y, 0.7)
            colour = TIMBER[min(3, max(0, int(g * 3.2)))]
            # Cracks in the bleached wood, every few columns.
            if x in (3, 9, 13) and (y + x) % 7 not in (0, 1):
                colour = lerp(colour, TIMBER[0], 0.7)
            bloom = salt_bloom(x, y, 1.3)
            if bloom > 1.55:
                colour = SALT[3] if bloom > 1.75 else SALT[2]
            elif bloom > 1.3:
                colour = lerp(colour, SALT[2], 0.4)
            c.put(x, y, colour)
    # A faint brine tide line across the wood.
    for x in range(SIZE):
        y = 10 + round(math.sin(x * 0.6) * 0.8)
        c.put(x, y, lerp(c.px[x, y][:3], (86, 92, 96), 0.35))
    return c.image


def timber_top():
    c = Canvas()
    for y in range(SIZE):
        for x in range(SIZE):
            r = math.hypot(x - 7.5, y - 7.5)
            if r > 6.6:
                colour = TIMBER[1] if (x + y) % 3 else TIMBER[0]
                bloom = salt_bloom(x, y, 2.1)
                if bloom > 1.3:
                    colour = SALT[2]
            else:
                ring = int(r) % 2
                colour = TIMBER[3] if ring else TIMBER[2]
                if r < 1.2:
                    colour = TIMBER[1]
            c.put(x, y, colour)
    # Salt crusted in the radial checks of the end grain.
    for angle in (0.4, 2.2, 4.1):
        for step in range(2, 7):
            c.put(7.5 + math.cos(angle) * step, 7.5 + math.sin(angle) * step, SALT[2] if step % 2 else SALT[3])
    return c.image


# ---------------------------------------------------------------- catalyst crops

GLOW_LEAF = [(52, 70, 34), (96, 126, 54), (150, 176, 84)]
GLOW_BULB = [(196, 160, 84), (244, 222, 140), (255, 250, 214)]
EMBER_LEAF = [(42, 14, 12), (88, 26, 20), (130, 42, 28)]
EMBER_POD = [(120, 30, 12), (214, 84, 24), (255, 176, 64)]


def leaf_fan(c, cx, base, height, ramp, count, spread, seed):
    rng = random.Random(seed)
    for i in range(count):
        angle = -spread / 2 + spread * i / max(1, count - 1) + rng.uniform(-0.1, 0.1)
        length = height * rng.uniform(0.75, 1.0)
        for step in range(int(length) + 1):
            t = step / max(1, length)
            x = cx + math.sin(angle) * step * (0.6 + 0.6 * t)
            y = base - math.cos(angle) * step
            c.put(x, y, ramp[2] if t > 0.7 else ramp[1])
            if t > 0.25 and t < 0.85:
                c.put(x + (1 if angle > 0 else -1), y, ramp[0])


def glowroot_stage(stage):
    c = Canvas()
    for i, cx in enumerate((3, 8, 13)):
        height = (2, 4, 7, 9)[stage]
        leaf_fan(c, cx, SIZE - 2, height, GLOW_LEAF, 2 + min(stage, 2), 1.2 + 0.15 * stage, 11 + i)
        if stage >= 2:
            # The root's crown breaks the soil, pale gold, and glows once ripe.
            for dx in (-1, 0, 1):
                c.put(cx + dx, SIZE - 1, GLOW_BULB[1] if stage == 3 else GLOW_BULB[0])
            if stage == 3:
                # A fat crown shouldering up out of the soil, its top lit.
                for dx in (-1, 0, 1):
                    c.put(cx + dx, SIZE - 2, GLOW_BULB[2] if dx == 0 else GLOW_BULB[1])
                    c.put(cx + dx, SIZE - 3, GLOW_BULB[1] if dx == 0 else GLOW_BULB[0])
                c.put(cx, SIZE - 4, (255, 255, 236))
    c.outline((30, 40, 20))
    return c.image


def emberpod_stage(stage):
    c = Canvas()
    for i, cx in enumerate((4, 11)):
        height = (3, 6, 10, 13)[stage]
        # A charred stalk with drooping leaves.
        for y in range(SIZE - height, SIZE):
            c.put(cx, y, EMBER_LEAF[1] if y % 3 else EMBER_LEAF[0])
        for k in range(1, height // 3 + 1):
            y = SIZE - 3 * k
            side = 1 if k % 2 else -1
            for step in range(1, 3 + min(stage, 2)):
                c.put(cx + side * step, y + (step // 2), EMBER_LEAF[2] if step == 1 else EMBER_LEAF[1])
        if stage >= 2:
            # The pods hang under the top: dull at first, cracked open and hot when ripe.
            top = SIZE - height
            for px, py in ((cx - 1, top + 2), (cx + 1, top + 4)):
                for dy in range(3):
                    for dx in (0, 1):
                        colour = EMBER_POD[0] if stage == 2 else EMBER_POD[1]
                        if stage == 3 and dy == 1 and dx == 0:
                            colour = EMBER_POD[2]
                        c.put(px + dx * (1 if px > cx else -1), py + dy, colour)
    c.outline((24, 6, 6))
    return c.image


def glowroot_item():
    c = Canvas()
    # A tapering pale-gold root, rootlets trailing, a tuft of leaf at its crown.
    for y in range(5, 15):
        t = (y - 5) / 9.0
        half = round(3.2 * (1 - t) + 0.4)
        for dx in range(-half, half + 1):
            light = 0.5 + 0.5 * (-dx / max(1, half))
            c.put(8 + dx + round(t * 1.5), y, lerp(GLOW_BULB[0], GLOW_BULB[2], light * (1 - 0.4 * t)))
    for x, y in ((5, 12), (4, 13), (11, 12), (12, 13), (10, 15)):
        c.put(x, y, GLOW_BULB[0])
    for x, y in ((7, 4), (8, 3), (9, 2), (8, 4), (6, 3), (10, 3), (11, 2)):
        c.put(x, y, GLOW_LEAF[2] if y < 4 else GLOW_LEAF[1])
    c.put(7, 7, (255, 255, 236))
    c.put(8, 8, (255, 255, 236))
    c.outline((60, 46, 20))
    return c.image


def emberpod_item():
    c = Canvas()
    for y in range(3, 14):
        for x in range(4, 13):
            dx, dy = (x - 8) / 4.2, (y - 8.5) / 5.6
            if dx * dx + dy * dy > 1.0:
                continue
            light = 0.55 - dx * 0.35 - dy * 0.25
            c.put(x, y, lerp(EMBER_POD[0], EMBER_POD[1], light))
    # Cracks glowing hot through the husk.
    for x, y in ((7, 5), (8, 6), (8, 7), (9, 8), (8, 9), (7, 10), (10, 6), (6, 8)):
        c.put(x, y, EMBER_POD[2])
    c.put(8, 8, (255, 232, 160))
    for x, y in ((8, 2), (9, 1), (7, 2)):
        c.put(x, y, EMBER_LEAF[1])
    c.outline((40, 8, 4))
    return c.image


def tainted_harvest_item():
    """A lumpy root off over-fed ground: its skin bruised dark, a Harbinger's blood threaded through it in veins, and a
    pale sprout gone red at the tip."""
    c = Canvas()
    skin = [(78, 52, 38), (122, 86, 58), (164, 124, 84)]
    vein = [(110, 8, 24), (176, 22, 36)]
    lumps = [(7.5, 9.0, 5.2, 4.4), (10.5, 7.5, 3.0, 2.8), (5.0, 11.0, 3.0, 2.6)]
    for y in range(SIZE):
        for x in range(SIZE):
            inside = [(x - cx) ** 2 / rx ** 2 + (y - cy) ** 2 / ry ** 2 for cx, cy, rx, ry in lumps]
            if min(inside) > 1.0:
                continue
            light = 0.62 - (x - 7) * 0.05 - (y - 8) * 0.06
            colour = lerp(skin[0], skin[2], light)
            if (x * 7 + y * 3) % 11 == 0:
                colour = skin[0]
            c.put(x, y, colour)
    # Veins of blood through the skin.
    for x, y in ((4, 9), (5, 9), (6, 10), (7, 10), (8, 9), (9, 9), (10, 8), (11, 7), (8, 11), (9, 12), (6, 7), (7, 8)):
        if c.filled(x, y):
            c.put(x, y, vein[1] if (x + y) % 2 else vein[0])
    # A sprout from the crown.
    for x, y, colour in ((11, 4, (150, 160, 96)), (12, 3, (150, 160, 96)), (12, 2, (176, 40, 44)), (10, 4, (120, 132, 76))):
        c.put(x, y, colour)
    c.put(6, 7, (196, 156, 112))
    c.outline((34, 14, 10))
    return c.image


# ---------------------------------------------------------------- data

def strain_assets(name):
    block = name + '_strain'
    write_json(os.path.join(ASSETS, 'blockstates', block + '.json'),
               {'variants': {'age=%d' % stage: {'model': 'hemomancy:block/%s_stage%d' % (block, stage)} for stage in range(4)}})
    for stage in range(4):
        save(strain_stage(name, stage), 'block/strain/%s_stage%d' % (name, stage))
        write_json(os.path.join(ASSETS, 'models/block/%s_stage%d.json' % (block, stage)), {
            'parent': 'minecraft:block/cross',
            'render_type': 'minecraft:cutout',
            'textures': {'cross': 'hemomancy:block/strain/%s_stage%d' % (name, stage)}})
    spore = 'hemomancy:%s_spores' % name
    write_json(os.path.join(DATA, 'loot_table/blocks', block + '.json'), {
        'type': 'minecraft:block',
        'pools': [
            {'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
             'entries': [{'type': 'minecraft:item', 'name': spore}]},
            {'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [
                {'condition': 'minecraft:block_state_property', 'block': 'hemomancy:' + block, 'properties': {'age': '3'}},
                {'condition': 'minecraft:random_chance', 'chance': 0.5},
                {'condition': 'minecraft:survives_explosion'}],
             'entries': [{'type': 'minecraft:item', 'name': spore}]}],
        'random_sequence': 'hemomancy:blocks/' + block})


def timber_assets():
    save(timber_side(), 'block/salt_stained_timber')
    save(timber_top(), 'block/salt_stained_timber_top')
    textures = {'end': 'hemomancy:block/salt_stained_timber_top', 'side': 'hemomancy:block/salt_stained_timber'}
    write_json(os.path.join(ASSETS, 'models/block/salt_stained_timber.json'), {'parent': 'minecraft:block/cube_column', 'textures': textures})
    write_json(os.path.join(ASSETS, 'models/block/salt_stained_timber_horizontal.json'),
               {'parent': 'minecraft:block/cube_column_horizontal', 'textures': textures})
    write_json(os.path.join(ASSETS, 'blockstates/salt_stained_timber.json'), {'variants': {
        'axis=x': {'model': 'hemomancy:block/salt_stained_timber_horizontal', 'x': 90, 'y': 90},
        'axis=y': {'model': 'hemomancy:block/salt_stained_timber'},
        'axis=z': {'model': 'hemomancy:block/salt_stained_timber_horizontal', 'x': 90}}})
    write_json(os.path.join(ASSETS, 'models/item/salt_stained_timber.json'), {'parent': 'hemomancy:block/salt_stained_timber'})
    write_json(os.path.join(DATA, 'loot_table/blocks/salt_stained_timber.json'), {
        'type': 'minecraft:block',
        'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
                   'entries': [{'type': 'minecraft:item', 'name': 'hemomancy:salt_stained_timber'}]}],
        'random_sequence': 'hemomancy:blocks/salt_stained_timber'})
    write_json(os.path.join(DATA, 'recipe/salt_stained_timber_from_voyager_log.json'), {
        'type': 'minecraft:crafting_shapeless',
        'category': 'building',
        'ingredients': [{'item': 'hemomancy:salt_stained_voyager_log'}] + [{'tag': 'minecraft:logs'}] * 8,
        'result': {'id': 'hemomancy:salt_stained_timber', 'count': 8}})
    write_json(os.path.join(DATA, 'recipe/salt_stained_timber.json'), {
        'type': 'minecraft:crafting_shapeless',
        'category': 'building',
        'ingredients': [{'item': 'hemomancy:salt_stained_timber'}, {'item': 'minecraft:dried_kelp'}] + [{'tag': 'minecraft:logs'}] * 4,
        'result': {'id': 'hemomancy:salt_stained_timber', 'count': 5}})


def crop_assets(name, painter, item_painter, recipe):
    write_json(os.path.join(ASSETS, 'blockstates', name + '.json'),
               {'variants': {'age=%d' % stage: {'model': 'hemomancy:block/%s_stage%d' % (name, stage)} for stage in range(4)}})
    for stage in range(4):
        save(painter(stage), 'block/crop/%s_stage%d' % (name, stage))
        write_json(os.path.join(ASSETS, 'models/block/%s_stage%d.json' % (name, stage)), {
            'parent': 'minecraft:block/crop',
            'render_type': 'minecraft:cutout',
            'textures': {'crop': 'hemomancy:block/crop/%s_stage%d' % (name, stage)}})
    save(item_painter(), 'item/' + name)
    write_json(os.path.join(ASSETS, 'models/item', name + '.json'),
               {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'hemomancy:item/' + name}})
    item = 'hemomancy:' + name
    write_json(os.path.join(DATA, 'loot_table/blocks', name + '.json'), {
        'type': 'minecraft:block',
        'pools': [
            {'rolls': 1.0, 'bonus_rolls': 0.0, 'entries': [{'type': 'minecraft:item', 'name': item}]},
            {'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [
                {'condition': 'minecraft:block_state_property', 'block': item, 'properties': {'age': '3'}}],
             'entries': [{'type': 'minecraft:item', 'name': item, 'functions': [
                 {'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 1.0, 'max': 2.0}, 'add': False}]}]}],
        'functions': [{'function': 'minecraft:explosion_decay'}],
        'random_sequence': 'hemomancy:blocks/' + name})
    write_json(os.path.join(DATA, 'recipe', name + '.json'), {
        'type': 'minecraft:crafting_shapeless',
        'category': 'misc',
        'ingredients': [{'item': ingredient} for ingredient in recipe],
        'result': {'id': item}})


def insert_after(lang, anchor, entries):
    """Sets each entry, a new key going in just after {anchor} (the file keeps its own order) or at the end."""
    keys = list(lang.keys())
    fresh = [(k, v) for k, v in entries.items() if k not in lang]
    for k, v in entries.items():
        if k in lang:
            lang[k] = v
    if not fresh:
        return lang
    at = keys.index(anchor) + 1 if anchor in keys else len(keys)
    ordered = keys[:at] + [k for k, _ in fresh] + keys[at:]
    merged = dict(lang)
    merged.update(fresh)
    return {k: merged[k] for k in ordered}


def names():
    lang = json.loads(io.open(LANG, encoding='utf-8').read())
    blocks = {'block.hemomancy.%s_strain' % name: english for name, english in ENGLISH.items()}
    blocks['block.hemomancy.salt_stained_timber'] = 'Salt-Stained Timber'
    blocks['block.hemomancy.glowroot'] = 'Glowroot'
    blocks['block.hemomancy.emberpod'] = 'Emberpod'
    lang = insert_after(lang, 'block.hemomancy.ravenous_heartwood', blocks)
    lang = insert_after(lang, 'item.hemomancy.tainted_meat.tooltip', {
        'item.hemomancy.tainted_harvest': 'Tainted Harvest',
        'item.hemomancy.glowroot': 'Glowroot',
        'item.hemomancy.emberpod': 'Emberpod'})
    io.open(LANG, 'w', encoding='utf-8', newline='\n').write(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')


def main():
    for name in STRAINS:
        strain_assets(name)
    timber_assets()
    crop_assets('glowroot', glowroot_stage, glowroot_item, ['minecraft:carrot', 'minecraft:glowstone_dust'])
    crop_assets('emberpod', emberpod_stage, emberpod_item, ['minecraft:nether_wart', 'minecraft:blaze_powder'])
    save(tainted_harvest_item(), 'item/tainted_harvest')
    write_json(os.path.join(ASSETS, 'models/item/tainted_harvest.json'),
               {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'hemomancy:item/tainted_harvest'}})
    names()


if __name__ == '__main__':
    main()

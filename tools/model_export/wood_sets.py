"""Generates the Blood Wood and Heartwood building sets: blockstates, models, recipes, loot, tags, names and
textures, plus the Heartwood core and Ravenous Heartwood. Run from the repo root: python tools/model_export/wood_sets.py

Blockstates, models, recipes and loot are vanilla's oak files with the family's name swapped in, so every piece
behaves exactly like its vanilla counterpart. Door, trapdoor, sign and item textures are vanilla's oak shapes recoloured
through a family ramp; the Heartwood wood, the core and Ravenous Heartwood are painted here.
"""
import glob
import io
import json
import math
import os
import random
import re
import zipfile

from PIL import Image

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
ASSETS = os.path.join(ROOT, 'src/main/resources/assets/hemomancy')
DATA = os.path.join(ROOT, 'src/main/resources/data/hemomancy')
MC_TAGS = os.path.join(ROOT, 'src/main/resources/data/minecraft/tags')
TEX = os.path.join(ASSETS, 'textures')
LANG = os.path.join(ASSETS, 'lang/en_us.json')
JAR = glob.glob(os.path.expanduser('~/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'))[0]
VANILLA = zipfile.ZipFile(JAR)

FAMILIES = {'blood_wood': 'Blood Wood', 'heartwood': 'Heartwood'}
# Lightness ramps, dark to bright, that recolour vanilla's oak shapes for each family.
RAMPS = {
    'blood_wood': [(16, 12, 14), (44, 38, 40), (70, 44, 44), (118, 40, 42)],
    'heartwood': [(20, 5, 14), (50, 10, 34), (88, 15, 46), (170, 24, 42)],
}
STRIPPED_RAMP = [(30, 12, 14), (70, 26, 28), (108, 38, 38), (142, 52, 50)]
OAK_NAME = re.compile(r'minecraft:(blocks?/|item/)?((?:stripped_)?oak_[a-z_]+)')
SKIP = re.compile(r'(sapling|leaves|boat)')


def mapped_name(name, family):
    """A vanilla oak id as this family's id; vanilla's oak_wood is <family>_bark."""
    if name in ('oak_wood', 'stripped_oak_wood'):
        return name.replace('oak_wood', family + '_bark')
    return name.replace('oak', family, 1)


def swap(text, family):
    def replace(match):
        prefix, name = match.group(1) or '', match.group(2)
        return 'hemomancy:' + prefix + mapped_name(name, family)
    return OAK_NAME.sub(replace, text)


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    io.open(path, 'w', encoding='utf-8', newline='\n').write(json.dumps(obj, indent=2, ensure_ascii=False) + '\n')


def write_text(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    io.open(path, 'w', encoding='utf-8', newline='\n').write(text)


def vanilla_files(prefix):
    for name in VANILLA.namelist():
        if not name.startswith(prefix) or not name.endswith('.json'):
            continue
        base = os.path.basename(name)[:-5]
        if re.match(r'(stripped_)?oak_', base) and not SKIP.search(base):
            yield name, base


def copy_vanilla(family):
    routes = (('assets/minecraft/blockstates/', os.path.join(ASSETS, 'blockstates')),
              ('assets/minecraft/models/block/', os.path.join(ASSETS, 'models/block')),
              ('assets/minecraft/models/item/', os.path.join(ASSETS, 'models/item')),
              ('data/minecraft/recipe/', os.path.join(DATA, 'recipe')),
              ('data/minecraft/loot_table/blocks/', os.path.join(DATA, 'loot_table/blocks')))
    for prefix, target in routes:
        for name, base in vanilla_files(prefix):
            obj = json.loads(swap(VANILLA.read(name).decode('utf-8'), family))
            # Doors and trapdoors have windows; vanilla sets their layer in code, a mod sets it in the model.
            if 'models/block' in prefix and re.search(r'_(door|trapdoor)', base):
                obj['render_type'] = 'minecraft:cutout'
            write_json(os.path.join(target, mapped_name(base, family) + '.json'), obj)


def add_to_tag(tag_file, values):
    path = os.path.join(MC_TAGS, tag_file + '.json')
    obj = {'replace': False, 'values': []}
    if os.path.exists(path):
        obj = json.loads(io.open(path, encoding='utf-8-sig').read())
    for value in values:
        if value not in obj['values']:
            obj['values'].append(value)
    write_json(path, obj)


def write_tags(family):
    logs = ['hemomancy:%s_log' % family, 'hemomancy:%s_bark' % family, 'hemomancy:stripped_%s_log' % family, 'hemomancy:stripped_%s_bark' % family]
    for kind in ('block', 'item'):
        write_json(os.path.join(DATA, 'tags', kind, family + '_logs.json'), {'replace': False, 'values': logs})
        add_to_tag(kind + '/logs', ['#hemomancy:%s_logs' % family])
    shared = {'planks': '_planks', 'wooden_slabs': '_slab', 'wooden_stairs': '_stairs', 'wooden_fences': '_fence',
              'fence_gates': '_fence_gate', 'wooden_doors': '_door', 'wooden_trapdoors': '_trapdoor',
              'wooden_buttons': '_button', 'wooden_pressure_plates': '_pressure_plate'}
    for tag, suffix in shared.items():
        for kind in ('block', 'item'):
            add_to_tag(kind + '/' + tag, ['hemomancy:' + family + suffix])
    for tag, suffix in {'standing_signs': '_sign', 'wall_signs': '_wall_sign', 'ceiling_hanging_signs': '_hanging_sign',
                        'wall_hanging_signs': '_wall_hanging_sign'}.items():
        add_to_tag('block/' + tag, ['hemomancy:' + family + suffix])
    add_to_tag('item/signs', ['hemomancy:%s_sign' % family])
    add_to_tag('item/hanging_signs', ['hemomancy:%s_hanging_sign' % family])


def names_for(family, english):
    return {
        family + '_log': english + ' Log', family + '_bark': english + ' Bark',
        'stripped_%s_log' % family: 'Stripped %s Log' % english, 'stripped_%s_bark' % family: 'Stripped %s Bark' % english,
        family + '_planks': english + ' Planks', family + '_slab': english + ' Slab', family + '_stairs': english + ' Stairs',
        family + '_fence': english + ' Fence', family + '_fence_gate': english + ' Fence Gate', family + '_door': english + ' Door',
        family + '_trapdoor': english + ' Trapdoor', family + '_button': english + ' Button',
        family + '_pressure_plate': english + ' Pressure Plate', family + '_sign': english + ' Sign',
        family + '_hanging_sign': english + ' Hanging Sign',
    }


# ---------------------------------------------------------------- recolouring

def ramp_colour(ramp, t):
    t = max(0.0, min(1.0, t)) * (len(ramp) - 1)
    i = min(int(t), len(ramp) - 2)
    f = t - i
    return tuple(round(ramp[i][c] + (ramp[i + 1][c] - ramp[i][c]) * f) for c in range(3))


def recolour(image, ramp):
    image = image.convert('RGBA')
    pixels = image.load()
    lights = [sum(pixels[x, y][:3]) / 3 for y in range(image.height) for x in range(image.width) if pixels[x, y][3] > 0]
    low, high = min(lights), max(lights)
    span = max(1.0, high - low)
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            if a > 0:
                pixels[x, y] = ramp_colour(ramp, (sum((r, g, b)) / 3 - low) / span) + (a,)
    return image


def vanilla_image(path):
    return Image.open(io.BytesIO(VANILLA.read('assets/minecraft/textures/' + path)))


def save(image, rel, frametime=None):
    path = os.path.join(TEX, rel + '.png')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    image.save(path)
    if frametime is not None:
        write_text(path + '.mcmeta', json.dumps({'animation': {'interpolate': True, 'frametime': frametime}}, indent=2) + '\n')


def recoloured_set(family):
    ramp = RAMPS[family]
    for vanilla, ours in (('block/oak_door_top', 'block/%s_door_top'), ('block/oak_door_bottom', 'block/%s_door_bottom'),
                          ('block/oak_trapdoor', 'block/%s_trapdoor'), ('item/oak_door', 'item/%s_door'),
                          ('item/oak_sign', 'item/%s_sign'), ('item/oak_hanging_sign', 'item/%s_hanging_sign'),
                          ('entity/signs/oak', 'entity/signs/%s'), ('entity/signs/hanging/oak', 'entity/signs/hanging/%s'),
                          ('gui/hanging_signs/oak', 'gui/hanging_signs/%s')):
        save(recolour(vanilla_image(vanilla + '.png'), ramp), ours % family)


def stripped_blood_wood():
    """The stripped log keeps the live log's flow, with the grey bark gone to raw red wood."""
    for name, out in (('blood_wood_log', 'stripped_blood_wood_log'), ('blood_wood_log_top', 'stripped_blood_wood_log_top')):
        source = Image.open(os.path.join(TEX, 'block', name + '.png'))
        save(recolour(source, STRIPPED_RAMP), 'block/' + out, frametime=10)


# ---------------------------------------------------------------- painting

SIZE = 16
FRAMES = 8


def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def strip(painter, frames=FRAMES):
    image = Image.new('RGBA', (SIZE, SIZE * frames))
    pixels = image.load()
    for frame in range(frames):
        for y in range(SIZE):
            for x in range(SIZE):
                colour = painter(x, y, frame / frames)
                pixels[x, y + frame * SIZE] = colour if len(colour) == 4 else colour + (255,)
    return image


def wrap_distance(a, b):
    d = abs(a - b) % SIZE
    return min(d, SIZE - d)


class Veins:
    """Vertical veins that wander and tile: x(y) is periodic, and blood pulses down them as t runs."""

    def __init__(self, seed, count, bright, dark, edge):
        rng = random.Random(seed)
        # Evenly spaced so the veins meander side by side and rarely cross; a crossing reads as a checker at 16 pixels.
        self.paths = [(SIZE * (i + rng.uniform(0.3, 0.7)) / count, rng.uniform(0.7, 1.3), rng.uniform(0, 6.28)) for i in range(count)]
        self.bright, self.dark, self.edge = bright, dark, edge

    def at(self, x, y, t, width=0.55):
        best = 99.0
        for centre, amp, phase in self.paths:
            path = centre + amp * math.sin(2 * math.pi * y / SIZE + phase)
            # Each vein swells and pinches along its length, which reads as flow rather than a printed ribbon.
            swell = 0.75 + 0.45 * math.sin(2 * math.pi * y / SIZE + phase * 3)
            best = min(best, wrap_distance(x, path) / swell)
        if best < width:
            flow = 0.5 + 0.5 * math.sin(2 * math.pi * (2 * y / SIZE - t))
            return lerp(self.dark, self.bright, flow)
        return self.edge if best < width + 0.9 else None


def bark_value(x, y, seed):
    return 0.45 + 0.34 * math.sin(2 * math.pi * (3 * x / SIZE + 0.12 * math.sin(2 * math.pi * y / SIZE + seed))) \
        + 0.16 * math.sin(2 * math.pi * (7 * x / SIZE + y / SIZE + seed)) + 0.1 * math.sin(2 * math.pi * (3 * y / SIZE + 5 * x / SIZE))


RED = (238, 36, 40)
DARK_RED = (112, 8, 30)
OXBLOOD = (59, 10, 38)
OXBLOOD_HIGH = (92, 16, 50)
GREY_LOW = (50, 46, 56)
GREY_HIGH = (112, 106, 116)


def heartwood_side():
    veins = Veins(11, 3, RED, DARK_RED, OXBLOOD)

    def paint(x, y, t):
        vein = veins.at(x, y, t)
        return vein if vein else lerp(GREY_LOW, GREY_HIGH, bark_value(x, y, 1.3))
    return strip(paint)


def rings(x, y):
    return math.hypot(x - 7.5, y - 7.5)


def heartwood_top():
    def paint(x, y, t):
        r = rings(x, y)
        if r > 6.6:
            return lerp(GREY_LOW, GREY_HIGH, bark_value(x, y, 0.4))
        pulse = 0.5 + 0.5 * math.sin(2 * math.pi * (r / 5 - t))
        # End grain stays dim: a bending branch shows dozens of these, and a bright centre turns it into a string of eyes.
        return lerp(OXBLOOD, OXBLOOD_HIGH, 0.5 + 0.5 * math.sin(r * 2.6)) if int(r) % 3 else lerp(OXBLOOD, DARK_RED, pulse * 0.6)
    return strip(paint)


def stripped_heartwood_side():
    veins = Veins(23, 3, RED, DARK_RED, (78, 12, 42))

    def paint(x, y, t):
        vein = veins.at(x, y, t, width=0.7)
        return vein if vein else lerp(OXBLOOD, OXBLOOD_HIGH, bark_value(x, y, 2.2))
    return strip(paint)


def stripped_heartwood_top():
    def paint(x, y, t):
        r = rings(x, y)
        pulse = 0.5 + 0.5 * math.sin(2 * math.pi * (r / 6 - t))
        return lerp(OXBLOOD, OXBLOOD_HIGH, 0.5 + 0.5 * math.sin(r * 2.6)) if int(r) % 3 else lerp(OXBLOOD, DARK_RED, pulse * 0.6)
    return strip(paint)


def heartwood_planks():
    """Boards of oxblood with a low-contrast seam and running grain; a thread of blood pulses along each board."""
    def paint(x, y, t):
        board = y // 4
        row = y % 4
        grain = 0.5 + 0.28 * math.sin(2 * math.pi * (2 * x / SIZE + board * 0.37) + row * 1.7) + 0.14 * math.sin(2 * math.pi * (5 * x / SIZE + board * 0.61))
        base = lerp((48, 10, 32), (86, 16, 46), grain)
        if row == 3:
            return lerp(base, (30, 6, 22), 0.55)
        if (x + board * 7) % 16 == 0 and board % 2 == 0:
            return lerp(base, (30, 6, 22), 0.4)
        if row == 1:
            flow = 0.5 + 0.5 * math.sin(2 * math.pi * (x / SIZE + board * 0.25 - t))
            return lerp(base, (150, 22, 40), flow * flow * 0.8)
        return base
    return strip(paint)


CORE_RAYS = [(0.3, 7.5), (1.5, 6.5), (2.7, 7.0), (3.9, 6.0), (5.1, 7.5)]


def core_mask(x, y, stage):
    """How far the core's fissures have opened: a seam at first, then rays reaching the block's edge."""
    dx, dy = x - 7.5, y - 7.5
    r = math.hypot(dx, dy)
    if r < 1.2 + stage * 0.55:
        return True
    reach = stage * 1.7 + 0.5
    if r > reach:
        return False
    angle = math.atan2(dy, dx) % (2 * math.pi)
    for ray, _ in CORE_RAYS:
        wobble = ray + 0.22 * math.sin(r * 1.3 + ray * 4)
        delta = min(abs(angle - wobble), 2 * math.pi - abs(angle - wobble))
        if delta * r < 0.65 + stage * 0.09:
            return True
    return False


CORE_GLOW = [((58, 10, 34), (96, 16, 46)), ((88, 12, 40), (150, 18, 44)), ((128, 12, 36), (196, 20, 38)),
             ((168, 10, 28), (232, 26, 32)), ((150, 0, 12), (236, 8, 18))]
CORE_FRAMETIME = [14, 11, 8, 6, 4]


def core_glow(stage):
    low, high = CORE_GLOW[stage]

    def paint(x, y, t):
        if not core_mask(x, y, stage):
            return (0, 0, 0, 0)
        r = math.hypot(x - 7.5, y - 7.5)
        return lerp(low, high, 0.5 + 0.5 * math.sin(2 * math.pi * (r / 7 - t))) + (255,)
    return strip(paint)


def core_base():
    veins = Veins(37, 3, (150, 18, 40), DARK_RED, OXBLOOD)

    def paint(x, y, t):
        r = math.hypot(x - 7.5, y - 7.5)
        if r < 3.4:
            return lerp((18, 4, 12), OXBLOOD, r / 3.4)
        vein = veins.at(x, y, 0.0, width=0.45)
        return vein if vein else lerp(GREY_LOW, GREY_HIGH, bark_value(x, y, 3.1) * 0.75)
    return strip(paint, frames=1)


def ravenous_side():
    veins = Veins(53, 4, (252, 44, 42), (150, 6, 22), (72, 6, 24))

    def paint(x, y, t):
        vein = veins.at(x, y, t, width=0.9)
        return vein if vein else lerp((20, 3, 10), (46, 6, 22), bark_value(x, y, 4.2))
    return strip(paint)


def ravenous_top():
    def paint(x, y, t):
        r = rings(x, y)
        pulse = 0.5 + 0.5 * math.sin(2 * math.pi * (r / 4 - t))
        if r > 6.6:
            return lerp((20, 3, 10), (46, 6, 22), bark_value(x, y, 1.1))
        return lerp((120, 4, 18), (252, 44, 42), pulse) if int(r) % 2 == 0 else (28, 3, 12)
    return strip(paint)


def glow_of(image):
    """The texture's blood alone: only pixels that read as bright red stay, for a full-bright overlay on the wood."""
    glow = image.convert('RGBA')
    pixels = glow.load()
    for y in range(glow.height):
        for x in range(glow.width):
            r, g, b, _ = pixels[x, y]
            pixels[x, y] = (r, g, b, 255) if r > 95 and r > g * 3 else (0, 0, 0, 0)
    return glow


def save_with_glow(image, rel):
    save(image, rel, frametime=6)
    save(glow_of(image), rel + '_glow', frametime=6)


def heartwood_textures():
    save_with_glow(heartwood_side(), 'block/heartwood_log')
    save(heartwood_top(), 'block/heartwood_log_top', frametime=6)
    save_with_glow(stripped_heartwood_side(), 'block/stripped_heartwood_log')
    save(stripped_heartwood_top(), 'block/stripped_heartwood_log_top', frametime=6)
    save(heartwood_planks(), 'block/heartwood_planks', frametime=8)
    save(core_base(), 'block/heartwood_core')
    for stage in range(5):
        save(core_glow(stage), 'block/heartwood_core_glow_%d' % stage, frametime=CORE_FRAMETIME[stage])
    save(ravenous_side(), 'block/ravenous_heartwood', frametime=5)
    save(ravenous_top(), 'block/ravenous_heartwood_top', frametime=5)


# ---------------------------------------------------------------- the core and Ravenous Heartwood

def core_assets():
    faces = ('down', 'up', 'north', 'south', 'west', 'east')
    write_json(os.path.join(ASSETS, 'blockstates/heartwood_core.json'),
               {'variants': {'fed=%d' % s: {'model': 'hemomancy:block/heartwood_core_%d' % s} for s in range(5)}})
    for stage in range(5):
        write_json(os.path.join(ASSETS, 'models/block/heartwood_core_%d.json' % stage), {
            'parent': 'minecraft:block/block',
            'render_type': 'minecraft:cutout',
            'textures': {'particle': 'hemomancy:block/heartwood_core', 'base': 'hemomancy:block/heartwood_core',
                         'glow': 'hemomancy:block/heartwood_core_glow_%d' % stage},
            'elements': [
                {'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {f: {'texture': '#base', 'cullface': f} for f in faces}},
                {'from': [0, 0, 0], 'to': [16, 16, 16], 'neoforge_data': {'block_light': 15, 'sky_light': 15},
                 'faces': {f: {'texture': '#glow', 'cullface': f} for f in faces}},
            ],
        })
    # The icon shows a core with its fissures open; stage 0 would read as a plain log.
    write_json(os.path.join(ASSETS, 'models/item/heartwood_core.json'), {'parent': 'hemomancy:block/heartwood_core_2'})
    write_json(os.path.join(DATA, 'loot_table/blocks/heartwood_core.json'), {
        'type': 'minecraft:block',
        'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [{'condition': 'minecraft:survives_explosion'}], 'entries': [{
            'type': 'minecraft:alternatives',
            'children': [
                {'type': 'minecraft:item', 'name': 'hemomancy:ravenous_heartwood', 'conditions': [{
                    'condition': 'minecraft:block_state_property', 'block': 'hemomancy:heartwood_core', 'properties': {'fed': '4'}}]},
                {'type': 'minecraft:item', 'name': 'hemomancy:heartwood_log'},
            ]}]}],
        'random_sequence': 'hemomancy:blocks/heartwood_core',
    })
    write_json(os.path.join(ASSETS, 'blockstates/ravenous_heartwood.json'), {'variants': {
        'axis=x': {'model': 'hemomancy:block/ravenous_heartwood_horizontal', 'x': 90, 'y': 90},
        'axis=y': {'model': 'hemomancy:block/ravenous_heartwood'},
        'axis=z': {'model': 'hemomancy:block/ravenous_heartwood_horizontal', 'x': 90}}})
    textures = {'end': 'hemomancy:block/ravenous_heartwood_top', 'side': 'hemomancy:block/ravenous_heartwood'}
    write_json(os.path.join(ASSETS, 'models/block/ravenous_heartwood.json'), {'parent': 'minecraft:block/cube_column', 'textures': textures})
    write_json(os.path.join(ASSETS, 'models/block/ravenous_heartwood_horizontal.json'), {'parent': 'minecraft:block/cube_column_horizontal', 'textures': textures})
    write_json(os.path.join(ASSETS, 'models/item/ravenous_heartwood.json'), {'parent': 'hemomancy:block/ravenous_heartwood'})
    write_json(os.path.join(DATA, 'loot_table/blocks/ravenous_heartwood.json'), {
        'type': 'minecraft:block',
        'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
                   'entries': [{'type': 'minecraft:item', 'name': 'hemomancy:ravenous_heartwood'}]}],
        'random_sequence': 'hemomancy:blocks/ravenous_heartwood',
    })
    add_to_tag('block/mineable/axe', ['hemomancy:ravenous_heartwood', 'hemomancy:heartwood_core'])


def log_faces(prefix, horizontal, bark=False):
    """Vanilla's column faces; the horizontal model's up face is turned so the grain meets the block state's rotation."""
    faces = {}
    for face in ('down', 'up', 'north', 'south', 'west', 'east'):
        end = face in ('down', 'up')
        if end and prefix and not bark:
            continue
        entry = {'texture': '#' + prefix + ('side' if prefix or not end else 'end'), 'cullface': face}
        if horizontal and face == 'up':
            entry['rotation'] = 180
        faces[face] = entry
    return faces


def emissive_logs():
    """Heartwood logs and bark carry a full-bright copy of their veins, so the wood runs red in the dark."""
    for stem, side, end in (('heartwood_log', 'heartwood_log', 'heartwood_log_top'),
                            ('heartwood_bark', 'heartwood_log', 'heartwood_log'),
                            ('stripped_heartwood_log', 'stripped_heartwood_log', 'stripped_heartwood_log_top'),
                            ('stripped_heartwood_bark', 'stripped_heartwood_log', 'stripped_heartwood_log')):
        for name, horizontal in ((stem, False), (stem + '_horizontal', True)):
            if horizontal and stem.endswith('_bark'):
                continue
            write_json(os.path.join(ASSETS, 'models/block', name + '.json'), {
                'parent': 'minecraft:block/block',
                'render_type': 'minecraft:cutout',
                'textures': {'particle': 'hemomancy:block/' + side, 'side': 'hemomancy:block/' + side, 'end': 'hemomancy:block/' + end,
                             'glow_side': 'hemomancy:block/' + side + '_glow'},
                'elements': [
                    {'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': log_faces('', horizontal)},
                    {'from': [0, 0, 0], 'to': [16, 16, 16], 'neoforge_data': {'block_light': 15, 'sky_light': 15},
                     'faces': log_faces('glow_', horizontal, side == end)},
                ],
            })


def write_names():
    lang = json.loads(io.open(LANG, encoding='utf-8').read())
    for family, english in FAMILIES.items():
        for block, name in names_for(family, english).items():
            lang['block.hemomancy.' + block] = name
    lang['block.hemomancy.heartwood_core'] = 'Heartwood Core'
    lang['block.hemomancy.ravenous_heartwood'] = 'Ravenous Heartwood'
    write_text(LANG, json.dumps(dict(sorted(lang.items())), indent=2, ensure_ascii=False) + '\n')


def main():
    heartwood_textures()
    stripped_blood_wood()
    for family in FAMILIES:
        copy_vanilla(family)
        write_tags(family)
        recoloured_set(family)
    core_assets()
    emissive_logs()
    write_names()
    print('wood sets written')


if __name__ == '__main__':
    main()

"""Author all three Enzymatic Scriptorium stages. Run from any directory with Python 3 + Pillow.

Writes the baked block models, the inventory model, editable Blockbench projects and the
generated ScriptoriumGeometry.java (jar positions, emissive rune quads and feed-line paths the
block entity renderer draws). Coordinates are model pixels for the north-facing block; the
reader stands to the north (low z) and the eight enzyme jars arc around the back.
"""

import base64
import copy
import io
import json
import math
from pathlib import Path
import uuid
import zipfile

from PIL import Image

import coplanar

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/hemomancy"
MODELS = ASSETS / "models/block"
SOURCE = MODELS / "bbmodel"
GLOW_SOURCE = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/ScriptoriumGeometry.java'
VANILLA_JAR = ROOT / 'build/moddev/artifacts/neoforge-21.1.251-client-extra-aka-minecraft-resources.jar'
STAGES = [('enzymatic_scriptorium', 0), ('eightfold_scriptorium', 1), ('palimpsest_scriptorium', 2)]

TEXTURES = {
    'particle': 'hemomancy:block/blood_wood_planks',
    'wood': 'hemomancy:block/blood_wood_planks',
    'log': 'hemomancy:block/blood_wood_log',
    'log_top': 'hemomancy:block/blood_wood_log_top',
    'iron': 'hemomancy:block/hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'glass': 'hemomancy:block/sanguine_tran_pane',
    'bulb': 'hemomancy:block/sanguine_glass',
    'cap': 'hemomancy:block/vial_cap',
    'crystal': 'hemomancy:block/sanguine_glass',
    'lacquer': 'hemomancy:block/polished_venous_stone',
    'bone': 'minecraft:block/bone_block_side',
    'bone_top': 'minecraft:block/bone_block_top',
    'vane': 'hemomancy:block/calcified_hyphae',
    'paper': 'hemomancy:entity/liber_sanguinum',
}
CAPS = {'log': 'log_top', 'bone': 'bone_top'}
# Drawn translucent: two coplanar panes blend twice even when their texels match.
TRANSLUCENT = {'glass', 'bulb', 'crystal'}
# The Liber atlas holds a 24x8 strip of written parchment at texel (0, 10).
PAPER_ORIGIN, PAPER_SIZE = (0, 10), (24, 8)
SIZES = {"north": (0, 1), "south": (0, 1), "east": (2, 1), "west": (2, 1), "up": (0, 2), "down": (0, 2)}

# Small authored glyphs, shared by the baked assets and the generated emissive mesh.
GLYPHS = ['010/111/010/010/101', '100/110/101/110/100',
          '111/001/010/100/111', '101/101/111/010/010',
          '010/101/010/101/010', '111/100/110/100/111',
          '101/010/111/010/101', '110/010/111/010/011']

# Enzyme jars sit on a horseshoe of radial arms around the lectern stem, open toward the reader.
# Index order matches the station's reservoir slots: west side, around the back, to the east side.
HUB = (8.0, 8.5)
JAR_RADIUS = 10.0
JAR_ANGLES = [180, 157.5, 135, 112.5, 67.5, 45, 22.5, 0]
JAR_HALF = 1.25
GLASS_BOTTOM, GLASS_TOP = 13.0, 18.0
FLUID_BOTTOM, FLUID_TOP, FLUID_HALF = 13.25, 17.75, 1.0
ARM_Y = (7.5, 8.25)
LINE_Y = 8.5
RISER = (8.0, 18.5)
DESK_ROTATION = {'origin': [8, 11, 8], 'axis': 'x', 'angle': -22.5}
# The rubricator's quill is authored along +z and tilted -45 degrees about x around this centre.
QUILL_CENTRE = (8.0, 23.0, 9.0)


def q(value):
    return round(value * 4) / 4


def polar(angle, radius):
    a = math.radians(angle)
    return HUB[0] + radius * math.cos(a), HUB[1] + radius * math.sin(a)


JARS = [tuple(map(q, polar(a, JAR_RADIUS))) for a in JAR_ANGLES]


def texture_path(resource):
    namespace, path = resource.split(':')
    return namespace, f'assets/{namespace}/textures/{path}.png'


def texture_bytes(resource):
    namespace, path = texture_path(resource)
    if namespace == 'hemomancy':
        return (ROOT / 'src/main/resources' / path).read_bytes()
    with zipfile.ZipFile(VANILLA_JAR) as jar:
        return jar.read(path)


def texture_image(resource):
    return Image.open(io.BytesIO(texture_bytes(resource)))


def texture_size(resource):
    """Atlas size plus one animation frame's size."""
    image = texture_image(resource)
    width, height = image.size
    namespace, path = texture_path(resource)
    meta = ROOT / 'src/main/resources' / (path + '.mcmeta')
    frame = width, height
    if namespace == 'hemomancy' and meta.exists():
        animation = json.loads(meta.read_text())['animation']
        frame = animation.get('width', min(width, height)), animation.get('height', animation.get('width', min(width, height)))
    return width, height, frame[0], frame[1]


def face_uv(side, a, b, material):
    """One texture pixel per model unit; ordinary materials stay world-aligned across neighbours."""
    dims = [hi - lo for lo, hi in zip(a, b)]
    w, h = dims[SIZES[side][0]], dims[SIZES[side][1]]
    assert w <= 16 and h <= 16, ('face exceeds one texture tile', side, a, b)
    if material == 'paper':
        assert w <= PAPER_SIZE[0] and h <= PAPER_SIZE[1], ('vellum larger than the parchment strip', a, b)
        width, height, _, _ = texture_size(TEXTURES['paper'])
        u0, v0 = PAPER_ORIGIN
        return [u0 * 16 / width, v0 * 16 / height, (u0 + w) * 16 / width, (v0 + h) * 16 / height]
    if side in ('north', 'south'):
        spans = (a[0], w), (16 - b[1], h)
    elif side in ('east', 'west'):
        spans = (a[2], w), (16 - b[1], h)
    else:
        spans = (a[0], w), (a[2], h)
    out = []
    for lo, size in spans:
        start = lo % 16
        if start + size > 16:
            start = 16 - size
        out.append((round(start, 4), round(start + size, 4)))
    return [out[0][0], out[1][0], out[0][1], out[1][1]]


class Model:
    def __init__(self):
        self.elements = []
        self.glow = []      # (element, stage key, visible face)
        self.items = []     # inventory-only static book

    def box(self, name, a, b, mat, rot=None, glow=None, face=None, shade=True):
        a = [round(v, 4) for v in a]
        b = [round(v, 4) for v in b]
        assert all(hi > lo for lo, hi in zip(a, b)), (name, a, b)
        faces = {}
        for side in ('north', 'south', 'east', 'west', 'up', 'down'):
            m = CAPS.get(mat, mat) if side in ('up', 'down') else mat
            faces[side] = {'texture': '#' + m, 'uv': face_uv(side, a, b, m)}
        e = {'name': name, 'from': a, 'to': b, 'faces': faces}
        if not shade:
            e['shade'] = False
        if rot:
            e['rotation'] = copy.deepcopy(rot)
        self.elements.append(e)
        if glow:
            self.glow.append((e, glow, face or 'up'))
        return e

    def bar(self, name, p0, p1, y0, y1, width, mat):
        """Horizontal bar between two xz points, using a 22.5-degree multiple model rotation."""
        dx, dz = p1[0] - p0[0], p1[1] - p0[1]
        angle = math.degrees(math.atan2(dz, dx))
        folded = ((angle + 90) % 180) - 90
        length = round(math.hypot(dx, dz) * 2) / 2
        cx, cz = q((p0[0] + p1[0]) / 2), q((p0[1] + p1[1]) / 2)
        if abs(folded) <= 45.01:
            theta = -folded
            a, b = [cx - length / 2, y0, cz - width / 2], [cx + length / 2, y1, cz + width / 2]
        else:
            theta = ((90 - folded) + 90) % 180 - 90
            a, b = [cx - width / 2, y0, cz - length / 2], [cx + width / 2, y1, cz + length / 2]
        theta = round(theta, 4)
        assert theta in (-45, -22.5, 0, 22.5, 45), (name, angle, theta)
        rot = {'origin': [cx, (y0 + y1) / 2, cz], 'axis': 'y', 'angle': theta} if theta else None
        return self.box(name, a, b, mat, rot)

    def glyph(self, name, pattern, x, y, z, unit=.5, rot=None, glow='rubric'):
        """Pixel glyph lying flat; each run of lit pixels becomes one thin slab."""
        for row, pixels in enumerate(pattern.split('/')):
            col = 0
            while col < len(pixels):
                if pixels[col] == '0':
                    col += 1
                    continue
                end = col + 1
                while end < len(pixels) and pixels[end] == '1':
                    end += 1
                self.box(f'{name}_{row}_{col}', [x + col * unit, y, z + row * unit],
                         [x + end * unit, y + .25, z + (row + 1) * unit], 'crystal', rot, glow, 'up', False)
                col = end

    def wall_glyph(self, name, pattern, x, outward, top, z, unit=.25, rot=None, glow='palimpsest'):
        """Pixel glyph scratched into a vertical vellum leaf facing +x or -x."""
        side = 'east' if outward > 0 else 'west'
        for row, pixels in enumerate(pattern.split('/')):
            col = 0
            while col < len(pixels):
                if pixels[col] == '0':
                    col += 1
                    continue
                end = col + 1
                while end < len(pixels) and pixels[end] == '1':
                    end += 1
                x0 = x if outward > 0 else x - .25
                self.box(f'{name}_{row}_{col}', [x0, top - (row + 1) * unit, z + col * unit],
                         [x0 + .25, top - row * unit, z + end * unit], 'crystal', rot, glow, side, False)
                col = end


# ---------------------------------------------------------------- shared skeleton

def lectern(m, stage):
    if stage == 0:
        m.box('lectern_foot', [1, 0, 1], [15, 1.5, 15], 'wood')
        m.box('foot_iron_band', [.75, .5, .75], [15.25, 1, 15.25], 'iron')
        m.box('foot_bevel', [2, 1.5, 2], [14, 2.5, 14], 'wood')
    else:
        octagon(m, 'plinth_lower', 7.25, 0, 1.5, 'wood')
        octagon(m, 'plinth_band', 7.5, .5, 1, 'iron')
        octagon(m, 'plinth_upper', 6.25, 1.5, 2.5, 'chisel')
    m.box('bloodwood_stem', [5.5, 2.5, 6], [10.5, 10.5, 11], 'log')
    m.box('lower_collar', [5, 3, 5.5], [11, 4, 11.5], 'iron')
    m.box('upper_collar', [5, 9.25, 5.5], [11, 10.25, 11.5], 'iron')
    m.box('sloped_reading_desk', [1, 10.5, 2], [15, 12, 13], 'wood', DESK_ROTATION)
    m.box('front_book_rest', [1, 12, 2], [15, 12.75, 3], 'iron' if stage else 'wood', DESK_ROTATION)
    if stage:
        for side, x in [('left', 1), ('right', 14.5)]:
            m.box(side + '_desk_edge', [x, 12, 3], [x + .5, 12.5, 13], 'lacquer', DESK_ROTATION)
    # Denaturing socket: a Blood Crystal Shard held in an iron cup on the stem's reader side.
    m.box('shard_cup', [7, 5, 4.75], [9, 5.75, 6], 'iron')
    m.box('shard_crystal', [7.5, 5.75, 5], [8.5, 7.5, 5.75], 'crystal', shade=False)


def octagon(m, name, radius, y0, y1, mat):
    """Regular octagonal slab centred on the hub: a cross plus four chamfer bars."""
    cx, cz = HUB
    flat = q(radius * math.tan(math.radians(22.5)))
    m.box(name + '_ns', [cx - radius, y0, cz - flat], [cx + radius, y1, cz + flat], mat)
    m.box(name + '_ew', [cx - flat, y0, cz - radius], [cx + flat, y1, cz + radius], mat)
    length = round((radius - flat) * math.sqrt(2) * 2) / 2
    width = round((radius - flat) / math.sqrt(2) * 2) / 2
    d = q((radius + flat) / 2 - width / (2 * math.sqrt(2)))
    for i, (sx, sz) in enumerate([(1, 1), (-1, 1), (-1, -1), (1, -1)]):
        x, z = cx + sx * d, cz + sz * d
        m.box(f'{name}_chamfer_{i}', [x - length / 2, y0, z - width / 2], [x + length / 2, y1, z + width / 2], mat,
              {'origin': [x, (y0 + y1) / 2, z], 'axis': 'y', 'angle': 45 if sx * sz > 0 else -45})


def jar_feed(m, stage):
    """Eight enzyme jars on radial arms, each feeding a glass capillary back to the stem hub."""
    cx, cz = HUB
    m.box('feed_hub', [4.75, 7.25, 5.25], [11.25, 8.75, 11.75], 'chisel' if stage else 'iron')
    rim_mat = 'bone' if stage == 2 else 'iron'
    for i, (angle, (x, z)) in enumerate(zip(JAR_ANGLES, JARS)):
        inner = polar(angle, 3)
        m.bar(f'jar_{i}_arm', inner, polar(angle, JAR_RADIUS + .5), *ARM_Y, 1, 'iron')
        m.bar(f'jar_{i}_line', polar(angle, 3.5), polar(angle, JAR_RADIUS - .5), ARM_Y[1], ARM_Y[1] + .5, .5, 'glass')
        lx, lz = map(q, polar(angle, JAR_RADIUS - .75))
        m.box(f'jar_{i}_drop', [lx - .25, ARM_Y[1], lz - .25], [lx + .25, 12.5, lz + .25], 'glass')
        m.box(f'jar_{i}_post', [x - .5, ARM_Y[0], z - .5], [x + .5, 12.5, z + .5], 'pillar')
        m.box(f'reservoir_{i}_cradle', [x - 1.5, 12.5, z - 1.5], [x + 1.5, 13, z + 1.5], rim_mat)
        m.box(f'reservoir_{i}_glass', [x - JAR_HALF, GLASS_BOTTOM, z - JAR_HALF], [x + JAR_HALF, GLASS_TOP, z + JAR_HALF], 'glass')
        m.box(f'reservoir_{i}_lid', [x - 1.5, GLASS_TOP, z - 1.5], [x + 1.5, GLASS_TOP + .5, z + 1.5],
              'lacquer' if stage else 'cap')
        m.box(f'reservoir_{i}_stopper', [x - .5, GLASS_TOP + .5, z - .5], [x + .5, GLASS_TOP + 1.25, z + .5], 'log')
        if stage == 2:
            m.box(f'reservoir_{i}_bone_band', [x - 1.5, 14.75, z - 1.5], [x + 1.5, 15.25, z + 1.5], 'bone')
        if stage:
            # Eightfold petcock: a metering valve on each post with a lacquered tangent lever.
            m.box(f'jar_{i}_petcock', [x - .75, 9.75, z - .75], [x + .75, 11, z + .75], 'iron')
            lever(m, i, angle, x, z)
    # The back arm carries the merged feed to the riser that rises behind the book.
    m.box('riser_arm', [7.5, ARM_Y[0], 11.75], [8.5, ARM_Y[1], RISER[1] + .5], 'iron')
    m.box('riser_line', [7.75, ARM_Y[1], 11.75], [8.25, ARM_Y[1] + .5, RISER[1] - .75], 'glass')


def lever(m, i, angle, x, z):
    tangent = angle + 90
    tx, tz = math.cos(math.radians(tangent)) * 1.25, math.sin(math.radians(tangent)) * 1.25
    m.bar(f'jar_{i}_lever', (x - tx, z - tz), (x + tx, z + tz), 11, 11.5, .5, 'lacquer')


def riser(m, stage):
    x, z = RISER
    bottom = 11.5 if stage == 2 else ARM_Y[1]
    m.box('riser_glass', [x - .75, bottom, z - .75], [x + .75, 21.5, z + .75], 'glass')
    m.box('riser_foot', [x - 1, ARM_Y[0], z - 1], [x + 1, ARM_Y[1] + .25, z + 1], 'iron')
    bands = [12, 16.5, 20.5] if stage else [12.5, 18]
    if stage == 2:
        bands = [12.5, 14.5, 16.5, 18.5, 20.5]
    for i, y in enumerate(bands):
        m.box(f'riser_band_{i}', [x - 1, y, z - 1], [x + 1, y + .75, z + 1], 'bone' if stage == 2 else 'iron')
    m.box('riser_cap', [x - 1.25, 21.5, z - 1.25], [x + 1.25, 22.5, z + 1.25], 'lacquer' if stage else 'iron')
    if stage == 0:
        # Plain spout: the merged feed runs forward over the desk and drips at the book's spine.
        m.box('spout_line', [x - .25, 20.75, 11], [x + .25, 21.25, z - .75], 'glass')
        m.box('spout_strap', [x - .5, 20.5, 14.5], [x + .5, 21.5, 15.25], 'iron')
        m.box('spout_nib', [x - .25, 20.75, 9.75], [x + .25, 21.25, 12.25], 'iron',
              {'origin': [x, 21, 11], 'axis': 'x', 'angle': -45})
    if stage == 2:
        # Palimpsest mixing bulb at the base of the spine, caged in bone.
        m.box('mixing_bulb', [x - 1.5, 8.5, z - 1.5], [x + 1.5, 11.5, z + 1.5], 'bulb')
        for i, (sx, sz) in enumerate([(-1, -1), (1, -1), (1, 1), (-1, 1)]):
            px, pz = x + sx * 1.5, z + sz * 1.5
            m.box(f'bulb_rib_{i}', [px - .25, 8.25, pz - .25], [px + .25, 11.75, pz + .25], 'bone')
        m.box('bulb_crown', [x - 1.25, 11.5, z - 1.25], [x + 1.25, 12, z + 1.25], 'bone')


def quill_parts(m):
    """Rubricator's quill: glass barrel, hyphal vane and lacquered nib on a hematic gooseneck."""
    x, z = RISER
    spine = 'bone' if m.stage == 2 else 'iron'
    m.box('gooseneck_rise', [x - .5, 22.5, z - .5], [x + .5, 27, z + .5], spine)
    m.box('gooseneck_line_rise', [x - .25, 22.5, z - 1], [x + .25, 26, z - .5], 'glass')
    m.box('gooseneck_reach', [x - .5, 26, 11.5], [x + .5, 27, z + .5], spine)
    m.box('gooseneck_line_reach', [x - .25, 25.5, 11.75], [x + .25, 26, z - .5], 'glass')
    m.box('quill_clamp', [x - .75, 25.25, 11.25], [x + .75, 26, 12.25], 'lacquer')
    qx, qy, qz = QUILL_CENTRE
    rot = {'origin': [qx, qy, qz], 'axis': 'x', 'angle': -45}
    m.box('quill_barrel', [qx - .25, qy - .25, qz - 3.75], [qx + .25, qy + .25, qz + 3.75], 'glass', rot)
    m.box('quill_nib', [qx - .25, qy - .25, qz - 4.25], [qx + .25, qy + .25, qz - 3.5], 'lacquer', rot)
    m.box('quill_binding', [qx - .5, qy - .5, qz - 1], [qx + .5, qy + .5, qz - .5], 'lacquer', rot)
    # A tapered, asymmetric vane: broad trailing side, narrow leading side.
    for i, (z0, z1, width) in enumerate([(-.5, 1, 1.25), (1, 2.75, 1.75), (2.75, 4, 1.25), (4, 4.75, .5)]):
        m.box(f'quill_vane_broad_{i}', [qx + .25, qy - .25, qz + z0], [qx + .25 + width, qy, qz + z1], 'vane', rot)
    m.box('quill_vane_narrow', [qx - .75, qy - .25, qz + .5], [qx - .25, qy, qz + 3.75], 'vane', rot)


def runes(m):
    """Eight emissive ground runes ring the plinth, and a red rubric is ruled across the desk."""
    for i in range(8):
        angle = 22.5 + 45 * i
        x, z = map(q, polar(angle, 9.75))
        m.glyph(f'ground_rune_{i}', GLYPHS[i], x - .75, 0, z - 1.25)
    for i in range(6):
        m.glyph(f'book_script_{i}', GLYPHS[i], 2.25 + i * 2, 12.25, 3.25, unit=.25, rot=DESK_ROTATION)


def ribcage(m):
    """Palimpsest hood: bone ribs branch from the spine and curl forward over the grimoire."""
    x, z = RISER
    for level, (y, reach) in enumerate([(20.5, 6.25), (24, 4)]):
        for side, sign in (('left', -1), ('right', 1)):
            cx, cz = x + sign * reach, z - reach
            m.bar(f'rib_{level}_{side}_branch', (x, z), (cx, cz), y, y + .75, .5, 'bone')
            m.box(f'rib_{level}_{side}_reach', [cx - .25, y, 9.25], [cx + .25, y + .75, cz + .25], 'bone')
            # The rib's tip bends down toward the page; the upper-left finger is tipped as the burin.
            curl = {'origin': [cx, y + .375, 9.25], 'axis': 'x', 'angle': -45}
            m.box(f'rib_{level}_{side}_curl', [cx - .25, y, 6.25], [cx + .25, y + .75, 9.25], 'bone', curl)
            if level == 1 and sign < 0:
                m.box('burin_ferrule', [cx - .5, y, 5.75], [cx + .5, y + .75, 6.25], 'lacquer', curl)
                m.box('burin_shaft', [cx - .25, y + .25, 3.75], [cx + .25, y + .75, 5.75], 'iron', curl)
                m.box('burin_point', [cx - .25, y + .25, 3.25], [cx + .25, y + .5, 3.75], 'iron', curl)
    # Scraped leaves hang beneath the lower ribs, two offset layers per side, clear of the jars.
    for side, sign in (('left', -1), ('right', 1)):
        inner_x = x + sign * 6.25 - (.25 if sign < 0 else 0)
        outer_x = inner_x + sign * .5
        m.box(f'vellum_{side}_inner', [inner_x, 15.5, 9.25], [inner_x + .25, 20.5, 13.75], 'paper')
        m.box(f'vellum_{side}_outer', [outer_x, 14.75, 9.75], [outer_x + .25, 19.25, 13.75], 'paper')
        face_x = outer_x if sign < 0 else outer_x + .25
        for g in range(2):
            m.wall_glyph(f'vellum_{side}_scratch_{g}', GLYPHS[2 + g * 3 + (sign > 0)],
                         face_x, sign, 18.75, 10 + g * 2, unit=.5)


def build(stage):
    m = Model()
    m.stage = stage
    lectern(m, stage)
    jar_feed(m, stage)
    riser(m, stage)
    if stage >= 1:
        quill_parts(m)
        runes(m)
    if stage == 2:
        ribcage(m)
    return m


# ---------------------------------------------------------------- feed paths for the renderer

def feed_path(i):
    angle = JAR_ANGLES[i]
    x, z = JARS[i]
    lx, lz = map(q, polar(angle, JAR_RADIUS - .75))
    hx, hz = polar(angle, 3.5)
    return [(x, FLUID_BOTTOM, z), (lx, 12.5, lz), (lx, LINE_Y, lz), (hx, LINE_Y, hz)]


def tail_path(stage):
    x, z = RISER
    path = [(x, LINE_Y, HUB[1] + 3.5), (x, LINE_Y, z), (x, 21, z)]
    if stage == 0:
        return path + [(x, 21, 11), (x, 20, 10)]
    qx, qy, qz = QUILL_CENTRE
    nib = (qx, qy - 4 * math.sqrt(.5), qz - 4 * math.sqrt(.5))
    top = (qx, qy + 3.75 * math.sqrt(.5), qz + 3.75 * math.sqrt(.5))
    return path + [(x, 21.75, z - .75), (x, 25.75, z - .75), (x, 25.75, top[2]), top, nib]


# ---------------------------------------------------------------- export

def to_json(m):
    used = sorted({f['texture'][1:] for e in m.elements for f in e['faces'].values()})
    textures = {'particle': TEXTURES['particle']}
    textures.update({k: TEXTURES[k] for k in used})
    model = {'parent': 'minecraft:block/block', 'render_type': 'translucent', 'ambientocclusion': False,
             'textures': textures, 'elements': copy.deepcopy(m.elements)}
    model['display'] = {'gui': {'rotation': [30, 225, 0], 'translation': [0, -3, 0], 'scale': [.42, .42, .42]},
                        'ground': {'translation': [0, 2, 0], 'scale': [.3, .3, .3]},
                        'fixed': {'scale': [.45, .45, .45]}}
    return model


def static_book():
    """Inventory stand-in for the renderer's floating grimoire."""
    rot = {'origin': [8, 15.5, 6], 'axis': 'x', 'angle': -45}
    out = []
    for name, a, b, uv in [('red_liber_cover', [2, 15.5, 2], [8, 16, 10], [0, 0, 6, 5]),
                           ('liber_pages', [2.5, 16, 2.5], [7.75, 16.75, 9.5], [1, 6, 6, 10]),
                           ('red_liber_cover', [8, 15.5, 2], [14, 16, 10], [0, 0, 6, 5]),
                           ('liber_pages', [8.25, 16, 2.5], [13.5, 16.75, 9.5], [1, 6, 6, 10])]:
        out.append({'name': name, 'from': a, 'to': b,
                    'faces': {s: {'texture': '#book', 'uv': uv} for s in SIZES}, 'rotation': copy.deepcopy(rot)})
    return out


def blockbench(name, model):
    textures, indices = [], {}
    for key, resource in model['textures'].items():
        if key == 'particle':
            continue
        namespace, path = texture_path(resource)
        width, height, _, _ = texture_size(resource)
        indices[key] = len(textures)
        textures.append({'name': Path(path).name, 'id': key,
                         'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, resource + '/' + key)),
                         'uv_width': width, 'uv_height': height,
                         'source': 'data:image/png;base64,' + base64.b64encode(texture_bytes(resource)).decode(),
                         'mode': 'bitmap', 'namespace': namespace,
                         'folder': str(Path(path).parent.relative_to(f'assets/{namespace}/textures')).replace('\\', '/')})
    elements = []
    for i, original in enumerate(model['elements']):
        e = copy.deepcopy(original)
        e['uuid'] = str(uuid.uuid5(uuid.NAMESPACE_URL, name + '/' + str(i)))
        e['type'] = 'cube'
        e['box_uv'] = False
        rotation = e.pop('rotation', None)
        e['origin'] = rotation['origin'] if rotation else [8, 8, 8]
        if rotation:
            e['rotation'] = [rotation['angle'] if a == rotation['axis'] else 0 for a in 'xyz']
        for face in e['faces'].values():
            key = face['texture'][1:]
            _, _, fw, fh = texture_size(model['textures'][key])
            face['uv'] = [v * (fw if j % 2 == 0 else fh) / 16 for j, v in enumerate(face['uv'])]
            face['texture'] = indices[key]
        elements.append(e)
    return {'meta': {'format_version': '4.10', 'model_format': 'java_block', 'box_uv': False},
            'name': name, 'ambientocclusion': model['ambientocclusion'],
            'resolution': {'width': 16, 'height': 16}, 'elements': elements,
            'outliner': [e['uuid'] for e in elements], 'textures': textures,
            'display': model.get('display', {})}


def rotate(point, rot):
    if not rot:
        return point
    p = [point[k] - rot['origin'][k] for k in range(3)]
    a = math.radians(rot['angle'])
    i, j = {'x': (1, 2), 'y': (2, 0), 'z': (0, 1)}[rot['axis']]
    p[i], p[j] = p[i] * math.cos(a) - p[j] * math.sin(a), p[i] * math.sin(a) + p[j] * math.cos(a)
    return [p[k] + rot['origin'][k] for k in range(3)]


def glow_quads(m, key):
    quads = []
    for e, stage_key, side in m.glow:
        if stage_key != key:
            continue
        (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
        lift = .0625
        corners = {
            'up': [[x0, y1 + lift, z0], [x1, y1 + lift, z0], [x1, y1 + lift, z1], [x0, y1 + lift, z1]],
            'east': [[x1 + lift, y0, z0], [x1 + lift, y1, z0], [x1 + lift, y1, z1], [x1 + lift, y0, z1]],
            'west': [[x0 - lift, y0, z1], [x0 - lift, y1, z1], [x0 - lift, y1, z0], [x0 - lift, y0, z0]],
        }[side]
        quads.append([rotate(c, e.get('rotation')) for c in corners])
    return quads


def java_floats(values):
    return '{' + ', '.join(f'{v / 16:.6f}F' for v in values) + '}'


def glow_source(models):
    rubric = glow_quads(models[1], 'rubric')
    palimpsest = glow_quads(models[2], 'palimpsest')

    def quad_block(quads):
        return ',\n'.join('        ' + java_floats([c for corner in quad for c in corner]) for quad in quads)

    def path_block(paths):
        return ',\n'.join('        ' + java_floats([c for point in path for c in point]) for path in paths)

    return ('package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;\n\n'
            '// Generated by tools/model_export/export_scriptorium_upgrades.py. Block units, north-facing.\n'
            'final class ScriptoriumGeometry {\n'
            f'    static final float[] JAR_X = {java_floats([x for x, _ in JARS])};\n'
            f'    static final float[] JAR_Z = {java_floats([z for _, z in JARS])};\n'
            f'    static final float FLUID_BOTTOM = {FLUID_BOTTOM / 16:.6f}F;\n'
            f'    static final float FLUID_HEIGHT = {(FLUID_TOP - FLUID_BOTTOM) / 16:.6f}F;\n'
            f'    static final float FLUID_HALF = {FLUID_HALF / 16:.6f}F;\n'
            '    /** Emissive rubric strokes shown from the Eightfold stage. */\n'
            '    static final float[][] RUBRIC_QUADS = {\n' + quad_block(rubric) + '\n    };\n'
            '    /** Scratched vellum glyphs shown at the Palimpsest stage. */\n'
            '    static final float[][] PALIMPSEST_QUADS = {\n' + quad_block(palimpsest) + '\n    };\n'
            '    /** Polyline from each jar, down its capillary and along its arm into the stem hub. */\n'
            '    static final float[][] FEEDS = {\n' + path_block([feed_path(i) for i in range(8)]) + '\n    };\n'
            '    /** Merged feed from the hub, up the riser and into the grimoire, by stage. */\n'
            '    static final float[][] TAILS = {\n' + path_block([tail_path(s) for s in range(3)]) + '\n    };\n'
            '    private ScriptoriumGeometry() {}\n}\n')


def separate(elements):
    """Pull apart coplanar faces that would z-fight; glow quads follow the moved glyphs."""
    textures = {'#' + k: v for k, v in TEXTURES.items()}
    textures['#book'] = TEXTURES['paper']
    coplanar.resolve(elements, textures, TRANSLUCENT)


def models():
    built = [build(stage) for _, stage in STAGES]
    for m in built:
        separate(m.elements)
    return built


def main():
    built = models()
    SOURCE.mkdir(exist_ok=True)
    for (name, _), m in zip(STAGES, built):
        model = to_json(m)
        (MODELS / (name + '.json')).write_text(json.dumps(model, indent=2) + '\n')
        (SOURCE / (name + '.bbmodel')).write_text(json.dumps(blockbench(name, model), indent=2) + '\n')
        print(f'Exported {name}: {len(model["elements"])} cubes')
    item = to_json(built[0])
    item['textures']['book'] = TEXTURES['paper']
    item['elements'] += static_book()
    separate(item['elements'])
    (ASSETS / 'models/item/enzymatic_scriptorium.json').write_text(json.dumps(item, indent=2) + '\n')
    # Keep the old internal resource name usable by saved model references/resource packs.
    (MODELS / 'monolithic_scriptorium.json').write_text(
        json.dumps({'parent': 'hemomancy:block/palimpsest_scriptorium'}, indent=2) + '\n')
    GLOW_SOURCE.write_text(glow_source(built))


if __name__ == '__main__':
    main()

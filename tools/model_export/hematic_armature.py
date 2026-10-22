"""Author the Hematic Armature: a restraint frame the subject stands in while armor is remade on
the body, four reagent bowls, its Consecrated chains and banners, and the Monolithic cornerstone
with its heart reservoir and calcified spine.

Emits the three stage bodies (split into sections, see below), the animated parts
HematicArmatureRenderer draws, editable Blockbench projects and the generated
ArmatureGeometry.java (collision cells, the footprint, bowl anchors and fluid boxes).
`--preview` also writes textured renders of every stage to build/hematic_armature_preview.png.

Model space is the north-facing controller block: the subject stands on the dais at its centre
facing north (low z), where the viewer and the two low front bowls are. The frame is five cells
wide (x -32..48) and five tall; vanilla model elements must stay inside -16..32, so each stage is
authored whole and split on export. The controller's blockstate model keeps everything inside
those bounds; the west and east bowl wings (shifted 16 px) and the upper frame (lowered 48 px)
become standalone sections the renderer draws back in place.

Python 3 + Pillow + NumPy.
"""
from pathlib import Path
import argparse
import copy
import json
import math
import zlib

import coplanar

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODELS = ASSETS / 'models/block'
BBMODEL = MODELS / 'bbmodel'
GEOMETRY = ROOT / 'src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/ArmatureGeometry.java'
PREFIX = 'hematic_armature'

# Shared with HematicArmatureRenderer through the generated ArmatureGeometry (model pixels).
CENTRE = (8.0, 8.0)
DAIS_TOP = 3.0                     # the subject stands here; the restraint seats them on it
POSTS = (-8.0, 24.0)               # frame post centres along x; they stand on the controller row
POST_Z = 8.0
BEAM = (55.5, 59.5)                # crossbeam bottom and top
OUTER = ((40.0, 8.0), (-24.0, 8.0))     # head and chest bowl stands, one cell past the posts
INNER = ((24.0, -8.0), (-8.0, -8.0))    # legs and feet bowl stands, in front of the posts
OUTER_FLOOR, INNER_FLOOR = 18.7, 10.7
ITEM_LIFT = 3.25                   # a held reagent floats this far over its bowl floor
VIAL_Z = 3.75                      # side vials strap to the posts' front faces
VIAL_FLUID = (1.45, 14.2, 26.8)    # vial fluid radius, bottom and full top
HEART = (8.0, 8.0)                 # Monolithic heart reservoir axis
HEART_FLUID = (2.6, 68.6, 74.9)
BANNERS = (-1.5, 17.5)             # Consecrated banner centres along x
BANNER_PIVOT = (55.1, 6.3)         # y/z of the banner hinge under the crossbeam
CUFF_Y = 13.0                      # wrist cuffs, at a standing subject's hands
CUFFS = (2.0, 14.0)

# Sections: name -> (dx, dy) subtracted on export so the section fits vanilla's element bounds.
SECTIONS = {'body': (0, 0), 'west': (-16, 0), 'east': (16, 0), 'upper': (0, 48)}
# Cells relative to the controller, north-facing; the subject's column needs no geometry.
FOOTPRINT = sorted([(x, 0, 0) for x in range(-2, 3)] + [(-1, 0, -1), (1, 0, -1)]
                   + [(x, 1, 0) for x in range(-2, 3)] + [(x, 2, 0) for x in range(-1, 2)]
                   + [(x, 3, 0) for x in range(-1, 2)] + [(0, 4, 0)])
SUBJECT_CELLS = [(0, 1, 0), (0, 2, 0)]

TEXTURES = {
    'plank': 'hemomancy:block/blood_wood_planks',
    'log': 'hemomancy:block/blood_wood_log',
    'logtop': 'hemomancy:block/blood_wood_log_top',
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'chain': 'hemomancy:block/hematic_iron_chain',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'brick': 'hemomancy:block/polished_venous_stone_bricks',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'slab': 'hemomancy:block/polished_venous_stone',
    'mono': 'minecraft:block/polished_blackstone',
    'monobrick': 'minecraft:block/polished_blackstone_bricks',
    'netherite': 'minecraft:block/netherite_block',
    'glass': 'hemomancy:block/ghastly_alembic_glass',
    'sanguine': 'hemomancy:block/sanguine_glass',
    'blood': 'hemomancy:block/blood_still',
    'scab': 'hemomancy:block/blood_scorched_scab',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'tendon': 'hemomancy:block/bog_flesh',
    'wax': 'hemomancy:block/myelin_sheath',
    'flame': 'hemomancy:block/crimson_flames',
    'cloth': 'minecraft:block/red_wool',
    'shroud': 'minecraft:block/black_wool',
}
TRANSLUCENT = {'glass', 'sanguine'}
CUTOUT = {'chain', 'flame'}        # sprites with holes: never hide a fight, never collide
# Restraint hardware around the subject: they must be able to step in among it.
PASSABLE = {'cuff', 'cuff_binding', 'vertebra', 'spinous_process', 'transverse_process', 'spinal_cord'}
CAP = {'bone': 'bonecap', 'pillar': 'pillartop', 'log': 'logtop'}
TAN = math.tan(math.radians(22.5))
STAGES = {'hematic_armature': 0, 'hematic_armature_consecrated': 1, 'hematic_armature_monolithic': 2}
DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [0.625, 0.625, 0.625]},
    'ground': {'translation': [0, 3, 0], 'scale': [0.25, 0.25, 0.25]},
    'fixed': {'rotation': [0, 180, 0], 'scale': [0.5, 0.5, 0.5]},
    'head': {'rotation': [0, 180, 0], 'translation': [0, 4, 0], 'scale': [0.5, 0.5, 0.5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'scale': [0.4, 0.4, 0.4]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'scale': [0.4, 0.4, 0.4]},
}


class Model:
    def __init__(self):
        self.elements = []

    def box(self, name, a, b, mat, rot=None, faces=None, caps=None, full=False):
        """Axis-aligned cube from a to b; rot=(axis, angle, origin) applies a Java model rotation.

        `full` maps the whole texture onto every face, for sprites like a candle flame.
        """
        a, b = [round(min(p, q), 4) for p, q in zip(a, b)], [round(max(p, q), 4) for p, q in zip(a, b)]
        if any(q - p <= 0 for p, q in zip(a, b)):
            return
        out = {}
        for side in faces or ('north', 'south', 'east', 'west', 'up', 'down'):
            m = caps.get(side, mat) if caps else mat
            coords = [0, 0, 16, 16] if full else uv(side, a, b)
            # Glass panes keep the world-aligned, face-sized UVs too, so the glass sits at block texel density.
            out[side] = (m, coords)
        self.elements.append(dict(name=name, a=a, b=b, rot=rot, faces=out))

    def octo(self, name, axis, centre, radius, start, end, mat, cap=None):
        """Solid regular octagonal prism along `axis` (flat-to-flat 2*radius)."""
        cap = cap or CAP.get(mat, mat)
        i = 'xyz'.index(axis)
        o = [k for k in range(3) if k != i]
        ends = {'x': ('east', 'west'), 'y': ('up', 'down'), 'z': ('north', 'south')}[axis]
        for n, (h0, h1, angle) in enumerate(((radius, radius * TAN, 0), (radius * TAN, radius, 0),
                                             (radius, radius * TAN, 45), (radius, radius * TAN, -45))):
            inset = n * .04   # caps sit clear of coplanar.TOLERANCE instead of fighting
            a, b = [0] * 3, [0] * 3
            a[i], b[i] = start + inset, end - inset
            a[o[0]], b[o[0]] = centre[o[0]] - h0, centre[o[0]] + h0
            a[o[1]], b[o[1]] = centre[o[1]] - h1, centre[o[1]] + h1
            self.box(name, a, b, mat, (axis, angle, list(centre)) if angle else None,
                     caps={ends[0]: cap, ends[1]: cap})

    def shell(self, name, centre, radius, y0, y1, thick, mat, ends=False):
        """Hollow upright octagon of eight panes, so whatever stands inside stays visible."""
        cx, cz = centre
        half = radius * TAN
        sides = ('north', 'south', 'east', 'west') + (('up', 'down') if ends else ())
        for angle in (0, 45):
            rot = ('y', angle, [cx, y0, cz]) if angle else None
            for s in (-1, 1):
                lo, hi = sorted((s * radius, s * (radius - thick)))
                self.box(name, (cx + lo, y0, cz - half), (cx + hi, y1, cz + half), mat, rot, faces=sides)
                self.box(name, (cx - half, y0, cz + lo), (cx + half, y1, cz + hi), mat, rot, faces=sides)

    def ring(self, name, centre, outer, inner, y0, y1, mat):
        """Hollow square collar standing on y."""
        cx, cz = centre
        for s0, s1 in ((-outer, -inner), (inner, outer)):
            self.box(name, (cx + s0, y0, cz - outer), (cx + s1, y1, cz + outer), mat)
            self.box(name, (cx - inner, y0, cz + s0), (cx + inner, y1, cz + s1), mat)

    def chain(self, name, x, z, y0, y1):
        """Crossed chain sprites, the way vanilla draws a chain block, cut into texture-high links."""
        y = y0
        while y < y1 - 1e-6:
            top = min(y + 16, y1)
            v0, v1 = 16 - (top - y), 16
            for angle, u in ((45, 0), (-45, 3)):
                self.elements.append(dict(name=name, a=[x - 1.5, y, z - .005], b=[x + 1.5, top, z + .005],
                                          rot=('y', angle, [x, y, z]),
                                          faces={'north': ('chain', [u, v0, u + 3, v1]),
                                                 'south': ('chain', [u, v0, u + 3, v1])}))
            y = top

    def candle(self, x, z, base, height):
        self.box('candle', (x - .6, base, z - .6), (x + .6, base + height, z + .6), 'wax')
        self.box('candle_wick', (x - .1, base + height, z - .1), (x + .1, base + height + .5, z + .1), 'scab')
        top = base + height + .2
        self.box('candle_flame', (x - .75, top, z - .005), (x + .75, top + 1.6, z + .005), 'flame',
                 faces=('north', 'south'), full=True)
        self.box('candle_flame', (x - .005, top, z - .75), (x + .005, top + 1.6, z + .75), 'flame',
                 faces=('east', 'west'), full=True)


def uv(side, a, b):
    """World-aligned UVs so neighbouring cubes continue the same texture."""
    if side in ('north', 'south'):
        u, v = (a[0], b[0]), (16 - b[1], 16 - a[1])
    elif side in ('east', 'west'):
        u, v = (a[2], b[2]), (16 - b[1], 16 - a[1])
    else:
        u, v = (a[0], b[0]), (a[2], b[2])
    out = []
    for lo, hi in (u, v):
        size = min(hi - lo, 16)
        start = lo % 16
        if start + size > 16:
            start = 16 - size
        out.append((round(start, 3), round(start + size, 3)))
    return [out[0][0], out[1][0], out[0][1], out[1][1]]


def tier_mats(t):
    """Frame, cladding and fitting materials for each stage: blood wood, consecrated iron, monolith."""
    return [dict(post='log', beam='log', deck='plank', stone='plank', fit='iron', trim='bold', band='iron'),
            dict(post='pillar', beam='iron', deck='iron', stone='brick', fit='chisel', trim='venous', band='chisel'),
            dict(post='pillar', beam='iron', deck='monobrick', stone='mono', fit='chisel', trim='monobrick', band='netherite')][t]


# ---------------------------------------------------------------- body (every stage)

def floor(m, t):
    """Deck under the frame row and two front aprons; the subject steps up onto the round dais."""
    mat = tier_mats(t)
    cx, cz = CENTRE
    m.box('deck', (-14, 0, 2), (30, 1.5, 14), mat['deck'])
    for x0, x1 in ((-14, -2), (18, 30)):
        m.box('apron', (x0, 0, -14), (x1, 1.5, 2), mat['deck'])
    m.octo('dais', 'y', (cx, 0, cz), 6.5, 1.5, DAIS_TOP, 'log' if t == 0 else mat['stone'],
           cap='logtop' if t == 0 else ('slab' if t == 1 else 'mono'))
    m.shell('dais_rim', CENTRE, 6.85, 1.5, DAIS_TOP - .4, .4, mat['band'], ends=True)
    for x, z in ((-14, -14), (26.5, -14), (-14, 10.5), (26.5, 10.5)):
        m.box('deck_strap', (x - .25, -.01, z - .25), (x + 3.75, 1.75, z + 3.75), mat['fit'])
    if t == 2:
        # Red rune seams let into the black deck, running out to the four bowl stands.
        m.box('rune_seam', (-12, 1.5, 7.6), (1.5, 1.6, 8.4), 'venous', faces=('up',))
        m.box('rune_seam', (14.5, 1.5, 7.6), (28, 1.6, 8.4), 'venous', faces=('up',))


def post(m, t, x):
    """One frame post: footing, post, banded collars and the capital the crossbeam rests on."""
    mat = tier_mats(t)
    z = POST_Z
    m.box('post_footing', (x - 3.5, 1.5, z - 3.5), (x + 3.5, 5, z + 3.5), mat['trim'] if t else 'bold')
    m.box('post', (x - 2, 5, z - 2), (x + 2, BEAM[0] - .5, z + 2), mat['post'])
    for y in (20, 40):
        m.ring('post_band', (x, z), 2.4, 2, y, y + 1.5, mat['band'])
    m.box('post_capital', (x - 3, BEAM[0] - 1, z - 3), (x + 3, BEAM[1] + 1.5, z + 3), mat['fit'])
    finial = 'bone' if t < 2 else 'netherite'
    m.octo('post_finial', 'y', (x, 0, z), 1.25, BEAM[1] + 1.5, BEAM[1] + 4, finial)
    if t >= 1:
        # Hemolytic plating: riveted plates down the post's front face.
        for y0, y1 in ((6, 19.5), (21.5, 39.5), (41.5, 53.5)):
            m.box('post_plate', (x - 1.6, y0, z - 2.4), (x + 1.6, y1, z - 2), 'iron', faces=('north', 'east', 'west', 'up', 'down'))
    if t == 2:
        m.box('post_sheath', (x - 2.75, 5, z - 2.75), (x + 2.75, 12, z + 2.75), 'mono')


def crossbeam(m, t):
    mat = tier_mats(t)
    caps = {'east': 'logtop', 'west': 'logtop'} if t == 0 else None
    m.box('crossbeam', (-10.5, BEAM[0], 6.5), (26.5, BEAM[1], 9.5), mat['beam'], caps=caps)
    for x in CUFFS:
        # Hanging eyes the cuffs' chains (or the base stage's bare hooks) depend from.
        m.box('beam_eye', (x - .75, BEAM[0] - 1.25, 7.25), (x + .75, BEAM[0], 8.75), mat['fit'])
    for x, angle in ((POSTS[0] + 5.5, 45), (POSTS[1] - 5.5, -45)):
        # Knee braces square the frame; each runs from a post's inner face up into the beam.
        m.box('knee_brace', (x - 5, BEAM[0] - 4.25, 7.25), (x + 5, BEAM[0] - 2.75, 8.75), mat['beam'],
              ('z', angle, [x, BEAM[0] - 3.5, 8]))


def crest(m, t):
    """What crowns the beam over the subject's head; it fills the fifth cell at every stage."""
    cx, cz = CENTRE
    if t == 0:
        m.box('crest_plate', (4.5, BEAM[1], 6), (11.5, BEAM[1] + 2.5, 10), 'iron')
        m.octo('crest_finial', 'y', (cx, 0, cz), 1.5, BEAM[1] + 2.5, 67, 'bone')
        m.box('crest_horn', (cx - .5, 64, cz - 4), (cx + .5, 65, cz + 4), 'bone')
    elif t == 1:
        m.box('crest_plate', (3.5, BEAM[1], 5.5), (12.5, 63, 10.5), 'chisel')
        m.box('crest_face', (5, 60.5, 5.2), (11, 62.5, 5.5), 'venous', faces=('north', 'east', 'west', 'up', 'down'))
        m.octo('crest_spire', 'y', (cx, 0, cz), 1.25, 63, 69, 'bone')
        for x in (4, 12):
            m.octo('crest_horn', 'y', (x, 0, cz), .75, 63, 66, 'bone')


def wrist_cuffs(m, t):
    """Iron arms from each post to an open cuff at the subject's hands."""
    mat = tier_mats(t)
    for x, side in zip(CUFFS, (-1, 1)):
        inner_face = POSTS[0] + 2 if side < 0 else POSTS[1] - 2
        reach = x + side * 2.2
        m.box('cuff_arm', (min(inner_face, reach), CUFF_Y + .75, 7.4), (max(inner_face, reach), CUFF_Y + 2.25, 8.6),
              'iron' if t == 0 else mat['fit'])
        m.ring('cuff', (x, 8), 2.2, 1.6, CUFF_Y, CUFF_Y + 3, mat['band'] if t else 'iron')
        if t == 0:
            # Base cuffs are bound with tendon rather than locked.
            m.ring('cuff_binding', (x, 8), 2.35, 2.2, CUFF_Y + 1, CUFF_Y + 2, 'tendon')


def side_vials(m, t):
    """Glass vials strapped to each post's front: the Armature's stored blood shows in them."""
    mat = tier_mats(t)
    for x in POSTS:
        m.octo('vial_foot', 'y', (x, 0, VIAL_Z), 2, 12.5, 14, mat['fit'])
        m.shell('vial_glass', (x, VIAL_Z), 1.75, 14, 27, .2, 'glass')
        m.octo('vial_collar', 'y', (x, 0, VIAL_Z), 2, 27, 28.25, mat['fit'])
        m.octo('vial_stopper', 'y', (x, 0, VIAL_Z), 1, 28.25, 29.5, 'bone')
        for y in (16, 25):
            m.box('vial_strap', (x - .5, y, VIAL_Z + 1.75), (x + .5, y + 1, POST_Z - 2), mat['band'])
        # The feed runs from the vial's foot back into the post and on to the cuff.
        m.box('vial_feed', (x - .4, 11.5, VIAL_Z - .4), (x + .4, 12.5, POST_Z - 2), 'vein')


def outer_stand(m, t, x, z):
    """Head and chest reagent stand: plinth, column and a broad bowl."""
    mat = tier_mats(t)
    m.box('outer_plinth', (x - 6.5, 0, z - 6.5), (x + 6.5, 3, z + 6.5), mat['stone'])
    if t == 0:
        for sx in (-1, 1):
            for sz in (-1, 1):
                m.box('plinth_corner', (x + sx * 6.75 - 1, -.01, z + sz * 6.75 - 1), (x + sx * 6.75 + 1, 3.25, z + sz * 6.75 + 1), 'iron')
    m.octo('outer_column', 'y', (x, 0, z), 3.25, 3, 15, mat['post'] if t < 2 else 'mono')
    m.octo('outer_collar', 'y', (x, 0, z), 4.5, 15, 17, mat['fit'])
    m.octo('outer_bowl', 'y', (x, 0, z), 7, 17, OUTER_FLOOR, 'iron' if t == 0 else 'chisel')
    m.shell('outer_lip', (x, z), 7.35, OUTER_FLOOR, 21, .7, 'iron' if t == 0 else mat['band'], ends=True)
    m.octo('outer_stain', 'y', (x, 0, z), 6.3, OUTER_FLOOR - .2, OUTER_FLOOR, 'scab')
    if t == 2:
        m.box('outer_rune', (x - 2, 4.5, z - 3.6), (x + 2, 13, z - 3.25), 'venous', faces=('north', 'east', 'west', 'up', 'down'))
    if t >= 1:
        for sx in (-1, 1):
            m.candle(x + sx * 5, z - 5, 3, 3 if sx < 0 else 4.5)


def inner_stand(m, t, x, z):
    """Legs and feet reagent stand: a low cup on a stem, set on the front apron."""
    mat = tier_mats(t)
    m.octo('inner_foot', 'y', (x, 0, z), 4.5, 1.5, 3, 'bold' if t < 2 else 'mono')
    m.octo('inner_stem', 'y', (x, 0, z), 2, 3, 9, mat['post'] if t < 2 else 'mono')
    m.octo('inner_bowl', 'y', (x, 0, z), 5, 9, INNER_FLOOR, 'iron' if t == 0 else 'chisel')
    m.shell('inner_lip', (x, z), 5.3, INNER_FLOOR, 12.5, .6, 'iron' if t == 0 else mat['band'], ends=True)
    m.octo('inner_stain', 'y', (x, 0, z), 4.4, INNER_FLOOR - .2, INNER_FLOOR, 'scab')
    if t >= 1:
        m.candle(x + (5.5 if x > CENTRE[0] else -5.5), z - 4.5, 1.5, 3.5)


def consecration(m, t):
    """Consecrated stage: chains from the beam to the cuffs and a shoulder yoke behind the subject."""
    for x in CUFFS:
        m.chain('restraint_chain', x, 8, CUFF_Y + 3, BEAM[0] - 1.25)
    m.box('yoke', (-9, 27, 12.5), (25, 29, 14.5), 'iron')
    for x in POSTS:
        m.box('yoke_bracket', (x - 1, 26.5, POST_Z + 2), (x + 1, 29.5, 12.5), 'chisel')
    for x in (3, 13):
        m.box('yoke_hook', (x - .5, 29, 12.25), (x + .5, 31, 13), 'bone')


def monolith(m):
    """Monolithic stage: the cornerstone set on the beam, its heart reservoir and the spine below."""
    cx, cz = CENTRE
    m.box('cornerstone', (1, BEAM[1], 4), (15, 67, 12), 'mono')
    m.box('cornerstone_lintel', (.25, 66, 3.25), (15.75, 67.75, 12.75), 'monobrick')
    m.box('cornerstone_rune', (4.5, 61, 3.7), (11.5, 65.5, 4), 'venous', faces=('north', 'east', 'west', 'up', 'down'))
    m.octo('heart_seat', 'y', (cx, 0, cz), 3.5, 67.75, 68.5, 'netherite')
    m.octo('heart_shoulder', 'y', (cx, 0, cz), 3.4, 75, 75.75, 'netherite')
    m.octo('heart_neck', 'y', (cx, 0, cz), 1.75, 75.75, 77, 'chisel')
    m.octo('heart_knob', 'y', (cx, 0, cz), 1, 77, 78.5, 'bone')
    for angle in (0, 90, 180, 270):
        a = math.radians(angle)
        x, z = cx + 3.45 * math.cos(a), cz + 3.45 * math.sin(a)
        m.box('heart_rib', (x - .4, 68.5, z - .4), (x + .4, 75, z + .4), 'bone')
    # Calcified spine behind the subject: vertebrae with their processes, from dais to cornerstone.
    for n, y in enumerate(range(5, 54, 4)):
        m.box('vertebra', (cx - 1.5, y, 12), (cx + 1.5, y + 2.5, 15), 'bone')
        m.box('spinous_process', (cx - .5, y + .5, 15), (cx + .5, y + 2, 16), 'bone')
        if n % 3 == 1:
            m.box('transverse_process', (cx - 3, y + .75, 13), (cx + 3, y + 1.75, 14), 'bone')
    m.box('spinal_cord', (cx - .6, DAIS_TOP, 12.9), (cx + .6, 54, 14.1), 'vein')


def static(t):
    m = Model()
    floor(m, t)
    for x in POSTS:
        post(m, t, x)
    crossbeam(m, t)
    crest(m, t)
    wrist_cuffs(m, t)
    side_vials(m, t)
    for x, z in OUTER:
        outer_stand(m, t, x, z)
    for x, z in INNER:
        inner_stand(m, t, x, z)
    if t >= 1:
        consecration(m, t)
    if t == 2:
        monolith(m)
    return m


# ---------------------------------------------------------------- animated and fluid parts

def banner(t):
    """Left banner, hanging from its hinge under the beam; the renderer sways it and copies it right."""
    m = Model()
    x = BANNERS[0]
    py, pz = BANNER_PIVOT
    cloth, rod, emblem = ('cloth', 'bone', 'chisel') if t == 1 else ('shroud', 'netherite', 'venous')
    m.box('banner_rod', (x - 3.6, py - .4, pz - .4), (x + 3.6, py + .4, pz + .4), rod)
    m.box('banner_cloth', (x - 3, 38.5, pz - .25), (x + 3, py - .4, pz + .25), cloth)
    for x0, x1 in ((x - 3, x - .6), (x + .6, x + 3)):
        m.box('banner_tail', (x0, 36, pz - .25), (x1, 38.5, pz + .25), cloth)
    m.box('banner_emblem', (x - 1.5, 45, pz - .55), (x + 1.5, 50, pz - .25), emblem,
          faces=('north', 'east', 'west', 'up', 'down'))
    m.box('banner_emblem_drop', (x - .5, 42, pz - .5), (x + .5, 45, pz - .25), 'vein',
          faces=('north', 'east', 'west', 'down'))
    return m


def vial_blood():
    """Unit-height fluid on the west vial's floor; the renderer scales it and repeats it east."""
    m = Model()
    radius, bottom, _ = VIAL_FLUID
    m.octo('vial_blood', 'y', (POSTS[0], 0, VIAL_Z), radius, bottom, bottom + 1, 'blood')
    return m


def heart_glass():
    m = Model()
    m.shell('heart_glass', HEART, 3, 68.5, 75, .2, 'sanguine')
    return m


def heart_blood():
    m = Model()
    radius, bottom, _ = HEART_FLUID
    m.octo('heart_blood', 'y', (HEART[0], 0, HEART[1]), radius, bottom, bottom + 1, 'blood')
    return m


PARTS = {'banner': lambda: banner(1), 'banner_monolithic': lambda: banner(2), 'vial_blood': vial_blood,
         'heart_glass': heart_glass, 'heart_blood': heart_blood}
# Parts drawn above the controller's bounds are exported lowered, like the upper section.
PART_LIFT = {'banner': 48, 'banner_monolithic': 48, 'vial_blood': 0, 'heart_glass': 48, 'heart_blood': 48}


def stage_parts(t):
    """Parts each stage shows at rest, in place (the east banner is a translated copy)."""
    if t == 0:
        return []
    return ['banner' if t == 1 else 'banner_monolithic'] + (['heart_glass'] if t == 2 else [])


def placed_parts(t, parts):
    """Every part instance a stage draws at rest, in model space, for clash checks and previews."""
    out = []
    for name in stage_parts(t):
        out.append(parts[name])
        if name.startswith('banner'):
            out.append(shifted(parts[name], BANNERS[1] - BANNERS[0], 0))
    return out


# ---------------------------------------------------------------- sections

def bounds(e):
    """Axis-aligned bounds of an element after its rotation."""
    corners = [(x, y, z) for x in (e['a'][0], e['b'][0]) for y in (e['a'][1], e['b'][1]) for z in (e['a'][2], e['b'][2])]
    if e['rot']:
        axis, angle, o = e['rot']
        m = coplanar._matrix({'axis': axis, 'angle': angle})
        corners = [tuple(c + o[i] for i, c in enumerate(coplanar._apply(m, tuple(p[i] - o[i] for i in range(3)))))
                   for p in corners]
    return [min(c[i] for c in corners) for i in range(3)], [max(c[i] for c in corners) for i in range(3)]


def fits(e, dx, dy):
    lo, hi = bounds(e)
    return (all(lo[i] - d >= -16 - 1e-6 and hi[i] - d <= 32 + 1e-6 for i, d in ((0, dx), (1, dy)))
            and lo[2] >= -16 - 1e-6 and hi[2] <= 32 + 1e-6)


def cut(e, y):
    """Split an element at height y; the two hidden faces at the cut are dropped."""
    if e['rot'] and e['rot'][0] != 'y':
        raise ValueError('cannot cut %s: rotated about %s' % (e['name'], e['rot'][0]))
    below, above = copy.deepcopy(e), copy.deepcopy(e)
    below['b'][1] = above['a'][1] = y
    below['faces'].pop('up', None)
    above['faces'].pop('down', None)
    for part in (below, above):
        for side, (mat, coords) in part['faces'].items():
            if side not in ('up', 'down') and mat not in CUTOUT and mat not in TRANSLUCENT:
                part['faces'][side] = (mat, uv(side, part['a'], part['b']))
    return below, above


def sections(model):
    """Assign every element to the first section whose bounds hold it, cutting posts at y = 32."""
    out = {name: Model() for name in SECTIONS}
    pending = list(model.elements)
    while pending:
        e = pending.pop(0)
        home = next((name for name, (dx, dy) in SECTIONS.items() if fits(e, dx, dy)), None)
        if home:
            out[home].elements.append(e)
            continue
        lo, hi = bounds(e)
        if lo[1] < 32 < hi[1]:
            pending[:0] = cut(e, 32)
            continue
        raise ValueError('%s fits no section: %s %s' % (e['name'], lo, hi))
    return out


def shifted(model, dx, dy):
    out = Model()
    for e in model.elements:
        e = copy.deepcopy(e)
        for k, d in ((0, dx), (1, dy)):
            e['a'][k] = round(e['a'][k] + d, 4)
            e['b'][k] = round(e['b'][k] + d, 4)
            if e['rot']:
                e['rot'][2][k] += d
        out.elements.append(e)
    return out


# ---------------------------------------------------------------- collision

def cells(model):
    """Collision boxes clipped into the cells they cross, north-facing, in model pixels."""
    boxes = set()
    for e in model.elements:
        if e['name'] in PASSABLE or all(mat in CUTOUT for mat, _ in e['faces'].values()):
            continue
        lo, hi = bounds(e)
        # Snap to quarter pixels, forgiving the hundredths coplanar separation nudges faces by.
        lo = [math.floor(v * 4 + .2) / 4 for v in lo]
        hi = [math.ceil(v * 4 - .2) / 4 for v in hi]
        boxes.add(tuple(lo + hi))
    covered = set()
    for box in boxes:
        for x in range(math.floor(box[0] / 16), math.ceil(box[3] / 16)):
            for y in range(math.floor(box[1] / 16), math.ceil(box[4] / 16)):
                for z in range(math.floor(box[2] / 16), math.ceil(box[5] / 16)):
                    covered.add((x, y, z))
    return sorted(boxes), covered


def check_footprint(t, covered):
    """Every filler cell carries geometry at every stage, and nothing spills past the footprint."""
    allowed = set(FOOTPRINT)
    stray = covered - allowed
    assert not stray, ('stage %d spills outside the footprint' % t, sorted(stray))
    bare = allowed - covered - set(SUBJECT_CELLS)
    assert not bare, ('stage %d leaves filler cells empty' % t, sorted(bare))


# ---------------------------------------------------------------- export

def views(model):
    """Java-model-shaped views for coplanar.py; they share each element's from/to lists."""
    out = []
    for e in model.elements:
        view = {'name': e['name'], 'from': e['a'], 'to': e['b'],
                'faces': {side: {'texture': TEXTURES[mat], 'uv': coords} for side, (mat, coords) in e['faces'].items()}}
        if e['rot']:
            axis, angle, origin = e['rot']
            view['rotation'] = {'axis': axis, 'angle': angle, 'origin': origin}
        out.append(view)
    return out


def to_json(model, dx=0, dy=0, translucent=False, display=False, particle='iron'):
    used, elements = [], []
    for e in model.elements:
        faces = {}
        for side, (mat, coords) in e['faces'].items():
            if mat not in used:
                used.append(mat)
            faces[side] = dict(uv=coords, texture='#' + mat)
        a = [round(e['a'][0] - dx, 4), round(e['a'][1] - dy, 4), e['a'][2]]
        b = [round(e['b'][0] - dx, 4), round(e['b'][1] - dy, 4), e['b'][2]]
        assert all(-16 <= v <= 32 for v in a + b), (e['name'], a, b)
        el = {'name': e['name'], 'from': a, 'to': b}
        if e['rot']:
            axis, angle, origin = e['rot']
            el['rotation'] = dict(angle=angle, axis=axis, origin=[round(origin[0] - dx, 4), round(origin[1] - dy, 4), origin[2]])
        el['faces'] = faces
        elements.append(el)
    textures = {'particle': TEXTURES[particle]}
    textures.update({mat: TEXTURES[mat] for mat in used})
    out = {'parent': 'minecraft:block/block'}
    if translucent:
        out['render_type'] = 'translucent'
    # The open frame and glass read wrong with vanilla corner occlusion.
    out.update(ambientocclusion=False, textures=textures, elements=elements)
    if display:
        out['display'] = DISPLAY
    return out


def bbmodel(name, groups):
    """Editable Blockbench project: every section and part in place as a named group."""
    textures, tex_index, elements, outliner = [], {}, [], []
    uid = 0
    for group, model in groups:
        children = []
        for e in model.elements:
            faces = {}
            for side in ('north', 'east', 'south', 'west', 'up', 'down'):
                if side in e['faces']:
                    mat, coords = e['faces'][side]
                    if mat not in tex_index:
                        ns, path = TEXTURES[mat].split(':')
                        folder, file = path.split('/', 1)
                        tex_index[mat] = len(textures)
                        textures.append(dict(path=str(ASSETS.parent / ns / 'textures' / (path + '.png')).replace('\\', '/'),
                                             name=file + '.png', folder=folder, namespace=ns, id=str(len(textures)),
                                             render_mode='default'))
                    faces[side] = dict(uv=coords, texture=tex_index[mat])
                else:
                    faces[side] = dict(uv=[0, 0, 0, 0], texture=None)
            u = '%08x-0000-4000-8000-%012x' % (zlib.crc32(name.encode()), uid)
            uid += 1
            rotation, origin = [0, 0, 0], [8, 8, 8]
            if e['rot']:
                axis, angle, origin = e['rot']
                rotation['xyz'.index(axis)] = angle
            elements.append(dict(name=e['name'], **{'from': e['a'], 'to': e['b']}, rotation=rotation, origin=origin,
                                 faces=faces, uuid=u, box_uv=False, autouv=0, rescale=False))
            children.append(u)
        outliner.append(dict(name=group, origin=[8, 8, 8], children=children))
    return dict(meta=dict(format_version='4.10', model_format='free', box_uv=False), name=name,
                ambientocclusion=False, resolution=dict(width=16, height=16), elements=elements,
                outliner=outliner, textures=textures)


def blockstates():
    """Multipart, so waterlogging never multiplies the facing and stage cases."""
    parts = []
    for name, stage in STAGES.items():
        for facing, angle in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            apply = {'model': 'hemomancy:block/' + name}
            if angle:
                apply['y'] = angle
            parts.append({'when': {'facing': facing, 'stage': str(stage)}, 'apply': apply})
    return {'multipart': parts}


def separate(statics, parts):
    """Pull apart coplanar faces, each part alone and every stage with its parts at rest.

    Parts move, so each must be clean alone; the east banner is a copy of the west one, so stages
    are checked against a fresh copy each pass and passes repeat until nothing moves.
    """
    translucent = {TEXTURES[m] for m in TRANSLUCENT | CUTOUT}
    for _ in range(10):
        moved = sum(coplanar.resolve(views(model), translucent=translucent) for model in parts.values())
        for t, body in statics.items():
            elements = views(body) + [v for p in stage_parts(t) for v in views(parts[p])]
            moved += coplanar.resolve(elements, translucent=translucent)
        if not moved:
            return
    raise ValueError("Hematic Armature stages keep moving each other's faces")


def geometry(stage_boxes):
    rows = []
    for boxes in stage_boxes:
        rows.append('        {\n' + ''.join('            {%s},\n' % ', '.join(repr(float(v)) for v in box)
                                         for box in boxes) + '        },\n')
    cells_src = ', '.join('{%d, %d, %d}' % c for c in FOOTPRINT)
    bowls = [(x, OUTER_FLOOR + ITEM_LIFT, z) for x, z in OUTER] + [(x, INNER_FLOOR + ITEM_LIFT, z) for x, z in INNER]
    # Bowl anchors are published in the south-facing frame, in blocks from the controller centre.
    bowl_src = ', '.join('{%sD, %sD, %sD}' % (_num((CENTRE[0] - x) / 16), _num(y / 16), _num((CENTRE[1] - z) / 16))
                         for x, y, z in bowls)
    java = GEOMETRY_TEMPLATE.format(
        dais=_num(DAIS_TOP), boxes=''.join(rows), cells=cells_src, bowls=bowl_src,
        vial_x=', '.join('%sf' % _num(x) for x in POSTS), vial_z=_num(VIAL_Z),
        vial=', '.join('%sf' % _num(v) for v in VIAL_FLUID), heart=', '.join('%sf' % _num(v) for v in HEART + HEART_FLUID),
        banner_x=', '.join('%sf' % _num(x) for x in BANNERS), banner_pivot=', '.join('%sf' % _num(v) for v in BANNER_PIVOT))
    GEOMETRY.write_text(java, encoding='utf-8')


def _num(v):
    return repr(round(float(v), 6))


GEOMETRY_TEMPLATE = '''package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Generated by tools/model_export/hematic_armature.py from the authored stage models. */
public final class ArmatureGeometry {{
    /** Height of the dais the restrained subject stands on, in model pixels. */
    public static final float DAIS_TOP = {dais}f;
    /** Head, chest, legs and feet reagent anchors: south-facing block offsets from the controller centre. */
    public static final double[][] BOWLS = {{{bowls}}};
    /** Side vial axes along x and their shared z, then fluid radius, bottom and full top (north-facing pixels). */
    public static final float[] VIAL_X = {{{vial_x}}};
    public static final float VIAL_Z = {vial_z}f;
    public static final float[] VIAL_FLUID = {{{vial}}};
    /** Monolithic heart axis x/z, then fluid radius, bottom and full top. */
    public static final float[] HEART = {{{heart}}};
    /** Consecrated banner centres along x, then the y/z of their hinge. */
    public static final float[] BANNER_X = {{{banner_x}}};
    public static final float[] BANNER_PIVOT = {{{banner_pivot}}};
    /** Filler cells relative to the controller, north-facing; the two above it hold the subject. */
    private static final int[][] FOOTPRINT = {{{cells}}};
    private static final double[][][] BOXES = {{
{boxes}    }};
    private static final BlockPos[][] FILLERS = buildFillers();
    private static final Map<BlockPos, VoxelShape>[][] CELLS = buildCells();

    private ArmatureGeometry() {{}}

    /** Every cell besides the controller that the Armature occupies at the given facing. */
    public static BlockPos[] fillers(Direction facing) {{
        return FILLERS[facing.get2DDataValue()].clone();
    }}

    public static boolean holdsSubject(BlockPos offset) {{
        return offset.getX() == 0 && offset.getZ() == 0 && (offset.getY() == 1 || offset.getY() == 2);
    }}

    public static VoxelShape shape(int stage, Direction facing, BlockPos offset) {{
        return CELLS[Math.clamp(stage, 0, 2)][facing.get2DDataValue()].getOrDefault(offset, Shapes.empty());
    }}

    private static int turns(Direction facing) {{
        return switch (facing) {{ case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; }};
    }}

    private static BlockPos[][] buildFillers() {{
        BlockPos[][] result = new BlockPos[4][];
        for (Direction facing : Direction.Plane.HORIZONTAL) {{
            BlockPos[] offsets = new BlockPos[FOOTPRINT.length - 1];
            int n = 0;
            for (int[] cell : FOOTPRINT) {{
                if (cell[0] == 0 && cell[1] == 0 && cell[2] == 0) continue;
                int x = cell[0], z = cell[2];
                for (int turn = 0; turn < turns(facing); turn++) {{
                    int nextX = -z;
                    z = x;
                    x = nextX;
                }}
                offsets[n++] = new BlockPos(x, cell[1], z);
            }}
            result[facing.get2DDataValue()] = offsets;
        }}
        return result;
    }}

    @SuppressWarnings("unchecked")
    private static Map<BlockPos, VoxelShape>[][] buildCells() {{
        Map<BlockPos, VoxelShape>[][] result = new Map[3][4];
        for (int stage = 0; stage < 3; stage++) {{
            for (Direction facing : Direction.Plane.HORIZONTAL) {{
                Map<BlockPos, VoxelShape> cells = new LinkedHashMap<>();
                for (double[] box : BOXES[stage]) {{
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
                result[stage][facing.get2DDataValue()] = Map.copyOf(cells);
            }}
        }}
        return result;
    }}
}}
'''


def export():
    statics = {t: static(t) for t in STAGES.values()}
    parts = {part: build() for part, build in PARTS.items()}
    separate(statics, parts)
    stage_boxes = []
    for t, body in statics.items():
        boxes, covered = cells(body)
        check_footprint(t, covered)
        stage_boxes.append(boxes)
    BBMODEL.mkdir(parents=True, exist_ok=True)
    for name, t in STAGES.items():
        split = sections(statics[t])
        assert not any(mat in TRANSLUCENT for s in ('west', 'east', 'upper') for e in split[s].elements
                       for mat, _ in e['faces'].values()), 'glass outside the body section must be a part'
        body = to_json(split['body'], translucent=True, particle='plank' if t == 0 else 'iron')
        (MODELS / (name + '.json')).write_text(json.dumps(body, indent=1) + '\n', encoding='utf-8')
        for section in ('west', 'east', 'upper'):
            dx, dy = SECTIONS[section]
            (MODELS / ('%s_%s.json' % (name, section))).write_text(
                json.dumps(to_json(split[section], dx, dy), indent=1) + '\n', encoding='utf-8')
        groups = [(s, split[s]) for s in SECTIONS] + [(p, parts[p]) for p in stage_parts(t)]
        (BBMODEL / (name + '.bbmodel')).write_text(json.dumps(bbmodel(name, groups)), encoding='utf-8')
    for part, model in parts.items():
        glass = any(mat in TRANSLUCENT for e in model.elements for mat, _ in e['faces'].values())
        (MODELS / ('%s_%s.json' % (PREFIX, part))).write_text(
            json.dumps(to_json(model, 0, PART_LIFT[part], glass), indent=1) + '\n', encoding='utf-8')
    item = {'parent': 'builtin/entity', 'gui_light': 'side', 'display': DISPLAY}
    (ASSETS / 'models/item/hematic_armature.json').write_text(json.dumps(item, indent=2) + '\n', encoding='utf-8')
    (ASSETS / 'blockstates/hematic_armature.json').write_text(json.dumps(blockstates(), indent=2) + '\n', encoding='utf-8')
    geometry(stage_boxes)
    for stale in ('models/block/bbmodel/HematicArmatureModel.bbmodel', 'textures/entity/model_hematic_armature.png',
                  'textures/block/model_hematic_armature.png'):
        path = ASSETS / stale
        if path.exists():
            path.unlink()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        export()
        print('Exported Hematic Armature stage sections, animated parts, Blockbench projects and ArmatureGeometry.java.')
    if args.preview:
        import hematic_armature_preview
        print(hematic_armature_preview.render())

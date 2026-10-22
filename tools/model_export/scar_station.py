"""Author the Cerebral Scarring Station: a Vein Mason's surgical lectern that carves routes into the mind.

A venous-stone plinth carries an instrument cabinet of blood-wood framed in hematic iron. Its drawers
have bone pulls, and scalpels hang from rails on both flanks. On top is a carving bed pitched toward the
mason. It holds the scar grid: an 8x8 bone lattice (one cell per ScarRecipe cell) over a flesh bed,
clamped at the corners. Beside the grid are a bone catalyst cup and a forked rest for the knapper. Blood
runs off the bed's front lip, down the cabinet's centre stile, and into a bone basin on the plinth.

Behind the bed, two iron pillars hold a blood-wood backboard. The pattern and the finished scar are
pinned to it. A shelf above carries a preserved cortex under a glass bell. Nerve cables run from its
stem down both pillars into the bed, studded with glowing synapses: the grid writes into a mind. A
surgeon's loupe swings over the grid on an articulated iron arm.

Emits:
- `scar_station`, the cutout body;
- `scar_station_glass`, the translucent bell and lens;
- the item model, which carries both;
- the two-layer facing blockstate, the cortex texture, and an editable Blockbench project.

ScarStationRenderer draws the slot items at the anchors below, so keep its constants in step.
`--preview` writes build/scar_station_preview.png, with stand-in plates at those anchors.

Model space is the north-facing block: the mason stands at z = 0. Python 3 + Pillow + NumPy.
"""
import argparse
import json
import math

import numpy as np
from PIL import Image

import station_kit as kit
from station_kit import BRIGHT, Model

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'blade': 'hemomancy:block/hematic_iron_blade',
    'brick': 'hemomancy:block/polished_venous_stone_bricks',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'wood': 'hemomancy:block/blood_wood_planks',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'flesh': 'hemomancy:block/bog_flesh',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'scab': 'hemomancy:block/blood_scorched_scab',
    'nerve': 'hemomancy:block/nerve_fiber',
    'synapse': 'hemomancy:block/synaptic_node',
    'cortex': 'hemomancy:block/scar_station_cortex',
    'glass': 'hemomancy:block/ghastly_alembic_glass',
}
TRANSLUCENT = {'glass'}
CAPS = {'bone': 'bonecap', 'pillar': 'pillartop'}
NAME = 'scar_station'

# Shared with ScarStationRenderer (model pixels, north-facing).
BED_ORIGIN = (8.0, 11.0, 2.5)       # the carving bed pitches about an x axis through its front edge
BED_PITCH = -22.5                   # negative: the back of the bed rises away from the mason
BED_TOP = 12.0                      # bed surface, in the bed's own (unpitched) frame
GRID = (5.9, 4.2, 7.2)              # x0, z0 and side of the 8x8 lattice, in the bed frame
GRID_CELLS = 8
CUP = (3.7, 9.0)                    # catalyst cup centre (x, z) in the bed frame, at the mason's right hand
REST = (3.7, 5.0)                   # knapper rest centre (x, z) in the bed frame
BOARD_FRONT = 13.4                  # pinned pattern and finished scar hang in front of this face
PINS = ((5.3, 20.0), (10.7, 20.0))  # (x, y) of the pattern and the finished scar
BELL = (8.0, 10.6)                  # glass bell centre (x, z)
LENS = (9.5, 6.7, 17.0)             # loupe centre (x, z, y), hanging over the grid below the pinned items' sightline

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -3.25, 0], 'scale': [.4, .4, .4]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, -3.5, 0], 'scale': [.42, .42, .42]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 8, 0], 'scale': [.7, .7, .7]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 1, 1], 'scale': [.28, .28, .28]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 1, 1], 'scale': [.28, .28, .28]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -2, 0], 'scale': [.28, .28, .28]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -2, 0], 'scale': [.28, .28, .28]},
}
PITCH = ('x', BED_PITCH, list(BED_ORIGIN))


def bed_point(x, y, z):
    """A bed-frame point in model space, turned the way the bed's elements are."""
    a = math.radians(BED_PITCH)
    dy, dz = y - BED_ORIGIN[1], z - BED_ORIGIN[2]
    return x, BED_ORIGIN[1] + dy * math.cos(a) - dz * math.sin(a), BED_ORIGIN[2] + dy * math.sin(a) + dz * math.cos(a)


def underside(z):
    """Height of the pitched bed's underside above model-space depth z (for the cheeks under it)."""
    return BED_ORIGIN[1] + (z - BED_ORIGIN[2]) * math.tan(math.radians(-BED_PITCH))


# ---------------------------------------------------------------- body

def plinth(m):
    """Venous-brick plinth on iron corner cleats, with a bone basin catching the bed's runoff."""
    m.box('plinth', (1.0, 0, 1.2), (15.0, 1.5, 15.6), 'brick')
    m.box('plinth_inlay', (2.2, 1.5, 2.0), (13.8, 1.7, 3.0), 'venous', faces=('up', 'north', 'east', 'west'))
    for x0, z0 in ((.6, .8), (12.6, .8), (.6, 13.2), (12.6, 13.2)):
        m.box('corner_cleat', (x0, 0, z0), (x0 + 2.8, 1.9, z0 + 2.8), 'bold')
    m.box('runoff_basin', (6.6, 1.5, 1.6), (9.4, 2.7, 3.6), 'bone')
    m.box('runoff_pool', (7.0, 2.4, 2.0), (9.0, 2.6, 3.2), 'vein', faces=('up',))


def cabinet(m):
    """Blood-wood instrument cabinet in an iron frame: four drawers with bone pulls either side of a bleeding stile."""
    m.box('cabinet', (2.6, 1.5, 4.0), (13.4, 10.0, 14.6), 'wood')
    for x0 in (2.0, 12.8):
        for z0 in (3.4, 13.6):
            m.box('cabinet_post', (x0, 1.5, z0), (x0 + 1.2, 10.0, z0 + 1.2), 'pillar')
    m.box('cabinet_stile', (7.3, 1.5, 3.6), (8.7, 10.0, 4.0), 'chisel')
    m.box('stile_vein', (7.7, 2.4, 3.45), (8.3, 10.0, 3.6), 'vein', faces=('north', 'east', 'west'))
    for x0, x1 in ((3.4, 7.1), (8.9, 12.6)):
        for y0, y1 in ((2.2, 5.6), (6.2, 9.4)):
            m.box('drawer', (x0, y0, 3.6), (x1, y1, 4.0), 'wood')
            cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
            m.box('drawer_pull', (cx - .7, cy - .25, 3.2), (cx + .7, cy + .25, 3.6), 'bone')
    m.box('cabinet_top', (1.5, 10.0, 2.5), (14.5, 11.0, 14.8), 'bold')


def instruments(m):
    """Bone-handled scalpels and probes hanging from a rail on each flank of the cabinet."""
    for side, x in (('west', 2.0), ('east', 14.0)):
        lo, hi = (x - .45, x) if side == 'west' else (x, x + .45)
        m.box('instrument_rail', (lo, 8.7, 5.0), (hi, 9.3, 11.6), 'iron')
        tlo, thi = (lo - .3, lo + .15) if side == 'west' else (hi - .15, hi + .3)
        for n, z in enumerate((5.6, 7.1, 8.6, 10.1)):
            length = (2.4, 1.8, 2.6, 2.0)[n if side == 'west' else 3 - n]
            m.box('instrument_handle', (tlo, 8.9 - length, z), (thi, 8.9, z + .5), 'bone')
            m.box('instrument_blade', (tlo + .1, 8.9 - length - 1.1, z + .1), (thi - .1, 8.9 - length, z + .4), 'blade')


def cheeks(m):
    """Stepped iron cheeks and a back block fill the wedge under the pitched bed."""
    for x0 in (1.6, 13.7):
        for z0, z1 in ((4.2, 6.6), (6.6, 9.0), (9.0, 11.4)):
            m.box('bed_cheek', (x0, 11.0, z0), (x0 + .7, underside(z0) + .05, z1), 'chisel')
    m.box('bed_back', (1.5, 11.0, 11.4), (14.5, underside(11.4) + .9, 13.0), 'iron')


def bed(m):
    """The pitched carving bed: iron slab, rim, flesh-bedded bone lattice, catalyst cup and knapper rest."""
    def box(name, a, b, mat, faces=None, extra=None):
        m.box(name, a, b, mat, rot=PITCH, faces=faces, extra=extra)

    x0, z0, side = GRID
    top = BED_TOP
    box('bed_slab', (1.5, top - 1.0, 2.5), (14.5, top, 12.0), 'bold')
    box('bed_lip', (1.5, top, 2.5), (14.5, top + .7, 3.3), 'iron')
    box('bed_rim', (1.5, top, 11.2), (14.5, top + .7, 12.0), 'iron')
    for a, b in ((1.5, 2.3), (13.7, 14.5)):
        box('bed_rim', (a, top, 3.3), (b, top + .7, 11.2), 'iron')
    # The scar grid: a flesh bed under a bone lattice, its corners clamped in iron.
    box('grid_bed', (x0, top, z0), (x0 + side, top + .15, z0 + side), 'flesh', faces=('up',))
    for i in range(GRID_CELLS + 1):
        t = i * side / GRID_CELLS
        box('grid_bar', (x0 + t - .12, top, z0), (x0 + t + .12, top + .35, z0 + side), 'bone')
        box('grid_bar', (x0, top + .02, z0 + t - .12), (x0 + side, top + .37, z0 + t + .12), 'bone')
    for cx in (x0, x0 + side):
        for cz in (z0, z0 + side):
            box('grid_clamp', (cx - .55, top, cz - .55), (cx + .55, top + .65, cz + .55), 'chisel')
    # Runoff channel from the grid's front edge over the lip.
    box('runoff_channel', (7.6, top, 2.5), (8.4, top + .72, z0 - .12), 'vein', faces=('up', 'north'))
    # Catalyst cup.
    cx, cz = CUP
    box('cup_floor', (cx - 1.0, top, cz - 1.0), (cx + 1.0, top + .4, cz + 1.0), 'scab')
    for a, b in (((cx - 1.3, cz - 1.3), (cx + 1.3, cz - 1.0)), ((cx - 1.3, cz + 1.0), (cx + 1.3, cz + 1.3)),
                 ((cx - 1.3, cz - 1.0), (cx - 1.0, cz + 1.0)), ((cx + 1.0, cz - 1.0), (cx + 1.3, cz + 1.0))):
        box('cup_wall', (a[0], top, a[1]), (b[0], top + 1.4, b[1]), 'bone')
    # Knapper rest: two bone forks the tool lies across.
    rx, rz = REST
    for dz in (-1.5, 1.5):
        for dx in (-.9, .5):
            box('rest_fork', (rx + dx, top, rz + dz - .3), (rx + dx + .4, top + 1.0, rz + dz + .3), 'bone')


def backboard(m):
    """Iron pillars, a blood-wood board bordered in bone with two pins, and a lintel with bone finials."""
    for x0 in (.8, 13.4):
        m.box('pillar', (x0, 11.0, 12.6), (x0 + 1.8, 29.0, 14.6), 'pillar')
        m.box('pillar_finial', (x0 - .2, 30.4, 12.4), (x0 + 2.0, 31.2, 14.8), 'bone')
    m.box('lintel', (.6, 29.0, 12.4), (15.4, 30.4, 14.8), 'bold')
    m.box('board', (2.6, 13.0, 13.4), (13.4, 25.0, 14.4), 'wood')
    m.box('board_frame', (2.6, 24.3, 13.1), (13.4, 25.0, 13.4), 'bone')
    m.box('board_frame', (2.6, 15.0, 13.1), (13.4, 15.6, 13.4), 'bone')
    m.box('board_frame', (7.75, 15.6, 13.1), (8.25, 24.3, 13.4), 'bone')
    for y in (17.6, 22.0):
        m.box('board_strap', (2.6, y, 14.4), (13.4, y + .8, 14.7), 'iron', faces=('south', 'up', 'down'))
    for x, y in PINS:
        m.box('board_pin', (x - .3, y + 2.6, 12.9), (x + .3, y + 3.2, 13.4), 'chisel')


def reliquary(m):
    """Shelf, bracketed to the board, carrying a preserved cortex under a bone-knobbed glass bell."""
    bx, bz = BELL
    m.box('shelf', (4.2, 25.0, 7.8), (11.8, 26.0, 14.4), 'bold')
    for x in (4.6, 10.6):
        m.box('shelf_bracket', (x, 23.4, 11.0), (x + .8, 25.0, 13.4), 'bone')
        m.box('shelf_bracket', (x, 22.4, 12.4), (x + .8, 23.4, 13.4), 'bone')
    c3 = (bx, 0, bz)
    m.octo('bell_seat', 'y', c3, 3.05, 26.0, 26.5, 'chisel')
    m.shell('bell', (bx, bz), 2.75, 26.5, 30.7, .3, 'glass')
    m.octo('bell_crown', 'y', c3, 2.75, 30.7, 31.05, 'glass')
    m.box('bell_knob', (bx - .5, 31.05, bz - .5), (bx + .5, 31.8, bz + .5), 'bone')
    # The cortex: two lobed hemispheres split by a fissure that glows with the routes carved below.
    for x0, x1, out in ((6.2, 7.9, -1), (8.1, 9.8, 1)):
        m.box('cortex_hemisphere', (x0, 27.2, bz - 1.3), (x1, 29.2, bz + 1.3), 'cortex')
        m.box('cortex_crown', (x0 + .25, 29.2, bz - 1.0), (x1 - .25, 29.8, bz + 1.0), 'cortex')
        m.box('cortex_frontal', (x0 + .2, 27.5, bz - 1.65), (x1 - .2, 28.9, bz - 1.3), 'cortex')
        m.box('cortex_occipital', (x0 + .2, 27.6, bz + 1.3), (x1 - .2, 28.9, bz + 1.6), 'cortex')
        edge = x0 if out < 0 else x1
        m.box('cortex_temporal', (min(edge, edge + .3 * out), 26.8, bz - .9), (max(edge, edge + .3 * out), 28.0, bz + .9),
              'cortex')
    m.box('cortex_fissure', (7.9, 27.3, bz - 1.5), (8.1, 29.6, bz + 1.45), 'vein', extra=BRIGHT)
    m.box('cerebellum', (6.8, 26.6, bz + 1.0), (9.2, 27.2, bz + 1.85), 'cortex')
    m.box('brain_stem', (7.6, 26.0, bz + .4), (8.4, 26.9, bz + 1.2), 'nerve')


def nerves(m):
    """Nerve cables from under the shelf down both pillars into the bed, studded with lit synapses."""
    m.box('nerve_root', (7.5, 24.3, 11.9), (8.5, 25.0, 12.9), 'nerve')
    for x0, x1, run in (((1.4, 2.0), (2.0, 7.5), 'west'), ((14.2, 14.8), (8.5, 14.2), 'east')):
        m.box('nerve_cable', (x1[0], 24.4, 12.1), (x1[1], 24.9, 12.6), 'nerve')
        m.box('nerve_cable', (x0[0], underside(12.0) + .6, 12.0), (x0[1], 24.9, 12.6), 'nerve')
        cx = (x0[0] + x0[1]) / 2
        for y in (19.8, 22.4):
            m.box('synapse', (cx - .5, y, 11.85), (cx + .5, y + 1.0, 12.75), 'synapse', extra=BRIGHT)
    m.box('synapse', (7.5, 23.7, 11.75), (8.5, 24.5, 12.75), 'synapse', extra=BRIGHT)


def loupe(m):
    """An articulated iron arm off the east pillar, holding a bone-ringed lens over the grid.

    The arm runs low and outboard of the board so it never crosses the pinned pattern or scar.
    """
    lx, lz, ly = LENS
    arm = ly + .9
    m.box('loupe_mount', (13.3, arm - .4, 11.9), (14.15, arm + 1.2, 12.6), 'chisel')
    m.box('loupe_arm', (13.4, arm, lz - .4), (14.05, arm + .8, 11.9), 'iron')
    m.box('loupe_knuckle', (13.25, arm - .2, lz - .6), (14.2, arm + 1.0, lz + .6), 'chisel')
    m.box('loupe_arm', (lx - .3, arm, lz - .4), (13.25, arm + .8, lz + .4), 'iron')
    m.box('loupe_drop', (lx - .3, ly + .3, lz - .3), (lx + .3, arm, lz + .3), 'iron')
    m.shell('loupe_ring', (lx, lz), 1.6, ly - .3, ly + .3, .35, 'bone', ends=True)
    m.octo('loupe_lens', 'y', (lx, 0, lz), 1.3, ly - .1, ly + .1, 'glass')


def station():
    m = Model(TRANSLUCENT, CAPS)
    plinth(m)
    cabinet(m)
    instruments(m)
    cheeks(m)
    bed(m)
    backboard(m)
    reliquary(m)
    nerves(m)
    loupe(m)
    return m


# ---------------------------------------------------------------- texture

def cortex_texture():
    """Preserved grey-pink tissue: winding gyri cut by darker sulci, lit along each fold's crest."""
    rng = np.random.default_rng(31)
    noise = rng.random((16, 16))
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    for y in range(16):
        for x in range(16):
            wave = math.sin(x * .55 + 1.3) * 1.8 + math.sin(x * .21 + 4.1) * 1.2
            fold = (y + int(round(wave))) % 4
            n = int(noise[y, x] * 10)
            if fold == 0:
                base = (104 + n // 2, 58, 62)
            elif fold == 1:
                base = (190 + n, 136 + n, 128 + n)
            else:
                base = (164 + n, 112 + n, 108 + n) if fold == 2 else (150 + n, 100 + n, 98 + n)
            px[x, y] = base + (255,)
    return image


def write_textures():
    cortex_texture().save(kit.ASSETS / 'textures/block/scar_station_cortex.png')


# ---------------------------------------------------------------- export

def export():
    write_textures()
    body = station()
    kit.separate([body], TEXTURES, TRANSLUCENT, 'Cerebral Scarring Station')
    # Body and glass bake into separate layers (see station_kit.split_glass); the item keeps both.
    solid, glass = kit.split_glass(body)
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'bold', DISPLAY))
    kit.write_json(kit.MODELS / (NAME + '_glass.json'), kit.to_json(glass, TEXTURES, 'bold'))
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(body, TEXTURES, 'bold', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), kit.two_layer_blockstate(NAME), indent=2)
    kit.BBMODEL.mkdir(parents=True, exist_ok=True)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(
        json.dumps(kit.bbmodel(NAME, [('station', body)], TEXTURES, DISPLAY)), encoding='utf-8')
    for stale in ('textures/block/model_scar_station.png', 'textures/entity/model_scar_station.png'):
        path = kit.ASSETS / stale
        if path.exists():
            path.unlink()
    return body


def stand_ins():
    """Where ScarStationRenderer puts each slot's item, as flat plates for previews only."""
    m = Model()
    x0, z0, side = GRID
    m.box('blank_scar', (x0 + .9, BED_TOP + .4, z0 + .9), (x0 + side - .9, BED_TOP + .5, z0 + side - .9), 'scab',
          rot=PITCH)
    cx, cz = CUP
    m.box('catalyst', (cx - 1.2, BED_TOP + .4, cz - .1), (cx + 1.2, BED_TOP + 3.6, cz + .1), 'synapse', rot=PITCH)
    rx, rz = REST
    m.box('knapper', (rx - .3, BED_TOP + 1.0, rz - 2.6), (rx + .3, BED_TOP + 1.2, rz + 2.6), 'blade', rot=PITCH)
    for (x, y), mat in zip(PINS, ('wood', 'scab')):
        m.box('pinned', (x - 2.3, y - 2.3, BOARD_FRONT - .2), (x + 2.3, y + 2.3, BOARD_FRONT - .1), mat)
    return m


def preview():
    body, items = station(), stand_ins()
    stocked = [(body, kit.identity), (items, kit.identity)]
    panels = [('Mason side', 200, 22, stocked), ('East flank', 120, 18, [(body, kit.identity)]),
              ('Behind', 20, 24, [(body, kit.identity)]), ('Above', 205, 58, stocked)]
    return kit.preview(panels, 4, kit.ROOT / 'build/scar_station_preview.png', (420, 600), 15, (8, 15, 8), TEXTURES)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Cerebral Scarring Station: %d elements' % len(model.elements))
    if args.preview:
        print(preview())

"""Author the Somatic Loom: a floor loom that weaves blood-memory on a warp of living nerve.

Two hematic iron side frames stand on bone claw feet, tied by blood-wood beams. The warp is pale
nerve fibre. It is wound on a nerve-bundle roll at the back, passes over the back rest, through
two heddle shafts and an underslung reed, and is beaten into the tapestry at the fell. The fell
glows under the floating memory. The woven tapestry runs over the breast beam and down onto the
cloth beam, which has a bone-handled crank. A bone castle carries the heddles from a nerve
ganglion; its veins run down the uprights. Treadles below are corded to the shafts. Eight bone
pirns stand on shelves off the side frames, each over a glass vial: the enzyme stands in the vial as
liquid and a filament rises from it to the pirn, where it is spun into thread for a weave.

Emits the cutout block model and its translucent `somatic_loom_glass` layer (one facing multipart
applies both), the item model (pirns wound, vials full), the eight dyed `somatic_loom_cop_<tendency>`
parts SomaticLoomRenderer winds onto the pirns for a weave, the eight `somatic_loom_vial_<tendency>`
liquid parts it fills by stored enzyme, an editable Blockbench project and SomaticLoomGeometry.java,
the anchors the renderer and the weave's server logic follow. `--preview` writes
build/somatic_loom_preview.png.

Model space is the north-facing main block: the weaver stands at -z (the front filler), the warp
roll sits in the back filler, and the castle rises into the filler above. SomaticLoomRenderer
floats the memory over the fell at the block centre. Python 3 + Pillow + NumPy.
"""
import argparse
import json
import math

import station_kit as kit
from station_kit import BRIGHT, Model

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'log': 'hemomancy:block/blood_wood_log',
    'logtop': 'hemomancy:block/blood_wood_log_top',
    'plank': 'hemomancy:block/blood_wood_planks',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'cloth': 'hemomancy:block/puppeteers_wool',
    'nerve': 'hemomancy:block/nerve_bundle',
    'nervetop': 'hemomancy:block/nerve_bundle_top',
    'hyphae': 'hemomancy:block/calcified_hyphae',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'scab': 'hemomancy:block/blood_scorched_scab',
    # Pirns in EnumBloodTendency order.
    'animus': 'minecraft:block/red_wool',
    'flammeus': 'minecraft:block/orange_wool',
    'ductilis': 'minecraft:block/yellow_wool',
    'lux': 'minecraft:block/white_wool',
    'mortem': 'minecraft:block/green_wool',
    'congeatio': 'minecraft:block/blue_wool',
    'ferric': 'minecraft:block/gray_wool',
    'tenebris': 'minecraft:block/purple_wool',
    'vialglass': 'hemomancy:block/vial_glass',
    # Enzyme liquid, tinted glass in EnumBloodTendency order.
    'liq_animus': 'minecraft:block/red_stained_glass',
    'liq_flammeus': 'minecraft:block/orange_stained_glass',
    'liq_ductilis': 'minecraft:block/yellow_stained_glass',
    'liq_lux': 'minecraft:block/white_stained_glass',
    'liq_mortem': 'minecraft:block/green_stained_glass',
    'liq_congeatio': 'minecraft:block/blue_stained_glass',
    'liq_ferric': 'minecraft:block/gray_stained_glass',
    'liq_tenebris': 'minecraft:block/purple_stained_glass',
}
CAPS = {'bone': 'bonecap', 'pillar': 'pillartop', 'log': 'logtop', 'nerve': 'nervetop'}
NAME = 'somatic_loom'
TENDENCIES = ('animus', 'flammeus', 'ductilis', 'lux', 'mortem', 'congeatio', 'ferric', 'tenebris')
TRANSLUCENT = {'vialglass'} | {'liq_' + t for t in TENDENCIES}

# Side frames (x spans) and the stations along the warp (z), front to back.
FRAMES = ((-4.5, -1.5), (17.5, 20.5))
FRONT_POST, CASTLE, BACK_POST = (-10.0, -7.0), (14.6, 17.6), (23.0, 26.0)
SILL_Y = (1.5, 4.0)
WARP = [1.0 + k for k in range(15)]          # thread centres; even k ride shaft A, odd k shaft B
UPPER, LOWER = (14.35, 14.6), (13.1, 13.35)  # the open shed
CLOTH_Y = (13.2, 14.4)
FELL_Z = 7.5                                 # shared with SomaticLoomRenderer.ORB_THREAD_CENTER_Y
REED_Z = (10.4, 11.0)
SHAFTS = ((15.0, 15.5), (16.7, 17.2))
BACK_REST = (14.0, 20.8)                     # roller centre (y, z)
BREAST = (12.8, -8.6)
CLOTH_BEAM = (7.2, -7.0)
WARP_ROLL = (11.0, 24.7)
CROSSBAR_Y = (22.0, 23.6)
HARNESS_Z = (SHAFTS[0][0] + SHAFTS[1][1]) / 2
UPRIGHT_VEINS = (((FRAMES[0][1] - .2, SILL_Y[1] + .5, 15.8), (FRAMES[0][1] + .05, CROSSBAR_Y[0], 16.4)),
                 ((FRAMES[1][0] - .05, SILL_Y[1] + .5, 15.8), (FRAMES[1][0] + .2, CROSSBAR_Y[0], 16.4)))
CROSSBAR_VEIN = ((-1.5, CROSSBAR_Y[0] - .2, HARNESS_Z - .3), (17.5, CROSSBAR_Y[0], HARNESS_Z + .3))
GANGLION = (2.2, 21.2, 24.3)                 # radius, foot, top; centred on (8, HARNESS_Z)
# Upgrade stages: base, Ganglionic (the knot grows and puts out feelers), Cortical (a cortex crowns it).
STAGES = ('', '_ganglionic', '_cortical')
GANGLION_STAGES = (GANGLION, (2.9, 20.7, 25.0), (2.9, 20.7, 25.0))
# Ganglionic feelers hang from the crossbar toward the echo: (x, bottom y) at FEELER_Z, hooked forward.
FEELERS = ((1.5, 16.6), (4.2, 17.6), (11.8, 17.2), (14.5, 16.2))
FEELER_Z = 14.3
CROWN_LOBES = ((6.3, 16.1), (9.7, 16.1), (8.0, 14.4), (8.0, 17.8))
MYELIN_Z = ((9.5, 10.2), (12.8, 13.6), (18.0, 18.8))
# Pirn (x, z) in EnumBloodTendency order: four on the -x shelf, four on the +x shelf.
PIRNS = tuple((xc, z) for xc in (FRAMES[0][0] - 1.8, FRAMES[1][1] + 1.8) for z in (0.0, 3.3, 6.6, 9.9))
PIRN_FLOOR, PIRN_HEIGHT, PIRN_RADIUS = 8.1, 3.3, .85
# Enzyme vials hang under the pirn shelf: the stock stands as liquid, a filament rises from it to the pirn.
# Square vials: sub-pixel octagons this small read as fanned slabs in game, so they stay axis-aligned.
VIAL_FLOOR, VIAL_DEPTH, VIAL_RADIUS, VIAL_TOP = 3.7, 2.4, 1.0, 6.5
# The echo stands on the warp between the reed and the heddles.
ECHO = (8.0, 15.0, 12.5)
# Renderer item anchors: the memory over the fell, catalysts laid on the fresh cloth, the pattern above the ganglion.
MEMORY_Y = 18.5
CATALYST_ROW = (3.5, 12.5, 17.0, 2.2)        # x0, x1, y, z
PREVIEW_Y = 27.5

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -1.5, 0], 'scale': [.32, .32, .32]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.15, .15, .15]},
    'fixed': {'rotation': [0, 180, 0], 'translation': [0, -2, 0], 'scale': [.3, .3, .3]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 9, 0], 'scale': [.4, .4, .4]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2, 0], 'scale': [.12, .12, .12]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2, 0], 'scale': [.12, .12, .12]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 2, 0], 'scale': [.13, .13, .13]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 2, 0], 'scale': [.13, .13, .13]},
}


def roller(m, name, centre, radius, x0, x1, mat, cap=None):
    """Octagonal beam across the loom; centre is (y, z)."""
    m.octo(name, 'x', (0, centre[0], centre[1]), radius, x0, x1, mat, cap=cap)


def brace(m, name, x0, x1, start, end, thick, mat):
    """Square-section strut between two (y, z) points that differ equally in y and z (a 45 degree pitch)."""
    (ya, za), (yb, zb) = start, end
    yc, zc = (ya + yb) / 2, (za + zb) / 2
    half = math.hypot(yb - ya, zb - za) / 2
    angle = -45 if (yb - ya) * (zb - za) > 0 else 45
    m.box(name, (x0, yc - thick / 2, zc - half), (x1, yc + thick / 2, zc + half), mat, rot=('x', angle, [x0, yc, zc]))


def frames(m, stage=0):
    """Two iron side frames on bone claw feet: sill, front and back posts, the castle upright and its braces."""
    for n, (x0, x1) in enumerate(FRAMES):
        out = -1 if n == 0 else 1
        xc = (x0 + x1) / 2
        for z0, z1 in (FRONT_POST, BACK_POST):
            zc = (z0 + z1) / 2
            m.box('claw_heel', (x0 - .3, 0, z0 - .3), (x1 + .3, SILL_Y[0], z1 + .3), 'bone')
            for dz in (-1, 1):
                m.box('claw_toe', (xc - .45, 0, zc + dz * 1.8 - .45), (xc + .45, .7, zc + dz * 2.6 + .45), 'bonecap')
            m.box('claw_toe', (xc + out * 1.8 - .45, 0, zc - .45), (xc + out * 2.6 + .45, .7, zc + .45), 'bonecap')
        m.box('sill', (x0, SILL_Y[0], FRONT_POST[0]), (x1, SILL_Y[1], BACK_POST[1]), 'bold')
        m.box('front_post', (x0 + .25, SILL_Y[1], FRONT_POST[0] + .25), (x1 - .25, 15.0, FRONT_POST[1] - .25), 'pillar')
        m.box('front_capital', (x0 - .15, 15.0, FRONT_POST[0] - .15), (x1 + .15, 15.9, FRONT_POST[1] + .15), 'bone')
        m.box('back_post', (x0 + .25, SILL_Y[1], BACK_POST[0] + .25), (x1 - .25, 15.0, BACK_POST[1] - .25), 'pillar')
        m.box('back_capital', (x0 - .15, 15.0, BACK_POST[0] - .15), (x1 + .15, 15.9, BACK_POST[1] + .15), 'bone')
        m.box('castle_upright', (x0 + .2, SILL_Y[1], CASTLE[0]), (x1 - .2, 25.0, CASTLE[1]), 'pillar')
        for y in (8.0, 18.2):
            m.box('castle_collar', (x0 - .2, y, CASTLE[0] - .2), (x1 + .2, y + .9, CASTLE[1] + .2), 'bone')
        m.octo('castle_finial', 'y', (xc, 0, (CASTLE[0] + CASTLE[1]) / 2), 1.6, 25.0, 25.8, 'bone')
        m.octo('castle_finial_knob', 'y', (xc, 0, (CASTLE[0] + CASTLE[1]) / 2), .9, 25.8, 26.6, 'bonecap')
        # A-frame struts from the sill up to the castle, one from each side.
        brace(m, 'castle_brace', x0 + .6, x1 - .6, (SILL_Y[1], 6.0), (SILL_Y[1] + 8.4, CASTLE[0] - .2), 1.2, 'iron')
        brace(m, 'castle_brace', x0 + .6, x1 - .6, (SILL_Y[1], 26.2), (SILL_Y[1] + 8.4, CASTLE[1] + .2), 1.2, 'iron')
        # The loom's own circulation: veins climb each upright's inner face to the ganglion.
        # Drained until the renderer fills them from the Loom's reservoir.
        a, b = UPRIGHT_VEINS[n]
        m.box('upright_vein', a, b, 'scab')
        if stage >= 1:
            # Ganglionic: a nerve cord grown down the upright's outer face from the crossbar to the sill.
            outer = x0 if n == 0 else x1
            zc = (CASTLE[0] + CASTLE[1]) / 2
            lo, hi = sorted((outer + out * .1, outer + out * .6))
            m.box('upright_nerve', (lo, SILL_Y[1], zc - .3), (hi, CROSSBAR_Y[0] - .3, zc + .3), 'nerve')
            for y in (9.5, 14.0, 19.0):
                lo, hi = sorted((outer - out * .05, outer + out * .85))
                m.octo('upright_node', 'x', (0, y, zc), .55, lo, hi, 'nerve', cap='nervetop')
    for z0, z1 in ((-10.0, -8.2), (23.4, 25.4)):
        m.box('stretcher', (FRAMES[0][1], 2.0, z0), (FRAMES[1][0], 3.5, z1), 'iron')


def beams(m):
    """Breast beam and cloth beam at the front, back rest and nerve-wound warp roll at the back."""
    left, right = FRAMES[0][0], FRAMES[1][1]
    roller(m, 'breast_beam', BREAST, 1.4, left, right, 'log')
    roller(m, 'cloth_axle', CLOTH_BEAM, .9, left - .6, right + .6, 'iron')
    roller(m, 'cloth_roll', CLOTH_BEAM, 2.4, .5, 15.5, 'cloth', cap='cloth')
    roller(m, 'back_rest', BACK_REST, .9, left, right, 'log')
    roller(m, 'warp_axle', WARP_ROLL, .9, left - .6, right + .6, 'iron')
    roller(m, 'warp_roll', WARP_ROLL, 3.0, .5, 15.5, 'nerve')
    for x0, x1 in ((-.4, .5), (15.5, 16.4)):
        roller(m, 'warp_flange', WARP_ROLL, 3.4, x0, x1, 'bone')
    # Ratchets hold each beam; a bone-handled crank winds the finished cloth on.
    roller(m, 'cloth_ratchet', CLOTH_BEAM, 2.0, left - 1.4, left - .6, 'bone')
    m.box('cloth_pawl', (left - 1.3, CLOTH_BEAM[0] + 1.6, CLOTH_BEAM[1] + .4), (left - .7, CLOTH_BEAM[0] + 2.4, CLOTH_BEAM[1] + 3.6), 'iron')
    roller(m, 'warp_ratchet', WARP_ROLL, 2.0, right + .6, right + 1.4, 'bone')
    m.box('warp_pawl', (right + .7, WARP_ROLL[0] + 1.6, WARP_ROLL[1] - 3.6), (right + 1.3, WARP_ROLL[0] + 2.4, WARP_ROLL[1] - .4), 'iron')
    y, z = CLOTH_BEAM
    m.box('crank_arm', (right + .6, y - 3.6, z - .45), (right + 1.4, y + .45, z + .45), 'iron')
    m.box('crank_handle', (right + 1.4, y - 3.6, z - .4), (right + 3.6, y - 2.8, z + .4), 'bone')


def cloth(m):
    """The woven tapestry from the fell over the breast beam and down to the cloth roll, its weft glowing."""
    y0, y1 = CLOTH_Y
    m.box('tapestry', (.5, y0, BREAST[1]), (15.5, y1, FELL_Z), 'cloth')
    m.box('tapestry_fall', (.5, CLOTH_BEAM[0], BREAST[1] - 1.75), (15.5, BREAST[0], BREAST[1] - 1.35), 'cloth')
    # Weft picks laid since the last memory: brightest at the fell, cooling toward the breast beam.
    for z in (-1.4, -4.0):
        m.box('woven_weft', (.5, y1, z), (15.5, y1 + .06, z + .45), 'vein')
    for x0, x1, z0, z1 in ((3.0, 4.6, -3.4, -2.6), (7.2, 8.8, -4.8, -2.0), (11.4, 13.0, -3.4, -2.6), (5.0, 11.0, -6.6, -6.1)):
        m.box('woven_glyph', (x0, y1, z0), (x1, y1 + .06, z1), 'vein')
    m.box('fell', (.5, y0 + .05, FELL_Z - .5), (15.5, y1 - .05, FELL_Z), 'vein', extra=BRIGHT)


def warp(m, stage=0):
    """Nerve-fibre warp from the back rest to the fell, open in a shed with a bone shuttle thrown through it."""
    for k, x in enumerate(WARP):
        y0, y1 = UPPER if k % 2 == 0 else LOWER
        m.box('warp_thread', (x - .15, y0, FELL_Z), (x + .15, y1, BACK_REST[1]), 'hyphae')
    y = (LOWER[1] + UPPER[0]) / 2
    m.octo('shuttle', 'x', (0, y, 8.9), .45, 1.6, 14.4, 'bone')
    for x0, x1 in ((.2, 1.6), (14.4, 15.8)):
        m.octo('shuttle_tip', 'x', (0, y, 8.9), .22, x0, x1, 'bonecap')
    m.box('shuttle_pirn', (5.0, y + .3, 8.7), (11.0, y + .5, 9.1), 'animus')
    m.box('weft_strand', (4.6, y - .1, FELL_Z), (4.9, y + .1, 8.5), 'vein', extra=BRIGHT)
    if stage >= 2:
        # Cortical: the warp is myelinated, pale sheaths at intervals along every thread.
        for k, x in enumerate(WARP):
            y0, y1 = UPPER if k % 2 == 0 else LOWER
            for z0, z1 in MYELIN_Z:
                m.box('myelin_sheath', (x - .24, y0 - .08, z0), (x + .24, y1 + .08, z1), 'hyphae')


def reed(m):
    """Underslung beater: rocker shaft, two swords, the shuttle race, iron dents and a bone hand-tree."""
    z0, z1 = REED_Z
    zc = (z0 + z1) / 2
    left, right = FRAMES[0][1], FRAMES[1][0]
    roller(m, 'rocker_shaft', (3.4, zc), .7, left, right, 'iron')
    for x0, x1 in ((-1.4, -.4), (16.4, 17.4)):
        m.box('beater_sword', (x0, 3.4, z0 - .1), (x1, 15.9, z1 + .1), 'plank')
    m.box('shuttle_race', (-.4, 11.7, z0 - 1.0), (16.4, 12.8, z1 + .2), 'plank')
    m.box('hand_tree', (-.4, 15.0, z0 - .1), (16.4, 15.9, z1 + .1), 'bone')
    for x in WARP[:-1]:
        m.box('reed_dent', (x + .44, 12.8, z0 + .1), (x + .56, 15.0, z1 - .1), 'iron')


def harness(m, stage=0):
    """Two heddle shafts hung from the castle's ganglion over bone pulleys, corded down to the treadles."""
    for n, (z0, z1) in enumerate(SHAFTS):
        m.box('shaft_head', (-.8, 18.6, z0), (16.8, 19.4, z1), 'chisel')
        m.box('shaft_foot', (-.8, 9.4, z0), (16.8, 10.2, z1), 'chisel')
        for k, x in enumerate(WARP):
            if k % 2 == n:
                m.box('heddle', (x - .07, 10.2, z0 + .15), (x + .07, 18.6, z1 - .15), 'hyphae')
                eye = UPPER if n == 0 else LOWER
                m.box('heddle_eye', (x - .2, eye[0] - .3, z0 + .1), (x + .2, eye[1] + .3, z1 - .1), 'bone')
    zc = HARNESS_Z
    m.box('castle_crossbar', (FRAMES[0][0], CROSSBAR_Y[0], CASTLE[0] + .5), (FRAMES[1][1], CROSSBAR_Y[1], CASTLE[1] - .5), 'iron')
    m.box('crossbar_vein', *CROSSBAR_VEIN, 'scab')
    # The ganglion that works the shafts: a knot of nerve bound in bone on the crossbar.
    radius, foot, top = GANGLION_STAGES[stage]
    m.octo('ganglion', 'y', (8, 0, zc), radius, foot, top, 'nerve')
    band = (22.3, 23.1) if stage == 0 else (22.2, 23.0)
    m.octo('ganglion_band', 'y', (8, 0, zc), radius + .25, band[0], band[1], 'bone')
    m.octo('ganglion_crown', 'y', (8, 0, zc), 1.2 if stage == 0 else 1.6, top, top + .6, 'vein', extra=BRIGHT)
    if stage >= 1:
        # Ganglionic: nerve coils knot along the crossbar, and feelers hang from it toward the echo.
        for x in (-.5, 1.5, 3.5, 12.5, 14.5, 16.5):
            m.box('crossbar_coil', (x - .3, CROSSBAR_Y[0] - .3, CASTLE[0] + .2), (x + .3, CROSSBAR_Y[1] + .3, CASTLE[1] - .2), 'nerve')
        for x, bottom in FEELERS:
            m.box('feeler', (x - .22, bottom, FEELER_Z - .22), (x + .22, CROSSBAR_Y[0] - .5, FEELER_Z + .22), 'nerve')
            m.box('feeler_root', (x - .28, CROSSBAR_Y[0] - .75, FEELER_Z - .28), (x + .28, CROSSBAR_Y[0] - .45, CASTLE[0] + .7), 'nerve')
            m.box('feeler_hook', (x - .15, bottom, FEELER_Z - 1.0), (x + .15, bottom + .35, FEELER_Z - .22), 'bonecap')
            m.octo('feeler_tip', 'y', (x, 0, FEELER_Z - 1.0), .26, bottom - .15, bottom + .5, 'vein', extra=BRIGHT)
    if stage >= 2:
        # Cortical: a folded cortex crowns the ganglion, bound by an iron arch between the castle finials.
        for x, z in CROWN_LOBES:
            m.octo('cortex_lobe', 'y', (x, 0, z), 1.3, top - .4, top + 1.4, 'hyphae')
        m.octo('cortex_vein', 'y', (8, 0, zc), 2.3, top + .45, top + .65, 'vein', extra=BRIGHT)
        left, right = (FRAMES[0][0] + FRAMES[0][1]) / 2, (FRAMES[1][0] + FRAMES[1][1]) / 2
        m.box('crown_arch', (left, 25.9, zc - .5), (right, 26.6, zc + .5), 'iron')
        m.box('crown_vein', (left + 1.0, 26.6, zc - .15), (right - 1.0, 26.75, zc + .15), 'vein', extra=BRIGHT)
        for x in (0.0, 4.0, 12.0, 16.0):
            m.box('crown_point', (x - .35, 26.6, zc - .35), (x + .35, 27.5, zc + .35), 'iron')
            m.box('crown_tip', (x - .2, 27.5, zc - .2), (x + .2, 27.9, zc + .2), 'bonecap')
    for x in (3.0, 13.0):
        m.octo('pulley', 'z', (x, 21.0, 0), .85, SHAFTS[0][0] - .2, SHAFTS[1][1] + .2, 'bone')
        m.box('pulley_hanger', (x - .2, 21.0, zc - .2), (x + .2, CROSSBAR_Y[0], zc + .2), 'iron')
        for z0, z1 in SHAFTS:
            m.box('shaft_cord', (x - .1, 19.4, (z0 + z1) / 2 - .1), (x + .1, 20.15, (z0 + z1) / 2 + .1), 'hyphae')
    # Lamm cords tie each shaft to its treadle.
    for (z0, z1), x in zip(SHAFTS, (4.5, 11.5)):
        m.box('treadle_cord', (x - .1, 2.4, (z0 + z1) / 2 - .1), (x + .1, 9.4, (z0 + z1) / 2 + .1), 'hyphae')


def treadles(m):
    """Two blood-wood treadles pivoting on a rear bar, bone foot-pads under the weaver."""
    left, right = FRAMES[0][1], FRAMES[1][0]
    roller(m, 'treadle_pivot', (1.9, 19.8), .7, left, right, 'iron')
    for x in (4.5, 11.5):
        m.box('treadle', (x - 1.0, 1.6, -6.4), (x + 1.0, 2.4, 20.5), 'plank')
        m.box('treadle_pad', (x - 1.2, 2.4, -6.2), (x + 1.2, 2.8, -3.8), 'bone')


def vial(m, x, z, tendency, stage):
    """The vial beneath a pirn: square glass on an iron foot, capped and hung from the shelf."""
    g = VIAL_RADIUS + .15
    for s in (-1, 1):
        # Broad faces only: a pane's thin edge reads as a stripe through the glass.
        m.box('vial_glass', (x + s * g - .05, VIAL_FLOOR, z - g), (x + s * g + .05, VIAL_TOP, z + g), 'vialglass',
              faces=('east', 'west'))
        m.box('vial_glass', (x - g, VIAL_FLOOR, z + s * g - .05), (x + g, VIAL_TOP, z + s * g + .05), 'vialglass',
              faces=('north', 'south'))
    m.box('vial_foot', (x - g - .15, VIAL_FLOOR - .5, z - g - .15), (x + g + .15, VIAL_FLOOR, z + g + .15), 'iron')
    m.box('vial_cap', (x - g - .15, VIAL_TOP, z - g - .15), (x + g + .15, VIAL_TOP + .4, z + g + .15), 'iron')
    m.box('vial_hanger', (x - .15, VIAL_TOP + .4, z - .15), (x + .15, 7.0, z + .15), 'iron')
    m.box('filament', (x - .1, VIAL_FLOOR + .2, z - .1), (x + .1, VIAL_TOP, z + .1), tendency)
    if stage >= 1:
        # Ganglionic: a nerve band grips each vial and a cord climbs to the shelf.
        m.box('vial_nerve', (x - g - .2, VIAL_FLOOR + 1.0, z - g - .2), (x + g + .2, VIAL_FLOOR + 1.5, z + g + .2), 'nerve')
        m.box('vial_nerve_cord', (x + g + .05, VIAL_FLOOR + 1.5, z - .15), (x + g + .35, 7.0, z + .15), 'nerve')


def pirns(m, stage=0):
    """Eight dyed pirns on bone shelves outside the frames: the Loom's tendency enzyme reservoirs."""
    for n, (x0, x1) in enumerate(FRAMES):
        out = -1 if n == 0 else 1
        face = x0 if n == 0 else x1
        shelf = (face + out * 3.6, face)
        m.box('pirn_shelf', (min(shelf), 7.0, -2.2), (max(shelf), 7.6, 12.2), 'bone')
        for z in (-1.75, 11.65):
            m.box('shelf_bracket', (min(shelf) + (1.2 if n == 0 else 0), 4.6, z - .3), (max(shelf) - (0 if n == 0 else 1.2), 7.0, z + .3), 'iron')
        for k in range(4):
            x, z = PIRNS[n * 4 + k]
            tendency = TENDENCIES[n * 4 + k]
            m.box('pirn_flange', (x - 1.05, 7.6, z - 1.05), (x + 1.05, PIRN_FLOOR, z + 1.05), 'bone', caps={'up': 'bonecap', 'down': 'bonecap'})
            m.octo('pirn_spindle', 'y', (x, 0, z), .3, PIRN_FLOOR, PIRN_FLOOR + PIRN_HEIGHT + 1.0, 'bonecap')
            vial(m, x, z, tendency, stage)


def cop(tendency, height=1.0):
    """Thread spun onto one pirn for a weave; the part is one pixel tall and the renderer stretches it."""
    m = Model(TRANSLUCENT, CAPS)
    x, z = PIRNS[TENDENCIES.index(tendency)]
    m.octo('cop_' + tendency, 'y', (x, 0, z), PIRN_RADIUS, PIRN_FLOOR, PIRN_FLOOR + height, tendency,
           faces=('north', 'south', 'east', 'west', 'up'))
    return m


def vial_liquid(tendency, height=1.0):
    """Enzyme liquid in one vial, one pixel deep; the renderer stretches it by stored stock."""
    m = Model(TRANSLUCENT, CAPS)
    x, z = PIRNS[TENDENCIES.index(tendency)]
    m.box('vial_' + tendency, (x - VIAL_RADIUS, VIAL_FLOOR, z - VIAL_RADIUS), (x + VIAL_RADIUS, VIAL_FLOOR + height, z + VIAL_RADIUS),
          'liq_' + tendency)
    return m


def loom(stage=0):
    m = Model(TRANSLUCENT, CAPS)
    frames(m, stage)
    beams(m)
    cloth(m)
    warp(m, stage)
    reed(m)
    harness(m, stage)
    treadles(m)
    pirns(m, stage)
    return m


def staged_blockstate():
    """Facing x stage multipart: each stage's cutout body, and the shared vial glass at every stage."""
    parts = []
    for facing, angle in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for stage, suffix in enumerate(STAGES):
            apply = {'model': 'hemomancy:block/' + NAME + suffix}
            if angle:
                apply['y'] = angle
            parts.append({'apply': apply, 'when': {'facing': facing, 'stage': str(stage)}})
        glass = {'model': 'hemomancy:block/' + NAME + '_glass'}
        if angle:
            glass['y'] = angle
        parts.append({'apply': glass, 'when': {'facing': facing}})
    return {'multipart': parts}


GEOMETRY = kit.ROOT / 'src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomGeometry.java'


def fmt(v):
    return ('%.4f' % v).rstrip('0').rstrip('.') + 'f' if v % 1 else '%.1ff' % v


def floats(values):
    return '{' + ', '.join(fmt(v) for v in values) + '}'


def geometry_java():
    indent = ',\n' + ' ' * 12
    veins = indent.join(floats(a + b) for a, b in UPRIGHT_VEINS)
    pirns = indent.join(floats(p) for p in PIRNS)
    radius, foot, top = GANGLION
    x0, x1, cy, cz = CATALYST_ROW
    stages = [floats(column) for column in zip(*GANGLION_STAGES)]
    feelers = indent.join(floats((x, bottom + .2, FEELER_Z - 1.0)) for x, bottom in FEELERS)
    return f"""package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

/** Generated by tools/model_export/somatic_loom.py: pixels of the north-facing loom the ritual effects follow. */
public final class SomaticLoomGeometry {{
    /** Warp thread centres across x; even threads ride the raised shed. */
    public static final float[] WARP_X = {floats(WARP)};
    public static final float THREAD_HALF = .15f;
    /** Top of the raised and lowered sheds; threads run from the fell back to the back rest. */
    public static final float UPPER_TOP = {fmt(UPPER[1])}, LOWER_TOP = {fmt(LOWER[1])};
    public static final float FELL_Z = {fmt(FELL_Z)}, WARP_END_Z = {fmt(BACK_REST[1])};
    public static final float FELL_Y = {fmt((CLOTH_Y[0] + CLOTH_Y[1]) / 2)};
    /** Woven tapestry top surface, from the breast beam back to the fell. */
    public static final float CLOTH_TOP = {fmt(CLOTH_Y[1])}, CLOTH_X0 = .5f, CLOTH_X1 = 15.5f, CLOTH_FRONT_Z = {fmt(BREAST[1])};
    /** Upright veins {{x0, y0, z0, x1, y1, z1}}, then the crossbar vein to the ganglion. */
    public static final float[][] UPRIGHT_VEINS = {{
            {veins}
    }};
    public static final float[] CROSSBAR_VEIN = {floats(CROSSBAR_VEIN[0] + CROSSBAR_VEIN[1])};
    public static final float GANGLION_X = 8.0f, GANGLION_Z = {fmt(HARNESS_Z)};
    public static final float GANGLION_RADIUS = {fmt(radius)}, GANGLION_FOOT = {fmt(foot)}, GANGLION_TOP = {fmt(top)};
    /** The ganglion by upgrade stage (base, Ganglionic, Cortical): radius, foot and top. */
    public static final float[] GANGLION_RADII = {stages[0]}, GANGLION_FEET = {stages[1]}, GANGLION_TOPS = {stages[2]};
    /** Ganglionic feeler tips {{x, y, z}}, hooked toward the echo. */
    public static final float[][] FEELER_TIPS = {{
            {feelers}
    }};
    /** Pirn (x, z) in EnumBloodTendency order, wound from PIRN_FLOOR up to PIRN_HEIGHT pixels of thread. */
    public static final float[][] PIRNS = {{
            {pirns}
    }};
    public static final float PIRN_FLOOR = {fmt(PIRN_FLOOR)}, PIRN_HEIGHT = {fmt(PIRN_HEIGHT)};
    public static final float MEMORY_Y = {fmt(MEMORY_Y)}, PREVIEW_Y = {fmt(PREVIEW_Y)};
    /** Catalysts lie in a row on the fresh cloth in front of the fell. */
    public static final float CATALYST_X0 = {fmt(x0)}, CATALYST_X1 = {fmt(x1)}, CATALYST_Y = {fmt(cy)}, CATALYST_Z = {fmt(cz)};
    /** Enzyme vials share the pirn (x, z); liquid stands from VIAL_FLOOR up to VIAL_DEPTH pixels at full stock. */
    public static final float VIAL_FLOOR = {fmt(VIAL_FLOOR)}, VIAL_DEPTH = {fmt(VIAL_DEPTH)};
    /** Where the echo's feet stand on the warp. */
    public static final float ECHO_X = {fmt(ECHO[0])}, ECHO_Y = {fmt(ECHO[1])}, ECHO_Z = {fmt(ECHO[2])};

    private SomaticLoomGeometry() {{
    }}
}}
"""


def export():
    bodies = [loom(stage) for stage in range(len(STAGES))]
    body = bodies[0]
    cops = [cop(t) for t in TENDENCIES]
    vials = [vial_liquid(t) for t in TENDENCIES]
    kit.separate(bodies, TEXTURES, TRANSLUCENT, 'Somatic Loom')
    # Body and vial glass bake into separate layers (see station_kit.split_glass); the item keeps both.
    # Every stage shares the base glass: upgrades grow nerve and cortex, never new glass.
    solid, glass = kit.split_glass(body)
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'log', DISPLAY))
    kit.write_json(kit.MODELS / (NAME + '_glass.json'), kit.to_json(glass, TEXTURES, 'log'))
    for suffix, staged in zip(STAGES[1:], bodies[1:]):
        staged_solid, staged_glass = kit.split_glass(staged)
        assert len(staged_glass.elements) == len(glass.elements), suffix + ' grew glass'
        kit.write_json(kit.MODELS / (NAME + suffix + '.json'), kit.to_json(staged_solid, TEXTURES, 'log'))
    for tendency, part in zip(TENDENCIES, cops):
        kit.write_json(kit.MODELS / (NAME + '_cop_' + tendency + '.json'), kit.to_json(part, TEXTURES, tendency))
    for tendency, part in zip(TENDENCIES, vials):
        kit.write_json(kit.MODELS / (NAME + '_vial_' + tendency + '.json'),
                       kit.to_json(part, TEXTURES, 'liq_' + tendency))
    # The inventory loom shows every pirn wound and every vial full.
    item = Model(TRANSLUCENT, CAPS)
    item.elements = body.elements + [e for t in TENDENCIES for e in cop(t, PIRN_HEIGHT).elements] \
        + [e for t in TENDENCIES for e in vial_liquid(t, VIAL_DEPTH).elements]
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(item, TEXTURES, 'log', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), staged_blockstate(), indent=2)
    kit.BBMODEL.mkdir(parents=True, exist_ok=True)
    groups = [('loom' + suffix, staged) for suffix, staged in zip(STAGES, bodies)] + [('cop_' + t, part) for t, part in zip(TENDENCIES, cops)] \
        + [('vial_' + t, part) for t, part in zip(TENDENCIES, vials)]
    project = kit.bbmodel(NAME, groups, TEXTURES, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    GEOMETRY.write_text(geometry_java(), encoding='utf-8')
    stale = kit.ASSETS / 'textures/block/model_somatic_loom.png'
    if stale.exists():
        stale.unlink()
    return body


def preview(stage=0):
    parts = [(loom(stage), kit.identity)] + [(cop(t, PIRN_HEIGHT * (k + 1) / 8), kit.identity) for k, t in enumerate(TENDENCIES)] \
        + [(vial_liquid(t, VIAL_DEPTH * (8 - k) / 8), kit.identity) for k, t in enumerate(TENDENCIES)]
    panels = [('Weaver side', 200, 20, parts), ('Three quarter', 235, 30, parts),
              ('Behind', 30, 22, parts), ('Side', 270, 12, parts), ('Above', 210, 62, parts),
              ('Pirns', 120, 20, parts)]
    return kit.preview(panels, 3, kit.ROOT / ('build/somatic_loom%s_preview.png' % STAGES[stage]), (560, 520), 11, (8, 12, 8), TEXTURES)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Somatic Loom: %d elements' % len(model.elements))
    if args.preview:
        for stage in range(len(STAGES)):
            print(preview(stage))

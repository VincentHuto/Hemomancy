"""Author the Consecrated Bloodwell: the Founding Fane's heart, a blood font raised on a flesh heart.

Arterial roots spread from under a stepped venous-stone plinth, binding the well into the fane's
ground. On the plinth, four bone ribs bow out round a flesh heart and splay again to carry the font.
The heart's great vessels rise into the basin. The basin is a wide strapped iron octagon with a venous
frieze and a bold rim. A bone-ringed ostium sits at its centre, where ConsecratedBloodwellRenderer
raises the fountain jet, and a crust tide line marks the brim. Four fanged spouts cut the rim on the
cardinal flats and spill the overflow into catch cups on the ground beside the plinth.

The block has no facing, so the model is symmetric under quarter turns and the blockstate has one
variant. Renderer geometry that must line up with this model (pool surface, spout lips, catch cups)
is listed in ConsecratedBloodwellRenderer next to its constants.

Emits the block model, the item model (carrying the body itself), the blockstate and an editable
Blockbench project. `--preview` writes build/consecrated_bloodwell_preview.png.
Python 3 + Pillow + NumPy.
"""
import argparse
import json

import station_kit as kit
from station_kit import Model

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'brick': 'hemomancy:block/polished_venous_stone_bricks',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'polished': 'hemomancy:block/polished_venous_stone',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'flesh': 'hemomancy:block/bog_flesh',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'scab': 'hemomancy:block/blood_scorched_scab',
}
CAPS = {'bone': 'bonecap', 'pillar': 'pillartop', 'brick': 'polished'}
NAME = 'consecrated_bloodwell'

CENTRE = (8.0, 8.0)
C3 = (CENTRE[0], 0, CENTRE[1])
SPOUTS = (0, 90, 180, 270)            # cardinal flats: overflow pours here
DIAGONALS = (45, 135, 225, 315)
EIGHTHS = (22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5)

PLINTH_TOP, STEP_TOP = 1.4, 2.3
BASIN_FLOOR = 11.6                    # dry floor; the renderer's pool rises from just above it
WALL_RADIUS, WALL_THICK, WALL_TOP = 6.4, 1.0, 13.6
RIM_RADIUS, RIM_THICK, RIM_TOP = 6.85, 1.5, 14.4
SPOUT_FLOOR = WALL_TOP                # spout channels cut the rim to the wall top, where a full pool brims
NOTCH = .6                            # half-width of each spout's cut through the rim
SPOUT_TIP = 7.6                       # the renderer's spill falls just past this lip into the cup below

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [.625, .625, .625]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [.5, .5, .5]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 10, 0], 'scale': [.8, .8, .8]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
}


def plinth(m):
    """Stepped venous-stone plinth in an iron band, arterial roots creeping out from beneath it."""
    for deg in DIAGONALS:
        m.radial('root', CENTRE, deg, 5.6, 8.6, -.6, .6, 0, .75, 'vein')
        m.radial('root_tip', CENTRE, deg, 8.6, 10.6, -.38, .38, 0, .45, 'vein')
    for deg in EIGHTHS:
        m.radial('rootlet', CENTRE, deg, 6.2, 7.6, -.32, .32, 0, .4, 'vein')
    m.octo('plinth', 'y', C3, 6.6, 0, PLINTH_TOP, 'brick')
    m.shell('plinth_band', CENTRE, 6.85, .35, 1.05, .3, 'iron', ends=True)
    m.octo('plinth_step', 'y', C3, 5.2, PLINTH_TOP, STEP_TOP, 'venous')
    m.octo('heart_dais', 'y', C3, 3.7, STEP_TOP, 2.9, 'bold')


def heart(m):
    """A flesh heart on the dais, coronary veins across it and its great vessels rising to the font."""
    m.octo('heart', 'y', C3, 2.9, 2.9, 7.4, 'flesh')
    m.octo('ventricle', 'y', (7.4, 0, 8.7), 2.3, 3.3, 6.4, 'flesh')
    m.octo('ventricle', 'y', (8.8, 0, 7.4), 2.1, 3.6, 6.8, 'flesh')
    m.octo('heart_apex', 'y', (7.6, 0, 8.4), 1.6, 2.6, 3.3, 'flesh')
    m.octo('atrium', 'y', (6.9, 0, 7.3), 1.5, 7.0, 8.4, 'flesh')
    m.octo('atrium', 'y', (9.1, 0, 8.9), 1.4, 7.0, 8.2, 'flesh')
    # Coronary veins on each open cardinal face, where the ribs leave the heart in view.
    for deg, y0, y1 in ((90, 3.4, 7.2), (0, 3.0, 6.6), (180, 4.2, 7.3), (270, 3.6, 6.9)):
        m.radial('coronary', CENTRE, deg, 2.85, 3.2, -.3, .3, y0, y1, 'vein')
        m.radial('coronary_branch', CENTRE, deg, 2.85, 3.15, -1.5, 1.5, y0 + 2.0, y0 + 2.3, 'vein')
    m.octo('aorta', 'y', (8.5, 0, 7.6), .95, 7.6, 10.4, 'vein')
    m.octo('aortic_arch', 'y', (8.5, 0, 7.6), 1.25, 9.0, 9.5, 'scab')
    m.octo('vena_cava', 'y', (7.1, 0, 8.7), .7, 8.0, 10.4, 'vein')
    m.octo('pulmonary', 'y', (7.3, 0, 7.0), .55, 8.2, 10.4, 'vein')


RIB_SEGMENTS = ((4.0, 4.8, 3.0, 3.8), (4.6, 5.4, 3.8, 4.8), (5.0, 5.8, 4.8, 6.0), (4.6, 5.4, 6.0, 7.0),
                (4.0, 4.8, 7.0, 7.9), (3.7, 4.5, 7.9, 8.8), (4.1, 4.9, 8.8, 9.6), (4.8, 5.8, 9.6, 10.3))


def ribs(m):
    """Four bone ribs socketed in iron on the diagonals, bowing out round the heart, splaying under the font."""
    for deg in DIAGONALS:
        m.radial('rib_socket', CENTRE, deg, 3.8, 5.3, -.8, .8, STEP_TOP, 3.1, 'chisel')
        for r0, r1, y0, y1 in RIB_SEGMENTS:
            m.radial('rib', CENTRE, deg, r0, r1, -.6, .6, y0, y1, 'bone')
        m.radial('rib_tubercle', CENTRE, deg, 5.8, 6.2, -.35, .35, 5.0, 5.8, 'bonecap')
        for side in (-1, 1):
            m.radial('costal_spur', CENTRE, deg, 4.3, 4.9, side * .6, side * 1.3, 4.5, 4.95, 'bone')
            m.radial('costal_spur', CENTRE, deg, 4.1, 4.7, side * .6, side * 1.2, 6.9, 7.3, 'bone')


def notched_shell(m, name, radius, y0, y1, thick, mat, notch):
    """Upright octagon like Model.shell, its four cardinal panes split by a gap of 2 * notch."""
    cx, cz = CENTRE
    half = radius * kit.TAN
    sides = ('north', 'south', 'east', 'west', 'up', 'down')
    rot = ('y', 45, [cx, y0, cz])
    for s in (-1, 1):
        lo, hi = sorted((s * radius, s * (radius - thick)))
        for t0, t1 in ((-half, -notch), (notch, half)):
            m.box(name, (cx + lo, y0, cz + t0), (cx + hi, y1, cz + t1), mat, faces=sides)
            m.box(name, (cx + t0, y0, cz + lo), (cx + t1, y1, cz + hi), mat, faces=sides)
        m.box(name, (cx + lo, y0, cz - half), (cx + hi, y1, cz + half), mat, rot, faces=sides)
        m.box(name, (cx - half, y0, cz + lo), (cx + half, y1, cz + hi), mat, rot, faces=sides)


def basin(m):
    """Wide iron font: strapped wall with a venous frieze, dried-blood floor, bone-ringed ostium, tide line."""
    m.octo('basin_heel', 'y', C3, 4.4, 9.4, 10.2, 'chisel')
    m.octo('basin_base', 'y', C3, 5.8, 10.2, 11.0, 'bold')
    m.shell('basin_wall', CENTRE, WALL_RADIUS, 11.0, WALL_TOP, WALL_THICK, 'iron', ends=True)
    notched_shell(m, 'basin_rim', RIM_RADIUS, WALL_TOP, RIM_TOP, RIM_THICK, 'bold', NOTCH)
    m.shell('basin_frieze', CENTRE, WALL_RADIUS + .12, 11.7, 12.35, .2, 'venous', ends=True)
    for deg in DIAGONALS:
        m.radial('basin_strap', CENTRE, deg, WALL_RADIUS - .2, WALL_RADIUS + .45, -.5, .5, 10.4, WALL_TOP, 'pillar')
        m.radial('strap_boss', CENTRE, deg, WALL_RADIUS + .4, WALL_RADIUS + .75, -.35, .35, 11.75, 12.3, 'bonecap')
    for deg in EIGHTHS:
        # Blood weeps from the brim down the outer wall between straps and spouts.
        for t0, t1, y0 in ((-.35, .15, 11.2), (.45, .8, 12.5)):
            m.radial('wall_drip', CENTRE, deg, WALL_RADIUS - .05, WALL_RADIUS + .3, t0, t1, y0, WALL_TOP, 'vein')
    m.octo('basin_floor', 'y', C3, WALL_RADIUS - WALL_THICK + .05, 11.0, BASIN_FLOOR, 'scab')
    m.octo('floor_inlay', 'y', C3, 3.4, BASIN_FLOOR, BASIN_FLOOR + .15, 'venous',
           faces=('up', 'north', 'south', 'east', 'west'))
    m.octo('ostium_ring', 'y', C3, 1.8, BASIN_FLOOR + .15, BASIN_FLOOR + .5, 'bone')
    m.octo('ostium', 'y', C3, 1.15, BASIN_FLOOR + .15, 12.35, 'pillar')
    m.octo('ostium_mouth', 'y', C3, .6, 12.35, 12.42, 'vein', faces=('up',))
    m.shell('tide_line', CENTRE, WALL_RADIUS - WALL_THICK + .02, 13.0, 13.3, .1, 'vein')


def spouts(m):
    """Fanged iron spouts through the rim notches; each pours into a blood-filled catch cup below."""
    for deg in SPOUTS:
        m.radial('spout_bracket', CENTRE, deg, 6.3, 7.4, -.55, .55, 11.9, SPOUT_FLOOR - .5, 'chisel')
        m.radial('spout_floor', CENTRE, deg, 6.2, SPOUT_TIP, -1.05, 1.05, SPOUT_FLOOR - .5, SPOUT_FLOOR - .02, 'bold')
        for t0, t1 in ((-1.05, -NOTCH), (NOTCH, 1.05)):
            m.radial('spout_rail', CENTRE, deg, RIM_RADIUS - .1, SPOUT_TIP, t0, t1, SPOUT_FLOOR - .02, 14.15, 'iron')
        m.radial('spout_channel', CENTRE, deg, WALL_RADIUS - WALL_THICK, SPOUT_TIP, -NOTCH, NOTCH, SPOUT_FLOOR,
                 SPOUT_FLOOR + .06, 'vein', faces=('up',))
        for t0, t1 in ((-.95, -.5), (.5, .95)):
            m.radial('spout_fang', CENTRE, deg, SPOUT_TIP - .55, SPOUT_TIP - .05, t0, t1, 12.2, SPOUT_FLOOR - .5, 'bone')
            m.radial('spout_fang_tip', CENTRE, deg, SPOUT_TIP - .45, SPOUT_TIP - .15, t0 * .8 + t1 * .2, t0 * .2 + t1 * .8, 11.7, 12.2,
                     'bonecap')
        m.radial('cup', CENTRE, deg, 6.5, 7.98, -1.15, 1.15, 0, 1.3, 'bold')
        m.radial('cup_blood', CENTRE, deg, 6.7, 7.75, -.9, .9, 1.3, 1.36, 'scab', faces=('up',))
        for t0, t1 in ((-1.15, -.9), (.9, 1.15)):
            m.radial('cup_lip', CENTRE, deg, 6.5, 7.98, t0, t1, 1.3, 1.7, 'iron')
        m.radial('cup_lip', CENTRE, deg, 7.75, 7.98, -.9, .9, 1.3, 1.7, 'iron')


def body():
    m = Model(caps=CAPS)
    plinth(m)
    heart(m)
    ribs(m)
    basin(m)
    spouts(m)
    return m


def blockstate():
    return {'variants': {'': {'model': 'hemomancy:block/' + NAME}}}


def export():
    solid = body()
    kit.separate([solid], TEXTURES, set(), 'Consecrated Bloodwell')
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'iron', DISPLAY))
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'iron', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), blockstate(), indent=2)
    project = kit.bbmodel(NAME, [('bloodwell', solid)], TEXTURES, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    return solid


def preview():
    solid = [(body(), kit.identity)]
    panels = [('Front', 200, 18, solid), ('Three-quarter', 235, 26, solid),
              ('Low', 20, 6, solid), ('Above', 210, 62, solid)]
    return kit.preview(panels, 4, kit.ROOT / 'build/consecrated_bloodwell_preview.png', (400, 460), 19, (8, 7.5, 8),
                       TEXTURES)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Consecrated Bloodwell: %d elements' % len(model.elements))
    if args.preview:
        print(preview())

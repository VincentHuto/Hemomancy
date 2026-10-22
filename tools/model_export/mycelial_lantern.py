"""Author the Mycelial Lantern: a blood-fed glass cloche over a fruiting mycelial bed.

An iron-hooped blood-wood crock on cast feet holds the Lantern's blood behind a front sight glass.
Its rim cradles an erythrocytic mycelium bed where a ring of fruiting bodies glows from the gills,
and a flesh wick lifts the spore culture into the bell. The stepped glass cloche stands in a
hematic iron lantern cage, crowned by a vent and a carry bail.

Emits the cutout body and translucent glass block models (one facing multipart applies both), the
item model carrying both, the blood gauge part MycelialLanternRenderer scales by fill, and an
editable Blockbench project. `--preview` writes build/mycelial_lantern_preview.png.

Model space is the north-facing main block: the gauge faces the player at z = 0, and the bell rises
into the filler block above. Python 3 + Pillow + NumPy.
"""
import argparse
import json

import numpy as np

import station_kit as kit
from station_kit import BRIGHT, Model, polar

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'plank': 'hemomancy:block/blood_wood_planks',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'glass': 'hemomancy:block/ghastly_alembic_glass',
    'blood': 'hemomancy:block/blood_still',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'flesh': 'hemomancy:block/bog_flesh',
    'hyphae': 'hemomancy:block/calcified_hyphae',
    'myc': 'hemomancy:block/erythrocytic_mycelium_side',
    'myctop': 'hemomancy:block/fruiting_erythrocytic_mycelium_top',
    'bed': 'hemomancy:block/mycelium_erythrocytic_dirt_side',
    'bedtop': 'hemomancy:block/mycelium_erythrocytic_dirt_top',
}
TRANSLUCENT = {'glass'}
CAPS = {'bone': 'bonecap', 'myc': 'myctop', 'bed': 'bedtop'}
NAME = 'mycelial_lantern'

# Shared with MycelialLanternRenderer (model pixels).
CENTRE = (8.0, 8.0)
CROCK_RADIUS, CROCK_TOP = 6.0, 6.0
BED_Y = 6.75                         # bed surface; the bell stands on it inside the rim
BELL_RADIUS, BELL_WALL = 5.0, 20.0
GAUGE = (7.3, 8.7, 1.75, 1.95)       # blood column x0, x1, z0, z1 behind the sight glass
GAUGE_FLOOR, GAUGE_DEPTH = 1.95, 3.2 # one pixel tall at the floor, stretched to the stored level
ITEM_Y = 13.25                       # the culture turns above the wick's cup

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -2, 0], 'scale': [.5, .5, .5]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, -2, 0], 'scale': [.5, .5, .5]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 9, 0], 'scale': [.8, .8, .8]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1], 'scale': [.3, .3, .3]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1], 'scale': [.3, .3, .3]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -1, 0], 'scale': [.32, .32, .32]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -1, 0], 'scale': [.32, .32, .32]},
}


def crock(m):
    """Blood-wood crock on cast feet, iron-hooped, with the blood sight glass on its front facet."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    for k in (1, 3, 5, 7):
        m.radial('cast_foot', CENTRE, k * 45, 4.6, 7.0, -.8, .8, 0, 1.25, 'bold')
        m.radial('foot_scroll', CENTRE, k * 45, 6.4, 7.25, -.55, .55, 1.25, 2.0, 'iron')
    m.octo('crock', 'y', c3, CROCK_RADIUS, 1, CROCK_TOP, 'plank')
    for y in (1.4, 5.0):
        m.shell('crock_hoop', CENTRE, CROCK_RADIUS + .25, y, y + .55, .35, 'iron', ends=True)
    m.shell('rim', CENTRE, 6.5, CROCK_TOP, 7.25, 1.25, 'chisel', ends=True)
    # Sight glass: an iron frame round a slot where the stored blood stands.
    x0, x1, z0, z1 = GAUGE
    top = GAUGE_FLOOR + GAUGE_DEPTH
    m.box('gauge_lintel', (x0 - .55, top, 1.45), (x1 + .55, top + .45, 2.05), 'iron')
    m.box('gauge_sill', (x0 - .55, GAUGE_FLOOR - .45, 1.45), (x1 + .55, GAUGE_FLOOR, 2.05), 'iron')
    for a, b in ((x0 - .55, x0), (x1, x1 + .55)):
        m.box('gauge_jamb', (a, GAUGE_FLOOR, 1.45), (b, top, 2.05), 'iron')
    m.box('gauge_backing', (x0, GAUGE_FLOOR, z1), (x1, top, 2.0), 'vein', faces=('north',))
    m.box('gauge_glass', (x0, GAUGE_FLOOR, 1.5), (x1, top, 1.6), 'glass', faces=('north', 'south'))
    for n in range(3):
        y = GAUGE_FLOOR + (n + 1) * GAUGE_DEPTH / 4
        m.box('gauge_tick', (x1 + .55, y - .1, 1.55), (x1 + .85, y + .1, 1.95), 'bone')


def bed(m):
    """The mycelium bed in the rim, a flesh wick at its heart and fruiting bodies glowing round it."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('bed', 'y', c3, 5.3, CROCK_TOP, BED_Y, 'bed')
    m.octo('wick', 'y', c3, .85, BED_Y, 9.6, 'flesh')
    m.octo('wick_vessel', 'y', (8.55, 0, 7.8), .22, BED_Y, 9.4, 'vein')
    m.octo('wick_cup', 'y', c3, 1.5, 9.6, 10.1, 'bone')
    m.octo('wick_cup_lip', 'y', c3, 1.75, 10.1, 10.35, 'bone', faces=('north', 'south', 'east', 'west', 'up'))
    for deg, dist, height, cap in ((25, 3.4, 4.6, 1.4), (95, 3.7, 2.4, 1.0), (150, 3.1, 5.4, 1.6),
                                   (215, 3.8, 3.0, 1.1), (280, 3.3, 4.0, 1.3), (335, 3.9, 1.8, .8)):
        x, z = polar(CENTRE, deg, dist)
        top = BED_Y + height
        m.box('stipe', (x - .28, BED_Y, z - .28), (x + .28, top, z + .28), 'hyphae')
        m.octo('fruiting_cap', 'y', (x, 0, z), cap, top, top + .65, 'myc', faces=('north', 'south', 'east', 'west', 'up'))
        m.octo('fruiting_umbo', 'y', (x, 0, z), cap * .45, top + .65, top + .95, 'myctop')
        m.octo('glowing_gills', 'y', (x, 0, z), cap * 1.02, top - .3, top + .05, 'vein', faces=('down', 'north', 'south', 'east', 'west'),
               extra=BRIGHT)
    for deg in (60, 185, 305):
        x, z = polar(CENTRE, deg, 4.45)
        m.box('hypha_climb', (x - .14, BED_Y, z - .14), (x + .14, BED_Y + 5.5, z + .14), 'hyphae')


def cloche(m):
    """Stepped glass bell in a hematic iron lantern cage, vented and carried by a bail."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.shell('bell_wall', CENTRE, BELL_RADIUS, BED_Y, BELL_WALL, .25, 'glass')
    m.shell('bell_shoulder', CENTRE, BELL_RADIUS, BELL_WALL, BELL_WALL + .3, 1.0, 'glass', ends=True)
    m.shell('bell_neck', CENTRE, 4.0, BELL_WALL + .3, 21.6, .25, 'glass')
    m.shell('bell_crown', CENTRE, 4.0, 21.6, 21.9, 1.25, 'glass', ends=True)
    m.shell('bell_finial_glass', CENTRE, 2.75, 21.9, 22.8, .25, 'glass')
    # Cage: diagonal ribs, a collar under the shoulder, and a bone-banded vent cap.
    for k in (1, 3, 5, 7):
        m.radial('cage_rib', CENTRE, k * 45, BELL_RADIUS - .05, BELL_RADIUS + .4, -.3, .3, 7.25, BELL_WALL + .3, 'iron')
        m.radial('cage_shoulder', CENTRE, k * 45, 3.95, BELL_RADIUS + .4, -.3, .3, BELL_WALL + .3, BELL_WALL + .75, 'iron')
        m.radial('cage_neck', CENTRE, k * 45, 3.95, 4.35, -.3, .3, BELL_WALL + .75, 21.95, 'iron')
    m.shell('cage_collar', CENTRE, BELL_RADIUS + .35, 13.0, 13.6, .3, 'iron', ends=True)
    m.octo('vent_cap', 'y', c3, 3.0, 22.8, 23.5, 'chisel')
    m.octo('vent_band', 'y', c3, 2.1, 23.5, 24.1, 'bone')
    m.octo('vent', 'y', c3, 1.3, 24.1, 25.0, 'iron')
    m.octo('vent_mouth', 'y', c3, .7, 25.0, 25.15, 'vein', faces=('up',), extra=BRIGHT)
    for x0 in (5.4, 10.0):
        m.box('bail_post', (x0, 23.5, 7.7), (x0 + .6, 27.2, 8.3), 'iron')
    m.box('bail_bar', (5.4, 27.2, 7.7), (10.6, 27.8, 8.3), 'iron')
    m.box('bail_grip', (6.9, 27.05, 7.55), (9.1, 27.95, 8.45), 'bone')


def lantern():
    m = Model(TRANSLUCENT, CAPS)
    crock(m)
    bed(m)
    cloche(m)
    return m


def gauge_blood():
    """Unit-height blood in the sight glass; the renderer scales it by stored blood."""
    m = Model(TRANSLUCENT, CAPS)
    x0, x1, z0, z1 = GAUGE
    m.box('gauge_blood', (x0, GAUGE_FLOOR, z0), (x1, GAUGE_FLOOR + 1, z1), 'blood', faces=('north', 'up'))
    return m


def export():
    body = lantern()
    blood = gauge_blood()
    kit.separate([body], TEXTURES, TRANSLUCENT, 'Mycelial Lantern')
    # Body and glass bake into separate layers (see station_kit.split_glass); the item keeps both.
    solid, glass = kit.split_glass(body)
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'plank', DISPLAY))
    kit.write_json(kit.MODELS / (NAME + '_glass.json'), kit.to_json(glass, TEXTURES, 'plank'))
    kit.write_json(kit.MODELS / (NAME + '_blood.json'), kit.to_json(blood, TEXTURES, 'blood'))
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(body, TEXTURES, 'plank', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), kit.two_layer_blockstate(NAME), indent=2)
    kit.BBMODEL.mkdir(parents=True, exist_ok=True)
    project = kit.bbmodel(NAME, [('lantern', body), ('gauge_blood', blood)], TEXTURES, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    stale = kit.ASSETS / 'textures/block/model_mycelial_lantern.png'
    if stale.exists():
        stale.unlink()
    return body


def preview():
    body = lantern()
    floor = np.array([0, GAUGE_FLOOR, 0])
    fill = lambda p, n: ((p - floor) * np.array([1, GAUGE_DEPTH * .6, 1]) + floor, n)
    parts = [(body, kit.identity), (gauge_blood(), fill)]
    panels = [('Operator side', 200, 18, parts), ('Three quarter', 235, 30, parts),
              ('Behind', 20, 22, parts), ('Above', 210, 60, parts)]
    return kit.preview(panels, 4, kit.ROOT / 'build/mycelial_lantern_preview.png', (400, 500), 15, (8, 13, 8), TEXTURES)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Mycelial Lantern: %d elements' % len(model.elements))
    if args.preview:
        print(preview())

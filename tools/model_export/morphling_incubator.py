"""Author the Morphling Incubator: a glass brood tank on a warmed venous-stone plinth.

Bone claw feet carry an octagonal plinth with a glowing brood hearth behind an iron grate. The tank
above it is strapped hematic iron and glass; under its lid a placenta of grafted flesh lowers an
umbilical cord toward whatever floats in the blood below. Four enzyme ampoules stand in cups on the
plinth's diagonals, and a blood ampoule on the east bracket feeds the lid.

Emits the block model (the item parents it), an editable Blockbench project, and removes the
retired entity-model assets. MorphlingIncubatorRenderer draws the blood (an octagon of FLUID_* below)
and the incubated item at ITEM_Y inside the tank. `--preview` writes build/morphling_incubator_preview.png.

Model space is the north-facing block: the hearth faces the player at z = 0. Python 3 + Pillow + NumPy.
"""
import argparse
import json
import math

import station_kit as kit
from station_kit import BRIGHT, Model, polar

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'brick': 'hemomancy:block/polished_venous_stone_bricks',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'glass': 'hemomancy:block/ghastly_alembic_glass',
    'blood': 'hemomancy:block/blood_still',
    'flesh': 'hemomancy:block/bog_flesh',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'amber': 'hemomancy:block/mnemonic_ambergris_block',
    'hyphae': 'hemomancy:block/calcified_hyphae',
    'sanguine': 'hemomancy:block/sanguine_glass',
}
TRANSLUCENT = {'glass', 'sanguine'}
CAPS = {'bone': 'bonecap'}
NAME = 'morphling_incubator'

# Shared with MorphlingIncubatorRenderer (model pixels).
CENTRE = (8.0, 8.0)
TANK_RADIUS, TANK_BOTTOM, TANK_TOP = 4.75, 5.5, 19.0
FLUID_RADIUS, FLUID_BOTTOM, FLUID_TOP = 4.4, 5.55, 18.4
ITEM_Y = 11.0                       # centre of the incubated item; its top clears the cord's bulb
CORD_TIP = 14.6

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -1.5, 0], 'scale': [.5, .5, .5]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, -1.5, 0], 'scale': [.5, .5, .5]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 9, 0], 'scale': [.8, .8, .8]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1], 'scale': [.3, .3, .3]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1], 'scale': [.3, .3, .3]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -1, 0], 'scale': [.32, .32, .32]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -1, 0], 'scale': [.32, .32, .32]},
}


def plinth(m):
    """Venous-stone plinth on four bone claw feet, its front set with the brood hearth."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    for k in (1, 3, 5, 7):
        m.radial('claw_foot', CENTRE, k * 45, 4.75, 7.1, -.9, .9, 0, 1.25, 'bone')
        m.radial('claw_knuckle', CENTRE, k * 45, 6.1, 7.1, -1.05, 1.05, .3, 1.5, 'bone')
        for side in (-.55, .55):
            m.radial('claw_talon', CENTRE, k * 45, 7.1, 7.75, side - .25, side + .25, 0, .6, 'bone')
    m.octo('plinth', 'y', c3, 7, 1.4, 3, 'iron')
    m.octo('plinth_cap', 'y', c3, 7.25, 3, 3.75, 'chisel')
    m.octo('plinth_bead', 'y', c3, 7.15, 1, 1.4, 'venous')
    # Brood hearth: coals glowing behind a hematic iron grate on the front facet (z = 1).
    m.box('hearth_lintel', (4.5, 2.6, .4), (11.5, 3.05, 1.05), 'bold')
    m.box('hearth_sill', (4.5, 1.15, .4), (11.5, 1.55, 1.05), 'bold')
    for x0 in (4.5, 10.8):
        m.box('hearth_jamb', (x0, 1.55, .4), (x0 + .7, 2.6, 1.05), 'bold')
    m.box('hearth_coals', (5.2, 1.55, .85), (10.8, 2.6, 1.0), 'vein', faces=('north', 'up', 'down', 'east', 'west'),
          extra=BRIGHT)
    for x in (6.2, 7.6, 9.0):
        m.box('hearth_bar', (x, 1.55, .55), (x + .4, 2.6, .75), 'iron')


def tank(m):
    """Strapped glass tank between two collars, capped by a domed lid with a bone vent."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('tank_foot', 'y', c3, 5.25, 3.75, TANK_BOTTOM, 'iron')
    m.shell('tank_foot_band', CENTRE, 5.45, 4.1, 4.85, .4, 'chisel', ends=True)
    m.shell('tank_glass', CENTRE, TANK_RADIUS, TANK_BOTTOM, TANK_TOP, .25, 'glass')
    for k in (1, 3, 5, 7):
        m.radial('tank_strap', CENTRE, k * 45, TANK_RADIUS - .05, TANK_RADIUS + .4, -.35, .35,
                 TANK_BOTTOM, TANK_TOP, 'iron')
        m.radial('strap_rivet', CENTRE, k * 45, TANK_RADIUS + .4, TANK_RADIUS + .6, -.2, .2, 12, 12.4, 'bone')
        # The brood's warmth has grown vessels up the straps from the plinth.
        reach = {1: 10.5, 3: 8.0, 5: 11.5, 7: 9.0}[k]
        m.radial('strap_vessel', CENTRE, k * 45, TANK_RADIUS + .4, TANK_RADIUS + .55, -.32, -.05, TANK_BOTTOM, reach, 'vein')
        m.radial('strap_vessel', CENTRE, k * 45, TANK_RADIUS + .4, TANK_RADIUS + .55, .05, .3, TANK_BOTTOM, reach - 2.5, 'vein')
    m.octo('tank_collar', 'y', c3, 5.25, TANK_TOP, 20.5, 'iron')
    m.shell('tank_collar_band', CENTRE, 5.45, 19.35, 20.1, .4, 'chisel', ends=True)
    m.octo('lid', 'y', c3, 4.5, 20.5, 21.25, 'chisel')
    m.octo('lid_dome', 'y', c3, 3.25, 21.25, 22.25, 'iron')
    m.octo('lid_stalk', 'y', c3, .6, 22.25, 24.3, 'venous')
    ribcage(m)


def ribcage(m):
    """Four bone ribs curling in from the collar to a boss over the lid, like a sternum over a cradle.

    Each rib is authored on the north side and turned to its cardinal: a post, a 22.5 degree bend
    and a 45 degree bend, every bend leaning toward the tank axis about the rib's own tangent.
    """
    cx, cz = CENTRE
    for k in range(4):
        # Rotate the authored north rib (d = distance north of the axis) a quarter turn at a time.
        def place(name, d0, d1, y0, y1, pivot=None, lean=0):
            if k == 0:
                a, b = (cx - .3, y0, cz - d1), (cx + .3, y1, cz - d0)
                rot = ('x', lean, [cx, pivot[1], cz - pivot[0]]) if lean else None
            elif k == 1:
                a, b = (cx + d0, y0, cz - .3), (cx + d1, y1, cz + .3)
                rot = ('z', lean, [cx + pivot[0], pivot[1], cz]) if lean else None
            elif k == 2:
                a, b = (cx - .3, y0, cz + d0), (cx + .3, y1, cz + d1)
                rot = ('x', -lean, [cx, pivot[1], cz + pivot[0]]) if lean else None
            else:
                a, b = (cx - d1, y0, cz - .3), (cx - d0, y1, cz + .3)
                rot = ('z', -lean, [cx - pivot[0], pivot[1], cz]) if lean else None
            m.box(name, a, b, 'bone', rot)
        place('rib_post', 4.6, 5.2, 20.5, 21.6)
        place('rib_bend', 4.6, 5.2, 21.5, 23.1, (4.9, 21.5), 22.5)
        x = 4.9 - 1.6 * math.sin(math.radians(22.5))
        y = 21.5 + 1.6 * math.cos(math.radians(22.5))
        place('rib_crest', x - .3, x + .3, y - .1, y + 2.0, (x, y - .1), 45)
    m.octo('sternum_boss', 'y', (cx, 0, cz), 2.9, 24.3, 25.0, 'bone')
    m.octo('sternum_vent', 'y', (cx, 0, cz), .65, 25.0, 26.0, 'venous')


def brood(m):
    """Grafted placenta under the lid and the umbilical cord it lowers into the blood."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('placenta', 'y', c3, 4.2, 18.35, 18.95, 'flesh', faces=('north', 'south', 'east', 'west', 'down'))
    m.octo('placenta_lobe', 'y', (7.25, 0, 8.6), 2.6, 17.75, 18.35, 'flesh', faces=('north', 'south', 'east', 'west', 'down'))
    m.octo('placenta_lobe', 'y', (9.2, 0, 7.1), 1.6, 17.9, 18.35, 'vein', faces=('north', 'south', 'east', 'west', 'down'))
    m.octo('cord', 'y', (7.9, 0, 8.1), .42, 16.2, 17.75, 'flesh')
    m.octo('cord', 'y', (8.1, 0, 7.95), .38, 15.25, 16.2, 'flesh')
    m.octo('cord_vessel', 'y', (8.45, 0, 8.05), .14, 15.6, 17.75, 'vein')
    m.octo('cord_bulb', 'y', (8.05, 0, 8.0), .7, CORD_TIP, 15.25, 'vein')
    # A nest of flesh on the tank floor, bared whenever the blood runs low.
    m.octo('brood_bed', 'y', c3, 3.6, TANK_BOTTOM, 6.0, 'flesh', faces=('north', 'south', 'east', 'west', 'up'))
    m.octo('brood_bed_mound', 'y', (7.4, 0, 8.5), 2.1, 6.0, 6.45, 'flesh', faces=('north', 'south', 'east', 'west', 'up'))


def ampoules(m):
    """The four enzyme catalysts, each stoppered in bone and seated in an iron cup on a diagonal."""
    contents = {1: 'amber', 3: 'hyphae', 5: 'vein', 7: 'sanguine'}
    for k in (1, 3, 5, 7):
        x, z = polar(CENTRE, k * 45, 6.2)
        m.octo('ampoule_cup', 'y', (x, 0, z), .8, 3.75, 4.55, 'iron')
        m.octo('ampoule_' + contents[k], 'y', (x, 0, z), .5, 4.6, 6.1, contents[k])
        m.shell('ampoule_glass', (x, z), .66, 4.55, 6.9, .14, 'glass', ends=True)
        m.octo('ampoule_stopper', 'y', (x, 0, z), .42, 6.9, 7.5, 'bone')


def blood_feed(m):
    """East bracket: an upturned blood ampoule whose drop pipe feeds the tank collar."""
    x, z = 14.65, 8.0
    m.box('feed_bracket', (13.4, 6.6, 7.6), (14.35, 7.2, 8.4), 'iron')
    m.octo('feed_cup', 'y', (x, 0, z), 1.0, 6.2, 6.9, 'iron')
    m.shell('feed_glass', (x, z), .95, 6.9, 11.6, .15, 'glass', ends=True)
    m.octo('feed_blood', 'y', (x, 0, z), .75, 6.95, 11.05, 'blood')
    m.octo('feed_neck', 'y', (x, 0, z), .55, 11.6, 12.4, 'bone')
    m.octo('feed_pipe', 'y', (x, 0, z), .3, 12.4, 19.6, 'iron')
    m.box('feed_elbow', (13.0, 19.45, 7.6), (14.95, 20.25, 8.4), 'iron')
    m.box('feed_cock', (14.4, 15.4, 7.4), (14.9, 15.9, 8.6), 'bone')
    m.box('feed_cock_handle', (14.55, 15.9, 7.9), (14.75, 16.8, 8.1), 'bone')


def incubator():
    m = Model(TRANSLUCENT, CAPS)
    plinth(m)
    tank(m)
    brood(m)
    ampoules(m)
    blood_feed(m)
    return m


def fluid_octagon():
    """The renderer's blood, as a model for previews only (it is drawn as coloured quads in game)."""
    m = Model(TRANSLUCENT | {'fluid'}, CAPS)
    m.octo('blood', 'y', (CENTRE[0], 0, CENTRE[1]), FLUID_RADIUS, FLUID_BOTTOM, FLUID_TOP * .6 + FLUID_BOTTOM * .4, 'fluid')
    return m


def export():
    body = incubator()
    kit.separate([body], TEXTURES, TRANSLUCENT, 'Morphling Incubator')
    # Body and glass bake into separate layers (see station_kit.split_glass); the item keeps both.
    solid, glass = kit.split_glass(body)
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'brick', DISPLAY))
    kit.write_json(kit.MODELS / (NAME + '_glass.json'), kit.to_json(glass, TEXTURES, 'brick'))
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(body, TEXTURES, 'brick', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), kit.two_layer_blockstate(NAME), indent=2)
    kit.BBMODEL.mkdir(parents=True, exist_ok=True)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(
        json.dumps(kit.bbmodel(NAME, [('incubator', body)], TEXTURES, DISPLAY)), encoding='utf-8')
    for stale in ('textures/block/model_morphling_incubator.png', 'textures/entity/morphling_incubator_model.png'):
        path = kit.ASSETS / stale
        if path.exists():
            path.unlink()
    return body


def preview():
    body = incubator()
    fluid = fluid_octagon()
    panels = [('Operator side', 200, 24, [(body, kit.identity), (fluid, kit.identity)]),
              ('East feed', 120, 20, [(body, kit.identity), (fluid, kit.identity)]),
              ('Behind', 20, 28, [(body, kit.identity)]),
              ('Above', 215, 62, [(body, kit.identity)])]
    return kit.preview(panels, 4, kit.ROOT / 'build/morphling_incubator_preview.png', (420, 520), 15,
                       (8, 12, 8), dict(TEXTURES, fluid='hemomancy:block/sanguine_glass'))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Morphling Incubator: %d elements' % len(model.elements))
    if args.preview:
        print(preview())

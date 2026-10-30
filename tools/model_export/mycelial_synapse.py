"""Author the Mycelial Synapse: hematic iron taught to listen, carrying a heart-node's reach.

The iron block it was raised from is drawn out into a fluted spike, collared where it narrows and sheathed
in pale hyphae that spiral up it. It ends in a Sporite Crystal bulb that splits into dendrite tips. A ring
of roots grips its base and runs out across the face it grew from.

Authored pointing up, base on the block's floor. The blockstate turns it to all six FACING values the way
an end rod turns, so the base always grips the attaching face: floor, wall or ceiling. The `withered`
variant (a synapse whose record is gone) has greyed iron, a dull crystal and dead brown roots, from
textures this script derives.

`mycelial_synapse_bulb` is the bulb and its dendrites alone, scaled by MycelialSynapseRenderer about
BULB_CENTRE (mirrored there) and lit when the heart-node's pulse reaches it.

Hematic iron, bone and mycelium only: no copper, brass or bronze. Emits both bodies, the bulb, the item
model, the blockstate, the withered textures and an editable Blockbench project. `--preview` writes
build/mycelial_synapse_preview.png. Python 3 + Pillow + NumPy.
"""
import argparse
import json

import station_kit as kit
from station_kit import Model

NAME = 'mycelial_synapse'
LIVING = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'root': 'hemomancy:block/calcified_hyphae',
    'knot': 'hemomancy:block/erythrocytic_mycelium_top',
    'crystal': 'hemomancy:block/sporite_crystal',
}
CAPS = {'pillar': 'pillartop'}
# (desaturate, darken, tint): iron greys to ash (lifted, since hematic iron is near black), the crystal dulls, roots die brown.
WITHER = {
    'iron': (.92, 1.45, (1.0, 1.0, 1.0)),
    'pillar': (.92, 1.45, (1.0, 1.0, 1.0)),
    'pillartop': (.92, 1.45, (1.0, 1.0, 1.0)),
    'chisel': (.92, 1.45, (1.0, 1.0, 1.0)),
    'root': (.35, .52, (1.0, .78, .55)),
    'knot': (.5, .55, (1.0, .8, .6)),
    'crystal': (.75, .5, (1.0, .95, .85)),
}

CENTRE = (8.0, 8.0)
BULB_CENTRE = (8.0, 11.0, 8.0)
EIGHTHS = (0, 45, 90, 135, 180, 225, 270, 315)
OFFSETS = (22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5)

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [.625, .625, .625]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [.5, .5, .5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
}


def grip(m):
    """A knot of roots clenched on the face, its roots and rootlets running out across it."""
    m.octo('grip', 'y', (8, 0, 8), 2.6, 0, 1.0, 'knot', cap='root')
    m.octo('grip_collar', 'y', (8, 0, 8), 2.0, 1.0, 1.5, 'root')
    for deg in EIGHTHS:
        m.radial('root', CENTRE, deg, 2.4, 5.6, -.42, .42, 0, .55, 'root')
        m.radial('root_tip', CENTRE, deg, 5.6, 7.6, -.26, .26, 0, .34, 'root')
    for deg in OFFSETS:
        m.radial('rootlet', CENTRE, deg, 3.4, 5.4, -.22, .22, 0, .3, 'root')


def stalk(m):
    """The iron drawn to a fluted spike: a heavy foot, a fluted shaft, a narrow neck, collars where it steps in."""
    m.octo('stalk_foot', 'y', (8, 0, 8), 1.7, .6, 3.4, 'iron')
    m.octo('collar', 'y', (8, 0, 8), 1.85, 3.2, 3.7, 'chisel')
    m.octo('stalk_shaft', 'y', (8, 0, 8), 1.25, 3.7, 7.2, 'pillar')
    for deg in EIGHTHS:
        m.radial('flute', CENTRE, deg, 1.15, 1.5, -.22, .22, 3.8, 7.0, 'chisel')
    m.octo('collar', 'y', (8, 0, 8), 1.4, 7.0, 7.5, 'chisel')
    m.octo('stalk_neck', 'y', (8, 0, 8), .9, 7.5, 9.2, 'iron')


# Three strands of hyphae wind up the stalk: each step is (radius, y0, y1), turning 22.5 degrees a step.
SHEATH = ((1.75, 1.2, 2.6), (1.75, 2.4, 3.4), (1.3, 3.7, 4.8), (1.3, 4.6, 5.8), (1.3, 5.6, 6.8), (.95, 7.5, 8.4),
          (.95, 8.2, 9.1))


def sheath(m):
    for strand in (0, 120 - 7.5, 240 - 15):
        for k, (r, y0, y1) in enumerate(SHEATH):
            deg = round((strand + k * 22.5) / 22.5) * 22.5
            m.radial('sheath', CENTRE, deg, r - .05, r + .35, -.28, .28, y0, y1, 'root')


def bulb(m):
    """The Sporite Crystal bulb on the neck, split at the top into dendrite tips."""
    cx, cy, cz = BULB_CENTRE
    m.octo('bulb_seat', 'y', (cx, 0, cz), 1.35, cy - 2.1, cy - 1.5, 'crystal')
    m.octo('bulb', 'y', (cx, 0, cz), 2.0, cy - 1.5, cy + 1.3, 'crystal')
    m.octo('bulb_shoulder', 'y', (cx, 0, cz), 1.35, cy + 1.3, cy + 2.0, 'crystal')
    m.octo('dendrite_core', 'y', (cx, 0, cz), .5, cy + 2.0, cy + 4.6, 'crystal')
    for deg in (45, 135, 225, 315):
        m.radial('dendrite', (cx, cz), deg, .7, 2.2, -.3, .3, cy + 1.8, cy + 2.4, 'crystal')
        m.radial('dendrite_tip', (cx, cz), deg, 1.8, 2.4, -.28, .28, cy + 2.4, cy + 3.9, 'crystal')
    for deg in (0, 90, 180, 270):
        m.radial('dendrite_spur', (cx, cz), deg, .8, 1.6, -.22, .22, cy + 2.2, cy + 3.0, 'crystal')


def body():
    m = Model(caps=CAPS)
    grip(m)
    stalk(m)
    sheath(m)
    bulb(m)
    return m


def bulb_part():
    m = Model(caps=CAPS)
    bulb(m)
    return m


def withered_textures():
    return {mat: kit.derive_texture(loc, NAME + '_withered_' + mat, *WITHER[mat]) for mat, loc in LIVING.items()}


def blockstate():
    turns = {'up': {}, 'down': {'x': 180}, 'north': {'x': 90}, 'south': {'x': 90, 'y': 180},
             'east': {'x': 90, 'y': 90}, 'west': {'x': 90, 'y': 270}}
    variants = {}
    for facing, turn in turns.items():
        for withered in (False, True):
            model = 'hemomancy:block/' + NAME + ('_withered' if withered else '')
            variants['facing=%s,withered=%s' % (facing, str(withered).lower())] = dict({'model': model}, **turn)
    return {'variants': variants}


def export():
    dead = withered_textures()
    solid, glow = body(), bulb_part()
    kit.separate([solid, glow], LIVING, set(), 'Mycelial Synapse')
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, LIVING, 'iron', DISPLAY))
    kit.write_json(kit.MODELS / (NAME + '_withered.json'), kit.to_json(solid, dead, 'iron'))
    kit.write_json(kit.MODELS / (NAME + '_bulb.json'), kit.to_json(glow, LIVING, 'crystal'))
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(solid, LIVING, 'iron', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), blockstate(), indent=2)
    project = kit.bbmodel(NAME, [('synapse', solid), ('bulb', glow)], LIVING, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    return solid


def preview():
    withered_textures()
    living = body()
    dead = body()
    for e in dead.elements:
        e['faces'] = {side: ('dead_' + mat, coords) for side, (mat, coords) in e['faces'].items()}
    textures = dict(LIVING)
    textures.update({'dead_' + mat: 'hemomancy:block/%s_withered_%s' % (NAME, mat) for mat in LIVING})
    alive = [(living, kit.identity)]
    panels = [('Front', 200, 18, alive), ('Three-quarter', 235, 26, alive), ('Above', 210, 62, alive),
              ('Withered', 235, 26, [(dead, kit.identity)])]
    return kit.preview(panels, 4, kit.ROOT / 'build/mycelial_synapse_preview.png', (340, 420), 19, (8, 8, 8),
                       textures)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Mycelial Synapse: %d elements' % len(model.elements))
    if args.preview:
        print(preview())

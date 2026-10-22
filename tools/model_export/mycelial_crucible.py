"""Author the Mycelial Crucible: a hematic iron culture basin under a grafted fruiting body,
flanked by the mycelial culture vat and the blood cistern that feed it.

The station fills its 3 x 2 x 1 footprint. The centre block holds the basin on a bone-ribbed
pedestal, framed by four posts carrying an erythrocytic cap whose stipe is clamped and tapped into
a bone dropper over the pool. The west vat holds a settled hyphal culture with fruiting strands;
the east cistern holds blood the renderer fills to the stored level. Each vat's arm runs over the
basin and turns down into a nozzle; MycelialCrucibleBlock streams its particles from SPOUTS to
TARGETS. Four enzyme ampoules stand on a rail under the basin's front.

Emits the block model (the item parents it), the blood fill part MycelialCrucibleRenderer scales,
an editable Blockbench project, and removes the retired assets. `--preview` writes
build/mycelial_crucible_preview.png.

Model space is the north-facing main block (the lower centre of the footprint): the operator stands
to the north (low z), and the side blocks sit at x -16..0 and 16..32. Python 3 + Pillow + NumPy.
"""
import argparse
import json

import numpy as np

import station_kit as kit
from station_kit import Model, polar

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'plank': 'hemomancy:block/blood_wood_planks',
    'brick': 'hemomancy:block/polished_venous_stone_bricks',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'glass': 'hemomancy:block/ghastly_alembic_glass',
    'blood': 'hemomancy:block/blood_still',
    'scab': 'hemomancy:block/blood_scorched_scab',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'flesh': 'hemomancy:block/bog_flesh',
    'amber': 'hemomancy:block/mnemonic_ambergris_block',
    'hyphae': 'hemomancy:block/calcified_hyphae',
    'myc': 'hemomancy:block/erythrocytic_mycelium_side',
    'myctop': 'hemomancy:block/fruiting_erythrocytic_mycelium_top',
    'sanguine': 'hemomancy:block/sanguine_glass',
}
TRANSLUCENT = {'glass', 'sanguine'}
CAPS = {'bone': 'bonecap', 'pillar': 'pillartop', 'myc': 'myctop'}
NAME = 'mycelial_crucible'

# Shared with MycelialCrucibleRenderer and MycelialCrucibleBlock (model pixels).
CENTRE = (8.0, 8.0)
BASIN_RADIUS, BASIN_Y = 5.8, 10.035          # apothem and height of the shader pool's octagon
CULTURE_VAT, CISTERN = (-8.0, 8.0), (24.0, 8.0)
VAT_RADIUS, VAT_FLOOR, VAT_TOP = 4.5, 3.5, 19.5
BLOOD_RADIUS, BLOOD_DEPTH = 4.2, 15.6          # cistern fluid: one pixel tall at VAT_FLOOR, scaled by fill
SPOUTS = {'culture': (2.6, 20.0, 8.0), 'blood': (13.4, 20.0, 8.0)}
TARGETS = {'culture': (6.5, 9.75, 8.0), 'blood': (9.5, 9.75, 8.0)}
ITEM_Y = 11.4                                  # the culture being worked turns half-sunk under the dropper

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -1.75, 0], 'scale': [.34, .34, .34]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [.15, .15, .15]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, -3, 0], 'scale': [.3, .3, .3]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 6, 0], 'scale': [.4, .4, .4]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1], 'scale': [.12, .12, .12]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1], 'scale': [.12, .12, .12]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [.12, .12, .12]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [.12, .12, .12]},
}


def dais(m):
    """Venous-stone dais across all three blocks, cast feet at its ends."""
    m.box('dais', (-14.5, 0, 1), (30.5, 1.5, 15), 'brick')
    m.box('dais_trim', (-14, 1.5, 1.5), (30, 2, 14.5), 'chisel')
    for x0 in (-15.25, 28.75):
        for z0 in (.5, 12.5):
            m.box('dais_foot', (x0, 0, z0), (x0 + 2.5, 2.25, z0 + 3), 'bold')


def crucible(m):
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('pedestal_foot', 'y', c3, 4.5, 2, 3, 'bold')
    m.octo('pedestal', 'y', c3, 2.6, 3, 6.2, 'pillar')
    m.octo('pedestal_collar', 'y', c3, 3.2, 4.2, 4.9, 'chisel')
    m.octo('bowl_floor', 'y', c3, 6.25, 6, 7, 'iron')
    m.shell('bowl_wall', CENTRE, 6.75, 6.5, 10.5, 1.0, 'iron')
    m.octo('bowl_stain', 'y', c3, 5.75, 7, 7.2, 'scab')
    m.shell('bowl_lip', CENTRE, 7.15, 10.5, 11.25, 1.1, 'chisel', ends=True)
    # Bone ribs grip the bowl from the pedestal foot, one on each diagonal facet.
    for k in (1, 3, 5, 7):
        m.radial('rib', CENTRE, k * 45, 6.7, 7.5, -.45, .45, 4, 10.5, 'bone')
        m.radial('rib_heel', CENTRE, k * 45, 4.3, 7.5, -.45, .45, 3, 4, 'bone')
        m.radial('rib_claw', CENTRE, k * 45, 6.2, 7.5, -.35, .35, 10.5, 11.6, 'bone')
    # Enzyme ampoules on a blood-wood rail under the bowl's front.
    m.box('ampoule_rail', (2.25, 2, 1.65), (13.75, 2.5, 3.35), 'plank')
    for x, mat in ((3.25, 'amber'), (5.25, 'hyphae'), (10.75, 'vein'), (12.75, 'sanguine')):
        m.octo('ampoule_' + mat, 'y', (x, 0, 2.5), .45, 2.55, 4.1, mat)
        m.shell('ampoule_glass', (x, 2.5), .6, 2.5, 4.75, .14, 'glass', ends=True)
        m.octo('ampoule_stopper', 'y', (x, 0, 2.5), .4, 4.75, 5.3, 'bone')


def canopy(m):
    """Four posts lifting a grafted erythrocytic cap; its stipe is clamped into a dropper."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    for k in (1, 3, 5, 7):
        x, z = polar(CENTRE, k * 45, 9.9)
        m.box('post_base', (x - 1, 2, z - 1), (x + 1, 3, z + 1), 'bold')
        m.box('post', (x - .7, 3, z - .7), (x + .7, 24.6, z + .7), 'pillar')
        m.box('post_capital', (x - .95, 24.6, z - .95), (x + .95, 25.4, z + .95), 'bone')
        m.radial('post_bracket', CENTRE, k * 45, 5.5, 9.9, -.35, .35, 24.85, 25.55, 'iron')
    # The cap: a rolled margin under a low dome of six tiers, pale warts scattered over it.
    m.shell('cap_margin', CENTRE, 8, 25.1, 26, .55, 'myc')
    tiers = ((8, 26, 26.75), (7.6, 26.75, 27.75), (6.8, 27.75, 28.75), (5.6, 28.75, 29.6), (4, 29.6, 30.3),
             (2.2, 30.3, 30.9))
    for n, (r, y0, y1) in enumerate(tiers):
        m.octo('cap_tier%d' % n, 'y', c3, r, y0, y1, 'myc', bottom='hyphae' if n == 0 else None)
    for (deg, dist, n) in ((10, 6.9, 1), (75, 7.0, 1), (140, 6.7, 1), (205, 7.1, 1), (290, 6.8, 1), (330, 7.2, 1),
                           (40, 5.9, 2), (115, 6.0, 2), (180, 5.8, 2), (250, 6.1, 2), (5, 4.6, 3), (95, 4.8, 3),
                           (160, 4.4, 3), (225, 4.7, 3), (300, 4.5, 3), (60, 3.0, 4), (200, 2.9, 4), (320, 3.1, 4)):
        x, z = polar(CENTRE, deg, dist)
        top = tiers[n][2]
        w = .45 if n < 3 else .35
        m.box('cap_wart', (x - w, top, z - w), (x + w, top + .3, z + w), 'hyphae', faces=('north', 'south', 'east', 'west', 'up'))
    for k in range(16):
        m.radial('gill', CENTRE, k * 22.5, 1.6, 7.4, -.1, .1, 25.4, 26, 'hyphae', faces=('north', 'south', 'east', 'west', 'down'))
    # Stipe, its torn veil, and the clamp that taps it.
    m.octo('stipe', 'y', c3, 1.35, 19.25, 26, 'hyphae')
    m.shell('veil', CENTRE, 2.25, 21.75, 22.6, .9, 'hyphae', ends=True)
    m.box('veil_tatter', (6.1, 21.0, 9.6), (7.0, 21.75, 9.8), 'hyphae')
    m.box('veil_tatter', (9.6, 20.6, 6.6), (9.8, 21.75, 7.5), 'hyphae')
    m.octo('stipe_clamp', 'y', c3, 1.8, 18.4, 19.4, 'chisel')
    m.box('clamp_screw', (9.6, 18.6, 7.7), (10.8, 19.2, 8.3), 'iron')
    m.octo('dropper', 'y', c3, .55, 15.4, 18.4, 'bone')
    m.octo('dropper_tip', 'y', c3, .3, 14.4, 15.4, 'vein')


def vat(m, centre, kind):
    """A strapped glass vat with its arm and nozzle reaching over the basin."""
    cx, cz = centre
    c3 = (cx, 0, cz)
    band = 'bone' if kind == 'culture' else 'chisel'
    m.octo('vat_foot', 'y', c3, 5, 2, 3, 'bold')
    m.octo('vat_floor', 'y', c3, 4.75, 3, VAT_FLOOR, 'iron')
    m.shell('vat_glass', centre, VAT_RADIUS, VAT_FLOOR, VAT_TOP, .25, 'glass')
    for k in (1, 3, 5, 7):
        m.radial('vat_strap', centre, k * 45, VAT_RADIUS - .05, VAT_RADIUS + .4, -.35, .35, VAT_FLOOR, VAT_TOP, 'iron')
    for y in (6.5, 15.5):
        m.shell('vat_hoop', centre, VAT_RADIUS + .45, y, y + .6, .3, band, ends=True)
    m.octo('vat_collar', 'y', c3, 5, VAT_TOP, 21, 'iron')
    m.shell('vat_collar_band', centre, 5.2, 19.85, 20.6, .4, band, ends=True)
    m.octo('vat_lid', 'y', c3, 4, 21, 22, 'chisel')
    m.octo('vat_dome', 'y', c3, 2.5, 22, 23, 'iron')
    # Riser, elbow and arm over the basin, then a nozzle down to the spout tip.
    sx, sy, sz = SPOUTS[kind]
    step = 1 if sx > cx else -1
    m.octo('arm_riser', 'y', c3, .55, 23, 24.4, 'iron')
    m.box('arm_elbow', (cx - .65, 24.0, cz - .65), (cx + .65, 25.1, cz + .65), 'chisel')
    x0, x1 = sorted((cx + .65 * step, sx - .6 * step))
    m.box('arm', (x0, 24.15, cz - .4), (x1, 24.95, cz + .4), 'iron')
    m.box('nozzle_elbow', (sx - .6, 23.2, cz - .6), (sx + .6, 25.1, cz + .6), 'chisel')
    m.octo('nozzle', 'y', (sx, 0, sz), .42, sy + .75, 23.2, 'iron')
    m.octo('nozzle_tip', 'y', (sx, 0, sz), .28, sy, sy + .75, 'bone' if kind == 'culture' else 'vein')
    vx = cx + (x1 - x0 + 1.3) * .45 * step
    m.box('arm_valve', (vx - .2, 25, cz - .2), (vx + .2, 25.6, cz + .2), 'iron')
    m.box('arm_valve_wheel', (vx - .7, 25.6, cz - .7), (vx + .7, 25.9, cz + .7), 'bone' if kind == 'culture' else 'vein')


def culture(m):
    """The west vat's settled hyphal mat, with fruiting strands climbing the glass."""
    cx, cz = CULTURE_VAT
    m.octo('culture_mat', 'y', (cx, 0, cz), 4.2, VAT_FLOOR, 5.6, 'hyphae')
    m.octo('culture_mound', 'y', (cx - .6, 0, cz + .5), 3.0, 5.6, 7.4, 'hyphae')
    m.octo('culture_mound', 'y', (cx + 1.2, 0, cz - .9), 1.6, 7.4, 8.6, 'hyphae')
    m.octo('culture_bloom', 'y', (cx - .5, 0, cz + .4), 2.2, 5.5, 5.9, 'myc', faces=('north', 'south', 'east', 'west', 'up'))
    for (dx, dz, top, cap) in ((-2.2, -1.6, 15.5, 1.1), (1.6, 1.8, 13.0, .9), (2.4, -2.0, 17.2, .8),
                               (-1.0, 2.6, 11.4, .7), (.3, -.4, 18.2, 1.3)):
        x, z = cx + dx, cz + dz
        m.box('strand', (x - .2, 7.0, z - .2), (x + .2, top, z + .2), 'hyphae')
        m.box('strand_branch', (x - .15, top - 3.0, z - .15), (x + .15, top - 2.6, z + 1.0 if dz < 0 else z + .15), 'hyphae')
        m.octo('fruiting_cap', 'y', (x, 0, z), cap, top, top + .55, 'myc', bottom='hyphae')
        m.octo('fruiting_umbo', 'y', (x, 0, z), cap * .45, top + .55, top + .85, 'myctop')


def overgrowth(m):
    """The colony has broken out: threads creeping over the dais, fruiting bodies on the vat's lid."""
    for (x0, x1, z, branches) in ((-5.2, 3.6, 4.2, ((-1.5, 2.6), (2.2, -1.2))),
                                  (-5.4, 1.2, 11.6, ((-2.8, -2.0), (.2, 1.6))),
                                  (-3.2, 4.0, 13.4, ((1.8, -1.5),))):
        m.box('thread', (x0, 2.0, z - .15), (x1, 2.2, z + .15), 'hyphae', faces=('north', 'south', 'east', 'west', 'up'))
        for bx, dz in branches:
            z0, z1 = sorted((z, z + dz))
            m.box('thread_branch', (bx - .15, 2.0, z0), (bx + .15, 2.2, z1), 'hyphae', faces=('north', 'south', 'east', 'west', 'up'))
    cx, cz = CULTURE_VAT
    for (x, z, y, r, h) in ((cx + 1.8, cz + 1.4, 22, 1.0, 1.6), (cx - 2.3, cz - .8, 22, .7, 1.0),
                            (-13.2, 13.0, 2, .9, 1.4), (-12.4, 2.6, 2, .6, .9), (17.4, 13.4, 2, .7, 1.1)):
        m.box('fungus_stem', (x - .2, y, z - .2), (x + .2, y + h, z + .2), 'hyphae')
        m.octo('fungus_cap', 'y', (x, 0, z), r, y + h, y + h + .5, 'myc', bottom='hyphae')


def station():
    m = Model(TRANSLUCENT, CAPS)
    dais(m)
    crucible(m)
    canopy(m)
    vat(m, CULTURE_VAT, 'culture')
    vat(m, CISTERN, 'blood')
    culture(m)
    overgrowth(m)
    return m


def cistern_blood():
    """Unit-height cistern fluid at the vat floor; the renderer scales it by stored blood."""
    m = Model(TRANSLUCENT, CAPS)
    m.octo('cistern_blood', 'y', (CISTERN[0], 0, CISTERN[1]), BLOOD_RADIUS, VAT_FLOOR + .05, VAT_FLOOR + 1.05, 'blood')
    return m


def basin_preview():
    """The shader pool as a flat octagon, for previews only."""
    m = Model(TRANSLUCENT | {'pool'}, CAPS)
    m.octo('pool', 'y', (CENTRE[0], 0, CENTRE[1]), BASIN_RADIUS, BASIN_Y - .01, BASIN_Y, 'pool', faces=('up',))
    return m


def export():
    body = station()
    blood = cistern_blood()
    kit.separate([body], TEXTURES, TRANSLUCENT, 'Mycelial Crucible')
    # Body and glass bake into separate layers (see station_kit.split_glass); the item keeps both.
    solid, glass = kit.split_glass(body)
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'iron', DISPLAY))
    kit.write_json(kit.MODELS / (NAME + '_glass.json'), kit.to_json(glass, TEXTURES, 'iron'))
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(body, TEXTURES, 'iron', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), kit.two_layer_blockstate(NAME), indent=2)
    kit.write_json(kit.MODELS / (NAME + '_blood.json'), kit.to_json(blood, TEXTURES, 'blood'))
    kit.BBMODEL.mkdir(parents=True, exist_ok=True)
    project = kit.bbmodel(NAME, [('station', body), ('cistern_blood', blood)], TEXTURES, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    for stale in ('textures/block/model_mycelial_crucible_model.png', 'models/block/bbmodel/MycelialCrucibleModel.bbmodel'):
        path = kit.ASSETS / stale
        if path.exists():
            path.unlink()
    return body


def preview():
    body, pool = station(), basin_preview()
    floor = np.array([0, VAT_FLOOR + .05, 0])
    def fill(level):
        def apply(p, n):
            return (p - floor) * np.array([1, BLOOD_DEPTH * level, 1]) + floor, n
        return apply
    parts = [(body, kit.identity), (pool, kit.identity), (cistern_blood(), fill(.7))]
    panels = [('Operator side', 200, 22, parts), ('Behind', 20, 24, parts),
              ('Culture vat', 250, 14, parts), ('Above', 205, 58, parts)]
    textures = dict(TEXTURES, pool='hemomancy:block/sanguine_glass')
    return kit.preview(panels, 2, kit.ROOT / 'build/mycelial_crucible_preview.png', (820, 600), 11, (8, 14, 8), textures)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Mycelial Crucible: %d elements' % len(model.elements))
    if args.preview:
        print(preview())

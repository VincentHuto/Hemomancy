"""Author the heart-node: a fungal heart an Apotheos plants in the Gardens, at four stages of maturity.

A crimson heart (the bleeding heart it was made from) sits half sunk in a knot of pale hyphal roots. The
roots splay over the block's floor and a little past it, so the sanctum's spread seems to come out of them.
Thin bone ribs grow up round the heart and close over it in a cage, the old rib ring kept at block scale,
and capped fruiting bodies come up on the roots once it is grown.

Stages (blockstate `stage`, HeartNodeRules.stageAt):
  0  a bare heart in a seed knot
  1  the roots spread
  2  the rib cage rises, still open at the crown
  3  the cage closes and fruiting bodies come up

The heart is not in the stage bodies: HeartNodeRenderer draws `heart_node_heart` so it can beat, scaled
about HEART_CENTRE by HEART_SCALE[stage]. Both constants are mirrored in HeartNodeRenderer. The withered
bodies and heart (greyed flesh, blackened roots) are drawn only by the withering ghost
(HeartNodeMomentRenderer). The block's outline and collision follow KNOT, the root knot.

Emits the four stage models, the heart, their withered twins, the item model (grown body and heart), the
blockstate, the derived withered textures and an editable Blockbench project. `--preview` writes
build/heart_node_preview.png. Python 3 + Pillow + NumPy.
"""
import argparse
import json

import station_kit as kit
from station_kit import Model

NAME = 'heart_node'
LIVING = {
    'root': 'hemomancy:block/calcified_hyphae',
    'knot': 'hemomancy:block/erythrocytic_mycelium_top',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'flesh': 'hemomancy:block/heart_node_flesh',
    'dark': 'hemomancy:block/heart_node_vessel',
    'scab': 'hemomancy:block/blood_scorched_scab',
    'cap': 'hemomancy:block/infected_cap',
    'gill': 'hemomancy:block/fungal_gill',
}
CAPS = {'bone': 'bonecap'}
# The heart's own flesh: the world's crust and bog flesh are near black at block scale, so the heart lifts them
# to a living crimson. (source, desaturate, darken, tint)
FLESH = {
    'flesh': ('hemomancy:block/hemorrhagic_crust', 0, 1.95, (1.0, 1.0, 1.0)),
    'dark': ('hemomancy:block/bog_flesh', 0, 1.5, (1.0, 1.0, 1.0)),
}
# How each material dies: (desaturate, darken, tint). Flesh greys, roots and ribs blacken, caps go brown.
WITHER = {
    'root': (.7, .42, (1.0, .9, .8)),
    'knot': (.85, .38, (1.0, .95, .9)),
    'bone': (.6, .5, (1.0, .92, .82)),
    'bonecap': (.6, .5, (1.0, .92, .82)),
    'flesh': (.9, .62, (1.0, .97, .95)),
    'dark': (.9, .55, (1.0, .97, .95)),
    'scab': (.9, .5, (1.0, 1.0, 1.0)),
    'cap': (.75, .5, (1.0, .85, .7)),
    'gill': (.75, .5, (1.0, .85, .7)),
}

CENTRE = (8.0, 8.0)
HEART_CENTRE = (8.0, 4.4, 8.0)
# The heart's size by stage: it fills out as the node matures.
HEART_SCALE = (.72, .86, 1.0, 1.0)
# The root knot in pixels (x0, y0, z0, x1, y1, z1): HeartNodeBlock's collision box.
KNOT = (4.5, 0, 4.5, 11.5, 3.4, 11.5)
EIGHTHS = (0, 45, 90, 135, 180, 225, 270, 315)
OFFSETS = (22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5)

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 1.5, 0], 'scale': [.8, .8, .8]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.4, .4, .4]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [.6, .6, .6]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 10, 0], 'scale': [.8, .8, .8]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.45, .45, .45]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.45, .45, .45]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
}


def seed_knot(m):
    """The knot the heart was planted in: a tight ball of hyphae with a few rootlets feeling outward."""
    m.octo('seed_knot', 'y', (8, 0, 8), 2.5, 0, 2.6, 'knot', cap='root')
    for deg in (45, 135, 225, 315):
        m.radial('seed_rootlet', CENTRE, deg, 2.2, 4.0, -.35, .35, 0, .5, 'root')
        m.radial('seed_rootlet_tip', CENTRE, deg + 22.5, 3.4, 4.6, -.25, .25, 0, .32, 'root')


def knot(m):
    """The grown knot: a low mound of hyphae, lumps where the main roots leave it, the heart sunk in its top."""
    x0, _, z0, x1, y1, z1 = KNOT
    m.octo('knot', 'y', (8, 0, 8), (x1 - x0) / 2, 0, 2.2, 'knot', cap='root')
    m.octo('knot_crown', 'y', (8, 0, 8), 2.7, 2.2, y1, 'root')
    for deg in (45, 135, 225, 315):
        x, z = kit.polar(CENTRE, deg, 2.7)
        m.octo('knot_lump', 'y', (x, 0, z), 1.1, 0, 2.9, 'root')


def roots(m):
    """Main roots splay over the floor and a little past the block; rootlets fork off between them."""
    for deg in EIGHTHS:
        m.radial('root', CENTRE, deg, 3.0, 6.8, -.55, .55, 0, .75, 'root')
        m.radial('root_reach', CENTRE, deg, 6.8, 8.6, -.4, .4, 0, .5, 'root')
        m.radial('root_tip', CENTRE, deg, 8.6, 9.8, -.26, .26, 0, .3, 'root')
        x, z = kit.polar(CENTRE, deg, 5.0)
        m.octo('root_node', 'y', (x, 0, z), .75, 0, 1.0, 'knot', cap='root')
    for deg in OFFSETS:
        m.radial('rootlet', CENTRE, deg, 4.2, 6.6, -.3, .3, 0, .42, 'root')
        m.radial('rootlet_tip', CENTRE, deg, 6.6, 7.6, -.2, .2, 0, .28, 'root')


# Each rib from its socket in the knot up round the heart: (r0, r1, y0, y1). The heart reaches 3.0 px
# from its centre at the top of a beat, so the ribs keep a pixel clear of it.
RIB_RISE = ((3.6, 4.4, 2.6, 3.6), (4.1, 4.9, 3.6, 4.8), (4.3, 5.1, 4.8, 6.0), (4.0, 4.8, 6.0, 7.1),
            (3.5, 4.3, 7.1, 8.0), (2.9, 3.7, 8.0, 8.8))
RIB_CLOSE = ((2.2, 3.0, 8.8, 9.6), (1.4, 2.3, 9.6, 10.3), (.8, 1.6, 10.3, 10.8))


def ribs(m, closed):
    """Eight thin bone ribs rise from the knot; open, their tips curl in short of the crown, closed they meet."""
    for deg in EIGHTHS:
        m.radial('rib_socket', CENTRE, deg, 3.3, 4.5, -.6, .6, 2.0, 2.9, 'bone')
        for r0, r1, y0, y1 in RIB_RISE:
            m.radial('rib', CENTRE, deg, r0, r1, -.38, .38, y0, y1, 'bone')
        m.radial('rib_spur', CENTRE, deg, 5.0, 5.5, -.22, .22, 5.0, 5.7, 'bonecap')
        if closed:
            for r0, r1, y0, y1 in RIB_CLOSE:
                m.radial('rib', CENTRE, deg, r0, r1, -.34, .34, y0, y1, 'bone')
        else:
            m.radial('rib_tip', CENTRE, deg, 2.5, 3.1, -.26, .26, 8.8, 9.3, 'bonecap')
    if closed:
        m.octo('cage_crown', 'y', (8, 0, 8), 1.5, 10.4, 11.4, 'bone')
        m.octo('crown_knot', 'y', (8, 0, 8), .9, 11.4, 12.0, 'root')


def fruiting_bodies(m):
    """Two capped fruiting bodies up on the roots, gills under their caps."""
    for deg, r, height, size in ((112.5, 6.4, 2.7, 1.35), (292.5, 5.6, 2.1, 1.05)):
        x, z = kit.polar(CENTRE, deg, r)
        m.octo('fruit_stalk', 'y', (x, 0, z), .42, .4, height, 'root')
        m.octo('fruit_cap', 'y', (x, 0, z), size, height, height + .7, 'cap', bottom='gill')
        m.octo('fruit_crown', 'y', (x, 0, z), size * .6, height + .7, height + 1.1, 'cap')
    x, z = kit.polar(CENTRE, 202.5, 4.6)
    m.octo('fruit_bud', 'y', (x, 0, z), .5, .3, 1.3, 'cap')


def stage(n):
    m = Model(caps=CAPS)
    if n == 0:
        seed_knot(m)
        return m
    knot(m)
    roots(m)
    if n >= 2:
        ribs(m, closed=n >= 3)
    if n >= 3:
        fruiting_bodies(m)
    return m


def heart():
    """A fist-sized crimson heart: two ventricles, an apex, dark atria and vessel stubs, coronaries over it."""
    m = Model()
    cx, cy, cz = HEART_CENTRE
    m.octo('heart', 'y', (cx, 0, cz), 2.3, cy - 2.0, cy + 1.9, 'flesh')
    m.octo('ventricle', 'y', (cx - .8, 0, cz + .7), 1.9, cy - 1.6, cy + 1.3, 'flesh')
    m.octo('ventricle', 'y', (cx + .9, 0, cz - .6), 1.8, cy - 1.3, cy + 1.6, 'flesh')
    m.octo('apex', 'y', (cx - .3, 0, cz + .4), 1.3, cy - 2.7, cy - 1.9, 'flesh')
    m.octo('atrium', 'y', (cx - .9, 0, cz - .6), 1.15, cy + 1.6, cy + 2.6, 'dark')
    m.octo('atrium', 'y', (cx + 1.0, 0, cz + .8), 1.05, cy + 1.6, cy + 2.4, 'dark')
    m.octo('aorta', 'y', (cx + .4, 0, cz - .5), .65, cy + 2.2, cy + 3.3, 'dark')
    m.octo('vessel', 'y', (cx - .6, 0, cz + .6), .45, cy + 2.2, cy + 2.9, 'dark')
    for deg, y0, y1 in ((0, cy - 1.6, cy + 1.2), (90, cy - 1.2, cy + 1.6), (180, cy - 1.4, cy + 1.0),
                        (270, cy - 1.8, cy + .9)):
        m.radial('coronary', (cx, cz), deg, 2.25, 2.55, -.25, .25, y0, y1, 'scab')
        m.radial('coronary_branch', (cx, cz), deg, 2.25, 2.5, .25, 1.3, y0 + 1.5, y0 + 1.75, 'scab')
        m.radial('coronary_branch', (cx, cz), deg, 2.25, 2.45, -1.0, -.25, y0 + .7, y0 + .9, 'scab')
    return m


def scaled(factor):
    """A transform for the preview: the heart scaled about its centre, as the renderer draws it."""
    import numpy as np
    centre = np.array(HEART_CENTRE)

    def apply(p, n):
        return (p - centre) * factor + centre, n
    return apply


def flesh_textures():
    for mat, (source, desaturate, darken, tint) in FLESH.items():
        kit.derive_texture(source, LIVING[mat].split('/')[-1], desaturate, darken, tint)


def withered_textures():
    flesh_textures()
    return {mat: kit.derive_texture(loc, NAME + '_withered_' + mat, *WITHER[mat]) for mat, loc in LIVING.items()}


def merged(*models):
    m = Model(caps=CAPS)
    for model in models:
        m.elements.extend(model.elements)
    return m


def blockstate():
    return {'variants': {'stage=%d' % n: {'model': 'hemomancy:block/%s_stage_%d' % (NAME, n)} for n in range(4)}}


def export():
    dead = withered_textures()
    stages = [stage(n) for n in range(4)]
    part = heart()
    kit.separate(stages + [part], LIVING, set(), 'Heart-node')
    for n, body in enumerate(stages):
        kit.write_json(kit.MODELS / ('%s_stage_%d.json' % (NAME, n)), kit.to_json(body, LIVING, 'root', DISPLAY))
        kit.write_json(kit.MODELS / ('%s_withered_stage_%d.json' % (NAME, n)), kit.to_json(body, dead, 'root'))
    kit.write_json(kit.MODELS / (NAME + '_heart.json'), kit.to_json(part, LIVING, 'flesh'))
    kit.write_json(kit.MODELS / (NAME + '_heart_withered.json'), kit.to_json(part, dead, 'flesh'))
    item = merged(stages[3], part)
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(item, LIVING, 'flesh', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), blockstate(), indent=2)
    groups = [('stage_%d' % n, body) for n, body in enumerate(stages)] + [('heart', part)]
    project = kit.bbmodel(NAME, groups, LIVING, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    return stages, part


def preview():
    withered_textures()
    part = heart()
    panels = []
    for n in range(4):
        panels.append(('Stage %d' % n, 215, 24, [(stage(n), kit.identity), (part, scaled(HEART_SCALE[n]))]))
    grown = [(stage(3), kit.identity), (part, kit.identity)]
    panels += [('Grown, low', 160, 6, grown), ('Grown, above', 200, 62, grown)]
    dead = {mat: 'hemomancy:block/%s_withered_%s' % (NAME, mat) for mat in LIVING}
    textures = dict(LIVING)
    preview_dead = merged(stage(3), heart())
    for e in preview_dead.elements:
        e['faces'] = {side: ('dead_' + mat, coords) for side, (mat, coords) in e['faces'].items()}
    textures.update({'dead_' + mat: loc for mat, loc in dead.items()})
    panels.append(('Withered', 215, 24, [(preview_dead, kit.identity)]))
    return kit.preview(panels, 4, kit.ROOT / 'build/heart_node_preview.png', (380, 380), 16, (8, 5.5, 8), textures)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        bodies, heart_part = export()
        print('Exported heart-node: stages %s, heart %d elements'
              % ([len(b.elements) for b in bodies], len(heart_part.elements)))
    if args.preview:
        print(preview())

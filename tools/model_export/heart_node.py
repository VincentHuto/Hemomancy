"""Author the heart-node: a fungal heart an Apotheos plants in the Gardens, at four stages of maturity.

A crimson heart (the bleeding heart it was made from) sits half sunk in a knot of pale hyphal roots. The
roots splay over the block's floor and a little past it, so the sanctum's spread seems to come out of them.
Four bone ribs grow up round the heart and close over it in a cage, the old rib ring kept at block scale,
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
    'root': 'hemomancy:block/heart_node_root',
    'knot': 'hemomancy:block/erythrocytic_mycelium_top',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'flesh': 'hemomancy:block/heart_node_flesh',
    'dark': 'hemomancy:block/heart_node_vessel',
    'cap': 'hemomancy:block/infected_cap',
    'gill': 'hemomancy:block/fungal_gill',
}
CAPS = {'bone': 'bonecap'}
# The node's own calm materials, drawn from world textures with their speckle settled: each source pixel is pulled
# toward its neighbours and its texture's mean, then the values are grouped into a few bands, so a face reads as a
# couple of clear tones at 16 px per block rather than per-pixel noise. The crust and bog flesh are near black at
# block scale, so the heart lifts them to a living crimson; the roots are the calcified hyphae taken off bone white
# so they part from the ribs. (source, smoothing 0..1, contrast, bands, gain, tint)
CALM = {
    'flesh': ('hemomancy:block/hemorrhagic_crust', .55, .5, 4, 1.85, (1.0, .92, .92)),
    'dark': ('hemomancy:block/bog_flesh', .5, .55, 3, 1.4, (1.0, 1.0, 1.0)),
    'root': ('hemomancy:block/calcified_hyphae', .55, .45, 3, .86, (1.0, .93, .88)),
}
# How each material dies: (desaturate, darken, tint). Flesh greys, roots and ribs blacken, caps go brown.
WITHER = {
    'root': (.7, .42, (1.0, .9, .8)),
    'knot': (.85, .38, (1.0, .95, .9)),
    'bone': (.6, .5, (1.0, .92, .82)),
    'bonecap': (.6, .5, (1.0, .92, .82)),
    'flesh': (.9, .62, (1.0, .97, .95)),
    'dark': (.9, .55, (1.0, .97, .95)),
    'cap': (.75, .5, (1.0, .85, .7)),
    'gill': (.75, .5, (1.0, .85, .7)),
}

CENTRE = (8.0, 8.0)
HEART_CENTRE = (8.0, 4.4, 8.0)
# The heart's size by stage: it fills out as the node matures.
HEART_SCALE = (.72, .86, 1.0, 1.0)
# The root knot in pixels (x0, y0, z0, x1, y1, z1): HeartNodeBlock's collision box.
KNOT = (4.5, 0, 4.5, 11.5, 3.4, 11.5)
# Four ribs on the quarter turns, so they sit square on the pixel grid; the main roots leave between them.
QUARTERS = (0, 90, 180, 270)
DIAGONALS = (45, 135, 225, 315)

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
    for deg, reach in zip(DIAGONALS, (4.4, 3.8, 4.6, 3.6)):
        m.radial('seed_rootlet', CENTRE, deg, 2.2, reach, -.5, .5, 0, .5, 'root')


def knot(m):
    """The grown knot: a low mound of hyphae, a lump where each main root leaves it, the heart sunk in its top."""
    x0, _, z0, x1, y1, z1 = KNOT
    m.octo('knot', 'y', (8, 0, 8), (x1 - x0) / 2, 0, 2.2, 'knot', cap='root')
    m.octo('knot_crown', 'y', (8, 0, 8), 2.7, 2.2, y1, 'root')
    for deg in DIAGONALS:
        x, z = kit.polar(CENTRE, deg, 2.7)
        m.octo('knot_lump', 'y', (x, 0, z), 1.1, 0, 2.9, 'root')


# The main roots, one from each lump, at uneven lengths so the splay is not a star: (degrees, reach).
ROOTS = ((45, 9.6), (135, 8.2), (225, 9.2), (315, 7.6))
# Two short rootlets between them.
ROOTLETS = ((22.5, 6.4), (202.5, 5.8))


def roots(m):
    """Four main roots splay over the floor and a little past the block, tapering in two steps; two rootlets."""
    for deg, reach in ROOTS:
        mid = 3.0 + (reach - 3.0) * .62
        m.radial('root', CENTRE, deg, 3.0, mid, -.75, .75, 0, 1.0, 'root')
        m.radial('root_tip', CENTRE, deg, mid, reach, -.5, .5, 0, .5, 'root')
    for deg, reach in ROOTLETS:
        m.radial('rootlet', CENTRE, deg, 3.6, reach, -.5, .5, 0, .5, 'root')


# Each rib from its socket in the knot up round the heart: (r0, r1, y0, y1), a few long overlapping spans so the
# curve steps cleanly. The heart reaches 3.0 px from its centre at the top of a beat, so the ribs keep clear of it.
RIB_RISE = ((3.7, 4.7, 2.4, 4.4), (4.2, 5.2, 4.2, 6.6), (3.8, 4.8, 6.4, 8.2), (3.0, 4.0, 8.0, 9.2))
RIB_CLOSE = ((1.9, 3.1, 9.0, 10.1), (.8, 2.0, 9.9, 10.7))


def ribs(m, closed):
    """Four bone ribs rise from the knot; open, their tips curl in short of the crown, closed they meet."""
    for deg in QUARTERS:
        m.radial('rib_socket', CENTRE, deg, 3.3, 4.9, -.75, .75, 1.8, 2.8, 'bone')
        for r0, r1, y0, y1 in RIB_RISE:
            m.radial('rib', CENTRE, deg, r0, r1, -.5, .5, y0, y1, 'bone')
        if closed:
            for r0, r1, y0, y1 in RIB_CLOSE:
                m.radial('rib', CENTRE, deg, r0, r1, -.5, .5, y0, y1, 'bone')
        else:
            m.radial('rib_tip', CENTRE, deg, 2.3, 3.2, -.5, .5, 9.0, 9.7, 'bonecap')
    if closed:
        m.octo('cage_crown', 'y', (8, 0, 8), 1.5, 10.4, 11.4, 'bone')
        m.octo('crown_knot', 'y', (8, 0, 8), .9, 11.4, 12.0, 'root')


def fruiting_bodies(m):
    """Two capped fruiting bodies up on the roots, gills under their caps."""
    for deg, r, height, size in ((112.5, 6.4, 2.7, 1.35), (292.5, 5.6, 2.1, 1.05)):
        x, z = kit.polar(CENTRE, deg, r)
        m.octo('fruit_stalk', 'y', (x, 0, z), .5, .4, height, 'root')
        m.octo('fruit_cap', 'y', (x, 0, z), size, height, height + .8, 'cap', bottom='gill')


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
    """A fist-sized crimson heart: two ventricles, an apex, dark atria and vessel stubs, two dark coronaries."""
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
    # Two coronaries down the front and back, each with one branch: the groove between the ventricles.
    for deg, y0, y1 in ((45, cy - 1.6, cy + 1.6), (225, cy - 1.4, cy + 1.4)):
        m.radial('coronary', (cx, cz), deg, 2.2, 2.6, -.5, .5, y0, y1, 'dark')
        m.radial('coronary_branch', (cx, cz), deg, 2.2, 2.5, .5, 1.6, y0 + 1.2, y0 + 1.7, 'dark')
    return m


def scaled(factor):
    """A transform for the preview: the heart scaled about its centre, as the renderer draws it."""
    import numpy as np
    centre = np.array(HEART_CENTRE)

    def apply(p, n):
        return (p - centre) * factor + centre, n
    return apply


def calm_texture(source, name, smoothing, contrast, bands, gain, tint):
    """Write textures/block/<name>.png: `source` with its speckle settled (see CALM). Returns its location."""
    import numpy as np
    from PIL import Image
    import resonant_forge_preview as raster
    pixels = raster.texture(source).copy()
    rgb = pixels[..., :3]
    # Each pixel pulled toward its eight neighbours (wrapping, so the texture still tiles).
    near = sum(np.roll(np.roll(rgb, dy, 0), dx, 1) for dy in (-1, 0, 1) for dx in (-1, 0, 1)) / 9
    rgb = rgb * (1 - smoothing) + near * smoothing
    mean = rgb.reshape(-1, 3).mean(0)
    rgb = mean + (rgb - mean) * contrast
    # The values grouped into a few bands about the mean, the hue kept.
    lum = rgb @ np.array([.299, .587, .114])
    lo, hi = lum.min(), lum.max()
    step = (np.round((lum - lo) / max(hi - lo, 1e-6) * (bands - 1)) / (bands - 1)) * (hi - lo) + lo
    rgb = rgb * (step / np.maximum(lum, 1e-6))[..., None]
    pixels[..., :3] = np.clip(rgb * gain * np.array(tint), 0, 1)
    out = kit.ASSETS / 'textures/block' / (name + '.png')
    Image.fromarray((pixels * 255).round().astype(np.uint8), 'RGBA').save(out)
    raster._textures.pop('hemomancy:block/' + name, None)
    return 'hemomancy:block/' + name


def calm_textures():
    for mat, args in CALM.items():
        calm_texture(args[0], LIVING[mat].split('/')[-1], *args[1:])


def withered_textures():
    calm_textures()
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

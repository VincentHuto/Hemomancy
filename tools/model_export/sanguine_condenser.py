"""Author the Sanguine Condenser's upgraded stages, grown out of the vanilla cauldron it began as.

Stage 0 is the vanilla cauldron itself (minecraft:block/cauldron); SanguineCondenserRenderer draws its blood.
Both upgrades keep the vanilla cauldron's exact body (2-px walls, floor at y 3-4, L-shaped corner legs) and its
side/top/inside/bottom texture layout, so they read as the same vessel re-made:
- Latticed (stage 1): hematic-iron plate with a crimson-lacquer lip, two riveted bands, a blood runnel down each
  face and bone feet, all in its authored texture. A slim iron lattice crosses the mouth under the lip, a clot
  beaded at each crossing.
- Druse (stage 2): blackened iron cracked with crystal veins under a crystal-crust lip; the inside walls are a
  druse of tiny blood-crystal facets. Clusters of lit crystal points splay out of each corner.

The 12x12 mouth stays clear from the floor to the lip for the renderer's blood, setting bead and formations.
Iron, bone and blood only: no copper.

Textures come from sanguine_condenser_textures.json (native 16x16 grids, plus two seeded fill patterns).
Emits the textures, block models, the item model (Latticed), the stage/facing blockstate and editable Blockbench
projects. `--preview` writes build/sanguine_condenser_preview.png. Python 3 + Pillow + NumPy.
"""
import argparse
import json
import random
from pathlib import Path

from PIL import Image

import station_kit as kit
from station_kit import BRIGHT, Model

SOURCE = Path(__file__).with_name('sanguine_condenser_textures.json')
TEXTURE_DIR = kit.ASSETS / 'textures/block/sanguine_condenser'
NAME = 'sanguine_condenser'
STAGES = {1: 'latticed', 2: 'druse'}
TEXTURES = {name: 'hemomancy:block/sanguine_condenser/' + name for name in (
    'latticed_side', 'latticed_top', 'latticed_inner', 'latticed_bottom', 'latticed_rib', 'clot',
    'druse_side', 'druse_top', 'druse_inner', 'druse_bottom', 'crystal')}

LIP = 16.0            # the cauldron's walls run to the top of the block
BLOOD_TOP = 15.0      # SanguineCondenserRules.surfaceHeight(1) * 16: the fullest pool

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [.625, .625, .625]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [.5, .5, .5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
}


# ---------------------------------------------------------------- textures

def textures():
    """{name: 16x16 RGBA image} from the authored grids and the two seeded fills."""
    data = json.loads(SOURCE.read_text(encoding='utf-8'))
    palette = {key: (*bytes.fromhex(value[1:]), 255) for key, value in data['palette'].items()}
    palette['.'] = (0, 0, 0, 0)
    grids = dict(data['textures'])
    gen = data['generated']
    grids['latticed_inner'] = stained_iron(**gen['latticed_inner'])
    grids['druse_inner'] = druse_crust(**gen['druse_inner'])
    images = {}
    for name, grid in grids.items():
        assert len(grid) == 16 and all(len(row) == 16 for row in grid), name + ' is not a native 16x16 grid'
        image = Image.new('RGBA', (16, 16))
        image.putdata([palette[pixel] for row in grid for pixel in row])
        images[name] = image
    return images


def stained_iron(base, dither, stain, stainFrom, seed):
    """Dark iron with a sparse dither and a blood tide line staining the lower rows."""
    rnd = random.Random(seed)
    rows = []
    for y in range(16):
        row = ''
        for x in range(16):
            if y >= stainFrom and rnd.random() < .25 + .12 * (y - stainFrom):
                row += stain
            else:
                row += dither if rnd.random() < .18 else base
        rows.append(row)
    return rows


def druse_crust(base, facets, sparkle, seed):
    """Tiny crystal facets packed over a dark crust: each a lit pixel over a mid one, a few sparkling."""
    rnd = random.Random(seed)
    grid = [[base] * 16 for _ in range(16)]
    for _ in range(46):
        x, y = rnd.randrange(16), rnd.randrange(16)
        grid[y][x] = facets[0]
        if y > 0:
            grid[y - 1][x] = facets[1] if rnd.random() < .7 else facets[2]
    for _ in range(5):
        grid[rnd.randrange(16)][rnd.randrange(16)] = sparkle
    return [''.join(row) for row in grid]


def write_textures(images):
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    for name, image in images.items():
        image.save(TEXTURE_DIR / (name + '.png'))


# ---------------------------------------------------------------- geometry

def cauldron(m, side, top, inside, bottom):
    """The vanilla cauldron body, element for element: four walls, the floor and the L-shaped corner legs."""
    wall = {'up': top, 'down': inside}
    m.box('wall_west', (0, 3, 0), (2, LIP, 16), side, caps=dict(wall, east=inside))
    m.box('wall_east', (14, 3, 0), (16, LIP, 16), side, caps=dict(wall, west=inside))
    m.box('wall_north', (2, 3, 0), (14, LIP, 2), side, faces=('north', 'south', 'up', 'down'),
          caps=dict(wall, south=inside))
    m.box('wall_south', (2, 3, 14), (14, LIP, 16), side, faces=('north', 'south', 'up', 'down'),
          caps=dict(wall, north=inside))
    m.box('floor', (2, 3, 2), (14, 4, 14), inside, faces=('up', 'down'))
    for x0, x1, z0, z1, sx, sz in ((0, 4, 0, 2, 0, 0), (12, 16, 0, 2, 1, 0), (0, 4, 14, 16, 0, 1),
                                   (12, 16, 14, 16, 1, 1)):
        m.box('leg', (x0, 0, z0), (x1, 3, z1), side, faces=('north', 'south', 'east', 'west', 'down'),
              caps={'down': bottom})
        zx0 = 0 if not sx else 14
        zz0, zz1 = (2, 4) if not sz else (12, 14)
        m.box('leg', (zx0, 0, zz0), (zx0 + 2, 3, zz1), side, faces=('north', 'south', 'east', 'west', 'down'),
              caps={'down': bottom})


def latticed():
    m = Model()
    cauldron(m, 'latticed_side', 'latticed_top', 'latticed_inner', 'latticed_bottom')
    # A slim lattice under the lip, just clear of the fullest pool; a clot beads where the ribs cross.
    y0, y1 = BLOOD_TOP + .1, BLOOD_TOP + .7
    for c in (6.0, 10.0):
        m.box('rib_x', (2, y0, c - .5), (14, y1, c + .5), 'latticed_rib')
        m.box('rib_z', (c - .5, y0 + .02, 2), (c + .5, y1 + .02, 14), 'latticed_rib')
    for x in (6.0, 10.0):
        for z in (6.0, 10.0):
            m.box('clot', (x - .8, y0 - .2, z - .8), (x + .8, y1 + .5, z + .8), 'clot')
    return m


def crystal(m, name, base, size, height, lean=None, row=0):
    """One upright point: a shaft and a narrower tip, mapped to the crystal texture's dark-to-lit bands.

    `lean` is (axis, angle) about the base, so a cluster splays outward; UVs are explicit so the tip reads bright.
    """
    x, y, z = base
    rot = (lean[0], lean[1], [x, y, z]) if lean else None
    shaft = height * .7
    half, tip = size / 2, size / 4
    for label, a, b, v0 in ((name, (x - half, y, z - half), (x + half, y + shaft, z + half), 8 - row),
                            (name + '_tip', (x - tip, y + shaft, z - tip), (x + tip, y + height, z + tip), 0)):
        m.box(label, a, b, 'crystal', rot, extra=BRIGHT)
        e = m.elements[-1]
        width, tall = b[0] - a[0], b[1] - a[1]
        for side, (mat, _) in e['faces'].items():
            if side in ('up', 'down'):
                e['faces'][side] = (mat, [4, 0, 4 + width, width])
            else:
                start = max(0.0, min(16 - tall, v0))
                e['faces'][side] = (mat, [6, round(start, 3), round(6 + width, 3), round(start + tall, 3)])


def druse():
    m = Model()
    cauldron(m, 'druse_side', 'druse_top', 'druse_inner', 'druse_bottom')
    # Each corner sprouts a cluster: a tall point leaning out along one axis, two shorter along the other.
    for cx, cz, sx, sz in ((1, 1, -1, -1), (15, 1, 1, -1), (1, 15, -1, 1), (15, 15, 1, 1)):
        crystal(m, 'spire', (cx, LIP, cz), 1.6, 5.0, ('x', 22.5 * sz))
        crystal(m, 'spur', (cx - sx * .9, LIP, cz), 1.0, 3.0, ('z', -22.5 * sx), row=2)
        crystal(m, 'spur', (cx, LIP, cz - sz * .9), 1.0, 2.2, ('x', 22.5 * sz), row=3)
    # Two low points break each long rim, so the crust reads as growing rather than placed.
    for x, z, lean in ((8, 1, ('x', -22.5)), (8, 15, ('x', 22.5)), (1, 8, ('z', 22.5)), (15, 8, ('z', -22.5))):
        crystal(m, 'rim_point', (x, LIP, z), 1.0, 2.4, lean, row=3)
    return m


BUILDERS = {1: latticed, 2: druse}


# ---------------------------------------------------------------- export

def blockstate():
    variants = {}
    for facing, angle in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        variants['facing=%s,stage=0' % facing] = {'model': 'minecraft:block/cauldron'}
        for stage, suffix in STAGES.items():
            apply = {'model': 'hemomancy:block/%s_%s' % (NAME, suffix)}
            if angle:
                apply['y'] = angle
            variants['facing=%s,stage=%d' % (facing, stage)] = apply
    return {'variants': variants}


def export():
    write_textures(textures())
    models = {stage: BUILDERS[stage]() for stage in STAGES}
    kit.separate(list(models.values()), TEXTURES, set(), 'Sanguine Condenser')
    for stage, suffix in STAGES.items():
        kit.write_json(kit.MODELS / ('%s_%s.json' % (NAME, suffix)),
                       kit.to_json(models[stage], TEXTURES, suffix + '_side', DISPLAY))
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'),
                   kit.to_json(models[1], TEXTURES, 'latticed_side', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), blockstate(), indent=2)
    for stage, suffix in STAGES.items():
        project = kit.bbmodel('%s_%s' % (NAME, suffix), [(suffix, models[stage])], TEXTURES, DISPLAY)
        (kit.BBMODEL / ('%s_%s.bbmodel' % (NAME, suffix))).write_text(json.dumps(project), encoding='utf-8')
    return models


def preview():
    panels = []
    for stage, suffix in STAGES.items():
        parts = [(BUILDERS[stage](), kit.identity)]
        panels += [(suffix.title() + ' front', 200, 18, parts), (suffix.title() + ' above', 215, 55, parts)]
    return kit.preview(panels, 4, kit.ROOT / 'build/sanguine_condenser_preview.png', (400, 460), 19, (8, 8, 8),
                       TEXTURES)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        models = export()
        print('Exported Sanguine Condenser: ' + ', '.join('%s %d elements' % (STAGES[s], len(m.elements))
                                                          for s, m in models.items()))
    if args.preview:
        print(preview())

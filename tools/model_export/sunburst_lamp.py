"""Author the Sunburst Lamp: the living bloom that is the Lighthouse Garden's beacon.

A Sunburst bloom grown on a short frond stalk inside a bone cage: four lashed bone posts on a bone foot ring, a
crown ring at the top. The bloom is a gold-orange crust rosette round a pale full-bright core, its petals splayed in
two tiers. The lantern room round it is glass in the template; the lamp is only the living part and its cage. Glows
gold at night (light 15) without any flame.

No facing: the cage is square and the bloom radially even. Block.box(2, 0, 2, 14, 16, 14) covers it (CourtFixtureBlock
shape in BlockInit). Emits block/sunburst_lamp on the sunburst_lamp atlas, the blockstate and an editable Blockbench
project. `--preview` writes build/model-previews/sunburst_lamp.png.
"""
import argparse
import json

import carved_stone
from court_kit import BRIGHT, HALF_BRIGHT, SPECIAL
from fixture_atlas import ASSETS, box, build, model_json, preview, separate, spin, write

ATLAS = 'sunburst_lamp'


def cage():
    e = []
    for x0, z0 in ((2, 2), (12.5, 2), (2, 12.5), (12.5, 12.5)):
        e.append(box('post', (x0, 1, z0), (x0 + 1.5, 15, z0 + 1.5), 'bone'))
        e.append(box('lashing', (x0 - .25, 6, z0 - .25), (x0 + 1.75, 7, z0 + 1.75), 'gristle'))
    e.append(box('foot_n', (2, 0, 2), (14, 1, 3.5), 'bone'))
    e.append(box('foot_s', (2, 0, 12.5), (14, 1, 14), 'bone'))
    e.append(box('foot_w', (2, 0, 3.5), (3.5, 1, 12.5), 'bone'))
    e.append(box('foot_e', (12.5, 0, 3.5), (14, 1, 12.5), 'bone'))
    e.append(box('crown_n', (2, 15, 2), (14, 16, 3), 'bone'))
    e.append(box('crown_s', (2, 15, 13), (14, 16, 14), 'bone'))
    e.append(box('crown_w', (2, 15, 3), (3, 16, 13), 'bone'))
    e.append(box('crown_e', (13, 15, 3), (14, 16, 13), 'bone'))
    e.append(box('bed', (4, 0.5, 4), (12, 1.5, 12), 'moss'))
    return e


def bloom():
    e = [box('stalk', (7.25, 1.5, 7.25), (8.75, 6, 8.75), 'frond')]
    e.append(box('leaf_n', (7, 2, 4.5), (9, 2.5, 7.25), 'frond', rotation=spin('x', 22.5, (8, 2, 7.25))))
    e.append(box('leaf_s', (7, 2, 8.75), (9, 2.5, 11.5), 'frond', rotation=spin('x', -22.5, (8, 2, 8.75))))
    # Lower petal tier: a wide cross turned 45 degrees, then square on.
    e.append(box('petal_low_x', (3.5, 6.5, 6.5), (12.5, 7.5, 9.5), 'sun', rotation=spin('y', 45, (8, 7, 8)),
                 extra=HALF_BRIGHT))
    e.append(box('petal_low_z', (6.5, 6.5, 3.5), (9.5, 7.5, 12.5), 'sun', rotation=spin('y', 45, (8, 7, 8)),
                 extra=HALF_BRIGHT))
    e.append(box('petal_mid_x', (4.5, 7.5, 7), (11.5, 8.5, 9), 'sun', extra=HALF_BRIGHT))
    e.append(box('petal_mid_z', (7, 7.5, 4.5), (9, 8.5, 11.5), 'sun', extra=HALF_BRIGHT))
    # Upper tier, cupped upward round the core.
    for name, lo, hi, axis, angle, origin in (
            ('cup_n', (6.5, 8.5, 5), (9.5, 11.5, 6), 'x', -22.5, (8, 8.5, 6)),
            ('cup_s', (6.5, 8.5, 10), (9.5, 11.5, 11), 'x', 22.5, (8, 8.5, 10)),
            ('cup_w', (5, 8.5, 6.5), (6, 11.5, 9.5), 'z', 22.5, (6, 8.5, 8)),
            ('cup_e', (10, 8.5, 6.5), (11, 11.5, 9.5), 'z', -22.5, (10, 8.5, 8))):
        e.append(box(name, lo, hi, 'sun', rotation=spin(axis, angle, origin), extra=HALF_BRIGHT))
    e.append(box('core', (6.5, 8, 6.5), (9.5, 11, 9.5), 'sun_core', extra=BRIGHT))
    e.append(box('core_cap', (7, 11, 7), (9, 12, 9), 'sun_core', extra=BRIGHT))
    return e


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body = cage() + bloom()
    moved = separate(body)
    groups = {'body': body}
    carved_stone.register(groups)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    model = model_json(models['body'], ATLAS, atlas.height, f'hemomancy:block/{ATLAS}')
    write({'block/sunburst_lamp': model}, 'sunburst_lamp', model)
    (ASSETS / 'blockstates/sunburst_lamp.json').write_text(
        json.dumps({'variants': {'': {'model': 'hemomancy:block/sunburst_lamp'}}}, indent=2) + '\n')
    print(f'Exported Sunburst Lamp: {len(body)} cubes, {moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        preview(model['elements'], ATLAS, 'Sunburst Lamp - block model, actual atlas', 'sunburst_lamp')


if __name__ == '__main__':
    main()

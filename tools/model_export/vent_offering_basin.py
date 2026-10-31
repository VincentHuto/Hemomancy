"""Author the Vent Offering Basin: the carved ledge-basin at the foot of each of Hydra's heads.

A squat basin of carved blackstone on a stepped foot, its front lip cut low where offerings are fed in toward the
chimney. Inside, a bed of glowing vent salts (mineral heat, light 6, nothing burning). On the rim: a bound bundle of
bone, the last offering, and a pale polyp bud starting to grow over the back wall, because the Kindling's offerings
feed the head they lie beside.

Authored for facing=north: the low lip faces north, toward the chimney. Block.box(1, 0, 1, 15, 9, 15) collides. Emits
block/vent_offering_basin, its atlas, the facing blockstate and a Blockbench project. `--preview` writes
build/model-previews/vent_offering_basin.png.
"""
import argparse

import carved_stone
from court_kit import BRIGHT, SPECIAL, facing_variants, write_blockstate
from fixture_atlas import ASSETS, box, build, model_json, preview, separate, spin, write

ATLAS = 'vent_offering_basin'


def basin():
    return [
        box('foot', (2, 0, 2), (14, 2, 14), 'blackstone'),
        box('foot_step', (1.5, 2, 1.5), (14.5, 3, 14.5), 'blackstone'),
        box('wall_s', (1.5, 3, 11.5), (14.5, 9, 14.5), 'blackstone'),
        box('wall_w', (1.5, 3, 3.5), (4, 8, 11.5), 'blackstone'),
        box('wall_e', (12, 3, 3.5), (14.5, 8, 11.5), 'blackstone'),
        box('lip_n', (1.5, 3, 1.5), (14.5, 5.5, 3.5), 'blackstone'),
        box('lip_notch_w', (1.5, 5.5, 1.5), (5, 7, 3.5), 'blackstone'),
        box('lip_notch_e', (11, 5.5, 1.5), (14.5, 7, 3.5), 'blackstone'),
        box('floor', (4, 3, 3.5), (12, 4.5, 11.5), 'blackstone'),
        box('salts', (4, 4.5, 3.5), (12, 5.5, 11.5), 'ember', extra=BRIGHT),
        box('salt_heap', (6, 5.5, 6), (10, 6.25, 10), 'ember', extra=BRIGHT),
    ]


def offerings():
    return [
        box('bone_bundle', (5, 6.25, 7), (11, 7.25, 8), 'bone', rotation=spin('y', 22.5, (8, 6.75, 7.5))),
        box('bone_bundle_b', (5.5, 6.25, 8.25), (10.5, 7, 9), 'bone', rotation=spin('y', -22.5, (8, 6.5, 8.5))),
        box('bundle_tie', (7.5, 6, 6.75), (8.5, 7.5, 9.25), 'lacquer'),
        box('polyp_bud', (9.5, 9, 12), (12.5, 11, 14), 'polyp'),
        box('polyp_stalk', (10.5, 8, 12.5), (11.5, 9, 13.5), 'polyp'),
        box('polyp_tip', (10.25, 11, 12.25), (11.75, 12.5, 13.75), 'polyp'),
    ]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body = basin() + offerings()
    moved = separate(body)
    groups = {'body': body}
    carved_stone.register(groups)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    model = model_json(models['body'], ATLAS, atlas.height, f'hemomancy:block/{ATLAS}')
    write({'block/vent_offering_basin': model}, 'vent_offering_basin', model)
    write_blockstate('vent_offering_basin', facing_variants('hemomancy:block/vent_offering_basin'))
    print(f'Exported Vent Offering Basin: {len(body)} cubes, {moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        preview(model['elements'], ATLAS, 'Vent Offering Basin - block model, actual atlas', 'vent_offering_basin')


if __name__ == '__main__':
    main()

"""Author the Sunburst Frond: the frond growth of the Lighthouse Garden's beds and arbors.

A low holdfast knot of gold crust with five broad fronds splayed from it: one upright, four leaning out to the
compass points at 22.5 degrees, each with a pale midrib and a ragged edge, gold at the tip and darker at the root.
It grows round the anchored arbor members and along the terraces, above and under water alike, and gives a soft
gold light (light 7) that reads at night from the shore.

No facing. No collision; Block.box(2, 0, 2, 14, 14, 14) is its outline. Emits block/sunburst_frond, its atlas, the
blockstate and an editable Blockbench project. `--preview` writes build/model-previews/sunburst_frond.png.
"""
import argparse
import json

import carved_stone
from court_kit import HALF_BRIGHT, SPECIAL
from fixture_atlas import ASSETS, box, build, model_json, preview, separate, spin, write

ATLAS = 'sunburst_frond'


def fronds():
    e = [box('knot', (5.5, 0, 5.5), (10.5, 2, 10.5), 'sun', extra=HALF_BRIGHT),
         box('knot_cap', (6.5, 2, 6.5), (9.5, 3, 9.5), 'sun', extra=HALF_BRIGHT),
         box('upright', (6.5, 2, 7.75), (9.5, 14, 8.25), 'frond', extra=HALF_BRIGHT)]
    e.append(box('lean_n', (6, 1.5, 6.75), (10, 12.5, 7.25), 'frond', rotation=spin('x', -22.5, (8, 1.5, 7)),
                 extra=HALF_BRIGHT))
    e.append(box('lean_s', (6, 1.5, 8.75), (10, 11.5, 9.25), 'frond', rotation=spin('x', 22.5, (8, 1.5, 9)),
                 extra=HALF_BRIGHT))
    e.append(box('lean_w', (6.75, 1.5, 6.5), (7.25, 11, 9.5), 'frond', rotation=spin('z', 22.5, (7, 1.5, 8)),
                 extra=HALF_BRIGHT))
    e.append(box('lean_e', (8.75, 1.5, 6), (9.25, 12, 10), 'frond', rotation=spin('z', -22.5, (9, 1.5, 8)),
                 extra=HALF_BRIGHT))
    e.append(box('bud_n', (7.5, 3, 5), (8.5, 4, 6), 'sun_core', extra=HALF_BRIGHT))
    e.append(box('bud_e', (10, 2.5, 8.5), (11, 3.5, 9.5), 'sun_core', extra=HALF_BRIGHT))
    return e


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body = fronds()
    moved = separate(body)
    groups = {'body': body}
    carved_stone.register(groups)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    model = model_json(models['body'], ATLAS, atlas.height, f'hemomancy:block/{ATLAS}')
    write({'block/sunburst_frond': model}, 'sunburst_frond', model)
    (ASSETS / 'blockstates/sunburst_frond.json').write_text(
        json.dumps({'variants': {'': {'model': 'hemomancy:block/sunburst_frond'}}}, indent=2) + '\n')
    print(f'Exported Sunburst Frond: {len(body)} cubes, {moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        preview(model['elements'], ATLAS, 'Sunburst Frond - block model, actual atlas', 'sunburst_frond')


if __name__ == '__main__':
    main()

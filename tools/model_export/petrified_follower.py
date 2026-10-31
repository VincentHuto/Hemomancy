"""Author the Petrified Follower: a follower of Marimo who has gone all the way into his moss.

The only true petrification the player sees: a kneeling figure in grey-green stone on the lowest walls of the
Lighthouse Garden's undercroft, head bowed, hands clasped on the knees, moss rooted in every crease. Unlike the
anchored arbor members above (Track 9 residents), nothing here can withdraw. Two states:

- moss=partial: the figure is still plainly a person; moss caps the head and shoulders, trails down the back and
  mounds round the knees.
- moss=whole: the moss has nearly closed over it; only the face and the clasped hands show through the coat.

Authored for facing=north (the figure looks north, -z). Stands 24 px tall (a kneeling adult at 16 px per block), so
the model rises half a block into the cell above; the template keeps that cell clear. Block.box(2, 0, 3, 14, 16, 15)
collides. Emits both models on one atlas, the blockstate (facing x moss) and a Blockbench project. `--preview` writes
build/model-previews/petrified_follower.png (partial on top, whole below).
"""
import argparse

import carved_stone
from court_kit import SPECIAL, facing_variants, write_blockstate
from fixture_atlas import ASSETS, box, build, mirror_x, model_json, preview_pair, separate, spin, write

ATLAS = 'petrified_follower'


def pair(el):
    return [el, mirror_x(el)]


def figure():
    e = []
    e += pair(box('shin', (4, 1, 6), (7.5, 4, 14), 'mossstone'))
    e.append(box('seat', (4, 4, 9.5), (12, 9, 14), 'mossstone'))
    e.append(box('torso', (4.5, 9, 9.5), (11.5, 18, 13), 'mossstone'))
    e += pair(box('upper_arm', (3, 11, 9.75), (4.5, 18, 12.5), 'mossstone'))
    e += pair(box('forearm', (3.25, 9, 6), (4.75, 10.75, 11), 'mossstone'))
    e.append(box('hands', (4.75, 8.75, 6), (11.25, 10.5, 8), 'mossstone', north='skin'))
    e.append(box('neck', (7, 18, 10), (9, 19, 12), 'mossstone'))
    bow = spin('x', 22.5, (8, 18.5, 11))
    e.append(box('head', (5.5, 18.5, 8.5), (10.5, 23.5, 13.5), 'mossstone', rotation=bow, north='skin'))
    e.append(box('brow', (5.75, 21.5, 8.25), (10.25, 22.25, 8.5), 'mossstone', rotation=bow, skip=('south',)))
    return e


def moss_partial():
    bow = spin('x', 22.5, (8, 18.5, 11))
    return [
        box('mound', (1.5, 0, 3.5), (14.5, 1, 14.5), 'moss'),
        box('mound_knees', (3, 1, 5), (13, 2, 8), 'moss'),
        box('shoulder_coat', (3, 17, 9.25), (13, 18.75, 13.5), 'moss'),
        box('back_trail', (5, 8, 13), (11, 17, 13.75), 'moss', skip=('north',)),
        box('head_cap', (5.25, 22.5, 9.5), (10.75, 24.25, 13.75), 'moss', rotation=bow),
    ]


def moss_whole():
    bow = spin('x', 22.5, (8, 18.5, 11))
    out = moss_partial()
    out += [
        box('coat_front', (3.75, 10.5, 8.75), (12.25, 17.5, 9.5), 'moss', skip=('south',)),
        box('coat_left', (2.5, 4, 8), (4, 17, 13.75), 'moss'),
        box('coat_right', (12, 4, 8), (13.5, 17, 13.75), 'moss'),
        box('lap', (3.5, 4, 5.5), (12.5, 8.75, 9.5), 'moss'),
        box('hood', (5, 19, 9.25), (11, 24, 14), 'moss', rotation=bow, skip=('north',)),
        box('hood_side_l', (5, 19, 8.25), (5.5, 23.5, 9.25), 'moss', rotation=bow),
        box('hood_side_r', (10.5, 19, 8.25), (11, 23.5, 9.25), 'moss', rotation=bow),
    ]
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body, partial, whole = figure(), moss_partial(), moss_whole()
    moved = separate(body + whole)
    separate(body + partial)
    groups = {'body': body, 'partial': partial, 'whole': whole}
    carved_stone.register(groups)
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'
    a = model_json(models['body'] + models['partial'], ATLAS, atlas.height, particle)
    b = model_json(models['body'] + models['whole'], ATLAS, atlas.height, particle)
    write({'block/petrified_follower': a, 'block/petrified_follower_whole': b}, 'petrified_follower', b)
    variants = facing_variants('hemomancy:block/petrified_follower', 'moss=partial,')
    variants.update(facing_variants('hemomancy:block/petrified_follower_whole', 'moss=whole,'))
    write_blockstate('petrified_follower', variants)
    print(f'Exported Petrified Follower: {len(body)} figure cubes, {len(partial)}/{len(whole)} moss cubes, '
          f'{moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        preview_pair((a['elements'], ATLAS), (b['elements'], ATLAS),
                     'Petrified Follower - moss=partial (top), moss=whole (bottom)', 'petrified_follower', lift=4)


if __name__ == '__main__':
    main()

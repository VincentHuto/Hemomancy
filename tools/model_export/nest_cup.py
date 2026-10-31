"""Author the Nest Cup: the grown purse cups of the Shared Wake's nursery ground.

A bird's-nest-fungus cup grown up from the bank, its wall cobalt outside and silver at the lip with the crimson
shimmer of the Mermaid's Purse, holding three red egg cases (the purses where young are raised). A second, smaller cup
leans off its side. No facing; no light. Block.box(2, 0, 2, 14, 9, 14) collides. Emits block/nest_cup, its atlas,
the blockstate and a Blockbench project. `--preview` writes build/model-previews/nest_cup.png.
"""
import argparse

from court_outpost_kit import export
from fixture_atlas import box, spin


def cup():
    # A flared bowl: narrow at its foot, octagonal at its lip (the corners are cut by four turned posts).
    e = [box('stalk', (6.5, 0, 6.5), (9.5, 2, 9.5), 'purse'),
         box('foot', (5, 2, 5), (11, 3.5, 11), 'purse'),
         box('floor', (4, 3, 4), (12, 4, 12), 'purse')]
    for name, lo, hi in (('wall_n', (5, 4, 3), (11, 8, 4.5)), ('wall_s', (5, 4, 11.5), (11, 8, 13)),
                         ('wall_w', (3, 4, 5), (4.5, 8, 11)), ('wall_e', (11.5, 4, 5), (13, 8, 11)),
                         ('lip_n', (5, 8, 2.5), (11, 9, 3.5)), ('lip_s', (5, 8, 12.5), (11, 9, 13.5)),
                         ('lip_w', (2.5, 8, 5), (3.5, 9, 11)), ('lip_e', (12.5, 8, 5), (13.5, 9, 11))):
        e.append(box(name, lo, hi, 'purse'))
    for name, x, z in (('corner_nw', 4.5, 4.5), ('corner_ne', 11.5, 4.5), ('corner_sw', 4.5, 11.5),
                       ('corner_se', 11.5, 11.5)):
        e.append(box(name, (x - 1.5, 4, z - .75), (x + 1.5, 8.75, z + .75), 'purse', rotation=spin('y', 45, (x, 6, z))))
    e += [box('egg_a', (5, 4, 5), (8, 7, 7.5), 'egg', rotation=spin('y', 22.5, (6.5, 4.5, 6.25))),
         box('egg_b', (8.5, 4, 6), (11, 6.5, 9), 'egg', rotation=spin('y', -22.5, (9.75, 4.25, 7.5))),
         box('egg_c', (5.5, 4, 8.5), (9, 6, 11), 'egg')]
    # Egg-case tendrils curling from the purse corners, as a mermaid's purse has.
    e.append(box('tendril_a', (4.5, 6, 4.75), (5, 9, 5.25), 'egg', rotation=spin('z', 22.5, (4.75, 6, 5))))
    e.append(box('tendril_b', (10.5, 5.5, 8.25), (11, 8.5, 8.75), 'egg', rotation=spin('z', -22.5, (10.75, 5.5, 8.5))))
    # The little cup leaning off the east side.
    e.append(box('small_stalk', (12, 0, 11), (14, 2, 13), 'purse'))
    e.append(box('small_cup', (11.5, 2, 10.5), (15.5, 5, 14.5), 'purse', rotation=spin('z', -22.5, (13, 2, 12.5))))
    e.append(box('small_egg', (12.5, 3.5, 11.5), (14.5, 5.5, 13.5), 'egg', rotation=spin('z', -22.5, (13, 2, 12.5))))
    return e


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export('nest_cup', {'body': cup()}, 'Nest Cup', {'': {'model': 'hemomancy:block/nest_cup'}}, args.preview)


if __name__ == '__main__':
    main()

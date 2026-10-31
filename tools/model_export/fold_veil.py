"""Author the Fold Veil: the hanging veil that screens the Veiled Fold's doorways.

A curtain of Ink Cap veil hung from a bone rod across the top of its cell: ink black folds with bone-white highlights
on the crests, three panels hung at slightly different depths so the screen has body, ragged and holed at the hem. From
outside a cleft it reads as shadow; up close it is cloth that was grown. No collision (a visitor parts it).

Authored for facing=north: the veil hangs across the cell's north half. Block.box(0, 0, 5, 16, 16, 9) is the outline.
Emits block/fold_veil, its atlas, the facing blockstate and a Blockbench project. `--preview` writes
build/model-previews/fold_veil.png.
"""
import argparse

from court_kit import facing_variants
from court_outpost_kit import export
from fixture_atlas import box


def veil():
    return [box('rod', (0, 15, 6.5), (16, 16, 7.5), 'bone'),
            box('panel_w', (0, 1, 6), (6, 15, 6.5), 'veil', skip=('up', 'down')),
            box('panel_m', (5.5, 0, 7), (11, 15, 7.5), 'veil', skip=('up', 'down')),
            box('panel_e', (10.5, 2, 6.25), (16, 15, 6.75), 'veil', skip=('up', 'down')),
            box('fold_a', (5, 3, 6.5), (6, 15, 7), 'veil'),
            box('fold_b', (10.5, 1, 6.75), (11.5, 15, 7.25), 'veil'),
            box('knot_w', (2, 14, 6), (3, 15.5, 8), 'bone'),
            box('knot_e', (13, 14, 6), (14, 15.5, 8), 'bone')]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export('fold_veil', {'body': veil()}, 'Fold Veil', facing_variants('hemomancy:block/fold_veil'), args.preview)


if __name__ == '__main__':
    main()

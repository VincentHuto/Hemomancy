"""Author the Current Strand: the Woven Current's living line, strung across a gully and climbable.

Two twisted cords of Dead Man's Rope wound round each other along the cell's axis, with a knot of pale glowing veins at
the middle where strands can join or part. Authored along y (a hanging line); the blockstate turns it for axis=x and
axis=z, like a chain. No collision; climbable (tag minecraft:climbable). Emits block/current_strand, its atlas, the
axis blockstate and a Blockbench project. `--preview` writes build/model-previews/current_strand.png.
"""
import argparse

from court_outpost_kit import export
from fixture_atlas import box, spin


def strand():
    return [box('cord_a', (6.5, 0, 7), (8, 16, 8.5), 'rope', skip=('up', 'down'), rotation=spin('y', 22.5, (8, 8, 8))),
            box('cord_b', (8, 0, 7.5), (9.5, 16, 9), 'rope', skip=('up', 'down'), rotation=spin('y', -22.5, (8, 8, 8))),
            box('knot', (6.5, 7, 6.5), (9.5, 9.5, 9.5), 'rope'),
            box('vein_a', (5, 7.5, 7.75), (6.5, 8.5, 8.25), 'rope_glow'),
            box('vein_b', (9.5, 8, 7.75), (11, 9, 8.25), 'rope_glow')]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    model = 'hemomancy:block/current_strand'
    export('current_strand', {'body': strand()}, 'Current Strand',
           {'axis=y': {'model': model}, 'axis=x': {'model': model, 'x': 90, 'y': 90},
            'axis=z': {'model': model, 'x': 90}}, args.preview)


if __name__ == '__main__':
    main()

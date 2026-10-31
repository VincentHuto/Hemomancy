"""Author the Messenger Buoy: a grown float on the open water, with a trailing line below it.

A tan float bladder (veined, darker beneath) riding the surface: it stands in the top water block and its upper half
rises above the water, with a short bone mast and a red current-thread pennant. Under it a Dead Man's Rope line trails
down into the dark with a weight knot at its end, where messengers find it from below. Set dressing only (L4).

No facing. The model reaches past its cell (y -14..24): the float tops out above the water, the line hangs below.
Block.box(4, 6, 4, 12, 16, 12) collides. Emits block/messenger_buoy, its atlas, the blockstate and a Blockbench
project. `--preview` writes build/model-previews/messenger_buoy.png.
"""
import argparse

from court_kit import HALF_BRIGHT
from court_outpost_kit import export
from fixture_atlas import box, spin


def buoy():
    return [box('float_low', (4.5, 8, 4.5), (11.5, 10, 11.5), 'bladder'),
            box('float', (3.5, 10, 3.5), (12.5, 16, 12.5), 'bladder'),
            box('float_top', (4.5, 16, 4.5), (11.5, 18, 11.5), 'bladder'),
            box('band', (3.25, 13, 3.25), (12.75, 14, 12.75), 'thread', extra=HALF_BRIGHT),
            box('mast', (7.5, 18, 7.5), (8.5, 24, 8.5), 'bone'),
            box('pennant', (8.5, 21, 7.75), (12, 23.5, 8.25), 'thread', extra=HALF_BRIGHT),
            box('tie', (7, 6, 7), (9, 8, 9), 'rope'),
            box('line', (7.5, -12, 7.5), (8.5, 6, 8.5), 'rope', rotation=spin('x', 22.5, (8, 6, 8))),
            box('weight', (6.5, -14, 4.5), (9.5, -11, 7.5), 'blackstone', rotation=spin('x', 22.5, (8, 6, 8)))]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export('messenger_buoy', {'body': buoy()}, 'Messenger Buoy', {'': {'model': 'hemomancy:block/messenger_buoy'}},
           args.preview)


if __name__ == '__main__':
    main()

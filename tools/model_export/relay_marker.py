"""Author the Relay Marker: the post where the Court's messengers hand off between depths.

A tall bone post on a blackstone footing at a layer boundary, wound with red current threads that trail loose from its
top (the blood-current signal the houses read), with a small grown cradle where a messenger leaves what it carries for
the next. The threads near it drift in the water: the block's animateTick adds slow red motes through HemoParticleData
(set dressing only; nothing is carried). Threads light 4.

No facing. The model reaches past its cell (y 0..24), a post tall enough to find; Block.box(5, 0, 5, 11, 16, 11)
collides. Emits block/relay_marker, its atlas, the blockstate and a Blockbench project. `--preview` writes
build/model-previews/relay_marker.png.
"""
import argparse

from court_kit import HALF_BRIGHT
from court_outpost_kit import export
from fixture_atlas import box, spin


def post():
    e = [box('footing', (3.5, 0, 3.5), (12.5, 2, 12.5), 'blackstone'),
         box('step', (5, 2, 5), (11, 3, 11), 'blackstone'),
         box('shaft', (6.5, 3, 6.5), (9.5, 22, 9.5), 'bone'),
         box('knuckle_a', (6, 8, 6), (10, 9, 10), 'bone'),
         box('knuckle_b', (6, 16, 6), (10, 17, 10), 'bone'),
         box('cradle_floor', (9.5, 12, 6.5), (13, 12.5, 9.5), 'rope'),
         box('cradle_lip', (12.5, 12.5, 6.5), (13, 14, 9.5), 'rope'),
         box('cradle_strut', (10, 10, 7.5), (11, 12, 8.5), 'rope'),
         box('finial', (7, 22, 7), (9, 24, 9), 'bone')]
    # Thread windings, then loose ends trailing off the top in the current.
    for i, y in enumerate((5, 11, 19)):
        e.append(box(f'winding_{i}', (6.25, y, 6.25), (9.75, y + 1, 9.75), 'thread', extra=HALF_BRIGHT))
    for i, (lo, hi, axis, angle, origin) in enumerate((
            ((8.5, 13, 9.5), (9, 21, 10), 'x', 22.5, (8.75, 21, 9.5)),
            ((6, 12, 6.5), (6.5, 21, 7), 'z', 22.5, (6.5, 21, 6.75)),
            ((9.5, 15, 6), (10, 22, 6.5), 'z', -22.5, (9.5, 22, 6.25)))):
        e.append(box(f'trail_{i}', lo, hi, 'thread', rotation=spin(axis, angle, origin), extra=HALF_BRIGHT))
    return e


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export('relay_marker', {'body': post()}, 'Relay Marker', {'': {'model': 'hemomancy:block/relay_marker'}},
           args.preview)


if __name__ == '__main__':
    main()

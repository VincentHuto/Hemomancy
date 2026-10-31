"""Author the Bell Sleeper: a Still Bell elder sealed in its folded mantle, hung in the bell's upper ring.

A Hushbell member at its mature form: a closed, folded bell of frost blue-grey mantle wrapped round a body, hung from
its cell's top face by a short cord of bone, a body's length (it hangs 14 px into the cell below). The folds overlap like a closed bud; a pale seam down the
front is where it would unfold when the house wakes it. A faint cold glow (light 3) breathes through the seam. It is
asleep, not dead, and not a resident (Track 9 marks the one awake keeper separately).

No facing; no collision. Block.box(3, 0, 3, 13, 16, 13) is the outline (the part in its own cell). Emits block/bell_sleeper, its atlas, the
blockstate and a Blockbench project. `--preview` writes build/model-previews/bell_sleeper.png.
"""
import argparse

from court_kit import HALF_BRIGHT
from court_outpost_kit import export
from fixture_atlas import box, spin


def sleeper():
    """A body-length cocoon hung from the top of its cell (the model reaches 14 px into the cell below)."""
    e = [box('cord', (7.5, 14, 7.5), (8.5, 16, 8.5), 'bone'),
         box('crown', (6, 12, 6), (10, 14, 10), 'hush'),
         box('shoulder', (4.5, 5, 4.5), (11.5, 12, 11.5), 'hush'),
         box('body', (3.5, -6, 3.5), (12.5, 5, 12.5), 'hush'),
         box('hem', (4.5, -11, 4.5), (11.5, -6, 11.5), 'hush'),
         box('tip', (6.5, -14, 6.5), (9.5, -11, 9.5), 'hush'),
         box('seam', (7.5, -10, 3), (8.5, 11, 3.5), 'frost_glow', extra=HALF_BRIGHT)]
    # Overlapping folds wrapped round the body, each turned a little, like a closed bud.
    for i, (lo, hi, axis, angle, origin) in enumerate((
            ((3, -4, 5), (4, 8, 11), 'z', 22.5, (3.5, 8, 8)),
            ((12, -4, 5), (13, 8, 11), 'z', -22.5, (12.5, 8, 8)),
            ((5, -4, 12), (11, 8, 13), 'x', 22.5, (8, 8, 12.5)),
            ((4, -2, 3), (7.5, 10, 4), 'x', -22.5, (5.75, 10, 3.5)),
            ((8.5, -2, 3), (12, 10, 4), 'x', -22.5, (10.25, 10, 3.5)),
            ((4, -9, 4), (12, -7, 12), 'y', 45, (8, -8, 8)))):
        e.append(box(f'fold_{i}', lo, hi, 'hush', rotation=spin(axis, angle, origin)))
    return e


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export('bell_sleeper', {'body': sleeper()}, 'Bell Sleeper', {'': {'model': 'hemomancy:block/bell_sleeper'}},
           args.preview)


if __name__ == '__main__':
    main()

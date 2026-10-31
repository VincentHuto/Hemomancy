"""Author the Holdfast Root Mass: the gripping growth that braces the Tide Refuge's entrance.

A Holding Stone member gone to its mature form: knotted, root-like masses cemented to the rock, broad and heavy, the
grip of a kelp holdfast rather than a plate or shell. Three trunk knots climb the back wall, a fan of strands spreads
across the floor toward the front, and a few strands reach up and over like fingers into a crack. Stone grey-brown
with coralline pink flecks where it grips. It holds; it is not petrified (the member can release, Track 9).

Authored for facing=north: the mass hugs the south side of its cell and spreads north. Block.box(1, 0, 5, 15, 14, 16)
collides. Emits block/holdfast_root_mass, its atlas, the facing blockstate and a Blockbench project. `--preview`
writes build/model-previews/holdfast_root_mass.png.
"""
import argparse

from court_kit import facing_variants
from court_outpost_kit import export
from fixture_atlas import box, spin


def mass():
    e = [box('footing', (1, 0, 9), (15, 2, 16), 'holdfast'),
         box('trunk_w', (2, 2, 11), (6, 12, 16), 'holdfast'),
         box('trunk_m', (6, 2, 12), (10, 14, 16), 'holdfast'),
         box('trunk_e', (10, 2, 10.5), (14.5, 10, 16), 'holdfast'),
         box('knot_w', (1.5, 6, 10), (6.5, 9, 15), 'holdfast'),
         box('knot_e', (9.5, 4, 9.5), (15, 7, 15), 'holdfast'),
         box('crown', (5, 12, 11), (11, 15, 16), 'holdfast')]
    # Floor strands fanning north, each angled a little off the axis.
    for i, (x, angle) in enumerate(((2.5, 22.5), (6, 0), (9.5, -22.5), (12.5, 0))):
        e.append(box(f'strand_{i}', (x, 0, 3), (x + 1.5, 1.5, 10), 'holdfast',
                     rotation=spin('y', angle, (x + .75, .75, 9.5))))
        e.append(box(f'grip_{i}', (x - .25, 0, 2.5), (x + 1.75, 1, 4), 'holdfast'))
    # Fingers reaching up and over the crown into the rock above.
    e.append(box('finger_w', (3, 12, 13), (4.5, 16, 14.5), 'holdfast', rotation=spin('z', 22.5, (3.75, 12, 13.75))))
    e.append(box('finger_e', (11.5, 10, 12.5), (13, 15, 14), 'holdfast', rotation=spin('z', -22.5, (12.25, 10, 13.25))))
    e.append(box('finger_n', (7.25, 14, 11.5), (8.75, 16, 13), 'holdfast'))
    return e


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export('holdfast_root_mass', {'body': mass()}, 'Holdfast Root Mass',
           facing_variants('hemomancy:block/holdfast_root_mass'), args.preview)


if __name__ == '__main__':
    main()

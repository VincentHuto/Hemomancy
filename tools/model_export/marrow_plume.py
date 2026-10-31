"""Author the Marrow Plume: the red feeding plumes of Last Marrow, grown out of the whale fall's bones.

A cluster of stalked sea lilies: three bone-white stalks beaded with blood red rise from a rooted foot, each ending in
a red club with a feathered crown of fine arms, the tallest leaning out over the others. It grows from the hollow bones
where rooted elders live and along the ribcage, and gives a dull red glow (light 5) at Carrion depth.

No facing; no collision. Block.box(3, 0, 3, 13, 15, 13) is the outline. Emits block/marrow_plume, its atlas, the
blockstate and a Blockbench project. `--preview` writes build/model-previews/marrow_plume.png.
"""
import argparse

from court_kit import HALF_BRIGHT
from court_outpost_kit import export
from fixture_atlas import box, spin


def stalk(name, x, z, height, lean_axis, lean):
    origin = (x + .75, 1, z + .75)
    rot = spin(lean_axis, lean, origin) if lean else None
    e = [box(f'{name}_stalk', (x, 1, z), (x + 1.5, height, z + 1.5), 'marrow', rotation=rot),
         box(f'{name}_club', (x - .75, height, z - .75), (x + 2.25, height + 2.5, z + 2.25), 'plume', rotation=rot,
             extra=HALF_BRIGHT)]
    for i, (dx, dz) in enumerate(((-1.5, 0), (2, 0), (0, -1.5), (0, 2))):
        e.append(box(f'{name}_arm_{i}', (x + dx, height + 2.5, z + dz), (x + dx + 1.5 if dx else x + 1.5,
                                                                         height + 4, z + dz + 1.5 if dz else z + 1.5),
                     'plume', rotation=rot, extra=HALF_BRIGHT))
    return e


def plumes():
    e = [box('foot', (4, 0, 4), (12, 1, 12), 'marrow'),
         box('root_a', (3, 0, 7), (5, .75, 9), 'marrow'),
         box('root_b', (11, 0, 6), (13, .75, 8), 'marrow')]
    e += stalk('tall', 7, 7, 10, 'x', -22.5)
    e += stalk('mid', 4.5, 9.5, 7, 'z', 22.5)
    e += stalk('low', 10, 5, 5, 'z', -22.5)
    return e


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export('marrow_plume', {'body': plumes()}, 'Marrow Plume', {'': {'model': 'hemomancy:block/marrow_plume'}},
           args.preview)


if __name__ == '__main__':
    main()

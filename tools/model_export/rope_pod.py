"""Author the Rope Pod: the lamp-lit pod that hangs from the Woven Current's lines.

A pod of Dead Man's Rope grown round a glowing core and hung from its cell's top face by a short strand: translucent
violet-blue ribs, a pale violet light (light 11) showing between them, and four trailing strands below it that drift
like a siphonophore's. It lights the Line Camp's pods and the strands between them. No facing; no collision.
Block.box(4, 1, 4, 12, 16, 12) is the outline. Emits block/rope_pod, its atlas, the blockstate and a Blockbench
project. `--preview` writes build/model-previews/rope_pod.png.
"""
import argparse

from court_kit import BRIGHT
from court_outpost_kit import export
from fixture_atlas import box, spin


def pod():
    e = [box('hanger', (7.5, 13, 7.5), (8.5, 16, 8.5), 'rope'),
         box('cap', (6, 12, 6), (10, 13, 10), 'rope'),
         box('core', (6.5, 5, 6.5), (9.5, 12, 9.5), 'rope_glow', extra=BRIGHT),
         box('heel', (6.5, 4, 6.5), (9.5, 5, 9.5), 'rope')]
    # Thin ribs with the glowing core between them, bowed out at the pod's waist.
    for i, (x, z) in enumerate(((6, 5.5), (9, 5.5), (6, 9.5), (9, 9.5), (5.5, 6), (5.5, 9), (9.5, 6), (9.5, 9))):
        e.append(box(f'rib_{i}', (x, 5, z), (x + 1, 12, z + 1), 'rope'))
    for i, (lo, hi) in enumerate((((5, 7.5, 7.5), (11, 9.5, 8.5)), ((7.5, 7.5, 5), (8.5, 9.5, 11)))):
        e.append(box(f'waist_{i}', lo, hi, 'rope'))
    for i, (x, z, angle, axis) in enumerate(((6.5, 6.5, 22.5, 'x'), (9, 6.5, -22.5, 'z'), (6.5, 9, 22.5, 'z'),
                                             (9, 9, -22.5, 'x'))):
        e.append(box(f'trail_{i}', (x, 1, z), (x + .5, 4, z + .5), 'rope',
                     rotation=spin(axis, angle, (x + .25, 4, z + .25))))
    return e


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    export('rope_pod', {'body': pod()}, 'Rope Pod', {'': {'model': 'hemomancy:block/rope_pod'}}, args.preview)


if __name__ == '__main__':
    main()

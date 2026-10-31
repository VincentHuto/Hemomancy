"""Author the Sunburst Bloom: Marimo's floating sign, the gold bloom on the water over reef and coast.

A lily-pad-like patch that floats on the water surface (it stands in the air cell above the top water block, like a
lily pad): flat gold rosette pads of Sunburst crust with pale centres, in three variants so a field of them does not
tile: `pad` (one broad rosette), `pair` (two smaller pads, edge to edge) and `bloom` (a pad with a short frond bloom
rising from it, the one that glows most). Light 6 for pad and pair, 9 for bloom: a bloom field reads at night.

No facing in the model (the blockstate spins each variant four ways by position, see the block class). No collision
but a lily pad's thin outline, Block.box(1, 0, 1, 15, 1.5, 15). Emits block/sunburst_bloom_<variant>, one atlas, the
blockstate and a Blockbench project. `--preview` writes build/model-previews/sunburst_bloom.png (the bloom variant).
"""
import argparse

from court_kit import BRIGHT, HALF_BRIGHT
from court_outpost_kit import export
from fixture_atlas import box, spin


def rosette(name, x0, z0, x1, z1):
    """A rounded pad: a long cross with its corners filled by a smaller square, a pale centre on top."""
    cx, cz = (x0 + x1) / 2, (z0 + z1) / 2
    inset = (x1 - x0) * .18
    return [box(f'{name}_x', (x0, 0, z0 + inset), (x1, 1, z1 - inset), 'sun', extra=HALF_BRIGHT),
            box(f'{name}_z', (x0 + inset, 0, z0), (x1 - inset, 1, z1), 'sun', extra=HALF_BRIGHT),
            box(f'{name}_core', (cx - 1.5, .5, cz - 1.5), (cx + 1.5, 1.25, cz + 1.5), 'sun_core', extra=BRIGHT)]


def pad():
    return rosette('pad', 2, 2, 14, 14) + [box('notch', (7.5, .25, 1.5), (8.5, 1.1, 5), 'frond')]


def pair():
    return rosette('big', 1.5, 1.5, 10, 10) + rosette('small', 9, 8.5, 14.5, 14)


def bloom():
    e = rosette('base', 2, 2, 14, 14)
    e += [box('stalk', (7.25, 1, 7.25), (8.75, 4, 8.75), 'frond'),
          box('petal_x', (5, 4, 7), (11, 5, 9), 'sun', rotation=spin('y', 45, (8, 4.5, 8)), extra=HALF_BRIGHT),
          box('petal_z', (7, 4, 5), (9, 5, 11), 'sun', rotation=spin('y', 45, (8, 4.5, 8)), extra=HALF_BRIGHT),
          box('heart', (7, 4.5, 7), (9, 6.5, 9), 'sun_core', extra=BRIGHT)]
    for name, lo, hi, axis, angle, origin in (
            ('cup_n', (7, 4.5, 5.5), (9, 7, 6.25), 'x', -22.5, (8, 4.5, 6)),
            ('cup_s', (7, 4.5, 9.75), (9, 7, 10.5), 'x', 22.5, (8, 4.5, 10))):
        e.append(box(name, lo, hi, 'frond', rotation=spin(axis, angle, origin), extra=HALF_BRIGHT))
    return e


VARIANTS = {'pad': pad, 'pair': pair, 'bloom': bloom}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    groups = {name: make() for name, make in VARIANTS.items()}
    blockstate = {}
    for variant in VARIANTS:
        for spin_index, y in enumerate((0, 90, 180, 270)):
            entry = {'model': f'hemomancy:block/sunburst_bloom_{variant}'}
            if y:
                entry['y'] = y
            blockstate[f'turn={spin_index},variant={variant}'] = entry
    outputs = export('sunburst_bloom', groups, 'Sunburst Bloom', blockstate, False,
                     models={f'block/sunburst_bloom_{v}': v for v in VARIANTS})
    if args.preview:
        from fixture_atlas import preview
        preview(outputs['block/sunburst_bloom_bloom']['elements'], 'sunburst_bloom',
                'Sunburst Bloom (bloom) - block model, actual atlas', 'sunburst_bloom')


if __name__ == '__main__':
    main()

"""Author the House Marker: the boundary post that names whose water you are in (eight houses, one block).

A carved blackstone post on a stepped footing, capped in the house's own grown matter, with a plaque on its face that
carries the house's mark as an inlaid glyph lit faintly from within (so a marker reads at Midnight depth). The eight
marks are the strain icons simplified to 7 x 7 glyphs: the Giving Light's sunburst, the Holding Stone's gripping knot,
the Shared Wake's fin and tail, the Woven Current's branching strands, the Veiled Fold's hood, the Still Bell's bell,
Last Marrow's stalked plume and the Kindling's flame.

One block with a `house` property (and `facing`); one model per house on a shared atlas. Authored for facing=north:
the plaque faces north. Block.box(3, 0, 3, 13, 15, 13) collides. Emits block/house_marker_<house>, the atlas, the
blockstate and a Blockbench project. `--preview` writes build/model-previews/house_marker.png (the Giving Light's).
"""
import argparse

from court_kit import EMBER, FLAME, HALF_BRIGHT, SUN_HI, facing_variants
from court_outpost_kit import (BONE_WHITE, COBALT, CORALLINE, HUSH_HI, INK, INK_HI, MARROW_RED_HI, ROPE_GLOW, SILVER,
                               SPECIAL, export)
from clairaudiograph import clamp, noise
from fixture_atlas import box

GLYPHS = {
    'giving_light': ('#..#..#', '.#.#.#.', '..###..', '#######', '..###..', '.#.#.#.', '#..#..#'),
    'holding_stone': ('.#####.', '##...##', '#.###.#', '#.#.#.#', '#.###.#', '##...##', '#.#.#.#'),
    'shared_wake': ('...#...', '..##...', '.###..#', '#######', '.###..#', '..##...', '...#...'),
    'woven_current': ('#..#..#', '.#.#.#.', '..#.#..', '...#...', '..#.#..', '.#...#.', '#.....#'),
    'veiled_fold': ('..###..', '.#...#.', '#.....#', '#.###.#', '#.#.#.#', '#.#.#.#', '##...##'),
    'still_bell': ('...#...', '..###..', '.#####.', '.#####.', '.#####.', '#######', '...#...'),
    'last_marrow': ('#.#.#.#', '.#####.', '..###..', '...#...', '...#...', '...#...', '.#####.'),
    'kindling': ('...#...', '..##...', '..###..', '.####..', '.#####.', '#######', '.#####.'),
}
COLOURS = {'giving_light': SUN_HI, 'holding_stone': CORALLINE, 'shared_wake': SILVER, 'woven_current': ROPE_GLOW,
           'veiled_fold': BONE_WHITE, 'still_bell': HUSH_HI, 'last_marrow': MARROW_RED_HI, 'kindling': EMBER}
CAPS = {'giving_light': 'sun', 'holding_stone': 'holdfast', 'shared_wake': 'purse', 'woven_current': 'rope',
        'veiled_fold': 'ink', 'still_bell': 'hush', 'last_marrow': 'marrow', 'kindling': 'ember'}
HOUSES = list(GLYPHS)


def ink(face, lx, ly, w, h, key):
    n = noise('ink', key, face, lx, ly)
    return clamp(INK_HI if n > .75 else INK, int(n * 8) - 4) + (255,)


def glyph_painter(house):
    mask, colour = GLYPHS[house], COLOURS[house]

    def paint(face, lx, ly, w, h, key):
        n = noise('glyph', house, face, lx, ly)
        if face == 'north' and w == 7 and h == 7 and mask[ly][lx] == '#':
            return clamp(colour, int(n * 16) - 8) + (255,)
        rgb = clamp((30, 26, 32), int(n * 8) - 4)
        if face == 'north' and (lx in (0, w - 1) or ly in (0, h - 1)) and w == 7:
            rgb = clamp(rgb, -8)
        return rgb + (255,)
    return paint


def marker(house):
    cap = CAPS[house]
    return [box('footing', (3, 0, 3), (13, 1.5, 13), 'blackstone'),
            box('step', (4, 1.5, 4), (12, 2.5, 12), 'blackstone'),
            box('shaft', (5, 2.5, 6), (11, 13, 10), 'blackstone'),
            box('plaque', (4.5, 5, 5.5), (11.5, 12, 6), f'glyph_{house}', extra=HALF_BRIGHT),
            box('collar', (4.5, 12.5, 5.5), (11.5, 13.5, 10.5), 'blackstone'),
            box('cap', (5, 13.5, 6.5), (11, 14.5, 9.5), cap),
            box('cap_knob', (6.5, 14.5, 7), (9.5, 15, 9), cap)]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    special = dict(SPECIAL, ink=ink, **{f'glyph_{h}': glyph_painter(h) for h in HOUSES})
    groups = {house: marker(house) for house in HOUSES}
    blockstate = {}
    for house in HOUSES:
        blockstate.update(facing_variants(f'hemomancy:block/house_marker_{house}', f'house={house},'))
    # The variant keys must list properties alphabetically: facing before house.
    ordered = {}
    for key, value in blockstate.items():
        props = dict(part.split('=') for part in key.split(','))
        ordered[','.join(f'{k}={props[k]}' for k in sorted(props))] = value
    outputs = export('house_marker', groups, 'House Marker', ordered, False, special=special,
                     models={f'block/house_marker_{h}': h for h in HOUSES})
    if args.preview:
        from fixture_atlas import preview
        preview(outputs['block/house_marker_giving_light']['elements'], 'house_marker',
                'House Marker (Giving Light) - block model, actual atlas', 'house_marker')


if __name__ == '__main__':
    main()

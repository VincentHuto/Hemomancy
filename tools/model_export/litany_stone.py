"""Author the Litany Stone: the standing inscription slab of the Litany trial.

A two-block slab of dark venous stone on a stepped plinth, under an overhanging capstone set with
three bone studs, its front corners held by hematic iron clamps. The north face is a recessed field
framed by a raised border, cut with four lines of Abocipher: each line hangs from a shared headline
as the cipher's letters do, joined within a word and broken between words. The cuts are dark at
their top edge and keep a faint crimson in-fill below, with the stone under each stroke catching
the light. The glyphs are 1 px reductions of letters in textures/font/abocipher.png.

A scorched variant (blockstate `charred`, Putriciel's Burned Library) is the same slab soot-blackened from
the ground up, spalled pale where the heat split its skin and flecked with ash; most of the inscription is
burned away to a black smear, and only the SURVIVING lines are still cut clean enough to read, their old
crimson in-fill dulled. It paints its own atlas (`litany_stone_charred`), so the plain stone stays
byte-identical.

Emits the lower and upper block models (`litany_stone[_charred]_<half>`), the atlases, the blockstate (facing x
half x charred) and editable Blockbench projects. Python 3 + Pillow + NumPy. `--preview` writes
build/model-previews/litany_stone.png and litany_stone_charred.png.

Coordinates are block pixels for facing=north, authored as one 32-pixel piece and cut at y = 16.
"""
import argparse
import json

import carved_stone
from carved_stone import stone_rgb, world
from clairaudiograph import clamp
from fixture_atlas import (ASSETS, box, build, item_elements, mirror_x, model_json, paint, preview, separate,
                           split_storeys, write)

ATLAS = 'litany_stone'
FIELD = (3, 13, 5, 26)    # texel-centre x range and block-pixel y range of the recessed field
HEADLINE_ROW = 1          # glyph row the shared headline runs along

SHAPE = {'lower': [(0.5, 0, 3.5, 15.5, 2, 12.5), (1, 2, 4.5, 15, 3, 11.5), (1.5, 3, 5.5, 14.5, 16, 10)],
         'upper': [(1.5, 0, 5.5, 14.5, 12, 10), (1, 12, 5, 15, 14, 10.5), (2, 14, 5.5, 14, 15, 10),
                   (4, 15, 6.5, 12, 16, 9)]}

# Each glyph: rows top to bottom, the mark above the headline, the headline, two rows hanging below.
GLYPHS = {
    'a': ['.#.', '###', '#.#', '.#.'],   # cross over a bowl
    'c': ['.#.', '###', '#.#', '###'],   # loop over loop
    'd': ['#.#', '###', '.#.', '#.#'],
    'e': ['.#.', '###', '.#.', '###'],   # tick over two bars
    'n': ['#.#', '###', '.#.', '###'],   # arcs over a box
    'o': ['#.#', '###', '.#.', '.#.'],   # fork over a stem
    'q': ['..#', '###', '#..', '.##'],
    't': ['##.', '###', '.#.', '.##'],
    'l': ['#.', '##', '#.', '.#'],
    'r': ['.#', '##', '#.', '##'],
    'j': ['#.', '##', '.#', '#.'],
}
# Four lines, each word a run of joined glyphs; every line fits the 9 px between the field margins.
LINES = [['ar', 'l'], ['j', 'ol'], ['crl'], ['tl', 'r']]
# The lines still legible on the charred stone; the fire took the rest.
SURVIVING = (0, 2)


def pair(el):
    return [el, mirror_x(el)]


def slab():
    e = []
    e.append(box('plinth', (0.5, 0, 3.5), (15.5, 2, 12.5), 'stone'))
    e.append(box('plinth_step', (1, 2, 4.5), (15, 3, 11.5), 'stone'))
    # The slab: its north face is the recessed field, the border stands half a pixel proud of it.
    e.append(box('slab', (1.5, 3, 6), (14.5, 28, 10), 'stone', north='field', skip=('down', 'up')))
    e += pair(box('border_side', (1.5, 3, 5.5), (2.5, 28, 6), 'stone', skip=('south', 'down', 'up')))
    e.append(box('border_foot', (2.5, 3, 5.5), (13.5, 5, 6), 'stone', skip=('south', 'down', 'east', 'west')))
    e.append(box('border_head', (2.5, 26, 5.5), (13.5, 28, 6), 'stone', skip=('south', 'up', 'east', 'west')))
    # Hematic iron clamps over the border's four corners.
    e += pair(box('clamp_low', (1.25, 3, 5.25), (3, 4.5, 5.5), 'iron', skip=('south', 'down')))
    e += pair(box('clamp_high', (1.25, 26.5, 5.25), (3, 28, 5.5), 'iron', skip=('south', 'up')))
    # Capstone, stepped to a low ridge, three bone studs along its brow.
    e.append(box('capstone', (1, 28, 5), (15, 30, 10.5), 'stone'))
    e.append(box('cap_step', (2, 30, 5.5), (14, 31, 10), 'stone', skip=('down',)))
    e.append(box('cap_ridge', (4, 31, 6.5), (12, 32, 9), 'stone', skip=('down',)))
    for x in (3.5, 7.5, 11.5):
        e.append(box('cap_stud', (x, 28.5, 4.75), (x + 1, 29.5, 5), 'bone', skip=('south',)))
    return e


def inscription(only=None):
    """Set of (x, y) texel centres cut into the field (only the lines in `only`, when given)."""
    cut = set()
    x0, x1, y0, y1 = FIELD
    for line, words in enumerate(LINES):
        if only is not None and line not in only:
            continue
        top = y1 - 1.5 - line * 5            # centre of the line's top glyph row
        width = sum(sum(len(GLYPHS[g][0]) for g in w) + len(w) - 1 for w in words) + len(words) - 1
        x = (x0 + x1 - width + 1) // 2
        for wi, word in enumerate(words):
            for gi, g in enumerate(word):
                rows = GLYPHS[g]
                for r, row in enumerate(rows):
                    for c, ch in enumerate(row):
                        if ch == '#':
                            cut.add((x + c, top - r))
                x += len(rows[0])
                if gi < len(word) - 1:
                    cut.add((x, top - HEADLINE_ROW))   # the headline joins the letters of a word
                    x += 1
            x += 1
    return cut


CUT = inscription()


def field(face, lx, ly, w, h, key):
    x, y, _ = world(key, face, lx, ly)
    x, y = round(x), round(y - .5) + .5      # texel centres, whatever separate() nudged
    n = carved_stone.noise('cut', x, y)
    if (x, y) in CUT:
        if (x, y + 1) not in CUT:
            return ((48, 20, 26) if n > .3 else (40, 22, 26)) + (255,)    # the shadowed top of the cut
        return ((104, 30, 40) if n > .25 else (84, 26, 34)) + (255,)      # old crimson in-fill
    tone = 12 + (12 if (x, y + 1) in CUT else 0)                          # the lip under a stroke
    if y > FIELD[3] - 1 or x == FIELD[1]:      # top and viewer's left (east) edges
        tone -= 18                                                        # shadow under the border's lip
    return stone_rgb(key, face, lx, ly, w, h, tone=tone, vein=False, mottle=10) + (255,)


SPECIAL = dict(carved_stone.SPECIAL, field=field)

# --- the charred stone -------------------------------------------------------------------------

LEGIBLE = inscription(SURVIVING)
# Block-pixel y spans of the burned-away lines: a black smear runs across each, hiding its strokes.
BURNED_ROWS = [(FIELD[3] - 1.5 - line * 5 - 3.5, FIELD[3] - 1.5 - line * 5 + .5)
               for line in range(len(LINES)) if line not in SURVIVING]
SOOT = (22, 17, 18)
SPALL = (118, 108, 104)
ASH = (96, 92, 90)


def scorch(rgb, key, face, lx, ly):
    """Soot over a texel: heaviest low down and in the cut's shadow, broken by pale spalls and ash flecks."""
    x, y, z = world(key, face, lx, ly)
    n = carved_stone.noise('scorch', key, face, lx, ly)
    bloom = carved_stone.value_noise(x, y, z, 3, 'soot')
    if bloom > .82 and face not in ('down',):
        return clamp(SPALL, int(n * 14) - 7)          # heat-split skin, the paler stone under the soot
    if n > .985:
        return clamp(ASH, int(n * 10))
    k = min(.92, .5 + .35 * bloom + max(0.0, 1 - y / 20) * .2)
    return tuple(int(c * (1 - k) + d * k) for c, d in zip(rgb, SOOT))


def charred_field(face, lx, ly, w, h, key):
    x, y, _ = world(key, face, lx, ly)
    x, y = round(x), round(y - .5) + .5
    n = carved_stone.noise('cut', x, y)
    if (x, y) in LEGIBLE:
        if (x, y + 1) not in LEGIBLE:
            return ((30, 14, 16) if n > .3 else (26, 14, 16)) + (255,)    # the cut's shadowed top
        return ((76, 26, 32) if n > .25 else (62, 22, 28)) + (255,)       # the in-fill, dulled by the fire
    if any(lo <= y <= hi for lo, hi in BURNED_ROWS):
        # A burned-away line: a soot smear over it, its strokes only a shade darker, too faint to read.
        smear = carved_stone.noise('smear', x // 2, y)
        return clamp(SOOT, int(smear * 12) - (3 if (x, y) in CUT else 0)) + (255,)
    rgb = field(face, lx, ly, w, h, key)[:3]
    return scorch(rgb, key, face, lx, ly) + (255,)


def charred(mat):
    def painter(face, lx, ly, w, h, key):
        rgb = paint(mat, face, lx, ly, w, h, key, SPECIAL)[:3]
        return scorch(rgb, key, face, lx, ly) + (255,)
    return painter


CHARRED_ATLAS = 'litany_stone_charred'
CHARRED_SPECIAL = {mat: charred(mat) for mat in ('stone', 'dressed', 'lid', 'bone', 'iron')}
CHARRED_SPECIAL['field'] = charred_field


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for half in ('lower', 'upper'):
            for charred_state in (False, True):
                model = f'hemomancy:block/litany_stone{"_charred" if charred_state else ""}_{half}'
                variants[f'charred={str(charred_state).lower()},facing={facing},half={half}'] = dict(
                    {'model': model}, **({'y': y} if y else {}))
    return {'variants': variants}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    elements = slab()
    moved = separate(elements)
    lower, upper = split_storeys(elements)
    groups = {'lower': lower, 'upper': upper}
    carved_stone.register(groups, {'upper': 16})
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'
    whole = model_json(item_elements((models['lower'], 0), (models['upper'], 16)), ATLAS, atlas.height, particle)
    outputs = {'block/litany_stone_lower': model_json(models['lower'], ATLAS, atlas.height, particle),
               'block/litany_stone_upper': model_json(models['upper'], ATLAS, atlas.height, particle)}
    write(outputs, 'litany_stone', whole)
    # The charred stone paints the same elements on its own atlas, so the plain one never moves.
    c_atlas, c_models = build(groups, CHARRED_SPECIAL)
    c_atlas.save(ASSETS / f'textures/block/{CHARRED_ATLAS}.png')
    c_particle = f'hemomancy:block/{CHARRED_ATLAS}'
    c_whole = model_json(item_elements((c_models['lower'], 0), (c_models['upper'], 16)), CHARRED_ATLAS,
                         c_atlas.height, c_particle)
    write({f'block/litany_stone_charred_{half}': model_json(c_models[half], CHARRED_ATLAS, c_atlas.height, c_particle)
           for half in ('lower', 'upper')}, 'litany_stone_charred', c_whole)
    (ASSETS / 'blockstates/litany_stone.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    print(f'Exported Litany Stone: {len(lower)} lower + {len(upper)} upper cubes, {moved} faces separated, '
          f'128x{atlas.height} atlas, {len(CUT)} glyph texels')
    print(f'  charred: own 128x{c_atlas.height} atlas, lines {SURVIVING} legible ({len(LEGIBLE)} of {len(CUT)} texels)')
    print('Voxel shape (Block.box, north):', SHAPE)
    if args.preview:
        preview(whole['elements'], ATLAS, 'Litany Stone - lower + upper block models, actual atlas', 'litany_stone',
                lift=8, size=400, scale=11)
        preview(c_whole['elements'], CHARRED_ATLAS, 'Litany Stone (charred) - most lines burned away', 'litany_stone_charred',
                lift=8, size=400, scale=11)


if __name__ == '__main__':
    main()

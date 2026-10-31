"""Shared materials and the export step for the Brined Court's outpost and sign set pieces (Track 8).

Builds on court_kit.py (Track 7's Seat materials). Each inner house gets the palette of its strain (spec "Silhouette
and palette"): Holdfast stone grey-brown flecked coralline pink, Mermaid's Purse cobalt and silver with a crimson
shimmer, Dead Man's Rope translucent violet-blue, Ink Cap ink black with bone-white highlights, Hushbell frost blue-grey,
Marrowplume bone white beaded with blood red. Light is biological; no copper, brass or bronze anywhere. 16 px per block.

Every painter has the fixture_atlas signature (face, lx, ly, w, h, key) -> RGBA. `export` runs one generator's last
steps the same way for every piece: separate faces, pack, paint, write the model(s), blockstate, Blockbench project and
an optional preview sheet.
"""
import json

import carved_stone
from clairaudiograph import clamp, noise
from court_kit import SPECIAL as SEAT_SPECIAL
from fixture_atlas import ASSETS, build, model_json, preview, separate, write

HOLDFAST = (106, 94, 82)
HOLDFAST_LO = (78, 68, 60)
CORALLINE = (214, 128, 142)
COBALT = (40, 70, 150)
COBALT_LO = (26, 46, 104)
SILVER = (196, 204, 212)
PURSE_RED = (168, 34, 44)
PURSE_RED_LO = (118, 22, 32)
ROPE = (112, 96, 196)
ROPE_LO = (72, 60, 150)
ROPE_GLOW = (178, 168, 255)
INK = (24, 22, 30)
INK_HI = (54, 50, 62)
BONE_WHITE = (226, 218, 200)
HUSH = (150, 168, 184)
HUSH_LO = (106, 124, 142)
HUSH_HI = (196, 212, 224)
MARROW = (226, 214, 190)
MARROW_RED = (170, 22, 36)
MARROW_RED_HI = (214, 60, 70)
CURRENT_RED = (196, 30, 44)
BLADDER = (196, 168, 130)
BLADDER_LO = (150, 120, 92)


def holdfast(face, lx, ly, w, h, key):
    """Kelp-holdfast root growth: knotted grey-brown strands, the odd coralline pink fleck where it grips."""
    n = noise('holdfast', key, face, lx, ly)
    strand = noise('strand', key, face, lx // 2, ly) > .5
    rgb = clamp(HOLDFAST if strand else HOLDFAST_LO, int(n * 16) - 8)
    if noise('fleck', key, face, lx, ly) > .93:
        rgb = CORALLINE
    return rgb + (255,)


def purse(face, lx, ly, w, h, key):
    """Mermaid's Purse cup wall: cobalt outside fading to a silver lip, a crimson shimmer in the grain."""
    t = ly / max(h - 1, 1)
    n = noise('purse', key, face, lx, ly)
    rgb = tuple(int(a + (b - a) * t) for a, b in zip(SILVER, COBALT))
    if noise('shimmer', key, face, lx, ly // 2) > .96:
        rgb = clamp(PURSE_RED, 20)
    return clamp(rgb, int(n * 12) - 6) + (255,)


def egg(face, lx, ly, w, h, key):
    """A red egg case, the purse itself: dark at the seams, a wet highlight on top."""
    n = noise('egg', key, face, lx, ly)
    rgb = PURSE_RED_LO if lx in (0, w - 1) and w > 2 else PURSE_RED
    if ly == 0 and face not in ('down',):
        rgb = clamp(PURSE_RED, 36)
    return clamp(rgb, int(n * 10) - 5) + (255,)


def rope(face, lx, ly, w, h, key):
    """Dead Man's Rope strand: translucent violet-blue with a paler vein down its length."""
    n = noise('rope', key, face, lx, ly)
    along = ly if h >= w else lx
    rgb = ROPE if (along + int(n * 3)) % 4 else ROPE_LO
    return clamp(rgb, int(n * 14) - 7) + (255,)


def rope_glow(face, lx, ly, w, h, key):
    n = noise('ropeglow', key, face, lx, ly)
    return (ROPE_GLOW if n > .3 else clamp(ROPE_GLOW, -30)) + (255,)


def veil(face, lx, ly, w, h, key):
    """Ink Cap veil: ink black folds, bone-white highlights along the fold crests, ragged holes near the hem."""
    n = noise('veil', key, face, lx, ly)
    hem = ly >= h - 3
    if hem and noise('hole', key, face, lx, ly) > .62:
        return (0, 0, 0, 0)
    crest = (lx + int(noise('fold', key, ly // 3) * 3)) % 4 == 0
    rgb = BONE_WHITE if crest and n > .7 else INK_HI if crest else INK
    return clamp(rgb, int(n * 8) - 4) + (255,)


def hush(face, lx, ly, w, h, key):
    """Hushbell mantle: frost blue-grey folds, a pale crest on each fold."""
    n = noise('hush', key, face, lx, ly)
    fold = (ly + int(noise('hfold', key, lx // 2) * 2)) % 3
    rgb = HUSH_HI if fold == 0 else HUSH if fold == 1 else HUSH_LO
    return clamp(rgb, int(n * 10) - 5) + (255,)


def frost_glow(face, lx, ly, w, h, key):
    """The cold light breathing through a sleeper's seam."""
    n = noise('frostglow', key, face, lx, ly)
    return ((214, 236, 250) if n > .35 else (172, 206, 230)) + (255,)


def marrow(face, lx, ly, w, h, key):
    """Marrowplume stalk: bone white, beaded with blood red droplets."""
    n = noise('marrow', key, face, lx, ly)
    if noise('bead', key, face, lx, ly) > .9:
        return MARROW_RED_HI + (255,)
    return clamp(MARROW, int(n * 12) - 8) + (255,)


def plume(face, lx, ly, w, h, key):
    """The red feeding crown: blood red clubs, paler at the feathered tips."""
    n = noise('plume', key, face, lx, ly)
    rgb = MARROW_RED_HI if ly == 0 or (lx + ly) % 3 == 0 and n > .5 else MARROW_RED
    return clamp(rgb, int(n * 12) - 6) + (255,)


def thread(face, lx, ly, w, h, key):
    """A red current thread: blood signal, full and bright."""
    n = noise('thread', key, face, lx, ly)
    return clamp(CURRENT_RED, int(n * 30) - 6) + (255,)


def bladder(face, lx, ly, w, h, key):
    """A grown float bladder: tan, veined, darker underneath."""
    n = noise('bladder', key, face, lx, ly)
    rgb = BLADDER_LO if face == 'down' or noise('bvein', key, face, lx // 2, ly) > .86 else BLADDER
    return clamp(rgb, int(n * 12) - 6) + (255,)


def rock(face, lx, ly, w, h, key):
    """Plain coastal stone for footings."""
    n = noise('rock', key, face, lx, ly)
    rgb = (112, 112, 108) if n > .25 else (88, 88, 86)
    return clamp(rgb, int(n * 10) - 5) + (255,)


SPECIAL = dict(SEAT_SPECIAL, holdfast=holdfast, purse=purse, egg=egg, rope=rope, rope_glow=rope_glow, veil=veil,
               hush=hush, frost_glow=frost_glow, marrow=marrow, plume=plume, thread=thread, bladder=bladder, rock=rock)


def export(name, groups, title, blockstate, show=False, special=None, models=None):
    """Pack and paint `groups` into one atlas named `name`, then write the models, the blockstate (a variants dict)
    and the Blockbench project. `models` maps a model path to a group (default: block/<name> from 'body')."""
    moved = sum(separate(elements) for elements in groups.values())
    carved_stone.register(groups)
    atlas, built = build(groups, special or SPECIAL)
    atlas.save(ASSETS / f'textures/block/{name}.png')
    models = models or {f'block/{name}': 'body'}
    outputs = {path: model_json(built[group], name, atlas.height, f'hemomancy:block/{name}')
               for path, group in models.items()}
    first = next(iter(outputs.values()))
    write(outputs, name, first)
    (ASSETS / f'blockstates/{name}.json').write_text(json.dumps({'variants': blockstate}, indent=2) + '\n')
    cubes = sum(len(e) for e in groups.values())
    print(f'Exported {title}: {cubes} cubes, {moved} faces separated, 128x{atlas.height} atlas, {len(outputs)} model(s)')
    if show:
        preview(first['elements'], name, f'{title} - block model, actual atlas', name)
    return outputs

"""Author the Adipocere Crust: corpse wax set on the wall of an old dark room.

A greasy pale sheet of wax spread over the wall in an uneven blob, thicker where it pooled into gobs, with a
darker yellow-brown rind round its edges where it has cured, and a ragged grease stain soaked into the wall
round it. Three runs of uneven length hang from its lower edge; two end in a slightly proud bead. Grown by the
decoration pass in monster rooms, strongholds and ancient cities and scraped off for adipocere, or crafted from four
adipocere and hung as decor.

Authored for `facing=north`: the sheet lies against the cell's south face (z 14..16) and faces north into the
room. 16 px per block (density 1). Block.box(2, 1, 14, 14, 15, 16) covers it (AdipocereCrustBlock SHAPES).

Emits `block/adipocere_crust` on the `adipocere_crust` atlas, the facing blockstate, the item model (the block model
turned so its face looks out of the slot) and an editable Blockbench project. Python 3 + Pillow + NumPy. `--preview` writes
build/model-previews/adipocere_crust.png.
"""
import argparse
import json

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, box, build, model_json, preview, separate, write

ATLAS = 'adipocere_crust'

WAX = (222, 214, 182)        # the greasy body of the sheet
WAX_HI = (244, 240, 220)     # wet sheen
WAX_LO = (190, 178, 140)
RIND = (128, 108, 74)        # the cured edge
RIND_DARK = (84, 68, 46)
GRIME = (96, 92, 70)         # dust caught in the wax
SHEET_PLATES = 5             # sheet() comes first in the body, so its plates are body indices 0..4


# --- geometry ---------------------------------------------------------------------------------

def sheet():
    """The flat body: overlapping one-pixel plates against the wall, making an uneven outline."""
    return [
        box('sheet_core', (4, 5, 15), (12, 13, 16), 'wax', skip=('south',)),
        box('sheet_top', (5, 13, 15.25), (11, 14.5, 16), 'wax', skip=('south',)),
        box('sheet_left', (2.5, 7, 15.25), (4, 12, 16), 'wax', skip=('south',)),
        box('sheet_right', (12, 6, 15.25), (13.5, 11.5, 16), 'wax', skip=('south',)),
        box('sheet_low', (5, 3.5, 15.25), (11, 5, 16), 'wax', skip=('south',)),
    ]


def gobs():
    """Where the wax pooled thicker before it set."""
    return [
        box('gob_big', (5.5, 7, 14.25), (9.5, 11, 15), 'wax', skip=('south',)),
        box('gob_cap', (6.5, 11, 14.5), (8.5, 12, 15), 'wax', skip=('south',)),
        box('gob_side', (10, 8, 14.5), (11.5, 10, 15), 'wax', skip=('south',)),
        box('gob_low', (8, 5, 14.5), (10, 6.5, 15), 'wax', skip=('south',)),
    ]


def drips():
    """Runs off the lower edge, uneven in length, each ending in a slightly proud bead no wider than the run."""
    return [
        box('run_long', (5.5, 1.25, 15.5), (6.5, 3.5, 16), 'run', skip=('south',)),
        box('bead_long', (5.5, 0.5, 15.25), (6.5, 1.25, 16), 'bead', skip=('south',)),
        box('run_short', (9, 2.75, 15.5), (10, 3.5, 16), 'run', skip=('south',)),
        box('run_side', (12.25, 3.5, 15.5), (13, 6, 16), 'run', skip=('south',)),
        box('bead_side', (12.25, 2.75, 15.25), (13, 3.5, 16), 'bead', skip=('south',)),
    ]


STAIN_SKIP = ('south', 'east', 'west', 'up', 'down')   # a flat soak: only its front shows


def stain():
    """A grease stain soaked into the wall round the wax, so the crust does not end in a hard edge."""
    return [
        box('stain_wide', (2, 4.5, 15.75), (14, 13, 16), 'stain', skip=STAIN_SKIP),
        box('stain_top', (3.5, 13, 15.75), (12.5, 15, 16), 'stain', skip=STAIN_SKIP),
        box('stain_low', (4, 2.5, 15.75), (11.5, 4.5, 16), 'stain', skip=STAIN_SKIP),
    ]


# --- painters -----------------------------------------------------------------------------------

def wax(face, lx, ly, w, h, key):
    """Pale wax: mottled, wet sheen up top, cured rind on every side face and round the front's border."""
    n = noise('wax', key, face, lx, ly)
    if face != 'north':
        shade = int(n * 14) - 7 - (14 if face == 'down' else 0) + (10 if face == 'up' else 0)
        return clamp(RIND_DARK if n < .18 else RIND, shade) + (255,)
    # Only the flat sheet cures at its rim; the gobs (body index 5 on) stayed soft under it.
    border = lx in (0, w - 1) or ly in (0, h - 1)
    if key[1] < SHEET_PLATES and border and w > 2 and h > 2 and noise('rind', key, lx, ly) < .45:
        return clamp(RIND, int(n * 12) - 6) + (255,)
    if n > .93:
        return GRIME + (255,)
    if ly <= h // 3 and noise('sheen', key, lx, ly // 2) > .72:
        return WAX_HI + (255,)
    rgb = WAX_LO if noise('mottle', key, lx // 2, ly // 2) < .3 else WAX
    return clamp(rgb, int(n * 10) - 5 - ly) + (255,)


def run(face, lx, ly, w, h, key):
    n = noise('run', key, face, lx, ly)
    return clamp(WAX_LO if face == 'north' else RIND, int(n * 10) - 5) + (255,)


def bead(face, lx, ly, w, h, key):
    """The bead at a run's end: a bright glint on its upper front, darker underneath."""
    n = noise('bead', key, face, lx, ly)
    if face == 'north' and ly == 0 and lx == 0:
        return WAX_HI + (255,)
    if face == 'down':
        return clamp(RIND, int(n * 8) - 8) + (255,)
    return clamp(WAX if face in ('north', 'up') else RIND, int(n * 10) - 5) + (255,)


def stain_paint(face, lx, ly, w, h, key):
    """Dark grease, speckled: only its rim shows past the wax, and the specks keep that rim ragged."""
    n = noise('stain', key, face, lx, ly)
    if face != 'north' or n < .35:
        return (0, 0, 0, 0)
    return clamp(GRIME, int(n * 10) - 22) + (255,)


SPECIAL = dict(wax=wax, run=run, bead=bead, stain=stain_paint)


# The item is a flat thing in the hand, so it takes vanilla's item/generated display values. Those expect the face
# to look out of the middle of the cell, which item_model() arranges.
ITEM_DISPLAY = {
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [0.5, 0.5, 0.5]},
    'head': {'rotation': [0, 180, 0], 'translation': [0, 13, 7], 'scale': [1, 1, 1]},
    'thirdperson_righthand': {'rotation': [0, 0, 0], 'translation': [0, 3, 1], 'scale': [0.55, 0.55, 0.55]},
    'firstperson_righthand': {'rotation': [0, -90, 25], 'translation': [1.13, 3.2, 1.13], 'scale': [0.68, 0.68, 0.68]},
    'fixed': {'rotation': [0, 180, 0], 'scale': [1, 1, 1]},
}


def item_model(block_model):
    """The block's elements pulled forward to the middle of the cell (z 7..9), face still north (the side item
    displays show), with the wall-side faces filled from the front so the crust has a back when it is held."""
    import copy
    elements = []
    for el in copy.deepcopy(block_model['elements']):
        el['from'][2] -= 7
        el['to'][2] -= 7
        faces = el['faces']
        # The stain lies on the same back plane as the sheet, and the gobs stop short of it inside the sheet:
        # backing either would z-fight or never show, so only the wax that touches the wall gets a back.
        touches_back = el['to'][2] == 9 and not el['name'].startswith('stain')
        if touches_back and 'north' in faces and 'south' not in faces:
            faces['south'] = dict(faces['north'])
        for face in faces.values():
            face.pop('cullface', None)
        el['faces'] = faces
        elements.append(el)
    model = {k: v for k, v in block_model.items() if k != 'elements'}
    model.update(gui_light='front', elements=elements, display=ITEM_DISPLAY)
    return model


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        variant = {'model': 'hemomancy:block/adipocere_crust'}
        if y:
            variant['y'] = y
        variants[f'facing={facing}'] = variant
    return {'variants': variants}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body = sheet() + gobs() + drips() + stain()
    moved = separate(body)
    atlas, models = build({'body': body}, SPECIAL, atlas_w=64)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    model = model_json(models['body'], ATLAS, atlas.height, f'hemomancy:block/{ATLAS}')
    model['texture_size'] = [64, atlas.height]
    write({'block/adipocere_crust': model}, 'adipocere_crust', model)
    (ASSETS / 'blockstates/adipocere_crust.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    (ASSETS / 'models/item/adipocere_crust.json').write_text(json.dumps(item_model(model), indent=2) + '\n')
    print(f'Exported Adipocere Crust: {len(body)} cubes, {moved} faces separated, 64x{atlas.height} atlas')
    if args.preview:
        preview(model['elements'], ATLAS, 'Adipocere Crust - facing north, wall at z 16', 'adipocere_crust')


if __name__ == '__main__':
    main()

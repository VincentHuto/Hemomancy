"""Author the Mnemonic Reliquary: a gabled blood-wood casket keeping a preserved memory-brain on a cushion.

The casket stands on a stepped plinth with bone claw feet, its walls strapped in hematic iron, lacquered
side and back panels bordered in bone and marked with a witnessing eye, and a bone-fret window in the
front through which the relic can be seen. Mnemonic Ambergris has dribbled over the plinth's front edge.
The lid is a lacquered reliquary roof with shingled slopes, an iron ridge beam and a bone finial; it
hinges on two knuckles along the back edge and its underside is lined in crimson with a bone inscription.

Emits the static casket, the lid, the relic and its full-bright memory veins as separate block models,
the closed-casket item model, the blockstate, the painted atlas and an editable Blockbench project.
Python 3 + Pillow + NumPy. `--preview` writes closed and open turnarounds to build/model-previews/.

Coordinates are block pixels for the north-facing state; the front (player side) is z = 0.
MnemonicReliquaryRenderer reads the anchors below, so keep its constants in step when moving them.
"""
import argparse
import copy
import json
import math

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, PALETTE, box, build, item_elements, mirror_x, model_json, preview, separate, spin, \
    write

ATLAS = 'mnemonic_reliquary'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}

# Renderer anchors (block pixels). Mirror these in MnemonicReliquaryRenderer.
HINGE = (8.0, 9.0, 12.5)   # lid hinge axis; the lid swings back about +x
LID_OPEN = 100.0           # degrees the lid turns when fully open
RELIC_RISE = 4.5           # how far the relic lifts off its cushion when the lid is open

ROOF_PITCH = 22.5
EAVE_FRONT, EAVE_BACK = 3.5, 12.25
SLOPE = (EAVE_BACK - EAVE_FRONT) / 2 / math.cos(math.radians(ROOF_PITCH))

BONE_PALE = (226, 212, 184)
VELVET = (112, 16, 30)
TISSUE = (172, 118, 110)
SULCUS = (116, 66, 66)
VEIN_CORE = (236, 52, 64)
VEIN_HOT = (255, 170, 150)


def mirror_z(el):
    """The same box reflected across z = 8, for front/back pairs."""
    out = copy.deepcopy(el)
    out['lo'][2], out['hi'][2] = 16 - el['hi'][2], 16 - el['lo'][2]
    swap = {'north': 'south', 'south': 'north'}
    out['skip'] = {swap.get(s, s) for s in el['skip']}
    out['faces'] = {swap.get(k, k): v for k, v in el['faces'].items()}
    return out


def plinth():
    e = []
    for x0 in (2, 12):
        foot = box('foot', (x0, 0, 3), (x0 + 2, 1.5, 5), 'bone')
        e += [foot, mirror_z(foot)]
        for tx in (x0 + .25, x0 + 1.25):
            claw = box('claw', (tx, 0, 2.25), (tx + .5, .75, 3), 'bone', skip=('south',))
            e += [claw, mirror_z(claw)]
    e.append(box('plinth', (1.5, 1.5, 2.5), (14.5, 2.5, 13.5), 'wood'))
    e.append(box('plinth_band', (1.75, 2.5, 2.75), (14.25, 3, 13.25), 'ferric', skip=('down',)))
    # Mnemonic Ambergris dribbled over the plinth's front edge.
    for lo, hi in [((4.5, 1, 2.25), (5.25, 2.5, 2.5)), ((9.75, 1.25, 2.25), (10.25, 2.5, 2.5)),
                   ((11.5, 1.75, 2.25), (12, 2.5, 2.5))]:
        e.append(box('drip', lo, hi, 'drip', skip=('south',)))
    e.append(box('drip_blob', (4.375, .75, 2.125), (5.375, 1.25, 2.5), 'drip', skip=('south',)))
    e.append(box('drip_spill', (4, 2.5, 2.375), (6.5, 2.625, 3.25), 'drip', skip=('down',)))
    return e


def casket():
    e = []
    inner = dict(south='cavity')
    e.append(box('floor', (4, 3, 5), (12, 3.25, 11), 'cavity', skip=('down', 'north', 'south', 'east', 'west')))
    # Front wall, open around the window.
    e.append(box('front_lower', (3, 3, 4), (13, 4.5, 5), 'wood', skip=('down',), **inner))
    e.append(box('front_upper', (3, 7.5, 4), (13, 8.5, 5), 'wood', skip=('up',), **inner))
    e.append(box('front_left', (3, 4.5, 4), (6, 7.5, 5), 'wood', north='lacquer', **inner))
    e.append(box('front_right', (10, 4.5, 4), (13, 7.5, 5), 'wood', north='lacquer', **inner))
    e.append(box('window_grille', (6, 4.5, 4.5), (10, 7.5, 4.5), 'grille', skip=('east', 'west', 'up', 'down')))
    for lo, hi in [((5.5, 7.5, 3.75), (10.5, 8, 4)), ((5.5, 4, 3.75), (10.5, 4.5, 4)),
                   ((5.5, 4.5, 3.75), (6, 7.5, 4)), ((10, 4.5, 3.75), (10.5, 7.5, 4))]:
        e.append(box('window_frame', lo, hi, 'iron', skip=('south',)))
    e.append(box('keyhole', (7.25, 3.125, 3.75), (8.75, 4, 4), 'keyhole', skip=('south',)))
    # Back and side walls: lacquered sigil panels outside, blackened boards within.
    e.append(box('back_wall', (3, 3, 11), (13, 8.5, 12), 'wood', north='cavity', south='sigil', skip=('down', 'up')))
    west = box('side_wall', (3, 3, 5), (4, 8.5, 11), 'wood', west='sigil', east='cavity', skip=('down', 'up'))
    e += [west, mirror_x(west)]
    # Hematic iron corner straps and the rim the lid closes on.
    post = box('corner_post', (2.5, 2.75, 3.5), (4, 9, 5), 'iron', skip=('down',))
    e += [post, mirror_x(post), mirror_z(post), mirror_x(mirror_z(post))]
    e.append(box('rim_front', (4, 8.5, 3.75), (12, 9, 5), 'ferric', skip=('down',)))
    e.append(box('rim_back', (4, 8.5, 11), (12, 9, 12.25), 'ferric', skip=('down',)))
    rim_west = box('rim_side', (2.75, 8.5, 5), (4, 9, 11), 'ferric', skip=('down',))
    e += [rim_west, mirror_x(rim_west)]
    # Carrying handles on both ends.
    for lo, hi in [((2.5, 6, 7), (3, 7, 7.5)), ((2.5, 6, 8.5), (3, 7, 9)), ((2.25, 5.5, 6.75), (2.75, 6, 9.25))]:
        handle = box('handle', lo, hi, 'iron')
        e += [handle, mirror_x(handle)]
    # Hinge knuckles on the back rim; the lid turns about their axis.
    for x0 in (4, 10):
        e.append(box('hinge_knuckle', (x0, HINGE[1] - .5, HINGE[2] - .25), (x0 + 2, HINGE[1] + .5, HINGE[2] + .25),
                     'iron'))
    # The cushion the relic rests on, tasselled in bone.
    e.append(box('cushion', (5.25, 3.25, 5.75), (10.75, 4, 10.25), 'velvet', skip=('down',)))
    tassel = box('tassel', (5, 3.25, 5.5), (5.5, 4.25, 6), 'bone', skip=('down',))
    e += [tassel, mirror_x(tassel), mirror_z(tassel), mirror_x(mirror_z(tassel))]
    return e


def lid():
    e = [box('lid_base', (2.5, 9, EAVE_FRONT), (13.5, 10, EAVE_BACK), 'iron', down='lining')]
    # Shingled slopes pitched up to the ridge, bone along the eaves.
    e.append(box('roof_front', (2.25, 10, EAVE_FRONT), (13.75, 11, EAVE_FRONT + SLOPE), 'lacquer',
                 up='shingle', down='lacquer_deep', north='bone', south='lacquer_deep',
                 rotation=spin('x', -ROOF_PITCH, (8, 10, EAVE_FRONT))))
    e.append(box('roof_back', (2.25, 10, EAVE_BACK - SLOPE), (13.75, 11, EAVE_BACK), 'lacquer',
                 up='shingle', down='lacquer_deep', south='bone', north='lacquer_deep',
                 rotation=spin('x', ROOF_PITCH, (8, 10, EAVE_BACK))))
    # Stepped gable fill, kept just under the slopes.
    rise = math.tan(math.radians(ROOF_PITCH))
    for i, top in enumerate((10.6, 11.2, 11.6)):
        inset = (top - 10) / rise + .05
        e.append(box(f'gable_{i}', (2.75, 10 + .6 * i if i else 10, EAVE_FRONT + inset),
                     (13.25, top, EAVE_BACK - inset), 'lacquer_deep', east='panel', west='panel', skip=('down',)))
    ridge = 10 + 1 / math.cos(math.radians(ROOF_PITCH)) + (EAVE_BACK - EAVE_FRONT) / 2 * rise
    mid = (EAVE_FRONT + EAVE_BACK) / 2
    e.append(box('ridge_beam', (2, ridge - .5, mid - .625), (14, ridge + .25, mid + .625), 'ferric'))
    end = box('ridge_cap', (1.5, ridge - .75, mid - .5), (2, ridge + .5, mid + .5), 'bone')
    e += [end, mirror_x(end)]
    e.append(box('finial', (7.25, ridge + .25, mid - .75), (8.75, ridge + 1, mid + .75), 'bone'))
    e.append(box('finial_knob', (7.625, ridge + 1, mid - .375), (8.375, ridge + 1.75, mid + .375), 'bone'))
    e.append(box('finial_eye', (7.5, ridge + .375, mid - .875), (8.5, ridge + .875, mid - .75), 'blood',
                 skip=('south',)))
    # Hasp hanging over the front rim.
    e.append(box('hasp', (7.25, 8, 3.25), (8.75, 10, EAVE_FRONT), 'ferric'))
    e.append(box('hasp_loop', (7.625, 8.25, 3), (8.375, 9, 3.25), 'iron', skip=('south',)))
    return e


def brain():
    """The relic's lobes; veins() inflates the same boxes into the full-bright memory overlay."""
    left = box('brain_left', (5.75, 4, 6), (7.9, 7, 10.25), 'brain', east='fissure')
    crown = box('brain_crown_left', (6.25, 7, 6.5), (7.75, 7.5, 9.75), 'brain')
    frontal = box('brain_frontal_left', (6, 4.5, 5.5), (7.75, 6.5, 6), 'brain', skip=('south',))
    e = [left, crown, frontal]
    for el in list(e):
        right = mirror_x(el)
        right['name'] = el['name'].replace('left', 'right')
        e.append(right)
    e.append(box('cerebellum', (6.5, 4, 10.25), (9.5, 5.25, 11), 'brain', skip=('north',)))
    e.append(box('brain_stem', (7.5, 3.25, 9.5), (8.5, 4, 10.5), 'brain', skip=('up',)))
    return e


def veins(lobes):
    grow = .075
    out = []
    for el in lobes:
        if el['name'] == 'brain_stem':
            continue
        v = box(el['name'].replace('brain', 'vein'), [a - grow for a in el['lo']], [b + grow for b in el['hi']],
                'veins', skip=el['skip'], extra=BRIGHT)
        out.append(v)
    return out


# --- special faces --------------------------------------------------------------------------

def grille(face, lx, ly, w, h, key):
    """Bone fretwork across the window: a diamond lattice, see-through between the bars.

    At block density the window is only a few texels across, so the lattice is a bone checker inside the iron frame.
    """
    small = w <= 8 or h <= 8
    if (lx + ly) % 2 == 0 if small else             lx in (0, w - 1) or ly in (0, h - 1) or (lx + ly) % 6 == 2 or (lx - ly) % 6 == 2:
        return clamp(PALETTE['bone'], int(noise('fret', key, face, lx, ly) * 12) - 14) + (255,)
    return (0, 0, 0, 0)


def velvet(face, lx, ly, w, h, key):
    """Crimson velvet tufted with a bone button, bone stitching round the top."""
    n = noise('velvet', key, face, lx, ly)
    if face == 'up':
        if (lx in (0, w - 1) or ly in (0, h - 1)) and (lx + ly) % 2 == 0:
            return clamp(PALETTE['bone'], -24) + (255,)
        if abs(lx - (w - 1) / 2) < 1 and abs(ly - (h - 1) / 2) < 1:
            return BONE_PALE + (255,)
    return clamp(VELVET, int(n * 10) - 5 + (6 if (lx + ly) % 2 else -6) - (16 if ly == h - 1 else 0)) + (255,)


def shingle(face, lx, ly, w, h, key):
    """Lacquered scale shingles, each course offset from the last, darker at its lower lip."""
    row = ly // 3
    n = noise('shingle', key, face, lx, ly)
    if (lx + (row % 2) * 2) % 4 == 0:
        return clamp(PALETTE['lacquer_deep'], -16) + (255,)
    if ly % 3 == 2:
        return clamp(PALETTE['lacquer_deep'], int(n * 6)) + (255,)
    return clamp(PALETTE['lacquer'], (14 if ly % 3 == 0 else 0) + int(n * 8) - 4) + (255,)


def lining(face, lx, ly, w, h, key):
    """The lid's underside: crimson lining with a band of bone inscription across its middle."""
    n = noise('lining', key, face, lx, ly)
    band = abs(ly - h / 2) < 1.5
    if band and lx % 3 != 2 and noise('glyph', key, lx // 3, ly) > .4 and 1 < lx < w - 2:
        return clamp((196, 176, 146), int(n * 10) - 5) + (255,)
    if band and abs(ly - h / 2) >= 1:
        return clamp(PALETTE['lacquer_deep'], -12) + (255,)
    return clamp(VELVET, int(n * 8) - 10) + (255,)


def sigil(face, lx, ly, w, h, key):
    """A lacquered panel bordered in bone, a witnessing eye drawn in bone at its centre."""
    n = noise('sigil', key, face, lx, ly)
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(PALETTE['bone'], int(n * 10) - 18) + (255,)
    u, v = (lx + .5 - w / 2) / (w * .36), (ly + .5 - h / 2) / (h * .2)
    lens = abs(v) - (1 - u * u)
    r = math.hypot(u * w * .36, v * h * .2)
    if abs(u) <= 1 and -.45 < lens <= 0:
        return clamp(PALETTE['bone'], int(n * 10) - 8) + (255,)
    if r < 1.1:
        return (24, 6, 10, 255)
    if r < 2.1 and lens < 0:
        return clamp(PALETTE['blood'], int(n * 16)) + (255,)
    return clamp(PALETTE['lacquer_deep'], (h / 2 - ly) / max(h, 1) * 16 + int(n * 6)) + (255,)


def brain_tex(face, lx, ly, w, h, key):
    """Preserved tissue: winding gyri cut by darker sulci, lit along each fold's crest."""
    phase = noise('gyri', key, face) * 6
    wave = math.sin(lx * .55 + phase) * 1.8 + math.sin(lx * .21 + phase * 2) * 1.2
    n = noise('tissue', key, face, lx, ly)
    fold = (ly + int(round(wave))) % 4
    if fold == 0:
        return clamp(SULCUS, int(n * 8) - 4) + (255,)
    if fold == 1:
        return clamp(TISSUE, 16 + int(n * 6)) + (255,)
    return clamp(TISSUE, int(n * 8) - 4 - (8 if fold == 3 else 0) + (6 if face == 'up' else 0)) + (255,)


def fissure(face, lx, ly, w, h, key):
    return clamp((82, 40, 44), int(noise('fissure', key, lx, ly) * 10) - 5) + (255,)


def veins_tex(face, lx, ly, w, h, key):
    """Memory veins kindling under the tissue: sparse branching crimson threads, clear between them."""
    lane = lx * 2 + ly + int(noise('trunk', key, face) * 13)
    trunk = lane % 13 == 0 and noise('run', key, face, lane // 13, ly // 4) < .5
    twig = noise('twig', key, face, lx, ly // 3) < .02
    if trunk or twig:
        return (VEIN_HOT if noise('hot', key, face, lx, ly) > .82 else VEIN_CORE) + (255,)
    return (0, 0, 0, 0)


def drip(face, lx, ly, w, h, key):
    n = noise('drip', key, face, lx, ly)
    if n > .93:
        return (166, 152, 112, 255)
    return clamp((150, 138, 102), int(n * 10) - 5 + (16 if lx == 0 else 0) - (14 if ly == h - 1 and h > 2 else 0)) \
        + (255,)


def keyhole(face, lx, ly, w, h, key):
    if face == 'north' and abs(lx - (w - 1) / 2) < 1 and 0 < ly < h - 1:
        return (24, 10, 12, 255)
    return clamp(PALETTE['ferric'], int(noise('plate', key, face, lx, ly) * 10) + (18 if ly == 0 else 0)) + (255,)


SPECIAL = dict(grille=grille, velvet=velvet, shingle=shingle, lining=lining, sigil=sigil, brain=brain_tex,
               fissure=fissure, veins=veins_tex, drip=drip, keyhole=keyhole)


# --- outputs --------------------------------------------------------------------------------

def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        variants[f'facing={facing}'] = dict({'model': 'hemomancy:block/mnemonic_reliquary'}, **({'y': y} if y else {}))
    return {'variants': variants}


def rotate_x(el, angle, origin):
    """An element turned `angle` degrees about an x axis through `origin`, folded into its own rotation."""
    out = copy.deepcopy(el)
    own = el.get('rotation', {'angle': 0, 'axis': 'x', 'origin': list(origin)})
    assert own['axis'] == 'x'
    a = math.radians(angle)
    oy, oz = own['origin'][1] - origin[1], own['origin'][2] - origin[2]
    ty = oy * math.cos(a) - oz * math.sin(a) - oy
    tz = oy * math.sin(a) + oz * math.cos(a) - oz
    for k in ('from', 'to'):
        out[k] = [out[k][0], out[k][1] + ty, out[k][2] + tz]
    out['rotation'] = {'angle': own['angle'] + angle, 'axis': 'x',
                       'origin': [own['origin'][0], own['origin'][1] + ty, own['origin'][2] + tz]}
    return out


def raised(el, dy):
    out = copy.deepcopy(el)
    out['from'][1] += dy
    out['to'][1] += dy
    if 'rotation' in out:
        out['rotation']['origin'][1] += dy
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body, top, relic = plinth() + casket(), lid(), brain()
    glow = veins(relic)
    moved = separate(body) + separate(top) + separate(relic)
    groups = {'body': body, 'lid': top, 'relic': relic, 'veins': glow}
    for name, elements in groups.items():
        for el in elements:
            el['density'] = 1   # one texel per unit: the mod's block texel density
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = 'hemomancy:block/blood_wood_planks'

    def block(elements):
        return model_json(elements, ATLAS, atlas.height, particle)

    item = model_json(item_elements((models['body'], 0), (models['lid'], 0), (models['relic'], 0)),
                      ATLAS, atlas.height, particle)
    outputs = {'block/mnemonic_reliquary': block(models['body']),
               **{f'block/mnemonic_reliquary_{part}': block(models[part]) for part in ('lid', 'relic', 'veins')},
               'item/mnemonic_reliquary': item}
    project = dict(item, elements=item['elements'] + item_elements((models['veins'], 0)))
    write(outputs, 'mnemonic_reliquary', project)
    (ASSETS / 'blockstates/mnemonic_reliquary.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    print(f'Exported Mnemonic Reliquary: {len(body)} body, {len(top)} lid, {len(relic)} relic (+{len(glow)} veins) '
          f'cubes, {moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        preview(item['elements'], ATLAS, 'Mnemonic Reliquary - closed, actual atlas', 'mnemonic_reliquary')
        opened = item_elements((models['body'], 0))
        opened += [rotate_x(el, LID_OPEN, HINGE) for el in item_elements((models['lid'], 0))]
        opened += [raised(el, RELIC_RISE) for el in item_elements((models['relic'], 0), (models['veins'], 0))]
        preview(opened, ATLAS, 'Mnemonic Reliquary - lid open, relic risen with its veins', 'mnemonic_reliquary_open')


if __name__ == '__main__':
    main()

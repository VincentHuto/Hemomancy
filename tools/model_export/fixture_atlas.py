"""Shared packer, painter and preview sheet for small static Harbinger fixtures.

Used by ossuary_clock.py and humoral_barometer.py. Elements are authored as boxes in block pixels;
`build` shelf-packs every face into one atlas and paints it procedurally. Most surfaces get one
texel per model unit; dials and other read-at-a-glance faces ask for `density=2`.

Harbinger materials only: blood-wood, hematic iron, bone, Crimson Lacquer and Mnemonic Ambergris.
Copper and its alloys are antiseptic Unstained metals, so no brass, bronze, copper or silver tones.
"""
import copy
import json
import math

import numpy as np
from PIL import Image, ImageDraw

import coplanar
from clairaudiograph import clamp, noise, render
from export_scriptorium_upgrades import ASSETS, ROOT, SOURCE, blockbench

PALETTE = dict(
    iron=(62, 60, 70), ferric=(112, 104, 114), wood=(80, 38, 40), bone=(212, 196, 166),
    lacquer=(136, 22, 36), lacquer_deep=(92, 12, 24), blood=(150, 16, 28), gristle=(168, 112, 104),
    ambergris=(94, 88, 66), cavity=(34, 14, 18), hand=(40, 34, 42))

# Faces whose transparent texels never hide a z-fight.
CUTOUT = {'glass'}

SIZES = {'north': (0, 1), 'south': (0, 1), 'east': (2, 1), 'west': (2, 1), 'up': (0, 2), 'down': (0, 2)}
CULL = {'down': (1, 0), 'up': (1, 16), 'north': (2, 0), 'south': (2, 16), 'west': (0, 0), 'east': (0, 16)}


def box(name, lo, hi, mat, skip=(), rotation=None, density=1, extra=None, **faces):
    """`extra` is merged into the emitted element, e.g. shade or neoforge_data for full-bright parts."""
    return dict(name=name, lo=list(lo), hi=list(hi), mat=mat, faces=faces, skip=set(skip),
                rotation=rotation, density=density, extra=extra or {})


def spin(axis, angle, origin):
    return dict(angle=angle, axis=axis, origin=list(origin))


def mirror_x(el):
    """The same box reflected across x = 8, for left/right pairs."""
    out = copy.deepcopy(el)
    out['lo'][0], out['hi'][0] = 16 - el['hi'][0], 16 - el['lo'][0]
    swap = {'east': 'west', 'west': 'east'}
    out['skip'] = {swap.get(s, s) for s in el['skip']}
    out['faces'] = {swap.get(k, k): v for k, v in el['faces'].items()}
    if el['rotation']:
        out['rotation'] = dict(el['rotation'], origin=[16 - el['rotation']['origin'][0]] + el['rotation']['origin'][1:])
        if el['rotation']['axis'] != 'x':
            out['rotation']['angle'] = -el['rotation']['angle']
    return out


def separate(elements):
    """Push coplanar overlapping faces apart (z-fighting) before the atlas is packed."""
    views = []
    for i, el in enumerate(elements):
        faces = {f: {'texture': el['faces'].get(f, el['mat']), 'uv': [i, 0, i + 1, 1]}
                 for f in SIZES if f not in el['skip']}
        view = {'from': el['lo'], 'to': el['hi'], 'faces': faces}
        if el['rotation']:
            view['rotation'] = el['rotation']
        views.append(view)
    return coplanar.resolve(views, translucent=CUTOUT)


def split_storeys(elements, at=16):
    """Cut a two-block-tall fixture into lower and upper block models, dropping the internal seam faces."""
    lower, upper = [], []
    for el in elements:
        lo_y, hi_y = el['lo'][1], el['hi'][1]
        if el['rotation'] or hi_y <= at or lo_y >= at:
            centre = (lo_y + hi_y) / 2 if not el['rotation'] else el['rotation']['origin'][1]
            target = lower if centre < at else upper
            part = copy.deepcopy(el)
            if target is upper:
                part['lo'][1] -= at
                part['hi'][1] -= at
                if part['rotation']:
                    part['rotation']['origin'][1] -= at
            target.append(part)
            continue
        bottom, top = copy.deepcopy(el), copy.deepcopy(el)
        bottom['hi'][1] = at
        bottom['skip'].add('up')
        top['lo'][1], top['hi'][1] = 0, hi_y - at
        top['skip'].add('down')
        lower.append(bottom)
        upper.append(top)
    return lower, upper


def faces_of(el):
    size = [b - a for a, b in zip(el['lo'], el['hi'])]
    d = el['density']
    for face, (u, v) in SIZES.items():
        if face not in el['skip']:
            yield face, max(1, math.ceil(size[u] * d - 1e-6)), max(1, math.ceil(size[v] * d - 1e-6)), \
                el['faces'].get(face, el['mat'])


def paint(mat, face, lx, ly, w, h, key, special):
    """One texel of a material. lx/ly run across the packed face; ly = 0 is the face's top edge."""
    if mat in special:
        return special[mat](face, lx, ly, w, h, key)
    n = noise(mat, face, lx, ly, key)
    top, bottom = ly == 0, ly == h - 1
    edge = lx in (0, w - 1) or ly in (0, h - 1)
    big = w >= 3 and h >= 3
    if mat == 'glass':
        # Clear pane: one short glint in the upper corner of the outward face, the rest see-through.
        if face == 'north' and 1 <= ly <= 2 and lx + ly == w - 2:
            return (214, 226, 234, 255)
        return (0, 0, 0, 0)
    base = PALETTE.get(mat, (255, 0, 255))
    if mat == 'iron':
        rgb = clamp(base, int(n * 12) - 6 + (18 if top and big else 0) - (14 if bottom and big else 0))
        if noise('vein', key, face, lx, ly // 2) < .05:
            rgb = (98, 24, 34)
    elif mat == 'wood':
        grain = ly if w >= h else lx
        rgb = clamp(base, int(noise('grain', key, face, grain) * 18) - 9 + int(n * 6) - (12 if edge and big else 0))
        if n < .02:
            rgb = (112, 30, 38)
        elif n > .97:
            rgb = (56, 24, 28)
    elif mat == 'ferric':
        # Polished hematic iron: a cold sheen on top with blood showing through the grain.
        rgb = clamp(base, int(n * 10) - 5 + (28 if top and h > 1 else 0) - (24 if bottom and h > 2 else 0))
        if noise('vein', key, face, lx, ly) < .035:
            rgb = (138, 34, 46)
    elif mat in ('bone', 'gristle'):
        rgb = clamp(base, int(n * 12) - 6 - (18 if edge and big else 0))
        if noise('stain', key, face, lx // 2, ly) < .08:
            rgb = (176, 128, 112) if mat == 'bone' else (132, 72, 70)
        elif n > .95:
            rgb = clamp(base, -44)
    elif mat in ('lacquer', 'lacquer_deep'):
        # Crimson Lacquer: a wet highlight along the top edge, darkening down each face.
        slope = (h / 2 - ly) / max(h, 1) * 20 if h > 2 else 0
        rgb = clamp(base, slope + int(n * 6) - 3 - (14 if edge and big else 0))
        if top and h > 1 and face not in ('up', 'down') and mat == 'lacquer':
            rgb = clamp(base, 40)
        elif n > .985:
            rgb = clamp(base, 30)
    elif mat == 'blood':
        rgb = clamp(base, (40 if top else -40 if bottom else 0) + int(n * 10))
    elif mat == 'ambergris':
        # Mnemonic Ambergris: grey-olive wax with pale nacre flecks.
        rgb = clamp(base, int(n * 18) - 9 + (16 if top else 0))
        if n > .9:
            rgb = (140, 127, 85)
        elif n < .08:
            rgb = (57, 58, 44)
    elif mat == 'chain':
        rgb = PALETTE['ferric'] if ly % 2 == 0 else (40, 34, 42)
    elif mat == 'hand':
        rgb = (150, 18, 28) if top and face == 'north' else clamp(base, int(n * 8))
    elif mat == 'cavity':
        # The case interior: blackened blood-wood boards, a pale mycelial thread creeping up one seam.
        rgb = clamp(base, int(noise('board', key, lx // 2) * 10) - (8 if lx % 2 == 0 else 0) + int(n * 4))
        if lx == w // 3 and noise('thread', key, ly // 3) < .55:
            rgb = (148, 120, 112)
        elif noise('vein', key, lx, ly) < .02:
            rgb = (98, 24, 34)
    elif mat == 'panel':
        # A lacquered inset bordered in bone.
        if edge:
            rgb = clamp(PALETTE['bone'], int(n * 10) - 18)
        else:
            rgb = clamp(PALETTE['lacquer_deep'], (h / 2 - ly) / max(h, 1) * 16 + int(n * 6))
    elif mat == 'fret':
        # Bone lattice fretwork over a lacquered ground.
        if edge or (lx + ly) % 4 == 1 or (lx - ly) % 4 == 1:
            rgb = clamp(PALETTE['bone'], int(n * 10) - (24 if edge else 10))
        else:
            rgb = clamp(PALETTE['lacquer_deep'], -20 + int(n * 6))
    else:
        rgb = clamp(base, int(n * 8) - 4 + (16 if top else 0))
    return rgb + (255,)


def build(groups, special=None, atlas_w=128):
    """Shelf-pack every face of every group into one atlas, paint it, and emit Java model elements."""
    special = special or {}
    requests = []
    for g, elements in groups.items():
        for i, el in enumerate(elements):
            for face, w, h, mat in faces_of(el):
                requests.append(((g, i, face), w, h, mat))
    requests.sort(key=lambda r: (-r[2], -r[1]))
    placed, x, y, shelf = {}, 0, 0, 0
    for key, w, h, mat in requests:
        if x + w > atlas_w:
            x, y, shelf = 0, y + shelf, 0
        placed[key] = (x, y, w, h, mat)
        x, shelf = x + w, max(shelf, h)
    height = 1 << max(4, math.ceil(math.log2(y + shelf)))
    atlas = Image.new('RGBA', (atlas_w, height), (0, 0, 0, 0))
    pixels = atlas.load()
    for (g, i, face), (x, y, w, h, mat) in placed.items():
        for ly in range(h):
            for lx in range(w):
                pixels[x + lx, y + ly] = paint(mat, face, lx, ly, w, h, (g, i), special)
    models = {}
    for g, elements in groups.items():
        out = []
        for i, el in enumerate(elements):
            faces = {}
            for face, w, h, _ in faces_of(el):
                x, y, _, _, _ = placed[(g, i, face)]
                uv = [x * 16 / atlas_w, y * 16 / height, (x + w) * 16 / atlas_w, (y + h) * 16 / height]
                faces[face] = {'uv': [round(v, 5) for v in uv], 'texture': '#atlas'}
                axis, value = CULL[face]
                bound = el['lo'][axis] if face in ('down', 'north', 'west') else el['hi'][axis]
                if bound == value and not el['rotation']:
                    faces[face]['cullface'] = face
            json_el = {'name': el['name'], 'from': [round(v, 4) for v in el['lo']],
                       'to': [round(v, 4) for v in el['hi']]}
            if el['rotation']:
                json_el['rotation'] = el['rotation']
            json_el['faces'] = faces
            json_el.update(el.get('extra', {}))
            out.append(json_el)
        models[g] = out
    return atlas, models


def model_json(elements, atlas, height, particle, display=None):
    model = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False,
             'texture_size': [128, height], 'textures': {'atlas': f'hemomancy:block/{atlas}', 'particle': particle},
             'elements': elements}
    if display:
        model['display'] = display
    return model


def item_elements(*groups):
    """Concatenate block groups (each with its y offset) into one cull-free item element list."""
    out = []
    for elements, dy in groups:
        for el in copy.deepcopy(elements):
            el['from'][1] += dy
            el['to'][1] += dy
            if 'rotation' in el:
                el['rotation']['origin'][1] += dy
            for f in el['faces'].values():
                f.pop('cullface', None)
            out.append(el)
    return out


def write(outputs, project_name, project_model):
    for name, model in outputs.items():
        (ASSETS / 'models' / (name + '.json')).write_text(json.dumps(model, indent=2) + '\n')
    project = blockbench(project_name, dict(project_model, ambientocclusion=False))
    (SOURCE / f'{project_name}.bbmodel').write_text(json.dumps(project, indent=2) + '\n')


def preview(elements, atlas, title, out_name, lift=0, size=360, scale=15):
    """Textured turnaround sheet in build/model-previews/; `lift` recentres tall fixtures."""
    texture = np.array(Image.open(ASSETS / f'textures/block/{atlas}.png').convert('RGBA'))
    model = copy.deepcopy(elements)
    for el in model:
        el['from'][1] -= lift
        el['to'][1] -= lift
        if 'rotation' in el:
            el['rotation']['origin'][1] -= lift
    views = [(0, 10), (30, 25), (-35, 25), (90, 10), (180, 25), (145, 30), (-60, 55), (15, -20)]
    tiles = [render(model, {'atlas': texture}, yaw, pitch, size, scale) for yaw, pitch in views]
    sheet = Image.new('RGBA', (size * 4, size * 2 + 30), (30, 24, 28, 255))
    for i, t in enumerate(tiles):
        sheet.paste(t, (size * (i % 4), 30 + size * (i // 4)))
    ImageDraw.Draw(sheet).text((10, 8), title, fill=(220, 200, 200))
    out = ROOT / f'build/model-previews/{out_name}.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(out)
    print(out)
    return out

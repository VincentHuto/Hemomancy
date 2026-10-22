"""Author the Clairaudiograph: a cylinder phonograph-lathe married to a clinical blood sampler.

Harbinger materials only: blood-wood, hematic iron, bone and Crimson Lacquer. Copper and its
alloys are antiseptic Unstained metals (and silver is consecrated copper), so no brass, bronze,
copper or silver tones belong on this machine.

Emits the block, carver-stylus, feed and inventory models, a painted one-texel-per-unit atlas,
the mounted wax-cylinder textures and an editable Blockbench project. Python 3 + Pillow + NumPy.
`--preview` also writes a textured turnaround to build/model-previews/clairaudiograph.png.

Coordinates are block pixels for the north-facing state; the front (player side) is z = 0.
ClairaudiographRenderer reads the anchors below, so keep its constants in step when moving them.
"""
import argparse
import copy
import hashlib
import json
import math

import numpy as np
from PIL import Image, ImageDraw

from export_scriptorium_upgrades import ASSETS, MODELS, ROOT, SOURCE, blockbench

ATLAS = 'model_clairaudiograph'
ATLAS_W = 128

# Renderer anchors (block pixels). Mirror these in ClairaudiographRenderer.
CYLINDER = (8, 7.25, 5.5)          # wax cylinder axis centre; the cylinder spins about +x
STYLUS_REST_X, STYLUS_TRAVEL = 4, 8  # carver needle x at rest, and how far it walks while carving
CHAMBER = (1.5, 8.25, 10, 5, 13.25, 13.5)  # sampler glass chamber box
FEED = ((1, 7.875, 10.5), (1, 7.875, 6.25))  # blood line the feed bolus travels along

PALETTE = dict(
    iron=(62, 60, 70), ferric=(112, 104, 114), wood=(80, 38, 40), bone=(212, 196, 166),
    lacquer=(136, 22, 36), lacquer_deep=(92, 12, 24), throat=(28, 6, 10), rubber=(84, 30, 34),
    blood=(150, 16, 28), wax=(226, 206, 160), gauge=(226, 214, 186))


def box(name, lo, hi, mat, skip=(), rotation=None, **faces):
    return dict(name=name, lo=list(lo), hi=list(hi), mat=mat, faces=faces, skip=set(skip), rotation=rotation)


def ring(name, cx, cy, half, wall, z0, z1, front='lacquer_deep', outer='lacquer'):
    """Four walls of a square horn section on the z axis: lacquered outside, deeper lacquer inside."""
    x0, x1, y0, y1 = cx - half, cx + half, cy - half, cy + half
    common = dict(north=front, south=outer)
    return [
        box(f'{name}_top', (x0, y1 - wall, z0), (x1, y1, z1), outer, down='lacquer_deep', **common),
        box(f'{name}_bottom', (x0, y0, z0), (x1, y0 + wall, z1), outer, up='lacquer_deep', **common),
        box(f'{name}_left', (x0, y0 + wall, z0), (x0 + wall, y1 - wall, z1), outer, east='lacquer_deep', **common),
        box(f'{name}_right', (x1 - wall, y0 + wall, z0), (x1, y1 - wall, z1), outer, west='lacquer_deep', **common),
    ]


def octagon(name, centre, half, thick, axis, mat, lo_axis, hi_axis, **faces):
    """A squat octagonal disc: an axis-aligned square plus the same square turned 45 degrees."""
    out = []
    for turn in (0, 45):
        lo, hi = [0, 0, 0], [0, 0, 0]
        for i in range(3):
            if 'xyz'[i] == axis:
                lo[i], hi[i] = lo_axis, hi_axis
            else:
                lo[i], hi[i] = centre[i] - half, centre[i] + half
        rot = dict(angle=45, axis=axis, origin=list(centre)) if turn else None
        out.append(box(f'{name}_{turn}', lo, hi, mat, rotation=rot, **faces))
    return out


def machine():
    e = []
    # Blood-wood cabinet on four iron feet, capped with an overhanging iron bed plate.
    for x, z in [(1, 1.5), (13, 1.5), (1, 12.5), (13, 12.5)]:
        e.append(box('foot', (x, 0, z), (x + 2, 1, z + 2), 'iron'))
    e.append(box('cabinet', (1.5, 1, 2), (14.5, 3.25, 14), 'wood', skip=('down', 'up')))
    e.append(box('bed_plate', (1, 3.25, 1.5), (15, 3.75, 14.5), 'iron'))
    e.append(box('nameplate', (6, 1.75, 1.75), (10, 2.75, 2), 'bone'))
    e.append(box('drawer_pull', (7.5, 1.25, 1.5), (8.5, 1.5, 1.75), 'bone'))

    # Lathe gantry: two cast uprights carry the mandrel bearings and the carver's rails.
    for side, (x0, x1) in (('left', (1.5, 2.5)), ('right', (13.5, 14.5))):
        e.append(box(f'{side}_upright', (x0, 3.75, 4.75), (x1, 12.5, 6.25), 'iron'))
        e.append(box(f'{side}_upright_foot', (x0 - .25, 3.75, 3.25), (x1 + .25, 4.75, 7.75), 'iron'))
        e.append(box(f'{side}_bearing', (x0 - .25, 6.25, 4.5), (x1 + .25, 8.25, 6.5), 'iron'))
        e.append(box(f'{side}_rail_cap', (x0 - .25, 12.5, 4.5), (x1 + .25, 13.25, 8.25), 'iron'))
        e.append(box(f'{side}_rail_post', (x0, 10.5, 7), (x1, 12.5, 8), 'iron'))
    e.append(box('axle_stub', (.75, 6.75, 5), (1.25, 7.75, 6), 'ferric'))
    e.append(box('lead_screw', (2.5, 11.25, 5.25), (13.5, 11.75, 5.75), 'screw', skip=('east', 'west')))
    e.append(box('guide_rod', (2.5, 11.25, 7.25), (13.5, 11.75, 7.75), 'ferric', skip=('east', 'west')))

    # Shavings tray under the mandrel, with two curls of carved wax.
    e.append(box('tray', (3, 3.75, 3.5), (13, 4, 7.5), 'iron', skip=('down',)))
    e.append(box('tray_lip_front', (3, 3.75, 3.25), (13, 4.5, 3.5), 'ferric', skip=('down',)))
    e.append(box('tray_lip_back', (3, 3.75, 7.5), (13, 4.5, 7.75), 'ferric', skip=('down',)))
    e.append(box('wax_curl', (5, 4, 4.5), (5.75, 4.25, 5), 'wax', skip=('down',)))
    e.append(box('wax_curl', (9.5, 4, 6), (10, 4.25, 6.75), 'wax', skip=('down',)))

    # Handwheel on the right bearing.
    cy, cz = CYLINDER[1], CYLINDER[2]
    e.append(box('wheel_hub', (14.5, cy - .5, cz - .5), (15.75, cy + .5, cz + .5), 'iron'))
    for turn in (0, 45):
        rot = dict(angle=45, axis='x', origin=[15.125, cy, cz]) if turn else None
        for name, lo, hi in [('rim_top', (14.75, cy + 1.75, cz - 1), (15.5, cy + 2.5, cz + 1)),
                             ('rim_bottom', (14.75, cy - 2.5, cz - 1), (15.5, cy - 1.75, cz + 1)),
                             ('rim_front', (14.75, cy - 1, cz - 2.5), (15.5, cy + 1, cz - 1.75)),
                             ('rim_back', (14.75, cy - 1, cz + 1.75), (15.5, cy + 1, cz + 2.5))]:
            e.append(box(f'wheel_{name}_{turn}', lo, hi, 'ferric', rotation=rot))
    e.append(box('wheel_spoke_v', (14.875, cy - 1.75, cz - .25), (15.375, cy + 1.75, cz + .25), 'iron'))
    e.append(box('wheel_spoke_h', (14.875, cy - .25, cz - 1.75), (15.375, cy + .25, cz + 1.75), 'iron'))
    e.append(box('wheel_knob', (15.5, cy + 1.5, cz - 2.25), (16, cy + 2.25, cz - 1.5), 'bone'))

    # Crimson-lacquered horn behind the gantry, lipped in bone, its throat descending into the cabinet.
    hx, hy = 9.75, 11.75
    # An exponential bell: each section steps in half a pixel and runs longer toward the throat.
    e += ring('horn_lip', hx, hy, 4.25, .5, 8.75, 9.25, front='bone', outer='bone')
    z = 9.25
    for i, (half, length) in enumerate([(3.75, .5), (3.25, .75), (2.75, 1), (2.25, 1.25), (1.75, 1.5)]):
        e += ring(f'horn_section{i}', hx, hy, half, .5, z, z + length)
        z += length
    e.append(box('horn_throat', (hx - 1.25, hy - 1.25, z), (hx + 1.25, hy + 1.25, 15), 'lacquer', north='throat'))
    e.append(box('horn_pipe', (hx - 1, 3.75, 13.5), (hx + 1, hy - 1.25, 15), 'lacquer', skip=('down',)))
    for y in (5, 8.5):
        e.append(box('horn_collar', (hx - 1.25, y, 13.25), (hx + 1.25, y + .5, 15.25), 'iron'))

    # Blood sampler: an iron pedestal, a glass chamber pierced by a plunger lancet, a pump bulb.
    x0, y0, z0, x1, y1, z1 = CHAMBER
    e.append(box('sampler_pedestal', (1.75, 3.75, 10.25), (4.75, 7.5, 13.25), 'iron', skip=('down',)))
    e.append(box('sampler_collar', (x0 - .25, 7.5, z0 - .25), (x1 + .25, y0, z1 + .25), 'ferric'))
    e.append(box('sampler_glass', (x0, y0, z0), (x1, y1, z1), 'glass', skip=('down', 'up')))
    for px, pz in [(x0 - .25, z0 - .25), (x1 - .25, z0 - .25), (x0 - .25, z1 - .25), (x1 - .25, z1 - .25)]:
        e.append(box('sampler_post', (px, y0, pz), (px + .5, y1, pz + .5), 'iron'))
    e.append(box('sampler_cap', (x0 - .25, y1, z0 - .25), (x1 + .25, y1 + .75, z1 + .25), 'ferric'))
    e.append(box('sampler_dome', (x0 + .5, y1 + .75, z0 + .5), (x1 - .5, y1 + 1.25, z1 - .5), 'iron'))
    mx, mz = (x0 + x1) / 2, (z0 + z1) / 2
    e.append(box('plunger_rod', (mx - .25, y1 + 1.25, mz - .25), (mx + .25, 15.75, mz + .25), 'ferric'))
    e.append(box('plunger_handle', (mx - 1.5, 15.75, mz - .375), (mx + 1.5, 16, mz + .375), 'bone'))
    e.append(box('lancet', (mx - .125, 11.75, mz - .125), (mx + .125, y1, mz + .125), 'bone'))
    e.append(box('pump_bulb', (2, 4.25, 13.25), (4, 6.75, 14.75), 'rubber'))
    e.append(box('pump_nozzle', (2.75, 6.75, 13.25), (3.25, 7.5, 13.75), 'iron'))
    e.append(box('pressure_gauge', (1.5, 4.25, 10.25), (1.75, 7.25, 13.25), 'iron', west='gauge'))

    # Feed line from the sampler into the left bearing.
    (fx, fy, fz0), (_, _, fz1) = FEED
    e.append(box('feed_line', (fx - .25, fy - .25, fz1), (fx + .25, fy + .25, fz0), 'blood_tube'))
    e.append(box('feed_elbow_front', (fx - .25, fy - .25, fz1 - .25), (1.5, fy + .25, fz1 + .25), 'ferric'))
    e.append(box('feed_elbow_back', (fx - .25, fy - .25, fz0 - .25), (1.5, fy + .25, fz0 + .25), 'ferric'))

    # Cylinder case under the horn mouth.
    e.append(box('case', (11.25, 3.75, 9.75), (14.75, 6.25, 14.25), 'wood', skip=('down',)))
    e.append(box('case_lid', (11, 6.25, 9.5), (15, 6.75, 14.5), 'wood'))
    e.append(box('case_latch', (12.5, 5.25, 9.5), (13.5, 6.25, 9.75), 'bone'))
    return e


def stylus():
    """Carver/reproducer carriage at rest; the renderer slides it along +x while carving."""
    x = STYLUS_REST_X
    e = [box('carriage', (x - 1, 10.75, 4.75), (x + 1, 12.25, 8.25), 'iron'),
         box('carriage_knob', (x - .375, 12.25, 6.125), (x + .375, 12.75, 6.875), 'bone'),
         box('carriage_band', (x - 1.125, 11.25, 4.625), (x + 1.125, 11.75, 8.375), 'ferric')]
    e += octagon('reproducer', (x, 10.375, CYLINDER[2]), 1.25, .75, 'y', 'ferric', 10, 10.75, down='bone')
    e.append(box('needle', (x - .125, 9.75, CYLINDER[2] - .125), (x + .125, 10, CYLINDER[2] + .125), 'bone'))
    return e


def feed():
    return [box('blood_bolus', (-.375, -.375, -.375), (.375, .375, .375), 'blood')]


# --- texture ------------------------------------------------------------------------------

def noise(*values):
    digest = hashlib.blake2s(repr(values).encode(), digest_size=2).digest()
    return int.from_bytes(digest, 'big') / 65535


def clamp(rgb, shift=0):
    return tuple(max(0, min(255, int(c + shift))) for c in rgb)


def paint(mat, face, lx, ly, w, h, key):
    n = noise(mat, face, lx, ly, key)
    top, bottom = ly == 0, ly == h - 1
    edge = lx in (0, w - 1) or ly in (0, h - 1)
    big = w >= 3 and h >= 3
    if mat == 'glass':
        if (lx + ly) % 5 == 1 and 0 < lx < w - 1 and 0 < ly < h - 1 and n > .35:
            return (236, 244, 248, 255)
        if (top or bottom) and face not in ('up', 'down'):
            return (198, 214, 222, 255) if n > .2 else (168, 186, 196, 255)
        return (0, 0, 0, 0)
    if mat == 'screw':
        return clamp(PALETTE['ferric'], 24 if top else 0) + (255,) if lx % 2 == 0 else (40, 34, 42, 255)
    base = PALETTE.get(mat, PALETTE['blood'] if mat == 'blood_tube' else (255, 0, 255))
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
    elif mat == 'bone':
        rgb = clamp(base, int(n * 12) - 6 - (18 if edge and big else 0))
        if noise('stain', key, face, lx // 2, ly) < .08:
            rgb = (176, 128, 112)
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
    elif mat == 'throat':
        rgb = clamp(base, 0 if edge else -16)
    elif mat == 'rubber':
        rgb = clamp(base, int(n * 10) - 5 + (22 if (lx, ly) == (1, 1) else 0))
    elif mat in ('blood', 'blood_tube'):
        rgb = clamp(base, (40 if top else -40 if bottom else 0) + int(n * 10))
        if mat == 'blood' and (lx, ly) == (0, 0):
            rgb = (226, 92, 98)
    elif mat == 'gauge':
        # Three by three: iron corners round off a bone dial with a crimson needle.
        rgb = clamp(base, int(n * 6))
        if lx in (0, w - 1) and ly in (0, h - 1):
            rgb = PALETTE['iron']
        elif lx == w // 2 and ly <= h // 2:
            rgb = (150, 18, 28) if ly < h // 2 else (40, 20, 22)
    else:
        rgb = clamp(base, int(n * 8) - 4 + (16 if top else 0))
    return rgb + (255,)


SIZES = {'north': (0, 1), 'south': (0, 1), 'east': (2, 1), 'west': (2, 1), 'up': (0, 2), 'down': (0, 2)}


def faces_of(el):
    size = [b - a for a, b in zip(el['lo'], el['hi'])]
    for face, (u, v) in SIZES.items():
        if face not in el['skip']:
            yield face, size[u], size[v], el['faces'].get(face, el['mat'])


def build(groups):
    """Shelf-pack every face into one atlas at one texel per model unit, then paint it."""
    requests = []
    for g, elements in groups.items():
        for i, el in enumerate(elements):
            for face, w, h, mat in faces_of(el):
                requests.append(((g, i, face), max(1, math.ceil(w)), max(1, math.ceil(h)), mat))
    requests.sort(key=lambda r: (-r[2], -r[1]))
    placed, x, y, shelf = {}, 0, 0, 0
    for key, w, h, mat in requests:
        if x + w > ATLAS_W:
            x, y, shelf = 0, y + shelf, 0
        placed[key] = (x, y, w, h, mat)
        x, shelf = x + w, max(shelf, h)
    height = 1 << max(4, math.ceil(math.log2(y + shelf)))
    atlas = Image.new('RGBA', (ATLAS_W, height), (0, 0, 0, 0))
    pixels = atlas.load()
    for (g, i, face), (x, y, w, h, mat) in placed.items():
        for ly in range(h):
            for lx in range(w):
                pixels[x + lx, y + ly] = paint(mat, face, lx, ly, w, h, (g, i))
    models = {}
    for g, elements in groups.items():
        out = []
        for i, el in enumerate(elements):
            faces = {}
            for face, w, h, _ in faces_of(el):
                x, y, _, _, _ = placed[(g, i, face)]
                uv = [x * 16 / ATLAS_W, y * 16 / height, (x + w) * 16 / ATLAS_W, (y + h) * 16 / height]
                faces[face] = {'uv': [round(v, 5) for v in uv], 'texture': '#atlas'}
                axis, value = {'down': (1, 0), 'up': (1, 16), 'north': (2, 0), 'south': (2, 16),
                               'west': (0, 0), 'east': (0, 16)}[face]
                bound = el['lo'][axis] if face in ('down', 'north', 'west') else el['hi'][axis]
                if bound == value and not el['rotation']:
                    faces[face]['cullface'] = face
            json_el = {'name': el['name'], 'from': el['lo'], 'to': el['hi']}
            if el['rotation']:
                json_el['rotation'] = el['rotation']
            json_el['faces'] = faces
            out.append(json_el)
        models[g] = out
    return atlas, models


def model_json(elements, height, particle=True):
    textures = {'atlas': f'hemomancy:block/{ATLAS}'}
    if particle:
        textures['particle'] = 'hemomancy:block/hematic_iron_block'
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False,
            'texture_size': [ATLAS_W, height], 'textures': textures, 'elements': elements}


def mounted_wax_textures():
    """Recolour the ambergris cylinder mesh texture into the pale wax the machine mounts."""
    for suffix in ('', '_recorded'):
        src = Image.open(ASSETS / f'textures/item/ambergris_cylinder{suffix}.png').convert('RGBA')
        out = src.copy()
        px = out.load()
        for y in range(src.height):
            for x in range(src.width):
                r, g, b, a = px[x, y]
                if y < 16 and not (r > 110 and g < 60):      # wax body, leaving carved grooves crimson
                    shade = (r + g + b) / 3 - 150
                    px[x, y] = clamp((214, 196, 150), shade * .6 + (noise('wax', x, y) * 8 - 4)) + (a,)
                elif y >= 16:                               # mandrel end caps: blackened iron
                    shade = (r + g + b) / 3 - 70
                    px[x, y] = clamp((58, 50, 52), shade * .5) + (a,)
        out.save(ASSETS / f'textures/item/wax_cylinder_mounted{suffix}.png')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    atlas, models = build({'block': machine(), 'stylus': stylus(), 'feed': feed()})
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    block = model_json(models['block'], atlas.height)
    outputs = {
        'block/clairaudiograph': block,
        'block/clairaudiograph_stylus': model_json(models['stylus'], atlas.height, False),
        'block/clairaudiograph_feed': model_json(models['feed'], atlas.height, False),
    }
    item = copy.deepcopy(block)
    item['elements'] = copy.deepcopy(models['block']) + copy.deepcopy(models['stylus'])
    for el in item['elements']:
        for f in el['faces'].values():
            f.pop('cullface', None)
    outputs['item/clairaudiograph'] = item
    for name, model in outputs.items():
        (ASSETS / 'models' / (name + '.json')).write_text(json.dumps(model, indent=2) + '\n')
    project = blockbench('clairaudiograph', dict(item, ambientocclusion=False))
    (SOURCE / 'clairaudiograph.bbmodel').write_text(json.dumps(project, indent=2) + '\n')
    mounted_wax_textures()
    print(f'Exported Clairaudiograph: {len(models["block"])} block cubes, {len(models["stylus"])} stylus cubes, '
          f'{ATLAS_W}x{atlas.height} atlas')
    if args.preview:
        preview(outputs)


# --- preview ------------------------------------------------------------------------------

def preview_extras():
    """Stand-ins for what ClairaudiographRenderer draws: the mounted cylinder and a sample vial."""
    cx, cy, cz = CYLINDER
    wax = {f: {'uv': [0, 0, 8, 2], 'texture': '#cyl'} for f in SIZES}
    caps = {f: {'uv': [0, 9, 2, 11], 'texture': '#cyl'} for f in SIZES}
    vial = {f: {'uv': [0, 0, 16, 16], 'texture': '#vial'} for f in SIZES}
    x0, y0, z0, x1, y1, z1 = CHAMBER
    mx, mz = (x0 + x1) / 2, (z0 + z1) / 2
    return [
        {'from': [cx - 5, cy - 2, cz - 2], 'to': [cx + 5, cy + 2, cz + 2], 'faces': wax},
        {'from': [cx - 4, cy - 2.7, cz - 1.4], 'to': [cx + 4, cy + 2.7, cz + 1.4], 'faces': wax},
        {'from': [cx - 4, cy - 1.4, cz - 2.7], 'to': [cx + 4, cy + 1.4, cz + 2.7], 'faces': wax},
        {'from': [cx - 5.5, cy - 2.5, cz - 2.5], 'to': [cx - 4.5, cy + 2.5, cz + 2.5], 'faces': caps},
        {'from': [cx + 4.5, cy - 2.5, cz - 2.5], 'to': [cx + 5.5, cy + 2.5, cz + 2.5], 'faces': caps},
        {'from': [mx - .75, y0 + .25, mz - .75], 'to': [mx + .75, y0 + 3.25, mz + .75], 'faces': vial},
    ]


def render(model, textures, yaw, pitch=32, size=360, scale=15):
    canvas = np.zeros((size, size, 4), dtype=float)
    canvas[:] = [30, 24, 28, 255]
    depth = np.full((size, size), -np.inf)
    a, p = math.radians(yaw), math.radians(pitch)
    eye = np.array([math.sin(a) * math.cos(p), math.sin(p), -math.cos(a) * math.cos(p)])
    right = np.cross([0, 1, 0], eye); right /= np.linalg.norm(right)
    up = np.cross(eye, right)
    faces_idx = {'north': [0, 1, 3, 2], 'south': [5, 4, 6, 7], 'west': [4, 0, 2, 6],
                 'east': [1, 5, 7, 3], 'up': [2, 3, 7, 6], 'down': [4, 5, 1, 0]}
    shades = {'north': .8, 'south': .8, 'west': .6, 'east': .6, 'up': 1, 'down': .5}
    for e in model:
        lo, hi = e['from'], e['to']
        pts = np.array([[x, y, z] for z in (lo[2], hi[2]) for y in (lo[1], hi[1]) for x in (lo[0], hi[0])], dtype=float)
        if 'rotation' in e:
            rot = e['rotation']; origin = np.array(rot['origin'])
            ax = [(1, 2), (2, 0), (0, 1)]['xyz'.index(rot['axis'])]
            ang = math.radians(rot['angle'])
            pts -= origin
            v, w = pts[:, ax[0]].copy(), pts[:, ax[1]].copy()
            pts[:, ax[0]] = v * math.cos(ang) - w * math.sin(ang)
            pts[:, ax[1]] = v * math.sin(ang) + w * math.cos(ang)
            pts += origin
        c = pts - [8, 8, 8]
        proj = np.stack([size / 2 + c @ right * scale, size / 2 - c @ up * scale, c @ eye], axis=1)
        for face, f in e['faces'].items():
            q = proj[faces_idx[face]]
            tex = textures[f['texture'][1:]]
            u0, v0, u1, v1 = f['uv']
            uv = np.array([[u0, v1], [u1, v1], [u1, v0], [u0, v0]]) / 16
            for ids in ([0, 1, 2], [0, 2, 3]):
                t, tuv = q[ids], uv[ids]
                xmin, ymin = np.maximum(np.floor(t[:, :2].min(0)).astype(int), 0)
                xmax, ymax = np.minimum(np.ceil(t[:, :2].max(0)).astype(int), size - 1)
                if xmax < xmin or ymax < ymin:
                    continue
                xx, yy = np.meshgrid(np.arange(xmin, xmax + 1) + .5, np.arange(ymin, ymax + 1) + .5)
                den = (t[1, 1] - t[2, 1]) * (t[0, 0] - t[2, 0]) + (t[2, 0] - t[1, 0]) * (t[0, 1] - t[2, 1])
                if abs(den) < 1e-8:
                    continue
                b0 = ((t[1, 1] - t[2, 1]) * (xx - t[2, 0]) + (t[2, 0] - t[1, 0]) * (yy - t[2, 1])) / den
                b1 = ((t[2, 1] - t[0, 1]) * (xx - t[2, 0]) + (t[0, 0] - t[2, 0]) * (yy - t[2, 1])) / den
                b2 = 1 - b0 - b1
                z = b0 * t[0, 2] + b1 * t[1, 2] + b2 * t[2, 2]
                u = b0 * tuv[0, 0] + b1 * tuv[1, 0] + b2 * tuv[2, 0]
                v = b0 * tuv[0, 1] + b1 * tuv[1, 1] + b2 * tuv[2, 1]
                px = tex[np.clip((v * tex.shape[0]).astype(int), 0, tex.shape[0] - 1),
                         np.clip((u * tex.shape[1]).astype(int), 0, tex.shape[1] - 1)].astype(float)
                region = np.s_[ymin:ymax + 1, xmin:xmax + 1]
                eps = 1e-6
                mask = (b0 >= -eps) & (b1 >= -eps) & (b2 >= -eps) & (z > depth[region]) & (px[:, :, 3] > 0)
                px[:, :, :3] *= shades[face]
                canvas[region][mask] = px[mask]
                depth[region][mask] = z[mask]
    return Image.fromarray(canvas.astype('uint8'), 'RGBA')


def preview(outputs):
    def load(resource, crop=True):
        im = Image.open(ASSETS / 'textures' / (resource + '.png')).convert('RGBA')
        return np.array(im.crop((0, 0, im.width, im.width)) if crop else im)
    textures = {'atlas': load(f'block/{ATLAS}', False), 'cyl': load('item/wax_cylinder_mounted_recorded'),
                'vial': load('block/blood_still')}
    model = outputs['item/clairaudiograph']['elements'] + preview_extras()
    views = [(0, 25), (35, 30), (-40, 30), (90, 15), (180, 30), (140, 35), (-120, 30), (20, 70)]
    tiles = [render(model, textures, yaw, pitch) for yaw, pitch in views]
    sheet = Image.new('RGBA', (360 * 4, 360 * 2 + 30), (30, 24, 28, 255))
    for i, t in enumerate(tiles):
        sheet.paste(t, (360 * (i % 4), 30 + 360 * (i // 4)))
    ImageDraw.Draw(sheet).text((10, 8), 'Clairaudiograph - actual atlas; mounted cylinder and vial are renderer stand-ins',
                               fill=(220, 200, 200))
    out = ROOT / 'build/model-previews/clairaudiograph.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(out)
    print(out)


if __name__ == '__main__':
    main()

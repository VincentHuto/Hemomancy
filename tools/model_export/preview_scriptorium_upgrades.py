"""Textured orthographic asset preview and export validation (Pillow + NumPy)."""

import copy
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

from export_scriptorium_upgrades import ASSETS, MODELS, ROOT, SOURCE, GLOW_SOURCE, blockbench, eightfold, palimpsest, texture_size, glow_source, fit_uvs


def validate():
    base = json.loads((MODELS / "enzymatic_scriptorium.json").read_text())
    first = eightfold(base)
    assert GLOW_SOURCE.read_text() == glow_source(first), 'emissive mesh export drift'
    for name, expected in [("enzymatic_scriptorium", fit_uvs(copy.deepcopy(base))),
                           ("eightfold_scriptorium", first),
                           ("palimpsest_scriptorium", palimpsest(first))]:
        model = json.loads((MODELS / (name + ".json")).read_text())
        assert model == expected, name + " export drift"
        source = json.loads((SOURCE / (name + ".bbmodel")).read_text())
        assert source == blockbench(name, expected), name + " Blockbench drift"
        assert len({t['uuid'] for t in source['textures']}) == len(source['textures'])
        for e in source['elements']:
            x, y, z = [b-a for a, b in zip(e['from'], e['to'])]
            sizes = {'north': (x, y), 'south': (x, y), 'east': (z, y),
                     'west': (z, y), 'up': (x, z), 'down': (x, z)}
            for face, f in e['faces'].items():
                u0, v0, u1, v1 = f['uv']
                assert np.allclose((abs(u1-u0), abs(v1-v0)), sizes[face]), (e['name'], face)
        # The fixed renderer positions are inherited exactly, including all eight vessels.
        exported_by_name = {e['name']: e for e in model['elements']}
        for original in base['elements']:
            if not original['name'].startswith('reservoir_'):
                continue
            exported = exported_by_name[original['name']]
            for field in ('name', 'from', 'to', 'rotation'):
                assert exported.get(field) == original.get(field)
        assert len(model['elements']) <= 160, name + ' exceeds the geometry budget'
        for e in model['elements']:
            assert all(-16 <= v <= 32 and v * 4 == round(v * 4)
                       for k in ('from', 'to') for v in e[k]), e['name']
            assert all(b > a for a, b in zip(e['from'], e['to'])), e['name']
            x, y, z = [b-a for a, b in zip(e['from'], e['to'])]
            sizes = {'north': (x, y), 'south': (x, y), 'east': (z, y),
                     'west': (z, y), 'up': (x, z), 'down': (x, z)}
            for face, f in e['faces'].items():
                resource = model['textures'][f['texture'][1:]].split(':')[1]
                assert (ASSETS / 'textures' / (resource + '.png')).is_file(), resource
                assert all(0 <= uv <= 16 for uv in f['uv'])
                _, _, width, height = texture_size(model['textures'][f['texture'][1:]])
                u0, v0, u1, v1 = f['uv']
                sampled = (abs(u1-u0)*width/16, abs(v1-v0)*height/16)
                assert np.allclose(sampled, sizes[face]), (e['name'], face, sampled, sizes[face])
        print(f"Validated {name}: geometry, one-pixel-per-unit UVs, animation frames, renderer alignment, Blockbench parity")
    states = json.loads((ASSETS / 'blockstates/enzymatic_scriptorium.json').read_text())['variants']
    for stage, name in enumerate(['enzymatic_scriptorium', 'eightfold_scriptorium', 'palimpsest_scriptorium']):
        for facing, angle in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]:
            v = states[f'facing={facing},stage={stage}']
            assert v['model'] == 'hemomancy:block/' + name and v.get('y', 0) == angle
    print('Validated all 12 facing/stage variants')
    item = json.loads((ASSETS / 'models/item/enzymatic_scriptorium.json').read_text())
    assert item['elements'][:len(base['elements'])] == base['elements'], 'inventory body UV drift'
    print('Validated inventory model body matches the corrected base block')


def render(model, yaw, size=420):
    # Draw the actual PNG textures onto projected model faces with a depth buffer.
    canvas = np.zeros((size, size, 4), dtype=float)
    canvas[:] = [24, 20, 25, 255]
    depth = np.full((size, size), -np.inf)
    a = math.radians(yaw)
    eye = np.array([math.sin(a), .7, -math.cos(a)])
    eye /= np.linalg.norm(eye)
    right = np.cross(eye, [0, 1, 0]); right /= np.linalg.norm(right)
    up = np.cross(right, eye)
    textures = {k: np.array(Image.open(ASSETS / 'textures' / (v.split(':')[1] + '.png')).convert('RGBA')
                           .crop((0, 0, *texture_size(v)[2:])))
                for k, v in model['textures'].items() if k != 'particle'}
    face_indices = {'north': [0, 1, 3, 2], 'south': [5, 4, 6, 7],
                    'west': [4, 0, 2, 6], 'east': [1, 5, 7, 3],
                    'up': [2, 3, 7, 6], 'down': [4, 5, 1, 0]}
    shades = {'north': .8, 'south': .8, 'west': .65, 'east': .65, 'up': 1, 'down': .5}
    for e in model['elements']:
        lo, hi = e['from'], e['to']
        points = np.array([[x, y, z] for z in (lo[2], hi[2])
                           for y in (lo[1], hi[1]) for x in (lo[0], hi[0])], dtype=float)
        if 'rotation' in e:
            rot = e['rotation']; origin = np.array(rot['origin'])
            axis = 'xyz'.index(rot['axis']); angle = math.radians(rot['angle'])
            axes = [(1, 2), (2, 0), (0, 1)][axis]
            points -= origin
            v, w = points[:, axes[0]].copy(), points[:, axes[1]].copy()
            points[:, axes[0]] = v * math.cos(angle) - w * math.sin(angle)
            points[:, axes[1]] = v * math.sin(angle) + w * math.cos(angle)
            points += origin
        centered = points - [8, 14, 10]
        projected = np.stack([size/2 + centered @ right * 10,
                              size/2 - centered @ up * 10, centered @ eye], axis=1)
        for face, f in e['faces'].items():
            q = projected[face_indices[face]]
            tex = textures[f['texture'][1:]]
            u0, v0, u1, v1 = f['uv']
            uv = np.array([[u0, v1], [u1, v1], [u1, v0], [u0, v0]]) / 16
            for ids in ([0, 1, 2], [0, 2, 3]):
                t = q[ids]; tuv = uv[ids]
                xmin, ymin = np.maximum(np.floor(t[:, :2].min(axis=0)).astype(int), 0)
                xmax, ymax = np.minimum(np.ceil(t[:, :2].max(axis=0)).astype(int), size-1)
                if xmax < xmin or ymax < ymin:
                    continue
                xx, yy = np.meshgrid(np.arange(xmin, xmax+1)+.5, np.arange(ymin, ymax+1)+.5)
                denom = (t[1,1]-t[2,1])*(t[0,0]-t[2,0]) + (t[2,0]-t[1,0])*(t[0,1]-t[2,1])
                if abs(denom) < 1e-8:
                    continue
                b0 = ((t[1,1]-t[2,1])*(xx-t[2,0]) + (t[2,0]-t[1,0])*(yy-t[2,1])) / denom
                b1 = ((t[2,1]-t[0,1])*(xx-t[2,0]) + (t[0,0]-t[2,0])*(yy-t[2,1])) / denom
                b2 = 1-b0-b1
                z = b0*t[0,2] + b1*t[1,2] + b2*t[2,2]
                u = b0*tuv[0,0] + b1*tuv[1,0] + b2*tuv[2,0]
                v = b0*tuv[0,1] + b1*tuv[1,1] + b2*tuv[2,1]
                pixels = tex[np.clip((v*tex.shape[0]).astype(int), 0, tex.shape[0]-1),
                             np.clip((u*tex.shape[1]).astype(int), 0, tex.shape[1]-1)].astype(float)
                region = np.s_[ymin:ymax+1, xmin:xmax+1]
                mask = (b0 >= 0) & (b1 >= 0) & (b2 >= 0) & (z > depth[region]) & (pixels[:,:,3] > 0)
                if e['name'].startswith(('ground_rune_', 'book_script_')):
                    pixels[:,:,:3] = [255, 48, 64]
                else:
                    pixels[:,:,:3] *= shades[face]
                canvas[region][mask] = pixels[mask]
                depth[region][mask] = z[mask]
    return Image.fromarray(canvas.astype('uint8'), 'RGBA')


def main():
    validate()
    names = [('enzymatic_scriptorium', 'Base'), ('eightfold_scriptorium', 'Eightfold'),
             ('palimpsest_scriptorium', 'Palimpsest')]
    sheet = Image.new('RGBA', (1260, 920), (24, 20, 25, 255))
    draw = ImageDraw.Draw(sheet)
    item = json.loads((ASSETS / 'models/item/enzymatic_scriptorium.json').read_text())
    book = [e for e in item['elements'] if e['name'] in ('red_liber_cover', 'liber_pages')]
    for col, (name, title) in enumerate(names):
        model = json.loads((MODELS / (name + '.json')).read_text())
        model['elements'] += copy.deepcopy(book)
        for row, yaw in enumerate([30, 150]):
            sheet.paste(render(model, yaw), (420*col, 40+440*row))
        draw.text((420*col+170, 15), title, fill=(244, 224, 216))
    draw.text((20, 905), 'Asset preview: actual textures. Static book stand-in; live fluid and animation omitted.', fill=(184, 171, 172))
    output = ROOT / 'build/model-previews/scriptorium_upgrades.png'
    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(output)
    print(output)


if __name__ == '__main__':
    main()

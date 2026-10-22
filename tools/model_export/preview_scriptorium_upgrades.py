"""Textured orthographic asset preview and export validation (Pillow + NumPy)."""

import copy
import json
import math

import numpy as np
from PIL import Image, ImageDraw

from export_scriptorium_upgrades import (ASSETS, MODELS, ROOT, SOURCE, GLOW_SOURCE, STAGES, SIZES, JARS, JAR_HALF,
                                         FLUID_HALF, blockbench, feed_path, glow_source, models, static_book,
                                         tail_path, texture_image, texture_size, to_json)

GEOMETRY_BUDGET = 300


def validate():
    built = models()
    assert GLOW_SOURCE.read_text() == glow_source(built), 'generated geometry drift'
    for (name, stage), m in zip(STAGES, built):
        expected = to_json(m)
        model = json.loads((MODELS / (name + '.json')).read_text())
        assert model == expected, name + ' export drift'
        source = json.loads((SOURCE / (name + '.bbmodel')).read_text())
        assert source == blockbench(name, expected), name + ' Blockbench drift'
        assert len({t['uuid'] for t in source['textures']}) == len(source['textures'])
        assert len(model['elements']) <= GEOMETRY_BUDGET, name + ' exceeds the geometry budget'
        names = [e['name'] for e in model['elements']]
        for i, (x, z) in enumerate(JARS):
            glass = model['elements'][names.index(f'reservoir_{i}_glass')]
            assert glass['from'][0] == x - JAR_HALF and glass['from'][2] == z - JAR_HALF, f'jar {i} off its renderer centre'
            assert FLUID_HALF < JAR_HALF
        for e in model['elements']:
            assert all(-16 <= v <= 32 and v * 4 == round(v * 4) for k in ('from', 'to') for v in e[k]), (e['name'], e['from'], e['to'])
            assert all(b > a for a, b in zip(e['from'], e['to'])), e['name']
            if 'rotation' in e:
                assert e['rotation']['angle'] in (-45, -22.5, 22.5, 45), e['name']
            dims = [b - a for a, b in zip(e['from'], e['to'])]
            for face, f in e['faces'].items():
                resource = model['textures'][f['texture'][1:]]
                texture_image(resource)
                _, _, width, height = texture_size(resource)
                u0, v0, u1, v1 = f['uv']
                assert all(0 <= uv <= 16 for uv in f['uv']), (e['name'], face)
                sampled = (abs(u1 - u0) * width / 16, abs(v1 - v0) * height / 16)
                size = dims[SIZES[face][0]], dims[SIZES[face][1]]
                assert np.allclose(sampled, size), (e['name'], face, sampled, size)
        print(f'Validated {name}: {len(model["elements"])} cubes, quarter grid, one-pixel-per-unit UVs, '
              'legal rotations, jar/renderer alignment, Blockbench parity')
    states = json.loads((ASSETS / 'blockstates/enzymatic_scriptorium.json').read_text())['variants']
    for name, stage in STAGES:
        for facing, angle in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]:
            v = states[f'facing={facing},stage={stage}']
            assert v['model'] == 'hemomancy:block/' + name and v.get('y', 0) == angle
    print('Validated all 12 facing/stage variants')
    item = json.loads((ASSETS / 'models/item/enzymatic_scriptorium.json').read_text())
    base = to_json(built[0])
    assert item['elements'][:len(base['elements'])] == base['elements'], 'inventory body drift'
    assert item['elements'][len(base['elements']):] == static_book(), 'inventory book drift'
    print('Validated inventory model: base body plus static grimoire')
    return built


def rotate(points, rot):
    origin = np.array(rot['origin'], dtype=float)
    axis = 'xyz'.index(rot['axis'])
    angle = math.radians(rot['angle'])
    i, j = [(1, 2), (2, 0), (0, 1)][axis]
    p = points - origin
    v, w = p[:, i].copy(), p[:, j].copy()
    p[:, i] = v * math.cos(angle) - w * math.sin(angle)
    p[:, j] = v * math.sin(angle) + w * math.cos(angle)
    return p + origin


def render(model, yaw, size=440, pitch=.7, paths=(), scale=9.5):
    """Draw the actual PNG textures onto projected model faces with a depth buffer."""
    canvas = np.zeros((size, size, 4), dtype=float)
    canvas[:] = [24, 20, 25, 255]
    depth = np.full((size, size), -np.inf)
    a = math.radians(yaw)
    eye = np.array([math.sin(a), pitch, -math.cos(a)])
    eye /= np.linalg.norm(eye)
    right = np.cross(eye, [0, 1, 0]); right /= np.linalg.norm(right)
    up = np.cross(right, eye)
    textures = {}
    for k, v in model['textures'].items():
        image = texture_image(v).convert('RGBA')
        textures[k] = np.array(image.crop((0, 0, *texture_size(v)[2:])))
    face_indices = {'north': [0, 1, 3, 2], 'south': [5, 4, 6, 7],
                    'west': [4, 0, 2, 6], 'east': [1, 5, 7, 3],
                    'up': [2, 3, 7, 6], 'down': [4, 5, 1, 0]}
    shades = {'north': .8, 'south': .8, 'west': .65, 'east': .65, 'up': 1, 'down': .5}


    def project(points):
        centered = points - [8, 13, 10]
        return np.stack([size / 2 + centered @ right * scale,
                         size / 2 - centered @ up * scale, centered @ eye], axis=1)

    for e in model['elements']:
        lo, hi = e['from'], e['to']
        points = np.array([[x, y, z] for z in (lo[2], hi[2])
                           for y in (lo[1], hi[1]) for x in (lo[0], hi[0])], dtype=float)
        if 'rotation' in e:
            points = rotate(points, e['rotation'])
        projected = project(points)
        glowing = e['name'].startswith(('ground_rune_', 'book_script_', 'vellum_left_scratch', 'vellum_right_scratch'))
        for face, f in e['faces'].items():
            quad = projected[face_indices[face]]
            tex = textures[f['texture'][1:]]
            u0, v0, u1, v1 = f['uv']
            uv = np.array([[u0, v1], [u1, v1], [u1, v0], [u0, v0]]) / 16
            for ids in ([0, 1, 2], [0, 2, 3]):
                t = quad[ids]; tuv = uv[ids]
                xmin, ymin = np.maximum(np.floor(t[:, :2].min(axis=0)).astype(int), 0)
                xmax, ymax = np.minimum(np.ceil(t[:, :2].max(axis=0)).astype(int), size - 1)
                if xmax < xmin or ymax < ymin:
                    continue
                xx, yy = np.meshgrid(np.arange(xmin, xmax + 1) + .5, np.arange(ymin, ymax + 1) + .5)
                denom = (t[1, 1] - t[2, 1]) * (t[0, 0] - t[2, 0]) + (t[2, 0] - t[1, 0]) * (t[0, 1] - t[2, 1])
                if abs(denom) < 1e-8:
                    continue
                b0 = ((t[1, 1] - t[2, 1]) * (xx - t[2, 0]) + (t[2, 0] - t[1, 0]) * (yy - t[2, 1])) / denom
                b1 = ((t[2, 1] - t[0, 1]) * (xx - t[2, 0]) + (t[0, 0] - t[2, 0]) * (yy - t[2, 1])) / denom
                b2 = 1 - b0 - b1
                z = b0 * t[0, 2] + b1 * t[1, 2] + b2 * t[2, 2]
                u = b0 * tuv[0, 0] + b1 * tuv[1, 0] + b2 * tuv[2, 0]
                v = b0 * tuv[0, 1] + b1 * tuv[1, 1] + b2 * tuv[2, 1]
                pixels = tex[np.clip((v * tex.shape[0]).astype(int), 0, tex.shape[0] - 1),
                             np.clip((u * tex.shape[1]).astype(int), 0, tex.shape[1] - 1)].astype(float)
                region = np.s_[ymin:ymax + 1, xmin:xmax + 1]
                mask = (b0 >= 0) & (b1 >= 0) & (b2 >= 0) & (z > depth[region]) & (pixels[:, :, 3] > 0)
                if glowing:
                    pixels[:, :, :3] = [255, 48, 64]
                else:
                    pixels[:, :, :3] *= shades[face]
                alpha = pixels[:, :, 3:4] / 255
                blended = canvas[region][:, :, :3] * (1 - alpha) + pixels[:, :, :3] * alpha
                current = canvas[region]
                current[:, :, :3] = np.where(mask[:, :, None], blended, current[:, :, :3])
                canvas[region] = current
                # Translucent glass still occludes what lies behind it, as in the baked model.
                depth[region] = np.where(mask & (pixels[:, :, 3] > 40), z, depth[region])
    image = Image.fromarray(canvas.astype('uint8'), 'RGBA')
    if paths:
        draw = ImageDraw.Draw(image)
        for path, colour in paths:
            pts = project(np.array(path, dtype=float))
            draw.line([(p[0], p[1]) for p in pts], fill=colour, width=2)
    return image


COLOURS = [(225, 25, 32), (231, 95, 25), (224, 207, 45), (245, 237, 222),
           (25, 90, 37), (35, 115, 220), (155, 155, 155), (130, 45, 175)]


def main():
    validate()
    titles = ['Base (D3)', 'Eightfold (D5)', 'Palimpsest (D7)']
    views = [(-35, .55), (35, .55), (180, .45)]
    cell = 440
    sheet = Image.new('RGBA', (cell * 3, 40 + cell * len(views) + 30), (24, 20, 25, 255))
    draw = ImageDraw.Draw(sheet)
    for col, ((name, stage), title) in enumerate(zip(STAGES, titles)):
        model = json.loads((MODELS / (name + '.json')).read_text())
        model['textures']['book'] = 'hemomancy:entity/liber_sanguinum'
        model['elements'] += copy.deepcopy(static_book())
        for row, (yaw, pitch) in enumerate(views):
            sheet.paste(render(model, yaw, cell, pitch), (cell * col, 40 + cell * row))
        draw.text((cell * col + 180, 15), title, fill=(244, 224, 216))
    draw.text((20, 40 + cell * len(views) + 8), 'Asset preview: actual textures. Static book stand-in; live fluid, '
              'feed beads and glow animation omitted.', fill=(184, 171, 172))
    output = ROOT / 'build/model-previews/scriptorium_upgrades.png'
    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(output)
    # Feed-line overlay: each selected jar's bead path drawn over the front view of every stage.
    overlay = Image.new('RGBA', (cell * 3, cell), (24, 20, 25, 255))
    for col, (name, stage) in enumerate(STAGES):
        model = json.loads((MODELS / (name + '.json')).read_text())
        paths = [(feed_path(i) + tail_path(stage), COLOURS[i]) for i in range(8)]
        overlay.paste(render(model, -35, cell, .55, paths), (cell * col, 0))
    overlay.convert('RGB').save(ROOT / 'build/model-previews/scriptorium_feed_paths.png')
    print(output)


if __name__ == '__main__':
    main()

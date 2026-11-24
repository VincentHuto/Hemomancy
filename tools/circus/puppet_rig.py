"""Shared authoring pipeline for the Crimson Troupe puppet rigs.

Each build_<puppet>.py describes its joints, materials and Java animation; this module packs the
box UVs, writes the Java model, exports the portable Blockbench file and paints the atlas so all
three stay in step. Geometry sizes are whole pixels (half pixels only for fingers, blades and
threads), so every face sits on the texel grid. No per-face outlines: light comes from above.
"""
from pathlib import Path
import base64
import json
import math
import subprocess
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon'
TEXTURES = 'textures/entity/puppeteer_summon/'
BBMODELS = ASSETS / 'models/entity/bbmodel'
ATLAS = 128


def joint(parts, name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
    parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))


def box(origin, size, material):
    return dict(origin=origin, size=size, material=material)


def noise(x, y, salt=0):
    n = (x * 374761393 + y * 668265263 + salt * 2246822519) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return (n ^ (n >> 16)) / 0xFFFFFFFF


def front_edge(side, px, w):
    """True on the column of a side strip that meets the front face."""
    return (side == 'east' and px == w - 1) or (side == 'west' and px == 0)


def back_edge(side, px, w):
    return (side == 'east' and px == 0) or (side == 'west' and px == w - 1)


def geometry(parts):
    """Pack box UVs and return the createBodyLayer() statements plus each UV origin's material."""
    x = y = row = 0
    materials, lines = {}, []
    f = lambda n: str(float(n)) + 'F'
    for part in parts:
        cubes = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            w, h, d = map(math.ceil, cube['size'])
            width, height = 2 * (w + d), h + d
            if x + width > ATLAS:
                x, y, row = 0, y + row + 1, 0
            if y + height > ATLAS:
                raise ValueError(part['name'] + ': atlas is full')
            materials[(x, y)] = cube['material']
            cubes += f'.texOffs({x}, {y}).addBox({", ".join(f(n) for n in (*cube["origin"], *cube["size"]))})'
            x += width + 1
            row = max(row, height)
        rotation = [math.radians(n) for n in part['rotation']]
        pose = 'PartPose.offsetAndRotation' if any(rotation) else 'PartPose.offset'
        values = (*part['pivot'], *rotation) if any(rotation) else part['pivot']
        lines.append(f'        PartDefinition {part["name"]} = {part["parent"]}.addOrReplaceChild('
                     f'"{part["name"]}", {cubes}, {pose}({", ".join(f(n) for n in values)}));')
    return '\n'.join(lines) + f'\n        return LayerDefinition.create(mesh, {ATLAS}, {ATLAS});', materials


def paint(model, materials, shade, palette, lit=()):
    """Paint every used UV face; `lit` palette indices are copied into a sparse glow atlas."""
    colors = [tuple(bytes.fromhex(color[1:])) + (255,) for color in palette]
    image = Image.new('RGBA', (ATLAS, ATLAS))
    glow = Image.new('RGBA', (ATLAS, ATLAS))
    for cube in model['elements']:
        material = materials[tuple(cube['uv_offset'])]
        for side, face in cube['faces'].items():
            u, v, s, t = face['uv']
            left, top = math.floor(min(u, s)), math.floor(min(v, t))
            w, h = math.ceil(max(u, s)) - left, math.ceil(max(v, t)) - top
            for py in range(h):
                for px in range(w):
                    index = shade(material, side, px, py, w, h, left + px, top + py)
                    image.putpixel((left + px, top + py), colors[index])
                    if index in lit:
                        glow.putpixel((left + px, top + py), colors[index])
    return image, glow


def build(texture_id, model_name, parts, java, shade, palette, glow=None, variants=()):
    """Write the Java model, portable Blockbench file and atlas for one puppet.

    `java` is the full source with a `/* GEOMETRY */` marker inside createBodyLayer().
    `glow` is a set of palette indices for `<id>_glow.png`; `variants` maps an atlas suffix
    to replacement colors by palette index (for example the Blood Cur).
    """
    for part in parts:
        for values in [part['pivot']] + [c[k] for c in part['cubes'] for k in ('origin', 'size')]:
            assert all(abs(n * 4 - round(n * 4)) < 1e-6 for n in values), (part['name'], values)
    statements, materials = geometry(parts)
    source = MODELS / (model_name + '.java')
    texture = TEXTURES + texture_id + '.png'
    output = BBMODELS / (model_name + '.bbmodel')
    source.write_text(java.replace('/* GEOMETRY */', statements.lstrip()), encoding='utf-8')
    Image.new('RGBA', (ATLAS, ATLAS)).save(ASSETS / texture)
    subprocess.run(['node', str(ROOT / 'tools/model_export/java_model_to_bbmodel.mjs'), '--source', str(source),
                    '--texture', texture, '--output', str(output)], check=True)
    model = json.loads(output.read_text(encoding='utf-8'))
    image, glow_image = paint(model, materials, shade, palette, glow or ())
    image.save(ASSETS / texture)
    if glow:
        glow_image.save(ASSETS / (TEXTURES + texture_id + '_glow.png'))
    for suffix, colors in dict(variants).items():
        swapped = [colors.get(i, color) for i, color in enumerate(palette)]
        paint(model, materials, shade, swapped)[0].save(ASSETS / (TEXTURES + texture_id + suffix + '.png'))
    model['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(
        (ASSETS / texture).read_bytes()).decode('ascii')
    output.write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')
    print(f'{model_name}: {len(parts)} joints, {len(model["elements"])} cubes, {ATLAS}px atlas')
    return output


def blockbench_mesh_source(model, mesh_name, project_name, script_name, joint_name, detail_name):
    elements = {cube['uuid']: cube for cube in model['elements']}
    lines = []
    f = lambda values: ', '.join(f'{v:.9g}F' for v in values)

    def offset(point, pivot):
        x, y, z = (a - b for a, b in zip(point, pivot))
        return x, -y, z

    def part(name, parent, node, parent_pivot, cubes):
        pivot = node['origin']
        x, y, z = node.get('rotation', (0, 0, 0))
        rotation = tuple(map(math.radians, (-x, y, -z)))
        pose = (f'PartPose.offsetAndRotation({f((*offset(pivot, parent_pivot), *rotation))})'
                if any(rotation) else f'PartPose.offset({f(offset(pivot, parent_pivot))})')
        lines.append(f'        PartDefinition {name} = {parent}.addOrReplaceChild("{name}",')
        lines.append('                CubeListBuilder.create()')
        for cube in cubes:
            a, b = cube['from'], cube['to']
            u, v = cube['uv_offset']
            size = tuple(hi - lo for lo, hi in zip(a, b))
            bounds = offset((a[0], b[1], a[2]), pivot)
            lines.append(f'                        .texOffs({u}, {v}).addBox({f((*bounds, *size))})')
        lines[-1] += f', {pose});'

    def visit(group, parent, parent_pivot):
        name = 'root' if parent == 'mesh.getRoot()' else joint_name(group['name'])
        cubes = [elements[node] for node in group['children'] if isinstance(node, str)]
        details = []
        attached = []
        for index, cube in enumerate(cubes):
            child = detail_name(name, index, cube)
            if child is None and any(cube.get('rotation', ())):
                child = f'{name}_detail_{index}'
            if child is None:
                attached.append(cube)
            else:
                details.append((child, cube))
        part(name, parent, group, parent_pivot, attached)
        for child, cube in details:
            part(child, name, cube, group['origin'], [cube])
        for node in group['children']:
            if isinstance(node, dict):
                visit(node, name, group['origin'])

    root, = model['outliner']
    visit(root, 'mesh.getRoot()', (0, 24, 0))
    w, h = (model['resolution'][axis] for axis in ('width', 'height'))
    geometry = '\n'.join(lines)
    return f'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Generated from {project_name} by tools/circus/{script_name}. */
final class {mesh_name} {{
    private {mesh_name}() {{}}

    static LayerDefinition createBodyLayer() {{
        MeshDefinition mesh = new MeshDefinition();
{geometry}
        return LayerDefinition.create(mesh, {w}, {h});
    }}
}}
'''

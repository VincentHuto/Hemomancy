"""Bake the editable Phlegethosoma Blockbench project into a Java mesh and runtime atlases."""
import base64
import io
import json
import math
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
SOURCE = ASSETS / 'models/entity/bbmodel/bosses/PhlegethosomaModel.bbmodel'
TEXTURES = ASSETS / 'textures/entity/phlegethosoma'
PACKAGE = 'com.vincenthuto.hemomancy.client.model.entity.boss.phlegethosoma'
MESH = ROOT / 'src/main/java' / PACKAGE.replace('.', '/') / 'PhlegethosomaMesh.java'


def floats(values):
    return ', '.join(f'{value:g}F' for value in values)


def write_group(lines, group, cubes, parent, parent_origin):
    x, y, z = group['origin']
    dx, dy, dz = x - parent_origin[0], parent_origin[1] - y, z - parent_origin[2]
    rx, ry, rz = group.get('rotation', (0, 0, 0))
    pose = f'PartPose.offset({floats((dx, dy, dz))})'
    if rx or ry or rz:
        rotations = tuple(math.radians(angle) for angle in (-rx, ry, -rz))
        pose = f'PartPose.offsetAndRotation({floats((dx, dy, dz, *rotations))})'

    name = group['name']
    children = [child for child in group['children'] if isinstance(child, dict)]
    assignment = f'PartDefinition {name} = ' if children else ''
    lines.append(f'        {assignment}{parent}.addOrReplaceChild("{name}", CubeListBuilder.create()')
    for child in group['children']:
        if isinstance(child, dict):
            continue
        cube = cubes[child]
        a, b, c = cube['from']
        A, B, C = cube['to']
        u, v = cube['uv_offset']
        w, h, d = A - a, B - b, C - c
        box_uv = {
            'east': [u, v+d, u+d, v+d+h], 'west': [u+d+w, v+d, u+2*d+w, v+d+h],
            'north': [u+d, v+d, u+d+w, v+d+h], 'south': [u+2*d+w, v+d, u+2*d+2*w, v+d+h],
            'up': [u+d+w, v+d, u+d, v], 'down': [u+d+2*w, v, u+d+w, v+d],
        }
        if min(w, h, d) < 1 or any(cube['faces'][face]['uv'] != uv for face, uv in box_uv.items()):
            raise ValueError(f"{cube['name']} requires unmirrored, one-pixel-per-unit box UVs")
        lines.append(f'                .texOffs({u}, {v}).addBox({floats((a-x, y-B, c-z, w, h, d))})')
    lines[-1] += f', {pose});'
    if children:
        lines.append('')
    for child in children:
        write_group(lines, child, cubes, name, group['origin'])


def write_glow(atlas, cubes):
    glow = Image.new('RGBA', atlas.size)
    artery = {(214, 77, 84), (238, 103, 93), (250, 141, 112)}
    crop = {(232, 97, 90), (248, 121, 97), (255, 151, 111), (255, 181, 130)}
    for cube in cubes.values():
        name = cube['name']
        if name in {'muscular_body', 'dorsal_fold'}:
            faces, colors, strength = ['up'], artery, (95, 22, 18)
        elif name in {'distended_blood_sac', 'dorsal_membrane'}:
            faces, colors, strength = ['up', 'west', 'east'], crop, (77, 20, 15)
        else:
            continue
        for face in faces:
            x, y, X, Y = cube['faces'][face]['uv']
            for v in range(min(y, Y), max(y, Y)):
                for u in range(min(x, X), max(x, X)):
                    if atlas.getpixel((u, v))[:3] in colors:
                        glow.putpixel((u, v), (*strength, 255))
    glow.save(TEXTURES / 'phlegethosoma_glow.png')


def main():
    project = json.loads(SOURCE.read_text(encoding='utf-8'))
    cubes = {cube['uuid']: cube for cube in project['elements']}
    width = project['resolution']['width']
    height = project['resolution']['height']
    encoded_atlas = project['textures'][0]['source'].removeprefix('data:image/png;base64,')
    png = base64.b64decode(encoded_atlas, validate=True)
    atlas = Image.open(io.BytesIO(png)).convert('RGBA')
    if atlas.size != (width, height):
        raise ValueError('The embedded atlas and the Blockbench UV grid must have the same dimensions')

    lines = [f'''package {PACKAGE};

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Geometry baked from PhlegethosomaModel.bbmodel by tools/model_export/phlegethosoma.py. */
final class PhlegethosomaMesh {{
    private PhlegethosomaMesh() {{}}

    static LayerDefinition createLayer() {{
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();''']
    for group in project['outliner']:
        write_group(lines, group, cubes, 'root', (0, 24, 0))
    lines.extend(['', f'        return LayerDefinition.create(mesh, {width}, {height});', '    }', '}'])
    MESH.write_text('\n'.join(lines) + '\n', encoding='utf-8')
    (TEXTURES / 'phlegethosoma.png').write_bytes(png)
    write_glow(atlas, cubes)


if __name__ == '__main__':
    main()

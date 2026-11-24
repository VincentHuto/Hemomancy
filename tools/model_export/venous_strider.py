"""Bake the Venous Strider Blockbench project into its Java mesh and runtime atlases.

    python tools/model_export/venous_strider.py
    python tools/model_export/venous_strider.py --check

Edit and save VenousStriderModel.bbmodel with the painted atlas embedded before rebaking.
"""
import argparse
import base64
import io
import json
import math
from pathlib import Path
from struct import iter_unpack

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
PROJECT = ASSETS / 'models/entity/bbmodel/VenousStriderModel.bbmodel'
TEXTURES = ASSETS / 'textures/entity/venous_strider'
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/VenousStriderMesh.java'

# Dim vessels and a small amber glint, added by the renderer's existing breath cycle.
GLOW_COLORS = {
    (157, 75, 88): (74, 14, 23),
    (135, 64, 76): (52, 9, 16),
    (165, 92, 101): (57, 15, 21),
    (190, 122, 123): (67, 20, 23),
    (190, 138, 130): (39, 10, 13),
    (228, 147, 81): (108, 54, 22),
}


def float_args(values):
    return ', '.join(f'{value if abs(value) >= 0.0000005 else 0.0:.6f}'.rstrip('0').rstrip('.')
                     + 'F' for value in values)


def mesh_source(model):
    elements = {element['uuid']: element for element in model['elements']}
    width, height = (model['resolution'][key] for key in ('width', 'height'))
    lines = [
        'package com.vincenthuto.hemomancy.client.model.entity.mob.animal;',
        '',
        'import net.minecraft.client.model.geom.PartPose;',
        'import net.minecraft.client.model.geom.builders.CubeDeformation;',
        'import net.minecraft.client.model.geom.builders.CubeListBuilder;',
        'import net.minecraft.client.model.geom.builders.LayerDefinition;',
        'import net.minecraft.client.model.geom.builders.MeshDefinition;',
        'import net.minecraft.client.model.geom.builders.PartDefinition;',
        '',
        '/** Baked from VenousStriderModel.bbmodel by tools/model_export/venous_strider.py. */',
        'public final class VenousStriderMesh {',
        '\tprivate VenousStriderMesh() {',
        '\t}',
        '',
        '\tpublic static LayerDefinition createBodyLayer() {',
        '\t\tMeshDefinition mesh = new MeshDefinition();',
    ]

    def add_part(name, variable, parent, origin, parent_origin, rotation, boxes, has_children=False):
        # Blockbench's Java entity codec reflects X and Y and keeps Z.
        offset = [(coordinate - ancestor) * sign
                  for coordinate, ancestor, sign in zip(origin, parent_origin, (-1, -1, 1))]
        pose = f'PartPose.offset({float_args(offset)})'
        if any(rotation):
            radians = [math.radians(degrees * sign) for degrees, sign in zip(rotation, (-1, -1, 1))]
            pose = f'PartPose.offsetAndRotation({float_args(offset + radians)})'
        prefix = f'PartDefinition {variable} = ' if has_children else ''
        lines.append(f'\t\t{prefix}{parent}.addOrReplaceChild("{name}", CubeListBuilder.create()')
        for box in boxes:
            lo, hi = box['from'], box['to']
            x, y, z = origin
            dimensions = [b - a for a, b in zip(lo, hi)]
            values = [x - hi[0], y - hi[1], lo[2] - z] + dimensions
            u, v = box['uv_offset']
            mirror = '.mirror()' if box.get('mirror_uv') else ''
            deformation = f", new CubeDeformation({float_args([box['inflate']])})" if box.get('inflate') else ''
            lines.append(f'\t\t\t\t.texOffs({u}, {v}){mirror}.addBox({float_args(values)}{deformation})'
                         + ('.mirror(false)' if mirror else ''))
        lines[-1] += f', {pose});'

    def walk(group, parent, parent_origin):
        name = group['name']
        children = group['children']
        elements_here = [elements[child] for child in children if isinstance(child, str)]
        subgroups = [child for child in children if isinstance(child, dict)]
        direct = [box for box in elements_here if not any(box.get('rotation', []))]
        rotated = [box for box in elements_here if any(box.get('rotation', []))]
        add_part(name, name, parent, group['origin'], parent_origin, group.get('rotation', []),
                 direct, bool(subgroups or rotated))
        for index, box in enumerate(rotated, 1):
            add_part(f'{name}_cube_{index}', None, name, box['origin'], group['origin'], box['rotation'], [box])
        for child in subgroups:
            walk(child, name, group['origin'])

    for group in model['outliner']:
        walk(group, 'mesh.getRoot()', (0, 24, 0))
    lines.extend([f'\t\treturn LayerDefinition.create(mesh, {width}, {height});', '\t}', '}', ''])
    return '\n'.join(lines).encode()


def bake():
    model = json.loads(PROJECT.read_text(encoding='utf-8'))
    if model['meta']['model_format'] != 'modded_entity' or not model['meta']['box_uv']:
        raise ValueError('Expected a Modded Entity project with box UVs')
    (texture,) = model['textures']
    atlas_bytes = base64.b64decode(texture['source'].split(',', 1)[1], validate=True)
    with Image.open(io.BytesIO(atlas_bytes)) as atlas:
        size = model['resolution']
        if atlas.size != (size['width'], size['height']):
            raise ValueError('The project UV resolution must match its embedded texture')
        pixels = iter_unpack('4B', atlas.convert('RGBA').tobytes())
        glow = Image.new('RGBA', atlas.size)
        glow.putdata([GLOW_COLORS[(r, g, b)] + (255,) if a and (r, g, b) in GLOW_COLORS else (0, 0, 0, 0)
                      for r, g, b, a in pixels])
    buffer = io.BytesIO()
    glow.save(buffer, format='PNG')
    return {
        JAVA: mesh_source(model),
        TEXTURES / 'model_venous_strider.png': atlas_bytes,
        TEXTURES / 'model_venous_strider_glow.png': buffer.getvalue(),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='Verify that the generated mesh and atlases are current')
    args = parser.parse_args()
    for path, data in bake().items():
        if args.check:
            existing = path.read_bytes().replace(b'\r\n', b'\n') if path == JAVA else path.read_bytes()
            if existing != data:
                parser.exit(1, f'Stale generated asset: {path.relative_to(ROOT)}\n')
        else:
            path.write_bytes(data)
        print(path.relative_to(ROOT))


if __name__ == '__main__':
    main()

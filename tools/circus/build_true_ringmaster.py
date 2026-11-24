"""Export both Ringmaster Blockbench rigs to Java meshes and runtime texture atlases."""

import base64
import io
import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODELS = ASSETS / 'models/entity/bbmodel/npc/harbinger/circus'
TEXTURES = ASSETS / 'textures/entity/npc/harbinger/circus'
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
FACES = ('north', 'east', 'south', 'west', 'up', 'down')


def numbers(values):
    return ', '.join(f'{v:.7g}F' for v in values)


def offset(point, parent):
    return [point[0] - parent[0], parent[1] - point[1], point[2] - parent[2]]


def rotation(node):
    x, y, z = node.get('rotation', (0, 0, 0))
    return (-x, y, -z)


def joint_name(name, side):
    side_names = {'hip': 'thigh', 'knee': 'shin', 'ankle': 'foot', 'coat tail': 'tail',
                  'shoulder': 'upper', 'elbow': 'fore', 'wrist': 'hand'}
    if name.startswith(('Left ', 'Right ')):
        side, _, name = name.lower().partition(' ')
        return f'{side}_{side_names[name]}', side
    if name.startswith('Finger '):
        return 'finger_' + str(int(name.split()[-1]) - 1), side
    if name in ('Ringmaster • Base Form', 'Ringmaster • True Self'):
        return 'root', side
    if name in ('topper', 'core'):
        return name, side
    if name.startswith('Blood tether '):
        return 'thread_' + name.split()[-1], side
    return {
        'Pelvis': 'pelvis', 'Chest': 'chest', 'Head': 'head', 'Top hat': 'hat',
        'Distal joint': 'tip', 'Thumb': 'thumb', 'Thumb tip': 'tip',
        'Conductor staff': 'staff', 'Three tethers': 'strings',
    }[name], side


def export(model_name, texture_name, method):
    model = json.loads((MODELS / (model_name + '.bbmodel')).read_text(encoding='utf-8'))
    elements = {e['uuid']: e for e in model['elements']}
    used = set()
    image = Image.open(io.BytesIO(base64.b64decode(model['textures'][0]['source'].split(',')[1]))).convert('RGBA')
    width, height = image.size
    assert (width, height) == (model['resolution']['width'], model['resolution']['height'])
    glow = Image.new('RGBA', image.size)

    def emit_cube(cube, origin):
        low = offset([cube['from'][0], cube['to'][1], cube['from'][2]], cube['origin'])
        high = offset([cube['to'][0], cube['from'][1], cube['to'][2]], cube['origin'])
        name = cube['name']
        uv = []
        dx, dy, dz = (b - a for a, b in zip(cube['from'], cube['to']))
        for face, size in zip(FACES, ((dx, dy), (dz, dy), (dx, dy), (dz, dy), (dx, dz), (dx, dz))):
            rect = cube['faces'][face]['uv']
            x, y, right, bottom = rect
            assert (right - x, bottom - y) == size and 0 <= x < right <= width and 0 <= y < bottom <= height
            for j in range(y, bottom):
                for i in range(x, right):
                    assert (i, j) not in used
                    used.add((i, j))
                    r, g, b, alpha = image.getpixel((i, j))
                    assert alpha == 255
                    emissive = (
                        (name == 'stage mask' and face == 'north' and (r, g, b) in ((175, 38, 62), (255, 98, 91)))
                        or name == 'suspended blood gem'
                        or (name == 'blood behind ribs' and r >= 145)
                        or (name.startswith('Blood tether') and r >= 145)
                        or (name in ('veined palm', 'finger phalanx', 'thumb proximal') and r >= 145 and g < 65)
                    )
                    if emissive:
                        glow.putpixel((i, j), (r, g, b, 255))
            uv.extend(rect)
        return (f'new FaceUvCube({width}, {height}, {numbers(low + high)}, '
                f'{numbers(offset(cube["origin"], origin) + list(rotation(cube)))},\n'
                f'                        {numbers(uv)})')

    parts = []

    def emit_group(node, parent, side=''):
        name, side = joint_name(node['name'], side)
        origin = node['origin']
        cubes = [elements[c] for c in node['children'] if isinstance(c, str)]
        children = [c for c in node['children'] if isinstance(c, dict)]
        if name == 'staff':
            topper = [c for c in cubes if c['name'] == 'staff socket' or 'hoop' in c['name']]
            core = next(c for c in cubes if c['name'] == 'suspended blood gem')
            cubes = [c for c in cubes if c not in topper and c != core]
            children.append(dict(name='topper', origin=origin, children=[
                *(c['uuid'] for c in topper), dict(name='core', origin=core['origin'], children=[core['uuid']])
            ]))
        if name == 'strings':
            for strand in [c for c in cubes if c['name'].startswith('Blood tether')]:
                anchor = [strand['origin'][0], strand['from'][1], strand['origin'][2]]
                children.append(dict(name=strand['name'], origin=anchor, children=[strand['uuid']]))
                cubes.remove(strand)
        entries = [emit_group(child, origin, side) for child in children]
        geometry = ',\n                    '.join(emit_cube(cube, origin) for cube in cubes)
        child_map = ', '.join(f'Map.entry("{key}", {var})' for key, var in entries)
        variable = f'{side + "_" if side else ""}{name}_{len(parts)}'
        position = offset(origin, parent)
        if name == 'root':
            position[1] += 24
        parts.append(f'        ModelPart {variable} = part({numbers(position + list(rotation(node)))},\n'
                     f'                List.of({geometry}),\n'
                     f'                Map.ofEntries({child_map}));')
        return name, variable

    _, result = emit_group(model['outliner'][0], [0, 0, 0])
    assert len(used) == image.getchannel('A').histogram()[255]
    image.save(TEXTURES / (texture_name + '.png'))
    glow.save(TEXTURES / (texture_name + '_glow.png'))
    return '\n'.join([f'    static ModelPart {method}() {{', *parts, f'        return {result};', '    }'])


def main():
    base = export('CircusRingmasterModel', 'ringmaster', 'base')
    true_self = export('CircusTrueRingmasterModel', 'circus_true_ringmaster', 'trueSelf')
    (JAVA / 'RingmasterMesh.java').write_text('''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.client.model.entity.FaceUvCube;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;

// Generated by tools/circus/build_true_ringmaster.py from the two Ringmaster Blockbench projects.
final class RingmasterMesh {
    private RingmasterMesh() {}

''' + base + '\n\n' + true_self + '''

    private static ModelPart part(float x, float y, float z, float pitch, float yaw, float roll,
            List<ModelPart.Cube> cubes, Map<String, ModelPart> children) {
        ModelPart part = new ModelPart(cubes, children);
        PartPose pose = PartPose.offsetAndRotation(x, y, z,
                pitch * Mth.DEG_TO_RAD, yaw * Mth.DEG_TO_RAD, roll * Mth.DEG_TO_RAD);
        part.setInitialPose(pose);
        part.loadPose(pose);
        return part;
    }
}
''', encoding='utf-8')


if __name__ == '__main__':
    main()

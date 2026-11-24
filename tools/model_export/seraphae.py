
"""Bake Seraphae's Blockbench project into the Java rig and its three atlases."""
import argparse
import base64
from io import BytesIO
import json
import math
import re

import numpy as np
from PIL import Image

import annetta_kit as kit
from annetta_kit import ASSETS, ROOT, Rig, entity_java_source

JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/seraphae/SeraphaeModel.java'
PROJECT = ASSETS / 'models/entity/bbmodel/bosses/SeraphaeModel.bbmodel'
TEXTURES = ASSETS / 'textures/entity/boss/seraphae'


def rotation(matrix):
    return tuple(map(math.degrees, (
        math.atan2(matrix[2, 1], matrix[2, 2]),
        math.asin(float(np.clip(-matrix[2, 0], -1, 1))),
        math.atan2(matrix[1, 0], matrix[0, 0]),
    )))


def model_origin(origin):
    x, y, z = origin
    return np.array((x, 24 - y, z))


def load_rig(project):
    rig = Rig('seraphae', 'SeraphaeModel', 'seraphae')
    elements = {element['uuid']: element for element in project['elements']}
    parts = {}
    nerve_positions = {}

    def visit(group, authored_parent, authored_frame, authored_world, parent, frame, world):
        authored_name = group['name']
        # The authoring names use the viewer's left. Combat uses Seraphae's own left.
        name = re.sub(r'^(left|right)_', lambda m: {'left': 'right_', 'right': 'left_'}[m[1]], authored_name)
        origin = model_origin(group['origin'])
        x, y, z = group.get('rotation', (0, 0, 0))
        old_world = authored_world @ kit.rot_matrix((-x, y, -z))
        old_frame = authored_frame + authored_world @ (origin - authored_parent)

        match = re.fullmatch(r'(left|right)_nerve_([1-3])_(trunk|branch|dendrite|fork|terminal)_(\d+)', name)
        if match:
            side, lane, kind, number = match.groups()
            number = int(number)
            stem = f'{side}_nerve_{lane}'
            if kind == 'trunk' and number > 1:
                parent = f'{stem}_trunk_{number - 1}'
            elif kind == 'branch':
                parent = f'{stem}_trunk_{number + 1}'
            elif kind in ('dendrite', 'fork'):
                parent = f'{stem}_branch_{number}'
            elif kind == 'terminal':
                last = 5 if side == 'left' and lane == '3' else 4
                parent = f'{stem}_trunk_{last}'
            if parent in parts:
                frame, world = parts[parent]
            place = int(lane) - 1
            position = min(3, number - 1 if kind == 'trunk' else number)
            nerve_positions[name] = (int(side == 'right'), place, 3 if kind == 'terminal' else position)

        # The root is at the feet. Rotate the torso at the girdle so its lean stays rooted at the waist.
        pivot = old_frame
        if name == 'root':
            pivot = np.zeros(3)
        elif name == 'body':
            pivot = old_frame + old_world @ np.array((0, 12, 0))
        if name != 'root':
            rig.part(name, parent, tuple(world.T @ (pivot - frame)), rotation(world.T @ old_world))
        parts[name] = (pivot, old_world)

        for child in group['children']:
            if isinstance(child, dict):
                visit(child, origin, old_frame, old_world, name, pivot, old_world)
                continue
            element = elements[child]
            x0, y1, z0 = model_origin(element['from'])
            x1, y0, z1 = model_origin(element['to'])
            low = np.array((x0, y0, z0)) - origin + old_world.T @ (old_frame - pivot)
            dims = (x1 - x0, y1 - y0, z1 - z0)
            rig.cube(name, (*low, *dims), element['name'], uv=tuple(element['uv_offset']))

    # Within a wing, authoring orders each branch beside its trunk. Visit all main segments first
    # so that branches can be parented to their true joints while retaining the original rest pose.
    def order(group):
        group['children'].sort(key=lambda child: isinstance(child, dict)
                               and not re.search(r'_trunk_\d+$', child['name']))
        for child in group['children']:
            if isinstance(child, dict):
                order(child)

    tree, = project['outliner']
    order(tree)
    zero, identity = np.zeros(3), np.eye(3)
    visit(tree, model_origin(tree['origin']), model_origin(tree['origin']), identity, None, zero, identity)
    return rig, nerve_positions


def make_masks(rig, nerve_positions, atlas):
    vessels = Image.new('RGBA', atlas.size)
    nerves = Image.new('RGBA', atlas.size)
    for part in rig.parts:
        name = part['name']
        wing = name in nerve_positions
        arm = name.endswith(('_forearm', '_hand'))
        for cube in part['cubes']:
            material = cube['material']
            if not (wing or arm or material in ('face', 'unclosing_eye') or name.startswith('halo_bone_')):
                continue
            if arm and not material.endswith(('_pale_forearm', '_open_palm')):
                continue
            for face, rect in kit.face_rects(*cube['uv'], *cube['box'][3:]).items():
                if face in ('up', 'down'):
                    continue
                x0, y0, x1, y1 = rect
                width = math.ceil(x1) - math.floor(x0)
                for y in range(math.floor(y0), math.ceil(y1)):
                    for x in range(math.floor(x0), math.ceil(x1)):
                        red, green, blue, alpha = atlas.getpixel((x, y))
                        if not alpha:
                            continue
                        center = math.floor(x0) + width // 2
                        gold = red == 255 and green == 191 and blue == 91
                        if gold or (wing and x == center) or (arm and width >= 3 and x == math.floor(x0)):
                            nerves.putpixel((x, y), (255, 208, 119, 255))
                        elif red >= 244 and green >= 233 and blue >= 220:
                            vessels.putpixel((x, y), (255, 250, 244, 255))
    return vessels, nerves


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    project = json.loads(PROJECT.read_text(encoding='utf-8-sig'))
    rig, nerve_positions = load_rig(project)
    atlas_bytes = base64.b64decode(project['textures'][0]['source'].split(',', 1)[1])
    atlas = Image.open(BytesIO(atlas_bytes)).convert('RGBA')
    assert atlas.size == (project['resolution']['width'], project['resolution']['height'])
    (TEXTURES / 'seraphae.png').write_bytes(atlas_bytes)
    masks = make_masks(rig, nerve_positions, atlas)
    for name, mask in zip(('vessels', 'nerves'), masks):
        mask.save(TEXTURES / f'seraphae_{name}.png', optimize=True)

    table = ',\n'.join(f'\t\t\t\tnew NervePart({kit.camel(name)}, {side}, {place}, {position})'
                        for name, (side, place, position) in nerve_positions.items())
    extra = (ROOT / 'tools/model_export/seraphae_anim.java').read_text(encoding='utf-8')
    extra = extra.replace('/*NERVE_TABLE*/', table)

    source = entity_java_source(rig, atlas.size, 'SeraphaeEntity', 'seraphae.py',
        package='com.vincenthuto.hemomancy.client.model.entity.boss.seraphae',
        entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae',
        extra_imports='import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEndingRules;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeNerveFlashRules;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeOpenings;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraint;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraintState;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeVisualRules;\n'
                      'import com.vincenthuto.hemomancy.common.util.HemoEasing;\n'
                      'import com.mojang.blaze3d.vertex.VertexConsumer;\n'
                      'import org.joml.Quaternionf;\nimport org.joml.Matrix3f;\nimport org.joml.Vector3f;\n'
                      'import javax.annotation.Nullable;\n'
                      'import java.util.List;\n'
                      'import static com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaePose.*;\n'
                      'import static com.vincenthuto.hemomancy.common.util.HemoEasing.catmullRom;\n',
        extra_methods=extra)
    source = source.replace('public class SeraphaeModel extends HierarchicalModel<SeraphaeEntity>',
                            'public class SeraphaeModel<T extends SeraphaeEntity> extends HierarchicalModel<T>')
    source = source.replace('import net.minecraft.world.entity.HumanoidArm;\n', '')
    JAVA.write_text(source, encoding='utf-8')

    if args.preview:
        kit.SCALE = 1
        quads = kit.quads(rig)
        kit.preview_sheet([(quads, atlas, yaw, 0) for yaw in (0, -.52, math.pi)],
                          ROOT / 'build/seraphae_preview.png', cols=3)


if __name__ == '__main__':
    main()

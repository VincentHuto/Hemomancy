"""Bake Velorum's authored Blockbench rig and aligned emissive atlases."""
import base64
import json
from io import BytesIO
from pathlib import Path

from PIL import Image

import annetta_kit as kit

kit.SCALE = 1
PROJECT = kit.ASSETS / 'models/entity/bbmodel/bosses/VelorumModel.bbmodel'
TEXTURES = kit.ASSETS / 'textures/entity/boss/velorum'
JAVA = kit.ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/velorum/VelorumModel.java'
ALIASES = {
    'pelvis': 'hips', 'torso': 'chest', 'still_hour_halo': 'halo',
    'right_upper_arm': 'left_arm', 'right_forearm': 'left_forearm', 'right_hand': 'left_hand',
    'left_upper_arm': 'right_arm', 'left_forearm': 'right_forearm', 'left_hand': 'right_hand',
    'lantern_staff': 'staff', 'rime_lantern': 'staff_lantern', 'lantern_light': 'staff_lantern_core',
    'pectoral_reliquary': 'breast_seam',
}


def read_rig(project):
    rig = kit.Rig('velorum', 'VelorumModel', 'velorum')
    elements = {cube['uuid']: cube for cube in project['elements']}

    hanger = next(cube for cube in elements.values() if cube['name'] == 'lantern_hanging_prayer')
    pendant_origin = (hanger['from'][0] + .5, hanger['to'][1], -.15)

    def visit(group, parent, origin):
        name = ALIASES.get(group['name'], group['name'])
        x, y, z = group['origin']
        rx, ry, rz = group.get('rotation', (0, 0, 0))
        if parent is not None:
            rig.part(name, parent, (x-origin[0], origin[1]-y, z-origin[2]), (-rx, ry, -rz))
        for child in group['children']:
            if isinstance(child, dict):
                visit(child, name, (x, y, z))
                continue
            cube = elements[child]
            low, high = cube['from'], cube['to']
            w, h, d = (round(b-a, 5) for a,b in zip(low, high))
            u, v = cube['uv_offset']
            label = cube['name']
            target, px, py, pz = name, x, y, z
            if label.startswith('held_'):
                target = label
                px, py, pz = low[0]+w/2, high[1], low[2]+d/2
                rig.part(target, name, (px-x, y-py, pz-z))
            elif label.endswith('_votive_glass'):
                target = label
                px, py, pz = low[0] + w / 2, high[1], low[2] + d / 2
                rig.part(target, name, (px-x, y-py, pz-z))
            elif label in ('lantern_hanging_prayer', 'lantern_prayer_weight'):
                target = 'lantern_pendant'
                px, py, pz = pendant_origin
                if label == 'lantern_hanging_prayer':
                    rig.part(target, name, (px-x, y-py, pz-z))
            rig.cube(target, (low[0]-px, py-high[1], low[2]-pz, w, h, d), 'authored', uv=(u,v))

    visit(project['outliner'][0], None, (0, 0, 0))
    return rig


def glow_atlases(project, base):
    frost = Image.new('RGBA', base.size)
    eclipse = Image.new('RGBA', base.size)
    for cube in project['elements']:
        name = cube['name']
        light = name == 'cold_lantern_light'
        cruor = any(word in name for word in ('cruor', 'votive_glass', 'reliquary_saddle', 'outer_arch', 'keystone', 'prayer_weight'))
        if not (light or cruor):
            continue
        for face in cube['faces'].values():
            u, v, U, V = face['uv']
            for x in range(min(u,U), max(u,U)):
                for y in range(min(v,V), max(v,V)):
                    r,g,b,a = base.getpixel((x,y))
                    if light:
                        frost.putpixel((x,y), (r,g,b,a))
                    elif r > 1.5 * g:
                        eclipse.putpixel((x,y), (min(160, r+22), g, b, a))
                        frost.putpixel((x,y), (r//4, g//5, b//5, a))
    return frost, eclipse


def main():
    project = json.loads(PROJECT.read_text(encoding='utf-8'))
    png = base64.b64decode(project['textures'][0]['source'].split(',', 1)[1], validate=True)
    base = Image.open(BytesIO(png)).convert('RGBA')
    assert base.size == (project['resolution']['width'], project['resolution']['height'])
    rig = read_rig(project)
    frost, eclipse = glow_atlases(project, base)
    (TEXTURES / 'velorum.png').write_bytes(png)
    frost.save(TEXTURES / 'velorum_glow.png')
    eclipse.save(TEXTURES / 'velorum_cruor_glow.png')
    java = kit.entity_java_source(
        rig, base.size, 'VelorumEntity', 'velorum.py',
        package='com.vincenthuto.hemomancy.client.model.entity.boss.velorum',
        entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.velorum',
        extra_imports='import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;\n'
                      'import com.vincenthuto.hemomancy.common.util.HemoEasing;\n'
                      'import org.joml.Quaternionf;\nimport org.joml.Matrix3f;\n\n'
                      'import static com.vincenthuto.hemomancy.common.util.HemoEasing.catmullRom;\n',
        extra_methods=Path(__file__).with_name('velorum_anim.java').read_text(encoding='utf-8'),
    )
    java = java.replace('public class VelorumModel extends HierarchicalModel<VelorumEntity>',
                        'public class VelorumModel<T extends VelorumEntity> extends HierarchicalModel<T>')
    JAVA.write_text(java.replace('import net.minecraft.world.entity.HumanoidArm;\n', ''), encoding='utf-8')


if __name__ == '__main__':
    main()

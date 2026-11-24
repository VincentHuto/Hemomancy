"""Bake the authored Blockbench rig and UV-matched effect layers for Saint Putriciel."""

import argparse
import base64
from io import BytesIO
import json
import math
import re

from PIL import Image, ImageDraw

from annetta_kit import Rig, entity_java_source
from putriciel import ASSETS, JAVA, ROOT, TEXTURES, fire_java

PROJECT = ASSETS / 'models/entity/bbmodel/bosses/PutricielVestment.bbmodel'
NAME = 'putriciel_vestment'
FLOOR = 24
FLAME = (214, 30, 46, 255)
HEAT = (255, 168, 190, 255)
SOOT = (22, 10, 12, 255)

GROUPS = {
    'saint_putriciel': 'root',
    'staff_hip': 'left_thigh', 'staff_knee': 'left_shin', 'staff_foot': 'left_foot',
    'offering_hip': 'right_thigh', 'offering_knee': 'right_shin', 'offering_foot': 'right_foot',
    'shrouded_head': 'head', 'crown_of_crosses': 'crown', 'fungal_back': 'back_colony',
    'split_back_cloth': 'cape',
    'staff_shoulder': 'left_arm', 'staff_elbow': 'left_forearm', 'staff_grip': 'left_hand',
    'offering_shoulder': 'right_arm', 'offering_elbow': 'right_forearm', 'offering_palm': 'right_hand',
    'processional_iron_staff': 'staff', 'hanging_purifier_censer': 'censer_chain',
}
SHELVES = ('High right bracket', 'High rear bracket', 'Outer right fan', 'Wide left fan',
           'Rear rosette upper', 'Rear rosette lower', 'Sleeve seam growth', 'Left trailing bracket',
           'Low spine bracket', 'Lower seam bracket')
REGROWTH = (2, 3, 8, 7, 4, 5, 6, 5, 1, 0)


def offset(point, pivot):
    return (point[0] - pivot[0], pivot[1] - point[1], point[2] - pivot[2])


def angle(rotation):
    x, y, z = rotation
    return -x, y, -z


def centre(cube):
    return tuple((lo + hi) / 2 for lo, hi in zip(cube['from'], cube['to']))


def slug(name):
    return re.sub(r'[^a-z0-9]+', '_', re.sub(r'-(?=\d)', 'minus ', name.lower())).strip('_')


class Vestment:
    def __init__(self, project):
        self.rig = Rig('putriciel', 'PutricielModel', 'putriciel')
        self.pivots = {'root': (0, 0, 0)}
        self.regions = {}
        self.elements = {cube['uuid']: cube for cube in project['elements']}
        self.parts_by_cube = {}
        self.regrow = {}
        self.anchors = []
        for group in project['outliner']:
            self.group(group, 'root')
        assert len(self.parts_by_cube) == len(self.elements)
        self.effects()

    def part(self, name, parent, pivot, region=None, rotation=(0, 0, 0)):
        self.rig.part(name, parent, offset(pivot, self.pivots[parent]), angle(rotation))
        self.pivots[name] = pivot
        if region:
            self.regions[name] = region
        return name

    def marker(self, name, parent, point):
        return self.part(name, parent, point)

    def group(self, group, parent):
        authored = group['name']
        name = GROUPS.get(authored, authored)
        pivot = tuple(group['origin'])
        if name == 'root':
            assert pivot == (0, 0, 0)
            self.rig.parts[0]['pivot'] = (0, FLOOR, 0)
        elif name == 'censer_chain':
            self.part(name, 'staff_head', pivot)
            self.marker('censer_bowl', name, (-13.07, 27.88, -3.56))
        else:
            if authored == 'chest':
                pivot = (0, 18.1, .3)
            self.part(name, parent, pivot, 'STAFF' if name == 'staff' else None)
        if name == 'staff':
            x, _, z = pivot
            self.marker('staff_foot', name, (x, 0, z))
            self.part('staff_head', name, (x, 34.06, z), 'STAFF')
        for item in group['children']:
            if isinstance(item, dict):
                self.group(item, name)
            else:
                self.cube(self.elements[item], name)

    def cube(self, cube, group):
        assert cube['type'] == 'cube' and cube['box_uv']
        name = cube['name']
        region = None
        parent = group
        pivot = tuple(cube['origin'])
        rotation = cube.get('rotation', (0, 0, 0))
        branch = None
        shelf = next((index for index, label in enumerate(SHELVES) if name.startswith(label + ' ')), None)
        if shelf is not None:
            branch = f'back_shelf_{shelf}'
            region = 'BACK'
            if branch not in self.pivots:
                x, y, z = centre(cube)
                self.part(branch, group, (x, y, z - .75), region)
                self.regrow[branch] = (20.8 + REGROWTH[shelf] * .85, REGROWTH[shelf], shelf in (2, 3))
            parent = branch
        elif name.startswith('Spore nodule '):
            index = int(name[-1]) - 1
            branch = f'back_spore_{index}'
            region = 'BACK'
            self.part(branch, group, centre(cube), region)
            self.regrow[branch] = (21.0 + index * .8, index + 1, False)
            parent = branch
        elif group == 'back_colony':
            region = 'BACK'
        elif group == 'censer_chain':
            region = 'CENSER'
            if not name.startswith('Chain link'):
                parent = 'censer_bowl'
        elif group in ('staff', 'staff_rot'):
            region = 'STAFF'
            if group == 'staff' and (name.startswith(('Reliquary', 'Suspended')) or
                                        name in ('Staff finial', 'Censer hanger')):
                parent = 'staff_head'
            if group == 'staff_rot' and not name.startswith('Pitted scale'):
                branch = 'staff_' + slug(name)
                self.part(branch, group, centre(cube), region)
                self.regrow[branch] = (24.0, 4, False)
                parent = branch
        elif group == 'crown':
            region = 'CROWN'
            if name.startswith('Cross '):
                branch = f'cross_{name.split()[1]}'
                if branch not in self.pivots:
                    self.part(branch, group, pivot, region)
                parent = branch
        elif name.endswith('sloped mantle'):
            branch = 'left_mantle' if name.startswith('Staff') else 'right_mantle'
            self.part(branch, group, pivot, 'SHOULDER')
            parent = branch
            region = 'SHOULDER'
        elif group in ('left_arm', 'right_arm', 'left_forearm', 'right_forearm', 'left_hand'):
            region = 'SHOULDER'
        elif name.startswith('Offering finger') or name == 'Turned thumb':
            limb = 'offering_thumb' if name == 'Turned thumb' else f'offering_finger_{int(name[-1]) - 1}'
            self.part(limb, group, pivot, rotation=rotation)
            parent, rotation = limb, (0, 0, 0)
        elif name == 'Long face shroud':
            self.part('veil', group, pivot, 'CROWN')
            parent = 'veil'
        elif name in ('Fitted lower cassock', 'Cassock chest', 'Iron ordinal clasp', 'Shoulder yoke') or name.endswith('chest orphrey'):
            region = 'CHEST'
        elif group in ('left_thigh', 'right_thigh', 'cape'):
            region = 'HEM'
            if name == 'Upper choir cape':
                branch = 'cape_upper'
            elif name.endswith('torn cape fall') or name.startswith('Burned cape fringe'):
                branch = 'cape_left' if name.startswith('Staff') or 'fringe -1' in name else 'cape_right'
            if branch and branch not in self.pivots:
                self.part(branch, group, pivot, 'HEM')
            parent = branch or group
        if any(rotation):
            parent = self.part('box_' + slug(name), parent, pivot, region, rotation)
        elif region and self.regions.get(parent) != region:
            parent = self.part('box_' + slug(name), parent, pivot, region)

        lo, hi = cube['from'], cube['to']
        dimensions = tuple(round(b - a, 4) for a, b in zip(lo, hi))
        local = offset((lo[0], hi[1], lo[2]), self.pivots[parent])
        self.rig.cube(parent, local + dimensions, name, uv=cube['uv_offset'])
        self.parts_by_cube[name] = (cube, parent, region)

    def site(self, part, point, normal, size, region, tick, palm=False):
        length = math.sqrt(sum(v * v for v in normal))
        self.anchors.append(dict(part=part, point=offset(point, self.pivots[part]),
                                 normal=tuple(v / length for v in normal), size=size,
                                 region=region, kindle=tick, palm=palm))

    def on_cube(self, name, side, size, region, tick, palm=False, cross=0):
        cube, part, _ = self.parts_by_cube[name]
        x, y, z = centre(cube)
        w, h, d = tuple((hi - lo) / 2 for lo, hi in zip(cube['from'], cube['to']))
        if side == 'front':
            point, normal = (x + cross * w, y, z - d), (0, -.45, -1)
        elif side == 'back':
            point, normal = (x + cross * w, y, z + d), (0, -.65, 1)
        else:
            point, normal = (x + cross * w, y + h, z), (0, -1, 0)
        self.site(part, point, normal, size, region, tick, palm)

    def effects(self):
        for i, (y, tick) in enumerate(((9, 28), (17.4, 23), (22.95, 16), (29, 21))):
            name = f'staff_wrap_{i}'
            point = (-9.55, y, -3.56)
            self.part(name, 'staff', point, 'STAFF')
            self.site(name, point, (0, -.9, -.3), .42, 'STAFF', tick)
        for label, tick in (('Staff finial', 26.9), ('Reliquary gable -1', 26.5),
                            ('Reliquary gable 1', 26.5), ('Reliquary stile -1', 25.2),
                            ('Reliquary stile 1', 25.2), ('Head socket rot', 24),
                            ('Rot caught on upper stave', 20), ('Rot packed in ferrule', 28.5)):
            self.on_cube(label, 'top', .45, 'STAFF', tick)
        self.on_cube('Censer iron rim', 'top', .65, 'CENSER', 22)
        self.on_cube('Eschar beneath the colony', 'back', 1.5, 'BACK', 20)
        for i, label in enumerate(SHELVES):
            tick = self.regrow[f'back_shelf_{i}'][0]
            self.on_cube(label + ' cap', 'back', .68, 'BACK', tick)
            if i < 4:
                self.on_cube(label + ' raised root', 'top', .55, 'BACK', tick + .3)
        for i in range(4):
            self.on_cube(f'Spore nodule {i+1}', 'top', .55, 'BACK', 21 + i * .8)
        for side in ('Staff', 'Offering'):
            palm = side == 'Offering'
            for label, tick in (('sloped mantle', 18), ('upper sleeve', 20), ('sleeve cuff', 24)):
                self.on_cube(f'{side} {label}', 'front', .27, 'SHOULDER', tick, palm)
        self.on_cube('Gloved grip', 'front', .21, 'SHOULDER', 26)
        self.on_cube('Extended offering sleeve', 'front', .23, 'SHOULDER', 23, True)
        for i in range(1, 6):
            self.on_cube(f'Cross {i} stem', 'top', .28, 'CROWN', 23 + i)
        self.on_cube('Oxidised iron brow band', 'top', .28, 'CROWN', 27)
        for side in ('Staff', 'Offering'):
            self.on_cube(f'{side} chest orphrey', 'front', .3, 'CHEST', 16)
            self.on_cube(f'{side} embroidered robe fall', 'front', .23, 'HEM', 26)
            self.on_cube(f'{side} torn cape fall', 'back', .23, 'HEM', 27)
        self.on_cube('Iron ordinal clasp', 'front', .35, 'CHEST', 17)
        self.on_cube('Fitted lower cassock', 'front', .3, 'CHEST', 18)
        for rank, anchor in enumerate(sorted(self.anchors, key=lambda a: a['kindle'])):
            anchor['order'] = rank
        for i in range(8):
            if i < 4:
                label = SHELVES[i*2] + ' cap'
                cube, parent, _ = self.parts_by_cube[label]
                x, y, z = centre(cube)
                self.marker(f'spore_point_{i}', parent, (x, y, cube['to'][2]))
            else:
                cube, parent, _ = self.parts_by_cube[f'Spore nodule {i-3}']
                self.marker(f'spore_point_{i}', parent, centre(cube))
        self.marker('face_anchor', 'head', (0, 31, -4.15))

    def layers(self, base, uv_size):
        """Paint only occupied texels. All four overlays share the authored box UVs."""
        glow, char, ember, burn = (Image.new('RGBA', base.size) for _ in range(4))
        gd, cd, ed, bd = (ImageDraw.Draw(layer) for layer in (glow, char, ember, burn))
        scale = base.width // uv_size[0]
        assert base.size == tuple(v * scale for v in uv_size)
        for name, (cube, _, region) in self.parts_by_cube.items():
            for side, face in cube['faces'].items():
                a, b, c, d = [round(v * scale) for v in face['uv']]
                u, v, right, bottom = min(a,c), min(b,d), max(a,c), max(b,d)
                for y in range(v, bottom):
                    for x in range(u, right):
                        r, g, b, alpha = base.getpixel((x, y))
                        if not alpha:
                            continue
                        grain = (x * 43 + y * 59 + (x ^ y) * 13) % 31
                        shade = int((r * .22 + g * .70 + b * .08) * (.35 if region == 'BACK' else .57))
                        cd.point((x, y), fill=(shade + 8, shade + 4, shade + 2, 255))
                        gill = (' pores' in name and side == 'down') or name.startswith('Spore nodule')
                        if gill and grain < (7 if ' pores' in name else 13):
                            gd.point((x,y), fill=(87, 139, 58, 255) if grain > 2 else (144, 161, 91, 255))
                        hearth = ((name == 'Burned open palm' and side == 'up')
                                  or (name in ('Censer iron rim', 'Stepped pierced lid') and side != 'down')
                                  or (name == 'Iron ordinal clasp' and side == 'north')
                                  or name.startswith('Spore nodule'))
                        if hearth and grain < 11:
                            ed.point((x,y), fill=HEAT if grain < 3 else FLAME)
                        metal = region in ('STAFF', 'CENSER', 'CROWN')
                        seam = x in (u, right-1) or y in (v, bottom-1)
                        if region and ((seam and grain < (8 if metal else 5))
                                       or (grain < 2 and (metal or region == 'BACK'))):
                            bd.point((x,y), fill=HEAT if hearth and grain < 3 else SOOT if grain == 0 else FLAME)
        return glow, char, ember, burn

    def java(self, size):
        growth = [dict(part=part, kindle=tick, order=max(0, min(14, round(tick - 16))),
                       step=step, eaten=eaten) for part, (tick, step, eaten) in self.regrow.items()]
        fields, methods = fire_java(self.rig, self.anchors, growth, self.regions)
        return entity_java_source(
            self.rig, size, 'PutricielEntity', 'putriciel_vestment.py',
            package='com.vincenthuto.hemomancy.client.model.entity.boss.putriciel',
            entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel',
            extra_imports='import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                          'import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;\n'
                          'import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;\n'
                          'import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;\n'
                          'import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;\n'
                          'import com.vincenthuto.hemomancy.common.util.HemoEasing;\n'
                          'import com.mojang.blaze3d.vertex.VertexConsumer;\n'
                          'import org.joml.Quaternionf;\nimport org.joml.Matrix3f;\n'
                          'import static com.vincenthuto.hemomancy.common.util.HemoEasing.catmullRom;\n',
            extra_fields=fields,
            extra_methods=methods + (ROOT / 'tools/model_export/putriciel_anim.java').read_text(encoding='utf-8')
        ).replace('public class PutricielModel extends HierarchicalModel<PutricielEntity>',
                  'public class PutricielModel<T extends PutricielEntity> extends HierarchicalModel<T>'
                  ).replace('import net.minecraft.world.entity.HumanoidArm;\n', '')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='Verify the checked-in Java and masks match the project')
    check = parser.parse_args().check
    project = json.loads(PROJECT.read_text(encoding='utf-8'))
    size = tuple(project['resolution'][side] for side in ('width', 'height'))
    base = base64.b64decode(project['textures'][0]['source'].split(',', 1)[1])
    vestment = Vestment(project)
    assert (TEXTURES / f'{NAME}.png').read_bytes() == base, 'Sync the embedded and standalone base texture'
    files = {JAVA: vestment.java(size).encode('utf-8')}
    for suffix, image in zip(('glow', 'charred', 'embers', 'burning'),
                             vestment.layers(Image.open(BytesIO(base)).convert('RGBA'), size)):
        output = BytesIO()
        image.save(output, format='PNG')
        files[TEXTURES / f'{NAME}_{suffix}.png'] = output.getvalue()
    for path, data in files.items():
        if check:
            assert path.read_bytes().replace(b'\r\n', b'\n') == data if path == JAVA else path.read_bytes() == data, path
        else:
            path.write_bytes(data)
    print(f'Putriciel: {len(vestment.elements)} cubes, {len(vestment.anchors)} fire anchors and four fitted effect masks')


if __name__ == '__main__':
    main()

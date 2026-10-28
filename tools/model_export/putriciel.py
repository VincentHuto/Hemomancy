"""Putriciel's fungal reliquary model, based on the supplied Rot and Offering sheet.

python tools/model_export/putriciel.py --preview

Writes the Java rig, portable Blockbench source, fitted base/emissive atlases,
and review images. All saved cube geometry and pivots use quarter units.
"""
import argparse
import base64
import json
import math
import subprocess

from PIL import Image, ImageDraw
import annetta_kit as kit
from annetta_kit import Rig, ROOT, ASSETS, layout, paint_atlases, entity_java_source, quads, add, mix
from hemorath import snap, euler, chain

kit.SCALE = 2
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/putriciel/PutricielModel.java'
TEXTURES = ASSETS / 'textures/entity/boss/putriciel'
BBMODEL = ASSETS / 'models/entity/bbmodel/bosses/PutricielModel.bbmodel'


def shelf(r, name, parent, pivot, width=4.5, depth=3, red=False, rotation=(0, 0, 0)):
    r.part(name, parent, pivot, rotation)
    material = 'red_cap' if red else 'shelf_cap'
    r.cube(name, (-width / 2, -.5, -depth / 2, width, .75, depth), material, cap_width=width, cap_depth=depth)
    r.cube(name, (-(width - 1) / 2, -1, -(depth - .5) / 2, width - 1, .5, depth - .5),
           material, cap_width=width, cap_depth=depth)
    r.cube(name, (-(width - 1) / 2, .25, -(depth - .5) / 2, width - 1, .25, depth - .5), 'gills')
    r.cube(name, (-.5, .25, depth / 2 - 1, 1, 1, 1), 'flesh')


def mushroom(r, name, parent, pivot, height=2.5):
    r.part(name, parent, pivot)
    r.cube(name, (-.25, -height, -.25, .5, height, .5), 'flesh')
    shelf(r, name + '_cap', name, (0, -height, 0), 3, 2.5, True)


def spore(r, name, parent, pivot, diameter=3.5, index=0):
    r.part(name, parent, pivot, (0, 0, (index - 1) * 7))
    narrow = diameter - 1
    for w, h, d in ((narrow, diameter, narrow), (diameter, narrow, narrow), (narrow, narrow, diameter)):
        r.cube(name, (-w / 2, -h / 2, -d / 2, w, h, d), 'spore', spore=index)
    r.cube(name, (-.5, diameter / 2 - .25, -.5, 1, 1.25, 1), 'blood')


def arm(r, side, sign):
    name = f'{side}_arm'
    r.part(name, 'chest', (sign * 5.5, -5.5, 0), (-6 if sign > 0 else -38, 0, -35 if sign > 0 else 40))
    r.cube(name, (-1.75, -1.25, -1.75, 3.5, 3.5, 3.5), 'flesh')
    r.cube(name, (-1.25, 1.5, -1.25, 2.5, 5, 2.5), 'flesh')
    r.cube(name, (-1.75, -.5, 1, 3.5, 8, .75), 'blood_cloth')
    fore = f'{side}_forearm'
    r.part(fore, name, (0, 6, 0), (0 if sign > 0 else -46, 0, -105 if sign > 0 else -10))
    r.cube(fore, (-1.25, -.75, -1.25, 2.5, 7, 2.5), 'flesh')
    r.cube(fore, (-1.5, 4.75, -1.5, 3, .75, 3), 'iron')
    hand = f'{side}_hand'
    r.part(hand, fore, (0, 6.5, 0))
    if sign > 0:
        r.cube(hand, (-1.25, -.5, -.75, 2.5, 2.75, 1.5), 'flesh')
        for i in range(4):
            finger = f'gripping_finger_{i}'
            r.part(finger, hand, (-1 + i * .5, 1.5, -.5), (-75, 0, 0))
            r.cube(finger, (-.25, -.25, -.25, .5, 1.75, .5), 'bone')
            r.part(finger + '_tip', finger, (0, 1.5, 0), (-90, 0, 0))
            r.cube(finger + '_tip', (-.25, 0, -.25, .5, 1.25, .5), 'bone')
    else:
        # A palm-up offering hand, with crooked fingers and long hanging crimson drips.
        world = kit.transforms(r)
        next(p for p in r.parts if p['name'] == hand)['rotation'] = euler(world[fore][:3, :3].T)
        r.cube(hand, (-2, -.75, -2, 4, 1.5, 3.5), 'flesh')
        for i in range(4):
            finger = f'offering_finger_{i}'
            r.part(finger, hand, (-1.5 + i, -.25, -1.75), (0, (i - 1) * 7, 0))
            r.cube(finger, (-.25, -.25, -2.25, .5, .75, 2.5), 'bone')
            r.part(finger + '_tip', finger, (0, 0, -2), (-32, 0, 0))
            r.cube(finger + '_tip', (-.25, -.25, -1.5, .5, .75, 1.75), 'bone')
        r.part('offering_thumb', hand, (-1.75, 0, -.5), (0, 35, -15))
        r.cube('offering_thumb', (-1.75, -.25, -.25, 2, .75, .75), 'bone')
        r.part('offering_drips', hand)
        for i in range(7):
            x = -1.5 + i * .5
            r.cube('offering_drips', (x, .75, -2.5 + (i % 3) * .5, .25, 3.5 + (i * 3 % 5), .25), 'blood')
        shelf(r, 'knuckle_growth', hand, (1.25, -.75, .5), 2.5, 2)
    shelf(r, f'{side}_elbow_growth', fore, (0, .75, 1), 4, 2.5)


def staff(r):
    world = kit.transforms(r)
    hand = world['left_hand']
    pivot = (0, 1, -1)
    bottom = snap(24 - (hand[:3, :3] @ pivot + hand[:3, 3])[1])
    r.part('staff', 'left_hand', pivot, euler(hand[:3, :3].T))
    r.cube('staff', (-.5, -15, -.5, 1, bottom + 15, 1), 'iron')
    r.cube('staff', (-1, bottom - .75, -1, 2, .75, 2), 'iron')
    for y in (-12, -7, -2, 4, 9, bottom - 1.5):
        r.cube('staff', (-.75, y, -.75, 1.5, .75, 1.5), 'iron')
    r.part('staff_head', 'staff', (0, -13, 0))
    r.cube('staff_head', (-4.5, -.5, -.5, 9, .75, 1), 'iron')
    r.cube('staff_head', (-3, -3.5, -.5, 6, .75, 1), 'iron')
    for x, height in ((-3, 4.5), (0, 6), (3, 5)):
        r.cube('staff_head', (x - .25, -height, -.25, .5, height + .75, .5), 'iron')
        r.cube('staff_head', (x - .75, -height + 1, -.25, 1.5, .5, .5), 'iron')
    for side, x, count in (('left', -4, 6), ('right', 4, 8)):
        name = 'staff_chain_' + side
        chain(r, name, 'staff_head', (x, .25, 0), count)
        censer = 'censer_' + side
        r.part(censer, name, (0, count * 1.5, 0))
        r.cube(censer, (-1.25, 0, -1.25, 2.5, .5, 2.5), 'iron')
        r.cube(censer, (-1.5, -1.5, -1.5, 3, .5, 3), 'iron')
        for sx in (-1.25, 1):
            for sz in (-1.25, 1):
                r.cube(censer, (sx, -2.75, sz, .25, 3.25, .25), 'iron')
        r.cube(censer, (-.75, -.5, -.75, 1.5, .5, 1.5), 'offering')
        for i in range(3):
            r.cube(censer, (-.75 + i * .75, .5, -.25, .25, 1.5 + i, .25), 'blood')
    for i, y in enumerate((-9, 6, 12)):
        shelf(r, f'staff_growth_{i}', 'staff', (0, y, .5), 3, 2)


def saint():
    r = Rig('putriciel', 'PutricielModel', 'putriciel')
    r.part('hips', pivot=(0, 11, 0))
    r.cube('hips', (-4, -.75, -3, 8, 5, 6), 'flesh')
    r.part('chest', 'hips', (0, -8, 0), (8, 0, -3))
    r.cube('chest', (-4.25, -6.5, -3, 8.5, 12, 6), 'blood_cloth')
    r.cube('chest', (-3, -6.75, -3.25, 6, 10, .5), 'flesh')
    r.cube('chest', (-4.75, -6.5, -2.25, 9.5, 3, 4.5), 'flesh')
    r.cube('chest', (-3, -7.75, -2.5, 6, .75, 5), 'iron')
    for side, sign in (('left', 1), ('right', -1)):
        name = f'{side}_collar'
        r.part(name, 'chest', (sign * 3.5, -6.75, -3.25), (0, 0, sign * 28))
        r.cube(name, (-1, 0, -.25, 2, 5.5, .5), 'ivory_cloth')
        for i in range(4):
            r.cube(name, (-1 + i * .5, 5, -.25, .25, 1 + i * .5, .5), 'ivory_cloth')
        mantle = f'{side}_mantle'
        r.part(mantle, 'chest', (sign * 4, -6.75, 0), (0, 0, -sign * 15))
        r.cube(mantle, (-1.75, -.5, -2.75, 3.5, 1.5, 5.5), 'ivory_cloth')
        for i in range(4):
            r.cube(mantle, (-1.5 + i, 1, -2.75, .5, 1.5 + (i % 3), .5), 'ivory_cloth')
        arm(r, side, sign)
        chain(r, f'{side}_vestment_chain', 'chest', (sign * 4.5, -3, -2.5), 9 if sign > 0 else 7)
        for i in range(3 if sign > 0 else 5):
            shelf(r, f'{side}_shoulder_shelf_{i}', 'chest',
                  (sign * (4.25 + (i % 2) * .75), -6.5 + i * 1.5, 1),
                  4 + (i % 2), 3, rotation=(0, sign * 25, sign * -8))
    r.part('spore_cluster', 'chest')
    for i, (x, y, z, d) in enumerate(((-1.5, -.5, -4, 4), (1.75, 1.5, -4.25, 3.5),
                                      (-1.25, 4, -4.25, 3.5), (1.5, 5, -3.75, 3))):
        spore(r, f'spore_{i}', 'spore_cluster', (x, y, z), d, i)
    spore(r, 'shoulder_spore', 'right_arm', (-1.75, -.75, 0), 3.5, 4)

    # A hollow face inside a ragged mycelial hood, surmounted by a tall open reliquary crown.
    r.part('head', 'chest', (0, -8.5, -1), (6, 0, 0))
    r.cube('head', (-2.25, -4.75, -1.75, 4.5, 4.75, 3.5), 'void')
    r.cube('head', (-3.25, -5.75, -2.75, 6.5, .75, 5.5), 'ivory_cloth')
    r.cube('head', (-3.25, -5, -2.75, .75, 6, 5.5), 'flesh')
    r.cube('head', (2.5, -5, -2.75, .75, 6, 5.5), 'flesh')
    r.cube('head', (-2.5, .25, -2.75, 5, .75, 5.5), 'iron')
    r.part('face_vines', 'head')
    for i in range(9):
        r.cube('face_vines', (-2 + i * .5, -4.75, -3 + (i % 2) * .25, .25, 4.5 + (i * 2 % 5), .25),
               'blood' if i % 3 else 'root')
    r.part('crown', 'head', (0, -5.75, 0))
    for y in (0, -4):
        for box in ((-3, y, -2.5, 6, .5, .5), (-3, y, 2, 6, .5, .5),
                    (-3, y, -2, .5, .5, 4), (2.5, y, -2, .5, .5, 4)):
            r.cube('crown', box, 'iron')
    for i, (x, z, height) in enumerate(((-3, -2, 7), (3, -2, 8.5), (-3, 2, 6), (3, 2, 7.5),
                                        (0, -2.25, 6), (0, 2.25, 9))):
        name = f'crown_spire_{i}'
        r.part(name, 'crown', (x, 0, z), (0, 0, (i % 3 - 1) * 4))
        r.cube(name, (-.25, -height, -.25, .5, height, .5), 'iron')
        r.cube(name, (-1, -height + 1, -.25, 2, .5, .5), 'iron')
        r.cube(name, (-.75, -height + 2.5, -.25, 1.5, .5, .5), 'iron')
    shelf(r, 'head_shelf_left', 'head', (2.75, -2.5, .75), 4, 3)
    shelf(r, 'head_shelf_right', 'head', (-2.75, -3.5, .75), 4.5, 3)
    mushroom(r, 'crown_mushroom', 'head', (-2.5, -4.5, 1.5), 3)
    mushroom(r, 'shoulder_mushroom', 'right_arm', (-1, -1.5, 1), 3)
    staff(r)

    r.part('back_colony', 'chest')
    for i, (x, y, z) in enumerate(((-3.5, -7, 3), (1, -6, 3.5), (4, -4, 2.75), (-3, -3, 3.75),
                                    (1, -1, 4), (3.5, 1, 3.75), (-3, 3, 3.75), (.5, 5, 4),
                                    (4, 7, 3.5), (-3.5, 9, 3), (0, 10.5, 3.5))):
        shelf(r, f'back_shelf_{i}', 'back_colony', (x, y, z), 4.5 + (i % 3) * .5, 3.5)
    mushroom(r, 'back_mushroom', 'back_colony', (2, -5, 4), 4)
    for i in range(14):
        angle = i * 360 / 14
        a = math.radians(angle)
        name = f'robe_panel_{i}'
        length = 11 + (i * 2 % 3)
        r.part(name, 'hips', (snap(math.sin(a) * 3.5), 0, snap(math.cos(a) * 2.5)),
               (math.cos(a) * 10, angle, -math.sin(a) * 10))
        material = 'ivory_cloth' if i % 4 == 0 else 'blood_cloth'
        r.cube(name, (-1.75, -.5, -.25, 3.5, length, .5), material)
        r.cube(name, (-1.75, length - .5, -.25, 1.25, 1.5, .5), material)
    for i, (x, y, z) in enumerate(((-4.5, 4, 2), (-5, 7, 2), (-5.5, 10, 2.5),
                                    (4.5, 5, 2.5), (5, 8, 2), (5.5, 11, 2.5))):
        shelf(r, f'hem_shelf_{i}', 'hips', (x, y, z), 5 + (i % 2), 3.5)
    spore(r, 'hem_spore', 'hips', (4.5, 9, -1.5), 3.5, 5)
    r.part('ground_roots', pivot=(0, 23.75, 0))
    for i in range(8):
        a = math.radians(i * 45)
        name = f'root_trail_{i}'
        r.part(name, 'ground_roots', (snap(math.sin(a) * 3), 0, snap(math.cos(a) * 2.5)), (0, i * 45, 0))
        r.cube(name, (-.5, -.25, 0, 1, .5, 3 + (i % 3)), 'root')
        r.cube(name, (-.75, -.25, 2.5, 1.5, .5, 2), 'blood_cloth')
    for i, (x, y, z) in enumerate(((-3, -5, 4), (3, -2, 4), (-4, 3, 3.5), (2, 6, 4))):
        mushroom(r, f'growth_mushroom_{i}', 'back_colony', (x, y, z), 3 + i * .5)
        name = f'rot_drip_{i}'
        r.part(name, 'back_colony', (x, y + 1, z))
        r.cube(name, (-.25, 0, -.25, .5, 3 + i, .5), 'blood')
        r.cube(name, (-.5, 3 + i, -.5, 1, 1, 1), 'blood')
    for part in r.parts:
        assert all(snap(v) == v for v in part['pivot']), part['name']
        for cube in part['cubes']:
            assert all(snap(v) == v for v in cube['box']), cube
            assert min(cube['box'][3:]) >= .25, cube
    return r


def paint(cube, t, glow=False):
    mat = cube['material']
    u, v, n = t['u'], t['v'], (t['n'] - .5) * 12
    x, y, z = t['lx'], t['ly'], t['lz']
    lit = False
    if mat == 'spore':
        cells = kit.smooth(t['gx'], t['gy'], t['gz'], scale=.75, seed=cube['spore'])
        rgb = mix((165, 144, 77), (227, 204, 139), cells)
        if cells < .29:
            rgb = (118, 100, 49)
        if t['n'] > .94 and cells < .57:
            rgb = (84, 73, 33)
        lit = cells > .66 and t['n'] > .58
        if t['n'] > .93:
            rgb = (242, 226, 167)
    elif mat in ('shelf_cap', 'red_cap'):
        radius = math.sqrt((x / (cube['cap_width'] / 2)) ** 2 + (z / (cube['cap_depth'] / 2)) ** 2)
        if mat == 'shelf_cap':
            band = int(radius * 8) % 4
            rgb = ((105, 86, 42), (130, 107, 53), (88, 77, 35), (148, 121, 63))[band]
            if radius > .83:
                rgb = (169, 141, 79)
        else:
            rgb = (82, 76, 39) if radius < .8 else (164, 148, 91)
            if t['n'] > .9:
                rgb = (220, 184, 146)
        rgb = add(rgb, n)
    elif mat == 'gills':
        rgb = (127, 106, 62) if (u + v * 2) % 4 else (84, 72, 36)
    elif mat == 'flesh':
        decay = kit.smooth(t['gx'], t['gy'], t['gz'], scale=1.5, seed=3)
        rgb = add(mix((62, 66, 33), (105, 88, 47), decay), n)
        if t['n'] > .94:
            rgb = (78, 70, 34)
    elif mat == 'iron':
        patina = kit.smooth(t['gx'], t['gy'], t['gz'], scale=2, seed=7)
        rgb = mix((61, 62, 29), (176, 133, 51), patina)
        if u == 0 or v == 0:
            rgb = add(rgb, 22)
        if (u + v * 3) % 13 == 0:
            rgb = (72, 46, 26)
    elif mat == 'void':
        rgb = (20, 24, 13)
    elif mat == 'bone':
        rgb = add((197, 181, 145), n)
        if u == 0:
            rgb = (126, 108, 68)
    elif mat in ('blood', 'root'):
        rgb = add((66, 62, 29) if mat == 'blood' else (80, 70, 32), n)
        if u == 0:
            rgb = add(rgb, 18)
        lit = False
    elif mat == 'offering':
        rgb, lit = (208, 61, 30), True
    else:
        pale = mat == 'ivory_cloth'
        rgb = (214, 197, 163) if pale else (63, 60, 36)
        rgb = add(rgb, (-16, -4, 5, -6)[u % 4] + n)
        if t['side'] and v > t['fh'] - 4:
            rgb = mix(rgb, (91, 72, 38) if pale else (47, 25, 22), .5)
        if pale and (u + v // 4) % 11 == 0:
            rgb = (78, 72, 39)
        elif u == t['fw'] - 1:
            rgb = (149, 122, 56)
    return (*rgb, 255) if not glow else ((255, 233, 145, 255) if lit else (0, 0, 0, 0))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = saint()
    size = layout(rig, 256)
    base, glow = paint_atlases(rig, size, [paint, lambda c, t: paint(c, t, True)])
    TEXTURES.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURES / 'putriciel.png')
    glow.save(TEXTURES / 'putriciel_glow.png')
    source = entity_java_source(rig, size, 'PutricielEntity', 'putriciel.py',
        package='com.vincenthuto.hemomancy.client.model.entity.boss.putriciel',
        entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel',
        extra_imports='import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;\n'
                      'import org.joml.Quaternionf;\nimport org.joml.Matrix3f;\n',
        extra_fields='',
        extra_methods=(ROOT / 'tools/model_export/putriciel_anim.java').read_text())
    source = source.replace('public class PutricielModel extends HierarchicalModel<PutricielEntity>',
                            'public class PutricielModel<T extends PutricielEntity> extends HierarchicalModel<T>')
    source = source.replace('import net.minecraft.world.entity.HumanoidArm;\n', '')
    JAVA.write_text(source, encoding='utf-8')
    subprocess.run(['node', str(ROOT / 'tools/model_export/java_model_to_bbmodel.mjs'), '--source', str(JAVA),
                    '--texture', 'textures/entity/boss/putriciel/putriciel.png', '--output', str(BBMODEL)],
                   cwd=ROOT, check=True)
    project = json.loads(BBMODEL.read_text())
    project['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(
        (TEXTURES / 'putriciel.png').read_bytes()).decode('ascii')
    BBMODEL.write_text(json.dumps(project, indent=2) + '\n', encoding='utf-8')
    print(f'Putriciel: {sum(len(p["cubes"]) for p in rig.parts)} cubes; {len(rig.parts)} joints; atlas {base.size}')
    if args.preview:
        def posed(overrides):
            world = kit.transforms(rig, overrides)
            for name, parent in (('staff', 'left_hand'), ('right_hand', 'right_forearm')):
                part = next(p for p in rig.parts if p['name'] == name)
                rotation = euler(world[parent][:3, :3].T)
                overrides[name] = tuple(a - b for a, b in zip(rotation, part['rotation']))
            return quads(rig, overrides=overrides)
        stable = posed({})
        nova = posed({'chest': (-10, 0, 0), 'right_arm': (-18, 0, 20)})
        absolved = posed({'chest': (14, 0, 0), 'head': (10, 0, 0), 'right_arm': (10, 0, 5)})
        kit.preview_sheet([(stable, base, 0, 0), (stable, base, -.65, -.1), (stable, base, math.pi, 0),
                           (stable, base, math.pi / 2, 0), (nova, base, -.3, 0), (absolved, base, -.3, 0)],
                          ROOT / 'build/putriciel_preview.png', cols=3)
        preview = Image.open(ROOT / 'build/putriciel_preview.png')
        draw = ImageDraw.Draw(preview)
        for i, label in enumerate(('FRONT', 'THREE QUARTER', 'BACK', 'SIDE', 'ROT NOVA', 'ABSOLUTION')):
            draw.text(((i % 3) * 420 + 14, (i // 3) * 420 + 12), label, fill=(223, 202, 157))
        preview.save(ROOT / 'build/putriciel_preview.png')
        kit.render_quads(stable, base, .35, -.08, 1100).save(ROOT / 'build/putriciel_portrait.png')


if __name__ == '__main__':
    main()

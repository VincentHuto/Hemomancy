"""Velorum's veiled frost saint, authored from the supplied Shadow and Frost sheet.

python tools/model_export/velorum.py --preview

Writes the Java rig, portable Blockbench project, fitted base/glow atlases and
review renders. Saved geometry and pivots use the shared quarter-unit grid.
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
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/velorum/VelorumModel.java'
TEXTURES = ASSETS / 'textures/entity/boss/velorum'
BBMODEL = ASSETS / 'models/entity/bbmodel/bosses/VelorumModel.bbmodel'


def icicle(r, parent, x, y, z, length=3):
    r.cube(parent, (x - .25, y, z - .25, .5, length - .75, .5), 'ice')
    r.cube(parent, (x - .25, y + length - .75, z - .25, .25, .75, .25), 'ice')


def lantern(r, name, parent, pivot, small=False):
    r.part(name, parent, pivot)
    half, height = (1.25, 3.5) if small else (2, 5.5)
    for y in (0, -height):
        r.cube(name, (-half - .25, y, -half - .25, half * 2 + .5, .5, half * 2 + .5), 'iron')
    # Open bars around the floating crystal rather than an opaque blue box.
    for x in (-half, half - .25):
        for z in (-half, half - .25):
            r.cube(name, (x, -height, z, .25, height, .25), 'iron')
    r.cube(name, (-half, -height - .5, -half, half * 2, .5, half * 2), 'iron')
    r.cube(name, (-half + .5, -height - 1, -half + .5, half * 2 - 1, .5, half * 2 - 1), 'iron')
    r.cube(name, (-.25, -height - 3, -.25, .5, 2, .5), 'iron')
    r.cube(name, (-1, -height - 2.5, -.25, 2, .5, .5), 'iron')
    r.part(name + '_crystal', name, (0, -height / 2 if small else -2.75, 0), (0, 45, 15))
    crystal = name + '_crystal'
    r.cube(crystal, (-.5, -1, -.5, 1, 2, 1), 'crystal')
    r.cube(crystal, (-.25, -1.5, -.25, .5, 3, .5), 'crystal')
    for i, (x, z) in enumerate(((-half, -half), (half, -half), (-half, half), (half, half))):
        icicle(r, name, x, .5, z, 2 + i * .5)
    if not small:
        for x in (-half, half):
            icicle(r, name, x, -height + .5, 0, 3)
            r.cube(name, (x - .25, -height - 2, -.25, .5, 2, .5), 'iron')


def arm(r, side, sign):
    name = f'{side}_arm'
    r.part(name, 'chest', (sign * 4.5, -5.25, 0), (-5, 0, -38 if sign > 0 else 55))
    r.cube(name, (-1.5, -1, -1.75, 3, 3, 3.5), 'shadow')
    r.cube(name, (-.75, 1, -.75, 1.5, 6.5, 1.5), 'bone')
    r.cube(name, (-1.75, -.75, -2, 3.5, 1, 4), 'iron')
    for i in range(3):
        r.cube(name, (-1.75, i * 1.25, -2, 3.5, .5, .75), 'silver')
        icicle(r, name, -1.5 + i * 1.5, .5, -2, 1.5 + i * .5)
    r.cube(name, (-2, -.5, -1.75, 4, 2, 3.5), 'mantle')
    r.cube(name, (-2, -.75, -1.75, 4, .25, 3.5), 'veil')
    for i in range(4):
        r.cube(name, (-2 + i, 1.5, -1.75, .5, 1.5 + (i % 2), .5), 'veil')
    fore = f'{side}_forearm'
    r.part(fore, name, (0, 6.5, 0), (-15, 0, -85 if sign > 0 else 90))
    r.cube(fore, (-.75, -.5, -.5, .5, 7, 1), 'bone')
    r.cube(fore, (.25, -.5, -.5, .5, 7, 1), 'bone')
    r.cube(fore, (-1, 5.5, -.75, 2, .75, 1.5), 'iron')
    hand = f'{side}_hand'
    r.part(hand, fore, (0, 6.5, 0), (-10, 0, 0 if sign > 0 else 15))
    r.cube(hand, (-1, -.5, -.5, 2, 2.5, 1), 'bone')
    for i, length in enumerate((2, 2.75, 3, 2.25)):
        finger = f'{side}_finger_{i}'
        r.part(finger, hand, (-.75 + i * .5, 1.5, -.25),
               (-75 if sign > 0 else -18, 0, 0 if sign > 0 else (i - 1.5) * 13))
        r.cube(finger, (-.25, -.25, -.25, .5, 1.5 if sign > 0 else length, .5), 'bone')
        r.part(finger + '_tip', finger, (0, 1.25 if sign > 0 else length - .25, 0),
               (-90 if sign > 0 else -32, 0, 0))
        r.cube(finger + '_tip', (-.25, 0, -.25, .5, 1.25, .5), 'bone')
    r.part(side + '_thumb', hand, (sign * 1, 0, 0), (-35, 0, -sign * 45))
    r.cube(side + '_thumb', (-.25, 0, -.25, .5, 2.25, .5), 'bone')
    # These cuffs counter the arm rotations, so their shredded cloth and ice hang down.
    world = kit.transforms(r)
    sleeve = side + '_sleeve'
    r.part(sleeve, fore, (0, 3.5, .5), euler(world[fore][:3, :3].T))
    for i in range(6):
        length = 7 + (i * 3 % 5)
        r.cube(sleeve, (-2.25 + i * .75, 0, -.5 + (i % 2) * .25, .75, length, .5), 'veil')
        r.cube(sleeve, (-2.25 + i * .75, length, -.5 + (i % 2) * .25, .25, 1.5, .5), 'veil')
        if i % 2 == 0:
            icicle(r, sleeve, -2 + i * .75, length - .5, -.5, 2.5)
    if sign < 0:
        r.part('claw_ice', hand, (0, 1.5, 0), euler(world[hand][:3, :3].T))
        for i in range(3):
            icicle(r, 'claw_ice', -1 + i, 0, -.5, 2.5 + i * .75)


def saint():
    r = Rig('velorum', 'VelorumModel', 'velorum')
    r.part('hips', pivot=(0, 10, 0))
    r.cube('hips', (-3, -1, -2.25, 6, 8, 4.5), 'shadow')
    r.part('chest', 'hips', (0, -8, 0), (3, 0, 0))
    r.cube('chest', (-3.5, -6, -2.5, 7, 12, 5), 'shadow')
    r.cube('chest', (-2.5, -6.5, -2.75, 5, 1, 5.5), 'silver')
    for side, sign in (('left', 1), ('right', -1)):
        r.part(side + '_stole', 'chest', (sign * 2.75, -6, -2.75), (0, 0, -sign * 8))
        r.cube(side + '_stole', (-.5, 0, -.25, 1, 16, .5), 'veil')
        for i in range(3):
            icicle(r, side + '_stole', -.25 + i * .25, 15, -.25, 2 + i)
        arm(r, side, sign)
    # Scalloped chains crossing the dark vestments; individual links remain open.
    r.part('pectoral_chains', 'chest')
    for row in range(3):
        for i in range(7):
            x = -3 + i
            y = snap(-4 + row * 2 + (3 - abs(x)) * .6)
            name = f'chest_link_{row}_{i}'
            r.part(name, 'pectoral_chains', (x, y, -3), (0, 0, -x * 15))
            for box in ((-.5, -.25, -.25, .25, 1, .5), (.25, -.25, -.25, .25, 1, .5),
                        (-.25, -.25, -.25, .5, .25, .5), (-.25, .5, -.25, .5, .25, .5)):
                r.cube(name, box, 'silver')
    chain(r, 'waist_chain', 'chest', (0, 4, -3), 7)
    r.part('breast_reliquary', 'chest', (0, 3.5, -3.5), (0, 0, 45))
    r.cube('breast_reliquary', (-.75, -.75, -.25, 1.5, 1.5, .5), 'silver')
    r.cube('breast_reliquary', (-.25, -.25, -.5, .5, .5, .25), 'crystal')
    chain(r, 'side_chain', 'chest', (-5, -5.5, 1), 10)
    lantern(r, 'side_lantern', 'side_chain', (0, 19, 0), True)

    r.part('head', 'chest', (0, -8, -.5), (4, 0, 0))
    r.cube('head', (-2.25, -5.5, -1.75, 4.5, 6, 3.5), 'void')
    r.cube('head', (-2.75, -6.5, -2.25, 5.5, 1, 4.5), 'veil')
    r.cube('head', (-2, -7, -1.75, 4, .5, 3.5), 'veil')
    r.part('back_veil', 'head')
    for i in range(7):
        length = 9 + (i * 2 % 4) * .5
        r.cube('back_veil', (-2.5 + i * .75, -5.75, 1.75, .75, length, .5), 'veil')
        r.cube('back_veil', (-2.5 + i * .75, length - 5.75, 1.75, .25, 1.5, .5), 'veil')
    r.part('face_veil', 'head')
    for i in range(9):
        name = f'veil_strip_{i}'
        r.part(name, 'face_veil', (-2.75 + i * .75, -5.75, -2.5 - (i % 2) * .25),
               (0, 0, (i - 4) * -1.5))
        length = 6.5 + (i * 3 % 5) * .5
        r.cube(name, (-.25, 0, -.25, .75, length, .5), 'veil')
        r.cube(name, (-.25, length, -.25, .25, .75 + (i % 3) * .5, .5), 'veil')
    for sign in (-1, 1):
        name = 'cowl_left' if sign > 0 else 'cowl_right'
        r.part(name, 'head', (sign * 2.5, -5, 0), (-4, 0, -sign * 9))
        r.cube(name, (-.5, -.5, -2, 1, 10, 4), 'veil')
        r.cube(name, (-.5, 9.5, 0, .5, 2.5, 2), 'veil')
    r.part('halo', 'head', (0, -3.5, 3))
    for i in range(20):
        angle = math.radians(i * 18)
        name = f'halo_segment_{i}'
        r.part(name, 'halo', (snap(math.sin(angle) * 7.25), snap(-math.cos(angle) * 7.25), 0), (0, 0, i * 18))
        r.cube(name, (-1.25, -.25, -.25, 2.5, .5, .5), 'iron')
        if i % 2 == 0:
            r.cube(name, (-.25, -2.25, -.25, .5, 2.5, .5), 'silver')
            r.cube(name, (-.75, -1.5, -.25, 1.5, .25, .5), 'iron')
    for i, x in enumerate((-7.25, -5, 5, 7.25)):
        chain(r, f'halo_chain_{i}', 'halo', (x, 0, 0), 2 + i % 2)
        icicle(r, f'halo_chain_{i}', 0, 3 + (i % 2) * 1.5, 0, 2 + (i % 2))

    # A dark inner skirt and overlapping wider mantle strips form a flared, ragged train.
    for i in range(16):
        a = math.radians(i * 22.5)
        name = f'robe_panel_{i}'
        r.part(name, 'hips', (snap(math.sin(a) * 3), 0, snap(math.cos(a) * 2)), (8, i * 22.5, 0))
        length = 12 + (i % 3) * .5
        r.cube(name, (-1.25, -1, -.25, 2.5, length, .5), 'shadow')
        r.cube(name, (-1.25, length - 1, -.25, .75, 1.5, .5), 'shadow')
    for i in range(9):
        a = math.radians(-95 + i * 23.75)
        name = f'mantle_panel_{i}'
        r.part(name, 'chest', (snap(math.sin(a) * 3.5), -5.5, snap(math.cos(a) * 2.5)),
               (12, math.degrees(a), 0))
        material = 'veil' if i in (0, 8) else 'mantle'
        r.cube(name, (-1.5, 0, 0, 3, 16, .5), material)
        r.part(name + '_train', name, (0, 15.5, 0), (12, 0, 0))
        r.cube(name + '_train', (-1.75, 0, 0, 3.5, 12, .5), material)
        for j in range(3):
            r.cube(name + '_train', (-1.75 + j * 1.25, 11, 0, .75, 1.25 + (j % 2), .5), material)
        if i % 2 == 0:
            icicle(r, name, -1, 11, -.25, 3.5)
    r.part('hem_frost', pivot=(0, 23.5, 0))
    for i in range(12):
        angle = i * 30
        a = math.radians(angle)
        name = f'frost_trail_{i}'
        r.part(name, 'hem_frost', (snap(math.sin(a) * 5), 0, snap(math.cos(a) * 4)), (0, angle, 0))
        r.cube(name, (-.5, 0, 0, 1, .5, 2 + (i % 3)), 'ice')

    world = kit.transforms(r)
    hand = world['left_hand']
    pivot = (0, 1, -1)
    bottom = snap(24 - (hand[:3, :3] @ pivot + hand[:3, 3])[1])
    r.part('staff', 'left_hand', pivot, euler(hand[:3, :3].T))
    r.cube('staff', (-.5, -15, -.5, 1, bottom + 15, 1), 'iron')
    r.cube('staff', (-1, bottom - .75, -1, 2, .75, 2), 'silver')
    for y in (-13, -8, -3, 3, 9, bottom - 1.5):
        r.cube('staff', (-.75, y, -.75, 1.5, .75, 1.5), 'silver')
    lantern(r, 'staff_lantern', 'staff', (0, -13, 0))
    r.cube('staff', (-.25, -5, -.25, 4, .5, .5), 'iron')
    chain(r, 'pendant_chain', 'staff', (3.5, -5, 0), 9)
    lantern(r, 'pendant_lantern', 'pendant_chain', (0, 17.5, 0), True)
    for part in r.parts:
        assert all(snap(v) == v for v in part['pivot']), part['name']
        for cube in part['cubes']:
            assert all(snap(v) == v for v in cube['box']), cube
            assert min(cube['box'][3:]) >= .25, cube
    return r


def paint(cube, t, glow=False):
    mat = cube['material']
    u, v = t['u'], t['v']
    noise = (t['n'] - .5) * 9
    lit = False
    if mat == 'crystal':
        rgb = mix((75, 119, 214), (213, 242, 255), t['fu'])
        lit = True
    elif mat == 'ice':
        rgb = add((154, 181, 218), noise)
        if u == 0 or t['face'] == 'up':
            rgb = (220, 234, 249)
        lit = t['n'] > .95
    elif mat in ('iron', 'silver'):
        patina = kit.smooth(t['gx'], t['gy'], t['gz'], scale=2, seed=9)
        rgb = mix((36, 38, 51), (119, 127, 149), patina)
        if mat == 'silver':
            rgb = add(rgb, 46)
        if u == 0 or v == 0:
            rgb = add(rgb, 25)
    elif mat == 'bone':
        rgb = add((180, 186, 201), noise)
        if u == 0:
            rgb = (101, 112, 140)
    elif mat == 'void':
        rgb = (10, 12, 23)
    else:
        pale = mat == 'veil'
        rgb = (199, 201, 215) if pale else ((35, 36, 63) if mat == 'shadow' else (47, 50, 79))
        rgb = add(rgb, (-12, -4, 4, -2)[u % 4] + noise)
        if t['side'] and (u == 0 or u == t['fw'] - 1):
            rgb = (156, 166, 192) if pale else (80, 90, 127)
        if t['side'] and v > t['fh'] - 3 and t['n'] > .38:
            rgb = mix(rgb, (174, 188, 214), .55)
        if pale and t['n'] > .94:
            rgb = (126, 138, 165)
    return (*rgb, 255) if not glow else ((*rgb, 255) if lit else (0, 0, 0, 0))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = saint()
    size = layout(rig, 256)
    base, glow = paint_atlases(rig, size, [paint, lambda c, t: paint(c, t, True)])
    TEXTURES.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURES / 'velorum.png')
    glow.save(TEXTURES / 'velorum_glow.png')
    source = entity_java_source(rig, size, 'VelorumEntity', 'velorum.py',
        package='com.vincenthuto.hemomancy.client.model.entity.boss.velorum',
        entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.velorum',
        extra_imports='import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;\n'
                      'import org.joml.Quaternionf;\nimport org.joml.Matrix3f;\n',
        extra_fields='',
        extra_methods=(ROOT / 'tools/model_export/velorum_anim.java').read_text())
    source = source.replace('public class VelorumModel extends HierarchicalModel<VelorumEntity>',
                            'public class VelorumModel<T extends VelorumEntity> extends HierarchicalModel<T>')
    source = source.replace('import net.minecraft.world.entity.HumanoidArm;\n', '')
    JAVA.write_text(source, encoding='utf-8')
    subprocess.run(['node', str(ROOT / 'tools/model_export/java_model_to_bbmodel.mjs'), '--source', str(JAVA),
                    '--texture', 'textures/entity/boss/velorum/velorum.png', '--output', str(BBMODEL)],
                   cwd=ROOT, check=True)
    project = json.loads(BBMODEL.read_text())
    project['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(
        (TEXTURES / 'velorum.png').read_bytes()).decode('ascii')
    BBMODEL.write_text(json.dumps(project, indent=2) + '\n', encoding='utf-8')
    print(f'Velorum: {sum(len(p["cubes"]) for p in rig.parts)} cubes; {len(rig.parts)} joints; atlas {base.size}')
    if args.preview:
        def posed(overrides):
            world = kit.transforms(rig, overrides)
            for name, parent in (('staff', 'left_hand'), ('left_sleeve', 'left_forearm'),
                                 ('right_sleeve', 'right_forearm'), ('claw_ice', 'right_hand')):
                part = next(p for p in rig.parts if p['name'] == name)
                rotation = euler(world[parent][:3, :3].T)
                overrides[name] = tuple(a - b for a, b in zip(rotation, part['rotation']))
            return quads(rig, overrides=overrides)
        stable = posed({})
        nova = posed({'chest': (-7, 0, 0), 'right_arm': (-8, 0, 15)})
        veil = posed({'head': (12, 0, 0), 'right_arm': (-18, 0, 20)})
        kit.preview_sheet([(stable, base, 0, 0), (stable, base, .55, -.06), (stable, base, math.pi, 0),
                           (stable, base, math.pi / 2, 0), (nova, base, .3, 0), (veil, base, .3, 0)],
                          ROOT / 'build/velorum_preview.png', cols=3)
        preview = Image.open(ROOT / 'build/velorum_preview.png')
        draw = ImageDraw.Draw(preview)
        for i, label in enumerate(('FRONT', 'THREE QUARTER', 'BACK', 'SIDE', 'FROST NOVA', 'WINTER VEIL')):
            draw.text(((i % 3) * 420 + 14, (i // 3) * 420 + 12), label, fill=(201, 217, 247))
        preview.save(ROOT / 'build/velorum_preview.png')
        kit.render_quads(stable, base, .35, -.06, 1100).save(ROOT / 'build/velorum_portrait.png')


if __name__ == '__main__':
    main()

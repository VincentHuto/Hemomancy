"""Seraphae, the Chain Saint: quarter-grid rig, fitted box UVs, and painted atlases.

Run python tools/model_export/seraphae.py --preview to regenerate the Java model,
embedded-texture Blockbench project, base/emissive atlases, and posed review sheet.
An ethereal female saint in ruined vestments, with open branching nervous projections.
Iron binds her wrists, waist, and wing roots; gold light leaks through the seams.
"""
import argparse
import base64
import json
import math
import subprocess

from PIL import Image, ImageDraw

import annetta_kit as kit
from annetta_kit import Rig, ROOT, ASSETS, layout, paint_atlases, entity_java_source, quads, preview_sheet, add, mix

kit.SCALE = 2
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/seraphae/SeraphaeModel.java'
TEXTURES = ASSETS / 'textures/entity/boss/seraphae'
BBMODEL = ASSETS / 'models/entity/bbmodel/bosses/SeraphaeModel.bbmodel'


def snap(value):
    return round(value * 4) / 4


def euler(matrix):
    return tuple(math.degrees(a) for a in (math.atan2(matrix[2, 1], matrix[2, 2]),
                 math.asin(max(-1, min(1, -matrix[2, 0]))), math.atan2(matrix[1, 0], matrix[0, 0])))


def chain(r, name, parent, pivot, count):
    r.part(name, parent, pivot)
    for i in range(count):
        link = f'{name}_link_{i}'
        r.part(link, name, (0, i * 2, 0), (0, 90 if i % 2 else 0, 0))
        for box in ((-1, 0, -.25, .5, 2.5, .5), (.5, 0, -.25, .5, 2.5, .5),
                    (-.5, 0, -.25, 1, .5, .5), (-.5, 2, -.25, 1, .5, .5)):
            r.cube(link, box, 'iron')


def wings(r, side, sign):
    wing = f'{side}_wing'
    outer = f'{side}_wing_outer'
    r.part(wing, 'body', (sign * 3, -9.5, 3), (0, -sign * 12, 0))
    r.part(f'{side}_wing_shoulder', wing, rotation=(0, 0, -sign * 125))
    r.cube(f'{side}_wing_shoulder', (-1.25, -1, -.75, 2.5, 13, 1.5), 'wing_bone')
    r.cube(wing, (-2, -.5, -1.25, 4, 1, 2.5), 'iron')
    r.cube(wing, (-2, 2, -1.25, 4, 1, 2.5), 'iron')
    # Unequal nerve trunks radiate from the restrained roots.
    for i, length in enumerate((10, 13, 17, 19, 20, 19)):
        name = f'{side}_inner_feather_{i}'
        r.part(name, wing, (sign * (1.5 + i * 1.5), -i, 1 + i * .75), (0, 0, -sign * 14))
        if side == 'right' and i == 4:
            length -= 4
        feather(r, name, length, i)
    r.part(outer, wing, (sign * 10, -7, 0), (0, -sign * 6, 0))
    r.part(f'{side}_wing_wrist', outer, rotation=(0, 0, -sign * 55))
    r.cube(f'{side}_wing_wrist', (-.75, -1, -.5, 1.5, 12, 1), 'wing_bone')
    # Long axons branch beyond the outer joint.
    for i, length in enumerate((20, 19, 18, 16, 14, 11, 8)):
        name = f'{side}_primary_{i}'
        r.part(name, outer, (sign * i * 1.5, i * .75, 1 + i * .75), (0, 0, -sign * (18 + i * 4)))
        if side == 'right' and i in (2, 5):
            length -= 3
        feather(r, name, length, i + 6)
    # Short axon fans leave negative space around the main trunks.
    for i in range(5):
        name = f'{side}_covert_{i}'
        r.part(name, wing, (sign * (1.5 + i * 2), -i * 1.25, -.75 + i * .25), (0, 0, -sign * 26))
        feather(r, name, 7 + i, i)


def feather(r, name, length, index):
    # Preserve the animated joint names; geometry is an axon with branching dendrites.
    r.cube(name, (-.25, 0, -.25, .5, length, .5), 'nerve')
    for j in range(3):
        y = snap(length * (.25 + j * .22))
        for sign in (-1, 1):
            branch = f'{name}_axon_{j}_{"left" if sign < 0 else "right"}'
            r.part(branch, name, (0, y, 0), (0, sign * 12, sign * (35 + index % 3 * 9)))
            reach = snap(2.5 + j * .75 + index % 2)
            r.cube(branch, (0, 0, -.25, .25, reach, .5), 'nerve')
            r.cube(branch, (0, reach, -.25, .25, 1, .25), 'core')
    r.cube(name, (-.5, snap(length * .5), -.5, 1, .75, 1), 'radiance')


def saint():
    r = Rig('seraphae', 'SeraphaeModel', 'seraphae')
    r.part('body', pivot=(0, 11, 0))
    # Shaped, fully clothed bodice: narrow waist, softly broader chest and hips.
    r.cube('body', (-3.25, -11, -2, 6.5, 4, 4), 'bodice')
    r.cube('body', (-2.75, -7, -1.75, 5.5, 3.5, 3.5), 'bodice')
    r.cube('body', (-2.25, -3.5, -1.5, 4.5, 3.5, 3), 'bodice')
    r.cube('body', (-3.25, -1, -2, 6.5, 2.5, 4), 'linen')
    for side, sign in (('left', 1), ('right', -1)):
        r.part(f'{side}_bodice', 'body', (sign * 1.5, -9.75, -1.75), (0, sign * 8, 0))
        r.cube(f'{side}_bodice', (-1.5, -1, -1, 3, 3.25, 1.25), 'bodice')
        r.part(f'{side}_mantle', 'body', (sign * 2.75, -11.25, 0), (0, 0, -sign * 20))
        r.cube(f'{side}_mantle', (-1, -.5, -2, 2, 4.5, 4), 'linen')
    r.cube('body', (-.25, -10.75, -2.5, .5, 9, .5), 'gilt')
    r.cube('body', (-.5, -8.5, -2.75, 1, 2, .5), 'core')
    # A real waist restraint with a hanging chain, rather than a cage across her chest.
    r.cube('body', (-2.75, -.75, -2.25, 5.5, 1, .75), 'iron')
    r.cube('body', (-2.75, -.75, 1.5, 5.5, 1, .75), 'iron')
    for x in (-2.75, 2):
        r.cube('body', (x, -.75, -1.5, .75, 1, 3), 'iron')
    r.cube('body', (-.75, -1, -2.75, 1.5, 1.5, .75), 'lock')
    r.cube('body', (-1, -13.5, -1, 2, 2.75, 2), 'skin')

    r.part('head', 'body', (0, -12, 0), (8, 0, -5))
    r.cube('head', (-2.75, -7, -2.25, 5.5, 5.5, 4.5), 'face')
    r.cube('head', (-2, -1.5, -2, 4, 1.5, 4), 'jaw')
    r.part('hair', 'head')
    r.cube('hair', (-3, -7.5, -2.5, 6, 1, 5), 'hair')
    r.cube('hair', (-3, -6.5, 1.75, 6, 10, 1.25), 'hair')
    for side, sign in (('left', 1), ('right', -1)):
        r.part(f'{side}_hair_lock', 'head', (sign * 2.5, -5.5, -1.25), (-4, 0, -sign * 8))
        r.cube(f'{side}_hair_lock', (-.75, -1, -.75, 1.5, 10, 1.5), 'hair')
        r.cube(f'{side}_hair_lock', (-.5, 9, -.5, 1, 2.5, 1), 'hair')
    r.part('veil', 'head')
    r.cube('veil', (-3.25, -8, -2.25, 6.5, .75, 5.5), 'linen')
    r.cube('veil', (-3.5, -7.25, -.5, .75, 10, 3.75), 'linen')
    r.cube('veil', (2.75, -7.25, -.5, .75, 10, 3.75), 'linen')
    r.cube('veil', (-3.25, -7.25, 2.75, 6.5, 13.5, .75), 'linen')
    r.cube('veil', (-3, -6.75, -2.75, 6, .75, .5), 'gilt')
    for side, sign in (('left', 1), ('right', -1)):
        r.part(f'{side}_veil_tail', 'head', (sign * 3, -.5, 1.5), (-5, 0, sign * -8))
        r.cube(f'{side}_veil_tail', (-1, 0, -.5, 2, 10 if sign == 1 else 12, .75), 'linen')

    # Open angular halo: restrained radiance, not a cross or a solid crown.
    r.part('halo', 'body', (0, -16, 4), (-5, 0, 0))
    for i in range(12):
        if i in (2, 7):
            continue
        angle = math.radians(i * 30)
        name = f'halo_segment_{i}'
        r.part(name, 'halo', (snap(math.sin(angle) * 9), snap(-math.cos(angle) * 9), 0), (0, 0, i * 30))
        r.cube(name, (-2.5, -.75, -.75, 5, 1.5, 1.5), 'gilt')
        r.cube(name, (-2, -.25, -1, 4, .5, .25), 'radiance')
        if i in (0, 3, 5, 9, 10):
            r.cube(name, (-.5, -3.5, -.5, 1, 2.75, 1), 'gilt')
            r.cube(name, (-.25, -5, -.25, .5, 1.5, .5), 'radiance')

    # Two broad feathered wings frame her body and stay separate from her chained arms.
    for side, sign in (('left', 1), ('right', -1)):
        wings(r, side, sign)

        r.part(f'{side}_arm', 'body', (sign * 3.5, -10, 0), (-12, 0, -sign * 50))
        r.cube(f'{side}_arm', (-1.25, -1, -1.25, 2.5, 4.5, 2.5), 'linen')
        r.cube(f'{side}_arm', (-.75, 2.5, -.75, 1.5, 4, 1.5), 'skin')
        r.part(f'{side}_forearm', f'{side}_arm', (0, 6, 0), (-18, 0, sign * 10))
        fore = f'{side}_forearm'
        r.cube(fore, (-.75, -.75, -.75, 1.5, 6.5, 1.5), 'skin')
        r.cube(fore, (-1.25, 3.75, -1.25, 2.5, 1.75, 2.5), 'iron')
        r.cube(fore, (-.5, 4.25, -2, 1, 1, .75), 'lock')
        r.part(f'{side}_hand', fore, (0, 5.5, 0), (-10, 0, sign * -8))
        r.cube(f'{side}_hand', (-1, -.25, -.75, 2, 2.75, 1.25), 'skin')
        for i, length in enumerate((1.75, 2.5, 2.25)):
            r.cube(f'{side}_hand', (-1.25 + i, 2, -.75, .5, length, .75), 'skin')
        r.cube(f'{side}_hand', (sign * 1 - .25, .25, -1.75, .75, 1.75, .75), 'skin')
        # Counter-rotation keeps each hanging chain vertical at the authored arm pose.
        chain(r, f'{side}_chain', fore, (sign * 1.5, 5, 0), 7)
        rotation = kit.rot_matrix((-12, 0, -sign * 50)) @ kit.rot_matrix((-18, 0, sign * 10))
        next(p for p in r.parts if p['name'] == f'{side}_chain')['rotation'] = euler(rotation.T)

        r.part(f'{side}_leg', pivot=(sign * 1.75, 11, 0), rotation=(-4, 0, sign * -4))
        r.cube(f'{side}_leg', (-1.25, -.75, -1.25, 2.5, 7, 2.5), 'skin')
        r.part(f'{side}_shin', f'{side}_leg', (0, 6, 0), (8, 0, 0))
        r.cube(f'{side}_shin', (-1, -.75, -1, 2, 6, 2), 'skin')
        r.cube(f'{side}_shin', (-1.5, 3.75, -1.5, 3, 1.5, 3), 'iron')
        r.cube(f'{side}_shin', (-1.25, 5, -2.75, 2.5, 1.5, 4), 'skin')

    # A full floor-length gown with an uneven hem and a torn opening above the ankle chains.
    for i, (x, z, width, length, angle, yaw) in enumerate(((-2, -2, 4, 12, 10, 0), (2, -2, 4, 13, -10, 0),
            (-2, 1.75, 4, 13, 9, 0), (2, 1.75, 4, 11.5, -9, 0),
            (-3, 0, 3.5, 12.5, 12, 90), (3, 0, 3.5, 12, -12, 90))):
        name = f'robe_panel_{i}'
        r.part(name, 'body', (x, -.5, z), (-6 if z > 0 else 6, yaw, angle))
        r.cube(name, (-width / 2, -.5, -.25, width, length, .5), 'linen')
        r.cube(name, (-width / 2, length - .5, -.25, snap(width / 2), 1.5, .5), 'hem')
    r.part('gown_front', 'body', (0, 0, -2.5), (5, 0, 0))
    r.cube('gown_front', (-1.75, 0, -.25, 3.5, 11, .5), 'linen')
    r.cube('gown_front', (-1.75, 11, -.25, 1.5, 2, .5), 'hem')
    chain(r, 'waist_chain', 'body', (-2.75, .5, -2.75), 6)
    # Enforce the saved-grid contract before either Java or Blockbench is emitted.
    for part in r.parts:
        assert all(snap(v) == v for v in part['pivot']), part['name']
        for cube in part['cubes']:
            assert all(snap(v) == v for v in cube['box']), cube
            assert min(cube['box'][3:]) >= .25, cube
    return r


def paint(cube, t, glow=False):
    mat = cube['material']
    u, v = t['u'], t['v']
    x, y = t['lx'], t['ly']
    n = (t['n'] - .5) * 8
    lit = mat in ('radiance', 'core')
    if mat in ('linen', 'bodice', 'hem'):
        rgb = (234, 230, 216) if mat != 'bodice' else (224, 219, 205)
        fold = (-16, -7, 4, 9, 4, -7)[(u // 2) % 6]
        rgb = add(rgb, fold + n)
        if t['side'] and v > t['fh'] - 5:
            rgb = mix(rgb, (130, 120, 101), .35)
        if u == 1 or u == t['fw'] - 2:
            rgb = (183, 160, 103)
        if mat == 'hem' and (u + v * 2) % 7 < 2:
            rgb = (121, 107, 87)
    elif mat in ('nerve', 'wing_bone'):
        rgb = add((158, 130, 122), n)
        channel = u == 0 or (v + cube.get('feather', 0)) % 9 < 2
        if channel:
            rgb, lit = (255, 250, 230), True
    elif mat == 'hair':
        rgb = add((201, 191, 153), n)
        if u % 4 == 0:
            rgb = (155, 145, 113)
        elif u % 4 == 2:
            rgb = (233, 222, 184)
    elif mat in ('skin', 'face', 'jaw'):
        rgb = add((211, 194, 178), n)
        channel = (u - (v // 3) - 2) % 17 == 0
        if channel:
            rgb, lit = (255, 227, 146), True
        if mat in ('face', 'jaw'):
            rgb = add((219, 202, 187), n)
            lit = False
            if t['face'] == 'north':
                if abs(x) > 1.75 and -3.5 < y < -2:
                    rgb = (200, 173, 160)
                if mat == 'jaw' and abs(x) < .75 and -.9 < y < -.4:
                    rgb = (165, 119, 111)
                if abs(x) < .4 and -4 < y < -2:
                    rgb = (233, 216, 196)
                if mat == 'face' and .75 < abs(x) < 2.25 and -4.75 < y < -4:
                    rgb, lit = (255, 233, 165), True
                if mat == 'face' and 1 < abs(x) < 1.5 and -4 < y < -2.5:
                    rgb, lit = (247, 221, 155), True
    elif mat in ('iron', 'lock'):
        rgb = add((49, 56, 53), n)
        if u == 0 or v == 0:
            rgb = (105, 113, 99)
        if (u + 3 * v) % 19 == 0:
            rgb = (100, 72, 39)
        if mat == 'lock' and t['face'] == 'north':
            rgb = (112, 98, 59) if u in (0, t['fw'] - 1) else (49, 47, 36)
    elif mat == 'gilt':
        rgb = add((150, 117, 53), n)
        if u % 5 == 0:
            rgb = (73, 65, 40)
        if v == 0:
            rgb = (215, 187, 111)
    else:
        rgb = (255, 230, 151) if mat == 'radiance' else (255, 245, 198)
        if (u + v) % 7 == 0:
            rgb = (226, 174, 63)
    return (*rgb, 255) if not glow else ((255, 242, 194, 255) if lit else (0, 0, 0, 0))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = saint()
    size = layout(rig, 128)
    base, glow = paint_atlases(rig, size, [paint, lambda c, t: paint(c, t, True)])
    TEXTURES.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURES / 'seraphae.png')
    glow.save(TEXTURES / 'seraphae_glow.png')
    source = entity_java_source(rig, size, 'SeraphaeEntity', 'seraphae.py',
        package='com.vincenthuto.hemomancy.client.model.entity.boss.seraphae',
        entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae',
        extra_imports='import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeState;\n'
                      'import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeVisualRules;\n'
                      'import org.joml.Quaternionf;\nimport org.joml.Matrix3f;\n',
        
        extra_methods=(ROOT / 'tools/model_export/seraphae_anim.java').read_text())
    source = source.replace('public class SeraphaeModel extends HierarchicalModel<SeraphaeEntity>',
                            'public class SeraphaeModel<T extends SeraphaeEntity> extends HierarchicalModel<T>')
    source = source.replace('import net.minecraft.world.entity.HumanoidArm;\n', '')
    JAVA.write_text(source, encoding='utf-8')
    subprocess.run(['node', str(ROOT / 'tools/model_export/java_model_to_bbmodel.mjs'),
                    '--source', str(JAVA), '--texture', 'textures/entity/boss/seraphae/seraphae.png',
                    '--output', str(BBMODEL)], cwd=ROOT, check=True)
    project = json.loads(BBMODEL.read_text())
    project['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(
        (TEXTURES / 'seraphae.png').read_bytes()).decode('ascii')
    BBMODEL.write_text(json.dumps(project, indent=2) + '\n', encoding='utf-8')
    print(f'Seraphae: {sum(len(p["cubes"]) for p in rig.parts)} cubes; {len(rig.parts)} joints; atlas {base.size}')
    if args.preview:
        def posed(overrides):
            world = kit.transforms(rig, overrides)
            for side in ('left', 'right'):
                name = f'{side}_chain'
                rotation = euler(world[f'{side}_forearm'][:3, :3].T)
                rest = next(p['rotation'] for p in rig.parts if p['name'] == name)
                overrides[name] = tuple(a - b for a, b in zip(rotation, rest))
            return quads(rig, overrides=overrides)
        stable = posed({})
        fracturing = posed({'left_arm': (0, 0, -12), 'right_arm': (0, 0, 12),
                            'left_wing': (0, 10, -7), 'right_wing': (0, -10, 7),
                            'left_wing_outer': (0, 12, 0), 'right_wing_outer': (0, -12, 0)})
        closing = posed({'body': (12, 0, 0), 'left_arm': (-35, 0, 30),
                         'right_arm': (-35, 0, -30), 'left_forearm': (-30, 0, 0),
                         'right_forearm': (-30, 0, 0), 'left_wing': (0, 43, 9),
                         'right_wing': (0, -43, -9), 'left_wing_outer': (0, 17, 4),
                         'right_wing_outer': (0, -17, -4)})
        preview_sheet([(stable, base, 0, 0), (stable, base, -.65, -.12), (stable, base, math.pi, 0),
                       (fracturing, base, 0, 0), (closing, base, -.3, 0), (stable, glow, 0, 0)],
                      ROOT / 'build/seraphae_preview.png', cols=3)
        preview = Image.open(ROOT / 'build/seraphae_preview.png')
        draw = ImageDraw.Draw(preview)
        for i, label in enumerate(('FRONT', 'THREE QUARTER', 'BACK', 'FRACTURING', 'CONDENSING', 'EMISSIVE MASK')):
            draw.text(((i % 3) * 420 + 14, (i // 3) * 420 + 12), label, fill=(210, 209, 196))
        preview.save(ROOT / 'build/seraphae_preview.png')
        kit.render_quads(stable, base, -.25, -.08, 1000).save(ROOT / 'build/seraphae_portrait.png')


if __name__ == '__main__':
    main()

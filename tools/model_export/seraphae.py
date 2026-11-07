"""Seraphae, the Chain Saint: half-grid jointed rig, nerve-tree wings, fitted box UVs, and three painted atlases.

Run python tools/model_export/seraphae.py --preview to regenerate the Java model, the embedded-texture Blockbench
project, the base atlas and the two emissive masks (white vessel light, gold nerve light), and the posed review sheet.

She is rebuilt at render scale 2.0 with kit.SCALE = 2: 16 texels per block, the same density as blocks and items. The
rig sits on a half-unit grid (one world pixel at that scale) and the script asserts it. Her body is about 25.5 units,
3.2 blocks, tall; the halo tops out near 3.8. Each wing is a nerve tree, not a fan of feathers: a root plexus at the
shoulder blade carrying the wing-root iron, three jointed trunks of three segments that taper and branch, and a tip fan
of four dendrites that open and close like fingers. The right wing keeps the distorted regrowth: a shorter middle
trunk and an extra kink in the lower one. Iron binds her wrists, wing roots and waist, each with an empty locator part
the renderer reads for the chains. Gold on her is only ever nerve light: the halo and trims are bone, the locks are
hematic iron with a bone pin, and no texel of the base atlas is gilt, brass or copper.
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
MODEL_SCALE = 2.0
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/seraphae/SeraphaeModel.java'
TEXTURES = ASSETS / 'textures/entity/boss/seraphae'
BBMODEL = ASSETS / 'models/entity/bbmodel/bosses/SeraphaeModel.bbmodel'

# trunk lengths per segment; the right wing's middle trunk is three shorter and its lower trunk gains a kink
TRUNKS = {'upper': (7, 6, 5), 'middle': (8, 7, 6), 'lower': (7, 6, 5)}
RIGHT_TRUNKS = {'upper': (7, 6, 5), 'middle': (7, 6, 5), 'lower': (7, 6, 5, 2.5)}
# where each trunk leaves the root plexus and which way it points (degrees: x back-tilt, y sweep, z spread)
TRUNK_POSE = {'upper': ((0, -1.5, 0.5), (15, 10, 145)), 'middle': ((0, -0.5, 1), (20, 18, 112)),
              'lower': ((0, 0.5, 0.5), (25, 24, 80))}
SEGMENT_BEND = 8          # degrees each segment curls further from the last
BRANCH = (3, 3.5)         # branch lengths, alternating
DENDRITE = 2              # tip dendrite length
HALO_RADIUS = 7


def snap(value):
    return round(value * 2) / 2


def euler(matrix):
    return tuple(math.degrees(a) for a in (math.atan2(matrix[2, 1], matrix[2, 2]),
                 math.asin(max(-1, min(1, -matrix[2, 0]))), math.atan2(matrix[1, 0], matrix[0, 0])))


def chain(r, name, parent, pivot, count):
    """Hanging iron links: two side bars and a top and bottom bar each, alternate links turned a quarter."""
    r.part(name, parent, pivot)
    for i in range(count):
        link = f'{name}_link_{i}'
        r.part(link, name, (0, i * 2, 0), (0, 90 if i % 2 else 0, 0))
        for box in ((-1, 0, 0, .5, 2.5, .5), (.5, 0, 0, .5, 2.5, .5),
                    (-.5, 0, 0, 1, .5, .5), (-.5, 2, 0, 1, .5, .5)):
            r.cube(link, box, 'iron')


def trunk(r, side, sign, name, lengths):
    """One nerve trunk: jointed tapering segments, two branches at each segment end, and a tip fan of dendrites."""
    pivot, (rx, ry, rz) = TRUNK_POSE[name]
    parent = f'{side}_wing'
    for i, length in enumerate(lengths):
        seg = f'{side}_trunk_{name}_{i}'
        if i == 0:
            r.part(seg, parent, (sign * pivot[0], pivot[1], pivot[2]), (rx, sign * ry, -sign * rz))
        else:
            kink = 40 if (side == 'right' and name == 'lower' and i == 3) else SEGMENT_BEND
            r.part(seg, parent, (0, lengths[i - 1], 0), (4, 0, sign * kink))
        thick = 1 if i < 2 else .5
        lo = -.5 if thick == 1 else -.5
        r.cube(seg, (lo, 0, lo, thick, length, thick), 'nerve', trunk=name, segment=i)
        # a myelin collar where the segment leaves its parent
        if thick == 1:
            r.cube(seg, (-.5, 0, -.5, 1, .5, 1), 'sheath', trunk=name, segment=i)
        for k, letter in enumerate('ab'):
            branch = f'{seg}_branch_{letter}'
            r.part(branch, seg, (0, snap(length - .5), 0), (k * 18 - 9, 0, (1 if k == 0 else -1) * (32 + i * 6)))
            r.cube(branch, (-.5, 0, 0, .5, BRANCH[(i + k) % 2], .5), 'nerve', trunk=name, segment=i, branch=True)
        parent = seg
    last = f'{side}_trunk_{name}_{len(lengths) - 1}'
    tip = f'{side}_trunk_{name}_tip'
    r.part(tip, last, (0, lengths[-1], 0))
    for k in range(4):
        r.part(f'{tip}_{k}', tip, (0, 0, 0), (0, 0, (k - 1.5) * 18))
        r.cube(f'{tip}_{k}', (-.5, 0, 0, .5, DENDRITE, .5), 'nerve', trunk=name, segment=9, tip=True)
    r.cube(tip, (-.5, -.5, -.5, 1, 1, 1), 'core')


def wing(r, side, sign):
    w = f'{side}_wing'
    r.part(w, 'body', (sign * 2, -7.5, 1.5), (0, -sign * 12, 0))
    # the root plexus: a sheathed knot of nerve held in two iron bands
    r.cube(w, (-1, -1.5, -1, 2, 3, 2), 'sheath')
    r.cube(w, (-1.5, -1.5, -1.5, 3, 1, 3), 'iron')
    r.cube(w, (-1.5, 0.5, -1.5, 3, 1, 3), 'iron')
    r.part(f'{side}_wing_shackle', w, (0, 0, 0))
    lengths = (RIGHT_TRUNKS if side == 'right' else TRUNKS)
    for name in ('upper', 'middle', 'lower'):
        trunk(r, side, sign, name, lengths[name])


def saint():
    r = Rig('seraphae', 'SeraphaeModel', 'seraphae')
    r.part('body', pivot=(0, 13, 0))
    # A shaped, fully clothed bodice: narrow waist, softly broader chest and hips.
    r.cube('body', (-2.5, -9, -1.5, 5, 3, 3), 'bodice')
    r.cube('body', (-2, -6, -1.5, 4, 3, 3), 'bodice')
    r.cube('body', (-2, -3, -1.5, 4, 3, 3), 'bodice')
    r.cube('body', (-2.5, -.5, -1.5, 5, 2, 3), 'linen')
    r.cube('body', (-1, -10.5, -1, 2, 2, 2), 'skin')
    # bone stitching down the bodice front, and the white vessel light of her heart
    r.cube('body', (-.5, -8.5, -2, 1, 8, .5), 'bone')
    r.cube('body', (-.5, -7.5, -2.5, 1, 1.5, .5), 'core')
    # the waist iron and its lock, with a bone pin
    r.cube('body', (-2.5, -.5, -2, 5, 1, .5), 'iron')
    r.cube('body', (-2.5, -.5, 1.5, 5, 1, .5), 'iron')
    for x in (-3, 2.5):
        r.cube('body', (x, -.5, -1.5, .5, 1, 3), 'iron')
    r.cube('body', (-.5, -1, -2.5, 1, 1.5, .5), 'iron')
    r.cube('body', (-.5, -.5, -3, .5, .5, .5), 'bone')
    r.part('waist_shackle', 'body', (0, 0, -2))
    for side, sign in (('left', 1), ('right', -1)):
        r.part(f'{side}_mantle', 'body', (sign * 2, -9, 0), (0, 0, -sign * 20))
        r.cube(f'{side}_mantle', (-1, -.5, -1.5, 2, 3.5, 3), 'linen')

    r.part('head', 'body', (0, -10, 0), (6, 0, -4))
    r.cube('head', (-2, -5, -2, 4, 4.5, 3.5), 'face')
    r.cube('head', (-1.5, -1, -1.5, 3, 1, 3), 'jaw')
    r.part('hair', 'head')
    r.cube('hair', (-2.5, -5.5, -2, 5, 1, 4.5), 'hair')
    r.cube('hair', (-2.5, -5, 1.5, 5, 8, 1), 'hair')
    for side, sign in (('left', 1), ('right', -1)):
        r.part(f'{side}_hair_lock', 'head', (sign * 2, -4, -1), (-4, 0, -sign * 8))
        r.cube(f'{side}_hair_lock', (-.5, -.5, -.5, 1, 8, 1), 'hair')
    r.part('veil', 'head')
    r.cube('veil', (-2.5, -6, -2, 5, .5, 4.5), 'linen')
    r.cube('veil', (-3, -5.5, -.5, .5, 8, 3), 'linen')
    r.cube('veil', (2.5, -5.5, -.5, .5, 8, 3), 'linen')
    r.cube('veil', (-2.5, -5.5, 2, 5, 10.5, .5), 'linen')
    r.cube('veil', (-2.5, -5, -2.5, 5, .5, .5), 'bone')
    for side, sign in (('left', 1), ('right', -1)):
        r.part(f'{side}_veil_tail', 'head', (sign * 2.5, -.5, 1.5), (-5, 0, sign * -8))
        r.cube(f'{side}_veil_tail', (-1, 0, 0, 2, 8 if sign == 1 else 9.5, .5), 'linen')

    # An open ring of ten bone segments with white inlays: restrained radiance, not a cross or a solid crown.
    r.part('halo', 'body', (0, -14, 3.5), (-5, 0, 0))
    for i in range(12):
        if i in (2, 7):
            continue
        angle = math.radians(i * 30)
        name = f'halo_segment_{i}'
        r.part(name, 'halo', (snap(math.sin(angle) * HALO_RADIUS), snap(-math.cos(angle) * HALO_RADIUS), 0), (0, 0, i * 30))
        r.cube(name, (-2, -.5, -.5, 4, 1, 1), 'bone')
        r.cube(name, (-1.5, -.5, -1, 3, .5, .5), 'radiance')
        if i in (0, 3, 5, 9, 10):
            r.cube(name, (-.5, -2.5, -.5, 1, 2, 1), 'bone')
            r.cube(name, (-.5, -3, -.5, 1, .5, 1), 'radiance')

    for side, sign in (('left', 1), ('right', -1)):
        wing(r, side, sign)

        r.part(f'{side}_arm', 'body', (sign * 3, -8.5, 0), (-12, 0, -sign * 50))
        r.cube(f'{side}_arm', (-1, -.5, -1, 2, 3.5, 2), 'linen')
        r.cube(f'{side}_arm', (-.5, 2.5, -.5, 1.5, 3, 1.5), 'skin', arm=True)
        fore = f'{side}_forearm'
        r.part(fore, f'{side}_arm', (0, 5, 0), (-18, 0, sign * 10))
        r.cube(fore, (-.5, -.5, -.5, 1.5, 5, 1.5), 'skin', arm=True)
        r.cube(fore, (-1, 3, -1, 2.5, 1.5, 2.5), 'iron')
        r.cube(fore, (0, 3.5, -1.5, .5, .5, .5), 'bone')
        r.part(f'{side}_wrist_shackle', fore, (0, 4, 0))
        hand = f'{side}_hand'
        r.part(hand, fore, (0, 5, 0), (-10, 0, sign * -8))
        r.cube(hand, (-.5, 0, -.5, 1.5, 2, 1), 'skin', arm=True)
        r.part(f'{side}_thumb', hand, (sign * 1 if sign > 0 else -.5, .5, -.5), (0, 0, sign * 25))
        r.cube(f'{side}_thumb', (0, 0, 0, .5, 1.5, .5), 'skin')
        r.part(f'{side}_fingers', hand, (0, 2, 0))
        r.cube(f'{side}_fingers', (-.5, 0, -.5, 1.5, 1.5, 1), 'skin', fingers=True)
        # Counter-rotation keeps each hanging chain vertical at the authored arm pose.
        chain(r, f'{side}_chain', fore, (sign * 1, 4.5, 0), 7)
        rotation = kit.rot_matrix((-12, 0, -sign * 50)) @ kit.rot_matrix((-18, 0, sign * 10))
        next(p for p in r.parts if p['name'] == f'{side}_chain')['rotation'] = euler(rotation.T)

        # legs hang slightly bent with the feet trailing: she hovers
        r.part(f'{side}_leg', pivot=(sign * 1, 13, 0), rotation=(-4, 0, sign * -4))
        r.cube(f'{side}_leg', (-1, -.5, -1, 2, 6, 2), 'skin')
        r.part(f'{side}_shin', f'{side}_leg', (0, 5.5, 0), (14, 0, 0))
        r.cube(f'{side}_shin', (-.5, -.5, -.5, 1.5, 5.5, 1.5), 'skin')
        r.cube(f'{side}_shin', (-.5, 4.5, -1, 1.5, 1, 2), 'skin')

    # A floor-length gown with an uneven hem.
    for i, (x, z, width, length, angle, yaw) in enumerate(((-1.5, -1.5, 3, 10.5, 10, 0), (1.5, -1.5, 3, 11, -10, 0),
            (-1.5, 1.5, 3, 11, 9, 0), (1.5, 1.5, 3, 10, -9, 0),
            (-2.5, 0, 4, 10.5, 12, 90), (2.5, 0, 4, 10, -12, 90))):
        name = f'robe_panel_{i}'
        r.part(name, 'body', (x, 1, z), (-6 if z > 0 else 6, yaw, angle))
        r.cube(name, (-width / 2, -.5, -.5, width, length, .5), 'linen')
        r.cube(name, (-width / 2, length - .5, -.5, snap(width / 2), 1.5, .5), 'hem')
    r.part('gown_front', 'body', (0, 1, -2), (5, 0, 0))
    r.cube('gown_front', (-1.5, 0, -.5, 3, 10, .5), 'linen')
    r.cube('gown_front', (-1.5, 10, -.5, 1.5, 1.5, .5), 'hem')
    chain(r, 'waist_chain', 'body', (-2, 1, -2.5), 6)

    # Enforce the half-grid contract before either Java or Blockbench is emitted.
    for part in r.parts:
        assert all(snap(v) == v for v in part['pivot']), part['name']
        for cube in part['cubes']:
            assert all(snap(v) == v for v in cube['box']), (part['name'], cube)
            assert min(cube['box'][3:]) >= .5, (part['name'], cube)
    assert kit.SCALE * 16 / MODEL_SCALE == 16
    return r


NERVE = (222, 206, 198)
MYELIN = (196, 178, 172)
VESSEL_WHITE = (255, 252, 246)
NERVE_GOLD = (255, 214, 120)
BONE = (226, 216, 196)
STITCH = (214, 204, 180)
IRON, IRON_EDGE, IRON_SCALE = (49, 56, 53), (105, 113, 99), (38, 42, 40)


def vessel_channel(t):
    """Her skin's vessels: a faint diagonal web the white light fills."""
    return (t['u'] - (t['v'] // 3) - 2) % 13 == 0


def nerve_centre(cube, t):
    """The one-pixel centre line of a trunk, branch or dendrite: the column at the middle of each side face."""
    if not t['side']:
        return False
    return t['u'] == t['fw'] // 2


def paint(cube, t, layer='base'):
    """layer 'base' paints the atlas; 'vessels' returns white where the vessel light lives; 'nerves' gold."""
    mat = cube['material']
    u, v = t['u'], t['v']
    x, y = t['lx'], t['ly']
    n = (t['n'] - .5) * 8
    vessel = nerve = False
    if mat in ('linen', 'bodice', 'hem'):
        rgb = (234, 230, 216) if mat != 'bodice' else (224, 219, 205)
        fold = (-14, -6, 4, 8, 4, -6)[(u // 2) % 6]
        rgb = add(rgb, fold + n)
        if t['side'] and v > t['fh'] - 4:
            rgb = mix(rgb, (130, 120, 101), .35)
        if u == 1 or u == t['fw'] - 2:
            rgb = STITCH
        if mat == 'hem' and (u + v * 2) % 7 < 2:
            rgb = (121, 107, 87)
    elif mat in ('nerve', 'sheath'):
        rgb = add(NERVE, n)
        if v % 3 == 0:
            rgb = add(MYELIN, n)
        if mat == 'nerve' and nerve_centre(cube, t):
            nerve = True
        if mat == 'sheath':
            rgb = add(MYELIN, n - 6)
    elif mat == 'hair':
        rgb = add((201, 191, 153), n)
        if u % 4 == 0:
            rgb = (155, 145, 113)
        elif u % 4 == 2:
            rgb = (233, 222, 184)
    elif mat in ('skin', 'face', 'jaw'):
        rgb = add((226, 214, 204), n)
        if mat == 'skin' and vessel_channel(t):
            rgb, vessel = (238, 222, 218), True
        if cube.get('arm') and t['face'] == 'south' and t['u'] == t['fw'] // 2:
            nerve = True
        if cube.get('fingers') and t['face'] == 'north' and u % 2 == 1:
            rgb = (196, 182, 172)
        if mat in ('face', 'jaw'):
            rgb = add((228, 216, 206), n)
            if t['face'] == 'north':
                if abs(x) > 1.5 and -3 < y < -1.5:
                    rgb = (206, 186, 176)
                if mat == 'jaw' and abs(x) < .5 and -.9 < y < -.4:
                    rgb = (170, 128, 120)
                if abs(x) < .5 and -3.5 < y < -1.5:
                    rgb = (236, 222, 208)
                if mat == 'face' and .5 <= abs(x) < 1.5 and -4 < y < -3.5:
                    rgb, vessel = (250, 246, 240), True
    elif mat == 'iron':
        rgb = add(IRON, n)
        if u == 0 or v == 0:
            rgb = IRON_EDGE
        elif (u * 7 + v * 3) % 11 == 0:
            rgb = IRON_SCALE
    elif mat == 'bone':
        rgb = add(BONE, n)
        if (u + v) % 5 == 0:
            rgb = add(BONE, n - 18)
    elif mat == 'radiance':
        rgb, vessel = (246, 240, 230), True
    else:  # core: the white heart and tips, lit
        rgb, vessel = (250, 246, 238), True
    if layer == 'vessels':
        return (*VESSEL_WHITE, 255) if vessel else (0, 0, 0, 0)
    if layer == 'nerves':
        return (*NERVE_GOLD, 255) if nerve else (0, 0, 0, 0)
    return (*[max(0, min(255, int(c))) for c in rgb], 255)


def nerve_part_names(rig):
    """Every part whose cubes carry nerve light, in rig order: the trunks, branches and tip dendrites of both wings."""
    return [p['name'] for p in rig.parts if any(c['material'] == 'nerve' for c in p['cubes'])]


def nerve_table(rig):
    """Java source for the nerve parts: each part's side, trunk place and front position for the flash rules."""
    rows = []
    for part in rig.parts:
        nerve = [c for c in part['cubes'] if c['material'] == 'nerve']
        if not nerve:
            continue
        name = part['name']
        side = 0 if name.startswith('left') else 1
        trunk_place = ('upper', 'middle', 'lower').index(nerve[0]['trunk'])
        segment = nerve[0]['segment']
        position = 3 if segment >= 3 else segment
        rows.append(f'\t\tnew NervePart({kit.camel(name)}, {side}, {trunk_place}, {position})')
    return ',\n'.join(rows)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = saint()
    size = layout(rig, 128)
    base, vessels, nerves = paint_atlases(rig, size, [lambda c, t: paint(c, t, 'base'),
                                                      lambda c, t: paint(c, t, 'vessels'),
                                                      lambda c, t: paint(c, t, 'nerves')])
    TEXTURES.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURES / 'seraphae.png')
    vessels.save(TEXTURES / 'seraphae_vessels.png')
    nerves.save(TEXTURES / 'seraphae_nerves.png')
    extra = (ROOT / 'tools/model_export/seraphae_anim.java').read_text(encoding='utf-8')
    extra = extra.replace('/*NERVE_TABLE*/', nerve_table(rig))
    source = entity_java_source(rig, size, 'SeraphaeEntity', 'seraphae.py',
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
    subprocess.run(['node', str(ROOT / 'tools/model_export/java_model_to_bbmodel.mjs'),
                    '--source', str(JAVA), '--texture', 'textures/entity/boss/seraphae/seraphae.png',
                    '--output', str(BBMODEL)], cwd=ROOT, check=True)
    project = json.loads(BBMODEL.read_text())
    project['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(
        (TEXTURES / 'seraphae.png').read_bytes()).decode('ascii')
    BBMODEL.write_text(json.dumps(project, indent=2) + '\n', encoding='utf-8')
    tall = 24 - min(min(kit.transforms(rig)[p['name']][1, 3] + c['box'][1] for c in p['cubes'])
                    for p in rig.parts if p['cubes'] and not p['name'].startswith('halo'))
    print(f'Seraphae: {sum(len(p["cubes"]) for p in rig.parts)} cubes; {len(rig.parts)} joints; atlas {base.size}; '
          f'about {tall:.1f} units ({tall * MODEL_SCALE / 16:.2f} blocks) to the crown')
    if args.preview:
        def posed(overrides):
            world = kit.transforms(rig, overrides)
            for side in ('left', 'right'):
                name = f'{side}_chain'
                rotation = euler(world[f'{side}_forearm'][:3, :3].T)
                rest = next(p['rotation'] for p in rig.parts if p['name'] == name)
                overrides[name] = tuple(a - b for a, b in zip(rotation, rest))
            return quads(rig, overrides=overrides)
        rest = posed({})
        spread = posed({'left_wing': (0, 10, -12), 'right_wing': (0, -10, 12),
                        'left_trunk_upper_0': (0, 0, -15), 'right_trunk_upper_0': (0, 0, 15),
                        'left_trunk_lower_0': (0, 0, 15), 'right_trunk_lower_0': (0, 0, -15),
                        'left_arm': (-40, 0, 20), 'right_arm': (-40, 0, -20)})
        reach = posed({'body': (12, 0, 0), 'left_arm': (-75, 0, 35), 'right_arm': (-75, 0, -35),
                       'left_forearm': (10, 0, 0), 'right_forearm': (10, 0, 0),
                       'left_wing': (0, 30, 0), 'right_wing': (0, -30, 0)})
        preview_sheet([(rest, base, 0, 0), (rest, base, -.65, -.12), (rest, base, math.pi, 0),
                       (spread, base, 0, 0), (reach, base, -.3, 0), (rest, vessels, 0, 0), (rest, nerves, 0, 0)],
                      ROOT / 'build/seraphae_preview.png', cols=4)
        preview = Image.open(ROOT / 'build/seraphae_preview.png')
        draw = ImageDraw.Draw(preview)
        for i, label in enumerate(('FRONT', 'THREE QUARTER', 'BACK', 'WINGS SPREAD', 'THE REACH',
                                   'VESSEL MASK (WHITE)', 'NERVE MASK (GOLD)')):
            draw.text(((i % 4) * 420 + 14, (i // 4) * 420 + 12), label, fill=(210, 209, 196))
        preview.save(ROOT / 'build/seraphae_preview.png')
        kit.render_quads(rest, base, -.25, -.08, 1000).save(ROOT / 'build/seraphae_portrait.png')


if __name__ == '__main__':
    main()

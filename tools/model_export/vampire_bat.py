"""The Vampire Bat: a lean flying hunter with a true bat wing, a hinged jaw and raptor legs.

Emits VampireBatMesh.java (animation lives in the hand-written VampireBatModel), the atlas and its emissive eye layer.

    python tools/model_export/vampire_bat.py            # mesh + textures
    python tools/model_export/vampire_bat.py --preview  # also build/vampire_bat_preview.png
    node tools/model_export/java_model_to_bbmodel.mjs src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/VampireBatMesh.java --texture textures/entity/vampire_bat/model_vampire_bat.png --out src/main/resources/assets/hemomancy/models/entity/bbmodel

Model space: head at -z, belly at +y, spread left wing along +x. The body flies level in the rest pose.

Each wing is three flat membrane panels on a shoulder, elbow and wrist that all hinge along z, so the panels stay joined
through a wingbeat. All three are cut from one silhouette in plan view (wing_outline), so the edge runs unbroken across
the hinges. Bones are separate thin spars lying on the panels. 16 px per block, one texel per model unit.
"""
from pathlib import Path
import argparse
import math

from PIL import Image

import hemomancy_books as hb
import unstained_weapons as uw

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/VampireBatMesh.java'
TEXTURES = uw.ASSETS / 'textures/entity/vampire_bat'

CLEAR = (0, 0, 0, 0)

# Wing plan, in model units from the body's centre line: shoulder, elbow, wrist, three fingertips, the hip it joins.
SHOULDER, ELBOW, WRIST = (2, -2), (7, -3), (12, -7.5)
FINGERS = ((22, -0.5), (19, 8), (13.5, 11))
HIP = (2, 7.5)
ELBOW_X, WRIST_X, TIP_X = 7, 12, 22
PLANE_Z, PLANE_D = -8, 20
SCALLOP = 2.75


def scalloped(a, b, depth=SCALLOP, steps=8):
    """The membrane's trailing edge from a to b, bowed inward toward the wrist."""
    mx, mz = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
    nx, nz = WRIST[0] - mx, WRIST[1] - mz
    length = math.hypot(nx, nz)
    nx, nz = nx / length, nz / length
    return [(a[0] + (b[0] - a[0]) * t + nx * depth * 4 * t * (1 - t),
             a[1] + (b[1] - a[1]) * t + nz * depth * 4 * t * (1 - t))
            for t in (i / steps for i in range(steps))]


def wing_outline():
    points = [SHOULDER, WRIST]
    for a, b in ((FINGERS[0], FINGERS[1]), (FINGERS[1], FINGERS[2]), (FINGERS[2], HIP)):
        points += scalloped(a, b)
    return points + [HIP]


OUTLINE = wing_outline()
TRAILING = OUTLINE[2:]


def inside(x, z, polygon=OUTLINE):
    hit = False
    for i, (ax, az) in enumerate(polygon):
        bx, bz = polygon[(i + 1) % len(polygon)]
        if (az > z) != (bz > z) and x < ax + (z - az) / (bz - az) * (bx - ax):
            hit = not hit
    return hit


def to_segment(x, z, a, b):
    dx, dz = b[0] - a[0], b[1] - a[1]
    t = max(0, min(1, ((x - a[0]) * dx + (z - a[1]) * dz) / (dx * dx + dz * dz)))
    return math.hypot(x - a[0] - dx * t, z - a[1] - dz * t)


def to_trailing(x, z):
    return min(math.hypot(x - px, z - pz) for px, pz in TRAILING)


# ---------------------------------------------------------------- rig

def spar(r, name, parent, start, end, thick, material):
    """A bone from start to end in its parent's plane, as a thin box turned about y."""
    dx, dz = end[0] - start[0], end[1] - start[1]
    length = math.hypot(dx, dz)
    r.part(name, parent, (start[0], 0, start[1]), (0, math.degrees(math.atan2(-dz, dx)), 0))
    r.cube(name, (0, -thick / 2, -thick / 2, length, thick, thick), material)


def left_wing(r):
    r.part('left_wing', 'body', (SHOULDER[0], -1, 0))
    r.cube('left_wing', (0, 0, PLANE_Z, ELBOW_X - SHOULDER[0], 0, PLANE_D), 'membrane')
    r.cube('left_wing', (-1.5, -1.25, -3.25, 3, 2, 3), 'fur')                   # the shoulder's muscle
    origin = lambda p, x0: (p[0] - x0, p[1])
    spar(r, 'left_humerus', 'left_wing', origin(SHOULDER, SHOULDER[0]), origin(ELBOW, SHOULDER[0]), 1.25, 'bone')

    r.part('left_forearm', 'left_wing', (ELBOW_X - SHOULDER[0], 0, 0))
    r.cube('left_forearm', (0, 0, PLANE_Z, WRIST_X - ELBOW_X, 0, PLANE_D), 'membrane')
    spar(r, 'left_radius', 'left_forearm', origin(ELBOW, ELBOW_X), origin(WRIST, ELBOW_X), 1, 'bone')

    r.part('left_hand', 'left_forearm', (WRIST_X - ELBOW_X, 0, 0))
    r.cube('left_hand', (0, 0, PLANE_Z, TIP_X - WRIST_X, 0, PLANE_D), 'membrane')
    wrist = origin(WRIST, WRIST_X)
    r.cube('left_hand', (wrist[0] - 0.5, -0.5, wrist[1] - 0.5, 1, 1, 1), 'knuckle')
    for i, tip in enumerate(FINGERS):
        spar(r, f'left_finger{i + 1}', 'left_hand', wrist, origin(tip, WRIST_X), 0.75 - i * 0.1, 'bone')
    # The thumb's hook, ahead of the wrist.
    spar(r, 'left_thumb', 'left_hand', wrist, (wrist[0] + 0.5, wrist[1] - 2.25), 0.6, 'bone')
    r.part('left_thumb_claw', 'left_thumb', (2.25, 0, 0), (0, 0, 55))
    r.cube('left_thumb_claw', (0, -0.25, -0.25, 1.75, 0.5, 0.5), 'claw')


def left_leg(r):
    r.part('left_leg', 'body', (1.25, 0.75, 4.5), (65, 0, 0))
    r.cube('left_leg', (-0.625, -0.25, -0.625, 1.25, 3.5, 1.25), 'fur')
    r.part('left_shin', 'left_leg', (0, 3.25, 0), (15, 0, 0))
    r.cube('left_shin', (-0.375, 0, -0.375, 0.75, 3.5, 0.75), 'hide')
    r.part('left_foot', 'left_shin', (0, 3.5, 0), (-55, 0, 0))
    r.cube('left_foot', (-0.75, -0.25, -0.75, 1.5, 0.75, 1), 'hide')
    for i, x in enumerate((-0.75, -0.2, 0.35)):
        toe = f'left_toe{i + 1}'
        r.part(toe, 'left_foot', (x + 0.2, 0.25, -0.75), (0, 0, 0))
        r.cube(toe, (-0.2, -0.2, -2, 0.4, 0.4, 2), 'hide')
        r.cube(toe, (-0.2, -0.1, -2.25, 0.4, 1.25, 0.4), 'claw')


def left_head_side(r):
    # Upper fang, a smaller tooth behind it.
    r.cube('head', (0.5, 0.9, -5.5, 0.5, 2, 0.5), 'tooth')
    r.cube('head', (0.75, 0.9, -4.25, 0.4, 0.75, 0.4), 'tooth')
    # Lower fang, standing inside the upper.
    r.cube('jaw', (0.25, -1, -3.5, 0.45, 1.1, 0.45), 'tooth')
    # A deep eye under a brow that slants down to the snout, which is what makes it a scowl.
    r.cube('head', (1.25, -1.25, -3.1, 1, 0.75, 0.1), 'eye')
    r.part('left_brow', 'head', (0.75, -1.75, -3.1), (0, 0, -24))
    r.cube('left_brow', (0, -0.5, -0.5, 2.25, 0.75, 1.25), 'hide')
    # Tall ears, laid back along the skull.
    r.part('left_ear', 'head', (1.75, -2, -1), (-40, 12, 20))
    r.cube('left_ear', (-2, -7, 0, 4, 7, 0), 'ear')
    r.part('left_ear_spar', 'left_ear', (-0.75, 0, 0), (0, 0, 5))
    r.cube('left_ear_spar', (-0.25, -5, -0.25, 0.5, 5, 0.5), 'hide')


def tufts(r):
    """Spikes of coarse fur that break the body's box outline: a neck ruff and a ridge along the spine."""
    ruff = [(-2.25, -2, -18, 0, 28), (2.25, -2, -18, 0, -28), (-2.75, 0, -22, 0, 62), (2.75, 0, -22, 0, -62),
            (0, -2.5, -30, 0, 0), (-1.25, -2.5, -28, 0, 14), (1.25, -2.5, -28, 0, -14), (-2, 1.75, -30, 0, 125),
            (2, 1.75, -30, 0, -125), (0, 2.25, -35, 0, 180)]
    for i, (x, y, rx, ry, rz) in enumerate(ruff):
        name = f'ruff{i}'
        r.part(name, 'body', (x, y, -3), (rx - 55, ry, rz))
        r.cube(name, (-0.625, -3, -0.625, 1.25, 3, 1.25), 'tuft')
    for i, (z, length) in enumerate(((-1.5, 2.25), (0.5, 2.75), (2.5, 2.25), (4.5, 1.75))):
        name = f'ridge{i}'
        r.part(name, 'body', (0, -2 + i * 0.3, z), (-62, 0, 0))
        r.cube(name, (-0.5, -length, -0.5, 1, length, 1), 'tuft')


def bat():
    r = uw.Rig('vampire_bat', 'VampireBatMesh', 'vampire_bat')
    r.parts[0]['pivot'] = (0, 16, 0)
    r.part('body', 'root')
    r.cube('body', (-2.5, -2.25, -3.5, 5, 4.5, 5), 'fur')                       # deep flying chest
    r.cube('body', (-1.5, 2.25, -3, 3, 0.75, 3.5), 'hide')                      # keel
    r.cube('body', (-2, -1.75, 1.5, 4, 3.5, 2.5), 'fur')
    r.cube('body', (-1.25, -1.25, 4, 2.5, 2.5, 2.25), 'hide')                   # lean waist and loin
    tufts(r)

    r.part('neck', 'body', (0, -0.5, -3.5), (-8, 0, 0))
    r.cube('neck', (-1.5, -1.5, -2.5, 3, 3, 3), 'fur')
    r.part('head', 'neck', (0, 0, -2.5), (12, 0, 0))
    r.cube('head', (-2.25, -2.25, -3, 4.5, 3.25, 3.5), 'fur')                   # cranium
    r.cube('head', (-2.5, -0.25, -2.5, 5, 1.25, 2.5), 'hide')                   # jaw muscle
    r.cube('head', (-1.25, -1.25, -5.75, 2.5, 2.25, 3), 'hide')                 # snout
    r.cube('head', (-1, -0.75, -6.75, 2, 1.5, 1.25), 'muzzle')
    r.cube('head', (-1.25, 0.9, -5.5, 2.5, 0.1, 3), 'maw')
    r.cube('head', (-1, -3, -6.8, 2, 2.75, 0), 'nose_leaf')                     # the spear above the nose
    r.part('jaw', 'head', (0, 1, -1.5), (6, 0, 0))
    r.cube('jaw', (-1.25, 0, -3.75, 2.5, 0.75, 3.75), 'jaw')
    r.cube('jaw', (-0.75, 0.25, -4.25, 1.5, 0.5, 0.75), 'hide')
    r.cube('jaw', (-0.5, -0.1, -3, 1, 0.1, 2), 'tongue')
    left_head_side(r)

    # A web between the legs, narrowing to a point.
    r.part('tail_web', 'body', (0, 0.25, 5.5), (12, 0, 0))
    r.cube('tail_web', (-3.5, 0, 0, 7, 0, 6.5), 'tail_web')
    left_wing(r)
    left_leg(r)
    mirror_left(r)
    return r


def mirror_left(r):
    """Copies every left_ part, and head's left-side cubes, to the right."""
    for part in list(r.parts):
        if part['name'] in ('head', 'jaw'):
            for cube in list(part['cubes']):
                x, y, z, w, hgt, d = cube['box']
                if x >= 0.25:
                    part['cubes'].append(dict(box=(-x - w, y, z, w, hgt, d), material=cube['material']))
    for part in list(r.parts):
        if not part['name'].startswith('left_'):
            continue
        parent = part['parent'].replace('left_', 'right_')
        px, py, pz = part['pivot']
        rx, ry, rz = part['rotation']
        name = part['name'].replace('left_', 'right_')
        r.part(name, parent, (-px, py, pz), (rx, -ry, -rz))
        for cube in part['cubes']:
            x, y, z, w, hgt, d = cube['box']
            r.cube(name, (-x - w, y, z, w, hgt, d), cube['material'])


# ---------------------------------------------------------------- painting

FUR = (46, 30, 32)
FUR_LIGHT = (76, 44, 42)
FUR_DEEP = (28, 18, 21)
HIDE = (62, 38, 38)
HIDE_DEEP = (40, 24, 26)
MEMBRANE = (64, 22, 28)
MEMBRANE_LIGHT = (94, 32, 36)
MEMBRANE_DEEP = (40, 14, 20)
BONE = (104, 78, 72)
BONE_LIGHT = (158, 130, 112)
CLAW = (28, 22, 24)
TOOTH = (232, 222, 196)
MAW = (120, 16, 26)
EYE = (255, 58, 40)


def fur(t):
    # Grain runs back along the body, with a darker saddle over the spine and a paler belly. Model y points down, so a
    # cube's 'down' face is the one on top.
    n = t['n']
    if t['face'] == 'down':
        rgb = FUR_DEEP if n < 0.45 else FUR
    elif t['face'] == 'up':
        rgb = FUR_LIGHT if n < 0.55 else FUR
    else:
        rgb = FUR if n < 0.7 else FUR_LIGHT if n < 0.93 else FUR_DEEP
    return uw.add(rgb, (uw.h(t['u'], t['v'], t['fw']) - 0.5) * 6)


def membrane(t):
    x, z = abs(t['gx']), t['gz']
    if not inside(x, z):
        return CLEAR
    edge = to_trailing(x, z)
    # Old tears along the trailing edge.
    if edge < 1.0 and uw.h(int(x), int(z), 9) > 0.8:
        return CLEAR
    # Veins fan from the wrist and the arm's blood vessels run beside each bone.
    angle = math.atan2(z - WRIST[1], x - WRIST[0])
    vein = abs(math.sin(angle * 9 + 0.6)) < 0.14 and math.hypot(x - WRIST[0], z - WRIST[1]) > 2
    rgb = MEMBRANE_LIGHT if t['n'] > 0.86 else MEMBRANE
    if vein:
        rgb = MEMBRANE_DEEP
    if edge < 1.6:
        rgb = uw.mix(rgb, MEMBRANE_DEEP, 0.6)
    bones = [(SHOULDER, ELBOW), (ELBOW, WRIST)] + [(WRIST, tip) for tip in FINGERS]
    if min(to_segment(x, z, a, b) for a, b in bones) < 1.1:
        rgb = uw.mix(rgb, MEMBRANE_DEEP, 0.5)
    return rgb + (255,)


def tail_web(t):
    x, z = abs(t['gx']), t['gz'] - 5.75
    half = 3.5 * (1 - z / 6.5) ** 0.8 if z < 6.5 else 0
    if x > half or (z > 3 and uw.h(int(x), int(z), 3) > 0.85):
        return CLEAR
    return (MEMBRANE_DEEP if t['n'] > 0.7 else MEMBRANE) + (255,)


def ear(t):
    # A tall point: wide at the base, tapering to a tip leaning outward, with a notch out of the rear edge.
    u, v, fw, fh = t['u'], t['v'], t['fw'], t['fh']
    width = (v + 1) / fh * fw * 0.95
    if abs(u + 0.5 - fw / 2) > width / 2 or (v == fh // 2 and u == fw - 1):
        return CLEAR
    if abs(u + 0.5 - fw / 2) < 0.75 and v > 1:
        return MAW + (255,)
    return (MEMBRANE if t['n'] > 0.5 else MEMBRANE_DEEP) + (255,)


def nose_leaf(t):
    # A blade: a stem at the nose, widening, then a point at the top.
    u, v, fw, fh = t['u'], t['v'], t['fw'], t['fh']
    half = {0: 0, 1: 1}.get(v, 1) if v < fh - 1 else 0
    if abs(u + 0.5 - fw / 2) > half + 0.01:
        return CLEAR
    return (HIDE if v > 0 else HIDE_DEEP) + (255,)


def paint(material, t):
    if material == 'membrane':
        return membrane(t), None
    if material == 'tail_web':
        return tail_web(t), None
    if material == 'ear':
        return ear(t), None
    if material == 'nose_leaf':
        return nose_leaf(t), None
    if material == 'eye':
        return EYE + (255,), EYE + (255,)
    if material == 'fur':
        rgb = fur(t)
    elif material == 'tuft':
        rgb = FUR_LIGHT if t['v'] <= 1 and t['side'] else fur(t)
    elif material == 'hide':
        rgb = HIDE if t['n'] < 0.75 else HIDE_DEEP
    elif material == 'muzzle':
        rgb = HIDE_DEEP
        if t['face'] == 'north' and t['v'] == 0 and t['u'] in (0, t['fw'] - 1):
            rgb = MAW                                       # flared nostrils
    elif material == 'jaw':
        rgb = MAW if t['face'] == 'down' else HIDE if t['n'] < 0.7 else HIDE_DEEP
    elif material in ('maw', 'tongue'):
        rgb = MAW if material == 'maw' else uw.add(MAW, 40)
    elif material == 'bone':
        rgb = BONE_LIGHT if t['n'] > 0.72 else BONE
    elif material == 'knuckle':
        rgb = BONE_LIGHT
    elif material == 'claw':
        rgb = CLAW if t['v'] < t['fh'] - 1 or t['fh'] <= 1 else BONE
    elif material == 'tooth':
        rgb = TOOTH
    else:
        raise KeyError(material)
    return rgb + (255,), None


# ---------------------------------------------------------------- Java

def java_source(rig, size):
    lines = []
    parents = {part['parent'] for part in rig.parts}
    for part in rig.parts[1:]:
        chain = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            u, v = cube['uv']
            chain += f'\n\t\t\t\t.texOffs({u}, {v}).addBox({", ".join(uw.f(c) for c in cube["box"])})'
        pose = ', '.join(uw.f(c) for c in part['pivot'])
        if any(part['rotation']):
            pose = 'PartPose.offsetAndRotation(' + pose + ', ' + ', '.join(uw.f(math.radians(a)) for a in part['rotation']) + ')'
        else:
            pose = 'PartPose.offset(' + pose + ')'
        declare = f'PartDefinition {part["name"]} = ' if part['name'] in parents else ''
        lines.append(f'\t\t{declare}{part["parent"]}.addOrReplaceChild("{part["name"]}", {chain}, {pose});')
    body = '\n'.join(lines)
    pivot = ', '.join(uw.f(c) for c in rig.parts[0]['pivot'])
    return f'''package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Generated by tools/model_export/{rig.name}.py; animation lives in {rig.java_class.replace("Mesh", "Model")}. */
public final class {rig.java_class} {{
	private {rig.java_class}() {{
	}}

	public static LayerDefinition createBodyLayer() {{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset({pivot}));
{body}
		return LayerDefinition.create(mesh, {size[0]}, {size[1]});
	}}
}}
'''


# ---------------------------------------------------------------- preview

def posed(rig, **rotations):
    saved = {p['name']: p['rotation'] for p in rig.parts}
    for p in rig.parts:
        if p['name'] in rotations:
            p['rotation'] = rotations[p['name']]
    quads = uw.rig_quads(rig)
    for p in rig.parts:
        p['rotation'] = saved[p['name']]
    return quads


def wings(shoulder, elbow, hand):
    out = {}
    for name, angle in (('wing', shoulder), ('forearm', elbow), ('hand', hand)):
        out['left_' + name] = (0, 0, angle)
        out['right_' + name] = (0, 0, -angle)
    return out


def preview(rig, image):
    poses = [(wings(-8, 6, 8), 0.5, 0.6), (wings(-42, 16, 26), 2.6, 0.25), (wings(30, 16, 12), math.pi / 2, 0.15),
             (wings(-8, 6, 8), math.pi, 1.45), (wings(-8, 6, 8), 0.0, 0.05), (wings(-8, 6, 8), math.pi / 2, 0.05)]
    tiles = [uw.render_quads(posed(rig, **pose), image, yaw, pitch, size=420) for pose, yaw, pitch in poses]
    sheet = Image.new('RGB', (420 * 3, 420 * 2))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % 3) * 420, (i // 3) * 420))
    out = ROOT / 'build/vampire_bat_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = bat()
    size = hb.layout(rig)
    image, glow = uw.paint_atlas(rig, size, paint)
    TEXTURES.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURES / 'model_vampire_bat.png')
    glow.save(TEXTURES / 'model_vampire_bat_eyes.png')
    JAVA.write_text(java_source(rig, size), encoding='utf-8')
    print(f'vampire_bat: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        preview(rig, image)


if __name__ == '__main__':
    main()

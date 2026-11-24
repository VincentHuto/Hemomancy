"""Bake the physical Juggler from its Blockbench project.

The legacy anatomy and palette below are the base of the separate Chamber true-self rig.
"""
import base64
import json

from puppet_rig import ASSETS, BBMODELS, MODELS, ROOT, TEXTURES, blockbench_mesh_source, noise, front_edge, back_edge

MODEL = BBMODELS / 'MarrowSpitterModel.bbmodel'
PROJECT = ROOT / 'docs/art/marrow_juggler/MarrowJuggler.bbmodel'
PALETTE = ['#17121a', '#2d1f2a', '#4b2232', '#74283a', '#a3384a', '#d0625e',
           '#8a7a6c', '#b8a48a', '#e4d4b4', '#f6ead2', '#6b6470', '#643a40']
# 11 is hematic iron (its shadow reuses 1): blood-side fittings are never brass, bronze or copper.
MASKS = {
    'mourn': ['99999', '90909', '95959', '90009', '90909'],
    'laugh': ['99999', '90909', '99999', '05550', '90009'],
    'blank': ['99999', '90909', '99999', '99999', '98889'],
}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('body', 'root', (0, -13, 0), [
        box((-4, -9, -2.5), (8, 6, 5), 'doublet'),
        box((-3, -3, -2), (6, 3, 4), 'doublet')], (8, 0, 0))
    # Two pleated ruff layers, the second turned to scallop the silhouette.
    joint('collar', 'body', (0, -9, 0), [
        box((-4.5, -1, -3), (9, 2, 6), 'ruff'),
        box((-1, .5, -3.5), (2, 2, 1), 'bone')])
    joint('collar_fold', 'collar', (0, 0, 0), [box((-3.5, -1.5, -3.5), (7, 1, 7), 'ruff')], (0, 45, 0))
    joint('hood', 'body', (0, -10, -.5), [box((-3, -6, -2.5), (6, 6, 5), 'hood')], (-10, 0, 0))
    joint('mask_mourn', 'hood', (0, -3, -2.5), [box((-2.5, -2.5, -1), (5, 5, 1), 'mourn')])
    joint('mask_laugh', 'hood', (-3, -3, 0), [box((-2.5, -2.5, -1), (5, 5, 1), 'laugh')], (0, 70, -8))
    joint('mask_blank', 'hood', (3, -3, 0), [box((-2.5, -2.5, -1), (5, 5, 1), 'blank')], (0, -70, 8))
    for side, sign in [('left', -1), ('right', 1)]:
        joint('hood_point_' + side, 'hood', (sign * 2, -5.5, 0), [
            box((-1, -4, -1), (2, 4, 2), 'hood')], (0, 0, sign * 35))
        joint('hood_tip_' + side, 'hood_point_' + side, (0, -4, 0), [
            box((-.5, -3, -.5), (1, 3, 1), 'hood'),
            box((-1, -5, -1), (2, 2, 2), 'hematic_iron')], (0, 0, sign * 45))
    joint('skirt', 'body', (0, 0, 0), [box((-3.5, 0, -2.5), (7, 3, 5), 'robe')])
    for side, sign in [('left', -1), ('right', 1)]:
        for facing, z, tilt, length in [('front', -2.5, -8, 9), ('rear', 2.5, 8, 10)]:
            name = facing + '_' + side
            joint(name, 'skirt', (sign * 1.75, 2, z), [
                box((-1.5, 0, -.5), (3, length, 1), 'panel')], (tilt, 0, -sign * 8))
            joint(name + '_rag', name, (0, length, 0), [
                box((-1.5, 0, -.5), (3, 2, 1), 'rag')], (0, 0, -sign * 8))
        joint('side_' + side, 'skirt', (sign * 3.5, 2, 0), [
            box((-.5, 0, -2), (1, 9, 4), 'panel')], (0, 0, -sign * 14))
    joint('sash', 'skirt', (0, 1, -3), [box((-1, 0, -.5), (2, 9, 1), 'sash')], (-6, 0, 0))
    shoulders = [(4, -8, 0), (4, -5.5, .5), (3, -2.5, .5)]
    for i in range(6):
        side = -1 if i % 2 == 0 else 1
        tier = i // 2
        sx, sy, sz = shoulders[tier]
        joint('arm_' + str(i), 'body', (side * sx, sy, sz), [
            box((-.5, 0, -.5), (1, 5, 1), 'bone'),
            box((-1, -.5, -1), (2, 3, 2), 'sleeve')], (-18, 0, -side * [115, 75, 35][tier]))
        joint('forearm_' + str(i), 'arm_' + str(i), (0, 4.5, 0), [
            box((-.5, 0, -.5), (1, 4, 1), 'bone'),
            box((-1, 3, -1), (2, 1, 2), 'hematic_iron')], (-55, 0, 0))
        joint('hand_' + str(i), 'forearm_' + str(i), (0, 4, 0), [
            box((-1, 0, -.5), (2, 2, 1), 'bone')], (15, 0, 0))
        joint('fingers_' + str(i), 'hand_' + str(i), (0, 1.5, 0), [
            box((-1, 0, -.5), (.5, 1.5, .5), 'bone'),
            box((.5, 0, -.5), (.5, 1.5, .5), 'bone')], (-35, 0, 0))
        joint('thumb_' + str(i), 'hand_' + str(i), (side * -1, .5, -.25), [
            box((-.25, 0, -.25), (.5, 1, .5), 'bone')], (-25, 0, side * 30))
    for i in range(3):
        joint('knife_' + str(i), 'body', (0, -8, -6), [
            box((-.5, -5, -.25), (1, 4, .5), 'blade'),
            box((-1, -1, -.5), (2, 1, 1), 'hematic_iron'),
            box((-.5, 0, -.5), (1, 2, 1), 'grip'),
            box((-.5, 2, -.5), (1, 1, 1), 'hematic_iron')])
    return parts


def harlequin(px, py, dark=False):
    # Diamonds touching at their points; the corners between them form the second color.
    cx, cy = px % 6, py % 6
    if abs(2 * cx - 5) + abs(2 * cy - 5) > 5:
        return 0 if dark else 1
    return 2 if dark else 3


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 11)
    flank = side not in ('north', 'up')
    if material in ('doublet', 'sleeve'):
        if side == 'down': return 0
        if side == 'up': return 3 if (px + py) % 2 else 2
        if py == 0 and material == 'doublet': return 4
        return harlequin(px, py, flank)
    if material == 'robe':
        if side in ('up', 'down'): return 2
        return 3 if px % 3 != 2 else 2
    if material == 'panel':
        if side in ('up', 'down'): return 2
        if py >= h - 2: return harlequin(px, py, flank)
        fold = px % 3
        if fold == 0 and not flank: return 4
        return (3 if fold != 2 else 2) if not flank else 2
    if material == 'rag':
        if side in ('up', 'down'): return 1
        return 2 if (px + py) % 2 == 0 else 0
    if material == 'sash':
        if side in ('up', 'down'): return 3
        return 8 if py % 3 == 0 else 4
    if material == 'hood':
        if side == 'down': return 0
        if side == 'up': return 3 if r > .3 else 2
        if py == 0: return 3
        return 2 if r > .15 else 1
    if material == 'ruff':
        if side == 'down': return 6
        if side == 'up': return 9 if px % 2 == 0 else 8
        return 8 if px % 2 == 0 else 7
    if material in MASKS:
        if side == 'north':
            return {'0': 0, '5': 5, '8': 8, '9': 9}[MASKS[material][py][px]]
        return 7
    if material == 'bone':
        if side == 'down' or (py == h - 1 and h > 2): return 6
        return 8 if side in ('north', 'up') else 7
    if material == 'hematic_iron':
        if side == 'up' or py == 0: return 11
        return 1 if side != 'north' else 11
    if material == 'blade':
        if side == 'north': return 9 if px == 0 else 8
        return 10
    if material == 'grip':
        return 1 if py % 2 else 2
    raise ValueError('Unknown material ' + material)


JOINTS = {
    'Suspended frame': 'body',
    'Doublet and shoulder yoke': 'chest',
    'Slumped velvet cowl': 'hood',
    'Comedy • porcelain mask': 'mask_comedy',
    'Tragedy • porcelain mask': 'mask_tragedy',
    'Split coattails': 'skirt',
    'Left foretail': 'front_left',
    'Right foretail': 'front_right',
    'Left side gore': 'side_left',
    'Right side gore': 'side_right',
    'Left train': 'rear_left',
    'Right train': 'rear_right',
    'Six articulated arms': 'arms',
    'Airborne daggers': 'props',
    'High dagger': 'knife_0',
    'Caught dagger': 'knife_1',
    'Falling dagger': 'knife_2',
}
ARM_NAMES = ('Left upper', 'Right upper', 'Left middle', 'Right middle', 'Left lower', 'Right lower')
for index, label in enumerate(ARM_NAMES):
    for suffix, joint in (('mount', 'mount'), ('shoulder', 'arm'), ('elbow', 'forearm'), ('wrist', 'hand')):
        JOINTS[f'{label} {suffix}'] = f'{joint}_{index}'


def prepare_runtime_rig(model):
    root, = model['outliner']
    props = next(group for group in root['children'] if group['name'] == 'Airborne daggers')

    def prepare(group, arm_index=None):
        label = group['name']
        if label.endswith(' wrist'):
            arm_index = ARM_NAMES.index(label.removesuffix(' wrist'))
        if label in ('Index', 'Middle', 'Ring'):
            group['name'] = f'{label.lower()}_{arm_index}'
        for node in group['children'][:]:
            if not isinstance(node, dict):
                continue
            if node['name'] == 'Caught dagger':
                # The authored grip is parented to the right wrist. In-game all daggers
                # must be independently movable so each can travel between both wrists.
                group['children'].remove(node)
                props['children'].append(node)
            else:
                prepare(node, arm_index)

    prepare(root)


def mesh_source(model):
    prepare_runtime_rig(model)
    return blockbench_mesh_source(
        model, 'MarrowSpitterMesh', PROJECT.name, 'build_marrow_juggler.py',
        lambda label: JOINTS.get(label, label), lambda arm, index, cube: None)


def build():
    data = PROJECT.read_bytes()
    model = json.loads(data)
    texture, = model['textures']
    png = base64.b64decode(texture['source'].split(',', 1)[1], validate=True)
    if png != PROJECT.with_name(texture['name']).read_bytes():
        raise ValueError('The Juggler project and standalone atlas must contain the same PNG')
    (MODELS / 'MarrowSpitterMesh.java').write_text(mesh_source(model), encoding='utf-8')
    MODEL.write_bytes(data)
    (ASSETS / TEXTURES / 'marrow_spitter.png').write_bytes(png)
    return MODEL


if __name__ == '__main__':
    build()

"""Bake the authored Hound into its in-game rig and atlases.

The older anatomy and palette below remain the source for the Chamber true self.
"""
import json
import math
import re

from PIL import Image

from puppet_rig import ROOT, ASSETS, MODELS, BBMODELS, TEXTURES, noise, front_edge

PALETTE = ['#140e12', '#2a1b20', '#523039', '#79333e', '#a44549', '#d06a5c',
           '#8a7e74', '#d8c9a8', '#4c4a52', '#807a84', '#2f2a33', '#6b4a2e']


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 0, 0))
    # Set-animal build: deep narrow keel, wasp waist, long back; stitched rupture seams mark the Curs.
    joint('body', 'root', (0, 13, 0), [
        box((-2, -3, -5), (4, 5, 5), 'chest'),
        box((-1.5, -2, 0), (3, 3, 4), 'muscle'),
        box((-2, -2.5, 3.5), (4, 4, 4), 'muscle'),
        box((-.5, -4, -5), (1, 1, 12), 'spine'),
        box((-2.5, -3.5, -3.5), (5, 6, 1), 'harness')])
    joint('neck', 'body', (0, -2, -4.5), [box((-1, -4, -1), (2, 4, 2), 'muscle')], (50, 0, 0))
    joint('head', 'root', (0, 9, -7.5), [
        box((-1.5, -1.5, -2), (3, 3, 3), 'skull'),
        box((-1, -.5, -5), (2, 2, 3), 'snout'),
        box((-1.5, -1, -4), (3, 3, 1), 'iron'),
        box((-.5, -2, -3), (1, 1, 2), 'iron')])
    joint('snout_tip', 'head', (0, .5, -5), [box((-.5, -1, -3), (1, 2, 3), 'snout')], (28, 0, 0))
    for side, sign in [('right', -1), ('left', 1)]:
        # Tall, upright, flat-topped ears.
        joint(side + '_ear', 'head', (sign * 1, -1.5, 0), [
            box((-.5, -5, -.5), (1, 5, 1), 'ear'),
            box((-.5, -6, -1), (1, 1, 2), 'ear')], (-10, 0, sign * 8))
        for end, z in [('front', -4), ('hind', 5)]:
            leg = end + '_' + side + '_leg'
            joint(leg, 'root', (sign * 1.5, 14, z), [box((-1, -1, -1), (2, 5, 2), 'muscle')])
            joint(end + '_' + side + '_shin', leg, (0, 4, 0), [
                box((-1, 0, -1), (2, 1, 2), 'iron'),
                box((-.5, 1, -.5), (1, 4, 1), 'bone'),
                box((-.5, 5, -1.5), (1, 1, 2), 'paw')])
    # Stiff tail ending in the Set-animal fork.
    joint('tail', 'root', (0, 11, 7.5), [box((-.5, -.5, 0), (1, 1, 6), 'spine')], (25, 0, 0))
    joint('tail_tip', 'tail', (0, 0, 6), [
        box((-1.5, -.5, 0), (3, 1, 1), 'bone'),
        box((-1.5, -.5, 1), (1, 1, 2), 'bone'),
        box((.5, -.5, 1), (1, 1, 2), 'bone')])
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 31)
    if material in ('muscle', 'chest'):
        if side == 'down': return 2
        if side == 'up': return 5 if r < .2 else 4
        if side in ('east', 'west'):
            # Exposed ribs on the chest; stitched rupture seams on the rest.
            if material == 'chest' and 1 <= py <= h - 3 and px % 2 == 0: return 7 if py < h - 3 else 6
            if material == 'muscle' and w >= 4 and px == w // 2: return 7 if py % 2 == 0 else 1
            if py == h - 1: return 2
            return 4 if py % 2 == 0 else 3
        if py == h - 1: return 2
        return 3 if r > .25 else 2
    if material == 'spine':
        if side == 'down': return 1
        return 7 if (px + py) % 2 == 0 else 1
    if material == 'harness':
        if side in ('up', 'down'): return 11
        if py == 0 or py == h - 1: return 1
        return 9 if px % 3 == 1 and py % 3 == 1 else 11
    if material == 'iron':
        if side == 'up' or py == 0: return 9
        return 8
    if material == 'skull':
        if side == 'up': return 7 if r > .15 else 6
        if side in ('east', 'west'):
            if py == 1 and front_edge(side, px, w): return 5
            if py == 1 and px == (w - 2 if side == 'east' else 1): return 0
            return 7 if py < h - 1 else 6
        if side == 'north': return 5 if py == 1 and px in (0, w - 1) else (0 if py == 1 else 7)
        return 6
    if material == 'snout':
        if side == 'north': return 0 if w == 1 else (7 if py == 0 else 3)
        if side == 'up': return 7 if r > .3 else 6
        if side == 'down': return 2
        if py == h - 1: return 2
        return 3
    if material == 'ear':
        if side == 'north': return 5
        return 3 if side != 'south' else 2
    if material == 'bone':
        return 7 if side in ('north', 'up', 'west') else 6
    if material == 'paw':
        if side == 'north': return 7 if px % 2 == 0 else 1
        return 1
    raise ValueError('Unknown material ' + material)


# Warm the exposed blood tones on Curs while preserving the hardware and bone.
CUR_BLOOD = {
    '3c1624': '591529', '641c30': '8c1a30', '8c2739': 'b82a3d',
    'ab3948': 'd84247', 'cd5b59': 'f66a62', 'a33345': 'cf3347',
    'af6260': 'cc6260', '81434b': 'a74450', '35131f': '521220',
    'be786e': 'db826f', '51232d': '63202c', '71313b': '8f2b3b',
    '8e4349': 'a93b47', 'a55555': 'c44750', 'a5444c': 'c7404e',
    '82253c': 'aa2942', '591c30': '791e33',
}


def java_numbers(values):
    return ', '.join(f'{value:.8f}'.rstrip('0').rstrip('.') + 'F' for value in values)


def java_geometry(model):
    elements = {cube['uuid']: cube for cube in model['elements']}
    lines = [
        '\tpublic static LayerDefinition createBodyLayer() {',
        '\t\tMeshDefinition mesh = new MeshDefinition();',
    ]

    def emit(group, parent, pivot):
        name = group['name']
        variable = re.sub(r'[^a-zA-Z0-9_]', '_', name)
        origin = group['origin']
        offset = [origin[0] - pivot[0], pivot[1] - origin[1], origin[2] - pivot[2]]
        x, y, z = map(math.radians, group['rotation'])
        pose = (f'offsetAndRotation({java_numbers([*offset, -x, y, -z])})'
                if any(group['rotation']) else f'offset({java_numbers(offset)})')
        lines.append(f'\t\tPartDefinition {variable} = {parent}.addOrReplaceChild("{name}", CubeListBuilder.create()')
        for child in group['children']:
            if isinstance(child, str):
                cube = elements[child]
                start, end = cube['from'], cube['to']
                coords = [start[0] - origin[0], origin[1] - end[1], start[2] - origin[2]]
                size = [high - low for low, high in zip(start, end)]
                u, v = cube['uv_offset']
                lines.append(f'\t\t\t\t.texOffs({u}, {v}).addBox({java_numbers(coords + size)})')
        lines[-1] += f', PartPose.{pose});'
        for child in group['children']:
            if isinstance(child, dict):
                emit(child, variable, origin)

    for group in model['outliner']:
        emit(group, 'mesh.getRoot()', [0, 24, 0])
    size = model['resolution']
    lines.extend([f'\t\treturn LayerDefinition.create(mesh, {size["width"]}, {size["height"]});', '\t}'])
    return '\n'.join(lines)


def build():
    art = ROOT / 'docs/art/sanguine_hound_puppet'
    model = (art / 'SanguineHoundPuppet.bbmodel').read_bytes()
    atlas = art / 'sanguine_hound_puppet.png'
    source = MODELS / 'SanguineHoundModel.java'
    java = source.read_text(encoding='utf-8')
    start = java.index('\t// BEGIN GENERATED HOUND GEOMETRY')
    end = java.index('\t// END GENERATED HOUND GEOMETRY', start)
    source.write_text(java[:start] + '\t// BEGIN GENERATED HOUND GEOMETRY\n'
                      + java_geometry(json.loads(model)) + '\n' + java[end:], encoding='utf-8', newline='\n')
    (BBMODELS / 'SanguineHoundModel.bbmodel').write_bytes(model)
    (ASSETS / TEXTURES / 'sanguine_hound.png').write_bytes(atlas.read_bytes())
    cur = Image.open(atlas).convert('RGBA')
    swaps = {tuple(bytes.fromhex(a)): tuple(bytes.fromhex(b)) for a, b in CUR_BLOOD.items()}
    cur.putdata([(*swaps.get((r, g, b), (r, g, b)), a) for r, g, b, a in cur.get_flattened_data()])
    cur.save(ASSETS / TEXTURES / 'sanguine_hound_cur.png')


if __name__ == '__main__':
    build()

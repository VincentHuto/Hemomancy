"""Render the shipped camp NBT using cached 1.21.1 block models and textures."""
from functools import lru_cache
from pathlib import Path
import json
import math
import shutil
import sys
import zipfile

import nbtlib
from build_teaching_camps import VARIANTS
from troupe_templates import ROOT, DATA
from serve_rig_preview import serve

OUTPUT = ROOT / 'build/teaching-camp-rework'
CACHE = Path.home() / '.gradle/caches/neoformruntime/intermediate_results'
VANILLA = None
TEXTURES = {}
OMITTED = set()
NEIGHBORS = {'up': (0, 1, 0), 'down': (0, -1, 0), 'north': (0, 0, -1),
             'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0)}
OPAQUE = {'red_wool', 'white_wool', 'dark_oak_planks', 'dark_oak_log', 'coarse_dirt',
          'grass_block', 'gravel', 'red_terracotta', 'smooth_stone', 'polished_blackstone',
          'hay_block', 'target', 'bone_block', 'barrel', 'crafting_table', 'bookshelf', 'smoker'}


def resource(location, kind, suffix):
    namespace, path = location.split(':') if ':' in location else ('minecraft', location)
    name = f'assets/{namespace}/{kind}/{path}.{suffix}'
    if namespace == 'minecraft': return VANILLA.read(name)
    return (ROOT / 'src/main/resources' / name).read_bytes()


@lru_cache(None)
def model(location):
    child = json.loads(resource(location, 'models', 'json'))
    parent = child.get('parent', '')
    if parent.removeprefix('minecraft:').startswith('builtin/'): return {**child, 'builtin': True}
    result = dict(model(parent)) if parent else {}
    result['textures'] = {**result.get('textures', {}), **child.get('textures', {})}
    if 'elements' in child: result['elements'] = child['elements']
    return result


def matches(condition, props):
    if 'OR' in condition: return any(matches(c, props) for c in condition['OR'])
    if 'AND' in condition: return all(matches(c, props) for c in condition['AND'])
    return all(props.get(k) in v.split('|') for k, v in condition.items())


@lru_cache(None)
def block_models(name, properties):
    props = {'facing': 'north', 'snowy': 'false', 'axis': 'y', 'hanging': 'false',
             'waterlogged': 'false', 'lit': 'false', **dict(properties)}
    state = json.loads(resource(name, 'blockstates', 'json'))
    result = []
    for selector, apply in state.get('variants', {}).items():
        condition = dict(v.split('=') for v in selector.split(',') if v)
        if matches(condition, props): result.append(apply[0] if isinstance(apply, list) else apply)
    for part in state.get('multipart', []):
        if matches(part.get('when', {}), props):
            apply = part['apply']
            result.append(apply[0] if isinstance(apply, list) else apply)
    if not result: raise ValueError(f'No block model: {name} {props}')
    return result


def texture(ref, textures):
    while ref.startswith('#'): ref = textures[ref[1:]]
    if ref not in TEXTURES:
        namespace, path = ref.split(':') if ':' in ref else ('minecraft', ref)
        target = OUTPUT / 'textures' / namespace / (path + '.png')
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(resource(ref, 'textures', 'png'))
        TEXTURES[ref] = str(target.relative_to(OUTPUT)).replace('\\', '/')
    return TEXTURES[ref]


def rotate(point, axis, degrees, origin=(8, 8, 8)):
    p = [point[i] - origin[i] for i in range(3)]
    a, b = {'x': (1, 2), 'y': (2, 0), 'z': (0, 1)}[axis]
    c, s = math.cos(math.radians(degrees)), math.sin(math.radians(degrees))
    p[a], p[b] = p[a] * c - p[b] * s, p[a] * s + p[b] * c
    return [p[i] + origin[i] for i in range(3)]


def face_points(side, lo, hi):
    x, y, z = lo
    X, Y, Z = hi
    return {
        'south': [(x, y, Z), (X, y, Z), (X, Y, Z), (x, Y, Z)],
        'north': [(X, y, z), (x, y, z), (x, Y, z), (X, Y, z)],
        'east': [(X, y, Z), (X, y, z), (X, Y, z), (X, Y, Z)],
        'west': [(x, y, z), (x, y, Z), (x, Y, Z), (x, Y, z)],
        'up': [(x, Y, Z), (X, Y, Z), (X, Y, z), (x, Y, z)],
        'down': [(x, y, z), (X, y, z), (X, y, Z), (x, y, Z)],
    }[side]


def default_uv(side, lo, hi):
    x, y, z = lo
    X, Y, Z = hi
    return {'down': [x, 16-Z, X, 16-z], 'up': [x, z, X, Z],
            'north': [16-X, 16-Y, 16-x, 16-y], 'south': [x, 16-Y, X, 16-y],
            'west': [z, 16-Y, Z, 16-y], 'east': [16-Z, 16-Y, 16-z, 16-y]}[side]


def template(path):
    root = nbtlib.load(path)
    palette = [(str(p['Name']), tuple(sorted((k, str(v)) for k, v in p.get('Properties', {}).items())))
               for p in root['palette']]
    blocks = {tuple(map(int, b['pos'])): palette[int(b['state'])] for b in root['blocks']}
    groups = {}
    for pos, (name, properties) in blocks.items():
        if name == 'minecraft:air': continue
        for apply in block_models(name, properties):
            data = model(apply['model'])
            if data.get('builtin') or not data.get('elements'):
                OMITTED.add(name)
                continue
            for element in data.get('elements', []):
                for side, face in element['faces'].items():
                    if side == 'down' and pos[1] == 0: continue
                    if 'cullface' in face and not (apply.get('x') or apply.get('y')):
                        direction = NEIGHBORS[face['cullface']]
                        neighbor = blocks.get(tuple(pos[i] + direction[i] for i in range(3)), ('', ()))[0]
                        if neighbor.removeprefix('minecraft:') in OPAQUE or neighbor == 'hemomancy:polished_venous_stone': continue
                    points = face_points(side, element['from'], element['to'])
                    if 'rotation' in element:
                        r = element['rotation']
                        points = [rotate(p, r['axis'], r['angle'], r['origin']) for p in points]
                    for axis in ('x', 'y'):
                        if apply.get(axis): points = [rotate(p, axis, -apply[axis]) for p in points]
                    points = [[round(pos[i] + p[i]/16, 5) for i in range(3)] for p in points]
                    uv = face.get('uv', default_uv(side, element['from'], element['to']))
                    u, v, U, V = uv
                    uvs = [[u/16, 1-V/16], [U/16, 1-V/16], [U/16, 1-v/16], [u/16, 1-v/16]]
                    turns = face.get('rotation', 0)//90
                    if turns: uvs = uvs[turns:] + uvs[:turns]
                    tex = texture(face['texture'], data['textures'])
                    color = '#809f50' if 'tintindex' in face else '#ffffff'
                    group = groups.setdefault(tex + color, {'texture': tex, 'color': color, 'vertices': [], 'uv': []})
                    for index in (0, 1, 2, 0, 2, 3):
                        group['vertices'].extend(points[index])
                        group['uv'].extend(uvs[index])
    return list(groups.values())


def main():
    global VANILLA
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for jar in CACHE.glob('stripClient_*_resourcesOutput.jar'):
        candidate = zipfile.ZipFile(jar)
        if json.loads(candidate.read('version.json'))['id'] == '1.21.1':
            VANILLA = candidate
            break
        candidate.close()
    if VANILLA is None: raise RuntimeError('Cached Minecraft 1.21.1 resource jar required')
    camps = {}
    for variant in VARIANTS:
        filename = f'troupe_camp_{variant}.nbt'
        camps[variant] = {'before': template(OUTPUT / 'before' / filename),
                          'after': template(DATA / 'structure' / filename)}
    (OUTPUT / 'camps.json').write_text(json.dumps(camps, separators=(',', ':')), encoding='utf-8')
    shutil.copyfile(ROOT / 'build/cinder-bellows-rework/three.module.js', OUTPUT / 'three.module.js')
    shutil.copyfile(Path(__file__).with_name('teaching_camp_preview.html'), OUTPUT / 'preview.html')
    print('Preview omits entity-rendered blocks:', ', '.join(sorted(OMITTED)))
    print(OUTPUT / 'preview.html')
    if '--serve' in sys.argv: serve(OUTPUT, 'teaching-camp', 8769)


if __name__ == '__main__': main()

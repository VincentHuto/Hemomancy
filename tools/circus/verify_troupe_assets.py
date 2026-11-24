"""Check the Troupe's authored geometry, UVs, teaching coverage and preserved arena.

Run from any directory. Requires Pillow and nbtlib, as does build_troupe_assets.py.
This verifies source assets; native rendering and natural terrain placement are separate.
"""
from pathlib import Path
import base64, io, json, math
import nbtlib
from PIL import Image

from build_marrow_juggler import PROJECT as JUGGLER_PROJECT, mesh_source as juggler_mesh_source
from build_gorebound_hulk import PROJECT as HULK_PROJECT, joint_name as hulk_joint_name

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
DATA = ROOT / 'src/main/resources/data/hemomancy'

def blocks(template):
    return {tuple(map(int, block['pos'])): (template['palette'][int(block['state'])], block.get('nbt'))
            for block in template['blocks']}

def verify_models():
    for name, texture in [('MarrowSpitterModel','marrow_spitter'), ('GoreboundHulkModel','gorebound_hulk'), ('CinderBellowsModel','cinder_bellows')]:
        bb = json.loads((ASSETS / 'models/entity/bbmodel' / (name + '.bbmodel')).read_text(encoding='utf-8'))
        atlas = Image.open(ASSETS / 'textures/entity/puppeteer_summon' / (texture + '.png')).convert('RGBA')
        embedded = Image.open(io.BytesIO(base64.b64decode(bb['textures'][0]['source'].split(',')[1]))).convert('RGBA')
        assert embedded.tobytes() == atlas.tobytes(), name + ': portable texture differs'
        occupied = set()
        for cube in bb['elements']:
            for key in ('from','to','origin'):
                assert all(abs(v*4 - round(v*4)) < .00001 for v in cube[key]), name + ': off-grid geometry'
            assert all(b-a >= .25 for a,b in zip(cube['from'],cube['to'])), name + ': undersized cube'
            for face in cube['faces'].values():
                u,v,s,t = face['uv']
                occupied.update((x,y) for x in range(math.floor(min(u,s)),math.ceil(max(u,s)))
                                for y in range(math.floor(min(v,t)),math.ceil(max(v,t))))
        for y in range(atlas.height):
            for x in range(atlas.width):
                assert bool(atlas.getpixel((x,y))[3]) == ((x,y) in occupied), name + ': UV paint mismatch'
        if name in ('CinderBellowsModel', 'MarrowSpitterModel', 'GoreboundHulkModel'):
            groups, cubes = {}, []

            def visit(group):
                assert group['uuid'] not in groups, name + ': joint appears more than once'
                assert all(abs(v*4-round(v*4)) < .00001 for v in group['origin']), name + ': joint is off-grid'
                groups[group['uuid']] = group
                for child in group['children']:
                    if isinstance(child, dict): visit(child)
                    else: cubes.append(child)

            for group in bb['outliner']: visit(group)
            assert len(cubes) == len(set(cubes)) == len(bb['elements']), name + ': cubes must appear exactly once'
            assert set(cubes) == {cube['uuid'] for cube in bb['elements']}, name + ': outliner has an unresolved cube'
            by_name = {group['name']: group for group in groups.values()}
            if name == 'CinderBellowsModel':
                source = ROOT / 'docs/art/cinder_bellows'
                assert (source / 'CinderBellowsPuppet.bbmodel').read_bytes() == (
                    ASSETS / 'models/entity/bbmodel/CinderBellowsModel.bbmodel').read_bytes()
                assert (source / 'cinder_bellows.png').read_bytes() == (
                    ASSETS / 'textures/entity/puppeteer_summon/cinder_bellows.png').read_bytes()
                parent = by_name['cinder_bellows']
                for i in range(1, 12):
                    segment = by_name[f'coil_{i:02}']
                    assert segment in parent['children'], 'Bellows tail must form a connected chain'
                    parent = segment
                for cube in bb['elements']:
                    w, h, d = (b-a for a, b in zip(cube['from'], cube['to']))
                    for face, (u_size, v_size) in {
                        'north': (w,h), 'south': (w,h), 'east': (d,h),
                        'west': (d,h), 'up': (w,d), 'down': (w,d)
                    }.items():
                        u, v, s, t = cube['faces'][face]['uv']
                        assert (abs(s-u), abs(t-v)) == (u_size, v_size), 'Bellows texel density changed'
            elif name == 'MarrowSpitterModel':
                assert JUGGLER_PROJECT.read_bytes() == (
                    ASSETS / 'models/entity/bbmodel/MarrowSpitterModel.bbmodel').read_bytes()
                assert JUGGLER_PROJECT.with_name('marrow_juggler.png').read_bytes() == (
                    ASSETS / 'textures/entity/puppeteer_summon/marrow_spitter.png').read_bytes()
                mesh = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MarrowSpitterMesh.java'
                assert mesh.read_text(encoding='utf-8') == juggler_mesh_source(bb), 'Stale Juggler mesh'
                for cube in bb['elements']:
                    w, h, d = (b-a for a,b in zip(cube['from'],cube['to']))
                    for face, dimensions in {'north': (w,h), 'south': (w,h), 'east': (d,h),
                                             'west': (d,h), 'up': (w,d), 'down': (w,d)}.items():
                        u,v,s,t = cube['faces'][face]['uv']
                        assert (abs(s-u), abs(t-v)) == dimensions, 'Juggler texel density changed'
            else:
                assert HULK_PROJECT.read_bytes() == (
                    ASSETS / 'models/entity/bbmodel/GoreboundHulkModel.bbmodel').read_bytes()
                assert HULK_PROJECT.with_name('gorebound_hulk.png').read_bytes() == (
                    ASSETS / 'textures/entity/puppeteer_summon/gorebound_hulk.png').read_bytes()
                root, = bb['outliner']
                by_name = {hulk_joint_name(group['name']): group
                           for group in groups.values() if group is not root}
                elements = {cube['uuid']: cube for cube in bb['elements']}
                for side in ('left', 'right'):
                    parent = by_name['chest']
                    for joint in ('arm', 'forearm', 'hand'):
                        segment = by_name[f'{side}_{joint}']
                        assert segment in parent['children'], 'Hulk arms must form connected chains'
                        parent = segment
                    forearm = by_name[f'{side}_forearm']
                    assert any(elements[child]['name'] == 'Iron wrist band'
                               for child in forearm['children'] if isinstance(child, str))
                    for digit in range(4):
                        assert by_name[f'{side}_finger_{digit}'] in parent['children']
                    parent = by_name['pelvis']
                    for joint in ('thigh', 'shin', 'foot'):
                        segment = by_name[f'{side}_{joint}']
                        assert segment in parent['children'], 'Hulk legs must form connected chains'
                        parent = segment
                for cube in bb['elements']:
                    w, h, d = (b-a for a, b in zip(cube['from'], cube['to']))
                    for face, (expected_u, expected_v) in {
                        'north': (w,h), 'south': (w,h), 'east': (d,h),
                        'west': (d,h), 'up': (w,d), 'down': (w,d)
                    }.items():
                        u,v,s,t = cube['faces'][face]['uv']
                        assert (abs(s-u), abs(t-v)) == (expected_u, expected_v), 'Hulk texel density changed'
            for animation in bb.get('animations', []):
                assert set(animation.get('animators', {})) <= groups.keys(), name + ': animation has an unresolved joint'
        print(f'{name}: {len(bb["elements"])} grid-aligned cubes, complete UV islands, portable atlas')


def verify():
    verify_models()
    legacy = nbtlib.load(ROOT / 'src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt')
    expanded = nbtlib.load(DATA / 'structure/crimson_troupe_pavilion.nbt')
    actual = blocks(expanded)
    for pos, block in blocks(legacy).items():
        assert actual[(pos[0]+16,pos[1],pos[2]+16)] == block, 'legacy arena block or NBT changed at ' + str(pos)
    assert tuple(map(int,expanded['size'])) == (65,14,65)
    assert len(expanded['entities']) == len(legacy['entities']) + 4
    print(f'Expanded pavilion: all {len(legacy["blocks"])} legacy blocks and block-entity NBT preserved; four added faculty')
    covered = set()
    for variant in ('air','blades','pageantry','embers','memory'):
        camp = nbtlib.load(DATA / ('structure/troupe_camp_' + variant + '.nbt'))
        assert tuple(map(int,camp['size'])) == (25,9,25)
        faculty = {str(e['nbt']['id']) for e in camp['entities']}
        assert 'hemomancy:circus_threadkeeper' in faculty
        covered.update(faculty)
    assert covered == {'hemomancy:circus_'+role for role in ('acrobat','knife_thrower','stilt_walker','strongman','beast_tamer','fire_eater','understudy','threadkeeper')}
    for name in ('cinder_bellows_frame','laughing_mask','mourning_mask','blank_mask','troupe_rigging'):
        folder = 'item' if name.endswith('_frame') else 'block'
        assert Image.open(ASSETS / f'textures/{folder}/{name}.png').size == (16,16)
    print('Five camps cover all seven professors with Threadkeepers; frame and props retain native 16px art')

if __name__ == '__main__':
    verify()

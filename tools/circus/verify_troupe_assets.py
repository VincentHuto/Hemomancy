"""Check the Troupe's authored geometry, UVs, teaching coverage and preserved arena.

Run from any directory. Requires Pillow and nbtlib, as does build_troupe_assets.py.
This verifies source assets; native rendering and natural terrain placement are separate.
"""
from pathlib import Path
import base64, io, json, math
import nbtlib
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
DATA = ROOT / 'src/main/resources/data/hemomancy'

def blocks(template):
    return {tuple(map(int, block['pos'])): (template['palette'][int(block['state'])], block.get('nbt'))
            for block in template['blocks']}

def verify():
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
        assert len({pixel for pixel in atlas.getdata() if pixel[3]}) <= 12, name + ': palette grew'
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
                parent = by_name['coil']
                for i in range(10):
                    segment = by_name['tail_' + str(i)]
                    assert segment in parent['children'], 'Bellows tail must form a connected chain'
                    parent = segment
            elif name == 'MarrowSpitterModel':
                body = by_name['body']
                for i in range(6):
                    parent = body
                    for joint in ('arm_', 'forearm_', 'hand_', 'fingers_'):
                        segment = by_name[joint + str(i)]
                        assert segment in parent['children'], 'Juggler arms must form connected chains'
                        parent = segment
                    assert by_name['thumb_' + str(i)] in by_name['hand_' + str(i)]['children']
                for i in range(3):
                    assert by_name['knife_' + str(i)] in body['children'], 'Juggler knife must move as one prop'
                for mask in ('laugh', 'mourn', 'blank'):
                    assert by_name['mask_' + mask] in by_name['hood']['children'], 'Juggler masks must follow the hood'
            else:
                for side in ('left', 'right'):
                    parent = by_name['chest']
                    for joint in ('arm', 'forearm', 'hand'):
                        segment = by_name[side + '_' + joint]
                        assert segment in parent['children'], 'Hulk arms must form connected chains'
                        parent = segment
                    assert by_name[side + '_binding'] in by_name[side + '_forearm']['children']
                    for digit in ('thumb', 'finger_0', 'finger_1', 'finger_2', 'finger_3'):
                        assert by_name[side + '_' + digit] in parent['children']
                    parent = by_name['pelvis']
                    for joint in ('thigh', 'shin', 'foot'):
                        segment = by_name[side + '_' + joint]
                        assert segment in parent['children'], 'Hulk legs must form connected chains'
                        parent = segment
            for animation in bb.get('animations', []):
                assert set(animation.get('animators', {})) <= groups.keys(), name + ': animation has an unresolved joint'
        print(f'{name}: {len(bb["elements"])} grid-aligned cubes, complete UV islands, portable atlas')
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

"""Verify UV scale, embedded/game atlas parity, and game export parity."""
import base64
import json
import struct
from pathlib import Path

root = Path(__file__).resolve().parents[2] / 'src/main/resources/assets/hemomancy'
for tier in ['condenser', 'athanor']:
    name = 'ghastly_alembic_' + tier
    source = json.loads((root/'models/block/bbmodel'/f'{name}.bbmodel').read_text(encoding='utf-8'))
    runtime = json.loads((root/'models/block'/f'{name}.json').read_text())
    atlas = (root/'textures/block'/f'{name}_atlas.png').read_bytes()
    assert atlas == base64.b64decode(source['textures'][0]['source'].split(',')[1])
    width,height = struct.unpack('>II',atlas[16:24])
    assert len(source['elements']) == len(runtime['elements'])
    rectangles = []
    for cube, exported in zip(source['elements'],runtime['elements']):
        assert cube['from'] == exported['from'] and cube['to'] == exported['to']
        assert all(round(v*4)==v*4 for field in ['from','to','origin'] for v in cube[field])
        dx,dy,dz = [b-a for a,b in zip(cube['from'],cube['to'])]
        for side,face in cube['faces'].items():
            assert exported['faces'][side] == dict(uv=face['uv'],texture='#atlas')
            u0,v0,u1,v1 = face['uv']
            measured = (abs(u1-u0)*width/16,abs(v1-v0)*height/16)
            expected = (dx,dy) if side in ['north','south'] else ((dz,dy) if side in ['east','west'] else (dx,dz))
            assert all(abs(a-b*4)<1e-8 for a,b in zip(measured,expected)), (cube['name'],side)
            rectangles.append((min(u0,u1),min(v0,v1),max(u0,u1),max(v0,v1)))
    for i,a in enumerate(rectangles):
        for b in rectangles[i+1:]:
            assert a[2]<=b[0] or b[2]<=a[0] or a[3]<=b[1] or b[3]<=a[1]
    print(f'{tier}: {len(source["elements"])} cubes, {len(rectangles)} proportional non-overlapping faces; embedded/runtime texture and geometry match')
states = json.loads((root/'blockstates/ghastly_alembic.json').read_text())['multipart']
assert len(states)==10
assert sum(p['when']['stage']=='0' for p in states)==8
assert {p['when']['stage'] for p in states if p['apply']['model'].endswith('upgraded')}=={'1','2'}
print('All stage/facing/lit blockstate entries verified')

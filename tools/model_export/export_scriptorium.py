"""Rebuild editable Scriptorium block/item models; no texture files are changed."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy/models'
layout = (ROOT / 'src/main/java/com/vincenthuto/hemomancy/common/enchanting/ScriptoriumLayout.java').read_text()
def coordinates(axis):
    return [float(v.strip().removesuffix('F')) for v in re.search(rf'{axis} = \{{([^}}]+)', layout)[1].split(',')]
CENTERS = list(zip(coordinates('X'), coordinates('Z')))
TEXTURES = {'particle':'hemomancy:block/blood_wood_planks', 'wood':'hemomancy:block/blood_wood_planks',
            'log':'hemomancy:block/blood_wood_log', 'iron':'hemomancy:block/hematic_iron_block',
            'glass':'hemomancy:block/sanguine_glass', 'crystal':'hemomancy:block/sanguine_omen',
            'book':'hemomancy:entity/liber_sanguinum'}

def cube(name, start, end, texture, rotation=None, uv=None):
    element = {'name':name, 'from':start, 'to':end,
               'faces':{face:{'texture':'#'+texture, 'uv':uv or [0,0,16,16]} for face in ('north','south','east','west','up','down')}}
    if rotation: element['rotation']=rotation
    return element

def build(tier):
    elements=[]
    def add(name,a,b,t='wood',rotation=None,uv=None): elements.append(cube(name,a,b,t,rotation,uv))
    add('lectern_foot',[1,0,1],[15,1.5,15])
    add('foot_bevel',[2,1.5,2],[14,2.5,14])
    add('bloodwood_stem',[5.5,2.5,6],[10.5,10.5,11],'log')
    add('lower_collar',[5,3,5.5],[11,4,11.5],'iron')
    add('upper_collar',[5,9,5.5],[11,10,11.5],'iron')
    slope={'origin':[8,11,8],'axis':'x','angle':-22.5}
    add('sloped_reading_desk',[1,10.5,2],[15,12,13],'wood',slope)
    add('front_book_rest',[1,12,2],[15,12.75,3],'wood',slope)
    for x,z in CENTERS:
        i=CENTERS.index((x,z))
        add(f'reservoir_{i}_glass',[x-1,18.5,z-1],[x+1,22.5,z+1],'glass')
        add(f'reservoir_{i}_lower_rim',[x-1.25,18,z-1.25],[x+1.25,18.5,z+1.25],'iron')
        add(f'reservoir_{i}_lid',[x-1.25,22.5,z-1.25],[x+1.25,23,z+1.25],'iron')
        add(f'reservoir_{i}_stopper',[x-.5,23,z-.5],[x+.5,23.5,z+.5],'wood')
    if tier>=5:
        for x in (2.5,12.75):
            add('vicar_bracket',[x,8,8],[x+.75,14,9],'iron')
            for y in (12.5,14,15.5):
                add('book_chain',[x,y,6.5],[x+.5,y+1,7],'iron')
    if tier>=7:
        add('cornerstone',[6,3.5,4.5],[10,7.5,6],'crystal')
        for x in (0,15):
            add('monolithic_rear_upright',[x,1,13],[x+1,16,14],'iron')
            add('restraint_shoulder',[x-1,15,12],[x+2,16,15],'iron')
        add('monolithic_rear_crossbar',[0,15,13],[16,16,14],'iron')
    return {'parent':'minecraft:block/block','render_type':'translucent','ambientocclusion':False,
            'textures':TEXTURES,'elements':elements}

for name,tier in [('enzymatic_scriptorium',3),('eightfold_scriptorium',5),('monolithic_scriptorium',7)]:
    model=build(tier)
    (ASSETS/'block'/f'{name}.json').write_text(json.dumps(model,indent=2)+'\n')
    # The placed station uses the animated Liber renderer; the inventory model includes an open book.
    item=json.loads(json.dumps(model))
    for side in (-1,1):
        a,b=([2,15.5,2],[8,16,10]) if side<0 else ([8,15.5,2],[14,16,10])
        rotation={'origin':[8,15.5,6],'axis':'x','angle':-45}
        item['elements'].append(cube('red_liber_cover',a,b,'book',rotation,[0,0,6,5]))
        a,b=([2.5,16,2.5],[7.75,16.75,9.5]) if side<0 else ([8.25,16,2.5],[13.5,16.75,9.5])
        item['elements'].append(cube('liber_pages',a,b,'book',rotation,[1,6,6,10]))
    item['display']={'gui':{'rotation':[30,225,0],'translation':[0,-3,0],'scale':[.48,.48,.48]},
                     'ground':{'translation':[0,2,0],'scale':[.3,.3,.3]},
                     'fixed':{'scale':[.5,.5,.5]}}
    (ASSETS/'item'/f'{name}.json').write_text(json.dumps(item,indent=2)+'\n')
    for e in item['elements']:
        assert all(-16<=v<=32 and v*4==round(v*4) for v in e['from']+e['to'])
        assert all(b>a for a,b in zip(e['from'],e['to']))
    assert len([e for e in model['elements'] if e['name'].endswith('_glass')])==8
    assert all(z>=8 for _,z in CENTERS)
print('Generated three station and item models; eight rear reservoirs per tier; quarter-grid geometry validated.')


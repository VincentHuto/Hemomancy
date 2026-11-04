"""Rebuild native Troupe models, UV atlases, and new structure templates.

The legacy pavilion NBT is an input and is never rewritten. Run from any directory.
Requires Pillow and nbtlib; model_export embeds the painted atlas into Blockbench.
"""
from pathlib import Path
from copy import deepcopy
import json
import nbtlib as nbt
from PIL import Image, ImageDraw
from build_cinder_bellows import build as build_cinder_bellows
from build_marrow_juggler import build as build_marrow_juggler
from build_gorebound_hulk import build as build_gorebound_hulk
from build_faculty import build as build_faculty
from build_teaching_camps import VARIANTS, build as build_teaching_camps
from troupe_templates import Template

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/hemomancy'
DATA = RES / 'data/hemomancy'

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(value, encoding='utf-8')

def js(path, value):
    write(path, json.dumps(value, indent=2) + '\n')

def native_items():
    # 16x16 lung-frame silhouette, drawn directly on the native transparent grid.
    image=Image.new('RGBA',(16,16));d=ImageDraw.Draw(image)
    d.line((5,2,5,12),fill='#ddd0a7',width=2);d.line((10,2,10,12),fill='#ddd0a7',width=2)
    d.rectangle((6,4,9,10),fill='#a64345');d.rectangle((7,5,8,8),fill='#ed9c64')
    d.line((3,11,6,14,10,14,13,11),fill='#714150',width=2);d.point((6,2),fill='#8b3546');d.point((9,2),fill='#8b3546')
    image.save(ASSETS/'textures/item/cinder_bellows_frame.png')
    for name in ['laughing_mask','mourning_mask','blank_mask','troupe_rigging']:
        im=Image.new('RGBA',(16,16),'#532436');d=ImageDraw.Draw(im)
        if name=='troupe_rigging':
            d.rectangle((6,0,9,15),fill='#b39a74');d.line((0,5,15,5),fill='#847060',width=2)
        else:
            d.rectangle((4,2,11,13),fill='#dfcea8')
            if name!='blank_mask':
                d.rectangle((5,5,6,6),fill='#402432');d.rectangle((9,5,10,6),fill='#402432')
                d.line((6,9,9,9),fill='#402432');d.line((5,8 if name=='laughing_mask' else 10,5,9),fill='#402432')
        out=ASSETS/'textures/block';im.save(out/f'{name}.png')
        texture='hemomancy:block/'+name
        if name=='troupe_rigging': elements=[{'from':[6,0,6],'to':[10,16,10]}]
        else: elements=[{'from':[3,2,7],'to':[13,14,9]}]
        for element in elements: element['faces']={f:{'texture':'#all'} for f in ['north','south','east','west','up','down']}
        js(ASSETS/'models/block'/f'{name}.json',{'textures':{'all':texture,'particle':texture},'elements':elements})
        js(ASSETS/'blockstates'/f'{name}.json',{'variants':{'facing='+direction:{'model':'hemomancy:block/'+name,'y':angle} for direction,angle in [('north',0),('east',90),('south',180),('west',270)]}})
        js(ASSETS/'models/item'/f'{name}.json',{'parent':'hemomancy:block/'+name})
        js(DATA/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'hemomancy:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
        js(DATA/'recipe'/f'{name}.json',{'type':'minecraft:crafting_shaped','pattern':['SSS','SBS','SSS'],
            'key':{'S':{'item':'minecraft:stick'},'B':{'item':'hemomancy:enthralling_filament'}},'result':{'id':'hemomancy:'+name,'count':4}})
    for clue in ['stage','loft','quarters','carousel']:
        name='circus_'+clue+'_record'
        js(ASSETS/'models/block'/f'{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'minecraft:block/stripped_dark_oak_log'}})
        js(ASSETS/'blockstates'/f'{name}.json',{'variants':{'':{'model':'hemomancy:block/'+name}}})
        js(ASSETS/'models/item'/f'{name}.json',{'parent':'hemomancy:block/'+name})
        js(DATA/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':[]})

def pool(name):
    js(DATA/'worldgen/template_pool'/name/'start_pool.json',{'name':'hemomancy:'+name+'/start_pool','fallback':'minecraft:empty',
     'elements':[{'weight':1,'element':{'location':'hemomancy:'+name,'processors':'minecraft:empty','projection':'rigid','element_type':'minecraft:single_pool_element'}}]})
    js(DATA/'worldgen/structure'/f'{name}.json',{'type':'hemomancy:circus_pavilion','start_pool':'hemomancy:'+name+'/start_pool',
        'size':1,'max_distance_from_center':64,'biomes':'#hemomancy:has_structure/circus_pavilion','step':'surface_structures',
        'terrain_adaptation':'beard_thin','start_height':{'absolute':0},'project_start_to_heightmap':'WORLD_SURFACE_WG','spawn_overrides':{}})

def structures():
    t=Template((65,14,65))
    # Translate every original arena state and entity exactly; preserve NBT for carousel/captives.
    legacy=nbt.load(ROOT/'src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt',gzipped=True)
    for b in legacy['blocks']:
        x,y,z=map(int,b['pos']);state=legacy['palette'][int(b['state'])]
        t.block(x+16,y,z+16,str(state['Name']),{str(k):str(v) for k,v in state.get('Properties',{}).items()})
        if 'nbt' in b:t.block_tags[x+16,y,z+16]=deepcopy(b['nbt'])
    for e in legacy['entities']:
        e=deepcopy(e);e['pos'][0]+=16;e['pos'][2]+=16;e['blockPos'][0]+=16;e['blockPos'][2]+=16;t.entities.append(e)
    # Six usable rooms, two practice courts, connected walks. Entrances face the southern approach.
    rooms=[(2,3,13,11),(50,3,13,11),(2,22,12,12),(51,22,12,12),(3,49,19,13),(43,49,19,13)]
    for x,z,w,h in rooms:t.room(x,z,w,h)
    for x in range(65):
        for z in range(65):
            if x in (14,15,49,50) or z in (14,15,48):
                t.block(x,0,z,'hemomancy:polished_venous_stone')
                for y in range(1,4):t.block(x,y,z,'minecraft:air')
    for x,z in [(23,53),(33,53)]:
        for dx in range(8):
            for dz in range(8):t.block(x+dx,0,z+dz,'minecraft:red_terracotta' if dx in (0,7) or dz in (0,7) else 'minecraft:smooth_stone')
    t.entity('circus_threadkeeper',8.5,1,7.5);t.entity('circus_understudy',56.5,1,55.5)
    t.entity('circus_strongman',8.5,1,26.5);t.entity('circus_beast_tamer',57.5,1,26.5)
    for x,y,z,block in [(5,1,6,'hemomancy:puppeteers_spindle'),(6,1,6,'minecraft:crafting_table'),
      (56,1,54,'hemomancy:circus_quarters_record'),(56,1,7,'minecraft:air'),(15,1,32,'hemomancy:impaled_procession_record'),
      (6,1,51,'minecraft:smoker'),(5,1,52,'minecraft:barrel'),(8,1,52,'minecraft:cauldron'),
      (52,1,25,'minecraft:hay_block'),(54,1,25,'minecraft:hay_block'),(5,1,24,'minecraft:anvil')]:t.block(x,y,z,block)
    # Raise the loft roof so a player has full headroom above the catwalk.
    for x in range(50,63):
        for z in range(3,14):
            for y in range(5,8):t.block(x,y,z,'minecraft:red_wool' if x in (50,62) or z in (3,13) else 'minecraft:air')
            t.block(x,8,z,'minecraft:red_wool')
    # Rigging loft: ladder ascent, raised catwalk, and hanging lines rather than a sealed box.
    for x in range(52,61):
        for z in range(5,12):t.block(x,3,z,'minecraft:dark_oak_slab',{'type':'bottom','waterlogged':'false'})
    for y in range(1,4):t.block(51,y,9,'minecraft:ladder',{'facing':'east','waterlogged':'false'})
    t.block(56,4,7,'hemomancy:circus_loft_record')
    for z in range(53,60):t.block(59,1,z,'hemomancy:blank_mask')
    t.save('crimson_troupe_pavilion')
    pool('crimson_troupe_pavilion')
    pavilion=json.loads((DATA/'worldgen/structure/circus_pavilion.json').read_text())
    pavilion['start_pool']='hemomancy:crimson_troupe_pavilion/start_pool';pavilion['max_distance_from_center']=64
    js(DATA/'worldgen/structure/circus_pavilion.json',pavilion)
    # Only circus_pavilion is placed as the expanded main site; omit duplicate placement entry.
    (DATA/'worldgen/structure/crimson_troupe_pavilion.json').unlink()
    build_teaching_camps()
    for variant in VARIANTS:
        name = 'troupe_camp_' + variant
        pool(name)
        js(DATA/'tags/worldgen/structure'/f'troupe_teacher_{variant}.json', {'replace':False,'values':['hemomancy:'+name]})
    js(DATA/'worldgen/structure_set/troupe_teaching_camps.json',{'structures':[{'structure':'hemomancy:troupe_camp_'+v,'weight':1} for v in VARIANTS],
        'placement':{'type':'minecraft:random_spread','spacing':24,'separation':8,'salt':2030148871}})
    t=Template((25,9,25))
    for x in range(3,22):
        for z in range(4,21):t.block(x,0,z,'minecraft:dark_oak_planks' if (x+z)%5 else 'minecraft:coarse_dirt')
    for x in (4,20):
        for y in range(1,7):t.block(x,y,15,'minecraft:dark_oak_log',{'axis':'y'})
    t.block(12,1,15,'hemomancy:circus_stage_record');t.block(5,1,15,'hemomancy:mourning_mask')
    t.save('troupe_abandoned_stage');pool('troupe_abandoned_stage')
    js(DATA/'worldgen/structure_set/troupe_abandoned_stages.json',{'structures':[{'structure':'hemomancy:troupe_abandoned_stage','weight':1}],
        'placement':{'type':'minecraft:random_spread','spacing':32,'separation':10,'salt':2030148873}})

if __name__=='__main__':
    build_gorebound_hulk()
    build_marrow_juggler()
    build_cinder_bellows()
    build_faculty()
    native_items();structures()
    print('Rebuilt three puppet models, four faculty models, native atlases, props, five camps, abandoned stage and expanded pavilion.')

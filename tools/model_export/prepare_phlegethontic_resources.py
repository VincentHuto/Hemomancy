"""Package generated albedo materials and the Phlegethontic data resources. Originals remain untouched."""
from pathlib import Path
from PIL import Image
import json, shutil

ROOT=Path(__file__).resolve().parents[2]
RES=ROOT/'src/main/resources'
ART=ROOT/'docs/phlegethontic-nether-worldgen/materials'
GENERATED=Path(r'C:\Users\Vince\.codex\generated_images\01a096e8-13da-7172-b4fb-00869c5f9018')
inputs={'scab':'exec-707714ce-6f40-4bc8-b6e3-17de3e155de7.png',
        'ichor':'exec-55f9d14f-d315-4282-8456-2bcaa935a2e7.png',
        'sagittary':'exec-80ce0c40-17f9-4508-aa13-2997a729c9fd.png'}
ART.mkdir(parents=True,exist_ok=True)
for name,source in inputs.items():
    dest=ART/(name+'-source.png')
    if not dest.exists():shutil.copyfile(GENERATED/source,dest)

def write(path,data):
    target=RES/path;target.parent.mkdir(parents=True,exist_ok=True)
    target.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')

textures=RES/'assets/hemomancy/textures'
(textures/'entity').mkdir(parents=True,exist_ok=True)
Image.open(ART/'scab-source.png').convert('RGB').resize((32,32),Image.Resampling.BOX).save(textures/'block/blood_scorched_scab.png')
shutil.copyfile(ROOT/'docs/phlegethontic-nether-worldgen/orcadian-horseman/orcadian_horseman_atlas.png',
                textures/'entity/excoriated_sagittary.png')
for suffix in ['still.png','still.png.mcmeta','flow.png','flow.png.mcmeta','overlay.png']:
    name='phlegethontic_ichor_'+suffix
    shutil.copyfile(ART/'ichor-16px'/name,textures/'block'/name)

write('assets/hemomancy/blockstates/blood_scorched_scab.json',{'variants':{'':{'model':'hemomancy:block/blood_scorched_scab'}}})
write('assets/hemomancy/models/block/blood_scorched_scab.json',{'parent':'minecraft:block/cube_all','textures':{'all':'hemomancy:block/blood_scorched_scab'}})
write('assets/hemomancy/models/item/blood_scorched_scab.json',{'parent':'hemomancy:block/blood_scorched_scab'})
write('assets/hemomancy/blockstates/phlegethontic_ichor_block.json',{'variants':{'':{'model':'hemomancy:block/phlegethontic_ichor'}}})
write('assets/hemomancy/models/block/phlegethontic_ichor.json',{'textures':{'particle':'hemomancy:block/phlegethontic_ichor_still'}})
write('assets/hemomancy/models/item/spawn_egg_excoriated_sagittary.json',{'parent':'minecraft:item/template_spawn_egg'})
write('data/hemomancy/loot_table/blocks/blood_scorched_scab.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'hemomancy:blood_scorched_scab'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
write('data/hemomancy/loot_table/entities/excoriated_sagittary.json',{'type':'minecraft:entity','pools':[
    {'rolls':{'type':'minecraft:uniform','min':1,'max':3},'entries':[{'type':'minecraft:item','name':'minecraft:bone'}]},
    {'rolls':{'type':'minecraft:uniform','min':1,'max':2},'entries':[{'type':'minecraft:item','name':'hemomancy:sanguine_formation'}],'conditions':[{'condition':'minecraft:killed_by_player'}]}]})
tags={
 'data/hemomancy/tags/block/phlegethontic_shore_blocks.json':['hemomancy:blood_scorched_scab','hemomancy:venous_stone','minecraft:blackstone','minecraft:basalt'],
 'data/hemomancy/tags/block/phlegethontic_vein_replaceable.json':['minecraft:netherrack','minecraft:blackstone','minecraft:basalt'],
 'data/hemomancy/tags/block/phlegethontic_basin_terrain_replaceable.json':['minecraft:netherrack','minecraft:blackstone','minecraft:basalt','minecraft:smooth_basalt','minecraft:lava','minecraft:gravel','minecraft:soul_sand','minecraft:soul_soil','minecraft:crimson_nylium','minecraft:warped_nylium','minecraft:crimson_roots','minecraft:warped_roots','minecraft:nether_sprouts','minecraft:crimson_fungus','minecraft:warped_fungus','hemomancy:venous_stone'],
 'data/hemomancy/tags/fluid/phlegethontic_ichor.json':['hemomancy:phlegethontic_ichor','hemomancy:phlegethontic_ichor_flowing'],
 'data/hemomancy/tags/entity_type/phlegethontic_river_guardians.json':['hemomancy:excoriated_sagittary'],
 'data/c/tags/worldgen/biome/is_nether.json':['hemomancy:phlegethontic_basin'],
 'data/minecraft/tags/worldgen/biome/is_nether.json':['hemomancy:phlegethontic_basin']}
for path,values in tags.items():
    existing=RES/path
    data=json.loads(existing.read_text(encoding='utf-8-sig')) if existing.exists() else {'replace':False,'values':[]}
    for value in values:
        if value not in data['values']:data['values'].append(value)
    write(path,data)
for tag in ['mineable/pickaxe','needs_stone_tool']:
    path=f'data/minecraft/tags/block/{tag}.json';existing=RES/path
    data=json.loads(existing.read_text(encoding='utf-8-sig')) if existing.exists() else {'replace':False,'values':[]}
    if 'hemomancy:blood_scorched_scab' not in data['values']:data['values'].append('hemomancy:blood_scorched_scab')
    write(path,data)
for feature in ['phlegethontic_basin_terrain','phlegethontic_vein']:
    write(f'data/hemomancy/worldgen/configured_feature/{feature}.json',{'type':'hemomancy:'+feature,'config':{}})
    write(f'data/hemomancy/worldgen/placed_feature/{feature}.json',{'feature':'hemomancy:'+feature,'placement':[]})
write('data/hemomancy/neoforge/biome_modifier/add_phlegethontic_veins.json',{'type':'neoforge:add_features','biomes':'#c:is_nether','features':'hemomancy:phlegethontic_vein','step':'underground_decoration'})
features=[[] for _ in range(11)];features[2]=['hemomancy:phlegethontic_basin_terrain'];features[7]=['minecraft:glowstone_extra']
write('data/hemomancy/worldgen/biome/phlegethontic_basin.json',{
    'temperature':2,'downfall':0,'has_precipitation':False,
    'effects':{'fog_color':0x250000,'sky_color':0x3A0505,'water_color':0x740000,'water_fog_color':0x210000,
               'ambient_sound':'minecraft:ambient.basalt_deltas.loop',
               'mood_sound':{'sound':'minecraft:ambient.basalt_deltas.mood','tick_delay':6000,'block_search_extent':8,'offset':2}},
    'spawners':{'monster':[{'type':'hemomancy:excoriated_sagittary','weight':4,'minCount':1,'maxCount':2}]},
    'spawn_costs':{},'carvers':{'air':['minecraft:nether_cave']},'features':features})
lang_path=RES/'assets/hemomancy/lang/en_us.json';text=lang_path.read_text(encoding='utf-8-sig');lang=json.loads(text)
entries={'block.hemomancy.blood_scorched_scab':'Blood-Scorched Scab','block.hemomancy.phlegethontic_ichor_block':'Phlegethontic Ichor',
         'fluid_type.hemomancy.phlegethontic_ichor':'Phlegethontic Ichor','biome.hemomancy.phlegethontic_basin':'Phlegethontic Basin',
         'entity.hemomancy.excoriated_sagittary':'Excoriated Sagittary','entity.hemomancy.recall_barb':'Recall Barb',
         'item.hemomancy.spawn_egg_excoriated_sagittary':'Excoriated Sagittary Spawn Egg',
         'message.hemomancy.recall_barb_escape':'Hold Sneak to tear the Recall Barb free.'}
new={k:v for k,v in entries.items() if k not in lang}
if new:
    end=text.rfind('}');prefix=text[:end].rstrip()
    lang_path.write_text(prefix+',\n'+',\n'.join('  '+json.dumps(k)+': '+json.dumps(v) for k,v in new.items())+'\n}\n',encoding='utf-8')
print('Packaged Phlegethontic materials, animation strips, registry resources, and language.')

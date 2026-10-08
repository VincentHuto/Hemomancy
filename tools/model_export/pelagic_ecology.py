"""Authored quarter-grid Pelagic rigs and UV atlases. Emits editable Blockbench projects and Java meshes.

Textures are preserved unless --textures is passed, so repainting an atlas is safe.
Python 3 + Pillow; run from any directory. Runtime animation lives in PelagicCreatureModel.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import argparse
import base64
import json
import math
import uuid

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic'
SIZE = (128, 256)


def uid(value):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, 'hemomancy:pelagic/' + value))


class Rig:
    def __init__(self, name, pivot=(0, 24, 0)):
        self.name, self.parts = name, []
        self.part('body', pivot=pivot)

    def part(self, name, parent='root', pivot=(0, 0, 0), rotation=(0, 0, 0)):
        self.parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=[]))
        return name

    def cube(self, part, box, material):
        assert all(v * 4 == round(v * 4) for v in box), (part, box)
        assert min(box[3:]) >= .25, (part, box)
        next(p for p in self.parts if p['name'] == part)['cubes'].append(dict(box=box, material=material))


def chiton():
    r = Rig('chiton')
    r.cube('body', (-4.5, -1.5, -4, 9, 1.5, 8), 'foot')
    r.cube('body', (-3.5, -1.5, -6, 7, 1.5, 12), 'foot')
    r.cube('body', (-2.25, -1.5, -7.5, 4.5, 1.5, 15), 'mantle')
    for i in range(8):
        width = [4, 6, 8, 8.5, 8.5, 8, 6, 4][i]
        p = r.part('plate' + str(i), 'body', (0, -1.5, -6 + i * 1.75))
        r.cube(p, (-width/2, -2, -1, width, 2, 2.25), 'plate')
        ridge = width - 2
        r.cube(p, (-ridge/2, -2.75, -.75, ridge, .75, 1.25), 'ridge')
    p = r.part('mouth', 'body', (0, -.75, -7))
    r.cube(p, (-1.5, -.25, -1, 3, .5, 1.5), 'mouth')
    return r


def pyrosome():
    r = Rig('pyrosome')
    for i in range(6):
        radius = 3.5 if i in (0, 5) else 4
        p = r.part('ring' + str(i), 'body', (0, -i * 3.5, 0))
        for box in [(-radius, -3.75, -radius, 1.25, 4, radius*2),
                    (radius-1.25, -3.75, -radius, 1.25, 4, radius*2),
                    (-radius+1.25, -3.75, -radius, radius*2-2.5, 4, 1.25),
                    (-radius+1.25, -3.75, radius-1.25, radius*2-2.5, 4, 1.25)]:
            r.cube(p, box, 'zooid')
    return r


def herring():
    r = Rig('pelagic_herring', (0, 21.5, 0))
    r.cube('body', (-1.5, -2, -4, 3, 4, 7.5), 'silver')
    r.cube('body', (-1, -1.5, -6, 2, 3, 2.25), 'fish_head')
    tail = r.part('tail', 'body', (0, 0, 3.25))
    r.cube(tail, (-.75, -1, -.25, 1.5, 2, 2), 'silver')
    for sign in (-1, 1):
        p = r.part('fork' + str(sign).replace('-', 'n'), tail, (0, 0, 1.5), (sign * 28, 0, 0))
        r.cube(p, (-.25, -1.5, -.25, .5, 3, 3), 'fin')
        p = r.part('fin' + str(sign).replace('-', 'n'), 'body', (sign * 1.25, .5, -2), (0, sign * 28, sign * -20))
        r.cube(p, (-.25, 0, 0, .5, 1.5, 3), 'fin')
    p = r.part('dorsal', 'body', (0, -1.5, -.5), (-16, 0, 0))
    r.cube(p, (-.25, -2, -1.25, .5, 2, 3.5), 'fin')
    return r


def siphonophore():
    r = Rig('siphonophore', (0, 12, 0))
    r.cube('body', (-2, -6, -2, 4, 5, 4), 'float')
    r.cube('body', (-.75, -1.5, -.75, 1.5, 14, 1.5), 'stem')
    for i in range(5):
        p = r.part('bell' + str(i), 'body', (0, i * 2.5, 0))
        side = -1 if i % 2 else 1
        r.cube(p, (side * 1.5 - 1.25, -1, -1.25, 2.5, 3, 2.5), 'nectophore')
    for i in range(6):
        angle = i * math.pi / 3
        parent = 'body'
        for j in range(4):
            pivot = (round(math.cos(angle)*2)*.75, 9, round(math.sin(angle)*2)*.75) if j == 0 else (0, 9.5, 0)
            p = r.part(f'filament{i}_{j}', parent, pivot)
            r.cube(p, (-.25, -.5, -.25, .5, 10, .5), 'filament')
            if j > 1:
                r.cube(p, (-.75, 7.5, -.5, 1.5, 1, 1), 'lure')
            parent = p
    return r


def comb_jelly():
    r = Rig('bloody_belly_comb_jelly', (0, 17.5, 0))
    r.cube('body', (-3.5, -4.5, -3.5, 7, 9, 7), 'glass')
    r.cube('body', (-2.5, -6, -2.5, 5, 1.5, 5), 'glass')
    r.cube('body', (-2.5, 4.5, -2.5, 5, 1.5, 5), 'glass')
    gut = r.part('gut', 'body')
    r.cube(gut, (-1.5, -2.5, -1.5, 3, 5.5, 3), 'gut')
    r.cube(gut, (-.5, -5.5, -.5, 1, 3, 1), 'gut')
    for i, (x, z) in enumerate([(-3.5, -2), (-3.5, 2), (3.5, -2), (3.5, 2), (-2, -3.5), (2, -3.5), (-2, 3.5), (2, 3.5)]):
        p = r.part('comb' + str(i), 'body', (x, 0, z))
        r.cube(p, (-.25, -4.5, -.25, .5, 9, .5), 'comb')
    for i, sign in enumerate((-1, 1)):
        p = r.part('lobe' + str(i), 'body', (sign * 2, 3.5, 0), (0, 0, sign * 15))
        r.cube(p, (-1.5, -.5, -2.5, 3, 3, 5), 'glass')
    return r


def hagfish():
    r = Rig('hagfish', (0, 21.5, -7))
    r.cube('body', (-2, -1.5, -3, 4, 3, 5), 'hag_head')
    parent = 'body'
    for i in range(6):
        p = r.part('tail' + str(i), parent, (0, 0, 1.5 if i == 0 else 3.5))
        width = 3.5 - i * .5
        r.cube(p, (-width/2, -1.25 + i*.125 if i%2==0 else -1, -.5, width, 2.5 if i<3 else 2, 4.25), 'hag_skin')
        parent = p
    for i in range(4):
        p = r.part('barbel' + str(i), 'body', ((i%2*2-1)*1.25, .25, -2.5), ((i//2*2-1)*20, (i%2*2-1)*25, 0))
        r.cube(p, (-.25, -.25, -3, .5, .5, 3.5), 'barbel')
    p = r.part('slime', 'body')
    r.cube(p, (-4, -2, -4, 8, 4, 10), 'slime')
    return r


def colony(kind):
    r = Rig(kind)
    if kind == 'tidepool_anemone':
        r.cube('body', (-4, -2, -4, 8, 2, 8), 'anemone_base')
        for i in range(8):
            angle = i * math.pi / 4
            p = r.part('plume' + str(i), 'body', (round(math.cos(angle)*3), -1.5, round(math.sin(angle)*3)), (-math.sin(angle)*25, 0, math.cos(angle)*25))
            r.cube(p, (-.5, -5, -.5, 1, 5.25, 1), 'anemone_tip')
        r.cube('body', (-1.5, -2.5, -1.5, 3, .5, 3), 'mouth')
    else:
        for i, (x,z,height) in enumerate([(-3,-2,18),(2,-3,24),(0,2,21),(4,3,15),(-4,3,12)]):
            if kind == 'bone_worm_colony':
                height = 3 + i % 3
            tube = r.part('tube'+str(i), 'body', (x, 0, z), (0, 0, (i-2)*2))
            r.cube(tube, (-1, -height, -1, 2, height, 2), 'tube' if kind != 'bone_worm_colony' else 'worm')
            plume = r.part('plume'+str(i), tube, (0, -height, 0))
            r.cube(plume, (-1.5, -3, -.5, 3, 3.5, 1), 'red_plume')
            r.cube(plume, (-.5, -3, -1.5, 1, 3.5, 3), 'red_plume')
    return r


COLORS = dict(foot=(80,65,61,255), mantle=(114,91,80,255), plate=(68,87,78,255), ridge=(111,137,113,255),
              mouth=(93,31,42,255), zooid=(127,160,172,145), silver=(160,184,192,255), fish_head=(132,161,172,255),
              fin=(87,115,129,245), float=(165,115,141,195), stem=(143,52,83,220), nectophore=(132,171,184,145),
              filament=(177,106,136,200), lure=(244,174,157,240), glass=(121,128,177,68), gut=(161,26,57,240),
              comb=(171,200,228,220), hag_head=(132,91,107,255), hag_skin=(108,77,93,255), barbel=(190,147,151,255),
              slime=(176,164,190,60), anemone_base=(114,49,68,255), anemone_tip=(203,101,116,255),
              tube=(211,204,178,255), worm=(180,87,102,255), red_plume=(190,43,73,255))


def faces(u, v, w, h, d):
    return dict(north=[u+d,v+d,u+d+w,v+d+h], east=[u,v+d,u+d,v+d+h],
                south=[u+d+w+d,v+d,u+d+w+d+w,v+d+h], west=[u+d+w,v+d,u+d+w+d,v+d+h],
                up=[u+d+w,v+d,u+d,v], down=[u+d+w+w,v,u+d+w,v+d])


def export(rig, repaint):
    image, glow = Image.new('RGBA', SIZE), Image.new('RGBA', SIZE)
    u, v, row_height = 1, 1, 0
    elements, groups, world = [], {}, {'root': (0,0,0)}
    lines = ['        MeshDefinition mesh = new MeshDefinition();', '        PartDefinition root = mesh.getRoot();']
    for index, part in enumerate(rig.parts):
        name, parent, pivot = part['name'], part['parent'], part['pivot']
        absolute = tuple(a+b for a,b in zip(world[parent], pivot)); world[name] = absolute
        group = dict(name=name, origin=[absolute[0],24-absolute[1],absolute[2]],
                     rotation=[-part['rotation'][0],part['rotation'][1],-part['rotation'][2]],
                     uuid=uid(rig.name+'/'+name), children=[], export=True, isOpen=True, visibility=True)
        groups[name] = group
        if parent != 'root': groups[parent]['children'].append(group)
        chain = 'CubeListBuilder.create()'
        for ci, cube in enumerate(part['cubes']):
            x,y,z,w,h,d = cube['box']; material=cube['material']
            width, height = math.ceil(2*(w+d))+2, math.ceil(h+d)+2
            if u + width >= SIZE[0]: u, v, row_height = 1, v+row_height+1, 0
            assert v+height < SIZE[1], (rig.name, v, height)
            row_height = max(row_height, height)
            uv = faces(u,v,w,h,d)
            color = COLORS[material]
            for face, rect in uv.items():
                x0,x1=sorted((rect[0],rect[2])); y0,y1=sorted((rect[1],rect[3]))
                for py in range(math.floor(y0),math.ceil(y1)):
                    for px in range(math.floor(x0),math.ceil(x1)):
                        lx,ly=px-math.floor(x0),py-math.floor(y0)
                        shade = {'up':18,'down':-23,'east':-10,'west':-7,'north':5,'south':0}[face]
                        shade += (6 if (lx//2+ly//3)%3==0 else -3)
                        if material in ('plate','tube','fin','worm','hag_skin') and lx%3==0: shade-=14
                        if material == 'ridge' and ly == 0: shade -= 22
                        if material == 'silver' and ly == max(1,int(h/2)): rgb=(153,56,76)
                        elif material == 'fish_head' and face in ('east','west') and lx==0 and ly==1: rgb=(13,24,34)
                        elif material == 'gut' and lx==1: rgb=(219,50,70)
                        else: rgb=tuple(max(0,min(255,c+shade)) for c in color[:3])
                        image.putpixel((px,py),rgb+(color[3],))
                        luminous = material in ('comb','lure') or material=='zooid' and lx%3==1 and ly%3==1
                        if luminous:
                            tint=[(152,223,225),(224,153,217),(243,189,145)][ly%3]
                            glow.putpixel((px,py),tint+(210,))
            eid=uid(rig.name+'/'+name+'/'+str(ci)); group['children'].append(eid)
            elements.append(dict(name=name+'_'+str(ci),uuid=eid,type='cube',from_=[absolute[0]+x,24-(absolute[1]+y+h),absolute[2]+z],
                                 to=[absolute[0]+x+w,24-(absolute[1]+y),absolute[2]+z+d],origin=group['origin'],uv_offset=[u,v],
                                 autouv=0,faces={key:dict(uv=val,texture=0) for key,val in uv.items()}))
            elements[-1]['from']=elements[-1].pop('from_')
            chain += f'\n                .texOffs({u}, {v}).addBox({x}F, {y}F, {z}F, {w}F, {h}F, {d}F)'
            u += width
        rot=[math.radians(a) for a in part['rotation']]
        pose=', '.join(f'{float(n):.7f}F' for n in (*pivot,*rot))
        lines.append(f'        PartDefinition {name} = {parent}.addOrReplaceChild("{name}", {chain}, PartPose.offsetAndRotation({pose}));')
    tex=ASSETS/'textures/entity/pelagic'/f'{rig.name}.png'; tex.parent.mkdir(parents=True,exist_ok=True)
    if repaint or not tex.exists(): image.save(tex)
    glow_path=tex.with_name(rig.name+'_glow.png')
    if repaint or not glow_path.exists(): glow.save(glow_path)
    source='data:image/png;base64,'+base64.b64encode(tex.read_bytes()).decode()
    bb=dict(meta=dict(format_version='4.10',model_format='free',box_uv=True),name=rig.name,model_identifier=rig.name,
            resolution=dict(width=SIZE[0],height=SIZE[1]),elements=elements,outliner=[groups['body']],
            textures=[dict(name=tex.name,uuid=uid(rig.name+'/texture'),id='0',source=source,mode='bitmap',render_mode='default',visible=True)],
            animations=[])
    # Preview animation is editable in Blockbench; runtime uses the same named joints.
    animated=next((p for p in rig.parts if p['name'] not in ('body','gut','slime')),rig.parts[0])
    aid=groups[animated['name']]['uuid']
    bb['animations'].append(dict(uuid=uid(rig.name+'/idle'),name='idle',loop='loop',length=2,
        animators={aid:dict(name=animated['name'],type='bone',keyframes=[dict(channel='rotation',time=t,interpolation='catmullrom',
            data_points=[dict(x=0,y=a,z=0)],uuid=uid(rig.name+'/idle/'+str(t))) for t,a in [(0,0),(.5,8),(1,0),(1.5,-8),(2,0)]])}))
    bbpath=ASSETS/'models/entity/bbmodel/pelagic'/f'{rig.name}.bbmodel'; bbpath.parent.mkdir(parents=True,exist_ok=True)
    bbpath.write_text(json.dumps(bb,indent=2)+'\n',encoding='utf-8')
    lines.append(f'        return LayerDefinition.create(mesh, {SIZE[0]}, {SIZE[1]});')
    return '\n'.join(lines)


def main():
    parser=argparse.ArgumentParser(); parser.add_argument('--textures',action='store_true'); args=parser.parse_args()
    rigs=[chiton(),pyrosome(),herring(),siphonophore(),comb_jelly(),hagfish()]+[colony(k) for k in ('tidepool_anemone','bone_worm_colony','giant_tube_worm_colony')]
    methods=[]
    for rig in rigs: methods.append(f'    private static LayerDefinition {rig.name}() {{\n{export(rig,args.textures)}\n    }}')
    cases='\n'.join(f'            case "{r.name}" -> {r.name}();' for r in rigs)
    source='''package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated from tools/model_export/pelagic_ecology.py. Animation lives in PelagicCreatureModel. */
public final class PelagicMeshes {
    private PelagicMeshes() {}
    public static LayerDefinition create(String id) {
        return switch (id) {
'''+cases+'''
            default -> throw new IllegalArgumentException("Unknown Pelagic rig " + id);
        };
    }
'''+ '\n\n'.join(methods)+'\n}\n'
    JAVA.mkdir(parents=True,exist_ok=True); (JAVA/'PelagicMeshes.java').write_text(source,encoding='utf-8')
    growth_assets(args.textures)
    print('Exported',len(rigs),'quarter-grid rigs, Blockbench projects and matching UV atlases.')


def growth_assets(repaint):
    def save_json(path, data):
        target=ASSETS/path; target.parent.mkdir(parents=True,exist_ok=True); target.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
    for kind in ('hematic_algal_crust','tidepool_anemone','bone_worm_colony','giant_tube_worm_colony'):
        icon=Image.new('RGBA',(16,16)); pen=ImageDraw.Draw(icon)
        if kind=='hematic_algal_crust':
            for rect in [(1,3,5,6),(4,1,8,4),(7,4,12,8),(2,8,7,13),(7,9,13,14),(11,2,14,5)]:
                pen.rectangle(rect,fill='#5b333c'); x,y,xx,yy=rect
                pen.rectangle((x+1,y+1,xx,yy),fill='#963e50'); pen.point((x+1,y+1),fill='#c76670')
            for x,y in [(5,4),(9,6),(4,10),(11,11),(12,4),(7,2),(6,12)]: pen.line((x,y,x+1,y),fill='#799073')
        elif kind=='tidepool_anemone':
            pen.rectangle((4,11,12,13),fill='#76364e'); pen.rectangle((5,13,11,14),fill='#a34e66')
            for x,y,xx in [(3,4,5),(6,2,7),(10,3,9),(13,5,11),(1,7,4)]:
                pen.line((x,y,xx,12),fill='#c97980',width=2); pen.point((x,y),fill='#f4b2a0')
            pen.rectangle((7,10,9,11),fill='#4a253b')
        elif kind=='bone_worm_colony':
            pen.rectangle((2,12,13,14),fill='#c6c1a0')
            for x,y in [(3,8),(6,5),(10,7),(13,4)]:
                pen.line((x,13,x,y),fill='#d79094',width=2)
                pen.line((x-1,y-2,x+1,y-2),fill='#c24c65'); pen.point((x,y-3),fill='#f2a09a')
        else:
            for x,y in [(3,7),(7,3),(11,5)]:
                pen.rectangle((x,y,x+1,14),fill='#d3cab0'); pen.line((x+1,y+1,x+1,14),fill='#8e8980')
                pen.rectangle((x-1,y-3,x+2,y-1),fill='#96354d'); pen.line((x,y-4,x,y-1),fill='#e17b80')
        path=ASSETS/'textures/item'/f'{kind}.png'; path.parent.mkdir(parents=True,exist_ok=True)
        if repaint or not path.exists(): icon.save(path)
        if kind=='hematic_algal_crust':
            block_path=ASSETS/'textures/block'/f'{kind}.png'
            if repaint or not block_path.exists(): icon.save(block_path)
            model=dict(render_type='minecraft:cutout',ambientocclusion=False,textures=dict(particle='hemomancy:block/'+kind,all='hemomancy:block/'+kind),
                       elements=[{'from':[0,0,0],'to':[16,.25,16],'faces':{'up':dict(texture='#all',uv=[0,0,16,16]),'down':dict(texture='#all',uv=[0,0,16,16])}}])
        else:
            model=dict(textures=dict(particle='hemomancy:item/'+kind),elements=[])
        save_json(Path('models/block')/(kind+'.json'),model)
        save_json(Path('models/item')/(kind+'.json'),dict(parent='minecraft:item/generated',textures=dict(layer0='hemomancy:item/'+kind)))
        save_json(Path('blockstates')/(kind+'.json'),dict(variants={'':dict(model='hemomancy:block/'+kind)}))


if __name__ == '__main__': main()

"""Build the microscope model and create missing GUI textures.

Requires Pillow. Edit GUI PNGs directly; existing GUI textures are preserved by default.
Pass --reset-gui-textures only to deliberately replace all GUI artwork with these defaults.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import base64
import json
import math
import random
import uuid
import argparse

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--reset-gui-textures', action='store_true', help='Replace existing GUI PNGs with the original generated defaults')
parser.add_argument('--model-only', action='store_true', help='Rebuild geometry without repainting any textures')
args = parser.parse_args()

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
GUI = ASSETS / 'textures/gui/microscope'
if not args.model_only:
    GUI.mkdir(parents=True, exist_ok=True)

    def save_gui(image, name):
        path = GUI / (name + '.png')
        if args.reset_gui_textures or not path.exists():
            path.parent.mkdir(parents=True, exist_ok=True)
            image.save(path)

    def save_icons(defaults, group, names):
        # Missing icons inherit existing painted atlas pixels; edited standalone icons win.
        legacy = GUI / (group + '.png')
        source = Image.open(legacy) if legacy.exists() and not args.reset_gui_textures else defaults
        try:
            for i, name in enumerate(names):
                save_gui(source.crop((i * 32, 0, (i + 1) * 32, 32)), group + '/' + name)
        finally:
            if source is not defaults:
                source.close()

    # Static screen surfaces are separate, directly editable sprites. Their PNG alpha is final.
    save_gui(Image.new('RGBA', (16, 16), (16, 13, 10, 96)), 'screen_dimming')
    readout = Image.new('RGBA', (168, 240), (28, 24, 20, 240))
    ImageDraw.Draw(readout).line((0, 0, 167, 0), fill=(170, 134, 80, 255))
    save_gui(readout, 'readout')
    save_gui(Image.new('RGBA', (148, 3), (57, 49, 40, 255)), 'progress_track')
    save_gui(Image.new('RGBA', (148, 3), (179, 155, 105, 255)), 'progress_fill')
    link = Image.new('RGBA', (32, 32))
    ImageDraw.Draw(link).line((6, 21, 25, 12), fill=(111, 113, 70, 112))
    save_gui(link, 'ductilis_link')

    def cell(kind):
        im = Image.new('RGBA', (32, 32))
        d = ImageDraw.Draw(im)
        white, shade, dark = (240, 240, 240, 230), (150, 150, 150, 200), (60, 60, 60, 220)
        if kind == 0:  # paired budding membranes
            d.ellipse((3, 7, 21, 26), fill=shade, outline=white, width=2)
            d.ellipse((15, 4, 28, 17), fill=shade, outline=white, width=2)
            d.ellipse((8, 12, 16, 21), fill=dark)
            d.ellipse((21, 20, 25, 24), fill=white)
        elif kind == 1:  # ragged corona, charred points
            points = [(16 + math.cos(i*math.pi/12)*(14 if i%2 == 0 else 9), 16 + math.sin(i*math.pi/12)*(14 if i%2 == 0 else 9)) for i in range(24)]
            d.polygon(points, fill=dark)
            d.ellipse((6, 6, 26, 26), fill=shade, outline=white, width=2)
            d.ellipse((11, 11, 21, 21), fill=dark)
        elif kind == 2:  # branching dendrites
            for p in [[(3,3),(10,10),(16,16),(25,23),(29,29)],[(3,25),(10,21),(16,16),(22,8),(28,4)],[(10,10),(13,3)],[(25,23),(29,17)],[(10,21),(7,29)]]:
                d.line(p, fill=white, width=1)
            d.ellipse((12,12,20,20), fill=shade, outline=white)
        elif kind == 3:  # haloed disc with sharp rays
            d.ellipse((4,4,28,28), outline=shade)
            d.ellipse((7,7,25,25), fill=shade, outline=white, width=2)
            d.ellipse((12,12,20,20), fill=white)
            d.line((16,0,16,5), fill=white); d.line((16,27,16,31), fill=white)
            d.line((0,16,5,16), fill=white); d.line((27,16,31,16), fill=white)
        elif kind == 4:  # collapsed hollow membrane
            d.polygon([(3,9),(14,4),(27,8),(23,14),(28,24),(15,28),(5,23),(9,17)], fill=shade)
            d.polygon([(8,10),(15,8),(22,10),(18,15),(24,22),(15,24),(9,21),(13,17)], fill=(0,0,0,0))
            d.line([(3,9),(14,4),(27,8)], fill=white, width=2)
        elif kind == 5:  # faceted ice disc
            pts = [(16+13*math.cos(i*math.pi/3),16+13*math.sin(i*math.pi/3)) for i in range(6)]
            d.polygon(pts, fill=shade, outline=white)
            for p in pts: d.line([p,(16,16)], fill=white)
            d.ellipse((12,12,20,20), fill=dark)
        elif kind == 6:  # angular platelets, rigid chain sockets
            d.polygon([(3,9),(20,5),(28,12),(26,23),(10,26),(3,19)], fill=shade, outline=white)
            d.line([(5,12),(19,9),(24,14),(22,21),(10,23)], fill=dark, width=2)
            d.rectangle((0,14,5,17), fill=white); d.rectangle((26,14,31,17), fill=white)
        else:  # broken silhouette
            d.arc((4,4,27,27), 20, 140, fill=white, width=3)
            d.arc((4,4,27,27), 180, 285, fill=shade, width=4)
            d.polygon([(12,10),(23,14),(16,23),(9,19)], fill=(140,140,140,110))
            d.ellipse((26,5,28,7), fill=white)
        return im

    atlas = Image.new('RGBA', (256,32))
    for i in range(8): atlas.paste(cell(i), (i*32,0))
    save_icons(atlas, 'tendencies', ['animus', 'flammeus', 'ductilis', 'lux', 'mortem', 'congeatio', 'ferric', 'tenebris'])

    motifs = Image.new('RGBA',(384,32))
    for i in range(12):
        im=Image.new('RGBA',(32,32)); d=ImageDraw.Draw(im); c=(225,230,220,225)
        if i == 0:
            d.ellipse((8,5,22,19),outline=c); d.arc((10,7,20,17),190,265,fill=c,width=2)
            d.arc((1,16,30,30),180,320,fill=c)
        elif i == 1:
            d.line((5,28,25,3),fill=c)
            for j in range(6):
                x,y=8+j*2,24-j*3; d.line((x,y,x-3,y-8),fill=c); d.line((x,y,x+9,y-2),fill=c)
        elif i == 2:
            d.polygon([(5,28),(12,8),(13,25)],fill=c); d.polygon([(17,25),(25,2),(24,23)],fill=c)
            d.ellipse((4,4,7,7),fill=c)
        elif i == 3:
            for j in range(4): d.ellipse((9,3+j*6,23,12+j*6),outline=c,width=2)
        elif i == 4:
            for j in range(6):
                a=j*math.pi/3; p=(16+14*math.cos(a),16+14*math.sin(a)); d.line([(16,16),p],fill=c)
                d.line([(16+8*math.cos(a+.4),16+8*math.sin(a+.4)),(16+10*math.cos(a),16+10*math.sin(a)),(16+8*math.cos(a-.4),16+8*math.sin(a-.4))],fill=c)
        elif i == 5:
            d.line([(10,29),(7,21),(12,12),(9,2)],fill=c); d.line([(23,28),(20,18),(25,9)],fill=c)
            d.rectangle((17,23,20,26),fill=c)
        elif i == 6:
            for box in [(2,3,10,6),(22,9,27,12),(8,23,16,26)]: d.rectangle(box,fill=c)
            d.arc((6,7,25,26),40,140,fill=c)
        elif i == 7:
            d.ellipse((3,11,29,21),outline=c,width=2); d.line((7,17,24,15),fill=c)
        elif i == 8:
            for p in [[(2,30),(9,21),(14,15),(20,7),(28,1)],[(9,21),(3,16),(1,9)],[(14,15),(22,17),(30,13)],[(20,7),(15,4),(15,1)],[(22,17),(25,25),(31,28)]]: d.line(p,fill=c)
        elif i == 9:
            d.ellipse((4,4,28,28),outline=c,width=2)
            for x,y in [(12,9),(20,13),(11,21),(22,22)]: d.ellipse((x,y,x+3,y+3),fill=c)
        elif i == 10:
            d.line([(1,9),(12,11),(18,20),(30,23)],fill=c)
            d.line([(1,13),(10,15),(15,24),(28,28)],fill=c)
            for x,y in [(7,4),(24,15),(12,27)]: d.rectangle((x,y,x+3,y+2),fill=c)
        else: d.ellipse((13,13,18,18),fill=c)
        motifs.paste(im,(i*32,0))
    save_icons(motifs, 'properties', ['aquatic', 'flying', 'venomous', 'arthropod', 'cold_native', 'nether_native', 'ender', 'undead', 'fungal', 'explosive', 'burrowing', 'neutral'])

    rng=random.Random(1940)
    plasma=Image.new('RGBA',(256,256)); px=plasma.load()
    for y in range(256):
        for x in range(256):
            v=math.sin(x*.046+math.sin(y*.038)*2)*4+math.cos(y*.024)*3+rng.random()*3
            alpha = 180 if math.hypot(x-127.5,y-127.5) <= 106 else 0
            px[x,y]=(int(49+v),int(31+v*.6),int(28+v*.45),alpha)
    save_gui(plasma, 'plasma')
    frame=Image.new('RGBA',(256,256)); px=frame.load()
    for y in range(256):
        for x in range(256):
            radius=math.hypot(x-127.5,y-127.5)
            noise=rng.random()*8
            if radius > 122: px[x,y]=(142,178,177,85)
            elif radius > 106:
                bevel=math.sin((radius-106)/16*math.pi)*28
                light=(128-x-y)*.06
                px[x,y]=(int(69+bevel+noise+light),int(53+bevel*.8+noise+light),int(31+bevel*.45+noise),255)
            elif radius > 99: px[x,y]=(10,8,7,int((radius-99)/7*170))
    d=ImageDraw.Draw(frame)
    for a in [45,135,225,315]:
        x=128+math.cos(math.radians(a))*115; y=128+math.sin(math.radians(a))*115
        d.ellipse((x-3,y-3,x+3,y+3),fill=(118,105,78),outline=(24,23,20)); d.line((x-2,y,x+2,y),fill=(30,28,23))
    for i in range(85):
        x,y=rng.randrange(30,226),rng.randrange(30,226)
        if math.hypot(x-128,y-128)<98: d.point((x,y),fill=(217,207,167,24))
    d.arc((35,31,216,222),200,248,fill=(235,224,192,32),width=1)
    save_gui(frame, 'frame')

    # Painted 32px material atlas: brass bands, stitched leather, steel teeth, imperfect glass.
    tex=Image.new('RGBA',(32,32)); d=ImageDraw.Draw(tex)
    colors=[(118,87,43),(72,43,30),(107,110,101),(116,153,149)]
    for y in range(32):
        for x in range(32):
            material=x//8; c=colors[material]; v=rng.randrange(-10,11)+(8 if x%8==1 else 0)
            tex.putpixel((x,y),tuple(max(0,min(255,z+v)) for z in c)+(255,))
    for y in [2,14,29]: d.line((0,y,7,y),fill=(182,142,74))
    for y in range(2,32,5): d.line((10,y,12,y+1),fill=(132,94,62))
    for y in range(0,32,4): d.line((17,y,22,y),fill=(51,53,51))
    d.line((25,5,29,2),fill=(195,225,214)); d.line((27,21,30,18),fill=(169,196,185))
    tex.save(ASSETS/'textures/item/hematic_microscope.png')

elements=[]
def box(name,lo,hi,material):
    faces={f:{'uv':[material*4,0,material*4+4,16],'texture':'#0'} for f in ['north','south','east','west','up','down']}
    elements.append({'name':name,'from':lo,'to':hi,'faces':faces})
box('leather_grip',[6.5,1,7],[9.5,8,10],1)
box('grip_ferrule',[6,7,6.5],[10,8.5,10.5],0)
# Four bands and a back wall leave a real recess for the vial's broad head.
box('tube_front',[6,8,4],[10,12,6.5],0)
box('tube_back',[6,8,9.5],[10,12,12],0)
box('tube_bottom',[6,8,6.5],[10,8.5,9.5],0)
box('tube_top',[6,11.5,6.5],[10,12,9.5],0)
box('socket_back',[7.8,8.5,6.5],[10,11.5,9.5],2)
box('socket_lower_lip',[5.5,8.2,6.2],[6.1,8.5,9.8],2)
box('socket_upper_lip',[5.5,11.5,6.2],[6.1,11.8,9.8],2)
box('socket_front_lip',[5.5,8.5,6.2],[6.1,11.5,6.5],2)
box('socket_rear_lip',[5.5,8.5,9.5],[6.1,11.5,9.8],2)
box('tube_upper_facet',[6.5,12,4.5],[9.5,12.5,11.5],0)
box('eyepiece_rim',[5.5,7.5,11],[10.5,12.5,13],2)
box('eyepiece_glass',[6.2,8.2,13],[9.8,11.8,13.2],3)
box('objective_rim',[6.5,8.5,2.5],[9.5,11.5,4],0)
box('objective_glass',[7,9,2.2],[9,11,2.5],3)
box('focus_wheel',[10,8.5,6.5],[11,11.5,9.5],2)
box('slide_support',[7.5,4.5,3],[8.5,8,4],0)
box('glass_slide',[4.5,7,1.5],[11.5,7.3,5],3)
box('slide_left_clamp',[4.5,7.3,2],[5.2,7.8,4.5],2)
box('slide_right_clamp',[10.8,7.3,2],[11.5,7.8,4.5],2)
display={'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[0,2,0],'scale':[.75,.75,.75]},
         'firstperson_righthand':{'rotation':[0,-90,15],'translation':[1,2,0],'scale':[.85,.85,.85]},
         'thirdperson_lefthand':{'rotation':[0,90,0],'translation':[0,2,0],'scale':[.75,.75,.75]},
         'firstperson_lefthand':{'rotation':[0,90,-15],'translation':[1,2,0],'scale':[.85,.85,.85]},
         'gui':{'rotation':[25,135,0],'translation':[0,0,0],'scale':[1,1,1]},
         'ground':{'translation':[0,3,0],'scale':[.5,.5,.5]},'fixed':{'rotation':[0,90,0]}}
model={'credit':'Hemomancy - editable source in tools/model_export/hematic_microscope.py','textures':{'0':'hemomancy:item/hematic_microscope','particle':'hemomancy:item/hematic_microscope'},'elements':elements,'display':display}
(ASSETS/'models/item/hematic_microscope.json').write_text(json.dumps(model,indent=2)+'\n')
bb_elements=[]
for e in elements:
    e=json.loads(json.dumps(e)); e['uuid']=str(uuid.uuid5(uuid.NAMESPACE_URL,'hemomancy:microscope/'+e['name'])); e['type']='cube'; e['box_uv']=False
    for f in e['faces'].values(): f['texture']=0; f['uv']=[v*2 for v in f['uv']]
    bb_elements.append(e)
bb={'meta':{'format_version':'4.10','model_format':'java_block','box_uv':False},'name':'hematic_microscope','resolution':{'width':32,'height':32},'elements':bb_elements,'outliner':[e['uuid'] for e in bb_elements],'textures':[{'path':'../../../textures/item/hematic_microscope.png','name':'hematic_microscope.png','id':'0','width':32,'height':32,'source':'data:image/png;base64,'+base64.b64encode((ASSETS/'textures/item/hematic_microscope.png').read_bytes()).decode()}],'display':display}
(ASSETS/'models/item/bbmodel/hematic_microscope.bbmodel').write_text(json.dumps(bb,indent=2)+'\n')
print('Wrote microscope model and editable Blockbench source; existing GUI textures preserved unless --reset-gui-textures was supplied')

mirrored=json.loads(json.dumps(model))
for element in mirrored['elements']:
    element['from'][0],element['to'][0]=16-element['to'][0],16-element['from'][0]
    faces=element['faces']; faces['east'],faces['west']=faces['west'],faces['east']
    for face in faces.values():
        uv=face['uv']; uv[0],uv[2]=uv[2],uv[0]
(ASSETS/'models/item/hematic_microscope_viewing_left.json').write_text(json.dumps(mirrored,indent=2)+'\n')

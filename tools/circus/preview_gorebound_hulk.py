"""Preview the actual textured Hulk rig, its walking joints and existing strike cycle."""
from pathlib import Path
import json
import shutil
import sys
import urllib.request
from build_gorebound_hulk import MODEL, ROOT
from serve_rig_preview import serve

POSE = '''function pose(view,c,move){
 for(const part of view.parts.values()){part.position.copy(part.userData.rest.position);part.rotation.copy(part.userData.rest.rotation);part.scale.set(1,1,1)}
 const p=name=>view.parts.get(name),strikeMode=state.value==='breath',time=strikeMode?c*.16:age;
 const windup=strikeMode&&c<60?c/60*10:0,strike=strikeMode&&c>=60?Math.max(.0001,(c-60)/40):0;
 if(view.key==='before'){
  p('left_leg').rotation.x=-Math.cos(time*.6662)*move*.8;p('right_leg').rotation.x=-Math.cos(time*.6662+Math.PI)*move*.8;
  const lift=windup>0?-.9:-.08;
  for(const side of ['left','right']){p(side+'_arm').rotation.x=-lift;p(side+'_fist').position.y-=lift*4}
  return;
 }
 let lift=Math.max(0,Math.min(1,windup/8));lift=lift*lift*(3-2*lift);
 const recovery=Math.min(1,strike*2.5),impact=Math.sin(strike*Math.PI);
 p('chest').rotation.x+=lift*.18-impact*.14;
 p('jaw').rotation.x-=Math.sin(time*.09)*.025+lift*.05;
 for(let i=0;i<2;i++){
  const side=i===0?'left':'right',sign=i===0?-1:1,gait=Math.cos(time*.6662+i*Math.PI)*move;
  p(side+'_thigh').rotation.x=-gait*.45;p(side+'_shin').rotation.x=-Math.max(0,-gait)*.55;
  p(side+'_foot').rotation.x=gait*.45+Math.max(0,-gait)*.55;
  const arm=p(side+'_arm'),forearm=p(side+'_forearm');
  arm.rotation.x+=lift*1.1;forearm.rotation.x+=lift*.65;
  if(windup<=0&&strike>0){arm.rotation.x=1.25*(1-recovery)+arm.rotation.x*recovery;forearm.rotation.x=.85*(1-recovery)+forearm.rotation.x*recovery}
  arm.rotation.x+=gait*.12*(1-lift);arm.rotation.z+=sign*lift*.12;p(side+'_hand').rotation.x+=lift*.12;
 }
}
'''


def main():
    output = ROOT / 'build/gorebound-hulk-rework'
    output.mkdir(parents=True, exist_ok=True)
    module = output / 'three.module.js'
    if not module.exists():
        previous = ROOT / 'build/cinder-bellows-rework/three.module.js'
        if previous.exists(): shutil.copyfile(previous, module)
        else: urllib.request.urlretrieve('https://cdn.jsdelivr.net/npm/three@0.169.0/build/three.module.js', module)
    models = {'after': json.loads(MODEL.read_text(encoding='utf-8'))}
    before = output / 'before/GoreboundHulkModel.bbmodel'
    if before.exists(): models['before'] = json.loads(before.read_text(encoding='utf-8'))
    template = Path(__file__).with_name('cinder_bellows_preview.html').read_text(encoding='utf-8')
    start, end = template.index('function pose(view, c, move)'), template.index('function frame(now)')
    template = template[:start] + POSE + template[end:]
    template = template.replace('Cinder Bellows', 'Gorebound Hulk').replace('preview_cinder_bellows.py', 'preview_gorebound_hulk.py')
    template = template.replace("'Gorebound Hulk · '+state.value", "'Gorebound Hulk · '+state.selectedOptions[0].textContent")
    template = template.replace('>Slither<', '>Walk<').replace('>Breath cycle<', '>Strike cycle<')
    template = template.replace('Slither and Breath cycle preview the procedural joint poses.', 'Walk and Strike cycle preview the knees, wrists and raised-fist wind-up.')
    template = template.replace("key==='after'?3:0,17,2", '0,15,2').replace('(-20,20,20,-20,.1,200)', '(-24,24,24,-24,.1,200)')
    template = template.replace('view.camera.left=-20*aspect;view.camera.right=20*aspect', 'view.camera.left=-24*aspect;view.camera.right=24*aspect')
    (output / 'preview.html').write_text(template.replace('/* MODEL_DATA */', json.dumps(models)), encoding='utf-8')
    print(output / 'preview.html')
    if '--serve' in sys.argv: serve(output, 'gorebound-hulk', 8768)


if __name__ == '__main__':
    main()

"""Textured before/after preview of the actual Juggler rig; --serve enables PNG saving.

Reuses the Bellows' textured hierarchy viewer. The poses mirror runtime setupPose;
Minecraft lighting, projectiles and summon lifecycle are outside this preview.
"""
from pathlib import Path
import json
import shutil
import sys
import urllib.request
from build_marrow_juggler import MODEL, ROOT
from serve_rig_preview import serve

POSE = '''function pose(view, c, move){
 for(const part of view.parts.values()){part.position.copy(part.userData.rest.position);part.rotation.copy(part.userData.rest.rotation);part.scale.set(1,1,1)}
 const p=name=>view.parts.get(name),time=state.value==='breath'?c/100*(Math.PI*2/.13):age;
 if(view.key==='before'){
  p('root').position.y-=Math.sin(time*.07)*.75;
  for(let i=0;i<6;i++){
   const arm=p('arm_'+i),hand=p('hand_'+i),z=(i%2===0?1:-1)*(.75+Math.sin(time*.22+i)*.35),x=-.5+Math.sin(time*.22+i)*.35;
   arm.rotation.z=-z;arm.rotation.x=-x;
   hand.position.set(arm.position.x-Math.sin(z)*Math.cos(x)*7,arm.position.y-Math.cos(z)*Math.cos(x)*7,arm.position.z+Math.sin(x)*7);
  }
  for(let i=0;i<3;i++){const phase=time*.14+i*Math.PI*2/3;for(const name of ['dagger_','hilt_','grip_']){const blade=p(name+i);blade.position.set(Math.sin(phase)*9,-3+Math.abs(Math.cos(phase))*8,-6);blade.rotation.z=-phase*2}}
  return;
 }
 p('root').position.y+=.75-Math.sin(time*.07)*.35;p('body').rotation.z-=Math.sin(time*.045)*.025;p('body').rotation.x-=move*.06;
 ['front_left','front_right','rear_left','rear_right','side_left','side_right','sash'].forEach((name,i)=>p(name).rotation.x-=Math.sin(time*.065+i*1.1)*.045);
 for(let i=0;i<6;i++){
  const side=i%2===0?-1:1,phase=time*.13+Math.floor(i/2)*Math.PI*2/3;
  p('arm_'+i).rotation.z+=side*Math.sin(phase)*.1;p('arm_'+i).rotation.x-=Math.cos(phase+i%2*Math.PI)*.08;
  p('forearm_'+i).rotation.x+=Math.cos(phase+i%2*Math.PI)*.12;p('hand_'+i).rotation.x-=Math.sin(phase)*.1;
 }
 view.holder.updateMatrixWorld(true);
 for(let i=0;i<3;i++){
  const phase=time*.13+i*Math.PI*2/3,t=(1-Math.cos(phase))*.5,arc=Math.sin(phase);
  const left=p('body').worldToLocal(p('hand_'+(i*2)).localToWorld(new T.Vector3(0,-.75,0)));
  const right=p('body').worldToLocal(p('hand_'+(i*2+1)).localToWorld(new T.Vector3(0,-.75,0)));
  p('knife_'+i).position.copy(left.lerp(right,t));p('knife_'+i).position.y+=arc*arc*(7+i*1.5);p('knife_'+i).rotation.z=-phase*2;
 }
}
'''


def main():
    output = ROOT / 'build/marrow-juggler-rework'
    output.mkdir(parents=True, exist_ok=True)
    module = output / 'three.module.js'
    if not module.exists():
        previous = ROOT / 'build/cinder-bellows-rework/three.module.js'
        if previous.exists(): shutil.copyfile(previous, module)
        else: urllib.request.urlretrieve('https://cdn.jsdelivr.net/npm/three@0.169.0/build/three.module.js', module)
    models = {'after': json.loads(MODEL.read_text(encoding='utf-8'))}
    before = output / 'before/MarrowSpitterModel.bbmodel'
    if before.exists(): models['before'] = json.loads(before.read_text(encoding='utf-8'))
    template = Path(__file__).with_name('cinder_bellows_preview.html').read_text(encoding='utf-8')
    start, end = template.index('function pose(view, c, move)'), template.index('function frame(now)')
    template = template[:start] + POSE + template[end:]
    template = template.replace('Cinder Bellows', 'Marrow Juggler').replace('preview_cinder_bellows.py', 'preview_marrow_juggler.py')
    template = template.replace("'Marrow Juggler · '+state.value", "'Marrow Juggler · '+state.selectedOptions[0].textContent")
    template = template.replace('>Idle<', '>Juggling<').replace('>Slither<', '>Travel<').replace('>Breath cycle<', '>Juggle phase<')
    template = template.replace('Slither and Breath cycle preview the procedural joint poses.', 'Travel and Juggle phase preview the connected arms, cloth and knife arcs.')
    template = template.replace('key===\'after\'?3:0,17,2', '0,13,2').replace('(-20,20,20,-20,.1,200)', '(-24,24,24,-24,.1,200)')
    template = template.replace('view.camera.left=-20*aspect;view.camera.right=20*aspect', 'view.camera.left=-24*aspect;view.camera.right=24*aspect')
    (output / 'preview.html').write_text(template.replace('/* MODEL_DATA */', json.dumps(models)), encoding='utf-8')
    print(output / 'preview.html')
    if '--serve' in sys.argv: serve(output, 'marrow-juggler', 8767)


if __name__ == '__main__':
    main()

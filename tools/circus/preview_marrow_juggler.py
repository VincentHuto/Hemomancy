"""Textured preview of the actual Juggler rig; --serve enables PNG saving.

Reuses the Bellows' textured hierarchy viewer. The poses mirror runtime setupPose;
Minecraft lighting, projectiles and summon lifecycle are outside this preview.
"""
from pathlib import Path
import json
import shutil
import sys
import urllib.request
from build_marrow_juggler import JOINTS, MODEL, ROOT, prepare_runtime_rig
from serve_rig_preview import serve

POSE = '''function pose(view, c, move){
 const p = name => view.parts.get(name);
 const review = state.value === 'breath';
 const time = review ? c / 100 * (Math.PI * 2 / .13) : age;
 const flick = review ? Math.sin(Math.sqrt(Math.min(1, c % 25 / 8)) * Math.PI) : 0;
 for (const part of view.parts.values()) {
  part.position.copy(part.userData.rest.position);
  part.rotation.copy(part.userData.rest.rotation);
 }
 p('Marrow Juggler').position.y -= Math.sin(time * .07) * .45;
 p('body').rotation.z -= Math.sin(time * .05) * .065 - flick * .065;
 p('body').rotation.x -= move * .13 + flick * .08;
 ['front_left','front_right','rear_left','rear_right','side_left','side_right'].forEach((name, i) => {
  p(name).rotation.x -= Math.sin(time * .085 - i * .8) * .09 - flick * .05;
  p(name).rotation.z -= Math.cos(time * .085 - i * .8) * .046;
 });
 const grip = new T.Quaternion().setFromEuler(new T.Euler(9 * deg, 0, -68 * deg, 'ZYX'));
 for (let pair = 0; pair < 3; pair++) {
  const phase = time * .13 + pair * Math.PI * 2 / 3;
  const wave = Math.sin(phase), transfer = (1 - Math.cos(phase)) / 2;
  for (let hand = 0; hand < 2; hand++) {
   const index = pair * 2 + hand, side = 2 * hand - 1, lift = wave * side;
   const release = index === 3 ? flick : 0;
   p('arm_' + index).rotation.z += side * wave * .13;
   p('arm_' + index).rotation.x -= lift * .12 - release * .37;
   p('forearm_' + index).rotation.x += lift * .21 + release * .55;
   p('hand_' + index).rotation.x -= lift * .14 - release * .28;
   p('hand_' + index).rotation.z -= Math.cos(phase) * .09;
   const close = Math.min(1, Math.max(0, ((hand ? transfer : 1 - transfer) - .76) / .24)) * (1 - release);
   for (const digit of ['index','middle','ring']) {
    const finger = p(digit + '_' + index);
    finger.rotation.x = T.MathUtils.lerp(Math.min(finger.rotation.x, .25), .82, close);
   }
  }
  const left = p('hand_' + pair * 2), right = p('hand_' + (pair * 2 + 1));
  const props = p('props'), knife = p('knife_' + pair);
  view.holder.updateMatrixWorld(true);
  const palm = hand => props.worldToLocal(hand.localToWorld(new T.Vector3(0, -2, -1.25)));
  knife.position.copy(palm(left).lerp(palm(right), transfer));
  knife.position.y += wave * wave * (8 + pair * 1.5);
  knife.position.z -= wave * wave * (6 + pair * 2);
  const orientation = hand => hand.getWorldQuaternion(new T.Quaternion()).multiply(grip);
  const spin = new T.Quaternion().setFromAxisAngle(new T.Vector3(0,0,1), -phase * 2);
  knife.quaternion.copy(props.getWorldQuaternion(new T.Quaternion()).invert())
   .multiply(orientation(left).slerp(orientation(right), transfer)).multiply(spin);
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
    prepare_runtime_rig(models['after'])

    def name_joints(group):
        group['name'] = JOINTS.get(group['name'], group['name'])
        for child in group['children']:
            if isinstance(child, dict):
                name_joints(child)

    name_joints(models['after']['outliner'][0])
    template = Path(__file__).with_name('cinder_bellows_preview.html').read_text(encoding='utf-8')
    template = template.replace('u/128,1-v/128,s/128,1-v/128,s/128,1-t/128,u/128,1-t/128',
                                'u/bb.resolution.width,1-v/bb.resolution.height,s/bb.resolution.width,1-v/bb.resolution.height,s/bb.resolution.width,1-t/bb.resolution.height,u/bb.resolution.width,1-t/bb.resolution.height')
    start, end = template.index('function pose(view, c, move)'), template.index('function frame(now)')
    template = template[:start] + POSE + template[end:]
    template = template.replace('Cinder Bellows', 'Marrow Juggler').replace('preview_cinder_bellows.py', 'preview_marrow_juggler.py')
    template = template.replace("'Marrow Juggler · '+state.value", "'Marrow Juggler · '+state.selectedOptions[0].textContent")
    template = template.replace('>Idle<', '>Juggling<').replace('>Slither<', '>Travel<').replace('>Breath cycle<', '>Juggle phase<')
    template = template.replace('Slither and Breath cycle preview the procedural joint poses.', 'Travel and Juggle phase preview the connected arms, cloth and knife arcs.')
    template = template.replace('key===\'after\'?3:0,17,2', '0,20,1').replace('(-20,20,20,-20,.1,200)', '(-24,24,24,-24,.1,200)')
    template = template.replace('view.camera.left=-20*aspect;view.camera.right=20*aspect', 'view.camera.left=-24*aspect;view.camera.right=24*aspect')
    (output / 'preview.html').write_text(template.replace('/* MODEL_DATA */', json.dumps(models)), encoding='utf-8')
    print(output / 'preview.html')
    if '--serve' in sys.argv: serve(output, 'marrow-juggler', 8767)


if __name__ == '__main__':
    main()

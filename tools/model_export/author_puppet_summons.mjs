// Rebuild the seven authored puppet meshes and UV-fitted atlases. Requires pngjs.
// NODE_PATH may point at the bundled Codex node_modules; no runtime mod dependency.
import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import assert from 'node:assert/strict';
const { PNG } = createRequire(import.meta.url)('pngjs');
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const javaDir = 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon';
const assetDir = 'src/main/resources/assets/hemomancy';
const palette = ['151219','30232c','523039','79333e','a44549','cf6159','694d44','997957','bca47b','e0cda3','48414a','716777']
  .map(hex => [0,2,4].map(i => parseInt(hex.slice(i,i+2),16)));
const materialPath = path.join(root, 'tools/model_export/puppet_materials.png');
const source = PNG.sync.read(fs.readFileSync(materialPath));
const models = [];
const cube = (material, x,y,z,w,h,d) => ({material, pos:[x,y,z], size:[w,h,d]});
// Material numbers follow the 4x3 ImageGen source sheet, left-to-right, top-to-bottom.
const flesh=0, bone=1, iron=2, leather=3, membrane=4, thread=5, mask=6, wood=7, cloth=8, gold=9, cavity=10, tendon=11;
function model(name, id, width, height) {
  const m = {name, id, width, height, parts:[]}; models.push(m); return m;
}
function part(m, name, parent, offset, boxes=[], rotation=[0,0,0]) {
  const p = {name,parent,offset,boxes,rotation}; m.parts.push(p); return p;
}

const v = model('VeinwingVultureModel','veinwing_vulture',128,128);
part(v,'root','mesh',[0,15,0],[cube(flesh,-2,-5,-1.5,4,8,3),cube(bone,-3,-5.5,-1,6,2,2),cube(thread,-.5,-7,-.5,1,3,1)]);
part(v,'breast_keel','root',[0,-3,-1.6],[cube(bone,-.5,0,-.5,1,6,1)]);
for (let i=0;i<3;i++) part(v,`rib_${i}`,'root',[0,-3+i*2,-1.7],[cube(bone,-2,0,-.5,4,1,1)]);
part(v,'head','root',[0,-6,-1],[cube(flesh,-2,-2,-3,4,3,3),cube(bone,-1.5,-2.5,-4,3,2,2),cube(bone,-1,-.5,-8,2,1,5),cube(iron,-2,-1.2,-3.5,1,1,1),cube(iron,1,-1.2,-3.5,1,1,1)]);
part(v,'beak_hook','head',[0,0,-7.5],[cube(bone,-.5,0,-1,1,2,1)],[0.28,0,0]);
for (const s of [-1,1]) {
  const side=s===1?'left':'right';
  part(v,`${side}_wing`,'root',[s*2,-4,0],[cube(iron,-1,-1,-1,2,2,2),cube(bone,s===1?0:-6,-.5,-.5,6,1,1)]);
  part(v,`${side}_forewing`,`${side}_wing`,[s*6,0,0],[cube(bone,s===1?0:-9,-.5,-.5,9,1,1)],[0,s*-.1,s*-.3]);
  for(let i=0;i<3;i++) {
    part(v,`${side}_membrane_${i}`,`${side}_forewing`,[s*(i*3+.2),0,0],
      [cube(membrane,s===1?0:-3,.5,0,3,3+i*2,1),cube(bone,s===1?2:-3,0,-.2,1,5+i*2,1)],[0,0,s*-.09]);
  }
  part(v,`${side}_talon`,'root',[s*1.2,3,0],[cube(thread,-.5,0,-.5,1,3,1),cube(bone,-1,2.5,-3,1,1,3),cube(bone,.5,2.5,-3,1,1,3),cube(bone,-.5,2.5,0,1,1,2)]);
}
part(v,'tail','root',[0,3,1],[cube(thread,-.5,0,-.5,1,4,1)]);
part(v,'tail_cord','tail',[0,3.5,0],[cube(thread,-.5,0,-.5,1,4,1)],[.2,0,0]);
part(v,'tail_needle','tail_cord',[0,3.5,0],[cube(bone,-.5,0,-.5,1,2,1)],[.3,0,0]);

const sp = model('MarrowSpitterModel','marrow_spitter',128,128);
part(sp,'root','mesh',[0,16,0],[cube(flesh,-3,-10,-2,6,10,5),cube(bone,-2.5,-11,-1.5,5,1,4),cube(iron,-4,-1,-2.5,8,2,6)]);
for(const s of [-1,1]) {
  part(sp,`${s===1?'left':'right'}_carriage`,'root',[s*3,-9,0],[cube(bone,-.5,0,-1,1,9,2)],[0,0,s*.08]);
  for(let i=0;i<3;i++) part(sp,`brace_${s===1?'l':'r'}_${i}`,'root',[s*2,-8+i*3,-2],[cube(bone,-1.5,0,-1,3,1,1)],[0,s*-.15,s*.12]);
}
part(sp,'nozzle','root',[0,-5,-2.5],[cube(tendon,-2,-2,-2,4,4,3),cube(bone,-1.5,-1.5,-8,3,3,6),cube(iron,-2,-2,-5,4,4,1),cube(cavity,-1,-1,-8.1,2,2,1)]);
part(sp,'upper_muzzle','nozzle',[0,-1.5,-7.5],[cube(bone,-1.5,-.5,-3,3,1,3)],[.1,0,0]);
part(sp,'lower_muzzle','nozzle',[0,1.5,-7.5],[cube(bone,-1.5,-.5,-3,3,1,3)],[-.1,0,0]);
for (const [name,x,z,roll,pitch] of [['left_leg',3,-1,-.27,.06],['right_leg',-3,-1,.27,.06],['rear_leg',0,2,0,.35]]) {
  part(sp,name,'root',[x,0,z],[cube(iron,-1,-1,-1,2,2,2),cube(bone,-.5,0,-.5,1,4,1)],[pitch,0,roll]);
  part(sp,`${name}_stilt`,name,[0,3.5,0],[cube(iron,-1,-.5,-1,2,1,2),cube(bone,-.5,0,-.5,1,4,1),cube(bone,-1,3.5,-2,2,1,3)],[-pitch,0,-roll*.5]);
}
part(sp,'tubing','root',[0,-8,2.5],[cube(thread,-4,0,0,8,1,1),cube(thread,-4,0,0,1,6,1),cube(thread,3,0,0,1,6,1),cube(thread,-4,5,0,8,1,1)]);
part(sp,'feed_hose','root',[3,-4,-2],[cube(thread,0,0,-4,1,1,4),cube(iron,-.5,-.5,-3,2,2,1)]);

const h = model('GoreboundHulkModel','gorebound_hulk',128,128);
part(h,'root','mesh',[0,18,0],[cube(flesh,-6,-12,-3.5,12,10,7),cube(flesh,-4,-3,-3,8,3,6)]);
part(h,'yoke','root',[0,-12,0],[cube(bone,-7,-1,-3,14,2,6),cube(iron,-1,-1.5,-3.5,2,3,7)]);
for(const s of [-1,1]) part(h,`${s===1?'left':'right'}_chest_strap`,'root',[s*3,-11,-3.7],[cube(leather,-.5,0,0,1,10,1)],[0,0,s*-.17]);
part(h,'waist_binding','root',[0,-3,-3.6],[cube(leather,-6,0,0,12,2,1),cube(iron,-1,-.5,-.3,2,3,1)]);
part(h,'head','root',[0,-10,-3.5],[cube(flesh,-2,-2,-3,4,4,3),cube(mask,-1.5,-1.5,-3.5,3,3,1),cube(iron,-2,-2,-3.5,4,1,1)]);
for(const s of [-1,1]) {
  const side=s===1?'left':'right';
  part(h,`${side}_arm`,'root',[s*6,-10,0],[cube(flesh,s===1?0:-5,-1,-2.5,5,7,5),cube(leather,s===1?-.5:-5.5,3,-3,6,1,6)],[0,0,s*-.06]);
  part(h,`${side}_forearm`,`${side}_arm`,[s*1,5.5,0],[cube(iron,s===1?-.5:-4.5,0,-2.5,5,2,5),cube(tendon,s===1?0:-4,1,-2.5,4,6,5),cube(flesh,s===1?-1:-6,5,-3.5,7,4,7)],[.08,0,s*.08]);
  for(let i=0;i<3;i++) part(h,`${side}_knuckle_${i}`,`${side}_forearm`,[s*(i*2+.5),7,-3.5],[cube(bone,-.5,0,-.5,1,2,1)]);
  part(h,`${side}_leg`,'root',[s*3,-1,0],[cube(flesh,-2,0,-2,4,5,4),cube(leather,-2.5,1,-2.5,5,1,5),cube(iron,-2.5,4,-3.5,5,3,6)]);
}
part(h,'back_mass','root',[0,-10,3],[cube(flesh,-5,0,0,10,8,5),cube(tendon,-4,-3,0,8,4,4),cube(leather,-5.5,1,4.4,11,1,1),cube(leather,-5.5,5,4.4,11,1,1),cube(bone,-.5,-2,4.4,1,9,1)]);

const mn = model('MnemonistPuppetModel','mnemonist_puppet',64,64);
part(mn,'head','mesh',[0,0,0],[cube(wood,-2.5,-7,-2,5,7,4),cube(mask,-2,-6.5,-2.5,4,6,1),cube(iron,-2.5,-7,-2.5,5,1,1)]);
part(mn,'hat','mesh',[0,0,0]);
part(mn,'body','mesh',[0,0,0],[cube(wood,-2,0,-1.5,4,10,3),cube(iron,-2.5,10,-1.5,5,2,3),cube(bone,-3,0,-1.5,6,1,3)]);
for(let i=0;i<3;i++) part(mn,`binding_${i}`,'body',[0,2+i*3,-1.6],[cube(leather,-2.5,0,-.4,5,1,1)]);
part(mn,'memory_spool','body',[0,4,3],[cube(thread,-4,-2,-1.5,8,4,3),cube(bone,-5,-3,-2,1,6,4),cube(bone,4,-3,-2,1,6,4),cube(iron,-6,-.5,-.5,12,1,1)]);
for(const s of [-1,1]) {
  const side=s===1?'left':'right';
  part(mn,`${side}_thread`,'body',[s*3,-3,-2],[cube(thread,-.5,0,0,1,14,1)]);
  part(mn,`${side}_arm`,'mesh',[s*4,2,0],[cube(wood,-1,-2,-1,2,6,2),cube(iron,-1.5,3,-1.5,3,1,3)]);
  part(mn,`${side}_forearm`,`${side}_arm`,[0,4,0],[cube(wood,-1,0,-1,2,5,2),cube(leather,-1.5,3,-1.5,3,1,3),cube(bone,-1,5,-1,2,2,2)],[0,0,s*-.06]);
  part(mn,`${side}_leg`,'mesh',[s*1.5,12,0],[cube(wood,-1,0,-1,2,6,2),cube(iron,-1.5,5,-1.5,3,1,3)]);
  part(mn,`${side}_shin`,`${side}_leg`,[0,6,0],[cube(wood,-1,0,-1,2,5,2),cube(iron,-1.5,5,-2,3,1,3)]);
}

const mu=model('ScarletMummerModel','scarlet_mummer',64,64);
part(mu,'head','mesh',[0,0,0],[cube(wood,-2,-7,-1.5,4,7,3),cube(mask,-1.5,-6.5,-2,3,6,1)]);
part(mu,'hat','mesh',[0,0,0]);
part(mu,'body','mesh',[0,0,0],[cube(flesh,-1.5,0,-1,3,10,2),cube(iron,-2,10,-1.5,4,2,3),cube(gold,-.5,1,-1.5,1,8,1)]);
for(const s of [-1,1]) {
  const side=s===1?'left':'right';
  part(mu,`${side}_gorget`,'body',[s*1.3,0,1],[cube(iron,-.5,-2,-.5,1,4,1)],[0,s*-.55,s*-.08]);
  // Stepped panels form a tapered cobra hood, with a clear hinge at the shoulder.
  part(mu,`${side}_upper_fan`,`${side}_gorget`,[0,-1,0],[cube(membrane,s===1?0:-4,-6,0,4,10,1),cube(gold,s===1?3:-4,-5,-.2,1,8,1)],[0,0,s*.12]);
  part(mu,`${side}_outer_fan`,`${side}_upper_fan`,[s*4,-2,0],[cube(membrane,s===1?0:-3,-2,0,3,7,1),cube(gold,s===1?2:-3,-1,-.2,1,5,1)],[0,0,s*-.18]);
  part(mu,`${side}_lower_fan`,`${side}_gorget`,[0,3,0],[cube(membrane,s===1?0:-3,0,0,3,5,1)],[0,0,s*.4]);
  part(mu,`${side}_arm`,'mesh',[s*2.8,2,0],[cube(flesh,-.5,-2,-.5,1,7,1),cube(iron,-1,4,-1,2,1,2)]);
  part(mu,`${side}_forearm`,`${side}_arm`,[0,5,0],[cube(flesh,-.5,0,-.5,1,6,1),cube(gold,-1,4.5,-1,2,1,2),cube(bone,-.5,6,-.5,1,3,1)],[0,0,s*-.1]);
  part(mu,`${side}_leg`,'mesh',[s*1.2,12,0],[cube(flesh,-.5,0,-.5,1,6,1),cube(iron,-1,5,-1,2,1,2)]);
  part(mu,`${side}_shin`,`${side}_leg`,[0,6,0],[cube(flesh,-.5,0,-.5,1,5,1),cube(iron,-1,5,-2,2,1,3)]);
}

const dog=model('SanguineHoundModel','sanguine_hound',64,32);
part(dog,'root','mesh',[0,0,0]);
part(dog,'body','root',[0,15,0],[cube(flesh,-3,-3,-5,6,6,10),cube(tendon,-3,-3.5,-5.5,6,6,4),cube(bone,-.5,-4,-4,1,1,8)]);
part(dog,'head','root',[0,14,-5],[cube(flesh,-2,-2,-3,4,4,4),cube(flesh,-1.5,0,-6,3,2,3),cube(bone,-2,-.5,-6,4,1,4),cube(iron,-1.5,.5,-6.2,3,1,1),cube(leather,-2.5,-2,-2,1,4,1),cube(leather,1.5,-2,-2,1,4,1)]);
for(const s of [-1,1]) {
  const side=s===1?'left':'right';
  part(dog,`${side}_ear`,'head',[s*1.5,-2,0],[cube(flesh,-.5,-2,-.5,1,2,1)],[.2,0,s*.2]);
  part(dog,`${side}_fang`,'head',[s*1,1,-5.5],[cube(bone,-.5,0,-.5,1,1,1)]);
  for(const [end,z] of [['front',-3.5],['hind',3.5]]) {
    part(dog,`${end}_${side}_leg`,'root',[s*2,17,z],[cube(tendon,-1,-1,-1,2,4,2)]);
    part(dog,`${end}_${side}_shin`,`${end}_${side}_leg`,[0,3,0],[cube(iron,-1,-.5,-1,2,1,2),cube(flesh,-.5,0,-.5,1,3,1),cube(bone,-1,3,-2,2,1,3)]);
  }
}
part(dog,'tail','root',[0,13,4.5],[cube(thread,-.5,-.5,0,1,1,5)],[-.4,0,0]);
part(dog,'tail_tip','tail',[0,0,4.5],[cube(thread,-.5,-.5,0,1,1,3)],[-.25,0,0]);

const ring=model('RingmasterPatternModel','ringmaster_pattern',128,128);
part(ring,'head','mesh',[0,0,0],[cube(wood,-2.5,-7,-2,5,7,4),cube(mask,-2,-6.5,-2.5,4,6,1)]);
part(ring,'hat','mesh',[0,0,0]);
part(ring,'top_hat','head',[0,-7,0],[cube(iron,-4,-1,-3.5,8,1,7),cube(cloth,-2.5,-5,-2,5,4,4),cube(gold,-3,-2,-2.5,6,1,5)],[0,0,-.07]);
part(ring,'body','mesh',[0,0,0],[cube(cloth,-3,0,-1.5,6,10,3),cube(gold,-2,1,-2,4,7,1),cube(iron,-3,9,-2,6,2,4)]);
for(const s of [-1,1]) {
  const side=s===1?'left':'right';
  part(ring,`${side}_lapel`,'body',[s*.5,0,-2],[cube(cloth,s===1?0:-2,0,0,2,7,1)],[0,0,s*-.2]);
  part(ring,`${side}_coattail`,'body',[s*.3,10,1],[cube(cloth,s===1?0:-3,0,0,3,7,1),cube(gold,s===1?2:-3,0,-.1,1,7,1)],[.15,0,s*-.1]);
  part(ring,`${side}_arm`,'mesh',[s*4,2,0],[cube(cloth,-1,-2,-1,2,6,2),cube(gold,-1.5,-2,-1.5,3,1,3),cube(iron,-1.5,3,-1.5,3,1,3)]);
  part(ring,`${side}_forearm`,`${side}_arm`,[0,4,0],[cube(wood,-1,0,-1,2,5,2),cube(gold,-1.5,3.5,-1.5,3,1,3),cube(bone,-1,5,-1,2,2,2)]);
  part(ring,`${side}_leg`,'mesh',[s*1.5,12,0],[cube(wood,-1,0,-1,2,6,2),cube(iron,-1.5,5,-1.5,3,1,3)]);
  part(ring,`${side}_shin`,`${side}_leg`,[0,6,0],[cube(wood,-1,0,-1,2,5,2),cube(iron,-1.5,5,-2,3,1,3)]);
  part(ring,`${side}_command_thread`,'body',[s*3,-1,2],[cube(thread,-.5,0,0,1,12,1)]);
}

function pack(m) {
  const unique = new Map();
  for(const p of m.parts) for(const b of p.boxes) {
    b.key=JSON.stringify([b.material,...b.size]);
    if(!unique.has(b.key)) unique.set(b.key,b);
  }
  const occupied = Array.from({length:m.height},()=>new Uint8Array(m.width));
  // Native box UVs; identical material/size boxes deliberately share the same pixels.
  for(const b of [...unique.values()].sort((a,b)=>(b.size[1]+b.size[2])-(a.size[1]+a.size[2]))) {
    const [w,h,d]=b.size, rw=2*(w+d), rh=h+d;
    let fit;
    for(let y=0;y<=m.height-rh&&!fit;y++) for(let x=0;x<=m.width-rw&&!fit;x++) {
      if(occupied.slice(y,y+rh).every(row=>row.slice(x,x+rw).every(v=>v===0))) fit=[x,y];
    }
    assert(fit,`${m.name}: cannot pack ${b.key}`); b.uv=fit;
    for(let y=fit[1];y<fit[1]+rh;y++) occupied[y].fill(1,fit[0],fit[0]+rw);
  }
  for(const p of m.parts) for(const b of p.boxes) b.uv=unique.get(b.key).uv;
}
const f=n=>`${Number(n.toFixed(4))}F`;
const variable=name=>name.replace(/_([a-z])/g,(_,letter)=>letter.toUpperCase());
function geometry(m) {
  let text='\tpublic static LayerDefinition createBodyLayer() {\n\t\tMeshDefinition mesh = new MeshDefinition();\n\t\tPartDefinition part = mesh.getRoot();\n';
  for(const p of m.parts) {
    const parent=p.parent==='mesh'?'part':variable(p.parent);
    assert(p.parent==='mesh'||m.parts.some(q=>q.name===p.parent),p.parent);
    text+=`\n\t\tPartDefinition ${variable(p.name)} = ${parent}.addOrReplaceChild("${p.name}", CubeListBuilder.create()`;
    for(const b of p.boxes) text+=`\n\t\t\t\t.texOffs(${b.uv.join(', ')}).addBox(${[...b.pos,...b.size].map(f).join(', ')})`;
    const rot=p.rotation.length===3?p.rotation:[0,0,0];
    text+=`,\n\t\t\t\tPartPose.offsetAndRotation(${[...p.offset,...rot].map(f).join(', ')}));\n`;
  }
  return text+`\n\t\treturn LayerDefinition.create(mesh, ${m.width}, ${m.height});\n\t}\n`;
}
const materialColors = [
  [1,2,3,4], [6,7,8,9], [0,10,11], [2,6,7,8], [2,3,4,5], [2,3,4,5],
  [8,9], [0,1,2,3], [1,2,10], [1,6,7,8,9], [0,2,3], [2,3,4,5,7,8]
];
function nearest(rgb,material) {
  return materialColors[material].map(i=>palette[i]).reduce((best,c)=>c.reduce((s,v,i)=>s+(v-rgb[i])**2,0)<best.d ? {c,d:c.reduce((s,v,i)=>s+(v-rgb[i])**2,0)}:best,{c:palette[0],d:Infinity}).c;
}
// Translate each generated material onto its native 16px grid before fitting UV faces.
const tiles=Array.from({length:12},(_,n)=>Array.from({length:256},(_,p)=>{
  const x=Math.floor(((n%4)+(p%16+.5)/16)*source.width/4);
  const y=Math.floor((Math.floor(n/4)+(Math.floor(p/16)+.5)/16)*source.height/3);
  return nearest([...source.data.subarray((y*source.width+x)*4,(y*source.width+x)*4+3)],n);
}));
function paint(m, bb) {
  const png=new PNG({width:m.width,height:m.height}); png.data.fill(0);
  const boxes=m.parts.flatMap(p=>p.boxes); assert.equal(boxes.length,bb.elements.length);
  for(const [i,e] of bb.elements.entries()) for(const [faceName,face] of Object.entries(e.faces)) {
    const b=boxes[i], [u,v,u2,v2]=face.uv;
    const l=Math.floor(Math.min(u,u2)),t=Math.floor(Math.min(v,v2)),r=Math.ceil(Math.max(u,u2)),bot=Math.ceil(Math.max(v,v2));
    assert(l>=0&&t>=0&&r<=m.width&&bot<=m.height,`${m.name}: ${faceName}`);
    for(let y=t;y<bot;y++) for(let x=l;x<r;x++) {
      const tx=(x-l)%16,ty=(y-t)%16;
      let color=tiles[b.material][ty*16+tx];
      // Flat mask faces stay quiet; small iron joints remain readable at one pixel wide.
      if(b.material===mask) color=palette[(x-l===Math.floor((r-l)*.65)&&y-t>1)?8:9];
      if(b.material===iron && (x===l||y===t)) color=palette[10];
      if(b.material===thread) color=palette[y%3===0?5:3];
      if(b.material===bone && (x===l||y===t)) color=palette[9];
      if(b.material===leather && y-t===Math.floor((bot-t)/2)) color=palette[(x-l)%3===0?8:2];
      if(b.material===cavity && faceName==='north') color=palette[0];
      png.data.set([...color,255],(y*m.width+x)*4);
    }
  }
  return PNG.sync.write(png);
}

for(const m of models) {
  pack(m);
  const java=path.join(root,javaDir,`${m.name}.java`);
  const text=fs.readFileSync(java,'utf8');
  const start=text.indexOf('\tpublic static LayerDefinition createBodyLayer()');
  const end=text.indexOf('\n\t@Override',start);
  assert(start>=0&&end>start,java);
  fs.writeFileSync(java,text.slice(0,start)+geometry(m)+text.slice(end));
  const output=`${assetDir}/models/entity/bbmodel/${m.name}.bbmodel`;
  const texture=`textures/entity/puppeteer_summon/${m.id}.png`;
  execFileSync(process.execPath,['tools/model_export/java_model_to_bbmodel.mjs','--source',`${javaDir}/${m.name}.java`,'--texture',texture,'--output',output],{cwd:root,stdio:'pipe'});
  const bb=JSON.parse(fs.readFileSync(path.join(root,output)));
  const png=paint(m,bb);
  fs.writeFileSync(path.join(root,assetDir,texture),png);
  if(m===dog) {
    const cur=PNG.sync.read(png);
    for(let i=0;i<cur.data.length;i+=4) {
      if(cur.data[i+3]===0) continue;
      const index=palette.findIndex(c=>c.every((v,k)=>v===cur.data[i+k]));
      if(index>=2&&index<=4) cur.data.set(palette[index+1],i);
    }
    fs.writeFileSync(path.join(root,assetDir,'textures/entity/puppeteer_summon/sanguine_hound_cur.png'),PNG.sync.write(cur));
  }
  if(m===ring) {
    const glow=PNG.sync.read(png), boxes=m.parts.flatMap(p=>p.boxes);
    const glowing=new Set();
    for(const [i,e] of bb.elements.entries()) if(boxes[i].material===thread) {
      for(const {uv} of Object.values(e.faces)) for(let y=Math.min(uv[1],uv[3]);y<Math.max(uv[1],uv[3]);y++) {
        for(let x=Math.min(uv[0],uv[2]);x<Math.max(uv[0],uv[2]);x++) glowing.add(y*m.width+x);
      }
    }
    for(let i=0;i<glow.data.length;i+=4) if(!glowing.has(i/4)) glow.data[i+3]=0;
    fs.writeFileSync(path.join(root,assetDir,'textures/entity/puppeteer_summon/ringmaster_pattern_glow.png'),PNG.sync.write(glow));
  }
  bb.textures[0].source=`data:image/png;base64,${png.toString('base64')}`;
  fs.writeFileSync(path.join(root,output),JSON.stringify(bb,null,2)+'\n');
  console.log(`${m.name}: ${bb.elements.length} cubes, ${m.width}x${m.height}, embedded PNG`);
}

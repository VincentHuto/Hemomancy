import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_DIR = path.join(ROOT,
  "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses");
const RAD_TO_DEG = 180 / Math.PI;

function uuid(seed) {
  const hex = crypto.createHash("sha256").update(seed).digest("hex").slice(0, 32);
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function round(value) {
  const result = Math.round(value * 100000) / 100000;
  return Object.is(result, -0) ? 0 : result;
}

function readModel(file) {
  const model = JSON.parse(fs.readFileSync(path.join(MODEL_DIR, file), "utf8"));
  model.animations ??= [];
  return model;
}

function keyframe(seed, channel, time, vector) {
  return {
    channel,
    data_points: [{ x: round(vector[0]), y: round(vector[1]), z: round(vector[2]) }],
    uuid: uuid(`${seed}:${channel}:${time}`),
    time: round(time),
    color: -1,
    interpolation: "catmullrom",
  };
}

function makeAnimation(model, modelName, name, length, loop, tracks) {
  const groups = new Map(model.groups.map(group => [group.name, group.uuid]));
  const fullName = `animation.${modelName}.${name}`;
  const animators = {};
  for (const [bone, channels] of Object.entries(tracks)) {
    if (bone === "body") continue;
    const boneUuid = groups.get(bone);
    if (!boneUuid) throw new Error(`${fullName} targets missing bone ${bone}`);
    const keys = [];
    for (const [channel, samples] of Object.entries(channels)) {
      for (const [time, vector] of samples) {
        keys.push(keyframe(`${fullName}:${bone}`, channel, time, vector));
      }
    }
    keys.sort((a, b) => a.time - b.time || a.channel.localeCompare(b.channel));
    animators[boneUuid] = { name: bone, type: "bone", keyframes: keys };
  }
  return {
    uuid: uuid(fullName), name: fullName, loop, override: false, length: round(length),
    snapping: 24, selected: false, anim_time_update: "", blend_weight: "",
    start_delay: "", loop_delay: "", animators,
  };
}

function install(model, modelName, animations) {
  const prefix = `animation.${modelName}.`;
  const generatedNames = new Set(animations.map(animation => animation.name));
  const hasKeyframes = animation => Object.values(animation.animators ?? {})
    .some(animator => animator.keyframes?.length);
  const preserved = model.animations.filter(animation =>
    !animation.name.startsWith(prefix)
    || hasKeyframes(animation)
    || !generatedNames.has(animation.name));
  const preservedNames = new Set(preserved.map(animation => animation.name));
  model.animations = preserved.concat(animations.filter(animation => !preservedNames.has(animation.name)));
}

function writeModel(file, model) {
  fs.writeFileSync(path.join(MODEL_DIR, file), `${JSON.stringify(model, null, 2)}\n`);
}

function rotationSamples(samples) {
  const bones = new Set(samples.flatMap(sample => Object.keys(sample.pose)));
  const tracks = {};
  for (const bone of bones) {
    tracks[bone] = {
      rotation: samples.map(sample => [sample.tick / 20,
        (() => {
          const [x, y, z] = sample.pose[bone] ?? [0, 0, 0];
          return [-x * RAD_TO_DEG, y * RAD_TO_DEG, z * RAD_TO_DEG];
        })()]),
    };
  }
  return tracks;
}

function poseSample(tick, pose) {
  return { tick, pose };
}

function set(pose, bone, x = 0, y = 0, z = 0) {
  pose[bone] = [x, y, z];
}

function assign(pose, bone, index, value) {
  pose[bone] ??= [0, 0, 0];
  pose[bone][index] = value;
}

function smootherstep(value) {
  const t = Math.max(0, Math.min(1, value));
  return t * t * t * (t * (t * 6 - 15) + 10);
}

function actionBlend(action, tick) {
  return Math.min(smootherstep(tick / 6), smootherstep((action.duration - tick) / 7));
}

function swingArc(action, tick) {
  const contact = action.contacts[0];
  const loaded = Math.max(1, contact - 3);
  const follow = Math.min(action.duration, contact + 1);
  if (tick <= 0 || tick >= action.duration) return 0;
  if (tick <= loaded) return -smootherstep(tick / loaded);
  if (tick <= follow) return -1 + 2 * smootherstep((tick - loaded) / Math.max(1, follow - loaded));
  return 1 - smootherstep((tick - follow) / Math.max(1, action.duration - follow));
}

function contactMotion(action, tick) {
  const contacts = action.contacts;
  const first = contacts[0];
  if (tick <= first) {
    const start = Math.max(0, first - 6);
    const loaded = Math.max(start + 1, first - 3);
    if (tick <= start) return 0;
    if (tick <= loaded) return -0.32 * smootherstep((tick - start) / Math.max(1, loaded - start));
    return -0.32 + 1.32 * smootherstep((tick - loaded) / Math.max(1, first - loaded));
  }
  for (let index = 0; index < contacts.length - 1; index++) {
    if (tick <= contacts[index + 1]) {
      const from = index % 2 === 0 ? 1 : -1;
      return from + (-2 * from) * smootherstep(
        (tick - contacts[index]) / Math.max(1, contacts[index + 1] - contacts[index]));
    }
  }
  const lastIndex = contacts.length - 1;
  const sign = lastIndex % 2 === 0 ? 1 : -1;
  return sign * (1 - smootherstep(
    (tick - contacts[lastIndex]) / Math.max(1, action.duration - contacts[lastIndex])));
}

function curve(tick, keys) {
  if (tick <= keys[0][0]) return keys[0][1];
  for (let index = 1; index < keys.length; index++) {
    if (tick <= keys[index][0]) {
      const [fromTick, fromValue] = keys[index - 1];
      const [toTick, toValue] = keys[index];
      return fromValue + (toValue - fromValue) * smootherstep((tick - fromTick) / (toTick - fromTick));
    }
  }
  return keys.at(-1)[1];
}

const ACTIONS = {
  ichimonji: { tendency: "animus", duration: 30, contacts: [18] },
  crosscut: { tendency: "animus", duration: 38, contacts: [15, 26] },
  leaping_cleave: { tendency: "mortem", duration: 34, contacts: [22] },
  reaper_sweep: { tendency: "mortem", duration: 30, contacts: [18] },
  sky_lance: { tendency: "lux", duration: 37, contacts: [17] },
  lance_flurry: { tendency: "lux", duration: 40, contacts: [12, 20, 28] },
  twin_rend: { tendency: "tenebris", duration: 34, contacts: [12, 20] },
  predator_pounce: { tendency: "tenebris", duration: 32, contacts: [20] },
  conductive_volley: { tendency: "ductilis", duration: 32, contacts: [20] },
  storm_lock: { tendency: "ductilis", duration: 52, contacts: [28, 36, 44] },
  branding_thrusts: { tendency: "flammeus", duration: 38, contacts: [12, 19, 26] },
  updraft_impalement: { tendency: "flammeus", duration: 36, contacts: [16, 24] },
  chain_sweep: { tendency: "congeatio", duration: 30, contacts: [18] },
  hook_and_crush: { tendency: "congeatio", duration: 40, contacts: [14, 28] },
  magnetic_axis: { tendency: "ferric", duration: 30, contacts: [18] },
  iron_retort: { tendency: "ferric", duration: 46, contacts: [16] },
  sickle_cyclone: { tendency: "rage", duration: 34, contacts: [12, 18, 24] },
  sickle_pounce: { tendency: "rage", duration: 24, contacts: [14] },
  sickle_cross_rend: { tendency: "rage", duration: 28, contacts: [10, 14, 18] },
  sickle_hook: { tendency: "rage", duration: 28, contacts: [14] },
  sanguine_crescents: { tendency: "rage", duration: 32, contacts: [12, 17, 22] },
};

const ALTERNATING = new Set([
  "ichimonji", "crosscut", "leaping_cleave", "reaper_sweep", "sky_lance", "lance_flurry",
  "twin_rend", "predator_pounce", "branding_thrusts", "updraft_impalement",
  "chain_sweep", "hook_and_crush", "sickle_cyclone", "sickle_cross_rend", "sanguine_crescents",
]);

const GRIPS = {
  animus: [-0.3840, -0.2555, 1.2013, -0.4760, 0.0605, 0.6636],
  mortem: [-0.4053, -0.1370, 1.3111, -0.6113, 0.2848, 0.9744],
};

const ACTION_GRIPS = {
  ichimonji: [
    [-0.4101, 0.2573, 2.1228, -0.1665, -0.0509, 0.7013],
    [-0.7057, 0.0813, 1.9849, -0.2312, -0.1239, 0.3728],
    [-0.8480, -0.1614, 1.7449, -0.2750, -0.1745, 0.1648],
    [-0.8195, -0.5072, 1.6567, -0.4752, -0.3165, -0.1058],
    [-0.7323, -0.8924, 1.5882, -0.6309, -0.4723, -0.3922],
  ],
  crosscut: [
    [-0.4373, -0.2679, 1.2184, -0.5963, 0.1482, 0.7487],
    [-0.4710, -0.1571, 1.3451, -0.5253, 0.1316, 0.7041],
    [-0.4912, -0.0822, 1.4290, -0.5412, 0.1727, 0.7829],
    [-0.5040, -0.0973, 1.4258, -0.6250, 0.2430, 0.8968],
    [-0.5117, -0.1344, 1.4017, -0.7072, 0.3011, 0.9921],
  ],
  leaping_cleave: [
    [-0.2018, 0.0610, 2.2429, -0.0681, -0.0910, 0.6300],
    [-0.5428, -0.0292, 2.1796, -0.2281, -0.1161, 0.5507],
    [-0.7856, -0.1554, 2.0346, -0.3252, -0.1682, 0.3300],
    [-0.9789, -0.1849, 1.7235, 0.0139, -0.2249, -0.2053],
    [-0.7289, -0.5558, 1.4817, 0.0165, -0.3217, -0.2116],
  ],
  reaper_sweep: [
    [-0.8712, 0.1671, 1.6024, 0.0121, -0.1409, -0.2003],
    [-0.8374, 0.2664, 1.7487, 0.0133, -0.1874, -0.2030],
    [-1.0552, 0.1358, 1.6369, 0.2553, -0.2958, 0.3549],
    [-1.2404, 0.0268, 1.6516, 0.2894, -0.3757, 0.5737],
    [-1.4163, -0.1123, 1.6445, 0.2972, -0.4346, 0.7260],
  ],
};

function interpolateGrip(name, motion) {
  const grips = ACTION_GRIPS[name];
  if (!grips) return null;
  const clamped = Math.max(-1, Math.min(1, motion));
  let from; let to; let amount;
  if (clamped <= -0.5) { from = 0; to = 1; amount = (clamped + 1) * 2; }
  else if (clamped <= 0) { from = 1; to = 2; amount = (clamped + 0.5) * 2; }
  else if (clamped <= 0.5) { from = 2; to = 3; amount = clamped * 2; }
  else { from = 3; to = 4; amount = (clamped - 0.5) * 2; }
  return grips[from].map((value, index) => value + (grips[to][index] - value) * amount);
}

function applyGrip(pose, grip, blend = 1) {
  if (!grip) return;
  const bones = [["leftArm", grip.slice(0, 3)], ["lElbow", grip.slice(3, 6)]];
  for (const [bone, target] of bones) {
    const current = pose[bone] ?? [0, 0, 0];
    pose[bone] = current.map((value, index) => value + (target[index] - value) * blend);
  }
}

function eveningIdlePose(tendency, phase = 0) {
  const pose = {};
  set(pose, "rightArm", Math.sin(phase) * 0.0325, 0, Math.abs(Math.cos(phase) * 0.0525 + Math.PI / 8));
  set(pose, "leftArm", 0, 0, -Math.abs(Math.sin(phase) * 0.0525 - Math.PI / 8));
  if (tendency === "rage") {
    const twitch = Math.sin(phase * 15.5) * 0.14;
    set(pose, "body", 0.38, twitch * 0.35, 0);
    set(pose, "rightArm", -0.95 + twitch, 0, 0.78);
    set(pose, "leftArm", -0.95 - twitch, 0, -0.78);
    set(pose, "rightLeg", 0.22, 0, 0);
    set(pose, "leftLeg", -0.16, 0, 0);
    return pose;
  }
  switch (tendency) {
    case "animus": assign(pose, "rightArm", 0, pose.rightArm[0] - 0.55); applyGrip(pose, GRIPS.animus); break;
    case "mortem": assign(pose, "rightArm", 0, pose.rightArm[0] - 0.85); set(pose, "body", 0.12, 0, 0); applyGrip(pose, GRIPS.mortem); break;
    case "lux": assign(pose, "rightArm", 0, -1.12); assign(pose, "leftArm", 0, -0.32); break;
    case "tenebris": set(pose, "body", 0.42, 0, 0); assign(pose, "rightArm", 0, -0.8); assign(pose, "leftArm", 0, -0.8); break;
    case "ductilis": assign(pose, "rightArm", 0, -1.15); assign(pose, "leftArm", 0, -1.05); break;
    case "flammeus": assign(pose, "rightArm", 0, -1.05); set(pose, "body", 0, 0.14, 0); break;
    case "congeatio": assign(pose, "rightArm", 0, -0.85); set(pose, "body", 0, Math.sin(phase) * 0.144, 0); break;
    case "ferric": assign(pose, "rightArm", 0, -1.42); assign(pose, "leftArm", 0, -0.55); break;
  }
  return pose;
}

function blendBone(pose, bone, target, blend) {
  const current = pose[bone] ?? [0, 0, 0];
  pose[bone] = current.map((value, index) => value + (target[index] - value) * blend);
}

function eveningActionPose(name, action, tick, sign) {
  const pose = eveningIdlePose(action.tendency, 0);
  const blend = actionBlend(action, tick);
  const arc = swingArc(action, tick);
  const contact = contactMotion(action, tick);
  switch (name) {
    case "ichimonji":
      blendBone(pose, "rightArm", [-1.2 + arc * 1.28, 0, -arc * 0.16 * sign], blend);
      blendBone(pose, "rElbow", [-0.52 + Math.max(0, arc) * 0.22, 0, 0], blend);
      blendBone(pose, "body", [arc * 0.24, -arc * 0.1 * sign, 0], blend);
      applyGrip(pose, interpolateGrip(name, arc), blend); break;
    case "crosscut": { const slash = contact * sign;
      blendBone(pose, "body", [0.1 + Math.abs(slash) * 0.08, slash * 0.78, 0], blend);
      blendBone(pose, "rightArm", [-1.32 + Math.abs(slash) * 0.12, 0, slash * 1.08], blend);
      blendBone(pose, "rElbow", [0, 0, slash * 0.18], blend);
      applyGrip(pose, interpolateGrip(name, slash), blend); break; }
    case "leaping_cleave":
      blendBone(pose, "rightArm", [-1.28 + arc * 1.18, 0, 0.2 + arc * 0.12 * sign], blend);
      blendBone(pose, "rElbow", [-0.48 + Math.max(0, arc) * 0.2, 0, 0], blend);
      blendBone(pose, "body", [arc * 0.32, -arc * 0.1 * sign, 0], blend);
      applyGrip(pose, interpolateGrip(name, arc), blend); break;
    case "reaper_sweep": { const sweep = arc * sign;
      blendBone(pose, "rightArm", [-1.38, 0, sweep * 1.12], blend);
      blendBone(pose, "rElbow", [-0.58 + Math.abs(sweep) * 0.18, 0, 0], blend);
      blendBone(pose, "body", [0.08 + Math.abs(sweep) * 0.1, -sweep * 0.76, 0], blend);
      applyGrip(pose, interpolateGrip(name, sweep), blend); break; }
    case "sky_lance": { const drive = Math.max(0, arc); const brace = Math.max(0, -arc);
      blendBone(pose, "body", [-brace * 0.22 + drive * 1.02, 0, 0], blend);
      blendBone(pose, "rightArm", [-1.52 - brace * 0.22 + drive * 0.18, 0, 0], blend);
      blendBone(pose, "rElbow", [-0.32 + drive * 0.14, 0, 0], blend);
      blendBone(pose, "leftArm", [-1.18 + drive * 0.12, 0, 0], blend);
      blendBone(pose, "rightLeg", [0.62, 0, 0], blend); blendBone(pose, "leftLeg", [0.45, 0, 0], blend);
      blendBone(pose, "ClothBack", [1.35, 0, 0], blend); blendBone(pose, "ClothBack2", [0.95, 0, 0], blend); break; }
    case "lance_flurry": case "branding_thrusts": case "updraft_impalement": { const thrust = contact * sign;
      blendBone(pose, "rightArm", [-1.3 - thrust * 0.58, thrust * 0.16, 0], blend);
      blendBone(pose, "rElbow", [-0.42 + Math.max(0, thrust) * 0.2, 0, 0], blend);
      blendBone(pose, "body", [0.08 + Math.abs(thrust) * 0.08, thrust * 0.25, 0], blend); break; }
    case "twin_rend": case "predator_pounce": { const slash = contact * sign;
      blendBone(pose, "body", [0.34 + Math.abs(slash) * 0.16, slash * 0.2, 0], blend);
      blendBone(pose, "rightArm", [-1.08 + slash * 0.72, 0, 0.72 - slash * 0.16], blend);
      blendBone(pose, "leftArm", [-1.08 - slash * 0.72, 0, -0.72 - slash * 0.16], blend); break; }
    case "conductive_volley": case "storm_lock": { const surge = 0.5 + 0.5 * Math.sin(tick * 0.28);
      blendBone(pose, "body", [0.08 + surge * 0.05, 0, 0], blend);
      blendBone(pose, "rightArm", [-1.35 - surge * 0.08, -0.42, 0], blend);
      blendBone(pose, "leftArm", [-1.35 - surge * 0.08, 0.62, 0], blend);
      blendBone(pose, "rElbow", [-0.35, 0, 0], blend); blendBone(pose, "lElbow", [-0.35, 0, 0], blend); break; }
    case "chain_sweep": case "hook_and_crush": { const isChain = name === "chain_sweep";
      const swing = curve(tick, isChain ? [[0,0],[6,-.25],[12,-.78],[18,.55],[22,1],[30,0]] : [[0,0],[8,-.55],[16,.9],[22,.35],[28,-.9],[34,-.6],[40,0]]) * sign;
      const follow = curve(tick, isChain ? [[0,0],[8,-.25],[14,-.72],[20,.45],[24,.85],[30,0]] : [[0,0],[10,-.5],[18,.75],[24,.3],[30,-.75],[35,-.5],[40,0]]) * sign;
      blendBone(pose, "body", [0.12 + Math.abs(swing) * 0.08, swing * 0.68, 0], blend);
      blendBone(pose, "rightArm", [-1.32 + Math.abs(swing) * 0.1, 0, swing * 0.94], blend);
      blendBone(pose, "rElbow", [0, -follow * 0.32, follow * 0.1], blend); break; }
    case "magnetic_axis": case "iron_retort": { const load = Math.max(0, -arc);
      blendBone(pose, "rightArm", [-1.55 - load * 0.18, 0, 0], blend); blendBone(pose, "rElbow", [-0.34, 0, 0], blend);
      blendBone(pose, "leftArm", [-0.72 - load * 0.12, 0, 0], blend); blendBone(pose, "lElbow", [-0.55, 0, 0], blend);
      blendBone(pose, "body", [0.12 + load * 0.08, 0, 0], blend); break; }
    case "sickle_cyclone": { const impact = action.contacts.at(-1); let spin;
      if (tick <= impact) spin = -0.7 * smootherstep(tick / impact) * sign;
      else spin = (-0.7 + (Math.PI * 4 + 0.7) * smootherstep((tick - impact) / (action.duration - impact))) * sign;
      blendBone(pose, "body", [0.32, spin, 0], blend); assign(pose, "body", 1, spin);
      blendBone(pose, "rightArm", [-1.18, 0, 1.28], blend); blendBone(pose, "leftArm", [-1.18, 0, -1.28], blend);
      blendBone(pose, "rightLeg", [0.48, 0, 0], blend); blendBone(pose, "leftLeg", [-0.35, 0, 0], blend); break; }
    case "sickle_pounce": { const cross = arc * 1.18;
      blendBone(pose, "body", [0.48 + Math.max(0, arc) * 0.42, 0, 0], blend);
      blendBone(pose, "rightArm", [-1.08 + cross * 0.58, 0, 1.05 - cross], blend);
      blendBone(pose, "leftArm", [-1.08 + cross * 0.58, 0, -1.05 + cross], blend);
      blendBone(pose, "rightLeg", [0.72, 0, 0], blend); blendBone(pose, "leftLeg", [0.62, 0, 0], blend); break; }
    case "sickle_cross_rend": { const rend = contact * sign;
      blendBone(pose, "body", [0.28 + Math.abs(rend) * 0.1, rend * 0.92, 0], blend);
      blendBone(pose, "rightArm", [-1 - Math.abs(rend) * 0.62, 0, 0.9 - rend * 1.35], blend);
      blendBone(pose, "leftArm", [-1 - Math.abs(rend) * 0.62, 0, -0.9 - rend * 1.35], blend); break; }
    case "sickle_hook": { const draw = Math.max(0, Math.min(1, tick / 14)); const release = Math.max(0, Math.min(1, (tick - 14) / 8));
      blendBone(pose, "body", [0.24 - draw * 0.12, -0.48 * draw + 0.28 * release, 0], blend);
      blendBone(pose, "rightArm", [-1.05 - draw * 0.72 + release * 0.85, -0.75 * draw + release * 0.9, 0.82 - release * 0.64], blend);
      blendBone(pose, "leftArm", [-1.18, 0, -0.88], blend); break; }
    case "sanguine_crescents": { const slash = contact * sign;
      blendBone(pose, "body", [0.22 + Math.abs(slash) * 0.08, slash * 0.52, 0], blend);
      blendBone(pose, "rightArm", [-1.42, -0.42 - slash * 0.45, 0.8 - slash * 1.18], blend);
      blendBone(pose, "leftArm", [-1.42, 0.42 + slash * 0.45, -0.8 - slash * 1.18], blend); break; }
  }
  return pose;
}

function actionTicks(name, action) {
  const ticks = new Set([0, 3, 6, action.duration - 7, action.duration]);
  for (const contact of action.contacts) {
    for (const tick of [contact - 6, contact - 3, contact, contact + 1, contact + 4])
      if (tick >= 0 && tick <= action.duration) ticks.add(tick);
  }
  if (name === "chain_sweep") [8,12,14,18,20,22,24,30].forEach(tick => ticks.add(tick));
  if (name === "hook_and_crush") [8,10,16,18,22,24,28,30,34,35,40].forEach(tick => ticks.add(tick));
  if (name === "sickle_hook") [8,14,18,22,28].forEach(tick => ticks.add(tick));
  return [...ticks].filter(tick => tick >= 0 && tick <= action.duration).sort((a, b) => a - b);
}

function buildEveningAnimations(model) {
  const modelName = "VesperTheEveningStarModel";
  const animations = [];
  const baseLoopTicks = [0, 15, 30, 45, 60];
  const baseLoop = baseLoopTicks.map((tick, index) => {
    const phase = index === baseLoopTicks.length - 1 ? 0 : tick * 0.12;
    const pose = eveningIdlePose("lux", phase);
    set(pose, "ClothBack", Math.sin(tick * .3) * .05, 0, 0);
    set(pose, "ClothBack1", Math.sin(tick * .5) * .08, 0, 0);
    set(pose, "ClothBack2", Math.sin(tick * .7) * .12, 0, 0);
    if (index === baseLoopTicks.length - 1) {
      set(pose, "ClothBack", 0, 0, 0); set(pose, "ClothBack1", 0, 0, 0); set(pose, "ClothBack2", 0, 0, 0);
    }
    return poseSample(tick, pose);
  });
  animations.push(makeAnimation(model, modelName, "idle", 3, "loop", rotationSamples(baseLoop)));

  const walkSamples = [0, 5, 10, 15, 20].map(tick => {
    const phase = tick / 20 * Math.PI * 2;
    const pose = eveningIdlePose("lux", 0);
    set(pose, "rightArm", Math.cos(phase + Math.PI), 0, Math.PI / 8);
    set(pose, "leftArm", Math.cos(phase), 0, -Math.PI / 8);
    set(pose, "rightLeg", Math.cos(phase) * .7, 0, 0); set(pose, "rightLeg2", Math.abs(Math.cos(phase + Math.PI)) * .7, 0, 0);
    set(pose, "leftLeg", Math.cos(phase + Math.PI) * .7, 0, 0); set(pose, "leftLeg2", Math.abs(Math.sin(phase + Math.PI)) * .7, 0, 0);
    return poseSample(tick, pose);
  });
  animations.push(makeAnimation(model, modelName, "walk", 1, "loop", rotationSamples(walkSamples)));

  const tendencies = ["animus","mortem","lux","tenebris","ductilis","flammeus","congeatio","ferric"];
  for (const tendency of tendencies) {
    const idle = [0, 13, 26, 39, 52].map((tick, index) => poseSample(tick,
      eveningIdlePose(tendency, index === 4 ? 0 : tick * .12)));
    animations.push(makeAnimation(model, modelName, `idle_${tendency}`, 2.6, "loop", rotationSamples(idle)));
    const stance = [0, 6, 15, 30].map(tick => {
      const pose = eveningIdlePose(tendency, 0);
      const morph = smootherstep(tick / 30);
      pose.rightArm[0] -= morph * .8; pose.rightArm[2] += morph * .25;
      if (GRIPS[tendency]) applyGrip(pose, GRIPS[tendency], morph);
      return poseSample(tick, pose);
    });
    animations.push(makeAnimation(model, modelName, `stance_${tendency}`, 1.5, "once", rotationSamples(stance)));
  }
  const rageIdle = [0, 3, 5, 8, 10].map(tick => poseSample(tick,
    eveningIdlePose("rage", tick === 10 ? 0 : tick * .62)));
  animations.push(makeAnimation(model, modelName, "rage_idle", .5, "loop", rotationSamples(rageIdle)));

  for (const [name, action] of Object.entries(ACTIONS)) {
    for (const sign of ALTERNATING.has(name) ? [1, -1] : [1]) {
      const samples = actionTicks(name, action).map(tick => poseSample(tick,
        eveningActionPose(name, action, tick, sign)));
      animations.push(makeAnimation(model, modelName, sign < 0 ? `${name}_alternate` : name,
        action.duration / 20, "once", rotationSamples(samples)));
    }
  }

  const defeatTicks = [0, 3, 6, 12, 20, 32];
  const defeatSamples = defeatTicks.map(tick => {
    const recoilPhase = Math.max(0, Math.min(1, tick / 6));
    const recoil = 1 - Math.abs(recoilPhase * 2 - 1);
    const rawKneel = Math.max(0, Math.min(1, (tick - 6) / 26));
    const kneel = rawKneel * rawKneel * (3 - 2 * rawKneel);
    const pose = eveningIdlePose("lux", 0);
    blendBone(pose, "head", [-recoil * .16, 0, 0], 1);
    blendBone(pose, "body", [-recoil * .22, 0, 0], 1);
    blendBone(pose, "rightArm", [pose.rightArm[0] + recoil * .28, 0, pose.rightArm[2]], 1);
    blendBone(pose, "leftArm", [pose.leftArm[0] + recoil * .28, 0, pose.leftArm[2]], 1);
    for (const [bone, target] of Object.entries({
      head:[.48,0,0], body:[.24,0,0], rightArm:[-.38,0,.18], rElbow:[-.72,0,0],
      leftArm:[-.18,0,-.14], lElbow:[-.66,0,0], rightLeg:[-1.18,0,0], rightLeg2:[1.82,0,0],
      leftLeg:[.18,0,0], leftLeg2:[.55,0,0], ClothBack:[.82,0,0], ClothBack1:[.24,0,0], ClothBack2:[.18,0,0], ClothBackL3:[.12,0,0],
    })) blendBone(pose, bone, target, kneel);
    return poseSample(tick, pose);
  });
  const defeatTracks = rotationSamples(defeatSamples);
  defeatTracks.whole = { position: defeatTicks.map(tick => {
    const raw = Math.max(0, Math.min(1, (tick - 6) / 26)); const kneel = raw * raw * (3 - 2 * raw);
    return [tick / 20, [0, round(-6.5917 * kneel), 0]];
  }) };
  animations.push(makeAnimation(model, modelName, "defeat", 1.6, "once", defeatTracks));
  return animations;
}

function crownedAmbientPose(tick, closeLoop = false) {
  const frame = closeLoop ? 0 : tick;
  const breathe = Math.sin(frame * .13);
  const pose = {};
  set(pose, "fLeftArm", Math.sin(frame * .13) * .035, 0, 0);
  set(pose, "fRightArm", Math.sin(frame * .13 + Math.PI) * .035, 0, 0);
  set(pose, "upperJaw", breathe * .055, 0, 0); set(pose, "lowerJaw", -breathe * .072, 0, 0);
  set(pose, "tail", 0, breathe * .035, 0); set(pose, "tail2", 0, -breathe * .10, 0);
  set(pose, "tail3", 0, breathe * .11, 0); set(pose, "tail4", 0, -breathe * .12, 0); set(pose, "tail5", 0, breathe * .10, 0);
  set(pose, "ClothBack", Math.sin(frame * .24) * .035, 0, 0); set(pose, "ClothBack1", Math.sin(frame * .31) * .055, 0, 0);
  set(pose, "ClothBack2", Math.sin(frame * .39) * .075, 0, 0); set(pose, "ClothBackR2", Math.sin(frame * .43) * .06, 0, 0);
  set(pose, "ClothBackL2", Math.sin(frame * .43 + Math.PI) * .06, 0, 0);
  return pose;
}

function stingerMotion(tick) {
  if (tick <= 8) return 0;
  if (tick <= 16) return smootherstep((tick - 8) / 8);
  const contacts = [16, 28, 40, 52];
  for (let index = 0; index < contacts.length - 1; index++) {
    if (tick <= contacts[index + 1]) {
      const from = index % 2 === 0 ? 1 : -1;
      return from + (-2 * from) * smootherstep((tick - contacts[index]) / 12);
    }
  }
  return -1 + smootherstep((tick - 52) / 16);
}

function broodPitch(tick) {
  if (tick <= 22) return -.16 * smootherstep(tick / 22);
  if (tick <= 32) return -.16;
  if (tick <= 38) return -.16 + .38 * smootherstep((tick - 32) / 6);
  return .22 - .22 * smootherstep((tick - 38) / 24);
}

function buildCrownedAnimations(model) {
  const modelName = "VesperTheCrownedRefusalModel";
  const animations = [];
  const idleTicks = [0, 15, 30, 45, 60];
  const idle = idleTicks.map((tick, index) => poseSample(tick,
    crownedAmbientPose(tick, index === idleTicks.length - 1)));
  animations.push(makeAnimation(model, modelName, "idle", 3, "loop", rotationSamples(idle)));

  const walkTicks = [0, 5, 10, 15, 20];
  const walk = walkTicks.map(tick => {
    const phase = tick / 20 * Math.PI * 2; const pose = crownedAmbientPose(0);
    const legs = [["fLeg","fLFemur2","fLTibia2",0],["fLeg2","fLFemur3","fLTibia3",Math.PI],
      ["fLeg3","fLFemur4","fLTibia6",.7],["fLeg4","fLFemur5","fLTibia8",Math.PI],
      ["fLeg5","fLFemur6","fLTibia10",0],["fLeg6","fLFemur7","fLTibia12",Math.PI+.7]];
    for (const [leg,femur,tibia,offset] of legs) {
      const stride = Math.cos(phase + offset) * .72;
      set(pose, leg, stride * .35, 0, 0); set(pose, femur, stride, 0, 0); set(pose, tibia, Math.abs(Math.sin(phase + offset)) * .72, 0, 0);
    }
    return poseSample(tick, pose);
  });
  const walkTracks = rotationSamples(walk);
  animations.push(makeAnimation(model, modelName, "walk", 1, "loop", walkTracks));
  animations.push(makeAnimation(model, modelName, "royal_scuttle", 3.1, "once",
    rotationSamples(walk.map((sample, index) => poseSample(index * 15.5, sample.pose)))));

  const vulnerable = [0, 15, 30, 45, 60].map((tick, index) => {
    const pose = crownedAmbientPose(tick, index === 4); set(pose, "vesper", .18, 0, Math.sin(tick * .2) * .035); set(pose, "lowerBody", .32, 0, 0);
    return poseSample(tick, pose);
  });
  const vulnerableTracks = rotationSamples(vulnerable);
  vulnerableTracks.vesper.position = [[0,[0,0,0]],[.35,[0,-7,0]],[3,[0,-7,0]]];
  animations.push(makeAnimation(model, modelName, "vulnerable", 3, "loop", vulnerableTracks));

  const pincerTicks = [0, 18, 24, 30, 38, 44, 58];
  const pincer = pincerTicks.map(tick => poseSample(tick, crownedAmbientPose(tick)));
  animations.push(makeAnimation(model, modelName, "pincer_vice", 2.9, "once", rotationSamples(pincer)));

  const stingerTicks = [0, 8, 16, 22, 28, 34, 40, 46, 52, 60, 68];
  const stinger = stingerTicks.map(tick => { const pose = crownedAmbientPose(tick); const strike = stingerMotion(tick);
    ["tail","tail2","tail3","tail4","tail5"].forEach((bone,index) => assign(pose,bone,0,-[.28,.38,.48,.58,.68][index]*strike));
    return poseSample(tick,pose); });
  animations.push(makeAnimation(model, modelName, "stinger_script", 3.4, "once", rotationSamples(stinger)));

  const broodTicks = [0, 11, 22, 32, 38, 50, 62];
  const brood = broodTicks.map(tick => { const pose=crownedAmbientPose(tick); set(pose,"lowerBody",broodPitch(tick),0,0); return poseSample(tick,pose); });
  animations.push(makeAnimation(model, modelName, "brood_trample", 3.1, "once", rotationSamples(brood)));

  const musterTicks = [0, 16, 28, 32, 44, 56, 68];
  const muster = musterTicks.map(tick => poseSample(tick, crownedAmbientPose(tick)));
  animations.push(makeAnimation(model, modelName, "puppet_muster", 3.4, "once", rotationSamples(muster)));

  const transitionTicks = [0, 9, 18, 27, 36, 48, 72, 96, 120];
  const transitionSamples = transitionTicks.map(tick => {
    const pose = crownedAmbientPose(tick); const dismountRaw=Math.max(0,Math.min(1,tick/36)); const dismount=smootherstep(dismountRaw);
    const collapse=dismountRaw*dismountRaw*(3-2*dismountRaw); const absorbRaw=Math.max(0,Math.min(1,(tick-36)/84)); const absorption=smootherstep(absorbRaw);
    set(pose,"vesper",-.16*Math.sin(dismount*Math.PI),0,.08*Math.sin(dismount*Math.PI)); set(pose,"body",.12*absorption,0,0);
    blendBone(pose,"leftArm",[-1.05,.28,0],absorption); blendBone(pose,"rightArm",[-1.05,-.28,0],absorption); blendBone(pose,"head2",[-.18,0,0],absorption);
    set(pose,"lowerBody",.18*collapse,0,.10*collapse); set(pose,"lLegs",0,0,.42*collapse); set(pose,"lLegs2",0,0,-.42*collapse);
    set(pose,"head",.24*collapse,-.16*collapse,.42*collapse); blendBone(pose,"upperJaw",[-.18,0,0],collapse); blendBone(pose,"lowerJaw",[.34,0,0],collapse);
    ["tail","tail2","tail3","tail4"].forEach((bone,index)=>assign(pose,bone,0,[.34,.40,.46,.52][index]*collapse)); blendBone(pose,"tail5",[1.12,0,0],collapse);
    return poseSample(tick,pose);
  });
  const transitionTracks=rotationSamples(transitionSamples);
  transitionTracks.vesper.position=transitionTicks.map(tick=>{const p=Math.max(0,Math.min(1,tick/36));const d=smootherstep(p);const a=smootherstep(Math.max(0,Math.min(1,(tick-36)/84)));const jump=4*p*(1-p);return[tick/20,[0,round(-(18*d-20*jump+8*a)),round(-24*d-10*a)]];});
  transitionTracks.lowerBody.position=transitionTicks.map(tick=>{const p=Math.max(0,Math.min(1,tick/36));const c=p*p*(3-2*p);return[tick/20,[0,round(-10*c),0]];});
  transitionTracks.lowerBody.scale=transitionTicks.map(tick=>{const a=smootherstep(Math.max(0,Math.min(1,(tick-36)/84)));const scale=Math.max(.03,1-a*.97);return[tick/20,[round(scale),round(Math.max(.02,scale*.72)),round(scale)]];});
  animations.push(makeAnimation(model,modelName,"transformation",6,"once",transitionTracks));
  return animations;
}

const eveningFile = "VesperTheEveningStarModel.bbmodel";
const crownedFile = "VesperTheCrownedRefusalModel.bbmodel";
const evening = readModel(eveningFile);
const crowned = readModel(crownedFile);
install(evening, "VesperTheEveningStarModel", buildEveningAnimations(evening));
install(crowned, "VesperTheCrownedRefusalModel", buildCrownedAnimations(crowned));
writeModel(eveningFile, evening);
writeModel(crownedFile, crowned);
console.log(`Updated ${eveningFile}: ${evening.animations.length} animations`);
console.log(`Updated ${crownedFile}: ${crowned.animations.length} animations`);

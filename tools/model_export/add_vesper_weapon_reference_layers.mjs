import crypto from "node:crypto";
import { execFileSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_PATH = path.join(ROOT,
  "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/VesperTheEveningStarModel.bbmodel");
const ITEM_BBMODEL_DIR = path.join(ROOT,
  "src/main/resources/assets/hemomancy/models/item/bbmodel");
const TEMP_DIR = path.join(os.tmpdir(), "hemomancy-vesper-weapon-reference-models");
const REF_PREFIX = "[REF]";

function uuid(seed) {
  const hex = crypto.createHash("sha256").update(seed).digest("hex").slice(0, 32);
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function groupRecord(name, groupUuid, origin, rotation, visibility) {
  return {
    name, uuid: groupUuid, export: false, locked: false, scope: 0, selected: false,
    _static: { properties: {}, temp_data: {} }, origin, rotation, color: 0,
    children: [], reset: false, shade: true, mirror_uv: false, visibility,
    autouv: 0, isOpen: true, primary_selected: false,
  };
}

function generatedSource(className, textureName) {
  fs.mkdirSync(TEMP_DIR, { recursive: true });
  const javaPath = path.join(ROOT,
    `src/main/java/com/vincenthuto/hemomancy/client/model/item/${className}.java`);
  execFileSync(process.execPath, [
    path.join(ROOT, "tools/model_export/java_model_to_bbmodel.mjs"),
    "--source", javaPath, "--out", TEMP_DIR,
  ], { cwd: ROOT, stdio: "ignore" });
  return {
    path: path.join(TEMP_DIR, `${className}.bbmodel`),
    texturePath: path.join(ROOT,
      `src/main/resources/assets/hemomancy/textures/entity/${textureName}`),
  };
}

const generated = {
  blade: generatedSource("LivingBladeHandTameModel", "model_living_blade_hand.png"),
  axe: generatedSource("LivingAxeModel", "model_living_axe_hand.png"),
  spear: generatedSource("LivingSpearModel", "model_living_spear_hand.png"),
  torch: generatedSource("LivingTorchModel", "model_living_torch.png"),
  flail: generatedSource("LivingFlailModel", "model_living_flail.png"),
  sickle: generatedSource("LivingSickleModel", "model_living_sickle.png"),
};

const rightAnchor = [9.9375, 17.10333, -2.65];
const leftAnchor = [-9.1375, 22.86333, 3.27];
const itemSource = file => path.join(ITEM_BBMODEL_DIR, file);
const layers = [
  { side:"RIGHT", label:"ANIMUS Living Blade", key:"right_blade", source:generated.blade.path, texturePath:generated.blade.texturePath, grip:[0,24,0], anchor:rightAnchor,
    transforms:[{rotation:[90,0,0]},{rotation:[0,180,0]},{scale:1.55},{rotation:[-180,0,0]},{rotation:[0,180,0]},{scale:.5},{translation:[-1,-1,1],units:"blocks"}] },
  { side:"RIGHT", label:"MORTEM Living Axe", key:"right_axe", source:generated.axe.path, texturePath:generated.axe.texturePath, grip:[0,24,0], anchor:rightAnchor,
    transforms:[{rotation:[90,0,0]},{rotation:[0,180,0]},{scale:1.55},{rotation:[-180,0,0]},{rotation:[0,180,0]},{scale:.65},{translation:[-.6,.25,.25],units:"blocks"}] },
  { side:"RIGHT", label:"LUX Living Spear", key:"right_spear", source:generated.spear.path, texturePath:generated.spear.texturePath, grip:[0,24,0], anchor:rightAnchor,
    transforms:[{rotation:[98,0,0]},{rotation:[0,180,0]},{scale:1.55},{rotation:[-180,0,0]},{rotation:[0,180,0]},{scale:.65},{translation:[-.675,0,.25],units:"blocks"}] },
  { side:"RIGHT", label:"TENEBRIS Living Baghnakh", key:"right_baghnakh", source:itemSource("living_baghnakh.bbmodel"), grip:[8,1.7,8], anchor:rightAnchor,
    transforms:[{rotation:[72,0,0]},{rotation:[0,180,0]},{scale:1.55},{translation:[7.25,-2,-.5],units:"pixels"},{rotation:[0,0,90]}] },
  { side:"RIGHT", label:"DUCTILIS Living Crossbow", key:"right_crossbow", source:itemSource("living_crossbow.bbmodel"), grip:[8,5,8], anchor:rightAnchor,
    transforms:[{rotation:[90,0,0]},{rotation:[0,180,0]},{scale:1.55},{translation:[.25,1.25,2.25],units:"pixels"}] },
  { side:"RIGHT", label:"FLAMMEUS Living Torch", key:"right_torch", source:generated.torch.path, texturePath:generated.torch.texturePath, grip:[0,0,0], anchor:rightAnchor,
    transforms:[{rotation:[98,0,0]},{rotation:[0,180,0]},{scale:1.55},{rotation:[-180,0,0]},{rotation:[0,180,0]},{scale:.68},{translation:[-.14,.08,.18],units:"blocks"},{rotation:[0,0,-180]},{translation:[.6,.62,.55],units:"blocks"}] },
  { side:"RIGHT", label:"CONGEATIO Living Flail", key:"right_flail", source:generated.flail.path, texturePath:generated.flail.texturePath, grip:[0,24,0], anchor:rightAnchor,
    transforms:[{translation:[.08,.06,-.09],units:"blocks"},{rotation:[0,0,-8]},{scale:1.395},{rotation:[0,0,-180]},{rotation:[80,0,0]},{translation:[.15,-.1,-.2],units:"blocks"}] },
  { side:"RIGHT", label:"FERRIC Living Staff", key:"right_staff", source:itemSource("living_staff.bbmodel"), grip:[8,1,8], anchor:rightAnchor,
    transforms:[{rotation:[90,0,0]},{rotation:[0,180,0]},{scale:1.55},{translation:[0,-9.25,-.25],units:"pixels"}] },
  { side:"RIGHT", label:"RAGE Living Sickle", key:"right_sickle", source:generated.sickle.path, texturePath:generated.sickle.texturePath, grip:[0,2,0], anchor:rightAnchor,
    transforms:[{translation:[.045,.04,-.10],units:"blocks"},{rotation:[92,0,0]},{rotation:[0,180,0]},{rotation:[0,0,-14]},{scale:1.55},{rotation:[-180,0,0]},{rotation:[0,180,0]},{scale:.7},{translation:[-.3,-.15,0],units:"blocks"}] },
  { side:"LEFT", label:"TENEBRIS Living Baghnakh", key:"left_baghnakh", source:itemSource("living_baghnakh.bbmodel"), grip:[8,1.7,8], anchor:leftAnchor,
    transforms:[{rotation:[-72,0,0]},{scale:1.55},{translation:[7.25,-2,-.5],units:"pixels"},{rotation:[0,0,90]}] },
  { side:"LEFT", label:"RAGE Living Sickle", key:"left_sickle", source:generated.sickle.path, texturePath:generated.sickle.texturePath, grip:[0,2,0], anchor:leftAnchor,
    transforms:[{translation:[-.045,.04,-.10],units:"blocks"},{rotation:[92,0,0]},{rotation:[0,0,14]},{scale:1.55},{rotation:[-180,0,0]},{rotation:[0,180,0]},{scale:.7},{translation:[-.3,-.15,0],units:"blocks"}] },
];

function stripExistingReferences(model) {
  const referenceGroups = new Set(model.groups
    .filter(group => group.name?.startsWith(REF_PREFIX)).map(group => group.uuid));
  model.groups = model.groups.filter(group => !referenceGroups.has(group.uuid));
  model.elements = model.elements.filter(element => !element.name?.startsWith(REF_PREFIX));
  model.textures = model.textures.filter(texture => !texture.name?.startsWith(REF_PREFIX));
  const cleanNodes = nodes => (nodes ?? []).flatMap(node => {
    if (typeof node === "string") return [node];
    if (referenceGroups.has(node.uuid)) return [];
    return [{ ...node, children: cleanNodes(node.children) }];
  });
  model.outliner = cleanNodes(model.outliner);
  model.textures.forEach((texture, index) => { texture.id = String(index); });
}

function transformPoint(point, grip, anchor, scale) {
  return point.map((value, axis) =>
    Math.round((anchor[axis] + (value - grip[axis]) * scale) * 100000) / 100000);
}

function translatedVector(step) {
  const unitScale = step.units === "blocks" ? 16 : 1;
  return step.translation.map((value, axis) => {
    const axisSign = axis === 1 ? -1 : 1;
    return value * unitScale * axisSign;
  });
}

const identityMatrix = () => [[1,0,0],[0,1,0],[0,0,1]];
const multiplyMatrices = (a, b) => a.map((row, y) => row.map((_, x) =>
  row.reduce((sum, value, k) => sum + value * b[k][x], 0)));
const multiplyVector = (matrix, vector) => matrix.map(row =>
  row.reduce((sum, value, index) => sum + value * vector[index], 0));
const transpose = matrix => matrix[0].map((_, x) => matrix.map(row => row[x]));
const addVectors = (a, b) => a.map((value, index) => value + b[index]);
const scaleVector = (vector, scale) => vector.map(value => value * scale);

function rotationMatrix(rotation) {
  const [x, y, z] = rotation.map(value => value * Math.PI / 180);
  const rx = [[1,0,0],[0,Math.cos(x),-Math.sin(x)],[0,Math.sin(x),Math.cos(x)]];
  const ry = [[Math.cos(y),0,Math.sin(y)],[0,1,0],[-Math.sin(y),0,Math.cos(y)]];
  const rz = [[Math.cos(z),-Math.sin(z),0],[Math.sin(z),Math.cos(z),0],[0,0,1]];
  return multiplyMatrices(multiplyMatrices(rz, ry), rx);
}

function collapseTransforms(transforms) {
  let rotation = identityMatrix();
  let scale = 1;
  let worldOffset = [0,0,0];
  for (const step of transforms) {
    if (step.translation) {
      worldOffset = addVectors(worldOffset,
        scaleVector(multiplyVector(rotation, translatedVector(step)), scale));
    } else if (step.rotation) {
      rotation = multiplyMatrices(rotation, rotationMatrix(step.rotation));
    } else if (step.scale) {
      scale *= step.scale;
    }
  }
  return { scale, localOffset: multiplyVector(transpose(rotation), worldOffset) };
}

function sourceGroups(source) {
  const result = new Map((source.groups ?? []).map(group => [group.uuid, group]));
  const visit = nodes => {
    for (const node of nodes ?? []) {
      if (typeof node !== "object") continue;
      if (!result.has(node.uuid) && node.name) result.set(node.uuid, node);
      visit(node.children);
    }
  };
  visit(source.outliner);
  return result;
}

function addTextures(model, source, layer) {
  const indexMap = new Map();
  const uuidMap = new Map();
  source.textures.forEach((texture, sourceIndex) => {
    const targetIndex = model.textures.length;
    const textureUuid = uuid(`vesper-reference:${layer.key}:texture:${sourceIndex}`);
    const clone = structuredClone(texture);
    clone.id = String(targetIndex);
    clone.uuid = textureUuid;
    clone.name = `${REF_PREFIX} ${layer.key} ${texture.name}`;
    if (layer.texturePath) {
      clone.path = layer.texturePath;
      clone.relative_path = path.relative(path.dirname(MODEL_PATH), layer.texturePath).replaceAll("\\", "/");
      clone.name = `${REF_PREFIX} ${layer.key} ${path.basename(layer.texturePath)}`;
      delete clone.source;
    }
    model.textures.push(clone);
    indexMap.set(sourceIndex, targetIndex);
    uuidMap.set(texture.uuid, textureUuid);
  });
  return { indexMap, uuidMap };
}

function addLayer(model, layer, folderNode) {
  const source = JSON.parse(fs.readFileSync(layer.source, "utf8"));
  const groups = sourceGroups(source);
  const textures = addTextures(model, source, layer);
  const elementUuidMap = new Map();
  const groupUuidMap = new Map();

  const layerUuid = uuid(`vesper-reference:${layer.key}:layer`);
  const layerGroup = groupRecord(`${REF_PREFIX} ${layer.side} ${layer.label}`,
    layerUuid, layer.anchor, [0,0,0], false);
  layerGroup.vesper_reference_transform = { key: layer.key, steps: layer.transforms };
  model.groups.push(layerGroup);
  const layerNode = { uuid: layerUuid, isOpen: false, children: [] };
  folderNode.children.push(layerNode);

  let transformNode = layerNode;
  layer.transforms.forEach((step, index) => {
    if (!step.rotation) return;
    const transformUuid = uuid(`vesper-reference:${layer.key}:transform:${index}`);
    const detail = `rotate ${step.rotation.join(",")}`;
    model.groups.push(groupRecord(`${REF_PREFIX} XFORM ${layer.key} ${index + 1}: ${detail}`,
      transformUuid, [...layer.anchor], step.rotation, true));
    const child = { uuid: transformUuid, isOpen: false, children: [] };
    transformNode.children.push(child);
    transformNode = child;
  });

  const collapsed = collapseTransforms(layer.transforms);
  const positionOrigin = addVectors(layer.anchor, collapsed.localOffset)
    .map(value => Math.round(value * 100000) / 100000);
  const positionUuid = uuid(`vesper-reference:${layer.key}:position`);
  model.groups.push(groupRecord(`${REF_PREFIX} POSITION ${layer.key}`,
    positionUuid, positionOrigin, [0,0,0], true));
  const positionNode = { uuid: positionUuid, isOpen: false, children: [] };
  transformNode.children.push(positionNode);
  transformNode = positionNode;

  source.elements.forEach((element, index) => {
    const clone = structuredClone(element);
    const targetUuid = uuid(`vesper-reference:${layer.key}:element:${index}`);
    elementUuidMap.set(element.uuid, targetUuid);
    clone.uuid = targetUuid;
    clone.name = `${REF_PREFIX} ${layer.key} ${element.name || "cube"}`;
    clone.export = false;
    if (clone.from) clone.from = transformPoint(clone.from, layer.grip, positionOrigin, collapsed.scale);
    if (clone.to) clone.to = transformPoint(clone.to, layer.grip, positionOrigin, collapsed.scale);
    if (clone.origin) clone.origin = transformPoint(clone.origin, layer.grip, positionOrigin, collapsed.scale);
    for (const face of Object.values(clone.faces ?? {})) {
      if (typeof face.texture === "number") face.texture = textures.indexMap.get(face.texture) ?? face.texture;
      else if (typeof face.texture === "string" && textures.uuidMap.has(face.texture))
        face.texture = textures.uuidMap.get(face.texture);
    }
    model.elements.push(clone);
  });

  for (const [sourceUuid] of groups) {
    groupUuidMap.set(sourceUuid, uuid(`vesper-reference:${layer.key}:group:${sourceUuid}`));
  }

  const cloneNode = node => {
    if (typeof node === "string") return elementUuidMap.get(node);
    const sourceGroup = groups.get(node.uuid) ?? node;
    const targetUuid = groupUuidMap.get(node.uuid)
      ?? uuid(`vesper-reference:${layer.key}:anonymous-group:${node.uuid}`);
    if (!groupUuidMap.has(node.uuid)) groupUuidMap.set(node.uuid, targetUuid);
    model.groups.push(groupRecord(
      `${REF_PREFIX} ${layer.key} :: ${sourceGroup.name || "group"}`,
      targetUuid,
      transformPoint(sourceGroup.origin ?? layer.grip, layer.grip, positionOrigin, collapsed.scale),
      sourceGroup.rotation ?? [0,0,0],
      true));
    return { uuid: targetUuid, isOpen: false,
      children: (node.children ?? []).map(cloneNode).filter(Boolean) };
  };

  transformNode.children.push(...source.outliner.map(cloneNode).filter(Boolean));
}

function findOutlinerNode(nodes, targetUuid) {
  for (const node of nodes ?? []) {
    if (typeof node !== "object") continue;
    if (node.uuid === targetUuid) return node;
    const nested = findOutlinerNode(node.children, targetUuid);
    if (nested) return nested;
  }
  return null;
}

const model = JSON.parse(fs.readFileSync(MODEL_PATH, "utf8"));
const animationSnapshot = JSON.stringify(model.animations);
stripExistingReferences(model);

for (const side of ["RIGHT", "LEFT"]) {
  const elbowName = side === "RIGHT" ? "rElbow" : "rElbow2";
  const elbow = model.groups.find(group => group.name === elbowName);
  const elbowNode = findOutlinerNode(model.outliner, elbow.uuid);
  if (!elbowNode) throw new Error(`Could not find ${elbowName} in the outliner`);
  const folderUuid = uuid(`vesper-reference:${side.toLowerCase()}:folder`);
  const folderName = `${REF_PREFIX} ${side}-HAND LIVING WEAPONS`;
  model.groups.push(groupRecord(folderName, folderUuid, elbow.origin, [0,0,0], true));
  const folderNode = { uuid: folderUuid, isOpen: true, children: [] };
  elbowNode.children.push(folderNode);
  for (const layer of layers.filter(candidate => candidate.side === side)) addLayer(model, layer, folderNode);
}

if (JSON.stringify(model.animations) !== animationSnapshot)
  throw new Error("Weapon reference injection modified authored animation data");

fs.writeFileSync(MODEL_PATH, `${JSON.stringify(model, null, 2)}\n`);
console.log(`Added ${layers.length} weapon reference layers to ${path.basename(MODEL_PATH)}`);

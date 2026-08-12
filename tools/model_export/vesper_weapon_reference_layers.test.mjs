import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_PATH = path.join(ROOT,
  "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/VesperTheEveningStarModel.bbmodel");

const RIGHT_LAYERS = [
  "ANIMUS Living Blade", "MORTEM Living Axe", "LUX Living Spear",
  "TENEBRIS Living Baghnakh", "DUCTILIS Living Crossbow", "FLAMMEUS Living Torch",
  "CONGEATIO Living Flail", "FERRIC Living Staff", "RAGE Living Sickle",
];
const LEFT_LAYERS = ["TENEBRIS Living Baghnakh", "RAGE Living Sickle"];

const EXPECTED_TRANSFORMS = {
  right_blade: [
    { rotation:[90,0,0] }, { rotation:[0,180,0] }, { scale:1.55 },
    { rotation:[-180,0,0] }, { rotation:[0,180,0] }, { scale:0.5 }, { translation:[-1,-1,1], units:"blocks" },
  ],
  right_axe: [
    { rotation:[90,0,0] }, { rotation:[0,180,0] }, { scale:1.55 },
    { rotation:[-180,0,0] }, { rotation:[0,180,0] }, { scale:0.65 }, { translation:[-0.6,0.25,0.25], units:"blocks" },
  ],
  right_spear: [
    { rotation:[98,0,0] }, { rotation:[0,180,0] }, { scale:1.55 },
    { rotation:[-180,0,0] }, { rotation:[0,180,0] }, { scale:0.65 }, { translation:[-0.675,0,0.25], units:"blocks" },
  ],
  right_baghnakh: [
    { rotation:[72,0,0] }, { rotation:[0,180,0] }, { scale:1.55 },
    { translation:[7.25,-2,-0.5], units:"pixels" }, { rotation:[0,0,90] },
  ],
  right_crossbow: [
    { rotation:[90,0,0] }, { rotation:[0,180,0] }, { scale:1.55 },
    { translation:[0.25,1.25,2.25], units:"pixels" },
  ],
  right_torch: [
    { rotation:[98,0,0] }, { rotation:[0,180,0] }, { scale:1.55 },
    { rotation:[-180,0,0] }, { rotation:[0,180,0] }, { scale:0.68 }, { translation:[-0.14,0.08,0.18], units:"blocks" },
    { rotation:[0,0,-180] }, { translation:[0.6,0.62,0.55], units:"blocks" },
  ],
  right_flail: [
    { translation:[0.08,0.06,-0.09], units:"blocks" }, { rotation:[0,0,-8] }, { scale:1.395 },
    { rotation:[0,0,-180] }, { rotation:[80,0,0] }, { translation:[0.15,-0.1,-0.2], units:"blocks" },
  ],
  right_staff: [
    { rotation:[90,0,0] }, { rotation:[0,180,0] }, { scale:1.55 },
    { translation:[0,-9.25,-0.25], units:"pixels" },
  ],
  right_sickle: [
    { translation:[0.045,0.04,-0.10], units:"blocks" }, { rotation:[92,0,0] }, { rotation:[0,180,0] }, { rotation:[0,0,-14] }, { scale:1.55 },
    { rotation:[-180,0,0] }, { rotation:[0,180,0] }, { scale:0.7 }, { translation:[-0.3,-0.15,0], units:"blocks" },
  ],
  left_baghnakh: [
    { rotation:[-72,0,0] }, { scale:1.55 },
    { translation:[7.25,-2,-0.5], units:"pixels" }, { rotation:[0,0,90] },
  ],
  left_sickle: [
    { translation:[-0.045,0.04,-0.10], units:"blocks" }, { rotation:[92,0,0] }, { rotation:[0,0,14] }, { scale:1.55 },
    { rotation:[-180,0,0] }, { rotation:[0,180,0] }, { scale:0.7 }, { translation:[-0.3,-0.15,0], units:"blocks" },
  ],
};

function readModel() {
  return JSON.parse(fs.readFileSync(MODEL_PATH, "utf8"));
}

function outlinerNode(nodes, uuid) {
  for (const node of nodes ?? []) {
    if (typeof node !== "object") continue;
    if (node.uuid === uuid) return node;
    const nested = outlinerNode(node.children, uuid);
    if (nested) return nested;
  }
  return null;
}

test("Evening Star embeds toggleable weapon reference layers beneath both elbows", () => {
  const model = readModel();
  const groups = new Map(model.groups.map(group => [group.name, group]));
  const rightFolder = groups.get("[REF] RIGHT-HAND LIVING WEAPONS");
  const leftFolder = groups.get("[REF] LEFT-HAND LIVING WEAPONS");
  assert.ok(rightFolder, "missing right-hand reference folder");
  assert.ok(leftFolder, "missing left-hand reference folder");

  const rightElbow = groups.get("rElbow");
  const leftElbow = groups.get("rElbow2");
  assert.ok(outlinerNode(model.outliner, rightElbow.uuid).children.some(node => node.uuid === rightFolder.uuid));
  assert.ok(outlinerNode(model.outliner, leftElbow.uuid).children.some(node => node.uuid === leftFolder.uuid));

  for (const [side, names] of [["RIGHT", RIGHT_LAYERS], ["LEFT", LEFT_LAYERS]]) {
    for (const name of names) {
      const group = groups.get(`[REF] ${side} ${name}`);
      assert.ok(group, `missing ${side.toLowerCase()} ${name} layer`);
      assert.equal(group.export, false, `${group.name} must never enter Java model export`);
      assert.equal(group.visibility, false, `${group.name} must start hidden for easy one-at-a-time toggling`);
      const node = outlinerNode(model.outliner, group.uuid);
      assert.ok(node.children.length > 0, `${group.name} must contain authored weapon geometry`);
    }
  }

  assert.ok(model.textures.length >= 12, "weapon references must bring their authored textures into the project");
  assert.ok(model.elements.filter(element => element.name?.startsWith("[REF]")).length >= 250,
    "weapon references must use full weapon models rather than placeholder cubes");
});

test("weapon reference injection preserves every authored Vesper animation", () => {
  const model = readModel();
  assert.equal(model.animations.length, 58);
	assert.ok(model.animations.some(animation =>
		animation.name === "animation.VesperTheEveningStarModel.hit"));
  assert.ok(model.animations.some(animation =>
    animation.name === "animation.VesperTheEveningStarModel.sanguine_crescents_alternate"));
});

test("every reference reproduces the ordered Java grip, item-display, and renderer transforms", () => {
  const model = readModel();
  const actual = new Map(model.groups
    .filter(group => group.vesper_reference_transform)
    .map(group => [group.vesper_reference_transform.key, group.vesper_reference_transform.steps]));
  for (const [key, steps] of Object.entries(EXPECTED_TRANSFORMS)) {
    assert.deepEqual(actual.get(key), steps, `${key} must retain its complete ordered runtime transform chain`);
  }
});

test("runtime transforms collapse around the hand anchor instead of cascading translated pivots", () => {
  const model = readModel();
  const groups = new Map(model.groups.map(group => [group.uuid, group]));
  for (const layer of model.groups.filter(group => group.vesper_reference_transform)) {
    const node = outlinerNode(model.outliner, layer.uuid);
    let cursor = node;
    for (const step of layer.vesper_reference_transform.steps.filter(step => step.rotation)) {
      cursor = cursor.children.map(child => groups.get(child.uuid)).find(group => group?.name.includes(" XFORM "))
        ? cursor.children.find(child => groups.get(child.uuid)?.name.includes(" XFORM ")) : null;
      assert.ok(cursor, `${layer.name} is missing an editable rotation group`);
      assert.deepEqual(groups.get(cursor.uuid).origin, layer.origin,
        `${layer.name} rotations must share the hand anchor rather than rotate translated pivots`);
    }
    assert.ok(model.groups.some(group => group.name === `[REF] POSITION ${layer.vesper_reference_transform.key}`),
      `${layer.name} is missing its editable consolidated position group`);
  }
});

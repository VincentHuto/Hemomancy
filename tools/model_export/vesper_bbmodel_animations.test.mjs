import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_DIR = path.join(ROOT,
  "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses");
const CROWNED_ANIMATIONS_JAVA = path.join(ROOT,
  "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame/VesperTheCrownedRefusalAnimations.java");

const EVENING_ACTIONS = [
  "ichimonji", "crosscut", "leaping_cleave", "reaper_sweep", "sky_lance",
  "lance_flurry", "twin_rend", "predator_pounce", "conductive_volley",
  "storm_lock", "branding_thrusts", "updraft_impalement", "chain_sweep",
  "hook_and_crush", "magnetic_axis", "iron_retort", "sickle_cyclone",
  "sickle_pounce", "sickle_cross_rend", "sickle_hook", "sanguine_crescents",
];
const ALTERNATING_ACTIONS = [
  "ichimonji", "crosscut", "leaping_cleave", "reaper_sweep", "sky_lance", "lance_flurry",
  "twin_rend", "predator_pounce", "branding_thrusts", "updraft_impalement",
  "chain_sweep", "hook_and_crush", "sickle_cyclone", "sickle_cross_rend",
  "sanguine_crescents",
];
const TENDENCIES = [
  "animus", "mortem", "lux", "tenebris", "ductilis", "flammeus", "congeatio", "ferric",
];

function readModel(name) {
  return JSON.parse(fs.readFileSync(path.join(MODEL_DIR, name), "utf8"));
}

function expected(prefix, names) {
  return names.map(name => `animation.${prefix}.${name}`);
}

function assertAnimationSchema(model, requiredNames) {
  const actual = new Set((model.animations ?? []).map(animation => animation.name));
  assert.deepEqual([...requiredNames].filter(name => !actual.has(name)), [],
    "all Java pose states must be present as editable Blockbench animations");

  const groups = new Map(model.groups.map(group => [group.uuid, group.name]));
  for (const animation of model.animations) {
    assert.equal(animation.loop === "loop" || animation.loop === "once", true,
      `${animation.name} must declare a Blockbench loop mode`);
    assert.equal(animation.snapping, 24, `${animation.name} must follow the Abhorrent Thought 24 fps layout`);
    assert.ok(animation.length > 0, `${animation.name} must have a positive duration`);
		for (const [boneUuid, animator] of Object.entries(animation.animators ?? {})) {
			if (!animator.keyframes?.length) continue;
			assert.equal(groups.get(boneUuid), animator.name,
        `${animation.name} animator must use a real UUID/name bone binding`);
      assert.equal(animator.type, "bone");
      assert.ok(animator.keyframes.length >= 2, `${animation.name}/${animator.name} needs editable keyframes`);
      for (const keyframe of animator.keyframes) {
        assert.match(keyframe.channel, /^(position|rotation|scale)$/);
				assert.match(keyframe.interpolation, /^(catmullrom|linear)$/);
        assert.equal(keyframe.data_points.length, 1);
        assert.ok(Number.isFinite(Number(keyframe.data_points[0].x)));
        assert.ok(Number.isFinite(Number(keyframe.data_points[0].y)));
        assert.ok(Number.isFinite(Number(keyframe.data_points[0].z)));
      }
    }
  }
}

function animation(model, name) {
  const result = model.animations.find(candidate => candidate.name === name);
  assert.ok(result, `missing animation ${name}`);
  return result;
}

test("Evening Star contains every Java idle, stance, attack, and defeat animation", () => {
  const model = readModel("VesperTheEveningStarModel.bbmodel");
  const names = ["idle", "walk", "rage_idle", "hit", "defeat"]
    .concat(TENDENCIES.map(name => `idle_${name}`))
    .concat(TENDENCIES.map(name => `stance_${name}`))
    .concat(EVENING_ACTIONS)
    .concat(ALTERNATING_ACTIONS.map(name => `${name}_alternate`));
  assertAnimationSchema(model, expected("VesperTheEveningStarModel", names));
	for (const name of names) {
		const clip = animation(model, `animation.VesperTheEveningStarModel.${name}`);
		const keyframeCount = Object.values(clip.animators ?? {})
			.reduce((count, animator) => count + (animator.keyframes?.length ?? 0), 0);
		assert.ok(keyframeCount > 0, `${clip.name} must contain playable keyframes`);
	}
});

test("Crowned Refusal contains every Java phase-one and transition animation", () => {
  const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
  const names = [
    "idle", "walk", "vulnerable", "royal_scuttle", "pincer_vice",
    "stinger_script", "brood_trample", "puppet_muster", "transformation",
  ];
  assertAnimationSchema(model, expected("VesperTheCrownedRefusalModel", names));
});

test("authored Blockbench animations retain real editable keyframes", () => {
  const evening = readModel("VesperTheEveningStarModel.bbmodel");
  const crowned = readModel("VesperTheCrownedRefusalModel.bbmodel");
	const authoredCount = model => model.animations.filter(candidate =>
		Object.values(candidate.animators ?? {}).some(animator => animator.keyframes?.length)).length;
	assert.equal(authoredCount(evening), 57);
	assert.equal(authoredCount(crowned), 9);
	const hit = animation(evening, "animation.VesperTheEveningStarModel.hit");
	const hitBones = new Set(Object.values(hit.animators ?? {})
		.filter(animator => animator.keyframes?.length)
		.map(animator => animator.name));
	for (const bone of ["whole", "body", "head", "rightArm", "leftArm", "ClothBack"])
		assert.ok(hitBones.has(bone), `Evening Star hit reaction must keyframe ${bone}`);
	assert.ok(animation(evening, "animation.VesperTheEveningStarModel.defeat"));
	assert.ok(animation(crowned, "animation.VesperTheCrownedRefusalModel.transformation"));
});

test("Crowned Refusal Java retains the complete authored transformation ending", () => {
	const source = fs.readFileSync(CROWNED_ANIMATIONS_JAVA, "utf8");
	const start = source.indexOf("public static final AnimationDefinition TRANSFORMATION");
	const end = source.indexOf("\n\t\t.build();", start);
	assert.ok(start >= 0 && end > start, "missing Crowned Refusal transformation definition");
	const transformation = source.slice(start, end);

	assert.equal((transformation.match(/new Keyframe\(/g) ?? []).length, 221,
		"Java must retain every authored transformation keyframe from the bbmodel");
	assert.match(transformation,
		/new Keyframe\(5\.0833F, KeyframeAnimations\.degreeVec\(0\.0F, -180\.0F, 0\.0F\)/,
		"the rider must hold its half-turn before the final reveal");
	assert.match(transformation,
		/new Keyframe\(6\.0F, KeyframeAnimations\.posVec\(0\.0F, -43\.0F, 4\.0F\)/,
		"the rider must finish at the authored reveal position");
});

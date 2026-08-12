import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";

import { generateAnimationClass } from "./bbmodel_to_mojang_animations.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_DIR = path.join(ROOT,
  "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses");
const CROWNED_ANIMATIONS_JAVA = path.join(ROOT,
  "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame/VesperTheCrownedRefusalAnimations.java");
const CROWNED_MODEL_JAVA = path.join(ROOT,
  "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame/VesperTheCrownedRefusalModel.java");
const CROWNED_PACKAGE = "com.vincenthuto.hemomancy.client.model.entity.boss.endgame";
const CROWNED_CLASS = "VesperTheCrownedRefusalAnimations";
const EVENING_ANIMATIONS_JAVA = path.join(ROOT,
	"src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame/VesperTheEveningStarAnimations.java");
const EVENING_PACKAGE = "com.vincenthuto.hemomancy.client.model.entity.boss.endgame";
const EVENING_CLASS = "VesperTheEveningStarAnimations";

const CROWNED_LEG_CHAINS = [
	["fLeg", "fLHip2", "fLFemur2", "fLTibia2", "fLTibia4", "flFoot2"],
	["fLeg2", "fLHip3", "fLFemur3", "fLTibia3", "fLTibia5", "flFoot3"],
	["fLeg3", "fLHip4", "fLFemur4", "fLTibia6", "fLTibia7", "flFoot4"],
	["fLeg4", "fLHip5", "fLFemur5", "fLTibia8", "fLTibia9", "flFoot5"],
	["fLeg5", "fLHip6", "fLFemur6", "fLTibia10", "fLTibia11", "flFoot6"],
	["fLeg6", "fLHip7", "fLFemur7", "fLTibia12", "fLTibia13", "flFoot7"],
];
const CROWNED_LOCOMOTION_CLIPS = [
	"walk", "royal_scuttle", "pincer_vice", "stinger_script", "brood_trample", "puppet_muster",
];

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

function animatedBones(clip) {
	return new Map(Object.values(clip.animators ?? {}).map(animator => [animator.name, animator]));
}

function channel(animator, name) {
	return (animator?.keyframes ?? []).filter(keyframe => keyframe.channel === name);
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

test("Evening Star authors the hood removal and keeps the revealed head silhouette in the model", () => {
	const model = readModel("VesperTheEveningStarModel.bbmodel");
	const removeHood = animation(model, "animation.VesperTheEveningStarModel.remove_hood");
	assert.equal(removeHood.loop, "once");
	assert.equal(removeHood.length, 1.5, "30 Minecraft ticks while retaining 24 fps authoring snaps");
	const animated = animatedBones(removeHood);
	for (const bone of ["hood", "hood2", "hood3", "hood4", "head", "hair", "ClothBack"])
		assert.ok(animated.get(bone)?.keyframes?.length >= 2, `remove_hood must author ${bone}`);
	assert.ok(model.groups.some(group => group.name === "hair"), "revealed hair must remain editable geometry");
});

test("Evening Star Tenebris idle and stance connect the spine through planted feet", () => {
	const model = readModel("VesperTheEveningStarModel.bbmodel");
	for (const name of ["idle_tenebris", "stance_tenebris"]) {
		const animated = animatedBones(animation(model, `animation.VesperTheEveningStarModel.${name}`));
		for (const bone of ["body", "leftLeg", "leftLeg2", "leftBoot", "rightLeg", "rightLeg2", "rightBoot"])
			assert.ok(channel(animated.get(bone), "rotation").length >= 3,
				`${name} must counter-pose ${bone}`);
		for (const foot of ["leftBoot", "rightBoot"])
			assert.ok(channel(animated.get(foot), "position").length >= 2,
				`${name} must author a planted ${foot} contact`);
	}
});

test("Evening Star Tenebris attacks author a connected lower body and neutral recovery", () => {
	const model = readModel("VesperTheEveningStarModel.bbmodel");
	for (const name of ["twin_rend", "twin_rend_alternate", "predator_pounce", "predator_pounce_alternate"]) {
		const clip = animation(model, `animation.VesperTheEveningStarModel.${name}`);
		const animated = animatedBones(clip);
		for (const bone of ["body", "leftLeg", "leftLeg2", "leftBoot", "rightLeg", "rightLeg2", "rightBoot"]) {
			const rotations = channel(animated.get(bone), "rotation");
			assert.ok(rotations.length >= 3, `${name} must connect ${bone}`);
			assert.deepEqual(rotations.at(-1).data_points[0], { x: "0", y: "0", z: "0" },
				`${name}/${bone} must recover to neutral`);
		}
	}
});

test("Evening Star Mojang definitions exactly reproduce the editable Blockbench project", () => {
	const model = readModel("VesperTheEveningStarModel.bbmodel");
	const generated = generateAnimationClass({
		packageName: EVENING_PACKAGE,
		className: EVENING_CLASS,
		animations: model.animations,
	});
	assert.equal(fs.readFileSync(EVENING_ANIMATIONS_JAVA, "utf8").replaceAll("\r\n", "\n"), generated,
		"regenerate the Evening Star Mojang definitions after editing the bbmodel source");
});

test("Crowned Refusal contains every Java phase-one and transition animation", () => {
  const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
	const names = [
    "idle", "walk", "vulnerable", "royal_scuttle", "pincer_vice",
    "stinger_script", "brood_trample", "puppet_muster", "transformation",
		"carapace_aneurysm", "grab_impalement", "wing_growth", "takeoff", "flight",
		"dive", "tail_fusillade", "landing", "folded_wing_idle", "folded_wing_walk",
  ];
  assertAnimationSchema(model, expected("VesperTheCrownedRefusalModel", names));
});

test("Crowned Refusal authors readable articulated wings and every flight pose", () => {
	const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
	const wingBones = ["leftWing", "leftWingTip", "leftWingOuter", "rightWing", "rightWingTip", "rightWingOuter"];
	for (const bone of wingBones) {
		const group = model.groups.find(candidate => candidate.name === bone);
		assert.ok(group, `missing editable ${bone} geometry`);
	}
	const leftGeometry = model.elements.filter(element => element.name.startsWith("left_wing_"));
	const rightGeometry = model.elements.filter(element => element.name.startsWith("right_wing_"));
	assert.ok(Math.min(...leftGeometry.map(element => element.from[0])) <= -90,
		"left wing must reach approximately twice its former span");
	assert.ok(Math.max(...rightGeometry.map(element => element.to[0])) >= 93,
		"right wing must reach approximately twice its former span");
	assert.ok(Math.max(...leftGeometry.map(element => element.to[2])) >= 30,
		"wing membranes must be substantially wider front-to-back");
	const runtime = fs.readFileSync(CROWNED_MODEL_JAVA, "utf8");
	for (const bone of wingBones)
		assert.match(runtime, new RegExp(`getChild\\(\"${bone}\"\\)`), `runtime export must bake ${bone}`);
	assert.match(runtime, /PartPose\.offsetAndRotation\(-9\.75F, -7\.0F, -0\.25F, -0\.1396F, 0\.2094F, -1\.1868F\)/,
		"left wing rest pose must negate Blockbench X/Y rotations for Mojang coordinates");
	assert.match(runtime, /PartPose\.offsetAndRotation\(9\.75F, -7\.0F, -0\.25F, -0\.1396F, -0\.2094F, 1\.1868F\)/,
		"right wing rest pose must negate Blockbench X/Y rotations for Mojang coordinates");
	assert.match(runtime, /leftWing\.visible = rightWing\.visible = entity\.hasWingsGrown\(\)/,
		"ungrown wings must stay hidden in the runtime model");
	for (const name of ["wing_growth", "takeoff", "flight", "dive", "tail_fusillade", "landing", "folded_wing_idle", "folded_wing_walk"]) {
		const clip = animatedBones(animation(model, `animation.VesperTheCrownedRefusalModel.${name}`));
		for (const bone of wingBones)
			assert.ok(channel(clip.get(bone), "rotation").length >= 2, `${name} must keyframe ${bone}`);
	}
	const fusillade = animatedBones(animation(model, "animation.VesperTheCrownedRefusalModel.tail_fusillade"));
	for (const bone of ["tail", "tail2", "tail3", "tail4", "tail5"])
		assert.ok(channel(fusillade.get(bone), "rotation").length >= 3, `fusillade must curve ${bone}`);
});

test("Crowned Refusal mount arsenal authors detachable scute waves and every grab socket", () => {
	const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
	const aneurysm = animatedBones(animation(model, "animation.VesperTheCrownedRefusalModel.carapace_aneurysm"));
	for (const bone of ["scutes_front", "scutes_mid", "scutes_rear"]) {
		assert.ok(channel(aneurysm.get(bone), "position").length >= 5, `${bone} needs eruption and reform keys`);
		assert.ok(channel(aneurysm.get(bone), "rotation").length >= 4, `${bone} needs an authored flare`);
	}
	const grab = animatedBones(animation(model, "animation.VesperTheCrownedRefusalModel.grab_impalement"));
	for (const bone of ["fLeftArm", "fRightArm", "upperJaw", "lowerJaw", "tail", "tail2", "tail3", "tail4", "tail5",
			"grab_socket", "bite_socket", "impale_socket"])
		assert.ok(grab.get(bone)?.keyframes?.length >= 2, `grab_impalement must author ${bone}`);
});

test("Crowned Refusal vulnerable clip keeps the rider seated and the mount braced", () => {
	const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
	const clip = animation(model, "animation.VesperTheCrownedRefusalModel.vulnerable");
	const animated = new Map(Object.values(clip.animators ?? {}).map(animator => [animator.name, animator]));
	const rider = animated.get("vesper").keyframes;
	const positions = rider.filter(keyframe => keyframe.channel === "position");
	const rotations = rider.filter(keyframe => keyframe.channel === "rotation");
	assert.ok(positions.length >= 2);
	for (const keyframe of positions) {
		assert.deepEqual(keyframe.data_points[0], { x: "0", y: "-7", z: "0" },
			"the rider pelvis must stay on its authored throne contact point");
	}
	for (const keyframe of rotations) {
		assert.equal(Number(keyframe.data_points[0].y), 0, "the vulnerable rider must not root-yaw");
		assert.equal(Number(keyframe.data_points[0].z), 0, "the vulnerable rider must not drift laterally");
	}
	for (const bone of ["body", "head2", "leftArm", "rightArm", "lowerBody"])
		assert.ok(animated.get(bone)?.keyframes?.length, `${bone} must have an authored vulnerable brace channel`);
	for (const bone of ["fLeftArm", "fRightArm"]) {
		const values = animated.get(bone).keyframes.map(keyframe => JSON.stringify(keyframe.data_points[0]));
		assert.equal(new Set(values).size, 1, `${bone} must stay planted instead of cycling like a walk`);
	}
});

test("Crowned Refusal locomotion authors the complete six-leg gait and carried upper bodies", () => {
	const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
	for (const name of CROWNED_LOCOMOTION_CLIPS) {
		const clip = animation(model, `animation.VesperTheCrownedRefusalModel.${name}`);
		const animated = animatedBones(clip);
		for (const bone of ["lowerBody", "throne", "vesper", "tail", "tail2", "tail3", "tail4", "tail5"])
			assert.ok(channel(animated.get(bone), "rotation").length >= 3,
				`${name} must author ${bone} locomotion/recovery`);
		for (const chain of CROWNED_LEG_CHAINS) {
			for (const bone of chain)
				assert.ok(channel(animated.get(bone), "rotation").length >= 3,
					`${name} must author the complete ${bone} gait chain`);
		}
	}
});

test("Crowned Refusal walk uses alternating tetrapod phases and lifted foot recovery", () => {
	const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
	const clip = animation(model, "animation.VesperTheCrownedRefusalModel.walk");
	const animated = animatedBones(clip);
	const xAtStart = bone => Number(channel(animated.get(bone), "rotation")[0].data_points[0].x);
	assert.equal(xAtStart("fLeg"), xAtStart("fLeg6"), "front-left and rear-right support together");
	assert.equal(xAtStart("fLeg3"), xAtStart("fLeg4"), "rear-left and front-right support together");
	assert.equal(Math.sign(xAtStart("fLeg")), -Math.sign(xAtStart("fLeg3")),
		"opposing diagonal supports must alternate");
	for (const foot of ["flFoot2", "flFoot3", "flFoot4", "flFoot5", "flFoot6", "flFoot7"]) {
		const position = channel(animated.get(foot), "position");
		assert.ok(position.length >= 5, `${foot} must author a planted and lifted trajectory`);
		assert.ok(position.some(keyframe => Number(keyframe.data_points[0].y) < 0),
			`${foot} must lift before advancing`);
	}
	const lowerBodyPosition = channel(animated.get("lowerBody"), "position");
	assert.ok(lowerBodyPosition.some(keyframe => Math.abs(Number(keyframe.data_points[0].x)) > 0),
		"the mount body must shift toward its supporting side");
	assert.ok(lowerBodyPosition.some(keyframe => Number(keyframe.data_points[0].y) > 0),
		"the mount body must compress on weight acceptance");
	const tailStarts = ["tail", "tail2", "tail3", "tail4", "tail5"]
		.map(bone => JSON.stringify(channel(animated.get(bone), "rotation")[1].data_points[0]));
	assert.equal(new Set(tailStarts).size, tailStarts.length,
		"tail counterbalance must propagate from the base instead of rotating every segment equally");
});

test("Crowned Refusal attacks recover feet tail throne and rider to the neutral pose", () => {
	const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
	for (const name of CROWNED_LOCOMOTION_CLIPS.filter(candidate => candidate !== "walk")) {
		const clip = animation(model, `animation.VesperTheCrownedRefusalModel.${name}`);
		const animated = animatedBones(clip);
		for (const bone of ["lowerBody", "throne", "vesper", "tail", "tail2", "tail3", "tail4", "tail5"]
			.concat(CROWNED_LEG_CHAINS.flat())) {
			for (const keyframe of (animated.get(bone)?.keyframes ?? []).filter(key => Number(key.time) === clip.length))
				assert.deepEqual(keyframe.data_points[0], { x: "0", y: "0", z: "0" },
					`${name}/${bone} must finish its locomotion recovery at neutral`);
		}
	}
});

test("Crowned Refusal Mojang definitions exactly reproduce the editable Blockbench project", () => {
	const model = readModel("VesperTheCrownedRefusalModel.bbmodel");
	const generated = generateAnimationClass({
		packageName: CROWNED_PACKAGE,
		className: CROWNED_CLASS,
		animations: model.animations,
	});
	assert.equal(fs.readFileSync(CROWNED_ANIMATIONS_JAVA, "utf8").replaceAll("\r\n", "\n"), generated,
		"regenerate the Mojang AnimationDefinition export after editing the bbmodel source");
});

test("Crowned Refusal model resets before authored selection and has no procedural leg overlay", () => {
	const source = fs.readFileSync(CROWNED_MODEL_JAVA, "utf8");
	const setupStart = source.indexOf("public void setupAnim(");
	const setupEnd = source.indexOf("\n\t@Override", setupStart);
	const setup = source.slice(setupStart, setupEnd);
	assert.ok(setup.indexOf("whole.getAllParts().forEach(ModelPart::resetPose);") <
		setup.indexOf("AnimationDefinition animation = switch"), "pose reset must precede clip selection");
	assert.doesNotMatch(setup, /animateLeg\s*\(/,
		"authored leg channels must not receive a second procedural walk pass");
});

test("authored Blockbench animations retain real editable keyframes", () => {
  const evening = readModel("VesperTheEveningStarModel.bbmodel");
  const crowned = readModel("VesperTheCrownedRefusalModel.bbmodel");
	const authoredCount = model => model.animations.filter(candidate =>
		Object.values(candidate.animators ?? {}).some(animator => animator.keyframes?.length)).length;
	assert.equal(authoredCount(evening), 58);
	assert.equal(authoredCount(crowned), 19);
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
	const start = source.indexOf("private static AnimationDefinition transformation()");
	const end = source.indexOf(".build();", start);
	assert.ok(start >= 0 && end > start, "missing Crowned Refusal transformation definition");
	const transformation = source.slice(start, end);

	assert.equal((transformation.match(/new Keyframe\(/g) ?? []).length, 286,
		"Java must retain every authored transformation keyframe from the bbmodel");
	assert.match(transformation,
		/new Keyframe\(2\.375F, KeyframeAnimations\.degreeVec\(0\.0F, -180\.0F, 0\.0F\)/,
		"the rider must retain the authored early half-turn settle keyframe");
	assert.match(transformation,
		/new Keyframe\(3\.95833F, KeyframeAnimations\.degreeVec\(0\.0F, -180\.0F, 0\.0F\)/,
		"the rider must hold its half-turn before the final reveal");
	assert.match(transformation,
		/new Keyframe\(2\.58333F, KeyframeAnimations\.degreeVec\(0\.0F, -180\.0F, 0\.0F\)/,
		"the transformation must retain the authored half-turn settle keyframes");
	assert.match(transformation,
		/new Keyframe\(6\.0F, KeyframeAnimations\.posVec\(0\.0F, -43\.0F, -14\.0F\)/,
		"the updated export must retain the final authored rider position");
	for (const bone of ["lShoulder", "lElbow", "rShoulder", "rElbow"])
		assert.match(transformation, new RegExp(`\\.addAnimation\\("${bone}"`),
			`the transformation must retain the authored ${bone} channel`);
});

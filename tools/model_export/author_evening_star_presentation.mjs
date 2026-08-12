import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL = path.join(ROOT,
	"src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/VesperTheEveningStarModel.bbmodel");
const model = JSON.parse(fs.readFileSync(MODEL, "utf8"));
const group = name => {
	const result = model.groups.find(candidate => candidate.name === name && candidate.export !== false);
	if (!result) throw new Error(`Missing authored group ${name}`);
	return result;
};

let sequence = 0;
const uuid = prefix => `${prefix}-${String(++sequence).padStart(4, "0")}-4000-8000-000000000000`;
const point = values => ({ x: String(values[0]), y: String(values[1]), z: String(values[2]) });
const keys = (channel, times, values) => times.map((time, index) => ({
	channel,
	data_points: [point(values[index])],
	uuid: uuid("e5a70000"),
	time,
	color: -1,
	interpolation: "catmullrom",
}));
const animator = (name, channels) => ({
	name,
	type: "bone",
	rotation_global: false,
	quaternion_interpolation: false,
	keyframes: channels.flat(),
});

function setAnimator(animation, name, channels) {
	animation.animators ??= {};
	animation.animators[group(name).uuid] = animator(name, channels);
}

function animation(name) {
	const result = model.animations.find(candidate => candidate.name.endsWith(`.${name}`));
	if (!result) throw new Error(`Missing animation ${name}`);
	return result;
}

function authorTenebris(name, times, poses) {
	const clip = animation(name);
	for (const bone of ["body", "leftLeg", "leftLeg2", "leftBoot", "rightLeg", "rightLeg2", "rightBoot"]) {
		setAnimator(clip, bone, [
			keys("rotation", times, poses[bone]),
			...(bone.endsWith("Boot") ? [keys("position", times, times.map(() => [0, 0, 0]))] : []),
		]);
	}
}

authorTenebris("idle_tenebris", [0, 0.65, 1.3, 1.95, 2.6], {
	body: [[8, 0, 0], [9, -1, 0], [8, 0, 0], [7, 1, 0], [8, 0, 0]],
	leftLeg: [[-10, -4, -2], [-9, -4, -2], [-10, -4, -2], [-11, -4, -2], [-10, -4, -2]],
	leftLeg2: [[16, 0, 0], [15, 0, 0], [16, 0, 0], [17, 0, 0], [16, 0, 0]],
	leftBoot: [[-6, 0, 2], [-6, 0, 2], [-6, 0, 2], [-6, 0, 2], [-6, 0, 2]],
	rightLeg: [[-14, 4, 2], [-15, 4, 2], [-14, 4, 2], [-13, 4, 2], [-14, 4, 2]],
	rightLeg2: [[20, 0, 0], [21, 0, 0], [20, 0, 0], [19, 0, 0], [20, 0, 0]],
	rightBoot: [[-6, 0, -2], [-6, 0, -2], [-6, 0, -2], [-6, 0, -2], [-6, 0, -2]],
});

authorTenebris("stance_tenebris", [0, 0.3, 0.75, 1.5], {
	body: [[0, 0, 0], [5, 0, 0], [10, 0, 0], [12, 0, 0]],
	leftLeg: [[0, 0, 0], [-5, -2, -1], [-10, -4, -2], [-12, -5, -2]],
	leftLeg2: [[0, 0, 0], [8, 0, 0], [16, 0, 0], [19, 0, 0]],
	leftBoot: [[0, 0, 0], [-3, 0, 1], [-6, 0, 2], [-7, 0, 2]],
	rightLeg: [[0, 0, 0], [-7, 2, 1], [-14, 4, 2], [-16, 5, 2]],
	rightLeg2: [[0, 0, 0], [10, 0, 0], [20, 0, 0], [23, 0, 0]],
	rightBoot: [[0, 0, 0], [-3, 0, -1], [-6, 0, -2], [-7, 0, -2]],
});

for (const name of ["twin_rend", "twin_rend_alternate", "predator_pounce", "predator_pounce_alternate"]) {
	const clip = animation(name);
	const times = [0, clip.length * 0.32, clip.length * 0.68, clip.length];
	const side = name.endsWith("alternate") ? -1 : 1;
	const pounce = name.startsWith("predator");
	const poses = {
		body: [[0,0,0], [pounce ? 24 : 14, side * 5, 0], [pounce ? 32 : 18, side * -4, 0], [0,0,0]],
		leftLeg: [[0,0,0], [pounce ? -28 : -14, -side * 4, -2], [pounce ? 18 : -8, side * 3, 2], [0,0,0]],
		leftLeg2: [[0,0,0], [pounce ? 42 : 22, 0, 0], [pounce ? 12 : 14, 0, 0], [0,0,0]],
		leftBoot: [[0,0,0], [pounce ? -14 : -7, 0, 2], [pounce ? -5 : -4, 0, 1], [0,0,0]],
		rightLeg: [[0,0,0], [pounce ? -22 : -16, side * 4, 2], [pounce ? 24 : -10, -side * 3, -2], [0,0,0]],
		rightLeg2: [[0,0,0], [pounce ? 36 : 25, 0, 0], [pounce ? 10 : 16, 0, 0], [0,0,0]],
		rightBoot: [[0,0,0], [pounce ? -12 : -7, 0, -2], [pounce ? -4 : -4, 0, -1], [0,0,0]],
	};
	for (const [bone, values] of Object.entries(poses)) setAnimator(clip, bone, [keys("rotation", times, values)]);
}

const hairGroupId = "e5a71000-0000-4000-8000-000000000001";
const hairCapId = "e5a71000-0000-4000-8000-000000000002";
const hairFringeId = "e5a71000-0000-4000-8000-000000000003";
const hairFaces = {
	north: { uv: [65, 50, 78, 55], texture: 0 }, east: { uv: [51, 50, 63, 55], texture: 0 },
	south: { uv: [79, 50, 82, 55], texture: 0 }, west: { uv: [63, 50, 65, 55], texture: 0 },
	up: { uv: [65, 50, 53, 48], texture: 0 }, down: { uv: [77, 48, 65, 50], texture: 0 },
};
if (!model.groups.some(candidate => candidate.uuid === hairGroupId)) {
	model.groups.push({
		...group("head"),
		name: "hair",
		uuid: hairGroupId,
		color: 6,
		children: [],
	});
	const cube = (name, id, from, to) => ({
		name, box_uv: true, render_order: "default", locked: false, export: true, scope: 0,
		allow_mirror_modeling: true, from, to, autouv: 0, color: 6,
		origin: group("head").origin, uv_offset: [51, 48], faces: hairFaces, type: "cube", uuid: id,
	});
	model.elements.push(cube("revealed_hair_cap", hairCapId, [-6.25, 50.15, -5.5], [6.25, 52.75, 7.0]));
	model.elements.push(cube("revealed_hair_fringe", hairFringeId, [-6.3, 47.85, -5.55], [6.3, 52.65, -4.85]));
	const findNode = (nodes, id) => {
		for (const node of nodes) {
			if (typeof node === "object" && node.uuid === id) return node;
			if (typeof node === "object") {
				const found = findNode(node.children ?? [], id);
				if (found) return found;
			}
		}
	};
	const headNode = findNode(model.outliner, group("head").uuid);
	if (!headNode) throw new Error("Missing head outliner node");
	headNode.children.push({ uuid: hairGroupId, isOpen: true, children: [hairCapId, hairFringeId] });
}
for (const id of [hairCapId, hairFringeId]) {
	const element = model.elements.find(candidate => candidate.uuid === id);
	if (element) {
		element.uv_offset = [51, 48];
		element.faces = hairFaces;
	}
}

const removeName = "animation.VesperTheEveningStarModel.remove_hood";
model.animations = model.animations.filter(candidate => candidate.name !== removeName);
const removeHood = {
	uuid: "e5a72000-0000-4000-8000-000000000001",
	name: removeName,
	loop: "once",
	override: false,
	length: 1.5,
	snapping: 24,
	selected: false,
	group_name: "",
	scope: 0,
	anim_time_update: "",
	blend_weight: "",
	start_delay: "",
	loop_delay: "",
	animators: {},
};
const hoodTimes = [0, 0.25, 0.625, 1.0, 1.5];
setAnimator(removeHood, "hood", [
	keys("rotation", hoodTimes, [[0,0,0],[-8,0,4],[-28,0,10],[-58,0,15],[-82,0,18]]),
	keys("position", hoodTimes, [[0,0,0],[0,1,0],[0,4,2],[0,9,5],[0,15,9]]),
]);
setAnimator(removeHood, "hood2", [keys("rotation", hoodTimes, [[0,0,0],[-5,0,0],[-18,0,0],[-32,0,0],[-45,0,0]])]);
setAnimator(removeHood, "hood3", [keys("rotation", hoodTimes, [[0,0,0],[-4,0,0],[-14,0,0],[-26,0,0],[-36,0,0]])]);
setAnimator(removeHood, "hood4", [keys("rotation", hoodTimes, [[0,0,0],[-3,0,0],[-10,0,0],[-18,0,0],[-28,0,0]])]);
setAnimator(removeHood, "head", [keys("rotation", hoodTimes, [[0,0,0],[-8,0,0],[-14,0,0],[5,0,0],[0,0,0]])]);
setAnimator(removeHood, "hair", [keys("scale", hoodTimes, [[0.98,0.98,0.98],[1,1,1],[1,1,1],[1,1,1],[1,1,1]])]);
setAnimator(removeHood, "ClothBack", [keys("rotation", hoodTimes, [[0,0,0],[-5,0,0],[-18,0,0],[9,0,0],[0,0,0]])]);
model.animations.push(removeHood);

fs.writeFileSync(MODEL, JSON.stringify(model));

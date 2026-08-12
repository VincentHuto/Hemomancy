import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_PATH = path.join(ROOT, "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/VesperTheCrownedRefusalModel.bbmodel");
const PREFIX = "animation.VesperTheCrownedRefusalModel.";
const model = JSON.parse(fs.readFileSync(MODEL_PATH, "utf8"));

function uuid(key) {
	const hex = crypto.createHash("sha256").update(`vesper-winged-flight|${key}`).digest("hex").slice(0, 32);
	return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function outlinerNode(items, id) {
	for (const item of items) {
		if (typeof item !== "object") continue;
		if (item.uuid === id) return item;
		const nested = outlinerNode(item.children ?? [], id);
		if (nested) return nested;
	}
}

function group(name, parentName, origin, rotation) {
	let result = model.groups.find(value => value.name === name);
	if (!result) {
		result = {
			name, uuid: uuid(`group|${name}`), export: true, locked: false, scope: 0, selected: false,
			_static: { properties: {}, temp_data: {} }, origin, rotation, color: 1,
			children: [], reset: false, shade: true, mirror_uv: false, visibility: true, autouv: 0,
			isOpen: true, primary_selected: false,
		};
		model.groups.push(result);
		const parent = model.groups.find(value => value.name === parentName);
		outlinerNode(model.outliner, parent.uuid).children.push({ uuid: result.uuid, isOpen: true, children: [] });
	}
	result.origin = origin;
	result.rotation = rotation;
	return result;
}

function cube(name, groupName, from, to, uv) {
	const id = uuid(`cube|${name}`);
	const dimensions = to.map((value, index) => value - from[index]);
	const faces = Object.fromEntries(["north", "east", "south", "west", "up", "down"].map(face => [face, {
		uv: [uv[0], uv[1], uv[0] + Math.max(1, dimensions[0]), uv[1] + Math.max(1, dimensions[2])], texture: 0,
	}]));
	let element = model.elements.find(value => value.uuid === id);
	if (!element) {
		element = {
			name, box_uv: true, render_order: "default", locked: false, export: true, scope: 0,
			allow_mirror_modeling: true, from, to, autouv: 0, color: 1,
			origin: from.map((value, index) => (value + to[index]) / 2), uv_offset: uv,
			faces, type: "cube", uuid: id,
		};
		model.elements.push(element);
		outlinerNode(model.outliner, model.groups.find(value => value.name === groupName).uuid).children.push(id);
	}
	element.from = from;
	element.to = to;
	element.origin = from.map((value, index) => (value + to[index]) / 2);
	element.uv_offset = uv;
	element.faces = faces;
}

group("leftWing", "lowerBody", [-8.5, 38, -1], [8, -12, -68]);
group("leftWingTip", "leftWing", [-42, 38, 0], [0, 14, -28]);
group("leftWingOuter", "leftWingTip", [-69, 38, 1], [0, 10, -16]);
group("rightWing", "lowerBody", [11, 38, -1], [8, 12, 68]);
group("rightWingTip", "rightWing", [45, 38, 0], [0, -14, 28]);
group("rightWingOuter", "rightWingTip", [72, 38, 1], [0, -10, 16]);

cube("left_wing_leading_spar", "leftWing", [-43, 37, -2], [-8, 39, 0], [620, 0]);
cube("left_wing_membrane_front", "leftWing", [-42, 36.4, -1], [-9, 37.2, 16], [620, 28]);
cube("left_wing_membrane_rear", "leftWing", [-39, 36.3, 14], [-9, 37.1, 31], [620, 62]);
cube("left_wing_tip_spar", "leftWingTip", [-70, 37, -1], [-42, 38.6, 1], [620, 0]);
cube("left_wing_tip_membrane", "leftWingTip", [-69, 36.4, 0], [-41, 37.1, 25], [620, 84]);
cube("left_wing_outer_spar", "leftWingOuter", [-90, 37.1, 0], [-69, 38.5, 2], [620, 105]);
cube("left_wing_outer_membrane", "leftWingOuter", [-89, 36.5, 1], [-68, 37.1, 20], [650, 90]);
cube("right_wing_leading_spar", "rightWing", [11, 37, -2], [46, 39, 0], [620, 0]);
cube("right_wing_membrane_front", "rightWing", [12, 36.4, -1], [45, 37.2, 16], [620, 28]);
cube("right_wing_membrane_rear", "rightWing", [12, 36.3, 14], [42, 37.1, 31], [620, 62]);
cube("right_wing_tip_spar", "rightWingTip", [45, 37, -1], [73, 38.6, 1], [620, 0]);
cube("right_wing_tip_membrane", "rightWingTip", [44, 36.4, 0], [72, 37.1, 25], [620, 84]);
cube("right_wing_outer_spar", "rightWingOuter", [72, 37.1, 0], [93, 38.5, 2], [620, 105]);
cube("right_wing_outer_membrane", "rightWingOuter", [71, 36.5, 1], [92, 37.1, 20], [650, 90]);

function point(value) { return { x: String(value[0]), y: String(value[1]), z: String(value[2]) }; }
function key(animationName, bone, channel, time, value, index) {
	return { channel, data_points: [point(value)], uuid: uuid(`${animationName}|${bone}|${channel}|${time}|${index}`), time, color: -1, interpolation: "catmullrom" };
}
function animation(name, length, tracks, loop = "once") {
	const fullName = PREFIX + name;
	const animators = {};
	for (const [bone, channels] of Object.entries(tracks)) {
		const boneGroup = model.groups.find(value => value.name === bone);
		const frames = [];
		for (const [channel, values] of Object.entries(channels)) values.forEach((value, index) =>
			frames.push(key(fullName, bone, channel, value[0], value.slice(1), index)));
		animators[boneGroup.uuid] = { name: bone, type: "bone", rotation_global: false, quaternion_interpolation: false, keyframes: frames };
	}
	const clip = { uuid: uuid(`animation|${name}`), name: fullName, loop, override: false, length, snapping: 24, selected: false, anim_time_update: "", blend_weight: "", start_delay: "", loop_delay: "", animators };
	const prior = model.animations.findIndex(value => value.name === fullName);
	if (prior >= 0) model.animations[prior] = clip; else model.animations.push(clip);
}

const folded = {
	leftWing: [[0,0,0,0],[1.5,1,-2,2],[3,0,0,0]], leftWingTip: [[0,0,0,0],[1.5,0,3,-2],[3,0,0,0]],
	leftWingOuter: [[0,0,0,0],[1.5,0,2,-1],[3,0,0,0]],
	rightWing: [[0,0,0,0],[1.5,1,2,-2],[3,0,0,0]], rightWingTip: [[0,0,0,0],[1.5,0,-3,2],[3,0,0,0]],
	rightWingOuter: [[0,0,0,0],[1.5,0,-2,1],[3,0,0,0]],
};
animation("folded_wing_idle", 3, Object.fromEntries(Object.entries(folded).map(([bone, rotation]) => [bone, { rotation }])), "loop");
animation("folded_wing_walk", 1, {
	leftWing: { rotation: [[0,0,0,0],[0.25,3,-4,5],[0.5,0,0,0],[0.75,-2,4,-4],[1,0,0,0]] },
	leftWingTip: { rotation: [[0,0,0,0],[0.25,0,5,-3],[0.5,0,0,0],[0.75,0,-4,3],[1,0,0,0]] },
	leftWingOuter: { rotation: [[0,0,0,0],[0.25,0,4,-2],[0.5,0,0,0],[0.75,0,-3,2],[1,0,0,0]] },
	rightWing: { rotation: [[0,0,0,0],[0.25,-2,-4,4],[0.5,0,0,0],[0.75,3,4,-5],[1,0,0,0]] },
	rightWingTip: { rotation: [[0,0,0,0],[0.25,0,4,-3],[0.5,0,0,0],[0.75,0,-5,3],[1,0,0,0]] },
	rightWingOuter: { rotation: [[0,0,0,0],[0.25,0,3,-2],[0.5,0,0,0],[0.75,0,-4,2],[1,0,0,0]] },
}, "loop");
animation("wing_growth", 3, {
	leftWing: { scale: [[0,0.02,0.02,0.02],[0.8,0.18,0.18,0.18],[1.8,0.72,0.72,0.72],[3,1,1,1]], rotation: [[0,35,20,-18],[0.8,24,14,-12],[1.8,-12,-8,58],[2.4,8,3,-12],[3,0,0,0]] },
	leftWingTip: { scale: [[0,0.02,0.02,0.02],[1.1,0.12,0.12,0.12],[2.1,0.8,0.8,0.8],[3,1,1,1]], rotation: [[0,18,12,18],[1.2,10,8,12],[2.1,-8,-4,36],[3,0,0,0]] },
	leftWingOuter: { scale: [[0,0.02,0.02,0.02],[1.3,0.08,0.08,0.08],[2.35,0.72,0.72,0.72],[3,1,1,1]], rotation: [[0,14,10,14],[1.3,8,7,10],[2.35,-6,-3,28],[3,0,0,0]] },
	rightWing: { scale: [[0,0.02,0.02,0.02],[0.8,0.18,0.18,0.18],[1.8,0.72,0.72,0.72],[3,1,1,1]], rotation: [[0,35,-20,18],[0.8,24,-14,12],[1.8,-12,8,-58],[2.4,8,-3,12],[3,0,0,0]] },
	rightWingTip: { scale: [[0,0.02,0.02,0.02],[1.1,0.12,0.12,0.12],[2.1,0.8,0.8,0.8],[3,1,1,1]], rotation: [[0,18,-12,-18],[1.2,10,-8,-12],[2.1,-8,4,-36],[3,0,0,0]] },
	rightWingOuter: { scale: [[0,0.02,0.02,0.02],[1.3,0.08,0.08,0.08],[2.35,0.72,0.72,0.72],[3,1,1,1]], rotation: [[0,14,-10,-14],[1.3,8,-7,-10],[2.35,-6,3,-28],[3,0,0,0]] },
});
animation("takeoff", 1, {
	lowerBody: { position: [[0,0,0,0],[0.45,0,2,0],[1,0,-1,0]], rotation: [[0,0,0,0],[0.45,-8,0,0],[1,4,0,0]] },
	leftWing: { rotation: [[0,0,0,0],[0.35,-20,-8,62],[0.65,18,4,-12],[1,-10,-4,48]] }, leftWingTip: { rotation: [[0,0,0,0],[0.35,-8,-4,32],[0.65,12,4,-18],[1,-6,-2,22]] },
	leftWingOuter: { rotation: [[0,0,0,0],[0.35,-5,-3,22],[0.65,9,3,-14],[1,-4,-1,16]] },
	rightWing: { rotation: [[0,0,0,0],[0.35,-20,8,-62],[0.65,18,-4,12],[1,-10,4,-48]] }, rightWingTip: { rotation: [[0,0,0,0],[0.35,-8,4,-32],[0.65,12,-4,18],[1,-6,2,-22]] },
	rightWingOuter: { rotation: [[0,0,0,0],[0.35,-5,3,-22],[0.65,9,-3,14],[1,-4,1,-16]] },
});
animation("flight", 1, {
	lowerBody: { position: [[0,0,0,0],[0.5,0,-0.8,0],[1,0,0,0]], rotation: [[0,3,0,0],[0.5,-3,0,0],[1,3,0,0]] },
	leftWing: { rotation: [[0,-10,-4,48],[0.25,-24,-6,66],[0.55,16,2,-18],[0.8,-8,-2,38],[1,-10,-4,48]] }, leftWingTip: { rotation: [[0,-6,-2,22],[0.25,-10,-3,34],[0.55,14,2,-24],[0.8,-4,-1,16],[1,-6,-2,22]] },
	leftWingOuter: { rotation: [[0,-4,-1,15],[0.25,-7,-2,24],[0.55,10,1,-18],[0.8,-3,-1,11],[1,-4,-1,15]] },
	rightWing: { rotation: [[0,-10,4,-48],[0.25,-24,6,-66],[0.55,16,-2,18],[0.8,-8,2,-38],[1,-10,4,-48]] }, rightWingTip: { rotation: [[0,-6,2,-22],[0.25,-10,3,-34],[0.55,14,-2,24],[0.8,-4,1,-16],[1,-6,2,-22]] },
	rightWingOuter: { rotation: [[0,-4,1,-15],[0.25,-7,2,-24],[0.55,10,-1,18],[0.8,-3,1,-11],[1,-4,1,-15]] },
}, "loop");
animation("dive", 1, {
	lowerBody: { rotation: [[0,0,0,0],[0.55,42,0,0],[1,18,0,0]] },
	leftWing: { rotation: [[0,-8,-4,42],[0.45,18,-12,-24],[1,8,-8,-12]] }, leftWingTip: { rotation: [[0,-4,-2,18],[0.45,12,-8,-16],[1,6,-4,-8]] },
	leftWingOuter: { rotation: [[0,-3,-1,12],[0.45,9,-6,-12],[1,4,-3,-6]] },
	rightWing: { rotation: [[0,-8,4,-42],[0.45,18,12,24],[1,8,8,12]] }, rightWingTip: { rotation: [[0,-4,2,-18],[0.45,12,8,16],[1,6,4,8]] },
	rightWingOuter: { rotation: [[0,-3,1,-12],[0.45,9,6,12],[1,4,3,6]] },
});
animation("tail_fusillade", 1.5, {
	leftWing: { rotation: [[0,-10,-4,46],[0.75,-16,-5,58],[1.5,-10,-4,46]] }, leftWingTip: { rotation: [[0,-5,-2,20],[0.75,-9,-3,30],[1.5,-5,-2,20]] },
	leftWingOuter: { rotation: [[0,-3,-1,14],[0.75,-6,-2,21],[1.5,-3,-1,14]] },
	rightWing: { rotation: [[0,-10,4,-46],[0.75,-16,5,-58],[1.5,-10,4,-46]] }, rightWingTip: { rotation: [[0,-5,2,-20],[0.75,-9,3,-30],[1.5,-5,2,-20]] },
	rightWingOuter: { rotation: [[0,-3,1,-14],[0.75,-6,2,-21],[1.5,-3,1,-14]] },
	tail: { rotation: [[0,0,0,0],[0.55,-32,8,0],[1.15,-38,-8,0],[1.5,0,0,0]] }, tail2: { rotation: [[0,0,0,0],[0.55,-42,-12,0],[1.15,-48,12,0],[1.5,0,0,0]] },
	tail3: { rotation: [[0,0,0,0],[0.55,-52,16,0],[1.15,-58,-16,0],[1.5,0,0,0]] }, tail4: { rotation: [[0,0,0,0],[0.55,-62,-18,0],[1.15,-68,18,0],[1.5,0,0,0]] },
	tail5: { rotation: [[0,0,0,0],[0.55,-74,20,0],[1.15,-80,-20,0],[1.5,0,0,0]] },
});
animation("landing", 1.25, {
	lowerBody: { position: [[0,0,0,0],[0.75,0,2,0],[1.25,0,0,0]], rotation: [[0,10,0,0],[0.75,-8,0,0],[1.25,0,0,0]] },
	leftWing: { rotation: [[0,-12,-4,52],[0.45,20,4,-20],[0.85,-18,-6,62],[1.25,0,0,0]] }, leftWingTip: { rotation: [[0,-6,-2,24],[0.45,14,2,-16],[0.85,-10,-3,34],[1.25,0,0,0]] },
	leftWingOuter: { rotation: [[0,-4,-1,16],[0.45,10,1,-12],[0.85,-7,-2,24],[1.25,0,0,0]] },
	rightWing: { rotation: [[0,-12,4,-52],[0.45,20,-4,20],[0.85,-18,6,-62],[1.25,0,0,0]] }, rightWingTip: { rotation: [[0,-6,2,-24],[0.45,14,-2,16],[0.85,-10,3,-34],[1.25,0,0,0]] },
	rightWingOuter: { rotation: [[0,-4,1,-16],[0.45,10,-1,12],[0.85,-7,2,-24],[1.25,0,0,0]] },
});

const texturePath = path.join(ROOT, "src/main/resources/assets/hemomancy/textures/entity/boss/endgame/vesper_crowned_refusal.png");
if (fs.existsSync(texturePath) && model.textures?.[0]) {
	model.textures[0].source = `data:image/png;base64,${fs.readFileSync(texturePath).toString("base64")}`;
}
fs.writeFileSync(MODEL_PATH, JSON.stringify(model));

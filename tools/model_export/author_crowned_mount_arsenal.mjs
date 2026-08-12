import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_PATH = path.join(ROOT, "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/VesperTheCrownedRefusalModel.bbmodel");
const PREFIX = "animation.VesperTheCrownedRefusalModel.";
const model = JSON.parse(fs.readFileSync(MODEL_PATH, "utf8"));

function uuid(key) {
	const hex = crypto.createHash("sha256").update(`vesper-mount-arsenal|${key}`).digest("hex").slice(0, 32);
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

function group(name, parentName, origin) {
	let result = model.groups.find(value => value.name === name);
	if (!result) {
		result = {
			name, uuid: uuid(`group|${name}`), export: true, locked: false, scope: 0, selected: false,
			_static: { properties: {}, temp_data: {} }, origin, rotation: [0, 0, 0], color: 3,
			children: [], reset: false, shade: true, mirror_uv: false, visibility: true, autouv: 0,
			isOpen: true, primary_selected: false,
		};
		model.groups.push(result);
		const parent = model.groups.find(value => value.name === parentName);
		const parentNode = outlinerNode(model.outliner, parent.uuid);
		parentNode.children.push({ uuid: result.uuid, isOpen: true, children: [] });
	}
	return result;
}

function cube(name, groupName, from, to, uv) {
	const id = uuid(`cube|${name}`);
	const width = to[0] - from[0];
	const height = to[1] - from[1];
	const depth = to[2] - from[2];
	const faces = {
		north: { uv: [uv[0], uv[1], uv[0] + width, uv[1] + height], texture: 0 },
		east: { uv: [uv[0], uv[1], uv[0] + depth, uv[1] + height], texture: 0 },
		south: { uv: [uv[0], uv[1], uv[0] + width, uv[1] + height], texture: 0 },
		west: { uv: [uv[0], uv[1], uv[0] + depth, uv[1] + height], texture: 0 },
		up: { uv: [uv[0], uv[1], uv[0] + width, uv[1] + depth], texture: 0 },
		down: { uv: [uv[0], uv[1], uv[0] + width, uv[1] + depth], texture: 0 },
	};
	let element = model.elements.find(value => value.uuid === id);
	if (!element) {
		element = {
			name, box_uv: true, render_order: "default", locked: false, export: true, scope: 0,
			allow_mirror_modeling: true, from, to, autouv: 0, color: 3,
			origin: from.map((value, index) => (value + to[index]) / 2), uv_offset: uv,
			faces, type: "cube", uuid: id,
		};
		model.elements.push(element);
		const target = group(groupName, "lowerBody", [1.25, 31, -0.75]);
		outlinerNode(model.outliner, target.uuid).children.push(id);
	} else element.faces = faces;
}

for (const [name, origin] of [
	["scutes_front", [1.25, 35, -17]], ["scutes_mid", [1.25, 37, 0]], ["scutes_rear", [1.25, 35, 18]],
	["grab_socket", [1.25, 35, -30]], ["bite_socket", [1.25, 32, -31]], ["impale_socket", [1.25, 38, 42]],
]) group(name, name.includes("impale") ? "tail5" : name.includes("bite") ? "head" : "lowerBody", origin);

cube("scute_front_left", "scutes_front", [-13, 35, -22], [-3, 38, -11], [464, 232]);
cube("scute_front_right", "scutes_front", [5, 35, -22], [15, 38, -11], [464, 232]);
cube("scute_mid_left", "scutes_mid", [-15, 37, -6], [-4, 40, 6], [464, 232]);
cube("scute_mid_right", "scutes_mid", [6, 37, -6], [17, 40, 6], [464, 232]);
cube("scute_rear_left", "scutes_rear", [-13, 35, 12], [-3, 38, 24], [464, 232]);
cube("scute_rear_right", "scutes_rear", [5, 35, 12], [15, 38, 24], [464, 232]);

function point(value) { return { x: String(value[0]), y: String(value[1]), z: String(value[2]) }; }
function key(animation, bone, channel, time, value, index) {
	return { channel, data_points: [point(value)], uuid: uuid(`${animation.name}|${bone}|${channel}|${time}|${index}`), time, color: -1, interpolation: "catmullrom" };
}
function animation(name, length, tracks) {
	const prior = model.animations.findIndex(value => value.name === PREFIX + name);
	const animators = {};
	for (const [bone, channels] of Object.entries(tracks)) {
		const boneGroup = model.groups.find(value => value.name === bone);
		const frames = [];
		for (const [channel, values] of Object.entries(channels)) values.forEach((value, index) => frames.push(key({ name: PREFIX + name }, bone, channel, value[0], value.slice(1), index)));
		animators[boneGroup.uuid] = { name: bone, type: "bone", rotation_global: false, quaternion_interpolation: false, keyframes: frames };
	}
	const clip = { uuid: uuid(`animation|${name}`), name: PREFIX + name, loop: "once", override: false, length, snapping: 24, selected: false, anim_time_update: "", blend_weight: "", start_delay: "", loop_delay: "", animators };
	if (prior >= 0) model.animations[prior] = clip; else model.animations.push(clip);
}

animation("carapace_aneurysm", 6, {
	lowerBody: { position: [[0,0,0,0],[0.7,0,2.2,1],[1.2,0,0.7,0],[4.25,0,0.7,0],[5.25,0,1.2,0],[6,0,0,0]], rotation: [[0,0,0,0],[0.7,8,0,0],[1.2,-4,0,0],[4.25,-4,0,0],[5.25,2,0,0],[6,0,0,0]] },
	scutes_front: { position: [[0,0,0,0],[0.7,0,2,-2],[1.2,0,10,-6],[4.25,0,10,-6],[4.6,0,0,0],[6,0,0,0]], rotation: [[0,0,0,0],[1.2,-28,0,0],[4.25,-28,0,0],[4.6,0,0,0],[6,0,0,0]] },
	scutes_mid: { position: [[0,0,0,0],[0.7,0,2,0],[1.2,0,11,0],[4.6,0,11,0],[4.95,0,0,0],[6,0,0,0]], rotation: [[0,0,0,0],[1.2,0,0,24],[4.6,0,0,24],[4.95,0,0,0],[6,0,0,0]] },
	scutes_rear: { position: [[0,0,0,0],[0.7,0,2,2],[1.2,0,10,6],[4.95,0,10,6],[5.25,0,0,0],[6,0,0,0]], rotation: [[0,0,0,0],[1.2,26,0,0],[4.95,26,0,0],[5.25,0,0,0],[6,0,0,0]] },
});

animation("grab_impalement", 3.5, {
	lowerBody: { position: [[0,0,0,0],[0.7,0,2.5,-1],[1.05,0,-1,-7],[1.5,0,0,-2],[2.15,0,0,-2],[2.55,0,0,1],[3.5,0,0,0]], rotation: [[0,0,0,0],[0.7,11,0,0],[1.05,-9,0,0],[2.15,-5,0,0],[2.55,4,0,0],[3.5,0,0,0]] },
	fLeftArm: { rotation: [[0,0,0,0],[0.7,-35,-24,-18],[1.05,-48,-34,-26],[1.5,-28,-18,-12],[2.15,-28,-18,-12],[2.55,18,24,20],[3.5,0,0,0]] },
	fRightArm: { rotation: [[0,0,0,0],[0.7,-35,24,18],[1.05,-48,34,26],[1.5,-28,18,12],[2.15,-28,18,12],[2.55,18,-24,-20],[3.5,0,0,0]] },
	upperJaw: { rotation: [[0,0,0,0],[1.35,-24,0,0],[1.5,18,0,0],[1.7,0,0,0],[3.5,0,0,0]] },
	lowerJaw: { rotation: [[0,0,0,0],[1.35,34,0,0],[1.5,-12,0,0],[1.7,0,0,0],[3.5,0,0,0]] },
	tail: { rotation: [[0,0,0,0],[1.55,-18,12,0],[2.05,-42,8,0],[2.1,28,-4,0],[2.55,8,0,0],[3.5,0,0,0]] },
	tail2: { rotation: [[0,0,0,0],[1.55,-24,-18,0],[2.05,-48,-10,0],[2.1,32,4,0],[2.55,10,0,0],[3.5,0,0,0]] },
	tail3: { rotation: [[0,0,0,0],[1.55,-28,20,0],[2.05,-55,12,0],[2.1,38,-6,0],[2.55,12,0,0],[3.5,0,0,0]] },
	tail4: { rotation: [[0,0,0,0],[1.55,-32,-22,0],[2.05,-62,-14,0],[2.1,44,7,0],[2.55,14,0,0],[3.5,0,0,0]] },
	tail5: { rotation: [[0,0,0,0],[1.55,-36,24,0],[2.05,-70,16,0],[2.1,52,-8,0],[2.55,16,0,0],[3.5,0,0,0]] },
	grab_socket: { position: [[0,0,0,0],[1.05,0,0,-8],[1.5,0,-9,-4],[2.15,0,-9,-4],[2.55,0,0,0],[3.5,0,0,0]] },
	bite_socket: { position: [[0,0,0,0],[1.5,0,-4,-3],[1.7,0,0,0],[3.5,0,0,0]] },
	impale_socket: { position: [[0,0,0,0],[2.05,0,-8,-18],[2.1,0,-18,-30],[2.55,0,0,0],[3.5,0,0,0]] },
});

fs.writeFileSync(MODEL_PATH, JSON.stringify(model));

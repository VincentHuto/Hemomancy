import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_PATH = path.join(ROOT,
	"src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/VesperTheCrownedRefusalModel.bbmodel");
const PREFIX = "animation.VesperTheCrownedRefusalModel.";

const LEGS = [
	{ bones: ["fLeg", "fLHip2", "fLFemur2", "fLTibia2", "fLTibia4", "flFoot2"], phase: 0.0 },
	{ bones: ["fLeg2", "fLHip3", "fLFemur3", "fLTibia3", "fLTibia5", "flFoot3"], phase: 0.25 },
	{ bones: ["fLeg3", "fLHip4", "fLFemur4", "fLTibia6", "fLTibia7", "flFoot4"], phase: 0.5 },
	{ bones: ["fLeg4", "fLHip5", "fLFemur5", "fLTibia8", "fLTibia9", "flFoot5"], phase: 0.5 },
	{ bones: ["fLeg5", "fLHip6", "fLFemur6", "fLTibia10", "fLTibia11", "flFoot6"], phase: 0.75 },
	{ bones: ["fLeg6", "fLHip7", "fLFemur7", "fLTibia12", "fLTibia13", "flFoot7"], phase: 0.0 },
];
const TAIL = ["tail", "tail2", "tail3", "tail4", "tail5"];

const model = JSON.parse(fs.readFileSync(MODEL_PATH, "utf8"));
const groups = new Map(model.groups.map(group => [group.name, group.uuid]));

function clip(name) {
	const result = model.animations.find(animation => animation.name === PREFIX + name);
	if (!result) throw new Error(`Missing ${PREFIX}${name}`);
	return result;
}

function animator(animation, bone) {
	const uuid = groups.get(bone);
	if (!uuid) throw new Error(`Missing model bone ${bone}`);
	const result = animation.animators[uuid];
	if (!result || result.name !== bone) throw new Error(`Missing ${animation.name}/${bone} animator`);
	return result;
}

function numeric(value) {
	const rounded = Math.abs(value) < 0.000005 ? 0 : Number(value.toFixed(5));
	return String(rounded);
}

function point(values) {
	return { x: numeric(values[0]), y: numeric(values[1]), z: numeric(values[2]) };
}

function uuidFor(animation, bone, channel, time, index) {
	const hex = crypto.createHash("sha256")
		.update(`${animation.name}|${bone}|${channel}|${time}|${index}`)
		.digest("hex").slice(0, 32);
	return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function setChannel(animation, bone, channelName, frames, interpolation = "catmullrom") {
	const target = animator(animation, bone);
	target.keyframes = (target.keyframes ?? []).filter(keyframe => keyframe.channel !== channelName);
	for (const [index, frame] of frames.entries()) {
		target.keyframes.push({
			channel: channelName,
			data_points: [point(frame.value)],
			uuid: uuidFor(animation, bone, channelName, frame.time, index),
			time: Number(frame.time),
			color: -1,
			interpolation,
		});
	}
	target.keyframes.sort((left, right) => left.time - right.time || left.channel.localeCompare(right.channel));
}

function frames(times, values) {
	return times.map((time, index) => ({ time, value: values[index] }));
}

function lerp(left, right, alpha) {
	return left + (right - left) * alpha;
}

const GAIT_KNOTS = [
	{ phase: 0.0, rotation: [8, 1.5, 18, -4, -10, 4], foot: [0, 0, 0] },
	{ phase: 0.25, rotation: [0, 0.5, 4, 0, -4, 2], foot: [0, 0, 0] },
	{ phase: 0.5, rotation: [-8, -1.5, -16, 8, 2, -2], foot: [0, 0, 0] },
	{ phase: 0.625, rotation: [-2, 0, -2, 22, 18, -12], foot: [0, -2, 0.9] },
	{ phase: 0.75, rotation: [8, 1.5, 22, 26, 22, -15], foot: [0, -3.2, -1.6] },
	{ phase: 0.875, rotation: [11, 2, 25, 12, 8, -6], foot: [0, -1.6, -0.8] },
	{ phase: 1.0, rotation: [8, 1.5, 18, -4, -10, 4], foot: [0, 0, 0] },
];

function gaitPose(phase) {
	const wrapped = ((phase % 1) + 1) % 1;
	const upper = GAIT_KNOTS.findIndex(knot => knot.phase >= wrapped);
	const right = GAIT_KNOTS[Math.max(1, upper)];
	const left = GAIT_KNOTS[Math.max(0, upper - 1)];
	const alpha = (wrapped - left.phase) / (right.phase - left.phase || 1);
	return {
		rotation: left.rotation.map((value, index) => lerp(value, right.rotation[index], alpha)),
		foot: left.foot.map((value, index) => lerp(value, right.foot[index], alpha)),
	};
}

function authorGait(animation, times, phaseAtTime, strengthAtTime = () => 1, recoverAtEnd = false) {
	for (const leg of LEGS) {
		const poses = times.map((time, index) => {
			if (recoverAtEnd && index === times.length - 1)
				return { rotation: [0, 0, 0, 0, 0, 0], foot: [0, 0, 0] };
			const pose = gaitPose(phaseAtTime(time, index) + leg.phase);
			const strength = strengthAtTime(time, index);
			return {
				rotation: pose.rotation.map(value => value * strength),
				foot: pose.foot.map(value => value * strength),
			};
		});
		for (let boneIndex = 0; boneIndex < leg.bones.length; boneIndex++) {
			const bone = leg.bones[boneIndex];
			setChannel(animation, bone, "rotation", frames(times,
				poses.map(pose => [pose.rotation[boneIndex], 0, 0])));
		}
		setChannel(animation, leg.bones.at(-1), "position", frames(times, poses.map(pose => pose.foot)));
	}
}

function authorCarriedMotion(animation, times, bodyValues, recoverAtEnd = false) {
	const bodyPosition = [];
	const bodyRotation = [];
	const throneRotation = [];
	const riderPosition = [];
	const riderRotation = [];
	for (let index = 0; index < times.length; index++) {
		const value = recoverAtEnd && index === times.length - 1
			? { shift: 0, compression: 0, pitch: 0, roll: 0 }
			: bodyValues(times[index], index);
		const delayed = recoverAtEnd && index === times.length - 1
			? { shift: 0, compression: 0, pitch: 0, roll: 0 }
			: bodyValues(times[Math.max(0, index - 1)], Math.max(0, index - 1));
		bodyPosition.push([value.shift, value.compression, 0]);
		bodyRotation.push([value.pitch, 0, value.roll]);
		throneRotation.push([delayed.pitch * 1.25, 0, delayed.roll * 1.18]);
		riderPosition.push([value.shift * 0.72, value.compression * 0.72, 0]);
		riderRotation.push([-delayed.pitch * 0.38, 0, -delayed.roll * 0.32]);
	}
	setChannel(animation, "lowerBody", "position", frames(times, bodyPosition));
	setChannel(animation, "lowerBody", "rotation", frames(times, bodyRotation));
	setChannel(animation, "throne", "rotation", frames(times, throneRotation));
	setChannel(animation, "vesper", "position", frames(times, riderPosition));
	setChannel(animation, "vesper", "rotation", frames(times, riderRotation));
}

function authorTail(animation, times, activityAtTime, pitchAtTime = () => 0, recoverAtEnd = false) {
	for (let segment = 0; segment < TAIL.length; segment++) {
		const amplitude = 2.2 + segment * 1.35;
		const delay = segment * 0.38;
		const values = times.map((time, index) => {
			if (recoverAtEnd && index === times.length - 1) return [0, 0, 0];
			const activity = activityAtTime(time, index);
			return [
				pitchAtTime(time, index) * (0.55 + segment * 0.13),
				Math.sin(time * Math.PI * 2 - delay) * amplitude * activity,
				Math.sin(time * Math.PI - delay) * 0.35 * activity,
			];
		});
		setChannel(animation, TAIL[segment], "rotation", frames(times, values));
	}
}

function authorIdle() {
	const animation = clip("idle");
	const times = [0, 0.5, 1, 1.5, 2, 2.5, 3];
	authorCarriedMotion(animation, times, time => ({
		shift: Math.sin(time * Math.PI * 2 / 3) * 0.18,
		compression: (1 - Math.cos(time * Math.PI * 2 / 3)) * 0.08,
		pitch: Math.sin(time * Math.PI * 2 / 3) * 0.35,
		roll: -Math.sin(time * Math.PI * 2 / 3) * 0.5,
	}));
	authorTail(animation, times, () => 0.45, time => Math.sin(time * Math.PI * 2 / 3) * 0.8);
}

function authorWalk() {
	const animation = clip("walk");
	const times = [0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1];
	authorGait(animation, times, time => time);
	authorCarriedMotion(animation, times, time => ({
		shift: Math.cos(time * Math.PI * 2) * 0.45,
		compression: Math.abs(Math.cos(time * Math.PI * 2)) * 0.38,
		pitch: Math.sin(time * Math.PI * 2) * 0.65,
		roll: -Math.cos(time * Math.PI * 2) * 1.45,
	}));
	authorTail(animation, times, () => 1, time => Math.sin(time * Math.PI * 2) * 1.2);
}

function authorRoyalScuttle() {
	const animation = clip("royal_scuttle");
	const times = [];
	for (let time = 0; time <= 2.75; time += 0.125) times.push(Number(time.toFixed(3)));
	times.push(2.9, 3.1);
	const strength = (time, index) => index === times.length - 2 ? 0.45 : 1.18;
	authorGait(animation, times, time => time / 0.75, strength, true);
	authorCarriedMotion(animation, times, (time, index) => {
		const amount = strength(time, index);
		return {
			shift: Math.cos(time * Math.PI * 2 / 0.75) * 0.62 * amount,
			compression: Math.abs(Math.cos(time * Math.PI * 2 / 0.75)) * 0.52 * amount,
			pitch: Math.sin(time * Math.PI * 2 / 0.75) * 1.1 * amount,
			roll: -Math.cos(time * Math.PI * 2 / 0.75) * 2.1 * amount,
		};
	}, true);
	authorTail(animation, times, (time, index) => strength(time, index),
		time => Math.sin(time * Math.PI * 2 / 0.75) * 2.2, true);
}

const ATTACKS = {
	pincer_vice: {
		times: [0, 0.45, 0.9, 1.2, 1.5, 1.9, 2.2, 2.55, 2.9],
		strength: [0, 0.35, 0.8, 1, 0.85, 0.65, 0.45, 0.2, 0],
		phase: [0, 0.08, 0.18, 0.3, 0.38, 0.45, 0.5, 0.5, 0.5],
		pitch: [0, -0.4, -1.2, -2.1, 1.4, 0.8, 0.3, 0.1, 0],
	},
	stinger_script: {
		times: [0, 0.4, 0.8, 1.1, 1.4, 1.7, 2, 2.3, 2.6, 3, 3.4],
		strength: [0, 0.35, 0.75, 0.9, 0.72, 0.9, 0.72, 0.9, 0.65, 0.25, 0],
		phase: [0, 0.05, 0.12, 0.18, 0.22, 0.3, 0.35, 0.42, 0.46, 0.5, 0.5],
		pitch: [0, 4, 20, 5, -22, 2, 24, 4, -18, -6, 0],
	},
	brood_trample: {
		times: [0, 0.55, 1.1, 1.6, 1.9, 2.5, 3.1],
		strength: [0, 0.45, 1, 0.82, 1.05, 0.32, 0],
		phase: [0, 0.08, 0.18, 0.28, 0.5, 0.55, 0.55],
		pitch: [0, -3, -8, -4, 7, 2, 0],
	},
	puppet_muster: {
		times: [0, 0.8, 1.4, 1.6, 2.2, 2.8, 3.4],
		strength: [0, 0.35, 0.55, 0.6, 0.5, 0.2, 0],
		phase: [0, 0.05, 0.1, 0.12, 0.15, 0.15, 0.15],
		pitch: [0, -1, -2, -2.5, -1.5, -0.5, 0],
	},
};

function authorAttack(name, profile) {
	const animation = clip(name);
	const valueAt = (values, index) => values[index];
	authorGait(animation, profile.times,
		(_time, index) => valueAt(profile.phase, index),
		(_time, index) => valueAt(profile.strength, index), true);
	authorCarriedMotion(animation, profile.times, (_time, index) => {
		const strength = valueAt(profile.strength, index);
		const side = Math.sin(valueAt(profile.phase, index) * Math.PI * 2);
		return {
			shift: side * 0.5 * strength,
			compression: strength * (name === "brood_trample" ? 0.75 : 0.38),
			pitch: valueAt(profile.pitch, index),
			roll: -side * 1.8 * strength,
		};
	}, true);
	authorTail(animation, profile.times,
		(_time, index) => valueAt(profile.strength, index),
		(_time, index) => valueAt(profile.pitch, index), true);
}

authorIdle();
authorWalk();
authorRoyalScuttle();
for (const [name, profile] of Object.entries(ATTACKS)) authorAttack(name, profile);

fs.writeFileSync(MODEL_PATH, JSON.stringify(model));

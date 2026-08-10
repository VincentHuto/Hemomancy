import crypto from "node:crypto";
import fs from "node:fs";

const ANIMATION_NAME = "animation.VesperTheEveningStarModel.hit";

function uuid(seed) {
  const hex = crypto.createHash("sha256").update(seed).digest("hex").slice(0, 32);
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function rotation(javaX, javaY, javaZ) {
  return [-javaX, -javaY, javaZ];
}

const tracks = {
  whole: { position: [[0, [0, 0, 0]], [.08, [0, -.85, .7]], [.18, [0, -.25, -.2]], [.32, [0, -.08, 0]], [.5, [0, 0, 0]]] },
  body: { rotation: [[0, rotation(0, 0, 0)], [.08, rotation(-17.5, 0, -8)], [.18, rotation(7, 0, 3.5)], [.32, rotation(-2, 0, -1)], [.5, rotation(0, 0, 0)]] },
  head: { rotation: [[0, rotation(0, 0, 0)], [.08, rotation(13, -7, 9)], [.2, rotation(-5, 3, -3)], [.5, rotation(0, 0, 0)]] },
  rightArm: { rotation: [[0, rotation(0, 0, 0)], [.08, rotation(18, -8, 16)], [.2, rotation(-7, 3, -6)], [.5, rotation(0, 0, 0)]] },
  leftArm: { rotation: [[0, rotation(0, 0, 0)], [.08, rotation(15, 7, -18)], [.2, rotation(-6, -3, 7)], [.5, rotation(0, 0, 0)]] },
  rElbow: { rotation: [[0, rotation(0, 0, 0)], [.1, rotation(14, 0, 5)], [.5, rotation(0, 0, 0)]] },
  rElbow2: { rotation: [[0, rotation(0, 0, 0)], [.1, rotation(12, 0, -5)], [.5, rotation(0, 0, 0)]] },
  ClothBack: { rotation: [[0, rotation(0, 0, 0)], [.08, rotation(14, 0, 0)], [.2, rotation(-10, 0, 0)], [.36, rotation(3, 0, 0)], [.5, rotation(0, 0, 0)]] },
  ClothBack1: { rotation: [[0, rotation(0, 0, 0)], [.12, rotation(18, 0, 0)], [.26, rotation(-12, 0, 0)], [.5, rotation(0, 0, 0)]] },
  ClothBack2: { rotation: [[0, rotation(0, 0, 0)], [.16, rotation(22, 0, 0)], [.3, rotation(-9, 0, 0)], [.5, rotation(0, 0, 0)]] },
};

function keyframe(bone, channel, time, vector) {
  return {
    channel,
    data_points: [{ x: vector[0], y: vector[1], z: vector[2] }],
    uuid: uuid(`${ANIMATION_NAME}:${bone}:${channel}:${time}`),
    time,
    color: -1,
    interpolation: "catmullrom",
  };
}

function install(file) {
  const model = JSON.parse(fs.readFileSync(file, "utf8"));
  const groups = new Map(model.groups.map(group => [group.name, group.uuid]));
  const animators = {};
  for (const [bone, channels] of Object.entries(tracks)) {
    const boneUuid = groups.get(bone);
    if (!boneUuid) throw new Error(`${file} is missing the ${bone} bone`);
    const keyframes = Object.entries(channels)
      .flatMap(([channel, samples]) => samples.map(([time, vector]) => keyframe(bone, channel, time, vector)))
      .sort((left, right) => left.time - right.time || left.channel.localeCompare(right.channel));
    animators[boneUuid] = { name: bone, type: "bone", keyframes };
  }
  const animation = {
    uuid: uuid(ANIMATION_NAME), name: ANIMATION_NAME, loop: "once", override: false,
    length: .5, snapping: 24, selected: false, anim_time_update: "", blend_weight: "",
    start_delay: "", loop_delay: "", animators,
  };
  model.animations = (model.animations ?? []).filter(candidate => candidate.name !== ANIMATION_NAME);
  model.animations.push(animation);
  fs.writeFileSync(file, `${JSON.stringify(model)}\n`);
}

const files = process.argv.slice(2);
if (!files.length) throw new Error("Pass one or more VesperTheEveningStarModel.bbmodel paths");
files.forEach(install);

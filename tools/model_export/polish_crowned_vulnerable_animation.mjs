import { createHash } from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

import { generateAnimationClass } from "./bbmodel_to_mojang_animations.mjs";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const MODEL_PATH = path.join(ROOT,
  "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/VesperTheCrownedRefusalModel.bbmodel");
const JAVA_PATH = path.join(ROOT,
  "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame/VesperTheCrownedRefusalAnimations.java");

const model = JSON.parse(fs.readFileSync(MODEL_PATH, "utf8"));
const vulnerable = model.animations.find(animation =>
  animation.name === "animation.VesperTheCrownedRefusalModel.vulnerable");
if (!vulnerable) throw new Error("Missing Crowned Refusal vulnerable animation");

const animators = new Map(Object.values(vulnerable.animators).map(animator => [animator.name, animator]));

function uuidFor(label) {
  const hex = createHash("sha256").update(`crowned-vulnerable:${label}`).digest("hex").slice(0, 32);
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function keyframe(bone, channel, time, x, y, z) {
  return {
    channel,
    data_points: [{ x: String(x), y: String(y), z: String(z) }],
    uuid: uuidFor(`${bone}:${channel}:${time}`),
    time,
    color: -1,
    interpolation: "catmullrom",
  };
}

function replaceTracks(bone, tracks) {
  const animator = animators.get(bone);
  if (!animator) throw new Error(`Missing vulnerable animator for ${bone}`);
  animator.keyframes = tracks.flatMap(track => track.samples.map(sample =>
    keyframe(bone, track.channel, sample[0], sample[1], sample[2], sample[3])));
}

replaceTracks("vesper", [
  { channel: "rotation", samples: [[0, -10.31324, 0, 0], [1.5, -10.31324, 0, 0], [3, -10.31324, 0, 0]] },
  { channel: "position", samples: [[0, 0, -7, 0], [1.5, 0, -7, 0], [3, 0, -7, 0]] },
]);
replaceTracks("body", [
  { channel: "rotation", samples: [[0, 0, 0, 0], [1.5, -1.2, 0, 0], [3, 0, 0, 0]] },
]);
replaceTracks("head2", [
  { channel: "rotation", samples: [[0, 0.6, 0, 0], [1.5, 1.6, 0, 0], [3, 0.6, 0, 0]] },
]);
replaceTracks("leftArm", [
  { channel: "rotation", samples: [[0, 0, 0, 0], [1.5, -1.1, 0, -0.45], [3, 0, 0, 0]] },
]);
replaceTracks("rightArm", [
  { channel: "rotation", samples: [[0, 0, 0, 0], [1.5, -1.1, 0, 0.45], [3, 0, 0, 0]] },
]);
replaceTracks("lowerBody", [
  { channel: "rotation", samples: [[0, -18.33465, 0, 0], [1.5, -18.33465, 0, 0], [3, -18.33465, 0, 0]] },
]);
replaceTracks("fLeftArm", [
  { channel: "rotation", samples: [[0, 0, 0, 0], [1.5, 0, 0, 0], [3, 0, 0, 0]] },
]);
replaceTracks("fRightArm", [
  { channel: "rotation", samples: [[0, 0, 0, 0], [1.5, 0, 0, 0], [3, 0, 0, 0]] },
]);

fs.writeFileSync(MODEL_PATH, JSON.stringify(model), "utf8");

const generated = generateAnimationClass({
  packageName: "com.vincenthuto.hemomancy.client.model.entity.boss.endgame",
  className: "VesperTheCrownedRefusalAnimations",
  animations: model.animations,
});
const generatedStart = generated.indexOf("\tpublic static final AnimationDefinition VULNERABLE");
const generatedEnd = generated.indexOf("\n\n\tpublic static final AnimationDefinition PINCER_VICE", generatedStart);
if (generatedStart < 0 || generatedEnd < 0) throw new Error("Could not isolate generated vulnerable definition");
const definition = generated.slice(generatedStart, generatedEnd);

const java = fs.readFileSync(JAVA_PATH, "utf8");
const javaStart = java.indexOf("\tpublic static final AnimationDefinition VULNERABLE");
const javaEnd = java.indexOf("\tpublic static final AnimationDefinition PINCER_VICE", javaStart);
if (javaStart < 0 || javaEnd < 0) throw new Error("Could not isolate existing vulnerable definition");
const eol = java.includes("\r\n") ? "\r\n" : "\n";
const normalizedDefinition = definition.replace(/\n/g, eol);
fs.writeFileSync(JAVA_PATH,
  java.slice(0, javaStart) + normalizedDefinition + eol + eol + java.slice(javaEnd), "utf8");

console.log("Updated authored vulnerable clip and its Java AnimationDefinition export.");

import fs from "node:fs";
import { pathToFileURL } from "node:url";

function number(value) {
  const parsed = Number(value);
  const normalized = Object.is(parsed, -0) ? 0 : parsed;
  return `${normalized.toFixed(5).replace(/0+$/, "").replace(/\.$/, ".0")}F`;
}

function constantName(name) {
  return name.slice(name.lastIndexOf(".") + 1).replace(/[^a-zA-Z0-9]+/g, "_").toUpperCase();
}

function channelSource(bone, channel, keyframes) {
  const target = { rotation: "ROTATION", position: "POSITION", scale: "SCALE" }[channel];
  const vector = { rotation: "degreeVec", position: "posVec", scale: "scaleVec" }[channel];
  const keys = keyframes
    .sort((left, right) => Number(left.time) - Number(right.time))
		.map(key => {
			const point = key.data_points[0];
			const x = channel === "rotation" ? -Number(point.x) : point.x;
			const y = channel === "rotation" ? -Number(point.y) : point.y;
			const interpolation = key.interpolation === "linear" ? "LINEAR" : "CATMULLROM";
			return `new Keyframe(${number(key.time)}, KeyframeAnimations.${vector}(${number(x)}, ${number(y)}, ${number(point.z)}), AnimationChannel.Interpolations.${interpolation})`;
    });
  return `\n\t\t\t.addAnimation("${bone}", new AnimationChannel(AnimationChannel.Targets.${target},\n\t\t\t\t\t${keys.join(",\n\t\t\t\t\t")}))`;
}

export function generateAnimationClass({ packageName, className, animations }) {
  const definitions = [];
  for (const animation of animations ?? []) {
    const channels = [];
    for (const animator of Object.values(animation.animators ?? {})) {
      if (animator.type !== "bone") continue;
      for (const channel of ["position", "rotation", "scale"]) {
        const keyframes = (animator.keyframes ?? []).filter(key => key.channel === channel);
        if (keyframes.length) channels.push(channelSource(animator.name, channel, keyframes));
      }
    }
    if (!channels.length) continue;
    const looping = animation.loop === "loop" ? ".looping()" : "";
    definitions.push(`\tpublic static final AnimationDefinition ${constantName(animation.name)} = AnimationDefinition.Builder.withLength(${number(animation.length)})${looping}${channels.join("")}\n\t\t\t.build();`);
  }

  return `package ${packageName};\n\nimport net.minecraft.client.animation.AnimationChannel;\nimport net.minecraft.client.animation.AnimationDefinition;\nimport net.minecraft.client.animation.Keyframe;\nimport net.minecraft.client.animation.KeyframeAnimations;\n\npublic final class ${className} {\n\tprivate ${className}() { }\n\n${definitions.join("\n\n")}\n}\n`;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const [input, output, packageName, className] = process.argv.slice(2);
  if (!className) throw new Error("Usage: node bbmodel_to_mojang_animations.mjs input.bbmodel output.java package class");
  const model = JSON.parse(fs.readFileSync(input, "utf8"));
  fs.writeFileSync(output, generateAnimationClass({ packageName, className, animations: model.animations }));
}

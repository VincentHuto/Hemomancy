import assert from "node:assert/strict";
import test from "node:test";
import { generateAnimationClass } from "./bbmodel_to_mojang_animations.mjs";

test("converts Blockbench rotation axes to Mojang animation coordinates", () => {
  const source = generateAnimationClass({
    packageName: "example",
    className: "ExampleAnimations",
    animations: [{
      name: "animation.Example.attack",
      length: 1.5,
      loop: "once",
      animators: {
        bone: {
          name: "arm",
          type: "bone",
          keyframes: [
            { channel: "rotation", time: 0.5, interpolation: "catmullrom", data_points: [{ x: "10", y: "-20", z: "30" }] },
            { channel: "position", time: 0, interpolation: "linear", data_points: [{ x: 1, y: 2, z: 3 }] },
          ],
        },
      },
    }],
  });

  assert.match(source, /public static final AnimationDefinition ATTACK = attack\(\);/);
	assert.match(source, /return AnimationDefinition\.Builder\.withLength\(1\.5F\)/);
  assert.doesNotMatch(source, /\.looping\(\)/);
	assert.match(source, /KeyframeAnimations\.degreeVec\(-10\.0F, 20\.0F, 30\.0F\)/);
  assert.match(source, /KeyframeAnimations\.posVec\(1\.0F, 2\.0F, 3\.0F\)/);
  assert.match(source, /AnimationChannel\.Interpolations\.CATMULLROM/);
  assert.match(source, /AnimationChannel\.Interpolations\.LINEAR/);
});

test("omits empty placeholder animations", () => {
  const source = generateAnimationClass({
    packageName: "example",
    className: "ExampleAnimations",
    animations: [{ name: "animation.Example.empty", length: 1, loop: "loop", animators: {} }],
  });

  assert.doesNotMatch(source, /AnimationDefinition EMPTY/);
});

test("splits animation builders into factory methods so large exports do not overflow the class initializer", () => {
	const source = generateAnimationClass({
		packageName: "example",
		className: "ExampleAnimations",
		animations: [{
			name: "animation.Example.walk",
			length: 1,
			loop: "loop",
			animators: {
				bone: {
					name: "leg",
					type: "bone",
					keyframes: [
						{ channel: "rotation", time: 0, interpolation: "linear", data_points: [{ x: 0, y: 0, z: 0 }] },
						{ channel: "rotation", time: 1, interpolation: "linear", data_points: [{ x: 10, y: 0, z: 0 }] },
					],
				},
			},
		}],
	});

	assert.match(source, /public static final AnimationDefinition WALK = walk\(\);/);
	assert.match(source, /private static AnimationDefinition walk\(\) \{/);
	assert.match(source, /return AnimationDefinition\.Builder\.withLength\(1\.0F\)\.looping\(\)/);
});

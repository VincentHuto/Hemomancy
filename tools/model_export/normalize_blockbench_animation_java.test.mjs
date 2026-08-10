import assert from "node:assert/strict";
import test from "node:test";
import { normalizeBlockbenchAnimationJava } from "./normalize_blockbench_animation_java.mjs";

test("normalizes Blockbench animation exports into a compilable mod class", () => {
	const input = `public class VesperTheCrownedRefusalModelAnimation {
	public static final AnimationDefinition animation.VesperTheCrownedRefusalModel.royal_scuttle = AnimationDefinition.Builder.withLength(3.1F).build();
}`;
	const output = normalizeBlockbenchAnimationJava(input, {
		packageName: "example",
		className: "VesperTheCrownedRefusalAnimations",
	});

	assert.match(output, /^package example;/);
	assert.match(output, /public final class VesperTheCrownedRefusalAnimations/);
	assert.match(output, /public static final AnimationDefinition ROYAL_SCUTTLE =/);
	assert.match(output, /private VesperTheCrownedRefusalAnimations\(\) \{ \}/);
	assert.doesNotMatch(output, /animation\.Vesper/);
});

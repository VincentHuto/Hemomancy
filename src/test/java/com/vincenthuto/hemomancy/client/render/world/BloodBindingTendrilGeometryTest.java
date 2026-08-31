package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BloodBindingTendrilGeometryTest {
	private static final Vec3 CASTER = new Vec3(0.0D, 64.0D, 0.0D);
	private static final Vec3 TARGET = new Vec3(6.0D, 64.0D, 0.0D);

	@Test
	void castSeedDeterministicallySelectsThreeToFiveSolidStrands() {
		Set<Integer> observed = new HashSet<>();
		for (long seed = 0; seed < 24; seed++) {
			int count = BloodBindingTendrilGeometry.strandCount(seed);
			assertTrue(count >= 3 && count <= 5);
			assertEquals(count, BloodBindingTendrilGeometry.strandCount(seed));
			observed.add(count);
		}
		assertEquals(Set.of(3, 4, 5), observed);

		var strands = strands(7L, 20.0F, 0.0F);
		assertEquals(BloodBindingTendrilGeometry.strandCount(7L), strands.size());
		assertTrue(strands.stream().flatMap(strand -> strand.joints().stream())
				.allMatch(joint -> joint.opacity() >= 0.85F),
				"Blood Binding strands should remain more opaque than the rite tendril tips");
	}

	@Test
	void formationRunsAcrossTheGroundBeforeClimbingTheTarget() {
		var halfGround = strands(3L, 4.0F, 0.0F).getFirst().joints();
		assertTrue(halfGround.getLast().center().x > 2.0D && halfGround.getLast().center().x < 4.0D,
				"The first eight ticks should grow toward the target along the ground");
		assertTrue(halfGround.getLast().center().y < 64.2D);

		var halfCoil = strands(3L, 12.0F, 0.0F).getFirst().joints();
		assertTrue(halfCoil.getLast().center().y > 64.5D && halfCoil.getLast().center().y < 65.5D,
				"The next eight ticks should climb the target");

		var complete = strands(3L, 16.0F, 0.0F).getFirst().joints();
		assertTrue(complete.getLast().center().y > 65.5D,
				"The completed binding should spiral over most of the target body");
	}

	@Test
	void strandsEmergeFromAroundTheCastersFeetInsteadOfTheirCenter() {
		var complete = strands(3L, 16.0F, 0.0F);
		for (var strand : complete) {
			Vec3 root = strand.joints().getFirst().center();
			double horizontalOffset = root.subtract(CASTER).horizontalDistance();
			assertTrue(horizontalOffset >= 0.18D && horizontalOffset <= 0.30D,
					"Each strand should emerge from the caster's footprint");
			assertTrue(root.y >= 64.035D && root.y <= 64.050D,
					"Caster roots should remain planted on the ground at foot level");
		}
	}

	@Test
	void overheadBlocksCannotLiftTheRootsOffTheCastersFeet() {
		var strands = BloodBindingTendrilGeometry.strands(CASTER, TARGET, 1.8D, 3L,
				20.0F, 16.0F, 0.0F, (x, z) -> 65.0D);
		assertEquals(64.035D, strands.getFirst().joints().getFirst().center().y, 0.0001D,
				"A surface above the expected path is overhead cover, not ground");
	}

	@Test
	void lowCeilingsCannotCreateVerticalSpikesAlongTheGroundPath() {
		var strands = BloodBindingTendrilGeometry.strands(CASTER, TARGET, 1.8D, 3L,
				20.0F, 8.0F, 0.0F, (x, z) -> x > 2.0D && x < 4.0D ? 64.5D : 64.0D);
		for (var strand : strands) {
			for (int index = 1; index < strand.joints().size(); index++) {
				double rise = Math.abs(strand.joints().get(index).center().y
						- strand.joints().get(index - 1).center().y);
				assertTrue(rise <= 0.25D, "A low ceiling must not pull a ground segment upward");
			}
		}
	}

	@Test
	void groundRibbonsRenderFlatAtFootLevel() {
		for (var strand : strands(3L, 8.0F, 0.0F)) {
			RecordingVertexConsumer vertices = new RecordingVertexConsumer();
			SanguineTendrilRibbonRenderer.renderLocal(new PoseStack(), vertices,
					List.of(strand), new Vec3(3.0D, 68.0D, -4.0D), false);
			float layer = vertices.yValues.getFirst();
			assertTrue(layer >= 64.035F && layer <= 64.050F);
			assertTrue(vertices.yValues.stream().allMatch(y -> Math.abs(y - layer) < 0.001F),
					"Each ground ribbon must lie flat instead of billboarding through entity torsos");
		}
	}

	@Test
	void overlappingGroundStrandsUseDistinctSubpixelLayers() {
		var strands = strands(3L, 8.0F, 0.0F);
		Set<Long> layers = strands.stream()
				.map(strand -> Math.round(strand.joints().getFirst().center().y * 10_000.0D))
				.collect(java.util.stream.Collectors.toSet());
		assertEquals(strands.size(), layers.size(),
				"Coplanar ground strands z-fight where their ribbons cross");
		assertTrue(layers.stream().mapToLong(Long::longValue).max().orElseThrow()
				- layers.stream().mapToLong(Long::longValue).min().orElseThrow() <= 200L,
				"Layer separation must remain visually planted at foot level");
	}

	@Test
	void groundStrandsTaperIntoPointsAtTheCaster() {
		var joints = strands(3L, 8.0F, 0.0F).getFirst().joints();
		assertTrue(joints.getFirst().halfWidth() <= 0.01F,
				"The player end should finish in a point instead of a flat cap");
		assertTrue(joints.get(1).halfWidth() > joints.getFirst().halfWidth());
		assertTrue(joints.get(2).halfWidth() > joints.get(1).halfWidth());
	}

	@Test
	void bloodBindingUsesCrimsonAndDeepRedInsteadOfBlack() {
		RecordingVertexConsumer vertices = new RecordingVertexConsumer();
		var strands = strands(3L, 16.0F, 0.0F);
		SanguineTendrilRibbonRenderer.renderLocal(new PoseStack(), vertices,
				strands, new Vec3(3.0D, 68.0D, -4.0D), false);
		SanguineTendrilRibbonRenderer.renderLocal(new PoseStack(), vertices,
				strands, new Vec3(3.0D, 68.0D, -4.0D), true);
		assertTrue(vertices.redValues.stream().allMatch(red -> red >= 30),
				"Blood Binding strands should remain deep red instead of falling to black");
	}

	@Test
	void bodyCoilNarrowsToATipAsItClimbs() {
		var joints = strands(3L, 16.0F, 0.0F).getFirst().joints();
		var coilBase = joints.get(13);
		var coilTip = joints.getLast();
		double baseRadius = coilBase.center().subtract(TARGET).horizontalDistance();
		double tipRadius = coilTip.center().subtract(TARGET).horizontalDistance();
		assertTrue(tipRadius < baseRadius * 0.30D,
				"The spiral envelope should taper instead of forming a cylinder");
		assertTrue(coilTip.halfWidth() < coilBase.halfWidth() * 0.50F,
				"The tendril itself should narrow toward the upper tip");
	}

	@Test
	void retractionUnwindsTheBodyThenReturnsAcrossTheGroundToTheCaster() {
		List<BloodBindingTendrilGeometry.Joint> complete = strands(11L, 20.0F, 0.0F).getFirst().joints();
		List<BloodBindingTendrilGeometry.Joint> unwinding = strands(11L, 20.0F, 4.0F).getFirst().joints();
		assertTrue(unwinding.getLast().center().y < complete.getLast().center().y,
				"The first half of retraction should unwind downward along the target");
		assertTrue(unwinding.getLast().center().x > 5.5D,
				"Ground strands must remain connected while the body coils unwind");

		List<BloodBindingTendrilGeometry.Joint> returning = strands(11L, 20.0F, 12.0F).getFirst().joints();
		assertTrue(returning.getLast().center().x > 2.0D && returning.getLast().center().x < 4.0D,
				"The second half should withdraw from the target back toward the caster");
		assertTrue(BloodBindingTendrilGeometry.strands(CASTER, TARGET, 1.8D, 11L,
				20.0F, 20.0F, 16.0F, (x, z) -> 64.0D).isEmpty(),
				"All tendrils should be gone after the reverse animation completes");
	}

	private static List<BloodBindingTendrilGeometry.Strand> strands(long seed, float age, float retraction) {
		return BloodBindingTendrilGeometry.strands(CASTER, TARGET, 1.8D, seed,
				20.0F, age, retraction, (x, z) -> 64.0D);
	}

	private static final class RecordingVertexConsumer implements VertexConsumer {
		private final List<Float> yValues = new java.util.ArrayList<>();
		private final List<Integer> redValues = new java.util.ArrayList<>();

		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			yValues.add(y);
			return this;
		}

		@Override public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			redValues.add(red);
			return this;
		}
		@Override public VertexConsumer setUv(float u, float v) { return this; }
		@Override public VertexConsumer setUv1(int u, int v) { return this; }
		@Override public VertexConsumer setUv2(int u, int v) { return this; }
		@Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
	}
}

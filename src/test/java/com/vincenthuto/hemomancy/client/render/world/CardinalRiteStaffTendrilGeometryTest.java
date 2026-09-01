package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteStaffTendrilGeometryTest {
	@Test
	void strandsRiseFromTheFocusAndCoilAroundTheStaff() {
		BlockPos focus = new BlockPos(20, 64, -10);
		List<CardinalRiteStaffTendrilGeometry.Strand> strands =
				CardinalRiteStaffTendrilGeometry.strands(focus, 120.0F);

		assertEquals(6, strands.size(), "the joint should have a small nest of distinct tendrils");
		for (CardinalRiteStaffTendrilGeometry.Strand strand : strands) {
			List<CardinalRiteStaffTendrilGeometry.Joint> joints = strand.joints();
			Vec3 root = joints.getFirst().center();
			Vec3 tip = joints.getLast().center();

			assertEquals(64.86D, root.y, 0.0001D, "the root must remain seated in the focus");
			assertTrue(tip.y - root.y >= 0.70D, "each tendril must visibly climb the staff");
			assertTrue(radialDistance(tip, focus) < radialDistance(root, focus),
					"the coil must tighten as it climbs");
			assertTrue(Math.abs(unwrappedTurn(joints, focus)) >= Math.PI * 1.70D,
					"each strand must visibly wind around the staff");
			assertTrue(joints.getLast().halfWidth() < joints.getFirst().halfWidth(),
					"the strand must taper toward its free tip");
		}
	}

	@Test
	void writhingMovesFreeLengthsWithoutSlidingTheirRoots() {
		BlockPos focus = new BlockPos(-4, 72, 9);
		List<CardinalRiteStaffTendrilGeometry.Strand> earlier =
				CardinalRiteStaffTendrilGeometry.strands(focus, 40.0F);
		List<CardinalRiteStaffTendrilGeometry.Strand> later =
				CardinalRiteStaffTendrilGeometry.strands(focus, 47.0F);

		for (int index = 0; index < earlier.size(); index++) {
			var first = earlier.get(index).joints();
			var second = later.get(index).joints();
			assertEquals(first.getFirst().center(), second.getFirst().center(),
					"animation must not detach a tendril from the focus");
			assertNotEquals(first.get(first.size() / 2).center(), second.get(second.size() / 2).center(),
					"the free length should writhe as time advances");
		}
	}

	@Test
	void plantingGrowsAndAbsorptionRetractsWholeTendrilSegments() {
		assertEquals(0.0F,
				CardinalRiteStaffTendrilGeometry.visibilityProgress(14.0F, 0.0F),
				0.0001F, "impact starts with only the tendril roots");
		assertEquals(0.5F,
				CardinalRiteStaffTendrilGeometry.visibilityProgress(18.0F, 0.0F),
				0.0001F, "half the segments have grown midway through recovery");
		assertEquals(1.0F,
				CardinalRiteStaffTendrilGeometry.visibilityProgress(22.0F, 0.0F),
				0.0001F, "all segments finish wrapping after planting");
		assertEquals(0.5F,
				CardinalRiteStaffTendrilGeometry.visibilityProgress(22.0F, 40.0F),
				0.0001F, "half the segments retract halfway through absorption");
		assertEquals(0.0F,
				CardinalRiteStaffTendrilGeometry.visibilityProgress(22.0F, 80.0F),
				0.0001F, "the final root segment retracts at full absorption");
		assertEquals(1.0F,
				CardinalRiteStaffTendrilGeometry.visibilityProgress(-1.0F, 0.0F),
				0.0001F, "rites without a planting intro retain complete tendrils");
	}

	@Test
	void partialGrowthExtendsOnlyTheLeadingSegmentInsteadOfScalingTheWholeCoil() {
		BlockPos focus = new BlockPos(20, 64, -10);
		List<CardinalRiteStaffTendrilGeometry.Joint> joints =
				CardinalRiteStaffTendrilGeometry.strands(focus, 120.0F)
						.getFirst().joints();

		assertTrue(CardinalRiteStaffTendrilGeometry.visibleJoints(joints, 0.0F).isEmpty());
		assertEquals(5,
				CardinalRiteStaffTendrilGeometry.visibleJoints(joints, 0.25F).size(),
				"four complete segments require five joints");

		List<CardinalRiteStaffTendrilGeometry.Joint> fourAndAHalf =
				CardinalRiteStaffTendrilGeometry.visibleJoints(joints, 0.28125F);
		assertEquals(6, fourAndAHalf.size());
		assertEquals(joints.get(4).center().lerp(joints.get(5).center(), 0.5D),
				fourAndAHalf.getLast().center(),
				"only the currently growing segment gets a clipped endpoint");
		assertEquals(joints.size(),
				CardinalRiteStaffTendrilGeometry.visibleJoints(joints, 1.0F).size());
	}

	private static double radialDistance(Vec3 point, BlockPos focus) {
		double dx = point.x - (focus.getX() + 0.5D);
		double dz = point.z - (focus.getZ() + 0.5D);
		return Math.sqrt(dx * dx + dz * dz);
	}

	private static double unwrappedTurn(List<CardinalRiteStaffTendrilGeometry.Joint> joints, BlockPos focus) {
		double centerX = focus.getX() + 0.5D;
		double centerZ = focus.getZ() + 0.5D;
		double previous = Math.atan2(joints.getFirst().center().z - centerZ,
				joints.getFirst().center().x - centerX);
		double turn = 0.0D;
		for (int index = 1; index < joints.size(); index++) {
			Vec3 point = joints.get(index).center();
			double angle = Math.atan2(point.z - centerZ, point.x - centerX);
			double step = angle - previous;
			while (step > Math.PI) step -= Math.PI * 2.0D;
			while (step < -Math.PI) step += Math.PI * 2.0D;
			turn += step;
			previous = angle;
		}
		return turn;
	}
}

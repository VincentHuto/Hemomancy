package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.vein.EarthenVeinTravelPhase;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EarthenVeinFeedingTendrilGeometryTest {
	private static final Vec3 MOUTH = new Vec3(0.5D, 0.72D, 0.5D);

	@Test
	void feedingStrandsGraspFromAroundTheVeinAndConvergeInsideItsMouth() {
		List<EarthenVeinFeedingTendrilGeometry.Strand> strands =
				EarthenVeinFeedingTendrilGeometry.strands(EarthenVeinTravelPhase.FEEDING, 0.6F, 40.0F);

		assertEquals(6, strands.size());
		for (var strand : strands) {
			var joints = strand.joints();
			assertTrue(radialDistance(joints.getFirst().center()) > 0.62D,
					"each grasp must begin outside the vein shell");
			assertTrue(joints.getLast().center().distanceTo(MOUTH) < 0.035D,
					"each grasp must disappear into the mouth");
			assertTrue(joints.getLast().halfWidth() < joints.getFirst().halfWidth(),
					"the sucking tip must taper as it enters the mouth");
		}
	}

	@Test
	void satedTendrilsTurnCrimsonAndWritheMoreSlowly() {
		var feedingEarlier = EarthenVeinFeedingTendrilGeometry.strands(
				EarthenVeinTravelPhase.FEEDING, 1.0F, 40.0F).getFirst();
		var feedingLater = EarthenVeinFeedingTendrilGeometry.strands(
				EarthenVeinTravelPhase.FEEDING, 1.0F, 41.0F).getFirst();
		var readyEarlier = EarthenVeinFeedingTendrilGeometry.strands(
				EarthenVeinTravelPhase.READY, 1.0F, 40.0F).getFirst();
		var readyLater = EarthenVeinFeedingTendrilGeometry.strands(
				EarthenVeinTravelPhase.READY, 1.0F, 41.0F).getFirst();

		assertTrue(readyEarlier.crimsonVeins());
		assertNotEquals(readyEarlier.joints().get(4).center(), readyLater.joints().get(4).center());
		double feedingMotion = feedingEarlier.joints().get(4).center()
				.distanceTo(feedingLater.joints().get(4).center());
		double readyMotion = readyEarlier.joints().get(4).center()
				.distanceTo(readyLater.joints().get(4).center());
		assertTrue(readyMotion < feedingMotion,
				"the fed cue must visibly settle instead of keeping the hungry twitch speed");
	}

	private static double radialDistance(Vec3 point) {
		double x = point.x - MOUTH.x;
		double z = point.z - MOUTH.z;
		return Math.sqrt(x * x + z * z);
	}
}

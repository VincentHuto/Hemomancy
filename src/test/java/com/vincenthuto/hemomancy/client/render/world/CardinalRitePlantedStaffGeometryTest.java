package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRitePlantedStaffGeometryTest {
	@Test
	void plantedStaffStandsUprightEnlargedAndEmbeddedInTheFocus() {
		var pose = CardinalRitePlantedStaffGeometry.pose(new BlockPos(20, 64, -10));

		assertEquals(20.425D, pose.position().x, 0.0001D);
		assertEquals(65.95D, pose.position().y, 0.0001D);
		assertEquals(-9.275D, pose.position().z, 0.0001D);
		assertEquals(0.0F, pose.pitchDegrees(), 0.0001F,
				"the staff model is already vertical and must not be laid on its side");
		assertEquals(1.75F, pose.scale(), 0.0001F,
				"the planted staff should read as a prominent ritual centerpiece");
	}

	@Test
	void dissolvingStaffConvergesOnThePhysicalFocusWithRealOpacity() {
		BlockPos focus = new BlockPos(20, 64, -10);

		var halfway = CardinalRitePlantedStaffGeometry.dissolvePose(focus, 0.5F, 0.4F);
		assertEquals(20.4625D, halfway.position().x, 0.0001D);
		assertEquals(65.45D, halfway.position().y, 0.0001D);
		assertEquals(-9.3875D, halfway.position().z, 0.0001D);
		assertEquals(0.875F, halfway.scale(), 0.0001F);
		assertEquals(0.4F, halfway.opacity(), 0.0001F);

		var gone = CardinalRitePlantedStaffGeometry.dissolvePose(focus, 0.0F, 0.0F);
		assertEquals(20.5D, gone.position().x, 0.0001D);
		assertEquals(64.95D, gone.position().y, 0.0001D);
		assertEquals(-9.5D, gone.position().z, 0.0001D);
		assertEquals(0.0F, gone.scale(), 0.0001F);
		assertEquals(0.0F, gone.opacity(), 0.0001F);
	}

	@Test
	void staffKeepsItsSilhouetteWhileTheBloodShaderMeltsItIntoTheFocusPool() {
		BlockPos focus = new BlockPos(20, 64, -10);

		var untouched = CardinalRitePlantedStaffGeometry.absorptionPose(focus, 0.0F);
		assertFalse(untouched.melt().active());
		assertEquals(1.75F, untouched.scale(), 0.0001F);

		var started = CardinalRitePlantedStaffGeometry.absorptionPose(focus, 1.0F);
		assertTrue(started.melt().active());
		assertTrue(started.melt().shaderProgress() > 0.08F,
				"the first absorption tick must visibly enter the shader's puddle phase");
		assertTrue(started.melt().progress() < 0.02F,
				"the immediate melt must still begin gradually");
		assertEquals(new net.minecraft.world.phys.Vec3(20.5D, 64.95D, -9.5D),
				started.melt().poolCenter());

		var halfway = CardinalRitePlantedStaffGeometry.absorptionPose(focus, 40.0F);
		assertEquals(CardinalRitePlantedStaffGeometry.pose(focus).position(), halfway.position(),
				"the shader should collapse the staff instead of moving its render origin off center");
		assertTrue(halfway.scale() > 1.5F,
				"the staff must remain large enough for its bottom-up melt to be visible");
		assertEquals(0.5F, halfway.melt().progress(), 0.0001F);
		assertEquals(0.5F, halfway.opacity(), 0.0001F);

		var complete = CardinalRitePlantedStaffGeometry.absorptionPose(focus, 80.0F);
		assertEquals(1.0F, complete.melt().progress(), 0.0001F);
		assertEquals(1.0F, complete.melt().shaderProgress(), 0.0001F);
		assertEquals(0.0F, complete.opacity(), 0.0001F);
	}
}

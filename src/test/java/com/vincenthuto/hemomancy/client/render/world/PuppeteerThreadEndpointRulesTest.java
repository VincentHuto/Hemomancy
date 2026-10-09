package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PuppeteerThreadEndpointRulesTest {
	@Test
	void summonEndpointInterpolatesItsCenterAtFortyFivePercentHeight() {
		Vec3 endpoint = PuppeteerThreadEndpointRules.summonEndpoint(
				2.0D, 4.0D, 6.0D, 10.0D, 12.0D, 14.0D, 2.0D, 0.25F);
		assertEquals(4.0D, endpoint.x, 0.0001D);
		assertEquals(7.9D, endpoint.y, 0.0001D);
		assertEquals(8.0D, endpoint.z, 0.0001D);
	}

	@Test
	void summonEndpointScalesWithShortAndTallBodiesWithoutChangingXZ() {
		Vec3 shortBody = PuppeteerThreadEndpointRules.summonEndpoint(
				3.0D, 7.0D, 11.0D, 3.0D, 7.0D, 11.0D, 1.0D, 0.75F);
		Vec3 tallBody = PuppeteerThreadEndpointRules.summonEndpoint(
				3.0D, 7.0D, 11.0D, 3.0D, 7.0D, 11.0D, 4.0D, 0.75F);
		assertEquals(new Vec3(3.0D, 8.45D, 11.0D), shortBody);
		assertEquals(new Vec3(3.0D, 9.8D, 11.0D), tallBody);
	}

	@Test
	void customHeightScaleLowersAHorizontalBodyAnchor() {
		Vec3 endpoint = PuppeteerThreadEndpointRules.summonEndpoint(
				3.0D, 7.0D, 11.0D, 3.0D, 7.0D, 11.0D, 0.8D, 0.25D, 0.5F);
		assertEquals(new Vec3(3.0D, 8.2D, 11.0D), endpoint);
	}

	@Test
	void playerHandEndpointIsLowerAndLateralInBothCameraModes() {
		Vec3 eye = new Vec3(10.0D, 20.0D, 30.0D);
		Vec3 view = new Vec3(0.0D, 0.0D, 1.0D);
		assertEquals(new Vec3(9.68D, 19.95D, 30.42D),
				PuppeteerThreadEndpointRules.playerHandEndpoint(eye, view, 0.0F, -1.0D, true));
		assertEquals(new Vec3(9.68D, 19.25D, 30.42D),
				PuppeteerThreadEndpointRules.playerHandEndpoint(eye, view, 0.0F, -1.0D, false));
	}

	@Test
	void threadClosesToAPointAtBothEndsInsteadOfLeavingAnOpenTube() {
		assertEquals(0.0D, PuppeteerThreadEndpointRules.threadRadius(0.0D), 1.0E-9D);
		assertEquals(0.0D, PuppeteerThreadEndpointRules.threadRadius(1.0D), 1.0E-9D);
		// The body keeps the original root-to-tip taper away from the closed ends.
		assertEquals(0.0125D, PuppeteerThreadEndpointRules.threadRadius(0.5D), 1.0E-9D);
		double previous = 0.0D;
		for (int i = 1; i <= 3; i++) {
			double radius = PuppeteerThreadEndpointRules.threadRadius(i / 28.0D);
			assertTrue(radius > previous, "the controller end must swell smoothly out of its point");
			previous = radius;
		}
	}

	@Test
	void performerHandEndpointMatchesAThirdPersonRightHandAlongTheBodyYaw() {
		Vec3 eye = new Vec3(10.0D, 20.0D, 30.0D);
		assertEquals(new Vec3(9.68D, 19.25D, 30.42D),
				PuppeteerThreadEndpointRules.performerHandEndpoint(eye, 0.0F));
		// Facing west (yaw 90): forward is -X and the right hand sits toward -Z.
		Vec3 west = PuppeteerThreadEndpointRules.performerHandEndpoint(eye, (float) Math.toRadians(90.0D));
		assertEquals(9.58D, west.x, 0.0001D);
		assertEquals(19.25D, west.y, 0.0001D);
		assertEquals(29.68D, west.z, 0.0001D);
	}
}

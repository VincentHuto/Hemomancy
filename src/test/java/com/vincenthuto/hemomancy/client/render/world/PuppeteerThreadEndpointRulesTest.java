package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PuppeteerThreadEndpointRulesTest {
	@Test
	void summonEndpointInterpolatesItsCenterAtFortyFivePercentHeight() {
		Vec3 endpoint = PuppeteerThreadEndpointRules.summonEndpoint(
				2.0D, 4.0D, 6.0D, 10.0D, 12.0D, 14.0D, 2.0D, 0.25F);
		assertEquals(4.0D, endpoint.x, 0.0001D);
		assertEquals(6.9D, endpoint.y, 0.0001D);
		assertEquals(8.0D, endpoint.z, 0.0001D);
	}

	@Test
	void summonEndpointScalesWithShortAndTallBodiesWithoutChangingXZ() {
		Vec3 shortBody = PuppeteerThreadEndpointRules.summonEndpoint(
				3.0D, 7.0D, 11.0D, 3.0D, 7.0D, 11.0D, 1.0D, 0.75F);
		Vec3 tallBody = PuppeteerThreadEndpointRules.summonEndpoint(
				3.0D, 7.0D, 11.0D, 3.0D, 7.0D, 11.0D, 4.0D, 0.75F);
		assertEquals(new Vec3(3.0D, 7.45D, 11.0D), shortBody);
		assertEquals(new Vec3(3.0D, 8.8D, 11.0D), tallBody);
	}

	@Test
	void customHeightScaleLowersAHorizontalBodyAnchor() {
		Vec3 endpoint = PuppeteerThreadEndpointRules.summonEndpoint(
				3.0D, 7.0D, 11.0D, 3.0D, 7.0D, 11.0D, 0.8D, 0.25D, 0.5F);
		assertEquals(new Vec3(3.0D, 7.2D, 11.0D), endpoint);
	}

	@Test
	void playerHandEndpointIsLowerAndLateralInBothCameraModes() {
		Vec3 eye = new Vec3(10.0D, 20.0D, 30.0D);
		Vec3 view = new Vec3(0.0D, 0.0D, 1.0D);
		assertEquals(new Vec3(9.38D, 18.95D, 30.42D),
				PuppeteerThreadEndpointRules.playerHandEndpoint(eye, view, 0.0F, -1.0D, true));
		assertEquals(new Vec3(9.38D, 18.55D, 30.42D),
				PuppeteerThreadEndpointRules.playerHandEndpoint(eye, view, 0.0F, -1.0D, false));
	}
}

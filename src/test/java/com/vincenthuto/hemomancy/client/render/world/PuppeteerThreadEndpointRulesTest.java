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
}

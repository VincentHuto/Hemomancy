package com.vincenthuto.hemomancy.client.render.world.zone;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class RiteZoneBoundaryRulesTest {
	@Test
	void perimeterDistanceIsMeasuredToTheNearestEdgeFromEitherSide() {
		assertEquals(2.0, RiteZoneBoundaryRules.distanceToPerimeter(2, 10, 0, 0, 20, 20), 1.0e-9);
		assertEquals(5.0, RiteZoneBoundaryRules.distanceToPerimeter(25, 10, 0, 0, 20, 20), 1.0e-9);
		assertEquals(5.0, RiteZoneBoundaryRules.distanceToPerimeter(23, 24, 0, 0, 20, 20), 1.0e-9);
	}

	@Test
	void onlyTheStretchNearTheCameraIsDrawn() {
		double[] span = RiteZoneBoundaryRules.visibleSpan(0, -200, 200, 10, 6, 10);
		assertNotNull(span);
		assertEquals(2.0, span[0], 1.0e-9);
		assertEquals(18.0, span[1], 1.0e-9);
		assertNull(RiteZoneBoundaryRules.visibleSpan(0, -200, 200, 10, 10, 10));
		double[] clipped = RiteZoneBoundaryRules.visibleSpan(0, 0, 5, 10, 0, 10);
		assertArrayEquals(new double[] { 0, 5 }, clipped, 1.0e-9);
	}

	@Test
	void fadeAndPulsesStayInRange() {
		assertEquals(1.0f, RiteZoneBoundaryRules.fade(0, 32), 1.0e-6f);
		assertEquals(0.0f, RiteZoneBoundaryRules.fade(32, 32), 1.0e-6f);
		for (int t = 0; t < 400; t += 7) {
			float dominion = RiteZoneBoundaryRules.dominionPulse(t, t * 0.3);
			float fervor = RiteZoneBoundaryRules.fervorPulse(t, t * 0.3);
			assertTrue(dominion >= 0.35f - 1.0e-6f && dominion <= 1.0f + 1.0e-6f);
			assertTrue(fervor >= 0.28f - 1.0e-6f && fervor <= 1.0f + 1.0e-6f);
			assertTrue(Math.abs(RiteZoneBoundaryRules.veinMeander(t, 1.3)) <= 0.19 + 1.0e-9);
		}
	}

	@Test
	void rendererAndSyncPacketAreWiredIn() throws IOException {
		String client = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/world/WorldEffectDispatcher.java"));
		String packets = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		assertTrue(client.contains("RiteZoneBoundaryRenderer.render(event.getPoseStack(), partialTick)"));
		assertTrue(packets.contains("RiteZoneSyncPacket.TYPE"));
	}
}

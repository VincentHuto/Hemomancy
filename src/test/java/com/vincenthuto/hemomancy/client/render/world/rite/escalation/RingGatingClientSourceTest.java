package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class RingGatingClientSourceTest {
	private static final String ROOT = "src/main/java/com/vincenthuto/hemomancy/";

	@Test
	void soresScarWhileAsleepAndTearOpen() throws IOException {
		String sores = RiteWorldSourceTest.read(ROOT + "client/render/world/floor/AnchorSores.java");
		assertTrue(sores.contains("RingTear.size(rite.anchorDormant(i), origin, ranks[i], 0.0F)"));
		assertTrue(sores.contains("/ (float) Math.max(1, rite.anchorRequirement(i))"));
		assertFalse(sores.contains("BLOOD_PER_ANCHOR_ML"));
	}

	@Test
	void jetsReadTheAnchorsOwnRequirementAndFlareOnWaking() throws IOException {
		String jets = RiteWorldSourceTest.read(ROOT + "client/render/world/rite/CardinalRiteAnchorJetRenderer.java");
		assertTrue(jets.contains("rite.anchorRequirement(i)"));
		assertTrue(jets.contains("RingTear.waking(center, ranks[i], partialTick)"));
		assertFalse(jets.contains("BLOOD_PER_ANCHOR_ML"));
	}

	@Test
	void theFloorShakesAndTheWorldSurgesWhenARingWakes() throws IOException {
		String floor = RiteWorldSourceTest.read(ROOT + "client/render/world/floor/FloorRiteResponse.java");
		String events = RiteWorldSourceTest.read(ROOT + "client/data/CardinalRiteEventClientState.java");
		assertTrue(floor.contains("CardinalRiteEventKind.RING_OPENED"));
		assertTrue(floor.contains("RingOpeningMath.shockScale("));
		assertTrue(events.contains("RiteWorldState.ringOpened(packet.center(),"));
	}

	@Test
	void thePanelShowsSleepingRings() throws IOException {
		String overlay = RiteWorldSourceTest.read(ROOT + "client/screen/overlay/CardinalRiteOverlay.java");
		assertTrue(overlay.contains("pips(rite.getCompletedRings(), rite.getOpenRings(), rite.getTotalRings())"));
	}
}

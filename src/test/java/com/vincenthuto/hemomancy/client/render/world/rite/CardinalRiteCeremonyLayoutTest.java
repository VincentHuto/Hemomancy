package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.client.render.world.CardinalRiteBoundaryGeometry;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteCeremonyLayoutTest {
	@Test
	void missingCeremonyHasNoLayout() {
		assertNull(CardinalRiteCeremonyLayout.of(null));
	}

	@Test
	void ringsPassThroughTheirAuthoredSocketsAndKnowTheirNeighbours() {
		CardinalRiteCeremonyLayout layout = CardinalRiteCeremonyLayout.of(ceremony(List.of(
				new CardinalRiteCeremonyDefinition.Anchor(3, 0, 0, 0, 0),
				new CardinalRiteCeremonyDefinition.Anchor(0, 0, 3, 0, 1),
				new CardinalRiteCeremonyDefinition.Anchor(-3, 0, 0, 0, 2),
				new CardinalRiteCeremonyDefinition.Anchor(0, 0, -3, 0, 3))));

		assertEquals(1, layout.ringCount());
		assertEquals(3.0F, layout.ringRadius(0), 0.0001F);
		assertEquals(Math.PI / 2.0D, layout.forwardSweep(0), 0.0001D);
		assertEquals(Math.PI / 2.0D, layout.backwardSweep(1), 0.0001D);
		assertEquals(4, layout.socketEndpoints(0).size() / 2, "two pinned endpoints per socket");
	}

	@Test
	void segmentClearancesMatchTheSocketCircleIntersection() {
		CardinalRiteCeremonyLayout layout = CardinalRiteCeremonyLayout.of(ceremony(List.of(
				new CardinalRiteCeremonyDefinition.Anchor(3, 0, 0, 0, 0),
				new CardinalRiteCeremonyDefinition.Anchor(0, 0, 3, 0, 1))));
		var segment = new CardinalRiteBoundaryProgress.Segment(0, 0.0D, Math.PI / 2.0D, 0, 1.0F);
		double expected = CardinalRiteBoundaryGeometry.socketClearanceAngle(3.0F, 3.0F,
				CardinalRiteBoundaryGeometry.socketThroatRadius(
						CardinalRiteCeremonyLayout.SOCKET_RADIUS, CardinalRiteCeremonyLayout.SOCKET_CORE_WIDTH));

		assertEquals(expected, layout.startClearance(segment), 0.0001D);
		assertEquals(expected, layout.endClearance(segment), 0.0001D);
	}

	@Test
	void stainMeshIsBuiltOnceAsQuadsAndClearsTheSockets() {
		CardinalRiteCeremonyLayout layout = CardinalRiteCeremonyLayout.of(ceremony(List.of(
				new CardinalRiteCeremonyDefinition.Anchor(3, 0, 0, 0, 0),
				new CardinalRiteCeremonyDefinition.Anchor(-3, 0, 0, 0, 1))));
		float[] mesh = layout.stainMesh(0);
		int stride = CardinalRiteCeremonyLayout.STAIN_VERTEX_STRIDE;

		assertTrue(mesh.length > 0);
		assertEquals(0, mesh.length % (stride * 4), "whole quads");
		boolean maskedAtSocket = false;
		for (int vertex = 0; vertex < mesh.length; vertex += stride) {
			float dx = mesh[vertex] - 3.0F;
			float dz = mesh[vertex + 1];
			if (dx * dx + dz * dz < 0.04F) maskedAtSocket |= mesh[vertex + 2] == 0.0F;
		}
		assertTrue(maskedAtSocket, "the black backing stops inside the anchor stain");
		assertEquals(0, layout.stainMesh(5).length, "rings without arcs have no stain");
	}

	private static CardinalRiteCeremonyDefinition ceremony(List<CardinalRiteCeremonyDefinition.Anchor> anchors) {
		return new CardinalRiteCeremonyDefinition(CardinalRiteCeremonyProfile.STANDARD, anchors, List.of(),
				List.of(), List.of(), List.of(), 200, "living_staff", 0, List.of(), 0, null, "");
	}
}

package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class CardinalRiteVirtualTargetingTest {
	@Test
	void selectsTheClosestMarkerAlongTheViewRayEvenWhenNoBlockExistsThere() {
		Vec3 eye = new Vec3(0.0D, 1.5D, 0.0D);
		Vec3 look = new Vec3(0.0D, 0.0D, 1.0D);
		List<Vec3> markers = List.of(
				new Vec3(0.2D, 1.5D, 4.5D),
				new Vec3(0.1D, 1.5D, 3.0D));

		assertEquals(1, CardinalRiteVirtualTargeting.closestTarget(
				eye, look, 5.5D, 0.75D, markers));
	}

	@Test
	void ignoresMarkersOutsideProjectionRangeOrAwayFromTheViewRay() {
		Vec3 eye = new Vec3(0.0D, 1.5D, 0.0D);
		Vec3 look = new Vec3(0.0D, 0.0D, 1.0D);

		assertEquals(-1, CardinalRiteVirtualTargeting.closestTarget(
				eye, look, 5.5D, 0.75D,
				List.of(new Vec3(0.0D, 1.5D, 6.0D), new Vec3(2.0D, 1.5D, 3.0D))));
	}

	@Test
	void selectsTheMarkerUnderTheCrosshairInsteadOfAnOffCenterMarkerInFront() {
		Vec3 eye = new Vec3(0.0D, 1.5D, 0.0D);
		Vec3 look = new Vec3(0.0D, 0.0D, 1.0D);
		List<Vec3> markers = List.of(
				new Vec3(0.55D, 1.5D, 2.0D),
				new Vec3(0.0D, 1.5D, 4.0D));

		assertEquals(1, CardinalRiteVirtualTargeting.closestTarget(
				eye, look, 5.5D, 0.75D, markers),
				"the centered rear marker is the one the player is actually aiming at");
	}

	@Test
	void completedFrontNodeBlocksACollinearNodeBehindIt() {
		List<Integer> raycastNodes = CardinalRiteSigilRules.raycastNodeIndices(1, 3);
		List<Vec3> markers = raycastNodes.stream()
				.map(index -> new Vec3(0.0D, 1.5D, 2.0D + index))
				.toList();

		int selectedListIndex = CardinalRiteVirtualTargeting.closestTarget(
				new Vec3(0.0D, 1.5D, 0.0D),
				new Vec3(0.0D, 0.0D, 1.0D),
				5.5D, 0.75D, markers);
		int selectedNode = raycastNodes.get(selectedListIndex);

		assertEquals(0, selectedNode, "the completed front marker must remain the ray blocker");
		assertFalse(CardinalRiteSigilRules.isActionableNode(selectedNode, 1, 3),
				"the blocker must absorb projection without advancing or punishing the sigil");
	}
}

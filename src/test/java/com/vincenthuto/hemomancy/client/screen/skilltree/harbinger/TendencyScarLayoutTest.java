package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TendencyScarLayoutTest {
	@Test
	void placesScarTiersBeyondTheirManipulationFamily() {
		Map<String, TendencyScarLayout.Point> points = TendencyScarLayout.arrange(
				500, 500,
				Map.of(EnumBloodTendency.ANIMUS, 260),
				List.of(
						new TendencyScarLayout.Node("heart", EnumBloodTendency.ANIMUS, 1, false),
						new TendencyScarLayout.Node("marrow", EnumBloodTendency.ANIMUS, 2, false),
						new TendencyScarLayout.Node("phoenix", EnumBloodTendency.ANIMUS, 3, false)));

		assertTrue(points.get("heart").y() < 240, "first scar sits beyond the outermost manipulation");
		assertEquals(50, points.get("heart").y() - points.get("marrow").y());
		assertEquals(50, points.get("marrow").y() - points.get("phoenix").y());
	}

	@Test
	void offsetsSideBranchesWithoutBreakingTheirTierRadius() {
		Map<String, TendencyScarLayout.Point> points = TendencyScarLayout.arrange(
				500, 500,
				Map.of(EnumBloodTendency.FERRIC, 200),
				List.of(
						new TendencyScarLayout.Node("anvil", EnumBloodTendency.FERRIC, 2, false),
						new TendencyScarLayout.Node("blood_honed", EnumBloodTendency.FERRIC, 2, true)));

		assertEquals(points.get("anvil").x(), points.get("blood_honed").x());
		assertEquals(40, Math.abs(points.get("anvil").y() - points.get("blood_honed").y()));
	}

	@Test
	void preservesAuthoredScarMovementWhileShiftingTheFamilyBeyondManipulations() {
		Map<String, TendencyScarLayout.Point> points = TendencyScarLayout.arrange(
				500, 500,
				Map.of(EnumBloodTendency.ANIMUS, 260),
				List.of(new TendencyScarLayout.Node(
						"heart", EnumBloodTendency.ANIMUS, 1, false, 492, 275)));

		assertEquals(new TendencyScarLayout.Point(512, 165), points.get("heart"));
	}
}

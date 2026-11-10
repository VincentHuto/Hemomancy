package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TemperScarLayoutTest {
	@Test
	void placesScarTiersBeyondTheirManipulationFamily() {
		Map<String, TemperScarLayout.Point> points = TemperScarLayout.arrange(
				500, 500,
				Map.of(EnumBloodTemper.ANIMUS, 260),
				List.of(
						new TemperScarLayout.Node("heart", EnumBloodTemper.ANIMUS, 1, false),
						new TemperScarLayout.Node("marrow", EnumBloodTemper.ANIMUS, 2, false),
						new TemperScarLayout.Node("phoenix", EnumBloodTemper.ANIMUS, 3, false)));

		assertTrue(points.get("heart").y() < 240, "first scar sits beyond the outermost manipulation");
		assertEquals(65, points.get("heart").y() - points.get("marrow").y());
		assertEquals(65, points.get("marrow").y() - points.get("phoenix").y());
	}

	@Test
	void tierOneScarsClearEveryFamilyByTheSameMargin() {
		Map<String, TemperScarLayout.Point> points = TemperScarLayout.arrange(
				500, 500,
				Map.of(EnumBloodTemper.ANIMUS, 410, EnumBloodTemper.DUCTILIS, 380),
				List.of(
						new TemperScarLayout.Node("heart", EnumBloodTemper.ANIMUS, 1, false, 480, 280),
						new TemperScarLayout.Node("feral", EnumBloodTemper.DUCTILIS, 1, false, 680, 480)));

		assertEquals(new TemperScarLayout.Point(500, 20), points.get("heart"));
		assertEquals(new TemperScarLayout.Point(950, 500), points.get("feral"));
	}

	@Test
	void offsetsSideBranchesWithoutBreakingTheirTierRadius() {
		Map<String, TemperScarLayout.Point> points = TemperScarLayout.arrange(
				500, 500,
				Map.of(EnumBloodTemper.FERRIC, 200),
				List.of(
						new TemperScarLayout.Node("anvil", EnumBloodTemper.FERRIC, 2, false),
						new TemperScarLayout.Node("blood_honed", EnumBloodTemper.FERRIC, 2, true)));

		assertEquals(points.get("anvil").x(), points.get("blood_honed").x());
		assertEquals(68, Math.abs(points.get("anvil").y() - points.get("blood_honed").y()));
	}

	@Test
	void preservesAuthoredScarMovementWhileShiftingTheFamilyBeyondManipulations() {
		Map<String, TemperScarLayout.Point> points = TemperScarLayout.arrange(
				500, 500,
				Map.of(EnumBloodTemper.ANIMUS, 260),
				List.of(new TemperScarLayout.Node(
						"heart", EnumBloodTemper.ANIMUS, 1, false, 492, 275)));

		assertEquals(new TemperScarLayout.Point(512, 165), points.get("heart"));
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class ScarTreeLayoutTest {
	@Test
	void tendencySectorsMatchTheManipulationClockwiseOrder() {
		List<ScarTreeLayout.Node> nodes = List.of(
				node("animus", EnumBloodTendency.ANIMUS, 1),
				node("flammeus", EnumBloodTendency.FLAMMEUS, 1),
				node("ductilis", EnumBloodTendency.DUCTILIS, 1),
				node("lux", EnumBloodTendency.LUX, 1),
				node("mortem", EnumBloodTendency.MORTEM, 1),
				node("congeatio", EnumBloodTendency.CONGEATIO, 1),
				node("ferric", EnumBloodTendency.FERRIC, 1),
				node("tenebris", EnumBloodTendency.TENEBRIS, 1));

		ScarTreeLayout.Result result = ScarTreeLayout.arrange(nodes);

		assertEquals(new ScarTreeLayout.Point(480, 280), result.pointFor("animus"));
		assertEquals(new ScarTreeLayout.Point(621, 339), result.pointFor("flammeus"));
		assertEquals(new ScarTreeLayout.Point(680, 480), result.pointFor("ductilis"));
		assertEquals(new ScarTreeLayout.Point(621, 621), result.pointFor("lux"));
		assertEquals(new ScarTreeLayout.Point(480, 680), result.pointFor("mortem"));
		assertEquals(new ScarTreeLayout.Point(339, 621), result.pointFor("congeatio"));
		assertEquals(new ScarTreeLayout.Point(280, 480), result.pointFor("ferric"));
		assertEquals(new ScarTreeLayout.Point(339, 339), result.pointFor("tenebris"));
	}

	@Test
	void tiersProgressOutwardWithinAFamily() {
		ScarTreeLayout.Result result = ScarTreeLayout.arrange(List.of(
				node("heart", EnumBloodTendency.ANIMUS, 1),
				node("marrow", EnumBloodTendency.ANIMUS, 2),
				node("phoenix", EnumBloodTendency.ANIMUS, 3)));

		assertEquals(new ScarTreeLayout.Point(480, 280), result.pointFor("heart"));
		assertEquals(new ScarTreeLayout.Point(480, 230), result.pointFor("marrow"));
		assertEquals(new ScarTreeLayout.Point(480, 180), result.pointFor("phoenix"));
	}

	@Test
	void builtInAuthoredBranchesUseTheCompactedTierRings() {
		ScarTreeLayout.Result result = ScarTreeLayout.arrange(List.of(
				node("hemomancy:scar_pyre", EnumBloodTendency.FLAMMEUS, 1),
				node("hemomancy:scar_sol", EnumBloodTendency.FLAMMEUS, 2),
				node("hemomancy:scar_corona", EnumBloodTendency.FLAMMEUS, 3),
				node("hemomancy:scar_thorn", EnumBloodTendency.FERRIC, 1),
				node("hemomancy:scar_anvil", EnumBloodTendency.FERRIC, 2),
				new ScarTreeLayout.Node("hemomancy:scar_blood_honed", EnumBloodTendency.FERRIC, 2, true),
				node("hemomancy:scar_crucible", EnumBloodTendency.FERRIC, 3)));

		assertEquals(new ScarTreeLayout.Point(601, 359), result.pointFor("hemomancy:scar_pyre"));
		assertEquals(new ScarTreeLayout.Point(647, 313), result.pointFor("hemomancy:scar_sol"));
		assertEquals(new ScarTreeLayout.Point(692, 268), result.pointFor("hemomancy:scar_corona"));
		assertEquals(new ScarTreeLayout.Point(280, 480), result.pointFor("hemomancy:scar_thorn"));
		assertEquals(new ScarTreeLayout.Point(230, 480), result.pointFor("hemomancy:scar_anvil"));
		assertEquals(new ScarTreeLayout.Point(230, 440), result.pointFor("hemomancy:scar_blood_honed"));
		assertEquals(new ScarTreeLayout.Point(180, 480), result.pointFor("hemomancy:scar_crucible"));
	}

	@Test
	void bloodHonedIsATierTwoSideBranchFromThorn() {
		ScarTreeLayout.Result result = ScarTreeLayout.arrange(List.of(
				node("scar_thorn", EnumBloodTendency.FERRIC, 1),
				node("scar_anvil", EnumBloodTendency.FERRIC, 2),
				new ScarTreeLayout.Node("scar_blood_honed", EnumBloodTendency.FERRIC, 2, true),
				node("scar_crucible", EnumBloodTendency.FERRIC, 3)));

		assertNotEquals(result.pointFor("scar_anvil"), result.pointFor("scar_blood_honed"));
		assertTrue(result.edges().contains(new ScarTreeLayout.Edge("scar_thorn", "scar_anvil")));
		assertTrue(result.edges().contains(new ScarTreeLayout.Edge("scar_anvil", "scar_crucible")));
		assertTrue(result.edges().contains(new ScarTreeLayout.Edge("scar_thorn", "scar_blood_honed")));
		assertEquals(3, result.edges().size());
	}

	@Test
	void authoredEditorPositionsAndParentsOverrideTheGeneratedFallback() {
		List<ScarTreeLayout.Node> nodes = List.of(
				node("heart", EnumBloodTendency.ANIMUS, 1),
				node("marrow", EnumBloodTendency.ANIMUS, 2));
		List<ScarTreeLayout.AuthoredNode> authored = List.of(
				new ScarTreeLayout.AuthoredNode("heart", 410, 205, List.of()),
				new ScarTreeLayout.AuthoredNode("marrow", 455, 135, List.of("heart")));

		ScarTreeLayout.Result result = ScarTreeLayout.arrange(nodes, authored);

		assertEquals(new ScarTreeLayout.Point(410, 205), result.pointFor("heart"));
		assertEquals(new ScarTreeLayout.Point(455, 135), result.pointFor("marrow"));
		assertEquals(List.of(new ScarTreeLayout.Edge("heart", "marrow")), result.edges());
	}

	private static ScarTreeLayout.Node node(String id, EnumBloodTendency tendency, int tier) {
		return new ScarTreeLayout.Node(id, tendency, tier, false);
	}
}

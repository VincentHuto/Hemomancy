package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CardinalRiteSigilPlacementRulesTest {
	@Test
	void shiftsTheWholeSigilOutwardWhenAnInwardNodeWouldOverlapTheBoundary() {
		Set<BlockPos> boundaryAnchors = Set.of(
				new BlockPos(3, 0, 0),
				new BlockPos(-3, 0, 0),
				new BlockPos(0, 0, 3),
				new BlockPos(0, 0, -3));
		List<IchorianSigilDefinition.Node> reservoirNodes = List.of(
				new IchorianSigilDefinition.Node(-1, 0),
				new IchorianSigilDefinition.Node(0, -1),
				new IchorianSigilDefinition.Node(1, 0),
				new IchorianSigilDefinition.Node(0, 1));

		BlockPos placement = CardinalRiteSigilPlacementRules.resolveSupportPlacement(
				new BlockPos(4, 0, 0), reservoirNodes, boundaryAnchors);

		assertEquals(new BlockPos(6, 0, 0), placement, "resolved socket");
		assertClear(boundaryAnchors,
				CardinalRiteSigilPlacementRules.footprint(placement, reservoirNodes));
	}

	@Test
	void separatelySpawnedSigilsCannotReuseAnOccupiedAnchor() {
		List<IchorianSigilDefinition.Node> nodes = List.of(
				new IchorianSigilDefinition.Node(0, 0),
				new IchorianSigilDefinition.Node(1, 0));
		Set<BlockPos> occupied = new HashSet<>();
		BlockPos first = CardinalRiteSigilPlacementRules.resolveSupportPlacement(
				new BlockPos(4, 0, 0), nodes, occupied);
		occupied.addAll(CardinalRiteSigilPlacementRules.footprint(first, nodes));

		BlockPos second = CardinalRiteSigilPlacementRules.resolveSupportPlacement(
				new BlockPos(4, 0, 0), nodes, occupied);

		assertEquals(new BlockPos(7, 0, 0), second, "second socket");
		assertClear(occupied, CardinalRiteSigilPlacementRules.footprint(second, nodes));
	}

	@Test
	void responseSigilFindsTheNearestClearSpaceBetweenBoundaryAndSupportNodes() {
		Set<BlockPos> occupied = new HashSet<>(Set.of(
				new BlockPos(3, 0, 0),
				new BlockPos(-3, 0, 0),
				new BlockPos(0, 0, 3),
				new BlockPos(0, 0, -3),
				new BlockPos(5, 0, 0)));
		List<IchorianSigilDefinition.Node> responseNodes = List.of(
				new IchorianSigilDefinition.Node(-2, 0),
				new IchorianSigilDefinition.Node(0, -2),
				new IchorianSigilDefinition.Node(2, 0),
				new IchorianSigilDefinition.Node(0, 2));

		BlockPos placement = CardinalRiteSigilPlacementRules.resolveNearestPlacement(
				BlockPos.ZERO, responseNodes, occupied);

		assertClear(occupied, CardinalRiteSigilPlacementRules.footprint(placement, responseNodes));
	}

	@Test
	void discoverySelectsTheMatchingSupportSocket() {
		ResourceLocation reservoir =
				ResourceLocation.fromNamespaceAndPath("hemomancy", "reservoir");
		List<ResourceLocation> supportSigils = List.of(
				ResourceLocation.fromNamespaceAndPath("hemomancy", "bastion"),
				reservoir);

		assertEquals(1,
				CardinalRiteSigilPlacementRules.matchingSigilIndex(reservoir, supportSigils),
				"matching support index");
	}

	@Test
	void discoveryReportsNoMatchWhenTheRiteHasNoMatchingSupportSocket() {
		ResourceLocation reservoir =
				ResourceLocation.fromNamespaceAndPath("hemomancy", "reservoir");

		assertEquals(-1,
				CardinalRiteSigilPlacementRules.matchingSigilIndex(reservoir, List.of()),
				"missing support index");
	}

	private static void assertDisjoint(Set<BlockPos> left, Set<BlockPos> right) {
		if (!java.util.Collections.disjoint(left, right)) {
			throw new AssertionError("expected disjoint anchor footprints but got " + left + " and " + right);
		}
	}

	private static void assertClear(Set<BlockPos> left, Set<BlockPos> right) {
		for (BlockPos first : left) {
			for (BlockPos second : right) {
				double horizontalDistance = Math.hypot(
						first.getX() - second.getX(), first.getZ() - second.getZ());
				if (horizontalDistance < CardinalRiteSigilPlacementRules.MINIMUM_NODE_CLEARANCE) {
					throw new AssertionError("expected separated target volumes but got "
							+ first + " and " + second);
				}
			}
		}
	}

	private static void assertEquals(Object expected, Object actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

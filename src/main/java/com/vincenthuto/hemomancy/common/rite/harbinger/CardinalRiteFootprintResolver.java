package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteFootprintRules;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class CardinalRiteFootprintResolver {
	private CardinalRiteFootprintResolver() {
	}

	public static float radius(CardinalRiteRecipe recipe, CardinalRiteFloorDefinition floor) {
		float fallbackRadius = recipe == null ? 0.0F
				: (float) CardinalRiteBoundaryLeashRules.ritualRadius(recipe.getRiteType().getSize());
		return radius(recipe, floor, fallbackRadius);
	}

	public static float radius(CardinalRiteRecipe recipe, CardinalRiteFloorDefinition floor,
			float fallbackRadius) {
		float floorRadius = floor == null ? 0.0F : floor.footprintRadius();
		if (recipe == null || recipe.getCeremony() == null) {
			return CardinalRiteFootprintRules.enclosingRadius(
					fallbackRadius, floorRadius, List.of(), List.of(), List.of());
		}

		List<BlockPos> anchors = recipe.getCeremony().anchors().stream()
				.map(com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition.Anchor::offset)
				.toList();
		List<BlockPos> supportSockets = new ArrayList<>();
		recipe.getCeremony().supportSockets().stream()
				.map(com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition.SupportSocket::offset)
				.forEach(supportSockets::add);
		List<BlockPos> sigilPoints = new ArrayList<>();
		List<CardinalRiteInteractionHandler.SigilPlacement> supportPlacements =
				CardinalRiteInteractionHandler.supportSigils(recipe);
		for (CardinalRiteInteractionHandler.SigilPlacement placement : supportPlacements) {
			supportSockets.add(new BlockPos(placement.x(), 0, placement.z()));
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(placement.id());
			if (sigil == null) continue;
			for (IchorianSigilDefinition.Node node : sigil.nodes()) {
				sigilPoints.add(new BlockPos(
						placement.x() + (int) Math.round(node.x()), 0,
						placement.z() + (int) Math.round(node.z())));
			}
		}
		for (IchorianSigilDefinition sigil : IchorianSigilRegistry.all()) {
			sigilPoints.addAll(CardinalRiteInteractionHandler.resolvedResponseSigilPoints(
					recipe.getCeremony().anchors(), supportPlacements, sigil));
		}
		return CardinalRiteFootprintRules.enclosingRadius(
				fallbackRadius, floorRadius, anchors, supportSockets, sigilPoints);
	}
}

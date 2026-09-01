package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteFootprintRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRiteResponseFootprintTest {
	@Test
	void collisionShiftedResponseSigilExpandsTheRitualFootprint() {
		List<CardinalRiteCeremonyDefinition.Anchor> anchors = new ArrayList<>();
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				anchors.add(new CardinalRiteCeremonyDefinition.Anchor(
						x, 1, z, 0, anchors.size()));
			}
		}
		IchorianSigilDefinition response = new IchorianSigilDefinition(
				id("response"), IchorianSigilDefinition.Kind.RESPONSE,
				1, 0xFFFFFF, "Response", "", 1, 0,
				List.of(new IchorianSigilDefinition.Node(0.0D, 0.0D)));

		List<BlockPos> responsePoints =
				List.copyOf(CardinalRiteSigilPlacementRules.resolvedFootprint(
						BlockPos.ZERO, response.nodes(),
						anchors.stream()
								.map(anchor -> new BlockPos(anchor.x(), 0, anchor.z()))
								.collect(java.util.stream.Collectors.toSet())));
		float footprint = CardinalRiteFootprintRules.enclosingRadius(
				4.5F, 4.5F,
				anchors.stream().map(CardinalRiteCeremonyDefinition.Anchor::offset).toList(),
				List.of(), responsePoints);

		assertEquals(List.of(new BlockPos(-5, 0, 0)), responsePoints);
		assertEquals(5.75F, footprint, 0.0001F);
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}
}

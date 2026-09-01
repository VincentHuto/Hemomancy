package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.EnumNodeShape;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;

import java.util.Set;
import java.util.function.Function;

final class ManipulationNodePresentation {
	private ManipulationNodePresentation() {
	}

	static int borderColor(EnumManipulationRank rank) {
		if (rank == null) return 0;
		return switch (rank) {
			case HUMILIS -> 0;
			case MEDIOCRITAS -> 0xFFCD7F32;
			case SUMMA -> 0xFFA7ADB2;
			case MAGISTER -> 0xFFFFC43D;
			case PERFECTUS -> 0xFFD94CFF;
		};
	}

	static EnumNodeShape shape(String manipulationId, EnumNodeShape authoredShape) {
		return ManipulationFamilyRegistry.family(manipulationId)
				.filter(family -> family.baselineId().equals(manipulationId))
				.map(family -> EnumNodeShape.OCTAGON)
				.orElse(authoredShape);
	}

	static EnumManipulationRank familyBorderRank(String manipulationId, Set<String> available,
			Function<String, EnumManipulationRank> rankById) {
		var family = ManipulationFamilyRegistry.family(manipulationId).orElse(null);
		if (family == null) return rankById.apply(manipulationId);
		EnumManipulationRank highest = rankById.apply(family.baselineId());
		for (var form : family.forms()) {
			if (!available.contains(form.id())) continue;
			EnumManipulationRank candidate = rankById.apply(form.id());
			if (candidate != null && (highest == null || candidate.ordinal() > highest.ordinal())) highest = candidate;
		}
		return highest;
	}

	static int masteryPipX(int index, int halfNode) {
		return index == 1 ? halfNode : index == 3 ? -halfNode : 0;
	}

	static int masteryPipY(int index, int halfNode) {
		return index == 0 ? -halfNode : index == 2 ? halfNode : 0;
	}
}

package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import java.util.List;
import java.util.Set;

public final class BloodLustLineageRules {
	private static final Set<String> KNOWN = Set.of("barbed", "chitinite", "prismatic");
	public record InheritedTrait(float thornsDamage, int bloodLossTicks, int poisonTicks,
			double toughness, float projectileReduction, int speedTicks, int blindnessTicks,
			int confusionTicks) {
		public static final InheritedTrait NONE = new InheritedTrait(0, 0, 0, 0, 0, 0, 0, 0);
	}

	private BloodLustLineageRules() {
	}

	public static String uniformLineage(List<String> lineages) {
		if (lineages.size() != 4 || !KNOWN.contains(lineages.getFirst())) return "";
		String lineage = lineages.getFirst();
		return lineages.stream().allMatch(lineage::equals) ? lineage : "";
	}

	public static int platingLevel(String lineage) {
		return switch (lineage) {
			case "prismatic" -> 1;
			case "barbed" -> 2;
			default -> 3;
		};
	}

	public static InheritedTrait inheritedTrait(String lineage) {
		return switch (lineage) {
			case "barbed" -> new InheritedTrait(1, 30, 40, 0, 0, 0, 0, 0);
			case "chitinite" -> new InheritedTrait(0, 0, 0, 1, .125f, 0, 0, 0);
			case "prismatic" -> new InheritedTrait(0, 0, 0, 0, 0, 30, 20, 40);
			default -> InheritedTrait.NONE;
		};
	}
}

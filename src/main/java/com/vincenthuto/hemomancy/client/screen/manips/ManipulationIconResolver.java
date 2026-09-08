package com.vincenthuto.hemomancy.client.screen.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public final class ManipulationIconResolver {
	public static final ResourceLocation MEMORY_BASE = Hemomancy.rloc("textures/item/memories/memory_blank.png");
	private static final ResourceLocation CONJURE_BASE = Hemomancy.rloc("textures/item/memories/memory_conjure_base.png");

	private ManipulationIconResolver() {
	}

	public static ResourceLocation base(String manipulationId) {
		if (manipulationId.startsWith("conjure_")) return CONJURE_BASE;
		if (ManipulationRetirementRules.isRetiredManipulation(manipulationId)) return MEMORY_BASE;
		var manipulation = ManipulationInit.MANIPS_TYPE_REGISTRY.get(Hemomancy.rloc(manipulationId));
		if (manipulation == null || manipulation.getRank() == EnumManipulationRank.HUMILIS) return MEMORY_BASE;
		return Hemomancy.rloc("textures/item/memories/memory_rank_"
				+ manipulation.getRank().name().toLowerCase(Locale.ROOT) + "_base.png");
	}

	public static ResourceLocation overlay(String manipulationId) {
		String texture = switch (manipulationId) {
			case "conjure_axe" -> "memory_living_axe_overlay";
			case "conjure_blade" -> "memory_living_blade_overlay";
			case "conjure_claws" -> "memory_living_claws_overlay";
			case "conjure_crossbow" -> "memory_living_crossbow_overlay";
			case "conjure_flail" -> "memory_living_flail_overlay";
			case "conjure_spear" -> "memory_living_spear_overlay";
			case "conjure_staff" -> "memory_living_staff_overlay";
			case "conjure_torch" -> "memory_living_torch_overlay";
			case "conjure_sickle" -> "memory_living_sickle_overlay";
			case "vigil_of_glass" -> "memory_unclosing_eye_overlay";
			case "hematic_ballast" -> "memory_iron_retort_overlay";
			default -> "memory_" + manipulationId + "_overlay";
		};
		return Hemomancy.rloc("textures/item/memories/" + texture + ".png");
	}
}

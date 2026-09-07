package com.vincenthuto.hemomancy.client.screen.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.ResourceLocation;

public final class ManipulationIconResolver {
	public static final ResourceLocation MEMORY_BASE = Hemomancy.rloc("textures/item/memories/memory_blank.png");

	private ManipulationIconResolver() {
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

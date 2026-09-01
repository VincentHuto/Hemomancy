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
			case "ironhearted" -> "memory_iron_retort_overlay";
			case "crimson_coronation" -> "memory_blood_aneurysm_overlay";
			case "sovereign_instinct" -> "memory_summon_avatar_overlay";
			case "synaptic_storm" -> "memory_activation_potential_overlay";
			case "living_circuit" -> "memory_conductive_mark_overlay";
			case "white_verdict" -> "memory_prismatic_reproof_overlay";
			case "vigil_of_glass" -> "memory_unclosing_eye_overlay";
			case "furnace_veins" -> "memory_vitric_combustion_overlay";
			case "phoenix_debt" -> "memory_sanguine_ignition_overlay";
			case "absolute_stillness" -> "memory_endless_hour_overlay";
			case "rimebound_sentence" -> "memory_glacial_bastion_overlay";
			case "hematic_ballast" -> "memory_iron_retort_overlay";
			case "iron_choir" -> "memory_sanguine_magnetism_overlay";
			case "funeral_bell" -> "memory_grave_debt_overlay";
			case "carrion_communion" -> "memory_bloom_of_rot_overlay";
			case "lignum_mortis" -> "memory_hemorrhage_overlay";
			case "penumbral_drift" -> "memory_void_shroud_overlay";
			case "eclipse_well" -> "memory_black_veil_covenant_overlay";
			default -> "memory_" + manipulationId + "_overlay";
		};
		return Hemomancy.rloc("textures/item/memories/" + texture + ".png");
	}
}

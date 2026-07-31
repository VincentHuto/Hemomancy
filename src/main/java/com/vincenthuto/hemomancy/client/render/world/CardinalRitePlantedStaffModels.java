package com.vincenthuto.hemomancy.client.render.world;

import java.util.List;

/** Selects the fistless model used only by a staff planted in a Cardinal Rite. */
public final class CardinalRitePlantedStaffModels {
	private static final String[] MODELS_BY_VISUAL = {
			"living_staff_planted",
			"living_staff_serpent_planted",
			"living_staff_leech_planted",
			"living_staff_fungal_planted",
			"living_staff_pests_planted",
			"living_staff_chitinite_planted",
			"living_staff_pests_planted",
			"living_staff_fungal_planted",
			"living_staff_chitinite_planted",
			"living_staff_worn_vow_planted",
			"living_staff_barbed_fitting_planted",
			"living_staff_chitinite_fitting_planted",
			"living_staff_prismatic_fitting_planted",
			"living_staff_crimson_vestment_planted",
			"living_staff_monolithic_frame_planted",
			"living_staff_assumed_limb_planted"
	};
	private static final List<String> UNIQUE_MODELS = List.of(
			"living_staff_planted",
			"living_staff_serpent_planted",
			"living_staff_leech_planted",
			"living_staff_fungal_planted",
			"living_staff_pests_planted",
			"living_staff_chitinite_planted",
			"living_staff_worn_vow_planted",
			"living_staff_barbed_fitting_planted",
			"living_staff_chitinite_fitting_planted",
			"living_staff_prismatic_fitting_planted",
			"living_staff_crimson_vestment_planted",
			"living_staff_monolithic_frame_planted",
			"living_staff_assumed_limb_planted");

	private CardinalRitePlantedStaffModels() {
	}

	public static String modelName(int visual) {
		return visual >= 0 && visual < MODELS_BY_VISUAL.length
				? MODELS_BY_VISUAL[visual]
				: MODELS_BY_VISUAL[0];
	}

	public static List<String> uniqueModelNames() {
		return UNIQUE_MODELS;
	}
}

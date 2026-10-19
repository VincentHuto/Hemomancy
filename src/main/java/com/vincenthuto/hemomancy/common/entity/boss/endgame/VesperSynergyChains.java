package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import java.util.List;

/**
 * Authored cross-school chains for the Open act: the first cast sets something up and the second, from another
 * school, cashes it in. This is Vesper showing mastery, not a random pick of two spells.
 */
public final class VesperSynergyChains {
	public record Chain(String first, String second, int gapTicks) {
	}

	public static final List<Chain> ALL = List.of(
			// Magnetised iron conducts: pull and tag, then the bolt runs through the tagged target.
			new Chain("sanguine_magnetism", "synaptic_jolt", 10),
			// Frozen in place, then bled dry.
			new Chain("glacial_grasp", "exsanguinate", 12),
			// Set alight, then the flare detonates the burning blood.
			new Chain("sanguine_ignition", "hematic_flare", 10),
			// Steps out of the dark behind the target, then a blood shot point-blank.
			new Chain("umbral_step", "blood_aneurysm", 6),
			// A mark the lightning seeks, then the frost pulse locks the marked target down.
			new Chain("conductive_mark", "cryogenic_pulse", 14),
			// Launched by the updraft, then needled out of the air.
			new Chain("soaring_updraft", "blood_needle_fan", 8),
			// Pulled into the well, then the bell's toll throws them out.
			new Chain("eclipse_well", "funeral_bell", 16),
			// Bound in threads, then the verdict falls on the bound.
			new Chain("blood_binding", "white_verdict", 12),
			// Iron spikes pen them in, then the searing lash sweeps the pen.
			new Chain("ferric_spikes", "searing_lash", 10),
			// Stillness, then a shadow rend through the still target.
			new Chain("absolute_stillness", "umbral_rend", 8));

	private VesperSynergyChains() {
	}
}

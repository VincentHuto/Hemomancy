package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Player-readable ceremony objectives kept independent from HUD rendering.
 */
public final class CardinalRiteChecklist {
	private CardinalRiteChecklist() {
	}

	public static List<Component> inscription(int optionalSigils, int completedSigils,
			int allies, boolean mediumReady) {
        return inscription(optionalSigils, completedSigils, 0, 0, allies, 0, mediumReady);
    }

    public static List<Component> inscription(int optionalSigils, int completedOptional,
            int requiredSigils, int completedRequired, int availableAllies, int requiredAllies,
            boolean mediumReady) {
        List<Component> lines = new ArrayList<>();
        if (requiredSigils > 0) lines.add(Component.translatable(
                "message.hemomancy.cardinal_rite_checklist.required_sigils", completedRequired, requiredSigils));
        if (optionalSigils > 0) lines.add(Component.translatable(
                "message.hemomancy.cardinal_rite_checklist.optional_sigils", completedOptional, optionalSigils));
        lines.add(requiredAllies > 0
                ? Component.translatable("message.hemomancy.cardinal_rite_checklist.required_allies",
                        availableAllies, requiredAllies)
                : Component.translatable("message.hemomancy.cardinal_rite_checklist.optional_allies_assigned",
                        availableAllies));
        lines.add(Component.translatable(mediumReady ? "message.hemomancy.cardinal_rite_checklist.focus_ready"
                : "message.hemomancy.cardinal_rite_checklist.focus_medium_mismatch"));
        boolean ready = mediumReady && completedRequired >= requiredSigils && availableAllies >= requiredAllies;
        lines.add(Component.translatable(ready
                ? "message.hemomancy.cardinal_rite_checklist.project_into_the_daemon_to_begin"
                : "message.hemomancy.cardinal_rite_checklist.fulfil_required_preparations_before_sealing"));
        return List.copyOf(lines);
    }

	public static List<Component> inscription(int optionalSigils, int allies, boolean mediumReady) {
		return inscription(optionalSigils, 0, allies, mediumReady);
	}

	public static Component ordealObjective(String wave) {
		if (wave == null) return Component.translatable("message.hemomancy.cardinal_rite_checklist.endure_the_ordeal");
		if (CardinalRiteWaveRules.objective(wave) != CardinalRiteWaveRules.Objective.SURVIVE)
			return CardinalRiteWaveRules.deadlineHint(wave);
		return Component.translatable(switch (wave) {
			case "bloodlicker_siphon" -> "message.hemomancy.cardinal_rite_checklist.defend_the_boundary_from_bloodlickers";
			case "fargone_dive" -> "message.hemomancy.cardinal_rite_checklist.survive_the_fargone_assault";
			case "rogue_will" -> "message.hemomancy.cardinal_rite_checklist.drive_the_rogue_will_from_the_boundary";
			default -> "message.hemomancy.cardinal_rite_checklist.endure_the_current_ordeal";
		});
	}
}

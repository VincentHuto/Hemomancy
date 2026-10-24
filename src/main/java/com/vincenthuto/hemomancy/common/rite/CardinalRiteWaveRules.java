package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.network.chat.Component;

/** Shared deadline and objective contract for the ordeal controller and its guidance. */
public final class CardinalRiteWaveRules {
    public static final int DEADLINE_TICKS = 360;

    public enum Objective { SURVIVE, TRACE, DEFEAT }

    private CardinalRiteWaveRules() {}

    public static Objective objective(String wave) {
        if (wave != null && (wave.startsWith("discover_") || wave.equals("response_sigil")
                || wave.equals("covenant_pressure"))) return Objective.TRACE;
        return "false_omens".equals(wave) ? Objective.DEFEAT : Objective.SURVIVE;
    }

    public static boolean missedRequiredObjective(String wave, boolean traced, boolean threatsRemain) {
        return switch (objective(wave)) {
            case TRACE -> !traced;
            case DEFEAT -> threatsRemain;
            case SURVIVE -> false;
        };
    }

    public static Component deadlineHint(String wave) {
        int seconds = DEADLINE_TICKS / 20;
        return Component.translatable(switch (objective(wave)) {
            case TRACE -> "message.hemomancy.cardinal_rite_wave_rules.trace_within_or_the_rite_fails";
            case DEFEAT -> "message.hemomancy.cardinal_rite_wave_rules.defeat_the_puppeteers_within_or_the_rite_fails";
            case SURVIVE -> "message.hemomancy.cardinal_rite_wave_rules.survive_or_defeat_the_threats";
        }, seconds);
    }
}

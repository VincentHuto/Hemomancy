package com.vincenthuto.hemomancy.common.rite;

/** Shared deadline and objective contract for the ordeal controller and its guidance. */
public final class CardinalRiteWaveRules {
    public static final int DEADLINE_TICKS = 360;

    public enum Objective { SURVIVE, TRACE, DEFEAT }

    private CardinalRiteWaveRules() {}

    public static Objective objective(String wave) {
        if (wave != null && (wave.startsWith("discover_") || wave.equals("response_sigil"))) return Objective.TRACE;
        return "false_omens".equals(wave) ? Objective.DEFEAT : Objective.SURVIVE;
    }

    public static boolean missedRequiredObjective(String wave, boolean traced, boolean threatsRemain) {
        return switch (objective(wave)) {
            case TRACE -> !traced;
            case DEFEAT -> threatsRemain;
            case SURVIVE -> false;
        };
    }

    public static String deadlineHint(String wave) {
        int seconds = DEADLINE_TICKS / 20;
        return switch (objective(wave)) {
            case TRACE -> "Trace within " + seconds + "s or the rite fails";
            case DEFEAT -> "Defeat the puppeteers within " + seconds + "s or the rite fails";
            case SURVIVE -> "Survive " + seconds + "s or defeat the threats";
        };
    }
}

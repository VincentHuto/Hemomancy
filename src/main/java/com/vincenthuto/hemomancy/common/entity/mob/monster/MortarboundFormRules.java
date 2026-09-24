package com.vincenthuto.hemomancy.common.entity.mob.monster;

public final class MortarboundFormRules {
    public static final int TRANSITION_TICKS = 20;

    public enum Form { EMBEDDED, EMERGING, ACTIVE, RETREATING }
    public record State(Form form, int ticks) { }

    private MortarboundFormRules() { }

    public static State tick(State state, boolean alert, boolean quiet) {
        return switch (state.form()) {
            case EMBEDDED -> alert ? new State(Form.EMERGING, 0) : state;
            case EMERGING -> state.ticks() + 1 >= TRANSITION_TICKS
                    ? new State(Form.ACTIVE, 0) : new State(Form.EMERGING, state.ticks() + 1);
            case ACTIVE -> quiet ? new State(Form.RETREATING, 0) : state;
            case RETREATING -> alert ? new State(Form.EMERGING, TRANSITION_TICKS - state.ticks())
                    : state.ticks() + 1 >= TRANSITION_TICKS ? new State(Form.EMBEDDED, 0)
                    : new State(Form.RETREATING, state.ticks() + 1);
        };
    }

    public static boolean canHunt(State state) { return state.form() == Form.ACTIVE; }

    public static float emergence(State state) {
        return switch (state.form()) {
            case EMBEDDED -> 0F;
            case ACTIVE -> 1F;
            case EMERGING -> (float)state.ticks() / TRANSITION_TICKS;
            case RETREATING -> 1F - (float)state.ticks() / TRANSITION_TICKS;
        };
    }
}

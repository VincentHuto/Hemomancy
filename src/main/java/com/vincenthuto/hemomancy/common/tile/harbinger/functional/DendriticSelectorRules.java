package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

/**
 * Slot geometry and timing for the Dendritic Distributor's selector axon. Angles are degrees about +Y from
 * the model's -z axis and mirror SLOTS in tools/model_export/dendritic_distributor.py.
 */
public final class DendriticSelectorRules {
    public static final int SLOT_COUNT = 7;
    public static final float FIRST_SLOT_DEGREES = -67.5F;
    public static final float SLOT_STEP_DEGREES = 22.5F;
    /** Largest swing per tick, so a distant selection sweeps across the lobes instead of snapping. */
    public static final float MAX_SWING_DEGREES = 9.0F;
    public static final float SETTLED_DEGREES = 0.5F;
    public static final int IMPULSE_PERIOD_TICKS = 40;
    public static final int IMPULSE_TRAVEL_TICKS = 10;
    public static final int FLARE_TICKS = 8;
    public static final float RESTING_GLOW = 0.78F;

    private DendriticSelectorRules() {
    }

    public static int clampSlot(int slot) {
        return Math.max(0, Math.min(SLOT_COUNT - 1, slot));
    }

    public static float slotDegrees(int slot) {
        return FIRST_SLOT_DEGREES + clampSlot(slot) * SLOT_STEP_DEGREES;
    }

    /** One tick of eased travel toward the target angle, capped at {@link #MAX_SWING_DEGREES}. */
    public static float approach(float current, float target) {
        float delta = target - current;
        if (Math.abs(delta) <= SETTLED_DEGREES) {
            return target;
        }
        return current + Math.max(-MAX_SWING_DEGREES, Math.min(MAX_SWING_DEGREES, delta * 0.35F));
    }

    public static boolean settled(float current, float target) {
        return Math.abs(target - current) <= SETTLED_DEGREES;
    }

    /** Impulse travel from soma (0) to bouton (1) along the selected dendrite, or -1 between impulses. */
    public static float impulseProgress(long gameTime, float partialTick) {
        float phase = phase(gameTime, partialTick);
        return phase < IMPULSE_TRAVEL_TICKS ? phase / IMPULSE_TRAVEL_TICKS : -1.0F;
    }

    /** Brightness of the selected bouton: a resting glow that flares as each impulse lands. */
    public static float glow(long gameTime, float partialTick) {
        float sinceLanding = phase(gameTime, partialTick) - IMPULSE_TRAVEL_TICKS;
        if (sinceLanding < 0 || sinceLanding >= FLARE_TICKS) {
            return RESTING_GLOW;
        }
        return 1.0F - (1.0F - RESTING_GLOW) * sinceLanding / FLARE_TICKS;
    }

    // gameTime stays a long until after the modulo so large world ages keep sub-tick precision.
    private static float phase(long gameTime, float partialTick) {
        return Math.floorMod(gameTime, IMPULSE_PERIOD_TICKS) + partialTick;
    }
}

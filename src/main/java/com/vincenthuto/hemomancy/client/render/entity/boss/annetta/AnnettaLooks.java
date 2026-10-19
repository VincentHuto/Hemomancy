package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity.EncounterState;

/** Which of Annetta's human looks an encounter state shows. Reads only state, never the held item, so old saves render. */
public final class AnnettaLooks {
    public enum Look { COWERING, PHASE_ONE, PURGED_SUPPORT, PURGED_RESOLVED }

    private static final float MIN_SWELL = 0.15F;

    private AnnettaLooks() {}

    public static Look look(EncounterState state) {
        return look(state, 0.0F);
    }

    /** During the purge her hair turns white once the Draught has drained half the red out of it. */
    public static Look look(EncounterState state, float hairDrain) {
        return switch (state) {
            case COWERING -> Look.COWERING;
            case PHASE_ONE, BREAKING -> Look.PHASE_ONE;
            case PURGING -> hairDrain >= 0.5F ? Look.PURGED_SUPPORT : Look.COWERING;
            case CURED_SUPPORT -> Look.PURGED_SUPPORT;
            case RESOLVED -> Look.PURGED_RESOLVED;
        };
    }

    public static boolean purged(Look look) {
        return look == Look.PURGED_SUPPORT || look == Look.PURGED_RESOLVED;
    }

    public static boolean showsTick(Look look) {
        return look == Look.PHASE_ONE;
    }

    /** Stand-in driver until the Harbinger fight owns the tick: it fattens as Phase 1 health falls. */
    public static float tickSwell(float health, float maxHealth) {
        float lost = 1.0F - Math.max(0.0F, Math.min(1.0F, health / maxHealth));
        return MIN_SWELL + (1.0F - MIN_SWELL) * lost;
    }
}

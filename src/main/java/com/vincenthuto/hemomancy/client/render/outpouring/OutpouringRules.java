package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import java.util.Locale;

/**
 * Timing, rank scaling and roster for the cast outpouring: the caster's blood leaving the vascular section. Pure, so the
 * JVM tests pin the timeline. Visual timing never gates gameplay; the server has already resolved the cast.
 */
public final class OutpouringRules {
    /** FULL outpouring; WOUND keeps veins and the exit wound for spells that draw their own blood on the body; TOGGLE is a passive switch. */
    public enum Mode { FULL, WOUND, TOGGLE }
    public enum Stage { BUILD, PEAK, AFTERMATH, WITHDRAW, DONE }
    /** How much of the outpouring a viewer gets, by particle setting and distance. */
    public enum Detail { FULL, REDUCED, VEINS, NONE }
    public record Profile(double volume, double reach, double wildness, int markLife, int maxMarks, double veinReach, int aftermath) {}
    public record Pulse(float strength, Stage stage) {}

    private static final Profile[] PROFILES = {
            new Profile(.2, .6, .1, 60, 2, .25, 20),
            new Profile(.4, 1.0, .3, 100, 6, .4, 30),
            new Profile(.65, 2.0, .55, 160, 14, .6, 40),
            new Profile(.85, 3.5, .8, 240, 26, .8, 50),
            new Profile(1.0, 5.0, 1.0, 320, 40, 1.0, 60)};
    private static final String PREVIEW = "preview_section_";
    /** Ticks the release holds at full strength before the aftermath starts. */
    static final int PEAK_TICKS = 8;
    static final int PASSIVE_AFTERMATH = 14;
    static final float PASSIVE_SCALE = .55f;

    private OutpouringRules() {}

    public static Profile profile(EnumManipulationRank rank) {
        return PROFILES[rank == null ? 0 : rank.ordinal()];
    }

    public static boolean woundOnly(String id) {
        if (id == null) return false;
        return id.equals("hematic_riposte") || id.equals("crimson_coronation") || id.startsWith("summon_avatar")
                || LivingStaffWeaponFormRules.isStaffWeaponFormManip(id);
    }

    public static Mode mode(String id, EnumManipulationType type) {
        if (woundOnly(id)) return Mode.WOUND;
        return type == EnumManipulationType.PASSIVE ? Mode.TOGGLE : Mode.FULL;
    }

    /**
     * Strength and stage at {@code sincePhase} ticks into the packet's phase. Charges build with their charge, channels ramp to
     * a living near-peak, every release peaks then fades over the rank's aftermath, and a cancel draws {@code carried} back in.
     */
    public static Pulse pulse(EnumManipulationType type, CastPhase phase, float charge, float sincePhase, float carried,
                              EnumManipulationRank rank) {
        int r = rank == null ? 0 : rank.ordinal();
        if (phase == CastPhase.CANCEL) {
            if (sincePhase >= 6) return new Pulse(0, Stage.DONE);
            return new Pulse(carried * (1 - clamp(sincePhase / 6)), Stage.WITHDRAW);
        }
        if (phase.sustained()) {
            if (type == EnumManipulationType.CHARGED) return new Pulse(.15f + .6f * clamp(charge), Stage.BUILD);
            return new Pulse(.8f * clamp(sincePhase / (10 + 2.5f * r)), Stage.BUILD);
        }
        boolean passive = type == EnumManipulationType.PASSIVE;
        int aftermath = passive ? PASSIVE_AFTERMATH : profile(rank).aftermath();
        float scale = passive ? PASSIVE_SCALE : 1;
        if (sincePhase < 2) return new Pulse(scale * (carried + (1 - carried) * sincePhase / 2), Stage.PEAK);
        if (sincePhase < PEAK_TICKS) return new Pulse(scale, Stage.PEAK);
        if (sincePhase < PEAK_TICKS + aftermath)
            return new Pulse(scale * (1 - smooth((sincePhase - PEAK_TICKS) / aftermath)), Stage.AFTERMATH);
        return new Pulse(0, Stage.DONE);
    }

    public static Detail detail(int particleSetting, double distanceSq) {
        if (distanceSq > 48 * 48) return Detail.NONE;
        if (particleSetting == 2 || distanceSq > 32 * 32) return Detail.VEINS;
        if (particleSetting == 1 || distanceSq > 24 * 24) return Detail.REDUCED;
        return Detail.FULL;
    }

    /** The section a preview packet asks for, or null when the id is not a preview id. */
    public static EnumVeinSections previewSection(String id) {
        if (id == null || !id.startsWith(PREVIEW)) return null;
        try {
            return EnumVeinSections.valueOf(id.substring(PREVIEW.length()).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException unknown) {
            return null;
        }
    }

    public static String previewId(EnumVeinSections section) {
        return PREVIEW + section.name().toLowerCase(Locale.ROOT);
    }

    /** How far frost, scorch or corrosion has crept over the caster's own body from the section (0..1). */
    public static double bodyContact(Pulse pulse, EnumManipulationRank rank) {
        double peak = (rank == null ? 0 : rank.ordinal()) / 4.0 * .8 + .2;
        return switch (pulse.stage()) {
            case PEAK -> peak;
            case AFTERMATH -> peak * pulse.strength();
            default -> 0;
        };
    }

    static float clamp(float value) { return Math.clamp(value, 0f, 1f); }
    static float smooth(float t) { t = clamp(t); return t * t * (3 - 2 * t); }
}

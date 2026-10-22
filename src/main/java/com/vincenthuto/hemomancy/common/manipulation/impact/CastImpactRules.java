package com.vincenthuto.hemomancy.common.manipulation.impact;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import javax.annotation.Nullable;

/**
 * Pure rules for cast impacts: how a school hit lands, how the cue packs into a visual packet's count, how often a
 * target may show one, and the 10-tick flash, burst and fray every impact plays.
 */
public final class CastImpactRules {
    public enum Delivery { MELEE, PROJECTILE, AREA, TETHER }
    public record Decoded(EnumBloodTendency primary, @Nullable EnumBloodTendency secondary, Delivery delivery, int weight) {}

    public static final int FLASH_END = 2, BURST_END = 6, LIFE = 10, THROTTLE = 5;
    private static final EnumBloodTendency[] SCHOOLS = EnumBloodTendency.values();

    private CastImpactRules() {}

    /** A projectile's own hit beats everything; weapon and Will strikes are melee; casts take their shape from purpose. */
    public static Delivery delivery(boolean projectile, boolean weapon, @Nullable CastPurpose purpose) {
        if (projectile) return Delivery.PROJECTILE;
        if (weapon) return Delivery.MELEE;
        if (purpose == null) return Delivery.TETHER;
        return switch (purpose) {
            case PROJECTILE -> Delivery.PROJECTILE;
            case AREA, SELF, CONSTRUCTION -> Delivery.AREA;
            case CONJURATION, TRAVEL -> Delivery.MELEE;
            case TARGETED -> Delivery.TETHER;
        };
    }

    /** Bits 0-3 primary, 4-7 secondary + 1 (0 for none), 8-9 delivery, 10-11 weight; bit 12 keeps it positive and non-zero. */
    public static int encode(EnumBloodTendency primary, @Nullable EnumBloodTendency secondary, Delivery delivery, int weight) {
        return 1 << 12 | Math.clamp(weight, 0, 2) << 10 | delivery.ordinal() << 8
                | (secondary == null ? 0 : secondary.ordinal() + 1) << 4 | primary.ordinal();
    }

    public static Decoded decode(int count) {
        int primary = Math.min(count & 15, SCHOOLS.length - 1), secondary = count >> 4 & 15;
        return new Decoded(SCHOOLS[primary], secondary == 0 || secondary > SCHOOLS.length ? null : SCHOOLS[secondary - 1],
                Delivery.values()[count >> 8 & 3], Math.min(2, count >> 10 & 3));
    }

    /** Light under 3 damage, heavy from 10. */
    public static int weight(float damage) { return damage < 3 ? 0 : damage < 10 ? 1 : 2; }

    public static double scale(int weight) { return weight <= 0 ? .75 : weight == 1 ? 1 : 1.35; }

    /** One impact per target every {@link #THROTTLE} ticks; heavy hits always read. {@code last} is -1 when none showed. */
    public static boolean allow(long last, long now, int weight) {
        return weight >= 2 || last < 0 || now - last >= THROTTLE;
    }

    /** The contact flash: full at once, gone by the end of the flash phase. */
    public static float flash(float age) { return 1 - HemoEasing.smoothstep(age / (FLASH_END + 1f)); }

    /** Burst growth: 0 at contact, 1 when the burst is fully thrown. */
    public static float burst(float age) { return HemoEasing.smoothstep(age / BURST_END); }

    /** Whole-impact opacity: holds through the burst and frays out over the rest of the life. */
    public static float presence(float age) {
        return age <= BURST_END ? 1 : 1 - HemoEasing.smoothstep((age - BURST_END) / (LIFE - BURST_END));
    }
}

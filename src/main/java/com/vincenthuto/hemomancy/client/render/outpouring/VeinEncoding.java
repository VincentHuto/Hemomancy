package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;

/**
 * Per-player vein inputs packed into the three ints a model draw already carries (colour, overlay, light), because entity
 * layers batch every player into one buffer and uniforms cannot differ between them. The cast_veins shader unpacks them.
 * Colour ARGB: A fade, R strength, G school·8 + section, B secondary·8 + rank (secondary 8 = none).
 * Overlay: u contact·1000, v seed. Light: u bit 0 when the off hand casts too, bit 1 when the main hand is the left.
 */
public final class VeinEncoding {
    public static final int SCHOOL = 0, SECTION = 1, RANK = 2, SECONDARY = 3, STRENGTH = 4, CONTACT = 5, FADE = 6,
            OFF_HAND = 7, SEED = 8, LEFT_MAIN = 9;
    public record Packed(int color, int overlay, int light) {}

    private VeinEncoding() {}

    public static Packed pack(EnumBloodTendency school, EnumBloodTendency secondary, EnumVeinSections section,
                              EnumManipulationRank rank, float strength, float contact, float fade, boolean offHand, boolean leftMain, int seed) {
        int a = unit(fade), r = unit(strength), g = school.ordinal() * 8 + section.ordinal();
        int b = (secondary == null ? 8 : secondary.ordinal()) * 8 + rank.ordinal();
        int overlay = Math.round(Math.clamp(contact, 0, 1) * 1000) | Math.floorMod(seed, 1000) << 16;
        return new Packed(a << 24 | r << 16 | g << 8 | b, overlay, (offHand ? 1 : 0) | (leftMain ? 2 : 0));
    }

    public static float[] unpack(Packed packed) {
        int c = packed.color();
        int g = c >> 8 & 255, b = c & 255;
        return new float[]{g / 8, g % 8, b % 8, b / 8, (c >> 16 & 255) / 255f, (packed.overlay() & 0xFFFF) / 1000f,
                (c >>> 24) / 255f, packed.light() & 1, packed.overlay() >>> 16, packed.light() >> 1 & 1};
    }

    private static int unit(float value) { return Math.round(Math.clamp(value, 0, 1) * 255); }
}

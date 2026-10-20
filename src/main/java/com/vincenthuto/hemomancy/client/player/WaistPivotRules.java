package com.vincenthuto.hemomancy.client.player;

/** Body pitch about the waist: the torso cube rotates at its neck origin, so its bottom edge is translated back onto the legs. */
public final class WaistPivotRules {
    private static final float LIMIT = (float)Math.toRadians(14);
    private static final float TORSO = 12;
    private WaistPivotRules() {}
    /** {y, z} model-unit translation for the body part (model +y is down). */
    public static float[] offsets(float pitch) {
        if (pitch == 0) return new float[]{0, 0};
        return new float[]{TORSO*(1-(float)Math.cos(pitch)), -TORSO*(float)Math.sin(pitch)};
    }
    /** Absolute {y, z} for a part riding the pivot, from its un-pivoted base; assign it, never add it. */
    public static float[] partOffsets(float pitch, float baseY, float baseZ) {
        float[] o=offsets(pitch);
        return new float[]{baseY+o[0], baseZ+o[1]};
    }
    /**
     * Absolute {y, z} for the head: it follows the neck forward but keeps its height. Lowering it with the neck while
     * the pitched torso lifts its back top edge sank the head about a pixel into the shoulders.
     */
    public static float[] headOffsets(float pitch, float baseY, float baseZ) {
        return new float[]{baseY, baseZ+offsets(pitch)[1]};
    }
    /** Torso-layer weight across an attack swing (0..1): eases out to mid-swing and back, so the lean never pops. */
    public static float swingWeight(float attack) {
        float s=(float)Math.sin(Math.clamp(attack,0,1)*Math.PI);
        return 1-s*s;
    }
    public static float clamp(float pitch) { return Math.clamp(pitch, -LIMIT, LIMIT); }
    /** The head keeps the player's aim rather than nodding with the torso. */
    public static float headCompensation(float pitch) { return -pitch; }
}

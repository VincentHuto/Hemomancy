package com.vincenthuto.hemomancy.client.model.entity.morphling;

/**
 * One part of a generated strain model, as {@code tools/model_export/morphling_strains.py} writes it.
 *
 * @param name       the part's name in the mesh
 * @param parent     its parent part, or {@code "root"}
 * @param stage      the maturity it grows at (0 Unfed .. 5 Primal)
 * @param limb       which segment of a jointed limb it belongs to (Track 9's NPC rig); NONE off the limbs
 * @param motion     how it moves
 * @param amp        motion amplitude (radians, or a scale fraction for BREATHE)
 * @param speed      motion speed in radians per tick (cycles per tick for DRIP)
 * @param phase      motion phase offset
 * @param flags      the {@code MorphlingActiveState} bits a state motion answers
 * @param glowGroup  the glow pulse it lights in (a travelling pulse lights one group after another)
 * @param grows      whether it is the first part of its stage in its branch, so it grows in (0.7, 0.88, 1.0)
 */
public record MorphlingPartSpec(String name, String parent, int stage, Limb limb, Motion motion, float amp, float speed,
                                float phase, int flags, int glowGroup, boolean grows) {
    public enum Limb {
        NONE, UPPER, LOWER
    }

    public enum Motion {
        /** Still. */
        NONE,
        /** A slow two-axis sway, as if underwater. */
        SWAY,
        /** A side-to-side wag about the part's own Y axis (a tail). */
        WAG,
        /** A wave travelling down a chain (phase grows along it). */
        RIPPLE,
        /** A faster, flickering ripple (Kinbud's mane), stronger while Kindled. */
        FLAME,
        /** Opens with daylight and with its flags (Sunburst's fronds). */
        FROND,
        /** A slow held-breath swell, then stillness (Hushbell). */
        BREATHE,
        /** Lies flat, then raises about its own Y axis while its flags are on (the sailfish sail). */
        SAIL,
        /** Swells with Mermaid's Purse's banked blood. */
        POUCH,
        /** Lengthens and resets like a falling droplet. */
        DRIP,
        /** Grows in while its flags are on and is gone while they are off. */
        GROW_ON,
        /** Splays outward while its flags are on (grip strands). */
        SPLAY_ON
    }
}

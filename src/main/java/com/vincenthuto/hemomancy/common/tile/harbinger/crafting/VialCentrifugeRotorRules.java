package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

/**
 * Rotor motion for the Vial Centrifuge renderer: how fast a spin runs, how the rotor gains and
 * loses speed, and how far the buckets, flyballs and dial needle answer that speed.
 */
public final class VialCentrifugeRotorRules {
    /** Degrees a bucket swings out at full speed; tools/model_export/vial_centrifuge.py checks its clearance. */
    public static final float MAX_SWING = 50F;
    public static final float MAX_GOVERNOR_LIFT = 40F;
    public static final float NEEDLE_SWEEP = 120F;
    /** The crank and flywheel turn through a step-up gear, slower than the rotor. */
    public static final float DRIVE_RATIO = .25F;

    private VialCentrifugeRotorRules() {}

    /** Top rotor speed in degrees per tick; upgraded rotors run faster to match shorter spins. */
    public static float topSpeed(int stage) {
        return 24F + Math.clamp(stage, 0, 2) * 8F;
    }

    /** 0..1 drive over a spin counting down from total: winds up, holds, then cuts out before the end. */
    public static float drive(int remaining, int total) {
        if (remaining <= 0 || total <= 0) return 0;
        float phase = 1 - Math.min(remaining, total) / (float) total;
        return Math.min(1F, Math.min(phase / .2F + .05F, (1 - phase) / .2F));
    }

    public static float targetSpeed(int stage, int remaining, int total) {
        return topSpeed(stage) * drive(remaining, total);
    }

    /** One tick of inertia: the rotor spins up eagerly and coasts down slowly. */
    public static float approach(float speed, float target) {
        float next = speed + (target - speed) * (target > speed ? .1F : .045F);
        return next < .05F ? 0 : next;
    }

    public static float speedFraction(int stage, float speed) {
        return Math.clamp(speed / topSpeed(stage), 0F, 1F);
    }

    /** Buckets reach full swing before full speed, as a real swing-out rotor does. */
    public static float swing(float fraction) {
        float t = Math.clamp(fraction / .7F, 0F, 1F);
        return MAX_SWING * t * t * (3 - 2 * t);
    }

    public static float governorLift(float fraction) {
        return MAX_GOVERNOR_LIFT * fraction * fraction;
    }

    /** Needle angle about the dial's Z axis: resting at the operator's left, full speed at their right. */
    public static float needle(float fraction) {
        return -NEEDLE_SWEEP + 2 * NEEDLE_SWEEP * Math.clamp(fraction, 0F, 1F);
    }

    /** Rotor angle of vial slot 2..9; balanced pairs (2,6) (3,7) (4,8) (5,9) sit opposite each other. */
    public static float slotAngle(int slot) {
        return (slot - 2) * 45F;
    }
}

package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;

/**
 * The caster's pose for a body-anchored cast visual, captured by the renderer each frame. Every point is a world-axis
 * offset from the caster's feet, so camera-facing cards stay true. {@code hand} is the main casting hand,
 * {@code motion} the body's movement over the last tick and {@code origin} where the cast was sent from. {@code fingers}
 * is the way the drawn first-person cell hand points, or null when none is drawn.
 */
record CasterFrame(float bodyYaw, float headYaw, float scale, boolean crouching, boolean firstPerson,
        Vec3 hand, Vec3 look, Vec3 motion, Vec3 origin, Vec3 fingers) {
    static final Vec3 UP = new Vec3(0, 1, 0);

    /** A standing caster at the origin facing +Z, for tests and entities that never render. */
    static CasterFrame standing(boolean firstPerson) {
        return new CasterFrame(0, 0, 1, false, firstPerson, estimateHand(0, 1, false, firstPerson, new Vec3(0, 0, 1), 1),
                new Vec3(0, 0, 1), Vec3.ZERO, Vec3.ZERO, null);
    }

    Vec3 forward() {
        double yaw = Math.toRadians(bodyYaw);
        return new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    /** The wearer's right-hand side. */
    Vec3 right() {
        double yaw = Math.toRadians(bodyYaw);
        return new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
    }

    /** Height of the chest above the feet. */
    double chest() {
        return (crouching ? 0.85 : 1.15) * scale;
    }

    /** Horizontal travel direction, or the body facing when standing still. */
    Vec3 travel() {
        Vec3 flat = new Vec3(motion.x, 0, motion.z);
        return flat.lengthSqr() < 1e-4 ? forward() : flat.normalize();
    }

    /** Horizontal speed in blocks per tick. */
    double speed() {
        return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    /**
     * Where the casting hand sits when no posed model captured it: cast poses hold it in front of the eyes, so the look
     * direction places it. {@code side} is +1 for a right-handed caster.
     */
    static Vec3 estimateHand(float bodyYaw, double scale, boolean crouching, boolean firstPerson, Vec3 look, int side) {
        double yaw = Math.toRadians(bodyYaw);
        Vec3 right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3 aim = look.lengthSqr() < 1e-6 ? new Vec3(-Math.sin(yaw), 0, Math.cos(yaw)) : look.normalize();
        double eye = (crouching ? 1.27 : 1.62) * scale;
        return UP.scale(eye - (firstPerson ? 0.38 : 0.5) * scale).add(aim.scale((firstPerson ? 0.55 : 0.45) * scale))
                .add(right.scale(side * (firstPerson ? 0.4 : 0.34) * scale));
    }
}

package com.vincenthuto.hemomancy.common.entity.projectile;

import net.minecraft.world.phys.Vec3;

/** Shared server orbit and fractional-tick visual orbit. */
public final class BloodShotOrbit {
    private BloodShotOrbit() {}

    public static Vec3 offset(double age, int slot) {
        double angle = age * .08 + slot * Math.PI * 2 / 5;
        return new Vec3(Math.cos(angle) * 1.25, 1.55 + Math.sin(angle * 2) * .12,
                Math.sin(angle) * 1.25);
    }
}

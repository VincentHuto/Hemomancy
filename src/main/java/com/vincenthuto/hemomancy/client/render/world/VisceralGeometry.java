package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;

/** Stable local frames and curves shared by the material meshes. */
public final class VisceralGeometry {
    private VisceralGeometry() {}

    public static double crownSwordGrowth(double charge, int slot) {
        return Math.clamp(charge * 8 - slot, 0, 1);
    }

    public static Vec3 side(Vec3 direction) {
        Vec3 axis = direction.lengthSqr() < 1e-12 ? new Vec3(0, 1, 0) : direction.normalize();
        return axis.cross(Math.abs(axis.y) > .9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize();
    }

    public static Vec3 strandPoint(Vec3 start, Vec3 end, double t, double bend, double phase) {
        Vec3 axis = end.subtract(start).normalize();
        Vec3 side = side(axis), up = axis.cross(side).normalize();
        double envelope = Math.sin(Math.PI * t);
        return start.lerp(end, t).add(side.scale(envelope * bend * Math.sin(t * 5 + phase)))
                .add(up.scale(envelope * bend * Math.cos(t * 3 + phase)));
    }

    public static Vec3 wellPoint(double radius, int arm, double t, double time) {
        double a = arm * Math.PI * 2 / 5 + t * (5.4 + arm * .18) - time * .025
                + .16 * Math.sin(t * 9 + arm) * Math.sin(Math.PI*t);
        double distance = .18 + Math.max(0, radius - .18) * Math.pow(1 - t, 1.3);
        distance *= 1 - .12 * Math.pow(Math.sin(Math.PI*t)*Math.sin(t*7+arm),2);
        return new Vec3(Math.cos(a) * distance, .055 + Math.sin(t * Math.PI) * (.12 + arm * .025),
                Math.sin(a) * distance);
    }

    /** Height runs from the open mouth to the crown. */
    public static double bellRadius(double height) {
        return .22 + .62 * Math.pow(1 - height, 3) + .055 * Math.sin(Math.PI * height);
    }
}

package com.vincenthuto.hemomancy.client.render.item.harbinger;

final class LivingSyringeMotion {
    static final float OPEN_ANGLE = -67.5F;
    private static final float HINGE_TICKS = 5;
    private static final float DRUM_TICKS = 8;
    private static final int FIRST_RACK_LOAD = 3;

    record Pose(float hinge, float drum) { }

    private int load = -1;
    private int slot;
    private double lastSeen;
    private double hingeStart;
    private double drumStart;
    private float hingeFrom;
    private float drumFrom;

    static Pose resting(int load) {
        return new Pose(load == 0 ? OPEN_ANGLE : 0, load >= FIRST_RACK_LOAD ? (load - FIRST_RACK_LOAD) * 45 : 0);
    }

    Pose sample(int next, int inventorySlot, double tick) {
        Pose target = resting(next);
        if (load == -1 || slot != inventorySlot || tick < lastSeen || tick - lastSeen > 5) {
            load = next;
            slot = inventorySlot;
            hingeFrom = target.hinge;
            drumFrom = target.drum;
            hingeStart = drumStart = tick;
        } else if (next != load) {
            Pose previous = resting(load);
            Pose current = interpolate(previous, tick);
            if (previous.hinge != target.hinge) {
                hingeFrom = current.hinge;
                hingeStart = tick;
            }
            if (previous.drum != target.drum || (load < FIRST_RACK_LOAD && next >= FIRST_RACK_LOAD)) {
                drumFrom = load >= FIRST_RACK_LOAD && next >= FIRST_RACK_LOAD ? current.drum : target.drum;
                drumStart = tick;
            }
            load = next;
        }
        lastSeen = tick;
        return interpolate(target, tick);
    }

    private Pose interpolate(Pose target, double tick) {
        float hinge = progress(tick, hingeStart, HINGE_TICKS);
        float drum = progress(tick, drumStart, DRUM_TICKS);
        return new Pose(hingeFrom + (target.hinge - hingeFrom) * hinge * (2 - hinge),
                drumFrom + (target.drum - drumFrom) * drum * drum * (3 - 2 * drum));
    }

    private static float progress(double tick, double start, float duration) {
        return (float) Math.clamp((tick - start) / duration, 0, 1);
    }
}

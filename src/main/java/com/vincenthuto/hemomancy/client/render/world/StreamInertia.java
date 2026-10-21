package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gives a drawn stream of blood weight: each station chases where the stream should be as a damped spring held in
 * world space, so when the caster turns, walks or swings the arm the stream trails behind, overshoots and settles
 * instead of moving rigidly with the hand. Stations with no slack are pinned; the slackest follow loosest. Streams are
 * remembered per caster and slot, and forgotten once they stop being drawn.
 */
final class StreamInertia {
    /** Spring rates in radians per tick: pinned stations snap, the slackest swing about once a second. */
    private static final double TIGHT = 1.6, LOOSE = 0.32, DAMPING = 0.5;
    /** Integration step in ticks, and the longest gap carried over before a stream is rebuilt where it should be. */
    private static final double STEP = 0.25, GAP = 3;
    /** How far a station may trail from where it should be, in blocks at caster scale 1. */
    private static final double REACH = 1.2;
    private static final int FORGET = 40;

    private static final Map<Long, Stream> STREAMS = new HashMap<>();
    private static double sweptAt;

    private StreamInertia() {}

    private static final class Stream {
        Vec3[] at, velocity;
        Vec3 feet;
        double time;
    }

    /**
     * Moves {@code path} (offsets from {@code feet}) in place to where the remembered stream has swung to by
     * {@code time}. {@code slack[i]} in [0, 1] is how loosely station {@code i} follows: 0 is pinned. A null
     * {@code feet} leaves the path rigid.
     */
    static void apply(int caster, int slot, List<Vec3> path, double[] slack, Vec3 feet, double time, double scale) {
        if (feet == null) return;
        sweep(time);
        int n = path.size();
        Stream stream = STREAMS.computeIfAbsent(((long) caster << 16) | (slot & 0xFFFF), key -> new Stream());
        double dt = time - stream.time;
        if (stream.at == null || stream.at.length != n || dt < 0 || dt > GAP) {
            stream.at = new Vec3[n];
            stream.velocity = new Vec3[n];
            for (int i = 0; i < n; i++) {
                stream.at[i] = feet.add(path.get(i));
                stream.velocity[i] = Vec3.ZERO;
            }
            stream.feet = feet;
            stream.time = time;
            return;
        }
        double reach = REACH * scale;
        for (int i = 0; i < n; i++) {
            Vec3 goal = feet.add(path.get(i));
            double loose = Math.clamp(slack[i], 0, 1);
            if (loose <= 0) {
                stream.at[i] = goal;
                stream.velocity[i] = Vec3.ZERO;
                continue;
            }
            double rate = TIGHT + (LOOSE - TIGHT) * loose;
            double k = rate * rate, c = 2 * DAMPING * rate;
            Vec3 x = stream.at[i], u = stream.velocity[i];
            for (double left = dt; left > 1e-6; left -= STEP) {
                double h = Math.min(STEP, left);
                u = u.add(goal.subtract(x).scale(k * h)).subtract(u.scale(Math.min(1, c * h)));
                x = x.add(u.scale(h));
            }
            // A violent jerk can fling a station off; keep it on a leash to where it belongs.
            Vec3 trail = x.subtract(goal);
            if (trail.lengthSqr() > reach * reach) x = goal.add(trail.normalize().scale(reach));
            stream.at[i] = x;
            stream.velocity[i] = u;
            path.set(i, x.subtract(feet));
        }
        stream.feet = feet;
        stream.time = time;
    }

    /**
     * The stream in {@code slot} as last drawn, as offsets from the caster's feet at that frame, or null when it has
     * not been drawn in the last couple of ticks.
     */
    static Vec3[] drawn(int caster, int slot, double time) {
        Stream stream = STREAMS.get(((long) caster << 16) | (slot & 0xFFFF));
        if (stream == null || stream.at == null || Math.abs(time - stream.time) > 2) return null;
        Vec3[] offsets = new Vec3[stream.at.length];
        for (int i = 0; i < offsets.length; i++) offsets[i] = stream.at[i].subtract(stream.feet);
        return offsets;
    }

    private static void sweep(double time) {
        if (time - sweptAt < FORGET && time >= sweptAt) return;
        sweptAt = time;
        STREAMS.values().removeIf(stream -> time - stream.time > FORGET || stream.time > time);
    }
}

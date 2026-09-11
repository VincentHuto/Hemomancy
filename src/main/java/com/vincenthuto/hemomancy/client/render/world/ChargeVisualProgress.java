package com.vincenthuto.hemomancy.client.render.world;

/** Packet updates are four ticks apart; sample their transition at render time. */
final class ChargeVisualProgress {
    private float from;
    private float target;
    private double updatedAt;

    ChargeVisualProgress(float initial, double time) {
        target = initial;
        updatedAt = time;
    }

    void update(float value, double time) {
        from = sample(time);
        target = value;
        updatedAt = time;
    }

    float sample(double time) {
        float blend = (float) Math.clamp((time - updatedAt) / 4.0, 0, 1);
        return from + (target - from) * blend;
    }
}

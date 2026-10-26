package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import net.minecraft.world.phys.Vec3;
import java.util.Map;
import java.util.WeakHashMap;

/** Independent clocks allow a nova and an absolution hit to animate during the offering window. */
public final class PutricielAnimState {
    private static final Map<PutricielEntity, PutricielAnimState> STATES = new WeakHashMap<>();
    private final BossReplayClock visualClock = new BossReplayClock(), windowClock = new BossReplayClock(), novaClock = new BossReplayClock();
    private float visual, window, nova, release;
    private Vec3 core = new Vec3(0, 1.8, 0), palm = new Vec3(1, 1.8, 0);
    private Vec3[] sacs = {new Vec3(0, 1.6, 0), new Vec3(.3, 1.8, 0), new Vec3(-.3, 1.5, 0), new Vec3(0, 2, 0)};
    private Vec3[] censers = {new Vec3(-1.5, 2.2, 0), new Vec3(-1.7, 2, 0)};
    private PutricielAnimState() {}
    public static PutricielAnimState of(PutricielEntity boss) { return STATES.computeIfAbsent(boss, ignored -> new PutricielAnimState()); }
    public static void clear() { STATES.clear(); }
    public void update(PutricielEntity boss, float partial) {
        visual = visualClock.sample(boss.getVisualState(), boss.getVisualTicks(), boss.tickCount, partial, -1);
        window = windowClock.sample(boss.isAbsolved() ? 1 : 0, boss.getAbsolutionWindowTimer(), boss.tickCount, partial, -1);
        boolean releasing = boss.getNovaReleaseTicks() > 0;
        float sampled = novaClock.sample(releasing ? 1 : 0, releasing ? boss.getNovaReleaseTicks() : boss.getNovaTimer(), boss.tickCount, partial, -1);
        release = releasing ? sampled : 0;
        nova = releasing ? boss.getNovaTimer() : sampled;
    }
    public float visual() { return visual; }
    public float window() { return window; }
    public float release() { return release; }
    public float nova() { return nova; }
    public Vec3 core() { return core; }
    public Vec3 palm() { return palm; }
    public Vec3[] sacs() { return sacs; }
    public Vec3[] censers() { return censers; }
    public void attachments(Vec3 core, Vec3 palm, Vec3[] sacs, Vec3[] censers) {
        this.core = core; this.palm = palm; this.sacs = sacs; this.censers = censers;
    }
}

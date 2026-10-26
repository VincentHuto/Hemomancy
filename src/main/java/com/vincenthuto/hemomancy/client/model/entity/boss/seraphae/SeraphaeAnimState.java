package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeVisualRules;
import net.minecraft.world.phys.Vec3;
import java.util.Map;
import java.util.WeakHashMap;

/** Each tracked saint owns her replay clock; model instances are shared by the renderer. */
public final class SeraphaeAnimState {
    private static final Map<SeraphaeEntity, SeraphaeAnimState> STATES = new WeakHashMap<>();
    private final BossReplayClock clock = new BossReplayClock();
    private float remaining;
    private Vec3 leftWrist = new Vec3(-.65, 1.2, 0), rightWrist = new Vec3(.65, 1.2, 0);
    private final Vec3[] feathers = {new Vec3(-1, 1.8, 0), new Vec3(-1.5, 1.9, 0), new Vec3(-2, 2, 0),
            new Vec3(1, 1.8, 0), new Vec3(1.5, 1.9, 0), new Vec3(2, 2, 0)};

    private SeraphaeAnimState() {}

    public static SeraphaeAnimState of(SeraphaeEntity entity) {
        return STATES.computeIfAbsent(entity, ignored -> new SeraphaeAnimState());
    }

    public static void clear() { STATES.clear(); }

    public void update(SeraphaeEntity entity, float partial) {
        remaining = clock.sample(entity.getSeraphaeState().ordinal(), entity.getStateTimer(), entity.tickCount, partial, -1);
    }

    public float remaining() { return remaining; }

    public SeraphaeVisualRules.Pose pose(SeraphaeEntity entity, float age) {
        return SeraphaeVisualRules.pose(entity.getSeraphaeState(), remaining, age);
    }

    public void wrists(Vec3 left, Vec3 right) { leftWrist = left; rightWrist = right; }

    public Vec3 wrist(boolean left) { return left ? leftWrist : rightWrist; }

    public void feather(int index, Vec3 point) { feathers[index] = point; }

    public Vec3 feather(int index) { return feathers[index]; }
}

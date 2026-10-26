package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import net.minecraft.world.phys.Vec3;
import java.util.Map;
import java.util.WeakHashMap;

/** One replay clock and posed effect attachments per saint, rather than on the renderer's shared model. */
public final class HemorathAnimState {
    private static final Map<HemorathEntity, HemorathAnimState> STATES = new WeakHashMap<>();
    private final BossReplayClock clock = new BossReplayClock();
    private float remaining;
    private Vec3 core = new Vec3(0, 2.6, 0), claw = new Vec3(1, 2.2, 0), staff = new Vec3(-1.5, 4, 0);

    private HemorathAnimState() {}
    public static HemorathAnimState of(HemorathEntity boss) { return STATES.computeIfAbsent(boss, ignored -> new HemorathAnimState()); }
    public static void clear() { STATES.clear(); }
    public void update(HemorathEntity boss, float partial) {
        remaining = clock.sample(boss.getVisualState(), boss.getVisualTicks(), boss.tickCount, partial, -1);
    }
    public float remaining() { return remaining; }
    public Vec3 core() { return core; }
    public Vec3 claw() { return claw; }
    public Vec3 staff() { return staff; }
    public void attachments(Vec3 core, Vec3 claw, Vec3 staff) { this.core = core; this.claw = claw; this.staff = staff; }
}

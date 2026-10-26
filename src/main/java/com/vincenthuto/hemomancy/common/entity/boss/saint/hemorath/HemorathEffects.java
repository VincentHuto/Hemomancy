package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Server-authored impacts and short custom accents over the persistent blood/iron geometry. */
public final class HemorathEffects {
    private static final ParticleColor BLOOD = new ParticleColor(159, 20, 35), CORE = new ParticleColor(255, 210, 196);
    private HemorathEffects() {}

    public static void burst(ServerLevel level, Vec3 at, int kind, float radius) {
        int ticks = kind == HemorathVisualRules.EXSANGUINATE ? HemorathVisualRules.FINALE_TICKS : HemorathVisualRules.BURST_TICKS;
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.HEMORATH_RELIQUARY, at, at, radius, ticks, kind);
        level.sendParticles(HemoParticleData.hitGlow(CORE), at.x, at.y, at.z, 1, 0, 0, 0, 0);
        level.sendParticles(HemoParticleData.bloodCell(BLOOD), at.x, at.y, at.z,
                kind == HemorathVisualRules.EXSANGUINATE ? 10 : 4, .15, .2, .15, .035);
    }

    public static void intake(ServerLevel level, Vec3 from, Vec3 to) {
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.HEMORATH_RELIQUARY,
                from, to, .8, 16, HemorathVisualRules.INTAKE);
    }
}

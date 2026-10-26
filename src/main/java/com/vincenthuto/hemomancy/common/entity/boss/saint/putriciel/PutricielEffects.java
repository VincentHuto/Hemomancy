package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class PutricielEffects {
    private static final ParticleColor SPORE = new ParticleColor(183, 159, 73), EMBER = new ParticleColor(223, 63, 38);
    private PutricielEffects() {}
    public static void burst(ServerLevel level, Vec3 at, int kind, float radius) {
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.PUTRICIEL_OFFERING, at, at, radius,
                kind == PutricielVisualRules.REST ? PutricielVisualRules.REST_TICKS : PutricielVisualRules.BURST_TICKS, kind);
        level.sendParticles(HemoParticleData.hitGlow(SPORE), at.x, at.y, at.z, 1, 0, 0, 0, 0);
        level.sendParticles(HemoParticleData.bloodCell(kind == PutricielVisualRules.NOVA ? EMBER : SPORE),
                at.x, at.y, at.z, 6, .2, .3, .2, .025);
    }
}

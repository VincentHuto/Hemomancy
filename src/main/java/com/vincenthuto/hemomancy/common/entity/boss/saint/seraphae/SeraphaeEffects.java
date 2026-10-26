package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Small authored particle accents over server-authored material cues; persistent radiance uses synced state. */
public final class SeraphaeEffects {
    private static final ParticleColor GOLD = new ParticleColor(255, 221, 156);
    private static final ParticleColor PALE = new ParticleColor(255, 240, 214);

    private SeraphaeEffects() {}

    public static void burst(ServerLevel level, Vec3 at, int kind, float radius) {
        int life = kind == SeraphaeVisualRules.SEALED ? SeraphaeVisualRules.SEAL_TICKS : SeraphaeVisualRules.BURST_TICKS;
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.SERAPHAE_RELIQUARY, at, at, radius, life, kind);
        level.sendParticles(HemoParticleData.hitGlow(PALE), at.x, at.y, at.z, 1, 0, 0, 0, 0);
        level.sendParticles(HemoParticleData.glow(GOLD), at.x, at.y, at.z,
                kind == SeraphaeVisualRules.SEALED ? 12 : 4, .15, .25, .15, .025);
    }

    public static void bind(ServerLevel level, Vec3 from, Vec3 to) {
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.SERAPHAE_RELIQUARY,
                from, to, .7, SeraphaeVisualRules.BURST_TICKS, SeraphaeVisualRules.CAPTURE);
    }
}

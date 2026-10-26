package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class VelorumEffects {
    private static final ParticleColor RIME=new ParticleColor(180,210,255);
    private VelorumEffects() {}
    public static void burst(ServerLevel level,Vec3 at,int kind,float radius) {
        int life=kind==VelorumVisualRules.REST?VelorumVisualRules.REST_TICKS:kind==VelorumVisualRules.VEIL?VelorumVisualRules.VEIL_TICKS:VelorumVisualRules.BURST_TICKS;
        ManipulationVisuals.burst(level,ManipulationVisuals.Form.VELORUM_RIME,at,at,radius,life,kind);
        level.sendParticles(HemoParticleData.hitGlow(RIME),at.x,at.y,at.z,1,0,0,0,0);
    }
    public static void drain(ServerLevel level,Vec3 from,Vec3 to) {
        ManipulationVisuals.burst(level,ManipulationVisuals.Form.VELORUM_RIME,from,to,.8,24,VelorumVisualRules.DRAIN);
    }
}

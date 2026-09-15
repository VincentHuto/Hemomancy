package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class PhlegethonticVisuals {
    private PhlegethonticVisuals() {}
    public static void surfacePulse(Level level,double x,double y,double z) {
        var particle=new ColorParticleData(HLParticleInit.glow.get(),new ParticleColor(120,12,19));
        level.addParticle(particle,x,y,z,0,.015,0);
    }
    public static void breath(ServerLevel level,double x,double y,double z,double dx,double dz) {
        var particle=new ColorParticleData(HLParticleInit.glow.get(),new ParticleColor(155,25,23));
        level.sendParticles(particle,x+dx,y,z+dz,3,.22,.12,.22,.015);
    }
}

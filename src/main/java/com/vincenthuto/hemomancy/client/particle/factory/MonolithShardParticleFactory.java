package com.vincenthuto.hemomancy.client.particle.factory;

import com.vincenthuto.hemomancy.client.particle.MonolithShardParticle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class MonolithShardParticleFactory implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet spriteSet;

    public MonolithShardParticleFactory(SpriteSet sprite) {
        this.spriteSet = sprite;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                    double x, double y, double z,
                                    double xSpeed, double ySpeed, double zSpeed) {
        float size     = 0.12f + level.random.nextFloat() * 0.18f;   // 0.12–0.30
        int lifetime   = 20 + level.random.nextInt(20);               // 20–40 ticks
        float rollSpd  = 0.18f + level.random.nextFloat() * 0.25f;    // 0.18–0.43 rad/tick
        if (level.random.nextBoolean()) rollSpd = -rollSpd;           // random spin direction
        return new MonolithShardParticle(level, x, y, z,
                xSpeed, ySpeed, zSpeed,
                size, lifetime, rollSpd, this.spriteSet);
    }
}

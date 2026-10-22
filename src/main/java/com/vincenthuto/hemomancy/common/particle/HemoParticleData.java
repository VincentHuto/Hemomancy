package com.vincenthuto.hemomancy.common.particle;

import com.vincenthuto.hemomancy.common.particle.data.AbsorbedBloodCellData;
import com.vincenthuto.hemomancy.common.particle.data.BloodAvatarHitParticleData;
import com.vincenthuto.hemomancy.common.particle.data.BloodCellData;
import com.vincenthuto.hemomancy.common.particle.data.BloodClawData;
import com.vincenthuto.hemomancy.common.particle.data.DaemonDiffuseGlowParticleData;
import com.vincenthuto.hemomancy.common.particle.data.HermitEdgeGlowParticleData;
import com.vincenthuto.hemomancy.common.particle.data.SerpentParticleData;
import com.vincenthuto.hemomancy.common.particle.data.SporiticSporeParticleData;
import com.vincenthuto.hemomancy.common.particle.data.WillAbsorptionGlowParticleData;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hemomancy.common.particle.data.HitColorParticleData;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.client.particle.data.DarkColorParticleData;
import com.vincenthuto.hutoslib.client.particle.data.EmberParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
import net.minecraft.core.particles.ParticleOptions;

/** Particle payloads usable on either side without loading a client ParticleProvider. */
public final class HemoParticleData {
    private HemoParticleData() {}

    public static ParticleOptions glow(ParticleColor color) {
        return new ColorParticleData(HLParticleInit.glow.get(), color);
    }

    public static ParticleOptions bloodCell(ParticleColor color) {
        return new BloodCellData(ParticleInit.blood_cell.get(), color);
    }

    public static ParticleOptions darkGlow(ParticleColor color) {
        return new DarkColorParticleData(HLParticleInit.dark_glow.get(), color);
    }

    public static ParticleOptions serpent(ParticleColor color) {
        return new SerpentParticleData(ParticleInit.serpent.get(), color);
    }

    public static ParticleOptions absorbedBloodCell(ParticleColor color) {
        return new AbsorbedBloodCellData(ParticleInit.absorbed_blood_cell.get(), color);
    }

    public static ParticleOptions ember(ParticleColor color, float s, float a, int l) {
        return new EmberParticleData(HLParticleInit.ember.get(), color, a, s, l);
    }

    /**
     * An ember with its scale and alpha where its parameters say. {@link #ember} hands them to HutosLib swapped, and
     * its callers are tuned around that; a large scale also flashes at full alpha for the glow's first frame.
     */
    public static ParticleOptions emberSpark(ParticleColor color, float scale, float alpha, int life) {
        return new EmberParticleData(HLParticleInit.ember.get(), color, scale, alpha, life);
    }

    public static ParticleOptions hitGlow(ParticleColor color) {
        return new HitColorParticleData(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static ParticleOptions sporiticSpore(ParticleColor color) {
        return new SporiticSporeParticleData(ParticleInit.sporitic_spore.get(), color);
    }

    public static ParticleOptions hermitEdgeGlow(ParticleColor color) {
        return new HermitEdgeGlowParticleData(ParticleInit.hermit_edge_glow.get(), color);
    }

    public static ParticleOptions daemonDiffuseGlow(float scale) {
        return new DaemonDiffuseGlowParticleData(
        Math.max(0.02F, Math.min(1.0F, scale)));
    }

    public static ParticleOptions bloodAvatarHit(ParticleColor color) {
        return new BloodAvatarHitParticleData(ParticleInit.blood_avatar_hit.get(), color);
    }

    public static ParticleOptions bloodClaw(ParticleColor color) {
        return new BloodClawData(ParticleInit.blood_claw.get(), color);
    }

    public static ParticleOptions willAbsorptionGlowPulse(ParticleColor color) {
        return new WillAbsorptionGlowParticleData(ParticleInit.will_absorption_glow.get(), color, true);
    }
}

package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.LetheanDripParticle;
import com.vincenthuto.hemomancy.client.particle.factory.*;
import com.vincenthuto.hemomancy.common.init.ParticleInit;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ParticleProviderRegistrar {

	private ParticleProviderRegistrar() {
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(ParticleInit.abocipher.get(), AbocipherParticleFactory::new);
		event.registerSpriteSet(ParticleInit.lethean_drip.get(), LetheanDripParticle.Provider::new);
		event.registerSpriteSet(ParticleInit.blood_avatar_hit.get(), BloodAvatarHitParticleFactory::new);
		event.registerSpriteSet(ParticleInit.hit_glow.get(), HitGlowParticleFactory::new);
		event.registerSpriteSet(ParticleInit.will_absorption_glow.get(), WillAbsorptionGlowParticleFactory::new);
		event.registerSpriteSet(ParticleInit.daemon_diffuse_glow.get(), DaemonDiffuseGlowParticleFactory::new);
		event.registerSpriteSet(ParticleInit.hermit_edge_glow.get(), HermitEdgeGlowParticleFactory::new);
		event.registerSpriteSet(ParticleInit.sporitic_spore.get(), SporiticSporeParticleFactory::new);
		event.registerSpriteSet(ParticleInit.serpent.get(), SerpentParticleFactory::new);
		event.registerSpriteSet(ParticleInit.blood_cell.get(), BloodCellParticleFactory::new);
		event.registerSpriteSet(ParticleInit.blood_claw.get(), BloodClawParticleFactory::new);
		event.registerSpriteSet(ParticleInit.absorbed_blood_cell.get(), AbsorbedBloodCellParticleFactory::new);
	}
}

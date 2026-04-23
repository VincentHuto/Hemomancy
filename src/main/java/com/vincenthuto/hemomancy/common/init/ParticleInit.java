package com.vincenthuto.hemomancy.common.init;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.data.AbsorbedBloodCellData;
import com.vincenthuto.hemomancy.client.particle.data.BloodAvatarHitParticleData;
import com.vincenthuto.hemomancy.client.particle.data.BloodCellData;
import com.vincenthuto.hemomancy.client.particle.data.BloodClawData;
import com.vincenthuto.hemomancy.client.particle.data.HitColorParticleData;
import com.vincenthuto.hemomancy.client.particle.data.SerpentParticleData;
import com.vincenthuto.hemomancy.client.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodAvatarHitParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodClawParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.HitGlowParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hemomancy.client.particle.type.AbsorbedBloodCellParticleType;
import com.vincenthuto.hemomancy.client.particle.type.BloodAvatarHitParticleType;
import com.vincenthuto.hemomancy.client.particle.type.BloodCellParticleType;
import com.vincenthuto.hemomancy.client.particle.type.BloodClawParticleType;
import com.vincenthuto.hemomancy.client.particle.type.HitGlowParticleType;
import com.vincenthuto.hemomancy.client.particle.type.SerpentParticleType;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleInit {

	
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
			.create(Registries.PARTICLE_TYPE, Hemomancy.MOD_ID);

	public static final DeferredHolder<ParticleType<?>, ParticleType<SerpentParticleData>> serpent = PARTICLE_TYPES.register("serpent",
			() -> new SerpentParticleType());

	public static final DeferredHolder<ParticleType<?>, ParticleType<HitColorParticleData>> hit_glow = PARTICLE_TYPES
			.register("hit_glow", () -> new HitGlowParticleType());
	
	public static final DeferredHolder<ParticleType<?>, ParticleType<BloodAvatarHitParticleData>> blood_avatar_hit = PARTICLE_TYPES
			.register("blood_avatar_hit", () -> new BloodAvatarHitParticleType());

	public static final DeferredHolder<ParticleType<?>, ParticleType<BloodCellData>> blood_cell = PARTICLE_TYPES.register("blood_cell",
			() -> new BloodCellParticleType());
	public static final DeferredHolder<ParticleType<?>, ParticleType<BloodClawData>> blood_claw = PARTICLE_TYPES.register("blood_claw",
			() -> new BloodClawParticleType());

	public static final DeferredHolder<ParticleType<?>, ParticleType<AbsorbedBloodCellData>> absorbed_blood_cell = PARTICLE_TYPES
			.register("absorbed_blood_cell", () -> new AbsorbedBloodCellParticleType());

	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
		Minecraft.getInstance().particleEngine.register(blood_avatar_hit.get(), BloodAvatarHitParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(hit_glow.get(), HitGlowParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(serpent.get(), SerpentParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(blood_cell.get(), BloodCellParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(blood_claw.get(), BloodClawParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(absorbed_blood_cell.get(), AbsrobedBloodCellParticleFactory::new);

	}
}

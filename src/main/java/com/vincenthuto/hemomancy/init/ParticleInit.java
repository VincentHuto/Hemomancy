package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.particle.data.AbsorbedBloodCellData;
import com.vincenthuto.hemomancy.particle.data.BloodAvatarHitParticleData;
import com.vincenthuto.hemomancy.particle.data.BloodCellData;
import com.vincenthuto.hemomancy.particle.data.BloodClawData;
import com.vincenthuto.hemomancy.particle.data.HitColorParticleData;
import com.vincenthuto.hemomancy.particle.data.SerpentParticleData;
import com.vincenthuto.hemomancy.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.particle.factory.BloodAvatarHitParticleFactory;
import com.vincenthuto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.particle.factory.BloodClawParticleFactory;
import com.vincenthuto.hemomancy.particle.factory.HitGlowParticleFactory;
import com.vincenthuto.hemomancy.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hemomancy.particle.type.AbsorbedBloodCellParticleType;
import com.vincenthuto.hemomancy.particle.type.BloodAvatarHitParticleType;
import com.vincenthuto.hemomancy.particle.type.BloodCellParticleType;
import com.vincenthuto.hemomancy.particle.type.BloodClawParticleType;
import com.vincenthuto.hemomancy.particle.type.HitGlowParticleType;
import com.vincenthuto.hemomancy.particle.type.SerpentParticleType;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleInit {
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
			.create(ForgeRegistries.PARTICLE_TYPES, Hemomancy.MOD_ID);

	public static final RegistryObject<ParticleType<SerpentParticleData>> serpent = PARTICLE_TYPES.register("serpent",
			() -> new SerpentParticleType());

	public static final RegistryObject<ParticleType<HitColorParticleData>> hit_glow = PARTICLE_TYPES
			.register("hit_glow", () -> new HitGlowParticleType());

	public static final RegistryObject<ParticleType<BloodAvatarHitParticleData>> blood_avatar_hit = PARTICLE_TYPES
			.register("blood_avatar_hit", () -> new BloodAvatarHitParticleType());

	public static final RegistryObject<ParticleType<BloodCellData>> blood_cell = PARTICLE_TYPES.register("blood_cell",
			() -> new BloodCellParticleType());
	public static final RegistryObject<ParticleType<BloodClawData>> blood_claw = PARTICLE_TYPES.register("blood_claw",
			() -> new BloodClawParticleType());

	public static final RegistryObject<ParticleType<AbsorbedBloodCellData>> absorbed_blood_cell = PARTICLE_TYPES
			.register("absorbed_blood_cell", () -> new AbsorbedBloodCellParticleType());

	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
		event.register(blood_avatar_hit.get(), BloodAvatarHitParticleFactory::new);
		event.register(hit_glow.get(), HitGlowParticleFactory::new);
		event.register(serpent.get(), SerpentParticleFactory::new);
		event.register(blood_cell.get(), BloodCellParticleFactory::new);
		event.register(blood_claw.get(), BloodClawParticleFactory::new);
		event.register(absorbed_blood_cell.get(), AbsrobedBloodCellParticleFactory::new);

	}
}

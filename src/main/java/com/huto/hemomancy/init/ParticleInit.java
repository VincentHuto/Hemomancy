package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.particle.data.ColorParticleTypeData;
import com.huto.hemomancy.particle.data.ColoredDynamicTypeData;
import com.huto.hemomancy.particle.data.GlowParticleData;
import com.huto.hemomancy.particle.data.HitColorParticleTypeData;
import com.huto.hemomancy.particle.data.HitGlowParticleData;
import com.huto.hemomancy.particle.data.ParticleComplexLightningData;
import com.huto.hemomancy.particle.data.ParticleLightningData;
import com.huto.hemomancy.particle.data.ParticleLineData;
import com.huto.hemomancy.particle.data.ParticleSparkleData;
import com.huto.hemomancy.particle.data.SerpentParticleData;
import com.huto.hemomancy.particle.data.SerpentParticleTypeData;
import com.huto.hemomancy.particle.type.ComplexLightningParticleType;
import com.huto.hemomancy.particle.type.GlowParticleType;
import com.huto.hemomancy.particle.type.HitGlowParticleType;
import com.huto.hemomancy.particle.type.LightningParticleType;
import com.huto.hemomancy.particle.type.LineParticleType;
import com.huto.hemomancy.particle.type.SerpentParticleType;
import com.huto.hemomancy.particle.type.SparkleParticleType;

import net.minecraft.client.Minecraft;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleInit {
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
			.create(ForgeRegistries.PARTICLE_TYPES, Hemomancy.MOD_ID);

	public static final RegistryObject<ParticleType<ColorParticleTypeData>> glow = PARTICLE_TYPES.register("glow",
			() -> new GlowParticleType());

	public static final RegistryObject<ParticleType<SerpentParticleTypeData>> serpent = PARTICLE_TYPES
			.register("serpent", () -> new SerpentParticleType());

	public static final RegistryObject<ParticleType<HitColorParticleTypeData>> hit_glow = PARTICLE_TYPES
			.register("hit_glow", () -> new HitGlowParticleType());

	public static final RegistryObject<ParticleType<ColoredDynamicTypeData>> line = PARTICLE_TYPES.register("line",
			() -> new LineParticleType());
	public static final RegistryObject<ParticleType<ColoredDynamicTypeData>> sparkle = PARTICLE_TYPES
			.register("sparkle", () -> new SparkleParticleType());

	public static RegistryObject<ParticleType<ColorParticleTypeData>> lightning_bolt = PARTICLE_TYPES.register("lightning_bolt",
			() -> new LightningParticleType());
	
	public static RegistryObject<ParticleType<ColorParticleTypeData>> complex_lightning_bolt = PARTICLE_TYPES.register("complex_lightning_bolt",
			() -> new ComplexLightningParticleType());

	@SubscribeEvent
	public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
		Minecraft.getInstance().particles.registerFactory(glow.get(), GlowParticleData::new);
		Minecraft.getInstance().particles.registerFactory(hit_glow.get(), HitGlowParticleData::new);
		Minecraft.getInstance().particles.registerFactory(line.get(), ParticleLineData::new);
		Minecraft.getInstance().particles.registerFactory(serpent.get(), SerpentParticleData::new);
		Minecraft.getInstance().particles.registerFactory(sparkle.get(), ParticleSparkleData::new);
		Minecraft.getInstance().particles.registerFactory(lightning_bolt.get(), ParticleLightningData::new);
		Minecraft.getInstance().particles.registerFactory(complex_lightning_bolt.get(), ParticleComplexLightningData::new);

	}
}

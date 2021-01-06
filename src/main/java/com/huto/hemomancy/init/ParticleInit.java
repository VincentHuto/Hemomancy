package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.particle.data.ColorParticleTypeData;
import com.huto.hemomancy.particle.data.ColoredDynamicTypeData;
import com.huto.hemomancy.particle.data.GlowParticleData;
import com.huto.hemomancy.particle.data.ParticleLineData;
import com.huto.hemomancy.particle.data.ParticleSparkleData;
import com.huto.hemomancy.particle.type.GlowParticleType;
import com.huto.hemomancy.particle.type.LineParticleType;
import com.huto.hemomancy.particle.type.SparkleParticleType;

import net.minecraft.client.Minecraft;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleInit {
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
			.create(ForgeRegistries.PARTICLE_TYPES, Hemomancy.MOD_ID);

	@ObjectHolder(Hemomancy.MOD_ID + ":" + ParticleSparkleData.NAME)
	public static ParticleType<ColoredDynamicTypeData> SPARKLE_TYPE;

	public static final RegistryObject<ParticleType<ColorParticleTypeData>> glow = PARTICLE_TYPES.register("glow",
			() -> new GlowParticleType());
	public static final RegistryObject<ParticleType<ColoredDynamicTypeData>> line = PARTICLE_TYPES.register("line",
			() -> new LineParticleType());
	public static final RegistryObject<ParticleType<ColoredDynamicTypeData>> sparkle = PARTICLE_TYPES
			.register("sparkle", () -> new SparkleParticleType());
	@SubscribeEvent
	public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
		Minecraft.getInstance().particles.registerFactory(glow.get(), GlowParticleData::new);
		Minecraft.getInstance().particles.registerFactory(line.get(), ParticleLineData::new);
		Minecraft.getInstance().particles.registerFactory(sparkle.get(), ParticleSparkleData::new);
	}
}

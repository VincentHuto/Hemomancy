package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.particle.data.AbsorbedBloodCellData;
import com.huto.hemomancy.particle.data.BloodCellData;
import com.huto.hemomancy.particle.data.BloodClawData;
import com.huto.hemomancy.particle.data.ColorLightningData;
import com.huto.hemomancy.particle.data.ColorParticleData;
import com.huto.hemomancy.particle.data.ColoredDynamicData;
import com.huto.hemomancy.particle.data.DarkColorParticleData;
import com.huto.hemomancy.particle.data.HitColorParticleData;
import com.huto.hemomancy.particle.data.SerpentParticleData;
import com.huto.hemomancy.particle.factory.AbsrobedBloodCellParticleFactory;
import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.huto.hemomancy.particle.factory.BloodClawParticleFactory;
import com.huto.hemomancy.particle.factory.DarkGlowParticleFactory;
import com.huto.hemomancy.particle.factory.GlowParticleFactory;
import com.huto.hemomancy.particle.factory.HitGlowParticleFactory;
import com.huto.hemomancy.particle.factory.ParticleLightningFactory;
import com.huto.hemomancy.particle.factory.ParticleLineFactory;
import com.huto.hemomancy.particle.factory.SerpentParticleFactory;
import com.huto.hemomancy.particle.type.AbsorbedBloodCellParticleType;
import com.huto.hemomancy.particle.type.BloodCellParticleType;
import com.huto.hemomancy.particle.type.BloodClawParticleType;
import com.huto.hemomancy.particle.type.DarkGlowParticleType;
import com.huto.hemomancy.particle.type.GlowParticleType;
import com.huto.hemomancy.particle.type.HitGlowParticleType;
import com.huto.hemomancy.particle.type.LightningParticleType;
import com.huto.hemomancy.particle.type.LineParticleType;
import com.huto.hemomancy.particle.type.SerpentParticleType;

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

	public static final RegistryObject<ParticleType<ColorParticleData>> glow = PARTICLE_TYPES.register("glow",
			() -> new GlowParticleType());

	public static final RegistryObject<ParticleType<DarkColorParticleData>> dark_glow = PARTICLE_TYPES
			.register("dark_glow", () -> new DarkGlowParticleType());

	public static final RegistryObject<ParticleType<SerpentParticleData>> serpent = PARTICLE_TYPES.register("serpent",
			() -> new SerpentParticleType());

	public static final RegistryObject<ParticleType<HitColorParticleData>> hit_glow = PARTICLE_TYPES
			.register("hit_glow", () -> new HitGlowParticleType());

	public static final RegistryObject<ParticleType<ColoredDynamicData>> line = PARTICLE_TYPES.register("line",
			() -> new LineParticleType());

	public static RegistryObject<ParticleType<ColorLightningData>> lightning_bolt = PARTICLE_TYPES
			.register("lightning_bolt", () -> new LightningParticleType());

	public static final RegistryObject<ParticleType<BloodCellData>> blood_cell = PARTICLE_TYPES.register("blood_cell",
			() -> new BloodCellParticleType());
	public static final RegistryObject<ParticleType<BloodClawData>> blood_claw = PARTICLE_TYPES.register("blood_claw",
			() -> new BloodClawParticleType());

	public static final RegistryObject<ParticleType<AbsorbedBloodCellData>> absorbed_blood_cell = PARTICLE_TYPES
			.register("absorbed_blood_cell", () -> new AbsorbedBloodCellParticleType());

	@SubscribeEvent
	public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
		Minecraft.getInstance().particles.registerFactory(glow.get(), GlowParticleFactory::new);
		Minecraft.getInstance().particles.registerFactory(dark_glow.get(), DarkGlowParticleFactory::new);
		Minecraft.getInstance().particles.registerFactory(hit_glow.get(), HitGlowParticleFactory::new);
		Minecraft.getInstance().particles.registerFactory(line.get(), ParticleLineFactory::new);
		Minecraft.getInstance().particles.registerFactory(serpent.get(), SerpentParticleFactory::new);
		Minecraft.getInstance().particles.registerFactory(lightning_bolt.get(), ParticleLightningFactory::new);
		Minecraft.getInstance().particles.registerFactory(blood_cell.get(), BloodCellParticleFactory::new);
		Minecraft.getInstance().particles.registerFactory(blood_claw.get(), BloodClawParticleFactory::new);

		Minecraft.getInstance().particles.registerFactory(absorbed_blood_cell.get(),
				AbsrobedBloodCellParticleFactory::new);

	}
}

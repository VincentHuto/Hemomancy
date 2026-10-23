package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.particle.data.*;
import com.vincenthuto.hemomancy.common.particle.type.HemoParticleType;
import com.vincenthuto.hemomancy.common.particle.data.HitColorParticleData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Particle types. Their client providers are registered by {@code client.registry.ParticleProviderRegistrar}. */
public class ParticleInit {

	
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
			.create(Registries.PARTICLE_TYPE, Hemomancy.MOD_ID);

	public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOptions>> serpent = PARTICLE_TYPES.register("serpent",
			() -> new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec));

	public static final DeferredHolder<ParticleType<?>, ParticleType<HitColorParticleData>> hit_glow = PARTICLE_TYPES
			.register("hit_glow", () -> new HemoParticleType<>(HitColorParticleData.MAP_CODEC, HitColorParticleData.STREAM_CODEC));

	public static final DeferredHolder<ParticleType<?>, ParticleType<WillAbsorptionGlowParticleData>> will_absorption_glow =
			PARTICLE_TYPES.register("will_absorption_glow", () -> new HemoParticleType<>(WillAbsorptionGlowParticleData.MAP_CODEC, WillAbsorptionGlowParticleData.STREAM_CODEC));

	public static final DeferredHolder<ParticleType<?>, ParticleType<DaemonDiffuseGlowParticleData>> daemon_diffuse_glow =
			PARTICLE_TYPES.register("daemon_diffuse_glow", () -> new HemoParticleType<>(DaemonDiffuseGlowParticleData.MAP_CODEC, DaemonDiffuseGlowParticleData.STREAM_CODEC));

	public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOptions>> hermit_edge_glow =
			PARTICLE_TYPES.register("hermit_edge_glow",
			() -> new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec));

	public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOptions>> sporitic_spore =
			PARTICLE_TYPES.register("sporitic_spore",
			() -> new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec));
	
	public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOptions>> blood_avatar_hit = PARTICLE_TYPES
			.register("blood_avatar_hit",
			() -> new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec));

	public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOptions>> blood_cell = PARTICLE_TYPES.register("blood_cell",
			() -> new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec));
	public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOptions>> blood_claw = PARTICLE_TYPES.register("blood_claw",
			() -> new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec));

	public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOptions>> absorbed_blood_cell = PARTICLE_TYPES
			.register("absorbed_blood_cell",
			() -> new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec));

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> abocipher = PARTICLE_TYPES.register("abocipher",
			() -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> lethean_drip = PARTICLE_TYPES.register(
			"lethean_drip", () -> new SimpleParticleType(false));
}

package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.LetheanDripParticle;
import com.vincenthuto.hemomancy.client.particle.data.*;
import com.vincenthuto.hemomancy.client.particle.factory.*;
import com.vincenthuto.hemomancy.client.particle.type.*;
import com.vincenthuto.hemomancy.common.particle.data.HitColorParticleData;
import com.vincenthuto.hemomancy.common.particle.type.HitGlowParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public class ParticleInit {

	
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
			.create(Registries.PARTICLE_TYPE, Hemomancy.MOD_ID);

	public static final DeferredHolder<ParticleType<?>, ParticleType<SerpentParticleData>> serpent = PARTICLE_TYPES.register("serpent",
            SerpentParticleType::new);

	public static final DeferredHolder<ParticleType<?>, ParticleType<HitColorParticleData>> hit_glow = PARTICLE_TYPES
			.register("hit_glow", HitGlowParticleType::new);

	public static final DeferredHolder<ParticleType<?>, ParticleType<WillAbsorptionGlowParticleData>> will_absorption_glow =
			PARTICLE_TYPES.register("will_absorption_glow", WillAbsorptionGlowParticleType::new);

	public static final DeferredHolder<ParticleType<?>, ParticleType<DaemonDiffuseGlowParticleData>> daemon_diffuse_glow =
			PARTICLE_TYPES.register("daemon_diffuse_glow", DaemonDiffuseGlowParticleType::new);

	public static final DeferredHolder<ParticleType<?>, ParticleType<HermitEdgeGlowParticleData>> hermit_edge_glow =
			PARTICLE_TYPES.register("hermit_edge_glow", HermitEdgeGlowParticleType::new);

	public static final DeferredHolder<ParticleType<?>, ParticleType<SporiticSporeParticleData>> sporitic_spore =
			PARTICLE_TYPES.register("sporitic_spore", SporiticSporeParticleType::new);
	
	public static final DeferredHolder<ParticleType<?>, ParticleType<BloodAvatarHitParticleData>> blood_avatar_hit = PARTICLE_TYPES
			.register("blood_avatar_hit", BloodAvatarHitParticleType::new);

	public static final DeferredHolder<ParticleType<?>, ParticleType<BloodCellData>> blood_cell = PARTICLE_TYPES.register("blood_cell",
			() -> new BloodCellParticleType());
	public static final DeferredHolder<ParticleType<?>, ParticleType<RitePillarData>> rite_pillar =
			PARTICLE_TYPES.register("rite_pillar", RitePillarParticleType::new);
	public static final DeferredHolder<ParticleType<?>, ParticleType<BloodClawData>> blood_claw = PARTICLE_TYPES.register("blood_claw",
			() -> new BloodClawParticleType());

	public static final DeferredHolder<ParticleType<?>, ParticleType<AbsorbedBloodCellData>> absorbed_blood_cell = PARTICLE_TYPES
			.register("absorbed_blood_cell", AbsorbedBloodCellParticleType::new);

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> abocipher = PARTICLE_TYPES.register("abocipher",
			() -> new SimpleParticleType(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> lethean_drip = PARTICLE_TYPES.register(
			"lethean_drip", () -> new SimpleParticleType(false));

	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
		Minecraft.getInstance().particleEngine.register(abocipher.get(), AbocipherParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(lethean_drip.get(), LetheanDripParticle.Provider::new);
		Minecraft.getInstance().particleEngine.register(blood_avatar_hit.get(), BloodAvatarHitParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(hit_glow.get(), HitGlowParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(will_absorption_glow.get(), WillAbsorptionGlowParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(daemon_diffuse_glow.get(), DaemonDiffuseGlowParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(hermit_edge_glow.get(), HermitEdgeGlowParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(sporitic_spore.get(), SporiticSporeParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(serpent.get(), SerpentParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(blood_cell.get(), BloodCellParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(rite_pillar.get(), RitePillarParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(blood_claw.get(), BloodClawParticleFactory::new);
		Minecraft.getInstance().particleEngine.register(absorbed_blood_cell.get(), AbsorbedBloodCellParticleFactory::new);

	}
}

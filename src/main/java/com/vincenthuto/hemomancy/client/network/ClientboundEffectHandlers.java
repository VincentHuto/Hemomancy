package com.vincenthuto.hemomancy.client.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.client.render.cinematic.scene.BossSceneCatalog;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameClientState;
import com.vincenthuto.hemomancy.common.network.boss.BossBarFramePacket;
import com.vincenthuto.hemomancy.common.network.boss.BossScenePacket;
import com.vincenthuto.hemomancy.client.render.entity.boss.MycophantGlimpse;
import com.vincenthuto.hemomancy.client.data.MycophantIntroClientState;
import com.vincenthuto.hemomancy.client.data.ActiveBloodCraftClientData;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import com.vincenthuto.hemomancy.client.data.BloodBindingTendrilClientState;
import com.vincenthuto.hemomancy.client.data.CrimsonFireClientState;
import com.vincenthuto.hemomancy.client.data.MonolithicDislocationClientState;
import com.vincenthuto.hemomancy.client.data.VeinSpiderCourierClientData;
import com.vincenthuto.hemomancy.client.event.AxonalTransductionClient;
import com.vincenthuto.hemomancy.client.event.CardinalRiteImpactClientEvents;
import com.vincenthuto.hemomancy.client.event.LayerEvents;
import com.vincenthuto.hemomancy.client.event.LivingFlailImpactClientEvents;
import com.vincenthuto.hemomancy.client.particle.ParticleBurstEffects;
import com.vincenthuto.hemomancy.client.player.BloodVialInjectionClientState;
import com.vincenthuto.hemomancy.client.player.CastingAnimationClientState;
import com.vincenthuto.hemomancy.client.player.HematicMicroscopeClientState;
import com.vincenthuto.hemomancy.client.player.LivingStaffMorphClientState;
import com.vincenthuto.hemomancy.client.player.PlayerAnimationClientState;
import com.vincenthuto.hemomancy.client.render.item.harbinger.CellHandParticleEffects;
import com.vincenthuto.hemomancy.client.render.world.BlackVeilRenderer;
import com.vincenthuto.hemomancy.client.render.world.BloodFlowEffects;
import com.vincenthuto.hemomancy.client.render.world.ClawSlashRenderer;
import com.vincenthuto.hemomancy.client.render.world.FerricDuctilisEffects;
import com.vincenthuto.hemomancy.client.render.world.LuxUmbraEffects;
import com.vincenthuto.hemomancy.client.render.world.ManipulationAmbientParticles;
import com.vincenthuto.hemomancy.client.render.world.ManipulationVisualRenderer;
import com.vincenthuto.hemomancy.client.render.world.SanguineMonolithShatterRenderer;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.client.data.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.client.screen.overlay.SanguineOmenOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.WillPresenceOverlay;
import com.vincenthuto.hemomancy.client.sound.AntecedentClientEffects;
import com.vincenthuto.hemomancy.client.sound.ClairaudiographSounds;
import com.vincenthuto.hemomancy.client.data.EarthenVeinTravelClientState;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.network.AntecedentEffectPacket;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import com.vincenthuto.hemomancy.common.network.ClairaudiographSoundPacket;
import com.vincenthuto.hemomancy.common.network.ClientPayloadHandlers;
import com.vincenthuto.hemomancy.common.network.ConductionPathPacket;
import com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection;
import com.vincenthuto.hemomancy.common.network.PacketHematicMicroscopeViewing;
import com.vincenthuto.hemomancy.common.network.axonal.AxonalStatePacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.CardinalRiteEventPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodCraftRing;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureFeed;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketCardinalRiteStaffPlanting;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLivingStaffMorph;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketMycophantIntro;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPlayerAnimation;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.SyncBloodBindingTendrilS2CPacket;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hemomancy.common.network.particle.CardinalRiteImpactPacket;
import com.vincenthuto.hemomancy.common.network.particle.EntityVisualPacket;
import com.vincenthuto.hemomancy.common.network.particle.LivingFlailImpactPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationAccentPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationFlowPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.network.particle.NerveLinkPacket;
import com.vincenthuto.hemomancy.common.network.particle.ParticleBurstPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnBlackVeilPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnBrazierItemAbsorptionParticlesPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnClawSlashPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnSanguineOmenEffectPacket;
import com.vincenthuto.hemomancy.common.network.routing.PacketSpawnVeinSpiderCourier;
import com.vincenthuto.hemomancy.common.network.vein.EarthenVeinTravelVisualPacket;
import com.vincenthuto.hemomancy.common.network.will.WillPresenceCuePacket;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client halves of client-bound payloads, registered with {@link ClientPayloadHandlers}. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientboundEffectHandlers {

	private ClientboundEffectHandlers() {
	}

	@SubscribeEvent
	public static void register(FMLClientSetupEvent event) {
		ClientPayloadHandlers.register(AntecedentEffectPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(AxonalStatePacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(BloodFlowPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(CardinalRiteEventPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(CardinalRiteImpactPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(CastingAnimationPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(ClairaudiographSoundPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(ConductionPathPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(EarthenVeinTravelVisualPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(EntityVisualPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(LivingFlailImpactPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(ManipulationAccentPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(ManipulationFlowPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(ManipulationVisualPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(NerveLinkPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketBloodCraftRing.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketBloodStructureFeed.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketBloodVialInjection.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketCardinalRiteStaffPlanting.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketCurvedHornAnimation.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketHematicMicroscopeViewing.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketLivingStaffMorph.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketMycophantIntro.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(BossBarFramePacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(BossScenePacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketSpawnVeinSpiderCourier.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncPlayerAnimation.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(ParticleBurstPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(SpawnBlackVeilPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(SpawnBrazierItemAbsorptionParticlesPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(SpawnClawSlashPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(SpawnSanguineOmenEffectPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(SyncBloodBindingTendrilS2CPacket.TYPE, ClientboundEffectHandlers::handle);
		ClientPayloadHandlers.register(WillPresenceCuePacket.TYPE, ClientboundEffectHandlers::handle);
	}

	static void handle(AxonalStatePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> AxonalTransductionClient.update(packet));
	}

	static void handle(BloodFlowPacket packet,IPayloadContext context){context.enqueueWork(()->BloodFlowEffects.accept(packet));}

	static void handle(CardinalRiteEventPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> CardinalRiteEventClientState.accept(packet));
	}

	static void handle(CardinalRiteImpactPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			CardinalRiteImpactClientEvents.start(packet.durationTicks(), packet.seed());
			if (SanguineOmenOverlay.instance != null) {
				SanguineOmenOverlay.instance.start(packet.durationTicks(), packet.peakAlpha(),
						packet.seed(), SanguineOmenOverlay.Mode.SCREEN_OVERLAY);
			}
		});
	}

	static void handle(CastingAnimationPacket packet,IPayloadContext context) {
		context.enqueueWork(() -> CastingAnimationClientState.accept(packet));
	}

	static void handle(ClairaudiographSoundPacket p,IPayloadContext c){c.enqueueWork(()->ClairaudiographSounds.accept(p));}

	static void handle(ConductionPathPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> FerricDuctilisEffects.accept(packet));
	}

	static void handle(EarthenVeinTravelVisualPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> EarthenVeinTravelClientState.update(packet));
	}

	static void handle(EntityVisualPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			switch (packet.kind()) {
				case MONOLITHIC_DISLOCATION ->
						MonolithicDislocationClientState.markActive(packet.entityId(), packet.durationTicks());
				case CRIMSON_FIRE -> CrimsonFireClientState.markActive(packet.entityId(), packet.durationTicks());
			}
		});
	}

	static void handle(LivingFlailImpactPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> LivingFlailImpactClientEvents.start(packet.charge(), packet.seed()));
	}

	static void handle(ManipulationAccentPacket packet,IPayloadContext context) {
		context.enqueueWork(()->ManipulationAmbientParticles.accept(packet));
	}

	static void handle(ManipulationFlowPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> LuxUmbraEffects.accept(packet));
	}

	static void handle(ManipulationVisualPacket p, IPayloadContext context) {
		context.enqueueWork(() -> ManipulationVisualRenderer.accept(p));
	}

	static void handle(NerveLinkPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> FerricDuctilisEffects.accept(packet));
	}

	static void handle(final PacketBloodCraftRing msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ActiveBloodCraftClientData.addRing(new ActiveBloodCraftClientData.CraftRingEntry(
					msg.center(), msg.startRadius(), msg.centerY(), msg.durationTicks()));
		});
	}

	static void handle(final PacketBloodStructureFeed msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (msg.clear()) {
				ActiveBloodStructureFeedClientData.clear(msg.channelId(), msg.positions());
			} else {
				ActiveBloodStructureFeedClientData.upsert(msg.channelId(), msg.positions(), msg.progress(), msg.visibleTicks(),
						msg.style(), msg.origin());
			}
		});
	}

	static void handle(PacketBloodVialInjection packet, IPayloadContext context) {
		context.enqueueWork(() -> BloodVialInjectionClientState.accept(packet));
	}

	static void handle(PacketCardinalRiteStaffPlanting packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (packet.active()) {
				CardinalRiteStaffPlantingClientState.start(packet.casterEntityId(), packet.focus(), packet.staff());
			} else {
				CardinalRiteStaffPlantingClientState.stop(packet.casterEntityId());
			}
		});
	}

	static void handle(final PacketCurvedHornAnimation msg, final IPayloadContext ctx) {
		ctx.enqueueWork(LayerEvents::playHornAnimation);
	}

	static void handle(PacketHematicMicroscopeViewing packet, IPayloadContext context) {
		context.enqueueWork(() -> HematicMicroscopeClientState.accept(packet));
	}

	static void handle(PacketLivingStaffMorph packet, IPayloadContext context) {
		context.enqueueWork(() -> LivingStaffMorphClientState.start(packet));
	}

	static void handle(BossBarFramePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> BossBarFrameClientState.bind(packet.bar(), packet.frame()));
	}

	static void handle(BossScenePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> BossSceneDirector.INSTANCE.start(packet.scene(), packet.entityId(), packet.offset()));
	}

	static void handle(PacketMycophantIntro packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (packet.kind() == PacketMycophantIntro.GLIMPSE) {
				MycophantGlimpse.start(packet.seed());
				return;
			}
			if (packet.kind() == PacketMycophantIntro.CLEAR) {
				MycophantGlimpse.clear();
				BossScene scene = BossSceneDirector.INSTANCE.scene();
				if (scene != null && BossSceneCatalog.isMycophantTaking(scene)) BossSceneDirector.INSTANCE.clear();
			}
			MycophantIntroClientState.INSTANCE.start(packet.kind(), packet.seed());
			// the waking and rematch scenes come from the server with the boss; the capture has no boss yet
			if (packet.kind() == PacketMycophantIntro.CAPTURE)
				BossSceneDirector.INSTANCE.start(BossPresentationIds.SCENE_MYCOPHANT_CAPTURE, -1, 0);
		});
	}

	static void handle(final PacketSpawnVeinSpiderCourier msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> VeinSpiderCourierClientData.spawn(msg.from(), msg.to(), msg.stack(), msg.seed()));
	}

	static void handle(PacketSyncPlayerAnimation packet, IPayloadContext context) {
		context.enqueueWork(() -> PlayerAnimationClientState.set(packet.entityId(), packet.kind(),
				packet.active(), packet.hand()));
	}

	static void handle(ParticleBurstPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> ParticleBurstEffects.spawn(packet, context.player()));
	}

	static void handle(SpawnBlackVeilPacket msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> BlackVeilRenderer.addVeil(msg.pos(), msg.radius(), msg.durationTicks(), msg.seed()));
	}

	static void handle(SpawnBrazierItemAbsorptionParticlesPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> CellHandParticleEffects.spawnBrazierItemAbsorptionParticles(
				packet.casterId(), packet.stack(), packet.source()));
	}

	static void handle(SpawnClawSlashPacket msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> ClawSlashRenderer.spawn(msg.pos(), msg.forward(), msg.color(), msg.ambush(), msg.scale(), msg.seed()));
	}

	static void handle(final SpawnSanguineOmenEffectPacket msg, final IPayloadContext ctxSupplier) {
		if (ctxSupplier.player() == null) return;
		ctxSupplier.enqueueWork(() -> {
			SanguineMonolithShatterRenderer.spawnOmenBurst(msg.getPos());
			if (SanguineOmenOverlay.instance != null) {
				SanguineOmenOverlay.Mode mode = msg.isScreenOverlay()
						? SanguineOmenOverlay.Mode.SCREEN_OVERLAY
						: SanguineOmenOverlay.Mode.WORLD_GRADE;
				SanguineOmenOverlay.instance.start(msg.getDurationTicks(), msg.getPeakAlpha(), msg.getSeed(), mode);
			}
		});
	}

	static void handle(SyncBloodBindingTendrilS2CPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> BloodBindingTendrilClientState.start(packet.casterId(), packet.targetId(),
				packet.durationTicks(), packet.seed()));
	}

	static void handle(final WillPresenceCuePacket msg, final IPayloadContext context) {
		if (context.player() == null) return;
		context.enqueueWork(() -> {
			context.player().level().playLocalSound(msg.position().x, msg.position().y, msg.position().z,
					SoundInit.ENTITY_WILL_PRESENCE_STING.get(), SoundSource.HOSTILE, 0.55F, 1.35F, false);
			if (WillPresenceOverlay.instance != null) {
				WillPresenceOverlay.instance.trigger();
			}
		});
	}

	static void handle(AntecedentEffectPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> AntecedentClientEffects.accept(packet));
	}
}

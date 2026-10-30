package com.vincenthuto.hemomancy.client.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.ClientLiberScreenHooks;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.data.BloodFlowDiagnosticsClientData;
import com.vincenthuto.hemomancy.client.data.BloodlinePoolClientData;
import com.vincenthuto.hemomancy.client.data.ChamberOfWillClientData;
import com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData;
import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.client.data.ManipulationCostDiagnosticsClientData;
import com.vincenthuto.hemomancy.client.data.ManipulationSlotDiagnosticsClientData;
import com.vincenthuto.hemomancy.client.data.MaxBloodDiagnosticsClientData;
import com.vincenthuto.hemomancy.client.data.MycophantFightClientData;
import com.vincenthuto.hemomancy.client.data.NpcProgressionMarkerClientState;
import com.vincenthuto.hemomancy.client.data.QliphothBloomClientData;
import com.vincenthuto.hemomancy.client.data.SutureLinkClientData;
import com.vincenthuto.hemomancy.client.data.VesperFightClientData;
import com.vincenthuto.hemomancy.client.event.ManipulationChargeInput;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.VesperFightFloorRenderer;
import com.vincenthuto.hemomancy.client.render.world.zone.RiteSpaceClientData;
import com.vincenthuto.hemomancy.client.render.world.zone.RiteZoneClientData;
import com.vincenthuto.hemomancy.client.screen.item.CircusSchoolLedgerState;
import com.vincenthuto.hemomancy.client.screen.manips.ClientManipulationCooldowns;
import com.vincenthuto.hemomancy.client.screen.overlay.ChamberVisitOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.CircusPerceptionOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.StillArtCooldownOverlay;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.network.ClientPayloadHandlers;
import com.vincenthuto.hemomancy.common.network.ManipulationInterruptedPacket;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRites;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodFlowDiagnostics;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePool;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncChamberOfWill;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncChamberVisit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFaneBoundaries;
import com.vincenthuto.hemomancy.common.network.heartnode.HeartNodeEnvelopePacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFungalProjection;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMaxBloodDiagnostics;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMycophantFightScene;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncQliphothBlooms;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncVesperFightScene;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ManipCooldownPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.PacketSyncManipulationCostDiagnostics;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.PacketSyncManipulationSlotDiagnostics;
import com.vincenthuto.hemomancy.common.network.capa.unstained.PacketSyncUnstainedProgress;
import com.vincenthuto.hemomancy.common.network.capa.unstained.StillArtCooldownPacket;
import com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusPerception;
import com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusSchool;
import com.vincenthuto.hemomancy.common.network.dialogue.PacketSyncNpcProgressionMarkers;
import com.vincenthuto.hemomancy.common.network.routing.PacketSyncSutureLinks;
import com.vincenthuto.hemomancy.common.network.rite.RiteSpaceSyncPacket;
import com.vincenthuto.hemomancy.common.network.rite.RiteZoneSyncPacket;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client halves of client-bound payloads, registered with {@link ClientPayloadHandlers}. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientboundStateHandlers {

	private ClientboundStateHandlers() {
	}

	@SubscribeEvent
	public static void register(FMLClientSetupEvent event) {
		ClientPayloadHandlers.register(ManipCooldownPacket.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(ManipulationInterruptedPacket.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncActiveRites.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncBloodFlowDiagnostics.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncBloodlinePool.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncChamberOfWill.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncChamberVisit.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncCircusPerception.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncCircusSchool.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncFaneBoundaries.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(HeartNodeEnvelopePacket.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncFungalProjection.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncLiberKnowledge.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncManipulationCostDiagnostics.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncManipulationSlotDiagnostics.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncMaxBloodDiagnostics.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncMycophantFightScene.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncNpcProgressionMarkers.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncQliphothBlooms.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncSutureLinks.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncUnstainedProgress.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(PacketSyncVesperFightScene.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(RiteSpaceSyncPacket.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(RiteZoneSyncPacket.TYPE, ClientboundStateHandlers::handle);
		ClientPayloadHandlers.register(StillArtCooldownPacket.TYPE, ClientboundStateHandlers::handle);
	}

	static void handle(final ManipCooldownPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (message.cooldownTicks() > 0) ManipulationChargeInput.manipulationCastAccepted();
			long now = ctx.player().level().getGameTime();
			ClientManipulationCooldowns.start(message.manipulationId(), message.cooldownTicks(), now);
		});
	}

	static void handle(ManipulationInterruptedPacket packet, IPayloadContext context) {
		context.enqueueWork(ManipulationChargeInput::interruptManipulationCharge);
	}

	static void handle(final PacketSyncActiveRites msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ActiveRiteClientData.set(msg.entries());
		});
	}

	static void handle(PacketSyncBloodFlowDiagnostics msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> BloodFlowDiagnosticsClientData.set(msg.snapshot()));
	}

	static void handle(final PacketSyncBloodlinePool msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			BloodlinePoolClientData.set(msg.poolVolume(), msg.poolMax(), msg.memberCount());
		});
	}

	static void handle(PacketSyncChamberOfWill msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> ChamberOfWillClientData.set(msg.skyTheme(), msg.tier(), msg.radius(), msg.qliphothPomesConsumed()));
	}

	static void handle(PacketSyncChamberVisit packet, IPayloadContext context) {
		context.enqueueWork(() -> ChamberVisitOverlay.setState(packet.active(), packet.mode(),
				packet.remainingTicks(), packet.totalTicks()));
	}

	static void handle(PacketSyncCircusPerception packet, IPayloadContext context) {
		context.enqueueWork(() -> CircusPerceptionOverlay.setState(packet.acclimation(), packet.active()));
	}

	static void handle(PacketSyncCircusSchool packet, IPayloadContext context) {
		context.enqueueWork(() -> CircusSchoolLedgerState.set(packet.progress()));
	}

	static void handle(final PacketSyncFaneBoundaries msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> FaneBoundaryClientData.setEntries(msg.entries()));
	}

	static void handle(HeartNodeEnvelopePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> HeartNodeClientState.accept(packet.envelopes()));
	}

	static void handle(PacketSyncFungalProjection packet, IPayloadContext context) {
		context.enqueueWork(() -> FungalWhisperVignetteOverlay.setProjectionState(
				packet.active(), packet.remainingTicks(), packet.totalTicks()));
	}

	static void handle(PacketSyncLiberKnowledge msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}
			var knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
			Set<ResourceLocation> before = Set.copyOf(knowledge.getUnlockedEntries());
			LiberKnowledge synced = new LiberKnowledge();
			msg.entrySources().forEach((entry, sources) -> {
				if (sources.isEmpty()) {
					synced.unlockEntry(entry, CommonDiscoverySource.OTHER);
				} else {
					for (IDiscoverySource source : sources) {
						synced.unlockEntry(entry, source);
					}
				}
			});
			for (ResourceLocation memo : msg.memos()) {
				synced.recordMemo(memo);
			}
			for (ResourceLocation memo : msg.pendingMemos()) {
				synced.recordPendingMemo(memo);
			}
			knowledge.setFrom(synced);
			// If the player currently has a Liber/guide screen open, rebuild
			// its visible chapter list against the freshly-synced knowledge
			// instead of waiting for them to close+reopen.
			ClientLiberScreenHooks.knowledgeSynced(
					player, knowledge, before, Set.copyOf(knowledge.getUnlockedEntries()), msg.markUnreadEntries());
		});
	}

	static void handle(PacketSyncManipulationCostDiagnostics msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> ManipulationCostDiagnosticsClientData.set(msg.snapshot()));
	}

	static void handle(PacketSyncManipulationSlotDiagnostics msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> ManipulationSlotDiagnosticsClientData.set(msg.snapshot()));
	}

	static void handle(PacketSyncMaxBloodDiagnostics msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> MaxBloodDiagnosticsClientData.set(msg.snapshot()));
	}

	static void handle(PacketSyncMycophantFightScene packet, IPayloadContext context) { context.enqueueWork(() -> { if (packet.active()) MycophantFightClientData.activate(packet.center()); else MycophantFightClientData.clear(); }); }

	static void handle(PacketSyncNpcProgressionMarkers packet, IPayloadContext context) {
		context.enqueueWork(() -> NpcProgressionMarkerClientState.replace(packet.markers()));
	}

	static void handle(final PacketSyncQliphothBlooms msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			QliphothBloomClientData.set(msg.entries());
		});
	}

	static void handle(final PacketSyncSutureLinks msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> SutureLinkClientData.setEntries(msg.entries().stream()
				.map(entry -> new SutureLinkClientData.Entry(entry.pos(), entry.mode(), entry.bloodlineEnabled()))
				.toList()));
	}

	static void handle(final PacketSyncUnstainedProgress msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player != null) {
				var progress = HemoCapabilityAccess.getUnstainedProgress(player);
				progress.setBegunPurification(msg.begunPurification());
				progress.setPurity(msg.purity());
				progress.setClarityUnlocked(msg.clarityUnlocked());
				progress.setClarity(msg.clarity());
				// Replay milestones from server counts
				for (int i = progress.getHemoMobKills(); i < msg.hemoMobKills(); i++) progress.addHemoMobKill();
				for (int i = progress.getUndeadKills(); i < msg.undeadKills(); i++) progress.addUndeadKill();
				for (int i = progress.getHostileKills(); i < msg.hostileKills(); i++) progress.addHostileKill();
				for (int i = progress.getFlawlessKills(); i < msg.flawlessKills(); i++) progress.addFlawlessKill();
				for (int i = progress.getAnimalsBreed(); i < msg.animalsBreed(); i++) progress.addAnimalBreed();
				for (int i = progress.getCropsPlanted(); i < msg.cropsPlanted(); i++) progress.addCropPlanted();
				for (int i = progress.getAdvancementsEarned(); i < msg.advancementsEarned(); i++) progress.addAdvancementEarned();
				for (int i = progress.getNightsSlept(); i < msg.nightsSlept(); i++) progress.addNightSlept();
				for (int i = progress.getPetsHealed(); i < msg.petsHealed(); i++) progress.addPetHealed();
				progress.setSleptWithHemolysis(msg.sleptWithHemolysis());
				progress.setKilledFirstHemoMob(msg.killedFirstHemoMob());
				progress.setReachedAbstinence(msg.reachedAbstinence());
				progress.setEmptiedBlood(msg.emptiedBlood());
				progress.setEarnedAdvancement(msg.earnedAdvancement());
				progress.setSilverWardEnabled(msg.silverWardEnabled());
				progress.setVerdigrisAuraEnabled(msg.verdigrisAuraEnabled());
				progress.setUsedAltarOfCleansing(msg.usedAltarOfCleansing());
				progress.setInfectionSuppressed(msg.infectionSuppressed());
				progress.setClarityPrepared(msg.clarityPrepared());
				progress.setAnnettaSeveranceUnlocked(msg.annettaSeveranceUnlocked());
				progress.setAcceptedObservances(msg.acceptedObservances());
				progress.setClaimedObservances(msg.claimedObservances());
				progress.setBaselineRestored(msg.baselineRestored());
				progress.setNovitiateRetortComplete(msg.novitiateRetortComplete());
				progress.setNovitiateDewProduced(msg.novitiateDewProduced());
				progress.setNovitiateBlocksConsecrated(msg.novitiateBlocksConsecrated());
				progress.setNovitiateProtectionComplete(msg.novitiateProtectionComplete());
				ClientLiberScreenHooks.progressChanged(player);
			}
		});
	}

	static void handle(PacketSyncVesperFightScene packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (packet.active()) {
				if (!packet.center().equals(VesperFightClientData.center())) {
					VesperFightFloorRenderer.clear();
				}
				VesperFightClientData.activate(packet.center());
			} else {
				VesperFightClientData.clear();
				VesperFightFloorRenderer.clear();
			}
		});
	}

	static void handle(RiteSpaceSyncPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> RiteSpaceClientData.set(packet.spaces()));
	}

	static void handle(RiteZoneSyncPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> RiteZoneClientData.set(packet.zones()));
	}

	static void handle(final StillArtCooldownPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> StillArtCooldownOverlay.startCooldown(message.cooldownTicks()));
	}
}

package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.world.GardensNetwork;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.BloodwoodGrowthHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.SynapticLoadoutLockout;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.HoldfastMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.KinbudSecondHead;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingAnchorState;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingFeeding;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingHomeConditions;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLinks;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingRegrowthQueue;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingSession;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryWorldEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.ConserveStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.mission.circus.CircusPracticalController;
import com.vincenthuto.hemomancy.common.effect.LanternTickLightManager;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialInjectionAnimation;
import com.vincenthuto.hemomancy.common.item.harbinger.MicroscopeExamination;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleResonanceState;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.TerrestrialSpeculumPlantingManager;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeLodestoneFields;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.FerricDuctilisStatusVisuals;
import com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChargeClock;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationStatusVisuals;
import com.vincenthuto.hemomancy.common.manipulation.MortemStatusVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ThermalStatusVisuals;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastingAnimationManager;
import com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationManager;
import com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteEvents;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceManager;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ConductionManager;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Discharge;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricContacts;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.BlackVeilCovenantManager;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.mission.vicar.VoyagerVesselGuidance;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineChatRateLimit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.WorkingCraftRites;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.ActiveEngramLooms;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing;
import com.vincenthuto.hemomancy.common.world.vein.EarthenVeinTravelManager;
import com.vincenthuto.hemomancy.common.worldgen.feature.CorticalArchipelagoFeature;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * The one home for session-only server state: static per-player and per-world maps that never reach a save.
 * Every such map is cleared here when a server starts and when it stops, and per-player entries end here on logout.
 * Logout drops only what was already dropped before, or what is pure presentation: cooldowns and timers that must
 * outlast a relog stay until the server stops.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TransientServerStateEvents {
	private TransientServerStateEvents() {
	}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		clearTransientState();
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		clearTransientState();
	}

	/**
	 * Before the chunks unload: every loaded kit Saint's chamber goes back to how it stood (the Saint arena's restore
	 * contract), so a stopped server never saves a changed chamber.
	 */
	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		for (net.minecraft.server.level.ServerLevel level : event.getServer().getAllLevels()) {
			// collect first: a restore discards the entities the arena owns, which would change the list being walked
			java.util.List<com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity> saints = new java.util.ArrayList<>();
			for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
				if (entity instanceof com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity saint) saints.add(saint);
			}
			saints.forEach(com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity::restoreArenaNow);
		}
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		endPlayerSession(event.getEntity());
	}

	private static void clearTransientState() {
		com.vincenthuto.hemomancy.common.entity.npc.dialogue.OfferedDialogues.clearSessionState();
		BloodManipulation.clearSessionState();
		HematicCommandManager.clearSessionState();
		StillArt.clearSessionState();
		SporiticThuribleResonanceState.clearSessionState();
		BloodwoodGrowthHandler.clearSessionState();
		EquippedMorphlingEvents.clearSessionState();
		MorphlingAnchorState.clearSessionState();
		MorphlingHomeConditions.clearSessionState();
		MorphlingLinks.clearSessionState();
		MorphlingRegrowthQueue.clearSessionState();
		KinbudSecondHead.clearSessionState();
		MorphlingFeeding.clearSessionState();
		MorphlingSession.clearSessionState();
		HoldfastMorphlingItem.clearSessionState();
		BlackVeilCovenantManager.clearSessionState();
		TemporaryIceManager.clearSessionState();
		BloodStructureFeedManager.clear();
		BloodInfusionManager.clear();
		PendingBloodCraftManager.clear();
		WorkingCraftRites.clear();
		HematicSalvageEvents.clearSessionState();
		RootedStateHelper.clearSessionState();
		ConserveStateHelper.clearSessionState();
		ThelemicMemoryEvents.clearSessionState();
		ThelemicMemoryWorldEvents.clearSessionState();
		SynapticLoadoutLockout.clearSessionState();
		ManipulationChargeClock.clearSessionState();
		BloodlineChatRateLimit.clearSessionState();
		com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRequestLimiter.clearSessionState();
		com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter.clearSessionState();
		CastingAnimationManager.clearSessionState();
		AvatarManifestationManager.clearSessionState();
		AxonalTransductionManager.clearSessionState();
		com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService.clearSessionState();
		ConductionManager.clearSessionState();
		Discharge.clearSessionState();
		DuctilisLightningEffects.clearSessionState();
		FerricContacts.clearSessionState();
		FerricDuctilisStatusVisuals.clearSessionState();
		ManipulationStatusVisuals.clearSessionState();
		MortemStatusVisuals.clearSessionState();
		ThermalStatusVisuals.clearSessionState();
		EarthenVeinTravelManager.clearSessionState();
		HarbingerCardinalRiteEvents.clearSessionState();
		PacketLedgerAction.clearSessionState();
		EngramTracing.clear();
		ActiveEngramLooms.clear();
		EarlyInitiation.clearSessionState();
		VoyagerVesselGuidance.clearSessionState();
		com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatSearch.clearSessionState();
		CircusPracticalController.clearSessionState();
		BloodVialInjectionAnimation.clearSessionState();
		MicroscopeExamination.clearSessionState();
		TerrestrialSpeculumPlantingManager.clearSessionState();
		LivingAxeLodestoneFields.clearSessionState();
		SporiticThuribleEvents.clearSessionState();
		LanternTickLightManager.clearSessionState();
		NpcProgressionMarkerEvents.clearSessionState();
		FoundingFaneEvents.clearSessionState();
		BloodFlowLedger.clearSessionState();
		CorticalArchipelagoFeature.clearSessionState();
		GardensNetwork.clearSessionState();
		com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchors.clearServer();
		com.vincenthuto.hemomancy.common.tile.harbinger.functional.VigilSites.clearServer();
		MausoleumEvents.clearSessionState();
		HeartNodeEnvelopeSync.clearSessionState();
		HeartNodeEvents.clearSessionState();
		com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSequences.clearSessionState();
		com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHealing.clear();
		com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines.clearSessionState();
		com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot.clearSessionState();
		com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter.clearSessionState();
		com.vincenthuto.hemomancy.common.entity.boss.BossHold.clearSessionState();
	}

	/** Ends one player's session state. Teardowns that cancel, restore or move something run before plain removals. */
	public static void endPlayerSession(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			com.vincenthuto.hemomancy.common.entity.npc.dialogue.OfferedDialogues.logout(serverPlayer);
			com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter.logout(serverPlayer);
			ManipulationChannelManager.logout(serverPlayer);
			CastingAnimationManager.logout(serverPlayer);
			AvatarManifestationManager.logout(serverPlayer);
			AxonalTransductionManager.logout(serverPlayer);
			com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService.logout(serverPlayer);
			EarthenVeinTravelManager.logout(serverPlayer);
			CircusPracticalController.logout(serverPlayer);
			TerrestrialSpeculumPlantingManager.logout(serverPlayer);
			FoundingFaneEvents.clearPreviewRelation(serverPlayer);
			HeartNodeEnvelopeSync.logout(serverPlayer);
			HeartNodeEvents.logout(serverPlayer);
			LanternTickLightManager.logout(serverPlayer);
			com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSequences.logout(serverPlayer);
			com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter.logout(serverPlayer);
		}
		ManipulationReactiveEvents.logout(player);
		ConductionManager.logout(player);
		EarlyInitiation.logout(player);
		VoyagerVesselGuidance.logout(player);
		com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatSearch.logout(player.getUUID());
		BloodVialInjectionAnimation.logout(player);
		MicroscopeExamination.logout(player);
		EngramTracing.end(player);

		SynapticLoadoutLockout.clear(player.getUUID());
		ThelemicMemoryEvents.logout(player);
		HematicRiposteEvents.logout(player);
		ManipulationChargeClock.release(player);
		NpcProgressionMarkerEvents.logout(player);
		BloodlineChatRateLimit.logout(player);
		com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRequestLimiter.logout(player);
		BloodFlowLedger.logout(player);
		SporiticThuribleEvents.logout(player);
		GardensNetwork.logout(player);
		MausoleumEvents.logout(player);
		EquippedMorphlingEvents.logout(player);
		// A wound queued before a relog still regrows inside its window, so MorphlingRegrowthQueue keeps its entry.
		MorphlingLinks.logout(player.getUUID());
		MorphlingAnchorState.logout(player.getUUID());
		MorphlingHomeConditions.logout(player.getUUID());
		MorphlingFeeding.clear(player.getUUID());
		MorphlingSession.clear(player.getUUID());
		KinbudSecondHead.clear(player.getUUID());
		HoldfastMorphlingItem.logout(player.getUUID());
		com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHealing.end(player);
		com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines.logout(player);
		com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot.end(player);
		com.vincenthuto.hemomancy.common.entity.boss.BossHold.logout(player.getUUID());
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncDegree;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonTrialEvents;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class InitiatoryDegreeEvents {

	public static void syncDegree(ServerPlayer player, IInitiatoryDegree degree) {
		PacketHandler.sendToPlayer(player, new PacketSyncDegree(degree));
		PuppeteerSummonTrialEvents.awardOrdealRecipes(player, degree.getDegreeNumber());
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree ->
				{
					migrateLegacyState(player, degree);
					syncDegree(player, degree);
					LiberKnowledgeHelper.unlockForDegree(player, degree.getDegreeNumber());
				});
	}

	private static void migrateLegacyState(ServerPlayer player, IInitiatoryDegree degree) {
		if (degree.isQliphothCommunionDone() && !degree.hasFungalSpineGranted()) {
			degree.setFungalSpineGranted(true);
		}
		String legacyChoice = player.getPersistentData().getString(FungalGardenTravelHelper.ARCHON_CHOICE_KEY);
		if (degree.getArchonPath() == EnumArchonPath.NONE && !legacyChoice.isBlank()) {
			degree.setFungalRevelationWitnessed(true);
			if (FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE.equals(legacyChoice)) {
				degree.setArchonPath(EnumArchonPath.SILENT_ARCHON);
			} else if (FungalGardenTravelHelper.ARCHON_CHOICE_APOTHEOS.equals(legacyChoice)) {
				degree.setArchonPath(degree.getDegreeNumber() >= 8
						? EnumArchonPath.APOTHEOS : EnumArchonPath.APOTHEOS_PENDING);
			}
		}
		if (!degree.hasFoundedBloodline()) {
			Bloodline line = BloodlineSavedData.get(player.server.overworld())
					.getBloodlineForPlayer(player.getUUID());
			if (line != null && line.isValid() && player.getUUID().equals(line.getLeaderUUID())) {
				degree.setHasFoundedBloodline(true);
			}
		}
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree ->
				syncDegree(player, degree));
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide) {
			HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree ->
					syncDegree((ServerPlayer) player, degree));
		}
	}
}

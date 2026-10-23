package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncDegree;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonTrialEvents;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class InitiatoryDegreeEvents {

	public static void syncDegree(ServerPlayer player, IInitiatoryDegree degree) {
		PacketHandler.sendToPlayer(player, new PacketSyncDegree(degree));
		PacketHandler.sendToPlayer(player, new PacketSyncPomeProgress(
				degree.isQliphothCommunionDone() ? 9 : degree.getTotalPomesConsumed()));
		PuppeteerSummonTrialEvents.awardOrdealRecipes(player, degree.getDegreeNumber());
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		reconcileFoundedBloodline(player, degree);
		LiberKnowledgeHelper.unlockForDegree(player, degree.getDegreeNumber());
	}

	/**
	 * A {@code PlayerDataMigrations} entry (F-21): the Archon choice kept as a root key, read once into the degree's
	 * path, which is the only authority, and then removed. A path already chosen wins over the old key.
	 */
	public static void migrateLegacyArchonChoice(ServerPlayer player) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		var persistent = player.getPersistentData();
		String legacyChoice = persistent.getString(FungalGardenTravelHelper.ARCHON_CHOICE_KEY);
		persistent.remove(FungalGardenTravelHelper.ARCHON_CHOICE_KEY);
		if (degree.getArchonPath() == EnumArchonPath.NONE && !legacyChoice.isBlank()) {
			degree.setFungalRevelationWitnessed(true);
			if (FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE.equals(legacyChoice)) {
				degree.setArchonPath(EnumArchonPath.SILENT_ARCHON);
			} else if (FungalGardenTravelHelper.ARCHON_CHOICE_APOTHEOS.equals(legacyChoice)) {
				degree.setArchonPath(ProgressionGates.hasDegree(degree, 8)
						? EnumArchonPath.APOTHEOS : EnumArchonPath.APOTHEOS_PENDING);
			}
		}
	}

	/** Every login, not once: the bloodline record lives in world data and can name the player as leader later. */
	private static void reconcileFoundedBloodline(ServerPlayer player, IInitiatoryDegree degree) {
		if (!degree.hasFoundedBloodline()) {
			Bloodline line = BloodlineSavedData.get(player.server.overworld())
					.getBloodlineForPlayer(player.getUUID());
			if (line != null && line.isValid() && player.getUUID().equals(line.getLeaderUUID())) {
				degree.setHasFoundedBloodline(true);
			}
		}
	}
}

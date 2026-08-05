package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;

/** Resolves chapter proofs from authored milestones and migrates established saves. */
public final class HarbingerChapterProgression {
	private HarbingerChapterProgression() {
	}

	public static EnumSet<HarbingerChapterMilestone> completedChapters(ServerPlayer player) {
		migrateExistingProgress(player);
		EnumSet<HarbingerChapterMilestone> completed = EnumSet.noneOf(HarbingerChapterMilestone.class);
		if (FirstBloodcraftAssignmentHelper.isClaimed(player)) completed.add(HarbingerChapterMilestone.FIRST_BLOODCRAFT);
		if (FirstSeparationAssignmentHelper.isClaimed(player)) completed.add(HarbingerChapterMilestone.FIRST_SEPARATION);
		if (HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(player)) completed.add(HarbingerChapterMilestone.WOVEN_VESSEL);
		if (HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player)) completed.add(HarbingerChapterMilestone.VEIN_MASON);
		if (HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE)) {
			completed.add(HarbingerChapterMilestone.COVENANT_WRITTEN_IN_PLACE);
		}
		if (HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE)) {
			completed.add(HarbingerChapterMilestone.LIVING_COVENANT);
		}
		return completed;
	}

	public static HarbingerChapterMilestone unmetChapterForTargetDegree(ServerPlayer player, int targetDegree) {
		HarbingerChapterMilestone required = HarbingerChapterMilestone.requiredForTargetDegree(targetDegree);
		return required != null && !completedChapters(player).contains(required) ? required : null;
	}

	public static void migrateExistingProgress(ServerPlayer player) {
		if (HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE)) return;

		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine()).orElse(Bloodline.NOBLOODLINE);
		if (!bloodline.isValid() || !player.getUUID().equals(bloodline.getLeaderUUID())) return;

		for (ServerLevel level : player.getServer().getAllLevels()) {
			if (FoundingFaneSavedData.get(level).hasFane(player.getUUID())) {
				HarbingerAdvancementGranter.grantIfNotDone(player,
						HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE);
				return;
			}
		}
	}

	public static void tryCompleteLivingCovenant(ServerPlayer player) {
		if (HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED)
				&& HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND)
				&& HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE);
		}
	}
}

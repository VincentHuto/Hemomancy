package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.AnchoriteAssignmentProgression;
import net.minecraft.server.level.ServerPlayer;

public record AnchoriteProgressSnapshot(int degree, boolean activeBlood, boolean purifying, boolean clarity,
        boolean firstLesson, boolean firstScarLearned, boolean firstEffigyPattern, boolean firstEffigyLoadout,
        boolean d4Reward, boolean d5Varicose, boolean d5Diagnosed, boolean d5Treated, boolean fortified,
        boolean d5Ready, boolean d5Reward, boolean d6Referral, boolean d6Counsel, boolean d6FirstRoute,
        boolean d6Loadout, boolean d6SecondRoute, boolean d6Ready, boolean d6Reward, int replacementTier) {
    public static AnchoriteProgressSnapshot from(ServerPlayer player) {
        return new AnchoriteProgressSnapshot(HemoCapabilityAccess.getPlayerDegreeNumber(player),
                HemoCapabilityAccess.getBloodVolume(player).map(volume -> volume.isActive()).orElse(false),
                HemoCapabilityAccess.getUnstainedProgress(player).map(progress -> progress.hasBegunPurification()).orElse(false),
                HemoCapabilityAccess.getUnstainedProgress(player).map(progress -> progress.hasClarityUnlocked()).orElse(false),
                HarbingerAdvancementGranter.isVeinMasonFirstLesson(player),
                HarbingerAdvancementGranter.isVeinMasonFirstScarLearned(player),
                HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(player),
                HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player),
                HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player),
                has(player, AnchoriteAssignmentProgression.D5_VARICOSE), has(player, AnchoriteAssignmentProgression.D5_DIAGNOSED),
                has(player, AnchoriteAssignmentProgression.D5_TREATED),
                HemoCapabilityAccess.getInitiatoryDegree(player).map(degree -> degree.hasHematicFortification()).orElse(false),
                has(player, AnchoriteAssignmentProgression.D5_READY), has(player, AnchoriteAssignmentProgression.D5_REWARD),
                has(player, AnchoriteAssignmentProgression.D6_REFERRAL), has(player, AnchoriteAssignmentProgression.D6_COUNSEL),
                has(player, AnchoriteAssignmentProgression.D6_FIRST_ROUTE), has(player, AnchoriteAssignmentProgression.D6_LOADOUT),
                has(player, AnchoriteAssignmentProgression.D6_SECOND_ROUTE), has(player, AnchoriteAssignmentProgression.D6_READY),
                has(player, AnchoriteAssignmentProgression.D6_REWARD), replacementTier(player));
    }

	private static int replacementTier(ServerPlayer player) {
		if (has(player, AnchoriteAssignmentProgression.D6_REWARD)
				&& VeinMasonScarLesson.needsReplacement(player, VeinMasonScarLesson.strongestForPlayer(player, 3))) return 3;
		if (has(player, AnchoriteAssignmentProgression.D5_REWARD)
				&& VeinMasonScarLesson.needsReplacement(player, VeinMasonScarLesson.strongestForPlayer(player, 2))) return 2;
		if (HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player)
				&& VeinMasonScarLesson.needsReplacement(player, VeinMasonScarLesson.continuationForPlayer(player))) return 1;
		if (HarbingerAdvancementGranter.isVeinMasonFirstLesson(player)
				&& VeinMasonScarLesson.needsReplacement(player, VeinMasonScarLesson.forPlayer(player))) return 1;
		return 0;
	}

    private static boolean has(ServerPlayer player, net.minecraft.resources.ResourceLocation id) {
        return HarbingerAdvancementGranter.hasAdvancement(player, id);
    }
}

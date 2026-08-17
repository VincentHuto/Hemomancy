package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class NoeticDiscoveryProgression {
    private static final String CONDUCTIVE_MARK_RECIPE = "memory_weaving/memory_conductive_mark";

    private NoeticDiscoveryProgression() {
    }

    public static boolean recognizeFromAlchemist(ServerPlayer player) {
        return recognize(player,
                HarbingerAdvancementGranter.hasAdvancement(player,
                        HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_NEUROTIC),
                false);
    }

    public static boolean recognizeFromMnemonist(ServerPlayer player) {
        return recognize(player, false,
                HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(player));
    }

    public static boolean isRecognized(ServerPlayer player) {
        return HarbingerAdvancementGranter.hasAdvancement(player,
                HarbingerAdvancementGranter.ADV_NOETIC_CONDUCTIVE_MARK_RECOGNIZED);
    }

    private static boolean recognize(ServerPlayer player, boolean hasDuctilisRecord, boolean completedFirstWeave) {
        if (isRecognized(player) || !NoeticDiscoveryRules.canRecognizeConductiveMark(
                HemoCapabilityAccess.getPlayerDegreeNumber(player), hasDuctilisRecord, completedFirstWeave)) {
            return false;
        }
        HarbingerAdvancementGranter.grantIfNotDone(player,
                HarbingerAdvancementGranter.ADV_NOETIC_CONDUCTIVE_MARK_RECOGNIZED);
        player.server.getRecipeManager().byKey(Hemomancy.rloc(CONDUCTIVE_MARK_RECIPE))
                .ifPresent(recipe -> player.awardRecipes(List.of(recipe)));
        return isRecognized(player);
    }
}

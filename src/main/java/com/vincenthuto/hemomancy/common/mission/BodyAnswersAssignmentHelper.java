package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class BodyAnswersAssignmentHelper {
    public static final ResourceLocation ADV_BRIEFED = Hemomancy.rloc("hemomancy/body_answers_briefed");
    public static final ResourceLocation ADV_COMPLETE = Hemomancy.rloc("hemomancy/body_answers_complete");

    private BodyAnswersAssignmentHelper() {
    }

    public static boolean canBrief(ServerPlayer player) {
        return FirstSeparationAssignmentHelper.isClaimed(player)
                && !HarbingerAdvancementGranter.hasAdvancement(player, ADV_BRIEFED);
    }

    public static boolean markBriefed(ServerPlayer player) {
        HarbingerAdvancementGranter.grantIfNotDone(player, ADV_BRIEFED);
        return HarbingerAdvancementGranter.hasAdvancement(player, ADV_BRIEFED);
    }

    public static void giveBriefingSupplies(ServerPlayer player) {
        for (ItemStack stack : List.of(
                new ItemStack(ItemInit.sanguine_formation.get()),
                new ItemStack(ItemInit.fervent_enzyme.get()),
                new ItemStack(ItemInit.bloody_flask.get()))) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    public static void markComplete(ServerPlayer player) {
        if (HarbingerAdvancementGranter.hasAdvancement(player, ADV_BRIEFED)) {
            HarbingerAdvancementGranter.grantIfNotDone(player, ADV_COMPLETE);
        }
    }
}

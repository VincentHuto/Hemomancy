package com.vincenthuto.hemomancy.common.mission.mnemonist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class MnemonicReliquaryProgression {
    public static final ResourceLocation ADV_REFERRAL =
            Hemomancy.rloc("hemomancy/mnemonic_reliquary_referral");
    public static final ResourceLocation ADV_TAUGHT =
            Hemomancy.rloc("hemomancy/mnemonic_reliquary_taught");

    private MnemonicReliquaryProgression() {}

    public static void onCapacityChanged(ServerPlayer player, IKnownManipulations known) {
        int capacity = ManipSlotHelper.getMaxSlots(player);
        int used = ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames());
        if (capacity <= 0 || used < capacity || HarbingerAdvancementGranter.hasAdvancement(player, ADV_REFERRAL)) {
            return;
        }
        HarbingerAdvancementGranter.grantIfNotDone(player, ADV_REFERRAL);
        player.displayClientMessage(Component.translatable("message.hemomancy.mnemonic_reliquary.referral")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
    }

    public static void teach(ServerPlayer player) {
        if (HarbingerAdvancementGranter.hasAdvancement(player, ADV_TAUGHT)) return;
        HarbingerAdvancementGranter.grantIfNotDone(player, ADV_TAUGHT);
        player.displayClientMessage(Component.translatable("message.hemomancy.mnemonic_reliquary.taught")
                .withStyle(ChatFormatting.DARK_RED), false);
    }

    public static boolean isTaught(ServerPlayer player) {
        return HarbingerAdvancementGranter.hasAdvancement(player, ADV_TAUGHT);
    }
}

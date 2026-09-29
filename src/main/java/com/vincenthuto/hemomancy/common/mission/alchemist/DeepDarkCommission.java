package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class DeepDarkCommission {
    private static final String DATA = "hemomancy:deep_dark_commission";
    private static final String REPORTED = "Reported";

    private DeepDarkCommission() {
    }

    public static boolean eligible(ServerPlayer player) {
        ItemInquiryContext context = ItemInquiryContext.from(player);
        return context.degree() >= 5 && context.activeBlood() && !context.purifying()
                && !context.clarityUnlocked();
    }

    public static DeepDarkCommissionProgress progress(ServerPlayer player) {
        boolean reported = data(player).getBoolean(REPORTED);
        return new DeepDarkCommissionProgress(reported
                || DeepDarkCommissionRules.hasSampleProof(HemoCapabilityAccess.antecedent(player)), reported);
    }

    public static boolean report(ServerPlayer player, Entity teacher) {
        if (!(teacher instanceof HarbingerAlchemistEntity) || !EarlyInitiation.near(player, teacher)
                || !eligible(player) || !progress(player).ready()) return false;
        CompoundTag data = data(player);
        data.putBoolean(REPORTED, true);
        save(player, data);
        player.displayClientMessage(Component.translatable("hemomancy.alchemist.deep_dark_commission.recorded")
                .withStyle(ChatFormatting.DARK_RED), false);
        return true;
    }

    private static CompoundTag data(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(DATA);
    }

    private static void save(ServerPlayer player, CompoundTag data) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(DATA, data);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }
}

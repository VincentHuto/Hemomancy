package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MorphlingHandlingAssignment {
    private static final String DATA = "hemomancy:morphling_handling";
    private static final String INSPECTED = "Inspected";
    private static final ResourceLocation POLYP = Hemomancy.rloc("morphling_polyp");

    private MorphlingHandlingAssignment() {}

    public static boolean eligible(ServerPlayer player) {
        ItemInquiryContext context = ItemInquiryContext.from(player);
        return context.degree() >= 4 && context.activeBlood() && !context.purifying()
                && !context.clarityUnlocked();
    }

    public static MorphlingHandlingProgress progress(ServerPlayer player) {
        boolean inspected = data(player).getBoolean(INSPECTED);
        boolean priorRecord = HemoCapabilityAccess.getSpecimenBestiary(player)
                .map(bestiary -> bestiary.hasRecordedSpecimen(POLYP))
                .orElse(false);
        return new MorphlingHandlingProgress(inspected || priorRecord || isPolypJar(player.getMainHandItem()),
                inspected);
    }

    public static boolean isPolypJar(ItemStack stack) {
        return stack.is(BlockInit.specimen_jar.get().asItem())
                && SpecimenJarData.getSpecimenEntityId(stack).filter(POLYP::equals).isPresent();
    }

    public static boolean inspect(ServerPlayer player, Entity teacher) {
        if (!(teacher instanceof HarbingerAlchemistEntity) || !EarlyInitiation.near(player, teacher)
                || !eligible(player) || !progress(player).ready()) return false;
        CompoundTag data = data(player);
        data.putBoolean(INSPECTED, true);
        save(player, data);
        player.displayClientMessage(Component.translatable("hemomancy.alchemist.morphling_handling.recorded")
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

package com.vincenthuto.hemomancy.common.mission.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class VagrantMindGuidance {
    private static final TagKey<Structure> TARGETS = TagKey.create(Registries.STRUCTURE,
            Hemomancy.rloc("vagrant_mind_targets"));

    private VagrantMindGuidance() {}

    public static boolean tell(ServerPlayer player, Entity teacher) {
        if (!(teacher instanceof HarbingerMnemonistEntity) || !EarlyInitiation.near(player, teacher)
                || !VagrantMindInquiry.eligible(player)) return false;
        ServerLevel end = player.server.getLevel(Level.END);
        if (end == null) return false;
        BlockPos from = Level.END.equals(player.level().dimension())
                ? player.blockPosition() : new BlockPos(1024, 64, 0);
        BlockPos found = end.findNearestMapStructure(TARGETS, from, 512, false);
        Component message = found == null
                ? Component.translatable("hemomancy.vagrant_mind_inquiry.bearing_missing")
                : Component.translatable("hemomancy.vagrant_mind_inquiry.bearing", found.getX(), found.getZ());
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.DARK_RED), false);
        return true;
    }
}

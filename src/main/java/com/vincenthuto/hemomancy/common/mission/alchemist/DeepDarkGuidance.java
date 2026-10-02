package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;

public final class DeepDarkGuidance {
    private DeepDarkGuidance() {}

    public static boolean tell(ServerPlayer player, Entity teacher) {
        if (!(teacher instanceof HarbingerAlchemistEntity) || !EarlyInitiation.near(player, teacher)
                || !DeepDarkCommission.eligible(player) || DeepDarkCommission.progress(player).reported()) return false;
        if (!Level.OVERWORLD.equals(player.level().dimension())) {
            reply(player, "bearing_overworld");
            return true;
        }
        var from = new BlockPos(player.blockPosition().getX(), -48, player.blockPosition().getZ());
        var found = player.serverLevel().findClosestBiome3d(biome -> biome.is(Biomes.DEEP_DARK),
                from, 2048, 32, 32);
        if (found == null) {
            reply(player, "bearing_missing");
        } else {
            BlockPos target = found.getFirst();
            reply(player, "bearing", target.getX(), target.getY(), target.getZ());
        }
        return true;
    }

    private static void reply(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(
                "hemomancy.alchemist.deep_dark_commission." + key, args)
                .withStyle(ChatFormatting.DARK_RED), false);
    }
}

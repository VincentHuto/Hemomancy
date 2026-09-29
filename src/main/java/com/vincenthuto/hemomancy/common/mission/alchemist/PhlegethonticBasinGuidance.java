package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class PhlegethonticBasinGuidance {
    private PhlegethonticBasinGuidance() {
    }

    public static String direction(int dx, int dz) {
        if (dx == 0 && dz == 0) return "here";
        if (Math.abs((long) dx) > Math.abs((long) dz) * 2) return dx < 0 ? "west" : "east";
        if (Math.abs((long) dz) > Math.abs((long) dx) * 2) return dz < 0 ? "north" : "south";
        return (dz < 0 ? "north" : "south") + (dx < 0 ? "west" : "east");
    }

    public static boolean tell(ServerPlayer player, Entity teacher) {
        if (!(teacher instanceof HarbingerAlchemistEntity) || !EarlyInitiation.near(player, teacher)
                || !PhlegethonticCommission.eligible(player)) return false;
        ServerLevel nether = player.server.getLevel(Level.NETHER);
        if (nether == null) return false;
        if (player.level().dimension() == Level.END) {
            player.displayClientMessage(Component.translatable(
                    "hemomancy.alchemist.phlegethontic_commission.bearing_end")
                    .withStyle(ChatFormatting.DARK_RED), false);
            return true;
        }
        int x = player.level().dimension() == Level.NETHER
                ? player.blockPosition().getX() : Math.floorDiv(player.blockPosition().getX(), 8);
        int z = player.level().dimension() == Level.NETHER
                ? player.blockPosition().getZ() : Math.floorDiv(player.blockPosition().getZ(), 8);
        var found = nether.findClosestBiome3d(biome -> biome.is(BiomeInit.PHLEGETHONTIC_BASIN),
                new BlockPos(x, 55, z), 8192, 32, 64);
        if (found == null) {
            player.displayClientMessage(Component.translatable(
                    "hemomancy.alchemist.phlegethontic_commission.bearing_missing")
                    .withStyle(ChatFormatting.DARK_RED), false);
            return true;
        }
        int dx = found.getFirst().getX() - x;
        int dz = found.getFirst().getZ() - z;
        int distance = (int) Math.round(Math.hypot(dx, dz));
        player.displayClientMessage(Component.translatable(
                "hemomancy.alchemist.phlegethontic_commission.bearing",
                distance,
                Component.translatable("hemomancy.alchemist.phlegethontic_commission.direction."
                        + direction(dx, dz))).withStyle(ChatFormatting.DARK_RED), false);
        return true;
    }
}

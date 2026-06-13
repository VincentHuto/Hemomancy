package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class HematicMemoryToolItem extends Item {
    private final int requiredDegree;

    public HematicMemoryToolItem(Properties properties, int requiredDegree) {
        super(properties.stacksTo(1));
        this.requiredDegree = requiredDegree;
    }

    protected boolean canUseHematicTool(Player player) {
        if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < requiredDegree) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("item.hemomancy.hematic_memory_tool.degree",
                        requiredDegree).withStyle(ChatFormatting.DARK_RED), true);
            }
            return false;
        }
        return HemoCapabilityAccess.getBloodVolume(player).map(IBloodVolume::isActive).orElse(false);
    }

    protected boolean canUseHematicToolSilently(Player player) {
        return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= requiredDegree
                && HemoCapabilityAccess.getBloodVolume(player).map(IBloodVolume::isActive).orElse(false);
    }

    protected boolean drainBlood(Player player, int cost) {
        return HemoCapabilityAccess.getBloodVolume(player).map(volume -> {
            if (!volume.isActive() || volume.getBloodVolume() < cost) {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(Component.translatable("item.hemomancy.hematic_memory_tool.blood")
                            .withStyle(ChatFormatting.DARK_RED), true);
                }
                return false;
            }
            volume.drain(cost);
            if (player instanceof ServerPlayer serverPlayer) {
                BloodVolumeEvents.syncVolume(serverPlayer, volume);
            }
            return true;
        }).orElse(false);
    }
}

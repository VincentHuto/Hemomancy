package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class HematicMemoryToolItem extends Item {
    private static final BloodAccounting.Source MEMORY_TOOL = BloodAccounting.Source.of("hematic_memory_tool", Category.TOOL, false, false);
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
        return HemoCapabilityAccess.getBloodVolume(player).isActive();
    }

    protected boolean canUseHematicToolSilently(Player player) {
        return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= requiredDegree
                && HemoCapabilityAccess.getBloodVolume(player).isActive();
    }

    protected boolean drainBlood(Player player, int cost) {
        var volume = HemoCapabilityAccess.getBloodVolume(player);
        if (!volume.isActive() || volume.getBloodVolume() < cost) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("item.hemomancy.hematic_memory_tool.blood")
                        .withStyle(ChatFormatting.DARK_RED), true);
            }
            return false;
        }
        BloodAccounting.pay(player, cost, MEMORY_TOOL);
        return true;
    }
}

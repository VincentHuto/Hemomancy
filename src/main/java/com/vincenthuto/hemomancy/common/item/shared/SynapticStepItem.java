package com.vincenthuto.hemomancy.common.item.shared;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

/** Retained registry identity for old saves. Crafting dismantles the inert tool into its ganglion. */
public final class SynapticStepItem extends Item {
    public SynapticStepItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hemomancy.synaptic_step.tooltip").withStyle(ChatFormatting.GRAY));
    }
}

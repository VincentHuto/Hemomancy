package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class VivianiteScalpelItem extends Item {

    public VivianiteScalpelItem(Properties prop) {
        super(prop);
        prop.stacksTo(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable("item.hemomancy.vivianite_scalpel.tooltip1").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.hemomancy.vivianite_scalpel.tooltip2").withStyle(ChatFormatting.DARK_GRAY));
    }

}

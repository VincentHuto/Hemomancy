package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class HemorathRibItem extends BloodGourdItem {

    public HemorathRibItem(EnumBloodGourdTiers tierIn) {
        super(new Properties().stacksTo(1).durability(4), tierIn);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("tooltip.hemomancy.hemorath_rib_bonus").withStyle(ChatFormatting.GOLD));
    }
}

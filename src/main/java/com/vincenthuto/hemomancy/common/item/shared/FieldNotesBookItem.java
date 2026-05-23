package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class FieldNotesBookItem extends ItemGuideBook {
    public FieldNotesBookItem(Properties prop, ResourceLocation loc) {
        super(prop, loc);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.hemomancy.field_notes.tooltip.curio")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.hemomancy.field_notes.tooltip.legacy")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}

package com.vincenthuto.hemomancy.radial;

import java.util.List;

import com.vincenthuto.hemomancy.init.KeyBindInit;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public interface IRadialMenuItem {
    default public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        String txt = I18n.get((String)RadialClientEvents.OPEN_TOOL_MENU_KEYBIND.getKey().getDisplayName().getString(), (Object[])new Object[0]);
        tooltip.add((Component)new TranslatableComponent("item.mna.item-with-gui.radial-open", new Object[]{txt}).withStyle(ChatFormatting.AQUA));
    }
}
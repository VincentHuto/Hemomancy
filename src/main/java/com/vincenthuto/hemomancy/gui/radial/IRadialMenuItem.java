package com.vincenthuto.hemomancy.gui.radial;

import java.util.List;

import com.vincenthuto.hemomancy.init.KeyBindInit;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public interface IRadialMenuItem {
	default public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		String txt = I18n.get(KeyBindInit.OPEN_CHARM_SLOT_KEYBIND.getKey().getDisplayName().getString());
		tooltip.add(Component.literal(txt).withStyle(ChatFormatting.AQUA));
	}
}
package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Hematic Cleats (CN-10): bone spurs and studs on a hematic iron sole plate, bound with sinew. Not worn. Set in a
 * Hematic Armature's bowl, they are fitted to the boots the restrained player wears ({@link CleatsFitting}).
 */
public class HematicCleatsItem extends Item {
	public HematicCleatsItem() {
		super(new Item.Properties().fireResistant().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.hemomancy.hematic_cleats").withStyle(ChatFormatting.DARK_RED));
	}
}

package com.vincenthuto.hemomancy.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemDSD extends Item {

	public ItemDSD(Properties prop) {
		super(prop);
		prop.stacksTo(1);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent("Also known as a D.S.D. used to"));
		tooltip.add(new TextComponent("commandeer Drudges to your will."));
	}

}

package com.vincenthuto.hemomancy.item.tool.living;

import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemLivingItem extends Item {

	public ItemLivingItem(Properties properties) {
		super(properties);
	}
	@Override
	public Component getName(ItemStack stack) {
		return  Component.translatable(
				HLTextUtils.stringToBloody(HLTextUtils.convertInitToLang(ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath()))).withStyle(ChatFormatting.DARK_RED);
	}
	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}
}

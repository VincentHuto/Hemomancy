package com.vincenthuto.hemomancy.item.tool.living;

import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LivingItemItem extends Item {

	public LivingItemItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return new TextComponent(
				HLTextUtils.stringToBloody(HLTextUtils.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}
}

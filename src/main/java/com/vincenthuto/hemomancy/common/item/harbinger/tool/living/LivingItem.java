package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class LivingItem extends Item {

	public LivingItem(Properties properties) {
		super(properties);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return Component
				.literal(HLTextUtils.stringToBloody(
						HLTextUtils.convertInitToLang(HLTextUtils.getItemRegistryName(stack.getItem()))))
				.withStyle(ChatFormatting.DARK_RED);
	}
}

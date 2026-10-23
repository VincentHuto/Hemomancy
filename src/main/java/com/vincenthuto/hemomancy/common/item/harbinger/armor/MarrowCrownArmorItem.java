package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class MarrowCrownArmorItem extends ArmorItem {

	public MarrowCrownArmorItem(Holder<ArmorMaterial> materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.marrow_crown_bonus").withStyle(ChatFormatting.GOLD));
	}

}

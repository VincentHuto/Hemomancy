package com.vincenthuto.hemomancy.common.item.shared.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class VenousStriderSabatonsItem extends ArmorItem {
	public VenousStriderSabatonsItem(Holder<ArmorMaterial> material, Type slot) {
		super(material, slot, new Item.Properties().fireResistant().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context,
			List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.hemomancy.venous_strider_sabatons")
				.withStyle(ChatFormatting.DARK_GREEN));
	}

}

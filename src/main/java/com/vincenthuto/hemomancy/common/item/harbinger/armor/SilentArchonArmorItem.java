package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class SilentArchonArmorItem extends ArmorItem {
	public SilentArchonArmorItem(Holder<ArmorMaterial> material, Type slot) {
		super(material, slot, new Item.Properties().fireResistant().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context,
			List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.hemomancy.silent_archon_set_bonus")
				.withStyle(ChatFormatting.DARK_PURPLE));
		tooltip.add(Component.translatable("tooltip.hemomancy.silent_archon_incorporeal")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.hemomancy.silent_archon_slipping")
				.withStyle(ChatFormatting.GRAY));
	}

}

package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentType;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Worn in the Scarlet Vanity's bandolier slot, it shows the Temper vials on the hematic HUD. */
public class TitrationBandolierItem extends Item implements IHarbingerEquipment {
	public TitrationBandolierItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public HarbingerEquipmentType getHarbingerEquipmentType() {
		return HarbingerEquipmentType.BANDOLIER;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.titration_bandolier.tooltip").withStyle(ChatFormatting.DARK_GRAY));
	}
}

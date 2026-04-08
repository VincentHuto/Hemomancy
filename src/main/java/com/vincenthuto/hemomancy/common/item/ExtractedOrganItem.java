package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * An extracted organ item produced by the Visceral Mirror ritual.
 * These items represent organs pulled from the player's own reflection,
 * imbued with sanguine potential for further modification.
 */
public class ExtractedOrganItem extends Item {

	private final EnumOrgan organ;

	public ExtractedOrganItem(Properties prop, EnumOrgan organ) {
		super(prop.stacksTo(1).rarity(Rarity.EPIC));
		this.organ = organ;
	}

	public EnumOrgan getOrgan() {
		return organ;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal(organ.getDescription()).withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.literal("Tier " + organ.getTier() + " Visceral Component")
				.withStyle(ChatFormatting.DARK_PURPLE));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return organ == EnumOrgan.HEART;
	}
}

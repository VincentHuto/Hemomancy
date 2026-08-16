package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class RecycledEnzymeItem extends Item {

	public RecycledEnzymeItem() {
		super(new Item.Properties());
	}

	public float getAmount(ItemStack stack) {
		return identity(stack).potency();
	}

	public EnumBloodTendency getTend(ItemStack stack) {
		return EnumBloodTendency.values()[identity(stack).tendencyIndex()];
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!level.isClientSide) {
			identity(stack);
		}
		super.inventoryTick(stack, level, entity, slot, selected);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.contains("RecycledEnzymeSeed")) {
			tooltip.add(Component.literal("Unsettled enzyme; resolves when carried").withStyle(ChatFormatting.GRAY));
			return;
		}
		RecycledEnzymeIdentity identity = RecycledEnzymeIdentity.fromSeed(tag.getLong("RecycledEnzymeSeed"));
		String tendency = EnumBloodTendency.values()[identity.tendencyIndex()].name().toLowerCase();
		tooltip.add(Component.literal("Recovered tendency: " + tendency).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Low-grade potency: " + identity.potency()).withStyle(ChatFormatting.DARK_RED));
	}

	private static RecycledEnzymeIdentity identity(ItemStack stack) {
		var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.contains("RecycledEnzymeSeed")) {
			tag.putLong("RecycledEnzymeSeed", RandomSource.create().nextLong());
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
		return RecycledEnzymeIdentity.fromSeed(tag.getLong("RecycledEnzymeSeed"));
	}

}

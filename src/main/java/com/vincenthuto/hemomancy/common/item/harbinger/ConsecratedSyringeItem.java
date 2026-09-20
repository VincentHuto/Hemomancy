package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Consecrated Syringe — produced by offering a filled Blood Vial to a Saint Sarcophagus.
 * Carries the saint type in NBT. Process it in a Vial Centrifuge to extract Hallowed Residuum.
 */
public class ConsecratedSyringeItem extends Item {

	public static final String TAG_SAINT_TYPE = "saint_type";

	public static EnumSaintType getSaintType(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData != null) {
			CompoundTag tag = customData.copyTag();
			if (tag.contains(TAG_SAINT_TYPE)) {
				try {
					return EnumSaintType.valueOf(tag.getString(TAG_SAINT_TYPE));
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}
		return null;
	}

	public ConsecratedSyringeItem(Properties properties) {
		super(properties);
	}

    /** Used by creative and JEI; sarcophagus rewards still overwrite this with their actual saint. */
    public ItemStack randomSaintStack() {
        ItemStack stack = new ItemStack(this);
        setSaintType(stack, EnumSaintType.values()[ThreadLocalRandom.current().nextInt(EnumSaintType.values().length)]);
        return stack;
    }

    @Override public ItemStack getDefaultInstance() { return randomSaintStack(); }

    public static void setSaintType(ItemStack stack, EnumSaintType saint) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(TAG_SAINT_TYPE, saint.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		EnumSaintType saint = getSaintType(stack);
		if (saint != null) {
			tooltip.add(Component.literal("Consecrated by Saint " + saint.getDisplayName() + ".")
					.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
		} else {
			tooltip.add(Component.literal("A syringe blessed by forgotten rites.")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
		tooltip.add(Component.literal("Process in a Vial Centrifuge to extract Hallowed Residuum.")
				.withStyle(ChatFormatting.DARK_PURPLE));
	}
}

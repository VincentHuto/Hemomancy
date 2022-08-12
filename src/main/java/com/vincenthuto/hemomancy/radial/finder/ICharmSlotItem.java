package com.vincenthuto.hemomancy.radial.finder;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public interface ICharmSlotItem {
	@Nonnull
	default ImmutableSet<ResourceLocation> getAcceptableSlots(@Nonnull ItemStack stack) {
		return CharmSlotCapability.ANY_SLOT_LIST;
	}

	default void onWornTick(@Nonnull ItemStack stack, @Nonnull ICharmSlot slot) {
	}

	default void onEquipped(@Nonnull ItemStack stack, @Nonnull ICharmSlot slot) {
	}

	default void onUnequipped(@Nonnull ItemStack stack, @Nonnull ICharmSlot slot) {
	}

	default boolean canEquip(@Nonnull ItemStack stack, @Nonnull ICharmSlot slot) {
		return true;
	}

	default boolean canUnequip(@Nonnull ItemStack stack, @Nonnull ICharmSlot slot) {
		return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, stack) <= 0;
	}
}

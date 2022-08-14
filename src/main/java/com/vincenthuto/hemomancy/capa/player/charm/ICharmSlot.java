package com.vincenthuto.hemomancy.capa.player.charm;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.vincenthuto.hemomancy.container.ICharmMenu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public interface ICharmSlot {
	@Nonnull
	ICharmMenu getContainer();

	@Nonnull
	ResourceLocation getType();

	@Nonnull
	ItemStack getContents();

	void setContents(@Nonnull ItemStack stack);

	void onCharmContentsChanged();

	default boolean canEquip(@Nonnull ItemStack stack) {
		return stack.getCapability(CharmSlotCapability.INSTANCE, null)
				.map((extItem) -> ICharmSlot.isAcceptableSlot(this, stack, extItem) && extItem.canEquip(stack, this))
				.orElse(false);
	}

	default boolean canUnequip(@Nonnull ItemStack stack) {
		return stack.getCapability(CharmSlotCapability.INSTANCE, null)
				.map((extItem) -> extItem.canUnequip(stack, this)
						&& EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, stack) <= 0)
				.orElse(true);
	}

	static boolean isAcceptableSlot(@Nonnull ICharmSlot slot, @Nonnull ItemStack stack,
			@Nonnull ICharmSlotItem extItem) {
		ImmutableSet<ResourceLocation> slots = extItem.getAcceptableSlots(stack);
		return slots.contains(CharmSlotCapability.ANY_SLOT) || slots.contains(slot.getType());
	}
}

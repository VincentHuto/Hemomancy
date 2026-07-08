package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.List;

public record LivingWeaponGraftJeiRecipe(LivingWeaponForm form) {
	public ItemStack livingStaff() {
		return new ItemStack(ItemInit.living_staff.get());
	}

	public ItemStack ironBrazier() {
		return new ItemStack(BlockInit.iron_brazier.get());
	}

	public ItemStack graft() {
		return LivingWeaponGraftData.createStack(form);
	}

	public ItemStack output() {
		return new ItemStack(outputHolder(form).get());
	}

	public static List<LivingWeaponGraftJeiRecipe> all() {
		return Arrays.stream(LivingWeaponForm.values()).map(LivingWeaponGraftJeiRecipe::new).toList();
	}

	private static DeferredHolder<Item, Item> outputHolder(LivingWeaponForm form) {
		return switch (form) {
		case BLADE -> ItemInit.living_blade;
		case AXE -> ItemInit.living_axe;
		case SPEAR -> ItemInit.living_spear;
		case CLAWS -> ItemInit.living_baghnakh;
		case CROSSBOW -> ItemInit.living_crossbow;
		case TORCH -> ItemInit.living_torch;
		case FLAIL -> ItemInit.living_flail;
		};
	}
}

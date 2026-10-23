package com.vincenthuto.hemomancy.common.entity.projectile;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * A projectile that remembers the weapon that fired it, so its hit is attributed to that weapon's school and
 * tendency. O-2: the copy and the saved {@code "CombatWeapon"} stack go through the helpers here.
 */
public interface CombatWeaponCarrierProjectile {
	String NBT_KEY = "CombatWeapon";

	ItemStack getCombatWeaponItem();

	/** A private copy of the firing weapon, or empty. */
	static ItemStack copyOf(@Nullable ItemStack weapon) {
		return weapon != null && !weapon.isEmpty() ? weapon.copy() : ItemStack.EMPTY;
	}

	static void save(CompoundTag tag, ItemStack weapon, HolderLookup.Provider registries) {
		if (!weapon.isEmpty()) {
			tag.put(NBT_KEY, weapon.save(registries));
		}
	}

	static ItemStack load(CompoundTag tag, HolderLookup.Provider registries) {
		return tag.contains(NBT_KEY, 10) ? ItemStack.parseOptional(registries, tag.getCompound(NBT_KEY)) : ItemStack.EMPTY;
	}
}

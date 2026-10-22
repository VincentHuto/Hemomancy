package com.vincenthuto.hemomancy.common.util;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Full armour-set checks. A set is exact pieces per slot, or four pieces of one armour material. */
public final class ArmorSets {
	private ArmorSets() {
	}

	/** Whether each armour slot holds its piece. A null entity wears nothing. */
	public static boolean isFullSet(@Nullable LivingEntity entity, Item head, Item chest, Item legs, Item feet) {
		return isFullSet(entity, List.of(head), chest, legs, feet);
	}

	/** As {@link #isFullSet(LivingEntity, Item, Item, Item, Item)}, where any of {@code heads} completes the head. */
	public static boolean isFullSet(@Nullable LivingEntity entity, List<Item> heads, Item chest, Item legs, Item feet) {
		return entity != null
				&& wearsAny(entity.getItemBySlot(EquipmentSlot.HEAD), heads)
				&& entity.getItemBySlot(EquipmentSlot.CHEST).is(chest)
				&& entity.getItemBySlot(EquipmentSlot.LEGS).is(legs)
				&& entity.getItemBySlot(EquipmentSlot.FEET).is(feet);
	}

	/** Whether each listed slot holds its piece. Slots left out of {@code pieces} may hold anything. */
	public static boolean isFullSet(@Nullable LivingEntity entity, Map<EquipmentSlot, ? extends Supplier<? extends Item>> pieces) {
		if (entity == null) return false;
		for (Map.Entry<EquipmentSlot, ? extends Supplier<? extends Item>> piece : pieces.entrySet()) {
			if (!entity.getItemBySlot(piece.getKey()).is(piece.getValue().get())) return false;
		}
		return true;
	}

	/** Whether all four armour slots hold armour of this material, whatever the pieces. */
	public static boolean isFullSet(@Nullable LivingEntity entity, Holder<ArmorMaterial> material) {
		if (entity == null) return false;
		int count = 0;
		for (ItemStack stack : entity.getArmorSlots()) {
			if (stack.getItem() instanceof ArmorItem armor && armor.getMaterial().value() == material.value()) {
				count++;
			}
		}
		return count >= 4;
	}

	private static boolean wearsAny(ItemStack worn, List<Item> items) {
		for (Item item : items) {
			if (worn.is(item)) return true;
		}
		return false;
	}
}
